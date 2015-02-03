package OurNeighborsChild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class SortDriverDialog extends ONCTableDialog implements ActionListener, PropertyChangeListener,
																	DatabaseListener, ListSelectionListener
{
	private static final int NUM_AGENT_ROWS_TO_DISPLAY = 10;
	private static final int NUM_FAMILY_ROWS_TO_DISPLAY = 15;
//	private static final String AGENT_EMAIL_SENDER_ADDRESS = "somerss@cox.net";
	private static final String AGENT_EMAIL_SENDER_ADDRESS = "volunteer@ourneighborschild.org";
//	private static final String AGENT_EMAIL_SENDER_ADDRESS = "johnwoneill1@gmail.com";
	private static final int MIN_EMAIL_ADDRESS_LENGTH = 2;
	private static final int MIN_EMAIL_NAME_LENGTH = 2;
	private static final Integer MAXIMUM_ON_NUMBER = 9999;
	private static final int ONC_GENERAL_USER = 0;
	
	private ONCRegions regions;
	public ONCTable sortTable, dependentTable;
	private DefaultTableModel sortDriverTableModel, familyTableModel;
	private JComboBox orgCB, titleCB;
	private DefaultComboBoxModel orgCBM, titleCBM;
	private JButton btnResetCriteria;
	private JButton btnEditAgentInfo;
	private JComboBox agentPrintCB, printCB, emailCB;
	private JLabel lblNumOfAgents, lblNumOfFamilies;
	private Families fDB;
	private ONCAgents agentDB;
	private ArrayList<Agent> atAL;	//Holds references to agent objects for agent table
	private ArrayList<ONCFamily> stAL;	//Holds references to family objects for family table
	
	private boolean bChangingAgentTable = false;	//Semaphore used to indicate the sort table is being changed
	private boolean bChangingFamilyTable = false;	//Semaphore used to indicate the sort table is being changed
	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
	private boolean bIgnoreSortDialogEvents = false;
	
	private String sortOrg, sortTitle;
	
	private JProgressBar progressBar;
	private ONCEmailer oncEmailer;
	
	private AgentInfoDialog aiDlg;
	
	private static String[] famstatus = {"Any","Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
	private static String[] delstatus = {"Any", "Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
	private static String[] stoplt = {"Any", "Green", "Yellow", "Red", "Off"};
	
	public SortDriverDialog(JFrame parentFrame)
	{
		super(parentFrame);
		
		regions = ONCRegions.getInstance();
		fDB = Families.getInstance();
		agentDB = ONCAgents.getInstance();
		this.setTitle("Our Neighbor's Child - Agent Management");
		
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		if(agentDB != null)
			agentDB.addDatabaseListener(this);
		
		//Set up the table content array lists
		atAL = new ArrayList<Agent>();
		stAL = new ArrayList<ONCFamily>();
		
		//Initialize the sort criteria variables
		sortOrg = "Any";
		sortTitle = "Any";
		
		//Set up the search criteria panel      
		JPanel sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));
		JPanel sortCriteriaPanelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));		

		JLabel lblONCicon = new JLabel(gvs.getImageIcon(0));
    	 	
		orgCB = new JComboBox();
		orgCBM = new DefaultComboBoxModel();
	    orgCBM.addElement("Any");
	    orgCBM.addElement("Deer Park ES");
	    orgCB.setModel(orgCBM);
		orgCB.setPreferredSize(new Dimension(144, 56));
		orgCB.setBorder(BorderFactory.createTitledBorder("Organization"));
		orgCB.addActionListener(this);
		
		titleCB = new JComboBox();
		titleCBM = new DefaultComboBoxModel();
	    titleCBM.addElement("Any");
	    titleCB.setModel(titleCBM);
		titleCB.setPreferredSize(new Dimension(144, 56));
		titleCB.setBorder(BorderFactory.createTitledBorder("Title"));
		titleCB.addActionListener(this);
		
		//Add all sort criteria components to dialog pane
        sortCriteriaPanelTop.add(lblONCicon);
        sortCriteriaPanelTop.add(orgCB);
		sortCriteriaPanelTop.add(titleCB);				
		
		sortCriteriaPanel.add(sortCriteriaPanelTop);
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
		
		//Set up the sort table panel
