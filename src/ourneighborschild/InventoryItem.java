package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem extends ONCObject
{
	private int count;
	private String number;
	private int nCommits;	//number of times the item has been committed from inventory
	private String itemname;
	private int wishID; 	//id of wish from ONC Wish Catalog
//	private String alias;
//	private String description;
//	private String avg_price;
//	private int rate_up;
//	private int rate_down;
/*	
	public InventoryItem(int id, UPCDatabaseItem item) 
	{
		super(id);
		this.count = 1;
		this.nCommits = 0;
		this.number = item.getNumber();
		this.itemname  = item.getItemName();
		this.wishID = -1;
		this.alias = item.getAlias();
		this.description = item.getDescription();
		this.avg_price = item.getAvgPrice();
		this.rate_up = item.getRateUp();
		this.rate_down = item.getRateDown();
	}
*/	
	public InventoryItem(int id, String number, String name) 
	{
		super(id);
		this.count = 1;
		this.nCommits = 0;
		this.number = number;
		this.itemname  = name;
		this.wishID = -1;
	}
	
	public InventoryItem(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.count = nextLine[1].isEmpty() ? 0 : Integer.parseInt(nextLine[1]);
		this.nCommits = nextLine[2].isEmpty() ? 0 : Integer.parseInt(nextLine[2]);
		this.number = nextLine[3].isEmpty() ? "" : nextLine[3];
		this.itemname  = nextLine[4].isEmpty() ? "" : nextLine[4];
		this.wishID = nextLine[5].isEmpty() ? 0 : Integer.parseInt(nextLine[5]);
	}
	
	public InventoryItem(InventoryItem ii)	//make a new object that is a copy
	{
		super(ii.id);
		this.count = ii.count;
		this.nCommits = ii.nCommits;
		this.number = ii.number;
		this.itemname  = ii.itemname;
		this.wishID = ii.wishID;
	}
	
	public InventoryItem(String name, int wishID, String barcode)
	{
		super(-1);	//server will add correct id
		this.count = 0;	//can add an item that has no stock on hand
		this.nCommits = 0;
		this.number = barcode;
		this.itemname = name;
		this.wishID = wishID;
	}
	
	//getters
	public int getCount() { return count; }
	int getNCommits() { return nCommits; }
	public String getNumber() { return number; }
	public String getItemName() { return itemname; }
	int getWishID() { return wishID; }
	
	//setters
	void setCount(int count) { this.count = count; }
	void setNCommits(int nCommits) { this.nCommits = nCommits; }
	public void setNumber(String number) { this.number = number; }
	void setItemName(String itemname) { this.itemname = itemname; }
	void setWishID(int wishID) { this.wishID = wishID; }
	
	//count change
	public int incrementCount(int amount)
	{ 
		//count cannot go below zero
		if(amount < 0 && count - amount < 0)
			return count;
		else
			return count += amount;
	}
	
	//count commits
	public int incrementCommits(int amount)
	{ 
		//commits cannot go below zero
		if(amount < 0 && nCommits - amount < 0)
			return nCommits;
		else
			return nCommits += amount; 
	}
	
	@Override
	public String[] getExportRow()
	{
		List<String> rowList = new ArrayList<String>();
		rowList.add(Integer.toString(getID()));
		rowList.add(Integer.toString(count));
		rowList.add(Integer.toString(nCommits));
		rowList.add(number);
		rowList.add(itemname);
		rowList.add(Integer.toString(wishID));
		
		return rowList.toArray(new String[rowList.size()]);
	}
	
	public String toString()
	{
		return String.format("id=%d, count=%d, nCommits = %d, name=%s, wishID = %d, barcode= %s,", 
				this.id, this.count, this.nCommits, this.itemname, this.wishID, this.number);
	}
}