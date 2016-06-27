package ourneighborschild;

public class InventoryRequest 
{
	private String barcode;
	private int count;
	private int commits;
	
	InventoryRequest(String barcode, int count, int commits)
	{
		this.barcode = barcode;
		this.count = count;
		this.commits = commits;
	}
	
	//getters
	public String getBarcode() { return barcode; }
	public int getCount() { return count; }
	public int getCommits() { return commits; }
	
	//setters
	void setBarcode(String barcode) { this.barcode = barcode; }
	void setCount(int count) { this.count = count; }
	void setCommits(int commits) { this.commits = commits; }
}
