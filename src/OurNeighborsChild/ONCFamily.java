package OurNeighborsChild;

import java.util.ArrayList;
import java.util.Date;

public class ONCFamily extends ONCEntity
{
	/**
	 * This class provides the blueprint for family object that contains all necessary information about
	 * family in the ONC application. 
	 */
	private static final long serialVersionUID = 1128727192408305791L;
		
	private String		oncNum;
	private int			region;
	private int			fstatus;
	private int 		dstatus;
	private int 		nBags;
	private int			nLargeItems;
	private String		ODBFamilyNum;
	private String		BatchNum;	
	private String 		DNSCode;
	private String		SpeakEnglish;	//Values are "Yes" or "No"	
	private String		Language;		//Spanish, Arabic, Korean, etc	
	private String		Notes;
	private String 		DeliveryInstructions;
	private String 		ClientFamily;
	private String		HOHFirstName;
	private String		HOHLastName;	
	private String		HouseNum;
	private String		Street;
	private String		UnitNum;
	private String		City;
	private String		ZipCode;
	private String		substituteDeliveryAddress;	//in Google Map Address format
	private String		AllPhoneNumbers;			
	private String		HomePhone;
	private String		OtherPhone;
	private String		FamilyEmail;
	private String		ODBDetails;
	private String		ChildrenNames;	//Only used for .csv file export
	private String		Schools;
	private String		ODBWishList;	
	private String		AdoptedFor;
	private int			agentID;
	private int			deliveryID;
	
	
	//constructor used to make a copy for server update requests
	ONCFamily(ONCFamily f)
	{
		super(f.getID(), f.getDateChanged(), f.getChangedBy(), f.getStoplightPos(), f.getStoplightMssg(), f.getStoplightChangedBy());
		oncNum = f.oncNum;
		region = f.region;
		fstatus = f.fstatus;
		dstatus = f.dstatus;
		nBags = f.nBags;
		nLargeItems = f.nLargeItems;
		ODBFamilyNum = f.ODBFamilyNum;
		BatchNum = f.BatchNum;	
		DNSCode = f.DNSCode;
		SpeakEnglish = f.SpeakEnglish;
		Language = f.Language;
		Notes = f.Notes;
		DeliveryInstructions = f.DeliveryInstructions;
		ClientFamily = f.ClientFamily;
		HOHFirstName = f.HOHFirstName;
		HOHLastName = f.HOHLastName;
		HouseNum = f.HouseNum;
		Street = f.Street;
		UnitNum = f.UnitNum;
		City = f.City;
		ZipCode = f.ZipCode;
		substituteDeliveryAddress = f.substituteDeliveryAddress;
		AllPhoneNumbers = f.AllPhoneNumbers;
		HomePhone = f.HomePhone;
		OtherPhone = f.OtherPhone;
		FamilyEmail = f.FamilyEmail;
		ODBDetails = f.ODBDetails;
		ChildrenNames = f.ChildrenNames;
		Schools = f.Schools;
		ODBWishList = f.ODBWishList;						
		AdoptedFor = f.AdoptedFor;
		agentID = f.agentID;
		deliveryID = f.deliveryID;
	}
	//Overloaded Constructor - 30 column input from ODB file
		ONCFamily(String RAName, String RAOrg, String RATitle, String ClientFam, String HOH, String FamMembers,
				String RAEmail, String ClientFamEmail, String ClientFamPhone, String RAPhone,
				String DeitaryRestrictions, String schools, String Details, String ID,
				String StreetAddress, String StreetNum, String Suffix, String StreetName, String UnitNumber, 
				String AddL2, String AddL3, String Cty, String Zip, String State, String AdoptFor,
				String nAdults, String nChldren, String Wishlist, String SpeakEng, String Lang, String bn, Date today,
				int region, String sONC, int id, String cb, int agentid)
		{
			super(id, new Date(), cb, STOPLIGHT_OFF, "Family created", cb);
			oncNum=sONC;
			this.region=region;
			fstatus = 0;
			dstatus = 0;
			nBags = 0;
			nLargeItems = 0;
			ODBFamilyNum = ID;
			BatchNum = bn;	
			DNSCode = "";		
			Notes = "";
			DeliveryInstructions = "";
			if(!ClientFam.isEmpty()) {ClientFamily = ClientFam.split("Household", 2)[0].trim();}
			HouseNum = "";
			Street = "";
			UnitNum = Suffix.concat(UnitNumber.concat(AddL2.concat(AddL3)));	//for batch 3 this was changed from UnitNumber
			City = Cty;
			ZipCode = Zip;
			substituteDeliveryAddress = "";
			AllPhoneNumbers = ClientFamPhone;			
			FamilyEmail = ClientFamEmail;
			ODBDetails = Details;
			ChildrenNames = "";
			Schools = schools;
			ODBWishList = Wishlist;						
			AdoptedFor = AdoptFor;
			agentID = agentid;
			deliveryID = -1;

			newGetHOHName(HOH);					//Populate the HOH info
			parsePhoneData(ClientFamPhone);		//Populate Home and Other phone fields
			getChildrenNames(FamMembers);
			parseAddress(StreetNum, StreetAddress);
			determineLanguage(SpeakEng, Lang);
		}

