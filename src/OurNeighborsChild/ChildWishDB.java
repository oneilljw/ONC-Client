package OurNeighborsChild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import au.com.bytecode.opencsv.CSVWriter;

public class ChildWishDB extends ONCDatabase
{
//	private static final int CHILD_WISH_DB_HEADER_LENGTH = 10;
	private static final int CHILD_WISH_STATUS_EMPTY = 1;
	private static final int CHILD_WISH_STATUS_SELECTED = 2;
	private static final int CHILD_WISH_STATUS_ASSIGNED = 3;
	private static final int CHILD_WISH_STATUS_RECEIVED = 4;
	
	private static ChildWishDB instance = null;
	private ONCWishCatalog cat;
	private ArrayList<ONCChildWish> childwishAL;
	
	private ChildWishDB()
	{	
		super();
		cat = ONCWishCatalog.getInstance();
		childwishAL = new ArrayList<ONCChildWish>();
	}
	
	public static ChildWishDB getInstance()
	{
		if(instance == null)
			instance = new ChildWishDB();
		
		return instance;
	}
	
	/*******
	 * This is a critical method in the ONC application. A child's wish is never modified or
	 * deleted, a new wish is always created to take an old wish's place. If the new wish 
	 * being added has a base or assignee change, the wish status must be check to see if 
	 * an automatic change needs to occur as well. The new wish, with correct status is 
	 * then sent to the server. 
	 */
	int add(Object source, int childid, int wishid, String wd, int wn, int wi, int ws, int waID, String cb, Date dc)
	{		
		//Get the old wish being replaced. getWish method returns null if wish not found
		ONCChildWish oldWish = getWish(childid, wn);
		
		//Determine if the status needs to change automatically
		int wishstatus = checkForStatusChange(oldWish, wishid, ws, waID);
		
		//create the new wish, with childwishID = -1, meaning no wish selected
		//the server will add the childwishID and return it
		int newWishID = -1;
		ONCChildWish cw = new ONCChildWish(-1, childid, wishid, wd, wn, wi,
											wishstatus, waID, cb, dc);		
		Gson gson = new Gson();
		String response = null;
		
		//send add new wish request to the server
		response = serverIF.sendRequest("POST<childwish>" + gson.toJson(cw, ONCChildWish.class));
		
		//get the wish in the sever response and add it to the local cache data base
		//it contains the wish id assigned by the server child wish data base
		//Notify all other ui's that a wish has been added
//		System.out.println(String.format("ChildWishDB: addWish Sever Response: %s", response));
		if(response != null && response.startsWith("WISH_ADDED"))
		{
//			System.out.println("ChildWish DB_add: Server Response: " + response);
			newWishID = processAddedWish(source, response.substring(10));
		}
		
		return newWishID;
	}
	
