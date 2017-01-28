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

public class GroupDB extends ONCSearchableDatabase 
{

	/********
	 * This class implements a singleton data base for ONC Groups. Groups are collections of 
	 * ONCEntity objects that are aligned organizationally. An example are referring agents that 
	 * are associated with a particular school. The school would be a group and each agent that
	 * serves that school would be a member of the group.
	 */
	private static final EntityType DB_TYPE = EntityType.GROUP;
	private static final int MAX_GROUP_ID_LENGTH = 4;
	
	private static GroupDB instance = null;
	private List<ONCGroup> groupList;
	
	private GroupDB()
	{
		super(DB_TYPE);
		groupList= new ArrayList<ONCGroup>();
	}
	
	public static GroupDB getInstance()
	{
		if(instance == null)
			instance = new GroupDB();
		
		return instance;
	}
	
	//getters from groupDB
	ONCGroup getGroup(int index) { return groupList.get(index); }

	List<ONCGroup> getList() { return groupList; }
	
	public ONCGroup getGroupByID(int id)
	{
		int index = 0;
		while(index < groupList.size() && groupList.get(index).getID() != id)
			index++;
		
		if(index < groupList.size())
			return groupList.get(index);
		else
			return null;
	}
	
	
	@Override
	String update(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_group>" + 
											gson.toJson(entity, ONCGroup.class));
		
		if(response.startsWith("UPDATED_GROUP"))
		{
			processUpdatedGroup(source, response.substring(13));
		}
		
		return response;	
	}
	
	void processUpdatedGroup(Object source, String json)
	{
		//convert json to added group
		Gson gson = new Gson();
		ONCGroup updatedGroup = gson.fromJson(json, ONCGroup.class);
		
		//find the updated group in the data base and replace and notify ui's
		int index=0;
		while(index < groupList.size() &&
			   groupList.get(index).getID() != updatedGroup.getID())
			index++;
		
		if(index < groupList.size())
		{
			groupList.set(index, updatedGroup);
			fireDataChanged(source, "UPDATED_GROUP", updatedGroup);
		}
	}
	
	ONCObject add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response;
		
		response = serverIF.sendRequest("POST<add_group>" + 
											gson.toJson(entity, ONCGroup.class));
		
		if(response != null && response.startsWith("ADDED_GROUP"))
			return processAddedGroup(source, response.substring(11));
		else
			return null;	
	}
	
	ONCObject processAddedGroup(Object source, String json)
	{
		Gson gson = new Gson();
		ONCGroup addedGroup = gson.fromJson(json, ONCGroup.class);
		
		if(addedGroup != null)
		{
			groupList.add(addedGroup);
			fireDataChanged(source, "ADDED_GROUP", addedGroup);
		}
		
		return addedGroup;
	}

	//Delete a group
	String  delete(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_group>" + 
											gson.toJson(entity, ONCGroup.class));
		
		if(response.startsWith("DELETED_GROUP"))
			processDeletedGroup(source, response.substring(13));
		
		return response;	
	}
	
	void processDeletedGroup(Object source, String json)
	{
		//locate and remove deleted group local data base. Notify ui's
		Gson gson = new Gson();
		ONCGroup deletedGroup = gson.fromJson(json, ONCGroup.class);
		
		int index = 0;
		while(index < groupList.size() &&
				groupList.get(index).getID() != deletedGroup.getID())
			index++;
		
		if(index < groupList.size())
		{
			groupList.remove(index);
			fireDataChanged(source, "DELETED_CATALOG_WISH", deletedGroup);
		}
	}
	
	
	String importGroupDBFromServer()
	{
		String response = "NO_GROUPS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCGroup>>(){}.getType();
			
			response = serverIF.sendRequest("GET<groups>");
			groupList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_GROUPS"))
			{		
				response =  "GROUPS_LOADED";
				fireDataChanged(this, "LOADED_GROUPS", null);
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
    		oncwritefile= fc.getFile("Select .csv file to save Group DB to",
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
	    		String[] header = {"ID", "Date Changed", "Changed By", "SL Position", "SL Message", 
						 "SL Changed By","Name", "Type", "Permission"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCGroup group:groupList)
	    	    	writer.writeNext(group.getExportRow());	//Get family data
	    	 
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

		if(ue.getType().equals("ADDED_GROUP"))
		{
			processAddedGroup(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_GROUP"))
		{
			processUpdatedGroup(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_GROUP"))
		{
			processDeletedGroup(this, ue.getJson());
		}
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		String searchType = "";
		searchAL.clear();
		
    	int sn; 	//-1 indicates family number not found
    	
    	if(isNumeric(data) && data.length() < MAX_GROUP_ID_LENGTH)
    	{
    		searchType = "Group ID";
    		if(data.matches("-?\\d+(\\.\\d+)?"))	//Check for numeric string
    		{
    			if((sn = getListIndexByID(groupList, Integer.parseInt(data))) > -1)
    				searchAL.add(getObjectAtIndex(sn).getID());
    		}	
    	}
    	else	//Check for partner name, email matches, 1st or 2nd contact matches
    	{
    		searchType = "Group Name";
			for(ONCGroup g:groupList)
			{
				if(g.getName().toLowerCase().contains(data.toLowerCase()))
				{
					searchAL.add(g.getID());
				}
			}
    	}
    	
    	return searchType;
	}

	@Override
	int size() { return groupList.size(); }
		
	@Override
	ONCEntity getObjectAtIndex(int index) { return groupList.isEmpty() ? null : groupList.get(index); }
}
