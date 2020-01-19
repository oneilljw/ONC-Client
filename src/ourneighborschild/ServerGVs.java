package ourneighborschild;

import java.util.Calendar;
import java.util.TimeZone;

public class ServerGVs extends ONCObject
{
	private Long giftDeliveryDate;
	private Long oncSeasonStartDate;
	private String warehouseAddress;
	private Long oncGiftsReceivedDate;
	private Long thanksgivingMealDeadline, decemberMealDeadline;
	private Long familyEditDeadline;
	private int defaultGiftID, defaultGiftCardID, deliveryActivityID;
	private Long decemberGiftDeadline, waitlistGiftDeadline;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long td, Long decd, Long fed, int defaultGiftID,
			 int defaultGiftCardID, Long decMealDeadlone, Long wlDeadline, int defaultDeliveryActivityID)
	{
		super(0); //id is 0 since there is only one instance of server global variables
//		giftDeliveryDate = Calendar.getInstance();
//		giftDeliveryDate.setTimeInMillis(dd);
		giftDeliveryDate = dd;
		
//		oncSeasonStartDate = Calendar.getInstance();
//		oncSeasonStartDate.setTimeInMillis(ssd);
		oncSeasonStartDate = ssd;
		
		warehouseAddress = wa;
		
//		oncGiftsReceivedDate = Calendar.getInstance();
//		oncGiftsReceivedDate.setTimeInMillis(grd);
		oncGiftsReceivedDate = grd;
		
//		thanksgivingMealDeadline = Calendar.getInstance();
//		thanksgivingMealDeadline.setTimeInMillis(td);
		thanksgivingMealDeadline = td;
		
		
//		decemberGiftDeadline = Calendar.getInstance();
//		decemberGiftDeadline.setTimeInMillis(decd);
		decemberGiftDeadline = decd;
		
//		familyEditDeadline = Calendar.getInstance();
//		familyEditDeadline.setTimeInMillis(fed);
		familyEditDeadline = fed;
		
		this.defaultGiftID = defaultGiftID;
		
		this.defaultGiftCardID = defaultGiftCardID;
		
//		decemberMealDeadline = Calendar.getInstance();
//		decemberMealDeadline.setTimeInMillis(decMealDeadlone);
		decemberMealDeadline = decMealDeadlone;
		
//		waitlistGiftDeadline = Calendar.getInstance();
//		waitlistGiftDeadline.setTimeInMillis(wlDeadline);
		waitlistGiftDeadline = wlDeadline;
		
		this.deliveryActivityID = defaultDeliveryActivityID;
	}
/*	
	public ServerGVs(Date delDate, Date seasonStartDate, String wa, Date giftsreceivedDate,
			Date thanksgivingDeadline, Date decemberDeadline, Date familyEditDeadline, int defaultGiftID,
			int defaultGiftCardID, Date decMealDeadline, Date waitlistGiftDeadline,
			int defaultDeliveryActivityID)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		giftDeliveryDate = Calendar.getInstance();
		giftDeliveryDate.setTime(delDate);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTime(seasonStartDate);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTime(giftsreceivedDate);
		
		this.thanksgivingMealDeadline = Calendar.getInstance();
		this.thanksgivingMealDeadline.setTime(thanksgivingDeadline);
		
		this.decemberGiftDeadline = Calendar.getInstance();
		this.decemberGiftDeadline.setTime(decemberDeadline);
		
		this.familyEditDeadline = Calendar.getInstance();
		this.familyEditDeadline.setTime(familyEditDeadline);
		
		this.defaultGiftID = defaultGiftID;
		
		this.defaultGiftCardID = defaultGiftCardID;
		
		this.decemberMealDeadline = Calendar.getInstance();
		this.decemberMealDeadline.setTime(decMealDeadline);
		
		this.waitlistGiftDeadline = Calendar.getInstance();
		this.waitlistGiftDeadline.setTime(waitlistGiftDeadline);
		
		this.deliveryActivityID = defaultDeliveryActivityID;
	}
*/	
	//getters
	Long getDeliveryDate() { return giftDeliveryDate; }
	public Calendar getDeliveryDateCal() { return getCalendar(giftDeliveryDate); }
	public Long getSeasonStartDate() { return oncSeasonStartDate; }
	public Calendar getSeasonStartCal() { return getCalendar(oncSeasonStartDate); }
	String getWarehouseAddress() { return warehouseAddress; }
	public Long getGiftsReceivedDate() { return oncGiftsReceivedDate; }
	public Calendar getGiftsReceivedDeadline() { return getCalendar(oncGiftsReceivedDate); }
	public Long getThanksgivingMealDeadline() { return thanksgivingMealDeadline; }
	public Long getDecemberGiftDeadline() { return decemberGiftDeadline; }
	public Long getFamilyEditDeadline() { return familyEditDeadline; }
	public int getDefaultGiftID() { return defaultGiftID; }
	public int getDefaultGiftCardID() { return defaultGiftCardID; }
	public Long getDecemberMealDeadline() { return decemberMealDeadline; }
	public Long getWaitListGiftDeadline() { return waitlistGiftDeadline; }
	public int getDeliveryActivityID() { return deliveryActivityID; }
	
	//setters
	@Override
	public String[] getExportRow()
	{	
		 String[] row = {
				 		Long.toString(giftDeliveryDate),
 						Long.toString(oncSeasonStartDate),
 						warehouseAddress,
 						Long.toString(oncGiftsReceivedDate),
 						Long.toString(thanksgivingMealDeadline),
 						Long.toString(decemberGiftDeadline),
 						Long.toString(familyEditDeadline),
 						Integer.toString(defaultGiftID),
 						Integer.toString(defaultGiftCardID),
 						Long.toString(decemberMealDeadline),
 						Long.toString(waitlistGiftDeadline),
 						Integer.toString(deliveryActivityID)
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
