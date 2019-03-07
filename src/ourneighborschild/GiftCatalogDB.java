package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVWriter;

public class GiftCatalogDB extends ONCDatabase
{
	/********
	 * This class implements a singleton data base for ONCGift objects, known as the Gift Catalog.
	 * The catalog is a list of gifts that users can choose from to fulfill the wish requests
	 * of served children. 
	 * 
	 * It's important to distinguish between ONCGift objects and ONCChildGift objects. An ONCChildGift
	 * object contains information about an ONCGift object after an ONCGift has been selected to 
	 * fulfill a child's wish. 
	 * 
	 * In addition to holding ONCGift objects, the catalog maintains a count of the number of
	 * times a gift has been selected to fulfill a wish. The count is broken into component
	 * counts - a separate count for each individual child gift. Each ONC child has more than 
	 * one wish request and fit. NUMBER_OF_GIFTS_PER_CHILD holds the number of gifts per child.
	 * 
	 * The catalog consists of a list of GiftCatalogItem objects. Each object contains an 
	 * ONCGift reference and the count for that gift, including component counts. Counts are
	 * not persistent. In the ONC Client application, counts are initialized once the catalog and
	 * family, child and child gift data is loaded from the server and maintained for the life
	 * of the client.
	 * 
	 * The catalog list is maintained in ONCGift name alphabetical order in order to support
	 * display using an AbstractTabelModel
	 * 
	 * The catalog provides a method that returns a list of ONCGifts that can be 
	 * used for sorting or selecting a gift. 
	 */
	private static final int GIFT_CATALOG_LIST_ALL = 7;
	private static final int NUMBER_OF_GIFTS_PER_CHILD = 3;
	
	private static GiftCatalogDB instance = null;
	private ArrayList<GiftCatalogItem> giftCatalog;
	private GiftDetailDB  giftDetailDB;
	
	private GiftCatalogDB()
	{
		super();
		giftDetailDB = GiftDetailDB.getInstance();
		giftCatalog = new ArrayList<GiftCatalogItem>();
	}
	
	public static GiftCatalogDB getInstance()
	{
		if(instance == null)
			instance = new GiftCatalogDB();
		
		return instance;
	}
	
	//getters from catalog
	ONCGift getGift(int index) { return giftCatalog.get(index).getGift(); }
	ArrayList<ONCGift> getCatalogGiftList()
	{
		ArrayList<ONCGift> giftList = new ArrayList<ONCGift>();
		for(GiftCatalogItem gci: giftCatalog)
			giftList.add(gci.getGift());
		
		return giftList;
	}
	
	//find gift object by giftID. Return ONCGift object or null if id not found
	public ONCGift getGiftByID(int giftID)
	{
		int index = 0;
		while(index < giftCatalog.size() && giftCatalog.get(index).getGift().getID() != giftID)
			index++;
		
		if(index < giftCatalog.size())
			return giftCatalog.get(index).getGift();
		else
			return null;
	}
	
	int findModelIndexFromID(int giftID)
	{
		int index = 0;
		while(index < giftCatalog.size() && giftCatalog.get(index).getGift().getID() != giftID)
			index++;
		
		if(index < giftCatalog.size())
			return index;
		else
			return -1;
	}
	
	int size() { return giftCatalog.size(); }
	int getGiftID(int index) { return giftCatalog.get(index).getGift().getID(); }
	String getGiftName(int index) { return giftCatalog.get(index).getGift().getName(); }
	
	boolean isInGiftList(int index, int giftnum)
	{
		int mask = 1;
		mask = mask << giftnum;
		return (giftCatalog.get(index).getGift().getListindex() & mask) > 0;
	}
	
	boolean isDetailRqrd(int index) 
	{ 
		return giftCatalog.get(index).getGift().isDetailRqrd(); 
	}
	
	int getGiftID(String giftName)
	{
		int index=0;
		while(index < giftCatalog.size() &&
			   !giftCatalog.get(index).getGift().getName().equals(giftName))
			index++;
		
		if(index < giftCatalog.size())
			return giftCatalog.get(index).getGift().getID();
		else
			return -1;
	}
	
