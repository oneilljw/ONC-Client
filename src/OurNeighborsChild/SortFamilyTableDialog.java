package OurNeighborsChild;

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

public abstract class SortFamilyTableDialog extends ChangeDialog
{
	/**
	 * Extends the blueprint for dialogs that display ONCFamily objects in the table. Provides
	 * a common list of families and region combobox model. Common methods are provided to sort
	 * the table list and fire an event notification when a family is selected by the user
	 */
	private static final long serialVersionUID = 1L;
	protected static final int FAMILY_STATUS_PACKAGED = 5;
	
	protected ChildDB cDB;
	protected DeliveryDB deliveryDB;
	protected ONCRegions regions;
	
	protected ArrayList<ONCFamily> stAL = new ArrayList<ONCFamily>();
//	protected ArrayList<ONCFamily> tableRowSelectedObjectList;
	protected DefaultComboBoxModel regionCBM;
	
	protected String[] columns;

	public SortFamilyTableDialog(JFrame pf, String[] colToolTips, String[] cols, int[] colWidths, int[] center_cols)
	{
		super(pf, colToolTips, cols, colWidths, center_cols);
		columns = cols;
		
		cDB = ChildDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		regions = ONCRegions.getInstance();
		
		stAL = new ArrayList<ONCFamily>();
//		tableRowSelectedObjectList = new ArrayList<ONCFamily>();
		regionCBM = new DefaultComboBoxModel();
		
		sortTable.setModel(new FamilyTableModel());
	}
	
	@Override
	int sortTableList(int col)
	{
		archiveTableSelections(stAL);
		
		if(fDB.sortDB(stAL, columns[col]))
		{
			displaySortTable(stAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;
	}
/*	
	void archiveTableSelections(ArrayList<? extends ONCObject> stAL)
	{
		tableRowSelectedObjectList.clear();
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			ONCFamily f = (ONCFamily) stAL.get(row_sel[i]);
			tableRowSelectedObjectList.add(f);
		}
	}
*/
	@Override
	public void valueChanged(ListSelectionEvent e) 
	{
		if (!e.getValueIsAdjusting() && e.getSource() == sortTable.getSelectionModel() &&
				sortTable.getSelectedRow() > -1 && !bChangingTable)
		{
			ONCFamily fam = (ONCFamily) stAL.get(sortTable.getSelectedRow());
			
			fireEntitySelected(this, "FAMILY_SELECTED", fam, null);
			this.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change family		
	}
	
	class FamilyTableModel extends AbstractTableModel
	{
		/**
		* Implements the table model for the Wish Catalog Dialog
		*/
		private static final long serialVersionUID = 1L;
		
		String[] ftCols = {"ONC", "Batch #", "DNS", "Fam Status", "Del Status", "First", "Last",
					"House", "Street", "Unit", "Zip", "Reg", "Changed By", "SL"};
	 
	    public int getColumnCount() { return ftCols.length; }
	 
	    public int getRowCount() { return stAL.size(); }
	 
	    public String getColumnName(int col) { return ftCols[col]; }
	 
	    public Object getValueAt(int row, int col)
	    {
	    	if(col == 0)  
	        	return stAL.get(row).getONCNum();
	        else if(col == 1)
	        	return stAL.get(row).getBatchNum();
	        else if(col == 2)
	        	return stAL.get(row).getDNSCode();
	        else if(col == 3)
	        	return famstatus[stAL.get(row).getFamilyStatus()+1];
	        else if(col == 4)
	        	return delstatus[stAL.get(row).getDeliveryStatus()+1];
	        else if(col == 5)
	        	return stAL.get(row).getHOHFirstName();
	        else if(col == 6)
	        	return stAL.get(row).getHOHLastName();
	        else if(col == 7)
	        	return stAL.get(row).getHouseNum();
	        else if(col == 8)
	        	return stAL.get(row).getStreet();
	        else if(col == 9)
	        	return stAL.get(row).getUnitNum();
	        else if(col == 10)
	        	return stAL.get(row).getZipCode();
	        else if(col == 11)
	        	return stAL.get(row).getRegion();
	        else if(col == 12)
	        	return stAL.get(row).getChangedBy();
	        else if(col == 13)
	        	return stAL.get(row).getStoplightPos(); 
	        else
	        	return "Error";
	    }
	        
	    //JTable uses this method to determine the default renderer/editor for each cell.
	    @Override
	    public Class<?> getColumnClass(int col)
	    {
	        if(col == 3 || col == 4 || col == 11 || col == 13)
	        	return Integer.class;
	        else
	        	return String.class;
	    }
	 
	    public boolean isCellEditable(int row, int col)
	    {
	        //Only the check boxes can be edited and then only if there is not
	    	//a wish already selected from the list associated with that column
	        return false;
	    }
	}
}
