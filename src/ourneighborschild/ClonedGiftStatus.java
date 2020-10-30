package ourneighborschild;

public enum ClonedGiftStatus
{
	Any (0, "Any"),
	No_Change (0, "No_Change"),
	Unassigned (1, "Unassign"),
	Assigned (2,"Assign"),
	Delivered (3,"Deliver"),
	Returned (4,"Return"),
	Received (5,"Receive");
	
	private final int statusIndex;
	private final String presentTense;
	
	ClonedGiftStatus(int statusIndex, String presentTense)
	{
		this.statusIndex = statusIndex;
		this.presentTense = presentTense;
	}
	
	public int statusIndex() { return statusIndex; }
	String presentTense() { return presentTense; }
	
	static ClonedGiftStatus[] getSearchFilterList()
	{
		ClonedGiftStatus[] wsSearch = {ClonedGiftStatus.Any, ClonedGiftStatus.Unassigned,
										ClonedGiftStatus.Assigned, ClonedGiftStatus.Delivered,
										ClonedGiftStatus.Returned, ClonedGiftStatus.Received};
		
		return wsSearch;
	}
	
	static ClonedGiftStatus[] getChangeList()
	{
		ClonedGiftStatus[] wsChange = {ClonedGiftStatus.No_Change, ClonedGiftStatus.Delivered, 
										ClonedGiftStatus.Returned, ClonedGiftStatus.Received};
		
		return wsChange;
	}
	
	static boolean valid(ClonedGiftStatus pgs)
	{
		return pgs.compareTo(ClonedGiftStatus.Unassigned) >=0 && 
				pgs.compareTo(ClonedGiftStatus.Received) <= 0 ;
	}
	
	static ClonedGiftStatus getPartnerGiftStatus(int statusIndex)
	{
		ClonedGiftStatus[] pgsArray = ClonedGiftStatus.values();
		if(statusIndex < 2 && statusIndex > 5)
			return ClonedGiftStatus.Unassigned;
		else
			return pgsArray[statusIndex];
		
	}
}
