package ourneighborschild;

public class ONCRegions extends ONCDatabase
{
	/**
	 * 
	 */
	private static ONCRegions  instance = null;
	private transient String[] regions = {"?"};	//Must initialize for dialog constructors
	
	private ONCRegions()
	{
		super();
	}
	
	public static ONCRegions getInstance()
	{
		if(instance == null)
			instance = new ONCRegions();
		
		return instance;
	}
	
	void getRegionsFromServer()
	{
		String response = "";
		response = serverIF.sendRequest("GET<regions>");
		
		if(response != null && response.startsWith("REGIONS"))
		{
			regions = response.substring(7).split(",");
			
			fireDataChanged(this, "UPDATED_REGION_LIST", regions);
		}
	}
	
	/********************************************************************************************
	 * An address for region match requires four parts: Street Number, Street Direction, Street Name 
	 * and Street Suffix. This method takes two parameters, street number and name and determines
	 * the four parts. It is possible the street number is embedded in the streetname parameter and
	 * or is present in the streetnum parameter
	 *********************************************************************************************/
	int getRegionMatch(String streetnum, String streetname)
	{
		if(serverIF == null || !serverIF.isConnected())
		{
			return 0;
		}
		else
		{
			String response = "";
			String addressjson = "{\"streetnum\":\"" + streetnum +
						"\",\"streetname\":\"" + streetname +"\"}";
			response = serverIF.sendRequest("GET<regionmatch>" + addressjson);
			
			if(response.startsWith("MATCH"))
				return Integer.parseInt(response.substring(5));
			else
				return 0;
		}
	}
	
	/*********************************************************************************
	 * This method takes a string and separates the leading digits. If there are characters
	 * after the leading digits without a blank space, it will throw those away. It will
	 * return a two element string array with the leading digits as element 1 and the remainder
	 * of the string past the blank space as element 2. If there are no leading digits, element 1
	 ***********************************************************************************/
/*	
	String[] separateLeadingDigits(String src)
	{
		String[] output = {"",""};
		
		if(!src.isEmpty())
		{	
			StringBuffer buff = new StringBuffer("");
		
			//IF there are leading digits, separate them and if the first character is a digit
			//and a non digit character is found before a black space, throw the non digit characters away.
			int ci = 0;
			if(Character.isDigit(src.charAt(ci)))
			{	
				while(ci < src.length() && Character.isDigit(src.charAt(ci)))	//Get leading digits
					buff.append(src.charAt(ci++));
		
				while(ci < src.length() && src.charAt(ci++) != ' '); //Throw away end of digit string if necessary	
				
			}
	
			output[0] = buff.toString();
			output[1] = src.substring(ci).trim();
		}

		return output;		
	}
*/	
	/*********************************************************************************
	 * This method takes a string and separates the street direction. If the first character
	 * is a N or a S and and the second character is a period or a blank space, a direction
	 * is present. The method will return a two element string array with the direction
	 * character as element 0 and the remainder of the string as element 1. If a street
	 * direction is not present, element 0 will be empty and element 1 will contain the 
	 * original street parameter
	 ***********************************************************************************/
/*	
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
	
	String[] separateStreetSuffix(String streetname)
	{
		StringBuffer buf = new StringBuffer("");
		
		String[] stnameparts = streetname.split(" ");
		
		int index = 0;
		while(index < stnameparts.length-1)
			buf.append(stnameparts[index++] + " ");
		
		String[] output = {buf.toString().trim(), stnameparts[index].trim()};
		return output;
	}
	
	int searchForRegionMatch(String[] searchAddress)
	{	
		int ri = 0;
		
		while(ri < regAL.size() && !regAL.get(ri).isRegionMatch(searchAddress))
			ri++;
		
		//If match not found return region = 0, else return region
		if(ri == regAL.size())
			return 0;
		else
			return getRegionNumber(regAL.get(ri).getRegion());
	}
*/	
//	String[] getRegions() { return regions; }
	
	String getRegionID(int regnum) { return regions[regnum]; }
	
	int size() { return regions.length; }
	
	boolean isRegionValid(int region) { return region >=0 && region < regions.length; }
	
	//Returns 0 if r is null or empty, number corresponding to letter otherwise
	int getRegionNumber(String r) 
	{
		int index = 0;
		if(r != null && !r.isEmpty())
			while (index < regions.length && !r.equals(regions[index]))
				index++;
		
		return index;
	}
/*	
	void getONCRegions(String path)
	{		
		try 
    	{
    		CSVReader reader = new CSVReader(new FileReader(path));
    		String[] nextLine, header;
    		
    		if((header = reader.readNext()) != null)
    		{
    			//Read the ONC CSV File
    			if(header.length == ONC_REGION_HEADER_LENGTH)
    			{
    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
    					regAL.add(new Region(nextLine));
    			}
    			else
    				JOptionPane.showMessageDialog(null, "Regions file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Region File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
    		}
    		else
				JOptionPane.showMessageDialog(null, "Couldn't read file, is it empty?: " + path, 
						"Invalid Region File", JOptionPane.ERROR_MESSAGE, oncIcon); 
    	} 
    	catch (IOException x)
    	{
    		JOptionPane.showMessageDialog(null, "Unable to open region file: " + path, 
    				"Regions Unavailable", JOptionPane.ERROR_MESSAGE, oncIcon);
    	}
    
	}
*/
	@Override
	public void dataChanged(ServerEvent ue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String update(Object source, ONCObject entity) {
		// TODO Auto-generated method stub
		return null;
	}
}
