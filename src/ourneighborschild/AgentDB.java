package ourneighborschild;

import java.io.File;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


//import javax.swing.ImageIcon;
//import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import au.com.bytecode.opencsv.CSVWriter;

public class AgentDB extends ONCDatabase
{
	/****************************************************************************************
	 * This singleton class implements a data base for ONC Agents. Agent objects are contained
	 * in a list. The class provides methods for adding and retrieving agent objects to/from 
	 * the data base.
	 **************************************************************************************/
	private static AgentDB instance = null;
	private ArrayList<Agent> oncObjectList;	//The agent object array list data base
	
	private AgentDB()
	{
		super();
		oncObjectList = new ArrayList<Agent>();
	}
	
	public static AgentDB getInstance()
	{
		if(instance == null)
			instance = new AgentDB();
		
		return instance;
	}	
	
	/***********************************************************************************
	 * This method retrieves an Agent object from the agent list. An id is 
	 * passed as a parameter and the list is searched for a matching Agent id. If
	 * the id is located, a reference to the first Agent object containing that id is returned.
	 * If the id is not located, the method returns a null Agent object. 
	 * @param id
	 * @return
	 ***********************************************************************************/
	ONCObject getONCObject(int id)
	{
		int index = 0;
		while(index < oncObjectList.size() && id != oncObjectList.get(index).getID())
			index++;
		
		if(index == oncObjectList.size())
			return null;
		else
			return oncObjectList.get(index);		
	}
	
	ArrayList<Agent> getList() { return oncObjectList; }
	ONCAgentNameComparator getAgentNameComparator() { return  new ONCAgentNameComparator(); }
	ONCAgentOrgComparator getAgentOrgComparator() { return new ONCAgentOrgComparator(); }
	ONCAgentTitleComparator getAgentTitleComparator() { return new ONCAgentTitleComparator(); }
	
	String importAgentDatabase()
	{
		String response = "NO_AGENTS";
		
		if(serverIF != null && serverIF.isConnected())
		{	
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<Agent>>(){}.getType();
			
			response = serverIF.sendRequest("GET<agents>");
			oncObjectList = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_AGENTS"))
			{
				response = "AGENTS_LOADED";
				fireDataChanged(this, "LOADED_AGENTS", null);
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
    		oncwritefile= fc.getFile("Select .csv file to save Agent DB to",
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
	    		 String[] header = {"Agent ID", "Name", "Organization", "Title", "Email", "Phone"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(Agent a:oncObjectList)
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
		if(ue.getType().equals("ADDED_AGENT"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_AGENT"))
		{
			processUpdatedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_AGENT"))
		{
			processDeletedObject(this, ue.getJson());
		}
		
	}
	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_agent>" + 
											gson.toJson(entity, Agent.class));
		
		if(response.startsWith("UPDATED_AGENT"))
		{
			processUpdatedObject(source, response.substring(13));
		}
		
		return response;	
	}
	
	ONCObject processUpdatedObject(Object source, String json)
	{
		Agent updatedAgt = null;
		
		Gson gson = new Gson();
		updatedAgt = gson.fromJson(json, Agent.class);
		
		int index = 0;
		while(updatedAgt != null && index < oncObjectList.size() && 
				updatedAgt.getID() != oncObjectList.get(index).getID())
			index++;
		
		if(updatedAgt != null && index < oncObjectList.size())
		{
			oncObjectList.set(index, updatedAgt);
			fireDataChanged(source, "UPDATED_AGENT", updatedAgt);
		}
		
		return updatedAgt;
	}

	/************************************************************************************
	 * This method takes agent info, searches the agent array list for a name match.
	 * If a match name match is found, the agent org, title, email and phone are overwritten.
	 * ODB Agent data is always updated with the latest import of family data.  
	 * and the agent object is returned. If a name match isn't found, it creates a new Agent
	 * object and adds the object to the array list and returns the new agent object 
	 **********************************************************************************/
	ONCObject add(Object source, ONCObject reqAddAgt)
	{
		Gson gson = new Gson();
		
		//create the add agent request and send it to the server
		String response = null;
		response = serverIF.sendRequest("POST<add_agent>" + gson.toJson(reqAddAgt, Agent.class));
		
		//response will determine if agent already existed or a new agent was added
		if(response != null && response.startsWith("UPDATED_AGENT"))
			return processUpdatedObject(source, response.substring(13));
		else if(response != null && response.startsWith("ADDED_AGENT"))
			return processAddedObject(source, response.substring(11));
		else
			return null;
	}
	
	ONCObject processAddedObject(Object source, String json)
	{
		Agent addedAgt = null;
		
		//Store added object in local database
		Gson gson = new Gson();
		addedAgt = gson.fromJson(json, Agent.class);
		
		if(addedAgt != null)
		{
			oncObjectList.add(addedAgt);
			fireDataChanged(source, "ADDED_AGENT", addedAgt);
		}
		
		return addedAgt;
	}
	
	//Delete an object from database.
	ONCObject delete(Object source, ONCObject entity) 
	{
		ONCObject delAgent = null;
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_agent>" + 
											gson.toJson(entity, Agent.class));
		
		if(response.startsWith("DELETED_AGENT"))
			delAgent = processDeletedObject(source, response.substring(13));
		
		return delAgent;	
	}
	
	ONCObject processDeletedObject(Object source, String json)
	{
		Gson gson = new Gson();
		Agent deletedAgt = gson.fromJson(json, Agent.class);
		int index = 0;
		while(deletedAgt != null && index < oncObjectList.size() && 
				deletedAgt.getID() != oncObjectList.get(index).getID())
			index++;
		
		if(index < oncObjectList.size())
			oncObjectList.remove(index);
		
		fireDataChanged(source, "DELETED_AGENT", deletedAgt);
		
		return deletedAgt;
	}
	
	boolean sortDB(ArrayList<Agent> aAL, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("Name"))	//Sort on Agent Name
    		Collections.sort(aAL, new ONCAgentNameComparator());
    	else if(dbField.contains("Org"))	// Sort on Agent Organization
    		Collections.sort(aAL, new ONCAgentOrgComparator());
    	else if (dbField.equals("Title"))	//Sort on Agent Title
    		Collections.sort(aAL, new ONCAgentTitleComparator());
		else
			bSortOccurred = false;
		
		return bSortOccurred;	
	}
	
	private class ONCAgentNameComparator implements Comparator<Agent>
	{
		@Override
		public int compare(Agent o1, Agent o2)
		{
			return o1.getAgentName().compareTo(o2.getAgentName());
		}
	}
	
	private class ONCAgentOrgComparator implements Comparator<Agent>
	{
		@Override
		public int compare(Agent o1, Agent o2)
		{			
			return o1.getAgentOrg().compareTo(o2.getAgentOrg());
		}
	}
	
	private class ONCAgentTitleComparator implements Comparator<Agent>
	{
		@Override
		public int compare(Agent o1, Agent o2)
		{			
			return o1.getAgentTitle().compareTo(o2.getAgentTitle());
		}
	}
}
