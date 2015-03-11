package OurNeighborsChild;

public abstract class ONCSortObject extends ONCObject
{
	public ONCSortObject(int itemID)
	{
		super(itemID);
	}
	
	abstract public String[] getExportRow();
}