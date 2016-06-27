package ourneighborschild;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;

public class InventoryItemTransferable implements Transferable
{
	private DataFlavor invItemDataFlavor;
	private InventoryDB inventoryDB;
	private ONCTable table;
	
	public InventoryItemTransferable(JComponent c)
	{
		invItemDataFlavor = new DataFlavor(InventoryItem.class, "Inventory Item");
		inventoryDB = InventoryDB.getInstance();
		
		if(c instanceof ONCTable)
			table = (ONCTable) c;
		else
			table = null;
	}
	
	@Override
	public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException 
	{
		if(!df.equals(invItemDataFlavor))
			throw new UnsupportedFlavorException(df);
		else
		{
			int modelrow = table.convertRowIndexToModel(table.getSelectedRow());
			InventoryItem selItem = inventoryDB.getItem(modelrow);
			
			return selItem;
		}	
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { invItemDataFlavor }; }
		
	@Override
	public boolean isDataFlavorSupported(DataFlavor df) { return df.equals(invItemDataFlavor); }
}
