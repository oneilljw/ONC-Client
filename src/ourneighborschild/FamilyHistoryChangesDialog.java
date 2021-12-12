package ourneighborschild;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.toedter.calendar.JDateChooser;

import au.com.bytecode.opencsv.CSVWriter;

public class FamilyHistoryChangesDialog extends ONCTableDialog implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int FAMILY_ONC_NUM_COL = 0;
	private static final int FAMILY_STATUS_COL = 1;
	private static final int GIFT_STATUS_COL = 2;
	private static final int DNS_CODE_COL = 3;
	private static final int DELIVERED_BY_COL = 4;
	private static final int NOTES_COL = 5;
	private static final int CHANGED_BY_COL = 6;
	private static final int DATE_CHANGED_COL = 7;
	
	private FamilyHistoryDB famHistDB;
	private FamilyDB famDB;
	private VolunteerDB volunteerDB;
	private UserDB userDB;
	private DNSCodeDB dnsCodeDB;
	
	private List<FamilyHistory> histList;
	
	private JTextField oncnumTF;
	private JComboBox<FamilyStatus> fstatusCB;
	private JComboBox<FamilyGiftStatus> giftStatusCB;
	private JComboBox<DNSCode> dnsCodeCB;
	private DefaultComboBoxModel<DNSCode> dnsCodeCBM;
	private JComboBox<String> notesCB, changedByCB;
	private DefaultComboBoxModel<String> changedByCBM;
	private JDateChooser ds, de;
	
	private String sortONCNum;
	private FamilyGiftStatus sortGiftStatus;
	private FamilyStatus sortFamilyStatus;
	private DNSCode sortDNSCode;
	private int sortChangedBy;
	private String sortNotes;
	private Calendar startFilterTimestamp, endFilterTimestamp;
	
	private FamilyHistoryTimestampComparator fhTimestampComparator;
	
	private String[] notes = {"Any", "Status Changed", "Family Referred", "Gift Status Change",
			"Automated Call Result: Contacted", "Automated Call Result: Confirmed",
			"Delivery Driver Assigned", "Agent updated family info", "Delivery confirmed by barcode scan"};
	
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
		
		dnsCodeDB = DNSCodeDB.getInstance();
		if(dnsCodeDB != null)
			dnsCodeDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		if(gvs != null)
			gvs.addDatabaseListener(this);
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		histList = new ArrayList<FamilyHistory>();
		fhTimestampComparator = new FamilyHistoryTimestampComparator(); 
		
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
		
		sortDNSCode = new DNSCode(-2, "Any",  "Any", "Any",false);
		dnsCodeCBM = new DefaultComboBoxModel<DNSCode>();
		dnsCodeCBM.addElement(sortDNSCode);
		dnsCodeCB = new JComboBox<DNSCode>();
		dnsCodeCB.setModel(dnsCodeCBM);
		dnsCodeCB.setPreferredSize(new Dimension(160, 56));
		dnsCodeCB.setBorder(BorderFactory.createTitledBorder("DNS Code"));
		dnsCodeCB.addActionListener(this);
		
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
		
	    startFilterTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		ds = new JDateChooser(startFilterTimestamp.getTime());
		ds.setPreferredSize(new Dimension(156, 56));
		ds.setBorder(BorderFactory.createTitledBorder("Changed On/After"));
		ds.getDateEditor().addPropertyChangeListener(this);
		
		endFilterTimestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		de = new JDateChooser(endFilterTimestamp.getTime());
		de.setPreferredSize(new Dimension(156, 56));
		de.setBorder(BorderFactory.createTitledBorder("Changed On/Before"));
		de.getDateEditor().addPropertyChangeListener(this);
		
		searchCriteriaPanelTop.add(oncnumTF);
		searchCriteriaPanelTop.add(fstatusCB);
		searchCriteriaPanelTop.add(giftStatusCB);
		searchCriteriaPanelTop.add(dnsCodeCB);
		searchCriteriaPanelBottom.add(notesCB);
		searchCriteriaPanelBottom.add(changedByCB);
		searchCriteriaPanelBottom.add(ds);
		searchCriteriaPanelBottom.add(de);
		
		//set up a cell renderer for the time stamp column to display the date 
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			DateFormat f = new SimpleDateFormat("M/dd/yy h:mm a");

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
	
	String[] exportChoices() { return new String[] {"Export", "Export Histories" }; }
	
	void buildTableList()
	{
		histList.clear();	//Clear the prior table information in the array list
		for(FamilyHistory fh : famHistDB.getList())
		{
			//ONC number is valid and matches criteria. It must be a served family and the
			//only allowable DNS code is WISH_ANTICIPATION.
			if(doesONCNumMatch(famDB.getFamily(fh.getFamID()).getONCNum()) &&
				doesFStatusMatch(fh.getFamilyStatus()) &&
				 doesDStatusMatch(fh.getGiftStatus()) &&
				  doesDNSCodeMatch(fh.getDNSCode()) &&
				  doesChangedByMatch(fh.getChangedBy()) &&
				   doesNotesMatch(fh.getNotes()) &&
				    isChangeDateBetween(fh.getTimestamp()))
			{
				histList.add(fh);
			}
		}
		
		Collections.sort(histList, fhTimestampComparator);
		
		setCount(histList.size());
		dlgTableModel.fireTableDataChanged();	//Display the table after table array list is built	
	}
	
	boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	boolean doesFStatusMatch(FamilyStatus fstat) {return sortFamilyStatus == FamilyStatus.Any  || fstat == (FamilyStatus) fstatusCB.getSelectedItem();}
	boolean doesDStatusMatch(FamilyGiftStatus fgs) {return sortGiftStatus == FamilyGiftStatus.Any  || fgs == giftStatusCB.getSelectedItem();}
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
	boolean doesNotesMatch(String notes) { return sortNotes.equals("Any") || notes.equals(notesCB.getSelectedItem()); }
	boolean doesDNSCodeMatch(int dnsCodeID) { return sortDNSCode.getID() == -2 || sortDNSCode.getID() == dnsCodeID; }
	boolean isChangeDateBetween(Long timestamp)
	{ 
		return timestamp >= startFilterTimestamp.getTimeInMillis() && timestamp <= endFilterTimestamp.getTimeInMillis();
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
	private void setDateFilters(Calendar start, Calendar end, int startOffset, int endOffset)
	{
		startFilterTimestamp.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
		startFilterTimestamp.set(Calendar.HOUR_OF_DAY, 0);
		startFilterTimestamp.set(Calendar.MINUTE, 0);
		startFilterTimestamp.set(Calendar.SECOND, 0);
		startFilterTimestamp.set(Calendar.MILLISECOND, 0);
		
		endFilterTimestamp.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH));
		endFilterTimestamp.set(Calendar.HOUR_OF_DAY, 0);
		endFilterTimestamp.set(Calendar.MINUTE, 0);
		endFilterTimestamp.set(Calendar.SECOND, 0);
		endFilterTimestamp.set(Calendar.MILLISECOND, 0);
		
		startFilterTimestamp.add(Calendar.DAY_OF_YEAR, startOffset);
		endFilterTimestamp.add(Calendar.DAY_OF_YEAR, endOffset);
	}	
	@Override
	void onExport()
	{
		//Write the selected row data to a .csv file
		String[] header = {"ONC #", "Family Status", "Gift Status", "DNS Code", "Del By", 
	 						"Notes", "Changed By", "Time Stamp"};
			
		ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of selected family histories" ,
       								new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
       			String[] cell = new String[header.length];
       			DateFormat df = new SimpleDateFormat("M/dd/yy h:mm a");
       			for(int i=0; i< dlgTable.getSelectedRowCount(); i++)
       			{
	    			int index = row_sel[i];
	    			int modelRow = index == -1 ? -1 : dlgTable.convertRowIndexToModel(index);

	    			if(modelRow > -1 && modelRow < histList.size())
	    			{
	    				FamilyHistory fh = histList.get(modelRow);
	    				ONCFamily f = famDB.getFamily(fh.getFamID());
	    				
	    				cell[0] = f.getONCNum();
	    				cell[1] = fh.getFamilyStatus().toString();
	    				cell[2] = fh.getGiftStatus().toString();
	    				if(fh.getDNSCode() == -1)
	        				cell[3] = "";
	        			else
	        			{
	        				DNSCode dnsCode = dnsCodeDB.getDNSCode(fh.getDNSCode());
	        				cell[3] = dnsCode == null ? "DNS Error" : dnsCode.getAcronym();
	        			}        
	    				
	    				cell[4] = volunteerDB.getDriverLNFN(fh.getdDelBy());
	    				cell[5] = fh.getNotes();
	    				cell[6] = fh.getChangedBy();
	    				cell[7] =  df.format(new Date(fh.getTimestamp()));

	    				writer.writeNext(cell);
	    			}
       			}
	    	   
       			writer.close();
	    	    
       			JOptionPane.showMessageDialog(this, 
						dlgTable.getSelectedRowCount() + " histories sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
       		} 
       		catch (IOException x)
       		{
       			JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
       			System.err.format("IOException: %s%n", x);
       		}
	    }
       	
       	exportCB.setSelectedIndex(0);
	}	
	
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());

		if(modelRow > -1)
		{
			ONCFamily fam = (ONCFamily) famDB.getFamily(histList.get(modelRow).getFamID());
			fireEntitySelected(this, EntityType.FAMILY, fam, histList.get(modelRow), null);
		}
		
		exportCB.setEnabled(dlgTable.getSelectedRowCount() > 0);
	}

	@Override
	int[] leftColumns() { return new int[] {}; }
	
	@Override
	int[] centeredColumns() { return new int[] {}; }
	
	@Override
	int listSelectionModel() { return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION; }

	@Override
	AbstractTableModel createTableModel()
	{
		dlgTableModel = new DialogTableModel();
		return dlgTableModel;
	}

	@Override
	String[] columnToolTips() 
	{
		return new String[] {"ONC Nubmer", "Family Status", "Gift Status", "Family DNS Code", 
							"Who delivered gifts to the family", "Notes", "User who changed the history item", 
							"Time when history item was created"};
	}

	@Override
	int[] columnWidths() { return new int[] {48, 88, 88, 64, 112, 292, 96, 128}; }
	
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
		
		dnsCodeCB.removeActionListener(this);
		dnsCodeCB.setSelectedIndex(0);
		sortDNSCode = new DNSCode(-2, "Any", "Any", "Any",false);
		dnsCodeCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		notesCB.removeActionListener(this);
		notesCB.setSelectedIndex(0);
		sortNotes = "Any";
		notesCB.addActionListener(this);
		
		updateDateFilters();
		
		buildTableList();
	}
	
	@SuppressWarnings("unchecked")
	void updateDNSCodeCB()
	{
		dnsCodeCB.removeActionListener(this);
		
		DNSCode currCode = (DNSCode) dnsCodeCB.getSelectedItem();
		
		dnsCodeCBM.removeAllElements();	//Clear the combo box selection list
		dnsCodeCBM.addElement(new DNSCode(-2, "Any",  "Any", "Any", false));
		
		for(DNSCode code: (List<DNSCode>) dnsCodeDB.getList())	//Add new list elements
			dnsCodeCBM.addElement(code);
			
		//set the proper selection in the updated combo box
		if(currCode != null)
			dnsCodeCB.setSelectedItem(currCode);
		
		dnsCodeCB.addActionListener(this);
	}
	
	void updateDateFilters()
	{
		de.getDateEditor().removePropertyChangeListener(this);
		ds.getDateEditor().removePropertyChangeListener(this);
		
		setDateFilters(gvs.getSeasonStartCal(), Calendar.getInstance(TimeZone.getTimeZone("UTC")), 0, 1);
		ds.setCalendar(startFilterTimestamp);
		de.setCalendar(endFilterTimestamp);
		
		ds.getDateEditor().addPropertyChangeListener(this);
		de.getDateEditor().addPropertyChangeListener(this);
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Family Histories", gvs.getCurrentSeason()));
			updateUserList();
			updateDateFilters();
			updateDNSCodeCB();
			buildTableList();
		}
		else if(dbe.getType().equals("UPDATED_DELIVERY") || dbe.getType().equals("ADDED_DELIVERY"))
		{
			buildTableList();
		}
		else if(dbe.getType().equals("UPDATED_DNSCODE")|| dbe.getType().equals("ADDED_DNSCODE"))
		{
			updateDNSCodeCB();
			buildTableList();
		}
		else if(dbe.getType().contains("ADDED_USER") || dbe.getType().contains("UPDATED_USER"))
		{
			updateUserList();
		}
		else if(dbe.getType().equals("UPDATED_GLOBALS"))
		{
			updateDateFilters();
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
		else if(e.getSource() == dnsCodeCB && !((DNSCode)dnsCodeCB.getSelectedItem()).equals(sortDNSCode))
		{						
			sortDNSCode = (DNSCode)dnsCodeCB.getSelectedItem();
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
			(!startFilterTimestamp.getTime().equals(ds.getDate()) || !endFilterTimestamp.getTime().equals(de.getDate())))
		{
			setDateFilters(ds.getCalendar(), de.getCalendar(), 0, 1);
			buildTableList();
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"ONC #", "Fam Status", "Gift Status", "DNS Code", "Gifts Delivered By", 
										"Notes", "Changed By", "Time Stamp"};
		
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return histList == null ? 0 : histList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		Object value;
        	
        		FamilyHistory histObj = histList.get(row);
        	
        		if(col == FAMILY_ONC_NUM_COL)
        			value = famDB.getFamily(histObj.getFamID()).getONCNum();
        		else if(col ==  FAMILY_STATUS_COL)
        			value = histObj.getFamilyStatus().toString();
        		else if(col == GIFT_STATUS_COL)
        			value = histObj.getGiftStatus().toString();
        		else if(col == DNS_CODE_COL)
        		{
        			if(histObj.getDNSCode() == -1)
        				return "";
        			else
        			{
        				DNSCode dnsCode = dnsCodeDB.getDNSCode(histObj.getDNSCode());
        				if(dnsCode == null)
        					return "DNS Error";
        				else
        					return dnsCode.getAcronym();
        			}        			
        		}
        		else if(col == DELIVERED_BY_COL)  
        			value = volunteerDB.getDriverLNFN(histObj.getdDelBy());
        		else if(col == NOTES_COL)
        			value = histObj.getNotes();
        		else if(col == CHANGED_BY_COL)
        			value = histObj.getChangedBy();
        		else if (col == DATE_CHANGED_COL)
        			value = new Date(histObj.getTimestamp());
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
	 
	private class FamilyHistoryTimestampComparator implements Comparator<FamilyHistory>
	{
		public int compare(FamilyHistory o1, FamilyHistory o2)
		{			
			if(o1.getTimestamp() > o2.getTimestamp())
				return -1;
			else if(o1.getTimestamp() < o2.getTimestamp())
				return 1;
			else
				return 0;
		}
	}
}
