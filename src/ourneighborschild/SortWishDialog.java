package ourneighborschild;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

import com.toedter.calendar.JDateChooser;

public class SortWishDialog extends ChangeDialog implements PropertyChangeListener 												
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_AGE_LIMIT = 21;
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;
	private static final int RS_ITEMS_PER_PAGE = 20;
	private static final Integer MAXIMUM_ON_NUMBER = 9999;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private PartnerDB partnerDB;
	private ONCWishCatalog wishCat;
	private ONCRegions regions;

	private ArrayList<SortWishObject> stAL;
	
	private JComboBox startAgeCB, endAgeCB, genderCB, wishnumCB, wishCB, resCB, assignCB, statusCB, changedByCB;
	private JComboBox changeResCB, changeStatusCB, changeAssigneeCB, printCB, regionCB, schoolCB;
	private DefaultComboBoxModel wishCBM, assignCBM, changeAssigneeCBM, changedByCBM, regionCBM, schoolCBM;
	private JTextField oncnumTF;
	private JButton btnExport;
	private JDateChooser ds, de;
	private Calendar sortStartCal = null, sortEndCal = null;
	private JCheckBox labelFitCxBox;
	
	private int sortStartAge = 0, sortEndAge = ONC_AGE_LIMIT, sortGender = 0, sortChangedBy = 0;
	private int sortWishNum = 0, sortRes = 0, sortAssigneeID, sortRegion = 0;
	private WishStatus sortStatus = WishStatus.Any;
	private String sortSchool = "Any";
	private int sortWishID = -2;
	private boolean bOversizeWishes = false;
	
	private static String[] genders = {"Any", "Boy", "Girl"};
	private static String[] res = {"Any", "Blank", "*", "#"};
	private static String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
										"Distributed", "Verified"};
	
	SortWishDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Wish Management");
		
		//set up the data base references
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		partnerDB = PartnerDB.getInstance();
		wishCat = ONCWishCatalog.getInstance();
		regions = ONCRegions.getInstance();
		
		//set up data base listeners
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(cwDB != null)
			cwDB.addDatabaseListener(this);
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		if(wishCat != null)
			wishCat.addDatabaseListener(this);
		if(regions != null)
			regions.addDatabaseListener(this);
		
		//initialize member variables
		stAL = new ArrayList<SortWishObject>();

		//Set up the search criteria panel
		oncnumTF = new JTextField(4);
    	oncnumTF.setEditable(true);
    	oncnumTF.setMaximumSize(new Dimension(64,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
		
		regionCBM = new DefaultComboBoxModel();
		regionCBM.addElement("Any");
		regionCB = new JComboBox();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.setPreferredSize(new Dimension(64,56));
		regionCB.addActionListener(this);
    	
		String[] ages = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};

		startAgeCB = new JComboBox(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Start Age"));
		startAgeCB.setPreferredSize(new Dimension(64,56));
		startAgeCB.addActionListener(this);
		
		endAgeCB = new JComboBox(ages);
		endAgeCB.setBorder(BorderFactory.createTitledBorder("End Age"));
		endAgeCB.setPreferredSize(new Dimension(64,56));
		endAgeCB.setSelectedIndex(ages.length-1);
		endAgeCB.addActionListener(this);
		
		genderCB = new JComboBox(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setSize(new Dimension(72,56));
		genderCB.addActionListener(this);
		
		String[] wishnums = {"Any", "1", "2", "3"};
		wishnumCB = new JComboBox(wishnums);
		wishnumCB.setBorder(BorderFactory.createTitledBorder("Wish #"));
		startAgeCB.setPreferredSize(new Dimension(72,56));
		wishnumCB.addActionListener(this);
		
		schoolCBM = new DefaultComboBoxModel();
	    schoolCBM.addElement("Any");
		schoolCB = new JComboBox();		
        schoolCB.setModel(schoolCBM);
		schoolCB.setPreferredSize(new Dimension(180, 56));
		schoolCB.setBorder(BorderFactory.createTitledBorder("School Attended"));
		schoolCB.addActionListener(this);
		
		wishCBM = new DefaultComboBoxModel();
	    wishCBM.addElement(new ONCWish(-2, "Any", 7));
		wishCB = new JComboBox();		
        wishCB.setModel(wishCBM);
		wishCB.setPreferredSize(new Dimension(180, 56));
		wishCB.setBorder(BorderFactory.createTitledBorder("Wish Type"));
		wishCB.addActionListener(this);
		
		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
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
		
		resCB = new JComboBox(res);
		resCB.setPreferredSize(new Dimension(104, 56));
		resCB.setBorder(BorderFactory.createTitledBorder("Restrictions"));
		resCB.addActionListener(this);
		res[0] = "No Change";	//Change "Any" to none after sort criteria list created
		
		statusCB = new JComboBox(WishStatus.getSearchFilterList());
		statusCB.setPreferredSize(new Dimension(136, 56));
		statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
		statusCB.addActionListener(this);
		status[0] = "No Change"; //Change "Any" to none after sort criteria list created
		
		assignCB = new JComboBox();
		assignCBM = new DefaultComboBoxModel();
		
		//take advantage of the fact that partner id's are 7 digits and start with the calendar year
	    assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
	    assignCBM.addElement(new ONCPartner(-1, "Unassigned", "Unassigned"));
	    assignCB.setModel(assignCBM);
		assignCB.setPreferredSize(new Dimension(192, 56));
		assignCB.setBorder(BorderFactory.createTitledBorder("Assigned To"));
		
		//set the user preference for assignee filter default setting
		assignCB.setSelectedIndex(1);
		sortAssigneeID = -1;
		assignCB.addActionListener(this);

		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(regionCB);
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(endAgeCB);
		sortCriteriaPanelTop.add(genderCB);
		sortCriteriaPanelTop.add(schoolCB);
		sortCriteriaPanelTop.add(wishnumCB);
		sortCriteriaPanelBottom.add(wishCB);
		sortCriteriaPanelBottom.add(resCB);
		sortCriteriaPanelBottom.add(statusCB);
		sortCriteriaPanelBottom.add(assignCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(ds);
		sortCriteriaPanelBottom.add(de);
		
		//set up the change panel, which consists of the item count panel and the 
		//change data panel
		itemCountPanel.setBorder(BorderFactory.createTitledBorder("Wishes Meeting Criteria"));
		
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Wish Restrictions/Status/Assignee"));
        
		changeResCB = new JComboBox(res);
        changeResCB.setPreferredSize(new Dimension(192, 56));
		changeResCB.setBorder(BorderFactory.createTitledBorder("Change Restrictions To:"));
		changeResCB.addActionListener(this);
        
        changeStatusCB = new JComboBox(WishStatus.getChangeList());
        changeStatusCB.setPreferredSize(new Dimension(192, 56));
		changeStatusCB.setBorder(BorderFactory.createTitledBorder("Change Status To:"));
		changeStatusCB.addActionListener(this);
        
        changeAssigneeCB = new JComboBox();
        changeAssigneeCBM = new DefaultComboBoxModel();
	    changeAssigneeCBM.addElement(new ONCPartner(0, "No Change", "No Change"));
	    changeAssigneeCBM.addElement(new ONCPartner(-1, "None", "None"));
        changeAssigneeCB.setModel(changeAssigneeCBM);
        changeAssigneeCB.setPreferredSize(new Dimension(192, 56));
		changeAssigneeCB.setBorder(BorderFactory.createTitledBorder("Change Assignee To:"));
		changeAssigneeCB.addActionListener(this);
		
		changeDataPanel.add(changeResCB);
		changeDataPanel.add(changeStatusCB);
		changeDataPanel.add(changeAssigneeCB);
		
		gbc.gridx = 1;
	    gbc.ipadx = 0;
	    gbc.weightx = 1.0;
	    changePanel.add(changeDataPanel, gbc);

	    //set up the dialog defined control panel to add to the bottom panel
	    JPanel infoPanel = new JPanel();
	    labelFitCxBox = new JCheckBox("Over Length Wishes Only");
	    labelFitCxBox.setSelected(bOversizeWishes);
	    labelFitCxBox.addActionListener(this);
	    infoPanel.add(labelFitCxBox);
	    
	    JPanel cntlPanel = new JPanel();
	    cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btnExport = new JButton("Export Data");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing", "Print Labels", "Print Partner Receiving Check Sheets"};
        printCB = new JComboBox(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        printCB.addActionListener(this);
   
        cntlPanel.add(btnExport);
      	cntlPanel.add(printCB);
      	
      	bottomPanel.add(infoPanel, BorderLayout.LINE_START);
    	bottomPanel.add(cntlPanel, BorderLayout.CENTER);
 
        //add the bottom two panels to the dialog and pack
        this.add(changePanel);
        this.add(bottomPanel);
        
        //calculate width
        int dlgWidth = 24; //account for vertical scroll bar
        int[] colWidths = getColumnWidths();
        for(int i=0; i< colWidths.length; i++)
        	dlgWidth += colWidths[i];
        this.setMinimumSize(new Dimension(dlgWidth, 560));
//      pack();
	}
	
	@Override
	int sortTableList(int col)
	{	
		archiveTableSelections(stAL);
		
		if(col == 0)	//Sort on ONC Family Number
    		Collections.sort(stAL, new ONCSortItemFamNumComparator());
		else if(col == 1)	// Sort on Child's Age
    		Collections.sort(stAL, new ONCSortItemFamilyRegionComparator());
    	else if(col == 2)	// Sort on Child's Age
    		Collections.sort(stAL, new ONCSortItemAgeComparator());
    	else if(col == 3)	//Sort on Child's Gender
    		Collections.sort(stAL, new ONCSortItemGenderComparator());
    	else if(col == 4)	//Sort on Child's School
    		Collections.sort(stAL, new ONCSortItemSchoolComparator());
    	else if(col == 5)	//Sort on Child's Wish #
    		Collections.sort(stAL, new ONCSortItemWishNumComparator());
    	else if(col == 6)	//Sort on Child's Base Wish
    		Collections.sort(stAL, new ONCSortItemWishBaseComparator());
    	else if(col == 7)	//Sort on Child's Wish Detail
    		Collections.sort(stAL, new ONCSortItemWishDetailComparator());
    	else if(col == 8)	//Sort on Child's Wish Indicator
    		Collections.sort(stAL, new ONCSortItemWishIndicatorComparator());
    	else if(col == 9)	//Sort on Child's Wish Status
    		Collections.sort(stAL, new ONCSortItemWishStatusComparator());
    	else if(col == 10)	//Sort on Child's Wish Assignee
    		Collections.sort(stAL, new ONCSortItemWishAssigneeComparator());
    	else if(col ==  11)	//Sort on Child's Wish Changed By
    		Collections.sort(stAL, new ONCSortItemWishChangedByComparator());
    	else if(col == 12)	//Sort on Child's Wish Date Changed
    		Collections.sort(stAL, new ONCSortItemWishDateChangedComparator());
    	else
    		col = -1;
		
		if(col > -1)
			displaySortTable(stAL, false, tableRowSelectedObjectList);
    		
    	return col;
	}

	@Override
	void setEnabledControls(boolean tf)
	{
		btnExport.setEnabled(tf);
	}
	
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
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()) && doesRegionMatch(f.getRegion()))	//Must be a valid family	
			{
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c) && doesSchoolMatch(c))	//Children criteria pass
					{
						for(int i=0; i< cwDB.getNumberOfWishesPerChild(); i++)
						{	 //Assignee, Status, Date & Wish match
							ONCChildWish cw = cwDB.getWish(c.getChildWishID(i));
							
							if(cw != null && doesResMatch(cw.getChildWishIndicator()) &&
								doesAssigneeMatch(cw.getChildWishAssigneeID()) &&
								 doesStatusMatch(cw.getChildWishStatus()) &&
								  isWishChangeDateBetween(cw.getChildWishDateChanged()) &&
								   doesChangedByMatch(cw.getChildWishChangedBy()) &&
									doesWishBaseMatch(cw.getWishID()) &&
									 doesWishNumMatch(i)  &&
									  !(bOversizeWishes && !isWishOversize(cw)))//Wish criteria pass
							{
								
								stAL.add(new SortWishObject(itemID++, f, c, cw));
							}
						}
					}
				}
			}
		}
		
		updateSchoolFilterList();
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table list is built	
	}

	boolean onApplyChanges()
	{
//		bChangingTable = true;
		boolean bRebuildTable = false; //set true if a wish is changed so table is only rebuilt once per applyWishChange
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			boolean bNewWishRqrd = false; 	//Restriction, status or assignee change
			
			//Find child and wish number for selected
			ONCChild c = stAL.get(row_sel[i]).getChild();
			int wn = stAL.get(row_sel[i]).getChildWish().getWishNumber();
			ONCChildWish cw = stAL.get(row_sel[i]).getChildWish();

			//Get current wish information
			int cwWishID = cw.getWishID();
			String cwd = cw.getChildWishDetail();
			int cwi = cw.getChildWishIndicator();
			WishStatus cws = cw.getChildWishStatus();
			ONCPartner partner = null;
			if(cw.getChildWishAssigneeID() > -1)
				partner = partnerDB.getPartnerByID(cw.getChildWishAssigneeID());
			
			//Determine if a change to wish restrictions, if so set new wish restriction for request
			if(changeResCB.getSelectedIndex() > 0 && cwi != changeResCB.getSelectedIndex()-1)
			{
				//a change to the indicator is requested. Can only change wish restrictions
				//in certain WishStatus
				if(cws == WishStatus.Selected || cws == WishStatus.Assigned
						||cws == WishStatus.Shopping || cws == WishStatus.Returned)
				{
					cwi = changeResCB.getSelectedIndex()-1;	//Restrictions start at 0
					bNewWishRqrd = true;
				}
			}
			
			//Determine if a change to wish assignee, if so, set new wish assignee in request
			if(changeAssigneeCB.getSelectedIndex() > 0 &&
					cw.getChildWishAssigneeID() != ((ONCPartner)changeAssigneeCB.getSelectedItem()).getID())
			{
				//can only change wish assignees in certain WishState's
				if(cws == WishStatus.Selected || cws == WishStatus.Assigned || cws == WishStatus.Delivered ||
					cws == WishStatus.Shopping || cws == WishStatus.Returned || cws == WishStatus.Missing)
				{
					partner = ((ONCPartner)changeAssigneeCB.getSelectedItem());
					bNewWishRqrd = true;
				}
			}
			
			//Determine if a change to wish status, if so, set new wish status in request
			if(changeStatusCB.getSelectedIndex() > 0 && cws != changeStatusCB.getSelectedItem())
			{
				//user has requested to change the wish status
				WishStatus reqStatus = (WishStatus) changeStatusCB.getSelectedItem();
				WishStatus newStatus = cwDB.checkForStatusChange(cw, cw.getWishID(), reqStatus, partner);
				
				if(newStatus != cw.getChildWishStatus())
				{
					cws = reqStatus;
					bNewWishRqrd = true;
				}
			}
			
			if(bNewWishRqrd)	//Restriction, Status or Partner change detected
			{
				//Add the new wish to the child wish history, returns -1 if no wish created
				ONCChildWish addedWish = cwDB.add(this, c.getID(), cwWishID, cwd, wn, cwi, cws, partner);
				
				if(addedWish != null)	//only proceed if wish was accepted by the data base
					bRebuildTable = true;	//set flag to rebuild/display the table array/wish table
			}
		}
		
		if(bRebuildTable)
			buildTableList(false);
		
		//Reset the change combo boxes to "No Change"
		changeResCB.setSelectedIndex(0);
		changeStatusCB.setSelectedIndex(0);
		changeAssigneeCB.setSelectedIndex(0);
		
		btnApplyChanges.setEnabled(false);
		
