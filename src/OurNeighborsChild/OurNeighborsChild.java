package OurNeighborsChild;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class OurNeighborsChild implements DatabaseListener
{
	/**
	 * Executable Main Class for ONC application
	 */
	//Static Final Variables
	private static final boolean DEBUG_MODE = false;
	private static final int SERVER_CONNECT_RETRY_LIMIT = 3;
	private static final int ONC_IMAGE_ICON_INDEX = 0;
	private static final int ONC_SUPERUSER = 2;
	private static final int ONC_ADMIN = 1;
	private static final int ONC_USER = 0;
	private static final int ONC_SAVE_FILE = 1;	
	private static final String ONC_VERSION = "2.39";
	private static final String ONC_COPYRIGHT = "\u00A92015 John W. O'Neill";	
	private static final String APPNAME = "Our Neighbor's Child";
	private static final int DB_UNLOCKED_IMAGE_INDEX = 17;
	private static final int DB_LOCKED_IMAGE_INDEX = 18;
	private static final String ONC_SERVER_IP_ADDRESS_FILE = "serveripaddress.txt";
	
	//GUI Objects
	private JFrame oncFrame;
	private JPanel oncContentPane, oncSplashPanel;	
	private GlobalVariables oncGVs;
	private FamilyPanel oncFamilyPanel;
	private ONCMenuBar oncMenuBar;
	private PreferencesDialog prefsDlg;
	private LogDialog logDlg;

	//Local Data Base Structures
	private UserDB oncUserDB;
	private Families oncFamDB;						//Holds ONC Family Database
	private ChildDB oncChildDB;						//Holds ONC Child database
	private ChildWishDB oncChildWishDB; 			//Holds ONC Child Wish database
	private ONCAgents oncAgentDB;					//Holds ONC Agents
	private ONCOrgs oncOrgDB;						//Holds ONC Partner Organizations
	private ONCWishCatalog oncWishCat;				//Holds ONC Wish Catalog
	private WishDetailDB oncWishDetailDB;			//Holds ONC Wish Detail Data Base
	private DriverDB oncDDB;						//Holds the ONC Driver Data Base
	private DeliveryDB oncDelDB;					//Holds the ONC Delivery Data Base
	private ONCRegions oncRegions;
	private DBStatusDB oncDB;						//Holds the years loaded on the server
	
	//Server Connection
	private ServerIF serverIF;	
//	private static String defaultServerAddress = "72.209.233.207";	//Cox based server
//	private static String defaultServerAddress = "localhost";
	private static String defaultServerAddress = "96.127.35.251";	//IDT-Amazon cloud based server
	private static final int PORT = 8901;

	//Check if we are on Mac OS X.  This is crucial to loading and using the OSXAdapter class.
    private static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
    	
    public OurNeighborsChild()
    {	
    	//If running under MAC OSX, use the system menu bar and set the application title appropriately and
    	//set up our application to respond to the Mac OS X application menu
        if (MAC_OS_X) 
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
        logDlg = new LogDialog();	//create the static log dialog
        
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
        		System.out.println("SocketTimeoutException");
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
        	JOptionPane.showMessageDialog(oncFrame, "Server connection not established, please contact " +
        			"the ONC IT director", "ONC Server Connecton Error", JOptionPane.ERROR_MESSAGE);
        	System.exit(0);
        }
        else
        {
        	//write the server address successfully used
        	writeServerIPAddressToFile(serverIPAddress);
        }
        
        //Create global variables, set the main frame and the version number
        oncGVs = GlobalVariables.getInstance();
        oncGVs.setFrame(oncFrame);
        oncGVs.setVersion(ONC_VERSION);
        
        //Initialize data structures
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
        oncFamDB = Families.getInstance();
        oncDB = DBStatusDB.getInstance();
        
        //Initialize the chat manager
        ChatManager.getInstance();
         
        //create mainframe window for the application
        createandshowGUI();
        
        // set up the preferences dialog
        prefsDlg = new PreferencesDialog(oncFrame);
        prefsDlg.oncFontSizeCB.addActionListener(new ActionListener() //Notify family panel of font changes
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				oncGVs.setFontIndex(prefsDlg.oncFontSizeCB.getSelectedIndex());
				oncFamilyPanel.setTextPaneFontSize();				
			}      	
        });

        //Get and authenticate user and privileges with Authentication dialog. Can't get past this
        //modal dialog unless a valid user id and password is authenticated by the server. 
        ONCAuthenticationDialog authDlg = null;
		authDlg = new ONCAuthenticationDialog(oncFrame, DEBUG_MODE);	
		authDlg.setVisible(true);
		
		//if we get here, the server has authenticated this client's userID and password
		ONCUser user = oncGVs.setUser(authDlg.getUser());
		if(oncGVs.getUser().getFirstname().isEmpty())
    		oncFamilyPanel.setMssg("Welcome to Our Neighbor's Child!", true);
    	else
    		oncFamilyPanel.setMssg(oncGVs.getUser().getFirstname() + ", welcome to " +
    								"Our Neighbor's Child!", true);
		
		//Connected & logged in to server
		if(serverIF != null && serverIF.isConnected())
			oncMenuBar.setEnabledServerConnected(true);
		
        if(user.getPermission() == ONC_SUPERUSER)	//Superuser privileges
        {
        	oncGVs.setUserPermission(ONC_SUPERUSER);
        	prefsDlg.setEnabledDateToday(true);
        	prefsDlg.setEnabledRestrictedPrefrences(true);
        	oncMenuBar.setVisibleAdminFunctions(true);
        	oncMenuBar.setVisibleSpecialImports(true);
        	oncFamilyPanel.setEnabledSuperuserPrivileges(true);
        }
        else if(user.getPermission() == ONC_ADMIN)
        {
        	oncGVs.setUserPermission(ONC_ADMIN);
        	oncMenuBar.setVisibleAdminFunctions(true);
        	prefsDlg.setEnabledRestrictedPrefrences(true);
        }
        else
        	oncGVs.setUserPermission(ONC_USER);
                     	
        oncFamilyPanel.setFamilyPanelDisplayPermission(oncGVs.isUserAdmin());	//Restrict personal data for general user
        
        //get database years from server to set the data menu item for user to select and get user db so 
        //a chat can start
        if(serverIF != null)
        {
        	//get the list of data bases on the server
        	List<DBYear> dbYears = oncDB.getDBStatus();
        	if(dbYears != null)
        		processDBYears(dbYears);	
        	
        	//get user data base
        	oncUserDB.importUserDatabase();		//imported here to support chat prior to loading a year
        }
        
        //remove splash panel after authentication
        oncContentPane.remove(oncSplashPanel);
        oncContentPane.revalidate();
        
        //add listener for odb/wfcm family imports and new season adds
        if(oncFamDB != null)
        	oncFamDB.addDatabaseListener(this);
        if(oncDB != null)
        	oncDB.addDatabaseListener(this);
        
        if(DEBUG_MODE)
 		{
        	//Debug - get year 2013 automatically
        	importObjectsFromDB(2013);
		}
        
        //everything is initialized, start polling server for changes
        if(serverIF != null && serverIF.isConnected())
        	serverIF.setEnabledServerPolling(true);   	
    }
    
    void processDBYears(List<DBYear> dbYears)
    {
    	//clear the current list
    	oncMenuBar.clearDataBaseYears();
    	
    	//create the listener for each year in the year list in the menu
    	MenuItemDBYearsListener menuItemDBYearListener = new MenuItemDBYearsListener();
		
		for(DBYear dbYear:dbYears)
			addDBYear(dbYear, menuItemDBYearListener);
		
		//determine if we can allow the user to add a new season. Enable adding a new
		//season if the current date is in the year to be added, the year hasn't already
		//been added, the user has administrative privileges and a data base has not been loaded
		Calendar today = Calendar.getInstance();
		today.setTime(oncGVs.getTodaysDate());
		int currYear = today.get(Calendar.YEAR);
		
		if(currYear != dbYears.get(dbYears.size()-1).getYear() && oncGVs.isUserAdmin())
			oncMenuBar.setEnabledNewMenuItem(true);	
    }
    
    /******************************************************************************************
     * The  addDBYear method is separate as it is called when the application instantiates as well
     * as when the user requests addition of a new ONC season by adding a year to the list
     * @param dbYear
     * @param menuItemDBYearListener
     ****************************************************************************************/
    void addDBYear(DBYear dbYear, MenuItemDBYearsListener menuItemDBYearListener)
    {	
    	String zYear = Integer.toString(dbYear.getYear());
		JMenuItem mi = oncMenuBar.addDBYear(zYear, oncGVs.getImageIcon(dbYear.isLocked() ? 
				DB_LOCKED_IMAGE_INDEX : DB_UNLOCKED_IMAGE_INDEX));
		mi.addActionListener(menuItemDBYearListener);
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
    	String serverIPAddress = defaultServerAddress;
    	
    	//Construct FileReader
    	FileReader reader = null;
		try
		{
			reader = new FileReader(System.getProperty("user.dir") + "/" + ONC_SERVER_IP_ADDRESS_FILE);
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			serverIPAddress = defaultServerAddress;
			
	    	String mssg = "<html>Could not find file<br>" + ONC_SERVER_IP_ADDRESS_FILE +"</html>";		
	    	JOptionPane.showMessageDialog(oncFrame, mssg, "ONC Client File Error", JOptionPane.ERROR_MESSAGE,
	    									createImageIcon("onclogosmall.gif", "ONC Logo"));
			return serverIPAddress;
		}
		
    	// Construct BufferedReader from FileReader
    	BufferedReader br = new BufferedReader(reader);
     
    	String line = null;
    	try
    	{
			if((line = br.readLine()) != null)
				serverIPAddress = line;
			else
			{
				serverIPAddress = defaultServerAddress;
				
		    	String mssg = "<html>Could not read IP address in<br>" + ONC_SERVER_IP_ADDRESS_FILE +"</html>";		
		    	JOptionPane.showMessageDialog(oncFrame, mssg, "ONC Client File Error", JOptionPane.ERROR_MESSAGE,
		    									createImageIcon("onclogosmall.gif", "ONC Logo"));
			}
		} 
    	catch (IOException e)
    	{
			serverIPAddress = defaultServerAddress;
			
			String mssg = "<html>Could not read file<br>" + ONC_SERVER_IP_ADDRESS_FILE +"</html>";		
	    	JOptionPane.showMessageDialog(oncFrame, mssg, "ONC Client File Error", JOptionPane.ERROR_MESSAGE,
	    									createImageIcon("onclogosmall.gif", "ONC Logo"));
			
			return serverIPAddress;
			
		}
    	finally
    	{
    		try {
    			br.close();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	
    	return serverIPAddress;
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
    	//User has chosen to view the About ONC dialog
		String versionMsg = String.format("Our Neighbor's Child Client Version %s\n%s", 
											ONC_VERSION, ONC_COPYRIGHT);
		JOptionPane.showMessageDialog(oncFrame, versionMsg, "About the ONC App", 
										JOptionPane.INFORMATION_MESSAGE,oncGVs.getImageIcon(0));
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences()
    {
    	prefsDlg.setLocation((int)oncFrame.getLocation().getX() + 22, (int)oncFrame.getLocation().getY() + 22);
    	prefsDlg.display();
        prefsDlg.setVisible(true);   	
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
        oncFrame.setMinimumSize(new Dimension(832, 660));
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
        
        MenuItemListener menuItemListener = new MenuItemListener();
        
        ONCMenuBar.newMI.addActionListener(menuItemListener);
        ONCMenuBar.importONCMI.addActionListener(menuItemListener);
        ONCMenuBar.importWishCatMI.addActionListener(menuItemListener);
        ONCMenuBar.importPYMI.addActionListener(menuItemListener);
        ONCMenuBar.importPYORGMI.addActionListener(menuItemListener);
        ONCMenuBar.importODBMI.addActionListener(menuItemListener);
        ONCMenuBar.importWFCMMI.addActionListener(menuItemListener);
        ONCMenuBar.importCallResultMI.addActionListener(menuItemListener);
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
//      ONCMenuBar.sortByONCMI.addActionListener(menuItemListener);
        ONCMenuBar.delstatusMI.addActionListener(menuItemListener);             
        ONCMenuBar.viewDBMI.addActionListener(menuItemListener);
        ONCMenuBar.catMI.addActionListener(menuItemListener);
        ONCMenuBar.sortWishesMI.addActionListener(menuItemListener);
        ONCMenuBar.recGiftsMI.addActionListener(menuItemListener);
        ONCMenuBar.labelViewerMI.addActionListener(menuItemListener);
        ONCMenuBar.orgMI.addActionListener(menuItemListener);
        ONCMenuBar.sortOrgsMI.addActionListener(menuItemListener);
        ONCMenuBar.sortFamiliesMI.addActionListener(menuItemListener);
        ONCMenuBar.agentMI.addActionListener(menuItemListener);
        ONCMenuBar.aboutONCMI.addActionListener(menuItemListener);
        ONCMenuBar.oncPrefrencesMI.addActionListener(menuItemListener);
        ONCMenuBar.oncAddUserMI.addActionListener(menuItemListener);
        ONCMenuBar.newChildMI.addActionListener(menuItemListener);
        ONCMenuBar.delChildMI.addActionListener(menuItemListener);
        ONCMenuBar.onlineMI.addActionListener(menuItemListener);
        ONCMenuBar.chatMI.addActionListener(menuItemListener);
        ONCMenuBar.changePWMI.addActionListener(menuItemListener);
        ONCMenuBar.stopPollingMI.addActionListener(menuItemListener);
        ONCMenuBar.showServerLogMI.addActionListener(menuItemListener);
        ONCMenuBar.showServerClientIDMI.addActionListener(menuItemListener);
        ONCMenuBar.showCurrDirMI.addActionListener(menuItemListener);

        //Create the family panel
        oncFamilyPanel = new FamilyPanel(oncFrame);
        oncContentPane.add(oncFamilyPanel);        
	}
    
    String exportFamilyReportToCSV()
    {
    	String filename = null;
    	
    	ONCFileChooser fc = new ONCFileChooser(oncFrame);
    	File oncwritefile= fc.getFile("Select .csv file to save to",
										new FileNameExtensionFilter("CSV Files", "csv"), ONC_SAVE_FILE);
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		ONCFamilyReportRowBuilder rb = new ONCFamilyReportRowBuilder();
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(rb.getFamilyReportHeader());
	    	    
	    	    for(ONCFamily fam:oncFamDB.getList())
	    	    	writer.writeNext(rb.getFamilyReportCSVRowData(fam));	//Get family data
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(oncFrame, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }

    void OnImportMenuItemClicked(String source)
	{
    	int olddbsize = oncFamDB.size();
    	
    	oncFamDB.importCSVFile(source, oncFrame);
    	oncFamilyPanel.setMssg(Integer.toString(oncFamDB.size() - olddbsize) + " families were imported", false);
    	
    	checkFamilyDataLoaded();
	}

    void OnClearMenuItemClicked()
    {
/*    	
    	//Clear the GUI's which disables buttons that are dependent on db being loaded
    	//These methods clear the buttons as necessary
    	oncStatusPanel.ClearData();
    	oncFamilyPanel.ClearFamilyData();
    	
    	//Clear any existing persistent data
    	if(oncFamDB.size() > 0)	//Clear child and delivery data before clearing family data
    		for(ONCFamily fam:oncFamDB.getList())
    		{
//   			fam.getChildArrayList().clear();
//    			fam.getDeliveryStatusAL().clear();	//NO LONGER NEEDED WHEN SEPARATE DELIVERY DB CREATED
    		}
    	
    	oncFamDB.clear();	
    	oncOrgDB.clear();
//    	oncPYCDB.clear();
    	oncWishCat.clearCatalogData();
    	
    	//Close all dialogs
    	oncFamilyPanel.closeAllDialogs();
    	
    	//Disable the menu bar for items that can't function without data
    	oncMenuBar.SetEnabledMenuItems(false);
    	oncMenuBar.SetEnabledRestrictedMenuItems(false);
    	
    	oncFrame.setTitle(APPNAME);
    	
    	return;
*/    	  	
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
   
    /********************************************************************************************
     * This method is called to add a new ONC season to the server.The user is asked to confirm
     * they really want to add a new year. If confirmed, the request is sent to the server. If
     * the server successfully adds the year, it responds with a new DBYear list that includes
     * the new year at the end. The list is processed to update the Menu Bar Database available
     * years list
     *******************************************************************************************/
    void addONCSeason()
    {    
    	//determine what year we'll be adding to the ONC Server 
		Calendar today = Calendar.getInstance();
		today.setTime(oncGVs.getTodaysDate());
		
		//ask the user to confirm the add of the new year
    	String confirmMssg = String.format("<html>%s, please confirm you want to add<br>the %d year to the ONC Server</html>", 
    			oncGVs.getUser().getFirstname(), today.get(Calendar.YEAR));
    	
    	Object[] options= {"Cancel", "Add " + today.get(Calendar.YEAR) };
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							oncGVs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(oncFrame, "*** Confirm Add New Year ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		
		//if confirmed, send the add request to the server and await the response
		if(selectedValue != null && selectedValue.toString().startsWith("Add"))
		{
			//set up user notification of result
	    	String mssg ="";
	    	String title = "Add Year Failed";
	    	int mssgType = JOptionPane.ERROR_MESSAGE;
	    	
			//send add new year request to the ONC Server via the  DBStatus data base
	    	//and process response
			String response = "Error message missing";
			if(oncDB != null)
			{
				response = oncDB.add(this);	//request add of new ONC season
				
				//if the response indicates the server successfully add the year, it returns a 
				//json of a list of new DBYear objects with the new year added to the end. 
				//Process the list
				if(response != null && response.startsWith("ADDED_DBYEAR"))
				{
					Gson gson = new Gson();
					Type listtype = new TypeToken<ArrayList<DBYear>>(){}.getType();
					ArrayList<DBYear> dbYearList =  gson.fromJson(response.substring(12), listtype);
					
					int newYear = processAddedONCSeason(dbYearList);
					
					mssg = String.format("%d sucessfully added to ONC Server", newYear);
					title = "Add Year Successful";
					mssgType = JOptionPane.INFORMATION_MESSAGE;
				}
				else if(response != null && response.startsWith("ADD_DBYEAR_FAILED")) //alert the user the add failed
					mssg = response.substring(17);					
				else //general server error - didn't respond
					mssg = "Error: ONC Server failed to respond";	
			}
			else //server is not connected
				mssg = "Error: Client is not connected to the ONC Server";
			
			JOptionPane.showMessageDialog(oncFrame, mssg, title, mssgType, oncGVs.getImageIcon(0));
		}
    }
    
    int processAddedONCSeason(ArrayList<DBYear> dbYearList)
    {
    	MenuItemDBYearsListener menuItemDBYearListener = new MenuItemDBYearsListener();
    	
    	//clear the current list
    	oncMenuBar.clearDataBaseYears();
		
		for(DBYear dbYear:dbYearList)
			addDBYear(dbYear, menuItemDBYearListener);
		
		//now that the year is added, disable adding another year
		oncMenuBar.setEnabledNewMenuItem(false);
		
		//return last year in the list
		return dbYearList.get(dbYearList.size()-1).getYear();
    }
    
    void importObjectsFromDB(int year)
    {
    	//create the progress bar frame
    	ONCProgressBar pb = new ONCProgressBar(oncGVs.getImageIcon(0), 100);
    	Point loc = oncFrame.getLocationOnScreen();
		pb.setLocation(loc.x+450, loc.y+70);
    	
		//create the swing worker background task to get the data from the server
		ONCServerDBImporter dataImporter = new ONCServerDBImporter(year, pb);
	    dataImporter.addPropertyChangeListener(pb);
	    
	    //show the progress bar.
	    pb.show("Loading " + Integer.toString(year) + " Data", null);
	    
	    //execute the background swing worker task
	    dataImporter.execute();
    }
    
    /*******************************************************************************************
     * This method is called when a server ADDED_FAMILY message is processed. If there were
     * no families in the local database and the ADDED_FAMILY is the first, it will display
     * that family and enable the navigation controls as well as the Menu Bar permissions
     */
    void checkFamilyDataLoaded()
    {
    	if(oncFamDB.size() > 0)
		{
			oncMenuBar.SetEnabledMenuItems(true);
			oncFamilyPanel.onFamilyDataLoaded();
		
			if(oncGVs.isUserAdmin())  			
				oncMenuBar.setEnabledRestrictedMenuItems(true);
		}
    }
    
    void exportObjectDBToCSV()
    {
    	String path = oncFamDB.exportDBToCSV(oncFrame);

    	oncGVs.exportGlobalVariablesToCSV(oncFrame, path + "GlobalVariables.csv", 
    										oncFamDB.getExportONCNumberRegionRangesRow());
    	oncAgentDB.exportAgentDBToCSV(oncFrame, path + "/AgentDB.csv");
    	oncChildDB.exportChildDBToCSV(oncFrame, path + "/ChildDB.csv");
    	oncChildWishDB.exportChildWishDBToCSV(oncFrame, path + "/ChildWishDB.csv");
    	oncOrgDB.exportDBToCSV(path + "/OrgDB.csv");
    //	oncPYCDB.exportPriorYearChildDBToCSV(oncFrame, path + "/PriorYearChildDB.csv");
    	oncDDB.exportDriverDBToCSV(oncFrame, path + "/DriverDB.csv");
    	oncDelDB.exportDeliveryDBToCSV(oncFrame, path + "/DeliveryDB.csv");
    	oncWishCat.exportWishCatalogToCSV(oncFrame, path + "/WishCatalog.csv");
    	oncWishDetailDB.exportWishDetailDBToCSV(oncFrame, path + "/WishDetailDB.csv");
    }
    
    void onAddNewAppUser() throws IOException
    {
    	if(serverIF != null && serverIF.isConnected())
    	{
    		String[] fieldNames = {"First Name", "Last Name", "User ID", "Password", "Permission"};
    		AddUserDialog auDlg = new AddUserDialog(oncFrame, fieldNames);
    		auDlg.setLocationRelativeTo(oncFamilyPanel);
    		if(auDlg.showDialog())
    		{
    			String response = oncUserDB.add(this, auDlg.getAddUserReq());
			
    			//Changes added locally are not echo'd from server, they must be processed locally
    			//Saves network bandwidth
    			if(!response.startsWith("ADDED_USER"))
    			{
    				JOptionPane.showMessageDialog(oncFrame, "Error: ONC Server denied add user request", 
						"Can't add user", JOptionPane.ERROR_MESSAGE);
    			}
    		}
    	}
    	else
			JOptionPane.showMessageDialog(oncFrame, "Error: Can't add user if not connected to ONC Server", 
						"Can't add user", JOptionPane.ERROR_MESSAGE);
    }
    
    void onWhoIsOnline()
    {
    	List<ONCUser> onlineUserList = null;
    	//get online users from user data base
    	if(oncUserDB != null)
    	{
    		String response = "";
    		response = serverIF.sendRequest("GET<online_users>");
    		
    		if(response.startsWith("ONLINE_USERS"))
    		{
    			Gson gson = new Gson();
    			Type listtype = new TypeToken<ArrayList<ONCUser>>(){}.getType();
    			
    			onlineUserList = gson.fromJson(response.substring(12), listtype);
    		}
    	}
    	
		if(onlineUserList != null)
		{
			StringBuffer sb = new StringBuffer("<html>");
			
			for(ONCUser ou: onlineUserList)
				if(ou != null)	//can be null if server log in failure -- known server issue Oct 16, 2014
				{
					String year = ou.getClientYear() == -1 ? "None" : Integer.toString(ou.getClientYear());
					sb.append(ou.getFirstname() + " " + ou.getLastname() + ": " + year + "<br>");
				}
			
			sb.append("</html>");
					
			//Show user list
	        JOptionPane.showMessageDialog(oncFrame, sb.toString(),
			"ONC Elves Online", JOptionPane.INFORMATION_MESSAGE,oncGVs.getImageIcon(0));
		}
		else
		{
			JOptionPane.showMessageDialog(oncFrame, "Couldn't retrive online users from ONC Server",
        			"ONC Server Error", JOptionPane.ERROR_MESSAGE,oncGVs.getImageIcon(0));
		}
    }
    
    void onChat()
    {
    	ChatDialog chatDlg = new ChatDialog(oncFrame, true, -1);	//true=user initiated chat, -1=no target yet
    	chatDlg.setLocationRelativeTo(oncFrame);
    	chatDlg.setVisible(true);
    }
    
    void onDBStatusClicked()
    {
    	DatabaseStatusDialog statusDlg = new DatabaseStatusDialog(oncFrame);
    	statusDlg.setLocationRelativeTo(oncFrame);
    	statusDlg.setVisible(true);
    }
    
    void onChangePassword()
    {
    	String[] fieldNames = {"Current Password", "New Password", "Re-enter New Password"};
		ChangePasswordDialog cpDlg = new ChangePasswordDialog(oncFrame, fieldNames);
		cpDlg.setLocationRelativeTo(oncFamilyPanel);
		String result = "<html>New and re-entered passwords didn't match.<br>Please try again.</html>";
		
		if(cpDlg.showDialog())
		{
			if(cpDlg.doNewPasswordsMatch())
			{
				ONCUser currUser = oncGVs.getUser();
				String[] pwInfo = cpDlg.getPWInfo();
				
				ChangePasswordRequest cpwReq = new ChangePasswordRequest(currUser.getID(),
						currUser.getFirstname(), currUser.getLastname(),
						pwInfo[0], pwInfo[1]);
				
				result = oncUserDB.changePassword(this, cpwReq);
			}
			
			JOptionPane.showMessageDialog(oncFrame, result,"Change Password Result",
        			 JOptionPane.ERROR_MESSAGE,oncGVs.getImageIcon(0));
		}
    } 

    private class MenuItemDBYearsListener implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		for(int i=0; i< oncMenuBar.dbYears.size(); i++)
    		{
    			JMenuItem mi = oncMenuBar.dbYears.get(i);
    			if(e.getSource() == mi)
    				importObjectsFromDB(Integer.parseInt(e.getActionCommand())); 
    		}
    	}
    }
    private class MenuItemListener implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		if(e.getSource() == ONCMenuBar.newMI) { addONCSeason(); }
//   		else if(e.getSource() == ONCMenuBar.importONCMI) {OnImportMenuItemClicked("ONC");}
    		else if(e.getSource() == ONCMenuBar.importONCMI) { }
    		else if(e.getSource() == ONCMenuBar.importWishCatMI)
    		{
    			oncWishCat.importWishCatalog(oncFrame, oncGVs.getImageIcon(ONC_IMAGE_ICON_INDEX), null);
    		}
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
    		{
    			oncOrgDB.importOrgDB(oncFrame, oncGVs.getImageIcon(0), null);
//    			oncFamilyPanel.updateComboBoxModels();
    		}
    		else if(e.getSource() == ONCMenuBar.importODBMI) {OnImportMenuItemClicked("ODB");}
    		else if(e.getSource() == ONCMenuBar.importWFCMMI) {OnImportMenuItemClicked("WFCM");}
    		else if(e.getSource() == ONCMenuBar.importRAFMI) { oncFamilyPanel.onImportRAFMenuItemClicked(); }
    		else if(e.getSource() == ONCMenuBar.importCallResultMI)
    		{
    			AngelAutoCallDialog angDlg = new AngelAutoCallDialog();
    			int nCallItems = angDlg.readAngelCallResults(oncFrame, oncGVs, oncFamDB);
    			//Confirm with the user that the deletion is really intended
    			String confirmMssg =String.format("%d call items imported, do you want to process them " +
    					"and create a call log report?", nCallItems);
    		
    			Object[] options= {"Cancel", "Process Call Items"};
    			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
    								oncGVs.getImageIcon(0), options, "Cancel");
    			JDialog confirmDlg = confirmOP.createDialog(oncFrame, "*** Confirm Process Call Items ***");
    			confirmDlg.setVisible(true);
    		
    			Object selectedValue = confirmOP.getValue();
    			if(selectedValue != null && selectedValue.toString().equals("Process Call Items"))
    			{
    				angDlg.writeAngelCallResults(oncFrame);
    				angDlg.updateFamilyDeliveryStatus();
    			}
    			else
    				angDlg.clearCallItemData(); //User chose not to process call items
    		}
//    		else if(e.getSource() == ONCMenuBar.exportMI){ exportFamilyReportToCSV(); }
    		else if(e.getSource() == ONCMenuBar.exportMI){ exportObjectDBToCSV(); }
    		else if(e.getSource() == ONCMenuBar.dbStatusMI) {onDBStatusClicked();}
    		else if(e.getSource() == ONCMenuBar.clearMI) {OnClearMenuItemClicked();} 			       	
    		else if(e.getSource() == ONCMenuBar.exitMI)	{exit("LOGOUT");}
    		else if(e.getSource() == ONCMenuBar.findDupFamsMI) {oncFamilyPanel.onCheckForDuplicateFamilies();}
    		else if(e.getSource() == ONCMenuBar.findDupChldrnMI) {oncFamilyPanel.onCheckForDuplicateChildren();}
    		else if(e.getSource() == ONCMenuBar.assignDelMI) {oncFamilyPanel.showAssignDelivererDialog();}
    		else if(e.getSource() == ONCMenuBar.editDelMI) {oncFamilyPanel.showDriverDialog();}
    		else if(e.getSource() == ONCMenuBar.manageDelMI) {oncFamilyPanel.showSortDriverDialog();}
    		else if(e.getSource() == ONCMenuBar.importDrvrMI)
    		{
    			String mssg = oncDDB.importDrivers(oncFrame, oncGVs.getTodaysDate(),
    									oncGVs.getUserLNFI(), oncGVs.getImageIcon(ONC_IMAGE_ICON_INDEX));
    			
//    			oncFamilyPanel.refreshDriverDisplays();	//Update dialog based on imported info
    			
    			//Information message that the drivr import completed successfully
    		    JOptionPane.showMessageDialog(oncFrame, mssg,
    					"Import Result", JOptionPane.INFORMATION_MESSAGE, oncGVs.getImageIcon(0));
    		}
    		else if(e.getSource() == ONCMenuBar.mapsMI) {oncFamilyPanel.showDrivingDirections();}
    		else if(e.getSource() == ONCMenuBar.distMI) {oncFamilyPanel.showClientMap(oncFamDB.getList());}
    		else if(e.getSource() == ONCMenuBar.changeONCMI) { oncFamilyPanel.showChangeONCNumberDialog(); }
    		else if(e.getSource() == ONCMenuBar.delstatusMI) {oncFamilyPanel.showDeliveryStatus();}
    		else if(e.getSource() == ONCMenuBar.viewDBMI) {oncFamilyPanel.showEntireDatabase(oncFamDB);}
    		else if(e.getSource() == ONCMenuBar.sortWishesMI) {oncFamilyPanel.showSortWishesDialog(oncFamDB.getList());}
    		else if(e.getSource() == ONCMenuBar.recGiftsMI) {oncFamilyPanel.showReceiveGiftsDialog(oncFamDB.getList());}
    		else if(e.getSource() == ONCMenuBar.labelViewerMI) {oncFamilyPanel.showWishLabelViewerDialog();}
    		else if(e.getSource() == ONCMenuBar.catMI) {oncFamilyPanel.showWishCatalogDialog(); }
    		else if(e.getSource() == ONCMenuBar.orgMI) {oncFamilyPanel.showOrgDialog();}
    		else if(e.getSource() == ONCMenuBar.sortOrgsMI) {oncFamilyPanel.showSortOrgsDialog();}
    		else if(e.getSource() == ONCMenuBar.sortFamiliesMI) {oncFamilyPanel.showSortFamiliesDialog(oncFamDB.getList());}
    		else if(e.getSource() == ONCMenuBar.agentMI) {oncFamilyPanel.showSortAgentDialog(oncAgentDB, oncFamDB.getList()); }
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
    		else if(e.getSource() == ONCMenuBar.oncAddUserMI)
    		{
    			try {
					onAddNewAppUser();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}   			
    		}
    		else if(e.getSource() == ONCMenuBar.delChildMI) { oncFamilyPanel.deleteChild(); }
    		else if(e.getSource() == ONCMenuBar.newChildMI) { oncFamilyPanel.onAddNewChildClicked(); }
    		else if(e.getSource() == ONCMenuBar.onlineMI) { onWhoIsOnline(); }
    		else if(e.getSource() == ONCMenuBar.chatMI) { onChat(); }
    		else if(e.getSource() == ONCMenuBar.changePWMI) { onChangePassword(); }
    		else if(e.getSource() == ONCMenuBar.stopPollingMI && serverIF != null) { serverIF.setEnabledServerPolling(false); }
    		else if(e.getSource() == ONCMenuBar.showServerLogMI && serverIF != null)
    		{
    			
    			logDlg.setLocationRelativeTo(oncFrame);
    			logDlg.setVisible(true);
    		}
    		else if(e.getSource() == ONCMenuBar.showServerClientIDMI)
    		{
    			ONCPopupMessage clientIDPU = new ONCPopupMessage( oncGVs.getImageIcon(0));
    			clientIDPU.setLocationRelativeTo(oncFamilyPanel);
    			String mssg = String.format("Your ONC Server Client ID is: %d", oncGVs.getUser().getClientID());
    			clientIDPU.show("ONC Server Client ID", mssg);
    		}    		
    		else if(e.getSource() == ONCMenuBar.showCurrDirMI)
    		{
    			ONCPopupMessage clientIDPU = new ONCPopupMessage( oncGVs.getImageIcon(0));
    			clientIDPU.setLocationRelativeTo(oncFamilyPanel);
    			String mssg = String.format("Current folder is: %s", System.getProperty("user.dir"));
    			clientIDPU.show("ONC Client Current Folder", mssg);
    		}
    	}   	
    }
    

    /***************************************************************************************************
     * This class communicates with the ONC Server to fetch season data from the server data base
     * and store in the local data base. This executes as a background task. A progress bar,
     * provided at the time the class is instantiated, shows the user the progress in fetching data. 
     **************************************************************************************************/
    public class ONCServerDBImporter extends SwingWorker<Void, Void>
    {
    	private static final int NUM_OF_DBs = 11;
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
//	    	ServerIF serverIF = ServerIF.getInstance();
	    	serverIF.sendRequest("POST<setyear>" + year);
			
			//import from ONC Server
	    	pb.updateHeaderText("<html>Loading Regions</html>");
			oncRegions.getRegionsFromServer();
			this.setProgress(progress += increment);
			pb.updateHeaderText("Loading Season Data");
			oncGVs.importGlobalVariableDatabase();
			this.setProgress(progress += increment);
//			oncUserDB.importUserDatabase();
//			this.setProgress(progress += increment);
			pb.updateHeaderText("Loading Families");
			oncFamDB.importDB();
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
			pb.updateHeaderText("Loading Wish Catalog");
			oncWishCat.importWishCatalogFromServer();
			this.setProgress(progress += increment);
			oncWishDetailDB.importWishDetailDatabase();
			this.setProgress(progress += increment);
			
			bServerDataLoaded = true;
			
			return null;
		}
		
		 /*
	     * Executed in event dispatching thread
	     */
	    @Override
	    public void done()
	    {
	    	serverDataLoadComplete(bServerDataLoaded, year);
	        Toolkit.getDefaultToolkit().beep();
	        pb.dispose();
	    }
    }
    
    void serverDataLoadComplete(boolean bServerDataLoaded, String year)
    {
    	if(bServerDataLoaded)
    	{
    		//Now that we have season data loaded
        	//let the user know that data has been loaded
    		oncFrame.setTitle(APPNAME + " - " + year + " Season Data");
			if(oncGVs.isUserAdmin()) 
				oncMenuBar.setEnabledImportMenuItems(true);
			
			String mssg;
			if(oncGVs.getUser().getFirstname().equals(""))
				mssg = year + " season data has been loaded";
			else
				mssg = oncGVs.getUser().getFirstname() + ", " + year + " season data has been loaded";
    		oncFamilyPanel.setMssg(mssg, true);
    		
    		oncMenuBar.setEnabledYear(false);
    		oncMenuBar.setEnabledNewMenuItem(false);
    		oncMenuBar.setEnabledWishCatalogAndOrgMenuItems(true);
		
			//Families may not have been imported from ODB yet, however, agents exist from prior
			//year and users can import drivers or add them if they wish
			oncMenuBar.setEnabledDataLoadedMenuItems(true);
			
			oncFamilyPanel.initializeCatalogWishCounts();
	    	
			//check to see if family data is present and enable controls
			checkFamilyDataLoaded();
    	}

    	//tell the server if to pass on server data base changes to local data bases
    	if(serverIF != null)
    		serverIF.setDatabaseLoaded(true);	
    }

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		//if this is the first family loaded locally, show it on the display
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY")  && oncFamDB.size() == 1)
			checkFamilyDataLoaded();
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DBYEAR"))
		{
			ArrayList<DBYear> newDBYearList = (ArrayList<DBYear>) dbe.getObject();
			int addedYear = processAddedONCSeason(newDBYearList);
			
			String mssg = String.format("%d database added, now available", addedYear);
			ONCPopupMessage popup = new ONCPopupMessage( oncGVs.getImageIcon(0));
			Point loc = oncFrame.getLocationOnScreen();
			popup.setLocation(loc.x+450, loc.y+70);
			popup.show("Message from ONC Server", mssg);	
		}
	}
	
	 public static void main(String args[])
	 {
		 SwingUtilities.invokeLater(new Runnable() {
	            public void run() { new OurNeighborsChild(); }
	     });
	 }	    
}//End of Class