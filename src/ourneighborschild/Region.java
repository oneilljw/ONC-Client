package ourneighborschild;

public class Region extends ONCObject
{
	/**
	 * 
	 */
	private String streetName;
	private String streetType;
	private String region;
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
	
	public Region(String[] file_line)
	{
		super(isNumeric(file_line[0]) ? Integer.parseInt(file_line[0]) : -1);
		streetName = file_line[1];
		streetType = file_line[2];
		region = file_line[3];
		streetDir = file_line[4];
		postDir = file_line[5];
		addressNumLow = isNumeric(file_line[6]) ? Integer.parseInt(file_line[6]) : 0;
		addressNumHi = isNumeric(file_line[7]) ? Integer.parseInt(file_line[7]) : 0;;
//		odd_even = file_line[8];
		zipCode = file_line[9];
//		precinct = file_line[10];
//		townPrecinct = file_line[11];
//		congDistrict = file_line[12];
//		stateSenateDistrict = file_line[13];
//		stateHouseDistrict = file_line[14];
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
	public String getPostDir() { return postDir; }
	public int getAddressNumLow() { return addressNumLow; }
	public int getAddressNumHi() { return addressNumHi; }
	public String getZipCode() { return zipCode; }
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[10];
		row[0] = Integer.toString(id);
		row[1] = streetName;
		row[2] = streetType;
		row[3] = region;
		row[4] = streetDir;
		row[5] = postDir;
		row[6] = Integer.toString(addressNumLow);
		row[7] = Integer.toString(addressNumHi);
		row[8] = "B";
		row[9] = zipCode;
		
		return row;
	}
}
