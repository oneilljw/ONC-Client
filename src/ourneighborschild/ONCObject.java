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
	
	public static long parseTimestamp(String input)
	{
		//determine if the input string is in scientific notation or not
		if(input.indexOf('E') > -1)
			return Long.parseLong(String.format("%.0f", Double.parseDouble(input)));
		else if(input.indexOf('.') > 1)
			return Long.parseLong(input.substring(0, input.indexOf('.')));
		else
			return Long.parseLong(input);
	}
}