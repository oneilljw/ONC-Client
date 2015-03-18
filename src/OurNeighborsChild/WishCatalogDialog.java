package OurNeighborsChild;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;

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

public class WishCatalogDialog extends JDialog implements ActionListener, ListSelectionListener,
															DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to add wishes to the wish catalog
	 * and specify which child wish lists contain the wish. It also allows the catalog designer
	 * to specify what additional detail is requested from the user when they select or modify
	 * a gift for a child. Finally, it shows the user how many of each gift type have been 
	 * assigned to children in a particular year. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int WISH_NAME_COL= 0;
	private static final int WISH_COUNT_COL = 1;
	private static final int WISH_1_COL = 2;
	private static final int WISH_2_COL = 3;
	private static final int WISH_3_COL = 4;
	private static final int WISH_ADD_DET_COL = 5;
//	private static final int TABLE_VERTICAL_SCROLL_WIDTH = 24;
	
	private ONCTable wcTable;
	private AbstractTableModel wcTableModel;
	private JButton btnAddWish, btnEditWish, btnDeleteWish, btnPrintCat;
	private ONCWishCatalog cat;
	private boolean bTableChanging;
		
	public WishCatalogDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Wish Catalog");
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		cat = ONCWishCatalog.getInstance();
		if(cat != null)
			cat.addDatabaseListener(this);
		
		ChildWishDB cwDB = ChildWishDB.getInstance();
		if(cwDB != null)
			cwDB.addDatabaseListener(this);	//Listen for child wish base changes
		
		ChildDB childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		//Create the catalog table model
		wcTableModel = new WishCatalogTableModel();
		
		bTableChanging = false;
		
		//create the catalog table
		String[] colToolTips = {"Wish Name",
				"Total number of times this wish has been selected",
				"Check to include in Wish 1 List",
				"Check to include in Wish 2 List",
				"Check to include in Wish 3 List",
				"Is additional detail associated with this wish?"};
		
		wcTable = new ONCTable(wcTableModel, colToolTips, new Color(240,248,255));

		wcTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		wcTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int[] colWidths = {152,56,48,48,48,80};
//		int tablewidth = 0;
		for(int i=0; i < colWidths.length; i++)
		{
			wcTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
//			tablewidth += colWidths[i];
		}
//		tablewidth += TABLE_VERTICAL_SCROLL_WIDTH; 	//Account for vertical scroll bar
        
        wcTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        wcTable.setAutoCreateRowSorter(true);
        
        JTableHeader anHeader = wcTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        wcTable.getColumnModel().getColumn(WISH_COUNT_COL).setCellRenderer(dtcr);
        
        //Set table size to 12 rows     