//		bChangingTable = false;
		
		return bRebuildTable;
	}
	
	void updateWishSelectionList()
	{
		//disable the combo box
		bIgnoreCBEvents = true;
		wishCB.setEnabled(false);
		
		//get the current selection
		int currSelID = ((ONCWish) wishCB.getSelectedItem()).getID();
		
		//reset the selection in case the current selection is removed by the update
		wishCB.setSelectedIndex(0);
		sortWishID = -2;
		
		//Clear the combo box of current elements
		wishCBM.removeAllElements();	
	
		List<ONCWish> wishList = wishCat.getWishList(WishListPurpose.Filtering);
		for(ONCWish w: wishList )	//Add new list elements
			wishCBM.addElement(w);
		
		//reselect the prior selection, if it's still in the list
		int index = 0;
		while(index < wishList.size() && wishList.get(index).getID() != currSelID)
			index++;
		
		if(index < wishList.size())
		{
			wishCB.setSelectedItem(wishList.get(index));
			sortWishID = wishList.get(index).getID();
		}
		
		//re-enable the combo box
		wishCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	/*********************************************************************************************
	 * This method updates the drop down selection list associated with the Wish Assignee combo
	 * boxes used to search for wish assignees and to select assignees. The lists must be updated
	 * every time a partner is confirmed or unconfirmed. 
	 * 
	 * The method must keep the current assignee selected in the combo boxes even if their position 
	 * changes. 
	 *********************************************************************************************/
	void updateWishAssigneeSelectionList()
	{
		bIgnoreCBEvents = true;
		int currentAssigneeID = -1, currentChangeAssigneeID = -1;	//set to null for reselection
		int currentAssigneeIndex = assignCB.getSelectedIndex();
		int currentChangeAssigneeIndex = changeAssigneeCB.getSelectedIndex();
		
		if(assignCB.getSelectedIndex() > 1)	//leaves the current selection null if no selection made
			currentAssigneeID = ((ONCPartner)assignCB.getSelectedItem()).getID();
		
		if(changeAssigneeCB.getSelectedIndex() > 1)
			currentChangeAssigneeID = ((ONCPartner)changeAssigneeCB.getSelectedItem()).getID();
		
		assignCB.setSelectedIndex(0);
		sortAssigneeID = 0;
		changeAssigneeCB.setSelectedIndex(0);
		
		assignCBM.removeAllElements();
		changeAssigneeCBM.removeAllElements();
		
		//take advantage of the fact that partner id's are 7 digits and start with the calendar year
		assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
		assignCBM.addElement(new ONCPartner(-1, "Unassigned", "Unassigned"));
		changeAssigneeCBM.addElement(new ONCPartner(-1, "No Change", "No Change"));
		changeAssigneeCBM.addElement(new ONCPartner(-1, "None", "None"));
		
		for(ONCPartner confirmedPartner :partnerDB.getConfirmedPartnerList(GiftCollection.Ornament))
		{
			assignCBM.addElement(confirmedPartner);
			changeAssigneeCBM.addElement(confirmedPartner);
		}
		
		//Attempt to reselect the previous selected organization, if one was selected. 
		//If the previous selection is no longer in the drop down, the top of the list is 
		//already selected 
		if(currentAssigneeIndex == 1)	//Organization == "No one", ID = -1
		{
			assignCB.setSelectedIndex(1);
			sortAssigneeID = -1;
		}
		else if(currentAssigneeIndex > 1)
		{
			ONCPartner assigneeOrg = partnerDB.getPartnerByID(currentAssigneeID);
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
			ONCPartner changeAssigneeOrg = partnerDB.getPartnerByID(currentChangeAssigneeID);
			if(changeAssigneeOrg != null)
				changeAssigneeCB.setSelectedItem(changeAssigneeOrg);
		}
		
		bIgnoreCBEvents = false;
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
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser user : userList)
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
	
	void updateUserPreferences(ONCUser u)
	{
		//if there was a change to user preferences and the assignee filter is set to "Any" or
		//"Unassigned" and the new preference is different, update the filter selection and rebuild
		//the table
		if(assignCB.getSelectedIndex() < 2 &&
			assignCB.getSelectedIndex() != u.getPreferences().getWishAssigneeFilter())
		{
			assignCB.removeActionListener(this);
			assignCB.setSelectedIndex(u.getPreferences().getWishAssigneeFilter());
			sortAssigneeID = assignCB.getSelectedIndex() == 0 ? 0 : -1;
			assignCB.addActionListener(this);
			
			buildTableList(false);
		}
	}
	
	void onPrintReceivingCheckSheets()
	{
		if(sortTable.getSelectedRowCount() > 0)	//Only print selected rows
		{
			//build the array list to print. For each row selected by the user, get the family object 
			//reference and build the packaging sheet array list. The packaging sheet array list is
			//used by the print method to print each page. A family packaging sheet may have two pages
			//if the family has more than 5 children
			ArrayList<ONCReceivingSheet> rsAL = new ArrayList<ONCReceivingSheet>();
			
			int totalpages = sortTable.getSelectedRowCount() / RS_ITEMS_PER_PAGE + 1;
//			int[] row_sel = sortTable.getSelectedRows();
			
			for(int i=0; i < totalpages; i++)
			{
				int countonpage = sortTable.getSelectedRowCount() - i*RS_ITEMS_PER_PAGE;
				int count = countonpage > RS_ITEMS_PER_PAGE ? RS_ITEMS_PER_PAGE : countonpage;
				rsAL.add(new ONCReceivingSheet(((ONCPartner) assignCB.getSelectedItem()).getName(), 
											   i*RS_ITEMS_PER_PAGE, count, i+1, totalpages));
			}
			
			//Create the info required for the print job
			SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(twodigitYear.format(gvs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
			String oncSeason = "ONC " + Integer.toString(GlobalVariables.getCurrentSeason());			
			
			//Create the print job
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(new ReceivingGiftCheckSheetPrinter(rsAL, img, oncSeason));
				
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	void onPrintListing(String tablename)
	{
		if(sortTable.getRowCount() > 0)
		{
			try
			{
				MessageFormat headerFormat = new MessageFormat(tablename);
				MessageFormat footerFormat = new MessageFormat("- {0} -");
				sortTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
			} 
			catch (PrinterException e) 
			{
				JOptionPane.showMessageDialog(this, 
						"Print Error: " + e.getMessage(), 
						"Print Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				e.printStackTrace();
			}
		}
		else	//Warn user that the table is empty
			JOptionPane.showMessageDialog(this, 
					"No wishes in table to print, table must contain wishes in order to" + 
					"print listing", 
					"No Wishes To Print", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
		
	void checkApplyChangesEnabled()
	{
		if(sortTable.getSelectedRows().length > 0 &&
				(changeResCB.getSelectedIndex() > 0 || changeStatusCB.getSelectedIndex() > 0 ||changeAssigneeCB.getSelectedIndex() > 0))	
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}
	
	void checkExportEnabled()
	{
		if(sortTable.getSelectedRowCount() > 0)
			btnExport.setEnabled(true);
		else
			btnExport.setEnabled(false);
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"ONC #", "Gender", "Age on 12/25", "DoB", "Res", "Wish", "Detail", "Status", 
    						"Assignee", "Changed By", "Date Changed"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       										new FileNameExtensionFilter("CSV Files", "csv"), 1);
       	if(oncwritefile!= null)
       	{
       		//If user types a new filename without extension.csv, add it
	    	String filePath = oncwritefile.getPath();
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    int[] row_sel = sortTable.getSelectedRows();
	    	    for(int i=0; i<sortTable.getSelectedRowCount(); i++)
	    	    {
	    	    	int index = row_sel[i];
	    	    	writer.writeNext(stAL.get(index).getExportRow());
	    	    }
	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(this, 
						sortTable.getSelectedRowCount() + " wishes sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}

	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	private boolean isAgeInRange(ONCChild c)
	{
		return c.getChildIntegerAge() >= startAgeCB.getSelectedIndex() && c.getChildIntegerAge() <= endAgeCB.getSelectedIndex();		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	private boolean doesSchoolMatch(ONCChild c)
	{
		return sortSchool.equals("Any") || (c.getChildSchool().equalsIgnoreCase(sortSchool));		
	}
	
	private boolean doesWishNumMatch(int wn)
	{
		return sortWishNum == 0 || sortWishNum == wn+1;		
	}
	
	private boolean doesResMatch(int res) { return sortRes == 0  || sortRes == res+1; }
	
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	private boolean doesStatusMatch(WishStatus ws){return sortStatus == WishStatus.Any || sortStatus.compareTo(ws) == 0;}
	
	private boolean doesAssigneeMatch(int assigneeID)
	{
		//sortAssigneeID's 0 and -1 are reserved for special filters. 0 shows all wishes regardless off assignee,
		//-1 displays wishes that are not assigned
		return sortAssigneeID == 0 || sortAssigneeID == assigneeID;
	}
	
	private boolean isWishChangeDateBetween(Calendar wcd)
	{
		return !wcd.getTime().after(sortEndCal.getTime()) && !wcd.getTime().before(sortStartCal.getTime());
	}
			
	private boolean doesWishBaseMatch(int wishID)	{return sortWishID == -2 ||  sortWishID == wishID; }
	
	private boolean doesChangedByMatch(String s) { return sortChangedBy == 0 || changedByCB.getSelectedItem().toString().equals(s); }
	
	/*************
	 * Method checks to see if a wish will fit on a label. A label has two lines to describe
	 * the wish. If the wish name and detail fit on one line, it's ok. If it requires two
	 * lines, need to check the 2nd line to see if it fits. If the second line is too long
	 * the method returns true. Otherwise it returns false.
	 * @param cw - ONCChildWish to check if it fits on a label
	 * @return - true if wish is too big for label, false it it fits on label
	 ******************/
	boolean isWishOversize(ONCChildWish cw)
	{
		ONCWish oncwish;
		boolean bOversizeWish = false;	//only get to true if 2nd line doesn't fit
		
		if(cw != null && bOversizeWishes && (oncwish=wishCat.getWishByID(cw.getWishID())) != null)
		{
			//construct how wish will appear on label
			String wishOnLabel = oncwish.getName() + " - " + cw.getChildWishDetail();
			
			//test the length to see if it needs two lines, if only one line, labels OK
			if(wishOnLabel.length() > MAX_LABEL_LINE_LENGTH)
			{
				//else, split into two lines and check 2nd line
				int index = MAX_LABEL_LINE_LENGTH;
				while(index > 0 && wishOnLabel.charAt(index) != ' ')	//find the line break
					index--;
				
				//does 2nd line fit on label?
				bOversizeWish = wishOnLabel.substring(index).length() > MAX_LABEL_LINE_LENGTH;
			}
		}
		
		return bOversizeWish;
	}
	
	void setSortStartDate(Date sd) {sortStartCal.setTime(sd); ds.setDate(sortStartCal.getTime());}
	
	void setSortEndDate(Date ed) {sortEndCal.setTime(ed); sortEndCal.add(Calendar.DATE, 1); de.setDate(sortEndCal.getTime());}
	
	void updateSchoolFilterList()
	{
		bIgnoreCBEvents = true;
		
		//archive the current selection. 
		String currSchool = (String) schoolCB.getSelectedItem();
		
		schoolCBM.removeAllElements();
		schoolCBM.addElement("Any");
		
		//create a list of schools that we will sort afterwards and before populating the
		//combo box model
		List<String> schoolList = new ArrayList<String>();
			
		//iterate thru the list of children and build the school list. Prevent duplicates
		for(ONCChild c : cDB.getList())
		{
			int index = 0;
			while(index < schoolList.size() && !c.getChildSchool().isEmpty() && 
					!c.getChildSchool().equalsIgnoreCase(schoolList.get(index)))
				index++;
		
			if(index == schoolList.size())
				schoolList.add(c.getChildSchool());
		}
		
		//sort the list of schools alphabetically
		Collections.sort(schoolList);
		
		//copy the list to the combo box model
		for(String school : schoolList)
			schoolCBM.addElement(school);
		
		//set the selected index to "Any" before trying to reselect the currently selected school.
		//in case the currently selected school was removed by a deleted child action. The JCombobox
		//will not change the selection if the item is not in the model and the combo box is not 
		//editable
		schoolCB.setSelectedIndex(0);
		schoolCB.setSelectedItem(currSchool);
		sortSchool = (String) schoolCB.getSelectedItem();
		
		bIgnoreCBEvents = false;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList(false);
		}
		else if(e.getSource() == startAgeCB && startAgeCB.getSelectedIndex() != sortStartAge)
		{
			sortStartAge = startAgeCB.getSelectedIndex();
			buildTableList(false);
		}		
		else if(e.getSource() == endAgeCB && endAgeCB.getSelectedIndex() != sortEndAge)
		{
			sortEndAge = endAgeCB.getSelectedIndex();
			buildTableList(false);	
		}
		else if(e.getSource() == genderCB && genderCB.getSelectedIndex() != sortGender)
		{
			sortGender = genderCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == schoolCB)
		{
			if(schoolCBM.getSize() > 0 && sortSchool != null && !schoolCB.getSelectedItem().equals(sortSchool))
			{
				sortSchool = (String) schoolCB.getSelectedItem();
				buildTableList(false);
			}
		}
		else if(e.getSource() == wishnumCB && wishnumCB.getSelectedIndex() != sortWishNum)
		{
			sortWishNum = wishnumCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == wishCB && !bIgnoreCBEvents && 
				((ONCWish)wishCB.getSelectedItem()).getID() != sortWishID)
		{						
			sortWishID = ((ONCWish)wishCB.getSelectedItem()).getID();
			buildTableList(false);
		}
		else if(e.getSource() == changedByCB && changedByCB.getSelectedIndex() != sortChangedBy && !bIgnoreCBEvents)
		{						
			sortChangedBy = changedByCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == resCB && resCB.getSelectedIndex() != sortRes )
		{						
			sortRes = resCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == regionCB && regionCB.getSelectedIndex() != sortRegion)
		{
			sortRegion = regionCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == statusCB && statusCB.getSelectedItem() != sortStatus )
		{						
			sortStatus = (WishStatus) statusCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == labelFitCxBox )
		{
			bOversizeWishes = labelFitCxBox.isSelected();
			buildTableList(false);
		}
		else if(e.getSource() == assignCB && !bIgnoreCBEvents && 
				((ONCPartner)assignCB.getSelectedItem()).getID() != sortAssigneeID )
		{						
			sortAssigneeID = ((ONCPartner)assignCB.getSelectedItem()).getID();
			buildTableList(false);
		}
		else if(e.getSource() == printCB)
		{
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
		}
		else if(e.getSource() == btnExport)
		{
			onExportRequested();	
		}
		else if(!bIgnoreCBEvents && (e.getSource() == changeResCB ||
					e.getSource() == changeStatusCB || e.getSource() == changeAssigneeCB))
		{
			checkApplyChangesEnabled();
		}
	}
	
	@Override
	String[] getColumnToolTips()
	{
		String[] colToolTips = {"ONC Family Number", "Child's Age", 
				"Child's Gender", "School Child Attends", "Wish Number - 1, 2 or 3", "Wish Assigned", "Wish Detail",
				"# - Selected by ONC or * - Don't asssign", "Wish Status", "Who is fulfilling?",
				"User who last changed wish", "Date & Time Wish Last Changed"};
		
		return colToolTips;
	}
	@Override
	String[] getColumnNames()
	{
		String[] columns = {"ONC", "Reg","Age", "Gend", "School", "Wish", "Wish Type", "Details", " Res ",
				"Status", "Assignee", "Changed By", "Time Stamp"};
		
		return columns;
	}
	
	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {40, 32, 48, 36, 96, 36, 84, 180, 32, 80, 160, 88, 92};
		return colWidths;
	}
	
	@Override
	int[] getCenteredColumns()
	{
		int[] center_cols = {1,5,8};
		return center_cols;
	}
	
	@Override
	void onResetCriteriaClicked()
	{
		oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
		sortONCNum = "";
		
		startAgeCB.removeActionListener(this);
		startAgeCB.setSelectedIndex(0);	//Will trigger the CB event handler which
		sortStartAge = 0;
		startAgeCB.addActionListener(this);
		
		endAgeCB.removeActionListener(this);
		endAgeCB.setSelectedIndex(ONC_AGE_LIMIT);	//will determine if the CB changed. Therefore,
		sortEndAge = ONC_AGE_LIMIT;
		endAgeCB.addActionListener(this);
		
		genderCB.removeActionListener(this);
		genderCB.setSelectedIndex(0);	//no need to test for a change here.
		sortGender = 0;
		genderCB.addActionListener(this);
		
		schoolCB.removeActionListener(this);
		schoolCB.setSelectedIndex(0);	//no need to test for a change here.
		sortSchool = "Any";
		schoolCB.addActionListener(this);
		
		wishnumCB.removeActionListener(this);
		wishnumCB.setSelectedIndex(0);
		sortWishNum = 0;
		wishnumCB.addActionListener(this);
		
		wishCB.removeActionListener(this);
		wishCB.setSelectedIndex(0);
		sortWishID = -2;
		wishCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		resCB.removeActionListener(this);
		resCB.setSelectedIndex(0);
		sortRes = 0;
		resCB.addActionListener(this);
		
		statusCB.removeActionListener(this);
		statusCB.setSelectedIndex(0);
		sortStatus = WishStatus.Any;
		statusCB.addActionListener(this);
		
		regionCB.removeActionListener(this);
		regionCB.setSelectedIndex(0);
		sortRegion = 0;
		statusCB.addActionListener(this);
		
		assignCB.removeActionListener(this);
		//set the user preference for assignee filter default setting
		UserPreferences uPrefs = userDB.getUserPreferences();
		assignCB.setSelectedIndex(uPrefs.getWishAssigneeFilter());
		sortAssigneeID = assignCB.getSelectedIndex() == 0 ? 0 : -1;
		assignCB.addActionListener(this);
		
		changeResCB.removeActionListener(this);
		changeResCB.setSelectedIndex(0);
		changeResCB.addActionListener(this);
		
		changeStatusCB.removeActionListener(this);
		changeStatusCB.setSelectedIndex(0);
		changeStatusCB.addActionListener(this);
		
		changeAssigneeCB.removeActionListener(this);
		changeAssigneeCB.setSelectedIndex(0);
		changeAssigneeCB.addActionListener(this);
		
		labelFitCxBox.removeActionListener(this);
		labelFitCxBox.setSelected(false);
		bOversizeWishes = false;
		labelFitCxBox.addActionListener(this);
		
		//Check to see if date sort criteria has changed. Since the setDate() method
		//will not trigger an event, must check for a sort criteria date change here
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(!sdf.format(sortStartCal.getTime()).equals(sdf.format(gvs.getSeasonStartDate())))
		{
			sortStartCal.setTime(gvs.getSeasonStartDate());
			ds.setDate(sortStartCal.getTime());	//Will not trigger the event handler
		}
		
		if(!sdf.format(sortEndCal.getTime()).equals(sdf.format(getTomorrowsDate())))
		{
			sortEndCal.setTime(getTomorrowsDate());
			de.setDate(sortEndCal.getTime());	//Will not trigger the event handler
		}
			
		buildTableList(false);
	}
	
	void onPrintLabels()
	{
		if(sortTable.getSelectedRowCount() > 0)	 //Print selected rows. If no rows selected, do nothing
		{
			AveryWishLabelPrinter awlp;
			PrinterJob pj = PrinterJob.getPrinterJob();
			
			if(sortTable.getSelectedRowCount() == 1)	//print 1 label, ask position
			{
				AveryLabelPrintPosDialog posDlg = new AveryLabelPrintPosDialog(this);
				Point labelPos = posDlg.showDialog(this);
				
				if(labelPos.x > 0 && labelPos.y > 0)
				{	
					//adjust from user coordinates (row: 1 - 30, col: 1-3) to print coordinates)
					labelPos.x--;
					labelPos.y--;
					
					//create the label printer and print
					awlp = new AveryWishLabelPrinter(stAL, sortTable, sortTable.getSelectedRowCount(), labelPos);
					pj.setPrintable(awlp);
		         
					boolean ok = pj.printDialog();
					if (ok)
					{
						try
						{
							pj.print();
						}
						catch (PrinterException ex)
						{
							/* The job did not successfully complete */
						}       
					}
				}
			}
			else
			{
				awlp = new AveryWishLabelPrinter(stAL, sortTable, sortTable.getSelectedRowCount(), new Point(0,0));
				pj.setPrintable(awlp);
	         
				boolean ok = pj.printDialog();
				if (ok)
				{
					try
					{
						pj.print();
					}
					catch (PrinterException ex)
					{
						/* The job did not successfully complete */
					}       
				}
			}
		}
		
        printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	@Override
	boolean isONCNumContainerEmpty() { return oncnumTF.getText().isEmpty(); }

	@Override
	public void propertyChange(PropertyChangeEvent pce)
	{
		//If the date has changed in either date chooser, then rebuild the sort table. Note, setting
		//the date using setDate() does not trigger a property change, only triggered by user action. 
		//So must rebuild the table each time a change is detected. 
		if("date".equals(pce.getPropertyName()) &&
				(!sortStartCal.getTime().equals(ds.getDate()) || !sortEndCal.getTime().equals(de.getDate())))
		{
			sortStartCal.setTime(ds.getDate());
			sortEndCal.setTime(de.getDate());
			buildTableList(false);
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel() &&
				sortTable.getSelectedRow() > -1 && !bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getChild();
			ONCChildWish cw = stAL.get(sortTable.getSelectedRow()).getChildWish();
			fireEntitySelected(this, EntityType.WISH, fam, child, cw);
			
			//determine if a partner has been assigned for the selected wish
			int childWishAssigneeID = cw.getChildWishAssigneeID();
			if(childWishAssigneeID > -1)
			{
				ONCPartner org = partnerDB.getPartnerByID(childWishAssigneeID);
				fireEntitySelected(this, EntityType.PARTNER, org, null);
			}
			
			sortTable.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
		checkExportEnabled();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && (dbe.getType().equals("WISH_ADDED") ||
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
											dbe.getType().equals("UPDATED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("LOADED_PARTNERS")))
		{
			updateWishAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			updateWishAssigneeSelectionList();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG"))
		{			
			updateWishSelectionList();
		}
		else if(dbe.getType().contains("ADDED_USER") || dbe.getType().contains("UPDATED_USER"))
		{
			updateUserList();
			
			//check to see if the current user was updated to update preferences
			ONCUser updatedUser = (ONCUser)dbe.getObject1();
 			if(userDB.getLoggedInUser().getID() == updatedUser.getID())
				updateUserPreferences(updatedUser);
		}
		else if(dbe.getType().contains("LOADED_USERS"))
		{
			updateUserList();
		}
		else if(dbe.getType().contains("LOADED_CHILDREN"))
		{
			updateSchoolFilterList();
			this.setTitle(String.format("Our Neighbor's Child - %d Wish Management", GlobalVariables.getCurrentSeason()));
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
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.WISH, EntityType.PARTNER);
	}
	
	/**************************************************************************************
	 * This class prints check sheets for verifying receipt of gifts from ONC partners. 
	 * 
	 * This class requires an array list, passed in the constructor, that hold a
	 * ONCReceivingSheet object for each page in the document to be printed
	 * by this class.
	 * @author johnwoneill
	 ************************************************************************************/
	private class ReceivingGiftCheckSheetPrinter implements Printable
	{	
		ArrayList<ONCReceivingSheet> rsAL;
		Image img;
		String oncSeason;
		
		ReceivingGiftCheckSheetPrinter(ArrayList<ONCReceivingSheet> rsal, Image img, String season)
		{
			rsAL = rsal;
			this.img = img;
			oncSeason = season;
		}
		
		/*********************************************************************************************
		 * This method draws a gift check box rectangle at the specified x, y location
		 ********************************************************************************************/
		void drawThickRect(Graphics2D g2d, int x, int y, int width, int height)
		{
//			float thickness = new Float(1.5);
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(new Float(1.5)));
			g2d.drawRect(x, y, width, height);
			g2d.setStroke(oldStroke);
		}

		void printReceivingSheetHeader(int x, int y, Image img, String season, String org, Font[] psFont, Graphics2D g2d)
		{
			 double scaleFactor = (72d / 300d) * 2;
     	     
			    // Now we perform our rendering 	       	    
			    int destX1 = (int) (img.getWidth(null) * scaleFactor);
			    int destY1 = (int) (img.getHeight(null) * scaleFactor);
			    
			    //Draw image scaled to fit image clip region on the label
			    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
		         
			    //Draw the ONC Season
			    g2d.setFont(psFont[1]);
			    g2d.drawString(season, x+54, y+22);
			    
			    //Draw the receiving sheet title
				g2d.setFont(psFont[1]);
				g2d.drawString("ONC Partner Receiving Check Sheet", x+160, y+20);
//				g2d.drawString("ONC Receiving Status Sheet", x+180, y+20);
				
			    //Draw first 32 characters of organization name
			    g2d.setFont(psFont[1]);
			    
			    String orgname = org.length() > 32 ? org.substring(0,31) : org;
//			    System.out.println(g2d.getFontMetrics(psFont[1]).stringWidth(orgname));
				g2d.drawString(orgname, x+374, y+20); 	//Organization name
				
				//Draw the receiving check instruction
			    g2d.setFont(psFont[0]);
			    g2d.drawString("CHECK BOX(     ) IF ITEM WAS RECEIVED", x+10, y+56);
//			    g2d.drawString("BOX IS GREEN IF ITEM WAS RECEIVED, RED IF ITEM NOT RECEIVED", x+10, y+56);
			    drawThickRect(g2d, x+70, y+46, 12, 12);
			    
			    //Draw the Checked By:
			    g2d.drawString("Checked By:", x+370, y+56);
			    
			  //Draw separator line
				g2d.drawLine(x, y+62, x+525, y+62);
		}
		
		void printReceivingSheetFooter(int x, int y, int pgNum, int pgTotal, Font[] psFont, Graphics2D g2d)
		{
			//Draw separator line
			g2d.drawLine(x, y, x+525, y);
			
			//Draw Packager Comments instruction
			g2d.setFont(psFont[0]);
			g2d.drawString("NOTES/SPECIAL COMMENTS:", x, y+16); 	//Comments note
			
			//Draw the page number 
			g2d.setFont(psFont[0]);
			String pageinfo = String.format("%d of %d", pgNum, pgTotal);
			g2d.drawString(pageinfo, x+262, y+76);
		}

		void printReceivingSheetBody(int x, int y, ArrayList<SortWishObject> stAL, int index, int linesonpage,
										Font[] psFont, Graphics2D g2d)
		{		
			for(int line=0; line< linesonpage; line++)
			{
				int[] row_sel = sortTable.getSelectedRows();
				String[] linedata = stAL.get(row_sel[index+line]).getReceivingSheetRow();
				drawThickRect(g2d, x, y+line*20, 12, 12);
//				if(line < 4)
//					drawFilledThickRect(g2d, x, y+line*20, 12, 12, Color.GREEN);
//				else
//					drawFilledThickRect(g2d, x, y+line*20, 12, 12, Color.RED);
				y += 10;
				g2d.drawString("Family " + linedata[0], x+20, y+line*20); 	//ONC Number
				g2d.drawString(linedata[1], x+88, y+line*20); 	//Age & Gender
				g2d.drawString(linedata[2], x+160, y+line*20); 	//Wish & Detail
			}
		}
		
		@Override
		public int print (Graphics g, PageFormat pf, int page) throws PrinterException
		{		
			if(page > rsAL.size())	//'page' is zero-based 
			{
				return NO_SUCH_PAGE;
		    }
			
			//Create 2d graphics context
			// User (0,0) is typically outside the imageable area, so we must
		    //translate by the X and Y values in the PageFormat to avoid clipping
		    Graphics2D g2d = (Graphics2D)g;
		    g2d.translate(pf.getImageableX(), pf.getImageableY());
			
			//Create fonts
			Font[] cFonts = new Font[5];
		    cFonts[0] = new Font("Calibri", Font.PLAIN, 12);//Instructions Font
		    cFonts[1] = new Font("Calibri", Font.BOLD, 14);	//Season Font
		    cFonts[2] = new Font("Calibri", Font.BOLD, 20);	//ONC Num Text Font
		    cFonts[3] = new Font("Calibri", Font.BOLD, 12);	//Child Font
		    cFonts[4] = new Font("Calibri", Font.BOLD, 13);	//Footer Font
			
			//Print page header
		    String org = rsAL.get(page).getRSOrg();;
			printReceivingSheetHeader(16, 0, img, oncSeason, org, cFonts, g2d);
			
			//Print page body
			printReceivingSheetBody(16, 70, stAL, rsAL.get(page).getRSIndex(), rsAL.get(page).getRSCount(),
										cFonts, g2d);			
			//Print page footer
			int pagetotal = rsAL.get(page).getRSTotalpages();
			printReceivingSheetFooter(16, 656, rsAL.get(page).getRSPage(), pagetotal, cFonts, g2d);
		    	    
		    //tell the caller that this page is part of the printed document	   
		    return PAGE_EXISTS;
	  	}
	}
	
	private class ONCReceivingSheet
	{	
		private String org;
		private int index;
		private int count;
		private int page;
		private int totalpages;
		
		ONCReceivingSheet(String o, int idx, int c, int p, int tp)
		{	
			org = o;
			index = idx;
			count = c;
			page = p;
			totalpages = tp;
		}
		
		//getters
		String getRSOrg() { return org; }
		int getRSIndex() { return index; }
		int getRSCount() { return count; }
		int getRSPage() { return page; }
		int getRSTotalpages() { return totalpages; }
	}

	private class ONCSortItemAgeComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChild().getChildDOB().compareTo(o2.getChild().getChildDOB());
		}
	}
	
	private class ONCSortItemFamNumComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			Integer onc1, onc2;
			
			if(!o1.getFamily().getONCNum().isEmpty() && isNumeric(o1.getFamily().getONCNum()))
				onc1 = Integer.parseInt(o1.getFamily().getONCNum());
			else
				onc1 = MAXIMUM_ON_NUMBER;
							
			if(!o2.getFamily().getONCNum().isEmpty() && isNumeric(o2.getFamily().getONCNum()))
				onc2 = Integer.parseInt(o2.getFamily().getONCNum());
			else
				onc2 = MAXIMUM_ON_NUMBER;
			
			return onc1.compareTo(onc2);
		}
	}
	
	private class ONCSortItemFamilyRegionComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			Integer o1Reg = (Integer) o1.getFamily().getRegion();
			Integer o2Reg = (Integer) o2.getFamily().getRegion();
			return o1Reg.compareTo(o2Reg);
		}
	}
	
	private class ONCSortItemGenderComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChild().getChildGender().compareTo(o2.getChild().getChildGender());
		}
	}
	
	private class ONCSortItemSchoolComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChild().getChildSchool().compareTo(o2.getChild().getChildSchool());
		}
	}
	
	private class ONCSortItemWishNumComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			Integer wishNum1 = o1.getChildWish().getWishNumber();
			Integer wishNum2 = o2.getChildWish().getWishNumber();
			return wishNum1.compareTo (wishNum2);
		}
	}
	
	private class ONCSortItemWishBaseComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			ONCWishCatalog cat = ONCWishCatalog.getInstance();

			String wishBase1, wishBase2;
			if(o1.getChildWish().getWishID() == -1)
				wishBase1 = "None";
			else
				wishBase1 = cat.getWishByID(o1.getChildWish().getWishID()).getName();
			
			if(o2.getChildWish().getWishID() == -1)
				wishBase2 = "None";
			else
				wishBase2 = cat.getWishByID(o2.getChildWish().getWishID()).getName();
			
//			String wishBase1 = cat.getWishByID(o1.getChildWish().getWishID()).getName();
//			String wishBase2 = cat.getWishByID(o2.getChildWish().getWishID()).getName();
			return wishBase1.compareTo(wishBase2);
//			return o1.getSortItemChildWishBase().compareTo(o2.getSortItemChildWishBase());
		}
	}
	
	private class ONCSortItemWishDetailComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChildWish().getChildWishDetail().compareTo(
					o2.getChildWish().getChildWishDetail());
		}
	}
	
	private class ONCSortItemWishIndicatorComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			Integer wishInd1 = o1.getChildWish().getChildWishIndicator();
			Integer wishInd2 = o2.getChildWish().getChildWishIndicator();
			return wishInd1.compareTo(wishInd2);	
