package ourneighborschild;

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

public class Families extends ONCSearchableDatabase
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
	private static final String DB_TYPE = "FAMILY";
	private static final int ONC_OPEN_FILE = 0;
	private static final int ONC_REBASELINE_REGION_MARGIN = 5;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final String ODB_FAMILY_MEMBER_COLUMN_SEPARATOR = " - ";
	
	private static Families instance = null;
	private ArrayList<ONCFamily> oncFamAL;	//The list of families
	private int[] oncnumRegionRanges;		//Holds starting ONC number for each region
	private ChildDB childDB;
	private AdultDB adultDB;
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
		adultDB = AdultDB.getInstance();
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
	String getDBType() { return DB_TYPE; }
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
			return processAddedObject(source, response.substring(12));
		else
			return null;
	}
	
	ONCObject processAddedObject(Object source, String json)
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
			
			fireDataChanged(source, "ADDED_FAMILY", addedFamily);
			
			if(isNumeric(addedFamily.getONCNum()) && addedFamily.getDNSCode().isEmpty())
			{
				DataChange regionChange = new DataChange(-1, addedFamily.getRegion());
				fireDataChanged(this, "UPDATED_REGIONS", regionChange);
				
				int[] countsChange = getServedFamilyAndChildCount();
				DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
				fireDataChanged(this, "UPDATED_SERVED_COUNTS", servedCountsChange);
			}
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
	    			if(header.length == 29)
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
	    							nextLine[28], batchNum, fGVs.getTodaysDate(), -1, "NNA",-1, GlobalVariables.getUserLNFI(), 
	    							agentID);
	    					
	    					ONCFamily addedFam = (ONCFamily) add(this, reqAddFam);
	    					
	    					//if family add was successful, add children to child data base
	    					if(addedFam != null)
	    						addFamiliesChildrenAndAdults(addedFam.getID(), nextLine[6]);
