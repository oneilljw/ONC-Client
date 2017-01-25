package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
	 * table that displays information about ONCEntity's being managed, a count panel displaying 
	 * the number of ONCEntities in the table, a change data panel that allows the user to change
	 * select ONCEntity data by selecting the ONCEntity(s) from the table and using the change
	 * data panel GUI, a control panel that allows an inheriting class to add custom GUI controls
	 * and a bottom panel that implements common reset criteria and apply changes buttons. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int SORT_TABLE_VERTICAL_SCROLL_WIDTH = 24;

	protected FamilyDB fDB;
	protected UserDB userDB;
	
	//sort column
	protected int tableSortCol;
	
	//JPanels the inherited class may use to add GUI elements
	protected JPanel sortCriteriaPanel, sortCriteriaPanelTop, sortCriteriaPanelBottom;
	protected JPanel bottomPanel;
	
	protected ArrayList<ONCObject> tableRowSelectedObjectList;
	
	protected ONCTable sortTable;
	protected DefaultTableModel sortDefaultTableModel;
	protected JButton btnApplyChanges;
	protected JButton btnResetCriteria;
	
	protected String sortONCNum = "";
	
	protected boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	protected boolean bIgnoreCBEvents = false;
	
//	protected String[] famstatus = {"Any", "Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
//	protected static String[] delstatus = {"Any", "None", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
	protected static String[] stoplt = {"Any", "Green", "Yellow", "Red", "Off"};
	
	public SortTableDialog(JFrame pf, int nTableRows)
	{
		super(pf);
		
		fDB = FamilyDB.getInstance();
		
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		//initialize member variables
		sortONCNum = "";
		tableSortCol = -1;
		tableRowSelectedObjectList = new ArrayList<ONCObject>();
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));

		sortCriteriaPanelTop = new JPanel();
		sortCriteriaPanelTop.setLayout(new BoxLayout(sortCriteriaPanelTop, BoxLayout.X_AXIS));
		sortCriteriaPanelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		//Create the ONC Icon label and add it to the search criteria panel
    	JLabel lblONCicon = new JLabel(gvs.getImageIcon(0));
    	lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
    	lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
        sortCriteriaPanelTop.add(lblONCicon);
		
		sortCriteriaPanel.add(sortCriteriaPanelTop);
		sortCriteriaPanel.add(sortCriteriaPanelBottom);	
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Set up the sort family table panel
		sortTable = new ONCTable(getColumnToolTips(), new Color(240,248,255));

		//Set up the table model. Cells are not editable
		sortDefaultTableModel = new DefaultTableModel(getColumnNames(), 0) {
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		};

		//Set the table model, select ability to select multiple rows and add a listener to 
		//check if the user has selected a row. 
		sortTable.setModel(sortDefaultTableModel);
		sortTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sortTable.getSelectionModel().addListSelectionListener(this);

		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = getColumnWidths();
		for(int i=0; i < colWidths.length; i++)
		{
			sortTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			tablewidth += colWidths[i];
		}
		tablewidth += SORT_TABLE_VERTICAL_SCROLL_WIDTH; 	//Account for vertical scroll bar

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

		//Center cell entries for specified cells. If parameter is null, no cells to center
        int[] center_cols = getCenteredColumns();
        if(center_cols != null)
        {
        	DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
        	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        	for(int i=0; i<center_cols.length; i++)
        		sortTable.getColumnModel().getColumn(center_cols[i]).setCellRenderer(dtcr);
        }

		sortTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		sortTable.setFillsViewportHeight(true);

		//Create the scroll pane and add the table to it.
		JScrollPane sortScrollPane = new JScrollPane(sortTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		sortScrollPane.setPreferredSize(new Dimension(tablewidth, sortTable.getRowHeight()*nTableRows));
		
        //Set up the bottom panel using border layout. Add common Reset Criteria and
        //Apply Changes buttons to a base panel as part of the bottom panel. This base panel
		//will be added to
		//the bottom panel at LINE_END so it is always the right most two buttons
		//Subclass dialogs will add additional panels to the bottom panel, either at LINE_START
		//or CENTER, or both
		bottomPanel = new JPanel(new BorderLayout());
		JPanel basePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
        btnResetCriteria = new JButton("Reset Filters");
        btnResetCriteria.addActionListener(this);  
        btnResetCriteria.addActionListener(new ActionListener() {
        	@Override
    		public void actionPerformed(ActionEvent e) {
    			if(e.getSource() == btnResetCriteria) { onResetCriteriaClicked(); }
    		}	
        });
        basePanel.add(btnResetCriteria);
 
        btnApplyChanges = new JButton("Apply Changes");
        btnApplyChanges.setEnabled(false);
        btnApplyChanges.addActionListener(new ActionListener() {
        	@Override
    		public void actionPerformed(ActionEvent e) {
    			if(e.getSource() == btnApplyChanges) { onApplyChanges(); }
    		}	
        });
        basePanel.add(btnApplyChanges);
       
        bottomPanel.add(basePanel, BorderLayout.LINE_END);
                
        //Add the top two panels to the dialog pane, the other panels are added
        //by the subclasses
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
        this.add(sortCriteriaPanel);
        this.add(sortScrollPane);

        setResizable(true);
	}
	
	abstract String[] getColumnToolTips();
	abstract String[] getColumnNames();
	abstract int[] getColumnWidths();
	abstract int[] getCenteredColumns();
	
	abstract void initializeFilters();
	
	abstract void buildTableList(boolean bPreserveSelections);
	
	abstract int sortTableList(int col);
	
