package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class VolunteerDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.VOLUNTEER;
	private static final int DRIVER_OBJECT_CSV_HEADER_LENGTH = 18;
	private static final int DRIVER_CSVFILE_HEADER_LENGTH = 20;
	private static VolunteerDB instance = null;
	private List<ONCVolunteer> volunteerList;
	
	private VolunteerDB()
	{
		super(DB_TYPE);
		volunteerList = new ArrayList<ONCVolunteer>();
	}
	
	public static VolunteerDB getInstance()
	{
		if(instance == null)
			instance = new VolunteerDB();
		
		return instance;
	}
	
	@Override
	List<ONCVolunteer> getList() { return volunteerList; }
	
	ONCVolunteer getDriver(int index) { return volunteerList.get(index); }
	
	//implementation of abstract classes
	ONCVolunteer getObjectAtIndex(int index) { return volunteerList.get(index); }
	
	public List<ONCVolunteer> getDriverDB() { return volunteerList; }
	
	String importDrivers(JFrame pFrame, Date today, String user, ImageIcon oncIcon)	
	{	
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select Volunteer .csv file to import");	
 	    chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
 	       
	    int volunteersAddedCount = 0;
	    File volFile = null;
	    
	    int returnVal = chooser.showOpenDialog(pFrame);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	volFile = chooser.getSelectedFile();
	    	CSVReader reader = null;
	    	try 
	    	{
	    		reader = new CSVReader(new FileReader(volFile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == DRIVER_CSVFILE_HEADER_LENGTH)
	    			{
	    				//build a list of ONCVolunteers from the file.
	    				List<ONCVolunteer> volList = new ArrayList<ONCVolunteer>();
	    				
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					//don't process records that don't have at least a first or last name
	    					if(nextLine.length > 8 && nextLine[6].length() + nextLine[7].length() > 2)
	    					{
//	    						System.out.println(String.format("fn %s ln %s", nextLine[6], nextLine[7]));
	    						ONCVolunteer currVol = searchVolunteerListForMatch(nextLine[6], nextLine[7], volList);
	    					
	    						if(currVol != null)
	    						{
	    							//update the volunteer with the activity from this line
	    							currVol.setActivityCode(updateActivityCode(nextLine[4], currVol.getActivityCode()));
	    						}
	    						else
	    						{
	    							//create a new volunteer and add it
	    							int newActivityCode = updateActivityCode(nextLine[4], 0);
	    							ONCVolunteer newVol = new ONCVolunteer(nextLine, today, user, newActivityCode);
	    						
	    							volList.add(newVol);
	    						}
	    					}
	    				}
	    				
	    				Collections.sort(volList);
	    				
	    				//now that we have a list of volunteers from the file, send the to the
	    				//server to add to the existing database
	    				//create the request to the server to import the families
		    			Gson gson = new Gson();
		    			Type listtype = new TypeToken<ArrayList<ONCVolunteer>>(){}.getType();
		    			
		    			String response = serverIF.sendRequest("POST<volunteer_group>" + gson.toJson(volList, listtype));
		    			
		    			if(response != null && response.startsWith("ADDED_VOLUNTEER_GROUP"))
		    			{
		    				//process the list of jsons returned, adding agent, families, adults
		    				//and children to the local databases
		    		    	Type jsonlisttype = new TypeToken<ArrayList<String>>(){}.getType();
		    		    	ArrayList<String> changeList = gson.fromJson(response.substring(21), jsonlisttype);
		    		    	
		    		    	//loop thru list of changes, processing each one
		    		    	for(String change: changeList)
		    		    		if(change.startsWith("ADDED_DRIVER"))
		    		    		{
		    		    			this.processAddedObject(this, change.substring("ADDED_DRIVER".length()));
		    		    			volunteersAddedCount++;
		    		    		}
		    			}
		    			else
		    			{
		    				JOptionPane.showMessageDialog(pFrame, "An error occured, " +
		    	    			 volFile.getName() + " cannot be imported by the server", 
		    	    			"ONC Server Britepath Import Error", JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		    			}
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pFrame, "Volunteer file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Volunteer File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pFrame, "Couldn't read header in file: " + volFile.getName(), 
						"Invalid Volunteer File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pFrame, "Unable to open Volunteer file: " + volFile.getName(), 
    				"Volunteer file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    	finally
	    	{
	    		try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	    
	    //If no drivers were in the file
	    if(volFile == null || volunteersAddedCount == 0)
	    	return "No Delivery Drivers were imported";
	    else
	    	return String.format("%d Delivery Drivers imported from %s", volunteersAddedCount, volFile.getName());
	    
	}
	
	int updateActivityCode(String line, int activityCode)
	{
		String[] choices = {"Gift Inventory and Set up",
							"Adult Warehouse Volunteers",
							"Shopper",
							"Packaging",
							"Cookie Bakers",
							"Miscellaneous Warehouse Support",
							"Set up for Delivery",
							"Delivery Day",
							"Warehouse Inventory/Clean-Up",
							"clothing wishes",
							"Coprorate",
							"Warehouse Set-Up",
							"arriors",
							"Bike Assembly Volunteers",
							"Adult Volunteers for Warehouse Delivery Day Assignments",
							"Post Delivery",
							"Warehouse Inventory/Pack Up"};
		
		int[] codes = {16,4,32,8,64,4,1,2,128,256,512,4096,4,1024,4,2048,128};
		
		
		int index = 0;
		while(index < choices.length && !line.contains(choices[index]))
			index++;
		
		if(index < choices.length)
			activityCode = activityCode | codes[index];
		
		return activityCode;
	}
	
	ONCVolunteer searchVolunteerListForMatch(String fn, String ln, List<ONCVolunteer> volList)
	{
		int index = 0;
		while(index < volList.size() &&
			  !(volList.get(index).getfName().equals(fn) && volList.get(index).getlName().equals(ln)))
			index++;
		
		return index < volList.size() ? volList.get(index) : null;
	}
	
	public void readDriverALObject(ObjectInputStream ois)
	{
		try 
		{
			volunteerList = (ArrayList<ONCVolunteer>) ois.readObject();		
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeDriverALObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(volunteerList);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void clearDriverData()
	{
		volunteerList.clear();
	}
	
	public int getDriverIndex(int driverID)
	{
		int index = 0;
		while(index < volunteerList.size() && volunteerList.get(index).getID() != driverID )
			index++;
			
		if(index < volunteerList.size())
			return index;
		else
			return -1;
	}
	
	public ONCVolunteer getVolunteer(int volunteerID)
	{
		int index = 0;
		while(index < volunteerList.size() && volunteerList.get(index).getID() != volunteerID )
			index++;
			
		if(index < volunteerList.size())
			return volunteerList.get(index);
		else
			return null;
	}
	
	String getDriverLNFI(int driverID)
	{
		int index = 0;
		while(index < volunteerList.size() && volunteerList.get(index).getID() != driverID )
			index++;
			
		if(index < volunteerList.size())
			return volunteerList.get(index).getlName() + ", " + volunteerList.get(index).getfName().charAt(0);
		else
			return Long.toString(driverID);
	}
	
	//Get index of driver in list by searching for driver number
	int getDriverIndex(String dNumber)
	{
		int index = 0;
		while(index < volunteerList.size() && !volunteerList.get(index).getDrvNum().equals(dNumber))
			index++;
		
		if(index < volunteerList.size())
			return index;
		else
			return -1;
	}
	
	String getDriverLNFN(String dNumber)
	{
		String result = dNumber;
		
		//If a numeric string (valid driver ID) search the data base. If a match is found
		//return the last name, first initial. If the ID is valid and no match is found 
		//it means the ID hasn't been entered in the data base yet. In that case, return it. 
		//id. If the ID isn't valid, simply return it as well 
		if(!dNumber.isEmpty() && dNumber.matches("-?\\d+(\\.\\d+)?"))
		{
		
			int index = 0;
			while(index < volunteerList.size() && !volunteerList.get(index).getDrvNum().equals(dNumber))
				index++;
		
			if(index < volunteerList.size())	//Valid ID found in database
				result = volunteerList.get(index).getlName() + ", " + volunteerList.get(index).getfName();
		}
		
		return result;	
	}
	
	String importDriverDatabase()
	{
		String response = "NO_DRIVERS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCVolunteer>>(){}.getType();
			
			response = serverIF.sendRequest("GET<drivers>");
				volunteerList = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_DRIVERS"))
			{
				response =  "DRIVERS_LOADED";
				fireDataChanged(this, "LOADED_DRIVERS", null);
			}
		}
		
		return response;
	}
	
	String importDriverDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
    		
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "DriverDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Driver DB .csv file to import");	
    		chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    		returnVal = chooser.showOpenDialog(pf);
    		pyfile = chooser.getSelectedFile();
		}
		
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	filename = pyfile.getName();
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == DRIVER_OBJECT_CSV_HEADER_LENGTH)
	    			{
	    				volunteerList.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					volunteerList.add(new ONCVolunteer(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Driver database file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Driver Database File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in driver database file: " + filename, 
						"Invalid Driver Database File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    		
	    		reader.close();
	    		
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open driver database file: " + filename, 
    				"Driver database file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Driver DB to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
    	}
    	else
    		oncwritefile = new File(filename);
    	
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		 String[] header = {"Driver ID", "First Name", "Last Name", "House Number", "Street",
	    				 			"Unit", "City", "Zip", "Email", "Home Phone", "Cell Phone", 
	    				 			"Driver License", "Car", "# Del. Assigned", "Time Stamp",
	    				 			"Stoplight Pos", "Stoplight Mssg", "Changed By"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCVolunteer d:volunteerList)
	    	    	writer.writeNext(d.getExportRow());	//Get family data
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }
	
	List<ONCWarehouseVolunteer> getWarehouseHistory(int volID)
	{
		List<ONCWarehouseVolunteer> whList = new ArrayList<ONCWarehouseVolunteer>();
		Gson gson = new Gson();
		
		String response = serverIF.sendRequest("GET<warehousehistory>" + Integer.toString(volID));
		
		if(response != null)
		{
			Type listtype = new TypeToken<ArrayList<ONCWarehouseVolunteer>>(){}.getType();	
			whList = gson.fromJson(response, listtype);
		}
		
		return whList;
	}
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_driver>" + 
											gson.toJson(entity, ONCVolunteer.class));
		
		if(response.startsWith("ADDED_DRIVER"))
			processAddedObject(source, response.substring(12));
		
		return response;	
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added organization in local data base
		Gson gson = new Gson();
		ONCVolunteer addedDriver = gson.fromJson(json, ONCVolunteer.class);
		volunteerList.add(addedDriver);
//		System.out.println(String.format("DriverDB processAddedDriver: Driver Added ID: %d",
//				addedDriver.getID()));
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_DRIVER", addedDriver);
	}
	
	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_driver>" + 
											gson.toJson(entity, ONCVolunteer.class));
		
		if(response.startsWith("UPDATED_DRIVER"))
		{
			processUpdatedObject(source, response.substring(14), volunteerList);
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json, List<? extends ONCObject> objList)
	{
		Gson gson = new Gson();
		ONCVolunteer updatedObj = gson.fromJson(json, ONCVolunteer.class);
		
		//store updated object in local data base
		int index = 0;
		while(index < objList.size() && objList.get(index).getID() != updatedObj.getID())
			index++;
		
		if(index < objList.size())
		{
			replaceObject(index,  updatedObj);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_DRIVER", updatedObj);
		}
	}
	
	String delete(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_driver>" + 
											gson.toJson(entity, ONCVolunteer.class));
		
		
		if(response.startsWith("DELETED_DRIVER"))
			processDeletedObject(source, response.substring(14));
		
		return response;
	}
	
	void processDeletedObject(Object source, String json)
	{
		//remove deleted organization in local data base
		Gson gson = new Gson();
		ONCVolunteer deletedObj = gson.fromJson(json, ONCVolunteer.class);
		
		int index=0;
		while(index < volunteerList.size() && volunteerList.get(index).getID() != deletedObj.getID())
			index++;
		
		//If deleted partner was found, remove it and notify ui's
		if(index < volunteerList.size())
		{
			volunteerList.remove(index);
			fireDataChanged(source, "DELETED_DRIVER", deletedObj);
		}
	}
	
	/*****************************************************************************
	 * This method handles the object specific replace processing for replacing
	 * an ONCDriver in the local data base. It is called by the super class 
	 * processUpdatedObject method that is inherited by all data base object classes
	 * @param updatedObj
	 * @param index
	 *************************************************************************/
	void replaceObject(int index, ONCObject updatedObj)
	{
		ONCVolunteer updatedDriver = (ONCVolunteer) updatedObj;
		volunteerList.set(index,  updatedDriver);
//		System.out.println(String.format("DriverDB- replaceObject: first name: %s", updatedDriver.getfName()));
	}
		
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_DRIVER"))
		{
//			System.out.println(String.format("DriverDB- datachanged: json: %s", ue.getJson()));
			processUpdatedObject(this, ue.getJson(), volunteerList);
		}
		else if(ue.getType().equals("ADDED_DRIVER"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_DRIVER"))
		{
			processDeletedObject(this, ue.getJson());
		}
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		searchAL.clear();
		String searchType = "";
		
//    	int sn; 	//-1 indicates family number not found
    	
    	if(!data.isEmpty() && isNumeric(data))
    	{
    		//If a numeric string, then search for Driver Number match, else search for last name match
    		searchType = "Driver #";
    		if(data.matches("-?\\d+(\\.\\d+)?"))
    		{
    			int index = 0;
    			while(index < volunteerList.size() && !volunteerList.get(index).getDrvNum().equals(data))
    				index++;
    			
    			if(index < volunteerList.size())
    				searchAL.add(volunteerList.get(index).getID());
    		}
    	}
    	else	//Check for driver first name or last name
    	{
    		searchType = "Driver Name";
//			for(int i=0; i<this.getNumberOfOrganizations(); i++)
			for(ONCVolunteer d:volunteerList)
			{
				if(d.getfName().toLowerCase().contains(data.toLowerCase()) ||
					d.getlName().toLowerCase().contains(data.toLowerCase()))
				{
					searchAL.add(d.getID());
				}
			}
    	}
    	
		return searchType;
	}

	@Override
	int size() { return volunteerList.size(); }
	
	boolean sortDB(ArrayList<ONCVolunteer> dAL, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("Drv #"))	//Sort on Driver Number
    		Collections.sort(dAL, new ONCDriverNumberComparator());
		else if(dbField.equals("Last Name"))	//Sort on Driver Last Name
    		Collections.sort(dAL, new ONCDriverLastNameComparator());
		else if(dbField.equals("# Del"))	//Sort on Driver Deliveries Assigned
    		Collections.sort(dAL, new ONCDriverDeliveryComparator());
		else if(dbField.equals("Changed By"))	//Sort on Driver Changed By
    		Collections.sort(dAL, new ONCDriverChangedByComparator());
		else if(dbField.equals("SL"))	//Sort on Driver Changed By
    		Collections.sort(dAL, new ONCDriverStoplightComparator());
		else
			bSortOccurred = false;
		
		return bSortOccurred;	
	}

	private class ONCDriverNumberComparator implements Comparator<ONCVolunteer>
	{
		@Override
		public int compare(ONCVolunteer o1, ONCVolunteer o2)
		{
			if(isNumeric(o1.getDrvNum()) && isNumeric(o2.getDrvNum()))
			{
				Integer onc1 = Integer.parseInt(o1.getDrvNum());
				Integer onc2 = Integer.parseInt(o2.getDrvNum());
				return onc1.compareTo(onc2);
			}
			else if(isNumeric(o1.getDrvNum()) && !isNumeric(o2.getDrvNum()))
				return -1;
			else if(!isNumeric(o1.getDrvNum()) && isNumeric(o2.getDrvNum()))
				return 1;
			else
				return o1.getDrvNum().compareTo(o2.getDrvNum());
		}
	}
	
	private class ONCDriverLastNameComparator implements Comparator<ONCVolunteer>
	{
		@Override
		public int compare(ONCVolunteer o1, ONCVolunteer o2)
		{
			return o1.getlName().compareTo(o2.getlName());
		}
	}
	
	private class ONCDriverDeliveryComparator implements Comparator<ONCVolunteer>
	{
		@Override
		public int compare(ONCVolunteer o1, ONCVolunteer o2)
		{
			Integer o1Del = (Integer) o1.getDelAssigned();
			Integer o2Del = (Integer) o2.getDelAssigned();
			return o1Del.compareTo(o2Del);
		}
	}
	
	private class ONCDriverChangedByComparator implements Comparator<ONCVolunteer>
	{
		@Override
		public int compare(ONCVolunteer o1, ONCVolunteer o2)
		{
			return o1.getChangedBy().compareTo(o2.getChangedBy());
		}
	}
	
	private class ONCDriverStoplightComparator implements Comparator<ONCVolunteer>
	{
		@Override
		public int compare(ONCVolunteer o1, ONCVolunteer o2)
		{
			Integer o1SL = (Integer) o1.getStoplightPos();
			Integer o2SL = (Integer) o2.getStoplightPos();
			return o1SL.compareTo(o2SL);
		}
	}
}
