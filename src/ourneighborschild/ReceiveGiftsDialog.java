package ourneighborschild;

import javax.swing.JFrame;

public class ReceiveGiftsDialog extends GiftActionDialog 
{
	/**
	 * Allows user to receive gifts. Subclasses GiftActionDialog where most of the
	 * functionality resides. Is implemented as a singleton to prevent multiple
	 * instantiations running concurrently
	 */
	private static final long serialVersionUID = 1L;
	private static final int FAMILY_STATUS_GIFTS_RECEIVED = 3;
	
	ReceiveGiftsDialog(JFrame pf, WishStatus dialogType) 
	{
		super(pf, dialogType);
	}

	@Override
	boolean doesChildWishStatusMatch(ONCChildWish cw) 
	{
		return cw.getChildWishStatus() == WishStatus.Delivered || 
				cw.getChildWishStatus() == WishStatus.Shopping ||
				 cw.getChildWishStatus() == WishStatus.Missing;
	}
	
	/***
	 * When receiving a gift via bar code, it's possible the elf receiving the gift will
	 * accidently scan it twice. The logic will allow that gift to be received twice.
	 * 
	 * return - 0 if gift receipt is valid next state, 1 if gift was already received, -1
	 * if gift receipt is not a valid state in the ONCWishLifeCycle
	 */
	int doesChildWishStatusMatchForBarcode(ONCChildWish cw) 
	{
		if(cw.getChildWishStatus() == WishStatus.Delivered || 
				cw.getChildWishStatus() == WishStatus.Shopping ||
				 cw.getChildWishStatus() == WishStatus.Missing)
			return 0;
		else if(cw.getChildWishStatus() == WishStatus.Received)
			return 1;
		else
			return -1;
	}

	@Override
	WishStatus getGiftStatusAction() 
	{
		return WishStatus.Received;
	}

	@Override
	boolean changeFamilyStatus() 
	{
		return lastWishChanged.getFamily().getFamilyStatus() == FAMILY_STATUS_GIFTS_RECEIVED;
	}
	
	@Override
	String[] getColumnToolTips() 
	{
		 String[] colTT = {"ONC Family Number", "Child Gender", "Child's Age",  
					"Wish Selected for Child", "Wish Detail"};
		return colTT;
	}

	@Override
	String[] getColumnNames()
	{
		String[] columns = {"ONC", "Gend", "Age", "Wish Type", "Details"};
		return columns;
	}

	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {40, 48, 72, 120, 248};
		return colWidths;
	}

	@Override
	int[] getCenteredColumns()
	{
		return null;
	}

	@Override
	void initializeFilters() {
		// TODO Auto-generated method stub
		
	}

	
}
