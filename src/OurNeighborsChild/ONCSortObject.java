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
	
	//determine if two ONCSortObjects match
	@Override
	public boolean matches(ONCObject otherObj)
	{
		if(otherObj != null && otherObj.getClass() == ONCSortObject.class)
		{
			ONCSortObject otherSO = (ONCSortObject) otherObj;
			
//			System.out.println(String.format("ONCSortObject.matches: SO.cwID = %d, otherSO.cwID = %d",
//					soChildWish.getID(), otherSO.soChildWish.getID()));
			
			return otherSO.soFamily != null && otherSO.soFamily.getID() == soFamily.getID() &&
					otherSO.soChild != null && otherSO.soChild.getID() == soChild.getID() &&
					otherSO.soChildWish != null && otherSO.soChildWish.getID() == soChildWish.getID();			
		}
		else
			return false;
	}

	@Override
	public String[] getExportRow() {
		//Not used for Gift Actions
		return null;
	}	
}