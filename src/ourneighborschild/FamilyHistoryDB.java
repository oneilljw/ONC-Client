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

public class FamilyHistoryDB extends ONCDatabase
{
	private static FamilyHistoryDB instance = null;
	private List<ONCFamilyHistory> fhList;
	
	private FamilyHistoryDB()
	{
		super();
		fhList = new ArrayList<ONCFamilyHistory>();
	}
	
	public static FamilyHistoryDB getInstance()
	{
		if(instance == null)
			instance = new FamilyHistoryDB();
		
		return instance;
	}
	
	List<ONCFamilyHistory> getList() { return fhList; }
	
	int size() { return fhList.size(); }

	ONCFamilyHistory add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		ONCFamilyHistory addedHistObj = null;
		
		response = serverIF.sendRequest("POST<add_delivery>" + 
											gson.toJson(entity, ONCFamilyHistory.class));
		if(response.startsWith("ADDED_DELIVERY"))
			addedHistObj = processAddedObject(source, response.substring(14));
		
		return addedHistObj;	
	}
	
	ONCFamilyHistory processAddedObject(Object source, String json)
	{
		//Store added ONCFamilyHistory object in local data base
		Gson gson = new Gson();
		ONCFamilyHistory addedObject = gson.fromJson(json, ONCFamilyHistory.class);
		
		fhList.add(addedObject);
/*	
		//update the family status this is a delivery is associated with
		FamilyDB familyDB = FamilyDB.getInstance();
		ONCFamily fam = familyDB.getFamily(addedObject.getFamID());
		fam.setFamilyStatus(addedObject.getFamilyStatus());
		fam.setGiftStatus(addedObject.getGiftStatus());
		fam.setDeliveryID(addedObject.getID());
*/		
		//Notify local user IFs that an organization/partner was added. This will
		//be all UI's that display or manage family delivery information
		fireDataChanged(source, "ADDED_DELIVERY", addedObject);
		
		return addedObject;
	}
	
	ONCFamilyHistory processDeletedFamilyHistory(Object source, String json)
	{
		//remove the family history item from this local data base
		Gson gson = new Gson();
		ONCFamilyHistory deletedFH = removeDeletedHistory(source, gson.fromJson(json, ONCFamilyHistory.class).getID());
		
		if(deletedFH != null)
			fireDataChanged(source, "DELETED_FAMILY_HISTORY", deletedFH);
		
		return deletedFH;
	}
	
	ONCFamilyHistory removeDeletedHistory(Object source, int famHistID)
	{
		//remove the meal from this data base
		ONCFamilyHistory deletedFH = null;
		
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != famHistID)
				index++;
				
		if(index < fhList.size())
		{
			deletedFH = fhList.get(index);
			fhList.remove(index);
		}
		
		return deletedFH;
	}
	
	ONCFamilyHistory getFamilyHistory(int id)
	{
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != id)
			index++;
		
		if(index == fhList.size())
			return null;
		else
			return fhList.get(index);
	}
	
	ArrayList<ONCFamilyHistory> getDeliveryHistoryAL(int famID)
	{
		ArrayList<ONCFamilyHistory> famDelAL = new ArrayList<ONCFamilyHistory>();
		for(ONCFamilyHistory d:fhList)
			if(d.getFamID() == famID)
				famDelAL.add(d);
		return famDelAL;
	}
	
	/**************************************************************************************************
	 * Return the delivered by field in the most recent delivery for the family
	 * @param famID
	 * @return
	 */
	String getDeliveredBy(int delID)
	{
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != delID)
			index++;
				
		if(index==fhList.size())
			return "";
		else
			return fhList.get(index).getdDelBy();
	}
	
	String importFamilyHistoryDatabase()
	{
		String response = "NO_FAMILY_HISTORY";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCFamilyHistory>>(){}.getType();
			
			response = serverIF.sendRequest("GET<deliveries>");
				fhList = gson.fromJson(response, listtype);
				
			if(!response.startsWith("NO_FAMILY_HISTORY"))
			{
				response =  "FAMILY_HISTORY_LOADED";
				this.fireDataChanged(this, "LOADED_FAMILY_HISTORY",  null);
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
    		oncwritefile= fc.getFile("Select .csv file to save Delivery DB to",
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
	    		 String[] header = {"Delivery ID", "Family ID", "Status", "Del By", 
	    				 			"Notes", "Changed By", "Time Stamp"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCFamilyHistory d:fhList)
	    	    	writer.writeNext(d.getExportRow());	//Get family data
	    	 
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
		if(ue.getType().equals("ADDED_DELIVERY"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_DELIVERY"))
		{
			processUpdatedObject(this, ue.getJson());
		}
	}

	@Override

	/***************************************************************
	 * This method is called when a family history object has been updated by 
	 * the user. The request update object is passed. 
	 *************************************************************/
	String update(Object source, ONCObject updatedHistory)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		
		response = serverIF.sendRequest("POST<update_delivery>" + 
												gson.toJson(updatedHistory, ONCFamilyHistory.class));
			 
		
		//check response. If response from server indicates a successful update,
		//create and store the updated object in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response.startsWith("UPDATED_DELIVERY"))
			processUpdatedObject(source, response.substring(16));
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		//Create a new object for the update
		Gson gson = new Gson();
		ONCFamilyHistory updatedObj = gson.fromJson(json, ONCFamilyHistory.class);
		
		//Find the position for the history object being updated
		int index = 0;
		while(index < fhList.size() && fhList.get(index).getID() != updatedObj.getID())
			index++;
		
		//Replace the current history object with the update
		if(index < fhList.size())
		{
			fhList.set(index, updatedObj);
			fireDataChanged(source, "UPDATED_DELIVERY", updatedObj);
		}
		else
			System.out.println(String.format("DeliveryDB processUpdatedObject - delivery id %d not found",
					updatedObj.getID()));
	}

	/**************************************************************************************************
	 * Return the number of deliveries made by a specifc driver. The driver number is passes and
	 * the count is returned.
	 */
	public int getNumberOfDeliveries(String drvNum) 
	{
		int nDeliveries = 0;
		for(ONCFamilyHistory del: fhList)
			if(del.getdDelBy().equals(drvNum))
				nDeliveries++;
		
		return nDeliveries;
	}
}
