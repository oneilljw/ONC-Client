package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.TableColumn;

public class ONCUserDialog extends JDialog implements ActionListener, ListSelectionListener,
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
	private static final int LAST_NAME_COL= 0;
	private static final int FIRST_NAME_COL = 1;
	private static final int PERMISSION_COL = 2;
	private static final int LOGINS_COL = 3;
	private static final int LAST_LOGIN_COL = 4;
	private static final int RESET_PW_COL = 5;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnAdd, btnEdit, btnResetPW, btnPrint;
	private UserDB userDB;
		
	public ONCUserDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("ONC Application Elves");
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		
		//Create the catalog table model
		dlgTableModel = new DialogTableModel();
		
		//create the catalog table
		String[] colToolTips = {"Last Name",
				"First Name",
				"Permission"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		TableColumn permColumn = dlgTable.getColumnModel().getColumn(PERMISSION_COL);
		JComboBox comboBox = new JComboBox(UserPermission.values());
		permColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {128, 96, 96, 40, 144, 24};
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
        
        //left justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        dlgTable.getColumnModel().getColumn(LOGINS_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        
        btnPrint = new JButton("Print Users");
        btnPrint.setToolTipText("Print the user list");
        btnPrint.addActionListener(this);
        
        btnAdd = new JButton("Add User");
        btnAdd.setToolTipText("Add a new user");
        btnAdd.addActionListener(this);
        
        btnEdit = new JButton("Edit Addl Info");
        btnEdit.setToolTipText("Edit users contact info");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);
        
        btnResetPW = new JButton("Reset Password");
        btnResetPW.setToolTipText("Reset the selected user's password");
        btnResetPW.setEnabled(false);
        btnResetPW.addActionListener(this);
          
        cntlPanel.add(btnAdd);
        cntlPanel.add(btnEdit);
        cntlPanel.add(btnResetPW);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
        this.setMinimumSize(new Dimension(tablewidth, 240));
	}
	
	void add()
	{
		String[] fieldNames = {"First Name", "Last Name", "User ID", "Permission"};
    	AddUserDialog auDlg = new AddUserDialog(GlobalVariables.getFrame(), fieldNames);
    	auDlg.setLocationRelativeTo(this);
    	if(userDB != null && auDlg.showDialog())
    	{
    		int tableRow = userDB.add(this, auDlg.getAddUserReq());
			if(tableRow > -1)
			{
				dlgTable.clearSelection();
				dlgTableModel.fireTableDataChanged();
				dlgTable.scrollRectToVisible(dlgTable.getCellRect(tableRow, 0, true));
				dlgTable.setRowSelectionInterval(tableRow, tableRow);
			}
			else
			{
				String err_mssg = "Add user request failed, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add User Request Failure",
											JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
			}
    	}		
	}
	
	void edit()
	{
		
	}

	void delete()	//is really reset password 
	{
		int viewrow = dlgTable.getSelectedRow();
		int modelrow = dlgTable.convertRowIndexToModel(viewrow);
		
		if(modelrow > -1)
		{
			ONCUser reqUpdateUser = new ONCUser(userDB.getUserFromIndex(modelrow));
			reqUpdateUser.bResetPassword = true;
			
			//notify the server of the update. When the server sees the reset password
			//flag set, it will reset the password to a pre-selected password
			String response = userDB.update(this, reqUpdateUser);
			if(response != null && response.startsWith("UPDATED_USER"))
			{
				String mssg = String.format("Password reset for %s %s",
						reqUpdateUser.getFirstname(), reqUpdateUser.getLastname()); 
    			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg, "Paswword Reset",
								JOptionPane.INFORMATION_MESSAGE, GlobalVariables.getONCLogo());
			}
			else
    		{
    			//request failed
    			String err_mssg = "ONC Server denied reset password request, try again later";
    			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Reset Password Request Failure",
								JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
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
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_USER") ||
				dbe.getType().equals("UPDATED_USER")))
		{
			ONCUser addedUser = (ONCUser) dbe.getObject();
			int tablerow = userDB.findModelIndexFromID(addedUser.getID());
			
			if(tablerow > -1)
			{
				//update the user table
				dlgTableModel.fireTableDataChanged();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
						dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1)
		{
//			btnEdit.setEnabled(true);	//not implemented yet
			btnResetPW.setEnabled(true);
		}
		else
		{
			btnEdit.setEnabled(false);
			btnResetPW.setEnabled(false);
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
		else if(e.getSource() == btnResetPW)
		{
			delete();
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC User List");
		}		
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Last Name", "First Name", "Permission",
				"Logins", "Last Login", "PW?"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return userDB.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCUser user = userDB.getUserFromIndex(row);
        	if(col == FIRST_NAME_COL)  
        		return user.getFirstname();
        	else if(col == LAST_NAME_COL)
        		return user.getLastname();
        	else if (col == PERMISSION_COL)
        		return user.getPermission();
        	else if (col == LOGINS_COL)
        		return user.getNSessions();
        	else if (col == LAST_LOGIN_COL)
        	{
        		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:mm:ss");
        		return sdf.format(user.getLastLogin());
        	}
        	else if (col == RESET_PW_COL)
        	{
        		GlobalVariables gvs = GlobalVariables.getInstance();
        		return user.changePasswordRqrd() ? gvs.getImageIcon(22) : gvs.getImageIcon(21);
        	}
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == PERMISSION_COL)
        		return UserPermission.class;
        	else if(column == LOGINS_COL)
        		return Long.class;
        	else if(column == RESET_PW_COL)
        		return ImageIcon.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
            //Only the check boxes can be edited and then only if there is not
        	//a wish already selected from the list associated with that column
        	if(col == LAST_NAME_COL || col == FIRST_NAME_COL || col == PERMISSION_COL)
        		return true;
        	else
        		return false;
        }

        public void setValueAt(Object value, int row, int col)
        { 
        	ONCUser currUser = userDB.getUserFromIndex(row);
        	ONCUser reqUpdateUser = null;
        	
        	//determine if the user made a change to a user object
        	if(col == LAST_NAME_COL && !currUser.getLastname().equals((String)value))
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setLastname((String) value);
        	}
        	else if(col == FIRST_NAME_COL && !currUser.getFirstname().equals((String) value))
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setFirstname((String) value);
        	}
        	else if(col == PERMISSION_COL && currUser.getPermission() != (UserPermission) value)
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setPermission((UserPermission) value);
        	}
        	
        	//if the user made a change in the table, attempt to update the user object in
        	//the local user data base
        	if(reqUpdateUser != null)
        	{
        		String response = userDB.update(this, reqUpdateUser);        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_USER")))
        		{
        			//request failed
        			String err_mssg = "ONC Server denied update user request, try again later";
        			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Update User Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
        		}
        	}
        }  
    }
}