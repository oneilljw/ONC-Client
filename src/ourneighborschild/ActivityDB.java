package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActivityDB extends ONCSearchableDatabase 
{
	private static final EntityType DB_TYPE = EntityType.ACTIVITY;
	private static ActivityDB instance = null;
	private List<Activity> activityList;
	private List<String> categoryList;
	private GeniusSignUps geniusSignUps;
	
	private ActivityDB()
	{
		super(DB_TYPE);
		this.title = "Activities";
		activityList = new ArrayList<Activity>();
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
	
	Activity getActivity(int id)
	{
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != id)
			index++;
		
		if(index < activityList.size())
			return activityList.get(index);
		else
			return null;
	}
	
	List<Activity> getDeliveryActivities()
	{
		List<Activity> delActList = new ArrayList<Activity>();
		for(Activity a : activityList)
			if(a.isDeliveryActivity())
				delActList.add(a);
		
		return delActList;	
	}
	
	/***
	 * Searches activity list for a match. 
	 * @param activity
	 * @param startDate
	 * @param endDate
	 * @param endTime
	 * @return
	 */
	Activity getActivity(String activity, long startDate,  long endDate)
	{
		//search for matching activity. A match requires the activity name, start date/time and
		//end date/time to all be equal
		int index = 0;
		while(index < activityList.size() && 
				!(activityList.get(index).getName().equals(activity) &&
				   activityList.get(index).getStartDate() == startDate &&
				    activityList.get(index).getEndDate() == endDate))
		{
			index++;
		}
		
		return index < activityList.size() ? activityList.get(index) : null;
	}
	
	//creates a list of volunteer activities based on stored string of activity ID's 
	//separated by the '_' character.
	List<Activity> createActivityList(String zActivities)
	{
		List<Activity> volActList = new LinkedList<Activity>();
			
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
			for(Activity va:activityList)
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
	Activity getObjectAtIndex(int index) { return activityList.get(index); }

	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "UPDATE_FAILED";
		
		response = serverIF.sendRequest("POST<update_activity>" + 
											gson.toJson(entity, Activity.class));
		
		if(response != null && response.startsWith("UPDATED_ACTIVITY"))
			processUpdatedObject(source, response.substring(16));
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		Gson gson = new Gson();
		Activity updatedObj = gson.fromJson(json, Activity.class);
		
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
		geniusSignUps = gson.fromJson(signUpsJson, GeniusSignUps.class);

		if(geniusSignUps != null)
			fireDataChanged(this, "UPDATED_GENIUS_SIGNUPS", geniusSignUps);
	}
	
	String updateGeniusSignUps(Object source, GeniusSignUps reqUpdateGSU)
	{
		Gson gson = new Gson();
		String response = "UPDATE_FAILED";
		
		response = serverIF.sendRequest("POST<update_genius_signups>" + 
											gson.toJson(reqUpdateGSU, GeniusSignUps.class));
		
		if(response != null && response.startsWith("UPDATED_GENIUS_SIGNUPS"))
			processUpdatedGeniusSignUps(source, response.substring(22));
		
		return response;
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
	
	@Override
	boolean importDB()
	{
		String response = "NO_ACTIVITIES";
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<Activity>>(){}.getType();
			
			response = serverIF.sendRequest("GET<activities>");
			activityList = gson.fromJson(response, listtype);
			if(!response.startsWith("NO_ACTIVITIES"))
			{
				response =  "ACTIVITIES_LOADED";
//				fireDataChanged(this, "LOADED_ACTIVITIES", null);
				
				bImportComplete = true;
				
				for(Activity va : activityList)
					if(!isCategoryInList(va.getCategory()))
						categoryList.add(va.getCategory());
				
//				fireDataChanged(this, "LOADED_CATEGORIES", null);
			}
			
			//import the sign ups
			response = serverIF.sendRequest("GET<genius_signups>");
			if(response != null)
			{
				geniusSignUps = gson.fromJson(response, GeniusSignUps.class);		
				fireDataChanged(this, "LOADED_SIGNUPS", geniusSignUps);
			}
		}
		
		return bImportComplete;
	}
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_activity>" + 
											gson.toJson(entity, Activity.class));
		
		if(response.startsWith("ADDED_ACTIVITY"))
			processAddedObject(source, response.substring(14));
		
		return response;	
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added activity in local data base
		Gson gson = new Gson();
		Activity addedActivity = gson.fromJson(json, Activity.class);
		activityList.add(addedActivity);

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
											gson.toJson(entity, Activity.class));
		
		
		if(response.startsWith("DELETED_ACTIVITY"))
			processDeletedObject(source, response.substring(16));
		
		return response;
	}
	
	void processDeletedObject(Object source, String json)
	{
		//remove deleted activity in local data base
		Gson gson = new Gson();
		Activity deletedAct = gson.fromJson(json, Activity.class);
		
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
	
	String requestGeniusSignUps()
	{
		return serverIF.sendRequest("GET<request_signups>");
	}
	
	@Override
	String[] getExportHeader()
	{
		return new String[] {"ID", "Genius ID", "Category" ,"Name","StartTimeMillis",
	 			"EndTimeMillis", "Location", "Description", "Open", "Notify", "Del Act?", 
	 			"Timestamp", "Changed By", "SL Pos","SL Message", "SL Changed By"};
	}
}
