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
	protected MealDB mealDB;
	protected RegionDB regions;
	protected DNSCodeDB dnsCodeDB;
	
	protected FamilyAndNoteListSorter tableSorter;
	
	protected ArrayList<ONCFamilyAndNote> stAL;
	protected DefaultComboBoxModel<String> regionCBM;

	protected String[] columns;

	public SortFamilyTableDialog(JFrame pf)
	{
		super(pf);
		columns = getColumnNames();
		
		cDB = ChildDB.getInstance();
		mealDB = MealDB.getInstance();
		regions = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
		
		stAL = new ArrayList<ONCFamilyAndNote>();
		tableSorter = new FamilyAndNoteListSorter();
		
		regionCBM = new DefaultComboBoxModel<String>();
	}
	
	@Override
	int sortTableList(int col)
	{
		archiveTableSelections(stAL);
		
		if(tableSorter.sortFamilyAndNoteDB(stAL, columns[col]))
		{
			displaySortTable(stAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) 
	{
		if (!e.getValueIsAdjusting() && e.getSource() == sortTable.getSelectionModel() &&
				sortTable.getSelectedRow() > -1 && !bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getFamily();
			FamilyHistory famHistory = stAL.get(sortTable.getSelectedRow()).getFamilyHistory();
			
			fireEntitySelected(this, EntityType.FAMILY, fam, famHistory, null);
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
