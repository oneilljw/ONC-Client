package ourneighborschild;

public class WebsiteStatus 
{
	protected boolean  bWebsiteLive;
	protected String zTimeBackUp;
	
	public WebsiteStatus(boolean bWebsiteLive, String zTimeBackUp)
	{
		this.bWebsiteLive = bWebsiteLive;
		this.zTimeBackUp = zTimeBackUp;
	}
	
	//getters
	public boolean getWebsiteStatus() { return bWebsiteLive; }
	public String getTimeBackUp() { return zTimeBackUp; }
	
	//setters
	public void setWebsiteStatus(boolean bStatus) { bWebsiteLive = bStatus; }
	public void setTimeBackUp(String timeBackUp) { zTimeBackUp = timeBackUp; }
}
