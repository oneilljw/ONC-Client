package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

public class ChildPanel extends JPanel implements DatabaseListener, EntitySelectionListener
{
	/**
	 * This class extends JPanel to provide the UI for display and edit of a child
	 * The child's first and last name, gender, date of birth, age and school are displayed and
	 * all but age can be changed by the user
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_MAX_CHILD_AGE = 21; //Used to highlight out of age range children
	
	private ONCChild dispChild = null;	//The panel needs to know which child is being displayed for listeners
	
	//databases
	private GlobalVariables gvs;
    private FamilyDB fDB;
	private ChildDB cDB;
	private UserDB userDB;
	
	//GUI elements
	private JTextField firstnameTF;
	private JTextField lastnameTF, schoolTF, genderTF;
	private JDateChooser dobDC;
	private JTextField ageTF;
	private Color ageNormalBackgroundColor;
	
	//Semaphores
	private boolean bChildDataChanging = false; //flag indicates child data displayed is updating
	
	public ChildPanel()
	{	
		//register database listeners for updates
		gvs = GlobalVariables.getInstance();
    	fDB = FamilyDB.getInstance();
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		cDB = ChildDB.getInstance();
		if(cDB != null)
			cDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		
		//Set layout and border for the Child Panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setToolTipText("Information for an ONC child");
		this.setBorder(BorderFactory.createTitledBorder("Child Information"));
		
		//Initialize class variables
		//Create a listener for panel gui events
		ChildUpdateListener cuListener = new ChildUpdateListener();
		
		//Setup sub panels that comprise the Child Panel
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		//Set up the child info text fields and age label
        firstnameTF = new JTextField();
        firstnameTF.setPreferredSize(new Dimension(112, 28));
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.setEditable(false);
        firstnameTF.addActionListener(cuListener);
        
        lastnameTF = new JTextField();
        lastnameTF.setPreferredSize(new Dimension(120, 28));
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.setEditable(false);
        lastnameTF.addActionListener(cuListener);
        
        dobDC = new JDateChooser(gvs.getTodaysDate());
		dobDC.setMinimumSize(new Dimension(136, 48));
		dobDC.setBorder(BorderFactory.createTitledBorder("Date of Birth"));
		dobDC.setEnabled(false);
		dobDC.getDateEditor().addPropertyChangeListener(cuListener);
        
        ageTF = new JTextField();
        ageTF.setPreferredSize(new Dimension(72, 28));
        ageTF.setBorder(BorderFactory.createTitledBorder("Age on 12/25"));
        ageNormalBackgroundColor = ageTF.getBackground();
        
        schoolTF = new JTextField();
        schoolTF.setPreferredSize(new Dimension(112, 28));
        schoolTF.setBorder(BorderFactory.createTitledBorder("School"));
        schoolTF.setEditable(false);
        schoolTF.addActionListener(cuListener);
        
        genderTF = new JTextField();
        genderTF.setPreferredSize(new Dimension(48, 28));
        genderTF.setBorder(BorderFactory.createTitledBorder("Gender"));
        genderTF.setEditable(false);
        genderTF.addActionListener(cuListener);
   
        //add the gui items to the child info panel             
        this.add(firstnameTF);
        this.add(lastnameTF);
        this.add(dobDC);
        this.add(ageTF);
        this.add(schoolTF);
        this.add(genderTF);
	}
	
	void setEditableGUIFields(boolean tf)
	{
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
		{
			firstnameTF.setEditable(tf);
			lastnameTF.setEditable(tf);
			dobDC.setEnabled(true);
			schoolTF.setEditable(tf);
			genderTF.setEditable(tf);
		}		
	}

	void displayChild(ONCChild child)
	{
		bChildDataChanging = true;
		dispChild=child;
		
		String logEntry = String.format("ChildPanel.displayChild:  Child: %s",
											child.getChildFirstName());
		LogDialog.add(logEntry, "M");
		
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
		{
			//Only display child's actual name if user permission permits
			firstnameTF.setText(child.getChildFirstName());
			lastnameTF.setText(child.getChildLastName());
		}
		else
		{
			//Else, display the restricted name for the child
			firstnameTF.setText("Child " + cDB.getChildNumber(child));
			lastnameTF.setText("");
		}
		
		dobDC.setCalendar(convertDOBFromGMT(child.getChildDateOfBirth()));
		
		//If the child is older than the max, use RED to COLOR the background
		if(dispChild.getChildIntegerAge() > ONC_MAX_CHILD_AGE || dispChild.getChildIntegerAge() < 0)
			ageTF.setBackground(Color.RED);
		else
			ageTF.setBackground(ageNormalBackgroundColor);
		ageTF.setText(child.getChildAge());
		
		schoolTF.setText(child.getChildSchool());
		genderTF.setText(child.getChildGender());
	
		bChildDataChanging = false;
	}
	
	/****************************************************************************************
	 * Takes the date of birth in milliseconds (GMT) and returns a local time zone 
	 * Calendar object of the date of birth
	 * @param gmtDOB
	 * @return
	 ***************************************************************************************/
	public static Calendar convertDOBFromGMT(long gmtDOB)
	{
		//gives you the current offset in ms from GMT at the current date
		TimeZone tz = TimeZone.getDefault();	//Local time zone
		int offsetFromUTC = tz.getOffset(gmtDOB);

		//create a new calendar in local time zone, set to gmtDOB and add the offset
		Calendar localCal = Calendar.getInstance();
		localCal.setTimeInMillis(gmtDOB);
		localCal.add(Calendar.MILLISECOND, (offsetFromUTC * -1));

		return localCal;
	}
	
