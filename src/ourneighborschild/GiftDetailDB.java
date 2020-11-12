package ourneighborschild;

import java.io.File;
import java.io.FileReader;
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

public class GiftDetailDB extends ONCDatabase
{
	private static final int WISH_DETAIL_HEADER_LENGTH = 3;
	private static GiftDetailDB instance = null;
	private ArrayList<GiftDetail> giftDetailList;
	
	private GiftDetailDB()
	{
		super();
		this.title = "Gift Detail";
		giftDetailList = new ArrayList<GiftDetail>();
	}
	
	public static GiftDetailDB getInstance()
	{
		if(instance == null)
			instance = new GiftDetailDB();
		
		return instance;
	}

	@Override
	List<? extends ONCObject> getList()
	{
		return giftDetailList;
	}
	
	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_wishdetail>" + 
											 gson.toJson(entity, GiftDetail.class));
		
		if(response != null && response.startsWith("UPDATED_WISH_DETAIL"))
			processUpdatedWishDetail(source, response.substring(19));
		
		return response;
		
	}
	
	void processUpdatedWishDetail(Object source, String json)
	{
		//create an object from the response
		Gson gson = new Gson();
		GiftDetail updatedWishDetail = gson.fromJson(json,GiftDetail.class);
		
		//replace the current wish detail with the update
		int index = 0;
		while(index < giftDetailList.size() && giftDetailList.get(index).getID() != updatedWishDetail.getID())
			index++;
		
		if(index < giftDetailList.size())
			giftDetailList.set(index, updatedWishDetail);
	}
	
	String add(Object source, ONCObject entity)
	{	
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_wishdetail>" + 
											 gson.toJson(entity, GiftDetail.class));
		
		if(response.startsWith("ADDED_WISH_DETAIL"))
			processAddedWishDetail(source, response.substring(17));
		
		return response;
	}
	
	void processAddedWishDetail(Object source, String json)
	{
		Gson gson = new Gson();
		GiftDetail addedWishDetail = gson.fromJson(json, GiftDetail.class);
		
		giftDetailList.add(addedWishDetail);
		
//		System.out.println(String.format("WishDetailDB: processAddedWish - WishDetail added - " +
//				"WishDetail Name: %s, WishDetail Choices %s, now have %d wish details in DB", addedWishDetail.getWishDetailName(),
//				addedWishDetail.getWishDetailChoiceString(), wdAL.size()));
	}
	
	String delete(Object source, ONCObject entity)
	{	
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_wishdetail>" + 
											 gson.toJson(entity, GiftDetail.class));
		
		if(response.startsWith("DELETED_WISH_DETAIL"))
			processDeletedWishDetail(source, response.substring(19));
		
		return response;
	}
	
	void processDeletedWishDetail(Object source, String json)
	{
		Gson gson = new Gson();
		GiftDetail deletedWishDetail = gson.fromJson(json, GiftDetail.class);
		
		
		//notify wish catalog that the detail has been deleted
		//TODO: create the notification. Otherwise, it's possible a user will try to 
		//TODO: to edit a wish that has already had a wish detail entry deleted.
	
		//remove deleted wish detail from the data base
		int index = 0;
		while(index < giftDetailList.size() && giftDetailList.get(index).getID() != deletedWishDetail.getID())
			index++;
				
		if(index < giftDetailList.size())
			giftDetailList.remove(index);
	}
	
	GiftDetail getWishDetail(int id)
	{
		int index = 0;
		while(index < giftDetailList.size() && giftDetailList.get(index).getID() != id)
			index++;
		
		if(index == giftDetailList.size())
			return null;
		else
			return giftDetailList.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public void readWishDetailALObject(ObjectInputStream ois)
	{
		ArrayList<GiftDetail> wdal = new ArrayList<GiftDetail>();
		
		try 
		{
			wdal = (ArrayList<GiftDetail>) ois.readObject();
			
			for(GiftDetail wd:wdal)
			{
				giftDetailList.add(wd);
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
			oos.writeObject(giftDetailList);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<GiftDetail>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<wishdetail>");
				
			
			if(response != null)
			{
				giftDetailList = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
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
	    				giftDetailList.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					giftDetailList.add(new GiftDetail(nextLine));
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

	@Override
	String[] getExportHeader()
	{
		return new String[] {"Wish Detail ID", "Name", "Choices"};
	}
}
