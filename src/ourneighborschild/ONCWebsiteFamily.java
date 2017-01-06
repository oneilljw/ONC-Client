package ourneighborschild;

public class ONCWebsiteFamily
{
	private int		id;
	private String	oncNum;
	private String 	targetID;
	private String	fstatus;
	private String	giftStatus;	
	private String 	DNSCode;
	private String	HOHFirstName;
	private String	HOHLastName;
	private String	mealStatus;
	private int		agentID;
	
	public ONCWebsiteFamily(ONCFamily f)
	{
		this.id = f.id;
		this.oncNum = f.getONCNum();
		this.targetID = f.getReferenceNum();
		this.fstatus = f.getFamilyStatus().toString();
		this.giftStatus = f.getGiftStatus().toString();
		
//		if(f.getGiftStatus() == FamilyGiftStatus.Requested && f.getFamilyStatus().compareTo(FamilyStatus.InfoVerified) > 1)
//				this.giftStatus = f.getFamilyStatus().toString();
//		else
//			this.giftStatus = f.getGiftStatus().toString();
		
		this.DNSCode = f.getDNSCode();
		this.HOHFirstName = f.getHOHFirstName();
		this.HOHLastName = f.getHOHLastName();
		this.mealStatus = f.getMealStatus().toString();
		this.agentID = f.getAgentID();
	}

	int getId() { return id; }
	String getOncNum() { return oncNum; }
	String getTargetID() { return targetID; }
	String getFstatus() { return fstatus; }
	String getDstatus() { return giftStatus; }
	String getDNSCode() { return DNSCode; }
	String getHOHFirstName() {return HOHFirstName;}
	public String getHOHLastName() { return HOHLastName; }
	String getMealStatus() { return mealStatus; }
	int getAgentID() { return agentID; }

	void setId(int id) { this.id = id; }
	void setOncNum(String oncNum) { this.oncNum = oncNum; }
	void setTargetID(String targetID) { this.targetID = targetID; }
	void setFstatus(String fstatus) { this.fstatus = fstatus; }
	void setDstatus(String dstatus) { this.giftStatus = dstatus; }
	void setDNSCode(String dNSCode) { this.DNSCode = dNSCode; }
	void setHOHFirstName(String hOHFirstName) {this.HOHFirstName = hOHFirstName; }	
	void setHOHLastName(String hOHLastName) { this.HOHLastName = hOHLastName; }	
	void setMealStatus(String mealStatus) {this.mealStatus = mealStatus; }
	void setAgentID(int agtID) {this.agentID = agtID; }
}
