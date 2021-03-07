package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ChildGiftDB extends ONCDatabase
{
	private static final int GIFT_INDICATOR_ALLOW_SUBSTITUE = 2;
	private static final String CHILD_GIFT_DEFAULT_DETAIL = "Age appropriate";
	
	private static ChildGiftDB instance = null;
	private GiftCatalogDB cat;
	private PartnerDB partnerDB;
	private ArrayList<ONCChildGift> childGiftList;
	
	private ChildGiftDB()
	{	
		super();
		this.title = "Gifts";
		cat = GiftCatalogDB.getInstance();
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
	 * @param currPartner0 - defines who the requested partner is. null to leave the 
	 * current partner unchanged
	 */
	ONCChildGift add(Object source, int childid, int giftID, String gd, int gn, int gi,
			GiftStatus gs, ONCPartner reqPartner)
	{	
		//Get the old gift being replaced. getGift method returns null if gift not found
		ONCChildGift replacedGift = getCurrentChildGift(childid, gn);
				
		String cb = UserDB.getInstance().getUserLNFI();
		long dc = System.currentTimeMillis();
		
		//get the new partner ID and new GiftStatus
		GiftPartnerAndStatus newGiftPartnerAndStatus = checkForPartnerAndStatusChange(replacedGift, giftID, gs, reqPartner);
		int newPartnerID = newGiftPartnerAndStatus.getPartnerID();
		GiftStatus newGiftStatus = newGiftPartnerAndStatus.getGiftStatus();
		
		//create the new gift, with childgiftID = -1, meaning no gift selected
		//the server will add the childgiftID and return it
		ONCChildGift retCG = null;
		ONCChildGift reqCG = new ONCChildGift(-1, childid, giftID,
				   checkForDetailChange(gi, gd, reqPartner, replacedGift), 
				   gn, gi, newGiftStatus, newPartnerID, cb, dc);
		
		Gson gson = new Gson();
		String response = null, helmetResponse = null;

		//send add new gift request to the server
		response = serverIF.sendRequest("POST<childwish>" + gson.toJson(reqCG, ONCChildGift.class));

		//get the gift in the server response and add it to the local cache data base
		//it contains the gift id assigned by the server child gift data base
		//Notify all other ui's that a gift has been added
		if(response != null && response.startsWith("WISH_ADDED"))
		{
			ONCChildGift addedGift = gson.fromJson(response.substring(10), ONCChildGift.class);
			retCG = processAddedGift(source, addedGift);

			//must check to see if new gift is gift number 1 and has changed to/from a 
			//Bike. If so, gift number 2 must become a Helmet/Empty
			int bikeID = cat.getGiftID("Bike");
			int helmetID = cat.getGiftID("Helmet");
			if(retCG.getGiftNumber() == 0 && replacedGift != null && replacedGift.getCatalogGiftID() != bikeID && 
					retCG.getCatalogGiftID() == bikeID || retCG.getGiftNumber() == 0 && replacedGift == null &&
					retCG.getCatalogGiftID() == bikeID)		
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
			//if replaced gift was a bike and now isn't, if gift 2 was selected, make gift 2 empty
			else if(retCG.getGiftNumber() == 0 && replacedGift != null && getCurrentChildGift(childid,1) != null &&
					replacedGift.getCatalogGiftID() == bikeID && retCG.getCatalogGiftID() != bikeID)
			{
				//change gift 2 from Helmet to None
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
		//if the added gift isn't the first gift in the linked list, find the previous gift and set it's
		//next id to link to the added gift
		ONCChildGift replacedGift = null;
		if(addedGift.getPriorID() != -1)
		{
			replacedGift = getCurrentChildGift(addedGift.getChildID(), addedGift.getGiftNumber());
			replacedGift.setNextID(addedGift.getID());
		}		
				
		//add the new gift to the local data base
		childGiftList.add(addedGift);
			
		//Set the new gift ID in the child object that has been assigned this gift in the child data base
//		ChildDB childDB = ChildDB.getInstance();
//		childDB.setChildWishID(addedGift.getChildID(), addedGift.getID(), addedGift.getGiftNumber());
		
		//notify the partner data base to evaluate the new gift to see if partner gifts assigned, delivered
		//or received counts have to change
		PartnerDB partnerDB = PartnerDB.getInstance();
		partnerDB.processAddedGift(replacedGift, addedGift);
			
		//data bases have been updated, notify ui's of changes
		fireDataChanged(source, "WISH_ADDED", addedGift, replacedGift);
		
		//notify the catalog to update counts if the wish has changed
		if(replacedGift == null && addedGift.getGiftStatus() == GiftStatus.Selected ||
			replacedGift != null && replacedGift.getCatalogGiftID() != addedGift.getCatalogGiftID())
		{
			cat.changeGiftCounts(replacedGift, addedGift);				
		}

		return addedGift;
	}

	//adds a list of gifts to the child gift data base.
	String addGiftList(Object source, List<AddGiftRequest> addGiftRequestList)
	{		
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
			ONCPartner reqPartner = partnerDB.getPartnerByID(requestedGift.getPartnerID());
			GiftPartnerAndStatus gpas = checkForPartnerAndStatusChange(replacedGift, requestedGift.getCatalogGiftID(), requestedGift.getGiftStatus(), reqPartner);
			int newPartnerID = gpas.getPartnerID();
			GiftStatus newGiftStatus = gpas.getGiftStatus();
			
			//create the added gift, with childwishID = -1, meaning no wish selected
			//the server will add the childwishID and return it
			ONCChildGift reqCW = new ONCChildGift(-1, requestedGift.getChildID(),requestedGift.getCatalogGiftID(),
								   checkForDetailChange(requestedGift.getIndicator(), requestedGift.getDetail(), reqPartner, replacedGift), 
								   requestedGift.getGiftNumber(),requestedGift.getIndicator(),
								   newGiftStatus, newPartnerID, cb, dc);
			
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
/*	
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
							reqPartner.getID() > -1 && reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
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
				else if(reqPartner != null && reqPartner.getGiftCollectionType() != GiftCollectionType.ONCShopper)
					newStatus = GiftStatus.Assigned;
				else if(reqPartner != null && reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
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
				else if(reqPartner != null && reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
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
*/	
	/*******************************************************************************************
	 * This method implements a rules engine governing the relationship between a gift type and
	 * gift status and partner assignment and gift status. It implements an automatic change of 
	 * gift status.
	 * 
	 * For example, if a child's base gift is empty and it is changing to a gift selected from
	 * the catalog, this method will set the gift status to SELECTED. Conversely, if
	 * a gift was selected from the catalog and is reset to empty, the status is set to Not_Selected.
	 ************************************************************************************************************/	
	GiftPartnerAndStatus checkForPartnerAndStatusChange(ONCChildGift oldGift, int giftBase, GiftStatus reqStatus, ONCPartner reqPartner)
	{
		GiftStatus currStatus;
		GiftPartnerAndStatus newPartnerAndStatus;
		
		//set up for default return
		if(oldGift == null)	//selecting first gift
		{	
			currStatus = GiftStatus.Not_Selected;
			newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Selected);
		}
		else
		{
			currStatus = oldGift.getGiftStatus();
			newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), oldGift.getGiftStatus());
		}
		
		//determine new partner and status based on current status, current partner, requested status & requested partner
		switch(currStatus)
		{
			case Not_Selected:
				if(giftBase > -1 && reqPartner != null && reqPartner.getID() != -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);	//assigned from inventory
				else if(giftBase > -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Selected);
				break;
				
			case Selected:
				if(giftBase == -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Not_Selected);
				else if(reqPartner != null && reqPartner.getID() != -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);
				break;
				
			case Assigned:
				if(giftBase == -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Not_Selected);	//Gift = None
				else if(oldGift.getCatalogGiftID() != giftBase)
					newPartnerAndStatus = new GiftPartnerAndStatus( -1, GiftStatus.Selected);	//New Gift
				else if(reqPartner == null || reqPartner != null && reqPartner.getID() == -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Selected);	//Partner = None
				else if(reqPartner != null && reqPartner.getID() >= -1 && oldGift.getPartnerID() != reqPartner.getID())
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);	//New Partner
				else if(reqStatus == GiftStatus.Delivered)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Delivered);	//Status Change
				break;
				
			case Delivered:
				if(reqPartner != null && reqPartner.getID() > -1 && oldGift.getPartnerID() != reqPartner.getID() && 
					reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Shopping);
				else if(reqPartner != null && reqPartner.getID() > -1 && oldGift.getPartnerID() != reqPartner.getID() &&
						 reqPartner.getGiftCollectionType() != GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);
				else if(reqStatus == GiftStatus.Assigned)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Assigned);
				else if(reqStatus == GiftStatus.Returned)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Returned);
				else if(reqStatus == GiftStatus.Received)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Received);
				break;
				
			case Returned:
				if(giftBase == -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Not_Selected);
				else if(reqPartner != null && reqPartner.getID() == -1)
					newPartnerAndStatus = new GiftPartnerAndStatus(-1, GiftStatus.Selected);
				else if(reqPartner != null && oldGift.getPartnerID() != reqPartner.getID() && reqPartner.getGiftCollectionType() != GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);
				else if(reqPartner != null && oldGift.getPartnerID() != reqPartner.getID() && reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Shopping);
				break;
				
			case Shopping:
				if(reqStatus == GiftStatus.Returned)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Returned);
				else if(reqStatus == GiftStatus.Received)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Received);
				break;
				
			case Received:
				if(reqStatus == GiftStatus.Missing)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Missing);
				else if(reqStatus == GiftStatus.Distributed)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Distributed);
				else if(reqStatus == GiftStatus.Delivered)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Delivered);
				break;
				
			case Distributed:
				if(reqStatus == GiftStatus.Missing)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Missing);
				else if(reqStatus == GiftStatus.Verified)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Verified);
				break;
			
			case Missing:
				if(reqStatus == GiftStatus.Received)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Received);
				else if(reqPartner != null && reqPartner.getID() > -1 && oldGift.getPartnerID() != reqPartner.getID() && reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Shopping);
				else if(reqPartner != null && reqPartner.getID() > -1 && oldGift.getPartnerID() != reqPartner.getID() && reqPartner.getGiftCollectionType() != GiftCollectionType.ONCShopper)
					newPartnerAndStatus = new GiftPartnerAndStatus(reqPartner.getID(), GiftStatus.Assigned);
				break;
				
			case Verified:
				if(reqStatus == GiftStatus.Missing)
					newPartnerAndStatus = new GiftPartnerAndStatus(oldGift.getPartnerID(), GiftStatus.Missing);
				break;
				
			default:
				break;
		}
		
		return newPartnerAndStatus;			
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
			 reqPartner.getGiftCollectionType() == GiftCollectionType.ONCShopper && 
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
		return null;
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
	
	ONCChildGift getCurrentChildGift(int childid, int gn)
	{
		int index = childGiftList.size() -1;
		
		//Search from the bottom of the data base for speed. New gifts are added to the bottom
		while (index >= 0 && (childGiftList.get(index).getChildID() != childid || 
								childGiftList.get(index).getGiftNumber() != gn ||
								 childGiftList.get(index).getNextID() != -1))
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
	
	@Override
	List<ONCChildGift> getList() { return getCurrentGiftList(); }
	
	List<ONCChildGift> getCurrentGiftList()
	{
		List<ONCChildGift> currGiftList = new ArrayList<ONCChildGift>();
		for(ONCChildGift cg : childGiftList)
			if(cg.getNextID() == -1)	//cloned gift is last in linked list, there for is current
				currGiftList.add(cg);
		
		return currGiftList;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<childwishes>");
			childGiftList = gson.fromJson(response, listtype);				

			if(response != null)
				bImportComplete = true;
		}
		
		return bImportComplete;
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
			Gson gson = new Gson();
			Type responseListType = new TypeToken<ArrayList<String>>(){}.getType();
			List<String> addedGiftList = gson.fromJson(ue.getJson(), responseListType);
			for(String responseGiftString : addedGiftList)
			{
				ONCChildGift addedGift = gson.fromJson(responseGiftString.substring(10), ONCChildGift.class);
				processAddedGift(this, addedGift);
			}
		}
	}
	
	@Override
	String[] getExportHeader()
	{
		return new String[] {"Child Gift ID", "Child ID", "Gift ID", "Detail", "Gift #",
							"Restrictions", "Status","Changed By", "Time Stamp","Partner ID"};
	}
	
	private class GiftPartnerAndStatus
	{
		private int partnerID;
		private GiftStatus giftStatus;
		
		GiftPartnerAndStatus(int partnerID, GiftStatus giftStatus)
		{
			this.partnerID = partnerID;
			this.giftStatus = giftStatus;
		}
		
		//getters
		int getPartnerID() { return partnerID; }
		GiftStatus getGiftStatus() { return giftStatus; }
	}
}