package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar implements ActionListener, DatabaseListener
{
	/**
	 * This singleton class provides the blueprint for the menu bar in the ONC application
	 * Much of the user interface is provided thru this menu bar.
	 */
	private static final long serialVersionUID = 1L;
	private static final Point SORT_DIALOG_OFFSET = new Point (5, 20);
	private static final int DB_UNLOCKED_IMAGE_INDEX = 17;
	private static final int DB_LOCKED_IMAGE_INDEX = 18;
	
	private static MenuBar instance;
	
	private DatabaseManager oncDB;
	private JMenuItem newMI;
	private JMenuItem importBritepathsMI, importWFCMMI, manageSMSMI;
//	private JMenuItem importVolMI;
	private JMenuItem manageCallResultMI;
	private JMenuItem exportMI, dbStatusMI, clearMI;
	public JMenuItem exitMI;	//public since exit method is external to the menu bar
	private JMenuItem findDupFamsMI, findDupChldrnMI, crosscheckMI;
	private JMenuItem editVolMI, editActMI, editDistCentersMI, viewSignInLogMI, manageVolMI, manageActMI;
	private JMenuItem assignDelMI, assignDistCentersMI, manageDelMI, barcodeDeliveryMI, mapsMI, schoolDelMI, distMI;
	private JMenuItem newFamMI, changeONCMI, changeRefMI, changeBatchMI, newChildMI, editDNSCodesMI;
	private JMenuItem delChildMI, markAdultMI, connectChildMI, famHistMI;
	private JMenu submenuImport, submenuFamilyDataChecks, submenuExport, submenuChangeFamilyNumbers;
	private JMenu submenuDBYearList, submenuUsers, submenuReceiveGifts, submenuDeliverGifts;
	private JMenuItem viewDBMI, sortGiftsMI, sortClonedGiftsMI, sortFamiliesMI, sortOrgsMI, recGiftsMI, recGiftsBarcodeMI, sortMealsMI, batteryMI, manageBatteryMI;
	private JMenuItem agentMI, groupMI, manageGroupsMI, orgMI, catMI, barcodeGiftHistoryMI, inventoryMI;
	private JMenuItem aboutONCMI, oncPrefrencesMI, profileMI, editUsersMI, manageUsersMI, onlineMI, chatMI, changePWMI, stopPollingMI;
	private JMenuItem showServerLogMI, showServerClientIDMI, showCurrDirMI, showWebsiteStatusMI;
	private List<JMenuItem> dbYearsMIList;
	
	private DialogManager dlgManager;
	private DatabaseManager dbManager;
	
	private FamilyDB familyDB;
	private UserDB userDB;
	
	private MenuBar()
	{
		//get reference to onc data base
		oncDB = DatabaseManager.getInstance();
		if(oncDB != null)
			oncDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		JMenu menuDatabase, menuAgents, menuFamilies, menuGifts, menuMeals, menuPartners, 
				menuVolunteers, menuDelivery, menuSettings;	    
        
	    //Build the Database menu.
	    menuDatabase = new JMenu("Database");
	    this.add(menuDatabase);
	 
	    submenuDBYearList = new JMenu("Select Year");
	    submenuDBYearList.setEnabled(false);   
	    dbYearsMIList = new ArrayList<JMenuItem>();
	    menuDatabase.add(submenuDBYearList);
	    
	    newMI = new JMenuItem("Add Year");
	    newMI.setEnabled(false);
	    newMI.setVisible(false);
	    newMI.addActionListener(this);
	    menuDatabase.add(newMI);
	    
	    dbStatusMI = new JMenuItem("Lock/Unlock Year");
	    dbStatusMI.setVisible(false);
	    dbStatusMI.addActionListener(this);
	    menuDatabase.add(dbStatusMI);

	    menuDatabase.addSeparator();
	    
	    submenuImport = new JMenu("Import");
	    submenuImport.setEnabled(false);
 
	    importBritepathsMI = new JMenuItem("From Britepaths...");
	    importBritepathsMI.addActionListener(this);
	    submenuImport.add(importBritepathsMI);
	    
	    importWFCMMI = new JMenuItem("From WFCM...");
	    importWFCMMI.addActionListener(this);
	    submenuImport.add(importWFCMMI);

	    //Import Delivery Partners
//	    importVolMI = new JMenuItem("Volunteers");
//	    importVolMI.addActionListener(this);
//	    submenuImport.add(importVolMI);
    
	    menuDatabase.add(submenuImport);
	    
	    submenuExport = new JMenu("Export");
	    submenuExport.setEnabled(false);
	    	    
	    exportMI = new JMenuItem("Database to .csv files");
	    exportMI.addActionListener(this);
	    submenuExport.add(exportMI);
	    
	    menuDatabase.add(submenuExport);
	   
	    menuDatabase.addSeparator();
	    
	    manageSMSMI = new JMenuItem("Manage SMS");
	    manageSMSMI.setEnabled(false);
	    manageSMSMI.addActionListener(this);
	    menuDatabase.add(manageSMSMI);
	    
	    clearMI = new JMenuItem("Clear");
	    clearMI.setEnabled(false);
	    clearMI.addActionListener(this);
	    menuDatabase.add(clearMI);
	    
	    exitMI = new JMenuItem("Log Out");
	    exitMI.addActionListener(this);
	    menuDatabase.add(exitMI);
	    	    
	    //Build the Agents menu.
	    menuAgents = new JMenu("Agents");
	    
	    agentMI = new JMenuItem("Manage Agents");
	    agentMI.setActionCommand("Agents");
	    agentMI.setEnabled(false);	
	    agentMI.addActionListener(this);
	    menuAgents.add(agentMI);
	    
	    menuAgents.addSeparator();
	    
	    groupMI = new JMenuItem("Edit Groups");
	    groupMI.setActionCommand("Edit Groups");
	    groupMI.setEnabled(false);	
	    groupMI.addActionListener(this);
	    menuAgents.add(groupMI);
	    
	    manageGroupsMI = new JMenuItem("Manage Groups");
	    manageGroupsMI.setActionCommand("Manage Groups");
	    manageGroupsMI.setEnabled(false);
	    manageGroupsMI.addActionListener(this);
	    menuAgents.add(manageGroupsMI);
	    
	    this.add(menuAgents);

	    //Build Families Menu Structure
	    menuFamilies = new JMenu("Families");
	    this.add(menuFamilies);
	    
	    sortFamiliesMI = new JMenuItem("Manage Families");
	    sortFamiliesMI.setActionCommand("Families");
	    sortFamiliesMI.setEnabled(false);
	    sortFamiliesMI.addActionListener(this);
	    menuFamilies.add(sortFamiliesMI);
	    
	    submenuChangeFamilyNumbers = new JMenu("Change Numbers");
	    submenuChangeFamilyNumbers.setEnabled(false);   
	    menuFamilies.add(submenuChangeFamilyNumbers);
	       
	    changeONCMI = new JMenuItem("Change ONC #");
	    changeONCMI.setActionCommand("Change ONC #");
	    changeONCMI.addActionListener(this);
	    submenuChangeFamilyNumbers.add(changeONCMI);
	    
	    changeRefMI = new JMenuItem("Change Ref #");
	    changeRefMI.setActionCommand("Change Ref #");
	    changeRefMI.addActionListener(this);
	    submenuChangeFamilyNumbers.add(changeRefMI);
	    
	    changeBatchMI = new JMenuItem("Change Batch #");
	    changeBatchMI.setActionCommand("Change Batch #");
	    changeBatchMI.addActionListener(this);
	    submenuChangeFamilyNumbers.add(changeBatchMI);
	    
	    menuFamilies.addSeparator();
	    
	    editDNSCodesMI = new JMenuItem("Edit DNS Codes");
	    editDNSCodesMI.setActionCommand("Edit Codes");
	    editDNSCodesMI.addActionListener(this);
	    menuFamilies.add(editDNSCodesMI);
	    
	    //Manage Angel Call Results
	    manageCallResultMI = new JMenuItem("Manage Call Results");
	    manageCallResultMI.setEnabled(false);
	    manageCallResultMI.addActionListener(this);
	    menuFamilies.add(manageCallResultMI);
	    
	    menuFamilies.addSeparator();
	    
	    newFamMI = new JMenuItem("Add New Family");
	    newFamMI.setEnabled(false);
	    newFamMI.addActionListener(this);
	    menuFamilies.add(newFamMI);
	    
	    menuFamilies.addSeparator();
	    
	    newChildMI = new JMenuItem("Add New Child");
	    newChildMI.setEnabled(false);
	    newChildMI.addActionListener(this);
	    menuFamilies.add(newChildMI);
	    
	    markAdultMI = new JMenuItem("Mark Child as Adult");
	    markAdultMI.setEnabled(false);
	    markAdultMI.addActionListener(this);
	    menuFamilies.add(markAdultMI);
	    
	    delChildMI = new JMenuItem("Delete Child");
	    delChildMI.setEnabled(false);
	    delChildMI.addActionListener(this);
	    menuFamilies.add(delChildMI);
	    
	    connectChildMI = new JMenuItem("Connect PY Child");
	    connectChildMI.setEnabled(false);
	    connectChildMI.addActionListener(this);
	    menuFamilies.add(connectChildMI);
	    
	    menuFamilies.addSeparator();
	    
	    //Build Family Data Checks Sub menu Structure
	    submenuFamilyDataChecks = new JMenu("Data Checks");
	    
	    findDupFamsMI = new JMenuItem("Duplicate Family Check");
	    findDupFamsMI.setActionCommand("Duplicate Family Check");
	    findDupFamsMI.setEnabled(false);
	    findDupFamsMI.addActionListener(this);
	    submenuFamilyDataChecks.add(findDupFamsMI);
	    
	    findDupChldrnMI = new JMenuItem("Duplicate Children Check");
	    findDupChldrnMI.setActionCommand("Duplicate Children Check");
	    findDupChldrnMI.setEnabled(false);
	    findDupChldrnMI.addActionListener(this);
	    submenuFamilyDataChecks.add(findDupChldrnMI);
	    
	    crosscheckMI = new JMenuItem("CBO Crosscheck");
	    crosscheckMI.setActionCommand("CBO Crosscheck");
	    crosscheckMI.setEnabled(false);
	    crosscheckMI.addActionListener(this);
	    submenuFamilyDataChecks.add(crosscheckMI);
	    
	    menuFamilies.add(submenuFamilyDataChecks);
	    
	    menuFamilies.addSeparator();
	    
	    famHistMI = new JMenuItem("Family Histories");
	    famHistMI.setEnabled(false);
	    famHistMI.addActionListener(this);
	    menuFamilies.add(famHistMI);
	    
	    viewDBMI = new JMenuItem("View All Family Data");
	    viewDBMI.setEnabled(false);
	    viewDBMI.addActionListener(this);
	    menuFamilies.add(viewDBMI);
	    
	    this.add(menuFamilies);
	    
	    //Build Gifts Menu Structure
	    menuGifts = new JMenu("Gifts");
	    this.add(menuGifts);
	    
	    catMI = new JMenuItem("Manage Catalog");
	    catMI.setEnabled(false);
	    catMI.addActionListener(this);
	    menuGifts.add(catMI);
	    
	    sortGiftsMI = new JMenuItem("Manage Gifts");
	    sortGiftsMI.setActionCommand("Gifts");
	    sortGiftsMI.setEnabled(false);
	    sortGiftsMI.addActionListener(this);
	    menuGifts.add(sortGiftsMI);
	    
	    sortClonedGiftsMI = new JMenuItem("Manage Cloned Gifts");
	    sortClonedGiftsMI.setActionCommand("Cloned Gifts");
	    sortClonedGiftsMI.setEnabled(false);
	    sortClonedGiftsMI.addActionListener(this);
	    menuGifts.add(sortClonedGiftsMI);
	    
	    menuGifts.addSeparator();
	    
	    submenuReceiveGifts = new JMenu("Receive Gifts");
	    submenuReceiveGifts.setEnabled(false);
	    
	    recGiftsBarcodeMI = new JMenuItem("By Barcode Scan");
	    recGiftsBarcodeMI.setActionCommand("Receive Gifts - Barcode");
	    recGiftsBarcodeMI.setEnabled(false);
	    recGiftsBarcodeMI.addActionListener(this);;
	    submenuReceiveGifts.add(recGiftsBarcodeMI);
	    
	    recGiftsMI = new JMenuItem("By Table Lookup");
	    recGiftsMI.setActionCommand("Receive Gifts");
	    recGiftsMI.setEnabled(false);
	    recGiftsMI.addActionListener(this);;
	    submenuReceiveGifts.add(recGiftsMI);
	    
	    menuGifts.add(submenuReceiveGifts);
	    
	    barcodeGiftHistoryMI = new JMenuItem("Wish History");
	    barcodeGiftHistoryMI.setActionCommand("Barcode Wish History");
	    barcodeGiftHistoryMI.setEnabled(false);
	    barcodeGiftHistoryMI.addActionListener(this);
	    menuGifts.add(barcodeGiftHistoryMI);
	    
	    menuGifts.addSeparator();
	    
	    batteryMI = new JMenuItem("Add Batteries");
	    batteryMI.setActionCommand("Add Batteries");
	    batteryMI.setEnabled(false);
	    batteryMI.addActionListener(this);
	    menuGifts.add(batteryMI);
	    
	    manageBatteryMI = new JMenuItem("Manage Batteries");
	    manageBatteryMI.setActionCommand("Manage Batteries");
	    manageBatteryMI.setEnabled(false);
	    manageBatteryMI.addActionListener(this);
	    menuGifts.add( manageBatteryMI);
	    
	    menuGifts.addSeparator();
	    
	    inventoryMI = new JMenuItem("Gift Inventory");
	    inventoryMI.setActionCommand("Inventory");
	    inventoryMI.setEnabled(false);
	    inventoryMI.addActionListener(this);
	    menuGifts.add(inventoryMI);
	    
	    //Build Meals Menu Structure
	    menuMeals = new JMenu("Meals");
	    this.add(menuMeals);
	    
	    sortMealsMI = new JMenuItem("Manage Meals");
	    sortMealsMI.setActionCommand("Meals");
	    sortMealsMI.setEnabled(false);
	    sortMealsMI.addActionListener(this);
	    menuMeals.add(sortMealsMI);
	    
	    //Build Partners Menu
	    menuPartners = new JMenu("Partners");
	    
	    orgMI = new JMenuItem("Edit Partners");
	    orgMI.setActionCommand("Edit Partners");
	    orgMI.setEnabled(false);
	    orgMI.addActionListener(this);;
	    menuPartners.add(orgMI);
	    
	    sortOrgsMI = new JMenuItem("Manage Partners");
	    sortOrgsMI.setActionCommand("Partners");
	    sortOrgsMI.setEnabled(false);
	    sortOrgsMI.addActionListener(this);
	    menuPartners.add(sortOrgsMI);
	    
	    this.add(menuPartners);
	    
	    //build Volunteer Menu
	    menuVolunteers = new JMenu("Volunteers");
	    this.add(menuVolunteers);
	    
	    //Edit Volunteers
	    editVolMI = new JMenuItem("Edit Volunteers");
	    editVolMI.setActionCommand("Edit Volunteers");
	    editVolMI.setEnabled(false);
	    editVolMI.addActionListener(this);
	    menuVolunteers.add(editVolMI);
	    
	    //View Sign-In log
	    viewSignInLogMI = new JMenuItem("View Sign-In Log");
	    viewSignInLogMI.setActionCommand("View Sign-In Log");
	    viewSignInLogMI.setEnabled(false);
	    viewSignInLogMI.addActionListener(this);
	    menuVolunteers.add(viewSignInLogMI);
	    
	    //Manage Volunteers Dialog
	    manageVolMI = new JMenuItem("Manage Volunteers");
	    manageVolMI .setActionCommand("Manage Volunteers");
	    manageVolMI .setEnabled(false);
	    manageVolMI .addActionListener(this);
	    menuVolunteers.add(manageVolMI );
	    
	    menuVolunteers.addSeparator();
	    
	    //Edit Activities
	    editActMI = new JMenuItem("Edit Activities");
	    editActMI.setActionCommand("Edit Activities");
	    editActMI.setEnabled(false);
	    editActMI.addActionListener(this);
	    menuVolunteers.add(editActMI);
	    
	    //Manage Activities
	    manageActMI = new JMenuItem("Manage Activities");
	    manageActMI .setActionCommand("Manage Activities");
	    manageActMI .setEnabled(false);
	    manageActMI .addActionListener(this);
	    menuVolunteers.add(manageActMI );
	 
	    //Build Delivery Menu
	    menuDelivery = new JMenu("Deliveries");
	    menuDelivery.setEnabled(true);
	    this.add(menuDelivery);
	    
	    //Delivery Status check
	    schoolDelMI = new JMenuItem("Deliveries to Schools");
	    schoolDelMI.setActionCommand("Deliveries to Schools");
	    schoolDelMI.setEnabled(false);
	    schoolDelMI.addActionListener(this);
	    menuDelivery.add(schoolDelMI);
	    
	    //Manage Delivery Volunteers
	    manageDelMI = new JMenuItem("Manage Delivery Volunteers");
	    manageDelMI.setActionCommand("Drivers");
	    manageDelMI.setEnabled(false);
	    manageDelMI.addActionListener(this);
	    menuDelivery.add(manageDelMI);
	    
	    //Assign Delivery Partners
	    assignDelMI = new JMenuItem("Assign Deliveries");
	    assignDelMI.setActionCommand("Deliveries");
	    assignDelMI.setEnabled(false);
	    assignDelMI.addActionListener(this);
	    menuDelivery.add(assignDelMI);
	    
	    menuDelivery.addSeparator();
	    
	    //Edit Distribution Centers
	    editDistCentersMI = new JMenuItem("Edit Distribution Centers");
	    editDistCentersMI.setActionCommand("Edit Distribution Centers");
	    editDistCentersMI.setEnabled(false);
	    editDistCentersMI.addActionListener(this);;
	    menuDelivery.add(editDistCentersMI);
	    
	    //Assign Pickup Location
	    assignDistCentersMI = new JMenuItem("Assign Distribution Centers");
	    assignDistCentersMI.setActionCommand("Centers");
	    assignDistCentersMI.setEnabled(false);
	    assignDistCentersMI.addActionListener(this);
	    menuDelivery.add(assignDistCentersMI);
	    
	    menuDelivery.addSeparator();
	    
	    submenuDeliverGifts = new JMenu("Confirm Deliveries");
	    submenuDeliverGifts.setEnabled(false);
	    
	    barcodeDeliveryMI = new JMenuItem("By Barcode Scan");
	    barcodeDeliveryMI.setEnabled(false);
	    barcodeDeliveryMI.addActionListener(this);
	    submenuDeliverGifts.add(barcodeDeliveryMI);
	    
	    menuDelivery.add(submenuDeliverGifts);
	    
	    menuDelivery.addSeparator();
	    
	    mapsMI = new JMenuItem("Delivery Directions");	
	    mapsMI.setEnabled(false);
	    mapsMI.addActionListener(this);
	    menuDelivery.add(mapsMI);
	    
	    //Delivery Map & Directions
	    distMI = new JMenuItem("Delivery Distribution");	
	    distMI.setEnabled(false);
	    distMI.addActionListener(this);
	    menuDelivery.add(distMI);
	    	    
	    //Build Tools/Settings Menu
	    menuSettings = new JMenu("Tools/Settings");
	    this.add(menuSettings);
	    
	    aboutONCMI = new JMenuItem("About ONC");
	    aboutONCMI.addActionListener(this);
	    menuSettings.add(aboutONCMI);
	    
	    profileMI = new JMenuItem("Edit Profile");
	    profileMI.addActionListener(this);
	    menuSettings.add(profileMI);
	    
	    changePWMI = new JMenuItem("Change Password");
	    changePWMI.addActionListener(this);
	    menuSettings.add(changePWMI);
	    
	    oncPrefrencesMI = new JMenuItem("Preferences");
	    oncPrefrencesMI.addActionListener(this);
	    menuSettings.add(oncPrefrencesMI);
	    
	    submenuUsers = new JMenu("Users");
	    submenuUsers.setVisible(false);
	    menuSettings.add(submenuUsers);
	    
	    //Edit Users
	    editUsersMI = new JMenuItem("Edit Users");
	    editUsersMI.setActionCommand("Edit Users");
	    editUsersMI.addActionListener(this);
	    submenuUsers.add(editUsersMI);
	    
	    manageUsersMI = new JMenuItem("Manage Users");
	    manageUsersMI.setEnabled(false);
	    manageUsersMI.addActionListener(this);
	    submenuUsers.add(manageUsersMI);
	    
	    onlineMI = new JMenuItem("Who's Online?");
	    onlineMI.addActionListener(this);
	    menuSettings.add(onlineMI);
	    
	    chatMI = new JMenuItem("Private Chat");
	    chatMI.addActionListener(this);
	    menuSettings.add(chatMI);
	      
	    stopPollingMI = new JMenuItem("Stop Server Polling");
	    stopPollingMI.setVisible(false);
	    stopPollingMI.addActionListener(this);
	    menuSettings.add(stopPollingMI);
	    
	    showServerLogMI = new JMenuItem("Message Log");
	    showServerLogMI.addActionListener(this);
	    menuSettings.add(showServerLogMI);
	    
	    showServerClientIDMI = new JMenuItem("Server Client ID");
	    showServerClientIDMI.addActionListener(this);
	    menuSettings.add(showServerClientIDMI);
	    
	    showCurrDirMI = new JMenuItem("Current Directory");
	    showCurrDirMI.addActionListener(this);
	    menuSettings.add(showCurrDirMI);
	    
	    showWebsiteStatusMI = new JMenuItem("Website Status");
	    showWebsiteStatusMI.setVisible(false);
	    showWebsiteStatusMI.addActionListener(this);
	    menuSettings.add(showWebsiteStatusMI);
	    
	    //get a reference to the dialog manager and database manager
	    dlgManager = DialogManager.getInstance();
	    dbManager = DatabaseManager.getInstance();
	}
	
	public static MenuBar getInstance()
	{
		if(instance == null)
			instance = new MenuBar();
		
		return instance;
	}
	
	/******************************************************************************************
     * Adds a DBYear to the Database menu SelectYear submenu
     ****************************************************************************************/
    void addDBYearToSubmenu(DBYear dbYear)
    {	
    		String zYear = Integer.toString(dbYear.getYear());
		
		JMenuItem mi = new JMenuItem(zYear, GlobalVariablesDB.getInstance().getImageIcon(dbYear.isLocked() ? 
				DB_LOCKED_IMAGE_INDEX : DB_UNLOCKED_IMAGE_INDEX));
		mi.setActionCommand(zYear);
			
		dbYearsMIList.add(mi);
		submenuDBYearList.add(mi);
		
		mi.addActionListener(new MenuItemDBYearsListener());
    }

	void processDBYears(List<DBYear> dbYears)
    {
		//clear the current list
		submenuDBYearList.removeAll();
    	
		for(DBYear dbYear:dbYears)
			addDBYearToSubmenu(dbYear);
		
		//determine if we can allow the user to add a new season. Enable adding a new
		//season if the current date is in the year to be added, the year hasn't already
		//been added, the user has administrative privileges and a data base has not been loaded
		Calendar today = Calendar.getInstance();
		today.setTime(GlobalVariablesDB.getInstance().getTodaysDate());
		int currYear = today.get(Calendar.YEAR);
		
		if(currYear != dbYears.get(dbYears.size()-1).getYear() && 
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			setEnabledNewMenuItem(true);	
    }
	
	void SetEnabledMenuItems(boolean tf)	//Only with at least one family present in db
	{
//		clearMI.setEnabled(tf);		//Disable clear until fixed after migration to Client Server Architecture
		schoolDelMI.setEnabled(tf);
		assignDelMI.setEnabled(tf);
		assignDistCentersMI.setEnabled(tf);
		mapsMI.setEnabled(tf);	
		distMI.setEnabled(tf);	
		sortGiftsMI.setEnabled(true);
		sortClonedGiftsMI.setEnabled(true);
		submenuReceiveGifts.setEnabled(true);
		recGiftsMI.setEnabled(true);
		recGiftsBarcodeMI.setEnabled(true);
		barcodeGiftHistoryMI.setEnabled(true);
		batteryMI.setEnabled(true);
		manageBatteryMI.setEnabled(true);
		submenuReceiveGifts.setEnabled(true);
		submenuDeliverGifts.setEnabled(true);
		barcodeDeliveryMI.setEnabled(tf);
	}
	
	void setEnabledRestrictedMenuItems(boolean tf)	//Only Admins can perform these functions
	{												//when at least one family is present in db
		findDupFamsMI.setEnabled(tf);
		findDupChldrnMI.setEnabled(tf);
		crosscheckMI.setEnabled(tf);
		viewDBMI.setEnabled(tf);
		submenuExport.setEnabled(tf);
		submenuChangeFamilyNumbers.setEnabled(tf);
		sortFamiliesMI.setEnabled(tf);
		sortMealsMI.setEnabled(tf);
		markAdultMI.setEnabled(tf);
		delChildMI.setEnabled(tf);
		newChildMI.setEnabled(tf);
		connectChildMI.setEnabled(tf);
		sortClonedGiftsMI.setEnabled(true);
		manageCallResultMI.setEnabled(tf);	
	}
	
	void setEnabledWishCatalogAndOrgMenuItems(boolean tf)
	{
		catMI.setEnabled(tf);	//Once new season created can manage wishes and partners
		orgMI.setEnabled(tf);	//or when a file is opened
		editDistCentersMI.setEnabled(tf);
		sortOrgsMI.setEnabled(tf);
	}	
	
	void setVisibleSpecialImports(boolean tf)	//Only Sys_Admin's can perform these functions
    {
		manageUsersMI.setEnabled(tf);
    		showWebsiteStatusMI.setVisible(true);
    		stopPollingMI.setVisible(true);
    }
	
	void setVisibleAdminFunctions(boolean tf)
	{
		newMI.setVisible(tf);
		dbStatusMI.setVisible(tf);
		submenuUsers.setVisible(tf);
	}
	
	void setEnabledServerConnected(boolean tf)
	{
		submenuDBYearList.setEnabled(tf);
	}
	
	void setEnabledYear(boolean tf) {submenuDBYearList.setEnabled(tf);}
	void setEnabledNewMenuItem(boolean tf) { newMI.setEnabled(tf); }
	void setEnabledAdminDataLoadedMenuItems(boolean tf) 
	{ 
		submenuImport.setEnabled(tf);
		manageSMSMI.setEnabled(tf);
//		newFamMI.setEnabled(tf);
	}
	
	void setEnabledMarkorDeleteChildMenuItem(boolean tf)
	{ 
		delChildMI.setEnabled(tf);
		markAdultMI.setEnabled(tf);
	}
	void setEnabledDataLoadedMenuItems(boolean tf)
	{ 
		agentMI.setEnabled(tf);
		groupMI.setEnabled(tf);
		manageGroupsMI.setEnabled(tf);
		manageDelMI.setEnabled(tf);
		inventoryMI.setEnabled(tf);
		editActMI.setEnabled(tf);
		editVolMI.setEnabled(tf);
		viewSignInLogMI.setEnabled(tf);
		manageVolMI.setEnabled(tf);
		manageActMI.setEnabled(tf);
		famHistMI.setEnabled(tf);
		editDNSCodesMI.setEnabled(tf);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DBYEAR"))
		{
//			System.out.println("ONCMenuBar.dataChanged: UPDATED_DBYEAR Received");
			//find the menu item associated with the year and update the lock status
			DBYear updatedDBYear = (DBYear) dbe.getObject1();
			
			int index=0;
			while(index<dbYearsMIList.size() &&
					!dbYearsMIList.get(index).getActionCommand().equals(updatedDBYear.toString()))
				index++;
			
			if(index < dbYearsMIList.size())
				dbYearsMIList.get(index).setIcon(updatedDBYear.isLocked() ? 
						GlobalVariablesDB.getLockedIcon() : GlobalVariablesDB.getUnLockedIcon());
		}
		else if(dbe.getType().equals("ADDED_DBYEAR"))
		{
			//returns a list of added years. Clear the menu item list of years and add new ones
			@SuppressWarnings("unchecked")
			List<DBYear> dbYears = (List<DBYear>) dbe.getObject1();
			processDBYears(dbYears);
			
			//now that the year is added, disable adding another year
//			setEnabledNewMenuItem(false);
		}
		
		//if this is the first family loaded locally, show it on the display
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_FAMILY"))
		{
			if(familyDB.size() == 1)
			{
				SetEnabledMenuItems(true);
				if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
					setEnabledRestrictedMenuItems(true);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			setEnabledYear(false);
    		setEnabledNewMenuItem(false);
    		setEnabledWishCatalogAndOrgMenuItems(true);
    		
    		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
				setEnabledAdminDataLoadedMenuItems(true);
    		
    		if(familyDB.size() > 0)
			{
				SetEnabledMenuItems(true);
				if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0) 
					setEnabledRestrictedMenuItems(true);
			}
    		
    		setEnabledDataLoadedMenuItems(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == newMI) { dbManager.addONCSeason(); }
		else if(e.getSource() == importBritepathsMI) {familyDB.importBPFile(GlobalVariablesDB.getFrame()); }
		else if(e.getSource() == manageCallResultMI) {dlgManager.showAngelCallDialog();}
		else if(e.getSource() == manageSMSMI) {dlgManager.showManageSMSDialog();}
		else if(e.getSource() == exportMI){ dbManager.exportComponentDBToCSV(); }
		else if(e.getSource() == dbStatusMI) {dlgManager.onDBStatusClicked();}
		else if(e.getSource() == clearMI) {dlgManager.onClearMenuItemClicked();} 			       	
//		else if(e.getSource() == exitMI)	{exit("LOGOUT");}
		else if(e.getSource() == findDupFamsMI)
			dlgManager.showCheckDialog(findDupFamsMI.getActionCommand());
		else if(e.getSource() == findDupChldrnMI)
			dlgManager.showCheckDialog(findDupChldrnMI.getActionCommand());
		else if(e.getSource() == crosscheckMI)
			dlgManager.showCrosscheckDialog();
		else if(e.getSource() == assignDelMI)
			dlgManager.showSortDialog(assignDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == editDistCentersMI)
			dlgManager.showEntityDialog(editDistCentersMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == assignDistCentersMI)
			dlgManager.showAssignDistributionCenterDialog();
		else if(e.getSource() == editVolMI)
			dlgManager.showEntityDialog(editVolMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == editDNSCodesMI)
			dlgManager.showEntityDialog(editDNSCodesMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == editActMI)
			dlgManager.showEntityDialog(editActMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == editUsersMI)
			dlgManager.showEntityDialog(editUsersMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == manageDelMI)
			dlgManager.showSortDialog(manageDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == barcodeDeliveryMI)
			dlgManager.showBarcodeDeliveryDialog();
//		else if(e.getSource() == importVolMI)
//		{
//			VolunteerDB volunteerDB = VolunteerDB.getInstance();
//			String mssg = volunteerDB.importSignUpGeniusVolunteers(GlobalVariables.getFrame(), 
//									GlobalVariables.getInstance().getTodaysDate(),
//									userDB.getUserLNFI(), GlobalVariables.getONCLogo());
//			
//			//Information message that the drivr import completed successfully
//		    JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg,
//					"Import Result", JOptionPane.INFORMATION_MESSAGE, GlobalVariables.getONCLogo());
//		}
		else if(e.getSource() == mapsMI) {dlgManager.showDrivingDirections();}
		else if(e.getSource() == distMI) {dlgManager.showClientMap();}
		else if(e.getSource() == changeONCMI)
			dlgManager.showFamilyInfoDialog(changeONCMI.getActionCommand());
		else if(e.getSource() == changeRefMI)
			dlgManager.showFamilyInfoDialog(changeRefMI.getActionCommand());
		else if(e.getSource() == changeBatchMI)
			dlgManager.showFamilyInfoDialog(changeBatchMI.getActionCommand());
		else if(e.getSource() == schoolDelMI)
			dlgManager.showSchoolDelDialog(schoolDelMI.getActionCommand());
		else if(e.getSource() == viewDBMI) {dlgManager.showEntireDatabase();}
		else if(e.getSource() == sortGiftsMI)
			dlgManager.showSortDialog(sortGiftsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortClonedGiftsMI)
			dlgManager.showSortDialog(sortClonedGiftsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortMealsMI)
			dlgManager.showSortDialog(sortMealsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == recGiftsMI)
			dlgManager.showSortDialog(recGiftsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == barcodeGiftHistoryMI)
			dlgManager.showBarcodeWishHistoryDialog();
		else if(e.getSource() == batteryMI)
			dlgManager.showBatteryDialog();
		else if(e.getSource() == recGiftsBarcodeMI )
			dlgManager.showReceiveGiftDialog();
		else if(e.getSource() == manageBatteryMI)
			dlgManager.showManageBatteryDialog();
		else if(e.getSource() == inventoryMI)
			dlgManager.showInventoryDialog();
		else if(e.getSource() == catMI) {dlgManager.showWishCatalogDialog(); }
		else if(e.getSource() == orgMI)
			dlgManager.showEntityDialog(orgMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortOrgsMI)
			dlgManager.showSortDialog(sortOrgsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortFamiliesMI)
			dlgManager.showSortDialog(sortFamiliesMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == agentMI)
			dlgManager.showSortDialog(agentMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == groupMI)
			dlgManager.showEntityDialog(groupMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == manageGroupsMI)
			dlgManager.showManageGroupsDialog();
		else if(e.getSource() == aboutONCMI)
			dlgManager.showAboutONCDialog();
		else if(e.getSource() == oncPrefrencesMI)
			dlgManager.showPreferencesDialog();
		else if(e.getSource() == famHistMI)
			dlgManager.showFamilyHistoriesDialog();
		else if(e.getSource() == newFamMI)
		{
			AddFamilyDialog afDlg = new AddFamilyDialog(GlobalVariablesDB.getFrame());
			afDlg.setVisible(true);
		}
		else if(e.getSource() == delChildMI) { dlgManager.onDeleteChild(); }
		else if(e.getSource() == newChildMI) { dlgManager.onAddNewChildClicked(); }
		else if(e.getSource() == markAdultMI) { dlgManager.onMarkChildAsAdult(); }
		else if(e.getSource() == connectChildMI) { dlgManager.showConnectPYChildDialog(); }
		else if(e.getSource() == manageUsersMI) { dlgManager.showUserDialog(); }
		else if(e.getSource() == viewSignInLogMI) { dlgManager.showSignInDialog(); }
		else if(e.getSource() == manageVolMI) { dlgManager.showManageVolDialog(); }
		else if(e.getSource() == manageActMI) { dlgManager.showManageActDialog(); }
		else if(e.getSource() == onlineMI) { dlgManager.showOnlineUsers(); }
		else if(e.getSource() == chatMI) { dlgManager.onChat(); }
		else if(e.getSource() == profileMI) { dlgManager.onEditProfile(); }
		else if(e.getSource() == changePWMI) { dlgManager.onChangePassword(); }
		else if(e.getSource() == stopPollingMI)
		{
			ServerIF serverIF = ServerIF.getInstance();
			if(serverIF != null)
				serverIF.setEnabledServerPolling(false); 
		}
		else if(e.getSource() == showServerLogMI)
		{
			ServerIF serverIF = ServerIF.getInstance();
			if(serverIF != null)
			{
				LogDialog logDlg = new LogDialog();
				logDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
				logDlg.setVisible(true);
			}
		}
		else if(e.getSource() == showServerClientIDMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariablesDB.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			String mssg = String.format("Your ONC Server Client ID is: %d", 
								userDB.getLoggedInUser().getClientID());
			clientIDPU.show("ONC Server Client ID", mssg);
		}    		
		else if(e.getSource() == showCurrDirMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariablesDB.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			String mssg = String.format("Current folder is: %s", System.getProperty("user.dir"));
			clientIDPU.show("ONC Client Current Folder", mssg);
		}
		else if(e.getSource() == showWebsiteStatusMI) { dlgManager.onWebsiteStatus(); }
	}
	
	private class MenuItemDBYearsListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
	    {
			for(int i=0; i< dbYearsMIList.size(); i++)
	    	{
	    		JMenuItem mi = dbYearsMIList.get(i);
	    		if(e.getSource() == mi)
	    		{	
	    			dbManager.importObjectsFromDB(Integer.parseInt(e.getActionCommand()));
	    			break;
	    		}
	    	}
	    }
	}
}
