package ourneighborschild;

public enum GiftCollection
{
	Any, 
	Unknown,
	General,
	Ornament,
	Meals;
	
	static GiftCollection[] selectionValues()
	{
		GiftCollection[] gcSelectionValues = {GiftCollection.Unknown, GiftCollection.General,
											  GiftCollection.Ornament, GiftCollection.Meals};
		
		return gcSelectionValues;
	}
}

