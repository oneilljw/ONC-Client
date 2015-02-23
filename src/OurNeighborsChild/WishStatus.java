package OurNeighborsChild;

public enum WishStatus
{
	No_Change (0, "No_Change"),
	Not_Selected (1, "Unselect"),
	Selected (2,"Select"),
	Assigned (3,"Assign"),
	Delivered (4,"Deliver"),
	Returned (5,"Return"),
	Shopping (6,"Shopping"),
	Received (7,"Receive"),
	Distributed (8,"Distribute"),
	Verified (9, "Verify");
	
	private final int statusIndex;
	private final String presentTense;
	
	WishStatus(int statusIndex, String presentTense)
	{
		this.statusIndex = statusIndex;
		this.presentTense = presentTense;
	}
	
	public int statusIndex() { return statusIndex; }
	String presentTense() { return presentTense; }
}

