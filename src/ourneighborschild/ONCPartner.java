package ourneighborschild;

public class ONCPartner extends ONCEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7854045836478089523L;
	
	public static final int PARTNER_TYPE_BUSINESS = 1;
	public static final int PARTNER_TYPE_CHURCH = 2;
	public static final int PARTNER_TYPE_SCHOOL = 3;
	public static final int PARTNER_TYPE_CLOTHING = 4;
	public static final int PARTNER_TYPE_COAT = 5;
	public static final int PARTNER_TYPE_ONC_SHOPPER = 6;
	
	public static final int PARTNER_STATUS_CONFIRMED = 5;
	
	private int status;
	private int type;
	private GiftCollectionType collection;
	private String lastName;
	private String houseNum;
	private String street;
	private String unit;
	private String city;
	private String zipCode;
	private int region;
	private String homePhone;
	private int orn_req;
	private int orn_assigned;
	private int orn_delivered;
	private int orn_rec_before;
	private int orn_rec_after;
	private String generalPartnerInfo;
	private String deliverTo;
	private String cyNotes;
	private String contact;
	private String contact_email;
	private String contact_phone;
	private String contact2;
	private String contact2_email;
	private String contact2_phone;
	private int pyRequested;
	private int pyAssigned;
	private int pyDelivered;
	private int pyReceivedBeforeDeadline;
	private int pyReceivedAfterDeadline;
	
	ONCPartner(int orgid, String createdBy)
	{
		//constructor used when adding a new organization
		super(orgid, System.currentTimeMillis(), createdBy, STOPLIGHT_OFF, "Partner created", createdBy);
		status = 0;
		type = 0;
		collection = GiftCollectionType.Unknown;
		lastName = "";
		houseNum = "";
		street = "";
		unit = "";
		city = "";
		zipCode = "";
		region = 0;
		homePhone = "";
		orn_req = 0;
		orn_assigned = 0;
		orn_delivered = 0;
		orn_rec_before = 0;
		orn_rec_after = 0;
		generalPartnerInfo = "";
		deliverTo = "";
		cyNotes = "";
		contact = "";
		contact_email = "";
		contact_phone = "";
		contact2 = "";
		contact2_email = "";
		contact2_phone = "";
		pyRequested = 0;
		pyAssigned = 0;
		pyDelivered = 0;
		pyReceivedBeforeDeadline = 0;
		pyReceivedAfterDeadline = 0;
	}
	
	public ONCPartner(int orgid, String name, String createdBy)
	{
		//constructor used when adding creating a non-assigned organization for wish filter and 
		//selection lists
		super(orgid, System.currentTimeMillis(), createdBy, STOPLIGHT_RED, "Non-Assigned Partner created", createdBy);
		status = 0;
		type = 0;
		collection = GiftCollectionType.Unknown;
		this.lastName = name;
		houseNum = "";
		street = "";
		unit = "";
		city = "";
		zipCode = "";
		region = 0;
		homePhone = "";
		orn_req = 0;
		orn_assigned = 0;
		orn_delivered = 0;
		orn_rec_before = 0;
		orn_rec_after = 0;
		generalPartnerInfo = "";
		deliverTo = "";
		cyNotes = "";
		contact = "";
		contact_email = "";
		contact_phone = "";
		contact2 = "";
		contact2_email = "";
		contact2_phone = "";
		pyRequested = 0;
		pyAssigned = 0;
		pyDelivered = 0;
		pyReceivedBeforeDeadline = 0;
		pyReceivedAfterDeadline = 0;
	}
	
	public ONCPartner(int orgid, long date, String changedBy, int slPos, String slMssg, String slChangedBy,
			int status, int type, GiftCollectionType collection, String name, String streetnum, String streetname,
			String unit, String city, String zipcode, String phone, int orn_req, String other, 
			String deliverTo, String specialNotes, String contact, String contact_email,
			String contact_phone, String contact2, String contact2_email, String contact2_phone)
	{
		//constructor used when adding a new organization
		super(orgid, date, changedBy, STOPLIGHT_OFF, slMssg, slChangedBy);
		this.status = status;
		this.type = type;
		this.collection = collection;
		this.lastName = name;
		this.houseNum = streetnum;
		this.street = streetname;
		this.unit = unit;
		this.city = city;
		this.zipCode = zipcode;
		this.region = 0;
		this.homePhone = formatPhone(phone);
		this.orn_req = orn_req;
		this.orn_assigned = 0;
		this.orn_delivered = 0;
		this.orn_rec_before = 0;
		this.orn_rec_after = 0;
		this.generalPartnerInfo = other;
		this.deliverTo = deliverTo;
		this.cyNotes = specialNotes;
		this.contact = contact;
		this.contact_email = contact_email;
		this.contact_phone = formatPhone(contact_phone);
		this.contact2 = contact2;
		this.contact2_email = contact2_email;
		this.contact2_phone = formatPhone(contact2_phone);
		this.pyRequested = 0;
		this.pyAssigned = 0;
		this.pyDelivered = 0;
		this.pyReceivedBeforeDeadline = 0;
		this.pyReceivedAfterDeadline = 0;
	}
	
	//copy constructor - makes a copy of the partner
	public ONCPartner(ONCPartner o)
	{
		super(o.getID(), o.getTimestamp(), o.getChangedBy(), o.getStoplightPos(),
				o.getStoplightMssg(), o.getStoplightChangedBy());
		this.status = o.status;
		this.type = o.type;
		this.collection = o.collection;
		this.lastName = o.lastName;
		this.houseNum = o.houseNum;
		this.street = o.street;
		this.unit = o.unit;
		this.city = o.city;
		this.zipCode = o.zipCode;
		this.region = o.region;
		this.homePhone = o.homePhone;
		this.orn_req = o.orn_req;
		this.orn_assigned = o.orn_assigned;
		this.orn_delivered = o.orn_delivered;
		this.orn_rec_before = o.orn_rec_before;
		this.orn_rec_after = o.orn_rec_after;
		this.generalPartnerInfo = o.generalPartnerInfo;
		this.deliverTo = o.deliverTo;
		this.cyNotes = o.cyNotes;
		this.contact = o.contact;
		this.contact_email = o.contact_email;
		this.contact_phone = o.contact_phone;
		this.contact2 = o.contact2;
		this.contact2_email = o.contact2_email;
		this.contact2_phone = o.contact2_phone;
		this.pyRequested = o.pyRequested;
		this.pyAssigned = o.pyAssigned;
		this.pyDelivered = o.pyDelivered;
		this.pyReceivedBeforeDeadline = o.pyReceivedBeforeDeadline;
		this.pyReceivedAfterDeadline = o.pyReceivedAfterDeadline;
	}
	
	//Constructor for import from .csv
	public ONCPartner(String[] nextLine)	
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[26]), nextLine[27],
				Integer.parseInt(nextLine[28]), nextLine[29], nextLine[30]);
		status = Integer.parseInt(nextLine[1]);
		type = Integer.parseInt(nextLine[2]);
		collection = nextLine[3].isEmpty() ? GiftCollectionType.Unknown : GiftCollectionType.valueOf(nextLine[3]);
		lastName = getDBString(nextLine[4]);
		houseNum = getDBString(nextLine[5]);
		street = getDBString(nextLine[6]);
		unit = getDBString(nextLine[7]);
		city =getDBString(nextLine[8]);
		zipCode = getDBString(nextLine[9]);
		region = nextLine[10].isEmpty() ? 0 : Integer.parseInt(nextLine[10]);
		homePhone = getDBString(nextLine[11]);
		orn_req = nextLine[12].isEmpty() ? 0 : Integer.parseInt(nextLine[12]);
		orn_assigned = nextLine[13].isEmpty() ? 0 : Integer.parseInt(nextLine[13]);
		orn_delivered = nextLine[14].isEmpty() ? 0 : Integer.parseInt(nextLine[14]);
		orn_rec_before = nextLine[15].isEmpty() ? 0 : Integer.parseInt(nextLine[15]);
		orn_rec_after = nextLine[16].isEmpty() ? 0 : Integer.parseInt(nextLine[16]);
		generalPartnerInfo = getDBString(nextLine[17]);
		deliverTo = getDBString(nextLine[18]);
		cyNotes = getDBString(nextLine[19]);
		contact = getDBString(nextLine[20]);
		contact_email = getDBString(nextLine[21]);
		contact_phone = getDBString(nextLine[22]);
		contact2 = getDBString(nextLine[23]);
		contact2_email = getDBString(nextLine[24]);
		contact2_phone = getDBString(nextLine[25]);
		pyRequested = nextLine[31].isEmpty() ? 0 : Integer.parseInt(nextLine[31]);
		pyAssigned = nextLine[32].isEmpty() ? 0 : Integer.parseInt(nextLine[32]);
		pyDelivered = nextLine[33].isEmpty() ? 0 : Integer.parseInt(nextLine[33]);
		pyReceivedBeforeDeadline = nextLine[34].isEmpty() ? 0 : Integer.parseInt(nextLine[34]);
		pyReceivedAfterDeadline = nextLine[35].isEmpty() ? 0 : Integer.parseInt(nextLine[35]);
	}
	
	/***
	 * Constructor used when importing partners from sign-up genius direct import
	 * @param line
	 * @param changedBy
	 * @param activityList
	 */
	public ONCPartner(SignUpActivity sua, SignUpType signUpType)
	{	
		super(-1, System.currentTimeMillis(), "Lavin, K", STOPLIGHT_OFF, "Sign-Up Genius Partner", "Lavin, K");
		this.status = PARTNER_STATUS_CONFIRMED;
		
		if(signUpType == SignUpType.Clothing)
		{	
			this.type = 4;	//individual
			this.collection = GiftCollectionType.Clothing;
		}
		else if(signUpType == SignUpType.Coat)
		{	
			this.type = 4;	//individual
			this.collection = GiftCollectionType.Coats;
		}
		else
		{
			this.type = 5;	//internal
			this.collection = GiftCollectionType.Ornament;
		}
		
		
		this.lastName = sua.getLastname() + ", " + sua.getFirstname();
		
		Address address = getAddress(sua);
		this.houseNum = address.getFullStreetNum();
		this.street = address.getFullStreetName();
		this.unit = sua.getAddress2();
		this.city = sua.getCity();
		this.zipCode = sua.getZipcode();
		this.region = 0;
		this.homePhone = formatPhone(sua.getPhone());
		this.orn_req = 0;
		this.orn_assigned = 0;
		this.orn_delivered = 0;
		this.orn_rec_before = 0;
		this.orn_rec_after = 0;
		this.generalPartnerInfo =  "Phone type: " + sua.getPhonetype();
		this.cyNotes = "Imported from SignUp Genius SignUp";
		this.deliverTo = "WH";
		this.contact = sua.getFirstname() + " " + sua.getLastname();
		this.contact_email = sua.getEmail();
		this.contact_phone = formatPhone(sua.getPhone());
		this.contact2 = "";
		this.contact2_email = "";
		this.contact2_phone = "";
		this.pyRequested = 0;
		this.pyAssigned = 0;
		this.pyDelivered = 0;
		this.pyReceivedBeforeDeadline = 0;
		this.pyReceivedAfterDeadline = 0;
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
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	//getters
	public int getStatus()	{ return status; }
	public int getType()	{ return type; }
	public GiftCollectionType getGiftCollectionType() {return collection; }
	public String getLastName()	{ return lastName; }
	public String getHouseNum()	{ return houseNum; }
	public String getStreet()	{return street; }
	public String getUnit() { return unit; }
	public String getCity()	{return city; }
	public String getZipCode()	{ return zipCode; }
	int getRegion()	{ return region; }
	public String getHomePhone()	{ return homePhone; }
	public int getNumberOfOrnamentsRequested()	{ return orn_req; }
	public int getNumberOfOrnamentsAssigned() { return orn_assigned; }
	public int getNumberOfOrnamentsDelivered() { return orn_delivered; }
	public int getNumberOfOrnamentsReceivedBeforeDeadline() { return orn_rec_before; }
	public int getNumberOfOrnamentsReceivedAfterDeadline() { return orn_rec_after; }
	public String getOther()	{ return generalPartnerInfo; }
	public String getDeliverTo() { return deliverTo; }
	public String getSpecialNotes()	{ return cyNotes; }
	public String getContact()	{ return contact; }
	public String getContact_email()	{ return contact_email; }
	public String getContact_phone()	{ return contact_phone; }
	public String getContact2()	{ return contact2; }
	public String getContact2_email()	{ return contact2_email; }
	public String getContact2_phone()	{ return contact2_phone; }
	public int getPriorYearRequested() { return pyRequested; }
	public int getPriorYearAssigned() { return pyAssigned; }
	public int getPriorYearDelivered() { return pyDelivered; }
	public int getPriorYearReceivedBeforeDeadline() { return pyReceivedBeforeDeadline; }
	public int getPriorYearReceivedAfterDeadline() { return pyReceivedAfterDeadline; }
	
	//setters
	public void setStatus(int s)	{ status = s; }
	public void setType(int t)		{ type = t; }
	public void setGiftCollectionType(GiftCollectionType gc)	{ collection = gc; }
	public void setLastName(String n)	{ lastName = n; }
	public void setHouseNum(String sn)	{ houseNum = sn; }
	public void setStreet(String sn)	{ street = sn; }
	public void setUnit(String sn) { unit = sn; }
	public void setCity(String c)	{ city = c; }
	public void setZipCode(String z)	{ zipCode = z; }
	public void setRegion(int r)	{ region = r; }
	public void setHomePhone(String p)	{ homePhone = p; }
	public void setNumberOfOrnamentsRequested(int n)	{ orn_req = n; }
	public void setNumberOfOrnamentsAssigned(int n)	{ orn_assigned = n; }
	public void setNumberOfOrnamentsDelivered(int n)	{ orn_delivered = n; }
	public void setNumberOfOrnamentsReceivedBeforeDeadline(int n)	{ orn_rec_before = n; }
	public void setNumberOfOrnamentsReceivedAfterDeadline(int n)	{ orn_rec_after = n; }
	public void setOther(String o)	{ generalPartnerInfo = o; }
	public void setDeliverTo(String dt) { deliverTo = dt; }
	public void setSpecialNotes(String sn)	{ cyNotes = sn; }
	public void setContact(String c)	{ contact = c; }
	public void setContact_email(String e)	{ contact_email = e; }
	public void setContact_phone(String p)	{ contact_phone = p; }
	public void setContact2(String c)	{ contact2 = c; }
	public void setContact2_email(String e)	{ contact2_email = e; }
	public void setContact2_phone(String p)	{ contact2_phone = p; }
	public void setPriorYearRequested(int n) { pyRequested = n; }
	public void setPriorYearAssigned(int n) { pyAssigned = n; }
	public void setPriorYearDelivered(int n) { pyDelivered = n; }
	public void setPriorYearReceivedBeforeDeadline(int n) { pyReceivedBeforeDeadline = n; }
	public void setPriorYearReceivedAfterDeadline(int n) { pyReceivedAfterDeadline = n; }
	
	public int incrementOrnRequested() { return ++orn_req; }
	public int incrementOrnAssigned() { return ++orn_assigned; }
	public int decrementOrnAssigned()
	{
		if(orn_assigned > 0)
			orn_assigned--;
		return orn_assigned;
	}
	
	public int incrementOrnDelivered() { return ++orn_delivered; }
	public int decrementOrnDelivered()
	{
		if(orn_delivered > 0)
			orn_delivered--;
		return orn_delivered;
	}
	
	public int incrementOrnReceived(boolean bBeforeDeadline)
	{ 
		return  bBeforeDeadline ? ++orn_rec_before : ++orn_rec_after; 
	}
	
	public int decrementOrnReceived(boolean bBeforeDeadline)
	{
		if(bBeforeDeadline && orn_rec_before > 0)
			return --orn_rec_before;
		else if(!bBeforeDeadline && orn_rec_after > 0)
			return --orn_rec_after;
		else if(bBeforeDeadline && orn_rec_before <= 0)
			return orn_rec_before;
		else
			return orn_rec_after; 
	}
	
	public int incrementPYAssigned() { return ++pyAssigned; }
	public int incrementPYDelivered() { return ++pyDelivered; }
	public int incrementPYReceivedBeforeDeadline() { return ++pyReceivedBeforeDeadline; }
	public int incrementPYReceivedAfterDeadline() { return ++pyReceivedAfterDeadline; }
	
	String formatPhone(String inputPhone)
	{
		//make sure inputPhone isn't null
		if(inputPhone != null)
		{
			//get rid of all non numeric characters and test to see if it has 10 digits as required
			String numericString = inputPhone.replaceAll("\\D", "");
			if(numericString.length() == 10)
				return numericString.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1-$2-$3");
			else
				return inputPhone;
		}
		else
			return "";
	}
	
	//used to determine if two partner object substantially match.
	boolean doPartnersMatch(ONCPartner mp)
	{
		return this.id == mp.getID() && this.status == mp.getStatus() && this.type == mp.getType() &&
				this.collection == mp.getGiftCollectionType() && this.lastName.equals(mp.getLastName()) && 
				this.homePhone.equals(mp.getHomePhone()) && this.houseNum.equals(mp.getHouseNum()) &&
				this.street.equals(mp.getStreet()) && this.unit.equals(mp.getUnit()) &&
				this.city.equals(mp.getCity()) && this.zipCode.equals(mp.getZipCode()) &&
				this.contact.equals(mp.getContact()) && this.contact_email.equals(mp.getContact_email()) &&
				this.contact_phone.equals(mp.getContact_phone());
	}
	
	String[] getOrgInfoTableRow()
	{
		String[] sorttablerow = {lastName, homePhone,
								contact, contact_email, contact_phone,
								contact2, contact2_email, contact2_phone};						 
		return sorttablerow;
	}
	
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(status), Integer.toString(type),
						collection.toString(), lastName, houseNum,
						street, unit, city, zipCode, Integer.toString(region), homePhone,
						Integer.toString(orn_req),Integer.toString(orn_assigned), Integer.toString(orn_delivered),
						Integer.toString(orn_rec_before), Integer.toString(orn_rec_after), 
						generalPartnerInfo, deliverTo, cyNotes, contact,
						contact_email, contact_phone, contact2, contact2_email, contact2_phone, 
						Long.toString(timestamp), changedBy, Integer.toString(slPos),
						slMssg, slChangedBy, Integer.toString(pyRequested), Integer.toString(pyAssigned),
						Integer.toString(pyDelivered), Integer.toString(pyReceivedBeforeDeadline), 
						Integer.toString(pyReceivedAfterDeadline)};
		return row;
	}
	
	public String[] getGmailContactExportRow(int contactNum, String groupName)
	{
		//create a row of all empty strings
		String[] gmailContactRow = new String[58];
		for(int i=0; i< gmailContactRow.length; i++)
			gmailContactRow[i] = "";
		
		if(contactNum == 1)
		{
			//populate the correct cells with volunteer info
			gmailContactRow[0] = contact;
		
			String[] contactParts = contact.split(" ", 2);
			if(contactParts.length == 1)
			{
				gmailContactRow[3] = contact;
			}
			else if(contactParts.length == 2)
			{
				gmailContactRow[1] = contactParts[0];
				gmailContactRow[3] = contactParts[1];
			}
		
			gmailContactRow[25] = "Contact 1";
			gmailContactRow[26] = groupName;
			gmailContactRow[27] = "Work";
			gmailContactRow[28] = contact_email;
			gmailContactRow[31] = "Work";
			gmailContactRow[32] = contact_phone;
			gmailContactRow[39] = "Work";
			gmailContactRow[41] = houseNum + " " + street + " " + unit;
			gmailContactRow[42] = city;
			gmailContactRow[45] = zipCode;
			gmailContactRow[49] = lastName;
		
		}
		else if(contactNum == 2)
		{
			//populate the correct cells with volunteer info
			gmailContactRow[0] = contact2;
		
			String[] contactParts = contact2.split(" ", 2);
			if(contactParts.length == 1)
			{
				gmailContactRow[3] = contact2;
			}
			else if(contactParts.length == 2)
			{
				gmailContactRow[1] = contactParts[0];
				gmailContactRow[3] = contactParts[1];
			}
		
			gmailContactRow[25] = "Contact 2";
			gmailContactRow[26] = groupName;
			gmailContactRow[27] = "Work";
			gmailContactRow[28] = contact2_email;
			gmailContactRow[31] = "Work";
			gmailContactRow[32] = contact2_phone;
			gmailContactRow[39] = "Work";
			gmailContactRow[41] = houseNum + " " + street + " " + unit;
			gmailContactRow[42] = city;
			gmailContactRow[45] = zipCode;
			gmailContactRow[49] = lastName;
		}
		
		return gmailContactRow;
	}
		
	/***
    	 * partners are a match if their name and email fields match or their email and home phone fields match
    	 * @param p1
    	 * @param p
    	 * @return
    	 */
	@Override
	public boolean matches(ONCObject o)
    	{
		if(o == null)
			return false;
		else
		{
			ONCPartner p = (ONCPartner) o;
		
			boolean idsMatch = this.id == p.id;
    			boolean partnersNameAndEmailMatches = this.contact.equalsIgnoreCase(p.getContact()) &&
    				this.contact_email.equalsIgnoreCase(p.getContact_email());
    		
    			boolean partnersEmailAndPhoneMatches = this.contact_phone.equals(p.getHomePhone()) &&
    				this.contact_email.equalsIgnoreCase(p.getContact_email());
    		
    			return idsMatch || partnersNameAndEmailMatches || partnersEmailAndPhoneMatches;
		}
	}
	
	 @Override
	 public String toString()
	 {
	     return lastName;
	 }
}
