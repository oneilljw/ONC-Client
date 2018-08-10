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
	
	//getters
	public String getCode() { return code; }
	public Address getAddress() { return address; }
	public String getName() { return name; }
	public String getLatLong() { return latlong; }
	
	@Override
	public String toString() { return name; }
}