	//Returns detail required array list by searching for gift name. If gift not found
	//or if detail required array list is empty, returns null
	ArrayList<GiftDetail> getGiftDetail(int giftID)	
	{
		int index = 0;
		while(index < giftCatalog.size() && giftID != giftCatalog.get(index).getGift().getID())
			index++;
		
		if(index < giftCatalog.size() && giftCatalog.get(index).getGift().getNumberOfDetails() > 0)
		{
			ArrayList<GiftDetail> wdAL = new ArrayList<GiftDetail>();
			for(int i=0; i < 4; i++)
				if(giftCatalog.get(index).getGift().getGiftDetailID(i) > -1)
					wdAL.add(giftDetailDB.getWishDetail(giftCatalog.get(index).getGift().getGiftDetailID(i)));
			
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
											gson.toJson(entity, ONCGift.class));
		
		if(response.startsWith("UPDATED_CATALOG_WISH"))
		{
			processUpdatedGift(source, response.substring(20));
		}
		
		return response;	
	}
	
	void processUpdatedGift(Object source, String json)
	{
		//Store added catalog gift in local catalog
		Gson gson = new Gson();
		ONCGift updatedGift = gson.fromJson(json, ONCGift.class);
		
		//find the updated gift in the data base and replace
		int index=0;
		while(index < giftCatalog.size() &&
			   giftCatalog.get(index).getGift().getID() != updatedGift.getID())
			index++;
		
		if(index < giftCatalog.size())
		{
			giftCatalog.get(index).setGift(updatedGift);
			//Notify local user IFs that gift was updated in the catalog
			fireDataChanged(source, "UPDATED_CATALOG_WISH", updatedGift);
		}
	}
	
	int add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response;
		
		response = serverIF.sendRequest("POST<add_catwish>" + 
											gson.toJson(entity, ONCGift.class));
		
