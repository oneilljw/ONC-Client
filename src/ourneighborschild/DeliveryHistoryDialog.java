package ourneighborschild;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import com.google.gson.Gson;

public class DeliveryHistoryDialog extends HistoryDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int STATUS_COL= 0;
	private static final int DELIVERED_BY_COL = 1;
	private static final int NOTES_COL = 2;
	private static final int CHANGED_BY_COL = 3;
	private static final int DATE_CHANGED_COL = 4;
	private static final String[] dstat = {"Empty", "Contacted", "Confirmed", "Assigned", "Attempted",
							  			   "Returned", "Delivered", "Counselor Pick-Up"};

	private AbstractTableModel dlgTableModel;
	
	private DeliveryDB deliveryDB;
	private VolunteerDB volunteerDB;
	
	private List<ONCDelivery> delList;
	
	public DeliveryHistoryDialog(JFrame pf) 
	{
		super(pf, "Delivery");
		// TODO Auto-generated constructor stub
		
		volunteerDB = VolunteerDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
		delList = new ArrayList<ONCDelivery>();
	}
	
	@Override
	void display(ONCObject obj) 
	{
		this.currFam = (ONCFamily) obj;
		setDialogTitle();
		delList = getSortedList();
		dlgTableModel.fireTableDataChanged();
	}
	
	List<ONCDelivery> getSortedList()
	{
		List<ONCDelivery> dList = deliveryDB.getDeliveryHistoryAL(currFam.getID());
		Collections.sort(dList, new DeliveryDateChangedComparator());
		
		return dList;
	}

	@Override
	void delete()
	{
		// TODO Auto-generated method stub
	}

	
	/************************************************************************************************
	 * Updates the data base for changes made to family delivery data in the table. Currently, only
	 * delivery notes can be modified from the table. To update, method creates a copy of the
	 * current delivery object with the user requested notes field change and sends it to the
	 * local delivery data base. If an affirmative change response is received, the method updates
	 * the list that holds table data. 
	 * @param row
	 * @param col
	 ***********************************************************************************************/
	void updateFamilyDeliveryData(String notes)
	{
		//If it exists, get the ONC Delivery object and compare it to the data in the cell that changed
		//Store new data back into the sub database as necessary and indicate the data base changed				      
		ONCDelivery updateDelReq = new ONCDelivery(delList.get(0));	//make a copy 
			
		//Update the notes and changed by fields in the request
		updateDelReq.setdNotes(notes);
		updateDelReq.setdChangedBy(userDB.getUserLNFI());
			
		//send the request to the local data base
		String response = deliveryDB.update(this, updateDelReq);	
		if(response.startsWith("UPDATED_DELIVERY"))	//did local data base update?
		{
			Gson gson = new Gson();
			ONCDelivery updatedDel = gson.fromJson(response.substring(16), ONCDelivery.class);
			delList.set(0, updatedDel);
		}
		else
		{
			//display an error message that update request failed
			GlobalVariables gvs = GlobalVariables.getInstance();
			JOptionPane.showMessageDialog(this, "ONC Server denied Delivery Update," +
					"try again later","Delivery Update Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
			
		display(currFam);				
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && this.isVisible() && dbe.getType().equals("UPDATED_DELIVERY") ||
			dbe.getType().equals("ADDED_DELIVERY"))
		{
			ONCDelivery updatedDelivery = (ONCDelivery) dbe.getObject1();
			
			//If updated delivery belongs to family delivery history being displayed,
			//re-display it
			if(currFam != null && currFam.getID() == updatedDelivery.getFamID())
				display(currFam);
		}
		else if(dbe.getSource() != this && this.isVisible() && dbe.getType().equals("ADDED_DRIVER"))
		{
			//driver may have been added to current family delivery history
			if(currFam != null)
				display(currFam);
		}
	}

	@Override
	AbstractTableModel createTableModel()
	{
		dlgTableModel = new DialogTableModel();
		return dlgTableModel;
	}

	@Override
	String[] getColumnToolTips() 
	{
		String[] colTT = {"Status", "Delivered By", "Notes", "Changed By", "Time Stamp"};
		return colTT;
	}

	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {128, 96, 208, 96, 128};
		return colWidths;
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Status", "Delivered By", "Notes", "Changed By", "Time Stamp"};
		private SimpleDateFormat sdf;
		
		public DialogTableModel()
		{
			sdf = new SimpleDateFormat("M/dd/yy H:mm");
		}

        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return delList == null ? 0 : delList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	Object value;
        	
        	ONCDelivery del = delList.get(row);
        	
        	if(col == STATUS_COL)
        		value = dstat[del.getdStatus()];
        	else if(col == DELIVERED_BY_COL)  
        		value = volunteerDB.getDriverLNFN(del.getdDelBy());
        	else if(col == NOTES_COL)
        		value = del.getdNotes();
        	else if(col == CHANGED_BY_COL)
        		value = del.getdChangedBy();
        	else if(col == DATE_CHANGED_COL)
        		value = sdf.format(del.getdChanged());
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
        	return row == 0 && col == NOTES_COL;
        }

        public void setValueAt(Object value, int row, int col)
        { 
        	//verify user changed the delivery not and if so,
        	//update the delivery and redisplay. Table row 0 always
        	//contains the most recent delivery
        	if(!delList.isEmpty() && row == 0 && col == NOTES_COL)
        	{
        		String notes = (String) value;
        		if(!notes.equals(delList.get(0).getdNotes()))
        			updateFamilyDeliveryData(notes);
        	}
        }
    }
	
	private class DeliveryDateChangedComparator implements Comparator<ONCDelivery>
	{
		@Override
		public int compare(ONCDelivery o1, ONCDelivery o2)
		{
			return o2.getdChanged().compareTo(o1.getdChanged());
		}
	}
}
