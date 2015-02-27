package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class WishPanel extends ONCPanel implements ActionListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_GIFT_ICON = 4;
	private static final int WISH_CATALOG_SELECTION_LISTTYPE = 0;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	//database references
	ONCWishCatalog cat;
	
	private ONCChildWish cw; 			//wish being displayed on panel
	private int wishNumber; 	//wish# being displayed on panel
	
	private boolean bWishDataChanging; 		//Semaphore indicating changing ComboBoxes
	private JComboBox wishCB, wishindCB, wishassigneeCB;
	private DefaultComboBoxModel wishCBM, assigneeCBM;
	private JTextField wishdetailTF;
	private JRadioButton wishRB;
	private WishPanelStatus wpStatus;
	
	public WishPanel(JFrame parentFrame)
	{
		super(parentFrame);
		
		cat = ONCWishCatalog.getInstance();
		
		this.setLayout(new GridLayout(3,1));
		
		JPanel wsp1 = new JPanel();
		wsp1.setLayout(new BoxLayout(wsp1, BoxLayout.LINE_AXIS));
		JPanel wsp2 = new JPanel();
		wsp2.setLayout(new BoxLayout(wsp2, BoxLayout.LINE_AXIS));
		JPanel wsp3 = new JPanel();
		wsp3.setLayout(new BoxLayout(wsp3, BoxLayout.LINE_AXIS));
		
		
		 //Set up the wish combo boxes and detail text fields for child wishes        
        String[] indications = {"", "*", "#"};        
//        String [] status = {"**Unused**", "Empty", "Selected", "Assigned", "Received",
//        					"Distributed", "Verified"};
        
        Dimension dwi = new Dimension(60, 24);     
        Dimension dwa = new Dimension(140, 24);
        
        //Get a catalog for type=selection
        wishCBM = new DefaultComboBoxModel();
        wishCBM.addElement(new ONCWish(-1, "None", 7));
        wishCB = new JComboBox();
        wishCB.setModel(wishCBM);
        wishCB.setPreferredSize(new Dimension(144, 24));
        wishCB.setToolTipText("Select wish from ONC gift catalog");
        wishCB.setEnabled(false);
        wishCB.addActionListener(this);
            
        wishindCB = new JComboBox(indications);
        wishindCB.setPreferredSize(dwi);           
        wishindCB.setToolTipText("Set Wish Restrictions: #- Not from ODB Wish List, *- Don't assign for fulfillment");
        wishindCB.setEnabled(false);
        wishindCB.addActionListener(this);
            
        wishRB = new JRadioButton(gvs.getImageIcon(ONC_GIFT_ICON));
        wishRB.setToolTipText("Click to see wish history");
        wishRB.setEnabled(false);
        wishRB.addActionListener(this);
            
        wishdetailTF = new JTextField();
        wishdetailTF.setToolTipText("Type wish details, then hit <Enter>");
        wishdetailTF.setEnabled(false);
        wishdetailTF.addActionListener(this);
        wishdetailTF.addMouseListener(new WishDetailMouseListener());
 
        assigneeCBM = new DefaultComboBoxModel();
        assigneeCBM.addElement(new Organization(-1, "None", "None"));
        wishassigneeCB = new JComboBox();
        wishassigneeCB.setModel(assigneeCBM);
        wishassigneeCB.setPreferredSize(dwa);
        wishassigneeCB.setToolTipText("Select the organization for wish fulfillment");
        wishassigneeCB.setEnabled(false);
        wishassigneeCB.addActionListener(this);
        
        wsp1.add(wishCB);
    	wsp1.add(wishindCB);
    	wsp2.add(wishdetailTF);
        wsp3.add(wishassigneeCB);
        wsp3.add(wishRB);
        
        this.add(wsp1);
    	this.add(wsp2);
    	this.add(wsp3);
    }
	
	void updateWishSelectionList()
	{
		bWishDataChanging = true;
		
		wishCBM.removeAllElements();	//Clear the combo box selection list

		for(ONCWish w: cat.getWishList(wishNumber, WISH_CATALOG_SELECTION_LISTTYPE))	//Add new list elements
			wishCBM.addElement(w);
			
		//Reselect the proper wish for the currently displayed child
		ONCWish wish = cw == null ? null : cat.getWishByID(cw.getWishID());
			
		if(wish != null) 
			wishCB.setSelectedItem(cat.getWishByID(cw.getWishID()));
		else
			wishCB.setSelectedIndex(0);
	
		bWishDataChanging = false;
	}

	/*****************************************************************************************************
	 * The combo box model holds organization objects with CONFIRMED status. Each time a CONFIRMED_PARTNER
	 * event occurs, the combo box is updated with a new set of organization objects. The first 
	 * organization object at the top of the box is a non-assigned organization named None
	 ***********************************************************************************************/
	
	void updateWishAssigneeSelectionList()
	{
		ONCOrgs orgDB = ONCOrgs.getInstance();
		bWishDataChanging = true;
		
		assigneeCBM.removeAllElements();
		assigneeCBM.addElement(new Organization(-1, "None", "None"));
		
		for(Organization confOrg: orgDB.getConfirmedOrgList())
			assigneeCBM.addElement(confOrg);
		
		//Restore selection to prior selection, if they are still confirmed
		if(cw != null  && cw.getChildWishAssigneeID() != -1)
			wishassigneeCB.setSelectedItem(orgDB.getOrganizationByID(cw.getChildWishAssigneeID()));
		else
			wishassigneeCB.setSelectedIndex(0);
		
		bWishDataChanging = false;
	}
	
	/********************************************************************************************
	 * This method is called to check to see if a child's wish has changed. A change did occur if
	 * the wish as represented on the child panel is different from the wish stored for the child.
	 * If a change is detected, a new wish is created and added to the child wish database thru
	 * a call to the addWish method in the child data base. 
	 * When the new base wish may require a detailed dialog box that prompts the
	 * user for more detail about the wish selected.  
	 * @param wn - which of the child's wishes is to be checked
	 *******************************************************************************************/
