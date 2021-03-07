package ourneighborschild;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ServerGVs extends ONCObject
{
	public static long TWENTY_FOUR_HOURS_MILLIS = 1000*60*60*24;
	public static final String DEFAULT_ADDRESS = "6476+Trillium+House+Lane+Centreville,VA";
//	public static int DEFAULT_WISH_TYPE_ARRAY_LENGTH = 3;
	
	private Long giftDeliveryDayMillis;
	private Long seasonStartDayMillis;
	private String warehouseAddress;
	private Long giftsReceivedDeadlineMillis;
	private Long thanksgivingMealDeadlineMillis, decemberMealDeadlineMillis;
	private Long familyEditDeadlineMillis;
	private int defaultGiftID, defaultGiftCardID;
	private Long decemberGiftDeadlineMillis, waitlistGiftDeadlineMillis;
	private int numberOfGiftsPerChild;
	private int childWishIntakeConfiguration;
	private GiftDistribution giftDistribution;
	private int mealIntake;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long td, Long decd, Long fed, int defaultGiftID,
			 int defaultGiftCardID, Long decMealDeadlone, Long wlDeadline,  int numGiftsPerChild,
			 int childWishIntakeConfiguration, GiftDistribution giftDistribution, int mealIntake)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		this.giftDeliveryDayMillis = dd;
		this.seasonStartDayMillis = ssd;
		this.warehouseAddress = wa;
		this.giftsReceivedDeadlineMillis = grd;
		this.thanksgivingMealDeadlineMillis = td;
		this.decemberGiftDeadlineMillis = decd;
		this.familyEditDeadlineMillis = fed;
		
		this.defaultGiftID = defaultGiftID;
		
		this.defaultGiftCardID = defaultGiftCardID;
		
		this.decemberMealDeadlineMillis = decMealDeadlone;
		this.waitlistGiftDeadlineMillis = wlDeadline;
		
		this.numberOfGiftsPerChild = numGiftsPerChild;
		this.childWishIntakeConfiguration = childWishIntakeConfiguration;
		
		this.giftDistribution = giftDistribution;
		this.mealIntake = mealIntake;
	}
	
	public ServerGVs(ServerGVs sgvs)
	{
		super(0);
		this.giftDeliveryDayMillis = sgvs.giftDeliveryDayMillis;
		this.seasonStartDayMillis = sgvs.seasonStartDayMillis;
		this.warehouseAddress = sgvs.warehouseAddress;
		this.giftsReceivedDeadlineMillis = sgvs.giftsReceivedDeadlineMillis;
		this.thanksgivingMealDeadlineMillis = sgvs.thanksgivingMealDeadlineMillis;
		this.decemberGiftDeadlineMillis = sgvs.decemberGiftDeadlineMillis;
		this.familyEditDeadlineMillis = sgvs.familyEditDeadlineMillis;
		this.defaultGiftID = sgvs.defaultGiftID;
		this.defaultGiftCardID = sgvs.defaultGiftCardID;
		this.decemberMealDeadlineMillis = sgvs.decemberMealDeadlineMillis;
		this.waitlistGiftDeadlineMillis = sgvs.waitlistGiftDeadlineMillis;
		this.numberOfGiftsPerChild = sgvs.numberOfGiftsPerChild;
		this.childWishIntakeConfiguration = sgvs.childWishIntakeConfiguration;
		this.giftDistribution = sgvs.giftDistribution;
		this.mealIntake = sgvs.mealIntake;
	}
	
	public ServerGVs()	//default server global variables
	{
		super(0);
		
		long timeNow = System.currentTimeMillis();
		
		this.giftDeliveryDayMillis = timeNow;
		this.seasonStartDayMillis = timeNow;
		this.warehouseAddress = DEFAULT_ADDRESS;
		this.giftsReceivedDeadlineMillis = timeNow;
		this.thanksgivingMealDeadlineMillis = timeNow;
		this.decemberGiftDeadlineMillis = timeNow;
		this.familyEditDeadlineMillis = timeNow;
		this.defaultGiftID = -1;
		this.defaultGiftCardID = -1;
		this.decemberMealDeadlineMillis = timeNow;
		this.waitlistGiftDeadlineMillis = timeNow;
		this.numberOfGiftsPerChild = 3;
		this.childWishIntakeConfiguration = 0xFFFF;
		this.giftDistribution = GiftDistribution.None;
		this.mealIntake = 0;
	}

	//getters
	public Long getDeliveryDayMillis() { return giftDeliveryDayMillis; }
	public Long getSeasonStartDateMillis() { return seasonStartDayMillis; }
	String getWarehouseAddress() { return warehouseAddress; }
	public Long getGiftsReceivedDeadlineMillis() { return giftsReceivedDeadlineMillis; }
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
	public int getNumberOfGiftsPerChild() { return numberOfGiftsPerChild; }
	public int getChildWishIntakeConfiguraiton() { return childWishIntakeConfiguration; }
	public GiftDistribution getGiftDistribution() { return giftDistribution; }
	public int getMealIntake() { return mealIntake; }
	
	//setters
	void setDeliveryDayMillis(Long dd) { this.giftDeliveryDayMillis = dd; }
	void setSeasonStartDateMillis(Long ssd) { this.seasonStartDayMillis = ssd; }
	void setWarehouseAddress(String wa) { this.warehouseAddress = wa; }
	void setGiftsReceivedDateMillis(Long grd) { this.giftsReceivedDeadlineMillis = grd; }
	void setThanksgivingMealDeadlineMillis(Long tmd) { this.thanksgivingMealDeadlineMillis = tmd; }
	void setDecemberGiftDeadlineMillis(Long dgd) { this.decemberGiftDeadlineMillis = dgd; }
	void setFamilyEditDeadlineMillis(Long fed) { this.familyEditDeadlineMillis = fed; }
	void setDefaultGiftID(int defGID) { this.defaultGiftID = defGID; }
	void setDefaultGiftCardID(int defGCID) {this.defaultGiftCardID = defGCID; }
	void setDecemberMealDeadlineMillis(Long dmd) { this.decemberMealDeadlineMillis = dmd; }
	void setWaitListGiftDeadlineMillis(Long wlgd) { this.waitlistGiftDeadlineMillis = wlgd; }
	void setNumberOfGiftsPerChild(int numGifts) { this.numberOfGiftsPerChild = numGifts; }
	void setChildWishIntakeConfiguration(int config) { this.childWishIntakeConfiguration = config; }
	void setGiftDistribution(GiftDistribution dist) { this.giftDistribution = dist; }
	void setMealIntake(int intake) { this.mealIntake = intake; }
	
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
		List<String> row = new ArrayList<String>();
		row.add(Long.toString(giftDeliveryDayMillis));
		row.add(Long.toString(seasonStartDayMillis));
		row.add(warehouseAddress);
 		row.add(Long.toString(giftsReceivedDeadlineMillis));
 		row.add(Long.toString(thanksgivingMealDeadlineMillis));
 		row.add(Long.toString(decemberGiftDeadlineMillis));
 		row.add(Long.toString(familyEditDeadlineMillis));
 		row.add(Integer.toString(defaultGiftID));
 		row.add(Integer.toString(defaultGiftCardID));
 		row.add(Long.toString(decemberMealDeadlineMillis));
 		row.add(Long.toString(waitlistGiftDeadlineMillis));
 		row.add(Integer.toString(numberOfGiftsPerChild));
 		row.add(Integer.toString(childWishIntakeConfiguration));
 		row.add(Integer.toString(giftDistribution.index()));
 		row.add(Integer.toString(mealIntake));
 								
		return row.toArray(new String[row.size()]);
	}
}
