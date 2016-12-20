package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class WarehouseSignInDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL = 1;
	private static final int GROUP_COL = 2;
	private static final int TIME_COL = 3;
	
	protected JPanel sortCriteriaPanel;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnPrint, btnClear;
	private VolunteerDB volDB;
	private List<WarehouseSignIn> whList;
		
	public WarehouseSignInDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Warehouse Sign-In Log");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
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
		JLabel lblONCicon = new JLabel(GlobalVariables.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
				
//		sortCriteriaPanel.add(sortCriteriaPanelTop);
//		sortCriteriaPanel.add(sortCriteriaPanelBottom);	
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"First Name", "Last Name", "Group", "Time"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
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
		int[] colWidths = {96, 96, 160, 128};
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
        
        //left justify wish count column
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.LEFT);
//      dlgTable.getColumnModel().getColumn(LOGINS_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the sign-in table list");
        btnPrint.addActionListener(this);
        
        btnClear = new JButton("Clear");
        btnClear.setToolTipText("Clear the sign-in table list");
        btnClear.addActionListener(this);
        
        cntlPanel.add(btnPrint);
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
				whList.add(new WarehouseSignIn(v.getfName(), v.getlName(), whv.getGroup(),
												whv.getCalTimestamp()));
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
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		}
	}

	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_DRIVER") ||
				dbe.getType().equals("UPDATED_DRIVER")))
		{
			//add the new sign-in to the first item in the list
			ONCVolunteer addedVol = (ONCVolunteer) dbe.getObject1();
			whList.add(0, new WarehouseSignIn(addedVol));
			
			//update the sign-in table
				dlgTableModel.fireTableRowsInserted(0, 0);;
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DRIVERS"))
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
		else if(e.getSource() == btnClear)
		{
			whList.clear();
			dlgTableModel.fireTableDataChanged();
		}
		
	}
	
	private class WarehouseSignIn
	{
		private String firstName;
		private String lastName;
		private String group;
		private long time;	//UTC time
		
		WarehouseSignIn(String fn, String ln, String group, Calendar time)
		{
			this.firstName = fn;
			this.lastName = ln;
			this.group = group;
			this.time = time.getTimeInMillis();
		}
		
		WarehouseSignIn(ONCVolunteer v)
		{
			this.firstName = v.getfName();
			this.lastName = v.getlName();
			this.group = v.getGroup();
			this.time = v.getDateChanged().getTime(); //last warehouse sign-in
		}
		
		//getters
		String getFirstName() { return firstName; }
		String getLastName() { return lastName; }
		String getGroup() { return group; }
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
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "Log-In Time"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return whList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	WarehouseSignIn whSignIn = whList.get(row);
        	if(col == FIRST_NAME_COL)  
        		return whSignIn.getFirstName();
        	else if(col == LAST_NAME_COL)
        		return whSignIn.getLastName();
        	else if (col == GROUP_COL)
        		return whSignIn.getGroup();
        	else if (col == TIME_COL)
        		return whSignIn.getTime();
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
        	//Name, Status, Access and Permission are editable
        	return false;
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
}
