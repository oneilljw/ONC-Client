package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class FamilyPanel extends ONCPanel implements ActionListener, ListSelectionListener, DatabaseListener,
													EntitySelector, EntitySelectionListener
{
	/**
	 * This class provides the blueprint for the main panel in the ONC application. It contains a number of 
	 * GUI elements and a child sub panel
	 */
	private static final long serialVersionUID = 1L;
	private static final int FAMILY_STATUS_PACKAGED = 5;
	private static final int DELIVERY_STATUS_ASSIGNED = 3;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	
	//Icon references for the icon bar
	private static final int REQUESTED_MEAL_ICON_INDEX = 30;
	private static final int TRANSPORTATION_ICON_INDEX = 31;
	private static final int HISTORY_ICON_INDEX = 32;
	private static final int AGENT_INFO_ICON_INDEX = 33;
	private static final int FAMILY_DETAILS_ICON_INDEX = 34;
	private static final int PHONE_ICON_INDEX = 35;
	private static final int NO_TRANSPORTATION_ICON_INDEX = 36;
	private static final int REFERRED_MEAL_ICON_INDEX = 37;
	private static final int NO_MEAL_ICON_INDEX = 38;
	private static final int GOOGLE_MAP_ICON_INDEX = 39;
	private static final int NOT_GIFT_CARD_ONLY_ICON_INDEX = 40;
	private static final int GIFT_CARD_ONLY_ICON_INDEX = 41;
	private static final int ADULT_ICON_INDEX = 42;
	private static final int NO_ADULT_ICON_INDEX = 43;
	
	private static final String[] dStat = {"Empty", "Contacted", "Confirmed", "Assigned", "Attempted",
											"Returned", "Delivered", "Counselor Pick-Up"};
	//data base references
    private Families fDB;
	private ChildDB cDB;
	private AdultDB adultDB;
	private DeliveryDB deliveryDB;
	private ONCRegions regions;
	
	private ONCFamily currFam;	//The panel needs to know which family is being displayed
	private ONCChild currChild;	//The panel needs to know which child is being displayed
	
	private JPanel p1, p2, p3;
	private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
	
	private ONCNavPanel nav;	//public to allow adding/removal of Entity Selection Listeners
	private JTextPane oncNotesPane, oncDIPane, wishlistPane;
	private JScrollPane wishlistScrollPane;
	private JTextPane homePhonePane, otherPhonePane;
	private JButton btnAssignONCNum;
	private JTextField oncDNScode;
	private JTextField HOHFirstName, HOHLastName, EMail;
	private JTextField housenumTF, Street, Unit, City, ZipCode;
	private JLabel lblONCNum, lblRefNum, lblBatchNum, lblRegion, lblNumBags, lblChangedBy, lblDelStatus;
	private JRadioButton rbDeliveryHistory, rbAltAddress, rbMeals, rbPriorHistory, rbAgentInfo;
	private JRadioButton rbShowAllPhones, rbFamDetails, rbTransportation, rbDirections;
	private JRadioButton rbNotGiftCardOnly, rbGiftCardOnly, rbAdults;
	private JComboBox Language, statusCB;
	private ComboItem[] delStatus;
	public  JTable childTable;
//	private DefaultTableModel childTableModel;
	private ChildTableModel childTableModel;
	private ArrayList<ONCChild> ctAL; //List holds children object references being displayed in table
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	private boolean bChildTableDataChanging = true;	//Flag indicating that Child Table data is changing
	
	//An instance of the private subclass of the default highlight painter
	private static DefaultHighlighter.DefaultHighlightPainter highlightPainter;
	
	private ChildPanel oncChildPanel;
	private WishPanel[] wishPanelList;
	
	//Dialogs that are children of the family panel
//	private static ClientMapDialog cmDlg;
	
//	private AdultDialog adultDlg;
//	private DirectionsDialog dirDlg;
//	private WishCatalogDialog catDlg;
//	private ONCUserDialog userDlg;
//	private ViewONCDatabaseDialog dbDlg;
//	private PYChildConnectionDialog pyConnectionDlg;
//	private AngelAutoCallDialog angelDlg;
	
	//dialogs that inherit from HistoryDialog
//	private Map<String, HistoryDialog> historyDlgMap;
//	private DeliveryHistoryDialog dsDlg;
//	private MealDialog mealDlg;
	
	//dialogs that inherit from InfoDialog
//	private Map<String, InfoDialog> familyInfoDlgMap;
//	private AgentInfoDialog agentInfoDlg;
//	private TransportationDialog transportationDlg;
//	private AddMealDialog addMealDlg;
//	private ChangeONCNumberDialog changeONCNumberDlg;
//	private ChangeReferenceNumberDialog changeReferenceNumberDlg;
//	private ChangeBatchNumberDialog changeBatchNumberDlg;
	
	//dialogs that inherit from CheckDialog
//	private Map<String, CheckDialog> checkDlgMap;
//	private ChildCheckDialog dcDlg;
//	private FamilyCheckDialog dfDlg;
	
	//dialogs that inherit from SortTableDialog
//	private Map<String, SortTableDialog> stDlgMap;
//	private SortFamilyDialog sortFamiliesDlg;
//	private SortAgentDialog sortAgentDlg;
//	private AssignDeliveryDialog assignDeliveryDlg;
//	private SortDriverDialog sortDriverDlg;
//	private SortPartnerDialog sortPartnerDlg;
//	private SortWishDialog sortWishesDlg;
//	private SortMealsDialog sortMealsDlg;
//	private ReceiveGiftsDialog recGiftsDlg;
	
	//dialogs that inherit from Entity Dialog
//	private Map<String, EntityDialog> entityDlgMap;
//	private OrganizationDialog orgDlg;
//	private DriverDialog driverDlg;
	
//	private LogDialog logDlg;
	
	public FamilyPanel(JFrame pf)
	{
		super(pf);
		
		//register database listeners for updates
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		adultDB = AdultDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		
		if(fDB != null)
			fDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(adultDB != null)
			adultDB.addDatabaseListener(this);
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
		currFam = null;
		
		//create the highlight painter used in the wish list pane
		highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

		//Set layout and border for the Family Panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		//get a reference to the EntityEventManager
        EntityEventManager eeManager = EntityEventManager.getInstance();
        
        //register this panel as a selector and listener
        eeManager.registerEntitySelector(this);
	    eeManager.registerEntitySelectionListener(this);
		
		//Setup the nav panel
		nav = new ONCNavPanel(pf, fDB);
		nav.setMssg("Our Neighbor's Child Families");
	    nav.setCount1("Served Families: " + Integer.toString(0));
	    nav.setCount2("Served Children: " + Integer.toString(0));
	    nav.setNextButtonText("Next Family");
	    nav.setPreviousButtonText("Previous Family");
//	    eeManager.registerEntitySelector(nav);	//register nav as entity selector
	    
		//Setup sub panels that comprise the Family Panel
		p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pBkColor = p1.getBackground();
		JPanel p4 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JPanel iconBar = new JPanel();

		//Set up the text fields for each of the characteristics displayed
		DataChangeListener fdcListener = new DataChangeListener();
        lblONCNum = new JLabel("");
        lblONCNum.setPreferredSize(new Dimension(60, 52));
        lblONCNum.setToolTipText("Seasonal Family Reference # - new each year");
        lblONCNum.setBorder(BorderFactory.createTitledBorder("ONC #"));
        lblONCNum.setHorizontalAlignment(JLabel.CENTER);
        
        lblRefNum = new JLabel("No Fams");
        lblRefNum.setPreferredSize(new Dimension(72, 52));
        lblRefNum.setToolTipText("Family Reference # - Consistent across years");
        lblRefNum.setBorder(BorderFactory.createTitledBorder("Ref #"));
        lblONCNum.setHorizontalAlignment(JLabel.CENTER);
        
        lblBatchNum = new JLabel();
        lblBatchNum.setPreferredSize(new Dimension(64, 52));
        lblBatchNum.setToolTipText("Indicates family intake grouping");
        lblBatchNum.setBorder(BorderFactory.createTitledBorder("Batch #"));
        lblBatchNum.setHorizontalAlignment(JLabel.CENTER);
        
        oncDNScode = new JTextField(6);
        oncDNScode.setToolTipText("Do not serve family code: e.g, SA= Salvation Army");
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
        lblNumBags.setPreferredSize(new Dimension(48, 52));
        lblNumBags.setBorder(BorderFactory.createTitledBorder("Bags"));
             
        homePhonePane = new JTextPane();
        JScrollPane homePhoneScrollPane = new JScrollPane(homePhonePane);
        homePhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        homePhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Home Phone(s)"));
        homePhonePane.setEditable(false);
        
        otherPhonePane = new JTextPane();
        JScrollPane otherPhoneScrollPane = new JScrollPane(otherPhonePane);
        otherPhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        otherPhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Alternate Phone(s)"));
        otherPhonePane.setEditable(false);
      
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
        
        lblDelStatus = new JLabel();
        lblDelStatus.setBorder(BorderFactory.createTitledBorder("Delivery Status"));
        lblDelStatus.setPreferredSize(new Dimension(132, 52));               

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
        
        rbAltAddress = new JRadioButton(gvs.getImageIcon(19));
        rbAltAddress.setToolTipText("Click to see alternate address");
        rbAltAddress.setEnabled(false);
        rbAltAddress.addActionListener(this);
 
        lblChangedBy = new JLabel();
        lblChangedBy.setPreferredSize(new Dimension(128, 44));
        lblChangedBy.setToolTipText("Shows ONC volunteer who last changed family data");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
        
        btnAssignONCNum = new JButton("Assign ONC #");
        btnAssignONCNum.setToolTipText("Click to have the system assign an ONC Number to family");
        btnAssignONCNum.setVisible(false);	//Invisible until ONC Number invalid and region valid
        btnAssignONCNum.addActionListener(this);
        
        rbPriorHistory = new JRadioButton(gvs.getImageIcon(HISTORY_ICON_INDEX));
        rbPriorHistory.setToolTipText("Click for prior ONC gift history for highlighted child");
        rbPriorHistory.setEnabled(false);
        rbPriorHistory.addActionListener(this);
        
        rbShowAllPhones = new JRadioButton(gvs.getImageIcon(PHONE_ICON_INDEX));
        rbShowAllPhones.setToolTipText("Click for all phone numbers for family");
        rbShowAllPhones.setEnabled(false);
        rbShowAllPhones.addActionListener(this);
        
        rbAgentInfo = new JRadioButton(gvs.getImageIcon(AGENT_INFO_ICON_INDEX));
        rbAgentInfo.setToolTipText("Click for info on agent who referred family");
        rbAgentInfo.setEnabled(false);
        rbAgentInfo.addActionListener(this);
        
        rbFamDetails = new JRadioButton(gvs.getImageIcon(FAMILY_DETAILS_ICON_INDEX));
        rbFamDetails.setToolTipText("Click for additional details for this family");
        rbFamDetails.setEnabled(false);
        rbFamDetails.addActionListener(this);
        
        rbMeals = new JRadioButton(gvs.getImageIcon(REQUESTED_MEAL_ICON_INDEX));
        rbMeals.setActionCommand("Meal History");
        rbMeals.setToolTipText("Click for family food assistance status");
        rbMeals.setEnabled(false);
        rbMeals.addActionListener(this);
        
        rbTransportation = new JRadioButton(gvs.getImageIcon(TRANSPORTATION_ICON_INDEX));
        rbTransportation.setToolTipText("Click for family transportation status");
        rbTransportation.setEnabled(false);
        rbTransportation.addActionListener(this);
        
        rbDirections = new JRadioButton(gvs.getImageIcon( GOOGLE_MAP_ICON_INDEX));
        rbDirections.setToolTipText("Click for directions to family address");
        rbDirections.setEnabled(false);
        rbDirections.addActionListener(this);
        
        rbNotGiftCardOnly = new JRadioButton(gvs.getImageIcon(NOT_GIFT_CARD_ONLY_ICON_INDEX));
        rbNotGiftCardOnly.setToolTipText("Click for change to Gift Card Only Family");
        rbNotGiftCardOnly.setEnabled(false);
        rbNotGiftCardOnly.setVisible(true);
        rbNotGiftCardOnly.addActionListener(this);
        
        rbGiftCardOnly = new JRadioButton(gvs.getImageIcon(GIFT_CARD_ONLY_ICON_INDEX));
        rbGiftCardOnly.setToolTipText("Click for from Gift Card Only Family");
        rbGiftCardOnly.setEnabled(false);
        rbGiftCardOnly.setVisible(false);
        rbGiftCardOnly.addActionListener(this);
        
        rbAdults = new JRadioButton(gvs.getImageIcon(ADULT_ICON_INDEX));
        rbAdults.setToolTipText("Manage Adults in Family");
        rbAdults.setEnabled(false);
        rbAdults.addActionListener(this);

        rbDeliveryHistory = new JRadioButton(gvs.getImageIcon(14));
        rbDeliveryHistory.setActionCommand("Delivery History");
        rbDeliveryHistory.setToolTipText("Click to see delivery history");
        rbDeliveryHistory.setEnabled(false);
        rbDeliveryHistory.addActionListener(this);
        
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
		
		childTableModel = new ChildTableModel();
       
 //       childTableModel = new DefaultTableModel(new Object[]{"First Name","Last Name", "DoB", "Gend"},0)
 //       {
 //          private static final long serialVersionUID = 1L;
 //          @Override
 //          	//every cell cannot be edited
 //          	public boolean isCellEditable(int row, int column) {return false;}
 //       };      
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
        wishlistPane = new JTextPane();
        wishlistPane.setToolTipText("Wish suggestions for child from referral");
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        wishlistPane.setParagraphAttributes(attribs, true);
  	   	wishlistPane.setEditable(false);
  	   	
	    //Create the ODB Wish List scroll pane and add the Wish List text pane to it.
        wishlistScrollPane = new JScrollPane(wishlistPane);
        wishlistScrollPane.setBorder(BorderFactory.createTitledBorder("Child Wish List"));
        
        //Set up the ONC Notes Pane
        oncNotesPane = new JTextPane();
        oncNotesPane.setToolTipText("Family specific notes entered by ONC elf");
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
        oncDIPane.setToolTipText("Family specific delivery instructions entered by ONC elf");
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        oncDIPane.setParagraphAttributes(attribs,true);             
	   	oncDIPane.setEditable(false);
	   	
	    //Create the ONC Delivery Instructions scroll pane and add the ONC Note text pane to it.
        JScrollPane oncDIscrollPane = new JScrollPane(oncDIPane);
        oncDIscrollPane.setBorder(BorderFactory.createTitledBorder("Delivery Instructions"));
        
/*        
        //initialize the dialog maps
        stDlgMap = new HashMap<String, SortTableDialog>();
        familyInfoDlgMap = new HashMap<String, InfoDialog>();
        entityDlgMap = new HashMap<String, EntityDialog>();
        checkDlgMap = new HashMap<String, CheckDialog>();
        historyDlgMap = new HashMap<String, HistoryDialog>();
        
        //Set up delivery history dialog box 
        dsDlg = new DeliveryHistoryDialog(parentFrame);
        historyDlgMap.put("Delivery History", dsDlg);
        eeManager.registerEntitySelectionListener(dsDlg);
        
        //Set up meal history dialog box 
        mealDlg = new MealDialog(parentFrame);
        historyDlgMap.put("Meal History", mealDlg);
        eeManager.registerEntitySelectionListener(mealDlg);
        
        //Set up adult dialog box 
        adultDlg = new AdultDialog(pf);
        eeManager.registerEntitySelectionListener(adultDlg);
       
        //Set up delivery directions dialog box 
        try { dirDlg = new DirectionsDialog(parentFrame); }
		catch (JSONException e1) {// TODO Auto-generated catch block 
			e1.printStackTrace();}
        eeManager.registerEntitySelectionListener(dirDlg);
        
        //Set up client map dialog
        cmDlg = new ClientMapDialog(parentFrame); 
        
        //Set up the sort wishes dialog
        sortWishesDlg = new SortWishDialog(parentFrame);
        eeManager.registerEntitySelector(sortWishesDlg);
        stDlgMap.put("Wishes", sortWishesDlg);

    	//Set up the manage catalog dialog
    	catDlg = new WishCatalogDialog(parentFrame);
    	
    	//Set up the manage user dialog
    	userDlg = new ONCUserDialog(parentFrame);
    	
    	 //Set up the sort family dialog
        sortFamiliesDlg = new SortFamilyDialog(parentFrame);
        stDlgMap.put("Families", sortFamiliesDlg);
        eeManager.registerEntitySelector(sortFamiliesDlg);
        
      //Set up the sort meals dialog
        sortMealsDlg = new SortMealsDialog(parentFrame);
        stDlgMap.put("Meals", sortMealsDlg);
        eeManager.registerEntitySelector(sortMealsDlg);
    	
    	//Set up the dialog to edit agent info
    	String[] tfNames = {"Name", "Organization", "Title", "Email", "Phone"};
    	agentInfoDlg = new AgentInfoDialog(parentFrame, tfNames, false);
    	familyInfoDlgMap.put("Agent", agentInfoDlg);
    	eeManager.registerEntitySelectionListener(agentInfoDlg);
    	
    	//Set up the dialog to edit family transportation info
    	String[] transNames = {"ONC #", "Last Name", "Has Transportation?"};
    	transportationDlg = new TransportationDialog(parentFrame, false, transNames);
    	familyInfoDlgMap.put("Transportation", transportationDlg);
    	eeManager.registerEntitySelectionListener(transportationDlg);
    	
    	//Set up the dialog to add a meal to family
    	String[] mealNames = {"ONC #", "Last Name", "Meal Type", "Restrictions"};
    	addMealDlg = new AddMealDialog(parentFrame, true, mealNames);
    	familyInfoDlgMap.put("Add Meal", addMealDlg);
    	
    	//Set up the dialog to change family ONC Number
    	String[] oncNum = {"Change ONC #"};
    	changeONCNumberDlg = new ChangeONCNumberDialog(parentFrame, oncNum);
    	familyInfoDlgMap.put("Change ONC #", changeONCNumberDlg);
    	
    	//Set up the dialog to change family ODB Number
    	String[] refNum = {"Change Ref #"};
    	changeReferenceNumberDlg = new ChangeReferenceNumberDialog(parentFrame, refNum);
    	familyInfoDlgMap.put("Change Ref #", changeReferenceNumberDlg);
    	
    	//Set up the dialog to change family batch number
    	String[] batchNum = {"Change Batch #"};
    	changeBatchNumberDlg = new ChangeBatchNumberDialog(parentFrame, batchNum);
    	familyInfoDlgMap.put("Change Batch #", changeBatchNumberDlg);

    	//Set up the sort agent dialog
    	sortAgentDlg = new SortAgentDialog(parentFrame);
    	stDlgMap.put("Agents", sortAgentDlg);
    	eeManager.registerEntitySelector(sortAgentDlg);
    	
    	//set up the assign delivery dialog
    	assignDeliveryDlg = new AssignDeliveryDialog(parentFrame);
    	stDlgMap.put("Deliveries", assignDeliveryDlg);
    	eeManager.registerEntitySelector(sortAgentDlg);
    	
    	//set up the sort driver dialog
    	sortDriverDlg = new SortDriverDialog(parentFrame);
    	stDlgMap.put("Drivers", sortDriverDlg);
    	eeManager.registerEntitySelector(sortDriverDlg);
    	
    	//Set up the edit driver (deliverer) dialog and register it to listen for Family 
    	//Selection events from particular ui's that have driver's associated
        driverDlg = new DriverDialog(parentFrame);
        entityDlgMap.put("Edit Delivery Partners", driverDlg);
        eeManager.registerEntitySelectionListener(driverDlg);
        
        //Set up the view family database dialog
        dbDlg = new ViewONCDatabaseDialog(parentFrame);
        
        //Set up the edit gift partner dialog
        orgDlg = new OrganizationDialog(parentFrame);
        entityDlgMap.put("Edit Partners", orgDlg);
        eeManager.registerEntitySelectionListener(orgDlg);
        
      //Set up the Angel auto-call dialog
       angelDlg = new AngelAutoCallDialog(parentFrame);
   	   eeManager.registerEntitySelector(angelDlg);
   	   
        
        //Set up the sort gift partner dialog
        sortPartnerDlg = new SortPartnerDialog(parentFrame);
        stDlgMap.put("Partners", sortPartnerDlg);
        eeManager.registerEntitySelector(sortPartnerDlg);
        
        //set up the data check dialog and table row selection listener
        dcDlg = new ChildCheckDialog(pf);
        checkDlgMap.put("Duplicate Children Check", dcDlg);
        eeManager.registerEntitySelector(dcDlg);
        
        //set up the family check dialog and table row selection listener
        dfDlg = new FamilyCheckDialog(pf);
        checkDlgMap.put("Duplicate Family Check", dfDlg);
        eeManager.registerEntitySelector(dfDlg);
*/        
        //Create the Child Panel
        oncChildPanel = new ChildPanel();
//      this.addEntitySelectionListener(oncChildPanel);
        eeManager.registerEntitySelectionListener(oncChildPanel);
/*        
        //set up a dialog to receive gifts
		recGiftsDlg = new ReceiveGiftsDialog(parentFrame, WishStatus.Received);
		stDlgMap.put("Receive Gifts", recGiftsDlg);
    	eeManager.registerEntitySelector(recGiftsDlg);
        
        //set up a dialog to connect prior year children
    	pyConnectionDlg = new PYChildConnectionDialog(parentFrame);
        eeManager.registerEntitySelectionListener(pyConnectionDlg);
*/        
        //create the wish panels
        JPanel childwishespanel = new JPanel(new GridLayout(1,3));
        wishPanelList = new WishPanel[NUMBER_OF_WISHES_PER_CHILD];
        for(int wp=0; wp < wishPanelList.length; wp++)
        {
        	wishPanelList[wp] = new WishPanel(wp);
        	childwishespanel.add(wishPanelList[wp]);
        	
        	//register the entity selection listeners
            eeManager.registerEntitySelectionListener(wishPanelList[wp]);
        }
        
        //Add components to the panels
        p1.add(lblONCNum);
        p1.add(lblRefNum);
        p1.add(lblBatchNum);
        p1.add(oncDNScode);
        p1.add(HOHFirstName);
        p1.add(HOHLastName);
        p1.add(statusCB);
        p1.add(lblNumBags);
        
        p2.add(homePhoneScrollPane);
        p2.add(otherPhoneScrollPane);
        p2.add(EMail);
		p2.add(Language);
		p2.add(lblDelStatus);
		
        p3.add(housenumTF);
        p3.add(Street);
        p3.add(Unit);
        p3.add(City);
        p3.add(ZipCode);
        p3.add(lblRegion);
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
        p4.add(wishlistScrollPane, c);
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
        
        iconBar.add(btnAssignONCNum);
        iconBar.add(rbPriorHistory);
        iconBar.add(rbShowAllPhones);
        iconBar.add(rbAltAddress);
        iconBar.add(rbFamDetails);
        iconBar.add(rbAgentInfo);
        iconBar.add(rbMeals);
        iconBar.add(rbTransportation);
        iconBar.add(rbAdults);
        iconBar.add(rbNotGiftCardOnly);
        iconBar.add(rbGiftCardOnly);
        iconBar.add(rbDirections);
        iconBar.add(rbDeliveryHistory);
              
        this.add(nav);
        this.add(p1);
        this.add(p2);
        this.add(p3);
        this.add(p4);
        this.add(iconBar);
        this.add(oncChildPanel);
        this.add(childwishespanel);
	}

	void setEditableGUIFields(boolean tf)
	{
		if(GlobalVariables.isUserAdmin())
		{
			oncDNScode.setEditable(tf);
			HOHFirstName.setEditable(tf);
			HOHLastName.setEditable(tf);;
			statusCB.setEnabled(tf);
			homePhonePane.setEditable(tf);
			otherPhonePane.setEditable(tf);
			EMail.setEditable(tf);
			Language.setEnabled(tf);
			housenumTF.setEditable(tf);
			Street.setEditable(tf);
			Unit.setEditable(tf);
			City.setEditable(tf);
			ZipCode.setEditable(tf);
			wishlistPane.setEditable(tf);
			oncNotesPane.setEditable(tf);
			oncDIPane.setEditable(tf);
		}
	}
/*	
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
*/	
	void setTextPaneFontSize()
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        wishlistPane.setParagraphAttributes(attribs, true);
        oncNotesPane.setParagraphAttributes(attribs, true);
        oncDIPane.setParagraphAttributes(attribs, true);
	}
	
	void setRestrictedEnabledButtons(boolean tf)
	{	 
		rbShowAllPhones.setEnabled(tf);
		rbAltAddress.setEnabled(tf);
	}
	
	void setEnabledButtons(boolean tf) 
	{ 
		rbAgentInfo.setEnabled(tf);
		rbDeliveryHistory.setEnabled(tf); 
	}

