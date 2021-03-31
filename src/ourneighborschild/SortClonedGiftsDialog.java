package ourneighborschild;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class SortClonedGiftsDialog extends SortGiftsBaseDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SortClonedGiftsDialog(JFrame pf)
	{
    	super(pf);
    	this.dialogGiftType = GiftType.Cloned;
    	this.setTitle(String.format("Our Neighbor's Child - Cloned Gift Management"));
    	if(clonedGiftDB != null)
			clonedGiftDB.addDatabaseListener(this);
    	
    	for(GiftStatus gs : GiftStatus.getClonedGiftSearchFilterList())
			giftStatusFilterCBM.addElement(gs);
		statusCB.addActionListener(this);
		
		for(GiftStatus gs : GiftStatus.getClonedGiftChangeList())
			giftStatusChangeCBM.addElement(gs);
		changeStatusCB.addActionListener(this);
		
    	printCB.addActionListener(this);
  
        cntlPanel.add(exportCB);
      	cntlPanel.add(printCB);
      	
      	bottomPanel.add(infoPanel, BorderLayout.LINE_START);
      	bottomPanel.add(cntlPanel, BorderLayout.CENTER);
    
        //add the bottom two panels to the dialog and pack
        this.add(changePanel);
        this.add(bottomPanel);        
	}

    @Override
	String[] getGiftNumbers() { return new String[] {"Any","4","5","6"}; }
    
    @Override
    List<ONCChildGift> getChildGiftList() { return clonedGiftDB.getList(); }
	
	/****
	 * Builds a list of changed gift requests from the highlighted ONCChildGifts in the table. Submits the list
	 * to the local database to be sent to the server. Does some checking based on current gift status to
	 * determine if gift restrictions or gift parters may be changed.
	 */
   
	boolean onApplyChanges()
	{
		List<ONCChildGift> reqAddGiftList = new ArrayList<ONCChildGift>();

		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			boolean bNewGiftrqrd = false; 	//status or assignee change
			
			//get the prior gift
			ONCChildGift priorClonedGift = stAL.get(row_sel[i]).getChildGift();

			//baseline request with prior gift information
			int cgi = priorClonedGift.getIndicator();
			GiftStatus gs = priorClonedGift.getGiftStatus();
			int partnerID = priorClonedGift.getPartnerID();
			
			//Determine if a change to gift restrictions, if so set new gift restriction for request
			if(changeResCB.getSelectedIndex() > 0 && cgi != changeResCB.getSelectedIndex()-1)
			{
				//a change to the indicator is requested. Can only change gift restrictions
				//in certain GiftStatus
				if(gs == GiftStatus.Unassigned || gs == GiftStatus.Selected || gs == GiftStatus.Assigned
						||gs == GiftStatus.Shopping || gs == GiftStatus.Returned)
				{
					cgi = changeResCB.getSelectedIndex()-1;	//Restrictions start at 0
					bNewGiftrqrd = true;
				}
			}
			
			//Determine if a change to a partner, if so, set new partner in request
			if(changePartnerCB.getSelectedIndex() > 0 &&
					priorClonedGift.getPartnerID() != ((ONCPartner)changePartnerCB.getSelectedItem()).getID())
			{
				//can only change clone partners in certain GiftState's
				if(gs == GiftStatus.Unassigned|| gs == GiftStatus.Assigned)
				{
					partnerID = ((ONCPartner)changePartnerCB.getSelectedItem()).getID();	//new partner ID
					bNewGiftrqrd = true;
				}
			}
			
			//Determine if a change to gift status, if so, set new status in request
			if(changeStatusCB.getSelectedIndex() > 0 && gs != changeStatusCB.getSelectedItem())
			{
				//user has requested to change the status
				GiftStatus reqStatus = (GiftStatus) changeStatusCB.getSelectedItem();
				gs = reqStatus;
				bNewGiftrqrd = true;
			}
			
			//if restriction, Status or Partner change, create gift request that includes the prior gift
			//and it's replacement.
			if(bNewGiftrqrd)
			{
				//create a copy and set the new parameters
				ONCChildGift addCloneGiftReq = new ONCChildGift(userDB.getUserLNFI(), priorClonedGift);
				addCloneGiftReq.setIndicator(cgi);
				addCloneGiftReq.setPartnerID(partnerID);
				addCloneGiftReq.setGiftStatus(gs);
				reqAddGiftList.add(addCloneGiftReq);
			}	
		}
		
		if(!reqAddGiftList.isEmpty())
		{
			String response = clonedGiftDB.addClonedGiftList(this, reqAddGiftList);
			if(response != null && response.startsWith("ADDED_LIST_CLONED_GIFTS"))
				buildTableList(false);
		}
		else
			buildTableList(true);
		
		//Reset the change combo boxes to "No Change"
		changeResCB.setSelectedIndex(0);
		changeStatusCB.setSelectedIndex(0);
		changePartnerCB.setSelectedIndex(0);
		
		btnApplyChanges.setEnabled(false);

		return false;
	}	

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Cloned Gift Management", gvs.getCurrentSeason()));
			updateSchoolFilterList();
			updateUserList();
			updateWishAssigneeSelectionList();
			updateDNSCodeCB();
			updateGiftSelectionList();
			updateDateFilters();
			buildTableList(false);
		}
		if(dbe.getType().equals("ADDED_LIST_CLONED_GIFTS"))	
		{
			buildTableList(true);
		}
		if(dbe.getType().equals("UPDATED_FAMILY"))	//ONC# or region?	
		{
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_CHILD") || 
				  dbe.getType().equals("DELETED_CHILD")))	
		{
			updateSchoolFilterList();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CHILD")))
		{
			updateSchoolFilterList();
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("DELETED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("UPDATED_CONFIRMED_PARTNER")))
		{
			updateWishAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			updateWishAssigneeSelectionList();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CATALOG_WISH") ||
				dbe.getType().equals("UPDATED_CATALOG_WISH") ||
				dbe.getType().equals("DELETED_CATALOG_WISH")))
		{			
			updateGiftSelectionList();
		}
		else if(dbe.getType().contains("ADDED_USER") || dbe.getType().contains("UPDATED_USER"))
		{
			updateUserList();
			
			//check to see if the current user was updated to update preferences
			ONCUser updatedUser = (ONCUser)dbe.getObject1();
 			if(userDB.getLoggedInUser().getID() == updatedUser.getID())
				updateUserPreferences(updatedUser);
		}
		else if(dbe.getType().contains("CHANGED_USER"))
		{
			//new user logged in, update preferences used by this dialog
			updateUserPreferences((ONCUser) dbe.getObject1());
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject1();
			updateRegionList(regList);
		}
		else if(dbe.getType().contains("ADDED_DNSCODE"))
		{
			updateDNSCodeCB();
		}
		else if(dbe.getType().contains("UPDATED_DNSCODE"))
		{
			updateDNSCodeCB();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			updateDateFilters();
			buildTableList(true);
		}
	}
}