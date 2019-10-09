package ourneighborschild;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.toedter.calendar.JDateChooser;

public class FamilyHistoryChangesDialog extends ONCTableDialog implements PropertyChangeListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int FAMILY_ONC_NUM_COL = 0;
	private static final int FAMILY_STATUS_COL = 1;
	private static final int GIFT_STATUS_COL = 2;
	private static final int DELIVERED_BY_COL = 3;
	private static final int NOTES_COL = 4;
	private static final int CHANGED_BY_COL = 5;
	private static final int DATE_CHANGED_COL = 6;
	
	private FamilyHistoryDB famHistDB;
	private FamilyDB famDB;
	private VolunteerDB volunteerDB;
	private UserDB userDB;
	
	private List<ONCFamilyHistory> histList;
	
	private JTextField oncnumTF;
	private JComboBox<FamilyStatus> fstatusCB;
	private JComboBox<FamilyGiftStatus> giftStatusCB;
	private JComboBox<String> notesCB, changedByCB;
	private DefaultComboBoxModel<String> changedByCBM;
	private JDateChooser ds, de;
	
	private String sortONCNum;
	private FamilyGiftStatus sortGiftStatus;
	private FamilyStatus sortFamilyStatus;
	private int sortChangedBy;
	private String sortNotes;
	private Calendar sortStartCal = null, sortEndCal = null;
	
	private String[] notes = {"Any", "Status Changed", "Family Referred", "Goft Status Change",
			"Automated Call Result: Contacted", "Automated Call Result: Confirmed",
			"Delivery Driver Assigned", "Agent updated family info"};
	
	public FamilyHistoryChangesDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Our Neighbor's Child - Family Histories");
		
		famHistDB = FamilyHistoryDB.getInstance();
		if(famHistDB != null)
			famHistDB.addDatabaseListener(this);
		
		famDB = FamilyDB.getInstance();
		if(famDB != null)
			famDB.addDatabaseListener(this);
		
		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		if(gvs != null)
			gvs.addDatabaseListener(this);
		
		histList = new ArrayList<ONCFamilyHistory>();
		
		//Set up the search criteria panel      
		oncnumTF = new JTextField(5);
		sortONCNum = "";
		oncnumTF.setEditable(true);
		oncnumTF.setMaximumSize(new Dimension(64,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
		
		fstatusCB = new JComboBox<FamilyStatus>(FamilyStatus.getSearchFilterList());
		sortFamilyStatus = FamilyStatus.Any;
		fstatusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
		fstatusCB.addActionListener(this);
		
		giftStatusCB = new JComboBox<FamilyGiftStatus>(FamilyGiftStatus.getSearchFilterList());
		sortGiftStatus = FamilyGiftStatus.Any;
		giftStatusCB.setPreferredSize(new Dimension(160, 56));
		giftStatusCB.setBorder(BorderFactory.createTitledBorder("Gift Status"));
		giftStatusCB.addActionListener(this);
		
		changedByCB = new JComboBox<String>();
		sortChangedBy = 0;
		changedByCBM = new DefaultComboBoxModel<String>();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setPreferredSize(new Dimension(144, 56));
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.addActionListener(this);
		
		notesCB = new JComboBox<String>(notes);
		sortNotes = "Any";
		notesCB.setEditable(true);
	    notesCB.setPreferredSize(new Dimension(240, 56));
	    notesCB.setBorder(BorderFactory.createTitledBorder("Notes"));
	    notesCB.addActionListener(this);
		
		sortStartCal = Calendar.getInstance();
		sortStartCal.setTime(gvs.getSeasonStartDate());
		
		ds = new JDateChooser(sortStartCal.getTime());
		ds.setPreferredSize(new Dimension(156, 56));
		ds.setBorder(BorderFactory.createTitledBorder("Created On/After"));
		ds.getDateEditor().addPropertyChangeListener(this);
		
		sortEndCal = Calendar.getInstance();
		sortEndCal.setTime(gvs.getTodaysDate());
		sortEndCal.add(Calendar.DATE, 1);
		
		de = new JDateChooser(sortEndCal.getTime());
		de.setPreferredSize(new Dimension(156, 56));
		de.setBorder(BorderFactory.createTitledBorder("Created Before"));
		de.getDateEditor().addPropertyChangeListener(this);
		
		searchCriteriaPanelTop.add(oncnumTF);
		searchCriteriaPanelTop.add(fstatusCB);
		searchCriteriaPanelTop.add(giftStatusCB);
		searchCriteriaPanelTop.add(notesCB);
		searchCriteriaPanelBottom.add(changedByCB);
		searchCriteriaPanelBottom.add(ds);
		searchCriteriaPanelBottom.add(de);
		
		//set up a cell renderer for the time stamp column to display the date 
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			SimpleDateFormat f = new SimpleDateFormat("M/dd/yy h:mm a");

			public Component getTableCellRendererComponent(JTable table, Object value,
				          		boolean isSelected, boolean hasFocus, int row, int column)
			{ 
				if(value instanceof java.util.Date)
					value = f.format(value);
				        
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		};
		dlgTable.getColumnModel().getColumn(DATE_CHANGED_COL).setCellRenderer(tableCellRenderer);
	}
	
	void buildTableList()
	{
		histList.clear();	//Clear the prior table information in the array list
		for(ONCFamilyHistory fh : famHistDB.getList())
		{
			//ONC number is valid and matches criteria. It must be a served family and the
			//only allowable DNS code is WISH_ANTICIPATION.
			if(doesONCNumMatch(famDB.getFamily(fh.getFamID()).getONCNum()) &&
				doesFStatusMatch(fh.getFamilyStatus()) &&
				 doesDStatusMatch(fh.getGiftStatus()) &&
				  doesChangedByMatch(fh.getdChangedBy()) &&
				   doesNotesMatch(fh.getdNotes()) &&
				    isChangeDateBetween(fh.getDateChangedCal()))
			{
				histList.add(fh);
			}
		}
		
		setCount(histList.size());
		dlgTableModel.fireTableDataChanged();	//Display the table after table array list is built	
	}
	
	boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	boolean doesFStatusMatch(FamilyStatus fstat) {return sortFamilyStatus == FamilyStatus.Any  || fstat == (FamilyStatus) fstatusCB.getSelectedItem();}
	boolean doesDStatusMatch(FamilyGiftStatus fgs) {return sortGiftStatus == FamilyGiftStatus.Any  || fgs == giftStatusCB.getSelectedItem();}
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
	boolean doesNotesMatch(String notes) { return sortNotes.equals("Any") || notes.equals(notesCB.getSelectedItem()); }
	boolean isChangeDateBetween(Calendar wcd)
	{
		return !wcd.getTime().after(sortEndCal.getTime()) && !wcd.getTime().before(sortStartCal.getTime());
	}
	
	void updateUserList()
	{	
		changedByCB.removeActionListener(this);
		
		String curr_sel = changedByCB.getSelectedItem().toString();
		int selIndex = 0;
		
		changedByCBM.removeAllElements();
		
		changedByCBM.addElement("Anyone");
		
		int index = 0;
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser user : userList)
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.addActionListener(this);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());

		if(modelRow > -1)
		{
			ONCFamily fam = (ONCFamily) famDB.getFamily(histList.get(modelRow).getFamID());
			fireEntitySelected(this, EntityType.FAMILY, fam, null);
		}
	}

	@Override
	int[] leftColumns() { return new int[] {}; }
	
	@Override
	int[] centeredColumns() { return new int[] {}; }
	
	@Override
	int listSelectionModel() { return ListSelectionModel.SINGLE_SELECTION; }

	@Override
	AbstractTableModel createTableModel()
	{
		dlgTableModel = new DialogTableModel();
		return dlgTableModel;
	}

	@Override
	String[] columnToolTips() 
	{
		return new String[] {"ONC Nubmer", "Family Status", "Gift Status", "Who delivered gifts to the family", "Notes", "User who changed the history item", "Time when history item was created"};
	}

	@Override
	int[] columnWidths() { return new int[] {48, 88, 88, 112, 208, 96, 128}; }
	
	@Override
	void resetFilters()
	{
		oncnumTF.setText("");
		sortONCNum = "";
		
		fstatusCB.removeActionListener(this);
		fstatusCB.setSelectedIndex(0);
		sortFamilyStatus = FamilyStatus.Any;
		fstatusCB.addActionListener(this);
		
		giftStatusCB.removeActionListener(this);
		giftStatusCB.setSelectedIndex(0);
		sortGiftStatus = FamilyGiftStatus.Any;
		giftStatusCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		notesCB.removeActionListener(this);
		notesCB.setSelectedIndex(0);
		sortNotes = "Any";
		notesCB.addActionListener(this);
		
		//Check to see if date sort criteria has changed. Since the setDate() method
		//will not trigger an event, must check for a sort criteria date change here
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		if(!sdf.format(sortStartCal.getTime()).equals(sdf.format(gvs.getSeasonStartDate())))
		{
			sortStartCal.setTime(gvs.getSeasonStartDate());
			ds.setDate(sortStartCal.getTime());	//Will not trigger the event handler
		}
				
		if(!sdf.format(sortEndCal.getTime()).equals(sdf.format(getTomorrowsDate())))
		{
			sortEndCal.setTime(getTomorrowsDate());
			de.setDate(sortEndCal.getTime());	//Will not trigger the event handler
		}
		
		buildTableList();
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("LOADED_FAMILY_HISTORY"))
			buildTableList();
		else if(dbe.getType().equals("UPDATED_DELIVERY") || dbe.getType().equals("ADDED_DELIVERY"))
		{
			buildTableList();
		}
		else if(dbe.getType().contains("ADDED_USER") || dbe.getType().contains("UPDATED_USER") || 
				dbe.getType().contains("LOADED_USERS"))
		{
//			ONCUser updatedUser = (ONCUser)dbe.getObject1();
			updateUserList();
		}
		else if(dbe.getType().equals("UPDATED_GLOBALS"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Family Histories", GlobalVariablesDB.getCurrentSeason()));
			
			sortStartCal.setTime(gvs.getSeasonStartDate());
			ds.setDate(sortStartCal.getTime());	//Will not trigger the event handler
			buildTableList();
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF)
		{
			sortONCNum = oncnumTF.getText();
			buildTableList();
		}
		else if(e.getSource() == fstatusCB && fstatusCB.getSelectedItem() != sortFamilyStatus)
		{						
			sortFamilyStatus = (FamilyStatus) fstatusCB.getSelectedItem();
			buildTableList();
		}
		else if(e.getSource() == giftStatusCB && giftStatusCB.getSelectedItem() != sortGiftStatus)
		{						
			sortGiftStatus = (FamilyGiftStatus) giftStatusCB.getSelectedItem();
			buildTableList();
		}
		else if(e.getSource() == changedByCB && changedByCB.getSelectedIndex() != sortChangedBy)
		{
			sortChangedBy = changedByCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == notesCB && !notesCB.getSelectedItem().equals(sortNotes))
		{
			sortNotes = (String) notesCB.getSelectedItem();
			buildTableList();
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent pce)
	{
		//If the date has changed in either date chooser, then rebuild the sort table. Note, setting
		//the date using setDate() does not trigger a property change, only triggered by user action. 
		//So must rebuild the table each time a change is detected. 
		if("date".equals(pce.getPropertyName()) &&
				(!sortStartCal.getTime().equals(ds.getDate()) || !sortEndCal.getTime().equals(de.getDate())))
		{
			sortStartCal.setTime(ds.getDate());
			sortEndCal.setTime(de.getDate());
			buildTableList();
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"ONC #", "Fam Status", "Gift Status", "Gifts Delivered By", "Notes", "Changed By", "Time Stamp"};
		
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return histList == null ? 0 : histList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		Object value;
        	
        		ONCFamilyHistory histObj = histList.get(row);
        	
        		if(col == FAMILY_ONC_NUM_COL)
        			value = famDB.getFamily(histObj.getFamID()).getONCNum();
        		else if(col ==  FAMILY_STATUS_COL)
        			value = histObj.getFamilyStatus().toString();
        		else if(col == GIFT_STATUS_COL)
        			value = histObj.getGiftStatus().toString();
        		else if(col == DELIVERED_BY_COL)  
        			value = volunteerDB.getDriverLNFN(histObj.getdDelBy());
        		else if(col == NOTES_COL)
        			value = histObj.getdNotes();
        		else if(col == CHANGED_BY_COL)
        			value = histObj.getdChangedBy();
        		else if (col == DATE_CHANGED_COL)
        			value = histObj.getDateChanged();
        		else
        			value = "Error";
        	
         	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == DATE_CHANGED_COL)
        			return Date.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
    }
	/***********************************************************************************
	 * This class implements a key listener for the ReceiveGiftDialog class that
	 * listens to the ONC Number text field to determine when it is empty. If it becomes empty,
	 * the listener rebuilds the sort table array list
	 ***********************************************************************************/
	 protected class ONCNumberKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}
		@Override
		public void keyReleased(KeyEvent arg0)
		{
			if(oncnumTF.getText().isEmpty())
			{
				sortONCNum = "";
				buildTableList();
			}		
		}
		@Override
		public void keyTyped(KeyEvent arg0)
		{
			
		}
	 }
	
}
