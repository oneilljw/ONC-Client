package ourneighborschild;

public class BarcodeInfo
{
	private String code;
	private String label;
	
	BarcodeInfo(BatterySize bs)
	{
		this.code = bs.code();
		this.label = bs.toString();
	}
	
	BarcodeInfo(BatteryQty bq)
	{
		this.code = bq.code();
		this.label = bq.toString();
	}
	
	String code() { return code; }
	String label() { return label; }
}
