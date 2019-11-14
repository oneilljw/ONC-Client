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
import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import au.com.bytecode.opencsv.CSVWriter;

public class ManageSMSDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener,
																		ListSelectionListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int ENTITY_ID_COL= 0;
	private static final int ENTITY_TYPE_COL = 1;
	private static final int PHONE_NUMBER_COL = 2;
	private static final int DIRECTION_COL = 3;
	private static final int BODY_COL = 4;
	private static final int STATUS_COL = 5;
	private static final int TIMESTAMP_COL = 6;
	
	private static final int NUM_TABLE_ROWS = 12;
	
	protected JPanel sortCriteriaPanel;
	private boolean bChangingTable;
	
	private String sortONCNum;
	private SMSDirection sortDirection;
	private SMSStatus sortStatus;
	
	private ONCTable smsTable;
	private AbstractTableModel smsTableModel;
	private JButton btnReset;
	private JTextField oncnumTF;
	private JComboBox<String> printCB, exportCB;
	private JComboBox<SMSDirection> directionCB;
	private JComboBox<SMSStatus> statusCB;
	private String[] printChoices, exportChoices;
	private JLabel lblCount;
	
	private SMSDB smsDB;
	private FamilyDB familyDB;
	
	private List<ONCSMS> smsTableList;
	
	public ManageSMSDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("SMS Managment");
		
		//Save the reference to data bases 
		smsDB = SMSDB.getInstance();
		if(smsDB != null)
			smsDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);

		//set up the table list
		smsTableList = new ArrayList<ONCSMS>();
		
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
		
		oncnumTF = new JTextField(3);
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.addActionListener(this);
		sortCriteriaPanel.add(oncnumTF);
		sortONCNum = "";
		
		directionCB = new JComboBox<SMSDirection>(SMSDirection.values());
		directionCB.setBorder(BorderFactory.createTitledBorder("Direction"));
		directionCB.addActionListener(this);
		sortCriteriaPanel.add(directionCB);
		sortDirection = SMSDirection.ANY;
		
		statusCB = new JComboBox<SMSStatus>(SMSStatus.values());
		statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
		statusCB.addActionListener(this);
		sortCriteriaPanel.add(statusCB);
		sortStatus = SMSStatus.ANY;
		
		//Create the volunteer table model
		smsTableModel = new SMSTableModel();
		
		//create the table
		String[] colToolTips = {"Message SID", "Entity ID", "Entity Type", "Phone Number", "Direction",
								"Body", "Status","Time Stamp"};
		
		smsTable = new ONCTable(smsTableModel, colToolTips, new Color(240,248,255));

		smsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		smsTable.getSelectionModel().addListSelectionListener(this);
		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm:ss");

			public Component getTableCellRendererComponent(JTable table, Object value,
				            boolean isSelected, boolean hasFocus, int row, int column)
			{ 
				if(value instanceof java.util.Date)
					value = f.format(value);
				        
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
		smsTable.getColumnModel().getColumn(TIMESTAMP_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {144, 72, 64, 104, 80, 360, 80, 144};
		for(int col=0; col < colWidths.length; col++)
		{
			smsTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		smsTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = smsTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//      smsTable.getColumnModel().getColumn(SIZE_COL).setCellRenderer(dtcr);
//      smsTable.getColumnModel().getColumn(QTY_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(smsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, smsTable.getRowHeight()*NUM_TABLE_ROWS));
//      dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setBorder(BorderFactory.createTitledBorder("SMS Messages"));
        
        //create the message table control panel
        JPanel messageCntlPanel = new JPanel();
        messageCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblCount = new JLabel("Messages Meeting Criteria: 0, Quantity; 0");
        messageCntlPanel.add(lblCount);
    
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        exportChoices = new String[] {"Export", "SMS Message Summary"};
        exportCB = new JComboBox<String>(exportChoices);
        exportCB.setToolTipText("Export to .csv file");
        exportCB.setEnabled(true);
        exportCB.addActionListener(this);
        
        printChoices = new String[] {"Print", "Table"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setToolTipText("Print the SMS table list");
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
        getContentPane().add(messageCntlPanel);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	void createTableList()
	{
		bChangingTable = true;
		
		smsTableList.clear();
		
		for(ONCSMS sms : smsDB.getList())
			if(doesDirectionFilterMatch(sms) && doesStatusFilterMatch(sms) && doesONCNumFilterMatch(sms))
				smsTableList.add(sms);
		
		lblCount.setText(String.format("Messages Meeting Criteria: %d", smsTableList.size()));
		
		smsTableModel.fireTableDataChanged();
		
		bChangingTable = false;
	}
	
	void resetFilters()
	{	
		oncnumTF.removeActionListener(this);
		oncnumTF.setText("");
		oncnumTF.addActionListener(this);
		sortONCNum = "";
		
		sortDirection = SMSDirection.ANY;
		directionCB.removeActionListener(this);
		directionCB.setSelectedIndex(0);
		directionCB.addActionListener(this);
		sortDirection = SMSDirection.ANY;
		
		statusCB.removeActionListener(this);
		statusCB.setSelectedIndex(0);
		statusCB.addActionListener(this);
		sortStatus = SMSStatus.ANY;
		
		createTableList();
	}
	
	boolean doesONCNumFilterMatch(ONCSMS sms)
	{
		//find ONC Num
		ONCFamily fam = familyDB.getFamily(sms.getEntityID());
		if(fam != null)
			return sortONCNum.isEmpty() || fam.getONCNum().equals(oncnumTF.getText());
		else
			return false;
	}

	boolean doesDirectionFilterMatch(ONCSMS sms)
	{
		//test for direction
		 return sortDirection == SMSDirection.ANY || sms.getDirection() == (SMSDirection) directionCB.getSelectedItem();
	}
	
	boolean doesStatusFilterMatch(ONCSMS sms)
	{
		//test for status
		 return sortStatus == SMSStatus.ANY || sms.getStatus() == (SMSStatus) statusCB.getSelectedItem();
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             smsTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print Message table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Message Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"ID", "Entity Type", "Phone #", "Direction", "Body", "Timestamp"};
    
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
	    	    
       			for(ONCSMS sms : smsTableList)
       				writer.writeNext(sms.getExportRow());

       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						smsTable.getRowCount() + " messages sucessfully exported to " + oncwritefile.getName(), 
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
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_SMS") ||
				dbe.getType().equals("UPDATED_SMS") || dbe.getType().equals("DELETED_SMS")))
		{
			createTableList(); //update the table
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			createTableList(); //update the table
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_SMS"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Message Management", GlobalVariablesDB.getCurrentSeason()));
			createTableList();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			createTableList();
		}
		else if(e.getSource() == directionCB && sortDirection != (SMSDirection) directionCB.getSelectedItem())
		{
			sortDirection = (SMSDirection) directionCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == statusCB && sortStatus != (SMSStatus) statusCB.getSelectedItem())
		{
			sortStatus = (SMSStatus) statusCB.getSelectedItem();
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
			
			printCB.setSelectedIndex(0);
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		//If user selects a SMS in the table, notify the entity listeners the selection occurred 
		if(!lse.getValueIsAdjusting() && !bChangingTable)
		{
			int modelRow = smsTable.getSelectedRow() == -1 ? -1 : 
				smsTable.convertRowIndexToModel(smsTable.getSelectedRow());
			
			if(modelRow > -1)
			{
//				ONCSMS sms = smsTableList.get(modelRow);
//				this.fireEntitySelected(this, EntityType.GIFT, sms.getFamily(), sms.getChild());
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY, EntityType.PARTNER);
	}
	
	class SMSTableModel extends AbstractTableModel
	{
        /**
		 * Implements the battery table model
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Message SID", "ID", "Entity Type", "Phone Number", "Direction", 
										"Body", "Status","Time Stamp"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return smsTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		ONCSMS sms = smsTableList.get(row);

        		if(col == ENTITY_ID_COL)
        		{
        			if(sms.getType() == EntityType.FAMILY)
        			{
        				ONCFamily f = familyDB.getFamily(sms.getEntityID());
        				if(f != null)
        					return f.getONCNum();
        				else
        					return "UNK";
        			}
        			else
        				return Integer.toString(sms.getEntityID());
        		}
       		else if(col == ENTITY_TYPE_COL)
       			return sms.getType().toString();
        		else if (col == PHONE_NUMBER_COL)
        			return sms.getPhoneNum();
        		else if(col == DIRECTION_COL)
           			return sms.getDirection().toString();
        		else if(col == BODY_COL)
        			return sms.getBody();
        		else if(col == STATUS_COL)
        			return sms.getStatus().toString();
        		else if (col == TIMESTAMP_COL)
        		{
        			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        			calendar.setTimeInMillis(sms.getTimestamp());
        			return calendar.getTime();
        		}
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == TIMESTAMP_COL)
        			return Date.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Name, Status, Access and Permission are editable
        		return false;
        }
	}
}
