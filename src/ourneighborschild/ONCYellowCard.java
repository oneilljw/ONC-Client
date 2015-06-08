package ourneighborschild;

public class ONCYellowCard
{
	private String		oncnum;
	private String		nameandaddress;
	private String		region;
	private String		homephone;
	private String		otherphone;
	private String		language;
	private String		oncnotes;
	private String		nFBags;
	private String		nBikes;
	private String		nOTL;

	ONCYellowCard(String[] ycData, String reg)
	{
		oncnum = ycData[0];
		nameandaddress = ycData[1];
		region = reg;
		homephone = ycData[3];
		otherphone = ycData[4];
		language = ycData[5];
		oncnotes = ycData[6];
		nFBags = ycData[7];
		nBikes = ycData[8];
		nOTL = ycData[9];
	}
	
	//getters
	public String getoncnum() { return oncnum; }
	public String getnameandaddress() { return nameandaddress; }
	public String getregion() { return region; }
	public String gethomephone() { return homephone; }
	public String getotherphone() { return otherphone; }
	public String getlanguage() { return language; }
	public String getoncnotes() { return oncnotes; }
	public String getnFBags() { return nFBags; }
	public String getnBikes() { return nBikes; }
	public String getnOTL() { return nOTL; }
	
	//setters
	public void setoncnum(String oncn) { oncnum = oncn; }
	public void setnameandaddress(String naa) { nameandaddress = naa; }
	public void setregion(String r) { region = r; }
	public void sethomephone(String hp) { homephone = hp; }
	public void setotherphone(String op) { otherphone = op; }
	public void setlanguage(String lang) { language = lang; }
	public void setoncnotes(String n) { oncnotes = n; }
	public void setnFBags(String nbags) { nFBags = nbags; }
	public void setnBikes(String nbikes) { nBikes = nbikes; }
	public void setnOTL(String notl) { nOTL = notl; }
}

	