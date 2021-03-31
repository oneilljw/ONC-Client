package ourneighborschild;

import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DatabaseManager extends ServerListenerComponent implements ServerListener
{
	/***
	 * This singleton class implements a manager for all component databases. The manager
	 * initializes each component data base and loads them from the server. It uses a Swing
	 * Worker to load the component data bases in a background thread. In addition, it 
	 * can export the local component data bases to .csv files. 
	 * 
	 * The method also manages retrieving data base status from the server and adding a new
	 * season to the server data base.
	 */
	private static DatabaseManager instance = null;
	
	private List<ONCDatabase> earlyImportDBList;	//db's that need to be imported before season is selected
	private List<ONCDatabase> importDBList;			//db's that are imported when user selects season
	
	//Local Data Base Structures
	private GlobalVariablesDB oncGVs;		//Holds the Global Variables
	private FamilyDB familyDB;				//Holds family Database
	private GiftCatalogDB giftCatalog;		//Holds gift Catalog
	
	//progress bar for database loading progress
	private ONCProgressNavPanel progressNavPanel;
	
	//gui
//	JDialog pbDlg;
/*	
	private UserDB userDB;					//Holds the UserDB
	private ChildDB childDB;				//Holds child database
	private ChildGiftDB childGiftDB; 		//Holds child gift database
	private ClonedGiftDB clonedGiftDB; 		//Holds cloned gift database
	private GroupDB groupDB;				//Holds group database
	private PartnerDB partnerDB;			//Holds partner Organization
	private GiftDetailDB giftDetailDB;		//Holds gift detail database
	private ActivityDB activityDB;			//Holds activity Data Base
	private VolunteerDB volunteerDB;		//Holds volunteer Data Base
	private VolunteerActivityDB volActDB;	//Holds volunteer ActivityData Base
	private FamilyHistoryDB famHistoryDB;	//Holds family history Data Base
	private RegionDB regionDB;				//Holds the region and school databases
	private AdultDB oncAdultDB;				//Holds adult database
	private NoteDB noteDB;					//Holds note database
	private DNSCodeDB dnsCodeDB;			//Holds DNS Code database
	private MealDB mealDB;					//Holds meal database
	private BatteryDB batteryDB;			//Holds battery database
	private InventoryDB inventoryDB;		//Holds current inventory database
	private SMSDB smsDB;					//Holds SMS messages
*/	
	private DatabaseManager()
	{
		super();
		
		progressNavPanel = null;
		
		if(serverIF != null)
			serverIF.addServerListener(this);
		
		earlyImportDBList = new ArrayList<ONCDatabase>();
		importDBList = new ArrayList<ONCDatabase>();
		
		//initialize the database components that are imported when client initializes
		earlyImportDBList.add(UserDB.getInstance());
		earlyImportDBList.add(GroupDB.getInstance());
		
		//initialize global variables such as season dates, warehouse address, etc.
		oncGVs = GlobalVariablesDB.getInstance();
		
		//initialize the component data bases
		importDBList.add(RegionDB.getInstance());
		importDBList.add(PartnerDB.getInstance());
		importDBList.add(GiftDetailDB.getInstance());
		importDBList.add((giftCatalog = GiftCatalogDB.getInstance()));
		importDBList.add(ActivityDB.getInstance());
		importDBList.add(VolunteerDB.getInstance());
		importDBList.add(VolunteerActivityDB.getInstance());
		importDBList.add(FamilyHistoryDB.getInstance());
		importDBList.add(ChildDB.getInstance());
		importDBList.add(ChildGiftDB.getInstance());
		importDBList.add(ClonedGiftDB.getInstance());
		importDBList.add(AdultDB.getInstance());
		importDBList.add(NoteDB.getInstance());
		importDBList.add(DNSCodeDB.getInstance());
		importDBList.add(MealDB.getInstance());
		importDBList.add(InventoryDB.getInstance());
		importDBList.add(SMSDB.getInstance());
		importDBList.add(BatteryDB.getInstance());
		importDBList.add((familyDB = FamilyDB.getInstance()));
				
//		oncGVs = GlobalVariablesDB.getInstance();
//		regionDB = RegionDB.getInstance();
//		userDB = UserDB.getInstance();
//		groupDB = GroupDB.getInstance();
//		partnerDB = PartnerDB.getInstance();
//		giftDetailDB = GiftDetailDB.getInstance();
//		giftCatalog = GiftCatalogDB.getInstance();
//		activityDB = ActivityDB.getInstance();
//		volunteerDB = VolunteerDB.getInstance();
//		volActDB = VolunteerActivityDB.getInstance();
//		famHistoryDB = FamilyHistoryDB.getInstance();
//		childDB = ChildDB.getInstance();
//		childGiftDB = ChildGiftDB.getInstance();
//		clonedGiftDB = ClonedGiftDB.getInstance();
//		oncAdultDB = AdultDB.getInstance();
//		noteDB = NoteDB.getInstance();
//		dnsCodeDB = DNSCodeDB.getInstance();
//		mealDB = MealDB.getInstance();
//		inventoryDB = InventoryDB.getInstance();
//		smsDB = SMSDB.getInstance();
//		batteryDB = BatteryDB.getInstance();
//		familyDB = FamilyDB.getInstance();
	}
	
	public static DatabaseManager getInstance()
	{
		if(instance == null)
			instance = new DatabaseManager();
		
		return instance;
	}
	
	void setProgressPanel(ONCProgressNavPanel pp) { this.progressNavPanel = pp; }
	
	void importObjectsFromDB(int year)
	{
		//create the swing worker background task to get the data from the server
		ONCServerDBImporter databaseImporter = new ONCServerDBImporter(year);
		
		//Enable the Progress bar
		if(progressNavPanel != null)
		{
			progressNavPanel.setVisibleProgressBar(true);
			databaseImporter.addPropertyChangeListener(progressNavPanel);
		}

		//execute the background swing worker task
		databaseImporter.execute();
	}
	
	List<DBYear> getDBStatus()
	{
		List<DBYear> dbYearList = null;
		
		//notify the server
		Gson gson = new Gson();
		Type listOfDBs = new TypeToken<ArrayList<DBYear>>(){}.getType();
		
		String response = null;
		response = serverIF.sendRequest("GET<dbstatus>");
				
		//check response. If response from server indicates a successful return,
		//return the list of DB Years to the ui who requested the list. Sort
		//the list such that most current year is at the top of the list (index = 0)
		if(response != null && response.startsWith("DB_STATUS"))
		{	
			dbYearList = gson.fromJson(response.substring(9), listOfDBs);
			Collections.sort(dbYearList, new DBYearComparator());
		}
		
		return dbYearList;
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
		UserDB userDB = UserDB.getInstance();
    		String confirmMssg = String.format("<html>%s, please confirm you want to add<br>the %d year to the ONC Server</html>", 
    			userDB.getLoggedInUser().getFirstName(), today.get(Calendar.YEAR));
    	
    		Object[] options= {"Cancel", "Add " + today.get(Calendar.YEAR) };
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							oncGVs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(GlobalVariablesDB.getFrame(), "*** Confirm Add New Year ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		
		//if confirmed, send the add request to the server and await the response
		if(selectedValue != null && selectedValue.toString().startsWith("Add"))
		{
			//set up user notification of result
			String mssg = null;
			String title = "Add Year Failed";
	    		int mssgType = JOptionPane.ERROR_MESSAGE;
	    	
			//send add new year request to the ONC Server via the  DBStatus data base
	    	//and process response. Inform the user of the result
			String response = serverIF.sendRequest("POST<add_newseason>");		
			if(response != null && response.startsWith("ADDED_DBYEAR"))
			{
				processAddedDBYear(response.substring(12));
			}
			else if(response != null && response.startsWith("ADD_DBYEAR_FAILED"))
			{
				mssg = response.substring(17);	 //alert the user the add failed
				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), mssg, title, mssgType, oncGVs.getImageIcon(0));
			}
			else 
			{
				mssg = "Error: ONC Server failed to respond";	//general server error - didn't respond
				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), mssg, title, mssgType, oncGVs.getImageIcon(0));
			}
		}
    }

	void processAddedDBYear(String json)
	{
		//create the dbYear list object returned by the server
		Gson gson = new Gson();
		Type listtype = new TypeToken<ArrayList<DBYear>>(){}.getType();
		ArrayList<DBYear> dbYearList =  gson.fromJson(json, listtype);
		Collections.sort(dbYearList, new DBYearComparator());
		
		//notify database listeners of the modified dbYear list
		fireDataChanged(this, "ADDED_DBYEAR", dbYearList);
		
		//latest year is always the first object in the dbYearList
		int addedYear = dbYearList.get(0).getYear();
		
		String mssg = String.format("%d database added, now available", addedYear);
		ONCPopupMessage popup = new ONCPopupMessage( oncGVs.getImageIcon(0));
		Point loc = GlobalVariablesDB.getFrame().getLocationOnScreen();
		popup.setLocation(loc.x+450, loc.y+70);
		popup.show("Message from ONC Server", mssg);
	}
	
	void importEarlyComponentDBs()
	{
		for(ONCDatabase earlyComponentDB : earlyImportDBList)
			earlyComponentDB.importDB();
	}
	
	String update(Object source, ONCObject oncObject)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_dbyear>" + gson.toJson(oncObject, DBYear.class));
				
		//check response. If response from server indicates a successful update,
		//create and store the updated child in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response != null && response.startsWith("UPDATED_DBYEAR"))
			fireDataChanged(source, "UPDATED_DBYEAR", gson.fromJson(response.substring(14), DBYear.class));
				
		return response;
	}
	
	void exportComponentDBToCSV()
    {
		String mssg;
		ONCFileChooser fc = new ONCFileChooser(GlobalVariablesDB.getFrame());
		File folder = fc.getDirectory("Select folder to save DB .csv files to");
    	
		if(folder == null) 
    		mssg = "Database save failed, no folder selected";	
		else if(!folder.exists())
    		mssg = String.format("Database save failed:<br>%s", folder.toString());	
		else
		{	
			mssg = String.format("Database sucessfully saved to:<br>%s", folder.getAbsolutePath());
			String path = folder.toString();
			
			boolean result = false;
			for(ONCDatabase componentDB : importDBList)
			{
				result = componentDB.exportDBToCSV(GlobalVariablesDB.getFrame(), String.format("%s/%sDB.csv",
																				path, componentDB.title()));
				if(!result)
				{
					mssg = componentDB.title() + " save failed";
					break;
				}
			}
/*	
			noteDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/NoteDB.csv");
			userDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/UserDB.csv");
			groupDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/GroupDB.csv");
			childDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/ChildDB.csv");
			childGiftDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/ChildGiftDB.csv");
			famHistoryDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/FamilyHistoryDB.csv");
			activityDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/ActivityDB.csv");
			volunteerDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/VolunteerDB.csv");
			familyDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/FamilyDB.csv");
			oncGVs.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/GlobalVariables.csv");
			inventoryDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/Inventory.csv");
			smsDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/SMSDB.csv");
			mealDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/MealDB.csv");
			oncAdultDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/AdultDB.csv");
			batteryDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/BatteryDB.csv");
			partnerDB.exportDBToCSV(path + "/OrgDB.csv");
			giftCatalog.exportToCSV(GlobalVariablesDB.getFrame(), path + "/GiftCatalog.csv");
			giftDetailDB.exportDBToCSV(GlobalVariablesDB.getFrame(), path + "/GiftDetailDB.csv");
*/		
		}
    	
    	ONCPopupMessage savedbPU = new ONCPopupMessage(oncGVs.getImageIcon(0));
		savedbPU.setLocationRelativeTo(GlobalVariablesDB.getFrame());
		savedbPU.show("Database Export Result", mssg);		
    }
	
	
	String exportFamilyReportToCSV()
    {
		String filename = null;
	
		ONCFileChooser fc = new ONCFileChooser(GlobalVariablesDB.getFrame());
		File oncwritefile= fc.getFile("Select .csv file to save to",
									new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
    	    
				for(ONCFamily fam:familyDB.getList())
					writer.writeNext(rb.getFamilyReportCSVRowData(fam));	//Get family data
    	 
				writer.close();
				filename = oncwritefile.getName();
    	       	    
			} 
			catch (IOException x)
			{
				System.err.format("IO Exception: %s%n", x);
				JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), oncwritefile.getName() + " could not be saved", 
					"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
			}
	    }
    	
	    return filename;
    }

    /****
     * Method is called when data is loaded from the server to the local data base. To prevent race conditions,
     * each component database is asked to notify they're listeners instead of notifying after each component 
     * database is loaded
     * @param bServerDataLoaded
     * @param year
     */
    void serverDataLoadComplete(boolean bServerDataLoaded, Integer year)
    {
    	if(progressNavPanel != null)
    		progressNavPanel.setVisibleProgressBar(false);
    	
		if(bServerDataLoaded)
		{
			//notify each of the client listeners that the import is complete.
			this.fireDataChanged(this, "LOADED_DATABASE", year);
			
			giftCatalog.initializeCounts();
			
			//Now that we have season data loaded let the user know that data has been loaded
			GlobalVariablesDB.getFrame().setTitle(String.format("Our Neighbor's Child - %d Season Data", year));	
    	}

    	//tell the server interface to pass on server data base changes to local data bases
    	if(serverIF != null)
    		serverIF.setDatabaseLoaded(true);	
    }
    
    @Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_DBYEAR"))
		{
			Gson gson = new Gson();
			fireDataChanged(this, "UPDATED_DBYEAR", gson.fromJson(ue.getJson(), DBYear.class));
		}
		else if(ue.getType().equals("ADDED_DBYEAR"))
		{
			processAddedDBYear(ue.getJson());
		}
	}
  
	 /***************************************************************************************************
     * This class communicates with the ONC Server to fetch season data from the server data base
     * and store in the local data base. This executes as a background task. A progress bar,
     * provided at the time the class is instantiated, shows the user the progress in fetching data. 
     **************************************************************************************************/
    public class ONCServerDBImporter extends SwingWorker<Void, Void>
    {
		Integer year;
		boolean bServerDataLoaded;
	
		ONCServerDBImporter(Integer year)
		{
			this.year = year;
			bServerDataLoaded = false;
		}
    	
		@Override
		protected Void doInBackground() throws Exception
		{
			int progress = 0;
			int increment = 100/importDBList.size();
			this.setProgress(progress);
	    	
			//Set the year this client will be working with
			serverIF.sendRequest("POST<setyear>" + year);
			
			//import global variables
			bServerDataLoaded = oncGVs.importDB();
			this.setProgress(progress += increment);
			
			if(bServerDataLoaded)
			{	
				//import component databases from ONC Server
				for(ONCDatabase componentDB : importDBList)
				{
					componentDB.importDB();
					this.setProgress(progress += increment);
				}
			}
					
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
	    }
    }
	
	private class DBYearComparator implements Comparator<DBYear>
	{
		@Override
		public int compare(DBYear o1, DBYear o2)
		{
			//return the earlier year
			Integer dbYear1 = o1.getYear();
			Integer dbYear2 = o2.getYear();
			
			if(dbYear1 < dbYear2)
				return 1;
			else if(dbYear1 > dbYear2)
				return -1;
			else
				return 0;
		}
	}
}
