package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActivityDB extends ONCSearchableDatabase 
{
	private static final EntityType DB_TYPE = EntityType.ACTIVITY;
	private static ActivityDB instance = null;
	private List<VolunteerActivity> activityList;
	private List<String> categoryList;
//	private List<SignUp> signUpList;
	private GeniusSignUps geniusSignUps;
	
	private ActivityDB()
	{
		super(DB_TYPE);
		activityList = new ArrayList<VolunteerActivity>();
		categoryList = new ArrayList<String>();
		geniusSignUps = new GeniusSignUps();
	}
	
	public static ActivityDB getInstance()
	{
		if(instance == null)
			instance = new ActivityDB();
		
		return instance;
	}
	
	@Override
	public void dataChanged(ServerEvent ue) 
	{
		if(ue.getType().equals("UPDATED_ACTIVITY"))
		{
			processUpdatedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_ACTIVITY"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_ACTIVITY"))
		{
			processDeletedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_SIGNUP"))
		{
			processUpdatedSignUp(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_GENIUS_SIGNUPS"))
		{
			processUpdatedGeniusSignUps(this, ue.getJson());
		}
	}
	
	VolunteerActivity getActivity(int id)
	{
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != id)
			index++;
		
		if(index < activityList.size())
			return activityList.get(index);
		else
			return null;
	}
	
	/***
	 * Searches activity list for a match. 
	 * @param activity
	 * @param startDate
	 * @param endDate
	 * @param endTime
	 * @return
	 */
	VolunteerActivity getActivity(String activity, String startDate,  String endDate)
	{
		//convert the startDate and endDate strings to calendar objects
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy hh:mm a");
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		
		//convert the parameter start and end date/time strings and create Calendar objects
		try
		{
			startCal.setTime(sdf.parse(startDate));
			endCal.setTime(sdf.parse(endDate));
		}
		catch (ParseException e)
		{
			return null;	//couldn't parse the input compare parameters so return null activity
		}
		
		//search for matching activity. A match requires the activity name, start date/time and
		//end date/time to all be equal
		int index = 0;
		while(index < activityList.size() && 
				!(activityList.get(index).getName().equals(activity) &&
				   activityList.get(index).getStartCal().equals(startCal) &&
				    activityList.get(index).getEndCal().equals(endCal)))
		{
			index++;
		}
		
		return index < activityList.size() ? activityList.get(index) : null;
		
//		if(index < activityList.size())
//		{
//			System.out.println(String.format("ActDB.getAct: found act id %d", activityList.get(index).getID()));
//			return activityList.get(index);
//		}
//		else
//		{
//			System.out.println(String.format("ActDB.getAct: didn't find act"));
//			return null;
//		}
	}
	
	//creates a list of volunteer activities based on stored string of activity ID's 
	//separated by the '_' character.
	List<VolunteerActivity> createActivityList(String zActivities)
	{
		List<VolunteerActivity> volActList = new LinkedList<VolunteerActivity>();
			
		String[] activityParts = zActivities.split("_");
		for(String zActivity : activityParts)
		{
			int index = 0;
			while(index < activityList.size() && activityList.get(index).getID() != Integer.parseInt(zActivity))
				index++;
			
			if(index < activityList.size())
				volActList.add(activityList.get(index));
		}
			
		return volActList;
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data) 
	{
		searchAL.clear();
		String searchType = "";

    	if(!data.isEmpty() && isNumeric(data))
    	{
    		//If a numeric string, then search for Driver Number match, else search for last name match
    		searchType = "Activity #";
    		if(data.matches("-?\\d+(\\.\\d+)?"))
    		{
    			int index = 0;
    			while(index < activityList.size() && activityList.get(index).getID() != Integer.parseInt(data))
    				index++;
    			
    			if(index < activityList.size())
    				searchAL.add(activityList.get(index).getID());
    		}
    	}
    	else	//search for activity name
    	{
    		searchType = "Activities containing";
			for(VolunteerActivity va:activityList)
			{
				if(va.getName().toLowerCase().contains(data.toLowerCase()) ||
					va.getDescription().toLowerCase().contains(data.toLowerCase()) ||
					 va.getLocation().toLowerCase().contains(data.toLowerCase()))
				{
					searchAL.add(va.getID());
				}
			}
    	}
    	
		return searchType;
	}

	@Override
	int size() 
	{
		return activityList.size();
	}

	@Override
	List<? extends ONCEntity> getList() { return activityList; }
	
	GeniusSignUps getSignUps() { return geniusSignUps; }
	
	//implementation of abstract classes
	VolunteerActivity getObjectAtIndex(int index) { return activityList.get(index); }

	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "UPDATE_FAILED";
		
		response = serverIF.sendRequest("POST<update_activity>" + 
											gson.toJson(entity, VolunteerActivity.class));
		
		if(response != null && response.startsWith("UPDATED_ACTIVITY"))
			processUpdatedObject(source, response.substring(16));
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		Gson gson = new Gson();
		VolunteerActivity updatedObj = gson.fromJson(json, VolunteerActivity.class);
		
		//store updated object in local data base
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != updatedObj.getID())
			index++;
		
		if(index < activityList.size())
		{
			activityList.set(index, updatedObj);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_ACTIVITY", updatedObj);
			
			//check to see if added activity category is in category list. If not, add it and notify
			if(!isCategoryInList(updatedObj.getCategory()))
			{
				categoryList.add(updatedObj.getCategory());
				fireDataChanged(this, "UPDATED_CATEGORIES", null);
			}
		}
	}
	
	void processUpdatedGeniusSignUps(Object source, String signUpsJson)
	{
		Gson gson = new Gson();
//		Type listtype = new TypeToken<ArrayList<SignUp>>(){}.getType();
		
		geniusSignUps = gson.fromJson(signUpsJson, GeniusSignUps.class);

		if(geniusSignUps != null)
			fireDataChanged(this, "UPDATED_GENIUS_SIGNUPS", geniusSignUps);
	}
	
	String updateSignUp(Object source, SignUp reqUpdateSignUp)
	{
		Gson gson = new Gson();
		String response = "UPDATE_FAILED";
		
		response = serverIF.sendRequest("POST<update_signup>" + 
											gson.toJson(reqUpdateSignUp, SignUp.class));
		
		if(response != null && response.startsWith("UPDATED_SIGNUP"))
			processUpdatedSignUp(source, response.substring(14));
		
		return response;
	}
	
	void processUpdatedSignUp(Object source, String signUpJson)
	{
		Gson gson = new Gson();
		SignUp updatedSignUp = gson.fromJson(signUpJson, SignUp.class);
		
		//find the signUp in the current list and replace it with the update
		int index = 0;
		List<SignUp> signUpList = geniusSignUps.getSignUpList();
		while(index < signUpList.size() && signUpList.get(index).getSignupid() != updatedSignUp.getSignupid())
			index++;
		
		if(index < signUpList.size())
			signUpList.set(index, updatedSignUp);
		
		if(updatedSignUp != null)
			fireDataChanged(this, "UPDATED_SIGNUP", updatedSignUp);
	}
	
	String importDatabase()
	{
		String response = "NO_ACTIVITIES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<VolunteerActivity>>(){}.getType();
			
			response = serverIF.sendRequest("GET<activities>");
			activityList = gson.fromJson(response, listtype);

			if(!response.startsWith("NO_ACTIVITIES"))
			{
				response =  "ACTIVITIES_LOADED";
				fireDataChanged(this, "LOADED_ACTIVITIES", null);
				
				for(VolunteerActivity va : activityList)
				{
					if(!isCategoryInList(va.getCategory()))
						categoryList.add(va.getCategory());
					
					fireDataChanged(this, "UPDATED_CATEGORIES", null);
				}
			}
			
			//import the sign ups
//			listtype = new TypeToken<ArrayList<SignUp>>(){}.getType();
			
			response = serverIF.sendRequest("GET<genius_signups>");
			if(response != null)
			{
				geniusSignUps = gson.fromJson(response, GeniusSignUps.class);
				
//				for(SignUp su : geniusSignUps.getSignUpList())
//					System.out.println(String.format("ActDB.importDB: SignUp Title %s, id= %d, endtime= %d, freq= %s",
//							su.getTitle(), su.getSignupid(), su.getEndtime(), su.getFrequency()));
				
				fireDataChanged(this, "LOADED_SIGNUPS", geniusSignUps);
			}
		}
		
		return response;
	}
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_activity>" + 
											gson.toJson(entity, VolunteerActivity.class));
		
		if(response.startsWith("ADDED_ACTIVITY"))
			processAddedObject(source, response.substring(14));
		
		return response;	
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added activity in local data base
		Gson gson = new Gson();
		VolunteerActivity addedActivity = gson.fromJson(json, VolunteerActivity.class);
		activityList.add(addedActivity);
