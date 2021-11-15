package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.google.gson.Gson;

public class DistributionCenterDialog extends EntityDialog
{
	/***********************************************************************************************
	 * Implements a dialog to edit distribution centers. This dialog looks like the main screen.
	 * It allows the user to scroll thru the distribution center data base, search the data base 
	 * for distribution centers by name or zip code, edit the various fields that are contained in a
	 * distribution center object, and add and delete distribution centers from the data base.
	 * 
	 * The distribution center data base is implemented by the DistributionCenterDB class. 
	 *********************************************************************************************/
	private static final long serialVersionUID = 1L;
	
	private FamilyDB familyDB;
	
	private JLabel lblAssignedCount, lblDateChanged, lblChangedBy;
    private JTextField nameTF, acronymTF;
    private JTextField streetnumTF, streetnameTF, streettypeTF, cityTF, zipTF, urlTF;
    private DistributionCenterDB centerDB;
    
    private DistributionCenter currCenter;	//reference to object being displayed

	DistributionCenterDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Our Neighbor's Child - Distribution Center Information");
		
		centerDB = DistributionCenterDB.getInstance();
		
		//register to listen for data changed events
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		//Create a content panel for the dialog and add panel components to it.
        JPanel odContentPane = new JPanel();
        odContentPane.setLayout(new BoxLayout(odContentPane, BoxLayout.PAGE_AXIS));
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(parentFrame, centerDB);
        nav.setDefaultMssg("ONC Distribution Centers");
        nav.setCount1("Distribution Centers: " + Integer.toString(0));
        nav.setCount2("Assigned Families: " + Integer.toString(0));

        //set up the edit organization panel;
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
       
        //set up panel 1
        nameTF = new JTextField(24);
        nameTF.setBorder(BorderFactory.createTitledBorder("Name"));
        nameTF.addActionListener(dcListener);
        
        acronymTF = new JTextField(12);
        acronymTF.setBorder(BorderFactory.createTitledBorder("Acronym"));
        acronymTF.addActionListener(dcListener);
        
        lblAssignedCount = new JLabel();
        lblAssignedCount.setHorizontalAlignment(SwingConstants.CENTER);
        lblAssignedCount.setPreferredSize(new Dimension (152, 48));
        lblAssignedCount.setToolTipText("How many families have been assigned this center");
        lblAssignedCount.setBorder(BorderFactory.createTitledBorder("# Families Assigned"));
        
        op1.add(nameTF);
        op1.add(acronymTF);
        op1.add(lblAssignedCount);
        
        //set up panel 2
        streetnumTF = new JTextField(6);
        streetnumTF.setToolTipText("Address of distribution center");
        streetnumTF.setBorder(BorderFactory.createTitledBorder("St. #"));
        streetnumTF.addActionListener(dcListener);
        
        streetnameTF = new JTextField(14);
        streetnameTF.setToolTipText("Address of distribution center");
        streetnameTF.setBorder(BorderFactory.createTitledBorder("Street"));
        streetnameTF.addActionListener(dcListener);
            
        streettypeTF = new JTextField(7);
        streettypeTF.setToolTipText("Address of distribution center");
        streettypeTF.setBorder(BorderFactory.createTitledBorder("Type"));
        streettypeTF.addActionListener(dcListener);
        
        cityTF = new JTextField(9);
        cityTF.setToolTipText("Address of distribution cente");
        cityTF.setBorder(BorderFactory.createTitledBorder("City"));
        cityTF.addActionListener(dcListener);
        
        zipTF = new JTextField(5);
        zipTF.setToolTipText("Address of distribution cente");
        zipTF.setBorder(BorderFactory.createTitledBorder("Zip"));
        zipTF.addActionListener(dcListener);
        
        op2.add(streetnumTF);
        op2.add(streetnameTF);
        op2.add(streettypeTF);
        op2.add(cityTF);
        op2.add(zipTF);
        
        //set up panel 3     
        urlTF = new JTextField(28);
        urlTF.setToolTipText("Google Map link used in emails or SMS messages");
        urlTF.setBorder(BorderFactory.createTitledBorder("Google Map URL"));
        urlTF.addActionListener(dcListener);
       
        lblChangedBy = new JLabel();
        lblChangedBy.setPreferredSize(new Dimension (136, 48));
        lblChangedBy.setToolTipText("Who last changed this center's information");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Changed By"));
        
        lblDateChanged = new JLabel();
        lblDateChanged.setPreferredSize(new Dimension (136, 48));
        lblDateChanged.setToolTipText("When this information last changed");
        lblDateChanged.setBorder(BorderFactory.createTitledBorder("Date Changed"));
        
