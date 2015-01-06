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
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import au.com.bytecode.opencsv.CSVWriter;

public class OurNeighborsChild implements DatabaseListener, ServerListener
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
	private static final String ONC_VERSION = "2.31";
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
//	private StatusPanel oncStatusPanel;
	private ONCMenuBar oncMenuBar;
	private PreferencesDialog prefsDlg;
//	private ChildCheckDialog dcDlg;
//	private FamilyCheckDialog dfDlg;
		
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
	
	//indexes that track families being displayed
	private int fn;		 							//Indexes for the family and search lists
	
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
        
        //Initialize the family data base indexes
        fn = 0;
//      rn = 0;
        
        //Initialize the chat manager
        ChatManager.getInstance();
         
        //create mainframe window for the application
//      FamilyChildSelectionListener fcsl = new FamilyChildSelectionListener();
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
        
        //set up the data check dialog and table row selection listener
//        dcDlg = new ChildCheckDialog(oncFrame);
//        dcDlg.addTableSelectionListener(fcsl);
        
        //set up the family check dialog and table row selection listener
//        dfDlg = new FamilyCheckDialog(oncFrame);
//        dfDlg.addTableSelectionListener(fcsl);
        
        //Get and authenticate user and privileges with Authentication dialog. Can't get past this
        //modal dialog unless a valid user id and password is authenticated by the server. 
        ONCAuthenticationDialog authDlg = null;
		authDlg = new ONCAuthenticationDialog(oncFrame, DEBUG_MODE);	
		authDlg.setVisible(true);
		
		//if we get here, the server has authenticated this client's userID and password
		ONCUser user = oncGVs.setUser(authDlg.getUser());
		setLoginStatusMssg();
		
		//Connected & logged in to server. Register for server event notifications
		if(serverIF != null && serverIF.isConnected())
		{
			serverIF.addServerListener(this);
			oncMenuBar.setEnabledServerConnected(true);
		}
		
        if(user.getPermission() == ONC_SUPERUSER)	//Superuser privileges
        {
        	oncGVs.setUserPermission(ONC_SUPERUSER);
        	prefsDlg.setEnabledDateToday(true);
        	prefsDlg.setEnabledRestrictedPrefrences(true);
        	oncMenuBar.setEnabledNewMenuItem(true);
        	oncMenuBar.setVisibleSpecialImports(true);
        	oncFamilyPanel.setEnabledSuperuserPrivileges(true);
        }
        else if(user.getPermission() == ONC_ADMIN)
        {
        	oncGVs.setUserPermission(ONC_ADMIN);
        	oncMenuBar.setEnabledNewMenuItem(true);
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
        	String response = null;
        	response = serverIF.sendRequest("GET<dbstatus>");
        	
        	if(response.startsWith("DB_STATUS"))
        	{
        		Type listOfDBs = new TypeToken<ArrayList<DBYear>>(){}.getType();
        		
        		Gson gson = new Gson();
        		List<DBYear> dbYears = gson.fromJson(response.substring(9), listOfDBs);
        		
        		MenuItemDBYearsListener menuItemDBYearListener = new MenuItemDBYearsListener();
        		
        		for(DBYear dbYear:dbYears)
        		{
        			String zYear = Integer.toString(dbYear.getYear());
        			JMenuItem mi = oncMenuBar.addDBYear(zYear, oncGVs.getImageIcon(dbYear.isLocked() ? 
        					DB_LOCKED_IMAGE_INDEX : DB_UNLOCKED_IMAGE_INDEX));
        			mi.addActionListener(menuItemDBYearListener);
        		}
        	}
        	
        	//get user data base
        	oncUserDB.importUserDatabase();		//imported here to support chat prior to loading a year
        }
        
        //remove splash panel after authentication
        oncContentPane.remove(oncSplashPanel);
        oncContentPane.revalidate();
        
        //add listener for odb/wfcm family imports
        if(oncFamDB != null)
        	oncFamDB.addDatabaseListener(this);
        
        if(DEBUG_MODE)
 		{
        	//Debug - get year 2013 automatically
        	importObjectsFromDB(2013);
		}
        
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
    
    void setLoginStatusMssg()
    {
    	if(oncGVs.getUser().getFirstname().isEmpty())
    		oncFamilyPanel.setMssg("Welcome to Our Neighbor's Child!", true);
    	else
    		oncFamilyPanel.setMssg(oncGVs.getUser().getFirstname() + ", welcome to Our Neighbor's Child!", true);
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
		String versionMsg = String.format("Our Neighbor's Child Client Version %s\n%s", ONC_VERSION, ONC_COPYRIGHT);
		JOptionPane.showMessageDialog(oncFrame, versionMsg, "About the ONC App", 
										JOptionPane.INFORMATION_MESSAGE,oncGVs.getImageIcon(0));
    }

    // General preferences dialog; fed to the OSXAdapter as the method to call when
    // "Preferences..." is selected from the application menu
    public void preferences()
    {
    	prefsDlg.setLocation((int)oncFrame.getLocation().getX() + 22, (int)oncFrame.getLocation().getY() + 22);
    	prefsDlg.updateData();
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
//    	JLabel lblONCicon = new JLabel(oncGVs.getONCFullScreenLogo());
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
        ONCMenuBar.clearMI.addActionListener(menuItemListener);       
        ONCMenuBar.exitMI.addActionListener(menuItemListener);       
        ONCMenuBar.findDupFamsMI.addActionListener(menuItemListener);
        ONCMenuBar.findDupChldrnMI.addActionListener(menuItemListener);
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
/*      
        //Create the Status Panel and add Action Listener for Family Array List Navigation
        oncStatusPanel = new StatusPanel(oncFrame, oncFamDB);       
        oncStatusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));            
        oncStatusPanel.btnNext.addActionListener(new NAVButtonListener());
        oncStatusPanel.btnPrevious.addActionListener(new NAVButtonListener());
        oncStatusPanel.searchTF.addActionListener(new NAVButtonListener());
        oncStatusPanel.searchTF.addKeyListener(new SearchTFKeyListener());
        oncStatusPanel.rbSrchNext.addActionListener(new NAVButtonListener());
        oncStatusPanel.rbSrchPrev.addActionListener(new NAVButtonListener());
        oncContentPane.add(oncStatusPanel); 
*/      
        //Create the family panel
        oncFamilyPanel = new FamilyPanel(oncFrame);
        oncContentPane.add(oncFamilyPanel);       
        
        //Set up the listener for the family, wish , receive gift and assign 
        //driver dialogs. When a table row selection event occurs in the tables in these dialogs,
        //the family and child (if applicable) that is selected in the tables is displayed in
        //the family panel
/*        
        oncFamilyPanel.sortFamiliesDlg.addTableSelectionListener(fcsl);
        oncFamilyPanel.assignDeliveryDlg.addTableSelectionListener(fcsl);
        oncFamilyPanel.sortAgentDlg.addTableSelectionListener(fcsl);
        oncFamilyPanel.sortWishesDlg.addTableSelectionListener(fcsl);
        oncFamilyPanel.recGiftsDlg.addTableSelectionListener(fcsl);
*/
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

    public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
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
    
    void OnExitMenuItemClicked()
    {
    	exit("LOGOUT");
    }
    
    /*************************************************************************************************
     * This method is called at the beginning of a ONC season to carry forward a prior years history.
     * The history data of interest is prior year children, prior year partners (organizations), prior
     * year referring agents and prior year wish catalog. The method opens a prior ONC objects data base
     * that is selected by the user. It then builds an organization data base using the opened objects 
     * organizational data base and setting each of the organizations status to "not contacted".
     * Additionally, it takes the wish information for each child and populates the previous year
     * child wish data. Previous year child are sorted by year of birth to optimize the search that 
     * determines if a child is present in two different data bases (prior year and current year, typically) 
     * If the child was already present in the previous year wish data base, it moves the
     * existing prior year data to the 2nd prior year. Only two years of history are kept for a child.
     * In addition, the method uses the prior year family regional distribution data to construct a 
     * database of ranges to be used in the current year to automatically assign ONC numbers to families 
     * imported from ODB or WFCM.
     * @return -1 if unsuccessful, 0 if successful
     *************************************************************************************************/
/*
    int importPriorYearObjectData() throws FileNotFoundException, IOException, ClassNotFoundException
    {	
//    	PriorYearChildDB dummyPYCDB = PriorYearChildDB.getInstance();
    	ChildDB pyChildDB = ChildDB.getInstance();
    	ChildWishDB pyChildWishDB = ChildWishDB.getInstance();
    	ONCAgents pyAgentDB = ONCAgents.getInstance();
    	DriverDB pyDriverDB = DriverDB.getInstance();
    	DeliveryDB pyDeliveryDB = DeliveryDB.getInstance();
    	Families pyFamilyDB = Families.getInstance();
    	
    	//Load prior year data base into app
    	String path =  "/Users/johnwoneill/Documents/ONC/Export Objects/2013 Season DB/2013 Final DB/";
    	oncFamDB.importFamilyDB(oncFrame, oncGVs.getImageIcon(0));
    	String[] oncnumRanges = oncGVs.importGlobalVariables(oncFrame, oncGVs.getImageIcon(0), path);
    	oncAgentDB.importAgentDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncChildDB.importChildDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncChildWishDB.importChildWishDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncOrgDB.importOrgDB(oncFrame, oncGVs.getImageIcon(0), path);
    //	oncPYCDB.importPriorYearChildDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncDDB.importDriverDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncDelDB.importDeliveryDB(oncFrame, oncGVs.getImageIcon(0), path);
    	oncWishCat.importWishCatalog(oncFrame, oncGVs.getImageIcon(0), path);
    	oncWishDetailDB.importWishDetailDB(oncFrame, oncGVs.getImageIcon(0), path);
    
	    //Reset the status of each organization.
	    oncOrgDB.resetAllOrgsStatus();
	    
	    //Enable the family panel to use prior year data bases
//	    if(!pycbyAgeAL.isEmpty()) { oncFamilyPanel.setPriorYearChildArrayList(pycbyAgeAL); }
	    
	    //Construct the array used to automatically assign ONC numbers based on last years
	    //regional family distribution.
	    oncFamDB.constructONCNumberRangesByRegion(pyFamilyDB.getFamilyDB());
	    
	    //Enable the user to manage the Wish Catalog
	    oncMenuBar.setEnabledWishCatalogAndOrgMenuItems(true);
	    	    
	    //Set new status mssg indicating input succeeded
	    oncStatusPanel.setStatusMssg("Prior year wishes and organizations sucessfully loaded");
	    
	    return 0;    
    }
*/   
    /******************************************************************************************
     * This method reassigns the ONC Numbers for the entire family data base. It is requested
     * by the user if/ when regions become full and require enlargement for all families in 
     * a region to have contiguous ONC numbers.
     * 
     * The method creates new ONC Number ranges for each region. It then goes thru the ONC
     * Family data base and assigns a new ONC number to each family.
     ***************************************************************************************/
/*
    void reassignONCNumbers()
    {
    	//Double check that the user really wants to reassign ONC numbers
		String confirmMssg = "Are you sure you want to reassign ONC numbers?";
	
		Object[] options= {"Cancel", "Reassign #'s"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							oncGVs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(oncFrame, "*** Confirm Child Database Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Reassign #'s"))
		{
			oncFamDB.resizeONCNumberRanges();
			
			//Now that the ONC number ranges are adequate, reassign an ONC Number to each in 8 steps
			//Step 1 - save the current family displayed before performing the reassign
			oncFamilyPanel.updateFamily();
			
			//Step 2 - get the ID of the currently displayed family We'll have to 
			//redisplay once the ONCFamily array list is reordered. 
			int famID = oncFamDB.getFamilyByIndex(fn).getID();
		
			//Step 3 - for valid ONC Numbers (all digits) or ONC Numbers that equal "OOR", clear
			//them so a new number can be assigned. Leave all other ONC Numbers alone.
			for(ONCFamily f: oncFamDB.getFamilyDB())
				if(isNumeric(f.getONCNum()) || f.getONCNum().equals("OOR"))
					f.setONCNum("");
	    
			//Step 4 - assign a new ONC number for each family with a cleared ONC number 
			//from step 3
			for(ONCFamily f: oncFamDB.getFamilyDB())
				if(f.getONCNum().isEmpty())
					f.setONCNum(oncFamDB.generateONCNumber(f.getRegion(), oncFrame));
	    
			//Step 5 - sort the family array list so next/previous is in numerical order
			oncFamDB.sortDB("ONC");
//			Collections.sort(oncFamDB.getFamilyDB(), new ONCNumComparator());
	  
			//Step 6 - now that the array has been restructured, find the currently displayed family
			//and redisplay
//			ONCDBSearch oncDBSrch = new ONCDBSearch();
//			fn = oncDBSrch.searchForFamilyIndexByID(oncFamDB, famID);
			fn = oncFamDB.searchForFamilyIndexByID(famID);
			oncFamilyPanel.displayFamily(oncFamDB.getFamilyByIndex(fn), null); 	
	
			//Step 7 - mark that family data has changed and rebuild all gui's that are
			//showing family data
			oncFamilyPanel.notifyONCNumbersReassigned();
			
			//Step 8 - Confirm for the user that the reassign operation was sucessful
			JOptionPane.showMessageDialog(oncFrame, 
					"ONC Numbers sucessfully reassigned", 
					"Reassign Successful", JOptionPane.INFORMATION_MESSAGE, oncGVs.getImageIcon(0));
		}
    }
*/    
    /******************************************************************************************
     * This method automatically assigns an ONC number to a family that doesn't have one. Using
     * the region (must not be ?) and the ranges of permissible ONC numbers by region, this
     * method calls the generateONCNumber function, then sort the family array list, saves and
     * displays the updated family
     ******************************************************************************************/
/*  void autoassignONCNum()
    {
    	//Save the family id to relocate the family in the array list after the sort
    	int famID = oncFamDB.getFamilyByIndex(fn).getID(); 
    	
    	//Generate and save the new onc number. First update the family panel text field,
    	//then call the method to compare displayed data to family data and update changes. After
    	//these three steps, the new family onc number is displayed and stored
    	String response = oncFamDB.update_AutoONCNum(this, oncFamDB.getFamilyByIndex(fn));
    	
    	if(response.startsWith("UPDATED_FAMILY"))
    	{
    		
    	}
    	
//   	String newONCnum = oncFamDB.generateONCNumber(oncFamDB.getFamilyByIndex(fn).getRegion(), oncFrame);
//   	oncFamilyPanel.displayNewONCnum(newONCnum); 	
//		oncFamilyPanel.checkAndUpdateFamilyData(oncFamDB.getFamilyByIndex(fn));

//    	//Sort the family array list so next/previous is in numerical order
//    	oncFamDB.sortDB("ONC");
    	
//    	//Since the sort likely has changed where the family is in the array list, find the 
//    	//family index again, save the new ONC number and display the update.
//    	fn = oncFamDB.searchForFamilyIndexByID(famID);
//		oncFamilyPanel.displayFamily(oncFamDB.getFamilyByIndex(fn), null);
//		if(oncGVs.isUserAdmin())
//			oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(oncFamDB.getFamilyByIndex(fn).getID()) > 0);
		
//		oncStatusPanel.setStoplightEntity(oncFamDB.getFamilyByIndex(fn));
//		oncFamilyPanel.notifyFamilyUpdateOccurred();
		
		//Update the served family and child counts		
//		oncStatusPanel.updateDBStatus(oncFamDB.getServedFamilyAndChildCount());
    }
*/    
    /********************************************************************************************
     * This method is called to start a new ONC season. First, a check is performed to determine
     * if family data, organization data or prior year child data is loaded. If so, the user is
     * queried if they would like to continue, as that data will be overwritten. Then a dialog
     * box is shown that notifies the user of two steps that will be performed. First, the
     * preferences dialog appears asking the user for global parameters for the new season. Second
     * an open file dialog appears asking for the file that contains the prior year family/org/
     * child information. The second step occurs by this method calling the importprioryearobject
     * method.
     *******************************************************************************************/
    void newONCSeason()
    {
    	//If data is already loaded make sure user wants to proceed
//    	if(oncFamDB.size() > 0 || oncOrgDB.getNumberOfOrganizations() > 0  || oncPYCDB.getNumberOfPriorYearChildren() > 0)
    	if(oncFamDB.size() > 0 || oncOrgDB.size() > 0)
    	{
    		if (JOptionPane.showConfirmDialog(oncFrame, "**** NEW ONC SEASON WARNING ****\n" + 
    				"Data is already loaded and will be lost\n " +
    				"Are you sure you want to proceed?", "WARNING - DATA WILL BE LOST",
    		        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, oncGVs.getImageIcon(0)) == JOptionPane.NO_OPTION) 
    		{
    		    return;
    		}
    	}
    	
    	String mssg = oncGVs.getUser().getFirstname() + ", you have chosen to start a new ONC season.\n"+
    					"You will be guided through the following three steps:\n\n" + 
    					"1. Setting parameters for the new season.\n" + 
    					"2. Review and update the wish catalog for the new season\n\n" +
    					"Click Ok to proceed or Cancel to abort";
    	
    	if(JOptionPane.showConfirmDialog(oncFrame, mssg, "Start New ONC Season? ",
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, oncGVs.getImageIcon(0)) == JOptionPane.OK_OPTION) 
		{
    		//To start a new season, first have the user set preferences
			prefsDlg.setLocation((int)oncFrame.getLocation().getX() + 22, 
									(int)oncFrame.getLocation().getY() + 22);
	    	prefsDlg.updateData();
	        prefsDlg.setVisible(true);
	        
	        //Request new season from server
	        
	        //Then we have to get last years data base file
//	        try
//	        {
//				if(importPriorYearObjectData() == 0);
//				{
//					if(oncGVs.isUserAdmin()) { oncMenuBar.setEnabledImportMenuItems(true); }
//					oncMenuBar.setEnabledSaveAsMenuItem(true);
//				}
//			}
//	    	catch (FileNotFoundException e1) { e1.printStackTrace();} 
//			catch (IOException e1) { e1.printStackTrace();} 
//			catch (ClassNotFoundException e1) { e1.printStackTrace();}
	        
	        //Then we have to reset prior year wish counts, update the wish selection lists, 
	        //and let the user review last years wish catalog for changes. 
//	        oncWishCat.clearCatalogWishCounts();
	        oncFamilyPanel.updateWishLists();
	        oncFamilyPanel.showWishCatalogDialog();
	        oncMenuBar.setEnabledWishCatalogAndOrgMenuItems(true);
		}
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
				oncMenuBar.SetEnabledRestrictedMenuItems(true);
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
					sb.append(ou.getFirstname() + " " + ou.getLastname() + "<br>");
			
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
/*    
    private class NAVButtonListener implements ActionListener
    {
    	public void actionPerformed(ActionEvent e)
    	{
    		if(e.getSource() == oncStatusPanel.btnNext || e.getSource() == oncStatusPanel.btnPrevious)
    		{
    			//Save changes, if any to both family and child info
    			//Only admin user can make family changes
    			if(oncGVs.isUserAdmin())	
    			{
    				oncFamilyPanel.update();
//    				oncStatusPanel.updateDBStatus(oncFamDB.getServedFamilyAndChildCount());
    			}
			
    			if(e.getSource() == oncStatusPanel.btnNext)
    			{						
    				if(++fn == oncFamDB.size())
    					fn=0;
    			}
    			else if(e.getSource() == oncStatusPanel.btnPrevious)
    			{
    				if(--fn < 0)
    					fn = oncFamDB.size()-1;
    			}
			
    			//display new family info, including delivery status and directions if visible
    			oncFamilyPanel.display(oncFamDB.getObjectAtIndex(fn), null);
    			
    			if(oncGVs.isUserAdmin())
    				oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(oncFamDB.getObjectAtIndex(fn).getID()) > 0);
    			
    			oncStatusPanel.setStoplightEntity(oncFamDB.getObjectAtIndex(fn));
				
    		}   		
    		else if(e.getSource() == oncStatusPanel.searchTF && !oncStatusPanel.searchTF.getText().isEmpty())
    		{
    			String s = oncStatusPanel.searchTF.getText();
    			oncFamDB.searchDB(s, srchResAL);
    			
    			if(srchResAL.size() > 0)
    			{
    				//Save changes, if any to both family and child info
        			if(oncGVs.isUserAdmin())	//Only admin user can make family changes
        			{
        				oncFamilyPanel.update();
 //       				oncStatusPanel.updateDBStatus(oncFamDB.getServedFamilyAndChildCount());
        			}
        			
        			//Set the search result array index to zero and set the family
        			//array index to the first ONCID in the search result array
        			fn = oncFamDB.searchForFamilyIndexByID(srchResAL.get((rn=0)));
        			
        			if(srchResAL.size() > 1)
        			{
        				oncStatusPanel.rbSrchNext.setVisible(true);
        				oncStatusPanel.rbSrchPrev.setVisible(true);
        				oncStatusPanel.setStatusMssg(String.format("%d %s's found", srchResAL.size(), s));
        			}
        			else
        			{
        				oncStatusPanel.rbSrchNext.setVisible(false);
        				oncStatusPanel.rbSrchPrev.setVisible(false);
        				setGenericStatusMssg();
        			}
        			
        			oncFamilyPanel.display(oncFamDB.getObjectAtIndex(fn), null);
        			if(oncGVs.isUserAdmin())
        				oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(oncFamDB.getObjectAtIndex(fn).getID()) > 0);
        			
        			oncStatusPanel.setStoplightEntity(oncFamDB.getObjectAtIndex(fn));
    			}    			
    			else
    				oncStatusPanel.searchTF.setText( s + " not found");
    		}
    		else if(e.getSource() == oncStatusPanel.rbSrchNext)
    		{	
    			//Save changes, if any to both family and child info
    			if(oncGVs.isUserAdmin())	//Only admin user can make family changes
    			{
    				oncFamilyPanel.update();
//    				oncStatusPanel.updateDBStatus(oncFamDB.getServedFamilyAndChildCount());
    			}
    			
    			//Calculate next value for result array index
    			if(++rn == srchResAL.size())
					rn=0;
    			  			
    			fn = oncFamDB.searchForFamilyIndexByID(srchResAL.get(rn));
    			
    			oncFamilyPanel.display(oncFamDB.getObjectAtIndex((fn)), null);
    			if(oncGVs.isUserAdmin())
    				oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(oncFamDB.getObjectAtIndex(fn).getID()) > 0);
    			
    			oncStatusPanel.setStoplightEntity(oncFamDB.getObjectAtIndex(fn));
    		}
    		else if(e.getSource() == oncStatusPanel.rbSrchPrev)
    		{
    			//Save changes, if any to both family and child info
    			if(oncGVs.isUserAdmin())	//Only admin user can make family changes
    			{
    				oncFamilyPanel.update();
//    				oncStatusPanel.updateDBStatus(oncFamDB.getServedFamilyAndChildCount());
    			}
    			
    			//Calculate next value for result array index
    			if(--rn < 0)
					rn = srchResAL.size()-1;
    			  			
    	//		fn = srchEng.searchForFamilyIndexByID(oncFamDB, srchResAL.get(rn));
    			fn = oncFamDB.searchForFamilyIndexByID(srchResAL.get(rn));
    			
    			oncFamilyPanel.display(oncFamDB.getObjectAtIndex((fn)), null);
    			if(oncGVs.isUserAdmin())
    				oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(oncFamDB.getObjectAtIndex(fn).getID()) > 0);
    			
    			oncStatusPanel.setStoplightEntity(oncFamDB.getObjectAtIndex(fn));
    		}
    	}   	
    }
    
    private class SearchTFKeyListener implements KeyListener
    {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
			if(oncStatusPanel.searchTF.getText().isEmpty())
			{
				srchResAL.clear();
				rn=0;
				oncStatusPanel.rbSrchNext.setVisible(false);
				oncStatusPanel.rbSrchPrev.setVisible(false);
				setGenericStatusMssg();
			}	
		}
    }
*/
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
    		if(e.getSource() == ONCMenuBar.newMI) 
    		{
    			//TEMPORARY HIJACK OF newONCSeason
//    			newONCSeason();
//    			
//    			//send request to server and print response
//  			if(serverIF != null && serverIF.isConnected())
//  			{
//    				String response = "";
//					try {
//						response = serverIF.sendRequest("POST<add_newseason>");
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//   				System.out.println("Our Neighbors Child New Season: " + response);
//    			}
    		}
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
    			oncFamilyPanel.updateWishLists();
    			oncFamilyPanel.updateWishAssignees();
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
    				if(angDlg.updateFamilyDeliveryStatus())
    				{
    					oncFamilyPanel.display(oncFamDB.getObjectAtIndex(fn), oncFamilyPanel.getDisplayedChild());	//Refresh Family Panel, keep cn same
    					oncFamilyPanel.notifyFamilyUpdateOccurred();
    				}
    			}
    			else
    				angDlg.clearCallItemData(); //User chose not to process call items
    		}
