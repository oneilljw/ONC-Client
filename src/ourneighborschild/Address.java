package ourneighborschild;

public class Address 
{
	private String streetNum;
	private String streetDir;
	private String streetName;
	private String streetType;
	private String streetPostDir;
	private String unit;
	private String city;
	private String zip;
	private String region;
	
	public Address(String streetnum, String streetname, String unit, String city, String zip)
	{
		this.streetNum = streetnum;
		this.streetName = streetname;
		this.unit = unit;
		this.city = city;
		this.zip = zip;
	}
	
	public Address(String[] parts)
	{
		if(parts.length == 9)
		{
			this.streetNum = parts[0];
			this.streetDir = parts[1];
			this.streetName = parts[2];
			this.streetType = parts[3];
			this.streetPostDir = parts[4];
			this.unit = parts[5];
			this.city = parts[6];
			this.zip = parts[7];
			this.region = parts[8];
		}
		else
		{
			
		}
	}
	
	public String getStreetNum() { return streetNum; }
	public String getStreetDir() { return streetDir; }
	public String getStreetName() { return streetName; }
	public String getStreetType() { return streetType; }
	public String getStreetPostDir() { return streetPostDir; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipCode() { return zip; }
	public String getRegion() { return region; }
	
	
	public void setStreetNum(String s) { streetNum = s; }
	public void setStreetDir(String s) { streetDir = s; }
	public void setStreetName(String s) { streetName = s; }
	public void setStreetType(String s) { streetType = s; }
	public void setStreetPostDir(String s) { streetPostDir = s; }
	public void setUnit(String s) { unit = s; }
	public void setCity(String s) { city = s; }
	public void setZipCode(String s) { zip = s; }
	public void setRegion(String s) { region = s; }
}
