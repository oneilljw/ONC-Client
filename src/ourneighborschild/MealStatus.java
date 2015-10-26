package ourneighborschild;

public enum MealStatus 
{
	Any, No_Change, None, Requested, Assigned, Referred, Thanksgiving_Confirmed, December_Confirmed, Both_Confirmed;

	static MealStatus[] getSearchFilterList()
	{
		MealStatus[] msSearch = {MealStatus.Any,
									MealStatus.None, 
									MealStatus.Requested,
									MealStatus.Assigned, 
									MealStatus.Referred,
									MealStatus.Thanksgiving_Confirmed,
									MealStatus.December_Confirmed,
									MealStatus.Both_Confirmed};

		return msSearch;
	}
	
	static MealStatus[] getChangeList()
	{
		MealStatus[] msSearch = {MealStatus.No_Change, 
									MealStatus.Referred,
									MealStatus.Thanksgiving_Confirmed,
									MealStatus.December_Confirmed,
									MealStatus.Both_Confirmed};

		return msSearch;
	}
}
