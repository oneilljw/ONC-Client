package OurNeighborsChild;

public class ONCSortObject extends ONCObject
{
	protected ONCFamily	 soFamily;
	protected ONCChild	 soChild;
	protected ONCChildWish soChildWish;
	
	public ONCSortObject(int itemID, ONCFamily fam, ONCChild c, ONCChildWish cw)
	{
		super(itemID);
		soFamily = fam;
		soChild = c;
		soChildWish = cw;
	}
	
	//getters
	ONCFamily getFamily() { return soFamily; }
	ONCChild getChild() { return soChild; }
	ONCChildWish getChildWish() { return soChildWish; }

	@Override
	public String[] getExportRow() {
		//Not used for Gift Actions
		return null;
	}
}