package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import javax.swing.table.TableColumn;

public class ManageUsersDialog extends JDialog implements ActionListener, ListSelectionListener,
														DatabaseListener
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int LAST_NAME_COL= 0;
	private static final int FIRST_NAME_COL = 1;
	private static final int STATUS_COL = 2;
	private static final int ACCESS_COL = 3;
	private static final int PERMISSION_COL = 4;
	private static final int LOGINS_COL = 5;
	private static final int LAST_LOGIN_COL = 6;
	private static final int RESET_PW_COL = 7;
	
	protected JPanel sortCriteriaPanel;
	
	private JFrame owner;
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnAdd, btnEdit, btnResetPW, btnPrint;
	private UserDB userDB;
		
	public ManageUsersDialog(JFrame pf)
	{
		super(pf);
		this.owner = pf;
		this.setTitle("ONC App & Website Elves");
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//Set up the search criteria panel      
		sortCriteriaPanel = new JPanel();
		sortCriteriaPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//		sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));

						
		//Create the ONC Icon label and add it to the search criteria panel
		JLabel lblONCicon = new JLabel(GlobalVariables.getONCLogo());
		lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		sortCriteriaPanel.add(lblONCicon);
						
//		sortCriteriaPanel.add(sortCriteriaPanelTop);
//		sortCriteriaPanel.add(sortCriteriaPanelBottom);	
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
		
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"Last Name", "First Name", "Status", "Access", "Permission",
				"Logins", "Last Login", "Password Change Required?"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		//set up the columns that use combo box editors for the status, access and permission enums
		TableColumn statusColumn = dlgTable.getColumnModel().getColumn(STATUS_COL);
		statusColumn.setCellEditor(new DefaultCellEditor(new JComboBox(UserStatus.values())));
		
		TableColumn accessColumn = dlgTable.getColumnModel().getColumn(ACCESS_COL);
		accessColumn.setCellEditor(new DefaultCellEditor(new JComboBox(UserAccess.values())));
		
		TableColumn permColumn = dlgTable.getColumnModel().getColumn(PERMISSION_COL);
		permColumn.setCellEditor(new DefaultCellEditor(new JComboBox(UserPermission.values())));
		
		//set up a cell renderer for the LAST_LOGINS column to display the date 
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy H:mm:ss");

		    public Component getTableCellRendererComponent(JTable table, Object value,
		            boolean isSelected, boolean hasFocus, int row, int column)
		    { 
		        if(value instanceof java.util.Date)
		            value = f.format(value);
		        
		        return super.getTableCellRendererComponent(table, value, isSelected,
		                hasFocus, row, column);
		    }
		};
		dlgTable.getColumnModel().getColumn(LAST_LOGIN_COL).setCellRenderer(tableCellRenderer);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {128, 96, 80, 104, 96, 48, 144, 32};
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
        
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the user list");
        btnPrint.addActionListener(this);
        
        btnAdd = new JButton("Add Elf");
        btnAdd.setToolTipText("Add a new user");
        btnAdd.addActionListener(this);
        
        btnEdit = new JButton("Elf Profile");
        btnEdit.setToolTipText("Edit users contact info");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(this);
        
        btnResetPW = new JButton("Reset Elf's Password");
        btnResetPW.setToolTipText("Reset the selected user's password");
        btnResetPW.setEnabled(false);
        btnResetPW.addActionListener(this);
          
        cntlPanel.add(btnAdd);
        cntlPanel.add(btnEdit);
        cntlPanel.add(btnResetPW);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(sortCriteriaPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
        this.setMinimumSize(new Dimension(tablewidth, 240));
	}
	
	void add()
	{
    	AddUserDialog auDlg = new AddUserDialog(GlobalVariables.getFrame());
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
		//determine selected user
		int viewrow = dlgTable.getSelectedRow();
		int modelrow = dlgTable.convertRowIndexToModel(viewrow);
		ONCUser currUser = userDB.getUserFromIndex(modelrow);
		
		//construct and display a UserProfile Dialog
		UserProfileDialog upDlg = new UserProfileDialog(owner, currUser, null);
		upDlg.setLocationRelativeTo(this);
		upDlg.showDialog();
	}

	void delete()	//is really reset password 
	{
		int viewrow = dlgTable.getSelectedRow();
		int modelrow = dlgTable.convertRowIndexToModel(viewrow);
		
		if(modelrow > -1)
		{
			ONCUser reqUpdateUser = new ONCUser(userDB.getUserFromIndex(modelrow));
			reqUpdateUser.setStatus(UserStatus.Change_PW);
			
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
			ONCUser addedUser = (ONCUser) dbe.getObject1();
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
			btnEdit.setEnabled(true);	//not implemented yet
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
		
		private String[] columnNames = {"Last Name", "First Name", "Status", "Access", "Permission",
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
        	else if (col == STATUS_COL)
        		return user.getStatus();
        	else if (col == ACCESS_COL)
        		return user.getAccess();
        	else if (col == PERMISSION_COL)
        		return user.getPermission();
        	else if (col == LOGINS_COL)
        		return user.getNSessions();
        	else if (col == LAST_LOGIN_COL)
        	{
        		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        		calendar.setTimeInMillis(user.getLastLogin());
        		return calendar.getTime();
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
        	if(column == STATUS_COL)
        		return UserStatus.class;
        	if(column == ACCESS_COL)
        		return UserAccess.class;
        	if(column == PERMISSION_COL)
        		return UserPermission.class;
        	else if(column == LOGINS_COL)
        		return Long.class;
        	else if(column == RESET_PW_COL)
        		return ImageIcon.class;
        	else if(column == LAST_LOGIN_COL)
        		return Date.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	//Name, Status, Access and Permission are editable
        	if(col <= PERMISSION_COL)
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
        	else if(col == STATUS_COL && currUser.getStatus() != (UserStatus) value)
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setStatus((UserStatus) value);
        	}
        	else if(col == ACCESS_COL && currUser.getAccess() != (UserAccess) value)
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setAccess((UserAccess) value);
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
