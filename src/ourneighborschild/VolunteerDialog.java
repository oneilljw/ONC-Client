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
	 * Implements a dialog to manage ONC Volunteers
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_VOLUNTEER_MSSG = "No Volunteers";
	private static final int HISTORY_ICON_INDEX = 32;
	
	//database references
	private VolunteerDB volDB;
	private FamilyHistoryDB familyHistoryDB;

	//ui components
	private JLabel lblFamDel, lblSignIns, lblLastSignIn, lblQty;
    private JTextField drvNumTF, firstnameTF,lastnameTF, groupTF, commentTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, hPhoneTF, cPhoneTF;
    private JTextField emailTF;
    private JRadioButton btnSignInHistory;
    private JCheckBox[] ckBoxActivities;
    
    private ONCVolunteer currVolunteer;	//reference to the current object displayed
    
	public VolunteerDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Volunteer Information");
		
		//Initialize database object reference variables and register listeners
		volDB = VolunteerDB.getInstance();
		if(volDB != null)
			volDB.addDatabaseListener(this);
		
		familyHistoryDB = FamilyHistoryDB.getInstance();
		if(familyHistoryDB != null)
			familyHistoryDB.addDatabaseListener(this);
		
		currVolunteer = null;
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, volDB);
        nav.setDefaultMssg("Our Neighbor's Child Volunteers");
        nav.setCount1("Attempted: " + Integer.toString(0));
        nav.setCount2("Delivered: " + Integer.toString(0));
        nav.setNextButtonText("Next Volunteer");
        nav.setPreviousButtonText("Previous Volunteer");

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op7 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JPanel ckBoxPanel = new JPanel();
        ckBoxPanel.setLayout(new BoxLayout(ckBoxPanel, BoxLayout.Y_AXIS));
        ckBoxPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Activities"));
        
        drvNumTF = new JTextField(NO_VOLUNTEER_MSSG);
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
        
        groupTF = new JTextField(18);
//        groupTF.setPreferredSize(new Dimension (216, 48));
        groupTF.setToolTipText("Group volunteer is with");
        groupTF.setBorder(BorderFactory.createTitledBorder("Group"));
        groupTF.addActionListener(dcListener);
        
        lblQty = new JLabel("1", JLabel.RIGHT);
        lblQty.setPreferredSize(new Dimension (48, 48));
        lblQty.setToolTipText("# in group");
        lblQty.setBorder(BorderFactory.createTitledBorder("Qty"));
        
        op1.add(drvNumTF);
        op1.add(firstnameTF);
        op1.add(lastnameTF);
        op1.add(groupTF);
        op1.add(lblQty);
        
        hPhoneTF = new JTextField(8);
        hPhoneTF.setToolTipText("Volunteer home phone #");
        hPhoneTF.setBorder(BorderFactory.createTitledBorder("Home Phone #"));
        hPhoneTF.addActionListener(dcListener);
        
        cPhoneTF = new JTextField(8);
        cPhoneTF.setToolTipText("Volunteer cell phone #");
        cPhoneTF.setBorder(BorderFactory.createTitledBorder(" Cell Phone #"));
        cPhoneTF.addActionListener(dcListener);
                                          
        emailTF = new JTextField(17);
        emailTF.setToolTipText("Volunteer email address");
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
        streetnumTF.setToolTipText("Address of Volunteer");
        streetnumTF.setBorder(BorderFactory.createTitledBorder("St. #"));
        streetnumTF.addActionListener(dcListener);
        
        streetnameTF = new JTextField(13);
        streetnameTF.setToolTipText("Address of Volunteer");
        streetnameTF.setBorder(BorderFactory.createTitledBorder("Street"));
        streetnameTF.addActionListener(dcListener);
            
        unitTF = new JTextField(5);
        unitTF.setToolTipText("Address of Volunteer");
        unitTF.setBorder(BorderFactory.createTitledBorder("Unit #"));
        unitTF.addActionListener(dcListener);
        
        cityTF = new JTextField(7);
        cityTF.setToolTipText("Address of Volunteer");
        cityTF.setBorder(BorderFactory.createTitledBorder("City"));
        cityTF.addActionListener(dcListener);
        
        zipTF = new JTextField(4);
        zipTF.setToolTipText("Address of Volunteer");
        zipTF.setBorder(BorderFactory.createTitledBorder("Zip"));
        zipTF.addActionListener(dcListener);
        
        lblFamDel = new JLabel("0", JLabel.RIGHT);
        lblFamDel.setPreferredSize(new Dimension (52, 48));
        lblFamDel.setToolTipText("# Deliveries Delivery Volunteer Made");
        lblFamDel.setBorder(BorderFactory.createTitledBorder("# Del"));
        
        btnSignInHistory = new JRadioButton(gvs.getImageIcon(HISTORY_ICON_INDEX));
        btnSignInHistory.setToolTipText("Click to view volunteer's warehouse sign-in history");
        btnSignInHistory.setEnabled(false);
        btnSignInHistory.addActionListener(new SignInHistoryListener(this)); 
        
        op3.add(streetnumTF);
        op3.add(streetnameTF);
        op3.add(unitTF);
        op3.add(cityTF);
        op3.add(zipTF);
        op3.add(lblFamDel);
        op3.add(btnSignInHistory);
                
        commentTF = new JTextField(50);
