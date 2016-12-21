package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class ONCWishCatalog extends ONCDatabase
{
	/********
	 * This class implements a singleton data base for ONC Wishes, known as the wish catalog.
	 * The catalog is a list of gifts that users can choose from to fulfill the wish requests
	 * of served children. In addition, the catalog maintains a count of the number of
	 * times a gift has been assigned to fulfill a wish. The count is broken into component
	 * counts - a separate count for each individual child wish. Each ONC child has more than 
	 * one wish, the variable NUMBER_OF_WISHES_PER_CHILD holds the number of wishes per child.
	 * 
	 * The catalog consists of a list of WishCatalogItems. Each WishCatalogItem contains an 
	 * ONCWish and the wish count for that wish, including component counts. Wish counts are
	 * not persistent in the ONCClient application, they are initialized once the catalog and
	 * family, child and child wish data is loaded from the server and maintained for the life
	 * of the client.
	 * 
	 * The catalog list is maintained in ONCWish name alphabetical order in order to support
	 * display using an AbstractTabelModel
	 * 
	 * The catalog provides a method that returns a list of ONCWishes that can be 
	 * used for sorting or selecting a wish. 
	 */
	private static final int ONC_WISH_CATALOG_HEADER_LENGTH = 7;
	private static final int WISH_CATALOG_LIST_ALL = 7;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	
	private static ONCWishCatalog instance = null;
	private ArrayList<WishCatalogItem> wishCatalog;
	private WishDetailDB  wdDB;
	
	private ONCWishCatalog()
	{
		super();
		wdDB = WishDetailDB.getInstance();
		
		wishCatalog = new ArrayList<WishCatalogItem>();
	}
	
	public static ONCWishCatalog getInstance()
	{
		if(instance == null)
			instance = new ONCWishCatalog();
		
		return instance;
	}
	
	//getters from wish catalog
	ONCWish getWish(int index) { return wishCatalog.get(index).getWish(); }
	ArrayList<ONCWish> getCatalogWishList()
	{
		ArrayList<ONCWish> wishList = new ArrayList<ONCWish>();
		for(WishCatalogItem wci: wishCatalog)
			wishList.add(wci.getWish());
		
		return wishList;
	}
	
	public ONCWish getWishByID(int wishID)
	{
		int index = 0;
		while(index < wishCatalog.size() && wishCatalog.get(index).getWish().getID() != wishID)
			index++;
		
		if(index < wishCatalog.size())
			return wishCatalog.get(index).getWish();
		else
			return null;
	}
	
	int findModelIndexFromID(int wishID)
	{
		int index = 0;
		while(index < wishCatalog.size() && wishCatalog.get(index).getWish().getID() != wishID)
			index++;
		
		if(index < wishCatalog.size())
			return index;
		else
			return -1;
	}
	
	int size() { return wishCatalog.size(); }
	int getWishID(int index) { return wishCatalog.get(index).getWish().getID(); }
	String getWishName(int index) { return wishCatalog.get(index).getWish().getName(); }
	
	ArrayList<Integer> getWishIDs()
	{
		ArrayList<Integer> wishIDs = new ArrayList<Integer>();
		for(ONCWish w:getCatalogWishList())
			wishIDs.add(w.getID());
		
		return wishIDs;
	}

	boolean isInWishList(int index, int wish)
	{
		int mask = 1;
		mask = mask << wish;
		return (wishCatalog.get(index).getWish().getListindex() & mask) > 0;
	}
	
	boolean isDetailRqrd(int index) { return wishCatalog.get(index).getWish().isDetailRqrd(); }
	
	int getWishID(String wishname)
	{
		int index=0;
		while(index < wishCatalog.size() &&
			   !wishCatalog.get(index).getWish().getName().equals(wishname))
			index++;
		
		if(index < wishCatalog.size())
			return wishCatalog.get(index).getWish().getID();
		else
			return -1;
	}
	
	//Returns detail required array list by searching for wish name. If wish not found
	//or if detail required array list is empty, returns null
	ArrayList<WishDetail> getWishDetail(int wishID)	
	{
		int index = 0;
		while(index < wishCatalog.size() && wishID != wishCatalog.get(index).getWish().getID())
			index++;
		
		if(index < wishCatalog.size() && wishCatalog.get(index).getWish().getNumberOfDetails() > 0)
		{
			ArrayList<WishDetail> wdAL = new ArrayList<WishDetail>();
			for(int i=0; i < 4; i++)
				if(wishCatalog.get(index).getWish().getWishDetailID(i) > -1)
					wdAL.add(wdDB.getWishDetail(wishCatalog.get(index).getWish().getWishDetailID(i)));
			
			return wdAL;
		}
		else
			return null;		
	}
	
	@Override
	String update(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_catwish>" + 
											gson.toJson(entity, ONCWish.class));
		
		if(response.startsWith("UPDATED_CATALOG_WISH"))
		{
			processUpdatedWish(source, response.substring(20));
		}
		
		return response;	
	}
	
	void processUpdatedWish(Object source, String json)
	{
		//Store added catalog wish in local wish catalog
		Gson gson = new Gson();
		ONCWish updatedWish = gson.fromJson(json, ONCWish.class);
		
		//find the updated wish in the data base and replace
		int index=0;
		while(index < wishCatalog.size() &&
			   wishCatalog.get(index).getWish().getID() != updatedWish.getID())
			index++;
		
		if(index < wishCatalog.size())
		{
			wishCatalog.get(index).setWish(updatedWish);
			//Notify local user IFs that an organization/partner was added
			fireDataChanged(source, "UPDATED_CATALOG_WISH", updatedWish);
		}
	}
	
	int add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response;
		
		response = serverIF.sendRequest("POST<add_catwish>" + 
											gson.toJson(entity, ONCWish.class));
		
		if(response != null && response.startsWith("ADDED_CATALOG_WISH"))
			return processAddedWish(source, response.substring(18));
		else
			return -1;	
	}
	
	int processAddedWish(Object source, String json)
	{
		//Store added catalog wish in local wish catalog
		Gson gson = new Gson();
		ONCWish addedWish = gson.fromJson(json, ONCWish.class);
		
		//add the wish in the proper spot alphabetically
		int index = 0;
		while(index < wishCatalog.size() &&
				(wishCatalog.get(index).getWish().getName().compareTo(addedWish.getName())) < 0)
			index++;
		
		if(index < wishCatalog.size())
			wishCatalog.add(index, new WishCatalogItem(addedWish));
		else
			wishCatalog.add(new WishCatalogItem(addedWish));
		
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "ADDED_CATALOG_WISH", addedWish);
		
		return index;
	}

	//setters to wish catalog
	void setWishName(int index, String name)
	{ 
		wishCatalog.get(index).getWish().setName(name); 
	}
	
	/*********************************************************************************
	 * This method is called when the user changes which wish list a wish appears in.
	 * An update request is formed and sent to the server.
	 * @param index
	 * @param wishnum
	 */