//			return o1.getSortItemChildWishIndicator().compareTo(o2.getSortItemChildWishIndicator());
		}
	}
	
	private class ONCSortItemWishStatusComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
//			Integer wishStatus1 = o1.getChildWish().getChildWishStatus();
//			Integer wishStatus2 = o2.getChildWish().getChildWishStatus();
//			return wishStatus1.compareTo(wishStatus2);	
			return o1.getChildWish().getChildWishStatus().compareTo(o2.getChildWish().getChildWishStatus());
		}
	}
	
	private class ONCSortItemWishAssigneeComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			PartnerDB partnerDB = PartnerDB.getInstance();
			
			ONCChildWish wish1 = o1.getChildWish();
			if(wish1 == null)
				return 10;
			
			ONCPartner partner1 =  partnerDB.getPartnerByID(wish1.getChildWishAssigneeID());
			if(partner1 == null)
				return 10;
			
			ONCChildWish wish2 = o2.getChildWish();
			if(wish2 == null)
				return -10;
			
			ONCPartner partner2 =  partnerDB.getPartnerByID(wish2.getChildWishAssigneeID());
			if(partner2 == null)	
				return -10;
				
			return partner1.getName().compareTo(partner2.getName());
		}
	}
	
	private class ONCSortItemWishChangedByComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChildWish().getChildWishChangedBy().compareTo(
					o2.getChildWish().getChildWishChangedBy());
		}
	}
	
	private class ONCSortItemWishDateChangedComparator implements Comparator<SortWishObject>
	{
		@Override
		public int compare(SortWishObject o1, SortWishObject o2)
		{
			return o1.getChildWish().getChildWishDateChanged().compareTo(
					o2.getChildWish().getChildWishDateChanged());
		}
	}

	@Override
	protected String[] getTableRow(ONCObject o) 
	{
		SortWishObject swo = (SortWishObject) o;
		
		String[] indicator = {"", "*", "#"};
		ONCWish wish = wishCat.getWishByID(swo.getChildWish().getWishID());
		String wishName = wish == null ? "None" : wish.getName();
		ONCPartner partner = partnerDB.getPartnerByID(swo.getChildWish().getChildWishAssigneeID());
		String partnerName = partner != null ? partner.getName() : "";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(swo.getChildWish().getChildWishDateChanged().getTime());
		String[] tablerow = {
							swo.getFamily().getONCNum(),
							regions.getRegionID(swo.getFamily().getRegion()),
							swo.getChild().getChildAge().split("old", 2)[0].trim(), //Take the word "old" out of string
							swo.getChild().getChildGender(),
							swo.getChild().getChildSchool(),
							Integer.toString(swo.getChildWish().getWishNumber()+1),
							wishName,
							swo.getChildWish().getChildWishDetail(),
							indicator[swo.getChildWish().getChildWishIndicator()],
							swo.getChildWish().getChildWishStatus().toString(), 
							partnerName, 
							swo.getChildWish().getChildWishChangedBy(), ds};
		return tablerow;
	}

	@Override
	void initializeFilters() 
	{
		setSortStartDate(gvs.getSeasonStartDate());
		setSortEndDate(gvs.getTodaysDate());
	}
}
