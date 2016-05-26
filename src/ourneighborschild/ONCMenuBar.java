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
import javax.swing.JOptionPane;

public class  ONCMenuBar extends JMenuBar implements ActionListener, DatabaseListener
{
	/**
	 * This singleton class provides the blueprint for the menu bar in the ONC application
	 * Much of the user interface is provided thru this menu bar.
	 */
	private static final long serialVersionUID = 1L;
	private static final Point SORT_DIALOG_OFFSET = new Point (5, 20);
	private static final int DB_UNLOCKED_IMAGE_INDEX = 17;
	private static final int DB_LOCKED_IMAGE_INDEX = 18;
	
	private static ONCMenuBar instance;
	
	private DatabaseManager oncDB;
	private JMenuItem newMI;
	private JMenuItem importODBMI, importWFCMMI, importRAFMI;
	private JMenuItem importONCMI, importPYMI, importPYORGMI,importWishCatMI, manageCallResultMI;
	private JMenuItem exportMI, dbStatusMI, clearMI;
	public JMenuItem exitMI;	//public since exit method is external to the menu bar
//	public static JMenuItem font8, font10, font12, font13, font14, font16, font18;
//	private static JMenuItem compODBtoONCfamMI, compODBtoONCdataMI, compWFCMtoONCfamMI, compWFCMtoONCdataMI;
	private JMenuItem findDupFamsMI, findDupChldrnMI;
	private JMenuItem assignDelMI, editDelMI, manageDelMI, importDrvrMI, mapsMI, delstatusMI, distMI;
	private JMenuItem newFamMI, changeONCMI, changeRefMI, changeBatchMI, newChildMI, delChildMI, markAdultMI, connectChildMI;
	private JMenu submenuImport, submenuFamilyDataChecks;
	private JMenu submenuExport, submenuChangeFamilyNumbers, submenuDBYearList;
	private JMenuItem viewDBMI, sortWishesMI, sortFamiliesMI, sortOrgsMI, recGiftsMI, sortMealsMI;
	private JMenuItem agentMI, orgMI, catMI, barcodeWishHistoryMI;
	private JMenuItem aboutONCMI, oncPrefrencesMI, profileMI, userMI, onlineMI, chatMI, changePWMI, stopPollingMI;
	private JMenuItem showServerLogMI, showServerClientIDMI, showCurrDirMI, showWebsiteStatusMI;
	private List<JMenuItem> dbYearsMIList;
	
	private DialogManager dlgManager;
	private DatabaseManager dbManager;
	
	private FamilyDB familyDB;
	
