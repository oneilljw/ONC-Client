package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public abstract class DependantTableDialog extends SortTableDialog
{
	/**
	 * This abstract class implements a blueprint for dialogs that have two tables, a selection table
	 * on an upper panel and a dependent table on a lower panel. The selection table is populated
	 * with ONCObjects such as Agents or ONCDrivers. When the user selects one or more ONCObjects
	 * from the selection table, the dependent table displays the ONCFamily objects associated with
	 * the selected ONCObjects.
	 * 
	 * The Manage Agent Dialog and Manage Driver Dialog subclass this class.
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_FAMILY_ROWS_TO_DISPLAY = 15;
	
	protected ONCRegions regions;
	protected ONCTable familyTable;
	private DefaultTableModel familyTableModel;
	protected JComboBox famPrintCB;
	protected JButton btnDependantTableExport;
	protected JLabel lblNumOfObjects, lblObjectMssg, lblNumOfFamilies;
	protected JPanel objectCountPanel, lowercntlpanel;
	protected JScrollPane familyTableScrollPane;
	protected String[] columns;
	protected ArrayList<ONCFamily> stAL;	//Holds references to family objects for family table
	
	protected boolean bChangingFamilyTable = false;	//Semaphore used to indicate the sort table is being changed
	
//	private static String[] famstatus = {"Any","Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
//	private static String[] delstatus = {"Any", "Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
//	private static String[] stoplt = {"Any", "Green", "Yellow", "Red", "Off"};
	

	public DependantTableDialog(JFrame pf, int nTableRows)
	{
		super(pf, 10);
		columns = getColumnNames();
		
		regions = ONCRegions.getInstance();
		
		stAL = new ArrayList<ONCFamily>();
		
      	//Set up the selection table object count panel
     	objectCountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      	lblObjectMssg = new JLabel();
        lblNumOfObjects = new JLabel();
        objectCountPanel.add(lblObjectMssg);
        objectCountPanel.add(lblNumOfObjects);
      
        //Set up the middle control panel
//    	middlecntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
       
        //Add object count and control panels to the middle control panel
//      cntlPanel.add(objectCountPanel);
//      cntlPanel.add(middlecntlPanel);
//      BorderLayout layoutMgr = (BorderLayout) cntlPanel.getLayout();
//      layoutMgr.setHgap(154);
        
        //remove the Apply Changes button from SortTableDialog, it's not used here
        btnApplyChanges.setVisible(false);	//not used in this subclass of SortTableDialog
           
        //Set up the family table panel
        String[] colTT = {"ONC Family Number", "Batch Number", "Do Not Serve Code", 
					"Family Status", "Delivery Status", "Meal Status", "Head of Household First Name", 
					"Head of Household Last Name", "House Number","Street", "Unit",
					"Zip Code", "Region","Changed By", "Stoplight Color"};
        
        familyTable = new ONCTable(colTT, new Color(240,248,255));

      	final String[] ftcolumns = {"ONC", "Batch #", "DNS", "Fam Status", "Del Status", "Meal Status", "First", "Last", "House",
      							"Street", "Unit", "Zip", "Reg", "Changed By", "SL"};
      	familyTableModel = new DefaultTableModel(ftcolumns, 0)
        {
      		private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
           
              
        familyTable.setModel(familyTableModel);
        familyTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        familyTable.getSelectionModel().addListSelectionListener(this);
              
      	familyTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
              
      	//Set table column widths
      	int familytablewidth = 0;
      	int[] familycolWidths = {32, 48, 48, 72, 72, 72, 72, 72, 48, 128, 56, 48, 28, 72, 24};
      	for(int i=0; i < familycolWidths.length; i++)
      	{
      	  	familyTable.getColumnModel().getColumn(i).setPreferredWidth(familycolWidths[i]);
      	  	familytablewidth += familycolWidths[i];
      	}
      	familytablewidth += 24; 	//Account for vertical scroll bar
             
              
        JTableHeader ftHeader = familyTable.getTableHeader();
        ftHeader.setForeground( Color.black);
        ftHeader.setBackground( new Color(161,202,241));
              
        //mouse listener for header click
        ftHeader.addMouseListener(new MouseAdapter()
        {
        	@Override
            public void mouseClicked(MouseEvent e)
            {
        		if(fDB.sortDB(stAL, ftcolumns[familyTable.columnAtPoint(e.getPoint())]))
        		{
        			clearFamilyTable();
        			displayFamilyTable();
        		}
            }
        });		
              
        //Center cell entries for Batch # and Region
        DefaultTableCellRenderer ftcr = new DefaultTableCellRenderer();    
        ftcr.setHorizontalAlignment(SwingConstants.CENTER);
        familyTable.getColumnModel().getColumn(1).setCellRenderer(ftcr);
        familyTable.getColumnModel().getColumn(11).setCellRenderer(ftcr);
//      familyTable.getColumnModel().getColumn(13).setCellRenderer(ftcr);
              
        familyTable.setFillsViewportHeight(true);    
              
        //Create the scroll pane and add the table to it.
        familyTableScrollPane = new JScrollPane(familyTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
              											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
              
        familyTableScrollPane.setPreferredSize(new Dimension(familytablewidth, familyTable.getRowHeight()*NUM_FAMILY_ROWS_TO_DISPLAY));

		//Set up the bottom panel
		lowercntlpanel = new JPanel(new BorderLayout());
		
		//Set up the family count panel
		JPanel famcountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		JLabel lblFamCountMssg = new JLabel("# of Families:");
		lblNumOfFamilies= new JLabel();
		famcountPanel.add(lblFamCountMssg);
		famcountPanel.add(lblNumOfFamilies);
        
        //Set up the family control panel
		JPanel famCntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		
		//Set up the control panel
		btnDependantTableExport = new JButton("Export Data");
		btnDependantTableExport.setEnabled(false);
		btnDependantTableExport.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Family Table"};
        famPrintCB = new JComboBox(printChoices);
        famPrintCB.setPreferredSize(new Dimension(136, 28));
        famPrintCB.setEnabled(false);
        famPrintCB.addActionListener(this);
        
        //Add the components to the control panel
        famCntlPanel.add(btnDependantTableExport);
        famCntlPanel.add(famPrintCB);
        
        //Add family count and control panels to bottom panel
        lowercntlpanel.add(famcountPanel, BorderLayout.LINE_START);
        lowercntlpanel.add(famCntlPanel, BorderLayout.LINE_END);
        
       
        pack();
	}
	
	void clearFamilyTable()
	{
		bChangingFamilyTable = true;	//don't process table messages while being changed
		
		while (familyTableModel.getRowCount() > 0)	//Clear the current table
			familyTableModel.removeRow(0);
		
		//Family table empty, disable print
		famPrintCB.setEnabled(false);
		
		lblNumOfFamilies.setText(Integer.toString(stAL.size()));
		
		bChangingFamilyTable = false;	
	}
	
	void displayFamilyTable()
	{
		bChangingFamilyTable = true;	//don't process table messages while being changed
		
		for(ONCFamily si:stAL)	//Build the new table
			familyTableModel.addRow(getFamilyTableRow(si));
				
		bChangingFamilyTable = false;	
	}
	
	Object[] getFamilyTableRow(ONCFamily f)
	{
//		GlobalVariables gvs = GlobalVariables.getInstance();
		
		Object[] familytablerow = new Object[15];
		
		familytablerow[0] = f.getONCNum(); 
		familytablerow[1] = f.getBatchNum();
		familytablerow[2] = f.getDNSCode();
		familytablerow[3] = f.getFamilyStatus().toString();
		familytablerow[4] = f.getGiftStatus().toString();
		familytablerow[5] = f.getMealStatus().toString();
		
		UserDB userDB = UserDB.getInstance();
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.General) > 0)
		{
			familytablerow[6] = f.getHOHFirstName();
			familytablerow[7] = f.getHOHLastName();
			familytablerow[8] = f.getHouseNum();
			familytablerow[9] = f.getStreet();
			familytablerow[10] = f.getUnitNum();
			familytablerow[11] = f.getZipCode();
		}
		else
		{
			familytablerow[6] = "";
			familytablerow[7] = "";
			familytablerow[8] = "";
			familytablerow[9] = "";
			familytablerow[10] = "";
			familytablerow[11] = "";
		}
		
		familytablerow[12] = regions.getRegionID(f.getRegion());
		familytablerow[13] = f.getChangedBy();
//		familytablerow[13] = stoplt[f.getStoplightPos()+1].substring(0,1);
		familytablerow[14] = gvs.getImageIcon(23 + f.getStoplightPos());
			
		return familytablerow;
	}
	
	abstract void buildFamilyTableListAndDisplay();
	
	void onPrintFamilyListing()
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat("ONC Families for Agent");
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             familyTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
             famPrintCB.setSelectedIndex(0);
		} 
		catch (PrinterException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	void setEnabledControls(boolean tf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	abstract Object[] getTableRow(ONCObject o); 
//	{
//		Agent a = (Agent) o;
//		Object[] ai = {a.getAgentName(), a.getAgentOrg(), a.getAgentTitle(), 
//						a.getAgentEmail(), a.getAgentPhone()};
//		return ai;
//	}

	@Override
	void checkApplyChangesEnabled()
	{ 
		//Not used in this subclass of SortTableDialog 
	}
		
	@Override
	boolean onApplyChanges()
	{
		//Not used in this subclass of SortTableDialog
		return false;
	}

	@Override
	boolean isONCNumContainerEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
}
