package ourneighborschild;

public class WebsiteStatus 
{
	private boolean  bWebsiteOnline, bWebsiteLoggingEnabled;
	private String zTimeBackUp;
	
	public WebsiteStatus(boolean bWebsiteOnline, boolean bWebsiteLoggingEnabled, String zTimeBackUp)
	{
		this.bWebsiteOnline = bWebsiteOnline;
		this.bWebsiteLoggingEnabled = bWebsiteLoggingEnabled;
		this.zTimeBackUp = zTimeBackUp;
	}
	
	//copy constructor
	public WebsiteStatus(WebsiteStatus ws)
	{
		this.bWebsiteOnline = ws.bWebsiteOnline;
		this.bWebsiteLoggingEnabled = ws.bWebsiteLoggingEnabled;
		this.zTimeBackUp = ws.zTimeBackUp;
	}
	
	//getters
	public boolean isWebsiteOnline() { return bWebsiteOnline; }
	public boolean isWebsiteLoggingEnabled() { return bWebsiteLoggingEnabled; }
	public String getTimeBackUp() { return zTimeBackUp; }
	
	//setters
	public void setWebsiteStatus(boolean bStatus) { bWebsiteOnline = bStatus; }
	public void setWebsiteLogginEnabled(boolean bTF) { bWebsiteLoggingEnabled = bTF; }
	public void setTimeBackUp(String timeBackUp) { zTimeBackUp = timeBackUp; }
}
