package ourneighborschild;

import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.event.ListSelectionEvent;

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
	protected FamilyHistoryDB familyHistoryDB;
	protected ONCRegions regions;
	
	protected ArrayList<ONCFamily> stAL = new ArrayList<ONCFamily>();
	protected ArrayList<ONCFamily> tableRowSelectedObjectList;
	protected DefaultComboBoxModel regionCBM;
	
//	protected SortTableModel fdTableModel;
	
	protected String[] columns;

	public SortFamilyTableDialog(JFrame pf)
	{
		super(pf);
		columns = getColumnNames();
		
		cDB = ChildDB.getInstance();
		familyHistoryDB = FamilyHistoryDB.getInstance();
		regions = ONCRegions.getInstance();
		
		stAL = new ArrayList<ONCFamily>();
		tableRowSelectedObjectList = new ArrayList<ONCFamily>();
		regionCBM = new DefaultComboBoxModel();
		
//		fdTableModel = new SortTableModel(stAL, cols);
//		sortTable.setModel(fdTableModel);
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
			
			fireEntitySelected(this, EntityType.FAMILY, fam, null);
			this.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change family		
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}
}
