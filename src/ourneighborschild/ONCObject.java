package ourneighborschild;

public abstract class ONCObject
{
	protected int id;
	
	public ONCObject(int id)
	{
		this.id = id;
	}
	
	public int getID() { return id; }
	public void setID(int id) { this.id = id; }
	
	public abstract String[] getExportRow();
	
	public boolean matches(ONCObject other) { return other != null && other.id == id; }
	
	public static boolean isNumeric(String s){ return s.matches("-?\\d+(\\.\\d+)?"); }
}