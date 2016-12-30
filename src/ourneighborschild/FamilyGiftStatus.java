package ourneighborschild;

public enum FamilyGiftStatus 
{
	Any (0, "Any"),
	No_Change (0, "No Change"),
	Requested (0, "Requested"),
	Contacted (1,"Contacted"),
	Confirmed (2,"Confirmed"),
	Assigned (3, "Assigned"),
	Attempted (4,"Attempted"),
	Returned (5,"Returned"),
	Delivered (6,"Delivered"),
	Counselor_Pickup (7,"Counselor Pick-Up"),
	Not_Requested (8,"Not Requested");
	
	private final int statusIndex;
	private final String english;
	
	FamilyGiftStatus(int statusIndex, String english)
	{
		this.statusIndex = statusIndex;
		this.english = english;
	}
	
	public String toString() { return this.english; }
	
	static FamilyGiftStatus getFamilyGiftStatus(int statusIndex)
	{
		FamilyGiftStatus result = FamilyGiftStatus.Requested;
		for(FamilyGiftStatus fgs : FamilyGiftStatus.getSearchList())
			if(fgs.statusIndex == statusIndex)
			{
				result = fgs;
				break;
			}
		
			return result;
	}
	public int statusIndex() { return statusIndex; }
	String english() { return english; }
	
	static FamilyGiftStatus[] getSearchList()
	{
		FamilyGiftStatus[] fgsSearch = {FamilyGiftStatus.Requested, FamilyGiftStatus.Contacted,
										FamilyGiftStatus.Confirmed, FamilyGiftStatus.Assigned, 
										FamilyGiftStatus.Attempted, FamilyGiftStatus.Returned, 
										FamilyGiftStatus.Delivered, FamilyGiftStatus.Counselor_Pickup,
										FamilyGiftStatus.Not_Requested};
		
		return fgsSearch;
	}
	
	static FamilyGiftStatus[] getSearchFilterList()
	{
		FamilyGiftStatus[] fgsSearch = {FamilyGiftStatus.Any, FamilyGiftStatus.Not_Requested,
										FamilyGiftStatus.Requested, FamilyGiftStatus.Contacted,
										FamilyGiftStatus.Confirmed, FamilyGiftStatus.Assigned, 
										FamilyGiftStatus.Attempted, FamilyGiftStatus.Returned, 
										FamilyGiftStatus.Delivered, FamilyGiftStatus.Counselor_Pickup};
		
		return fgsSearch;
	}
	
	static FamilyGiftStatus[] getChangeList()
	{
		FamilyGiftStatus[] fgsChange = {FamilyGiftStatus.No_Change, FamilyGiftStatus.Not_Requested, FamilyGiftStatus.Requested,
										FamilyGiftStatus.Contacted, FamilyGiftStatus.Confirmed, 
										FamilyGiftStatus.Attempted, FamilyGiftStatus.Returned, 
										FamilyGiftStatus.Delivered, FamilyGiftStatus.Counselor_Pickup};
		
		return fgsChange;
	}	
}