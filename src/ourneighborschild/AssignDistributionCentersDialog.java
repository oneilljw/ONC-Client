package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import au.com.bytecode.opencsv.CSVWriter;

public class AssignDistributionCentersDialog extends ONCEntityTableDialog implements ActionListener, 
															DatabaseListener, ListSelectionListener
{
	/**
	 * This class implements a dialog which allows the user to manage family distribution center assignments
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int ONC_NUM_COL= 0;
	private static final int HOH_FIRST_NAME_COL = 1;
	private static final int HOH_LAST_NAME_COL = 2;
	private static final int STREET_NUM_COL = 3;
	private static final int STREET_NAME = 4;
	private static final int ZIPCODE_COL = 5;
	private static final int REGION_COL = 6;
	private static final int SCHOOL_COL = 7;
	private static final int DIST_CENTER_COL = 8;
	
	private static final int NUM_TABLE_ROWS = 18;
	
	protected JPanel sortCriteriaPanel;
	private JComboBox<DistributionCenter> centerCB, changeCenterCB;
	private DefaultComboBoxModel<DistributionCenter> centerCBM, changeCenterCBM;
	private JComboBox<School> schoolCB;
	private DefaultComboBoxModel<School> schoolCBM;
	private DefaultComboBoxModel<String> regionCBM;
	private JComboBox<String> oncCB, zipcodeCB, regionCB;
	private boolean bChangingTable;
	
	private ONCTable centerTable;
	private AbstractTableModel centerTableModel;
	private JButton btnReset, btnApplyChanges;
	private JComboBox<String> printCB, exportCB;
	private String[] printChoices, exportChoices;
	private JLabel lblCount;
	
	private DistributionCenterDB centerDB;
	private FamilyDB familyDB;
	private FamilyHistoryDB familyHistoryDB;
	private RegionDB regionDB;
	private UserDB userDB;
	
	private List<ONCFamily> familyTableList;
	
	public AssignDistributionCentersDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Distribution Center Assignment");
		
		//Save the reference to data bases 
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		centerDB = DistributionCenterDB.getInstance();
		if(centerDB != null)
			centerDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		regionDB = RegionDB.getInstance();
		if(regionDB != null)
			regionDB.addDatabaseListener(this);
		
		familyHistoryDB = FamilyHistoryDB.getInstance();
		if(familyHistoryDB != null)
			familyHistoryDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		//set up the table list
		familyTableList = new ArrayList<ONCFamily>();
		
		bChangingTable = false;
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		//Set up unique search criteria GUI
		String[] oncStrings = {"Any", "NNA", "OOR", "RNV", "DEL"};
    	oncCB = new JComboBox<String>(oncStrings);
    	oncCB.setEditable(true);
    	oncCB.setPreferredSize(new Dimension(80,56));
		oncCB.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncCB.addActionListener(this);
		
		String[] onczipCodes = {"Any", "20120", "20121", "20124", "20151", "22033", "22039", "Out Of Area"};
		zipcodeCB = new JComboBox<String>(onczipCodes);
		zipcodeCB.setBorder(BorderFactory.createTitledBorder("Zip Code"));
		zipcodeCB.addActionListener(this);
		
		regionCBM = new DefaultComboBoxModel<String>();
		regionCBM.addElement("Any");
		regionCB = new JComboBox<String>();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.addActionListener(this);
		
		schoolCB = new JComboBox<School>();
		schoolCBM = new DefaultComboBoxModel<School>();
		schoolCBM.addElement(new School("Any", "Any"));
		schoolCB.setModel(schoolCBM);
		schoolCB.setBorder(BorderFactory.createTitledBorder("School"));
		schoolCB.setPreferredSize(new Dimension(180, 56));
		schoolCB.addActionListener(this);
		
		centerCB = new JComboBox<DistributionCenter>();
		centerCBM = new DefaultComboBoxModel<DistributionCenter>();
		centerCBM.addElement(new DistributionCenter(-2, "Any", "Any"));
		centerCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
		centerCB.setModel(centerCBM);
		centerCB.setBorder(BorderFactory.createTitledBorder("Distribution Center"));
		centerCB.setPreferredSize(new Dimension(300,56));
		centerCB.addActionListener(this);
		
		sortCriteriaPanel.add(oncCB);
		sortCriteriaPanel.add(zipcodeCB);
		sortCriteriaPanel.add(regionCB);
		sortCriteriaPanel.add(schoolCB);
		sortCriteriaPanel.add(centerCB);
		
		//Create the volunteer table model
		centerTableModel = new TableModel();
		
		//create the table
		String[] colToolTips = {"ONC #", "HoH FN", "HoH LN", "Street #", "Street",
								"Zip Code", "Region", "School", "Distribution Center"};
		
		centerTable = new ONCTable(centerTableModel, colToolTips, new Color(240,248,255));

		centerTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		centerTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {48, 88, 96, 56, 120, 64, 48, 120, 192};
		for(int col=0; col < colWidths.length; col++)
		{
			centerTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 28; 	//count for vertical scroll bar
		
		centerTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = centerTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        centerTable.getColumnModel().getColumn(ZIPCODE_COL).setCellRenderer(dtcr);
        centerTable.getColumnModel().getColumn(REGION_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(centerTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, centerTable.getRowHeight()*NUM_TABLE_ROWS));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        //create the change panel
        JPanel changePanel = new JPanel();
        changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.X_AXIS));
        
        JPanel countPanel = new JPanel();
        countPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        countPanel.setBorder(BorderFactory.createTitledBorder("Families Meeting Criteria"));
        
        lblCount = new JLabel("0");
        countPanel.add(lblCount);
        
        JPanel updateChoicesPanel = new JPanel();
        updateChoicesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        updateChoicesPanel.setBorder(BorderFactory.createTitledBorder("Change Select Family Data"));
        
        changeCenterCB = new JComboBox<DistributionCenter>();
        changeCenterCBM = new DefaultComboBoxModel<DistributionCenter>();
        changeCenterCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
        changeCenterCB.setModel(changeCenterCBM);
        changeCenterCB.setBorder(BorderFactory.createTitledBorder("Change Distribution Center"));
        changeCenterCB.setPreferredSize(new Dimension(300,56));
        changeCenterCB.addActionListener(this);
        updateChoicesPanel.add(changeCenterCB);
        
        changePanel.add(countPanel);
        changePanel.add(updateChoicesPanel);
    
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        exportChoices = new String[] {"Export", "Distribution Center Summary"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setToolTipText("Export to .csv file");
        exportCB.setEnabled(true);
        exportCB.addActionListener(this);
        
        printChoices = new String[] {"Print", "Table"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setToolTipText("Print the distibution center assignment table");
        printCB.addActionListener(this);
        
        btnReset = new JButton("Reset Filters");
        btnReset.setToolTipText("Restore Filters to Defalut State");
        btnReset.addActionListener(this);
        
        btnApplyChanges = new JButton("Apply Changes");
        btnApplyChanges.setToolTipText("Update distribution center assignements for selected families");
        btnApplyChanges.addActionListener(this);
       
        btnPanel.add(exportCB);
        btnPanel.add(printCB);
        btnPanel.add(btnReset);
        btnPanel.add(btnApplyChanges);
        
        cntlPanel.add(btnPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(changePanel);
        getContentPane().add(cntlPanel);
        
        this.setMinimumSize(new Dimension(920, 500));
	}
	void createTableList()
	{
		bChangingTable = true;
		
		familyTableList.clear();
		
		//to qualify for pickup location assignment, a family must be being served (FamilyStatus of
		//Verified at a minimum and they must have a gift distribution Pickup choice
		for(ONCFamily f : familyDB.getList())
		{	
			FamilyHistory fh = familyHistoryDB.getLastFamilyHistory(f.getID());
			
			if(f.getGiftDistribution() == GiftDistribution.Pickup &&
				fh != null && fh.getFamilyStatus().compareTo(FamilyStatus.Verified) >= 0 && 
				 doesONCNumMatch(f.getONCNum()) && doesZipMatch(f.getZipCode()) &&
				  doesRegionMatch(f.getRegion()) && doesSchoolMatch(f.getSchoolCode()) &&
				   doesLocationMatch(f.getDistributionCenterID()))
			{	
				familyTableList.add(f);
			}
		}
		
		lblCount.setText(String.format("%d", familyTableList.size()));
		
		centerTableModel.fireTableDataChanged();
		
		bChangingTable = false;
	}
	
	//set up the search criteria filters
	boolean doesONCNumMatch(String oncn) {return oncCB.getSelectedItem().equals("Any") || oncn.equals(oncCB.getSelectedItem().toString());}
		
	boolean doesZipMatch(String zip)
	{
		if(zipcodeCB.getSelectedItem().equals("Out of Area"))
			return !(zip.equals("20120") || zip.equals("20121") || zip.equals("20124") ||
					zip.equals("20151") || zip.equals("22033") || zip.equals("22039"));	
		else
			return zipcodeCB.getSelectedIndex() == 0 || zip.equals(zipcodeCB.getSelectedItem());
	}
	
	boolean doesRegionMatch(int fr) { return regionCB.getSelectedIndex() == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	boolean doesSchoolMatch(String schoolCode) 
	{
		School selectedSchool = (School) schoolCB.getSelectedItem();
		return schoolCB.getSelectedIndex() == 0 || schoolCode.equals(selectedSchool.getCode());
	}
	
	boolean doesLocationMatch(int pickupLocation) 
	{
		DistributionCenter selectedLocation = (DistributionCenter) centerCB.getSelectedItem();
		return centerCB.getSelectedIndex() == 0 || pickupLocation == selectedLocation.getID();
	}
	
	void resetFilters()
	{	
		oncCB.removeActionListener(this);
		oncCB.setSelectedIndex(0);
		oncCB.addActionListener(this);
		
		zipcodeCB.removeActionListener(this);
		zipcodeCB.setSelectedIndex(0);
		zipcodeCB.addActionListener(this);
		
		regionCB.removeActionListener(this);
		regionCB.setSelectedIndex(0);
		regionCB.addActionListener(this);
		
		schoolCB.removeActionListener(this);
		schoolCB.setSelectedIndex(0);
		schoolCB.addActionListener(this);
		
		centerCB.removeActionListener(this);
		centerCB.setSelectedIndex(0);
		centerCB.addActionListener(this);
		
		changeCenterCB.removeActionListener(this);
		changeCenterCB.setSelectedIndex(0);
		changeCenterCB.addActionListener(this);
		
		createTableList();
	}

	void updateSchoolList()
	{	
		schoolCB.removeActionListener(this);
		
		School curr_sel = (School) schoolCB.getSelectedItem();
		int selIndex = 0;
		
		schoolCBM.removeAllElements();
		
		//creates a dummy school with code "Any"
		schoolCBM.addElement(new School("Any", "Any"));
		
		//creates a dummy school Not in Pyramid
		schoolCBM.addElement(new School("Y", "Not In Pyramid"));
		
		//creates a dummy school Not in Area
		schoolCBM.addElement(new School("Z", "Not In Area"));
		
		int index = 0;
		for(School sch : regionDB.getServedSchoolList(SchoolType.ES))
		{
			schoolCBM.addElement(sch);
			index++;
			if(curr_sel.matches(sch))
				selIndex = index;
		}
		
		schoolCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		
		schoolCB.addActionListener(this);
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
	
	void updateDistributionCenterLists()
	{	
		centerCB.removeActionListener(this);
		changeCenterCB.removeActionListener(this);
		
		DistributionCenter curr_filter_sel = (DistributionCenter) centerCB.getSelectedItem();
		int filterSelIndex = 0;
		
		centerCBM.removeAllElements();
		changeCenterCBM.removeAllElements();
		
		//creates a dummy with code "Any"
		centerCBM.addElement(new DistributionCenter(-2, "Any", "Any"));
		centerCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
		
		//creates a dummy with code "Any"
		changeCenterCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
		
		int index = 0;
		for(DistributionCenter pl : centerDB.getList())
		{
			centerCBM.addElement(pl);
			changeCenterCBM.addElement(pl);
			index++;
			if(curr_filter_sel.matches(pl))
				filterSelIndex = index;
		}
		
		centerCB.setSelectedIndex(filterSelIndex); //Keep current selection in sort criteria
		changeCenterCB.setSelectedIndex(0);
		
		changeCenterCB.addActionListener(this);
		centerCB.addActionListener(this);
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             centerTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	void checkApplyChangesEnabled()
	{
		//Enable apply changes button when there are table rows selected and the 
		//change center has been selected
		if(centerTable.getSelectedRows().length > 0 && changeCenterCB.getSelectedIndex() > 0 )
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	
		if(centerTable.getSelectedRowCount() > 0  && userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 )
		{
			exportCB.setEnabled(true);
			printCB.setEnabled(true);
		}
		else
		{
			printCB.setEnabled(false);
			exportCB.setEnabled(false);
		}	
	}	
	
	//Returns a boolean that a change distribution center assignment occurred
	boolean onApplyChanges()
	{		
		int[] row_sel = centerTable.getSelectedRows();
		boolean bDataChanged = false;

		List<ONCFamily> updateFamReqList = new ArrayList<ONCFamily>();
		for(int i=0; i<row_sel.length; i++)
		{
			ONCFamily updateFamReq = new ONCFamily(familyTableList.get(row_sel[i]));

			boolean bFamilyChangeDetected = false;
	
			//If a change to the pickup location, process it
			if(changeCenterCB.getSelectedIndex() > 0 && 
				updateFamReq.getDistributionCenterID() != ((DistributionCenter) changeCenterCB.getSelectedItem()).getID())
			{
				updateFamReq.setDistributionCenter((DistributionCenter) changeCenterCB.getSelectedItem());
				bFamilyChangeDetected = true;
			}
			
			if(bFamilyChangeDetected)
				updateFamReqList.add(updateFamReq);
			
		}
		
		if(!updateFamReqList.isEmpty())
		{
			String response = familyDB.updateFamList(this, updateFamReqList);
			if(response.startsWith("UPDATED_LIST_FAMILIES"))
			{	
				createTableList();
				bDataChanged = true;
			}
			else
			{
				createTableList();
				
				//display an error message that update request failed
				GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
				JOptionPane.showMessageDialog(this, "ONC Server denied Distribution Center Assignment Update," +
						"try again later","Family Distribution Center Update Failed",  JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
		
		//Reset the change combo boxes to DEFAULT_NO_CHANGE_LIST_ITEM
		changeCenterCB.setSelectedIndex(0);
					
		//Changes were applied, disable until user selects new table row(s) and values
		btnApplyChanges.setEnabled(false);
			
		return bDataChanged;
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"ONC #", "HoH FN", "HoH LN", "Street #", "Street", "Zip Code", "Region", "School", "Distribution Center"};
    
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
	    	    
       			List<String> rowList = new ArrayList<String>();
       			for(ONCFamily f : familyTableList)
       			{
       				rowList.add(f.getONCNum());		
       				rowList.add(f.getFirstName());
       				rowList.add(f.getLastName());
       				rowList.add(f.getHouseNum());
       				rowList.add(f.getStreet());
       				rowList.add(f.getZipCode());
       				rowList.add(Integer.toString(f.getRegion()));
       				rowList.add(f.getSchoolCode());
       				rowList.add(Integer.toString(f.getDistributionCenterID()));
       				writer.writeNext(rowList.toArray(new String[rowList.size()]));
       			}

       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						centerTable.getRowCount() + " families with distribution center assignments sucessfully exported to " + oncwritefile.getName(), 
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
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			createTableList(); //update the table
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
        {
			createTableList(); //update the table
        }
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_CENTER"))
        {
			updateDistributionCenterLists();
        }
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CENTER"))
		{
			updateDistributionCenterLists();
			createTableList();
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject1();
			updateRegionList(regList);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Distribution Center Assignment", gvs.getCurrentSeason()));
			updateSchoolList();
			updateDistributionCenterLists();
			createTableList();
		}
		else if(dbe.getType().contains("UPDATED_USER"))
		{
			createTableList();
			checkApplyChangesEnabled();	//if user permission changed, they maybe be able to see family data
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncCB || e.getSource() == zipcodeCB || e.getSource() == regionCB ||
				e.getSource() == schoolCB || e.getSource() == centerCB)
		{
			createTableList();
		}
		else if(e.getSource() == changeCenterCB)
		{
			checkApplyChangesEnabled();
		}
		else if(e.getSource() == exportCB)
		{
			if(exportCB.getSelectedIndex() == 1)
				onExportRequested();
			
			exportCB.setSelectedIndex(0);
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1)
				print("Pickup Locations");
			printCB.setSelectedIndex(0);
		}
		else if(e.getSource() == btnReset)
		{
			resetFilters();
		}
		else if(e.getSource() == btnApplyChanges)
		{
			onApplyChanges();
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && !bChangingTable)
		{
			int modelRow = centerTable.getSelectedRow() == -1 ? -1 : 
				centerTable.convertRowIndexToModel(centerTable.getSelectedRow());
			
			if(modelRow > -1)
			{
				ONCFamily f = familyTableList.get(modelRow);
				this.fireEntitySelected(this, EntityType.FAMILY, f, null);
			}
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change family pickup location
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}
	
	private class TableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"ONC #", "HoH FN", "HoH LN", "Street #", "Street", "Zipcode", "Region", "Elementary School", "Distribution Center",};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return familyTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
    		ONCFamily f  = familyTableList.get(row);
    		ONCUser u = userDB.getLoggedInUser();
    		
    		if(col == ONC_NUM_COL)  
    			return f == null ? "Error" : f.getONCNum();
       		else if(col == HOH_FIRST_NAME_COL)
       			return u.getPermission().compareTo(UserPermission.Admin) >=0 ? f.getFirstName() : "";	
    		else if (col == HOH_LAST_NAME_COL)
    			return u.getPermission().compareTo(UserPermission.Admin) >=0 ? f.getLastName() : "";
    		else if(col == STREET_NUM_COL)
    			return f.getHouseNum();
        	else if (col == STREET_NAME)
        		return f.getStreet();
    		else if (col == ZIPCODE_COL)
    			return f.getZipCode();
    		else if(col == REGION_COL)
    			return regionDB.getRegionID(f.getRegion());
    		else if (col == SCHOOL_COL)
    			return regionDB.getSchoolName(f.getSchoolCode());
    		else if (col == DIST_CENTER_COL)
    		{
    			if(f.getDistributionCenterID() < 0)
    				return "Not Assigned";
    			else
    				return centerDB.getDistributionCenter(f.getDistributionCenterID()).getAcronym();
    		}
    		else
    			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
	}
}
