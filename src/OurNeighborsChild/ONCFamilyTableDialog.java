package OurNeighborsChild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public abstract class ONCFamilyTableDialog extends ONCSortTableDialog implements ActionListener, DatabaseListener,
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
	protected ArrayList<ONCFamily> stAL; //Holds reference to ONCFamily objects shown in table
	protected DriverDB driverDB;
	protected DeliveryDB deliveryDB;
	protected ONCRegions regions;
	
	//JPanels the inherited class may use to add GUI elements
	protected JPanel sortCriteriaPanelTop, sortCriteriaPanelBottom;
	protected JPanel itemCountPanel, changeDataPanel, cntlPanel;
	
	public ONCTable sortTable;
	private DefaultTableModel sortTableModel;
	protected JButton btnApplyChanges;
	private JButton btnResetCriteria;
	protected JLabel lblNumOfTableItems;
	
	protected boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	protected boolean bIngoreCBEvents = false;
	
	private String[] columns;
	
	protected String[] famstatus = {"Any", "Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
	protected static String[] delstatus = {"Any", "Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
	protected static String[] stoplt = {"Any", "Green", "Yellow", "Red", "Off"};
	
	public ONCFamilyTableDialog(JFrame pf, String[] colToolTips, String[] cols, int[] colWidths, int[] center_cols)
	{
		super(pf);
		oncGVs = GlobalVariables.getInstance();
		
		driverDB = DriverDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		columns = cols;
		this.setTitle("Our Neighbor's Child - Delivery Assignment");
		
		//Initialize the sort table array list
		stAL = new ArrayList<ONCFamily>();

		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
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
            public void mouseClicked(MouseEvent e) {
            	if(fDB.sortDB(stAL, columns[sortTable.columnAtPoint(e.getPoint())]))
    				displaySortTable();
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
        sortCriteriaPanelTop.setPreferredSize(new Dimension(sortTable.getWidth(), 64));
		
        //Set up the third panel holding count panel and change panel
        JPanel thirdPanel = new JPanel();
        thirdPanel.setLayout(new BoxLayout(thirdPanel, BoxLayout.X_AXIS));
        
        itemCountPanel = new JPanel();       
        lblNumOfTableItems = new JLabel("0");
        itemCountPanel.setBorder(BorderFactory.createTitledBorder("Families Meeting Criteria"));
        itemCountPanel.setSize(new Dimension(300, 90));
        itemCountPanel.add(lblNumOfTableItems);
        
        changeDataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 
		//Add the components to the third panel	
		thirdPanel.add(itemCountPanel);
		thirdPanel.add(changeDataPanel);
		thirdPanel.setPreferredSize(new Dimension(sortTable.getWidth(), 90));
				
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
       
        pack();
        setResizable(true);
	}
	
	abstract public void buildSortTable();
	
	void displaySortTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		while (sortTableModel.getRowCount() > 0)	//Clear the current table
			sortTableModel.removeRow(0);
		
		disableControls();
		
		for(ONCFamily si:stAL)	//Build the new table
			sortTableModel.addRow(getTableRow(si));
				
		bChangingTable = false;	
	}
	
	protected void disableControls() {};
	
	abstract protected String[] getTableRow(ONCFamily f);
	
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
	
	boolean isNumeric(String s){ return s.matches("-?\\d+(\\.\\d+)?"); }
	
	@Override
	public void valueChanged(ListSelectionEvent e) 
	{
		if (!e.getValueIsAdjusting() && e.getSource() == sortTable.getSelectionModel() &&
				!bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow());
			
			fireEntitySelected(this, "FAMILY_SELECTED", fam, null);
			this.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change family		
	}
}
