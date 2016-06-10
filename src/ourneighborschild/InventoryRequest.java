package ourneighborschild;

public class InventoryRequest 
{
	private String barcode;
	private int count;
	
	InventoryRequest(String barcode, int count)
	{
		this.barcode = barcode;
		this.count = count;
	}
	
	//getters
	public String getBarcode() { return barcode; }
	public int getCount() { return count; }
	
	//setters
	void setBarcode(String barcode) { this.barcode = barcode; }
	void setCount(int count) { this.count = count; }
}
