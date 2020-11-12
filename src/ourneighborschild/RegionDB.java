package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class RegionDB extends ONCDatabase
{
	/**
	 * 
	 */
	private static RegionDB  instance = null;
	private transient String[] regions = {"?"};	//Must initialize for dialog constructors
	private List<School> schoolList;
	
	private RegionDB()
	{
		super();
		this.title = "Schools";
	
		//Create the list of elementary schools in ONC's serving area, ordered by school name
	
		schoolList = new ArrayList<School>();
/*			
		schoolList.add(new School("J", new Address("4200", "", "", "Lees Corner", "Road", "", "", "Chantilly", "20151"), "Brookfield", "38.882490,-77.419328"));
		schoolList.add(new School("A", new Address("15301", "", "", "Lee", "Highway", "", "", "Centreville", "20120"), "Bull Run", "38.828835,-77.475749"));
		schoolList.add(new School("B", new Address("14400", "", "", "New Braddock", "Road", "", "", "Centreville", "20121"), "Centre Ridge", "38.826088,-77.445725"));
		schoolList.add(new School("C", new Address("14330", "", "", "Green Trais", "Blvd", "", "", "Centreville", "20121"), "Centreville", "38.820056,-77.440809"));
		schoolList.add(new School("F", new Address("5301", "", "", "Sully Station", "Drive", "", "", "Centreville", "20120"), "Cub Run", "38.865949,-77.458535"));
		schoolList.add(new School("H", new Address("15109", "", "", "Carlbern", "Drive", "", "", "Centreville", "20120"), "Deer Park", "38.855992,-77.470855"));
		schoolList.add(new School("Q", new Address("2708", "", "", "Centreville", "Road", "", "", "Herndon", "20171"), "Floris", "38.936122,-77.414987"));
		schoolList.add(new School("K", new Address("13006", "", "", "Point Pleasant", "Drive", "", "", "Fairfax", "22033"), "Greenbriar East", "38.872170,-77.394355"));
		schoolList.add(new School("L", new Address("13300", "", "", "Poplar Tree", "Road", "", "", "Fairfax", "22033"), "Greenbriar West", "38.876560,-77.405355"));
		schoolList.add(new School("M", new Address("13500", "", "", "Hollinger", "Avenue", "", "", "Fairfax", "22033"), "Lees Corner", "38.890636,-77.411140"));
		schoolList.add(new School("I", new Address("6100", "", "", "Stone", "Road", "", "", "Centreville", "20120"), "London Towne", "38.839880,-77.456534"));
		schoolList.add(new School("R", new Address("2480", "", "", "River Birch", "Road", "", "", "Herndon", "20171"), "Lutie Lewis Coates", "38.952190,-77.419300"));
		schoolList.add(new School("S", new Address("2499", "", "", "Thomas Jefferson", "Drive", "", "", "Herndon", "20171"), "McNair", "38.946508,-77.404669"));
		schoolList.add(new School("P", new Address("3210", "", "", "Kincross", "Circle", "", "", "Herndon", "20171"), "Oak Hill", "38.913202,-77.408478"));
		schoolList.add(new School("N", new Address("13440", "", "", "Melville", "Lane", "", "", "Chantilly", "20151"), "Poplar Tree", "38.863387,-77.414740"));
		schoolList.add(new School("D", new Address("13340", "", "", "Leland", "Road", "", "", "Centreville", "20120"), "Powell", "38.846304,-77.407887"));
		schoolList.add(new School("E", new Address("13611", "", "", "Springstone", "Drive", "", "", "Clifton", "20124"), "Union Mill", "38.820727,-77.417579"));
		schoolList.add(new School("G", new Address("15450", "", "", "Martins Hundred", "Drive", "", "", "Centreville", "20120"), "Virginia Run", "38.852333,-77.485570"));
		schoolList.add(new School("O", new Address("5400", "", "", "Willow Springs School", "Road", "", "", "Fairfax", "22030"), "Willow Springs", "38.832115,-77.379740"));
*/
	}
	
	public static RegionDB getInstance()
	{
		if(instance == null)
			instance = new RegionDB();
		
		return instance;
	}
	
	@Override
	List<School> getList() { return schoolList; }
/*	
	void getRegionsFromServer()
	{
		String response = "NO_REGIONS";
		response = serverIF.sendRequest("GET<regions>");
		
		if(response != null && response.startsWith("REGIONS"))
		{
			regions = response.substring(7).split(",");
			
			fireDataChanged(this, "UPDATED_REGION_LIST", regions);
		}
	}
*/	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		if(serverIF != null && serverIF.isConnected())
		{
			String response = serverIF.sendRequest("GET<regions>");
			
			if(response != null && response.startsWith("REGIONS"))
			{
				regions = response.substring(7).split(",");
				
				fireDataChanged(this, "UPDATED_REGION_LIST", regions);
			
    			response = null;
    			Gson gson = new Gson();
    			Type listtype = new TypeToken<ArrayList<School>>(){}.getType();
    			
    			response = serverIF.sendRequest("GET<served_schools>");
    			if(response != null)
    			{
    				schoolList = gson.fromJson(response, listtype);
        			Collections.sort(schoolList, new SchoolNameComparator());	//sort list by school code
        			bImportComplete = true;
    			}
			}
		}
		
		return bImportComplete;
	}
	
	@Override 
	String[] getExportHeader() 
	{
		return new String[] {"ID", "Code", "Street #", "Suffix", "Dir", "Street Name", "Type", "Post Dir",
							"Unit", "City", "Zipcode", "School Name", "Lat/Long", "Pyramid", "Type"};
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
	String getSchoolName(String schoolCode)
	{
		if(schoolCode.equals("X"))
			return "Wish Anticipation";
		else if(schoolCode.equals("Y"))
			return "Not in Pyramid";
		else if(schoolCode.equals("Z"))
			return "Not in Area";
		else
		{	
			int index = 0;
			while(index < schoolList.size() && ! schoolList.get(index).getCode().equals(schoolCode))
				index++;
		
			return index < schoolList.size() ? schoolList.get(index).getName() : "";
		}
	}
	
	List<School> getServedSchoolList(SchoolType type)
	{
		List<School> resultSchoolList = new ArrayList<School>();
		for(School sch : schoolList)
			if(type == SchoolType.ALL || type == sch.getType())
				resultSchoolList.add(sch);
		
		return resultSchoolList; 
	}
	
	/****
	 * Takes an address and determines if it matches a served school address
	 * @param streetNum
	 * @param streetName
	 * @param city
	 * @param zipcode
	 * @return ServedSchool
	 */
	School findServedSchool(String streetNum, String streetName, String city, String zipcode)
	{
		School servedSchool = null;
		for(School school : schoolList)
		{
			Address schoolAdd = school.getAddress();
			
			if(schoolAdd.getStreetNum().equals(streetNum) && 
				streetName.toLowerCase().contains(schoolAdd.getStreetName().toLowerCase()) &&
				 schoolAdd.getCity().equals(city) && schoolAdd.getZipCode().equals(zipcode))
					servedSchool = school;
		}
			return servedSchool;
	}
		
	@Override
	public void dataChanged(ServerEvent ue) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	String update(Object source, ONCObject entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class SchoolNameComparator implements Comparator<School>
	{
		public int compare(School o1, School o2)
		{
			return o1.getName().compareTo(o2.getName());
		}
	}
}
