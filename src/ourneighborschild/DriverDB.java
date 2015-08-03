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

public class DriverDB extends ONCSearchableDatabase
{
	private static final String DB_TYPE = "DRIVER";
	private static final int DRIVER_OBJECT_CSV_HEADER_LENGTH = 18;
	private static final int DRIVER_CSVFILE_HEADER_LENGTH = 20;
	private static DriverDB instance = null;
	private List<ONCDriver> driverAL;
	
	private DriverDB()
	{
		super();
		driverAL = new ArrayList<ONCDriver>();
	}
	
	public static DriverDB getInstance()
	{
		if(instance == null)
			instance = new DriverDB();
		
		return instance;
	}
	
	@Override
	List<ONCDriver> getList() { return driverAL; }
	
	ONCDriver getDriver(int index) { return driverAL.get(index); }
	
	//implementation of abstract classes
	ONCDriver getObjectAtIndex(int index) { return driverAL.get(index); }
	
	String getDBType() { return DB_TYPE; }
	
	public List<ONCDriver> getDriverDB() { return driverAL; }
	
	String importDrivers(JFrame pFrame, Date today, String user, ImageIcon oncIcon)	
	{	
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select Driver .csv file to import");	
 	    chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
 	       
	    String filename = "";
	    int importcount = 0;
	    
	    int returnVal = chooser.showOpenDialog(pFrame);
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	File pyfile = chooser.getSelectedFile();
	    	filename = pyfile.getName();
	    	CSVReader reader = null;
	    	try 
	    	{
	    		reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == DRIVER_CSVFILE_HEADER_LENGTH)
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					//Check to see if its a delivery day driver
	    					if(nextLine[5].contains("Deliver gifts to recipients") && 
	    							!(nextLine[6].isEmpty() && nextLine[7].isEmpty()))
	    					{
	    						//If it is, read them into an array list of string[]
	    						//If it is, generate and send add request to the server
	    						ONCDriver addDriverReq = new ONCDriver(nextLine, generateDriverID(), today, user);
	    						String response = add(this, addDriverReq);
	    						
	    						if(response.startsWith("ADDED_DRIVER"))
	    							importcount++;
	    						else
	    						{
	    							JOptionPane.showMessageDialog(pFrame, "Add Driver failed, driver: " + addDriverReq.getlName(), 
	    		    						"Server ADD DRIVER failed", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    						}
	    					}
	    				}
	 
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pFrame, "Driver file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Driver File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pFrame, "Couldn't read header in file: " + filename, 
						"Invalid Driver File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pFrame, "Unable to open Driver file: " + filename, 
    				"Organizations file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
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
	    if(importcount == 0)
	    	return "No Delivery Drivers were imported";
	    else	//Alphabetize and add new driver objects to the driver db
	    {
	    	return String.format("%d Delivery Drivers imported from %s", importcount, filename);
	    }
	}
	
