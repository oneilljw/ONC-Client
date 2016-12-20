package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import au.com.bytecode.opencsv.CSVReader;
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
	private static final int ORGANIZATION_DB_HEADER_LENGTH = 27;
	private static final int STATUS_NO_ACTION_YET = 0;
	private static final int STATUS_CONFIRMED = 5;
	private static final int ORG_TYPE_CLOTHING = 4;
//	private static final int ORG_TYPE_COAT = 5;
	private static final int MAX_ORGANIZATION_ID_LENGTH = 10;
	
	private static PartnerDB instance = null;
	private ArrayList<ONCPartner> orgsAL;	//The list of Organizations
//	private ArrayList<String> cOrgs;	//The list of confirmed Organizations
	private GlobalVariables orgGVs;
	private UserDB userDB;
	
	private PartnerDB()
	{
		super(DB_TYPE);
		//Instantiate the organization and confirmed organization lists
		orgsAL = new ArrayList<ONCPartner>();
//		cOrgs = new ArrayList<String>();
		orgGVs = GlobalVariables.getInstance();
		userDB = UserDB.getInstance();
	}
	
	public static PartnerDB getInstance()
	{
		if(instance == null)
			instance = new PartnerDB();
		
		return instance;
	}

	public void readOrgALObject(ObjectInputStream ois)
	{
		ArrayList<ONCPartner> orgDB = new ArrayList<ONCPartner>();
	
		//Read the ONC Family data base and ONC region ranges
		try {
			orgDB = (ArrayList<ONCPartner>) ois.readObject();
			orgsAL.clear();
			for(ONCPartner o: orgDB)
			{
				orgsAL.add(o);
//				if(o.getStatus() == STATUS_CONFIRMED)			
//					cOrgs.add(o.getName());
			}
			
			orgDB.clear();
		} 
		
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeOrgALObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(orgsAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			o.setDateChanged(orgGVs.getTodaysDate());
			o.setStoplightChangedBy(userDB.getUserLNFI());
//			addConfirmedOrganization(o.getName());
		}
		else if(o.getStatus() == STATUS_CONFIRMED && newstatus != STATUS_CONFIRMED)
		{
			//A change request from confirmed status to a different status has occurred. If the number of
			//ornaments is not zero, can't change the status If the number or ornaments
			//assigned is zero, delete the organization from the confirmed array list
			if(o.getNumberOfOrnamentsAssigned() == 0)
			{
				o.setStatus(newstatus);
				o.setDateChanged(orgGVs.getTodaysDate());
				o.setStoplightChangedBy(userDB.getUserLNFI());
//				deleteConfirmedOrganization(o.getName());
			}		
		}
		else	//Status change does not involve to/from confirmed status
		{
			o.setStatus(newstatus);
			o.setDateChanged(orgGVs.getTodaysDate());
			o.setStoplightChangedBy(userDB.getUserLNFI());
		}
			
		return o.getStatus();
	}
	
	void clear()
	{
		orgsAL.clear();
//		cOrgs.clear();
	}
	
	//implementation of abstract classes
	List<ONCPartner> getList() {return orgsAL; }
	
	ONCPartner getObjectAtIndex(int on) { return orgsAL.get(on); }
	
	ONCPartner getOrganizationByID(int id )
	{
		int index = 0;
		while(index < orgsAL.size() && orgsAL.get(index).getID() != id)
			index++;
		
		if(index < orgsAL.size())
			return orgsAL.get(index);
		else
			return null;
	}
	
	ONCPartner getOrganizationByNameAndType(String name, int type )
	{
		int index = 0;
		while(index < orgsAL.size() && orgsAL.get(index).getType() != type && 
				!orgsAL.get(index).getName().equals(name))
			index++;
		
		if(index < orgsAL.size())
			return orgsAL.get(index);
		else
			return null;
	}
	
	int size() { return orgsAL.size(); }
	
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
    			if((sn = getListIndexByID(orgsAL, Integer.parseInt(data))) > -1)
    				searchAL.add(getObjectAtIndex(sn).getID());
    		}	
    	}
    	else	//Check for partner name, email matches, 1st or 2nd contact matches
    	{
//			for(int i=0; i<this.getNumberOfOrganizations(); i++)
    		searchType = "Partner Name, Email or Contacts";
			for(ONCPartner o:orgsAL)
			{
				if(o.getName().toLowerCase().contains(data.toLowerCase()) ||
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
		for(ONCPartner org:orgsAL)
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
	
	/**********************************************************************************
	 * This method adds a organization to the CONFIRMED organization list. cOrgs is an
	 * array list of strings, each list item is the name of a CONFIRMED organization
	 * @param orgname. The method receives the organization name to be added as a string
	 * passed in the method call. 
	 **********************************************************************************/
//	void addConfirmedOrganization(String orgname)
//	{
//		cOrgs.add(orgname);
//		structureConfirmedOrgsList();	//Sort by org type and alphabetically
//	}
	
//	void deleteConfirmedOrganization(String orgname) { cOrgs.remove(orgname); }
	
//	int getNumberOfConfirmedOrganizations() { return cOrgs.size(); }
	
//	ArrayList<String> getConfirmedOrgs() { return cOrgs; }
	
	/*****************************************************************************************
	 * Creates a list of confirmed organizations that take ornaments, broken into two parts.
	 * The top half of the list are confirmed businesses, churches and schools, sorted alphabetically.
	 * The bottom half of the list are all other confirmed organizations sorted alphabetically
	 *****************************************************************************************/
	List<ONCPartner> getConfirmedOrgList(GiftCollection collectionType)
	{
		//Create two lists, the list to be returned and a temporary list
		ArrayList<ONCPartner> confOrgList = new ArrayList<ONCPartner>();
		ArrayList<ONCPartner> confOrgOtherList = new ArrayList<ONCPartner>();
		
		//Add the confirmed business, church and schools to the returned list and add all other 
		//confirmed organizations to the temporary list
		for(ONCPartner o: orgsAL)
		{
			if(o.getStatus() == STATUS_CONFIRMED && o.getGiftCollectionType() == collectionType && 
				o.getType() < ORG_TYPE_CLOTHING)
				confOrgList.add(o);
			else if(o.getStatus() == STATUS_CONFIRMED && o.getGiftCollectionType() == collectionType)
				confOrgOtherList.add(o);		
		}
		
		//Sort the two lists alphabetically by organization name
		OrgNameComparator nameComparator = new OrgNameComparator();
		Collections.sort(confOrgList, nameComparator);	//Sort alphabetically
		Collections.sort(confOrgOtherList, nameComparator);	//Sort alphabetically
		
		//Append the all other temporary confirmed list to the bottom of the confirmed list
		for(ONCPartner otherOrg:confOrgOtherList)
			confOrgList.add(otherOrg);
		
		//return the integrated list
		return confOrgList;
	}
	
	/******************************************************************************************
	 * This method returns an list of the names of confirmed organizations. The top part of the
	 * list will be organizations that are businesses, churches, or schools. The bottom part of the list
	 * will be clothing or coat organizations. Each part is alphabetized. If the organization's 
	 * type is clothing or coat, the name is inverted such that it appears as last name, first
	 * name to make it easy to find alphabetically
	 * @return
	 *****************************************************************************************/
/*	
	void structureConfirmedOrgsList()
	{
		ArrayList<String> cOrgsGifts = new ArrayList<String>();
		ArrayList<String> cOrgsClothesOrCoats = new ArrayList<String>();
		
		cOrgs.clear();
		
		for(Organization org: orgsAL)
		{
			//Sort the organizations by  type
			if(org.getStatus() == STATUS_CONFIRMED)
			{
				//If clothing or coat donor, invert name and add to list
				if(org.getType() == ORG_TYPE_CLOTHING || org.getType() == ORG_TYPE_COAT)
					cOrgsClothesOrCoats.add(org.getName());
				else	//must be a business, church or school
					cOrgsGifts.add(org.getName());
			}
		}
		
		//Alphabetize each list and then merge clothes and coats into gifts and return
		Collections.sort(cOrgsGifts, Collator.getInstance());	//Sort alphabetically
		Collections.sort(cOrgsClothesOrCoats, Collator.getInstance());	//Sort alphabetically
		
		for(String org:cOrgsGifts)
			cOrgs.add(org);
		
		for(String org:cOrgsClothesOrCoats)
			cOrgs.add(org);		
	}
		
	String invertOrgName(String orgName)
	{
		if(!orgName.isEmpty())
		{
			StringBuffer firstName = new StringBuffer("");
			String[] orgNameParts = orgName.trim().split(" ");
		
			for(int i=0; i<orgNameParts.length - 1; i++)
				firstName.append(orgNameParts[i] + " ");
		
			String lastName = orgNameParts[orgNameParts.length-1];
		
			return lastName + ", " + firstName.toString().trim();
		}
		else
			return orgName;
	}

	String getConfirmedOrganizationName(int orgID)
	{
		String name = "None";
		if(orgID > 0) 	//Org has been assigned 
		{
			int index = 0;			
			while(index < orgsAL.size() && orgsAL.get(index).getID() != orgID )
				index++;
		
			if(index==orgsAL.size())
				name = "Org not Found";
			else 
				name = orgsAL.get(index).getName();
		}
		
		return name;
	}
*/	
	/***************************************************************************************************************
	 * This method takes the selected index from a COrgs combo box and returns the ID of the selected organization.
	 * The method compares the name of the selected organization to the name of the organizations in the data base.
	 * It will invert the names of clothing and coat organizations to match how they appear in a selection list. 
	 * @param selnum
	 * @return
	 *************************************************************************************************************/
/*	
	int getConfirmedOrganizationID(int selnum)
	{
		int id = 0;
		
		if(selnum > 0)	//If selected index == 0 user has reset the assignee to "None"
		{
			int index = 0;			
			while(index < orgsAL.size() && !orgsAL.get(index).getName().equals(cOrgs.get(selnum-1)))
				index++;
			
			//Check if org was found in the array list and status == confirmed;
			if(index < orgsAL.size())
				id=orgsAL.get(index).getID();	
		}
		return id;
	}
*/	
/*	
	@Override
	Integer getListIndexByID(Integer orgID)	//returns position in array list or -1 if not found
	{
		int index = 0;
		
		while(index < orgsAL.size() && orgsAL.get(index).getID() != orgID )
			index++;
		
		if(index < orgsAL.size())	//The org was found 
			return index;
		else
			return -1;	
	}
	
	int getConfirmedOrganizationIndex(int orgID)
	{
		int cbIndex = 0, i = 0;
		
		while(i < orgsAL.size() && orgsAL.get(i).getID() != orgID )
			i++;
		
		if(i < orgsAL.size() && orgsAL.get(i).getStatus() == STATUS_CONFIRMED)
		{
			//The org was found and is confirmed, find the index of the org in the cOrgs array
			int ci = 0;
			while(ci < cOrgs.size() && !cOrgs.get(ci++).equals(orgsAL.get(i).getName()));				
			cbIndex = ci;
		}
		
		return cbIndex;	//Org isn't in the org array list or the org isn't confirmed currently
	}
*/	
//	int incrementConfirmedOrgOrnAssigned(int orgID)
//	{
//		//find org by id
//		int i = 0;
//		while(i < orgsAL.size() && orgsAL.get(i).getID() != orgID )
//			i++;
//		
//		//if found, increment
//		if(i<orgsAL.size())
//			return orgsAL.get(i).incrementOrnAssigned();
//		else
//			return 0;
//	}
	
	int decrementConfirmedOrgOrnAssigned(int orgID)
	{
		//find org by id
		int i = 0;
		while(i < orgsAL.size() && orgsAL.get(i).getID() != orgID )
			i++;
		
		//if found, decrement
		if(i<orgsAL.size())
			return orgsAL.get(i).decrementOrnAssigned();
		else
			return 0;
		
	}
	
	void deleteChildWishAssignments(ONCChild delChild)
	{
		ChildWishDB cwDB = ChildWishDB.getInstance();
		
		//For each of the three wishes, if wish assignment has been made, decrement the
		//wish counts for the assignee
		for(int wn=0; wn< NUMBER_OF_WISHES_PER_CHILD; wn++)
		{
			ONCChildWish cw = cwDB.getWish(delChild.getChildWishID(wn));
			if(cw != null && cw.getChildWishAssigneeID() > 0)
				decrementConfirmedOrgOrnAssigned(cw.getChildWishAssigneeID());
		}
	}
/*	
	void rebaselineWishesAssigned(ChildDB cDB)
	{
		//For each confirmed organization, examine each current wish and count the number
		//assigned to the confirmed organization.
		
		for(Organization o:orgsAL)
		{
			int wishcount = 0;
			if(o.getStatus() == STATUS_CONFIRMED)
				for(ONCChild c:cDB.getChildDB())
					wishcount += c.compareCurrentWishAssignees(o.getOrgID());	//0 to 3 in ONC
			
			o.setNumberOfOrnamentsAssigned(wishcount);
		}
	}
*/
	void resetAllOrgsStatus()
	{
		for(ONCPartner o:orgsAL)
			o.setStatus(STATUS_NO_ACTION_YET);
//		cOrgs.clear();	
	}
	
	//Overloaded sortDB methods allow user to specify a data base to be sorted
	//or use the current data base
	boolean sortDB(ArrayList<ONCPartner> oal, String dbField) { return sortList(oal, dbField); }
	boolean sortDB(String dbField) { return sortList(orgsAL, dbField); }
	
	private boolean sortList(ArrayList<ONCPartner> oal, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("Partner")) {
			Collections.sort(oal, new OrgNameComparator()); }
		else if(dbField.equals("Status")) {
			Collections.sort(oal, new OrgStatusComparator()); }
		else if(dbField.equals("Type")) {
			Collections.sort(oal, new OrgTypeComparator()); }
		else if(dbField.equals("Collection")) {
			Collections.sort(oal, new OrgCollectionComparator()); }
		else if(dbField.equals("Req")) {
			Collections.sort(oal, new OrgOrnReqComparator()); }
		else if(dbField.equals("Assigned")) {
			Collections.sort(oal, new OrgOrnAssignedComparator()); }
		else if(dbField.equals("Delivery Information")) {
			Collections.sort(oal, new OrgDeliveryInfoComparator()); }
		else if(dbField.equals("Date Changed")) {
			Collections.sort(oal, new OrgDateChangedComparator()); }
		else if(dbField.equals("Changed By")) {
			Collections.sort(oal, new OrgChangedByComparator()); }
		else if(dbField.equals("Reg")) {
			Collections.sort(oal, new OrgRegionComparator()); }
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
			orgsAL = gson.fromJson(response, listtype);	
					
			if(!response.startsWith("NO_PARTNERS"))
			{
				response = "PARTNERS_LOADED";
				fireDataChanged(this, "LOADED_PARTNERS", null);
			}
		}
		
		return response;
	}
	
	String importOrgDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "OrgDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Organization DB .csv file to import");	
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
	    			if(header.length == ORGANIZATION_DB_HEADER_LENGTH)
	    			{
	    				orgsAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{	
	    					ONCPartner newOrg = new ONCPartner(nextLine);
	    					orgsAL.add(newOrg);
	    				
//	    					if(newOrg.getStatus() == STATUS_CONFIRMED)
//	    						cOrgs.add(orgsAL.get(orgsAL.size()-1).getName());	
	    				}
	    				
//	    				structureConfirmedOrgsList();	//Sort by org type and alphabetically
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Organization DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Organizationl DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Organization DB file: " + filename, 
	    					"Invalid Organization DB File", JOptionPane.ERROR_MESSAGE, oncIcon);
	    		
	    		reader.close();
	    		
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Organization DB file: " + filename, 
    				"Organization DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	
	String exportDBToCSV(String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(GlobalVariables.getFrame());
    		oncwritefile= fc.getFile("Select .csv file to save Org DB to",
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
	    		 String[] header = {"Org ID", "Status", "Type", "Name", "Orn Delivered", "Street #",
	    				 			"Street", "Unit", "City", "Zip", "Region", "Phone",
	    				 			"Orn Requested", "Orn Assigned", "Other",
	    				 			"Deliver To", "Special Notes",
	    				 			"Contact", "Contact Email", "Contact Phone",
	    				 			"Contact2", "Contact2 Email", "Contact2 Phone",
	    				 			"Time Stamp", "Stoplight Pos", "Stoplight Mssg", "Stoplight C/B"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCPartner o: orgsAL)
	    	    	writer.writeNext(o.getExportRow());	//Get family data
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(GlobalVariables.getFrame(), oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }
	/**********************************************************************************
	 * This method is called to change organization/partner assigned counts. It is 
	 * called by another part of the client data base, usually the ChildWishDB component.
	 * 
	 * @param decOrgID
	 * @param incOrgID
	 */
	void processGiftAssignmentChange(ONCChildWish replacedWish, ONCChildWish addedWish)
	{
		//Find the the current partner being decremented
		ONCPartner oldWishAssignee = null;
		ONCPartner newWishAssignee = null;
		
		if(replacedWish != null)
		{
			oldWishAssignee = (ONCPartner) find(orgsAL, replacedWish.getChildWishAssigneeID());
			if(oldWishAssignee != null)
				oldWishAssignee.decrementOrnAssigned();
		}
			
//		int index = 0;
//		while(index < orgsAL.size() && orgsAL.get(index).getID() != wac.getOldData())
//			index++;
//		
//		//Decrement the gift assigned count for the partner being replaced
//		if(index < orgsAL.size())
//			orgsAL.get(index).decrementOrnAssigned();
		
		if(addedWish != null)
		{
			newWishAssignee = (ONCPartner) find(orgsAL, addedWish.getChildWishAssigneeID());
			if(newWishAssignee != null)
				newWishAssignee.incrementOrnAssigned();
		}
		
		fireDataChanged(this, "WISH_PARTNER_CHANGED", oldWishAssignee, newWishAssignee);
		
//		//Find the the current partner being incremented
//		index = 0;
//		while(index < orgsAL.size() && orgsAL.get(index).getID() != wac.getNewData())
//			index++;
//				
//		//Increment the gift assigned count for the partner being replaced
//		if(index < orgsAL.size())
//			orgsAL.get(index).incrementOrnAssigned();
	}
	
	void processGiftDelivered(int partnerID)
	{
		ONCPartner partner = (ONCPartner) find(orgsAL, partnerID);
		if(partner != null)
		{
			//increment the delivered count
			partner.incrementOrnDelivered();
			
			//notify the gui's that the partners delivered count changed
			fireDataChanged(this, "PARTNER_ORNAMENT_DELIVERED", partner);
		}
	}
	
	public void processGiftReceivedChange(DataChange wgr) 
	{
		int index;
		//If there is a  current partner being decremented, find them
		if(wgr.getOldData() > -1)
		{
			index = 0;
			while(index < orgsAL.size() && orgsAL.get(index).getID() != wgr.getOldData())
				index++;
				
			//Decrement the gift received count for the partner being replaced
			if(index < orgsAL.size())
				orgsAL.get(index).decrementOrnReceived();
		}
				
		//Find the the current partner the gift was received from, if any
		if(wgr.getNewData() > -1)
		{
			index = 0;
			while(index < orgsAL.size() && orgsAL.get(index).getID() != wgr.getNewData())
				index++;
						
			//Increment the gift received count for the partner 
			if(index < orgsAL.size())
			orgsAL.get(index).incrementOrnReceived();
		}
	}
	
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
		//Store added organization in local data base
		Gson gson = new Gson();
		ONCPartner addedOrg = gson.fromJson(json, ONCPartner.class);
		orgsAL.add(addedOrg);
		
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_PARTNER", addedOrg);
		
		//determine if added organization is confirmed. If so, add to confirmed
		//Organization list
		if(addedOrg.getStatus() == STATUS_CONFIRMED)
		{
//			addConfirmedOrganization(addedOrg.getName());
			
			//Notify ui's that a confirmed organization is added
			fireDataChanged(source, "ADDED_CONFIRMED_PARTNER", addedOrg);
		}	
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
		//remove deleted organization in local data base
		Gson gson = new Gson();
		ONCPartner deletedOrg = gson.fromJson(json, ONCPartner.class);
		
		int index=0;
		while(index < orgsAL.size() && orgsAL.get(index).getID() != deletedOrg.getID())
			index++;
		
		//If deleted partner was found, remove it and notify ui's
		if(index < orgsAL.size())
		{
			orgsAL.remove(index);
			fireDataChanged(source, "DELETED_PARTNER", deletedOrg);
		}
	
		//determine if deleted organization is confirmed. If so, remove from confirmed
		//organization list
		if(deletedOrg.getStatus() == STATUS_CONFIRMED)
		{
//			deleteConfirmedOrganization(deletedOrg.getName());
			
			//Notify ui's that a confirmed organization is added
			fireDataChanged(source, "DELETED_CONFIRMED_PARTNER", deletedOrg);
		}	
	}
	
	void processUpdatedPartner(Object source, String json)
	{
		Gson gson = new Gson();
		ONCPartner updatedOrg = gson.fromJson(json, ONCPartner.class);
		
		//store updated organization in the Organization data base
		int index = 0;
		while(index < orgsAL.size() && orgsAL.get(index).getID() != updatedOrg.getID())
			index++;
		
		if(index < orgsAL.size())
		{
			ONCPartner replacedOrg = orgsAL.get(index);	//use replaced org for change assessment
			orgsAL.set(index,  updatedOrg);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_PARTNER", updatedOrg);
			
			//If status has changed to or from confirmed or if the organization is still confirmed
			//and the type has changed, or if the collection type has changed, update the wish assignee 
			//lists by firing an UPDATED_CONFIRMED_PARTNER message
			if(updatedOrg.getStatus() == STATUS_CONFIRMED && replacedOrg.getStatus() != STATUS_CONFIRMED ||
			    updatedOrg.getStatus() != STATUS_CONFIRMED && replacedOrg.getStatus() == STATUS_CONFIRMED ||
			     updatedOrg.getStatus() == STATUS_CONFIRMED && updatedOrg.getType() != replacedOrg.getType() ||
			      updatedOrg.getStatus() == STATUS_CONFIRMED  && 
			      !updatedOrg.getGiftCollectionType().equals(replacedOrg.getGiftCollectionType()))
			{
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER", updatedOrg);
			}
			
			//If status is still confirmed and the name has changed by firing an 
			//UPDATED_CONFIRMED_PARTNER_NAME message
			if(updatedOrg.getStatus() == STATUS_CONFIRMED && !updatedOrg.getName().equals(replacedOrg.getName()))
			{
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER_NAME", updatedOrg);
			}
			
/*			
			if(replacedOrg.getStatus() == STATUS_CONFIRMED && updatedOrg.getStatus() != STATUS_CONFIRMED)
			{
				deleteConfirmedOrganization(updatedOrg.getName());
				structureConfirmedOrgsList();
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER", updatedOrg);
			}
			else if(replacedOrg.getStatus() != STATUS_CONFIRMED && updatedOrg.getStatus() == STATUS_CONFIRMED)
			{
				addConfirmedOrganization(updatedOrg.getName());
				structureConfirmedOrgsList();
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER", updatedOrg);
			}
			else if(replacedOrg.getType() != updatedOrg.getType())
			{
				//may have been a change from business, church or school to coat or clothing
				//or vice versa
				structureConfirmedOrgsList();
				fireDataChanged(source, "UPDATED_CONFIRMED_PARTNER", updatedOrg);
			}
*/			
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
	
	private class OrgNameComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getName().compareTo(o2.getName());
		}
	}
	private class OrgStatusComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer s1 = o1.getStatus();
			Integer s2 = o2.getStatus();
			return s1.compareTo(s2);	
		}
	}
	private class OrgTypeComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer t1 = o1.getType();
			Integer t2 = o2.getType();
			return t1.compareTo(t2);
		}
	}
	private class OrgCollectionComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			return o1.getGiftCollectionType().compareTo(o2.getGiftCollectionType());
		}
	}
	private class OrgOrnReqComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			Integer or1 = o1.getNumberOfOrnamentsRequested();
			Integer or2 = o2.getNumberOfOrnamentsRequested();
			return or1.compareTo(or2);
		}
	}
	private class OrgOrnAssignedComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{
			Integer oa1 = o1.getNumberOfOrnamentsAssigned();
			Integer oa2 = o2.getNumberOfOrnamentsAssigned();
			return oa1.compareTo(oa2);
		}
	}
	private class OrgDeliveryInfoComparator implements Comparator<ONCPartner>
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
	private class OrgDateChangedComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getDateChanged().compareTo(o2.getDateChanged());
		}
	}
	private class OrgChangedByComparator implements Comparator<ONCPartner>
	{
		@Override
		public int compare(ONCPartner o1, ONCPartner o2)
		{			
			return o1.getStoplightChangedBy().compareTo(o2.getStoplightChangedBy());
		}
	}
	private class OrgRegionComparator implements Comparator<ONCPartner>
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
