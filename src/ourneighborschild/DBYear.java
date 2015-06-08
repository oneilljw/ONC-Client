package ourneighborschild;

public class DBYear extends ONCObject
{
	private boolean bLocked;
	
	public DBYear(int year, boolean bLocked)
	{
		super(year);
		this.bLocked = bLocked;
	}
	
	//constructor used to make a copy
	DBYear(DBYear dbYear)
	{
		super(dbYear.getYear());
		this.bLocked = dbYear.bLocked;
	}
	
	//getters
	public int getYear() { return id; }
	public boolean isLocked() { return bLocked; }
	public DBYear getDBYear() { return this; }
	
	//setters
	public void setLock(boolean tf) { bLocked = tf; }
	
	@Override
	public String toString() { return Integer.toString(id); }

	@Override
	public String[] getExportRow() {
		// TODO Auto-generated method stub
		return null;
	}
}
