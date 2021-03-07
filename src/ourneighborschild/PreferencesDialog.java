package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.google.gson.Gson;
import com.toedter.calendar.JDateChooser;


public class PreferencesDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This class implements the preferences dialog for ONC
	 */
	private static final long serialVersionUID = 1L;
	private static final Integer DEFAULT_FONT_SIZE = 13;
	private static final int PREFERRED_NUMBER_OF_TABLE_ROWS = 10;
	
	private static final int TITLE_COL = 0;
	private static final int TYPE_COL = 1;
	private static final int LAST_IMPORT_COL = 2;
	private static final int EXPIRES_COL = 3;
	private static final int FREQ_COL = 4;
	
	private GlobalVariablesDB gvDB;
	private DatabaseManager dbMgr;
	private UserDB userDB;
	private ActivityDB activityDB;
	private GiftCatalogDB catDB;
	
	private UserPreferences currUserPrefs;
	private ServerGVs currServerGVs;
	
	GeniusSignUps geniusSignUps;
	
	private ONCDateChooser dc_delivery, dc_seasonstart, dc_giftsreceived;
	private ONCDateChooser dc_DecemberGiftCutoff, dc_InfoEditCutoff, dc_ThanksgivingMealCutoff;
	private ONCDateChooser dc_DecemberMealCutoff, dc_WaitlistGiftCutoff;
	private JTextField whStreetNumTF, whStreetTF, whCityTF, whStateTF;
	private String whStreetNum, whStreet,whCity, whState;
	public JComboBox<Integer> oncFontSizeCB;
	private JComboBox<ONCGift> defaultGiftCB, defaultGiftCardCB;
	private JComboBox<Integer> numGiftsPerChildCB;
	private JComboBox<WishIntakeType> wishType0BeforeCB, wishType1BeforeCB, wishType2BeforeCB, wishType3BeforeCB;
	private JComboBox<WishIntakeType> wishType0AfterCB, wishType1AfterCB, wishType2AfterCB, wishType3AfterCB;
	private JComboBox<GiftDistribution> giftDistributionCB;
	private DefaultComboBoxModel<GiftDistribution> giftDistributionCBM;
	private JComboBox<MealIntake> mealIntakeCB;
	private DefaultComboBoxModel<MealIntake> mealIntakeCBM;
//	private JComboBox<Activity> deliveryActivityCB;
	private DefaultComboBoxModel<ONCGift> defaultGiftCBM, defaultGiftCardCBM;
