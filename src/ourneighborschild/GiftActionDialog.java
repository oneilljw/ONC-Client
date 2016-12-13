package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

public abstract class GiftActionDialog extends SortTableDialog
{
	/**
	 * Provides a base class for dialogs that allow users to act on gifts in the warehouse
	 * operation. Gifts can be received or audited in the warehouse, using either
	 * manual data entry from a keyboard or by scanning a bar code on the ornament label
	 * if the bar code option is employed when labels are generated
	 */
	private static final long serialVersionUID = 1L;
	private static final int SOUND_DURATION = 250;
	private static final int SUCCESS_SOUND_FREQ = 500;
	private static final int FAILED_SOUND_FREQ = 150;
	private static final String BLANK_RESULT_LABEL = "          ";
	
	private static final int GIFT_ACTION_SUCCESSFUL = 0;
	private static final int GIFT_ACTION_ALREADY_OCCURRED = 1;
	private static final int GIFT_STATUS_INVALID = -3;
	private static final int GIFT_ACTION_REQUEST_SERVER_FAILURE = -4;
	private static final int CHILD_NOT_IN_LOCAL_DB = -5;
	private static final int GIFT_ACTION_FAMILY_NOT_SERVED = -6;

	private JTextField oncnumTF, barcodeTF;
	private JComboBox startAgeCB, genderCB;
	private JButton btnUndo;
	private JLabel lblResult;
	private Color pBkColor; //Used to restore background after gift action successful
	private Color[] successColor;
	private int successColorIndex;
	
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ONCWishCatalog cat;
	
	private ArrayList<SortWishObject> stAL;
	protected SortWishObject lastWishChanged;	//Holds the last wish received for undo function
	
	private int sortStartAge, sortGender;
	
	private static String[] genders = {"Any", "Boy", "Girl"};
	
