package OurNeighborsChild;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import au.com.bytecode.opencsv.CSVWriter;
import com.toedter.calendar.JDateChooser;

public class SortWishDialog extends ONCSortTableDialog implements ActionListener, PropertyChangeListener, 
													ListSelectionListener, DatabaseListener													
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_AGE_LIMIT = 21;
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;
	private static final int WISH_CATALOG_LIST_ALL = 7;
	private static final int WISH_CATALOG_SORT_LIST = 1;
	private static final int CHILD_WISH_STATUS_ASSIGNED = 3;
	private static final int RS_ITEMS_PER_PAGE = 20;
	private static final int NUM_ROWS_TO_DISPLAY = 15;
	private static final Integer MAXIMUM_ON_NUMBER = 9999;
	
	private ONCOrgs orgs;
	private ONCWishCatalog cat;
	public ONCTable sortTable;
	private DefaultTableModel sortTableModel;
	private JComboBox startAgeCB, endAgeCB, genderCB, wishnumCB, wishCB, resCB, assignCB, statusCB, changedByCB;
	private JComboBox changeResCB, changeStatusCB, changeAssigneeCB, printCB;
	private DefaultComboBoxModel wishCBM, assignCBM, changeAssigneeCBM, changedByCBM;
	private JTextField oncnumTF;
	private JButton btnResetCriteria, btnExport;
	private JButton btnApplyChanges;
	private JLabel lblWishes;
	private JDateChooser ds, de;
	private Calendar sortStartCal = null, sortEndCal = null;
	private Families fDB;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ArrayList<ONCWishDlgSortItem> stAL = new ArrayList<ONCWishDlgSortItem>();
	private boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
//	private boolean bChildDataChanged = false;
	private boolean bIgnoreSortDialogEvents = false;
	private int tableSortCol = -1;
	private ArrayList<Integer> tableRowSelectedItemIDList;
	private int sortStartAge = 0, sortEndAge = ONC_AGE_LIMIT, sortGender = 0, sortChangedBy = 0;;
	private int sortWishNum = 0, sortRes = 0, sortStatus = 0, sortAssigneeID = 0;
	private String sortONCNum = "";