        op3.add(urlTF);
        op3.add(lblChangedBy);
        op3.add(lblDateChanged);
        
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New Center");
    	btnNew.setToolTipText("Click to add a new distribution center");
        
        btnDelete.setText("Delete Center");
    	btnDelete.setToolTipText("Click to delete this distribution center");
        
        btnSave.setText("Save New Center");
    	btnSave.setToolTipText("Click to save the new distribution center");
        
        btnCancel.setText("Cancel Add New Center");
    	btnCancel.setToolTipText("Click to cancel adding a new distriubiton center");
       
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        pack();
        setSize(new Dimension(780, 320));
        setResizable(true);
        Point pt = parentFrame.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}

	void display(ONCEntity center)	//displays currOrg
	{
		if(centerDB.size() <=0 )
		{
			currCenter = null;
			clear();
			nameTF.setText("No Centers Yet");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else
		{
			//Determine what to display based on currDriver and driver
			if(currCenter == null && center == null)
				currCenter = (DistributionCenter) centerDB.getObjectAtIndex(0);
			else if(center != null)
				currCenter = (DistributionCenter) center;
			
			bIgnoreEvents = true;
			
			nameTF.setText(currCenter.getName());
			nameTF.setCaretPosition(0);
			
			acronymTF.setText(currCenter.getAcronym());
			
			int assignedCount = familyDB.getNuberOfFamiliesAssignedToDistributionCenter(currCenter.getID());
			lblAssignedCount.setText(Integer.toString(assignedCount));
			btnDelete.setEnabled(assignedCount == 0);	//can only delete a center that hasn't been assigned
			
			streetnumTF.setText(currCenter.getStreetNum());
			streetnameTF.setText(currCenter.getStreet());
			streettypeTF.setText(currCenter.getSuffix());
			cityTF.setText(currCenter.getCity());
			zipTF.setText(currCenter.getZipcode());
			urlTF.setText(currCenter.getGoogleMapURL());
			urlTF.setCaretPosition(0);
			
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
			lblDateChanged.setText(sdf.format(currCenter.getTimestampDate()));
			
			lblChangedBy.setText(currCenter.getChangedBy());
		
			nav.setCount1("Distribution Centers: " + Integer.toString(centerDB.size()));
			nav.setCount2("Assigned Families: " + Integer.toString(familyDB.getNuberOfFamiliesAssignedToAnyDistributionCenter()));
			
			nav.setStoplightEntity(currCenter);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			bIgnoreEvents = false;
		}
	}
	
	void update()
	{
		//Check to see if user has changed any field, if so, save it
		DistributionCenter reqCenter;
		
		if(currCenter != null)
			reqCenter = new DistributionCenter(currCenter);	//make a copy for update request
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(this, "Edit Distribution Center Dialog Error:," +
					"No current center","Edit Distribution Center Dialog Error",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			return;	//If no current center, should never have gotten an update request
		}
		
		int bCD = 0; //used to indicate a change has been detected
		
		if(!nameTF.getText().equals(reqCenter.getName())) { reqCenter.setName(nameTF.getText()); bCD = bCD | 1; }
		if(!acronymTF.getText().equals(reqCenter.getAcronym())) { reqCenter.setAcronym(acronymTF.getText()); bCD = bCD | 2;}
		if(!streetnumTF.getText().equals(reqCenter.getStreetNum())) { reqCenter.setStreetNum(streetnumTF.getText()); bCD = bCD | 4; }
		if(!streetnameTF.getText().equals(reqCenter.getStreet())) {reqCenter.setStreet(streetnameTF.getText());bCD = bCD | 8; }
		if(!streettypeTF.getText().equals(reqCenter.getSuffix())) { reqCenter.setSuffix(streettypeTF.getText()); bCD = bCD | 16; }
		if(!cityTF.getText().equals(reqCenter.getCity())) { reqCenter.setCity(cityTF.getText()); bCD = bCD | 32; }
		if(!zipTF.getText().equals(reqCenter.getZipcode())) { reqCenter.setZipcode(zipTF.getText()); bCD = bCD | 64; }
		if(!urlTF.getText().equals(reqCenter.getGoogleMapURL())) { reqCenter.setGoogleMapURL(urlTF.getText()); bCD = bCD | 128; }
		
		if(bCD > 0)	//If an update to distribution center data (not stop light data) was detected
		{
			reqCenter.setDateChanged(System.currentTimeMillis());
			reqCenter.setChangedBy(userDB.getUserLNFI());
			
			String response = centerDB.update(this, reqCenter);	//notify the database of the change
			
			if(response.startsWith("UPDATED_CENTER"))
			{
				Gson gson = new Gson();
				DistributionCenter updatedCenter = gson.fromJson(response.substring(14), DistributionCenter.class);
				display(updatedCenter);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Distribution Center Update," +
						"try again later","Distribution Center Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currCenter);
			}
		}
	}

	void clear()
	{
		bIgnoreEvents = true;
		
		nameTF.setText("");		
		acronymTF.setText("");
		lblAssignedCount.setText("0");
		streetnumTF.setText("");
		streetnameTF.setText("");
		streettypeTF.setText("");
		cityTF.setText("");
		zipTF.setText("");
		urlTF.setText("");
		lblChangedBy.setText("");
		lblDateChanged.setText("");
		
		nav.clearStoplight();
		
		bIgnoreEvents = false;
	}
	
	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Distribution Center's Information"));
		clear();
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
		nameTF.setText("New Center Name");
	}
	
	void onDelete()
	{
		DistributionCenter delCenter = (DistributionCenter) centerDB.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("Are you sure you want to delete %s from the data base?", 
											delCenter.getName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Distribtion Center Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			DistributionCenter deletedCenter = centerDB.delete(this, delCenter);
			
			if(deletedCenter != null)
			{
				processDeletedEntity(centerDB);
			}
			else
			{
				String err_mssg = "ONC Server denied delete distribution center request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete Distribution Center Request Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currCenter);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new partner from user input	
		DistributionCenter newCenter = new DistributionCenter(-1, nameTF.getText(), acronymTF.getText(),
				streetnumTF.getText(), streetnameTF.getText(), streettypeTF.getText(), cityTF.getText(),
				zipTF.getText(), urlTF.getText(),  System.currentTimeMillis(), userDB.getUserLNFI(),
				3, "Partner Created", userDB.getUserLNFI());
		
		//send request to add new center to the local data base
		DistributionCenter addedCenter = (DistributionCenter) centerDB.add(this, newCenter);
		
		if(addedCenter != null)
		{
			//set the display index, on, to the new center added and display center
			nav.setIndex(centerDB.getListIndexByID(centerDB.getList(), addedCenter.getID()));
			display(addedCenter);
		}
		else
		{
			String err_mssg = "ONC Server denied add distribution center request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Distribution Center Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currCenter);
		}
		
		//reset to review mode and display the proper center
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Distribution Center Information"));
		entityPanel.setBackground(pBkColor);

		bAddingNewEntity = false;
		setControlState();
	}
	
	void onCancelNew()
	{
		nav.navSetEnabled(true);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Distribution Center Information"));
		display(currCenter);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	 
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(!bAddingNewEntity && dbe.getSource() != this && dbe.getType().equals("UPDATED_CENTER"))
		{
			DistributionCenter updatedCenter = (DistributionCenter) dbe.getObject1();
			
			//If current center is being displayed has changed, re-display it
			if(currCenter != null && currCenter.getID() == updatedCenter.getID())
				display(updatedCenter); 
		}
		else if(!bAddingNewEntity && dbe.getSource() != this && dbe.getType().equals("DELETED_CENTER"))
		{
			//if the deleted center was the only center in data base, clear the display
			//otherwise, if the deleted center is currently being displayed, change the on
			//to the next prior partner and display.
			if(centerDB.size() == 0)
			{
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				DistributionCenter deletedCenter = (DistributionCenter) dbe.getObject1();
				if(currCenter != null && currCenter.getID() == deletedCenter.getID())
				{
					if(nav.getIndex() == 0)
						nav.setIndex(centerDB.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(centerDB.getObjectAtIndex(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_DATABASE"))
		{
			display(null);
		}
		else if(!bAddingNewEntity && currCenter != null && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFamily = (ONCFamily) dbe.getObject1();
			
			//if updated family has a center assigned and it's this center, redisplay to 
			//keep the assigned count accurate
			if(currCenter != null && updatedFamily != null && currCenter.getID() == updatedFamily.getDistributionCenterID())
				display(currCenter);
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		/*************************************************************************************
		 * If the table selection event is fired  and the current mode is
		 * not adding a new center, save any changes to the currently displayed 
		 * center and display center selected.
		 ************************************************************************************/
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(tse.getSource() != nav && tse.getType() == EntityType.CENTER)
			{
				DistributionCenter center = (DistributionCenter) tse.getObject1();
				update();
				nav.setIndex(centerDB.getListIndexByID(centerDB.getList(),center.getID()));
				display(center);
			}
			else if(tse.getSource() == nav && tse.getType() == EntityType.CENTER)
			{
				update();
				display(centerDB.getObjectAtIndex(nav.getIndex()));
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.CENTER);
	}
}
