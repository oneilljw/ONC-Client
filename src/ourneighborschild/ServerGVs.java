package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ServerGVs extends ONCObject
{
	private Calendar oncDeliveryDate;
	private Calendar oncSeasonStartDate;
	private String warehouseAddress;
	private Calendar oncGiftsReceivedDate;
	private Calendar thanksgivingDeadline, decemberDeadline;
	private Calendar familyEditDeadline;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long td, Long decd, Long fed)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTimeInMillis(dd);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTimeInMillis(ssd);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTimeInMillis(grd);
		
		thanksgivingDeadline = Calendar.getInstance();
		thanksgivingDeadline.setTimeInMillis(td);
		
		decemberDeadline = Calendar.getInstance();
		decemberDeadline.setTimeInMillis(decd);
		
		familyEditDeadline = Calendar.getInstance();
		familyEditDeadline.setTimeInMillis(fed);
	}
	
	public ServerGVs(Date delDate, Date seasonStartDate, String wa, Date giftsreceivedDate,
			Date thanksgivingDeadline, Date decemberDeadline, Date familyEditDeadline)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTime(delDate);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTime(seasonStartDate);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTime(giftsreceivedDate);
		
		this.thanksgivingDeadline = Calendar.getInstance();
		this.thanksgivingDeadline.setTime(thanksgivingDeadline);
		
		this.decemberDeadline = Calendar.getInstance();
		this.decemberDeadline.setTime(decemberDeadline);
		
		this.familyEditDeadline = Calendar.getInstance();
		this.familyEditDeadline.setTime(familyEditDeadline);
	}
	
	//getters
	Date getDeliveryDate() { return oncDeliveryDate.getTime(); }
	public Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	String getWarehouseAddress() { return warehouseAddress; }
	public Date getGiftsReceivedDate() { return oncGiftsReceivedDate.getTime(); }
	public Calendar getGiftsReceivedDeadline() { return oncGiftsReceivedDate; }
	public Date getThanksgivingDeadline() { return thanksgivingDeadline.getTime(); }
	public Date getDecemberDeadline() { return decemberDeadline.getTime(); }
	public Date getFamilyEditDeadline() { return familyEditDeadline.getTime(); }

	@Override
	public String[] getExportRow()
	{	
		 String[] row = {Long.toString(oncDeliveryDate.getTimeInMillis()),
 						 Long.toString(oncSeasonStartDate.getTimeInMillis()),
 						 warehouseAddress,
 						Long.toString(oncGiftsReceivedDate.getTimeInMillis()),
 						Long.toString(thanksgivingDeadline.getTimeInMillis()),
 						Long.toString(decemberDeadline.getTimeInMillis()),
 						Long.toString(familyEditDeadline.getTimeInMillis())
 						 };		
		return row;
	}
}
