package ourneighborschild;

public class UserPreferences 
{
	private int fontSize;
	private int wishAssigneeFilter;
	private int familyDNSFilter;
	
	public UserPreferences()
	{
		this.fontSize = 13;
		this.wishAssigneeFilter = 1;
		this.familyDNSFilter = 1;
	}
	
	public UserPreferences(int fs, int wafPos, int fdfPos)
	{
		this.fontSize = fs;
		this.wishAssigneeFilter = wafPos;
		this.familyDNSFilter = fdfPos;
	}
	
	//getters
	public int getFontSize() { return fontSize; }
	public int getWishAssigneeFilter() { return wishAssigneeFilter; }
	public int getFamilyDNSFilter() { return familyDNSFilter; }
	
	//setters
	public void setFontSize(int fs) { this.fontSize = fs; }
	public void setWishAssigneeFilter(int pos) { this.wishAssigneeFilter = pos; }
	public void setFamilyDNSFilter(int pos) { this.familyDNSFilter = pos; }
}
