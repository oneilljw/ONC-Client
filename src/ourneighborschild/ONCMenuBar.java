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

public class ONCMenuBar extends JMenuBar implements ActionListener, DatabaseListener
{
	/**
	 * This class provides the blueprint for the menu bar in the ONC application
	 */
	private static final long serialVersionUID = 1L;
	private static final Point SORT_DIALOG_OFFSET = new Point (5, 20);
	private static final int DB_UNLOCKED_IMAGE_INDEX = 17;
	private static final int DB_LOCKED_IMAGE_INDEX = 18;
	
	private DatabaseManager oncDB;
	private static JMenuItem newMI;
	private static JMenuItem importODBMI, importWFCMMI, importRAFMI;
	private static JMenuItem importONCMI, importPYMI, importPYORGMI,importWishCatMI, manageCallResultMI;
	private static JMenuItem exportMI, dbStatusMI, clearMI;
	public static JMenuItem exitMI;	//public since exit method is external to the menu bar
//	public static JMenuItem font8, font10, font12, font13, font14, font16, font18;
//	private static JMenuItem compODBtoONCfamMI, compODBtoONCdataMI, compWFCMtoONCfamMI, compWFCMtoONCdataMI;
	private static JMenuItem findDupFamsMI, findDupChldrnMI;
	private static JMenuItem assignDelMI, editDelMI, manageDelMI, importDrvrMI, mapsMI, delstatusMI, distMI;
	private static JMenuItem newFamMI, changeONCMI, changeRefMI, changeBatchMI, newChildMI, delChildMI, markAdultMI, connectChildMI;
	private static JMenu submenuImport, submenuFamilyDataChecks;
	private static JMenu submenuExport, submenuChangeFamilyNumbers, submenuCompareData, submenuDBYearList;
	private static JMenuItem viewDBMI, sortWishesMI, sortFamiliesMI, sortOrgsMI, recGiftsMI, sortMealsMI;
	private static JMenuItem agentMI, orgMI, catMI;
	private static JMenuItem aboutONCMI, oncPrefrencesMI, profileMI, userMI, onlineMI, chatMI, changePWMI, stopPollingMI;
	private static JMenuItem showServerLogMI, showServerClientIDMI, showCurrDirMI, showWebsiteStatusMI;
	private List<JMenuItem> dbYearsMIList;
	
	private DialogManager dlgManager;
	private DatabaseManager dbManager;
	
	private Families familyDB;
	
