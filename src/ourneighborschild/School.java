package ourneighborschild;

public class School extends ONCObject
{
	private String code;
	private Address address;
	private String name;
	private String latlong;
	private String pyramid;
	private SchoolType type;
	
	
	public School(int id, String code, Address address, String name, String latlong, String pyramid, SchoolType type)
	{
		super(id);
		this.code = code;
		this.address = address;
		this.name = name;
		this.latlong = latlong;
		this.pyramid = pyramid;
		this.type = type;
	}
	
	public School(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.code = nextLine[1];
		this.address = new Address(nextLine[2], nextLine[3], nextLine[4], nextLine[5], nextLine[6], 
								  nextLine[7], nextLine[8], nextLine[9], nextLine[10]);
		this.name = nextLine[11];
		this.latlong = nextLine[12];
		this.pyramid = nextLine[13];
		this.type = SchoolType.valueOf(nextLine[14]);
	}
	
	public School()
	{
		super(-1);
		this.code = "Any";
		this.address = null;
		this.name = "Any";
		this.latlong = "";
		this.pyramid = "";
		this.type = SchoolType.ES;
	}
	
	//getters
	public String getCode() { return code; }
	public Address getAddress() { return address; }
	public String getName() { return name; }
	public String getLatLong() { return latlong; }
	public String getPyramid() { return pyramid; }
	public SchoolType getType() { return type; }
	
	boolean matches(School school)
	{
		return school != null && code.equals(school.getCode());
	}
	
	public String[] getExportRow()
	{
		String[] row = new String[15];
		
		row[0] = Integer.toString(id);
		row[1] = code;
		row[2] = address.getStreetNum();
		row[3] = address.getStreetNumSuffix();
		row[4] = address.getStreetDir();
		row[5] = address.getStreetName();
		row[6] = address.getStreetType();
		row[7] = address.getStreetPostDir();
		row[8] = address.getUnit();
		row[9] = address.getCity();
		row[10] = address.getZipCode();
		row[11] = name;
		row[12] = latlong;
		row[13] = pyramid;
		row[14] = type.toString();
		
		return row;
	}
	
	@Override
	public String toString() { return name; }
}
