package ourneighborschild;

public enum ActivityCode 
{
	Any (0, "Any"),
	No_Change (0, "No_Change"),
	Delivery_SetUp (1, "Delivery Set-Up"),
	Delivery (2, "Delivery"),
	Warehouse (4, "Warehouse Support"),
	Packager (8, "Packaging"),
	Gift_Inventory (16, "Gift Inventory"),
	Shopper (32, "Shopping"),
	Cookie_Baker (64, "Cookie Baker"),
	Warehouse_CleanUp (128, "Warehouse Clean-Up");
	
	private final int code;
	private final String activity;
	
	ActivityCode(int code, String activity)
	{
		this.code = code;
		this.activity = activity;
	}
	
	
	public int code() { return code; }
	String activity() { return activity; }
	static int lastCode() 
	{ 
		ActivityCode[] actCodes = ActivityCode.values();
		if(actCodes.length == 0)
			return 0;
		else
			return actCodes[actCodes.length-1].code;
	}
	
	static ActivityCode getActivity(int code)
	{
		ActivityCode[] activities = ActivityCode.getActivityList();
		
		int index = 0;
		while(index < activities.length && code != activities[index].code)
			index++;
		
		if(index < activities.length)
			return activities[index];
		else
			return null;
	}
	
	static ActivityCode[] getSearchFilterList()
	{
		ActivityCode[] actSearch = {ActivityCode.Any, ActivityCode.Delivery_SetUp,
									ActivityCode.Delivery, ActivityCode.Warehouse, 
									ActivityCode.Packager, ActivityCode.Gift_Inventory,
									ActivityCode.Shopper, ActivityCode.Cookie_Baker,
									ActivityCode.Warehouse_CleanUp};
							
		return actSearch;
	}
	
	static ActivityCode[] getChangeList()
	{
		ActivityCode[] actChange = {ActivityCode.No_Change, ActivityCode.Delivery_SetUp,
									ActivityCode.Delivery, ActivityCode.Warehouse, 
									ActivityCode.Packager, ActivityCode.Gift_Inventory,
									ActivityCode.Shopper, ActivityCode.Cookie_Baker,
									ActivityCode.Warehouse_CleanUp};
		
		return actChange;
	}
	
	static ActivityCode[] getActivityList()
	{
		ActivityCode[] actChange = {ActivityCode.Delivery_SetUp,
									ActivityCode.Delivery, ActivityCode.Warehouse, 
									ActivityCode.Packager, ActivityCode.Gift_Inventory,
									ActivityCode.Shopper, ActivityCode.Cookie_Baker,
									ActivityCode.Warehouse_CleanUp};
		
		return actChange;
	}
}
