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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
	private static final String ONC_VERSION = "3.27";
	private static final String APPNAME = "Our Neighbor's Child";
	private static final String ONC_SERVER_IP_ADDRESS_FILE = "serveripaddress.txt";
	
	//GUI Objects
	private JFrame oncFrame;
	private JPanel oncContentPane, oncSplashPanel;	
	private GlobalVariables oncGVs;
	private FamilyPanel oncFamilyPanel;
	private ONCMenuBar oncMenuBar;
	

	//Local Data Base Structures
	private DatabaseManager dbManager;			//Holds the years loaded on the server
	
//	private UserDB oncUserDB;
//	private Families oncFamDB;				//Holds ONC Family Database
//	private ChildDB oncChildDB;				//Holds ONC Child database
//	private ChildWishDB oncChildWishDB; 	//Holds ONC Child Wish database
//	private ONCAgents oncAgentDB;			//Holds ONC Agents
//	private ONCOrgs oncOrgDB;				//Holds ONC Partner Organizations
//	private ONCWishCatalog oncWishCat;		//Holds ONC Wish Catalog
//	private WishDetailDB oncWishDetailDB;	//Holds ONC Wish Detail Data Base
//	private DriverDB oncDDB;				//Holds the ONC Driver Data Base
//	private DeliveryDB oncDelDB;			//Holds the ONC Delivery Data Base
//	private ONCRegions oncRegions;
//	private AdultDB oncAdultDB;				//Holds ONC Adult database
//	private MealDB oncMealDB;				//Holds ONC Meal database
	
	private DialogManager dlgManager;		//Managed all dialogs in client
	
	//Server Connection
	private ServerIF serverIF;	
//	private static String defaultServerAddress = "72.209.233.207";	//Cox based server
//	private static String defaultServerAddress = "localhost";
	private static String defaultServerAddress = "96.127.35.251";	//IDT-Amazon cloud based server
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
        
        //get the last server address saved in the ipaddressfile.txt located in the same folder as the
        //client application .jar or .app
        String serverIPAddress = readServerIPAddressFromFile();
        
        //connect to server
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
        else
        	writeServerIPAddressToFile(serverIPAddress);	//write the server address successfully used
        
        //server is connected, proceed with initialization
        
        
        //Create global variables, set the main frame and the version number
        oncGVs = GlobalVariables.getInstance();
        oncGVs.setFrame(oncFrame);
        oncGVs.setVersion(ONC_VERSION);
        
        dbManager = DatabaseManager.getInstance();
