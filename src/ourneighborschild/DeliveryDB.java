package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
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

public class DeliveryDB extends ONCDatabase
{
	private static final int DELIVERYDB_CSV_HEADER_LENGTH = 7;
	private static DeliveryDB instance = null;
	private ArrayList<ONCFamilyHistory> dAL;
	
	private DeliveryDB()
	{
		super();
		dAL = new ArrayList<ONCFamilyHistory>();
	}
	
	public static DeliveryDB getInstance()
	{
		if(instance == null)
			instance = new DeliveryDB();
		
		return instance;
	}
/*	
	int addDelivery(int famid, int dStat, String dBy, String notes, String cb, Calendar dateChanged)
	{
		int did = dID++;
		
		dAL.add(new ONCDelivery(did, famid, dStat, dBy, notes, cb, dateChanged));
		
		return did;
	}
*/	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_delivery>" + 
											gson.toJson(entity, ONCFamilyHistory.class));
		if(response.startsWith("ADDED_DELIVERY"))
			processAddedObject(source, response.substring(14));
		
		return response;	
	}
	
	String addGroup(Object source, List<ONCFamilyHistory> deliveryList)
	{
		Gson gson = new Gson();
		Type listtype = new TypeToken<ArrayList<ONCFamilyHistory>>(){}.getType();
			
		String response = gson.toJson(deliveryList, listtype);
		
		response = serverIF.sendRequest("POST<delivery_group>" + gson.toJson(deliveryList, listtype));
		
		return response;
	}
	
	void processAddedObject(Object source, String json)
	{
		//Store added ONCDelivery object in local data base
		Gson gson = new Gson();
		ONCFamilyHistory addedObject = gson.fromJson(json, ONCFamilyHistory.class);
		
		dAL.add(addedObject);
//		System.out.println(String.format("DeliveryDB processAddedObject: Delivery Added ID: %d",
//				addedObject.getID()));
		
		//update the family status this delivery is associated with
		FamilyDB familyDB = FamilyDB.getInstance();
		ONCFamily fam = familyDB.getFamily(addedObject.getFamID());
		fam.setGiftStatus(addedObject.getdStatus());
		fam.setDeliveryID(addedObject.getID());
		
		//Notify local user IFs that an organization/partner was added. This will
		//be all UI's that display or manage family delivery information
		fireDataChanged(source, "ADDED_DELIVERY", addedObject);
	}
	
	ONCFamilyHistory getDelivery(int id)
	{
		int index = 0;
		while(index < dAL.size() && dAL.get(index).getID() != id)
			index++;
		
		if(index == dAL.size())
			return null;
		else
			return dAL.get(index);
	}
	
	ArrayList<ONCFamilyHistory> getDeliveryHistoryAL(int famID)
	{
		ArrayList<ONCFamilyHistory> famDelAL = new ArrayList<ONCFamilyHistory>();
		for(ONCFamilyHistory d:dAL)
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
		while(index < dAL.size() && dAL.get(index).getID() != delID)
			index++;
				
		if(index==dAL.size())
			return "";
		else
			return dAL.get(index).getdDelBy();
	}
	
	public void readDeliveryALObject(ObjectInputStream ois)
	{
		ArrayList<ONCFamilyHistory> dal = new ArrayList<ONCFamilyHistory>();
		
		try 
		{
			dal = (ArrayList<ONCFamilyHistory>) ois.readObject();
			
			for(ONCFamilyHistory d:dal)
			{
				dAL.add(d);
			}
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
	
	public void writeDeliveryALObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(dAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String importDeliveryDatabase()
	{
		String response = "NO_DELIVERIES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCFamilyHistory>>(){}.getType();
			
			response = serverIF.sendRequest("GET<deliveries>");
				dAL = gson.fromJson(response, listtype);
				
			if(!response.startsWith("NO_DELIVIERIES"))		
				response =  "DELIVERIES_LOADED";
		}
		
		return response;
	}
	
	String importDeliveryDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
    		
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "DeliveryDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Delivery DB .csv file to import");	
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
	    			if(header.length == DELIVERYDB_CSV_HEADER_LENGTH)
	    			{
	    				dAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					dAL.add(new ONCFamilyHistory(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Delivery DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Delivery DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Delivery DB file: " + filename, 
						"Invalid Delivery DB File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	
	    		reader.close();
	    	
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Delivery DB file: " + filename, 
    				"Delivery DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	
	String exportDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Delivery DB to",
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
	    		 String[] header = {"Delivery ID", "Family ID", "Status", "Del By", 
	    				 			"Notes", "Changed By", "Time Stamp"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCFamilyHistory d:dAL)
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
	 * This method is called when a child wish has been updated by 
	 * the user. The request update child object is passed. 
	 *************************************************************/
	String update(Object source, ONCObject updatedDelivery)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		
		response = serverIF.sendRequest("POST<update_delivery>" + 
												gson.toJson(updatedDelivery, ONCFamilyHistory.class));
			 
		
		//check response. If response from server indicates a successful update,
		//create and store the updated child in the local data base and notify local
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
		
		//Find the position for the delivery being updated
		int index = 0;
		while(index < dAL.size() && dAL.get(index).getID() != updatedObj.getID())
			index++;
		
		//Replace the current delivery object with the update
		if(index < dAL.size())
		{
			dAL.set(index, updatedObj);
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
		for(ONCFamilyHistory del: dAL)
			if(del.getdDelBy().equals(drvNum))
				nDeliveries++;
		
		return nDeliveries;
	}
}
