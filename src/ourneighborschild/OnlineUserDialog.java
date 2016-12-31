package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

public class OnlineUserDialog extends JDialog
{
	/**
	 * This class implements a dialog which allows the user to see who is online
	 */
	private static final long serialVersionUID = 1L;
	private static final int FIRST_NAME_COL= 0;
	private static final int LAST_NAME_COL= 1;
	private static final int VER_COL= 2;
	private static final int YEAR_COL= 3;
	private static final int TIME_COL= 4;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private UserDB userDB;
	private List<ONCUser> tableList;
	
	public OnlineUserDialog(JFrame pf)
	{
		super(pf, true);
		this.setTitle("ONC Elves And/Or Agents Online");
		this.setAlwaysOnTop(true);
		
		userDB = UserDB.getInstance();
		
		//create the list of adults shown in the table
		tableList = new ArrayList<ONCUser>();
		tableList = userDB.getOnlineUsers();
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"User First Name", "User Last Name", "Online using the App or Website",
								"Data year they are using", "Time they logged in"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {88, 88, 56, 56, 112};
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
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 150));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
//      JPanel cntlPanel = new JPanel();
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
//      getContentPane().add(cntlPanel);
        
        pack();
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Online UserDialog
		 */
		private SimpleDateFormat sdf;
		
		public DialogTableModel()
		{
			sdf = new SimpleDateFormat("M/d h:mm a");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"First Name", "Last Name", "Using", "DB Year", "Login Date/Time"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return tableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCUser user = tableList.get(row);
        	if(col == FIRST_NAME_COL)  
        		return user.getFirstname();
        	else if(col == LAST_NAME_COL)
        		return user.getLastname();
        	else if (col == VER_COL)
        		return user.getClientID() == -1 ? "Website" : "App";
        	else if (col == YEAR_COL)
        		return user.getClientID() == -1 ? "2016" : 
        				user.getClientYear() == -1 ? "None" : Integer.toString(user.getClientYear());
        	else if (col == TIME_COL)
        	{
        		//convert UTC to local time zone
        		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        		calendar.setTimeInMillis(user.getLastLogin());
        		return sdf.format(calendar.getTime());
        	}
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
        	return false;
        }
    }
}
