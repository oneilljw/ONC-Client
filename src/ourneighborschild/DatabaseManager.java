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

public class DatabaseManager extends ONCDatabase
{
	/***
	 * This singleton class implements a manager for all ONC component databases. The manager
	 * initializes each component data base and loads them from the server. It uses a Swing
	 * Worker to load the component data bases in a background thread. In addition, it 
	 * can export the local component data bases to .csv files. 
	 * 
	 * The method also manages retrieving data base status from the server and adding a new
	 * season to the server data base.
	 */
	private static DatabaseManager instance = null;
	
	//Local Data Base Structures
	private GlobalVariables oncGVs;			//Holds the Global Variables
	private UserDB oncUserDB;				//Holds the ONC User, many of which are Agents
	private FamilyDB oncFamDB;				//Holds ONC Family Database
	private ChildDB oncChildDB;				//Holds ONC Child database
	private ChildWishDB oncChildWishDB; 	//Holds ONC Child Wish database
	private GroupDB oncGroupDB;				//Holds ONC Groups
	private PartnerDB oncOrgDB;				//Holds ONC Partner Organizations
	private ONCWishCatalog oncWishCat;		//Holds ONC Wish Catalog
	private WishDetailDB oncWishDetailDB;	//Holds ONC Wish Detail Data Base
	private VolunteerDB oncDDB;				//Holds the ONC Driver Data Base
	private FamilyHistoryDB oncDelDB;			//Holds the ONC Delivery Data Base
	private ONCRegions oncRegions;
	private AdultDB oncAdultDB;				//Holds ONC Adult database
	private MealDB oncMealDB;				//Holds ONC Meal database
	private InventoryDB oncInvDB;			//Holds current inventory
	
	private DatabaseManager()
	{
		super();
		
		//initialize the component data bases
		oncGVs = GlobalVariables.getInstance();
		oncRegions = ONCRegions.getInstance();
		oncUserDB = UserDB.getInstance();
		oncGroupDB = GroupDB.getInstance();
		oncOrgDB = PartnerDB.getInstance();
		oncWishDetailDB = WishDetailDB.getInstance();
		oncWishCat = ONCWishCatalog.getInstance();
		oncDDB = VolunteerDB.getInstance();
		oncDelDB = FamilyHistoryDB.getInstance();
		oncChildDB = ChildDB.getInstance();
		oncChildWishDB = ChildWishDB.getInstance();
		oncAdultDB = AdultDB.getInstance();
		oncMealDB = MealDB.getInstance();
		oncInvDB = InventoryDB.getInstance();
		oncFamDB = FamilyDB.getInstance();
	}
	
	public static DatabaseManager getInstance()
	{
		if(instance == null)
			instance = new DatabaseManager();
		
		return instance;
	}
	
	void importObjectsFromDB(int year)
	{
		//create the progress bar frame
	    ONCProgressBar pb = new ONCProgressBar(oncGVs.getImageIcon(0), 100);
	    Point loc = GlobalVariables.getFrame().getLocationOnScreen();
		pb.setLocation(loc.x+450, loc.y+70);
	    	
		//create the swing worker background task to get the data from the server
		ONCServerDBImporter dataImporter = new ONCServerDBImporter(year, pb);
	    dataImporter.addPropertyChangeListener(pb);
		    
	    //show the progress bar.
		pb.show("Loading " + Integer.toString(year) + " Data", null);
		    
		//execute the background swing worker task
		dataImporter.execute();
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
    			userDB.getLoggedInUser().getFirstname(), today.get(Calendar.YEAR));
    	
