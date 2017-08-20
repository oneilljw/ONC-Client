package ourneighborschild;

//import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ONCVolunteer extends ONCEntity implements Comparable<ONCVolunteer>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5277718159822649083L;
	
	private String drvNum;
	private String fName;
	private String lName;
	private String hNum;
	private String street;
	private String unit;
	private String city;
	private String zipcode;
	private String email;
	private String homePhone;
	private String cellPhone;
	private String comment;
	private List<VolunteerActivity>  activityList;
	private String group;
	private int qty;
	private int delAssigned;
	private int signIns;
	
	public ONCVolunteer(int driverid, String drvNum, String fName, String lName, String email, String hNum, 
						String street, String unit, String city, String zipcode, 
						String homePhone, String cellPhone,
						String qty, String group, String comment, 
						List<VolunteerActivity> actList, Date today, String changedBy)
	{
		super(driverid, new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		this.drvNum = drvNum;
		this.fName = fName;
		this.lName = lName;
		this.hNum = hNum;
		this.email = email;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.zipcode = zipcode;
		this.homePhone = homePhone;
		this.cellPhone = cellPhone;
		this.comment = comment;
		this.activityList = actList;
		this.group = group;
		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.signIns = 0;
	}
	
	public ONCVolunteer(int driverid, String drvNum, String fName, String lName, String email, String hNum, 
			String street, String unit, String city, String zipcode, 
			String homePhone, String cellPhone, String qty, List<VolunteerActivity> actList, String group,
			String comment, Date today, String changedBy)
	{
		super(driverid, new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		this.drvNum = drvNum;
		this.fName = fName;
		this.lName = lName;
		this.hNum = hNum;
		this.email = email;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.zipcode = zipcode;
		this.homePhone = homePhone;
		this.cellPhone = cellPhone;
		this.comment = comment;
		this.activityList = actList;
		this.group = group;
		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.signIns = 0;
	}
	
	public ONCVolunteer(String[] nextLine, Date today, String changedBy, List<Integer> activityList)
	{
		super(-1, new Date(), changedBy, STOPLIGHT_OFF, "Volunteer added", changedBy);
		
		drvNum = "N/A";
		qty = nextLine[3].isEmpty() ? 1: Integer.parseInt(nextLine[3]);
		
		if(nextLine[6].isEmpty())
			fName = "No First Name";
		else
			this.fName = nextLine[6];
		
		if(nextLine[7].isEmpty())
			lName = "No Last Name";		
		else
			this.lName = nextLine[7];
		
		this.email = nextLine[8];
		
		if(nextLine[11].isEmpty())
		{
			this.hNum = "";
			this.street = "";
		}
		else
		{
			String[] addressParts = nextLine[11].split(" ", 2);
			if(addressParts.length == 1)
			{
				this.hNum = "";
				this.street = addressParts[0];
			}
			else
			{
				this.hNum = addressParts[0];
				this.street = addressParts[1];
			}
		}
		
		this.unit = nextLine[12];
		this.city = nextLine[13];
		this.zipcode = nextLine[15];
		if(nextLine[18].contains("Home"))
		{
			this.homePhone = nextLine[17];
			this.cellPhone = "";
		}
		else if(nextLine[18].contains("Mobile"))
		{
			this.homePhone = "";
			this.cellPhone = nextLine[17];
		}
		else
		{
			this.homePhone = "";
			this.cellPhone = "";
		}
		this.activityList = new LinkedList<VolunteerActivity>();
		this.group = "Self";
//		this.comment = nextLine[9];
		this.delAssigned = 0;
		this.signIns = 0;
	}
	
	ONCVolunteer(int id, String changedBy)
	{
		//constructor used when adding a new entity
		super(id, new Date(), changedBy, STOPLIGHT_OFF, "Volunteer Added", changedBy);
		qty = 1;
		delAssigned = 0;
		signIns = 0;
	}
	
	public ONCVolunteer(String[] nextLine, List<VolunteerActivity> volActList)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[19]), nextLine[20],
				Integer.parseInt(nextLine[21]), nextLine[22], nextLine[23]);
		drvNum = getDBString(nextLine[1]);
		fName = getDBString(nextLine[2]);
		lName = getDBString(nextLine[3]);
		hNum = getDBString(nextLine[4]);
		street = getDBString(nextLine[5]);
		unit = getDBString(nextLine[6]);
		city = getDBString(nextLine[7]);;
		zipcode = getDBString(nextLine[8]);
		email = getDBString(nextLine[9]);
		homePhone = getDBString(nextLine[10]);
		cellPhone = getDBString(nextLine[11]);
		comment = getDBString(nextLine[12]);
		activityList = volActList;
		group = getDBString(nextLine[14]);
		qty = nextLine[15].isEmpty() ? 0 : Integer.parseInt(nextLine[16]);;
		delAssigned = nextLine[17].isEmpty() ? 0 : Integer.parseInt(nextLine[17]);;
		signIns = nextLine[18].isEmpty() ? 0 : Integer.parseInt(nextLine[18]);
	}
	
	public ONCVolunteer(ONCVolunteer v)	//copy constructor
	{
		super(v.getID(), v.getDateChanged(), v.getChangedBy(), v.getStoplightPos(),
				v.getStoplightMssg(), v.getStoplightChangedBy());
		drvNum = v.drvNum;
		fName = v.fName;
		lName = v.lName;
		hNum = v.hNum;
		street = v.street;
		unit = v.unit;
		city = v.city;
		zipcode = v.zipcode;
		email = v.email;
		homePhone = v.homePhone;
		cellPhone = v.cellPhone;
		comment = v.comment;
		
		this.activityList = new ArrayList<VolunteerActivity>();
		for(VolunteerActivity activity : v.activityList)
			this.activityList.add(activity);
		
		group = v.group;
		qty = v.qty;
		delAssigned = v.delAssigned; 
		signIns = v.signIns;
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public String getDrvNum() { return drvNum; }
	public String getfName() { return fName; }
	public String getlName() { return lName; }
	public String gethNum() { return hNum; }
	public String getCellPhone() { return cellPhone; }
	public String getStreet() { return street; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipcode() { return zipcode; }
	public String getEmail() { return email; }
	public String getHomePhone() { return homePhone; }
	public String getComment() { return comment; }
	public String getGroup() { return group; }
	public int getQty() { return qty; }
	public int getDelAssigned() { return delAssigned; }
	public int getSignIns() { return signIns; }
	public List<VolunteerActivity> getActivityList() { return activityList; }
	public String getChangedBy() { return changedBy; }
	
	//setters
	public void setDrvNum(String drvNum) { this.drvNum = drvNum; }
	public void setfName(String fName) { this.fName = fName; }
	public void setlName(String lName) { this.lName = lName; }
	public void sethNum(String hNum) {this.hNum = hNum; }
	public void setStreet(String street) { this.street = street; }
	public void setUnit(String unit) { this.unit = unit; }
	public void setCity(String city) { this.city = city; }
	public void setZipcode(String zipcode) {this.zipcode = zipcode; }
	public void setEmail(String em) { this.email = em; }
	public void setHomePhone(String homePhone) { this.homePhone = homePhone; }
	public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }
	public void setComment(String comment) { this.comment = comment; }
	public void setActivityList(List<VolunteerActivity> list) { this.activityList = list; }
	public void setGroup(String drl) { this.group = drl; }
	public void setQtyd(int qty) { this.qty = qty; }
	public void setDelAssigned(int da) { delAssigned = da; }
	public void setSignIns(int si) { signIns = si; }
	public void setChangedBy(String cb) { changedBy = cb; }
	
	
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
	
	public boolean doesCellPhoneMatch(String cell)
	{
		//first, remove any non digit characters
		return cell.replaceAll("[\\D]", "").equals(cellPhone.replaceAll("[\\D]", ""));
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
		String[] row = {Integer.toString(id), drvNum, fName, lName, hNum, street, unit, city, zipcode,
						email, homePhone, cellPhone, comment,
						convertActivityIDListToString(), group, convertActivityCommentsToString(), 
						Integer.toString(qty), Integer.toString(delAssigned), Integer.toString(signIns),
						Long.toString(dateChanged.getTimeInMillis()), changedBy,  Integer.toString(slPos),
						slMssg, slChangedBy};
		
		return row;
	}

    public boolean equals(Object o)
    {
        if (!(o instanceof ONCVolunteer))
            return false;
        
        ONCVolunteer v = (ONCVolunteer) o;
        return v.fName.equals(this.fName) && v.lName.equals(this.lName) && 
        		v.email.equals(this.email);
    }
    
	@Override
    public int hashCode() 
	{
        int hash = 1;
        hash = hash * 17 + (fName == null ? 0 : fName.hashCode());
        hash = hash * 31 + (lName == null ? 0 : lName.hashCode());
        hash = hash * 13 + (email == null ? 0 : email.hashCode());
        return hash;
    }
	
	public int compareTo(ONCVolunteer v) 
	{
        int lastCmp = lName.compareTo(v.lName);
        return (lastCmp != 0 ? lastCmp : fName.compareTo(v.fName));
    }
}
