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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class AddFamilyDialog extends JDialog implements ActionListener, DatabaseListener
{
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
	
	private JTextPane detailsPane, oncDIPane, odbWishListPane;
	private JScrollPane odbWishscrollPane;
	private JTextPane HomePhone, OtherPhone;
	private JButton btnAddChild, btnDelChild, btnAddAdult, btnDelAdult, btnSubmit;
	private JTextField hohFN, hohLN, email;
	private JTextField housenumTF, street, unit, city, zipCode;
	private JTextField altHousenumTF, altStreet, altUnit, altCity, altZipCode;
	private JRadioButton delRB, altAddressRB;
	
	private JComboBox oncBatchNum, Language, statusCB, delstatCB;
	private  ONCTable childTable, adultTable;
	private ChildTableModel childTableModel;
	private AdultTableModel adultTableModel;
	private ArrayList<ONCChild> ctAL; //List holds children object references being displayed in table
	
	public boolean bFamilyDataChanging = false; //Flag indicating program is triggering gui events, not user
	private boolean bChildTableDataChanging = true;	//Flag indicating that Child Table data is changing
	private boolean bDispAll = false; //Flag indicating whether all personal family data is displayed or not
	
	//An instance of the private subclass of the default highlight painter
	private static DefaultHighlighter.DefaultHighlightPainter highlightPainter = 
	        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	
	public AddFamilyDialog(JFrame parentFrame)
	{
		super(parentFrame, true);
		this.setTitle("Add Family Dialog");
		
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
		hohPanel.setBorder(BorderFactory.createTitledBorder("Head of Household Information"));
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JPanel delAddressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		delAddressPanel.setBorder(BorderFactory.createTitledBorder("Delivery Address"));
		
		JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pBkColor = p1.getBackground();
		
		JPanel familyMemberPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		familyMemberPanel.setBorder(BorderFactory.createTitledBorder("Family Members"));
		JPanel cntlPanel = new JPanel();

		//Set up the text fields for each of the characteristics displayed
        hohFN = new JTextField(9);
        hohFN.setBorder(BorderFactory.createTitledBorder("First Name"));
        
        hohLN = new JTextField(11);
        hohLN.setBorder(BorderFactory.createTitledBorder("Last Name"));
             
        HomePhone = new JTextPane();
        JScrollPane homePhoneScrollPane = new JScrollPane(HomePhone);
        homePhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        homePhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Home Phone"));
        
        OtherPhone = new JTextPane();
        JScrollPane otherPhoneScrollPane = new JScrollPane(OtherPhone);
        otherPhoneScrollPane.setPreferredSize(new Dimension(128, 44));
        otherPhoneScrollPane.setBorder(BorderFactory.createTitledBorder("Other Phone"));
      
        String[] languages = {"?", "English", "Spanish", "Arabic", "Korean", "Vietnamese", "Other"};
        Language = new JComboBox(languages);
        Language.setToolTipText("Select the primary language spoken by the family");
        Language.setPreferredSize(new Dimension(140, 48));
        Language.setBorder(BorderFactory.createTitledBorder("Language"));
        
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
        
        altHousenumTF = new JTextField();
        altHousenumTF.setPreferredSize(new Dimension(72, 44));
        altHousenumTF.setBorder(BorderFactory.createTitledBorder("House #"));
        
        altStreet = new JTextField();
        altStreet.setPreferredSize(new Dimension(192, 44));
        altStreet.setBorder(BorderFactory.createTitledBorder("Street"));
        
        altUnit = new JTextField();
        altUnit.setPreferredSize(new Dimension(80, 44));
        altUnit.setBorder(BorderFactory.createTitledBorder("Unit"));
        
        altCity = new JTextField();
        altCity.setPreferredSize(new Dimension(128, 44));
        altCity.setBorder(BorderFactory.createTitledBorder("City"));
       
        altZipCode = new JTextField();
        altZipCode.setPreferredSize(new Dimension(88, 44));
        altZipCode.setBorder(BorderFactory.createTitledBorder("Zip Code"));
        
        JCheckBox sameAddressCkBox = new JCheckBox("Check if same as HOH Address");
        sameAddressCkBox.addActionListener(this);
       
        btnSubmit = new JButton("Submit");
        btnSubmit.setToolTipText("Click to submit family to Our Neighbors Child");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(this);
        
        //set up the adult table panel
        JPanel adultPanel = new JPanel();
        adultPanel.setBorder(BorderFactory.createTitledBorder("Other Adults in Family"));
        
        String[] atToolTips = {"Adult Number", "First & Last Name", "Gender"};
      	adultTable = new ONCTable(atToolTips, new Color(240,248,255));

      	//Set up the table model. Cells are not editable
      	adultTableModel = new AdultTableModel(); 
        adultTable.setModel(adultTableModel);

      	//Set table column widths
        int[] atWidths = {16,160,48};
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
      	adultTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane adultScrollPane = new JScrollPane(adultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	adultScrollPane.setPreferredSize(new Dimension(tablewidth, adultTable.getRowHeight()*6));
      	
      	JPanel adultCntlPanel = new JPanel();
      	adultCntlPanel.setLayout(new BoxLayout(adultCntlPanel, BoxLayout.Y_AXIS));
      	btnAddAdult = new JButton("Add");
      	btnAddAdult.setToolTipText("Click to add new adult to family");
      	btnAddAdult.addActionListener(this);;
      	adultCntlPanel.add(btnAddAdult);
      	
      	btnDelAdult = new JButton("Delete");
      	btnDelAdult.setToolTipText("Click to remove adult from family");
      	btnDelAdult.addActionListener(this);;
      	adultCntlPanel.add(btnDelAdult);
      	
      	adultPanel.add(adultScrollPane);
      	adultPanel.add(adultCntlPanel);
      	
        //set up the child table
        JPanel childPanel = new JPanel();
        childPanel.setBorder(BorderFactory.createTitledBorder("Children in Family"));
        String[] ctToolTips = {"Child Number", "First Name", "Last Name", "Date of Birth", "Gender",
        							"School Attended"};
      	childTable = new ONCTable(ctToolTips, new Color(240,248,255));

      	//Set up the table model. Cells are not editable
      	childTableModel = new ChildTableModel(); 
        childTable.setModel(childTableModel);

      	//Set table column widths
        int[] ctWidths = {16,72,80,80,48,72};
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
      	childTable.setFillsViewportHeight(true);

      	//Create the scroll pane and add the table to it.
      	JScrollPane childScrollPane = new JScrollPane(childTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      										JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      	childScrollPane.setPreferredSize(new Dimension(tablewidth, childTable.getRowHeight()*6));
      	
      	//set up the child table controls
      	JPanel childCntlPanel = new JPanel();
      	childCntlPanel.setLayout(new BoxLayout(childCntlPanel, BoxLayout.Y_AXIS));
      	
      	btnAddChild = new JButton("Add");
      	btnAddChild.setToolTipText("Click to add new child to family");
      	btnAddChild.addActionListener(this);;
      	childCntlPanel.add(btnAddChild);
      	
      	btnDelChild = new JButton("Delete");
      	btnDelChild.setToolTipText("Click to remove child from family");
      	btnDelChild.addActionListener(this);;
      	childCntlPanel.add(btnDelChild);
      	
      	childPanel.add(childScrollPane);
      	childPanel.add(childCntlPanel);
      	
      	//set up the family details pane
      	JPanel mealdetailsPanel = new JPanel();
      	mealdetailsPanel.setBorder(BorderFactory.createTitledBorder("Food Assistance & Other Family Details"));
      	
      	JPanel detailsPanel = new JPanel();
      	detailsPanel.setBorder(BorderFactory.createTitledBorder("Family Details you want ONC to know"));
      	JTextPane detailsPane = new JTextPane();
        JScrollPane detailsScrollPane = new JScrollPane(detailsPane);
        detailsScrollPane.setPreferredSize(new Dimension(128, 44));
        detailsPanel.add(detailsScrollPane);
        
        JPanel mealsPanel = new JPanel();
        mealsPanel.setLayout(new BoxLayout(mealsPanel, BoxLayout.Y_AXIS));
        mealsPanel.setBorder(BorderFactory.createTitledBorder("Food Assitance"));
        
        JComboBox mealsCB = new JComboBox(MealType.values());
        mealsCB.setPreferredSize(new Dimension(128, 44));
        mealsCB.setBorder(BorderFactory.createTitledBorder("Assitance Req. For"));
        mealsPanel.add(mealsCB);
        
        JTextPane dietPane = new JTextPane();
        JScrollPane dietScrollPane = new JScrollPane(dietPane);
        dietScrollPane.setPreferredSize(new Dimension(128, 44));
        dietScrollPane.setBorder(BorderFactory.createTitledBorder("Assitance Req. For"));
        mealsPanel.add(detailsScrollPane);
        
        mealdetailsPanel.add(mealsPanel);
        mealdetailsPanel.add(detailsPanel);
        
      	
        //Set up the Wish List Pane
        odbWishListPane = new JTextPane();
        odbWishListPane.setToolTipText("Wish suggestions by child from ODB or WFCM");
         
//        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
//        StyleConstants.setFontSize(attribs, gvs.getFontSize());
//        StyleConstants.setSpaceBelow(attribs, 3);
//        odbWishListPane.setParagraphAttributes(attribs, true);
  	   	odbWishListPane.setEditable(false);
  	   	
	    //Create the ODB Wish List scroll pane and add the Wish List text pane to it.
        odbWishscrollPane = new JScrollPane(odbWishListPane);
        odbWishscrollPane.setBorder(BorderFactory.createTitledBorder("Child Wish List"));
                    
        //Add components to the panels
        p1.add(hohFN);
        p1.add(hohLN);
        p1.add(homePhoneScrollPane);
        p1.add(otherPhoneScrollPane);
		p1.add(Language);
		
        p2.add(housenumTF);
        p2.add(street);
        p2.add(unit);
        p2.add(city);
        p2.add(zipCode);
        p2.add(email);
        
        hohPanel.add(p1);
        hohPanel.add(p2);
        
        delAddressPanel.add(altHousenumTF);
        delAddressPanel.add(altStreet);
        delAddressPanel.add(altUnit);
        delAddressPanel.add(altCity);
        delAddressPanel.add(altZipCode);
        delAddressPanel.add(sameAddressCkBox);
        
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        familyMemberPanel.add(adultPanel, c);
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        familyMemberPanel.add(childPanel, c);
//        c.gridx=4;
//        c.gridy=0;
//        c.gridwidth=1;
//        c.gridheight = 1;
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx=0.5;
//        c.weighty=0.5;
//        p4.add(oncNotesscrollPane, c);
//        c.gridx=4;
//        c.gridy=1;
//        c.gridwidth=1;
//        c.gridheight = 1;
//        c.fill = GridBagConstraints.BOTH;
//        c.weightx=0.5;
//        c.weighty=0.5;
//        p4.add(oncDIscrollPane, c);
        
        
        cntlPanel.add(btnSubmit);
        
        contentPane.add(hohPanel);
        contentPane.add(delAddressPanel);
        contentPane.add(familyMemberPanel);
        contentPane.add(mealdetailsPanel);
//        contentPane.add(p4);
        contentPane.add(cntlPanel);
        
        pack();
        this.setPreferredSize(new Dimension(832, 660));
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnAddChild)
		{
			childList.add(new AddChild());
			childTableModel.fireTableDataChanged();
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
//        	System.out.println(String.format("row = %d, col = %d", row, col));
        	
        	AddChild child = childList.get(row);
            if(col == FIRST_NAME_COL)  
            	 child.setFirstName((String) value);
            else if(col == LAST_NAME_COL)
            	child.setLastName((String) value);
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
                                        "Wish List (ideally, 3 wishes in priority order, incl. size & color if rqrd.)"};
 
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
        	else
        		return child.getWishList();     			
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
 
        public int getRowCount() { return childList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCAdult adult = adultList.get(row);
        	if(col == NUM_COL)
        		return row+1;
        	if(col == NAME_COL)  
        		return adult.getName();
        	else
        		return adult.getGender();   			
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
//        	System.out.println(String.format("row = %d, col = %d", row, col));
        	
        	ONCAdult adult = adultList.get(row);
            if(col == NAME_COL)  
            	adult.setName((String) value);
            else
            	adult.setGender((String) value);  
        }  
    }
}
