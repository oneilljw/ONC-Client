package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.google.gson.Gson;

public class InventoryDialog extends BarcodeTableDialog implements ActionListener, ListSelectionListener
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
	private static final int DEFAULT_TABLE_ROW_COUNT = 15;
	
	private InventoryDB inventoryDB;
	private JRadioButton rbEdit, rbAdd, rbRemove;

	public InventoryDialog(JFrame parentFrame) 
	{
		super(parentFrame);
		this.setTitle("ONC Inventory Manager");
		
		inventoryDB = InventoryDB.getInstance();
		if(inventoryDB != null)
			inventoryDB.addDatabaseListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		rbEdit = new JRadioButton("Edit Item");
		rbAdd = new JRadioButton("Add Qtu");
		rbRemove = new JRadioButton("Remove Gty");
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbEdit);
		modeGroup.add(rbAdd);
		modeGroup.add(rbRemove);
		rbEdit.setSelected(true);
		
		buttonPanel.add(rbEdit);
		buttonPanel.add(rbAdd);
		buttonPanel.add(rbRemove);
		
		topPanel.add(buttonPanel, BorderLayout.EAST);
		
		dlgTable.getSelectionModel().addListSelectionListener(this);
		dlgTable.setDragEnabled(true);
		dlgTable.setTransferHandler(new InventoryTransferhandler());
		
		btnAction.setText("Add Item");
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
		return new String[] {"Item Name & Description", "Quanity of item in inventory", "Item Barcode"};
	}
	
	@Override
	int[] getColumnWidths() { return new int[] {440, 32, 96}; }
	
	@Override
	int[] getLeftColumns() { return new int[] {}; }
	
	@Override
	int[] getCenteredColumns() { return new int[] {COUNT_COL}; }
	
	@Override
	int getDefaultRowCount() { return DEFAULT_TABLE_ROW_COUNT; }

	@Override
	AbstractTableModel getDialogTableModel() { return new DialogTableModel(); }

	@Override
	void onBarcodeTFEvent() 
	{
		//action is based on selected mode: review, add or remove. Add adds an item to 
		//inventory, remove removes an item from inventory. Review does nothing.
		
		if(!rbEdit.isSelected())
		{
			String barcode = barcodeTF.getText();
			InventoryRequest invAddReq = new InventoryRequest(barcode, rbAdd.isSelected() ? 1 : -1);
		
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
				lblInfo.setText(rbAdd.isSelected() ? "Increased Qty" : "Decreased Qty");
				dlgTableModel.fireTableDataChanged();
			}
			else if(response.startsWith("ADDED_INVENTORY_ITEM"))
			{
				lblInfo.setText("Added New Item to Inventory");
				dlgTableModel.fireTableDataChanged();
			}
		}
		else
			lblInfo.setText("Set Mode to Add or Remove Items");
	}

	@Override
	String getPrintTitle() { return "Current Inventory"; }
	
	@Override
	void onActionEvent(ActionEvent e)
	{
		if(e.getSource() == btnAction)
		{
			AddInventoryItemDialog addDlg = new AddInventoryItemDialog(parentFrame, true);
			addDlg.setLocationRelativeTo(this);
			if(addDlg.showDialog())
			{
				InventoryItem addItemReq = addDlg.getAddItemRequest();
				
				//make sure item isn't already in the inventory. Suppress leading zeros
				String searchCode = addItemReq.getNumber().replaceFirst("^0+(?!$)", "");
				if(addItemReq != null && inventoryDB.getItemByBarcode(searchCode) == null)
				{
					//add it to the database
					String result = inventoryDB.add(this, addItemReq);
					if(result.startsWith("ADDED_INVENTORY_ITEM"))
					{
						lblInfo.setText("Added Inventory Item");
						dlgTableModel.fireTableDataChanged();
					}
					else
						lblInfo.setText("Add Inventory Request Rejected by Server");
				}
				else
					lblInfo.setText("Item already in database or invalid");
			}
		}
		else if(e.getSource() == btnDelete)
		{
			//get the selected item name from the model
			InventoryItem reqDelItem = inventoryDB.getItem(dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow()));
			
			//Confirm with the user that the deletion is really intended
			String confirmMssg = String.format("<html>Are you sure you want to delete <br>%s<br>from the data base?</html>", 
											reqDelItem.getItemName());
			
			Object[] options= {"Cancel", "Delete"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								GlobalVariables.getONCLogo(), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(GlobalVariables.getFrame(), "*** Confirm Item Deletion ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete"))
			{
				String response = inventoryDB.delete(this, reqDelItem);
				if(response != null && response.startsWith("DELETED_INVENTORY_ITEM"))
				{
					dlgTableModel.fireTableDataChanged();
					lblInfo.setText("Inventory Item Deleted");
				}
				else
					lblInfo.setText("Delete Item Request Failed");	
			}
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		int selRow = dlgTable.getSelectedRow();
		if(selRow > -1)
		{
			int modelRow = dlgTable.convertRowIndexToModel(selRow);
			int count = (Integer) dlgTableModel.getValueAt(modelRow, COUNT_COL);
			btnDelete.setEnabled(count == 0);
		}
		else
			btnDelete.setEnabled(false);
	}
	
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
  
        public int getRowCount() { return inventoryDB != null ? inventoryDB.size() : 0; }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	InventoryItem ii = inventoryDB.getItem(row);
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
 
        /****
         * A table cell is editable if the mode is set to  either add or remove inventory.
         * Only a user with Administrative or higher privilege may change the count
         */
        public boolean isCellEditable(int row, int col)
        {
        	return rbEdit.isSelected() && (col == NAME_COL ||
        			col == COUNT_COL &&  
        			UserDB.getInstance().getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0);
        }
        
        public void setValueAt(Object value, int row, int col)
        { 
        	InventoryItem selItem = inventoryDB.getItem(row);
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
	
	private class InventoryTransferhandler extends TransferHandler
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int getSourceActions(JComponent c)
		{
			return TransferHandler.COPY;
		}
		
		protected Transferable createTransferable(JComponent c)
		{
			Transferable iit = new InventoryItemTransferable();
			
			InventoryItem ii = null;
			try 
			{
				ii = (InventoryItem) iit.getTransferData(iit.getTransferDataFlavors()[0]);
			} 
			catch (UnsupportedFlavorException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(ii.getNumber());
			return iit;
		}
		
		protected void exportDone(JComponent source, Transferable data, int action)
		{
			System.out.println(String.format("Export Done %d", action));
		}
	}
	
	private class InventoryItemTransferable implements Transferable
	{
		private DataFlavor invItemDataFlavor;
		
		public InventoryItemTransferable()
		{
			invItemDataFlavor = new DataFlavor(InventoryItem.class, "Inventory Item");
		}
		
		@Override
		public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException 
		{
			if(df.equals(invItemDataFlavor))
				throw new UnsupportedFlavorException(df);
			else
			{
				int modelrow = dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
				InventoryItem selItem = inventoryDB.getItem(modelrow);
				return selItem;
			}	
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { invItemDataFlavor }; }
			
		@Override
		public boolean isDataFlavorSupported(DataFlavor df) { return df.equals(invItemDataFlavor); }
	}
	
	private class AddInventoryItemDialog extends InfoDialog
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int BARCODE_FIELD_LENGTH = 12;
		
		private InventoryItem addItemReq;
		

		AddInventoryItemDialog(JFrame pf, boolean bModal)
		{
			super(pf, bModal);
			this.setTitle("Add Inventory Item");
			
			addItemReq = null;
			
			lblONCIcon.setText("<html><font color=blue>Add New Inventory Item<br>Information Below</font></html>");
			btnAction.setText("Add Item");
			
			//Set up the main panel, loop to set up components associated with names
			for(int pn=0; pn < getDialogFieldNames().length; pn++)
			{
				tf[pn] = new JTextField(20);
				tf[pn].addKeyListener(tfkl);
				infopanel[pn].add(tf[pn]);
			}
			
			pack();
		}
		
		InventoryItem getAddItemRequest() {return addItemReq;}

		@Override
		String[] getDialogFieldNames() 
		{
			return new String[] {"Item", "Barcode"};
		}

		@Override
		void display(ONCObject obj) {
			// TODO Auto-generated method stub
			
		}

		@Override
		void update() //user clicked the action button
		{
			//generate an add inventory item request object and close the dialog
			addItemReq = new InventoryItem(tf[0].getText(), tf[1].getText());
			result = true;
			this.dispose();
		}

		@Override
		void delete() {	/* TODO Auto-generated method stub */  }

		@Override
		boolean fieldUnchanged()
		{ 
			//check that item text field isn't empty, the bar code text field is of proper
			//length and the bar code text field only contains digit characters (is numeric)
			if(!tf[0].getText().isEmpty())
			{
				//item is not empty, check the bar code field length 
				char[] barcode = tf[1].getText().toCharArray();
				if(barcode.length == BARCODE_FIELD_LENGTH)
				{
					//bar code is of proper length, check if all characters are numeric
					int index = 0;
					while(index < barcode.length && Character.isDigit(barcode[index++]));
					
					//if we get here, the item field is not empty, the bar code field contains
					//the proper number of characters. index should equal bar code length if
					//it's an acceptable bar code.
					return index < barcode.length;	//returns false if all conditions met and
				}									//item is acceptable to be added
			}
			
			return true;	//item is not ready to be added
		}
	}
}
