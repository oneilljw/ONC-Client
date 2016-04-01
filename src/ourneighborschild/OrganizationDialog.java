package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
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
	private JLabel lblOrgID, lblRegion, lblDateChanged, lblChangedBy;
    private JComboBox typeCB, statusCB, collTypeCB;
    private JTextPane otherTP, specialNotesTP, deliverToTP;
    private JLabel lblPYReq, lblPYAssigned, lblPYRec;
    private JTextField nameTF, cyReqTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, phoneTF;
//  private JTextField deliverToTF;
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
//      nav.addEntitySelectionListener(this);

        //set up the edit organization panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Gift Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
//      JPanel op5 = new JPanel(new GridBagLayout());
        JPanel op6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        //set up panel 1
        lblOrgID = new JLabel("No Orgs Yet");
        lblOrgID.setPreferredSize(new Dimension (80, 48));
        lblOrgID.setBorder(BorderFactory.createTitledBorder("Partner ID"));
        lblOrgID.setHorizontalAlignment(JLabel.RIGHT);
        
        nameTF = new JTextField(23);
        nameTF.setBorder(BorderFactory.createTitledBorder("Name (Last, First if individual)"));
        nameTF.addActionListener(dcListener);
                
        String[] types = {"?","Business","Church","School", "Clothing", "Coat", "ONC Shopper"};
        typeCB = new JComboBox(types);
        typeCB.setToolTipText("Type of organization e.g. Business");
        typeCB.setPreferredSize(new Dimension (128, 48));
        typeCB.setBorder(BorderFactory.createTitledBorder("Partner Type"));
        typeCB.addActionListener(dcListener);
        
        collTypeCB = new JComboBox();
        collTypeCB.setModel(new DefaultComboBoxModel(GiftCollection.values()));
        collTypeCB.setPreferredSize(new Dimension (128, 48));
        collTypeCB.setBorder(BorderFactory.createTitledBorder("Collection Type"));
        collTypeCB.addActionListener(dcListener);
                     
        String[] status = {"No Action Yet", "1st Email Sent", "Responded", "2nd Email Sent", "Called, Left Mssg", "Confirmed", "Not Participating"}; 
        statusCB = new JComboBox(status);
        statusCB.setPreferredSize(new Dimension (136, 48));
        statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
        statusCB.addActionListener(dcListener);
        
        op1.add(lblOrgID);
        op1.add(nameTF);
        op1.add(typeCB);
        op1.add(collTypeCB);
        op1.add(statusCB);
        
        //set up panel 2
        streetnumTF = new JTextField(6);
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
        
        cityTF = new JTextField(8);
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
        
        //set up panel 3     
        contact1TF = new JTextField(15);
        contact1TF.setToolTipText("Primary Contact");
        contact1TF.setBorder(BorderFactory.createTitledBorder("1st Contact"));
        contact1TF.addActionListener(dcListener);
        
        email1TF = new JTextField(20);
        email1TF.setToolTipText("Primary Contact e-mail");
        email1TF.setBorder(BorderFactory.createTitledBorder("1st Contact Email"));
        email1TF.addActionListener(dcListener);
        
        phone1TF = new JTextField(12);
        phone1TF.setToolTipText("Primary Contact phone #");
        phone1TF.setBorder(BorderFactory.createTitledBorder("1st Contact Phone"));
        phone1TF.addActionListener(dcListener);
        
        lblDateChanged = new JLabel();
        lblDateChanged.setPreferredSize(new Dimension (136, 48));
        lblDateChanged.setToolTipText("When this information last changed");
        lblDateChanged.setBorder(BorderFactory.createTitledBorder("Date Changed"));
        
        op3.add(contact1TF);
        op3.add(email1TF);
        op3.add(phone1TF);
        op3.add(lblDateChanged);
        
        //set up panel 4
        contact2TF = new JTextField(15);
        contact2TF.setToolTipText("Secondary Contact");
        contact2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact"));
        contact2TF.addActionListener(dcListener);
        
        email2TF = new JTextField(20);
        email2TF.setToolTipText("Secondary Contact e-mail");
        email2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact Email"));
        email2TF.addActionListener(dcListener);
        
        phone2TF = new JTextField(12);
        phone2TF.setToolTipText("Secondary Contact phone #");
        phone2TF.setBorder(BorderFactory.createTitledBorder("2nd Contact Phone"));
        phone2TF.addActionListener(dcListener);
        
        lblChangedBy = new JLabel();
        lblChangedBy.setPreferredSize(new Dimension (136, 48));
        lblChangedBy.setToolTipText("Who last changed this partner's information");
        lblChangedBy.setBorder(BorderFactory.createTitledBorder("Changed By"));
       
        op4.add(contact2TF);
        op4.add(email2TF);
        op4.add(phone2TF);
        op4.add(lblChangedBy);
        
        //set up panel 5
        otherTP = new JTextPane();
        otherTP.setPreferredSize(new Dimension (264, 100));
        SimpleAttributeSet attribs = new SimpleAttributeSet(); 
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        otherTP.setParagraphAttributes(attribs,true);             
	   	otherTP.setEditable(true);
	   	
	    //Create a scroll pane and add the other text pane to it.
        JScrollPane otherTPSP = new JScrollPane(otherTP);
        otherTPSP.setBorder(BorderFactory.createTitledBorder("Other Partner Info"));
              
        specialNotesTP = new JTextPane();
        specialNotesTP.setPreferredSize(new Dimension (264, 100));  