//		JPanel sortTablePanel = new JPanel();
		
		String[] colToolTips = {"Name", "Organization", "Title", "EMail Address", "Phone"};
		sortTable = new ONCTable(colToolTips, new Color(240,248,255));

		String[] columns = {"Name", "Org", "Title", "EMail", "Phone"};		
        sortDriverTableModel = new DefaultTableModel(columns, 0)
        {
        	private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
     
        sortTable.setModel(sortDriverTableModel);
        sortTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);   
//	    sortAgentTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        
	  //Set table column widths
	  		int tablewidth = 0;
	  		int[] colWidths = {144, 160, 120, 168, 120};
	  		for(int i=0; i < colWidths.length; i++)
	  		{
	  			sortTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
	  			tablewidth += colWidths[i];
	  		}
	  		tablewidth += 24; 	//Account for vertical scroll bar
       
        
        JTableHeader anHeader = sortTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //mouse listener for header click
        anHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	if(sortTable.columnAtPoint(e.getPoint()) == 0)	//Sort on Agent Name
            		Collections.sort(atAL, agentDB.getAgentNameComparator());
            	else if(sortTable.columnAtPoint(e.getPoint()) == 1)	// Sort on Agent Organization
            		Collections.sort(atAL, agentDB.getAgentOrgComparator());
            	else if (sortTable.columnAtPoint(e.getPoint()) == 2)	//Sort on Agent Title
            		Collections.sort(atAL, agentDB.getAgentTitleComparator());
            	else
            		return;
            	