//    		else if(e.getSource() == ONCMenuBar.exportMI){ exportFamilyReportToCSV(); }
    		else if(e.getSource() == ONCMenuBar.exportMI){ exportObjectDBToCSV(); }
    		else if(e.getSource() == ONCMenuBar.clearMI) {OnClearMenuItemClicked();} 			       	
    		else if(e.getSource() == ONCMenuBar.exitMI)	{OnExitMenuItemClicked();}
    		else if(e.getSource() == ONCMenuBar.findDupFamsMI) {oncFamilyPanel.onCheckForDuplicateFamilies();}
    		else if(e.getSource() == ONCMenuBar.findDupChldrnMI) {oncFamilyPanel.onCheckForDuplicateChildren();}
    		else if(e.getSource() == ONCMenuBar.assignDelMI) {oncFamilyPanel.showAssignDelivererDialog();}
    		else if(e.getSource() == ONCMenuBar.manageDelMI) {oncFamilyPanel.showDriverDialog();}
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
    	    	prefsDlg.updateData();
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
    			ServerLogDialog sld = new ServerLogDialog(serverIF.getServerLog());
    			sld.setLocationRelativeTo(oncFrame);
    			sld.setVisible(true);
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
    	int year;
    	ONCProgressBar pb;
    	boolean bServerDataLoaded;
    	
    	ONCServerDBImporter(int year, ONCProgressBar pb)
    	{
    		this.year = year;
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
	    	serverIF.sendRequest("POST<setyear>" + Integer.toString(year));
			
			//import from ONC Server
			oncRegions.getRegionsFromServer();
			this.setProgress(progress += increment);
			oncGVs.importGlobalVariableDatabase();
			this.setProgress(progress += increment);
//			oncUserDB.importUserDatabase();
//			this.setProgress(progress += increment);
			oncFamDB.importDB();
			this.setProgress(progress += increment);
			oncChildDB.importChildDatabase();
			this.setProgress(progress += increment);
			oncChildWishDB.importChildWishDatabase();
			this.setProgress(progress += increment);
			oncAgentDB.importAgentDatabase();
			this.setProgress(progress += increment);
			oncOrgDB.importDB();
			this.setProgress(progress += increment);
			oncDDB.importDriverDatabase();
			this.setProgress(progress += increment);
			oncDelDB.importDeliveryDatabase();
			this.setProgress(progress += increment);
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
    
    void serverDataLoadComplete(boolean bServerDataLoaded, int year)
    {
    	if(bServerDataLoaded)
    	{
    		//Now that we have season data loaded
        	//let the user know that data has been loaded
    		
    		oncFrame.setTitle(APPNAME + " - " + Integer.toString(year) + " Season Data");
			if(oncGVs.isUserAdmin()) 
				oncMenuBar.setEnabledImportMenuItems(true);
			
			String mssg;
			if(oncGVs.getUser().getFirstname().equals(""))
				mssg = Integer.toString(year) + " season data has been loaded";
			else
				mssg = oncGVs.getUser().getFirstname() + ", " + Integer.toString(year) + " season data has been loaded";
//    		oncStatusPanel.setStatusMssg(mssg);
    		oncFamilyPanel.setMssg(mssg, true);
    		
    		oncMenuBar.setEnabledYear(false);
    	
    		//Set wish and wish assignee combo box lists in child panel and wish sort dialog
    		oncFamilyPanel.updateWishLists();
    		oncFamilyPanel.updateWishAssignees();
    		
    		//set dates in partner dialog combo box borders
    		oncFamilyPanel.updateComboBoxBorders();
 
    		oncMenuBar.setEnabledWishCatalogAndOrgMenuItems(true);
    		oncFamilyPanel.initializeCatalogWishCounts();
    	
			//check to see if family data is present and enable controls
			checkFamilyDataLoaded();
		
			//Families may not have been imported from ODB yet, however, agents exist from prior year
			if(!oncAgentDB.getAgentsAL().isEmpty())	//Families may be empty
				oncMenuBar.setEnabledAgentMenuItem(true);
    	}

    	//tell the server if to pass on server data base changes to local data bases
    	if(serverIF != null)
    		serverIF.setDatabaseLoaded(true);	
    }
    
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {new OurNeighborsChild();}
        });
    }
    //End of Class

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		//if this is the first family loaded locally, show it on the display
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY")  && oncFamDB.size() == 1)
			checkFamilyDataLoaded();
	}

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("GLOBAL_MESSAGE"))
		{
			ONCPopupMessage popup = new ONCPopupMessage( oncGVs.getImageIcon(0));
			Point loc = oncFrame.getLocationOnScreen();
			popup.setLocation(loc.x+450, loc.y+70);
			popup.show("Message from ONC Server", ue.getJson());
		}	
	}
/*
	private class FamilyChildSelectionListener implements TableSelectionListener
    {
		@Override
		public void tableRowSelected(TableSelectionEvent tse)
		{
			if(tse.getType().equals("FAMILY_SELECTED") || tse.getType().equals("WISH_SELECTED"))
			{
				ONCFamily fam = (ONCFamily) tse.getObject1();
				ONCChild child = (ONCChild) tse.getObject2();
			
				int rtn;
				if((rtn=oncFamDB.searchForFamilyIndexByID(fam.getID())) >= 0)
				{
					fn = rtn;
					oncFamilyPanel.display(fam, child);
				
					if(oncGVs.isUserAdmin())
						oncMenuBar.setEnabledDeleteChildMenuItem(oncChildDB.getNumberOfChildrenInFamily(fam.getID()) > 0);
				
					oncStatusPanel.setStoplightEntity(fam);
				}
			}
		}
    }
*/    
    
}