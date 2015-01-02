package OurNeighborsChild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.gson.Gson;

public class OrganizationDialog extends EntityDialog implements EntitySelectionListener
{
	/***********************************************************************************************
	 * Implements a dialog to manage ONC Organizations. This dialog looks like the main screen and 
	 * allows the user to scroll thru the organization data base, search the data base for
	 * organizations by ID, name or contact email address, edit the various fields that are
	 * contained in an organization object, and add and delete organizations from the data base.
	 * 
	 * The organization data base is implemented by the ONCOrgs class. The data base is an array list
	 * of Organization objects. 
	 *********************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int	CONFIRMED_STATUS_INDEX = 5;
	
	private ONCRegions regions;
	
	private JLabel lblCYAssigned, lblCYRec;
	private JLabel lblOrgID, lblRegion, lblDateChanged;
    private JComboBox typeCB, statusCB, collTypeCB;
    private JTextPane otherTP, specialNotesTP;
    private JLabel lblPYReq, lblPYAssigned, lblPYRec;
    private JTextField nameTF, cyReqTF, cyAssignedTF, cyRecTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, phoneTF;
    private JTextField deliverToTF;
    private JTextField contact1TF, email1TF, phone1TF;
    private JTextField contact2TF, email2TF, phone2TF;
  
    private ONCOrgs odOrgs;
    private int orgcount = 0, wishcount = 0;	//Holds the status panel overall counts
    
    private Organization currOrg;	//reference to org being displayed

	OrganizationDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Our Neighbor's Child - Gift Partner Information");
		
		regions = ONCRegions.getInstance();
		odOrgs = ONCOrgs.getInstance();
		
		//register to receive changed data events
		//register to listen for family and child data changed events
		if(odOrgs != null)
			odOrgs.addDatabaseListener(this);
		
		ChildDB childDB = ChildDB.getInstance();	//Listen for deleted child
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		ChildWishDB childwishDB = ChildWishDB.getInstance();
		if(childwishDB != null)
			childwishDB.addDatabaseListener(this);	//listen for partner gift assignment changes

		//Create a content panel for the dialog and add panel components to it.
        JPanel odContentPane = new JPanel();
        odContentPane.setLayout(new BoxLayout(odContentPane, BoxLayout.PAGE_AXIS));
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(parentFrame, odOrgs);
        nav.setDefaultMssg("Our Neighbor's Child Gift Partners");
        nav.setCount1("Confirmed: " + Integer.toString(0));
        nav.setCount2("Assigned: " + Integer.toString(0));
        nav.setNextButtonText("Next Partner");
        nav.setPreviousButtonText("Previous Partner");
        nav.addEntitySelectionListener(this);

        //set up the edit organization panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op4 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
        JPanel op5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        lblOrgID = new JLabel("No Orgs Yet");
        lblOrgID.setPreferredSize(new Dimension (80, 48));
        lblOrgID.setBorder(BorderFactory.createTitledBorder("Partner ID"));
        lblOrgID.setHorizontalAlignment(JLabel.RIGHT);
        
        nameTF = new JTextField(24);
        nameTF.setBorder(BorderFactory.createTitledBorder("Name (Last, First if individual)"));
        nameTF.addActionListener(dcListener);
                
        String[] types = {"?","Business","Church","School", "Clothing", "Coat", "ONC Shopper"};
        typeCB = new JComboBox(types);
        typeCB.setToolTipText("Type of organization e.g. Business");
        typeCB.setPreferredSize(new Dimension (132, 48));
        typeCB.setBorder(BorderFactory.createTitledBorder("Type"));
        typeCB.addActionListener(dcListener);
        
        String[] status = {"No Action Yet", "1st Email Sent", "Responded", "2nd Email Sent", "Called, Left Mssg", "Confirmed", "Not Participating"}; 
        statusCB = new JComboBox(status);
        statusCB.setPreferredSize(new Dimension (132, 48));
        statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
        statusCB.addActionListener(dcListener);
        
        String[] cType = {"Unknown", "General", "Ornament"}; 
        collTypeCB = new JComboBox(cType);
        collTypeCB.setPreferredSize(new Dimension (132, 48));
        collTypeCB.setBorder(BorderFactory.createTitledBorder("Collection Type"));
        collTypeCB.addActionListener(dcListener);
                     
        op1.add(lblOrgID);
        op1.add(nameTF);
        op1.add(typeCB);
        op1.add(statusCB);
        op1.add(collTypeCB);
        
        //set up the third panel for ornament information
        //determine year
        GlobalVariables gvs = GlobalVariables.getInstance();
        Calendar seasonStartCal = Calendar.getInstance();
        seasonStartCal.setTime(gvs.getSeasonStartDate());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String currentYear = sdf.format(seasonStartCal.getTime());
        
        seasonStartCal.add(Calendar.YEAR, -1);
        String priorYear = sdf.format(seasonStartCal.getTime());
        
        Dimension ornDimension = new Dimension(112,48);
        lblPYReq = new JLabel();
        lblPYReq.setPreferredSize(ornDimension);
        lblPYReq.setToolTipText("Number of prior year ornaments reqeusted by partner");
        lblPYReq.setBorder(BorderFactory.createTitledBorder(priorYear + " Requested"));
//        pyReqTF.setHorizontalAlignment(JTextField.RIGHT);
//        pyReqTF.addActionListener(dcListener);
        
        lblPYAssigned = new JLabel();
        lblPYAssigned.setPreferredSize(ornDimension);
        lblPYAssigned.setToolTipText("Number of prior year ornaments assigned to partner");
        lblPYAssigned.setBorder(BorderFactory.createTitledBorder(priorYear + " Assigned"));
//        pyAssignedTF.setHorizontalAlignment(JTextField.RIGHT);
//        pyAssignedTF.addActionListener(dcListener);
        
        lblPYRec = new JLabel();
        lblPYRec.setPreferredSize(ornDimension);
        lblPYRec.setToolTipText("Number of prior year gifts received from partner");
        lblPYRec.setBorder(BorderFactory.createTitledBorder(priorYear + " Received"));
//        pyRecTF.setHorizontalAlignment(JT.RIGHT);
//        pyRecTF.addActionListener(dcListener);
        
        cyReqTF = new JTextField();
        cyReqTF.setPreferredSize(ornDimension);
        cyReqTF.setToolTipText("Number of ornaments reqeusted by partner");
        cyReqTF.setBorder(BorderFactory.createTitledBorder(currentYear + " Requested"));
        cyReqTF.setHorizontalAlignment(JTextField.RIGHT);
        cyReqTF.addActionListener(dcListener);
        
        lblCYAssigned = new JLabel();
        lblCYAssigned.setPreferredSize(ornDimension);
        lblCYAssigned.setToolTipText("Number of ornnaments assigned to this partner");
        lblCYAssigned.setBorder(BorderFactory.createTitledBorder(currentYear + " Assigned"));
        
        lblCYRec = new JLabel();
        lblCYRec.setPreferredSize(ornDimension);
        lblCYRec.setToolTipText("Number of current year gifts received partner");
        lblCYRec.setBorder(BorderFactory.createTitledBorder(currentYear + " Received"));
        
        op3.add(lblPYReq);
        op3.add(lblPYAssigned);
        op3.add(lblPYRec);
        op3.add(cyReqTF);
        op3.add(lblCYAssigned);
        op3.add(lblCYRec);
                     
        cyAssignedTF = new JTextField(8);
        cyAssignedTF.setToolTipText("Enter Current Year Ornaments Assigned to Partner");
        cyAssignedTF.setBorder(BorderFactory.createTitledBorder(currentYear + " Assigned"));
        
        otherTP = new JTextPane();
        otherTP.setPreferredSize(new Dimension (168, 88));
        SimpleAttributeSet attribs = new SimpleAttributeSet(); 
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        otherTP.setParagraphAttributes(attribs,true);             
	   	otherTP.setEditable(true);
	   	
	    //Create a scroll pane and add the other text pane to it.
        JScrollPane otherTPSP = new JScrollPane(otherTP);
        otherTPSP.setToolTipText("Other Information");
        otherTPSP.setBorder(BorderFactory.createTitledBorder("Other"));
              
        specialNotesTP = new JTextPane();
        specialNotesTP.setPreferredSize(new Dimension (168, 88));  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        specialNotesTP.setParagraphAttributes(attribs,true);             
	   	specialNotesTP.setEditable(true);
	   	
        JScrollPane specialNotesTPSP = new JScrollPane(specialNotesTP);
        specialNotesTPSP.setToolTipText("Special Notes");
        specialNotesTPSP.setBorder(BorderFactory.createTitledBorder("Special Notes"));
                   
        deliverToTF = new JTextField(10);
        deliverToTF.setToolTipText("Instructions on where gifts will be delivered");
        deliverToTF.setBorder(BorderFactory.createTitledBorder("Gift Delivery"));
        deliverToTF.addActionListener(dcListener);
        
        lblDateChanged = new JLabel();
        lblDateChanged.setPreferredSize(new Dimension (120, 48));
        lblDateChanged.setToolTipText("When this information last changed");
        lblDateChanged.setBorder(BorderFactory.createTitledBorder("Date Changed"));
        
        //Set up gridbag layout for 4th panel
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        op4.add(otherTPSP, c);
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        op4.add(specialNotesTPSP, c);
        c.gridx=4;
        c.gridy=0;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
//        op4.add(cyReqTF, c);
        c.gridx=4;
        c.gridy=1;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
        op4.add(deliverToTF, c);
        c.gridx=5;
        c.gridy=0;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
//        op4.add(lblCYAssigned, c);
        c.gridx=5;
        c.gridy=1;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
        op4.add(lblDateChanged, c);
        
        streetnumTF = new JTextField(4);
        streetnumTF.setToolTipText("Address of partner");
        streetnumTF.setBorder(BorderFactory.createTitledBorder("St. #"));
        streetnumTF.addActionListener(dcListener);
        
        streetnameTF = new JTextField(14);
        streetnameTF.setToolTipText("Address of partner");
        streetnameTF.setBorder(BorderFactory.createTitledBorder("Street"));
        streetnameTF.addActionListener(dcListener);
            
        unitTF = new JTextField(7);
        unitTF.setToolTipText("Address of partner");
        unitTF.setBorder(BorderFactory.createTitledBorder("Unit #"));
        unitTF.addActionListener(dcListener);
        
        cityTF = new JTextField(7);
        cityTF.setToolTipText("Address of partner");
        cityTF.setBorder(BorderFactory.createTitledBorder("City"));
        cityTF.addActionListener(dcListener);
        
        zipTF = new JTextField(4);
        zipTF.setToolTipText("Address of partner");
        zipTF.setBorder(BorderFactory.createTitledBorder("Zip"));
        zipTF.addActionListener(dcListener);
        
        phoneTF = new JTextField(10);
        phoneTF.setToolTipText("Partner phone #");
        phoneTF.setBorder(BorderFactory.createTitledBorder("Phone #")); 
        phoneTF.addActionListener(dcListener);
              
        lblRegion = new JLabel("?", JLabel.CENTER);
        lblRegion.setToolTipText("ONC Region Location of this fulfillment partner");
        lblRegion.setPreferredSize(new Dimension (60, 48));
        lblRegion.setBorder(BorderFactory.createTitledBorder("Region"));
        
        op2.add(streetnumTF);
        op2.add(streetnameTF);
        op2.add(unitTF);
        op2.add(cityTF);
        op2.add(zipTF);
        op2.add(phoneTF);
        op2.add(lblRegion);   
              
        contact1TF = new JTextField(16);
        contact1TF.setToolTipText("Primary Contact");
        contact1TF.setBorder(BorderFactory.createTitledBorder("1st Contact"));
        contact1TF.addActionListener(dcListener);
        
        email1TF = new JTextField(22);
        email1TF.setToolTipText("Primary Contact e-mail");
        email1TF.setBorder(BorderFactory.createTitledBorder("1st Contact Email"));
        email1TF.addActionListener(dcListener);
        
        phone1TF = new JTextField(20);
        phone1TF.setToolTipText("Primary Contact phone #");
        phone1TF.setBorder(BorderFactory.createTitledBorder("1st Contact Phone"));
        phone1TF.addActionListener(dcListener);
        
        op5.add(contact1TF);
        op5.add(email1TF);
        op5.add(phone1TF);
              
        contact2TF = new JTextField(16);
        contact2TF.setToolTipText("Secondary Contact");
        contact2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact"));
        contact2TF.addActionListener(dcListener);
        
        email2TF = new JTextField(22);
        email2TF.setToolTipText("Secondary Contact e-mail");
        email2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact Email"));
        email2TF.addActionListener(dcListener);
        
        phone2TF = new JTextField(20);
        phone2TF.setToolTipText("Secondary Contact phone #");
        phone2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact Phone"));
        phone2TF.addActionListener(dcListener);
       
        op6.add(contact2TF);
        op6.add(email2TF);
        op6.add(phone2TF);
        
        entityPanel.add(op1);
        entityPanel.add(op2);
        entityPanel.add(op3);
        entityPanel.add(op4);
        entityPanel.add(op5);
        entityPanel.add(op6);
        
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New Partner");
    	btnNew.setToolTipText("Click to add a new partner");
        
        btnDelete.setText("Delete Partner");
    	btnDelete.setToolTipText("Click to delete this partner");
        
        btnSave.setText("Save New Partner");
    	btnSave.setToolTipText("Click to save the new partner");
        
        btnCancel.setText("Cancel Add New Partner");
    	btnCancel.setToolTipText("Click to cancel adding a new partner");
       
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        pack();
        setResizable(true);
        Point pt = parentFrame.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void display(ONCEntity partner)	//displays currOrg
	{
		if(odOrgs.size() <=0 )
		{
			currOrg = null;
			clear();
			lblOrgID.setText("No Orgs Yet");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else
		{
			//Determine what to display based on currDriver and driver
			if(currOrg == null && partner == null)
				currOrg = odOrgs.getObjectAtIndex(0);
			else if(partner != null  && currOrg != partner)
				currOrg = (Organization) partner;
			
			bIgnoreEvents = true;
			
//			currOrg = odOrgs.getObjectAtIndex(nav.getIndex());
		
			lblOrgID.setText(Long.toString(currOrg.getID()));
			nameTF.setText(currOrg.getName());
			nameTF.setCaretPosition(0);
			statusCB.setSelectedIndex(currOrg.getStatus());
			
			//CANNOT CHAGE STATUS OF A CONFIRMED ORGANIZATION WITH ASSIGNED ORNAMENTS
			if(currOrg.getNumberOfOrnammentsAssigned() == 0)	
				statusCB.setEnabled(true);	
			else
				statusCB.setEnabled(false);
			
			//CANNOT DELETE A ORGANIZATION THAT IS CONFIRMED
			if(currOrg.getStatus() != CONFIRMED_STATUS_INDEX)
				btnDelete.setEnabled(true);	
			else
				btnDelete.setEnabled(false);
	
			typeCB.setSelectedIndex(currOrg.getType());		
			cyAssignedTF.setText(currOrg.getOrnamentDelivery());
			cyReqTF.setText(Integer.toString(currOrg.getNumberOfOrnamentsRequested()));
			lblCYAssigned.setText(Integer.toString(currOrg.getNumberOfOrnammentsAssigned()));
			otherTP.setText(currOrg.getOther());
			otherTP.setCaretPosition(0);
			specialNotesTP.setText(currOrg.getSpecialNotes());
			specialNotesTP.setCaretPosition(0);
			streetnumTF.setText(Integer.toString(currOrg.getStreetnum()));
			streetnameTF.setText(currOrg.getStreetname());
			unitTF.setText(currOrg.getUnit());
			cityTF.setText(currOrg.getCity());
			zipTF.setText(currOrg.getZipcode());
			phoneTF.setText(currOrg.getPhone());
			phoneTF.setCaretPosition(0);
			lblRegion.setText(regions.getRegionID(currOrg.getRegion()));
			deliverToTF.setText(currOrg.getDeliverTo());
			deliverToTF.setCaretPosition(0);
			SimpleDateFormat sdf = new SimpleDateFormat("MMMMM dd, yyyy");
			lblDateChanged.setText(sdf.format(currOrg.getDateChanged()));
			contact1TF.setText(currOrg.getContact());
			email1TF.setText(currOrg.getContact_email());
			email1TF.setCaretPosition(0);
			phone1TF.setText(currOrg.getContact_phone());
			phone1TF.setCaretPosition(0);
			contact2TF.setText(currOrg.getContact2());
			email2TF.setText(currOrg.getContact2_email());
			email2TF.setCaretPosition(0);
			phone2TF.setText(currOrg.getContact2_phone());
			phone1TF.setCaretPosition(0);
		
			int[] counts = odOrgs.getOrnamentAndWishCounts();
			orgcount = counts[0];
			wishcount = counts[1];
			nav.setCount1("Confirmed: " + Integer.toString(orgcount));
			nav.setCount2("Assigned: " + Integer.toString(wishcount));
			
			nav.setStoplightEntity(currOrg);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			bIgnoreEvents = false;
		}
	}
	
	void update()
	{
		//Check to see if user has changed any field, if so, save it
		Organization reqOrg;
		
		if(currOrg != null)
			reqOrg = new Organization(currOrg);	//make a copy for update request
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(this, "ONC Organizaiton Dialog Error:," +
					"No current partner","ONC Org Dialog Error",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			return;	//If no current org, should never have gotten an update request
		}
		
		int n;
		boolean bCD = false; //used to indicate a change has been detected
		
		if(!nameTF.getText().equals(reqOrg.getName())) { reqOrg.setName(nameTF.getText()); bCD = true; }
		if(statusCB.getSelectedIndex() !=reqOrg.getStatus())
		{
			//Can only change status if not confirmed or if confirmed and no ornaments assigned
			if(reqOrg.getStatus() != CONFIRMED_STATUS_INDEX || reqOrg.getNumberOfOrnammentsAssigned() == 0)
			{
				reqOrg.setStatus(statusCB.getSelectedIndex());
				bCD = true;
			}
		}
		if(typeCB.getSelectedIndex() != reqOrg.getType())
		{
			//The organization type has changed, store the new type and update the 
			//confirmed organization list since changes between gift organizations 
			//and clothing and coat donors are displayed differently
			//in the confirmed organization list. 
			reqOrg.setType(typeCB.getSelectedIndex());
//			odOrgs.structureConfirmedOrgsList();
			bCD = true;
		}
		if(!cyAssignedTF.getText().equals(reqOrg.getOrnamentDelivery())) { reqOrg.setOrnamentDelivery(cyAssignedTF.getText()); bCD = true; }
		
		if(cyReqTF.getText().isEmpty())
			reqOrg.setNumberOfOrnamentsRequested(0);
		else if((n=Integer.parseInt(cyReqTF.getText().trim().replaceAll(",", ""))) != 
					reqOrg.getNumberOfOrnamentsRequested())
		{
			reqOrg.setNumberOfOrnamentsRequested(n);
			bCD = true;
		}
		
		if(!otherTP.getText().equals(reqOrg.getOther())) {reqOrg.setOther(otherTP.getText()); bCD = true; }
		if(!specialNotesTP.getText().equals(reqOrg.getSpecialNotes())) { reqOrg.setSpecialNotes(specialNotesTP.getText()); bCD = true; }
		
		if(streetnumTF.getText().isEmpty())
		{
			reqOrg.setStreetnum(0);
			bCD = true;
		}
		else if((n=Integer.parseInt(streetnumTF.getText().trim())) != reqOrg.getStreetnum())
		{
			reqOrg.setStreetnum(n);
			bCD = true;
		}
		
		if(!streetnameTF.getText().equals(reqOrg.getStreetname()))
		{
			reqOrg.setStreetname(streetnameTF.getText());
			bCD = true;
		}
		if(!unitTF.getText().equals(reqOrg.getUnit())) { reqOrg.setUnit(unitTF.getText()); bCD = true; }
		if(!cityTF.getText().equals(reqOrg.getCity())) { reqOrg.setCity(cityTF.getText()); bCD = true; }
		if(!zipTF.getText().equals(reqOrg.getZipcode())) { reqOrg.setZipcode(zipTF.getText()); bCD = true; }
		if(!phoneTF.getText().equals(reqOrg.getPhone())) { reqOrg.setPhone(phoneTF.getText()); bCD = true; }
		if(!deliverToTF.getText().equals(reqOrg.getDeliverTo())) { reqOrg.setDeliverTo(deliverToTF.getText()); bCD = true; }
		if(!contact1TF.getText().equals(reqOrg.getContact())) { reqOrg.setContact(contact1TF.getText()); bCD = true; }
		if(!email1TF.getText().equals(reqOrg.getContact_email())) { reqOrg.setContact_email(email1TF.getText()); bCD = true; }
		if(!phone1TF.getText().equals(reqOrg.getContact_phone())) { reqOrg.setContact_phone(phone1TF.getText()); bCD = true; }
		if(!contact2TF.getText().equals(reqOrg.getContact2())) { reqOrg.setContact2(contact2TF.getText()); bCD = true; }
		if(!email2TF.getText().equals(reqOrg.getContact2_email())) { reqOrg.setContact2_email(email2TF.getText()); bCD = true; }
		if(!phone2TF.getText().equals(reqOrg.getContact2_phone())) { reqOrg.setContact2_phone(phone2TF.getText()); bCD = true; }
		
		if(bCD)	//If an update to organization data (not stop light data) was detected
		{
			reqOrg.setDateChanged(gvs.getTodaysDate());
			reqOrg.setStoplightChangedBy(gvs.getUserLNFI());
			
			String response = odOrgs.update(this, reqOrg);	//notify the database of the change
			
			if(response.startsWith("UPDATED_PARTNER"))
			{
				display(reqOrg);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Partner Update," +
						"try again later","Partner Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currOrg);
			}
					
			bCD = false;
		}
	}

	void clear()
	{
		bIgnoreEvents = true;
		
		lblOrgID.setText("");
		nameTF.setText("");		
		statusCB.setSelectedIndex(0);
		typeCB.setSelectedIndex(0);		
		cyAssignedTF.setText("");
		cyReqTF.setText("");
		lblCYAssigned.setText("0");
		otherTP.setText("");
		specialNotesTP.setText("");
		streetnumTF.setText("");
		streetnameTF.setText("");
		unitTF.setText("");
		cityTF.setText("");
		zipTF.setText("");
		phoneTF.setText("");
		lblRegion.setText("?");
		deliverToTF.setText("");
		contact1TF.setText("");
		email1TF.setText("");
		phone1TF.setText("");
		contact2TF.setText("");
		email2TF.setText("");
		phone2TF.setText("");
		nav.clearStoplight();
		
		bIgnoreEvents = false;
	}
	
	void setTextPaneFontSize()
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        otherTP.setParagraphAttributes(attribs, true);
        specialNotesTP.setParagraphAttributes(attribs, true);
	}

	void onNew()
	{
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		entityPanel.setBorder(BorderFactory.createTitledBorder("Enter New Partner's Information"));
		clear();
		statusCB.setEnabled(true);	//If was disabled, enable now
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		setControlState();
		lblOrgID.setText("New");
	}
	
	void onDelete()
	{
		Organization delOrg = odOrgs.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("Are you sure you want to delete %s from the data base?", 
											delOrg.getName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Partner Database Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			String response = odOrgs.delete(this, delOrg);
			
			if(response.startsWith("DELETED_PARTNER"))
			{
				processDeletedPartner(odOrgs);
			}
			else
			{
				String err_mssg = "ONC Server denied delete partner request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete Partner Request Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currOrg);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new organization from user input	
		Organization newOrg = new Organization(-1, new Date(), 3, "Partner Created", gvs.getUserLNFI(),
				statusCB.getSelectedIndex(), typeCB.getSelectedIndex(), nameTF.getText(), "", 
				streetnumTF.getText().isEmpty() ? 0 : Integer.parseInt(streetnumTF.getText()),
				streetnameTF.getText(), unitTF.getText(), cityTF.getText(), zipTF.getText(), 
				phoneTF.getText(), cyReqTF.getText().isEmpty() ? 0 : Integer.parseInt(cyReqTF.getText()),
				otherTP.getText(), deliverToTF.getText(), specialNotesTP.getText(), 
				contact1TF.getText(), email1TF.getText(), phone1TF.getText(),
				contact2TF.getText(), email2TF.getText(), phone2TF.getText());
		
		//send request to add new partner/organization to the local data base
		String response = odOrgs.add(this, newOrg);
		
		if(response.startsWith("ADDED_PARTNER"))
		{
			//update the ui with new partner id assigned by the server 
			Gson gson = new Gson();
			Organization addedOrg = gson.fromJson(response.substring(13), Organization.class);
			
			//set the display index, on, to the new partner added and display organization
			nav.setIndex(odOrgs.getListIndexByID(odOrgs.getList(), addedOrg.getID()));
			display(addedOrg);
		}
		else
		{
			String err_mssg = "ONC Server denied add partner request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Partner Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currOrg);
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
		display(currOrg);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	 
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_PARTNER"))
		{
			Organization updatedOrg = (Organization) dbe.getObject();
			
			//If current partner is being displayed has changed, reshow it
			if(isNumeric(lblOrgID.getText()) && Integer.parseInt(lblOrgID.getText()) == updatedOrg.getID() && !bAddingNewEntity)
				display(updatedOrg); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_PARTNER"))
		{
			//if the deleted partner was the only partner in data base, clear the display
			//otherwise, if the deleted partner is currently being displayed, change the on
			//to the next prior partner and display.
			if(odOrgs.size() == 0)
			{
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				Organization deletedOrg = (Organization) dbe.getObject();
				if(Integer.parseInt(lblOrgID.getText()) == deletedOrg.getID())
				{
					if(nav.getIndex() == 0)
						nav.setIndex(odOrgs.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(odOrgs.getObjectAtIndex(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("WISH_PARTNER_CHANGED"))
		{
			DataChange change = (DataChange) dbe.getObject();
			
//			System.out.println(String.format("OrgDlg DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			//If current partner being displayed gifts assigned have changed, refresh the UI
			if(!bAddingNewEntity && currOrg != null && (currOrg.getID() == change.getOldData() ||
															currOrg.getID() == change.getNewData()))
				display(currOrg); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			if(this.isVisible())
			{
				//Assume that partner displayed gift count assigned has changed
				display(currOrg);
			}
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		/*************************************************************************************
		 * If the table selection event is fired by a sortPartnerDialog and the current mode is
		 * not adding a new partner, save any changes to the currently displayed 
		 * partner and display the partner selected in the sort partner dialog partner table.
		 ************************************************************************************/
		if(this.isVisible() && !bAddingNewEntity)
		{
			if(tse.getSource() != nav && tse.getType().equals("PARTNER_SELECTED"))
			{
				Organization partner = (Organization) tse.getObject1();
				update();
				nav.setIndex(odOrgs.getListIndexByID(odOrgs.getList(), partner.getID()));
				display(partner);
			}
			else if(tse.getSource() == nav && tse.getType().equals("PARTNER_SELECTED"))
			{
				update();
				display(odOrgs.getObjectAtIndex(nav.getIndex()));
			}
		}
	}
}
