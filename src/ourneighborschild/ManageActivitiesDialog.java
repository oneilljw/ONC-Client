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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class ManageActivitiesDialog extends ONCTableDialog implements ActionListener, ListSelectionListener,
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

	private static final int ACT_NAME_COL= 0;
	private static final int ACT_START_DATE_COL = 1;
	private static final int ACT_START_TIME_COL = 2;
	private static final int ACT_END_DATE_COL = 3;
	private static final int ACT_END_TIME_COL = 4;

	private static final int NUM_VOL_TABLE_ROWS = 12;
	private static final int NUM_ACT_TABLE_ROWS = 15;

	protected JPanel sortCriteriaPanel;
	private JComboBox activityCB;
	private DefaultComboBoxModel activityCBM;
	private boolean bIgnoreCBEvents;
	private String sortActivityCategory;

	private ONCTable volTable, actTable;
	private AbstractTableModel volTableModel, actTableModel;
	private JButton btnResetFilters, btnPrint, btnExport;
	private JLabel lblActCount;

	private VolunteerDB volDB;
	private ActivityDB activityDB;

	private List<VolunteerActivity> actTableList;
	private List<ONCVolunteer> volTableList;
	private VolunteerActivity selectedAct;

	public ManageActivitiesDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Activity Managment");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		//set up the table lists
		actTableList = new ArrayList<VolunteerActivity>();
		volTableList = new ArrayList<ONCVolunteer>();
		
		bIgnoreCBEvents = false;
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariables.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		//create search filters
		activityCBM = new DefaultComboBoxModel();
	    activityCBM.addElement("Any");
		activityCB = new JComboBox(activityCBM);
		activityCB.setBorder(BorderFactory.createTitledBorder("Activitiy"));
		activityCB.setPreferredSize(new Dimension(200,56));
		activityCB.addActionListener(this);
		sortCriteriaPanel.add(activityCB);
		sortActivityCategory = "Any";

        //create the activity table
      	actTableModel = new ActivityTableModel();
       	String[] actToolTips = {"Name", "Start Date", "Start Time", "End Date", "End Time"};
      	actTable = new ONCTable(actTableModel, actToolTips, new Color(240,248,255));

     	actTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      	actTable.getSelectionModel().addListSelectionListener(this);
      		
      	//Set table column widths
      	int tablewidth = 0;
      	int[] act_colWidths = {256, 80, 80, 80, 80};
      	for(int col=0; col < act_colWidths.length; col++)
      	{
      		actTable.getColumnModel().getColumn(col).setPreferredWidth(act_colWidths[col]);
      		tablewidth += act_colWidths[col];
      	}
      	tablewidth += 24; 	//count for vertical scroll bar
      		
      	JTableHeader anHeader = actTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
              
        //Center justify wish count column
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//      volTable.getColumnModel().getColumn(NUM_ACT_COL).setCellRenderer(dtcr);
//      volTable.getColumnModel().getColumn(NUM_SIGNIN_COL).setCellRenderer(dtcr);
              
        //Create the scroll pane and add the table to it.
        JScrollPane actScrollPane = new JScrollPane(actTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
      													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actScrollPane.setPreferredSize(new Dimension(tablewidth, actTable.getRowHeight()*NUM_ACT_TABLE_ROWS));
        actScrollPane.setBorder(BorderFactory.createTitledBorder("Activites"));
        
        //create the activity table control panel
        JPanel actCntlPanel = new JPanel();
        actCntlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        lblActCount = new JLabel("Activities Meeting Criteria: 0");
        actCntlPanel.add(lblActCount);
        
		//Create the volunteer table model and table
		volTableModel = new VolunteerTableModel();
		String[] colToolTips = {"First Name", "Last Name", "Group", "# of Actvities", 
								"# of Warehouse Sign-Ins", "Last Sign-In Time"};
		
		volTable = new ONCTable(volTableModel, colToolTips, new Color(240,248,255));
		volTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
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
		volTable.getColumnModel().getColumn(TIME_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		tablewidth = 0;
		int[] colWidths = {96, 96, 192, 80, 64, 128};
		for(int col=0; col < colWidths.length; col++)
		{
			volTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
//		volTable.setAutoCreateRowSorter(true);	//add a sorter
        
        anHeader = volTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center justify columns
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        volTable.getColumnModel().getColumn(NUM_ACT_COL).setCellRenderer(dtcr);
        volTable.getColumnModel().getColumn(NUM_SIGNIN_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane volScrollPane = new JScrollPane(volTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        volScrollPane.setPreferredSize(new Dimension(tablewidth, volTable.getRowHeight()*NUM_VOL_TABLE_ROWS));
        volScrollPane.setBorder(BorderFactory.createTitledBorder("Volunteers for selected activity"));
        
        //create the control panel
        JPanel cntlPanel = new JPanel();
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Export Activity Table to .csv file");
        btnExport.setEnabled(false);
        btnExport.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the activity table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
       
        cntlPanel.add(btnExport);
        cntlPanel.add(btnPrint);
        cntlPanel.add(btnResetFilters);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(actScrollPane);
        getContentPane().add(actCntlPanel);
        getContentPane().add(volScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	@SuppressWarnings("unchecked")
	void createTableList()
	{	
		actTableList.clear();
		
		for(VolunteerActivity va : (List<VolunteerActivity>) activityDB.getList())
			if(doesActivityMatch(va))
				actTableList.add(va);
		
		lblActCount.setText(String.format("Activities Meeting Criteria: %d", actTableList.size()));
		
		actTableModel.fireTableDataChanged();
	}
	
	void resetFilters()
	{
		activityCB.removeActionListener(this);
		activityCB.setSelectedIndex(0);
		activityCB.addActionListener(this);
		sortActivityCategory = "Any";
		
		selectedAct = null;
		actTableModel.fireTableDataChanged();
		
		createTableList();
	}
	
	boolean doesActivityMatch(VolunteerActivity va)
	{
		//test for activity category match
		 return sortActivityCategory.equals("Any") || va.getCategory().equals(sortActivityCategory);
	}
	
	void updateActivityList()
	{	
		bIgnoreCBEvents = true;
		activityCB.setEnabled(false);
		String curr_sel = activityCB.getSelectedItem().toString();
		
		activityCBM.removeAllElements();
		
		activityCBM.addElement("Any");
		
		boolean currSelFound = false;
		for(String category : activityDB.getActivityCategoryList())
		{
			activityCBM.addElement(category);
			if(curr_sel.equals(category))
				currSelFound = true;		
		}
		
		//reset selection to previous selection. If previous selection is no longer
		//available, reset to index and rebuild
		if(currSelFound = true)
			activityCB.setSelectedItem(currSelFound);
		else
		{
			activityCB.setSelectedItem("Any");
			sortActivityCategory = "Any";
			createTableList();
		}
			
		activityCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             actTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print Activities table: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Activity Table Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		}
	}	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_ACTIVITY") ||
				dbe.getType().equals("UPDATED_ACTIVITY")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("CATEGOR"))
		{
			updateActivityList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_ACTIVITIES"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Activity Management", GlobalVariables.getCurrentSeason()));
			createTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = actTable.getSelectedRow() == -1 ? -1 : 
						actTable.convertRowIndexToModel(actTable.getSelectedRow());
		
		if(modelRow > -1)
		{
			selectedAct = actTableList.get(modelRow);
			volTableList = volDB.getVolunteersForActivity(selectedAct);
			fireEntitySelected(this, EntityType.ACTIVITY, selectedAct, null, null);
			volTableModel.fireTableDataChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		if(e.getSource() == activityCB && !bIgnoreCBEvents)
		{
			sortActivityCategory = (String) activityCB.getSelectedItem();
			createTableList();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC Activity Table");
		}
		else if(e.getSource() == btnResetFilters)
		{
			resetFilters();
		}
	}	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.ACTIVITY);
	}
	
	class VolunteerTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "# Activities", "# Sign-Ins", "Last Changed"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return volTableList == null ? 0 : volTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCVolunteer v = volTableList.get(row);
        	
        	if(col == FIRST_NAME_COL)  
        		return v.getfName();
        	else if(col == LAST_NAME_COL)
        		return v.getlName();
        	else if (col == GROUP_COL)
        		return v.getGroup();
        	else if (col == NUM_ACT_COL)
        		return v.getActivityList().size();
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
	
	class ActivityTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the activity table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Activity Name", "Start Date", "StartTime", "End Date", "End Time"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return actTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	VolunteerActivity act = actTableList.get(row);
        	
        	if(col == ACT_NAME_COL)  
        		return act.getName();
        	else if(col == ACT_START_DATE_COL)
        		return act.getStartDate();
        	else if(col == ACT_START_TIME_COL)
        		return act.getStartTime();
        	else if (col == ACT_END_DATE_COL)
        		return act.getEndDate();
        	else if (col == ACT_END_TIME_COL)
        		return act.getEndTime();
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
        	//Name, Status, Access and Permission are editable
        	return false;
        }
	}	
}