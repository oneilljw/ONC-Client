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

public class FamilyHistoryDB extends ONCDatabase
{
	private static FamilyHistoryDB instance = null;
	private Map<Integer,List<FamilyHistory>> historyMap;
	private Comparator<FamilyHistory> historyTimestampComparator;
//	private List<FamilyHistory> fhList;
	
	private FamilyHistoryDB()
	{
		super();
		this.title = "History";
		historyMap = new HashMap<Integer, List<FamilyHistory>>();
		historyTimestampComparator = new HistoryTimestampComparator();
//		fhList = new ArrayList<FamilyHistory>();
	}
	
	public static FamilyHistoryDB getInstance()
	{
		if(instance == null)
			instance = new FamilyHistoryDB();
		
		return instance;
	}
	
//	List<FamilyHistory> getList() 
//	{ 
//		return fhList; 
//	}
	
	List<FamilyHistory> getList()
	{ 
		List<FamilyHistory> allHistoriesList = new ArrayList<FamilyHistory>();
		for(Map.Entry<Integer,List<FamilyHistory>> entry : historyMap.entrySet())
			for(FamilyHistory fh : entry.getValue())
				allHistoriesList.add(fh);
		
		return allHistoriesList;
	}
	
//	int size() { return fhList.size(); }

	FamilyHistory add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		FamilyHistory addedHistObj = null;
		
		response = serverIF.sendRequest("POST<add_delivery>" + 
											gson.toJson(entity, FamilyHistory.class));
		if(response.startsWith("ADDED_DELIVERY"))
			addedHistObj = processAddedObject(source, response.substring(14));
		
		return addedHistObj;	
	}
	
	String addHistoryGroup(Object source, List<FamilyHistory> historyList)
	{
		Gson gson = new Gson();
		Type listtype = new TypeToken<ArrayList<FamilyHistory>>(){}.getType();
			
		String response = gson.toJson(historyList, listtype);
		
		response = serverIF.sendRequest("POST<family_history_group>" + gson.toJson(historyList, listtype));
		
		return response;
	}
	
	FamilyHistory processAddedObject(Object source, String json)
	{
		//Store added ONCFamilyHistory object in local data base
		Gson gson = new Gson();
		FamilyHistory addedHistory = gson.fromJson(json, FamilyHistory.class);
		
		if(addedHistory != null)
		{
			int familyID = addedHistory.getFamID();
			if(!historyMap.containsKey(familyID))
			{
				//add a new history list to the map
				historyMap.put(familyID, new ArrayList<FamilyHistory>());
			}
			historyMap.get(addedHistory.getFamID()).add(addedHistory);
			fireDataChanged(source, "ADDED_DELIVERY", addedHistory);
		}
		
		return addedHistory;
	}
/*	
	FamilyHistory processDeletedFamilyHistory(Object source, String json)
	{
		//remove the family history item from this local data base
		Gson gson = new Gson();
		FamilyHistory deletedFH = removeDeletedHistory(source, gson.fromJson(json, FamilyHistory.class).getID());
		
		if(deletedFH != null)
			fireDataChanged(source, "DELETED_FAMILY_HISTORY", deletedFH);
		
		return deletedFH;
	}
	
	FamilyHistory removeDeletedHistory(Object source, int famHistID)
	{
		//remove the meal from this data base
		FamilyHistory deletedFH = null;
		
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != famHistID)
				index++;
				
		if(index < fhList.size())
		{
			deletedFH = fhList.get(index);
			fhList.remove(index);
		}
		
		return deletedFH;
	}
	
	FamilyHistory getFamilyHistory(int id)
	{
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != id)
			index++;
		
		if(index == fhList.size())
			return null;
		else
			return fhList.get(index);
	}
*/	
	FamilyHistory getLastFamilyHistory(int famID)
	{
		List<FamilyHistory> famHistoryList = historyMap.get(famID);
		
		if(famHistoryList != null && !famHistoryList.isEmpty())
		{
			//sort the list by time stamp and return the newest history
			Collections.sort(famHistoryList,historyTimestampComparator);
			return famHistoryList.get(famHistoryList.size()-1);
		}
		else
			return null;
	}
	
	List<FamilyHistory> getFamilyHistoryList(int famID)
	{
		List<FamilyHistory> famHistoryList = historyMap.get(famID);
		return famHistoryList;
	}
	
	/**************************************************************************************************
	 * Return the delivered by field in the most recent delivery for the family
	 * @param famID
	 * @return
	 */
/*	
	String getDeliveredBy(int delID)
	{
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != delID)
			index++;
				
		if(index==fhList.size())
			return "";
		else
			return fhList.get(index).getdDelBy();
	}
*/	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type mapOfHistories = new TypeToken<HashMap<Integer,List<FamilyHistory>>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<family_histories>");
			if(response != null)
			{
				historyMap = gson.fromJson(response, mapOfHistories);
			}	bImportComplete = true;
		}
		
		return bImportComplete;
	}
	
	@Override
	public void dataChanged(ServerEvent ue) 
	{
		if(ue.getType().equals("ADDED_DELIVERY"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_DELIVERY"))
		{
			processUpdatedObject(this, ue.getJson());
		}
	}

	@Override
	/***************************************************************
	 * This method is called when a family history object has been updated by 
	 * the user. The request update object is passed. 
	 *************************************************************/
	String update(Object source, ONCObject updatedHistory)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		
		response = serverIF.sendRequest("POST<update_delivery>" + 
												gson.toJson(updatedHistory, FamilyHistory.class));
			 
		
		//check response. If response from server indicates a successful update,
		//create and store the updated object in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response.startsWith("UPDATED_DELIVERY"))
			processUpdatedObject(source, response.substring(16));
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		//Create a new object for the update
		Gson gson = new Gson();
		FamilyHistory updatedHistoryObj = gson.fromJson(json, FamilyHistory.class);
		
		List<FamilyHistory> famHistList = historyMap.get(updatedHistoryObj.getFamID());
		
		//Find the position for the history object being updated
		int index = 0;
		while(index < famHistList.size() && famHistList.get(index).getID() != updatedHistoryObj.getID())
			index++;
		
		//Replace the current history object with the update
		if(index < famHistList.size())
		{
			famHistList.set(index, updatedHistoryObj);
			fireDataChanged(source, "UPDATED_DELIVERY", updatedHistoryObj);
		}
		else
			System.out.println(String.format("DeliveryDB processUpdatedObject - delivery id %d not found",
					updatedHistoryObj.getID()));
	}

	/**************************************************************************************************
	 * Return the number of deliveries made by a specifc driver. The driver number is passes and
	 * the count is returned.
	 */
	public int getNumberOfDeliveries(String drvNum) 
	{
		int nDeliveries = 0;
		for(FamilyHistory del: getList())
			if(del.getdDelBy().equals(drvNum))
				nDeliveries++;
		
		return nDeliveries;
	}

	@Override
	String[] getExportHeader()
	{
		return new String[]  {"Delivery ID", "Family ID", "Status", "Del By",  "Notes", "Changed By", "Time Stamp"};
	}
	
	private class HistoryTimestampComparator implements Comparator<FamilyHistory>
	{
		@Override
		public int compare(FamilyHistory fh1, FamilyHistory fh2)
		{
			if(fh1.getTimestamp() < fh2.getTimestamp())
				return -1;
			else if(fh1.getTimestamp() == fh2.getTimestamp())
				return 0;
			else
				return 1;
		}
	}
}
