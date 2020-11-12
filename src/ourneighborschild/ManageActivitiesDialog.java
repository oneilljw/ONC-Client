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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import au.com.bytecode.opencsv.CSVWriter;

public class ManageActivitiesDialog extends ONCEntityTableDialog implements ActionListener, ListSelectionListener,
																		DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL = 1;
	private static final int GROUP_COL = 2;
	private static final int QTY_COL = 3;
	private static final int COMMENT_COL = 4;

	private static final int ACT_NAME_COL= 0;
	private static final int ACT_VOL_COUNT_COL = 1;
	private static final int ACT_DELIVERY_COL = 2;
	private static final int ACT_DEFAULT_COL = 3;
	private static final int ACT_START_DATE_COL = 4;
	private static final int ACT_END_DATE_COL = 5;
	
	private static final int NUM_VOL_TABLE_ROWS = 12;
	private static final int NUM_ACT_TABLE_ROWS = 15;

	protected JPanel sortCriteriaPanel;
	private JComboBox<String> activityCB;
	private JComboBox<String> volExportCB;
	private DefaultComboBoxModel<String> activityCBM;
	private boolean bIgnoreCBEvents;
	private String sortActivityCategory;

	private ONCTable volTable, actTable;
	private AbstractTableModel volTableModel, actTableModel;
	private JButton btnResetFilters, btnPrint;
	private JLabel lblActCount, lblVolCount;

	private VolunteerDB volDB;
	private ActivityDB activityDB;
	private VolunteerActivityDB volActDB;

	private List<Activity> actTableList;
	private List<ONCVolunteer> volTableList;
	private Activity selectedAct;

	public ManageActivitiesDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Activity Managment");
		
		//Save the reference to the one volunteer data base object in the app. It is created in the 
		//top level object and passed to all objects that require the data base, including
		//this dialog
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		volActDB = VolunteerActivityDB.getInstance();
		if(volActDB != null)
			volActDB.addDatabaseListener(this);
		
		//set up the table lists
		actTableList = new ArrayList<Activity>();
		volTableList = new ArrayList<ONCVolunteer>();
		
		bIgnoreCBEvents = false;
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
	
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
		
		//create search filters
		activityCBM = new DefaultComboBoxModel<String>();
	    activityCBM.addElement("Any");
		activityCB = new JComboBox<String>(activityCBM);
		activityCB.setBorder(BorderFactory.createTitledBorder("Activitiy"));
		activityCB.setPreferredSize(new Dimension(200,56));
		activityCB.addActionListener(this);
		sortCriteriaPanel.add(activityCB);
		sortActivityCategory = "Any";

        //create the activity table
      	actTableModel = new ActivityTableModel();
       	String[] actToolTips = {"Name", "# of Volunteers Signed Up", "Do Volunteers for Activity Deliver Gifts?",
       			"Is Activity the Default Delivery Activity?", "Start Date", "End Date", };
      	actTable = new ONCTable(actTableModel, actToolTips, new Color(240,248,255));

     	actTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      	actTable.getSelectionModel().addListSelectionListener(this);
      		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
		DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm");

		    public Component getTableCellRendererComponent(JTable table,Object value,
		            boolean isSelected, boolean hasFocus, int row, int column)
		    {
		        if( value instanceof Date)
		            value = f.format(value);
		        
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};
		actTable.getColumnModel().getColumn(ACT_START_DATE_COL).setCellRenderer(tableCellRenderer);
		actTable.getColumnModel().getColumn(ACT_END_DATE_COL).setCellRenderer(tableCellRenderer);
		
      	//Set table column widths
      	int tablewidth = 0;
      	int[] act_colWidths = {360, 56, 56, 68, 160, 160};
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
		String[] colToolTips = {"First Name", "Last Name", "Group",
								"Quantity for Activity", "Comment for Activiity"};
		
		volTable = new ONCTable(volTableModel, colToolTips, new Color(240,248,255));
		volTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		volTable.getSelectionModel().addListSelectionListener(this);

		//Set table column widths
		tablewidth = 0;
		int[] colWidths = {96, 96, 176, 32, 288};
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
        volTable.getColumnModel().getColumn(QTY_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane volScrollPane = new JScrollPane(volTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
													JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        volScrollPane.setPreferredSize(new Dimension(tablewidth, volTable.getRowHeight()*NUM_VOL_TABLE_ROWS));
        volScrollPane.setBorder(BorderFactory.createTitledBorder("Volunteers for selected activity"));
        
        //create the control panel
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BoxLayout(cntlPanel, BoxLayout.X_AXIS));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        lblVolCount = new JLabel("Volunteers for selected activity:");
        infoPanel.add(lblVolCount);
        
        String[] exportChoices = {"Export", "Volunteer Contact Group"};
        volExportCB = new JComboBox<String>(exportChoices);
        volExportCB.setToolTipText("Export Volunteer Info to .csv file");
        volExportCB.setEnabled(false);
        volExportCB.addActionListener(this);
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the activity table list");
        btnPrint.addActionListener(this);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.setToolTipText("Restore Filters to Defalut State");
        btnResetFilters.addActionListener(this);
        
        btnPanel.add(volExportCB);
        btnPanel.add(btnPrint);
        btnPanel.add(btnResetFilters);
        
        cntlPanel.add(infoPanel);
        cntlPanel.add(btnPanel);
        
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
		volTableList.clear();
		volExportCB.setEnabled(false);
		
		lblVolCount.setText("Volunteers for selected activity: " + Integer.toString(volTableList.size()));
		
		for(Activity va : (List<Activity>) activityDB.getList())
			if(doesActivityMatch(va))
				actTableList.add(va);
		
		lblActCount.setText(String.format("Activities Meeting Criteria: %d", actTableList.size()));
		
		actTableModel.fireTableDataChanged();
		volTableModel.fireTableDataChanged();
	}
	
	void updateVolTableList()
	{	
		if(selectedAct != null)
		{
			volTableList = volDB.getVolunteersForActivity(selectedAct);
			lblVolCount.setText("Volunteers for selected activity: " + Integer.toString(volTableList.size()));
			volTableModel.fireTableDataChanged();
		}
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
	
	boolean doesActivityMatch(Activity va)
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
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	void onExportVolunteerContactGroup()
	{
		//get group name input from user. If null exit
		String groupName = (String) JOptionPane.showInputDialog(this, "Please enter Contact Group Name:",  
				"Contact Group Name", JOptionPane.QUESTION_MESSAGE, gvs.getImageIcon(0),
				null, "");
		
		if(groupName != null)
		{
			ONCFileChooser oncfc = new ONCFileChooser(this);
			File oncwritefile = oncfc.getFile("Select file for export of Volunteer Gmail Contact Group" ,
       						new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE, groupName);
			if(oncwritefile!= null)
			{
				//If user types a new filename without extension.csv, add it
				String filePath = oncwritefile.getPath();
				if(!filePath.toLowerCase().endsWith(".csv")) 
					oncwritefile = new File(filePath + ".csv");
	    	
				try 
				{
					CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
					writer.writeNext(ONCGmailContactEntity.getGmailContactCSVHeader());
	    	    
					int[] row_sel = volTable.getSelectedRows();
					for(int i=0; i<volTable.getSelectedRowCount(); i++)
					{
						int index = volTable.convertRowIndexToModel(row_sel[i]);
						writer.writeNext(volTableList.get(index).getGoogleContactExportRow(groupName));
					}
	    	   
					writer.close();
	    	    
					JOptionPane.showMessageDialog(this, 
							volTable.getSelectedRowCount() + " volunteer contacts sucessfully exported to " + oncwritefile.getName(), 
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
	}
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_ACTIVITY") ||
				dbe.getType().equals("UPDATED_ACTIVITY") || dbe.getType().equals("DELETED_ACTIVITY")))
		{
			//update the table
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("CATEGOR"))
		{
			updateActivityList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			//get the initial data and display
			this.setTitle(String.format("Our Neighbor's Child - %d Activity Management", gvs.getCurrentSeason()));
			createTableList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_VOLUNTEER_ACTIVITY"))
		{
			updateVolTableList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == actTable.getSelectionModel())
		{
			int modelRow = actTable.getSelectedRow() == -1 ? -1 : 
						actTable.convertRowIndexToModel(actTable.getSelectedRow());
		
			if(modelRow > -1)
			{
				selectedAct = actTableList.get(modelRow);
				volTableList = volDB.getVolunteersForActivity(selectedAct);
				lblVolCount.setText("Volunteers for selected activity: " + Integer.toString(volTableList.size()));
				fireEntitySelected(this, EntityType.ACTIVITY, selectedAct, null, null);
				volTableModel.fireTableDataChanged();
			}
			else
			{
				//clear the table, activity selected was cleared
				selectedAct = null;
				volTableList.clear();
				lblVolCount.setText("Volunteers for selected activity: " + Integer.toString(volTableList.size()));
				volTableModel.fireTableDataChanged();
			}
		}
		else if(!lse.getValueIsAdjusting() && lse.getSource() == volTable.getSelectionModel())
		{
			int modelRow = volTable.getSelectedRow() == -1 ? -1 : 
				volTable.convertRowIndexToModel(volTable.getSelectedRow());

			volExportCB.setEnabled(modelRow > -1);
			
			ONCVolunteer selVolunteer = modelRow > -1 ? volTableList.get(modelRow) : null;
			if(selVolunteer != null)
				fireEntitySelected(this, EntityType.VOLUNTEER, selVolunteer, null, null);
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
		else if(e.getSource() == volExportCB)
		{
			if(volExportCB.getSelectedItem().toString().equals("Volunteer Contact Group") &&
					volTable.getSelectedRowCount() > 0)
			{ 
				onExportVolunteerContactGroup();
			}
			
			volExportCB.setSelectedIndex(0);
		}
	}	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.ACTIVITY, EntityType.VOLUNTEER);
	}
	
	class VolunteerTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Group", "Qty", "Comment for Activity"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return volTableList == null ? 0 : volTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		//get the volunteer for the row
        		ONCVolunteer v = volTableList.get(row);
        	
        		//get the detailed activity for the volunteer. Get the generic activity first
        		//then get the detailed activity
        		int actModelRow = actTable.getSelectedRow() == -1 ? -1 : 
				actTable.convertRowIndexToModel(actTable.getSelectedRow());

        		VolAct va = null;
        		if(actModelRow > -1)
        		{
        			Activity genericAct = actTableList.get(actModelRow);
        			if(genericAct != null)
        				va = volActDB.getVolunteerActivity(v.getID(), genericAct.getID());
        		}
        	
        		if(col == FIRST_NAME_COL)  
        			return v.getFirstName();
        		else if(col == LAST_NAME_COL)
        			return v.getLastName();
        		else if (col == GROUP_COL)
        			return v.getOrganization();
        		else if (col == QTY_COL)
        			return va != null ? va.getQty() : -1;
        		else if (col == COMMENT_COL)
        			return va != null ? va.getComment() : "";
        		else
        			return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == QTY_COL)
        			return Integer.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
	}
	
	class ActivityTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the activity table
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Activity Name", "# Vols", "Del Act? ", "Default Del?",
										"Start Date", "End Date"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return actTableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		Activity act = actTableList.get(row);
        	
        		if(col == ACT_NAME_COL)  
        			return act.getName();
        		else if (col == ACT_VOL_COUNT_COL)
        			return volActDB.getVolunteerCountForActivity(act.getID());
        		else if (col == ACT_DELIVERY_COL)
        			return act.isDeliveryActivity();
        		else if (col == ACT_DEFAULT_COL)
        			return act.isDefaultDeliveryActivity();
        		else if(col == ACT_START_DATE_COL)
        			return convertLongToDate(act.getStartDate());
        		else if (col == ACT_END_DATE_COL)
        			return  convertLongToDate(act.getEndDate());
        		else
        			return "Error";
        }
        
        private Date convertLongToDate(long date)
        {
        		Calendar cal = Calendar.getInstance();
        		cal.setTimeInMillis(date);
        		return cal.getTime();
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == ACT_START_DATE_COL || column == ACT_END_DATE_COL)
        			return Date.class;
        		else if(column == ACT_VOL_COUNT_COL)
        			return Integer.class;
        		else if(column == ACT_DELIVERY_COL || column == ACT_DEFAULT_COL)
        			return Boolean.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
	}	
}
