package ourneighborschild;

import java.io.Serializable;

public class Region implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1523174258125581706L;
	private String streetName;
//	private String streetType;
	private String region;
	private String streetDir;
//	private String postDir;
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
		streetName = file_line[0];
//		streetType = file_line[1];
		region = file_line[2];
		streetDir = file_line[3];
//		postDir = file_line[4];
		addressNumLow = Integer.parseInt(file_line[5]);
		addressNumHi = Integer.parseInt(file_line[6]);
//		odd_even = file_line[7];
		zipCode = file_line[8];
//		precinct = file_line[9];
//		townPrecinct = file_line[10];
//		congDistrict = file_line[11];
//		stateSenateDistrict = file_line[12];
//		stateHouseDistrict = file_line[13];
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
	
	public String getRegion() { return region; }
	public String getStreetName() { return streetName; }
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