/*	
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
*/
	void display(ONCFamily fam, ONCChild child)
	{
		int cn = 0; //allows child number in family to be passed with only one search
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
			if(!ctAL.isEmpty())
			{
				currChild = ctAL.get(0);
				LogDialog.add("FamilyPanel.display ONC# " + currFam.getONCNum() + ", 1st Child: " + currChild.getChildFirstName(), "M");
			}
			else
				LogDialog.add("FamilyPanel.display ONC# " + currFam.getONCNum() + ", No Children in Family", "M");
			
			cn = 0;
		}
		else
		{
			currFam = fam;
			ctAL = cDB.getChildren(currFam.getID());
			int index = 0;
			while(index < ctAL.size() && ctAL.get(index).getID() != child.getID())
				index++;
			
			if(index < ctAL.size())
			{
				cn = index;
				currChild = ctAL.get(index);
				LogDialog.add("FamilyPanel.display ONC# " + currFam.getONCNum() + ", Requested Child: " + currChild.getChildFirstName(), "M");
			}
		}		
		
		p1.setBackground(pBkColor);
		p2.setBackground(pBkColor);
		p3.setBackground(pBkColor);
		
		rbMeals.setEnabled(true);
		rbTransportation.setEnabled(true);
		rbDirections.setEnabled(true);
		rbNotGiftCardOnly.setEnabled(true);
		rbNotGiftCardOnly.setVisible(true);
		rbGiftCardOnly.setEnabled(true);
		rbGiftCardOnly.setVisible(false);
		rbAdults.setEnabled(true);
		
		lblONCNum.setText(currFam.getONCNum());
		lblONCNum.setToolTipText("Family Database ID= " + Integer.toString(currFam.getID()));
		lblRefNum.setText(currFam.getODBFamilyNum());
		lblBatchNum.setText(currFam.getBatchNum());
		oncDNScode.setText(currFam.getDNSCode());
		oncDNScode.setCaretPosition(0);
		
		checkForDNSorGiftCardOnly();
		
		statusCB.setSelectedIndex(currFam.getFamilyStatus());
		lblNumBags.setText(Integer.toString(currFam.getNumOfBags()));
		
		lblDelStatus.setText(dStat[currFam.getDeliveryStatus()]);
		Language.setSelectedItem((String)currFam.getLanguage());
		lblChangedBy.setText(currFam.getChangedBy());
		lblRegion.setText(regions.getRegionID(currFam.getRegion()));
		
		oncNotesPane.setText(currFam.getNotes());
		oncNotesPane.setCaretPosition(0);
		oncDIPane.setText(currFam.getDeliveryInstructions());
		oncDIPane.setCaretPosition(0);
		
		//set meal button icon
		if(currFam.getMealID()  == -1)
		{
			rbMeals.setIcon(gvs.getImageIcon( NO_MEAL_ICON_INDEX));
			rbMeals.setActionCommand("Add Meal");
		}
		else if(currFam.getMealStatus() == MealStatus.Requested)	//meal not referred yet
		{
			rbMeals.setIcon(gvs.getImageIcon(REQUESTED_MEAL_ICON_INDEX));
			rbMeals.setActionCommand("Meal History");
		}
		else
		{
			rbMeals.setIcon(gvs.getImageIcon( REFERRED_MEAL_ICON_INDEX));
			rbMeals.setActionCommand("Meal History");
		}
		
		//set transportation button icon
		if(currFam.getTransportation() == Transportation.Yes)
			rbTransportation.setIcon(gvs.getImageIcon(TRANSPORTATION_ICON_INDEX));
		else
			rbTransportation.setIcon(gvs.getImageIcon(NO_TRANSPORTATION_ICON_INDEX));
		
		addChildrenToTable(cn);	//update child table
		checkForAdultsInFamily(); //set the adults icon
		
		nav.setStoplightEntity(currFam);
		nav.btnNextSetEnabled(true);
		nav.btnPreviousSetEnabled(true);
		
		if(GlobalVariables.isUserAdmin())	//show personal information for family
		{	
			HOHFirstName.setText(currFam.getHOHFirstName());
			HOHLastName.setText(currFam.getHOHLastName());
			
			homePhonePane.setText(currFam.getHomePhone());
			homePhonePane.setCaretPosition(0);
			otherPhonePane.setText(currFam.getOtherPhon());
			otherPhonePane.setCaretPosition(0);
			EMail.setText(currFam.getFamilyEmail());
			EMail.setCaretPosition(0);
			
			housenumTF.setText(currFam.getHouseNum());
			Street.setText(currFam.getStreet());
			Street.setCaretPosition(0);
			Unit.setText(currFam.getUnitNum());
			Unit.setCaretPosition(0);
			City.setText(currFam.getCity());
			ZipCode.setText(currFam.getZipCode());
			if(currFam.getSubstituteDeliveryAddress() != null &&
				!currFam.getSubstituteDeliveryAddress().isEmpty())
				rbAltAddress.setIcon(gvs.getImageIcon(20));
			else
				rbAltAddress.setIcon(gvs.getImageIcon(19));
			
			wishlistPane.setText(currFam.getODBWishList());
			
			rbFamDetails.setEnabled(currFam.getDetails().length() > 1);
			
			//Test to see if an ONC number could be assigned. If so, make the auto assign button visible
			if(!lblONCNum.getText().equals("DEL") && !lblRegion.getText().equals("?") &&
					  !Character.isDigit(lblONCNum.getText().charAt(0)))	
				btnAssignONCNum.setVisible(true);
			else
				btnAssignONCNum.setVisible(false);
			
		}
		else	//eliminate personal data in wish list
		{	
			//Find all names in the wish list and replace them with "Child x" before displaying
			if(!ctAL.isEmpty())
			{	
				String[] replace = new String[ctAL.size() * 2 + 1];
				replace[0] = currFam.getODBWishList();
			
				int cnum = 0, sn = 0;
				while(cnum < ctAL.size())
				{
					replace[sn+1] = replace[sn].replaceAll(ctAL.get(cnum).getChildFirstName(),
							"Child " + Integer.toString(cnum+1));
				
					replace[sn+2] = replace[sn+1].replaceFirst(ctAL.get(cnum).getChildLastName(), "");
				
					cnum++;	
					sn += 2;			
				}
			
				String almostDone = replace[replace.length-1];
				String done = almostDone.replaceAll(" :", ":");
				wishlistPane.setText(done);
			}
			else
			{
				wishlistPane.setText("");
			}
		}
		
		if(ctAL.size() > 0)	//Check to see if the family has children
		{
			refreshODBWishListHighlights(currFam, currChild);
			refreshPriorHistoryButton(currFam, currChild);
		}
		else
		{
			refreshODBWishListHighlights(currFam, null);
			rbPriorHistory.setEnabled(false);
		}
	}
	
	void addChildrenToTable(int cn)
	{
		bChildTableDataChanging = true;		
//		ClearChildTable();
		if(ctAL.size() > 0)
		{		
//			addChildrentoTable(ctAL, bDispAll, cn);
			childTableModel.fireTableDataChanged();
			childTable.setRowSelectionInterval(cn, cn);
	    	childTable.requestFocus();
			if(GlobalVariables.isUserAdmin())
				ONCMenuBar.setEnabledMarkorDeleteChildMenuItem(true);	//Enable Delete Child Menu Bar item
		}
		else
		{
			//family has no children, clear the child panel
			currChild = null;
			ONCMenuBar.setEnabledMarkorDeleteChildMenuItem(false);	//Disable Delete Child Menu Bar item
		}
		bChildTableDataChanging = false;
	}
	
	void checkForDNSorGiftCardOnly()
	{
		//determine what color to paint the background
		if(currFam.getDNSCode().length() > 1)
		{
			p1.setBackground(Color.RED);
			p2.setBackground(Color.RED);
			p3.setBackground(Color.RED);
		}
		else if(currFam.isGiftCardOnly())
		{
			p1.setBackground(Color.GREEN);
			p2.setBackground(Color.GREEN);
			p3.setBackground(Color.GREEN);
			
			rbNotGiftCardOnly.setVisible(false);
			rbGiftCardOnly.setVisible(true);
		}
	}
	
	void checkForAdultsInFamily()
	{	
		//set the adults icon
		if(adultDB.getAdultsInFamily(currFam.getID()).size() > 0)
			rbAdults.setIcon(gvs.getImageIcon(ADULT_ICON_INDEX));
		else
			rbAdults.setIcon(gvs.getImageIcon(NO_ADULT_ICON_INDEX));	
	}

	void refreshPriorHistoryButton(ONCFamily fam, ONCChild c)
	{
		if(c != null && c.getPriorYearChildID() != -1)
			rbPriorHistory.setEnabled(true);
		else
			rbPriorHistory.setEnabled(false);
	}
	
	void refreshODBWishListHighlights(ONCFamily fam, ONCChild c)
	{		
		wishlistPane.getHighlighter().removeAllHighlights();		
		String odbWishList = wishlistPane.getText().toLowerCase();
		
		if(c == null)	
			return;	//No children to highlight
		
		else if(GlobalVariables.isUserAdmin())	//Show all data
		{	
			String childfn = c.getChildFirstName().toLowerCase();
			String childln = c.getChildLastName().toLowerCase();
			String childname = childfn + " " + childln;
			
			int startPos;
			
			if(odbWishList.indexOf(childname) == -1)	//Found 1st instance of child first name
				startPos = odbWishList.indexOf(childfn);
			else
				startPos = odbWishList.indexOf(childname);
				
			if(startPos > -1)	//Found 1st instance of child name
			{
				int endPos = odbWishList.indexOf(':', startPos);
				if(endPos != -1)
				{	
					endPos = odbWishList.indexOf(childln, startPos);
					if(endPos != -1)
						endPos += childln.length();
					else
						endPos = startPos + childfn.length();
				}
				
				highlightAndCenterODBWish(startPos, endPos);
			}
		}
		else //Show restricted data
		{
			String childfn = "child " + Integer.toString(childTable.getSelectedRow() + 1);
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
			wishlistPane.setCaretPosition(startPos);
			wishlistPane.getHighlighter().addHighlight(startPos, endPos+1, highlightPainter);
			int caretPosition = wishlistPane.getCaretPosition();
			Rectangle caretRectangle = wishlistPane.modelToView(caretPosition);
			Rectangle viewRectangle = new Rectangle(0, caretRectangle.y -
			        (wishlistScrollPane.getHeight() - caretRectangle.height) / 2,
			        wishlistScrollPane.getWidth(), wishlistScrollPane.getHeight());
			wishlistPane.scrollRectToVisible(viewRectangle);
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
	}
	
	/***************************************************************************************************
	*Query family data input fields to get data and record changes. This method is called when
	*family data may have changed by either an external object or by an internal component event 
	*such as the enter key or combo box list selection change
	****************************************************************************************************/
	void checkAndUpdateFamilyData(ONCFamily family)
	{
		if(!GlobalVariables.isUserAdmin())	//must have administrative privilege to make changes to families
			return;

		//make a copy of the current family object to create the request
		ONCFamily fam = new ONCFamily(family);
		
		int cf = 0;	//used to indicate if a change is detected to a GUI field
		
		//If data has been changed store updated family info and return true to indicate a change
		if(!oncDNScode.getText().equals(fam.getDNSCode())) //DNS code changed
		{
			//Save the new DNS text field and mark that a field has changed
			fam.setDNSCode(oncDNScode.getText());
			cf = 3;
		}
		if(!HOHFirstName.getText().equals(fam.getHOHFirstName())) {fam.setHOHFirstName(HOHFirstName.getText()); cf = 4;}
		if(!HOHLastName.getText().equals(fam.getHOHLastName())) {fam.setHOHLastName(HOHLastName.getText()); cf = 5;}
		if(Integer.parseInt(lblNumBags.getText()) != fam.getNumOfBags()) {fam.setNumOfBags(Integer.parseInt(lblNumBags.getText())); cf = 20;}
		if(!homePhonePane.getText().equals(fam.getHomePhone())) {fam.setHomePhone(homePhonePane.getText()); cf = 6;}
		if(!otherPhonePane.getText().equals(fam.getOtherPhon())) {fam.setOtherPhon(otherPhonePane.getText()); cf = 7;}
		if(!EMail.getText().equals(fam.getFamilyEmail())) {fam.setFamilyEmail(EMail.getText()); cf = 8;}
		if(!Language.getSelectedItem().toString().equals(fam.getLanguage())){fam.setLanguage(Language.getSelectedItem().toString());cf = 9;}
		if(!housenumTF.getText().equals(fam.getHouseNum())) {fam.setHouseNum(housenumTF.getText()); cf = 10;}
		if(!Street.getText().equals(fam.getStreet())) {fam.setStreet(Street.getText()); cf = 11;}
		if(!Unit.getText().equals(fam.getUnitNum())) {fam.setUnitNum(Unit.getText()); cf = 12;}
		if(!City.getText().equals(fam.getCity())) {fam.setCity(City.getText()); cf = 13;}
		if(!ZipCode.getText().equals(fam.getZipCode())) {fam.setZipCode(ZipCode.getText()); cf = 14;}
		if(statusCB.getSelectedIndex() != fam.getFamilyStatus()) {fam.setFamilyStatus(statusCB.getSelectedIndex()); cf = 15;}
		if(!wishlistPane.getText().equals(fam.getODBWishList())) {fam.setODBWishList(wishlistPane.getText()); cf = 17;}
		if(!oncNotesPane.getText().equals(fam.getNotes())) {fam.setNotes(oncNotesPane.getText()); cf = 18;}
		if(!oncDIPane.getText().equals(fam.getDeliveryInstructions())) {fam.setDeliveryInstructions(oncDIPane.getText()); cf = 19;}
		
		if(cf > 0)
		{
//			System.out.println(String.format("Family Panel - Family Change Detected, Field: %d", cf));
			
			fam.setChangedBy(GlobalVariables.getUserLNFI());
			lblChangedBy.setText(fam.getChangedBy());	//Set the changed by field to current user
			
			String response = fDB.update(this, fam);
			if(response.startsWith("UPDATED_FAMILY"))
			{
//				System.out.println("FamilyPanel- response: " + response);
				//family id will not change from update request, get updated family
				//from the data base and display
				ONCFamily updatedFamily = fDB.getFamily(fam.getID());
//				System.out.println(String.format("Family Panel - DNS: %s", updatedFamily.getDNSCode()));
				display(updatedFamily, currChild);
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
/*
	void addChildrentoTable(ArrayList<ONCChild> childAL, boolean bDispAll, int cn)
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
	
	void refreshChildTable(String[] childdata, int row)
	{
		for(int i=0; i<4; i++)
			childTableModel.setValueAt(childdata[i], row, i);
	}
*/
	void editAltAddress()
	{
		//Set up the dialog to edit agent info
    	AltAddressDialog altAddDlg = new AltAddressDialog(parentFrame, true);
    	altAddDlg.display(currFam);
    	altAddDlg.setLocationRelativeTo(rbAltAddress);
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
     * This method updates the family panel when the user imports one or more families
     *********************************************************************************************/
    void onFamilyDataLoaded()
    {
    	nav.navSetEnabled(true);
    	
		setEnabledButtons(true);
		setEditableGUIFields(true);
		updateDBStatus(fDB.getServedFamilyAndChildCount());
		ONCFamily firstFam = fDB.getObjectAtIndex(nav.getIndex());
		
		if(firstFam != null)
		{
			display(firstFam, null);
			fireEntitySelected(this, EntityType.FAMILY, firstFam, null);
			nav.setStoplightEntity(fDB.getObjectAtIndex(nav.getIndex()));
			
			if(GlobalVariables.isUserAdmin())
				setRestrictedEnabledButtons(true);
		}
    }
 /*   
    void showHistoryDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(historyDlgMap.containsKey(name))
		{
			if(!historyDlgMap.get(name).isShowing())
			{
				historyDlgMap.get(name).setLocationRelativeTo(this);
				historyDlgMap.get(name).display(currFam);
				historyDlgMap.get(name).setVisible(true);
			}
		}
		else
			showDialogError(name);
	}

	void showAdultDialog()
	{
		if(!adultDlg.isShowing())
		{
			adultDlg.setLocationRelativeTo(rbAdults);
			adultDlg.display(currFam);
			adultDlg.setVisible(true);
		}
	}
	
	void showDrivingDirections()
	{
		if(!dirDlg.isShowing())
		{
			dirDlg.display(currFam);
			dirDlg.setVisible(true);
		}
	}
	
	void showClientMap()
	{
		if(!cmDlg.isShowing())
		{
			cmDlg.display();
			cmDlg.setVisible(true);
		}
	}

	void showWishCatalogDialog()
	{
		if(!catDlg.isVisible())
		{
	        catDlg.setLocationRelativeTo(this);
			catDlg.setVisible(true);
		}
	}
	
	void showUserDialog()
	{
		if(!userDlg.isVisible())
		{
	        userDlg.setLocationRelativeTo(this);
			userDlg.setVisible(true);
		}
	}
	
	void initializeCatalogWishCounts()
	{
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		cat.initializeWishCounts(fDB);
	}
/*	
	void showAngelCallDialog()
	{
		if(!angelDlg.isVisible())
		{	
			Point pt = parentFrame.getLocation();
	        angelDlg.setLocation(pt.x + 5, pt.y + 20);
			angelDlg.setVisible(true);
		}
	}
*/	
/*	
	void showSortDialog(String name, Point offsetPt)
	{
		//retrieve the sort dialog from the map
		if(stDlgMap.containsKey(name))
		{
			if(!stDlgMap.get(name).isVisible())
			{
				stDlgMap.get(name).initializeFilters();
				stDlgMap.get(name).buildTableList(true);
				
				Point originPt = GlobalVariables.getFrame().getLocation();
				stDlgMap.get(name).setLocation(originPt.x + offsetPt.x, originPt.y + offsetPt.y);
		        stDlgMap.get(name).setVisible(true);
			}
		}
		else
			showDialogError(name);
	}
	
	void showFamilyInfoDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(familyInfoDlgMap.containsKey(name))
		{
			if(!familyInfoDlgMap.get(name).isVisible())
			{
				familyInfoDlgMap.get(name).display(currFam);
				familyInfoDlgMap.get(name).setLocationRelativeTo(this);
				familyInfoDlgMap.get(name).showDialog();
			}
		}	
		else
			showDialogError(name);
	}
	
	void showEntityDialog(String name, Point offsetPt)
	{
		//retrieve the sort dialog from the map
		if(entityDlgMap.containsKey(name))
		{
			if(!entityDlgMap.get(name).isVisible())
			{
				entityDlgMap.get(name).display(null);
				Point originPt = GlobalVariables.getFrame().getLocation();
				entityDlgMap.get(name).setLocation(originPt.x + offsetPt.x, originPt.y + offsetPt.y);
				entityDlgMap.get(name).setVisible(true);
			}
		}	
		else
			showDialogError(name);
	}
	
	void showCheckDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(checkDlgMap.containsKey(name))
		{
			if(!checkDlgMap.get(name).isVisible())
			{
				checkDlgMap.get(name).buildTableList();
				checkDlgMap.get(name).setLocationRelativeTo(this);
				checkDlgMap.get(name).setVisible(true);
			}
		}	
		else
			showDialogError(name);
	}

	void showDialogError(String name)
	{
		String errMssg = String.format("<html>Show Famaily Info Dialog Error:<br>%s dialog doesn't exist,<br>"
				+ "						please contact the ONC IT Director</html>", name);
		JOptionPane.showMessageDialog(parentFrame, errMssg, "System Error - Show Family Info Dialog",
				JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	}
    
    void showConnectPYChildDialog()
    {
    	if(!pyConnectionDlg.isVisible())
		{
    		pyConnectionDlg.display(currFam, currChild);
    		pyConnectionDlg.setLocationRelativeTo(lblDelStatus);
    		pyConnectionDlg.setVisible(true);
		}
    }
/*	
	void showEntireDatabase()
	{
		if(!dbDlg.isVisible())
		{
			dbDlg.buildDatabase();
			dbDlg.setVisible(true);
		}
	}
	
	void setEnabledSuperuserPrivileges(boolean tf)
	{
		sortFamiliesDlg.setFamilyStatusComboItemEnabled(FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX, tf);
	}
*/	
	void onAddNewChildClicked()
	{
		AddNewChildDialog newchildDlg = new AddNewChildDialog(parentFrame, currFam);
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
		String ly = Integer.toString(GlobalVariables.getCurrentSeason()-1);
		String py = Integer.toString(GlobalVariables.getCurrentSeason()-2);
		
		//Get prior year child
		ONCPriorYearChild pyc = cDB.getPriorYearChild(currChild.getPriorYearChildID());
			
		if(pyc != null)
		{		
			String[] pyWishes = pyc.getPriorYearWishes();
		
			StringBuffer wishes = new StringBuffer("");			
		
			if(pyWishes[0].equals(""))
					wishes.append(ly + " Wish 1: Family Not Served\n");
			else
			wishes.append(ly + " Wish 1: " + pyWishes[0] + "\n");
		
			if(pyWishes[1].equals(""))
				wishes.append(ly + " Wish 2: Family Not Served\n");
			else
				wishes.append(ly + " Wish 2: " + pyWishes[1] + "\n");
		
			if(pyWishes[2].equals(""))
				wishes.append(ly + " Wish 3: Family Not Served\n\n");
			else
				wishes.append(ly + " Wish 3: " + pyWishes[2] + "\n\n");
		
			if(pyWishes[3].equals(""))
				wishes.append(py + " Wish 1: Family Not Served\n");
			else
				wishes.append(py + " Wish 1: " + pyWishes[3] + "\n");
		
			if(pyWishes[4].equals(""))
				wishes.append(py + " Wish 2: Family Not Served\n");
			else
				wishes.append(py + " Wish 2: " + pyWishes[4] + "\n");
		
			if(pyWishes[5].equals(""))
				wishes.append(py + " Wish 3: Family Not Served");
			else
				wishes.append(py + " Wish 3: " + pyWishes[5]);			
		
			//determine child's first name, is it protected? Get it from table
			String childFN;
			if(GlobalVariables.isUserAdmin())
				childFN = currChild.getChildFirstName();
			else
				childFN = "Child " + Integer.toString(childTable.getSelectedRow() + 1);
				
			JOptionPane.showMessageDialog(parentFrame, wishes, childFN + "'s Gift History",
						JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
			
			return true;
		}
		else
			return false;
	}
	
	void setEnabledAssignedDeliveryStatus(boolean tf) { delStatus[DELIVERY_STATUS_ASSIGNED].setEnabled(tf); }
	
	/*************************************************************************************************************
	 * This method is called when the user requests to mark a child as an adult from the menu bar. 
	 * The child object is added to the adult db and deleted from the child db.The child to be
	 * marked as an adult is the child currently selected in child table in the family panel.
	 * The first step in mark is to confirm with the user that they intended to mark as an adult. 
	 **********************************************************************************************************/
	void markChildAsAdult()
	{
		//Obtain the child to be marked as an Adult
		if(currFam != null)
		{
			//Save any changed family data prior to moving the child to adult
			checkAndUpdateFamilyData(currFam);
			
			ONCChild delChild = currChild;
		
			//Confirm with the user that the mark is really intended
			String confirmMssg =String.format("Are you sure you want to change %s %s to an adult?", 
											delChild.getChildFirstName(), delChild.getChildLastName());
		
			Object[] options= {"Cancel", "Make Adult"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(parentFrame, "*** Confirm Child to Adult Change ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Make Adult"))
			{
				//create a new adult object and add it to the adult database
				String name = delChild.getChildFirstName() + " " + delChild.getChildLastName();
				AdultGender gender;
				if(delChild.getChildGender().toLowerCase().equals("girl"))
					gender = AdultGender.Female;
				else if(delChild.getChildGender().toLowerCase().equals("boy"))
					gender = AdultGender.Male;
				else
					gender = AdultGender.Unknown;
				
				//add to the adult database
				adultDB.add(this, new ONCAdult(-1, delChild.getFamID(), name, gender));
				
				//delete from the child data base
				cDB.delete(this, delChild);
			}
		}
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
			
			ONCChild delChild = currChild;
		
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
				cDB.delete(this, delChild);
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
			display(updatedFamily, currChild);
		}
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server denied family auto ONC Number asssingment," +
					"try again later","Family Auto Assign ONC # Failed",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
    }

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == rbPriorHistory) 
		{
			if(onShowPriorHistory() == false)
				JOptionPane.showMessageDialog(parentFrame, "Wish History Currently Unavailable",
						"Wish History Currently Unavailable",  JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == rbShowAllPhones)
		{
			JOptionPane.showMessageDialog(parentFrame, currFam.getAllPhoneNumbers(),
					currFam.getClientFamily() + " family phone #'s", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == rbAgentInfo)
		{   
	        DialogManager.getInstance().showFamilyInfoDialog("Agent");
		}
		else if(e.getSource() == rbFamDetails)
		{
			//Wrap the details string on 50 character length boundary's
			String[] input = currFam.getDetails().split(" ");
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
				"Details For ONC Family #" + currFam.getONCNum(), JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else if(e.getSource() == btnAssignONCNum)
		{
			autoassignONCNum();
		}
		else if(e.getSource() == rbDeliveryHistory)
		{
			DialogManager.getInstance().showHistoryDialog(rbDeliveryHistory.getActionCommand());
		}
		else if(e.getSource() == rbMeals)
		{
			if(rbMeals.getActionCommand().equals("Add Meal"))
				DialogManager.getInstance().showFamilyInfoDialog(rbMeals.getActionCommand());
			else
				DialogManager.getInstance().showHistoryDialog(rbMeals.getActionCommand());
		}
		else if(e.getSource() == rbAdults)
		{
			DialogManager.getInstance().showAdultDialog();
		}
		else if(e.getSource() == rbAltAddress)			 
		{	
			editAltAddress();
		}
		else if(e.getSource() == housenumTF && !bFamilyDataChanging && 
				!housenumTF.getText().equals(currFam.getHouseNum()) && GlobalVariables.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);
			
		}
		else if(e.getSource() == Street && !bFamilyDataChanging && 
								!Street.getText().equals(currFam.getStreet()) && GlobalVariables.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);	
		}
		else if(e.getSource() == City && !bFamilyDataChanging &&
								!City.getText().equals(currFam.getCity()) && GlobalVariables.isUserAdmin()) 
		{
			checkAndUpdateFamilyData(currFam);
			
		}
		else if(e.getSource() == ZipCode && !bFamilyDataChanging &&
				!ZipCode.getText().equals(currFam.getZipCode()) && GlobalVariables.isUserAdmin()) 
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
		else if(e.getSource() == rbTransportation)
		{
			DialogManager.getInstance().showFamilyInfoDialog("Transportation");
		}
		else if(e.getSource() == rbDirections)
		{
			DialogManager.getInstance().showDrivingDirections();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if(!e.getValueIsAdjusting() && e.getSource() == childTable.getSelectionModel() && !bChildTableDataChanging && 
				cDB.getNumberOfChildrenInFamily(currFam.getID()) > 0)
		{
			System.out.println(String.format("FamPanel.valueChanged Inside: valueAdjusting: %s", e.getValueIsAdjusting()));
			//Get new child selected by user and display their information
			currChild = ctAL.get(childTable.getSelectedRow());
			refreshODBWishListHighlights(currFam, currChild);
			refreshPriorHistoryButton(currFam, currChild);
			
			fireEntitySelected(this, EntityType.CHILD, currFam, currChild);
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
			bagPanel.add(lblBagMssg);
			bagPanel.add(bagsPackagedCB);
			
			JPanel liPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblLIMssg = new JLabel("Select the number of large items");
			largeItemCB = new JComboBox(selections);
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
			{
				LogDialog.add("FamilyPanel: UPDATED_FAMILY ONC# " + updatedFam.getONCNum(), "M");
				display(updatedFam, currChild); //Don't change the displayed child		
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY"))
		{
//			System.out.println(String.format("FamilyPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			ONCFamily updatedFam = (ONCFamily) dbe.getObject();
			
			//If no current family being displayed (null) display the added family
			if(currFam == null)
			{
				LogDialog.add("FamilyPanel: ADDED_FAMILY ONC# " + updatedFam.getONCNum(), "M");
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
				LogDialog.add("FamilyPanel: UPDATED_CHILD ONC# " + currFam.getONCNum() + ", Child: " + updatedChild.getChildFirstName(), "M");
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
					childTableModel.fireTableRowsUpdated(row, row);
				
//					if(GlobalVariables.isUserAdmin())
//					{
//						childTableModel.setValueAt(updatedChild.getChildFirstName(), row, 0);
//						childTableModel.setValueAt(updatedChild.getChildLastName(), row, 1);
//					}
//				
//					childTableModel.setValueAt(updatedChild.getChildDOBString("MM/dd/yy"), row, 2);
//					childTableModel.setValueAt(updatedChild.getChildGender(), row, 3);
				
					bChildTableDataChanging = false;
					
					//If current child is being displayed on child panel, refresh the
					//prior year history button and refresh the child panel
					if(childTable.getSelectedRow() == row)
					{
						currChild = updatedChild;
						refreshODBWishListHighlights(currFam, updatedChild);
						refreshPriorHistoryButton(currFam, updatedChild);
					}
				}
			}
		}
		else if(dbe.getType().equals("ADDED_CHILD")) 
		{
			ONCChild changeChild = (ONCChild) dbe.getObject();
				
			if(currFam != null && currFam.getID() == changeChild.getFamID())	//Child was added to the displayed family
			{
				if(currFam != null)
				{
					LogDialog.add("FamilyPanel: ADDED_CHILD ONC# " + currFam.getONCNum() + ", Child: " + changeChild.getChildFirstName(), "M");
					display(currFam, null);	
				}
			}
		}
		else if(dbe.getType().equals("DELETED_CHILD")) 
		{
			ONCChild changeChild = (ONCChild) dbe.getObject();
				
			if(currFam.getID() == changeChild.getFamID())	//Child was added to the displayed family
			{
				if(currFam != null)
				{
					LogDialog.add("FamilyPanel: DELETED_CHILD ONC# " + currFam.getONCNum() + ", Child: " + changeChild.getChildFirstName(), "M");
					display(currFam, null);
					
					//need to tell other gui's that if new child is selected (e.g. Child Panel)
					if(currChild != null)
						fireEntitySelected(this, EntityType.CHILD, currFam, currChild);
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
		{
			ONCDelivery updatedDelivery = (ONCDelivery) dbe.getObject();
				
			//If updated delivery belongs to family being displayed, re-display it
			if(currFam.getID() == updatedDelivery.getFamID())
			{
				if(currFam != null)
				{
					LogDialog.add("FamilyPanel: ADDED_DELIVERY ONC# " + currFam.getONCNum() + ", Delivery Note: " + updatedDelivery.getdNotes(), "M");
					display(currFam, null);
				}
			}
		}
		else if(dbe.getType().equals("UPDATED_SERVED_COUNTS"))
		{
//			System.out.println(String.format("StatusPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			LogDialog.add("FamilyPanel: UPDATED_SERVED_COUNTS", "M");
			DataChange servedCountsChange = (DataChange) dbe.getObject();
			int[] changes = {servedCountsChange.getOldData(), servedCountsChange.getNewData()};
			updateDBStatus(changes);
		}
		else if(dbe.getType().equals("ADDED_ADULT") || dbe.getType().equals("DELETED_ADULT"))
		{
			//may have added or deleted from current family, if so need to redisplay
			ONCAdult changedAdult = (ONCAdult) dbe.getObject();
			if(currFam != null && currFam.getID() == changedAdult.getFamID())
			{
				//update the icon in the icon bar
				checkForAdultsInFamily();
			}
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getSource() != this && (tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.WISH))
		{
			ONCFamily selFam = (ONCFamily) tse.getObject1();
			ONCChild selChild = (ONCChild) tse.getObject2();
		
			//is from nav, update and display new family
			if(tse.getSource() == nav && selFam.getID() != currFam.getID())
			{
				LogDialog.add("FamilyPanel: " + tse.getType() + " from Nav ONC# " + selFam.getONCNum(), "M");
				update();
				display(selFam, selChild);	
			}
			//not nav and isn't current family displayed
			else if(tse.getSource() != nav && selFam.getID() != currFam.getID())
			{
				int rtn;
				if((rtn=fDB.searchForFamilyIndexByID(selFam.getID())) >= 0)
				{
					LogDialog.add("FamilyPanel: " + tse.getType() + " ONC# " + selFam.getONCNum(), "M");
					update();
					nav.setIndex(rtn);
					display(selFam, selChild);
					
					nav.setStoplightEntity(selFam);
				}
			}
			//not nav and is current family displayed, but not current child selected
			else if(tse.getSource() != nav && selFam.getID() == currFam.getID() &&
					currChild !=null && selChild != null && selChild.getID() != currChild.getID())
			{
				int rtn;
				if((rtn=fDB.searchForFamilyIndexByID(selFam.getID())) >= 0)
				{
					LogDialog.add("FamilyPanel: " + tse.getType() + " ONC# " + selFam.getONCNum(), "M");
					update();
					nav.setIndex(rtn);
					display(selFam, selChild);
					
					nav.setStoplightEntity(selFam);
				}
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.WISH);
	}
    
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD);
	}
	
	class ChildTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the child table
		 */
		
		private static final long serialVersionUID = 1L;
		private static final int FIRST_NAME_COL = 0;
		private static final int LAST_NAME_COL = 1;
		private static final int DOB_COL = 2;
		private static final int GENDER_COL = 3;
		
		private String[] columnNames = {"First Name", "Last Name", "DoB", "Gend"};
		
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return ctAL == null ? 0 : ctAL.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCChild child = ctAL.get(row);

        	if(col == FIRST_NAME_COL)
        		return GlobalVariables.isUserAdmin() ? child.getChildFirstName() : "Child " + Integer.toString(row+1);
        	else if(col == LAST_NAME_COL)
        		return GlobalVariables.isUserAdmin() ? child.getChildLastName() : "";
        	else if(col == DOB_COL)
        		return child.getChildDOBString("M/d/yy");
        	else if(col == GENDER_COL)
        		return child.getChildGender();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
    }
}

