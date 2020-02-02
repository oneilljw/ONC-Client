package ourneighborschild;

import java.util.Calendar;
import java.util.TimeZone;

public class ServerGVs extends ONCObject
{
	public static long TWENTY_FOUR_HOURS_MILLIS = 1000*60*60*24;
	public static final String DEFAULT_ADDRESS = "6476+Trillium+House+Lane+Centreville,VA";
	
	private Long giftDeliveryDayMillis;
	private Long seasonStartDayMillis;
	private String warehouseAddress;
	private Long oncGiftsReceivedDayMillis;
	private Long thanksgivingMealDeadlineMillis, decemberMealDeadlineMillis;
	private Long familyEditDeadlineMillis;
	private int defaultGiftID, defaultGiftCardID;
	private Long decemberGiftDeadlineMillis, waitlistGiftDeadlineMillis;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long td, Long decd, Long fed, int defaultGiftID,
			 int defaultGiftCardID, Long decMealDeadlone, Long wlDeadline)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		giftDeliveryDayMillis = dd;
		seasonStartDayMillis = ssd;
		warehouseAddress = wa;
		oncGiftsReceivedDayMillis = grd;
		thanksgivingMealDeadlineMillis = td;
		decemberGiftDeadlineMillis = decd;
		familyEditDeadlineMillis = fed;
		
		this.defaultGiftID = defaultGiftID;
		
		this.defaultGiftCardID = defaultGiftCardID;
		
		decemberMealDeadlineMillis = decMealDeadlone;
		waitlistGiftDeadlineMillis = wlDeadline;
	}
	
	public ServerGVs(ServerGVs sgvs)
	{
		super(0);
		this.giftDeliveryDayMillis = sgvs.giftDeliveryDayMillis;
		this.seasonStartDayMillis = sgvs.seasonStartDayMillis;
		this.warehouseAddress = sgvs.warehouseAddress;
		this.oncGiftsReceivedDayMillis = sgvs.oncGiftsReceivedDayMillis;
		this.thanksgivingMealDeadlineMillis = sgvs.thanksgivingMealDeadlineMillis;
		this.decemberGiftDeadlineMillis = sgvs.decemberGiftDeadlineMillis;
		this.familyEditDeadlineMillis = sgvs.familyEditDeadlineMillis;
		this.defaultGiftID = sgvs.defaultGiftID;
		this.defaultGiftCardID = sgvs.defaultGiftCardID;
		this.decemberMealDeadlineMillis = sgvs.decemberMealDeadlineMillis;
		this.waitlistGiftDeadlineMillis = sgvs.waitlistGiftDeadlineMillis;
	}

	//getters
	public Long getDeliveryDayMillis() { return giftDeliveryDayMillis; }
	public Long getSeasonStartDateMillis() { return seasonStartDayMillis; }
	String getWarehouseAddress() { return warehouseAddress; }
	public Long getGiftsReceivedDateMillis() { return oncGiftsReceivedDayMillis; }
	public Long getThanksgivingMealDeadlineMillis() { return thanksgivingMealDeadlineMillis; }
	public Long getDecemberGiftDeadlineMillis() { return decemberGiftDeadlineMillis; }
	public Long getFamilyEditDeadlineMillis() { return familyEditDeadlineMillis; }
	public int getDefaultGiftID() { return defaultGiftID; }
	public int getDefaultGiftCardID() { return defaultGiftCardID; }
	public Long getDecemberMealDeadlineMillis() { return decemberMealDeadlineMillis; }
	public Long getWaitListGiftDeadlineMillis() { return waitlistGiftDeadlineMillis; }
	public int getCurrentSeason()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(seasonStartDayMillis);
		return cal.get(Calendar.YEAR);
	}
	
	//setters
	void setDeliveryDayMillis(Long dd) { this.giftDeliveryDayMillis = dd; }
	void setSeasonStartDateMillis(Long ssd) { this.seasonStartDayMillis = ssd; }
	String setWarehouseAddress(String wa) { return warehouseAddress; }
	void setGiftsReceivedDateMillis(Long grd) { this.oncGiftsReceivedDayMillis = grd; }
	void setThanksgivingMealDeadlineMillis(Long tmd) { this.thanksgivingMealDeadlineMillis = tmd; }
	void setDecemberGiftDeadlineMillis(Long dgd) { this.decemberGiftDeadlineMillis = dgd; }
	void setFamilyEditDeadlineMillis(Long fed) { this.familyEditDeadlineMillis = fed; }
	void setDefaultGiftID(int defGID) { this.defaultGiftID = defGID; }
	void setDefaultGiftCardID(int defGCID) {this.defaultGiftCardID = defGCID; }
	void setDecemberMealDeadlineMillis(Long dmd) { this.decemberMealDeadlineMillis = dmd; }
	void setWaitListGiftDeadlineMillis(Long wlgd) { this.waitlistGiftDeadlineMillis = wlgd; }
	
	public boolean isDeliveryDay()
	{
		System.out.println(String.format("ServGlobVarDB.isDelDay: delDay %d, now %d, delDay+24 %d",
				giftDeliveryDayMillis,System.currentTimeMillis(),giftDeliveryDayMillis + TWENTY_FOUR_HOURS_MILLIS));
		
		return System.currentTimeMillis() > giftDeliveryDayMillis && 
				System.currentTimeMillis() < giftDeliveryDayMillis + TWENTY_FOUR_HOURS_MILLIS;
	}
	
	public boolean isDeliveryDayOrDayBefore()
	{
		return System.currentTimeMillis() > giftDeliveryDayMillis && 
				System.currentTimeMillis() < giftDeliveryDayMillis + (TWENTY_FOUR_HOURS_MILLIS *2);
	}
	
	
	//If season start date is midnight Sept 1, 2019 UTC (1567296000000), it is 8pm Aug 31, 2019 EDT
	//that means if this test is performed exactly at or after 8pm Aug 31, 2019, true will be returned.
	//If you want it to ask if it's past midnight on Sept 1, 2019 EDT, then we'd need to add the four
	//hour offset to the test by adding it to the seasonStartDayMillis. So, one way to do that is to 
	//determine what time zone we are in, calculate the offset to UTC, then perform the test and
	//return the result.
	public boolean isAfterSeasonStartDate() 
	{
		//gives you the current offset in ms from GMT at the current date
		long currentTime = System.currentTimeMillis();
		int offsetFromUTC = TimeZone.getDefault().getOffset(currentTime);
		
		return currentTime + offsetFromUTC >= seasonStartDayMillis; 
	}
	
	public boolean isInSeason(int year) 
	{ 
		Calendar yearEndCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		yearEndCal.set(Calendar.YEAR, year);
		yearEndCal.set(Calendar.MONTH, Calendar.DECEMBER);
		yearEndCal.set(Calendar.DAY_OF_MONTH, 31);
		yearEndCal.set(Calendar.HOUR, 11);
		yearEndCal.set(Calendar.MINUTE, 59);
		yearEndCal.set(Calendar.SECOND, 59);
		yearEndCal.set(Calendar.MILLISECOND, 999);
		
		return System.currentTimeMillis() >= seasonStartDayMillis && 
				System.currentTimeMillis() <= yearEndCal.getTimeInMillis();
	}
	
	boolean isBeforeGiftReceivedDeadline()
	{
		return System.currentTimeMillis() < decemberGiftDeadlineMillis;
	}
	
	@Override
	public String[] getExportRow()
	{	
		 String[] row = {
				 		Long.toString(giftDeliveryDayMillis),
 						Long.toString(seasonStartDayMillis),
 						warehouseAddress,
 						Long.toString(oncGiftsReceivedDayMillis),
 						Long.toString(thanksgivingMealDeadlineMillis),
 						Long.toString(decemberGiftDeadlineMillis),
 						Long.toString(familyEditDeadlineMillis),
 						Integer.toString(defaultGiftID),
 						Integer.toString(defaultGiftCardID),
 						Long.toString(decemberMealDeadlineMillis),
 						Long.toString(waitlistGiftDeadlineMillis),
 						};		
		return row;
	}
	
	//helpers
	Calendar getCalendar(Long day)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(day);
		return cal;
	}
}
