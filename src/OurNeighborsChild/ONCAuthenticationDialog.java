package OurNeighborsChild;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import com.google.gson.Gson;

public class ONCAuthenticationDialog extends ONCConnectDialog
{
	/*************************************************************************************
	 * This class implements the authentication dialog for the ONC application. The 
	 * constructor attempts create a connection to the ONC Server. If the ONC Server 
	 * connection fails, the constructor substitutes a hard coded user list. The dialog
	 * subclasses the ONCConnectDialog class.
	 * 
	 * A modal login dialog is constructed that has a user id text field and a password
	 * text field using JPasswordField. If the user successfully enters a user id and password
	 * that is found on the server or the hard coded user list, the login is successful. 
	 * If the user is unable to complete the login, the dialog loops until that occurs or the 
	 * dialog is canceled. Upon cancel, the application exits.
	 * 
	 * The dialog has a debug_mode flag. When set, it fills the user id and password
	 * fields and doesn't try to read the external user file, using the hard coded user
	 * information instead to construct the users array list class member. 
	 * 
	 * The dialog listens for parent move events and relocates the dialog accordingly
	 * 
	 * The users.dat file contains a record for each user. Each record contains five fields:
	 * 	Field 0 - user id
	 * 	Field 1 - password
	 * 	Field 2 - user permission level: SUPERUSER, ADMIN, or GENERALUSER
	 * 	Field 3 - User First Name
	 * 	Field 4 - User Last Name
	 ************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int USER_USERID_FIELD = 0;
	private static final int USER_PASSWORD_FIELD = 1;
	private static final int USER_PERMISSION_FIELD = 2;
	private static final int USER_LAST_NAME_FIELD = 4;
	private static final int USER_FIRST_NAME_FIELD = 3;
	
	private int count;	//Login attempts
	private JPasswordField passwdPF;
	private ArrayList<String[]>  usersAL;
	private ONCUser userObj;
		
	public ONCAuthenticationDialog(final JFrame parent, boolean debug_mode)
	{
		super(parent);
		this.setTitle("Our Neighbor's Child Login");

		//Initialize the class variables
		userObj = null;
       	count = 0;
       		
		//Layout GUI
       	lblMssg1.setText("<html><b><i>Welcome to Our Neighbor's Child</i></b><br></html>");
		lblMssg2.setText("<html>Working Offline, Please Login</html>");
		lblTF1.setText("User Name:");
		lblTF2.setText("Password:  ");
		passwdPF = new JPasswordField(12);
		p4.add(passwdPF);
		
		//To make login quicker for debug, pre-fill user id and password
		if(debug_mode)
		{
			tf1.setText("KMLavin");
			passwdPF.setText("Redskins5");
		}
		
		btnAction.setText("Login");
		
		//Determine if connected the server or working off-line
        if(serverIF != null && serverIF.isConnected())
        	connectedToServer();
        else
        	notConnectedToServer();
	}
	
	void connectedToServer()
	{
		//Display login message from server
    	lblMssg2.setText("<html>"+ serverIF.getLoginMssg().substring(5) +"</html>");
    	this.setTitle("Connected to ONC Server");
	}
	
	void notConnectedToServer()
	{
		usersAL = new ArrayList<String[]>();	
  	  
    	String[][] users = {{"john", "erin1992", "SYS_ADMIN", "John","O'Neill"},
  				   		  {"KMLavin", "Redskins5", "SYS_ADMIN", "Kelly", "Lavin"},
  				   		  {"nicole", "chester", "ADMIN", "Nicole", "Rogers"},
  				   		  {"ONC Guest", "oncguest", "GENERAL", "", "ONC Guest"},
  				   		  {"ONC Admin", "oncadmin", "ADMIN", "", "ONC Admin"}};
  			 
  		for(int i =0; i<users.length; i++)	//build array list from users[][]
  			usersAL.add(users[i]);
	}
	
	/******************************************************************************
	 * ONCConnectDialog base class calls this when the "Login" button is pressed
	 * When connected to ONC Server, send LOGIN command to server, wait for response
	 *****************************************************************************/
	void onServerAttempt()
	{
		String response = "";	
		Login loginReq = new Login(tf1.getText(), new String(passwdPF.getPassword()), GlobalVariables.getVersion());
		
		if(serverIF != null && serverIF.isConnected())
		{
			//change the cursor
			try 
			{
				Gson gson = new Gson();
				
	            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	            
	            //create the login request, including the user id and password json
//				response = serverIF.sendRequest("LOGIN_REQUEST{userid:\"" + uid + "\",password:\"" + pw + "\"}");
				response = serverIF.sendRequest("LOGIN_REQUEST" + gson.toJson(loginReq, Login.class));
				
				if (response.startsWith("INVALID"))	//Login unsuccessful, print retry message
				{
					String reason = response.substring(7);
					
					if(reason.startsWith("Downlevel"))
						lblMssg2.setText("<html><font color=red><b>" + reason + "</b></font></html>");
					else if(reason.startsWith("Inactive"))
						lblMssg2.setText("<html><font color=red><b>" + reason + ", please contact Exec Dir</b></font></html>");
					else
						lblMssg2.setText("<html><font color=red><b>" + reason + ", please try agian</b></font></html>");
					
//					lblMssg2.setText("<html><font color=red><b>" + reason + remedy + "</b></font></html>");
					btnAction.setText("Login Try " + Integer.toString(++count));
					passwdPF.requestFocus();
					passwdPF.selectAll();
				}
	            else if(response.startsWith("VALID"))	//Login successful, now get Userslist
	            {             	
	            	//Create the user object
//	            	Gson gson = new Gson();
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
	
	void onLocalAttempt()
	{
		String uid = tf1.getText();
		String pw = new String(passwdPF.getPassword());
		
		//Search for the user id and password in the users array list. If a match is
		//found the login is successful. If not match is found, then set a new message
		//and return, effectively having the dialog loop. 
		int index = 0;
		while(index < usersAL.size() &&
			(!usersAL.get(index)[USER_USERID_FIELD].equals(uid) ||
			!usersAL.get(index)[USER_PASSWORD_FIELD].equals(pw)))
		{
			index++;	//No user id and password yet, try the next user record
		}
	
		if(index == usersAL.size() )	//User id and password match not found
		{
			lblMssg2.setText("<html><FONT COLOR = RED><b>Unrecognized entry, please try again</b></FONT></html>");
			btnAction.setText("Login Try " + Integer.toString(++count));
			passwdPF.requestFocus();
			passwdPF.selectAll();
		}
		else	//Match was found, create the user object and exit
		{
			userObj = new ONCUser(index, new Date(), "", 3, "", "", usersAL.get(index)[USER_FIRST_NAME_FIELD],
										usersAL.get(index)[USER_LAST_NAME_FIELD],
										UserPermission.valueOf(usersAL.get(index)[USER_PERMISSION_FIELD]),
										0, new Date(), false);
										
			this.dispose();
		}
	}
	
	/******************************************************************************
	 * This method parses a valid login response from the server and populates
	 * the user information for the application to fetch.
	 * @return
	 ******************************************************************************/
	void parseUser(String response)
	{
		if(response.contains("permission:") && response.contains("firstname:") &&
							response.contains("lastname:"))
		{	
			Gson gson = new Gson();
			userObj = gson.fromJson(response, ONCUser.class);
		}		
	}
	
	ONCUser getUser() { return userObj; }
}
