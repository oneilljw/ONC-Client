package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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

import com.google.gson.Gson;

public class FamilyPanel extends ONCPanel implements ActionListener, ListSelectionListener, DatabaseListener,
													EntitySelector, EntitySelectionListener
{
	/**
	 * This class provides the blueprint for the main panel in the ONC application. It contains a number of 
	 * GUI elements and a child sub panel
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final int[] PHONECODE_MASK = {3,12,48};
	
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
	
	private static final int FAMILY_NOTE_GRAY_ICON_INDEX = 47;
	private static final int GIFT_PICKUP_ICON_INDEX = 57;
	private static final int HOME_DELIVERY_ICON_INDEX = 58;
	private static final int HOME_DELIVERY_SIGNATURE_INDEX = 59;
	
	private JFrame parentFrame;
	
	//data base references
	private DatabaseManager dbMgr;
    private FamilyDB fDB;
	private ChildDB cDB;
	private MealDB mealDB;
	private AdultDB adultDB;
	private FamilyHistoryDB familyHistoryDB;
	private RegionDB regions;
	private UserDB userDB;
	private GroupDB groupDB;
	private NoteDB noteDB;
	private DNSCodeDB dnsCodeDB;
	
	private ONCFamily currFam;	//The panel needs to know which family is being displayed
	private FamilyHistory currFamHistory; //The panel needs to know the family's history being displayed
	private ONCChild currChild;	//The panel needs to know which child is being displayed
	
	private JPanel p1, p2, p3;
	private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
	
	private ONCProgressNavPanel nav;	//public to allow adding/removal of Entity Selection Listeners
	private JTextPane oncNotesPane, oncDIPane, wishlistPane;
	private JScrollPane wishlistScrollPane;
	private JTextPane primaryPhonePane, altPhonePane;
	private JButton btnAssignONCNum;
	private JComboBox<DNSCode> dnsCodeCB;
	private DefaultComboBoxModel<DNSCode> dnsCodeCBM;
	private JTextField HOHFirstName, HOHLastName, emailTF, altPhone2TF;
	private JTextField housenumTF, Street, Unit, City, ZipCode;
	private JLabel lblONCNum, lblRefNum,lblSchool, lblChangedBy;
//	private JLabel lblBatchNum, lblNumBags;
	private JRadioButton rbGiftStatusHistory, rbAltAddress, rbPriorHistory, rbAgentInfo;
	private JRadioButton rbCheckAllPhones, rbFamDetails, rbDirections;
	private JRadioButton rbNotGiftCardOnly, rbGiftCardOnly, rbAdults, rbConfirmation;
	private JRadioButton[] rbFamilyNote, rbMeals, rbTransportation, rbDistribution;
	private JComboBox<String> languageCB;
	private JComboBox<FamilyStatus> statusCB;
	private JComboBox<FamilyGiftStatus> giftStatusCB;
//	private ComboItem[] famStatus;
	public  JTable childTable;
	private ChildTableModel childTableModel;
	private ArrayList<ONCChild> ctAL; //List holds children object references being displayed in table
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	private boolean bChildTableDataChanging = true;	//Flag indicating that Child Table data is changing
	
	//An instance of the private subclass of the default highlight painter
	private static DefaultHighlighter.DefaultHighlightPainter highlightPainter;
	
	//sub-panels of the family panel
	private ChildPanel oncChildPanel;
	private GiftPanel[] wishPanelList;
	
	//Temporary for debug
	SimpleDateFormat sdf;
	
	public FamilyPanel(JFrame pf)
	{
		super(pf);
		parentFrame = pf;
		//register database listeners for updates
		dbMgr = DatabaseManager.getInstance();
		fDB = FamilyDB.getInstance();
		cDB = ChildDB.getInstance();
		mealDB = MealDB.getInstance();
		adultDB = AdultDB.getInstance();
		familyHistoryDB = FamilyHistoryDB.getInstance();
		regions = RegionDB.getInstance();
		userDB = UserDB.getInstance();
		groupDB = GroupDB.getInstance();
		noteDB = NoteDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		if(gvs != null)
			gvs.addDatabaseListener(this);
		if(fDB != null)
			fDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(mealDB != null)
			mealDB.addDatabaseListener(this);
		if(adultDB != null)
			adultDB.addDatabaseListener(this);
		if(familyHistoryDB != null)
			familyHistoryDB.addDatabaseListener(this);
		if(userDB != null)
			userDB.addDatabaseListener(this);	//font preference updates
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		if(noteDB != null)
			noteDB.addDatabaseListener(this);
		if(dnsCodeDB != null)
			dnsCodeDB.addDatabaseListener(this);
		
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
		nav = new ONCProgressNavPanel(pf, fDB);
		nav.setMssg("Our Neighbor's Child Families");
	    nav.setCount1("Families Served Gifts: " + Integer.toString(0));
	    nav.setCount2("Children Served Gifts: " + Integer.toString(0));

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
/*        
        lblBatchNum = new JLabel();
        lblBatchNum.setPreferredSize(new Dimension(64, 52));
        lblBatchNum.setToolTipText("Indicates family intake grouping");
        lblBatchNum.setBorder(BorderFactory.createTitledBorder("Batch #"));
        lblBatchNum.setHorizontalAlignment(JLabel.CENTER);
*/       
        //Get a catalog for type=selection
        dnsCodeCBM = new DefaultComboBoxModel<DNSCode>();
        dnsCodeCBM.addElement(new DNSCode());
        dnsCodeCB = new JComboBox<DNSCode>();
        dnsCodeCB.setModel(dnsCodeCBM);
        dnsCodeCB.setToolTipText("Do not serve family code: e.g, SA= Salvation Army");
        dnsCodeCB.setBorder(BorderFactory.createTitledBorder("DNS Code"));
        dnsCodeCB.setEnabled(false);
        dnsCodeCB.addActionListener(this);

        HOHFirstName = new JTextField(8);
        HOHFirstName.setBorder(BorderFactory.createTitledBorder("First Name"));
        HOHFirstName.setEditable(false);
        HOHFirstName.addActionListener(fdcListener);
        
        HOHLastName = new JTextField(11);
        HOHLastName.setBorder(BorderFactory.createTitledBorder("Last Name"));
        HOHLastName.setEditable(false);
        HOHLastName.addActionListener(fdcListener);
  
        statusCB = new JComboBox<FamilyStatus>(FamilyStatus.getSearchList());
        statusCB.setPreferredSize(new Dimension(152, 52));
        statusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
        statusCB.setEnabled(false);
        statusCB.addActionListener(this);
/*        
        lblNumBags = new JLabel("0", JLabel.RIGHT);
        lblNumBags.setPreferredSize(new Dimension(48, 52));
        lblNumBags.setBorder(BorderFactory.createTitledBorder("Bags"));
*/             
        primaryPhonePane = new JTextPane();
        JScrollPane homePhoneScrollPane = new JScrollPane(primaryPhonePane);
        homePhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        homePhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Primary Phone"));
        primaryPhonePane.setEditable(false);
        
        altPhonePane = new JTextPane();
        JScrollPane otherPhoneScrollPane = new JScrollPane(altPhonePane);
        otherPhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        otherPhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Alt Phone"));
        altPhonePane.setEditable(false);
        
        altPhone2TF = new JTextField();
        altPhone2TF.setPreferredSize(new Dimension(128, 44));
        altPhone2TF.setBorder(BorderFactory.createTitledBorder("2nd Alt Phone"));
        altPhonePane.setEditable(false);
        altPhone2TF.addActionListener(fdcListener);
      
        emailTF = new JTextField(18);
        emailTF.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailTF.setEditable(false);
        emailTF.addActionListener(fdcListener);
        
        String[] languages = {"?", "English", "Spanish", "Arabic", "Korean", "Vietnamese", "Other"};
        languageCB = new JComboBox<String>(languages);
        languageCB.setToolTipText("Select the primary language spoken by the family");
        languageCB.setPreferredSize(new Dimension(140, 48));
        languageCB.setBorder(BorderFactory.createTitledBorder("Language"));
        languageCB.setEnabled(false);
        languageCB.addActionListener(this);
        
        giftStatusCB = new JComboBox<FamilyGiftStatus>(FamilyGiftStatus.getSearchList());
        giftStatusCB.setBorder(BorderFactory.createTitledBorder("Gift Status"));
        giftStatusCB.setPreferredSize(new Dimension(132, 52));
        giftStatusCB.setEnabled(false);
        giftStatusCB.addActionListener(this);

        housenumTF = new JTextField();
        housenumTF.setPreferredSize(new Dimension(56, 44));
        housenumTF.setBorder(BorderFactory.createTitledBorder("St #"));
        housenumTF.setEditable(false);
        housenumTF.addActionListener(this);
        
        Street = new JTextField();
        Street.setPreferredSize(new Dimension(192, 44));
        Street.setBorder(BorderFactory.createTitledBorder("Street"));
        Street.setEditable(false);
        Street.addActionListener(this);
        
        Unit = new JTextField();
        Unit.setPreferredSize(new Dimension(72, 44));
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
        
        lblSchool = new JLabel();
        lblSchool.setPreferredSize(new Dimension(120, 44));
        lblSchool.setBorder(BorderFactory.createTitledBorder("Elem School"));
        
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
        
        rbCheckAllPhones = new JRadioButton(gvs.getImageIcon(PHONE_ICON_INDEX));
        rbCheckAllPhones.setToolTipText("Check all phone numbers for family");
        rbCheckAllPhones.setEnabled(false);
        rbCheckAllPhones.addActionListener(this);
        
        rbAgentInfo = new JRadioButton(gvs.getImageIcon(AGENT_INFO_ICON_INDEX));
        rbAgentInfo.setToolTipText("Click for info on agent who referred family");
        rbAgentInfo.setEnabled(false);
        rbAgentInfo.addActionListener(this);
        
        
        String[] noteToolTip = {"No notes for family, click to edit", "Last note sent, not yet viewed",
        							"Last note read, no response", "Agent responded to last note",
        							"Response to last note is overdue"};
        rbFamilyNote = new JRadioButton[noteToolTip.length];
        for(int i=0; i< noteToolTip.length; i++)
        {
        		rbFamilyNote[i] = new JRadioButton(gvs.getImageIcon(FAMILY_NOTE_GRAY_ICON_INDEX + i));
            rbFamilyNote[i].setToolTipText(noteToolTip[i]);
            rbFamilyNote[i].setVisible(false);
            rbFamilyNote[i].addActionListener(this);
        }
       
        rbFamDetails = new JRadioButton(gvs.getImageIcon(FAMILY_DETAILS_ICON_INDEX));
        rbFamDetails.setToolTipText("Click for additional details for this family");
        rbFamDetails.setEnabled(false);
        rbFamDetails.addActionListener(this);
        
        int[] rbMealImageIndex = {NO_MEAL_ICON_INDEX, REQUESTED_MEAL_ICON_INDEX, REFERRED_MEAL_ICON_INDEX};
        rbMeals = new JRadioButton[rbMealImageIndex.length];
        for(int i=0; i<rbMeals.length; i++)
        {
        		rbMeals[i] = new JRadioButton(gvs.getImageIcon(rbMealImageIndex[i]));
        		rbMeals[i].setToolTipText("Click for family food assistance status");
        		rbMeals[i].setVisible(false);
        		rbMeals[i].addActionListener(this);
        }
       
        int[] rbTransImageIndex = {NO_TRANSPORTATION_ICON_INDEX, TRANSPORTATION_ICON_INDEX};
        rbTransportation = new JRadioButton[rbTransImageIndex.length];
        for(int i=0; i<rbTransportation.length; i++)
        {
        	rbTransportation[i] = new JRadioButton(gvs.getImageIcon(rbTransImageIndex[i]));
        	rbTransportation[i].setToolTipText("Click for family transportation status");
            rbTransportation[i].setVisible(false);
            rbTransportation[i].addActionListener(this);
        }
        
        int[] rbDistributionIndex = {GIFT_PICKUP_ICON_INDEX, HOME_DELIVERY_ICON_INDEX};
        rbDistribution = new JRadioButton[rbDistributionIndex.length];
        for(int i=0; i<rbDistributionIndex.length; i++)
        {
        	rbDistribution[i] = new JRadioButton(gvs.getImageIcon(rbDistributionIndex[i]));
        	rbDistribution[i].setToolTipText("Click for gift pickup or home delivery");
        	rbDistribution[i].setVisible(false);
        	rbDistribution[i].addActionListener(this);
        }
        
        rbConfirmation = new JRadioButton(gvs.getImageIcon(HOME_DELIVERY_SIGNATURE_INDEX));
        rbConfirmation.setToolTipText("Click to view delivery confirmation");
        rbConfirmation.setEnabled(false);
    	rbConfirmation.addActionListener(this);
       
        rbDirections = new JRadioButton(gvs.getImageIcon( GOOGLE_MAP_ICON_INDEX));
        rbDirections.setToolTipText("Click for directions to family address");
        rbDirections.setEnabled(false);
        rbDirections.addActionListener(this);
        
        rbNotGiftCardOnly = new JRadioButton(gvs.getImageIcon(NOT_GIFT_CARD_ONLY_ICON_INDEX));
        rbNotGiftCardOnly.setToolTipText("Family is not a Gift Card Only Family");
//        rbNotGiftCardOnly.setEnabled(false);
        rbNotGiftCardOnly.setVisible(true);
//        rbNotGiftCardOnly.addActionListener(this);
        
        rbGiftCardOnly = new JRadioButton(gvs.getImageIcon(GIFT_CARD_ONLY_ICON_INDEX));
        rbGiftCardOnly.setToolTipText("Family is a Gift Card Only Family");
//        rbGiftCardOnly.setEnabled(false);
        rbGiftCardOnly.setVisible(false);
//        rbGiftCardOnly.addActionListener(this);
        
        rbAdults = new JRadioButton(gvs.getImageIcon(ADULT_ICON_INDEX));
        rbAdults.setToolTipText("Manage Adults in Family");
        rbAdults.setEnabled(false);
        rbAdults.addActionListener(this);

        rbGiftStatusHistory = new JRadioButton(gvs.getImageIcon(14));
        rbGiftStatusHistory.setActionCommand("Family Status History");
        rbGiftStatusHistory.setToolTipText("Click to see family & gift status history");
        rbGiftStatusHistory.setEnabled(false);
        rbGiftStatusHistory.addActionListener(this);
        
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
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
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
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
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
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        oncDIPane.setParagraphAttributes(attribs,true);             
	   	oncDIPane.setEditable(false);
	   	
	    //Create the ONC Delivery Instructions scroll pane and add the ONC Note text pane to it.
        JScrollPane oncDIscrollPane = new JScrollPane(oncDIPane);
        oncDIscrollPane.setBorder(BorderFactory.createTitledBorder("Delivery Instructions"));
            
        //Create the Child Panel
        oncChildPanel = new ChildPanel();
        eeManager.registerEntitySelectionListener(oncChildPanel);
        
        //create the wish panels
        JPanel childwishespanel = new JPanel(new GridLayout(1,3));
        wishPanelList = new GiftPanel[NUMBER_OF_WISHES_PER_CHILD];
        for(int wp=0; wp < wishPanelList.length; wp++)
        {
        	wishPanelList[wp] = new GiftPanel(wp);
        	childwishespanel.add(wishPanelList[wp]);
        	
        	//register the entity selection listeners
            eeManager.registerEntitySelectionListener(wishPanelList[wp]);
        }
        
        //temporary for debug
        sdf = new SimpleDateFormat("HH:mm:ss:SSS"); 
        
        //Add components to the panels
        p1.add(lblONCNum);
        p1.add(lblRefNum);
//      p1.add(lblBatchNum);
        p1.add(dnsCodeCB);
        p1.add(HOHFirstName);
        p1.add(HOHLastName);
        p1.add(statusCB);
//      p1.add(lblNumBags);
        p1.add(giftStatusCB);
        
        p2.add(homePhoneScrollPane);
        p2.add(otherPhoneScrollPane);
        p2.add(altPhone2TF);
        p2.add(emailTF);
		p2.add(languageCB);
		
		
        p3.add(housenumTF);
        p3.add(Street);
        p3.add(Unit);
        p3.add(City);
        p3.add(ZipCode);
        p3.add(lblSchool);
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
        iconBar.add(rbCheckAllPhones);
        iconBar.add(rbAltAddress);
        iconBar.add(rbFamDetails);
        iconBar.add(rbAgentInfo);
        
        for(int i=0; i<rbFamilyNote.length; i++)
        		iconBar.add(rbFamilyNote[i]);
        
        for(int i=0; i< rbMeals.length; i++)
        		iconBar.add(rbMeals[i]);
        
        for(int i=0; i< rbTransportation.length; i++)
    		iconBar.add(rbTransportation[i]);
        
        for(int i=0; i< rbDistribution.length; i++)
    		iconBar.add(rbDistribution[i]);
        
        iconBar.add(rbAdults);
        iconBar.add(rbNotGiftCardOnly);
        iconBar.add(rbGiftCardOnly);
        iconBar.add(rbDirections);
        iconBar.add(rbGiftStatusHistory);
        iconBar.add(rbConfirmation);
              
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
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
		{
			dnsCodeCB.setEnabled(tf);
			HOHFirstName.setEditable(tf);
			HOHLastName.setEditable(tf);;
			statusCB.setEnabled(tf);
			giftStatusCB.setEnabled(tf);
			primaryPhonePane.setEditable(tf);
			altPhonePane.setEditable(tf);
			altPhone2TF.setEditable(tf);
			emailTF.setEditable(tf);
			languageCB.setEnabled(tf);
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

	void setTextPaneFontSize(Integer fontSize)
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, fontSize);
        StyleConstants.setSpaceBelow(attribs, 3);
        wishlistPane.setParagraphAttributes(attribs, true);
        oncNotesPane.setParagraphAttributes(attribs, true);
        oncDIPane.setParagraphAttributes(attribs, true);
        
        if(currFam != null)
        	display(currFam, currFamHistory, currChild);
	}
	
	void setRestrictedEnabledButtons(boolean tf)
	{	 
		rbCheckAllPhones.setEnabled(tf);
		rbAltAddress.setEnabled(tf);
	}
	
	void setEnabledButtons(boolean tf) 
	{ 
		rbAgentInfo.setEnabled(tf);
		rbGiftStatusHistory.setEnabled(tf); 
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
	void display(ONCFamily fam, FamilyHistory fh, ONCChild child)
	{
		int cn = 0; //allows child number in family to be passed with only one search
		if(fam == null)
		{
			//error has occurred, display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(),
					"ERROR - NULL Family, can't display, contact the ONC IT Director",
					"ERROR - NULL Family",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			return;
		}
		else
		{
			currFam = fam;
			currFamHistory = fh != null ? fh : familyHistoryDB.getLastFamilyHistory(fam.getID());
			
			if(currFamHistory == null)
				return;	//can't display a family until the family history arrives as well
			else if(child == null)
    		{
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
		}
		
		p1.setBackground(pBkColor);
		p2.setBackground(pBkColor);
		p3.setBackground(pBkColor);
		
		rbDirections.setEnabled(true);
//		rbNotGiftCardOnly.setEnabled(true);
		rbNotGiftCardOnly.setVisible(!currFam.isGiftCardOnly());
//		rbGiftCardOnly.setEnabled(true);
		rbGiftCardOnly.setVisible(currFam.isGiftCardOnly());
		rbAdults.setEnabled(true);
		rbConfirmation.setEnabled(currFam.hasDeliveryImage());
		
		lblONCNum.setText(currFam.getONCNum());
		lblONCNum.setToolTipText("Family Database ID= " + Integer.toString(currFam.getID()));
		lblRefNum.setText(currFam.getReferenceNum());
//		lblBatchNum.setText(currFam.getBatchNum());
		
		//get DNS Code
		if(currFamHistory.getDNSCode() == -1)
			dnsCodeCB.setSelectedIndex(0);
		else
			dnsCodeCB.setSelectedItem(dnsCodeDB.getDNSCode(currFamHistory.getDNSCode()));
		
		dnsCodeCB.setToolTipText(String.format("DNS Code ID= %d", currFamHistory.getDNSCode()));
		
		checkForDNSorGiftCardOnly();
		
		statusCB.setSelectedItem(currFamHistory.getFamilyStatus());
//		lblNumBags.setText(Integer.toString(currFam.getNumOfBags()));
		
		giftStatusCB.setSelectedItem(currFamHistory.getGiftStatus());
		languageCB.setSelectedItem((String)currFam.getLanguage());
		lblChangedBy.setText(currFam.getChangedBy());
		lblSchool.setText(regions.getSchoolName(currFam.getSchoolCode()));
		
		String groupName = "Error: No Group Found";
		if(currFam.getGroupID() > -1)
		{
			ONCGroup group = groupDB.getGroupByID(currFam.getGroupID());
			if(group != null)
				groupName = group.getName();	
		}
		lblSchool.setToolTipText("Referring Group: " + groupName);
		
		oncNotesPane.setText(currFam.getNotes());
		oncNotesPane.setCaretPosition(0);
		oncDIPane.setText(currFam.getDeliveryInstructions());
		oncDIPane.setCaretPosition(0);
		
		//set family note button icon
		ONCNote lastFamNote = noteDB.getLastNoteForFamily(currFam.getID());
		int visibleNoteIndex = lastFamNote == null ? 0 : lastFamNote.getStatus(); 
		for(int i=0; i< rbFamilyNote.length; i++)
			rbFamilyNote[i].setVisible(visibleNoteIndex == i);
				
		//set meal button iconBar
		updateFamilyMeals(mealDB.getFamiliesCurrentMeal(currFam.getID()));
		
		//set transportation button icon
		rbTransportation[0].setVisible(currFam.getTransportation() == Transportation.No ||
									  currFam.getTransportation() == Transportation.TBD);
		rbTransportation[1].setVisible(currFam.getTransportation() == Transportation.Yes);
		
		if(currFam.getGiftDistribution() == GiftDistribution.Pickup)
		{
			rbDistribution[0].setVisible(true);
			rbDistribution[1].setVisible(false);
		}
		else if(currFam.getGiftDistribution() == GiftDistribution.Delivery)
		{
			rbDistribution[0].setVisible(false);
			rbDistribution[1].setVisible(true);
		}
		else
		{
			rbDistribution[0].setVisible(false);
			rbDistribution[1].setVisible(false);
		}
		
		addChildrenToTable(cn);	//update child table
		checkForAdultsInFamily(); //set the adults icon
		
		nav.setStoplightEntity(currFam);
		nav.btnNextSetEnabled(true);
		nav.btnPreviousSetEnabled(true);
		
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
		{	
			//show personal information for family since user is Administrator or higher		
			HOHFirstName.setText(currFam.getFirstName());
			HOHLastName.setText(currFam.getLastName());
			
			primaryPhonePane.setText(currFam.getHomePhone());
			primaryPhonePane.setCaretPosition(0);
			PhoneTipAndBackground primaryPhone = new PhoneTipAndBackground(0, currFam.getHomePhone().isEmpty());
			primaryPhonePane.setToolTipText(primaryPhone.getTip());
			primaryPhonePane.setBackground(primaryPhone.getPhoneBackground());
			
			altPhonePane.setText(currFam.getCellPhone());
			altPhonePane.setCaretPosition(0);
			PhoneTipAndBackground altPhone = new PhoneTipAndBackground(1, currFam.getCellPhone().isEmpty());
			altPhonePane.setToolTipText(altPhone.getTip());
			altPhonePane.setBackground(altPhone.getPhoneBackground());
			
			altPhone2TF.setText(currFam.getAlt2Phone());
			altPhone2TF.setCaretPosition(0);
			PhoneTipAndBackground alt2Phone = new PhoneTipAndBackground(2, currFam.getAlt2Phone().isEmpty());
			altPhone2TF.setToolTipText(alt2Phone.getTip());
			altPhone2TF.setBackground(alt2Phone.getPhoneBackground());
			
			emailTF.setText(currFam.getEmail());
			emailTF.setCaretPosition(0);
			
			housenumTF.setText(currFam.getHouseNum());
			Street.setText(currFam.getStreet());
			Street.setCaretPosition(0);
			Unit.setText(currFam.getUnit());
			Unit.setCaretPosition(0);
			City.setText(currFam.getCity());
			ZipCode.setText(currFam.getZipCode());
			if(currFam.getSubstituteDeliveryAddress() != null &&
				!currFam.getSubstituteDeliveryAddress().isEmpty())
				rbAltAddress.setIcon(gvs.getImageIcon(20));
			else
				rbAltAddress.setIcon(gvs.getImageIcon(19));
			
			wishlistPane.setText(currFam.getWishList());
			rbFamDetails.setEnabled(currFam.getDetails().length() > 1);
			
			//Test to see if an ONC number could be assigned. If so, make the auto assign button visible
			if(!lblONCNum.getText().equals("DEL") && !lblSchool.getText().equals("Y") &&
					  !lblSchool.getText().equals("Z") && !Character.isDigit(lblONCNum.getText().charAt(0)))	
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
				replace[0] = currFam.getWishList();
			
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
		MenuBar menuBar = MenuBar.getInstance();
		bChildTableDataChanging = true;		

		if(ctAL.size() > 0)
		{		
			childTableModel.fireTableDataChanged();
			childTable.setRowSelectionInterval(cn, cn);
	    	childTable.requestFocus();
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
				menuBar.setEnabledMarkorDeleteChildMenuItem(true);	//Enable Delete Child Menu Bar item
		}
		else
		{
			//family has no children, clear the child panel
			currChild = null;
			childTableModel.fireTableDataChanged();
			menuBar.setEnabledMarkorDeleteChildMenuItem(false);	//Disable Delete Child Menu Bar item
		}
		bChildTableDataChanging = false;
	}
	
	void checkForDNSorGiftCardOnly()
	{
		//determine what color to paint the background
		if(currFamHistory.getDNSCode() > -1)
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
		}
	}
	
	void updateFamilyMeals(ONCMeal changedMeal)
	{	
		//set the meal icon
		rbMeals[0].setVisible(changedMeal == null);
		rbMeals[1].setVisible(changedMeal != null && (changedMeal.getStatus() == MealStatus.Requested ||
												changedMeal.getStatus() == MealStatus.Assigned));
		rbMeals[2].setVisible(changedMeal != null && changedMeal.getStatus().compareTo(MealStatus.Referred) >= 0);	
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
		
		else if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)	//Show all data
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
//			Rectangle caretRectangle = wishlistPane.modelToView(caretPosition);
//			Rectangle viewRectangle = new Rectangle(0, caretRectangle.y -
//			        (wishlistScrollPane.getHeight() - caretRectangle.height) / 2,
//			        wishlistScrollPane.getWidth(), wishlistScrollPane.getHeight());
//			wishlistPane.scrollRectToVisible(viewRectangle);
			
//			Rectangle2D caretRectangle = wishlistPane.modelToView2D(caretPosition);
//			Rectangle2D viewRectangle = new Rectangle2D(0, caretRectangle.getY() -
//			        (wishlistScrollPane.getHeight() - caretRectangle.getHeight()) / 2,
//			        wishlistScrollPane.getWidth(), wishlistScrollPane.getHeight());
//			wishlistPane.scrollRectToVisible(viewRectangle);
			
			Rectangle2D caretRectangle = wishlistPane.modelToView2D(caretPosition);
			wishlistPane.scrollRectToVisible(caretRectangle.getBounds()); // Scroll to the rectangle provided by view
			wishlistPane.setCaretPosition(caretPosition); // Sets carat position to pos
			
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
		checkAndUpdateFamilyData(currFam, currFamHistory);
	}
	
	/***************************************************************************************************
	*Query family data input fields to get data and record changes. This method is called when
	*family data may have changed by either an external object or by an internal component event 
	*such as the enter key or combo box list selection change
	****************************************************************************************************/
	void checkAndUpdateFamilyData(ONCFamily family, FamilyHistory fh)
	{
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) < 0)	//must have administrative privilege to make changes to families
			return;

		//make a copy of the current family object to create the request
		ONCFamily fam = new ONCFamily(family);
		FamilyHistory addFHreq = new FamilyHistory(fh);
		
		int cf = 0;	//used to indicate if a change is detected to a GUI field
		int cfh = 0;	//used to indicate if a change to a family history GUI field
		
		DNSCode selCode = (DNSCode) dnsCodeCB.getSelectedItem();
		if(selCode.getID() != fh.getDNSCode()) //DNS code changed
		{
			//Save the new DNS text field and mark that a field has changed
			addFHreq.setDNSCode(selCode.getID());
			cfh = 3;
		}
		
		if(!HOHFirstName.getText().equals(fam.getFirstName())) {fam.setHOHFirstName(HOHFirstName.getText()); cf = 4;}
		if(!HOHLastName.getText().equals(fam.getLastName())) {fam.setHOHLastName(HOHLastName.getText()); cf = 5;}
