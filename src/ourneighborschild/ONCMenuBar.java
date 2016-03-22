package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ONCMenuBar extends JMenuBar implements DatabaseListener
{
	/**
	 * This class provides the blueprint for the menu bar in the ONC application
	 */
	private static final long serialVersionUID = 1L;
	
	public DBStatusDB oncDB;
	public static JMenuItem newMI;
	public static JMenuItem importODBMI, importWFCMMI, importRAFMI;
	public static JMenuItem importONCMI, importPYMI, importPYORGMI,importWishCatMI, manageCallResultMI;
	public static JMenuItem exportMI, dbStatusMI, clearMI, exitMI;
//	public static JMenuItem font8, font10, font12, font13, font14, font16, font18;
//	public static JMenuItem compODBtoONCfamMI, compODBtoONCdataMI, compWFCMtoONCfamMI, compWFCMtoONCdataMI;
	public static JMenuItem findDupFamsMI, findDupChldrnMI;
	public static JMenuItem assignDelMI, editDelMI, manageDelMI, importDrvrMI, mapsMI, delstatusMI, distMI;
	public static JMenuItem newFamMI, changeONCMI, changeRefMI, changeBatchMI, newChildMI, delChildMI, markAdultMI, connectChildMI;
	public static JMenu submenuImport, submenuFamilyDataChecks;
	public static JMenu submenuExport, submenuChangeFamilyNumbers, submenuCompareData, submenuDatabase;
	public static JMenuItem viewDBMI, sortWishesMI, sortFamiliesMI, sortOrgsMI, recGiftsMI, sortMealsMI;
	public static JMenuItem agentMI, orgMI, catMI;
	public static JMenuItem aboutONCMI, oncPrefrencesMI, profileMI, userMI, onlineMI, chatMI, changePWMI, stopPollingMI;
	public static JMenuItem showServerLogMI, showServerClientIDMI, showCurrDirMI, showWebsiteStatusMI;
	public List<JMenuItem> dbYears;
	
	public ONCMenuBar()
	{
		//get reference to onc data base
		oncDB = DBStatusDB.getInstance();
		if(oncDB != null)
			oncDB.addDatabaseListener(this);
		
		JMenu menuFile, menuAgents, menuFamilies, menuWishes, menuMeals, menuPartners, menuDelivery, menuSettings;	    
        
	    //Build the Database menu.
	    menuFile = new JMenu("Database");
	    this.add(menuFile);
	 
	    submenuDatabase = new JMenu("Select Year");
	    submenuDatabase.setEnabled(false);   
	    dbYears = new ArrayList<JMenuItem>();
	    menuFile.add(submenuDatabase);
	    
	    newMI = new JMenuItem("Add Year");
	    newMI.setEnabled(false);
	    newMI.setVisible(false);
	    menuFile.add(newMI);
	    
	    dbStatusMI = new JMenuItem("Lock/Unlock Year");
	    dbStatusMI.setVisible(false);
	    menuFile.add(dbStatusMI);

	    menuFile.addSeparator();
	    
	    submenuImport = new JMenu("Import");
	    submenuImport.setEnabled(false);
	       
	    importONCMI = new JMenuItem("Curent year ONC .csv file");
	    importONCMI.setVisible(false);
	    submenuImport.add(importONCMI);
	    
	    importPYMI = new JMenuItem("Prior year ONC .csv file");
	    importPYMI.setVisible(false);
	    submenuImport.add(importPYMI);
	    
	    importPYORGMI = new JMenuItem("ONC Organizations .csv file");
	    importPYORGMI.setVisible(false);
	    submenuImport.add(importPYORGMI);
	    
	    importWishCatMI = new JMenuItem("Wish Catalog .csv file");
	    importWishCatMI.setVisible(false);
	    submenuImport.add(importWishCatMI);
	  	    
	    importODBMI = new JMenuItem("From ODB...");
	    submenuImport.add(importODBMI);
	    
	    importWFCMMI = new JMenuItem("From WFCM...");
	    submenuImport.add(importWFCMMI);
	    
	    //Import Angel Call Results
//	    manageCallResultMI = new JMenuItem("Angel Call Results...");
//	    manageCallResultMI.setEnabled(false);
//	    submenuImport.add(manageCallResultMI);
	    
	    //Import Delivery Partners
	    importDrvrMI = new JMenuItem("Delivery Partners...");	
	    submenuImport.add(importDrvrMI);
	    
	    //Import Referring Agent Families
	    importRAFMI = new JMenuItem("RA Families...");	
	    submenuImport.add(importRAFMI);
    
	    menuFile.add(submenuImport);
	    
	    submenuExport = new JMenu("Export");
	    submenuExport.setEnabled(false);
	    	    
	    exportMI = new JMenuItem("Database to .csv files");
	    submenuExport.add(exportMI);
	    
	    menuFile.add(submenuExport);
	   
	    menuFile.addSeparator();
	    
	    clearMI = new JMenuItem("Clear");
	    clearMI.setEnabled(false);
	    menuFile.add(clearMI);
	    
	    exitMI = new JMenuItem("Log Out");
	    menuFile.add(exitMI);
	    	    
	    //Build the Agents menu.
	    menuAgents = new JMenu("Agents");
	    
	    agentMI = new JMenuItem("Manage Agents");
	    agentMI.setActionCommand("Agents");
	    agentMI.setEnabled(false);	    
	    menuAgents.add(agentMI);
	    
	    this.add(menuAgents);

	    //Build Families Menu Structure
	    menuFamilies = new JMenu("Families");
	    this.add(menuFamilies);
	    
	    sortFamiliesMI = new JMenuItem("Manage Families");
	    sortFamiliesMI.setActionCommand("Families");
	    sortFamiliesMI.setEnabled(false);	    
	    menuFamilies.add(sortFamiliesMI);
	    
	    submenuChangeFamilyNumbers = new JMenu("Change Numbers");
	    submenuChangeFamilyNumbers.setEnabled(false);   
	    menuFamilies.add(submenuChangeFamilyNumbers);
	       
	    changeONCMI = new JMenuItem("Change ONC #");
	    changeONCMI.setActionCommand("Change ONC #");
	    submenuChangeFamilyNumbers.add(changeONCMI);
	    
	    changeRefMI = new JMenuItem("Change Ref #");
	    changeRefMI.setActionCommand("Change Ref #");
	    submenuChangeFamilyNumbers.add(changeRefMI);
	    
	    changeBatchMI = new JMenuItem("Change Batch #");
	    changeBatchMI.setActionCommand("Change Batch #");
	    submenuChangeFamilyNumbers.add(changeBatchMI);
	    
	    menuFamilies.addSeparator();
	    
	    //Manage Angel Call Results
	    manageCallResultMI = new JMenuItem("Manage Call Results");
	    manageCallResultMI.setEnabled(false);
	    menuFamilies.add(manageCallResultMI);
	    
	    menuFamilies.addSeparator();
	    
	    newFamMI = new JMenuItem("Add New Family");
//	    newFamMI.setEnabled(false);
	    menuFamilies.add(newFamMI);
	    
	    menuFamilies.addSeparator();
	    
	    newChildMI = new JMenuItem("Add New Child");
	    newChildMI.setEnabled(false);
	    menuFamilies.add(newChildMI);
	    
	    markAdultMI = new JMenuItem("Mark Child as Adult");
	    markAdultMI.setEnabled(false);
	    menuFamilies.add(markAdultMI);
	    
	    delChildMI = new JMenuItem("Delete Child");
	    delChildMI.setEnabled(false);
	    menuFamilies.add(delChildMI);
	    
	    connectChildMI = new JMenuItem("Connect PY Child");
	    connectChildMI.setEnabled(false);
	    menuFamilies.add(connectChildMI);
	    
	    menuFamilies.addSeparator();
	    
	    //Build Family Data Checks Sub menu Structure
	    submenuFamilyDataChecks = new JMenu("Data Checks");
	    
	    findDupFamsMI = new JMenuItem("Duplicate Family Check");
	    findDupFamsMI.setActionCommand("Duplicate Family Check");
	    findDupFamsMI.setEnabled(false);
	    submenuFamilyDataChecks.add(findDupFamsMI);
	    
	    findDupChldrnMI = new JMenuItem("Duplicate Children Check");
	    findDupChldrnMI.setActionCommand("Duplicate Children Check");
	    findDupChldrnMI.setEnabled(false);
	    submenuFamilyDataChecks.add(findDupChldrnMI);
	    
	    menuFamilies.add(submenuFamilyDataChecks);
	    
	    menuFamilies.addSeparator();
	    
	    viewDBMI = new JMenuItem("View All Family Data");
	    viewDBMI.setEnabled(false);
	    menuFamilies.add(viewDBMI);
	    
	    this.add(menuFamilies);
	    
	    //Build Wishes Menu Structure
	    menuWishes = new JMenu("Wishes");
	    this.add(menuWishes);
	    
	    catMI = new JMenuItem("Manage Catalog");
	    catMI.setEnabled(false);
	    menuWishes.add(catMI);
	    
	    sortWishesMI = new JMenuItem("Manage Wishes");
	    sortWishesMI.setActionCommand("Wishes");
	    sortWishesMI.setEnabled(false);
	    menuWishes.add(sortWishesMI);
	    
	    menuWishes.addSeparator();
	    
	    recGiftsMI = new JMenuItem("Receive Gifts");
	    recGiftsMI.setActionCommand("Receive Gifts");
	    recGiftsMI.setEnabled(false);
	    menuWishes.add(recGiftsMI);
	    
	    //Build Meals Menu Structure
	    menuMeals = new JMenu("Meals");
	    this.add(menuMeals);
	    
	    sortMealsMI = new JMenuItem("Manage Meals");
	    sortMealsMI.setActionCommand("Meals");
	    sortMealsMI.setEnabled(false);
	    menuMeals.add(sortMealsMI);
	    
	    //Build Partners Menu
	    menuPartners = new JMenu("Partners");
	    
	    orgMI = new JMenuItem("Edit Partners");
	    orgMI.setActionCommand("Edit Partners");
	    orgMI.setEnabled(false);
	    menuPartners.add(orgMI);
	    
	    sortOrgsMI = new JMenuItem("Manage Partners");
	    sortOrgsMI.setActionCommand("Partners");
	    sortOrgsMI.setEnabled(false);
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
	    menuDelivery.add(delstatusMI);
	    
	    //Edit Delivery Partners
	    editDelMI = new JMenuItem("Edit Delivery Partners");
	    editDelMI.setActionCommand("Edit Delivery Partners");
	    menuDelivery.add(editDelMI);
	    
	    //Edit Delivery Partners
	    manageDelMI = new JMenuItem("Manage Delivery Partners");
	    manageDelMI.setActionCommand("Drivers");
	    manageDelMI.setEnabled(false);
	    menuDelivery.add(manageDelMI);
	    
	    //Assign Delivery Partners
	    assignDelMI = new JMenuItem("Assign Deliveries");
	    assignDelMI.setActionCommand("Deliveries");
	    assignDelMI.setEnabled(false);
	    menuDelivery.add(assignDelMI);   
	    
	    mapsMI = new JMenuItem("Delivery Directions");	
	    mapsMI.setEnabled(false);
	    menuDelivery.add(mapsMI);
	    
	    //Delivery Map & Directions
	    distMI = new JMenuItem("Delivery Distribution");	
	    distMI.setEnabled(false);
	    menuDelivery.add(distMI);
	    	    
	    //Build About Menu
	    menuSettings = new JMenu("Tools/Settings");
	    this.add(menuSettings);
	    
	    aboutONCMI = new JMenuItem("About ONC");
	    menuSettings.add(aboutONCMI);
	    
	    profileMI = new JMenuItem("Edit Profile");
	    menuSettings.add(profileMI);
	    
	    changePWMI = new JMenuItem("Change Password");
	    menuSettings.add(changePWMI);
	    
	    oncPrefrencesMI = new JMenuItem("Preferences");
	    menuSettings.add(oncPrefrencesMI);
	    
	    userMI = new JMenuItem("Manage Users");
	    userMI.setVisible(false);
	    menuSettings.add(userMI);
	    
	    onlineMI = new JMenuItem("Who's Online?");
	    menuSettings.add(onlineMI);
	    
	    chatMI = new JMenuItem("Private Chat");
	    menuSettings.add(chatMI);
	      
	    stopPollingMI = new JMenuItem("Stop Server Polling");
	    stopPollingMI.setVisible(false);
	    menuSettings.add(stopPollingMI);
	    
	    showServerLogMI = new JMenuItem("Message Log");
	    menuSettings.add(showServerLogMI);
	    
	    showServerClientIDMI = new JMenuItem("Server Client ID");
	    menuSettings.add(showServerClientIDMI);
	    
	    showCurrDirMI = new JMenuItem("Current Directory");
	    menuSettings.add(showCurrDirMI);
	    
	    showWebsiteStatusMI = new JMenuItem("Website Status");
	    showWebsiteStatusMI.setVisible(false);
	    menuSettings.add(showWebsiteStatusMI);
	}
	
	JMenuItem addDBYear(String year, ImageIcon lockIcon)
	{
		JMenuItem mi = new JMenuItem(year, lockIcon);
		mi.setActionCommand(year);
			
		dbYears.add(mi);
		submenuDatabase.add(mi);
		
		return mi;
	}

	
	void clearDataBaseYears()
	{ 
		submenuDatabase.removeAll();
		dbYears.clear(); 
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
		submenuDatabase.setEnabled(tf);
	}
	
	void setEnabledYear(boolean tf) {submenuDatabase.setEnabled(tf);}
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
			while(index<dbYears.size() &&
					!dbYears.get(index).getActionCommand().equals(updatedDBYear.toString()))
				index++;
			
			if(index < dbYears.size())
				dbYears.get(index).setIcon(updatedDBYear.isLocked() ? 
						GlobalVariables.getLockedIcon() : GlobalVariables.getUnLockedIcon());
		}
	}
}
