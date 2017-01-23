package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class WishDetailDB extends ONCDatabase
{
	private static final int WISH_DETAIL_HEADER_LENGTH = 3;
	private static WishDetailDB instance = null;
	private ArrayList<WishDetail> wdAL;
	
	private WishDetailDB()
	{
		super();
		wdAL = new ArrayList<WishDetail>();
	}
	
	public static WishDetailDB getInstance()
	{
		if(instance == null)
			instance = new WishDetailDB();
		
		return instance;
	}
	
	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_wishdetail>" + 
											 gson.toJson(entity, WishDetail.class));
		
		if(response != null && response.startsWith("UPDATED_WISH_DETAIL"))
			processUpdatedWishDetail(source, response.substring(19));
		
		return response;
		
	}
	
	void processUpdatedWishDetail(Object source, String json)
	{
		//create an object from the response
		Gson gson = new Gson();
		WishDetail updatedWishDetail = gson.fromJson(json,WishDetail.class);
		
		//replace the current wish detail with the update
		int index = 0;
		while(index < wdAL.size() && wdAL.get(index).getID() != updatedWishDetail.getID())
			index++;
		
		if(index < wdAL.size())
			wdAL.set(index, updatedWishDetail);
	}
	
	String add(Object source, ONCObject entity)
	{	
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_wishdetail>" + 
											 gson.toJson(entity, WishDetail.class));
		
		if(response.startsWith("ADDED_WISH_DETAIL"))
			processAddedWishDetail(source, response.substring(17));
		
		return response;
	}
	
	void processAddedWishDetail(Object source, String json)
	{
		Gson gson = new Gson();
		WishDetail addedWishDetail = gson.fromJson(json, WishDetail.class);
		
		wdAL.add(addedWishDetail);
		
//		System.out.println(String.format("WishDetailDB: processAddedWish - WishDetail added - " +
//				"WishDetail Name: %s, WishDetail Choices %s, now have %d wish details in DB", addedWishDetail.getWishDetailName(),
//				addedWishDetail.getWishDetailChoiceString(), wdAL.size()));
	}
	
	String delete(Object source, ONCObject entity)
	{	
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_wishdetail>" + 
											 gson.toJson(entity, WishDetail.class));
		
		if(response.startsWith("DELETED_WISH_DETAIL"))
			processDeletedWishDetail(source, response.substring(19));
		
		return response;
	}
	
	void processDeletedWishDetail(Object source, String json)
	{
		Gson gson = new Gson();
		WishDetail deletedWishDetail = gson.fromJson(json, WishDetail.class);
		
		
		//notify wish catalog that the detail has been deleted
		//TODO: create the notification. Otherwise, it's possible a user will try to 
		//TODO: to edit a wish that has already had a wish detail entry deleted.
	
		//remove deleted wish detail from the data base
		int index = 0;
		while(index < wdAL.size() && wdAL.get(index).getID() != deletedWishDetail.getID())
			index++;
				
		if(index < wdAL.size())
			wdAL.remove(index);
	}
	
	WishDetail getWishDetail(int id)
	{
		int index = 0;
		while(index < wdAL.size() && wdAL.get(index).getID() != id)
			index++;
		
		if(index == wdAL.size())
			return null;
		else
			return wdAL.get(index);
	}
	
	public void readWishDetailALObject(ObjectInputStream ois)
	{
		ArrayList<WishDetail> wdal = new ArrayList<WishDetail>();
		
		try 
		{
			wdal = (ArrayList<WishDetail>) ois.readObject();
			
			for(WishDetail wd:wdal)
			{
				wdAL.add(wd);
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
	
	public void writeWishetailALObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(wdAL);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String importWishDetailDatabase()
	{
		String response = "NO_DETAIL";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<WishDetail>>(){}.getType();
			
			response = serverIF.sendRequest("GET<wishdetail>");
				wdAL = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_DETAIL"))		
				response =  "DETAIL_LOADED";
		}
		
		return response;
	}
	
	String importWishDetailDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "WishDetailDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Wish Detail DB .csv file to import");	
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
	    			if(header.length == WISH_DETAIL_HEADER_LENGTH)
	    			{
	    				wdAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					wdAL.add(new WishDetail(nextLine));
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Wish Detail DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Wish Detail DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in wish detail db file: " + filename, 
	    					"Invalid Wish Detail DB File", JOptionPane.ERROR_MESSAGE, oncIcon);
	    		reader.close();
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open wish detail db file: " + filename, 
    				"Wish Detail DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
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
    		oncwritefile= fc.getFile("Select .csv file to save Wish Detail DB to",
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
	    		 String[] header = {"Wish Detail ID", "Name", "Choices"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(WishDetail wd:wdAL)
	    	    	writer.writeNext(wd.getExportRow());	//Get family data
	    	 
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
		if(ue.getType().equals("ADDED_WISH_DETAIL"))
		{
			processAddedWishDetail(this, ue.getJson());
		}
		if(ue.getType().equals("UPDATED_WISH_DETAIL"))
		{
			processUpdatedWishDetail(this, ue.getJson());
		}
	}
}
