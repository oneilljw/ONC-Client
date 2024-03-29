package ourneighborschild;

import java.awt.BasicStroke;
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
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.toedter.calendar.JDateChooser;

import au.com.bytecode.opencsv.CSVWriter;

public abstract class SortGiftsBaseDialog extends ChangeDialog implements PropertyChangeListener
{
	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;
	protected static final int ONC_AGE_LIMIT = 21;
	protected static final int NUM_OF_XMAS_ICONS = 5;
	protected static final int XMAS_ICON_OFFSET = 9;
	protected static final int RS_ITEMS_PER_PAGE = 20;
	protected static final Integer MAXIMUM_ON_NUMBER = 9999;
	protected static final int MAX_LABEL_LINE_LENGTH = 26;
	
	protected ChildGiftDB giftDB;
	protected ChildDB cDB;
	protected PartnerDB partnerDB;
	protected GiftCatalogDB giftCat;
	protected RegionDB regions;
	protected DNSCodeDB dnsCodeDB;
	protected ClonedGiftDB clonedGiftDB;

	protected ArrayList<SortGiftObject> stAL;
	protected GiftType dialogGiftType;
	
	protected JPanel infoPanel, cntlPanel;
	
	protected JComboBox<String> startAgeCB, endAgeCB, giftnumCB, genderCB, resCB, changedByCB;
	protected JComboBox<String> changeResCB, printCB, regionCB, schoolCB, exportCB, cloneCB, giftCardCB;
	protected JComboBox<ONCGift> giftCB;
	protected JComboBox<GiftStatus>  statusCB, changeStatusCB;
	protected JComboBox<ONCPartner> assignCB, changePartnerCB;
	protected JComboBox<FamilyStatus> famStatusCB;
	protected JComboBox<DNSCode> dnsCodeCB;
	protected List<DNSCode> filterCodeList;
	
	protected DefaultComboBoxModel<String> changedByCBM, regionCBM, schoolCBM, printCBM;
	protected DefaultComboBoxModel<GiftStatus> giftStatusFilterCBM, giftStatusChangeCBM;
	protected DefaultComboBoxModel<DNSCode> dnsCodeCBM;
	protected DefaultComboBoxModel<ONCGift> giftCBM;
	protected DefaultComboBoxModel<ONCPartner> assignCBM, changePartnerCBM;
	
	protected JTextField oncnumTF;
	protected JDateChooser ds, de;
	protected Calendar startFilterTimestamp, endFilterTimestamp;
	protected JCheckBox labelFitCxBox;
	
	protected int sortStartAge = 0, sortEndAge = ONC_AGE_LIMIT, sortGender = 0, sortChangedBy = 0;
	protected int sortGiftNum = 0, sortRes = 0, sortPartnerID, sortRegion = 0, sortGCO = 0;
	protected GiftStatus sortStatus = GiftStatus.Any;
	protected FamilyStatus sortFamilyStatus;
	protected DNSCode sortDNSCode;
	protected String sortSchool = "Any";
	protected int sortGiftID = -2;
	protected boolean bOversizeGifts = false;
	
	protected static String[] genders = {"Any", "Boy", "Girl"};
	protected static String[] res = {"Any", "Blank", "*", "#"};
	protected static String[] giftCardFilter = {"Any", "True", "False"};