//      StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
//      StyleConstants.setFontSize(attribs, gvs.getFontSize());
//      StyleConstants.setSpaceBelow(attribs, 3);
        specialNotesTP.setParagraphAttributes(attribs,true);             
	   	specialNotesTP.setEditable(true);
	   	
        JScrollPane specialNotesTPSP = new JScrollPane(specialNotesTP);
        specialNotesTPSP.setBorder(BorderFactory.createTitledBorder("Special Notes About Partner"));
                   
        deliverToTP = new JTextPane();
        deliverToTP.setPreferredSize(new Dimension (180, 100));  
//      deliverToTP.setToolTipText("Instructions on where gifts will be delivered");
//      deliverToTP.setBorder(BorderFactory.createTitledBorder("Gift Delivery Info"));
        deliverToTP.setParagraphAttributes(attribs,true);             
        deliverToTP.setEditable(true);
//      deliverToTP.addActionListener(dcListener);
        
        JScrollPane deliverToTPSP = new JScrollPane(deliverToTP);
        deliverToTPSP.setBorder(BorderFactory.createTitledBorder("Gift Delivery Info"));
/*        
        //Set up gridbag layout for 5th panel
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        op5.add(otherTPSP, c);
        c.gridx=2;
        c.gridy=0;
        c.gridwidth=2;
        c.gridheight = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=1.0;
        c.weighty=1.0;
        op5.add(specialNotesTPSP, c);
        c.gridx=4;
        c.gridy=0;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
//        op5.add(cyReqTF, c);
        c.gridx=4;
        c.gridy=1;
        c.gridwidth=1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx=0.5;
        c.weighty=0.5;
        op5.add(deliverToTPSP, c);
*/        
        op5.add(otherTPSP);
        op5.add(specialNotesTPSP);
        op5.add(deliverToTPSP);
        
        //set up panel 6
        Dimension ornDimension = new Dimension(122,48);
        lblPYReq = new JLabel();
        lblPYReq.setPreferredSize(ornDimension);
        lblPYReq.setToolTipText("Number of prior year ornaments reqeusted by partner");
        lblPYReq.setBorder(BorderFactory.createTitledBorder("Prior Yr Req"));
        lblPYReq.setHorizontalAlignment(JLabel.RIGHT);
//        pyReqTF.addActionListener(dcListener);
        
        lblPYAssigned = new JLabel();
        lblPYAssigned.setPreferredSize(ornDimension);
        lblPYAssigned.setToolTipText("Number of prior year ornaments assigned to partner");
        lblPYAssigned.setBorder(BorderFactory.createTitledBorder("Prior Yr Assig"));
        lblPYAssigned.setHorizontalAlignment(JLabel.RIGHT);
//        pyAssignedTF.setHorizontalAlignment(JTextField.RIGHT);
//        pyAssignedTF.addActionListener(dcListener);
        
        lblPYRec = new JLabel();
        lblPYRec.setPreferredSize(ornDimension);
        lblPYRec.setToolTipText("Number of prior year gifts received from partner");
        lblPYRec.setBorder(BorderFactory.createTitledBorder("Prior Yr Rec"));
        lblPYRec.setHorizontalAlignment(JLabel.RIGHT);