//      commentTF.setPreferredSize(new Dimension (600, 48));
        commentTF.setToolTipText("Last comment from volunteer");
        commentTF.setBorder(BorderFactory.createTitledBorder("Last Volunteer Comment"));
        commentTF.addActionListener(dcListener);
       
        op4.add(commentTF);
        
        ckBoxActivities = new JCheckBox[13];
        ckBoxActivities[0] = new JCheckBox("Delivery Set-Up");
        ckBoxActivities[1] = new JCheckBox("Delivery");
        ckBoxActivities[2] = new JCheckBox("Warehouse Support");
        ckBoxActivities[3] = new JCheckBox("Packager");
        ckBoxActivities[4] = new JCheckBox("Gift Inventory");
        ckBoxActivities[5] = new JCheckBox("Shopping");
        ckBoxActivities[6] = new JCheckBox("Cookie Baker");
        ckBoxActivities[7] = new JCheckBox("Warehouse Clean-Up");
        ckBoxActivities[8] = new JCheckBox("Clothing");
        ckBoxActivities[9] = new JCheckBox("Corp Team Building");
        ckBoxActivities[10] = new JCheckBox("Bike Assembly");
        ckBoxActivities[11] = new JCheckBox("Post Delivery");
        ckBoxActivities[12] = new JCheckBox("Warehouse Set-Up");
        
        int bn;
        for(bn=0; bn < 5; bn++ )
        {
        	ckBoxActivities[bn].addActionListener(dcListener);
        	op5.add(ckBoxActivities[bn]);
        }	
        
        for(bn=5; bn < 9; bn++)
        {
        	ckBoxActivities[bn].addActionListener(dcListener);
        	op6.add(ckBoxActivities[bn]);
        }
        for(bn=9; bn < ckBoxActivities.length; bn++)
        {
        	ckBoxActivities[bn].addActionListener(dcListener);
        	op7.add(ckBoxActivities[bn]);
        }
       
        ckBoxPanel.add(op5);
        ckBoxPanel.add(op6);
        ckBoxPanel.add(op7);
            
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
		ONCVolunteer updateVol = new ONCVolunteer(currVolunteer);	//make a copy of current driver
		int bCD = 0; //used to indicate a change has been detected
		
		if(!drvNumTF.getText().equals(updateVol.getDrvNum()))
		{
			updateVol.setDrvNum(drvNumTF.getText());
			bCD = bCD | 1;
		}
		if(!firstnameTF.getText().equals(updateVol.getfName()))
		{
			updateVol.setfName(firstnameTF.getText());
			bCD = bCD | 2;
		}
		if(!lastnameTF.getText().equals(updateVol.getlName())) { updateVol.setlName(lastnameTF.getText()); bCD = bCD | 4; }
		if(!groupTF.getText().equals(updateVol.getGroup())) { updateVol.setGroup(groupTF.getText()); bCD = bCD | 8; }
		if(!hPhoneTF.getText().equals(updateVol.getHomePhone())) { updateVol.setHomePhone(hPhoneTF.getText()); bCD = bCD | 16; }
		if(!cPhoneTF.getText().equals(updateVol.getCellPhone())) { updateVol.setCellPhone(cPhoneTF.getText()); bCD = bCD | 32; }
		if(!emailTF.getText().equals(updateVol.getEmail())) { updateVol.setEmail(emailTF.getText()); bCD = bCD | 64; }
		if(!streetnumTF.getText().equals(updateVol.gethNum())) { updateVol.sethNum(streetnumTF.getText()); bCD = bCD | 128; }
		if(!streetnameTF.getText().equals(updateVol.getStreet())) { updateVol.setStreet(streetnameTF.getText()); bCD = bCD | 256; }		
		if(!unitTF.getText().equals(updateVol.getUnit())) { updateVol.setUnit(unitTF.getText()); bCD = bCD | 512; }
		if(!cityTF.getText().equals(updateVol.getCity())) { updateVol.setCity(cityTF.getText()); bCD = bCD | 1024; }
		if(!zipTF.getText().equals(updateVol.getZipcode())) { updateVol.setZipcode(zipTF.getText()); bCD = bCD | 2048; }
		if(!commentTF.getText().equals(updateVol.getComment())) { updateVol.setComment(commentTF.getText()); bCD = bCD | 4096; }
		if(generateActivityCode() != updateVol.getActivityCode()) { updateVol.setActivityCode(generateActivityCode()); bCD = bCD | 8192; }
		
		if(bCD > 0)	//If an update to organization data (not stop light data) was detected
		{
//			System.out.println(String.format("VolDlg.update: bCD= %d, volLN= %s", bCD, updateVol.getlName()));
			updateVol.setDateChanged(gvs.getTodaysDate());
			
			//request an update from the server
			String response = volDB.update(this, updateVol);
			
			if(response.startsWith("UPDATED_DRIVER"))
			{
				display(updateVol);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Volunteer Update," +
						"try again later","Volunteer Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
			
//			bCD = false;
		}
	}
	
	void display(ONCEntity volunteer)
	{	
		if(volDB.size() <= 0)
		{
			currVolunteer = null;
			clear();
			drvNumTF.setText("None");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
			btnSignInHistory.setEnabled(false);
		}
		else 
		{
			//Determine what to display based on currVolunteer and volunteer
			if(currVolunteer == null && volunteer == null)
				currVolunteer = volDB.getObjectAtIndex(0);
			else if(volunteer != null  && volunteer != currVolunteer)
				currVolunteer = (ONCVolunteer) volunteer;
			
			//display the current volunteer
			bIgnoreEvents = true;
			
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
			
			//Can only delete volunteer if the've never signed in and didn't make a delivery
			if(currVolunteer.getSignIns() == 0 && currVolunteer.getDelAssigned() == 0)
				btnDelete.setEnabled(true);
			else
				btnDelete.setEnabled(false);
			
			streetnumTF.setText(currVolunteer.gethNum());
			streetnameTF.setText(currVolunteer.getStreet());
			unitTF.setText(currVolunteer.getUnit());
			cityTF.setText(currVolunteer.getCity());
			zipTF.setText(currVolunteer.getZipcode());
			
			groupTF.setText(currVolunteer.getGroup());
			commentTF.setText(currVolunteer.getComment());
			
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm");
			lblLastSignIn.setText(currVolunteer.getSignIns() == 0 ? "Never" : sdf.format(currVolunteer.getDateChanged()));
			
			setActivities(currVolunteer.getActivityCode());
			
			btnSignInHistory.setEnabled(currVolunteer.getSignIns() > 0);

			nav.setStoplightEntity(currVolunteer);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			bIgnoreEvents = false;
		}
	}
	
	void setActivities(int activityCode)
	{
		if(activityCode > 0)
		{
			int cbIndex = 0;
			for(int mask = 1; mask <= ActivityCode.lastCode(); mask = mask << 1)
				ckBoxActivities[cbIndex++].setSelected((mask & activityCode) > 0);
		}		
		else
			clearActivities();
	}
	
	int generateActivityCode()
	{
		int activitycode = 0;
		int cbIndex = 0;
		for(int mask = 1; mask <= ActivityCode.lastCode(); mask = mask << 1)
			if(ckBoxActivities[cbIndex++].isSelected())
				activitycode = activitycode | mask;
			
		return activitycode;
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
		commentTF.setText("");
		groupTF.setText("");
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
		btnSignInHistory.setEnabled(false);
	}

	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Volunteer's Information"));
		clear();
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
		
		drvNumTF.setText("New");
	}
	
	void onDelete()
	{
		ONCVolunteer delVol = volDB.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("Are you sure you want to delete %s from the data base?", 
											delVol.getfName() + " " + delVol.getlName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Volunteer Database Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			String response = volDB.delete(this, delVol);
			
			if(response.startsWith("DELETED_DRIVER"))
			{
				processDeletedEntity(volDB);
			}
			else
			{
				String err_mssg = "ONC Server denied volunteer deletion request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete Volunteer Request Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currVolunteer);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new volunteer from user input	
		ONCVolunteer newVol = new ONCVolunteer(-1, "N/A", firstnameTF.getText(), lastnameTF.getText(),
					emailTF.getText(), streetnumTF.getText(), streetnameTF.getText(), 
					unitTF.getText(), cityTF.getText(), zipTF.getText(), 
					hPhoneTF.getText(), cPhoneTF.getText(), "1", 
					Integer.toHexString(generateActivityCode()), "", "",
					new Date(), userDB.getUserLNFI());
						
		//send add request to the local data base
		String response = volDB.add(this, newVol);
						
		if(response.startsWith("ADDED_DRIVER"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			ONCVolunteer addedVol = gson.fromJson(response.substring(12), ONCVolunteer.class);
							
			//set the display index, on, to the new volunteer added and display group
			display(addedVol);
			nav.setIndex(volDB.getListIndexByID(volDB.getList(), addedVol.getID()));
		}
		else
		{
			String err_mssg = "ONC Server denied add volunteer request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Volunteer Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currVolunteer);
		}
				
		//reset to review mode and display the proper volunteer
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Information"));
		entityPanel.setBackground(pBkColor);

		bAddingNewEntity = false;
		setControlState();
	}
	
	void onCancelNew()
	{
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Volunteer Information"));
		display(currVolunteer);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DRIVER"))
		{
			ONCVolunteer updatedVol = (ONCVolunteer) dbe.getObject1();
			
			//If a current volunteer is being displayed has changed, re-display it
			if(currVolunteer != null && currVolunteer.getID() == updatedVol.getID() && !bAddingNewEntity)
				display(updatedVol);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DRIVER"))
		{
			ONCVolunteer addedDriver = (ONCVolunteer) dbe.getObject1();
			//If no volunteer is being displayed, display the added one
			if(this.isVisible() && currVolunteer == null && volDB.size() > 0 && !bAddingNewEntity)
				display(addedDriver);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_DRIVER"))
		{
			//if the deleted volunteer was the only volunteer in data base, clear the display
			//otherwise, if the deleted volunteer is currently being displayed, change the
			//index to the previous volunteer in the database and display.
			if(volDB.size() == 0)
			{
				currVolunteer = null;
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCVolunteer deletedVol = (ONCVolunteer) dbe.getObject1();
				if(currVolunteer.getID() == deletedVol.getID())
				{
					if(nav.getIndex() == 0)
						nav.setIndex(volDB.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(volDB.getDriver(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DELIVERY"))
		{
			//If the added delivery is associated with the current volunteer being displayed,
			//update the display so the # of deliveries assigned field updates
			ONCFamilyHistory del = (ONCFamilyHistory) dbe.getObject1();
			
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
	 * If the FAMILY, WISH or VOLUNTEER entity selection event is fired 
	 * and the current mode is not adding a new volunteer, save any changes to the 
	 * currently displayed volunteer and display the volunteer
	 ************************************************************************************/
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.WISH)
			{
				ONCFamily fam = (ONCFamily) tse.getObject1();
				
				String logEntry = String.format("Volunteer Dialog Event: %s, ONC# %s selected",
						tse.getType(), fam.getONCNum());
				LogDialog.add(logEntry, "M");
				
				ONCFamilyHistory del = familyHistoryDB.getFamilyHistory(fam.getDeliveryID());
			
				if(del != null && !del.getdDelBy().isEmpty())
				{
					//There is a delivery volunteer assigned. Determine who it is from the driver
					//number and display that volunteer, if they have been entered into the 
					//volunteer data base.
					int index = volDB.getDriverIndex(del.getdDelBy());
					if(index > -1)
					{
						update();
						nav.setIndex(index);
						display(volDB.getDriver(index));
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
	 * This class implements a listener for the sign-in history button. When the button is
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
			//create and display sign-in history as a modal dialog.
			SignInHistoryDialog siHistoryDlg = new SignInHistoryDialog(owner, true);
//	        EntityEventManager.getInstance().registerEntitySelectionListener(siHistoryDlg);
	        
	        siHistoryDlg.setLocationRelativeTo(btnSignInHistory);
	        siHistoryDlg.display(currVolunteer);
	    	siHistoryDlg.setVisible(true);
	    	
//	    	EntityEventManager.getInstance().removeEntitySelectionListener(siHistoryDlg);
		}
	}
}
