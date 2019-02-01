package ourneighborschild;

public enum EntityType
{ 
	AGENT ("Agent"),
	FAMILY ("Family"), 
	CHILD ("Child"),
	GIFT ("Gift"), 
	PARTNER ("Partner"), 
	VOLUNTEER ("Volunteer"), 
	USER ("User"), 
	INVENTORY_ITEM ("Inventory Item"), 
	GROUP ("Group"), 
	ACTIVITY ("Activity"),
	NOTE ("Note");
	
	private final String name;
	
	EntityType(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString() { return name; }
}


