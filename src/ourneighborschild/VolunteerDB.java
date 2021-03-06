package ourneighborschild;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VolunteerDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.VOLUNTEER;
	private static VolunteerDB instance = null;
	private VolunteerActivityDB volActDB;
	
	private List<ONCVolunteer> volunteerList;
	
	private VolunteerDB()
	{
		super(DB_TYPE);
		this.title = "Volunteers";
		volunteerList = new ArrayList<ONCVolunteer>();

		volActDB = VolunteerActivityDB.getInstance();
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
	
	public List<ONCVolunteer> getVolunteerList() { return volunteerList; }
/*	
	String importSignUpGeniusVolunteers(JFrame pFrame, String user )	
	{	
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select SignUpGenius .csv file to import");	
 	    chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
 	       
	    int volunteersModifiedCount = 0, volunteersAddedCount = 0;
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
	    			if(header.length == SIGN_UP_GENIUS_FILE_HEADER_LENGTH ||
	    				header.length == SIGN_UP_GENIUS_FILE_HEADER_LENGTH + 1)
	    			{
	    				//create a date format to parse date/time columns in sigh-up genius
	    				//.csv file
	    				
	    				SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy H:mm");
	    				//get the logged in user LNFI
	    				String userLNFI = UserDB.getInstance().getLoggedInUser().getLNFI();
	    				
	    				//clone the local volunteer list
	    				List<ONCVolunteer> cloneVolList = new ArrayList<ONCVolunteer>();
	    				for(ONCVolunteer v : volunteerList)
	    					cloneVolList.add(new ONCVolunteer(v));
	    			
	    				//read each line of the .csv. Determine if the line is a valid volunteer
	    				//record. Determine if the volunteer is already in the database. If they are,
	    				//get the activity associated with the record and determine if it's a new
	    				//activity for the volunteer. If so add it.
	    				//If the volunteer is new to the database, add a new record with a new activity
	    				//list that contsinas the activity assocaited with the record.
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					//don't process records that don't have at least a first or last name
	    					if(nextLine.length > 8 && (nextLine[6].length() + nextLine[7].length() > 1))
	    					{
//	    						System.out.println(String.format("Checking vol: %s %s", nextLine[6], nextLine[7]));
	    						ONCVolunteer currVol = searchVolunteerListForMatch(nextLine[6], nextLine[7], cloneVolList);
	    					
	    						if(currVol != null)
	    						{
//	    							System.out.println(String.format("Curr vol: %s %s", nextLine[6], nextLine[7]));
	    							
	    							//the volunteer is already in the database, determine if the 
	    							//associated activity is new for the volunteer
	    							VolunteerActivity genericActivity = activityDB.getActivity(nextLine[4],  nextLine[0], nextLine[1]);
	    							if(genericActivity != null && !currVol.isVolunteeringFor(genericActivity))
	    							{
	    								//it is a new activity for this existing volunteer. Personalize it and add
	    								//it to their activity list. Add the volunteer to the changed list
	    								VolunteerActivity newAct = new VolunteerActivity(genericActivity);
		    							newAct.setComment(nextLine[9]);	//add comment
		    							try
										{
											newAct.setDateChanged(sdf.parse(nextLine[10]));
										}
										catch (ParseException e)
										{
											newAct.setDateChanged(new Date());
										}
		    							
		    							currVol.addActivity(newAct);
		    							
//   									System.out.println(String.format("Updating vol: %s %s,  added act= %s", 
//	    										currVol.getFirstName(), currVol.getLastName(), newAct.getName()));
	    							}
	    						}
	    						else
	    						{
	    							//new volunteer for the database, create an object and add to 
	    							//change list
	    							
//	    							System.out.println(String.format("New vol: %s %s", nextLine[6], nextLine[7]));
	    							List<VolunteerActivity> actList = new ArrayList<VolunteerActivity>();
	    							
	    							//find the generic activity, copy it, and then personalize it by adding any comment and
	    							//the time the volunteer signed up in SignUpGenius
//	    							System.out.println(String.format("Search for Activity: col4: %s, col0: %s, col1 %s", nextLine[4], nextLine[0], nextLine[1]));
	    							VolunteerActivity genericAct = activityDB.getActivity(nextLine[4],  nextLine[0], nextLine[1]);
	    							if(genericAct != null)
	    							{
//	    								System.out.println(String.format("New vol activity: %d: %s", genericAct.getID(), genericAct.getName()));
	    								VolunteerActivity newAct = new VolunteerActivity(genericAct);
	    								newAct.setComment(nextLine[9]);	//add comment
	    								try
	    								{
	    									newAct.setDateChanged(sdf.parse(nextLine[10]));
	    								}
	    								catch (ParseException e)
	    								{
	    									newAct.setDateChanged(new Date());
	    								}
	    								actList.add(newAct);
	    								ONCVolunteer newVol = new ONCVolunteer(nextLine, userLNFI, actList);
	    								cloneVolList.add(newVol);
	    								
//	    								System.out.println(String.format("Added new vol: %s %s,  act= %s", 
//	    										newVol.getFirstName(), newVol.getLastName(), newAct.getName()));
	    							}
	    						}
	    					}
	    				}
	    				
	    				//create a list of new volunteers or volunteers who's activities have changed
	    				//to do so, compare each clone list volunteer to the original volunteer. If the
	    				//clone list volunteer is new, then them to the new list, eose if they are updated
	    				//add them to the update list;
	    				List<ONCVolunteer> newVolList = new ArrayList<ONCVolunteer>();
	    				List<ONCVolunteer> updateVolList = new ArrayList<ONCVolunteer>();
	    				
	    				for(ONCVolunteer cv: cloneVolList)
	    				{
	    					int index = 0;
	    					while(index < volunteerList.size() &&
	    							!(cv.getFirstName().equals(volunteerList.get(index).getFirstName()) &&
	    							 cv.getLastName().equals(volunteerList.get(index).getLastName())))
	    					{
	    						index++;
	    					}
	    					
	    					if(index < volunteerList.size())
	    					{
	    						//found clone volunteer, see if activity list is changed. If so, add to update list
	    						if(cv.getActivityList().size() != volunteerList.get(index).getActivityList().size())
	    							updateVolList.add(cv);
	    					}
	    					else
	    					{
	    						//clone volunteer not found, they are new
	    						newVolList.add(cv);
	    					}
	    					
	    				}
	    				
	    				//DEBUG Diagnostic
	    				for(ONCVolunteer v : newVolList)
	    				{
	    					System.out.println(String.format("New vol: id=%d, fn=%s, ln=%s",
	    							v.getID(), v.getFirstName(), v.getLastName()));
	    					
	    					for(VolunteerActivity va : v.getActivityList())
	    						System.out.println(String.format("Activity: %s", va.getName()));
	    				}
	    				
	    				for(ONCVolunteer v : updateVolList)
	    				{
	    					System.out.println(String.format("Update vol: id=%d, fn=%s, ln=%s",
	    							v.getID(), v.getFirstName(), v.getLastName()));
	    					
	    					for(VolunteerActivity va : v.getActivityList())
	    						System.out.println(String.format("Activity: %s", va.getName()));
	    				}
	    				
	    				//now that we have a list of new and updated volunteers from the file, send the to the
	    				//server to add to the existing database
	    				//create the request to the server and process the return
		    			Gson gson = new Gson();
		    			Type listtype = new TypeToken<ArrayList<ONCVolunteer>>(){}.getType();
		    			
		    			if(!newVolList.isEmpty())
		    			{
		    				String response = serverIF.sendRequest("POST<new_volunteer_group>" + gson.toJson(newVolList, listtype));
		    			
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
		    							"ONC Server SignUpGenius Import Error", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		    				}
		    			}
		    			if(!updateVolList.isEmpty())
		    			{
		    				String response = serverIF.sendRequest("POST<update_volunteer_group>" + gson.toJson(updateVolList, listtype));
		    			
		    				if(response != null && response.startsWith("UPDATED_VOLUNTEER_GROUP"))
		    				{
		    					//process the list of jsons returned, adding agent, families, adults
		    					//and children to the local databases
		    					Type jsonlisttype = new TypeToken<ArrayList<String>>(){}.getType();
		    					ArrayList<String> changeList = gson.fromJson(response.substring(23), jsonlisttype);
		    		    	
		    					//loop thru list of changes, processing each one
		    					for(String change: changeList)
		    						if(change.startsWith("UPDATED_DRIVER"))
		    						{
		    							this.processUpdatedObject(this, change.substring("UPDATED_DRIVER".length()), volunteerList);
		    							volunteersModifiedCount++;
		    						}
		    				}
		    				else
		    				{
		    					JOptionPane.showMessageDialog(pFrame, "An error occured, " +
		    							volFile.getName() + " cannot be imported by the server", 
		    							"ONC Server SignUpGenius Import Error", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		    				}
		    			}
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pFrame, "Volunteer file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Volunteer File", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pFrame, "Couldn't read header in file: " + volFile.getName(), 
						"Invalid Volunteer File", JOptionPane.ERROR_MESSAGE,GlobalVariablesDB.getONCLogo()); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pFrame, "Unable to open Volunteer file: " + volFile.getName(), 
    				"Volunteer file not found", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
	    	}
	    	finally
	    	{
	    		try 
	    		{
					reader.close();
				} catch (IOException e) 
				{
					JOptionPane.showMessageDialog(pFrame, "Unable to close Volunteer file: " + volFile.getName(), 
		    			"Volunteer File Error", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
				}
	    	}
	    }
	    
	    //create a message describing the result of processing the sign-up genius file
	    if(volFile == null || (volunteersAddedCount + volunteersModifiedCount) == 0)
	    	return "No new or modified volunteer records were found";
	    else
	    	return String.format("%d volunteers added and %d volunteers modified from %s", 
	    			volunteersAddedCount, volunteersModifiedCount, volFile.getName());
	    
	}
*/	
	ONCVolunteer searchVolunteerListForMatch(String fn, String ln, List<ONCVolunteer> volList)
	{
		int index = 0;
		while(index < volList.size() &&
			  !(volList.get(index).getFirstName().equals(fn) && volList.get(index).getLastName().equals(ln)))
			index++;
		
		return index < volList.size() ? volList.get(index) : null;
	}
	
	@SuppressWarnings("unchecked")
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
	
	List<ONCVolunteer> getVolunteersForActivity(Activity va)
	{
		List<ONCVolunteer> volList = new ArrayList<ONCVolunteer>();
		for(ONCVolunteer v : volunteerList)
			if(volActDB.getVolunteerActivity(v.getID(), va.getID()) != null)
				volList.add(v);
		
		return volList;
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
			return volunteerList.get(index).getLastName() + ", " + volunteerList.get(index).getFirstName().charAt(0);
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
			{	
				result = volunteerList.get(index).getLastName();
				if(!volunteerList.get(index).getFirstName().isEmpty())
					result = result.concat(", " + volunteerList.get(index).getFirstName());
			}
		}
		
		return result;	
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCVolunteer>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<drivers>");
				
				
			if(response != null && !response.isEmpty())
			{
				volunteerList = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	List<String> getGroupList()
	{
		List<String> groupList = new ArrayList<String>();
		
		for(ONCVolunteer v : volunteerList)
		{
			int index = 0;
			while(index < groupList.size() && !groupList.get(index).equals(v.getOrganization()))
				index++;
		
			if(index == groupList.size())
				groupList.add(v.getOrganization());
		}
		
		return groupList;
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
		else if(ue.getType().equals("ADDED_WAREHOUSE_VOLUNTEER"))
		{
			Gson gson = new Gson();
			ONCVolunteer addedVol = gson.fromJson(ue.getJson(), ONCVolunteer.class);
			
			fireDataChanged(this, "ADDED_WAREHOUSE_VOLUNTEER", addedVol);
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
				if(d.getFirstName().toLowerCase().contains(data.toLowerCase()) ||
					d.getLastName().toLowerCase().contains(data.toLowerCase()))
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
			return o1.getLastName().compareTo(o2.getLastName());
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

	@Override
	String[] getExportHeader()
	{
		// TODO Auto-generated method stub
		return new  String[] {"Vol ID", "First Name", "Last Name", "House Number", "Street",
	 			"Unit", "City", "Zip", "Email", "Home Phone", "Cell Phone", 
	 			"Driver License", "Car", "# Del. Assigned", "Time Stamp",
	 			"Stoplight Pos", "Stoplight Mssg", "Changed By"};
	}
}
