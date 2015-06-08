package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import com.google.gson.Gson;

public class DeliveryStatusDialog extends JDialog implements TableModelListener, DatabaseListener,
																EntitySelectionListener
{
	/**
	 * This class displays the history of delivery objects for a family.It allows the user to
	 * change the delivery notes associated with the current delivery object.  
	 */
	private static final long serialVersionUID = 1L;
	private JTable dsTable;
	private DefaultTableModel dsTableModel;
	private boolean bDeliveryDataChanging = false;
//	private boolean bUnsavedDeliveryDataChanges = false;
	private String[] dstat = {"Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
	private DriverDB driverDB;
	private DeliveryDB deliveryDB;
	private ArrayList<ONCDelivery> delTableAL;
	private ONCFamily currFam;	//current family who's delivery history is displayed

	public DeliveryStatusDialog(JFrame parent)								
	{
		super(parent);	
		driverDB = DriverDB.getInstance();
		deliveryDB = DeliveryDB.getInstance();
		
		if(driverDB != null)
			driverDB.addDatabaseListener(this);
		
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
	
		delTableAL = new ArrayList<ONCDelivery>(); 
		
		dsTable = new JTable();
        dsTableModel = new DefaultTableModel(new Object[]{"Status", "Delivered By", "Notes", "Changed By", "Time Stamp"}, 0)
        {
        	private static final long serialVersionUID = 1L;
            @Override
            //All cells can be edited, critical for update to work
            public boolean isCellEditable(int row, int column)
            {
            	if(column == 2)
            		return true;
            	else
            		return false;
            }
        };
        dsTableModel.addTableModelListener(this);
        
        dsTable.setModel(dsTableModel);
        dsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        
        dsTable.getColumnModel().getColumn(0).setPreferredWidth(128);
        dsTable.getColumnModel().getColumn(1).setPreferredWidth(96);
        dsTable.getColumnModel().getColumn(2).setPreferredWidth(208);
        dsTable.getColumnModel().getColumn(3).setPreferredWidth(96);
        dsTable.getColumnModel().getColumn(4).setPreferredWidth(128);

        JTableHeader anHeader = dsTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Set table size to 4 rows     
        dsTable.setSize(dsTable.getRowHeight() * 4, dsTable.getWidth());
        dsTable.setFillsViewportHeight(true);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dsTable);
        dsTable.setPreferredScrollableViewportSize(new Dimension(dsTable.getRowHeight() * 4, dsTable.getWidth()));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
      
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
       
        pack();
        setSize(632, 150);
        setResizable(false);
	}
	
	void displayDeliveryInfo(ONCFamily fam)
	{	
		//save a reference to the current family
		currFam = fam;
		
		//UpdateDialog Title
		this.setTitle("Delivery History for ONC family #" + fam.getONCNum());
		
		//Disable handling of UPDATE table events during non user updates
		bDeliveryDataChanging = true;
		
		//Clear currently displayed status
		delTableAL.clear();
		for(int i = dsTableModel.getRowCount() - 1; i >=0; i--)		
			   dsTableModel.removeRow(i);
		
		//Display data for family
		int  nDeliveries = deliveryDB.getDeliveryHistoryAL(fam.getID()).size()-1;
		for(int i=nDeliveries; i>=0; i--)
		{
			ONCDelivery del = deliveryDB.getDeliveryHistoryAL(fam.getID()).get(i);
			String[] tr = {dstat[del.getdStatus()], driverDB.getDriverLNFN(del.getdDelBy()),
							del.getdNotes(), del.dChangedBy, del.getdChanged()};
			
			dsTableModel.addRow(tr);
			delTableAL.add(del);
		}
		
		bDeliveryDataChanging = false;
		this.validate();
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
	void updateFamilyDeliveryData(int row, int col)
	{
		//If it exists, get the ONC Delivery object and compare it to the data in the cell that changed
		//Store new data back into the sub database as necessary and indicate the data base changed				      
		if(delTableAL.size() > 0 &&
			!delTableAL.get(row).getdNotes().equals(dsTableModel.getValueAt(row, 2).toString())) 
		{
			ONCDelivery updateDelReq = new ONCDelivery(delTableAL.get(row));	//make a copy 
			
			//Update the notes and changed by fields in the request
			updateDelReq.setdNotes(dsTableModel.getValueAt(row, 2).toString());
			updateDelReq.setdChangedBy(GlobalVariables.getInstance().getUserLNFI());
			
			//send the request to the local data base
			String response = deliveryDB.update(this, updateDelReq);	
			if(response.startsWith("UPDATED_DELIVERY"))	//did local data base update?
			{
				Gson gson = new Gson();
				ONCDelivery updatedDel = gson.fromJson(response.substring(16), ONCDelivery.class);
				delTableAL.set(row, updatedDel);
			}
			else
			{
				//display an error message that update request failed
				GlobalVariables gvs = GlobalVariables.getInstance();
				JOptionPane.showMessageDialog(this, "ONC Server denied Delivery Update," +
						"try again later","Delivery Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
			
			displayDeliveryInfo(currFam);
		}				
	}
	
	boolean isDeliveryDataChanging() { return bDeliveryDataChanging; }

	public void ClearDeliveryData()
	{
		setVisible(false);
	}

	@Override
	public void tableChanged(TableModelEvent tme) {
		if(tme.getType() == TableModelEvent.UPDATE && !bDeliveryDataChanging)
		{
			updateFamilyDeliveryData(tme.getFirstRow(), tme.getColumn());
		}	
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && this.isVisible() && dbe.getType().equals("UPDATED_DELIVERY") ||
			dbe.getType().equals("ADDED_DELIVERY"))
		{
			ONCDelivery updatedDelivery = (ONCDelivery) dbe.getObject();
			
			//If updated delivery belongs to family delivery history being displayed,
			//re-display it
			if(currFam != null && currFam.getID() == updatedDelivery.getFamID())
				displayDeliveryInfo(currFam);
		}
		else if(dbe.getSource() != this && this.isVisible() && dbe.getType().equals("ADDED_DRIVER"))
		{
			//driver may have been added to current family delivery history
			if(currFam != null)
				displayDeliveryInfo(currFam);
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && (tse.getType().equals("FAMILY_SELECTED") || 
				tse.getType().equals("WISH_SELECTED")))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			if(fam != null)
			{
				displayDeliveryInfo(fam);	
			}	
		}
	}
}