	public void readDriverALObject(ObjectInputStream ois)
	{
		try 
		{
			driverAL = (ArrayList<ONCDriver>) ois.readObject();		
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
			oos.writeObject(driverAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void clearDriverData()
	{
		driverAL.clear();
	}
	
	int generateDriverID()
	{
		int id = driverAL.size() + 1;
		
		return id;
	}

	public int getDriverIndex(int driverID)
	{
		int index = 0;
		while(index < driverAL.size() && driverAL.get(index).getID() != driverID )
			index++;
			
		if(index < driverAL.size())
			return index;
		else
			return -1;
	}
	
	String getDriverLNFI(int driverID)
	{
		int index = 0;
		while(index < driverAL.size() && driverAL.get(index).getID() != driverID )
			index++;
			
		if(index < driverAL.size())
			return driverAL.get(index).getlName() + ", " + driverAL.get(index).getfName().charAt(0);
		else
			return Long.toString(driverID);
	}
	
	//Get index of driver in list by searching for driver number
	int getDriverIndex(String dNumber)
	{
		int index = 0;
		while(index < driverAL.size() && !driverAL.get(index).getDrvNum().equals(dNumber))
			index++;
		
		if(index < driverAL.size())
			return index;
		else
			return -1;
	}
	
/*	CHANGED WHEN DRIVER NOW HAS BOTH AN ID and a DRV NUM
	String getDriverLNFI(String dID)
	{
		String result = dID;
		
		//If a numeric string (valid driver ID) search the data base. If a match is found
		//return the last name, first initial. If the ID is valid and no match is found 
		//it means the ID hasn't been entered in the data base yet. In that case, return it. 
		//id. If the ID isn't valid, simply return it as well 
		if(!dID.isEmpty() && dID.matches("-?\\d+(\\.\\d+)?"))
		{
			long driverID = Long.parseLong(dID);
		
			int index = 0;
			while(index < driverAL.size() && driverAL.get(index).getID() != driverID )
				index++;
		
			if(index < driverAL.size())	//Valid ID found in database
				result = driverAL.get(index).getlName() + ", " + driverAL.get(index).getfName().charAt(0);
		}
		
		return result;	
	}
*/	
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
			while(index < driverAL.size() && !driverAL.get(index).getDrvNum().equals(dNumber))
				index++;
		
			if(index < driverAL.size())	//Valid ID found in database
				result = driverAL.get(index).getlName() + ", " + driverAL.get(index).getfName();
		}
		
		return result;	
	}
	
	String importDriverDatabase()
	{
		ServerIF serverIF = ServerIF.getInstance();
		String response = "NO_DRIVERS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCDriver>>(){}.getType();
			
			response = serverIF.sendRequest("GET<drivers>");
				driverAL = gson.fromJson(response, listtype);				

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
	    				driverAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					driverAL.add(new ONCDriver(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Driver database file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Driver Database File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in driver database file: " + filename, 
						"Invalid Driver Database File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open driver database file: " + filename, 
    				"Driver database file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	String exportDriverDBToCSV(JFrame pf, String filename)
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
	    	    
	    	    for(ONCDriver d:driverAL)
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
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_driver>" + 
											gson.toJson(entity, ONCDriver.class));
		
		if(response.startsWith("ADDED_DRIVER"))
			processAddedObject(source, response.substring(12));
		
		return response;	
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added organization in local data base
		Gson gson = new Gson();
		ONCDriver addedDriver = gson.fromJson(json, ONCDriver.class);
		driverAL.add(addedDriver);
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
											gson.toJson(entity, ONCDriver.class));
		
		if(response.startsWith("UPDATED_DRIVER"))
		{
			processUpdatedObject(source, response.substring(14), driverAL);
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json, List<? extends ONCObject> objList)
	{
		Gson gson = new Gson();
		ONCDriver updatedObj = gson.fromJson(json, ONCDriver.class);
		
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
											gson.toJson(entity, ONCDriver.class));
		
		
		if(response.startsWith("DELETED_DRIVER"))
			processDeletedObject(source, response.substring(14));
		
		return response;
	}
	
	void processDeletedObject(Object source, String json)
	{
		//remove deleted organization in local data base
		Gson gson = new Gson();
		ONCDriver deletedObj = gson.fromJson(json, ONCDriver.class);
		
		int index=0;
		while(index < driverAL.size() && driverAL.get(index).getID() != deletedObj.getID())
			index++;
		
		//If deleted partner was found, remove it and notify ui's
		if(index < driverAL.size())
		{
			driverAL.remove(index);
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
		ONCDriver updatedDriver = (ONCDriver) updatedObj;
		driverAL.set(index,  updatedDriver);
//		System.out.println(String.format("DriverDB- replaceObject: first name: %s", updatedDriver.getfName()));
	}
		
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_DRIVER"))
		{
//			System.out.println(String.format("DriverDB- datachanged: json: %s", ue.getJson()));
			processUpdatedObject(this, ue.getJson(), driverAL);
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
    			while(index < driverAL.size() && !driverAL.get(index).getDrvNum().equals(data))
    				index++;
    			
    			if(index < driverAL.size())
    				searchAL.add(driverAL.get(index).getID());
    		}
    	}
    	else	//Check for driver first name or last name
    	{
    		searchType = "Driver Name";
//			for(int i=0; i<this.getNumberOfOrganizations(); i++)
			for(ONCDriver d:driverAL)
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
	int size() { return driverAL.size(); }
	
	boolean sortDB(ArrayList<ONCDriver> dAL, String dbField)
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

	private class ONCDriverNumberComparator implements Comparator<ONCDriver>
	{
		@Override
		public int compare(ONCDriver o1, ONCDriver o2)
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
	
	private class ONCDriverLastNameComparator implements Comparator<ONCDriver>
	{
		@Override
		public int compare(ONCDriver o1, ONCDriver o2)
		{
			return o1.getlName().compareTo(o2.getlName());
		}
	}
	
	private class ONCDriverDeliveryComparator implements Comparator<ONCDriver>
	{
		@Override
		public int compare(ONCDriver o1, ONCDriver o2)
		{
			Integer o1Del = (Integer) o1.getDelAssigned();
			Integer o2Del = (Integer) o2.getDelAssigned();
			return o1Del.compareTo(o2Del);
		}
	}
	
	private class ONCDriverChangedByComparator implements Comparator<ONCDriver>
	{
		@Override
		public int compare(ONCDriver o1, ONCDriver o2)
		{
			return o1.getChangedBy().compareTo(o2.getChangedBy());
		}
	}
	
	private class ONCDriverStoplightComparator implements Comparator<ONCDriver>
	{
		@Override
		public int compare(ONCDriver o1, ONCDriver o2)
		{
			Integer o1SL = (Integer) o1.getStoplightPos();
			Integer o2SL = (Integer) o2.getStoplightPos();
			return o1SL.compareTo(o2SL);
		}
	}
}