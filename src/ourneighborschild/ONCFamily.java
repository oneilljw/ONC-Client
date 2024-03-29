package ourneighborschild;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ONCFamily extends ONCEntity
{
	/**
	 * This class provides the blueprint for family object that contains all necessary information about
	 * a family in the ONC application. 
	 */
	private static final long serialVersionUID = 1128727192408305791L;
		
	private String		oncNum;
	private int			region;
	private String		schoolCode;
//	private FamilyStatus		famStatus;
//	private FamilyGiftStatus 	giftStatus;
	private int 		nBags;
	private int			nLargeItems;
	private String		referenceNum;
	private String		batchNum;	
//	private int	 		dnsCode;		//reference to dns code object id
	private String		speakEnglish;	//values are "Yes" or "No"	
	private String		language;		//Spanish, Arabic, Korean, etc	
	private String		notes;
	private String 		deliveryInstructions;
	private String 		clientFamily;
	private String		firstName;	//Head of household first name
	private String		lastName;	//Head of household last name	
	private String		houseNum;
	private String		street;
	private String		unit;
	private String		city;
	private String		zipCode;
	private String		substituteDeliveryAddress;	//in Google Map Address format
	private String		altPhone2;			
	private String		homePhone;
	private String		cellPhone;
	private String		email;
	private String		details;
	private String		childrenNames;	//Only used for .csv file export
	private String		schools;
	private String		wishList;	
	private String		adoptedFor;
	private int			agentID;
	private int			groupID;
//	private int			deliveryID;
	private int			phoneCode;	//indicates if the three phone numbers are valid and either mobile or other
//	private int 		mealID;
//	private MealStatus  mealStatus;
	private Transportation transportation;
	private boolean		bGiftCardOnly;
	private GiftDistribution	giftDistribution;
	private boolean		bDeliveryConfirmation;
	private int			distributionCenterID;
	
	//constructor used to make a copy for server update requests
	public ONCFamily(ONCFamily f)
	{
		super(f.getID(), f.getTimestamp(), f.getChangedBy(), f.getStoplightPos(), f.getStoplightMssg(), f.getStoplightChangedBy());
		this.oncNum = f.oncNum;
		this.region = f.region;
		this.schoolCode = f.schoolCode;
//		this.famStatus = f.famStatus;
//		this.giftStatus = f.giftStatus;
		this.nBags = f.nBags;
		this.nLargeItems = f.nLargeItems;
		this.referenceNum = f.referenceNum;
		this.batchNum = f.batchNum;	
//		this.dnsCode = f.dnsCode;
		this.speakEnglish = f.speakEnglish;
		this.language = f.language;
		this.notes = f.notes;
		this.deliveryInstructions = f.deliveryInstructions;
		this.clientFamily = f.clientFamily;
		this.firstName = f.firstName;
		this.lastName = f.lastName;
		this.houseNum = f.houseNum;
		this.street = f.street;
		this.unit = f.unit;
		this.city = f.city;
		this.zipCode = f.zipCode;
		this.substituteDeliveryAddress = f.substituteDeliveryAddress;
		this.altPhone2 = f.altPhone2;
		this.homePhone = f.homePhone;
		this.cellPhone = f.cellPhone;
		this.email = f.email;
		this.details = f.details;
		this.childrenNames = f.childrenNames;
		this.schools = f.schools;
		this.wishList = f.wishList;						
		this.adoptedFor = f.adoptedFor;
		this.agentID = f.agentID;
		this.groupID = f.groupID;
//		this.deliveryID = f.deliveryID;
		this.phoneCode = f.phoneCode;
//		this.mealID = f.mealID;
//		this.mealStatus = f.mealStatus;
		this.transportation = f.transportation;
		this.bGiftCardOnly = f.bGiftCardOnly;
		this.giftDistribution = f.giftDistribution;
		this.bDeliveryConfirmation = f.bDeliveryConfirmation;
		this.distributionCenterID = f.distributionCenterID;
	}

	//Overloaded Constructor - 29 column (A to AC) input from ODB .csv file - 2014, 2015
	public ONCFamily(String RAName, String RAOrg, String RATitle, String ClientFam, String HOH, String FamMembers, String RAEmail,
				String ClientFamEmail, String ClientFamPhone, String RAPhone, String DeitaryRestrictions, String Schools,
				String Details, String ID, String StreetAdd, String AddL2,  String Cty, String Zip,
				String State,String AdoptFor, String nAdults, String nChldren, String Wishlist, String SpeakEng,
				String Lang, String transportation, String bn, Date today, int region, String sONC, int id,
				String cb, int agentid, int groupid)
	{
		super(id, System.currentTimeMillis(), cb, STOPLIGHT_OFF, "Family imported", cb);
		oncNum = sONC;
		this.region = region;
		this.schoolCode = "Z";
//		famStatus = FamilyStatus.Unverified;
//		giftStatus = FamilyGiftStatus.Requested;
		nBags = 0;
		nLargeItems = 0;
		referenceNum = ID;
		batchNum = bn;
//		dnsCode = "";
//		dnsCode = -1;
		notes = "";
		deliveryInstructions = "";
		if(!ClientFam.isEmpty()) {clientFamily = ClientFam.split("Household", 2)[0].trim();}
		houseNum = "";
		street = StreetAdd;
		unit = AddL2;
		city = Cty;
		zipCode = Zip;
		substituteDeliveryAddress = "";
		altPhone2 = ClientFamPhone;			
		email = ClientFamEmail;
		details = Details;
		this.schools = Schools;
		childrenNames = "";
		wishList = Wishlist;
		adoptedFor = AdoptFor;
		agentID = agentid;
		groupID = groupid;
//		deliveryID = -1;
		this.phoneCode = 0;
//		mealID = -1;
//		mealStatus = MealStatus.None;
		if(transportation.equals("Yes") || transportation.equals("No"))
			this.transportation = Transportation.valueOf(transportation);
		else
			this.transportation = Transportation.TBD;
		bGiftCardOnly = false;
		this.giftDistribution = GiftDistribution.Delivery;
		this.bDeliveryConfirmation = false;
		this.distributionCenterID = -1;

		parseHOH(HOH);
		parsePhoneData(ClientFamPhone);
		getChildrenNames(FamMembers);
		parseAddress(StreetAdd, "");
		determineLanguage(SpeakEng, Lang);
	}
	
	//Overloaded Constructor - import Family DB from .csv file
	public ONCFamily(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), System.currentTimeMillis(), nextLine[8], 
				Integer.parseInt(nextLine[34]), nextLine[35], nextLine[36]);
		oncNum = getDBString(nextLine[1]);
		region = Integer.parseInt(nextLine[2]);
		this.schoolCode = nextLine[3];
		referenceNum = getDBString(nextLine[4]);
		batchNum = getDBString(nextLine[5]);	
