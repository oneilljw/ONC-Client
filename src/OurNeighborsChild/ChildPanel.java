package OurNeighborsChild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.toedter.calendar.JDateChooser;

public class ChildPanel extends JPanel implements ActionListener, DatabaseListener
{
	/**
	 * This class extends JPanel to provide the UI for display and edit of a child and the child's
	 * wishes. The child's first and last name, gender, date of birth, age and school are displayed
	 * in JTextFields or JLabels and all but age can be changed by the user. In addition,
	 * the class provides a wish sub-panel for display and edit of three ONCChildWish objects
	 * for each child.
	 * Each wish sub-panel consists of combo boxes and text fields. In addition, a radio button icon
	 * in each sub-panel allows the user to launch a Wish History viewer for each wish. Wish history 
	 * radio button events are handled by this panel. Other wish sub-panel combo box
	 * events are handled by the Family Panel class, allowing coordination with other GUI elements 
	 * that display child wish information such as the wish management dialog, organization dialog
	 * and the wish catalog dialog. When a user changes a child wish from a wish sub-panel, the 
	 * selected wish may require additional information. If so, a additional info dialog is created
	 * and displayed, allowing the user to specify additional information. All base wish and 
	 * additional detail requirement information is obtained from the Wish Catalog singleton object. 
	 * In the ONC application, this class is created by the family panel. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONC_GIFT_ICON = 4;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final int NO_WISH = -1;
	private static final int WISH_CATALOG_SELECTION_LISTTYPE = 0;
	private static final int ONC_MAX_CHILD_AGE = 24; //Used for sorting children into array lists
	private static final int CHILD_WISH_STATUS_EMPTY = 1;
	private static final int CHILD_WISH_STATUS_SELECTED = 2;
	private static final int ONC_HELMET_WISHID = 17; //Used for sorting children into array lists
	private static final String ONC_HELMET_NAME = "Helmet"; //Used for associated Helmet's with Bike's
	private static final String ONC_BIKE_NAME = "Bike"; //Used for associated Helmet's with Bike's
	
	//Singleton application objects
	private GlobalVariables cpGVs;
	private ONCOrgs orgs;
	private ONCWishCatalog cat;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	
	private ONCChild c = null;	//The panel needs to know which child is being displayed for listeners
	private JFrame pFrame = null;
	
	//GUI elements
	public JTextField firstnameTF;	//public so prior history dialog can access
	private JTextField lastnameTF, schoolTF, genderTF;
	private JDateChooser dobDC;
	private JTextField ageTF;
	private JComboBox[] wishCB, wishindCB, wishstatusCB, wishassigneeCB;
	private DefaultComboBoxModel[] wishCBM, assigneeCBM;
	private JTextField[] wishdetailTF;
	private JRadioButton[] wishRB;
	private Color ageNormalBackgroundColor;
	
	//Semaphores
//	private boolean bChildDataChanged = false; //flag indicates child data has been modified
	private boolean bChildDataChanging = false; //flag indicates child data displayed is updating
	
	public ChildPanel()
	{		
		//Set layout and border for the Child Panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setToolTipText("Information for an ONC child");
		this.setBorder(BorderFactory.createTitledBorder("Child Information"));
		
		//Set parent frame and share app data structures
		cpGVs = GlobalVariables.getInstance();
//		pFrame = cpGVs.getFrame();
		orgs = ONCOrgs.getInstance();
		cat = ONCWishCatalog.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		
		//register database listeners for updates
		if(cwDB != null) { cwDB.addDatabaseListener(this); }
		if(orgs != null) { orgs.addDatabaseListener(this); }
		if(cat != null) { cat.addDatabaseListener(this); }
		
		//Initialize class variables
		wishCB = new JComboBox[NUMBER_OF_WISHES_PER_CHILD];
		wishindCB = new JComboBox[NUMBER_OF_WISHES_PER_CHILD];
		wishdetailTF = new JTextField[NUMBER_OF_WISHES_PER_CHILD];
		wishRB = new JRadioButton[NUMBER_OF_WISHES_PER_CHILD];
		wishstatusCB = new JComboBox[NUMBER_OF_WISHES_PER_CHILD];
		wishassigneeCB = new JComboBox[NUMBER_OF_WISHES_PER_CHILD];
		wishCBM = new DefaultComboBoxModel[NUMBER_OF_WISHES_PER_CHILD];
		assigneeCBM = new DefaultComboBoxModel[NUMBER_OF_WISHES_PER_CHILD];
		
		//Create a listener for panel gui events
		ChildUpdateListener cuListener = new ChildUpdateListener();
		
		//Setup sub panels that comprise the Child Panel
		JPanel childinfopanel = new JPanel();
		childinfopanel.setLayout(new BoxLayout(childinfopanel, BoxLayout.LINE_AXIS));
		JPanel childwishespanel = new JPanel(new GridLayout(1,3));
		
		JPanel[] wp = new JPanel[NUMBER_OF_WISHES_PER_CHILD];
		JPanel[] wsp1 = new JPanel[NUMBER_OF_WISHES_PER_CHILD];
		JPanel[] wsp2 = new JPanel[NUMBER_OF_WISHES_PER_CHILD];
		JPanel[] wsp3 = new JPanel[NUMBER_OF_WISHES_PER_CHILD];
		
		//Set up the wish panels. Each wish panel contains 3 subpanels
		for(int i =0; i<wp.length; i++)
		{
			wp[i] = new JPanel(new GridLayout(3,1));
			wsp1[i] = new JPanel();
			wsp1[i].setLayout(new BoxLayout(wsp1[i], BoxLayout.LINE_AXIS));
			wsp2[i] = new JPanel();
			wsp2[i].setLayout(new BoxLayout(wsp2[i], BoxLayout.LINE_AXIS));
			wsp3[i] = new JPanel();
			wsp3[i].setLayout(new BoxLayout(wsp3[i], BoxLayout.LINE_AXIS));
			wp[i].setBorder(BorderFactory.createTitledBorder("Wish " + Integer.toString(i+1)));
		}

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
        
        dobDC = new JDateChooser(cpGVs.getTodaysDate());
		dobDC.setMinimumSize(new Dimension(136, 48));
		dobDC.setBorder(BorderFactory.createTitledBorder("Date of Birth"));
		dobDC.setEnabled(false);
		dobDC.getDateEditor().addPropertyChangeListener(cuListener);
        
        ageTF = new JTextField();
        ageTF.setPreferredSize(new Dimension(56, 28));
        ageTF.setBorder(BorderFactory.createTitledBorder("Age"));
        ageNormalBackgroundColor = ageTF.getBackground();
        
        schoolTF = new JTextField();
        schoolTF.setPreferredSize(new Dimension(120, 28));
        schoolTF.setBorder(BorderFactory.createTitledBorder("School"));
        schoolTF.setEditable(false);
        schoolTF.addActionListener(cuListener);
        
        genderTF = new JTextField();
        genderTF.setPreferredSize(new Dimension(56, 28));
        genderTF.setBorder(BorderFactory.createTitledBorder("Gender"));
        genderTF.setEditable(false);
        genderTF.addActionListener(cuListener);
 
        //Set up the wish combo boxes and detail text fields for child wishes        
        String[] indications = {"", "*", "#"};        
        String [] status = {"**Unused**", "Empty", "Selected", "Assigned", "Received",
        					"Distributed", "Verified"};
        
        Dimension dwi = new Dimension(60, 24);
        Dimension dws = new Dimension(100, 24);       
        Dimension dwa = new Dimension(140, 24);
        
        for(int i=0; i<wishCB.length; i++)
        {
        	//Get a catalog for type=selection
        	wishCBM[i] = new DefaultComboBoxModel();
        	wishCBM[i].addElement(new ONCWish(-1, "None", 7));
        	wishCB[i] = new JComboBox();
        	wishCB[i].setModel(wishCBM[i]);
            wishCB[i].setPreferredSize(new Dimension(144, 24));
            wishCB[i].setToolTipText("Select wish from ONC gift catalog");
            wishCB[i].setEnabled(false);
            wishCB[i].addActionListener(cuListener);
            
            wishindCB[i] = new JComboBox(indications);
            wishindCB[i].setPreferredSize(dwi);           
            wishindCB[i].setToolTipText("Set Wish Restrictions: #- Not from ODB Wish List, *- Don't assign for fulfillment");
            wishindCB[i].setEnabled(false);
            wishindCB[i].addActionListener(cuListener);
            
            wishRB[i] = new JRadioButton(cpGVs.getImageIcon(ONC_GIFT_ICON));
            wishRB[i].setToolTipText("Click to see wish history");
            wishRB[i].setEnabled(false);
            wishRB[i].addActionListener(this);
            
            wishdetailTF[i] = new JTextField();
            wishdetailTF[i].setToolTipText("Type wish details, then hit <Enter>");
            wishdetailTF[i].setEnabled(false);
            wishdetailTF[i].addActionListener(cuListener);
            
        	wishstatusCB[i] = new JComboBox(status);
            wishstatusCB[i].setPreferredSize(dws);
            wishstatusCB[i].setToolTipText("Change the status of the wish in its fulfillment lifecycle");
            wishstatusCB[i].setEnabled(false);
            wishstatusCB[i].addActionListener(cuListener);
            
        	assigneeCBM[i] = new DefaultComboBoxModel();
        	assigneeCBM[i].addElement(new Organization(-1, "None", "None"));
        	wishassigneeCB[i] = new JComboBox();
        	wishassigneeCB[i].setModel(assigneeCBM[i]);
        	wishassigneeCB[i].setPreferredSize(dwa);
        	wishassigneeCB[i].setToolTipText("Select the organization for wish fulfillment");
        	wishassigneeCB[i].setEnabled(false);
        	wishassigneeCB[i].addActionListener(cuListener);
        }
               
        //add the gui items to the child info panel             
        childinfopanel.add(firstnameTF);
        childinfopanel.add(lastnameTF);
        childinfopanel.add(dobDC);
        childinfopanel.add(ageTF);
        childinfopanel.add(schoolTF);
        childinfopanel.add(genderTF);
        
        //Add the gui objects to the proper wish subpanel
        for(int i=0; i<wp.length; i++)
        {
        	wsp1[i].add(wishCB[i]);
        	wsp1[i].add(wishindCB[i]);
        	wsp1[i].add(wishRB[i]);
        	wsp2[i].add(wishdetailTF[i]);
        	wsp3[i].add(wishstatusCB[i]);
            wsp3[i].add(wishassigneeCB[i]);
        }
         
        //Add the wish sub panels to each wish panel
        for(int i=0; i<wp.length; i++)
        {
        	wp[i].add(wsp1[i]);
        	wp[i].add(wsp2[i]);
        	wp[i].add(wsp3[i]);
        }
        
        //Add the child wish panels to the child wish panel
        for(int i=0; i<wp.length; i++)
        	childwishespanel.add(wp[i]);
      
        //Add the child wish and child info panels to the child panel  
        this.add(childinfopanel);
        this.add(childwishespanel);     
	}
	
	void setEditableGUIFields(boolean tf)
	{
		if(cpGVs.isUserAdmin())
		{
			firstnameTF.setEditable(tf);
			lastnameTF.setEditable(tf);
			dobDC.setEnabled(true);
			schoolTF.setEditable(tf);
			genderTF.setEditable(tf);
		}		
	}
	
	void setEnabledWishPanels(boolean tf)
	{
		for(int i=0; i<NUMBER_OF_WISHES_PER_CHILD; i++ )
		{
			wishCB[i].setEnabled(tf);
			wishdetailTF[i].setEnabled(tf);
			wishindCB[i].setEnabled(tf);
			wishRB[i].setEnabled(tf);
			wishstatusCB[i].setEnabled(tf);
			wishassigneeCB[i].setEnabled(tf);
		}	
	}
	
	void setEnabledWishCBs(boolean tf)
	{
		for(int i=0; i<NUMBER_OF_WISHES_PER_CHILD; i++ )
			wishCB[i].setEnabled(tf);
	}
	
	/***********************************************************************************
	 * Overloaded method - without index can only be called if user is admin or higher,
	 * does not protect the privacy of the child
	 * @param child
	 ***********************************************************************************/
	void displayChild(ONCChild child)
	{
		bChildDataChanging = true;
		c=child;
		
		firstnameTF.setText(child.getChildFirstName());
		lastnameTF.setText(child.getChildLastName());

//		dobDC.setCalendar(child.getChildDOB());
		dobDC.setCalendar(convertDOBFromGMT(child.getChildDateOfBirth()));
		
		//If the child is older than the max, use RED to COLOR the background
		if(c.getChildIntegerAge() > ONC_MAX_CHILD_AGE || c.getChildIntegerAge() < 0)
			ageTF.setBackground(Color.RED);
		else
			ageTF.setBackground(ageNormalBackgroundColor);
		ageTF.setText(child.getChildAge());
		
		schoolTF.setText(child.getChildSchool());
		genderTF.setText(child.getChildGender());
		
		for(int wn=0; wn<wishCB.length; wn++)
			displayWish(cwDB.getWish(child.getChildWishID(wn)), wn);	
		
		bChildDataChanging = false;
	}
	
