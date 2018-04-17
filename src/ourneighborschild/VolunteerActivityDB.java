package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VolunteerActivityDB extends ONCDatabase
{
	private static VolunteerActivityDB instance = null;
	private List<VolAct> volunteerActivityList;
	
	private VolunteerActivityDB()
	{
		super();
		volunteerActivityList = new ArrayList<VolAct>();
	}
	
	public static VolunteerActivityDB getInstance()
	{
		if(instance == null)
			instance = new VolunteerActivityDB();
		
		return instance;
	}
	
	List<VolAct> getList() { return volunteerActivityList; }
	
	List<VolAct> getVolunteerActivityList(int volID)
	{
		List<VolAct> vaList = new ArrayList<VolAct>();
		
		for(VolAct va : volunteerActivityList)
			if(va.getVolID() == volID)
				vaList.add(va);
		
		return vaList;
	}
	
	int getVolunteerCountForActivity(int actID)
	{
		int count = 0;
		for(VolAct va : volunteerActivityList)
			if(va.getActID() == actID)
				count++;
		
		return count;
	}
	
	VolAct getVolunteerActivity(int volID, int actID)
	{
		int index = 0;
		while(index < volunteerActivityList.size() && !(volunteerActivityList.get(index).getVolID() == volID &&
			volunteerActivityList.get(index).getActID() == actID))
			index++;
		
		return index < volunteerActivityList.size() ? volunteerActivityList.get(index) : null;
	}
	
	String importDB()
	{
		String response = "NO_VOLUNTEER_ACTIVITIES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<VolAct>>(){}.getType();
			
			response = serverIF.sendRequest("GET<volunteer_activities>");
			volunteerActivityList = gson.fromJson(response, listtype);
			
			if(response != null && !response.isEmpty())
			{
				response =  "VOLUNTEER_ACTIVITIES_LOADED";
			}	fireDataChanged(this, "LOADED_VOLUNTEER_ACTIVITIES", null);
		}
		
		return response;
	}
	
	String add(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_volunteer_activity>" + 
											gson.toJson(entity, VolAct.class));
		
		if(response.startsWith("ADDED_VOLUNTEER_ACTIVITY"))
			processAddedObject(source, response.substring(24));
		
		return response;	
	}
	
	String addVolActGroup(Object source, List<VolAct> volActList)
	{
		Gson gson = new Gson();
		Type listtype = new TypeToken<ArrayList<VolAct>>(){}.getType();
			
		String response = gson.toJson(volActList, listtype);
		
		response = serverIF.sendRequest("POST<volunteer_activity_group>" + gson.toJson(volActList, listtype));
		
		return response;
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added organization in local data base
		Gson gson = new Gson();
		VolAct addedVolAct = gson.fromJson(json, VolAct.class);
		volunteerActivityList.add(addedVolAct);
//		System.out.println(String.format("DriverDB processAddedDriver: Driver Added ID: %d",
//				addedDriver.getID()));
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_VOLUNTEER_ACTIVITY", addedVolAct);
	}
	
	String delete(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_volunteer_activity>" + 
											gson.toJson(entity, VolAct.class));
		
		
		if(response.startsWith("DELETED_VOLUNTEER_ACTIVITY"))
			processDeletedObject(source, response.substring(26));
		
		return response;
	}
	
	void processDeletedObject(Object source, String json)
	{
		//remove deleted organization in local data base
		Gson gson = new Gson();
		VolAct deletedObj = gson.fromJson(json, VolAct.class);
		
		int index=0;
		while(index < volunteerActivityList.size() && 
				volunteerActivityList.get(index).getID() != deletedObj.getID())
			index++;
		
		//If deleted volunteer activity was found, remove it and notify ui's
		if(index < volunteerActivityList.size())
		{
			volunteerActivityList.remove(index);
			fireDataChanged(source, "DELETED_VOLUNTEER_ACTIVITY", deletedObj);
		}
	}

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("ADDED_VOLUNTEER_ACTIVITY"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_VOLUNTEER_ACTIVITY"))
		{
			processDeletedObject(this, ue.getJson());
		}
	}

	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_volunteer_activity>" + 
											gson.toJson(entity, VolAct.class));
		
		if(response.startsWith("UPDATED_VOLUNTEER_ACTIVITY"))
		{
			processUpdatedObject(source, response.substring(26));
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		Gson gson = new Gson();
		VolAct updatedObj = gson.fromJson(json, VolAct.class);
		
		//store updated object in local data base
		int index = 0;
		while(index < volunteerActivityList.size() && volunteerActivityList.get(index).getID() != updatedObj.getID())
			index++;
		
		if(index < volunteerActivityList.size())
		{
			volunteerActivityList.set(index,  updatedObj);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_VOLUNTEER_ACTIVITY", updatedObj);
		}
	}

}
