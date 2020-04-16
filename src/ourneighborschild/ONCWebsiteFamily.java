package ourneighborschild;

public class ONCWebsiteFamily
{
	private int		id;
	private String	oncNum;
	private String 	referenceNum;
	private boolean	bAlreadyReferred;
	private String	fstatus;
	private String	giftStatus;	
	private int		dnsCodeID;
	@SuppressWarnings("unused")
	private String	dnsCodeAcronym;
	private String	HOHFirstName;
	private String	HOHLastName;
	private String	mealStatus;
	private String	agentID;
	private int		groupID;
	private int		lastNoteStatus;
	
	public ONCWebsiteFamily(ONCFamily f, DNSCode dnsCode)
	{
		this.id = f.id;
		this.oncNum = f.getONCNum();
		this.referenceNum = f.getReferenceNum();
		this.bAlreadyReferred = false;
		this.fstatus = f.getFamilyStatus().toString();
		this.giftStatus = f.getGiftStatus().toString();
		
//		if(f.getGiftStatus() == FamilyGiftStatus.Requested && f.getFamilyStatus().compareTo(FamilyStatus.InfoVerified) > 1)
//				this.giftStatus = f.getFamilyStatus().toString();
//		else
//			this.giftStatus = f.getGiftStatus().toString();
		
		this.dnsCodeID = f.getDNSCode();
		this.dnsCodeAcronym = dnsCode.getAcronym();
		this.HOHFirstName = f.getFirstName();
		this.HOHLastName = f.getLastName();
		this.mealStatus = f.getMealStatus().toString();
		this.agentID = Integer.toString(f.getAgentID());
		this.groupID = f.getGroupID();
		this.lastNoteStatus = -1;
	}
	public ONCWebsiteFamily(ONCFamily f, boolean bAlreadyReferred, DNSCode dnsCode, int lastNoteStatus)
	{
		this.id = f.id;
		this.oncNum = f.getONCNum();
		this.referenceNum = f.getReferenceNum();
		this.bAlreadyReferred = bAlreadyReferred;
		this.fstatus = f.getFamilyStatus().toString();
		this.giftStatus = f.getGiftStatus().toString();
		
//		if(f.getGiftStatus() == FamilyGiftStatus.Requested && f.getFamilyStatus().compareTo(FamilyStatus.InfoVerified) > 1)
//				this.giftStatus = f.getFamilyStatus().toString();
//		else
//			this.giftStatus = f.getGiftStatus().toString();
		
		this.dnsCodeID = f.getDNSCode();
		this.dnsCodeAcronym = dnsCode.getAcronym();
		this.HOHFirstName = f.getFirstName();
		this.HOHLastName = f.getLastName();
		this.mealStatus = f.getMealStatus().toString();
		this.agentID = Integer.toString(f.getAgentID());
		this.groupID = f.getGroupID();
		this.lastNoteStatus = lastNoteStatus;
	}

	int getId() { return id; }
	String getOncNum() { return oncNum; }
	String getReferenceNum() { return referenceNum; }
	boolean alreadyReferred() { return bAlreadyReferred; }
	String getFstatus() { return fstatus; }
	String getDstatus() { return giftStatus; }
	int getDNSCodeID() { return dnsCodeID; }
	String getHOHFirstName() {return HOHFirstName;}
	public String getHOHLastName() { return HOHLastName; }
	String getMealStatus() { return mealStatus; }
	int getAgentID() { return Integer.parseInt(agentID); }
	int getGroupID() {return groupID;}
	int getLastNoteStatus() { return lastNoteStatus; }

	void setId(int id) { this.id = id; }
	void setOncNum(String oncNum) { this.oncNum = oncNum; }
	void setReferenceNum(String referenceNum) { this.referenceNum = referenceNum; }
	void setAlreadyReferred(boolean tf) { this.bAlreadyReferred = tf; }
	void setFstatus(String fstatus) { this.fstatus = fstatus; }
	void setDstatus(String dstatus) { this.giftStatus = dstatus; }
	void setDNSCodeID(int dnsCodeID){ this.dnsCodeID = dnsCodeID; }
	void setHOHFirstName(String hOHFirstName) {this.HOHFirstName = hOHFirstName; }	
	void setHOHLastName(String hOHLastName) { this.HOHLastName = hOHLastName; }	
	void setMealStatus(String mealStatus) {this.mealStatus = mealStatus; }
	void setAgentID(int agtID) {this.agentID = Integer.toString(agtID); }
	void setGroupID(int groupID) {this.groupID = groupID;}
}
