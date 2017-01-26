package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.toedter.calendar.JDateChooser;

public class AddFamilyDialog extends JDialog implements ActionListener, ListSelectionListener
{
	/**
	 * Dialog allows user to add a family to the database
	 */
	private static final long serialVersionUID = 1L;
	private static final long DAYS_TO_MILLIS = 1000 * 60 * 60 * 24; 
	private static final Color OLD_LACE = new Color(253, 245, 230);
	
	private GlobalVariables gvs;
	private FamilyDB fDB;
	private ChildDB cDB;
	private AdultDB adultDB;
	private MealDB mealDB;
	
	private List<AddChild> childList;
	private List<ONCAdult> adultList;
	
	private JTextPane dietPane, detailsPane;
	private JTextField HomePhone, OtherPhone, AltPhone;
	private JButton btnSubmit;
	private JTextField hohFN, hohLN, email, housenumTF, street, unit, city, zipCode;
	private JTextField altHousenumTF, altStreet, altUnit, altCity, altZipCode;
	private JRadioButton btnAddChild, btnDelChild, btnAddAdult, btnDelAdult;
	private JCheckBox sameAddressCkBox, ownTransportCxBox, giftsRequestedCkBox, foodAssistanceCkBox;
	private JLabel lblErrorMssg;
	