//	private String sortWish = "Any";
	private int sortWishID = -2;
	private int totalNumOfLabelsToPrint;	//Holds total number of labels requested in a print job
	private static String[] genders = {"Any", "Boy", "Girl"};
	private static String[] res = {"Any", "Blank", "*", "#"};
	private static String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
										"Distributed", "Verified"};
	
	SortWishDialog(JFrame pf)
	{
		super(pf);
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		
		orgs = ONCOrgs.getInstance();
		cat = ONCWishCatalog.getInstance();
		this.setTitle("Our Neighbor's Child - Wish Management");
		
		//set up data base listeners
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(cwDB != null)
			cwDB.addDatabaseListener(this);
		if(orgs != null)
			orgs.addDatabaseListener(this);
		if(cat != null)
			cat.addDatabaseListener(this);
		
		//initialize member variables
		tableRowSelectedItemIDList = new ArrayList<Integer>();
		        
		//Set up the search criteria panel
		String[] ages = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
							"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
       
        JPanel sortCriteriaPanel = new JPanel();
        sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));
		JPanel sortCriteriaPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel sortCriteriaPanelBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
    	JLabel lblONCicon = new JLabel(gvs.getImageIcon(0));
    	
    	oncnumTF = new JTextField();
    	oncnumTF.setEditable(true);
    	oncnumTF.setPreferredSize(new Dimension(72,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
		startAgeCB = new JComboBox(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Start Age"));
		startAgeCB.setPreferredSize(new Dimension(80,56));
		startAgeCB.addActionListener(this);
		
		endAgeCB = new JComboBox(ages);
		endAgeCB.setBorder(BorderFactory.createTitledBorder("End Age"));
		endAgeCB.setPreferredSize(new Dimension(80,56));
		endAgeCB.setSelectedIndex(ages.length-1);
		endAgeCB.addActionListener(this);
		
		genderCB = new JComboBox(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setSize(new Dimension(72,56));
		genderCB.addActionListener(this);
		
		String[] wishnums = {"Any", "1", "2", "3"};
		wishnumCB = new JComboBox(wishnums);
		wishnumCB.setBorder(BorderFactory.createTitledBorder("Wish #"));
		startAgeCB.setPreferredSize(new Dimension(72,56));
		wishnumCB.addActionListener(this);
		
		wishCBM = new DefaultComboBoxModel();
	    wishCBM.addElement(new ONCWish(-2, "Any", 7));
		wishCB = new JComboBox();		
        wishCB.setModel(wishCBM);
		wishCB.setPreferredSize(new Dimension(180, 56));
		wishCB.setBorder(BorderFactory.createTitledBorder("Wish Type"));
		wishCB.addActionListener(this);
		
		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.setPreferredSize(new Dimension(144,56));
		changedByCB.addActionListener(this);
		
		sortStartCal = Calendar.getInstance();
		sortStartCal.setTime(gvs.getSeasonStartDate());
		
		ds = new JDateChooser(sortStartCal.getTime());
		ds.setPreferredSize(new Dimension(156, 56));
		ds.setBorder(BorderFactory.createTitledBorder("Changed On/After"));
		ds.getDateEditor().addPropertyChangeListener(this);
		
		sortEndCal = Calendar.getInstance();
		sortEndCal.setTime(gvs.getTodaysDate());
		sortEndCal.add(Calendar.DATE, 1);
		
		de = new JDateChooser(sortEndCal.getTime());
		de.setPreferredSize(new Dimension(156, 56));
		de.setBorder(BorderFactory.createTitledBorder("Changed Before"));
		de.getDateEditor().addPropertyChangeListener(this);
		
		resCB = new JComboBox(res);
		resCB.setPreferredSize(new Dimension(104, 56));
		resCB.setBorder(BorderFactory.createTitledBorder("Restrictions"));
		resCB.addActionListener(this);
		res[0] = "No Change";	//Change "Any" to none after sort criteria list created
		
		statusCB = new JComboBox(status);
		statusCB.setPreferredSize(new Dimension(136, 56));
		statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
		statusCB.addActionListener(this);
		status[0] = "No Change"; //Change "Any" to none after sort criteria list created
		
		assignCB = new JComboBox();
		assignCBM = new DefaultComboBoxModel();
	    assignCBM.addElement(new Organization(0, "Any", "Any"));
	    assignCBM.addElement(new Organization(-1, "None", "None"));
	    assignCB.setModel(assignCBM);
		assignCB.setPreferredSize(new Dimension(192, 56));
		assignCB.setBorder(BorderFactory.createTitledBorder("Assigned To"));
		assignCB.addActionListener(this);
		
		sortCriteriaPanelTop.add(lblONCicon);
		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(endAgeCB);
		sortCriteriaPanelTop.add(genderCB);
		sortCriteriaPanelTop.add(wishnumCB);
		sortCriteriaPanelTop.add(wishCB);
		sortCriteriaPanelTop.add(resCB);
		sortCriteriaPanelBottom.add(statusCB);
		sortCriteriaPanelBottom.add(assignCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(ds);
		sortCriteriaPanelBottom.add(de);
		
		sortCriteriaPanel.add(sortCriteriaPanelTop);
		sortCriteriaPanel.add(sortCriteriaPanelBottom);
		
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
		
		//Set up the sort wish table
		String[] colToolTips = {"ONC Family Number", "Child's Age", 
				"Child's Gender", "Wish Number - 1, 2 or 3", "Wish Assigned", "Wish Detail",
				"# - Selected by ONC or * - Don't asssign", "Wish Status", "Who is fulfilling?",
				"User who last changed wish", "Date & Time Wish Last Changed"};
		sortTable = new ONCTable(colToolTips, new Color(240,248,255));
		
		//set up the sort wish table model
		String[] columns = {"ONC", "Age", "Gend", "Wish", "Wish Type", "Details", " Res ",
							"Status", "Assignee", "Changed By", "Time Stamp"};
        sortTableModel = new DefaultTableModel(columns, 0)
        {
        	private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
     
        //add the model to the table and set selection interval
        sortTable.setModel(sortTableModel);
        sortTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//	    sortTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        
        //Set table column widths
	  	int tablewidth = 0;
	  	int[] colWidths = {40, 48, 36, 36, 76, 160, 32, 80, 132, 80, 96};
	  	for(int i=0; i < colWidths.length; i++)
	  	{
	  		sortTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
	  		tablewidth += colWidths[i];
	  	}
	  	tablewidth += 24; 	//Account for vertical scroll bar      
        
	  	//set the table header style
        JTableHeader anHeader = sortTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //mouse listener for header click to sort by column
        anHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	if(sortTableByColumn(sortTable.columnAtPoint(e.getPoint())))
            	{
            		tableSortCol = sortTable.columnAtPoint(e.getPoint());
            		displaySortTable();
            	}
    /*        	
            	if(sortTable.columnAtPoint(e.getPoint()) == 0)	//Sort on ONC Family Number
            	{
            		Collections.sort(stAL, new ONCSortItemFamNumComparator());
            		tableSortCol = 0;
            	}
            	else if(sortTable.columnAtPoint(e.getPoint()) == 1)	// Sort on Child's Age
            	{
            		Collections.sort(stAL, new ONCSortItemAgeComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 2)	//Sort on Child's Gender
            	{
            		Collections.sort(stAL, new ONCSortItemGenderComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 3)	//Sort on Child's Wish #
            	{
            		Collections.sort(stAL, new ONCSortItemWishNumComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 4)	//Sort on Child's Base Wish
            	{
            		Collections.sort(stAL, new ONCSortItemWishBaseComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 5)	//Sort on Child's Wish Detail
            	{
            		Collections.sort(stAL, new ONCSortItemWishDetailComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 6)	//Sort on Child's Wish Indicator
            	{
            		Collections.sort(stAL, new ONCSortItemWishIndicatorComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 7)	//Sort on Child's Wish Status
            	{
            		Collections.sort(stAL, new ONCSortItemWishStatusComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 8)	//Sort on Child's Wish Assignee
            	{
            		Collections.sort(stAL, new ONCSortItemWishAssigneeComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) ==  9)	//Sort on Child's Wish Changed By
            	{
            		Collections.sort(stAL, new ONCSortItemWishChangedByComparator());
            		tableSortCol = 0;
            	}
            	else if (sortTable.columnAtPoint(e.getPoint()) == 10)	//Sort on Child's Wish Date Changed
            	{
            		Collections.sort(stAL, new ONCSortItemWishDateChangedComparator());
            		tableSortCol = 0;
            	}
            	else
            		return;
            	
            	displaySortTable();
*/            	
            }
        });
        
        //Center cell entries for Child and Wish Number, Wish Indicator
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//      sortTable.getColumnModel().getColumn(1).setCellRenderer(dtcr);
        sortTable.getColumnModel().getColumn(3).setCellRenderer(dtcr);
        sortTable.getColumnModel().getColumn(6).setCellRenderer(dtcr);
        
        sortTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        sortTable.setFillsViewportHeight(true);     
        sortTable.getSelectionModel().addListSelectionListener(this);
        
        //Create the scroll pane and add the table to it.
        JScrollPane sortScrollPane = new JScrollPane(sortTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
        												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        sortScrollPane.setPreferredSize(new Dimension(tablewidth, sortTable.getRowHeight()*NUM_ROWS_TO_DISPLAY));
//      sortTablePanel.add(sortScrollPane);
        
        //Set up the third panel holding wish count panel and wish status and assignee change panel
        JPanel thirdpanel = new JPanel();
        thirdpanel.setLayout(new BoxLayout(thirdpanel, BoxLayout.X_AXIS));
        
        JPanel wishCountPanel = new JPanel();       
        lblWishes = new JLabel("0");
        wishCountPanel.setBorder(BorderFactory.createTitledBorder("Wishes Meeting Criteria"));
        wishCountPanel.setPreferredSize(new Dimension(165, 90));
        wishCountPanel.add(lblWishes);
        
        JPanel changeWishPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        changeWishPanel.setPreferredSize(new Dimension(tablewidth-165, 90));
        
        changeResCB = new JComboBox(res);
        changeResCB.setPreferredSize(new Dimension(192, 56));
		changeResCB.setBorder(BorderFactory.createTitledBorder("Change Restrictions To:"));
		changeResCB.addActionListener(this);
        
        changeStatusCB = new JComboBox(status);
        changeStatusCB.setPreferredSize(new Dimension(192, 56));
		changeStatusCB.setBorder(BorderFactory.createTitledBorder("Change Status To:"));
		changeStatusCB.addActionListener(this);
        
        changeAssigneeCB = new JComboBox();
        changeAssigneeCBM = new DefaultComboBoxModel();
	    changeAssigneeCBM.addElement(new Organization(0, "No Change", "No Change"));
	    changeAssigneeCBM.addElement(new Organization(-1, "None", "None"));
        changeAssigneeCB.setModel(changeAssigneeCBM);
        changeAssigneeCB.setPreferredSize(new Dimension(192, 56));
		changeAssigneeCB.setBorder(BorderFactory.createTitledBorder("Change Assignee To:"));
		changeAssigneeCB.addActionListener(this);
		
		changeWishPanel.add(changeResCB);
		changeWishPanel.add(changeStatusCB);
		changeWishPanel.add(changeAssigneeCB);
		
		changeWishPanel.setBorder(BorderFactory.createTitledBorder("Change Wish Restrictions/Status/Assignee"));
         
		thirdpanel.add(wishCountPanel);
		thirdpanel.add(changeWishPanel);
		
        //Set up the button control panel
		JPanel cntlPanel = new JPanel();
                    
        btnResetCriteria = new JButton("Reset Criteria");
        btnResetCriteria.addActionListener(this);        
    
        btnExport = new JButton("Export Data");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing", "Print Labels", "Print Partner Receiving Check Sheets"};
        printCB = new JComboBox(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        printCB.addActionListener(this);
        
        btnApplyChanges = new JButton("Apply Changes");
        btnApplyChanges.setEnabled(false);
        btnApplyChanges.addActionListener(this);
        
        cntlPanel.add(btnExport);
        cntlPanel.add(printCB);
        cntlPanel.add(btnResetCriteria);
        cntlPanel.add(btnApplyChanges);
         
        //Add the components to the frame pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
        this.add(sortCriteriaPanel);
        this.add(sortScrollPane);
        this.add(thirdpanel);
        this.add(cntlPanel);
       
        pack();
//        setSize(tablewidth, 600);
        setResizable(true);
//        Point pt = GlobalVariables.getFrame().getLocation();
//        setLocation(pt.x + GlobalVariables.getFrame().getWidth() - tablewidth, pt.y + 20);
	}
	
	boolean sortTableByColumn(int col)
	{
		boolean bTableSorted = true; //set to true assuming col will be found
		if(col == 0)	//Sort on ONC Family Number
    		Collections.sort(stAL, new ONCSortItemFamNumComparator());
    	else if(col == 1)	// Sort on Child's Age
    		Collections.sort(stAL, new ONCSortItemAgeComparator());
    	else if(col == 2)	//Sort on Child's Gender
    		Collections.sort(stAL, new ONCSortItemGenderComparator());
    	else if(col == 3)	//Sort on Child's Wish #
    		Collections.sort(stAL, new ONCSortItemWishNumComparator());
    	else if(col == 4)	//Sort on Child's Base Wish
    		Collections.sort(stAL, new ONCSortItemWishBaseComparator());
    	else if(col == 5)	//Sort on Child's Wish Detail
    		Collections.sort(stAL, new ONCSortItemWishDetailComparator());
    	else if(col == 6)	//Sort on Child's Wish Indicator
    		Collections.sort(stAL, new ONCSortItemWishIndicatorComparator());
    	else if(col == 7)	//Sort on Child's Wish Status
    		Collections.sort(stAL, new ONCSortItemWishStatusComparator());
    	else if(col == 8)	//Sort on Child's Wish Assignee
    		Collections.sort(stAL, new ONCSortItemWishAssigneeComparator());
    	else if(col ==  9)	//Sort on Child's Wish Changed By
    		Collections.sort(stAL, new ONCSortItemWishChangedByComparator());
    	else if(col == 10)	//Sort on Child's Wish Date Changed
    		Collections.sort(stAL, new ONCSortItemWishDateChangedComparator());
    	else
    		bTableSorted = false;
    		
    	return bTableSorted;
	}
	
	void displaySortTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		ListSelectionModel lsModel = sortTable.getSelectionModel();
		
		lsModel.clearSelection();	//clear any selected rows
		
		while (sortTableModel.getRowCount() > 0)	//Clear the current table
			sortTableModel.removeRow(0);
		
		
		for(ONCWishDlgSortItem si:stAL)	//Build the new table
			sortTableModel.addRow(si.getSortTableRow());
		
		//check to see if the sortTable needs to be sorted by column
		if(tableSortCol > -1)
			sortTableByColumn(tableSortCol);
		
		//check to see if rows need to be re-selected
		for(Integer itemID:tableRowSelectedItemIDList)
		{
			//find the id in the stAL, getting it's row, the reselect it
			int index = 0;
			while(index < stAL.size() && stAL.get(index).getID() != itemID)
				index++;
			
			if(index < stAL.size())
				lsModel.addSelectionInterval(index, index);	
		}
		
		//set status of export capability
		btnExport.setEnabled(tableRowSelectedItemIDList.isEmpty() ? false : true);
				
		bChangingTable = false;	
	}
	
	public void buildSortTableList()
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedItemIDList.clear();
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			System.out.println(String.format("SortWishDialog.buildSortTableList: Added ID %d",
					stAL.get(row_sel[i]).getID()));
			tableRowSelectedItemIDList.add(stAL.get(row_sel[i]).getID());
		}
		
		stAL.clear();	//Clear the prior table information in the array list
		int itemID = 0;
		for(ONCFamily f:fDB.getList())
		{
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()))	//Must be a valid family	
			{
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c))	//Children criteria pass
					{
						for(int i=0; i< cwDB.getNumberOfWishesPerChild(); i++)
						{	 //Assignee, Status, Date & Wish match
							ONCChildWish cw = cwDB.getWish(c.getChildWishID(i));
							
							if(cw != null && doesResMatch(cw.getChildWishIndicator()) &&
								doesAssigneeMatch(cw.getChildWishAssigneeID()) &&
								 doesStatusMatch(cw.getChildWishStatus()) &&
								  isWishChangeDateBetween(cw.getChildWishDateChanged()) &&
								   doesChangedByMatch(cw.getChildWishChangedBy()) &&
									doesWishBaseMatch(cw.getWishID()) &&
									 doesWishNumMatch(i)) //Wish criteria pass
							{
								Organization org =  orgs.getOrganizationByID(cw.getChildWishAssigneeID());
								
								stAL.add(new ONCWishDlgSortItem(itemID++, f, f.getID(),f.getONCNum(),
									c, i, c.getChildAge(), c.getChildDateOfBirth(), c.getChildGender(),
									cw.getChildWishIndicator(),
									cat.getWishByID(cw.getWishID()).getName(),
									cw.getChildWishDetail(), status[cw.getChildWishStatus()],
									org == null ? "None" : org.getName(),
									cw.getChildWishChangedBy(), cw.getChildWishDateChanged()));
							}
						}
					}
				}
			}
		}
		
		lblWishes.setText(Integer.toString(stAL.size()));
		displaySortTable();		//Display the table after table array list is built	
	}
	
	void onApplyWishChanges()
	{
		bChangingTable = true;
		boolean bRebuildTable = false; //set true if a wish is changed so table is only rebuilt once per applyWishChange
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			boolean bNewWishRqrd = false; 	//Restriction, status or assignee change
			
			//Find child and wish number for selected
			ONCChild c = stAL.get(row_sel[i]).getSortItemChild();
			int wn = stAL.get(row_sel[i]).getSortItemChildWishNumber();
			ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));

			//Get current wish information
			int cwWishID = cw.getWishID();
