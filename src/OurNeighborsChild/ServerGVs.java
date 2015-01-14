package OurNeighborsChild;

import java.util.Calendar;
import java.util.Date;

public class ServerGVs extends ONCObject
{
	private Calendar oncDeliveryDate;
	private Calendar oncSeasonStartDate;
	private String warehouseAddress;
	private Calendar oncGiftsReceivedDate;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTimeInMillis(dd);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTimeInMillis(ssd);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTimeInMillis(grd);
	}
	
	public ServerGVs(Date delDate, Date seasonStartDate, String wa, Date giftsreceivedDate)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTime(delDate);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTime(seasonStartDate);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTime(giftsreceivedDate);
	}
	
	//getters
	Date getDeliveryDate() { return oncDeliveryDate.getTime(); }
	Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	String getWarehouseAddress() { return warehouseAddress; }
	public Date getGiftsReceivedDate() { return oncGiftsReceivedDate.getTime(); }

	@Override
	public String[] getExportRow()
	{	
		 String[] row = {Long.toString(oncDeliveryDate.getTimeInMillis()),
 						 Long.toString(oncSeasonStartDate.getTimeInMillis()),
 						 warehouseAddress,
 						Long.toString(oncGiftsReceivedDate.getTimeInMillis())
 						 };		
		return row;
	}
}
