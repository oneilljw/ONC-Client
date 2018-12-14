package ourneighborschild;

public class Battery extends ONCObject
{
	private int childID;
	private int wishnum;
	private String size;
	private int quantity;
	
	public Battery(int id, int childID, int wishnum, String size, int quantity)
	{
		super(id);
		this.childID = childID;
		this.wishnum = wishnum;
		this.size = size;
		this.quantity = quantity;
	}
	
	public Battery(String[] line)
	{
		super(Integer.parseInt(line[0]));
		this.childID = line[1].isEmpty() ? -1 : Integer.parseInt(line[1]);
		this.wishnum = line[2].isEmpty() ? -1 : Integer.parseInt(line[2]);
		this.size = line[3].isEmpty() ? "" : line[3];
		this.quantity = line[4].isEmpty() ? 0 : Integer.parseInt(line[4]);
	}
	
	//copy constructor
	public Battery(Battery b)
	{
		super(b.id);
		this.childID = b.childID;
		this.wishnum = b.wishnum;
		this.size = b.size;
		this.quantity = b.quantity;
	}
	
	int getChildID() { return childID; }
	int getWishNum() { return wishnum; }
	String getSize() { return size; }
	int getQuantity() { return quantity; }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[5];
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(childID);
		row[2] = Integer.toString(wishnum);
		row[3] = size;
		row[4] = Integer.toString(quantity);
		return row;
	}
}