    	Object[] options= {"Cancel", "Add " + today.get(Calendar.YEAR) };
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							oncGVs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(GlobalVariables.getFrame(), "*** Confirm Add New Year ***");
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
//			String response = "Error message missing";
//			response = add(this);	//request add of new ONC season
			String response = serverIF.sendRequest("POST<add_newseason>");		
			if(response != null && response.startsWith("ADDED_DBYEAR"))
			{
				processAddedDBYear(response.substring(12));
//				mssg = String.format("New season ucessfully added to ONC Server");
//				title = "Add Year Successful";
//				mssgType = JOptionPane.INFORMATION_MESSAGE;
			}
			else if(response != null && response.startsWith("ADD_DBYEAR_FAILED"))
			{
				mssg = response.substring(17);	 //alert the user the add failed
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg, title, mssgType, oncGVs.getImageIcon(0));
			}
			else 
			{
				mssg = "Error: ONC Server failed to respond";	//general server error - didn't respond
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg, title, mssgType, oncGVs.getImageIcon(0));
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
		Point loc = GlobalVariables.getFrame().getLocationOnScreen();
		popup.setLocation(loc.x+450, loc.y+70);
		popup.show("Message from ONC Server", mssg);
	}
	

	@Override
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
	
	void exportObjectDBToCSV()
    {
    	ONCFileChooser fc = new ONCFileChooser(GlobalVariables.getFrame());
    	File folder = fc.getDirectory("Select folder to save DB .csv files to");
    	
    	String mssg;
    	if(folder == null) 
    		mssg = "Database save failed, no folder selected";	
    	else if(!folder.exists())
    		mssg = String.format("Database save failed:<br>%s", folder.toString());	
    	else
    	{	
    		String path = folder.toString();
    	
    		oncAdultDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/AdultDB.csv");
    		oncUserDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/UserDB.csv");
    		oncGroupDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/GroupDB.csv");
    		oncChildDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/ChildDB.csv");
    		oncChildWishDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/ChildWishDB.csv");
    		oncDelDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/DeliveryDB.csv");
    		oncDDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/DriverDB.csv");
    		oncFamDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/FamilyDB.csv");
    		oncGVs.exportDBToCSV(GlobalVariables.getFrame(), path + "/GlobalVariables.csv");
    		oncInvDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/Inventory.csv");
    		oncMealDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/MealDB.csv");
    		oncOrgDB.exportDBToCSV(path + "/OrgDB.csv");
    		oncWishCat.exportToCSV(GlobalVariables.getFrame(), path + "/WishCatalog.csv");
    		oncWishDetailDB.exportDBToCSV(GlobalVariables.getFrame(), path + "/WishDetailDB.csv");
    		
    		mssg = String.format("Database sucessfully saved to:<br>%s", path); 			
    	}
    	
    	ONCPopupMessage savedbPU = new ONCPopupMessage(oncGVs.getImageIcon(0));
		savedbPU.setLocationRelativeTo(GlobalVariables.getFrame());
		savedbPU.show("Database Export Result", mssg);		
    }
	
	
	String exportFamilyReportToCSV()
    {
    	String filename = null;
    	
    	ONCFileChooser fc = new ONCFileChooser(GlobalVariables.getFrame());
    	File oncwritefile= fc.getFile("Select .csv file to save to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
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
	    		JOptionPane.showMessageDialog(GlobalVariables.getFrame(), oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }

    /****
     * Method is called when data is loaded from the server to the local data base
     * @param bServerDataLoaded
     * @param year
     */
    void serverDataLoadComplete(boolean bServerDataLoaded, String year)
    {
    	if(bServerDataLoaded)
    	{
    		//Now that we have season data loaded let the user know that data has been loaded
    		GlobalVariables.getFrame().setTitle("Our Neighbor's Child - " + year + " Season Data");

			oncWishCat.initializeWishCounts();
			
			//check to see if family data is present and enable controls
			this.fireDataChanged(this, "LOADED_DATABASE", year);
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
	//		System.out.println("DBStatusDB.dataChanged: UPDATED_DBYEAR Received");
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
    	private static final int NUM_OF_DBs = 14;
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
			
			pb.updateHeaderText("Loading Inventory");
			oncInvDB.importInventoryDatabase();
			this.setProgress(progress += increment);
						
//			pb.updateHeaderText("Loading Agents");
//			oncAgentDB.importAgentDatabase();
//			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Groups");
			oncGroupDB.importGroupDBFromServer();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Partners");
			oncOrgDB.importDB();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Drivers");
			oncDDB.importDriverDatabase();
			this.setProgress(progress += increment);
			
			pb.updateHeaderText("Loading Deliveries");
			oncDelDB.importFamilyHistoryDatabase();
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
