package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BatteryDB extends ONCDatabase
{
	private static BatteryDB instance = null;
	private List<Battery> batteryList;
	
	private BatteryDB()
	{
		super();
		this.title = "Batteries";
		batteryList = new ArrayList<Battery>();
	}
	
	public static BatteryDB getInstance()
	{
		if(instance == null)
			instance = new BatteryDB();
		
		return instance;
	}
	
	@Override
	List<Battery> getList() { return batteryList; }
	
	Battery getBattery(int id)
	{
		int index = 0;
		while(index < batteryList.size() && batteryList.get(index).getID() != id)
			index++;
		
		if(index == batteryList.size())
			return null;		//battery not found
		else
			return batteryList.get(index);
	}
	
	//Used to create a battery internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedBattery = null;
				
		//send add battery request to server
		String response = "";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_battery>" + gson.toJson(entity, Battery.class));
				
			//if the server added the battery,  add the new battery to the data base and notify ui's
			if(response.startsWith("ADDED_BATTERY"))		
				addedBattery = processAddedBattery(source, response.substring(13));
		}
				
		return addedBattery;
	}
			
	ONCObject processAddedBattery(Object source, String json)
	{
		Battery addedBattery = null;
		Gson gson = new Gson();
		addedBattery = gson.fromJson(json, Battery.class);
			
		if(addedBattery != null)
		{
			batteryList.add(addedBattery);
			fireDataChanged(source, "ADDED_BATTERY", addedBattery);
		}
			
		return addedBattery;
	}
	
	/***************************************************************
	 * This method is called when an adult has been updated by 
	 * the user. The request updated adult object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedBattery)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_battery>" + 
												gson.toJson(updatedBattery, Battery.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated battery in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_BATTERY"))
			processUpdatedBattery(source, response.substring(15));
		
		return response;
	}
	
	void processUpdatedBattery(Object source, String json)
	{
		//Create a Battery object for the updated adult
		Gson gson = new Gson();
		Battery updatedBattery = gson.fromJson(json, Battery.class);
		
		//Find the position for the current battery being updated
		int index = 0;
		while(index < batteryList.size() && batteryList.get(index).getID() != updatedBattery.getID())
			index++;
		
		//Replace the current object with the update
		if(index < batteryList.size())
		{
			batteryList.set(index, updatedBattery);
			fireDataChanged(source, "UPDATED_BATTERY", updatedBattery);
		}
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete a battery.
	 **********************************************************************************************************/
	Battery delete(Object source, Battery reqDelBattery)
	{
		String response = "";
		Battery deletedBattery = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_battery>" + gson.toJson(reqDelBattery, Battery.class));		
			
			//if the server deleted the battery, notify ui's
			if(response.startsWith("DELETED_BATTERY"))		
				deletedBattery = processDeletedBattery(source, response.substring(15));
		}
		
		return deletedBattery;
	}
	
	Battery processDeletedBattery(Object source, String json)
	{
		//remove the battery from this local data base
		Gson gson = new Gson();
		Battery deletedBattery = removeBattery(source, gson.fromJson(json, Battery.class).getID());
		
		if(deletedBattery != null)
			fireDataChanged(source, "DELETED_BATTERY", deletedBattery);
		
		return deletedBattery;
	}
	
	Battery removeBattery(Object source, int batteryID)
	{
		//remove the battery from this data base
		Battery deletedBattery = null;
		
		int index = 0;
		while(index < batteryList.size() && batteryList.get(index).getID() != batteryID)
				index++;
				
		if(index < batteryList.size())
		{
			deletedBattery = batteryList.get(index);
			batteryList.remove(index);	
		}
		
		return deletedBattery;
	}
	
	List<Battery> getBatteryListForGift(int childID, int wn)
	{
		List<Battery> giftBatteryList = new ArrayList<Battery>();
		for(Battery battery : batteryList)
			if(battery.getChildID() == childID && battery.getWishNum() == wn)
				giftBatteryList.add(battery);
		
		return giftBatteryList;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<Battery>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<batteries>");
			
			
			if(response != null && !response.startsWith("NO_BATTERIES"))
			{
				batteryList = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_BATTERY"))
		{
			processUpdatedBattery(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_BATTERY"))
		{
			processAddedBattery(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_BATTERY"))
		{
			processDeletedBattery(this, ue.getJson());
		}			
	}

	@Override
	String[] getExportHeader()
	{
		return new String[] {"ID", "Child ID", "Wish #", "Size", "Quantity"};
	}
}
