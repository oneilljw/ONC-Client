package ourneighborschild;

public enum GroupType 
{
	Any,
	Business,
	Church,
	Community,
	Government,
	Mixed,
	School,
	Volunteer;
	
	static GroupType[] getSearchFilterList()
	{
		GroupType[] wsSearch = {GroupType.Any, GroupType.Business, GroupType.Church,
					GroupType.Community, GroupType.Government, GroupType.Mixed,
					GroupType.School, GroupType.Volunteer};
		
		return wsSearch;
	}
	
	static GroupType[] getGroupTypeList()
	{
		GroupType[] wsSearch = {GroupType.Business, GroupType.Church,
					GroupType.Community, GroupType.Government, GroupType.Mixed,
					GroupType.School, GroupType.Volunteer};
		
		return wsSearch;
	}
}
