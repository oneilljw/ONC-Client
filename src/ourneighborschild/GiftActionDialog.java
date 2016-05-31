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

	private JTextField oncnumTF, barcodeTF;
	private JComboBox startAgeCB, genderCB;
	private JButton btnUndo;
	private JLabel lblResult;
	private Color pBkColor; //Used to restore background after gift action successful
	
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
		
		pBkColor = sortCriteriaPanelTop.getBackground();
		
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
    	barcodeTF.setMaximumSize(new Dimension(192,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Type Barcode and press <enter>");
		barcodeTF.addActionListener(this);
		
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(genderCB);
		sortCriteriaPanelTop.add(Box.createRigidArea(new Dimension(5,0)));
		sortCriteriaPanelTop.add(barcodeTF);
		
		//change the default row selection setting to single row selection
		sortTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		 //change the text of the super class apply changes button
        btnApplyChanges.setText(String.format("%s Gift", dialogType.presentTense()));

        //add an undo button and label to the a control panel.
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
        pack();
        this.setResizable(false);
        
        barcodeTF.requestFocus();
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
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()))	
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
	
	int actOnGiftFromBarcode(int cwID)
	{
		//bar code could be from a wish that is not in the local data base because it is not
		//current. Or it may be in the local DB and not a current wish.
		//Need to account for both of these. Cannot act on a gift that is not current
		int returnCode;
		
		ONCChildWish cw = cwDB.getWish(cwID);
		if(cw == null)
			returnCode = -1;	//wish not in local child wish data base
		else
		{
			//determine if wish is current
			ONCChild c = cDB.getChild(cw.getChildID());
			if(c.getChildWishID(cw.getWishNumber()) != cwID)
			{
				//wish is not current, determine if it's in the correct status already, i.e.
				//it's accidently been double scanned.
				ONCChildWish newerWish = cwDB.getWish(c.getID(), cw.getWishNumber());
				if(newerWish.getChildWishStatus() == getGiftStatusAction() && 
						doesChildWishStatusMatch(cw))
					returnCode = 1;	//double scan
				else
					returnCode = -2;	//wish not current and not in correct state
			}
			else
			{
				ONCFamily f = fDB.getFamily(c.getFamID());
				SortWishObject swo = new SortWishObject(-1, f, c, cw);
				if(!doesChildWishStatusMatch(cw))
					returnCode = -3;	//wish is current and not in correct state
				else
					returnCode = actOnGift(swo) ? 0 : -4;	//if false, server didn't accept update
			}
		}
		
		if(returnCode >= 0)
			setSearchCriteriaBackgroundColor(Color.GREEN);
		else
			setSearchCriteriaBackgroundColor(Color.RED);
		
		clearBarCode();
		return returnCode;
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
		barcodeTF.grabFocus();
		
		return addedWish != null;
	}
	
	abstract WishStatus getGiftStatusAction();
	
	void onUndoReceiveGift()
	{
		//To undo the wish, add the old wish back with the previous status		
		ONCChildWish lastWish = lastWishChanged.getChildWish();
		
		ONCChildWish addedWish = cwDB.add(this, lastWishChanged.getChild().getID(),
									lastWish.getWishID(),
									lastWish.getChildWishDetail(),
									lastWish.getWishNumber(),
									lastWish.getChildWishIndicator(),
									lastWish.getChildWishStatus(),
									null);	//null to keep same partner
		
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
				//if using UPC-E, eliminate check digits before converting to childwishID integer
				int cwID;
				if(gvs.getBarcodeCode() == Barcode.UPCE)
					cwID = Integer.parseInt(barcodeTF.getText().substring(0, barcodeTF.getText().length()-1));
				else
					cwID = Integer.parseInt(barcodeTF.getText());
			
				try
				{
					int rc = actOnGiftFromBarcode(cwID);
					if(rc == 0)
					{
						lblResult.setText(String.format("Gift %d received", cwID));
						SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc == 1)
					{
						lblResult.setText(String.format("Gift %d already received", cwID));
						SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc == -1)
					{
						lblResult.setText(String.format("Gift %d not in local DB", cwID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc == -2)
					{
						lblResult.setText(String.format("Gift %d not current", cwID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc == -3)
					{
						lblResult.setText(String.format("Gift %d status invalid", cwID));
						SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
					}
					else if(rc == -4)
					{
						lblResult.setText(String.format("Gift %d receive server error", cwID));
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
		sortCriteriaPanelTop.setBackground(color);
		sortCriteriaPanelBottom.setBackground(color);
	}
	
	void clearBarCode()
	{
		barcodeTF.removeActionListener(this);
		barcodeTF.setText("");	//no need to test for a change here.
		barcodeTF.addActionListener(this);
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
			
			sortTable.requestFocus();
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
}