	//Overloaded Constructor - 29 column input from ODB file
	ONCFamily(String RAName, String RAOrg, String RATitle, String ClientFam, String HOH, String FamMembers,
				String RAEmail, String ClientFamEmail, String ClientFamPhone, String RAPhone, 
				String DeitaryRestrictions, String Schools, String Details, String ID, String StreetNum,
				String Suffix, String StreetName, String UnitNumber, String AddL2, String AddL3, String Cty,
				String Zip, String State, String AdoptFor, String nAdults, String nChldren, String Wishlist,
				String SpeakEng, String Lang, String bn, Date today, int region, String sONC, int id, String cb, int agentid)
	{
		super(id, new Date(), cb, STOPLIGHT_OFF, "Family created", cb);
		oncNum=sONC;
		this.region=region;
		fstatus = 0;
		dstatus = 0;
		nBags = 0;
		nLargeItems = 0;
		ODBFamilyNum = ID;
		BatchNum = bn;	
		DNSCode = "";			
		Notes = "";
		DeliveryInstructions = "";
		if(!ClientFam.isEmpty()) {ClientFamily = ClientFam.split("Household", 2)[0].trim();}
		HouseNum = StreetNum;
		Street = StreetName;
		UnitNum = AddL2;	//for batch 3 this was changed from UnitNumber
		City = Cty;
		ZipCode = Zip;
		substituteDeliveryAddress = "";
		AllPhoneNumbers = ClientFamPhone;			
		FamilyEmail = ClientFamEmail;
		ODBDetails = Details;
		ChildrenNames = "";
		this.Schools = "";
		ODBWishList = Wishlist;
		AdoptedFor = AdoptFor;
		agentID = agentid;
		deliveryID = -1;
		
		newGetHOHName(HOH);
//		getPhones(ClientFamPhone);
		parsePhoneData(ClientFamPhone);
		getChildrenNames(FamMembers);
		parseAddress(StreetNum, StreetName);
		determineLanguage(SpeakEng, Lang);		
	}
	
	//Overloaded Constructor - 26 column input from ODB file
	ONCFamily(String RAName, String RAOrg, String RATitle, String ClientFam, String HOH, String FamMembers, String RAEmail,
			String ClientFamEmail, String ClientFamPhone, String RAPhone, String DeitaryRestrictions, String Schools,
			String Details, String ID, String StreetNum, String Suffix, String StreetName,  String Cty, String Zip,
			String State,String AdoptFor, String nAdults, String nChldren, String Wishlist, String SpeakEng, String Lang,
			String bn, Date today, int region, String sONC, int id, String cb, int agentid)
	{
		super(id, new Date(), cb, STOPLIGHT_OFF, "Family created", cb);
		oncNum = sONC;
		this.region = region;
		fstatus = 0;
		dstatus = 0;
		nBags = 0;
		nLargeItems = 0;
		ODBFamilyNum = ID;
		BatchNum = bn;	
		DNSCode = "";		
		Notes = "";
		DeliveryInstructions = "";
		if(!ClientFam.isEmpty()) {ClientFamily = ClientFam.split("Household", 2)[0].trim();}
		HouseNum = StreetNum;
		Street = StreetName;
		UnitNum = Suffix;
		City = Cty;
		ZipCode = Zip;
		substituteDeliveryAddress = "";
		AllPhoneNumbers = ClientFamPhone;			
		FamilyEmail = ClientFamEmail;
		ODBDetails = Details;
		this.Schools = Schools;
		ChildrenNames = "";
		ODBWishList = Wishlist;
		agentID = agentid;
		deliveryID = -1;

		newGetHOHName(HOH);
		parsePhoneData(ClientFamPhone);
		getChildrenNames(FamMembers);
		parseAddress(StreetNum, StreetName);
		determineLanguage(SpeakEng, Lang);
	}
	