	SortGiftsBaseDialog(JFrame pf)
	{
		super(pf);
		
		//set up the data base references
		cDB = ChildDB.getInstance();
		giftDB = ChildGiftDB.getInstance();
		clonedGiftDB = ClonedGiftDB.getInstance();
		partnerDB = PartnerDB.getInstance();
		giftCat = GiftCatalogDB.getInstance();
		regions = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
		
		//set up data base listeners
		if(dbMgr != null)
    		dbMgr.addDatabaseListener(this);
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		if(giftCat != null)
			giftCat.addDatabaseListener(this);
		if(regions != null)
			regions.addDatabaseListener(this);
		if(dnsCodeDB != null)
			dnsCodeDB.addDatabaseListener(this);
		if(gvs != null)
			gvs.addDatabaseListener(this);
		
		//initialize member variables
		stAL = new ArrayList<SortGiftObject>();

		//Set up the search criteria panel
		oncnumTF = new JTextField(4);
		oncnumTF.setEditable(true);
		oncnumTF.setMaximumSize(new Dimension(64,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());

		//Get a catalog for type=selection
		sortDNSCode = new DNSCode(-4, "No Codes", "No Codes", "Families being served", false);
		filterCodeList = new ArrayList<DNSCode>();
		filterCodeList.add(sortDNSCode);
		filterCodeList.add(new DNSCode(-3, "Any", "Any", "Any", false));
		filterCodeList.add(new DNSCode(-2, "All Codes", "All Codes", "All Codes", false));
		
		dnsCodeCBM = new DefaultComboBoxModel<DNSCode>();
		for(DNSCode filterCode : filterCodeList)
			dnsCodeCBM.addElement(filterCode);
		dnsCodeCB = new JComboBox<DNSCode>();
		dnsCodeCB.setModel(dnsCodeCBM);
		dnsCodeCB.setPreferredSize(new Dimension(120, 56));
		dnsCodeCB.setBorder(BorderFactory.createTitledBorder("DNS Code"));
		dnsCodeCB.addActionListener(this);
		
		giftCardCB = new JComboBox<String>(giftCardFilter);
		giftCardCB.setPreferredSize(new Dimension(88, 56));
		giftCardCB.setToolTipText("GCO - Gift Card Only Family Filter");
		giftCardCB.setBorder(BorderFactory.createTitledBorder("GCO Family?"));
		giftCardCB.addActionListener(this);
		
		regionCBM = new DefaultComboBoxModel<String>();
		regionCBM.addElement("Any");
		regionCB = new JComboBox<String>();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.setPreferredSize(new Dimension(72,56));
		regionCB.addActionListener(this);
    	
		String[] ages = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};

		startAgeCB = new JComboBox<String>(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Start Age"));
		startAgeCB.setPreferredSize(new Dimension(64,56));
		startAgeCB.addActionListener(this);
		
		endAgeCB = new JComboBox<String>(ages);
		endAgeCB.setBorder(BorderFactory.createTitledBorder("End Age"));
		endAgeCB.setPreferredSize(new Dimension(64,56));
		endAgeCB.setSelectedIndex(ages.length-1);
		endAgeCB.addActionListener(this);
		
		genderCB = new JComboBox<String>(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setSize(new Dimension(72,56));
		genderCB.addActionListener(this);
		
		String[] giftnums = getGiftNumbers();
		giftnumCB = new JComboBox<String>(giftnums);
		giftnumCB.setBorder(BorderFactory.createTitledBorder("Gift #"));
		giftnumCB.setPreferredSize(new Dimension(72,56));
		giftnumCB.addActionListener(this);
		
		famStatusCB = new JComboBox<FamilyStatus>(FamilyStatus.getSearchFilterList());
		sortFamilyStatus = FamilyStatus.Any;
		famStatusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
		famStatusCB.addActionListener(this);
		
		schoolCBM = new DefaultComboBoxModel<String>();
	    schoolCBM.addElement("Any");
		schoolCB = new JComboBox<String>();		
        schoolCB.setModel(schoolCBM);
		schoolCB.setPreferredSize(new Dimension(180, 56));
		schoolCB.setBorder(BorderFactory.createTitledBorder("School Attended"));
		schoolCB.addActionListener(this);
		
		giftCBM = new DefaultComboBoxModel<ONCGift>();
	    giftCBM.addElement(new ONCGift(-2, "Any", 7));
		giftCB = new JComboBox<ONCGift>();		
        giftCB.setModel(giftCBM);
		giftCB.setPreferredSize(new Dimension(180, 56));
		giftCB.setBorder(BorderFactory.createTitledBorder("Gift Type"));
		giftCB.addActionListener(this);
		
		changedByCB = new JComboBox<String>();
		changedByCBM = new DefaultComboBoxModel<String>();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.setPreferredSize(new Dimension(144,56));
		changedByCB.addActionListener(this);
		
		startFilterTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		ds = new JDateChooser(startFilterTimestamp.getTime());
		ds.setPreferredSize(new Dimension(156, 56));
		ds.setBorder(BorderFactory.createTitledBorder("Changed On/After"));
		ds.getDateEditor().addPropertyChangeListener(this);
		sortCriteriaPanel.add(ds);
		
		endFilterTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		de = new JDateChooser(endFilterTimestamp.getTime());
		de.setPreferredSize(new Dimension(156, 56));
		de.setBorder(BorderFactory.createTitledBorder("Changed On/Before"));
		de.getDateEditor().addPropertyChangeListener(this);
		sortCriteriaPanel.add(de);
		
		resCB = new JComboBox<String>(res);
		resCB.setPreferredSize(new Dimension(104, 56));
		resCB.setBorder(BorderFactory.createTitledBorder("Restrictions"));
		resCB.addActionListener(this);
		res[0] = "No Change";	//Change "Any" to none after sort criteria list created
		
		giftStatusFilterCBM = new DefaultComboBoxModel<GiftStatus>();
		statusCB = new JComboBox<GiftStatus>(giftStatusFilterCBM);
		statusCB.setPreferredSize(new Dimension(136, 56));
		statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
		
		assignCB = new JComboBox<ONCPartner>();
		assignCBM = new DefaultComboBoxModel<ONCPartner>();
		
		//take advantage of the fact that partner id's are 7 digits and start with the calendar year
	    assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
	    assignCBM.addElement(new ONCPartner(-1, "Unassigned", "Unassigned"));
	    assignCB.setModel(assignCBM);
		assignCB.setPreferredSize(new Dimension(192, 56));
		assignCB.setBorder(BorderFactory.createTitledBorder("Partner Assigned To"));
		
		//set the user preference for assignee filter default setting
		assignCB.setSelectedIndex(1);
		sortPartnerID = -1;
		assignCB.addActionListener(this);

		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(dnsCodeCB);
		sortCriteriaPanelTop.add(giftCardCB);
		sortCriteriaPanelTop.add(regionCB);
		sortCriteriaPanelTop.add(startAgeCB);
		sortCriteriaPanelTop.add(endAgeCB);
		sortCriteriaPanelTop.add(famStatusCB);
		sortCriteriaPanelTop.add(genderCB);
		sortCriteriaPanelTop.add(schoolCB);
		sortCriteriaPanelTop.add(giftnumCB);
		sortCriteriaPanelBottom.add(giftCB);
		sortCriteriaPanelBottom.add(resCB);
		sortCriteriaPanelBottom.add(statusCB);
		sortCriteriaPanelBottom.add(assignCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(ds);
		sortCriteriaPanelBottom.add(de);
		
		//set up the change panel, which consists of the item count panel and the 
		//change data panel
		itemCountPanel.setBorder(BorderFactory.createTitledBorder("Gifts Meeting Criteria"));
		
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Gift Restrictions/Status/Partner"));
        
		changeResCB = new JComboBox<String>(res);
        changeResCB.setPreferredSize(new Dimension(192, 56));
		changeResCB.setBorder(BorderFactory.createTitledBorder("Change Restrictions To:"));
		changeResCB.addActionListener(this);
        
		giftStatusChangeCBM = new DefaultComboBoxModel<GiftStatus>();
        changeStatusCB = new JComboBox<GiftStatus>(giftStatusChangeCBM);
        changeStatusCB.setPreferredSize(new Dimension(192, 56));
		changeStatusCB.setBorder(BorderFactory.createTitledBorder("Change Status To:"));
        
        changePartnerCB = new JComboBox<ONCPartner>();
        changePartnerCBM = new DefaultComboBoxModel<ONCPartner>();
	    changePartnerCBM.addElement(new ONCPartner(0, "No Change", "No Change"));
	    changePartnerCBM.addElement(new ONCPartner(-1, "None", "None"));
        changePartnerCB.setModel(changePartnerCBM);
        changePartnerCB.setPreferredSize(new Dimension(192, 56));
		changePartnerCB.setBorder(BorderFactory.createTitledBorder("Change Partner To:"));
		changePartnerCB.addActionListener(this);
		
		changeDataPanel.add(changeResCB);
		changeDataPanel.add(changeStatusCB);
		changeDataPanel.add(changePartnerCB);
		
		gbc.gridx = 1;
	    gbc.ipadx = 0;
	    gbc.weightx = 1.0;
	    changePanel.add(changeDataPanel, gbc);

	    //set up the dialog defined control panel to add to the bottom panel
	    infoPanel = new JPanel();
	    labelFitCxBox = new JCheckBox("Overlength Ornament Labels Only");
	    labelFitCxBox.setSelected(bOversizeGifts);
	    labelFitCxBox.addActionListener(this);
	    infoPanel.add(labelFitCxBox);
	    
	    cntlPanel = new JPanel();
	    cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    
	    String[] cloneChoices = {"Clone Gifts", "Create Cloned Gifts"};
        cloneCB = new JComboBox<String>(cloneChoices);
        cloneCB.setPreferredSize(new Dimension(136, 28));
        cloneCB.setEnabled(false);
        cloneCB.addActionListener(this);
   
        String[] exportChoices = {"Export", "Export .csv file", "Export SignUp Genius"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setPreferredSize(new Dimension(136, 28));
        exportCB.setEnabled(true);
        exportCB.addActionListener(this);
        
        printCBM = new DefaultComboBoxModel<String>();
	    printCBM.addElement("Print");
	    printCBM.addElement("Print Listing");
	    printCBM.addElement("Print Labels");
        printCB = new JComboBox<String>(printCBM);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        
        //calculate width
        int dlgWidth = 24; //account for vertical scroll bar
        int[] colWidths = getColumnWidths();
        for(int i=0; i< colWidths.length; i++)
        	dlgWidth += colWidths[i];
        this.setMinimumSize(new Dimension(dlgWidth, 560));
	}
	
	abstract String[] getGiftNumbers();
	abstract List<ONCChildGift> getChildGiftList();
	
	@Override
	int sortTableList(int col)
	{	
		archiveTableSelections(stAL);
		
		if(col == 0)	//Sort on ONC Family Number
    			Collections.sort(stAL, new ONCSortItemFamNumComparator());
		else if(col == 1)	// Sort on DNS Code
    			Collections.sort(stAL, new ONCSortItemFamilyDNSComparator());
		else if(col == 2)	// Sort on Child's Age
    			Collections.sort(stAL, new ONCSortItemFamilyRegionComparator());
		else if(col == 3)	// Sort on Child's Age
    			Collections.sort(stAL, new ONCSortItemAgeComparator());
		else if(col == 4)	//Sort on Child's Gender
			Collections.sort(stAL, new ONCSortItemGenderComparator());
		else if(col == 5)	//Sort on Child's School
			Collections.sort(stAL, new ONCSortItemSchoolComparator());
		else if(col == 6)	//Sort on Child's Wish #
			Collections.sort(stAL, new ONCSortItemGiftNumComparator());
		else if(col == 7)	//Sort on Child's Base Wish
			Collections.sort(stAL, new ONCSortItemGiftBaseComparator());
		else if(col == 8)	//Sort on Child's Wish Detail
			Collections.sort(stAL, new ONCSortItemGiftDetailComparator());
		else if(col == 9)	//Sort on Child's Wish Indicator
			Collections.sort(stAL, new ONCSortItemGiftIndicatorComparator());
		else if(col == 10)	//Sort on Child's Wish Status
			Collections.sort(stAL, new ONCSortItemGiftStatusComparator());
		else if(col == 11)	//Sort on Child's Wish Assignee
			Collections.sort(stAL, new ONCSortItemGiftPartnerComparator());
		else if(col ==  12)	//Sort on Child's Wish Changed By
			Collections.sort(stAL, new ONCSortItemGiftChangedByComparator());
		else if(col == 13)	//Sort on Child's Wish Date Changed
			Collections.sort(stAL, new ONCSortItemGiftDateChangedComparator());
		else
			col = -1;
		
		if(col > -1)
			displaySortTable(stAL, false, tableRowSelectedObjectList);
    		
		return col;
	}

	@Override
	void setEnabledControls(boolean tf)
	{
		cloneCB.setEnabled(tf);
		exportCB.setEnabled(tf);
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
		for(ONCChildGift cg : getChildGiftList())	//returns a list of current gifts
		{
			ONCChild c = cDB.getChild(cg.getChildID());
			if(c != null)
			{	
				ONCFamily f = fDB.getFamily(c.getFamID());
				FamilyHistory fh = fhDB.getLastFamilyHistory(c.getFamID());
				if(f != null && isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()) &&
					doesFamilyStatusMatch(fh.getFamilyStatus()) && doesDNSCodeMatch(fh.getDNSCode()) && 
					doesRegionMatch(f.getRegion()) &&	isAgeInRange(c) && doesGenderMatch(c) &&
					doesSchoolMatch(c) && doesResMatch(cg.getIndicator()) && doesGiftCardOnlyMatch(f.isGiftCardOnly()) &&
					doesPartnerMatch(cg.getPartnerID()) && doesStatusMatch(cg.getGiftStatus()) &&
					isGiftChangeDateBetween(cg.getTimestamp()) && doesChangedByMatch(cg.getChangedBy()) &&
					doesGiftBaseMatch(cg.getCatalogGiftID()) && doesGiftNumMatch(cg.getGiftNumber())  &&
					!(bOversizeGifts && !isGiftOversize(cg)))//Wish criteria pass
				{
					stAL.add(new SortGiftObject(itemID++, f, fh, c, cg));
				}
			}
		}
		
		Collections.sort(stAL, new ONCSortItemTableListComparator());	//sort the list by ONC Number, then by child age
		updateSchoolFilterList();
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table list is built	
	}
	
	void updateGiftSelectionList()
	{
		//disable the combo box
		bIgnoreCBEvents = true;
		giftCB.setEnabled(false);
		
		//get the current selection
		int currSelID = ((ONCGift) giftCB.getSelectedItem()).getID();
		
		//reset the selection in case the current selection is removed by the update
		giftCB.setSelectedIndex(0);
		sortGiftID = -2;
		
		//Clear the combo box of current elements
		giftCBM.removeAllElements();	
	
		List<ONCGift> giftList = giftCat.getGiftList(GiftListPurpose.Filtering);
		for(ONCGift w: giftList )	//Add new list elements
			giftCBM.addElement(w);
		
		//reselect the prior selection, if it's still in the list
		int index = 0;
		while(index < giftList.size() && giftList.get(index).getID() != currSelID)
			index++;
		
		if(index < giftList.size())
		{
			giftCB.setSelectedItem(giftList.get(index));
			sortGiftID = giftList.get(index).getID();
		}
		
		//re-enable the combo box
		giftCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	/*********************************************************************************************
	 * This method updates the drop down selection list associated with the Assignee combo
	 * boxes used to search for assignees and to select partners. The lists must be updated
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
		int currentChangeAssigneeIndex = changePartnerCB.getSelectedIndex();
		
		if(assignCB.getSelectedIndex() > 1)	//leaves the current selection null if no selection made
			currentAssigneeID = ((ONCPartner)assignCB.getSelectedItem()).getID();
		
		if(changePartnerCB.getSelectedIndex() > 1)
			currentChangeAssigneeID = ((ONCPartner)changePartnerCB.getSelectedItem()).getID();
		
		assignCB.setSelectedIndex(0);
		sortPartnerID = 0;
		changePartnerCB.setSelectedIndex(0);
		
		assignCBM.removeAllElements();
		changePartnerCBM.removeAllElements();
		
		//take advantage of the fact that partner id's are 7 digits and start with the calendar year
		assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
		assignCBM.addElement(new ONCPartner(-1, "Unassigned", "Unassigned"));
		changePartnerCBM.addElement(new ONCPartner(-1, "No Change", "No Change"));
		changePartnerCBM.addElement(new ONCPartner(-1, "None", "None"));
		
		//get enumset of Ornament, Clothing, Coats, ONCShopper, the allowable types that are assigned gifts to fulfill
		EnumSet<GiftCollectionType> assigneeCollectionTypeSet = EnumSet.of(GiftCollectionType.Ornament, GiftCollectionType.Clothing, GiftCollectionType.Coats, GiftCollectionType.ONCShopper);
		for(ONCPartner confirmedPartner:partnerDB.getConfirmedPartnerList(assigneeCollectionTypeSet))
		{
			assignCBM.addElement(confirmedPartner);
			changePartnerCBM.addElement(confirmedPartner);
		}
		
		//Attempt to reselect the previous selected organization, if one was selected. 
		//If the previous selection is no longer in the drop down, the top of the list is 
		//already selected 
		if(currentAssigneeIndex == 1)	//Organization == "No one", ID = -1
		{
			assignCB.setSelectedIndex(1);
			sortPartnerID = -1;
		}
		else if(currentAssigneeIndex > 1)
		{
			ONCPartner assigneeOrg = partnerDB.getPartnerByID(currentAssigneeID);
			if(assigneeOrg != null)
			{
				assignCB.setSelectedItem(assigneeOrg);
				sortPartnerID = assigneeOrg.getID();	//Need to update the sort Assignee as well
			}
		}
		
		if(currentChangeAssigneeIndex == 1)		//Organization == "None"
			changePartnerCB.setSelectedIndex(1);
		else
		{
			ONCPartner changeAssigneeOrg = partnerDB.getPartnerByID(currentChangeAssigneeID);
			if(changeAssigneeOrg != null)
				changePartnerCB.setSelectedItem(changeAssigneeOrg);
		}
		
		bIgnoreCBEvents = false;
	}
	
	@SuppressWarnings("unchecked")
	void updateDNSCodeCB()
	{
		dnsCodeCB.removeActionListener(this);
		
		DNSCode currFilterSel = (DNSCode) dnsCodeCB.getSelectedItem();
		
		dnsCodeCBM.removeAllElements();	//Clear the combo box selection list
		for(DNSCode filterCode : filterCodeList)
			dnsCodeCBM.addElement(filterCode);
		
		for(DNSCode code: (List<DNSCode>) dnsCodeDB.getList())	//Add new list elements	
			dnsCodeCBM.addElement(code);
			
		//reselect the DNS code
		dnsCodeCB.setSelectedItem(currFilterSel);
	
		dnsCodeCB.addActionListener(this);
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
			sortPartnerID = assignCB.getSelectedIndex() == 0 ? 0 : -1;
			assignCB.addActionListener(this);
			
			buildTableList(false);
		}
		
		DNSCode selDNSCode = (DNSCode) dnsCodeCB.getSelectedItem();
		if(dnsCodeCB.getSelectedIndex() < 2 &&
			selDNSCode != u.getPreferences().getFamilyDNSFilterCode())
		{
			dnsCodeCB.removeActionListener(this);
			dnsCodeCB.setSelectedItem(u.getPreferences().getFamilyDNSFilterCode());
			sortDNSCode = u.getPreferences().getFamilyDNSFilterCode();
			dnsCodeCB.addActionListener(this);
				
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
			
			for(int i=0; i < totalpages; i++)
			{
				int countonpage = sortTable.getSelectedRowCount() - i*RS_ITEMS_PER_PAGE;
				int count = countonpage > RS_ITEMS_PER_PAGE ? RS_ITEMS_PER_PAGE : countonpage;
				rsAL.add(new ONCReceivingSheet(((ONCPartner) assignCB.getSelectedItem()).getLastName(), 
											   i*RS_ITEMS_PER_PAGE, count, i+1, totalpages));
			}
			
			//Create the info required for the print job
			SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(twodigitYear.format(gvs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
			String oncSeason = "ONC " + Integer.toString(gvs.getCurrentSeason());			
			
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
					"No gifts in table to print, table must contain gifts in order to" + 
					"print listing", 
					"No Gifts To Print", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
		
	void checkApplyChangesEnabled()
	{
		if(sortTable.getSelectedRows().length > 0 &&
				(changeResCB.getSelectedIndex() > 0 || changeStatusCB.getSelectedIndex() > 0 ||changePartnerCB.getSelectedIndex() > 0))	
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}
	
	void checkExportEnabled()
	{
		cloneCB.setEnabled(sortTable.getSelectedRowCount() > 0);
		exportCB.setEnabled(sortTable.getSelectedRowCount() > 0);
	}
	
	void onCloneRequested()
	{
		List<ONCChildGift> reqAddClonedGiftList = new ArrayList<ONCChildGift>();

		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			//check to see the gift has been selected or is farther along the gift life cycle
			if(stAL.get(row_sel[i]).getChildGift().getGiftStatus().compareTo(GiftStatus.Not_Selected) > 0)
				reqAddClonedGiftList.add(stAL.get(row_sel[i]).getChildGift());
		}
		
		if(reqAddClonedGiftList.isEmpty())
		{
			String message = "No clones created. No selected gifts eliglible for cloning.";
			JOptionPane.showMessageDialog(this, message, "All Selected Gifts Ineligible", 
					JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else if(row_sel.length != reqAddClonedGiftList.size())
		{
			String message = "One or more selected gifts is not eliglible. Continue anyway?";
			Object[] options = {"Cancel", "Continue"};
			JOptionPane confirmOP = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(parentFrame, "*** Confirm Create Clone Gifts ***");
			confirmDlg.setLocationRelativeTo(this);
			confirmDlg.setAlwaysOnTop(true);
			confirmDlg.setVisible(true);
			
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Continue"))
				sendCloneRequestToServer(reqAddClonedGiftList);
		}		
		else
			sendCloneRequestToServer(reqAddClonedGiftList);
	}
	
	void sendCloneRequestToServer(List<ONCChildGift> reqAddClonedGiftList)
	{
		//Confirm with the user that creating clones is really intended
		String confirmMssg = String.format("Are you sure you want to clone %d gifts?", reqAddClonedGiftList.size()); 
										
		Object[] options= {"Cancel", "Clone"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(parentFrame, "*** Confirm Create Clone Gifts ***");
		confirmDlg.setLocationRelativeTo(this);
		confirmDlg.setAlwaysOnTop(true);
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Clone"))
		{	
			String response = "ADD_CLONE_LIST_FAILED";
			response = clonedGiftDB.addClonesFromChildGiftList(this, reqAddClonedGiftList);
			
			JOptionPane.showMessageDialog(this, response, "Cloned Gift Result", 
										JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Clone gift request canceled by user", 
				"Clone Request Canceled", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
		}
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"ONC #", "DNS", "ES Boundary","Gender", "Age on 12/25", "DoB", "Res", "Gift", "Detail", "Status", 
    						"Partner", "Changed By", "Date Changed"};
    
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       							new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
	    	    
       			String mssg = String.format("%d %s gifts successfuly exported to %s", 
       					sortTable.getSelectedRowCount(), dialogGiftType.toString(), oncwritefile.getName());
       			
       			JOptionPane.showMessageDialog(this, mssg,"Export Successful", JOptionPane.INFORMATION_MESSAGE,
       											gvs.getImageIcon(0));
       		} 
       		catch (IOException x)
       		{
       			JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
       						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
       			System.err.format("IOException: %s%n", x);
       		}
	    }
       	
        exportCB.setSelectedIndex(0);	//Reset the user export request
	}
	
	void onExportSignUpGeniusRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"Start Date/Time", "Qty", "Item"};
    
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       							new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
       				writer.writeNext(stAL.get(index).getSignUpGeniusExportRow());
       			}
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						sortTable.getSelectedRowCount() + " gifts sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
       		} 
       		catch (IOException x)
       		{
       			JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
       						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
       			System.err.format("IOException: %s%n", x);
       		}
	    }
       	
        exportCB.setSelectedIndex(0);	//Reset the user export request
	}

	protected boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	protected boolean doesDNSCodeMatch(int dnsCodeID)
	{
		if(sortDNSCode.getID() == -4 && dnsCodeID == -1)	//"No Code" -- show only families being served
			return true;
		else if(sortDNSCode.getID() == -3)	//show all families
			return true;
		else if(sortDNSCode.getID() == -2 && dnsCodeID > -1)	//show all families not being served
			return true;
		else
			return sortDNSCode.getID() == dnsCodeID;
	}
	
	protected boolean doesGiftCardOnlyMatch(boolean gco) { return sortGCO == 0 || (gco && giftCardCB.getSelectedIndex()== 1) || 
																	(!gco && giftCardCB.getSelectedIndex()== 2); } 
	
	protected boolean isAgeInRange(ONCChild c)
	{
		return c.getChildIntegerAge() >= startAgeCB.getSelectedIndex() && c.getChildIntegerAge() <= endAgeCB.getSelectedIndex();		
	}
	
	protected boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	protected boolean doesFamilyStatusMatch(FamilyStatus fs)
	{
		return sortFamilyStatus == FamilyStatus.Any || fs == sortFamilyStatus;
	}
	
	protected boolean doesSchoolMatch(ONCChild c)
	{
		return sortSchool.equals("Any") || (c.getChildSchool().equalsIgnoreCase(sortSchool));		
	}
	
	protected boolean doesGiftNumMatch(int wn)
	{
		return sortGiftNum == 0 || sortGiftNum == wn+1;		
	}
	
	protected boolean doesResMatch(int res) { return sortRes == 0  || sortRes == res+1; }
	
	protected boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	protected boolean doesStatusMatch(GiftStatus ws){return sortStatus == GiftStatus.Any || sortStatus.compareTo(ws) == 0;}
	
	protected boolean doesPartnerMatch(int assigneeID)
	{
		//sortAssigneeID's 0 and -1 are reserved for special filters. 0 shows all gifts regardless off assignee,
		//-1 displays gifts that are not assigned
		return sortPartnerID == 0 || sortPartnerID == assigneeID;
	}
	
	protected boolean isGiftChangeDateBetween(long timestamp)
	{
		return timestamp >= startFilterTimestamp.getTimeInMillis() && timestamp <= endFilterTimestamp.getTimeInMillis();
	}
			
	protected boolean doesGiftBaseMatch(int giftID)	{return sortGiftID == -2 ||  sortGiftID == giftID; }
	
	protected boolean doesChangedByMatch(String s) { return sortChangedBy == 0 || changedByCB.getSelectedItem().toString().equals(s); }
	
	/*************
	 * Method checks to see if a gift will fit on a label. A label has two lines to describe
	 * the gift. If the name and detail fit on one line, it's ok. If it requires two
	 * lines, need to check the 2nd line to see if it fits. If the second line is too long
	 * the method returns true. Otherwise it returns false.
	 * @param cg - ONCChildWish to check if it fits on a label
	 * @return - true if gift is too big for label, false it it fits on label
	 ******************/
	protected boolean isGiftOversize(ONCChildGift cg)
	{
		ONCGift goft;
		boolean bOversize = false;	//only get to true if 2nd line doesn't fit
		
		if(cg != null && bOversizeGifts && (goft=giftCat.getGiftByID(cg.getCatalogGiftID())) != null)
		{
			//construct how gift will appear on label
			String giftOnLabel = goft.getName() + " - " + cg.getDetail();
			
			//test the length to see if it needs two lines, if only one line, labels OK
			if(giftOnLabel.length() > MAX_LABEL_LINE_LENGTH)
			{
				//else, split into two lines and check 2nd line
				int index = MAX_LABEL_LINE_LENGTH;
				while(index > 0 && giftOnLabel.charAt(index) != ' ')	//find the line break
					index--;
				
				//does 2nd line fit on label?
				bOversize = giftOnLabel.substring(index).length() > MAX_LABEL_LINE_LENGTH;
			}
		}
		
		return bOversize;
	}

	protected void updateSchoolFilterList()
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
		else if(e.getSource() == dnsCodeCB && sortDNSCode.getID() != ((DNSCode) dnsCodeCB.getSelectedItem()).getID())
		{
			sortDNSCode = (DNSCode) dnsCodeCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == giftCardCB && giftCardCB.getSelectedIndex() != sortGCO)
		{
			sortGCO = giftCardCB.getSelectedIndex();
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
		else if(e.getSource() == famStatusCB && sortFamilyStatus != (FamilyStatus) famStatusCB.getSelectedItem())
		{
			sortFamilyStatus = (FamilyStatus) famStatusCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == giftnumCB && giftnumCB.getSelectedIndex() != sortGiftNum)
		{
			sortGiftNum = giftnumCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == giftCB && !bIgnoreCBEvents && 
				((ONCGift)giftCB.getSelectedItem()).getID() != sortGiftID)
		{						
			sortGiftID = ((ONCGift)giftCB.getSelectedItem()).getID();
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
			sortStatus = (GiftStatus) statusCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == labelFitCxBox )
		{
			bOversizeGifts = labelFitCxBox.isSelected();
			buildTableList(false);
		}
		else if(e.getSource() == assignCB && !bIgnoreCBEvents && 
				((ONCPartner)assignCB.getSelectedItem()).getID() != sortPartnerID )
		{						
			sortPartnerID = ((ONCPartner)assignCB.getSelectedItem()).getID();
			buildTableList(false);
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1)	//Can always print listing
			{
				onPrintListing(String.format("ONC %d %s Gifts", gvs.getCurrentSeason(), dialogGiftType.toString()));
			} 
			else	//Can only print labels, check sheets if table rows are selected
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
    					"No gifts selected, please selected gifts in order to " + 
    					printCB.getSelectedItem().toString(), 
    					"No Gifts Selected", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					printCB.setSelectedIndex(0);
				}
			}
		}
		else if(e.getSource() == cloneCB)
		{
			onCloneRequested();		
			cloneCB.setSelectedIndex(0);	//Reset the combo box choice			
		}
		else if(e.getSource() == exportCB)
		{
			if(sortTable.getSelectedRowCount() > 0)
			{
				if(exportCB.getSelectedIndex() == 1)
					onExportRequested();
				else if(exportCB.getSelectedIndex() == 2)
					onExportSignUpGeniusRequested();
			}
			else	//Warn user
			{
				JOptionPane.showMessageDialog(this, 
					"No gifts selected, please selected gifts in order to " + 
					exportCB.getSelectedItem().toString(), 
					"No Gifts Selected", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				exportCB.setSelectedIndex(0);
			}
		}
		else if(!bIgnoreCBEvents && (e.getSource() == changeResCB ||
					e.getSource() == changeStatusCB || e.getSource() == changePartnerCB))
		{
			checkApplyChangesEnabled();
		}
	}
	
	@Override
	String[] getColumnToolTips()
	{
		String[] colToolTips = {"ONC Family Number", "Family DNS Code", "Family Del. Address Region", "Child's Age", 
				"Child's Gender", "School Child Attends", "Gift Number: 1, 2 or 3", "Type of Gift Assigned",
				"Detailed Description for Gift Assigned",
				"# - Selected by ONC or * - Don't asssign", "Gift Status", "Partner gift is assigned to fulfill",
				"User who last changed gift", "Date & Time Gift Last Changed"};
		
		return colToolTips;
	}
	@Override
	String[] getColumnNames()
	{
		String[] columns = {"ONC", "DNS", "Reg","Age", "Gend", "School", "Gift", "Gift Type", "Details", " Res ",
				"Status", "Partner", "Changed By", "Timestamp"};
		
		return columns;
	}
	
	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {40, 40, 32, 48, 36, 120, 36, 84, 200, 32, 80, 200, 96, 96};
		return colWidths;
	}
	
	@Override
	int[] getCenteredColumns()
	{
		int[] center_cols = {1,2,6,9};
		return center_cols;
	}
	
	@Override
	void onResetCriteriaClicked()
	{
		oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
		sortONCNum = "";
		
		dnsCodeCB.removeActionListener(this);
		UserPreferences uPrefs = userDB.getUserPreferences();
		dnsCodeCB.setSelectedItem(uPrefs.getFamilyDNSFilterCode());
		sortDNSCode = uPrefs.getFamilyDNSFilterCode();
		dnsCodeCB.addActionListener(this);
		
		giftCardCB.removeActionListener(this);
		giftCardCB.setSelectedIndex(0);
		sortGCO = 0;
		giftCardCB.addActionListener(this);
		
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
		
		famStatusCB.removeActionListener(this);
		famStatusCB.setSelectedIndex(0);
		sortFamilyStatus = FamilyStatus.Any;
		famStatusCB.addActionListener(this);
		
		giftnumCB.removeActionListener(this);
		giftnumCB.setSelectedIndex(0);
		sortGiftNum = 0;
		giftnumCB.addActionListener(this);
		
		giftCB.removeActionListener(this);
		giftCB.setSelectedIndex(0);
		sortGiftID = -2;
		giftCB.addActionListener(this);
		
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
		sortStatus = GiftStatus.Any;
		statusCB.addActionListener(this);
		
		regionCB.removeActionListener(this);
		regionCB.setSelectedIndex(0);
		sortRegion = 0;
		statusCB.addActionListener(this);
		
		assignCB.removeActionListener(this);
		//set the user preference for assignee filter default setting
		assignCB.setSelectedIndex(uPrefs.getWishAssigneeFilter());
		sortPartnerID = assignCB.getSelectedIndex() == 0 ? 0 : -1;
		assignCB.addActionListener(this);
		
		changeResCB.removeActionListener(this);
		changeResCB.setSelectedIndex(0);
		changeResCB.addActionListener(this);
		
		changeStatusCB.removeActionListener(this);
		changeStatusCB.setSelectedIndex(0);
		changeStatusCB.addActionListener(this);
		
		changePartnerCB.removeActionListener(this);
		changePartnerCB.setSelectedIndex(0);
		changePartnerCB.addActionListener(this);
		
		labelFitCxBox.removeActionListener(this);
		labelFitCxBox.setSelected(false);
		bOversizeGifts = false;
		labelFitCxBox.addActionListener(this);
		
		de.getDateEditor().removePropertyChangeListener(this);
		ds.getDateEditor().removePropertyChangeListener(this);
		setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
		ds.setCalendar(startFilterTimestamp);
		de.setCalendar(endFilterTimestamp);
		ds.getDateEditor().addPropertyChangeListener(this);
		de.getDateEditor().addPropertyChangeListener(this);
			
		buildTableList(false);
	}
	
	void updateDateFilters()
	{
		de.getDateEditor().removePropertyChangeListener(this);
		ds.getDateEditor().removePropertyChangeListener(this);
		
		setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
		
		ds.setCalendar(startFilterTimestamp);
		de.setCalendar(endFilterTimestamp);
		
		ds.getDateEditor().addPropertyChangeListener(this);
		de.getDateEditor().addPropertyChangeListener(this);
	}
	
	void setDateFilters(Calendar start, Calendar end, int startOffset, int endOffset)
	{
		startFilterTimestamp.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
		startFilterTimestamp.set(Calendar.HOUR_OF_DAY, 0);
		startFilterTimestamp.set(Calendar.MINUTE, 0);
		startFilterTimestamp.set(Calendar.SECOND, 0);
		startFilterTimestamp.set(Calendar.MILLISECOND, 0);
		
		endFilterTimestamp.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH));
		endFilterTimestamp.set(Calendar.HOUR_OF_DAY, 0);
		endFilterTimestamp.set(Calendar.MINUTE, 0);
		endFilterTimestamp.set(Calendar.SECOND, 0);
		endFilterTimestamp.set(Calendar.MILLISECOND, 0);
	
