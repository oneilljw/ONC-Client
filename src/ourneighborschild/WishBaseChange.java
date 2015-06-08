package ourneighborschild;

public class WishBaseChange
{
	private ONCChildWish replWish;
	private ONCChildWish addedWish;
	
	WishBaseChange(ONCChildWish replWish, ONCChildWish addedWish)
	{
		this.replWish = replWish;
		this.addedWish = addedWish;
	}
	
	//getters
	ONCChildWish getReplacedWish() { return  replWish; }
	ONCChildWish getAddedWish() { return  addedWish; }
}
