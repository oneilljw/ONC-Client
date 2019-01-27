package ourneighborschild;

public enum GiftStatus
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
	
	GiftStatus(int statusIndex, String presentTense)
	{
		this.statusIndex = statusIndex;
		this.presentTense = presentTense;
	}
	
	public int statusIndex() { return statusIndex; }
	String presentTense() { return presentTense; }
	
	static GiftStatus[] getSearchFilterList()
	{
		GiftStatus[] wsSearch = {GiftStatus.Any, GiftStatus.Not_Selected, GiftStatus.Selected,
							GiftStatus.Assigned, GiftStatus.Delivered, GiftStatus.Returned,
							GiftStatus.Shopping, GiftStatus.Received, GiftStatus.Distributed,
							GiftStatus.Missing, GiftStatus.Verified};
		
		return wsSearch;
	}
	
	static GiftStatus[] getChangeList()
	{
		GiftStatus[] wsChange = {GiftStatus.No_Change, GiftStatus.Delivered, GiftStatus.Returned,
								GiftStatus.Shopping, GiftStatus.Received, GiftStatus.Distributed,
								GiftStatus.Missing, GiftStatus.Verified};
		
		return wsChange;
	}
	
}

