package ourneighborschild;

public enum MealStatus 
{
	Any, None, Requested, Referred, Thanksgiving_Confirmed, December_Confirmed, Both_Confirmed;

	static MealStatus[] getSearchFilterList()
	{
		MealStatus[] msSearch = {MealStatus.Any,
									MealStatus.None, 
									MealStatus.Requested,
									MealStatus.Referred,
									MealStatus.Thanksgiving_Confirmed,
									MealStatus.December_Confirmed,
									MealStatus.Both_Confirmed};

		return msSearch;
	}
}
