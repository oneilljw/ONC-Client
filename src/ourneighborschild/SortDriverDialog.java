package ourneighborschild;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;

public class SortDriverDialog extends DependantTableDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox lNameCB, changedByCB, stoplightCB;
	private DefaultComboBoxModel lNameCBM, changedByCBM;
	private String sortLName;
	private int sortChangedBy, sortStoplight;
	private JComboBox drvCB, printCB;
	
	private DeliveryDB deliveryDB;
	private DriverDB driverDB;
	private ArrayList<ONCDriver> atAL;	//Holds references to driver objects for driver table
	
	SortDriverDialog(JFrame pf)
	{
		super(pf, 10);
		this.setTitle("Our Neighbor's Child - Delivery Partner Management");
		
		deliveryDB = DeliveryDB.getInstance();
		
		driverDB = DriverDB.getInstance();
		if(driverDB != null)
			driverDB.addDatabaseListener(this);
		
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//Set up the agent table content array list
		atAL = new ArrayList<ONCDriver>();
		
		//Initialize the sort criteria variables. Reusing superclass sortONCNum for driver number
		sortONCNum = "Any";
		sortLName = "Any";
		sortChangedBy = 0;
		sortStoplight = 0;
		
		//Set up the search criteria panel
		//Set up unique serach criteria gui
    	String[] oncStrings = {"Any", "N/A"};
    	drvCB = new JComboBox(oncStrings);
    	drvCB.setEditable(true);
    	drvCB.setPreferredSize(new Dimension(88,56));
		drvCB.setBorder(BorderFactory.createTitledBorder("Driver #"));
		drvCB.addActionListener(this);
		
		lNameCB = new JComboBox();
		lNameCBM = new DefaultComboBoxModel();
	    lNameCBM.addElement("Any");
	    lNameCB.setModel(lNameCBM);
	    lNameCB.setEditable(true);
		lNameCB.setPreferredSize(new Dimension(144, 56));
		lNameCB.setBorder(BorderFactory.createTitledBorder("Last Name"));
		lNameCB.addActionListener(this);
		
		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setPreferredSize(new Dimension(144, 56));
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.addActionListener(this);
		
//		stoplightCB = new JComboBox(stoplt);
		stoplightCB = new JComboBox(GlobalVariables.getLights());
		stoplightCB.setMaximumSize(new Dimension(80, 56));
		stoplightCB.setBorder(BorderFactory.createTitledBorder("Stoplight"));
		stoplightCB.addActionListener(this);
		
		//Add all sort criteria components to dialog pane
		sortCriteriaPanelTop.add(drvCB);
        sortCriteriaPanelTop.add(lNameCB);
        sortCriteriaPanelTop.add(changedByCB);
        sortCriteriaPanelTop.add(stoplightCB);
        sortCriteriaPanelTop.add(new JPanel());
		
        //Set up the cntl panel
		//Set the text for the driver count label
		lblObjectMssg.setText("# of Delivery Partners:"); 
      
      	//Create a print button for agent information
      	String[] agentPrintChoices = {"Print", "Print Driver Listing"};
        printCB = new JComboBox(agentPrintChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
                      
        //Add the components to the control panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,0,0,396);
      	cntlPanel.add(objectCountPanel, gbc);
      	
      	gbc.gridx = 1;
        gbc.insets = new Insets(0,0,0,0);
        cntlPanel.add(printCB, gbc);
        
        //set the border title for the family table 
        familyTableScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(), "Family Deliveries Made/Attempted By Selected Delivery Partner(s)"));

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
			for(ONCFamily f:fDB.getList())
			{
				//determine if the family has a driver based on the delivery. If the family
				//has a driver, does the delivery driver's ID match the id of the driver selected
				//in the selection table. If so, add to the defendant table list
				ONCDelivery del = deliveryDB.getDelivery(f.getDeliveryID());
			
				if(del != null && !del.getdDelBy().isEmpty())
				{
					//There is s driver assigned. Determine who it is from the driver number
					//and check to see if it matches the selected driver(s) in the selection table
					int index = driverDB.getDriverIndex(del.getdDelBy());
					if(index > -1 && driverDB.getDriver(index).getDrvNum().equals(atAL.get(row_sel[i]).getDrvNum()))
						stAL.add(f);
				}	
			}
					
		lblNumOfFamilies.setText(Integer.toString(stAL.size()));
		displayFamilyTable();		//Display the table after table array list is built					
	}
	
	@Override
	void buildTableList(boolean bPreserveSelections) 
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(atAL);
		else
			tableSortCol = -1;
		
		atAL.clear();	//Clear the prior table data array list
		stAL.clear();
		
		clearFamilyTable();
		familyTable.clearSelection();
		
		for(ONCDriver d:driverDB.getDriverDB())
			if(doesDrvNumMatch(d.getDrvNum()) && doesLNameMatch(d.getlName()) && 
				doesChangedByMatch(d.getChangedBy()) && doesStoplightMatch(d.getStoplightPos()))
				atAL.add(d);
			
		lblNumOfObjects.setText(Integer.toString(atAL.size()));
		displaySortTable(atAL, true, tableRowSelectedObjectList);
	}
	
	void updateLNameCBList()
	{
		bIgnoreCBEvents = true;
		
		ArrayList<String> lNameAL = new ArrayList<String>();
		
		lNameCBM.removeAllElements();
		
		
		for(ONCDriver d:driverDB.getDriverDB())
		{
			int index = 0;
			while(index < lNameAL.size() && !d.getlName().equals(lNameAL.get(index)))
				index++;
			
			if(index == lNameAL.size())
				lNameAL.add(d.getlName());	
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
		for(ONCUser user:userDB.getUserList())
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.addActionListener(this);
	}
	
	void onExportDependantTableRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"Driver", "ONC #", "Batch #", "DNS", "Family Status", "Delivery Status",
    						"Meal Status", "First Name", "Last Name", "House #", "Street",
    						"Unit", "Zip", "Region", "Changed By"};
    	
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
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
		ONCFamily f = stAL.get(index);
		
		String[] row = {
						driverDB.getDriverLNFN(deliveryDB.getDelivery(f.getDeliveryID()).getdDelBy()),
						f.getONCNum(),
						f.getBatchNum(),
						f.getDNSCode(),
						famstatus[f.getFamilyStatus()+1],
						delstatus[f.getDeliveryStatus()+1],
						f.getMealStatus().toString(),
						f.getHOHFirstName(),
						f.getHOHLastName(),
						f.getHouseNum(),
						f.getStreet(),
						f.getUnitNum(),
						f.getZipCode(),
						regions.getRegionID(f.getRegion()),
						f.getChangedBy()
						};
		return row;
	}
	
	void checkPrintandEmailEnabled()
	{
		if(familyTable.getSelectedRowCount() > 0)
		{
			btnDependantTableExport.setEnabled(true);
			famPrintCB.setEnabled(true);
		}
		
		if(sortTable.getSelectedRowCount() > 0)
			printCB.setEnabled(true);
		else
			printCB.setEnabled(false);			
	}
	
	
	boolean doesDrvNumMatch(String drvNum) { return sortONCNum.equals("Any") || sortONCNum.equals(drvNum); }
	boolean doesLNameMatch(String drvLName) {return sortLName.equals("Any") || sortLName.equals(drvLName);}
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
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
			
			sortChangedBy = changedByCB.getSelectedIndex();
			buildTableList(false);			
		}
		else if(e.getSource() == stoplightCB && stoplightCB.getSelectedIndex() != sortStoplight)
		{
			sortStoplight = stoplightCB.getSelectedIndex();
			buildTableList(false);
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
		if(dbe.getType().equals("UPDATED_FAMILY") || dbe.getType().equals("ADDED_DELIVERY"))
		{
//			System.out.println(String.format("Sort Agent Dialog DB event, Source: %s, Type %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			buildFamilyTableListAndDisplay();		
		}
		else if(dbe.getType().contains("_DRIVER"))	//build on add, update or delete event
		{
			//update the agent table and update the org and title combo box models
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
//		System.out.println("SortAgtDlg.valueChanged: valueIsAdjusting: " + lse.getValueIsAdjusting());
		if(!lse.getValueIsAdjusting() &&lse.getSource() == sortTable.getSelectionModel() 
				&& !bChangingTable)
		{
			if(sortTable.getSelectedRowCount() == 0)	//No selection
			{
//				System.out.println("SortAgtDlg.valueChanged: lse event occurred, agent row count = 0");
				stAL.clear();
				clearFamilyTable();
			}
			else	//Agent selected, build new family table associated with the agent
			{
//				System.out.println("SortAgtDlg.valueChanged: lse event occurred, agent selected");
				buildFamilyTableListAndDisplay();
				
				fireEntitySelected(this, EntityType.DRIVER, atAL.get(sortTable.getSelectedRow()), null);
				requestFocus();
			}		
		}
		else if (!lse.getValueIsAdjusting() && lse.getSource() == familyTable.getSelectionModel() &&
					!bChangingFamilyTable)
		{
			fireEntitySelected(this, EntityType.FAMILY, stAL.get(familyTable.getSelectedRow()), null);
			requestFocus();
		}
	
		checkPrintandEmailEnabled();
	}

	@Override
	int sortTableList(int col) 
	{
		archiveTableSelections(atAL);
		
		if(driverDB.sortDB(atAL, columns[col]))
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
		ONCDriver d = (ONCDriver) o;
		Object[] di = {d.getDrvNum(), d.getfName(), d.getlName(),
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
		sortChangedBy = 0;
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
		return EnumSet.of(EntityType.FAMILY, EntityType.DRIVER);
	}
}
