package OurNeighborsChild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class Families extends ONCDatabase
{
	/**
	 * This class implements a data base that holds families managed by Our
	 * Neighbor's Child. The class provides a set of I/O functions for
	 * importing and exporting the data base to a .csv file, opening 
	 * and storing the data base, searching the data base to locate families 
	 * using a variety of keywords such as ONCID, ONC #, ODB #, phone number
	 * last name, etc. This class also manages ONC Numbers by region. 
	 * In addition, it provides support for managing families such as
	 * generation of ONC Numbers.
	 */
	private static final int ONC_OPEN_FILE = 0;
	
	private static final Integer MAXIMUM_ONC_NUMBER = 9999;
	private static final int ONC_REBASELINE_REGION_MARGIN = 5;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final int CHILD_WISH_STATUS_EMPTY = 1;
	
	private static Families instance = null;
	private ArrayList<ONCFamily> oncFamAL;	//The list of families
	private int[] oncnumRegionRanges;		//Holds starting ONC number for each region
	private ChildDB childDB;
	private ChildWishDB childwishDB;
	private ONCAgents oncAgentDB;
	private DriverDB driverDB;
	private DeliveryDB deliveryDB;
	private GlobalVariables fGVs;
	
	private Families()
	{
		//Instantiate the organization and confirmed organization lists
		super();
		childDB = ChildDB.getInstance();
		childwishDB = ChildWishDB.getInstance();
		driverDB = DriverDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		oncAgentDB = ONCAgents.getInstance();;
		
		oncFamAL = new ArrayList<ONCFamily>();
		fGVs = GlobalVariables.getInstance();
//		serverIF.addServerListener(this);
		
//		initializeONCNumberRegionRanges();
	}
	
	public static Families getInstance()
	{
		if(instance == null)
			instance = new Families();
		
		return instance;
	}
	
	ONCFamily getObjectAtIndex(int index) { return oncFamAL.get(index); }
	int size() { return oncFamAL.size(); }
	void clear() { oncFamAL.clear(); }
	ArrayList<ONCFamily> getList() { return oncFamAL; }
	
	//get family by ID
	ONCFamily getFamily(int famid)
	{
		int index = 0;
		while(index < oncFamAL.size() && oncFamAL.get(index).getID() != famid)
			index++;
		
		if(index < oncFamAL.size())
			return oncFamAL.get(index);
		else
			return null;	
	}
	
	//Get and set ONC Number region ranges
	String [] getExportONCNumberRegionRangesRow()
	{
		String[] row = new String[oncnumRegionRanges.length];
		
		for(int i=0; i<oncnumRegionRanges.length; i++)
			row[i] = Integer.toString(oncnumRegionRanges[i]);
		
		return row;
	}
	
	String update(Object source, ONCObject oncfamily)
	{
		Gson gson = null;
		String response = "ERROR";
		if(serverIF != null && serverIF.isConnected())
		{
			gson = new Gson();
			response = serverIF.sendRequest("POST<update_family>" + 
											gson.toJson(oncfamily, ONCFamily.class));
			
			//Store the update in the local data base and notify local user IFs 
			//that an update occurred
			if(response.startsWith("UPDATED_FAMILY"))
				processUpdatedObject(source, response.substring(14));		
		}
		
		return response;
	}
	
	String update_AutoONCNum(Object source, ONCObject oncfamily)
	{
		Gson gson = null;
		String response = "";
		if(serverIF != null && serverIF.isConnected())
		{
			
			gson = new Gson();
			response = serverIF.sendRequest("POST<update_family_oncnum>" + 
											gson.toJson(oncfamily, ONCFamily.class));
			
			//Store the update in the local data base and notify local user IFs 
			//that an update occurred
			if(response.startsWith("UPDATED_FAMILY"))
				processUpdatedObject(source, response.substring(14));		
		}
		
		return response;
	}
	
	ONCObject processUpdatedObject(Object source, String json)
	{
//		System.out.println(String.format("Families- processUpdateFamily: json: %s", json));
		//Create a family object for the updated family
		Gson gson = new Gson();
		ONCFamily updatedFamily = gson.fromJson(json, ONCFamily.class);
		
		//Find the position for the current family being replaced
		int index = 0;
		while(index < oncFamAL.size() && oncFamAL.get(index).getID() != updatedFamily.getID())
			index++;
		
		//Replace the current family object with the update. First, check if the region has
		//changed or if the DNS code is added or removed. If so construct a appropriate
		//data changed event.
		if(index < oncFamAL.size())
		{
			//reusing WishChange object to change region counts, served family counts
			//get current DNS and region to determine if they changed after updating data base
			String currONCNum = oncFamAL.get(index).getONCNum();
			String currDNSCode  = oncFamAL.get(index).getDNSCode();
			int currRegion = oncFamAL.get(index).getRegion();
			
			oncFamAL.set(index, updatedFamily);
			
			//check if the ONC number has changed, if so, Sort the family array list 
			//so next/previous is in numerical order
			if(!currONCNum.equals(updatedFamily.getONCNum()))
				sortDB("ONC");
			
			fireDataChanged(source, "UPDATED_FAMILY", updatedFamily);
			
//			System.out.println(String.format("Families- processUpdateFamily: currFam region: %d", currFam.getRegion()));
//			System.out.println(String.format("Families- processUpdateFamily: updatedFamily region: %d", updatedFamily.getRegion()));
			if(currRegion != updatedFamily.getRegion())
			{
				//create DataChange object for regions changing
				DataChange regionChange = new DataChange(currRegion, updatedFamily.getRegion());
				fireDataChanged(this, "UPDATED_REGIONS", regionChange);
			}
			else if(!currDNSCode.equals(updatedFamily.getDNSCode()) ||
					!currONCNum.equals(updatedFamily.getONCNum()))
			{
				//A change to either ONC Number or DNS code or both detected
				DataChange regionChange;
				
				//if both are now valid, add a region and update the counts
				if(isNumeric(updatedFamily.getONCNum()) && updatedFamily.getDNSCode().isEmpty())
				{
					regionChange = new DataChange(-1, updatedFamily.getRegion());
					fireDataChanged(this, "UPDATED_REGIONS", regionChange);
					
					int[] countsChange = getServedFamilyAndChildCount();
					DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
					fireDataChanged(this, "UPDATED_SERVED_COUNTS", servedCountsChange);
				}
				//if both were valid and now one is invalid, remove a region and update the counts
				else if(isNumeric(currONCNum) && currDNSCode.isEmpty() && 
						!(isNumeric(updatedFamily.getONCNum()) && updatedFamily.getDNSCode().isEmpty()))
				{
					regionChange = new DataChange(updatedFamily.getRegion(), -1);
					fireDataChanged(this, "UPDATED_REGIONS", regionChange);
					
					int[] countsChange = getServedFamilyAndChildCount();
					DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
					fireDataChanged(this, "UPDATED_SERVED_COUNTS", servedCountsChange);
				}				
			}
			
			if(!currONCNum.equals(updatedFamily.getONCNum()))
			{
				//Sort the family array list so next/previous is in numerical order
		    	sortDB("ONC");
			}
		}
		
		return updatedFamily;
	}
	
	/************************************************************************************
	 * This method takes a ONCFamily request object and send the add request to the
	 * server.
	 **********************************************************************************/
	ONCObject add(Object source, ONCObject reqAddObj)
	{
		Gson gson = new Gson();
		
		//create the add agent request and send it to the server
		String response = null;
		response = serverIF.sendRequest("POST<add_family>" + gson.toJson(reqAddObj, ONCFamily.class));
		
		//response will determine if agent already existed or a new agent was added
		if(response != null && response.startsWith("ADDED_FAMILY"))
			return processAddedObject(response.substring(12));
		else
			return null;
	}
	
	ONCObject processAddedObject(String json)
	{
		ONCFamily addedFamily = null;
		
		//Store added object in local database
		Gson gson = new Gson();
		addedFamily = gson.fromJson(json, ONCFamily.class);
		
		if(addedFamily != null)
		{
			oncFamAL.add(addedFamily);
			
			if(oncFamAL.size() > 1)	//Sort if we have more than one family
				sortDB("ONC");
			
			fireDataChanged(this, "ADDED_FAMILY", addedFamily);
		}
		
		return addedFamily;
	}
/*	
	void setONCNumberRegionRanges(String[] nextLine)
	{
		if(nextLine != null)
		{
			oncnumRegionRanges = new int[nextLine.length];
			
			for(int i=0; i<nextLine.length; i++)
				oncnumRegionRanges[i] = Integer.parseInt(nextLine[i]);	
		}
	}
*/
	/**************************************************************************************************
	 * Return the delivered by field in the most recent delivery for the family
	 * @param famID
	 * @return
	 */

	String importCSVFile(String source, JFrame parentFrame)
    { 
//    	ONCRegions regions = ONCRegions.getInstance();
//    	Agent a;
    	
    	File odbfile = new ONCFileChooser(parentFrame).getFile("Select " + source + " .csv file to import families from",
				new FileNameExtensionFilter("CSV Files", "csv"), ONC_OPEN_FILE);
    	
    	String filename = "";
    	
    	if( odbfile!= null)
    	{
	    	filename = odbfile.getName();
	    	
	    	//Solicit a batch number for the import from the user by instantiating the modal
	    	//batch number dialog. Dialog returns a batch number or "N/A" if cancelled
	    	BatchNumDialog bnDlg = new BatchNumDialog(parentFrame, generateRecommendedImportBatchNum());
	    	bnDlg.setVisible(true);
	    	String batchNum = bnDlg.getBatchNumberFromDlg();
	    	
	    	//Check to see if user canceled the modal batch number dialog
	    	//If so, cancel the import and return a blank filename
	    	if(batchNum == null)	{ return "";}
	    	
//	    	int oncID = getHighestONCID() + 1;	//Start with the next ONC ID number
	    	
	    	//If user selected OK in batch dialog, then proceed with the import
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(odbfile.getAbsoluteFile()));
	    		String[] nextLine;
	    		String[] header;
	    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			int agentID = -1;
	    			
	    			//Determine which format ODB or WFCM is using
//	    			if(header.length == 29)
//	    			{
//	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
//	    				{
//	    					//determine if agent already exists or not. If agent action fails, set
//	    					//agent ID to -1
//	    					Agent reqAgent = new Agent(-1, nextLine[0], nextLine[1], nextLine[2], nextLine[6], nextLine[9]);
//	    					
//	    					Agent responseAgt = (Agent) oncAgentDB.add(this, reqAgent);
//	    					if(responseAgt != null)
//	    						agentID = responseAgt.getID();
//	    					
//	    					//now that the agent has been separated from the input data, create the
//	    					//family
//	    					ONCFamily reqAddFam = new ONCFamily(nextLine[0], nextLine[1], nextLine[2], nextLine[3],
//	    							nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9],
//	    							nextLine[10], nextLine[11], nextLine[12], nextLine[13], nextLine[14], nextLine[15],
//	    							nextLine[16], nextLine[17], nextLine[18], nextLine[19], nextLine[20], nextLine[21],
//	    							nextLine[22], nextLine[23], nextLine[24], nextLine[25], nextLine[26], nextLine[27],
//	    							nextLine[28], batchNum, fGVs.getTodaysDate(),
//	    							-1,
//	    							"NNA",
//	    							generateONCNumber(regions.getRegionMatch(nextLine[14], nextLine[16]), null),
//	    							oncID,
//	    							-1,
//	    							fGVs.getUserLNFI(), 
//	    							agentID);
//	    					
//	    					ONCFamily addedFam = (ONCFamily) add(this, reqAddFam);
//	    					
//	    					//if family add was successful, add children to child data base
//	    					if(addedFam != null)
//	    						addFamiliesChildren(addedFam.getID(), nextLine[5]);	
//	    				}
//	    				
//	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
//	    			}
	    			
	    			//For 2014 ODB .csv format, take 29 field per record input and use 25 field ONCFamily constructor
	    			//For 2013 ODB .csv format, take 28 field per record input and use 25 field ONCFamily constructor
	    			//The Sponsor Contact Name, Delivery Address Line 3 and Donor Type fields are ignored after being read
	    			if(header.length == 28 || header.length == 29)
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					//determine if agent already exists or not. If agent action fails, set
	    					//agent ID to -1
	    					Agent reqAgent = new Agent(-1, nextLine[0], nextLine[1], nextLine[2], nextLine[7], nextLine[10]);
	    					
	    					Agent responseAgt = (Agent) oncAgentDB.add(this, reqAgent);
	    					if(responseAgt != null)
	    						agentID = responseAgt.getID();
	    					
	    					//now that the agent has been separated from the input data, create the family
	    					ONCFamily reqAddFam = new ONCFamily(nextLine[0], nextLine[1], nextLine[2],
	    							nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9],
	    							nextLine[10], nextLine[11], nextLine[12], nextLine[13], nextLine[14], nextLine[15],
	    							nextLine[16], nextLine[18], nextLine[19], nextLine[20],
	    							nextLine[22], nextLine[23], nextLine[24], nextLine[25], nextLine[26], nextLine[27],
	    							batchNum, fGVs.getTodaysDate(),
	    							-1,
	    							"NNA",
//	    							generateONCNumber(regions.getRegionMatch(nextLine[14], nextLine[16]), null),
//	    							oncID,
	    							-1,
	    							fGVs.getUserLNFI(), 
	    							agentID);
	    					
	    					ONCFamily addedFam = (ONCFamily) add(this, reqAddFam);
	    					
	    					//if family add was successful, add children to child data base
	    					if(addedFam != null)
	    						addFamiliesChildren(addedFam.getID(), nextLine[6]);	

	    				}
	    				
	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
	    				
	    			}
