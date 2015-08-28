package ourneighborschild;

public enum Transportation
{
	Any, Yes, No, TBD;

	static Transportation[] getSearchFilterList()
	{
		Transportation[] msSearch = {Transportation.Any, Transportation.Yes,
									 Transportation.No, Transportation.TBD};
		return msSearch;
	}
}