	void displayChild(ONCChild child, int index)
	{
		bChildDataChanging = true;
		c=child;
		
		if(cpGVs.isUserAdmin())	//Only display child's actual name if user permission permits
		{
			firstnameTF.setText(child.getChildFirstName());
			lastnameTF.setText(child.getChildLastName());
		}
		else	//Else, display the restricted name for the child
		{
			firstnameTF.setText("Child " + Integer.toString(index+1));
			lastnameTF.setText("");
		}
		
		System.out.println("ChildPanel.displayChild: CalendarDOB: " + c.getChildDOB());
		System.out.println(String.format("ChildPanel.displayChild: DOB in millis: %d", c.getChildDOB().getTimeInMillis()));
		
		
		
		dobDC.setCalendar(convertDOBFromGMT(child.getChildDateOfBirth()));
		
		//If the child is older than the max, use RED to COLOR the background
		if(c.getChildIntegerAge() > ONC_MAX_CHILD_AGE || c.getChildIntegerAge() < 0)
			ageTF.setBackground(Color.RED);
		else
			ageTF.setBackground(ageNormalBackgroundColor);
		ageTF.setText(child.getChildAge());
		
		schoolTF.setText(child.getChildSchool());
		genderTF.setText(child.getChildGender());
		
		for(int wn=0; wn<wishCB.length; wn++)
			displayWish(cwDB.getWish(child.getChildWishID(wn)), wn);	
		
		bChildDataChanging = false;
	}
	
