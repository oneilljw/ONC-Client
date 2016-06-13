package ourneighborschild;

import java.util.EnumSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.google.gson.Gson;

public class InventoryDialog extends BarcodeTableDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int BARCODE_COL= 2;
	private static final int COUNT_COL = 1;
	private static final int NAME_COL = 0;
//	private static final int ALIAS_COL = 3;
//	private static final int DESC_COL = 4;
//	private static final int AVG_PRICE_COL = 4;
//	private static final int RATE_UP_COL = 5;
//	private static final int RATE_DOWN_COL = 6;
	
	private InventoryDB inventoryDB;

	public InventoryDialog(JFrame parentFrame) 
	{
		super(parentFrame);
		this.setTitle("ONC Inventory");
		
		inventoryDB = InventoryDB.getInstance();
		if(inventoryDB != null)
			inventoryDB.addDatabaseListener(this);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().contains("_INVENTORY_ITEM"))
		{
			dlgTableModel.fireTableDataChanged();
		}
	}
	
	@Override
	String[] getColumnToolTips()
	{
		return new String[] {"Item Name & Description", "Quanity of item in inventory", "Barcode"};
	}
	
	@Override
	int[] getColumnWidths() { return new int[] {440, 32, 96}; }
	
	@Override
	int[] getLeftColumns() { return new int[] {}; }
	
	@Override
	int[] getCenteredColumns() { return new int[] {COUNT_COL}; }

	@Override
	AbstractTableModel getDialogTableModel() { return new DialogTableModel(); }

	@Override
	void onBarcodeTFEvent() 
	{
		//add the bar coded item to inventory
		String barcode = barcodeTF.getText();
		InventoryRequest invAddReq = new InventoryRequest(barcode, 1);
		
		Gson gson = new Gson();
		String response = inventoryDB.add(this, invAddReq);
		
		if(response == null)
		{
			lblInfo.setText("Server Did Not Respond");
		}
		else if(response.startsWith("UPC_LOOKUP_FAILED"))
		{
			UPCFailure failure = gson.fromJson(response.substring(17), UPCFailure.class);
			lblInfo.setText(failure.getReason());
		}
		else if(response.startsWith("ADD_INVENTORY_FALIED"))
		{
			UPCFailure failure = gson.fromJson(response.substring(20), UPCFailure.class);
			lblInfo.setText(failure.getReason());
		}
		else if(response.startsWith("INCREMENTED_INVENTORY_ITEM"))
		{
			lblInfo.setText("Increased Item Count");
			dlgTableModel.fireTableDataChanged();
		}
		else if(response.startsWith("ADDED_INVENTORY_ITEM"))
		{
			lblInfo.setText("Added New Item to Inventory");
			dlgTableModel.fireTableDataChanged();
		}
	}

	@Override
	String getPrintTitle() { return "Current Inventory"; }
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.INVENTORY_ITEM);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Inventory Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Item", "Qty", "Barcode"};
 
        public int getColumnCount() { return columnNames.length; }
 
//      public int getRowCount() { return stAL != null ? stAL.size() : 0; }
        
        public int getRowCount() { return inventoryDB != null ? inventoryDB.size() : 0; }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	InventoryItem ii = inventoryDB.getItem(dlgTable.convertRowIndexToView(row));
        	if(col == BARCODE_COL)
        		return ii.getNumber();
        	else if(col == COUNT_COL)
        		return ii.getCount();
        	else if (col == NAME_COL)
        		return ii.getItemName().isEmpty() ?  ii.getDescription() : ii.getItemName();
//        	else if (col == ALIAS_COL)
//        		return ii.getAlias();
//        	else if (col == AVG_PRICE_COL)
//        		return ii.getAvgPrice();
//        	else if (col == RATE_UP_COL)
//        		return ii.getRateUp();
//        	else if (col == RATE_DOWN_COL)
//        		return ii.getRateDown();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == COUNT_COL)
        		return Integer.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return col == NAME_COL ||
        			(col == COUNT_COL &&  
        			UserDB.getInstance().getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0);
        }
        
        public void setValueAt(Object value, int row, int col)
        { 
        	InventoryItem selItem = inventoryDB.getItem(dlgTable.convertRowIndexToView(row));
        	InventoryItem reqUpdateItem = null;
        	
        	//determine if the user made a change to a user object
        	if(col == NAME_COL && !selItem.getItemName().equals((String)value))
        	{
        		reqUpdateItem = new InventoryItem(selItem);	//make a copy
        		reqUpdateItem.setItemName((String) value);
        	}
        	else if(col == COUNT_COL && selItem.getCount() != ((Integer) value))
        	{
        		reqUpdateItem = new InventoryItem(selItem);	//make a copy
        		reqUpdateItem.setCount((Integer) value);
        	}
        	
        	//if the user made a change in the table, attempt to update the user object in
        	//the local user data base
        	if(reqUpdateItem != null)
        	{
        		String response = inventoryDB.update(this, reqUpdateItem);        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_INVENTORY_ITEM")))
        		{
        			//request failed
        			String err_mssg = "ONC Server denied update item request, try again later";
        			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Update Inventory Item Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
        		}
        		else
        		{
        			lblInfo.setText("Updated Inventory Item");
        			dlgTableModel.fireTableDataChanged();
        		}
        	}
        }
    }
}
