package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class ChildGiftDB extends ONCDatabase
{
	private static final int PARTNER_TYPE_ONC_SHOPPER = 6;
	private static final int GIFT_INDICATOR_ALLOW_SUBSTITUE = 2;
	private static final String CHILD_GIFT_DEFAULT_DETAIL = "Age appropriate";
	
	private static ChildGiftDB instance = null;
	private GiftCatalogDB cat;
	private ChildDB childDB;
	private PartnerDB partnerDB;
	private ArrayList<ONCChildGift> childGiftList;
	
	private ChildGiftDB()
	{	
		super();
		cat = GiftCatalogDB.getInstance();
		childDB = ChildDB.getInstance();
		partnerDB = PartnerDB.getInstance();
		childGiftList = new ArrayList<ONCChildGift>();
	}
	
	public static ChildGiftDB getInstance()
	{
		if(instance == null)
			instance = new ChildGiftDB();
		
		return instance;
	}
	
	/*******
	 * This is a critical method in the ONC application. A child's gift is never modified or
	 * deleted, a new gift is always created to take an old gifts place. If the new gift 
	 * being added has a base or assignee change, the status must be checked to see if 
	 * an automatic change needs to occur as well. The new gift, with correct status is 
	 * then sent to the server. The list should never be sorted, keeping the newest gifts for
	 * a child at the bottom of the list
	 * @param currPartner - defines who the requested partner is. null to leave the 
	 * current partner unchanged
	 */
	ONCChildGift add(Object source, int childid, int giftID, String gd, int gn, int gi,
			GiftStatus gs, ONCPartner currPartner)
	{		
//		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		String cb = UserDB.getInstance().getUserLNFI();
		long dc = System.currentTimeMillis();

		//Get the old gift being replaced. getGift method returns null if gift not found
		ONCChildGift replacedGift = getGift(childid, gn);

		//determine if we need to change the partner id
		int newPartnerID = -1;
		if(replacedGift != null && replacedGift.getGiftID() != giftID)
			newPartnerID = -1;
		else if(currPartner == null && replacedGift != null)
			newPartnerID = replacedGift.getPartnerID(); 	//Staying the same
		else if(currPartner != null)
			newPartnerID = currPartner.getID();

		//create the new gift, with childgiftID = -1, meaning no gift selected
		//the server will add the childgiftID and return it
		ONCChildGift retCG = null;
		ONCChildGift reqCG = new ONCChildGift(-1, childid, giftID,
								   checkForDetailChange(gi, gd, currPartner, replacedGift), 
								   gn, gi,
								   checkForStatusChange(replacedGift, giftID, gs, currPartner), 
								   newPartnerID, cb, dc);		
		Gson gson = new Gson();
		String response = null, helmetResponse = null;

		//send add new gift request to the server
		response = serverIF.sendRequest("POST<childwish>" + gson.toJson(reqCG, ONCChildGift.class));

		//get the gift in the sever response and add it to the local cache data base
		//it contains the gift id assigned by the server child gift data base
		//Notify all other ui's that a gift has been added
		if(response != null && response.startsWith("WISH_ADDED"))
		{
			ONCChildGift addedGift = gson.fromJson(response.substring(10), ONCChildGift.class);
			retCG = processAddedGift(source, addedGift);

			//must check to see if new gift is gift number 1 and has changed to/from a 
			//Bike. If so, gift number 2 must become a Helmet/Empty
			ONCChild child = childDB.getChild(retCG.getChildID());
			int bikeID = cat.getGiftID("Bike");
			int helmetID = cat.getGiftID("Helmet");
			if(retCG.getGiftNumber() == 0 && replacedGift != null && replacedGift.getGiftID() != bikeID && 
					retCG.getGiftID() == bikeID || retCG.getGiftNumber() == 0 && replacedGift == null &&
					retCG.getGiftID() == bikeID)		
			{
				//add Helmet as gift 2
				ONCChildGift helmetCG = new ONCChildGift(-1, childid, helmetID, "", 1, 1,
						GiftStatus.Selected, -1, cb, dc);
				helmetResponse = serverIF.sendRequest("POST<childwish>" + gson.toJson(helmetCG, ONCChildGift.class));
				if(helmetResponse != null && helmetResponse.startsWith("WISH_ADDED"))
				{
					ONCChildGift helmetGift = gson.fromJson(helmetResponse.substring(10), ONCChildGift.class);
					processAddedGift(this, helmetGift);
				}
					
			}
			//if replaced gift was a bike and now isn't and gift 2 was a helmet, make
			//gift 1 empty
			else if(retCG.getGiftNumber() == 0 && replacedGift != null && child.getChildGiftID(1) > -1 &&
					replacedGift.getGiftID() == bikeID && retCG.getGiftID() != bikeID)
			{
				//change gift 1 from Helmet to None
				ONCChildGift helmetCG = new ONCChildGift(-1, childid, -1, "", 1, 0,
						GiftStatus.Not_Selected, -1, cb, dc);
				helmetResponse = serverIF.sendRequest("POST<childwish>" + gson.toJson(helmetCG, ONCChildGift.class));
				if(helmetResponse != null && helmetResponse.startsWith("WISH_ADDED"))
				{
					ONCChildGift helmetGift = gson.fromJson(helmetResponse.substring(10), ONCChildGift.class);
					processAddedGift(this, helmetGift);
				}
			}
		}

		return retCG;
	}
	
	ONCChildGift processAddedGift(Object source, ONCChildGift addedGift)
	{
		ONCChildGift replacedGift = getGift(addedGift.getChildID(), addedGift.getGiftNumber());
				
		//add the new gift to the local data base
		childGiftList.add(addedGift);
			
		//Set the new gift ID in the child object that has been assigned this gift in the child data base
		ChildDB childDB = ChildDB.getInstance();
		childDB.setChildWishID(addedGift.getChildID(), addedGift.getID(), addedGift.getGiftNumber());
		
		//notify the partner data base to evaluate the new gift to see if partner gifts assigned, delivered
		//or received counts have to change
		PartnerDB partnerDB = PartnerDB.getInstance();
		partnerDB.processAddedGift(replacedGift, addedGift);
			
		//data bases have been updated, notify ui's of changes
		fireDataChanged(source, "WISH_ADDED", addedGift);
		
		//notify the catalog to update counts if the wish has changed
		if(replacedGift == null && addedGift.getGiftStatus() == GiftStatus.Selected ||
			replacedGift != null && replacedGift.getGiftID() != addedGift.getGiftID())
		{
			cat.changeGiftCounts(replacedGift, addedGift);				
		}

		return addedGift;
	}
	
	//adds a list of gifts to the child gift data base.
	String addGiftList(Object source, List<AddGiftRequest> addGiftRequestList)
	{		
//		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		String cb = UserDB.getInstance().getUserLNFI();
		long dc = System.currentTimeMillis();
		
		List<ONCChildGift> reqAddGiftList = new ArrayList<ONCChildGift>();
		
		//create the list of added gifts
		for(AddGiftRequest addGiftReq : addGiftRequestList)
		{
			//Get the old gift being replaced
			ONCChildGift replacedGift = addGiftReq.getPriorGift();
			ONCChildGift requestedGift = addGiftReq.getNewGift();

			//determine if we need to change the partner. If the gift is changing, then the partner ID 
			//in the requested gift must be set to -1 (no partner)
			int newPartnerID;
			ONCPartner newPartner = null;
			if(replacedGift != null && replacedGift.getGiftID() != requestedGift.getGiftID())
				newPartnerID = -1;	//selected gift has changed, partner must be reset
			else if(requestedGift.getPartnerID() == -1)
				newPartnerID = -1;	//request gift has removed partner	
			else
			{
				newPartnerID = requestedGift.getPartnerID();
				newPartner = partnerDB.getPartnerByID(requestedGift.getPartnerID());
			}
			
			
			//create the added gift, with childwishID = -1, meaning no wish selected
			//the server will add the childwishID and return it
			ONCChildGift reqCW = new ONCChildGift(-1, requestedGift.getChildID(),requestedGift.getGiftID(),
								   checkForDetailChange(requestedGift.getIndicator(), requestedGift.getDetail(), newPartner, replacedGift), 
								   requestedGift.getGiftNumber(),requestedGift.getIndicator(),
								   checkForStatusChange(replacedGift, requestedGift.getGiftID(), requestedGift.getGiftStatus(), newPartner), 
								   newPartnerID, cb, dc);
			
			reqAddGiftList.add(reqCW);
		}
		
		//wrap the child gift list in a json array and send the add request to the server
		Gson gson = new Gson();
		Type listOfChildGifts = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
		String response = null, returnResp = "ADD_FAILED";
		response = serverIF.sendRequest("POST<add_giftlist>" + gson.toJson(reqAddGiftList, listOfChildGifts));

		//get the response from the server, validate it. Once validated, decompose the response json
		//to a list of added gift json strings, convert them to ChildGift objects and process the adds.
		//set the return string to indicate the server add list request was successful
		if(response != null && response.startsWith("ADDED_GIFT_LIST"))
		{
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> responseList = gson.fromJson(response.substring(15), responseListType);
			
			for(String addGiftResp : responseList)
			{
				ONCChildGift addedGift = gson.fromJson(addGiftResp.substring(10), ONCChildGift.class);
				processAddedGift(source, addedGift);
			}
			
			returnResp = "ADDED_GIFT_LIST";
		}

		return returnResp;
	}
	
	/*******************************************************************************************
	 * This method implements a rules engine governing the relationship between a gift type and
	 * gift status and gift assignment and gift status. It is called when a child's gift or
	 * assignee changes and implements an automatic change of gift status.
	 * 
	 * For example, if a child's base gift is empty and it is changing to a gift selected from
	 * the catalog, this method will set the gift status to SELECTED. Conversely, if
	 * a gift was selected from the catalog and is reset to empty, the status is set to Not_Selected.
	 ************************************************************************************************************/	
	GiftStatus checkForStatusChange(ONCChildGift oldGift, int giftBase, GiftStatus reqStatus, ONCPartner reqPartner)
	{
		GiftStatus currStatus, newStatus;
		
		if(oldGift == null)	//selecting first gift
			currStatus = GiftStatus.Not_Selected;
		else	
			currStatus = oldGift.getGiftStatus();
		
		//set new status = current status for default return
		newStatus = currStatus;
		
		switch(currStatus)
		{
			case Not_Selected:
				if(giftBase > -1 && reqPartner != null && reqPartner.getID() != -1)
					newStatus = GiftStatus.Assigned;	//assigned from inventory
				else if(giftBase > -1)
					newStatus = GiftStatus.Selected;
				break;
				
			case Selected:
				if(giftBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(reqPartner != null && reqPartner.getID() != -1)
					newStatus = GiftStatus.Assigned;
				break;
				
			case Assigned:
				if(giftBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(oldGift.getGiftID() != giftBase)
					newStatus = GiftStatus.Selected;
				else if(reqPartner == null || reqPartner != null && reqPartner.getID() == -1)
					newStatus = GiftStatus.Selected;
				else if(reqStatus == GiftStatus.Delivered)
					newStatus = GiftStatus.Delivered;
				break;
				
			case Delivered:
				if(reqStatus == GiftStatus.Returned)
					newStatus = GiftStatus.Returned;
				else if(reqStatus == GiftStatus.Delivered && reqPartner != null && 
							reqPartner.getID() > -1 && reqPartner.getType() == PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Delivered && reqPartner != null && reqPartner.getID() > -1)
					newStatus = GiftStatus.Assigned;
				else if(reqStatus == GiftStatus.Shopping)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Received)
					newStatus = GiftStatus.Received;
				break;
				
			case Returned:
				if(giftBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(reqPartner != null && reqPartner.getID() == -1)
					newStatus = GiftStatus.Selected;
				else if(reqPartner != null && reqPartner.getType() != PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Assigned;
				else if(reqPartner != null && reqPartner.getType() == PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Shopping;
				break;
				
			case Shopping:
				if(reqStatus == GiftStatus.Returned)
					newStatus = GiftStatus.Returned;
				else if(reqStatus == GiftStatus.Received)
					newStatus = GiftStatus.Received;
				break;
				
			case Received:
				if(reqStatus == GiftStatus.Missing)
					newStatus = GiftStatus.Missing;
				else if(reqStatus == GiftStatus.Distributed)
					newStatus = GiftStatus.Distributed;
				else if(reqStatus == GiftStatus.Delivered)
					newStatus = GiftStatus.Delivered;
				break;
				
			case Distributed:
				if(reqStatus == GiftStatus.Missing)
					newStatus = GiftStatus.Missing;
				else if(reqStatus == GiftStatus.Verified)
					newStatus = GiftStatus.Verified;
				break;
			
			case Missing:
				if(reqStatus == GiftStatus.Received)
					newStatus = GiftStatus.Received;
				else if(reqPartner != null && reqPartner.getType() == PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Assigned && reqPartner != null && reqPartner.getID() > -1)
					newStatus = GiftStatus.Assigned;
				break;
				
			case Verified:
				if(reqStatus == GiftStatus.Missing)
					newStatus = GiftStatus.Missing;
				break;
				
			default:
				break;
		}
		
		return newStatus;			
	}
	
	/*** checks for automatic change of gift detail. An automatic change is triggered if
	 * the replaced gift is of status Delivered and requested parter is of type ONC Shopper
	 * and the requested gift indicator is #. 
	 */
	
	String checkForDetailChange(int reqGiftRes, String reqGiftDetail,
									ONCPartner reqPartner, ONCChildGift replGift)
	{
		if(replGift != null && reqPartner != null && 
			replGift.getGiftStatus() == GiftStatus.Delivered && 
			 reqPartner.getType() == PARTNER_TYPE_ONC_SHOPPER && 
			  reqGiftRes == GIFT_INDICATOR_ALLOW_SUBSTITUE)
		{
			return CHILD_GIFT_DEFAULT_DETAIL;
		}
		else
			return reqGiftDetail;
	}

	/**
	 * This method removes a child's gift from the child gift database.
	 * @param gift id
	 */
	ONCChildGift deleteChildGift(int giftID)
	{
		int index = 0;
		while(index < childGiftList.size() && childGiftList.get(index).getID() != giftID)
			index++;
			
		if(index < childGiftList.size())
		{
			ONCChildGift deletedGift = childGiftList.get(index);
			childGiftList.remove(index);
			return deletedGift;
		}
		else
			return null;
	}
	
	String update(Object source, ONCObject oncchildwish)
	{		
		//A gift is not updated in the current design. A new gift is always created 
		//and added to the data base. This allows a child's gift history to be 
		//preserved for a season. 
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_child_wish>" + 
											gson.toJson(oncchildwish, ONCChildGift.class));
		
		if(response.startsWith("UPDATED_CHILD_WISH"))
		{
			processUpdatedObject(source, response.substring(18), childGiftList);
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json, List<? extends ONCObject> objList)
	{
		Gson gson = new Gson();
		ONCChildGift updatedObj = gson.fromJson(json, ONCChildGift.class);
		
		//store updated object in local data base
		int index = 0;
		while(index < objList.size() && objList.get(index).getID() != updatedObj.getID())
			index++;
		
		if(index < objList.size())
		{
			replaceObject(index, updatedObj);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_CHILD_WISH", updatedObj);
		}
	}
	
	/*****************************************************************************
	 * This method handles the object specific replace processing for replacing
	 * an ONCDriver in the local data base. It is called by the super class 
	 * processUpdatedObject method that is inherited by all data base object classes
	 * @param updatedObj
	 * @param index
	 *************************************************************************/
	void replaceObject(int index, ONCObject updatedObj)
	{
		ONCChildGift updatedGift = (ONCChildGift) updatedObj;
		childGiftList.set(index,  updatedGift);
	}

	ONCChildGift getGift(int giftID)
	{
		int index = childGiftList.size() -1;
		
		//Search from the bottom of the data base for speed. New gifts are added to the bottom
		while (index >= 0 && childGiftList.get(index).getID() != giftID)
			index--;
		
		if(index == -1)
			return null;	//Gift wasn't found in data base
		else
			return childGiftList.get(index);
	}
	
	ONCChildGift getGift(int childid, int gn)
	{
		int index = childGiftList.size() -1;
		
		//Search from the bottom of the data base for speed. New gifts are added to the bottom
		while (index >= 0 && (childGiftList.get(index).getChildID() != childid || 
								childGiftList.get(index).getGiftNumber() != gn))
			index--;
		
		if(index == -1)
			return null;	//gift wasn't found in data base
		else
			return childGiftList.get(index);
	}
	
	List<ONCChildGift> getGiftHistory(int childID, int gn)
	{
		List<ONCChildGift> cghList = new ArrayList<ONCChildGift>();
		Gson gson = new Gson();
		
		HistoryRequest req = new HistoryRequest(childID, gn);
		String response = serverIF.sendRequest("GET<wishhistory>"+ 
											gson.toJson(req, HistoryRequest.class));
		
		if(response != null)
		{
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();	
			cghList = gson.fromJson(response, listtype);
		}
		
		return cghList;
	}
	
	int getNumberOfGiftsPerChild() { return NUMBER_OF_WISHES_PER_CHILD; }
	
	ArrayList<ONCChildGift> getList() { return childGiftList; }
	
	String importChildGiftDatabase()
	{
		String response = "NO_WISHES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
			
			response = serverIF.sendRequest("GET<childwishes>");
			childGiftList = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_WISHES"))
			{
				response =  "WISHES_LOADED";
				fireDataChanged(this, "LOADED_WISHES", null);
			}
		}
		
		return response;
	}

	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Child Gift DB to",
							new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
    	}
    	else
    		oncwritefile = new File(filename);
    	
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		 String[] header = {"Child GIft ID", "Child ID", "GIft ID", "Detail",
	    				 			"Gift #", "Restrictions", "Status",
	    				 			"Changed By", "Time Stamp", "Org ID"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCChildGift cw:childGiftList)
	    	    	writer.writeNext(cw.getExportRow());	
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("WISH_ADDED"))
		{
//			System.out.println(String.format("ChildWishDB Server Event, Source: %s, Type: %s, Json: %s",
//					ue.getSource().toString(), ue.getType(), ue.getJson()));
			Gson gson = new Gson();
			processAddedGift(this, gson.fromJson(ue.getJson(), ONCChildGift.class));
		}
		else if(ue.getType().equals("UPDATED_CHILD_WISH"))
		{
//			System.out.println(String.format("ChildWishDB Server Event, Source: %s, Type: %s, Json: %s",
//					ue.getSource().toString(), ue.getType(), ue.getJson()));
			processUpdatedObject(this, ue.getJson(), childGiftList);
		}
		else if(ue.getType().equals("ADDED_GIFT_LIST"))
		{
			Gson gson = new Gson()
;			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> addedGiftList = gson.fromJson(ue.getJson(), responseListType);
			for(String responseGiftString : addedGiftList)
			{
				ONCChildGift addedGift = gson.fromJson(responseGiftString.substring(10), ONCChildGift.class);
				processAddedGift(this, addedGift);
			}
//			processUpdatedObject(this, ue.getJson(), childwishAL);
		}
	}
}