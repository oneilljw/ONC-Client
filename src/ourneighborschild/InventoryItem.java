package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public class InventoryItem extends ONCObject
{
	private int count;
	private String number;
	private String itemname;
	private String alias;
	private String description;
	private String avg_price;
	private int rate_up;
	private int rate_down;
	
	public InventoryItem(int id, UPCDatabaseItem item) 
	{
		super(id);
		this.count = 1;
		this.number = item.getNumber();
		this.itemname  = item.getItemName();
		this.alias = item.getAlias();
		this.description = item.getDescription();
		this.avg_price = item.getAvgPrice();
		this.rate_up = item.getRateUp();
		this.rate_down = item.getRateDown();
	}
	
	public InventoryItem(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.count = nextLine[1].isEmpty() ? 0 : Integer.parseInt(nextLine[1]);
		this.number = nextLine[2].isEmpty() ? "" : nextLine[2];
		this.itemname  = nextLine[3].isEmpty() ? "" : nextLine[3];
		this.alias = nextLine[4].isEmpty() ? "" : nextLine[4];
		this.description = nextLine[5].isEmpty() ? "" : nextLine[5];
		this.avg_price = nextLine[6].isEmpty() ? "" : nextLine[6];
		this.rate_up = nextLine[7].isEmpty() ? 0 : Integer.parseInt(nextLine[7]);
		this.rate_down = nextLine[8].isEmpty() ? 0 : Integer.parseInt(nextLine[8]);
	}
	
	//getters
	int getCount() { return count; }
	public String getNumber() { return number; }
	public String getItemName() { return itemname; }
	String getAlias() { return alias; }
	String getDescription() { return description; }
	String getAvgPrice() { return avg_price; }
	int getRateUp() { return rate_up; }
	int getRateDown() { return rate_down; }
	
	//setters
	void setCount(int count) { this.count = count; }
	void setNumber(String number) { this.number = number; }
	void setItemName(String itemname) { this.itemname = itemname; }
	void setAlias(String alias) { this.alias = alias; }
	void setDescription(String description) { this.description = description; }
	void setAvgPrice(String avg_price) { this.avg_price = avg_price; }
	void setRateUp(int rate_up) { this.rate_up = rate_up; }
	void setRateDown(int rate_down) { this.rate_down = rate_down; }
	
	//count change
	public int incrementCount(int amount) { return count += amount; }
	
	@Override
	public String[] getExportRow()
	{
		List<String> rowList = new ArrayList<String>();
		rowList.add(Integer.toString(getID()));
		rowList.add(Integer.toString(count));		
		rowList.add(number);
		rowList.add(itemname);
		rowList.add(alias);	
		rowList.add(description);
		rowList.add(avg_price);
		rowList.add(Integer.toString(rate_up));
		rowList.add(Integer.toString(rate_down));
		
		return rowList.toArray(new String[rowList.size()]);
	}
}