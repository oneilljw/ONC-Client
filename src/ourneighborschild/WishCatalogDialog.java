package ourneighborschild;

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
	
	private ONCTable dlgTable;
	private AbstractTableModel wcTableModel;
	private JButton btnAdd, btnEdit, btnDelete, btnPrint;
	private ONCWishCatalog cat;
		
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
		
//		ChildWishDB cwDB = ChildWishDB.getInstance();
//		if(cwDB != null)
//			cwDB.addDatabaseListener(this);	//Listen for child wish base changes
		
		ChildDB childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		//Create the catalog table model
		wcTableModel = new WishCatalogTableModel();
		
		//create the catalog table
		String[] colToolTips = {"Wish Name",
				"Total number of times this wish has been selected",
				"Check to include in Wish 1 List",
				"Check to include in Wish 2 List",
				"Check to include in Wish 3 List",
				"Is additional detail associated with this wish?"};
		
		dlgTable = new ONCTable(wcTableModel, colToolTips, new Color(240,248,255));

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
        
        //left justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        dlgTable.getColumnModel().getColumn(WISH_COUNT_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrint = new JButton("Print Catalog");
        btnPrint.setToolTipText("Print the wish catalog");
        btnPrint.addActionListener(this);
        
        btnAdd = new JButton("Add Wish");
        btnAdd.setToolTipText("Add a new wish to the catalog");
        btnAdd.addActionListener(this);
        
        btnEdit = new JButton("Edit Wish");
        btnEdit.setToolTipText("Edit the selected wish");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);
        
        btnDelete = new JButton("Delete Wish");
        btnDelete.setToolTipText("Delete the selected wish");
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
			ONCWish wishsel = cat.getWish(modelRow);
			
			WishDetailDialog wdDlg =  new WishDetailDialog(this, "Edit Catalog Wish");
			wdDlg.displayWishDetail(wishsel, cat.getTotalWishCount(modelRow));
			
			wdDlg.setLocationRelativeTo(btnDelete);
			
			//a wish detail change could include the wish name changing or adding or removal
			//of a wish detail as well as the editing of a detail field. 
			if(wdDlg.showDialog())	//true if wish detail changed
			{
				//send updated wish request object to server
				String response = cat.update(this, wishsel);
			
				if(response != null && response.startsWith("UPDATED_CATALOG_WISH"))
				{
					//Wish has been updated, update to wish table with the name change
					wcTableModel.fireTableRowsUpdated(modelRow, modelRow);
					
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
	
	void add()
	{
		//create new wish request
		ONCWish reqAddWish = new ONCWish(-1, "New Wish", 0);
		
		//use wish detail dialog to get input from user on the new wish request
		WishDetailDialog wdDlg =  new WishDetailDialog(this, "Add New Catalog Wish");
				
		wdDlg.displayWishDetail(reqAddWish, 0);
		wdDlg.setLocationRelativeTo(btnDelete);
		
		if(wdDlg.showDialog())	//returns true if wish detail changed
		{
			//send add wish request object to the database. It responds with the table row
			//where the added wish was inserted. If the requested wish wasn't added, a 
			//value of -1 is returned.
			int tableRow = cat.add(this, reqAddWish);
			if(tableRow > -1)
			{
				dlgTable.clearSelection();
				wcTableModel.fireTableDataChanged();
				dlgTable.scrollRectToVisible(dlgTable.getCellRect(tableRow, 0, true));
				dlgTable.setRowSelectionInterval(tableRow, tableRow);
			}
			else
			{
				String err_mssg = "Add catalog wish request failed, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add Catalog Wish Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
			}
		}
	}
	
	void delete()
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
			dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
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
					wcTableModel.fireTableRowsDeleted(modelRow, modelRow);	
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
			String err_mssg = "Unable to print wish catalog: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Wish Catalog Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
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
			print(Integer.toString(GlobalVariables.getCurrentSeason()) +
						" ONC Wish Catalog");
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
			
			if(cat.getTotalWishCount(modelRow) == 0)
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
			//User changed a wish base, must update wish counts
			ONCChildWish replWish = (ONCChildWish) dbe.getObject1();
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject2();
			
			//update table
			if(replWish != null &&  replWish.getWishID() > -1 || addedWish != null && addedWish.getWishID() > -1)
					wcTableModel.fireTableDataChanged();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_CATALOG_WISH"))
		{
			ONCWish addedWish = (ONCWish) dbe.getObject1();
			int tablerow = cat.findModelIndexFromID(addedWish.getID());
			
			if(tablerow > -1)
			{
				//add new wish to wish table
				wcTableModel.fireTableDataChanged();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CATALOG_WISH"))
		{
			//determine which row the updated wish is in
			ONCWish updatedWish = (ONCWish) dbe.getObject1();
			int tablerow = cat.findModelIndexFromID(updatedWish.getID());
			wcTableModel.fireTableRowsUpdated(tablerow, tablerow);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CATALOG_WISH"))
		{
			wcTableModel.fireTableDataChanged();
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
 
        public int getRowCount() { return cat.size(); }
 
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
