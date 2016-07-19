package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.gson.Gson;

public class InventoryDialog extends BarcodeTableDialog implements ActionListener, ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NAME_COL = 0;
	private static final int TYPE_COL = 1;
	private static final int COUNT_COL = 2;
	private static final int COMMIT_COL = 3;
	private static final int BARCODE_COL= 4;
	private static final int DEFAULT_TABLE_ROW_COUNT = 15;
	
	private InventoryDB inventoryDB;
	private ONCWishCatalog cat;
	private JRadioButton rbEdit, rbAdd, rbRemove;
	private JComboBox wishCB;
	private DefaultComboBoxModel wishCBM;

	public InventoryDialog(JFrame parentFrame) 
	{
		super(parentFrame);
		this.setTitle("ONC Inventory Management");
		
		inventoryDB = InventoryDB.getInstance();
		if(inventoryDB != null)
			inventoryDB.addDatabaseListener(this);
		
		cat = ONCWishCatalog.getInstance();
		if(cat != null)
			cat.addDatabaseListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		
		rbEdit = new JRadioButton("Edit Item");
		rbEdit.addActionListener(this);
		rbAdd = new JRadioButton("Add Qty");
		rbAdd.addActionListener(this);
		rbRemove = new JRadioButton("Remove Qty");
		rbRemove.addActionListener(this);
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(rbEdit);
		modeGroup.add(rbAdd);
		modeGroup.add(rbRemove);
		rbEdit.setSelected(true);
		
		buttonPanel.add(rbEdit);
		buttonPanel.add(rbAdd);
		buttonPanel.add(rbRemove);
		
		topPanel.add(buttonPanel, BorderLayout.EAST);
		
		//set up the columns that use combo box editors for the status, access and permission enums
		TableColumn typeColumn = dlgTable.getColumnModel().getColumn(TYPE_COL);
		//set up the catalog wish type panel
        wishCBM = new DefaultComboBoxModel();
        for(ONCWish w: cat.getWishList(WishListPurpose.Selection))	//Add new list elements
			wishCBM.addElement(w);
        wishCB = new JComboBox();
        wishCB.setModel(wishCBM);
		typeColumn.setCellEditor(new DefaultCellEditor(wishCB));
		
		dlgTable.getSelectionModel().addListSelectionListener(this);
		dlgTable.setDragEnabled(false);	//only enabled when commits are less than count
		dlgTable.setTransferHandler(new InventoryTransferHandler());
		
		btnAction.setText("Add Item");
	}
	
	void updateWishSelectionList()
	{
		wishCBM.removeAllElements();	//Clear the combo box selection list

		for(ONCWish w: cat.getWishList(WishListPurpose.Selection))	//Add new list elements
			wishCBM.addElement(w);
	}
	
	@Override
	void onExport()
	{
    	String[] header = {"Item", "Wish Type", "Qty", "# Commits", "Barcode"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of ONC Gift Inventory" ,
       										new FileNameExtensionFilter("CSV Files", "csv"), 1);
       	if(oncwritefile!= null)
       	{
       		//If user types a new filename without extension.csv, add it
	    	String filePath = oncwritefile.getPath();
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    int[] row_sel = dlgTable.getSelectedRows();
	    	    for(int i=0; i<dlgTable.getSelectedRowCount(); i++)
	    	    {
	    	    	int index = row_sel[i];
	    	    	InventoryItem ii = inventoryDB.getItem(index);
	    	    	String[] exportRow = new String[3];
	    	    	exportRow[0] = ii.getItemName();
	    	    	exportRow[1] = ii.getWishID() == -1 ? " None" : cat.getWishName(ii.getWishID());
	    	    	exportRow[2] = Integer.toString(ii.getCount());
	    	    	exportRow[2] = Integer.toString(ii.getNCommits());
	    	    	exportRow[4] = ii.getNumber();
	    	    	writer.writeNext(exportRow);
	    	    }
	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(this, 
						dlgTable.getSelectedRowCount() + " inventory gifts sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().contains("_INVENTORY_ITEM"))
		{
			dlgTableModel.fireTableDataChanged();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG"))
		{
			updateWishSelectionList();
			dlgTableModel.fireTableDataChanged();
		}
	}
	
	@Override
	String[] getColumnToolTips()
	{
		return new String[] {"Item Name & Description", "Catalog Wish Type", 
				"Quanity of item in inventory", "# of times item has been commited",
				"Item Barcode"};
	}
	
	@Override
	int[] getColumnWidths() { return new int[] {432, 177, 32, 32, 104}; }
	
	@Override
	int[] getLeftColumns() { return new int[] {}; }
	
	@Override
	int[] getCenteredColumns() { return new int[] {COUNT_COL, COMMIT_COL}; }
	
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
			InventoryRequest invAddReq = new InventoryRequest(barcode, rbAdd.isSelected() ? 1 : -1,
					rbAdd.isSelected() ? 0 : -1);
		
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
				
				//make sure item isn't already in the inventory. Suppress leading zeros in the
				//request
				addItemReq.setNumber(addItemReq.getNumber().replaceFirst("^0+(?!$)", ""));
				if(addItemReq != null && inventoryDB.getItemByBarcode(addItemReq.getNumber()) == null)
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
			
			int commits = (Integer) dlgTableModel.getValueAt(modelRow, COMMIT_COL);
			dlgTable.setDragEnabled(count > 0 && commits < count);
		}
		else
		{
			btnDelete.setEnabled(false);
			dlgTable.setDragEnabled(false);
		}
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
		
		private String[] columnNames = {"Item", "Type", "Qty", "Com", "Barcode"};
 
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
        		return ii.getItemName();
        	else if(col == COMMIT_COL)
        		return ii.getNCommits();
        	else if(col == TYPE_COL)
        		return ii.getWishID() == -1 ? new ONCWish(-1, "None", 7) : cat.getWishByID(ii.getWishID());
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == COUNT_COL || column == COMMIT_COL)
        		return Integer.class;
        	else if(column == TYPE_COL)
        		return ONCWish.class;
        	else
        		return String.class;
        }
 
        /****
         * A table cell is editable if the mode is set to edit. It the mode is Edit, the 
         * name column can be edited by all users. Only administrator or above users can edit the
         * quantity or bar code columns.
         */
        public boolean isCellEditable(int row, int col)
        {
        	if(!rbEdit.isSelected())
        		return false;
        	else if(col == NAME_COL || col == TYPE_COL)
        		return true;
        	else if(UserDB.getInstance().getLoggedInUser().getPermission().compareTo(UserPermission.Admin) <0)
        		return false;
        	else if(col == COUNT_COL || col == COMMIT_COL || col == BARCODE_COL)
        		return true;
        	else
        		return false;
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
        	else if(col == COMMIT_COL && selItem.getNCommits() != ((Integer) value))
        	{
        		reqUpdateItem = new InventoryItem(selItem);	//make a copy
        		reqUpdateItem.setNCommits((Integer) value);
        	}
        	else if(col == BARCODE_COL && !selItem.getNumber().equals(((String) value)))
        	{
        		reqUpdateItem = new InventoryItem(selItem);	//make a copy
        		reqUpdateItem.setNumber( ((String) value).replaceFirst("^0+(?!$)", ""));
        	}
        	else if(col == TYPE_COL && selItem.getWishID() != ((ONCWish) value).getID())
        	{
        		reqUpdateItem = new InventoryItem(selItem);	//make a copy
        		reqUpdateItem.setWishID(((ONCWish) value).getID());
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
	
	private class InventoryTransferHandler extends TransferHandler
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		@Override
		public int getSourceActions(JComponent c)
		{
	        return TransferHandler.COPY;
	    }
		
		@Override
		protected Transferable createTransferable(JComponent c)
		{
			Transferable iit = new InventoryItemTransferable(c);
			return iit;
		}
		
		@Override
		protected void exportDone(JComponent source, Transferable data, int action)
		{
			if(action == TransferHandler.COPY && source == dlgTable)
			{
				DataFlavor df = new DataFlavor(InventoryItem.class, "InventoryItem");
				try 
				{
					InventoryItem itemXfered = (InventoryItem) data.getTransferData(df);
					if(itemXfered != null)
					{
						//transfer was successful, increase the commits
						InventoryRequest req = new InventoryRequest(itemXfered.getNumber(), 0, 1);
						String response = inventoryDB.add(this, req);
						if(response.startsWith("INCREMENTED_INVENTORY_ITEM"))
							dlgTableModel.fireTableDataChanged();
					}
				} 
				catch (UnsupportedFlavorException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private class AddInventoryItemDialog extends InfoDialog
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int NAME_FIELD = 0;
		private static final int WISH_FIELD = 1;
		private static final int BARCODE_FIELD = 2;
		private static final int BARCODE_FIELD_LENGTH = 12;
		
		private InventoryItem addItemReq;
		private JComboBox wishCB;
		private DefaultComboBoxModel wishCBM;

		AddInventoryItemDialog(JFrame pf, boolean bModal)
		{
			super(pf, bModal);
			this.setTitle("Add Inventory Item");
			
			addItemReq = null;
			
			lblONCIcon.setText("<html><font color=blue>Add New Inventory Item<br>Information Below</font></html>");
			btnAction.setText("Add Item");
			btnDelete.setText("Clear");
			btnDelete.setVisible(true);
			
			//Set up the main panel, loop to set up components associated with names
			for(int pn=0; pn < getDialogFieldNames().length; pn++)
			{
				tf[pn] = new JTextField(20);
				tf[pn].addKeyListener(tfkl);
				infopanel[pn].add(tf[pn]);
			}
			
			//set up the catalog wish type panel
	        wishCBM = new DefaultComboBoxModel();
	        for(ONCWish w: cat.getWishList(WishListPurpose.Selection))	//Add new list elements
				wishCBM.addElement(w);
	        wishCB = new JComboBox();
	        wishCB.setModel(wishCBM);
	        wishCB.setPreferredSize(new Dimension(256, 32));
	        wishCB.setToolTipText("Select wish type from ONC Wish Catalog");
	        wishCB.addActionListener(new ActionListener()
	        {
	        	//if wish is changed, test to see if all Add Item conditions are met. If
	        	//they are, enable the Add Item button
	            public void actionPerformed(ActionEvent e)
	            {
	            	btnAction.setEnabled(!fieldUnchanged());
	            }
	        });
	        
			infopanel[WISH_FIELD].remove(tf[WISH_FIELD]);
			infopanel[WISH_FIELD].add(wishCB);
			
			pack();
		}
		
		InventoryItem getAddItemRequest() {return addItemReq;}

		@Override
		String[] getDialogFieldNames() 
		{
			return new String[] {"Item", "Wish Type", "Barcode"};
		}

		@Override
		void display(ONCObject obj) { /* Required method in InfoDialog, unused here */ }

		@Override
		void update() //user clicked the action button
		{
			//generate an add inventory item request object and close the dialog
			ONCWish addReqWishType = (ONCWish) wishCB.getSelectedItem();
			addItemReq = new InventoryItem(tf[NAME_FIELD].getText(), addReqWishType.getID(),
											tf[BARCODE_FIELD].getText());
			result = true;
			this.dispose();
		}

		@Override
		void delete()
		{
			//Reset all fields
			tf[NAME_FIELD].setText("");
			tf[BARCODE_FIELD].setText("");
			wishCB.setSelectedIndex(0);
		}

		@Override
		boolean fieldUnchanged()
		{ 
			//check that item text field isn't empty, the bar code text field is of proper
			//length and the bar code text field only contains digit characters (is numeric)
			if(!tf[NAME_FIELD].getText().isEmpty())
			{
				//item is not empty, check that wishid != 1 && bar code field length 
				char[] barcode = tf[BARCODE_FIELD].getText().toCharArray();
				ONCWish reqWish = (ONCWish) wishCB.getSelectedItem();
				
				if(reqWish.getID() != -1 && barcode.length == BARCODE_FIELD_LENGTH)
				{
					//bar code is of proper length, check if all characters are numeric
					int index = 0;
					while(index < barcode.length && Character.isDigit(barcode[index++]));
					
					//if we get here, the item field is not empty, the bar code field contains
					//the proper number of characters. index should equal bar code length if
					return index < barcode.length;	//returns false if all conditions met and
				}									//item is acceptable to be added
			}
			
			return true;	//item is not ready to be added
		}
	}
}
