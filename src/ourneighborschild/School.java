package ourneighborschild;

public class School
{
	private String code;
	private Address address;
	private String name;
	private String latlong;
	
	public School(String code, Address address, String name, String latlong)
	{
		this.code = code;
		this.address = address;
		this.name = name;
		this.latlong = latlong;
	}
	
	public School(String[] nextLine)
	{
		this.code = nextLine[0];
		this.address = new Address(nextLine[1], nextLine[2], nextLine[3], nextLine[4], nextLine[5], 
								  nextLine[6], nextLine[7], nextLine[8], nextLine[9]);
		this.name = nextLine[10];
		this.latlong = nextLine[11];
	}
	
	//getters
	public String getCode() { return code; }
	public Address getAddress() { return address; }
	public String getName() { return name; }
	public String getLatLong() { return latlong; }
	
	boolean matches(School school)
	{
		return code.equals(school.getCode());
	}
	
	public String[] getExportRow()
	{
		String[] row = new String[12];
		
		row[0] = code;
		row[1] = address.getStreetNum();
		row[2] = address.getStreetNumSuffix();
		row[3] = address.getStreetDir();
		row[4] = address.getStreetName();
		row[5] = address.getStreetType();
		row[6] = address.getStreetPostDir();
		row[7] = address.getUnit();
		row[8] = address.getCity();
		row[9] = address.getZipCode();
		row[10] = name;
		row[11] = latlong;
		
		return row;
	}
	
	@Override
	public String toString() { return name; }
}
