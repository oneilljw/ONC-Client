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
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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

public class SortGiftsDialog extends ChangeDialog implements PropertyChangeListener 												
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
	private ChildGiftDB giftDB;
	private PartnerDB partnerDB;
	private GiftCatalogDB giftCat;
	private RegionDB regions;
	private DNSCodeDB dnsCodeDB;

	private ArrayList<SortGiftObject> stAL;
	
	private JComboBox<String> startAgeCB, endAgeCB, giftnumCB, genderCB, resCB, changedByCB;
	private JComboBox<String> changeResCB, printCB, regionCB, schoolCB, exportCB;
	private JComboBox<ONCGift> giftCB;
	private JComboBox<GiftStatus>  statusCB, changeStatusCB;
	private JComboBox<ONCPartner> assignCB, changePartnerCB;
	private JComboBox<FamilyStatus> famStatusCB;
	private JComboBox<DNSCode> dnsCodeCB;
	private List<DNSCode> filterCodeList;
	
	private DefaultComboBoxModel<String> changedByCBM, regionCBM, schoolCBM;
	private DefaultComboBoxModel<DNSCode> dnsCodeCBM;
	private DefaultComboBoxModel<ONCGift> giftCBM;
	private DefaultComboBoxModel<ONCPartner> assignCBM, changePartnerCBM;
	
	private JTextField oncnumTF;
//	private JButton btnExport;
	private JDateChooser ds, de;
	private Calendar startFilterTimestamp, endFilterTimestamp;
	private JCheckBox labelFitCxBox;
	
	private int sortStartAge = 0, sortEndAge = ONC_AGE_LIMIT, sortGender = 0, sortChangedBy = 0;
	private int sortGiftNum = 0, sortRes = 0, sortPartnerID, sortRegion = 0;
	private GiftStatus sortStatus = GiftStatus.Any;
	private FamilyStatus sortFamilyStatus;
	private DNSCode sortDNSCode;
	private String sortSchool = "Any";
	private int sortGiftID = -2;
	private boolean bOversizeGifts = false;
	
	private static String[] genders = {"Any", "Boy", "Girl"};
	private static String[] res = {"Any", "Blank", "*", "#"};
	private static String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
										"Distributed", "Verified"};
