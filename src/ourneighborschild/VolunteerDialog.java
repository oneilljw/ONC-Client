package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.google.gson.Gson;

public class VolunteerDialog extends EntityDialog
{
	/**
	 * Implements a dialog to manage ONC Delivery Drivers
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_DRIVER_MSSG = "No Volunteers";
	private static final int HISTORY_ICON_INDEX = 32;
	
	//database references
	private VolunteerDB ddb;
	private DeliveryDB deliveryDB;

	
	//ui components
	private JLabel lblFamDel, lblSignIns, lblGroup, lblComment, lblLastSignIn, lblQty;
    private JTextField drvNumTF, firstnameTF,lastnameTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, hPhoneTF, cPhoneTF;
    private JTextField emailTF;
    private JRadioButton btnLog;
    private JCheckBox[] ckBoxActivities;
    
    private ONCVolunteer currVolunteer;	//reference to the current ONCDriver object being displayed
    
	public VolunteerDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Volunteer Information");
		
		//Initialize database object variables and register listeners
		ddb = VolunteerDB.getInstance();	//Reference to the driver data base
		if(ddb != null)
			ddb.addDatabaseListener(this);
		
		deliveryDB = DeliveryDB.getInstance();
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
		currVolunteer = null;
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, ddb);
        nav.setDefaultMssg("Our Neighbor's Child Volunteers");
        nav.setCount1("Attempted: " + Integer.toString(0));
        nav.setCount2("Delivered: " + Integer.toString(0));
        nav.setNextButtonText("Next Partner");
        nav.setPreviousButtonText("Previous Partner");

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JPanel ckBoxPanel = new JPanel();
        ckBoxPanel.setLayout(new BoxLayout(ckBoxPanel, BoxLayout.Y_AXIS));
        ckBoxPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Activities"));
        
        drvNumTF = new JTextField(NO_DRIVER_MSSG);
        drvNumTF.setPreferredSize(new Dimension (72, 48));
        drvNumTF.setBorder(BorderFactory.createTitledBorder("Driver #"));
        drvNumTF.setHorizontalAlignment(JLabel.RIGHT);
        drvNumTF.addActionListener(dcListener);
        
        firstnameTF = new JTextField(10);
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.addActionListener(dcListener);
        
        lastnameTF = new JTextField(10);
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.addActionListener(dcListener);
        
        lblGroup = new JLabel("None");
        lblGroup.setPreferredSize(new Dimension (216, 48));
        lblGroup.setToolTipText("Group volunteer is with");
        lblGroup.setBorder(BorderFactory.createTitledBorder("Group"));
        
        lblQty = new JLabel("1", JLabel.RIGHT);
        lblQty.setPreferredSize(new Dimension (48, 48));
        lblQty.setToolTipText("# in group");
        lblQty.setBorder(BorderFactory.createTitledBorder("Qty"));
        
        op1.add(drvNumTF);
        op1.add(firstnameTF);
        op1.add(lastnameTF);
        op1.add(lblGroup);
        op1.add(lblQty);
        
        hPhoneTF = new JTextField(8);
        hPhoneTF.setToolTipText("Delivery partner home phone #");
        hPhoneTF.setBorder(BorderFactory.createTitledBorder("Home Phone #"));
        hPhoneTF.addActionListener(dcListener);
        
        cPhoneTF = new JTextField(8);
        cPhoneTF.setToolTipText("Delivery partner cell phone #");
        cPhoneTF.setBorder(BorderFactory.createTitledBorder(" Cell Phone #"));
        cPhoneTF.addActionListener(dcListener);
                                          
        emailTF = new JTextField(17);
        emailTF.setToolTipText("Delivery partner email address");
        emailTF.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailTF.setHorizontalAlignment(JTextField.LEFT);
        emailTF.addActionListener(dcListener);
        
        lblSignIns = new JLabel("0", JLabel.RIGHT);
        lblSignIns.setPreferredSize(new Dimension (72, 48));
        lblSignIns.setToolTipText("# Warehouse Sign-Ins");
        lblSignIns.setBorder(BorderFactory.createTitledBorder("Sign-Ins"));
        
        lblLastSignIn = new JLabel("Never");
        lblLastSignIn.setPreferredSize(new Dimension (104, 48));
        lblLastSignIn.setToolTipText("Last time volunteer signed into the warehouse");
        lblLastSignIn.setBorder(BorderFactory.createTitledBorder("Last Sign In"));
      
        op2.add(hPhoneTF);
        op2.add(cPhoneTF);
        op2.add(emailTF);
        op2.add(lblSignIns);
        op2.add(lblLastSignIn);
 
        streetnumTF = new JTextField(4);
        streetnumTF.setToolTipText("Address of delivery partner");
        streetnumTF.setBorder(BorderFactory.createTitledBorder("St. #"));
        streetnumTF.addActionListener(dcListener);
        
        streetnameTF = new JTextField(13);
        streetnameTF.setToolTipText("Address of delivery partner");
        streetnameTF.setBorder(BorderFactory.createTitledBorder("Street"));
        streetnameTF.addActionListener(dcListener);
            
        unitTF = new JTextField(5);
        unitTF.setToolTipText("Address of delivery partner");
        unitTF.setBorder(BorderFactory.createTitledBorder("Unit #"));
        unitTF.addActionListener(dcListener);
        
        cityTF = new JTextField(7);
        cityTF.setToolTipText("Address of delivery partner");
        cityTF.setBorder(BorderFactory.createTitledBorder("City"));
        cityTF.addActionListener(dcListener);
        
        zipTF = new JTextField(4);
        zipTF.setToolTipText("Address of deliverer");
        zipTF.setBorder(BorderFactory.createTitledBorder("Zip"));
        zipTF.addActionListener(dcListener);
        
        lblFamDel = new JLabel("0", JLabel.RIGHT);
        lblFamDel.setPreferredSize(new Dimension (52, 48));
        lblFamDel.setToolTipText("# Deliveries Partner Made");
        lblFamDel.setBorder(BorderFactory.createTitledBorder("# Del"));
        
        btnLog = new JRadioButton(gvs.getImageIcon(HISTORY_ICON_INDEX));
        btnLog.setToolTipText("Click to view volunteer's sign-in history");
        btnLog.setEnabled(false);
        btnLog.addActionListener(new SignInHistoryListener(this)); 
        
        op3.add(streetnumTF);
        op3.add(streetnameTF);
        op3.add(unitTF);
        op3.add(cityTF);
        op3.add(zipTF);
        op3.add(lblFamDel);
        op3.add(btnLog);
                
        lblComment = new JLabel("None");
        lblComment.setPreferredSize(new Dimension (600, 48));
        lblComment.setToolTipText("Last comment from volunteer");
        lblComment.setBorder(BorderFactory.createTitledBorder("Volunteer Comment"));
       
        op4.add(lblComment);
        
        ckBoxActivities = new JCheckBox[8];
        ckBoxActivities[0] = new JCheckBox("Delivery Set Up");
        ckBoxActivities[1] = new JCheckBox("Delivery");
        ckBoxActivities[2] = new JCheckBox("Warehouse Support");
        ckBoxActivities[3] = new JCheckBox("Packager");
        ckBoxActivities[4] = new JCheckBox("Gift Inventory");
        ckBoxActivities[5] = new JCheckBox("Shopping");
        ckBoxActivities[6] = new JCheckBox("Cookie Baker");
        ckBoxActivities[7] = new JCheckBox("Warehouse Clean Up");
        
        int bn;
        for(bn=0; bn<5; bn++ )
        	op5.add(ckBoxActivities[bn]);
        
        for(bn=5; bn < ckBoxActivities.length; bn++)
        	op6.add(ckBoxActivities[bn]);
       
        ckBoxPanel.add(op5);
        ckBoxPanel.add(op6);
            
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        entityPanel.add(op4);
        entityPanel.add(ckBoxPanel);
        
        //Set up control panel
        btnNew.setText("Add New Volunteer");
    	btnNew.setToolTipText("Click to add a new volunteer");
     
        btnDelete.setText("Delete Volunteer");
    	btnDelete.setToolTipText("Click to delete this volunteer");
    	
        btnSave.setText("Save New Volunteer");
    	btnSave.setToolTipText("Click to save the new volunteer");
        
        btnCancel.setText("Cancel Add New Volunteer");
    	btnCancel.setToolTipText("Click to cancel adding a new volunteer");

    	//add the panels to the content pane
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        //add the content pane to the dialog and arrange
        this.setContentPane(contentPane);
        pack();
        setResizable(true);
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void update()
	{
		//Check to see if user has changed any field, if so, save it	
		ONCVolunteer updateDriver = new ONCVolunteer(currVolunteer);	//make a copy of current driver
		boolean bCD = false; //used to indicate a change has been detected
		
		if(!drvNumTF.getText().equals(updateDriver.getDrvNum()))
		{
			updateDriver.setDrvNum(drvNumTF.getText());
			bCD = true;
		}
		if(!firstnameTF.getText().equals(updateDriver.getfName()))
		{
			updateDriver.setfName(firstnameTF.getText());
			bCD = true;
		}
		if(!lastnameTF.getText().equals(updateDriver.getlName())) { updateDriver.setlName(lastnameTF.getText()); bCD = true; }
		if(!hPhoneTF.getText().equals(updateDriver.getHomePhone())) { updateDriver.setHomePhone(hPhoneTF.getText()); bCD = true; }
		if(!cPhoneTF.getText().equals(updateDriver.getCellPhone())) { updateDriver.setCellPhone(cPhoneTF.getText()); bCD = true; }
		if(!emailTF.getText().equals(updateDriver.getEmail())) { updateDriver.setEmail(emailTF.getText()); bCD = true; }
		if(!streetnumTF.getText().equals(updateDriver.gethNum())) { updateDriver.sethNum(streetnumTF.getText()); bCD = true; }
		if(!streetnameTF.getText().equals(updateDriver.getStreet())) { updateDriver.setStreet(streetnameTF.getText()); bCD = true; }		
		if(!unitTF.getText().equals(updateDriver.getUnit())) { updateDriver.setUnit(unitTF.getText()); bCD = true; }
		if(!cityTF.getText().equals(updateDriver.getCity())) { updateDriver.setCity(cityTF.getText()); bCD = true; }
		if(!zipTF.getText().equals(updateDriver.getZipcode())) { updateDriver.setZipcode(zipTF.getText()); bCD = true; }
		
		if(bCD)	//If an update to organization data (not stop light data) was detected
		{
			updateDriver.setDateChanged(gvs.getTodaysDate());
			
			//request an update from the server
			String response = ddb.update(this, updateDriver);
			
			if(response.startsWith("UPDATED_DRIVER"))
			{
				display(updateDriver);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Driver Update," +
						"try again later","Driver Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
			
			bCD = false;
		}
	}
	
	void display(ONCEntity driver)
	{	
		if(ddb.size() <= 0)
		{
			currVolunteer = null;
			clear();
			drvNumTF.setText("None");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
			btnLog.setEnabled(false);
		}
		else 
		{
			//Determine what to display based on currDriver and driver
			if(currVolunteer == null && driver == null)
				currVolunteer = ddb.getObjectAtIndex(0);
			else if(driver != null  && driver != currVolunteer)
				currVolunteer = (ONCVolunteer) driver;
				
			//display the current driver
			drvNumTF.setText(currVolunteer.getDrvNum());
			firstnameTF.setText(currVolunteer.getfName());
			firstnameTF.setCaretPosition(0);
			lastnameTF.setText(currVolunteer.getlName());
			lastnameTF.setCaretPosition(0);
			emailTF.setText(currVolunteer.getEmail());
			emailTF.setCaretPosition(0);
			hPhoneTF.setText(currVolunteer.getHomePhone());
			hPhoneTF.setCaretPosition(0);
			cPhoneTF.setText(currVolunteer.getCellPhone());
			cPhoneTF.setCaretPosition(0);
				
			lblQty.setText(Integer.toString(currVolunteer.getQty()));
			lblFamDel.setText(Integer.toString(currVolunteer.getDelAssigned()));
			lblSignIns.setText(Integer.toString(currVolunteer.getSignIns()));
			
			if(currVolunteer.getDelAssigned() == 0)	//Can only delete if a delivery was assigned
				btnDelete.setEnabled(true);
			else
				btnDelete.setEnabled(false);
			
			streetnumTF.setText(currVolunteer.gethNum());
			streetnameTF.setText(currVolunteer.getStreet());
			unitTF.setText(currVolunteer.getUnit());
			cityTF.setText(currVolunteer.getCity());
			zipTF.setText(currVolunteer.getZipcode());
			
			lblGroup.setText(currVolunteer.getGroup().isEmpty() ? "None" : currVolunteer.getGroup());
			lblComment.setText(currVolunteer.getComment());
			
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm");
			lblLastSignIn.setText(currVolunteer.getSignIns() == 0 ? "Never" : sdf.format(currVolunteer.getDateChanged()));
			
			setActivities(currVolunteer.getActivityCode());
			
			btnLog.setEnabled(currVolunteer.getSignIns() > 0);

			nav.setStoplightEntity(currVolunteer);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
		}
	}
	
	void setActivities(int activityCode)
	{
		if(activityCode > 0)
		{
			int bn = 0;
			for(int mask = 1; mask <= ActivityCode.lastCode(); mask = mask << 1)
				ckBoxActivities[bn++].setSelected((mask & activityCode) > 0);
		}		
		else
			clearActivities();
	}
	
	void clearActivities()
	{
		for(int bn=0; bn < ckBoxActivities.length; bn++)
			ckBoxActivities[bn].setSelected(false);
	}
	
	void clear()
	{
		drvNumTF.setText("");
		firstnameTF.setText("");
		lastnameTF.setText("");
		lblQty.setText("0");
		lblFamDel.setText("0");
		lblSignIns.setText("0");
		lblComment.setText("");
		lblGroup.setText("");
		hPhoneTF.setText("");
		cPhoneTF.setText("");
		emailTF.setText("");		
		streetnumTF.setText("");
		streetnameTF.setText("");
		unitTF.setText("");
		cityTF.setText("");
		zipTF.setText("");
		nav.clearStoplight();
		
		clearActivities();
		btnLog.setEnabled(false);
	}

	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Partner's Information"));
		clear();
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
		
		drvNumTF.setText("New");
	}
	
	void onDelete()
	{
		ONCVolunteer delDriver = ddb.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("Are you sure you want to delete %s from the data base?", 
											delDriver.getfName() + " " + delDriver.getlName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Partner Database Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			String response = ddb.delete(this, delDriver);
			
			if(response.startsWith("DELETED_DRIVER"))
			{
				processDeletedEntity(ddb);
			}
			else
			{
				String err_mssg = "ONC Server denied delete partner request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete Partner Request Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currVolunteer);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new volunteer from user input	
		ONCVolunteer newDriver = new ONCVolunteer(-1, "N/A", firstnameTF.getText(), lastnameTF.getText(),
					emailTF.getText(), streetnumTF.getText(), streetnameTF.getText(), 
					unitTF.getText(), cityTF.getText(), zipTF.getText(), 
					hPhoneTF.getText(), cPhoneTF.getText(), "1", "Delivery", "", "", new Date(),
					userDB.getUserLNFI());
						
		//send add request to the local data base
		String response = ddb.add(this, newDriver);
						
		if(response.startsWith("ADDED_DRIVER"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			ONCVolunteer addedDriver = gson.fromJson(response.substring(12), ONCVolunteer.class);
							
			//set the display index, on, to the new partner added and display organization
			display(addedDriver);
			nav.setIndex(ddb.getListIndexByID(ddb.getList(), addedDriver.getID()));
		}
		else
		{
			String err_mssg = "ONC Server denied add driver request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Driver Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currVolunteer);
		}
				
		//reset to review mode and display the proper organization
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Partner Information"));
		entityPanel.setBackground(pBkColor);

		bAddingNewEntity = false;
		setControlState();
	}
	
	void onCancelNew()
	{
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Partner Information"));
		display(currVolunteer);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DRIVER"))
		{
			ONCVolunteer updatedDriver = (ONCVolunteer) dbe.getObject();
			
			//If current driver is being displayed has changed, re-display it
			if(currVolunteer != null && currVolunteer.getID() == updatedDriver.getID() && !bAddingNewEntity)
				display(updatedDriver);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DRIVER"))
		{
			ONCVolunteer addedDriver = (ONCVolunteer) dbe.getObject();
			//If no driver is being displayed, display the added one
			if(currVolunteer == null && ddb.size() > 0 && !bAddingNewEntity)
				display(addedDriver);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_DRIVER"))
		{
			//if the deleted driver was the only driver in data base, clear the display
			//otherwise, if the deleted driver is currently being displayed, change the
			//index to the previous driver in the database and display.
			if(ddb.size() == 0)
			{
				currVolunteer = null;
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCVolunteer deletedDriver = (ONCVolunteer) dbe.getObject();
				if(currVolunteer.getID() == deletedDriver.getID())
				{
					if(nav.getIndex() == 0)
						nav.setIndex(ddb.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(ddb.getDriver(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
		{
			//If the added delivery is associated with the current driver being displayed,
			//update the display so the # of deliveries assigned field updates
			ONCDelivery del = (ONCDelivery) dbe.getObject();
			
			if(!bAddingNewEntity && del != null && currVolunteer != null && 
					del.getdDelBy().equals(currVolunteer.getDrvNum()))
				
				display(currVolunteer);
		}
		else if(dbe.getType().equals("LOADED_DRIVERS"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Volunteer Information", GlobalVariables.getCurrentSeason()));
		}
	}

	/*************************************************************************************
	 * If the entity selection event is fired by a ONCFamilyTableDialog or by the nav panel, 
	 * and the current mode is not adding a new partner, save any changes to the 
	 * currently displayed partner and display the partner selected in the sort partner 
	 * dialog partner table. The listeners are currently registered by the FamilyPanel
	 * when this dialog is created.
	 ************************************************************************************/
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.WISH)
			{
				ONCFamily fam = (ONCFamily) tse.getObject1();
				
				String logEntry = String.format("Driver Dialog Event: %s, ONC# %s selected",
						tse.getType(), fam.getONCNum());
				LogDialog.add(logEntry, "M");
				
				ONCDelivery del = deliveryDB.getDelivery(fam.getDeliveryID());
			
				if(del != null && !del.getdDelBy().isEmpty())
				{
					//There is s driver assigned. Determine who it is from the driver number
					//and display that driver, if they have been entered into the driver data base.
					int index = ddb.getDriverIndex(del.getdDelBy());
					if(index > -1)
					{
						update();
						nav.setIndex(index);
						display(ddb.getDriver(index));
					}
				}
			}
			else if(tse.getType() == EntityType.VOLUNTEER)
			{
				ONCVolunteer driver = (ONCVolunteer) tse.getObject1();
				update();
				display(driver);
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.WISH, EntityType.VOLUNTEER);
	}
	
	/***********************************************************************************************
	 * This class implements a listener for the sign-in history radio button. When the button is
	 * clicked (the button is only enabled when a volunteer has at least one warehouse sign-in)
	 * a SignInHistory dialog is created and displayed. 
	 ***********************************************************************************************/
	private class SignInHistoryListener implements ActionListener
	{
		private JDialog owner;
		
		SignInHistoryListener(JDialog owner)
		{
			this.owner = owner;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			//create and show the sign-in history dialog
			SignInHistoryDialog siHistoryDlg = new SignInHistoryDialog(owner, true);
	        EntityEventManager.getInstance().registerEntitySelectionListener(siHistoryDlg);
	        
	        siHistoryDlg.setLocationRelativeTo(btnLog);
	        siHistoryDlg.display(currVolunteer);
	    	siHistoryDlg.setVisible(true);
		}
	}
}
