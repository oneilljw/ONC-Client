package ourneighborschild;

public enum Barcode 
{ 
	UPCE(8), CODE128(7);
	
	private final int length;
	
	Barcode(int length)
	{
		this.length = length;
	}
	
	public int length() { return length; }
}
