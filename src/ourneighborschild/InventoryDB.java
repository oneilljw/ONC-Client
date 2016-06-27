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

public class InventoryDB extends ONCDatabase
{
	private static InventoryDB instance = null;
	private List<InventoryItem> invList;
	
	private InventoryDB()
	{
		super();
		invList = new ArrayList<InventoryItem>();
	}
	
	public static InventoryDB getInstance()
	{
		if(instance == null)
			instance = new InventoryDB();
		
		return instance;
	}
	
	int size() { return invList.size(); }
	
	InventoryItem getItem(int index) { return invList != null ? invList.get(index) : null; }
	
	InventoryItem getItemByBarcode(String barcode)
	{
		int index = 0;
		while(index < invList.size() && !invList.get(index).getNumber().equals(barcode))
			index++;
		
		return index < invList.size() ? invList.get(index) : null;
	}
	
	String add(Object source, Object entity)
	{
		//the entity parameter may be either an InventoryRequest object (UPC bar code scan)
		//or an InventoryItem (manual bar code entry). The method will determine which and send
		//the appropriate json embedded in the add request to the server. The server will return 
		//either a ADDED_INVENTORY_ITEM message, an INCREMENTED_INVENTORY_ITEM message or and ADDED_INVENTORY_FAILED message
		
		String response = "";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			if(entity instanceof InventoryItem)
				response = serverIF.sendRequest("POST<add_inventory>" + gson.toJson(entity, InventoryItem.class));
			else
				response = serverIF.sendRequest("POST<add_barcode>" + gson.toJson(entity, InventoryRequest.class));
					
			//if the server added the item,  add the new child to the data base and notify ui's
			if(response.startsWith("ADDED_INVENTORY_ITEM"))		
				processAddedItem(source, response.substring(20));
			else if(response.startsWith("INCREMENTED_INVENTORY_ITEM"))
				processIncrementedItem(source, response.substring(26));
		}

		return response;	
	}
	
	InventoryItem processAddedItem(Object source, String json)
	{
		InventoryItem addedItem = null;
		Gson gson = new Gson();
		addedItem = gson.fromJson(json, InventoryItem.class);
		
		if(addedItem != null)
		{
			invList.add(addedItem);
			fireDataChanged(source, "ADDED_INVENTORY_ITEM", addedItem);
		}
		
		return addedItem;
	}
	
	InventoryItem processIncrementedItem(Object source, String json)
	{
		InventoryItem item = null;
		InventoryChange incrementedItem = null;
		Gson gson = new Gson();
		incrementedItem = gson.fromJson(json, InventoryChange.class);
		
		if(incrementedItem != null)
		{
			//find the item and change the count according to the increment
			int index = 0;
			while(index < invList.size() && invList.get(index).getID() != incrementedItem.getID())
				index++;
			
			if(index < invList.size())
			{
				item = invList.get(index);
				item.setCount(incrementedItem.getCount());
				item.setNCommits(incrementedItem.getCommits());
				fireDataChanged(source, "INCREMENTED_INVENTORY_ITEM", item);
			}
		}
		
		return item;
	}
	
	@Override
	String update(Object source, ONCObject entity)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_inventory>" + 
											gson.toJson(entity, InventoryItem.class));
				
		//check response. If response from server indicates a successful update,
		//create and store the updated item in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_INVENTORY_ITEM"))
			processUpdatedItem(source, response.substring(22));
				
		return response;
	}
	
	InventoryItem processUpdatedItem(Object source, String json)
	{
		InventoryItem updatedItem = null;
		Gson gson = new Gson();
		updatedItem = gson.fromJson(json, InventoryItem.class);
		
		if(updatedItem != null)
		{
			//find the item and change the count according to the increment
			int index = 0;
			while(index < invList.size() && invList.get(index).getID() != updatedItem.getID())
				index++;
			
			if(index < invList.size())
			{
				invList.set(index, updatedItem);
				fireDataChanged(source, "UPDATED_INVENTORY_ITEM", updatedItem);
			}
		}
		
		return updatedItem;
	}
	
	/****************************************************************************
	 * This method is called when the user requests to delete an inventory item.
	 *****************************************************************************/
	String delete(Object source, InventoryItem reqDelItem)
	{
		String response = null;
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_inventory>" + gson.toJson(reqDelItem, InventoryItem.class));		
			
			//if the server added the child,  add the new child to the data base and notify ui's
			if(response.startsWith("DELETED_INVENTORY_ITEM"))
				processDeletedItem(source, response.substring(22));
		}
		
		return response;
	}
	
	void processDeletedItem(Object source, String json)
	{	
		Gson gson = new Gson();
		InventoryItem deletedItem = gson.fromJson(json, InventoryItem.class);
				
		//remove item from local database
		int index = 0;
		while(index < invList.size() && invList.get(index).getID() != deletedItem.getID())
			index++;
		
		if(index < invList.size())
		{
			invList.remove(index);
			fireDataChanged(source, "DELETED_INVENTORY_ITEM", deletedItem);
		}
	}
	
	String importInventoryDatabase()
	{
		String response = "NO_INVENTORY";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<InventoryItem>>(){}.getType();
			
			response = serverIF.sendRequest("GET<inventory>");
			invList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_INVENTORY"))
			{
				response =  "INVENTORY_LOADED";
				fireDataChanged(this, "LOADED_INVENTORY", null);
			}
		}
		
		return response;
	}

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_INVENTORY_ITEM"))
			processUpdatedItem(this, ue.getJson());
		else if(ue.getType().equals("ADDED_INVENTORY_ITEM"))
			processAddedItem(this, ue.getJson());
		else if(ue.getType().equals("INCREMENTED_INVENTORY_ITEM"))
			processIncrementedItem(this, ue.getJson());
		else if(ue.getType().equals("DELETED_INVENTORY_ITEM"))
			processDeletedItem(this, ue.getJson());
	}

	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Inventory DB to",
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
	    		 String[] header = {"Child ID", "Family ID", "Child #", "First Name", "Last Name",
	    				 			"Gender", "DOB", "School", "Wish 1 ID", "Wish 2 ID",
	    				 			"Wish 3 ID", "Prior Year Child ID"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(InventoryItem ii:invList)
	    	    	writer.writeNext(ii.getExportRow());	//Get family data
	    	 
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