	/****************************************************************************************
	 * Takes a local time zone Calendar date of birth and returns the date of birth 
	 * in milliseconds (GMT)
	 * Calendar object of the date of birth
	 * @param gmtDOB
	 * @return
	 ***************************************************************************************/
	public long convertCalendarDOBToGMT(Calendar dobCal)
	{
		//gives you the current offset in ms from GMT at the current date
		dobCal.set(Calendar.HOUR, 0);
		dobCal.set(Calendar.MINUTE, 0);
		dobCal.set(Calendar.SECOND, 0);
		dobCal.set(Calendar.MILLISECOND, 0);
		
		TimeZone tz = dobCal.getTimeZone();
		int offsetFromUTC = tz.getOffset(dobCal.getTimeInMillis());
		return dobCal.getTimeInMillis() + offsetFromUTC;
	}

	void clearChildData()
	{
		dispChild = null;
		
		bChildDataChanging = true;
		
		firstnameTF.setText("");
		lastnameTF.setText("");
		dobDC.setDate(gvs.getTodaysDate());
		ageTF.setText("");
		schoolTF.setText("");
		genderTF.setText("");

		bChildDataChanging = false;
	}

	//Store new data for the child if any text field changed and the user has permission to change the data
	void updateChild(ONCChild c)
	{
		//field changed and user has permission to change
		if(c != null &&
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 &&
				(!firstnameTF.getText().equals(c.getChildFirstName()) ||
				  !lastnameTF.getText().equals(c.getChildLastName()) ||
				   !schoolTF.getText().equals(c.getChildSchool()) ||
				    !genderTF.getText().equals(c.getChildGender()) ||
				     hasDOBChanged(c)))
		{
/*			
			if(!firstnameTF.getText().equals(c.getChildFirstName()))
					System.out.println(firstnameTF.getText() + " || " + c.getChildFirstName());
			if( !lastnameTF.getText().equals(c.getChildLastName()))
				System.out.println(lastnameTF.getText() + " || " + c.getChildLastName());
			if(!schoolTF.getText().equals(c.getChildSchool()))
				System.out.println(schoolTF.getText() + " || " + c.getChildSchool());
			if(!firstnameTF.getText().equals(c.getChildFirstName()))
				System.out.println(firstnameTF.getText() + " || " + c.getChildFirstName());
			if(!genderTF.getText().equals(c.getChildGender()))
				System.out.println(genderTF.getText() + " || " + c.getChildGender());
			if(hasDOBChanged(c))
				System.out.println(convertCalendarDOBToGMT(dobDC.getCalendar()) + " || "+ c.getChildDateOfBirth());
*/			
			//child change detected, create change request object and send to the server
			//the child prior wish history may have changed when the child data changed
			ONCChild reqUpdateChild = new ONCChild(c);
			reqUpdateChild.updateChildData(firstnameTF.getText(), lastnameTF.getText(),
											schoolTF.getText(), genderTF.getText(),
											 convertCalendarDOBToGMT(dobDC.getCalendar()),
											 GlobalVariables.getCurrentSeason());
			
			String response = cDB.update(this, reqUpdateChild);	//notify child has changed
			if(response.startsWith("UPDATED_CHILD"))
			{
				ONCChild updatedChild = cDB.getChild(reqUpdateChild.getID());
				displayChild(updatedChild);
			}
			else
			{
				//display an error message that update request failed
				Thread.currentThread().getStackTrace();
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server denied Child Update," +
						"try again later","Child Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				
				displayChild(c);
			}
		}
	}
	
