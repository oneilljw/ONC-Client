package ourneighborschild;

//import java.io.Serializable;
import java.util.Date;

public class ONCVolunteer extends ONCGmailContactEntity implements Comparable<ONCVolunteer>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5277718159822649083L;
	
	public static final int VOLUNTEER_EXACT_MATCH = 255;
	public static final int VOLUNTEER_ZIPCODE_MATCH = 128;
	public static final int VOLUNTEER_CITY_MATCH = 64;
	public static final int VOLUNTEER_UNIT_MATCH = 32;
	public static final int VOLUNTEER_STREET_NAME_MATCH = 16;
	public static final int VOLUNTEER_STREET_NUM_MATCH = 8;
	public static final int VOLUNTEER_HOME_PHONE_MATCH = 4;
	public static final int VOLUNTEER_CELL_PHONE_MATCH = 2;
	public static final int VOLUNTEER_NAME_EMAIL_MATCH = 1;
	public static final int VOLUNTEER_DOES_NOT_MATCH = 0;
	
	private String drvNum;
//	private List<VolunteerActivity>  activityList;
//	private int qty;
	private int delAssigned;
	private int warehouseSignIns;
	
	public ONCVolunteer(int driverid, String drvNum, String fName, String lName, String email, 
						String hNum, String street, String unit, String city, String zipcode, 
						String homePhone, String cellPhone, String group, String comment, 
						Date today, String changedBy)
	{
		super(driverid, fName, lName, email, homePhone, cellPhone, hNum, street, unit, city, zipcode,
				comment, group,  new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		this.drvNum = drvNum;
//		this.activityList = actList;
//		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
//	public ONCVolunteer(int driverid, String drvNum, String fName, String lName, String email, 
//			String hNum, String street, String unit, String city, String zipcode, 
//			String homePhone, String cellPhone, String group,
//			String comment, Date today, String changedBy)
//	{
//		super(driverid, fName, lName, email, homePhone, cellPhone, hNum, street, unit, city, zipcode,
//				comment, group,  new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
//		this.drvNum = drvNum;
//		this.activityList = actList;
//		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
//		this.delAssigned = 0;
//		this.warehouseSignIns = 0;
//	}
	
	/***
	 * Constructor used when importing volunteers from sign-up genius exported .csv file.
	 * Should be deprecated for the 2018 season and beyond by direct import thru the API.
	 * @param line
	 * @param changedBy
	 * @param activityList
	 */
	public ONCVolunteer(String[] line, String changedBy)
	{
		super(-1, line[6], line[7], line[8], getPhone("Home", line[17], line[18]), 
				getPhone("Mobile", line[17], line[18]), parseAddress(line[11])[0], 
				parseAddress(line[11])[1], line[12], line[13], line[15],
				"", "Self",  new Date(), changedBy, STOPLIGHT_OFF, "Sign-Up Genius Volunteer", changedBy); 
		
		drvNum = "N/A";
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
	/***
	 * Constructor used when importing volunteers from sign-up genius direct import
	 * @param line
	 * @param changedBy
	 * @param activityList
	 */
	public ONCVolunteer(SignUpActivity sua)
	{
		super(-1, sua.getFirstname(), sua.getLastname(), sua.getEmail(), getPhone("Home", sua), 
				getPhone("Mobile", sua), getAddress(sua), "", "Self",  new Date(), 
				"Lavin, K", STOPLIGHT_OFF, "Sign-Up Genius Volunteer", "Lavin, K");
		
		this.drvNum = "";
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
	static String getPhone(String type, SignUpActivity sua)
	{
		if(type.equals("Mobile") && sua.getPhonetype().equals("Mobile"))
			return sua.getPhone();
		else if(type.equals("Home") && sua.getPhonetype().equals("Home"))
			return sua.getPhone();
		else
			return "";
	}
	
	static Address getAddress(SignUpActivity sua)
	{
		return new Address("", sua.getAddress1(), sua.getAddress2(), sua.getCity(), sua.getZipcode());
	}
	
	
	static String getPhone(String field, String phone, String type)
	{
		return field.equals(type) ? phone : "";
	}
	
	static String[] parseAddress(String address)
	{
		String[] splitAddress = new String[2];
		splitAddress[0] = "";
		splitAddress[1] = "";
		
		String[] parts = address.split(" ", 2);
		
		if(parts.length == 2)
		{
			splitAddress[0] = parts[0];
			splitAddress[1] = parts[1];
		}
		else
			splitAddress[0] = parts[0];
		
		return splitAddress;
		
	}
	
	
	ONCVolunteer(int id, String changedBy)
	{
		//constructor used when adding a new entity
		super(id, "", "", "", "", "", "", "", "", "", "", "", "", 
				new Date(), changedBy, STOPLIGHT_OFF, "Volunteer Added", changedBy);
		
		delAssigned = 0;
		warehouseSignIns = 0;
	}
	
	public ONCVolunteer(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), nextLine[2], nextLine[3], nextLine[9],nextLine[10],
				nextLine[11], nextLine[4],nextLine[5], nextLine[6], nextLine[7], nextLine[8],
				nextLine[12], nextLine[13], Long.parseLong(nextLine[16]), nextLine[17],
				Integer.parseInt(nextLine[18]), nextLine[19], nextLine[20]);
		
		drvNum = getDBString(nextLine[1]);
		delAssigned = nextLine[14].isEmpty() ? 0 : Integer.parseInt(nextLine[14]);
		warehouseSignIns = nextLine[15].isEmpty() ? 0 : Integer.parseInt(nextLine[15]);
	}
	
	public ONCVolunteer(ONCVolunteer v)	//copy constructor
	{
		super(v.id, v.firstName, v.lastName, v.email, v.homePhone, v.cellPhone, v.houseNum, v.street,
				v.unit, v.city, v.zipCode, v.comment, v.organization, v.dateChanged, v.changedBy,
				v.slPos, v.slMssg, v.slChangedBy);
		
		drvNum = v.drvNum;
		delAssigned = v.delAssigned; 
		warehouseSignIns = v.warehouseSignIns;
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public String getDrvNum() { return drvNum; }
//	public int getQty() { return qty; }
	public int getDelAssigned() { return delAssigned; }
	public int getSignIns() { return warehouseSignIns; }
//	public List<VolunteerActivity> getActivityList() { return activityList; }
	
	//setters
	public void setDrvNum(String drvNum) { this.drvNum = drvNum; }
//	public void setActivityList(List<VolunteerActivity> list) { this.activityList = list; }
//	public void setQtyd(int qty) { this.qty = qty; }
	public void setDelAssigned(int da) { delAssigned = da; }
	public void setSignIns(int si) { warehouseSignIns = si; }
/*	
	//activity list methods
	public void addActivity(VolunteerActivity activity)
	{
		//check to see if it's already in the list, otherwise, add it
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != activity.getID())
			index++;
			
		if(index == activityList.size())
			activityList.add(activity);
	}
	
	void removeActivity(VolunteerActivity activity)
	{
		//check to see if it's in the list, if so, remove it
		int index = 0;
		while(index < activityList.size() && activityList.get(index).getID() != activity.getID())
			index++;
					
		if(index < activityList.size())
			activityList.remove(index);
	}
	
	boolean isVolunteeringFor(String category)
	{
		int index = 0;
		while(index < this.activityList.size() && !activityList.get(index).getCategory().equals(category))
			index++;
		
		return index < activityList.size();
	}
	
	boolean isVolunteeringFor(int activityID)
	{
		int index = 0;
		while(index < this.activityList.size() && activityList.get(index).getID() != activityID)
			index++;
		
		return index < activityList.size();
	}
	
	boolean isVolunteeringFor(VolunteerActivity va)
	{
		int index = 0;
		while(index < this.activityList.size() && activityList.get(index).getID() != va.getID())
			index++;
		
		return index < activityList.size();
	}
	
	VolunteerActivity getVolunteerActivity(int activityID)
	{
		int index = 0;
		while(index < this.activityList.size() && activityList.get(index).getID() != activityID)
			index++;
		
		return index < activityList.size() ? activityList.get(index) : null;
	}
*/	
	public void incrementDeliveryCount(int count) { delAssigned += count; }
/*	
//	List<VolunteerActivity> convertActivityStringToList(String zActivities, List<VolunteerActivity> allActList)
//	{
//		String[] activities = zActivities.split("_");
//		for(String zActivityID : activities)
//		{
//			int index = 0;
//			while(index < allActList.size() && allActList.get(index).getID() != Integer.parseInt(zActivityID))
//				index++;
//			
//			if(index < allActList.size())
//				activityList.add(allActList.get(index));
//		}
//		
//		return activityList;
//	}
	
	String convertActivityIDListToString()
	{
		if(activityList.isEmpty())
			return "";
		else
		{
			StringBuffer activityBuffer = new StringBuffer(Integer.toString(activityList.get(0).getID()));
			for(int i=1; i < activityList.size(); i++)
				activityBuffer.append("_" + Integer.toString(activityList.get(i).getID()));
			
			return activityBuffer.toString();
		}
	}
	
//	String convertActivityCommentsToString()
//	{
//		if(activityList.isEmpty())
//			return "";
//		else
//		{
//			StringBuffer activityBuffer = new StringBuffer(getEncodedActivityComment(activityList.get(0)));
//			for(int i=1; i < activityList.size(); i++)
//				activityBuffer.append("_" + getEncodedActivityComment(activityList.get(i)));
//			
//			return activityBuffer.toString();
//		}
//	}
	
//	String getEncodedActivityComment(VolunteerActivity va)
//	{
//		if(va.getID() < 10) { return "000" + Integer.toString(va.getID()) + va.getComment(); }
//		else if(va.getID() < 100) { return "00" + Integer.toString(va.getID()) + va.getComment(); }
//		else if(va.getID() < 1000) { return "0" + Integer.toString(va.getID()) + va.getComment(); }
//		else { return Integer.toString(va.getID()) + va.getComment(); }
//	}
*/	
	/****
	 * compares ONCVolunteer object to an object imported from SignUpGenius. If the first name, last name
	 * & email addresses are not identical, returns VOLUNTEER_DOES_NOT_MATCH. If they are identical, 
	 * compares cell and home phones, street number, street, unit, city and zip code and returns 
	 * an integer indicating the result. Since SignUpGenius only provides a cell or a home phone, care
	 * is taken to ensure comparisons are only done if both home or cell are present. 
	 * @param currVol
	 * @return
	 */
	public int compareVolunteers(ONCVolunteer currVol)
	{
		int match = VOLUNTEER_DOES_NOT_MATCH;
		
		if(currVol.getEmail().equalsIgnoreCase(this.email) && currVol.getFirstName().equalsIgnoreCase(this.firstName) &&
				currVol.getLastName().equalsIgnoreCase(this.lastName))
		{
			match = match | VOLUNTEER_NAME_EMAIL_MATCH;
 
			//omit all non-numeric numbers from the phone number
			String volCell = currVol.getCellPhone().replaceAll("[^\\d]", "");
			String volHome = currVol.getHomePhone().replaceAll("[^\\d]", "");
			String sugCell = this.cellPhone.replaceAll("[^\\d]", "");
			String sugHome = this.homePhone.replaceAll("[^\\d]", "");

			if(sugCell.isEmpty() &&  !volCell.isEmpty() || sugCell.equals(volCell))
				match = match | VOLUNTEER_CELL_PHONE_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s cell %s does not match imported cell %s", currVol.getLastName(), currVol.getCellPhone(), this.cellPhone));
	
			if(sugHome.isEmpty() && !volHome.isEmpty() || sugHome.equals(volHome))
				match = match | VOLUNTEER_HOME_PHONE_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s home %s does not match imported home %s", currVol.getLastName(), currVol.getCellPhone(), this.cellPhone));
				
			if(currVol.getHouseNum().equals(this.houseNum))
				match = match | VOLUNTEER_STREET_NUM_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s housenum %s does not match imported housenum %s", currVol.getLastName(), currVol.getHouseNum(), this.houseNum));
			
			//remove all white space and '.' characters
			String volStreet = currVol.getStreet().replaceAll("[\\s.]", "");
			String sugStreet = this.street.replaceAll("[\\s.]", "");
			
			if(volStreet.equalsIgnoreCase(sugStreet))
				match = match | VOLUNTEER_STREET_NAME_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s street name %s does not match imported street %s", currVol.getLastName(), currVol.getStreet(), this.street));
	
			if(currVol.getUnit().equalsIgnoreCase(this.unit))
				match = match | VOLUNTEER_UNIT_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s unit %s does not match imported unit %s", currVol.getLastName(), currVol.getUnit(), this.unit));
	
			if(currVol.getCity().equalsIgnoreCase(this.city))
				match = match | VOLUNTEER_CITY_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s city %s does not match imported city %s", currVol.getLastName(), currVol.getCity(), this.city));
		
			if(currVol.getZipCode().equals(this.zipCode))
				match = match | VOLUNTEER_ZIPCODE_MATCH;
//			else
//				System.out.println(String.format("ONCVol.compare %s zipcode %s does not match imported zipcode %s", currVol.getLastName(), currVol.getZipCode(), this.zipCode));
		}
		
		return match;
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {Integer.toString(id), drvNum, firstName, lastName, 
						houseNum, street, unit, city, zipCode, email, homePhone, cellPhone, comment,
						organization, Integer.toString(delAssigned), Integer.toString(warehouseSignIns),
						Long.toString(dateChanged.getTimeInMillis()), changedBy,  Integer.toString(slPos),
						slMssg, slChangedBy};
		
		return row;
	}
}