/*        
        //initialize data structures
        oncRegions = ONCRegions.getInstance();
        oncUserDB = UserDB.getInstance();
        oncAgentDB = ONCAgents.getInstance();
        oncOrgDB = ONCOrgs.getInstance();
        oncWishDetailDB = WishDetailDB.getInstance();
        oncWishCat = ONCWishCatalog.getInstance();
        oncDDB = DriverDB.getInstance();
        oncDelDB = DeliveryDB.getInstance();
        oncChildDB = ChildDB.getInstance();
        oncChildWishDB = ChildWishDB.getInstance();
        oncAdultDB = AdultDB.getInstance();
        oncMealDB = MealDB.getInstance();
        oncFamDB = Families.getInstance();
        oncDB = DatabaseManager.getInstance();
*/        
        //initialize the entity event manager
        EntityEventManager.getInstance();
        
        //Initialize the chat and dialog managers
        ChatManager.getInstance();
        dlgManager = DialogManager.getInstance();
         
        //create mainframe window for the application
        createandshowGUI();

        //Get and authenticate user and privileges with Authentication dialog. Can't get past this
        //modal dialog unless a valid user id and password is authenticated by the server. 
        ONCAuthenticationDialog authDlg = null;
		authDlg = new ONCAuthenticationDialog(oncFrame);	
		authDlg.setVisible(true);
		
		//if we get here, the server has authenticated this client's userID and password
		//must check to see if the password needs to be changed, if so, force the change
		ONCUser user = oncGVs.setUser(authDlg.getUser());
		if(user.changePasswordRqrd() && !dlgManager.onChangePassword())
			System.exit(0);
		
		if(oncGVs.getUser().getFirstname().isEmpty())
    		oncFamilyPanel.setMssg("Welcome to Our Neighbor's Child!", true);
    	else
    		oncFamilyPanel.setMssg(oncGVs.getUser().getFirstname() + ", welcome to " +
    								"Our Neighbor's Child!", true);
		
		//Connected & logged in to server
		if(serverIF != null && serverIF.isConnected())
			oncMenuBar.setEnabledServerConnected(true);
		
        if(user.getPermission() == UserPermission.Sys_Admin)	//Superuser privileges
        {
        	oncGVs.setUserPermission(UserPermission.Sys_Admin);
        	dlgManager.setEnabledAdminPrivileges(true);
        	oncMenuBar.setVisibleAdminFunctions(true);
        	oncMenuBar.setVisibleSpecialImports(true);
        	dlgManager.setEnabledSuperuserPrivileges(true);
        }
        else if(user.getPermission() == UserPermission.Admin)
        {
        	oncGVs.setUserPermission(UserPermission.Admin);
        	oncMenuBar.setVisibleAdminFunctions(true);
        	dlgManager.setEnabledAdminPrivileges(true);
        }
        else
        	oncGVs.setUserPermission(UserPermission.General);
     
        //get database years from server to set the data menu item for user to select and get user db so 
        //a chat can start
        if(serverIF != null)
        {
        	//get the list of data bases on the server
        	List<DBYear> dbYears = dbManager.getDBStatus();
        	if(dbYears != null)
        		oncMenuBar.processDBYears(dbYears);	
        	
        	//get user data base
        	UserDB.getInstance().importUserDatabase();
//        	oncUserDB.importUserDatabase();		//imported here to support chat prior to loading a year
        }
        
        //remove splash panel after authentication
        oncContentPane.remove(oncSplashPanel);
        oncContentPane.revalidate();
        
        //initialize web site status
        oncGVs.initializeWebsiteStatusFromServer();
       
        //everything is initialized, start polling server for changes
        if(serverIF != null && serverIF.isConnected())
        	serverIF.setEnabledServerPolling(true);   	
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
    
    void writeServerIPAddressToFile(String ipAddress)
    {
    	PrintWriter outputStream = null;
        FileWriter fileWriter = null;
       
		try
		{
			fileWriter = new FileWriter(System.getProperty("user.dir") + "/" + ONC_SERVER_IP_ADDRESS_FILE);
			outputStream = new PrintWriter(fileWriter);
			 
		    outputStream.println(ipAddress);
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
        oncFrame.setMinimumSize(new Dimension(832, 668));
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
        //Create the menu bar and set action listener for each menu item
        oncMenuBar = new ONCMenuBar();
        oncFrame.setJMenuBar(oncMenuBar);
        
        ONCMenuBar.exitMI.addActionListener(new ActionListener()
        {		
        	@Override
        	public void actionPerformed(ActionEvent e) { exit("LOGOUT"); }
        });
/*        
        MenuItemListener menuItemListener = new MenuItemListener();
        
        ONCMenuBar.newMI.addActionListener(menuItemListener);
        ONCMenuBar.importONCMI.addActionListener(menuItemListener);
        ONCMenuBar.importWishCatMI.addActionListener(menuItemListener);
        ONCMenuBar.importPYMI.addActionListener(menuItemListener);
        ONCMenuBar.importPYORGMI.addActionListener(menuItemListener);
        ONCMenuBar.importODBMI.addActionListener(menuItemListener);
        ONCMenuBar.importWFCMMI.addActionListener(menuItemListener);
        ONCMenuBar.manageCallResultMI.addActionListener(menuItemListener);
        ONCMenuBar.exportMI.addActionListener(menuItemListener);
        ONCMenuBar.dbStatusMI.addActionListener(menuItemListener);
        ONCMenuBar.clearMI.addActionListener(menuItemListener);       
        ONCMenuBar.exitMI.addActionListener(menuItemListener);       
        ONCMenuBar.findDupFamsMI.addActionListener(menuItemListener);
        ONCMenuBar.findDupChldrnMI.addActionListener(menuItemListener);
        ONCMenuBar.editDelMI.addActionListener(menuItemListener);
        ONCMenuBar.manageDelMI.addActionListener(menuItemListener);
        ONCMenuBar.importDrvrMI.addActionListener(menuItemListener);
        ONCMenuBar.importRAFMI.addActionListener(menuItemListener);
        ONCMenuBar.assignDelMI.addActionListener(menuItemListener);
        ONCMenuBar.mapsMI.addActionListener(menuItemListener);
        ONCMenuBar.distMI.addActionListener(menuItemListener);
        ONCMenuBar.changeONCMI.addActionListener(menuItemListener);
        ONCMenuBar.changeRefMI.addActionListener(menuItemListener);
        ONCMenuBar.changeBatchMI.addActionListener(menuItemListener);
//      ONCMenuBar.sortByONCMI.addActionListener(menuItemListener);
        ONCMenuBar.delstatusMI.addActionListener(menuItemListener);             
        ONCMenuBar.viewDBMI.addActionListener(menuItemListener);
        ONCMenuBar.catMI.addActionListener(menuItemListener);
        ONCMenuBar.sortWishesMI.addActionListener(menuItemListener);
        ONCMenuBar.recGiftsMI.addActionListener(menuItemListener);
        ONCMenuBar.sortMealsMI.addActionListener(menuItemListener);
        ONCMenuBar.orgMI.addActionListener(menuItemListener);
        ONCMenuBar.sortOrgsMI.addActionListener(menuItemListener);
        ONCMenuBar.sortFamiliesMI.addActionListener(menuItemListener);
        ONCMenuBar.agentMI.addActionListener(menuItemListener);
        ONCMenuBar.aboutONCMI.addActionListener(menuItemListener);
        ONCMenuBar.oncPrefrencesMI.addActionListener(menuItemListener);
        ONCMenuBar.userMI.addActionListener(menuItemListener);
        ONCMenuBar.newFamMI.addActionListener(menuItemListener);
        ONCMenuBar.newChildMI.addActionListener(menuItemListener);
        ONCMenuBar.markAdultMI.addActionListener(menuItemListener);
        ONCMenuBar.delChildMI.addActionListener(menuItemListener);
        ONCMenuBar.connectChildMI.addActionListener(menuItemListener);
        ONCMenuBar.onlineMI.addActionListener(menuItemListener);
        ONCMenuBar.chatMI.addActionListener(menuItemListener);
        ONCMenuBar.profileMI.addActionListener(menuItemListener);
        ONCMenuBar.changePWMI.addActionListener(menuItemListener);
        ONCMenuBar.stopPollingMI.addActionListener(menuItemListener);
        ONCMenuBar.showServerLogMI.addActionListener(menuItemListener);
        ONCMenuBar.showServerClientIDMI.addActionListener(menuItemListener);
        ONCMenuBar.showCurrDirMI.addActionListener(menuItemListener);
        ONCMenuBar.showWebsiteStatusMI.addActionListener(menuItemListener);
*/
        //Create the family panel
        oncFamilyPanel = new FamilyPanel(oncFrame);
        oncContentPane.add(oncFamilyPanel);        
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
    
    /*******************************************************************************************
     * This method is called when a server ADDED_FAMILY message is processed. If there were
     * no families in the local database and the ADDED_FAMILY is the first, it will display
     * that family and enable the navigation controls as well as the Menu Bar permissions
     */
/*    
    void checkFamilyDataLoaded()
    {
    	if(oncFamDB.size() > 0)
		{
			oncMenuBar.SetEnabledMenuItems(true);
			oncFamilyPanel.onFamilyDataLoaded();
			
			if(GlobalVariables.isUserAdmin()) 
				oncMenuBar.setEnabledRestrictedMenuItems(true);
		}
    }
*/    
/* 
    private class MenuItemListener implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		if(e.getSource() == ONCMenuBar.newMI) { addONCSeason(); }
//   		else if(e.getSource() == ONCMenuBar.importONCMI) {OnImportMenuItemClicked("ONC");}
    		else if(e.getSource() == ONCMenuBar.importONCMI) { }
    		else if(e.getSource() == ONCMenuBar.importWishCatMI)
    			oncWishCat.importWishCatalog(oncFrame, oncGVs.getImageIcon(ONC_IMAGE_ICON_INDEX), null);
    		else if(e.getSource() == ONCMenuBar.importPYMI)
    		{
    			//A prior year child database will be read and then split into different databases that 
    			//correspond to prior year child birth years. This allows higher performance when trying
    		    //to match a child from one season with a child from another season. 
    			//This ArrayList holds each array lists of PriorYearChild objects by year of birth
//    			ReadPriorYearCSVFile(oncPYCDB.getPriorYearChildAL());
//    		    ArrayList<ArrayList<ONCPriorYearChild>> pycbyAgeAL = buildPriorYearByAgeArrayList();
    			    			
    			//Enable the family panel to use prior year child data bases
//    		    if(!pycbyAgeAL.isEmpty()) { oncFamilyPanel.setPriorYearChildArrayList(pycbyAgeAL); }
    		}
    		else if(e.getSource() == ONCMenuBar.importPYORGMI)
    			oncOrgDB.importOrgDB(oncFrame, oncGVs.getImageIcon(0), null);
    		else if(e.getSource() == ONCMenuBar.importODBMI) {OnImportMenuItemClicked("ODB");}
    		else if(e.getSource() == ONCMenuBar.importWFCMMI) {OnImportMenuItemClicked("WFCM");}
    		else if(e.getSource() == ONCMenuBar.importRAFMI) { dlgManager.onImportRAFMenuItemClicked(); }
    		else if(e.getSource() == ONCMenuBar.manageCallResultMI) {dlgManager.showAngelCallDialog();}
    		else if(e.getSource() == ONCMenuBar.exportMI){ exportObjectDBToCSV(); }
    		else if(e.getSource() == ONCMenuBar.dbStatusMI) {dlgManager.onDBStatusClicked();}
    		else if(e.getSource() == ONCMenuBar.clearMI) {dlgManager.onClearMenuItemClicked();} 			       	
    		else if(e.getSource() == ONCMenuBar.exitMI)	{exit("LOGOUT");}
    		else if(e.getSource() == ONCMenuBar.findDupFamsMI)
    			dlgManager.showCheckDialog(ONCMenuBar.findDupFamsMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.findDupChldrnMI)
    			dlgManager.showCheckDialog(ONCMenuBar.findDupChldrnMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.assignDelMI)
    			dlgManager.showSortDialog(ONCMenuBar.assignDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.editDelMI)
    			dlgManager.showEntityDialog(ONCMenuBar.editDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.manageDelMI)
    			dlgManager.showSortDialog(ONCMenuBar.manageDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.importDrvrMI)
    		{
    			String mssg = oncDDB.importDrivers(oncFrame, oncGVs.getTodaysDate(),
    									GlobalVariables.getUserLNFI(), oncGVs.getImageIcon(ONC_IMAGE_ICON_INDEX));
    			
//    			oncFamilyPanel.refreshDriverDisplays();	//Update dialog based on imported info
    			
    			//Information message that the drivr import completed successfully
    		    JOptionPane.showMessageDialog(oncFrame, mssg,
    					"Import Result", JOptionPane.INFORMATION_MESSAGE, oncGVs.getImageIcon(0));
    		}
    		else if(e.getSource() == ONCMenuBar.mapsMI) {dlgManager.showDrivingDirections();}
    		else if(e.getSource() == ONCMenuBar.distMI) {dlgManager.showClientMap();}
    		else if(e.getSource() == ONCMenuBar.changeONCMI)
    			dlgManager.showFamilyInfoDialog(ONCMenuBar.changeONCMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.changeRefMI)
    			dlgManager.showFamilyInfoDialog(ONCMenuBar.changeRefMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.changeBatchMI)
    			dlgManager.showFamilyInfoDialog(ONCMenuBar.changeBatchMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.delstatusMI)
    			dlgManager.showHistoryDialog(ONCMenuBar.delstatusMI.getActionCommand());
    		else if(e.getSource() == ONCMenuBar.viewDBMI) {dlgManager.showEntireDatabase();}
    		else if(e.getSource() == ONCMenuBar.sortWishesMI)
    			dlgManager.showSortDialog(ONCMenuBar.sortWishesMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.sortMealsMI)
    			dlgManager.showSortDialog(ONCMenuBar.sortMealsMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.recGiftsMI)
    			dlgManager.showSortDialog(ONCMenuBar.recGiftsMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.catMI) {dlgManager.showWishCatalogDialog(); }
    		else if(e.getSource() == ONCMenuBar.orgMI)
    			dlgManager.showEntityDialog(ONCMenuBar.orgMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.sortOrgsMI)
    			dlgManager.showSortDialog(ONCMenuBar.sortOrgsMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.sortFamiliesMI)
    			dlgManager.showSortDialog(ONCMenuBar.sortFamiliesMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.agentMI)
    			dlgManager.showSortDialog(ONCMenuBar.agentMI.getActionCommand(), SORT_DIALOG_OFFSET);
    		else if(e.getSource() == ONCMenuBar.aboutONCMI)
    		{
    			//User has chosen to view the About ONC dialog
    			String versionMsg = String.format("Our Neighbor's Child Client Version %s\n%s", ONC_VERSION, ONC_COPYRIGHT);
        		JOptionPane.showMessageDialog(oncFrame, versionMsg, "About the ONC App", 
        										JOptionPane.INFORMATION_MESSAGE,oncGVs.getImageIcon(0));
    		}
    		else if(e.getSource() == ONCMenuBar.oncPrefrencesMI)
    		{
    			//User has chosen to view the preferences dialog
    			prefsDlg.setLocation((int)oncFrame.getLocation().getX() + 22, 
    									(int)oncFrame.getLocation().getY() + 22);
    	    	prefsDlg.display();
    	        prefsDlg.setVisible(true); 
    		}
    		else if(e.getSource() == ONCMenuBar.newFamMI)
    		{
    			AddFamilyDialog afDlg = new AddFamilyDialog(oncFrame);
    			afDlg.setVisible(true);
    		}
    		else if(e.getSource() == ONCMenuBar.delChildMI) { dlgManager.onDeleteChild(); }
    		else if(e.getSource() == ONCMenuBar.newChildMI) { dlgManager.onAddNewChildClicked(); }
    		else if(e.getSource() == ONCMenuBar.markAdultMI) { dlgManager.onMarkChildAsAdult(); }
    		else if(e.getSource() == ONCMenuBar.connectChildMI) { dlgManager.showConnectPYChildDialog(); }
    		else if(e.getSource() == ONCMenuBar.userMI) { dlgManager.showUserDialog(); }
    		else if(e.getSource() == ONCMenuBar.onlineMI) { dlgManager.onWhoIsOnline(); }
    		else if(e.getSource() == ONCMenuBar.chatMI) { dlgManager.onChat(); }
    		else if(e.getSource() == ONCMenuBar.profileMI) { dlgManager.onEditProfile(); }
    		else if(e.getSource() == ONCMenuBar.changePWMI) { dlgManager.onChangePassword(); }
    		else if(e.getSource() == ONCMenuBar.stopPollingMI && serverIF != null) { serverIF.setEnabledServerPolling(false); }
    		else if(e.getSource() == ONCMenuBar.showServerLogMI && serverIF != null)
    		{
    			logDlg.setLocationRelativeTo(oncFrame);
    			logDlg.setVisible(true);
    		}
    		else if(e.getSource() == ONCMenuBar.showServerClientIDMI)
    		{
    			ONCPopupMessage clientIDPU = new ONCPopupMessage( oncGVs.getImageIcon(0));
    			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
    			String mssg = String.format("Your ONC Server Client ID is: %d", oncGVs.getUser().getClientID());
    			clientIDPU.show("ONC Server Client ID", mssg);
    		}    		
    		else if(e.getSource() == ONCMenuBar.showCurrDirMI)
    		{
    			ONCPopupMessage clientIDPU = new ONCPopupMessage( oncGVs.getImageIcon(0));
    			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
    			String mssg = String.format("Current folder is: %s", System.getProperty("user.dir"));
    			clientIDPU.show("ONC Client Current Folder", mssg);
    		}
    		else if(e.getSource() == ONCMenuBar.showWebsiteStatusMI) { dlgManager.onWebsiteStatus(); }
    	}   	
    }
    
*/
    /***************************************************************************************************
     * This class communicates with the ONC Server to fetch season data from the server data base
     * and store in the local data base. This executes as a background task. A progress bar,
     * provided at the time the class is instantiated, shows the user the progress in fetching data. 
     **************************************************************************************************/
/*    
    public class ONCServerDBImporter extends SwingWorker<Void, Void>
    {
    	private static final int NUM_OF_DBs = 13;
    	String year;
    	ONCProgressBar pb;
    	boolean bServerDataLoaded;
    	
    	ONCServerDBImporter(int year, ONCProgressBar pb)
    	{
    		this.year = Integer.toString(year);;
    		this.pb = pb;
    		bServerDataLoaded = false;
    	}
    	
		@Override
		protected Void doInBackground() throws Exception
		{
			int progress = 0;
			int increment = 100/NUM_OF_DBs;
			this.setProgress(progress);
	    	
	    	//Set the year this client will be working with
	    	serverIF.sendRequest("POST<setyear>" + year);
			
			//import from ONC Server
	    	pb.updateHeaderText("<html>Loading Regions</html>");
			oncRegions.getRegionsFromServer();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Season Data");
			oncGVs.importGlobalVariableDatabase();
			this.setProgress(progress += increment);

			pb.updateHeaderText("Loading Families");
			oncFamDB.importDB();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Adults");
			oncAdultDB.importDB();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Meals");
			oncMealDB.importDB();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Children");
			oncChildDB.importChildDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Wishes");
			oncChildWishDB.importChildWishDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Agents");
			oncAgentDB.importAgentDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Partners");
			oncOrgDB.importDB();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Drivers");
			oncDDB.importDriverDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Deliveries");
			oncDelDB.importDeliveryDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Catalog");
			oncWishCat.importWishCatalogFromServer();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Detail");
			oncWishDetailDB.importWishDetailDatabase();
			this.setProgress(progress += increment);
			
			bServerDataLoaded = true;
			
			return null;
		}
		
		 // Executed in event dispatching thread
	    @Override
	    public void done()
	    {
	    	serverDataLoadComplete(bServerDataLoaded, year);
	        Toolkit.getDefaultToolkit().beep();
	        pb.dispose();
	    }
    }
*/    
	
	 public static void main(String args[])
	 {
		 SwingUtilities.invokeLater(new Runnable() {
	            public void run() { new OurNeighborsChild(); }
	     });
	 }	    
}//End of Class