//		dnsCode = getDBString(nextLine[6]);
//		dnsCode = nextLine[6].isEmpty() ? -1 : Integer.parseInt(nextLine[6]);
//		famStatus = FamilyStatus.getFamilyStatus(Integer.parseInt(nextLine[7]));
//		giftStatus = FamilyGiftStatus.getFamilyGiftStatus(Integer.parseInt(nextLine[8]));
		speakEnglish =getDBString(nextLine[6]);
		language = getDBString(nextLine[7]);
		notes = getDBString(nextLine[9]);
		deliveryInstructions = getDBString(nextLine[10]);	
		clientFamily = getDBString(nextLine[11]);
		firstName = getDBString(nextLine[12]);
		lastName = getDBString(nextLine[13]);
		houseNum = getDBString(nextLine[14]);
		street = getDBString(nextLine[15]);
		unit = getDBString(nextLine[16]);
		city = getDBString(nextLine[17]);
		zipCode = getDBString(nextLine[18]);
		substituteDeliveryAddress = getDBString(nextLine[19]);
		altPhone2 = getDBString(nextLine[20]);
		homePhone = getDBString(nextLine[21]);
		cellPhone = getDBString(nextLine[22]);
		email = getDBString(nextLine[23]);
		details = getDBString(nextLine[24]);
		childrenNames = getDBString(nextLine[25]);
		schools = getDBString(nextLine[26]);
		wishList = getDBString(nextLine[27]);
		adoptedFor = getDBString(nextLine[28]);
		agentID = Integer.parseInt(nextLine[29]);
		groupID = Integer.parseInt(nextLine[30]);
