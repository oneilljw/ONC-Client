package ourneighborschild;

public enum ActivityCode 
{
	Any (0, "Any"),
	No_Change (0, "No_Change"),
	Delivery (1, "Delivery Driver"),
	Warehouse (2, "Warehouse Volunteer"),
	Packager (4, "Packaging Volunteer");
	
	private final int code;
	private final String activity;
	
	ActivityCode(int code, String activity)
	{
		this.code = code;
		this.activity = activity;
	}
	
	public int code() { return code; }
	String activity() { return activity; }
	
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
}
