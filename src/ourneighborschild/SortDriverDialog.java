package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

public class SortDriverDialog extends DependantFamilyTableDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> lNameCB, changedByCB;
	private JComboBox<ImageIcon> stoplightCB;
	private DefaultComboBoxModel<String> lNameCBM, changedByCBM;
	private String sortLName, sortChangedBy;
	private int sortStoplight;
	private JComboBox<String> drvCB, exportCB, printCB;
	
	private FamilyHistoryDB familyHistoryDB;
	private VolunteerDB volunteerDB;
	private ActivityDB activityDB;
	private VolunteerActivityDB volActDB;
	private ArrayList<ONCVolunteer> atAL;	//Holds references to driver objects for driver table
//	private Activity deliveryActivity;
	
	SortDriverDialog(JFrame pf)
	{
		super(pf, 10);
		this.setTitle("Our Neighbor's Child - Delivery Volunteer Management");
		
		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		volActDB = VolunteerActivityDB.getInstance();
		if(volActDB != null)
			volActDB.addDatabaseListener(this);
		
		familyHistoryDB = FamilyHistoryDB.getInstance();
		if(familyHistoryDB != null)
			familyHistoryDB.addDatabaseListener(this);
		
		//Set up the agent table content array list
		atAL = new ArrayList<ONCVolunteer>();
		
		//Initialize the sort criteria variables. Reusing superclass sortONCNum for driver number
		sortONCNum = "Any";
		sortLName = "Any";
		sortChangedBy = "Anyone";
		sortStoplight = 0;
		
		//Set up the search criteria panel
		//Set up unique serach criteria gui
		String[] oncStrings = {"Any", "N/A"};
    		drvCB = new JComboBox<String>(oncStrings);
    		drvCB.setEditable(true);
    		drvCB.setPreferredSize(new Dimension(88,56));
		drvCB.setBorder(BorderFactory.createTitledBorder("Driver #"));
		drvCB.addActionListener(this);
		
		lNameCB = new JComboBox<String>();
		lNameCBM = new DefaultComboBoxModel<String>();
	    lNameCBM.addElement("Any");
	    lNameCB.setModel(lNameCBM);
	    lNameCB.setEditable(true);
		lNameCB.setPreferredSize(new Dimension(144, 56));
		lNameCB.setBorder(BorderFactory.createTitledBorder("Last Name"));
		lNameCB.addActionListener(this);
		
		changedByCB = new JComboBox<String>();
		changedByCBM = new DefaultComboBoxModel<String>();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setPreferredSize(new Dimension(144, 56));
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.addActionListener(this);
		
//		stoplightCB = new JComboBox(stoplt);
		stoplightCB = new JComboBox<ImageIcon>(GlobalVariablesDB.getLights());
		stoplightCB.setMaximumSize(new Dimension(80, 56));
		stoplightCB.setBorder(BorderFactory.createTitledBorder("Stoplight"));
		stoplightCB.addActionListener(this);
		
		//Add all sort criteria components to dialog pane
		sortCriteriaPanelTop.add(drvCB);
        sortCriteriaPanelTop.add(lNameCB);
        sortCriteriaPanelTop.add(changedByCB);
        sortCriteriaPanelTop.add(stoplightCB);
        sortCriteriaPanelTop.add(new JPanel());
		
        //Set up a panel and label for the driver count
        JPanel infoPanel = new JPanel();
        infoPanel.add(objectCountPanel);

		lblObjectMssg.setText("# of Delivery Partners:"); 
      
		JPanel cntlPanel = new JPanel();
      	cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      	
      	//Create a print button for delivery volunteer information
      	String[] exportChoices = {"Export", "Export Delivery Report"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setPreferredSize(new Dimension(136, 28));
        exportCB.setEnabled(false);
        exportCB.addActionListener(this);
      	
      	//Create a print button for delivery volunteer information
      	String[] agentPrintChoices = {"Print", "Print Driver Listing"};
        printCB = new JComboBox<String>(agentPrintChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
        
        cntlPanel.add(exportCB);
        cntlPanel.add(printCB);
        
        //set the border title for the family table 
        familyTableScrollPane.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLoweredBevelBorder(), "Family Deliveries Made/Attempted By Selected Delivery Partner(s)"));

        bottomPanel.add(infoPanel, BorderLayout.LINE_START);
        bottomPanel.add(cntlPanel, BorderLayout.CENTER);
        
        this.add(bottomPanel);
        this.add(familyTableScrollPane);
        this.add(lowercntlpanel);
        
        pack();
	}

	void buildFamilyTableListAndDisplay()
	{
		int[] row_sel = sortTable.getSelectedRows();
		
		stAL.clear();	//Clear the prior table data array list
		clearFamilyTable();
		
		for(int i=0; i< row_sel.length; i++)
			for(ONCFamily f:fDB.getListOfFamiliesWithDeliveries())
			{
				//determine if the family has a driver based on the delivery. If the family
				//has a driver, does the delivery driver's ID match the id of the driver selected
				//in the selection table. If so, add to the dependent table list
				FamilyHistory fh = familyHistoryDB.getLastFamilyHistory(f.getID());
				
				if(fh != null && !fh.getdDelBy().isEmpty())
				{
					//There is s driver assigned. Determine who it is from the driver number
					//and check to see if it matches the selected driver(s) in the selection table
					int index = volunteerDB.getDriverIndex(fh.getdDelBy());
					if(index > -1 && volunteerDB.getDriver(index).getDrvNum().equals(atAL.get(row_sel[i]).getDrvNum()))
						stAL.add(new ONCFamilyAndNote(i, f, fh, null));
				}	
			}
					
		lblNumOfFamilies.setText(Integer.toString(stAL.size()));
		displayFamilyTable();		//Display the table after table array list is built					
	}
	
	@Override
	void buildTableList(boolean bPreserveSelections) 
	{
		if(activityDB.getDeliveryActivities().isEmpty())
		{
			atAL.clear();	//Clear the delivery volunteer table
			lblNumOfObjects.setText(Integer.toString(atAL.size()));
			displaySortTable(atAL, false, tableRowSelectedObjectList);
			
			JOptionPane.showMessageDialog(parentFrame, "<html><b>ERROR:</b> Couldn't find a "
					+ "volunteer delivery activity in the data base.<br> "
					+ "Please ensure at least one Activity is checked as gift delivery, <br>"
					+ "using the Edit Activites dialog to do so. The volunteer table will <br>"
					+ "be empty until there is a delivery activity.</html>",
					"ERROR: Couldn't find Volunteer Delivery Activity",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));	
		}
		else
		{
			//archive the table rows selected prior to rebuild so the can be reselected if the
			//build occurred due to an external modification of the table
			tableRowSelectedObjectList.clear();
			if(bPreserveSelections)
				archiveTableSelections(atAL);
			else
				tableSortCol = -1;
			
			stAL.clear();
		
			clearFamilyTable();
			familyTable.clearSelection();
			
			atAL.clear();	//Clear the delivery volunteer table

			for(ONCVolunteer v : volunteerDB.getVolunteerList())
				if(doesVolunteerDeliver(v) && doesDrvNumMatch(v.getDrvNum()) && doesLNameMatch(v.getLastName()) && 
				    doesChangedByMatch(v.getChangedBy()) && doesStoplightMatch(v.getStoplightPos()))
					atAL.add(v);
			
			lblNumOfObjects.setText(Integer.toString(atAL.size()));
			displaySortTable(atAL, true, tableRowSelectedObjectList);
		}
	}
	
	//determine if the volunteer has agreed to deliver gifts. Get a list of 
	//the volunteers activities and determine if at least one is marked as
	//a gift delivery activity
	boolean doesVolunteerDeliver(ONCVolunteer v)
	{
		boolean bVolunteerDelivers = false;
		List<VolAct>  volActList = volActDB.getVolunteerActivityList(v.getID());
		for(VolAct va : volActList)
		{
			Activity a = activityDB.getActivity(va.getActID());
			if(a.isDeliveryActivity())
			{
				bVolunteerDelivers = true;
				break;
			}	
		}
		
		return bVolunteerDelivers;
//		return true;
	}
	
	void updateLNameCBList()
	{
		bIgnoreCBEvents = true;
		
		ArrayList<String> lNameAL = new ArrayList<String>();
		
		lNameCBM.removeAllElements();
		
		
		for(ONCVolunteer d:volunteerDB.getVolunteerList())
		{
			int index = 0;
			while(index < lNameAL.size() && !d.getLastName().equals(lNameAL.get(index)))
				index++;
			
			if(index == lNameAL.size())
				lNameAL.add(d.getLastName());	
		}
		
		Collections.sort(lNameAL);
		lNameCBM.addElement("Any");
		for(String s:lNameAL)
			lNameCBM.addElement(s);
		
		bIgnoreCBEvents = false;
	}
	
	void updateUserList()
	{	
		UserDB userDB = UserDB.getInstance();
		
		changedByCB.removeActionListener(this);
		
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
		sortChangedBy = (String) changedByCB.getSelectedItem();
		
		changedByCB.addActionListener(this);
	}
	
	void onExportDeliveryReport()
	{
		String[] header = new String[] {"Drv #", "First Name", "Last Name", "# Del", "Cell #", "Home #", 
										"E-Mail Address"};
			
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected delivery volunteers" ,
       								new FileNameExtensionFilter("CSV Files", "csv"),ONCFileChooser.SAVE_FILE);
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
	    	    			ONCVolunteer d = atAL.get(index);
	    	    			String[] di = new String[] {d.getDrvNum(), d.getFirstName(), d.getLastName(),
	    	    							Integer.toString(d.getDelAssigned()),
	    	    							d.getCellPhone(), d.getHomePhone(), d.getEmail()};
	   
	    	    			writer.writeNext(di);
       			}
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						sortTable.getSelectedRowCount() + " deliveries sucessfully exported to " + oncwritefile.getName(), 
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
	
	void onExportDependantTableRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"Driver", "ONC #", "Batch #", "DNS", "Family Status", "Delivery Status",
    						"Meal Status", "First Name", "Last Name", "House #", "Street",
    						"Unit", "Zip", "Region", "Changed By"};
    	
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
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
	    	    
	    	    int[] row_sel = familyTable.getSelectedRows();
	    	    for(int i=0; i<familyTable.getSelectedRowCount(); i++)
	    	    	writer.writeNext(getDependantTableExportRow(row_sel[i]));
	    	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " partners sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(parentFrame, 
						"Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}
	
	String[] getDependantTableExportRow(int index)
	{
		ONCFamily f = stAL.get(index).getFamily();
		FamilyHistory fh = stAL.get(index).getFamilyHistory();
		ONCMeal meal = mealDB.getFamiliesCurrentMeal(f.getID());
		DNSCode code = fh.getDNSCode() > -1 ?dnsCodeDB.getDNSCode(fh.getDNSCode()) : null;
		
		String[] row = {
						volunteerDB.getDriverLNFN(fh.getdDelBy()),
						f.getONCNum(),
						f.getBatchNum(),
						code == null ? "" : code.getAcronym(),
						fh.getFamilyStatus().toString(),
						fh.getGiftStatus().toString(),
						meal != null ? meal.getStatus().toString() : MealStatus.None.toString(),
						f.getFirstName(),
						f.getLastName(),
						f.getHouseNum(),
						f.getStreet(),
						f.getUnit(),
						f.getZipCode(),
						regionDB.getRegionID(f.getRegion()),
						f.getChangedBy()
						};
		return row;
	}
	
	void checkExportandPrintandEmailEnabled()
	{
		if(familyTable.getSelectedRowCount() > 0)
		{
			btnDependantTableExport.setEnabled(true);
			famPrintCB.setEnabled(true);
		}
		
		exportCB.setEnabled(sortTable.getSelectedRowCount() > 0);
		printCB.setEnabled(sortTable.getSelectedRowCount() > 0);		
	}

	boolean doesDrvNumMatch(String drvNum) { return sortONCNum.equals("Any") || sortONCNum.equals(drvNum); }
	boolean doesLNameMatch(String drvLName) {return sortLName.equals("Any") || sortLName.equals(drvLName);}
	boolean doesChangedByMatch(String cb) { return sortChangedBy.equals("Anyone") || cb.equals(changedByCB.getSelectedItem()); }
	boolean doesStoplightMatch(int sl) { return sortStoplight == 0 || sl == stoplightCB.getSelectedIndex()-1; }
	
	@Override
	String[] getColumnToolTips() 
	{
		String[] toolTips = {"Driver Number", "First Name", "Last Name", "# of Deliveries",
				"Cell Phone #", "Home Phone #",
				"E-Mail address", "Changed By", "Stoplight Color"};
		return toolTips;
	}

	@Override
	String[] getColumnNames() 
	{
		String[] columns = {"Drv #", "First Name", "Last Name", "# Del", "Cell #", "Home #", "E-Mail Address",
				"Changed By", "SL"};
		return columns;
	}

	@Override
	int[] getColumnWidths() 
	{
		int[] colWidths = {32, 80, 96, 28, 88, 88, 168, 88, 16};
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
		if(e.getSource() == drvCB && !bIgnoreCBEvents && !drvCB.getSelectedItem().toString().equals(sortONCNum))
		{
			sortTable.clearSelection();
			familyTable.clearSelection();
			
			sortONCNum = drvCB.getSelectedItem().toString();
			buildTableList(false);			
		}
		else if(e.getSource() == lNameCB && !bIgnoreCBEvents && !lNameCB.getSelectedItem().toString().equals(sortLName))
		{
			sortTable.clearSelection();
			familyTable.clearSelection();
			
			sortLName = lNameCB.getSelectedItem().toString();
			buildTableList(false);			
		}
		else if(e.getSource() == changedByCB && !bIgnoreCBEvents && !changedByCB.getSelectedItem().toString().equals(sortChangedBy))
		{
			sortTable.clearSelection();
			familyTable.clearSelection();
			
			sortChangedBy = (String) changedByCB.getSelectedItem();
			buildTableList(false);			
		}
		else if(e.getSource() == stoplightCB && stoplightCB.getSelectedIndex() != sortStoplight)
		{
			sortStoplight = stoplightCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == exportCB)
		{
			onExportDeliveryReport();
			exportCB.setSelectedIndex(0);
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1)
				onPrintListing("ONC Delivery Partners");
			
			printCB.setSelectedIndex(0);
		}
		else if(e.getSource() == famPrintCB)
		{
			if(famPrintCB.getSelectedIndex() == 1)
			{ 
				onPrintListing("ONC Families for Agent");
				famPrintCB.setSelectedIndex(0);
			}
		}
		else if(e.getSource() == btnDependantTableExport)
		{
			onExportDependantTableRequested();	
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{	
		if(dbe.getType().equals("UPDATED_FAMILY") || dbe.getType().equals("ADDED_DELIVERY") ||
				dbe.getType().equals("ADDED_MEAL") || dbe.getType().equals("UPDATED_DNSCODE") ||
				 dbe.getType().equals("ADDED_DELIVERY"))
		{
			buildFamilyTableListAndDisplay();		
		}
		else if(dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Delivery Volunteer Management", gvs.getCurrentSeason()));
			updateUserList();
		}
		else if(dbe.getType().equals("ADDED_ACTIVITY") || dbe.getType().equals("UPDATED_ACTIVITY") ||
				dbe.getType().equals("DELETED_ACTIVITY"))
		{
			if(this.isVisible())
				buildTableList(true);
		}
//		else if(dbe.getType().equals("UPDATED_GLOBALS"))
//		{
//			int updatedDelActID = gvs.getDeliveryActivityID();
//			if(deliveryActivity != null && updatedDelActID != deliveryActivity.getID())
//			{
//				deliveryActivity = activityDB.getActivity(updatedDelActID);
//				buildTableList(true);
//			}
//		}
		else if(dbe.getType().contains("_DRIVER") || dbe.getType().contains("_VOLUNTEER_ACTIVITY"))
		{
			buildTableList(true);
			updateLNameCBList();
		}
		else if(dbe.getType().contains("_USER"))
		{
			updateUserList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() &&lse.getSource() == sortTable.getSelectionModel() 
				&& !bChangingTable)
		{
			if(sortTable.getSelectedRowCount() == 0)	//No selection
			{
				stAL.clear();
				clearFamilyTable();
			}
			else	//delivery volunteer selected, build new family table associated with the volunteer
			{
//				System.out.println("SortDrvDlg.valueChanged: lse event occurred, delivery volunteer selected");
				buildFamilyTableListAndDisplay();
				
				fireEntitySelected(this, EntityType.VOLUNTEER, atAL.get(sortTable.getSelectedRow()), null);
				requestFocus();
			}		
		}
		else if (!lse.getValueIsAdjusting() && lse.getSource() == familyTable.getSelectionModel() &&
					!bChangingFamilyTable)
		{
			ONCFamily selectedFam = stAL.get(familyTable.getSelectedRow()).getFamily();
			FamilyHistory selectedFamHistory = stAL.get(familyTable.getSelectedRow()).getFamilyHistory();
			fireEntitySelected(this, EntityType.FAMILY, selectedFam, selectedFamHistory, null);
			requestFocus();
		}
	
		checkExportandPrintandEmailEnabled();
	}

	@Override
	int sortTableList(int col) 
	{
		archiveTableSelections(atAL);
		
		if(volunteerDB.sortDB(atAL, columns[col]))
		{
			displaySortTable(atAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;	
	}

	@Override
	void setEnabledControls(boolean tf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	Object[] getTableRow(ONCObject o) 
	{
		ONCVolunteer d = (ONCVolunteer) o;
		Object[] di = {d.getDrvNum(), d.getFirstName(), d.getLastName(),
						Integer.toString(d.getDelAssigned()),
						d.getCellPhone(), d.getHomePhone(), d.getEmail(), d.getChangedBy(),
//						stoplt[d.getStoplightPos()+1].substring(0,1)};
						gvs.getImageIcon(23 + d.getStoplightPos())};
		return di;
	}
	
	@Override
	void onResetCriteriaClicked()
	{
		stAL.clear();
		clearFamilyTable();
		sortTable.clearSelection();
		familyTable.clearSelection();
		
		drvCB.removeActionListener(this);;
		drvCB.setSelectedIndex(0);
		sortONCNum = "Any";
		drvCB.addActionListener(this);
				
		lNameCB.removeActionListener(this);
		lNameCB.setSelectedIndex(0);		//Will trigger the CB event handler which
		sortLName = "Any";
		lNameCB.addActionListener(this);;
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);		//Will trigger the CB event handler which
		sortChangedBy = "Anyone";
		changedByCB.addActionListener(this);
		
		stoplightCB.removeActionListener(this);
		stoplightCB.setSelectedIndex(0);
		sortStoplight = 0;
		stoplightCB.addActionListener(this);
		
		buildTableList(false);
	}

	@Override
	void initializeFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY, EntityType.VOLUNTEER);
	}
}
