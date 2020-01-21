package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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

public class SortMealsDialog extends ChangeDialog implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Integer MAXIMUM_ON_NUMBER = 9999;

	private MealDB mealDB;
	private PartnerDB orgs;
	protected RegionDB regions;

	private ArrayList<SortMealObject> stAL;

	
	private JComboBox<MealType> typeCB;
	private JComboBox<ONCPartner> assignCB;
	private JComboBox<String> batchCB;
	private JComboBox<FamilyStatus> familyStatusCB;
	private JComboBox<MealStatus> mealStatusCB;
	private JComboBox<String> changedByCB;
	private JComboBox<String> regionCB;
	private JComboBox<MealStatus> changeMealStatusCB;
	private JComboBox<ONCPartner> changeAssigneeCB;
	private JComboBox<String> printCB;
	private JComboBox<String> exportCB;
	
	private DefaultComboBoxModel<ONCPartner> assignCBM;
	private DefaultComboBoxModel<ONCPartner> changeAssigneeCBM;
	private DefaultComboBoxModel<String> changedByCBM;
	private DefaultComboBoxModel<String> regionCBM;
	
	private JTextField oncnumTF;
	private JDateChooser ds, de;
	private Calendar startFilterTimestamp, endFilterTimestamp;
	
	private int sortBatchNum = 0, sortChangedBy = 0, sortAssigneeID = 0, sortRegion = 0;
	private MealStatus sortMealStatus = MealStatus.Any;
	private FamilyStatus sortFamilyStatus = FamilyStatus.Any;
	private MealType sortType = MealType.Any;

	private String[] exportChoices = {"Export Data", "2016 WFCM Format", "2016 WFCM Format+", "2015 WFCM Format"};
	
	SortMealsDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Meal Management");
		
		//set up the data base references. Family data base reference is inherited.
		mealDB = MealDB.getInstance();
		orgs = PartnerDB.getInstance();
		regions = RegionDB.getInstance();
		
		//set up data base listeners
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		if(mealDB != null)
			mealDB.addDatabaseListener(this);
		if(orgs != null)
			orgs.addDatabaseListener(this);
		if(regions != null)
			regions.addDatabaseListener(this);
		if(gvs != null)
			gvs.addDatabaseListener(this);
		
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
		
		String[] batchNums = {"Any","B-01","B-02","B-03","B-04","B-05","B-06","B-07","B-08",
				"B-09","B-10", "B-CR", "B-DI"};
		batchCB = new JComboBox<String>(batchNums);
		batchCB.setBorder(BorderFactory.createTitledBorder("Batch #"));
		batchCB.addActionListener(this);
		
		familyStatusCB = new JComboBox<FamilyStatus>(FamilyStatus.getSearchFilterList());
		familyStatusCB.setPreferredSize(new Dimension(144, 56));
		familyStatusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
		familyStatusCB.addActionListener(this);
		
		mealStatusCB = new JComboBox<MealStatus>(MealStatus.getSearchFilterList());
		mealStatusCB.setPreferredSize(new Dimension(220, 56));
		mealStatusCB.setBorder(BorderFactory.createTitledBorder("Meal Status"));
		mealStatusCB.addActionListener(this);
		
		typeCB = new JComboBox<MealType>(MealType.getSearchFilterList());
		typeCB.setPreferredSize(new Dimension(156, 56));
		typeCB.setBorder(BorderFactory.createTitledBorder("Holiday Requested"));
		typeCB.addActionListener(this);
		
		regionCBM = new DefaultComboBoxModel<String>();
		regionCBM.addElement("Any");
		regionCB = new JComboBox<String>();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.addActionListener(this);
		
		assignCB = new JComboBox<ONCPartner>();
		assignCBM = new DefaultComboBoxModel<ONCPartner>();
	    assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
	    assignCBM.addElement(new ONCPartner(-1, "None", "None"));
	    assignCB.setModel(assignCBM);
		assignCB.setPreferredSize(new Dimension(192, 56));
		assignCB.setBorder(BorderFactory.createTitledBorder("Meal Assigned To"));
		assignCB.addActionListener(this);

		changedByCB = new JComboBox<String>();
		changedByCBM = new DefaultComboBoxModel<String>();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
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

		sortCriteriaPanelTop.add(oncnumTF);
		sortCriteriaPanelTop.add(batchCB);
		sortCriteriaPanelTop.add(regionCB);
		sortCriteriaPanelTop.add(familyStatusCB);
		sortCriteriaPanelTop.add(mealStatusCB);
		sortCriteriaPanelTop.add(typeCB);
		sortCriteriaPanelBottom.add(assignCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(ds);
		sortCriteriaPanelBottom.add(de);
		
		//set up the change panel, which consists of the item count panel and the 
		//change data panel
		itemCountPanel.setBorder(BorderFactory.createTitledBorder("Meals Meeting Criteria"));
		
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Meal Status or Meal Assignee"));
        
		changeMealStatusCB = new JComboBox<MealStatus>(MealStatus.getChangeList());
        changeMealStatusCB.setPreferredSize(new Dimension(224, 56));
		changeMealStatusCB.setBorder(BorderFactory.createTitledBorder("Change Status To:"));
		changeMealStatusCB.addActionListener(this);
		
        changeAssigneeCB = new JComboBox<ONCPartner>();
        changeAssigneeCBM = new DefaultComboBoxModel<ONCPartner>();
	    changeAssigneeCBM.addElement(new ONCPartner(0, "No Change", "No Change"));
	    changeAssigneeCBM.addElement(new ONCPartner(-1, "None", "None"));
        changeAssigneeCB.setModel(changeAssigneeCBM);
        changeAssigneeCB.setPreferredSize(new Dimension(192, 56));
		changeAssigneeCB.setBorder(BorderFactory.createTitledBorder("Change Assignee To:"));
		changeAssigneeCB.addActionListener(this);
		
		changeDataPanel.add(changeMealStatusCB);
		changeDataPanel.add(changeAssigneeCB);
		
		gbc.gridx = 1;
	    gbc.ipadx = 0;
	    gbc.weightx = 1.0;
	    changePanel.add(changeDataPanel, gbc);

	    //set up the dialog defined control panel that is added to the bottom
	    //panel which uses Border Layout
	    JPanel cntlPanel = new JPanel();
      	cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setEnabled(false);
        exportCB.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(true);
        printCB.addActionListener(this);
        
//      btnApplyChanges = new JButton("Apply Changes");
//      btnApplyChanges.setEnabled(false);
//      btnApplyChanges.addActionListener(this);
        
        //Add the components to the control panel
        cntlPanel.add(exportCB);
        cntlPanel.add(printCB);
 
        //add the bottom two panels to the dialog and pack     
        bottomPanel.add(cntlPanel, BorderLayout.CENTER);
        
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
			//Must be a valid family, meaning has a numeric ONC number, requested a meal, the
			//family has been verified, and either has no DNSCode or has a Food Only DNS code. 
			//(Not a DUP, SA, etc family)
			if(isNumeric(f.getONCNum()) && f.getMealID() > -1 && doesONCNumMatch(f.getONCNum()) &&
			   (f.getDNSCode()==-1 || f.getDNSCode()==DNSCode.FOOD_ONLY || f.getDNSCode()==DNSCode.WAITLIST) &&
				doesFamilyStatusMatch(f.getFamilyStatus()))		
			{
				ONCMeal m = mealDB.getMeal(f.getMealID());
				if(m != null && doesBatchNumMatch(f.getBatchNum()) && 
								 doesTypeMatch(m.getType()) &&
								  doesMealStatusMatch(m.getStatus()) &&
								   doesRegionMatch(f.getRegion()) &&
								    doesAssigneeMatch(m.getPartnerID()) &&
								     isMealChangeDateBetween(m.getTimestamp()) &&
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
	private boolean doesBatchNumMatch(String bn) {return sortBatchNum == 0 ||  bn.equals((String)batchCB.getSelectedItem());}
	private boolean doesTypeMatch(MealType mt){return sortType == MealType.Any || sortType.compareTo(mt) == 0;}
	private boolean doesFamilyStatusMatch(FamilyStatus fs){return sortFamilyStatus == FamilyStatus.Any || sortFamilyStatus.compareTo(fs) == 0;}
	private boolean doesMealStatusMatch(MealStatus ms){return sortMealStatus == MealStatus.Any || sortMealStatus.compareTo(ms) == 0;}
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	private boolean doesAssigneeMatch(int assigneeID)
	{	
		return sortAssigneeID == 0 || sortAssigneeID == assigneeID;
	}
	
	private boolean isMealChangeDateBetween(Long timestamp)
	{
		return timestamp >= startFilterTimestamp.getTimeInMillis() && timestamp <= endFilterTimestamp.getTimeInMillis();
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
			currentAssigneeID = ((ONCPartner)assignCB.getSelectedItem()).getID();
		
		if(changeAssigneeCB.getSelectedIndex() > 1)
			currentChangeAssigneeID = ((ONCPartner)changeAssigneeCB.getSelectedItem()).getID();
		
		assignCB.setSelectedIndex(0);
		sortAssigneeID = 0;
		changeAssigneeCB.setSelectedIndex(0);
		
		assignCBM.removeAllElements();
		changeAssigneeCBM.removeAllElements();
		
		assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
		assignCBM.addElement(new ONCPartner(-1, "None", "None"));
		changeAssigneeCBM.addElement(new ONCPartner(-1, "No_Change", "No_Change"));
		changeAssigneeCBM.addElement(new ONCPartner(-1, "None", "None"));
		
		for(ONCPartner confOrg :orgs.getConfirmedPartnerList(GiftCollectionType.Meals))
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
			ONCPartner assigneeOrg = orgs.getPartnerByID(currentAssigneeID);
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
			ONCPartner changeAssigneeOrg = orgs.getPartnerByID(currentChangeAssigneeID);
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
		
		startFilterTimestamp.add(Calendar.DAY_OF_YEAR, startOffset);
		endFilterTimestamp.add(Calendar.DAY_OF_YEAR, endOffset);
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
					"No meals in table to print, table must contain meals in order to" + 
					"print listing", 
					"No Meals To Print", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
	void on2015ExportRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"Referring Agent Name", "Referring Organization", "Referring Agent Title",
    						"Sponsor Contact Name", "Client Family", "Head of Household", "Family Members",
    						"Referring Agent Email", "Client Family Email",	"Client Family Phone",
    						"Referring Agent Phone", "Dietary Restrictions", "School(s) Attended",
    						"Details", "Target Contact ID", "Delivery Street Address",
    						"Delivery Address Line 2",	"Delivery Address Line 3", "Delivery City",
    						"Delivery Zip Code", "Delivery State/Province", "Donor Type", "Adopted for:",
    						"Number of Adults in Household", "Number of Children in Household",
    						"Does the family speak English?",	"If No. Language spoken",
    						"Client has transportation to pick up holiday assistance if necessary"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected meals" ,
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
						sortTable.getSelectedRowCount() + " meals sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
       	
       	exportCB.setSelectedIndex(0);
	}
	
	void on2016ExportRequested(int choice)
	{
		//Write the selected row data to a .csv file
		List<String> headerList = new ArrayList<String>();
		headerList.add("Client ID");
		headerList.add("First Name");
		headerList.add("Last Name");
		headerList.add("Client Home Phone #");
		headerList.add("Client Celluar Phone #");
		headerList.add("Number");
		headerList.add("Street");
		headerList.add("Apt");
		headerList.add("City");
		headerList.add("ZipCode");
		headerList.add("Referring School Name");
		headerList.add("Language");
		headerList.add("Trans");
		headerList.add("Count of Adults");
		headerList.add("Count of Children");
		headerList.add("CTotal");
		
		if(choice == 2)	//requested 2016 WFCM format +
		{
			headerList.add("Requested For");
			headerList.add("Dietary Restrictions");
			headerList.add("Family Details");
		}
		
		String[] header = headerList.toArray(new String[0]);
			
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected meals" ,
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
	    	    		writer.writeNext(stAL.get(index).get2016ExportRow(choice));
	    	    }
	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(this, 
						sortTable.getSelectedRowCount() + " meals sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
       	
       	exportCB.setSelectedIndex(0);
	}

	@Override
	String[] getColumnToolTips()
	{
		String[] toolTips = {"ONC Family Number", "Batch Number", "HoH Last Name", "Region", "Which holiday is meal for?",
				"What is the status of the request?",
				"Who is providing the meal to the family?", 
				"User who last changed meal request", 
				"Date & Time Meal Last Changed"};
		return toolTips;
	}

	@Override
	String[] getColumnNames()
	{
		String[] columns = {"ONC", "Batch", "Last Name", "Reg", "Family Status", "Meal Status",
							"Holiday Requested", "Assignee", "Changed By", "Time Stamp"};
		return columns;
	}

	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {40, 40, 80, 28, 80, 80, 160, 144, 96, 92};
		return colWidths;
	}

	@Override
	int[] getCenteredColumns() 
	{
		int[] center_cols = {3};
		return center_cols;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList(false);
		}
		else if(e.getSource() == batchCB && batchCB.getSelectedIndex() != sortBatchNum)
		{
			sortBatchNum = batchCB.getSelectedIndex();
			buildTableList(false);			
		}		
		else if(e.getSource() == typeCB && typeCB.getSelectedItem() != sortType)
		{						
			sortType = (MealType) typeCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == familyStatusCB && familyStatusCB.getSelectedItem() != sortFamilyStatus )
		{						
			sortFamilyStatus = (FamilyStatus) familyStatusCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == mealStatusCB && mealStatusCB.getSelectedItem() != sortMealStatus )
		{						
			sortMealStatus = (MealStatus) mealStatusCB.getSelectedItem();
			buildTableList(false);
		}
		else if(e.getSource() == regionCB && regionCB.getSelectedIndex() != sortRegion)
		{
			sortRegion = regionCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == assignCB && !bIgnoreCBEvents && 
				((ONCPartner)assignCB.getSelectedItem()).getID() != sortAssigneeID )
		{						
			sortAssigneeID = ((ONCPartner)assignCB.getSelectedItem()).getID();
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
			if(printCB.getSelectedIndex() == 1)	//Can always print listing
			{
				onPrintListing("ONC Meals");
			} 
		}
		else if(e.getSource() == exportCB)
		{
			if(exportCB.getSelectedItem().toString().equals(exportChoices[1]))
				on2016ExportRequested(1);
			else if(exportCB.getSelectedItem().toString().equals(exportChoices[2]))
				on2016ExportRequested(2);
			else if(exportCB.getSelectedItem().toString().equals(exportChoices[3]))
				on2015ExportRequested();
		}
		else if(!bIgnoreCBEvents && (e.getSource() == changeAssigneeCB))
		{
			//first, enforce mutual exclusivity between status and assignee change. Can't
			//change status if you are changing partner.
			if(changeAssigneeCB.getSelectedIndex() == 0)
				changeMealStatusCB.setEnabled(true);
			else
			{
				changeMealStatusCB.setSelectedIndex(0);
				changeMealStatusCB.setEnabled(false);
			}
			
			checkApplyChangesEnabled();
		}
		else if(!bIgnoreCBEvents && (e.getSource() == changeMealStatusCB))
		{
			//first, enforce mutual exclusivity between status and assignee change. Can't
			//change status if you are changing partner. 
			if(changeMealStatusCB.getSelectedIndex() == 0)
				changeAssigneeCB.setEnabled(true);
			else
			{
				changeAssigneeCB.setSelectedIndex(0);
				changeAssigneeCB.setEnabled(false);
			}
			
			checkApplyChangesEnabled();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
//		System.out.println(String.format("SortMealsDlg.dataChanged: dbe type = %s", dbe.getType()));
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_MEAL") ||
										dbe.getType().equals("UPDATED_MEAL") ||
										dbe.getType().equals("DELETED_MEAL") ||
										dbe.getType().equals("ADDED_FAMILY") ||
										dbe.getType().equals("UPDATED_FAMILY") ||
										dbe.getType().equals("DELETED_FAMILY")))
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
			String[] regList = (String[]) dbe.getObject1();
			updateRegionList(regList);
		}
		else if(dbe.getType().equals("LOADED_MEALS"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Meal Management", GlobalVariablesDB.getCurrentSeason()));
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
	public void propertyChange(PropertyChangeEvent pce)
	{
		///If the date has changed in either date chooser, then rebuild the sort table. Note, setting
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
			fireEntitySelected(this, EntityType.FAMILY, fam, null);
			
			ONCMeal meal = stAL.get(sortTable.getSelectedRow()).getMeal();
			//determine if a partner has been assigned for the selected meal
			int partnerID = meal.getPartnerID();
			if(partnerID > -1)
			{
				ONCPartner org = orgs.getPartnerByID(partnerID);
				fireEntitySelected(this, EntityType.PARTNER, org, null);
				
			}
			
			sortTable.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.
		setEnabledControls(true);	//Check to see if user postured to export data
	}

	@Override
	int sortTableList(int col) 
	{
		archiveTableSelections(stAL);
		
		if(col == 0)
    			Collections.sort(stAL, new SortItemFamNumComparator());
		else if(col == 1)	// Sort on Child's Age
    			Collections.sort(stAL, new SortItemBatchNumComparator());
		else if(col == 2)	// Sort on Child's Age
    			Collections.sort(stAL, new SortItemFamilyLNComparator());
    		else if(col == 3)
    			Collections.sort(stAL, new SortItemRegionComparator());
    		else if(col == 4)
    			Collections.sort(stAL, new SortItemFamilyStatusComparator());
    		else if(col == 5)
    			Collections.sort(stAL, new SortItemMealStatusComparator());
    		else if(col == 6)
    			Collections.sort(stAL, new SortItemMealTypeComparator());
    		else if(col == 7)
    			Collections.sort(stAL, new SortItemMealAssigneeComparator());
    		else if(col == 8)
    			Collections.sort(stAL, new SortItemMealChangedByComparator());
    		else if(col == 9)	
    			Collections.sort(stAL, new SortItemMealDateChangedComparator());
    		else
    			col = -1;
		
		if(col > -1)
			displaySortTable(stAL, false, tableRowSelectedObjectList);
    		
    	return col;
	}

	@Override
	void setEnabledControls(boolean tf) 
	{
		exportCB.setEnabled(tf == true && sortTable.getSelectedRowCount() > 0);
	}

	@Override
	Object[] getTableRow(ONCObject o) 
	{
		SortMealObject smo = (SortMealObject) o;
		
		ONCPartner partner = orgs.getPartnerByID(smo.getMeal().getPartnerID());
		String partnerName = partner != null ? partner.getLastName() : "None";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(smo.getMeal().getTimestampDate().getTime());
		String[] tablerow = {smo.getFamily().getONCNum(),
							smo.getFamily().getBatchNum(),
							smo.getFamily().getLastName(),
							regions.getRegionID(smo.getFamily().getRegion()),
							smo.getFamily().getFamilyStatus().toString(),
							smo.getMeal().getStatus().toString(),
							smo.getMeal().getType().toString(),
							partnerName, smo.getMeal().getChangedBy(), ds};
		return tablerow;
	}

	@Override
	void checkApplyChangesEnabled()
	{
		//check if should enable ApplyChanges button. Enable if one or more rows are selected
		//and either the change status or change assignee selection indexes are not 0 (No Change)
		if(sortTable.getSelectedRows().length > 0 && 
		   (changeAssigneeCB.getSelectedIndex() > 0 || changeMealStatusCB.getSelectedIndex() > 0))
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}

	/**
	 * Can assign meals, change assignee or remove assignees here. If an assignee change causes
	 * a change in status that is handled at the server and returned when the meal update occurs.
	 * So, all that's done here is to update the meal status. Can also change status here, but only
	 * if a meal is already assigned and has attained MealStatus = Assigned. Then, can change 
	 * status to Referred, Thanksgiving_Confirmed, December_Confirmed or Both_Confirmed based on feedback
	 * from partner providers.
	 */	
	@Override
	boolean onApplyChanges() 
	{
		boolean bChangesMade = false;
		int[] row_sel = sortTable.getSelectedRows();
		
		List<ONCMeal> addMealReqList = new ArrayList<ONCMeal>();
		
		for(int i=0; i < row_sel.length; i++)	
		{
			ONCPartner cbPartner = (ONCPartner) changeAssigneeCB.getSelectedItem();
				
			//is it a change to either meal status or meal partner?  Can only be a change to one or the
			//other, can't be both, that's not allowed and is prevented when the action listener for either
			//combo box is triggered. If it is a change to meal parter, create and send an add meal request
			//to the server. The server will determine the meal status accordingly. Meals are not updated, 
			//meal history is retained. If it is a change to meal status, create and send an add meal request
			//for the family
			if(changeAssigneeCB.getSelectedIndex() > 0 && 
				stAL.get(row_sel[i]).getMeal().getPartnerID() != cbPartner.getID())		
			{
				ONCMeal addMealReq = new ONCMeal(-1, stAL.get(row_sel[i]).getMeal().getFamilyID(),
										stAL.get(row_sel[i]).getMeal().getStatus(),
										stAL.get(row_sel[i]).getMeal().getType(),
										stAL.get(row_sel[i]).getMeal().getRestricitons(), 
										cbPartner.getID(),userDB.getUserLNFI(),
										System.currentTimeMillis(),
										stAL.get(row_sel[i]).getMeal().getStoplightPos(),
										"Changed Partner", userDB.getUserLNFI());
				
				addMealReqList.add(addMealReq);
				
//				ONCMeal addedMeal = mealDB.add(this, addMealReq);
//				
//				if(addedMeal != null)
//					bChangesMade = true;
			}
			//check if a change to meal status. Can only change meal status if current meal status has
			//attained at least 'Assigned' status and not yet referred plus, it's an actual change to 
			//the family's meal status
			else if(changeMealStatusCB.getSelectedIndex() > 0 && 
					 stAL.get(row_sel[i]).getMeal().getStatus().compareTo(MealStatus.Assigned) >= 0 &&
					  stAL.get(row_sel[i]).getMeal().getStatus() != changeMealStatusCB.getSelectedItem())
			{
				ONCMeal addMealReq = new ONCMeal(-1, stAL.get(row_sel[i]).getMeal().getFamilyID(),
													(MealStatus) changeMealStatusCB.getSelectedItem(),
													stAL.get(row_sel[i]).getMeal().getType(),
													stAL.get(row_sel[i]).getMeal().getRestricitons(), 
													stAL.get(row_sel[i]).getMeal().getPartnerID(),
													userDB.getUserLNFI(), System.currentTimeMillis(),
													stAL.get(row_sel[i]).getMeal().getStoplightPos(),
													"Changed Status", userDB.getUserLNFI());
				
				addMealReqList.add(addMealReq);

//				ONCMeal addedMeal = mealDB.add(this, addMealReq);
//
//				if(addedMeal != null)
//					bChangesMade = true;
			}
		}
		
		//if the addMealReq list has meals to be added, request the server do so.
		if(!addMealReqList.isEmpty())
		{
			String response = mealDB.addMealList(this, addMealReqList);
			if(response.startsWith("ADDED_LIST_MEALS"))
			{	
				buildTableList(false);
				bChangesMade = true;
			}
			else
			{
				buildTableList(true);
				//display an error message that update request failed
				GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
				JOptionPane.showMessageDialog(this, "ONC Server denied Family Update," +
						"try again later","Family Update Failed",  JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
		
		//Reset the change combo boxes to "No Change and disable the ApplyChanges buttton
		changeAssigneeCB.setSelectedIndex(0);
		changeMealStatusCB.setSelectedIndex(0);
		btnApplyChanges.setEnabled(false);
		
		return bChangesMade;
	}

	@Override
	void onResetCriteriaClicked() 
	{
		oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
		sortONCNum = "";
		
		batchCB.removeActionListener(this);
		batchCB.setSelectedIndex(0);
		sortBatchNum = 0;
		batchCB.addActionListener(this);
		
		typeCB.removeActionListener(this);
		typeCB.setSelectedIndex(0);	//no need to test for a change here.
		sortType = MealType.Any;
		typeCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		familyStatusCB.removeActionListener(this);
		familyStatusCB.setSelectedIndex(0);
		sortFamilyStatus = FamilyStatus.Any;
		familyStatusCB.addActionListener(this);
		
		mealStatusCB.removeActionListener(this);
		mealStatusCB.setSelectedIndex(0);
		sortMealStatus = MealStatus.Any;
		mealStatusCB.addActionListener(this);
		
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
		changeAssigneeCB.setEnabled(true);
		changeAssigneeCB.addActionListener(this);
		
		changeMealStatusCB.removeActionListener(this);
		changeMealStatusCB.setSelectedIndex(0);
		changeMealStatusCB.setEnabled(true);
		changeMealStatusCB.addActionListener(this);
		
		de.getDateEditor().removePropertyChangeListener(this);
		ds.getDateEditor().removePropertyChangeListener(this);
		setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
		ds.setCalendar(startFilterTimestamp);
		de.setCalendar(endFilterTimestamp);
		ds.getDateEditor().addPropertyChangeListener(this);
		de.getDateEditor().addPropertyChangeListener(this);
			
		buildTableList(false);
	}

	@Override
	boolean isONCNumContainerEmpty() { return oncnumTF.getText().isEmpty(); }
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY, EntityType.PARTNER);
	}
	
	private class SortMealObject extends ONCObject
	{
		private ONCFamily	soFamily;
		private ONCMeal	 	soMeal;
		
		PartnerDB partnerDB;
		
		public SortMealObject(int itemID, ONCFamily fam, ONCMeal meal) 
		{
			super(itemID);
			soFamily = fam;
			soMeal = meal;
			
			partnerDB = PartnerDB.getInstance();
		}
		
		//getters
		ONCFamily getFamily() { return soFamily; }
		ONCMeal getMeal() { return soMeal; }
		
		public String[] getExportRow()
		{
			ONCUser user = userDB.getUser(soFamily.getAgentID());
			ONCPartner partner = partnerDB.getPartnerByID(soMeal.getPartnerID());
			
			String delAddress, unit, city, zip;
			if(soFamily.getSubstituteDeliveryAddress().isEmpty())
			{
				delAddress = soFamily.getHouseNum() + " " + soFamily.getStreet();
				unit = soFamily.getUnit();
				city = soFamily.getCity();
				zip = soFamily.getZipCode();
			}
			else
			{
				String[] parts = soFamily.getSubstituteDeliveryAddress().split("_");
				delAddress = parts[0] + " " + parts[1];
				unit = parts[2].equals("None")  ? "" : parts[2];
				city = parts[3];
				zip = parts[4];
			}
			
			AdultDB adultDB = AdultDB.getInstance();
			ChildDB childDB = ChildDB.getInstance();
			
			String famMembers = "";
			List<ONCChild> famChildren = childDB.getChildren(soFamily.getID());
			List<ONCAdult> famAdults = adultDB.getAdultsInFamily(soFamily.getID());
			
			if(!famChildren.isEmpty())
			{
				StringBuilder members = new StringBuilder(buildChildString(famChildren.get(0)));
				for(int cn=1; cn<famChildren.size(); cn++)
					members.append("\r" + buildChildString(famChildren.get(cn)));
				
				for(int an=0; an<famAdults.size(); an++)
					members.append("\r" + buildAdultString(famAdults.get(an)));
				
				famMembers = members.toString();
			}
			else if(!famAdults.isEmpty())
			{
				StringBuilder members = new StringBuilder(buildAdultString(famAdults.get(0)));
				for(int an=0; an<famAdults.size(); an++)
					members.append("\r" + buildAdultString(famAdults.get(an)));
				
				famMembers = members.toString();
			}
			
			String[] exportRow = { user.getLastName(), user.getOrganization(), user.getTitle(),
									partner != null ? partner.getLastName() : "",
									soFamily.getLastName() + " Household",
									soFamily.getFirstName() + " " + soFamily.getLastName(),
									famMembers,
									user.getEmail(), soFamily.getEmail(),
									soFamily.getAllPhoneNumbers().replaceAll("\n","\r"),
									user.getCellPhone(), soMeal.getRestricitons(), soFamily.getSchools(),
									soFamily.getDetails(), soFamily.getReferenceNum(),
									delAddress, unit, "", city, zip, "Virginia", "CBO",
									soMeal.getType().toString(),
									Integer.toString(adultDB.getNumberOfOtherAdultsInFamily(soFamily.getID())+1),
									Integer.toString(childDB.getNumberOfChildrenInFamily(soFamily.getID())),
									soFamily.getLanguage().equalsIgnoreCase("english") ? "Y" : "N",
									soFamily.getLanguage().equalsIgnoreCase("english") ? "" : soFamily.getLanguage(),
									soFamily.getTransportation().toString()};

			return exportRow;
		}
		
		public String[] get2016ExportRow(int choice)
		{
			ONCUser user = userDB.getUser(soFamily.getAgentID());
			
			String delStreetNum, delStreet, unit, city, zip;
			if(soFamily.getSubstituteDeliveryAddress().isEmpty())
			{
				delStreetNum = soFamily.getHouseNum();
				delStreet = soFamily.getStreet();
				unit = soFamily.getUnit();
				city = soFamily.getCity();
				zip = soFamily.getZipCode();
			}
			else
			{
				String[] parts = soFamily.getSubstituteDeliveryAddress().split("_");
				delStreetNum = parts[0];
				delStreet = parts[1];
				unit = parts[2].equals("None")  ? "" : parts[2];
				city = parts[3];
				zip = parts[4];
			}
			
			//determine 1st and 2md phones
			String firstPhone = "", secondPhone = "";
			if(soFamily.getHomePhone() != null && soFamily.getHomePhone().length() >= 10)
			{
				String[] firstPhoneParts = soFamily.getHomePhone().split("\n");
				firstPhone = firstPhoneParts[0];
			}			
			if(soFamily.getCellPhone() != null && soFamily.getCellPhone().length() >= 10)
			{
				String[] secondPhoneParts = soFamily.getCellPhone().split("\n");
				secondPhone = secondPhoneParts[0];
			}
			
			AdultDB adultDB = AdultDB.getInstance();
			ChildDB childDB = ChildDB.getInstance();
			int nAdults = adultDB.getNumberOfOtherAdultsInFamily(soFamily.getID())+1;
			int nChildren = childDB.getNumberOfChildrenInFamily(soFamily.getID());
			
			List<String> exportRowList = new ArrayList<String>();
			
			exportRowList.add("");
			exportRowList.add(soFamily.getFirstName());
			exportRowList.add(soFamily.getLastName());
			exportRowList.add(firstPhone);
			exportRowList.add(secondPhone);
			exportRowList.add(delStreetNum);
			exportRowList.add(delStreet);
			exportRowList.add(unit);
			exportRowList.add(city);
			exportRowList.add(zip);
			exportRowList.add(user.getOrganization());
			exportRowList.add(soFamily.getLanguage());
			exportRowList.add(soFamily.getTransportation().toString());
			exportRowList.add(Integer.toString(nAdults));
			exportRowList.add(Integer.toString(nChildren));
			exportRowList.add(Integer.toString(nAdults + nChildren));
			
			if(choice == 2) //2016 WFCM+ format
			{
				exportRowList.add(soMeal.getType().toString());
				exportRowList.add(soMeal.getRestricitons());
				exportRowList.add(soFamily.getDetails());
			}
			
			return exportRowList.toArray(new String[0]);
		}
		
		String buildChildString(ONCChild c)
		{
			return c.getChildFirstName() + " " + c.getChildLastName() + " - " +
							c.getChildGender() + " - " + c.getChildDOBString("yyyy-MM-dd");
		}
		
		String buildAdultString(ONCAdult a)
		{
			return a.getName() + " - " + a.getGender() + " - Adult";			
		}
	}
	
	private class SortItemFamNumComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
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
	
	private class SortItemBatchNumComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getFamily().getBatchNum().compareTo(o2.getFamily().getBatchNum());
		}
	}
	
	private class SortItemFamilyLNComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getFamily().getLastName().compareTo(o2.getFamily().getLastName());
		}
	}
	
	private class SortItemMealTypeComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getMeal().getType().compareTo(o2.getMeal().getType());
		}
	}
	
	private class SortItemFamilyStatusComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getFamily().getFamilyStatus().compareTo(o2.getFamily().getFamilyStatus());
		}
	}
	
	private class SortItemMealStatusComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getMeal().getStatus().compareTo(o2.getMeal().getStatus());
		}
	}
	
	private class SortItemRegionComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			Integer o1Reg = (Integer) o1.getFamily().getRegion();
			Integer o2Reg = (Integer) o2.getFamily().getRegion();
			return o1Reg.compareTo(o2Reg);
		}
	}
	
	private class SortItemMealAssigneeComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			PartnerDB partnerDB = PartnerDB.getInstance();
			String part1, part2;
			 
			if(o1.getMeal().getPartnerID() > -1 )
				part1 = partnerDB.getPartnerByID(o1.getMeal().getPartnerID()).getLastName();
			else
				part1 = "";
			
			if(o2.getMeal().getPartnerID() > -1)
				part2 = partnerDB.getPartnerByID(o2.getMeal().getPartnerID()).getLastName();
			else
				part2 = "";
			
			return part1.compareTo(part2);
		}
	}
	
	private class SortItemMealChangedByComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getMeal().getChangedBy().compareTo(o2.getMeal().getChangedBy());
		}
	}
	
	private class SortItemMealDateChangedComparator implements Comparator<SortMealObject>
	{
		@Override
		public int compare(SortMealObject o1, SortMealObject o2)
		{
			return o1.getMeal().getTimestampDate().compareTo(o2.getMeal().getTimestampDate());
		}
	}

	@Override
	void initializeFilters() 
	{
	
	}
}
