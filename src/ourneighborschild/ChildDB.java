package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ChildDB extends ONCDatabase
{
	private static final int NUM_GIFTS_PER_CHILD = 3;
	private static ChildDB instance = null;
	private ArrayList<ONCChild> childAL;
	
	private ChildDB()
	{
		super();
		this.title = "Children";
		childAL = new ArrayList<ONCChild>();
	}
	
	public static ChildDB getInstance()
	{
		if(instance == null)
			instance = new ChildDB();
		
		return instance;
	}
	
	int size() { return childAL.size(); }
	
	//Used to create a child internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedChild = null;
		
		//send add child request to server
		String response = "";
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_child>" + gson.toJson(entity, ONCChild.class));		
			
			//if the server added the child,  add the new child to the data base and notify ui's
			if(response.startsWith("ADDED_CHILD"))		
				addedChild =  processAddedChild(source, response.substring(11));
		}
		
		return addedChild;
	}
	
	ONCObject processAddedChild(Object source, String json)
	{
		ONCChild addedChild = null;
		Gson gson = new Gson();
		addedChild = gson.fromJson(json, ONCChild.class);
		
		if(addedChild != null)
		{
			childAL.add(addedChild);
			fireDataChanged(source, "ADDED_CHILD", addedChild);
			
			FamilyDB fDB = FamilyDB.getInstance();
			int[] countsChange = fDB.getServedFamilyAndChildCount();
			DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
			fireDataChanged(source, "UPDATED_SERVED_COUNTS", servedCountsChange);
		}
		
		return addedChild;
	}

	/*************************************************************************************************************
	 * This method is called when the user requests to delete a child from the menu bar. The child to be deleted
	 * is the child currently selected in child table in the family panel. The first step in deletion is to confirm
	 * with the user that they intended to delete the child. Prior to removing the child from the 
	 * family objects child array list, the method checks for wish assignees and, if any, removes the child's
	 * assigned ornament count prior to removal from the child array list. 
	 **********************************************************************************************************/
	String delete(Object source, ONCChild reqDelChild)
	{
		String response = null;
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_child>" + gson.toJson(reqDelChild, ONCChild.class));		
			
			//if the server added the child,  add the new child to the data base and notify ui's
			if(response.startsWith("DELETED_CHILD"))
				processDeletedChild(source, response.substring(13));
		}
		
		return response;
	}
	
	ONCChild processDeletedChild(Object source, String json)
	{
		ChildGiftDB cgDB = ChildGiftDB.getInstance();
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		Gson gson = new Gson();
		
		ONCChild deletedChild =  removeChild(source, gson.fromJson(json, ONCChild.class).getID());
		
		//If the child had gifts, delete them from the child gift database & update the gift catalog counts. 
		for(int giftNum= 0; giftNum < NUM_GIFTS_PER_CHILD; giftNum++)
		{
			if(deletedChild.getChildGiftID(giftNum) > -1)
			{
				//ask the child gift data base to delete the gift
				ONCChildGift deletedGift = cgDB.deleteChildGift(deletedChild.getChildGiftID(giftNum));
				
				//inform the gift catalog that the gift has been deleted
				cat.changeGiftCounts(deletedGift, null);
			}
		}
		
		FamilyDB fDB = FamilyDB.getInstance();
		int[] countsChange = fDB.getServedFamilyAndChildCount();
		DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
		fireDataChanged(source, "UPDATED_SERVED_COUNTS", servedCountsChange);
					
		fireDataChanged(source, "DELETED_CHILD", deletedChild);
		
		return deletedChild;
	}
	
	ONCChild removeChild(Object source, int deletedChildID)
	{
		ONCChild deletedChild = null;
		
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != deletedChildID)
			index++;
		
		if(index < childAL.size())	//found the deleted child
		{
			deletedChild = childAL.get(index);
			childAL.remove(index);
		}
		
		return deletedChild;
	}
	
	/***************************************************************
	 * This method is called when a child wish has been updated by 
	 * the user. The request update child object is passed. 
	 *************************************************************/
	String update(Object source, ONCObject updatedChild)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_child>" + 
												gson.toJson(updatedChild, ONCChild.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated child in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response.startsWith("UPDATED_CHILD"))
			processUpdatedChild(source, response.substring(13));
		
		return response;
	}
	
	void processUpdatedChild(Object source, String json)
	{
		//Create a child object for the updated child
		Gson gson = new Gson();
		ONCChild updatedChild = gson.fromJson(json, ONCChild.class);
		
		//Find the position for the current child being updated
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != updatedChild.getID())
			index++;
		
		//Replace the current child object with the update
		if(index < childAL.size())
		{
			childAL.set(index, updatedChild);
			fireDataChanged(source, "UPDATED_CHILD", updatedChild);
		}
		else
			System.out.println(String.format("ChildDB processUpdatedChild - child id %d not found",
					updatedChild.getID()));
	}
	
	ONCPriorYearChild getPriorYearChild(int childID)
	{
		String zPYCID = Integer.toString(childID);
		String response = null;
		ONCPriorYearChild pyc = null;
		
		response = serverIF.sendRequest("GET<pychild>" + zPYCID);
		
		if(response != null && response.startsWith("PYC"))
		{		
			Gson gson = new Gson();
			pyc = gson.fromJson(response.substring(3), ONCPriorYearChild.class);
		}
		
		return pyc;
	}
	
	ONCPriorYearChild searchForPriorYearChild(ONCPriorYearChild pyChildReq)
	{
		String response = null;
		ONCPriorYearChild pyc = null;
		
		Gson gson = new Gson();
		response = serverIF.sendRequest("GET<search_pychild>" + gson.toJson(pyChildReq, ONCPriorYearChild.class));
		
		if(response != null && !response.equals("PYC_NOT_FOUND"))
		{		
			pyc = gson.fromJson(response.substring(3), ONCPriorYearChild.class);
		}
		
		return pyc;
	}
	
	ONCChild getChild(int childid)
	{
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != childid)
			index++;
		
		if(index==childAL.size())
			return null;	//Child wasn't found
		else
			return childAL.get(index);
	}
	
	ArrayList<ONCChild> getChildren(int famid)
	{
		ArrayList<ONCChild> fChildrenAL = new ArrayList<ONCChild>();
		
		for(ONCChild c:childAL)
			if(c.getFamID() == famid)
				fChildrenAL.add(c);
		
		//sort by age if there is more than one child in the family
		if(fChildrenAL.size() > 1)
			Collections.sort(fChildrenAL, new ONCChildAgeComparator());
		
		return fChildrenAL;
	}
	
	int getChildNumber(ONCChild child)
	{
		ArrayList<ONCChild> childrenInFam = getChildren(child.getFamID());
		int index = 0;
		while(index < childrenInFam.size() && childrenInFam.get(index).getID() != child.getID())
			index++;
		
		if(index < childrenInFam.size())
			return index + 1;
		else
			return -1;
	}
	
	int getNumberOfChildrenInFamily(int famid)
	{
		int count = 0;
		for(ONCChild c:childAL)
		{
			if(c.getFamID() == famid)
				count++;
		}
		
		return count;
	}
	
	
	
	void setChildWishID(int childid, int newWishID, int wishnumber)
	{
		ONCChild c = getChild(childid);
		if(c != null)
			c.setChildGiftID(newWishID, wishnumber);	
	}
	
	void searchForLastName(String s, List<Integer> rAL)
    {	
		int lastFamIDAdded = -1;	//prevent searching for same family id twice in a row
    	for(ONCChild c: childAL)
    		if(c.getFamID() != lastFamIDAdded && 
    			c.getChildLastName().toLowerCase().contains(s.toLowerCase()))
    		{
    			//check to see that the family hasn't already been found
    			//if it hasn't, add it
    			int index=0;
    			while(index < rAL.size() && rAL.get(index) != c.getFamID())
    				index++;
    			
    			if(index == rAL.size())
    			{
    				rAL.add(c.getFamID()); 
    				lastFamIDAdded = c.getFamID();
    			}
    		}	
    }
	
	@Override
	List<ONCChild> getList() { return childAL; }
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChild>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<children>");
			
			if(response != null)
			{
				childAL = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}

	@Override
	public void dataChanged(ServerEvent ue) 
	{
		if(ue.getType().equals("UPDATED_CHILD"))
		{
			processUpdatedChild(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_CHILD"))
		{
			processAddedChild(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_CHILD"))
		{
			processDeletedChild(this, ue.getJson());
		}			
	}
	
	@Override
	String[] getExportHeader()
	{
		return new String[] {"Child ID", "Family ID", "Child #", "First Name", "Last Name",
	 			"Gender", "DOB", "School", "Wish 1 ID", "Wish 2 ID",
	 			"Wish 3 ID", "Prior Year Child ID"};
	}
	
	private class ONCChildAgeComparator implements Comparator<ONCChild>
	{
	    @Override
	    public int compare(ONCChild o1, ONCChild o2)
	    {
	        return o1.getChildDateOfBirth().compareTo(o2.getChildDateOfBirth());
	    }
	}

}
