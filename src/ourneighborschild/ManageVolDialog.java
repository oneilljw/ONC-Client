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
import java.util.ArrayList;
import java.util.Date;
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
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class ManageVolDialog extends ONCTableDialog implements ActionListener, ListSelectionListener,
														DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL = 1;
	private static final int GROUP_COL = 2;
	private static final int NUM_ACT_COL = 3;
	private static final int NUM_SIGNIN_COL = 4;
	private static final int TIME_COL = 5;
	private static final int NUM_TABLE_ROWS = 15;
	
	protected JPanel sortCriteriaPanel;
	private JComboBox activityCB;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnResetFilters, btnPrint, btnExport;
	private VolunteerDB volDB;
	
	private List<ONCVolunteer> tableList;
		
	public ManageVolDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Volunteer Managment");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		//set up the table list
		tableList = new ArrayList<ONCVolunteer>();
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariables.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		//create search fiters
		activityCB = new JComboBox(ActivityCode.getSearchFilterList());
		activityCB.setBorder(BorderFactory.createTitledBorder("Activitiy"));
		activityCB.setPreferredSize(new Dimension(200,56));
		activityCB.addActionListener(this);
		sortCriteriaPanel.add(activityCB);
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"First Name", "Last Name", "Group", "# of Actvities", 
								"# of Warehouse Sign-Ins", "Last Sign-In Time"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
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
		int[] colWidths = {96, 96, 192, 64, 56, 128};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
		dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        dlgTable.getColumnModel().getColumn(NUM_ACT_COL).setCellRenderer(dtcr);
        dlgTable.getColumnModel().getColumn(NUM_SIGNIN_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, dlgTable.getRowHeight()*NUM_TABLE_ROWS));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
    
        JPanel cntlPanel = new JPanel();
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Restore Table to .csv file");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the sign-in table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
       
        cntlPanel.add(btnExport);
        cntlPanel.add(btnPrint);
        cntlPanel.add(btnResetFilters);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void createTableList()
	{
		tableList.clear();
		for(ONCVolunteer v : volDB.getList())
			if(volunteerMeetsSearchCriteria(v))
				tableList.add(v);
		
		dlgTableModel.fireTableDataChanged();
	}
	
	void resetFilters()
	{
		activityCB.removeActionListener(this);
		activityCB.setSelectedIndex(0);
		activityCB.addActionListener(this);
		
		createTableList();
	}
	
	boolean volunteerMeetsSearchCriteria(ONCVolunteer v)
	{
		//test for ActitvityCode match
		ActivityCode cbCode = (ActivityCode) activityCB.getSelectedItem();
		int volCode = v.getActivityCode();
		
		if(cbCode == ActivityCode.Any || (volCode & cbCode.code()) > 0)
			return true;
		else
			return false;
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
			String err_mssg = "Unable to print Volunteer table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Volunteer Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		}
	}

	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_DRIVER") ||
				dbe.getType().equals("UPDATED_DRIVER")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DRIVERS"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Volunteer Management", GlobalVariables.getCurrentSeason()));
			createTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
						dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1)
		{
			ONCVolunteer selectedVol = tableList.get(modelRow);
			fireEntitySelected(this, EntityType.VOLUNTEER, selectedVol, null, null);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		if(e.getSource() == activityCB)
		{
			createTableList();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC Volunteer Table");
		}
		else if(e.getSource() == btnResetFilters)
		{
			resetFilters();
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.VOLUNTEER);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "# Activities", "# Sign-Ins", "Last Changed"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return tableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCVolunteer v = tableList.get(row);
        	
        	if(col == FIRST_NAME_COL)  
        		return v.getfName();
        	else if(col == LAST_NAME_COL)
        		return v.getlName();
        	else if (col == GROUP_COL)
        		return v.getGroup();
        	else if (col == NUM_ACT_COL)
        		return v.getNumberOfActivities();
        	else if (col == NUM_SIGNIN_COL)
        		return v.getSignIns();
        	else if (col == TIME_COL)
        		return v.getDateChanged();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == TIME_COL)
        		return Date.class;
        	else if(column == NUM_ACT_COL || column == NUM_SIGNIN_COL)
        		return Integer.class;
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
