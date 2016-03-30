package ourneighborschild;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class UserProfileDialog extends InfoDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	 
	private static final int FIRST_NAME_INDEX = 0;
	private static final int LAST_NAME_INDEX = 1;
	private static final int ORG_INDEX = 2;
	private static final int TITLE_INDEX = 3;
	private static final int EMAIL_INDEX = 4;
	private static final int PHONE_INDEX = 5;
	
	private ONCUser user;
	private UserDB userDB;

	UserProfileDialog(JFrame owner, ONCUser user) 
	{
		super(owner, true);
		this.user = user;
		this.setTitle(String.format("User Profile for %s", user.getLastname()));
		
		//connect to User DB and register this dialog as a listener
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);

		lblONCIcon.setText(String.format("<html><font color=blue>User Profile Information for<br>%s %s</font></html>", 
							user.getFirstname(), user.getLastname()));

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(18);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		//display profile data
		display();
		
		//add text to action button
		btnAction.setText("Update Profile");
				
		pack();
	}
	
	void display()
	{
		tf[FIRST_NAME_INDEX].setText(user.getFirstname());
		tf[LAST_NAME_INDEX].setText(user.getLastname());
		tf[ORG_INDEX].setText(user.getOrg());
		tf[TITLE_INDEX].setText(user.getTitle());
		tf[EMAIL_INDEX].setText(user.getEmail());
		tf[PHONE_INDEX].setText(user.getPhone());
	}

	@Override
	void update() 
	{
		//update user profile request
		ONCUser reqUpdateUser = new ONCUser(user);
		reqUpdateUser.setFirstname(tf[FIRST_NAME_INDEX].getText());
		reqUpdateUser.setLastname(tf[LAST_NAME_INDEX].getText());
		reqUpdateUser.setOrg(tf[ORG_INDEX].getText());
		reqUpdateUser.setTitle(tf[TITLE_INDEX].getText());
		reqUpdateUser.setEmail(tf[EMAIL_INDEX].getText());
		reqUpdateUser.setPhone(tf[PHONE_INDEX].getText());
		
		//send update request to server
		String response = userDB.update(this, reqUpdateUser);
		if(response != null && response.startsWith("UPDATED_USER"))
		{
			//update the user
			user.setFirstname(reqUpdateUser.getFirstname());
			user.setLastname(reqUpdateUser.getLastname());
			user.setOrg(reqUpdateUser.getOrg());
			user.setTitle(reqUpdateUser.getTitle());
			user.setEmail(reqUpdateUser.getEmail());
			user.setPhone(reqUpdateUser.getPhone());
		}
		else
		{
			//update failed, redisplay current user
			display();
		}
	}

	@Override
	void delete() 
	{
		// no delete button in this child dialog, unused from parent
	}

	@Override
	boolean fieldUnchanged() 
	{
		return user.getFirstname().equals(tf[FIRST_NAME_INDEX].getText()) && 
				user.getLastname().equals(tf[LAST_NAME_INDEX].getText()) &&
				 user.getOrg().equals(tf[ORG_INDEX].getText()) && 
				  user.getTitle().equals(tf[TITLE_INDEX].getText()) &&
				   user.getEmail().equals(tf[EMAIL_INDEX].getText()) && 
				    user.getPhone().equals(tf[PHONE_INDEX].getText());
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_USER"))
		{
			ONCUser updatedUser = (ONCUser) dbe.getObject();
			if(updatedUser.getID() == user.getID())
			{
				this.user = updatedUser;
				display();
			}
		}
	}

	@Override
	void display(ONCObject obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String[] getDialogFieldNames() 
	{
		return new String[] {"First Name", "last Name", "Organization", "Title", "Email", "Phone"};
	}
}