		if(response != null && response.startsWith("ADDED_CATALOG_WISH"))
			return processAddedGift(source, response.substring(18));
		else
			return -1;	
	}
	
	int processAddedGift(Object source, String json)
	{
		//Store added catalog gift in local catalog
		Gson gson = new Gson();
		ONCGift addedGift = gson.fromJson(json, ONCGift.class);
		
		//add the gift in the proper spot alphabetically
		int index = 0;
		while(index < giftCatalog.size() &&
				(giftCatalog.get(index).getGift().getName().compareTo(addedGift.getName())) < 0)
			index++;
		
		if(index < giftCatalog.size())
			giftCatalog.add(index, new GiftCatalogItem(addedGift));
		else
			giftCatalog.add(new GiftCatalogItem(addedGift));

		//Notify local user IFs that gift was added to the catalog
		fireDataChanged(source, "ADDED_CATALOG_WISH", addedGift);
		
		return index;
	}

	//setters to catalog
	void setGiftName(int index, String name) { giftCatalog.get(index).getGift().setName(name); }
	
	/*********************************************************************************
	 * This method is called when the user changes which gift list a gift appears in.
	 * An update request is formed and sent to the server.
	 * @param index
	 * @param giftnum
	 */

	//Delete a gift from catalog. Overloaded. Can delete by index or by gift
	String  delete(Object source, ONCObject entity) 
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<delete_catwish>" + 
											gson.toJson(entity, ONCGift.class));
		
		if(response.startsWith("DELETED_CATALOG_WISH"))
			processDeletedGift(source, response.substring(20));
		
		return response;	
	}
	
	void processDeletedGift(Object source, String json)
	{
		//locate and remove deleted catalog gift in local catalog
		//delete the corresponding gift count
		Gson gson = new Gson();
		ONCGift deletedGift = gson.fromJson(json, ONCGift.class);
		
		int index = 0;
		while(index < giftCatalog.size() &&
				giftCatalog.get(index).getGift().getID() != deletedGift.getID())
			index++;
		
		if(index < giftCatalog.size())
			giftCatalog.remove(index);
		
		//Notify local user IFs that a gift was deleted from the catalog
		fireDataChanged(source, "DELETED_CATALOG_WISH", deletedGift);
	}
	
	
	/***************************************************************************************
	 * This method returns a subset of the names of gifts in the catalog based on the
	 * requested type and purpose of the list. Two purposes are supported: SORT lists puts
	 * an ONCGift "Any" in element 0 and ONCGift "None" in the returned list alphabetically.  
	 * SELECTION lists put an ONCGift "None" in element 0 of the list.
	 * 
	 * The binary representation of the ONCGift list index
	 * member variable to determines inclusion in a list. All gifts that have odd list indexes 
	 * are included in giftNumber 0 lists for example. All gifts with list indexes greater than 4
	 * are included in giftNumber 2 list requests and all gifts with list indexes of 2, 3, 6,
	 * or 7 are included in giftNumber 1 gift list requests.
	 * 
	 * An overloaded method that only takes a GiftListType will return a list for that
	 * purpose that contains all ONCGift objects
	 * 
	 * @param listtype - Valid lists are 0 - 2.
	 * @param listpurpose Valid purposes are GiftListType.Sort and GiftListType.Select
	 * @return list of ONCGift objects in accordance with requested list type and purpose
	 ******************************************************************************************************/
	List<ONCGift> getGiftList(GiftListPurpose listPurpose)  { return getGiftList(GIFT_CATALOG_LIST_ALL, listPurpose); }
	List<ONCGift> getGiftList(int giftNumber, GiftListPurpose listPurpose)
	{
		List<ONCGift> giftlist = new ArrayList<ONCGift>();
	
		//Add catalog items to the list based on type of list requested
		int bitmask = GIFT_CATALOG_LIST_ALL;	//include gift in all lists
		if(giftNumber < GIFT_CATALOG_LIST_ALL)
			bitmask = 1 << giftNumber; 	//raise 2 to the listtype power
		
		for(ONCGift g:getCatalogGiftList())
			if((g.getListindex() & bitmask) > 0)
				giftlist.add(g);
		
		//Add appropriate elements based on purpose. For selection lists, "None" must be at the
		//top of the list. For sort lists, "None" must be alphabetized in the list and "Any" 
		//must be at the top of the list
		if(listPurpose == GiftListPurpose.Selection)
		{
			Collections.sort(giftlist, new GiftListComparator());	//Alphabetize the list
			giftlist.add(0, new ONCGift(-1, "None", 7));
		}
		else if(listPurpose == GiftListPurpose.Filtering)
		{	
			giftlist.add(new ONCGift(-1, "None", 7));	//Add "None" to the list
			Collections.sort(giftlist, new GiftListComparator());	//Alphabetize the list
			giftlist.add(0, new ONCGift(-2, "Any", 7));
		}

		return  giftlist;
	}
	List<ONCGift> getDefaultGiftList()
	{
		List<ONCGift> giftlist = new ArrayList<ONCGift>();
	
		for(ONCGift g : getCatalogGiftList())
			if(g.getListindex() == GIFT_CATALOG_LIST_ALL)
				giftlist.add(g);
		
		Collections.sort(giftlist, new GiftListComparator());	//Alphabetize the list
		giftlist.add(0, new ONCGift(-1, "None", GIFT_CATALOG_LIST_ALL));

		return  giftlist;
	}

	void initializeCounts()
	{	
		FamilyDB fDB = FamilyDB.getInstance();
		ArrayList<int[]> giftCounts = fDB.getWishBaseSelectedCounts(getCatalogGiftList());
		for(int index=0; index < giftCounts.size(); index++)
			giftCatalog.get(index).setGiftCounts(giftCounts.get(index));		
	}
	
	/***************************************************************************************************
	 * This method takes changes the gift counts associated with an ONCGift held in catalog
	 * The replaced gift and added gift are passed as parameters. The first object is 
	 * the ONCChildGift that has been replaced, the second object is the ONCChildGift that is
	 * it's replacement. 
	 * 
	 * The method locates the associated ONCGift in the catalog, decrementing the count for 
	 * the replaced ONCGift and incrementing the count for the add ONCGift. Counts have 
	 * component counts, one for each possible gift afforded an ONCCHild.
	 * 
	 * If either ONCChildGift object is null, the decrement  or increment count is not performed.
	 * @param GiftBaseChange contains the replaced and added ONCChildWish objects
	 **************************************************************************************************/
	void changeGiftCounts(ONCChildGift replGift, ONCChildGift addedGift)
	{
		boolean bCountChanged = false;
		
		if(replGift != null && replGift.getGiftID() > -1)	//Search for replaced gift if it's not "None"
		{
			//Decrement the count of the first gift and update the table
			int row = findModelIndexFromID(replGift.getGiftID());
			if(row > -1)
			{
				giftCatalog.get(row).incrementCount(replGift.getGiftNumber(), -1);
				bCountChanged = true;
			}
		}
		
		if(addedGift != null && addedGift.getGiftID() > -1)	//Search for second gift if it's not "None"
		{
			//Increment the count of the second gift and update the table
			int row = findModelIndexFromID(addedGift.getGiftID());
			if(row > -1)
			{
				giftCatalog.get(row).incrementCount(addedGift.getGiftNumber(), 1);
				bCountChanged = true;
			}
		}
		
		if(bCountChanged)
			fireDataChanged(this, "WISH_BASE_CHANGED", replGift, addedGift); //notify the UI's
		
	}
	
	int getTotalGiftCount(int row) { return giftCatalog.get(row).getTotalGiftCount(); }
	
	int getGiftCount(int row, int giftnum)
	{
		if(!giftCatalog.isEmpty() && giftnum >=0 && giftnum < giftCatalog.size())
			return giftCatalog.get(row).getCountForGiftNumber(giftnum);
		else
			return -1;
	}
	
	String importCatalogFromServer()
	{
		ArrayList<ONCGift> giftList = new ArrayList<ONCGift>();
		String response = "NO_CATALOG";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCGift>>(){}.getType();
			
			response = serverIF.sendRequest("GET<catalog>");
			giftList = gson.fromJson(response, listtype);
			Collections.sort(giftList, new GiftListComparator());
			
			if(!response.startsWith("NO_CATALOG"))
			{		
				for(ONCGift g: giftList)
					giftCatalog.add(new GiftCatalogItem(g));
				
				response =  "CATALOG_LOADED";
				fireDataChanged(this, "LOADED_CATALOG", null);
			}
		}
		
		return response;
	}
	
	String exportToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
		if(filename == null)
		{
    			ONCFileChooser fc = new ONCFileChooser(pf);
    			oncwritefile= fc.getFile("Select .csv file to save Wish Catalog to",
								new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
	    	    
    				for(GiftCatalogItem wci:giftCatalog)
    					writer.writeNext(wci.getGift().getExportRow());	//Get family data
	    	 
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

	void clearCatalogData() { giftCatalog.clear(); }

	@Override
	public void dataChanged(ServerEvent ue)
	{

		if(ue.getType().equals("ADDED_CATALOG_WISH"))
		{
			processAddedGift(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_CATALOG_WISH"))
		{
			processUpdatedGift(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_CATALOG_WISH"))
		{
			processDeletedGift(this, ue.getJson());
		}
	}
	
	private class GiftListComparator implements Comparator<ONCGift>
	{
		@Override
		public int compare(ONCGift g1, ONCGift g2)
		{
			return g1.getName().compareTo(g2.getName());
		}
	}
	
	/*********
	 * This class holds each item in the Gift Catalog. A catalog item consists of an ONCGift
	 * and a count. Each count has NUMBER_OF_GIFTS_PER_CHILD component counts, 
	 * one count for each of the gifts for an ONCChild
	 */
	private class GiftCatalogItem
	{
		private ONCGift gift;
		private int[] giftCount;
		
		GiftCatalogItem(ONCGift gift)
		{
			this.gift = gift;
			giftCount = new int[NUMBER_OF_GIFTS_PER_CHILD];
			for(int i=0; i<giftCount.length; i++)
				giftCount[i] = 0;
		}
		
		//getters
		ONCGift getGift() { return gift; }
		
		int getCountForGiftNumber(int gn) 
		{
			if(gn >= 0 && gn < giftCount.length)
				return giftCount[gn];
			else
				return -1;
		}
		
		int getTotalGiftCount()
		{
			int count = 0;
			for(int gn=0; gn< giftCount.length; gn++)
				count += giftCount[gn];
			
			return count;
		}
		
		//setters
		void setGift(ONCGift newGift) { gift = newGift; }
		
		
		//helper method to increment/decrement component counts
		int incrementCount(int giftNumber, int increment)
		{
			if(giftNumber >= 0 && giftNumber < giftCount.length)
				return giftCount[giftNumber] += increment;
			
			return giftCount[giftNumber];
		}
		
		//helper method used at client start up to initialize counts
		void setGiftCounts(int[] giftCounts)
		{
			if(giftCounts.length == giftCount.length)
				for(int wn=0; wn < giftCount.length; wn++)
					giftCount[wn] = giftCounts[wn];
		}
	}
}
