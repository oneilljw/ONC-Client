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
	Books,
	ONCShopper;
	
	static GiftCollectionType[] selectionValues()
	{
		GiftCollectionType[] gcSelectionValues = {GiftCollectionType.Unknown, GiftCollectionType.General,
											  GiftCollectionType.Ornament, GiftCollectionType.Meals,
											  GiftCollectionType.Clothing, GiftCollectionType.Coats,
											  GiftCollectionType.Books, GiftCollectionType.ONCShopper};
		
		return gcSelectionValues;
	}
}

