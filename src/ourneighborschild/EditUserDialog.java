package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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

public class EditUserDialog extends EntityDialog implements ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int PREFERRED_NUMBER_OF_TABLE_ROWS = 10;


	private GroupDB groupDB;
	
	private ONCTable memberTbl, candidateTbl;
	private MemberTableModel memberTM;
	private CandidateTableModel candidateTM;
	private JTextField firstnameTF, lastnameTF, usernameTF, orgTF, titleTF, emailTF, phoneTF;
	private JLabel lblLastChangedBy, lblDateChanged, lblLogins, lblLastLogin;
	private JComboBox<UserStatus> statusCB;
    private JComboBox<UserAccess> accessCB;
    private JComboBox<UserPermission> permissionCB;
    private JButton btnAddMember, btnRemoveMember;
    private JCheckBox ckBoxSameAsEmail;
    private SimpleDateFormat sdf;
    
    private ONCUser currUser;	//reference to ONCUser object being displayed
    private List<ONCGroup> memberList; //holds groups the new user is being added to
    private List<ONCGroup> candidateList; //holds available groups

	EditUserDialog(JFrame pf) 
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Edit App & Website Users");
		
		groupDB = GroupDB.getInstance();
		
		//register to listen for user and group data change events
		if(groupDB != null)
			groupDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//initialize the table lists
		memberList = new LinkedList<ONCGroup>();
		candidateList = new ArrayList<ONCGroup>();
		
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, userDB);
        nav.setDefaultMssg("Our Neighbor's Child Users");
        nav.setCount1("Total: " + Integer.toString(0));
        nav.setCount2("Season: " + Integer.toString(0));

        //set up the edit user panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        //set up panel 1
        firstnameTF = new JTextField(12);
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.addActionListener(dcListener);
        
        lastnameTF = new JTextField(12);
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.addActionListener(dcListener);
        
        emailTF = new JTextField(20);
        emailTF.setBorder(BorderFactory.createTitledBorder("Email"));
        emailTF.addActionListener(dcListener);
        
        phoneTF = new JTextField(10);
        phoneTF.setBorder(BorderFactory.createTitledBorder("Phone"));
        phoneTF.addActionListener(dcListener);
        
        p1.add(firstnameTF);
        p1.add(lastnameTF);
        p1.add(emailTF);
        p1.add(phoneTF);
        
        //set up op2
        orgTF = new JTextField(18);
        orgTF.setBorder(BorderFactory.createTitledBorder("School/Organization"));
        orgTF.setToolTipText("What organization does this user belong too?");
        orgTF.addActionListener(dcListener);
        
        titleTF = new JTextField(16);
        titleTF.setBorder(BorderFactory.createTitledBorder("Title"));
        titleTF.setToolTipText("What job title does this user hold?");
        titleTF.addActionListener(dcListener);
        
        lblLastChangedBy = new JLabel("No one");
        lblLastChangedBy.setToolTipText("Who last changed this user's info");
        lblLastChangedBy.setPreferredSize(new Dimension (124, 48));
        lblLastChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
        
        lblDateChanged = new JLabel("Never");
        lblDateChanged.setToolTipText("Timestamp when user info last changed");
        lblDateChanged.setPreferredSize(new Dimension (120, 48));
        lblDateChanged.setBorder(BorderFactory.createTitledBorder("Date Changed"));
        sdf = new SimpleDateFormat("MM/dd/yyyy");
        
        //set up panel 2
        p2.add(orgTF);
        p2.add(titleTF);
        p2.add(lblDateChanged);
        p2.add(lblLastChangedBy);
        
        //set up o03
        accessCB = new JComboBox<UserAccess>(UserAccess.values());
        accessCB.setBorder(BorderFactory.createTitledBorder("User Access"));
        accessCB.setToolTipText("What environments may this user login to?");
        accessCB.addActionListener(dcListener);
 
        permissionCB = new JComboBox<UserPermission>(UserPermission.values());
        permissionCB.setBorder(BorderFactory.createTitledBorder("User Permission"));
        permissionCB.setToolTipText("What level of data is this user allowed to view/process?");
        permissionCB.addActionListener(dcListener);
        
        statusCB = new JComboBox<UserStatus>(UserStatus.getStatusValues());
        statusCB.setBorder(BorderFactory.createTitledBorder("User Status"));
        statusCB.setToolTipText("What status should this user have?");
        statusCB.addActionListener(dcListener);
        
        lblLogins = new JLabel();
        lblLogins.setToolTipText("How many times has this user logged in (all-time)?");
        lblLogins.setPreferredSize(new Dimension (80, 48));
        lblLogins.setBorder(BorderFactory.createTitledBorder("# Logins"));
        lblLogins.setHorizontalAlignment(JLabel.CENTER);
        
        lblLastLogin = new JLabel("Never");
        lblLastLogin.setToolTipText("When did this user last log in?");
        lblLastLogin.setPreferredSize(new Dimension (120, 48));
        lblLastLogin.setBorder(BorderFactory.createTitledBorder("Last Login"));
        
        usernameTF = new JTextField(20);
        usernameTF.setBorder(BorderFactory.createTitledBorder("User Name"));
        usernameTF.setToolTipText("What User Name does this user want?");
        usernameTF.setVisible(false);

        ActionListener usernameAndResetListener = new SameAsEmailListener();
        ckBoxSameAsEmail = new JCheckBox("Same as Email?");
        ckBoxSameAsEmail.setToolTipText("Check to use users email dddress as their User Name");
        ckBoxSameAsEmail.setVisible(false);
        ckBoxSameAsEmail.addActionListener(usernameAndResetListener);
        
        p3.add(accessCB);
        p3.add(permissionCB);
        p3.add(statusCB);
        p3.add(lblLogins);
        p3.add(lblLastLogin);
        p3.add(usernameTF);
        p3.add(ckBoxSameAsEmail);
        
        //set up panel 4
        JPanel memberPanel = new JPanel();	//left panel
		JPanel btnPanel = new JPanel();	//center panel
		JPanel candidatePanel = new JPanel();	//right panel
		
		//set up the member panel - it will contain the member table
		memberPanel.setLayout(new BoxLayout(memberPanel, BoxLayout.Y_AXIS));
		memberPanel.setBorder(BorderFactory.createTitledBorder("Group(s) User Participates In"));
		
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
        
        GroupButtonListener gbListener = new GroupButtonListener();
        
        btnAddMember = new JButton("<- Add");
        btnAddMember.setToolTipText("Click to add user to highlighted group");
        btnAddMember.setEnabled(false);
        btnAddMember.addActionListener(gbListener);
        btnPanel.add(btnAddMember);
        
        btnRemoveMember = new JButton("Remove");
        btnRemoveMember.setToolTipText("Click to remove user from highlighted group");
        btnRemoveMember.setEnabled(false);
        btnRemoveMember.addActionListener(gbListener);
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
        entityPanel.add(p3);
        entityPanel.add(p4);
       
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New User");
        btnNew.setToolTipText("Click to add a new user");
        
        btnDelete.setText("Reset Password");
        btnDelete.setToolTipText("Click to reset the displayed users password");
        
        btnSave.setText("Save New User");
        btnSave.setToolTipText("Click to save the new user");
        
        btnCancel.setText("Cancel Add New User");
    		btnCancel.setToolTipText("Click to cancel adding a new user");
    		
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        this.pack();
        this.setResizable(true);
        this.setMinimumSize(new Dimension(768, 550));
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_USER"))
		{
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			
			//If current user is being displayed has changed, reshow it
			if(currUser != null && currUser.getID() ==  updatedUser.getID() && !bAddingNewEntity)
				display(updatedUser); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_USER"))
		{
			//if the deleted user was the only user in data base, clear the display
			//otherwise, if the deleted user is currently being displayed, change the index
			//to the prior user and display.
			if(userDB.size() == 0)
			{
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCUser deletedUser = (ONCUser) dbe.getObject1();
				if(currUser != null && currUser.getID()  == deletedUser.getID() && !bAddingNewEntity )
				{
					if(nav.getIndex() == 0)
						nav.setIndex(userDB.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(userDB.getObjectAtIndex(nav.getIndex()));
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
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_GROUPS"))
		{
			candidateList = groupDB.getAgentGroupList();
			candidateTM.fireTableDataChanged();
			
			//loading users occurs before loading groups. Use loading of groups as trigger to 
			//display the first user in the database. If the trigger was loading users, the 
			//groups the first user is a member of wouldn't be available resulting in a error.
			if(currUser == null)
				display(currUser);
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
		if(!bAddingNewEntity)
		{	
			if(tse.getSource() != nav && (tse.getType() == EntityType.USER || tse.getType() == EntityType.AGENT))
			{
				ONCUser user = (ONCUser) tse.getObject1();
				update();
				nav.setIndex(userDB.getListIndexByID(userDB.getList(), user.getID()));
				display(user);
			}
			else if(tse.getSource() == nav && tse.getType() == EntityType.USER)
			{
				update();
				display(userDB.getObjectAtIndex(nav.getIndex()));
			}
			else if(tse.getSource() != nav && tse.getType() == EntityType.FAMILY)
			{
				ONCFamily fam = (ONCFamily) tse.getObject1();
				ONCUser user = userDB.getUser(fam.getAgentID());
				update();
				nav.setIndex(userDB.getListIndexByID(userDB.getList(), user.getID()));
				display(user);
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.USER, EntityType.AGENT, EntityType.FAMILY);
	}

	@Override
	void update()
	{
		if(currUser != null && !memberList.isEmpty())
		{
			ONCUser reqUpdateUser = new ONCUser(currUser);	//make a copy for update request
			int bCD = 0; //used to indicate a change to the user has been detected	
			if(!firstnameTF.getText().equals(reqUpdateUser.getFirstName())) { reqUpdateUser.setFirstName(firstnameTF.getText()); bCD = bCD | 1; }
			if(!lastnameTF.getText().equals(reqUpdateUser.getLastName())) { reqUpdateUser.setLastName(lastnameTF.getText()); bCD = bCD | 2; }
			if(!emailTF.getText().equals(reqUpdateUser.getEmail())) { reqUpdateUser.setEmail(emailTF.getText()); bCD = bCD | 4; }
			if(!phoneTF.getText().equals(reqUpdateUser.getHomePhone())) { reqUpdateUser.setHomePhone(phoneTF.getText()); reqUpdateUser.setCellPhone(phoneTF.getText());bCD = bCD | 8; }
			if(!orgTF.getText().equals(reqUpdateUser.getOrganization())) { reqUpdateUser.setOrganization(orgTF.getText()); bCD = bCD | 16; }
			if(!titleTF.getText().equals(reqUpdateUser.getTitle())) { reqUpdateUser.setTitle(titleTF.getText()); bCD = bCD | 32; }
			if(statusCB.getSelectedItem() != reqUpdateUser.getStatus()) { reqUpdateUser.setStatus((UserStatus)statusCB.getSelectedItem()); bCD = bCD | 64; }
			if(accessCB.getSelectedItem() != reqUpdateUser.getAccess()) { reqUpdateUser.setAccess((UserAccess)accessCB.getSelectedItem()); bCD = bCD | 128; }
			if(permissionCB.getSelectedItem() != reqUpdateUser.getPermission()) { reqUpdateUser.setPermission((UserPermission)permissionCB.getSelectedItem()); bCD = bCD | 256; }
			if(memberList.size() != reqUpdateUser.getGroupList().size()){ reqUpdateUser.setGroupList(getUserGroupList()); bCD = bCD | 512; }
			
			if(bCD > 0)	//If an update to partner data (not stop light data) was detected
			{
//				System.out.println(String.format("EditUserDlg.update: bCD= %d", bCD));
				reqUpdateUser.setChangedBy(userDB.getUserLNFI());
			
				String response = userDB.update(this, reqUpdateUser);	//notify the database of the change
			
				if(response.startsWith("UPDATED_USER"))
				{
					Gson gson = new Gson();
					ONCUser updatedUser = gson.fromJson(response.substring(12), ONCUser.class);
				
					display(updatedUser);
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
		if(userDB.size() <= 0 )
		{
			currUser= null;
			clear();
			firstnameTF.setText("No Users Yet");	//If no users, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
			btnAddMember.setEnabled(false);
			btnRemoveMember.setEnabled(false);
		}
		else
		{
			//Determine what to display based on currUser and user
			if(currUser == null && user == null)
				currUser = (ONCUser) userDB.getObjectAtIndex(0);
			if(currUser == null && user != null)
				currUser = (ONCUser) user;
			else if(currUser != null && user != null)
				currUser = (ONCUser) user;
			
			bIgnoreEvents = true;
			
			firstnameTF.setText(currUser.getFirstName());
			firstnameTF.setCaretPosition(0);
			
			lastnameTF.setText(currUser.getLastName());
			lastnameTF.setCaretPosition(0);
			
			emailTF.setText(currUser.getEmail());
			emailTF.setCaretPosition(0);
			
			phoneTF.setText(currUser.getCellPhone());
			phoneTF.setCaretPosition(0);
			
			orgTF.setText(currUser.getOrganization());
			orgTF.setCaretPosition(0);
			
			titleTF.setText(currUser.getTitle());
			titleTF.setCaretPosition(0);
			
			lblDateChanged.setText(sdf.format(currUser.getDateChanged()));
			lblLastChangedBy.setText(currUser.getChangedBy());
			
			statusCB.setSelectedItem(currUser.getStatus());
			accessCB.setSelectedItem(currUser.getAccess());
			permissionCB.setSelectedItem(currUser.getPermission());
			
			lblLogins.setText(Long.toString(currUser.getNSessions()));
			
			if(currUser.getNSessions() == 0)
				lblLastLogin.setText("Never");
			else
			{
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				calendar.setTimeInMillis(currUser.getLastLogin());
				lblLastLogin.setText(sdf.format(calendar.getTime()));
			}
			
			ckBoxSameAsEmail.setSelected(false);
			usernameTF.setText("");
			
			lblLastChangedBy.setText(currUser.getChangedBy());
			lblDateChanged.setText(sdf.format(currUser.getDateChanged()));
			
			nav.setCount1("Total Users: " + Integer.toString(userDB.size()));
			nav.setCount2("Active Users: " + Integer.toString(userDB.getActiveUserCount()));
			
			nav.setStoplightEntity(currUser);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);

			memberList = groupDB.getGroupList(currUser.getGroupList());
			memberTM.fireTableDataChanged();
			
			bIgnoreEvents = false;
		}
	}

	@Override
	void clear() 
	{
		bIgnoreEvents = true;
		
		firstnameTF.setText("");	
		lastnameTF.setText("");
		emailTF.setText("");
		phoneTF.setText("");
		orgTF.setText("");
		titleTF.setText("");
		lblDateChanged.setText(sdf.format(new Date()));
		lblLastChangedBy.setText(userDB.getLoggedInUser().getLNFI());
		statusCB.setSelectedIndex(0);
		accessCB.setSelectedIndex(0);
		permissionCB.setSelectedIndex(0);
		lblLogins.setText("");
		lblLastLogin.setText("");
		ckBoxSameAsEmail.setSelected(false);
		usernameTF.setText("");
		
		memberList.clear();
		memberTM.fireTableDataChanged();
		
		bIgnoreEvents = false;
	}
	
	List<Integer> getUserGroupList()
	{
		List<Integer> list = new LinkedList<Integer>();
		for(ONCGroup g : memberList)
			list.add(g.getID());
		
		return list;
	}
	
	String isUserInfoCompleteAndValid()
	{
		if(firstnameTF.getText().isEmpty() || lastnameTF.getText().isEmpty())
			return "First Name or Last Name missing.";
		else if(!(emailTF.getText().contains("@") && emailTF.getText().contains(".")))
			return "Email is missing or invalid.";
		else if(!(phoneTF.getText().length() == 10 ||  phoneTF.getText().length() == 12 ||
					phoneTF.getText().length() == 14))
			return "Phone is missing or not a valid format";
		else if(orgTF.getText().isEmpty() || titleTF.getText().isEmpty())
			return "Organization and/or Title is missing";
		else if(usernameTF.getText().isEmpty())
			return "User Name is missing"; 
		else if(memberList.isEmpty())
			return "New user must participate in at least one group.";
		else
			return "";
	}

	@Override
	void onNew() 
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New User's Information"));
		clear();
		statusCB.setVisible(false);
		lblLogins.setVisible(false);
		lblLastLogin.setVisible(false);
		ckBoxSameAsEmail.setVisible(true);
		usernameTF.setVisible(true);
		memberTbl.clearSelection();
		candidateTbl.clearSelection();
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		btnSave.setEnabled(false);
		setControlState();
	}

	@Override
	void onCancelNew() 
	{
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
		statusCB.setVisible(true);
		lblLogins.setVisible(true);
		lblLastLogin.setVisible(true);
		ckBoxSameAsEmail.setVisible(false);
		usernameTF.setVisible(false);
		display(currUser);
		memberTbl.clearSelection();
		candidateTbl.clearSelection();
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}

	@Override
	void onSaveNew() 
	{	
		//construct a new user if all the fields are valid
		String err_mssg = isUserInfoCompleteAndValid();
		if(err_mssg.isEmpty())
		{	
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			ONCServerUser reqAddUser = new ONCServerUser(0, calendar.getTime(), userDB.getUserLNFI(), 3, 
					"New user added", userDB.getUserLNFI(), firstnameTF.getText(), lastnameTF.getText(),
					UserStatus.Change_PW, (UserAccess) accessCB.getSelectedItem(), 
					(UserPermission) permissionCB.getSelectedItem(), usernameTF.getText(), "********", 0,
					calendar.getTimeInMillis(), true, orgTF.getText(), titleTF.getText(),
					emailTF.getText(), phoneTF.getText(), getUserGroupList());
		
			//check for a duplicate user. Search twice, once for duplicate user names and
			//once for duplicate email addresses
			ArrayList<Integer> searchList = new ArrayList<Integer>();
			userDB.searchForListItem(searchList, usernameTF.getText());
			if(searchList.isEmpty()) 
				userDB.searchForListItem(searchList, emailTF.getText());
			
			if(searchList.isEmpty())
			{
				//no duplicate user name or email found, send add user request
				ONCUser addedUser = (ONCUser) userDB.add(this, reqAddUser);
					
				if(addedUser != null)
				{
					//set the display index to the newly added user and display the user
					nav.setIndex(userDB.getListIndexByID(userDB.getList(), addedUser.getID()));
					display(addedUser);
				}
				else
				{
					err_mssg = "ONC Server denied add user request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Add User Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					display(currUser);
				}
					
				//reset to review mode and display the proper partner
				nav.navSetEnabled(true);
				entityPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
				entityPanel.setBackground(pBkColor);
				statusCB.setVisible(true);
				lblLogins.setVisible(true);
				lblLastLogin.setVisible(true);
				ckBoxSameAsEmail.setVisible(false);
				usernameTF.setVisible(false);
			
				memberTbl.clearSelection();
				candidateTbl.clearSelection();
				
				bAddingNewEntity = false;
				setControlState();
			}
			else
			{	
				//get the users who are duplicate
				List<ONCUser> dupUsers = new ArrayList<ONCUser>();
				for(Integer id : searchList)
					dupUsers.add(userDB.getUser(id));
			
				//if there are duplicate users, form a error message
				StringBuffer buff = new StringBuffer("<html><b><font color=\"red\">Error: duplicate User Name or<br>Email Address found for users:<font color=\"black\"></b><br><br>");
				for(ONCUser dupUser : dupUsers)
					buff.append(dupUser.getFirstName() + " " + dupUser.getLastName() +",<br>");
				
				buff.append("<br><b><i>Please try again with a different<br>User Name and/or Email address.</i></b></html>");
				
				JOptionPane.showMessageDialog(this, buff.toString(), "Duplicate UserName or Email",
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
		else
		{
			String mssg = String.format("<html>Error: <b><i>%s.</i></b><br>"
							+ "Please correct and try to save again.</html>", err_mssg);
			
			JOptionPane.showMessageDialog(this, mssg, "Incomplete or Invalid Info",
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}		
	}

	@Override
	void onDelete() 
	{
		//Confirm with the user they really intended to reset the password
		String confirmMssg = String.format("<html>Are you sure you want to resete<br>the password for %s %s?</html>", 
											currUser.getFirstName(), currUser.getLastName());
	
		Object[] options= {"Cancel", "Reset"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Password Reset ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Reset"))
		{
			statusCB.setSelectedItem(UserStatus.Reset_PW);
		}			
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
			if(col == NAME_COL)
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
	
	private class GroupButtonListener implements ActionListener
	{
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
					btnSave.setEnabled(true);
					memberTM.fireTableDataChanged();
					if(!bAddingNewEntity)
						update();
				}
				
				candidateTbl.clearSelection();
				btnAddMember.setEnabled(false);
			}
			else if(e.getSource() == btnRemoveMember && memberTbl.getSelectedRow() > -1)
			{
				ONCGroup selectedGroup = memberList.get(memberTbl.getSelectedRow());
				memberList.remove(selectedGroup);
				btnSave.setEnabled(!memberList.isEmpty());
				memberTM.fireTableDataChanged();
				
				memberTbl.clearSelection();
				btnRemoveMember.setEnabled(false);
				
				if(!bAddingNewEntity)
					update();
			}
		}
	}
	
	/***********************************************************************************************
	 * This class implements a listener
	 ***********************************************************************************************/
	private class SameAsEmailListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(bAddingNewEntity && e.getSource() == ckBoxSameAsEmail)
				usernameTF.setText(ckBoxSameAsEmail.isSelected() ? emailTF.getText() : "");
		}
	}
}
