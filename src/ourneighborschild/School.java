package ourneighborschild;

public class School
{
	private String code;
	private Address address;
	private String name;
	
	public School(String code, Address address, String name)
	{
		this.code = code;
		this.address = address;
		this.name = name;
	}
	
	//getters
	public String getCode() { return code; }
	public Address getAddress() { return address; }
	public String getName() { return name; }
	
	@Override
	public String toString() { return name; }
}
