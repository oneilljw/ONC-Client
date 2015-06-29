package ourneighborschild;

public class ONCAdult extends ONCObject
{
	private int	famid;
	private String name;
	private String gender;
	
	
	public ONCAdult() 
	{
		super(-1);
		this.famid = -1;
		this.name = "new adult";
		this.gender = "";
	}
	
	public ONCAdult(int id, int famid, String name, String gender) 
	{
		super(id);
		this.famid = famid;
		this.name = name;
		this.gender = gender;
	}
	
	//Constructor used when importing data base from CSV by the server
	public ONCAdult(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.famid = Integer.parseInt(nextLine[1]);
		this.name = nextLine[2].isEmpty() ? "" : nextLine[2];
		this.gender = nextLine[3].isEmpty() ? "" : nextLine[3];
	}
	
	//getters
	String getName() { return name; }
	String getGender() { return gender; }
	
	//setters
	void setName(String name) { this.name = name; }
	void setGender(String gender) {this.gender = gender; }
	
	@Override
	public String[] getExportRow() 
	{
		String[] row= {Long.toString(id), Integer.toString(famid), name, gender};
		return row;
	}
}
