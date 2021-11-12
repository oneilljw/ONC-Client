package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DistributionCenterDB extends ONCDatabase
{
	private static DistributionCenterDB instance = null;
	private List<DistributionCenter> centerList;
	
	private DistributionCenterDB()
	{
		super();
		this.title = "Distribution Centers";
		centerList = new ArrayList<DistributionCenter>();
	}
	
	public static DistributionCenterDB getInstance()
	{
		if(instance == null)
			instance = new DistributionCenterDB();
		
		return instance;
	}
	
	@Override
	List<DistributionCenter> getList() { return centerList; }
	
	DistributionCenter getDistributionCenter(int id)
	{
		int index = 0;
		while(index < centerList.size() && centerList.get(index).getID() != id)
			index++;
		
		if(index == centerList.size())
			return null;	//center not found
		else
			return centerList.get(index);
	}
	
	//Used to create a Distribution Center internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedCenter = null;
				
		//send add Pickup Location request to server
		String response = "";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_center>" + gson.toJson(entity, DistributionCenter.class));
				
			//if the server added the location,  add the new center to the data base and notify ui's
			if(response.startsWith("ADDED_CENTER"))		
				addedCenter = processAddedDistributionCenter(source, response.substring(12));
		}
				
		return addedCenter;
	}
			
	ONCObject processAddedDistributionCenter(Object source, String json)
	{
		DistributionCenter addedLocation = null;
		Gson gson = new Gson();
		addedLocation = gson.fromJson(json, DistributionCenter.class);
			
		if(addedLocation!= null)
		{
			centerList.add(addedLocation);
			fireDataChanged(source, "ADDED_CENTER", addedLocation);
		}
			
		return addedLocation;
	}
	
	/***************************************************************
	 * This method is called when a pickup location has been updated by 
	 * the user. The request updated object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedLocation)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_center>" + 
												gson.toJson(updatedLocation, DistributionCenter.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated location in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_CENTER"))
			processUpdatedDistributionCenter(source, response.substring(14));
		
		return response;
	}
	
	void processUpdatedDistributionCenter(Object source, String json)
	{
		//Create an object for the updated center
		Gson gson = new Gson();
		DistributionCenter updatedCenter = gson.fromJson(json, DistributionCenter.class);
		
		//Find the position for the current center being updated
		int index = 0;
		while(index < centerList.size() && centerList.get(index).getID() != updatedCenter.getID())
			index++;
		
		//Replace the current object with the update
		if(index < centerList.size())
		{
			centerList.set(index, updatedCenter);
			fireDataChanged(source, "UPDATED_CENTER", updatedCenter);
		}
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete a distribution center
	 *******************************************************************************************/
	DistributionCenter delete(Object source, DistributionCenter reqDelLocation)
	{
		String response = "";
		DistributionCenter deletedLocation = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_center>" + gson.toJson(reqDelLocation, DistributionCenter.class));		
			
			//if the server deleted the center, notify ui's
			if(response.startsWith("DELETED_CENTER"))		
				deletedLocation = processDeletedDistributionCenter(source, response.substring(14));
		}
		
		return deletedLocation;
	}
	
	DistributionCenter processDeletedDistributionCenter(Object source, String json)
	{
		//remove the center from this local data base
		Gson gson = new Gson();
		DistributionCenter deletedCenter = removeDistributionCenter(source, gson.fromJson(json, DistributionCenter.class).getID());
		
		if(deletedCenter != null)
			fireDataChanged(source, "DELETED_CENTER", deletedCenter);
		
		return deletedCenter;
	}
	
	DistributionCenter removeDistributionCenter(Object source, int locationID)
	{
		//remove the center from this data base
		DistributionCenter deletedCenter = null;
		
		int index = 0;
		while(index < centerList.size() && centerList.get(index).getID() != locationID)
				index++;
				
		if(index < centerList.size())
		{
			deletedCenter = centerList.get(index);
			centerList.remove(index);	
		}
		
		return deletedCenter;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<DistributionCenter>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<distribution_centers>");
			
			
			if(response != null && !response.startsWith("NO_CENTERS"))
			{
				centerList = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_CENTER"))
		{
			processUpdatedDistributionCenter(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_CENTER"))
		{
			processAddedDistributionCenter(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_CENTER"))
		{
			processDeletedDistributionCenter(this, ue.getJson());
		}			
	}

	@Override
	String[] getExportHeader()
	{
		return new String[] {"ID", "Name", "Acronym", "Street #", "Street", "Suffix", "City", "Zipcode","Google Map URL"};
	}

}
