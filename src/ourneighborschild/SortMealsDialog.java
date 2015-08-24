package ourneighborschild;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import ourneighborschild.SortTableDialog.ONCNumberKeyListener;

import com.toedter.calendar.JDateChooser;

public class SortMealsDialog extends ChangeDialog implements PropertyChangeListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Families fDB;
	private MealDB mDB;
	private ONCOrgs orgs;
	protected ONCRegions regions;

	private ArrayList<SortMealObject> stAL;

	
	private JComboBox typeCB, assignCB, statusCB, changedByCB, regionCB;
	private JComboBox changeAssigneeCB, printCB;
	private DefaultComboBoxModel assignCBM, changeAssigneeCBM, changedByCBM, regionCBM;
	private JTextField oncnumTF;
	private JButton btnExport;
	private JDateChooser ds, de;
	private Calendar sortStartCal = null, sortEndCal = null;
	
	private int sortChangedBy = 0, sortAssigneeID = 0, sortRegion = 0;
	private MealStatus sortStatus = MealStatus.Any;
	private MealType sortType = MealType.Any;

//	private int totalNumOfLabelsToPrint;	//Holds total number of labels requested in a print job
	
	
	SortMealsDialog(JFrame pf, String[] colToolTips, String[] cols, int[] colWidths, int[] center_cols)
	{
		super(pf, colToolTips, cols, colWidths, center_cols);
		this.setTitle("Our Neighbor's Child - Meal Management");
		
		//set up the data base references
		fDB = Families.getInstance();
		mDB = MealDB.getInstance();
		orgs = ONCOrgs.getInstance();
		regions = ONCRegions.getInstance();
		
		//set up data base listeners
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(mDB != null)
			mDB.addDatabaseListener(this);
		if(orgs != null)
			orgs.addDatabaseListener(this);
		if(regions != null)
			regions.addDatabaseListener(this);
		
		//initialize member variables
		stAL = new ArrayList<SortMealObject>();

		//Set up the search criteria panel
		oncnumTF = new JTextField();
    	oncnumTF.setEditable(true);
    	oncnumTF.setPreferredSize(new Dimension(72,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
		
		typeCB = new JComboBox(MealType.getSearchFilterList());
		typeCB.setPreferredSize(new Dimension(156, 56));
		typeCB.setBorder(BorderFactory.createTitledBorder("Holiday Requested"));
		typeCB.addActionListener(this);
		
		statusCB = new JComboBox(MealStatus.getSearchFilterList());
		statusCB.setPreferredSize(new Dimension(136, 56));
		statusCB.setBorder(BorderFactory.createTitledBorder("Meal Status"));
		statusCB.addActionListener(this);
		
		regionCBM = new DefaultComboBoxModel();
		regionCBM.addElement("Any");
		regionCB = new JComboBox();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.addActionListener(this);
		
		assignCB = new JComboBox();
		assignCBM = new DefaultComboBoxModel();
	    assignCBM.addElement(new Organization(0, "Any", "Any"));
	    assignCBM.addElement(new Organization(-1, "None", "None"));
	    assignCB.setModel(assignCBM);
		assignCB.setPreferredSize(new Dimension(192, 56));
		assignCB.setBorder(BorderFactory.createTitledBorder("Meal Assigned To"));
		assignCB.addActionListener(this);

		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
		changedByCB.setPreferredSize(new Dimension(144,56));
		changedByCB.addActionListener(this);
		
		sortStartCal = Calendar.getInstance();
		sortStartCal.setTime(gvs.getSeasonStartDate());
		
		ds = new JDateChooser(sortStartCal.getTime());
		ds.setPreferredSize(new Dimension(156, 56));
		ds.setBorder(BorderFactory.createTitledBorder("Changed On/After"));
		ds.getDateEditor().addPropertyChangeListener(this);
		
		sortEndCal = Calendar.getInstance();
		sortEndCal.setTime(gvs.getTodaysDate());
		sortEndCal.add(Calendar.DATE, 1);
		
		de = new JDateChooser(sortEndCal.getTime());
		de.setPreferredSize(new Dimension(156, 56));
		de.setBorder(BorderFactory.createTitledBorder("Changed Before"));
		de.getDateEditor().addPropertyChangeListener(this);

		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(typeCB);
		sortCriteriaPanelTop.add(statusCB);
		sortCriteriaPanelTop.add(regionCB);
		sortCriteriaPanelBottom.add(assignCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(ds);
		sortCriteriaPanelBottom.add(de);
		
		//set up the change panel, which consists of the item count panel and the 
		//change data panel
		itemCountPanel.setBorder(BorderFactory.createTitledBorder("Meals Meeting Criteria"));
		
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Assign Meals to Partner"));
        
        changeAssigneeCB = new JComboBox();
        changeAssigneeCBM = new DefaultComboBoxModel();
	    changeAssigneeCBM.addElement(new Organization(0, "No Change", "No Change"));
	    changeAssigneeCBM.addElement(new Organization(-1, "None", "None"));
        changeAssigneeCB.setModel(changeAssigneeCBM);
        changeAssigneeCB.setPreferredSize(new Dimension(192, 56));
		changeAssigneeCB.setBorder(BorderFactory.createTitledBorder("Change Assignee To:"));
		changeAssigneeCB.addActionListener(this);
		
	
		changeDataPanel.add(changeAssigneeCB);
		
		gbc.gridx = 1;
	    gbc.ipadx = 0;
	    gbc.weightx = 1.0;
	    changePanel.add(changeDataPanel, gbc);

	    //set up the dialog defined control panel
        btnExport = new JButton("Export Data");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing"};
        printCB = new JComboBox(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        printCB.addActionListener(this);
        
//        btnApplyChanges = new JButton("Apply Changes");
//        btnApplyChanges.setEnabled(false);
//        btnApplyChanges.addActionListener(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,0,0,120);
//        cntlPanel.add(labelFitCxBox, gbc);
        gbc.gridx=1;
        gbc.insets = new Insets(0,0,0,0);
        cntlPanel.add(btnExport, gbc);
        gbc.gridx=2;
        cntlPanel.add(printCB, gbc);
 
        //add the bottom two panels to the dialog and pack
        this.add(changePanel);
        this.add(bottomPanel);
        pack();
	}
	
	@Override
	public void buildTableList(boolean bPreserveSelections)
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(stAL);
		else
			tableSortCol = -1;
		
		stAL.clear();	//Clear the prior table information in the array list
		
		int itemID = 0;
		for(ONCFamily f:fDB.getList())
		{
			if(isNumeric(f.getONCNum()) && f.getMealID() > -1 && doesONCNumMatch(f.getONCNum()))	//Must be a valid family	
			{
				ONCMeal m = mDB.getMeal(f.getMealID());
				if(m != null && doesTypeMatch(m.getType()) &&
								 doesStatusMatch(f.getMealStatus()) &&
								  doesRegionMatch(f.getRegion()) &&
								   doesAssigneeMatch(m.getPartnerID()) &&
								    isMealChangeDateBetween(m.getDateChanged()) &&
								     doesChangedByMatch(m.getChangedBy())) //meal criteria pass
				{			
					stAL.add(new SortMealObject(itemID++, f, m));
				}
			}
		}
		
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table array list is built	
	}

	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	private boolean doesTypeMatch(MealType mt){return sortType == MealType.Any || sortType.compareTo(mt) == 0;}
	private boolean doesStatusMatch(MealStatus ms){return sortStatus == MealStatus.Any || sortStatus.compareTo(ms) == 0;}
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	private boolean doesAssigneeMatch(int assigneeID)
	{	
		return sortAssigneeID == 0 || sortAssigneeID == assigneeID;
	}
	
	private boolean isMealChangeDateBetween(Date wcd)
	{
		return !wcd.after(sortEndCal.getTime()) && !wcd.before(sortStartCal.getTime());
	}
	
	private boolean doesChangedByMatch(String s) { return sortChangedBy == 0 || changedByCB.getSelectedItem().toString().equals(s); }
	
	/*********************************************************************************************
	 * This method updates the drop down selection list associated with the Wish Assignee combo
	 * boxes used to search for wish assignees and to select assignees. The lists must be updated
	 * every time an organization is confirmed or unconfirmed. 
	 * 
	 * The method must keep the current assignee selected in the assignCB even if their position 
	 * changes. 
	 *********************************************************************************************/
	void updateMealAssigneeSelectionList()
	{
		bIgnoreCBEvents = true;
		int currentAssigneeID = -1, currentChangeAssigneeID = -1;	//set to null for reselection
		int currentAssigneeIndex = assignCB.getSelectedIndex();
		int currentChangeAssigneeIndex = changeAssigneeCB.getSelectedIndex();
		
		if(assignCB.getSelectedIndex() > 1)	//leaves the current selection null if no selection made
			currentAssigneeID = ((Organization)assignCB.getSelectedItem()).getID();
		
		if(changeAssigneeCB.getSelectedIndex() > 1)
			currentChangeAssigneeID = ((Organization)changeAssigneeCB.getSelectedItem()).getID();
		
		assignCB.setSelectedIndex(0);
		sortAssigneeID = 0;
		changeAssigneeCB.setSelectedIndex(0);
		
		assignCBM.removeAllElements();
		changeAssigneeCBM.removeAllElements();
		
		assignCBM.addElement(new Organization(0, "Any", "Any"));
		assignCBM.addElement(new Organization(-1, "None", "None"));
		changeAssigneeCBM.addElement(new Organization(-1, "No Change", "No Change"));
		changeAssigneeCBM.addElement(new Organization(-1, "None", "None"));
		
		for(Organization confOrg :orgs.getConfirmedOrgList(GiftCollection.Meals))
		{
			assignCBM.addElement(confOrg);
			changeAssigneeCBM.addElement(confOrg);
		}
		
		//Attempt to reselect the previous selected organization, if one was selected. 
		//If the previous selection is no longer in the drop down, the top of the list is 
		//already selected 
		if(currentAssigneeIndex == 1)	//Organization == "None", ID = -1
		{
			assignCB.setSelectedIndex(1);
			sortAssigneeID = -1;
		}
		else if(currentAssigneeIndex > 1)
		{
			Organization assigneeOrg = orgs.getOrganizationByID(currentAssigneeID);
			if(assigneeOrg != null)
			{
				assignCB.setSelectedItem(assigneeOrg);
				sortAssigneeID = assigneeOrg.getID();	//Need to update the sort Assignee as well
			}
		}
		
		if(currentChangeAssigneeIndex == 1)		//Organization == "None"
			changeAssigneeCB.setSelectedIndex(1);
		else
		{
			Organization changeAssigneeOrg = orgs.getOrganizationByID(currentChangeAssigneeID);
			if(changeAssigneeOrg != null)
				changeAssigneeCB.setSelectedItem(changeAssigneeOrg);
		}
		
		bIgnoreCBEvents = false;
	}
	
	void updateRegionList(String[] regions)
	{
		regionCB.removeActionListener(this);
		String currSel = regionCB.getSelectedItem().toString();
		
		regionCBM.removeAllElements();	//Clear the combo box selection list
		regionCBM.addElement("Any");
		
		for(String s: regions)	//Add new list elements
				regionCBM.addElement(s);
			
		//Reselect the prior region, if it still exists
		regionCB.setSelectedItem(currSel);
		
		regionCB.addActionListener(this);
	}

	void updateUserList()
	{	
		UserDB userDB = UserDB.getInstance();

		bIgnoreCBEvents = true;
		changedByCB.setEnabled(false);
		String curr_sel = changedByCB.getSelectedItem().toString();
		int selIndex = 0;
		
		changedByCBM.removeAllElements();
		
		changedByCBM.addElement("Anyone");

		int index = 0;
		for(ONCUser user:userDB.getUserList())
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	void setSortStartDate(Date sd) {sortStartCal.setTime(sd); ds.setDate(sortStartCal.getTime());}
	
	void setSortEndDate(Date ed) {sortEndCal.setTime(ed); sortEndCal.add(Calendar.DATE, 1); de.setDate(sortEndCal.getTime());}
	
		@Override
	public void actionPerformed(ActionEvent e) 
	{
			if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
			{
				sortONCNum = oncnumTF.getText();
				buildTableList(false);
			}
			else if(e.getSource() == typeCB && typeCB.getSelectedItem() != sortType)
			{						
				sortType = (MealType) typeCB.getSelectedItem();
				buildTableList(false);
			}
			else if(e.getSource() == statusCB && statusCB.getSelectedItem() != sortStatus )
			{						
				sortStatus = (MealStatus) statusCB.getSelectedItem();
				buildTableList(false);
			}
			else if(e.getSource() == regionCB && regionCB.getSelectedIndex() != sortRegion)
			{
				sortRegion = regionCB.getSelectedIndex();
				buildTableList(false);
			}
			else if(e.getSource() == assignCB && !bIgnoreCBEvents && 
					((Organization)assignCB.getSelectedItem()).getID() != sortAssigneeID )
			{						
				sortAssigneeID = ((Organization)assignCB.getSelectedItem()).getID();
				buildTableList(false);
			}
			else if(e.getSource() == changedByCB && !bIgnoreCBEvents &&
					changedByCB.getSelectedIndex() != sortChangedBy && !bIgnoreCBEvents)
			{						
				sortChangedBy = changedByCB.getSelectedIndex();
				buildTableList(false);
			}
			else if(e.getSource() == printCB)
			{
/*				
				if(printCB.getSelectedIndex() == 1)	//Can always print listing
				{
					onPrintListing("ONC Wishes");
				} 
				else	//Can only print if table rows are selected
				{
					if(sortTable.getSelectedRowCount() > 0)
					{
						if(printCB.getSelectedIndex() == 2)
							onPrintLabels();
						else if(printCB.getSelectedIndex() == 3)
							onPrintReceivingCheckSheets();
					}
					else	//Warn user
					{
						JOptionPane.showMessageDialog(this, 
	    					"No wishes selected, please selected wishes in order to" + 
	    					printCB.getSelectedItem().toString(), 
	    					"No Wishes Selected", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
						printCB.setSelectedIndex(0);
					}
				}
*/				
			}
			else if(e.getSource() == btnExport)
			{
//				onExportRequested();	
			}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		System.out.println(String.format("SortMealsDlg.dataChanged: type: %s", dbe.getType()));
		if(dbe.getSource() != this && ( dbe.getType().equals("ADDED_MEAL") ||
			dbe.getType().equals("UPDATED_MEAL") || dbe.getType().equals("DELETED_MEAL")))

		{
			buildTableList(true);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CONFIRMED_PARTNER") ||
					dbe.getType().equals("DELETED_CONFIRMED_PARTNER") ||
					dbe.getType().equals("UPDATED_CONFIRMED_PARTNER") ||
					dbe.getType().equals("LOADED_PARTNERS")))
		{
			updateMealAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			updateMealAssigneeSelectionList();
			buildTableList(true);
		}
		else if(dbe.getType().contains("_USER"))
		{
			updateUserList();
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject();
			updateRegionList(regList);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent pce)
	{
		//If the date has changed in either date chooser, then rebuild the sort table. Note, setting
		//the date using setDate() does not trigger a property change, only triggered by user action. 
		//So must rebuild the table each time a change is detected. 
		if("date".equals(pce.getPropertyName()) &&
				(!sortStartCal.getTime().equals(ds.getDate()) || !sortEndCal.getTime().equals(de.getDate())))
		{
			System.out.println("SortMealsDlg.PropertyChangedEvent Detected");
			sortStartCal.setTime(ds.getDate());
			sortEndCal.setTime(de.getDate());
			buildTableList(false);
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	int sortTableList(int col) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	void setEnabledControls(boolean tf) 
	{
		btnExport.setEnabled(tf);
	}

	@Override
	Object[] getTableRow(ONCObject o) 
	{
		SortMealObject smo = (SortMealObject) o;
		
		Organization partner = orgs.getOrganizationByID(smo.getMeal().getPartnerID());
		String partnerName = partner != null ? partner.getName() : "None";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(smo.getMeal().getDateChanged().getTime());
		String[] tablerow = {smo.getFamily().getONCNum(),
							smo.getFamily().getHOHLastName(),
							regions.getRegionID(smo.getFamily().getRegion()),
							smo.getMeal().getType().toString(),
							smo.getFamily().getMealStatus().toString(),
							partnerName, smo.getMeal().getChangedBy(), ds};
		return tablerow;
	}

	@Override
	void checkApplyChangesEnabled() 
	{
		// TODO Auto-generated method stub

	}

	@Override
	boolean onApplyChanges() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void onResetCriteriaClicked() 
	{
		oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
		sortONCNum = "";
		
		typeCB.removeActionListener(this);
		typeCB.setSelectedIndex(0);	//no need to test for a change here.
		sortType = MealType.Any;
		typeCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		statusCB.removeActionListener(this);
		statusCB.setSelectedIndex(0);
		sortStatus = MealStatus.Any;
		statusCB.addActionListener(this);
		
		regionCB.removeActionListener(this);
		regionCB.setSelectedIndex(0);
		sortRegion = 0;
		regionCB.addActionListener(this);
		
		assignCB.removeActionListener(this);
		assignCB.setSelectedIndex(0);
		sortAssigneeID = 0;
		assignCB.addActionListener(this);
		
		changeAssigneeCB.removeActionListener(this);
		changeAssigneeCB.setSelectedIndex(0);
		changeAssigneeCB.addActionListener(this);
		
		//Check to see if date sort criteria has changed. Since the setDate() method
		//will not trigger an event, must check for a sort criteria date change here
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(!sdf.format(sortStartCal.getTime()).equals(sdf.format(gvs.getSeasonStartDate())))
		{
			sortStartCal.setTime(gvs.getSeasonStartDate());
			ds.setDate(sortStartCal.getTime());	//Will not trigger the event handler
		}
		
		if(!sdf.format(sortEndCal.getTime()).equals(sdf.format(gvs.getTomorrowsDate())))
		{
			sortEndCal.setTime(gvs.getTomorrowsDate());
			de.setDate(sortEndCal.getTime());	//Will not trigger the event handler
		}
			
		buildTableList(false);

	}

	@Override
	boolean isONCNumContainerEmpty() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	private class SortMealObject extends ONCObject
	{
		private ONCFamily	soFamily;
		private ONCMeal	 	soMeal;
		
		ONCOrgs partnerDB;
		
		public SortMealObject(int itemID, ONCFamily fam, ONCMeal meal) 
		{
			super(itemID);
			soFamily = fam;
			soMeal = meal;
			
			partnerDB = ONCOrgs.getInstance();
		}
		
		//getters
		ONCFamily getFamily() { return soFamily; }
		ONCMeal getMeal() { return soMeal; }
		
		public String[] getExportRow()
		{
/* ONCE WE"VE AGREED ON EXPORT FORMAT WITH WFCM, CODE TO PRODUCE A ROW OF THE EXPORT GOES HERE			
			SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
			String dateChanged = dob.format(soChildWish.getChildWishDateChanged().getTime());
			
			String wishName = wish == null ? "None" : wish.getName();
			
			String[] exportRow = {soFamily.getONCNum(), soChild.getChildGender(),
									soChild.getChildAge(),
									soChild.getChildDOBString("MM-dd-yyyy"), 
									indicator[soChildWish.getChildWishIndicator()],
									wishName,
									soChildWish.getChildWishDetail(),
									soChildWish.getChildWishStatus().toString(),
									partnerDB.getOrganizationByID(soChildWish.getChildWishAssigneeID()).getName(),
									soChildWish.getChildWishChangedBy(),
									dateChanged};
			return exportRow;
*/
			return null;
		}
	}
}
