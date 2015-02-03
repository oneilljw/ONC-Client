package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

public abstract class GiftActionDialog extends SortTableDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTextField oncnumTF;
	private JComboBox startAgeCB, genderCB;
	private JButton btnUndo; 
	
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ONCWishCatalog cat;
	
	private ArrayList<ONCSortObject> stAL;
	protected ArrayList<ONCSortObject> tableRowSelectedObjectList;
	protected ONCSortObject lastWishChanged;	//Holds the last wish received for undo function
	
	private int sortStartAge, sortGender;
	
	private static String[] genders = {"Any", "Boy", "Girl"};
	
	GiftActionDialog(JFrame pf, String[] columnToolTips, String[] columns, int[] colWidths, int[] center_cols, WishStatus dialogType)
	{
		super(pf, columnToolTips, columns, colWidths, center_cols);
		
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		cat = ONCWishCatalog.getInstance();
		
		if(cDB != null)
			cDB.addDatabaseListener(this);	//Child updates
		if(cwDB != null)
			cwDB.addDatabaseListener(this);	//Wish updates
		
		//Create/initialize the class variables
		stAL = new ArrayList<ONCSortObject>();
		tableRowSelectedObjectList = new ArrayList<ONCSortObject>();
		sortStartAge = 0;
		sortGender = 0;
		
		//set the title in accordance with the purpose
		this.setTitle(String.format("Our Neighbor's Child - %s Gifts", dialogType.presentTense()));
		
		//Set up the search criteria panel      
    	oncnumTF = new JTextField();
    	oncnumTF.setEditable(true);
    	oncnumTF.setPreferredSize(new Dimension(88,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
    	String[] ages = {"Any", "<1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
		startAgeCB = new JComboBox(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Child Age"));
		startAgeCB.setToolTipText("Select or Type Child's Age and press <enter>");
		startAgeCB.setPreferredSize(new Dimension(88,56));
		startAgeCB.setEditable(true);
		startAgeCB.addActionListener(this);
		
		genderCB = new JComboBox(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setToolTipText("Select Child's Gender");
		genderCB.setSize(new Dimension(96,56));
		genderCB.addActionListener(this);

		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(genderCB);
		
		//change the default row selection setting to single row selection
		sortTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //Set up the control panel, adding an undo button
		btnUndo = new JButton(gvs.getImageIcon(16));
        btnUndo.setToolTipText(String.format("Click to undo last gift %s", dialogType.toString()));
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
        cntlPanel.add(btnUndo);
        
        //change the text of the super class apply changes button
        btnApplyChanges.setText(String.format("%s Gift", dialogType.presentTense()));
         
        //Add the components to the frame pane and pack
        this.add(bottomPanel);       
        pack();
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
								stAL.add(new ONCSortObject(index++, f, c, cw));
							}
						}
					}
				}
			}
		}
		
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table array list is built	
	}
	
	void archiveTableSelections(ArrayList<? extends ONCObject> stAL)
	{
		tableRowSelectedObjectList.clear();
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			ONCSortObject so = (ONCSortObject) stAL.get(row_sel[i]);
			tableRowSelectedObjectList.add(so);
		}
	}
	
	@Override
	boolean onApplyChanges()
	{
		bChangingTable = true;
		
		int row_sel = sortTable.getSelectedRow();
		
		//Find child and wish number for selected
		int oncID = stAL.get(row_sel).getFamily().getID();
		
		ONCFamily fam = fDB.searchForFamilyByID(oncID);
			
		ONCChild c = stAL.get(row_sel).getChild();
		int wn = stAL.get(row_sel).getChildWish().getWishNumber();
		ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));

		//Get current wish information
		int wishtypeid = cw.getWishID();
		String cwd = cw.getChildWishDetail();
		int cwi = cw.getChildWishIndicator();
		int cwaID = cw.getChildWishAssigneeID();
			
		//Process change to wish status. Store the new wish to be added in case of an undo operation 
		//and add the new wish to the child wish history. We reuse an ONCSortObject to store the
		//new wish
		lastWishChanged = new ONCSortObject(-1, fam, c, cw);
			
		int wishid = cwDB.add(this, c.getID(), wishtypeid, cwd, wn, cwi, getGiftStatusAction(),
								cwaID, gvs.getUserLNFI(), gvs.getTodaysDate());
			
		c.setChildWishID(wishid, wn);				
				
		//Update the sort table itself
		buildTableList(false);
		
		sortTable.clearSelection();
		
		btnApplyChanges.setEnabled(false);
		
		btnUndo.setEnabled(true);
		
		bChangingTable = false;
		
		return true;
	}
	
	abstract int getGiftStatusAction();
	
	void onUndoReceiveGift()
	{
		bChangingTable = true;
		
		//To undo the wish, add the old wish back with the previous status		
		ONCChild lastChild = lastWishChanged.getChild();
		ONCChildWish lastWish = lastWishChanged.getChildWish();
		
		int wishid = cwDB.add(this, lastWishChanged.getChild().getID(),
									lastWish.getWishID(),
									lastWish.getChildWishDetail(),
									lastWish.getWishNumber(),
									lastWish.getChildWishIndicator(),
									lastWish.getChildWishStatus(),
									lastWish.getChildWishAssigneeID(),
									gvs.getUserLNFI(),
									gvs.getTodaysDate());
		
		lastChild.setChildWishID(wishid, lastWish.getWishNumber());
		
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
		
		bChangingTable = false;
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
		//Undo button is only control
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
		}		
		else if(e.getSource() == genderCB && genderCB.getSelectedIndex() != sortGender)
		{
			sortGender = genderCB.getSelectedIndex();
			sortONCNum = oncnumTF.getText();	//TF might have changed, without enter key
			buildTableList(false);
		}
		else if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList(false);
		}
		else if(e.getSource() == btnUndo)
		{
			//Receive gifts dialog recGift button event handler is in this class for coordination
			//with update of the child panel and family status
			onUndoReceiveGift();
		}
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
		
		buildTableList(false);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel() &&
				!bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getChild();
			fireEntitySelected(this, "WISH_SELECTED", fam, child);
			
