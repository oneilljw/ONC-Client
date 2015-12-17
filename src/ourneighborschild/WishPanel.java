package ourneighborschild;

import java.awt.Color;
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

public class WishPanel extends ONCPanel implements ActionListener, DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_GIFT_ICON = 4;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	private static final int FAMILY_STATUS_UNVERIFIED = 0;
	private static final String GIFT_CARD_ONLY_TEXT = "gift card only";
	
	//database references
	ONCWishCatalog cat;
	ONCOrgs orgDB;
	
	private ONCChild child;		//child being displayed on panel
	private ONCChildWish childWish; 	//wish being displayed on panel
	private int wishNumber; 	//wish# being displayed on panel
	
	private ChildWishDB cwDB;
	
	private boolean bWishChanging; 		//Semaphore indicating changing ComboBoxes
	private JComboBox wishCB, wishindCB, wishassigneeCB;
	private DefaultComboBoxModel wishCBM, assigneeCBM;
	private JTextField wishdetailTF;
	private JRadioButton wishRB;
	private WishPanelStatus wpStatus;
	
	public WishPanel(JFrame parentFrame, int wishNumber)
	{
		super(parentFrame);
		this.wishNumber = wishNumber;
		
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		if(cDB != null)
			cDB.addDatabaseListener(this);
		
		cwDB = ChildWishDB.getInstance();
		if(cwDB != null)
			cwDB.addDatabaseListener(this);
		
		cat = ONCWishCatalog.getInstance();
		if(cat != null)
			cat.addDatabaseListener(this);
		
		orgDB = ONCOrgs.getInstance();
		if(orgDB != null)
			orgDB.addDatabaseListener(this);
		
		this.setLayout(new GridLayout(3,1));
		
		JPanel wsp1 = new JPanel();
		wsp1.setLayout(new BoxLayout(wsp1, BoxLayout.LINE_AXIS));
		JPanel wsp2 = new JPanel();
		wsp2.setLayout(new BoxLayout(wsp2, BoxLayout.LINE_AXIS));
		JPanel wsp3 = new JPanel();
		wsp3.setLayout(new BoxLayout(wsp3, BoxLayout.LINE_AXIS));
		
		 //Set up the wish combo boxes and detail text fields for child wishes        
        String[] indications = {"", "*", "#"};        

        this.setBorder(BorderFactory.createTitledBorder("Wish " + Integer.toString(wishNumber+1)));
        
        Dimension dwa = new Dimension(140, 24);
//        Dimension dwi = new Dimension(44, 24);     
          
        //Get a catalog for type=selection
        wishCBM = new DefaultComboBoxModel();
        wishCBM.addElement(new ONCWish(-1, "None", 7));
        wishCB = new JComboBox();
        wishCB.setModel(wishCBM);
//        wishCB.setPreferredSize(new Dimension(144, 24));
        wishCB.setToolTipText("Select wish from ONC gift catalog");
        wishCB.setEnabled(false);
        wishCB.addActionListener(this);
            
        wishindCB = new JComboBox(indications);
//        wishindCB.setPreferredSize(dwi);           
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
	
	/********************************************************************************************
	 * This method is called to check to see if a child's wish detail has changed.
	 * If a change is detected, call updateWish to update the current wish in the data base
	 * This method is called when the current wish is changing so the user does not lose
	 * changes to the detail they have made prior to the new wish being displayed
	 *******************************************************************************************/
	void checkForUpdateToWishDetail()
	{
		if(childWish != null && !childWish.getChildWishDetail().equals(wishdetailTF.getText()))
			addWish();
	}
	
	void displayWish(ONCChildWish cw, ONCChild c)
	{
		bWishChanging = true;
		
		child = c;
		childWish = cw;
		
		this.setBorder(BorderFactory.createTitledBorder("Wish " + Integer.toString(wishNumber+1) +
					": " + cw.getChildWishStatus().toString()));
			
		ONCWish wish = cat.getWishByID(cw.getWishID());
		if(wish != null)
			wishCB.setSelectedItem(wish);
		else
			wishCB.setSelectedIndex(0);
			
		String logEntry = String.format("Wish Panel.displayWish: Wish# %d: %s, Status: %s",
					wishNumber, wishCB.getSelectedItem().toString(), cw.getChildWishStatus().toString());
		LogDialog.add(logEntry, "M");
			
		wishindCB.setSelectedIndex(cw.getChildWishIndicator());
			
		wishdetailTF.setText(cw.getChildWishDetail());
		wishdetailTF.setCaretPosition(0);
		if(doesWishFitOnLabel(cw))
			wishdetailTF.setBackground(Color.WHITE);
		else
			wishdetailTF.setBackground(Color.YELLOW);

		Organization org = orgDB.getOrganizationByID(cw.getChildWishAssigneeID());
		if(org != null)
			wishassigneeCB.setSelectedItem(org);
		else
			wishassigneeCB.setSelectedIndex(0);
			
		setEnabledWishPanelComponents(cw.getChildWishStatus());

		bWishChanging = false;
	}
	
	void clearWish()
	{
		bWishChanging = true;
		
		childWish = null;
		
		this.setBorder(BorderFactory.createTitledBorder("Wish " + Integer.toString(wishNumber+1)));
		wishCB.setSelectedIndex(0);
		wishdetailTF.setText("");
		wishindCB.setSelectedIndex(0);
		wishassigneeCB.setSelectedIndex(0);
		
		bWishChanging = false;
	}
	
	void updateWishSelectionList()
	{
		bWishChanging = true;
		
		wishCBM.removeAllElements();	//Clear the combo box selection list

		for(ONCWish w: cat.getWishList(wishNumber, WishListPurpose.Selection))	//Add new list elements
			wishCBM.addElement(w);
			
		//Reselect the proper wish for the currently displayed child
		ONCWish wish = childWish == null ? null : cat.getWishByID(childWish.getWishID());
			
		if(wish != null) 
			wishCB.setSelectedItem(cat.getWishByID(childWish.getWishID()));
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
		
		for(Organization confOrg: orgDB.getConfirmedOrgList(GiftCollection.Ornament))
			assigneeCBM.addElement(confOrg);
		
		//Restore selection to prior selection, if they are still confirmed
		if(childWish != null  && childWish.getChildWishAssigneeID() != -1)
			wishassigneeCB.setSelectedItem(orgDB.getOrganizationByID(childWish.getChildWishAssigneeID()));
		else
			wishassigneeCB.setSelectedIndex(0);
		
		bWishChanging = false;
	}	

	void setEnabledWish(ONCFamily fam)
	{
//		System.out.println(String.format("ChildPanel.setEnabledChildWishes ONC# %s", fam.getONCNum()));
		//only enable wish panels if family has been verified
		if(fam.getFamilyStatus() == FAMILY_STATUS_UNVERIFIED)	
			wpStatus = WishPanelStatus.Disabled;
		else 
		{
			if(fam.getNotes().toLowerCase().contains(GIFT_CARD_ONLY_TEXT))
				wpStatus = WishPanelStatus.Assignee_Only;
			else
				wpStatus = WishPanelStatus.Enabled;
		}
		
		//now that we've update the panel status, update the component status
		if(childWish != null)
			setEnabledWishPanelComponents(childWish.getChildWishStatus());
		else
			setEnabledWishPanelComponents(WishStatus.Not_Selected);
	}

	void setEnabledWishPanelComponents(WishStatus ws)
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
		else if(wpStatus == WishPanelStatus.Assignee_Only && 
				(ws == WishStatus.Selected || ws == WishStatus.Assigned || ws == WishStatus.Returned ||
				 ws == WishStatus.Delivered || ws == WishStatus.Missing))
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
		
		String[] indicator = {"", "*", "#"};
		
		String wish = indicator[cw.getChildWishIndicator()] +
				wishName + " - " + cw.getChildWishDetail();
		
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
		String response = null, errTitle = "ONC Server Failed to Respond";
		
		if(serverIF != null && serverIF.isConnected())
		{	
			if(childWish != null)
			{
				HistoryRequest req = new HistoryRequest(childWish.getChildID(), wishNumber);
				response = serverIF.sendRequest("GET<wishhistory>"+ 
													gson.toJson(req, HistoryRequest.class));
			}
			else
				errTitle = "No History Yet";
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
			if(GlobalVariables.isUserAdmin())
				firstname = child.getChildFirstName();
			else
				firstname = "Child " + cDB.getChildNumber(child);
			WishHistoryDialog whDlg = new WishHistoryDialog(GlobalVariables.getFrame(), wishHistoryTable,
											wishNumber, firstname);
			whDlg.setLocationRelativeTo(wishRB);
			whDlg.setVisible(true);
		}
		else
		{
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), 
					"Child Wish History Not Available", 
					errTitle, JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(!bWishChanging && e.getSource() == wishCB && (childWish == null || childWish!= null &&
			((ONCWish)wishCB.getSelectedItem()).getID() != childWish.getWishID()))
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
		
			addWish();
		}
		else if(!bWishChanging && e.getSource() == wishindCB && childWish.getChildWishIndicator() != 
				wishindCB.getSelectedIndex())
		{
			//add a new wish with the new indicator
			addWish();
		}
		else if(!bWishChanging && e.getSource() == wishdetailTF &&
				!childWish.getChildWishDetail().equals(wishdetailTF.getText())) 
		{
			//Add a new wish with new wish detail
			addWish();
		}
		else if(!bWishChanging && e.getSource() == wishassigneeCB &&
				childWish.getChildWishAssigneeID() != ((Organization) wishassigneeCB.getSelectedItem()).getID()) 
		{
			//Add a new wish with the new organization
			addWish();
		}
		else if(e.getSource() == wishRB)
		{ 
			showWishHistoryDlg(); 
		}	
	}
	
	void addWish()
	{
		WishStatus ws = childWish != null ? childWish.getChildWishStatus() : WishStatus.Not_Selected;
		ONCChildWish addedWish =  cwDB.add(this, child.getID(),
									((ONCWish) wishCB.getSelectedItem()).getID(),
									wishdetailTF.getText(), wishNumber, wishindCB.getSelectedIndex(),
									ws, (Organization) wishassigneeCB.getSelectedItem());
		
		if(addedWish != null)
			displayWish(addedWish, child);
		else
			displayWish(childWish, child);
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("WISH_ADDED"))
		{	
			//Get the added wish to extract the child
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject();
			
//			System.out.println(String.format("WishPanel %d DB Event: Type %s, addWishID: %d, addWish# %d, addWishchildID %d, childID: %d",
//					wishNumber, dbe.getType(), addedWish.getWishID(), addedWish.getWishNumber(), addedWish.getChildID(), child.getID()));

			//If the added wish would be displayed by ths wish panel and the added wish belongs
			//to the child this panel is currently displaying, display the added wish
			if(addedWish.getWishNumber() == wishNumber && child != null &&
				child.getID() == addedWish.getChildID())
			{
				String logEntry = String.format("WishPanel Event: %s, Child: %s, wish %d",
						dbe.getType(), child.getChildFirstName(), addedWish.getWishNumber());
				LogDialog.add(logEntry, "M");
				
				displayWish(addedWish, child);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildWish updatedWish = (ONCChildWish) dbe.getObject();
			
			if(updatedWish.getWishNumber() == wishNumber && updatedWish.getID() == childWish.getID())
			{
				String logEntry = String.format("WishPanel Event: %s, Child: %s, wish %d",
						dbe.getType(), child.getChildFirstName(), updatedWish.getWishNumber());
				LogDialog.add(logEntry, "M");
				
				displayWish(updatedWish, child);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFam = (ONCFamily) dbe.getObject();
//			System.out.println(String.format("WishPanel Event: %s, ONC# %s, status= %d",
//					dbe.getType(), updatedFam.getONCNum(), updatedFam.getFamilyStatus()));
			
			if(child != null)
//				System.out.println(String.format("WishPanel Event: %s, childFamily ID:= %d, updatedFamilyID= %d",
//						dbe.getType(), child.getFamID(), updatedFam.getID()));
			
			if(child != null && updatedFam.getID() == child.getFamID())
			{
				//current child displayed is in family, check for wish panel status change
				String logEntry = String.format("WishPanel Event: %s, ONC# %s, status = %d",
						dbe.getType(), updatedFam.getONCNum(), updatedFam.getFamilyStatus());
				LogDialog.add(logEntry, "M");
				setEnabledWish(updatedFam);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			ONCChild delChild = (ONCChild) dbe.getObject();
			
			if(delChild != null && delChild.getID()  == child.getID())
			{
				//current childwish displayed belongs to deleted child, clear the panel
				String logEntry = String.format("WishPanel Event: %s, Child: %s %s",
						dbe.getType(), delChild.getChildFirstName(), delChild.getChildLastName());
				LogDialog.add(logEntry, "M");
				clearWish();
				wpStatus = WishPanelStatus.Disabled;
			}
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CONFIRMED_PARTNER") ||
				dbe.getType().equals("DELETED_CONFIRMED_PARTNER")) ||
				dbe.getType().equals("UPDATED_CONFIRMED_PARTNER") ||
				dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME") ||
				dbe.getType().equals("LOADED_PARTNERS"))
		{
			String logEntry = String.format("WishPanel Event: %s", dbe.getType());
			LogDialog.add(logEntry, "M");
			updateWishAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG"))
		{
			String logEntry = String.format("WishPanel Event: %s", dbe.getType());
			LogDialog.add(logEntry, "M");
			updateWishSelectionList();
		}
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{	
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = cDB.getChildren(fam.getID());
			
			checkForUpdateToWishDetail();
		
			setEnabledWish(fam);
			
			//check to see if there are children in the family, is so, display first child
			if(childList != null && !childList.isEmpty() &&
					childList.get(0).getChildWishID(wishNumber) > -1)
			{
				ONCChildWish cw = cwDB.getWish(childList.get(0).getID(), wishNumber);
				if(cw != null)
					displayWish(cw, childList.get(0));
				else
					clearWish();
			}
			else
			{
				if(childList != null && !childList.isEmpty())
					child = childList.get(0);
				else
					child = null;
				
				clearWish();
			}
		}
		else if(tse.getType().equals("CHILD_SELECTED") || tse.getType().equals("WISH_SELECTED")  )
		{
			ONCChild selChild = (ONCChild) tse.getObject2();
			
			checkForUpdateToWishDetail();
			
			if(selChild.getChildWishID(wishNumber) > -1)
				displayWish(cwDB.getWish(selChild.getChildWishID(wishNumber)), selChild);
			else
			{
				child = selChild;
				clearWish();
			}
		}
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
			if(me.getSource() == wishdetailTF)
			{
				//need to get family, child, and child wish objects
				ONCFamily fam = fDB.getFamily(child.getFamID());
				fireEntitySelected(this, "WISH_SELECTED", fam, child, childWish);
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
