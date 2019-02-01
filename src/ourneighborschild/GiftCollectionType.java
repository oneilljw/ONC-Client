package ourneighborschild;

public enum GiftCollectionType
{
	Any, 
	Unknown,
	General,
	Ornament,
	Meals;
	
	static GiftCollectionType[] selectionValues()
	{
		GiftCollectionType[] gcSelectionValues = {GiftCollectionType.Unknown, GiftCollectionType.General,
											  GiftCollectionType.Ornament, GiftCollectionType.Meals};
		
		return gcSelectionValues;
	}
}

