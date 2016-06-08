package ourneighborschild;

public class UPCDatabaseItem
{
	private String valid;
	private String number;
	private String itemname;
	private String alias;
	private String description;
	private String avg_price;
	private int rate_up;
	private int rate_down;
	
	//getters
	String getValid() { return valid; }
	String getNumber() { return number; }
	String getItemName() { return itemname; }
	String getAlias() { return alias; }
	String getDescription() { return description; }
	String getAvgPrice() { return avg_price; }
	int getRateUp() { return rate_up; }
	int getRateDown() { return rate_down; }
	
	//setters
	void setValid(String valid) { this.valid = valid; }
	void setNumber(String number) { this.number = number; }
	void setItemName(String itemname) { this.itemname = itemname; }
	void setAlias(String alias) { this.alias = alias; }
	void setDescription(String description) { this.description = description; }
	void setAvgPrice(String avg_price) { this.avg_price = avg_price; }
	void setRateUp(int rate_up) { this.rate_up = rate_up; }
	void setRateDown(int rate_down) { this.rate_down = rate_down; }
}
