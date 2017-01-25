package ourneighborschild;

public class BritepathFamily 
{
	public String batchNum;
	public String referringAgentName;
	public String referringAgentOrg;
	public String referringAgentTitle;
	public String SponsorContactName;
	public String clientFamily;
	public String headOfHousehold;
	public String familyMembers;
	public String referringAgentEmail;
	public String clientFamilyEmail;
	public String clientFamilyPhone;
	public String referringAgentPhone;
	public String dietartyRestrictions;
	public String schoolsAttended;
	public String details;
	public String assigneeContactID;
	public String deliveryStreetAddress;
	public String deliveryAddressLine2;
	public String deliveryAddressLine3;
	public String deliveryCity;
	public String deliveryZip;
	public String deliveryState;
	public String donorType;
	public String adoptedFor;
	public String numberOfAdults;
	public String numberOfChildren;
	public String wishlist;
	public String speaksEnglish;
	public String language;
	public String hasTransportation;
	
	public BritepathFamily(String batchNum, String[] line)
	{
		this.batchNum = batchNum; 
		referringAgentName = line[0].isEmpty() ? "" : line[0];
		referringAgentOrg = line[1].isEmpty() ? "" : line[1];
		referringAgentTitle = line[2].isEmpty() ? "" : line[2];
		SponsorContactName = line[3].isEmpty() ? "" : line[3];
		clientFamily = line[4].isEmpty() ? "" : line[4];
		headOfHousehold = line[5].isEmpty() ? "" : line[5];
		familyMembers = line[6].isEmpty() ? "" : line[6];
		referringAgentEmail = line[7].isEmpty() ? "" : line[7];
		clientFamilyEmail = line[8].isEmpty() ? "" : line[8];
		clientFamilyPhone = line[9].isEmpty() ? "" : line[9];
		referringAgentPhone = line[10].isEmpty() ? "" : line[10];
		dietartyRestrictions = line[11].isEmpty() ? "" : line[11];
		schoolsAttended = line[12].isEmpty() ? "" : line[12];
		details = line[13].isEmpty() ? "" : line[13];
		assigneeContactID = line[14].isEmpty() ? "" : line[14];
		deliveryStreetAddress = line[15].isEmpty() ? "" : line[15];
		deliveryAddressLine2 = line[16].isEmpty() ? "" : line[16];
		deliveryAddressLine3 = line[17].isEmpty() ? "" : line[17];
		deliveryCity = line[18].isEmpty() ? "" : line[18];
		deliveryZip = line[19].isEmpty() ? "" : line[19];
		deliveryState = line[20].isEmpty() ? "" : line[20];
		donorType = line[21].isEmpty() ? "" : line[21];
		adoptedFor = line[22].isEmpty() ? "" : line[22];
		numberOfAdults = line[23].isEmpty() ? "" : line[23];
		numberOfChildren = line[24].isEmpty() ? "" : line[24];
		wishlist = line[25].isEmpty() ? "" : line[25];
		speaksEnglish = line[26].isEmpty() ? "" : line[26];
		language = line[27].isEmpty() ? "" : line[27];
		hasTransportation = line[28].isEmpty() ? "" : line[28];
	}
	
	//getters
	public String getReferringAgentName() { return referringAgentName; }
	public String getReferringAgentOrg() { return referringAgentOrg; }
	public String getReferringAgentTitle() { return referringAgentTitle; }
	public String getReferringAgentEmail() { return referringAgentEmail; }
	public String getReferringAgentPhone() { return referringAgentPhone; }
	public String getFamilyMembers() { return familyMembers; }
}
