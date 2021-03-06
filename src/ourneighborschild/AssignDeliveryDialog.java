package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AssignDeliveryDialog extends SortFamilyTableDialog 
{
	/**
	 * This class implements a dialog that allows the user to assign delivery drivers to families
	 * that are packaged and ready for delivery. The class extends the SortFamilyDialog which in
	 * turn extends SortTable dialog which provides the common look and feel for all ONC
	 * application dialogs that manage ONCEntity data. The SortFamilyDialog provides specific 
	 * features unique to dialogs that display only ONCFamily data in their table. This class
	 * extends SortFamilyDialog to add unique search criteria and the
	 * functionality that allows the user to assign a driver for delivery. It also adds a print
	 * listing button to the control panel
	 */
	private static final long serialVersionUID = 1L;
	private static final int SOUND_DURATION = 250;
	private static final int SUCCESS_SOUND_FREQ = 500;
	private static final int FAILED_SOUND_FREQ = 150;
	
	private FamilyHistoryDB familyHistoryDB;
	private VolunteerDB volunteerDB;
	
	private int sortstartRegion, sortendRegion;
	private FamilyGiftStatus sortGiftStatus;
	
	private JComboBox<String> sRegCB;
	private JComboBox<String> eRegCB;
	private JComboBox<FamilyGiftStatus> dstatusCB;
	private DefaultComboBoxModel<String> eRegCBM;	//start region uses the inherited model
	private JTextField oncnumTF, barcodeTF, assignDriverTF;
	private JLabel lblResult;
	
	public AssignDeliveryDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Delivery Assignment");
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		familyHistoryDB = FamilyHistoryDB.getInstance();
		
		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		RegionDB regions = RegionDB.getInstance();
		if(regions != null)
			regions.addDatabaseListener(this);
		
		//Initialize member variables
		sortstartRegion = 0;
		sortendRegion = 0;
		sortGiftStatus = FamilyGiftStatus.Any;
				
		//Set up unique sort criteria gui
		oncnumTF = new JTextField(5);
		oncnumTF.setEditable(true);
		oncnumTF.setMaximumSize(new Dimension(64,56));
//    	oncnumTF.setPreferredSize(new Dimension(88,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
		regionCBM.addElement("Any");	//superclass has regionCBM
		sRegCB = new JComboBox<String>();
		sRegCB.setModel(regionCBM);
		sRegCB.setMaximumSize(new Dimension(48,56));
		sRegCB.setBorder(BorderFactory.createTitledBorder("Region Start"));
		sRegCB.addActionListener(this);
		
		eRegCBM = new DefaultComboBoxModel<String>();
		eRegCBM.addElement("Any");
		eRegCB = new JComboBox<String>();
		eRegCB.setModel(eRegCBM);
		eRegCB.setMaximumSize(new Dimension(48,56));
		eRegCB.setBorder(BorderFactory.createTitledBorder("Region End"));
		eRegCB.addActionListener(this);
		
		dstatusCB = new JComboBox<FamilyGiftStatus>(FamilyGiftStatus.getSearchFilterList());
		dstatusCB.setMaximumSize(new Dimension(192,56));
		dstatusCB.setBorder(BorderFactory.createTitledBorder("Gift Status Less Then"));
		dstatusCB.addActionListener(this);
		
		barcodeTF = new JTextField(6);
		barcodeTF.setMaximumSize(new Dimension(96,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Type or scan barcode for delivery card. If typed, press <Enter>");
		barcodeTF.addActionListener(this);
		
		//Add all sort criteria gui to search criteria pane
        sortCriteriaPanelTop.add(oncnumTF);
        sortCriteriaPanelTop.add(sRegCB);
		sortCriteriaPanelTop.add(eRegCB);				
		sortCriteriaPanelTop.add(dstatusCB);
		sortCriteriaPanelTop.add(new JPanel());
		sortCriteriaPanelTop.add(barcodeTF);
		 
		//Set up change data panel gui
        assignDriverTF = new JTextField(12);
        assignDriverTF.setToolTipText("Type Delivery ID followed by <Enter>");
		assignDriverTF.setBorder(BorderFactory.createTitledBorder("Delivery Driver ID#"));
		assignDriverTF.addKeyListener(new AssignDriverTFKeyListener());

		//Add the change data gui components to the third panel	
		changeDataPanel.add(assignDriverTF);
        changeDataPanel.setBorder(BorderFactory.createTitledBorder("Assign Delivery Driver"));
        
        gbc.gridx = 1;
        gbc.ipadx = 0;
        gbc.weightx = 1.0;
        changePanel.add(changeDataPanel, gbc);
        
        //Change the text of the ApplyChanges button
        btnApplyChanges.setText("Assign Delivery");
        
        //Add result and print listing button to the bottom panel
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lblResult = new JLabel("Barcode result:");
        resultPanel.add(lblResult);
        
        bottomPanel.add(resultPanel, BorderLayout.CENTER);
        
        this.add(changePanel);
        this.add(bottomPanel);
        
        pack();
        
        barcodeTF.requestFocusInWindow();	//we want scans to process immediately
	}
	
	/**********************************************************************************
	 * This method builds an array list of ONCFamily references that meet criteria for
	 * being assigned for delivery. The first criteria is family status = PACKAGED.
	 * Additional criteria include the ONC Number, the family region and delivery 
	 * status, all selected from the sort criteria panel. 
	 **********************************************************************************/
	void buildTableList(boolean bPreserveSelections)
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(stAL);
		else
			tableSortCol = -1;
				
		stAL.clear();	//Clear the prior table information in the array list
		
		for(ONCFamily f:fDB.getList())
		{
			//Determine if family meets search criteria to be added to the table. If a
			//family's status isn't FamilyGiftStatus.Packaged, Attempted or Returned, they aren't eligible to be
			//assigned for delivery regardless of search criteria
			int id = 0;
			FamilyHistory fh = familyHistoryDB.getLastFamilyHistory(f.getID());
			if((fh.getGiftStatus() == FamilyGiftStatus.Packaged || 
				 fh.getGiftStatus() == FamilyGiftStatus.Attempted || 
				  fh.getGiftStatus() == FamilyGiftStatus.Returned) &&
				   isRegionInRange(f.getRegion()) &&
				    doesONCNumMatch(f.getONCNum()) &&
				     doesDStatusPass(fh.getGiftStatus()))	
			{													
				stAL.add(new ONCFamilyAndNote(id++, f, fh, null)); //Family is eligible for delivery and search criteria pass	
			}
		}
		
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));	//# items in table
		displaySortTable(stAL, true, tableRowSelectedObjectList);	//Display the table after table array list is built					
	}
	
	@Override
	void setEnabledControls(boolean tf) {}

	void updateRegionList(String[] regions)
	{
		sRegCB.setEnabled(false);
		eRegCB.setEnabled(false);
		
		String currSelStart = sRegCB.getSelectedItem().toString();
		String currSelEnd = eRegCB.getSelectedItem().toString();
		
		regionCBM.removeAllElements();	//Clear the combo box selection list
		regionCBM.addElement("Any");
		
		eRegCBM.removeAllElements();	//Clear the combo box selection list
		eRegCBM.addElement("Any");
		
		for(String s: regions)	//Add new list elements
		{
			regionCBM.addElement(s);
			eRegCBM.addElement(s);
		}
			
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
	protected String[] getTableRow(ONCObject o)
	{
		ONCFamilyAndNote fan = (ONCFamilyAndNote) o;
		ONCFamily f = fan.getFamily();
		FamilyHistory fh = fan.getFamilyHistory();
		
		String[] deliverytablerow = {f.getONCNum(),
				 fh.getFamilyStatus().toString(),
				 fh.getGiftStatus().toString(),
				 Integer.toString(f.getNumOfBags()),
				 Integer.toString(fDB.getNumberOfBikesSelectedForFamily(f)),
				 Integer.toString(f.getNumOfLargeItems()),
				 f.getHouseNum(),
				 f.getStreet(),
				 f.getZipCode(),
				 regions.getRegionID(f.getRegion()),
				 f.getChangedBy(),
				 stoplt[f.getStoplightPos()+1].substring(0, 1),
				 fh.getdDelBy()};
		
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
	boolean doesDStatusPass(FamilyGiftStatus fgs) 
	{
		return sortGiftStatus == FamilyGiftStatus.Any  || fgs.compareTo((FamilyGiftStatus) dstatusCB.getSelectedItem()) < 0;
	}
	
	//Determines if family ONC number matches search criteria
	boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	/**********************************************************************************************
	 * This method takes row(s) selected by the user and the id of the driver entered by the
	 * user in the Driver ID text field and changes the delivery assignment for each family
	 * selected
	 *********************************************************************************************/
	boolean onApplyChanges()
	{
//		bChangingTable = true;
		
		int assignmentsMade = 0;
		int[] row_sel = sortTable.getSelectedRows();

		for(int i=0; i<row_sel.length; i++)
		{
			//Get reference to family object selected
			ONCFamily f = stAL.get(row_sel[i]).getFamily();
			FamilyHistory fh = familyHistoryDB.getLastFamilyHistory(f.getID());
			
			//If a change to the assigned delivery driver, process it
			if(!assignDriverTF.getText().isEmpty() && !fh.getdDelBy().equals(assignDriverTF.getText()))
			{
				//Add a new delivery to the delivery history with the assigned driver
				//and the status set to assigned. Adding new delivery updates family changed by field
				FamilyHistory reqDelivery = new FamilyHistory(-1, f.getID(),
														fh.getFamilyStatus(),
														FamilyGiftStatus.Assigned,
														assignDriverTF.getText(),
														"Delivery Driver Assigned",
														userDB.getUserLNFI(),
														System.currentTimeMillis(),
														fh.getDNSCode());
				
				FamilyHistory response = familyHistoryDB.add(this, reqDelivery);
				if(response != null && response instanceof FamilyHistory)
				{
					assignmentsMade++;	
				}
				else
				{
					//display an error message that update request failed
					GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
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
			buildTableList(false);
		
		barcodeTF.requestFocus();
		
//		bChangingTable = false;
		
		return (assignmentsMade > 0 ? true : false);
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
		oncnumTF.setText("");
		barcodeTF.setText("");
		sortONCNum = "";
		lblResult.setText("");
		
		sRegCB.removeActionListener(this);
		sRegCB.setSelectedIndex(0);
		sortstartRegion = 0;
		sRegCB.addActionListener(this);
		
		eRegCB.removeActionListener(this);
		eRegCB.setSelectedIndex(0);
		sortendRegion = 0;
		eRegCB.addActionListener(this);
		
		dstatusCB.removeActionListener(this);
		dstatusCB.setSelectedIndex(0);
		sortGiftStatus = FamilyGiftStatus.Any;
		dstatusCB.addActionListener(this);
		
		buildTableList(false);
		
		barcodeTF.requestFocus();
	}
	
	@Override
	String[] getColumnToolTips() 
	{
		String[] colToolTips = {"ONC Family Number", "Family Status", "Delivery Status",
				"# of bags packaged", "# of bikes assigned to family",
				"# of large items assigned to family",
				"House Number","Street", "Zip Code", "Region","Changed By",
				"Stoplight Color", "Name of delivery volunteeer or pickup location"};



		return colToolTips;
	}

	@Override
	String[] getColumnNames() 
	{
		String[] columns = {"ONC", "Fam Status", "Del Status", "# Bags", "# Bikes", "# Lg It.", "House",
				"Street", "Zip", "Reg", "Changed By", "SL", "Deliverer or Location"};
		return columns;
	}

	@Override
	int[] getColumnWidths() 
	{
		int[] colWidths = {32, 72, 72, 48, 48, 48, 48, 128, 48, 32, 72, 24, 120};
		return colWidths;
	}

	@Override
	int[] getCenteredColumns() 
	{
		int[] center_cols = {3, 4, 5, 9};
		return center_cols;
	}
	
	@Override
	boolean isONCNumContainerEmpty() { return oncnumTF.getText().isEmpty(); }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			buildTableList(false);
			oncnumTF.setText("");
			lblResult.setText("");
			lblResult.setText("");
			if(sortTable.getRowCount() == 1)
			{
				lblResult.setText("Found family # " + sortONCNum);
				sortTable.setRowSelectionInterval(0, 0);
			}
			else
				lblResult.setText("Family " + sortONCNum + " not found");
			sortONCNum = "";
		}
		else if(e.getSource() == barcodeTF)
		{
			if(gvs.getBarcodeCode().length() != barcodeTF.getText().length())
			{
				lblResult.setText(String.format("Barcode %s is not a valid length", barcodeTF.getText()));
				try 
				{
					SoundUtils.tone(FAILED_SOUND_FREQ, SOUND_DURATION);
				} 
				catch (LineUnavailableException e1) 
				{
					
				}
			}
			else
			{
				//if using UPC-E, eliminate check digits before converting to ONC Number
				String oncNum = barcodeTF.getText().replaceFirst("^0+(?!$)", "");
				sortONCNum = gvs.getBarcodeCode() == Barcode.UPCE  ?  oncNum.substring(0, oncNum.length()-1) : oncNum;
				buildTableList(false);
				barcodeTF.setText("");
				if(sortTable.getRowCount() == 1)
				{	
					lblResult.setText("Found family # " + sortONCNum);
					sortTable.setRowSelectionInterval(0, 0);
				}
				else
					lblResult.setText("Family " + sortONCNum + " not found");
				
				sortONCNum = "";
				try 
				{
					SoundUtils.tone(SUCCESS_SOUND_FREQ, SOUND_DURATION);
				} 
				catch (LineUnavailableException e1) 
				{
					
				}
			}
		}
		else if(e.getSource() == sRegCB && sRegCB.getSelectedIndex() != sortstartRegion)
		{
			lblResult.setText("");
			sortstartRegion = sRegCB.getSelectedIndex();
			buildTableList(false);		
		}				
		else if(e.getSource() == eRegCB && eRegCB.getSelectedIndex() != sortendRegion)
		{
			lblResult.setText("");
			sortendRegion = eRegCB.getSelectedIndex();
			buildTableList(false);			
		}		
		else if(e.getSource() == dstatusCB && dstatusCB.getSelectedItem() != sortGiftStatus)
		{	
			lblResult.setText("");
			sortGiftStatus = (FamilyGiftStatus) dstatusCB.getSelectedItem();
			buildTableList(false);
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
				buildTableList(true);
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject1();
			updateRegionList(regList);
		}
		else if(dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Delivery Assignment", gvs.getCurrentSeason()));
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

	@Override
	void initializeFilters() {
		// TODO Auto-generated method stub
		
	}
}
