package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ServerGVs extends ONCObject
{
	private Calendar oncDeliveryDate;
	private Calendar oncSeasonStartDate;
	private String warehouseAddress;
	private Calendar oncGiftsReceivedDate;
	private Calendar familyIntakeDeadline;
	private Calendar familyEditDeadline;
	
	public ServerGVs(Long dd, Long ssd, String wa, Long grd, Long fid, Long fed)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTimeInMillis(dd);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTimeInMillis(ssd);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTimeInMillis(grd);
		
		familyIntakeDeadline = Calendar.getInstance();
		familyIntakeDeadline.setTimeInMillis(fid);
		
		familyEditDeadline = Calendar.getInstance();
		familyEditDeadline.setTimeInMillis(fed);
	}
	
	public ServerGVs(Date delDate, Date seasonStartDate, String wa, Date giftsreceivedDate,
			Date familyIntakeDeadline, Date familyEditDeadline)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTime(delDate);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTime(seasonStartDate);
		
		warehouseAddress = wa;
		
		oncGiftsReceivedDate = Calendar.getInstance();
		oncGiftsReceivedDate.setTime(giftsreceivedDate);
		
		this.familyIntakeDeadline = Calendar.getInstance();
		this.familyIntakeDeadline.setTime(familyIntakeDeadline);
		
		this.familyEditDeadline = Calendar.getInstance();
		this.familyEditDeadline.setTime(familyEditDeadline);
	}
	
	//getters
	Date getDeliveryDate() { return oncDeliveryDate.getTime(); }
	Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	String getWarehouseAddress() { return warehouseAddress; }
	public Date getGiftsReceivedDate() { return oncGiftsReceivedDate.getTime(); }
	public Date getFamilyIntakeDeadline() { return familyIntakeDeadline.getTime(); }
	public Date getFamilyEditDeadline() { return familyEditDeadline.getTime(); }

	@Override
	public String[] getExportRow()
	{	
		 String[] row = {Long.toString(oncDeliveryDate.getTimeInMillis()),
 						 Long.toString(oncSeasonStartDate.getTimeInMillis()),
 						 warehouseAddress,
 						Long.toString(oncGiftsReceivedDate.getTimeInMillis()),
 						Long.toString(familyIntakeDeadline.getTimeInMillis()),
 						Long.toString(familyEditDeadline.getTimeInMillis())
 						 };		
		return row;
	}
}