/*	    			
	    			else if(header.length == 26)
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					a=oncAgentDB.add(nextLine[0], nextLine[1], nextLine[2], nextLine[6], nextLine[9]);
	    					
	    					oncFamAL.add(new ONCFamily(nextLine[0], nextLine[1], nextLine[2], nextLine[3],
	    							nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9],
	    							nextLine[10], nextLine[11], nextLine[12], nextLine[13], nextLine[14], nextLine[15],
	    							nextLine[16], nextLine[17], nextLine[18], nextLine[19], nextLine[20], nextLine[21],
	    							nextLine[22], nextLine[23], nextLine[24], nextLine[25], batchNum,
	    							fGVs.getTodaysDate(), regions.getRegionMatch(nextLine[14], nextLine[16]),
	    							generateONCNumber(regions.getRegionMatch(nextLine[14], nextLine[16]), null), oncID,  fGVs.getUserLNFI(),
	    							a.getID()));
	    					
	    					addFamiliesChildren(oncID++, nextLine[5]);	//add children to child data base
	    					
	    				}
	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
	    			}
	    			else if(header.length == 25)	//ODB Batch 4 modified
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{	
	    					a=oncAgentDB.add(nextLine[0], nextLine[1], nextLine[2], nextLine[6], nextLine[9]);
	    					oncFamAL.add(new ONCFamily(nextLine[0], nextLine[1], nextLine[2], nextLine[3],
	    							nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9],
	    							nextLine[10], nextLine[11], nextLine[12], nextLine[13], nextLine[14], nextLine[15],
	    							nextLine[16], nextLine[17], nextLine[18], nextLine[19], nextLine[20], nextLine[21],
	    							nextLine[22], nextLine[23], nextLine[24], batchNum,
	    							fGVs.getTodaysDate(), regions.getRegionMatch("", nextLine[14]),
	    							generateONCNumber(regions.getRegionMatch("", nextLine[14]), null), oncID,  fGVs.getUserLNFI(),					
	    							a.getID()));
	    					
	    					addFamiliesChildren(oncID++, nextLine[5]);	//add children to child data base
	    					
	    				}
	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
	    			}
	    			else if(header.length == 30 & source.equals("ODB"))	//ODB Batch 4 modified
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					a=oncAgentDB.add(nextLine[0], nextLine[1], nextLine[2], nextLine[6], nextLine[9]);
	    					oncFamAL.add(new ONCFamily(nextLine[0], nextLine[1], nextLine[2], nextLine[3],
	    							nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9],
	    							nextLine[10], nextLine[11], nextLine[12], nextLine[13], nextLine[14], nextLine[15],
	    							nextLine[16], nextLine[17], nextLine[18], nextLine[19], nextLine[20], nextLine[21],
	    							nextLine[22], nextLine[23], nextLine[24], nextLine[25], nextLine[26], nextLine[27],
	    							nextLine[28], nextLine[29], batchNum, fGVs.getTodaysDate(), regions.getRegionMatch(nextLine[15], nextLine[17]),
	    							generateONCNumber(regions.getRegionMatch(nextLine[15],nextLine[17]), null), oncID,  fGVs.getUserLNFI(),
	    							a.getID()));
	    					
	    					addFamiliesChildren(oncID++, nextLine[5]);	//add children to child data base
	    				}
	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
	    			}
	    			else if(header.length == 30 & source.equals("WFCM"))	//WFCM format
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{	
	    					a=oncAgentDB.add(nextLine[25], nextLine[27], nextLine[28], nextLine[29], nextLine[26]);
	    					oncFamAL.add(new ONCFamily(nextLine[25], nextLine[27], nextLine[28], nextLine[6],
	    							nextLine[7], nextLine[8], nextLine[29], nextLine[20], nextLine[17],
	    							nextLine[26], nextLine[21], nextLine[22], nextLine[23], nextLine[24],
	    							nextLine[12], nextLine[11], nextLine[13], nextLine[12], "", "", "", nextLine[14],
	    							nextLine[16], nextLine[13], "", nextLine[9], nextLine[10], "",
	    							nextLine[18], nextLine[19], batchNum, fGVs.getTodaysDate(),
	    							regions.getRegionMatch(nextLine[11], nextLine[12]),
	    							generateONCNumber(regions.getRegionMatch(nextLine[11], nextLine[17]), null), oncID,  fGVs.getUserLNFI(),
	    							a.getID()));
	    					
	    					addFamiliesChildren(oncID++, nextLine[8]);	//add children to child data base
	    				}
	    				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
	    			}
*/	    			
	    			else
	    				JOptionPane.showMessageDialog(parentFrame, 
	    						odbfile.getName() + " is not in ODB format, cannot be imported", 
	    						"Invalid ODB Format", JOptionPane.ERROR_MESSAGE, fGVs.getImageIcon(0)); 			    			
	    		}	    		
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	   
	    return filename;
    }

	/*******************************************************************************************
	 * This method searches the family data base for the largest ID number. It returns
	 * the largest id number found. This is used when importing new families into the data base. 
	 ******************************************************************************************/