	public ONCMenuBar()
	{
		//get reference to onc data base
		oncDB = DatabaseManager.getInstance();
		if(oncDB != null)
			oncDB.addDatabaseListener(this);
		
		familyDB = Families.getInstance();
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
	
	/******************************************************************************************
     * Adds a DBYear to the Database menu SelectYear submenu
     ****************************************************************************************/
    void addDBYearToSubmenu(DBYear dbYear)
    {	
    	String zYear = Integer.toString(dbYear.getYear());
    	
//		JMenuItem mi = addDBYear(zYear, GlobalVariables.getInstance().getImageIcon(dbYear.isLocked() ? 
//				DB_LOCKED_IMAGE_INDEX : DB_UNLOCKED_IMAGE_INDEX));
		
		JMenuItem mi = new JMenuItem(zYear, GlobalVariables.getInstance().getImageIcon(dbYear.isLocked() ? 
				DB_LOCKED_IMAGE_INDEX : DB_UNLOCKED_IMAGE_INDEX));
		mi.setActionCommand(zYear);
			
		dbYearsMIList.add(mi);
		submenuDBYearList.add(mi);
		
		mi.addActionListener(new MenuItemDBYearsListener());
    }
/*	
	JMenuItem addDBYear(String year, ImageIcon lockIcon)
	{
		JMenuItem mi = new JMenuItem(year, lockIcon);
		mi.setActionCommand(year);
			
		dbYears.add(mi);
		submenuDatabase.add(mi);
		
		return mi;
	}
*/	
	void processDBYears(List<DBYear> dbYears)
    {
    	//clear the current list
		submenuDBYearList.removeAll();
//		dbYears.clear(); 
    	
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
	static void setEnabledMarkorDeleteChildMenuItem(boolean tf)
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
		if(e.getSource() == ONCMenuBar.newMI) { dbManager.addONCSeason(); }
//		else if(e.getSource() == ONCMenuBar.importONCMI) {OnImportMenuItemClicked("ONC");}
		else if(e.getSource() == ONCMenuBar.importONCMI) { }
		else if(e.getSource() == ONCMenuBar.importWishCatMI)
		{
			ONCWishCatalog cat = ONCWishCatalog.getInstance();
			cat.importWishCatalog(GlobalVariables.getFrame(), GlobalVariables.getONCLogo(), null);
		}
		else if(e.getSource() == ONCMenuBar.importPYMI)
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
		else if(e.getSource() == ONCMenuBar.importPYORGMI)
		{
			ONCOrgs partnerDB = ONCOrgs.getInstance();
			partnerDB.importOrgDB(GlobalVariables.getFrame(), GlobalVariables.getONCLogo(), null);
		}
		else if(e.getSource() == ONCMenuBar.importODBMI) {dbManager.onImportMenuItemClicked("ODB");}
		else if(e.getSource() == ONCMenuBar.importWFCMMI) {dbManager.onImportMenuItemClicked("WFCM");}
		else if(e.getSource() == ONCMenuBar.importRAFMI) { dlgManager.onImportRAFMenuItemClicked(); }
		else if(e.getSource() == ONCMenuBar.manageCallResultMI) {dlgManager.showAngelCallDialog();}
		else if(e.getSource() == ONCMenuBar.exportMI){ dbManager.exportObjectDBToCSV(); }
		else if(e.getSource() == ONCMenuBar.dbStatusMI) {dlgManager.onDBStatusClicked();}
		else if(e.getSource() == ONCMenuBar.clearMI) {dlgManager.onClearMenuItemClicked();} 			       	
//		else if(e.getSource() == ONCMenuBar.exitMI)	{exit("LOGOUT");}
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
			DriverDB driverDB = DriverDB.getInstance();
			String mssg = driverDB.importDrivers(GlobalVariables.getFrame(), 
									GlobalVariables.getInstance().getTodaysDate(),
									GlobalVariables.getUserLNFI(), GlobalVariables.getONCLogo());
			
//			oncFamilyPanel.refreshDriverDisplays();	//Update dialog based on imported info
			
			//Information message that the drivr import completed successfully
		    JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg,
					"Import Result", JOptionPane.INFORMATION_MESSAGE, GlobalVariables.getONCLogo());
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
			dlgManager.showAboutONCDialog();
		else if(e.getSource() == ONCMenuBar.oncPrefrencesMI)
			dlgManager.showPreferencesDialog();
		else if(e.getSource() == ONCMenuBar.newFamMI)
		{
			AddFamilyDialog afDlg = new AddFamilyDialog(GlobalVariables.getFrame());
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
		else if(e.getSource() == ONCMenuBar.stopPollingMI)
		{
			ServerIF serverIF = ServerIF.getInstance();
			if(serverIF != null)
				serverIF.setEnabledServerPolling(false); 
		}
		else if(e.getSource() == ONCMenuBar.showServerLogMI)
		{
			ServerIF serverIF = ServerIF.getInstance();
			if(serverIF != null)
			{
				LogDialog logDlg = new LogDialog();
				logDlg.setLocationRelativeTo(GlobalVariables.getFrame());
				logDlg.setVisible(true);
			}
		}
		else if(e.getSource() == ONCMenuBar.showServerClientIDMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariables.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			String mssg = String.format("Your ONC Server Client ID is: %d", 
								GlobalVariables.getInstance().getUser().getClientID());
			clientIDPU.show("ONC Server Client ID", mssg);
		}    		
		else if(e.getSource() == ONCMenuBar.showCurrDirMI)
		{
			ONCPopupMessage clientIDPU = new ONCPopupMessage(GlobalVariables.getONCLogo());
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			String mssg = String.format("Current folder is: %s", System.getProperty("user.dir"));
			clientIDPU.show("ONC Client Current Folder", mssg);
		}
		else if(e.getSource() == ONCMenuBar.showWebsiteStatusMI) { dlgManager.onWebsiteStatus(); }
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
