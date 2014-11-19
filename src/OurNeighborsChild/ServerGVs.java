package OurNeighborsChild;

import java.util.Calendar;
import java.util.Date;

public class ServerGVs extends ONCObject
{
	private Calendar oncDeliveryDate;
	private Calendar oncSeasonStartDate;
	private String warehouseAddress;
	
	public ServerGVs(Long dd, Long ssd, String wa)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTimeInMillis(dd);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTimeInMillis(ssd);
		
		warehouseAddress = wa;
	}
	
	public ServerGVs(Date delDate, Date seasonStartDate, String wa)
	{
		super(0); //id is 0 since there is only one instance of server global variables
		oncDeliveryDate = Calendar.getInstance();
		oncDeliveryDate.setTime(delDate);
		
		oncSeasonStartDate = Calendar.getInstance();
		oncSeasonStartDate.setTime(seasonStartDate);
		
		warehouseAddress = wa;
	}
	
	//getters
	Date getDeliveryDate() { return oncDeliveryDate.getTime(); }
	Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	String getWarehouseAddress() { return warehouseAddress; }

	@Override
	public String[] getDBExportRow()
	{	
		 String[] row = {Long.toString(oncDeliveryDate.getTimeInMillis()),
 						 Long.toString(oncSeasonStartDate.getTimeInMillis()),
 						 warehouseAddress};		
		return row;
	}
}
