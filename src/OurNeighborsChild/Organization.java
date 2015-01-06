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
	private GiftCollection collection;
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
	private int orn_rec;
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
	
	Organization(int orgid, String createdBy)
	{
		//constructor used when adding a new organization
		super(orgid, new Date(), createdBy, STOPLIGHT_OFF, "Partner created", createdBy);
		status = 0;
		type = 0;
		collection = GiftCollection.Unknown;
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
		orn_rec = 0;
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
	
	Organization(int orgid, String name, String createdBy)
	{
		//constructor used when adding creating a non-assigned organization for wish filter and 
		//selection lists
		super(orgid, new Date(), createdBy, STOPLIGHT_RED, "Non-Assigned Partner created", createdBy);
		status = 0;
		type = 0;
		collection = GiftCollection.Unknown;
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
		orn_rec = 0;
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
	
	Organization(int orgid, Date date, String changedBy, int slPos, String slMssg, String slChangedBy,
			int status, int type, GiftCollection collection, String name, String ornDelivery, int streetnum, String streetname,
			String unit, String city, String zipcode, String phone, int orn_req, String other, 
			String deliverTo, String specialNotes, String contact, String contact_email,
			String contact_phone, String contact2, String contact2_email, String contact2_phone)
	{
		//constructor used when adding a new organization
		super(orgid, date, changedBy, STOPLIGHT_OFF, slMssg, slChangedBy);
		this.status = status;
		this.type = type;
		this.collection = collection;
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
		this.orn_rec = 0;
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
		this.status = o.status;
		this.type = o.type;
		this.collection = o.collection;
		this.name = o.name;
		this.ornamentDelivery = o.ornamentDelivery;
		this.streetnum = o.streetnum;
		this.streetname = o.streetname;
		this.unit = o.unit;
		this.city = o.city;
		this.zipcode = o.zipcode;
		this.region = o.region;
		this.phone = o.phone;
		this.orn_req = o.orn_req;
		this.orn_assigned = o.orn_assigned;
		this.orn_rec = o.orn_rec;
		this.other = o.other;
		this.confirmed = "";
		this.deliverTo = o.deliverTo;
		this.specialNotes = o.specialNotes;
		this.contact = o.contact;
		this.contact_email = o.contact_email;
		this.contact_phone = o.contact_phone;
		this.contact2 = o.contact2;
		this.contact2_email = o.contact2_email;
		this.contact2_phone = o.contact2_phone;
		this.pyRequested = 0;
		this.pyAssigned = 0;
		this.pyReceived = 0;
	}
	
	//Constructor for import from .csv
	public Organization(String[] nextLine)	
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[25]), nextLine[26],
				Integer.parseInt(nextLine[27]), nextLine[28], nextLine[29]);
		status = Integer.parseInt(nextLine[1]);
		type = Integer.parseInt(nextLine[2]);
		collection = nextLine[3].isEmpty() ? GiftCollection.Unknown : GiftCollection.valueOf(nextLine[3]);
		name = getDBString(nextLine[4]);
		ornamentDelivery = getDBString(nextLine[5]);
		streetnum = nextLine[6].isEmpty() ? 0 : Integer.parseInt(nextLine[6]);
		streetname = getDBString(nextLine[7]);
		unit = getDBString(nextLine[8]);
		city =getDBString(nextLine[9]);
		zipcode = getDBString(nextLine[10]);
		region = nextLine[11].isEmpty() ? 0 : Integer.parseInt(nextLine[11]);
		phone = getDBString(nextLine[12]);
		orn_req = nextLine[13].isEmpty() ? 0 : Integer.parseInt(nextLine[13]);
		orn_assigned = nextLine[14].isEmpty() ? 0 : Integer.parseInt(nextLine[14]);
		orn_rec = nextLine[15].isEmpty() ? 0 : Integer.parseInt(nextLine[15]);
		other = getDBString(nextLine[16]);
		deliverTo = getDBString(nextLine[17]);
		specialNotes = getDBString(nextLine[18]);
		contact = getDBString(nextLine[19]);
		contact_email = getDBString(nextLine[20]);
		contact_phone = getDBString(nextLine[21]);
		contact2 = getDBString(nextLine[22]);
		contact2_email = getDBString(nextLine[23]);
		contact2_phone = getDBString(nextLine[24]);
		pyRequested = nextLine[30].isEmpty() ? 0 : Integer.parseInt(nextLine[30]);
		pyAssigned = nextLine[31].isEmpty() ? 0 : Integer.parseInt(nextLine[31]);
		pyReceived = nextLine[32].isEmpty() ? 0 : Integer.parseInt(nextLine[32]);
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public int getStatus()	{ return status; }
	int getType()		{ return type; }
	GiftCollection getGiftCollectionType() {return collection; }
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
	public int getNumberOfOrnammentsReceived() { return orn_rec; }
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
	void setGiftCollectionType(GiftCollection gc)	{ collection = gc; }
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
	public void setNumberOfOrnamentsReceived(int n)	{ orn_rec = n; }
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
	
	public int incrementOrnReceived() { return ++orn_rec; }
	public int decrementOrnReceived()
	{
		if(orn_rec > 0)
			orn_rec--;
		return orn_rec;
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
						Integer.toString(orn_assigned), Integer.toString(orn_rec), other, 
						deliverTo, specialNotes, contact, contact_email, contact_phone, 
						contact2, contact2_email, contact2_phone, 
						Long.toString(dateChanged.getTimeInMillis()), changedBy, Integer.toString(slPos),
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
