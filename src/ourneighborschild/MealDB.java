package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MealDB extends ONCDatabase
{
	private static MealDB instance = null;
	private List<ONCMeal> mealList;
	
	private MealDB()
	{
		super();
		mealList = new ArrayList<ONCMeal>();
	}
	
	public static MealDB getInstance()
	{
		if(instance == null)
			instance = new MealDB();
		
		return instance;
	}
	
	ONCMeal getMeal(int id)
	{
		int index = 0;
		while(index < mealList.size() && mealList.get(index).getID() != id)
			index++;
		
		if(index == mealList.size())
			return null;	//Meal wasn't found
		else
			return mealList.get(index);
	}

	
	//Used to create a meal internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedMeal = null;
			
		//send add meal request to server
		String response = "";
			
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_meal>" + gson.toJson(entity, ONCMeal.class));		
				
			//if the server added the meal,  add the new meal to the data base and notify ui's
			if(response.startsWith("ADDED_MEAL"))		
				addedMeal =  processAddedMeal(source, response.substring(10));
		}
			
		return addedMeal;
	}
		
	ONCObject processAddedMeal(Object source, String json)
	{
		ONCMeal addedMeal = null;
		Gson gson = new Gson();
		addedMeal = gson.fromJson(json, ONCMeal.class);
			
		if(addedMeal != null)
		{
			mealList.add(addedMeal);
			fireDataChanged(source, "ADDED_MEAL", addedMeal);
		}
			
		return addedMeal;
	}
	

	/***************************************************************
	 * This method is called when a meal has been updated by 
	 * the user. The request update meal object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedMeal)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		ServerIF serverIF = ServerIF.getInstance();
		response = serverIF.sendRequest("POST<update_child>" + 
												gson.toJson(updatedMeal, ONCMeal.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated meal in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_MEAL"))
			processUpdatedMeal(source, response.substring(12));
		
		return response;
	}
	
	void processUpdatedMeal(Object source, String json)
	{
		//Create a ONCMeal object for the updated meal
		Gson gson = new Gson();
		ONCMeal updatedMeal = gson.fromJson(json, ONCMeal.class);
		
		//Find the position for the current meal being updated
		int index = 0;
		while(index < mealList.size() && mealList.get(index).getID() != updatedMeal.getID())
			index++;
		
		//Replace the current ONCMeal object with the update
		if(index < mealList.size())
		{
			mealList.set(index, updatedMeal);
			fireDataChanged(source, "UPDATED_MEAL", updatedMeal);
		}
		else
			System.out.println(String.format("Meal DB processUpdatedMeal - meal id %d not found",
					updatedMeal.getID()));
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete a meal. The first step in deletion
	 * is to confirm with the user that they intended to delete the meal. 
	 **********************************************************************************************************/
	void delete(Object source, ONCMeal reqDelMeal)
	{
		String response = "";
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_meal>" + gson.toJson(reqDelMeal, ONCMeal.class));		
			
			//if the server deleted the meal, notify ui's
			if(response.startsWith("DELETED_MEAL"))		
				processDeletedMeal(source, response.substring(12));
		}
	}
	
	void processDeletedMeal(Object source, String json)
	{
		Gson gson = new Gson();
		ONCMeal deletedMeal = gson.fromJson(json, ONCMeal.class);
				
		//remove the meal from this data base
		int index = 0;
		while(index < mealList.size() && mealList.get(index).getID() != deletedMeal.getID())
			index++;
		
		if(index < mealList.size())
		{
			mealList.remove(index);
			fireDataChanged(source, "DELETED_MEAL", deletedMeal);
		}
		else
			System.out.println("Meal DB: Meal deletion failed, mealID not found");
	}
	
	String importDB()
	{
		String response = "NO_MEALS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCMeal>>(){}.getType();
			
			response = serverIF.sendRequest("GET<meals>");
			mealList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_MEALS"))
			{
				response =  "MEALS_LOADED";
			}	fireDataChanged(this, "LOADED_MEALS", null);
		}
		
		return response;
	}

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_MEAL"))
		{
			processUpdatedMeal(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_MEAL"))
		{
			processAddedMeal(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_MEAL"))
		{
			processDeletedMeal(this, ue.getJson());
		}			

	}

}
