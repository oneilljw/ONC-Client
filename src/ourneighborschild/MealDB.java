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
		this.title = "Meals";
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
	
	@Override
	List<ONCMeal> getList() { return mealList; }

	List<ONCMeal> getFamilyMealHistory(int famID)
	{
		List<ONCMeal> mealHistoryList = new ArrayList<ONCMeal>();
		
		for(ONCMeal meal:mealList)
			if(meal.getFamilyID() == famID)
				mealHistoryList.add(meal);
	
		return mealHistoryList;
	}
	//Used to create a meal internal to the application via the ONC Server
	ONCMeal add(Object source, ONCObject entity)
	{	
		ONCMeal addedMeal = null;
			
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
	
	//updates a list of meals with changes
	String addMealList(Object source, List<ONCMeal> addMealReqList)
	{		
		//wrap the requested add meal list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfMeals = new TypeToken<ArrayList<ONCMeal>>(){}.getType();
		String response = null, returnResp = "ADD_FAILED";
		response = serverIF.sendRequest("POST<add_list_of_meals>" + gson.toJson(addMealReqList, listOfMeals));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added meal strings, convert them to ONC Meal objects and process the adds.
		//set the return string to indicate the server add list request was successful
		if(response != null && response.startsWith("ADDED_LIST_MEALS"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(16), responseListType);
				
			for(String updatedFamJson : responseList)
			if(updatedFamJson.startsWith("ADDED_MEAL"))
					processAddedMeal(source, updatedFamJson.substring(10));
				
			returnResp = "ADDED_LIST_MEALS";
		}

		return returnResp;
	}
		
	ONCMeal processAddedMeal(Object source, String json)
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
		response = serverIF.sendRequest("POST<update_meal>" + 
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
		//remove the meal from this data base
		Gson gson = new Gson();
		ONCMeal deletedMeal = removeMeal(source, gson.fromJson(json, ONCMeal.class).getID());
		
		if(deletedMeal != null)
			fireDataChanged(source, "DELETED_MEAL", deletedMeal);
		else
			System.out.println("Meal DB: Meal deletion failed, mealID not found");
	}
	
	ONCMeal removeMeal(Object source, int mealID)
	{
		//remove the meal from this data base
		ONCMeal deletedMeal = null;
		
		int index = 0;
		while(index < mealList.size() && mealList.get(index).getID() != mealID)
				index++;
				
		if(index < mealList.size())
		{
			deletedMeal = mealList.get(index);
			mealList.remove(index);
		}
		
		return deletedMeal;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCMeal>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<meals>");
			
			
			if(response != null)
			{
				mealList = gson.fromJson(response, listtype);
			}	bImportComplete = true;
		}
		
		return bImportComplete;
	}
	
	@Override 
	String[] getExportHeader() 
	{
		return new String[] {"Meal ID", "Family ID", "Status", "Type", "Partner ID", "Restrictions", 
	 							"Changed By", "Time Stamp", "SL Pos", "SL Mssg", "SL Changed By"};
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
		else if(ue.getType().equals("ADDED_LIST_MEALS"))
		{
			Gson gson = new Gson();
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> addedMealList = gson.fromJson(ue.getJson(), responseListType);
			for(String addedMealString : addedMealList)
				processAddedMeal(this, addedMealString.substring(10));
		}
		else if(ue.getType().equals("DELETED_MEAL"))
		{
			processDeletedMeal(this, ue.getJson());
		}			
	}
}
