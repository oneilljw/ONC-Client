package ourneighborschild;

public class WebChildWish 
{
	private String name;
	private String wishID;
	private String detail;
	private String restriction;
	private String partner;
	private String partnerID;
	private String status;
	
	public WebChildWish()
	{
		this.name = "";
		this.wishID = "-1";
		this.detail = "";
		this.restriction = "";
		this.partner = "";
		this.partnerID = "-1";
		this.status = "Not Selected";
	}
	
	public WebChildWish(String name, int wishID, String detail, String res, String partner, 
						int partnerID, GiftStatus status)
	{
		this.name = name;
		this.wishID = Integer.toString(wishID);
		this.detail = detail;
		this.restriction = res;
		this.partner = partner;
		this.partnerID = Integer.toString(partnerID);
		this.status = status.toString();
	}

	//getters
	public String getName() { return name; }
	public String getWishID() { return wishID; }
	public String getDetail() { return detail; }
	public String getRestriction() { return restriction; }
	public String getParter() { return partner; }
	public String getPartnerID() { return partnerID; }
	public String getStatus() { return status; }
	
	//setters
	public void setName(String name) { this.name = name; }
	public void setWishID(String wishID) { this.wishID = wishID; }
	public void setDetail(String detail) { this.detail = detail; }
	public void setRestriction(String restriction) { this.restriction = restriction; }
	public void setParter(String partner) { this.partner = partner; }
	public void setParterID(String partnerID) { this.partnerID = partnerID; }
	public void setStatus(String status) { this.status = status; }	
}
