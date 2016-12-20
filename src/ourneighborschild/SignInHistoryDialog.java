package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
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
	 * Displays a table of warehouse sign-in history for a volunteer 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_COL= 0;
	private static final int GROUP_COL= 1;
	private static final int DATE_COL = 2;
	private static final int COMMENT_COL = 3;
	
	private List<ONCWarehouseVolunteer> signInList;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	
	private VolunteerDB volunteerDB;
	private ONCVolunteer currVol;

	public SignInHistoryDialog(JDialog owner, boolean modality)
	{
		super(owner, modality);

		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		signInList = new ArrayList<ONCWarehouseVolunteer>();
		
		//create the sign-in history table
		dlgTableModel = new DialogTableModel();
		String[] colTT = {"Group volunteer listed at sign-in", "Sign-In #", "Sign-In time", "Sign-In comment"};
		dlgTable = new ONCTable(dlgTableModel, colTT, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//set table column widths
		int tablewidth = 0;
		int[] colWidths = {28, 120, 120, 180};
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
		        
		//create the scroll pane and add the table to it.
		JScrollPane dsScrollPane = new JScrollPane(dlgTable);
		dsScrollPane.setPreferredSize(new Dimension(tablewidth, 120));
		dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(dsScrollPane);
		        
		pack();
	}

	void display(ONCObject obj) 
	{
		this.currVol = (ONCVolunteer) obj;
		this.setTitle(String.format("%s %s's Sign-In History", currVol.getfName(), currVol.getlName()));
		
		signInList = volunteerDB.getWarehouseHistory(currVol.getID());
		dlgTableModel.fireTableDataChanged();
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && this.isVisible() && dbe.getType().equals("UPDATED_DRIVER"))
		{
			ONCVolunteer updatedVolunteer = (ONCVolunteer) dbe.getObject1();
			
			//If updated delivery belongs to family delivery history being displayed,
			//re-display it
			if(currVol != null && currVol.getID() == updatedVolunteer.getID())
				display(currVol);
		}
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && tse.getType() == EntityType.VOLUNTEER)
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
		return EnumSet.of(EntityType.VOLUNTEER);
	}

	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"#", "Group", "Sign In Date & Time", "Comment"};
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
        	
        	if(col == GROUP_COL)
        		value = vol.getGroup();
        	else if(col == NUM_COL)
        		value = row + 1;
        	else if(col == DATE_COL)  
        		value = sdf.format(vol.getTimestamp());
        	else if(col == COMMENT_COL)
        		value = vol.getComment();
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
