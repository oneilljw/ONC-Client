package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MealDB extends ONCDatabase
{
	private static MealDB instance = null;
//	private List<ONCMeal> mealList;
	private Map<Integer,List<ONCMeal>> mealMap;
	private Comparator<ONCMeal> mealDateChangedComparator;
	private Comparator<ONCMeal> mealTimestampComparator;
	
	private MealDB()
	{
		super();
		this.title = "Meals";
//		mealList = new ArrayList<ONCMeal>();
		mealMap = new HashMap<Integer, List<ONCMeal>>();
		mealDateChangedComparator = new MealDateChangedComparator();
		mealTimestampComparator = new MealTimestampComparator();
	}
	
	public static MealDB getInstance()
	{
		if(instance == null)
			instance = new MealDB();
		
		return instance;
	}
/*	
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
*/	
	ONCMeal getMeal(int famid, int id)
	{
		List<ONCMeal> famMealList = mealMap.get(famid);
		
		int index = 0;
		while(index < famMealList.size() && famMealList.get(index).getID() != id)
			index++;
		
		if(index == famMealList.size())
			return null;	//Meal wasn't found
		else
			return famMealList.get(index);
	}
/*	
	ONCMeal getFamiliesCurrentMeal(int familyID)
	{
		int index = mealList.size() -1;
		
		//Search from the bottom of the data base for speed. New meals are added to the bottom
		while (index >= 0 && (mealList.get(index).getFamilyID() != familyID || mealList.get(index).getNextID() != -1))
			index--;
		
		if(index == -1)
			return null;	//gift wasn't found in data base
		else
			return mealList.get(index);
	}
*/	
	ONCMeal getFamiliesCurrentMeal(int familyID)
	{
		List<ONCMeal> famMealList = mealMap.get(familyID);
		if(famMealList != null && !famMealList.isEmpty())
		{
			//sort the family meal list by time stamp and return the newest meal
			Collections.sort(famMealList,mealTimestampComparator);
			return famMealList.get(famMealList.size()-1);
		}
		else
			return null;
	}
	
	@Override
//	List<ONCMeal> getList() { return mealList; }	
	List<ONCMeal> getList()
	{ 
		List<ONCMeal> allMealsList = new ArrayList<ONCMeal>();
		for(Map.Entry<Integer,List<ONCMeal>> entry : mealMap.entrySet())
			for(ONCMeal m : entry.getValue())
				allMealsList.add(m);
		
		return allMealsList;
	}
/*
	List<ONCMeal> getFamilyMealHistory(int famID)
	{
		List<ONCMeal> mealHistoryList = new ArrayList<ONCMeal>();
		
		for(ONCMeal meal:mealList)
			if(meal.getFamilyID() == famID)
				mealHistoryList.add(meal);
		
		Collections.sort(mealHistoryList, mealDateChangedComparator);
		return mealHistoryList;
	}
*/	
	List<ONCMeal> getFamilyMealHistory(int famID)
	{	
		Collections.sort(mealMap.get(famID), mealDateChangedComparator);
		return mealMap.get(famID);
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
/*		
	ONCMeal processAddedMeal(Object source, String json)
	{
		ONCMeal addedMeal = null;
		Gson gson = new Gson();
		addedMeal = gson.fromJson(json, ONCMeal.class);
		
		//update prior meal linked list pointers
		if(addedMeal != null)
		{
			if(addedMeal.getPriorID() != -1)
			{
				//there is a prior meal
				ONCMeal priorMeal = getMeal(addedMeal.getPriorID());
				priorMeal.setNextID(addedMeal.getID());
				fireDataChanged(source, "UPDATED_MEAL", priorMeal);
			}
			
			mealList.add(addedMeal);
			fireDataChanged(source, "ADDED_MEAL", addedMeal);
		}	
		return addedMeal;
	}
*/	
	ONCMeal processAddedMeal(Object source, String json)
	{
		ONCMeal addedMeal = null;
		Gson gson = new Gson();
		addedMeal = gson.fromJson(json, ONCMeal.class);
		
		if(addedMeal != null)
		{
			int familyID = addedMeal.getFamilyID();
			if(!mealMap.containsKey(familyID))
				mealMap.put(familyID, new ArrayList<ONCMeal>());
			
			mealMap.get(addedMeal.getFamilyID()).add(addedMeal);
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
/*	
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
*/	
	void processUpdatedMeal(Object source, String json)
	{
		//Create a ONCMeal object for the updated meal
		Gson gson = new Gson();
		ONCMeal updatedMeal = gson.fromJson(json, ONCMeal.class);
		
		//retrieve the family's meal list from the map
		List<ONCMeal> famMealList = mealMap.get(updatedMeal.getFamilyID());
		
		//Find the position for the current meal being updated
		int index = 0;
		while(index < famMealList.size() && famMealList.get(index).getID() != updatedMeal.getID())
			index++;
		
		//Replace the current ONCMeal object with the update
		if(index < famMealList.size())
		{
			famMealList.set(index, updatedMeal);
			fireDataChanged(source, "UPDATED_MEAL", updatedMeal);
		}
		else
			System.out.println(String.format("Meal DB processUpdatedMeal - meal id %d not found",
					updatedMeal.getID()));
	}
	
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type mapOfMeals = new TypeToken<HashMap<Integer,List<ONCMeal>>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<meals>");
			if(response != null)
			{
				mealMap = gson.fromJson(response, mapOfMeals);
			}	bImportComplete = true;
		}
		
		return bImportComplete;
	}
	
	@Override 
	String[] getExportHeader() 
	{
		return new String[] {"Meal ID", "Family ID", "Status", "Type", "Partner ID", "Restrictions", 
	 							"Changed By", "Time Stamp", "SL Pos", "SL Mssg", "SL Changed By", "Prior ID", "Next ID"};
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
	}
	
	private class MealDateChangedComparator implements Comparator<ONCMeal>
	{
		@Override
		public int compare(ONCMeal o1, ONCMeal o2)
		{
			return o2.getTimestampDate().compareTo(o1.getTimestampDate());
		}
	}
	
	private class MealTimestampComparator implements Comparator<ONCMeal>
	{
		@Override
		public int compare(ONCMeal m1, ONCMeal m2)
		{
			if(m1.getTimestamp() < m2.getTimestamp())
				return -1;
			else if(m1.getTimestamp() == m2.getTimestamp())
				return 0;
			else
				return 1;
		}
	}
}