//	void toggleWishListIndex(int index, int wishnum )
//	{
//		ONCWish reqUpdateWish = new ONCWish(wishCatalog.get(index).getWish());	//copy current wish
//		int li = reqUpdateWish.getListindex(); //Get current list index	
//		int bitmask = 1 << wishnum;	//which wish is being toggled
//		
//		reqUpdateWish.setListindex(li ^ bitmask); //Set updated list index	
//	}
	
	
	//Delete a wish from catalog. Overloaded. Can delete by index or by wish
	String  delete(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_catwish>" + 
											gson.toJson(entity, ONCWish.class));
		
		if(response.startsWith("DELETED_CATALOG_WISH"))
			processDeletedWish(source, response.substring(20));
		
		return response;	
	}
	
	void processDeletedWish(Object source, String json)
	{
		//locate and remove deleted catalog wish in local wish catalog
		//delete the corresponding wish count
		Gson gson = new Gson();
		ONCWish deletedWish = gson.fromJson(json, ONCWish.class);
		
		int index = 0;
		while(index < wishCatalog.size() &&
				wishCatalog.get(index).getWish().getID() != deletedWish.getID())
			index++;
		
		if(index < wishCatalog.size())
			wishCatalog.remove(index);
		
		//Notify local user IFs that an organization/partner was added
		fireDataChanged(source, "DELETED_CATALOG_WISH", deletedWish);
	}
	
	
	/***************************************************************************************
	 * This method returns a subset of the names of wishes in the wish catalog based on the
	 * requested type and purpose of the list. Two purposes are supported: SORT lists puts
	 * an ONCWish "Any" in element 0 and ONCWish "None" in the returned list alphabetically.  
	 * SELECTION lists put an ONCWish "None" in element 0 of the list.
	 * 
	 * The binary representation of the ONCWish list index
	 * member variable to determines inclusion in a list. All wishes that have odd list indexes 
	 * are included in wishNumber 0 lists for example. All wishes with list indexes greater than 4
	 * are included in wishNubmer 2 list requests and all wishes with list indexes of 2, 3, 6,
	 * or 7 are included in wishNubmer 1 wish list requests.
	 * 
	 * An overloaded method that only takes a WishListType will return a list for that
	 * purpose that contains all ONCWish objects
	 * 
	 * @param listtype - Valid lists are 0 - 2.
	 * @param listpurpose Valid purposes are WishListType.Sort and WishListType.Select
	 * @return list of ONCWish objects in accordance with requested list type and purpose
	 ******************************************************************************************************/
	List<ONCWish> getWishList(WishListPurpose listPurpose)  { return getWishList(WISH_CATALOG_LIST_ALL, listPurpose); }
	List<ONCWish> getWishList(int wishNumber, WishListPurpose listPurpose)
	{
		List<ONCWish> wishlist = new ArrayList<ONCWish>();
	
		//Add catalog items to the list based on type of list requested
		int bitmask = WISH_CATALOG_LIST_ALL;	//include wish in all lists
		if(wishNumber < WISH_CATALOG_LIST_ALL)
			bitmask = 1 << wishNumber; 	//raise 2 to the listtype power
		
		for(ONCWish w:getCatalogWishList())
			if((w.getListindex() & bitmask) > 0)
				wishlist.add(w);
		
		//Add appropriate elements based on purpose. For selection lists, "None" must be at the
		//top of the list. For sort lists, "None" must be alphabetized in the list and "Any" 
		//must be at the top of the list
		if(listPurpose == WishListPurpose.Selection)
		{
			Collections.sort(wishlist, new WishListComparator());	//Alphabetize the list
			wishlist.add(0, new ONCWish(-1, "None", 7));
		}
		else if(listPurpose == WishListPurpose.Filtering)
		{	
			wishlist.add(new ONCWish(-1, "None", 7));	//Add "None" to the list
			Collections.sort(wishlist, new WishListComparator());	//Alphabetize the list
			wishlist.add(0, new ONCWish(-2, "Any", 7));
		}

		return  wishlist;
	}

	void initializeWishCounts()
	{	
		FamilyDB fDB = FamilyDB.getInstance();
		ArrayList<int[]> wishCounts = fDB.getWishBaseSelectedCounts(getCatalogWishList());
		for(int index=0; index < wishCounts.size(); index++)
			wishCatalog.get(index).setWishCounts(wishCounts.get(index));		
	}
	
	/***************************************************************************************************
	 * This method takes changes the wish counts associated with an ONCWish held in catalog
	 * The replaced wish and added wish are passed as parameters. The first object is 
	 * the ONCChildWish that has been replaced, the second object is the ONCChildWish that is
	 * it's replacement. 
	 * 
	 * The method locates the associated ONCWish in the catalog, decrementing the count for 
	 * the replaced ONCWish and incrementing the count for the add ONCWish. Wish counts have 
	 * component counts, one for each possible wish afforded an ONCCHild.
	 * 
	 * If either ONCChildWish object is null, the decrement  or increment count is not performed.
	 * @param WishBaseChange contains the replaced and added ONCChildWish objects
	 **************************************************************************************************/
	void changeWishCounts(ONCChildWish replWish, ONCChildWish addedWish)
	{
		boolean bWishCountChanged = false;
		
		if(replWish != null && replWish.getWishID() > -1)	//Search for from wish if it's not "None"
		{
			//Decrement the count of the first wish and update the table
			int row = findModelIndexFromID(replWish.getWishID());
			if(row > -1)
			{
				wishCatalog.get(row).incrementWishCount(replWish.getWishNumber(), -1);
				bWishCountChanged = true;
			}
		}
		
		if(addedWish != null && addedWish.getWishID() > -1)	//Search for second wish if it's not "None"
		{
			//Increment the count of the second wish and update the table
			int row = findModelIndexFromID(addedWish.getWishID());
			if(row > -1)
			{
				wishCatalog.get(row).incrementWishCount(addedWish.getWishNumber(), 1);
				bWishCountChanged = true;
			}
		}
		
		if(bWishCountChanged)
			fireDataChanged(this, "WISH_BASE_CHANGED", replWish, addedWish); //notify the UI's
		
	}
	
	int getTotalWishCount(int row) { return wishCatalog.get(row).getTotalWishCount(); }
	
	int getWishCount(int row, int wishnum)
	{
		if(!wishCatalog.isEmpty() && wishnum >=0 && wishnum < wishCatalog.size())
			return wishCatalog.get(row).getWishCountForWishNumber(wishnum);
		else
			return -1;
	}
	
	String importWishCatalogFromServer()
	{
		ArrayList<ONCWish> wishList = new ArrayList<ONCWish>();
		String response = "NO_CATALOG";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCWish>>(){}.getType();
			
			response = serverIF.sendRequest("GET<catalog>");
			wishList = gson.fromJson(response, listtype);
			Collections.sort(wishList, new WishListComparator());
			
			if(!response.startsWith("NO_CATALOG"))
			{		
				for(ONCWish wish: wishList)
					wishCatalog.add(new WishCatalogItem(wish));
				
				response =  "CATALOG_LOADED";
				fireDataChanged(this, "LOADED_CATALOG", null);
			}
		}
		
		return response;
	}
	
	String importWishCatalog(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "WishCatalog.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Wish Catalog .csv file to import");	
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
	    			if(header.length == ONC_WISH_CATALOG_HEADER_LENGTH)
	    			{
	    				wishCatalog.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					wishCatalog.add(new WishCatalogItem(new ONCWish(nextLine)));
	    			}
	    			else
	    			{
	    				
	    				JOptionPane.showMessageDialog(pf, "Wish catalog file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Wish Catalog File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    			}
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in wish catalog file: " + filename, 
						"Invalid Wish Catalog File", JOptionPane.ERROR_MESSAGE, oncIcon);
	    		
	    		reader.close();
	    		
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open wish catalog file: " + filename, 
    				"Wish Catalog file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	
	String exportToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Wish Catalog to",
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
	    		 String[] header = {"Wish ID", "Name", "List Index", "Wish Detail 1 ID", 
	    				 			"Wish Detail 2 ID", "Wish Detail 3 ID", "Wish Detail 4 ID"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(WishCatalogItem wci:wishCatalog)
	    	    	writer.writeNext(wci.getWish().getExportRow());	//Get family data
	    	 
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
/*	
	public void readWishCatalogALObject(ObjectInputStream ois)
	{
		ArrayList<ONCWish> wcAL = new ArrayList<ONCWish>();
		
		try 
		{
			wcAL = (ArrayList<ONCWish>) ois.readObject();
			
			wishCatalog.clear();
			long newWishID, newWishDetailID;
			for(ONCWish w:wcAL)
			{
				newWishID = addWish(w.getName(), w.getListindex());
				ONCWish newWish = getWish(newWishID);
				
				for(int index=0; index < w.getWishDetailAL().size(); index++)
				{
					WishDetail wd = w.getWishDetailAL().get(index);
					String name = wd.getWishDetailName();
					if(!name.isEmpty())
					{
						newWishDetailID = wdDB.addWishDetail(wd.getWishDetailName(), wd.getOldWishDetailChoices());
						newWish.setWishDetailID(index, newWishDetailID);
					}
				}
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
	
	public void writeWishCatalogObject(ObjectOutputStream oos)
	{
		try
		{
			oos.writeObject(wishCatalog);
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/	
	void clearCatalogData() { wishCatalog.clear(); }

	@Override
	public void dataChanged(ServerEvent ue)
	{

		if(ue.getType().equals("ADDED_CATALOG_WISH"))
		{
			processAddedWish(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_CATALOG_WISH"))
		{
			processUpdatedWish(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_CATALOG_WISH"))
		{
			processDeletedWish(this, ue.getJson());
		}
	}
	
	private class WishListComparator implements Comparator<ONCWish>
	{
		@Override
		public int compare(ONCWish w1, ONCWish w2)
		{
			return w1.getName().compareTo(w2.getName());
		}
	}
	
	/*********
	 * This class holds each item in the Wish Catalog. A catalog item consists of an ONCWish
	 * and a wish count. Each wish count has NUMBER_OF_WISHES_PER_CHILD component counts, 
	 * one count for each of the wishes for an ONCChild
	 */
	private class WishCatalogItem
	{
		private ONCWish wish;
		private int[] wishCount;
		
		WishCatalogItem(ONCWish wish)
		{
			this.wish = wish;
			wishCount = new int[NUMBER_OF_WISHES_PER_CHILD];
			for(int i=0; i<wishCount.length; i++)
				wishCount[i] = 0;
		}
		
		//getters
		ONCWish getWish() { return wish; }
		
		int getWishCountForWishNumber(int wishNumber) 
		{
			if(wishNumber >= 0 && wishNumber < wishCount.length)
				return wishCount[wishNumber];
			else
				return -1;
		}
		
		int getTotalWishCount()
		{
			int count = 0;
			for(int wn=0; wn< wishCount.length; wn++)
				count += wishCount[wn];
			
			return count;
		}
		
		//setters
		void setWish(ONCWish newWish) { wish = newWish; }
		
		
		//helper method to increment/decrement component wish counts
		int incrementWishCount(int wishNumber, int increment)
		{
			if(wishNumber >= 0 && wishNumber < wishCount.length)
				return wishCount[wishNumber] += increment;
			
			return wishCount[wishNumber];
		}
		
		//helper method used at client start up to initialize counts
		void setWishCounts(int[] wishCounts)
		{
			if(wishCounts.length == wishCount.length)
				for(int wn=0; wn < wishCount.length; wn++)
					wishCount[wn] = wishCounts[wn];
		}
	}
}