//	abstract void archiveTableSelections(ArrayList<? extends ONCObject> stAL);
	
	void archiveTableSelections(List<? extends ONCObject> stAL)
	{
		tableRowSelectedObjectList.clear();
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
			tableRowSelectedObjectList.add(stAL.get(row_sel[i]));
	}
	
	/*****************************************************************************************
	 * Displays the contents of the sort table list in the ONC table. 
	 * @param stAL	- List of table rows to be displayed
	 * @param bSortReq - true: don't resort the table
	 ****************************************************************************************/
	void displaySortTable(List<? extends ONCObject> stAL, boolean bResort,
							List<? extends ONCObject> tableRowSelectedObjectList)
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		//clear any selections the user made
		ListSelectionModel lsModel = sortTable.getSelectionModel();
		lsModel.setValueIsAdjusting(true);
		lsModel.clearSelection();	//clear any selected rows
		
		//clear the table
		while (sortDefaultTableModel.getRowCount() > 0)
			sortDefaultTableModel.removeRow(0);
		
		setEnabledControls(false);	//disable any controls that are reset
		
		//add rows to the table
		for(int i=0; i < stAL.size(); i++)
			sortDefaultTableModel.addRow(getTableRow(stAL.get(i)));
		
		lsModel.setValueIsAdjusting(false);
		
		//check to see if the table needs to be sorted by column
		if(bResort && tableSortCol > -1)
			sortTableList(tableSortCol);
				
		//check to see if table rows need to be re-selected
		for(int iIndex=0; iIndex < tableRowSelectedObjectList.size(); iIndex++)
		{
			//find the id in the stAL, getting it's row, the reselect it
			int jIndex = 0;
			boolean bMatchFound = false;
			while(jIndex < stAL.size() && !bMatchFound)
			{
				if(stAL.get(jIndex).matches(tableRowSelectedObjectList.get(iIndex)))
					bMatchFound = true;
				else		
					jIndex++;
			}
			
			if(jIndex < stAL.size())
				lsModel.addSelectionInterval(jIndex, jIndex);
		}
		
		//re-enable any controls if rows are still selected
		if(!tableRowSelectedObjectList.isEmpty())
			setEnabledControls(true);
				
		bChangingTable = false;	
	}
	
	abstract void setEnabledControls(boolean tf);
	
	abstract Object[] getTableRow(ONCObject o);
	
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
