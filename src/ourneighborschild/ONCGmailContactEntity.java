package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public abstract class ONCGmailContactEntity extends ONCEntity 
{
	/**
	 * Implements an ONC Entity that can be exported for Gmail contacts import
	 */
	private static final long serialVersionUID = 1L;
	private static final int GOOGLE_CONTACT_CSV_RECORD_LENGTH = 58;
	
	protected String firstName;
	protected String lastName;
	protected String email;
	protected String homePhone;
	protected String cellPhone;
	protected String houseNum;
	protected String street;
	protected String unit;
	protected String city;
	protected String zipCode;
	protected String comment;
	protected String organization;
	
	public ONCGmailContactEntity(int id, String fn, String ln, String email, String homePhone, 
			String cellPhone, String houseNum, String street, String unit, String city, String zipCode,
			String comment, String organization, Date today, String changedBy, int slpos,
			String slmssg, String slchgby) 
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.firstName = fn;
		this.lastName = ln;
		this.email = email;
		this.homePhone = homePhone;
		this.cellPhone = cellPhone;
		this.houseNum = houseNum;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.zipCode = zipCode;
		this.comment = comment;
		this.organization = organization;
	}
	
	public ONCGmailContactEntity(int id, String fn, String ln, String email, String homePhone, 
			String cellPhone, String houseNum, String street, String unit, String city, String zipCode,
			String comment, String organization, long dateInMillis, String changedBy, int slpos,
			String slmssg, String slchgby) 
	{
		super(id, dateInMillis, changedBy, slpos, slmssg, slchgby);
		this.firstName = fn;
		this.lastName = ln;
		this.email = email;
		this.homePhone = homePhone;
		this.cellPhone = cellPhone;
		this.houseNum = houseNum;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.zipCode = zipCode;
		this.comment = comment;
		this.organization = organization;
	}
	
	//copy constructor
	public ONCGmailContactEntity(ONCGmailContactEntity ce)
	{
		super(ce.id, ce.dateChanged, ce.changedBy, ce.slPos, ce.slMssg, ce.slChangedBy);
		this.firstName = ce.firstName;
		this.lastName = ce.lastName;
		this.email = ce.email;
		this.homePhone = ce.homePhone;
		this.cellPhone = ce.cellPhone;
		this.houseNum = ce.houseNum;
		this.street = ce.street;
		this.unit = ce.unit;
		this.city = ce.city;
		this.zipCode = ce.zipCode;
		this.comment = ce.comment;
		this.organization = ce.organization;
	}
	
	public ONCGmailContactEntity(int id, String fn, String ln, String email, String homePhone, 
			String cellPhone, String houseNum, String street, String unit, String city, String zipCode,
			String comment, String organization, Calendar dateChanged, String changedBy, int slpos,
			String slmssg, String slchgby) 
	{
		super(id, dateChanged, changedBy, slpos, slmssg, slchgby);
		this.firstName = fn;
		this.lastName = ln;
		this.email = email;
		this.homePhone = homePhone;
		this.cellPhone = cellPhone;
		this.houseNum = houseNum;
		this.street = street;
		this.unit = unit;
		this.city = city;
		this.zipCode = zipCode;
		this.comment = comment;
		this.organization = organization;
	}
	
	public ONCGmailContactEntity(String[] nextLine, Date today, String slMssg, String changedBy)
	{
		
		super(-1, new Date(), changedBy, STOPLIGHT_OFF, slMssg, changedBy);
		
//		public ONCGmailContactEntity(int id, String fn, String ln, String email, String homePhone, 
//		String cellPhone, String houseNum, String street, String unit, String city, String zipCode,
//		String comment, String organization, Date today, String changedBy, int slpos,
//		String slmssg, String slchgby) 
		
//		drvNum = "N/A";
//		qty = nextLine[3].isEmpty() ? 1: Integer.parseInt(nextLine[3]);
		
		if(nextLine[6].isEmpty())
			firstName = "No First Name";
		else
			this.firstName = nextLine[6];
		
		if(nextLine[7].isEmpty())
			lastName = "No Last Name";		
		else
			this.lastName = nextLine[7];
		
		this.email = nextLine[8];
		
		if(nextLine[11].isEmpty())
		{
			this.houseNum = "";
			this.street = "";
		}
		else
		{
			String[] addressParts = nextLine[11].split(" ", 2);
			if(addressParts.length == 1)
			{
				this.houseNum = "";
				this.street = addressParts[0];
			}
			else
			{
				this.houseNum = addressParts[0];
				this.street = addressParts[1];
			}
		}
		
		this.unit = nextLine[12];
		this.city = nextLine[13];
		this.zipCode = nextLine[15];
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
//		this.activityList = new LinkedList<VolunteerActivity>();
		this.organization = "Self";
//		this.comment = nextLine[9];
//		this.delAssigned = 0;
//		this.signIns = 0;
	}
	
	//getters
	public String getFirstName() { return firstName; }
	public String getLastName() { return lastName; }
	public String getEmail() { return email; }
	public String getHomePhone() { return homePhone; }
	public String getCellPhone() { return cellPhone; }
	public String getHouseNum() { return houseNum; }
	public String getStreet() { return street; }
	public String getUnit() { return unit; }
	public String getCity() { return city; }
	public String getZipCode() { return zipCode; }
	public String getComment() { return comment; }
	public String getOrganization() { return organization; }
	
	//setters
	public void setFirstName(String fn) { this.firstName = fn; }
	public void setLastName(String ln) { this.lastName = ln; }
	public void setEmail(String email ) { this.email = email; }
	public void setHomePhone(String homePhone) { this.homePhone = homePhone; }
	public void setCellPhone(String cellPhone ) { this.cellPhone = cellPhone; }
	public void setHouseNum(String houseNum ) { this.houseNum = houseNum; }
	public void setStreet(String street ) { this.street = street; }
	public void setUnit(String unit ) { this.unit = unit; }
	public void setCity(String city ) { this.city = city; }
	public void setZipCode(String zipCode ) { this.zipCode = zipCode; }
	public void setComment(String comment ) { this.comment = comment; }
	public void setOrganization(String organization ) { this.organization = organization; }
	
	public boolean doesCellPhoneMatch(String cell)
	{
		//first, remove any non digit characters
		return cell.replaceAll("[\\D]", "").equals(cellPhone.replaceAll("[\\D]", ""));
	}
	
	public static String[] getGmailContactCSVHeader()
	{
		return new String[] {"Name","Given Name","Additional Name","Family Name",
				"Yomi Name", "Given Name Yomi","Additional Name Yomi","Family Name Yomi",
				"Name Prefix", "Name Suffix","Initials","Nickname","Short Name","Maiden Name",
				"Birthday","Gender","Location","Billing Information","Directory Server","Mileage",
    			"Occupation","Hobby","Sensitivity","Priority","Subject","Notes",
    			"Group Membership","E-mail 1 - Type","E-mail 1 - Value",
    			"E-mail 2 - Type","E-mail 2 - Value",
    			"Phone 1 - Type","Phone 1 - Value","Phone 2 - Type","Phone 2 - Value",
    			"Phone 3 - Type","Phone 3 - Value","Phone 4 - Type","Phone 4 - Value",
    			"Address 1 - Type","Address 1 - Formatted","Address 1 - Street",
    			"Address 1 - City","Address 1 - PO Box","Address 1 - Region",
    			"Address 1 - Postal Code","Address 1 - Country","Address 1 - Extended Address",
    			"Organization 1 - Type","Organization 1 - Name","Organization 1 - Yomi Name",
    			"Organization 1 - Title","Organization 1 - Department","Organization 1 - Symbol",
    			"Organization 1 - Location","Organization 1 - Job Description","Website 1 - Type",
    			"Website 1 - Value"};
	}
	
	public String[] getGoogleContactExportRow(String groupName)
	{
		//create a row of all empty strings
		String[] gmailContactRow = new String[GOOGLE_CONTACT_CSV_RECORD_LENGTH];
		for(int i=0; i< gmailContactRow.length; i++)
			gmailContactRow[i] = "";
		
		//populate the correct cells with volunteer info
		gmailContactRow[0] = firstName + " " + lastName;
		gmailContactRow[1] = firstName;
		gmailContactRow[3] = lastName;
		gmailContactRow[25] = comment;
		gmailContactRow[26] = groupName;
		gmailContactRow[27] = "Home";
		gmailContactRow[28] = email;
		gmailContactRow[31] = "Home";
		gmailContactRow[32] = homePhone;
		gmailContactRow[33] = "Mobile";
		gmailContactRow[34] = cellPhone;
		gmailContactRow[39] = "Home";
		gmailContactRow[41] = houseNum + " " + street + " " + unit;
		gmailContactRow[42] = city;
		gmailContactRow[45] = zipCode;
		gmailContactRow[49] = "ONC Volunteer: " + " " + organization;
		
		return gmailContactRow;
	}
	
    public boolean equals(Object o)
    {
        if (!(o instanceof ONCVolunteer))
            return false;
        
        ONCVolunteer v = (ONCVolunteer) o;
        return v.firstName.equals(this.firstName) && v.lastName.equals(this.lastName) && 
        		v.email.equals(this.email);
    }
    
	@Override
    public int hashCode() 
	{
        int hash = 1;
        hash = hash * 17 + (firstName == null ? 0 : firstName.hashCode());
        hash = hash * 31 + (lastName == null ? 0 : lastName.hashCode());
        hash = hash * 13 + (email == null ? 0 : email.hashCode());
        return hash;
    }
	
	public int compareTo(ONCVolunteer v) 
	{
        int lastCmp = lastName.compareTo(v.lastName);
        return (lastCmp != 0 ? lastCmp : firstName.compareTo(v.firstName));
    }	
}
