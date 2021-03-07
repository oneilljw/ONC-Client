package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClonedGiftDB extends ONCDatabase
{
	private static ClonedGiftDB instance = null;
	private ArrayList<ONCChildGift> clonedGiftList;
	
	private ClonedGiftDB()
	{	
		super();
		this.title = "Clones";
		clonedGiftList = new ArrayList<ONCChildGift>();
	}
	
	public static ClonedGiftDB getInstance()
	{
		if(instance == null)
			instance = new ClonedGiftDB();
		
		return instance;
	}
	
	@Override
	List<ONCChildGift> getList() { return getCurrentGiftList(); }
	
	List<ONCChildGift> getCurrentGiftList()
	{
		List<ONCChildGift> currClonedGiftList = new ArrayList<ONCChildGift>();
		for(ONCChildGift cg : clonedGiftList)
			if(cg.getNextID() == -1)	//cloned gift is last in linked list, there for is current
				currClonedGiftList.add(cg);
		
		return currClonedGiftList;
	}
	
	//adds a list of child gifts to the cloned gift data base.
	String addClonesFromChildGiftList(Object source, List<ONCChildGift> cloneChildGiftRequestList)
	{		
		String cb = UserDB.getInstance().getUserLNFI();
		
		List<ONCChildGift> reqAddClonedGiftList = new ArrayList<ONCChildGift>();
		
		//create the list of added cloned gifts
		for(ONCChildGift addGiftReq : cloneChildGiftRequestList)
			reqAddClonedGiftList.add(new ONCChildGift(-1, cb, addGiftReq, 3));
		
		//wrap the child gift list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfClonedGifts = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
		String response = null;
		response = serverIF.sendRequest("POST<add_newclonedgiftlist>" + gson.toJson(reqAddClonedGiftList, listOfClonedGifts));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added gift json strings, convert them to ClonedGift objects and process the adds.
		//set the return string to indicate the server add list request was successful
		int addedCloneGiftCount = 0;
		if(response != null && response.startsWith("ADDED_LIST_CLONED_GIFTS"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(23), responseListType);
			List<ONCChildGift> processedAddedClonedGiftList = new ArrayList<ONCChildGift>();
			
			for(String giftResponse : responseList)
			{
				ONCChildGift addedCloneGift = processAddedClonedGift(source, gson.fromJson(giftResponse.substring(17), ONCChildGift.class));
				processedAddedClonedGiftList.add(addedCloneGift);
				addedCloneGiftCount++;
			}
			
			if(!processedAddedClonedGiftList.isEmpty())
				fireDataChanged(source, "ADDED_LIST_CLONED_GIFTS", processedAddedClonedGiftList);
		}
		
		String pluralOrNot = addedCloneGiftCount == 1 ? "Gift" : "Gifts";
		return String.format("%d Cloned %s Created", addedCloneGiftCount, pluralOrNot);
	}
	
	//updates a list of cloned gifts to the cloned gift data base.
	String addClonedGiftList(Object source, List<ONCChildGift> reqAddClonedGiftList)
	{	
		//wrap the child gift list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfClonedGifts = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
		
		String response = null, returnResp = "ADD_FAILED";
		response = serverIF.sendRequest("POST<add_clonedgiftlist>" + gson.toJson(reqAddClonedGiftList, listOfClonedGifts));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added gift json strings, convert them to ClonedGift objects and process the adds.
		//set the return string to indicate the server add list request was successful
		if(response != null && response.startsWith("ADDED_LIST_CLONED_GIFTS"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(23), responseListType);
			List<ONCChildGift> processedAddedCloneGiftList = new ArrayList<ONCChildGift>();
		
			for(String giftResponse : responseList)
			{
				ONCChildGift addedCG = processAddedClonedGift(source, gson.fromJson(giftResponse.substring(17), ONCChildGift.class));
				processedAddedCloneGiftList.add(addedCG);
			}
			
			returnResp = "ADDED_LIST_CLONED_GIFTS";
			
			//data bases have been updated, notify ui's that a list has been added
			if(!processedAddedCloneGiftList.isEmpty())
				fireDataChanged(source, "ADDED_LIST_CLONED_GIFTS", processedAddedCloneGiftList);
		}

		return returnResp;
	}

	ONCChildGift processAddedClonedGift(Object source, ONCChildGift addedGift)
	{	
		//determine if the added clone gift has a prior clone in the linked list. If it does, update the linked list pointers
		if(addedGift.getPriorID() > -1)
		{
			//find the gift and change and set the next gift pointer to the added gift
			int index = clonedGiftList.size()-1;
			while(index >= 0 && clonedGiftList.get(index).getID() != addedGift.getPriorID())
				index--;
			
			if(index >= 0)
				clonedGiftList.get(index).setNextID(addedGift.getID());
		}
		
		//add the new gift to the local data base
		clonedGiftList.add(addedGift);
		
		return addedGift;
	}
	
	ONCChildGift getClonedGift(int childID, int gn)
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
	
	List<ONCChildGift> getGiftHistory(int childID, int gn)
	{
		List<ONCChildGift> cghList = new ArrayList<ONCChildGift>();
		Gson gson = new Gson();
		
		HistoryRequest req = new HistoryRequest(childID, gn);
		String response = serverIF.sendRequest("GET<clonedgifthistory>"+ 
											gson.toJson(req, HistoryRequest.class));
		
		if(response != null)
		{
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();	
			cghList = gson.fromJson(response, listtype);
		}
		
		return cghList;
	}
	

	ONCChildGift getClonedGift(int id)
	{
		int index = 0;
		while(index < clonedGiftList.size() && clonedGiftList.get(index).getID() != id)
			index++;
		
		return index < clonedGiftList.size() ? clonedGiftList.get(index) : null;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
			
			String jsonResponse = serverIF.sendRequest("GET<clonedgifts>");
			
			if(jsonResponse != null)
			{
				clonedGiftList = gson.fromJson(jsonResponse, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("ADDED_LIST_CLONED_GIFTS"))
		{
			Gson gson = new Gson();
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> addedGiftListString = gson.fromJson(ue.getJson(), responseListType);
			List<ONCChildGift> processedAddedCloneGiftList = new ArrayList<ONCChildGift>();
			for(String giftResponse : addedGiftListString)
			{
				ONCChildGift addedCG = processAddedClonedGift(this, gson.fromJson(giftResponse.substring(17), ONCChildGift.class));
				processedAddedCloneGiftList.add(addedCG);
			}
			
			if(!processedAddedCloneGiftList.isEmpty())
				this.fireDataChanged(this, "ADDED_LIST_CLONED_GIFTS", processedAddedCloneGiftList);
		}
	}

	@Override
	String update(Object source, ONCObject entity)
	{
		//Cloned gifts are never updated by a user, an update only occurs to modify the
		//the linked list chain
		return null;
	}

	@Override
	String[] getExportHeader()
	{
		return new String[] {"Cloned Gift ID", "Child ID", "Gift ID", "Detail", "Gift #", "Restrictions",
				"Status","Changed By", "Time Stamp","PartnerID", "Prior ID", "Next ID"};
	}
}
