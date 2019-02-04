package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ServerGVs extends ONCObject
{
	private Calendar giftDeliveryDate;
	private Calendar oncSeasonStartDate;
	private String warehouseAddress;
	private Calendar oncGiftsReceivedDate;
	private Calendar thanksgivingMealDeadline, decemberMealDeadline;
	private Calendar familyEditDeadline;
	private int defaultGiftID, defaultGiftCardID, deliveryActivityID;
	private Calendar decemberGiftDeadline, waitlistGiftDeadline;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long td, Long decd, Long fed, int defaultGiftID,
			 int defaultGiftCardID, Long decMealDeadlone, Long wlDeadline, int defaultDeliveryActivityID)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		giftDeliveryDate = Calendar.getInstance();
		giftDeliveryDate.setTimeInMillis(dd);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTimeInMillis(ssd);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTimeInMillis(grd);
		
		thanksgivingMealDeadline = Calendar.getInstance();
		thanksgivingMealDeadline.setTimeInMillis(td);
		
		decemberGiftDeadline = Calendar.getInstance();
		decemberGiftDeadline.setTimeInMillis(decd);
		
		familyEditDeadline = Calendar.getInstance();
		familyEditDeadline.setTimeInMillis(fed);
		
		this.defaultGiftID = defaultGiftID;
		
		this.defaultGiftCardID = defaultGiftCardID;
		
		decemberMealDeadline = Calendar.getInstance();
		decemberMealDeadline.setTimeInMillis(decMealDeadlone);
		
		waitlistGiftDeadline = Calendar.getInstance();
		waitlistGiftDeadline.setTimeInMillis(wlDeadline);
		
		this.deliveryActivityID = defaultDeliveryActivityID;
	}
	
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
	
	//getters
	Date getDeliveryDate() { return giftDeliveryDate.getTime(); }
	public Calendar getDeliveryDateCal() { return giftDeliveryDate; }
	public Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	public Calendar getSeasonStartCal() { return oncSeasonStartDate; }
	String getWarehouseAddress() { return warehouseAddress; }
	public Date getGiftsReceivedDate() { return oncGiftsReceivedDate.getTime(); }
	public Calendar getGiftsReceivedDeadline() { return oncGiftsReceivedDate; }
	public Date getThanksgivingMealDeadline() { return thanksgivingMealDeadline.getTime(); }
	public Date getDecemberGiftDeadline() { return decemberGiftDeadline.getTime(); }
	public Date getFamilyEditDeadline() { return familyEditDeadline.getTime(); }
	public int getDefaultGiftID() { return defaultGiftID; }
	public int getDefaultGiftCardID() { return defaultGiftCardID; }
	public Date getDecemberMealDeadline() { return decemberMealDeadline.getTime(); }
	public Date getWaitListGiftDeadline() { return waitlistGiftDeadline.getTime(); }
	public int getDeliveryActivityID() { return deliveryActivityID; }
	
	//setters

	@Override
	public String[] getExportRow()
	{	
		 String[] row = {
				 		Long.toString(giftDeliveryDate.getTimeInMillis()),
 						Long.toString(oncSeasonStartDate.getTimeInMillis()),
 						warehouseAddress,
 						Long.toString(oncGiftsReceivedDate.getTimeInMillis()),
 						Long.toString(thanksgivingMealDeadline.getTimeInMillis()),
 						Long.toString(decemberGiftDeadline.getTimeInMillis()),
 						Long.toString(familyEditDeadline.getTimeInMillis()),
 						Integer.toString(defaultGiftID),
 						Integer.toString(defaultGiftCardID),
 						Long.toString(decemberMealDeadline.getTimeInMillis()),
 						Long.toString(waitlistGiftDeadline.getTimeInMillis()),
 						Integer.toString(deliveryActivityID)
 						};		
		return row;
	}
}
