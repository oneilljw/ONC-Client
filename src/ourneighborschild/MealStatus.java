package ourneighborschild;

public enum MealStatus 
{
	Any, None, Requested, Referred;

	static MealStatus[] getSearchFilterList()
	{
		MealStatus[] msSearch = {MealStatus.Any, MealStatus.None, MealStatus.Requested, MealStatus.Referred};
		
		return msSearch;
	}
}
