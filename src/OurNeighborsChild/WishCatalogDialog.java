package OurNeighborsChild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import javax.swing.BoxLayout;
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
import javax.swing.table.TableCellRenderer;

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
	private static final int WISH_COUNT_COLUMN = 2;
	
	private JTable wcTable;
	private AbstractTableModel wcTableModel;	//public due to parent listener
	private JButton btnAddWish, btnEditWish, btnDeleteWish, btnPrintCat;
	private ONCWishCatalog cat;
	private GlobalVariables wcGVs;
//	private WishDetailDialog wdDlg;
		
	public WishCatalogDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Wish Catalog");
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		cat = ONCWishCatalog.getInstance();
		wcGVs = GlobalVariables.getInstance();
		ChildWishDB cwDB = ChildWishDB.getInstance();
		cwDB.addDatabaseListener(this);	//Listen for child wish base changes
		ChildDB childDB = ChildDB.getInstance();
		childDB.addDatabaseListener(this);
		
		//add a listener for wish catalog changes
		cat.addDatabaseListener(this);
		
		//Create the table model
		wcTableModel = new WishCatalogTableModel();
		
		wcTable = new JTable(wcTableModel)
		{
			private static final long serialVersionUID = 1L;

			//Implement table header tool tips.
			protected String[] columnToolTips = {"Wish Name", "ID",
												"Total number of times this wish has been selected",
												"Check to include in Wish 1 List",
												"Check to include in Wish 2 List",
												"Check to include in Wish 3 List",
												"Is additional detail associated with this wish?"}; 
			
		    protected JTableHeader createDefaultTableHeader()
		    {
		        return new JTableHeader(columnModel)
		        {
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e)
		            {
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return columnToolTips[realIndex];
		            }
		        };
		    }
		    
			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
			{
			  Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			  		 
			  if(isRowSelected(Index_row))
				  comp.setBackground(comp.getBackground());
			  else if (Index_row % 2 == 1)			  
				  comp.setBackground(new Color(240,248,255));
			  else
				  comp.setBackground(Color.white);
			  
			  return comp;
			}
		};
		
		wcTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		wcTable.getSelectionModel().addListSelectionListener(this);
        
        wcTable.getColumnModel().getColumn(0).setPreferredWidth(152);
        wcTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        wcTable.getColumnModel().getColumn(2).setPreferredWidth(64);
        wcTable.getColumnModel().getColumn(3).setPreferredWidth(64);
        wcTable.getColumnModel().getColumn(4).setPreferredWidth(64);
        wcTable.getColumnModel().getColumn(5).setPreferredWidth(64);
        wcTable.getColumnModel().getColumn(6).setPreferredWidth(112);
        
        wcTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JTableHeader anHeader = wcTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center cell entries in columns 5 - Additional Detail 
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        wcTable.getColumnModel().getColumn(6).setCellRenderer(dtcr);
        
        //Set table size to 12 rows     
//        wcTable.setSize(wcTable.getRowHeight() * 12, wcTable.getWidth());
//        wcTable.setFillsViewportHeight(true);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(wcTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        cntlPanel.setPreferredSize(new Dimension(536, 50));
        
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
//      setSize(544, 290);
//      Point pt = pf.getLocation();
//      setLocation(pt.x + 120, pt.y + 120);
	}
	
	void editWish()
	{
		int row = wcTable.getSelectedRow();
		if(row > -1)
		{
			ONCWish wishsel = cat.getWish(row);
			
			WishDetailDialog wdDlg =  new WishDetailDialog(this, "Edit Catalog Wish");
			wdDlg.displayWishDetail(wishsel, cat.getTotalWishCount(row));
			
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
					wcTableModel.fireTableRowsUpdated(row, row);
				}
				else
				{
					String err_mssg = "ONC Server denied add catalog wish request, try again later";
					JOptionPane.showMessageDialog(wcGVs.getFrame(), err_mssg, "Add Catalog Wish Request Failure",
												JOptionPane.ERROR_MESSAGE, wcGVs.getImageIcon(0));
				}
			}
			
