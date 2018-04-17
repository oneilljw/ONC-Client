package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class ManageVolDialog extends ONCEntityTableDialog implements ActionListener, ListSelectionListener,
														DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL = 1;
	private static final int GROUP_COL = 2;
	private static final int NUM_ACT_COL = 3;
	private static final int NUM_SIGNIN_COL = 4;
	private static final int QTY_COL = 5;
	private static final int NUM_COMMENTS_COL = 6;
	private static final int TIME_COL = 7;
	
	private static final int ACT_NAME_COL= 0;
	private static final int ACT_START_DATE_COL = 1;
	private static final int ACT_END_DATE_COL = 2;
	private static final int ACT_QTY_COL = 3;
	private static final int ACT_COMMENT_COL = 4;
	
	private static final int NUM_VOL_TABLE_ROWS = 12;
	private static final int NUM_ACT_TABLE_ROWS = 9;
	
	protected JPanel sortCriteriaPanel;
	private JComboBox<String> groupCB, commentCB, qtyCB;
	private DefaultComboBoxModel<String> groupCBM;
	private boolean bIgnoreCBEvents;
	private String sortGroup, sortComment, sortQty;
	
	private ONCTable volTable, actTable;
	private AbstractTableModel volTableModel, actTableModel;
	private JButton btnResetFilters, btnPrint, btnExport, btnImport;
	private JLabel lblVolCount;
	
	private VolunteerDB volDB;
	private ActivityDB activityDB;
	private VolunteerActivityDB volActDB;
	
	private List<ONCVolunteer> volTableList;
	private ONCVolunteer selectedVol;
		
	public ManageVolDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Volunteer Managment");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		volActDB = VolunteerActivityDB.getInstance();
		if(volActDB != null)
			volActDB.addDatabaseListener(this);
		
		//set up the table list
		volTableList = new ArrayList<ONCVolunteer>();
		
		bIgnoreCBEvents = false;
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		groupCBM = new DefaultComboBoxModel<String>();
	    groupCBM.addElement("Any");
		groupCB = new JComboBox<String>(groupCBM);
		groupCB.setBorder(BorderFactory.createTitledBorder("Group"));
		groupCB.setPreferredSize(new Dimension(200,56));
		groupCB.addActionListener(this);
		sortCriteriaPanel.add(groupCB);
		sortGroup = "Any";
		
		String[] qtyCBcontent = {"Any", "1", ">1"};
		qtyCB = new JComboBox<String>(qtyCBcontent);
		qtyCB.setBorder(BorderFactory.createTitledBorder("Quanity?"));
		sortQty = "Any";
		qtyCB.addActionListener(this);
		sortCriteriaPanel.add(qtyCB);
		
		String[] commentCBcontent = {"Any", "Yes", "No"};
		commentCB = new JComboBox<String>(commentCBcontent);
		commentCB.setBorder(BorderFactory.createTitledBorder("Comments?"));
		sortComment = "Any";
		commentCB.addActionListener(this);
		sortCriteriaPanel.add(commentCB);
		
		//Create the volunteer table model
		volTableModel = new VolunteerTableModel();
		
		//create the table
		String[] colToolTips = {"First Name", "Last Name", "Group", "# of Actvities", 
								"# of Warehouse Sign-Ins",
								"Number of activities for which a quantity > 1 was provided",
								"Number of activities for which a comment was provided", 
								"Last Sign-In Time"};
		
		volTable = new ONCTable(volTableModel, colToolTips, new Color(240,248,255));

		volTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		volTable.getSelectionModel().addListSelectionListener(this);
		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm");

		    public Component getTableCellRendererComponent(JTable table,Object value,
		            boolean isSelected, boolean hasFocus, int row, int column)
		    {
		        if( value instanceof Date)
		            value = f.format(value);
		        
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};
		volTable.getColumnModel().getColumn(TIME_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {96, 96, 144, 56, 56, 48, 56, 96};
		for(int col=0; col < colWidths.length; col++)
		{
			volTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		volTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = volTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        volTable.getColumnModel().getColumn(NUM_ACT_COL).setCellRenderer(dtcr);
        volTable.getColumnModel().getColumn(NUM_SIGNIN_COL).setCellRenderer(dtcr);
        volTable.getColumnModel().getColumn(QTY_COL).setCellRenderer(dtcr);
        volTable.getColumnModel().getColumn(NUM_COMMENTS_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(volTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, volTable.getRowHeight()*NUM_VOL_TABLE_ROWS));
//      dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setBorder(BorderFactory.createTitledBorder("Volunteers"));
        
        //create the volunteer table control panel
        JPanel volCntlPanel = new JPanel();
        volCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblVolCount = new JLabel("Volunteers Meeting Criteria: 0");
        volCntlPanel.add(lblVolCount);
        
        //create the activity table
      	actTableModel = new ActivityTableModel();
      		
      	//create the table
      	String[] actToolTips = {"Name", "Start Date", "End Date", "Quanity", "Comment"};
      		
      	actTable = new ONCTable(actTableModel, actToolTips, new Color(240,248,255));

      	actTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      	actTable.getSelectionModel().addListSelectionListener(this);
      	
      	actTable.getColumnModel().getColumn(ACT_START_DATE_COL).setCellRenderer(tableCellRenderer);
      	actTable.getColumnModel().getColumn(ACT_END_DATE_COL).setCellRenderer(tableCellRenderer);
      	//Set table column widths
      	tablewidth = 0;
      	int[] act_colWidths = {344, 104, 104, 40, 240};
      	for(int col=0; col < act_colWidths.length; col++)
      	{
     		actTable.getColumnModel().getColumn(col).setPreferredWidth(act_colWidths[col]);
      		tablewidth += act_colWidths[col];
      	}
      	tablewidth += 24; 	//count for vertical scroll bar
      		
        anHeader = actTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
              
        //Center justify wish count column
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        actTable.getColumnModel().getColumn(ACT_QTY_COL).setCellRenderer(dtcr);
              
        //Create the scroll pane and add the table to it.
        JScrollPane actScrollPane = new JScrollPane(actTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actScrollPane.setPreferredSize(new Dimension(tablewidth, actTable.getRowHeight()*NUM_ACT_TABLE_ROWS));
        actScrollPane.setBorder(BorderFactory.createTitledBorder("Selected Volunteer's Activites"));
//      actScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
    
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        btnImport = new JButton("Import");
        btnImport.setToolTipText("Import Volunteers from .csv file");
        btnImport.addActionListener(this);
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Export Table to .csv file");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the sign-in table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
       
        btnPanel.add(btnImport);
        btnPanel.add(btnExport);
        btnPanel.add(btnPrint);
        btnPanel.add(btnResetFilters);
        
        cntlPanel.add(infoPanel);
        cntlPanel.add(btnPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(volCntlPanel);
        getContentPane().add(actScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void createTableList()
	{	
		volTableList.clear();
		
		for(ONCVolunteer v : volDB.getList())
			if(doesGroupFilterMatch(v) && doesQtyFilterMatch(v) && doesCommentFilterMatch(v))
				volTableList.add(v);
		
		lblVolCount.setText(String.format("Volunteer's Meeting Criteria: %d", volTableList.size()));
		
		volTableModel.fireTableDataChanged();
	}
	
	void resetFilters()
	{	
		groupCB.removeActionListener(this);
		groupCB.setSelectedIndex(0);
		groupCB.addActionListener(this);
		sortGroup = "Any";
		
		commentCB.removeActionListener(this);
		commentCB.setSelectedIndex(0);
		commentCB.addActionListener(this);
		sortComment = "Any";
		
		qtyCB.removeActionListener(this);
		qtyCB.setSelectedIndex(0);
		qtyCB.addActionListener(this);
		sortQty = "Any";
		
		selectedVol = null;
		actTableModel.fireTableDataChanged();
		
		createTableList();
	}
	
	
	boolean doesGroupFilterMatch(ONCVolunteer v)
	{
		//test for activity category match
		 return sortGroup.equals("Any") || v.getOrganization().equals(sortGroup);
	}
	
	boolean doesCommentFilterMatch(ONCVolunteer v)
	{
		//test if any of volunteer's activities have comments
		if(sortComment.equals("Any"))
			return true;
		else
		{
			List<VolAct> vActList = volActDB.getVolunteerActivityList(v.getID());
			int index= 0, count = 0;
			while(index < vActList.size() && !vActList.get(index++).getComment().isEmpty())
				count++;
			
			return sortComment.equals("Yes") && count > 0 || sortComment.equals("No") && count == 0;
		}
	}
	
	boolean doesQtyFilterMatch(ONCVolunteer v)
	{
		//test if any of volunteer's activities have quantity match
		if(sortQty.equals("Any"))
			return true;
		else
		{
			List<VolAct> vActList = volActDB.getVolunteerActivityList(v.getID());
			int index= 0, count = 0;
			while(index < vActList.size() && vActList.get(index++).getQty() > 1)
				count++;
			
			return sortQty.equals(">1") && count > 0 || sortQty.equals("1") && count == 0;
		}
	}
	
	void updateGroupList()
	{	
		bIgnoreCBEvents = true;
		groupCB.setEnabled(false);
		String curr_sel = groupCB.getSelectedItem().toString();
		
		groupCBM.removeAllElements();
		
		groupCBM.addElement("Any");
		
		boolean currSelFound = false;
		for(String group : volDB.getGroupList())
		{
			groupCBM.addElement(group);
			if(curr_sel.equals(group))
				currSelFound = true;		
		}
		
		//reset selection to previous selection. If previous selection is no longer
		//available, reset to index and rebuild
		if(currSelFound = true)
			groupCB.setSelectedItem(currSelFound);
		else
		{
			groupCB.setSelectedItem("Any");
			sortGroup = "Any";
			createTableList();
		}
			
		groupCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             volTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print Volunteer table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Volunteer Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}

	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_DRIVER") ||
				dbe.getType().equals("UPDATED_DRIVER")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DRIVERS"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Volunteer Management", GlobalVariablesDB.getCurrentSeason()));
			updateGroupList();
			createTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == volTable.getSelectionModel())
		{
			int modelRow = volTable.getSelectedRow() == -1 ? -1 : 
						volTable.convertRowIndexToModel(volTable.getSelectedRow());
		
			if(modelRow > -1)
			{
				selectedVol = volTableList.get(modelRow);
				fireEntitySelected(this, EntityType.VOLUNTEER, selectedVol, null, null);
			
				actTableModel.fireTableDataChanged();
			}
		}
		else if(lse.getSource() == actTable.getSelectionModel())
		{
			int modelRow = actTable.getSelectedRow() == -1 ? -1 : 
				actTable.convertRowIndexToModel(actTable.getSelectedRow());
			
//			VolunteerActivity selActivity = modelRow > -1 ? selectedVol.getActivityList().get(modelRow) : null;
			VolAct va = modelRow > -1 ? volActDB.getVolunteerActivityList(selectedVol.getID()).get(modelRow) : null;
			if(va != null)
			{
				VolunteerActivity selActivity = activityDB.getActivity(va.getActID());
				if(selActivity != null)
					fireEntitySelected(this, EntityType.ACTIVITY, selActivity, null, null);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		if(e.getSource() == groupCB && !bIgnoreCBEvents)
		{
			sortGroup = (String) groupCB.getSelectedItem();
			createTableList();
		}
		if(e.getSource() == qtyCB && !bIgnoreCBEvents)
		{
			sortQty = (String) qtyCB.getSelectedItem();
			createTableList();
		}
		if(e.getSource() == commentCB && !bIgnoreCBEvents)
		{
			sortComment = (String) commentCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC Volunteer Table");
		}
		else if(e.getSource() == btnResetFilters)
		{
			resetFilters();
		}
		else if(e.getSource() == btnImport)
		{
//			String loggedInLNFI = UserDB.getInstance().getLoggedInUser().getLNFI();
//			String resultMssg = volDB.importSignUpGeniusVolunteers(GlobalVariablesDB.getFrame(), loggedInLNFI);
//			
//			JOptionPane.showMessageDialog(this, resultMssg, "Sign-Up Genius Import Result", 
//					JOptionPane.INFORMATION_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.VOLUNTEER, EntityType.ACTIVITY);
	}
	
	class VolunteerTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "# Activities", 
										"# Sign-Ins", "Qty > 1?", "# Comments:", "Last Changed"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return volTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		ONCVolunteer v = volTableList.get(row);
        	
        		if(col == FIRST_NAME_COL)  
        			return v.getFirstName();
        		else if(col == LAST_NAME_COL)
        			return v.getLastName();
        		else if (col == GROUP_COL)
        			return v.getOrganization();
        		else if (col == NUM_ACT_COL)
        			return volActDB.getVolunteerActivityList(v.getID()).size();
        		else if (col == NUM_SIGNIN_COL)
        			return v.getSignIns();
        		else if (col == QTY_COL)
        		{
        			List<VolAct> vActList = volActDB.getVolunteerActivityList(v.getID());
        			int index= 0, count = 0;
        			while(index < vActList.size() && vActList.get(index++).getQty() > 1)
        				count++;
        			
        			return count;
        		}
        		else if (col == NUM_COMMENTS_COL)
        		{
        			List<VolAct> vActList = volActDB.getVolunteerActivityList(v.getID());
        			int index= 0, count = 0;
        			while(index < vActList.size() && !vActList.get(index++).getComment().isEmpty())
        				count++;
        			
        			return count;
        		}
        		else if (col == TIME_COL)
        			return v.getDateChanged();
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == TIME_COL)
        			return Date.class;
        		else if(column == FIRST_NAME_COL || column == LAST_NAME_COL || column == GROUP_COL)
        			return String.class;
        		else
        			return Integer.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Name, Status, Access and Permission are editable
        		return false;
        }
	}
	
	class ActivityTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the activity table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Activity Name", "Start Date","End Date", "Qty", "Comment"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return selectedVol == null ? 0 : volActDB.getVolunteerActivityList(selectedVol.getID()).size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		List<VolAct> volActList = volActDB.getVolunteerActivityList(selectedVol.getID());
        		VolAct va = volActList.get(row);
        		VolunteerActivity act = activityDB.getActivity(va.getActID());
        	
        		if(col == ACT_NAME_COL)  
        			return act.getName();
        		else if(col == ACT_START_DATE_COL)
        			return convertLongToDate(act.getStartDate());
        		else if (col == ACT_END_DATE_COL)
        			return convertLongToDate(act.getEndDate());
        		else if(col == ACT_QTY_COL)
        			return va.getQty();
        		else if (col == ACT_COMMENT_COL)
        			return va.getComment();
        		else
        			return "Error";
        }
        
        private Date convertLongToDate(long date)
        {
        		Calendar cal = Calendar.getInstance();
        		cal.setTimeInMillis(date);
        		return cal.getTime();
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == ACT_START_DATE_COL || column == ACT_END_DATE_COL)
        			return Date.class;
        		else if(column == ACT_QTY_COL)
        			return Integer.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
	}
}
