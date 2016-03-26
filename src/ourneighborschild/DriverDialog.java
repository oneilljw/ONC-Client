package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.Gson;

public class DriverDialog extends EntityDialog
{
	/**
	 * Implements a dialog to manage ONC Delivery Drivers
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_DRIVER_MSSG = "No Partners";
	
	//database references
	private DriverDB ddb;
	private DeliveryDB deliveryDB;
	
	//ui components
	private JLabel lblFamDel;
    private JTextField drvNumTF, firstnameTF,lastnameTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, hPhoneTF, cPhoneTF;
    private JTextField emailTF;
    
    private ONCDriver currDriver;	//reference to the current ONCDriver object being displayed
    
	public DriverDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Delivery Partner Information");
		
		//Initialize object variables
		ddb = DriverDB.getInstance();	//Reference to the driver data base
		if(ddb != null)
			ddb.addDatabaseListener(this);
		
		deliveryDB = DeliveryDB.getInstance();
		if(deliveryDB != null)
			deliveryDB.addDatabaseListener(this);
		
		currDriver = null;
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, ddb);
        nav.setDefaultMssg("Our Neighbor's Child Delivery Partners");
        nav.setCount1("Attempted: " + Integer.toString(0));
        nav.setCount2("Delivered: " + Integer.toString(0));
        nav.setNextButtonText("Next Partner");
        nav.setPreviousButtonText("Previous Partner");
//      nav.addNavigationListener(this);
        nav.addEntitySelectionListener(this);

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Delivery Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        drvNumTF = new JTextField(NO_DRIVER_MSSG);
        drvNumTF.setPreferredSize(new Dimension (72, 48));
        drvNumTF.setBorder(BorderFactory.createTitledBorder("Driver #"));
        drvNumTF.setHorizontalAlignment(JLabel.RIGHT);
        drvNumTF.addActionListener(dcListener);
        
        firstnameTF = new JTextField(12);
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.addActionListener(dcListener);
        
        lastnameTF = new JTextField(12);
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.addActionListener(dcListener);
        
        lblFamDel = new JLabel("0", JLabel.RIGHT);
        lblFamDel.setPreferredSize(new Dimension (52, 48));
        lblFamDel.setToolTipText("# Deliveries Partner Made");
        lblFamDel.setBorder(BorderFactory.createTitledBorder("# Del"));
        
        hPhoneTF = new JTextField(9);
        hPhoneTF.setToolTipText("Delivery partner home phone #");
        hPhoneTF.setBorder(BorderFactory.createTitledBorder("Home Phone #"));
        hPhoneTF.addActionListener(dcListener);
        
        cPhoneTF = new JTextField(9);
        cPhoneTF.setToolTipText("Delivery partner cell phone #");
        cPhoneTF.setBorder(BorderFactory.createTitledBorder(" Cell Phone #"));
        cPhoneTF.addActionListener(dcListener);
                
        op1.add(drvNumTF);
        op1.add(firstnameTF);
        op1.add(lastnameTF);
        op1.add(hPhoneTF);
        op1.add(cPhoneTF);
        op1.add(lblFamDel);
                                           
        emailTF = new JTextField(18);
        emailTF.setToolTipText("Delivery partner email address");
        emailTF.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailTF.setHorizontalAlignment(JTextField.LEFT);
        emailTF.addActionListener(dcListener);
 
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
              
        op2.add(streetnumTF);
        op2.add(streetnameTF);
        op2.add(unitTF);
        op2.add(cityTF);
        op2.add(zipTF);
        op2.add(emailTF);
                      
        entityPanel.add(op1);
        entityPanel.add(op2);
        
        //Set up control panel
        btnNew.setText("Add New Driver");
    	btnNew.setToolTipText("Click to add a new driverr");
     
        btnDelete.setText("Delete Driver");
    	btnDelete.setToolTipText("Click to delete this driver");
    	
        
        btnSave.setText("Save NewDriver");
    	btnSave.setToolTipText("Click to save the new driver");
        
        btnCancel.setText("Cancel Add New Driver");
    	btnCancel.setToolTipText("Click to cancel adding a new drivonconer");

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
		ONCDriver updateDriver = new ONCDriver(currDriver);	//make a copy of current driver
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
			currDriver = null;
			clear();
			drvNumTF.setText("None");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else 
		{
			//Determine what to display based on currDriver and driver
			if(currDriver == null && driver == null)
				currDriver = ddb.getObjectAtIndex(0);
			else if(driver != null  && driver != currDriver)
				currDriver = (ONCDriver) driver;
				
			//display the current driver
			drvNumTF.setText(currDriver.getDrvNum());
			firstnameTF.setText(currDriver.getfName());
			firstnameTF.setCaretPosition(0);
			lastnameTF.setText(currDriver.getlName());
			lastnameTF.setCaretPosition(0);
			emailTF.setText(currDriver.getEmail());
			emailTF.setCaretPosition(0);
			hPhoneTF.setText(currDriver.getHomePhone());
			hPhoneTF.setCaretPosition(0);
			cPhoneTF.setText(currDriver.getCellPhone());
			cPhoneTF.setCaretPosition(0);
				
			lblFamDel.setText(Integer.toString(currDriver.getDelAssigned()));
			
			if(currDriver.getDelAssigned() == 0)	//Can only delete if a delivery was assigned
				btnDelete.setEnabled(true);
			else
				btnDelete.setEnabled(false);
			
			streetnumTF.setText(currDriver.gethNum());
			streetnameTF.setText(currDriver.getStreet());
			unitTF.setText(currDriver.getUnit());
			cityTF.setText(currDriver.getCity());
			zipTF.setText(currDriver.getZipcode());

			nav.setStoplightEntity(currDriver);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
		}
	}
	
	void clear()
	{
		drvNumTF.setText("");
		firstnameTF.setText("");
		lastnameTF.setText("");
		lblFamDel.setText("0");
		hPhoneTF.setText("");
		cPhoneTF.setText("");
		emailTF.setText("");		
		streetnumTF.setText("");
		streetnameTF.setText("");
		unitTF.setText("");
		cityTF.setText("");
		zipTF.setText("");
		nav.clearStoplight();
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
		ONCDriver delDriver = ddb.getObjectAtIndex(nav.getIndex());
		
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
				display(currDriver);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new driver from user input	
		ONCDriver newDriver = new ONCDriver(-1, "N/A", firstnameTF.getText(), lastnameTF.getText(),
					emailTF.getText(), streetnumTF.getText(), streetnameTF.getText(), 
					unitTF.getText(), cityTF.getText(), zipTF.getText(), 
					hPhoneTF.getText(), cPhoneTF.getText(), "", "", new Date(),
					GlobalVariables.getUserLNFI());
						
		//send add request to the local data base
		String response = ddb.add(this, newDriver);
						
		if(response.startsWith("ADDED_DRIVER"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			ONCDriver addedDriver = gson.fromJson(response.substring(12), ONCDriver.class);
							
			//set the display index, on, to the new partner added and display organization
			display(addedDriver);
			nav.setIndex(ddb.getListIndexByID(ddb.getList(), addedDriver.getID()));
		}
		else
		{
			String err_mssg = "ONC Server denied add driver request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Driver Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currDriver);
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
		display(currDriver);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DRIVER"))
		{
			ONCDriver updatedDriver = (ONCDriver) dbe.getObject();
			
			//If current driver is being displayed has changed, re-display it
			if(currDriver.getID() == updatedDriver.getID() && !bAddingNewEntity)
				display(updatedDriver);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DRIVER"))
		{
			ONCDriver addedDriver = (ONCDriver) dbe.getObject();
			//If no driver is being displayed, display the added one
			if(currDriver == null && ddb.size() > 0 && !bAddingNewEntity)
				display(addedDriver);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_DRIVER"))
		{
			//if the deleted driver was the only driver in data base, clear the display
			//otherwise, if the deleted driver is currently being displayed, change the
			//index to the previous driver in the database and display.
			if(ddb.size() == 0)
			{
				currDriver = null;
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCDriver deletedDriver = (ONCDriver) dbe.getObject();
				if(currDriver.getID() == deletedDriver.getID())
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
			
			if(!bAddingNewEntity && del != null && currDriver != null && 
					del.getdDelBy().equals(currDriver.getDrvNum()))
				
				display(currDriver);
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
			else if(tse.getType() == EntityType.DRIVER)
			{
				ONCDriver driver = (ONCDriver) tse.getObject1();
				update();
				display(driver);
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.WISH, EntityType.DRIVER);
	}
}
