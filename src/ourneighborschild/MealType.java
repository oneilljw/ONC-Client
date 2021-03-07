package ourneighborschild;

public enum MealType
{
	Any(0, "Any"),
	No_Assistance_Rqrd(1, "No Assistance Req."),
	Thanksgiving(2, "Thanksgiving"),
	December(3, "December"),
	Thanksgiving_December(4, "Thanksgiving & December"),
	December_Thanksgiving(5, "December & Thanksgiving"),
	Both(6,"Both");
	
	private int index;
	private final String english;
	
	MealType(int index, String text)
	{
		this.index = index;
		this.english = text;
	}
	
	MealType type(int type)
	{
		MealType result = MealType.No_Assistance_Rqrd;
		for(MealType mt : MealType.values())
		{
			if(mt.index == type)
			{
				result = mt;
				break;
			}
		}
		
		return result;
	}
	
	static MealType[] getSearchFilterList()
	{
		MealType[] mtSearch = {MealType.Any, MealType.Thanksgiving, MealType.December, 
								MealType.Thanksgiving_December, 
								MealType.December_Thanksgiving, MealType.Both};
		
		return mtSearch;
	}
	
	static MealType[] getSelectionList()
	{
		MealType[] mtSearch = {MealType.Thanksgiving, MealType.December, 
								MealType.Thanksgiving_December,
								MealType.December_Thanksgiving, MealType.Both};
		
		return mtSearch;
	}
	
	static MealType[] getAddMealList()
	{
		MealType[] mtSearch = {MealType.No_Assistance_Rqrd, MealType.Thanksgiving, MealType.December, 
								MealType.Thanksgiving_December,
								MealType.December_Thanksgiving, MealType.Both};
		
		return mtSearch;
	}
	
	
	
	@Override
	public String toString() { return english; }
}