	private ONCMenuBar()
	{
		//get reference to onc data base
		oncDB = DatabaseManager.getInstance();
		if(oncDB != null)
			oncDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		JMenu menuDatabase, menuAgents, menuFamilies, menuWishes, menuMeals, menuPartners, menuDelivery, menuSettings;	    
        
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
	       
	    importONCMI = new JMenuItem("Curent year ONC .csv file");
	    importONCMI.setVisible(false);
	    importONCMI.addActionListener(this);
	    submenuImport.add(importONCMI);
	    
	    importPYMI = new JMenuItem("Prior year ONC .csv file");
	    importPYMI.setVisible(false);
	    importPYMI.addActionListener(this);
	    submenuImport.add(importPYMI);
	    
	    importPYORGMI = new JMenuItem("ONC Organizations .csv file");
	    importPYORGMI.setVisible(false);
	    importPYORGMI.addActionListener(this);
	    submenuImport.add(importPYORGMI);
	    
	    importWishCatMI = new JMenuItem("Wish Catalog .csv file");
	    importWishCatMI.setVisible(false);
	    importWishCatMI.addActionListener(this);
	    submenuImport.add(importWishCatMI);
	  	    
	    importODBMI = new JMenuItem("From ODB...");
	    importODBMI.addActionListener(this);
	    submenuImport.add(importODBMI);
	    
	    importWFCMMI = new JMenuItem("From WFCM...");
	    importWFCMMI.addActionListener(this);
	    submenuImport.add(importWFCMMI);

	    //Import Delivery Partners
	    importDrvrMI = new JMenuItem("Delivery Partners...");
	    importDrvrMI.addActionListener(this);
	    submenuImport.add(importDrvrMI);
	    
	    //Import Referring Agent Families
	    importRAFMI = new JMenuItem("RA Families...");	
	    importRAFMI.addActionListener(this);
	    submenuImport.add(importRAFMI);
    
	    menuDatabase.add(submenuImport);
	    
	    submenuExport = new JMenu("Export");
	    submenuExport.setEnabled(false);
	    	    
	    exportMI = new JMenuItem("Database to .csv files");
	    exportMI.addActionListener(this);
	    submenuExport.add(exportMI);
	    
	    menuDatabase.add(submenuExport);
	   
	    menuDatabase.addSeparator();
	    
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
	    
	    //Manage Angel Call Results
	    manageCallResultMI = new JMenuItem("Manage Call Results");
	    manageCallResultMI.setEnabled(false);
	    manageCallResultMI.addActionListener(this);
	    menuFamilies.add(manageCallResultMI);
	    
	    menuFamilies.addSeparator();
	    
	    newFamMI = new JMenuItem("Add New Family");
//	    newFamMI.setEnabled(false);
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
	    
	    menuFamilies.add(submenuFamilyDataChecks);
	    
	    menuFamilies.addSeparator();
	    
	    viewDBMI = new JMenuItem("View All Family Data");
	    viewDBMI.setEnabled(false);
	    viewDBMI.addActionListener(this);
	    menuFamilies.add(viewDBMI);
	    
	    this.add(menuFamilies);
	    
	    //Build Wishes Menu Structure
	    menuWishes = new JMenu("Wishes");
	    this.add(menuWishes);
	    
	    catMI = new JMenuItem("Manage Catalog");
	    catMI.setEnabled(false);
	    catMI.addActionListener(this);
	    menuWishes.add(catMI);
	    
	    sortWishesMI = new JMenuItem("Manage Wishes");
	    sortWishesMI.setActionCommand("Wishes");
	    sortWishesMI.setEnabled(false);
	    sortWishesMI.addActionListener(this);
	    menuWishes.add(sortWishesMI);
	    
	    menuWishes.addSeparator();
	    
	    recGiftsMI = new JMenuItem("Receive Gifts");
	    recGiftsMI.setActionCommand("Receive Gifts");
	    recGiftsMI.setEnabled(false);
	    recGiftsMI.addActionListener(this);;
	    menuWishes.add(recGiftsMI);
	    
	    barcodeWishHistoryMI = new JMenuItem("Wish History");
	    barcodeWishHistoryMI.setActionCommand("Barcode Wish History");
	    barcodeWishHistoryMI.setEnabled(false);
	    barcodeWishHistoryMI.addActionListener(this);
	    menuWishes.add(barcodeWishHistoryMI);
	    
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
	    
	    //Build Delivery Menu
	    menuDelivery = new JMenu("Deliveries");
	    menuDelivery.setEnabled(true);
	    this.add(menuDelivery);
	    
	    //Delivery Status check
	    delstatusMI = new JMenuItem("Delivery Status");
	    delstatusMI.setActionCommand("Delivery History");
	    delstatusMI.setEnabled(false);
	    delstatusMI.addActionListener(this);
	    menuDelivery.add(delstatusMI);
	    
	    //Edit Delivery Partners
	    editDelMI = new JMenuItem("Edit Delivery Partners");
	    editDelMI.setActionCommand("Edit Delivery Partners");
	    editDelMI.addActionListener(this);
	    menuDelivery.add(editDelMI);
	    
	    //Edit Delivery Partners
	    manageDelMI = new JMenuItem("Manage Delivery Partners");
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
	    
	    mapsMI = new JMenuItem("Delivery Directions");	
	    mapsMI.setEnabled(false);
	    mapsMI.addActionListener(this);
	    menuDelivery.add(mapsMI);
	    
	    //Delivery Map & Directions
	    distMI = new JMenuItem("Delivery Distribution");	
	    distMI.setEnabled(false);
	    distMI.addActionListener(this);
	    menuDelivery.add(distMI);
	    	    
	    //Build About Menu
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
	    
	    userMI = new JMenuItem("Manage Users");
	    userMI.setVisible(false);
	    userMI.addActionListener(this);
	    menuSettings.add(userMI);
	    
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
	
	public static ONCMenuBar getInstance()
	{
		if(instance == null)
			instance = new ONCMenuBar();
		
		return instance;
	}
	
	/******************************************************************************************
     * Adds a DBYear to the Database menu SelectYear submenu
     ****************************************************************************************/
    void addDBYearToSubmenu(DBYear dbYear)
    {	
    	String zYear = Integer.toString(dbYear.getYear());
		
		JMenuItem mi = new JMenuItem(zYear, GlobalVariables.getInstance().getImageIcon(dbYear.isLocked() ? 
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
		today.setTime(GlobalVariables.getInstance().getTodaysDate());
		int currYear = today.get(Calendar.YEAR);
		
		if(currYear != dbYears.get(dbYears.size()-1).getYear() && GlobalVariables.isUserAdmin())
			setEnabledNewMenuItem(true);	
    }
	
	void SetEnabledMenuItems(boolean tf)	//Only with at least one family present in db
	{
//		clearMI.setEnabled(tf);		//Disable clear until fixed after migration to Client Server Architecture
		delstatusMI.setEnabled(tf);
		assignDelMI.setEnabled(tf);
		mapsMI.setEnabled(tf);	
		distMI.setEnabled(tf);	
		sortWishesMI.setEnabled(true);
		recGiftsMI.setEnabled(true);
		barcodeWishHistoryMI.setEnabled(true);
	}
	
	void setEnabledRestrictedMenuItems(boolean tf)	//Only Admins can perform these functions
	{												//when at least one family is present in db
		findDupFamsMI.setEnabled(tf);
		findDupChldrnMI.setEnabled(tf);
		viewDBMI.setEnabled(tf);
		submenuExport.setEnabled(tf);
		submenuChangeFamilyNumbers.setEnabled(tf);
//		changeONCMI.setEnabled(tf);
//		changeRefMI.setEnabled(tf);
		sortFamiliesMI.setEnabled(tf);
		sortMealsMI.setEnabled(tf);
		markAdultMI.setEnabled(tf);
		delChildMI.setEnabled(tf);
		newChildMI.setEnabled(tf);
		connectChildMI.setEnabled(tf);
		manageCallResultMI.setEnabled(tf);
	}
	
	void setEnabledWishCatalogAndOrgMenuItems(boolean tf)
	{
		catMI.setEnabled(tf);	//Once new season created can manage wishes and partners
		orgMI.setEnabled(tf);	//Or when a file is opened
		sortOrgsMI.setEnabled(tf);
	}	
	
	void setVisibleSpecialImports(boolean tf)	//Only Superuser can perform these functions
    {
//    	importONCMI.setVisible(tf);	//Only with no data loaded
//    	importWishCatMI.setVisible(tf);
//    	importPYMI.setVisible(tf);	//Only with no prior year data loaded
//    	importPYORGMI.setVisible(tf);
    	submenuImport.setEnabled(true);
    	
    	showWebsiteStatusMI.setVisible(true);
    	stopPollingMI.setVisible(true);
    }
	
	void setVisibleAdminFunctions(boolean tf)
	{
		newMI.setVisible(tf);
		dbStatusMI.setVisible(tf);
		userMI.setVisible(tf);
	}
	
	void setEnabledServerConnected(boolean tf)
	{
		submenuDBYearList.setEnabled(tf);
	}
	
	void setEnabledYear(boolean tf) {submenuDBYearList.setEnabled(tf);}
	void setEnabledNewMenuItem(boolean tf) { newMI.setEnabled(tf); }
	void setEnabledPriorYearSpecialImport(boolean tf) { importPYMI.setEnabled(tf); }
	void setEnabledImportMenuItems(boolean tf) { submenuImport.setEnabled(tf); }
	void setEnabledMarkorDeleteChildMenuItem(boolean tf)
	{ 
		delChildMI.setEnabled(tf);
		markAdultMI.setEnabled(tf);
	}
	void setEnabledDataLoadedMenuItems(boolean tf)
	{ 
		agentMI.setEnabled(tf);
		manageDelMI.setEnabled(tf);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DBYEAR"))
		{
//			System.out.println("ONCMenuBar.dataChanged: UPDATED_DBYEAR Received");
			//find the menu item associated with the year and update the lock status
			DBYear updatedDBYear = (DBYear) dbe.getObject();
			
			int index=0;
			while(index<dbYearsMIList.size() &&
					!dbYearsMIList.get(index).getActionCommand().equals(updatedDBYear.toString()))
				index++;
			
			if(index < dbYearsMIList.size())
				dbYearsMIList.get(index).setIcon(updatedDBYear.isLocked() ? 
						GlobalVariables.getLockedIcon() : GlobalVariables.getUnLockedIcon());
		}
		else if(dbe.getType().equals("ADDED_DBYEAR"))
		{
			//returns a list of added years. Clear the menu item list of years and add new ones
			@SuppressWarnings("unchecked")
			List<DBYear> dbYears = (List<DBYear>) dbe.getObject();
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
				if(GlobalVariables.isUserAdmin()) 
					setEnabledRestrictedMenuItems(true);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			setEnabledYear(false);
    		setEnabledNewMenuItem(false);
    		setEnabledWishCatalogAndOrgMenuItems(true);
    		
    		if(GlobalVariables.isUserAdmin()) 
				setEnabledImportMenuItems(true);
    		
    		if(familyDB.size() > 0)
			{
				SetEnabledMenuItems(true);
				if(GlobalVariables.isUserAdmin()) 
					setEnabledRestrictedMenuItems(true);
			}
    		
    		setEnabledDataLoadedMenuItems(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == newMI) { dbManager.addONCSeason(); }
//		else if(e.getSource() == importONCMI) {OnImportMenuItemClicked("ONC");}
		else if(e.getSource() == importONCMI) { }
		else if(e.getSource() == importWishCatMI)
		{
			ONCWishCatalog cat = ONCWishCatalog.getInstance();
			cat.importWishCatalog(GlobalVariables.getFrame(), GlobalVariables.getONCLogo(), null);
		}
		else if(e.getSource() == importPYMI)
		{
			//A prior year child database will be read and then split into different databases that 
			//correspond to prior year child birth years. This allows higher performance when trying
		    //to match a child from one season with a child from another season. 
			//This ArrayList holds each array lists of PriorYearChild objects by year of birth
//			ReadPriorYearCSVFile(oncPYCDB.getPriorYearChildAL());
//		    ArrayList<ArrayList<ONCPriorYearChild>> pycbyAgeAL = buildPriorYearByAgeArrayList();
			    			
			//Enable the family panel to use prior year child data bases
//		    if(!pycbyAgeAL.isEmpty()) { oncFamilyPanel.setPriorYearChildArrayList(pycbyAgeAL); }
		}
		else if(e.getSource() == importPYORGMI)
		{
			PartnerDB partnerDB = PartnerDB.getInstance();
			partnerDB.importOrgDB(GlobalVariables.getFrame(), GlobalVariables.getONCLogo(), null);
		}
		else if(e.getSource() == importODBMI) {dbManager.onImportMenuItemClicked("ODB");}
		else if(e.getSource() == importWFCMMI) {dbManager.onImportMenuItemClicked("WFCM");}
		else if(e.getSource() == importRAFMI) { dlgManager.onImportRAFMenuItemClicked(); }
		else if(e.getSource() == manageCallResultMI) {dlgManager.showAngelCallDialog();}
		else if(e.getSource() == exportMI){ dbManager.exportObjectDBToCSV(); }
		else if(e.getSource() == dbStatusMI) {dlgManager.onDBStatusClicked();}
		else if(e.getSource() == clearMI) {dlgManager.onClearMenuItemClicked();} 			       	
//		else if(e.getSource() == exitMI)	{exit("LOGOUT");}
		else if(e.getSource() == findDupFamsMI)
			dlgManager.showCheckDialog(findDupFamsMI.getActionCommand());
		else if(e.getSource() == findDupChldrnMI)
			dlgManager.showCheckDialog(findDupChldrnMI.getActionCommand());
		else if(e.getSource() == assignDelMI)
			dlgManager.showSortDialog(assignDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == editDelMI)
			dlgManager.showEntityDialog(editDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == manageDelMI)
			dlgManager.showSortDialog(manageDelMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == importDrvrMI)
		{
			DriverDB driverDB = DriverDB.getInstance();
			String mssg = driverDB.importDrivers(GlobalVariables.getFrame(), 
									GlobalVariables.getInstance().getTodaysDate(),
									GlobalVariables.getUserLNFI(), GlobalVariables.getONCLogo());
			
//			oncFamilyPanel.refreshDriverDisplays();	//Update dialog based on imported info
			
			//Information message that the drivr import completed successfully
		    JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg,
					"Import Result", JOptionPane.INFORMATION_MESSAGE, GlobalVariables.getONCLogo());
		}
		else if(e.getSource() == mapsMI) {dlgManager.showDrivingDirections();}
		else if(e.getSource() == distMI) {dlgManager.showClientMap();}
		else if(e.getSource() == changeONCMI)
			dlgManager.showFamilyInfoDialog(changeONCMI.getActionCommand());
		else if(e.getSource() == changeRefMI)
			dlgManager.showFamilyInfoDialog(changeRefMI.getActionCommand());
		else if(e.getSource() == changeBatchMI)
			dlgManager.showFamilyInfoDialog(changeBatchMI.getActionCommand());
		else if(e.getSource() == delstatusMI)
			dlgManager.showHistoryDialog(delstatusMI.getActionCommand());
		else if(e.getSource() == viewDBMI) {dlgManager.showEntireDatabase();}
		else if(e.getSource() == sortWishesMI)
			dlgManager.showSortDialog(sortWishesMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortMealsMI)
			dlgManager.showSortDialog(sortMealsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == recGiftsMI)
			dlgManager.showSortDialog(recGiftsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == barcodeWishHistoryMI)
			dlgManager.showBarcodeWishHistoryDialog();
		else if(e.getSource() == catMI) {dlgManager.showWishCatalogDialog(); }
		else if(e.getSource() == orgMI)
			dlgManager.showEntityDialog(orgMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortOrgsMI)
			dlgManager.showSortDialog(sortOrgsMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == sortFamiliesMI)
			dlgManager.showSortDialog(sortFamiliesMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == agentMI)
			dlgManager.showSortDialog(agentMI.getActionCommand(), SORT_DIALOG_OFFSET);
		else if(e.getSource() == aboutONCMI)
			dlgManager.showAboutONCDialog();
		else if(e.getSource() == oncPrefrencesMI)
			dlgManager.showPreferencesDialog();
		else if(e.getSource() == newFamMI)
		{
			AddFamilyDialog afDlg = new AddFamilyDialog(GlobalVariables.getFrame());
			afDlg.setVisible(true);
		}
		else if(e.getSource() == delChildMI) { dlgManager.onDeleteChild(); }
		else if(e.getSource() == newChildMI) { dlgManager.onAddNewChildClicked(); }
		else if(e.getSource() == markAdultMI) { dlgManager.onMarkChildAsAdult(); }
		else if(e.getSource() == connectChildMI) { dlgManager.showConnectPYChildDialog(); }
		else if(e.getSource() == userMI) { dlgManager.showUserDialog(); }
		else if(e.getSource() == onlineMI) { dlgManager.onWhoIsOnline(); }
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
				logDlg.setLocationRelativeTo(GlobalVariables.getFrame());
				logDlg.setVisible(true);
			}
		}
		else if(e.getSource() == showServerClientIDMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariables.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			String mssg = String.format("Your ONC Server Client ID is: %d", 
								GlobalVariables.getInstance().getUser().getClientID());
			clientIDPU.show("ONC Server Client ID", mssg);
		}    		
		else if(e.getSource() == showCurrDirMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariables.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
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
	    			dbManager.importObjectsFromDB(Integer.parseInt(e.getActionCommand())); 
	    	}
	    }
	}
}
