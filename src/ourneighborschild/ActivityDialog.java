package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.gson.Gson;

public class ActivityDialog extends EntityDialog 
{
	/**
	 * Implements a dialog to manage ONC volunteer activities
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_DATA_MSSG = "No Activities";
	
	//database references
	private ActivityDB activityDB;
	private VolunteerDB volunteerDB;
	private VolunteerActivityDB volActDB;

	//ui components
	private JLabel lblActID, lblTimestamp, lblChangedBy, lblVolCount;
	private JCheckBox openCkBox, reminderCkBox;
    private JTextField categoryTF,nameTF, locationTF, descriptionTF;
    private JSpinner startTimeSpinner, endTimeSpinner;
    private SpinnerDateModel startModel, endModel;
    private JButton btnSaveTimeChanges;
    private TimeChangeListener timeChangeListener;
    
    private Activity currActivity;	 //reference to the current object displayed
    
	public ActivityDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Volunteer Activities");
		
		//Initialize database object reference variables and register listeners
		activityDB = ActivityDB.getInstance();
		if(activityDB != null)
			activityDB.addDatabaseListener(this);
		
		volunteerDB = VolunteerDB.getInstance();
		if(volunteerDB != null)
			volunteerDB.addDatabaseListener(this);
		
		volActDB = VolunteerActivityDB.getInstance();
		if(volActDB != null)
			volActDB.addDatabaseListener(this);
		
		currActivity = null;

        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, activityDB);
        nav.setDefaultMssg("Our Neighbor's Child Volunteer Activities");
        nav.setCount1("Activities: " + Integer.toString(0));
        nav.setCount2("Volunteers: " + Integer.toString(0));

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Activity Description"));
        JPanel op1 = new JPanel();
        op1.setLayout(new BoxLayout(op1, BoxLayout.X_AXIS));
        JPanel op2 = new JPanel();
        op2.setLayout(new BoxLayout(op2, BoxLayout.X_AXIS));
        JPanel op3 = new JPanel();
        op3.setLayout(new BoxLayout(op3, BoxLayout.X_AXIS));
        JPanel op4 = new JPanel();
        op4.setLayout(new BoxLayout(op4, BoxLayout.X_AXIS));

        lblActID = new JLabel("None", SwingConstants.CENTER);
        lblActID.setBorder(BorderFactory.createTitledBorder("ID"));
        lblActID.setPreferredSize(new Dimension(48, 48));
       
        nameTF = new JTextField();
        nameTF.setBorder(BorderFactory.createTitledBorder("Name"));
        nameTF.setPreferredSize(new Dimension(304, 48));
        nameTF.addActionListener(dcListener);
        
        categoryTF = new JTextField();
        categoryTF.setText(NO_DATA_MSSG);
        categoryTF.setBorder(BorderFactory.createTitledBorder("Category"));
        categoryTF.setPreferredSize(new Dimension(96, 48));
        categoryTF.addActionListener(dcListener);
        
        lblVolCount = new JLabel("None", SwingConstants.CENTER);
        lblVolCount.setBorder(BorderFactory.createTitledBorder("# Volunteers"));
        lblVolCount.setToolTipText("Number of volunteers who have signed up for this activity");
        lblVolCount.setPreferredSize(new Dimension(96, 48));
        
        op1.add(lblActID);
        op1.add(nameTF);
        op1.add(categoryTF);
        op1.add(lblVolCount);
       
        descriptionTF = new JTextField(62);
        descriptionTF.setToolTipText("Detailed description of activity");
        descriptionTF.setBorder(BorderFactory.createTitledBorder("Detailed Description"));
        descriptionTF.addActionListener(dcListener);
        
        op2.add(descriptionTF); 
        
        timeChangeListener = new TimeChangeListener();
        ActivityActionListener actActionListener = new ActivityActionListener();
       
        JPanel startTimePanel = new JPanel();
        startTimePanel.setLayout(new BoxLayout(startTimePanel, BoxLayout.X_AXIS));
        
        startModel = new SpinnerDateModel();
        startTimeSpinner = new JSpinner( startModel);
//      startTimeSpinner.setMinimumSize(new Dimension(96, 48));
        JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "EEE M/dd/yy h:mm a");
        startTimeSpinner.setEditor(startTimeEditor);

        startTimePanel.setBorder(BorderFactory.createTitledBorder("Start Date/Time"));
        startTimePanel.add(startTimeSpinner);
        
        JPanel endTimePanel = new JPanel();
        endTimePanel.setLayout(new BoxLayout(endTimePanel, BoxLayout.X_AXIS));

        endModel = new SpinnerDateModel();
        endTimeSpinner = new JSpinner( endModel);
//      endTimeSpinner.setMinimumSize(new Dimension(96, 48));
        JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "EEE M/dd/yy h:mm a");
        endTimeSpinner.setEditor(endTimeEditor);
        
        endTimePanel.setBorder(BorderFactory.createTitledBorder("End Date/Time"));
        endTimePanel.add(endTimeSpinner);
        
        btnSaveTimeChanges = new JButton("Save Time Changes");
        btnSaveTimeChanges.setToolTipText("Click to save changes made to activity start or end times");
        btnSaveTimeChanges.setEnabled(false);
    		btnSaveTimeChanges.addActionListener(actActionListener);

        op3.add(startTimePanel);
        op3.setBorder(BorderFactory.createTitledBorder("Activity Start & End Dates/Times"));
        op3.add(endTimePanel);
        op3.add(btnSaveTimeChanges);
       
        locationTF = new JTextField(14);
        locationTF.setToolTipText("Location activity is performed");
        locationTF.setBorder(BorderFactory.createTitledBorder("Location"));
        locationTF.addActionListener(dcListener);
        
        JPanel ckBoxPanel = new JPanel();
        ckBoxPanel.setLayout(new BoxLayout(ckBoxPanel, BoxLayout.Y_AXIS));
        
        openCkBox = new JCheckBox("Open to volunteers?");
        openCkBox.setToolTipText("Check to enable volunteers to sign-up via website");
        openCkBox.addActionListener(actActionListener);
        ckBoxPanel.add(openCkBox);

        reminderCkBox = new JCheckBox("Send volunteers a reminder?");
        reminderCkBox.setToolTipText("Check to automatically generate reminder email 24 hours before start");
        reminderCkBox.addActionListener(actActionListener);
        ckBoxPanel.add(reminderCkBox);
        
        lblTimestamp = new JLabel("0", JLabel.RIGHT);
        lblTimestamp.setPreferredSize(new Dimension (128, 48));
        lblTimestamp.setToolTipText("Date activity last changed");
        lblTimestamp.setBorder(BorderFactory.createTitledBorder("Last Changed"));
        
        lblChangedBy = new JLabel("0", JLabel.RIGHT);
        lblChangedBy.setPreferredSize(new Dimension (128, 48));
        lblChangedBy.setToolTipText("ONC Elf who last changed the activity");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
       
        op4.add(locationTF);
        op4.add(ckBoxPanel);
        op4.add(lblTimestamp);
        op4.add(lblChangedBy);
        
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        entityPanel.add(op4);
        
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
		Activity reqUpdateAct = new Activity(currActivity);	//make a copy of current driver
		int bCD = 0; //used to indicate a change has been detected
		
		if(!categoryTF.getText().equals(currActivity.getCategory())) { reqUpdateAct.setName(categoryTF.getText()); bCD = bCD | 1; }
		if(!nameTF.getText().equals(currActivity.getName())) { reqUpdateAct.setName(nameTF.getText()); bCD = bCD | 2; }
		if(!locationTF.getText().equals(currActivity.getLocation())) { reqUpdateAct.setLocation(locationTF.getText()); bCD = bCD | 4; }
		if(!descriptionTF.getText().equals(currActivity.getDescription())) { reqUpdateAct.setDescription(descriptionTF.getText()); bCD = bCD | 8; }
		if(getSpinnerTimeInMillis(startModel) != currActivity.getStartDate()) { reqUpdateAct.setStartDate(getSpinnerTimeInMillis(startModel)); bCD = bCD | 16; }
		if(getSpinnerTimeInMillis(endModel) != currActivity.getEndDate()) { reqUpdateAct.setEndDate(getSpinnerTimeInMillis(endModel)); bCD = bCD | 32; }
		if(openCkBox.isSelected() != currActivity.isOpen()) { reqUpdateAct.setOpen(openCkBox.isSelected()); bCD = bCD | 64; }
		if(reminderCkBox.isSelected() != currActivity.sendReminder()) { reqUpdateAct.setReminder(reminderCkBox.isSelected()); bCD = bCD | 128; }
		
		if(bCD > 0)	//If an update to organization data (not stop light data) was detected
		{
//			System.out.println(String.format("ActDlg.update: bCD= %d", bCD));
			reqUpdateAct.setDateChanged(System.currentTimeMillis());
			
			//request an update from the server
			String response = activityDB.update(this, reqUpdateAct);
			
			if(response.startsWith("UPDATED_ACTIVITY"))
				display(reqUpdateAct);
			else
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied activity update," +
						"try again later","Activity Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
	}
	
	void display(ONCEntity activity)
	{	
		startModel.removeChangeListener(timeChangeListener);
		endModel.removeChangeListener(timeChangeListener);
		btnSaveTimeChanges.setEnabled(false);
		
		if(activityDB.size() <= 0)
		{
			currActivity = null;
			clear();
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else 
		{
			//Determine what to display based on currActivity and activity. If currActivity is
			//null, the set it to the first activity in the database. If currActviity is not null
			//and activity isn't null, then set currActivity = activity;
			if(currActivity == null)
				currActivity = (Activity) activityDB.getObjectAtIndex(0);
			else if(activity != null)
				currActivity =  (Activity) activity;
			
			//check if activity can be deleted
			int nVolunteers = volActDB.getVolunteerCountForActivity(currActivity.getID());
			btnDelete.setEnabled(nVolunteers == 0);
			 
			//display the current volunteer
			bIgnoreEvents = true;
			
			lblActID.setText(Integer.toString(currActivity.getID()));
			categoryTF.setText(currActivity.getCategory());
			categoryTF.setCaretPosition(0);
			nameTF.setText(currActivity.getName());
			nameTF.setCaretPosition(0);
			openCkBox.setSelected(currActivity.isOpen());
			lblVolCount.setText(Integer.toString(nVolunteers));
			
			Calendar startCal = Calendar.getInstance();
			startCal.setTimeInMillis(currActivity.getStartDate());
			startTimeSpinner.setValue(startCal.getTime());
			
			Calendar endCal = Calendar.getInstance();
			endCal.setTimeInMillis(currActivity.getEndDate());
			endTimeSpinner.setValue(endCal.getTime());

			reminderCkBox.setSelected(currActivity.sendReminder());
			
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d h:mm a", Locale.US);
			Calendar localTimeStamp = createLocalCalendarFromGMT(currActivity.getTimestamp());
			lblTimestamp.setText(sdf.format(localTimeStamp.getTime()));
			
			lblChangedBy.setText(currActivity.getChangedBy());
			
			descriptionTF.setText(currActivity.getDescription());
			descriptionTF.setCaretPosition(0);
			locationTF.setText(currActivity.getLocation());
			locationTF.setCaretPosition(0);
			
			nav.setStoplightEntity(currActivity);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			startModel.addChangeListener(timeChangeListener);
			endModel.addChangeListener(timeChangeListener);
			
			bIgnoreEvents = false;
		}
		
		//update the counts in the nav bar
		nav.setCount1("Activities: " + Integer.toString(activityDB.size()));
        nav.setCount2("Volunteers: " + Integer.toString(volunteerDB.size()));
	}
	
	void clear()
	{	
		lblActID.setText("");
		nameTF.setText("");
		categoryTF.setText("");
		openCkBox.setSelected(false);
		descriptionTF.setText("");
		
		startModel.removeChangeListener(timeChangeListener);
		endModel.removeChangeListener(timeChangeListener);
		btnSaveTimeChanges.setEnabled(false);
		
		Calendar startCal = Calendar.getInstance();
		startTimeSpinner.setValue(startCal.getTime());
		
		Calendar endCal = Calendar.getInstance();
		endTimeSpinner.setValue(endCal.getTime());
		
		startModel.addChangeListener(timeChangeListener);
		endModel.addChangeListener(timeChangeListener);
		
		locationTF.setText("");
		reminderCkBox.setSelected(false);
		lblTimestamp.setText("");
		lblChangedBy.setText("");
		nav.clearStoplight();
		
		currActivity = null;
	}
	
	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Activities Information"));
		clear();
		lblActID.setText("New");
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
	}

	@Override
	void onDelete()
	{	
		Activity delAct = activityDB.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("<html>Are you sure you want to delete<br>%s<br>from the data base?</html>", 
											delAct.getName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Volunteer Activity Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			String response = activityDB.delete(this, delAct);
			
			if(response.startsWith("DELETED_ACTIVITY"))
			{
				processDeletedEntity(activityDB);
			}
			else
			{
				String err_mssg = "ONC Server denied activity deletion request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete ActivityRequest Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currActivity);
			}
		}		
	}
	
	void onSaveNew()
	{
		//construct a new volunteer activity from user input. Comment field is empty
		
		Activity newAct = new Activity(-1, -1, categoryTF.getText(), nameTF.getText(),
					getSpinnerTimeInMillis(startModel),getSpinnerTimeInMillis(endModel),
					locationTF.getText(), descriptionTF.getText(), "", 
					openCkBox.isSelected(), reminderCkBox.isSelected(),
					userDB.getUserLNFI()); 
						
		//send add request to the local data base
		String response = activityDB.add(this, newAct);
						
		if(response.startsWith("ADDED_ACTIVITY"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			Activity addedAct = gson.fromJson(response.substring(14), Activity.class);
							
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
				Activity updatedAct = (Activity) dbe.getObject1();
				
				//If a current activity is being displayed has changed, re-display it
				if(currActivity != null && currActivity.getID() == updatedAct.getID())
					display(updatedAct);
			}
			else if(dbe.getSource() != this && dbe.getType().equals("ADDED_ACTIVITY"))
			{
				Activity addedActivity = (Activity) dbe.getObject1();
				
				//If no activity is being displayed, display the added one
				if(currActivity == null && activityDB.size() > 0)
					display(addedActivity);
				else
				{
					//update the counts in the nav bar
					nav.setCount1("Activities: " + Integer.toString(activityDB.size()));
			        nav.setCount2("Volunteers: " + Integer.toString(volunteerDB.size()));
				}
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
					
					//update the counts in the nav bar
					nav.setCount1("Activities: " + Integer.toString(activityDB.size()));
			        nav.setCount2("Volunteers: " + Integer.toString(volunteerDB.size()));
				}
				else
				{
					Activity deletedAct = (Activity) dbe.getObject1();
					if(currActivity.getID() == deletedAct.getID())
					{
						if(nav.getIndex() == 0)
							nav.setIndex(activityDB.size() - 1);
						else
							nav.setIndex(nav.getIndex() - 1);
					
						display(activityDB.getActivity(nav.getIndex()));
					}
					else
					{
						//update the counts in the nav bar
						nav.setCount1("Activities: " + Integer.toString(activityDB.size()));
				        nav.setCount2("Volunteers: " + Integer.toString(volunteerDB.size()));
					}
				}
			}
			else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DRIVER"))
			{
				nav.setCount2("Volunteers: " + Integer.toString(volunteerDB.size()));
			}
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
				Activity activity = (Activity) tse.getObject1();
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
	
	long getSpinnerTimeInMillis(SpinnerDateModel model)
	{
		Date date = model.getDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
	}

	/****************************************************************************************
	 * Takes the time milliseconds (GMT) and returns a local time zone 
	 * Calendar object of the date
	 * @param gmt
	 * @return
	 ***************************************************************************************/
	Calendar createLocalCalendarFromGMT(long gmt)
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
	
	private class ActivityActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent ae)
		{	
			if(currActivity != null && !bIgnoreEvents && !bAddingNewEntity)
				update();
		}	
	}
	
	private class TimeChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			Date date = ((SpinnerDateModel)e.getSource()).getDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            long changeTimeInMillis = calendar.getTimeInMillis();
            
            boolean timeChangesEnabled = e.getSource() == startModel && changeTimeInMillis != currActivity.getStartDate() ||
            									e.getSource() == endModel && changeTimeInMillis != currActivity.getEndDate();
            btnSaveTimeChanges.setEnabled(timeChangesEnabled);	
		}	
	}
}
