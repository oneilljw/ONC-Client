package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class AssignDeliveryDialog extends SortTableDialog 
{
	/**
	 * This class implements a dialog that allows the user to assign delivery drivers to families
	 * that are packaged and ready for delivery. The class extends the ONCFamilyDialog which 
	 * provides the common look and feel for all ONC application dialogs that manage ONCEntity
	 * data. This class extends the common look and feel to add unique search criteria and the
	 * functionality that allows the user to assign a driver for delivery. It also adds a print
	 * listing button to the control panel
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int FAMILY_STATUS_PACKAGED = 5;
	private static final int DELIVERY_STATUS_ASSIGNED = 3;

	private JComboBox sRegCB, eRegCB, dstatusCB;
	private DefaultComboBoxModel regionCBM;
	private JTextField oncnumTF, assignDriverTF;
	private JButton btnPrintListing;
	
	private int sortstartRegion = 0, sortendRegion = 0, sortDStatus=0;
	private String sortONCNum = "";
	
	private static String[] columnToolTips = {"ONC Family Number", "Family Status", "Delivery Status",
										"# of bags packaged", "# of bikes assigned to family",
										"# of large items assigned to family",
										"House Number","Street", "Zip Code", "Region","Changed By",
										"Stoplight Color", "Driver"};
	
	private static String[] columns = {"ONC", "Fam Status", "Del Status", "# Bags", "# Bikes", "# Lg It.", "House",
								"Street", "Zip", "Reg", "Changed By", "SL", "Deliverer"};
	
	private static int[] colWidths = {32, 72, 72, 48, 48, 48, 48, 128, 48, 32, 72, 24, 120};
	
	private static int [] center_cols = {3, 4, 5, 9};

	public AssignDeliveryDialog(JFrame pf)
	{
		super(pf, columnToolTips, columns, colWidths, center_cols);
		this.setTitle("Our Neighbor's Child - Delivery Assignment");
		
		DriverDB driverDB = DriverDB.getInstance();
		if(driverDB != null)
			driverDB.addDatabaseListener(this);
		
		ONCRegions regions = ONCRegions.getInstance();
		if(regions != null)
			regions.addDatabaseListener(this);
		
		//Initialize the sort table array list
		stAL = new ArrayList<ONCFamily>();
				
		//Set up unique sort criteria gui
    	oncnumTF = new JTextField();
    	oncnumTF.setEditable(true);
    	oncnumTF.setPreferredSize(new Dimension(88,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
		regionCBM = new DefaultComboBoxModel();
    	regionCBM.addElement("Any");
    	sRegCB = new JComboBox();
    	sRegCB.setModel(regionCBM);
    	sRegCB.setPreferredSize(new Dimension(112,56));
		sRegCB.setBorder(BorderFactory.createTitledBorder("Region Start"));
		sRegCB.addActionListener(this);
		
		eRegCB = new JComboBox();
		eRegCB.setModel(regionCBM);
    	eRegCB.setPreferredSize(new Dimension(112,56));
		eRegCB.setBorder(BorderFactory.createTitledBorder("Region End"));
		eRegCB.addActionListener(this);
		
		dstatusCB = new JComboBox(delstatus);
		dstatusCB.setPreferredSize(new Dimension(192,56));
		dstatusCB.setBorder(BorderFactory.createTitledBorder("Delivery Status Less Then"));
		dstatusCB.addActionListener(this);
		
		//Add all sort criteria gui to search criteria pane
        sortCriteriaPanelTop.add(oncnumTF);
        sortCriteriaPanelTop.add(sRegCB);
		sortCriteriaPanelTop.add(eRegCB);				
		sortCriteriaPanelTop.add(dstatusCB);
		 
		//Set up change data panel gui
        assignDriverTF = new JTextField(12);
        assignDriverTF.setToolTipText("Type Delivery ID followed by <Enter>");
		assignDriverTF.setBorder(BorderFactory.createTitledBorder("Delivery Driver ID#"));
		assignDriverTF.addKeyListener(new AssignDriverTFKeyListener());

		//Add the change data gui components to the third panel	
		changeDataPanel.add(assignDriverTF);
		changeDataPanel.setPreferredSize(new Dimension(sortTable.getWidth()-300, 90));
        changeDataPanel.setBorder(BorderFactory.createTitledBorder("Assign Delivery Driver"));
        
        //Change the text of the ApplyChanges button
        btnApplyChanges.setText("Assign Delivery");
        
        //Add Print Listing button to the control panel
        btnPrintListing = new JButton("Print Listing");
        btnPrintListing.addActionListener(this);
        
        cntlPanel.add(btnPrintListing);
	}
	
	/**********************************************************************************
	 * This method builds an array list of ONCFamily references that meet criteria for
	 * being assigned for delivery. The first criteria is family status = PACKAGED.
	 * Additional criteria include the ONC Number, the family region and delivery 
	 * status, all selected from the sort criteria panel. 
	 **********************************************************************************/
	public void buildTableList()
	{
		stAL.clear();	//Clear the prior table information in the array list
		
		for(ONCFamily f:fDB.getList())
		{
			//Determine if family meets search criteria to be added to the table. If a
			//family's status isn't FAMILY_STATUS_PACKAGED, they aren't eligible to be
			//assigned for delivery regardless of search criteria
			if(f.getFamilyStatus() == FAMILY_STATUS_PACKAGED &&
				isRegionInRange(f.getRegion()) &&
				 doesONCNumMatch(f.getONCNum()) &&
				  doesDStatusPass(f.getDeliveryStatus()))	
			{													
				stAL.add(f); //Family is eligible for delivery and search criteria pass	
			}
		}
		
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));	//# items in table
		displaySortTable();		//Display the table after table array list is built					
	}
	
	void updateRegionList(String[] regions)
	{
		sRegCB.setEnabled(false);
		eRegCB.setEnabled(false);
		
		String currSelStart = sRegCB.getSelectedItem().toString();
		String currSelEnd = eRegCB.getSelectedItem().toString();
		
		regionCBM.removeAllElements();	//Clear the combo box selection list
		regionCBM.addElement("Any");
		
		for(String s: regions)	//Add new list elements
				regionCBM.addElement(s);
			
		//Reselect the prior region, if it still exists
		sRegCB.setSelectedItem(currSelStart);
		eRegCB.setSelectedItem(currSelEnd);
		
		sRegCB.setEnabled(true);
		eRegCB.setEnabled(true);
	}
	
	/**********************************************************************************
	 * This method builds an array of strings for each row in the family table. It is
	 * called by the super class display table method. 
	 **********************************************************************************/
	protected String[] getTableRow(ONCFamily si)
	{
		String[] deliverytablerow = {si.getONCNum(),
				 famstatus[si.getFamilyStatus() + 1], 
				 delstatus[si.getDeliveryStatus() + 1],
				 Integer.toString(si.getNumOfBags()),
				 Integer.toString(fDB.getNumberOfBikesSelectedForFamily(si)),
				 Integer.toString(si.getNumOfLargeItems()),
				 si.getHouseNum(),
				 si.getStreet(),
				 si.getZipCode(),
				 regions.getRegionID(si.getRegion()),
				 si.getChangedBy(),
				 stoplt[si.getStoplightPos()+1].substring(0, 1), 
				 driverDB.getDriverLNFN(deliveryDB.getDeliveredBy(si.getDeliveryID()))};
		
		return deliverytablerow;
	}

	//Determines if a region is between start and end search criteria
	boolean isRegionInRange(int reg)	
	{
		if(sRegCB.getSelectedIndex() == 0 || eRegCB.getSelectedIndex() == 0)
			return true;
		else
			return reg >= sRegCB.getSelectedIndex()-1 && reg <= eRegCB.getSelectedIndex()-1;
	}
	
	//Determines if family delivery status is less then search criteria
	boolean doesDStatusPass(int dstat) {return sortDStatus == 0  || dstat < dstatusCB.getSelectedIndex()-1;}
	
	//Determines if family ONC number matches search criteria
	boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	/**********************************************************************************************
	 * This method takes row(s) selected by the user and the id of the driver entered by the
	 * user in the Driver ID text field and changes the delivery assignment for each family
	 * selected
	 *********************************************************************************************/
	boolean onApplyChanges()
	{
		bChangingTable = true;
		
		int assignmentsMade = 0;
		int[] row_sel = sortTable.getSelectedRows();

		for(int i=0; i<row_sel.length; i++)
		{
			//Get reference to family object selected
			ONCFamily f = stAL.get(row_sel[i]);
			
			//If a change to the assigned delivery driver, process it
			if(!assignDriverTF.getText().isEmpty() && 
					!deliveryDB.getDeliveredBy(f.getDeliveryID()).equals(assignDriverTF.getText()))
			{
				//Add a new delivery to the delivery history with the assigned driver
				//and the status set to assigned. Adding new delivery updates family changed by field
//				f.addDelivery(new ONCDelivery(DELIVERY_STATUS_ASSIGNED,
//												assignDriverTF.getText(), 
//												"Delivery Driver Assigned", chngdBy,
//												oncGVs.getTodaysDate()));
				
				ONCDelivery reqDelivery = new ONCDelivery(-1, f.getID(), DELIVERY_STATUS_ASSIGNED,
															assignDriverTF.getText(),
															"Delivery Driver Assigned",
															oncGVs.getUserLNFI(),
															Calendar.getInstance());
				
				String response = deliveryDB.add(this, reqDelivery);
				if(response.startsWith("ADDED_DELIVERY"))
				{
					assignmentsMade++;	
				}
				else
				{
					//display an error message that update request failed
					GlobalVariables gvs = GlobalVariables.getInstance();
					JOptionPane.showMessageDialog(this, "ONC Server denied Driver Update," +
							"try again later","Driver Update Failed",  
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}
			}
		}
		
		sortTable.clearSelection();
		
		//Clear the change text field after change is applied	
		assignDriverTF.setText("");
		
		//Changes may have been applied, disable until user selects new table row(s) and values
		btnApplyChanges.setEnabled(false);
		
		//If drivers were assigned, rebuild the sort table. This will cause the families who
		//have just been assigned drivers to no longer be in the table. 
		if(assignmentsMade > 0)
			buildTableList();
		
		bChangingTable = false;
		
		return (assignmentsMade > 0 ? true:false);
	}
	
	//Checks to see if the Apply Changes button should be enabled. It is enabled whenever
	//a row(s) is selected in the table and a valid driver id is present in the text
	//field. If either condition is not met, the button is disabled
	void checkApplyChangesEnabled()
	{
		if(sortTable.getSelectedRows().length > 0 && 
			assignDriverTF.getText().length() > 0 &&
			 assignDriverTF.getText().length() < 4 &&
			  assignDriverTF.getText().matches("-?\\d+(\\.\\d+)?") && 
			   assignDriverTF.getText().charAt(0) != '0')
						
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}
	
	
	//Resets each search criteria gui and its corresponding member variable to the initial
	//condition and then rebuilds the table array.  It disables the gui event before changing
	//to prevent multiple builds of the table.
	void onResetCriteriaClicked()
	{
		oncnumTF.setEnabled(false);
		oncnumTF.setText("");
		sortONCNum = "";
		oncnumTF.setEnabled(true);
	
		sRegCB.setEnabled(false);
		sRegCB.setSelectedIndex(0);
		sortstartRegion = 0;
		sRegCB.setEnabled(true);
		
		eRegCB.setEnabled(false);
		eRegCB.setSelectedIndex(0);
		sortendRegion = 0;
		eRegCB.setEnabled(true);
		
		dstatusCB.setEnabled(false);
		dstatusCB.setSelectedIndex(0);
		sortDStatus = 0;
		dstatusCB.setEnabled(true);
		
		buildTableList();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList();
		}
		else if(e.getSource() == sRegCB && sRegCB.getSelectedIndex() != sortstartRegion)
		{
			sortstartRegion = sRegCB.getSelectedIndex();
			buildTableList();		
		}				
		else if(e.getSource() == eRegCB && eRegCB.getSelectedIndex() != sortendRegion)
		{
			sortendRegion = eRegCB.getSelectedIndex();
			buildTableList();			
		}		
		else if(e.getSource() == dstatusCB && dstatusCB.getSelectedIndex() != sortDStatus)
		{						
			sortDStatus = dstatusCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == btnPrintListing) { onPrintListing("ONC Deliverers"); }
		else if(e.getSource() == btnApplyChanges)
		{
			onApplyChanges();
		}

		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee. 
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("UPDATED_FAMILY") || dbe.getType().equals("UPDATED_DRIVER") ||
				dbe.getType().equals("ADDED_DRIVER") || dbe.getType().equals("ADDED_DELIVERY"))
		{
			if(this.isVisible())
				buildTableList();
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject();
			updateRegionList(regList);
		}
	}
	
	/***********************************************************************************
	 * This class implements a key listener for the AssignDriver text field that
	 * listens to determine when it is empty. If it becomes empty, the listener rebuilds
	 * disables the Assign Deliverer button.
	 ***********************************************************************************/
	 private class AssignDriverTFKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {			
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			if(assignDriverTF.getText().isEmpty())
				btnApplyChanges.setEnabled(false);
			else
				checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee.			
		}

		@Override
		public void keyTyped(KeyEvent arg0){
		}
			
	 }
	
	/***********************************************************************************
	 * This class implements a key listener for the ONC Number test field that
	 * listens to the ONC Number text field to determine when it is empty. If it becomes empty,
	 * the listener rebuilds the sort table array list
	 ***********************************************************************************/
	 private class ONCNumberKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
			if(oncnumTF.getText().isEmpty())
			{
				sortONCNum = "";
				buildTableList();
			}	
		}
	 }
}
