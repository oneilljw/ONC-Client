package OurNeighborsChild;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class ONCMenuBar extends JMenuBar
{
	/**
	 * This class provides the blueprint for the menu bar in the ONC application
	 */
	private static final long serialVersionUID = 1L;
	public static JMenuItem newMI;
	public static JMenuItem importODBMI, importWFCMMI, importRAFMI;
	public static JMenuItem importONCMI, importPYMI, importPYORGMI,importWishCatMI, importCallResultMI;
	public static JMenuItem exportMI, clearMI, exitMI;
	public static JMenuItem font8, font10, font12, font13, font14, font16, font18;
	public static JMenuItem compODBtoONCfamMI, compODBtoONCdataMI, compWFCMtoONCfamMI, compWFCMtoONCdataMI;
	public static JMenuItem findDupFamsMI, findDupChldrnMI;
	public static JMenuItem assignDelMI, manageDelMI, importDrvrMI, mapsMI, delstatusMI, distMI;
	public static JMenuItem newFamMI, changeONCMI, delFamMI, newChildMI, delChildMI, corrPhoneMI;
	public static JMenu submenuImport, submenuodbwlFont, submenuCompareRecords;
	public static JMenu submenuExport, submenuCompareData, submenuDatabase;
	public static JMenuItem viewDBMI, sortWishesMI, sortFamiliesMI, sortOrgsMI, recGiftsMI, agentMI, orgMI, catMI;
	public static JMenuItem aboutONCMI, oncPrefrencesMI, oncAddUserMI, onlineMI, chatMI, changePWMI, stopPollingMI;
	public static JMenuItem showServerLogMI, showServerClientIDMI, showCurrDirMI;
	public List<JMenuItem> dbYears;
	
	public ONCMenuBar()
	{
		JMenu menuFile, menuView, menuFamilies, menuWishes, menuDataChecks, menuDelivery, menuSettings;	    
        
	    //Build the first menu.
	    menuFile = new JMenu("Data");
	    this.add(menuFile);
	 
	   //a group of JMenuItems for the File Menu
	    newMI = new JMenuItem("New Season");
	    newMI.setEnabled(false);
//	    menuFile.add(newMI);	//Taken out temporairly until new season functionality fixed - Oct 2014
	    
//	    menuFile.addSeparator();
	    
	    submenuDatabase = new JMenu("Year");
	    submenuDatabase.setEnabled(false);   
	    dbYears = new ArrayList<JMenuItem>();
	    menuFile.add(submenuDatabase);

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
	    importCallResultMI = new JMenuItem("Angel Call Results...");
	    importCallResultMI.setEnabled(false);
	    submenuImport.add(importCallResultMI);
	    
	    //Import Delivery Partners
	    importDrvrMI = new JMenuItem("Delivery Partners...");	
	    submenuImport.add(importDrvrMI);
	    
	    //Import Referring Agent Families
	    importRAFMI = new JMenuItem("RA Families...");	
	    submenuImport.add(importRAFMI);
    
	    menuFile.add(submenuImport);
	    
	    submenuExport = new JMenu("Export");
	    submenuExport.setEnabled(false);
	    	    
	    exportMI = new JMenuItem("Export...");
	    submenuExport.add(exportMI);
	    
	    menuFile.add(submenuExport);
	   
	    menuFile.addSeparator();
	    
	    clearMI = new JMenuItem("Clear");
	    clearMI.setEnabled(false);
	    menuFile.add(clearMI);
	    
	    exitMI = new JMenuItem("Log Out");
	    menuFile.add(exitMI);
	    	    
	    //Build the view menu.
	    menuView = new JMenu("View");
	    this.add(menuView);
	    
	    submenuodbwlFont = new JMenu("Select Font Size");
	    
	    font8 = new JMenuItem("8 pt.");
	    submenuodbwlFont.add(font8);
	    
	    font10 = new JMenuItem("10 pt.");
	    submenuodbwlFont.add(font10);
	    
	    font12 = new JMenuItem("12 pt.");
	    submenuodbwlFont.add(font12);
	    
	    font13 = new JMenuItem("13 pt.");
	    submenuodbwlFont.add(font13);
	    
	    font14 = new JMenuItem("14 pt.");
	    submenuodbwlFont.add(font14);
	    
	    font16 = new JMenuItem("16 pt.");
	    submenuodbwlFont.add(font16);
	    
	    font18 = new JMenuItem("18 pt.");
	    submenuodbwlFont.add(font18);
	    
	    menuView.add(submenuodbwlFont);
	    
	    menuView.addSeparator();
	    
	    viewDBMI = new JMenuItem("View Family Data");
	    viewDBMI.setEnabled(false);
	    menuView.add(viewDBMI);
	    
	    //Build Families Menu Structure
	    menuFamilies = new JMenu("Families");
	    this.add(menuFamilies);
	    
	    sortFamiliesMI = new JMenuItem("Manage Families");
	    sortFamiliesMI.setEnabled(false);	    
	    menuFamilies.add(sortFamiliesMI);
	    
	    agentMI = new JMenuItem("Manage Agents");
	    agentMI.setEnabled(false);	    
	    menuFamilies.add(agentMI);
	    
	    menuFamilies.addSeparator();
	    	    
	    changeONCMI = new JMenuItem("Change ONC #");
	    changeONCMI.setEnabled(false);
	    menuFamilies.add(changeONCMI);
	    
	    menuFamilies.addSeparator();
	    
	    newFamMI = new JMenuItem("New Family");
	    newFamMI.setEnabled(false);
	    menuFamilies.add(newFamMI);
	    
	    delFamMI = new JMenuItem("Delete Family");
	    delFamMI.setEnabled(false);
	    menuFamilies.add(delFamMI);
	    
	    menuFamilies.addSeparator();
	    
	    newChildMI = new JMenuItem("Add New Child");
	    newChildMI.setEnabled(false);
	    menuFamilies.add(newChildMI);
	    
	    delChildMI = new JMenuItem("Delete Child");
	    delChildMI.setEnabled(false);
	    menuFamilies.add(delChildMI);
	    
	    corrPhoneMI = new JMenuItem("Correct Family Phone Data");
	    corrPhoneMI.setEnabled(false);
	    menuFamilies.add(corrPhoneMI);
	    
	    //Build Wishes Menu Structure
	    menuWishes = new JMenu("Wishes");
	    this.add(menuWishes);
	    
	    catMI = new JMenuItem("Manage Catalog");
	    catMI.setEnabled(false);
	    menuWishes.add(catMI);
	    
	    sortWishesMI = new JMenuItem("Manage Wishes");
	    sortWishesMI.setEnabled(false);
	    menuWishes.add(sortWishesMI);
	    
	    menuWishes.addSeparator();
	    
	    recGiftsMI = new JMenuItem("Receive Gifts");
	    recGiftsMI.setEnabled(false);
	    menuWishes.add(recGiftsMI);
	    
	    menuWishes.addSeparator();
	    
	    orgMI = new JMenuItem("Edit Gift Partners");
	    orgMI.setEnabled(false);
	    menuWishes.add(orgMI);
	    
	    sortOrgsMI = new JMenuItem("Manage Gift Partners");
	    sortOrgsMI.setEnabled(false);
	    menuWishes.add(sortOrgsMI);
	   
	    //Build Delivery Menu
	    menuDelivery = new JMenu("Delivery");
	    menuDelivery.setEnabled(true);
	    this.add(menuDelivery);
	    
	    //Delivery Status check
	    delstatusMI = new JMenuItem("Delivery Status");	
	    delstatusMI.setEnabled(false);
	    menuDelivery.add(delstatusMI);
	    
	    //Manage Delivery Partners
	    manageDelMI = new JMenuItem("Edit Delivery Partners");	
	    menuDelivery.add(manageDelMI);
	    
	    //Assign Delivery Partners
	    assignDelMI = new JMenuItem("Assign Deliveries");	
	    assignDelMI.setEnabled(false);
	    menuDelivery.add(assignDelMI);   
	    
	    mapsMI = new JMenuItem("Delivery Directions");	
	    mapsMI.setEnabled(false);
	    menuDelivery.add(mapsMI);
	    
	    //Delivery Map & Directions
	    distMI = new JMenuItem("Delivery Distribution");	
	    distMI.setEnabled(false);
	    menuDelivery.add(distMI);
	    
	    //Build Data Checks Menu Structure
	    menuDataChecks = new JMenu("Data Checks");
	    this.add(menuDataChecks);
	    
	    findDupFamsMI = new JMenuItem("Duplicate Family Check");
	    findDupFamsMI.setEnabled(false);
	    menuDataChecks.add(findDupFamsMI);
	    
	    findDupChldrnMI = new JMenuItem("Duplicate Children Check");
	    findDupChldrnMI.setEnabled(false);
	    menuDataChecks.add(findDupChldrnMI);
	    
	    menuDataChecks.addSeparator();
	           
	    submenuCompareRecords = new JMenu("Compare Family Record Count:");
	    submenuCompareRecords.setEnabled(false);
	    
	    compODBtoONCfamMI = new JMenuItem("Against ODB File");
//	    compODBtoONCfamMI.setEnabled(false);
	    submenuCompareRecords.add(compODBtoONCfamMI);
	    
	    compWFCMtoONCfamMI = new JMenuItem("Against WFCM File");
//	    compWFCMtoONCfamMI.setEnabled(false);
	    submenuCompareRecords.add(compWFCMtoONCfamMI);
	    
	    menuDataChecks.add(submenuCompareRecords);
	    
	    submenuCompareData = new JMenu("Compare Family Data:");
	    submenuCompareData.setEnabled(false);
	    
	    compODBtoONCdataMI = new JMenuItem("Against ODB file");
//	    compODBtoONCdataMI.setEnabled(false);
	    submenuCompareData.add(compODBtoONCdataMI);
	    
	    compWFCMtoONCdataMI = new JMenuItem("Against WFCM file");
//	    compWFCMtoONCdataMI.setEnabled(false);
	    submenuCompareData.add(compWFCMtoONCdataMI);
	    
	    menuDataChecks.add(submenuCompareData);
	    	    
	    //Build About Menu
	    menuSettings = new JMenu("Tools/Settings");
	    this.add(menuSettings);
	    
	    aboutONCMI = new JMenuItem("About ONC");
	    menuSettings.add(aboutONCMI);
	    
	    oncPrefrencesMI = new JMenuItem("Preferences");
	    menuSettings.add(oncPrefrencesMI);
	    
	    oncAddUserMI = new JMenuItem("Add User");
	    oncAddUserMI.setEnabled(false);
	    menuSettings.add(oncAddUserMI);
	    
	    onlineMI = new JMenuItem("Who's Online?");
	    menuSettings.add(onlineMI);
	    
	    chatMI = new JMenuItem("Private Chat");
	    menuSettings.add(chatMI);
	    
	    changePWMI = new JMenuItem("Change Password");
	    menuSettings.add(changePWMI);
	    
	    stopPollingMI = new JMenuItem("Stop Server Polling");
	    stopPollingMI.setVisible(false);
	    menuSettings.add(stopPollingMI);
	    
	    showServerLogMI = new JMenuItem("Server Log");
	    menuSettings.add(showServerLogMI);
	    
	    showServerClientIDMI = new JMenuItem("Server Client ID");
	    menuSettings.add(showServerClientIDMI);
	    
	    showCurrDirMI = new JMenuItem("Current Directory");
	    menuSettings.add(showCurrDirMI);
	}
	
	JMenuItem addDBYear(String year, ImageIcon lock)
	{
		JMenuItem mi = new JMenuItem(year, lock);
		mi.setActionCommand(year);
		dbYears.add(mi);
		submenuDatabase.add(dbYears.get(dbYears.size() - 1));
		
		return mi;
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
	
	void SetEnabledRestrictedMenuItems(boolean tf)	//Only Admins can perform these functions
	{												//when at least one family is present in db
//		submenuCompareRecords.setEnabled(tf);		//Disabled until capability reimplemented
//		submenuCompareData.setEnabled(tf);			//Disabled until capability reimplemented
		findDupFamsMI.setEnabled(tf);
		findDupChldrnMI.setEnabled(tf);
		viewDBMI.setEnabled(tf);
		submenuExport.setEnabled(tf);
		changeONCMI.setEnabled(tf);
		sortFamiliesMI.setEnabled(tf);
		agentMI.setEnabled(tf);
		delChildMI.setEnabled(tf);
		newChildMI.setEnabled(tf);
		importCallResultMI.setEnabled(tf);
		oncAddUserMI.setEnabled(tf);
	}
	
	void setEnabledWishCatalogAndOrgMenuItems(boolean tf)
	{
		catMI.setEnabled(tf);	//Once new season created can manage wishes and partners
		orgMI.setEnabled(tf);	//Or when a file is opened
		sortOrgsMI.setEnabled(tf);
	}	
	
	void setVisibleSpecialImports(boolean tf)	//Only Superuser can perform these functions
    {
    	importONCMI.setVisible(tf);	//Only with no data loaded
    	importWishCatMI.setVisible(tf);
    	importPYMI.setVisible(tf);	//Only with no prior year data loaded
    	importPYORGMI.setVisible(tf);
    	submenuImport.setEnabled(true);
    	
    	stopPollingMI.setVisible(true);
    	
    	corrPhoneMI.setEnabled(tf);
    }
	
	void setEnabledServerConnected(boolean tf)
	{
		submenuDatabase.setEnabled(tf);
	}
	
	void setEnabledYear(boolean tf) {submenuDatabase.setEnabled(tf);}
	void setEnabledNewMenuItem(boolean tf) { newMI.setEnabled(tf); }
	void setEnabledPriorYearSpecialImport(boolean tf) { importPYMI.setEnabled(tf); }
	void setEnabledImportMenuItems(boolean tf) { submenuImport.setEnabled(tf); }
	void setEnabledDeleteChildMenuItem(boolean tf) { delChildMI.setEnabled(tf); }
	void setEnabledAgentMenuItem(boolean tf) { agentMI.setEnabled(tf); }	
}
