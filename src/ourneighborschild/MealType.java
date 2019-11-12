package ourneighborschild;

public enum MealType
{
	Any("Any"),
	No_Assistance_Rqrd("No Assistance Req."),
	Thanksgiving("Thanksgiving"),
	December("December"),
	Thanksgiving_December("Thanksgiving & December"),
	December_Thanksgiving("December & Thanksgiving"),
	Both("Both");
	
	private final String text;
	
	MealType(String text)
	{
		this.text = text;
	}
	
	public String text() {  return text; }
	
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
	public String toString() { return text; }
}
