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
	public void dataChanged(ServerEvent ue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String update(Object source, ONCObject entity) {
		// TODO Auto-generated method stub
		return null;
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
