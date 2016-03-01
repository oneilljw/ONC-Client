package ourneighborschild;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;


public class AddFamilyDialog extends JDialog implements ActionListener, DatabaseListener, ListSelectionListener
{
	/**
	 * Dialog allows user to add a family to the database
	 */
	private static final long serialVersionUID = 1L;
	private static final Color OLD_LACE = new Color(253, 245, 230);
	private GlobalVariables gvs;
	
	private Families fDB;
	private ChildDB cDB;
	private AdultDB adultDB;
	private DeliveryDB deliveryDB;
	private ONCRegions regions;
	private ONCAgents agentDB;
	private MealDB mealDB;
	
	private ONCFamily currFam;	//The panel needs to know which family is being displayed
	private List<AddChild> childList;
	private List<ONCAdult> adultList;
	private ONCChild currChild;	//The panel needs to know which child is being displayed
	
	private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
	
	private JTextPane dietPane;
	private JTextPane HomePhone, OtherPhone;
	private JButton btnSubmit;
	private JTextField hohFN, hohLN, email, housenumTF, street, unit, city, zipCode;
	private JTextField altHousenumTF, altStreet, altUnit, altCity, altZipCode;
	private JRadioButton btnAddChild, btnDelChild, btnAddAdult, btnDelAdult;
	private JCheckBox sameAddressCkBox, foodAssistanceCkBox;
	
	private JComboBox languageCB, mealsCB;
	private ONCTable childTable, childWishTable, adultTable;
	private ChildTableModel childTableModel;
	private AdultTableModel adultTableModel;
	private ChildWishTableModel childWishTableModel;
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	private boolean bChildTableDataChanging = true;	//Flag indicating that Child Table data is changing
	private boolean bDispAll = false; //Flag indicating whether all personal family data is displayed or not
	