//		System.out.println(String.format("DriverDB processAddedDriver: Driver Added ID: %d",
//				addedDriver.getID()));
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_ACTIVITY", addedActivity);
		
		//check to see if added activity category is in category list. If not, add it and notify
		if(!isCategoryInList(addedActivity.getCategory()))
		{
			categoryList.add(addedActivity.getCategory());
			fireDataChanged(this, "UPDATED_CATEGORIES", null);
		}
	}
	
	String delete(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_activity>" + 
											gson.toJson(entity, VolunteerActivity.class));
		
		
		if(response.startsWith("DELETED_ACTIVITY"))
			processDeletedObject(source, response.substring(16));
		
		return response;
	}
	
	void processDeletedObject(Object source, String json)
	{
		//remove deleted activity in local data base
		Gson gson = new Gson();
		VolunteerActivity deletedAct = gson.fromJson(json, VolunteerActivity.class);
		
		int index=0;
		while(index < activityList.size() && activityList.get(index).getID() != deletedAct.getID())
			index++;
		
		//If deleted activity was found, remove it and notify ui's
		if(index < activityList.size())
		{
			activityList.remove(index);
			fireDataChanged(source, "DELETED_ACTIVITY", deletedAct);
		}
	}
	
	String[] getActivityCategoryList()
	{
		String[] catArr = new String[categoryList.size()];
		return categoryList.toArray(catArr);
	}
	
	boolean isCategoryInList(String category)
	{
		int index = 0;
		while(index < categoryList.size() && !categoryList.get(index).equals(category))
			index++;
			
		return index < categoryList.size();
	}
	
	String requestGeniusSignUps(SignUpStatus status)
	{
		Gson gson = new Gson();
		return serverIF.sendRequest("GET<request_signups>"+ gson.toJson(status, SignUpStatus.class));
	}
	
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
		if(filename == null)
		{
    			ONCFileChooser fc = new ONCFileChooser(pf);
    			oncwritefile= fc.getFile("Select .csv file to save Activity DB to",
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
	    			String[] header = {"ID", "Category" ,"Name", "Start Time", "End Time", 
		 				  			"Location", "Description"};
	    		
	    			CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    			writer.writeNext(header);
	    	    
	    			for(VolunteerActivity va:activityList)
	    				writer.writeNext(va.getExportRow());	//Get activity data
	    	 
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
}
