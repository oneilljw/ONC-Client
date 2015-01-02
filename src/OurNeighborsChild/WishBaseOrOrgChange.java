package OurNeighborsChild;

public class WishBaseOrOrgChange
{
	private ONCObject oldObject;
	private ONCObject newObject;
	private int value;
	
	WishBaseOrOrgChange(ONCObject oldObject, ONCObject newObject, int value)
	{
		this.oldObject = oldObject;
		this.newObject = newObject;
		this.value = value;
	}
	
	//getters
	ONCObject getOldObject() { return  oldObject; }
	ONCObject getNewObject() { return  newObject; }
	int getValue() { return value; }
}
