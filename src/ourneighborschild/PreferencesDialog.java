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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
	private UserDB userDB;
	private ActivityDB activityDB;
	private WishCatalogDB catDB;
	
	private UserPreferences uPrefs;
	
	GeniusSignUps geniusSignUps;
	
	private JDateChooser dc_today, dc_delivery, dc_seasonstart, dc_giftsreceived;
	private JDateChooser dc_DecemberGiftCutoff, dc_InfoEditCutoff, dc_ThanksgivingMealCutoff;
	private JDateChooser dc_DecemberMealCutoff, dc_WaitlistGiftCutoff;
	private JTextField whStreetNumTF, whStreetTF, whCityTF, whStateTF;
	private String whStreetNum, whStreet,whCity, whState;
	public JComboBox<Integer> oncFontSizeCB;
	private JComboBox<ONCWish> defaultGiftCB, defaultGiftCardCB;
	private JComboBox<Activity> deliveryActivityCB;
	private DefaultComboBoxModel<ONCWish> defaultGiftCBM, defaultGiftCardCBM;
	private DefaultComboBoxModel<Activity> deliveryActivityCBM;
	private JButton btnApplyDateChanges, btnApplyAddressChanges;
	private boolean bIgnoreDialogEvents;
	private JCheckBox barcodeCkBox, signUpImportCkBox;
	private JComboBox<String> wishAssigneeFilterDefaultCB, fdnsFilterDefaultCB;
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
		this.setTitle("Our Neighbor's Child Elf & Season Settings");
		
		gvDB = GlobalVariablesDB.getInstance();
		if(gvDB != null)
			gvDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		catDB = WishCatalogDB.getInstance();
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
		String[] options = {"None", "Any"};
		fdnsFilterDefaultCB = new JComboBox<String>(options);
		fdnsFilterDefaultCB.setSelectedIndex(0);
		fdnsFilterDefaultCB.setEnabled(false);
		fdnsFilterDefaultCB.addActionListener(this);
		fdfPanel.add(lblFDF);
		fdfPanel.add(fdnsFilterDefaultCB);
		
		userSettingsPanel.add(fontPanel);
		userSettingsPanel.add(wafdPanel);
		userSettingsPanel.add(fdfPanel);
		
		tabbedPane.addTab("User Filters/Font", userSettingsPanel);
		
		//set up the season dates tab
		JPanel dateTab = new JPanel();
		dateTab.setLayout(new BoxLayout(dateTab, BoxLayout.Y_AXIS));
		
		JPanel datePanelTop = new JPanel();
		datePanelTop.setLayout(new FlowLayout(FlowLayout.LEFT));
//		datePanel.setLayout(new GridLayout(3,3));
		DateChangeListener dcl = new DateChangeListener();	//listener for all date changes
		
		Dimension dateSize = new Dimension(200, 56);
		dc_today = new JDateChooser(gvDB.getTodaysDate());
		dc_today.setPreferredSize(dateSize);
		dc_today.setBorder(BorderFactory.createTitledBorder("Today's Date"));
		dc_today.setEnabled(false);
		dc_today.getDateEditor().addPropertyChangeListener(dcl); 
