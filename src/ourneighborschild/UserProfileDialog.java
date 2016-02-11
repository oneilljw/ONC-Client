package ourneighborschild;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class UserProfileDialog extends InfoDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCUser user;
	private UserDB userDB;

	UserProfileDialog(JFrame owner, String[] tfNames, ONCUser user) 
	{
		super(owner, true, tfNames);
		this.user = user;
		this.setTitle(String.format("User Profile for %s", user.getLastname()));
		
		//connect to User DB and register this dialog as a listener
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);

		lblONCIcon.setText(String.format("<html><font color=blue>User Profile Information for<br>%s %s</font></html>", 
							user.getFirstname(), user.getLastname()));

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < tfNames.length; pn++)
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
		tf[0].setText(user.getOrg());
		tf[1].setText(user.getTitle());
		tf[2].setText(user.getEmail());
		tf[3].setText(user.getPhone());
	}

	@Override
	void update() 
	{
		//update user profile request
		ONCUser reqUpdateUser = new ONCUser(user);
		reqUpdateUser.setOrg(tf[0].getText());
		reqUpdateUser.setTitle(tf[1].getText());
		reqUpdateUser.setEmail(tf[2].getText());
		reqUpdateUser.setPhone(tf[3].getText());
		
		//send update request to server
		String response = userDB.update(this, reqUpdateUser);
		if(response != null && response.startsWith("UPDATED_USER"))
		{
			//update the user
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
		return user.getOrg().equals(tf[0].getText()) && user.getTitle().equals(tf[1].getText()) &&
				user.getEmail().equals(tf[2].getText()) && user.getPhone().equals(tf[3].getText());
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
}