	GiftActionDialog(JFrame pf, WishStatus dialogType)
	{
		super(pf, 15);
		
		//create/initialize the success colors
		successColor = new Color[2];
		successColor[0] = new Color(0, 225, 0);
		successColor[1] = new Color(0, 125, 0);
		successColorIndex = 0;
		
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		cat = ONCWishCatalog.getInstance();
		
		if(cDB != null)
			cDB.addDatabaseListener(this);	//Child updates
		if(cwDB != null)
			cwDB.addDatabaseListener(this);	//Wish updates
		
		//Create/initialize the class variables
		stAL = new ArrayList<SortWishObject>();
		sortStartAge = 0;
		sortGender = 0;
		
		//set the title in accordance with the purpose
		this.setTitle(String.format("Our Neighbor's Child - %s Gifts", dialogType.presentTense()));
		
		pBkColor = sortCriteriaPanel.getBackground();
		
		//Set up the search criteria panel      
    	oncnumTF = new JTextField(5);
    	oncnumTF.setEditable(true);
    	oncnumTF.setMaximumSize(new Dimension(64,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
    	String[] ages = {"Any", "<1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
		startAgeCB = new JComboBox(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Child Age"));
		startAgeCB.setToolTipText("Select or Type Child's Age and press <enter>");
		startAgeCB.setMaximumSize(new Dimension(88,56));
		startAgeCB.setEditable(true);
		startAgeCB.addActionListener(this);
		
		genderCB = new JComboBox(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setToolTipText("Select Child's Gender");
		genderCB.setMaximumSize(new Dimension(96,56));
		genderCB.addActionListener(this);
		
		barcodeTF = new JTextField(6);
    	barcodeTF.setMaximumSize(new Dimension(96,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Type Barcode and press <enter>");
		barcodeTF.addActionListener(this);
		
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(oncnumTF);		
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(genderCB);
		sortCriteriaPanelTop.add(Box.createHorizontalGlue());
		sortCriteriaPanelTop.add(barcodeTF);
		
		sortCriteriaPanel.remove(sortCriteriaPanelBottom);	//inherited panel not used
		
		//change the default row selection setting to single row selection
		sortTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//change the text of the super class apply changes button
        btnApplyChanges.setText(String.format("%s Gift", dialogType.presentTense()));

        //add an undo button and label to the control panel.
		JPanel cntlPanel = new JPanel();
		
		btnUndo = new JButton(gvs.getImageIcon(16));
        btnUndo.setToolTipText(String.format("Click to undo last gift %s", dialogType.toString()));
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
        cntlPanel.add(btnUndo);
        
        lblResult = new JLabel();
        cntlPanel.add(lblResult);
        
        bottomPanel.add(cntlPanel, BorderLayout.LINE_START);
        
       
        
        //Add the components to the frame pane and pack
        this.add(bottomPanel);
        this.setResizable(false);
        pack();
        
        barcodeTF.requestFocusInWindow();	//we want scans to process immediately
	}

	void buildTableList(boolean bPreserveSelections)
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(stAL);
		else
			tableSortCol = -1;
		
		stAL.clear();	//Clear the prior table information in the array list
		int index = 0;
		for(ONCFamily f:fDB.getList())
		{
			//ONC number is valid and matches criteria
			if(isNumeric(f.getONCNum()) && f.getDNSCode().isEmpty() && doesONCNumMatch(f.getONCNum()))	
			{
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c))	//Children criteria pass
					{
						for(int i=0; i< cwDB.getNumberOfWishesPerChild(); i++)
						{	
							ONCChildWish cw = cwDB.getWish(c.getChildWishID(i));
							
							//Status matches and wish was assigned (Wish indicator is not *)
							if(cw != null && doesChildWishStatusMatch(cw))
							{
								stAL.add(new SortWishObject(index++, f, c, cw));
							}
						}
					}
				}
			}
		}
		
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table array list is built	
	}
	
	/****
	 * Method processes a gift identified by child id and wish number. actOnGift to change the status of the child wish/gift.
	 * A number of error conditions may occur that result in the wish/gift being invalid.
	 * The method returns an integer code that provides a result of the attempt to change the
	 * wish/gift status. The background color of the search criteria panel color is changed to
	 * either green or red depending on the success of the requested state change
	 * 
	 * Return codes are:
	 * 0: wish/gift status successfully changed
	 * 1: wish/gift status unchanged, was already successfully changed
	 * -1: wish/gift is not in the local copy of the server data base, indicating the wish/gift
	 * is not current
	 * -2: wish/gift is in the local data base, but is not current & also not in a valid state
	 * -3: wish/gift is current, but not in valid state for the requested action
	 * -4: wish/gift state change not processed successfully by the server
	 * @param cID - child ID number
	 * @param wn - wish number
	 * @return error code 0 or 1 indicates success, a negative number indicates an error
	 */
	GiftActionReturnCode actOnGiftFromBarcode(int cID, int wn)
	{
//		int returnCode;
		GiftActionReturnCode rc = null;
		
		ONCChildWish cw = cwDB.getWish(cID, wn);	//get latest wish for child
		if(cw == null)
			rc = new GiftActionReturnCode(CHILD_NOT_IN_LOCAL_DB, null);
//			returnCode = CHILD_NOT_IN_LOCAL_DB;	//wish not in local child wish data base
		else if(cw.getChildWishStatus() == getGiftStatusAction())
			rc = new GiftActionReturnCode(GIFT_ACTION_ALREADY_OCCURRED, new SortWishObject(-1, null, null, cw));
//			returnCode = GIFT_ACTION_ALREADY_OCCURRED;	//double scan
		else if(!doesChildWishStatusMatch(cw))
			rc = new GiftActionReturnCode(GIFT_STATUS_INVALID, null);
//			returnCode = GIFT_STATUS_INVALID;	//wish  not in valid state
		else
		{
			//attempt to act on the wish/gift,if it fails, server didn't accept update
			ONCChild c = cDB.getChild(cw.getChildID());
			ONCFamily f = fDB.getFamily(c.getFamID());
			SortWishObject swo = new SortWishObject(-1, f, c, cw);
			
			//should only be if family is being served
			if(!f.getDNSCode().isEmpty())
				rc = new GiftActionReturnCode(GIFT_ACTION_FAMILY_NOT_SERVED, swo);
			else if(actOnGift(swo))
				rc = new GiftActionReturnCode(GIFT_ACTION_SUCCESSFUL, swo);
//				returnCode = GIFT_ACTION_SUCCESSFUL;
			else
				rc = new GiftActionReturnCode(GIFT_ACTION_REQUEST_SERVER_FAILURE, null);
//				returnCode = GIFT_ACTION_REQUEST_SERVER_FAILURE;
		}
		
		if(rc.getReturnCode() >= 0)
		{	
			setSearchCriteriaBackgroundColor(successColor[successColorIndex]);
			successColorIndex = (successColorIndex + 1) % 2;
		}
		else
		{
			successColorIndex = 0;
			setSearchCriteriaBackgroundColor(Color.RED);
		}
		
		clearBarCode();
		return rc;
	}

	@Override
	boolean onApplyChanges()
	{
		return actOnGift(stAL.get(sortTable.getSelectedRow()));
	}
	
	boolean actOnGift(SortWishObject swo)
	{
		//Process change to wish status. Store the new wish to be added in case of an undo operation 
		//and add the new wish to the child wish history. We reuse an ONCSortObject to store the
		//new wish. Organization parameter is null, indicating we're not changing the gift partner
		lastWishChanged = new SortWishObject(-1, swo.getFamily(), swo.getChild(), swo.getChildWish());
					
		ONCChildWish addedWish = cwDB.add(this, swo.getChild().getID(), swo.getChildWish().getWishID(),
											swo.getChildWish().getChildWishDetail(),
											swo.getChildWish().getWishNumber(),
											swo.getChildWish().getChildWishIndicator(), 
											getGiftStatusAction(), null);
		
		//Update the sort table, as the wish status should have changed
		if(addedWish != null)
		{
			buildTableList(false);
			btnUndo.setEnabled(true);
			btnApplyChanges.setEnabled(false);
		}
		
		sortTable.clearSelection();
		barcodeTF.requestFocus();
		
		return addedWish != null;
	}
	
	abstract WishStatus getGiftStatusAction();
	
	void onUndoReceiveGift()
	{
		//To undo the wish, add the old wish back with the previous status		
		ONCChildWish lastWish = lastWishChanged.getChildWish();
		
		cwDB.add(this, lastWishChanged.getChild().getID(),
					lastWish.getWishID(), lastWish.getChildWishDetail(),
					lastWish.getWishNumber(),lastWish.getChildWishIndicator(),
					lastWish.getChildWishStatus(), null);	//null to keep same partner
		
		//Update the receive gifts sort table itself
		buildTableList(false);
				
		//check to see if family status should change as well. If the family status had
		//changed to gifts received with the receive that is being undone, the family 
		//status must be reset to the prior status.
		if(changeFamilyStatus())
			lastWishChanged.getFamily().setFamilyStatus(lastWishChanged.getFamily().getFamilyStatus());
		
		sortTable.clearSelection();
		
		btnApplyChanges.setEnabled(false);
		
		btnUndo.setEnabled(false);
	}
	
	abstract boolean changeFamilyStatus();
	
	@Override
	void checkApplyChangesEnabled()
	{
		btnApplyChanges.setEnabled(sortTable.getSelectedRows().length > 0);
	}
	
	@Override
	void setEnabledControls(boolean tf)
	{
		//Undo button is only control and it is enabled when a gift is acted on
	}

	private boolean isAgeInRange(ONCChild c)
	{
		return sortStartAge == 0 || c.getChildIntegerAge() == startAgeCB.getSelectedIndex()-1;		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	abstract boolean doesChildWishStatusMatch(ONCChildWish cw);

	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == startAgeCB && startAgeCB.getSelectedIndex() != sortStartAge)
		{
			sortStartAge = startAgeCB.getSelectedIndex();
			sortONCNum = oncnumTF.getText();	//TF might have changed, without enter key
			buildTableList(false);
			setSearchCriteriaBackgroundColor(pBkColor);
		}		
		else if(e.getSource() == genderCB && genderCB.getSelectedIndex() != sortGender)
		{
			sortGender = genderCB.getSelectedIndex();
			sortONCNum = oncnumTF.getText();	//TF might have changed, without enter key
			buildTableList(false);
			setSearchCriteriaBackgroundColor(pBkColor);
		}
		else if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList(false);
			setSearchCriteriaBackgroundColor(pBkColor);
		}
		else if(e.getSource() == btnUndo)
		{
			//Receive gifts dialog recGift button event handler is in this class for coordination
			//with update of the child panel and family status
			onUndoReceiveGift();
			setSearchCriteriaBackgroundColor(pBkColor);
		}
		else if(e.getSource() == barcodeTF)
		{
			if(gvs.getBarcodeCode().length() != barcodeTF.getText().length())
			{
				lblResult.setText(String.format("Barcode %s is invalid", barcodeTF.getText()));
				try {
					SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
				} catch (LineUnavailableException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else
			{
				//if using UPC-E, eliminate check digits before converting to childID and wish number
				int cID, wn;
				String s = barcodeTF.getText();
				if(gvs.getBarcodeCode() == Barcode.UPCE)
				{
					cID = Integer.parseInt(s.substring(0, s.length()-2));
					wn = Integer.parseInt(s.substring(s.length()-2, s.length()-1));
						
				}
				else
				{
					cID = Integer.parseInt(s.substring(0, s.length()-1));
					wn = Integer.parseInt(s.substring(s.length()-1, s.length()-0));
				}
			
				try
				{
					GiftActionReturnCode rc = actOnGiftFromBarcode(cID, wn);
					if(rc.getReturnCode() == GIFT_ACTION_SUCCESSFUL)
					{
//						ONCChild child = cDB.getChild(cID);
//						ONCFamily fam = fDB.getFamily(child.getFamID());
//						ONCChildWish cw = cwDB.getWish(cID, wn);
//						ONCWish catWish = cat.getWishByID(cw.getWishID());
//						String wishName = catWish.getName();
						
						ONCFamily fam = rc.getSortWishObject().getFamily();
						ONCChildWish cw = rc.getSortWishObject().getChildWish();
						String wishName = cat.getWishByID(cw.getWishID()).getName();
						
						String mssg = String.format("Family# %s: %s- %s received", 
								fam.getONCNum(), wishName, cw.getChildWishDetail());
						
						lblResult.setText(mssg);
						
//						lblResult.setText(String.format("Gift %d for child %d received", wn+1, cID));
						SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc.getReturnCode() == GIFT_ACTION_FAMILY_NOT_SERVED)
					{
						//family was no longer being served
						ONCFamily fam = rc.getSortWishObject().getFamily();
						
						lblResult.setText(String.format("Family #s is not being served", fam));
						SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc.getReturnCode() == GIFT_ACTION_ALREADY_OCCURRED)
					{
						ONCChildWish cw = rc.getSortWishObject().getChildWish();
						String wishName = cat.getWishByID(cw.getWishID()).getName();
						
						lblResult.setText(String.format("Gift: %s- %s already received", wishName, cw.getChildWishDetail()));
						SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc.getReturnCode() == CHILD_NOT_IN_LOCAL_DB)
					{
						lblResult.setText(String.format("Child %d not in local DB", cID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc.getReturnCode() == GIFT_STATUS_INVALID)
					{
						lblResult.setText(String.format("Gift %d for child %d status invalid", wn+1, cID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc.getReturnCode() == GIFT_ACTION_REQUEST_SERVER_FAILURE)
					{
						lblResult.setText(String.format("Gift %d for child %d receive server error", wn+1, cID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
				} 
				catch (LineUnavailableException lue) 
				{
					// TODO Auto-generated catch block
					lue.printStackTrace();
				}
			}
		}
		
//		barcodeTF.requestFocus();
	}
	
	//Resets each search criteria gui and its corresponding member variable to the initial
	//condition and then rebuilds the table array. It disables the gui event before changing
	//to prevent multiple builds of the table.
	@Override
	void onResetCriteriaClicked()
	{
		oncnumTF.removeActionListener(this);
		oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
		sortONCNum = "";
		oncnumTF.addActionListener(this);
		
		startAgeCB.removeActionListener(this);
		startAgeCB.setSelectedIndex(0);	//Will trigger the CB event handler which
		sortStartAge = 0;				//will determine if the CB changed. Therefore,
		startAgeCB.addActionListener(this);
		
		genderCB.removeActionListener(this);
		genderCB.setSelectedIndex(0);	//no need to test for a change here.
		sortGender = 0;
		genderCB.addActionListener(this);
		
		clearBarCode();
		
		setSearchCriteriaBackgroundColor(pBkColor);
		
		buildTableList(false);
		
		checkApplyChangesEnabled();
		
		lblResult.setText(BLANK_RESULT_LABEL);
		
		barcodeTF.requestFocus();
	}
	
	void setSearchCriteriaBackgroundColor(Color color)
	{
		sortCriteriaPanel.setBackground(color);
		sortCriteriaPanelTop.setBackground(color);
		sortCriteriaPanelBottom.setBackground(color);
	}
	
	void clearBarCode()
	{
		barcodeTF.removeActionListener(this);
		barcodeTF.setText("");	//no need to test for a change here.
		barcodeTF.addActionListener(this);
	}
	
	void barcodeRequestFocus()
	{
		barcodeTF.requestFocus();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel()  &&
				sortTable.getSelectedRowCount() > 0 && !bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getChild();
			fireEntitySelected(this,EntityType.WISH, fam, child);
			
			checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
			
//			sortTable.requestFocus();
			barcodeTF.requestFocus();
		}
	}
	
	@Override
	String[] getTableRow(ONCObject obj)
	{
		SortWishObject so = (SortWishObject) obj;
		ONCWish wish = cat.getWishByID(so.getChildWish().getWishID());
		String wishName = wish == null ? "None" : wish.getName();
		
		String[] tableRow = {so.getFamily().getONCNum(), so.getChild().getChildGender(),
				so.getChild().getChildAge(), wishName,
				so.getChildWish().getChildWishDetail()};
		
		return tableRow;
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_CHILD") || 
										dbe.getType().equals("DELETED_CHILD") ||
										 dbe.getType().equals("WISH_ADDED") ||
										  dbe.getType().equals("UPDATED_CHILD_WISH")))
		{
			buildTableList(true);
			barcodeTF.requestFocus();
		}
		else if(dbe.getType().equals("LOADED_WISHES"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %s %d Gifts", 
				getGiftStatusAction().presentTense(), GlobalVariables.getCurrentSeason()));
		}
	}
	
	@Override
	int sortTableList(int col)
	{
		return -1;	//sorting not supported
	}
	
	@Override
	boolean isONCNumContainerEmpty() { return oncnumTF.getText().isEmpty(); }
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.WISH);
	}
	
	private class GiftActionReturnCode
	{
		private int rc;
		private SortWishObject swo;
		
		GiftActionReturnCode(int rc, SortWishObject swo)
		{
			this.rc = rc;
			this.swo = swo;
		}
		
		//getters
		int getReturnCode() { return rc; }
		SortWishObject getSortWishObject() { return swo; }
	}
}
