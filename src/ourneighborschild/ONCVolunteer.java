package ourneighborschild;

//import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ONCVolunteer extends ONCGmailContactEntity implements Comparable<ONCVolunteer>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5277718159822649083L;
	
	private long geniusID;
	private String drvNum;
	private List<VolunteerActivity>  activityList;
	private int qty;
	private int delAssigned;
	private int warehouseSignIns;
	
	public ONCVolunteer(int driverid, long geniusID, String drvNum, String fName, String lName, String email, 
						String hNum, String street, String unit, String city, String zipcode, 
						String homePhone, String cellPhone, String qty, String group, String comment, 
						List<VolunteerActivity> actList, Date today, String changedBy)
	{
		super(driverid, fName, lName, email, homePhone, cellPhone, hNum, street, unit, city, zipcode,
				comment, group,  new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		this.geniusID = geniusID;
		this.drvNum = drvNum;
		this.activityList = actList;
		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
	public ONCVolunteer(int driverid, long geniusID, String drvNum, String fName, String lName, String email, 
			String hNum, String street, String unit, String city, String zipcode, 
			String homePhone, String cellPhone, String qty, List<VolunteerActivity> actList, String group,
			String comment, Date today, String changedBy)
	{
		super(driverid, fName, lName, email, homePhone, cellPhone, hNum, street, unit, city, zipcode,
				comment, group,  new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		this.geniusID = geniusID;
		this.drvNum = drvNum;
		this.activityList = actList;
		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
	/***
	 * Constrcutor used when importing volunteers from sign-up genius eported .csv file.
	 * Should be deprecated for the 2018 season and beyond by direct import thur the API.
	 * @param line
	 * @param changedBy
	 * @param activityList
	 */
	public ONCVolunteer(String[] line, String changedBy, List<VolunteerActivity> activityList)
	{
		super(-1, line[6], line[7], line[8], getPhone("Home", line[17], line[18]), 
				getPhone("Mobile", line[17], line[18]), parseAddress(line[11])[0], 
				parseAddress(line[11])[1], line[12], line[13], line[15],
				"", "Self",  new Date(), changedBy, STOPLIGHT_OFF, "Sign-Up Genius Volunteer", changedBy); 
		
		this.geniusID = -1;
		drvNum = "N/A";
		qty = activityList.size();
		this.activityList = activityList;
		this.delAssigned = 0;
		this.warehouseSignIns = 0;
	}
	
	/***
	 * Constrcutor used when importing volunteers from sign-up genius direct import
	 * @param line
	 * @param changedBy
	 * @param activityList
	 */
	public ONCVolunteer(SignUpActivity sua)
	{
		super(-1, sua.getFirstname(), sua.getLastname(), sua.getEmail(), getPhone("Home", sua), 
				getPhone("Mobile", sua), getAddress(sua), "", "SignUpGenius",  new Date(), 
				"Lavin, K", STOPLIGHT_OFF, "SignUp Volunteer added", "Lavin, K");
		
		this.geniusID = sua.getItemmemberid();
		this.drvNum = "";
		this.activityList = new ArrayList<VolunteerActivity>();
		this.qty = 0;
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
		
		this.geniusID = -1;
		qty = 1;
		delAssigned = 0;
		warehouseSignIns = 0;
	}
	
	public ONCVolunteer(String[] nextLine, List<VolunteerActivity> volActList)
	{
		super(Integer.parseInt(nextLine[0]), nextLine[3], nextLine[4], nextLine[10],nextLine[11],
				nextLine[12], nextLine[5],nextLine[6], nextLine[7], nextLine[8], nextLine[9],
				nextLine[13], (nextLine[15]), Long.parseLong(nextLine[20]), nextLine[21],
				Integer.parseInt(nextLine[22]), nextLine[23], nextLine[24]);
		
		this.geniusID = nextLine[1].isEmpty() ? -1 :Long.parseLong(nextLine[1]);
		drvNum = getDBString(nextLine[2]);
		activityList = volActList;
		qty = nextLine[17].isEmpty() ? 0 : Integer.parseInt(nextLine[17]);;
		delAssigned = nextLine[18].isEmpty() ? 0 : Integer.parseInt(nextLine[18]);;
		warehouseSignIns = nextLine[19].isEmpty() ? 0 : Integer.parseInt(nextLine[19]);
	}
	
	public ONCVolunteer(ONCVolunteer v)	//copy constructor
	{
		super(v.id, v.firstName, v.lastName, v.email, v.homePhone, v.cellPhone, v.houseNum, v.street,
				v.unit, v.city, v.zipCode, v.comment, v.organization, v.dateChanged, v.changedBy,
				v.slPos, v.slMssg, v.slChangedBy);
		
		this.geniusID = v.geniusID;
		drvNum = v.drvNum;
		this.activityList = new ArrayList<VolunteerActivity>();
		for(VolunteerActivity activity : v.activityList)
			this.activityList.add(activity);
		
		qty = v.qty;
		delAssigned = v.delAssigned; 
		warehouseSignIns = v.warehouseSignIns;
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public long getGeniusID() { return geniusID; }
	public String getDrvNum() { return drvNum; }
	public int getQty() { return qty; }
	public int getDelAssigned() { return delAssigned; }
	public int getSignIns() { return warehouseSignIns; }
	public List<VolunteerActivity> getActivityList() { return activityList; }
	
	//setters
	public void setGeniusID(long gID) { this.geniusID = gID; }
	public void setDrvNum(String drvNum) { this.drvNum = drvNum; }
	public void setActivityList(List<VolunteerActivity> list) { this.activityList = list; }
	public void setQtyd(int qty) { this.qty = qty; }
	public void setDelAssigned(int da) { delAssigned = da; }
	public void setSignIns(int si) { warehouseSignIns = si; }
	
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
	
	public void incrementDeliveryCount(int count) { delAssigned += count; }
	
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
	
	String convertActivityCommentsToString()
	{
		if(activityList.isEmpty())
			return "";
		else
		{
			StringBuffer activityBuffer = new StringBuffer(getEncodedActivityComment(activityList.get(0)));
			for(int i=1; i < activityList.size(); i++)
				activityBuffer.append("_" + getEncodedActivityComment(activityList.get(i)));
			
			return activityBuffer.toString();
		}
	}
	
	String getEncodedActivityComment(VolunteerActivity va)
	{
		if(va.getID() < 10) { return "000" + Integer.toString(va.getID()) + va.getComment(); }
		else if(va.getID() < 100) { return "00" + Integer.toString(va.getID()) + va.getComment(); }
		else if(va.getID() < 1000) { return "0" + Integer.toString(va.getID()) + va.getComment(); }
		else { return Integer.toString(va.getID()) + va.getComment(); }
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {Integer.toString(id), Long.toString(geniusID), drvNum, firstName, lastName, 
						houseNum, street, unit, city, zipCode, email, homePhone, cellPhone, comment,
						convertActivityIDListToString(), organization, convertActivityCommentsToString(), 
						Integer.toString(qty), Integer.toString(delAssigned), Integer.toString(warehouseSignIns),
						Long.toString(dateChanged.getTimeInMillis()), changedBy,  Integer.toString(slPos),
						slMssg, slChangedBy};
		
		return row;
	}
}
