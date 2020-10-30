package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class PartnerDB extends ONCSearchableDatabase
{
	/**
	 * This method provides an interface to the organizations managed by the ONC application.
	 * Each Organization is stored in an array list named orgsAL. A second array list, cOrgs 
	 * keeps the name of each organization with confirmed status. An import method allows 
	 * a user to import an external .csv formatted file of organizations. Read and write 
	 * object methods provide for persistent storage of the organization array list. 
	 */
	private static final EntityType DB_TYPE = EntityType.PARTNER;
	private static final int STATUS_NO_ACTION_YET = 0;
	private static final int STATUS_CONFIRMED = 5;
	private static final int ORG_TYPE_CLOTHING = 4;
	private static final int MAX_ORGANIZATION_ID_LENGTH = 10;
	
	private static PartnerDB instance = null;
	private ArrayList<ONCPartner> partnerList;	//The list of ONCPartner objects
	private GlobalVariablesDB orgGVs;
	private UserDB userDB;
	
	private PartnerDB()
	{
		super(DB_TYPE);
		
		//Instantiate the partner list
		partnerList = new ArrayList<ONCPartner>();
		orgGVs = GlobalVariablesDB.getInstance();
		userDB = UserDB.getInstance();
	}
	
	public static PartnerDB getInstance()
	{
		if(instance == null)
			instance = new PartnerDB();
		
		return instance;
	}
	
	/**********************************************************************************************************
	 * This method processes a request for a change in organization status. Possible status is 0 - Not yet
	 * contacted, 1- 1st Email Sent, 2 - Responded, 3 - 2nd Email Sent,  4 - Called, Left Mssg, 
	 * 5- Confirmed, 6 - Not participating. A change request from confirmed to
	 * another status is only permitted if no ornaments have been assigned to the organization. If a 
	 * status change request can be fulfilled, the method makes the change. If not, it ignores the request
	 * and leaves the organization status confirmed. After processing the request the method returns the
	 * organizations status
	 *********************************************************************************************************/
	int requestOrgStatusChange(ONCPartner o, int newstatus)
	{
		if(o.getStatus() != STATUS_CONFIRMED && newstatus == STATUS_CONFIRMED)
		{
			//A change from a previous status to confirmed has occurred, add to the confirmed array
			o.setStatus(STATUS_CONFIRMED);
			o.setDateChanged(System.currentTimeMillis());
			o.setStoplightChangedBy(userDB.getUserLNFI());
		}
		else if(o.getStatus() == STATUS_CONFIRMED && newstatus != STATUS_CONFIRMED)
		{
			//A change request from confirmed status to a different status has occurred. If the number of
			//ornaments is not zero, can't change the status If the number or ornaments
			//assigned is zero, delete the organization from the confirmed array list
			if(o.getNumberOfOrnamentsAssigned() == 0)
			{
				o.setStatus(newstatus);
				o.setDateChanged(System.currentTimeMillis());
				o.setStoplightChangedBy(userDB.getUserLNFI());
			}		
		}
		else	//Status change does not involve to/from confirmed status
		{
			o.setStatus(newstatus);
			o.setDateChanged(System.currentTimeMillis());
			o.setStoplightChangedBy(userDB.getUserLNFI());
		}
			
		return o.getStatus();
	}

	//implementation of abstract classes
	List<ONCPartner> getList() {return partnerList; }
	
	ONCPartner getObjectAtIndex(int on) { return partnerList.get(on); }
	
	ONCPartner getPartnerByID(int id )
	{
//		int index = 0;
//		while(index < partnerList.size() && partnerList.get(index).getID() != id)
//			index++;
//		
//		if(index < partnerList.size())
//			return partnerList.get(index);
//		else
//			return null;
		
		return (ONCPartner) find(partnerList, id);
	}
	
	ONCPartner getPartnerByNameAndType(String name, int type )
	{
		int index = 0;
		while(index < partnerList.size() && partnerList.get(index).getType() != type && 
				!partnerList.get(index).getLastName().equals(name))
			index++;
		
		if(index < partnerList.size())
			return partnerList.get(index);
		else
			return null;
	}
	
	int size() { return partnerList.size(); }
	
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		String searchType = "";
		searchAL.clear();
		
    	int sn; 	//-1 indicates family number not found
    	
    	if(isNumeric(data) && data.length() < MAX_ORGANIZATION_ID_LENGTH)
    	{
    		searchType = "Partner ID";
    		if(data.matches("-?\\d+(\\.\\d+)?"))	//Check for numeric string
    		{
    			if((sn = getListIndexByID(partnerList, Integer.parseInt(data))) > -1)
    				searchAL.add(getObjectAtIndex(sn).getID());
    		}	
    	}
    	else	//Check for partner name, email matches, 1st or 2nd contact matches
    	{
    		searchType = "Partner Name, Email or Contacts";
			for(ONCPartner o:partnerList)
			{
				if(o.getLastName().toLowerCase().contains(data.toLowerCase()) ||
					o.getContact_email().toLowerCase().contains(data.toLowerCase()) ||
					o.getContact2_email().toLowerCase().contains(data.toLowerCase()) ||
					o.getContact().toLowerCase().contains(data.toLowerCase()) ||
					o.getContact2().toLowerCase().contains(data.toLowerCase()) || 
					o.getContact_phone().toLowerCase().contains(data.toLowerCase()) ||
					o.getContact2_phone().toLowerCase().contains(data.toLowerCase()))
				{
					searchAL.add(o.getID());
				}
			}
    	}
    	
    	return searchType;
	}
	
	/***************************************************************************************
	 * This method searches the organization array list and counts the total number of 
	 * CONFIRMED organizations and the total number of ornaments assigned to them. It returns
	 * a two element int array, the first element is the CONFIRMED organization count, the
	 * second element is the total assigned ornament count.
	 ***************************************************************************************/
	int[] getOrnamentAndWishCounts()
	{
		int orgcount = 0, wishcount = 0;
		for(ONCPartner org:partnerList)
		{		
			if(org.getStatus() == STATUS_CONFIRMED) 
			{ 
				orgcount++;
				wishcount += org.getNumberOfOrnamentsAssigned();
			}
		}
		
		int[] oaw = {orgcount, wishcount};
		return oaw;
	}
	
	/*****************************************************************************************
	 * Creates a list of confirmed partners that take ornaments, broken into two parts.
	 * The top half of the list are confirmed businesses, churches and schools, sorted alphabetically.
	 * The bottom half of the list are all other confirmed partners sorted alphabetically
	 *****************************************************************************************/
	List<ONCPartner> getConfirmedPartnerList(EnumSet<GiftCollectionType> collectionTypeSet)
	{
		//Create two lists, the list to be returned and a temporary list
		ArrayList<ONCPartner> confirmedPartnerList = new ArrayList<ONCPartner>();
		ArrayList<ONCPartner> confirmedPartnerOtherList = new ArrayList<ONCPartner>();
		
		//Add the confirmed business, church and schools, individuals and internal partners to the returned 
		//list. Business, churches and schools are at the top of the list.
		for(ONCPartner o: partnerList)
		{
			if(o.getStatus() == STATUS_CONFIRMED && collectionTypeSet.contains(o.getGiftCollectionType()))
			{
				if(o.getType() <= ONCPartner.PARTNER_TYPE_SCHOOL)
					confirmedPartnerList.add(o);
				else
					confirmedPartnerOtherList.add(o);					
			}
		}
		
		//Sort the two lists alphabetically by partner name
		PartnerNameComparator nameComparator = new PartnerNameComparator();
		Collections.sort(confirmedPartnerList, nameComparator);	//Sort alphabetically
		Collections.sort(confirmedPartnerOtherList, nameComparator);	//Sort alphabetically
		
		//Append the all other temporary confirmed list to the bottom of the confirmed list
		for(ONCPartner otherOrg:confirmedPartnerOtherList)
			confirmedPartnerList.add(otherOrg);
		
		//return the integrated list
		return confirmedPartnerList;
	}
