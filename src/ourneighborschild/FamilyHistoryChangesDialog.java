package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

public class FamilyHistoryChangesDialog extends ONCTableDialog 
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
	
	private List<ONCFamilyHistory> histList;
	
	private JTextField oncnumTF;
	
	private String sortONCNum;

	public FamilyHistoryChangesDialog(JFrame parentFrame)
	{
		super(parentFrame);
		
		famHistDB = FamilyHistoryDB.getInstance();
		if(famHistDB != null)
			famHistDB.addDatabaseListener(this);
		
		famDB = FamilyDB.getInstance();
		if(famDB != null)
			famDB.addDatabaseListener(this);
		
		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		histList = new ArrayList<ONCFamilyHistory>();
		
		//Set up the search criteria panel      
		oncnumTF = new JTextField(5);
		oncnumTF.setEditable(true);
		oncnumTF.setMaximumSize(new Dimension(64,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
		
		sortONCNum = "";
	}
	
	void buildTableList()
	{
		histList.clear();	//Clear the prior table information in the array list
		for(ONCFamilyHistory fh : famHistDB.getList())
		{
			//ONC number is valid and matches criteria. It must be a served family and the
			//only allowable DNS code is WISH_ANTICIPATION.
			if(doesONCNumMatch(famDB.getFamily(fh.getFamID()).getONCNum()))	
			{
				histList.add(fh);
			}
		}
		
		dlgTableModel.fireTableDataChanged();	//Display the table after table array list is built	
	}
	
	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		// TODO Auto-generated method stub
		
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
		return new String[] {"ONC Nubmer", "Family Status", "Gift Status", "Who delivered gifts to the family", "Notes", "Changed By", "Time Stamp"};
	}

	@Override
	int[] columnWidths() { return new int[] {48, 88, 88, 112, 208, 96, 128}; }
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"ONC #", "Fam Status", "Gift Status", "Gifts Delivered By", "Notes", "Changed By", "Time Stamp"};
		private SimpleDateFormat sdf;
		
		public DialogTableModel()
		{
			sdf = new SimpleDateFormat("M/dd/yy H:mm");
		}

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
        		else if(col == DATE_CHANGED_COL)
        			value = sdf.format(histObj.getdChanged());
        		else
        			value = "Error";
        	
         	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
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
