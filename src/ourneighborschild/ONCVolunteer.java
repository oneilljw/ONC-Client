package ourneighborschild;

//import java.io.Serializable;
import java.util.Date;

public class ONCVolunteer extends ONCEntity implements Comparable<ONCVolunteer>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5277718159822649083L;
	private static final int HEXADECIMAL = 16;
	
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
	private int	   activityCode;
	private String group;
	private String comment;
	private int qty;
	private int delAssigned;
	private int signIns;
	
	public ONCVolunteer(int driverid, String drvNum, String fName, String lName, String email, String hNum, 
						String street, String unit, String city, String zipcode, 
						String homePhone, String cellPhone, String qty, String zActivityCode, String group,
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
		this.activityCode = convertActivityCode(zActivityCode, HEXADECIMAL);
		this.group = group;
		this.comment = comment;
		this.qty = qty.isEmpty() ? 0 : Integer.parseInt(qty);
		this.delAssigned = 0;
		this.signIns = 0;
	}
	
	//used when importing volunteers from Sign-Up Genius .csv export
	public ONCVolunteer(String[] nextLine, Date today, String changedBy, int activityCode)
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
		this.activityCode = activityCode;
		this.group = "Self";
		this.comment = nextLine[9];
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
	
	public ONCVolunteer(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[18]), nextLine[19],
				Integer.parseInt(nextLine[20]), nextLine[21], nextLine[22]);
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
		activityCode = nextLine[12].isEmpty() ? 0 : Integer.parseInt(nextLine[12]);
		group = getDBString(nextLine[13]);
		comment = getDBString(nextLine[14]);
		qty = nextLine[15].isEmpty() ? 0 : Integer.parseInt(nextLine[15]);;
		delAssigned = nextLine[16].isEmpty() ? 0 : Integer.parseInt(nextLine[16]);;
		signIns = nextLine[17].isEmpty() ? 0 : Integer.parseInt(nextLine[17]);
	}
	
	public ONCVolunteer(ONCVolunteer d)	//copy constructor
	{
		super(d.getID(), d.getDateChanged(), d.getChangedBy(), d.getStoplightPos(),
				d.getStoplightMssg(), d.getStoplightChangedBy());
		drvNum = d.drvNum;
		fName = d.fName;
		lName = d.lName;
		hNum = d.hNum;
		street = d.street;
		unit = d.unit;
		city = d.city;
		zipcode = d.zipcode;
		email = d.email;
		homePhone = d.homePhone;
		cellPhone = d.cellPhone;
		activityCode = d.activityCode;
		group = d.group;
		comment = d.comment;
		qty = d.qty;
		delAssigned = d.delAssigned; 
		signIns = d.signIns;
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
	public String getGroup() { return group; }
	public String getComment() { return comment; }
	public int getQty() { return qty; }
	public int getDelAssigned() { return delAssigned; }
	public int getSignIns() { return signIns; }
	public int getActivityCode() { return activityCode; }
	public String getChangedBy() { return changedBy; }
	
	String getActivities()
	{
		if(activityCode > 0)
		{
			StringBuffer sb = new StringBuffer();
			
			for(int mask = 1; mask <= ActivityCode.lastCode(); mask = mask << 1)
			{
				if((mask & activityCode) > 0)
					sb.append(ActivityCode.getActivity(mask).activity() + "\n");
			}
			
			//remove the last newline
			String result = sb.toString();
			return result.substring(0, result.length()-1);
		}
		else
			return "None";
	}
	
	int getNumberOfActivities()
	{
		int nActivities = 0;
		
		for(int actMask = 1; actMask <= ActivityCode.lastCode(); actMask = actMask << 1)
			if((activityCode & actMask) > 0)
				nActivities++;
		
		return nActivities;
	}
	
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
	public void setActivityCode(int activityCode) { this.activityCode = activityCode; }
	public void setGroup(String drl) { this.group = drl; }
	public void setComment(String c) { this.comment = c; }
	public void setQtyd(int qty) { this.qty = qty; }
	public void setDelAssigned(int da) { delAssigned = da; }
	public void setSignIns(int si) { signIns = si; }
	public void setChangedBy(String cb) { changedBy = cb; }
	
	public void incrementDeliveryCount(int count) { delAssigned += count; }
	
	/***
	 * takes a binary string parameter, verifies it only contains digits returns the corresponding integer, else return 0
	****/
	int convertActivityCode(String zActivityCode, int radix)
	{
		return isNumeric(zActivityCode) ? Integer.parseInt(zActivityCode, radix) : 0;
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {Integer.toString(id), drvNum, fName, lName, hNum, street, unit, city, zipcode,
						email, homePhone, cellPhone, Integer.toString(activityCode), group, comment, 
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