/*	
	int getHighestONCID()
	{
		int highestID = 0;
	    if(oncFamAL.size() > 0)
	    	for(ONCFamily f:oncFamAL)
	    		if(f.getID() > highestID)
	    			highestID = f.getID();	
	   	
	    return highestID;
	}

	public void readONCNumberRanges(ObjectInputStream ois)
	{	
		try {
				
			oncnumRegionRanges = (int[]) ois.readObject();
			 	
		} 
		catch (IOException e)	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Save the ONC Family data base and region ranges
	public void writeFamilyDBObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(oncFamAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Save the ONC Family data base and region ranges
	public void writeONCNumberRanges(ObjectOutputStream oos)
	{
		try {
			 oos.writeObject(oncnumRegionRanges);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/	
	/***
	 * This method forms an add child request and send it to the server via
	 * the local child data base.
	 */
	void addFamiliesChildren(int famid, String fm)
	{
		String[] members = fm.split("\n");
		
		for(int i=0; i<members.length; i++)
			if(!members[i].contains("Adult") && !members[i].contains("adult"))
			{
				//crate the add child requst object
				ONCChild reqAddChild = new ONCChild(-1, famid, members[i]);
				
				//interact with the server to add the child
				childDB.add(this, reqAddChild);
			}
		
//		//Sort the families children by age and set the child number for each child in the family
//		childDB.assignChildNumbers(famid);	
	}
	
	String searchDB(String s, List<Integer> rAL)
	{
		String searchtype;
		rAL.clear();	//Clear the prior search result array
		
		//Determine the type of search based on characteristics of search string
		if(s.matches("-?\\d+(\\.\\d+)?") && s.length() < 5)
		{
			searchForONCNumber(s, rAL);
			searchtype = "ONC Number";
		}
		else if(s.matches("-?\\d+(\\.\\d+)?") && s.length() < 7)
		{
			searchForODBNumber(s, rAL);
			searchtype = "ODB Number";
		}
		else if(s.matches("-?\\d+(\\.\\d+)?") && s.length() < 13)
		{
			searchForPhoneNumber(s, rAL);
			searchtype = "Phone Number";
		}
		else
		{
			searchForLastName(s, rAL);
			searchtype = "Last Name";
		}
		
		return searchtype;
	}
	
	 int searchForFamilyIndexByID(int id)
	 {
	    	int index = 0;
	    	while(index < oncFamAL.size() && oncFamAL.get(index).getID() != id)
	    		index++;
	    	
	    	if(index==oncFamAL.size())
	    		return -1;	// Family not found
	    	else
	    		return index;   	
	}
	 
	 public ONCFamily searchForFamilyByID(int id)
	 {
	    	int index = 0;
	    	while(index < oncFamAL.size() && oncFamAL.get(index).getID() != id)
	    		index++;
	    	
	    	if(index==oncFamAL.size())
	    		return null;	// Family not found
	    	else
	    		return oncFamAL.get(index);   	
	}
	
	private void searchForONCNumber(String s, List<Integer> rAL)
    {
    	for(ONCFamily f: oncFamAL)
    		if(s.equals(f.getONCNum()))
    			rAL.add(f.getID());		
    }
	
	public int searchForONCNumber(String oncnum)
    {
    	int index = 0;
    	
    	while(index < oncFamAL.size() && !oncnum.equals(oncFamAL.get(index).getONCNum()))
    		index++;
    	
    	return index == oncFamAL.size() ? -1 : index;   		
    }
	
	private void searchForODBNumber(String s, List<Integer> rAL)
    {
    	for(ONCFamily f: oncFamAL)
    		if(s.equals(f.getODBFamilyNum()))
    			rAL.add(f.getID());   	
    }
	
	int searchForODBNumber(String odbnum)
    {
    	int index = 0;
    	
    	while(index < oncFamAL.size() && !odbnum.equals(oncFamAL.get(index).getODBFamilyNum()))
    		index++;
    	
    	return index==oncFamAL.size() ? -1 : index;   		
    }
	
	private void searchForLastName(String s, List<Integer> rAL)
    {	
    	for(ONCFamily f: oncFamAL)
    		if(f.getClientFamily().toLowerCase().contains(s.toLowerCase()))
    			rAL.add(f.getID());    	
    }
	
	private void searchForPhoneNumber(String s, List<Integer> rAL)
    {
    	for(ONCFamily f:oncFamAL)
    	{
    		//Ensure just 10 digits, no dashes in numbers
    		String hp = f.getHomePhone().replaceAll("-", "");
    		String op = f.getOtherPhon().replaceAll("-", "");
    		String target = s.replaceAll("-", "");
    		
    		if(hp.contains(target) || op.contains(target))
    			rAL.add(f.getID());
    	}
    }
	
	/******************************************************************************************************
     * This method counts the number of served families and served children in the family data base
     * It returns a two element integer array, element 0 = # of served families, element 1 = # of 
     * served children
     ******************************************************************************************************/
    int[] getServedFamilyAndChildCount()
    {
    	int nServedFamilies = 0, nServedChildren = 0;
    	for(ONCFamily f:oncFamAL)
    	{
    		if(isNumeric(f.getONCNum()) && f.getDNSCode().isEmpty())
    		{
    			nServedFamilies++;
    			nServedChildren += childDB.getNumberOfChildrenInFamily(f.getID()) ;
    		}
    	}
    	
    	int[] count_results = {nServedFamilies, nServedChildren};
    	
    	return count_results;
    }
    
    /*******************************************************************************************************
     * This method counts the number of families with a particular agent
     **************************************************************************************************/
    int getNuberOfFamiliesWithAgent(int agentID)
    {
    	int agentCount = 0;
    	
    	for(ONCFamily f:oncFamAL)
    		if(f.getAgentID() == agentID)
    			agentCount++;
    	
    	return agentCount;
    }
    
    /******************************************************************************************************
     * This method recommends a batch number prior to import of external data
     ******************************************************************************************************/
    String generateRecommendedImportBatchNum()
    {
    	String recBN = "B-";
    	int highestBatchNum = 0;
    	if(oncFamAL.size() > 0)
    	{
    		for(ONCFamily f:oncFamAL)
    		{
    			String currentBN = f.getBatchNum();
    			String[] bnParts = currentBN.split("-", 2);
    			if(bnParts.length == 2 && isNumeric(bnParts[1]) && Integer.parseInt(bnParts[1]) > highestBatchNum)
    				highestBatchNum = Integer.parseInt(bnParts[1]);
    		}
    	}
    	
    	if(highestBatchNum < 10)
			recBN += "0" + Integer.toString(highestBatchNum+1);
    	else
			recBN += Integer.toString(highestBatchNum+1);
    	
    	return recBN;
    }
    
    /******************************************************************************************************
     * This method recommends a batch number prior to import of external data
     ******************************************************************************************************/
    String generateIntakeODBNumber()
    {
    	int highestONCODBNum = 0;
    	
    	if(oncFamAL.size() > 0)
    	{
    		for(ONCFamily f:oncFamAL)
    		{
    			if(f.getODBFamilyNum().startsWith("ONC"))
    			{
    				int num = Integer.parseInt(f.getODBFamilyNum().substring(3));
    				if(num > highestONCODBNum)
    					highestONCODBNum = num;
    			}
    		}
    	}
    	
    	//now that we have the number, format it to a three digit string
    	highestONCODBNum++;
    	
    	if(highestONCODBNum < 10)
    		return "ONC00" + Integer.toString(highestONCODBNum);
    	else if(highestONCODBNum >= 10 && highestONCODBNum < 100)
    		return "ONC0" + Integer.toString(highestONCODBNum);
    	else
    		return "ONC" + Integer.toString(highestONCODBNum);
    }
    
    /******************************************************************************************
     * This method automatically generates an ONC Number for family that does not have an ONC
     * number already assigned. The method uses the integer region number passed (after checking
     * to see if it is in the valid range) and indexes into the oncnumRegionRanges array to get
     * the starting ONC number for the region. It then queries the family array to see if that
     * number is already in use. If it's in use, it goes to the next number to check again. It 
     * continues until it finds an unused number or reaches the end of the range for that region.
     * The first unused ONC number in the range is returned. If the end of the range is reached 
     * and all numbers have been assigned, it will display an error dialog and after the user
     * acknowledges the error, it will return string "OOR" for out of range.
     * If the region isn't valid, it will complain and then return the string "RNV" for region
     * not valid 
     * @param region
     * @return
     ********************************************************************************************/
