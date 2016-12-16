package ourneighborschild;

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPasswordField;

import com.google.gson.Gson;

public class ONCAuthenticationDialog extends ONCConnectDialog
{
	/*************************************************************************************
	 * This class implements the authentication dialog for the ONC application. The 
	 * constructor creates a modal login dialog that subclasses the ONCConnectDialog class.
	 * 
	 * The dialog is has a user id text field and a password text field (using JPasswordField). 
	 * If the user successfully enters a user id and password that is authenticated by the 
	 * server, the login is successful. If the user is unable to complete the login, the dialog 
	 * loops until that occurs or the dialog is canceled. Upon cancel, the application exits.
	 * 
	 * The dialog listens for parent move events and relocates the dialog accordingly
	 ************************************************************************************/
	private static final long serialVersionUID = 1L;
	
	private int count;	//Login attempts
	private JPasswordField passwdPF;
	private ONCUser userObj;
		
	public ONCAuthenticationDialog(final JFrame parent)
	{
		super(parent);
		this.setTitle("Our Neighbor's Child Login");

		//Initialize the class variables
		userObj = null;
       	count = 0;
       		
		//Layout GUI
       	lblMssg1.setText("<html><b><i>Welcome to Our Neighbor's Child</i></b><br></html>");
		lblTF1.setText("User Name:");
		lblTF2.setText("Password:  ");
		passwdPF = new JPasswordField(12);
		p4.add(passwdPF);
		
		btnAction.setText("Login");
		
		//Determine if connected the server
        if(serverIF != null && serverIF.isConnected())
        {	
        	//Display login message from server
        	lblMssg2.setText("<html>"+ serverIF.getLoginMssg().substring(5) +"</html>");
        	this.setTitle("Connected to ONC Server");
        }
	}
	
	/******************************************************************************
	 * When connected to ONC Server, send LOGIN command to server, wait for response
	 *****************************************************************************/
	void onLoginAttempt()
	{
		String response = "";
			
		//Encrypted
		Login loginReq = new Login(EncryptionManager.encrypt(tf1.getText()),
						  EncryptionManager.encrypt(new String(passwdPF.getPassword())),
						   GlobalVariables.getVersion());
		
		if(serverIF != null && serverIF.isConnected())
		{
			
			try 
			{
				Gson gson = new Gson();
				
	            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));	//change the cursor
	            
	            //create the login request, including the user id and password json
				response = serverIF.sendRequest("LOGIN_REQUEST" + gson.toJson(loginReq, Login.class));
				
				if (response.startsWith("INVALID"))	//Login unsuccessful, print retry message
				{
					lblMssg2.setText("<html><font color=red><b>" + response.substring(7) + "</b></font></html>");
					
					btnAction.setText("Login Try " + Integer.toString(++count));
					passwdPF.requestFocus();
					passwdPF.selectAll();
				}
	            else if(response.startsWith("VALID"))	//Login successful, now get Userslist
	            {             	
	            	//Create the user object
	            	userObj = gson.fromJson(response.substring(5), ONCUser.class);

	    			this.dispose();
	            }
	        } 
			finally 
			{
	            this.setCursor(Cursor.getDefaultCursor());
	        }	
		}
	}
	
	ONCUser getUser() { return userObj; }

	
	@Override
	public void actionPerformed(ActionEvent e)
	{	
		if(e.getSource() == btnAction && serverIF != null && serverIF.isConnected())
			onLoginAttempt();	
	}
}
