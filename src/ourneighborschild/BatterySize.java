package ourneighborschild;

public enum BatterySize
{
	NONE("", "0800000"),
	AA("AA","0800001"),
	AAA("AAA","0800002"),
	AG13("AG13","0800003"),
	FIVE_VOLT("5V","0800004"),
	C("C","0800005"),
	CR927("CR927","0800006"),
	D("D","0800007"),
	NINE_VOLT("9V","0800008"),
	LI_POLY3_7V("3.7V LI-POLY", "0080009"),
	LR03("LR03","0800010"),
	LR41("LR41","0800011"),
	LR44("LR44","0800012"),
	Other("Other","0800000");;
			
	private final String text;
	private final String code;
	
	BatterySize(String text, String code)
	{
		this.text = text;
		this.code = code;
	}
	
	@Override public String toString() { return text; }
	
	public String code() { return code; }
	
	public static BatterySize find(String code)
	{
		BatterySize result = null;
		for(BatterySize bs : BatterySize.values())
			if(bs.code().equals(code))
			{
				result = bs;
				break;
			}
		
		return result;
	}
	
	public static String[] filterList()
	{
		return new String[] {"All", AA.text,AAA.text,AG13.text,FIVE_VOLT.text,C.text,CR927.text,D.text,NINE_VOLT.text,
				LI_POLY3_7V.text,LR03.text,LR41.text,LR44.text};
	}
	
	public static BatterySize[] searchList()
	{
		return new BatterySize[] {AA,AAA,NINE_VOLT,C,D,FIVE_VOLT,AG13,CR927,LI_POLY3_7V,LR03,LR41,LR44};
	}
	
	public static String[] textValues()
	{
		return new String[] {NONE.text, AA.text,AAA.text,AG13.text,FIVE_VOLT.text,C.text,CR927.text,D.text,NINE_VOLT.text,
				LI_POLY3_7V.text,LR03.text,LR41.text,LR44.text};
	}
}
