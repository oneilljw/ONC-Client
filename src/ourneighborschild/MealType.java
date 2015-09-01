package ourneighborschild;

public enum MealType
{
	Any, No_Assistance_Rqrd, Thanksgiving, December, Both;
	
	static MealType[] getSearchFilterList()
	{
		MealType[] mtSearch = {MealType.Any, MealType.Thanksgiving, 
								MealType.December, MealType.Both};
		
		return mtSearch;
	}
	
	static MealType[] getSelectionList()
	{
		MealType[] mtSearch = {MealType.Thanksgiving, MealType.December, MealType.Both};
		
		return mtSearch;
	}
}