//			String cwb = cat.getWishByID(cw.getWishID()).getName();
			String cwd = cw.getChildWishDetail();
			int cwi = cw.getChildWishIndicator();
			int cws = cw.getChildWishStatus();
			int cwaID = cw.getChildWishAssigneeID();
//			String cwaName = cw.getChildWishAssigneeName();
			
			//Determine if a change to wish restrictions, if so set new wish restriction for request
			if(changeResCB.getSelectedIndex() > 0 && cwi != changeResCB.getSelectedIndex()-1)
			{
				cwi = changeResCB.getSelectedIndex()-1;	//Restrictions start at 0
				bNewWishRqrd = true;
			}
			
			//Determine if a change to wish status, if so, set new wish status in request
			if(changeStatusCB.getSelectedIndex() > 0 && cws != changeStatusCB.getSelectedIndex())
			{
				cws = changeStatusCB.getSelectedIndex();
				bNewWishRqrd = true;
			}
			
			//Determine if a change to wish assignee, if so, set new wish assignee in request
//			if(changeAssigneeCB.getSelectedIndex() > 0 && cwaID != orgs.getConfirmedOrganizationID(changeAssigneeCB.getSelectedIndex()))
			if(changeAssigneeCB.getSelectedIndex() > 0 && cwaID != ((Organization)changeAssigneeCB.getSelectedItem()).getID())
			{
				//Set the new org data
				cwaID = ((Organization)changeAssigneeCB.getSelectedItem()).getID();
	//			cwaName = ((Organization)changeAssigneeCB.getSelectedItem()).getName();
				
				//If assignee is changing, then status may be changing as well
				if(cws < CHILD_WISH_STATUS_ASSIGNED)
					cws = CHILD_WISH_STATUS_ASSIGNED;
				
				bNewWishRqrd = true;
			}
			
			if(bNewWishRqrd)	//Restriction, Status or Assignee change detected
			{
				//Add the new wish to the child wish history, returns -1 if no wish created
				int wishid = cwDB.add(this, c.getID(), cwWishID, cwd, wn, cwi, cws, cwaID,
										gvs.getUserLNFI(), gvs.getTodaysDate()); 
				
				if(wishid != -1)	//only proceed if wish was accepted by the data base
					bRebuildTable = true;	//set flag to rebuild/display the table array/wish table
			}
		}
		
		if(bRebuildTable)
		{
			
			tableRowSelectedItemIDList.clear();
			buildSortTableList();
		}

		//Reset the change combo boxes to "No Change"
		changeResCB.setSelectedIndex(0);
		changeStatusCB.setSelectedIndex(0);
		changeAssigneeCB.setSelectedIndex(0);
		
		btnApplyChanges.setEnabled(false);
		
		bChangingTable = false;
	}
	
	void updateWishSelectionList()
	{
		//disable the combo box
		bIgnoreSortDialogEvents = true;
		wishCB.setEnabled(false);
		
		//get the current selection
		int currSelID = ((ONCWish) wishCB.getSelectedItem()).getID();
		
		//reset the selection in case the current selection is removed by the update
		wishCB.setSelectedIndex(0);
		sortWishID = -2;
		
		//Clear the combo box of current elements
		wishCBM.removeAllElements();	
	
		List<ONCWish> wishList = cat.getWishList(WISH_CATALOG_LIST_ALL, WISH_CATALOG_SORT_LIST);
		for(ONCWish w: wishList )	//Add new list elements
			wishCBM.addElement(w);
		
		//reselect the prior selection, if it's still in the list
		int index = 0;
		while(index < wishList.size() && wishList.get(index).getID() != currSelID)
			index++;
		
		if(index < wishList.size())
		{
			wishCB.setSelectedItem(wishList.get(index));
			sortWishID = wishList.get(index).getID();
		}
		
		//re-enable the combo box
		wishCB.setEnabled(true);
		bIgnoreSortDialogEvents = false;
	}
	
	/*********************************************************************************************
	 * This method updates the drop down selection list associated with the Wish Assignee combo
	 * boxes used to search for wish assignees and to select assignees. The lists must be updated
	 * every time an organization is confirmed or unconfirmed. 
	 * 
	 * The method must keep the current assignee selected in the assignCB even if their position 
	 * changes in the COrgs array. 
	 *********************************************************************************************/
	void updateWishAssigneeSelectionList()
	{
		bIgnoreSortDialogEvents = true;
		int currentAssigneeID = -1, currentChangeAssigneeID = -1;	//set to null for reselection
		int currentAssigneeIndex = assignCB.getSelectedIndex();
		int currentChangeAssigneeIndex = changeAssigneeCB.getSelectedIndex();
		
		if(assignCB.getSelectedIndex() > 1)	//leaves the current selection null if no selection made
			currentAssigneeID = ((Organization)assignCB.getSelectedItem()).getID();
		
		if(changeAssigneeCB.getSelectedIndex() > 1)
			currentChangeAssigneeID = ((Organization)changeAssigneeCB.getSelectedItem()).getID();
		
		assignCB.setSelectedIndex(0);
		sortAssigneeID = 0;
		changeAssigneeCB.setSelectedIndex(0);
		
		assignCBM.removeAllElements();
		changeAssigneeCBM.removeAllElements();
		
		assignCBM.addElement(new Organization(0, "Any", "Any"));
		assignCBM.addElement(new Organization(-1, "None", "None"));
		changeAssigneeCBM.addElement(new Organization(-1, "No Change", "No Change"));
		changeAssigneeCBM.addElement(new Organization(-1, "None", "None"));
		
		for(Organization confOrg :orgs.getConfirmedOrgList())
		{
			assignCBM.addElement(confOrg);
			changeAssigneeCBM.addElement(confOrg);
		}
		
		//Attempt to reselect the previous selected organization, if one was selected. 
		//If the previous selection is no longer in the drop down, the top of the list is 
		//already selected 
		if(currentAssigneeIndex == 1)	//Organization == "None", ID = -1
		{
			assignCB.setSelectedIndex(1);
			sortAssigneeID = -1;
		}
		else if(currentAssigneeIndex > 1)
		{
			Organization assigneeOrg = orgs.getOrganizationByID(currentAssigneeID);
			if(assigneeOrg != null)
			{
				assignCB.setSelectedItem(assigneeOrg);
				sortAssigneeID = assigneeOrg.getID();	//Need to update the sort Assignee as well
			}
		}
		
		if(currentChangeAssigneeIndex == 1)		//Organization == "None"
			changeAssigneeCB.setSelectedIndex(1);
		else
		{
			Organization changeAssigneeOrg = orgs.getOrganizationByID(currentChangeAssigneeID);
			if(changeAssigneeOrg != null)
				changeAssigneeCB.setSelectedItem(changeAssigneeOrg);
		}
		
		bIgnoreSortDialogEvents = false;
	}