	int processAddedWish(Object source, String json)
	{
		//create the new wish object from the json and get the wish it's replacing
		Gson gson = new Gson();
		ONCChildWish addedWish = gson.fromJson(json, ONCChildWish.class);
		ONCChildWish replacedWish = getWish(addedWish.getChildID(), addedWish.getWishNumber());
		
//		if(addedWish != null)
//			System.out.println(String.format("ChildWishDB_processAddedWish_addedWish: Child ID: %d, Wish: %s", addedWish.getChildID(), addedWish.getChildWishAll()));
//		if(replacedWish != null)
//		System.out.println(String.format("ChildWishDB_processAddedWish_replacedWish: Child ID: %d, Wish: %s", replacedWish.getChildID(), replacedWish.getChildWishAll()));
		
		//add the new wish to the data base
		childwishAL.add(addedWish);
			
		//Set the new wish ID in the child object that has been assigned this wish
		//in the child data base
		ChildDB childDB = ChildDB.getInstance();
		childDB.setChildWishID(addedWish.getChildID(), addedWish.getID(), addedWish.getWishNumber());
		
		//determine if gift assignments have changed. If they have, notify the Organization DB
		//to update partner assignment counts
		ONCOrgs orgDB = ONCOrgs.getInstance();
		DataChange  wac = null;
		if(replacedWish != null && replacedWish.getChildWishAssigneeID() != 
									addedWish.getChildWishAssigneeID())
		{	
			//assignee change -- need to notify to adjust partner gift assignment counts
			wac= new DataChange(replacedWish.getChildWishAssigneeID(), 
								 addedWish.getChildWishAssigneeID());
			
			orgDB.processGiftAssignmentChange(wac);
		}
		
		//determine if gift has been received. If it has, notify the Organization DB
		//to update partner gift received counts
		DataChange  wgr = null;
		if(replacedWish != null && replacedWish.getChildWishStatus() == CHILD_WISH_STATUS_ASSIGNED  && 
				addedWish.getChildWishStatus() == CHILD_WISH_STATUS_RECEIVED &&
				replacedWish.getChildWishAssigneeID() == addedWish.getChildWishAssigneeID())
		{	
			//gift was received from partner it was assigned to
			wgr= new DataChange(-1, addedWish.getChildWishAssigneeID());	
			orgDB.processGiftReceivedChange(wgr);
		}
		else if(replacedWish != null && replacedWish.getChildWishStatus() == CHILD_WISH_STATUS_RECEIVED  && 
				 addedWish.getChildWishStatus() == CHILD_WISH_STATUS_ASSIGNED &&
				 replacedWish.getChildWishAssigneeID() == addedWish.getChildWishAssigneeID())
		{
			//gift was un-received from partner it was assigned to. This occurs when an undo
			//action is performed by the user
			wgr= new DataChange(addedWish.getChildWishAssigneeID(), -1);
			orgDB.processGiftReceivedChange(wgr);
		}
		else if(replacedWish != null && replacedWish.getChildWishStatus() == CHILD_WISH_STATUS_RECEIVED  && 
				 addedWish.getChildWishStatus() == CHILD_WISH_STATUS_RECEIVED &&
				 replacedWish.getChildWishAssigneeID() != addedWish.getChildWishAssigneeID())
		{
			//In theory, this should never occur. However, if a gift is received twice from two
			//different partners, decrement the first and credit the receipt to the second
			wgr= new DataChange(replacedWish.getChildWishAssigneeID(), addedWish.getChildWishAssigneeID());
			orgDB.processGiftReceivedChange(wgr);
		}
			
		//data bases have been updated, notify ui's of changes
		fireDataChanged(source, "WISH_ADDED", addedWish);
		
		if(replacedWish != null && replacedWish.getWishID() != addedWish.getWishID())
		{
			//base change - need to tell wish catalog dialog to adjust wish counts
			WishBaseOrOrgChange wbc= new WishBaseOrOrgChange(replacedWish, addedWish, addedWish.getWishNumber());
			
			cat.changeWishCounts(wbc);	//notify the catalog to update counts
			
			fireDataChanged(source, "WISH_BASE_CHANGED", wbc); //notify the UI's			
		}
		
		if(replacedWish != null && replacedWish.getChildWishStatus() != addedWish.getChildWishStatus())
		{
			//status change - need to notify wish status changed
			DataChange wsc= new DataChange(replacedWish.getChildWishStatus(), 
											addedWish.getChildWishStatus());	
				
			fireDataChanged(source, "WISH_STATUS_CHANGED", wsc);
		}
		
		if(wac != null)
			fireDataChanged(source, "WISH_PARTNER_CHANGED", wac);
		
		if(wgr != null)
			fireDataChanged(source, "WISH_RECEIVED", wgr);
		
		
		return addedWish.getID();
	}
	
	/************************************************************************************************************
	 * This method implements a rules engine governing the relationship between a wish type and wish status and
	 * wish assignment and wish status. It is called when a child's wish  or assignee changes and implements an
	 * automatic change of wish status.
	 * 
	 * For example, if a child's base wish is empty and it is changing to a wish selected from the catalog, this
	 * method will set the wish status to CHILD_WISH_SELECTED. Conversely, if a wish was selected from the catalog
	 * and is reset to empty, the wish status is set to CHILD_WISH_EMPTY.
	 ************************************************************************************************************/
	int checkForStatusChange(ONCChildWish oldWish, int wishBase, int wishStatus, int wishAssigneeID)
	{
		int currentwishstatus;
		if(oldWish == null)	//Creating first wish
			currentwishstatus = CHILD_WISH_STATUS_EMPTY;
		else	
			currentwishstatus = oldWish.getChildWishStatus();
		
		if(currentwishstatus > CHILD_WISH_STATUS_EMPTY && wishStatus > CHILD_WISH_STATUS_ASSIGNED)
			return wishStatus;		//Can receive, distribute or verify any gift without automatic change
		else if(wishBase < 0)
			return CHILD_WISH_STATUS_EMPTY;
		else if(wishBase >= 0  && wishAssigneeID <= 0)
			return CHILD_WISH_STATUS_SELECTED;
		else if(wishBase >= 0 && wishAssigneeID > 0  && currentwishstatus < CHILD_WISH_STATUS_SELECTED)
			return CHILD_WISH_STATUS_SELECTED;
		else
			return wishStatus;				
	}
		