/*  String generateONCNumber(int region, JFrame parentFrame)
    {
    	String oncNum = null;
    	//Verify region number is valid. If it's not return an error
    	ONCRegions r = ONCRegions.getInstance();
    	 
    	if(region == 0)		//Don't assign numbers without known valid region addresses
    		oncNum = "NNA";
    	else if(r.isRegionValid(region))
    	{
    		int start = oncnumRegionRanges[region];
    		int	end = oncnumRegionRanges[(region+1)%r.getNumberOfRegions()];
    		
    		String searchstring = Integer.toString(start);
    		while(start < end && searchForONCNumber(searchstring) != -1)
    			searchstring = Integer.toString(++start);
    		
    		if(start==end)
    		{
    			oncNum = "OOR";		//Not enough size in range
    			JOptionPane.showMessageDialog(parentFrame, "ERROR: Too many families in region " + r.getRegionID(region) + 
    					", can't automatically assign an ONC Nubmer ", 
    					"ONC Number Can't Be Assigned", JOptionPane.ERROR_MESSAGE);
    		}
    		else
    			oncNum = Integer.toString(start);
    		
    	}
    	else
    	{
    		JOptionPane.showMessageDialog(parentFrame, "ERROR: ONC Region Invalid: Region is not in " +
					"the vaild range", "ONC Region Invaild", JOptionPane.ERROR_MESSAGE);
    		oncNum = "RNV";
    	}	
    	
    	return oncNum;
    }
    
    void initializeONCNumberRegionRanges()
    {
    	//Construct the database used to automatically assign ONC numbers based on last years
	    //regional family distribution. Assume 20% growth in each region.
	    ONCRegions r = ONCRegions.getInstance();

	    oncnumRegionRanges = new int[r.getNumberOfRegions()];
	    
	    for(int i=0; i<oncnumRegionRanges.length; i++)
	    	oncnumRegionRanges[i] = 0;
    }
 */   
    /*******************************************************************************************
     * This method takes an ONCFamily array list and determines ranges to use for
     * automated assignment of ONC family numbers. It first counts the number of families
     * in each region last year. Based on that count and both a starting ONC number and a growth
     * allocation percentage, it creates an array that contains the starting ONC number for each
     * region. This method will only be run one at the beginning of a season or when ONC
     * numbers must be reassigned as requested by a user. 
     * 
     * For the method to work properly, the number of regions must be consistent from year
     * to year. If changes are made to regions or new regions are added, last years 
     * family data should be updated with the new regions prior to executing this method.
     * The method returns an array of integers holding the starting values for each region. 
     ******************************************************************************************/
 /*   int[] constructONCNumberRangesByRegion(ArrayList<ONCFamily> pyFamilyAL)
    {
    	//Construct the database used to automatically assign ONC numbers based on last years
	    //regional family distribution. Assume 20% growth in each region.
	    int[] pyRegionDist = new int[oncnumRegionRanges.length];
	       
	    for(int i=0; i<pyRegionDist.length; i++)
	    {
	    	pyRegionDist[i] = 0;	//Initialize prior year region counts to zero
	    	oncnumRegionRanges[i] = 0;	//Initialize region range variables
	    }
	    
	    for(ONCFamily pyf:pyFamilyAL)
	    	pyRegionDist[pyf.getRegion()]++;	//Set prior year region counts
	    
	    oncnumRegionRanges[1] = fGVs.getStartONCNum();	//Region 0 is for unknown addresses resolutions, region 1 starts at 100
	    for(int reg=1; reg < oncnumRegionRanges.length; reg++)
	    {	
	    	int nextreg = (reg+1) % oncnumRegionRanges.length;
	    	int regionsize = (pyRegionDist[reg]*(100 + fGVs.getYTYGrwthPct()))/100;	//region size is bigger than last year
	    	if(regionsize < ONC_MIN_REGION_RANGE_SIZE)	//If regions size growth is less then minimum, set it to a minimum
	    		regionsize = ONC_MIN_REGION_RANGE_SIZE;
	    	oncnumRegionRanges[nextreg] = oncnumRegionRanges[reg] + regionsize;	//Unknown's start at the end of last region
	    }
	    
	    for(int i=0; i< oncnumRegionRanges.length; i++)
	    {
	    	System.out.println("Priory Region " + Integer.toString(i) +" size: " + pyRegionDist[i]);
	    	System.out.println("New Region " + Integer.toString(i) +" index: " + oncnumRegionRanges[i]);
	    }
	    
	    return oncnumRegionRanges;
    }
 */   
    /******************************************************************************************
     * This method resizes the ONC Number ranges. It is requested
     * by the user if/ when regions become full and require enlargement for all families in 
     * a region to have contiguous ONC numbers.
     * 
     * The method creates new ONC Number ranges for each region.
     ***************************************************************************************/
    void resizeONCNumberRanges()
    {
		//Reconstruct the array used to automatically assign ONC numbers based the 
		//current years regional family distribution.			
		int[] neededRegionSize = new int[oncnumRegionRanges.length];	//Current # of families in each region
		int[] currentRegionSize = new int[oncnumRegionRanges.length];	//Current size of each region
			
		for(int i=0; i< neededRegionSize.length; i++)	//Initialize the needed sizes
			neededRegionSize[i] = 0;
			
		for(int i=1; i< neededRegionSize.length; i++)	//Initialize the region sizes
			currentRegionSize[i] = oncnumRegionRanges[(i+1) % oncnumRegionRanges.length] - oncnumRegionRanges[i];	
				
		for(ONCFamily f: oncFamAL)	//Count the number of current families in each region
			neededRegionSize[f.getRegion()]++;	
			
		//For each region where the current number of families exceeds the currrent region size, 
		//resize the region to fit and add some additional margin. The additional margin
		//is stored in a class constant. If the region size is currently adequate, leave it alone. 
		for(int i=1; i<oncnumRegionRanges.length; i++)
		{
			int nxtReg = (i+1) % oncnumRegionRanges.length;
				
			if(neededRegionSize[i] > currentRegionSize[i])
				oncnumRegionRanges[nxtReg] = oncnumRegionRanges[i] + neededRegionSize[i] +  ONC_REBASELINE_REGION_MARGIN;
			else
				oncnumRegionRanges[nxtReg] = oncnumRegionRanges[i] + currentRegionSize[i];
		}
	}
  
	//getter for Yellow Card data
	String[] getYellowCardData(ONCFamily f) 
	{
		String[] ycData = new String[15];
		ycData[0] = f.getONCNum();
		ycData[1] = f.getHOHFirstName() + " " + f.getHOHLastName();
		ycData[2] =	f.getHouseNum() + " " + f.getStreet() + " " + f.getUnitNum() ;
		ycData[3] = f.getCity() + ", VA " + f.getZipCode();
		ycData[4] = "?";
			
		//Format the first two phone numbers in Home and Other phone strings
		String[] fmtPh = formatPhoneNumbers(f.getHomePhone());
		ycData[5] = fmtPh[0];
		ycData[6] = fmtPh[1];
			
		fmtPh = formatPhoneNumbers(f.getOtherPhon());
		ycData[7] = fmtPh[0];
		ycData[8] = fmtPh[1];
		
		ycData[9] = f.getLanguage();
		ycData[10] = f.getDeliveryInstructions();
		ycData[11] = Integer.toString(f.getNumOfBags());
		ycData[12] = Integer.toString(getNumberOfBikesSelectedForFamily(f));
		ycData[13] = Integer.toString(f.getNumOfLargeItems());
		ycData[14] = Integer.toString(f.getFamilyStatus());
			
		return ycData;
	}
	
	/*
	 * This method takes a string of phone numbers, separated by a \n and
	 * returns the first two formated with parenthesis and a dash. The returned
	 * array of strings will have one or two formated numbers depending on how
	 * many phone numbers were present in the input parameter string. If no numbers
	 * are passed, an array with "None Provided, "" will be returned.
	 */
	String[] formatPhoneNumbers(String phonestring)
	{	
		String[] output = {"None Provided",""};
		
		String[] phnums = phonestring.split("\n");
		int count = (phnums.length > 2) ? 2 : phnums.length;
		for(int i=0; i< count; i++)
			if(!phnums[i].contains("None Found"))
			{
				StringBuffer phNum = new StringBuffer("");
				phnums[i] = phnums[i].trim();
				phNum.append("(" + phnums[i].substring(0,3) + ") ");
				phNum.append(phnums[i].substring(3,6) +"-");
				phNum.append(phnums[i].substring(6,10));
				output[i] = phNum.toString().trim();
			}
		
		return output;
	}
	
	int getNumberOfBikesSelectedForFamily(ONCFamily f)
	{
		int nBikes = 0;
		
		for(ONCChild c: childDB.getChildren(f.getID()))	
			for(int wn=0; wn<NUMBER_OF_WISHES_PER_CHILD; wn++)
			{
				int wishID = c.getChildWishID(wn);
				if(wishID > -1 && childwishDB.getWish(wishID).getChildWishBase().equals("Bike"))
					nBikes++;
			}
			
		return nBikes;
	}
	
	ArrayList<int[]> getWishBaseSelectedCounts(ArrayList<String> wishnames)
	{
		ArrayList<int[]> wishcountAL = new ArrayList<int[]>();
	
		//Initialize the array list
		for(int i=0; i<wishnames.size(); i++)
		{
			int[] counts = new int[NUMBER_OF_WISHES_PER_CHILD];
			counts[0] = 0;
			counts[1] = 0;
			counts[2] = 0;
			
			wishcountAL.add(counts);
		}
		
		//For each valid child, get their wishes. For each wish type, increment the count for the
		//wish number
		for(ONCFamily f:oncFamAL)
		{
			if(isNumeric(f.getONCNum()))	//Must be a valid family
			{
				for(ONCChild c: childDB.getChildren(f.getID()))
				{
					for(int wn=0; wn < NUMBER_OF_WISHES_PER_CHILD; wn++)	//get each of thier wishes
					{
						ONCChildWish cw = childwishDB.getWish(c.getChildWishID(wn));
						
						//cw is null if child doesn't have this wish yet
						if(cw != null && cw.getChildWishStatus() > CHILD_WISH_STATUS_EMPTY)
						{
							//what wish are we looking for to count
							String wishname = cw.getChildWishBase();
							
							//Find wish in array list and increment the count
							int index = 0;
							while(index < wishcountAL.size() && !wishname.equals(wishnames.get(index)))
								index++;
						
							if(index == wishcountAL.size())
								System.out.println(String.format("Error: Creating Wish Catalog counts: " +
									"child wish not found, family %s, wish %s", f.getONCNum(), wishname));
							else
								wishcountAL.get(index)[wn]++;
						}
					}
				}			
			}
		}
		
		return wishcountAL;
	}
 
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	void correctPhoneNumbers()
	{
		for(ONCFamily currFamily:oncFamAL)
		{
			//make a copy of the family object
			ONCFamily reqFamUpdate = new ONCFamily(currFamily);
			
			//parse family phone data provided by odb
			reqFamUpdate.parsePhoneData(reqFamUpdate.getAllPhoneNumbers());
			
			System.out.println(String.format("FamilyDB_correctPhoneNumbers: ONC #%s Home Phone: %s, Other Phone: %s", 
					reqFamUpdate.getONCNum(), reqFamUpdate.getHomePhone(), reqFamUpdate.getOtherPhon()));
			
			//Update the family on the server
//			update(this, reqFamUpdate);
		}
	}
	
	//Overloaded sortDB methods allow user to specify a data base to be sorted
	//or use the current data base
	boolean sortDB(ArrayList<ONCFamily> fal, String dbField) { return sortFamilyDataBase(fal, dbField); }
	boolean sortDB(String dbField) { return sortFamilyDataBase(oncFamAL, dbField); }

	private boolean sortFamilyDataBase(ArrayList<ONCFamily> fal, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("ONC")) {
			Collections.sort(fal, new ONCFamilyONCNumComparator()); }
		else if(dbField.equals("Batch #")) {
			Collections.sort(fal, new ONCFamilyBatchNumComparator()); }
		else if(dbField.equals("DNS")) {
			Collections.sort(fal, new ONCFamilyDNSComparator()); }
		else if(dbField.equals("Fam Status")) {
			Collections.sort(fal, new ONCFamilyStatusComparator()); }
		else if(dbField.equals("Del Status")) {
			Collections.sort(fal, new ONCFamilyDelStatusComparator()); }
		else if(dbField.equals("First")) {
			Collections.sort(fal, new ONCFamilyFNComparator()); }
		else if(dbField.equals("Last")) {
			Collections.sort(fal, new ONCFamilyLNComparator()); }
		else if(dbField.equals("Street")) {
			Collections.sort(fal, new ONCFamilyStreetComparator()); }
		else if(dbField.equals("Zip")) {
			Collections.sort(fal, new ONCFamilyZipComparator()); }
		else if(dbField.equals("Reg")) {
			Collections.sort(fal, new ONCFamilyRegionComparator()); }
		else if(dbField.equals("Changed By")) {
			Collections.sort(fal, new ONCFamilyCallerComparator()); }
		else if(dbField.equals("SL")) {
			Collections.sort(fal, new ONCFamilyStoplightComparator()); }
		else if(dbField.equals("# Bikes")) {
			Collections.sort(fal, new ONCFamilyBikesComparator()); }
		else if(dbField.equals("Deliverer")) {
			Collections.sort(fal, new ONCFamilyDelivererComparator()); }
		else
			bSortOccurred = false;
		
		return bSortOccurred;
		
	}
	String importDB()
	{
		String response = "NO_FAMILIES";
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			Type listOfFamilies = new TypeToken<ArrayList<ONCFamily>>(){}.getType();
			
			response = serverIF.sendRequest("GET<familys>");
			oncFamAL = gson.fromJson(response, listOfFamilies);				
			
			if(!response.startsWith("NO_FAMILIES"))
			{
				Collections.sort(oncFamAL, new ONCFamilyONCNumComparator());
				response = "FAMILIES_LOADED";
			}
		}
		
		return response;
	}
	
	String importFamilyDB(JFrame pf, ImageIcon oncIcon)	//Only used by superuser to import from .csv file
	{
		ONCFamilyReportRowBuilder rb = new ONCFamilyReportRowBuilder(childDB, childwishDB, oncAgentDB);
    		
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select Family DB .csv file to import");	
 	    chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
	    
	    String filename = "";
	    File pyfile = null;
	    int returnVal = chooser.showOpenDialog(pf);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	pyfile = chooser.getSelectedFile();
	    	filename = pyfile.getName();
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == rb.getFamilyObjectRowLength())
	    			{
	    				oncFamAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					oncFamAL.add(new ONCFamily(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Family DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Family DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Family DB file: " + filename, 
	    					"Invalid Family DB File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Family DB file: " + filename, 
    				"Family DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return pyfile.getParent();    
	}
	
	String exportDBToCSV(JFrame pf)
    {
		ONCFamilyReportRowBuilder rb = new ONCFamilyReportRowBuilder(childDB, childwishDB, oncAgentDB);
    	
    	ONCFileChooser fc = new ONCFileChooser(pf);
    	File oncwritefile= fc.getFile("Select .csv file to save Family DB to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(rb.getFamilyObjectExportHeader());
	    	    
	    	    for(ONCFamily f:oncFamAL)
	    	    	writer.writeNext(rb.getFamilyExportOjectCSVRowData(f));	//Get family object row
	    	 
	    	    writer.close();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return oncwritefile.getParent();
    }
	
	@Override
	public void dataChanged(ServerEvent ue) 
	{
		if(ue.getType().equals("UPDATED_FAMILY"))
		{
			processUpdatedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_FAMILY"))
		{
			processAddedObject(ue.getJson());
		}			
	}
	
/*	 
	 private class ONCDeliveryItemComparator implements Comparator<ONCDelivery>
	 {
		 @Override
		 public int compare(ONCDelivery d1, ONCDelivery d2)
		 {
			return d1.getTimeStamp().compareTo(d2.getTimeStamp());
		 }
	 }
*/	 
	 private class ONCFamilyONCNumComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer onc1, onc2;
				
			if(!o1.getONCNum().isEmpty() && isNumeric(o1.getONCNum()))
				onc1 = Integer.parseInt(o1.getONCNum());
			else
				onc1 = MAXIMUM_ONC_NUMBER;
								
			if(!o2.getONCNum().isEmpty() && isNumeric(o2.getONCNum()))
				onc2 = Integer.parseInt(o2.getONCNum());
			else
				onc2 = MAXIMUM_ONC_NUMBER;
				
			return onc1.compareTo(onc2);
		}
	}
		
	private class ONCFamilyDNSComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{			
			return o1.getDNSCode().compareTo(o2.getDNSCode());
		}
	}
		
	private class ONCFamilyBatchNumComparator implements Comparator<ONCFamily>
	{
		public int compare(ONCFamily o1, ONCFamily o2)
		{
		
			return o1.getBatchNum().compareTo(o2.getBatchNum());
		}
	}
	
	private class ONCFamilyStatusComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer o1FS = (Integer) o1.getFamilyStatus();
			Integer o2FS = (Integer) o2.getFamilyStatus();
			return o1FS.compareTo(o2FS);
		}
	}
	
	private class ONCFamilyDelStatusComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer o1DS = (Integer) o1.getDeliveryStatus();
			Integer o2DS = (Integer) o2.getDeliveryStatus();
			return o1DS.compareTo(o2DS);
		}
	}
		
	private class ONCFamilyFNComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return o1.getHOHFirstName().compareTo(o2.getHOHFirstName());
		}
	}
		
	private class ONCFamilyLNComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)			{
			return o1.getHOHLastName().compareTo(o2.getHOHLastName());
		}
	}
		
	private class ONCFamilyStreetComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return o1.getStreet().compareTo(o2.getStreet());
		}
	}
		
	private class ONCFamilyZipComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return o1.getZipCode().compareTo(o2.getZipCode());
		}
	}
		
	private class ONCFamilyRegionComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer o1Reg = (Integer) o1.getRegion();
			Integer o2Reg = (Integer) o2.getRegion();
			return o1Reg.compareTo(o2Reg);
		}
	}
		
	private class ONCFamilyCallerComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return o1.getChangedBy().compareTo(o2.getChangedBy());
		}
	}
		
	private class ONCFamilyStoplightComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer o1SL = (Integer) o1.getStoplightPos();
			Integer o2SL = (Integer) o2.getStoplightPos();
			return o1SL.compareTo(o2SL);
		}
	}
	
	private class ONCFamilyBikesComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			Integer nb1 = getNumberOfBikesSelectedForFamily(o1);
			Integer nb2 = getNumberOfBikesSelectedForFamily(o1);
			return nb1.compareTo(nb2);
		}
	}
	
	private class ONCFamilyDelivererComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return driverDB.getDriverLNFI(deliveryDB.getDeliveredBy(o1.getDeliveryID())).compareTo
					(driverDB.getDriverLNFI(deliveryDB.getDeliveredBy(o2.getDeliveryID())));
		}
	}
}
