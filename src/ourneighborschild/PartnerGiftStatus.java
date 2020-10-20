package ourneighborschild;

public enum PartnerGiftStatus
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
	
	PartnerGiftStatus(int statusIndex, String presentTense)
	{
		this.statusIndex = statusIndex;
		this.presentTense = presentTense;
	}
	
	public int statusIndex() { return statusIndex; }
	String presentTense() { return presentTense; }
	
	static PartnerGiftStatus[] getSearchFilterList()
	{
		PartnerGiftStatus[] wsSearch = {PartnerGiftStatus.Any, PartnerGiftStatus.Unassigned,
										PartnerGiftStatus.Assigned, PartnerGiftStatus.Delivered,
										PartnerGiftStatus.Returned, PartnerGiftStatus.Received};
		
		return wsSearch;
	}
	
	static PartnerGiftStatus[] getChangeList()
	{
		PartnerGiftStatus[] wsChange = {PartnerGiftStatus.No_Change, PartnerGiftStatus.Delivered, 
										PartnerGiftStatus.Returned, PartnerGiftStatus.Received};
		
		return wsChange;
	}
	
	static boolean valid(PartnerGiftStatus pgs)
	{
		return pgs.compareTo(PartnerGiftStatus.Unassigned) >=0 && 
				pgs.compareTo(PartnerGiftStatus.Received) <= 0 ;
	}
	
	static PartnerGiftStatus getPartnerGiftStatus(int statusIndex)
	{
		PartnerGiftStatus[] pgsArray = PartnerGiftStatus.values();
		if(statusIndex < 2 && statusIndex > 5)
			return PartnerGiftStatus.Unassigned;
		else
			return pgsArray[statusIndex];
		
	}
}