//        pyRecTF.setHorizontalAlignment(JT.RIGHT);
//        pyRecTF.addActionListener(dcListener);
        
        cyReqTF = new JTextField();
        cyReqTF.setPreferredSize(ornDimension);
        cyReqTF.setToolTipText("Number of ornaments reqeusted by partner");
        cyReqTF.setBorder(BorderFactory.createTitledBorder("Curr Yr Req"));
        cyReqTF.setHorizontalAlignment(JTextField.RIGHT);
        cyReqTF.addActionListener(dcListener);
        
        lblCYAssigned = new JLabel();
        lblCYAssigned.setPreferredSize(ornDimension);
        lblCYAssigned.setToolTipText("Number of ornnaments assigned to this partner");
        lblCYAssigned.setBorder(BorderFactory.createTitledBorder("Curr Yr Assig"));
        lblCYAssigned.setHorizontalAlignment(JLabel.RIGHT);
        
        lblCYRec = new JLabel();
        lblCYRec.setPreferredSize(ornDimension);
        lblCYRec.setToolTipText("Number of current year gifts received partner");
        lblCYRec.setBorder(BorderFactory.createTitledBorder("Curr Yr Rec"));
        lblCYRec.setHorizontalAlignment(JLabel.RIGHT);
        
        op6.add(lblPYReq);
        op6.add(lblPYAssigned);
        op6.add(lblPYRec);
        op6.add(cyReqTF);
        op6.add(lblCYAssigned);
        op6.add(lblCYRec);
        
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
	
	void updateComboBoxBorders()
	{
        //determine year
        GlobalVariables gvs = GlobalVariables.getInstance();
        Calendar seasonStartCal = Calendar.getInstance();
        seasonStartCal.setTime(gvs.getSeasonStartDate());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        
        String currentYear = sdf.format(seasonStartCal.getTime());
        
        seasonStartCal.add(Calendar.YEAR, -1);
        String priorYear = sdf.format(seasonStartCal.getTime());
        
        lblPYReq.setBorder(BorderFactory.createTitledBorder(priorYear + " Request"));
        lblPYAssigned.setBorder(BorderFactory.createTitledBorder(priorYear + " Assigned"));
        lblPYRec.setBorder(BorderFactory.createTitledBorder(priorYear + " Received"));
        
        cyReqTF.setBorder(BorderFactory.createTitledBorder(currentYear + " Request"));
        lblCYAssigned.setBorder(BorderFactory.createTitledBorder(currentYear + " Assigned"));
        lblCYRec.setBorder(BorderFactory.createTitledBorder(currentYear + " Received"));
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
			typeCB.setSelectedIndex(currOrg.getType());
			collTypeCB.setSelectedItem(currOrg.getGiftCollectionType());
			
			//Can't change stats or collection type of organization with ornaments assigned
			if(currOrg.getNumberOfOrnamentsAssigned() == 0)
			{
				statusCB.setEnabled(true);
				collTypeCB.setEnabled(true);
			}
			else
			{
				statusCB.setEnabled(false);
				collTypeCB.setEnabled(false);
			}
			
			//CANNOT DELETE A ORGANIZATION THAT IS CONFIRMED
			if(currOrg.getStatus() != CONFIRMED_STATUS_INDEX)
				btnDelete.setEnabled(true);	
			else
				btnDelete.setEnabled(false);
			
//			cyAssignedTF.setText(currOrg.getOrnamentDelivery());
			cyReqTF.setText(Integer.toString(currOrg.getNumberOfOrnamentsRequested()));
			lblCYAssigned.setText(Integer.toString(currOrg.getNumberOfOrnamentsAssigned()));
			lblCYRec.setText(Integer.toString(currOrg.getNumberOfOrnamentsReceived()));
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
			deliverToTP.setText(currOrg.getDeliverTo());
			deliverToTP.setCaretPosition(0);
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
			lblDateChanged.setText(sdf.format(currOrg.getDateChanged()));
			lblChangedBy.setText(currOrg.getChangedBy());
			contact1TF.setText(currOrg.getContact());
			contact1TF.setCaretPosition(0);
			email1TF.setText(currOrg.getContact_email());
			email1TF.setCaretPosition(0);
			phone1TF.setText(currOrg.getContact_phone());
			phone1TF.setCaretPosition(0);
			contact2TF.setText(currOrg.getContact2());
			contact2TF.setCaretPosition(0);
			email2TF.setText(currOrg.getContact2_email());
			email2TF.setCaretPosition(0);
			phone2TF.setText(currOrg.getContact2_phone());
			phone1TF.setCaretPosition(0);
			lblPYReq.setText(Integer.toString(currOrg.getPriorYearRequested()));
			lblPYAssigned.setText(Integer.toString(currOrg.getPriorYearAssigned()));
			lblPYRec.setText(Integer.toString(currOrg.getPriorYearReceived()));
		
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
			if(reqOrg.getStatus() != CONFIRMED_STATUS_INDEX || reqOrg.getNumberOfOrnamentsAssigned() == 0)
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
		if(!collTypeCB.getSelectedItem().equals(reqOrg.getGiftCollectionType()))
		{
			//The organization collection type has changed, store the new type and update the 
			//confirmed organization list since changes between general and ornament affect 
			//the organization selection lists in other ui's
			reqOrg.setGiftCollectionType((GiftCollection) collTypeCB.getSelectedItem());
			bCD = true;
		}
//		if(!cyAssignedTF.getText().equals(reqOrg.getOrnamentDelivery())) { reqOrg.setOrnamentDelivery(cyAssignedTF.getText()); bCD = true; }
		
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
		if(!deliverToTP.getText().equals(reqOrg.getDeliverTo())) { reqOrg.setDeliverTo(deliverToTP.getText()); bCD = true; }
		if(!contact1TF.getText().equals(reqOrg.getContact())) { reqOrg.setContact(contact1TF.getText()); bCD = true; }
		if(!email1TF.getText().equals(reqOrg.getContact_email())) { reqOrg.setContact_email(email1TF.getText()); bCD = true; }
		if(!phone1TF.getText().equals(reqOrg.getContact_phone())) { reqOrg.setContact_phone(phone1TF.getText()); bCD = true; }
		if(!contact2TF.getText().equals(reqOrg.getContact2())) { reqOrg.setContact2(contact2TF.getText()); bCD = true; }
		if(!email2TF.getText().equals(reqOrg.getContact2_email())) { reqOrg.setContact2_email(email2TF.getText()); bCD = true; }
		if(!phone2TF.getText().equals(reqOrg.getContact2_phone())) { reqOrg.setContact2_phone(phone2TF.getText()); bCD = true; }
		
		if(bCD)	//If an update to organization data (not stop light data) was detected
		{
			reqOrg.setDateChanged(gvs.getTodaysDate());
			reqOrg.setChangedBy(GlobalVariables.getUserLNFI());
//			reqOrg.setStoplightChangedBy(gvs.getUserLNFI());
			
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
		collTypeCB.setSelectedIndex(0);
//		cyAssignedTF.setText("");
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
		deliverToTP.setText("");
		contact1TF.setText("");
		email1TF.setText("");
		phone1TF.setText("");
		contact2TF.setText("");
		email2TF.setText("");
		phone2TF.setText("");
		lblPYReq.setText("");
		lblPYAssigned.setText("");
		lblPYRec.setText("");
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
				processDeletedEntity(odOrgs);
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
		Organization newOrg = new Organization(-1, new Date(), GlobalVariables.getUserLNFI(),
				3, "Partner Created", GlobalVariables.getUserLNFI(),
				statusCB.getSelectedIndex(), typeCB.getSelectedIndex(),
				(GiftCollection) collTypeCB.getSelectedItem(), nameTF.getText(), "", 
				streetnumTF.getText().isEmpty() ? 0 : Integer.parseInt(streetnumTF.getText()),
				streetnameTF.getText(), unitTF.getText(), cityTF.getText(), zipTF.getText(), 
				phoneTF.getText(), cyReqTF.getText().isEmpty() ? 0 : Integer.parseInt(cyReqTF.getText()),
				otherTP.getText(), deliverToTP.getText(), specialNotesTP.getText(), 
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
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_PARTNERS"))
		{
			updateComboBoxBorders();
		}
		else if(dbe.getSource() != this &&
				(dbe.getType().equals("WISH_PARTNER_CHANGED") || dbe.getType().equals("WISH_RECEIVED")))
		{
			DataChange change = (DataChange) dbe.getObject();
			
//			System.out.println(String.format("OrgDlg DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			//if current partner being displayed gifts assigned or received have changed,
			//refresh the displayed data
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
			if(tse.getSource() != nav && tse.getType() == EntityType.PARTNER)
			{
				Organization partner = (Organization) tse.getObject1();
				update();
				nav.setIndex(odOrgs.getListIndexByID(odOrgs.getList(), partner.getID()));
				display(partner);
			}
			else if(tse.getSource() == nav && tse.getType() == EntityType.PARTNER)
			{
				update();
				display(odOrgs.getObjectAtIndex(nav.getIndex()));
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.PARTNER);
	}
}