	public static Calendar convertDOBFromGMT(long gmtDOB)
	{
		//gives you the current offset in ms from GMT at the current date
		TimeZone tz = TimeZone.getDefault();	//Local time zone
		int offsetFromUTC = tz.getOffset(gmtDOB);
		System.out.println(String.format("ChildPanel.convertDOB offset: %d", offsetFromUTC));

		//create a new calendar in local time zone, set to gmtDOB and add the offset
		Calendar localCal = Calendar.getInstance();
		localCal.setTimeInMillis(gmtDOB);
		localCal.add(Calendar.MILLISECOND, (offsetFromUTC * -1));

		return localCal;
	}
	
	public static Calendar convertToGMT(Calendar cal) {

		Date date = cal.getTime();
		TimeZone tz = cal.getTimeZone();

		//Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT 
		long msFromEpochGmt = date.getTime();

		//gives you the current offset in ms from GMT at the current date
		int offsetFromUTC = tz.getOffset(msFromEpochGmt);

		//create a new calendar in GMT timezone, set to this date and add the offset
		Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		gmtCal.setTime(date);
		gmtCal.add(Calendar.MILLISECOND, offsetFromUTC);

		return gmtCal;
	}
	
	void displayWish(ONCChildWish cw, int wn)
	{
		bChildDataChanging = true;
		if(cw !=null)
		{	
			ONCWish wish = cat.getWishByID(cw.getWishID());
			if(wish != null)
				wishCB[wn].setSelectedItem(wish);
			else
				wishCB[wn].setSelectedIndex(0);
			
			wishindCB[wn].setSelectedIndex(cw.getChildWishIndicator());
			wishdetailTF[wn].setText(cw.getChildWishDetail());
			wishdetailTF[wn].setCaretPosition(0);
			wishstatusCB[wn].setSelectedIndex(cw.getChildWishStatus());
			
			Organization org = orgs.getOrganizationByID(cw.getChildWishAssigneeID());
			if(org != null)
				wishassigneeCB[wn].setSelectedItem(org);
			else
				wishassigneeCB[wn].setSelectedIndex(0);
		}
		else	//child wish doesn't exist yet
		{
			wishCB[wn].setSelectedIndex(0);
			wishindCB[wn].setSelectedIndex(0);
			wishdetailTF[wn].setText("");
			wishdetailTF[wn].setCaretPosition(0);
			wishstatusCB[wn].setSelectedIndex(1);
			wishassigneeCB[wn].setSelectedIndex(0);
		}
		
		bChildDataChanging = false;
	}