/*	
	int decrementConfirmedOrgOrnAssigned(int orgID)
	{
		//find org by id
		int i = 0;
		while(i < partnerList.size() && partnerList.get(i).getID() != orgID )
			i++;
		
		//if found, decrement
		if(i<partnerList.size())
			return partnerList.get(i).decrementOrnAssigned();
		else
			return 0;
		
	}
	
	void deleteChildWishAssignments(ONCChild delChild)
	{
		ChildGiftDB cwDB = ChildGiftDB.getInstance();
		
		//For each of the three wishes, if wish assignment has been made, decrement the
		//wish counts for the assignee
		for(int wn=0; wn< NUMBER_OF_WISHES_PER_CHILD; wn++)
		{
			ONCChildGift cw = cwDB.getGift(delChild.getChildGiftID(wn));
			if(cw != null && cw.getPartnerID() > 0)
				decrementConfirmedOrgOrnAssigned(cw.getPartnerID());
		}
	}
*/
	void resetAllOrgsStatus()
	{
		for(ONCPartner o:partnerList)
			o.setStatus(STATUS_NO_ACTION_YET);	
	}
	
	//Overloaded sortDB methods allow user to specify a data base to be sorted
	//or use the current data base
	boolean sortDB(ArrayList<ONCPartner> oal, String dbField) { return sortList(oal, dbField); }
	boolean sortDB(String dbField) { return sortList(partnerList, dbField); }
	
	private boolean sortList(ArrayList<ONCPartner> oal, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("Partner")) {
			Collections.sort(oal, new PartnerNameComparator()); }
		else if(dbField.equals("Status")) {
			Collections.sort(oal, new PartnerStatusComparator()); }
		else if(dbField.equals("Type")) {
			Collections.sort(oal, new PartnerTypeComparator()); }
		else if(dbField.equals("Collection")) {
			Collections.sort(oal, new PartnerCollectionComparator()); }
		else if(dbField.equals("Req")) {
			Collections.sort(oal, new PartnerOrnReqComparator()); }
		else if(dbField.equals("Assigned")) {
			Collections.sort(oal, new PartnerOrnAssignedComparator()); }
		else if(dbField.equals("Del")) {
			Collections.sort(oal, new PartnerOrnDeliveredComparator()); }
		else if(dbField.equals("Rec")) {
			Collections.sort(oal, new PartnerOrnOnTimeComparator()); }
		else if(dbField.equals("Late")) {
			Collections.sort(oal, new PartnerOrnLateComparator()); }
		else if(dbField.equals("Delivery Information")) {
			Collections.sort(oal, new PartnerDeliveryInfoComparator()); }
		else if(dbField.equals("Date Changed")) {
			Collections.sort(oal, new PartnerDateChangedComparator()); }
		else if(dbField.equals("Changed By")) {
			Collections.sort(oal, new PartnerChangedByComparator()); }
		else if(dbField.equals("Reg")) {
			Collections.sort(oal, new PartnerRegionComparator()); }
		else if(dbField.equals("SL")) {
			Collections.sort(oal, new OrgStoplightComparator()); }
		else
			bSortOccurred = false;
		
		return bSortOccurred;
		
	}
	
	String importDB()
	{
		String response = "NO_PARTNERS";
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCPartner>>(){}.getType();
			
			response = serverIF.sendRequest("GET<partners>");
			partnerList = gson.fromJson(response, listtype);	
					
			if(!response.startsWith("NO_PARTNERS"))
			{
				response = "PARTNERS_LOADED";
				fireDataChanged(this, "LOADED_PARTNERS", null);
			}
		}
		
		return response;
	}
	
	String exportDBToCSV(String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(GlobalVariablesDB.getFrame());
    		oncwritefile= fc.getFile("Select .csv file to save Partner DB to",
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
	    		 String[] header = {"Org ID", "Status", "Type", "Name", "Orn Delivered", "Street #",
	    				 			"Street", "Unit", "City", "Zip", "Region", "Phone",
	    				 			"Orn Requested", "Orn Assigned", "Other",
	    				 			"Deliver To", "Special Notes",
	    				 			"Contact", "Contact Email", "Contact Phone",
	    				 			"Contact2", "Contact2 Email", "Contact2 Phone",
	    				 			"Time Stamp", "Stoplight Pos", "Stoplight Mssg", "Stoplight C/B"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCPartner o: partnerList)
	    	    	writer.writeNext(o.getExportRow());	//Get family data
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }
	
	/****************************************************************************************************
	 * This method is called when a gift is added. It determines if the partner's gifts assigned,
	 * delivered or received counts should be incremented or decremented. If any changes occur based on 
	 * the gift added and the gift it's replacing, the GUI's are notified.
	 * 
	 * @param replGift - ONCChildGift that is being replaced, null if the initial gift selection
	 * @param addedGift - ONCChildGift that is being added
	 *********************************/
	void processAddedGift(ONCChildGift replGift, ONCChildGift addedGift)
	{
		ONCPartner addedGiftPartner = null;
		
		if(replGift != null && replGift.getGiftStatus() == GiftStatus.Selected &&
			addedGift.getGiftStatus() == GiftStatus.Assigned && addedGift.getPartnerID() > -1)		
		{
			//gift status changing from Selected To Assigned w/ a partner
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", null, addedGiftPartner);
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Assigned &&
			(addedGift.getGiftStatus() == GiftStatus.Not_Selected || addedGift.getGiftStatus() == GiftStatus.Selected)) 	
		{
			//gift status changing from Assigned to Selected or Not Selected
			ONCPartner replGiftPartner = (ONCPartner) find(partnerList, replGift.getPartnerID());
			replGiftPartner.decrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", replGiftPartner, null);
		}
		else if(replGift != null && addedGift.getGiftStatus() == GiftStatus.Assigned &&
				replGift.getGiftStatus() == GiftStatus.Assigned && addedGift.getPartnerID() > -1 &&
					replGift.getPartnerID() > -1 && addedGift.getPartnerID() != replGift.getPartnerID())
		{
			//gift status was Assigned and remains Assigned, however, partner has changed
			ONCPartner replGiftPartner = (ONCPartner) find(partnerList, replGift.getPartnerID());
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			replGiftPartner.decrementOrnAssigned();
			addedGiftPartner.incrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", replGiftPartner, addedGiftPartner);
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Missing && 
				replGift.getPartnerID() != addedGift.getPartnerID() &&
				(addedGift.getGiftStatus() == GiftStatus.Assigned || addedGift.getGiftStatus() == GiftStatus.Shopping))
		{
			//gift status changed from Missing to Assigned or Missing to Shopping with a new partner
			//Increment the new partners assigned count
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", null, addedGiftPartner);
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Returned && 
				replGift.getPartnerID() != addedGift.getPartnerID() &&
				(addedGift.getGiftStatus() == GiftStatus.Assigned || addedGift.getGiftStatus() == GiftStatus.Shopping))
		{
			//gift status changed from Returned to Assigned or Returned to Shopping with a new partner
			//Increment the new partners assigned count
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", null, addedGiftPartner);
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Delivered && 
				replGift.getPartnerID() != addedGift.getPartnerID() &&
				addedGift.getGiftStatus() == GiftStatus.Shopping)
		{
			//gift status changed from Delivered to Shopping with a new partner
			//Increment the new partners assigned count
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnAssigned();
			fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", null, addedGiftPartner);
		}
		else if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
			replGift.getGiftStatus() == GiftStatus.Assigned && addedGift.getGiftStatus() == GiftStatus.Delivered)
		{
			//gift status changed from Assigned to Delivered with the same partner, increment the delivered count
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnDelivered();
			fireDataChanged(this, "PARTNER_ORNAMENT_DELIVERED", addedGiftPartner);
		}
		else if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
				replGift.getGiftStatus() == GiftStatus.Delivered && addedGift.getGiftStatus() == GiftStatus.Assigned)
		{
			//gift status changed from Delivered to Assigned w/ same partner, decrement delivered count
			ONCPartner replGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			replGiftPartner.decrementOrnDelivered();
			fireDataChanged(this, "PARTNER_ORNAMENT_DELIVERED", replGiftPartner);
		}
		else if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
				 replGift.getGiftStatus() == GiftStatus.Delivered  && 
				 addedGift.getGiftStatus() == GiftStatus.Received)
		{
			//gift status changed from Delivered to Received w/ same partner, increment received count
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			boolean bBeforeDeadline = addedGift.getTimestamp() < orgGVs.getGiftsReceivedDate();
			addedGiftPartner.incrementOrnReceived(bBeforeDeadline);
			fireDataChanged(this, "PARTNER_WISH_RECEIVED", addedGiftPartner);
		}
		else if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
				(replGift.getGiftStatus() == GiftStatus.Shopping || 
				   replGift.getGiftStatus() == GiftStatus.Missing) &&
				addedGift.getGiftStatus() == GiftStatus.Received)
		{
			//gift status changed from Delivered, Shopping or Missing to Received w/ same partner, 
			//increment received count. Do not mark it as received late, since it's already handling
			//an exception
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			addedGiftPartner.incrementOrnReceived(true);
			fireDataChanged(this, "PARTNER_WISH_RECEIVED", addedGiftPartner);
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Received  && 
				 addedGift.getGiftStatus() == GiftStatus.Delivered &&
				  replGift.getPartnerID() == addedGift.getPartnerID())
		{
			//gift was un-received from partner it was assigned to. This occurs when an undo
			//action is performed by the user
			ONCPartner replGiftPartner = (ONCPartner) find(partnerList, replGift.getPartnerID());
			boolean bBeforeDeadline = addedGift.getTimestamp() < orgGVs.getGiftsReceivedDate();
			replGiftPartner.decrementOrnReceived(bBeforeDeadline);
			fireDataChanged(this, "PARTNER_WISH_RECEIVE_UNDONE", replGiftPartner);
		}
	}	
