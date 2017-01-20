package ourneighborschild;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

public class OurNeighborsChild
{
	/**
	 * Executable Main Class for ONC application
	 */
	//Static Final Variables
	private static final int SERVER_CONNECT_RETRY_LIMIT = 3;
	private static final String VERSION = "4.30";
	private static final String APPNAME = "Our Neighbor's Child";
	private static final String ONC_SERVER_IP_ADDRESS_FILE = "serveraddress.txt";
	private static final int MAIN_FRAME_WIDTH = 837;
	private static final int MAIN_FRAME_HEIGHT = 668;
	
	//GUI Objects
	private JFrame oncFrame;
	private JPanel oncContentPane, oncSplashPanel;	
	private GlobalVariables oncGVs;
	private FamilyPanel oncFamilyPanel;
	private ONCMenuBar oncMenuBar;
	
	private DatabaseManager dbManager;	//manages the local data base
	private DialogManager dlgManager;	//manages all dialogs in client
	
	//Server Connection
	private ServerIF serverIF;	
//	private static final String defaultServerAddress = "72.209.233.207";	//Cox based server
//	private static final String defaultServerAddress = "localhost";
//	private static final String defaultServerAddress = "96.127.35.251";	//IDT-Amazon cloud based server
	private static final String defaultServerAddress = "onc.idtus.com";	//IDT-Amazon cloud based server
	private static final int PORT = 8901;

