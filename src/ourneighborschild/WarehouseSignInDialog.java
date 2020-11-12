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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import javax.swing.table.TableCellRenderer;

import au.com.bytecode.opencsv.CSVWriter;

public class WarehouseSignInDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener, ListSelectionListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL = 1;
	private static final int GROUP_COL = 2;
	private static final int TIME_COL = 3;
	private static final int DRIVER_NUM_COL = 4;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnPrint, btnClear, btnRefresh, btnExport;
	private VolunteerDB volDB;
	private List<WarehouseSignIn> whList;
	private SimpleDateFormat f;
	
		
	public WarehouseSignInDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Warehouse Sign-In Log");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		//create the list that the table will display
		whList = new LinkedList<WarehouseSignIn>();
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));

				
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
				
//		sortCriteriaPanel.add(sortCriteriaPanelTop);
//		sortCriteriaPanel.add(sortCriteriaPanelBottom);	
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"First Name", "Last Name", "Group", "Time", "Delivery Driver #"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
		f = new SimpleDateFormat("M/dd/yy H:mm:ss");
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm:ss");

		    public Component getTableCellRendererComponent(JTable table,Object value,
		            boolean isSelected, boolean hasFocus, int row, int column)
		    {
		        if( value instanceof Date)
		            value = f.format(value);
		        
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};
		dlgTable.getColumnModel().getColumn(TIME_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {96, 96, 160, 128, 48};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
//      dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //center justify driver number column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        dlgTable.getColumnModel().getColumn(DRIVER_NUM_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnRefresh = new JButton("Refresh");
        btnRefresh.setToolTipText("Refresh the sign-in table list");
        btnRefresh.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the sign-in table list");
        btnPrint.addActionListener(this);
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Export the sign-in table list");
        btnExport.addActionListener(this);
        
        btnClear = new JButton("Clear");
        btnClear.setToolTipText("Clear the sign-in table list");
        btnClear.addActionListener(this);
        
        cntlPanel.add(btnRefresh);
        cntlPanel.add(btnPrint);
        cntlPanel.add(btnExport);
        cntlPanel.add(btnClear);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
        this.setMinimumSize(new Dimension(tablewidth, 440));
	}
	
	void createWarehouseSignInList()
	{
		whList.clear();
		List<ONCWarehouseVolunteer> volList = volDB.getWarehouseHistory(-1);
		
		for(ONCWarehouseVolunteer whv : volList)
		{
			ONCVolunteer v = volDB.getVolunteer(whv.getVolunteerID());
			if(v != null)
				whList.add(new WarehouseSignIn(v, whv.getCalTimestamp()));
		}
		
		Collections.sort(whList, new WHDateComparator());
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             dlgTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print sign-in log: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Sign-In Log Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		List<String> headerList = new ArrayList<String>();
		headerList.add("First Name");
		headerList.add("Last Name");
		headerList.add("Group");
		headerList.add("Sign-In Time");
		headerList.add("Driver Number");
		String[] header = headerList.toArray(new String[0]);
			
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of sign-in log" ,
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
	    	    
       			for(WarehouseSignIn whsi : whList)
       				writer.writeNext(whsi.getExportRow());
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, String.format("%d volunteer sign-ins sucessfully exported to %s", 
       					whList.size(), oncwritefile.getName()), 
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
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_WAREHOUSE_VOLUNTEER")))
		{
			//add the new sign-in to the first item in the list
			ONCVolunteer addedVol = (ONCVolunteer) dbe.getObject1();
			whList.add(0, new WarehouseSignIn(addedVol));
			
			//update the sign-in table
			dlgTableModel.fireTableRowsInserted(0, 0);
		}
		if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_DRIVER")))
		{
			//see if the volunteer is in the list, if they are then update the table. 
			ONCVolunteer updatedVol = (ONCVolunteer) dbe.getObject1();

			int index = 0;
			while(index < whList.size() && updatedVol.getID() != whList.get(index).getVolunteer().getID())
				index++;
					
			if(index < whList.size())
			{
				//found the volunteer who changed, update the warehouse sign in
//				System.out.println(String.format("WareSignInDlg.dataChg: updatedDriverFound: %d, %s", updatedVol.getID(), updatedVol.getLastName()));
				whList.get(index).setUpdatedVolunteer(updatedVol);
				
				int tableRow = dlgTable.convertRowIndexToView(index);
				dlgTableModel.fireTableRowsUpdated(tableRow, tableRow);
			}	
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//get the initial data and display
	        createWarehouseSignInList();
	        dlgTableModel.fireTableDataChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnPrint)
		{
			print("Warehouse Sign-In Log");
		}
		if(e.getSource() == btnExport)
		{
			onExportRequested();
		}
		else if(e.getSource() == btnClear)
		{
			whList.clear();
			dlgTableModel.fireTableDataChanged();
		}
		else if(e.getSource() == btnRefresh)
		{
			createWarehouseSignInList();
			dlgTableModel.fireTableDataChanged();
		}
		
	}
	
	private class WarehouseSignIn
	{
		private ONCVolunteer vol;
		private long time;	//UTC time
		
		WarehouseSignIn(ONCVolunteer vol, Calendar time)
		{
			this.vol = vol;
			this.time = time.getTimeInMillis();
		}
		
		WarehouseSignIn(ONCVolunteer v)
		{
			this.vol = v;
			this.time = v.getTimestampDate().getTime(); //last warehouse sign-in
		}
		
		//getters
		ONCVolunteer getVolunteer() { return vol; }
		Date getTime() //return local time
		{  
			TimeZone tz = TimeZone.getDefault();	//Local time zone
			int offsetFromUTC = tz.getOffset(time);
			 
			//create a new calendar in local time zone, set to gmtDOB and add the offset
			Calendar localCal = Calendar.getInstance();
			localCal.setTimeInMillis(time);
			localCal.add(Calendar.MILLISECOND, offsetFromUTC);

			return localCal.getTime();
		}
		String getDriverNum() { return vol.getDrvNum(); }
		
		//setters
		void setUpdatedVolunteer(ONCVolunteer updatedVolunteer) { this.vol = updatedVolunteer; }
		
		String[] getExportRow()
		{
			String[] row = new String[5];
			row[0] = vol.getFirstName();
			row[1] = vol.getLastName();
			row[2] = vol.getOrganization();
			
			//create a new calendar in local time zone, set to gmtDOB and add the offset
			TimeZone tz = TimeZone.getDefault();	//Local time zone
			int offsetFromUTC = tz.getOffset(time);
			Calendar localCal = Calendar.getInstance();
			localCal.setTimeInMillis(time);
			localCal.add(Calendar.MILLISECOND, offsetFromUTC);
			
			row[3] = f.format(localCal.getTime());
			row[4] = vol.getDrvNum();
			
			return row;
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "Log-In Time", "Drv. #"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return whList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		WarehouseSignIn whSignIn = whList.get(row);
        		if(col == FIRST_NAME_COL)  
        			return whSignIn.getVolunteer().getFirstName();
        		else if(col == LAST_NAME_COL)
        			return whSignIn.getVolunteer().getLastName();
        		else if (col == GROUP_COL)
        			return whSignIn.getVolunteer().getOrganization();
        		else if (col == TIME_COL)
        			return whSignIn.getTime();
        		else if (col == DRIVER_NUM_COL)
        			return whSignIn.getDriverNum();
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == TIME_COL)
        			return Date.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		//Driver number is editable
        		return col == DRIVER_NUM_COL;
        }
        
        public void setValueAt(Object value, int row, int col)
        { 
        		ONCVolunteer selectedVol = whList.get(row).getVolunteer();
        	
        		//determine if the user made a change to the volunteer driver number
        		if(col == DRIVER_NUM_COL && !selectedVol.getDrvNum().equals((String)value))
        		{
        			ONCVolunteer reqUpdateVol = new ONCVolunteer(selectedVol);	//make a copy
        			reqUpdateVol.setDrvNum(((String) value));
        		
        			//if the user made a change to the driver number, attempt to update the volunteer object
        			if(reqUpdateVol != null)
        			{
        				String response = volDB.update(this, reqUpdateVol);        		
        				if(response == null || (response !=null && !response.startsWith("UPDATED_DRIVER")))
        				{
        					//request failed
        					String err_mssg = "ONC Server denied update volunteer request, try again later";
        					JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), err_mssg, "Update Volunteer Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
        					
        					dlgTableModel.fireTableDataChanged();
        				}
        			}
        		}
        }
	}
	
	private class WHDateComparator implements Comparator<WarehouseSignIn>
	{
		public int compare(WarehouseSignIn o1, WarehouseSignIn o2)
		{
			if(o1.getTime().before(o2.getTime()))
				return 1;
			else if(o1.getTime().after(o2.getTime())) 
				return -1;
			else 
				return 0;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getSource() == dlgTable.getSelectionModel())
		{
			int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
			dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
			if(modelRow > -1)
			{
				WarehouseSignIn si = whList.get(modelRow);
				fireEntitySelected(this, EntityType.VOLUNTEER, si.getVolunteer(), null, null);
			}
		}		
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.VOLUNTEER);
	}
}
