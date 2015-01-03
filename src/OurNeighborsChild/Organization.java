package OurNeighborsChild;

import java.util.Date;

public class Organization extends ONCEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7854045836478089523L;
	
	private int status;
	private int type;
	private String name;
	private String ornamentDelivery;
	private int streetnum;
	private String streetname;
	private String unit;
	private String city;
	private String zipcode;
	private int region;
	private String phone;
	private int orn_req;
	private int orn_assigned;
	private String other;
	private String confirmed;
	private String deliverTo;
	private String specialNotes;
	private String contact;
	private String contact_email;
	private String contact_phone;
	private String contact2;
	private String contact2_email;
	private String contact2_phone;
	private int pyRequested;
	private int pyAssigned;
	private int pyReceived;
	
	Organization(int orgid)
	{
		//constructor used when adding a new organization
		super(orgid, new Date(), "", STOPLIGHT_OFF, "Partner created", "");
		status = 0;
		type = 0;
		name = "";
		ornamentDelivery = "";
		streetnum = 0;
		streetname = "";
		unit = "";
		city = "";
		zipcode = "";
		region = 0;
		phone = "";
		orn_req = 0;
		orn_assigned = 0;
		other = "";
		confirmed = "";
		deliverTo = "";
		specialNotes = "";
		contact = "";
		contact_email = "";
		contact_phone = "";
		contact2 = "";
		contact2_email = "";
		contact2_phone = "";
		pyRequested = 0;
		pyAssigned = 0;
		pyReceived = 0;
	}
	
	Organization(int orgid, String name)
	{
		//constructor used when adding creating a non-assigned organization for wishes
		super(orgid, new Date(), "", STOPLIGHT_RED, "Non-Assigned Partner created", "");
		status = 0;
		type = 0;
		this.name = name;
		ornamentDelivery = "";
		streetnum = 0;
		streetname = "";
		unit = "";
		city = "";
		zipcode = "";
		region = 0;
		phone = "";
		orn_req = 0;
		orn_assigned = 0;
		other = "";
		confirmed = "";
		deliverTo = "";
		specialNotes = "";
		contact = "";
		contact_email = "";
		contact_phone = "";
		contact2 = "";
		contact2_email = "";
		contact2_phone = "";
		pyRequested = 0;
		pyAssigned = 0;
		pyReceived = 0;
	}
	
	Organization(int orgid, Date date, int slPos, String slMssg, String slChangedBy,
			int status, int type, String name, String ornDelivery, int streetnum, String streetname,
			String unit, String city, String zipcode, String phone, int orn_req, String other, 
			String deliverTo, String specialNotes, String contact, String contact_email,
			String contact_phone, String contact2, String contact2_email, String contact2_phone)
	{
		//constructor used when adding a new organization
		super(orgid, date, "", STOPLIGHT_OFF, slMssg, slChangedBy);
		this.status = status;
		this.type = type;
		this.name = name;
		this.ornamentDelivery = ornDelivery;
		this.streetnum = streetnum;
		this.streetname = streetname;
		this.unit = unit;
		this.city = city;
		this.zipcode = zipcode;
		this.region = 0;
		this.phone = phone;
		this.orn_req = orn_req;
		this.orn_assigned = 0;
		this.other = other;
		this.confirmed = "";
		this.deliverTo = deliverTo;
		this.specialNotes = specialNotes;
		this.contact = contact;
		this.contact_email = contact_email;
		this.contact_phone = contact_phone;
		this.contact2 = contact2;
		this.contact2_email = contact2_email;
		this.contact2_phone = contact2_phone;
		this.pyRequested = 0;
		this.pyAssigned = 0;
		this.pyReceived = 0;
	}
	
	//copy constructor - makes a copy of the organanization
	public Organization(Organization o)
	{
		super(o.getID(), o.getDateChanged(), o.getChangedBy(), o.getStoplightPos(),
				o.getStoplightMssg(), o.getStoplightChangedBy());
		this.status = o.getStatus();
		this.type = o.getType();
		this.name = o.getName();
		this.ornamentDelivery = o.getOrnamentDelivery();
		this.streetnum = o.getStreetnum();
		this.streetname = o.getStreetname();
		this.unit = o.getUnit();;
		this.city = o.getCity();
		this.zipcode = o.getZipcode();
		this.region = o.getRegion();
		this.phone = o.getPhone();
		this.orn_req = o.getNumberOfOrnamentsRequested();
		this.orn_assigned = o.getNumberOfOrnammentsAssigned();
		this.other = o.getOther();
		this.confirmed = "";
		this.deliverTo = o.getDeliverTo();
		this.specialNotes = o.getSpecialNotes();
		this.contact = o.getContact();
		this.contact_email = o.getContact_email();
		this.contact_phone = o.getContact_phone();
		this.contact2 = o.getContact2();
		this.contact2_email = o.getContact2_email();
		this.contact2_phone = o.getContact2_phone();
		this.pyRequested = 0;
		this.pyAssigned = 0;
		this.pyReceived = 0;
	}
	
	//Constructor for import from .csv
	public Organization(String[] nextLine)	
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[23]), "",
				Integer.parseInt(nextLine[24]), nextLine[25], nextLine[26]);
		status = Integer.parseInt(nextLine[1]);
		type = Integer.parseInt(nextLine[2]);
		name = getDBString(nextLine[3]);
		ornamentDelivery = getDBString(nextLine[4]);
		streetnum = Integer.parseInt(nextLine[5]);
		streetname = getDBString(nextLine[6]);
		unit = getDBString(nextLine[7]);
		city =getDBString(nextLine[8]);
		zipcode = getDBString(nextLine[9]);
		region = Integer.parseInt(nextLine[10]);
		phone = getDBString(nextLine[11]);
		orn_req = Integer.parseInt(nextLine[12]);
		orn_assigned = Integer.parseInt(nextLine[13]);
		other = getDBString(nextLine[14]);
		deliverTo = getDBString(nextLine[15]);
		specialNotes = getDBString(nextLine[16]);
		contact = getDBString(nextLine[17]);
		contact_email = getDBString(nextLine[18]);
		contact_phone = getDBString(nextLine[19]);
		contact2 = getDBString(nextLine[20]);
		contact2_email = getDBString(nextLine[21]);
		contact2_phone = getDBString(nextLine[22]);
		pyRequested = Integer.parseInt(nextLine[27]);
		pyAssigned = Integer.parseInt(nextLine[28]);
		pyReceived = Integer.parseInt(nextLine[29]);
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public int getStatus()	{ return status; }
	int getType()		{ return type; }
	public String getName()	{ return name; }
	String getOrnamentDelivery()	{ return ornamentDelivery; }
	public int getStreetnum()	{ return streetnum; }
	public String getStreetname()	{return streetname; }
	String getUnit() { return unit; }
	public String getCity()	{return city; }
	public String getZipcode()	{ return zipcode; }
	int getRegion()	{ return region; }
	String getPhone()	{ return phone; }
	int getNumberOfOrnamentsRequested()	{ return orn_req; }
	public int getNumberOfOrnammentsAssigned() { return orn_assigned; }
	String getOther()	{ return other; }
	String getConfirmed() { return confirmed;}
	String getDeliverTo() { return deliverTo; }
	String getSpecialNotes()	{ return specialNotes; }
	String getContact()	{ return contact; }
	String getContact_email()	{ return contact_email; }
	String getContact_phone()	{ return contact_phone; }
	String getContact2()	{ return contact2; }
	String getContact2_email()	{ return contact2_email; }
	String getContact2_phone()	{ return contact2_phone; }
	int getPriorYearRequested() { return pyRequested; }
	int getPriorYearAssigned() { return pyAssigned; }
	int getPriorYearReceived() { return pyReceived; }
	
	//setters
	public void setStatus(int s)	{ status = s; }
	void setType(int t)		{ type = t; }
	void setName(String n)	{ name = n; }
	void setOrnamentDelivery(String od)	{ ornamentDelivery = od; }
	void setStreetnum(int sn)	{ streetnum = sn; }
	void setStreetname(String sn)	{ streetname = sn; }
	void setUnit(String sn) { unit = sn; }
	void setCity(String c)	{ city = c; }
	void setZipcode(String z)	{ zipcode = z; }
	public void setRegion(int r)	{ region = r; }
	void setPhone(String p)	{ phone = p; }
	public void setNumberOfOrnamentsRequested(int n)	{ orn_req = n; }
	public void setNumberOfOrnamentsAssigned(int n)	{ orn_assigned = n; }
	void setOther(String o)	{ other = o; }
	void setConfirmed(String c) { confirmed = c;}
	void setDeliverTo(String dt) { deliverTo = dt; }
	void setSpecialNotes(String sn)	{ specialNotes = sn; }
	void setContact(String c)	{ contact = c; }
	void setContact_email(String e)	{ contact_email = e; }
	void setContact_phone(String p)	{ contact_phone = p; }
	void setContact2(String c)	{ contact2 = c; }
	void setContact2_email(String e)	{ contact2_email = e; }
	void setContact2_phone(String p)	{ contact2_phone = p; }
	public void setPriorYearRequested(int n) { pyRequested = n; }
	void setPriorYearAssigned(int n) { pyAssigned = n; }
	void setPriorYearReceived(int n) { pyReceived = n; }
	
	
	public int incrementOrnAssigned() { return ++orn_assigned; }
	public int decrementOrnAssigned()
	{
		if(orn_assigned > 0)
			orn_assigned--;
		return orn_assigned;
	}
	
	public int incrementPYAssigned() { return ++pyAssigned; }
	public int incrementPYReceived() { return ++pyReceived; }
	
	String[] getOrgInfoTableRow()
	{
		String[] sorttablerow = {name, phone,
								contact, contact_email, contact_phone,
								contact2, contact2_email, contact2_phone};						 
		return sorttablerow;
	}
	
	public String[] getDBExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(status), Integer.toString(type),
						name, ornamentDelivery, Integer.toString(streetnum), streetname, unit, 
						city, zipcode, Integer.toString(region), phone, Integer.toString(orn_req),
						Integer.toString(orn_assigned), other, deliverTo, specialNotes, 
						contact, contact_email, contact_phone, 
						contact2, contact2_email, contact2_phone, 
						Long.toString(dateChanged.getTimeInMillis()), Integer.toString(slPos),
						slMssg, slChangedBy, Integer.toString(pyRequested), Integer.toString(pyAssigned),
						Integer.toString(pyReceived)};
		return row;
	}
	
	 @Override
	 public String toString()
	 {
	     return name;
	 }
}
