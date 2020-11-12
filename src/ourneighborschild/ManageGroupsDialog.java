package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import au.com.bytecode.opencsv.CSVWriter;

public class ManageGroupsDialog extends ONCEntityTableDialog implements ActionListener, ListSelectionListener, 
																	DatabaseListener 
{
	private static final long serialVersionUID = 1L;
	private static final int GROUP_NAME_COL= 0;
	private static final int GROUP_TYPE_COL = 1;
	private static final int GROUP_AGENT_COUNT_COL = 2;
	private static final int GROUP_ACTIVE_COUNT_COL = 3;
	private static final int GROUP_REFER_COL = 4;
	private static final int GROUP_SHARING_COL = 5;

	private static final int AGENT_FIRST_NAME_COL= 0;
	private static final int AGENT_LAST_NAME_COL = 1;
	private static final int AGENT_ORG_COL = 2;
	private static final int AGENT_STATUS_COL = 3;
	private static final int AGENT_LOGINS_COL = 4;
	private static final int AGENT_LAST_LOGIN_COL = 5;

	private static final int NUM_GROUP_TABLE_ROWS = 12;
	private static final int NUM_AGENT_TABLE_ROWS = 8;
	
	private JPanel sortCriteriaPanel;
	private JComboBox<GroupType> typeCB;
	private JComboBox<String> sharingCB;
	private JComboBox<String> referCB;
	private JComboBox<ONCUser> userCB;
	private JComboBox<String> volExportCB;
	
	private DefaultComboBoxModel<ONCUser> userCBM;
	
	private boolean bIgnoreCBEvents;
	private GroupType sortGroupType;
	private String sortSharing, sortRefer;
	private ONCUser sortUser;

	private ONCTable agentTable, groupTable;
	private AbstractTableModel groupTableModel, agentTableModel;
	private JButton btnResetFilters, btnPrint;
	private JLabel lblGroupCount, lblAgentCount;
	private JScrollPane agentScrollPane;

	private GroupDB groupDB;
	private UserDB userDB;

	private List<ONCGroup> groupTableList;
	private List<ONCUser> agentTableList;
	private ONCGroup selectedGroup;

	public ManageGroupsDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Group Managment");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		groupDB = GroupDB.getInstance();
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//set up the table lists
		groupTableList = new ArrayList<ONCGroup>();
		agentTableList = new ArrayList<ONCUser>();
		
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
		
		//create search filter
		typeCB = new JComboBox<GroupType>(GroupType.getSearchFilterList());
		typeCB.setBorder(BorderFactory.createTitledBorder("Group Type"));
		typeCB.setPreferredSize(new Dimension(144,56));
		typeCB.addActionListener(this);
		sortCriteriaPanel.add(typeCB);
		sortGroupType = GroupType.Any;
		
		String[] sharingChoices = {"Any", "Yes", "No"};
		sharingCB = new JComboBox<String>(sharingChoices);
		sharingCB.setBorder(BorderFactory.createTitledBorder("Sharing?"));
		sharingCB.setPreferredSize(new Dimension(88,56));
		sharingCB.addActionListener(this);
		sortCriteriaPanel.add(sharingCB);
		sortSharing ="Any";
		
		referCB = new JComboBox<String>(sharingChoices);
		referCB.setBorder(BorderFactory.createTitledBorder("Members Refer?"));
		referCB.setPreferredSize(new Dimension(120,56));
		referCB.addActionListener(this);
		sortCriteriaPanel.add(referCB);
		sortRefer ="Any";
		
		userCB = new JComboBox<ONCUser>();
		userCBM = new DefaultComboBoxModel<ONCUser>();
		sortUser = new ONCUser("", "Any");
	    userCBM.addElement(sortUser);
	    userCB.setModel(userCBM);
	    userCB.setRenderer(new UserComboBoxRenderer());
		userCB.setBorder(BorderFactory.createTitledBorder("Agent"));
		userCB.setPreferredSize(new Dimension(144,56));
		userCB.addActionListener(this);
		sortCriteriaPanel.add(userCB);
		
        //create the group table
      	groupTableModel = new GroupTableModel();
       	String[] actToolTips = {"Name of group", "Type of group", "# Agents in the group",
       							"# Active Agents in the group",
       							"Do any members of the group submit family referrals?",
       							"Is group sharing family referral information between members?"};
       	
      	groupTable = new ONCTable(groupTableModel, actToolTips, new Color(240,248,255));

     	groupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      	groupTable.getSelectionModel().addListSelectionListener(this);
      		
      	//Set table column widths
      	int tablewidth = 0;
      	int[] group_colWidths = {256, 108, 56, 56, 96, 56};
      	for(int col=0; col < group_colWidths.length; col++)
      	{
      		groupTable.getColumnModel().getColumn(col).setPreferredWidth(group_colWidths[col]);
      		tablewidth += group_colWidths[col];
      	}
      	tablewidth += 24; 	//count for vertical scroll bar
      		
      	JTableHeader anHeader = groupTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
              
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        groupTable.getColumnModel().getColumn(GROUP_AGENT_COUNT_COL).setCellRenderer(dtcr);
        groupTable.getColumnModel().getColumn(GROUP_ACTIVE_COUNT_COL).setCellRenderer(dtcr);
        
        groupTable.setAutoCreateRowSorter(true);	//add a sorter
              
        //Create the scroll pane and add the table to it.
        JScrollPane groupScrollPane = new JScrollPane(groupTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        groupScrollPane.setPreferredSize(new Dimension(tablewidth, groupTable.getRowHeight()*NUM_GROUP_TABLE_ROWS));
        groupScrollPane.setBorder(BorderFactory.createTitledBorder("Groups"));
        
        //create the group table control panel
        JPanel groupCntlPanel = new JPanel();
        groupCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblGroupCount = new JLabel("# Groups Meeting Criteria: 0");
        groupCntlPanel.add(lblGroupCount);
        
		//Create the agent table model and table
		agentTableModel = new AgentTableModel();
		String[] colToolTips = {"Agent's First Name", "Agent's Last Name",
								"Organization listed in agents user profile",
								"User status of agent", "# of logins by agent",
								"Last time agent logged in"};
		
		agentTable = new ONCTable(agentTableModel, colToolTips, new Color(240,248,255));
		agentTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		agentTable.getSelectionModel().addListSelectionListener(this);
		
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
		agentTable.getColumnModel().getColumn(AGENT_LAST_LOGIN_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		tablewidth = 0;
		int[] colWidths = {96, 104, 180, 88, 56, 104};
		for(int col=0; col < colWidths.length; col++)
		{
			agentTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
//		volTable.setAutoCreateRowSorter(true);	//add a sorter
        
        anHeader = agentTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify columns
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        agentTable.getColumnModel().getColumn(AGENT_LOGINS_COL).setCellRenderer(dtcr);
//        agentTable.getColumnModel().getColumn(NUM_SIGNIN_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        agentScrollPane = new JScrollPane(agentTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        agentScrollPane.setPreferredSize(new Dimension(tablewidth, agentTable.getRowHeight()*NUM_AGENT_TABLE_ROWS));
        agentScrollPane.setBorder(BorderFactory.createTitledBorder("Agents"));
        
        //create the control panel
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        lblAgentCount = new JLabel("# Agents in Group:");
        infoPanel.add(lblAgentCount);
        
        String[] exportChoices = {"Export", "Agent Gmail Contact Group"};
        volExportCB = new JComboBox<String>(exportChoices);
        volExportCB.setToolTipText("Export Agent Info to .csv file");
        volExportCB.setEnabled(false);
        volExportCB.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the agent table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
        
        btnPanel.add(volExportCB);
        btnPanel.add(btnPrint);
        btnPanel.add(btnResetFilters);
        
        cntlPanel.add(infoPanel);
        cntlPanel.add(btnPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(groupScrollPane);
        getContentPane().add(groupCntlPanel);
        getContentPane().add(agentScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void createTableList()
	{	
		groupTableList.clear();
		agentTableList.clear();
		volExportCB.setEnabled(false);
		
		lblAgentCount.setText("# Agents in group: " + Integer.toString(agentTableList.size()));
	
		for(ONCGroup g : (List<ONCGroup>) groupDB.getList())
			if(doesGroupTypeMatch(g) && doesSharingMatch(g) && doesAgentMatch(g) && doesMembersReferMatch(g))
				groupTableList.add(g);
		
		lblGroupCount.setText(String.format("# Groups Meeting Criteria: %d", groupTableList.size()));
		
		agentTableModel.fireTableDataChanged();
		groupTableModel.fireTableDataChanged();
	}
	
	void resetFilters()
	{
		typeCB.removeActionListener(this);
		typeCB.setSelectedIndex(0);
		typeCB.addActionListener(this);
		sortGroupType = GroupType.Any;
		
		sharingCB.removeActionListener(this);
		sharingCB.setSelectedIndex(0);
		sharingCB.addActionListener(this);
		sortSharing = "Any";
		
		referCB.removeActionListener(this);
		referCB.setSelectedIndex(0);
		referCB.addActionListener(this);
		sortRefer = "Any";
		
		userCB.removeActionListener(this);
		userCB.setSelectedIndex(0);
		userCB.addActionListener(this);
		sortUser = new ONCUser("", "Any");
		
		selectedGroup = null;
		groupTableModel.fireTableDataChanged();
		
		createTableList();
	}
	
	boolean doesGroupTypeMatch(ONCGroup g)
	{
		 return sortGroupType == GroupType.Any || g.getType() == sortGroupType;
	}
	
	boolean doesSharingMatch(ONCGroup g)
	{
		 return sortSharing.equals("Any") || (g.groupSharesInfo() && sortSharing.equals("Yes")) ||
				 (!g.groupSharesInfo() && sortSharing.equals("No"));
	}
	
	boolean doesMembersReferMatch(ONCGroup g)
	{
		 return sortRefer.equals("Any") || (g.memberRefer() && sortRefer.equals("Yes")) ||
				 (!g.memberRefer() && sortRefer.equals("No"));
	}
	
	boolean doesAgentMatch(ONCGroup g)
	{
		//if selected agent is in the group, then they match
		 return userCB.getSelectedIndex() == 0 || sortUser.isInGroup(g.getID());
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             groupTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print Agent table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Agent Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	void updateUserList()
	{	
		UserDB userDB = UserDB.getInstance();

		bIgnoreCBEvents = true;
		userCB.setEnabled(false);
		ONCUser curr_sel = (ONCUser) userCB.getSelectedItem();
		int selIndex = 0;
		
		userCBM.removeAllElements();
		
		userCBM.addElement(new ONCUser("", "Any"));

		int index = 0;
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser user : userList)
		{
			if(user.getPermission().compareTo(UserPermission.Agent) >= 0)
			{
				userCBM.addElement(user);
				index++;
				if(curr_sel.getID() == user.getID())
					selIndex = index;
			}
		}
		
		userCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortUser = (ONCUser) userCB.getSelectedItem();
		
		userCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	void onExportAgentContactGroup()
	{
		//get group name input from user. If null exit
		String groupName = (String) JOptionPane.showInputDialog(this, "Please enter Contact Group Name:",  
				"Contact Group Name", JOptionPane.QUESTION_MESSAGE, gvs.getImageIcon(0),
				null, "");
		
		if(groupName != null)
		{
			ONCFileChooser oncfc = new ONCFileChooser(this);
			File oncwritefile = oncfc.getFile("Select file for export of Agent Gmail Contact Group" ,
       						new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE, groupName);
			if(oncwritefile!= null)
			{
				//If user types a new filename without extension.csv, add it
				String filePath = oncwritefile.getPath();
				if(!filePath.toLowerCase().endsWith(".csv")) 
					oncwritefile = new File(filePath + ".csv");
	    	
				try 
				{
					CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
					writer.writeNext(ONCGmailContactEntity.getGmailContactCSVHeader());
	    	    
					int[] row_sel = agentTable.getSelectedRows();
					for(int i=0; i<agentTable.getSelectedRowCount(); i++)
					{
						int index = agentTable.convertRowIndexToModel(row_sel[i]);
						writer.writeNext(agentTableList.get(index).getGoogleContactExportRow(groupName));
					}
	    	   
					writer.close();
	    	    
					JOptionPane.showMessageDialog(this, 
							agentTable.getSelectedRowCount() + " agent contacts sucessfully exported to " + oncwritefile.getName(), 
							"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
				} 
				catch (IOException x)
				{
					JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
							"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					System.err.format("IOException: %s%n", x);
				}
			}
		}
	}
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_GROUP") ||
				dbe.getType().equals("UPDATED_GROUP")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Group Management", gvs.getCurrentSeason()));
			updateUserList();
			createTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == groupTable.getSelectionModel())
		{
			int modelRow = groupTable.getSelectedRow() == -1 ? -1 : 
						groupTable.convertRowIndexToModel(groupTable.getSelectedRow());
		
			if(modelRow > -1)
			{
				selectedGroup = groupTableList.get(modelRow);
				agentTableList = userDB.getGroupMembers(selectedGroup.getID(), EnumSet.allOf(UserStatus.class));
				lblAgentCount.setText("Agents in selected group: " + Integer.toString(agentTableList.size()));
				fireEntitySelected(this, EntityType.GROUP, selectedGroup, null, null);
				agentTableModel.fireTableDataChanged();
				if(selectedGroup != null)
					agentScrollPane.setBorder(BorderFactory.createTitledBorder(String.format("Agents in %s Group", selectedGroup.getName())));
				else
					agentScrollPane.setBorder(BorderFactory.createTitledBorder("Agents"));
					
			}
		}
		else if(lse.getSource() == agentTable.getSelectionModel())
		{
			int modelRow = agentTable.getSelectedRow() == -1 ? -1 : 
				agentTable.convertRowIndexToModel(agentTable.getSelectedRow());

			volExportCB.setEnabled(modelRow > -1);
			
			ONCUser selUser = modelRow > -1 ? agentTableList.get(modelRow) : null;
			if(selUser != null)
				fireEntitySelected(this, EntityType.USER, selUser, null, null);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		if(e.getSource() == typeCB && !bIgnoreCBEvents)
		{
			sortGroupType = (GroupType) typeCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == sharingCB && !bIgnoreCBEvents)
		{
			sortSharing = (String) sharingCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == referCB && !bIgnoreCBEvents)
		{
			sortRefer = (String) referCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == userCB && !bIgnoreCBEvents)
		{
			sortUser = (ONCUser) userCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC Group Table");
		}
		else if(e.getSource() == btnResetFilters)
		{
			resetFilters();
		}
		else if(e.getSource() == volExportCB)
		{
			if(volExportCB.getSelectedItem().toString().equals("Agent Gmail Contact Group") &&
					agentTable.getSelectedRowCount() > 0)
			{ 
				onExportAgentContactGroup();
			}
			
			volExportCB.setSelectedIndex(0);
		}
	}	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.GROUP, EntityType.USER);
	}
	
	class GroupTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Group table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Name", "Type", "# Agents", "# Active", "Members Refer?", "Sharing?"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return groupTableList == null ? 0 : groupTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		ONCGroup g = groupTableList.get(row);
 //       	GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
        	
        		if(col == GROUP_NAME_COL)  
        			return g.getName();
        		else if(col == GROUP_TYPE_COL)
        			return g.getType();
        		else if(col == GROUP_AGENT_COUNT_COL)
        			return userDB.getGroupMembers(g.getID(), EnumSet.allOf(UserStatus.class)).size();
        		else if(col == GROUP_ACTIVE_COUNT_COL)
        			return userDB.getGroupMembers(g.getID(), UserStatus.getActiveUserSet()).size();
        		else if(col == GROUP_REFER_COL)
        			return g.memberRefer() ? gvs.getImageIcon(21) : gvs.getImageIcon(22);
        		else if (col == GROUP_SHARING_COL)
        			return g.groupSharesInfo() ? gvs.getImageIcon(21) : gvs.getImageIcon(22);
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == GROUP_AGENT_COUNT_COL)
        			return Integer.class;
        		else if(column == GROUP_TYPE_COL)
        			return GroupType.class;
        		else if(column >= GROUP_REFER_COL)
        			return ImageIcon.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Name, Status, Access and Permission are editable
        		return false;
        }
	}
	
	class AgentTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the agent table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Org", "Status", "# Logins", "Last Login"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return agentTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCUser u = agentTableList.get(row);
        	
        	if(col == AGENT_FIRST_NAME_COL)  
        		return u.getFirstName();
        	else if(col == AGENT_LAST_NAME_COL)
        		return u.getLastName();
        	else if(col == AGENT_ORG_COL)
        		return u.getOrganization();
        	else if (col == AGENT_STATUS_COL)
        		return u.getStatus().toString();
        	else if (col == AGENT_LOGINS_COL)
        		return u.getNSessions();
        	else if (col == AGENT_LAST_LOGIN_COL)
        	{
        		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        		calendar.setTimeInMillis(u.getLastLogin());
        		return calendar.getTime();
        	}
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == AGENT_LOGINS_COL)
        		return Long.class;
        	else if(column == AGENT_LAST_LOGIN_COL)
        		return Date.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	//Name, Status, Access and Permission are editable
        	return false;
        }
	}
	
	public class UserComboBoxRenderer extends JLabel implements ListCellRenderer<ONCUser> 
	{
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("rawtypes")
		@Override
	    public Component getListCellRendererComponent(JList list, ONCUser value,
	            int index, boolean isSelected, boolean cellHasFocus) 
		{
	        ONCUser selUser = (ONCUser) value;
	        setText(selUser.getLNFI());
	        return this;
		}
	}
}
