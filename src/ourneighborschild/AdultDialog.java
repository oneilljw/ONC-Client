package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class AdultDialog extends JDialog implements ActionListener, EntitySelectionListener,
														ListSelectionListener, DatabaseListener 
{
	/**
	 * This class implements a dialog which allows the user to manage adults in families
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_NUM_COL= 0;
	private static final int NAME_COL= 1;
	private static final int GENDER_COL= 2;
	
	private JFrame owner;
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnAdd, btnDelete, btnPrint;
	private AdultDB adultDB;
	private UserDB userDB;
	private ONCFamily currFam;
	private List<ONCAdult> tableList;
	
	public AdultDialog(JFrame pf)
	{
		super(pf);
		this.owner = pf;
		
		//Save the reference to the adult data base
		adultDB = AdultDB.getInstance();
		if(adultDB != null)
			adultDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		//create the list of adults shown in the table
		tableList = new ArrayList<ONCAdult>();
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"ONC #", "Name", "Gender"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		//set up the gender column with a combo box
		TableColumn genderColumn = dlgTable.getColumnModel().getColumn(GENDER_COL);
		genderColumn.setCellEditor(new DefaultCellEditor(new JComboBox(AdultGender.values())));
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {40, 160, 56};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 88));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the adults in the family");
        btnPrint.addActionListener(this);
        
        btnAdd = new JButton("Add");
        btnAdd.setToolTipText("Add a new adult to family");
        btnAdd.setEnabled(false);
        btnAdd.addActionListener(this);
    
        btnDelete = new JButton("Delete");
        btnDelete.setToolTipText("Delete adult from family");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(this);
          
        cntlPanel.add(btnAdd);
        cntlPanel.add(btnDelete);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
	}
	
	void display(ONCFamily fam)
	{
		currFam = fam;
		tableList = adultDB.getAdultsInFamily(currFam.getID());
		
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)	//can't add is not administrator or higher permission
		{
			btnAdd.setEnabled(true);
			this.setTitle(String.format("%s Family Other Adults", currFam.getHOHLastName()));
		}
		else
			this.setTitle(String.format("ONC #%s Family Other Adults", currFam.getONCNum()));
			
		dlgTableModel.fireTableDataChanged();	//build the table with the current family
	}
	
	void add()
	{
    	AddAdultDialog addDlg = new AddAdultDialog(owner);
    	addDlg.setLocationRelativeTo(this);
    	if(adultDB != null && addDlg.showDialog())
    	{
    		ONCAdult addedAdult =  (ONCAdult) adultDB.add(this, addDlg.getAddAdultReq());
    		
			if(addedAdult != null)
			{
				tableList.add(addedAdult);
	    		int tableRow = tableList.size() - 1;
				dlgTable.clearSelection();
				dlgTableModel.fireTableDataChanged();
				dlgTable.scrollRectToVisible(dlgTable.getCellRect(tableRow, 0, true));
				dlgTable.setRowSelectionInterval(tableRow, tableRow);
			}
			else
			{
				String err_mssg = "Add adult request failed, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add Adult Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
			}
    	}		
	}
	
	void delete()	//delete adult in family 
	{
		int viewrow = dlgTable.getSelectedRow();
		int modelrow = dlgTable.convertRowIndexToModel(viewrow);
		
		if(modelrow > -1)
		{
			ONCAdult reqDeleteAdult = new ONCAdult(tableList.get(modelrow));
			
			//Confirm with the user that the deletion is really intended
			String confirmMssg =String.format("Are you sure you want to delete %s from the data base?", 
											reqDeleteAdult.getName());
		
			Object[] options= {"Cancel", "Delete"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE,
													JOptionPane.YES_NO_OPTION,
													GlobalVariables.getONCLogo(), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(owner, "*** Confirm Adult Database Deletion ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete"))
			{
				//notify the server of the delete request.
				ONCAdult deletedAdult = adultDB.delete(this, reqDeleteAdult);
				if(deletedAdult != null)
				{
					//delete the adult from the table and display
					tableList.remove(modelrow);
					dlgTableModel.fireTableDataChanged();
				}
				else
				{
					//request failed
					String err_mssg = "ONC Server denied delete request, try again later";
					JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Delete Adult Failure",
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
			String err_mssg = "Unable to print adult listing: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Adults Error",
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		}
	}

	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_ADULT") ||
				dbe.getType().equals("UPDATED_ADULT") || dbe.getType().equals("DELETED_ADULT")))
			
		{
			//is the adult in the current family being displayed? If so, update the table
			ONCAdult changedAdult = (ONCAdult) dbe.getObject1();
			
			if(currFam != null && changedAdult.getFamID() == currFam.getID())
			{
				//update the table list and the user table
				tableList = adultDB.getAdultsInFamily(currFam.getID());
				dlgTableModel.fireTableDataChanged();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
						dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1 && userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			btnDelete.setEnabled(true);
		
		else
			btnDelete.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnAdd)
		{
			add();
		}
		else if(e.getSource() == btnDelete)
		{
			delete();
		}
		else if(e.getSource() == btnPrint)
		{
			print(String.format("ONC #%s Adult Listing", currFam.getONCNum()));
		}		
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			display((ONCFamily) tse.getObject1());
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Adult Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"ONC #", "Name", "Gender"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return tableList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCAdult adult = tableList.get(row);
        	if(col == ONC_NUM_COL)  
        		return currFam.getONCNum();
        	else if(col == NAME_COL)
        		return userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 ? adult.getName() : String.format("Adult %d", row+1);
        	else if (col == GENDER_COL)
        		return adult.getGender();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == GENDER_COL)
        		return AdultGender.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	//Name, Status, Access and Permission are editable
        	if(col > ONC_NUM_COL && userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
        		return true;
        	else
        		return false;
        }

        public void setValueAt(Object value, int row, int col)
        { 
        	ONCAdult selectedAdult = tableList.get(row);
        	ONCAdult reqUpdateAdult = null;
        	
        	//determine if the user made a change to a user object
        	if(col == NAME_COL && !selectedAdult.getName().equals((String)value))
        	{
        		reqUpdateAdult = new ONCAdult(selectedAdult);	//make a copy
        		reqUpdateAdult.setName((String) value);
        	}
        	else if(col == GENDER_COL && selectedAdult.getGender() != (AdultGender) value)
        	{
        		reqUpdateAdult = new ONCAdult(selectedAdult);	//make a copy
        		reqUpdateAdult.setGender((AdultGender) value);
        	}
        	
        	//if the user made a change in the table, attempt to update the adult object in
        	//the local adult data base
        	if(reqUpdateAdult != null)
        	{
        		String response = adultDB.update(this, reqUpdateAdult);        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_ADULT")))
        		{
        			//request failed
        			String err_mssg = "ONC Server denied update adult request, try again later";
        			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Update Adult Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
        		}
        	}
        }  
    }
	
	public class AddAdultDialog extends InfoDialog
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private static final int NAME_INDEX = 0;
		private static final int GENDER_INDEX = 1;
		
		private JComboBox genderCB;
		private ONCAdult reqAddAdult;	
		
		AddAdultDialog(JFrame pf)
		{
			super(pf, true);
			this.setTitle("Add New Adult");

			lblONCIcon.setText("<html><font color=blue>Add New Adult<br>Information Below</font></html>");

			//Set up the main panel, loop to set up components associated with names
			for(int pn=0; pn < getDialogFieldNames().length; pn++)
			{
				tf[pn] = new JTextField(14);
				tf[pn].addKeyListener(tfkl);
				infopanel[pn].add(tf[pn]);
			}
			
			//set up the transformation panel
			genderCB = new JComboBox(AdultGender.values());
			genderCB.setPreferredSize(new Dimension(158,36));
			infopanel[GENDER_INDEX].remove(tf[GENDER_INDEX]);
			infopanel[GENDER_INDEX].add(genderCB);

			//add text to action button
			btnAction.setText("Add Adult");
					
			pack();
		}
		
		ONCAdult getAddAdultReq() { return reqAddAdult; }
		
		@Override
		void update()
		{	
			reqAddAdult = new ONCAdult(-1, currFam.getID(), tf[NAME_INDEX].getText(), 
					(AdultGender) genderCB.getSelectedItem());
			
			result = true;
			dispose();
		}

		@Override
		boolean fieldUnchanged() 
		{
			return tf[NAME_INDEX].getText().isEmpty();
		}

		@Override
		void delete() {
			// TODO Auto-generated method stub
			
		}

		@Override
		void display(ONCObject obj) {
			// TODO Auto-generated method stub
			
		}

		@Override
		String[] getDialogFieldNames()
		{
			return new String[] {"Name", "Gender"};
		}
	}
}
