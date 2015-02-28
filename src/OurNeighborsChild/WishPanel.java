package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	
	private ONCChild child;		//child being displayed on panel
	private ONCChildWish cw; 	//wish being displayed on panel
	private int wishNumber; 	//wish# being displayed on panel
	
	private ChildWishDB cwDB;
	
	private boolean bWishChanging; 		//Semaphore indicating changing ComboBoxes
	private JComboBox wishCB, wishindCB, wishassigneeCB;
	private DefaultComboBoxModel wishCBM, assigneeCBM;
	private JTextField wishdetailTF;
	private JRadioButton wishRB;
	private WishPanelStatus wpStatus;
	
	public WishPanel(JFrame parentFrame)
	{
		super(parentFrame);
		
		cwDB = ChildWishDB.getInstance();
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
		bWishChanging = true;
		
		wishCBM.removeAllElements();	//Clear the combo box selection list

		for(ONCWish w: cat.getWishList(wishNumber, WISH_CATALOG_SELECTION_LISTTYPE))	//Add new list elements
			wishCBM.addElement(w);
			
		//Reselect the proper wish for the currently displayed child
		ONCWish wish = cw == null ? null : cat.getWishByID(cw.getWishID());
			
		if(wish != null) 
			wishCB.setSelectedItem(cat.getWishByID(cw.getWishID()));
		else
			wishCB.setSelectedIndex(0);
	
		bWishChanging = false;
	}

	/*****************************************************************************************************
	 * The combo box model holds organization objects with CONFIRMED status. Each time a CONFIRMED_PARTNER
	 * event occurs, the combo box is updated with a new set of organization objects. The first 
	 * organization object at the top of the box is a non-assigned organization named None
	 ***********************************************************************************************/
	
	void updateWishAssigneeSelectionList()
	{
		ONCOrgs orgDB = ONCOrgs.getInstance();
		bWishChanging = true;
		
		assigneeCBM.removeAllElements();
		assigneeCBM.addElement(new Organization(-1, "None", "None"));
		
		for(Organization confOrg: orgDB.getConfirmedOrgList())
			assigneeCBM.addElement(confOrg);
		
		//Restore selection to prior selection, if they are still confirmed
		if(cw != null  && cw.getChildWishAssigneeID() != -1)
			wishassigneeCB.setSelectedItem(orgDB.getOrganizationByID(cw.getChildWishAssigneeID()));
		else
			wishassigneeCB.setSelectedIndex(0);
		
		bWishChanging = false;
	}
	
	/********************************************************************************************
	 * This method is called to check to see if a child's wish detail has changed.
	 * If a change is detected, a new wish is created and added to the child wish database thru
	 * a call to the addWish method in the child data base. 
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
	
	void showWishHistoryDlg()
	{
		ServerIF serverIF = ServerIF.getInstance();
		Gson gson = new Gson();
		String response = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			HistoryRequest req = new HistoryRequest(child.getID(), wishNumber);
			
			response = serverIF.sendRequest("GET<wishhistory>"+ 
													gson.toJson(req, HistoryRequest.class));
		}
		
		if(response != null)
		{
			ArrayList<ONCChildWish> cwh = new ArrayList<ONCChildWish>();
			Type listtype = new TypeToken<ArrayList<ONCChildWish>>(){}.getType();
			
			cwh = gson.fromJson(response, listtype);
			
			//need to add the assignee name based on the assignee ID for the table
			String[] indicators = {"", "*", "#"};
			ONCOrgs orgDB = ONCOrgs.getInstance();
			
			ArrayList<String[]> wishHistoryTable = new ArrayList<String[]>();
			for(ONCChildWish cw:cwh)
			{
				ONCWish wish = cat.getWishByID(cw.getWishID());
				Organization assignee = orgDB.getOrganizationByID(cw.getChildWishAssigneeID());
				
				String[] whTR = new String[7];
				whTR[0] = wish == null ? "None" : wish.getName();
				whTR[1] = cw.getChildWishDetail();
				whTR[2] = indicators[cw.getChildWishIndicator()];
				whTR[3] = cw.getChildWishStatus().toString();
				whTR[4] = assignee == null ? "None" : assignee.getName();
				whTR[5] = cw.getChildWishChangedBy();
				whTR[6] = new SimpleDateFormat("MM/dd H:mm:ss").format(cw.getChildWishDateChanged().getTime());
				
				wishHistoryTable.add(whTR);
			}
			//need to determine what name to use for child due to user privileges
			String firstname;
			if(gvs.isUserAdmin())
				firstname = child.getChildFirstName();
			else
				firstname = "Child x";
			WishHistoryDialog whDlg = new WishHistoryDialog(GlobalVariables.getFrame(), wishHistoryTable,
											wishNumber, firstname);
			whDlg.setLocationRelativeTo(wishRB);
			whDlg.setVisible(true);
		}
		else
		{
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), 
					"Child Wish History Not Available", 
					"ONC Server Failed to Respond", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(!bWishChanging && e.getSource() == wishCB && 
			((ONCWish)wishCB.getSelectedItem()).getID() != cw.getWishID())
		{
			//user selected a new wish. Check to see if we need to show wish detail dialog
			//Check if a detail dialog is required. It is required if the wish name is found
			//in the catalog (return != null) and the ONC Wish object detail required array list
			//contains data. If required, construct and show the modal dialog. If not required, clear
			//the wish detail text field so the user can create new detail. This prevents inadvertent
			//legacy wish detail from being carried forward with a wish change
			int selectedCBWishID = ((ONCWish) wishCB.getSelectedItem()).getID();
			ArrayList<WishDetail> drDlgData = cat.getWishDetail(selectedCBWishID);
			if(drDlgData != null)
			{
				//Construct and show the wish detail required dialog
				String newWishName = wishCB.getSelectedItem().toString();
				DetailDialog dDlg = new DetailDialog(GlobalVariables.getFrame(), newWishName, drDlgData);
				Point pt = GlobalVariables.getFrame().getLocation();	//Used to set dialog location
				dDlg.setLocation(pt.x + (wishNumber*200) + 20, pt.y + 400);
				dDlg.setVisible(true);
				
				//Retrieve the data and update the wish
				wishdetailTF.setText(dDlg.getDetail());
			}
			else
			{
				wishdetailTF.setText("");	//Clear the text field if base wish changed so user can complete
			}
		

		cwDB.add(this, child.getID(), selectedCBWishID, wishdetailTF.getText(), wishNumber, 
					cw.getChildWishIndicator(), cw.getChildWishStatus(),
					(Organization) wishassigneeCB.getSelectedItem());
		}
		else if(!bWishChanging && e.getSource() == wishindCB && cw.getChildWishIndicator() != 
				wishindCB.getSelectedIndex())
		{
			//Add a new wish with the new indicator
			cwDB.add(this, child.getID(), cw.getWishID(), wishdetailTF.getText(), wishNumber, 
						wishindCB.getSelectedIndex(), cw.getChildWishStatus(),
						(Organization) wishassigneeCB.getSelectedItem());
		}
		else if(!bWishChanging && e.getSource() == wishdetailTF &&
				!cw.getChildWishDetail().equals(wishdetailTF.getText())) 
		{
			//Add a new wish with the new wish detail
			cwDB.add(this, child.getID(), cw.getWishID(), wishdetailTF.getText(), wishNumber, 
						wishindCB.getSelectedIndex(), cw.getChildWishStatus(),
						(Organization) wishassigneeCB.getSelectedItem());
		}
		else if(!bWishChanging && e.getSource() == wishdetailTF &&
				cw.getChildWishAssigneeID() != ((Organization) wishassigneeCB.getSelectedItem()).getID()) 
		{
			//Add a new wish with the new organization
			cwDB.add(this, child.getID(), cw.getWishID(), wishdetailTF.getText(), wishNumber, 
						wishindCB.getSelectedIndex(), cw.getChildWishStatus(),
						(Organization) wishassigneeCB.getSelectedItem());
		}
		else if(e.getSource() == wishRB)
		{ 
			showWishHistoryDlg(); 
		}
		
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
	
	class DetailDialog extends JDialog implements ActionListener
	{
		/*****************************************************************************************
		 * This class implements a dialog used to get additional detail from the 
		 * user regarding gift selection details. It provides customizable 
		 * combo boxes and a detail text field. 
		 * @params - JFrame pf - reference to the parent frame of this dialog
		 * @params - String wishname - name of the wish for which additional detail is obtained
		 * @params - ArrayList<WishDetail> wdAL - Array list containing additional detail objects
		 ******************************************************************************************/
		private static final long serialVersionUID = 1L;
		JComboBox[] cbox;
		String[] titles;	
		JTextField detailTF;
		JButton btnOK;
		
		public DetailDialog(JFrame pf, String wishname, ArrayList<WishDetail> wdAL)
		{
			super(pf, true);
			this.setTitle("Additional " + wishname + " Detail");
			
			//Create the combo boxes
			titles = new String[wdAL.size()];
			cbox = new JComboBox[wdAL.size()];
			
			JPanel infopanel = new JPanel();			
			
			for(int i=0; i<cbox.length; i++)
			{
				titles[i] = wdAL.get(i).getWishDetailName();
				cbox[i] = new JComboBox(wdAL.get(i).getWishDetailChoices());
				cbox[i].setBorder(BorderFactory.createTitledBorder(titles[i]));
				infopanel.add(cbox[i]);
			}
			
			JPanel detailpanel = new JPanel();
			detailTF = new JTextField();
			detailTF.setPreferredSize(new Dimension (320, 50));
			detailTF.setBorder(BorderFactory.createTitledBorder("Additional Details"));
			detailpanel.add(detailTF);
			
			JPanel cntlpanel = new JPanel();
			btnOK = new JButton("Ok");
			btnOK.addActionListener(this);
			cntlpanel.add(btnOK);
			
			 //Add the components to the frame pane
	        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
	        this.add(infopanel);
	        this.add(detailpanel);
	        this.add(cntlpanel);
	        
	        pack();  
		}
		
		String getDetail()
		{
			StringBuffer detail = new StringBuffer(cbox[0].getSelectedItem().toString());
			for(int i=1; i<titles.length; i++)
			{
				if(titles[i].toLowerCase().contains("size"))
					detail.append(", " + "Sz: " + cbox[i].getSelectedItem().toString());
				else if(titles[i].toLowerCase().contains("color"))
				{
					if(!cbox[i].getSelectedItem().toString().equals("Any") &&
						!cbox[i].getSelectedItem().toString().equals("?"))
							detail.append(", " + cbox[i].getSelectedItem().toString());
				}
				else
					detail.append(", " + titles[i] + ": "+ cbox[i].getSelectedItem().toString());
			}
					
			if(detailTF.getText().isEmpty())
				return detail.toString();
			else
				return detail.toString() + ", " + detailTF.getText();
		}
		
		void clearDetail()
		{
			for(int i = 0; i<cbox.length; i++)	//Clear combo boxes
				cbox[i].setSelectedIndex(0);
			
			detailTF.setText("");			
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == btnOK) 
				this.setVisible(false);
		}
	}
}
