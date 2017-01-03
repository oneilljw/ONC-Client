package ourneighborschild;

public enum FamilyStatus 
{
	Any (0, "Any"),
	No_Change (0, "No Change"),
	Unverified (0, "Unverified"),
	InfoVerified (1,"Info Verified"),
	GiftsSelected (2,"Gifts Selected"),
	GiftsReceived (3, "Gifts Received"),
	GiftsVerified (4, "Gifts Verified"),
	Packaged(5,"Packaged");

	private final int statusIndex;
	private final String english;
	
	FamilyStatus(int statusIndex, String english)
	{
		this.statusIndex = statusIndex;
		this.english = english;
	}
	
	public String toString() { return this.english; }
	
	public static FamilyStatus getFamilyStatus(int statusIndex)
	{
		FamilyStatus result = FamilyStatus.Unverified;
		for(FamilyStatus fs : FamilyStatus.getSearchList())
			if(fs.statusIndex == statusIndex)
			{
				result = fs;
				break;
			}
		
			return result;
	}
	public int statusIndex() { return statusIndex; }
	String english() { return english; }
	
	static FamilyStatus[] getSearchList()
	{
		FamilyStatus[] fgsSearch = {FamilyStatus.Unverified, FamilyStatus.InfoVerified,
									FamilyStatus.GiftsSelected, FamilyStatus.GiftsReceived, 
									FamilyStatus.GiftsVerified, FamilyStatus.Packaged};
		
		return fgsSearch;
	}
	
	static FamilyStatus[] getSearchFilterList()
	{
		FamilyStatus[] fgsSearch = {FamilyStatus.Any, FamilyStatus.Unverified, FamilyStatus.InfoVerified,
									FamilyStatus.GiftsSelected, FamilyStatus.GiftsReceived, 
									FamilyStatus.GiftsVerified, FamilyStatus.Packaged};
		
		return fgsSearch;
	}
	
	static FamilyStatus[] getChangeList()
	{
		FamilyStatus[] fgsSearch = {FamilyStatus.No_Change, FamilyStatus.Unverified, FamilyStatus.InfoVerified,
									FamilyStatus.GiftsSelected, FamilyStatus.GiftsReceived, 
									FamilyStatus.GiftsVerified, FamilyStatus.Packaged};
		
		return fgsSearch;
	}
}