//		if(Integer.parseInt(lblNumBags.getText()) != fam.getNumOfBags()) {fam.setNumOfBags(Integer.parseInt(lblNumBags.getText())); cf = 20;}
		if(!primaryPhonePane.getText().equals(fam.getHomePhone())) {fam.setHomePhone(primaryPhonePane.getText()); cf = 6;}
		if(!altPhonePane.getText().equals(fam.getCellPhone())) {fam.setOtherPhon(altPhonePane.getText()); cf = 7;}
		if(!altPhone2TF.getText().equals(fam.getAlt2Phone())) {fam.setAlt2Phone(altPhone2TF.getText()); cf = 20;}
		if(!emailTF.getText().equals(fam.getEmail())) {fam.setFamilyEmail(emailTF.getText()); cf = 8;}
		if(!languageCB.getSelectedItem().toString().equals(fam.getLanguage())){fam.setLanguage(languageCB.getSelectedItem().toString());cf = 9;}
		if(!housenumTF.getText().equals(fam.getHouseNum())) {fam.setHouseNum(housenumTF.getText()); cf = 10;}
		if(!Street.getText().equals(fam.getStreet())) {fam.setStreet(Street.getText()); cf = 11;}
		if(!Unit.getText().equals(fam.getUnit())) {fam.setUnitNum(Unit.getText()); cf = 12;}
		if(!City.getText().equals(fam.getCity())) {fam.setCity(City.getText()); cf = 13;}
		if(!ZipCode.getText().equals(fam.getZipCode())) {fam.setZipCode(ZipCode.getText()); cf = 14;}
		if(statusCB.getSelectedItem() != addFHreq.getFamilyStatus()) {addFHreq.setFamilyStatus( (FamilyStatus) statusCB.getSelectedItem()); cfh = 15;}
		if(giftStatusCB.getSelectedItem() != addFHreq.getGiftStatus()) {addFHreq.setFamilyGiftStatus( (FamilyGiftStatus) giftStatusCB.getSelectedItem()); cfh = 16;}
		if(!wishlistPane.getText().equals(fam.getWishList())) {fam.setWishList(wishlistPane.getText()); cf = 17;}
		if(!oncNotesPane.getText().equals(fam.getNotes())) {fam.setNotes(oncNotesPane.getText()); cf = 18;}
		if(!oncDIPane.getText().equals(fam.getDeliveryInstructions())) {fam.setDeliveryInstructions(oncDIPane.getText()); cf = 19;}
		
		if(cf > 0)
		{	
			fam.setChangedBy(userDB.getUserLNFI());
			lblChangedBy.setText(fam.getChangedBy());	//Set the changed by field to current user
			
			String response = fDB.update(this, fam);
			if(response.startsWith("UPDATED_FAMILY"))
			{
				//family id will not change from update request, get updated family
				//from the data base and display
				ONCFamily updatedFamily = fDB.getFamily(fam.getID());
				display(updatedFamily, currFamHistory, currChild);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), "ONC Server Error: " + response +
						", try again later","Family Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
		
		if(cfh > 0)
		{	
			addFHreq.setChangedBy(userDB.getUserLNFI());
			lblChangedBy.setText(addFHreq.getChangedBy());	//Set the changed by field to current user
			
			FamilyHistory addedFamHist = familyHistoryDB.add(this, addFHreq);
			if(addedFamHist != null)
			{
				//family will not change from update request, display updated family history
				display(currFam, addedFamHist, currChild);
				setCounts();
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), "ONC Server Error: " + addedFamHist +
						", try again later","Family History Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
	}

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
	
	void setCounts()
    {
		int[] dbCounts = fDB.getServedFamilyAndChildCount();
    	nav.setCount1("Families Served Gifts: " + Integer.toString((dbCounts[0])));
    	nav.setCount2("Children Served Gifts: " + Integer.toString((dbCounts[1])));
    }
	
    /*********************************************************************************************
     * This method updates the family panel when one or more families are loaded/added/imported
     *********************************************************************************************/
    void onInitialFamilyDataLoaded()
    {
    	nav.navSetEnabled(true);
    	
		setEnabledButtons(true);
		setEditableGUIFields(true);
		
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			setRestrictedEnabledButtons(true);
    }
    
    @SuppressWarnings("unchecked")
	void updateDNSCodeCB()
	{
		bFamilyDataChanging = true;
		
		dnsCodeCBM.removeAllElements();	//Clear the combo box selection list

		dnsCodeCBM.addElement(new DNSCode());
		
		for(DNSCode code: (List<DNSCode>) dnsCodeDB.getList())	//Add new list elements
			dnsCodeCBM.addElement(code);
			
		//set the proper selection in the updated combo box
		if(currFam != null && currFamHistory.getDNSCode() > -1) 
			dnsCodeCB.setSelectedItem(dnsCodeDB.getDNSCode(currFamHistory.getDNSCode()));
		else
			dnsCodeCB.setSelectedIndex(0);
	
		bFamilyDataChanging = false;
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
		String ly = Integer.toString(gvs.getCurrentSeason()-1);
		String py = Integer.toString(gvs.getCurrentSeason()-2);
		
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
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
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
	
//	void setEnabledAssignedDeliveryStatus(boolean tf) { delStatus[DELIVERY_STATUS_ASSIGNED].setEnabled(tf); }
		
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
			display(updatedFamily, currFamHistory, currChild);
		}
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), "ONC Server denied family auto ONC Number asssingment," +
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
		else if(e.getSource() == rbCheckAllPhones)
		{
			String response = fDB.getFamilyPhoneInfo(this, currFam);
			if(response != null && response.startsWith("FAMILY_PHONE_INFO"))
			{	
				Gson gson = new Gson();
				FamilyPhoneInfo fpi = gson.fromJson(response.substring(17), FamilyPhoneInfo.class);
			
			
				PhoneInfoDialog phoneInfoDlg = new PhoneInfoDialog(GlobalVariablesDB.getFrame(), fpi);
				phoneInfoDlg.setLocationRelativeTo(rbFamDetails);
				phoneInfoDlg.setVisible(true);
			}
			else
			{
				StringBuilder buff = new StringBuilder("");
				//create the AllPhoneNumber data
    			if(currFam != null)
    			{	
    				if(currFam.getHomePhone().length() > 9)
    					buff.append("Home Phone: " + currFam.getHomePhone());
    				if(currFam.getCellPhone().length() > 9)	//Ensure it's a valid 10 digit phone number at minimum
    					buff.append("\nAlternate Phone: " + currFam.getCellPhone());
    				if(currFam.getAlt2Phone().length() > 9)
    					buff.append("\n2nd Alternate Phone: " + currFam.getAlt2Phone());
    			}
				JOptionPane.showMessageDialog(parentFrame, buff.toString(), currFam.getClientFamily() + " family phone #'s",
						JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
			}
		}
		else if(e.getSource() == rbAgentInfo)
		{   
			Point currLocation = this.getLocation();
			currLocation.translate(30, 60);
			
			if(currFam != null)
			{
				ONCUser famAgent = userDB.getUser(currFam.getAgentID());
				
				if(famAgent != null)
				{
					fireEntitySelected(this, EntityType.AGENT, famAgent, null);
					DialogManager.getInstance().showEntityDialog("Edit Users", currLocation );
				}
			}
		}
		else if(e.getSource() == rbFamilyNote[0] || e.getSource() == rbFamilyNote[1] ||
				e.getSource() == rbFamilyNote[2] || e.getSource() == rbFamilyNote[3] ||
				e.getSource() == rbFamilyNote[4])
		{   
			if(currFam != null)
				DialogManager.getInstance().showEntityDialog("Edit Notes", new Point(40, 80));	
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
		else if(e.getSource() == rbGiftStatusHistory)
		{
			DialogManager.getInstance().showHistoryDialog(rbGiftStatusHistory.getActionCommand());
		}
		else if(e.getSource() == rbMeals[0])
			DialogManager.getInstance().showFamilyInfoDialog("Add Meal");
		else if(e.getSource() == rbMeals[1])
			DialogManager.getInstance().showHistoryDialog("Meal History");
		else if(e.getSource() == rbMeals[2])
			DialogManager.getInstance().showHistoryDialog("Meal History");
		else if(e.getSource() == rbAdults)
		{
			DialogManager.getInstance().showAdultDialog();
		}
		else if(e.getSource() == rbAltAddress)			 
		{	
			editAltAddress();
		}
		else if(e.getSource() == housenumTF && !bFamilyDataChanging && 
				!housenumTF.getText().equals(currFam.getHouseNum()) && 
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
		{
			checkAndUpdateFamilyData(currFam, currFamHistory);
			
		}
		else if(e.getSource() == Street && !bFamilyDataChanging && 
								!Street.getText().equals(currFam.getStreet()) &&
								userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
		{
			checkAndUpdateFamilyData(currFam, currFamHistory);
			
		}
		else if(e.getSource() == City && !bFamilyDataChanging &&
								!City.getText().equals(currFam.getCity()) && 
								userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
		{
			checkAndUpdateFamilyData(currFam, currFamHistory);
		}
		else if(e.getSource() == ZipCode && !bFamilyDataChanging &&
				!ZipCode.getText().equals(currFam.getZipCode()) &&
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
		{
			checkAndUpdateFamilyData(currFam, currFamHistory);	
		}
		else if(e.getSource() == languageCB && !bFamilyDataChanging &&
				!((String) languageCB.getSelectedItem()).equals(currFam.getLanguage()))
		{	
			checkAndUpdateFamilyData(currFam, currFamHistory);	
		}
		else if(e.getSource() == dnsCodeCB && !bFamilyDataChanging &&
						((DNSCode) dnsCodeCB.getSelectedItem()).getID() != currFamHistory.getDNSCode())
		{	
			checkAndUpdateFamilyData(currFam, currFamHistory);
		}
		else if(e.getSource() == statusCB && !bFamilyDataChanging &&
									statusCB.getSelectedItem() != currFamHistory.getFamilyStatus())
		{
			checkAndUpdateFamilyData(currFam, currFamHistory);
		}
		else if(e.getSource() == giftStatusCB && !bFamilyDataChanging &&
				giftStatusCB.getSelectedItem() != currFamHistory.getGiftStatus())
		{
			//If family gift status is changing to PACKAGED, solicit the number of bags. Else, the 
			//number of bags must be zero
			if(giftStatusCB.getSelectedItem() == FamilyGiftStatus.Packaged)
			{
				FamilyBagDialog fbDlg = new FamilyBagDialog(parentFrame);
				fbDlg.setVisible(true);

//				lblNumBags.setText(Integer.toString(fbDlg.getNumOfBags()));
				currFam.setNumOfBags(fbDlg.getNumOfBags());
				currFam.setNumOfLargeItems(fbDlg.getNumOfLargeItems());
			}
			else
			{
//				lblNumBags.setText("0");
				currFam.setNumOfBags(0);
				currFam.setNumOfLargeItems(0);
			}

			checkAndUpdateFamilyData(currFam, currFamHistory);
		}
		else if(e.getSource() == rbTransportation[0] || e.getSource() == rbTransportation[1])
		{
			DialogManager.getInstance().showFamilyInfoDialog("Transportation");
		}
		else if(e.getSource() == rbDistribution[0] || e.getSource() == rbDistribution[1])
		{
			DialogManager.getInstance().showFamilyInfoDialog("Distribution");
		}

		else if(e.getSource() == rbDirections)
		{
			DialogManager.getInstance().showDrivingDirections();
		}
		else if(e.getSource() == rbConfirmation)
		{
			PNGViewer viewer = new PNGViewer(parentFrame, currFam);
			viewer.setVisible(true);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if(!e.getValueIsAdjusting() && e.getSource() == childTable.getSelectionModel() && !bChildTableDataChanging && 
				cDB.getNumberOfChildrenInFamily(currFam.getID()) > 0)
		{
			//Get new child selected by user and display their information
			currChild = ctAL.get(childTable.getSelectedRow());
			refreshODBWishListHighlights(currFam, currChild);
			refreshPriorHistoryButton(currFam, currChild);
			
			fireEntitySelected(this, EntityType.CHILD, currFam, currFamHistory, currChild);
		}		
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{ 
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFam = (ONCFamily) dbe.getObject1();
			
			//If current family being displayed has changed, reshow it
			if(currFam.getID() == updatedFam.getID())
			{
				LogDialog.add(String.format("FamilyPanel: UPDATED_FAMILY ONC #%s at %s", 
						updatedFam.getONCNum(), sdf.format(new Date(System.currentTimeMillis()))), "M");
				display(updatedFam, currFamHistory, currChild); //Don't change the displayed child
				setCounts();
				
				//in case the user is receiving gifts with the bar code scanner, attempt
				//to set focus to the scanner
				DialogManager.getInstance().receiveGiftBarcodeRequestFocus();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY"))
		{
			ONCFamily addedFam = (ONCFamily) dbe.getObject1();
			
			//If no current family being displayed (null) display the added family and fire
			//entity selected event
			if(fDB.size() == 1)
			{
				LogDialog.add("FamilyPanel: ADDED_FAMILY ONC# " + addedFam.getONCNum(), "M");
				display(addedFam, null, null); //No child to display, & probably no history yet either
				setCounts();
				nav.setStoplightEntity(addedFam);
				onInitialFamilyDataLoaded();
				this.fireEntitySelected(this, EntityType.FAMILY, currFam, currFamHistory, currChild);
//				System.out.println(String.format("FamilyPanel DB Event -- displayed family: Source: %s, Type: %s, Object: %s",
//						dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			}
		}
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject1();
			
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
			ONCChild changeChild = (ONCChild) dbe.getObject1();
				
			if(currFam != null && currFam.getID() == changeChild.getFamID())	//Child was added to the displayed family
			{
				if(currFam != null)
				{
					LogDialog.add("FamilyPanel: ADDED_CHILD ONC# " + currFam.getONCNum() + ", Child: " + changeChild.getChildFirstName(), "M");
					display(currFam, currFamHistory, null);
					setCounts();
				}
			}
		}
		else if(dbe.getType().equals("DELETED_CHILD")) 
		{
			ONCChild changeChild = (ONCChild) dbe.getObject1();
				
			if(currFam.getID() == changeChild.getFamID())	//Child was added to the displayed family
			{
				if(currFam != null)
				{
					LogDialog.add("FamilyPanel: DELETED_CHILD ONC# " + currFam.getONCNum() + ", Child: " + changeChild.getChildFirstName(), "M");
					display(currFam, currFamHistory, null);
					setCounts();
					
					//need to tell other gui's that if new child is selected (e.g. Child Panel)
					if(currChild != null)
						fireEntitySelected(this, EntityType.CHILD, currFam, currFamHistory, currChild);
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
		{
			FamilyHistory addedHistory = (FamilyHistory) dbe.getObject1();
			
			//If updated delivery belongs to family being displayed, re-display it
			if(currFam != null && currFam.getID() == addedHistory.getFamID())
				display(currFam, addedHistory, null);
			
			setCounts();
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_MEAL") || dbe.getType().equals("UPDATED_MEAL")))
		{
			//may have added or deleted from current family, if so need to redisplay
			ONCMeal changedMeal = (ONCMeal) dbe.getObject1();
			if(currFam != null && currFam.getID() == changedMeal.getFamilyID())
				updateFamilyMeals(changedMeal);	//update the icon in the icon bar
		}
		else if(dbe.getType().equals("ADDED_ADULT") || dbe.getType().equals("DELETED_ADULT"))
		{
			//may have added or deleted from current family, if so need to redisplay
			ONCAdult changedAdult = (ONCAdult) dbe.getObject1();
			if(currFam != null && currFam.getID() == changedAdult.getFamID())
			{
				//update the icon in the icon bar
				checkForAdultsInFamily();
			}
		}
		else if(dbe.getType().equals("LOADED_DATABASE"))
		{
			//set the family panel nav message
			String mssg;
			String year = Integer.toString((Integer) dbe.getObject1());
			
			if(userDB.getLoggedInUser().getFirstName().equals(""))
				mssg = year + " season data has been loaded";
			else
				mssg = userDB.getLoggedInUser().getFirstName() + ", " + year + " season data has been loaded";
			
    			setMssg(mssg, true);
    			
    			//update the dnsCode combo box
    			updateDNSCodeCB();
    		
    			//if first family is present, load family and fire family selected event
    			if(fDB.size() > 0)
    			{
    				currFam = (ONCFamily) fDB.getObjectAtIndex(nav.getIndex());
    				display(currFam, null, null); //will set currChild if family has children
    				nav.setStoplightEntity(currFam);
    				onInitialFamilyDataLoaded();
    				setCounts();
    				this.fireEntitySelected(this, EntityType.FAMILY, currFam, currFamHistory, currChild);	
    			}
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") ||
				dbe.getType().equals("CHANGED_USER")))
		{
			//determine if it's the currently logged in user
			ONCUser updatedUser = (ONCUser)dbe.getObject1();
			if(userDB.getLoggedInUser().getID() == updatedUser.getID())
				setTextPaneFontSize(updatedUser.getPreferences().getFontSize());
		}
		else if(dbe.getType().equals("ADDED_NOTE") || dbe.getType().equals("UPDATED_NOTE")
				|| dbe.getType().equals("DELETED_NOTE"))
		{
			ONCNote changedNote = (ONCNote) dbe.getObject1();
			if(currFam != null && changedNote.getOwnerID() == currFam.getID())
				display(currFam, currFamHistory, currChild);
		}
		else if(dbe.getType().equals("ADDED_DNSCODE") || dbe.getType().equals("UPDATED_DNSCODE"))
		{
			updateDNSCodeCB();
		}
		else if(dbe.getType().equals("UPDATED_GLOBALS") && currFam != null)
		{
			display(currFam, currFamHistory, currChild);
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getSource() != this && (tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.GIFT))
		{
			ONCFamily selFam = (ONCFamily) tse.getObject1();
			FamilyHistory selFamHistory = (FamilyHistory) tse.getObject2();
			ONCChild selChild = (ONCChild) tse.getObject3();
		
			//is from nav, update and display new family
			if(tse.getSource() == nav && selFam.getID() != currFam.getID())
			{
				update();
				display(selFam, selFamHistory, selChild);	
			}
			//not nav and isn't current family displayed
			else if(tse.getSource() != nav && selFam.getID() != currFam.getID())
			{
				int rtn;
				if((rtn=fDB.searchForFamilyIndexByID(selFam.getID())) >= 0)
				{
					update();
					nav.setIndex(rtn);
					display(selFam, selFamHistory, selChild);
					
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
					display(selFam, selFamHistory, selChild);
					
					nav.setStoplightEntity(selFam);
				}
			}
		}
	}
	
	ONCProgressNavPanel getProgressNavPanel() { return nav; }
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.GIFT);
	}
    
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.AGENT);
	}

	class FamilyBagDialog extends JDialog implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private JComboBox<String> bagsPackagedCB, largeItemCB;
		private JButton btnOK;
		
		FamilyBagDialog(JFrame pf)
		{
			super(pf, true);
			this.setTitle("Family Packging Bag Detail");
			
			String[] selections = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
			
			JPanel bagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblBagMssg = new JLabel("Select the number of bags used");
			bagsPackagedCB = new JComboBox<String>(selections);
			bagPanel.add(lblBagMssg);
			bagPanel.add(bagsPackagedCB);
			
			JPanel liPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lblLIMssg = new JLabel("Select the number of large items");
			largeItemCB = new JComboBox<String>(selections);
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
			checkAndUpdateFamilyData(currFam, currFamHistory);
		}	
	}
	
	private class ChildTableModel extends AbstractTableModel
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
        			return userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 ? child.getChildFirstName() : "Child " + Integer.toString(row+1);
        		else if(col == LAST_NAME_COL)
        			return userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 ? child.getChildLastName() : "";
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
	
	private class PhoneTipAndBackground
	{
		private Color background;
		private String tip;
		
		PhoneTipAndBackground(int phoneid, boolean bPhoneEmpty)
		{
			int code = currFam.getPhoneCode() & PHONECODE_MASK[phoneid];
			int individualcode = code >> phoneid * 2;
			
			if(bPhoneEmpty)
				background = Color.white;
			else if(!bPhoneEmpty && individualcode == 1)
				background = Color.white;
			else if(!bPhoneEmpty && individualcode == 3)
				background = Color.yellow;
			else
				background = Color.red;
			
			String[] phone = {"Primary","Alternate","2nd Alternate"};
			String[]type = {"an empty or invalid","a valid mobile","an invalid","a valid landline/other"};
			tip =  String.format("%s is %s number, phonecode %d", phone[phoneid], type[individualcode], currFam.getPhoneCode());
		}
		
		Color getPhoneBackground() { return background; }
		String getTip() { return tip; }
	}
}

