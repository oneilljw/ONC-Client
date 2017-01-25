package ourneighborschild;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class ChangeAgentDialog extends InfoDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ONCFamily currFamily;
	ONCUser newUser;
	UserDB userDB;
	FamilyDB familyDB;
	
	ChangeAgentDialog(JFrame pf, boolean bModal)
	{
		super(pf, bModal);
		this.setTitle("Change Agent");
		lblONCIcon.setText("<html><font color=blue>Change Family's Agent</font></html>");
		
		//connect to local User DB
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
				
		//get a reference to Family Db
		familyDB = FamilyDB.getInstance();
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addActionListener(dcl);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
				
		btnAction.setText("Change Agent");
		
		pack();
	}

	@Override
	String[] getDialogFieldNames()
	{
		return new String[] {"Current Agent", "New Agent"};
	}

	@Override
	void display(ONCObject obj)
	{
		if (obj instanceof ONCFamily)
		{
			currFamily = (ONCFamily) obj;
			ONCUser currUser = (ONCUser) userDB.getUser(currFamily.getAgentID());
		
			tf[0].setText(currUser.getLastname());
			tf[0].setCaretPosition(0);
		
			tf[1].setCaretPosition(0);
		}
	}

	@Override
	void update()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void delete()	//is unused in this subclass
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean fieldUnchanged()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}
}
