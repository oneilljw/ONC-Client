package OurNeighborsChild;

public enum WishStatus
{ 
	Unselected ("Unselect"),
	Selected ("Select"),
	Assigned ("Assign"),
	Received ("Receive"),
	Distributed ("Distribute"),
	Verified ("Verify");
	
	private final String presentTense;
	
	WishStatus(String presentTense)
	{
		this.presentTense = presentTense;
	}
	
	String presentTense() { return presentTense; }
}

