package OurNeighborsChild;

import javax.swing.JFrame;
import javax.swing.JPasswordField;


public class ChangePasswordDialog extends InfoDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPasswordField[] pf;
	private String[] pw;
	
	ChangePasswordDialog(JFrame owner, String[] tfNames) 
	{
		super(owner, true, tfNames);
		this.setTitle("Change Your Password");
		
		//initialize data structures
		pf = new JPasswordField[tfNames.length];
		pw = new String[tfNames.length];

		lblONCIcon.setText("<html><font color=blue>Enter Password<br>Information Below</font></html>");
		for(int pn=0; pn< tfNames.length; pn++)
		{
			pw[pn] = "";
			pf[pn] = new JPasswordField(12);
			pf[pn].addKeyListener(tfkl);
			infopanel[pn].add(pf[pn]);
		}
		
		//add text to action button
		btnAction.setText("Submit New Password");
						
		pack();
	}
	
	boolean doNewPasswordsMatch()
	{
		return !pw[1].isEmpty() && !pw[2].isEmpty() && pw[1].equals(pw[2]);
	}
	
	String[] getPWInfo() { return pw; }

	@Override
	void update() 
	{
		pw[0] = new String(pf[0].getPassword());
		pw[1] = new String(pf[1].getPassword());
		pw[2] = new String(pf[2].getPassword());
		
		result = true;
		dispose();
	}

	@Override
	boolean fieldUnchanged() 
	{
		// TODO Auto-generated method stub
		return pf[0].getPassword().length == 0 || 
				pf[1].getPassword().length == 0 || 
				 pf[2].getPassword().length == 0;
	}

	@Override
	void delete() { } // Unused, we don't delete passwords
}
