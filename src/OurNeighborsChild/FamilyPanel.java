package OurNeighborsChild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.json.JSONException;

import com.google.gson.Gson;

public class FamilyPanel extends JPanel implements ActionListener, ListSelectionListener,
													DatabaseListener
{
	/**
	 * This class provides the blueprint for the main panel in the ONC application. It contains a number of 
	 * GUI elements and a child sub panel
	 */
	private static final long serialVersionUID = 1L;
	private static final int FAMILY_STATUS_UNVERIFIED = 0;
	private static final int FAMILY_STATUS_INFO_VERIFIED = 1;
	private static final int FAMILY_STATUS_PACKAGED = 5;
	private static final int FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX = 6;
	private static final int DELIVERY_STATUS_ASSIGNED = 3;
	private static final String GIFT_CARD_ONLY_TEXT = "gift card only";
	
	private GlobalVariables gvs;
	private DeliveryDB deliveryDB;
	private ONCRegions regions;
	private Families fDB;
	private ChildDB cDB;
	private ONCAgents agentDB;
	
	private ONCFamily currFam;	//The panel needs to know which family is being displayed
	private ONCChild currChild;	//The panel needs to know which child is being displayed
	
	private static JFrame parentFrame = null;
	
	private JPanel p1, p2, p3;
	private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
	
	private ONCNavPanel nav;	//public to allow adding/removal of Entity Selection Listeners
	private JTextPane oncNotesPane, oncDIPane, odbWishListPane;
	private JScrollPane odbWishscrollPane;
	private JTextPane HomePhone, OtherPhone;
	public  JButton btnAssignONCNum;
	private JButton btnShowPriorHistory, btnShowAllPhones, btnShowODBDetails; 
	private JButton btnShowAgentInfo, btnShowDeliveryStatus;
	private JTextField oncDNScode;
	private JTextField HOHFirstName, HOHLastName, EMail;
	private JTextField housenumTF, Street, Unit, City, ZipCode;
	private JLabel lblONCNum, odbFamilyNum, lblRegion, lblNumBags, lblChangedBy;
	private JRadioButton delRB, altAddressRB;
	
	private JComboBox oncBatchNum, Language, statusCB, delstatCB;
	private ComboItem[] delStatus;
	public  JTable childTable;
	private DefaultTableModel childTableModel;
	private ArrayList<ONCChild> ctAL; //List holds children object references being displayed in table
	private int cn; //Child number being displayed
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	private boolean bChildTableDataChanging = true;	//Flag indicating that Child Table data is changing
//	private boolean bUnsavedFamilyDataChanges = false;	//Flag indicating family data has changed and not yet saved
//	private boolean bUnsavedCatalogChanges = false; //Flag indicating wish catalog has changed
	private boolean bDispAll = false; //Flag indicating whether all personal family data is displayed or not
	
	//An instance of the private subclass of the default highlight painter
	private static DefaultHighlighter.DefaultHighlightPainter highlightPainter = 
	        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	
	private ChildPanel oncChildPanel;
	
	//Dialogs that are children of the family panel
	private static ClientMapDialog cmDlg;
	private DeliveryStatusDialog dsDlg;
	private DirectionsDialog dirDlg;
	private DriverDialog driverDlg;
	private WishCatalogDialog catDlg;
	public SortWishDialog sortWishesDlg;
//	public GiftActionDialog recGiftsDlg;
	public SortFamilyDialog sortFamiliesDlg;
	public SortAgentDialog sortAgentDlg;
	private AgentInfoDialog agentInfoDlg;
	private ChangeONCNumberDialog changeONCNumberDlg;
	public AssignDeliveryDialog assignDeliveryDlg;
	public SortDriverDialog sortDriverDlg;
	private ViewONCDatabaseDialog dbDlg;
	private OrganizationDialog orgDlg;
	private SortPartnerDialog sortOrgsDlg;
	private ChildCheckDialog dcDlg;
	private FamilyCheckDialog dfDlg;
	
	FamilyChildSelectionListener familyChildSelectionListener;	//Listener for family/child selection events
	
	public FamilyPanel(JFrame pf)
	{
		parentFrame = pf;
		gvs = GlobalVariables.getInstance();
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		agentDB = ONCAgents.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		
		currFam = null;
		cn=0;	//Initialize the child index
		
		//register to listen for family, delivery and child data changed events
		if(fDB != null)
			fDB.addDatabaseListener(this);
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);

		//Set layout and border for the Family Panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//		this.setBorder(BorderFactory.createTitledBorder("Family Information"));
		
		//Setup the nav panel
		nav = new ONCNavPanel(pf, fDB);
		nav.setMssg("Our Neighbor's Child Families");
	    nav.setCount1("Served Families: " + Integer.toString(0));
	    nav.setCount2("Served Children: " + Integer.toString(0));
	    nav.setNextButtonText("Next Family");
	    nav.setPreviousButtonText("Previous Family");
	    familyChildSelectionListener = new FamilyChildSelectionListener();	//set up the listener
	    nav.addEntitySelectionListener(familyChildSelectionListener);	//register the listener
		
		//Setup sub panels that comprise the Family Panel
		p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pBkColor = p1.getBackground();
		JPanel p4 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JPanel p5 = new JPanel();

		//Set up the text fields for each of the characteristics displayed
		DataChangeListener fdcListener = new DataChangeListener();
        lblONCNum = new JLabel("");
        lblONCNum.setPreferredSize(new Dimension(56, 44));
        lblONCNum.setBorder(BorderFactory.createTitledBorder("ONC #"));
        lblONCNum.setHorizontalAlignment(JLabel.CENTER);
//      oncnum.setEditable(false);
//      oncnum.addActionListener(this);
        
        odbFamilyNum = new JLabel("No Fams");
        odbFamilyNum.setPreferredSize(new Dimension(72, 44));
        odbFamilyNum.setBorder(BorderFactory.createTitledBorder("ODB #"));
        lblONCNum.setHorizontalAlignment(JLabel.CENTER);
        
        String[] batchNums = {"","B-01","B-02","B-03","B-04","B-05","B-06","B-07","B-08","B-09","B-10", "B-CR"};
        oncBatchNum = new JComboBox(batchNums);
        oncBatchNum.setPreferredSize(new Dimension (96, 48));
        oncBatchNum.setToolTipText("Which ODB input batch contained this family");
        oncBatchNum.setBorder(BorderFactory.createTitledBorder("Batch #"));
        oncBatchNum.setEnabled(false);
        oncBatchNum.addActionListener(this);
        
        oncDNScode = new JTextField(6);
        oncDNScode.setToolTipText("Do not serve family code: e.g, SA- Salvation Army");
        oncDNScode.setBorder(BorderFactory.createTitledBorder("DNS Code"));
        oncDNScode.setEditable(false);
        oncDNScode.addActionListener(this);

        HOHFirstName = new JTextField(9);
        HOHFirstName.setBorder(BorderFactory.createTitledBorder("First Name"));
        HOHFirstName.setEditable(false);
        HOHFirstName.addActionListener(fdcListener);
        
        HOHLastName = new JTextField(11);
        HOHLastName.setBorder(BorderFactory.createTitledBorder("Last Name"));
        HOHLastName.setEditable(false);
        HOHLastName.addActionListener(fdcListener);
  
        String[] fstat = {"Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
        statusCB = new JComboBox(fstat);
        statusCB.setPreferredSize(new Dimension(152, 52));
        statusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
        statusCB.setEnabled(false);
        statusCB.addActionListener(this);
        
        lblNumBags = new JLabel("0", JLabel.RIGHT);
        lblNumBags.setPreferredSize(new Dimension(48, 44));
        lblNumBags.setBorder(BorderFactory.createTitledBorder("Bags"));
             
        HomePhone = new JTextPane();
        JScrollPane homePhoneScrollPane = new JScrollPane(HomePhone);
        homePhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        homePhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Home Phone"));
        HomePhone.setEditable(false);
        
        OtherPhone = new JTextPane();
        JScrollPane otherPhoneScrollPane = new JScrollPane(OtherPhone);
        otherPhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        otherPhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Other Phone"));
        OtherPhone.setEditable(false);
      
        EMail = new JTextField(18);
        EMail.setBorder(BorderFactory.createTitledBorder("Email Address"));
        EMail.setEditable(false);
        EMail.addActionListener(fdcListener);
        
        String[] languages = {"?", "English", "Spanish", "Arabic", "Korean", "Vietnamese", "Other"};
        Language = new JComboBox(languages);
        Language.setToolTipText("Select the primary language spoken by the family");
        Language.setPreferredSize(new Dimension(140, 48));
        Language.setBorder(BorderFactory.createTitledBorder("Language"));
        Language.setEnabled(false);
        
        delStatus = new ComboItem[8];	//Delivery status combo box list objects can be enabled/disabled
        delStatus[0] = new ComboItem("Empty");
        delStatus[1] = new ComboItem("Contacted");  
        delStatus[2] = new ComboItem("Confirmed");
        delStatus[3] = new ComboItem("Assigned", false);   
        delStatus[4] = new ComboItem("Attempted");
        delStatus[5] = new ComboItem("Returned");
        delStatus[6] = new ComboItem("Delivered");
        delStatus[7] = new ComboItem("Counselor Pick-Up");
        
        delstatCB = new JComboBox(delStatus);
        delstatCB.setRenderer(new ComboRenderer());
        delstatCB.setBorder(BorderFactory.createTitledBorder("Delivery Status"));
        delstatCB.setPreferredSize(new Dimension(132, 52));
        delstatCB.setEnabled(false);
        delstatCB.addActionListener(new ComboListener(delstatCB));	//Manages "Assigned" list item
        delstatCB.addActionListener(this);	//Manages actual change events
        
        delRB = new JRadioButton(gvs.getImageIcon(14));
        delRB.setToolTipText("Click to see delivery history");
        delRB.setEnabled(false);
        delRB.addActionListener(this);
        
        housenumTF = new JTextField();
        housenumTF.setPreferredSize(new Dimension(72, 44));
        housenumTF.setBorder(BorderFactory.createTitledBorder("House #"));
        housenumTF.setEditable(false);
        housenumTF.addActionListener(this);
        
        Street = new JTextField();
        Street.setPreferredSize(new Dimension(192, 44));
        Street.setBorder(BorderFactory.createTitledBorder("Street"));
        Street.setEditable(false);
        Street.addActionListener(this);
        
        Unit = new JTextField();
        Unit.setPreferredSize(new Dimension(80, 44));
        Unit.setBorder(BorderFactory.createTitledBorder("Unit"));
        Unit.setEditable(false);
        Unit.addActionListener(this);
        
        City = new JTextField();
        City.setPreferredSize(new Dimension(128, 44));
        City.setBorder(BorderFactory.createTitledBorder("City"));
        City.setEditable(false);
        City.addActionListener(this);
        
        ZipCode = new JTextField();
        ZipCode.setPreferredSize(new Dimension(88, 44));
        ZipCode.setBorder(BorderFactory.createTitledBorder("Zip Code"));
        ZipCode.setEditable(false);
        ZipCode.addActionListener(this);
        
        lblRegion = new JLabel("?", JLabel.CENTER);
        lblRegion.setPreferredSize(new Dimension(60, 44));
        lblRegion.setBorder(BorderFactory.createTitledBorder("Region"));
        
        altAddressRB = new JRadioButton(gvs.getImageIcon(19));
        altAddressRB.setToolTipText("Click to see alternate address");
        altAddressRB.setEnabled(false);
        altAddressRB.addActionListener(this);
       
//      Caller = new JTextField();
//      Caller.setPreferredSize(new Dimension(120, 44));
//      Caller.setToolTipText("Shows ONC volunteer who last changed family data");
//      Caller.setBorder(BorderFactory.createTitledBorder("Changed By"));
//      Caller.setEditable(false);
        
        lblChangedBy = new JLabel();
        lblChangedBy.setPreferredSize(new Dimension(128, 44));
        lblChangedBy.setToolTipText("Shows ONC volunteer who last changed family data");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
        
        btnAssignONCNum = new JButton("Assign ONC #");
        btnAssignONCNum.setToolTipText("Click to have the system assign an ONC Number to family");
        btnAssignONCNum.setVisible(false);	//Invisible until ONC Number invalid and region valid
        btnAssignONCNum.addActionListener(this);
        
        btnShowPriorHistory = new JButton("Prior History");
        btnShowPriorHistory.setToolTipText("Click to see prior ONC wish history for highlighted child");
        btnShowPriorHistory.setEnabled(false);
        btnShowPriorHistory.addActionListener(this);
        
        btnShowAllPhones = new JButton("All Phone #'s");
        btnShowAllPhones.setToolTipText("Click to see all phone numbers for family");
        btnShowAllPhones.setEnabled(false);
        btnShowAllPhones.addActionListener(this);
        
        btnShowAgentInfo = new JButton("Agent Info");
        btnShowAgentInfo.setToolTipText("Click to see info on agent who input family to ODB or WFCM");
        btnShowAgentInfo.setEnabled(false);
        btnShowAgentInfo.addActionListener(this);
        
        btnShowODBDetails = new JButton("ODB Details");
        btnShowODBDetails.setToolTipText("Click to see ODB details for this family");
        btnShowODBDetails.setEnabled(false);
        btnShowODBDetails.addActionListener(this);
        
        btnShowDeliveryStatus = new JButton("Delivery Status");
        btnShowDeliveryStatus.setToolTipText("Click to see status of ONC's delivery to this family");
        btnShowDeliveryStatus.setEnabled(false);
        btnShowDeliveryStatus.addActionListener(this);
        
        //Set up the Child Table
        childTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
			{
			  Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			  		 
			  if(isRowSelected(Index_row))
				  comp.setBackground(comp.getBackground());
			  else if (Index_row % 2 == 1)			  
				  comp.setBackground(new Color(240,248,255));
			  else
				  comp.setBackground(Color.white);
			  
			  return comp;
			}
		};
		childTable.setToolTipText("Click or use up/down keys to select a child");
       
        childTableModel = new DefaultTableModel(new Object[]{"First Name","Last Name", "DOB", "Gend"},0)
        {
           private static final long serialVersionUID = 1L;
           @Override
           	//every cell cannot be edited
           	public boolean isCellEditable(int row, int column) {return false;}
        };      
        childTable.setModel(childTableModel);
       
        childTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             
        childTable.getColumnModel().getColumn(0).setPreferredWidth(72);
        childTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        childTable.getColumnModel().getColumn(2).setPreferredWidth(48);
        childTable.getColumnModel().getColumn(3).setPreferredWidth(24);
        
        childTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        childTable.setFillsViewportHeight(true);
       
        childTable.getSelectionModel().addListSelectionListener(this);    	
       
        JTableHeader anHeader =childTable.getTableHeader();
        anHeader.setForeground( Color.black);
//      anHeader.setBackground( new Color(161,202,241));
        anHeader.setBackground( Color.LIGHT_GRAY); 
 
        //Create the Child Table scroll pane and add the Child Table to it.
        JScrollPane childscrollPane = new JScrollPane(childTable);
        
        //Set up the ODB Wish List Pane
        odbWishListPane = new JTextPane();
        odbWishListPane.setToolTipText("Wish suggestions by child from ODB or WFCM");
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        odbWishListPane.setParagraphAttributes(attribs, true);
  	   	odbWishListPane.setEditable(false);
  	   	
	    //Create the ODB Wish List scroll pane and add the Wish List text pane to it.
        odbWishscrollPane = new JScrollPane(odbWishListPane);
        odbWishscrollPane.setBorder(BorderFactory.createTitledBorder("ODB Wish List"));
        
        //Set up the ONC Notes Pane
        oncNotesPane = new JTextPane();
        oncNotesPane.setToolTipText("Family specific notes entered by ONC user");
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        oncNotesPane.setParagraphAttributes(attribs,true);             
	   	oncNotesPane.setEditable(false);
	   	
	    //Create the ONC Notes scroll pane and add the ONC Note text pane to it.
        JScrollPane oncNotesscrollPane = new JScrollPane(oncNotesPane);
        oncNotesscrollPane.setBorder(BorderFactory.createTitledBorder("ONC Notes"));
	   	
	    //Set up the ONC Delivery Instructions Pane
        oncDIPane = new JTextPane();
        oncDIPane.setToolTipText("Family specific delivery instructions entered by ONC user");
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        oncDIPane.setParagraphAttributes(attribs,true);             
	   	oncDIPane.setEditable(false);
	   	
	    //Create the ONC Delivery Instructions scroll pane and add the ONC Note text pane to it.
        JScrollPane oncDIscrollPane = new JScrollPane(oncDIPane);
        oncDIscrollPane.setBorder(BorderFactory.createTitledBorder("Delivery Instructions"));
        
        //Set up delivery info dialog box 
        dsDlg = new DeliveryStatusDialog(parentFrame);
//      dsDlg.dsTableModel.addTableModelListener(this);
       
        //Set up delivery directions dialog box 
        try { dirDlg = new DirectionsDialog(parentFrame); }
		catch (JSONException e1) {// TODO Auto-generated catch block 
			e1.printStackTrace();}
        
        //Set up client map dialog
        cmDlg = new ClientMapDialog(parentFrame); 
        
        //Set up the sort wishes dialog
        String[] colToolTips = {"ONC Family Number", "Child's Age", 
				"Child's Gender", "Wish Number - 1, 2 or 3", "Wish Assigned", "Wish Detail",
				"# - Selected by ONC or * - Don't asssign", "Wish Status", "Who is fulfilling?",
				"User who last changed wish", "Date & Time Wish Last Changed"};
        String[] columns = {"ONC", "Age", "Gend", "Wish", "Wish Type", "Details", " Res ",
				"Status", "Assignee", "Changed By", "Time Stamp"};
        int[] colWidths = {40, 48, 36, 36, 84, 160, 32, 80, 136, 80, 92};
        int[] center_cols = {3,6};
        sortWishesDlg = new SortWishDialog(parentFrame, colToolTips, columns, colWidths, center_cols);
        sortWishesDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
    	//Set up the receive gifts dialog
//    	recGiftsDlg = new GiftActionDialog(parentFrame, WishStatus.Received);
//   	recGiftsDlg.addEntitySelectionListener(familyChildSelectionListener);

    	//Set up the manage catalog dialog
    	catDlg = new WishCatalogDialog(parentFrame);
    	
    	 //Set up the sort family dialog
    	String[] fdColToolTips = {"ONC Family Number", "Batch Number", "Do Not Serve Code", 
			  "Family Status", "Delivery Status", "Head of Household First Name", 
			  "Head of Household Last Name", "House Number","Street",
			  "Unit or Apartment Number", "Zip Code", "Region",
			  "Changed By", "Stoplight Color"};
    	String[] fdCols = {"ONC", "Batch #", "DNS", "Fam Status", "Del Status", "First", "Last",
    						"House", "Street", "Unit", "Zip", "Reg", "Changed By", "SL"};
    	int[] fdColWidths = {32, 48, 48, 72, 72, 72, 72, 48, 128, 72, 48, 32, 72, 24};
    	int [] fdCenter_cols = {1, 11, 13};
        sortFamiliesDlg = new SortFamilyDialog(parentFrame, fdColToolTips, fdCols, fdColWidths, fdCenter_cols);
        sortFamiliesDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
    	//Set up the dialog to edit agent info
    	String[] tfNames = {"Name", "Organization", "Title", "Email", "Phone"};
    	agentInfoDlg = new AgentInfoDialog(parentFrame, tfNames);
    	
    	//Set up the dialog to change family ONC Number
    	String[] oncNum = {"Change ONC #"};
    	changeONCNumberDlg = new ChangeONCNumberDialog(parentFrame, oncNum);

    	//Set up the sort agent dialog
    	String[] agtColToolTips = {"Name", "Organization", "Title", "EMail Address", "Phone"};
      	String[] agtCols = {"Name", "Org", "Title", "EMail", "Phone"};
      	int[] agtColWidths = {144, 160, 120, 168, 120};
  //  	int [] agtCenter_cols = {1, 11, 13};
    	sortAgentDlg = new SortAgentDialog(parentFrame, agtColToolTips, agtCols, agtColWidths, null);
    	sortAgentDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
    	//set up the assign delivery dialog
    	String[] addToolTips = {"ONC Family Number", "Family Status", "Delivery Status",
    								"# of bags packaged", "# of bikes assigned to family",
    								"# of large items assigned to family",
    								"House Number","Street", "Zip Code", "Region","Changed By",
    								"Stoplight Color", "Driver"};
    	String[] addCols = {"ONC", "Fam Status", "Del Status", "# Bags", "# Bikes", "# Lg It.", "House",
    						"Street", "Zip", "Reg", "Changed By", "SL", "Deliverer"};
    	int[] addColWidths = {32, 72, 72, 48, 48, 48, 48, 128, 48, 32, 72, 24, 120};
    	int[] addCenter_cols = {3, 4, 5, 9};
    	assignDeliveryDlg = new AssignDeliveryDialog(parentFrame, addToolTips, addCols, addColWidths, addCenter_cols);
    	assignDeliveryDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
    	//set up the sort driver dialog
    	String[] sdToolTips = {"Driver Number", "First Name", "Last Name", "# of Deliveries",
    								"Cell Phone #", "Home Phone #",
    								"E-Mail address", "Changed By", "Stoplight Color"};
    	String[] sdCols = {"Drv #", "First Name", "Last Name", "# Del", "Cell #", "Home #", "E-Mail Address",
    						"Changed By", "SL"};
    	int[] sdColWidths = {28, 80, 80, 28, 88, 88, 160, 88, 28};
    	int[] sdCenter_cols = {3, 8};
    	sortDriverDlg = new SortDriverDialog(parentFrame, sdToolTips, sdCols, sdColWidths, sdCenter_cols);
    	sortDriverDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
    	//Set up the edit driver (deliverer) dialog and register it to listen for Family 
    	//Selection events from particular ui's that have driver's associated
        driverDlg = new DriverDialog(parentFrame);
        nav.addEntitySelectionListener(driverDlg);	//family panel/main screen nav
        assignDeliveryDlg.addEntitySelectionListener(driverDlg);
        sortDriverDlg.addEntitySelectionListener(driverDlg);
        sortFamiliesDlg.addEntitySelectionListener(driverDlg);
        sortWishesDlg.addEntitySelectionListener(driverDlg);
        
        //Set up the view family database dialog
        dbDlg = new ViewONCDatabaseDialog(parentFrame);
        
        //Set up the edit gift partner dialog
        orgDlg = new OrganizationDialog(parentFrame);
        sortWishesDlg.addEntitySelectionListener(orgDlg);
        
        //Set up the sort gift partner dialog
        String[] orgToolTips = {"ONC Partner", "Partner Status","Type of Organization",
				"Number of Ornaments Requested","Number of Ornaments Assigned",
				"Special Notes for Partner","Date Partner Info Last Changed", 
				"ONC User that last changed partner info", "ONC Region that partner is located",
				"Partner Stop Light Color"};
        String[] orgCols = {"Partner","Status", "Type", "Req", "Assigned", "Special Notes",
				"Date Changed","Changed By","Reg", "SL"};
        int[] orgColWidths = {180, 96, 68, 48, 56, 180, 72, 80, 28, 24};
        int[] orgCenter_cols = {8, 9};
        sortOrgsDlg = new SortPartnerDialog(parentFrame, orgToolTips, orgCols, orgColWidths, orgCenter_cols);
        sortOrgsDlg.addEntitySelectionListener(orgDlg);
        
        //set up the data check dialog and table row selection listener
        dcDlg = new ChildCheckDialog(pf);
        dcDlg.addEntitySelectionListener(familyChildSelectionListener);
        
        //set up the family check dialog and table row selection listener
        dfDlg = new FamilyCheckDialog(pf);
        dfDlg.addEntitySelectionListener(familyChildSelectionListener);
        
        //Create the Child Panel
        oncChildPanel = new ChildPanel();
      
        //Add components to the panels
        p1.add(lblONCNum);
        p1.add(odbFamilyNum);
        p1.add(oncBatchNum);
        p1.add(oncDNScode);
//      p1.add(clientFamily);
        p1.add(HOHFirstName);
        p1.add(HOHLastName);
        p1.add(statusCB);
        p1.add(lblNumBags);
        
        p2.add(homePhoneScrollPane);
        p2.add(otherPhoneScrollPane);
        p2.add(EMail);
		p2.add(Language);
		p2.add(delstatCB);
		p2.add(delRB);
		
        p3.add(housenumTF);
        p3.add(Street);
        p3.add(Unit);
        p3.add(City);
        p3.add(ZipCode);
        p3.add(lblRegion);
        p3.add(altAddressRB);
        p3.add(lblChangedBy);
        
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        p4.add(childscrollPane, c);
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        p4.add(odbWishscrollPane, c);
        c.gridx=4;
        c.gridy=0;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
        p4.add(oncNotesscrollPane, c);
        c.gridx=4;
        c.gridy=1;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
        p4.add(oncDIscrollPane, c);
        
        p5.add(btnAssignONCNum);
        p5.add(btnShowPriorHistory);
        p5.add(btnShowAllPhones);
        p5.add(btnShowODBDetails);
        p5.add(btnShowAgentInfo);
//      p5.add(btnShowDeliveryStatus);
        
        this.add(nav);
        this.add(p1);
        this.add(p2);
        this.add(p3);
        this.add(p4);
        this.add(p5);
        this.add(oncChildPanel);        
	}

	void setEditableGUIFields(boolean tf)
	{
		if(gvs.isUserAdmin())
		{
			oncDNScode.setEditable(tf);
			oncBatchNum.setEnabled(tf);
			HOHFirstName.setEditable(tf);
			HOHLastName.setEditable(tf);;
			statusCB.setEnabled(tf);
			HomePhone.setEditable(tf);
			OtherPhone.setEditable(tf);
			EMail.setEditable(tf);
			Language.setEnabled(tf);
			delstatCB.setEnabled(tf);
//			Caller.setEditable(tf);
			housenumTF.setEditable(tf);
			Street.setEditable(tf);
			Unit.setEditable(tf);
			City.setEditable(tf);
			ZipCode.setEditable(tf);
			odbWishListPane.setEditable(tf);
			oncNotesPane.setEditable(tf);
			oncDIPane.setEditable(tf);
		}
		
		oncChildPanel.setEditableGUIFields(tf);
		if(statusCB.getSelectedIndex() > FAMILY_STATUS_UNVERIFIED)
			oncChildPanel.setEnabledWishPanels(tf);
	}
	
	void closeAllDialogs()
	{
		cmDlg.setVisible(false);
		dsDlg.setVisible(false);
		dirDlg.setVisible(false);
		sortWishesDlg.setVisible(false);
		sortFamiliesDlg.setVisible(false);
		sortAgentDlg.setVisible(false);
		assignDeliveryDlg.setVisible(false);
		dbDlg.setVisible(false);
		orgDlg.setVisible(false);
		catDlg.setVisible(false);
		agentInfoDlg.setVisible(false);
		changeONCNumberDlg.setVisible(false);
	}
	
	void setTextPaneFontSize()
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        odbWishListPane.setParagraphAttributes(attribs, true);
        oncNotesPane.setParagraphAttributes(attribs, true);
        oncDIPane.setParagraphAttributes(attribs, true);
        orgDlg.setTextPaneFontSize();
	}
	
	void setRestrictedEnabledButtons(boolean tf)
	{	 
		btnShowAllPhones.setEnabled(tf);
		altAddressRB.setEnabled(tf);
//		btnShowODBDetails.setEnabled(tf);
	}
	
	void setEnabledButtons(boolean tf) 
	{ 
		btnShowAgentInfo.setEnabled(tf);
		delRB.setEnabled(tf); 
	}
	
	ONCChild getDisplayedChild()
	{ 
		if(ctAL != null && ctAL.size() > 0 && cn > 0 && cn < ctAL.size())
			return ctAL.get(cn);
		else
			return null;
	}
	
	void clear()
	{
		bFamilyDataChanging = true;
		
		lblONCNum.setText("ONC#");
		odbFamilyNum.setText("No Families");
		oncBatchNum.setSelectedIndex(0);
		oncDNScode.setText("");
//		clientFamily.setText("");
		HOHFirstName.setText("");
		HOHLastName.setText("");
		statusCB.setSelectedIndex(0);
		HomePhone.setText("");
		OtherPhone.setText("");
		EMail.setText("");
		Language.setSelectedIndex(0);
//		Caller.setText("");
		lblChangedBy.setText("");
		housenumTF.setText("");
		Street.setText("");
		Unit.setText("");
		City.setText("");
		ZipCode.setText("");
		lblRegion.setText("?");
		
		bChildTableDataChanging = true;		//Disable table list from updating when there's not child data
		ClearChildTable();
		bChildTableDataChanging = false;
		
		odbWishListPane.setText("");			
		odbWishListPane.getHighlighter().removeAllHighlights();	
		oncNotesPane.setText("");
		oncDIPane.setText("");
		
		setEditableGUIFields(false);
		bFamilyDataChanging = false;
		
		btnShowODBDetails.setEnabled(false);
		btnShowPriorHistory.setEnabled(false);
		
		oncChildPanel.clearChildData();
		dsDlg.ClearDeliveryData();
		
		setEnabledButtons(false);
		setRestrictedEnabledButtons(false);
	}

	void display(ONCFamily fam, ONCChild child)
	{
		if(fam == null)
		{
			//error has occurred, display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(),
					"ERROR - NULL Family, can't display, contact the ONC IT Director",
					"ERROR - NULL Family",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			return;
		}
		else if(child == null)
		{
			currFam = fam;
			ctAL = cDB.getChildren(currFam.getID());
			cn = 0;
		}
		else
		{
			currFam = fam;
			ctAL = cDB.getChildren(currFam.getID());
			int index = 0;
			while(index < ctAL.size() && ctAL.get(index) != child)
				index++;
			
			if(index < ctAL.size())
				cn = index;
			
		}		
//		System.out.println(String.format("FamilyPanel displayFamily - Family ID: %d, cn: %d", fam.getID(), cn));		
		p1.setBackground(pBkColor);
		p2.setBackground(pBkColor);
		p3.setBackground(pBkColor);
		
		if(bDispAll)
		{
			lblONCNum.setText(currFam.getONCNum());
			odbFamilyNum.setText(currFam.getODBFamilyNum());
			oncBatchNum.setSelectedItem((String)currFam.getBatchNum());
			oncDNScode.setText(currFam.getDNSCode());
			oncDNScode.setCaretPosition(0);
			
			checkForDNSorGiftCardOnly();
						
			HOHFirstName.setText(currFam.getHOHFirstName());
			HOHLastName.setText(currFam.getHOHLastName());
			
			HomePhone.setText(currFam.getHomePhone());
			HomePhone.setCaretPosition(0);
			OtherPhone.setText(currFam.getOtherPhon());
			OtherPhone.setCaretPosition(0);
			EMail.setText(currFam.getFamilyEmail());
			EMail.setCaretPosition(0);
			Language.setSelectedItem((String)currFam.getLanguage());
//			Caller.setText(f.getCaller());
			lblChangedBy.setText(currFam.getChangedBy());
			housenumTF.setText(currFam.getHouseNum());
			Street.setText(currFam.getStreet());
			Street.setCaretPosition(0);
			Unit.setText(currFam.getUnitNum());
			Unit.setCaretPosition(0);
			City.setText(currFam.getCity());
			ZipCode.setText(currFam.getZipCode());
			if(currFam.getSubstituteDeliveryAddress() != null &&
				!currFam.getSubstituteDeliveryAddress().isEmpty())
				altAddressRB.setIcon(gvs.getImageIcon(20));
			else
				altAddressRB.setIcon(gvs.getImageIcon(19));
			
			statusCB.setSelectedIndex(currFam.getFamilyStatus());
			lblNumBags.setText(Integer.toString(currFam.getNumOfBags()));
			if(currFam.getDeliveryStatus() == DELIVERY_STATUS_ASSIGNED)
				setEnabledAssignedDeliveryStatus(true);
			else
				setEnabledAssignedDeliveryStatus(false);
			delstatCB.setSelectedIndex(currFam.getDeliveryStatus());
			lblRegion.setText(regions.getRegionID(currFam.getRegion()));
			
			odbWishListPane.setText(currFam.getODBWishList());
//			odbWishListPane.setCaretPosition(0);
			if(ctAL.size() > 0)	//Check to see if the family has children
			{			
				refreshODBWishListHighlights(currFam, ctAL.get(cn));
				refreshPriorHistoryButton(currFam, ctAL.get(cn));
				
			}
			else
			{
				refreshODBWishListHighlights(currFam, null);
				btnShowPriorHistory.setEnabled(false);
			}
			
			//Test to see if an ONC number could be assigned. If so, make the auto assign button
			//visible
			if(!lblONCNum.getText().equals("DEL") && !lblRegion.getText().equals("?") &&
					  !Character.isDigit(lblONCNum.getText().charAt(0)))	
				btnAssignONCNum.setVisible(true);
			else
				btnAssignONCNum.setVisible(false);
			
			oncNotesPane.setText(currFam.getNotes());
			oncNotesPane.setCaretPosition(0);
			oncDIPane.setText(currFam.getDeliveryInstructions());
			oncDIPane.setCaretPosition(0);
			
			if(currFam.getODBDetails().trim().isEmpty())	//Typically, an empty field from ODB contains 1 blank space
				btnShowODBDetails.setEnabled(false);
			else
				btnShowODBDetails.setEnabled(true);
		}
		else	//restricted viewing user
		{
			lblONCNum.setText(currFam.getONCNum());
			odbFamilyNum.setText(currFam.getODBFamilyNum());
			oncBatchNum.setSelectedItem((String)currFam.getBatchNum());
			oncDNScode.setText(currFam.getDNSCode());
			oncDNScode.setCaretPosition(0);
			
			checkForDNSorGiftCardOnly();
			
			statusCB.setSelectedIndex(currFam.getFamilyStatus());
			lblNumBags.setText(Integer.toString(currFam.getNumOfBags()));
			
			if(currFam.getDeliveryStatus() == DELIVERY_STATUS_ASSIGNED)
				setEnabledAssignedDeliveryStatus(true);
			else
				setEnabledAssignedDeliveryStatus(false);
			delstatCB.setSelectedIndex(currFam.getDeliveryStatus());
			Language.setSelectedItem((String)currFam.getLanguage());
//			Caller.setText(f.getCaller());
			lblChangedBy.setText(currFam.getChangedBy());
			lblRegion.setText(regions.getRegionID(currFam.getRegion()));
			
			//Find all names in the ODB wishlist and replace them with "Child x" before displaying
			String[] replace = new String[cDB.getNumberOfChildrenInFamily(currFam.getID()) * 2 + 1];
			replace[0] = currFam.getODBWishList();
			
			int cnum = 0, sn = 0;
			while(cnum < cDB.getNumberOfChildrenInFamily(currFam.getID()))
			{
				replace[sn+1] = replace[sn].replaceAll(ctAL.get(cnum).getChildFirstName(),
//					"Child " + Integer.toString(ctAL.get(cn).getChildNumber()));
						"Child " + Integer.toString(cnum+1));
				
				replace[sn+2] = replace[sn+1].replaceFirst(ctAL.get(cnum).getChildLastName(), "");
				
				cnum++;	
				sn += 2;			
			}
			
			String almostDone = replace[replace.length-1];
			String done = almostDone.replaceAll(" :", ":");
			
			if(cDB.getNumberOfChildrenInFamily(currFam.getID()) > 0 )	//If no children, don't display a wish list
//				odbWishListPane.setText(replace[replace.length-1]);
				odbWishListPane.setText(done);	
			else
				odbWishListPane.setText("");
			
//				odbWishListPane.setCaretPosition(0);
				
			if(ctAL.size() > 0)	//Check to see if the family has children
			{
				refreshODBWishListHighlights(currFam, ctAL.get(cn));
				refreshPriorHistoryButton(currFam, ctAL.get(cn));
			}
			else
			{
				refreshODBWishListHighlights(currFam, null);
				btnShowPriorHistory.setEnabled(false);
			}
			oncNotesPane.setText(currFam.getNotes());
			oncNotesPane.setCaretPosition(0);
			oncDIPane.setText(currFam.getDeliveryInstructions());
			oncDIPane.setCaretPosition(0);
			btnShowODBDetails.setEnabled(false);
		}
		
		//Disable table list from updating when there's not child data
		bChildTableDataChanging = true;		
		ClearChildTable();
		if(ctAL.size() > 0)
		{		
			addChildrentoTable(ctAL, bDispAll);
			displayChild((currChild = ctAL.get(cn)), cn);
			if(bDispAll)
				ONCMenuBar.setEnabledDeleteChildMenuItem(true);	//Enable Delete Child Menu Bar item
		}
		else
		{
			//family has no children, clear the child panel
			currChild = null;
			oncChildPanel.clearChildData();
			ONCMenuBar.setEnabledDeleteChildMenuItem(false);	//Disable Delete Child Menu Bar item
			
		}
		bChildTableDataChanging = false;
		
		nav.setStoplightEntity(currFam);
		nav.btnNextSetEnabled(true);
		nav.btnPreviousSetEnabled(true);
			
		if(currFam.getFamilyStatus() >= FAMILY_STATUS_INFO_VERIFIED)
			oncChildPanel.setEnabledWishPanels(true);
		else
			oncChildPanel.setEnabledWishPanels(false);
		
		if(currFam.getNotes().toLowerCase().contains(GIFT_CARD_ONLY_TEXT))
			oncChildPanel.setEnabledWishCBs(false);
		
		if(dsDlg.isShowing())
			dsDlg.displayDeliveryInfo(currFam);
		
//		if(driverDlg.isShowing())	//If found, display the driver assigned for the family displayed
//			driverDlg.searchForDriver(deliveryDB.getDeliveredBy(f.getDeliveryID()));
		
		if(dirDlg.isShowing())
			updateDrivingDirections();
		
		if(changeONCNumberDlg.isShowing())
		{
			//Check to see if client user changed families. Then set the dialog to display
			//the ONC Number for the new family being displayed						
			changeONCNumberDlg.display(currFam);	
		}															
		
		if(agentInfoDlg.isShowing())
		{
			//Check to see if client user changed Agent info. Then set the dialog to display
			//the agent for the new family being displayed	
			agentInfoDlg.update();					
			agentInfoDlg.display(agentDB.getAgent(currFam.getAgentID()));	
		}															
	}
	
	void checkForDNSorGiftCardOnly()
	{
		if(currFam.getDNSCode().length() > 1)
		{
			p1.setBackground(Color.RED);
			p2.setBackground(Color.RED);
			p3.setBackground(Color.RED);
		}
		else if(currFam.getNotes().toLowerCase().contains(GIFT_CARD_ONLY_TEXT))
		{
			p1.setBackground(Color.GREEN);
			p2.setBackground(Color.GREEN);
			p3.setBackground(Color.GREEN);
		}
	}
	
	void displayNewONCnum(String oncNum) { lblONCNum.setText(oncNum); }	//Non-user change to ONC Number TF
	
	void refreshPriorHistoryButton(ONCFamily fam, ONCChild c)
	{
		if(c != null && c.getPriorYearChildID() != -1)
			btnShowPriorHistory.setEnabled(true);
		else
			btnShowPriorHistory.setEnabled(false);
	}
	
	void refreshODBWishListHighlights(ONCFamily fam, ONCChild c)
	{		
		odbWishListPane.getHighlighter().removeAllHighlights();		
		String odbWishList = odbWishListPane.getText();
		
		if(c == null)	
			return;	//No children to highlight
		
		else if(bDispAll)	//Show all data
		{	
			String childfn = c.getChildFirstName();
			String childln = c.getChildLastName();
			String childname = childfn + " " + childln;
			
			int startPos;
			
			if(odbWishList.indexOf(childname) == -1)	//Found 1st instance of child first name
				startPos = odbWishList.indexOf(childfn);
			else
				startPos = odbWishList.indexOf(childname);
				
			if(startPos > -1)	//Found 1st instance of child name
			{
				int endPos = odbWishList.indexOf(childln, startPos);
				if(endPos != -1)
					endPos += childln.length();
				else
					endPos = startPos + childfn.length();
				
				highlightAndCenterODBWish(startPos, endPos);
			}
		}
		else //Show restricted data
		{
			String childfn = "Child " + Integer.toString(cn+1);
			
			int startPos = odbWishList.indexOf(childfn);
			if(startPos > -1)	//ensure the child full name is found
			{
				int endPos = startPos + childfn.length();
				highlightAndCenterODBWish(startPos, endPos);
			}
		}
	}
	
	void highlightAndCenterODBWish(int startPos, int endPos)
	{
		try
		{
			odbWishListPane.setCaretPosition(startPos);
			odbWishListPane.getHighlighter().addHighlight(startPos, endPos+1, highlightPainter);
			int caretPosition = odbWishListPane.getCaretPosition();
			Rectangle caretRectangle = odbWishListPane.modelToView(caretPosition);
			Rectangle viewRectangle = new Rectangle(0, caretRectangle.y -
			        (odbWishscrollPane.getHeight() - caretRectangle.height) / 2,
			        odbWishscrollPane.getWidth(), odbWishscrollPane.getHeight());
			odbWishListPane.scrollRectToVisible(viewRectangle);
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}						
	}
	
	/***************************************************************************************************
	*Checks for and records family data changes, then child data changes in preparation for displaying
	*a new family. This method is called when the family being displayed is changing by the the
	*navigation button or the search field on the status panel. The displayed child is the first child
	*in the array list and the previous child is saved first.
	****************************************************************************************************/
	void update()
	{		
		checkAndUpdateFamilyData(currFam);
		
		if(cDB.getNumberOfChildrenInFamily(currFam.getID()) > 0)
			updateChild();
		
		cn=0;	//Family updates occur when new family is selected. After updating child displayed, reset to first child
	}
	
	/***************************************************************************************************
	*Query family data input fields to get data and record changes. This method is called when
	*family data may have changed by either an external object or by an internal component event 
	*such as the enter key or combo box list selection change
	****************************************************************************************************/
	void checkAndUpdateFamilyData(ONCFamily family)
	{
		//make a copy of the current family object to create the request
		ONCFamily fam = new ONCFamily(family);
		
		int cf = 0;	//used to indicate if a change is detected to a GUI field
		
		//If data has been changed store updated family info and return true to indicate a change
//		if(!oncnum.getText().equals(fam.getONCNum())) { fam.setONCNum(oncnum.getText()); cf = 1; }
		if(!oncBatchNum.getSelectedItem().equals(fam.getBatchNum()))
		{
			fam.setBatchNum((String) oncBatchNum.getSelectedItem());
			cf = 2;	
		}
		if(!oncDNScode.getText().equals(fam.getDNSCode())) //DNS code changed
		{
			//Save the new DNS text field and mark that a field has changed
			fam.setDNSCode(oncDNScode.getText());
			cf = 3;
			
			//Update the client map served region distribution
//			int[] changes = {-1, -1};
//			changes[(oncDNScode.getText().isEmpty()) ? 1 : 0] = fam.getRegion();						
//			cmDlg.updateRegionCounts(changes);
		}
//		if(!clientFamily.getText().equals(fam.getClientFamily())) {fam.setClientFamily(clientFamily.getText()); cf = true;}
		if(!HOHFirstName.getText().equals(fam.getHOHFirstName())) {fam.setHOHFirstName(HOHFirstName.getText()); cf = 4;}
		if(!HOHLastName.getText().equals(fam.getHOHLastName())) {fam.setHOHLastName(HOHLastName.getText()); cf = 5;}
		if(Integer.parseInt(lblNumBags.getText()) != fam.getNumOfBags()) {fam.setNumOfBags(Integer.parseInt(lblNumBags.getText())); cf = 20;}
		if(!HomePhone.getText().equals(fam.getHomePhone())) {fam.setHomePhone(HomePhone.getText()); cf = 6;}
		if(!OtherPhone.getText().equals(fam.getOtherPhon())) {fam.setOtherPhon(OtherPhone.getText()); cf = 7;}
		if(!EMail.getText().equals(fam.getFamilyEmail())) {fam.setFamilyEmail(EMail.getText()); cf = 8;}
		if(!Language.getSelectedItem().toString().equals(fam.getLanguage())){fam.setLanguage(Language.getSelectedItem().toString());cf = 9;}
//		if(!Caller.getText().equals(fam.getCaller())) {fam.setCaller(Caller.getText()); cf = true;}
		if(!housenumTF.getText().equals(fam.getHouseNum())) {fam.setHouseNum(housenumTF.getText()); cf = 10;}
		if(!Street.getText().equals(fam.getStreet())) {fam.setStreet(Street.getText()); cf = 11;}
		if(!Unit.getText().equals(fam.getUnitNum())) {fam.setUnitNum(Unit.getText()); cf = 12;}
		if(!City.getText().equals(fam.getCity())) {fam.setCity(City.getText()); cf = 13;}
		if(!ZipCode.getText().equals(fam.getZipCode())) {fam.setZipCode(ZipCode.getText()); cf = 14;}
		if(statusCB.getSelectedIndex() != fam.getFamilyStatus()) {fam.setFamilyStatus(statusCB.getSelectedIndex()); cf = 15;}
		if(delstatCB.getSelectedIndex() != fam.getDeliveryStatus())
		{
			ONCDelivery reqDelivery = new ONCDelivery(-1, fam.getID(), delstatCB.getSelectedIndex(),
					deliveryDB.getDeliveredBy(fam.getDeliveryID()),
					"Delivery Status Updated",
					gvs.getUserLNFI(),
					Calendar.getInstance());

			String response = deliveryDB.add(this, reqDelivery);
			if(response.startsWith("ADDED_DELIVERY"))
			{
				Gson gson = new Gson();
				ONCDelivery addedDelivery = gson.fromJson(response.substring(14), ONCDelivery.class);
				fam.setDeliveryID(addedDelivery.getID());
				fam.setDeliveryStatus(delstatCB.getSelectedIndex());
//				cf = 16;	//Don't update the family object, the server handles that
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server denied Driver Update," +
						"try again later","Driver Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
		if(!odbWishListPane.getText().equals(fam.getODBWishList())) {fam.setODBWishList(odbWishListPane.getText()); cf = 17;}
		if(!oncNotesPane.getText().equals(fam.getNotes())) {fam.setNotes(oncNotesPane.getText()); cf = 18;}
		if(!oncDIPane.getText().equals(fam.getDeliveryInstructions())) {fam.setDeliveryInstructions(oncDIPane.getText()); cf = 19;}
		
		if(cf > 0)
		{
//			System.out.println(String.format("Family Panel - Family Change Detected, Field: %d", cf));
			
			fam.setChangedBy(gvs.getUserLNFI());
			lblChangedBy.setText(fam.getChangedBy());	//Set the changed by field to current user
//			notifyFamilyUpdateOccurred();	//Wont need this in future
			
			String response = fDB.update(this, fam);
			if(response.startsWith("UPDATED_FAMILY"))
			{
//				System.out.println("FamilyPanel- response: " + response);
				//family id will not change from update request, get updated family
				//from the data base and display
				ONCFamily updatedFamily = fDB.getFamily(fam.getID());
//				System.out.println(String.format("Family Panel - DNS: %s", updatedFamily.getDNSCode()));
				display(updatedFamily, getDisplayedChild());
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server Error: " + response +
						", try again later","Family Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
	}
	
	void notifyFamilyUpdateOccurred()
	{
		//Update the family sort table if the family dialog is visible
		if(sortFamiliesDlg.isVisible())
			sortFamiliesDlg.buildTableList(true);
		
		if(sortAgentDlg.isVisible())
			sortAgentDlg.buildFamilyTableListAndDisplay();

		if(assignDeliveryDlg.isVisible())
			assignDeliveryDlg.buildTableList(true);
	}
	
	void addChildrentoTable(ArrayList<ONCChild> childAL, boolean bDispAll)
    {	
    	for(int index=0; index < ctAL.size(); index++)
    	{
    		ONCChild child = ctAL.get(index);
    		
    		if(bDispAll)
    			childTableModel.addRow(new String[]{child.getChildFirstName(), 
    												child.getChildLastName(),
        											child.getChildDOBString("MM/dd/yy"),
        											child.getChildGender()});
    		else
    			childTableModel.addRow(new String[]{"Child " + Integer.toString(index+1),
    												"",
													child.getChildDOBString("MM/dd/yy"),
													child.getChildGender()});		
    	}
    	
    	childTable.setRowSelectionInterval(cn, cn);
    	childTable.requestFocus();
    }
	
	void ClearChildTable()
    {
		for(int i = childTableModel.getRowCount() - 1; i >=0; i--)		
		   childTableModel.removeRow(i);  	
    }
	
	void RefreshChildTable(String[] childdata, int row)
	{
		for(int i=0; i<4; i++)
			childTableModel.setValueAt(childdata[i], row, i);
	}

	void editAltAddress()
	{
		//Set up the dialog to edit agent info
		String[] tfNames = {"House #", "Street", "Unit", "City", "Zip Code"};
    	AltAddressDialog altAddDlg = new AltAddressDialog(parentFrame, true, tfNames);
    	altAddDlg.display(currFam);
    	altAddDlg.setLocationRelativeTo(altAddressRB);
    	altAddDlg.setVisible(true);
	}
	
	void setMssg(String mssg, boolean bDefault)
	{ 
		if(bDefault)
			nav.setDefaultMssg(mssg);
		else
			nav.setMssg(mssg);
	}
	
	void updateDBStatus(int[] dbCounts)
    {
    	nav.setCount1("Served Families: " + Integer.toString((dbCounts[0])));
    	nav.setCount2("Served Children: " + Integer.toString((dbCounts[1])));
    }
	
	 /*********************************************************************************************
     * This method imports a .csv file that contains a ONC Family Referral Worksheet. It creates
     * a RAFamilyImporter object and registers the family panel to receive family selection 
     * events from the importer. Then it executes the importer which will interface with 
     * the user to select and import a sequence of ONC Family Referral Worksheets
     *********************************************************************************************/
    void onImportRAFMenuItemClicked()
    {
    	RAFamilyImporter importer = new RAFamilyImporter(GlobalVariables.getFrame());
    	importer.addEntitySelectionListener(familyChildSelectionListener);
    	importer.onImportRAFMenuItemClicked();
    	importer.removeEntitySelectionListener(familyChildSelectionListener);
    }
    
    /*********************************************************************************************
     * This method updates the family panel when the user imports one or more families
     *********************************************************************************************/
    void onFamilyDataLoaded()
    {
    	nav.navSetEnabled(true);
		
		setEnabledButtons(true);
		setEditableGUIFields(true);
		updateDBStatus(fDB.getServedFamilyAndChildCount());
		display(fDB.getObjectAtIndex(nav.getIndex()), null);
		nav.setStoplightEntity(fDB.getObjectAtIndex(nav.getIndex()));
		
		if(gvs.isUserAdmin())
			setRestrictedEnabledButtons(true);
    }
	
	void showDeliveryStatus()
	{
		if(!dsDlg.isShowing())
		{
			dsDlg.setLocationRelativeTo(City);
			dsDlg.displayDeliveryInfo(currFam);
			dsDlg.setVisible(true);
		}
	}
	
	void showDrivingDirections()
	{
		if(!dirDlg.isShowing())
		{
			updateDrivingDirections();
			dirDlg.setVisible(true);
		}
	}
	
	void showClientMap(ArrayList<ONCFamily> oncFAL)
	{
		if(!cmDlg.isShowing())
		{
			try
    		{
    			cmDlg.displayClientMap(oncFAL);
    		} 
    		catch (IOException e1)
    		{
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
		}
	}
	
	void showSortWishesDialog(ArrayList<ONCFamily> fAL)
	{
		if(!sortWishesDlg.isVisible())
		{
			//Dates are set here after Global Variables have been initialized from server
			sortWishesDlg.setSortStartDate(gvs.getSeasonStartDate());
			sortWishesDlg.setSortEndDate(gvs.getTodaysDate());
			sortWishesDlg.buildTableList(true);
			
			sortWishesDlg.setLocationRelativeTo(GlobalVariables.getFrame());
			sortWishesDlg.setVisible(true);
		}
	}
	
	void showReceiveGiftsDialog(ArrayList<ONCFamily> fAL)
	{
		String[] colTT = {"ONC Family Number", "Child Gender", "Child's Age",  
				"Wish Selected for Child", "Wish Detail"};
		String[] columns = {"ONC", "Gend", "Age", "Wish Type", "Details"};
		int[] colWidths = {40, 48, 72, 120, 248};
		
		ReceiveGiftsDialog recGiftsDlg = new ReceiveGiftsDialog(parentFrame, colTT, columns, 
									colWidths, null, WishStatus.Received);
    	recGiftsDlg.addEntitySelectionListener(familyChildSelectionListener);
    	
		recGiftsDlg.buildTableList(false);
			
		recGiftsDlg.setLocationRelativeTo(GlobalVariables.getFrame());
		recGiftsDlg.setVisible(true);
	}
	
	void showWishCatalogDialog()
	{
		if(!catDlg.isVisible())
		{
			Point pt = parentFrame.getLocation();
	        catDlg.setLocation(pt.x + 125, pt.y + 125);
			catDlg.setVisible(true);
		}
	}
	
	void initializeCatalogWishCounts()
	{
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		cat.initializeWishCounts(fDB);
	}
	
	void showAssignDelivererDialog()
	{
		if(!assignDeliveryDlg.isVisible())
		{
			assignDeliveryDlg.buildTableList(true);
			Point pt = parentFrame.getLocation();
	        assignDeliveryDlg.setLocation(pt.x + 5, pt.y + 20);
			assignDeliveryDlg.setVisible(true);
		}
	}
	
	void showDriverDialog()
	{	
		if(!driverDlg.isVisible())
		{
			driverDlg.display(null);
			Point pt = parentFrame.getLocation();
	        driverDlg.setLocation(pt.x + 5, pt.y + 20);
			driverDlg.setVisible(true);
		}
	}
	
	void showSortDriverDialog()
	{
		if(!sortDriverDlg.isVisible())
		{
			sortDriverDlg.buildTableList(true);
			
			Point pt = parentFrame.getLocation();
	        sortDriverDlg.setLocation(pt.x + 5, pt.y + 20);
			sortDriverDlg.setVisible(true);
		}
	}
	
	
	void showSortFamiliesDialog(ArrayList<ONCFamily> fAL)
	{
		if(!sortFamiliesDlg.isVisible())
		{
			sortFamiliesDlg.buildTableList(true);
			
			Point pt = parentFrame.getLocation();
	        sortFamiliesDlg.setLocation(pt.x + 5, pt.y + 20);
			sortFamiliesDlg.setVisible(true);
		}
	}
	
	void showSortOrgsDialog()
	{
		if(!sortOrgsDlg.isVisible())
		{
			sortOrgsDlg.buildTableList(true);
			Point pt = GlobalVariables.getFrame().getLocation();
	        sortOrgsDlg.setLocation(pt.x + 5, pt.y + 20);
			sortOrgsDlg.setVisible(true);
		}
	}
	
	void showSortAgentDialog(ONCAgents agentsDB, ArrayList<ONCFamily> fAL)
	{
		if(!sortAgentDlg.isVisible())
		{
			sortAgentDlg.buildTableList(true);
			sortAgentDlg.setLocationRelativeTo(parentFrame);
			sortAgentDlg.setVisible(true);
		}
	}
	
	void showAgentInfoDialog(int source)
	{
		if(!agentInfoDlg.isVisible())
		{
			agentInfoDlg.display(agentDB.getAgent(currFam.getAgentID()));
			agentInfoDlg.setLocationRelativeTo(btnShowAgentInfo);
			agentInfoDlg.showDialog();
		}
	}
	
	/****************************************************************************************
     * Shows a dialog box to change the ONC Number for the family displayed. If no family is 
     * displayed the method displays a warning message
     */
    void showChangeONCNumberDialog()
    {
    	if(!changeONCNumberDlg.isVisible())
		{
    		changeONCNumberDlg.display(currFam);
    		changeONCNumberDlg.setLocationRelativeTo(lblONCNum);
    		changeONCNumberDlg.showDialog();
		}
    }
	
	void showEntireDatabase(Families fDB)
	{
		if(!dbDlg.isVisible())
		{
			dbDlg.buildDatabase(fDB);
			dbDlg.setVisible(true);
		}
	}
	
	void showOrgDialog()
	{
		if(!orgDlg.isVisible())
	    {
	    	orgDlg.setLocation((int)parentFrame.getLocation().getX() + 22, (int)parentFrame.getLocation().getY() + 22);
	        orgDlg.display(null);
	        orgDlg.setVisible(true);
	    }
	}
	
	void onCheckForDuplicateChildren()
	{
	    if(!dcDlg.isVisible())
	    {
	    	dcDlg.buildDupTable();
	    	dcDlg.setVisible(true);
	    }
	}
	    
	void onCheckForDuplicateFamilies()
	{
	    if(!dfDlg.isVisible())
	    {
	    	dfDlg.buildDupTable();
	    	dfDlg.setVisible(true);
	    }
	}
	
	void updateDrivingDirections()
	{   	
		try {
			dirDlg.displayDirections(currFam);
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		dirDlg.setVisible(true);
	}
	
	//Determines whether to show all personal data or restrict data 
	void setFamilyPanelDisplayPermission(boolean disp_all)
	{
		bDispAll = disp_all;
	}
	
	void setEnabledSuperuserPrivileges(boolean tf)
	{
		sortFamiliesDlg.setFamilyStatusComboItemEnabled(FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX, tf);
	}
	
	void updateComboBoxModels()
	{
		oncChildPanel.updateWishSelectionList();
		sortWishesDlg.updateWishSelectionList();
		
		oncChildPanel.updateWishAssigneeSelectionList();
		sortWishesDlg.updateWishAssigneeSelectionList();
		
		sortAgentDlg.updateOrgCBList();
		sortAgentDlg.updateTitleCBList();
		
		sortDriverDlg.updateLNameCBList();
	}
	
	void updateComboBoxBorders()
	{
		orgDlg.updateComboBoxBorders();
	}
	
	void onAddNewChildClicked()
	{
		String[] fieldNames = {"First Name", "Last Name", "School", "Gender", "Date of Birth"};
		AddNewChildDialog newchildDlg = new AddNewChildDialog(parentFrame, fieldNames, currFam);
		newchildDlg.setLocationRelativeTo(childTable);
		newchildDlg.showDialog();
		
		ONCChild newchild = newchildDlg.getNewChild();
		if(newchild != null)
		{
			cDB.add(this, newchild);
		}
	}
	
	/****************
	 * Method fetches a prior year child from the server for the current child selected in
	 * the child table. It displays the wish history for the child in a modal dialog. No
	 * checking is performed to determine whether the child has a prior history, that is
	 * assumed to have already occurred if the prior year history button was enabled. 
	 * If the sever isn't connected, or the server doesn't respond with a valid message, 
	 * the method returns false. It returns true if prior year history is successfully displayed. 
	 * @return true or false
	 **************/
	boolean onShowPriorHistory()
	{
		ServerIF serverIF = ServerIF.getInstance();
		
		if(serverIF != null && serverIF.isConnected())
		{
			String ly = Integer.toString(GlobalVariables.getCurrentSeason()-1);
			String py = Integer.toString(GlobalVariables.getCurrentSeason()-2);
		
			//Get prior year child from server
			String zPYCID = Integer.toString(ctAL.get(cn).getPriorYearChildID());
			String response = null;
			response = serverIF.sendRequest("GET<pychild>" + zPYCID);
			
			if(response != null && response.startsWith("PYC"))
			{		
				Gson gson = new Gson();
				ONCPriorYearChild pyc = gson.fromJson(response.substring(3), ONCPriorYearChild.class);
		
				String[] pyWishes = pyc.getPriorYearWishes();
		
				StringBuffer wishes = new StringBuffer("");			
		
				if(pyWishes[0].equals(""))
					wishes.append(ly + " Wish 1: Empty\n");
				else
				wishes.append(ly + " Wish 1: " + pyWishes[0] + "\n");
		
				if(pyWishes[1].equals(""))
				wishes.append(ly + " Wish 2: Empty\n");
				else
					wishes.append(ly + " Wish 2: " + pyWishes[1] + "\n");
		
				if(pyWishes[2].equals(""))
					wishes.append(ly + " Wish 3: Empty\n\n");
				else
					wishes.append(ly + " Wish 3: " + pyWishes[2] + "\n\n");
		
				if(pyWishes[3].equals(""))
					wishes.append(py + " Wish 1: Empty\n");
				else
					wishes.append(py + " Wish 1: " + pyWishes[3] + "\n");
		
				if(pyWishes[4].equals(""))
					wishes.append(py + " Wish 2: Empty\n");
				else
					wishes.append(py + " Wish 2: " + pyWishes[4] + "\n");
		
				if(pyWishes[5].equals(""))
					wishes.append(py + " Wish 3: Empty");
				else
					wishes.append(py + " Wish 3: " + pyWishes[5]);			
		
				JOptionPane.showMessageDialog(parentFrame, wishes, oncChildPanel.firstnameTF.getText() +
				"'s Gift History", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
			
				return true;
			}
			else
				return false;
		}
		else 
			return false;			
	}
	
	void setEnabledAssignedDeliveryStatus(boolean tf) { delStatus[DELIVERY_STATUS_ASSIGNED].setEnabled(tf); }
	
	/**********************************************************************************************
	 * This method is called to cause all gui's that display ONC numbers to rebuild after
	 * ONC numbers have been reassigned. 
	 **********************************************************************************************/
/*
	void notifyONCNumbersReassigned()
	{
		//Update the family sort table if the family dialog is visible
		if(sortFamiliesDlg.isVisible())
			sortFamiliesDlg.buildSortTable();
				
		if(sortAgentDlg.isVisible())
			sortAgentDlg.buildFamilyTableListAndDisplay();
		
		if(sortWishesDlg.isVisible())	//Update the wish management dialog
			sortWishesDlg.buildSortTableList(true);

		if(assignDeliveryDlg.isVisible())
			assignDeliveryDlg.buildSortTable();
	}
*/
	
	/**
	 * This method gets a reference to the current child and commands the child panel
	 * to update it. If the child can't be found, the child panel is cleared. 
	 */
	void updateChild()
	{	
		ONCChild c = null;
		if(ctAL.size() > 0 && cn < ctAL.size())
			c = cDB.getChild(ctAL.get(cn).getID());
		
		if(c != null)
			oncChildPanel.updateChild(c);
		else
			System.out.println("Family Panel updateChild() - Can't update, child object is null");
	}
	
	/**
	 * This method gets a reference to the current child and commands the child panel
	 * to display it.If the child can't be found, the child panel is cleared. 
	 */
	void displayChild(ONCChild c, int cn)
	{	
		if(c != null)
			oncChildPanel.displayChild(c, cn);
		else
			oncChildPanel.clearChildData();
	}
			
	
	/*************************************************************************************************************
	 * This method is called when the user requests to delete a child from the menu bar. The child to be deleted
	 * is the child currently selected in child table in the family panel. The first step in deletion is to confirm
	 * with the user that they intended to delete the child. 
	 **********************************************************************************************************/
	void deleteChild()
	{
		//Obtain the child to be deleted
		if(currFam != null)
		{
			//Save any changed family data prior to the deletion of the child
			checkAndUpdateFamilyData(currFam);
			
			ONCChild delChild = ctAL.get(cn);
		
			//Confirm with the user that the deletion is really intended
			String confirmMssg =String.format("Are you sure you want to delete %s %s from the data base?", 
											delChild.getChildFirstName(), delChild.getChildLastName());
		
			Object[] options= {"Cancel", "Delete"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(parentFrame, "*** Confirm Child Database Deletion ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete"))
			{
				cDB.delete(this, delChild);
			}
		}
	}
	

    /******************************************************************************************
     * This method automatically assigns an ONC number to a family that doesn't have one.The 
     * server assigns all ONC #'s. The client simply asks the server to do. Using
     * the region (must not be ?) and the ranges of permissible ONC numbers by region, the server
     * method calls the generateONCNumber function, then sort the family array list, saves and
     * displays the updated family
     ******************************************************************************************/
    void autoassignONCNum()
    {
    	//Generate and save the new onc number. First update the family panel text field,
    	
    	String response = fDB.update_AutoONCNum(this, currFam);
    	if(response.startsWith("UPDATED_FAMILY"))
		{
			//family id will not change from update request, get updated family
			//from the data base and display
			ONCFamily updatedFamily = fDB.getFamily(currFam.getID());
			display(updatedFamily, getDisplayedChild());
		}
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server denied family auto ONC Number asssingment," +
					"try again later","Family Auto Assign ONC # Failed",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
    }
/*    
	void registerForTableEvents(TableSelectionListener fcsl)
	{
		//Set up the listener for the family, wish , receive gift and assign 
        //driver dialogs. When a table row selection event occurs in the tables in these dialogs,
        //the family and child (if applicable) that is selected in the tables is displayed in
        //the family panel
        sortFamiliesDlg.addTableSelectionListener(fcsl);
        assignDeliveryDlg.addTableSelectionListener(fcsl);
        sortAgentDlg.addTableSelectionListener(fcsl);
        sortWishesDlg.addTableSelectionListener(fcsl);
        recGiftsDlg.addTableSelectionListener(fcsl);
	}
*/	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnShowPriorHistory) 
		{
			if(onShowPriorHistory() == false)
				JOptionPane.showMessageDialog(parentFrame, "Wish History Currently Unavailable",
						"Wish History Currently Unavailable",  JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == btnShowAllPhones)
		{
			JOptionPane.showMessageDialog(parentFrame, currFam.getAllPhoneNumbers(),
					currFam.getClientFamily() + " family phone #'s", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == btnShowAgentInfo)
		{   
	        showAgentInfoDialog(0); //Source is family panel button
		}
		else if(e.getSource() == btnShowODBDetails)
		{
			//Wrap the ODB Details string on 50 character length boundary's
			String[] input = currFam.getODBDetails().split(" ");
			StringBuffer output = new StringBuffer("");
			int linelength = 0;
			
			for(int i=0; i<input.length; i++)
			{
				output.append(input[i] + " ");
				
				if((linelength += input[i].length() + 1) > 50 )
				{
					output.append("\n");
					linelength = 0;
				}
			}
			
			JOptionPane.showMessageDialog(parentFrame, output.toString(),
				"ODB Details For ONC Family #" + currFam.getONCNum(), JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == btnAssignONCNum)
		{
			autoassignONCNum();
		}
		else if(e.getSource() == delRB)
		{
			showDeliveryStatus();
		}
		else if(e.getSource() == altAddressRB)			 
		{	
			editAltAddress();
		}
		else if(e.getSource() == housenumTF && !bFamilyDataChanging && 
				!housenumTF.getText().equals(currFam.getHouseNum()) && gvs.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);
			
		}
		else if(e.getSource() == Street && !bFamilyDataChanging && 
								!Street.getText().equals(currFam.getStreet()) && gvs.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);	
		}
		else if(e.getSource() == City && !bFamilyDataChanging &&
								!City.getText().equals(currFam.getCity()) && gvs.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);
			
		}
		else if(e.getSource() == ZipCode && !bFamilyDataChanging &&
				!ZipCode.getText().equals(currFam.getZipCode()) && gvs.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);
		}
		else if(e.getSource() == oncBatchNum && !bFamilyDataChanging &&
									!oncBatchNum.getSelectedItem().equals(currFam.getBatchNum()))
		{
			checkAndUpdateFamilyData(currFam);
		}
		else if(e.getSource() == oncDNScode && !bFamilyDataChanging &&
									!oncDNScode.getText().equals(currFam.getDNSCode()))
		{	
			checkAndUpdateFamilyData(currFam);
		}
		else if(e.getSource() == statusCB && !bFamilyDataChanging &&
									statusCB.getSelectedIndex() != currFam.getFamilyStatus())
		{
			//If family status is changing to PACKAGED, solicit the number of bags. Else, the 
			//number of bags must be zero
			if(statusCB.getSelectedIndex() == FAMILY_STATUS_PACKAGED)
			{
				FamilyBagDialog fbDlg = new FamilyBagDialog(parentFrame);
				fbDlg.setVisible(true);
				
				lblNumBags.setText(Integer.toString(fbDlg.getNumOfBags()));
				currFam.setNumOfBags(fbDlg.getNumOfBags());
				currFam.setNumOfLargeItems(fbDlg.getNumOfLargeItems());
			}
			else
			{
				lblNumBags.setText("0");
				currFam.setNumOfBags(0);
				currFam.setNumOfLargeItems(0);
			}
			
			checkAndUpdateFamilyData(currFam);
		}
		else if(e.getSource() == delstatCB && !bFamilyDataChanging &&
				delstatCB.getSelectedIndex() != currFam.getDeliveryStatus())
		{
			checkAndUpdateFamilyData(currFam);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if(!e.getValueIsAdjusting() && e.getSource() == childTable.getSelectionModel() && !bChildTableDataChanging && 
				cDB.getNumberOfChildrenInFamily(currFam.getID()) > 0)
		{
			//If user can change child data, save possible updated data prior to displaying newly selected child
			//and update the Family Panel Child Table
			if(bDispAll)
			{
				updateChild();
			}
			
			//Get new child selected by user and display their information
			cn = childTable.getSelectedRow();
			refreshODBWishListHighlights(currFam, ctAL.get(cn));
			refreshPriorHistoryButton(currFam, ctAL.get(cn));

			displayChild(ctAL.get(cn), cn);
		}		
	}

	class FamilyBagDialog extends JDialog implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private JComboBox bagsPackagedCB, largeItemCB;
		private JButton btnOK;
		
		FamilyBagDialog(JFrame pf)
		{
			super(pf, true);
			this.setTitle("Family Packging Bag Detail");
			
			String[] selections = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
			
			JPanel bagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblBagMssg = new JLabel("Select the number of bags used");
			bagsPackagedCB = new JComboBox(selections);
//			bagsPackagedCB.setMaximumSize(new Dimension(80,44));
			bagPanel.add(lblBagMssg);
			bagPanel.add(bagsPackagedCB);
			
			JPanel liPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblLIMssg = new JLabel("Select the number of large items");
			largeItemCB = new JComboBox(selections);
//			largeItemsCB.setMaximumSize(new Dimension(80, 44));
			liPanel.add(lblLIMssg);
			liPanel.add(largeItemCB);
					
			JPanel cntlpanel = new JPanel();
			btnOK = new JButton("Ok");
			btnOK.addActionListener(this);
			cntlpanel.add(btnOK);
			
			 //Add the components to the frame pane
	        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
	        this.add(bagPanel);
	        this.add(liPanel);
	        this.add(cntlpanel);
	        
	        pack();
	        this.setLocationRelativeTo(statusCB);
//	        setSize(280, 180);
//	        setResizable(false);
		}
		
		int getNumOfBags()
		{
			return bagsPackagedCB.getSelectedIndex(); 
		}
		
		int getNumOfLargeItems()
		{
			return largeItemCB.getSelectedIndex();
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == btnOK)
				this.dispose();			
		}
	}
	
	/***********************************************************************************************
	 * This class implements a listener for the fields in the family panel that need to check for 
	 * data updates when the user presses the <Enter> key. The only action this listener takes is to
	 * call the check and update family data method which checks if the data has changed, if it has 
	 * it saves the new data and set the flag that unsaved changes have occurred to family data. 
	 ***********************************************************************************************/
	private class DataChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			checkAndUpdateFamilyData(currFam);
		}	
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
//			System.out.println(String.format("FamilyPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			ONCFamily updatedFam = (ONCFamily) dbe.getObject();
			
			//If current family being displayed has changed, reshow it
			if(currFam.getID() == updatedFam.getID())
				display(updatedFam, this.getDisplayedChild()); //Don't change the displayed child
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY"))
		{
//			System.out.println(String.format("FamilyPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			ONCFamily updatedFam = (ONCFamily) dbe.getObject();
			
			//If no current family being displayed (null) display the added family
			if(currFam == null)
			{
				display(updatedFam, null); //No child to display, probably
//				System.out.println(String.format("FamilyPanel DB Event -- displayed family: Source: %s, Type: %s, Object: %s",
//						dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			}
		}
		
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject();
			
			//If current family being displayed contains child, refresh the table
			if(updatedChild.getFamID() == currFam.getID())	
			{
				//Find the child row and update it
				int row = 0;
				while(row < ctAL.size() && ctAL.get(row).getID() != updatedChild.getID())
					row++;
				
				if(row < ctAL.size())
				{
					//update the child table list
					ctAL.set(row, updatedChild);
					
					//update the table row
					bChildTableDataChanging = true;
				
					if(gvs.isUserAdmin())
					{
						childTableModel.setValueAt(updatedChild.getChildFirstName(), row, 0);
						childTableModel.setValueAt(updatedChild.getChildLastName(), row, 1);
					}
				
					childTableModel.setValueAt(updatedChild.getChildDOBString("M/d/yy"), row, 2);
					childTableModel.setValueAt(updatedChild.getChildGender(), row, 3);
				
					bChildTableDataChanging = false;
					
					//If current child is being displayed on child panel, refresh the
					//prior year history button and refresh the child panel
					if(childTable.getSelectedRow() == row)
					{
						refreshODBWishListHighlights(currFam, updatedChild);
						refreshPriorHistoryButton(currFam, updatedChild);
						oncChildPanel.displayChild(updatedChild, row);
					}
				}
			}
		}
		
		else if(dbe.getType().equals("ADDED_CHILD")) 
		{
			ONCChild addedChild = (ONCChild) dbe.getObject();
				
			if(currFam.getID() == addedChild.getFamID())	//Child was added to the displayed family
			{
				ONCFamily fam = fDB.getFamily(addedChild.getFamID());
				if(fam != null)
					display(fam, null);
			}
		}
		else if(dbe.getType().equals("DELETED_CHILD")) 
		{
			ONCChild deletedChild = (ONCChild) dbe.getObject();
			
			if(currFam.getID() == deletedChild.getFamID())	//Child was deleted from the displayed family
			{
				ONCFamily fam = fDB.getFamily(deletedChild.getFamID());
				if(fam != null)
					display(fam, null);
			}
		}
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
		{
			ONCDelivery updatedDelivery = (ONCDelivery) dbe.getObject();
				
			//If updated delivery belongs to family being displayed, re-display it
			if(currFam.getID() == updatedDelivery.getFamID())
			{
				ONCFamily fam = fDB.getFamily(updatedDelivery.getFamID());
				if(fam != null)
					display(fam, null);
			}
		}
		else if(dbe.getType().equals("UPDATED_SERVED_COUNTS"))
		{
//			System.out.println(String.format("StatusPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			DataChange servedCountsChange = (DataChange) dbe.getObject();
			int[] changes = {servedCountsChange.getOldData(), servedCountsChange.getNewData()};
			updateDBStatus(changes);
		}
	}

	private class FamilyChildSelectionListener implements EntitySelectionListener
    {
		@Override
		public void entitySelected(EntitySelectionEvent tse)
		{
			if(tse.getType().equals("FAMILY_SELECTED") || tse.getType().equals("WISH_SELECTED"))
			{
//				System.out.println(String.format("FamilyPanel.entitySelected: Type = %s", tse.getType()));
				ONCFamily fam = (ONCFamily) tse.getObject1();
				ONCChild child = (ONCChild) tse.getObject2();
			
				//if family and child selected not displayed, then update currFam and display.
				if(fam.getID() != currFam.getID() && tse.getSource() != nav ||
						fam.getID() == currFam.getID() && tse.getSource() !=nav &&
						currChild !=null && child != null && child.getID() != currChild.getID())
				{
					int rtn;
					if((rtn=fDB.searchForFamilyIndexByID(fam.getID())) >= 0)
					{
						update();
						nav.setIndex(rtn);
						display(fam, child);
						
						nav.setStoplightEntity(fam);
					}
				}
				else if(fam.getID() != currFam.getID() && tse.getSource() == nav)
				{
					update();
					display(fam, child);
				}
			}
		}
    }
}

