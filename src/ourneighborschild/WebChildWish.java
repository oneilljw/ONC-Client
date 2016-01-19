package ourneighborschild;

public class WebChildWish 
{
	private String name;
	private String detail;
	private String restriction;
	private String partner;
	private String status;
	
	public WebChildWish()
	{
		name = "";
		detail = "";
		restriction = "";
		partner = "";
		status = "Not Selected";
	}
	
	public WebChildWish(String name, String detail, String res, String partner, WishStatus status)
	{
		this.name = name;
		this.detail = detail;
		this.restriction = res;
		this.partner = partner;
		this.status = status.toString();
	}

	//getters
	public String getName() { return name; }
	public String getDetail() { return detail; }
	public String getRestriction() { return restriction; }
	public String getParter() { return partner; }
	public String getStatus() { return status; }
	
	//setters
	public void setName(String name) { this.name = name; }
	public void setDetail(String detail) { this.detail = detail; }
	public void setRestriction(String restriction) { this.restriction = restriction; }
	public void setParter(String parter) { this.partner = parter; }
	public void setStatus(String status) { this.status = status; }	
}
