package OurNeighborsChild;

import java.util.ArrayList;
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
	protected DeliveryDB deliveryDB;
	protected ONCRegions regions;
	
	protected ArrayList<ONCFamily> stAL = new ArrayList<ONCFamily>();
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
		regionCBM = new DefaultComboBoxModel();
	}
	
	@Override
	int sortTableList(int col)
	{
		archiveTableSelections(stAL);
		
		if(fDB.sortDB(stAL, columns[col]))
		{
			displaySortTable(stAL, false);
			return col;
		}
		else
			return -1;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) 
	{
		if (!e.getValueIsAdjusting() && e.getSource() == sortTable.getSelectionModel() &&
				!bChangingTable)
		{
			ONCFamily fam = (ONCFamily) stAL.get(sortTable.getSelectedRow());
			
			fireEntitySelected(this, "FAMILY_SELECTED", fam, null);
			this.requestFocus();
		}
		
		checkApplyChangesEnabled();	//Check to see if user postured to change family		
	}
}