	private JComboBox languageCB, mealsCB;
	private ONCTable childTable, childWishTable, adultTable;
	private ChildTableModel childTableModel;
	private AdultTableModel adultTableModel;
	private ChildWishTableModel childWishTableModel;
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	
	public AddFamilyDialog(JFrame parentFrame)
	{
		super(parentFrame, true);
		this.setTitle("Add New Family");
		
		gvs = GlobalVariables.getInstance();
		fDB = FamilyDB.getInstance();
		cDB = ChildDB.getInstance();
		adultDB = AdultDB.getInstance();
		mealDB = MealDB.getInstance();
		
		//set up the adult t child list for the tables
		adultList = new ArrayList<ONCAdult>();
		childList = new ArrayList<AddChild>();
		
		//Set layout and border for the Family Panel
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		//Setup panels that comprise the Add Family Dialog
		JPanel hohPanel = new JPanel();
		hohPanel.setLayout(new BoxLayout(hohPanel, BoxLayout.Y_AXIS));
		hohPanel.setBorder(BorderFactory.createTitledBorder("Head of Household (HOH) Information:"));
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		//Set up the text fields for each of the characteristics displayed
        hohFN = new JTextField(9);
        hohFN.setBorder(BorderFactory.createTitledBorder("First Name"));
        
        hohLN = new JTextField(11);
        hohLN.setBorder(BorderFactory.createTitledBorder("Last Name"));
             
        HomePhone = new JTextField();
        HomePhone.setPreferredSize(new Dimension(128, 44));
        HomePhone.setBorder(BorderFactory.createTitledBorder("Primary Phone"));
        
        OtherPhone = new JTextField();
        OtherPhone.setPreferredSize(new Dimension(128, 44));
        OtherPhone.setBorder(BorderFactory.createTitledBorder("Alternate Phone"));
        
        AltPhone = new JTextField();
        AltPhone.setPreferredSize(new Dimension(128, 44));
        AltPhone.setBorder(BorderFactory.createTitledBorder("2nd Alt. Phone"));
      
        String[] languages = {"?", "English", "Spanish", "Arabic", "Korean", "Vietnamese", "Other"};
        languageCB = new JComboBox(languages);
        languageCB.setToolTipText("Select the primary language spoken by the family");
        languageCB.setPreferredSize(new Dimension(140, 48));
        languageCB.setBorder(BorderFactory.createTitledBorder("Language"));
        
        housenumTF = new JTextField();
        housenumTF.setPreferredSize(new Dimension(72, 44));
        housenumTF.setBorder(BorderFactory.createTitledBorder("House #"));
        
        street = new JTextField();
        street.setPreferredSize(new Dimension(192, 44));
        street.setBorder(BorderFactory.createTitledBorder("Street"));
        
        unit = new JTextField();
        unit.setPreferredSize(new Dimension(80, 44));
        unit.setBorder(BorderFactory.createTitledBorder("Unit"));
        
        city = new JTextField();
        city.setPreferredSize(new Dimension(128, 44));
        city.setBorder(BorderFactory.createTitledBorder("City"));
       
        zipCode = new JTextField();
        zipCode.setPreferredSize(new Dimension(88, 44));
        zipCode.setBorder(BorderFactory.createTitledBorder("Zip Code"));
        
        email = new JTextField(18);
        email.setBorder(BorderFactory.createTitledBorder("Email Address"));
        
        //Add components to the panels
        p1.add(hohFN);
        p1.add(hohLN);
        p1.add(HomePhone);
        p1.add(OtherPhone);
        p1.add(AltPhone);
		p1.add(languageCB);
		
        p2.add(housenumTF);
        p2.add(street);
        p2.add(unit);
        p2.add(city);
        p2.add(zipCode);
        p2.add(email);
        
        hohPanel.add(p1);
        hohPanel.add(p2);
        
        //set up the delivery address panel
        JPanel delAddressPanel = new JPanel();
        delAddressPanel.setLayout(new BoxLayout(delAddressPanel, BoxLayout.Y_AXIS));
		delAddressPanel.setBorder(BorderFactory.createTitledBorder("Delivery Address: ONC will deliver gifts to this address"));
		JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		delAddressPanel.setBackground(OLD_LACE);
		p3.setBackground(OLD_LACE);
		p4.setBackground(OLD_LACE);
		
        sameAddressCkBox = new JCheckBox("Check if same as HOH address");
        sameAddressCkBox.addActionListener(this);
        p3.add(sameAddressCkBox);
        
        altHousenumTF = new JTextField();
        altHousenumTF.setPreferredSize(new Dimension(72, 44));
        altHousenumTF.setBorder(BorderFactory.createTitledBorder("House #"));
        p3.add(altHousenumTF);
        
        altStreet = new JTextField();
        altStreet.setPreferredSize(new Dimension(192, 44));
        altStreet.setBorder(BorderFactory.createTitledBorder("Street"));
        p3.add(altStreet);
        
        altUnit = new JTextField();
        altUnit.setPreferredSize(new Dimension(80, 44));
        altUnit.setBorder(BorderFactory.createTitledBorder("Unit"));
        p3.add(altUnit);
        
        altCity = new JTextField();
        altCity.setPreferredSize(new Dimension(128, 44));
        altCity.setBorder(BorderFactory.createTitledBorder("City"));
        p3.add(altCity);
       
        altZipCode = new JTextField();
        altZipCode.setPreferredSize(new Dimension(88, 44));
        altZipCode.setBorder(BorderFactory.createTitledBorder("Zip Code"));
        p3.add(altZipCode);
        
        ownTransportCxBox = new JCheckBox("Check if family has their own transporation");
        p4.add(ownTransportCxBox);
        
        delAddressPanel.add(p3);
        delAddressPanel.add(p4);
      
        //set up the family members panel
        JPanel familyMemberPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		familyMemberPanel.setBorder(BorderFactory.createTitledBorder("Family Members"));
		
        //set up the adult table panel as part of the family members panel
        JPanel adultPanel = new JPanel();
        adultPanel.setBorder(BorderFactory.createTitledBorder("Other Adults: add adults in addition to HOH"));
        
        String[] atToolTips = {"First & Last Name", "Gender"};
      	adultTable = new ONCTable(atToolTips, new Color(240,248,255));

      	//Set up the table model. Cells are not editable
      	adultTableModel = new AdultTableModel(); 
        adultTable.setModel(adultTableModel);
        
        adultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adultTable.getSelectionModel().addListSelectionListener(this);
        
        TableColumn adultGenderColumn = adultTable.getColumnModel().getColumn(1);
		JComboBox adultGenderCB = new JComboBox(AdultGender.values());
		adultGenderColumn.setCellEditor(new DefaultCellEditor(adultGenderCB));

      	//Set table column widths
        int[] atWidths = {164,56};
      	int tablewidth = 0;
      	for(int i=0; i < atWidths.length; i++)
      	{
      		adultTable.getColumnModel().getColumn(i).setPreferredWidth(atWidths[i]);
      		tablewidth += atWidths[i];
      	}
      	tablewidth += 24; 	//Account for vertical scroll bar

      	//Set up the table header
      	JTableHeader anHeader = adultTable.getTableHeader();
      	anHeader.setForeground( Color.black);
      	anHeader.setBackground( new Color(161,202,241));

      	adultTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	adultTable.setRowHeight(adultTable.getRowHeight() + 2);
      	adultTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane adultScrollPane = new JScrollPane(adultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	adultScrollPane.setPreferredSize(new Dimension(tablewidth, adultTable.getRowHeight()*5));
      	
      	//set up the button controls for the adult panel
      	JPanel adultCntlPanel = new JPanel();
      	adultCntlPanel.setLayout(new BoxLayout(adultCntlPanel, BoxLayout.Y_AXIS));
      	btnAddAdult = new JRadioButton(gvs.getImageIcon(28));
      	btnAddAdult.setToolTipText("Click to add new adult to family");
      	btnAddAdult.addActionListener(this);;
      	adultCntlPanel.add(btnAddAdult);
      	
      	btnDelAdult = new JRadioButton(gvs.getImageIcon(29));
      	btnDelAdult.setToolTipText("Click to remove adult from family");
      	btnDelAdult.setEnabled(false);
      	btnDelAdult.addActionListener(this);
      	adultCntlPanel.add(btnDelAdult);
      	
      	adultPanel.add(adultScrollPane);
      	adultPanel.add(adultCntlPanel);
      	
        //set up the child table as part of the family members panel
        JPanel childPanel = new JPanel();
        childPanel.setBorder(BorderFactory.createTitledBorder("Children: add children under 18, or 18+ and still enrolled in FCPS:"));
        String[] ctToolTips = {"First Name", "Last Name", "Date of Birth in mm/dd/yyyy format", "Select gender from list",
        							"School Attended - leave blank if not attending school"};
      	childTable = new ONCTable(ctToolTips, new Color(240,248,255));

      	//Set up the table model
      	childTableModel = new ChildTableModel();
        childTable.setModel(childTableModel);
        
        childTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        childTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        childTable.getSelectionModel().addListSelectionListener(this);
		
		TableColumn childGenderColumn = childTable.getColumnModel().getColumn(3);
		String[] childGenders = {"Boy", "Girl", "Unknown"};
		JComboBox childGenderCB = new JComboBox(childGenders);
		childGenderColumn.setCellEditor(new DefaultCellEditor(childGenderCB));
		
		TableColumn childDOBColumn = childTable.getColumnModel().getColumn(2);
		JDateChooserCellEditor dcCellEditor = new JDateChooserCellEditor();
		childDOBColumn.setCellEditor(dcCellEditor);

      	//Set table column widths
        int[] ctWidths = {88,88,128,56,96};
      	tablewidth = 0;
      	for(int i=0; i < ctWidths.length; i++)
      	{
      		childTable.getColumnModel().getColumn(i).setPreferredWidth(ctWidths[i]);
      		tablewidth += ctWidths[i];
      	}
      	tablewidth += 24; 	//Account for vertical scroll bar

      	//Set up the table header
      	anHeader = childTable.getTableHeader();
      	anHeader.setForeground( Color.black);
      	anHeader.setBackground( new Color(161,202,241));
      	
      	childTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	childTable.setRowHeight(childTable.getRowHeight() + 2);
      	childTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane childScrollPane = new JScrollPane(childTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	childScrollPane.setPreferredSize(new Dimension(tablewidth, childTable.getRowHeight()*5));
      	
      	//set up the child table controls
      	JPanel childCntlPanel = new JPanel();
      	childCntlPanel.setLayout(new BoxLayout(childCntlPanel, BoxLayout.Y_AXIS));
      	
      	btnAddChild = new JRadioButton(gvs.getImageIcon(28));
      	btnAddChild.setToolTipText("Click to add new child to family");
      	btnAddChild.addActionListener(this);;
      	childCntlPanel.add(btnAddChild);
      	
      	btnDelChild = new JRadioButton(gvs.getImageIcon(29));
      	btnDelChild.setToolTipText("Click to remove child from family");
      	btnDelChild.addActionListener(this);
      	btnDelChild.setEnabled(false);
      	childCntlPanel.add(btnDelChild);
      	
      	childPanel.add(childScrollPane);
      	childPanel.add(childCntlPanel);
      	
      	c1.gridx=0;
        c1.gridy=0;
        c1.gridwidth=2;
        c1.gridheight = 2;
        c1.fill = GridBagConstraints.BOTH;
        familyMemberPanel.add(childPanel, c1);
        
        c1.gridx=2;
        familyMemberPanel.add(adultPanel, c1);
        
        //set up the child wish table
        JPanel childWishPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c3 = new GridBagConstraints();
        childWishPanel.setBorder(BorderFactory.createTitledBorder("Gift Assistance: complete if family requested gift assistance"));
        
        giftsRequestedCkBox = new JCheckBox("Check if gift assistance requested, then complete table for each child in the family");
        giftsRequestedCkBox.addActionListener(this);
        
        String[] cwToolTips = {"First Name", "Age/Gender", "Wish 1", "Wish 2", "Wish 3", "Alternate Wish"};
      	childWishTable = new ONCTable(cwToolTips, new Color(240,248,255));

      	//Set up the table model.
      	childWishTableModel = new ChildWishTableModel(); 
        childWishTable.setModel(childWishTableModel);

      	//Set table column widths
        int[] cwtWidths = {32, 64, 104, 104, 104, 104};
      	tablewidth = 0;
      	for(int i=0; i < ctWidths.length; i++)
      	{
      		childWishTable.getColumnModel().getColumn(i).setPreferredWidth(cwtWidths[i]);
      		tablewidth += cwtWidths[i];
      	}
      	tablewidth += 24; 	//Account for vertical scroll bar

      	//Set up the table header
      	anHeader = childWishTable.getTableHeader();
      	anHeader.setForeground( Color.black);
      	anHeader.setBackground( new Color(161,202,241));
      	
      	childWishTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	childWishTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane childWishScrollPane = new JScrollPane(childWishTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	childWishScrollPane.setPreferredSize(new Dimension(tablewidth, childWishTable.getRowHeight()*5));
      	
      	c3.gridx=0;
      	c3.gridy=0;
      	c3.gridwidth=1;
      	c3.gridheight = 1;
//      	c3.anchor = GridBagConstraints.PAGE_START; //bottom of space
      	childWishPanel.add(giftsRequestedCkBox);
      	
      	c3.gridy=1;
      	c3.gridwidth=6;
      	c3.fill = GridBagConstraints.BOTH;
      	c3.weightx=0.5;
      	c3.weighty=0.5;
//      	c3.anchor = GridBagConstraints.PAGE_END; //bottom of space
      	childWishPanel.add(childWishScrollPane, c3);
      	
      //set up the food assistance and details panel
        JPanel mealsPanel =new JPanel(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        mealsPanel.setBorder(BorderFactory.createTitledBorder("Meal Assistance: ONC will refer family to WFCM or equivalent"));
        mealsPanel.setBackground(OLD_LACE);
        
        foodAssistanceCkBox = new JCheckBox("Check if meal assistance requested");
        foodAssistanceCkBox.addActionListener(this);
        delAddressPanel.add(foodAssistanceCkBox);
        c2.gridx=0;
      	c2.gridy=0;
      	c2.gridwidth=1;
      	c2.gridheight = 1;
      	c2.fill = GridBagConstraints.BOTH;
      	c2.weightx=0.5;
      	c2.weighty=0.5;
        mealsPanel.add(foodAssistanceCkBox, c2);
        
        mealsCB = new JComboBox(MealType.getSelectionList());
        mealsCB.setBorder(BorderFactory.createTitledBorder("Requested For:"));
        mealsCB.setEnabled(false);
        c2.gridx=1;
      	c2.gridy=0;
      	c2.gridwidth=1;
      	c2.gridheight = 1;
      	c2.fill = GridBagConstraints.BOTH;
      	c2.weightx=0.5;
      	c2.weighty=0.5;
        mealsPanel.add(mealsCB, c2);
      
        dietPane = new JTextPane();
        dietPane.setEnabled(false);
        JScrollPane dietScrollPane = new JScrollPane(dietPane);
        dietScrollPane.setBorder(BorderFactory.createTitledBorder("Dietary restrictions (if any) for family:"));
        dietScrollPane.setPreferredSize(new Dimension(350, 52));
        c2.gridx=2;
      	c2.gridwidth=2;
      	c2.fill = GridBagConstraints.BOTH;
      	c2.ipadx = 40;
        mealsPanel.add(dietScrollPane, c2);
        
      	//set up family details panel
      	JPanel detailsPanel = new JPanel();
      	detailsPanel.setBorder(BorderFactory.createTitledBorder("Family Details"));
      	detailsPanel.setBackground(OLD_LACE);
      	detailsPane = new JTextPane();
      	detailsPane.setPreferredSize(new Dimension(800, 52));
      	detailsPane.setBorder(BorderFactory.createTitledBorder("Details about family ONC should know:"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsPane);
        detailsPanel.add(detailsScrollPane);
      	
        //set up the bottom panel which consists of the message panel and the control panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        
        JPanel mssgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblErrorMssg = new JLabel();
        mssgPanel.add(lblErrorMssg);
        
        JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSubmit = new JButton("Add New Family");
        btnSubmit.setToolTipText("Click to submit new family");
        btnSubmit.addActionListener(this);
        cntlPanel.add(btnSubmit);
        
        bottomPanel.add(mssgPanel);
        bottomPanel.add(cntlPanel);

        contentPane.add(hohPanel);
        contentPane.add(delAddressPanel);
        contentPane.add(familyMemberPanel);
        contentPane.add(mealsPanel);
        contentPane.add(childWishPanel);
        contentPane.add(detailsPanel);
        contentPane.add(bottomPanel);
        
        pack();
        this.setPreferredSize(new Dimension(864, 660));
	}
	
	void clearWishTableWishes()
	{
		for(AddChild ac:childList)
			ac.clearWishList();
		
		childWishTableModel.fireTableDataChanged();
	}
	
	/****
	 * Method verifies the information on the form is complete and accurate. If it finds an error
	 * it places a message in the error bar in the control panel. 
	 * 
	 * @return true if information is complete and accurate, false otherwise
	 */
	boolean verifyForm()
	{
		String errorMssg = "";
		//check if the HoH address is valid
		
		//check if the Delivery address is valid if it's not the same as the HoH address
		
		//check that assistance has been requested
		if(!foodAssistanceCkBox.isSelected() && !giftsRequestedCkBox.isSelected());
			errorMssg = "Error: Neither gift nor meal assistance was requested";
		
		//check that phone numbers are valid
		
		setErrorMessage(errorMssg);
		return errorMssg.isEmpty();
	}
	
	void setErrorMessage(String mssg)
	{
		lblErrorMssg.setText(String.format("<html><font color='red'><b><i>%s, please correct and submit again</i></b></font></html>", mssg));
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == sameAddressCkBox)
		{
			if(sameAddressCkBox.isSelected())
			{
				altHousenumTF.setText(housenumTF.getText());
				altStreet.setText(street.getText());
				altUnit.setText(unit.getText());
				altCity.setText(city.getText());
				altZipCode.setText(zipCode.getText());
			}
			else
			{
				altHousenumTF.setText("");
				altStreet.setText("");
				altUnit.setText("");
				altCity.setText("");
				altZipCode.setText("");
			}
		}
		else if(e.getSource() == foodAssistanceCkBox)
		{
			if(foodAssistanceCkBox.isSelected())
			{
				mealsCB.setEnabled(true);
				dietPane.setEnabled(true);
			}
			else
			{
				mealsCB.setEnabled(false);
				mealsCB.setSelectedIndex(0);
				dietPane.setEnabled(false);
				dietPane.setText("");
			}
		}
		else if(e.getSource() == giftsRequestedCkBox)
		{
			if(!giftsRequestedCkBox.isSelected())
				clearWishTableWishes();
		}
		else if(e.getSource() == btnAddChild)
		{
			childList.add(new AddChild());
			childTableModel.fireTableDataChanged();
			childWishTableModel.fireTableDataChanged();
		}
		
		else if(e.getSource() == btnDelChild)
		{
			int row = childTable.getSelectedRow();
			if(row > -1 && row < childList.size())
			{
				childList.remove(row);
				childTableModel.fireTableDataChanged();
				childWishTableModel.fireTableDataChanged();
			}
		}
		else if(e.getSource() == btnAddAdult)
		{
			
			adultList.add(new ONCAdult());
			adultTableModel.fireTableDataChanged();
		}
		
		else if(e.getSource() == btnDelAdult)
		{
			int row = adultTable.getSelectedRow();
			if(row > -1 && row < adultList.size())
			{
				adultList.remove(row);
				adultTableModel.fireTableDataChanged();
			}
		}
		else if(e.getSource() == btnSubmit)
		{
			forceStopCellEditing();
			if(verifyForm())
			{
				processFamilyReferral();
				this.dispose();
			}
		}
	}
	
	void processFamilyReferral()
	{
		//get the user
		ONCUser user = UserDB.getInstance().getLoggedInUser();
		
		//create a meal request, if meal was requested
		ONCMeal mealReq = null, addedMeal = null;
		if(foodAssistanceCkBox.isSelected())
		{
			mealReq = new ONCMeal(-1, -1, MealStatus.Requested, (MealType) mealsCB.getSelectedItem(),
								dietPane.getText(), -1, user.getLNFI(), new Date(), 3,
								"Family Referred", user.getLNFI());
			
			addedMeal = mealDB.add(this, mealReq);
		}
		
		ONCFamily fam = new ONCFamily(-1, user.getLNFI(), "NNA",
					"NNA", "B-CR",
					languageCB.getSelectedIndex()==0 ? "Yes" : "No",
					(String) languageCB.getSelectedItem(),
					hohFN.getText(), hohLN.getText(), housenumTF.getText(),
					ensureUpperCaseStreetName(street.getText()),
					unit.getText(), city.getText(), zipCode.getText(), 
					altHousenumTF.getText(),
					ensureUpperCaseStreetName(altStreet.getText()),
					altUnit.getText(), altCity.getText(), altZipCode.getText(),
					HomePhone.getText(), OtherPhone.getText(), AltPhone.getText(),
					email.getText(), detailsPane.getText(), createFamilySchoolList(),
					true, createWishList(), user.getID(), addedMeal != null ? addedMeal.getID() : -1,
					addedMeal != null ? MealStatus.Requested : MealStatus.None,
					ownTransportCxBox.isSelected() ? Transportation.Yes : Transportation.No);
			
		ONCFamily addedFamily = (ONCFamily) fDB.add(this, fam);
		
		if(addedFamily != null)
		{
			//update the family id for the meal, if a meal was requested
			if(addedMeal != null)
			{
				addedMeal.setFamilyID(addedFamily.getID());
				mealDB.update(this, addedMeal);
			}
		
			//add children in the family
			for(AddChild c: childList)
			{
				if(!c.getLastName().isEmpty())	//only add a child if the last name is provided
				{
					System.out.println(String.format("AddFamiliyDialog.processFamily: childDOB= %d",c.getGMTDoB()));
					ONCChild addChildReq = new ONCChild(-1, addedFamily.getID(), c.getFirstName(), c.getLastName(),
										c.getGender(), c.getGMTDoB(), c.getSchool(), gvs.getCurrentYear());
					
					cDB.add(this, addChildReq);
				}
			}
/*			
			//now that we have added children, we can check for duplicate family in this year.
			ONCFamily dupFamily = familyDB.getDuplicateFamily(year, addedFamily, addedChildList);
			
//			if(dupFamily != null)
//				System.out.println(String.format("HttpHandler.processFamilyReferral: "
//						+ "dupFamily HOHLastName= %s, dupRef#= %s, addedFamily HOHLastName = %s, addedFamily Ref#= %s", 
//						dupFamily.getHOHLastName(), dupFamily.getODBFamilyNum(), 
//						addedFamily.getHOHLastName(), addedFamily.getODBFamilyNum()));
//			
			if(dupFamily == null)	//if not a dup, then for new families, check for prior year
			{
				//added family not in current year, check if in prior years
				//only check new families for prior year existence. If a re-referral,
				//we already know the reference id was from prior year
				ONCFamily pyFamily = null;
				if(bNewFamily)	
				{
					pyFamily = familyDB.isPriorYearFamily(year, addedFamily, addedChildList);
					if(pyFamily != null)
					{				
						//added new family was in prior year, keep the prior year reference # 
						//and reset the newly assigned target id index
						addedFamily.setODBFamilyNum(pyFamily.getODBFamilyNum());
						familyDB.decrementReferenceNumber();
					}
				}
			}
			//else if family was a dup, determine which family has the best reference number to
			//use. The family with the best reference number is retained and the family with 
			//the worst reference number is marked as duplicate
			else if(!dupFamily.getODBFamilyNum().startsWith("C") && 
						addedFamily.getODBFamilyNum().startsWith("C"))
			{
//				System.out.println(String.format("HttpHandler.processFamilyReferral, dupFamily no C: "
//						+ "dupFamily HOHLastName= %s, dupRef#= %s, addedFamily HOHLastName = %s, addedFamily Ref#= %s", 
//						dupFamily.getHOHLastName(), dupFamily.getODBFamilyNum(), 
//						addedFamily.getHOHLastName(), addedFamily.getODBFamilyNum()));
				
				//family is in current year already with an ODB referred target ID
				addedFamily.setONCNum("DEL");
				addedFamily.setDNSCode("DUP");
				addedFamily.setStoplightPos(FAMILY_STOPLIGHT_RED);
				addedFamily.setStoplightMssg("DUP of " + dupFamily.getODBFamilyNum());
				addedFamily.setODBFamilyNum(dupFamily.getODBFamilyNum());
				familyDB.decrementReferenceNumber();
			}	
			else if(dupFamily.getODBFamilyNum().startsWith("C") && 
					!addedFamily.getODBFamilyNum().startsWith("C"))
			{
//				System.out.println(String.format("HttpHandler.processFamilyReferral: dupFamily with C "
//						+ "dupFamily HOHLastName= %s, dupRef#= %s, addedFamily HOHLastName = %s, addedFamily Ref#= %s", 
//						dupFamily.getHOHLastName(), dupFamily.getODBFamilyNum(), 
//						addedFamily.getHOHLastName(), addedFamily.getODBFamilyNum()));
				
				//family is already in current year with an ONC referred target ID and added family 
				//does not have an ONC target id. In this situation, we can't decrement the assigned
				//ONC based target id and will just have to burn one.
				dupFamily.setONCNum("DEL");
				dupFamily.setDNSCode("DUP");
				dupFamily.setStoplightPos(FAMILY_STOPLIGHT_RED);
				dupFamily.setStoplightMssg("DUP of " + addedFamily.getODBFamilyNum());
				dupFamily.setStoplightChangedBy(wc.getWebUser().getLNFI());
				dupFamily.setODBFamilyNum(addedFamily.getODBFamilyNum());
			}
			else if(dupFamily.getODBFamilyNum().startsWith("C") && 
					addedFamily.getODBFamilyNum().startsWith("C"))
			{
				//which one was first?
				int dupNumber = Integer.parseInt(dupFamily.getODBFamilyNum().substring(1));
				int addedNumber = Integer.parseInt(addedFamily.getODBFamilyNum().substring(1));
				
				if(dupNumber < addedNumber)
				{
					//dup family has the correct ref #, so added family is duplicate
					addedFamily.setONCNum("DEL");
					addedFamily.setDNSCode("DUP");
					addedFamily.setStoplightPos(FAMILY_STOPLIGHT_RED);
					addedFamily.setStoplightMssg("DUP of " + dupFamily.getODBFamilyNum());
					addedFamily.setStoplightChangedBy(wc.getWebUser().getLNFI());
					addedFamily.setODBFamilyNum(dupFamily.getODBFamilyNum());
					familyDB.decrementReferenceNumber();
				}
				else
				{
					//added family has the correct ref #, so dup family is the duplicate
					dupFamily.setONCNum("DEL");
					dupFamily.setDNSCode("DUP");
					dupFamily.setStoplightPos(FAMILY_STOPLIGHT_RED);
					dupFamily.setStoplightMssg("DUP of " + addedFamily.getODBFamilyNum());
					dupFamily.setStoplightChangedBy(wc.getWebUser().getLNFI());
					dupFamily.setODBFamilyNum(addedFamily.getODBFamilyNum());
				}
			}
*/			
			//add adults in the family
			for(ONCAdult a:adultList)
				if(!a.getName().isEmpty())
					adultDB.add(this, new ONCAdult(-1, addedFamily.getID(), a.getName(), a.getGender()));
		}
		
//		return new FamilyResponseCode(0, addedFamily.getHOHLastName() + " Family Referral Accepted",
//										addedFamily.getODBFamilyNum());
	}
	
	String ensureUpperCaseStreetName(String street)
	{
		StringBuffer buff = new StringBuffer();
		String[] streetparts = street.split(" ");
		for(int i=0; i< streetparts.length; i++)
		{	
			if(streetparts[i].length() > 0)
			{
				buff.append(Character.toUpperCase(streetparts[i].charAt(0)) +
						streetparts[i].substring(1) + " ");
			}
		}
		
		return buff.toString().trim();
	}
	
	/**************************************************************************************************
	 * This method takes a string date in one of two formats (yyyy-MM-dd or M/D/yy) and returns a Date
	 * object from the string. If the input string is not of either format, the current date is returned.
	 ***************************************************************************************************/
    Long createChildDOB(String dob)
    {
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		Calendar gmtDOB = Calendar.getInstance(timezone);
		
		SimpleDateFormat websitesdf = new SimpleDateFormat();
		websitesdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	
    	//Create a date formatter to  parse the input string to create an Calendar
		//variable for DOB. If it can't be determined, set DOB = today.
		if(dob.length() == 10 && dob.contains("-"))
			websitesdf.applyPattern("MM-dd-yyyy");
		else if(dob.length() < 10 && dob.contains("-"))
			websitesdf.applyPattern("M-d-yy");
		else if(dob.length() == 10 && dob.contains("/"))
			websitesdf.applyPattern("MM/dd/yyyy");
		else
			websitesdf.applyPattern("M/d/yy");
		
		try
		{
			gmtDOB.setTime(websitesdf.parse(dob));
			gmtDOB.set(Calendar.HOUR_OF_DAY, 0);
			gmtDOB.set(Calendar.MINUTE, 0);
			gmtDOB.set(Calendar.SECOND, 0);
			gmtDOB.set(Calendar.MILLISECOND, 0);
			
			//perform a check to see that the dob is in UTC with no hours, minutes or seconds
			//if that's not true correct it.
			if(gmtDOB.getTimeInMillis() % DAYS_TO_MILLIS != 0)
			{
//				System.out.println(String.format("HttpHandler.createChildDOB: Set Time= %d",
//						gmtDOB.getTimeInMillis()));
				float badDOBinDays = gmtDOB.getTimeInMillis() / DAYS_TO_MILLIS;
				int goodDOBinDays = (int) (badDOBinDays + 0.5);
				gmtDOB.setTimeInMillis(goodDOBinDays * DAYS_TO_MILLIS);
//				System.out.println(String.format("HttpHandler.createChildDOB: Adj Time= %d",
//						gmtDOB.getTimeInMillis()));
			}
		}
		catch (ParseException e)
		{
			String errMssg = "Couldn't determine DOB from input: " + dob;
		}

    	//then convert the Calendar to a Date in Millis and return it
    	return gmtDOB.getTimeInMillis();
    }
	
	String createFamilySchoolList()
	{
		int nSchoolsAdded = 0;
		
		StringBuffer buff = new StringBuffer();
		
		//using child school as the iterator, create list of unique schools
		for(AddChild ac:childList)
		{
			String school = ac.getSchool();
			if(!school.isEmpty() && buff.indexOf(school) == -1)
			{
				if(nSchoolsAdded > 0)
					buff.append("\r" + school);
				else
					buff.append(school);
				
				nSchoolsAdded++;
			}
		}
		
		if(nSchoolsAdded > 0)
			return buff.toString();
		else
			return "";
	}
	
	String createWishList()
	{
		//check to see if gift assistance was requested. If not, simply return a message saying that
		if(giftsRequestedCkBox.isSelected())
		{
			//gift assistance was requested
			StringBuffer buff = new StringBuffer();
			
			//using child first name as the iterator, build a wish list string for each 
			//child in the family
			for(int i=0; i< childList.size(); i++)
			{
				AddChild ac = childList.get(i);
				buff.append(String.format("%s %s: ", ac.getFirstName(), ac.getLastName()));
				for(int wn=0; wn<4; wn++)
				{
					buff.append(ac.getWish(wn).isEmpty() ? "None" : ac.getWish(wn));
					buff.append(wn < 3 ? ", " : ";");			
				}
				
				if(i < childList.size()-1)
					buff.append("\n\n");
			}
			
			return buff.toString();
		}
		else
			return "Gift assistance not requested";
	}
	
	/*******
	 * This method is called to ensure if a user edits a table cell and then clicks a button prior to hitting
	 * enter or moving to another cell, thereby leaving the editor open and not firing the setValueAt event,
	 * this method corrects that
	 */
	void forceStopCellEditing()
	{
		if(childTable.isEditing())
			childTable.getCellEditor().stopCellEditing();
		
		if(adultTable.isEditing())
			adultTable.getCellEditor().stopCellEditing();
		
		if(childWishTable.isEditing())
			childWishTable.getCellEditor().stopCellEditing();	
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == childTable.getSelectionModel())
			btnDelChild.setEnabled(childTable.getSelectedRowCount() > 0);
		else if(lse.getSource() == adultTable.getSelectionModel())
			btnDelAdult.setEnabled(adultTable.getSelectedRowCount() > 0);
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
		private static final int WISH_AGE_GENDER_COLUMN = 1;
		
		private String[] columnNames = {"First Name", "Last Name", "Date Of Birth", "Gender", "School"};                             
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return childList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	AddChild child = childList.get(row);
        	if(col == FIRST_NAME_COL)  
        		return child.getFirstName();
        	else if(col == LAST_NAME_COL)
        		return child.getLastName();
        	else if(col == DOB_COL)
        		return child.getLocalDoB();	//convert long-gmt to long local
        	else if(col == GENDER_COL)
        		return child.getGender();
        	else 
        		return child.getSchool();    			
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == DOB_COL)
        		return Date.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return true;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 
//        	System.out.println(String.format("Setting value:row = %d, col = %d", row, col));
        	
        	if(childList.size() > 0)
        	{	
        		AddChild child = childList.get(row);
        		if(col == FIRST_NAME_COL) 
        		{
        			child.setFirstName((String) value);
        			childWishTableModel.fireTableCellUpdated(row, col);
        		}
        		else if(col == LAST_NAME_COL)
        			child.setLastName((String) value);
        		else if(col == DOB_COL)
        		{	
        			child.setDob((Date) value);	//Date-locale to Date-GMT
        			childWishTableModel.fireTableCellUpdated(row, WISH_AGE_GENDER_COLUMN);
        		}	
        		else if(col == GENDER_COL)
        		{
        			child.setGender((String) value);
        			childWishTableModel.fireTableCellUpdated(row, WISH_AGE_GENDER_COLUMN);
        		}
        		else
        			child.setSchool((String) value);
        	}
        }  
    }
	
	class ChildWishTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the child table
		 */
		private static final long serialVersionUID = 1L;
		private static final int FIRST_NAME_COL = 0;
		private static final int AGE_GENDER_COL = 1;
		private static final int WISH_1_COL = 2;
		private static final int ALT_WISH_COL = 5;
		private static final int WISH_COL_OFFSET = WISH_1_COL;
		
//		private String[] columnNames = {"#", "First Name", "Last Name", 
//                                        "Wish List (ideally, 3 wishes in order of importance; if clothing, include size & color preference)"};
		
		private String[] columnNames = {"First Name", "Age/Gender", "Wish 1", "Wish 2", "Wish 3", "Alt. Wish"}; 
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return childList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	if(childList.isEmpty())
        	{
        		return "";
        	}
        	else
        	{
        		AddChild child = childList.get(row);
        		if(col == FIRST_NAME_COL)  
        			return child.getFirstName();
        		else if(col == AGE_GENDER_COL)
        			return child.getAge_Gender();
        		else if(col >= WISH_1_COL && col <= ALT_WISH_COL)
        			return child.getWish(col-WISH_COL_OFFSET);
        		else
        			return "Error";
        	}
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	if(col >= WISH_1_COL && giftsRequestedCkBox.isSelected())
        		return true;
        	else
        		return false;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
        	if(col >= WISH_1_COL && col <= ALT_WISH_COL)
        	{
        		AddChild child = childList.get(row);
            	child.setWish(col-WISH_COL_OFFSET, (String) value); 
        	}
        }  
    }
	
	private class AddChild
	{
		private String		firstName;
		private String		lastName;
		private String		school;
		private String		gender;
		private long		gmtDoB;	//GMT time in milliseconds
		private List<String> wishList;
		
		AddChild()
		{
			firstName = "First Name";
			lastName = "Last Name";
			school = "School";
			gender = "Unknown";
			gmtDoB = Calendar.getInstance().getTimeInMillis();
			wishList = new ArrayList<String>();
			for(int wn=0; wn < 4; wn++)
				wishList.add("Wish " + Integer.toString(wn+1));
		}
		
		String getFirstName() { return firstName; }	
		String getLastName() { return lastName; }
		String getSchool() { return school; }
		String getGender() { return gender; }
		long getGMTDoB() { return gmtDoB; }
		Date getLocalDoB() { return convertDOBFromGMTToLocalTime(gmtDoB).getTime(); }
		String getAge_Gender() { return getAgeAndGender(GlobalVariables.getCurrentSeason()); }
		String getWish(int wn) { return wn >= 0 && wn < wishList.size() ? wishList.get(wn) : "Error"; }
		
		void setFirstName(String firstName) { this.firstName = firstName; }
		void setLastName(String lastName) { this.lastName = lastName; }
		void setSchool(String school) { this.school = school; }
		void setGender(String gender) { this.gender = gender; }
		void setDob(Date dob) { this.gmtDoB = convertDateDOBToGMT(dob); }
		void setWish(int wn, String wish)
		{
			if(wn >= 0 && wn < wishList.size())
				wishList.set(wn, wish); 
		}
		
		void clearWishList()
		{
			for(int wn=0; wn< 4; wn++)
				setWish(wn, "");
		}
		
		/****************************************************************************************
		 * Calculates the child's age as of 12/25 in the current season. If the child is at least
		 * one year old on 12/25, their age is calculated in years. If the child is less then a 
		 * year old the child's age is calculated in months. The age relative to 12/25 corrects
		 * the issue of a child's birthday occurring between the time ornament labels are printed
		 * for the child and the time the gift is received showing different ages (one year off).
		 * @return A string of the child's age, in either years or months
		 ***************************************************************************************/
		String getAgeAndGender(int currYear)
		{
			Calendar christmasDayCal = Calendar.getInstance();
			christmasDayCal.set(currYear, Calendar.DECEMBER, 25, 0, 0, 0);
			Calendar dobCal = Calendar.getInstance();
			dobCal.setTimeInMillis(gmtDoB);
			int nChildAge = 0;
			
			if (dobCal.after(christmasDayCal))
				return "Future DOB";
			else
			{		
				int sesaonYear = christmasDayCal.get(Calendar.YEAR);
				int dobYear = dobCal.get(Calendar.YEAR);
				nChildAge = sesaonYear - dobYear;
			
				int december = christmasDayCal.get(Calendar.MONTH);
				int dobMonth = dobCal.get(Calendar.MONTH);
				if (dobMonth > december)	//can't really happen now that we calculate to 12/25
				{
					nChildAge--;
				} 
				else if (december == dobMonth)
				{
					int christmasDay = christmasDayCal.get(Calendar.DAY_OF_MONTH);
					int dobDay = dobCal.get(Calendar.DAY_OF_MONTH);
					if (dobDay > christmasDay)
					{
						nChildAge--;
					}
				}
			
				if(nChildAge > 0)
					return String.format("%d yr. old %s", nChildAge, gender.toLowerCase()); 
				else if(nChildAge == 0 && dobYear == sesaonYear && dobMonth == december)
					return "Newborn " + gender.toLowerCase();
				else if(nChildAge == 0 && dobYear == sesaonYear)
					return String.format("%d mos. old %s", december-dobMonth, gender.toLowerCase()); 
				else
					return String.format("%d mos. old %s", december-12-dobMonth, gender.toLowerCase()); 
			}
		}
		
		/****************************************************************************************
		 * Takes the date of birth in milliseconds (GMT) and returns a local time zone 
		 * Calendar object of the date of birth
		 * @param gmtDOB
		 * @return
		 ***************************************************************************************/
		public Calendar convertDOBFromGMTToLocalTime(long gmtDOB)
		{
			//gives you the current offset in ms from GMT at the current date
			TimeZone tz = TimeZone.getDefault();	//Local time zone
			int offsetFromUTC = tz.getOffset(gmtDOB);

			//create a new calendar in local time zone, set to gmtDOB and add the offset
			Calendar localCal = Calendar.getInstance();
			localCal.setTimeInMillis(gmtDOB);
			localCal.add(Calendar.MILLISECOND, (offsetFromUTC * -1));

			return localCal;
		}
		
		/****************************************************************************************
		 * Takes a local time zone Calendar date of birth and returns the date of birth 
		 * in milliseconds (GMT)
		 * Calendar object of the date of birth
		 * @param gmtDOB
		 * @return
		 ***************************************************************************************/
		public long convertDateDOBToGMT(Date dob)
		{
			//gives you the current offset in ms from GMT at the current date
			Calendar dobCal = Calendar.getInstance();
			dobCal.setTime(dob);
			dobCal.set(Calendar.HOUR, 0);
			dobCal.set(Calendar.MINUTE, 0);
			dobCal.set(Calendar.SECOND, 0);
			dobCal.set(Calendar.MILLISECOND, 0);
			
			TimeZone tz = dobCal.getTimeZone();
			int offsetFromUTC = tz.getOffset(dobCal.getTimeInMillis());
			return dobCal.getTimeInMillis() + offsetFromUTC;
		}
	}
	
	class AdultTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the adult table
		 */
		private static final long serialVersionUID = 1L;
		private static final int NAME_COL = 0;
		private static final int GENDER_COL = 1;
		
		private String[] columnNames = {"First & Last Name", "Gender"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return adultList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	if(adultList.isEmpty())
        	{
        		return "";
        	}
        	else
        	{
        		ONCAdult adult = adultList.get(row);
        		if(col == NAME_COL)  
        			return adult.getName();
        		else
        			return adult.getGender(); 
        	}
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == GENDER_COL)
        		return AdultGender.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return true;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	 	
        	ONCAdult adult = adultList.get(row);
            if(col == NAME_COL)  
            	adult.setName((String) value);
            else
            	adult.setGender((AdultGender) value);  
        }  
    }
	
	 public class JDateChooserCellEditor extends AbstractCellEditor implements TableCellEditor 
	 {
		 /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JDateChooser dateChooser;
		 
		 public JDateChooserCellEditor()
		 {
			 dateChooser = new JDateChooser();
			 dateChooser.addPropertyChangeListener(new PropertyChangeListener() 
			 {
				 @Override
	             public void propertyChange(PropertyChangeEvent evt)
	             {
					 if (evt.getPropertyName().equals("date"))
	                   stopCellEditing();
	             }
	          });
		 }
		
		 public Component getTableCellEditorComponent(JTable table, Object value,
	        boolean isSelected, int row, int column)
		 {
			 Date date = null;
			 if (value instanceof Date)
				 date = (Date) value;
			 
			 dateChooser.setDateFormatString("MMM d, yyyy");
			 dateChooser.setDate(date);

			 return dateChooser;
		 }

		 public Object getCellEditorValue()
		 {
			 dateChooser.setDateFormatString("MMM d, yyyy");
			 return dateChooser.getDate();
		 }
	 }
}
