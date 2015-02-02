package OurNeighborsChild;

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
	private static final int CHILD_WISH_STATUS_RECEIVED = 4;
	
	ReceiveGiftsDialog(JFrame pf, String[] columnToolTips, String[] columns, int[] colWidths,
						int[] center_cols, WishStatus dialogType) 
	{
		super(pf, columnToolTips, columns, colWidths, center_cols, dialogType);
	}

	@Override
	boolean doesChildWishStatusMatch(ONCChildWish cw) 
	{
		return cw.getChildWishStatus() < CHILD_WISH_STATUS_RECEIVED;
	}

	@Override
	int getGiftStatusAction() 
	{
		return CHILD_WISH_STATUS_RECEIVED;
	}

	@Override
	boolean changeFamilyStatus() 
	{
		return lastWishChanged.getFamily().getFamilyStatus() == FAMILY_STATUS_GIFTS_RECEIVED;
	}
}