//		deliveryID = Integer.parseInt(nextLine[31]);
		phoneCode = Integer.parseInt(nextLine[31]);
		nBags = Integer.parseInt(nextLine[32]);
		nLargeItems = Integer.parseInt(nextLine[33]);
		transportation = Transportation.valueOf(nextLine[37]);
		bGiftCardOnly = nextLine[38].equals("TRUE") ? true : false;
		this.giftDistribution = nextLine[39].isEmpty() ? GiftDistribution.Delivery : GiftDistribution.distribution(Integer.parseInt(nextLine[39]));
		this.bDeliveryConfirmation = nextLine[40].equals("T") ? true : false;
		this.distributionCenterID =  Integer.parseInt(nextLine[41]);
	}
	
	//Overloaded Constructor - Direct Intake Processing
	public ONCFamily(int id, String cb, String oncNum, String odbFamilyNum, String batchNum,
				String speakEnglish, String language, String hohFirstName, String hohLastName, 
				String houseNum, String street, String unitNum, String city, String zipCode, 
				String altHouseNum, String altStreet, String altUnitNum, String altCity, String altZipCode,
				String primaryPhone, String altPhone, String alt2Phone, String familyEmail, String odbDetails,
				String schools, String odbWishList, int agentID, int groupID,
				int phoneCode, Transportation transportation, GiftDistribution giftDistribution)
	{
		super(id, System.currentTimeMillis(), cb, STOPLIGHT_OFF, "Family referred", cb);
		this.oncNum = oncNum;
		this.region = -1;
		this.schoolCode  = "Z";
//		this.famStatus = bWaitlistFamily ? FamilyStatus.Waitlist : FamilyStatus.Unverified;
//		this.giftStatus = bGiftsRequested ? FamilyGiftStatus.Requested : FamilyGiftStatus.NotRequested;
		this.nBags = 0;
		this.nLargeItems = 0;
		this.referenceNum = odbFamilyNum;
		this.batchNum = batchNum;
//		this.dnsCode = dnsCode;
		this.speakEnglish = speakEnglish;	//Values are "Yes" or "No"	
		this.language = language;		//Spanish, Arabic, Korean, etc	
		this.notes = "";
		this.deliveryInstructions = "";
		this.clientFamily = hohLastName;
		this.firstName = hohFirstName;
		this.lastName = hohLastName;	
		this.houseNum = houseNum;
		this.street = street;
		this.unit = unitNum;
		this.city = city;
		this.zipCode = zipCode;
		if((altHouseNum.equals(houseNum) && altStreet.equals(street) && altUnitNum.equals(unitNum) &&
				altCity.equals(city) && altZipCode.equals(zipCode)) || altHouseNum.isEmpty() ||
				altStreet.isEmpty() || altCity.isEmpty())
			this.substituteDeliveryAddress = "";
		else
			this.substituteDeliveryAddress = altHouseNum + "_" + altStreet +"_" + 
										 altUnitNum +"_" + altCity + "_" + altZipCode ;
		
		this.homePhone = formatPhoneNumber(primaryPhone);
		this.cellPhone = formatPhoneNumber(altPhone);
		this.altPhone2 = formatPhoneNumber(alt2Phone);
		
//		if(alt2Phone.length() < 10)
//			this.cellPhone = formatPhoneNumber(altPhone);
//		else
//			this.cellPhone = formatPhoneNumber(altPhone) + "\n" + formatPhoneNumber(alt2Phone);
		
//		//create the AllPhoneNumber field
//		StringBuilder buff = new StringBuilder("Home Phone: " + primaryPhone);
//		if(altPhone.length() > 9)	//Ensure it's a valid 10 digit phone number at minimum
//			buff.append("\n" + "Other phone: " + formatPhoneNumber(altPhone));
//		if(alt2Phone.length() > 9)
//			buff.append("\n" + "Other phone: " + formatPhoneNumber(alt2Phone));
//		this.altPhone2 = buff.toString();

		this.email = familyEmail;
		this.details = odbDetails;
		this.childrenNames = "";	//Only used for .csv file export
		this.schools = schools;
		this.wishList = odbWishList;	
		this.adoptedFor = "";
		this.agentID = agentID;
		this.groupID = groupID;
//		this.deliveryID = -1;
		this.phoneCode = phoneCode;
//		this.mealID = mealID;
//		this.mealStatus = mStatus;
		this.transportation = transportation;
		this.bGiftCardOnly = false;
		this.giftDistribution = giftDistribution;
		this.bDeliveryConfirmation = false;
		this.distributionCenterID = -1;
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	/****************************************************************************************
	 * This method takes an ODB head of household field and parses it into the first and last
	 * name of the first head of household. The input format is name - gender - DOB. If there is
	 * more than one HOH in the ODB field, they are separated by a carriage return character. 
	 * The first step is to split the array on \n and focus on the first element. 
	 * 
	 * Then determine if the first element is a female or male. Using that info, remove the -\b
	 * before the gender. This leaves the HOH name. Split the HOH name into two parts at the first
	 * blank space. The first element is the HOHFirstName, the second is HOHLastName 
	 * @param hoh
	 ****************************************************************************************/
	void parseHOH(String hoh)
	{
		int index = 0;
		
		String lcFirstHOH = hoh.split("\n")[0].toLowerCase();
		if(lcFirstHOH.contains("female"))
			index = lcFirstHOH.indexOf("female");
		else if(lcFirstHOH.contains("unknown"))
			index = lcFirstHOH.indexOf("unknown");
		else if(lcFirstHOH.contains("male"))
			index = lcFirstHOH.indexOf("male");
		else
			index = lcFirstHOH.indexOf("adult");
		
		index -= 2;
		String nameString ="";
		if(index > 0)
			nameString = hoh.substring(0, index);
		
		if(nameString.length() == 0)
		{
			firstName = "UNDETERMINED";
			lastName = "UNDETERMINED";
		}
		else
		{
			String[] hohNames = nameString.split(" ", 2);
			if(hohNames.length == 1)
			{
				firstName = "UNDETERMINED";
				lastName = hohNames[0].trim();
			}
			else
			{
				firstName = hohNames[0].trim();
				lastName = hohNames[1].trim();
			}
		}
	}
	
	void parseAddress(String snum, String sname)
	{
		String address; 
		if(sname.contains(snum))		
			address = sname.trim();
		else		
			address = snum.concat(" " + sname).trim();
		
		houseNum = street = "UNDETERMINED";
		
    	if(address.length() > 0)
    	{
    		String[] addParts = address.split(" ", 2);
    		houseNum = addParts[0].trim();
    		if(addParts.length ==2)
    			street = addParts[1].trim();
    	}
	}

	void parsePhoneData(String cfp)
	{
		int nHomeFound = 0;
		int nOtherFound = 0;
		String phones[] = cfp.split("\n");
		StringBuffer homeph = new StringBuffer();
		StringBuffer otherph = new StringBuffer();
		
		for(int i=0; i< phones.length; i++)
		{
			if(phones[i].contains("Home Phone:"))
			{
				if(nHomeFound == 0)	//Create a new line for each home phone after the first one found
					homeph.append(phones[i].split("Home Phone:", 2)[1].trim());
				else
					homeph.append("\n" + phones[i].split("Home Phone:", 2)[1].trim());
				nHomeFound++;	
			}
			else if(phones[i].contains("Home Mobile:"))
			{
				if(nHomeFound == 0)	//Create a new line for each home phone after the first one found
					homeph.append(phones[i].split("Home Mobile:", 2)[1].trim());
				else
					homeph.append("\n" + phones[i].split("Home Mobile:", 2)[1].trim());
				nHomeFound++;
			}
			else if(phones[i].contains("Other Phone:"))
			{
				if(nOtherFound == 0)	//Create a new line for each other phone after the first one found
					otherph.append(phones[i].split("Other Phone:", 2)[1].trim());
				else				
					otherph.append("\n" + phones[i].split("Other Phone:", 2)[1].trim());
				nOtherFound++;
			}
			else if(phones[i].contains("Work Phone:"))
			{
				if(nOtherFound == 0)	//Create a new line for each other phone after the first one found
					otherph.append(phones[i].split("Work Phone:", 2)[1].trim());
				else
					otherph.append("\n" + phones[i].split("Work Phone:", 2)[1].trim());
				nOtherFound++;
			}
		}
		
		if(homeph.length() > 0)
			homePhone = formatPhoneNumber(homeph.toString().trim());
		else
			homePhone = "None Found";
		
		if(otherph.length() > 0)
			cellPhone = formatPhoneNumber(otherph.toString().trim());
		else
			cellPhone = "None Found";	
	}

	void getChildrenNames(String fm)
	{
		int ch = 0, startch = 0;
    	String temp = null;
    	
    	for(ch=0; ch<fm.length(); ch++)
    	{   		
    		if(fm.charAt(ch) == '\n')
    		{
    			temp = fm.substring(startch, ch);
    			if(!temp.contains("Adult"))
    			{
    				childrenNames += fm.substring(startch, ch);
    				childrenNames += '\n';
    			}
    			startch=ch+1;   			
    		}
    	}
    	temp = fm.substring(startch, ch);
    	if(!temp.contains("Adult"))
		{
    		childrenNames += fm.substring(startch, ch);
		}
	}

	void determineLanguage(String speakEng, String lang)
	{
		if(speakEng.contains("Yes") || speakEng.contains("yes"))
		{
			speakEnglish = "Yes";
			language = "English";
		}
		else
		{
			speakEnglish = "No";
			language = lang;
		}	
	}
	
	String formatPhoneNumber(String phoneNumber)
	{
		if(phoneNumber.length() == 10)
		{
			char[] formattedNumber = new char[12];
			
			int phoneIndex = 0, formattedIndex = 0;
			while(phoneIndex < phoneNumber.length())
			{
				if(formattedIndex == 3 || formattedIndex == 7)
					formattedNumber[formattedIndex++] = '-';
				else
					formattedNumber[formattedIndex++] = phoneNumber.charAt(phoneIndex++);
			}
			
			return new String(formattedNumber);
		}
		else
			return phoneNumber;
	}

	//Getters
	public String	getONCNum() {return oncNum;}
	public int		getRegion() {return region;}
	public String	getSchoolCode() { return schoolCode; }
//	public FamilyStatus		getFamilyStatus() {return famStatus; }
//	public FamilyGiftStatus	getGiftStatus() { return giftStatus; }
	public int		getNumOfBags() { return nBags; }
	public int		getNumOfLargeItems() { return nLargeItems; }
	public String	getReferenceNum()	{return referenceNum;}
	public String	getBatchNum() {return batchNum;}	
//	public String 	getDNSCode() {return dnsCode;}
//	public int 		getDNSCode() {return dnsCode;}
	public String	getSpeakEnglish() {return speakEnglish;}
	public String 	getLanguage() {return language; }
	public String	getNotes() {return notes;}
	public String	getDeliveryInstructions() {return deliveryInstructions; }
	public String 	getClientFamily() {return clientFamily;}
	public String	getFirstName() {return firstName;}
	public String	getLastName() {return lastName;}
	public String	getHouseNum() {return houseNum;}
	public String	getStreet() {return street;}
	public String	getUnit() {return unit;}
	public String	getCity() {return city;}
	public String	getZipCode() {return zipCode;}
	public String	getSubstituteDeliveryAddress() {return substituteDeliveryAddress;}
	public String	getAlt2Phone() {return altPhone2;}			
	public String	getHomePhone() {return homePhone;}
	public String	getCellPhone() {return cellPhone;}
	public String	getEmail() {return email;}
	public String	getNamesOfChildren() { return childrenNames; }
	public String	getDetails() {return details;}
	public String	getSchools() {return schools;}
	public String	getWishList() {return wishList;}
	public String	getAdoptedFor() {return adoptedFor;}	
	public int		getAgentID() { return agentID; }
	public int		getGroupID() { return groupID; }
//	public int		getDeliveryID() { return deliveryID; }
	public int		getPhoneCode() { return phoneCode; }
//	public int		getMealID() { return mealID; }
//	public MealStatus getMealStatus() { return mealStatus; }
	public Transportation getTransportation() { return transportation; }
	public boolean 	isGiftCardOnly() { return bGiftCardOnly; }
	public GiftDistribution getGiftDistribution() { return giftDistribution; }
	public boolean hasDeliveryImage() { return bDeliveryConfirmation; }
	public int		getDistributionCenterID() { return distributionCenterID; }

	//Setters
	public void setONCNum(String s) { oncNum = s;}
	public void setRegion(int r) { region = r;}
	public void setSchoolCode(String sc) { this.schoolCode = sc; }
//	public void setFamilyStatus(FamilyStatus fs){ famStatus = fs; }
//	public void setGiftStatus(FamilyGiftStatus fgs) { giftStatus = fgs; }
	public void setNumOfBags(int b) { nBags = b; }
	public void setNumOfLargeItems(int li) { nLargeItems = li; }
	public void setReferenceNum(String s)	{ referenceNum = s;}
	public void setBatchNum(String s) { batchNum = s;}	
//	public void setDNSCode(String s) { dnsCode = s;}
//	public void setDNSCode(int code) { dnsCode = code;}
	public void setSpeakEnglish(String s) { speakEnglish = s;}	
	public void setLanguage(String s) { speakEnglish = ((language=s).equals("English")) ? "Yes":"No"; }
	public void setNotes(String s) { notes = s;}
	public void setDeliveryInstructions(String s) { deliveryInstructions = s; }
	public void setClientFamily(String s) { clientFamily = s;}
	public void setHOHFirstName(String s) { firstName = s;}
	public void setHOHLastName(String s) { lastName = s;}
	public void setHouseNum(String s) { houseNum = s;}
	public void setStreet(String s) { street = s;}
	public void setUnitNum(String s) { unit = s;}
	public void setCity(String s) { city = s;}
	public void setZipCode(String s) { zipCode = s;}
	public void setSubstituteDeliveryAddress(String s) { substituteDeliveryAddress = s;}
	public void setAlt2Phone(String s) { altPhone2 = s;}			
	public void setHomePhone(String s) { homePhone = s;}
	public void setOtherPhon(String s) { cellPhone = s;}
	public void setFamilyEmail(String s) { email = s;}
	public void setDetails(String s) { details = s;}
	public void setSchools(String s) { schools = s;}
	public void setWishList(String s) { wishList = s;}
	public void setAdoptedFor(String s) { adoptedFor = s;}
	public void setAgentID(int  aid) { agentID = aid; }
	public void setGroupID(int  gid) { groupID = gid; }
//	public void setDeliveryID(int did) { deliveryID = did; }
	public void setPhoneCode(int code) { phoneCode = code; }
//	public void setMealID(int id) { mealID = id; }
//	public void setMealStatus(MealStatus ms) { mealStatus = ms; }
	public void setTransportation(Transportation t) { transportation = t; }
	public void setGiftCardOnly(boolean gco) { bGiftCardOnly = gco; }
	public void setGiftDistribution(GiftDistribution dist) { this.giftDistribution = dist; }
	public void setDeliveryConfirmation(boolean gc) { bDeliveryConfirmation = gc; }
	public void setDistributionCenter(DistributionCenter dc) {this.distributionCenterID = dc.getID(); }
	
	public String getGoogleMapAddress()
	{
		String dbdestAddress;
		
		if(substituteDeliveryAddress != null && substituteDeliveryAddress.split("_").length == 5)
		{
			//format the substituteDeliveryAddress for the URL request to Google Maps
			String[] addPart = substituteDeliveryAddress.split("_");
			dbdestAddress = addPart[0] + "+" + addPart[1] + "+" + addPart[3] + ",VA" + "+" + addPart[4];
		}
		else	//Get actual family address and format it for the URL request to Google Maps
			dbdestAddress = houseNum.trim() + "+" + street.trim() + "+" + city.trim() + ",VA" + "+" + zipCode.trim();
		
		return dbdestAddress.replaceAll(" ", "+");
	}

	/*********************************************************************************************************
	 * This method compares an ONC Family passed as a parameter to this family based on five user changeable 
	 * criteria, passes as a second parameter. The five criteria are: 0 - HOH First Name matches;
	 * 1 - HOH Last Name Matches; 2 - HOH Last Name partially matches; 3 - House Number matches;
	 * 4 - Street matches; If all criteria are selected in the compare, the method will return true
	 * if all family class variables match. If only some criteria are selected, only those class
	 * variables will be included in the compare
	 * @param cf
	 * @param criteria
	 * @return - true if class variables requested to be compared match, false if they don't
	 ********************************************************************************************************/
	boolean isFamilyDuplicate(ONCFamily cf, boolean[] criteria)
	{
		return doesHouseNumMatch(cf.getHouseNum(), criteria[3]) &&
				 doesStreetMatch(cf.getStreet(), criteria[4]) &&
				  doesHOHFirstNameMatch(cf.getFirstName(), criteria[0]) &&
				   doesHOHLastNameMatch(cf.getLastName(), criteria[1]) &&
				    doesHOHLastNamePartiallyMatch(cf.getLastName(), criteria[2]);		
	}

	boolean doesHouseNumMatch(String hn, boolean criteria) { return !criteria || (criteria && hn.equals(houseNum)); }
	boolean doesStreetMatch(String st, boolean criteria) { return !criteria || (criteria && street.equalsIgnoreCase(st)); }
	boolean doesHOHFirstNameMatch(String hohfn, boolean criteria) { return !criteria || (criteria && firstName.equalsIgnoreCase(hohfn)); }
	boolean doesHOHLastNameMatch(String hohln, boolean criteria) { return !criteria || (criteria && lastName.equalsIgnoreCase(hohln)); }
	boolean doesHOHLastNamePartiallyMatch(String hohln, boolean criteria) { return !criteria || (criteria && lastName.trim().toLowerCase().contains(hohln.trim().toLowerCase())); }
	
	
	@Override
	public String[] getExportRow()
	{
		List<String> rowList = new ArrayList<String>();
		rowList.add(Integer.toString(getID()));
		rowList.add(getONCNum());		
		rowList.add(Integer.toString(getRegion()));
		rowList.add(getSchoolCode());
		rowList.add(getReferenceNum());
		rowList.add(getBatchNum());	
		rowList.add(getSpeakEnglish());
		rowList.add(getLanguage());			
		rowList.add(getChangedBy());
		rowList.add(getNotes());
		rowList.add(getDeliveryInstructions());
		rowList.add(getClientFamily());
		rowList.add(getFirstName());
		rowList.add(getLastName());
		rowList.add(getHouseNum());
		rowList.add(getStreet());
		rowList.add(getUnit());
		rowList.add(getCity());
		rowList.add(getZipCode());
		rowList.add(getSubstituteDeliveryAddress());
		rowList.add(getAlt2Phone());			
		rowList.add(getHomePhone());
		rowList.add(getCellPhone());
		rowList.add(getEmail());
		rowList.add(getDetails());
		rowList.add(getNamesOfChildren());
		rowList.add(getSchools());
		rowList.add(getWishList());
		rowList.add(getAdoptedFor());
		rowList.add(Integer.toString(getAgentID()));
		rowList.add(Integer.toString(getGroupID()));
		rowList.add(Integer.toString(phoneCode));
		rowList.add(Integer.toString(getNumOfBags()));
		rowList.add(Integer.toString(getNumOfLargeItems()));
		rowList.add(Integer.toString(getStoplightPos()));
		rowList.add(getStoplightMssg());
		rowList.add(getStoplightChangedBy());
		rowList.add(getTransportation().toString());
		rowList.add(isGiftCardOnly() ? "TRUE" : "FALSE");
		rowList.add(Integer.toString(giftDistribution.index()));
		rowList.add(hasDeliveryImage() ? "T" : "F");
		rowList.add(Integer.toString(getDistributionCenterID()));
		
		return rowList.toArray(new String[rowList.size()]);
	}
}