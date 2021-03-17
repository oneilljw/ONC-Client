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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import au.com.bytecode.opencsv.CSVWriter;

public class SchoolDeliveryDialog extends ONCEntityTableDialog implements ActionListener, ListSelectionListener,
																			DatabaseListener
{
	private static final long serialVersionUID = 1L;
	private static final int ONC_NUM_COL= 0;
	private static final int REF_NUM_COL= 1;
	private static final int FAMILY_STATUS_COL = 2;
	private static final int GIFT_STATUS_COL = 3;
	private static final int FIRST_NAME_COL= 4;
	private static final int LAST_NAME_COL = 5;
	private static final int SCHOOL_COL = 6;
	private static final int SCHOOL_TYPE_COL = 7;
	private static final int VOLUNTEER_COL = 8;
	private static final int SL_COL = 9;
	
	private static final int NUM_TABLE_ROWS = 12;
	
	protected JPanel sortCriteriaPanel;
	private JComboBox<School> schoolCB;
	private JComboBox<FamilyStatus> famStatusCB;
	private JComboBox<FamilyGiftStatus> famGiftStatusCB;
	private DefaultComboBoxModel<School> schoolCBM;
	private boolean bIgnoreCBEvents;
//	private FamilyStatus sortFamStatus;
//	private FamilyGiftStatus sortFamGiftStatus;
//	private School sortSchool;
	
	private ONCTable delTable;
	private AbstractTableModel delTableModel;
	private JButton btnResetFilters, btnPrint, btnExport;
	private JLabel lblDelCount;
	
	private FamilyDB famDB;
	private FamilyHistoryDB fhDB;
	private RegionDB regionDB;
	private FamilyHistoryDB famHistoryDB;
	
	private List<SchoolDelivery> delList;
	private ONCFamily selectedFam;
	boolean bTableChanging;
	
	public SchoolDeliveryDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("School Deliveries");
		
		//Save the reference to the one family data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		famDB = FamilyDB.getInstance();
		if(famDB != null)
			famDB.addDatabaseListener(this);
		
		fhDB = FamilyHistoryDB.getInstance();
		if(fhDB != null)
			fhDB.addDatabaseListener(this);
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		regionDB = RegionDB.getInstance();
		if(regionDB != null)
			regionDB.addDatabaseListener(this);
		
		famHistoryDB = FamilyHistoryDB.getInstance();
		if(famHistoryDB != null)
			famHistoryDB.addDatabaseListener(this);
		
		//set up the table list
		delList = new ArrayList<SchoolDelivery>();
		
		bIgnoreCBEvents = false;
		bTableChanging = false;
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		famStatusCB = new JComboBox<FamilyStatus>(FamilyStatus.getSearchFilterList());
//		sortFamStatus = FamilyStatus.Any;
		famStatusCB.setPreferredSize(new Dimension(180, 56));
		famStatusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
		famStatusCB.addActionListener(this);
		
		famGiftStatusCB = new JComboBox<FamilyGiftStatus>(FamilyGiftStatus.getSearchFilterList());
//		sortFamGiftStatus = FamilyGiftStatus.Any;
		famGiftStatusCB.setPreferredSize(new Dimension(180, 56));
		famGiftStatusCB.setBorder(BorderFactory.createTitledBorder("Family Gift Status"));
		famGiftStatusCB.addActionListener(this);
		
		schoolCB = new JComboBox<School>();
		schoolCBM = new DefaultComboBoxModel<School>();
//		sortSchool = new School();	
		schoolCBM.addElement(new School());//creates a dummy school with code "Any"
		schoolCB.setModel(schoolCBM);
		schoolCB.setPreferredSize(new Dimension(180, 56));
		schoolCB.setBorder(BorderFactory.createTitledBorder("School"));
		schoolCB.addActionListener(this);
		
		sortCriteriaPanel.add(famStatusCB);
		sortCriteriaPanel.add(famGiftStatusCB);
		sortCriteriaPanel.add(schoolCB);
		
		//Create the volunteer table model
		delTableModel = new DeliveryTableModel();
		
		//create the table
		String[] colToolTips = {"ONC#", "Ref#", "Family Status", "Gift Status", "First Name", "Last Name", 
								"School", "Type", "Volunteer", "Stop Light"};
		
		delTable = new ONCTable(delTableModel, colToolTips, new Color(240,248,255));

		delTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		delTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {56, 64, 96, 96, 104, 116, 120, 32, 120, 48};
		for(int col=0; col < colWidths.length; col++)
		{
			delTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		delTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = delTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//      volTable.getColumnModel().getColumn(NUM_ACT_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(delTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, delTable.getRowHeight()*NUM_TABLE_ROWS));
//      dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setBorder(BorderFactory.createTitledBorder("Volunteers"));
        
        //create the school delivery table control panel
        JPanel delCntlPanel = new JPanel();
        delCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblDelCount = new JLabel("School Deliveries Meeting Criteria: 0");
        delCntlPanel.add(lblDelCount);
        
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Export Table to .csv file");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the sign-in table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
       
        btnPanel.add(btnExport);
        btnPanel.add(btnPrint);
        btnPanel.add(btnResetFilters);
        
        cntlPanel.add(infoPanel);
        cntlPanel.add(btnPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(delCntlPanel);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void createTableList()
	{
		delList.clear();
		
		for(ONCFamily f : famDB.getList())
		{	
			if(!f.getSubstituteDeliveryAddress().isEmpty())
			{
				String[] delAddrParts = f.getSubstituteDeliveryAddress().split("_");
	
				if(delAddrParts.length == 5)	//format uses"_" as separator. Five components
				{	
					FamilyHistory fh = fhDB.getLastFamilyHistory(f.getID());
					School searchSchoolResult = regionDB.findServedSchool(delAddrParts[0], delAddrParts[1],
																		delAddrParts[3], delAddrParts[4]);
					
					if(searchSchoolResult != null && doesFamilyStatusMatch(fh.getFamilyStatus()) &&
						doesFamilyGiftStatusMatch(fh.getGiftStatus()) && doesSchoolMatch(searchSchoolResult))
						delList.add(new SchoolDelivery(f, searchSchoolResult));
				}
			}	
		}
		
		lblDelCount.setText(String.format("School Deliveries Meeting Criteria: %d", delList.size()));
		
		bTableChanging = true;
		delTableModel.fireTableDataChanged();
		bTableChanging = false;		
	}
	
	void resetFilters()
	{
		famStatusCB.removeActionListener(this);
		famStatusCB.setSelectedIndex(0);
		famStatusCB.addActionListener(this);
//		sortFamStatus = FamilyStatus.Any;
		
		famGiftStatusCB.removeActionListener(this);
		famGiftStatusCB.setSelectedIndex(0);
		famGiftStatusCB.addActionListener(this);
//		sortFamGiftStatus = FamilyGiftStatus.Any;
		
		schoolCB.removeActionListener(this);
		schoolCB.setSelectedIndex(0);
		schoolCB.addActionListener(this);
//		sortSchool = new School();	//creates a dummy school with code "Any"
		
		selectedFam = null;

		createTableList();
	}
	
	boolean doesFamilyStatusMatch(FamilyStatus fs) 
	{
		FamilyStatus selectedStatus = (FamilyStatus) famStatusCB.getSelectedItem();
		return selectedStatus == FamilyStatus.Any || selectedStatus == fs;
	}
	
	boolean doesFamilyGiftStatusMatch(FamilyGiftStatus fgs) 
	{
		FamilyGiftStatus selectedStatus = (FamilyGiftStatus) famGiftStatusCB.getSelectedItem();
		return selectedStatus == FamilyGiftStatus.Any || selectedStatus == fgs;
	}
	
	boolean doesSchoolMatch(School famDelSchool) 
	{
		//a school matches if the school name and type matches the selected school name and type match
		School selectedSchool = (School) schoolCB.getSelectedItem();
		boolean doesNameMatch = selectedSchool.getName().equals(famDelSchool.getName());
		boolean doesTypeMatch = selectedSchool.getType() == famDelSchool.getType();
		return schoolCB.getSelectedIndex() == 0 || (doesNameMatch && doesTypeMatch);
	}
	
	void updateSchoolList()
	{	
		schoolCB.removeActionListener(this);
		
		School curr_sel = (School) schoolCB.getSelectedItem();
		int selIndex = 0;
		
		schoolCBM.removeAllElements();
		
//		sortSchool = new School();	//creates a dummy school with code "Any"
		schoolCBM.addElement(new School());
		
		int index = 0;
		for(School sch : regionDB.getServedSchoolList(SchoolType.ALL))
		{
			schoolCBM.addElement(sch);
			index++;
			if(curr_sel.matches(sch))
				selIndex = index;
		}
		
		schoolCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
//		sortSchool = (School) schoolCB.getSelectedItem();
		
		schoolCB.addActionListener(this);
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             delTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print School Delivery table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print School Delivery Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	void onExportSchoolDeliveryReport()
	{
		//Write the selected row data to a .csv file	
		List<String> headerList = new ArrayList<String>();
		headerList.add("ONC #");
		headerList.add("Ref #");
		headerList.add("HoH FN");
		headerList.add("HoH LN");
		headerList.add("Elem School");
		headerList.add("Delivery School");
		headerList.add("Street #");
		headerList.add("Street");
		headerList.add("City");
		headerList.add("Zip Code");
		headerList.add("Delivered By");
		
		String[] header = headerList.toArray(new String[0]);
			
    		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected families" ,
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
	    	    
       			List<String> row = new ArrayList<String>();
       			int[] row_sel = delTable.getSelectedRows();
	    	    
       			for(int i=0; i < delTable.getSelectedRowCount(); i++)
       			{
	    	    			int modelRow = delTable.getSelectedRow() == -1 ? -1 : 
							delTable.convertRowIndexToModel(row_sel[i]);
			
	    	    			if(modelRow > -1)
	    	    			{
	    	    				row.clear();
	    	    				selectedFam = delList.get(modelRow).getFamily();
	    	    				row.add(selectedFam.getONCNum());
	    	    				row.add(selectedFam.getBatchNum());
	    	    				row.add(selectedFam.getFirstName());
	    	    				row.add(selectedFam.getLastName());
	    	    				row.add(regionDB.getSchoolName(selectedFam.getSchoolCode()));
	    	    				row.add(delList.get(modelRow).getServedSchool().getName());
					
	    	    				String delAddr = selectedFam.getSubstituteDeliveryAddress();
	    	    				String[] delAddrParts = delAddr.split("_");
					
	    	    				if(delAddrParts.length == 5)	//format uses"_" as separator. Five components
	    	    				{	
	    	    					row.add(delAddrParts[0]);	//street #
	    	    					row.add(delAddrParts[1]);	//street
	    	    					row.add(delAddrParts[3]);	//city
	    	    					row.add(delAddrParts[4]);	//zip
	    	    				}
	    	    				else
	    	    				{
	    	    					row.add("Error");
	    	    					row.add("Error");
	    	    					row.add("Error");
	    	    					row.add("Error");
	    	    				}
	    	    					
	    	    				row.add(famHistoryDB.getLastFamilyHistory(selectedFam.getID()).getdDelBy());
					
	    	    				writer.writeNext(row.toArray(new String[0]));
	    	    			}
       			}
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						delTable.getSelectedRowCount() + " school deliveries sucessfully exported to " + oncwritefile.getName(), 
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
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_FAMILY") ||
				dbe.getType().equals("UPDATED_FAMILY")|| dbe.getType().equals("ADDED_DELIVERY")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//update the school search filter combo, then build the table. Families are loaded before
			//schools ny the data base importer.
			updateSchoolList();
			createTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == delTable.getSelectionModel() && !bTableChanging)
		{
			int modelRow = delTable.getSelectedRow() == -1 ? -1 : 
						delTable.convertRowIndexToModel(delTable.getSelectedRow());
		
			if(modelRow > -1)
			{
				selectedFam = delList.get(modelRow).getFamily();
				fireEntitySelected(this, EntityType.FAMILY, selectedFam, null, null);
			}
		}
		
		//check to enable the export button
		btnExport.setEnabled(delTable.getSelectedRowCount() > 0);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{		
		if(e.getSource() == schoolCB && !bIgnoreCBEvents)
		{
//			sortSchool = (School) schoolCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == famStatusCB)
		{
//			sortFamStatus = (FamilyStatus) famStatusCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == famGiftStatusCB)
		{
//			sortFamGiftStatus = (FamilyGiftStatus) famGiftStatusCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC School Deliveries Table");
		}
		else if(e.getSource() == btnExport)
		{
			onExportSchoolDeliveryReport();
		}
		else if(e.getSource() == btnResetFilters)
		{
			resetFilters();
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}
	
	class DeliveryTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the School Deliveries Table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"ONC#", "Ref#", "FamilyStatus", "Gift Status","First Name", "Last Name",
										"School", "Type", "Volunteer", "SL"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return delList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		ONCFamily f = delList.get(row).getFamily();
        		FamilyHistory fh = famHistoryDB.getLastFamilyHistory(f.getID());
        		School deliverySchool = delList.get(row).getServedSchool();
        	
        		if(col == ONC_NUM_COL)  
        			return f.getONCNum();
        		else if(col == REF_NUM_COL)  
        			return f.getReferenceNum();
        		else if(col == FAMILY_STATUS_COL)  
        			return fh.getFamilyStatus();
        		else if(col == GIFT_STATUS_COL)  
        			return fh.getGiftStatus();
        		else if(col == FIRST_NAME_COL)  
        			return f.getFirstName();
        		else if(col == LAST_NAME_COL)
        			return f.getLastName();
        		else if (col == SCHOOL_COL)
        			return  deliverySchool.getName();
        		else if (col == SCHOOL_TYPE_COL)
        			return  deliverySchool.getType();
        		else if (col == VOLUNTEER_COL)
        			return fh.getdDelBy();
        		else if (col == SL_COL)
        			return gvs.getImageIcon(23 + f.getStoplightPos());
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == SL_COL)
        			return ImageIcon.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Name, Status, Access and Permission are editable
        		return false;
        }
	}
	
	private class SchoolDelivery
	{
		ONCFamily family;
		School servedSchool;
		
		SchoolDelivery(ONCFamily f, School servedSchool)
		{
			this.family = f;
			this.servedSchool = servedSchool;
		}
		
		//getters
		ONCFamily getFamily() { return this.family; }
		School getServedSchool() { return this.servedSchool; }
	}
}
