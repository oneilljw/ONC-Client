package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ClonedGiftDB extends ONCDatabase
{
	private static ClonedGiftDB instance = null;
	private ArrayList<ClonedGift> clonedGiftList;
	
	private ClonedGiftDB()
	{	
		super();
		clonedGiftList = new ArrayList<ClonedGift>();
	}
	
	public static ClonedGiftDB getInstance()
	{
		if(instance == null)
			instance = new ClonedGiftDB();
		
		return instance;
	}
	
	List<ClonedGift> getCurrentCloneGiftList()
	{
		List<ClonedGift> currClonedGiftList = new ArrayList<ClonedGift>();
		for(ClonedGift cg : clonedGiftList)
			if(cg.getNextID() == -1)	//cloned gift is last in linked list, there for is current
				currClonedGiftList.add(cg);
		
		return currClonedGiftList;
	}
	
	//adds a list of child gifts to the cloned gift data base.
	String addClonesFromChildGiftList(Object source, List<ONCChildGift> cloneChildGiftRequestList)
	{		
		String cb = UserDB.getInstance().getUserLNFI();
		
		List<ClonedGift> reqAddClonedGiftList = new ArrayList<ClonedGift>();
		
		//create the list of added cloned gifts
		for(ONCChildGift addGiftReq : cloneChildGiftRequestList)
			reqAddClonedGiftList.add(new ClonedGift(-1, cb, addGiftReq, 3));
		
		//wrap the child gift list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfClonedGifts = new TypeToken<ArrayList<ClonedGift>>(){}.getType();
		String response = null;
		response = serverIF.sendRequest("POST<add_clonedgiftlist>" + gson.toJson(reqAddClonedGiftList, listOfClonedGifts));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added gift json strings, convert them to ClonedGift objects and process the adds.
		//set the return string to indicate the server add list request was successful
		int addedCloneGiftCount = 0;
		if(response != null && response.startsWith("ADDED_LIST_CLONED_GIFTS"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(23), responseListType);
			
			for(String giftResponse : responseList)
			{
				if(giftResponse.startsWith("ADDED_CLONED_GIFT"))
				{	
					ClonedGift addedGift = gson.fromJson(giftResponse.substring(17), ClonedGift.class);
					processAddedClonedGift(source, addedGift);
					addedCloneGiftCount++;
				}
			}
		}
		
		String pluralOrNot = addedCloneGiftCount == 1 ? "Gift" : "Gifts";
		return String.format("%d Cloned %s Created", addedCloneGiftCount, pluralOrNot);
	}
	
	//updates a list of cloned gifts to the cloned gift data base.
	String addClonedGiftList(Object source, List<ClonedGift> addGiftRequestList)
	{	
		List<ClonedGift> reqAddClonedGiftList = new ArrayList<ClonedGift>();
		
		//create the list of added cloned gifts
		for(ClonedGift addGiftReq : addGiftRequestList)
			reqAddClonedGiftList.add(new ClonedGift(UserDB.getInstance().getUserLNFI(), addGiftReq));
		
		//wrap the child gift list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfClonedGifts = new TypeToken<ArrayList<ClonedGift>>(){}.getType();
		String response = null, returnResp = "ADD_FAILED";
		response = serverIF.sendRequest("POST<add_clonedgiftlist>" + gson.toJson(reqAddClonedGiftList, listOfClonedGifts));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added gift json strings, convert them to ClonedGift objects and process the adds.
		//set the return string to indicate the server add list request was successful
		if(response != null && response.startsWith("ADDED_LIST_CLONED_GIFTS"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(23), responseListType);
			
			for(String giftResponse : responseList)
			{
				if(giftResponse.startsWith("UPDATED_CLONED_GIFT"))
				{	
					ClonedGift updatedGift = gson.fromJson(giftResponse.substring(19), ClonedGift.class);
					processUpdatedClonedGift(source, updatedGift);
				}
				else if(giftResponse.startsWith("ADDED_CLONED_GIFT"))
				{	
					ClonedGift addedGift = gson.fromJson(giftResponse.substring(17), ClonedGift.class);
					processAddedClonedGift(source, addedGift);
				}
			}
			
			returnResp = "ADDED_GIFT_LIST";
		}

		return returnResp;
	}
	
	void processUpdatedClonedGift(Object source, ClonedGift updatedClonedGift)
	{
		//store updated object in local data base
		int index = 0;
		while(index < clonedGiftList.size() && clonedGiftList.get(index).getID() != updatedClonedGift.getID())
			index++;
		
		if(index < clonedGiftList.size())
		{
			replaceObject(index, updatedClonedGift);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_CLONED_GIFT", updatedClonedGift);
		}
		
	}
	
	void replaceObject(int index, ONCObject updatedObj)
	{
		ClonedGift updatedGift = (ClonedGift) updatedObj;
		clonedGiftList.set(index,  updatedGift);
	}
	
	ClonedGift processAddedClonedGift(Object source, ClonedGift addedGift)
	{	
		//add the new gift to the local data base
		clonedGiftList.add(addedGift);
		
		//data bases have been updated, notify ui's of changes
		fireDataChanged(source, "ADDED_CLONED_GIFT", addedGift);
		
		return addedGift;
	}
	
	ClonedGift getClonedGift(int childID, int gn)
	{
		int index = clonedGiftList.size() -1;
		while(index >= 0 && 
				!(clonedGiftList.get(index).getChildID() == childID &&
				   clonedGiftList.get(index).getGiftNumber() == gn &&
				    clonedGiftList.get(index).getNextID() == -1))
		{
			index--;
		}
		
		return index >= 0 ? clonedGiftList.get(index) : null;
	}
	
	List<ClonedGift> getGiftHistory(int childID, int gn)
	{
		List<ClonedGift> cghList = new ArrayList<ClonedGift>();
		Gson gson = new Gson();
		
		HistoryRequest req = new HistoryRequest(childID, gn);
		String response = serverIF.sendRequest("GET<clonedgifthistory>"+ 
											gson.toJson(req, HistoryRequest.class));
		
		if(response != null)
		{
			Type listtype = new TypeToken<ArrayList<ClonedGift>>(){}.getType();	
			cghList = gson.fromJson(response, listtype);
		}
		
		return cghList;
	}
	

	ClonedGift getClonedGift(int id)
	{
		int index = 0;
		while(index < clonedGiftList.size() && clonedGiftList.get(index).getID() != id)
			index++;
		
		return index < clonedGiftList.size() ? clonedGiftList.get(index) : null;
	}
	
	String importClonedGiftDatabase()
	{
		String response = "NO_CLONED_GIFTS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ClonedGift>>(){}.getType();
			
			String jsonResponse = serverIF.sendRequest("GET<clonedgifts>");
			
			if(!jsonResponse.startsWith("NO_CLONED_GIFTS"))
			{
				clonedGiftList = gson.fromJson(jsonResponse, listtype);
				response =  "CLONED_GIFTS_LOADED";
				fireDataChanged(this, "LOADED_CLONED_GIFTS", null);
			}
		}
		
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("ADDED_CLONED_GIFT"))
		{
			Gson gson = new Gson();
			processAddedClonedGift(this, gson.fromJson(ue.getJson(), ClonedGift.class));
		}
		else if(ue.getType().equals("UPDATED_CLONED_GIFT"))
		{
			Gson gson = new Gson();
			processUpdatedClonedGift(this, gson.fromJson(ue.getJson(), ClonedGift.class));
		}
		else if(ue.getType().equals("ADDED_LIST_CLONED_GIFTS"))
		{
			Gson gson = new Gson();
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> addedGiftListString = gson.fromJson(ue.getJson(), responseListType);
			for(String giftResponse : addedGiftListString)
			{
				if(giftResponse.startsWith("UPDATED_CLONED_GIFT"))
				{	
					ClonedGift updatedGift = gson.fromJson(giftResponse.substring(19), ClonedGift.class);
					processUpdatedClonedGift(this, updatedGift);
				}
				else if(giftResponse.startsWith("ADDED_CLONED_GIFT"))
				{	
					ClonedGift addedGift = gson.fromJson(giftResponse.substring(17), ClonedGift.class);
					processAddedClonedGift(this, addedGift);
				}
			}
		}
	}

	@Override
	String update(Object source, ONCObject entity)
	{
		//Cloned gifts are never updated by a user, an update only occurs to modify the
		//the linked list chain
		return null;
	}
}
