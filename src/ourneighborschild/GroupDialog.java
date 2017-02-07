package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import com.google.gson.Gson;

public class GroupDialog extends EntityDialog implements ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int PREFERRED_NUMBER_OF_TABLE_ROWS = 12;

	private GroupDB groupDB;
	
	private ONCTable memberTbl, candidateTbl;
	private MemberTableModel memberTM;
	private CandidateTableModel candidateTM;
	private JTextField nameTF;
	private JLabel lblLastChangedBy, lblDateChanged;
	private SimpleDateFormat sdf;
    private JComboBox groupTypeCB;
    private JButton btnAddMember, btnRemoveMember;
    private JCheckBox ckBoxShared;
    private int seasonCount = 0;	//Holds the navigation panel overall counts
    
    private ONCGroup currGroup;	//reference to ONCGroup object being displayed
    private List<ONCUser> memberList; //holds ONCUser's in the member table
    private List<ONCUser> candidateList; //holds ONCUser's in the candidate table

	GroupDialog(JFrame pf) 
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Group Information");
		
		groupDB = GroupDB.getInstance();
		
		//register to listen for partner, global variable, child and  and childwish
		//data changed events
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//initialize the table lists
		memberList = new LinkedList<ONCUser>();
		candidateList = new ArrayList<ONCUser>();
		
		//Create a content panel for the dialog and add panel components to it.
        JPanel odContentPane = new JPanel();
        odContentPane.setLayout(new BoxLayout(odContentPane, BoxLayout.PAGE_AXIS));
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, groupDB);
        nav.setDefaultMssg("Our Neighbor's Child Groups");
        nav.setCount1("Total: " + Integer.toString(0));
        nav.setCount2("Season: " + Integer.toString(0));
        nav.setNextButtonText("Next Group");
        nav.setPreviousButtonText("Previous Group");

        //set up the edit organization panel