/*	    					
	    					//need to tell the server here to check if family is a duplicate once
	    					//adults and children are added
	    					Gson gson = new Gson();
	    					String response = null;
	    					response = serverIF.sendRequest("POST<check_duplicatefamily>" + gson.toJson(addedFam, ONCFamily.class));
	    					
	    					//response will determine if agent already existed or a new agent was added
	    					if(response == null || response.startsWith("DUPLICATE_FAMILY") || response.startsWith("UNIQUE_FAMILY"))
	    					{
	    						
	    					}
*/	    					
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
	    		
	    		reader.close();
	    		
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
	 * Adults associated with a family are stored in the Adult data base. Children associated
	 * with a family are stored in the Child database. This method forms add adult and add 
	 * child requests and send them to the server via the local adult or child data base.
	 * Adults and children are located by family ID.
	 */
	void addFamiliesChildrenAndAdults(int famid, String fm)
	{
		String[] members = fm.split("\n");
		
		for(int i=0; i<members.length; i++)
		{
			if(members[i].toLowerCase().contains("adult"))
			{
				//crate the add adult request object
				String[] adult = members[i].split(ODB_FAMILY_MEMBER_COLUMN_SEPARATOR, 3);
				if(adult.length == 3)
				{
					ONCAdult reqAddAdult = new ONCAdult(-1, famid, adult[0], adult[1]);
				
					//interact with the server to add the adult
					adultDB.add(this, reqAddAdult);
				}
			}
			else
			{
				//crate the add child request object
				ONCChild reqAddChild = new ONCChild(-1, famid, members[i],
													GlobalVariables.getCurrentSeason());
				
				//interact with the server to add the child
				childDB.add(this, reqAddChild);
			}
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
		else if((s.startsWith("ONC") || s.matches("-?\\d+(\\.\\d+)?")) && s.length() < 7)
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
	
	/***
	 * Search for families that contain string and also children that contain the string.
	 * @param s
	 * @param rAL
	 */
	private void searchForLastName(String s, List<Integer> rAL)
    {	
		//search the family db
    	for(ONCFamily f: oncFamAL)
    		if(f.getClientFamily().toLowerCase().contains(s.toLowerCase()))
    			rAL.add(f.getID());
    	
    	//search the child db
    	childDB.searchForLastName(s, rAL);
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
    
    /*******************************************************************************************************
     * This method determines if a particular agent has referred a family
     **************************************************************************************************/
    boolean didAgentRefer(int agentID)
    {
    	int index = 0;
    	while(index < oncFamAL.size() && oncFamAL.get(index).getAgentID() != agentID)
    		index++;
    	
    	return index < oncFamAL.size();
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
		
		//determine whether using alternate delivery address
		if(f.getSubstituteDeliveryAddress()!= null && !f.getSubstituteDeliveryAddress().isEmpty() &&
				f.getSubstituteDeliveryAddress().split("_").length == 5)
		{
			String[] addPart = f.getSubstituteDeliveryAddress().split("_");
//			String unit = addPart[2].equals("None") ? "" : addPart[2];
			String unit = "";
			if(!addPart[2].equals("None"))
			{
				if(isNumeric(addPart[2]))
					unit = "#" + addPart[2];
				else
					unit = addPart[2];
			}
			ycData[2] = addPart[0] + " " + addPart[1] + " " + unit;
			ycData[3] = addPart[3] + ", VA " + addPart[4];
		}
		else	//no alternate delivery address
		{
			String unit = isNumeric(f.getUnitNum()) ? "#" + f.getUnitNum() : f.getUnitNum();
			ycData[2] =	f.getHouseNum() + " " + f.getStreet() + " " + unit ;
			ycData[3] = f.getCity() + ", VA " + f.getZipCode();
		}
		
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
		String[] output = {"Not Provided",""};
		
		String[] phnums = phonestring.split("\n");
		int count = (phnums.length > 2) ? 2 : phnums.length;
		for(int i=0; i< count; i++)
			if(!phnums[i].contains("None Found"))
			{
				if(phnums[i].length() == 12)
					output[i] = phnums[i];
				else if(isNumeric(phnums[i]) && phnums[i].length() == 10)
				{
					StringBuffer phNum = new StringBuffer("");
					phNum.append(phnums[i].substring(0,3) + "-");
					phNum.append(phnums[i].substring(3,6) +"-");
					phNum.append(phnums[i].substring(6,10));
					output[i] = phNum.toString();
				}
				else
					output[i] = "";
			}
		
		return output;
	}
	
	int getNumberOfBikesSelectedForFamily(ONCFamily f)
	{
		int nBikes = 0;
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		for(ONCChild c: childDB.getChildren(f.getID()))	
			for(int wn=0; wn<NUMBER_OF_WISHES_PER_CHILD; wn++)
			{
				int childwishID = c.getChildWishID(wn);
				if(childwishID > -1 && childwishDB.getWish(childwishID).getWishID() == cat.getWishID("Bike"))
					nBikes++;
			}
			
		return nBikes;
	}
	
	ArrayList<int[]> getWishBaseSelectedCounts(ArrayList<ONCWish> wishList)
	{
		ArrayList<int[]> wishcountAL = new ArrayList<int[]>();
	
		//Initialize the array list
		for(int i=0; i<wishList.size(); i++)
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
			if(isNumeric(f.getONCNum()))	//Must be a served family
			{
				for(ONCChild c: childDB.getChildren(f.getID()))
				{
					for(int wn=0; wn < NUMBER_OF_WISHES_PER_CHILD; wn++)	//get each of their wishes
					{
						ONCChildWish cw = childwishDB.getWish(c.getChildWishID(wn));
						
						//cw is null if child doesn't have this wish yet
						if(cw != null && cw.getChildWishStatus().compareTo(WishStatus.Selected) >= 0)
						{
							//Find wish in array list and increment the count
							int index = 0;
							while(index < wishcountAL.size() && cw.getWishID() != wishList.get(index).getID())
								index++;
						
							if(index == wishcountAL.size())
								System.out.println(String.format("Error: Creating Wish Catalog counts: " +
									"child wish not found, family %s, wish %d", f.getONCNum(), cw.getWishID()));
							else
								wishcountAL.get(index)[wn]++;
						}
					}
				}			
			}
		}
		
		return wishcountAL;
	}
/* 
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
*/	
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
		else if(dbField.equals("Ref #")) {
			Collections.sort(fal, new ONCFamilyReferenceNumComparator()); }
		else if(dbField.equals("DNS")) {
			Collections.sort(fal, new ONCFamilyDNSComparator()); }
		else if(dbField.equals("Fam Status")) {
			Collections.sort(fal, new ONCFamilyStatusComparator()); }
		else if(dbField.equals("Del Status")) {
			Collections.sort(fal, new ONCFamilyDelStatusComparator()); }
		else if(dbField.equals("Meal Status")) {
			Collections.sort(fal, new ONCFamilyMealStatusComparator()); }
		else if(dbField.equals("First")) {
			Collections.sort(fal, new ONCFamilyFNComparator()); }
		else if(dbField.equals("Last")) {
			Collections.sort(fal, new ONCFamilyLNComparator()); }
		else if(dbField.equals("House")) {
			Collections.sort(fal, new ONCFamilyHouseNumComparator()); }
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
		ONCFamilyReportRowBuilder rb = new ONCFamilyReportRowBuilder();
    		
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
	    		
	    		reader.close();
	    		
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Family DB file: " + filename, 
    				"Family DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return pyfile.getParent();    
	}
	
	String exportDBToCSV(JFrame pf, String filename)
    {
//    	ONCFileChooser fc = new ONCFileChooser(pf);
//    	File oncwritefile= fc.getFile("Select .csv file to save Family DB to",
//										new FileNameExtensionFilter("CSV Files", "csv"), 1);
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Family DB to",
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
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    		ONCFamilyReportRowBuilder rb = new ONCFamilyReportRowBuilder();
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
    	
	    return oncwritefile != null ? oncwritefile.getParent() : null;
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
			processAddedObject(this, ue.getJson());
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
/*		 
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
*/
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			if(isNumeric(o1.getONCNum()) && isNumeric(o2.getONCNum()))
			{
				Integer onc1 = Integer.parseInt(o1.getONCNum());
				Integer onc2 = Integer.parseInt(o2.getONCNum());
				return onc1.compareTo(onc2);
			}
			else if(isNumeric(o1.getONCNum()) && !isNumeric(o2.getONCNum()))
				return -1;
			else if(!isNumeric(o1.getONCNum()) && isNumeric(o2.getONCNum()))
				return 1;
			else
				return o1.getONCNum().compareTo(o2.getONCNum());
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
	
	private class ONCFamilyReferenceNumComparator implements Comparator<ONCFamily>
	{
		public int compare(ONCFamily o1, ONCFamily o2)
		{
		
			return o1.getODBFamilyNum().compareTo(o2.getODBFamilyNum());
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
	
	private class ONCFamilyMealStatusComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			
			return o1.getMealStatus().compareTo(o2.getMealStatus());
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
	
	private class ONCFamilyHouseNumComparator implements Comparator<ONCFamily>
	{
		@Override
		public int compare(ONCFamily o1, ONCFamily o2)
		{
			return o1.getHouseNum().compareTo(o2.getHouseNum());
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
			return driverDB.getDriverLNFN(deliveryDB.getDeliveredBy(o1.getDeliveryID())).compareTo
					(driverDB.getDriverLNFN(deliveryDB.getDeliveredBy(o2.getDeliveryID())));
		}
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		String searchtype;
		searchAL.clear();
		
		//Determine the type of search based on characteristics of search string
		if(data.matches("-?\\d+(\\.\\d+)?") && data.length() < 5)
		{
			searchForONCNumber(data, searchAL);
			searchtype = "ONC Number";
		}
		else if(data.matches("-?\\d+(\\.\\d+)?") && data.length() < 7)
		{
			//ODB Format
			searchForODBNumber(data, searchAL);
			searchtype = "ODB Number";
		}
		else if((data.startsWith("C") && data.substring(1).matches("-?\\d+(\\.\\d+)?")) && data.length() < 7)
		{
			//ONCFormat
			searchForODBNumber(data, searchAL);
			searchtype = "ODB Number";
		}
		else if((data.startsWith("W") && data.substring(1).matches("-?\\d+(\\.\\d+)?")) && data.length() < 6)
		{
			//WFCM Format
			searchForODBNumber(data, searchAL);
			searchtype = "ODB Number";
		}
		else if(data.matches("-?\\d+(\\.\\d+)?") && data.length() < 13)
		{
			searchForPhoneNumber(data, searchAL);
			searchtype = "Phone Number";
		}
		else
		{
			searchForLastName(data, searchAL);
			searchtype = "Last Name";
		}
		
		return searchtype;
	}
}