	//Overloaded Constructor - 25 column input from ODB file
		ONCFamily(String RAName, String RAOrg, String RATitle, String ClientFam, String HOH, String FamMembers, String RAEmail,
				String ClientFamEmail, String ClientFamPhone, String RAPhone, String DeitaryRestrictions, String Schools,
				String Details, String ID, String StreetAdd, String AddL2,  String Cty, String Zip,
				String State,String AdoptFor, String nAdults, String nChldren, String Wishlist, String SpeakEng,
				String Lang, String bn, Date today, int region, String sONC, int id, String cb, int agentid)
		{
			super(id, new Date(), cb, STOPLIGHT_OFF, "Family created", cb);
			oncNum = sONC;
			this.region = region;
			fstatus = 0;
			dstatus = 0;
			nBags = 0;
			nLargeItems = 0;
			ODBFamilyNum = ID;
			BatchNum = bn;
			DNSCode = "";			
			Notes = "";
			DeliveryInstructions = "";
			if(!ClientFam.isEmpty()) {ClientFamily = ClientFam.split("Household", 2)[0].trim();}
			HouseNum = "";
			Street = StreetAdd;
			UnitNum = AddL2;
			City = Cty;
			ZipCode = Zip;
			substituteDeliveryAddress = "";
			AllPhoneNumbers = ClientFamPhone;			
			FamilyEmail = ClientFamEmail;
			ODBDetails = Details;
			this.Schools = Schools;
			ChildrenNames = "";
			ODBWishList = Wishlist;
			AdoptedFor = AdoptFor;
			agentID = agentid;
			deliveryID = -1;

			parseHOH(HOH);
			parsePhoneData(ClientFamPhone);
			getChildrenNames(FamMembers);
			parseAddress(StreetAdd, "");
			determineLanguage(SpeakEng, Lang);
		}
	//Overloaded Constructor - import Family DB from .csv file
	public ONCFamily(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), new Date(), nextLine[10], 
				Integer.parseInt(nextLine[35]), nextLine[36], nextLine[37]);
		oncNum = getDBString(nextLine[1]);
		region = Integer.parseInt(nextLine[2]);
		ODBFamilyNum = getDBString(nextLine[3]);
		BatchNum = getDBString(nextLine[4]);	
		DNSCode = getDBString(nextLine[5]);
		fstatus = Integer.parseInt(nextLine[6]);
		dstatus = Integer.parseInt(nextLine[7]);
		SpeakEnglish =getDBString(nextLine[8]);
		Language = getDBString(nextLine[9]);
		Notes = getDBString(nextLine[11]);
		DeliveryInstructions = getDBString(nextLine[12]);	
		ClientFamily = getDBString(nextLine[13]);
		HOHFirstName = getDBString(nextLine[14]);
		HOHLastName = getDBString(nextLine[15]);
		HouseNum = getDBString(nextLine[16]);
		Street = getDBString(nextLine[17]);
		UnitNum = getDBString(nextLine[18]);
		City = getDBString(nextLine[19]);
		ZipCode = getDBString(nextLine[20]);
		substituteDeliveryAddress = getDBString(nextLine[21]);
		AllPhoneNumbers = getDBString(nextLine[22]);
		HomePhone = getDBString(nextLine[23]);
		OtherPhone = getDBString(nextLine[24]);
		FamilyEmail = getDBString(nextLine[25]);
		ODBDetails = getDBString(nextLine[26]);
		ChildrenNames = getDBString(nextLine[27]);
		Schools = getDBString(nextLine[28]);
		ODBWishList = getDBString(nextLine[29]);
		AdoptedFor = getDBString(nextLine[30]);
		agentID = Integer.parseInt(nextLine[31]);
		deliveryID = Integer.parseInt(nextLine[32]);
		nBags = Integer.parseInt(nextLine[33]);
		nLargeItems = Integer.parseInt(nextLine[34]);
