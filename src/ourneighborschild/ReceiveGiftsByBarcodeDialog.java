package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class ReceiveGiftsByBarcodeDialog extends GiftLabelDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JRadioButton rbRegular, rbClone;
	
	ReceiveGiftsByBarcodeDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Receive Gifts");
		
		//set up unique button specifics
		this.btnUndo.setToolTipText("Click to undo last gift receipt");
		this.btnSubmit.setVisible(false);
		
		//set up the regular vs cloned gift mode panel
		JPanel modePanel = new JPanel();
		modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
		
		rbRegular = new JRadioButton("Regular");
		rbRegular.addActionListener(this);
		rbClone = new JRadioButton("Cloned");
		rbClone.addActionListener(this);
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbRegular);
		modeGroup.add(rbClone);
		rbRegular.setSelected(true);
		
		modePanel.add(rbRegular);
		modePanel.add(rbClone);
		modePanel.setBorder(BorderFactory.createTitledBorder("Gift Type"));
		
		topPanel.add(modePanel, BorderLayout.EAST);
		
		//set up the dialog pane
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(topPanel);
		this.getContentPane().add(bottomPanel);
		
		this.setMinimumSize(new Dimension(640, 160));
	}
	
	void onGiftLabelFound(SortGiftObject swo)
	{
		//Check to see if this is a double scan of the gift by comparing the scanned swo with the
		//lastWishChanged swo. If they are the same, don't receive the gift again. If they aren't the same,
		//process change to wish status. Store the new wish to be added in case of an undo operation 
		//and add the new wish to the child wish history. We reuse an ONCSortObject to store the
		//new wish. Organization parameter is null, indicating we're not changing the gift partner
		if(lastGiftChanged != null && swo.getFamily().getID() == lastGiftChanged.getFamily().getID() &&
			swo.getChild().getID() == lastGiftChanged.getChild().getID() && 
			swo.getChildGift().getGiftNumber() == lastGiftChanged.getChildGift().getGiftNumber() &&
			swo.getChildGift().getGiftStatus() == GiftStatus.Received)
		{
			//double scan of the last received gift at this workstation
			alert(Result.SUCCESS, String.format("Gift Just Received: Family # %s, %s",
				swo.getFamily().getONCNum(),swo.getGiftPlusDetail()));
		}
		else if(swo.getChildGift().getGiftStatus() == GiftStatus.Received)
		{
			//double scan of the last received gift at this workstation
			alert(Result.SUCCESS, String.format("Gift Previously Received: Family # %s, %s",
				swo.getFamily().getONCNum(),swo.getGiftPlusDetail()));
		}
		else
		{
			lastGiftChanged = new SortGiftObject(-1, swo.getFamily(),swo.getFamilyHistory(), swo.getChild(), swo.getChildGift());
			
			ONCChildGift addedGift;
			if(swo.getChildGift().isClonedGift())
			{
				List<ONCChildGift> reqAddClonedGiftList = new ArrayList<ONCChildGift>();
				
				ONCChildGift addCloneReq = new ONCChildGift(userDB.getUserLNFI(), swo.getChildGift());
				addCloneReq.setGiftStatus(GiftStatus.Received);
				reqAddClonedGiftList.add(addCloneReq);
				
				String response = clonedGiftDB.addClonedGiftList(this, reqAddClonedGiftList);
				
				if(response != null && response.equals("ADDED_LIST_CLONED_GIFTS"))
				{	
					addedGift = clonedGiftDB.getClonedGift(addCloneReq.getChildID(), addCloneReq.getGiftNumber());
				}
				else
					addedGift = null;
			}
			else
			{
				addedGift = childGiftDB.add(this, swo.getChild().getID(), swo.getChildGift().getCatalogGiftID(),
												swo.getChildGift().getDetail(), swo.getChildGift().getGiftNumber(),
												swo.getChildGift().getIndicator(), GiftStatus.Received, null);
			}
			
			//change color and enable undo operation based on success of receiving gift
			if(addedGift != null)
			{	
				alert(Result.SUCCESS, String.format("Gift Received: Family # %s, %s",
						swo.getFamily().getONCNum(),swo.getGiftPlusDetail()));
				btnUndo.setEnabled(true);
			}
			else
			{
				alert(Result.FAILURE, String.format("Server Gift Received Failure: Family # %s, Gift: %s",
						swo.getFamily().getONCNum(),swo.getGiftPlusDetail()));
				btnUndo.setEnabled(false);
			}
		}
		
		clearBarcodeTF();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//set title
			this.setTitle(String.format("Receive Gifts - %d Season", gvs.getCurrentSeason()));
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
		//To undo the gift action, add the old gift back with the previous status
		if(lastGiftChanged != null && lastGiftChanged.getChildGift().isClonedGift())
		{
			if(lastGiftChanged != null)
			{
				List<ONCChildGift> reqUndoClonedGiftList = new ArrayList<ONCChildGift>();
				reqUndoClonedGiftList.add(lastGiftChanged.getChildGift());
				
				String response = null;
				response = clonedGiftDB.addClonedGiftList(this, reqUndoClonedGiftList);
				
				if(response != null)
				{
					alert(Result.UNDO, String.format("Cloned Gift Unreceived: Family # %s, %s",
	    					lastGiftChanged.getFamily().getONCNum(), lastGiftChanged.getGiftPlusDetail()));
	    			
	    			btnUndo.setEnabled(false);
				}
				else
				{
					alert(Result.FAILURE, String.format("Cloned Gift Undo Failed: Family # %s, %s",
	    					lastGiftChanged.getFamily().getONCNum(), lastGiftChanged.getGiftPlusDetail()));
				}	
			}
		}
		else if(lastGiftChanged != null)
		{	
    		ONCChildGift lastGift = lastGiftChanged.getChildGift();
    				
    		ONCChildGift undoneGift = childGiftDB.add(this, lastGiftChanged.getChild().getID(),
    						lastGift.getCatalogGiftID(), lastGift.getDetail(),
    						lastGift.getGiftNumber(),lastGift.getIndicator(),
    						lastGift.getGiftStatus(), null);	//null to keep same partner
    		
    		if(undoneGift != null)
    		{
    			//check to see if family status should change as well. If the family status had
    			//changed to gifts received with the receive that is being undone, the family 
    			//status must be reset to the prior status.
    			alert(Result.UNDO, String.format("Gift Unreceived: Family # %s, %s",
    					lastGiftChanged.getFamily().getONCNum(), lastGiftChanged.getGiftPlusDetail()));
    			
    			btnUndo.setEnabled(false);
    			
    			if(lastGiftChanged.getFamilyHistory().getGiftStatus() == FamilyGiftStatus.Received)
    				lastGiftChanged.getFamilyHistory().setFamilyStatus(lastGiftChanged.getFamilyHistory().getFamilyStatus());
    		}
    		else
    		{
    			alert(Result.FAILURE, String.format("Gift Undo Failed: Family # %s, %s",
    					lastGiftChanged.getFamily().getONCNum(), lastGiftChanged.getGiftPlusDetail()));
    		}
		}
		
		clearBarcodeTF();
	}
	
	@Override
	void onGiftLabelNotFound()
	{
		alert(Result.FAILURE, "Receive Failed: " + errMessage);
		btnUndo.setEnabled(false);
	}

	@Override
	boolean isGiftEligible(ONCChildGift cg)
	{
		if(cg.isClonedGift())
		{
			return rbClone.isSelected() && cg.getGiftNumber() >= CLONED_GIFT_FIRST_GIFT_NUMBER &&
					(cg.getGiftStatus() == GiftStatus.Delivered || cg.getGiftStatus() == GiftStatus.Received);
		}
		else
		{
			return rbRegular.isSelected() && cg.getGiftNumber() < CLONED_GIFT_FIRST_GIFT_NUMBER &&
				(cg.getGiftStatus() == GiftStatus.Delivered || cg.getGiftStatus() == GiftStatus.Shopping ||
				 cg.getGiftStatus() == GiftStatus.Missing || cg.getGiftStatus() == GiftStatus.Received);
		}
	}
/*	
	@Override
	boolean isGiftEligible(ClonedGift clonedGift)
	{
		return rbClone.isSelected() && clonedGift.getGiftNumber() >= CLONED_GIFT_FIRST_GIFT_NUMBER &&
				(clonedGift.getGiftStatus() == GiftStatus.Delivered || 
				clonedGift.getGiftStatus() == GiftStatus.Received);
	}
*/	
	@Override
	public void onActionPerformed(ActionEvent e)
	{
		if(e.getSource() == rbRegular || e.getSource() == rbClone)
			clearBarcodeTF();	//request focus and highlight bar code entry
	}
}
