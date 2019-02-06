package ourneighborschild;

public class DBYear extends ONCObject
{
	private boolean bLocked;
	
	public DBYear(int year, boolean bLocked)
	{
		super(year);	 //uses ONCObjects id member variable as the season.
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
	public String[] getExportRow() 
	{
		String[] row = new String[2];
		row[0] = Integer.toString(id);
		row[1] = bLocked ? "yes" : "no";
		return row;
	}
}
