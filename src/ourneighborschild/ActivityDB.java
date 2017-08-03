package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ActivityDB extends ONCSearchableDatabase 
{
	private static final EntityType DB_TYPE = EntityType.ACTIVITY;
	private static ActivityDB instance = null;
	private List<VolunteerActivity> activityList;
	
	private ActivityDB()
	{
		super(DB_TYPE);
		activityList = new ArrayList<VolunteerActivity>();
	}
	
	public static ActivityDB getInstance()
	{
		if(instance == null)
			instance = new ActivityDB();
		
		return instance;
	}
	
	@Override
	public void dataChanged(ServerEvent ue) 
	{
		// TODO Auto-generated method stub
		
	}
	
	VolunteerActivity getActivity(int id)
	{
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != id)
			index++;
		
		if(index < activityList.size())
			return activityList.get(index);
		else
			return null;
	}
	
	//creates a list of volunteer activities based on stored string of activity ID's 
	//separated by the '_' character.
	List<VolunteerActivity> createActivityList(String zActivities)
	{
		List<VolunteerActivity> volActList = new LinkedList<VolunteerActivity>();
			
		String[] activityParts = zActivities.split("_");
		for(String zActivity : activityParts)
		{
			int index = 0;
			while(index < activityList.size() && activityList.get(index).getID() != Integer.parseInt(zActivity))
				index++;
			
			if(index < activityList.size())
				volActList.add(activityList.get(index));
		}
			
		return volActList;
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	int size() 
	{
		return activityList.size();
	}

	@Override
	List<? extends ONCEntity> getList() { return activityList; }
	
	//implementation of abstract classes
	VolunteerActivity getObjectAtIndex(int index) { return activityList.get(index); }

	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_activity>" + 
											gson.toJson(entity, VolunteerActivity.class));
		
		if(response.startsWith("UPDATED_ACTIVITY"))
		{
			processUpdatedObject(source, response.substring(16), activityList);
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json, List<? extends ONCObject> objList)
	{
		Gson gson = new Gson();
		VolunteerActivity updatedObj = gson.fromJson(json, VolunteerActivity.class);
		
		//store updated object in local data base
		int index = 0;
		while(index < objList.size() && objList.get(index).getID() != updatedObj.getID())
			index++;
		
		if(index < objList.size())
		{
			activityList.set(index, updatedObj);
			
			//Notify local user IFs that a change occurred
			fireDataChanged(source, "UPDATED_ACTIVITY", updatedObj);
		}
	}
	
	String importDatabase()
	{
		String response = "NO_ACTIVITIES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<VolunteerActivity>>(){}.getType();
			
			response = serverIF.sendRequest("GET<activities>");
				activityList = gson.fromJson(response, listtype);				

			if(!response.startsWith("NO_ACTIVITIES"))
			{
				response =  "ACTIVITIES_LOADED";
				fireDataChanged(this, "LOADED_ACTIVITIES", null);
			}
		}
		
		return response;
	}
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_activity>" + 
											gson.toJson(entity, VolunteerActivity.class));
		
		if(response.startsWith("ADDED_ACTIVITY"))
			processAddedObject(source, response.substring(14));
		
		return response;	
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added activity in local data base
		Gson gson = new Gson();
		VolunteerActivity addedActivity = gson.fromJson(json, VolunteerActivity.class);
		activityList.add(addedActivity);
//		System.out.println(String.format("DriverDB processAddedDriver: Driver Added ID: %d",
//				addedDriver.getID()));
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_ACTIVITY", addedActivity);
	}
	
	String[] getActivityCategoryList()
	{
		List<String> catList = new ArrayList<String>();
		for(VolunteerActivity activity : activityList)
		{
			int index = 0;
			while(index < catList.size() && !catList.get(index).equals(activity.getCategory()))
				index++;
				
			if(index == catList.size())
				catList.add(activity.getCategory());
		}
		
		String[] catArr = new String[catList.size()];
		return catList.toArray(catArr);
	}
	
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Activity DB to",
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
	    		String[] header = {"ID", "Category" ,"Name", "Start Time", "End Time", 
		 				  			"Location", "Description"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(VolunteerActivity va:activityList)
	    	    	writer.writeNext(va.getExportRow());	//Get activity data
	    	 
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
}