	void clearChildData()
	{
		bChildDataChanging = true;
		
		firstnameTF.setText("");
		lastnameTF.setText("");
		dobDC.setDate(cpGVs.getTodaysDate());
		ageTF.setText("");
		schoolTF.setText("");
		genderTF.setText("");
		
		for(int wishID=0; wishID<wishCB.length; wishID++)
			clearChildWish(wishID);
		
		bChildDataChanging = false;
	}
	
	void clearChildWish(int wishID)
	{
		wishCB[wishID].setSelectedIndex(0);
		wishdetailTF[wishID].setText("");
		wishindCB[wishID].setSelectedIndex(0);
		wishstatusCB[wishID].setSelectedIndex(0);
		wishassigneeCB[wishID].setSelectedIndex(0);
	}
	
	//Store new data for the child if any text field changed and the user has permission to change the data
	void updateChild(ONCChild c)
	{
		//field changed and user has permission to change
		if(cpGVs.isUserAdmin() && 
				(!firstnameTF.getText().equals(c.getChildFirstName())) ||
				  !lastnameTF.getText().equals(c.getChildLastName()) ||
				   !schoolTF.getText().equals(c.getChildSchool()) ||
				    !genderTF.getText().equals(c.getChildGender()) ||
				     hasDOBChanged(c))	
		{
			//child change detected, create change request object and send to the server
			//the child prior wish history may have changed when the child data changed
			ONCChild reqUpdateChild = new ONCChild(c);
			reqUpdateChild.updateChildData(firstnameTF.getText(), lastnameTF.getText(),
											schoolTF.getText(), genderTF.getText(),
											 dobDC.getCalendar().getTimeInMillis());
			
			String response = cDB.update(this, reqUpdateChild);	//notify child has changed
			if(response.startsWith("UPDATED_CHILD"))
			{
				ONCChild updatedChild = cDB.getChild(reqUpdateChild.getID());
				displayChild(updatedChild);
				
				updateChildWishDetail(updatedChild);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "ONC Server denied Child Update," +
						"try again later","Child Update Failed",  
						JOptionPane.ERROR_MESSAGE, cpGVs.getImageIcon(0));
				displayChild(c);
			}
		}
	}
	
	boolean hasDOBChanged(ONCChild c)
	{
		//create a CAL for current DOB (GMT)
//		Locale locale = new Locale("en", "US");
//		TimeZone timezone = TimeZone.getTimeZone("GMT");
//		Calendar gmtDOB = Calendar.getInstance(timezone, locale);
//		gmtDOB.setTimeInMillis(c.getChildDateOfBirth());
		
		//get the CAL for the dateChooser date
		long dcDOB  = dobDC.getCalendar().getTimeInMillis();
			
		if(dcDOB != c.getChildDateOfBirth())
			return true;
		else
			return false;
	}
	
	//Check all three wish detail fields for changes. Process changes if they occur. If a 
	//wish detail changes, we do add a new wish to the data base. 
	void updateChildWishDetail(ONCChild c)
	{
		for(int wn=0; wn < NUMBER_OF_WISHES_PER_CHILD; wn++)
		{
			ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
			if(cw != null && !wishdetailTF[wn].getText().equals(cw.getChildWishDetail()))
				updateWish(wn);
		}
	}

	String[] getChildTableData()
	{
		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
		String[] childdata = {firstnameTF.getText(), lastnameTF.getText(),
								oncdf.format(dobDC.getDate()), genderTF.getText()};
		
		return childdata;
	}
	
	void updateWishSelectionList()
	{
		bChildDataChanging = true;
		
		for(int wn=0; wn < wishCBM.length; wn++)
		{
			wishCBM[wn].removeAllElements();	//Clear the combo box selection list
		
//			for(String s: cat.getWishList(wn, WISH_CATALOG_SELECTION_LISTTYPE))	//Add new list elements
//				wishCBM[wn].addElement(s);
			
			for(ONCWish w: cat.getWishList(wn, WISH_CATALOG_SELECTION_LISTTYPE))	//Add new list elements
				wishCBM[wn].addElement(w);
			
			//Reselect the proper wish for the currently displayed child
			ONCChildWish cw;
			if(c != null && (cw = cwDB.getWish(c.getChildWishID(wn))) != null) 
//				wishCB[wn].setSelectedItem(cw.getChildWishBase());
				wishCB[wn].setSelectedItem(cat.getWishByID(cw.getWishID()));
			else
				wishCB[wn].setSelectedIndex(0);
		}
		
		bChildDataChanging = false;
	}
