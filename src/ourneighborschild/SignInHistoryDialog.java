package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class SignInHistoryDialog extends JDialog implements  EntitySelectionListener, DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NAME_COL= 0;
	private static final int NUM_COL= 1;
	private static final int DATE_COL = 2;
	
	private List<ONCWarehouseVolunteer> signInList;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	
	private VolunteerDB volunteerDB;
	private ONCVolunteer currVol;

	public SignInHistoryDialog(JFrame pf)
	{
		super(pf);

		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		signInList = new ArrayList<ONCWarehouseVolunteer>();
		
		//create the history table
		dlgTableModel = new DialogTableModel();
		String[] colTT = {"Name of Volunteer that signed in", "Sign In #", "Sign-In Time"};
		dlgTable = new ONCTable(dlgTableModel, colTT, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {120, 32, 120};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
				
//		dlgTable.setAutoCreateRowSorter(true);	//add a sorter
		        
		JTableHeader anHeader = dlgTable.getTableHeader();
		anHeader.setForeground( Color.black);
		anHeader.setBackground( new Color(161,202,241));
		        
		//center justify columns
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
		dlgTable.getColumnModel().getColumn(NUM_COL).setCellRenderer(dtcr);
		        
		//Create the scroll pane and add the table to it.
		JScrollPane dsScrollPane = new JScrollPane(dlgTable);
		dsScrollPane.setPreferredSize(new Dimension(tablewidth, 96));
		dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(dsScrollPane);
		        
		pack();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}

	void display(ONCObject obj) 
	{
		this.currVol = (ONCVolunteer) obj;
		setDialogTitle();
		
		signInList = volunteerDB.getWarehouseHistory(currVol.getID());
		dlgTableModel.fireTableDataChanged();
	}
	
	void setDialogTitle()
	{
		this.setTitle(String.format("%s %s's Sign-In History", currVol.getfName(), currVol.getlName()));
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && tse.getType() == EntityType.DRIVER)
		{
			ONCVolunteer vol = (ONCVolunteer) tse.getObject1();
			if(vol != null)
			{
				display(vol);	
			}	
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.DRIVER);
	}

	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Volunteer Name", " # ", "Sign In Date & Time"};
		private SimpleDateFormat sdf;
		
		public DialogTableModel()
		{
			sdf = new SimpleDateFormat("M/dd/yy H:mm");
		}

        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return signInList == null ? 0 : signInList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	Object value;
        	
        	ONCWarehouseVolunteer vol = signInList.get(row);
        	
        	if(col == NAME_COL)
        		value = currVol.getfName() + " " + currVol.getlName();
        	else if(col == NUM_COL)
        		value = row + 1;
        	else if(col == DATE_COL)  
        		value = sdf.format(vol.getTimestamp());
        	else
        		value = "Error";
        	
         	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == NUM_COL)
        		return Integer.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
    }
}
