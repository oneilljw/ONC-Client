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

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AdultDB extends ONCDatabase
{
	private static AdultDB instance = null;
	private List<ONCAdult> adultList;
	
	private AdultDB()
	{
		super();
		adultList = new ArrayList<ONCAdult>();
	}
	
	public static AdultDB getInstance()
	{
		if(instance == null)
			instance = new AdultDB();
		
		return instance;
	}
	
	ONCAdult getAdult(int id)
	{
		int index = 0;
		while(index < adultList.size() && adultList.get(index).getID() != id)
			index++;
		
		if(index == adultList.size())
			return null;	//Adult wasn't found
		else
			return adultList.get(index);
	}
	
	//Used to create an adult internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedAdult = null;
				
		//send add adult request to server
		String response = "";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_adult>" + gson.toJson(entity, ONCAdult.class));
				
			//if the server added the adult,  add the new adult to the data base and notify ui's
			if(response.startsWith("ADDED_ADULT"))		
				addedAdult =  processAddedAdult(source, response.substring(11));
		}
				
		return addedAdult;
	}
			
	ONCObject processAddedAdult(Object source, String json)
	{
		ONCAdult addedAdult = null;
		Gson gson = new Gson();
		addedAdult = gson.fromJson(json, ONCAdult.class);
			
		if(addedAdult != null)
		{
			adultList.add(addedAdult);
			fireDataChanged(source, "ADDED_ADULT", addedAdult);
		}
			
		return addedAdult;
	}
	
	/***************************************************************
	 * This method is called when an adult has been updated by 
	 * the user. The request updated adult object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedAdult)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_adult>" + 
												gson.toJson(updatedAdult, ONCAdult.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated adult in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_ADULT"))
			processUpdatedAdult(source, response.substring(13));
		
		return response;
	}
	
	void processUpdatedAdult(Object source, String json)
	{
		//Create a ONCAdult object for the updated adult
		Gson gson = new Gson();
		ONCAdult updatedAdult = gson.fromJson(json, ONCAdult.class);
		
		//Find the position for the current adult being updated
		int index = 0;
		while(index < adultList.size() && adultList.get(index).getID() != updatedAdult.getID())
			index++;
		
		//Replace the current ONCAdult object with the update
		if(index < adultList.size())
		{
			adultList.set(index, updatedAdult);
			fireDataChanged(source, "UPDATED_ADULT", updatedAdult);
		}
		else
			System.out.println(String.format("Adult DB processUpdatedAdult - adult id %d not found",
					updatedAdult.getID()));
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete an adult.
	 **********************************************************************************************************/
	ONCAdult delete(Object source, ONCAdult reqDelAdult)
	{
		String response = "";
		ONCAdult deletedAdult = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_adult>" + gson.toJson(reqDelAdult, ONCAdult.class));		
			
			//if the server deleted the meal, notify ui's
			if(response.startsWith("DELETED_ADULT"))		
				deletedAdult = processDeletedAdult(source, response.substring(13));
		}
		
		return deletedAdult;
	}
	
	ONCAdult processDeletedAdult(Object source, String json)
	{
		Gson gson = new Gson();
		ONCAdult deletedAdult = gson.fromJson(json, ONCAdult.class);
				
		//remove the adult from this local data base
		int index = 0;
		while(index < adultList.size() && adultList.get(index).getID() != deletedAdult.getID())
			index++;
		
		if(index < adultList.size())
		{
			adultList.remove(index);
			fireDataChanged(source, "DELETED_ADULT", deletedAdult);
			return deletedAdult;
		}
		else
			
			return null;
	}
	
	int getNumberOfOtherAdultsInFamily(int familyID)
	{
		int count = 0;
		for(ONCAdult adult : adultList)
			if(adult.getFamID() == familyID)
				count++;
		
		return count;
	}
	
	List<ONCAdult> getAdultsInFamily(int famID)
	{
		List<ONCAdult> famAdultList = new ArrayList<ONCAdult>();
		
		for(ONCAdult adult:adultList)
			if(adult.getFamID() == famID)
				famAdultList.add(adult);
		
		return famAdultList;
	}
	
	String importDB()
	{
		String response = "NO_ADULTS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCAdult>>(){}.getType();
			
			response = serverIF.sendRequest("GET<adults>");
			adultList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_ADULTS"))
			{
				response =  "ADULTS_LOADED";
			}	fireDataChanged(this, "LOADED_ADULTS", null);
		}
		
		return response;
	}
	
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Adult DB to",
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
	    		 String[] header = {"ID", "Family ID", "Name", "Gender"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCAdult a : adultList)
	    	    	writer.writeNext(a.getExportRow());	//Get family data
	    	 
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
		if(ue.getType().equals("UPDATED_ADULT"))
		{
			processUpdatedAdult(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_ADULT"))
		{
			processAddedAdult(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_ADULT"))
		{
			processDeletedAdult(this, ue.getJson());
		}			
	}
}
