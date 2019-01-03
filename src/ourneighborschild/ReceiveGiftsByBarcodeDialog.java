package ourneighborschild;

import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class ReceiveGiftsByBarcodeDialog extends GiftLabelDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ReceiveGiftsByBarcodeDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Receive Gifts");
		
		//set up unique button specifics
		this.btnUndo.setToolTipText("Click to undo last battery ");
		this.btnSubmit.setVisible(false);
		
		//set up the dialog pane
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(topPanel);
		this.getContentPane().add(bottomPanel);
		
		clearBarcodeTF();	//request focus and highlight bar code entry
		
		pack();
	}
	
	void onGiftLabelFound(SortWishObject swo)
	{
		//Check to see if this is a double scan of the gift by comparing the scanned swo with the
		//lastWishChanged swo. If they are the same, don't receive the gift again. If they aren't the same,
		//process change to wish status. Store the new wish to be added in case of an undo operation 
		//and add the new wish to the child wish history. We reuse an ONCSortObject to store the
		//new wish. Organization parameter is null, indicating we're not changing the gift partner
		if(lastWishChanged != null &&
			swo.getFamily().getID() == lastWishChanged.getFamily().getID() &&
			swo.getChild().getID() == lastWishChanged.getChild().getID() && 
			swo.getChildWish().getWishNumber() == lastWishChanged.getChildWish().getWishNumber() &&
			swo.getChildWish().getChildWishStatus() == WishStatus.Received)
		{
			//double scan of the last received gift at this workstation
			alert(Result.SUCCESS, String.format("Gift Already Received: Family # %s, %s",
				swo.getFamily().getONCNum(),swo.getWishPlusDetail()));
		}
		else
		{
			lastWishChanged = new SortWishObject(-1, swo.getFamily(), swo.getChild(), swo.getChildWish());
			
			ONCChildWish addedWish = childWishDB.add(this, swo.getChild().getID(), swo.getChildWish().getWishID(),
												swo.getChildWish().getChildWishDetail(),
												swo.getChildWish().getWishNumber(),
												swo.getChildWish().getChildWishIndicator(), 
												WishStatus.Received, null);
			
			//change color and enable undo operation based on success of receiving gift
			if(addedWish != null)
			{	
				alert(Result.SUCCESS, String.format("Gift Received: Family # %s, %s",
						swo.getFamily().getONCNum(),swo.getWishPlusDetail()));
				btnUndo.setEnabled(true);
			}
			else
			{
				alert(Result.FAILURE, String.format("Server Gift Received Failure: Family # %s, Gift: %s",
						swo.getFamily().getONCNum(),swo.getWishPlusDetail()));
				btnUndo.setEnabled(false);
			}
		}
		
		clearBarcodeTF();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("LOADED_WISHES"))
		{
			//get the initial data and set title
			this.setTitle(String.format("Receive Gifts - %d Season", GlobalVariablesDB.getCurrentSeason()));
		}
	}

	@Override
	void onClearOtherPanels()
	{
		//no other panels to clear in this dialog
	}

	@Override
	void onSubmit()
	{
		// Submit button is not used in this dialog
	}

	@Override
	void onUndoSubmittal()
	{
		//To undo the wish, add the old wish back with the previous status		
		ONCChildWish lastWish = lastWishChanged.getChildWish();
				
		ONCChildWish undoneWish = childWishDB.add(this, lastWishChanged.getChild().getID(),
						lastWish.getWishID(), lastWish.getChildWishDetail(),
						lastWish.getWishNumber(),lastWish.getChildWishIndicator(),
						lastWish.getChildWishStatus(), null);	//null to keep same partner
		
		if(undoneWish != null)
		{
			//check to see if family status should change as well. If the family status had
			//changed to gifts received with the receive that is being undone, the family 
			//status must be reset to the prior status.
			alert(Result.UNDO, String.format("Gift Unreceived: Family # %s, %s",
					lastWishChanged.getFamily().getONCNum(), lastWishChanged.getWishPlusDetail()));
			
			btnUndo.setEnabled(false);
			
			if(lastWishChanged.getFamily().getGiftStatus() == FamilyGiftStatus.Received)
				lastWishChanged.getFamily().setFamilyStatus(lastWishChanged.getFamily().getFamilyStatus());
		}
		else
		{
			alert(Result.FAILURE, String.format("Gift Undo Failed: Family # %s, %s",
					lastWishChanged.getFamily().getONCNum(), lastWishChanged.getWishPlusDetail()));
		}
	}
	
	@Override
	void onGiftLabelNotFound()
	{
		alert(Result.FAILURE, "Receive Failed: " + errMessage);
		btnUndo.setEnabled(false);
	}

	@Override
	void onActionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	boolean isGiftEligible(ONCChildWish cw)
	{
		return cw.getChildWishStatus() == WishStatus.Delivered || 
				cw.getChildWishStatus() == WishStatus.Shopping ||
				 cw.getChildWishStatus() == WishStatus.Missing ||
				  cw.getChildWishStatus() == WishStatus.Received;
	}
}
