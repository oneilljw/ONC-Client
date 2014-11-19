package OurNeighborsChild;

public class WishBaseOrOrgChange
{
	private String olditem;
	private String newitem;
	private int wishnum;
	
	WishBaseOrOrgChange(String olditem, String newitem, int wishnum)
	{
		this.olditem = olditem;
		this.newitem = newitem;
		this.wishnum = wishnum;
	}
	
	//getters
	String getOldItem() { return  olditem; }
	String getNewItem() { return  newitem; }
	int getWishNum() { return wishnum; }
}
