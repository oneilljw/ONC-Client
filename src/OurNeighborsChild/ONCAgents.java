package OurNeighborsChild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ONCAgents extends ONCDatabase
{
	/****************************************************************************************
	 * This class implements a data base for ONC Agents. Agent objects are contained in an 
	 * array list. The class provides methods for adding and retrieving agent objects to/from the
	 * data base. Agent objects can be retrieved by id. The class implements support for 
	 * serializing Agent objects to/from the input/output stream and clearing the data base.
	 * Finally, the class provides support for merging two agent object data bases together
	 * The ONC application maintains one Agent data base per instance of the application currently.
	 **************************************************************************************/
	private static final int AGENT_DB_HEADER_LENGTH = 6;
	private static ONCAgents instance = null;
	private ArrayList<Agent> agentsAL;	//The agent object array list data base
	
	private ONCAgentNameComparator oncAgentNameComparator;
	private ONCAgentOrgComparator oncAgentOrgComparator;
	private ONCAgentTitleComparator oncAgentTitleComparator;
	
	private ONCAgents()
	{
		super();
		agentsAL = new ArrayList<Agent>();
		oncAgentNameComparator = new ONCAgentNameComparator();
		oncAgentOrgComparator = new ONCAgentOrgComparator();
		oncAgentTitleComparator = new ONCAgentTitleComparator();
	}
	
	public static ONCAgents getInstance()
	{
		if(instance == null)
			instance = new ONCAgents();
		
		return instance;
	}	
	
	/***********************************************************************************
	 * This method retrieves an Agent object from the array list data base. An id is 
	 * passed as a parameter and the array list is searched for a matching Agent id. If
	 * the id is located, a reference to the first Agent object containing that id is returned.
	 * If the id is not located, the method returns a null Agent object. 
	 * @param id
	 * @return
	 ***********************************************************************************/
	Agent getAgent(int id)
	{
		int index = 0;
		while(index < agentsAL.size() && id != agentsAL.get(index).getID())
			index++;
		
		if(index == agentsAL.size())
			return null;
		else
			return agentsAL.get(index);		
	}
	
	ArrayList<Agent> getAgentsAL() { return agentsAL; }
	
	public void readAgentALObject(ObjectInputStream ois)
	{
		ArrayList<Agent> agentDB = new ArrayList<Agent>();
		try 
		{
			agentDB = (ArrayList<Agent>) ois.readObject();
			
			for(Agent a:agentDB)
				agentsAL.add(a);

			agentDB.clear();
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
	
	public void writeAgentALObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(agentsAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void clearAgentData()
	{
		agentsAL.clear();
	}
	
	/***********************************************************************************
	 * This method takes an Agent data base and merges it with the current data base.
	 * Each Agent object in the data base to be merged (merge data base) is compared to each
	 * Agent object in the current data base. If an identical match is found, the merge object
	 * is ignored. If the merge Agent name matches the current Agent object name the merge
	 * Agent object data is ignored, as ODB Agent data always takes preference over ONC Agent 
	 * data. 
	 ***********************************************************************************/
	void mergeAgentDataBase(ArrayList<Agent> mergeAgentAL)
	{
		for(Agent mergeAgent:mergeAgentAL)
		{
			//Search current Agent data base for Agent name match
			int index = 0;
			while(index < agentsAL.size() && !mergeAgent.getAgentName().equals(agentsAL.get(index).getAgentName()))
			{
				index++;
			}
			
			if(index == agentsAL.size())	//Agent not found, add a new agent
			{
				agentsAL.add(new Agent(index, mergeAgent.getAgentName(), mergeAgent.getAgentOrg(),
										mergeAgent.getAgentTitle(), mergeAgent.getAgentEmail(),
										mergeAgent.getAgentPhone()));
			}		
		}
	}
	
	ONCAgentNameComparator getAgentNameComparator() { return oncAgentNameComparator; }
	ONCAgentOrgComparator getAgentOrgComparator() { return oncAgentOrgComparator; }
	ONCAgentTitleComparator getAgentTitleComparator() { return oncAgentTitleComparator; }
	
	
	String importAgentDatabase()
	{
		ServerIF serverIF = ServerIF.getInstance();
		String response = "NO_AGENTS";
		
		if(serverIF != null && serverIF.isConnected())
		{	
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<Agent>>(){}.getType();
			
			response = serverIF.sendRequest("GET<agents>");
			agentsAL = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_AGENTS"))		
				response = "AGENTS_LOADED";
		}
		
		return response;
	}
	
	String importAgentDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{   		
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			System.out.println(path);
			pyfile = new File(path + "AgentDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Agent DB .csv file to import");	
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
	    			if(header.length == AGENT_DB_HEADER_LENGTH)
	    			{
	    				agentsAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					agentsAL.add(new Agent(Integer.parseInt(nextLine[0]), nextLine[1], nextLine[2],
	    											nextLine[3], nextLine[4], nextLine[5]));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Agent DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Agent DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Agent DB file: " + filename, 
	    					"Invalid Agent DB File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Agent db file: " + filename, 
    				"Agent DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	
	String exportAgentDBToCSV(JFrame pf, String filename)
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
	    	    
	    	    for(Agent a:agentsAL)
	    	    	writer.writeNext(a.getDBExportRow());	//Get family data
	    	 
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
		while(updatedAgt != null && index < agentsAL.size() && 
				updatedAgt.getID() != agentsAL.get(index).getID())
			index++;
		
		if(updatedAgt != null && index < agentsAL.size())
		{
			agentsAL.set(index, updatedAgt);
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
//	Agent add(Object source, String name, String org, String title, String email, String phone)
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
			agentsAL.add(addedAgt);
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
		while(deletedAgt != null && index < agentsAL.size() && 
				deletedAgt.getID() != agentsAL.get(index).getID())
			index++;
		
		if(index < agentsAL.size())
			agentsAL.remove(index);
		
		fireDataChanged(source, "DELETED_AGENT", deletedAgt);
		
		return deletedAgt;
	}
}
