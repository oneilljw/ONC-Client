package ourneighborschild;

public enum ActivityCode 
{
	Any (0, "Any"),
	No_Change (0, "No_Change"),
	Delivery (1, "Delivery"),
	Warehouse (2, "Warehouse Support"),
	Packager (4, "Packaging");
	
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
		ActivityCode[] actSearch = {ActivityCode.Any, ActivityCode.Delivery,
									ActivityCode.Warehouse, ActivityCode.Packager};
							
		return actSearch;
	}
	
	static ActivityCode[] getChangeList()
	{
		ActivityCode[] actChange = {ActivityCode.No_Change, ActivityCode.Delivery,
									ActivityCode.Warehouse, ActivityCode.Packager};
		
		return actChange;
	}
	
	static ActivityCode[] getActivityList()
	{
		ActivityCode[] actChange = { ActivityCode.Delivery, ActivityCode.Warehouse, 
									 ActivityCode.Packager};
		
		return actChange;
	}
}
