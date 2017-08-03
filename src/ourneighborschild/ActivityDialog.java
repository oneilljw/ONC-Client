package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import com.google.gson.Gson;
import com.toedter.calendar.JDateChooser;

public class ActivityDialog extends EntityDialog 
{
	/**
	 * Implements a dialog to manage ONC volunteer activities
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_DATA_MSSG = "No Activities";
	
	//database references
	private ActivityDB activityDB;

	//ui components
	private JLabel lblTimestamp, lblChangedBy;
    private JTextField categoryTF,nameTF, locationTF, descriptionTF;
    private JDateChooser startDC, endDC;
    private JSpinner startTimeSpinner, endTimeSpinner;
    
    private VolunteerActivity currActivity;	 //reference to the current object displayed
    
	public ActivityDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Volunteer Activities");
		
		//Initialize database object reference variables and register listeners
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		currActivity = null;
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, activityDB);
        nav.setDefaultMssg("Our Neighbor's Child Volunteer Activities");
        nav.setCount1("Activities: " + Integer.toString(0));
        nav.setCount2("");
        nav.setNextButtonText("Next Activity");
        nav.setPreviousButtonText("Previous Activity");

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Activity Description"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel();
        op3.setLayout(new BoxLayout(op3, BoxLayout.X_AXIS));
       
        nameTF = new JTextField(32);
        nameTF.setBorder(BorderFactory.createTitledBorder("Name"));
        nameTF.addActionListener(dcListener);
        
        categoryTF = new JTextField(12);
        categoryTF.setText(NO_DATA_MSSG);
        categoryTF.setBorder(BorderFactory.createTitledBorder("Category"));
        categoryTF.addActionListener(dcListener);
        
        locationTF = new JTextField(14);
        locationTF.setToolTipText("Location activity is performed");
        locationTF.setBorder(BorderFactory.createTitledBorder("Location"));
        locationTF.addActionListener(dcListener);
       
        op1.add(nameTF);
        op1.add(categoryTF);
        op1.add(locationTF);
       
        descriptionTF = new JTextField(62);
        descriptionTF.setToolTipText("Detailed description of activity");
        descriptionTF.setBorder(BorderFactory.createTitledBorder("Detailed Description"));
        descriptionTF.addActionListener(dcListener);
        
        op2.add(descriptionTF); 
        
        DateChangeListener dateListener = new DateChangeListener();
    
        JPanel startTimePanel = new JPanel();
        startTimePanel.setLayout(new BoxLayout(startTimePanel, BoxLayout.X_AXIS));
        startDC = new JDateChooser(gvs.getTodaysDate());
        startDC.setMinimumSize(new Dimension(136, 48));
        startDC.setEnabled(false);
        startDC.getDateEditor().addPropertyChangeListener(dateListener);
        
        startTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "hh:mm a");
        startTimeSpinner.setEditor(startTimeEditor);
        
        startTimePanel.setBorder(BorderFactory.createTitledBorder("Activity Start"));
        startTimePanel.add(startDC);
        startTimePanel.add(startTimeSpinner);
        
        JPanel endTimePanel = new JPanel();
        endTimePanel.setLayout(new BoxLayout(endTimePanel, BoxLayout.X_AXIS));
        endDC = new JDateChooser(gvs.getTodaysDate());
        endDC.setMinimumSize(new Dimension(136, 48));
        endDC.setEnabled(false);
        endDC.getDateEditor().addPropertyChangeListener(dateListener);
        
        endTimeSpinner = new JSpinner( new SpinnerDateModel());
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "hh:mm a");
        endTimeSpinner.setEditor(endTimeEditor);
        
        endTimePanel.setBorder(BorderFactory.createTitledBorder("Activity End"));
        endTimePanel.add(endDC);
        endTimePanel.add(endTimeSpinner);

        lblTimestamp = new JLabel("0", JLabel.RIGHT);
        lblTimestamp.setPreferredSize(new Dimension (128, 48));
        lblTimestamp.setToolTipText("Date activity last changed");
        lblTimestamp.setBorder(BorderFactory.createTitledBorder("Last Changed"));
        
        lblChangedBy = new JLabel("0", JLabel.RIGHT);
        lblChangedBy.setPreferredSize(new Dimension (128, 48));
        lblChangedBy.setToolTipText("ONC Elf who last changed the activity");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));

        op3.add(startTimePanel);
        op3.add(endTimePanel);
        op3.add(lblTimestamp);
        op3.add(lblChangedBy);
        
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        
        //Set up control panel
        btnNew.setText("Add New Activity");
    	btnNew.setToolTipText("Click to add a new activity");
     
        btnDelete.setText("Delete Activity");
    	btnDelete.setToolTipText("Click to delete this activity");
    	
        btnSave.setText("Save New Activity");
    	btnSave.setToolTipText("Click to save the new activity");
        
        btnCancel.setText("Cancel Add New Activity");
    	btnCancel.setToolTipText("Click to cancel adding a new activity");

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
		VolunteerActivity reqUpdateAct = new VolunteerActivity(currActivity);	//make a copy of current driver
		int bCD = 0; //used to indicate a change has been detected
		
		if(!categoryTF.getText().equals(currActivity.getCategory())) { reqUpdateAct.setName(categoryTF.getText()); bCD = bCD | 1; }
		if(!nameTF.getText().equals(currActivity.getName())) { reqUpdateAct.setName(nameTF.getText()); bCD = bCD | 2; }
		if(!locationTF.getText().equals(currActivity.getLocation())) { reqUpdateAct.setLocation(locationTF.getText()); bCD = bCD | 4; }
		if(!descriptionTF.getText().equals(currActivity.getDescription())) { reqUpdateAct.setDescription(descriptionTF.getText()); bCD = bCD | 8; }
		if(hasDateChanged())
		{ 
			reqUpdateAct.setStartTime(convertLocalCalendarToGMT(startDC.getCalendar()));
			reqUpdateAct.setEndTime(convertLocalCalendarToGMT(endDC.getCalendar()));
			bCD = bCD | 16;
		}
		
		if(bCD > 0)	//If an update to organization data (not stop light data) was detected
		{
			reqUpdateAct.setDateChanged(gvs.getTodaysDate());
			
			//request an update from the server
			String response = activityDB.update(this, reqUpdateAct);
			
			if(response.startsWith("UPDATED_ACTIVITY"))
			{
				display(reqUpdateAct);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied activity update," +
						"try again later","Activity Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
	}
	
	void display(ONCEntity activity)
	{	
		if(activityDB.size() <= 0)
		{
			currActivity = null;
			clear();
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else 
		{
			//Determine what to display based on currActivity and activity
			if(currActivity == null)
				currActivity = (VolunteerActivity) activityDB.getObjectAtIndex(0);	
			else
				currActivity = (VolunteerActivity) activity;
			//enable the date choosers
			startDC.setEnabled(true);
			endDC.setEnabled(true);
			 
			//display the current volunteer
			bIgnoreEvents = true;
			
			categoryTF.setText(currActivity.getCategory());
			categoryTF.setCaretPosition(0);
			nameTF.setText(currActivity.getName());
			nameTF.setCaretPosition(0);
			
			startDC.setCalendar(convertDOBFromGMT(currActivity.getStartTimeInMillis()));
			startTimeSpinner.setValue(currActivity.getStartTime().getTime());
			
			endDC.setCalendar(convertDOBFromGMT(currActivity.getEndTimeInMillis()));
			endTimeSpinner.setValue(currActivity.getEndTime().getTime());
			
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d h:mm a");
			lblTimestamp.setText(sdf.format(currActivity.getDateChanged()));
			lblChangedBy.setText(currActivity.getChangedBy());
			
			//Can only delete activity if there are no volunteers
//			if(currActivity.getSignIns() == 0 && currActivity.getDelAssigned() == 0)
//				btnDelete.setEnabled(true);
//			else
				btnDelete.setEnabled(false);
			
			descriptionTF.setText(currActivity.getDescription());
			descriptionTF.setCaretPosition(0);
			locationTF.setText(currActivity.getLocation());
			locationTF.setCaretPosition(0);
			
			nav.setStoplightEntity(currActivity);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			bIgnoreEvents = false;
		}
	}
	
	void clear()
	{
		categoryTF.setText("");
		nameTF.setText("");
		locationTF.setText("");
		startDC.setCalendar(Calendar.getInstance());
		endDC.setCalendar(Calendar.getInstance());
		lblTimestamp.setText("");
		lblChangedBy.setText("");	
		descriptionTF.setText("");
		nav.clearStoplight();
		
		currActivity = null;
	}

	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Activities Information"));
		clear();
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
		
	}

	@Override
	void onDelete()
	{
/*	
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
				display(currActivity);
			}
		}
*/		
	}
	
	void onSaveNew()
	{
		//construct a new volunteer activity from user input	
		VolunteerActivity newAct = new VolunteerActivity(-1, categoryTF.getText(), nameTF.getText(),
					convertLocalCalendarToGMT(startDC.getCalendar()), 
					convertLocalCalendarToGMT(endDC.getCalendar()),
					locationTF.getText(), descriptionTF.getText(), userDB.getUserLNFI()); 
						
		//send add request to the local data base
		String response = activityDB.add(this, newAct);
						
		if(response.startsWith("ADDED_ACTIVITY"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			VolunteerActivity addedAct = gson.fromJson(response.substring(14), VolunteerActivity.class);
							
			//set the display index, on, to the new volunteer added and display group
			display(addedAct);
			nav.setIndex(activityDB.getListIndexByID(activityDB.getList(), addedAct.getID()));
		}
		else
		{
			String err_mssg = "ONC Server denied add activity request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Activity Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currActivity);
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
		entityPanel.setBorder(BorderFactory.createTitledBorder("Activity Description"));
		display(currActivity);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	
	public void dataChanged(DatabaseEvent dbe)
	{
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(dbe.getSource() != this && dbe.getType().equals("UPDATED_ACTIVITY"))
			{
				VolunteerActivity updatedAct = (VolunteerActivity) dbe.getObject1();
			
				//If a current activity is being displayed has changed, re-display it
				if(currActivity != null && currActivity.getID() == updatedAct.getID())
					display(updatedAct);
			}
			else if(dbe.getSource() != this && dbe.getType().equals("ADDED_ACTIVITY"))
			{
				VolunteerActivity addedActivity = (VolunteerActivity) dbe.getObject1();
				//If no activity is being displayed, display the added one
				if(currActivity == null && activityDB.size() > 0)
					display(addedActivity);
			}
			else if(dbe.getSource() != this && dbe.getType().equals("DELETED_ACTIVITY"))
			{
				//if the deleted activity was the only one in the data base, clear the display
				//otherwise, if the deleted activity is currently being displayed, change the
				//index to the previous activity in the database and display.
				if(activityDB.size() == 0)
				{
					currActivity = null;
					nav.setIndex(0);
					clear();
					btnDelete.setEnabled(false);
				}
				else
				{
					VolunteerActivity deletedAct = (VolunteerActivity) dbe.getObject1();
					if(currActivity.getID() == deletedAct.getID())
					{
						if(nav.getIndex() == 0)
							nav.setIndex(activityDB.size() - 1);
						else
							nav.setIndex(nav.getIndex() - 1);
					
						display(activityDB.getActivity(nav.getIndex()));
					}
				}
			}
//			else if(dbe.getSource() != this && dbe.getType().equals("LOADED_ACTIVITIES"))
//			{
//				if(activityDB.size() > 0)
//				{
//					VolunteerActivity firstActivity = (VolunteerActivity) activityDB.getObjectAtIndex(0);
//					nav.setIndex(0);
//					display(firstActivity);
//				}
//			}
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
/*			
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
*/			
			if(tse.getType() == EntityType.ACTIVITY)
			{
//				System.out.println(String.format("ActDlg.entitySelected: type: %s, source: %s",
//						tse.getType().toString(), tse.getSource().toString()));
				VolunteerActivity activity = (VolunteerActivity) tse.getObject1();
				update();
				display(activity);
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.ACTIVITY);
	}
	
	boolean hasDateChanged()
	{
		return convertLocalCalendarToGMT(startDC.getCalendar()) != currActivity.getStartTimeInMillis() ||
				convertLocalCalendarToGMT(endDC.getCalendar()) != currActivity.getEndTimeInMillis();
	}
	
	/****************************************************************************************
	 * Takes a local time zone Calendar date of birth and returns the date of birth 
	 * in milliseconds (GMT)
	 * Calendar object of the date of birth
	 * @param gmtDOB
	 * @return
	 ***************************************************************************************/
	long convertLocalCalendarToGMT(Calendar localCal)
	{
		//gives you the current offset in ms from GMT at the current date
		localCal.set(Calendar.SECOND, 0);
		localCal.set(Calendar.MILLISECOND, 0);
		
		TimeZone tz = localCal.getTimeZone();
		int offsetFromUTC = tz.getOffset(localCal.getTimeInMillis());
		return localCal.getTimeInMillis() + offsetFromUTC;
	}
	
	/****************************************************************************************
	 * Takes the time milliseconds (GMT) and returns a local time zone 
	 * Calendar object of the date
	 * @param gmt
	 * @return
	 ***************************************************************************************/
	Calendar convertDOBFromGMT(long gmt)
	{
		//gives you the current offset in ms from GMT at the current date
		TimeZone tz = TimeZone.getDefault();	//Local time zone
		int offsetFromUTC = tz.getOffset(gmt);

		//create a new calendar in local time zone, set to gmtDOB and add the offset
		Calendar localCal = Calendar.getInstance();
		localCal.setTimeInMillis(gmt);
		localCal.add(Calendar.MILLISECOND, (offsetFromUTC * -1));

		return localCal;
	}
	
	private class DateChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(currActivity != null && !bIgnoreEvents && !bAddingNewEntity && 
			   (pce.getSource() == startDC.getDateEditor() || pce.getSource() == endDC.getDateEditor()) && 
				"date".equals(pce.getPropertyName()))				  
			{
				update();
			}		
		}
	}
}
