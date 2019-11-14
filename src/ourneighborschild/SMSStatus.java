package ourneighborschild;

public enum SMSStatus
{
	ANY ("Any"),
	REQUESTED ("Requested"),
	VALIDATED ("Validated"),
	ACCEPTED ("Accepted"),
	QUEUED ("Queued"),
	SENDING ("Sending"),
	SENT ("Sent"),
	RECEIVING ("Receiving"),
	RECEIVED ("Recceived"),
	DELIVERED ("Delivered"),
	UNDELIVERED ("Undelivered"),
	FAILED ("Failed"),
	ERR_NO_PHONE("ERR: No Phone"),
	ERR_NOT_MOBILE ("ERR: Not Mobile"),
	ERROR ("Error");
	
	private final String englishName;
	
	SMSStatus(String englishName)
	{
		this.englishName = englishName;
	}
	
	String englishName() { return englishName; }
	
	@Override
	public String toString() { return englishName; }
}