/*
	void updateWish(int wn)	//NEED TO HANDLE IF CURRET WISH IS NULL, ADDING THE FIRST WISH TO HISTORY
	{
		ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
		int oldWishID = cw != null ? cw.getWishID() : -1;
		
		//Test to see that the wish has changed and not simply a combo box event without a change
		//to the selected item.
		ONCWish selectedCBWish = (ONCWish) wishCB[wn].getSelectedItem();
		Organization selectedCBOrg = (Organization) wishassigneeCB[wn].getSelectedItem();
		
		if(selectedCBWish.getID() != cw.getWishID() ||
			wishindCB[wn].getSelectedIndex() != cw.getChildWishIndicator() ||
//			 wishstatusCB[wn].getSelectedItem() != cw.getChildWishStatus() ||
			  !wishdetailTF[wn].getText().equals(cw.getChildWishDetail()) ||
			   selectedCBOrg.getID() != (cw.getChildWishAssigneeID()))
		{
			//A change to the wish has occurred, test to see if it's a change to the base
			//If it is a change to the base, additional detail may be required.
			if(selectedCBWish.getID() != cw.getWishID()) 	
			{
				//It was a change to the wish base
				//Check if a detail dialog is required. It is required if the wish name is found
				//in the catalog (return != null) and the ONC Wish object detail required array list
				//contains data. If required, construct and show the modal dialog. If not required, clear
				//the wish detail text field so the user can create new detail. This prevents inadvertent
				//legacy wish detail from being carried forward with a wish change
				ArrayList<WishDetail> drDlgData = cat.getWishDetail(selectedCBWish.getID());
				if(drDlgData != null)
				{
					//Construct and show the wish detail required dialog
					String newWishName = wishCB[wn].getSelectedItem().toString();
					DetailDialog dDlg = new DetailDialog(GlobalVariables.getFrame(), newWishName, drDlgData);
					Point pt = GlobalVariables.getFrame().getLocation();	//Used to set dialog location
					dDlg.setLocation(pt.x + (wn*200) + 20, pt.y + 400);
					dDlg.setVisible(true);
					
					//Retrieve the data and update the wish
					wishdetailTF[wn].setText(dDlg.getDetail());
				}
				else
				{
					wishdetailTF[wn].setText("");	//Clear the text field if base wish changed so user can complete
				}
			}
			
			int orgID;
			if(cw != null && selectedCBOrg.getID() != cw.getChildWishAssigneeID())	
			{		
				//A change to the wish assignee has occurred, set the new child wish
				orgID = selectedCBOrg.getID();
			}
			else	//use the existing assignee with the new wish
			{
				orgID = cw.getChildWishAssigneeID();
			}
			
			//Now that we have assessed/received base,  detail and assignee changes, create a new wish			
			int wishID = cwDB.add(this, c.getID(), selectedCBWish.getID(),
						wishdetailTF[wn].getText(), wn, wishindCB[wn].getSelectedIndex(),
						cw.getChildWishStatus(),
						selectedCBOrg,
						gvs.getUserLNFI(), gvs.getTodaysDate());
			
			//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
			//the status of the wish.
			if(wishID != -1)
			{
				ONCChildWish addedWish = cwDB.getWish(wishID);
				if(addedWish != null)
					displayWish(addedWish, addedWish.getWishNumber());	
			}
			else	//an error occurred, display original wish
			{
				ONCChildWish origWish = cwDB.getWish(c.getChildWishID(wn));
				if(origWish != null)
					displayWish(origWish, origWish.getWishNumber());
				else
				{
					clearChildWish(wn);
				}		
			}
			
			//if child wish selected was a Bike as wish 1, make wish 2 a helmet. If child wish 1 was a bike and
			//and has been changed, make wish 2 empty		
			if(wn==0 && ((ONCWish)wishCB[0].getSelectedItem()).getID() == cat.getWishID(ONC_BIKE_NAME) &&
					(c.getChildWishID(1) == -1 || (c.getChildWishID(1) != -1 &&
						cwDB.getWish(c.getChildWishID(1)).getWishID() != cat.getWishID(ONC_HELMET_NAME))))
			{
				autoAddHelmetAsWish1();
			}
			else if(wn==0 && oldWishID == cat.getWishID(ONC_BIKE_NAME) &&
					((ONCWish)wishCB[0].getSelectedItem()).getID() != cat.getWishID(ONC_BIKE_NAME) && 
					 c.getChildWishID(1) != -1 &&
					  cwDB.getWish(c.getChildWishID(1)).getWishID() == cat.getWishID(ONC_HELMET_NAME))
			{
				//set the combo boxes in the panel
				bChildDataChanging = true;
				
				wishCB[1].setSelectedItem("None");
				wishdetailTF[1].setText("");
				wishindCB[1].setSelectedIndex(0);
//				wishstatusCB[1].setSelectedIndex(CHILD_WISH_STATUS_EMPTY);
				wishassigneeCB[1].setSelectedIndex(0);
				
				//set wish 1 to none	
				wishID = cwDB.add(this, c.getID(), -1, "", 1, 0, WishStatus.Not_Selected, 
									new Organization(-1, "None", "None"), gvs.getUserLNFI(),
									gvs.getTodaysDate());
				
				//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
				//the status of the wish.
				if(wishID != -1)
				{
					ONCChildWish addedWish = cwDB.getWish(wishID);
					if(addedWish != null)
						displayWish(addedWish, addedWish.getWishNumber());	
				}
				else	//an error occurred, display original wish
				{
					ONCChildWish origWish = cwDB.getWish(c.getChildWishID(1));
					if(origWish != null)
						displayWish(origWish, origWish.getWishNumber());
					else
					{
						clearChildWish(1);
					}		
				}
				
				bChildDataChanging = false;
			}
			
		}
	}
*/

	void setEnabledWishPanelComponents(int wn, WishStatus ws)
	{
//		System.out.println(String.format("ChildPanel.setEnabledWishPanelComponenst  wn = %d, ws = %s",
//											wn, ws));
		if(wpStatus == WishPanelStatus.Enabled)
		{
			if(ws == WishStatus.Not_Selected)
			{
				wishCB.setEnabled(true);
				wishindCB.setEnabled(false);
				wishdetailTF.setEnabled(false);
				wishassigneeCB.setEnabled(false);
			}
			else if(ws == WishStatus.Selected || ws == WishStatus.Assigned ||
					ws == WishStatus.Returned)
			{
				wishCB.setEnabled(true);
				wishindCB.setEnabled(true);
				wishdetailTF.setEnabled(true);
				wishassigneeCB.setEnabled(true);
			}
			else if(ws == WishStatus.Delivered || ws == WishStatus.Missing)
			{
				
				wishCB.setEnabled(false);
				wishindCB.setEnabled(false);
				wishdetailTF.setEnabled(false);
				wishassigneeCB.setEnabled(true);
			}
			else
			{
				wishCB.setEnabled(false);
				wishindCB.setEnabled(false);
				wishdetailTF.setEnabled(false);
				wishassigneeCB.setEnabled(false);
			}
		}
		else if(wpStatus == WishPanelStatus.Assignee_Only)
		{
			wishCB.setEnabled(false);
			wishindCB.setEnabled(false);
			wishdetailTF.setEnabled(false);
			wishassigneeCB.setEnabled(true);
		}
		else
		{	
			wishCB.setEnabled(false);
			wishindCB.setEnabled(false);
			wishdetailTF.setEnabled(false);
			wishassigneeCB.setEnabled(false);
		}
		
		wishRB.setEnabled(true);
	}
	
	boolean doesWishFitOnLabel(ONCChildWish cw)
	{
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		ONCWish catWish = cat.getWishByID(cw.getWishID());
		String wishName = catWish == null ? "None" : catWish.getName();
		
		String wish = wishName + " - " + cw.getChildWishDetail();
		//does it fit on one line?
		if(wish != null && wish.length() <= MAX_LABEL_LINE_LENGTH)
			return true;
		else	//split into two lines
		{
			int index = MAX_LABEL_LINE_LENGTH;
			while(index > 0 && wish.charAt(index) != ' ')	//find the line break
				index--;
		
			if(wish.substring(index).length() > MAX_LABEL_LINE_LENGTH)
				return false;
			else
				return true;
		}
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
/*		
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = cDB.getChildren(fam.getID());
			
			//check to see if there are children in the family, is so, display first child
			if(childList != null && !childList.isEmpty())
			{
				//check to see if any of the wishes exist yet, if they do enable wish panels
				if(childList.get(0).getChildWishID(0) != -1 ||
					childList.get(0).getChildWishID(1) != -1 ||
					 childList.get(0).getChildWishID(2) != -1)
				{
					setEnabledWish(fam);
				}
				
				String logEntry = String.format("ChildPanel Event: %s, ONC# %s with %d children",
												tse.getType(), fam.getONCNum(), childList.size());
				LogDialog.add(logEntry, "M");
	
			}
			else
			{
				String logEntry = String.format("ChildPanel Event: %s, ONC#  with %d children",
						tse.getType(), fam.getONCNum(), childList.size());
				LogDialog.add(logEntry, "M");
				clearChildData();
			}
		}
		else if(tse.getType().equals("CHILD_SELECTED"))
		{
			ONCChild child = (ONCChild) tse.getObject2();
			
			if(c!= null)
				updateChild(c);
			
			String logEntry = String.format("ChildPanel Event: %s, Child Selected: %s",
					tse.getType(), child.getChildFirstName());
			LogDialog.add(logEntry, "M");
			displayChild(child);
		}
		else if(tse.getType().equals("WISH_SELECTED"))
		{
			ONCChild child = (ONCChild) tse.getObject2();
			
			if(c != null)
				updateChild(c);
			String logEntry = String.format("ChildPanel Event: %s, Child Selected: %s",
					tse.getType(), child.getChildFirstName());
			LogDialog.add(logEntry, "M");
			displayChild(child);
		}
*/		
	}
	
	private enum WishPanelStatus 
	{
		Enabled,
		Disabled,
		Assignee_Only;
	}
	
	private class WishDetailMouseListener implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent me)
		{
			if(me.getSource() != wishdetailTF)
			{
				//need to get family, child, and child wish objects
				ONCChild child = cDB.getChild(cw.getChildID());
				ONCFamily fam = fDB.getFamily(child.getFamID());
				fireEntitySelected(this, "WISH_SELECTED", fam, child, cw);
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}
	}
}
