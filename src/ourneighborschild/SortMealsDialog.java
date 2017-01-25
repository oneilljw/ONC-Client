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
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
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
	protected ONCRegions regions;

	private ArrayList<SortMealObject> stAL;

	
	private JComboBox typeCB, assignCB, batchCB, statusCB, changedByCB, regionCB, changeStatusCB;
	private JComboBox changeAssigneeCB, printCB, exportCB;
	private DefaultComboBoxModel assignCBM, changeAssigneeCBM, changedByCBM, regionCBM;
	private JTextField oncnumTF;
	private JDateChooser ds, de;
	private Calendar sortStartCal = null, sortEndCal = null;
	
	private int sortBatchNum = 0, sortChangedBy = 0, sortAssigneeID = 0, sortRegion = 0;
	private MealStatus sortStatus = MealStatus.Any;
	private MealType sortType = MealType.Any;

	private String[] exportChoices = {"Export Data", "2016 WFCM Format", "2016 WFCM Format+", "2015 WFCM Format"};
//	private int totalNumOfLabelsToPrint;	//Holds total number of labels requested in a peint job
	
	
	SortMealsDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Meal Management");
		
		//set up the data base references. Family data base reference is inherited.
		mealDB = MealDB.getInstance();
		orgs = PartnerDB.getInstance();
		regions = ONCRegions.getInstance();
		
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
		batchCB = new JComboBox(batchNums);
		batchCB.setBorder(BorderFactory.createTitledBorder("Batch #"));
		batchCB.addActionListener(this);
		
		typeCB = new JComboBox(MealType.getSearchFilterList());
		typeCB.setPreferredSize(new Dimension(156, 56));
		typeCB.setBorder(BorderFactory.createTitledBorder("Holiday Requested"));
		typeCB.addActionListener(this);
		
		statusCB = new JComboBox(MealStatus.getSearchFilterList());
		statusCB.setPreferredSize(new Dimension(220, 56));
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
	    assignCBM.addElement(new ONCPartner(0, "Any", "Any"));
	    assignCBM.addElement(new ONCPartner(-1, "None", "None"));
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
		sortCriteriaPanelTop.add(batchCB);
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
		
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Meal Status or Meal Assignee"));
        
		changeStatusCB = new JComboBox(MealStatus.getChangeList());
        changeStatusCB.setPreferredSize(new Dimension(224, 56));
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
		
		changeDataPanel.add(changeStatusCB);
		changeDataPanel.add(changeAssigneeCB);
		
		gbc.gridx = 1;
	    gbc.ipadx = 0;
	    gbc.weightx = 1.0;
	    changePanel.add(changeDataPanel, gbc);

	    //set up the dialog defined control panel that is added to the bottom
	    //panel which uses Border Layout
	    JPanel cntlPanel = new JPanel();
      	cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        exportCB = new JComboBox(exportChoices);
        exportCB.setEnabled(false);
        exportCB.addActionListener(this);
        
        String[] printChoices = {"Print", "Print Listing"};
        printCB = new JComboBox(printChoices);
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
			if(isNumeric(f.getONCNum()) && f.getMealID() > -1 && doesONCNumMatch(f.getONCNum()))	//Must be a valid family	
			{
				ONCMeal m = mealDB.getMeal(f.getMealID());
				if(m != null && doesBatchNumMatch(f.getBatchNum()) && 
								 doesTypeMatch(m.getType()) &&
								  doesStatusMatch(m.getStatus()) &&
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
	private boolean doesBatchNumMatch(String bn) {return sortBatchNum == 0 ||  bn.equals((String)batchCB.getSelectedItem());}
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
		
		for(ONCPartner confOrg :orgs.getConfirmedPartnerList(GiftCollection.Meals))
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
	
	void setSortStartDate(Date sd) {sortStartCal.setTime(sd); ds.setDate(sortStartCal.getTime());}
	
	void setSortEndDate(Date ed) {sortEndCal.setTime(ed); sortEndCal.add(Calendar.DATE, 1); de.setDate(sortEndCal.getTime());}
	
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
		String[] columns = {"ONC", "Batch", "Last Name", "Reg", "Holiday", "Status", "Assignee", 
				"Changed By", "Time Stamp"};
		return columns;
	}

	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {40, 40, 80, 28, 80, 160, 144, 80, 92};
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
				changeStatusCB.setEnabled(true);
			else
			{
				changeStatusCB.setSelectedIndex(0);
				changeStatusCB.setEnabled(false);
			}
			
			checkApplyChangesEnabled();
		}
		else if(!bIgnoreCBEvents && (e.getSource() == changeStatusCB))
		{
			//first, enforce mutual exclusivity between status and assignee change. Can't
			//change status if you are changing partner. 
			
			if(changeStatusCB.getSelectedIndex() == 0)
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
			this.setTitle(String.format("Our Neighbor's Child - %d Meal Management", GlobalVariables.getCurrentSeason()));
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
    		Collections.sort(stAL, new SortItemMealTypeComparator());
    	else if(col == 5)
    		Collections.sort(stAL, new SortItemMealStatusComparator());
    	else if(col == 6)
    		Collections.sort(stAL, new SortItemMealAssigneeComparator());
    	else if(col == 7)
    		Collections.sort(stAL, new SortItemMealChangedByComparator());
    	else if(col == 8)	
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
		String partnerName = partner != null ? partner.getName() : "None";
		String ds = new SimpleDateFormat("MM/dd H:mm").format(smo.getMeal().getDateChanged().getTime());
		String[] tablerow = {smo.getFamily().getONCNum(),
							smo.getFamily().getBatchNum(),
							smo.getFamily().getHOHLastName(),
							regions.getRegionID(smo.getFamily().getRegion()),
							smo.getMeal().getType().toString(),
							smo.getMeal().getStatus().toString(),
							partnerName, smo.getMeal().getChangedBy(), ds};
		return tablerow;
	}

	@Override
	void checkApplyChangesEnabled()
	{
		//check if should enable ApplyChanges button. Enable if one or more rows are selected
		//and either the change status or change assignee selection indexes are not 0 (No Change)
		if(sortTable.getSelectedRows().length > 0 && 
		   (changeAssigneeCB.getSelectedIndex() > 0 || changeStatusCB.getSelectedIndex() > 0))
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}

	/**
	 * Can assign meals, change assignee or remove assignees here. If an assignee change causes
	 * a change in status that is handled at the server and returned when the meal update occurs.
	 * So, all that's done here is to update the meal status. Can also change status here, but only
	 * if a meal is already assigned and has attained MealStatus = Referred. Then, can change 
	 * status to Thanksgiving_Confirmed, December_Confirmed or Both_Confirmed based on feedback
	 * from partner providers.
	 */	
	@Override
	boolean onApplyChanges() 
	{
		boolean bChangesMade = false;
		int[] row_sel = sortTable.getSelectedRows();
		
		for(int i=0; i < row_sel.length; i++)	
		{
			ONCPartner cbPartner = (ONCPartner) changeAssigneeCB.getSelectedItem();
				
			//is it a change to either meal status or meal partner?  Can only be a change to one or the
			//other, can't be both, that's not allowed and is prevented in checkApplyChangesEnabled(). 
			//If it is a change to meal parter, create and send an add meal request to the server.
			//The server will determine the meal status accordingly.Meals are not updated, 
			//meal history is retained. If it is a change to meal status, create and send a new meal
			//for the family
			if(changeAssigneeCB.getSelectedIndex() > 0 && 
				stAL.get(row_sel[i]).getMeal().getPartnerID() != cbPartner.getID())		
			{
				ONCMeal addMealReq = new ONCMeal(-1, stAL.get(row_sel[i]).getMeal().getFamilyID(),
										stAL.get(row_sel[i]).getMeal().getStatus(),
										stAL.get(row_sel[i]).getMeal().getType(),
										stAL.get(row_sel[i]).getMeal().getRestricitons(), 
										cbPartner.getID(),userDB.getUserLNFI(),
										new Date(), stAL.get(row_sel[i]).getMeal().getStoplightPos(),
										"Changed Partner", userDB.getUserLNFI());
				
				ONCMeal addedMeal = mealDB.add(this, addMealReq);
				
				if(addedMeal != null)
					bChangesMade = true;
			}
			//check if a change to meal status. Can only change meal status if current meal status has
			//attained at least 'Assigned' status and not yet referred plus, it's an actual change to 
			//the family's meal status
			else if(changeStatusCB.getSelectedIndex() > 0 && 
					 stAL.get(row_sel[i]).getMeal().getStatus().compareTo(MealStatus.Assigned) >= 0 &&
					  stAL.get(row_sel[i]).getMeal().getStatus() != changeStatusCB.getSelectedItem())
			{
				ONCMeal addMealReq = new ONCMeal(-1, stAL.get(row_sel[i]).getMeal().getFamilyID(),
													(MealStatus) changeStatusCB.getSelectedItem(),
													stAL.get(row_sel[i]).getMeal().getType(),
													stAL.get(row_sel[i]).getMeal().getRestricitons(), 
													stAL.get(row_sel[i]).getMeal().getPartnerID(),
													userDB.getUserLNFI(), new Date(),
													stAL.get(row_sel[i]).getMeal().getStoplightPos(),
													"Changed Status", userDB.getUserLNFI());

				ONCMeal addedMeal = mealDB.add(this, addMealReq);

				if(addedMeal != null)
					bChangesMade = true;
			}
		}
		
		//Reset the change combo boxes to "No Change and disable the ApplyChanges buttton
		changeAssigneeCB.setSelectedIndex(0);
		changeStatusCB.setSelectedIndex(0);
		btnApplyChanges.setEnabled(false);
		
		//If at least one change was made, update the table
		if(bChangesMade)
			buildTableList(false);

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
		changeAssigneeCB.setEnabled(true);
		changeAssigneeCB.addActionListener(this);
		
		changeStatusCB.removeActionListener(this);
		changeStatusCB.setSelectedIndex(0);
		changeStatusCB.setEnabled(true);
		changeStatusCB.addActionListener(this);
		
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
				unit = soFamily.getUnitNum();
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
			
			String[] exportRow = { user.getLastname(), user.getOrg(), user.getTitle(),
									partner != null ? partner.getName() : "",
									soFamily.getHOHLastName() + " Household",
									soFamily.getHOHFirstName() + " " + soFamily.getHOHLastName(),
									famMembers,
									user.getEmail(), soFamily.getEmail(),
									soFamily.getAllPhoneNumbers().replaceAll("\n","\r"),
									user.getPhone(), soMeal.getRestricitons(), soFamily.getSchools(),
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
				unit = soFamily.getUnitNum();
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
			if(soFamily.getOtherPhon() != null && soFamily.getOtherPhon().length() >= 10)
			{
				String[] secondPhoneParts = soFamily.getOtherPhon().split("\n");
				secondPhone = secondPhoneParts[0];
			}
			
			AdultDB adultDB = AdultDB.getInstance();
			ChildDB childDB = ChildDB.getInstance();
			int nAdults = adultDB.getNumberOfOtherAdultsInFamily(soFamily.getID())+1;
			int nChildren = childDB.getNumberOfChildrenInFamily(soFamily.getID());
			
			List<String> exportRowList = new ArrayList<String>();
			
			exportRowList.add("");
			exportRowList.add(soFamily.getHOHFirstName());
			exportRowList.add(soFamily.getHOHLastName());
			exportRowList.add(firstPhone);
			exportRowList.add(secondPhone);
			exportRowList.add(delStreetNum);
			exportRowList.add(delStreet);
			exportRowList.add(unit);
			exportRowList.add(city);
			exportRowList.add(zip);
			exportRowList.add(user.getOrg());
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
			return o1.getFamily().getHOHLastName().compareTo(o2.getFamily().getHOHLastName());
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
				part1 = partnerDB.getPartnerByID(o1.getMeal().getPartnerID()).getName();
			else
				part1 = "";
			
			if(o2.getMeal().getPartnerID() > -1)
				part2 = partnerDB.getPartnerByID(o2.getMeal().getPartnerID()).getName();
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
			return o1.getMeal().getDateChanged().compareTo(o2.getMeal().getDateChanged());
		}
	}

	@Override
	void initializeFilters() 
	{
		setSortStartDate(gvs.getSeasonStartDate());
		setSortEndDate(gvs.getTodaysDate());
	}

}
