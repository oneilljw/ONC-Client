package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.border.TitledBorder;

public class GiftPanel extends JPanel implements ActionListener, DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_GIFT_ICON = 4;
	private static final int ONC_LABEL_ICON = 44;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	//database references
	private GlobalVariablesDB gvs;
    private FamilyDB fDB;
	private ChildDB cDB;
	private ChildGiftDB cwDB;
	private GiftCatalogDB cat;
	private PartnerDB partnerDB;
	private UserDB userDB;
	
	private ONCChild child;			//child being displayed on panel
	private ONCChildGift childGift; //wish being displayed on panel
	private int giftNumber; 		//wish# being displayed on panel
	
	private boolean bGiftChanging; 		//semaphore indicating changing ComboBoxes
	private TitledBorder border;
	private JComboBox<ONCGift> giftCB;
	private JComboBox<String> giftindCB;
	private JComboBox<ONCPartner> partnerCB;
	private DefaultComboBoxModel<ONCGift> giftCBM;
	private DefaultComboBoxModel<ONCPartner> partnerCBM;
	private JTextField giftdetailTF;
	private JRadioButton rbGift, rbLabel;
	private GiftPanelStatus gpStatus;
	
	public GiftPanel(int wishNumber)
	{
		this.giftNumber = wishNumber;
		this.setTransferHandler(new InventoryTransferhandler());
		
		gvs = GlobalVariablesDB.getInstance();
    		fDB = FamilyDB.getInstance();
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		cDB = ChildDB.getInstance();
		if(cDB != null)
			cDB.addDatabaseListener(this);
		
		cwDB = ChildGiftDB.getInstance();
		if(cwDB != null)
			cwDB.addDatabaseListener(this);
		
		cat = GiftCatalogDB.getInstance();
		if(cat != null)
			cat.addDatabaseListener(this);
		
		partnerDB = PartnerDB.getInstance();
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		this.setLayout(new GridLayout(3,1));
		
		JPanel wsp1 = new JPanel();
		wsp1.setLayout(new BoxLayout(wsp1, BoxLayout.LINE_AXIS));
		JPanel wsp2 = new JPanel();
		wsp2.setLayout(new BoxLayout(wsp2, BoxLayout.LINE_AXIS));
		JPanel wsp3 = new JPanel();
		wsp3.setLayout(new BoxLayout(wsp3, BoxLayout.LINE_AXIS));
		
		 //Set up the gift combo boxes and detail text fields for child gifts     
        String[] indications = {"", "*", "#"};        

        border = BorderFactory.createTitledBorder(String.format("Gift %d", wishNumber+1));
        this.setBorder(border);
        
        Dimension dwa = new Dimension(140, 24);  
          
        //Get a catalog for type=selection
        giftCBM = new DefaultComboBoxModel<ONCGift>();
        giftCBM.addElement(new ONCGift(-1, "None", 7));
        giftCB = new JComboBox<ONCGift>();
        giftCB.setModel(giftCBM);
        giftCB.setPreferredSize(new Dimension(177, 24));
        giftCB.setToolTipText("Select wish from ONC gift catalog");
        giftCB.setEnabled(false);
        giftCB.addActionListener(this);
            
        giftindCB = new JComboBox<String>(indications);
        giftindCB.setPreferredSize(new Dimension(52, 24));           
        giftindCB.setToolTipText("Set Wish Restrictions: #- Not from Child Wish List, *- Don't assign to partner");
        giftindCB.setEnabled(false);
        giftindCB.addActionListener(this);
            
        rbGift = new JRadioButton(gvs.getImageIcon(ONC_GIFT_ICON));
        rbGift.setToolTipText("Click to see wish history");
        rbGift.setEnabled(false);
        rbGift.addActionListener(this);
        
        rbLabel = new JRadioButton(gvs.getImageIcon(ONC_LABEL_ICON));
        rbLabel.setToolTipText("Click to see wish label");
        rbLabel.setEnabled(false);
        rbLabel.addActionListener(this);
            
        giftdetailTF = new JTextField();
        giftdetailTF.setToolTipText("Type wish details, then hit <Enter>");
        giftdetailTF.setEnabled(false);
        giftdetailTF.addActionListener(this);
 
        partnerCBM = new DefaultComboBoxModel<ONCPartner>();
        partnerCBM.addElement(new ONCPartner(-1, "None", "None"));
        partnerCB = new JComboBox<ONCPartner>();
        partnerCB.setModel(partnerCBM);
        partnerCB.setPreferredSize(dwa);
        partnerCB.setToolTipText("Select the organization for wish fulfillment");
        partnerCB.setEditable(true);
        partnerCB.setEnabled(false);
        partnerCB.addActionListener(this);
        
        wsp1.add(giftCB);
    		wsp1.add(giftindCB);
    		wsp1.add(rbGift);
    		wsp2.add(giftdetailTF);
        wsp3.add(partnerCB);
        wsp3.add(rbLabel);
        
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
		if(childGift != null && !childGift.getDetail().equals(giftdetailTF.getText()))
			addGift();
	}
	
	void display(ONCChildGift cg, ONCChild c)
	{
		bGiftChanging = true;
		
		this.child = c;
		this.childGift = cg;
		
		border.setTitle(String.format("Gift %d: %s", giftNumber+1, cg.getGiftStatus()));
		this.repaint();
			
		ONCGift wish = cat.getGiftByID(cg.getGiftID());
		if(wish != null)
			giftCB.setSelectedItem(wish);
		else
			giftCB.setSelectedIndex(0);
		giftCB.setToolTipText("Child Gift ID " + Integer.toString(cg.getID()));
		
		giftindCB.setSelectedIndex(cg.getIndicator());
			
		giftdetailTF.setText(cg.getDetail());
		giftdetailTF.setCaretPosition(0);
		if(doesGiftFitOnLabel(cg))
			giftdetailTF.setBackground(Color.WHITE);
		else
			giftdetailTF.setBackground(Color.YELLOW);

		if(cg.getPartnerID() == -1)
		{
			//wish does not have a partner assigned
			partnerCB.setSelectedIndex(0);
		}
		else
		{
			ONCPartner giftPartner = partnerDB.getPartnerByID(cg.getPartnerID());		
			if(giftPartner != null)
			{	
//				System.out.println("WishPanel %d.Display partner= " + wishPartner);
//				int orgCBIndex = assigneeCBM.getIndexOf(wishPartner);
//				debugAssigneeCBContents();
//				System.out.println("WishPanel " + wishNumber + " Display Model Index for " + wishPartner + " = " + orgCBIndex);
				partnerCB.setSelectedItem(giftPartner);
//				System.out.println("WishPanel " + wishNumber + " Display Selected Index= " + wishassigneeCB.getSelectedIndex());
			}
			else
			{
				partnerCB.setSelectedIndex(0);
				System.out.println(String.format("WishPanel %d.Display partner was null", giftNumber));
			}
		}
			
		setEnabledGiftPanelComponents(cg.getGiftStatus());

		bGiftChanging = false;
	}
	
	void clearGift()
	{
		bGiftChanging = true;
		
		childGift = null;
		
		border.setTitle(String.format("Gift %d", giftNumber+1));
		this.repaint();
		
		giftCB.setSelectedIndex(0);
		giftdetailTF.setText("");
		giftindCB.setSelectedIndex(0);
		partnerCB.setSelectedIndex(0);
		
		setEnabledGiftPanelComponents(GiftStatus.Not_Selected);
		
		bGiftChanging = false;
	}
	
	void updateGiftSelectionList()
	{
		bGiftChanging = true;
		
		giftCBM.removeAllElements();	//Clear the combo box selection list

		for(ONCGift w: cat.getGiftList(giftNumber, GiftListPurpose.Selection))	//Add new list elements
			giftCBM.addElement(w);
			
		//Reselect the proper gift for the currently displayed child
		ONCGift wish = childGift == null ? null : cat.getGiftByID(childGift.getGiftID());
			
		if(wish != null) 
			giftCB.setSelectedItem(cat.getGiftByID(childGift.getGiftID()));
		else
			giftCB.setSelectedIndex(0);
	
		bGiftChanging = false;
	}

	/*****************************************************************************************************
	 * The combo box model holds organization objects with CONFIRMED status. Each time a CONFIRMED_PARTNER
	 * event occurs, the combo box is updated with a new set of organization objects. The first 
	 * organization object at the top of the box is a non-assigned organization named None
	 ***********************************************************************************************/
	void updateGiftPartnerSelectionList()
	{
		PartnerDB partnerDB = PartnerDB.getInstance();
		bGiftChanging = true;
		
		partnerCBM.removeAllElements();
		partnerCBM.addElement(new ONCPartner(-1, "None", "None"));
		
		for(ONCPartner confirmedPartner: partnerDB.getConfirmedPartnerList(GiftCollectionType.Ornament))
			partnerCBM.addElement(confirmedPartner);
		
		//Restore selection to prior selection, if they are still confirmed
		if(childGift != null  && childGift.getPartnerID() != -1)
			partnerCB.setSelectedItem(partnerDB.getPartnerByID(childGift.getPartnerID()));
		else
			partnerCB.setSelectedIndex(0);
		
		bGiftChanging = false;
		
//		System.out.println(String.format("WishPanel.updateWishAssigneeSelectionList: Assignee Contents"));
//		debugAssigneeCBContents();
	}	

	void setEnabledGift(ONCFamily fam)
	{
		//only enable gift panels if family has been verified and gifts have been requested
		if(fam.getFamilyStatus() == FamilyStatus.Unverified || fam.getGiftStatus() == FamilyGiftStatus.NotRequested)	
			gpStatus = GiftPanelStatus.Disabled;
		else 
			gpStatus = GiftPanelStatus.Enabled;
		
		//now that we've updated the panel status, update the component status
		if(childGift != null)
			setEnabledGiftPanelComponents(childGift.getGiftStatus());
		else
			setEnabledGiftPanelComponents(GiftStatus.Not_Selected);
	}

	void setEnabledGiftPanelComponents(GiftStatus ws)
	{
		if(gpStatus == GiftPanelStatus.Enabled)
		{
			if(ws == GiftStatus.Not_Selected)
			{
				giftCB.setEnabled(true);
				giftindCB.setEnabled(false);
				giftdetailTF.setEnabled(true);
				partnerCB.setEnabled(false);
			}
			else if(ws == GiftStatus.Selected || ws == GiftStatus.Assigned ||
					ws == GiftStatus.Returned)
			{
				giftCB.setEnabled(true);
				giftindCB.setEnabled(true);
				giftdetailTF.setEnabled(true);
				partnerCB.setEnabled(true);
			}
			else if(ws == GiftStatus.Delivered || ws == GiftStatus.Shopping || 
					ws == GiftStatus.Missing)
			{
				giftCB.setEnabled(false);
				giftindCB.setEnabled(false);
				giftdetailTF.setEnabled(false);
				partnerCB.setEnabled(true);
			}
			else
			{
				giftCB.setEnabled(false);
				giftindCB.setEnabled(false);
				giftdetailTF.setEnabled(false);
				partnerCB.setEnabled(false);
			}
		}
		else
		{	
			giftCB.setEnabled(false);
			giftindCB.setEnabled(false);
			giftdetailTF.setEnabled(false);
			partnerCB.setEnabled(false);
		}
		
		rbGift.setEnabled(true);
		rbLabel.setEnabled(ws != GiftStatus.Not_Selected);
	}
	
	boolean doesGiftFitOnLabel(ONCChildGift cw)
	{
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		ONCGift catWish = cat.getGiftByID(cw.getGiftID());
		String wishName = catWish == null ? "None" : catWish.getName();
		
//		String[] indicator = {"", "*", "#"};
		
//		String wish = indicator[cw.getChildWishIndicator()] + wishName + "- " + cw.getChildWishDetail();
		String wish = wishName + "- " + cw.getDetail();
		
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
	
	void showHistoryDlg()
	{
		List<ONCChildGift> cwhList = null;
		
		if(childGift != null &&
			!(cwhList = cwDB.getGiftHistory(childGift.getChildID(), giftNumber)).isEmpty())
		{
			//need to add the assignee name based on the assignee ID for the table
			String[] indicators = {"", "*", "#"};
			PartnerDB orgDB = PartnerDB.getInstance();
			
			ArrayList<String[]> wishHistoryTable = new ArrayList<String[]>();
			for(ONCChildGift cw:cwhList)
			{
				ONCGift wish = cat.getGiftByID(cw.getGiftID());
				ONCPartner assignee = orgDB.getPartnerByID(cw.getPartnerID());
				
				String[] whTR = new String[7];
				whTR[0] = wish == null ? "None" : wish.getName();
				whTR[1] = cw.getDetail();
				whTR[2] = indicators[cw.getIndicator()];
				whTR[3] = cw.getGiftStatus().toString();
				whTR[4] = assignee == null ? "None" : assignee.getLastName();
				whTR[5] = cw.getChangedBy();
				whTR[6] = new SimpleDateFormat("MM/dd H:mm:ss").format(cw.getDateChanged().getTime());
				
				wishHistoryTable.add(whTR);
			}
			//need to determine what name to use for child due to user privileges
			String firstname;
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
				firstname = child.getChildFirstName();
			else
				firstname = "Child " + cDB.getChildNumber(child);
			GiftHistoryDialog whDlg = new GiftHistoryDialog(GlobalVariablesDB.getFrame(), wishHistoryTable,
											giftNumber, firstname);
			whDlg.setLocationRelativeTo(rbGift);
			whDlg.setVisible(true);
		}
		else
		{
			JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), 
					"Child Wish History Not Available", 
					"No History Available", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(!bGiftChanging && e.getSource() == giftCB && (childGift == null || childGift!= null &&
			((ONCGift)giftCB.getSelectedItem()).getID() != childGift.getGiftID()))
		{
			//user selected a new gift. Check to see if we need to show gift detail dialog
			//Check if a detail dialog is required. It is required if the gift name is found
			//in the catalog (return != null) and the ONCGift object detail required array list
			//contains data. If required, construct and show the modal dialog. If not required, clear
			//the detail text field so the user can create new detail. This prevents inadvertent
			//legacy detail from being carried forward with a gift change
			int selectedCBWishID = ((ONCGift) giftCB.getSelectedItem()).getID();
			ArrayList<GiftDetail> drDlgData = cat.getGiftDetail(selectedCBWishID);
			if(drDlgData != null)
			{
				//Construct and show the gift detail required dialog
				String newWishName = giftCB.getSelectedItem().toString();
				DetailDialog dDlg = new DetailDialog(GlobalVariablesDB.getFrame(), newWishName, drDlgData);
				Point pt = GlobalVariablesDB.getFrame().getLocation();	//Used to set dialog location
				dDlg.setLocation(pt.x + (giftNumber*200) + 20, pt.y + 400);
				dDlg.setVisible(true);
				
				//Retrieve the data and update the gift
				giftdetailTF.setText(dDlg.getDetail());
			}
			else
			{
				giftdetailTF.setText("");	//Clear the text field if base gift changed so user can complete
			}
		
			addGift();
		}
		else if(!bGiftChanging && e.getSource() == giftindCB && childGift.getIndicator() != 
				giftindCB.getSelectedIndex())
		{
			//add a new gift with the new indicator
			addGift();
		}
		else if(!bGiftChanging && e.getSource() == giftdetailTF && !giftdetailTF.getText().isEmpty() &&
				(childGift == null || childGift != null && !childGift.getDetail().equals(giftdetailTF.getText()))) 
		{
			//Add a new gift with new detail. If the current gift id = -1 (None) and the indicator
			//selected index is 0, then set the giftID combo box to the default gift as well, prior to adding
			//the gift
			ONCGift cbWish = (ONCGift) giftCB.getSelectedItem();
			if(cbWish.getID() == -1 && giftindCB.getSelectedIndex() == 0 && gvs.getDefaultGiftID() > -1 )
			{
				giftCB.removeActionListener(this);
				ONCGift defaultWish = (ONCGift) cat.getGiftByID(gvs.getDefaultGiftID());
				giftCB.setSelectedItem(defaultWish);
				giftCB.addActionListener(this);
			}
			addGift();
		}
		else if(!bGiftChanging && e.getSource() == partnerCB &&
				childGift.getPartnerID() != ((ONCPartner) partnerCB.getSelectedItem()).getID()) 
		{
			//Add a new gift with the new partner
			addGift();
		}
		else if(e.getSource() == rbGift)
		{ 
			showHistoryDlg(); 
		}
		else if(e.getSource() == rbLabel)
		{
			//construct and show label viewer for selected label
			OrnamentLabelViewer viewer = new OrnamentLabelViewer(GlobalVariablesDB.getFrame(), child, giftNumber);
			viewer.setLocationRelativeTo(this);
			viewer.setVisible(true);
		}
	}
	
	void addGift()
	{
		GiftStatus gs = childGift != null ? childGift.getGiftStatus() : GiftStatus.Not_Selected;
		ONCChildGift addedGift =  cwDB.add(this, child.getID(),
									((ONCGift) giftCB.getSelectedItem()).getID(),
									giftdetailTF.getText(), giftNumber, giftindCB.getSelectedIndex(),
									gs, (ONCPartner) partnerCB.getSelectedItem());
		
		if(addedGift != null)
			display(addedGift, child);
		else
			display(childGift, child);
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("WISH_ADDED"))
		{	
			//Get the added wish to extract the child
			ONCChildGift addedWish = (ONCChildGift) dbe.getObject1();
			
//			System.out.println(String.format("WishPanel %d DB Event: Type %s, addWishID: %d, addWish# %d, addWishchildID %d, childID: %d, partnerID: %d",
//					wishNumber, dbe.getType(), addedWish.getWishID(), addedWish.getWishNumber(), addedWish.getChildID(), child.getID(), addedWish.getChildWishAssigneeID()));

			//If the added wish would be displayed by this wish panel and the added wish belongs
			//to the child this panel is currently displaying, display the added wish
			if(addedWish.getGiftNumber() == giftNumber && child != null &&
				child.getID() == addedWish.getChildID())
			{
				display(addedWish, child);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildGift updatedWish = (ONCChildGift) dbe.getObject1();
			
			if(updatedWish.getGiftNumber() == giftNumber && updatedWish.getID() == childGift.getID())
			{
				String logEntry = String.format("WishPanel Event: %s, Child: %s, wish %d",
						dbe.getType(), child.getChildFirstName(), updatedWish.getGiftNumber());
				LogDialog.add(logEntry, "M");
				
				display(updatedWish, child);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFam = (ONCFamily) dbe.getObject1();
			if(child != null && updatedFam.getID() == child.getFamID())
			{
				//current child displayed is in family, check for wish panel status change
				setEnabledGift(updatedFam);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			ONCChild delChild = (ONCChild) dbe.getObject1();
			
			if(delChild != null && delChild.getID()  == child.getID())
			{
				//current child wish displayed belongs to deleted child, clear the panel
				clearGift();
				gpStatus = GiftPanelStatus.Disabled;
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
			updateGiftPartnerSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG"))
		{
			String logEntry = String.format("WishPanel Event: %s", dbe.getType());
			LogDialog.add(logEntry, "M");
			updateGiftSelectionList();
		}
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = cDB.getChildren(fam.getID());
			
			checkForUpdateToWishDetail();
			
			//check to see if there are children in the family, is so, display first child
			if(childList != null && !childList.isEmpty() &&
					childList.get(0).getChildGiftID(giftNumber) > -1)
			{
				ONCChildGift cw = cwDB.getGift(childList.get(0).getID(), giftNumber);
				if(cw != null)
					display(cw, childList.get(0));
				else
					clearGift();
			}
			else
			{
				if(childList != null && !childList.isEmpty())
					child = childList.get(0);
				else
					child = null;
				
				clearGift();
			}
			
			setEnabledGift(fam);	//lock or unlock the panel and/or panel components
		}
		else if(tse.getType() == EntityType.CHILD || tse.getType() == EntityType.GIFT)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ONCChild selChild = (ONCChild) tse.getObject2();
			
			checkForUpdateToWishDetail();
			
			if(selChild.getChildGiftID(giftNumber) > -1)
				display(cwDB.getGift(selChild.getChildGiftID(giftNumber)), selChild);
			else
			{
				child = selChild;
				clearGift();
			}
			
			setEnabledGift(fam);	//lock or unlock the panel and/or panel components
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.GIFT);
	}
	
	private enum GiftPanelStatus 
	{
		Enabled,
		Disabled,
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
		List<JComboBox<String>> cbox;
		String[] titles;	
		JTextField detailTF;
		JButton btnOK;
		
		public DetailDialog(JFrame pf, String wishname, ArrayList<GiftDetail> wdAL)
		{
			super(pf, true);
			this.setTitle("Additional " + wishname + " Detail");
			
			//Create the combo boxes
			titles = new String[wdAL.size()];
			cbox = new ArrayList<JComboBox<String>>();
			
			JPanel infopanel = new JPanel();			
			
			for(int i=0; i<wdAL.size(); i++)
			{
				titles[i] = wdAL.get(i).getWishDetailName();
				JComboBox<String> addDetailChoice = new JComboBox<String>(wdAL.get(i).getWishDetailChoices());
				addDetailChoice.setBorder(BorderFactory.createTitledBorder(titles[i]));
				cbox.add(addDetailChoice);
				infopanel.add(addDetailChoice);
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
			StringBuffer detail = new StringBuffer(cbox.get(0).getSelectedItem().toString());
			for(int i=1; i<titles.length; i++)
			{
				if(titles[i].toLowerCase().contains("size"))
					detail.append(", " + "Sz: " + cbox.get(i).getSelectedItem().toString());
				else if(titles[i].toLowerCase().contains("color"))
				{
					if(!cbox.get(i).getSelectedItem().toString().equals("Any") &&
						!cbox.get(i).getSelectedItem().toString().equals("?"))
							detail.append(", " + cbox.get(i).getSelectedItem().toString());
				}
				else
					detail.append(", " + titles[i] + ": "+ cbox.get(i).getSelectedItem().toString());
			}
					
			if(detailTF.getText().isEmpty())
				return detail.toString();
			else
				return detail.toString() + ", " + detailTF.getText();
		}
		
		void clearDetail()
		{
			for(int i = 0; i<cbox.size(); i++)	//Clear combo boxes
				cbox.get(i).setSelectedIndex(0);
			
			detailTF.setText("");			
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == btnOK) 
				this.setVisible(false);
		}
	}
	
	private class InventoryTransferhandler extends TransferHandler
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
		{
			if(gpStatus.equals(GiftPanelStatus.Disabled))
				return false;
			else if(childGift == null)
				return true;
			else
			{
				GiftStatus ws = childGift.getGiftStatus();
				if(ws.compareTo(GiftStatus.Delivered) <= 0)
					return true;
				else if(ws.equals(GiftStatus.Returned))
					return true;
				else if(ws.equals(GiftStatus.Missing))
					return true;
				else
					return false;
			}
		}
		
		@Override
		public boolean importData(JComponent comp, Transferable t)
		{
			DataFlavor df = new DataFlavor(InventoryItem.class, "InventoryItem");
			InventoryItem transItem;
			try 
			{
				transItem = (InventoryItem) t.getTransferData(df);
				ONCChildGift transferredWish = createGiftFromInventoryTransfer(transItem);
				if(transferredWish != null)
					display(transferredWish, child);
				return true;
			} 
			catch (UnsupportedFlavorException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
		}
		
		ONCChildGift createGiftFromInventoryTransfer(InventoryItem ii)
		{
			//create new wish with the wish status = WishStatus.ASSIGNED && assignee = ONC_CONTAINER
			ONCPartner partner = partnerDB.getPartnerByNameAndType("ONC Container", 6);
			ONCChildGift addWishReq = null;
			if(partner != null && child != null)
			{
				addWishReq = cwDB.add(this, child.getID(), ii.getWishID(), ii.getItemName(),
										giftNumber, 0, GiftStatus.Assigned, partner);
			}
			
			return addWishReq;
		}
	}
}
