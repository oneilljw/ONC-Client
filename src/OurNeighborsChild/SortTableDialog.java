package OurNeighborsChild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public abstract class SortTableDialog extends ONCTableDialog implements ActionListener, DatabaseListener,
																				ListSelectionListener
{
	/**
	 * This class implements a base class blueprint for the dialogs in the ONC application that allow
	 * the user display data in a table based on search criteria. Once data is present in the table,
	 * the user may select row(s) from the table and change the data present. This base class implements
	 * the dialog with five panels, three of which are exposed to an inheriting class for adding
	 * custom GUI elements. The base class provides a search criteria panel with two sub-panels, a
	 * table that displays information about the ONCEntity being managed, a count panel displaying 
	 * the number of items in the table, a change data panel that allows the user to change select
	 * ONCEntity data by selecting the ONCEntity(s) from the table and using the change data panel
	 * GUI, a control panel that allows an inheriting class to add custom GUI controls and a bottom
	 * panel that implements common reset criteria and apply changes buttons. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_ROWS_TO_DISPLAY = 15;

	protected JFrame parentFrame;
	protected GlobalVariables oncGVs;
	protected Families fDB;
	protected ChildDB cDB;
	protected ChildWishDB cwDB;
//	protected ArrayList<ONCFamily> stAL; //Holds reference to ONCFamily objects shown in table
	protected DriverDB driverDB;
	protected DeliveryDB deliveryDB;
	protected ONCRegions regions;
	
	//sort column and list of selected table rows
	protected int tableSortCol;
	protected ArrayList<Integer> tableRowSelectedItemIDList;
	
	//JPanels the inherited class may use to add GUI elements
	protected JPanel sortCriteriaPanelTop, sortCriteriaPanelBottom;
	protected JPanel itemCountPanel, changeDataPanel, cntlPanel;
	
	protected ONCTable sortTable;
	protected DefaultTableModel sortTableModel;
	protected JButton btnApplyChanges;
	protected JButton btnResetCriteria;
	protected JLabel lblNumOfTableItems;
	
	protected String sortONCNum = "";
	
	protected boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	protected boolean bIgnoreCBEvents = false;
	
	protected String[] famstatus = {"Any", "Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
	protected static String[] delstatus = {"Any", "Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
	protected static String[] stoplt = {"Any", "Green", "Yellow", "Red", "Off"};
	
	public SortTableDialog(JFrame pf, String[] colToolTips, String[] columns, int[] colWidths, int[] center_cols)
	{
		super(pf);
		oncGVs = GlobalVariables.getInstance();
		
		driverDB = DriverDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
		//initialize member variables
		sortONCNum = "";
		tableSortCol = -1;
		tableRowSelectedItemIDList = new ArrayList<Integer>();
		
		//Set up the search criteria panel      
		JPanel sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));
		sortCriteriaPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		//Create the ONC Icon label and add it to the search criteria panel
    	JLabel lblONCicon = new JLabel(oncGVs.getImageIcon(0));
        sortCriteriaPanelTop.add(lblONCicon);
		
		sortCriteriaPanel.add(sortCriteriaPanelTop);
		sortCriteriaPanel.add(sortCriteriaPanelBottom);
		
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
	
		//Set up the sort family table panel
		sortTable = new ONCTable(colToolTips, new Color(240,248,255));

		//Set up the table model. Cells are not editable
		sortTableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		};

		//Set the table model, select ability to select multiple rows and add a listener to 
		//check if the user has selected a row. 
		sortTable.setModel(sortTableModel);
		sortTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sortTable.getSelectionModel().addListSelectionListener(this);

		//Set table column widths
		int tablewidth = 0;
		for(int i=0; i < colWidths.length; i++)
		{
			sortTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			tablewidth += colWidths[i];
		}
		tablewidth += 24; 	//Account for vertical scroll bar

		//Set up the table header
		JTableHeader anHeader = sortTable.getTableHeader();
		anHeader.setForeground( Color.black);
		anHeader.setBackground( new Color(161,202,241));

		//mouse listener for table header click causes table to be sorted based on column selected
		//uses family data base sort method to sort. Method requires ONCFamily array list to be sorted
		//and column name
        anHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	int sortCol = sortTableList(sortTable.columnAtPoint(e.getPoint()));
            	if(sortCol > -1)
            		tableSortCol = sortCol;
            }
        });

		//Center cell entries for specified cells
	    DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
	    dtcr.setHorizontalAlignment(SwingConstants.CENTER);
	    for(int i=0; i<center_cols.length; i++)
	    	sortTable.getColumnModel().getColumn(center_cols[i]).setCellRenderer(dtcr);

		sortTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		sortTable.setFillsViewportHeight(true);

		//Create the scroll pane and add the table to it.
		JScrollPane sortScrollPane = new JScrollPane(sortTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		sortScrollPane.setPreferredSize(new Dimension(tablewidth, sortTable.getRowHeight()*NUM_ROWS_TO_DISPLAY));      
		
		//Now that we've created the sort table, set the preferred dimensions of the search panel
//		sortCriteriaPanelTop.setPreferredSize(new Dimension(sortTable.getWidth(), 64));
		
        //Set up the third panel holding count panel and change panel
        JPanel thirdPanel = new JPanel();
//        thirdPanel.setLayout(new BoxLayout(thirdPanel, BoxLayout.X_AXIS));
//        thirdPanel.setLayout(new BorderLayout());
        
        thirdPanel.setLayout( new GridBagLayout() ); 
        GridBagConstraints gbc = new GridBagConstraints();
        
        itemCountPanel = new JPanel();       
        lblNumOfTableItems = new JLabel("000000000000000");
//      itemCountPanel.setSize(new Dimension(180, 90));
        itemCountPanel.add(lblNumOfTableItems);
        itemCountPanel.setBorder(BorderFactory.createTitledBorder("Families Meeting Criteria"));
        
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 120;
        gbc.weightx = 0.1;
        thirdPanel.add(itemCountPanel, gbc);
        
        changeDataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//      changeDataPanel.setBorder( BorderFactory.createTitledBorder( eBorder, "" ) );
        gbc.gridx = 1;
        gbc.ipadx = 0;
//      gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.9;
//      gbc.insets = new Insets( 2, 2, 2, 2 );
        thirdPanel.add(changeDataPanel, gbc);
//      setSize( 500, 500 );
        
        
        
//        changeDataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        changeDataPanel.setSize(new Dimension(sortTable.getWidth()-180, 90));
 
		//Add the components to the third panel	
//		thirdPanel.add(itemCountPanel, gbc);
//		thirdPanel.add(changeDataPanel);
//		thirdPanel.setPreferredSize(new Dimension(sortTable.getWidth(), 90));
				
        //Set up the button control panel and bottom panel
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
           
        btnResetCriteria = new JButton("Reset Criteria");
        btnResetCriteria.addActionListener(this);  
        btnResetCriteria.addActionListener(new ActionListener() {
        	@Override
    		public void actionPerformed(ActionEvent e) {
    			if(e.getSource() == btnResetCriteria) { onResetCriteriaClicked(); }
    		}	
        });
 
        btnApplyChanges = new JButton("Apply Changes");
        btnApplyChanges.setEnabled(false);
        btnApplyChanges.addActionListener(this);
       
        bottomPanel.add(cntlPanel);
        bottomPanel.add(btnResetCriteria);
        bottomPanel.add(btnApplyChanges);
                
        //Add the four components to the dialog pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
        this.add(sortCriteriaPanel);
        this.add(sortScrollPane);
        this.add(thirdPanel);
        this.add(bottomPanel);
       
//      pack();
        setResizable(true);
	}
	
	abstract void buildTableList(boolean bPreserveSelections);
	
	abstract int sortTableList(int col);
	
	void archiveTableSelections(ArrayList<? extends ONCObject> stAL)
	{
		tableRowSelectedItemIDList.clear();
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
			tableRowSelectedItemIDList.add(stAL.get(row_sel[i]).getID());
	}
	
	/*****************************************************************************************
	 * Displays the contents of the sort table list in the ONC table. 
	 * @param stAL	- List of table rows to be displayed
	 * @param bSortReq - true: don't resort the table
	 ****************************************************************************************/
	void displaySortTable(ArrayList<? extends ONCObject> stAL, boolean bResort)
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		//clear any selections the user made
		ListSelectionModel lsModel = sortTable.getSelectionModel();
		lsModel.clearSelection();	//clear any selected rows
		
		//clear the table
		while (sortTableModel.getRowCount() > 0)
			sortTableModel.removeRow(0);
		
		setEnabledControls(false);	//disable any controls that are reset
		
		//add rows to the table
		for(int i=0; i < stAL.size(); i++)
			sortTableModel.addRow(getTableRow(stAL.get(i)));
		
		//check to see if the table needs to be sorted by column
		if(bResort && tableSortCol > -1)
			sortTableList(tableSortCol);
				
		//check to see if table rows need to be re-selected
		for(Integer itemID:tableRowSelectedItemIDList)
		{
			//find the id in the stAL, getting it's row, the reselect it
			int index = 0;
			while(index < stAL.size() && stAL.get(index).getID() != itemID)
				index++;
			
			if(index < stAL.size())
				lsModel.addSelectionInterval(index, index);	
		}
		
		//re-enable any controls if rows are still selected
		if(!tableRowSelectedItemIDList.isEmpty())
			setEnabledControls(true);
				
		bChangingTable = false;	
	}
	
	abstract void setEnabledControls(boolean tf);
	
	abstract String[] getTableRow(ONCObject o);
	
	void onPrintListing(String tablename)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(tablename);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             sortTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	abstract void checkApplyChangesEnabled();
	
	abstract boolean onApplyChanges();
	
	abstract void onResetCriteriaClicked();
	
	abstract boolean isONCNumContainerEmpty();
	
	boolean isNumeric(String s){ return s.matches("-?\\d+(\\.\\d+)?"); }
	
	/***********************************************************************************
	 * This class implements a key listener for the ReceiveGiftDialog class that
	 * listens to the ONC Number text field to determine when it is empty. If it becomes empty,
	 * the listener rebuilds the sort table array list
	 ***********************************************************************************/
	 protected class ONCNumberKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyReleased(KeyEvent arg0)
		{
			if(isONCNumContainerEmpty())
			{
				sortONCNum = "";
				buildTableList(false);
			}		
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
			
		}
	 }
}
