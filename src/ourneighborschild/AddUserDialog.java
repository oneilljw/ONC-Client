package ourneighborschild;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class AddUserDialog extends InfoDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int FIRST_NAME_INDEX = 0;
	private static final int LAST_NAME_INDEX = 1;
	private static final int USERID_INDEX = 2;
	private static final int ACCESS_INDEX = 3;
	private static final int PERMISSION_INDEX = 4;
	private static final int ORG_INDEX = 5;
	private static final int TITLE_INDEX = 6;
	private static final int EMAIL_INDEX = 7;
	private static final int PHONE_INDEX = 8;
	
	private JComboBox accessCB, permissionCB;
	private ONCServerUser reqAddUser;	
	
	AddUserDialog(JFrame pf)
	{
		super(pf, true);
		this.setTitle("Add New ONC User");

		lblONCIcon.setText("<html><font color=blue>Add New User<br>Information Below</font></html>");

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(14);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		//set up the transformation panel
		accessCB = new JComboBox(UserAccess.values());
		accessCB.setPreferredSize(new Dimension(158,36));
		infopanel[ACCESS_INDEX].remove(tf[ACCESS_INDEX]);
		infopanel[ACCESS_INDEX].add(accessCB);
		
		permissionCB = new JComboBox(UserPermission.values());
		permissionCB.setPreferredSize(new Dimension(158,36));
		infopanel[PERMISSION_INDEX].remove(tf[PERMISSION_INDEX]);
		infopanel[PERMISSION_INDEX].add(permissionCB);
		
		//add text to action button
		btnAction.setText("Add User");
				
		pack();
	}
	
	ONCServerUser getAddUserReq() { return reqAddUser; }
	
	@Override
	void update()
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		UserAccess userAccess = (UserAccess) accessCB.getSelectedItem();
		UserPermission userPermission = (UserPermission) permissionCB.getSelectedItem();
		String pw = "********";
		reqAddUser = new ONCServerUser(0, calendar.getTime(), userDB.getUserLNFI(), 3, "New user added",
				userDB.getUserLNFI(), tf[FIRST_NAME_INDEX].getText(), tf[LAST_NAME_INDEX].getText(),
				UserStatus.Change_PW, userAccess, userPermission, tf[USERID_INDEX].getText(), pw, 0,
				calendar.getTimeInMillis(), true, tf[ORG_INDEX].getText(), tf[TITLE_INDEX].getText(),
				tf[EMAIL_INDEX].getText(), tf[PHONE_INDEX].getText(), new LinkedList<Integer>());
		
		result = true;
		dispose();
	}

	@Override
	boolean fieldUnchanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void display(ONCObject obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String[] getDialogFieldNames()
	{
		// TODO Auto-generated method stub
		return new String[] {"First Name", "Last Name", "User ID", "Access", "Permission",
			"Organization", "Title", "Email", "Phone"};
	}
}