//      entityPanel.setBorder(BorderFactory.createTitledBorder("Gift Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        //set up panel 1
        nameTF = new JTextField(23);
        nameTF.setBorder(BorderFactory.createTitledBorder("Group Name"));
        nameTF.addActionListener(dcListener);
                
        groupTypeCB = new JComboBox(GroupType.values());
        groupTypeCB.setToolTipText("Type of organization e.g. Business");
        groupTypeCB.setPreferredSize(new Dimension (144, 48));
        groupTypeCB.setBorder(BorderFactory.createTitledBorder("Group Type"));
        groupTypeCB.addActionListener(dcListener);
        
        lblLastChangedBy = new JLabel("No one");
        lblLastChangedBy.setToolTipText("User that last changed group info");
        lblLastChangedBy.setPreferredSize(new Dimension (124, 48));
        lblLastChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
        
        lblDateChanged = new JLabel("Never");
        lblDateChanged.setToolTipText("Timestamp group info last changed");
        lblDateChanged.setPreferredSize(new Dimension (120, 48));
        lblDateChanged.setBorder(BorderFactory.createTitledBorder("Date Changed"));
        sdf = new SimpleDateFormat("MM/d/yyyy");
        
        op1.add(nameTF);
        op1.add(groupTypeCB);
        op1.add(lblLastChangedBy);
        op1.add(lblDateChanged);

        //set up panel 2
        ckBoxShared = new JCheckBox("Info shared among members");
        ckBoxShared.addActionListener(dcListener);
        
        op2.add(ckBoxShared);
       
        //set up panel 3
        JPanel memberPanel = new JPanel();	//left panel
		JPanel btnPanel = new JPanel();	//center panel
		JPanel candidatePanel = new JPanel();	//right panel
		
		//set up the member panel - it will contain the member table
		memberPanel.setLayout(new BoxLayout(memberPanel, BoxLayout.Y_AXIS));
		memberPanel.setBorder(BorderFactory.createTitledBorder("Group Members"));
		
		//instantiate the member table model
		memberTM = new MemberTableModel();
		
		//set up the member table
		String[] memberTblTT = {"First Name", "Lat Name", "User status of member", "User authority of member"};
		memberTbl = new ONCTable(memberTM, memberTblTT, new Color(240,248,255)); 

		memberTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memberTbl.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {80, 96, 72, 72};
		for(int col=0; col < colWidths.length; col++)
		{
			memberTbl.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		memberTbl.setAutoCreateRowSorter(true);	//add a row sorter

		//set the header color
        JTableHeader anHeader = memberTbl.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
   
        //Create the scroll pane and add the table to it and set the user tip
        Dimension tablesize = new Dimension(tablewidth, memberTbl.getRowHeight() *
        										PREFERRED_NUMBER_OF_TABLE_ROWS);
        memberTbl.setPreferredScrollableViewportSize(tablesize);
        JScrollPane memberScrollPane = new JScrollPane(memberTbl,
        	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        memberScrollPane.setToolTipText("Table of stocks that can be added to tracker");
 
        //add the table scroll pane to the symbol panel
        memberPanel.add(memberScrollPane);
      
        //set up the button panel
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        
        GroupButtonListener gbListener = new GroupButtonListener();
        
        btnAddMember = new JButton("<- Add");
        btnAddMember.setToolTipText("Click to add highlighted candidate to member table");
        btnAddMember.setEnabled(false);
        btnAddMember.addActionListener(gbListener);
        btnPanel.add(btnAddMember);
        
        btnRemoveMember = new JButton("Remove");
        btnRemoveMember.setToolTipText("Click to stop tracking highlighted Stock in Quote table");
        btnRemoveMember.setEnabled(false);
        btnRemoveMember.addActionListener(gbListener);
        btnPanel.add(btnRemoveMember);
        
        //set up the candidate Panel
        candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
		candidatePanel.setBorder(BorderFactory.createTitledBorder("Candidate Members"));

		//instantiate the candidate table model
		candidateTM = new CandidateTableModel();
		
		//create the candidate table
		String[] memberTableTT = {"First Name", "Last Name"};
		candidateTbl = new ONCTable(candidateTM, memberTableTT, new Color(240,248,255));

		candidateTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		candidateTbl.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		tablewidth = 0;
		int[] posColWidths = {88, 112};
		for(int col=0; col < posColWidths.length; col++)
		{
			candidateTbl.getColumnModel().getColumn(col).setPreferredWidth(posColWidths[col]);
			tablewidth += posColWidths[col];
		}
		tablewidth += 24; 	//account for vertical scroll bar
		
		candidateTbl.setAutoCreateRowSorter(true);	//add a row sorter

		//set the table header color
        JTableHeader candidateHeader = candidateTbl.getTableHeader();
        candidateHeader.setForeground( Color.black);
        candidateHeader.setBackground( new Color(161,202,241));
        
        //justify columns in the StockQuote table
//      DefaultTableCellRenderer quotedtcr = new DefaultTableCellRenderer();
//      quotedtcr.setHorizontalAlignment(SwingConstants.CENTER);

        //Create the scroll pane and add the table to it and set the user tip
        Dimension candidatetablesize = new Dimension(tablewidth, candidateTbl.getRowHeight() *
        										PREFERRED_NUMBER_OF_TABLE_ROWS);
        candidateTbl.setPreferredScrollableViewportSize(candidatetablesize);
        JScrollPane candidateScrollPane = new JScrollPane(candidateTbl,
        	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        candidateScrollPane.setToolTipText("Table of candidates for groups");
        
        //add the table scroll pane to the candidate panel
        candidatePanel.add(candidateScrollPane);
       
        op3.add(memberPanel);
        op3.add(btnPanel);
        op3.add(candidatePanel);
        
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New Group");
    	btnNew.setToolTipText("Click to add a new group");
        
        btnDelete.setText("Delete Group");
    	btnDelete.setToolTipText("Click to delete this group");
        
        btnSave.setText("Save New Group");
    	btnSave.setToolTipText("Click to save the new group");
        
        btnCancel.setText("Cancel Add New Group");
    	btnCancel.setToolTipText("Click to cancel adding a new group");
       
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        this.pack();
        this.setMinimumSize(new Dimension(768, 400));
        this.setResizable(true);
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
//		System.out.println(dbe.getType());
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GROUP"))
		{
			ONCGroup updatedGroup = (ONCGroup) dbe.getObject1();
			
			//If current group is being displayed has changed, reshow it
			if(currGroup != null && currGroup.getID() ==  updatedGroup.getID() && !bAddingNewEntity)
				display(updatedGroup); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_GROUP"))
		{
			//if the deleted group was the only group in data base, clear the display
			//otherwise, if the deleted group is currently being displayed, change the index
			//to the next prior group and display.
			if(groupDB.size() == 0)
			{
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCGroup deletedGroup = (ONCGroup) dbe.getObject1();
				if(currGroup != null && currGroup.getID()  == deletedGroup.getID() && !bAddingNewEntity )
				{
					if(nav.getIndex() == 0)
						nav.setIndex(groupDB.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(groupDB.getObjectAtIndex(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_USER"))
		{
			ONCUser addedUser = (ONCUser) dbe.getObject1();
			if(addedUser.getPermission().compareTo(UserPermission.Agent) >= 0)
			{
				candidateList = userDB.getCandidateGroupMembers();
				candidateTM.fireTableDataChanged();
				
				setEnabledEditGroupMembers(true);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_USER"))
		{
			//refresh the candidate table
			candidateList = userDB.getCandidateGroupMembers();
			candidateTM.fireTableDataChanged();
				
			//refresh the member table
			memberList = userDB.getGroupMembers(currGroup.getID());
			memberTM.fireTableDataChanged();
				
			setEnabledEditGroupMembers(true);		
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_GROUPS"))
		{
			display(null);	//will display the first group in the data base, if there is one
			
			candidateList = userDB.getCandidateGroupMembers();
			candidateTM.fireTableDataChanged();
			
			setEnabledEditGroupMembers(true);
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		/*************************************************************************************
		 * If the table selection event is fired externally and the current mode is
		 * not adding a new group, save any changes to the currently displayed 
		 * group and display the group selected.
		 ************************************************************************************/
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(tse.getSource() != nav && tse.getType() == EntityType.GROUP)
			{
				ONCGroup group = (ONCGroup) tse.getObject1();
				update();
				nav.setIndex(groupDB.getListIndexByID(groupDB.getList(), group.getID()));
				display(group);
			}
			else if(tse.getSource() == nav && tse.getType() == EntityType.GROUP)
			{
				update();
				display(groupDB.getObjectAtIndex(nav.getIndex()));
			}
			
			setEnabledEditGroupMembers(true);
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.GROUP);
	}

	@Override
	void update()
	{
		//Check to see if user has changed any field, if so, save it
		ONCGroup reqGroup;
		
		if(currGroup != null)
			reqGroup = new ONCGroup(currGroup);	//make a copy for update request
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(this, "ONC Group Dialog Error:," +
					"No current group","ONC Group Dialog Error",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			return;	//If no current group, should never have gotten an update request
		}
		
		int bCD = 0; //used to indicate a change has been detected	
		if(!nameTF.getText().equals(reqGroup.getName())) { reqGroup.setName(nameTF.getText()); bCD = bCD | 1; }
		if(groupTypeCB.getSelectedItem() !=reqGroup.getType()) { reqGroup.setType((GroupType)groupTypeCB.getSelectedItem()); bCD = bCD | 2; }
		if(ckBoxShared.isSelected() && reqGroup.getPermission() == 0) { reqGroup.setPermission(1);  bCD = bCD | 4; }
		if(!ckBoxShared.isSelected() && reqGroup.getPermission() == 1) { reqGroup.setPermission(0);  bCD = bCD | 8; }
			
		if(bCD > 0)	//If an update to partner data (not stop light data) was detected
		{
			reqGroup.setChangedBy(userDB.getUserLNFI());
			
			String response = groupDB.update(this, reqGroup);	//notify the database of the change
			
			if(response.startsWith("UPDATED_GROUP"))
			{
				Gson gson = new Gson();
				ONCGroup updatedGroup = gson.fromJson(response.substring(13), ONCGroup.class);
				
				display(updatedGroup);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Group Update," +
						"try again later","Group Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currGroup);
			}
		}
	}

	void display(ONCEntity group)	//displays currGroup
	{
		if(groupDB.size() <= 0 )
		{
			currGroup = null;
			clear();
			nameTF.setText("No Groups Yet");	//If no groups, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else
		{
			//Determine what to display based on currDriver and driver
			if(currGroup == null && group == null)
				currGroup = (ONCGroup) groupDB.getObjectAtIndex(0);
			else if(group != null  && currGroup != group)
				currGroup = (ONCGroup) group;
			
			bIgnoreEvents = true;
			
			nameTF.setText(currGroup.getName());
			nameTF.setCaretPosition(0);
			
			groupTypeCB.setSelectedItem(currGroup.getType());
			
			ckBoxShared.setSelected(currGroup.getPermission() > 0);
			
			lblLastChangedBy.setText(currGroup.getChangedBy());
			lblDateChanged.setText(sdf.format(currGroup.getDateChanged()));
			
			nav.setCount1("Total Groups: " + Integer.toString(groupDB.size()));
			nav.setCount2("Season: " + Integer.toString(seasonCount));
			
			nav.setStoplightEntity(currGroup);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);

			memberList = userDB.getGroupMembers(currGroup.getID());
			memberTM.fireTableDataChanged();
			
			bIgnoreEvents = false;
		}
	}

	@Override
	void clear() 
	{
		bIgnoreEvents = true;
		
		nameTF.setText("");		
		groupTypeCB.setSelectedIndex(0);
		lblDateChanged.setText(sdf.format(new Date()));
		lblLastChangedBy.setText(userDB.getLoggedInUser().getLNFI());
		ckBoxShared.setSelected(false);
		
		memberList.clear();
		memberTM.fireTableDataChanged();
		
		bIgnoreEvents = false;
	}
	
	void setEnabledEditGroupMembers(boolean bEditMembersEnabled)
	{
		btnAddMember.setEnabled(false);
		btnRemoveMember.setEnabled(false);
		memberTbl.clearSelection();
		candidateTbl.clearSelection();
		memberTbl.setRowSelectionAllowed(bEditMembersEnabled);
		candidateTbl.setRowSelectionAllowed(bEditMembersEnabled);
	}

	@Override
	void onNew() 
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Group's Information"));
		clear();
		setEnabledEditGroupMembers(false);
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
	}

	@Override
	void onCancelNew() 
	{
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Group Information"));
		display(currGroup);
		setEnabledEditGroupMembers(true);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}

	@Override
	void onSaveNew() 
	{
		//construct a new group from user input	
		ONCGroup newGroup = new ONCGroup(-1, new Date(), userDB.getUserLNFI(),
										  3, "Group Created", userDB.getUserLNFI(),
										  nameTF.getText(), (GroupType) groupTypeCB.getSelectedItem(),
										  ckBoxShared.isSelected() ? 1 : 0);
				
		//send request to add new group to the local data base
		ONCGroup addedGroup = (ONCGroup) groupDB.add(this, newGroup);
				
		if(addedGroup != null)
		{
			//set the display index to the newly  added group and display the group
			nav.setIndex(groupDB.getListIndexByID(groupDB.getList(), addedGroup.getID()));
			display(addedGroup);
		}
		else
		{
			String err_mssg = "ONC Server denied add group request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Group Request Failure",
										JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currGroup);
		}
				
		//reset to review mode and display the proper partner
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Partner Information"));
		entityPanel.setBackground(pBkColor);
		
		setEnabledEditGroupMembers(true);

		bAddingNewEntity = false;
		setControlState();
	}

	@Override
	void onDelete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		if(lse.getSource() == memberTbl.getSelectionModel() && !bAddingNewEntity)
		{
			//a row in the StockSymbol table was selected or de-selected by the user
			if(memberTbl.getSelectedRow() > -1)	//if a row was selected, enable add of member
			{
				btnAddMember.setEnabled(false);
				btnRemoveMember.setEnabled(true);
				candidateTbl.clearSelection();
			}
		}
		else if(lse.getSource() == candidateTbl.getSelectionModel() && !bAddingNewEntity)
		{
			//a row in the StockQuote table was selected or de-selected by the user
			if(candidateTbl.getSelectedRow() > -1)	//if a row was selected, enable removal of member
			{
				btnAddMember.setEnabled(true);
				btnRemoveMember.setEnabled(false);
				memberTbl.clearSelection();
			}
		}
	}
	
	private class MemberTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int FIRST_NAME_COL = 0;
		private static final int LAST_NAME_COL = 1;
		private static final int STATUS_COL = 2;
		private static final int PERMISSION_COL = 3;
		
		public String[] columnNames = {"First Name", "Last Name", "Status", "Authority" };
		
		@Override
		public String getColumnName(int col) { return columnNames[col]; }
		
		@Override
		public int getColumnCount() { return columnNames.length; }

		@Override
		public int getRowCount() { return memberList.size(); }

		@Override
		public Object getValueAt(int row, int col)
		{
			ONCUser u = memberList.get(row);
			if(col == FIRST_NAME_COL)
				return u.getFirstname();
			else if(col == LAST_NAME_COL)
				return u.getLastname();
			else if(col == STATUS_COL)
				return u.getStatus().toString();
			else if(col == PERMISSION_COL)
				return u.getPermission().toString();
			else
				return "Error";
		}
	}
	
	private class CandidateTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int FIRST_NAME_COL = 0;
		private static final int LAST_NAME_COL = 1;
		
		private String[] columnNames = {"First Name", "Last Name" };
		
		@Override
		public String getColumnName(int col) { return columnNames[col]; }
		
		@Override
		public int getColumnCount() { return columnNames.length; }
	
		@Override
		public int getRowCount() { return candidateList.size(); }

		@Override
		public Object getValueAt(int row, int col)
		{
			ONCUser u = candidateList.get(row);
			if(col == FIRST_NAME_COL)
				return u.getFirstname();
			else if(col == LAST_NAME_COL)
				return u.getLastname();
			else
				return "Error";
		}		
	}
	
	private class GroupButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) 
		{
			ONCUser reqUpdatedUser = null;
			if(e.getSource() == btnAddMember && candidateTbl.getSelectedRow() > -1)
			{
				reqUpdatedUser = candidateList.get(candidateTbl.getSelectedRow());
				
				//check to see if user is already in the group. If they are, don't add them
				//a second time
				int index = 0;
				while(index < memberList.size() && memberList.get(index).getID() != reqUpdatedUser.getID())
					index++;
				
				if(index == memberList.size())
					reqUpdatedUser.addGroup(currGroup.getID());
				else
				{
					//user is already in group. Clear the selection and disable the button
					reqUpdatedUser = null;
					candidateTbl.clearSelection();
					btnAddMember.setEnabled(false);
				}
			}
			else if(e.getSource() == btnRemoveMember && memberTbl.getSelectedRow() > -1)
			{
				reqUpdatedUser = memberList.get(memberTbl.getSelectedRow());
				reqUpdatedUser.removeGroup(currGroup.getID());
			}
			
			if(reqUpdatedUser != null)
			{
				String response = userDB.update(this, reqUpdatedUser);
				if(response != null && response.startsWith("UPDATED_USER"))
				{
					memberList = userDB.getGroupMembers(currGroup.getID());
					memberTM.fireTableDataChanged();
				}	
			}
		}
	}
}