	public AddFamilyDialog(JFrame parentFrame)
	{
		super(parentFrame, true);
		this.setTitle("Request Family Assistance");
		
		gvs = GlobalVariables.getInstance();
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		adultDB = AdultDB.getInstance();
		agentDB = ONCAgents.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		mealDB = MealDB.getInstance();
		
		currFam = null;

		//register to listen for family, delivery and child data changed events
		if(fDB != null)
			fDB.addDatabaseListener(this);
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		
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
		
//		pBkColor = p1.getBackground();

		//Set up the text fields for each of the characteristics displayed
        hohFN = new JTextField(9);
        hohFN.setBorder(BorderFactory.createTitledBorder("First Name"));
        
        hohLN = new JTextField(11);
        hohLN.setBorder(BorderFactory.createTitledBorder("Last Name"));
             
        HomePhone = new JTextPane();
        JScrollPane homePhoneScrollPane = new JScrollPane(HomePhone);
        homePhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        homePhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Primary Phone"));
        
        OtherPhone = new JTextPane();
        JScrollPane otherPhoneScrollPane = new JScrollPane(OtherPhone);
        otherPhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        otherPhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Alternate Phone"));
      
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
        p1.add(homePhoneScrollPane);
        p1.add(otherPhoneScrollPane);
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
        JPanel delAddressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		delAddressPanel.setBorder(BorderFactory.createTitledBorder("Delivery Address: ONC will deliver gifts to this address"));
		delAddressPanel.setBackground(OLD_LACE);
		
        sameAddressCkBox = new JCheckBox("Check if same as HOH address");
        sameAddressCkBox.addActionListener(this);
        delAddressPanel.add(sameAddressCkBox);
        
        altHousenumTF = new JTextField();
        altHousenumTF.setPreferredSize(new Dimension(72, 44));
        altHousenumTF.setBorder(BorderFactory.createTitledBorder("House #"));
        delAddressPanel.add(altHousenumTF);
        
        altStreet = new JTextField();
        altStreet.setPreferredSize(new Dimension(192, 44));
        altStreet.setBorder(BorderFactory.createTitledBorder("Street"));
        delAddressPanel.add(altStreet);
        
        altUnit = new JTextField();
        altUnit.setPreferredSize(new Dimension(80, 44));
        altUnit.setBorder(BorderFactory.createTitledBorder("Unit"));
        delAddressPanel.add(altUnit);
        
        altCity = new JTextField();
        altCity.setPreferredSize(new Dimension(128, 44));
        altCity.setBorder(BorderFactory.createTitledBorder("City"));
        delAddressPanel.add(altCity);
       
        altZipCode = new JTextField();
        altZipCode.setPreferredSize(new Dimension(88, 44));
        altZipCode.setBorder(BorderFactory.createTitledBorder("Zip Code"));
        delAddressPanel.add(altZipCode);
      
        //set up the family members panel
        JPanel familyMemberPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		familyMemberPanel.setBorder(BorderFactory.createTitledBorder("Family Members"));
		
        //set up the adult table panel as part of the family members panel
        JPanel adultPanel = new JPanel();
        adultPanel.setBorder(BorderFactory.createTitledBorder("Other Adults: add adults in addition to HOH"));
        
        String[] atToolTips = {"Adult Number", "First & Last Name", "Gender"};
      	adultTable = new ONCTable(atToolTips, new Color(240,248,255));

      	//Set up the table model. Cells are not editable
      	adultTableModel = new AdultTableModel(); 
        adultTable.setModel(adultTableModel);
        
        adultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        adultTable.getSelectionModel().addListSelectionListener(this);
        
        TableColumn adultGenderColumn = adultTable.getColumnModel().getColumn(2);
		String[] adultGenders = {"Female", "Male", "Unknown"};
		JComboBox adultGenderCB = new JComboBox(adultGenders);
		adultGenderColumn.setCellEditor(new DefaultCellEditor(adultGenderCB));

      	//Set table column widths
        int[] atWidths = {16, 168, 56};
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
      	
      	 //left justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        adultTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);

      	adultTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	adultTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane adultScrollPane = new JScrollPane(adultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	adultScrollPane.setPreferredSize(new Dimension(tablewidth, adultTable.getRowHeight()*6));
      	
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
        String[] ctToolTips = {"Child #", "First Name", "Last Name", "Date of Birth in mm/dd/yyyy format", "Select gender from list",
        							"School Attended - leave blank if not attending school"};
      	childTable = new ONCTable(ctToolTips, new Color(240,248,255));

      	//Set up the table model
      	childTableModel = new ChildTableModel(); 
        childTable.setModel(childTableModel);
        
//      childTable.setCellSelectionEnabled(true);
        childTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        childTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        childTable.getSelectionModel().addListSelectionListener(this);
		
		TableColumn childGenderColumn = childTable.getColumnModel().getColumn(4);
		String[] childGenders = {"Boy", "Girl", "Unknown"};
		JComboBox childGenderCB = new JComboBox(childGenders);
		childGenderColumn.setCellEditor(new DefaultCellEditor(childGenderCB));

      	//Set table column widths
        int[] ctWidths = {16,80,88,88,56,96};
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
      	
      	childTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);

      	childTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	childTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane childScrollPane = new JScrollPane(childTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	childScrollPane.setPreferredSize(new Dimension(tablewidth, childTable.getRowHeight()*6));
      	
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
//        c1.weightx=1.0;
//        c1.weighty=1.0;
        familyMemberPanel.add(childPanel, c1);
        c1.gridx=2;
        c1.gridy=0;
        c1.gridwidth=2;
        c1.gridheight = 2;
        c1.fill = GridBagConstraints.BOTH;
//        c1.weightx=1.0;
//        c1.weighty=1.0;
        familyMemberPanel.add(adultPanel, c1);
      	
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
        
        mealsCB = new JComboBox(MealType.values());
//      mealsCB.setPreferredSize(new Dimension(200, 44));
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
      	c2.gridy=0;
      	c2.gridwidth=2;
      	c2.gridheight = 1;
      	c2.fill = GridBagConstraints.BOTH;
      	c2.ipadx = 40;
      	c2.weightx=0.5;
      	c2.weighty=0.5;
        mealsPanel.add(dietScrollPane, c2);
        
        //set up the child wish table
        JPanel childWishPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c3 = new GridBagConstraints();
        childWishPanel.setBorder(BorderFactory.createTitledBorder("Gift Assistance: complete for each child in the family"));
        String[] cwToolTips = {"Child Number", "First Name", "Last Name", "Wish List"};
      	childWishTable = new ONCTable(cwToolTips, new Color(240,248,255));

      	//Set up the table model.
      	childWishTableModel = new ChildWishTableModel(); 
        childWishTable.setModel(childWishTableModel);

      	//Set table column widths
        int[] cwWidths = {16,72,80,640};
      	tablewidth = 0;
      	for(int i=0; i < cwWidths.length; i++)
      	{
      		childWishTable.getColumnModel().getColumn(i).setPreferredWidth(cwWidths[i]);
      		tablewidth += ctWidths[i];
      	}
      	tablewidth += 24; 	//Account for vertical scroll bar

      	//Set up the table header
      	anHeader = childWishTable.getTableHeader();
      	anHeader.setForeground( Color.black);
      	anHeader.setBackground( new Color(161,202,241));
      	
      	childWishTable.getColumnModel().getColumn(0).setCellRenderer(dtcr);

      	childTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      	childTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane childWishScrollPane = new JScrollPane(childWishTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	childWishScrollPane.setPreferredSize(new Dimension(tablewidth, childWishTable.getRowHeight()*6));
      	
      	c3.gridx=0;
      	c3.gridy=0;
      	c3.gridwidth=1;
      	c3.gridheight = 1;
      	c3.fill = GridBagConstraints.BOTH;
      	c3.weightx=0.5;
      	c3.weighty=0.5;
      	childWishPanel.add(childWishScrollPane, c3);
      	
      	//set up family details panel
      	JPanel detailsPanel = new JPanel();
      	detailsPanel.setBorder(BorderFactory.createTitledBorder("Family Details"));
      	detailsPanel.setBackground(OLD_LACE);
      	JTextPane detailsPane = new JTextPane();
      	detailsPane.setPreferredSize(new Dimension(800, 52));
      	detailsPane.setBorder(BorderFactory.createTitledBorder("Details about family ONC should know:"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsPane);
        detailsPanel.add(detailsScrollPane);
      	
      	
        //set up the control panel
        JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSubmit = new JButton("Submit to ONC");
        btnSubmit.setToolTipText("Click to submit family to Our Neighbors Child");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(this);
        cntlPanel.add(btnSubmit);

        contentPane.add(hohPanel);
        contentPane.add(delAddressPanel);
        contentPane.add(familyMemberPanel);
        contentPane.add(mealsPanel);
        contentPane.add(childWishPanel);
        contentPane.add(detailsPanel);
        contentPane.add(cntlPanel);
        
        pack();
        this.setPreferredSize(new Dimension(832, 660));
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
		if(e.getSource() == foodAssistanceCkBox)
		{
			if(foodAssistanceCkBox.isSelected())
			{
				mealsCB.setEnabled(true);
				dietPane.setEnabled(true);
			}
			else
			{
				mealsCB.setEnabled(false);
				dietPane.setEnabled(false);
				dietPane.setText("");
			}
		}
		else if(e.getSource() == btnAddChild)
		{
			childList.add(new AddChild());
			childTableModel.fireTableDataChanged();
			childWishTableModel.fireTableDataChanged();
			btnSubmit.setEnabled(true);
		}
		
		else if(e.getSource() == btnDelChild)
		{
			int row = childTable.getSelectedRow();
			if(row > -1 && row < childList.size())
			{
				childList.remove(row);
				childTableModel.fireTableDataChanged();
				childWishTableModel.fireTableDataChanged();
				btnSubmit.setEnabled(!childList.isEmpty());
			}
		}
		else if(e.getSource() == btnAddAdult)
		{
			
			adultList.add(new ONCAdult());
			adultTableModel.fireTableDataChanged();
		}
		
		if(e.getSource() == btnDelAdult)
		{
			int row = adultTable.getSelectedRow();
			if(row > -1 && row < adultList.size())
			{
				adultList.remove(row);
				adultTableModel.fireTableDataChanged();
			}
		}
		
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}
	
	class ChildTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the child table
		 */
		private static final long serialVersionUID = 1L;
		private static final int NUM_COL = 0;
		private static final int FIRST_NAME_COL = 1;
		private static final int LAST_NAME_COL = 2;
		private static final int DOB_COL = 3;
		private static final int GENDER_COL = 4;
		
		private String[] columnNames = {"#", "First Name", "Last Name", "DOB", "Gender", "School"};                             
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return childList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	AddChild child = childList.get(row);
        	if(col == NUM_COL)
        		return row+1;
        	if(col == FIRST_NAME_COL)  
        		return child.getFirstName();
        	else if(col == LAST_NAME_COL)
        		return child.getLastName();
        	else if(col == DOB_COL)
        		return child.getDob();
        	else if(col == GENDER_COL)
        		return child.getGender();
        	else 
        		return child.getSchool();    			
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	if(col == NUM_COL)
        		return false;
        	else
        		return true;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 
//        	System.out.println(String.format("Setting value:row = %d, col = %d", row, col));
        	
        	AddChild child = childList.get(row);
            if(col == FIRST_NAME_COL) 
            {
            	 child.setFirstName((String) value);
            	 childWishTableModel.fireTableCellUpdated(row, col);
            }
            else if(col == LAST_NAME_COL)
            {
            	child.setLastName((String) value);
            	childWishTableModel.fireTableCellUpdated(row, col);
            }
            else if(col == DOB_COL)
            	child.setDob((String) value);
            else if(col == GENDER_COL)
            	child.setGender((String) value);
            else
            	child.setSchool((String) value); 
        }  
    }
	
	class ChildWishTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the child table
		 */
		private static final long serialVersionUID = 1L;
		private static final int NUM_COL = 0;
		private static final int FIRST_NAME_COL = 1;
		private static final int LAST_NAME_COL = 2;
		private static final int WISHLIST_COL = 3;
		
		private String[] columnNames = {"#", "First Name", "Last Name", 
                                        "Wish List (ideally, 3 wishes in order of importance; if clothing, include size & color preference)"};
 
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
        		if(col == NUM_COL)
        			return row+1;
        		if(col == FIRST_NAME_COL)  
        			return child.getFirstName();
        		else if(col == LAST_NAME_COL)
        			return child.getLastName();
        		else
        			return child.getWishList();
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
        	if(col == WISHLIST_COL)
        		return true;
        	else
        		return false;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
//        	System.out.println(String.format("row = %d, col = %d", row, col));
        	
        	AddChild child = childList.get(row);
            if(col == WISHLIST_COL)
            	child.setWishList((String) value);  
        }  
    }
	
	private class AddChild
	{
		private String		firstName;
		private String		lastName;
		private String		school;
		private String		gender;
		private String		dob;	//GMT time in milliseconds
		private String 		wishList;
		
		AddChild()
		{
			firstName = "";
			lastName = "";
			school = "";
			gender = "";
			dob = "mm/dd/yyyy";
			wishList = "";	
		}
		
		String getFirstName() { return firstName; }	
		String getLastName() { return lastName; }
		String getSchool() { return school; }
		String getGender() { return gender; }
		String getDob() { return dob; }
		String getWishList() { return wishList; }
		
		void setFirstName(String firstName) { this.firstName = firstName; }
		void setLastName(String lastName) { this.lastName = lastName; }
		void setSchool(String school) { this.school = school; }
		void setGender(String gender) { this.gender = gender; }
		void setDob(String dob) { this.dob = dob; }
		void setWishList(String wishList) { this.wishList = wishList; }
	}
	
	class AdultTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the adult table
		 */
		private static final long serialVersionUID = 1L;
		private static final int NUM_COL = 0;
		private static final int NAME_COL = 1;
		private static final int GENDER_COL = 2;
		
		private String[] columnNames = {"#", "First & Last Name", "Gender"};
 
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
        		if(col == NUM_COL)
        			return row+1;
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
        	if(col == NUM_COL)
        		return false;
        	else
        		return true;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
//        	System.out.println(String.format("row = %d, col = %d", row, col));
        	
        	ONCAdult adult = adultList.get(row);
            if(col == NAME_COL)  
            	adult.setName((String) value);
            else
            	adult.setGender((AdultGender) value);  
        }  
    }

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == childTable.getSelectionModel())
			btnDelChild.setEnabled(childTable.getSelectedRowCount() > 0);
		else if(lse.getSource() == adultTable.getSelectionModel())
			btnDelAdult.setEnabled(adultTable.getSelectedRowCount() > 0);
	}
}