	boolean hasDOBChanged(ONCChild c)
	{
		return convertCalendarDOBToGMT(dobDC.getCalendar()) != c.getChildDateOfBirth();
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject1();
			if(updatedChild != null && updatedChild.getID() == dispChild.getID())
			{
				String logEntry = String.format("ChildPanel Event: %s, Child: %s",
													dbe.getType(), dispChild.getChildFirstName());
				LogDialog.add(logEntry, "M");
				displayChild(updatedChild);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			ONCChild deletedChild = (ONCChild) dbe.getObject1();
			if(deletedChild != null && deletedChild.getID() == dispChild.getID())
			{
				String logEntry = String.format("ChildPanel Event: %s, Child: %s",
													dbe.getType(), dispChild.getChildFirstName());
				LogDialog.add(logEntry, "M");
				clearChildData();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_CHILDREN"))
		{
			String logEntry = String.format("ChildPanel Event: %s", dbe.getType());
			LogDialog.add(logEntry, "M");
			setEditableGUIFields(true);
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = cDB.getChildren(fam.getID());
			
			if(dispChild != null)
				updateChild(dispChild);
			
			//check to see if there are children in the family, is so, display first child
			if(childList != null && !childList.isEmpty())
			{
				String logEntry = String.format("ChildPanel Event: %s, ONC# %s with %d children",
												tse.getType(), fam.getONCNum(), childList.size());
				LogDialog.add(logEntry, "M");
				
				displayChild(childList.get(0));
			}
			else
			{
				String logEntry = String.format("ChildPanel Event: %s, ONC# %s with %d children",
						tse.getType(), fam.getONCNum(), childList.size());
				LogDialog.add(logEntry, "M");
				clearChildData();
			}
		}
		else if(tse.getType() == EntityType.CHILD)
		{
			ONCChild child = (ONCChild) tse.getObject2();
			
			if(dispChild != null)
				updateChild(dispChild);
			
			String logEntry = String.format("ChildPanel Event: %s, Child Selected: %s",
					tse.getType(), child.getChildFirstName());
			LogDialog.add(logEntry, "M");
			displayChild(child);
		}
		else if(tse.getType() == EntityType.WISH)
		{
			ONCChild child = (ONCChild) tse.getObject2();
			
			if(dispChild != null)
				updateChild(dispChild);
			
			String logEntry = String.format("ChildPanel Event: %s, Child Selected: %s",
					tse.getType(), child.getChildFirstName());
			LogDialog.add(logEntry, "M");
			displayChild(child);
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.WISH);
	}
	
	/***********************************************************************************************
	 * This class implements the listeners for all events associated with the update of child 
	 * information from child panel UI.
	 * 
	 * The Action listener processes events from the ChildPanel info text fields (First Name, Last Name,
	 * School, Gender) and all child wish combo boxes and text fields.
	 * 
	 * In addition, this listener implements a PropertyChange Listener that processes changes from the
	 * Date of Birth date chooser on the Child Panel. 
	 ************************************************************************************************/
	private class ChildUpdateListener implements ActionListener, PropertyChangeListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(dispChild != null && !bChildDataChanging &&
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 && 
				 (e.getSource() == firstnameTF || e.getSource() == lastnameTF ||
					e.getSource() == schoolTF ||e.getSource() == genderTF))
			{
				updateChild(dispChild);	
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(pce.getSource() == dobDC.getDateEditor() && 
				"date".equals(pce.getPropertyName()) && 
				 !bChildDataChanging && dispChild != null &&
				  userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 &&
				   dispChild.getChildDateOfBirth() != convertCalendarDOBToGMT(dobDC.getCalendar()))
				  
			{
					updateChild(dispChild);
			        displayChild(dispChild);
			}		
		}
	}
}
