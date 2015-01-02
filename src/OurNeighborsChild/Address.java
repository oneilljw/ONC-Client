package OurNeighborsChild;

public class Address 
{
	private String streetnum;
	private String streetname;
	private String unit;
	private String city;
	private String zip;
	
	public Address(String streetnum, String streetname, String unit, String city, String zip)
	{
		this.streetnum = streetnum;
		this.streetname = streetname;
		this.unit = unit;
		this.city = city;
		this.zip = zip;
	}
	
	public String getStreetnum() { return streetnum; }
	public String getStreetname() { return streetname; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipCode() { return zip; }
	
	public void setStreetnum(String s) { streetnum = s; }
	public void setStreetname(String s) { streetname = s; }
	public void setUnit(String s) { unit = s; }
	public void setCity(String s) { city = s; }
	public void setZipCode(String s) { zip = s; }
}
