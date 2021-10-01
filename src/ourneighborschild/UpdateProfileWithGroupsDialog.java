package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
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

public class UpdateProfileWithGroupsDialog extends JDialog implements ActionListener, DatabaseListener,
														ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	private static final int PREFERRED_NUMBER_OF_TABLE_ROWS = 8;

	private static final int GROUP_MASK = 32;
	
	private GlobalVariablesDB gvs;
	private UserDB userDB;
	private GroupDB groupDB;
	
	boolean bIgnoreEvents;
	boolean bReviewRequired;
	
	private ONCTable memberTbl, candidateTbl;
	private MemberTableModel memberTM;
	private CandidateTableModel candidateTM;
	private JTextField[] profileTF;
	private int profileChanged;
    private JButton btnAddMember, btnRemoveMember;
    private JButton btnNoChange, btnUpdate;
    
    private ONCUser currUser;	//reference to ONCUser object being displayed
    private List<ONCGroup> memberList; //holds groups the new user is being added to
    private List<ONCGroup> candidateList; //holds available groups

	UpdateProfileWithGroupsDialog(JFrame pf, ONCUser user, boolean bReviewRequired)
	{
		super(pf, true);
		this.setTitle("Our Neighbor's Child - Update Profile");
		this.currUser = user;
		this.bReviewRequired = bReviewRequired;
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
			
		groupDB = GroupDB.getInstance();
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		
		profileChanged = 0;
		
		//register to listen for user and group data change events
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		bIgnoreEvents = true;
		
		//initialize the table lists
		memberList = new LinkedList<ONCGroup>();
		candidateList = new ArrayList<ONCGroup>();
		
        //set up the edit user panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JPanel entityPanel = new JPanel();
        entityPanel.setLayout(new BoxLayout(entityPanel, BoxLayout.Y_AXIS));
        entityPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        profileTF = new JTextField[7];
        ProfileChangeListener pcl = new ProfileChangeListener();
        
        //set up panel 1
        profileTF[0] = new JTextField(9);
        profileTF[0].setBorder(BorderFactory.createTitledBorder("First Name"));
        profileTF[0].addKeyListener(pcl);
        
        profileTF[1] = new JTextField(12);
        profileTF[1].setBorder(BorderFactory.createTitledBorder("Last Name"));
        profileTF[1].addKeyListener(pcl);
        
        profileTF[2] = new JTextField(18);
        profileTF[2].setBorder(BorderFactory.createTitledBorder("Email"));
        profileTF[2].addKeyListener(pcl);
        
        profileTF[3] = new JTextField(10);
        profileTF[3].setBorder(BorderFactory.createTitledBorder("Work Phone"));
        profileTF[3].addKeyListener(pcl);
        
        //set up panel 1
        p1.add(profileTF[0]);
        p1.add(profileTF[1]);
        p1.add(profileTF[2]);
        p1.add(profileTF[3]);
        
        //set up op2
        profileTF[4]  = new JTextField(20);
        profileTF[4].setBorder(BorderFactory.createTitledBorder("Organization"));
        profileTF[4].setToolTipText("What organization does this user belong too?");
        profileTF[4].addKeyListener(pcl);
        
        profileTF[5] = new JTextField(20);
        profileTF[5].setBorder(BorderFactory.createTitledBorder("Title"));
        profileTF[5].setToolTipText("What job title does this user hold?");
        profileTF[5].addKeyListener(pcl);
        
        profileTF[6] = new JTextField(10);
        profileTF[6].setBorder(BorderFactory.createTitledBorder("Cell Phone"));
        profileTF[6].addKeyListener(pcl);
        
        //set up panel 2
        p2.add(profileTF[4]);
        p2.add(profileTF[5]);
        p2.add(profileTF[6]);
        
        //set up panel 4
        JPanel memberPanel = new JPanel();	//left panel
		JPanel btnPanel = new JPanel();	//center panel
		JPanel candidatePanel = new JPanel();	//right panel
		
		//set up the member panel - it will contain the member table
		memberPanel.setLayout(new BoxLayout(memberPanel, BoxLayout.Y_AXIS));
		memberPanel.setBorder(BorderFactory.createTitledBorder("Group(s) You Participate In"));
		
		//instantiate the member table model
		memberTM = new MemberTableModel();
		
		//set up the member table
		String[] memberTblTT = {"Group Name"};
		memberTbl = new ONCTable(memberTM, memberTblTT, new Color(240,248,255)); 

		memberTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memberTbl.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {240};
		for(int col=0; col < colWidths.length; col++)
		{
			memberTbl.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar

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
        memberScrollPane.setToolTipText("Group new user will belong to");
 
        //add the table scroll pane to the symbol panel
        memberPanel.add(memberScrollPane);
      
        //set up the button panel
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        
        btnAddMember = new JButton("<- Add");
        btnAddMember.setToolTipText("Click to add user to highlighted group");
        btnAddMember.setEnabled(false);
        btnAddMember.addActionListener(this);
        btnPanel.add(btnAddMember);
        
        btnRemoveMember = new JButton("Remove");
        btnRemoveMember.setToolTipText("Click to remove user from highlighted group");
        btnRemoveMember.setEnabled(false);
        btnRemoveMember.addActionListener(this);
        btnPanel.add(btnRemoveMember);
        
        //set up the candidate Panel
        candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
		candidatePanel.setBorder(BorderFactory.createTitledBorder("Candidate Groups"));

		//instantiate the candidate table model
		candidateTM = new CandidateTableModel();
		
		//create the candidate table
		String[] memberTableTT = {"Group Name"};
		candidateTbl = new ONCTable(candidateTM, memberTableTT, new Color(240,248,255));

		candidateTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		candidateTbl.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		tablewidth = 0;
		int[] posColWidths = {240};
		for(int col=0; col < posColWidths.length; col++)
		{
			candidateTbl.getColumnModel().getColumn(col).setPreferredWidth(posColWidths[col]);
			tablewidth += posColWidths[col];
		}
		tablewidth += 24; 	//account for vertical scroll bar

		//set the table header color
        JTableHeader candidateHeader = candidateTbl.getTableHeader();
        candidateHeader.setForeground( Color.black);
        candidateHeader.setBackground( new Color(161,202,241));
        
        //justify columns in the table
//      DefaultTableCellRenderer quotedtcr = new DefaultTableCellRenderer();
//      quotedtcr.setHorizontalAlignment(SwingConstants.CENTER);

        //Create the scroll pane and add the table to it and set the user tip
        Dimension candidatetablesize = new Dimension(tablewidth, candidateTbl.getRowHeight() *
        										PREFERRED_NUMBER_OF_TABLE_ROWS);
        candidateTbl.setPreferredScrollableViewportSize(candidatetablesize);
        JScrollPane candidateScrollPane = new JScrollPane(candidateTbl,
        	JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        candidateScrollPane.setToolTipText("Table of users that can be added to groups");
        
        //add the table scroll pane to the candidate panel
        candidatePanel.add(candidateScrollPane);
       
        p4.add(memberPanel);
        p4.add(btnPanel);
        p4.add(candidatePanel);
        
        entityPanel.add(p1);
        entityPanel.add(p2);
        entityPanel.add(p4);
       
        //Set the button names and tool tips for control panel
        JPanel cntlPanel = new JPanel();
        btnNoChange = new JButton("No Change Necessary");
        btnNoChange.setToolTipText("Click if current profile is still accurate");
        btnNoChange.addActionListener(this);
        cntlPanel.add(btnNoChange);
        
        btnUpdate = new JButton("Update Profile");
        btnUpdate.setToolTipText("Click to update your profile");
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(this);
        cntlPanel.add(btnUpdate);
       
        contentPanel.add(entityPanel);
        contentPanel.add(cntlPanel);
        
        this.setContentPane(contentPanel);
        
        this.pack();
        this.setResizable(true);
        this.setMinimumSize(new Dimension(720, 320));
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void showDialog()
	{
		display(currUser);
		this.setVisible(true);	//modal dialog, blocks here until next action
	}
	
	void update()
	{
		if(currUser != null && !memberList.isEmpty())
		{
			ONCUser reqUpdateUser = new ONCUser(currUser);	//make a copy for update request
			reqUpdateUser.setFirstName(profileTF[0].getText());
			reqUpdateUser.setLastName(profileTF[1].getText());
			reqUpdateUser.setEmail(profileTF[2].getText());
			reqUpdateUser.setHomePhone(profileTF[3].getText());
			reqUpdateUser.setCellPhone(profileTF[6].getText());
			reqUpdateUser.setOrganization(profileTF[4].getText());
			reqUpdateUser.setTitle(profileTF[5].getText());
			reqUpdateUser.setGroupList(getUserGroupList());
			
			if(currUser.getStatus() == UserStatus.Update_Profile)
				reqUpdateUser.setStatus(UserStatus.Active);
			
			reqUpdateUser.setChangedBy(userDB.getUserLNFI());
			
			String response = userDB.update(this, reqUpdateUser);	//notify the database of the change
			
			if(response.startsWith("UPDATED_USER"))
			{
				if(bReviewRequired)
					this.dispose();
				else
				{
					Gson gson = new Gson();
					ONCUser updatedUser = gson.fromJson(response.substring(12), ONCUser.class);
				
					display(updatedUser);
				}
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied User Update," +
											"try again later","User Update Failed",  
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currUser);
			}
		}
		else if(currUser != null && memberList.isEmpty()) //memeberList is empty
		{
			//display an error message that update request could not be processed
			String mssg = "<html>Each user must be in at least <b>one</b> group at all times!<br>Please add a new group "
						+ "before removing the last<br>group the user participates in.</html>";
			
			JOptionPane.showMessageDialog(this, mssg, "User Dialog Error",  
										JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currUser);
		}
	}

	void display(ONCEntity user)	//displays currGroup
	{
		currUser = (ONCUser) user;
			
		bIgnoreEvents = true;
			
		profileTF[0].setText(currUser.getFirstName());
		profileTF[0].setCaretPosition(0);
			
		profileTF[1].setText(currUser.getLastName());
		profileTF[1].setCaretPosition(0);
			
		profileTF[2].setText(currUser.getEmail());
		profileTF[2].setCaretPosition(0);
			
		profileTF[3].setText(currUser.getHomePhone());
		profileTF[3].setCaretPosition(0);
			
		profileTF[4].setText(currUser.getOrganization());
		profileTF[4].setCaretPosition(0);
			
		profileTF[5].setText(currUser.getTitle());
		profileTF[5].setCaretPosition(0);
		
		profileTF[6].setText(currUser.getCellPhone());
		profileTF[6].setCaretPosition(0);
		
		candidateList = groupDB.getAgentGroupList();
		candidateTM.fireTableDataChanged();
			
		memberList = groupDB.getGroupList(currUser.getGroupList());
		memberTM.fireTableDataChanged();
		
		profileChanged = 0;
		btnUpdate.setEnabled(false);
			
		bIgnoreEvents = false;
	}

	void clear() 
	{
		bIgnoreEvents = true;
		
		for(int i=0; i<profileTF.length; i++)
			profileTF[i].setText("");
		
		memberList.clear();
		memberTM.fireTableDataChanged();
	
		profileChanged = 0;
		btnUpdate.setEnabled(false);
		
		bIgnoreEvents = false;
	}
	
	List<Integer> getUserGroupList()
	{
		List<Integer> list = new LinkedList<Integer>();
		for(ONCGroup g : memberList)
			list.add(g.getID());
		
		return list;
	}
	
	void checkIfGroupsChanged()
	{
		boolean bUserGroupsChanged = false;
		if(memberList.size() != currUser.getGroupList().size())
			bUserGroupsChanged = true;
		else
		{
			for(ONCGroup userGroup : groupDB.getGroupList(currUser.getGroupList()))
			{
				int index = 0;
				while(index < memberList.size() && memberList.get(index).getID() != userGroup.getID())
					index++;
				
				if(index == memberList.size())
				{
					System.out.println(String.format("NewUpdProfile.checkIfGroupsChagned: profileChanged= %d", profileChanged));
					bUserGroupsChanged = true;
				}
			}
		}
		
		if(bUserGroupsChanged)
			profileChanged = profileChanged | GROUP_MASK;
		else
			profileChanged = profileChanged & ~GROUP_MASK;
		
		btnUpdate.setEnabled(profileChanged > 0);
	}
	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		if(lse.getSource() == memberTbl.getSelectionModel())
		{
			if(memberTbl.getSelectedRow() > -1)
			{
				btnAddMember.setEnabled(false);
				btnRemoveMember.setEnabled(true);
				candidateTbl.clearSelection();
			}
		}
		else if(lse.getSource() == candidateTbl.getSelectionModel())
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

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_USER"))
		{
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			
			//If current user is being displayed has changed, reshow it
			if(currUser != null && currUser.getID() ==  updatedUser.getID())
				display(updatedUser); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_USER"))
		{
			//if the deleted user was the only user in data base, clear the display
			//otherwise, if the deleted user is currently being displayed, change the index
			//to the prior user and display.
			if(userDB.size() == 0)
			{
				System.exit(0);	
			}
			else
			{
				ONCUser deletedUser = (ONCUser) dbe.getObject1();
				if(currUser != null && currUser.getID()  == deletedUser.getID())
				{
					System.exit(0);
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_GROUP"))
		{
			ONCGroup addedGroup = (ONCGroup) dbe.getObject1();
			if(addedGroup.getType() != GroupType.Any && addedGroup.getType() == GroupType.Volunteer)
			{	
				candidateList.add(addedGroup);
				candidateTM.fireTableDataChanged();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GROUP"))
		{
			ONCGroup updatedGroup = (ONCGroup) dbe.getObject1();
			
			//refresh the candidate table
			candidateList = groupDB.getAgentGroupList();
			candidateTM.fireTableDataChanged();
				
			//if the user is in the updated group, update the table
			int index = 0;
			
			while(index < memberList.size() && memberList.get(index).getID() != updatedGroup.getID())
				index++;
			
			if(index < memberList.size())
			{
				memberList.remove(index);
				memberList.add(index, updatedGroup);
				memberTM.fireTableDataChanged();
			}	
		}
	}
	
	void checkForChanges(KeyEvent e)
	{
		String[] userProfileElement = new String[] { currUser.getFirstName(), currUser.getLastName(),
													currUser.getEmail(), currUser.getHomePhone(),
													currUser.getOrganization(), currUser.getTitle(),
													currUser.getCellPhone()};
		int index = 0, mask = 1;
		while(index < profileTF.length && !e.getSource().equals(profileTF[index]))
			index++;
		
		if(index < profileTF.length)
		{
			mask = mask << index;
			if(profileTF[index].getText().equals(userProfileElement[index]))
				profileChanged = profileChanged & ~mask;
			else
				profileChanged = profileChanged | mask;
			
			btnUpdate.setEnabled(profileChanged > 0);
		}
	}
	
	private class ProfileChangeListener implements KeyListener
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
			checkForChanges(e);
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			checkForChanges(e);
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			checkForChanges(e);
		}
	}
	private class MemberTableModel extends AbstractTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int NAME_COL = 0;
//		private static final int COUNT_COL = 1;
//		private static final int STATUS_COL = 2;
//		private static final int PERMISSION_COL = 3;
		
		public String[] columnNames = {"Group Name"};
		
		@Override
		public String getColumnName(int col) { return columnNames[col]; }
		
		@Override
		public int getColumnCount() { return columnNames.length; }

		@Override
		public int getRowCount() { return memberList.size(); }

		@Override
		public Object getValueAt(int row, int col)
		{
			ONCGroup g = memberList.get(row);
			if(col == NAME_COL && g != null)
				return g.getName();
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
		private static final int NAME_COL = 0;
//		private static final int LAST_NAME_COL = 1;
		
		private String[] columnNames = {"Group Name"};
		
		@Override
		public String getColumnName(int col) { return columnNames[col]; }
		
		@Override
		public int getColumnCount() { return columnNames.length; }
	
		@Override
		public int getRowCount() { return candidateList.size(); }

		@Override
		public Object getValueAt(int row, int col)
		{
			ONCGroup g = candidateList.get(row);
			if(col == NAME_COL)
				return g.getName();
			else
				return "Error";
		}		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnAddMember && candidateTbl.getSelectedRow() > -1)
		{
			ONCGroup selectedGroup = candidateList.get(candidateTbl.getSelectedRow());
			
			//check to see if user is already in the selected group. If they are, don't add them
			int index = 0;
			while(index < memberList.size() && memberList.get(index).getID() != selectedGroup.getID())
				index++;
			
			if(index == memberList.size())
			{
				//update the user with the added group
				memberList.add(selectedGroup);
				memberTM.fireTableDataChanged();
			}
			
			candidateTbl.clearSelection();
			btnAddMember.setEnabled(false);
			checkIfGroupsChanged();
		}
		else if(e.getSource() == btnRemoveMember && memberTbl.getSelectedRow() > -1)
		{
			ONCGroup selectedGroup = memberList.get(memberTbl.getSelectedRow());
			memberList.remove(selectedGroup);
			memberTM.fireTableDataChanged();
			
			memberTbl.clearSelection();
			btnRemoveMember.setEnabled(false);
			checkIfGroupsChanged();
		}
		else if(e.getSource() == btnUpdate)
		{
			update();
		}
		else if(e.getSource() == btnNoChange)
		{
			if(bReviewRequired)
				update();
			else
				dispose();
		}
	}
}
