package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JFrame;

public class SortGiftsDialog extends SortGiftsBaseDialog 												
{
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	SortGiftsDialog(JFrame pf)
	{
		super(pf);
		this.dialogGiftType = GiftType.Child;
		this.setTitle(String.format("Our Neighbor's Child - Child Gift Management"));
		if(giftDB != null)
			giftDB.addDatabaseListener(this);
		
		for(GiftStatus gs : GiftStatus.getSearchFilterList())
			giftStatusFilterCBM.addElement(gs);
		statusCB.addActionListener(this);
		
		for(GiftStatus gs : GiftStatus.getChangeList())
			giftStatusChangeCBM.addElement(gs);
		changeStatusCB.addActionListener(this);
		
		printCBM.addElement("Print Partner Receiving Check Sheets");	//only in this dialog, not cloned gift dialog
		printCB.addActionListener(this);

	    String[] cloneChoices = {"Clone Gifts", "Create Cloned Gifts"};
        cloneCB = new JComboBox<String>(cloneChoices);
        cloneCB.setPreferredSize(new Dimension(136, 28));
        cloneCB.setEnabled(false);
        cloneCB.addActionListener(this);

        cntlPanel.add(cloneCB);
        cntlPanel.add(exportCB);
      	cntlPanel.add(printCB);
      	
      	bottomPanel.add(infoPanel, BorderLayout.LINE_START);
      	bottomPanel.add(cntlPanel, BorderLayout.CENTER);
 
        //add the bottom two panels to the dialog and pack
        this.add(changePanel);
        this.add(bottomPanel);        
	}
	
	@Override
	String[] getGiftNumbers() { return new String[] {"Any","1","2","3"};}
	
	 @Override
	 List<ONCChildGift> getChildGiftList() { return giftDB.getList(); }

	/****
	 * Builds a list of changed gift requests from the highlighted ONCChildGifts in the table. Submits the list
	 * to the local database to be sent to the server. Does some checking based on current gift status to
	 * determine if gift restrictions or gift parters may be changed. The local database determines
	 * if current gift status can be changed.
	 */
	boolean onApplyChanges()
	{
		List<AddGiftRequest> reqAddGiftList = new ArrayList<AddGiftRequest>();

		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			boolean bNewGiftrqrd = false; 	//Restriction, status or assignee change
			
			//get the prior gift
			ONCChildGift priorGift = stAL.get(row_sel[i]).getChildGift();

			//baseline request with prior gift information
			int cgi = priorGift.getIndicator();
			GiftStatus gs = priorGift.getGiftStatus();
			int partnerID = priorGift.getPartnerID();
			
			//Determine if a change to gift restrictions, if so set new gift restriction for request
			if(changeResCB.getSelectedIndex() > 0 && cgi != changeResCB.getSelectedIndex()-1)
			{
				//a change to the indicator is requested. Can only change gift restrictions
				//in certain GiftStatus
				if(gs == GiftStatus.Selected || gs == GiftStatus.Assigned
						||gs == GiftStatus.Shopping || gs == GiftStatus.Returned)
				{
					cgi = changeResCB.getSelectedIndex()-1;	//Restrictions start at 0
					bNewGiftrqrd = true;
				}
			}
			
			//Determine if a change to a partner, if so, set new partner in request
			if(changePartnerCB.getSelectedIndex() > 0 &&
					priorGift.getPartnerID() != ((ONCPartner)changePartnerCB.getSelectedItem()).getID())
			{
				//can only change partners in certain GiftState's
				if(gs == GiftStatus.Selected || gs == GiftStatus.Assigned || gs == GiftStatus.Delivered ||
					gs == GiftStatus.Shopping || gs == GiftStatus.Returned || gs == GiftStatus.Missing)
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
				reqAddGiftList.add(new AddGiftRequest(priorGift, new ONCChildGift(-1, priorGift.getChildID(), priorGift.getCatalogGiftID(), priorGift.getDetail(),
						priorGift.getGiftNumber(), cgi, gs, partnerID, userDB.getUserLNFI(), System.currentTimeMillis())));	
		}
		
		if(!reqAddGiftList.isEmpty())
		{
			String response = giftDB.addGiftList(this, reqAddGiftList);
			if(response != null && response.startsWith("ADDED_GIFT_LIST"))
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
		if(dbe.getSource() != this && dbe.getType().contentEquals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Child Gift Management", gvs.getCurrentSeason()));
			updateUserList();
			updateSchoolFilterList();
			updateDNSCodeCB();
			updateGiftSelectionList();
			updateWishAssigneeSelectionList();
			buildTableList(false);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("WISH_ADDED") ||
									   dbe.getType().equals("UPDATED_CHILD_WISH") ||
										dbe.getType().equals("UPDATED_FAMILY")))	//ONC# or region?	
		{
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_CHILD") || 
				  dbe.getType().equals("DELETED_CHILD")))	//ONC# or region?
		{
			updateSchoolFilterList();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CHILD")))	//ONC# or region?
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
			de.getDateEditor().removePropertyChangeListener(this);
			ds.getDateEditor().removePropertyChangeListener(this);
			
			setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
			ds.setCalendar(startFilterTimestamp);
			de.setCalendar(endFilterTimestamp);
			
			ds.getDateEditor().addPropertyChangeListener(this);
			de.getDateEditor().addPropertyChangeListener(this);
			
			buildTableList(true);
		}
	}	
}