//			System.out.println(String.format("GiftActionDialog.valueChanged: lse received"));
			checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
			
			sortTable.requestFocus();
		}
	}
	
	@Override
	String[] getTableRow(ONCObject obj)
	{
		ONCSortObject so = (ONCSortObject) obj;
		
		String[] tableRow = {so.getFamily().getONCNum(), so.getChild().getChildGender(),
				so.getChild().getChildAge(),
				cat.getWishByID(so.getChildWish().getWishID()).getName(),
				so.getChildWish().getChildWishDetail()};
		
		return tableRow;
	}
/*
	private class GiftSortItem extends ONCSortObject
	{	
		public GiftSortItem(int id, ONCFamily fam, ONCChild c, ONCChildWish cw)
		{
			super(id, fam, c, cw);
		}
		
		public String[] getSortTableRow()
		{
			String[] sorttablerow = {soFamily.getONCNum(), soChild.getChildGender(),
										soChild.getChildAge(),
										cat.getWishByID(soChildWish.getWishID()).getName(),
										soChildWish.getChildWishDetail()};
			return sorttablerow;
		}
	}

	 /************************************************************************************
	  * This class defines the object used to store the last wish received by the Receive
	  * Gifts dialog user in support of the undo function.
	  * @author John O'Neill
	  ************************************************************************************/
/*	
	 class LastWishChanged
	 {
		 private ONCFamily fam;
		 private int lastFamilyStatus;
		 private ONCChild child;
		 private ONCChildWish lastWishReceived;
		 
		 LastWishChanged(ONCFamily f, ONCChild c, ONCChildWish lwr)
		 {
			 fam = f;
			 lastFamilyStatus = f.getFamilyStatus();
			 child = c;
			 lastWishReceived = lwr;
		 }
		 
		 //getters
		 ONCFamily getLastFamily() { return fam; }
		 int getLastFamilyStatus() { return lastFamilyStatus; }
		 ONCChild getLastChild() { return child; }
		 ONCChildWish getLastWishReceived() { return lastWishReceived; }
	 }
*/
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_CHILD") || 
										dbe.getType().equals("DELETED_CHILD") ||
										 dbe.getType().equals("WISH_ADDED")))
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
}
