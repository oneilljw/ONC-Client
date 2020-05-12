package ourneighborschild;

public enum GiftCollectionType
{
	Any, 
	Unknown,
	General,
	Ornament,
	Meals,
	Clothing,
	Coats,
	ONCShopper;
	
	static GiftCollectionType[] selectionValues()
	{
		GiftCollectionType[] gcSelectionValues = {GiftCollectionType.Unknown, GiftCollectionType.General,
											  GiftCollectionType.Ornament, GiftCollectionType.Meals,
											  GiftCollectionType.Clothing, GiftCollectionType.Coats,
											  GiftCollectionType.ONCShopper};
		
		return gcSelectionValues;
	}
}