//      wcTable.setSize(wcTable.getRowHeight() * 12, wcTable.getWidth());
//      wcTable.setFillsViewportHeight(true);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(wcTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrintCat = new JButton("Print Catalog");
        btnPrintCat.setToolTipText("Print the wish catalog");
        btnPrintCat.addActionListener(this);
        
        btnAddWish = new JButton("Add Wish");
        btnAddWish.setToolTipText("Add a new wish to the catalog");
        btnAddWish.addActionListener(this);
        
        btnEditWish = new JButton("Edit Wish");
        btnEditWish.setToolTipText("Edit the selected wish");
        btnEditWish.setEnabled(false);
        btnEditWish.addActionListener(this);
        
        btnDeleteWish = new JButton("Delete Wish");
        btnDeleteWish.setToolTipText("Delete the selected wish");
        btnDeleteWish.setEnabled(false);
        btnDeleteWish.addActionListener(this);
          
        cntlPanel.add(btnAddWish);
        cntlPanel.add(btnEditWish);
        cntlPanel.add(btnDeleteWish);
        cntlPanel.add(btnPrintCat);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void editWish()
	{
		int viewRow = wcTable.getSelectedRow();
		int modelRow = wcTable.convertRowIndexToModel(viewRow);
		if(modelRow > -1)
		{
			ONCWish wishsel = cat.getWish(modelRow);
			
			WishDetailDialog wdDlg =  new WishDetailDialog(this, "Edit Catalog Wish");
			wdDlg.displayWishDetail(wishsel, cat.getTotalWishCount(modelRow));
			
			wdDlg.setLocationRelativeTo(btnDeleteWish);
			
			//a wish detail change could include the wish name changing or adding or removal
			//of a wish detail as well as the editing of a detail field. 
			if(wdDlg.showDialog())	//true if wish detail changed
			{
				//send updated wish request object to server
				String response = cat.update(this, wishsel);
			
				if(response != null && response.startsWith("UPDATED_CATALOG_WISH"))
				{
					//Wish has been updated, update to wish table with the name change
					bTableChanging = true;
					wcTableModel.fireTableRowsUpdated(modelRow, modelRow);
					bTableChanging = false;
				}
				else
				{
					String err_mssg = "ONC Server denied add catalog wish request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Add Catalog Wish Request Failure",
												JOptionPane.ERROR_MESSAGE,
												GlobalVariables.getONCLogo());
				}
			}
		}
	}
	
	void addWish()
	{
		//create new wish request
		ONCWish reqAddWish = new ONCWish(-1, "New Wish", 0);
		
		//use wish detail dialog to get input from user on the new wish request
		wcTable.clearSelection();
		
		WishDetailDialog wdDlg =  new WishDetailDialog(this, "Add New Catalog Wish");
				
		wdDlg.displayWishDetail(reqAddWish, 0);
		wdDlg.setLocationRelativeTo(btnDeleteWish);
		
		if(wdDlg.showDialog())	//returns true if wish detail changed
		{
			//send add wish request object to the database. It responds with the table row
			//where the added wish was inserted. If the requested wish wasn't added, a 
			//value of -1 is returned.
			int tableRow = cat.add(this, reqAddWish);
			if(tableRow > -1)
			{
				System.out.println(String.format("WishCatDialog.addWish: TableRowAdded: %d, table size %d",
						tableRow, cat.getNumberOfItems()));
//				wcTableModel.fireTableRowsInserted(tableRow, tableRow);
				bTableChanging = true;
				wcTableModel.fireTableDataChanged();
				wcTable.scrollRectToVisible(wcTable.getCellRect(tableRow, 0, true));
				wcTable.setRowSelectionInterval(tableRow, tableRow);
				bTableChanging = false;
			}
			else
			{
				String err_mssg = "Add catalog wish request failed, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add Catalog Wish Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
			}
		}
	}
	
	void deleteWish()
	{
		int viewRow = wcTable.getSelectedRow();
		int modelRow = wcTable.convertRowIndexToModel(viewRow);
		if(modelRow > -1 && cat.getTotalWishCount(modelRow) == 0)
		{
			GlobalVariables gvs = GlobalVariables.getInstance();
			
			//create the delete wish request object
			ONCWish delreqWish = cat.getWish(modelRow);
			
			//Confirm with the user that the deletion is really intended
			String confirmMssg =String.format("Are you sure you want to delete %s from the catalog?", 
												delreqWish.getName());
	
			Object[] options= {"Cancel", "Delete Wish"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Wish Catalog Deletion ***");
			confirmDlg.setVisible(true);
	
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete Wish"))
			{
				//send updated wish request object to server
				String response = cat.delete(this, delreqWish);
		
				if(response != null && response.startsWith("DELETED_CATALOG_WISH"))
				{
					//Wish has been deleted
					bTableChanging = true;
					wcTableModel.fireTableRowsDeleted(modelRow, modelRow);
					bTableChanging = false;
//					wcTableModel.fireTableDataChanged();
				}
				else
				{
					String err_mssg = "ONC Server denied delete catalog wish request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Delete Catalog Wish Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
				}
			}
		}
	}
	
	void onPrintCatalog()
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(Integer.toString(GlobalVariables.getCurrentSeason()) +
					 											" ONC Wish Catalog");
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             wcTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnAddWish)
		{
			addWish();
		}
		else if(e.getSource() == btnEditWish)
		{
			editWish();
		}
		else if(e.getSource() == btnDeleteWish)
		{
			deleteWish();
		}
		else if(e.getSource() == btnPrintCat)
		{
			onPrintCatalog();
		}		
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int viewRow = wcTable.getSelectedRow();
		int modelRow = viewRow == -1 ? -1 : wcTable.convertRowIndexToModel(viewRow);
		
		if(modelRow > -1)
			System.out.println(String.format("WishCatalogDlg.valueChanged: view row: %d, model row: %d",
				viewRow, modelRow));
		else
			System.out.println("WishCatalogDlg.valueChanged: Selection no longer in visible table");
		
		if(modelRow > -1)
		{
			btnEditWish.setEnabled(true);
			
			if(cat.getTotalWishCount(modelRow) == 0)
				btnDeleteWish.setEnabled(true);
			else
				btnDeleteWish.setEnabled(false);
		}
		else
		{
			btnEditWish.setEnabled(false);
			btnDeleteWish.setEnabled(false);
		}
	}
	

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("WISH_BASE_CHANGED"))
		{
			//User changed a wish base, must update wish counts
			WishBaseChange wbc = (WishBaseChange) dbe.getObject();
			ONCChildWish replWish = (ONCChildWish) wbc.getReplacedWish();
			ONCChildWish addedWish = (ONCChildWish) wbc.getAddedWish();
			
			String logEntry = String.format("WishCatalog Event: %s, -- Wish ID: %d, ++ Wish ID: %d",
					dbe.getType(), replWish.getWishID(), addedWish.getWishID());
			LogDialog.add(logEntry, "M");
			
			//get row of decremented wish from catalog and update table
			int row;
			
			bTableChanging = true;
			if(replWish != null &&  replWish.getWishID() > -1 &&
				(row = cat.findWishRow(replWish.getWishID())) > -1)
				wcTableModel.fireTableCellUpdated(row, WISH_COUNT_COL);
			
			//get row  of incremented wish from catalog and update
			if(addedWish != null && addedWish.getWishID() > -1  &&
				(row = cat.findWishRow(addedWish.getWishID())) > -1)
				wcTableModel.fireTableCellUpdated(row, WISH_COUNT_COL);
			bTableChanging = false;
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_CATALOG_WISH"))
		{
			ONCWish addedWish = (ONCWish) dbe.getObject();
			int tablerow = cat.findWishRow(addedWish.getID());
//			System.out.println(String.format("WishCatDlg.dataChanged: tablerow: %d", tablerow));
//			tablerow = wcTable.convertRowIndexToView(cat.findWishRow(addedWish.getID()));
//			System.out.println(String.format("WishCatDlg.dataChanged: tablerow: %d", tablerow));
			if(tablerow > -1)
			{
				//add new wish to wish table
//				wcTableModel.fireTableRowsInserted(tablerow, tablerow);
				bTableChanging = true;
				wcTableModel.fireTableDataChanged();
				bTableChanging = false;
//				wcTable.scrollRectToVisible(wcTable.getCellRect(tablerow, 0, true));
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CATALOG_WISH"))
		{
			//determine which row the updated wish is in
			ONCWish updatedWish = (ONCWish) dbe.getObject();
			int tablerow = cat.findWishRow(updatedWish.getID());
			
			bTableChanging = true;
			wcTableModel.fireTableRowsUpdated(tablerow, tablerow);
			bTableChanging = false;
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CATALOG_WISH"))
		{
			bTableChanging = true;
			wcTableModel.fireTableDataChanged();
			bTableChanging = false;
		}	
	}
	
	class WishCatalogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		private GlobalVariables gvs = GlobalVariables.getInstance();
		private String[] columnNames = {"Wish Name", "Count", "Wish 1", "Wish 2",
                                        "Wish 3", "Addl. Detail?"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount()
        { 
//        	System.out.println(String.format("WishCatDlg.getRowCount: #rows: %d",
//        			cat.getNumberOfItems()));
        	return cat.getNumberOfItems();
        }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	if(col == WISH_NAME_COL)  
        		return cat.getWishName(row);
        	else if(col == WISH_COUNT_COL)
        		return cat.getTotalWishCount(row);
        	else if(col == WISH_ADD_DET_COL)
        		return cat.isDetailRqrd(row) ? gvs.getImageIcon(21) : gvs.getImageIcon(22);
        	else
        		return cat.isInWishList(row, col- WISH_1_COL);      			
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == WISH_NAME_COL)
                return String.class;
        	if(column == WISH_COUNT_COL)
        		return Integer.class;
        	else if(column == WISH_ADD_DET_COL)
        		return ImageIcon.class;
        	else
        		return Boolean.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
            //Only the check boxes can be edited and then only if there is not
        	//a wish already selected from the list associated with that column
            if(col == WISH_1_COL && cat.getWishCount(row, 0) == 0)
            	return true;
            else if(col == WISH_2_COL && cat.getWishCount(row, 1) == 0)
            	return true;
            else if(col == WISH_3_COL && cat.getWishCount(row, 2) == 0)
            	return true;
            else 
                return false;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
        	if(col >= WISH_1_COL && col <= WISH_3_COL)	//Wish list columns
        	{
        		ONCWish reqUpdateWish = new ONCWish(cat.getWish(row));	//copy current wish
        		int li = reqUpdateWish.getListindex(); //Get current list index	
        		int bitmask = 1 << col-WISH_1_COL;	//which wish is being toggled
        		
        		reqUpdateWish.setListindex(li ^ bitmask); //Set updated list index
        		
        		String response = cat.update(this, reqUpdateWish);
        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_CATALOG_WISH")))
        		{
        			//request failed
        			GlobalVariables gvs = GlobalVariables.getInstance();
					String err_mssg = "ONC Server denied update catalog wish  request, try again later";
					JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Update Catalog Request Failure",
													JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
        		}
        	}                      
        }  
    }
}
