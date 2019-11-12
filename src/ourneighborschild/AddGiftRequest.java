package ourneighborschild;

public class AddGiftRequest
{
	private ONCChildGift priorGift;
	private ONCChildGift newGift;
	
	AddGiftRequest(ONCChildGift priorGift, ONCChildGift newGift)
	{
		this.priorGift = priorGift;
		this.newGift = newGift;
	}
	
	//getters
	ONCChildGift getPriorGift() { return priorGift; }
	ONCChildGift getNewGift() { return newGift; }
}