/*	
	void updateWishAssigneeSelectionList()
	{
		bChildDataChanging = true;
		
		String[] currentAssignee = new String[assigneeCBM.length];
		
		for(int i=0; i<assigneeCBM.length; i++)
		{
			currentAssignee[i] = wishassigneeCB[i].getSelectedItem().toString();
			assigneeCBM[i].removeAllElements();
			assigneeCBM[i].addElement("None");
		}
		
		for(String s:orgs.getConfirmedOrgs())
			for(int i=0; i<assigneeCBM.length; i++)
				assigneeCBM[i].addElement(s);
		
		//Restore selection to prior selection, if they are still confirmed
		if(c != null)
		{
			for(int wn=0; wn<wishassigneeCB.length; wn++)
			{
				ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
				if(cw != null)
					wishassigneeCB[wn].setSelectedIndex(orgs.getConfirmedOrganizationIndex(cw.getChildWishAssigneeID()));
			}
		}	
		bChildDataChanging = false;
	}
*/	
	/*****************************************************************************************************
	 * The combo box model holds organization objects with CONFIRMED status. Each time a CONFIRMED_PARTNER
	 * event occurs, the combo box is updated with a new set of organization objects. The first 
	 * organization object at the top of the box is a non-assigned organization named None
	 ***********************************************************************************************/
	
	void updateWishAssigneeSelectionList()
	{
		bChildDataChanging = true;
		
		
		for(int i=0; i<assigneeCBM.length; i++)
		{
			assigneeCBM[i].removeAllElements();
			assigneeCBM[i].addElement(new Organization(-1, "None", "None"));
		}
		
		for(Organization confOrg: orgs.getConfirmedOrgList())
			for(int i=0; i<assigneeCBM.length; i++)
				assigneeCBM[i].addElement(confOrg);
		
		//Restore selection to prior selection, if they are still confirmed
		if(c != null)
		{
			for(int wn=0; wn<wishassigneeCB.length; wn++)
			{
				ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
				if(cw != null)
					wishassigneeCB[wn].setSelectedItem(orgs.getOrganizationByID(cw.getChildWishAssigneeID()));
			}
		}	
		bChildDataChanging = false;
	}
	
	/********************************************************************************************
	 * This method is called to check to see if a child's wish has changed. A change did occur if
	 * the wish as represented on the child panel is different from the wish stored for the child.
	 * If a change is detected, a new wish is created and added to the child wish database thru
	 * a call to the addWish method in the child data base. 
	 * When the new base wish may require a detailed dialog box that prompts the
	 * user for more detail about the wish selected.  
	 * @param wn - which of the child's wishes is to be checked
	 *******************************************************************************************/
	void updateWish(int wn)	//NEED TO HANDLE IF CURRET WISH IS NULL, ADDING THE FIRST WISH TO HISTORY
	{
		ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
//		String oldWishBase = cw.getChildWishBase();
		int oldWishID = cw.getWishID();
		
		//Test to see that the wish has changed and not simply a combo box event without a change
		//to the selected item.
		ONCWish selectedCBWish = (ONCWish) wishCB[wn].getSelectedItem();
		Organization selectedCBOrg = (Organization) wishassigneeCB[wn].getSelectedItem();
		
		if(selectedCBWish.getID() != cw.getWishID() ||
			wishindCB[wn].getSelectedIndex() != cw.getChildWishIndicator() ||
			 wishstatusCB[wn].getSelectedIndex() != cw.getChildWishStatus() ||
			  !wishdetailTF[wn].getText().equals(cw.getChildWishDetail()) ||
//			   !wishassigneeCB[wn].getSelectedItem().toString().equals(cw.getChildWishAssigneeName()))
			   selectedCBOrg.getID() != (cw.getChildWishAssigneeID()))
		{
			
			//A change to the wish has occurred, test to see if it's a change to the base
			//If it is a change to the base, additional detail may be required.
			if(selectedCBWish.getID() != cw.getWishID()) 	
			{
				//It was a change to the wish base
				//Check if a detail dialog is required. It is required if the wish name is found
				//in the catalog (return != null) and the ONC Wish object detail required array list
				//contains data. If required, construct and show the modal dialog. If not required, clear
				//the wish detail text field so the user can create new detail. This prevents inadvertent
				//legacy wish detail from being carried forward with a wish change
				ArrayList<WishDetail> drDlgData = cat.getWishDetail(selectedCBWish.getID());
				if(drDlgData != null)
				{
					//Construct and show the wish detail required dialog
					String newWishName = wishCB[wn].getSelectedItem().toString();
					DetailDialog dDlg = new DetailDialog(GlobalVariables.getFrame(), newWishName, drDlgData);
					Point pt = pFrame.getLocation();	//Used to set dialog location
					dDlg.setLocation(pt.x + (wn*200) + 20, pt.y + 400);
					dDlg.setVisible(true);
					
					//Retrieve the data and update the wish
					wishdetailTF[wn].setText(dDlg.getDetail());
				}
				else
				{
					wishdetailTF[wn].setText("");	//Clear the text field if base wish changed so user can complete
				}
			}
			
			int orgID;
//			String orgName;
			
//			if(cw != null && !wishassigneeCB[wn].getSelectedItem().toString().equals(cw.getChildWishAssigneeName()))
			if(cw != null && selectedCBOrg.getID() != cw.getChildWishAssigneeID())	
			{		
				//A change to the wish assignee has occurred, set the new child wish
				orgID = selectedCBOrg.getID();
//				orgName = selectedCBOrg.getName();
			}
			else	//use the existing assignee with the new wish
			{
				orgID = cw.getChildWishAssigneeID();
//				orgName = cw.getChildWishAssigneeName();
			}
			
			//Now that we have assessed/received base,  detail and assignee changes, create a new wish			
			int wishID = cwDB.add(this, c.getID(), selectedCBWish.getID(),
						wishdetailTF[wn].getText(), wn, wishindCB[wn].getSelectedIndex(),
						wishstatusCB[wn].getSelectedIndex(), orgID, cpGVs.getUserLNFI(),
						cpGVs.getTodaysDate());
			
			//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
			//the status of the wish.
			if(wishID != -1)
			{
				ONCChildWish addedWish = cwDB.getWish(wishID);
				if(addedWish != null)
					displayWish(addedWish, addedWish.getWishNumber());	
			}
			else	//an error occurred, display original wish
			{
				ONCChildWish origWish = cwDB.getWish(c.getChildWishID(wn));
				if(origWish != null)
					displayWish(origWish, origWish.getWishNumber());
				else
				{
					clearChildWish(wn);
				}		
			}
			
			//if child wish selected was a Bike as wish 1, make wish 2 a helmet. If child wish 1 was a bike and
			//and has been changed, make wish 2 empty
/*****	DIAGNOTICS ****************		
			System.out.println("Wish #; " + wn);
			System.out.println("Old Wish Base: " +oldWishBase);
			System.out.println("Wish 0 CB: " + wishCB[0].getSelectedItem().toString());
			System.out.println("Child Wish 1 Wish ID: " + c.getChildWishID(1));
			if(c.getChildWishID(1) != -1)
				System.out.println("Child Wish 1 Wish Base: " + cwDB.getWish(c.getChildWishID(1)).getChildWishBase());
*********************************/			
			if(wn==0 && ((ONCWish)wishCB[0].getSelectedItem()).getID() == cat.getWishID(ONC_BIKE_NAME) &&
					(c.getChildWishID(1) == -1 || (c.getChildWishID(1) != -1 &&
						cwDB.getWish(c.getChildWishID(1)).getWishID() != cat.getWishID(ONC_HELMET_NAME))))
			{
				autoAddHelmetAsWish1();
			}
			else if(wn==0 && oldWishID == cat.getWishID(ONC_BIKE_NAME) &&
					((ONCWish)wishCB[0].getSelectedItem()).getID() != cat.getWishID(ONC_BIKE_NAME) && 
					 c.getChildWishID(1) != -1 &&
					  cwDB.getWish(c.getChildWishID(1)).getWishID() == cat.getWishID(ONC_HELMET_NAME))
			{
				//set the combo boxes in the panel
				bChildDataChanging = true;
				
				wishCB[1].setSelectedItem("None");
				wishdetailTF[1].setText("");
				wishindCB[1].setSelectedIndex(0);
				wishstatusCB[1].setSelectedIndex(CHILD_WISH_STATUS_EMPTY);
				wishassigneeCB[1].setSelectedIndex(0);
				
				//set wish 1 to none	
				wishID = cwDB.add(this, c.getID(), -1, "", 1, 0, CHILD_WISH_STATUS_EMPTY, 
							0, cpGVs.getUserLNFI(), cpGVs.getTodaysDate());
				
				//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
				//the status of the wish.
				if(wishID != -1)
				{
					ONCChildWish addedWish = cwDB.getWish(wishID);
					if(addedWish != null)
						displayWish(addedWish, addedWish.getWishNumber());	
				}
				else	//an error occurred, display original wish
				{
					ONCChildWish origWish = cwDB.getWish(c.getChildWishID(1));
					if(origWish != null)
						displayWish(origWish, origWish.getWishNumber());
					else
					{
						clearChildWish(1);
					}		
				}
				
				bChildDataChanging = false;
			}
			
		}
	}
	
	void createWish(int wn)
	{
		String newWishName = wishCB[wn].getSelectedItem().toString();
		ONCWish selectedCBWish = (ONCWish) wishCB[wn].getSelectedItem();
		
		//Check if a detail dialog is required. It is required if the wish name is found
		//in the catalog (return != null) and the ONC Wish object detail required array list
		//contains data. If required, construct and show the modal dialog. If not required, clear
		//the wish detail text field so the user can create new detail. This prevents inadvertent
		//legacy wish detail from being carried forward with a wish change
		ArrayList<WishDetail> drDlgData = cat.getWishDetail(selectedCBWish.getID());
		if(drDlgData != null)
		{
			//Construct and show the wish detail required dialog
			DetailDialog dDlg = new DetailDialog(pFrame, newWishName, drDlgData);
			Point pt = pFrame.getLocation();	//Used to set dialog location
			dDlg.setLocation(pt.x + (wn*200) + 20, pt.y + 400);
			dDlg.setVisible(true);
			
			//Retrieve the data and update the wish
			wishdetailTF[wn].setText(dDlg.getDetail());
		}
		else
		{
			wishdetailTF[wn].setText("");	//Clear the text field if base wish changed so user can complete
		}
		

		ONCWish selectedWish = (ONCWish) wishCB[wn].getSelectedItem();
		Organization selectedCBOrg = (Organization) wishassigneeCB[wn].getSelectedItem();
		
		//Now that we have assessed/received base,  detail and assignee changes, create a new wish			
		int wishID = cwDB.add(this, c.getID(), selectedWish.getID(),
							wishdetailTF[wn].getText(), wn, wishindCB[wn].getSelectedIndex(),
							wishstatusCB[wn].getSelectedIndex(), selectedCBOrg.getID(),
							cpGVs.getUserLNFI(), cpGVs.getTodaysDate());
		
		//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
		//the status of the wish.
		if(wishID != -1)
		{
			ONCChildWish addedWish = cwDB.getWish(wishID);
			if(addedWish != null)
				displayWish(addedWish, addedWish.getWishNumber());	
		}
		else	//an error occurred, display original wish
		{
			ONCChildWish origWish = cwDB.getWish(c.getChildWishID(wn));
			if(origWish != null)
				displayWish(origWish, origWish.getWishNumber());
			else
			{
				clearChildWish(wn);
			}		
		}
		
		//if child wish selected was a Bike as wish 1, make wish 2 a helmet
		if(wn==0 && wishCB[wn].getSelectedItem().toString().equals(ONC_BIKE_NAME) &&
			(c.getChildWishID(1) == -1 || c.getChildWishID(1) != -1 &&
				(cwDB.getWish(c.getChildWishID(1)).getWishID() != ONC_HELMET_WISHID)))
		{
			autoAddHelmetAsWish1();
		}
		
	}
	
	void autoAddHelmetAsWish1()
	{
		//set the combo boxes in the panel
		bChildDataChanging = true;
		
		wishCB[1].setSelectedItem(ONC_HELMET_NAME);
		wishdetailTF[1].setText("");
		wishindCB[1].setSelectedIndex(0);
		wishstatusCB[1].setSelectedIndex(CHILD_WISH_STATUS_SELECTED);
		wishassigneeCB[1].setSelectedIndex(0);
		
		//add the helmet as wish 1		
		int wishtypeid = cat.getWishID(ONC_HELMET_NAME); 	//Not implemented yet
		int wishID = cwDB.add(this, c.getID(), wishtypeid, "", 1, 0, CHILD_WISH_STATUS_SELECTED, 
					0, cpGVs.getUserLNFI(), cpGVs.getTodaysDate());
		
		//if adding the wish was successful, we need to fetch and display the wish. The db may have changed
		//the status of the wish.
		if(wishID != -1)
		{
			ONCChildWish addedWish = cwDB.getWish(wishID);
			if(addedWish != null)
				displayWish(addedWish, addedWish.getWishNumber());	
		}
		else	//an error occurred, display original wish
		{
			ONCChildWish origWish = cwDB.getWish(c.getChildWishID(1));
			if(origWish != null)
				displayWish(origWish, origWish.getWishNumber());
			else
			{
				clearChildWish(1);
			}		
		}
		
		bChildDataChanging = false;
	}
	
	void showWishHistoryDlg(int wn)
	{
		ServerIF serverIF = ServerIF.getInstance();
		Gson gson = new Gson();
		String response = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			HistoryRequest req = new HistoryRequest(c.getID(), wn);
			
			response = serverIF.sendRequest("GET<wishhistory>"+ 
													gson.toJson(req, HistoryRequest.class));
		}
		
		if(response != null)
		{
			ArrayList<ONCChildWish> cwh = new ArrayList<ONCChildWish>();
			Type listtype = new TypeToken<ArrayList<ONCChildWish>>(){}.getType();
			
			cwh = gson.fromJson(response, listtype);
			
			//need to add the assignee name based on the assignee ID for the table
			String [] status = {"", "Empty", "Selected", "Assigned", "Received", 
					"Distributed", "Verified"};
			String[] indicators = {"", "*", "#"};
			ArrayList<String[]> wishHistoryTable = new ArrayList<String[]>();
			for(ONCChildWish cw:cwh)
			{
				ONCWish wish = cat.getWishByID(cw.getWishID());
				Organization assignee = orgs.getOrganizationByID(cw.getChildWishAssigneeID());
				
				String[] whTR = new String[7];
				whTR[0] = wish == null ? "None" : wish.getName();
				whTR[1] = cw.getChildWishDetail();
				whTR[2] = indicators[cw.getChildWishIndicator()];
				whTR[3] = status[cw.getChildWishStatus()];
				whTR[4] = assignee == null ? "None" : assignee.getName();
				whTR[5] = cw.getChildWishChangedBy();
				whTR[6] = new SimpleDateFormat("MM/dd H:mm:ss").format(cw.getChildWishDateChanged().getTime());
				
				wishHistoryTable.add(whTR);
			}
			WishHistoryDialog whDlg = new WishHistoryDialog(pFrame, wishHistoryTable, wn, firstnameTF.getText());
			whDlg.setLocationRelativeTo(wishRB[wn]);
			whDlg.setVisible(true);
		}
		else
		{
			JOptionPane.showMessageDialog(pFrame, 
					"Child Wish History Not Available", 
					"ONC Server Failed to Respond", JOptionPane.ERROR_MESSAGE, cpGVs.getImageIcon(0));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == wishRB[0]) { showWishHistoryDlg(0); }
		else if(e.getSource() == wishRB[1]) { showWishHistoryDlg(1); }
		else if(e.getSource() == wishRB[2]) { showWishHistoryDlg(2); }
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{		
		if(dbe.getSource() != this && dbe.getType().equals("WISH_ADDED"))
		{
//			System.out.println(String.format("Child Panel DB Event: Source %s, Type %s, Object %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			//Get the added wish to extract the child
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject();
		
			//If the current child being displayed wishes has been updated, update the 
			//wish display. The wish number is contained in the updated wish object
			if(addedWish.getChildID() == c.getID())	
				displayWish(addedWish, addedWish.getWishNumber());
			
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("DELETED_CONFIRMED_PARTNER")) ||
											dbe.getType().equals("UPDATED_CONFIRMED_PARTNER") ||
											dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			updateWishAssigneeSelectionList();
		}
		else if(dbe.getSource() != this && dbe.getType().contains("_CATALOG_WISH"))
		{
			updateWishSelectionList();
		}
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
			if(c != null && !bChildDataChanging && cpGVs.isUserAdmin() && 
													(e.getSource() == firstnameTF || 
													 e.getSource() == lastnameTF ||
													 e.getSource() == schoolTF ||
													 e.getSource() == genderTF))
			{
				updateChild(c);	
			}
			else if(!bChildDataChanging && (e.getSource() == wishCB[0] || 
											 e.getSource() == wishindCB[0] ||
											  e.getSource() == wishdetailTF[0] ||
											   e.getSource() == wishstatusCB[0] ||
											    e.getSource() == wishassigneeCB[0]))
			{
				if(c.getChildWishID(0) == NO_WISH)
					createWish(0);
				else
					updateWish(0);
			}
			else if(!bChildDataChanging && (e.getSource() == wishCB[1] || 
											 e.getSource() == wishindCB[1] ||
											  e.getSource() == wishdetailTF[1] ||
											   e.getSource() == wishstatusCB[1] ||
											    e.getSource() == wishassigneeCB[1]))
			{
				if(c.getChildWishID(1) == NO_WISH)
					createWish(1);
				else
					updateWish(1);
			}
			else if(!bChildDataChanging && (e.getSource() == wishCB[2] || 
					 						 e.getSource() == wishindCB[2] ||
					 						  e.getSource() == wishdetailTF[2] ||
					 						   e.getSource() == wishstatusCB[2] ||
					 							e.getSource() == wishassigneeCB[2]))
			{
				if(c.getChildWishID(2) == NO_WISH)
					createWish(2);
				else
					updateWish(2);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(pce.getSource() == dobDC.getDateEditor() && 
				"date".equals(pce.getPropertyName()) && 
				 !bChildDataChanging && c != null && cpGVs.isUserAdmin())
				  
			{
				if(!c.getChildDOB().equals(dobDC.getCalendar()))
				{
					updateChild(c);
			        displayChild(c);
				}
			}		
		}
	}
	
	class DetailDialog extends JDialog implements ActionListener
	{
		/*****************************************************************************************
		 * This class implements a dialog used to get additional detail from the 
		 * user regarding gift selection details. It provides customizable 
		 * combo boxes and a detail text field. 
		 * @params - JFrame pf - reference to the parent frame of this dialog
		 * @params - String wishname - name of the wish for which additional detail is obtained
		 * @params - ArrayList<WishDetail> wdAL - Array list containing additional detail objects
		 ******************************************************************************************/
		private static final long serialVersionUID = 1L;
		JComboBox[] cbox;
		String[] titles;	
		JTextField detailTF;
		JButton btnOK;
		
		public DetailDialog(JFrame pf, String wishname, ArrayList<WishDetail> wdAL)
		{
			super(pf, true);
			this.setTitle("Additional " + wishname + " Detail");
			
			//Create the combo boxes
			titles = new String[wdAL.size()];
			cbox = new JComboBox[wdAL.size()];
			
			JPanel infopanel = new JPanel();			
			
			for(int i=0; i<cbox.length; i++)
			{
				titles[i] = wdAL.get(i).getWishDetailName();
				cbox[i] = new JComboBox(wdAL.get(i).getWishDetailChoices());
				cbox[i].setBorder(BorderFactory.createTitledBorder(titles[i]));
				infopanel.add(cbox[i]);
			}
			
			JPanel detailpanel = new JPanel();
			detailTF = new JTextField();
			detailTF.setPreferredSize(new Dimension (320, 50));
			detailTF.setBorder(BorderFactory.createTitledBorder("Additional Details"));
			detailpanel.add(detailTF);
			
			JPanel cntlpanel = new JPanel();
			btnOK = new JButton("Ok");
			btnOK.addActionListener(this);
			cntlpanel.add(btnOK);
			
			 //Add the components to the frame pane
	        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));        
	        this.add(infopanel);
	        this.add(detailpanel);
	        this.add(cntlpanel);
	        
	        pack();				
		}
		
		String getDetail()
		{
			StringBuffer detail = new StringBuffer(cbox[0].getSelectedItem().toString());
			for(int i=1; i<titles.length; i++)
			{
				if(titles[i].toLowerCase().contains("size"))
					detail.append(", " + "Sz: " + cbox[i].getSelectedItem().toString());
				else if(titles[i].toLowerCase().contains("color"))
				{
					if(!cbox[i].getSelectedItem().toString().equals("Any") &&
						!cbox[i].getSelectedItem().toString().equals("?"))
							detail.append(", " + cbox[i].getSelectedItem().toString());
				}
				else
					detail.append(", " + titles[i] + ": "+ cbox[i].getSelectedItem().toString());
			}
					
			if(detailTF.getText().isEmpty())
				return detail.toString();
			else
				return detail.toString() + ", " + detailTF.getText();
		}
		
		void clearDetail()
		{
			for(int i = 0; i<cbox.length; i++)	//Clear combo boxes
				cbox[i].setSelectedIndex(0);
			
			detailTF.setText("");			
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == btnOK) 
				this.setVisible(false);
		}
	}	
}
