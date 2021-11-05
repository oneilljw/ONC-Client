package ourneighborschild;

public class PhoneInfo
{
	private String number;
	private int code;	//0-empty or invalid, 1- mobile, 2 - UNUSED, 3 - Landline or VOIP
	private String type;
	private String carrier;
	
	public PhoneInfo(String number, int code, String type, String carrier)
	{
		this.number = number;
		this.code = code;
		this.type = type;
		this.carrier = carrier;
	}
	
	public PhoneInfo()
	{
		this.number = "";
		this.code = 0;
		this.type = "";
		this.carrier = "";
	}
	
	//getters
	String getPhoneNumber() { return number; }
	public int getCode() { return code; }
	public String getType() { return type; }
	String getCarrier() { return carrier; }
	
	public boolean isPhoneValid() { return code == 1 || code == 3; }
	boolean isEmpty() { return number.isEmpty(); }
}
