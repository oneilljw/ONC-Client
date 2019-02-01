package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class ChildGiftDB extends ONCDatabase
{
//	private static final int CHILD_WISH_DB_HEADER_LENGTH = 10;
	private static final int PARTNER_TYPE_ONC_SHOPPER = 6;
	private static final int WISH_INDICATOR_ALLOW_SUBSTITUE = 2;
	private static final String CHILD_WISH_DEFAULT_DETAIL = "Age appropriate";
	
	private static ChildGiftDB instance = null;
	private GiftCatalogDB cat;
	private ChildDB childDB;
	private ArrayList<ONCChildGift> childwishAL;
	
	private ChildGiftDB()
	{	
		super();
		cat = GiftCatalogDB.getInstance();
		childDB = ChildDB.getInstance();
		childwishAL = new ArrayList<ONCChildGift>();
	}
	
	public static ChildGiftDB getInstance()
	{
		if(instance == null)
			instance = new ChildGiftDB();
		
		return instance;
	}
	
	/*******
	 * This is a critical method in the ONC application. A child's wish is never modified or
	 * deleted, a new wish is always created to take an old wish's place. If the new wish 
	 * being added has a base or assignee change, the wish status must be check to see if 
	 * an automatic change needs to occur as well. The new wish, with correct status is 
	 * then sent to the server. The list should never be sorted, that way the newest wish for
	 * a child and wish number is at the bottom of the list
	 * @param currPartner - defines who the requested partner is. null to leave the 
	 * current partner unchanged
	 */
	ONCChildGift add(Object source, int childid, int wishid, String wd, int wn, int wi,
			GiftStatus ws, ONCPartner currPartner)
{		
GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
String cb = UserDB.getInstance().getUserLNFI();
Date dc = gvs.getTodaysDate();

//Get the old wish being replaced. getWish method returns null if wish not found
ONCChildGift replacedWish = getWish(childid, wn);

//determine if we need to change the partner id
int newPartnerID = -1;
if(replacedWish != null && replacedWish.getGiftID() != wishid)
newPartnerID = -1;
else if(currPartner == null && replacedWish != null)
newPartnerID = replacedWish.getPartnerID(); 	//Staying the same
else if(currPartner != null)
newPartnerID = currPartner.getID();

//create the new wish, with childwishID = -1, meaning no wish selected
//the server will add the childwishID and return it
ONCChildGift retCW = null;
ONCChildGift reqCW = new ONCChildGift(-1, childid, wishid,
								   checkForDetailChange(wi, wd, currPartner, replacedWish), 
								   wn, wi,
								   checkForStatusChange(replacedWish, wishid, ws, currPartner), 
								   newPartnerID, cb, dc);		
Gson gson = new Gson();
String response = null, helmetResponse = null;

//send add new wish request to the server
response = serverIF.sendRequest("POST<childwish>" + gson.toJson(reqCW, ONCChildGift.class));

//get the wish in the sever response and add it to the local cache data base
//it contains the wish id assigned by the server child wish data base
//Notify all other ui's that a wish has been added
if(response != null && response.startsWith("WISH_ADDED"))
{
//System.out.println("ChildWish DB_add: Server Response: " + response);
retCW = processAddedWish(source, response.substring(10));

//must check to see if new wish is wish 0 and has changed to/from a 
//Bike. If so, wish 2 must become a Helmet/Empty
ONCChild child = childDB.getChild(retCW.getChildID());
int bikeID = cat.getGiftID("Bike");
int helmetID = cat.getGiftID("Helmet");
if(retCW.getGiftNumber() == 0 && replacedWish != null && replacedWish.getGiftID() != bikeID && 
	retCW.getGiftID() == bikeID || retCW.getGiftNumber() == 0 && replacedWish == null &&
	 retCW.getGiftID() == bikeID)		
{
	//add Helmet as wish 1
	ONCChildGift helmetCW = new ONCChildGift(-1, childid, helmetID, "", 1, 1,
			GiftStatus.Selected, -1, cb, dc);
	helmetResponse = serverIF.sendRequest("POST<childwish>" + gson.toJson(helmetCW, ONCChildGift.class));
	if(helmetResponse != null && helmetResponse.startsWith("WISH_ADDED"))
		processAddedWish(this, helmetResponse.substring(10));
}
//if replaced wish was a bike and now isn't and wish 1 was a helmet, make
//wish one empty
else if(retCW.getGiftNumber() == 0 && replacedWish != null && child.getChildGiftID(1) > -1 &&
		replacedWish.getGiftID() == bikeID && retCW.getGiftID() != bikeID)
{
	//change wish 1 from Helmet to None
	ONCChildGift helmetCW = new ONCChildGift(-1, childid, -1, "", 1, 0,
			GiftStatus.Not_Selected, -1, cb, dc);
	helmetResponse = serverIF.sendRequest("POST<childwish>" + gson.toJson(helmetCW, ONCChildGift.class));
	if(helmetResponse != null && helmetResponse.startsWith("WISH_ADDED"))
		processAddedWish(this, helmetResponse.substring(10));
}
}

return retCW;
}
	
	ONCChildGift processAddedWish(Object source, String json)
	{
		//create the new wish object from the json and get the wish it's replacing
		Gson gson = new Gson();
		ONCChildGift addedWish = gson.fromJson(json, ONCChildGift.class);
		ONCChildGift replacedWish = getWish(addedWish.getChildID(), addedWish.getGiftNumber());
		
//		if(addedWish != null)
//			System.out.println(String.format("ChildWishDB_processAddedWish_addedWish: Child ID: %d, Wish: %s", addedWish.getChildID(), addedWish.getChildWishAll()));
//		if(replacedWish != null)
//		System.out.println(String.format("ChildWishDB_processAddedWish_replacedWish: Child ID: %d, Wish: %s", replacedWish.getChildID(), replacedWish.getChildWishAll()));
		
		//add the new wish to the local data base
		childwishAL.add(addedWish);
			
		//Set the new wish ID in the child object that has been assigned this wish
		//in the child data base
		ChildDB childDB = ChildDB.getInstance();
		childDB.setChildWishID(addedWish.getChildID(), addedWish.getID(), addedWish.getGiftNumber());
		
		//notify the partner data base to evaluate the new wish to see if partner wishes assigned, delivered
		//or received counts have to change
		PartnerDB partnerDB = PartnerDB.getInstance();
		partnerDB.processAddedWish(replacedWish, addedWish);
			
		//data bases have been updated, notify ui's of changes
		fireDataChanged(source, "WISH_ADDED", addedWish);
		
		//notify the catalog to update counts if the wish has changed
		if(replacedWish == null && addedWish.getGiftStatus() == GiftStatus.Selected ||
			replacedWish != null && replacedWish.getGiftID() != addedWish.getGiftID())
		{
			cat.changeGiftCounts(replacedWish, addedWish);				
		}

		return addedWish;
	}
	
	/*******************************************************************************************
	 * This method implements a rules engine governing the relationship between a wish type and
	 * wish status and wish assignment and wish status. It is called when a child's wish or
	 * assignee changes and implements an automatic change of wish status.
	 * 
	 * For example, if a child's base wish is empty and it is changing to a wish selected from
	 * the catalog, this method will set the wish status to CHILD_WISH_SELECTED. Conversely, if
	 * a wish was selected from the catalog and is reset to empty, the wish status is set to
	 * CHILD_WISH_EMPTY.
	 ************************************************************************************************************/	
	GiftStatus checkForStatusChange(ONCChildGift oldWish, int wishBase, GiftStatus reqStatus, ONCPartner reqOrg)
	{
		GiftStatus currStatus, newStatus;
		
		if(oldWish == null)	//Creating first wish
			currStatus = GiftStatus.Not_Selected;
		else	
			currStatus = oldWish.getGiftStatus();
		
		//set new status = current status for default return
		newStatus = currStatus;
		
		switch(currStatus)
		{
			case Not_Selected:
				if(wishBase > -1 && reqOrg != null && reqOrg.getID() != -1)
					newStatus = GiftStatus.Assigned;	//wish assigned from inventory
				else if(wishBase > -1)
					newStatus = GiftStatus.Selected;
				break;
				
			case Selected:
				if(wishBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(reqOrg != null && reqOrg.getID() != -1)
					newStatus = GiftStatus.Assigned;
				break;
				
			case Assigned:
				if(wishBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(oldWish.getGiftID() != wishBase)
					newStatus = GiftStatus.Selected;
				else if(reqOrg == null || reqOrg != null && reqOrg.getID() == -1)
					newStatus = GiftStatus.Selected;
				else if(reqStatus == GiftStatus.Delivered)
					newStatus = GiftStatus.Delivered;
				break;
				
			case Delivered:
				if(reqStatus == GiftStatus.Returned)
					newStatus = GiftStatus.Returned;
				else if(reqStatus == GiftStatus.Delivered && reqOrg != null && 
							reqOrg.getID() > -1 && reqOrg.getType() == PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Delivered && reqOrg != null && reqOrg.getID() > -1)
					newStatus = GiftStatus.Assigned;
				else if(reqStatus == GiftStatus.Shopping)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Received)
					newStatus = GiftStatus.Received;
				break;
				
			case Returned:
				if(wishBase == -1)
					newStatus = GiftStatus.Not_Selected;
				else if(reqOrg != null && reqOrg.getID() == -1)
					newStatus = GiftStatus.Selected;
				else if(reqOrg != null && reqOrg.getType() != PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Assigned;
				else if(reqOrg != null && reqOrg.getType() == PARTNER_TYPE_ONC_SHOPPER)
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
				else if(reqOrg != null && reqOrg.getType() == PARTNER_TYPE_ONC_SHOPPER)
					newStatus = GiftStatus.Shopping;
				else if(reqStatus == GiftStatus.Assigned && reqOrg != null && reqOrg.getID() > -1)
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
	
	/*** checks for automatic change of wish detail. An automatic change is triggered if
	 * the replaced wish is of status Delivered and requested parter is of type ONC Shopper
	 * and the requested wish indicator is #. 
	 */
	
	String checkForDetailChange(int reqWishRes, String reqWishDetail,
									ONCPartner reqPartner, ONCChildGift replWish)
	{
//		if(replWish != null && reqPartner != null)
//			System.out.println(String.format("ChildWishDB.checkforDetailChange: replWishStatus= %s, reqWishInd= %d, reqPartnerType = %d, reqDetail= %s",
//				replWish.getChildWishStatus().toString(), reqWishRes, reqPartner.getType(),
//				reqWishDetail));
	
		if(replWish != null && reqPartner != null && 
			replWish.getGiftStatus() == GiftStatus.Delivered && 
			 reqPartner.getType() == PARTNER_TYPE_ONC_SHOPPER && 
			  reqWishRes == WISH_INDICATOR_ALLOW_SUBSTITUE)
		{
			return CHILD_WISH_DEFAULT_DETAIL;
		}
		else
			return reqWishDetail;
	}

	/**
	 * This method removes a child's wishes from the child wish database. It is called
	 * when a child is deleted.
	 * @param childid
	 */
	ArrayList<GiftBaseChange> deleteChildWishes(ONCChild delChild)
	{
		//Create the list of wish base changes for wishes selected or higher status
		ArrayList<GiftBaseChange> wishbasechangelist = new ArrayList<GiftBaseChange>();
		for(int wn=0; wn<NUMBER_OF_WISHES_PER_CHILD; wn++)
		{
			ONCChildGift cw = getWish(delChild.getChildGiftID(wn));
			if(cw != null && cw.getGiftStatus().compareTo(GiftStatus.Selected) >= 0)
				wishbasechangelist.add(new GiftBaseChange(cw, null));	
		}
		
		//delete the wishes from local cache
		for(int index=0; index < childwishAL.size(); index++)
			if(childwishAL.get(index).getChildID() == delChild.getID())
			{
				childwishAL.remove(index);
				index--;
			}
		
		return wishbasechangelist;
	}
	
	String update(Object source, ONCObject oncchildwish)
	{
		//A wish is not updated in the current design. A new wish is always created 
		//and added to the data base. This allows a child's wish history to be 
		//preserved for a season. 
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_child_wish>" + 
											gson.toJson(oncchildwish, ONCChildGift.class));
		
		if(response.startsWith("UPDATED_CHILD_WISH"))
		{
			processUpdatedObject(source, response.substring(18), childwishAL);
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
		ONCChildGift updatedWish = (ONCChildGift) updatedObj;
		childwishAL.set(index,  updatedWish);
	}

	ONCChildGift getWish(int wishid)
	{
		int index = childwishAL.size() -1;
		
		//Search from the bottom of the data base for speed. New wishes are added to the bottom
		while (index >= 0 && childwishAL.get(index).getID() != wishid)
			index--;
		
		if(index == -1)
			return null;	//Wish wasn't found in data base
		else
			return childwishAL.get(index);
	}
	
	ONCChildGift getWish(int childid, int wn)
	{
		int index = childwishAL.size() -1;
		
		//Search from the bottom of the data base for speed. New wishes are added to the bottom
		while (index >= 0 && (childwishAL.get(index).getChildID() != childid || 
								childwishAL.get(index).getGiftNumber() != wn))
			index--;
		
		if(index == -1)
			return null;	//Wish wasn't found in data base
		else
			return childwishAL.get(index);
	}
	
	List<ONCChildGift> getWishHistory(int childID, int wn)
	{
		List<ONCChildGift> cwhList = new ArrayList<ONCChildGift>();
		Gson gson = new Gson();
		
		HistoryRequest req = new HistoryRequest(childID, wn);
		String response = serverIF.sendRequest("GET<wishhistory>"+ 
											gson.toJson(req, HistoryRequest.class));
		
		if(response != null)
		{
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();	
			cwhList = gson.fromJson(response, listtype);
		}
		
		return cwhList;
	}
	
	int getNumberOfWishesPerChild() { return NUMBER_OF_WISHES_PER_CHILD; }
	
	ArrayList<ONCChildGift> getList() { return childwishAL; }
	
	String importChildWishDatabase()
	{
		String response = "NO_WISHES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChildGift>>(){}.getType();
			
			response = serverIF.sendRequest("GET<childwishes>");
			childwishAL = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_WISHES"))
			{
				response =  "WISHES_LOADED";
				fireDataChanged(this, "LOADED_WISHES", null);
			}
		}
		
		return response;
	}
/*	
	String importChildWishDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
    		
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "ChildWishDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Child Wish DB .csv file to import");	
    		chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    		returnVal = chooser.showOpenDialog(pf);
    		pyfile = chooser.getSelectedFile();
		}
		
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	filename = pyfile.getName();
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == CHILD_WISH_DB_HEADER_LENGTH)
	    			{
	    				childwishAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					childwishAL.add(new ONCChildWish(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Child Wish DB file corrupted, header length = " + Integer.toString(header.length), 
    						"InvalidChild Wish DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Child Wish db file: " + filename, 
	    					"Invalid Child Wish DB File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Child Wish db file: " + filename, 
    				"Child Wish DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
*/	
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Child Wish DB to",
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
	    		 String[] header = {"Child Wish ID", "Child ID", "Wish ID", "Detail",
	    				 			"Wish #", "Restrictions", "Status",
	    				 			"Changed By", "Time Stamp", "Org ID"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCChildGift cw:childwishAL)
	    	    	writer.writeNext(cw.getExportRow());	//Get family data
	    	 
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
			processAddedWish(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_CHILD_WISH"))
		{
//			System.out.println(String.format("ChildWishDB Server Event, Source: %s, Type: %s, Json: %s",
//					ue.getSource().toString(), ue.getType(), ue.getJson()));
			processUpdatedObject(this, ue.getJson(), childwishAL);
		}		
	}
}