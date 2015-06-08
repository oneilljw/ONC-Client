package ourneighborschild;

public class DataChange 
{
	private int olddata;
	private int newdata;
	
	public DataChange(int olddata, int newdata)
	{
		this.olddata = olddata;
		this.newdata = newdata;
	}
	
	//getters
	public int getOldData() { return olddata; }
	public int getNewData() { return newdata; }
}
