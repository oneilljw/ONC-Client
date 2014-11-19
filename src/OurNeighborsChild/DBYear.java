package OurNeighborsChild;

public class DBYear
{
	private int year;
	private boolean bLocked;
	
	public DBYear(int year, boolean bLocked)
	{
		this.year = year;
		this.bLocked = bLocked;
	}
	
	//getters
	public int getYear() { return year; }
	public boolean isLocked() { return bLocked; }
	
	//setters
	void setLock(boolean tf) { bLocked = tf; }
}
