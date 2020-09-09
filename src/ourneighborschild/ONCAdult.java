package ourneighborschild;

public class ONCAdult extends ONCObject
{
	private int	famid;
	private String name;
	private AdultGender gender;
	
	
	public ONCAdult() 
	{
		super(-1);
		this.famid = -1;
		this.name = "new adult";
		this.gender = AdultGender.Unknown;
	}
	
	public ONCAdult(ONCAdult a)	//make a copy 
	{
		super(a.id);
		this.famid = a.famid;
		this.name = a.name;
		this.gender = a.gender;
	}
	
	public ONCAdult(int id, int famid, String name, AdultGender gender) 
	{
		super(id);
		this.famid = famid;
		this.name = name;
		this.gender = gender;
	}
	
	public ONCAdult(ONCWebChild wc)	//move a web child to an adult
	{
		super(-1);
		this.famid = wc.getFamID();
		this.name = wc.getFirstName() + " " + wc.getLirstName();
		
		//if a child is a boy or girl, covert
		if(wc.getGender().toLowerCase().contentEquals("boy") || wc.getGender().toLowerCase().contentEquals("male"))
				this.gender = AdultGender.Male;
		else if(wc.getGender().toLowerCase().contentEquals("girl") || wc.getGender().toLowerCase().contentEquals("female"))
			this.gender = AdultGender.Female;
		else
			this.gender = AdultGender.Unknown;
	}
	
	//Constructor used when importing data base from CSV by the server
	public ONCAdult(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.famid = Integer.parseInt(nextLine[1]);
		this.name = nextLine[2].isEmpty() ? "" : nextLine[2];
		this.gender = nextLine[3].isEmpty() ? AdultGender.Unknown : AdultGender.valueOf(nextLine[3]);
	}
	
	//getters
	public int getFamID() { return famid; }
	public String getName() { return name; }
	public AdultGender getGender() { return gender; }
	
	//setters
	void setName(String name) { this.name = name; }
	void setGender(AdultGender gender) { this.gender = gender; }
	
	@Override
	public String[] getExportRow() 
	{
		String[] row= {Long.toString(id), Integer.toString(famid), name, gender.toString()};
		return row;
	}
}