//            	displaySortAgentTable();
            }
        });
        
        sortTable.setFillsViewportHeight(true);
        sortTable.getSelectionModel().addListSelectionListener(this);
        
        //Create the scroll pane and add the table to it.
        JScrollPane sortAgentScrollPane = new JScrollPane(sortTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
        												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        sortAgentScrollPane.setPreferredSize(new Dimension(tablewidth, sortTable.getRowHeight()*NUM_AGENT_ROWS_TO_DISPLAY));
        sortAgentScrollPane.setBorder(BorderFactory.createTitledBorder("ONC Agents"));
        sortAgentScrollPane.setBorder(BorderFactory.createTitledBorder(
        								BorderFactory.createLoweredBevelBorder(), "Agents"));
        
        //Set up the third panel
        
        //Set up the bottom panel
      	JPanel thirdpanel = new JPanel(new BorderLayout());
      	
      	//Set up the family count panel
      	JPanel agentcountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      	JLabel agentCountMssg = new JLabel("Number of Agents:");
        lblNumOfAgents = new JLabel();
        agentcountPanel.add(agentCountMssg);
        agentcountPanel.add(lblNumOfAgents);
      
        //Set up the middle control panel
      	JPanel middlecntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      
      	//Set up the email progress bar
      	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        String[] emailChoices = {"Email", "2014 Season Agent Email", "2014 Season Reminder Email",
        							"2014 Intake Reminder Email", "2014 December Gift Confirmation Email"};
        emailCB = new JComboBox(emailChoices);
        emailCB.setPreferredSize(new Dimension(136, 28));
        emailCB.setEnabled(false);
        emailCB.addActionListener(this);
              
      	//Create a print button for agent information
      	String[] agentPrintChoices = {"Print", "Print Agent Listing"};
        agentPrintCB = new JComboBox(agentPrintChoices);
        agentPrintCB.setPreferredSize(new Dimension(136, 28));
        agentPrintCB.setEnabled(false);
        agentPrintCB.addActionListener(this);
        
      	//Create the middle control panel buttons
      	btnEditAgentInfo = new JButton("Edit Agent Info");
      	btnEditAgentInfo.setEnabled(false);
      	btnEditAgentInfo.addActionListener(this);
      	
        btnResetCriteria = new JButton("Reset Criteria");
        btnResetCriteria.addActionListener(this);
                      
        //Add the components to the control panel
        middlecntlPanel.add(progressBar);
        middlecntlPanel.add(emailCB);
        middlecntlPanel.add(agentPrintCB);
        middlecntlPanel.add(btnEditAgentInfo);
        middlecntlPanel.add(btnResetCriteria);
              
        //Add family count and control panels to bottom panel
        thirdpanel.add(agentcountPanel, BorderLayout.LINE_START);
        thirdpanel.add(middlecntlPanel, BorderLayout.LINE_END);
           
        //Set up the family table panel
        String[] colTT = {"ONC Family Number", "Batch Number", "Do Not Serve Code", 
					"Family Status", "Delivery Status", "Head of Household First Name", 
					"Head of Household Last Name", "House Number","Street", "Unit",
					"Zip Code", "Region","Changed By", "Stoplight Color"};
        
        dependentTable = new ONCTable(colTT, new Color(240,248,255));

      	final String[] ftcolumns = {"ONC", "Batch #", "DNS", "Fam Status", "Del Status", "First", "Last", "House",
      							"Street", "Unit", "Zip", "Reg", "Changed By", "SL"};
      	familyTableModel = new DefaultTableModel(ftcolumns, 0)
        {
      		private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
           
              
        dependentTable.setModel(familyTableModel);
        dependentTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        dependentTable.getSelectionModel().addListSelectionListener(this);
              
      	dependentTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
              
      	//Set table column widths
      	int familytablewidth = 0;
      	int[] familycolWidths = {32, 48, 48, 72, 72, 72, 72, 48, 128, 56, 48, 24, 72, 24};
      	for(int i=0; i < familycolWidths.length; i++)
      	{
      	  	dependentTable.getColumnModel().getColumn(i).setPreferredWidth(familycolWidths[i]);
      	  	familytablewidth += familycolWidths[i];
      	}
      	familytablewidth += 24; 	//Account for vertical scroll bar
             
              
        JTableHeader ftHeader = dependentTable.getTableHeader();
        ftHeader.setForeground( Color.black);
        ftHeader.setBackground( new Color(161,202,241));
              
        //mouse listener for header click
        ftHeader.addMouseListener(new MouseAdapter()
        {
        	@Override
            public void mouseClicked(MouseEvent e)
            {
        		if(fDB.sortDB(stAL, ftcolumns[dependentTable.columnAtPoint(e.getPoint())]))
        		{
//        			clearFamilyTable();
//        			displayFamilyTable();
        		}
            }
        });		
              
        //Center cell entries for Batch # and Region
        DefaultTableCellRenderer ftcr = new DefaultTableCellRenderer();    
        ftcr.setHorizontalAlignment(SwingConstants.CENTER);
        dependentTable.getColumnModel().getColumn(1).setCellRenderer(ftcr);
        dependentTable.getColumnModel().getColumn(10).setCellRenderer(ftcr);
        dependentTable.getColumnModel().getColumn(12).setCellRenderer(ftcr);
              
        dependentTable.setFillsViewportHeight(true);    
              
        //Create the scroll pane and add the table to it.
        JScrollPane familyTableScrollPane = new JScrollPane(dependentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
              											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
              
        familyTableScrollPane.setPreferredSize(new Dimension(familytablewidth, dependentTable.getRowHeight()*NUM_FAMILY_ROWS_TO_DISPLAY));
        familyTableScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(), "Families Represented By Selected Agent(s)"));

		//Set up the bottom panel
		JPanel bottompanel = new JPanel(new BorderLayout());
		
		//Set up the family count panel
		JPanel famcountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));	
		JLabel lblFamCountMssg = new JLabel("Number of Families:");
		lblNumOfFamilies= new JLabel();
		famcountPanel.add(lblFamCountMssg);
		famcountPanel.add(lblNumOfFamilies);
        
        //Set up the control panel
		JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        String[] printChoices = {"Print", "Print Family Table"};
        printCB = new JComboBox(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
        
        //set up the Agent Info Dialog
        String[] tfNames = {"Name", "Organization", "Title", "Email", "Phone"};
    	aiDlg = new AgentInfoDialog(GlobalVariables.getFrame(), tfNames);
    	this.addEntitySelectionListener(aiDlg);
        
        //Add the components to the control panel
        cntlPanel.add(printCB);
        
        //Add family count and control panels to bottom panel
        bottompanel.add(famcountPanel, BorderLayout.LINE_START);
        bottompanel.add(cntlPanel, BorderLayout.LINE_END);
       
        
        //Add the four panels to the dialog pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
        this.add(sortCriteriaPanel);
        this.add(sortAgentScrollPane);
        this.add(thirdpanel);
        this.add(familyTableScrollPane);
        this.add(bottompanel);
       
        pack();
        setMinimumSize(new Dimension(familytablewidth+10, 516));
        setResizable(true);
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