/*
	void processAddedGift(ONCChildGift replGift, ONCChildGift addedGift)
	{
		//process gift partner changes
		if(replGift == null && addedGift.getGiftStatus() == GiftStatus.Assigned)
		{
			//This is the typical path in the gift life cycle. Find the new partner and increment their 
			//assigned gift count
			ONCPartner addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			if(addedGiftPartner != null)
			{
				addedGiftPartner.incrementOrnAssigned();
				fireDataChanged(this, "PARTNER_WISH_ASSIGNED_CHANGED", null, addedGiftPartner);
			}
		}
		else if(replGift != null && replGift.getPartnerID() != addedGift.getPartnerID())
		{
			ONCPartner replGiftPartner = null;
			ONCPartner addedGiftPartner = null;
			
			//decrement the old partner if they exist
			if(replGift.getPartnerID() > -1)
			{
				replGiftPartner = (ONCPartner) find(partnerList, replGift.getPartnerID());
				if(replGiftPartner != null)
					replGiftPartner.decrementOrnAssigned();
			}
			
			//increment the new partner if they exist
			addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			if(addedGiftPartner != null)
				addedGiftPartner.incrementOrnAssigned();
			
			//notify the gui's if at least one partner's gifts assigned count changed
			if(replGiftPartner != null || addedGiftPartner != null)
				fireDataChanged(this, "PARTNER_WISH_ASSIGNEE_CHANGED", replGiftPartner, addedGiftPartner);
		}	
			
		//process ornaments that are delivered to partners
		if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
			replGift.getGiftStatus() == GiftStatus.Assigned && addedGift.getGiftStatus() == GiftStatus.Delivered)
		{
			ONCPartner addedGiftPartner = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			if(addedGiftPartner != null)
			{
				//increment the delivered count
				addedGiftPartner.incrementOrnDelivered();
				
				//notify the gui's that the partners delivered count changed
				fireDataChanged(this, "PARTNER_ORNAMENT_DELIVERED", addedGiftPartner);
			}
		}
		
		//process gifts received. Determine if the gift added time is before or after the deadline 
//		boolean bReceviedBeforeDeadline = addedGift.getChildWishDateChanged().before(orgGVs.getGiftsReceivedDate());
		
		if(replGift != null && replGift.getPartnerID() == addedGift.getPartnerID() &&
		   (replGift.getGiftStatus() == GiftStatus.Delivered || replGift.getGiftStatus() == GiftStatus.Shopping)  && 
			addedGift.getGiftStatus() == GiftStatus.Received)
		{	
			//gift was received from partner it was assigned to or was received from shopping
			ONCPartner addedWishAssignee = (ONCPartner) find(partnerList, addedGift.getPartnerID());
			if(addedWishAssignee != null)
			{
				boolean bBeforeDeadline = addedGift.getDateChanged().before(orgGVs.getGiftsReceivedCalendar());
				addedWishAssignee.incrementOrnReceived(bBeforeDeadline);
				fireDataChanged(this, "PARTNER_WISH_RECEIVED", addedWishAssignee);
			}
		}
		else if(replGift != null && replGift.getGiftStatus() == GiftStatus.Received  && 
				 addedGift.getGiftStatus() == GiftStatus.Delivered &&
				  replGift.getPartnerID() == addedGift.getPartnerID())
		{
			//gift was un-received from partner it was assigned to. This occurs when an undo
			//action is performed by the user
			ONCPartner replWishAssignee = (ONCPartner) find(partnerList, replGift.getPartnerID());
			if(replWishAssignee != null)
			{
				boolean bBeforeDeadline = addedGift.getDateChanged().before(orgGVs.getGiftsReceivedCalendar());
				replWishAssignee.decrementOrnReceived(bBeforeDeadline);
				fireDataChanged(this, "PARTNER_WISH_RECEIVE_UNDONE", replWishAssignee);
			}
		}
	}
*/	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_partner>" + 
											gson.toJson(entity, ONCPartner.class));
		
		if(response.startsWith("ADDED_PARTNER"))
			processAddedPartner(source, response.substring(13));
		
		return response;	
	}
	
	void processAddedPartner(Object source, String json)
	{
		//Store added partner in local data base
		Gson gson = new Gson();
		ONCPartner addedPartner = gson.fromJson(json, ONCPartner.class);
		partnerList.add(addedPartner);
		
		//Notify GUI's that an partner was added
		fireDataChanged(source, "ADDED_PARTNER", addedPartner);
		
		//determine if added partner is confirmed. If so, notify the gui's
		if(addedPartner.getStatus() == STATUS_CONFIRMED)
			fireDataChanged(source, "ADDED_CONFIRMED_PARTNER", addedPartner);
	}
	
	String delete(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_partner>" + 
											gson.toJson(entity, ONCPartner.class));
		
		if(response.startsWith("DELETED_PARTNER"))
			processDeletedPartner(source, response.substring(15));
		
		return response;
	}
	
	void processDeletedPartner(Object source, String json)
	{
		//remove deleted partner in local data base
		Gson gson = new Gson();
		ONCPartner deletedPartner = gson.fromJson(json, ONCPartner.class);
		
		int index=0;
		while(index < partnerList.size() && partnerList.get(index).getID() != deletedPartner.getID())
			index++;
		
		//If deleted partner was found, remove it and notify ui's
		if(index < partnerList.size())
		{
			partnerList.remove(index);
			fireDataChanged(source, "DELETED_PARTNER", deletedPartner);
		}
	
		//determine if deleted partner is confirmed. notify the GUI's
		if(deletedPartner.getStatus() == STATUS_CONFIRMED)
			fireDataChanged(source, "DELETED_CONFIRMED_PARTNER", deletedPartner);
	}
	
	void processUpdatedPartner(Object source, String json)
	{
		Gson gson = new Gson();
		ONCPartner updatedPartner = gson.fromJson(json, ONCPartner.class);
		
		//store updated partner in the partner data base
		int index = 0;
		while(index < partnerList.size() && partnerList.get(index).getID() != updatedPartner.getID())
			index++;
		
		if(index < partnerList.size())
		{	
			ONCPartner replacedPartner = partnerList.get(index);	//use replaced org for change assessment
			partnerList.set(index,  updatedPartner);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_PARTNER", updatedPartner);
			
			//If status has changed to or from confirmed or if the partner is still confirmed
			//and the type has changed, or if the collection type has changed, update the wish assignee 
			//lists by firing an UPDATED_CONFIRMED_PARTNER message
			if(updatedPartner.getStatus() == STATUS_CONFIRMED && replacedPartner.getStatus() != STATUS_CONFIRMED ||
				updatedPartner.getStatus() != STATUS_CONFIRMED && replacedPartner.getStatus() == STATUS_CONFIRMED ||
				 updatedPartner.getStatus() == STATUS_CONFIRMED && updatedPartner.getType() != replacedPartner.getType() ||
				  updatedPartner.getStatus() == STATUS_CONFIRMED  && !updatedPartner.getGiftCollectionType().equals(replacedPartner.getGiftCollectionType()))
			{
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER", updatedPartner);
			}
			
			//If status is still confirmed and the name has changed by firing an 
			//UPDATED_CONFIRMED_PARTNER_NAME message
			if(updatedPartner.getStatus() == STATUS_CONFIRMED && !updatedPartner.getLastName().equals(replacedPartner.getLastName()))
			{
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER_NAME", updatedPartner);
			}
		}
	}
	
	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		response = serverIF.sendRequest("POST<update_partner>" + gson.toJson(entity, ONCPartner.class));
		
		if(response != null && response.startsWith("UPDATED_PARTNER"))
		{
			processUpdatedPartner(source, response.substring(15));
		}
		
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_PARTNER"))
		{
			processUpdatedPartner(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_PARTNER"))
		{
			processAddedPartner(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_PARTNER"))
		{
			processDeletedPartner(this, ue.getJson());
		}
	}
	
	private class PartnerNameComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getLastName().compareTo(o2.getLastName());
		}
	}
	private class PartnerStatusComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer s1 = o1.getStatus();
			Integer s2 = o2.getStatus();
			return s1.compareTo(s2);	
		}
	}
	private class PartnerTypeComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer t1 = o1.getType();
			Integer t2 = o2.getType();
			return t1.compareTo(t2);
		}
	}
	private class PartnerCollectionComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			return o1.getGiftCollectionType().compareTo(o2.getGiftCollectionType());
		}
	}
	private class PartnerOrnReqComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			Integer or1 = o1.getNumberOfOrnamentsRequested();
			Integer or2 = o2.getNumberOfOrnamentsRequested();
			return or1.compareTo(or2);
		}
	}
	private class PartnerOrnAssignedComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer oa1 = o1.getNumberOfOrnamentsAssigned();
			Integer oa2 = o2.getNumberOfOrnamentsAssigned();
			return oa1.compareTo(oa2);
		}
	}
	private class PartnerOrnDeliveredComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer oa1 = o1.getNumberOfOrnamentsDelivered();
			Integer oa2 = o2.getNumberOfOrnamentsDelivered();
			return oa1.compareTo(oa2);
		}
	}
	private class PartnerOrnOnTimeComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer oa1 = o1.getNumberOfOrnamentsReceivedBeforeDeadline();
			Integer oa2 = o2.getNumberOfOrnamentsReceivedBeforeDeadline();
			return oa1.compareTo(oa2);
		}
	}
	private class PartnerOrnLateComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer oa1 = o1.getNumberOfOrnamentsReceivedAfterDeadline();
			Integer oa2 = o2.getNumberOfOrnamentsReceivedAfterDeadline();
			return oa1.compareTo(oa2);
		}
	}
	private class PartnerDeliveryInfoComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			if(o1.getDeliverTo().isEmpty() && !o2.getDeliverTo().isEmpty())
				return 10;
			else if(!o1.getDeliverTo().isEmpty() && o2.getDeliverTo().isEmpty())
				return -10;
			else
				return  o1.getDeliverTo().compareTo(o2.getDeliverTo());
		}
	}
	private class PartnerDateChangedComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getTimestampDate().compareTo(o2.getTimestampDate());
		}
	}
	private class PartnerChangedByComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getStoplightChangedBy().compareTo(o2.getStoplightChangedBy());
		}
	}
	private class PartnerRegionComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{	
			Integer r1 = o1.getRegion();
			Integer r2 = o2.getRegion();
			return r1.compareTo(r2);
		}
	}
	private class OrgStoplightComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{	
			Integer slp1 = o1.getStoplightPos();
			Integer slp2 = o2.getStoplightPos();
			return slp1.compareTo(slp2);
		}
	}
	
	
 }
