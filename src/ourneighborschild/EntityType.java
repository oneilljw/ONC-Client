package ourneighborschild;

public enum EntityType
{ 
	AGENT ("Agent"),
	FAMILY ("Family"), 
	CHILD ("Child"),
	WISH ("Wish"), 
	PARTNER ("Partner"), 
	VOLUNTEER ("Volunteer"), 
	USER ("User"), 
	INVENTORY_ITEM ("Inventory Item"), 
	GROUP ("Group"), 
	ACTIVITY ("Activity");
	
	private final String name;
	
	EntityType(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString() { return name; }
}