//	String[] dnsCodes = {"None", "Any", "All", "DUP", "FO", "NC", "NISA", "OPT-OUT", "SA", "SBO", "WA"};
	
	SortGiftsDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Gift Management");
		
		//set up the data base references
		cDB = ChildDB.getInstance();
		giftDB = ChildGiftDB.getInstance();
		partnerDB = PartnerDB.getInstance();
		giftCat = GiftCatalogDB.getInstance();
		regions = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
		
		//set up data base listeners
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(cDB != null)
			cDB.addDatabaseListener(this);
		if(giftDB != null)
			giftDB.addDatabaseListener(this);
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
		sortDNSCode = new DNSCode(-4, "No Codes", "No Codes", "Families being served");
		filterCodeList = new ArrayList<DNSCode>();
		filterCodeList.add(sortDNSCode);
		filterCodeList.add(new DNSCode(-3, "Any", "Any", "Any"));
		filterCodeList.add(new DNSCode(-2, "All Codes", "All Codes", "All Codes"));
		
		dnsCodeCBM = new DefaultComboBoxModel<DNSCode>();
		for(DNSCode filterCode : filterCodeList)
			dnsCodeCBM.addElement(filterCode);
		dnsCodeCB = new JComboBox<DNSCode>();
		dnsCodeCB.setModel(dnsCodeCBM);
		dnsCodeCB.setPreferredSize(new Dimension(120, 56));
		dnsCodeCB.setBorder(BorderFactory.createTitledBorder("DNS Code"));
		dnsCodeCB.addActionListener(this);
		
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
		
		String[] giftnums = {"Any", "1", "2", "3"};
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
		
		statusCB = new JComboBox<GiftStatus>(GiftStatus.getSearchFilterList());
		statusCB.setPreferredSize(new Dimension(136, 56));
		statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
		statusCB.addActionListener(this);
		status[0] = "No Change"; //Change "Any" to none after sort criteria list created
		
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
        
        changeStatusCB = new JComboBox<GiftStatus>(GiftStatus.getChangeList());
        changeStatusCB.setPreferredSize(new Dimension(192, 56));
		changeStatusCB.setBorder(BorderFactory.createTitledBorder("Change Status To:"));
		changeStatusCB.addActionListener(this);
        
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
	    JPanel infoPanel = new JPanel();
	    labelFitCxBox = new JCheckBox("Overlength Ornament Labels Only");
	    labelFitCxBox.setSelected(bOversizeGifts);
	    labelFitCxBox.addActionListener(this);
	    infoPanel.add(labelFitCxBox);
	    
	    JPanel cntlPanel = new JPanel();
	    cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
   
        String[] exportChoices = {"Export", "Export Listing", "Export SignUp Genius"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setPreferredSize(new Dimension(136, 28));
        exportCB.setEnabled(true);
        exportCB.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing", "Print Labels", "Print Partner Receiving Check Sheets"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        printCB.addActionListener(this);
   
        cntlPanel.add(exportCB);
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
	}
	
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
		for(ONCFamily f:fDB.getList())
		{
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()) && doesFamilyStatusMatch(f.getFamilyStatus()) &&
				doesDNSCodeMatch(f.getDNSCode()) && doesRegionMatch(f.getRegion()))	//Must be a valid family	
			{
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c) && doesSchoolMatch(c))	//Children criteria pass
					{
						for(int i=0; i< giftDB.getNumberOfGiftsPerChild(); i++)
						{	 //Assignee, Status, Date & Wish match
							ONCChildGift cw = giftDB.getGift(c.getChildGiftID(i));
							
							if(cw != null && doesResMatch(cw.getIndicator()) &&
								doesPartnerMatch(cw.getPartnerID()) &&
								 doesStatusMatch(cw.getGiftStatus()) &&
								  isGiftChangeDateBetween(cw.getTimestamp()) &&
								   doesChangedByMatch(cw.getChangedBy()) &&
									doesGiftBaseMatch(cw.getGiftID()) &&
									 doesGiftNumMatch(i)  &&
									  !(bOversizeGifts && !isGiftOversize(cw)))//Wish criteria pass
							{
								
								stAL.add(new SortGiftObject(itemID++, f, c, cw));
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
	
	/****
	 * Builds a list of changed gift requests from the highlighted ONCChildGifts in the table. Submits the list
	 * to the local database to be sent to the server. Does some checking based on current gift status to
	 * determine if gift restrictions or gift parters may be changed. The local database determines
	 * if current gift status can be changed.
	 */
	boolean onApplyChanges()
	{
		List<AddGiftRequest> reqAddGiftList = new ArrayList<AddGiftRequest>();

		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			boolean bNewGiftrqrd = false; 	//Restriction, status or assignee change
			
			//get the prior gift
			ONCChildGift priorGift = stAL.get(row_sel[i]).getChildGift();

			//baseline request with prior gift information
			int cgi = priorGift.getIndicator();
			GiftStatus gs = priorGift.getGiftStatus();
			int partnerID = priorGift.getPartnerID();
			
			//Determine if a change to gift restrictions, if so set new gift restriction for request
			if(changeResCB.getSelectedIndex() > 0 && cgi != changeResCB.getSelectedIndex()-1)
			{
				//a change to the indicator is requested. Can only change gift restrictions
				//in certain GiftStatus
				if(gs == GiftStatus.Selected || gs == GiftStatus.Assigned
						||gs == GiftStatus.Shopping || gs == GiftStatus.Returned)
				{
					cgi = changeResCB.getSelectedIndex()-1;	//Restrictions start at 0
					bNewGiftrqrd = true;
				}
			}
			
			//Determine if a change to a partner, if so, set new partner in request
			if(changePartnerCB.getSelectedIndex() > 0 &&
					priorGift.getPartnerID() != ((ONCPartner)changePartnerCB.getSelectedItem()).getID())
			{
				//can only change partners in certain GiftState's
				if(gs == GiftStatus.Selected || gs == GiftStatus.Assigned || gs == GiftStatus.Delivered ||
					gs == GiftStatus.Shopping || gs == GiftStatus.Returned || gs == GiftStatus.Missing)
				{
					partnerID = ((ONCPartner)changePartnerCB.getSelectedItem()).getID();	//new partner ID
					bNewGiftrqrd = true;
				}
			}
			
			//Determine if a change to gift status, if so, set new status in request
			if(changeStatusCB.getSelectedIndex() > 0 && gs != changeStatusCB.getSelectedItem())
			{
				//user has requested to change the status
				GiftStatus reqStatus = (GiftStatus) changeStatusCB.getSelectedItem();
				gs = reqStatus;
				bNewGiftrqrd = true;
			}
			
			//if restriction, Status or Partner change, create gift request that includes the prior gift
			//and it's replacement.
			if(bNewGiftrqrd)	
				reqAddGiftList.add(new AddGiftRequest(priorGift, new ONCChildGift(-1, priorGift.getChildID(), priorGift.getGiftID(), priorGift.getDetail(),
						priorGift.getGiftNumber(), cgi, gs, partnerID, userDB.getUserLNFI(), System.currentTimeMillis())));	
		}
		
		if(!reqAddGiftList.isEmpty())
		{
			String response = giftDB.addGiftList(this, reqAddGiftList);
			if(response.startsWith("ADDED_GIFT_LIST"))
				buildTableList(false);
		}
		else
			buildTableList(true);
		
		//Reset the change combo boxes to "No Change"
		changeResCB.setSelectedIndex(0);
		changeStatusCB.setSelectedIndex(0);
		changePartnerCB.setSelectedIndex(0);
		
		btnApplyChanges.setEnabled(false);

		return false;
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
		
		for(ONCPartner confirmedPartner :partnerDB.getConfirmedPartnerList(GiftCollectionType.Ornament))
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
		if(sortTable.getSelectedRowCount() > 0)
			exportCB.setEnabled(true);
		else
			exportCB.setEnabled(false);
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

	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	boolean doesDNSCodeMatch(int dnsCodeID)
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
	
	private boolean isAgeInRange(ONCChild c)
	{
		return c.getChildIntegerAge() >= startAgeCB.getSelectedIndex() && c.getChildIntegerAge() <= endAgeCB.getSelectedIndex();		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	private boolean doesFamilyStatusMatch(FamilyStatus fs)
	{
		return sortFamilyStatus == FamilyStatus.Any || fs == sortFamilyStatus;
	}
	
	private boolean doesSchoolMatch(ONCChild c)
	{
		return sortSchool.equals("Any") || (c.getChildSchool().equalsIgnoreCase(sortSchool));		
	}
	
	private boolean doesGiftNumMatch(int wn)
	{
		return sortGiftNum == 0 || sortGiftNum == wn+1;		
	}
	
	private boolean doesResMatch(int res) { return sortRes == 0  || sortRes == res+1; }
	
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	private boolean doesStatusMatch(GiftStatus ws){return sortStatus == GiftStatus.Any || sortStatus.compareTo(ws) == 0;}
	
	private boolean doesPartnerMatch(int assigneeID)
	{
		//sortAssigneeID's 0 and -1 are reserved for special filters. 0 shows all gifts regardless off assignee,
		//-1 displays gifts that are not assigned
		return sortPartnerID == 0 || sortPartnerID == assigneeID;
	}
	
	private boolean isGiftChangeDateBetween(long timestamp)
	{
		return timestamp >= startFilterTimestamp.getTimeInMillis() && timestamp <= endFilterTimestamp.getTimeInMillis();
	}
			
	private boolean doesGiftBaseMatch(int giftID)	{return sortGiftID == -2 ||  sortGiftID == giftID; }
	
	private boolean doesChangedByMatch(String s) { return sortChangedBy == 0 || changedByCB.getSelectedItem().toString().equals(s); }
	
	/*************
	 * Method checks to see if a gift will fit on a label. A label has two lines to describe
	 * the gift. If the name and detail fit on one line, it's ok. If it requires two
	 * lines, need to check the 2nd line to see if it fits. If the second line is too long
	 * the method returns true. Otherwise it returns false.
	 * @param cg - ONCChildWish to check if it fits on a label
	 * @return - true if gift is too big for label, false it it fits on label
	 ******************/
	boolean isGiftOversize(ONCChildGift cg)
	{
		ONCGift goft;
		boolean bOversize = false;	//only get to true if 2nd line doesn't fit
		
		if(cg != null && bOversizeGifts && (goft=giftCat.getGiftByID(cg.getGiftID())) != null)
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
/*	
	void setSortStartDate(Long sd) 
	{
		sortStartCal = sd; 
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(sortStartCal);
		ds.setCalendar(cal);
	}
	
	void setSortEndDate(Long ed)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(ed);
		cal.add(Calendar.DATE, 1);
		
		de.setCalendar(cal);
		sortEndCal = cal.getTimeInMillis();
	}
*/	
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
		else if(e.getSource() == dnsCodeCB && sortDNSCode.getID() != ((DNSCode) dnsCodeCB.getSelectedItem()).getID())
		{
			sortDNSCode = (DNSCode) dnsCodeCB.getSelectedItem();
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
    					"No gifts selected, please selected gifts in order to " + 
    					printCB.getSelectedItem().toString(), 
    					"No Gifts Selected", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					printCB.setSelectedIndex(0);
				}
			}
		}
//		else if(e.getSource() == btnExport)
//		{
//			onExportRequested();	
//		}
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
		int[] colWidths = {40, 40, 32, 48, 36, 120, 36, 84, 180, 32, 80, 184, 96, 96};
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
	
	private void setDateFilters(Calendar start, Calendar end, int startOffset, int endOffset)
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
		
//		System.out.println(String.format("SortGiftDlg.setDateFilters: startFilterTimestampMillis= %d",
//				startFilterTimestamp.getTimeInMillis()));
		
		startFilterTimestamp.add(Calendar.DAY_OF_YEAR, startOffset);
		endFilterTimestamp.add(Calendar.DAY_OF_YEAR, endOffset);
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
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getChild();
			ONCChildGift cg = stAL.get(sortTable.getSelectedRow()).getChildGift();
			fireEntitySelected(this, EntityType.GIFT, fam, child, cg);
			
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
			updateGiftSelectionList();
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
			this.setTitle(String.format("Our Neighbor's Child - %d Gift Management", gvs.getCurrentSeason()));
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
		else if(dbe.getType().equals("LOADED_DNSCODES") || dbe.getType().contains("ADDED_DNSCODE"))
		{
			updateDNSCodeCB();
		}
		else if(dbe.getType().contains("UPDATED_DNSCODE"))
		{
			updateDNSCodeCB();
			buildTableList(true);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			de.getDateEditor().removePropertyChangeListener(this);
			ds.getDateEditor().removePropertyChangeListener(this);
			
			setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
			ds.setCalendar(startFilterTimestamp);
			de.setCalendar(endFilterTimestamp);
			
			ds.getDateEditor().addPropertyChangeListener(this);
			de.getDateEditor().addPropertyChangeListener(this);
			
			buildTableList(true);
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.GIFT, EntityType.PARTNER);
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
			DNSCode fam1Code = dnsCodeDB.getDNSCode(o1.getFamily().getDNSCode());
			DNSCode fam2Code = dnsCodeDB.getDNSCode(o2.getFamily().getDNSCode());
			
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
			if(o1.getChildGift().getGiftID() == -1)
				base1 = "None";
			else
				base1 = cat.getGiftByID(o1.getChildGift().getGiftID()).getName();
			
			if(o2.getChildGift().getGiftID() == -1)
				base2 = "None";
			else
				base2 = cat.getGiftByID(o2.getChildGift().getGiftID()).getName();
			
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
		ONCGift gift = giftCat.getGiftByID(sgo.getChildGift().getGiftID());
		String giftName = gift == null ? "None" : gift.getName();
		ONCPartner partner = partnerDB.getPartnerByID(sgo.getChildGift().getPartnerID());
		String partnerName = partner != null ? partner.getLastName() : "";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(sgo.getChildGift().getDateChanged().getTime());
		String dnsAcronym = "";
		if(sgo.getFamily().getDNSCode() > -1)
			dnsAcronym = dnsCodeDB.getDNSCode(sgo.getFamily().getDNSCode()).getAcronym();
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
		return tablerow;
	}

	@Override
	void initializeFilters() 
	{
//		setSortStartDate(gvs.getSeasonStartDate());
//		setSortEndDate(System.currentTimeMillis());
	}
}
