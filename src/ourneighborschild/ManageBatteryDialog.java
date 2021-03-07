package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import au.com.bytecode.opencsv.CSVWriter;

public class ManageBatteryDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener,
																		ListSelectionListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int CLONED_GIFT_FIRST_GIFT_NUMBER = 3;
	
	private static final int ONC_NUM_COL= 0;
	private static final int CHILD_FIRST_NAME_COL = 1;
	private static final int CHILD_LAST_NAME_COL = 2;
	private static final int CHILD_AGE_COL = 3;
	private static final int CHILD_GENDER_COL = 4;
	private static final int GIFT_COL = 5;
	private static final int CLONE_COL = 6;
	private static final int SIZE_COL = 7;
	private static final int QTY_COL = 8;
	
	private static final int NUM_TABLE_ROWS = 12;
	
	protected JPanel sortCriteriaPanel;
	private JComboBox<String> sizeCB, cloneCB;
	private boolean bChangingTable;
	private String sortSize, sortClone;
	
	private ONCTable batteryTable;
	private AbstractTableModel batteryTableModel;
	private JButton btnReset;
	private JComboBox<String> printCB, exportCB;
	private String[] printChoices, exportChoices;
	private JLabel lblCount;
	
	private BatteryDB batteryDB;
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildGiftDB giftDB;
	private ClonedGiftDB clonedGiftDB;
	private GiftCatalogDB catDB;
	private UserDB userDB;
	
	private List<BatteryTableObject> batteryTableList;
	
	public ManageBatteryDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Battery Managment");
		
		//Save the reference to data bases 
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		batteryDB = BatteryDB.getInstance();
		if(batteryDB != null)
			batteryDB.addDatabaseListener(this);
		
		giftDB = ChildGiftDB.getInstance();
		if(giftDB != null)
			giftDB.addDatabaseListener(this);
		
		clonedGiftDB = ClonedGiftDB.getInstance();
		if(clonedGiftDB != null)
			clonedGiftDB.addDatabaseListener(this);
		
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		catDB = GiftCatalogDB.getInstance();
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//set up the table list
		batteryTableList = new ArrayList<BatteryTableObject>();
		
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
		
		sizeCB = new JComboBox<String>(BatterySize.filterList());
		sizeCB.setBorder(BorderFactory.createTitledBorder("Battery Size"));
		sizeCB.setPreferredSize(new Dimension(200,56));
		sizeCB.addActionListener(this);
		sortCriteriaPanel.add(sizeCB);
		sortSize = "Any";
		
		String[] choices = new String[] {"Any","Yes","No"};
		cloneCB = new JComboBox<String>(choices);
		cloneCB.setBorder(BorderFactory.createTitledBorder("Cloned Gift ?"));
		cloneCB.setPreferredSize(new Dimension(120,56));
		cloneCB.addActionListener(this);
		sortCriteriaPanel.add(cloneCB);
		sortClone = "Any";
		
		//Create the volunteer table model
		batteryTableModel = new BatteryTableModel();
		
		//create the table
		String[] colToolTips = {"ONC #", "Child First Name", "Child Last Name", "Child's Age", "Childs Gender",
								"Gift", "Is a cloned gift?", "Battery Type", "Qty"};
		
		batteryTable = new ONCTable(batteryTableModel, colToolTips, new Color(240,248,255));

		batteryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		batteryTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {40, 96, 96,64, 48, 240, 56, 80, 32};
		for(int col=0; col < colWidths.length; col++)
		{
			batteryTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		batteryTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = batteryTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        batteryTable.getColumnModel().getColumn(SIZE_COL).setCellRenderer(dtcr);
        batteryTable.getColumnModel().getColumn(QTY_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(batteryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, batteryTable.getRowHeight()*NUM_TABLE_ROWS));
//      dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setBorder(BorderFactory.createTitledBorder("Batteries"));
        
        //create the volunteer table control panel
        JPanel batteryCntlPanel = new JPanel();
        batteryCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblCount = new JLabel("Batteries Meeting Criteria: 0, Quantity; 0");
        batteryCntlPanel.add(lblCount);
    
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        exportChoices = new String[] {"Export", "Gift Battery Summary"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setToolTipText("Export to .csv file");
        exportCB.setEnabled(true);
        exportCB.addActionListener(this);
        
        printChoices = new String[] {"Print", "Table", "Battery Sheet"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setToolTipText("Print the sign-in table list");
        printCB.addActionListener(this);
        
        btnReset = new JButton("Reset Filters");
        btnReset.setToolTipText("Restore Filters to Defalut State");
        btnReset.addActionListener(this);
       
        btnPanel.add(exportCB);
        btnPanel.add(printCB);
        btnPanel.add(btnReset);
        
        cntlPanel.add(infoPanel);
        cntlPanel.add(btnPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(batteryCntlPanel);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	void createTableList()
	{
		bChangingTable = true;
		
		batteryTableList.clear();
		
		for(Battery b : batteryDB.getList())
			if(doesSizeFilterMatch(b) && doesCloneFilterMatch(b))
				batteryTableList.add(new BatteryTableObject(b));
		
		lblCount.setText(String.format("Batteries Meeting Criteria: %d, Quantity: %d", 
							batteryTableList.size(), getBatteryQtyCount()));
		
		batteryTableModel.fireTableDataChanged();
		
		bChangingTable = false;
	}
	
	int getBatteryQtyCount()
	{
		int count = 0;
		for(BatteryTableObject bto : batteryTableList)
			count += bto.getBattery().getQuantity();
		
		return count;
	}
	
	void resetFilters()
	{	
		sizeCB.removeActionListener(this);
		sizeCB.setSelectedIndex(0);
		sizeCB.addActionListener(this);
		sortSize = "Any";
		
		cloneCB.removeActionListener(this);
		cloneCB.setSelectedIndex(0);
		cloneCB.addActionListener(this);
		sortClone = "Any";
		
		createTableList();
	}

	boolean doesSizeFilterMatch(Battery b)
	{
		//test for size match
		 return sortSize.equals("Any") || b.getSize().equals(sortSize);
	}
	
	boolean doesCloneFilterMatch(Battery b)
	{
		//test for size match
		 return sortClone.equals("Any") || 
				 b.getWishNum() < CLONED_GIFT_FIRST_GIFT_NUMBER && sortClone.equals("No") ||
		 		  b.getWishNum() >= CLONED_GIFT_FIRST_GIFT_NUMBER && sortClone.equals("Yes");
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             batteryTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print Battery table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Battery Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	void printBatterySheet()
	{
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(new BatteryBarcodeSheetPrinter());
         
		boolean ok = pj.printDialog();
		if (ok)
		{
			try { pj.print(); }
			catch (PrinterException ex) { /* The job did not successfully complete */ }       
		}
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"ONC #", "Child FN", "Child LN", "Age", "Gender", "Gift Type", "Clone?", "Detail", "Battery Size", "Qty"};
    
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
	    	    
       			for(BatteryTableObject bto : batteryTableList)
       				writer.writeNext(bto.getExportRow());
       				
 //      		int[] row_sel = batteryTable.getSelectedRows();
 //    			for(int i=0; i < batteryTable.getSelectedRowCount(); i++)
 //      		{
 //      			int index = row_sel[i];
 //      			writer.writeNext(batteryTableList.get(index).getExportRow());
 //      		}
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						batteryTable.getRowCount() + " gifts with batteries sucessfully exported to " + oncwritefile.getName(), 
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
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_BATTERY") ||
				dbe.getType().equals("UPDATED_BATTERY") || dbe.getType().equals("DELETED_BATTERY")))
		{
			createTableList(); //update the table
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Battery Management", gvs.getCurrentSeason()));
			createTableList();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == sizeCB && !sortSize.equals((String) sizeCB.getSelectedItem()))
		{
			sortSize = (String) sizeCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == cloneCB)
		{
			sortClone = (String) cloneCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == btnReset)
		{
			resetFilters();
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
				print("Gift Batteries");
			else if(printCB.getSelectedIndex() == 2)
				printBatterySheet();
			
			printCB.setSelectedIndex(0);
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		//If user selects a battery in the table, notify the entity listeners the selection occurred 
//		ListSelectionModel stLSM = batteryTable.getSelectionModel();
//		if(!lse.getValueIsAdjusting() && lse.getSource() == stLSM && batteryTable.getSelectedRow() > -1 && !bChangingTable)
//		{
//			BatteryTableObject bto = batteryTableList.get(batteryTable.getSelectedRow());
//			fireEntitySelected(this, EntityType.WISH, bto.getFamily(), bto.getChild());
//		}
		
		if(!lse.getValueIsAdjusting() && !bChangingTable)
		{
			int modelRow = batteryTable.getSelectedRow() == -1 ? -1 : 
				batteryTable.convertRowIndexToModel(batteryTable.getSelectedRow());
			
			if(modelRow > -1)
			{
				BatteryTableObject bto = batteryTableList.get(modelRow);
				this.fireEntitySelected(this, EntityType.GIFT, bto.getFamily(), bto.getChild());
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.GIFT);
	}
	
	class BatteryTableModel extends AbstractTableModel
	{
        /**
		 * Implements the battery table model
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"ONC #", "Child FN", "Child LN", "Age", "Gender", "Gift", "Clone ?", "Batteries", "Qty",};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return batteryTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
    		BatteryTableObject bto = batteryTableList.get(row);
    		ONCChildGift gift = null;
    		ONCChild child = null;
    		ONCFamily family = null;
    		
    		boolean bClonedGift = bto.getBattery().getWishNum() >= CLONED_GIFT_FIRST_GIFT_NUMBER;
    		
    		if(bClonedGift)
    			gift = clonedGiftDB.getClonedGift(bto.getChild().getID(), bto.getBattery().getWishNum());
    		else
    			gift = giftDB.getCurrentChildGift(bto.getChild().getID(), bto.getBattery().getWishNum());
    		
    		String childFN, childLN;
    		if(gift != null)
    		{
    			child = childDB.getChild(gift.getChildID());
    			
    			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
        		{
        			//Only display child's actual name if user permission permits
        			childFN = child.getChildFirstName();
        			childLN = child.getChildLastName();
        		}
        		else
        		{
        			//Else, display the restricted name for the child
        			childFN = "Child " + childDB.getChildNumber(child);
        			childLN = "";
        		}
    		}
    		else
    		{
    			childFN = "Error";
    			childLN = "Error:";
    		}
        		
    		if(child != null)
    			family = familyDB.getFamily(child.getFamID());
    		
    		
    		if(col == ONC_NUM_COL)  
    			return family == null ? "Error" : family.getONCNum();
       		else if(col == CHILD_FIRST_NAME_COL)
       			return childFN;
    		else if (col == CHILD_LAST_NAME_COL)
    			return childLN;
    		else if(col == CHILD_AGE_COL)
       			return child == null ? "Error" : child.getChildAge();
        		else if (col == CHILD_GENDER_COL)
        			return child == null ? "Error" : child.getChildGender();
    		else if (col == GIFT_COL)
    		{
    			if(gift != null && !bClonedGift)
    			{
    				ONCGift catalogGift = catDB.getGiftByID(gift.getCatalogGiftID());
    				return  catalogGift == null ? "Error" : catalogGift.getName().equals("-") ? 
    						gift.getDetail() : catalogGift.getName() + "- " + gift.getDetail();
    			}
    			else
    				return "Error";
    		}
    		else if(col == CLONE_COL)
    			return bClonedGift;
    		else if (col == SIZE_COL)
    			return bto.getBattery().getSize();
    		else if (col == QTY_COL)
    			return bto.getBattery().getQuantity();
    		else
    			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == QTY_COL)
        			return Integer.class;
        		else if(column == CLONE_COL)
        			return Boolean.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Name, Status, Access and Permission are editable
        		return false;
        }
	}
	
	private class BatteryTableObject
	{
		private ONCFamily family;
		private ONCChild child;
		private Battery battery;
		
		BatteryTableObject(Battery battery)
		{
			this.battery = battery;
			this.child = childDB.getChild(battery.getChildID());
			if(child != null)
				family = familyDB.getFamily(child.getFamID());
			else
				family = null;
		}
		
		ONCFamily getFamily() { return family; }
		ONCChild getChild() { return child; }
		Battery getBattery() { return battery; }
		
		String[] getExportRow()
		{
			String[] row = new String[10];
			
			ONCChildGift gift = null;
			String giftDetail = "Error";
			
			if(battery.getWishNum() <  CLONED_GIFT_FIRST_GIFT_NUMBER)
				gift = giftDB.getCurrentChildGift(battery.getChildID(), battery.getWishNum());
			else
				gift = clonedGiftDB.getClonedGift(battery.getChildID(), battery.getWishNum());
				
			ONCGift catalogGift = catDB.getGiftByID(gift.getCatalogGiftID());
			giftDetail = gift.getDetail();
	
			
			//determine if user has permission to see child first and last name. If not, substitute
			String childFN = "Error", childLN = "Error";
			child = childDB.getChild(battery.getChildID());
			if(battery != null && (child = childDB.getChild(battery.getChildID())) != null)
			{
				if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
				{
					//Only display child's actual name if user permission permits
					childFN = child.getChildFirstName();
					childLN = child.getChildLastName();
				}
				else
				{
					//Else, display the restricted name for the child
					childFN = "Child " + childDB.getChildNumber(child);
					childLN = "";
				}
			}
  
			row[0] = family == null ? "Error" : family.getONCNum();
			row[1] = childFN;
			row[2] = childLN;
			row[3] = child == null ? "Error" : child.getChildAge();
			row[4] = child == null ? "Error" : child.getChildGender();
			row[5] = catalogGift == null ? "Error" : catalogGift.getName();
			row[6] = battery == null ? "Error" : battery.getWishNum() < CLONED_GIFT_FIRST_GIFT_NUMBER ? "No":"Yes";
			row[7] = giftDetail;
			row[8] = battery.getSize();
			row[9] = Integer.toString(battery.getQuantity());
			
			return row;	
		}
	}
}
