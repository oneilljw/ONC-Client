package ourneighborschild;

public enum BatteryQty
{
	NONE("0", 0, "0900000"),
	ONE("1", 1,"0900001"),
	TWO("2",2,"0900002"),
	THREE("3",3,"0900003"),
	FOUR("4",4,"0900004"),
	FIVE("5",5,"0900005"),
	SIX("6",6,"0900006");
			
	private final String text;
	private final int value;
	private final String code;
	
	BatteryQty(String text, int value, String code)
	{
		this.text = text;
		this.value = value;
		this.code = code;
	}
	
	@Override public String toString() { return text; }
	public int value() { return value; }
	public String code() { return code; }
	
	public static BatteryQty find(String code)
	{
		BatteryQty result = null;
		for(BatteryQty bq : BatteryQty.values())
			if(bq.code().equals(code))
			{
				result = bq;
				break;
			}
		
		return result;
	}
	
	public static BatteryQty[] printValues()
	{
		return new BatteryQty[] {ONE,TWO,THREE,FOUR,FIVE,SIX};
	}
	
	public static String[] textValues()
	{
		return new String[] {NONE.text, ONE.text,TWO.text,THREE.text,FOUR.text, FIVE.text,SIX.text};
	}
}
