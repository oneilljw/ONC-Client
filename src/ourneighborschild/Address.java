package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public class Address 
{
	/*****
	 * There are nine component parts to an ONC address
	 */
	private String streetNum;	//digits in the street number
	private String streetNumSuffix;	//non digit characters other than '-' in street number
	private String streetDir;	//N, E, S, W
	private String streetName;
	private String streetType;	//St, Rd, Ln, Ave, Ct, etc
	private String streetPostDir;	//N, E, S, W, NE, NW, SE, SW
	private String unit;	//apartment or other id in multiple address structure
	private String city;
	private String zip;
	
	public Address(String streetNum, String streetNumSuffix, String streetDir, String streetName, 
						String streetType, String streetPostDir, String unit, String city, String zip)
	{
		this.streetNum = streetNum;
		this.streetNumSuffix = streetNumSuffix;
		this.streetPostDir = streetPostDir;
		this.streetDir = streetDir;
		this.streetName = streetName;
		this.streetType = streetType;
		this.unit = unit;
		this.city = city;
		this.zip = zip;
	}
	
	/*****
	 * Method will build an address object by breaking a street name into 4 component parts:
	 * Number, Name, Type. It will check to see if the number is included at the beginning of the name or
	 * if it is passed in the street number parameter
	 * @param streetnum
	 * @param streetname
	 * @param unit
	 * @param city
	 * @param zip
	 */
	public Address(String streetnum, String streetname, String unit, String city, String zip)
	{
		//check to see if street number was included in the street name string. If it was, separate it
		if(streetnum.isEmpty())
		{
			this.streetName = separateStreetNumber(streetname);
			separateStreetNumberSuffix(this.streetNum);
		}
		else
		{
			this.streetNum = streetnum.trim();
			separateStreetNumberSuffix(this.streetNum);
			this.streetName = streetname.trim();
		}
		
		//check to see if remaining street name starts with a direction. If so, separate it
		this.streetName = separateStreetDirectionFromStreetName(this.streetName);
		
		//check to see if remaining street name contains a post direction
		this.streetName = separateStreetPostDirectionFromStreetName(this.streetName);
		
		//check to see if remaining street name contains a street type
		this.streetName = separateStreetType(this.streetName);
		
		this.unit = unit;
		this.city = city;
		this.zip = zip;	
	}

	/*********************************************************************************
	 * This method takes a string and separates the street number if its starts with a digit.
	 * The method will separate all subsequent characters until a blank space is found. It
	 * will return the remaining string.
	 ***********************************************************************************/
	String separateStreetNumber(String streetname)
	{
		streetNum = "";
		String parts[] = streetname.trim().split(" ");
		if(parts.length > 1 && Character.isDigit(parts[0].charAt(0)))
		{
			streetNum = parts[0].trim();
			return streetname.substring(parts[0].length()).trim();
		}
		else
			return streetname;			
	}
	
	/*********************************************************************************
	 * This method takes a string and separates the street number and suffix. A street
	 * number that contains a letter or a dash followed by a letter has a street number
	 * suffix
	 ***********************************************************************************/
	void separateStreetNumberSuffix(String streetnumber)
	{
		streetNumSuffix = "";
		
		//determine the first character that is not a digit
		int ci = 0;
		while(ci < streetnumber.length() && Character.isDigit(streetnumber.charAt(ci)))
			ci++;
		
		if(ci < streetnumber.length())
		{
			//non-digit found, check to see if it's a dash - if so increment to next character
			if(streetnumber.charAt(ci) == '-' && ci <= streetnumber.length()-2)
				streetNumSuffix = streetnumber.substring(ci+1);
			else
				streetNumSuffix = streetnumber.substring(ci);
			
			streetNum = streetnumber.substring(0, ci);
		}			
	}

	/*********************************************************************************
	 * This method takes a string and separates the street direction. If the first character
	 * is a N, S, E or W and and the second character is a period or a blank space, a direction
	 * is present. The method will set the streetDir and return the remaining street string
	 ***********************************************************************************/
	String separateStreetDirectionFromStreetName(String street)
	{
		if(!street.isEmpty() && (street.charAt(0) == 'N' || street.charAt(0) == 'S' || street.charAt(0) == 'E' || street.charAt(0) == 'W') &&
			(street.charAt(1) == '.' || street.charAt(1) == ' '))
		{
			streetDir = Character.toString(street.charAt(0));
			return(street.substring(2).trim());
		}
		else
		{
			streetDir = "";
			return street;
		}
	}
	
	/*********************************************************************************
	 * This method takes a string and separates the street direction. If the first character
	 * is a N, S, E or W and and the second character is a period or a blank space, a direction
	 * is present. The method will set the streetPostDir and return the remaining street string
	 ***********************************************************************************/
	String separateStreetPostDirectionFromStreetName(String street)
	{
		String[] parts = street.split(" ");
		
		if(parts.length < 2)
		{
			streetPostDir = "";
			return street;
		}
		else
		{
			int partsCount = parts.length-1;
			String lastpart = parts[parts.length-1];
			
			if(lastpart.equalsIgnoreCase("North") || lastpart.equalsIgnoreCase("N.") || lastpart.equalsIgnoreCase("N"))
				streetPostDir = "N";
			else if(lastpart.equalsIgnoreCase("South") || lastpart.equalsIgnoreCase("S.") || lastpart.equalsIgnoreCase("S"))
				streetPostDir = "S";
			else if(lastpart.equalsIgnoreCase("East") || lastpart.equalsIgnoreCase("E.") || lastpart.equalsIgnoreCase("E"))
				streetPostDir = "E";
			else if(lastpart.equalsIgnoreCase("West") || lastpart.equalsIgnoreCase("W.") || lastpart.equalsIgnoreCase("W"))
				streetPostDir = "W";
			else if(lastpart.equalsIgnoreCase("NorthEast") || lastpart.equalsIgnoreCase("NE.") || lastpart.equalsIgnoreCase("NE"))
				streetPostDir = "NE";
			else if(lastpart.equalsIgnoreCase("NorthWest") || lastpart.equalsIgnoreCase("NW.") || lastpart.equalsIgnoreCase("NW"))
				streetPostDir = "NW";
			else if(lastpart.equalsIgnoreCase("SouthEast") || lastpart.equalsIgnoreCase("SE.") || lastpart.equalsIgnoreCase("SE"))
				streetPostDir = "SE";
			else if(lastpart.equalsIgnoreCase("SouthWest") || lastpart.equalsIgnoreCase("SW.") || lastpart.equalsIgnoreCase("SW"))
				streetPostDir = "SW";
			else
			{
				streetPostDir = "";
				partsCount++;
			}
			
			StringBuilder buff = new StringBuilder(parts[0]);
			for(int pn=1; pn< partsCount; pn++)
				buff.append(" " + parts[pn]);
			
			return buff.toString().trim();			
		}	
	}

	String separateStreetType(String streetname)
	{
		String[] parts = streetname.split(" ");
	
		if(parts.length > 1)
		{
			streetType = parts[parts.length-1].trim();
			return streetname.replace(streetType, "").trim();
		}
		else
		{
			streetType = "";
			return streetname;
		}
	}
	
	public Address(String[] parts)
	{
		this.streetNum = parts[0];
		this.streetNumSuffix = parts[1];
		this.streetDir = parts[2];
		this.streetName = parts[3];
		this.streetType = parts[4];
		this.streetPostDir = parts[5];
		this.unit = parts[6];
		this.city = parts[7];
		this.zip = parts[8];
	}
	
	public String getStreetNum() { return streetNum; }
	public String getStreetNumSuffix() { return streetNumSuffix; }
	public String getStreetPostDir() { return streetPostDir; }
	public String getStreetDir() { return streetDir; }
	public String getStreetName() { return streetName; }
	public String getStreetType() { return streetType; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipCode() { return zip; }
	public String getPrintableAddress()
	{
		StringBuilder address = new StringBuilder(streetNum);
		if(!streetNumSuffix.isEmpty())
			address.append("-" + streetNumSuffix);
		if(!streetDir.isEmpty())
			address.append(" " + streetDir + ".");
		address.append(" " + streetName +" " + streetType);
		if(!streetPostDir.isEmpty())
			address.append(" " + streetPostDir);
		if(!unit.isEmpty())
			address.append(" " + unit);
		address.append(" " + city + " " + zip);
		
		return address.toString();
	}
	
	public void setStreetNum(String s) { streetNum = s; }
	public void setStreetNumSuffix(String s) { streetNumSuffix = s; }
	public void setStreetPostDir(String s) { streetPostDir = s; }
	public void setStreetDir(String s) { streetDir = s; }
	public void setStreetName(String s) { streetName = s; }
	public void setStreetType(String s) { streetType = s; }
	public void setUnit(String s) { unit = s; }
	public void setCity(String s) { city = s; }
	public void setZipCode(String s) { zip = s; }
	
	public boolean isSameHouseNumAndStreet(String streetnum, String streetname)
	{
		return streetname.contains(this.streetName) && streetnum.contains(this.streetNum);
	}
	
	public String[] getExportRow() 
	{
		List<String> addressList = new ArrayList<String>();
		addressList.add(streetNum);
		addressList.add(streetNumSuffix);
		addressList.add(streetDir);
		addressList.add(streetName);
		addressList.add(streetType);
		addressList.add(streetPostDir);
		addressList.add(unit);
		addressList.add(city);
		addressList.add(zip);
		
		return addressList.toArray(new String[addressList.size()]);
	}
}
