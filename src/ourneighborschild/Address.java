package ourneighborschild;

public class Address 
{
	/*****
	 * There are 9 component parts to an ONC address
	 */
	private String streetNum;
	private String streetPostDir;
	private String streetDir;
	private String streetName;
	private String streetType;
	private String unit;
	private String city;
	private String zip;
	private String region;
	
	
	public Address(String streetNum, String streetPostDir, String streetDir, String streetName, String streetType, 
					String unit, String city, String zip)
	{
		this.streetNum = streetNum;
		this.streetPostDir = streetPostDir;
		this.streetDir = streetDir;
		this.streetName = streetName;
		this.streetType = streetType;
		this.unit = unit;
		this.city = city;
		this.zip = zip;
		this.region = "?";
	}
	
	/*****
	 * Method will build an address object by breaking a street name into 4 component parts:
	 * Number, Name, Type. It will check to see if the number is included at the beginning of the name or
	 * if it is passed in the streetnum parameter
	 * @param streetnum
	 * @param streetname
	 * @param unit
	 * @param city
	 * @param zip
	 */
	public Address(String streetnum, String streetname, String unit, String city, String zip)
	{
		String[] searchAddress = new String[5];
		String[] step1 = new String[2];
			
		//Break the street name into its five parts using a three step process. If only the street name
		//or street number are empty, take different paths. 
		if(streetname.length() < 2)
			step1 = separateLeadingDigits(streetnum);
		else
			step1 = separateLeadingDigits(streetname);
			
		String[] step2 = separateStreetDirection(step1[2]);
		String[] step3 = separateStreetType(step2[1]);
			
		streetNum = step1[0];	//Street Number
		streetPostDir = step1[1];
		streetDir = step2[0];	//Street Direction
		streetName = step3[0];	//Street Name
		streetType= step3[1];	//Street Type
		this.unit = unit;
		this.city = city;
		this.zip = zip;		//zip code
		this.region = "?";
			
		if(searchAddress[0].isEmpty())	//Street number may not be contained in street name
		{
			String[] stnum = separateLeadingDigits(streetnum);
			streetNum = stnum[0];
		}
	}

	/*********************************************************************************
	 * This method takes a string and separates the leading digits. If there are characters
	 * after the leading digits that aren't a blank space and start with a '-' and, it will place
	 * all characters after the '-' into a separate field. If the first non digit character without
	 * a blank space, it will throw those away. It will
	 * return a two element string array with the leading digits as element 1 and the remainder
	 * of the string past the blank space as element 2. If there are no leading digits, element 1
	 ***********************************************************************************/
	String[] separateLeadingDigits(String streetname)
	{
		String[] output = {"","", ""};
	
		if(!streetname.trim().isEmpty())
		{	
			StringBuffer streetnumbuffer = new StringBuffer("");
			StringBuffer postdirbuffer = new StringBuffer("");
	
			//If there are leading digits, separate them and if the first character is a digit
			//and a non digit character is found before a black space, throw the non digit characters away.
			int ci = 0;
			if(Character.isDigit(streetname.charAt(ci)))
			{	
				while(ci < streetname.length() && Character.isDigit(streetname.charAt(ci)))	//Get leading digits
					streetnumbuffer.append(streetname.charAt(ci++));
				
				while(ci < streetname.length() && streetname.charAt(ci) != ' ' && streetname.charAt(ci++) == '-')	//throw away any dashes after digits
				
				while(ci < streetname.length() && streetname.charAt(ci) != ' '); //Throw away end of digit string if necessary
					postdirbuffer.append(streetname.charAt(ci++));
			}

		output[0] = streetnumbuffer.toString();	//house number if src starts with digit characters
		output[1] = postdirbuffer.toString();	//street post direction if the digits are followed by a -x or x
		output[2] = streetname.substring(ci).trim();	//remaining street name
		}

		return output;		
	}

	/*********************************************************************************
	 * This method takes a string and separates the street direction. If the first character
	 * is a N or a S and and the second character is a period or a blank space, a direction
	 * is present. The method will return a two element string array with the direction
	 * character as element 0 and the remainder of the string as element 1. If a street
	 * direction is not present, element 0 will be empty and element 1 will contain the 
	 * original street parameter
	 ***********************************************************************************/
	String[] separateStreetDirection(String street)
	{
		String[] output = new String[2];
	
		if(!street.isEmpty() && (street.charAt(0) == 'N' || street.charAt(0) == 'S') &&
			(street.charAt(1) == '.' || street.charAt(1) == ' '))
		{
			output[0] = Character.toString(street.charAt(0));
			output[1] = street.substring(2);
		}
		else
		{
			output[0] ="";
			output[1] = street;
		}

		return output;
	}

	String[] separateStreetType(String streetname)
	{
		StringBuffer buf = new StringBuffer("");
	
		String[] stnameparts = streetname.split(" ");
	
		int index = 0;
		while(index < stnameparts.length-1)
			buf.append(stnameparts[index++] + " ");
	
		String[] output = {buf.toString().trim(), stnameparts[index].trim()};
		return output;
	}
	
	public Address(String[] parts)
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
	
	public String getStreetNum() { return streetNum; }
	public String getStreetPostDir() { return streetPostDir; }
	public String getStreetDir() { return streetDir; }
	public String getStreetName() { return streetName; }
	public String getStreetType() { return streetType; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipCode() { return zip; }
	public String getRegion() { return region; }
	
	public void setStreetNum(String s) { streetNum = s; }
	public void setStreetPostDir(String s) { streetPostDir = s; }
	public void setStreetDir(String s) { streetDir = s; }
	public void setStreetName(String s) { streetName = s; }
	public void setStreetType(String s) { streetType = s; }
	public void setUnit(String s) { unit = s; }
	public void setCity(String s) { city = s; }
	public void setZipCode(String s) { zip = s; }
	public void setRegion(String s) { region = s; }
}
