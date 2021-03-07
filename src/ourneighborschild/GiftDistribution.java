package ourneighborschild;

public enum GiftDistribution
{
	Any	(0,"Any"),
	None (0, "None"),
	Pickup(1, "Pickup"),
	Delivery (2, "Delivery"),
	Pickup_Delivery (3, "Pickup & Delivery");
	
	private final int index;
	private final String english;
	
	GiftDistribution(int index, String english)
	{
		this.index = index;
		this.english = english;
	}
	
	int index() { return this.index; }
	@Override
	public String toString() { return english; } 
	
	public static GiftDistribution distribution(int i)
	{
		GiftDistribution result = GiftDistribution.None;
		for(GiftDistribution dist : GiftDistribution.values())
		{
			if(dist.index == i)
			{
				result = dist;
				break;
			}
		}
		return result; 
	}
	
	static GiftDistribution[] getPreferenceOptions()
	{
		return new GiftDistribution[] { GiftDistribution.None, GiftDistribution.Pickup,
										GiftDistribution.Delivery, GiftDistribution.Pickup_Delivery};
	}
	
	static GiftDistribution[] getDistributionOptions()
	{
		return new GiftDistribution[] { GiftDistribution.Pickup, GiftDistribution.Delivery};
	}
}
