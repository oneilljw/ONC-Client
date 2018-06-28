package ourneighborschild;

public enum Frequency
{
	NEVER (0, "Never"),
	ONE_TIME(0, "One Time"),
	MINUTE(1000*60, "1 minute"),
	FIVE_MINUTES(1000*60*5, "5 minutes"),
	HALF_HOUR (1000*60*30, "30 minutes"),
	HOUR (1000*60*60, "1 hour"),
	TWO_HOURS (1000*60*120, "2 hours"),
	FOUR_HOURS (1000*60*240, "4 hours"),
	EIGHT_HOURS (1000*60*60*8, "8 hours"),
	DAILY (1000*60*60*24, "Daily"),
	WEEKLY (1000*60*60*168, "Weekly");
	
	private final long interval;
	private final String text;
	
	Frequency(int interval, String text)
	{
		this.interval = interval;
		this.text = text;
	}
	
	public long interval() { return interval; }
	public String toString() { return text; }
	
	static Frequency[] getGeniusImportFrequencies()
	{
		Frequency[] frequency = {Frequency.NEVER, Frequency.ONE_TIME, Frequency.FIVE_MINUTES,
								Frequency.HALF_HOUR, Frequency.HOUR, Frequency.TWO_HOURS,
								Frequency.FOUR_HOURS, Frequency.EIGHT_HOURS, Frequency.DAILY,
								Frequency.WEEKLY};
		
		return frequency;
	}
}
