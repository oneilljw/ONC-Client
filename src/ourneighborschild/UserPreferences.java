package ourneighborschild;

public class UserPreferences 
{
	private int fontSize;
	private int wishAssigneeFilter;
	private DNSCode familyDNSFilterCode;
	
	public UserPreferences()
	{
		this.fontSize = 13;
		this.wishAssigneeFilter = 1;
		this.familyDNSFilterCode = new DNSCode(-3, "Any", "Any", "Any", false);
	}
	
	public UserPreferences(int fs, int wafPos, DNSCode fdfDNSCode)
	{
		this.fontSize = fs;
		this.wishAssigneeFilter = wafPos;
		this.familyDNSFilterCode = fdfDNSCode;
	}
	
	//getters
	public int getFontSize() { return fontSize; }
	public int getWishAssigneeFilter() { return wishAssigneeFilter; }
	public DNSCode getFamilyDNSFilterCode() { return familyDNSFilterCode; }
	
	//setters
	public void setFontSize(int fs) { this.fontSize = fs; }
	public void setWishAssigneeFilter(int pos) { this.wishAssigneeFilter = pos; }
	public void setFamilyDNSFilterCode(DNSCode code) { this.familyDNSFilterCode = code; }
}
