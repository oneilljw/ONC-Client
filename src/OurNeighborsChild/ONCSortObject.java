package OurNeighborsChild;

public abstract class ONCSortObject extends ONCObject
{
	public ONCSortObject(int itemID)
	{
		super(itemID);
	}
	
	abstract Object getTableCell(int col);
	
	abstract public Class<?> getColumnClass(int col);
	
	abstract public String[] getExportRow();
}