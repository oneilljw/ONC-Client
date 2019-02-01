package ourneighborschild;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class GiftCatalogDialog extends JDialog implements ActionListener, ListSelectionListener,
															DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to add gifts to the catalog
	 * and specify which child gift lists contain the gift. It also allows the catalog designer
	 * to specify what additional detail is requested from the user when they select or modify
	 * a gift for a child. Finally, it shows the user how many of each gift type have been 
	 * assigned to children in a particular year. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NAME_COL= 0;
	private static final int COUNT_COL = 1;
	private static final int GIFT_1_COL = 2;
	private static final int GIFT_2_COL = 3;
	private static final int GIFT_3_COL = 4;
	private static final int ADDL_DET_COL = 5;
	
	private ONCTable dlgTable;
	private AbstractTableModel gcTableModel;
	private JButton btnAdd, btnEdit, btnDelete, btnPrint;
	private GiftCatalogDB cat;
		
	public GiftCatalogDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Gift Catalog");
		
		//Save the reference to the one catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the gift catalog, including
		//this dialog
		cat = GiftCatalogDB.getInstance();
		if(cat != null)
			cat.addDatabaseListener(this);
	
		ChildDB childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		//Create the catalog table model
		gcTableModel = new GiftCatalogTableModel();
		
		//create the catalog table
		String[] colToolTips = {"Gift Name",
				"Total number of times this gift has been selected",
				"Check to include in Gift 1 List",
				"Check to include in Gift 2 List",
				"Check to include in Gift 3 List",
				"Is additional detail associated with this gift?"};
		
		dlgTable = new ONCTable(gcTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int[] colWidths = {152,56,48,48,48,80};
		for(int i=0; i < colWidths.length; i++)
			dlgTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
		
        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify gift count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        dlgTable.getColumnModel().getColumn(COUNT_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrint = new JButton("Print Catalog");
        btnPrint.setToolTipText("Print the gift catalog");
        btnPrint.addActionListener(this);
        
        btnAdd = new JButton("Add Gift");
        btnAdd.setToolTipText("Add a new gift to the catalog");
        btnAdd.addActionListener(this);
        
        btnEdit = new JButton("Edit Gift");
        btnEdit.setToolTipText("Edit the selected gift");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);
        
        btnDelete = new JButton("Delete Gift");
        btnDelete.setToolTipText("Delete the selected gift");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(this);
          
        cntlPanel.add(btnAdd);
        cntlPanel.add(btnEdit);
//      cntlPanel.add(btnDelete);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void edit()
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
			dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1)
		{
			ONCGift giftsel = cat.getGift(modelRow);
			
			GiftDetailDialog gdDlg =  new GiftDetailDialog(this, "Edit Catalog Gift");
			gdDlg.displayWishDetail(giftsel, cat.getTotalGiftCount(modelRow));
			
			gdDlg.setLocationRelativeTo(btnDelete);
			
			//a gift detail change could include the gift name changing or adding or removal
			//of a gift detail as well as the editing of a detail field. 
			if(gdDlg.showDialog())	//true if gift detail changed
			{
				//send updated gift request object to server
				String response = cat.update(this, giftsel);
			
				if(response != null && response.startsWith("UPDATED_CATALOG_WISH"))
				{
					//Gift has been updated, update to gift table with the name change
					gcTableModel.fireTableRowsUpdated(modelRow, modelRow);
					
				}
				else
				{
					String err_mssg = "ONC Server denied add catalog gift request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Add Catalog Gift Request Failure",
												JOptionPane.ERROR_MESSAGE,
												GlobalVariablesDB.getONCLogo());
				}
			}
		}
	}
	
	void add()
	{
		//create new gift request. Create the list with 4 slots for details. This HACK will
		//need to be fixed when the design is updated to a variable amount of detail per gift.
		LinkedList<Integer> giftDetailIDList = new LinkedList<Integer>();
		giftDetailIDList.add(-1);
		giftDetailIDList.add(-1);
		giftDetailIDList.add(-1);
		giftDetailIDList.add(-1);
	
		ONCGift reqAddGift = new ONCGift(-1, "New Gift", 0, giftDetailIDList);
		
		//use gift detail dialog to get input from user on the new gift request
		GiftDetailDialog gdDlg =  new GiftDetailDialog(this, "Add New Catalog Wish");
				
		gdDlg.displayWishDetail(reqAddGift, 0);
		gdDlg.setLocationRelativeTo(btnDelete);
		
		if(gdDlg.showDialog())	//returns true if gift detail changed
		{
			//send add gift request object to the database. It responds with the table row
			//where the added gift was inserted. If the requested gift wasn't added, a 
			//value of -1 is returned.
			int tableRow = cat.add(this, reqAddGift);
			if(tableRow > -1)
			{
				dlgTable.clearSelection();
				gcTableModel.fireTableDataChanged();
				dlgTable.scrollRectToVisible(dlgTable.getCellRect(tableRow, 0, true));
				dlgTable.setRowSelectionInterval(tableRow, tableRow);
			}
			else
			{
				String err_mssg = "Add catalog gift request failed, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add Catalog Gift Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
			}
		}
	}
	
	void delete()
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
			dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1 && cat.getTotalGiftCount(modelRow) == 0)
		{
			GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
			
			//create the delete gift request object
			ONCGift delreqGift = cat.getGift(modelRow);
			
			//Confirm with the user that the deletion is really intended
			String confirmMssg =String.format("Are you sure you want to delete %s from the catalog?", 
												delreqGift.getName());
	
			Object[] options= {"Cancel", "Delete Gift"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Gift Catalog Deletion ***");
			confirmDlg.setVisible(true);
	
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete Gift"))
			{
				//send updated gift request object to server
				String response = cat.delete(this, delreqGift);
		
				if(response != null && response.startsWith("DELETED_CATALOG_WISH"))
				{
					//Gift has been deleted
					gcTableModel.fireTableRowsDeleted(modelRow, modelRow);	
				}
				else
				{
					String err_mssg = "ONC Server denied delete catalog gift request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Delete Catalog Gift Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
				}
			}
		}
	}
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             dlgTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print gift catalog: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Gift Catalog Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnAdd)
		{
			add();
		}
		else if(e.getSource() == btnEdit)
		{
			edit();
		}
		else if(e.getSource() == btnDelete)
		{
			delete();
		}
		else if(e.getSource() == btnPrint)
		{
			print(Integer.toString(GlobalVariablesDB.getCurrentSeason()) + " ONC Gift Catalog");
		}		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
						dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1)
		{
			btnEdit.setEnabled(true);
			
			if(cat.getTotalGiftCount(modelRow) == 0)
				btnDelete.setEnabled(true);
			else
				btnDelete.setEnabled(false);
		}
		else
		{
			btnEdit.setEnabled(false);
			btnDelete.setEnabled(false);
		}
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("WISH_BASE_CHANGED"))
		{
			//User changed a gift base, must update gift counts
			ONCChildGift replGift = (ONCChildGift) dbe.getObject1();
			ONCChildGift addedGift = (ONCChildGift) dbe.getObject2();
			
			//update table
			if(replGift != null &&  replGift.getGiftID() > -1 || addedGift != null && addedGift.getGiftID() > -1)
					gcTableModel.fireTableDataChanged();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_CATALOG_WISH"))
		{
			ONCGift addedGift = (ONCGift) dbe.getObject1();
			int tablerow = cat.findModelIndexFromID(addedGift.getID());
			
			if(tablerow > -1)
			{
				//add new gift to gift table
				gcTableModel.fireTableDataChanged();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CATALOG_WISH"))
		{
			//determine which row the updated gift is in
			ONCGift updatedGift = (ONCGift) dbe.getObject1();
			int tablerow = cat.findModelIndexFromID(updatedGift.getID());
			gcTableModel.fireTableRowsUpdated(tablerow, tablerow);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CATALOG_WISH"))
		{
			gcTableModel.fireTableDataChanged();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_CATALOG"))
		{
			gcTableModel.fireTableDataChanged();
		}
	}
	
	class GiftCatalogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Gift Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		private GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		private String[] columnNames = {"Gift Name", "Count", "Gift 1", "Gift 2",
                                        "Gift 3", "Addl. Detail?"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return cat.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		if(col == NAME_COL)  
        			return cat.getGiftName(row);
        		else if(col == COUNT_COL)
        			return cat.getTotalGiftCount(row);
        		else if(col == ADDL_DET_COL)
        			return cat.isDetailRqrd(row) ? gvs.getImageIcon(21) : gvs.getImageIcon(22);
        		else
        			return cat.isInGiftList(row, col- GIFT_1_COL);      			
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == NAME_COL)
                return String.class;
        		if(column == COUNT_COL)
        			return Integer.class;
        		else if(column == ADDL_DET_COL)
        			return ImageIcon.class;
        		else
        			return Boolean.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
            //Only the check boxes can be edited and then only if there is not
        		//a gift already selected from the list associated with that column.
        		//also, if the gift is the default gift, it cannot be edited.
        		ONCGift gift = cat.getGift(row);
        		if(gift.getID() != gvs.getDefaultGiftID())
        		{
        			if(col == GIFT_1_COL && cat.getGiftCount(row, 0) == 0)
        	            	return true;
        	        else if(col == GIFT_2_COL && cat.getGiftCount(row, 1) == 0)
        	            	return true;
        	        else if(col == GIFT_3_COL && cat.getGiftCount(row, 2) == 0)
        	            	return true;
        	        else 
                     return false;
        		}
            else 
                return false;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
        		if(col >= GIFT_1_COL && col <= GIFT_3_COL)	//Gift list columns
        		{
        			ONCGift reqUpdateGift = new ONCGift(cat.getGift(row));	//copy current gift
        			int li = reqUpdateGift.getListindex(); //Get current list index	
        			int bitmask = 1 << col-GIFT_1_COL;	//which gift is being toggled
        		
        			reqUpdateGift.setListindex(li ^ bitmask); //Set updated list index
        		
        			String response = cat.update(this, reqUpdateGift);
        		
        			if(response == null || (response !=null && !response.startsWith("UPDATED_CATALOG_WISH")))
        			{
        				//request failed
        				GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
					String err_mssg = "ONC Server denied update catalog gift  request, try again later";
					JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), err_mssg, "Update Catalog Request Failure",
													JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
        			}
        		}                      
        }  
    }
}
