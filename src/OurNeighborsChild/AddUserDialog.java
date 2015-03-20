package OurNeighborsChild;

import java.awt.Dimension;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class AddUserDialog extends InfoDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox permissionCB;
	private ONCServerUser reqAddUser;	
	
	AddUserDialog(JFrame pf, String[] tfNames)
	{
		super(pf, true, tfNames);
		this.setTitle("Add New ONC App User");

		lblONCIcon.setText("<html><font color=blue>Enter New User's<br>Information Below</font></html>");

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < 4; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		//set up the permission panel
//		String[] permissions = {"General", "Admin", "Super"};
		permissionCB = new JComboBox(UserPermission.values());
		permissionCB.setPreferredSize(new Dimension(158,36));
		infopanel[4].add(permissionCB);
		
		//add text to action button
		btnAction.setText("Add User");
				
		pack();
	}
	
	ONCServerUser getAddUserReq() { return reqAddUser; }
	
	@Override
	void update()
	{
		Date today = new Date();
		reqAddUser = new ONCServerUser(0, today, gvs.getUserLNFI(), 3, "New user added",
				gvs.getUserLNFI(), tf[0].getText(), tf[1].getText(),
				(UserPermission) permissionCB.getSelectedItem(), tf[2].getText(),tf[3].getText(), 0, today);
		
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
}
