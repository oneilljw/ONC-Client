package ourneighborschild;

public enum WishStatus
{
	Any (0, "Any"),
	No_Change (0, "No_Change"),
	Not_Selected (1, "Unselect"),
	Selected (2,"Select"),
	Assigned (3,"Assign"),
	Delivered (4,"Deliver"),
	Returned (5,"Return"),
	Shopping (6,"Shopping"),
	Received (7,"Receive"),
	Distributed (8,"Distribute"),
	Missing (9,"Missed"),
	Verified (10, "Verify");
	
	private final int statusIndex;
	private final String presentTense;
	
	WishStatus(int statusIndex, String presentTense)
	{
		this.statusIndex = statusIndex;
		this.presentTense = presentTense;
	}
	
	public int statusIndex() { return statusIndex; }
	String presentTense() { return presentTense; }
	
	static WishStatus[] getSearchFilterList()
	{
		WishStatus[] wsSearch = {WishStatus.Any, WishStatus.Not_Selected, WishStatus.Selected,
							WishStatus.Assigned, WishStatus.Delivered, WishStatus.Returned,
							WishStatus.Shopping, WishStatus.Received, WishStatus.Distributed,
							WishStatus.Missing, WishStatus.Verified};
		
		return wsSearch;
	}
	
	static WishStatus[] getChangeList()
	{
		WishStatus[] wsChange = {WishStatus.No_Change, WishStatus.Delivered, WishStatus.Returned,
								WishStatus.Shopping, WishStatus.Received, WishStatus.Distributed,
								WishStatus.Missing, WishStatus.Verified};
		
		return wsChange;
	}
	
}