//			wdDlg.setVisible(true);							
//			if(wdDlg.hasWishDetailChanged())
//				wcTableModel.fireTableRowsUpdated(row, row);//Update the table row	
		}
	}
	
	void addWish()
	{
		//create new wish request
		ONCWish reqAddWish = new ONCWish(-1, "New Wish", 0);
		
		//use wish detail dialog to get input from user on the new wish request
		wcTable.clearSelection();
		
		WishDetailDialog wdDlg =  new WishDetailDialog(this, "Add New Catalog Wish");
				
//		wdDlg.setTitle("Add New Catalog Wish");
		wdDlg.displayWishDetail(reqAddWish, 0);
		wdDlg.setLocationRelativeTo(btnDeleteWish);
		
		if(wdDlg.showDialog())	//returns true if wish detail changed
		{
			//send add wish request object to server
			String response = cat.add(this, reqAddWish);
		
			if(response != null && response.startsWith("ADDED_CATALOG_WISH"))
			{
				//add new wish to wish table
				wcTableModel.fireTableRowsInserted(cat.getNumberOfItems()-1, cat.getNumberOfItems()-1);
				wcTable.scrollRectToVisible(wcTable.getCellRect(wcTable.getRowCount()-1, 0, true));
				wcTable.setRowSelectionInterval(cat.getNumberOfItems()-1, cat.getNumberOfItems()-1);
			}
			else
			{
				String err_mssg = "ONC Server denied add catalog wish request, try again later";
				JOptionPane.showMessageDialog(wcGVs.getFrame(), err_mssg, "Add Catalog Wish Request Failure",
											JOptionPane.ERROR_MESSAGE, wcGVs.getImageIcon(0));
			}
		}
	}
	
	void deleteWish()
	{
		int row = wcTable.getSelectedRow();
		if(row > -1 && cat.getTotalWishCount(row) == 0)
		{
			GlobalVariables gvs = GlobalVariables.getInstance();
			
			//create the delete wish request object
			ONCWish delreqWish = cat.getWish(row);
			
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
					//Wish has been updated, update to wish table with the name change
					wcTableModel.fireTableRowsDeleted(row, row);
					wcTable.clearSelection();
				}
				else
				{
					String err_mssg = "ONC Server denied delete catalog wish request, try again later";
					JOptionPane.showMessageDialog(wcGVs.getFrame(), err_mssg, "Delete Catalog Wish Request Failure",
											JOptionPane.ERROR_MESSAGE, wcGVs.getImageIcon(0));
				}
			}
		}
	}
	
	void onPrintCatalog()
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(Integer.toString(wcGVs.getCurrentSeason()) +
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
		int row = wcTable.getSelectedRow();
		
		if(row > -1)
		{
			btnEditWish.setEnabled(true);
			
			if(cat.getTotalWishCount(row) == 0)
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
	
	class WishCatalogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Wish Name", "ID", "Count", "Wish 1", "Wish 2",
                                        "Wish 3", "Additional Detail"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return cat.getNumberOfItems(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)  
        		return cat.getWishName(row);
        	else if(col == 1)
        		return cat.getWishID(row);
        	else if(col == 2)
        		return cat.getTotalWishCount(row);
        	else if(col == 6)
        		return cat.isDetailRqrd(row) ? "Yes" : "No";
        	else
        		return cat.isInWishList(row, col-3);      			
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int c)
        {
        	if(c == 0)
                return String.class;
        	if(c == 1 || c == 2)
        		return Integer.class;
        	else if(c == 6)
        		return String.class;
        	else
        		return Boolean.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
            //Only the check boxes can be edited and then only if there is not
        	//a wish already selected from the list associated with that column
            if(col==3 && cat.getWishCount(row, 0) == 0)
            	return true;
            else if(col==4 && cat.getWishCount(row, 1) == 0)
            	return true;
            else if(col==5 && cat.getWishCount(row, 2) == 0)
            	return true;
            else 
                return false;
        }
 
        //Don't need to implement this method unless your table's data can change. 
        public void setValueAt(Object value, int row, int col)
        { 	
        	if(col >= 3 && col <= 5)	//Wish list columns
        	{
        		ONCWish reqUpdateWish = new ONCWish(cat.getWish(row));	//copy current wish
        		int li = reqUpdateWish.getListindex(); //Get current list index	
        		int bitmask = 1 << col-3;	//which wish is being toggled
        		
        		reqUpdateWish.setListindex(li ^ bitmask); //Set updated list index
        		
        		String response = cat.update(this, reqUpdateWish);
        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_CATALOG_WISH")))
        		{
        			//request failed
        			GlobalVariables gvs = GlobalVariables.getInstance();
					String err_mssg = "ONC Server denied update catalog wish  request, try again later";
					JOptionPane.showMessageDialog(gvs.getFrame(), err_mssg, "Update Catalog Request Failure",
													JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
        		}
        	}                      
        }  
    }

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("WISH_BASE_CHANGED"))
		{
			//User changed a wish base, must update wish counts
			WishBaseOrOrgChange wbc = (WishBaseOrOrgChange) dbe.getObject();
			
			//get row deleted from catalog and update
			int row = cat.findWishRow(wbc.getOldItem());
			wcTableModel.fireTableCellUpdated(row, WISH_COUNT_COLUMN);
			
			//get row incremented from catalog and update
			row = cat.findWishRow(wbc.getNewItem());
			wcTableModel.fireTableCellUpdated(row, WISH_COUNT_COLUMN);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_CATALOG_WISH"))
		{
			//add new wish to wish table
			wcTableModel.fireTableRowsInserted(cat.getNumberOfItems()-1, cat.getNumberOfItems()-1);
			wcTable.scrollRectToVisible(wcTable.getCellRect(wcTable.getRowCount()-1, 0, true));
			wcTable.setRowSelectionInterval(cat.getNumberOfItems()-1, cat.getNumberOfItems()-1);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CATALOG_WISH"))
		{
			//determine which row the updated wish is in
			ONCWish updatedWish = (ONCWish) dbe.getObject();
			int tablerow = cat.findWishRow(updatedWish.getName());
			
			wcTableModel.fireTableRowsUpdated(tablerow, tablerow);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CATALOG_WISH"))
		{
			System.out.println(String.format("WishCatDlg - dataChanged: Source: %s, Type %s, Object: %s",
					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			//determine which row the updated wish is in
			ONCWish deletedWish = (ONCWish) dbe.getObject();
			System.out.println(String.format("WishCatDlg - dataChanged: Wish ID: %d, Name: %s",
					deletedWish.getID(), deletedWish.getName()));
			int tablerow = cat.findWishRow(deletedWish.getName());
			System.out.println(String.format("WishCatDlg - dataChanged: tablerow: %d", tablerow));
			
//			wcTableModel.fireTableRowsDeleted(tablerow, tablerow);
			wcTableModel.fireTableDataChanged();
//			wcTable.clearSelection();
		}	
	}
}
