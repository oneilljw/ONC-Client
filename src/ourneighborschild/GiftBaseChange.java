package ourneighborschild;

public class GiftBaseChange
{
	private ONCChildGift replGift;
	private ONCChildGift addedGift;
	
	GiftBaseChange(ONCChildGift replGift, ONCChildGift addedGift)
	{
		this.replGift = replGift;
		this.addedGift = addedGift;
	}
	
	//getters
	ONCChildGift getReplacedGift() { return  replGift; }
	ONCChildGift getAddedGift() { return  addedGift; }
}