/*	
	void updateUserList(ArrayList<ONCUser> uAL)
	{
		changedByCB.setEnabled(false);
		
		for(ONCUser user:uAL)
			changedByCBM.addElement(user.getLNFI());
		
		changedByCB.setEnabled(true);
	}
*/	
	void updateUserList()
	{	
		UserDB userDB = UserDB.getInstance();

		bIgnoreSortDialogEvents = true;
		changedByCB.setEnabled(false);
		String curr_sel = changedByCB.getSelectedItem().toString();
		int selIndex = 0;
		
		changedByCBM.removeAllElements();
		
		changedByCBM.addElement("Anyone");

		int index = 0;
		for(ONCUser user:userDB.getUserList())
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.setEnabled(true);
		bIgnoreSortDialogEvents = false;
	}
	
	void addUser(String user)
	{
		changedByCB.setEnabled(false);		
		changedByCBM.addElement(user);
		changedByCB.setEnabled(true);
	}

	void onPrintReceivingCheckSheets()
	{
		if(sortTable.getSelectedRowCount() > 0)	//Only print selected rows
		{
			//build the array list to print. For each row selected by the user, get the family object 
			//reference and build the packaging sheet array list. The packaging sheet array list is
			//used by the print method to print each page. A family packaging sheet may have two pages
			//if the family has more than 5 children
			ArrayList<ONCReceivingSheet> rsAL = new ArrayList<ONCReceivingSheet>();
			
			int totalpages = sortTable.getSelectedRowCount() / RS_ITEMS_PER_PAGE + 1;
//			int[] row_sel = sortTable.getSelectedRows();
			
			for(int i=0; i < totalpages; i++)
			{
				int countonpage = sortTable.getSelectedRowCount() - i*RS_ITEMS_PER_PAGE;
				int count = countonpage > RS_ITEMS_PER_PAGE ? RS_ITEMS_PER_PAGE : countonpage;
				rsAL.add(new ONCReceivingSheet(((Organization) assignCB.getSelectedItem()).getName(), 
											   i*RS_ITEMS_PER_PAGE, count, i+1, totalpages));
			}
			
			//Create the info required for the print job
			SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(twodigitYear.format(gvs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
			String oncSeason = "ONC " + Integer.toString(gvs.getCurrentSeason());			
			
			//Create the print job
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(new ReceivingGiftCheckSheetPrinter(rsAL, img, oncSeason));
				
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	void onPrintListing()
	{
		if(sortTable.getRowCount() > 0)
		{
			try
			{
				MessageFormat headerFormat = new MessageFormat("ONC Wishes");
				MessageFormat footerFormat = new MessageFormat("- {0} -");
				sortTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
			} 
			catch (PrinterException e) 
			{
				JOptionPane.showMessageDialog(this, 
						"Print Error: " + e.getMessage(), 
						"Print Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				e.printStackTrace();
			}
		}
		else	//Warn user that the table is empty
			JOptionPane.showMessageDialog(this, 
					"No wishes in table to print, table must contain wishes in order to" + 
					"print listing", 
					"No Wishes To Print", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
		
	void checkApplyChangesEnabled()
	{
//		System.out.println(String.format("Checking enabling of Apply Changes button: %d, %d, %d, %d", 
//				sortTable.getSelectedRowCount(), changeResCB.getSelectedIndex(),
//				changeStatusCB.getSelectedIndex(), changeAssigneeCB.getSelectedIndex()));
		
		if(sortTable.getSelectedRows().length > 0 &&
				(changeResCB.getSelectedIndex() > 0 || changeStatusCB.getSelectedIndex() > 0 ||changeAssigneeCB.getSelectedIndex() > 0))	
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
		
	}
	
	void checkExportEnabled()
	{
		if(sortTable.getSelectedRowCount() > 0)
			btnExport.setEnabled(true);
		else
			btnExport.setEnabled(false);
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"ONC #", "Gender", "Age on 12/25", "DoB", "Res", "Wish", "Detail", "Status", 
    						"Assignee", "Changed By", "Date Changed"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       										new FileNameExtensionFilter("CSV Files", "csv"), 1);
       	if(oncwritefile!= null)
       	{
       		//If user types a new filename without extension.csv, add it
	    	String filePath = oncwritefile.getPath();
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    int[] row_sel = sortTable.getSelectedRows();
	    	    for(int i=0; i<sortTable.getSelectedRowCount(); i++)
	    	    {
	    	    	int index = row_sel[i];
	    	    	writer.writeNext(stAL.get(index).getExportRow());
	    	    }
	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(this, 
						sortTable.getSelectedRowCount() + " wishes sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}
	
//	ListSelectionModel getSortWishTableLSM() { return sortTable.getSelectionModel(); }
	
//	int getSelectedSortItemONCID(){return stAL.get(sortTable.getSelectedRow()).getSortItemONCID();}
	
//	public ONCChild getSelectedSortItemONCChild()	{return stAL.get(sortTable.getSelectedRow()).getSortItemChild();}
	
//	boolean isSortTableChanging() { return bChangingTable; }
	
	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	private boolean isAgeInRange(ONCChild c)
	{
		return c.getChildIntegerAge() >= startAgeCB.getSelectedIndex() && c.getChildIntegerAge() <= endAgeCB.getSelectedIndex();		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	private boolean doesWishNumMatch(int wn)
	{
		return sortWishNum == 0 || sortWishNum == wn+1;		
	}
	
	private boolean doesResMatch(int res) { return sortRes == 0  || sortRes == res+1; }
	
	private boolean doesStatusMatch(int status){return sortStatus == 0 || sortStatus == status;}
	
	private boolean doesAssigneeMatch(int assigneeID)
	{	
		return sortAssigneeID == 0 || sortAssigneeID == assigneeID;
	}
	
	private boolean isWishChangeDateBetween(Calendar wcd)
	{
		return !wcd.getTime().after(sortEndCal.getTime()) && !wcd.getTime().before(sortStartCal.getTime());
	}
		
//	private boolean doesWishBaseMatch(String wb)	{return sortWish.equals("Any") ||  sortWish.equals(wb);}	
	private boolean doesWishBaseMatch(int wishID)	{return sortWishID == -2 ||  sortWishID == wishID; }
	
	private boolean doesChangedByMatch(String s) { return sortChangedBy == 0 || changedByCB.getSelectedItem().toString().equals(s); }
	
	void setSortStartDate(Date sd) {sortStartCal.setTime(sd); ds.setDate(sortStartCal.getTime());}
	
	void setSortEndDate(Date ed) {sortEndCal.setTime(ed); sortEndCal.add(Calendar.DATE, 1); de.setDate(sortEndCal.getTime());}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == startAgeCB && startAgeCB.getSelectedIndex() != sortStartAge)
		{
			sortStartAge = startAgeCB.getSelectedIndex();
			bSortTableBuildRqrd = true;			
		}		
		else if(e.getSource() == endAgeCB && endAgeCB.getSelectedIndex() != sortEndAge)
		{
			sortEndAge = endAgeCB.getSelectedIndex();
			bSortTableBuildRqrd = true;		
		}
		else if(e.getSource() == genderCB && genderCB.getSelectedIndex() != sortGender)
		{
			sortGender = genderCB.getSelectedIndex();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == wishnumCB && wishnumCB.getSelectedIndex() != sortWishNum)
		{
			sortWishNum = wishnumCB.getSelectedIndex();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == wishCB && !bIgnoreSortDialogEvents && 
				((ONCWish)wishCB.getSelectedItem()).getID() != sortWishID)
		{						
			sortWishID = ((ONCWish)wishCB.getSelectedItem()).getID();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == changedByCB && changedByCB.getSelectedIndex() != sortChangedBy && !bIgnoreSortDialogEvents)
		{						
			sortChangedBy = changedByCB.getSelectedIndex();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == resCB && resCB.getSelectedIndex() != sortRes )
		{						
			sortRes = resCB.getSelectedIndex();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == statusCB && statusCB.getSelectedIndex() != sortStatus )
		{						
			sortStatus = statusCB.getSelectedIndex();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == assignCB && !bIgnoreSortDialogEvents && 
				((Organization)assignCB.getSelectedItem()).getID() != sortAssigneeID )
		{						
			sortAssigneeID = ((Organization)assignCB.getSelectedItem()).getID();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == btnResetCriteria)
		{
			bResetInProcess = true;	//Prevent building of new sort table by event handlers
			
			oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
			sortONCNum = "";
			startAgeCB.setSelectedIndex(0);	//Will trigger the CB event handler which
			endAgeCB.setSelectedIndex(21);	//will determine if the CB changed. Therefore,
			genderCB.setSelectedIndex(0);	//no need to test for a change here.
			wishnumCB.setSelectedIndex(0);
			wishCB.setSelectedIndex(0);
			changedByCB.setSelectedIndex(0);
			resCB.setSelectedIndex(0);
			statusCB.setSelectedIndex(0);
			assignCB.setSelectedIndex(0);
			changeResCB.setSelectedIndex(0);
			changeStatusCB.setSelectedIndex(0);
			changeAssigneeCB.setSelectedIndex(0);
			
			tableSortCol = -1;	//reset table to unsorted
			tableRowSelectedItemIDList.clear(); //clear selected item id list
			
			//Check to see if date sort criteria has changed. Since the setDate() method
			//will not trigger an event, must check for a sort criteria date change here
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			if(!sdf.format(sortStartCal.getTime()).equals(sdf.format(gvs.getSeasonStartDate())))
			{
				sortStartCal.setTime(gvs.getSeasonStartDate());
				ds.setDate(sortStartCal.getTime());	//Will not trigger the event handler
			}
			
			if(!sdf.format(sortEndCal.getTime()).equals(sdf.format(gvs.getTomorrowsDate())))
			{
				sortEndCal.setTime(gvs.getTomorrowsDate());
				de.setDate(sortEndCal.getTime());	//Will not trigger the event handler
			}
				
			bSortTableBuildRqrd = true;	//Force a table build to reset order change from header click
			bResetInProcess = false;	//Restore sort table build by event handlers
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1)	//Can always print listing
			{
				onPrintListing();
			} 
			else	//Can only print if table rows are selected
			{
				if(sortTable.getSelectedRowCount() > 0)
				{
					if(printCB.getSelectedIndex() == 2)
						onPrintLabels();
					else if(printCB.getSelectedIndex() == 3)
						onPrintReceivingCheckSheets();
				}
				else	//Warn user
				{
					JOptionPane.showMessageDialog(this, 
    					"No wishes selected, please selected wishes in order to" + 
    					printCB.getSelectedItem().toString(), 
    					"No Wishes Selected", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					printCB.setSelectedIndex(0);
				}
			}
		}
		else if(e.getSource() == btnExport)
		{
			onExportRequested();	
		}
		else if(e.getSource() == btnApplyChanges)
		{
			onApplyWishChanges();
		}
		else if(!bIgnoreSortDialogEvents && (e.getSource() == changeResCB ||
					e.getSource() == changeStatusCB || e.getSource() == changeAssigneeCB))
		{
			checkApplyChangesEnabled();
		}
		
		//Only build one time if multiple changes occur, i.e. Reset Button event
		if(bSortTableBuildRqrd && !bResetInProcess )
		{
			buildSortTableList();
			bSortTableBuildRqrd = false;
		}
	}
	
	void onPrintLabels()
	{
		if(sortTable.getSelectedRowCount() > 0)	 //Print selected rows. If no rows selected, do nothing
		{
			totalNumOfLabelsToPrint = sortTable.getSelectedRowCount();
		
			PrinterJob pj = PrinterJob.getPrinterJob();

			pj.setPrintable(new AveryWishLabelPrinter());
         
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
        printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	

	@Override
	public void propertyChange(PropertyChangeEvent pce)
	{
		//If the date has changed in either date chooser, then rebuild the sort table. Note, setting
		//the date using setDate() does not trigger a property change, only triggered by user action. 
		//So must rebuild the table each time a change is detected. 
		if("date".equals(pce.getPropertyName()) &&
				(!sortStartCal.getTime().equals(ds.getDate()) || !sortEndCal.getTime().equals(de.getDate())))
		{
			sortStartCal.setTime(ds.getDate());
			sortEndCal.setTime(de.getDate());
			buildSortTableList();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel() &&
				!bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getSortItemFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getSortItemChild();
			fireEntitySelected(this, "WISH_SELECTED", fam, child);
			
			//determine if a partner has been assigned for the selected wish
			int wn = stAL.get(sortTable.getSelectedRow()).getSortItemChildWishNumber();
			int childID = stAL.get(sortTable.getSelectedRow()).getSortItemChild().getID();
			ONCChildWish cw = cwDB.getWish(childID, wn);
			int childWishAssigneeID = cw.getChildWishAssigneeID();
			if(childWishAssigneeID > -1)
			{
				Organization org = orgs.getOrganizationByID(childWishAssigneeID);
				fireEntitySelected(this, "PARTNER_SELECTED", org, null);
				
			}
			
			sortTable.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
		checkExportEnabled();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && (dbe.getType().equals("WISH_ADDED") ||
										dbe.getType().equals("UPDATED_CHILD") || 
										 dbe.getType().equals("DELETED_CHILD")))
			
		{
//			System.out.println(String.format("Sort Wish Dialog DB event, Source: %s, Type %s, Object: %s",
//												dbe.getSource().toString(), 
//												dbe.getType(), 
//												dbe.getObject().toString()));
			buildSortTableList();
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("DELETED_CONFIRMED_PARTNER")) ||
											dbe.getType().equals("UPDATED_CONFIRMED_PARTNER"))
		{
			updateWishAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			updateWishAssigneeSelectionList();
			buildSortTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG_WISH"))
		{			
			updateWishSelectionList();
		}
		else if(dbe.getType().equals("LOADED_USERS") || dbe.getType().equals("ADDED_USER"))
		{
			updateUserList();
		}
	}
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	/**************************************************************************************
	 * This class prints check sheets for verifying receipt of gifts from ONC partners. 
	 * 
	 * This class requires an array list, passed in the constructor, that hold a
	 * ONCReceivingSheet object for each page in the document to be printed
	 * by this class.
	 * @author johnwoneill
	 ************************************************************************************/
	private class ReceivingGiftCheckSheetPrinter implements Printable
	{	
		ArrayList<ONCReceivingSheet> rsAL;
		Image img;
		String oncSeason;
		
		ReceivingGiftCheckSheetPrinter(ArrayList<ONCReceivingSheet> rsal, Image img, String season)
		{
			rsAL = rsal;
			this.img = img;
			oncSeason = season;
		}
		
		/*********************************************************************************************
		 * This method draws a gift check box rectangle at the specified x, y location
		 ********************************************************************************************/
		void drawThickRect(Graphics2D g2d, int x, int y, int width, int height)
		{
			float thickness = new Float(1.5);
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(thickness));
			g2d.drawRect(x, y, width, height);
			g2d.setStroke(oldStroke);
		}
/*		
		void drawFilledThickRect(Graphics2D g2d, int x, int y, int width, int height, Color color)
		{
			float thickness = new Float(1.5);
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(thickness));
			g2d.setPaint(color);
			g2d.fill(new Rectangle(x, y, width, height));
			g2d.setPaint(Color.BLACK);
			g2d.setStroke(oldStroke);
		}
*/		
		void printReceivingSheetHeader(int x, int y, Image img, String season, String org, Font[] psFont, Graphics2D g2d)
		{
			 double scaleFactor = (72d / 300d) * 2;
     	     
			    // Now we perform our rendering 	       	    
			    int destX1 = (int) (img.getWidth(null) * scaleFactor);
			    int destY1 = (int) (img.getHeight(null) * scaleFactor);
			    
			    //Draw image scaled to fit image clip region on the label
			    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
		         
			    //Draw the ONC Season
			    g2d.setFont(psFont[1]);
			    g2d.drawString(season, x+54, y+22);
			    
			    //Draw the receiving sheet title
				g2d.setFont(psFont[1]);
				g2d.drawString("ONC Partner Receiving Check Sheet", x+160, y+20);
//				g2d.drawString("ONC Receiving Status Sheet", x+180, y+20);
				
			    //Draw first 32 characters of organization name
			    g2d.setFont(psFont[1]);
			    
			    String orgname = org.length() > 32 ? org.substring(0,31) : org;
//			    System.out.println(g2d.getFontMetrics(psFont[1]).stringWidth(orgname));
				g2d.drawString(orgname, x+374, y+20); 	//Organization name
				
				//Draw the receiving check instruction
			    g2d.setFont(psFont[0]);
			    g2d.drawString("CHECK BOX(     ) IF ITEM WAS RECEIVED", x+10, y+56);
//			    g2d.drawString("BOX IS GREEN IF ITEM WAS RECEIVED, RED IF ITEM NOT RECEIVED", x+10, y+56);
			    drawThickRect(g2d, x+70, y+46, 12, 12);
			    
			    //Draw the Checked By:
			    g2d.drawString("Checked By:", x+370, y+56);
			    
			  //Draw separator line
				g2d.drawLine(x, y+62, x+525, y+62);
		}
		
		void printReceivingSheetFooter(int x, int y, int pgNum, int pgTotal, Font[] psFont, Graphics2D g2d)
		{
			//Draw separator line
			g2d.drawLine(x, y, x+525, y);
			
			//Draw Packager Comments instruction
			g2d.setFont(psFont[0]);
			g2d.drawString("NOTES/SPECIAL COMMENTS:", x, y+16); 	//Comments note
			
			//Draw the page number 
			g2d.setFont(psFont[0]);
			String pageinfo = String.format("%d of %d", pgNum, pgTotal);
			g2d.drawString(pageinfo, x+262, y+76);
		}

		void printReceivingSheetBody(int x, int y, ArrayList<ONCWishDlgSortItem> stAL, int index, int linesonpage,
										Font[] psFont, Graphics2D g2d)
		{		
			for(int line=0; line< linesonpage; line++)
			{
				int[] row_sel = sortTable.getSelectedRows();
				String[] linedata = stAL.get(row_sel[index+line]).getReceivingSheetRow();
				drawThickRect(g2d, x, y+line*20, 12, 12);
//				if(line < 4)
//					drawFilledThickRect(g2d, x, y+line*20, 12, 12, Color.GREEN);
//				else
//					drawFilledThickRect(g2d, x, y+line*20, 12, 12, Color.RED);
				y += 10;
				g2d.drawString("Family " + linedata[0], x+20, y+line*20); 	//ONC Number
				g2d.drawString(linedata[1], x+88, y+line*20); 	//Age & Gender
				g2d.drawString(linedata[2], x+160, y+line*20); 	//Wish & Detail
			}
		}
		
		@Override
		public int print (Graphics g, PageFormat pf, int page) throws PrinterException
		{		
			if(page > rsAL.size())	//'page' is zero-based 
			{
				return NO_SUCH_PAGE;
		    }
			
			//Create 2d graphics context
			// User (0,0) is typically outside the imageable area, so we must
		    //translate by the X and Y values in the PageFormat to avoid clipping
		    Graphics2D g2d = (Graphics2D)g;
		    g2d.translate(pf.getImageableX(), pf.getImageableY());
			
			//Create fonts
			Font[] cFonts = new Font[5];
		    cFonts[0] = new Font("Calibri", Font.PLAIN, 12);//Instructions Font
		    cFonts[1] = new Font("Calibri", Font.BOLD, 14);	//Season Font
		    cFonts[2] = new Font("Calibri", Font.BOLD, 20);	//ONC Num Text Font
		    cFonts[3] = new Font("Calibri", Font.BOLD, 12);	//Child Font
		    cFonts[4] = new Font("Calibri", Font.BOLD, 13);	//Footer Font
			
			//Print page header
		    String org = rsAL.get(page).getRSOrg();;
			printReceivingSheetHeader(16, 0, img, oncSeason, org, cFonts, g2d);
			
			//Print page body
			printReceivingSheetBody(16, 70, stAL, rsAL.get(page).getRSIndex(), rsAL.get(page).getRSCount(),
										cFonts, g2d);			
			//Print page footer
			int pagetotal = rsAL.get(page).getRSTotalpages();
			printReceivingSheetFooter(16, 656, rsAL.get(page).getRSPage(), pagetotal, cFonts, g2d);
		    	    
		    //tell the caller that this page is part of the printed document	   
		    return PAGE_EXISTS;
	  	}
	}
	private class ONCReceivingSheet
	{	
		private String org;
		private int index;
		private int count;
		private int page;
		private int totalpages;
		
		ONCReceivingSheet(String o, int idx, int c, int p, int tp)
		{	
			org = o;
			index = idx;
			count = c;
			page = p;
			totalpages = tp;
		}
		
		//getters
		String getRSOrg() { return org; }
		int getRSIndex() { return index; }
		int getRSCount() { return count; }
		int getRSPage() { return page; }
		int getRSTotalpages() { return totalpages; }
	}
		
	private class AveryWishLabelPrinter implements Printable
	{		
		private static final int AVERY_LABEL_X_OFFSET = 30;
		private static final int AVERY_LABEL_Y_OFFSET = 50;
		private static final int AVERY_LABELS_PER_PAGE = 30;
		private static final int AVERY_COLUMNS_PER_PAGE = 3;
		private static final int AVERY_LABEL_HEIGHT = 72;
		private static final int AVERY_LABEL_WIDTH = 196;
		
		void printLabel(int x, int y, String[] line, Font[] lFont, Image img, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null),null); 
	         
		    //Draw the label text, either 3 or 4 lines, depending on the wish base + detail length
		    g2d.setFont(lFont[0]);
		    drawCenteredString(line[0], 120, x+50, y+5, g2d);	//Draw line 1
		     
		    g2d.setFont(lFont[1]);
		    drawCenteredString(line[1], 120, x+50, y+20, g2d);	//Draw line 2
		    
		    if(line[3] == null)	//Only a 3 line label
		    {
		    	g2d.setFont(lFont[2]);
		    	drawCenteredString(line[2], 120, x+50, y+35, g2d);	//Draw line 3
		    }
		    else	//A 4 line label
		    {	    	
		    	drawCenteredString(line[2], 120, x+50, y+35, g2d);	//Draw line 3	    	
		    	g2d.setFont(lFont[2]);
		    	drawCenteredString(line[3], 120, x+50, y+50, g2d);	//Draw line 4
		    }	    
		}
		
		private void drawCenteredString(String s, int width, int XPos, int YPos, Graphics2D g2d)
		{  
	        int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();  
	        int start = width/2 - stringLen/2;  
	        g2d.drawString(s, start + XPos, YPos);  
		}  
		
		@Override
		public int print(Graphics g, PageFormat pf, int page) throws PrinterException
		{
			
			if (page > (totalNumOfLabelsToPrint+1)/AVERY_LABELS_PER_PAGE)		//'page' is zero-based 
			{ 
				return NO_SUCH_PAGE;
		    }
			
			SimpleDateFormat sYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(sYear.format(gvs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();
		     
			Font[] lFont = new Font[3];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 11);
		    lFont[1] = new Font("Calibri", Font.BOLD, 12);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 10);	     
		    
		    int endOfSelection = 0, index = 0;
		    int[] row_sel = sortTable.getSelectedRows();
	 		if(sortTable.getSelectedRowCount() > 0)
	 		{	//print a label for each row selected
	 			index = page * AVERY_LABELS_PER_PAGE;
	 			endOfSelection = row_sel.length;
	 		}
		    	 
		    // User (0,0) is typically outside the imageable area, so we must
		    //translate by the X and Y values in the PageFormat to avoid clipping
		    Graphics2D g2d = (Graphics2D)g;
		    g2d.translate(AVERY_LABEL_X_OFFSET, AVERY_LABEL_Y_OFFSET);	//For a 8150 Label
		    
		    String line[];
		    int row = 0, col = 0;
		    
		    while(row < AVERY_LABELS_PER_PAGE/AVERY_COLUMNS_PER_PAGE && index < endOfSelection)
		    {
		    	line = stAL.get(row_sel[index++]).getWishLabel();
		    	printLabel(col * AVERY_LABEL_WIDTH, row * AVERY_LABEL_HEIGHT, line, lFont, img, g2d);	
		    	if(++col == AVERY_COLUMNS_PER_PAGE) { row++; col = 0; } 	
		    }
		    	    
		     /* tell the caller that this page is part of the printed document */
		     return PAGE_EXISTS;
		}
	}

	
	private class ONCSortItemAgeComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildDOB().compareTo(o2.getSortItemChildDOB());
		}
	}
	
	private class ONCSortItemFamNumComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			Integer onc1, onc2;
			
			if(!o1.getSortItemONCFamilyNum().isEmpty() && isNumeric(o1.getSortItemONCFamilyNum()))
				onc1 = Integer.parseInt(o1.getSortItemONCFamilyNum());
			else
				onc1 = MAXIMUM_ON_NUMBER;
							
			if(!o2.getSortItemONCFamilyNum().isEmpty() && isNumeric(o2.getSortItemONCFamilyNum()))
				onc2 = Integer.parseInt(o2.getSortItemONCFamilyNum());
			else
				onc2 = MAXIMUM_ON_NUMBER;
			
			return onc1.compareTo(onc2);
		}
	}
	
	private class ONCSortItemGenderComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildGender().compareTo(o2.getSortItemChildGender());
		}
	}
	
	private class ONCSortItemWishNumComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishNumber().compareTo (o2.getSortItemChildWishNumber());
		}
	}
	
	private class ONCSortItemWishBaseComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishBase().compareTo(o2.getSortItemChildWishBase());
		}
	}
	
	private class ONCSortItemWishDetailComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishDetail().compareTo(o2.getSortItemChildWishDetail());
		}
	}
	
	private class ONCSortItemWishIndicatorComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishIndicator().compareTo(o2.getSortItemChildWishIndicator());
		}
	}
	
	private class ONCSortItemWishStatusComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishStatus().compareTo(o2.getSortItemChildWishStatus());
		}
	}
	
	private class ONCSortItemWishAssigneeComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishAssignee().compareTo(o2.getSortItemChildWishAssignee());
		}
	}
	
	private class ONCSortItemWishChangedByComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishChangedBy().compareTo(o2.getSortItemChildWishChangedBy());
		}
	}
	
	private class ONCSortItemWishDateChangedComparator implements Comparator<ONCWishDlgSortItem>
	{
		@Override
		public int compare(ONCWishDlgSortItem o1, ONCWishDlgSortItem o2)
		{
			return o1.getSortItemChildWishDateChanged().compareTo(o2.getSortItemChildWishDateChanged());
		}
	}
	
	/***********************************************************************************
	 * This class implements a key listener for the ReceiveGiftDialog class that
	 * listens to the ONC Number text field to determine when it is empty. If it becomes empty,
	 * the listener rebuilds the sort table array list
	 ***********************************************************************************/
	 private class ONCNumberKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
			if(oncnumTF.getText().isEmpty())
			{
				sortONCNum = "";
				buildSortTableList();
			}	
		}
	 }
}
