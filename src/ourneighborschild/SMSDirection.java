package ourneighborschild;

public enum SMSDirection
{
	ANY ("Any"),
	INBOUND ("Inbound"), 
	OUTBOUND_API ("Outbound API"),
	OUTBOUND_CALL ("Outbound Call"), 
	OUTBOUND_REPLY ("Outbound Reply"), 
	UNKNOWN ("Unknown");
	
	private final String englishName;
	
	SMSDirection(String englishName)
	{
		this.englishName = englishName;
	}
	
	String englishName() { return englishName; }
	
	@Override
	public String toString() { return englishName; }
}