//		datePanelTop.add(dc_today);
		
		dc_seasonstart = new JDateChooser(gvDB.getSeasonStartDate());
		dc_seasonstart.setPreferredSize(dateSize);
		dc_seasonstart.setBorder(BorderFactory.createTitledBorder("Season Start Date"));
		dc_seasonstart.setEnabled(false);
		dc_seasonstart.getDateEditor().addPropertyChangeListener(dcl);	
		datePanelTop.add(dc_seasonstart);
		
		dc_delivery = new JDateChooser(gvDB.getDeliveryDate());
		dc_delivery.setPreferredSize(dateSize);
		dc_delivery.setBorder(BorderFactory.createTitledBorder("Delivery Date"));
		dc_delivery.setEnabled(false);
		dc_delivery.getDateEditor().addPropertyChangeListener(dcl);
		datePanelTop.add(dc_delivery);

		dc_InfoEditCutoff = new JDateChooser();
		dc_InfoEditCutoff.setPreferredSize(dateSize);
		dc_InfoEditCutoff.setBorder(BorderFactory.createTitledBorder("Family Update Deadline"));
		dc_InfoEditCutoff.setEnabled(false);
		dc_InfoEditCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelTop.add(dc_InfoEditCutoff);
		
		JPanel datePanelMid = new JPanel();
		datePanelMid.setLayout(new FlowLayout(FlowLayout.LEFT));
		dc_ThanksgivingMealCutoff = new JDateChooser();
		dc_ThanksgivingMealCutoff.setPreferredSize(dateSize);
		dc_ThanksgivingMealCutoff.setBorder(BorderFactory.createTitledBorder("Thanksgiving Meal Deadline"));
		dc_ThanksgivingMealCutoff.setEnabled(false);
		dc_ThanksgivingMealCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelMid.add(dc_ThanksgivingMealCutoff);
		
		dc_DecemberMealCutoff = new JDateChooser();
		dc_DecemberMealCutoff.setPreferredSize(dateSize);
		dc_DecemberMealCutoff.setBorder(BorderFactory.createTitledBorder("December Meal Deadline"));
		dc_DecemberMealCutoff.setEnabled(false);
		dc_DecemberMealCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanelMid.add(dc_DecemberMealCutoff);
		
		JPanel datePanelBottom = new JPanel();
		datePanelBottom.setLayout(new FlowLayout(FlowLayout.LEFT));
		dc_DecemberGiftCutoff = new JDateChooser();
		dc_DecemberGiftCutoff.setPreferredSize(dateSize);
		dc_DecemberGiftCutoff.setBorder(BorderFactory.createTitledBorder("December Gift Deadline"));
		dc_DecemberGiftCutoff.setEnabled(false);
		dc_DecemberGiftCutoff.getDateEditor().addPropertyChangeListener(dcl);
		datePanelBottom.add(dc_DecemberGiftCutoff);
				
		dc_WaitlistGiftCutoff = new JDateChooser();
		dc_WaitlistGiftCutoff.setPreferredSize(dateSize);
		dc_WaitlistGiftCutoff.setBorder(BorderFactory.createTitledBorder("Waitlist Gift Deadline"));
		dc_WaitlistGiftCutoff.setEnabled(false);
		dc_WaitlistGiftCutoff.getDateEditor().addPropertyChangeListener(dcl);
		datePanelBottom.add(dc_WaitlistGiftCutoff);
		
		dc_giftsreceived = new JDateChooser(gvDB.getGiftsReceivedDate());
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
		
		tabbedPane.addTab("Season Dates", dateTab);
		
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
		
		//set up the ornament label tab
		JPanel wishlabelPanel = new JPanel();
		wishlabelPanel.setLayout(new BoxLayout(wishlabelPanel, BoxLayout.Y_AXIS));
		JPanel wishlabelPanelTop = new JPanel();
		JPanel wishlabelPanelMiddle = new JPanel();
		JPanel wishlabelPanelBottom = new JPanel();
		
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
		
		defaultGiftCB = new JComboBox<ONCWish>();
		defaultGiftCBM = new DefaultComboBoxModel<ONCWish>();
		
		defaultGiftCBM.addElement(new ONCWish(-1, "None", 7));//creates a dummy gift with name "None", id = -1;
		defaultGiftCB.setModel(defaultGiftCBM);
		defaultGiftCB.setPreferredSize(new Dimension(180, 56));
		defaultGiftCB.setBorder(BorderFactory.createTitledBorder("Default Gift"));
		defaultGiftCB.addActionListener(this);
		wishlabelPanelBottom.add(defaultGiftCB);
		
		defaultGiftCardCB = new JComboBox<ONCWish>();
		defaultGiftCardCBM = new DefaultComboBoxModel<ONCWish>();
		
		defaultGiftCardCBM.addElement(new ONCWish(-1, "None", 7));//creates a dummy gift with name "None", id = -1;
		defaultGiftCardCB.setModel(defaultGiftCardCBM);
		defaultGiftCardCB.setPreferredSize(new Dimension(180, 56));
		defaultGiftCardCB.setBorder(BorderFactory.createTitledBorder("Default Gift Card"));
		defaultGiftCardCB.addActionListener(this);
		wishlabelPanelBottom.add(defaultGiftCardCB);
		
		wishlabelPanel.add(wishlabelPanelTop);
		wishlabelPanel.add(wishlabelPanelMiddle);
		wishlabelPanel.add(wishlabelPanelBottom);
		
		tabbedPane.addTab("Ornament Labels", wishlabelPanel);
		
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
		
		JPanel deliveryActivityPanel = new JPanel();
		deliveryActivityPanel.setLayout(new BoxLayout(deliveryActivityPanel, BoxLayout.X_AXIS));
		deliveryActivityPanel.setBorder(BorderFactory.createTitledBorder("Delivery Activity"));
		
		JLabel lblDeliveryActivity = new JLabel("Set the Delivery Activity:");
		deliveryActivityPanel.add(lblDeliveryActivity);
		
		deliveryActivityCB = new JComboBox<Activity>();
		deliveryActivityCBM = new DefaultComboBoxModel<Activity>();
		
		deliveryActivityCBM.addElement(new Activity(-1, "None"));//creates a dummy Activity with name "None", id = -1;
		deliveryActivityCB.setModel(deliveryActivityCBM);
		deliveryActivityCB.setPreferredSize(new Dimension(180, 56));
		deliveryActivityCB.setToolTipText("Used to determine what volunteers are making deliveries");
		deliveryActivityCB.addActionListener(this);
		deliveryActivityPanel.add(deliveryActivityCB);
		
		deliveryActivityPanel.add(Box.createRigidArea(new Dimension(240,0)));
		
		//add the table scroll pane to the symbol panel
		geniusPanel.add(importPanel);
		geniusPanel.add(signUpScrollPane);
		geniusPanel.add(geniusControlPanel);
		geniusPanel.add(deliveryActivityPanel);
				
		tabbedPane.addTab("SignUpGenius", geniusPanel);
		
		//Add the components to the frame pane
        this.getContentPane().add(tabbedPane);
               
        this.pack();
        this.setMinimumSize(new Dimension(720, 160));
        btnApplyDateChanges.requestFocusInWindow();
	}
	
	void display(UserPreferences uPrefs)	//Update gui with preference data changes (called when an saved ONC object loaded)
	{
		if(uPrefs != null)
			this.uPrefs = uPrefs;
		
		bIgnoreDialogEvents = true;
		
		dc_today.setDate(gvDB.getTodaysDate());
		dc_delivery.setDate(gvDB.getDeliveryDate());
		dc_seasonstart.setDate(gvDB.getSeasonStartDate());
		dc_giftsreceived.setDate(gvDB.getGiftsReceivedDate());
		dc_ThanksgivingMealCutoff.setDate(gvDB.getThanksgivingMealDeadline());
		dc_DecemberMealCutoff.setDate(gvDB.getDecemberMealDeadline());
		dc_DecemberGiftCutoff.setDate(gvDB.getDecemberGiftDeadline());
		dc_WaitlistGiftCutoff.setDate(gvDB.getWaitlistGiftDeadline());
		dc_InfoEditCutoff.setDate(gvDB.getFamilyEditDeadline());
		
		if(gvDB.getDefaultGiftID() > -1 && catDB.size() > 0)
		{
			ONCWish defaultWish = catDB.getWishByID(gvDB.getDefaultGiftID());
			if(defaultWish != null)
				defaultGiftCB.setSelectedItem(defaultWish);
			else
				defaultGiftCB.setSelectedIndex(0);	
		}
		else
			defaultGiftCB.setSelectedIndex(0);
		
		if(gvDB.getDefaultGiftCardID() > -1 && catDB.size() > 0)
		{
			ONCWish defaultGiftCardWish = catDB.getWishByID(gvDB.getDefaultGiftCardID());
			if(defaultGiftCardWish != null)
				defaultGiftCardCB.setSelectedItem(defaultGiftCardWish);
			else
				defaultGiftCardCB.setSelectedIndex(0);	
		}
		else
			defaultGiftCardCB.setSelectedIndex(0);
		
		if(gvDB.getDeliveryActivityID() > -1 && activityDB.size() > 0)
		{
			Activity defaultDeliveryActivity = activityDB.getActivity(gvDB.getDeliveryActivityID());
			if(defaultDeliveryActivity != null)
				deliveryActivityCB.setSelectedItem(defaultDeliveryActivity);
			else
				deliveryActivityCB.setSelectedIndex(0);	
		}
		else
			defaultGiftCardCB.setSelectedIndex(0);
		
		displayWarehouseAddress();
		
		ONCUser user = userDB.getLoggedInUser();
		if(user != null)
		{
//			System.out.println(String.format("PrefDlg.display: font: %d, wishIndex: %d, dnsIndex: %d",
//					user.getPreferences().getFontSize(),
//					user.getPreferences().getWishAssigneeFilter(),
//					user.getPreferences().getFamilyDNSFilter()));
			
			oncFontSizeCB.setSelectedItem(user.getPreferences().getFontSize());
			wishAssigneeFilterDefaultCB.setSelectedIndex(user.getPreferences().getWishAssigneeFilter());
			fdnsFilterDefaultCB.setSelectedIndex(user.getPreferences().getFamilyDNSFilter());
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
		if(!gvDB.getSeasonStartDate().equals(dc_seasonstart.getDate())) { cf |= 1;}
		if(!gvDB.getDeliveryDate().equals(dc_delivery.getDate())) { cf |= 2; }
		if(!gvDB.getWarehouseAddress().equals(getWarehouseAddressInGoogleMapsFormat())) {cf |= 4;}
		if(!gvDB.getGiftsReceivedDate().equals(dc_giftsreceived.getDate())) {cf |= 8;}
		if(!gvDB.getThanksgivingMealDeadline().equals(dc_ThanksgivingMealCutoff.getDate())) {cf |= 16;}
		if(!gvDB.getDecemberGiftDeadline().equals(dc_DecemberGiftCutoff.getDate())) {cf |= 32;}
		if(!gvDB.getFamilyEditDeadline().equals(dc_InfoEditCutoff.getDate())) {cf |= 64;}
		if(!gvDB.getDecemberMealDeadline().equals(dc_DecemberMealCutoff.getDate())) {cf |= 128;}
		if(!gvDB.getWaitlistGiftDeadline().equals(dc_WaitlistGiftCutoff.getDate())) {cf |= 256;}
		
		ONCWish cbWish = (ONCWish) defaultGiftCB.getSelectedItem();
		if(gvDB.getDefaultGiftID() != cbWish.getID()) {cf |= 512;}
		
		ONCWish cbGiftCardWish = (ONCWish) defaultGiftCardCB.getSelectedItem();
		if(gvDB.getDefaultGiftCardID() != cbGiftCardWish.getID()) {cf |= 1024;}
		
		Activity cbDefaultDeliveryActivity = (Activity) deliveryActivityCB.getSelectedItem();
		if(gvDB.getDeliveryActivityID() != cbDefaultDeliveryActivity.getID()) {cf |= 2048;}
		
		
		if(cf > 0)
		{
			ServerGVs updateGVreq = new ServerGVs(dc_delivery.getDate(), 
													dc_seasonstart.getDate(), 
													 getWarehouseAddressInGoogleMapsFormat(),
													  dc_giftsreceived.getDate(),
													   dc_ThanksgivingMealCutoff.getDate(),
													    dc_DecemberGiftCutoff.getDate(),
													     dc_InfoEditCutoff.getDate(),
													      cbWish.getID(), cbGiftCardWish.getID(),
													       dc_DecemberMealCutoff.getDate(),
													        dc_WaitlistGiftCutoff.getDate(),
													         cbDefaultDeliveryActivity.getID());
			
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
				display(uPrefs);
			}
		}
	}
	
	void setEnabledDateToday(boolean tf) { dc_today.setEnabled(tf); }
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
	}
	
	void checkApplyChangesEnabled()
	{
		if(!gvDB.getSeasonStartDate().equals(dc_seasonstart.getDate()) || 
			!gvDB.getDeliveryDate().equals(dc_delivery.getDate()) ||
			!gvDB.getGiftsReceivedDate().equals(dc_giftsreceived.getDate()) ||
			!gvDB.getThanksgivingMealDeadline().equals(dc_ThanksgivingMealCutoff.getDate()) ||
			!gvDB.getDecemberGiftDeadline().equals(dc_DecemberGiftCutoff.getDate()) ||
			!gvDB.getDecemberMealDeadline().equals(dc_DecemberMealCutoff.getDate()) ||
			!gvDB.getWaitlistGiftDeadline().equals(dc_WaitlistGiftCutoff.getDate()) ||
			!gvDB.getFamilyEditDeadline().equals(dc_InfoEditCutoff.getDate()))
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
				userDB.getUserPreferences().getFamilyDNSFilter() != fdnsFilterDefaultCB.getSelectedIndex())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(defaultGiftCB) &&
				((ONCWish) defaultGiftCB.getSelectedItem()).getID() != gvDB.getDefaultGiftID())
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(defaultGiftCardCB) &&
				((ONCWish) defaultGiftCardCB.getSelectedItem()).getID() != gvDB.getDefaultGiftCardID())
		{
			update();
		}
		else if(!bIgnoreDialogEvents && e.getSource().equals(deliveryActivityCB) &&
				((Activity) deliveryActivityCB.getSelectedItem()).getID() != gvDB.getDeliveryActivityID())
		{
			update();
		}
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
		userPrefs.setFamilyDNSFilter(fdnsFilterDefaultCB.getSelectedIndex());
		
		String response = UserDB.getInstance().update(this, updateUserReq);
		if(!response.startsWith("UDATED_USER"))
		{
			//we have a failure, display the original preferences
			display(uPrefs);
		}
	}
	
	private void updateDefalutGiftCBLists(boolean bInitialize)
	{	
		//remove the action listener for each CB
		defaultGiftCB.removeActionListener(this);
		defaultGiftCardCB.removeActionListener(this);
		
		//archive the default gift selection
		ONCWish curr_defalut_gift_sel;
		if(bInitialize &&  gvDB.getDefaultGiftID() > -1)
			curr_defalut_gift_sel = (ONCWish) catDB.getWishByID(gvDB.getDefaultGiftID());
		else
			curr_defalut_gift_sel = (ONCWish) defaultGiftCB.getSelectedItem();
			
		int selDefalutGiftIndex = 0;
		
		//update the default gift CBM
		defaultGiftCBM.removeAllElements();
		int index = 0;
		for(ONCWish w : catDB.getDefaultWishList())
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
		ONCWish curr_defalut_gift_card_sel;
		if(bInitialize &&  gvDB.getDefaultGiftCardID() > -1)
			curr_defalut_gift_card_sel = (ONCWish) catDB.getWishByID(gvDB.getDefaultGiftCardID());
		else
			curr_defalut_gift_card_sel = (ONCWish) defaultGiftCardCB.getSelectedItem();
			
		int selDefaultGiftCardIndex = 0;
		
		//update the default gift card CBM
		defaultGiftCardCBM.removeAllElements();
		index = 0;
		for(ONCWish w : catDB.getDefaultWishList())
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

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			display(uPrefs);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") || 
				dbe.getType().equals("CHANGED_USER")))	
		{
			//verify one of the user preferences has changed for the current user
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			if(updatedUser != null && userDB.getLoggedInUser().getID() == updatedUser.getID() &&
				(updatedUser.getPreferences().getWishAssigneeFilter() != wishAssigneeFilterDefaultCB.getSelectedIndex() ||
				 updatedUser.getPreferences().getFamilyDNSFilter() != fdnsFilterDefaultCB.getSelectedIndex()) ||
				 updatedUser.getPreferences().getFontSize() != (Integer)oncFontSizeCB.getSelectedItem())
			{
				display(updatedUser.getPreferences());
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
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_CATALOG"))
		{
			updateDefalutGiftCBLists(true);
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG_WISH"))
		{
			updateDefalutGiftCBLists(false);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_ACTIVITIES"))
		{
			updateDefaultDeliveryActivityCBList(true);
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_ACTIVITY"))
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
				checkApplyChangesEnabled();
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
}