		startFilterTimestamp.add(Calendar.DAY_OF_YEAR, startOffset);
		endFilterTimestamp.add(Calendar.DAY_OF_YEAR, endOffset);
	}	
	
	void onPrintLabels()
	{
		if(sortTable.getSelectedRowCount() > 0)	 //Print selected rows. If no rows selected, do nothing
		{
			AveryGiftLabelPrinter awlp;
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
					awlp = new AveryGiftLabelPrinter(stAL, sortTable, sortTable.getSelectedRowCount(), labelPos);
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
				awlp = new AveryGiftLabelPrinter(stAL, sortTable, sortTable.getSelectedRowCount(), new Point(0,0));
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
			(!startFilterTimestamp.getTime().equals(ds.getDate()) || !endFilterTimestamp.getTime().equals(de.getDate())))
		{
			setDateFilters(ds.getCalendar(), de.getCalendar(), 0, 1);
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
			FamilyHistory fh = stAL.get(sortTable.getSelectedRow()).getFamilyHistory();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getChild();
			ONCChildGift cg = stAL.get(sortTable.getSelectedRow()).getChildGift();
			fireEntitySelected(this, EntityType.GIFT, fam, fh, child);
			
			//determine if a partner has been assigned for the selected gift
			int childWishAssigneeID = cg.getPartnerID();
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
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.GIFT, EntityType.PARTNER);
	}
	
	protected enum GiftType { Child, Cloned; }
	
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
			float thickness = 1.5f;
			Stroke oldStroke = g2d.getStroke();
			g2d.setStroke(new BasicStroke(thickness));
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

		void printReceivingSheetBody(int x, int y, ArrayList<SortGiftObject> stAL, int index, int linesonpage,
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
		    cFonts[0] = new Font("Times New Roman", Font.PLAIN, 12);//Instructions Font
		    cFonts[1] = new Font("Times New Roman", Font.BOLD, 14);	//Season Font
		    cFonts[2] = new Font("Times New Roman", Font.BOLD, 20);	//ONC Num Text Font
		    cFonts[3] = new Font("Times New Roman", Font.BOLD, 12);	//Child Font
		    cFonts[4] = new Font("Times New Roman", Font.BOLD, 13);	//Footer Font
			
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
	
	private class ONCSortItemTableListComparator implements Comparator<SortGiftObject>
	{
		/*****
		 * Sorts table list by ONC number, then by age, then by gift number. ONC number is sorted smallest to
		 * largest, age is sorted oldest to youngest and gift number is sorted smallest to largest.
		 */
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			try
			{
				Integer onc1 = Integer.parseInt(o1.getFamily().getONCNum());
				Integer onc2 = Integer.parseInt(o2.getFamily().getONCNum());
				int oncComp = onc1.compareTo(onc2);
			
				if(oncComp != 0)
					return oncComp;
				else
				{
					Integer age1 = o1.getChild().getChildIntegerAge();
					Integer age2 = o2.getChild().getChildIntegerAge();
					int ageComp = age2.compareTo(age1);
					
					if(ageComp != 0)
						return ageComp;
					else
					{
						Integer giftNum1 = o1.getChildGift().getGiftNumber();
						Integer giftNum2 = o2.getChildGift().getGiftNumber();
						return giftNum1.compareTo(giftNum2);
					}		
				}
			}
			catch (NumberFormatException e)
			{
				return 10;	//ONC number is not an Integer, put it at the bottom
			}
		}
	}

	private class ONCSortItemAgeComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChild().getChildDOB().compareTo(o2.getChild().getChildDOB());
		}
	}
	
	private class ONCSortItemFamNumComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
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
	
	private class ONCSortItemFamilyDNSComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{	
			DNSCode fam1Code = dnsCodeDB.getDNSCode(o1.getFamilyHistory().getDNSCode());
			DNSCode fam2Code = dnsCodeDB.getDNSCode(o2.getFamilyHistory().getDNSCode());
			
			if(fam1Code.getID() == -1 && fam2Code.getID() == -1)
				return 0;
			else if(fam1Code.getID() == -1 && fam2Code.getID() > -1)
				return 1;
			else if(fam1Code.getID() > -1 && fam2Code.getID() == -1)
				return -1;
			else
				return fam1Code.getAcronym().compareTo(fam2Code.getAcronym());
		}
	}
	
	private class ONCSortItemFamilyRegionComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			Integer o1Reg = (Integer) o1.getFamily().getRegion();
			Integer o2Reg = (Integer) o2.getFamily().getRegion();
			return o1Reg.compareTo(o2Reg);
		}
	}
	
	private class ONCSortItemGenderComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChild().getChildGender().compareTo(o2.getChild().getChildGender());
		}
	}
	
	private class ONCSortItemSchoolComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChild().getChildSchool().compareTo(o2.getChild().getChildSchool());
		}
	}
	
	private class ONCSortItemGiftNumComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			Integer gn1 = o1.getChildGift().getGiftNumber();
			Integer gn2 = o2.getChildGift().getGiftNumber();
			return gn1.compareTo (gn2);
		}
	}
	
	private class ONCSortItemGiftBaseComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			GiftCatalogDB cat = GiftCatalogDB.getInstance();

			String base1, base2;
			if(o1.getChildGift().getCatalogGiftID() == -1)
				base1 = "None";
			else
				base1 = cat.getGiftByID(o1.getChildGift().getCatalogGiftID()).getName();
			
			if(o2.getChildGift().getCatalogGiftID() == -1)
				base2 = "None";
			else
				base2 = cat.getGiftByID(o2.getChildGift().getCatalogGiftID()).getName();
			
			return base1.compareTo(base2);
		}
	}
	
	private class ONCSortItemGiftDetailComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChildGift().getDetail().compareTo(
					o2.getChildGift().getDetail());
		}
	}
	
	private class ONCSortItemGiftIndicatorComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			Integer ind1 = o1.getChildGift().getIndicator();
			Integer ind2 = o2.getChildGift().getIndicator();
			return ind1.compareTo(ind2);	
		}
	}
	
	private class ONCSortItemGiftStatusComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChildGift().getGiftStatus().compareTo(o2.getChildGift().getGiftStatus());
		}
	}
	
	private class ONCSortItemGiftPartnerComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			PartnerDB partnerDB = PartnerDB.getInstance();
			
			ONCChildGift g1 = o1.getChildGift();
			if(g1 == null)
				return 10;
			
			ONCPartner partner1 =  partnerDB.getPartnerByID(g1.getPartnerID());
			if(partner1 == null)
				return 10;
			
			ONCChildGift g2 = o2.getChildGift();
			if(g2 == null)
				return -10;
			
			ONCPartner partner2 =  partnerDB.getPartnerByID(g2.getPartnerID());
			if(partner2 == null)	
				return -10;
				
			return partner1.getLastName().compareTo(partner2.getLastName());
		}
	}
	
	private class ONCSortItemGiftChangedByComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChildGift().getChangedBy().compareTo(
					o2.getChildGift().getChangedBy());
		}
	}
	
	private class ONCSortItemGiftDateChangedComparator implements Comparator<SortGiftObject>
	{
		@Override
		public int compare(SortGiftObject o1, SortGiftObject o2)
		{
			return o1.getChildGift().getDateChanged().compareTo(
					o2.getChildGift().getDateChanged());
		}
	}

	@Override
	protected String[] getTableRow(ONCObject o) 
	{
		SortGiftObject sgo = (SortGiftObject) o;
		
		String[] indicator = {"", "*", "#"};
		ONCGift gift = giftCat.getGiftByID(sgo.getChildGift().getCatalogGiftID());
		String giftName = gift == null ? "None" : gift.getName();
		ONCPartner partner = partnerDB.getPartnerByID(sgo.getChildGift().getPartnerID());
		String partnerName = partner != null ? partner.getLastName() : "";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(sgo.getChildGift().getDateChanged().getTime());
		String dnsAcronym = "";
		if(sgo.getFamilyHistory().getDNSCode() > -1)
			dnsAcronym = dnsCodeDB.getDNSCode(sgo.getFamilyHistory().getDNSCode()).getAcronym();
		String[] tablerow = {
							sgo.getFamily().getONCNum(),
							dnsAcronym,
							regions.getRegionID(sgo.getFamily().getRegion()),
							sgo.getChild().getChildAge().split("old", 2)[0].trim(), //Take the word "old" out of string
							sgo.getChild().getChildGender(),
							sgo.getChild().getChildSchool(),
							Integer.toString(sgo.getChildGift().getGiftNumber()+1),
							giftName,
							sgo.getChildGift().getDetail(),
							indicator[sgo.getChildGift().getIndicator()],
							sgo.getChildGift().getGiftStatus().toString(), 
							partnerName, 
							sgo.getChildGift().getChangedBy(), ds};
		
//		System.out.println(String.format("SortGiftsBaseDlg.getTableRow: child gift id= %d, status=%d", 
//				sgo.getChildGift().getID(), sgo.getChildGift().getGiftStatus().statusIndex()));
		return tablerow;
	}

	@Override
	void initializeFilters() 
	{
	}
}
