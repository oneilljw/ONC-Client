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
	private ONCChild currChild;	//The panel needs to know which child is being displayed
	
	private JPanel p1, p2, p3;
	private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
	
	private ONCNavPanel nav;	//public to allow adding/removal of Entity Selection Listeners
	private JTextPane detailsPane, oncDIPane, odbWishListPane;
	private JScrollPane odbWishscrollPane;
	private JTextPane HomePhone, OtherPhone;
	private JButton btnAddChild, btnDelChild, btnSubmit;
	private JTextField oncDNScode;
	private JTextField HOHFirstName, HOHLastName, EMail;
	private JTextField housenumTF, Street, Unit, City, ZipCode;
	private JLabel lblONCNum, odbFamilyNum, lblRegion, lblNumBags, lblChangedBy;
	private JRadioButton delRB, altAddressRB;
	
	private JComboBox oncBatchNum, Language, statusCB, delstatCB;
	private ComboItem[] delStatus;
	public  JTable childTable;
	private ChildTableModel childTableModel;
	private ArrayList<ONCChild> ctAL; //List holds children object references being displayed in table
//	private int cn; //Child number being displayed
	
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
		
		//set up the child list for the child table and add the dummy child
		childList = new ArrayList<AddChild>();
		
		//Set layout and border for the Family Panel
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		
		//Setup panels that comprise the Add Family Dialog
		p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pBkColor = p1.getBackground();
		JPanel p4 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JPanel p5 = new JPanel();

		//Set up the text fields for each of the characteristics displayed
        HOHFirstName = new JTextField(9);
        HOHFirstName.setBorder(BorderFactory.createTitledBorder("First Name"));
        HOHFirstName.setEditable(false);
        HOHFirstName.addActionListener(this);
        
        HOHLastName = new JTextField(11);
        HOHLastName.setBorder(BorderFactory.createTitledBorder("Last Name"));
        HOHLastName.setEditable(false);
        HOHLastName.addActionListener(this);
             
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
        EMail.addActionListener(this);
        
        String[] languages = {"?", "English", "Spanish", "Arabic", "Korean", "Vietnamese", "Other"};
        Language = new JComboBox(languages);
        Language.setToolTipText("Select the primary language spoken by the family");
        Language.setPreferredSize(new Dimension(140, 48));
        Language.setBorder(BorderFactory.createTitledBorder("Language"));
        Language.setEnabled(false);
        
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
        
        altAddressRB = new JRadioButton(gvs.getImageIcon(19));
        altAddressRB.setToolTipText("Click to see alternate address");
        altAddressRB.setEnabled(false);
        altAddressRB.addActionListener(this);
        
        btnSubmit = new JButton("Submit");
        btnSubmit.setToolTipText("Click to submit family to Our Neighbors Child");
//        btnMeals.setUI((ButtonUI) BasicButtonUI.createUI(btnMeals));
//        btnMeals.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(this);
        
        //set up the child table
      //Set up the sort family table panel
        String[] colToolTips = {"Child Number", "First Name", "Last Name", "Date of Birth", "Gender",
        							"School Attended", "Holiday Assistance Wish List"};
      	childTable = new ONCTable(colToolTips, new Color(240,248,255));

      	//Set up the table model. Cells are not editable
      	childTableModel = new ChildTableModel(); 
        childTable.setModel(childTableModel);

      	//Set table column widths
        int[] colWidths = {16,72,80,80,48,72,360};
      	int tablewidth = 0;
      	for(int i=0; i < colWidths.length; i++)
      	{
      		childTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
      		tablewidth += colWidths[i];
      	}
      	tablewidth += 24; 	//Account for vertical scroll bar

      	//Set up the table header
      	JTableHeader anHeader = childTable.getTableHeader();
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
      	
      	btnAddChild = new JButton("Add Child");
      	btnAddChild.setToolTipText("Click to add new child to family");
      	btnAddChild.addActionListener(this);;
      	childCntlPanel.add(btnAddChild);
      	
      	btnDelChild = new JButton("Del Child");
      	btnDelChild.setToolTipText("Click to remove child from family");
      	btnDelChild.addActionListener(this);;
      	childCntlPanel.add(btnDelChild);
      	
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
        odbWishscrollPane.setBorder(BorderFactory.createTitledBorder("Child Wish List"));
        
        //Set up the ONC Notes Pane
        detailsPane = new JTextPane();
        detailsPane.setToolTipText("Family specific notes entered by ONC user");
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        detailsPane.setParagraphAttributes(attribs,true);             
	   	detailsPane.setEditable(false);
	   	
	    //Create the ONC Notes scroll pane and add the ONC Note text pane to it.
        JScrollPane oncNotesscrollPane = new JScrollPane(detailsPane);
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
		
        //Add components to the panels
        p1.add(HOHFirstName);
        p1.add(HOHLastName);
        
        p2.add(homePhoneScrollPane);
        p2.add(otherPhoneScrollPane);
        p2.add(EMail);
		p2.add(Language);
		
        p3.add(housenumTF);
        p3.add(Street);
        p3.add(Unit);
        p3.add(City);
        p3.add(ZipCode);
        p3.add(altAddressRB);
        
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        p4.add(childScrollPane, c);
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        p4.add(childCntlPanel, c);
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
        
        p5.add(btnSubmit);
        
        contentPane.add(p1);
        contentPane.add(p2);
        contentPane.add(p3);
        contentPane.add(p4);
        contentPane.add(p5);
        
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
		private static final int SCHOOL_COL = 5;
		private static final int WISHLIST_COL = 6;
		
		private GlobalVariables gvs = GlobalVariables.getInstance();
		private String[] columnNames = {"#", "First Name", "Last Name", "DOB", "Gender",
                                        "School",
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
        	else if(col == DOB_COL)
        		return child.getDob();
        	else if(col == GENDER_COL)
        		return child.getGender();
        	else if(col == SCHOOL_COL)
        		return child.getSchool();
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
            else if(col == SCHOOL_COL)
            	child.setSchool((String) value);
            else
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
}
