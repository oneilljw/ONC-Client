package OurNeighborsChild;

public abstract class ONCObject
{
	protected int id;
	
	public ONCObject(int id)
	{
		this.id = id;
	}
	
	public int getID() { return id; }
	public void setID(int id) { this.id = id; }
	
	public boolean matches(ONCObject other) { return other != null && other.id == id; } 
	
	abstract public String[] getExportRow();
}
