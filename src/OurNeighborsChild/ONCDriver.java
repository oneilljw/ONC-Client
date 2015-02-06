package OurNeighborsChild;

//import java.io.Serializable;
import java.util.Date;

public class ONCDriver extends ONCEntity
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
	private String drLicense;
	private String car;
	private int delAssigned; 
	
	public ONCDriver(int driverid, String drvNum, String fName, String lName, String email, String hNum, 
						String street, String unit, String city, String zipcode, 
						String homePhone, String cellPhone, String drLicense,
						String car, Date today, String changedBy)
	{
		super(driverid, new Date(), changedBy, STOPLIGHT_OFF, "Driver created", changedBy);
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
		this.drLicense = drLicense;
		this.car = car;
		this.delAssigned = 0;		
	}
	
	public ONCDriver(String[] nextLine, int driverid, Date today, String changedBy)
	{
		super(driverid, new Date(), changedBy, STOPLIGHT_OFF, "Driver created", changedBy);
		
		drvNum = "N/A";
		
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
			this.hNum = nextLine[11].split(" ", 2)[0];
			this.street = nextLine[11].split(" ", 2)[1];;
		}
		
		this.unit = "";
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
		this.drLicense = "";
		this.car = "";
		this.delAssigned = 0;
	}
	
	ONCDriver(int id)
	{
		//constructor used when adding a new entity
		super(id, new Date(), "", STOPLIGHT_OFF, "Driver created", "");
		delAssigned = 0;
	}
	
	public ONCDriver(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[15]), nextLine[18],
				Integer.parseInt(nextLine[16]), nextLine[17], nextLine[18]);
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
		drLicense = getDBString(nextLine[12]);
		car = getDBString(nextLine[13]);
		delAssigned = Integer.parseInt(nextLine[14]); 
	}
	
	public ONCDriver(ONCDriver d)	//copy constructor
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
		drLicense = d.drLicense;
		car = d.car;
		delAssigned = d.delAssigned; 
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
	public String getDrLicense() { return drLicense; }
	public String getCar() { return car; }
	public int getDelAssigned() { return delAssigned; }
	
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
	public void setDrLicense(String drl) { this.drLicense = drl; }
	public void setCar(String c) { this.car = c; }
	public void setDelAssigned(int da) { delAssigned = da; }
	
	public void incrementDeliveryCount(int count) { delAssigned += count; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {Integer.toString(id), drvNum, fName, lName, hNum, street, unit, city, zipcode,
						email, homePhone, cellPhone, drLicense, car, Integer.toString(delAssigned),
						Long.toString(dateChanged.getTimeInMillis()), Integer.toString(slPos),
						slMssg, slChangedBy};
		
		return row;
	}
}