//		slPos = Integer.parseInt(nextLine[35]);
//		slMssg = getDBString(nextLine[36]);
//		slChangedBy = getDBString(nextLine[37]);
	}
	
	//Overloaded Constructor - Direct Intake Processing
	ONCFamily(int id, String cb, String oncNum, String odbFamilyNum, String batchNum, String speakEnglish, String language,
				String hohFirstName, String hohLastName, String houseNum, String street, String unitNum,
				String city, String zipCode, String homePhone, String otherPhone, String familyEmail,
				String odbDetails, String odbWishList, int agentID)
	{
		super(id, new Date(), cb, STOPLIGHT_OFF, "Family created", cb);
		this.oncNum = oncNum;
		this.region = -1;
		this.fstatus = 0;
		this.dstatus = 0;
		this.nBags = 0;
		this.nLargeItems = 0;
		this.ODBFamilyNum = odbFamilyNum;
		this.BatchNum = batchNum;	
		this.DNSCode = "";
		this.SpeakEnglish = speakEnglish;	//Values are "Yes" or "No"	
		this.Language = language;		//Spanish, Arabic, Korean, etc	
		this.Notes = "";
		this.DeliveryInstructions = "";
		this.ClientFamily = hohLastName;
		this.HOHFirstName = hohFirstName;
		this.HOHLastName = hohLastName;	
		this.HouseNum = houseNum;
		this.Street = street;
		this.UnitNum = unitNum;
		this.City = city;
		this.ZipCode = zipCode;
		this.substituteDeliveryAddress = "";
		this.AllPhoneNumbers = homePhone + "\n" + otherPhone;			
		this.HomePhone = homePhone;
		this.OtherPhone = otherPhone;
		this.FamilyEmail = familyEmail;
		this.ODBDetails = odbDetails;
		this.ChildrenNames = "";	//Only used for .csv file export
		this.Schools = "";
		this.ODBWishList = odbWishList;	
		this.AdoptedFor = "";
		this.agentID = agentID;
		this.deliveryID = -1;
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}

	void getHOHName(String c)
	{
		int ch = 0, startch = 0, nNamesFound = 0;
		boolean bLeadingCharFound = false;
		
    	if(c.length() > 0)
    	{
    		while(ch < c.length() || nNamesFound < 2)   	
    		{   
    			if(c.charAt(ch) == ' ' && bLeadingCharFound == false) //Ignore leading blank characters  		
    				ch++;
    		
    			else if (c.charAt(ch) != ' ' && bLeadingCharFound == false)
    			{
    				bLeadingCharFound = true;
    				ch++;
    			}
    	   		
    			else if(c.charAt(ch) == ' ' && bLeadingCharFound == true && nNamesFound == 0)
    			{ 			
    				HOHFirstName = c.substring(startch, ch);
    				bLeadingCharFound = false;
    				startch = ++ch;
    				nNamesFound++;
    			}
    		
    			else if(c.charAt(ch) == ' ' && bLeadingCharFound == true && nNamesFound == 1)
    			{
    				HOHLastName = c.substring(startch, ch);   			
    				startch = ++ch;
    				nNamesFound++;
    			}
    			else
    				ch++;
    		}
    	}
    	else
    	{
    		HOHFirstName = "MISSING";
    		HOHLastName = "MISSING";   				
    	}
	}
	
	void newGetHOHName(String HOH)
	{
		int ch = 0;
		String name = "";
		ArrayList<String> HOHName = new ArrayList<String>();
				
		String c = HOH.trim();
		c = c.replaceAll(" - ", " ");
		
		do
		{
			name = "";
			while(ch < c.length() && (c.charAt(ch) != ' '))
			{
				name += c.charAt(ch++);
			}
			
			if(!(name.contains("Female") || name.contains("Male") || name.contains("male")
					|| name.contains("Unknown")) && ch < c.length())
				HOHName.add(name);
			
			ch++;
		}
		while(!(name.contains("Female") || name.contains("Male") || name.contains("female") || name.contains("male")
				|| name.contains("Unknown")) && ch < c.length());
		
		
		if(HOHName.isEmpty())
		{
			HOHFirstName = "MISSING";
			HOHLastName = "MISSING";
		}
		else if(HOHName.size() == 1)
		{
			HOHFirstName = "MISSING";
			HOHLastName = HOHName.get(0);
		}
		else
		{
			HOHFirstName = HOHName.get(0);
			HOHLastName = HOHName.get(HOHName.size()-1);
		}
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
		{
			index = lcFirstHOH.indexOf("female");
		}
		else if(lcFirstHOH.contains("adult"))
		{
			index = lcFirstHOH.indexOf("adult");
		}
		else if(lcFirstHOH.contains("unknown"))
		{
			index = lcFirstHOH.indexOf("unknown");
		}
		else
			index = lcFirstHOH.indexOf("male");
		
		index -= 2;
		String nameString ="";
		if(index > 0)
			nameString = hoh.substring(0, index);
		
		if(nameString.length() == 0)
		{
			HOHFirstName = "UNDETERMINED";
			HOHLastName = "UNDETERMINED";
		}
		else
		{
			String[] hohNames = nameString.split(" ", 2);
			if(hohNames.length == 1)
			{
				HOHFirstName = "UNDETERMINED";
				HOHLastName = hohNames[0].trim();
			}
			else
			{
				HOHFirstName = hohNames[0].trim();
				HOHLastName = hohNames[1].trim();
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
		
		HouseNum = Street = "UNDETERMINED";
		
    	if(address.length() > 0)
    	{
    		String[] addParts = address.split(" ", 2);
    		HouseNum = addParts[0].trim();
    		if(addParts.length ==2)
    			Street = addParts[1].trim();
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
			HomePhone = homeph.toString().trim();
		else
			HomePhone = "None Found";
		
		if(otherph.length() > 0)
			OtherPhone = otherph.toString().trim();
		else
			OtherPhone = "None Found";	
	}
/*	
	void getPhones(String cfp)
	{
		int ch = 0, startch = 0;
		boolean bHomeFound = false, bOtherFound = false;
    	String temp = null;
    	
    	for(ch=0; ch<cfp.length(); ch++)
    	{   
    		//Process any lines before last line in cell
    		if(cfp.charAt(ch) == '\n')
    		{ 
    			temp = cfp.substring(startch, ch-1);
    			
    			if(temp.contains("Home Phone:") && bHomeFound == false)
    			{
    				HomePhone = cfp.substring(ch-11, ch-1);
    				bHomeFound = true;
    			}
    			if(temp.contains("Other Phone:") && bOtherFound == false)
    			{
    				OtherPhone = cfp.substring(ch-11, ch-1);
    				bOtherFound = true;
    			}	
    			startch=ch+1;   			
    		}
    	}
    	
    	//Process last line in cell
    	temp = cfp.substring(startch, ch);
    	
    	if(temp.contains("Home Phone:") && bHomeFound == false)
		{
			HomePhone = cfp.substring(ch-11, ch);
			bHomeFound = true;
		}
		if(temp.contains("Other Phone:") && bOtherFound == false)
		{
			OtherPhone = cfp.substring(ch-11, ch);
			bOtherFound = true;
		}   	
	}
*/	
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
    				ChildrenNames += fm.substring(startch, ch);
    				ChildrenNames += '\n';
    			}
    			startch=ch+1;   			
    		}
    	}
    	temp = fm.substring(startch, ch);
    	if(!temp.contains("Adult"))
		{
    		ChildrenNames += fm.substring(startch, ch);
		}
	}

	void determineLanguage(String speakEng, String lang)
	{
		if(speakEng.contains("Yes") || speakEng.contains("yes"))
		{
			SpeakEnglish = "Yes";
			Language = "English";
		}
		else
		{
			SpeakEnglish = "No";
			Language = lang;
		}	
	}

	//Getters
	public String	getONCNum() {return oncNum;}
	public int		getRegion() {return region;}
	public int		getFamilyStatus() {return fstatus; }
	public int		getDeliveryStatus() { return dstatus; }
	public int		getNumOfBags() { return nBags; }
	public int		getNumOfLargeItems() { return nLargeItems; }
	public String	getODBFamilyNum()	{return ODBFamilyNum;}
	public String	getBatchNum() {return BatchNum;}	
	public String 	getDNSCode() {return DNSCode;}
	public String	getSpeakEnglish() {return SpeakEnglish;}
	public String 	getLanguage() {return Language; }
	public String	getNotes() {return Notes;}
	public String	getDeliveryInstructions() {return DeliveryInstructions; }
	public String 	getClientFamily() {return ClientFamily;}
	public String	getHOHFirstName() {return HOHFirstName;}
	public String	getHOHLastName() {return HOHLastName;}
	public String	getHouseNum() {return HouseNum;}
	public String	getStreet() {return Street;}
	public String	getUnitNum() {return UnitNum;}
	public String	getCity() {return City;}
	public String	getZipCode() {return ZipCode;}
	public String	getSubstituteDeliveryAddress() {return substituteDeliveryAddress;}
	public String	getAllPhoneNumbers() {return AllPhoneNumbers;}			
	public String	getHomePhone() {return HomePhone;}
	public String	getOtherPhon() {return OtherPhone;}
	public String	getFamilyEmail() {return FamilyEmail;}
	public String	getNamesOfChildren() { return ChildrenNames; }
	public String	getODBDetails() {return ODBDetails;}
	public String	getSchools() {return Schools;}
	public String	getODBWishList() {return ODBWishList;}
	public String	getAdoptedFor() {return AdoptedFor;}	
	public int		getAgentID() { return agentID; }
	public int		getDeliveryID() { return deliveryID; }

	//Setters
	public void setONCNum(String s) { oncNum = s;}
	public void setRegion(int r) { region = r;}
	public void setFamilyStatus(int s){ fstatus = s; }
	public void setDeliveryStatus(int d) { dstatus = d; }
	public void setNumOfBags(int b) { nBags = b; }
	public void setNumOfLargeItems(int li) { nLargeItems = li; }
	public void setODBFamilyNum(String s)	{ ODBFamilyNum = s;}
	public void setBatchNum(String s) { BatchNum = s;}	
	public void setDNSCode(String s) { DNSCode = s;}
	public void setSpeakEnglish(String s) { SpeakEnglish = s;}	
	public void setLanguage(String s) { SpeakEnglish = ((Language=s).equals("English")) ? "Yes":"No"; }
	public void setNotes(String s) { Notes = s;}
	public void setDeliveryInstructions(String s) { DeliveryInstructions = s; }
	public void setClientFamily(String s) { ClientFamily = s;}
	public void setHOHFirstName(String s) { HOHFirstName = s;}
	public void setHOHLastName(String s) { HOHLastName = s;}
	public void setHouseNum(String s) { HouseNum = s;}
	public void setStreet(String s) { Street = s;}
	public void setUnitNum(String s) { UnitNum = s;}
	public void setCity(String s) { City = s;}
	public void setZipCode(String s) { ZipCode = s;}
	public void setSubstituteDeliveryAddress(String s) { substituteDeliveryAddress = s;}
	public void setAllPhoneNumbers(String s) { AllPhoneNumbers = s;}			
	public void setHomePhone(String s) { HomePhone = s;}
	public void setOtherPhon(String s) { OtherPhone = s;}
	public void setFamilyEmail(String s) { FamilyEmail = s;}
	public void setODBDetails(String s) { ODBDetails = s;}
	public void setSchools(String s) { Schools = s;}
	public void setODBWishList(String s) { ODBWishList = s;}
	public void setAdoptedFor(String s) { AdoptedFor = s;}
	public void setAgentID(int  aid) { agentID = aid; }
	public void setDeliveryID(int did) { deliveryID = did; }
	
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
			dbdestAddress = HouseNum.trim() + "+" + Street.trim() + "+" + City.trim() + ",VA" + "+" + ZipCode.trim();
		
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
				  doesHOHFirstNameMatch(cf.getHOHFirstName(), criteria[0]) &&
				   doesHOHLastNameMatch(cf.getHOHLastName(), criteria[1]) &&
				    doesHOHLastNamePartiallyMatch(cf.getHOHLastName(), criteria[2]);		
	}

	boolean doesHouseNumMatch(String hn, boolean criteria) { return !criteria || (criteria && hn.equals(HouseNum)); }
	boolean doesStreetMatch(String st, boolean criteria) { return !criteria || (criteria && Street.equalsIgnoreCase(st)); }
	boolean doesHOHFirstNameMatch(String hohfn, boolean criteria) { return !criteria || (criteria && HOHFirstName.equalsIgnoreCase(hohfn)); }
	boolean doesHOHLastNameMatch(String hohln, boolean criteria) { return !criteria || (criteria && HOHLastName.equalsIgnoreCase(hohln)); }
	boolean doesHOHLastNamePartiallyMatch(String hohln, boolean criteria) { return !criteria || (criteria && HOHLastName.trim().toLowerCase().contains(hohln.trim().toLowerCase())); }
	
	
	@Override
	public String[] getExportRow()
	{
		String[] row = new String[38];
		int index = 0;
		
		row[index++] = 	Integer.toString(getID());
		row[index++] =	getONCNum();		
		row[index++] = 	Integer.toString(getRegion());
		row[index++] = 	getODBFamilyNum();
		row[index++] = 	getBatchNum();	
		row[index++] =  getDNSCode();
		row[index++] = 	Integer.toString(getFamilyStatus());
		row[index++] = 	Integer.toString(getDeliveryStatus());
		row[index++] = 	getSpeakEnglish();
		row[index++] = 	getLanguage();			
		row[index++] = 	getChangedBy();
		row[index++] = 	getNotes();
		row[index++] = 	getDeliveryInstructions();
		row[index++] =  getClientFamily();
		row[index++] = 	getHOHFirstName();
		row[index++] = 	getHOHLastName();
		row[index++] = 	getHouseNum();
		row[index++] = 	getStreet();
		row[index++] = 	getUnitNum();
		row[index++] = 	getCity();
		row[index++] = 	getZipCode();
		row[index++] = 	getSubstituteDeliveryAddress();
		row[index++] = 	getAllPhoneNumbers();			
		row[index++] = 	getHomePhone();
		row[index++] = 	getOtherPhon();
		row[index++] = 	getFamilyEmail();
		row[index++] = 	getODBDetails();
		row[index++] = 	getNamesOfChildren();
		row[index++] = 	getSchools();
		row[index++] = 	getODBWishList();
		row[index++] = 	getAdoptedFor();
		row[index++] = 	Integer.toString(getAgentID());
		row[index++] = 	Integer.toString(getDeliveryID());
		row[index++] = 	Integer.toString(getNumOfBags());
		row[index++] = 	Integer.toString(getNumOfLargeItems());
		row[index++] = 	Integer.toString(getStoplightPos());
		row[index++] = 	getStoplightMssg();
		row[index] = 	getStoplightChangedBy();

		return row;		
	}
	@Override
	Object getTableCell(int col)
	{
		String[] famstatus = {"Any", "Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
		String[] delstatus = {"Any", "Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
		String[] stoplt = {"G", "Y", "R", "O"};
		ONCRegions regions = ONCRegions.getInstance();
		
		if(col == 0)
			return oncNum;
		else if (col == 1)
			return BatchNum;
		else if(col == 2)
			return DNSCode;
		else if (col == 3)
			return famstatus[fstatus];
		else if (col == 4)
			return delstatus[dstatus];
		else if(col == 5)
			return HOHFirstName;
		else if(col == 6)
			return HOHLastName;
		else if(col == 7)
			return HouseNum;
		else if(col == 8)
			return Street;
		else if(col == 9)
			return UnitNum;
		else if(col == 10)
			return ZipCode;
		else if (col == 11)
			return regions.getRegionID(region);
		else if(col == 12)
			return changedBy;
		else if (col == 13)
			return stoplt[slPos];
		else
			return "Error";
	}
	
	@Override
	public Class<?> getColumnClass(int col)
	{
		return String.class;
	}
}