    public OurNeighborsChild()
    {	
    	//If running under MAC OSX, use the system menu bar and set the application title appropriately and
    	//set up our application to respond to the Mac OS X application menu
        if(System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
        {          	
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APPNAME);
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); }
			catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); }
			catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); }
			
            // Generic registration with the Mac OS X application, attempts to register with the Apple EAWT
            // See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
            try
            {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[])null));
                OSXAdapter.setAboutHandler(this,getClass().getDeclaredMethod("about", (Class[])null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[])null));
 //             OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));
            } 
            catch (Exception e)
            {
                System.err.println("Error while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
       
        //Create and show mainframe with splash panel
        createMainFrame();
        
        //Setup networking with the ONC Server
        serverIF = null;
        int retrycount = 0;
        
        //see if there is an override server address file. If there is, use that address in
        //place of the default address. The method will return that address or the default address
        //if the file doesn't exist
        String serverIPAddress = readServerIPAddressFromFile();
        
        //connect to server. First, initialize the encryption manager
        EncryptionManager encryptionMgr = EncryptionManager.getInstance();
        
        while(retrycount < SERVER_CONNECT_RETRY_LIMIT)
        {
        	try 
        	{
        		serverIF = new ServerIF(serverIPAddress, PORT);
        		break;
        	} 
        	catch (SocketTimeoutException e2) 
        	{
        		serverIPAddress = getServerIPAddress(serverIPAddress);
        		if(serverIPAddress == null)
        			break;
        		else
        			retrycount++;
        	} 
        	catch (UnknownHostException e2) 
        	{
        		serverIPAddress = getServerIPAddress(serverIPAddress);
        		if(serverIPAddress == null)
        			break;
        		else
        			retrycount++;
        	} 
        	catch (IOException e2) 
        	{
        		serverIPAddress = getServerIPAddress(serverIPAddress);
        		if(serverIPAddress == null)
        			break;
        		else
        			retrycount++;
        	}
		}
        
        //if the server if is not connected, notify and exit
        if(serverIF == null)
        {
        	String mssg = "<html>Server connection could not established,<br>"
        					+ "please contact the ONC IT director</html>";
        	JOptionPane.showMessageDialog(oncFrame, mssg, "ONC Server Connecton Failure", JOptionPane.ERROR_MESSAGE);
        	System.exit(0);
        }
//      else
//       	writeServerIPAddressToFile(serverIPAddress);	//write the server address successfully used
        
        //since we didn't exit, server is connected, proceed with initialization
        //create global variables, set the main frame and the version number
        oncGVs = GlobalVariables.getInstance();
        oncGVs.setFrame(oncFrame);
        oncGVs.setVersion(VERSION);
        
        //initialize data structures
        dbManager = DatabaseManager.getInstance();	

        //initialize the entity event manager
        EntityEventManager.getInstance();
        
        //create mainframe window for the application
        createandshowGUI();
        
        //Initialize the chat and dialog managers
        ChatManager.getInstance();
        dlgManager = DialogManager.getInstance();

        //Get and authenticate user and privileges with Authentication dialog. Can't get past this
        //modal dialog unless a valid user id and password is authenticated by the server. 
        ONCAuthenticationDialog authDlg = null;
		authDlg = new ONCAuthenticationDialog(oncFrame);	
		authDlg.setVisible(true);
		
		//if we get here, the server has authenticated this client's userID and password
		//must check to see if the password needs to be changed, if so, force the change.
		//if password change isnt required, check to see if user should review their profile.
		ONCUser user = UserDB.getInstance().setLoggedInUser(authDlg.getUser());
		if(user.changePasswordRqrd() && !dlgManager.onChangePassword())
			System.exit(0);
		else if(user.getStatus() == UserStatus.Update_Profile)
		{
			UserProfileDialog upDlg = new UserProfileDialog(oncFrame, user, "Please Review & Update Your Profile:");
			upDlg.setLocationRelativeTo(oncFrame);
			upDlg.showDialog();
		}
		
		if(user.getFirstname().isEmpty())
    		oncFamilyPanel.setMssg("Welcome to Our Neighbor's Child!", true);
    	else
    		oncFamilyPanel.setMssg(user.getFirstname() + ", welcome to " +
    								"Our Neighbor's Child!", true);
		
		//Connected & logged in to server
		if(serverIF != null && serverIF.isConnected())
			oncMenuBar.setEnabledServerConnected(true);
		
        if(user.getPermission() == UserPermission.Sys_Admin)	//Superuser privileges
        {
        	dlgManager.setEnabledAdminPrivileges(true);
        	oncMenuBar.setVisibleAdminFunctions(true);
        	oncMenuBar.setVisibleSpecialImports(true);
        	dlgManager.setEnabledSuperuserPrivileges(true);
        }
        else if(user.getPermission() == UserPermission.Admin)
        {
        	oncMenuBar.setVisibleAdminFunctions(true);
        	dlgManager.setEnabledAdminPrivileges(true);
        }
     
        //get database years from server to set the data menu item for user to select and get user db so 
        //a chat can start
        if(serverIF != null)
        {
        	//get the list of data bases on the server
        	List<DBYear> dbYears = dbManager.getDBStatus();
        	if(dbYears != null)
        		oncMenuBar.processDBYears(dbYears);	
        	
        	//get encryption keys
        	encryptionMgr.importKeyMapFromServer();
        	
        	//get user data base
        	UserDB.getInstance().importUserDatabase();
        }
        
        //remove splash panel after authentication
        oncContentPane.remove(oncSplashPanel);
        oncContentPane.revalidate();
        
        //initialize web site status
        oncGVs.initializeWebsiteStatusFromServer();
       
        //everything is initialized, start polling server for changes
        if(serverIF != null && serverIF.isConnected())
        	serverIF.setEnabledServerPolling(true);
        
        //diagnostic for who is a registered listener for FamilyDB data changed events
//        FamilyDB fDB = FamilyDB.getInstance();
//        for(DatabaseListener dbl : fDB.getListenerList())
//        	System.out.println(dbl.toString());
    }
    
    String getServerIPAddress(String serverIPAddress)
    {
    	ServerIPDialog sipDlg = null;
    	
    	sipDlg = new ServerIPDialog(oncFrame, serverIPAddress);
    	sipDlg.setVisible(true);
    	
    	return sipDlg.getNewAddress();
    }
    
    String readServerIPAddressFromFile()
    {
    	String serverIPAddress;
    	String line;
    	
    	//Construct FileReader and BufferedReader from FileReader
    	FileReader reader = null;
    	BufferedReader br = null;
    	
		try
		{
			reader = new FileReader(System.getProperty("user.dir") + "/" + ONC_SERVER_IP_ADDRESS_FILE);
			br = new BufferedReader(reader);
			
			serverIPAddress = (line = br.readLine()) == null ? defaultServerAddress : line;
			
			br.close();
			
			return serverIPAddress;
		}
		catch (FileNotFoundException e) { return defaultServerAddress; }
    	catch (IOException e) { return defaultServerAddress; }
    }
/*    
    void writeServerIPAddressToFile(String ipAddress)
    {
    	PrintWriter outputStream = null;
        FileWriter fileWriter = null;
       
		try
		{
			fileWriter = new FileWriter(System.getProperty("user.dir") + "/" + ONC_SERVER_IP_ADDRESS_FILE);
			outputStream = new PrintWriter(fileWriter);
			 
		    outputStream.println(ipAddress);
		    System.out.println("Wrote " + ipAddress + " to " + System.getProperty("user.dir") + "/" + ONC_SERVER_IP_ADDRESS_FILE);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			if(outputStream != null)
				outputStream.close();	
		}
    }
*/    
    // General quit handler; fed to the OSXAdapter as the method to call when a system quit event occurs
    // A quit event is triggered by Cmd-Q, selecting Quit from the application or Dock menu, or logging out
    public boolean quit()
    {
    	String response = "";
		response = serverIF.sendRequest("QUIT");
		
    	if(response.equals("GOODBYE"))
    		serverIF.close();
    			
    	return true;
    }
    
    // General info dialog; fed to the OSXAdapter as the method to call when 
    // "About OSXAdapter" is selected from the application menu   
    public void about()
    {
    	dlgManager.showAboutONCDialog();
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences()
    {
    	dlgManager.showPreferencesDialog(); 	
    }
    
    private void createMainFrame()
    {
    	oncFrame = new JFrame(APPNAME);
		oncFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we)
			 {
				exit("QUIT");			  
			 }});
        oncFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	//On close, user is prompted to confirm
        oncFrame.setMinimumSize(new Dimension(MAIN_FRAME_WIDTH, MAIN_FRAME_HEIGHT));
        oncFrame.setLocationByPlatform(true);
        
        //Create a content panel for the frame and add components to it.
        oncContentPane = new JPanel();
        oncContentPane.setLayout(new BoxLayout(oncContentPane, BoxLayout.PAGE_AXIS));
          
        // set up a splash screen panel
      	oncSplashPanel = new JPanel();        
      	JLabel lblONCicon = new JLabel(createImageIcon("oncsplash.gif", "ONC Full Screen Logo"));
      	oncSplashPanel.add(lblONCicon);	 
        oncContentPane.add(oncSplashPanel);
        
        
        oncFrame.setContentPane(oncContentPane); 
        oncFrame.setVisible(true);
    }
    
    /** Returns an ImageIcon, or null if the path was invalid. */
   	ImageIcon createImageIcon(String path, String description)
   	{
   		java.net.URL imgURL = getClass().getResource(path);
   		if (imgURL != null) { return new ImageIcon(imgURL, description); } 
   		else { System.err.println("Couldn't find file: " + path); return null; }
   	}

    private void createandshowGUI()
	{
        //Create the family panel main screen
        oncFamilyPanel = new FamilyPanel(oncFrame);
        oncContentPane.add(oncFamilyPanel);
        
//      oncFrame.addWindowFocusListener(new WindowAdapter() {
//		    public void windowGainedFocus(WindowEvent e) {
//		    	System.out.println("Main frame gained focus");
//		        oncFamilyPanel.gainedFocus();
//		    }
//		});
        
      //Create the menu bar and set action listener for exit menu item
        oncMenuBar = ONCMenuBar.getInstance(); 
        oncMenuBar.exitMI.addActionListener(new ActionListener()
        {		
        	@Override
        	public void actionPerformed(ActionEvent e) { exit("LOGOUT"); }
        });
        oncFrame.setJMenuBar(oncMenuBar);
	}
      
    void exit(String command)
    {
    	if(serverIF != null && serverIF.isConnected())
    	{
			serverIF.sendRequest("LOGOUT");
    		serverIF.close();
    	}
    	
    	System.exit(0);
    }
    
    public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run() { new OurNeighborsChild(); }
	    });
	}	    
}