//	private DefaultComboBoxModel<Activity> deliveryActivityCBM;
	private JButton btnApplyDateChanges, btnApplyAddressChanges;
	private boolean bIgnoreDialogEvents;
	private JCheckBox barcodeCkBox, signUpImportCkBox;
	private JComboBox<String> wishAssigneeFilterDefaultCB;
	private JComboBox<DNSCode> fdnsFilterDefaultCB;
	private DefaultComboBoxModel<DNSCode> fndsFilterDefaultCBM;
	private JComboBox<Barcode> barcodeCB;
	private JSpinner averyXOffsetSpinner, averyYOffsetSpinner;
	private Integer[] fontSizes = {8, 10, 12, 13, 14, 16, 18};
	
	private SimpleDateFormat sdf;
	private JLabel lblLastSignUpImportTime;
	private ONCTable signUpTbl;
	private SignUpTableModel signUpTM;
	private JButton btnImportSignUpList;
	
	PreferencesDialog(JFrame parentFrame)
	{
		super(parentFrame, false);
		this.setTitle("Our Neighbor's Child User & Season Settings");
		
		gvDB = GlobalVariablesDB.getInstance();
		if(gvDB != null)
			gvDB.addDatabaseListener(this);
		
		dbMgr = DatabaseManager.getInstance();
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		catDB = GiftCatalogDB.getInstance();
		if(catDB != null)
			catDB.addDatabaseListener(this);
		
		geniusSignUps = new GeniusSignUps();
		
		bIgnoreDialogEvents = false;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//set up personal elf settings tab
		JPanel userSettingsPanel = new JPanel();
		userSettingsPanel.setLayout(new BoxLayout(userSettingsPanel, BoxLayout.Y_AXIS));
		
		JPanel fontPanel = new JPanel();
		JLabel lblFont = new JLabel("Font Size:");
		oncFontSizeCB = new JComboBox<Integer>(fontSizes);
		oncFontSizeCB.setSelectedItem(DEFAULT_FONT_SIZE);
		oncFontSizeCB.setEnabled(false);
		oncFontSizeCB.addActionListener(this);
		fontPanel.add(lblFont);
		fontPanel.add(oncFontSizeCB);
		
		JPanel wafdPanel = new JPanel();
		JLabel lblWAFD = new JLabel("Wish Assignee Filter Default:");
		String[] choices = {"Any", "Unassigned"};
		wishAssigneeFilterDefaultCB = new JComboBox<String>(choices);
		wishAssigneeFilterDefaultCB.setSelectedIndex(0);
		wishAssigneeFilterDefaultCB.setEnabled(false);
		wishAssigneeFilterDefaultCB.addActionListener(this);
		wafdPanel.add(lblWAFD);
		wafdPanel.add(wishAssigneeFilterDefaultCB);
		
		JPanel fdfPanel = new JPanel();
		JLabel lblFDF = new JLabel("Family Do Not Serve Filter Default:");
//		String[] options = {"None", "Any"};
		fndsFilterDefaultCBM = new DefaultComboBoxModel<DNSCode>();
		fndsFilterDefaultCBM.addElement(new DNSCode(-4, "No Codes", "No Codes", "No Codes"));
		fndsFilterDefaultCBM.addElement(new DNSCode(-3, "Any", "Any", "Any"));
		fdnsFilterDefaultCB = new JComboBox<DNSCode>();
		fdnsFilterDefaultCB.setModel(fndsFilterDefaultCBM);
		fdnsFilterDefaultCB.setEnabled(false);
		fdnsFilterDefaultCB.addActionListener(this);
		fdfPanel.add(lblFDF);
		fdfPanel.add(fdnsFilterDefaultCB);
		
		userSettingsPanel.add(fontPanel);
		userSettingsPanel.add(wafdPanel);
		userSettingsPanel.add(fdfPanel);
		
		tabbedPane.addTab("User Preferences", userSettingsPanel);
		
		//set up the season dates tab
		JPanel dateTab = new JPanel();
		dateTab.setLayout(new BoxLayout(dateTab, BoxLayout.Y_AXIS));
		
		JPanel datePanelTop = new JPanel();
		datePanelTop.setLayout(new FlowLayout(FlowLayout.LEFT));
//		datePanel.setLayout(new GridLayout(3,3));
		DateChangeListener dcl = new DateChangeListener();	//listener for all date changes
		
		Dimension dateSize = new Dimension(200, 56);
//		dc_today = new JDateChooser(gvDB.getTodaysDate());
//		dc_today.setPreferredSize(dateSize);
//		dc_today.setBorder(BorderFactory.createTitledBorder("Today's Date"));
//		dc_today.setEnabled(false);
//		dc_today.getDateEditor().addPropertyChangeListener(dcl); 
//		datePanelTop.add(dc_today);
		
//		dc_seasonstart = new JDateChooser(gvDB.getSeasonStartDate());
		dc_seasonstart = new ONCDateChooser();
		dc_seasonstart.setCalendar(getCalendar(gvDB.getSeasonStartDate()));
		dc_seasonstart.setPreferredSize(dateSize);
		dc_seasonstart.setBorder(BorderFactory.createTitledBorder("Season Start Date"));
		dc_seasonstart.setEnabled(false);
		dc_seasonstart.getDateEditor().addPropertyChangeListener(dcl);	
		datePanelTop.add(dc_seasonstart);
		
		dc_delivery = new ONCDateChooser();
		dc_delivery.setCalendar(getCalendar(gvDB.getDeliveryDateMillis()));
		dc_delivery.setCalendar(getCalendar(gvDB.getDeliveryDateMillis()));
		dc_delivery.setPreferredSize(dateSize);
		dc_delivery.setBorder(BorderFactory.createTitledBorder("Delivery Date"));
		dc_delivery.setEnabled(false);
		dc_delivery.getDateEditor().addPropertyChangeListener(dcl);
		datePanelTop.add(dc_delivery);

		dc_InfoEditCutoff = new ONCDateChooser();
		dc_InfoEditCutoff.setPreferredSize(dateSize);
		dc_InfoEditCutoff.setBorder(BorderFactory.createTitledBorder("Family Update Deadline"));
		dc_InfoEditCutoff.setEnabled(false);
		dc_InfoEditCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelTop.add(dc_InfoEditCutoff);
		
		JPanel datePanelMid = new JPanel();
		datePanelMid.setLayout(new FlowLayout(FlowLayout.LEFT));
		dc_ThanksgivingMealCutoff = new ONCDateChooser();
		dc_ThanksgivingMealCutoff.setPreferredSize(dateSize);
		dc_ThanksgivingMealCutoff.setBorder(BorderFactory.createTitledBorder("Thanksgiving Meal Deadline"));
		dc_ThanksgivingMealCutoff.setEnabled(false);
		dc_ThanksgivingMealCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelMid.add(dc_ThanksgivingMealCutoff);
		
		dc_DecemberMealCutoff = new ONCDateChooser();
		dc_DecemberMealCutoff.setPreferredSize(dateSize);
		dc_DecemberMealCutoff.setBorder(BorderFactory.createTitledBorder("December Meal Deadline"));
		dc_DecemberMealCutoff.setEnabled(false);
		dc_DecemberMealCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelMid.add(dc_DecemberMealCutoff);
		
		JPanel datePanelBottom = new JPanel();
		datePanelBottom.setLayout(new FlowLayout(FlowLayout.LEFT));
		dc_DecemberGiftCutoff = new ONCDateChooser();
		dc_DecemberGiftCutoff.setPreferredSize(dateSize);
		dc_DecemberGiftCutoff.setBorder(BorderFactory.createTitledBorder("December Gift Deadline"));
		dc_DecemberGiftCutoff.setEnabled(false);
		dc_DecemberGiftCutoff.getDateEditor().addPropertyChangeListener(dcl);
		datePanelBottom.add(dc_DecemberGiftCutoff);
				
		dc_WaitlistGiftCutoff = new ONCDateChooser();
		dc_WaitlistGiftCutoff.setPreferredSize(dateSize);
		dc_WaitlistGiftCutoff.setBorder(BorderFactory.createTitledBorder("Waitlist Gift Deadline"));
		dc_WaitlistGiftCutoff.setEnabled(false);
		dc_WaitlistGiftCutoff.getDateEditor().addPropertyChangeListener(dcl);
		datePanelBottom.add(dc_WaitlistGiftCutoff);
		
		dc_giftsreceived = new ONCDateChooser();
		dc_giftsreceived.setCalendar(getCalendar(gvDB.getGiftsReceivedDate()));
		dc_giftsreceived.setPreferredSize(dateSize);
		dc_giftsreceived.setToolTipText("<html>All gifts must be received from partners <b><i>BEFORE</i></b> this date</html>");
		dc_giftsreceived.setBorder(BorderFactory.createTitledBorder("Gifts Received Deadline"));
		dc_giftsreceived.setEnabled(false);
		dc_giftsreceived.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelBottom.add(dc_giftsreceived);
		
		btnApplyDateChanges = new JButton("Apply Date Changes");
		btnApplyDateChanges.setEnabled(false);
		btnApplyDateChanges.addActionListener(this);
		
		dateTab.add(datePanelTop);
		dateTab.add(datePanelMid);
		dateTab.add(datePanelBottom);
		dateTab.add(btnApplyDateChanges);
		
		tabbedPane.addTab("Dates", dateTab);
		
		//set up the warehouse address tab
		JPanel addressTab = new JPanel();
		addressTab.setLayout(new BoxLayout(addressTab, BoxLayout.Y_AXIS));
		addressTab.setBorder(BorderFactory.createTitledBorder("Warehouse Address:"));
		
		JPanel addressPanel = new JPanel();
		AddressKeyListener akl = new AddressKeyListener();	//address key listener
		
		whStreetNumTF = new JTextField(5);
		whStreetNumTF.setBorder(BorderFactory.createTitledBorder("Street #"));
		whStreetNumTF.addKeyListener(akl);
		
		whStreetTF = new JTextField(12);
		whStreetTF.setBorder(BorderFactory.createTitledBorder("Street Name"));
		whStreetTF.addKeyListener(akl);
		
		whCityTF = new JTextField(8);
		whCityTF.setBorder(BorderFactory.createTitledBorder("City"));
		whCityTF.addKeyListener(akl);
		
		whStateTF = new JTextField(4);
		whStateTF.setBorder(BorderFactory.createTitledBorder("State"));
		whStateTF.addKeyListener(akl);
		
		addressPanel.add(whStreetNumTF);
		addressPanel.add(whStreetTF);
		addressPanel.add(whCityTF);
		addressPanel.add(whStateTF);
		
		JPanel addressCntlPanel = new JPanel();
		btnApplyAddressChanges = new JButton("Apply Address Changes");
		btnApplyAddressChanges.setEnabled(false);
		btnApplyAddressChanges.addActionListener(this);
		addressCntlPanel.add(btnApplyAddressChanges);
		
		addressTab.add(addressPanel);
		addressTab.add(addressCntlPanel);
		
		tabbedPane.addTab("Warehouse", addressTab);
		
		//set up the gift preference tab.
		JPanel giftPanel = new JPanel();
		giftPanel.setLayout(new BoxLayout(giftPanel, BoxLayout.Y_AXIS));
		JPanel giftPanelTop = new JPanel();
		
		//wish intake configuration panels
		JPanel beforeDeadlineGiftPanel = new JPanel();
		JPanel afterDeadlineGiftPanel = new JPanel();
		
		JPanel giftPanelBottom = new JPanel();
		
		giftPanelTop.setBorder(BorderFactory.createTitledBorder("Gift Settings"));
		beforeDeadlineGiftPanel.setBorder(BorderFactory.createTitledBorder("Before December Gift Deadline Wish Intake Settings"));
		afterDeadlineGiftPanel.setBorder(BorderFactory.createTitledBorder("After December Gift Deadline Wish Intake Settings"));
		giftPanelBottom.setBorder(BorderFactory.createTitledBorder("Gift Distribution & Meal Intake Settings"));
		
		
		Integer[] numGiftChoices = {0,1,2,3};
		numGiftsPerChildCB = new JComboBox<Integer>(numGiftChoices);
		numGiftsPerChildCB.setPreferredSize(new Dimension(180, 56));
		numGiftsPerChildCB.setBorder(BorderFactory.createTitledBorder("# Gifts Per Child"));
		numGiftsPerChildCB.addActionListener(this);
		giftPanelTop.add(numGiftsPerChildCB);
		
		defaultGiftCB = new JComboBox<ONCGift>();
		defaultGiftCBM = new DefaultComboBoxModel<ONCGift>();
		defaultGiftCBM.addElement(new ONCGift(-1, "None", 7));//creates a dummy gift with name "None", id = -1;
		defaultGiftCB.setModel(defaultGiftCBM);
		defaultGiftCB.setPreferredSize(new Dimension(180, 56));
		defaultGiftCB.setBorder(BorderFactory.createTitledBorder("Default Gift"));
		defaultGiftCB.addActionListener(this);
		giftPanelTop.add(defaultGiftCB);
		
		defaultGiftCardCB = new JComboBox<ONCGift>();
		defaultGiftCardCBM = new DefaultComboBoxModel<ONCGift>();		
		defaultGiftCardCBM.addElement(new ONCGift(-1, "None", 7));//creates a dummy gift with name "None", id = -1;
		defaultGiftCardCB.setModel(defaultGiftCardCBM);
		defaultGiftCardCB.setPreferredSize(new Dimension(180, 56));
		defaultGiftCardCB.setBorder(BorderFactory.createTitledBorder("Default Gift Card"));
		defaultGiftCardCB.addActionListener(this);
		giftPanelTop.add(defaultGiftCardCB);
		
		wishType0BeforeCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType0BeforeCB.setPreferredSize(new Dimension(180, 56));
		wishType0BeforeCB.setBorder(BorderFactory.createTitledBorder("Wish 1"));
		wishType0BeforeCB.addActionListener(this);
		beforeDeadlineGiftPanel.add(wishType0BeforeCB);
		
		wishType1BeforeCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType1BeforeCB.setPreferredSize(new Dimension(180, 56));
		wishType1BeforeCB.setBorder(BorderFactory.createTitledBorder("Wish 2"));
		wishType1BeforeCB.addActionListener(this);
		beforeDeadlineGiftPanel.add(wishType1BeforeCB);
		
		wishType2BeforeCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType2BeforeCB.setPreferredSize(new Dimension(180, 56));
		wishType2BeforeCB.setBorder(BorderFactory.createTitledBorder("Wish 3"));
		wishType2BeforeCB.addActionListener(this);
		beforeDeadlineGiftPanel.add(wishType2BeforeCB);
		
		wishType3BeforeCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType3BeforeCB.setPreferredSize(new Dimension(180, 56));
		wishType3BeforeCB.setBorder(BorderFactory.createTitledBorder("Alternate Wish"));
		wishType3BeforeCB.addActionListener(this);
		beforeDeadlineGiftPanel.add(wishType3BeforeCB);
		
		wishType0AfterCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType0AfterCB.setPreferredSize(new Dimension(180, 56));
		wishType0AfterCB.setBorder(BorderFactory.createTitledBorder("Wish 1"));
		wishType0AfterCB.addActionListener(this);
		afterDeadlineGiftPanel.add(wishType0AfterCB);
		
		wishType1AfterCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType1AfterCB.setPreferredSize(new Dimension(180, 56));
		wishType1AfterCB.setBorder(BorderFactory.createTitledBorder("Wish 2"));
		wishType1AfterCB.addActionListener(this);
		afterDeadlineGiftPanel.add(wishType1AfterCB);
		
		wishType2AfterCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType2AfterCB.setPreferredSize(new Dimension(180, 56));
		wishType2AfterCB.setBorder(BorderFactory.createTitledBorder("Wish 3"));
		wishType2AfterCB.addActionListener(this);
		afterDeadlineGiftPanel.add(wishType2AfterCB);
		
		wishType3AfterCB = new JComboBox<WishIntakeType>(WishIntakeType.values());
		wishType3AfterCB.setPreferredSize(new Dimension(180, 56));
		wishType3AfterCB.setBorder(BorderFactory.createTitledBorder("Alternate Wish"));
		wishType3AfterCB.addActionListener(this);
		afterDeadlineGiftPanel.add(wishType3AfterCB);
		
		giftDistributionCBM = new DefaultComboBoxModel<GiftDistribution>();
		for(GiftDistribution dist : GiftDistribution.getPreferenceOptions())
			giftDistributionCBM.addElement(dist);
		giftDistributionCB = new JComboBox<GiftDistribution>(giftDistributionCBM);
		giftDistributionCB.setPreferredSize(new Dimension(180, 56));
		giftDistributionCB.setBorder(BorderFactory.createTitledBorder("Gift Distribution"));
		giftDistributionCB.addActionListener(this);
		giftPanelBottom.add(giftDistributionCB);
		
		mealIntakeCBM = new DefaultComboBoxModel<MealIntake>();
		for(MealIntake mi : MealIntake.values())
			mealIntakeCBM.addElement(mi);
		mealIntakeCB = new JComboBox<MealIntake>(mealIntakeCBM);
		mealIntakeCB.setPreferredSize(new Dimension(240, 56));
		mealIntakeCB.setBorder(BorderFactory.createTitledBorder("Meal Intake"));
		mealIntakeCB.addActionListener(this);
		giftPanelBottom.add(mealIntakeCB);
		
		giftPanel.add(giftPanelTop);
		giftPanel.add(beforeDeadlineGiftPanel);
		giftPanel.add(afterDeadlineGiftPanel);
		giftPanel.add(giftPanelBottom);
		
		tabbedPane.addTab("Gifts/Meals", giftPanel);
		
		//set up the ornament label tab
		JPanel wishlabelPanel = new JPanel();
		wishlabelPanel.setLayout(new BoxLayout(wishlabelPanel, BoxLayout.Y_AXIS));
		JPanel wishlabelPanelTop = new JPanel();
		JPanel wishlabelPanelMiddle = new JPanel();
		
		barcodeCkBox = new JCheckBox("Use barcode instead of icon on label using barcode:");
		barcodeCkBox.setSelected(gvDB.includeBarcodeOnLabels());
		barcodeCkBox.addActionListener(this);
		wishlabelPanelTop.add(barcodeCkBox);
		
		barcodeCB = new JComboBox<Barcode>(Barcode.values());
		barcodeCB.setSelectedItem(gvDB.getBarcodeCode());
		barcodeCB.addActionListener(this);;
		wishlabelPanelTop.add(barcodeCB);
		
		Point lblOffset = gvDB.getAveryLabelOffset();
		averyXOffsetSpinner = new JSpinner(new SpinnerNumberModel(lblOffset.x, 0, 100, 1));
		averyYOffsetSpinner = new JSpinner(new SpinnerNumberModel(lblOffset.y, 0, 100, 1));
		
		SpinnerChangeListener listener = new SpinnerChangeListener();
		averyXOffsetSpinner.addChangeListener(listener);
		averyYOffsetSpinner.addChangeListener(listener);
		
		wishlabelPanelMiddle.add(new JLabel("Label X Offset:"));
		wishlabelPanelMiddle.add(averyXOffsetSpinner);
		wishlabelPanelMiddle.add(new JLabel("Label Y Offset:"));
		wishlabelPanelMiddle.add(averyYOffsetSpinner);
		
		wishlabelPanel.add(wishlabelPanelTop);
		wishlabelPanel.add(wishlabelPanelMiddle);
		
		tabbedPane.addTab("Ornaments", wishlabelPanel);
		
		//set up the SignUpGenuis/Activities Tab
		JPanel geniusPanel = new JPanel();
		geniusPanel.setLayout(new BoxLayout(geniusPanel, BoxLayout.Y_AXIS));
		
		JPanel importPanel = new JPanel();
		importPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		signUpImportCkBox = new JCheckBox("Automatic Import of Volunteers from Sign-Up(s) Enabled?");
		signUpImportCkBox.setSelected(false);
		signUpImportCkBox.addActionListener(this);
		importPanel.add(signUpImportCkBox);
		
		//instantiate the signup table model
		signUpTM = new SignUpTableModel();
				
		//set up the member table
		String[] signUpTblTT = {"Name of SignUp", "Type of SignUp, i.e., Volunteer, Clothing...",
								"Time volunteers last imported from SignUp Genius",
								"Deadline for volunteers to sign up", 
								"Frequency of automatica import from SignUp Genius"};
		
		signUpTbl = new ONCTable(signUpTM, signUpTblTT, new Color(240,248,255)); 

//		signUpTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		signUpTbl.setRowSelectionAllowed(false);
		
		TableColumn typeColumn = signUpTbl.getColumnModel().getColumn(TYPE_COL);
		typeColumn.setCellEditor(new DefaultCellEditor(new JComboBox<SignUpType>(SignUpType.values())));
		
		TableColumn permColumn = signUpTbl.getColumnModel().getColumn(FREQ_COL);
		permColumn.setCellEditor(new DefaultCellEditor(new JComboBox<Frequency>(Frequency.getGeniusImportFrequencies())));
		
		//set up a cell renderer for the LAST_IMPORT column to display the time of last import
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm");

		    public Component getTableCellRendererComponent(JTable table, Object value,
		            boolean isSelected, boolean hasFocus, int row, int column)
		    { 
		        if(value instanceof java.util.Date)
		            value = f.format(value);
		        
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};
		signUpTbl.getColumnModel().getColumn(LAST_IMPORT_COL).setCellRenderer(tableCellRenderer);
		signUpTbl.getColumnModel().getColumn(EXPIRES_COL).setCellRenderer(tableCellRenderer);
				
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {256, 80, 88, 88, 64};
		for(int col=0; col < colWidths.length; col++)
		{
			signUpTbl.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
				
//		signUpTbl.setAutoCreateRowSorter(true);	//add a row sorter

		//set the header color
		JTableHeader anHeader = signUpTbl.getTableHeader();
		anHeader.setForeground( Color.black);
		anHeader.setBackground( new Color(161,202,241));
		
		 //justify columns
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        signUpTbl.getColumnModel().getColumn(FREQ_COL).setCellRenderer(dtcr);
		   
		//Create the scroll pane and add the table to it and set the user tip
		Dimension tablesize = new Dimension(tablewidth, signUpTbl.getRowHeight() *
		        										PREFERRED_NUMBER_OF_TABLE_ROWS);
		signUpTbl.setPreferredScrollableViewportSize(tablesize);
		JScrollPane signUpScrollPane = new JScrollPane(signUpTbl,
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		signUpScrollPane.setToolTipText("ONC Sign Ups");
		
		JPanel geniusControlPanel = new JPanel();
		geniusControlPanel.setLayout(new BoxLayout(geniusControlPanel, BoxLayout.X_AXIS));
		JPanel geniusImportPanel = new JPanel();
		geniusImportPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel geniusBtnPanel = new JPanel();
		geniusBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		sdf = new SimpleDateFormat("M/d/yy h:mm a");
		lblLastSignUpImportTime = new JLabel("Time List of Active Sign-Up(s) Last Imported: Never");
//		lblLastSignUpImportTime.setPreferredSize(new Dimension(184, 52));
		geniusImportPanel.add(lblLastSignUpImportTime);
		
		btnImportSignUpList = new JButton("Import Sign-Ups");
		btnImportSignUpList.addActionListener(this);
		geniusBtnPanel.add(btnImportSignUpList);
		
		geniusControlPanel.add(geniusImportPanel);
		geniusControlPanel.add(geniusBtnPanel);
		
//		JPanel deliveryActivityPanel = new JPanel();
//		deliveryActivityPanel.setLayout(new BoxLayout(deliveryActivityPanel, BoxLayout.X_AXIS));
//		deliveryActivityPanel.setBorder(BorderFactory.createTitledBorder("Delivery Activity"));
//		
//		JLabel lblDeliveryActivity = new JLabel("Set the Delivery Activity:");
//		deliveryActivityPanel.add(lblDeliveryActivity);
		
//		deliveryActivityCB = new JComboBox<Activity>();
//		deliveryActivityCBM = new DefaultComboBoxModel<Activity>();
		
//		deliveryActivityCBM.addElement(new Activity(-1, "None"));//creates a dummy Activity with name "None", id = -1;
//		deliveryActivityCB.setModel(deliveryActivityCBM);
//		deliveryActivityCB.setPreferredSize(new Dimension(180, 56));
//		deliveryActivityCB.setToolTipText("Used to determine what volunteers are making deliveries");
//		deliveryActivityCB.addActionListener(this);
//		deliveryActivityPanel.add(deliveryActivityCB);
		
//		deliveryActivityPanel.add(Box.createRigidArea(new Dimension(240,0)));
		
		//add the table scroll pane to the symbol panel
		geniusPanel.add(importPanel);
		geniusPanel.add(signUpScrollPane);
		geniusPanel.add(geniusControlPanel);
//		geniusPanel.add(deliveryActivityPanel);
				
		tabbedPane.addTab("SignUpGenius", geniusPanel);
		
		//Add the components to the frame pane
        this.getContentPane().add(tabbedPane);
               
        this.pack();
        this.setMinimumSize(new Dimension(720, 160));
        btnApplyDateChanges.requestFocusInWindow();
	}
	
	//helpers
	void checkEnabledMealsAndWishTypes(Integer nGiftsPerChild, Integer priorNGiftsPerChild)
	{
		bIgnoreDialogEvents = true;
		
		//adjust wish intake types based on number of gifts provided to a child. If number of gifts is 
		//zero, we must be providing meals. Adjust meal in-take options accordingly
		if(nGiftsPerChild == 0)
		{
			giftDistributionCB.setSelectedItem(GiftDistribution.None);
			mealIntakeCB.setSelectedItem(MealIntake.Thanksgiving_December);
			mealIntakeCB.setEnabled(nGiftsPerChild == 0);
			
			//add None as only gift distribution option and select it
			giftDistributionCBM.removeAllElements();
			giftDistributionCBM.addElement(GiftDistribution.None);
			giftDistributionCB.setSelectedItem(GiftDistribution.None);
			
			//remove None as meal in-take option
			MealIntake currSelection = (MealIntake) mealIntakeCB.getSelectedItem();
			mealIntakeCBM.removeElement(MealIntake.None);
			if(currSelection != MealIntake.None)
				mealIntakeCB.setSelectedItem(currSelection);
			else
				mealIntakeCB.setSelectedItem(MealIntake.Thanksgiving_December);
		}
		else if(nGiftsPerChild > 0 && priorNGiftsPerChild == 0)
		{	
			//add Pickup, Delivery and Pickup_Delivery as gift distribution options, select Pickup_Delivery
			giftDistributionCBM.removeAllElements();
			giftDistributionCBM.addElement(GiftDistribution.Pickup);
			giftDistributionCBM.addElement(GiftDistribution.Delivery);
			giftDistributionCBM.addElement(GiftDistribution.Pickup_Delivery);			
			giftDistributionCB.setSelectedItem(GiftDistribution.Pickup_Delivery);

			//add None as meal in-take option
			MealIntake currSelection = (MealIntake) mealIntakeCB.getSelectedItem();
			mealIntakeCBM.removeAllElements();
			for(MealIntake mi : MealIntake.values())
				mealIntakeCBM.addElement(mi);
			if(currSelection != null)
				mealIntakeCB.setSelectedItem(currSelection);					
		}
			
		//before gift deadline settings
		wishType0BeforeCB.setEnabled(nGiftsPerChild > 0);
		wishType1BeforeCB.setEnabled(nGiftsPerChild > 1);
		wishType2BeforeCB.setEnabled(nGiftsPerChild > 2);
		wishType3BeforeCB.setEnabled(nGiftsPerChild > 0);  //Alternate Wish for ONC follows wish 0
		
		//after gift deadline settings
		wishType0AfterCB.setEnabled(nGiftsPerChild > 0);
		wishType1AfterCB.setEnabled(nGiftsPerChild > 1);
		wishType2AfterCB.setEnabled(nGiftsPerChild > 2);
		wishType3AfterCB.setEnabled(nGiftsPerChild > 0);  //Alternate Wish for ONC follows wish 0
		
		giftDistributionCB.setEnabled(nGiftsPerChild > 0);
		
		bIgnoreDialogEvents = false;
			
	}
	
	Calendar getCalendar(Long day)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(day);
		return cal;
	}
	
	void display(ServerGVs serverGVs, UserPreferences uPrefs)	//Update gui with preference data changes (called when an saved ONC object loaded)
	{
		if(serverGVs != null)
			this.currServerGVs = serverGVs;
		else
			this.currServerGVs = gvDB.getServerGVs();
		
		if(uPrefs != null)
			this.currUserPrefs = uPrefs;
		
		bIgnoreDialogEvents = true;
		
		dc_delivery.setTime(currServerGVs.getDeliveryDayMillis());
//		dc_delivery.setCalendar(getCalendar(currServerGVs.getDeliveryDayMillis()));
		dc_seasonstart.setTime(currServerGVs.getSeasonStartDateMillis());
		dc_giftsreceived.setTime(currServerGVs.getGiftsReceivedDeadlineMillis());
		dc_ThanksgivingMealCutoff.setTime(currServerGVs.getThanksgivingMealDeadlineMillis());
		dc_DecemberMealCutoff.setTime(currServerGVs.getDecemberMealDeadlineMillis());
		dc_DecemberGiftCutoff.setTime(currServerGVs.getDecemberGiftDeadlineMillis());
		dc_WaitlistGiftCutoff.setTime(currServerGVs.getWaitListGiftDeadlineMillis());
		dc_InfoEditCutoff.setTime(currServerGVs.getFamilyEditDeadlineMillis());
		
		if(gvDB.getDefaultGiftID() > -1 && catDB.size() > 0)
		{
			ONCGift defaultWish = catDB.getGiftByID(gvDB.getDefaultGiftID());
			if(defaultWish != null)
				defaultGiftCB.setSelectedItem(defaultWish);
			else
				defaultGiftCB.setSelectedIndex(0);	
		}
		else
			defaultGiftCB.setSelectedIndex(0);
		
		if(gvDB.getDefaultGiftCardID() > -1 && catDB.size() > 0)
		{
			ONCGift defaultGiftCardWish = catDB.getGiftByID(gvDB.getDefaultGiftCardID());
			if(defaultGiftCardWish != null)
				defaultGiftCardCB.setSelectedItem(defaultGiftCardWish);
			else
				defaultGiftCardCB.setSelectedIndex(0);
		}
		else
			defaultGiftCardCB.setSelectedIndex(0);
		
		numGiftsPerChildCB.setSelectedItem(currServerGVs.getNumberOfGiftsPerChild());
		
		wishType0BeforeCB.setSelectedItem(getWishIntakeType(0, WishIntakeTiming.BEFORE_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType1BeforeCB.setSelectedItem(getWishIntakeType(1, WishIntakeTiming.BEFORE_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType2BeforeCB.setSelectedItem(getWishIntakeType(2, WishIntakeTiming.BEFORE_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType3BeforeCB.setSelectedItem(getWishIntakeType(3, WishIntakeTiming.BEFORE_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType0AfterCB.setSelectedItem(getWishIntakeType(0, WishIntakeTiming.AFTER_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType1AfterCB.setSelectedItem(getWishIntakeType(1, WishIntakeTiming.AFTER_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType2AfterCB.setSelectedItem(getWishIntakeType(2, WishIntakeTiming.AFTER_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		wishType3AfterCB.setSelectedItem(getWishIntakeType(3, WishIntakeTiming.AFTER_DEADLINE, currServerGVs.getChildWishIntakeConfiguraiton()));
		
		giftDistributionCB.setSelectedItem(currServerGVs.getGiftDistribution());
		mealIntakeCB.setSelectedItem(MealIntake.intake(currServerGVs.getMealIntake()));

		
//		if(gvDB.getDeliveryActivityID() > -1 && activityDB.size() > 0)
//		{
//			Activity defaultDeliveryActivity = activityDB.getActivity(gvDB.getDeliveryActivityID());
//			if(defaultDeliveryActivity != null)
//				deliveryActivityCB.setSelectedItem(defaultDeliveryActivity);
//			else
//				deliveryActivityCB.setSelectedIndex(0);	
//		}
//		else
//			deliveryActivityCB.setSelectedIndex(0);
		
		displayWarehouseAddress();
		
		ONCUser user = userDB.getLoggedInUser();
		if(user != null)
		{
			oncFontSizeCB.setSelectedItem(user.getPreferences().getFontSize());
			wishAssigneeFilterDefaultCB.setSelectedIndex(user.getPreferences().getWishAssigneeFilter());
			fdnsFilterDefaultCB.setSelectedItem(user.getPreferences().getFamilyDNSFilterCode());
		}
		else
		{
			oncFontSizeCB.setSelectedItem(DEFAULT_FONT_SIZE);
			wishAssigneeFilterDefaultCB.setSelectedIndex(0);
			fdnsFilterDefaultCB.setSelectedIndex(0);
		}
		
		barcodeCkBox.setSelected(gvDB.includeBarcodeOnLabels());
		
		btnApplyDateChanges.setEnabled(false);
		
		bIgnoreDialogEvents = false;
	}
	
	/***
	 * Takes the warehouse address string in GoogleMap format and separates it into
	 * its component parts and displays in address component text fields.
	 * GoogleMap format is a continuous line separated by '+'.
	 * Example: 1000 Main Street Anywhere, USA in GoogleMap format is
	 * 1000+Main+Street+Anywhere,USA. 
	 */
	void displayWarehouseAddress()
	{
		String[] whAddressPart = gvDB.getWarehouseAddress().split("\\+");
		if(whAddressPart.length > 2)
		{	
			int index=0;
			whStreetNum = whAddressPart[index++];
			whStreetNumTF.setText(whStreetNum);	//set street number
			
			//determine what parts belong to the street name
			StringBuffer buff = new StringBuffer(whAddressPart[index++]);
			while(index < whAddressPart.length-1)
				buff.append(" " + whAddressPart[index++]);
			whStreet = buff.toString();
			whStreetTF.setText(whStreet);
			
			//set the city and state
			String[] whAddressCityAndState = whAddressPart[whAddressPart.length-1].split(",");
			whCity = whAddressCityAndState[0];
			whCityTF.setText(whCity);
			whState = whAddressCityAndState[1];
			whStateTF.setText(whState);
		}
	}
	
	void displaySignUpData()
	{
		GeniusSignUps geniusSignUps = activityDB.getSignUps();
		String time = String.format("Time List of Active Sign Up(s) Last Imported: %s", 
				sdf.format(geniusSignUps.getLastImportTime().getTime()));
		
		signUpImportCkBox.setSelected(geniusSignUps.isImportEnabled());
		lblLastSignUpImportTime.setText(time);
			
		signUpTM.fireTableDataChanged();
	}
	
	String getWarehouseAddressInGoogleMapsFormat()
	{
		StringBuffer buff = new StringBuffer(whStreetNumTF.getText().trim());
		
		String[] streetNamePart = whStreetTF.getText().split(" ");
		for(int i=0; i<streetNamePart.length; i++)
			buff.append("+" + streetNamePart[i].trim());
		
		buff.append("+" + whCityTF.getText().trim() + "," + whStateTF.getText().trim());
		
		return buff.toString();
	}
	
	//update the server if serverGVs have changed
	void update()
	{
		int cf = 0;
		
		if(gvDB.getSeasonStartDate() != dc_seasonstart.getTime()) { cf |= 1;}
		if(gvDB.getDeliveryDateMillis() != dc_delivery.getTime()) { cf |= 2; }
		if(!gvDB.getWarehouseAddress().equals(getWarehouseAddressInGoogleMapsFormat())) {cf |= 4;}
		if(gvDB.getGiftsReceivedDate() != dc_giftsreceived.getTime()) {cf |= 8;}
		if(gvDB.getThanksgivingMealDeadline() != dc_ThanksgivingMealCutoff.getTime()) {cf |= 16;}
		if(gvDB.getDecemberGiftDeadline() !=dc_DecemberGiftCutoff.getTime()) {cf |= 32;}
		if(gvDB.getFamilyEditDeadline() != dc_InfoEditCutoff.getTime()) {cf |= 64;}
		if(gvDB.getDecemberMealDeadline() != dc_DecemberMealCutoff.getTime()) {cf |= 128;}
		if(gvDB.getWaitlistGiftDeadline() != dc_WaitlistGiftCutoff.getTime()) {cf |= 256;}
		
		ONCGift cbWish = (ONCGift) defaultGiftCB.getSelectedItem();
		if(gvDB.getDefaultGiftID() != cbWish.getID()) { cf |= 512; }
		
		ONCGift cbGiftCardWish = (ONCGift) defaultGiftCardCB.getSelectedItem();
		if(gvDB.getDefaultGiftCardID() != cbGiftCardWish.getID()) {cf |= 1024;}
		
		if(gvDB.getNumberOfGiftsPerChild() != (Integer) numGiftsPerChildCB.getSelectedItem()) {cf |= 2048;}
		if(getWishIntakeType(0, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType0BeforeCB.getSelectedItem()) {cf |= 4096;}
		if(getWishIntakeType(1, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType1BeforeCB.getSelectedItem()) {cf |= 8192;}
		if(getWishIntakeType(2, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType2BeforeCB.getSelectedItem()) {cf |= 16384;}
		if(getWishIntakeType(3, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType3BeforeCB.getSelectedItem()) {cf |= 32768;}
		if(getWishIntakeType(0, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType0AfterCB.getSelectedItem()) {cf |= 65536;}
		if(getWishIntakeType(1, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType1AfterCB.getSelectedItem()) {cf |= 131072;}
		if(getWishIntakeType(2, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType2AfterCB.getSelectedItem()) {cf |= 262144;}
		if(getWishIntakeType(3, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()) != (WishIntakeType) wishType3AfterCB.getSelectedItem()) {cf |= 524288;}
		if(gvDB.getGiftDistribution() != ((GiftDistribution) giftDistributionCB.getSelectedItem())) { cf |= 1048576; }
		if(gvDB.getMealIntake() != ((MealIntake) mealIntakeCB.getSelectedItem()).index()) { cf |= 2097152; }
		
		if(cf > 0)
		{
			ServerGVs updateGVreq = new ServerGVs(dc_delivery.getTime(), //getCalendar().getTimeInMillis(), 
													dc_seasonstart.getTime(), 
													 getWarehouseAddressInGoogleMapsFormat(),
													  dc_giftsreceived.getTime(),
													   dc_ThanksgivingMealCutoff.getTime(),
													    dc_DecemberGiftCutoff.getTime(),
													     dc_InfoEditCutoff.getTime(),
													      cbWish.getID(), cbGiftCardWish.getID(),
													       dc_DecemberMealCutoff.getTime(),
													        dc_WaitlistGiftCutoff.getTime(),
													        (Integer) numGiftsPerChildCB.getSelectedItem(),
													         getWishIntakeConfiguration(),
													         ((GiftDistribution) giftDistributionCB.getSelectedItem()),
													         ((MealIntake) mealIntakeCB.getSelectedItem()).index());
			
			String response = gvDB.update(this, updateGVreq);
			if(!response.startsWith("UPDATED_GLOBALS"))
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied global update," +
						"try again later","Global Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvDB.getImageIcon(0));
			}
			else
			{
				display(gvDB.getServerGVs(), currUserPrefs);
			}
		}
	}
	
	void setEnabledRestrictedPrefrences(boolean tf)
	{
		dc_delivery.setEnabled(tf);
		dc_giftsreceived.setEnabled(tf);
		dc_seasonstart.setEnabled(tf);
		dc_ThanksgivingMealCutoff.setEnabled(tf);
		dc_DecemberGiftCutoff.setEnabled(tf);
		dc_DecemberMealCutoff.setEnabled(tf);
		dc_WaitlistGiftCutoff.setEnabled(tf);
		dc_InfoEditCutoff.setEnabled(tf);
		whStreetNumTF.setEnabled(tf);
		whStreetTF.setEnabled(tf);
		whCityTF.setEnabled(tf);
		whStateTF.setEnabled(tf);
		numGiftsPerChildCB.setEnabled(tf);
		defaultGiftCB.setEnabled(tf);
		defaultGiftCardCB.setEnabled(tf);
		wishType0BeforeCB.setEnabled(tf);
		wishType1BeforeCB.setEnabled(tf);
		wishType2BeforeCB.setEnabled(tf);
		giftDistributionCB.setEnabled(tf);
		mealIntakeCB.setEnabled(tf);
		
	}
	
	void checkApplyChangesEnabled()
	{
		if(gvDB.getSeasonStartDate() != dc_seasonstart.getTime() || 
			gvDB.getDeliveryDateMillis() != dc_delivery.getTime() || //.getDate().getTime() ||
			gvDB.getGiftsReceivedDate() != dc_giftsreceived.getTime() ||
			gvDB.getThanksgivingMealDeadline() != dc_ThanksgivingMealCutoff.getTime() ||
			gvDB.getDecemberGiftDeadline() != dc_DecemberGiftCutoff.getTime() ||
			gvDB.getDecemberMealDeadline() != dc_DecemberMealCutoff.getTime() ||
			gvDB.getWaitlistGiftDeadline() != dc_WaitlistGiftCutoff.getTime() ||
			gvDB.getFamilyEditDeadline() != dc_InfoEditCutoff.getTime())
		{			
			btnApplyDateChanges.setEnabled(true);
		}
		else
			btnApplyDateChanges.setEnabled(false);
		
		if(!whStreetNum.equals(whStreetNumTF.getText()) ||
			!whStreet.equals(whStreetTF.getText()) ||
			!whCity.equals(whCityTF.getText()) ||
			!whState.equals(whStateTF.getText()))
		{
			btnApplyAddressChanges.setEnabled(true);
		}
		else
			btnApplyAddressChanges.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!bIgnoreDialogEvents && e.getSource()==btnApplyDateChanges)
			update();
		else if(!bIgnoreDialogEvents && e.getSource()==btnApplyAddressChanges)
			update();
		else if(!bIgnoreDialogEvents && e.getSource() == oncFontSizeCB && 
				userDB.getLoggedInUser().getPreferences().getFontSize() != (Integer) oncFontSizeCB.getSelectedItem())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource() == barcodeCkBox)
			gvDB.setIncludeBarcodeOnLabels(barcodeCkBox.isSelected());
		else if(!bIgnoreDialogEvents && e.getSource() == barcodeCB && gvDB.getBarcodeCode() != barcodeCB.getSelectedItem())
			gvDB.setBarcode( (Barcode) barcodeCB.getSelectedItem());
		else if(!bIgnoreDialogEvents && e.getSource() == wishAssigneeFilterDefaultCB &&
				userDB.getLoggedInUser().getPreferences().getWishAssigneeFilter() != wishAssigneeFilterDefaultCB.getSelectedIndex())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource() == fdnsFilterDefaultCB &&
				userDB.getUserPreferences().getFamilyDNSFilterCode().getID() != ((DNSCode) fdnsFilterDefaultCB.getSelectedItem()).getID())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(defaultGiftCB) &&
				((ONCGift) defaultGiftCB.getSelectedItem()).getID() != gvDB.getDefaultGiftID())
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(defaultGiftCardCB) &&
				((ONCGift) defaultGiftCardCB.getSelectedItem()).getID() != gvDB.getDefaultGiftCardID())
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(numGiftsPerChildCB) &&
				((Integer) numGiftsPerChildCB.getSelectedItem()) != gvDB.getNumberOfGiftsPerChild())
		{
			checkEnabledMealsAndWishTypes((Integer) numGiftsPerChildCB.getSelectedItem(), gvDB.getNumberOfGiftsPerChild());
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType0BeforeCB) &&
				((WishIntakeType) wishType0BeforeCB.getSelectedItem()) != 
					getWishIntakeType(0, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType1BeforeCB) &&
				((WishIntakeType) wishType1BeforeCB.getSelectedItem()) !=
					getWishIntakeType(1, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType2BeforeCB) &&
				((WishIntakeType) wishType2BeforeCB.getSelectedItem()) !=
					getWishIntakeType(2, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{		
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType3BeforeCB) &&
				((WishIntakeType) wishType3BeforeCB.getSelectedItem()) !=
					getWishIntakeType(3, WishIntakeTiming.BEFORE_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{		
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType0AfterCB) &&
				((WishIntakeType) wishType0AfterCB.getSelectedItem()) != 
					getWishIntakeType(0, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType1AfterCB) &&
				((WishIntakeType) wishType1AfterCB.getSelectedItem()) !=
					getWishIntakeType(1, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType2AfterCB) &&
				((WishIntakeType) wishType2AfterCB.getSelectedItem()) !=
					getWishIntakeType(2, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{	
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(wishType3AfterCB) &&
				((WishIntakeType) wishType3AfterCB.getSelectedItem()) !=
					getWishIntakeType(3, WishIntakeTiming.AFTER_DEADLINE, gvDB.getChildWishIntakeConfiguration()))
		{
				
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(giftDistributionCB) && 
				((GiftDistribution)giftDistributionCB.getSelectedItem()) != gvDB.getGiftDistribution())
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(mealIntakeCB) && 
				((MealIntake)mealIntakeCB.getSelectedItem()).index() != gvDB.getMealIntake())
		{
			update();
		}
//		else if(!bIgnoreDialogEvents && e.getSource().equals(deliveryActivityCB) &&
//				((Activity) deliveryActivityCB.getSelectedItem()).getID() != gvDB.getDeliveryActivityID())
//		{
//			update();
//		}
		else if(e.getSource().equals(btnImportSignUpList))
		{
			activityDB.requestGeniusSignUps();
		}
		else if(e.getSource().equals(signUpImportCkBox))
		{
			GeniusSignUps reqUpdateGSU = new GeniusSignUps(geniusSignUps);
			reqUpdateGSU.setSignUpImportEnabled(signUpImportCkBox.isSelected());
			String response = activityDB.updateGeniusSignUps(this, reqUpdateGSU);
			
			if(response != null && response.startsWith("UPDATED_GENIUS_SIGNUPS"))
			{
				Gson gson = new Gson();
				geniusSignUps = gson.fromJson(response.substring(22), GeniusSignUps.class);	
			}
			
			displaySignUpData();
		}
	}
	
	void updateUserPreferences()
	{
		ONCUser updateUserReq = new ONCUser(userDB.getLoggedInUser());
		UserPreferences userPrefs = updateUserReq.getPreferences();
		
		userPrefs.setFontSize((Integer)oncFontSizeCB.getSelectedItem());
		userPrefs.setWishAssigneeFilter(wishAssigneeFilterDefaultCB.getSelectedIndex());
		userPrefs.setFamilyDNSFilterCode((DNSCode) fdnsFilterDefaultCB.getSelectedItem());
		
		String response = UserDB.getInstance().update(this, updateUserReq);
		if(!response.startsWith("UPDATED_USER"))
		{
			//we have a failure, display the original preferences
			display(currServerGVs, currUserPrefs);
		}
	}
	
	private void updateDefalutGiftCBLists(boolean bInitialize)
	{	
		//remove the action listener for each CB
		defaultGiftCB.removeActionListener(this);
		defaultGiftCardCB.removeActionListener(this);
		
		//archive the default gift selection
		ONCGift curr_defalut_gift_sel;
		if(bInitialize &&  gvDB.getDefaultGiftID() > -1)
			curr_defalut_gift_sel = (ONCGift) catDB.getGiftByID(gvDB.getDefaultGiftID());
		else
			curr_defalut_gift_sel = (ONCGift) defaultGiftCB.getSelectedItem();
			
		int selDefalutGiftIndex = 0;
		
		//update the default gift CBM
		defaultGiftCBM.removeAllElements();
		int index = 0;
		for(ONCGift w : catDB.getDefaultGiftList())
		{
			defaultGiftCBM.addElement(w);
			if(curr_defalut_gift_sel.getID() == w.getID())
				selDefalutGiftIndex = index;
				
			index++;
		}
		
		//restore a prior selection if there was one
		defaultGiftCB.setSelectedIndex(selDefalutGiftIndex); //Keep current selection
		
		//update the default gift card gift/wish
		//archive the default gift card selection
		ONCGift curr_defalut_gift_card_sel;
		if(bInitialize &&  gvDB.getDefaultGiftCardID() > -1)
			curr_defalut_gift_card_sel = (ONCGift) catDB.getGiftByID(gvDB.getDefaultGiftCardID());
		else
			curr_defalut_gift_card_sel = (ONCGift) defaultGiftCardCB.getSelectedItem();
		
		int selDefaultGiftCardIndex = 0;
		//update the default gift card CBM
		defaultGiftCardCBM.removeAllElements();
		
		index = 0;
		for(ONCGift w : catDB.getDefaultGiftList())
		{
			defaultGiftCardCBM.addElement(w);
			if(curr_defalut_gift_card_sel.getID() == w.getID())
				selDefaultGiftCardIndex = index;
				
			index++;
		}
		
		//restore a prior selection if there was one
		defaultGiftCardCB.setSelectedIndex(selDefaultGiftCardIndex); //Keep current selection
		
		//restore the action listeners
		defaultGiftCB.addActionListener(this);
		defaultGiftCardCB.addActionListener(this);
	}
	
	/***
	 * Decodes 8 wish in-take settings, each with 4 (2 bits) choices. 16 bits are used to encode 
	 * the choice for each in-take setting. The setting order, from left to right is:
	 * Wish 3 After Deadline bits(15,14) 
	 * Wish 3 Before Deadline(bits 13,12) 
	 * Wish 2 After Deadline (bits 11,10),
	 * Wish 2 Before Deadline (bits 9,8),
	 * Wish 1 After Deadline bits(7,6) 
	 * Wish 1 Before Deadline(bits 5,4) 
	 * Wish 0 After Deadline (bits 3,2),
	 * Wish 0 Before Deadline (bits 1,0),
	 * @return WishIntakeType
	 */
	WishIntakeType getWishIntakeType(int wishnum, WishIntakeTiming timing, int configuration)
	{
		if(wishnum >= 0 && wishnum < 4)
		{	
			//determine WishIntakeType by calculating how many bits to shift right based on the wish number
			//and if before or after the deadline. Use same info to create the mask.
			//apply the mask, shift the result, yielding an integer between 0 and 3
			int shift = ((wishnum * 2) + timing.offset()) * 2;	//and even integer between 0 and 14
			int mask = 0x0003 << shift;	
			int intakeType = (configuration & mask) >> shift;
			
			return WishIntakeType.getWishInputType(intakeType);
		}
		else
			return WishIntakeType.Unused;
	}
	
	/***
	 * Encodes 8 wish in-take settings, each with 4 (2 bits) choices. 16 bits are used to encode 
	 * the choice for each in-take setting. The setting order, from left to right is:
	 * Wish 3 After Deadline bits(15,14) 
	 * Wish 3 Before Deadline(bits 13,12) 
	 * Wish 2 After Deadline (bits 11,10),
	 * Wish 2 Before Deadline (bits 9,8),
	 * Wish 1 After Deadline bits(7,6) 
	 * Wish 1 Before Deadline(bits 5,4) 
	 * Wish 0 After Deadline (bits 3,2),
	 * Wish 0 Before Deadline (bits 1,0),
	 * @return int where lower 16 bits represented the encoded settings and choices
	 */
	int getWishIntakeConfiguration()
	{
		int config = 0, statusIndex=0;
		
		statusIndex = ((WishIntakeType)wishType3AfterCB.getSelectedItem()).statusIndex;
		config = config | statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType3BeforeCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType2AfterCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType2BeforeCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType1AfterCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType1BeforeCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType0AfterCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		config = config << 2;
		statusIndex = ((WishIntakeType)wishType0BeforeCB.getSelectedItem()).statusIndex;
		config |= statusIndex;
		
		return config;
	}
/*	
	void updateDefaultDeliveryActivityCBList(boolean bInitialize)
	{	
		//remove the action listener
		deliveryActivityCB.removeActionListener(this);
		
		//archive the default delivery activity selection
		Activity curr_defalut_act_sel;
		if(bInitialize &&  gvDB.getDeliveryActivityID() > -1)
			curr_defalut_act_sel = (Activity) activityDB.getActivity(gvDB.getDeliveryActivityID());
		else
			curr_defalut_act_sel = (Activity) deliveryActivityCB.getSelectedItem();
			
		int selDefalutDeliveryActivityIndex = 0;
		
		//update the default delivery activity CBM
		deliveryActivityCBM.removeAllElements();
		int index = 0;
		@SuppressWarnings("unchecked")
		List<Activity> activityList = (List<Activity>) activityDB.getList();
		
		deliveryActivityCBM.addElement(new Activity(-1, "None")); //creates a dummy Activity with name "None", id = -1;
		for(Activity a: activityList)
		{
			deliveryActivityCBM.addElement(a);
			if(curr_defalut_act_sel.getID() == a.getID())
				selDefalutDeliveryActivityIndex = index;
				
			index++;
		}
		
		//restore a prior selection if there was one
		deliveryActivityCB.setSelectedIndex(selDefalutDeliveryActivityIndex); //Keep current selection
		
		//restore the action listeners
		deliveryActivityCB.addActionListener(this);
	}
*/
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			checkEnabledMealsAndWishTypes((Integer) gvDB.getNumberOfGiftsPerChild(), (Integer) numGiftsPerChildCB.getSelectedItem());
			display(gvDB.getServerGVs(), currUserPrefs);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") || 
				dbe.getType().equals("CHANGED_USER")))	
		{
			//verify one of the user preferences has changed for the current user
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			if(updatedUser != null && userDB.getLoggedInUser().getID() == updatedUser.getID() &&
				(updatedUser.getPreferences().getWishAssigneeFilter() != wishAssigneeFilterDefaultCB.getSelectedIndex() ||
				 updatedUser.getPreferences().getFamilyDNSFilterCode().getID() != ((DNSCode) fdnsFilterDefaultCB.getSelectedItem()).getID()) ||
				 updatedUser.getPreferences().getFontSize() != (Integer)oncFontSizeCB.getSelectedItem())
			{
				display(null, updatedUser.getPreferences());
			}
			
			//we have a user, so enable changing preferences
			oncFontSizeCB.setEnabled(true);
			wishAssigneeFilterDefaultCB.setEnabled(true);
			fdnsFilterDefaultCB.setEnabled(true);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_SIGNUPS"))
		{
			if(dbe.getObject1() != null)
			{
				geniusSignUps = (GeniusSignUps) dbe.getObject1();
				displaySignUpData();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_SIGNUP"))
		{
			displaySignUpData();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GENIUS_SIGNUPS"))
		{
			geniusSignUps = (GeniusSignUps) dbe.getObject1();
			displaySignUpData();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child  %d User & Season Settings", gvDB.getCurrentSeason()));
			updateDefalutGiftCBLists(true);
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG_WISH"))
		{
			updateDefalutGiftCBLists(false);
		}
	}
	
	private class DateChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(!bIgnoreDialogEvents && "date".equals(pce.getPropertyName()))
			{
				checkApplyChangesEnabled();
//				if(pce.getSource() == dc_delivery.getDateEditor())
//				{
//					System.out.println(String.format("PrefDlg.DateChangeList: delivery time= %d, delivery time UTC = %d",
//						 dc_delivery.getDate().getTime(), dc_delivery.getTime()));
//				}
			}
		}
	}
	
	/***********************************************************************************
	 * This class implements a key listener for the  that
	 * listens a warehouse address text field to determine when it has changed. When it has
	 * changed, it calls the checkApplyChangesEnabled method
	 ***********************************************************************************/
	 protected class AddressKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent ke) 
		{
			// TODO Auto-generated method stub		
		}
		@Override
		public void keyReleased(KeyEvent ke)
		{
			checkApplyChangesEnabled();
		}
		@Override
		public void keyTyped(KeyEvent ke)
		{
			
		}
	 }
	 
	 private class SpinnerChangeListener implements ChangeListener
	 {
		@Override
		public void stateChanged(ChangeEvent ce)
		{
			if(ce.getSource() == averyXOffsetSpinner || ce.getSource() == averyYOffsetSpinner)
			{
				Point offset = new Point((Integer)averyXOffsetSpinner.getValue(), 
										 (Integer)averyYOffsetSpinner.getValue());
				
				gvDB.setAveryLabelOffset(offset);
			}
		}
	 }
	private class SignUpTableModel extends AbstractTableModel
	{
		/**
		* 
		*/
		private static final long serialVersionUID = 1L;
			
		public String[] columnNames = {"List of SignUps From SignUpGenius", "Type", "Last Import", "Expires", "Import Freq."};
			
		@Override
		public String getColumnName(int col) { return columnNames[col]; }
			
		@Override
		public int getColumnCount() { return columnNames.length; }

		@Override
		public int getRowCount() { return geniusSignUps.getSignUpList().size(); }

		@Override
		public Object getValueAt(int row, int col)
		{
			SignUp su = geniusSignUps.getSignUpList().get(row);
			if(col == TITLE_COL)
				return su.getTitle();
			else if(col == TYPE_COL)
				return su.getSignUpType();
			else if(col == LAST_IMPORT_COL)
				return su.getLastImportTime().getTime();
			else if(col == EXPIRES_COL)
				return su.getEndTime().getTime();
			else if(col == FREQ_COL)
				return su.getFrequency();
			else
				return "Error";
		}
		public Class<?> getColumnClass(int column)
        {
        		if(column == FREQ_COL)
        			return Frequency.class;
        		else if(column == TYPE_COL)
        			return SignUpType.class;
        		else if(column == LAST_IMPORT_COL || column == EXPIRES_COL)
        			return Date.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		SignUp su = geniusSignUps.getSignUpList().get(row);
        		
        		//frequency can't change unless import is enabled, the sign-up hasn't expired and the type is set to
        		//either Volunteer or CLothing
        		if(geniusSignUps.isImportEnabled() && col == FREQ_COL &&
        			System.currentTimeMillis() < su.getEndtimeInMillis() &&
        			 (su.getSignUpType() == SignUpType.Volunteer || su.getSignUpType() == SignUpType.Clothing ||
        			  su.getSignUpType() == SignUpType.Coat))
        			
//        				&& (su.getSignUpType() == SignUpType.Volunteer || su.getSignUpType() == SignUpType.Clothing))
        			return true;
        		//type can only be set if the sign up has never been imported.
        		else if(col == TYPE_COL && geniusSignUps.getSignUpList().get(row).getLastImportTimeInMillis() == 0)
        			return true;
        		else
        			return false;
        }

        public void setValueAt(Object value, int row, int col)
        { 
        		SignUp signUp = geniusSignUps.getSignUpList().get(row);
        		
        		//determine if the user made a change to a user object
        		if(col == FREQ_COL && signUp.getFrequency() != ((Frequency)value))
        		{
        			SignUp reqUpdateSU = new SignUp(signUp);	//make a copy of current signUp
        			reqUpdateSU.setFrequency((Frequency) value);
        		
        			//if the user made a change in the table, attempt to update the signUp object in
        			//the local user data base
        			String response = activityDB.updateSignUp(this, reqUpdateSU);
        			if(response != null && response.startsWith("UPDATED_SIGNUP"))
        			{
        				//request succeeded
        				signUp.setFrequency(reqUpdateSU.getFrequency());
        			}
        			else
        			{
        				//request failed
        				String err_mssg = "ONC Server denied update signup request, try again later";
        				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), err_mssg, "Update SignUp Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
        				displaySignUpData();	
        			}
        		}
        		else if(col == TYPE_COL && signUp.getSignUpType() != ((SignUpType)value))
        		{
        			SignUp reqUpdateSU = new SignUp(signUp);	//make a copy of current signUp
        			reqUpdateSU.setSignUpType((SignUpType) value);
        		
        			//if the user made a change in the table, attempt to update the signUp object in
        			//the local user data base
        			String response = activityDB.updateSignUp(this, reqUpdateSU);
        			if(response != null && response.startsWith("UPDATED_SIGNUP"))
        			{
        				//request succeeded
        				signUp.setSignUpType(reqUpdateSU.getSignUpType());
        			}
        			else
        			{
        				//request failed
        				String err_mssg = "ONC Server denied update signup request, try again later";
        				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), err_mssg, "Update SignUp Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
        				displaySignUpData();	
        			}
        		}
        }		
	}
	
	private class ONCDateChooser extends JDateChooser
	{	
		/**
		 *  Wraps a JDateChooser such that the time is reduced to a year, month and day at midnight UTC.
		 * @author johnoneil
		 */
		private static final long serialVersionUID = 1L;

		ONCDateChooser()
		{
			super();
		}
		
		void setTime(long time)
		{
			ServerGVs serverGVs = gvDB.getServerGVs();
			String beforeAfterSeasonStart = "Before";
			if(serverGVs.isAfterSeasonStartDate())
				beforeAfterSeasonStart = "After";
			
			this.setToolTipText(String.format("%s season start= %d", beforeAfterSeasonStart, time));
			
			//gives you the current offset in ms from GMT at the current date
			TimeZone tz = TimeZone.getDefault();	//Local time zone
			int offsetFromUTC = tz.getOffset(time);

			//create a new calendar in local time zone, set to gmtDOB and add the offset
			Calendar localCal = Calendar.getInstance();
			localCal.setTimeInMillis(time);
			localCal.add(Calendar.MILLISECOND, (offsetFromUTC * -1));

			this.setCalendar(localCal);
		}
		
		long getTime() 
		{
			Calendar delCal = this.getCalendar();
			TimeZone tz = delCal.getTimeZone();		
			return delCal.getTimeInMillis() + tz.getOffset(delCal.getTimeInMillis());
		}
	}
	
	private enum WishIntakeTiming 
	{ 
		BEFORE_DEADLINE (0),
		AFTER_DEADLINE (1);
		
		private final int offset;
		
		WishIntakeTiming(int offset)
		{
			this.offset = offset;
		}
		
		int offset() { return this.offset; }
	}
	
	private enum WishIntakeType
	{
		Unused (0, "Unused"),
		Age_Appropiate (1, "Age Appropriate"),
		ONC_Selected(2, "ONC Selected"),
		Agent_Selected (3,"Agent Selected");
		
		private final int statusIndex;
		private final String english;
		
		WishIntakeType(int statusIndex, String english)
		{
			this.statusIndex = statusIndex;
			this.english = english;
		}
		
		public static WishIntakeType getWishInputType(int statusIndex)
		{
			WishIntakeType result = WishIntakeType.Unused;
			for(WishIntakeType cwit : WishIntakeType.values())
			{
				if(cwit.statusIndex == statusIndex)
				{
					result = cwit;
					break;
				}
			}
			
			return result;
		}
		
		public String toString() { return this.english; }
	}	
	
	private enum MealIntake
	{
		None (0, "None"),
		Thanksgiving (1, "Thanksgiving"),
		December (2,"December"),
		Thanksgiving_December (3, "Thanksgiving & December");
		
		private final int index;
		private final String english;
		
		MealIntake(int index, String english)
		{
			this.index = index;
			this.english = english;
		}
		
		int index() { return this.index; }
		@Override
		public String toString() { return english; } 
		
		static MealIntake intake(int i)
		{
			MealIntake result = MealIntake.None;
			for(MealIntake mi : MealIntake.values())
			{
				if(mi.index == i)
				{
					result = mi;
					break;
				}
			}
			return result; 
		}
	}
}
