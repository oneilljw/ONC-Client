package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

public class PhoneInfoDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private static final int PHONE_COL= 0;
	private static final int NUMBER_COL = 1;
	private static final int TYPE_COL = 2;
	private static final int CARRIER_COL = 3;
	private static final int VALID_COL = 4;
	
	private FamilyPhoneInfo fpi;
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	
	PhoneInfoDialog(JFrame parentFrame, FamilyPhoneInfo fpi)
	{
		super(parentFrame, true);
		this.setTitle("Family Phone Information");
		this.fpi = fpi;
		
		String[] colTT = {"Phone","Phone Number","Type of phone","Network phone is on","Is a vaild working phone number",};
		
		//create the table
		dlgTableModel = new DialogTableModel();
		dlgTable = new ONCTable(dlgTableModel, colTT, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {80, 104, 72, 144, 36};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
  
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
		        
		//center justify columns
//		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//		dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//		dlgTable.getColumnModel().getColumn(VALID_COL).setCellRenderer(dtcr);
		
		//Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 192));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        
        pack();
        this.setSize(tablewidth, 100);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Phone Info Dialog
		 */
		
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Phone", "Phone Number","Type", "Carrier",  "Valid"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return fpi == null ? 0 : fpi.getNumberOfValidPhoneNumbers(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	Object value;
        	
        	if(col == PHONE_COL && row == 0)
        		value = "Primary";
        	else if(col == PHONE_COL && row == 1)
        		value = "Alternate";
        	else if(col == PHONE_COL && row == 2)
        		value = "2nd Alternate";
        	else if(col == NUMBER_COL)  
        		value = fpi.getPhoneInfo(row).getPhoneNumber();
        	else if (col == TYPE_COL)
        		value = fpi.getPhoneInfo(row).getType();
        	else if (col == CARRIER_COL)
        		value = fpi.getPhoneInfo(row).getCarrier();
        	else if (col == VALID_COL)
        		value = fpi.isPhoneValid(row);
        	else
        		value = "Error";

        	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == VALID_COL)
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
