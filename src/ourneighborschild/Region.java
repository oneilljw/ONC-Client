package ourneighborschild;

public class Region extends ONCObject
{
	/**
	 * 
	 */
	private String streetName;
	private String streetType;
	private String region;
	private String schoolRegion;
	private String school;
	private String location;
	private String streetDir;
	private String postDir;
	private int addressNumLow;
	private int addressNumHi;
//	private String odd_even;
	private String zipCode;
//	private String precinct;
//	private String townPrecinct;
//	private String congDistrict;
//	private String stateSenateDistrict;
//	private String stateHouseDistrict;
	
	public Region(String[] col)
	{
		super(isNumeric(col[0]) ? Integer.parseInt(col[0]) : -1);
		streetName = col[1];
		streetType = col[2];
		region = col[3];
		schoolRegion = col[4];
		school = col[5];
		location = col[6];
		streetDir = col[7];
		postDir = col[8];
		addressNumLow = isNumeric(col[9]) ? Integer.parseInt(col[9]) : 0;
		addressNumHi = isNumeric(col[10]) ? Integer.parseInt(col[10]) : 0;;
//		odd_even = file_line[11];
		zipCode = col[12];
//		precinct = file_line[13];
//		townPrecinct = file_line[14];
//		congDistrict = file_line[15];
//		stateSenateDistrict = file_line[16];
//		stateHouseDistrict = file_line[17];
	}
	
	/*******************************************************************************************
	 * A region is a match if the street name and street direction match and the street number
	 * is within the low to high range. A four part string array is passed. Element 0 is the 
	 * street number, element 1 is the street direction, element 2 is the street name and element
	 * 3 is the street suffix. The suffix is currently not used in the search
	 ********************************************************************************************/
	public boolean isRegionMatch(String[] address)
	{	
		if(isNumeric(address[0]))
		{
			int stnum = Integer.parseInt(address[0]);
			return address[2].toLowerCase().equals(streetName.toLowerCase()) &&
					stnum >= addressNumLow && stnum <= addressNumHi &&
					 address[1].equals(streetDir) &&
					  address[4].equals(zipCode);
		}
		else
			return false;
	}
	
	public boolean isRegionMatch(Address address)
	{	
		if(isNumeric(address.getStreetNum()))
		{
			int stnum = Integer.parseInt(address.getStreetNum());
			return address.getStreetName().toLowerCase().equals(streetName.toLowerCase()) &&
					stnum >= addressNumLow && stnum <= addressNumHi &&
					 address.getStreetDir().equals(streetDir) &&
					  address.getZipCode().equals(zipCode);
		}
		else
			return false;
	}
	
	public boolean isRegionMatch(Region r)
	{
		return r != null && this.id == r.id && this.streetName.toLowerCase().equals(r.streetName.toLowerCase()) &&
				this.streetDir.equals(r.streetDir) && this.postDir.equals(r.postDir) &&
				this.zipCode.equals(r.zipCode) && this.addressNumLow == r.addressNumLow &&
				this.addressNumHi == r.addressNumHi && this.region.equals(r.region);
	}
	
	//getters
	public String getStreetName() { return streetName; }
	public String getStreetType() { return streetType; }
	public String getStreetDir() { return streetDir; }
	public String getRegion() { return region; }
	public String getSchoolRegion() { return schoolRegion; }
	public String getSchool() { return school; }
	public String getLocation() { return location; }
	public int getAddressNumLow() { return addressNumLow; }
	public int getAddressNumHi() { return addressNumHi; }
	public String getZipCode() { return zipCode; }
	
	public String getPrintalbeRegion()
	{
		StringBuffer buff = new StringBuffer();
		if(!streetDir.isEmpty())
			buff.append(streetDir +".");
		
		buff.append(String.format("%s %s, Lo= %d, Hi=%d, Region= %S, ES= %s, Lat/Long=%s, SC= %s",
				streetName, streetType, addressNumLow, addressNumHi, region, school, location, schoolRegion));
		
		return buff.toString();	
	}
	
	//setters
	public void setLocation(String latlong) { this.location = latlong; }
	public void setSchool(String school) { this.school = school; }
	public void setSchoolRegion(String schoolCode) { this.schoolRegion = schoolCode; }
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[13];
		row[0] = Integer.toString(id);
		row[1] = streetName;
		row[2] = streetType;
		row[3] = region;
		row[4] = schoolRegion;
		row[5] = school;
		row[6] = location;
		row[7] = streetDir;
		row[8] = postDir;
		row[9] = Integer.toString(addressNumLow);
		row[10] = Integer.toString(addressNumHi);
		row[11] = "B";
		row[12] = zipCode;
		
		return row;
	}
}