	/******************************************************************************************
	 * This method checks an added wish to see if the base, status or assignee has changed. 
	 * If they have, messages are created to notify the ui's of the change. In addition, if
	 * it's a status change, the FamilyDB (Families) is notified to check for a family
	 * status change. 
	 * @param oldWish
	 * @param addedWish
	 **********************************************************************************/
/*
	void processWishAdded(ONCChildWish oldWish, ONCChildWish addedWish)
	{
		//test to see if base, status or assignee are changing, if the old wish exists
		if(oldWish != null)
		{	
			if(oldWish.getWishID() != addedWish.getWishID())
			{
				//base change - need to tell wish catalog dialog to adjust wish counts
				WishBaseOrOrgChange wbc= new WishBaseOrOrgChange(oldWish.getChildWishBase(),
																  addedWish.getChildWishBase(),
																   addedWish.getWishNumber());
				
				cat.changeWishCounts(wbc);
				
				fireDataChanged(this, "WISH_BASE_CHANGED", wbc);			
			}
					
			if(oldWish.getChildWishStatus() != addedWish.getChildWishStatus())
			{
				//status change - need to notify familyDB of status change
				DataChange wsc= new DataChange(oldWish.getChildWishStatus(), 
												addedWish.getChildWishStatus());	
					
				fireDataChanged(this, "WISH_STATUS_CHANGED", wsc);
			}

			if(oldWish.getChildWishAssigneeID() != addedWish.getChildWishAssigneeID())
			{				
				//assignee change -- need to notify to adjust partner gift assignment counts
				DataChange wac= new DataChange(oldWish.getChildWishAssigneeID(),
												addedWish.getChildWishAssigneeID());
				
				fireDataChanged(this, "WISH_PARTNER_CHANGED", wac);
			}
		}
	}
	
	void processWishDeleted(ONCChildWish oldWish)
	{
		//test to see if base, status or assignee are changing, if the old wish exists
		if(oldWish != null)
		{
			if(!oldWish.getChildWishBase().equals("None"))
			{
				//deleted wish - need to tell wish catalog dialog to adjust wish counts
				WishBaseOrOrgChange wbc= new WishBaseOrOrgChange(oldWish.getChildWishBase(),
																  "None",
																   oldWish.getWishNumber());	
				fireDataChanged(this, "WISH_BASE_CHANGED", wbc);
			}
					
			if(oldWish.getChildWishStatus() > 1)
			{
				//status change - need to notify familyDB of status change
				WishChange wsc= new WishChange(oldWish.getChildWishStatus(), 0);		
				fireDataChanged(this, "WISH_STATUS_CHANGED", wsc);
			}
			
			if(oldWish.getChildWishAssigneeID() > 0)
			{				
				//assignee change -- need to notify to adjust partner gift assignment counts
				WishChange wac= new WishChange(oldWish.getChildWishAssigneeID(), 0);
				fireDataChanged(this, "WISH_PARTNER_CHANGED", wac);
			}
		}
	}
*/	
	/**
	 * This method removes a child's wishes from the child wish database. It is called
	 * when a child is deleted.
	 * @param childid
	 */
	ArrayList<WishBaseOrOrgChange> deleteChildWishes(ONCChild delChild)
	{
		//Create the list of wish base changes for wishes selected or higher status
		ArrayList<WishBaseOrOrgChange> wishbasechangelist = new ArrayList<WishBaseOrOrgChange>();
		for(int wn=0; wn<NUMBER_OF_WISHES_PER_CHILD; wn++)
		{
			ONCChildWish cw = getWish(delChild.getChildWishID(wn));
			if(cw != null && cw.getChildWishStatus() >= CHILD_WISH_STATUS_SELECTED)
				wishbasechangelist.add(new WishBaseOrOrgChange(cat.getWishByID(cw.getWishID()), new ONCWish(-1, "None", 7), wn));	
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
		return null;
	}
/*	
	ArrayList<ONCChildWish> getChildWishHistory(int childid, int wn)
	{	
		ArrayList<ONCChildWish> cwh = new ArrayList<ONCChildWish>();
		
		for(ONCChildWish cw: childwishAL)
			if(cw.getChildID() == childid && cw.getWishNumber() == wn)
				cwh.add(cw);
		
		return cwh;		
	}
*/	
	ONCChildWish getWish(int wishid)
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
	
	ONCChildWish getWish(int childid, int wn)
	{
		int index = childwishAL.size() -1;
		
		//Search from the bottom of the data base for speed. New wishes are added to the bottom
		while (index >= 0 && (childwishAL.get(index).getChildID() != childid || 
								childwishAL.get(index).getWishNumber() != wn))
			index--;
		
		if(index == -1)
			return null;	//Wish wasn't found in data base
		else
			return childwishAL.get(index);
	}
	
//	long getTotalNumberOfChildWishes() { return childwishAL.size(); }
	
	int getNumberOfWishesPerChild() { return NUMBER_OF_WISHES_PER_CHILD; }
	
	ArrayList<ONCChildWish> getChildWishAL() { return childwishAL; }
	
	String importChildWishDatabase()
	{
		ServerIF serverIF = ServerIF.getInstance();
		String response = "NO_WISHES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChildWish>>(){}.getType();
			
			response = serverIF.sendRequest("GET<childwishes>");
			childwishAL = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_WISHES"))		
				response =  "WISHES_LOADED";
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
	String exportChildWishDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Child Wish DB to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
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
	    	    
	    	    for(ONCChildWish cw:childwishAL)
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
			//Create a child wish object for the added child wish and add it to
			//the local child wish cache
			processAddedWish(this, ue.getJson());
		}		
	}
}