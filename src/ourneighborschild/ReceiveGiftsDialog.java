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
	
	@Override
	WishStatus getGiftStatusAction() 
	{
		return WishStatus.Received;
	}

	@Override
	boolean changeFamilyStatus() 
	{
		return lastWishChanged.getFamily().getGiftStatus() == FamilyGiftStatus.Received;
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
		int[] colWidths = {40, 48, 72, 120, 344};
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
