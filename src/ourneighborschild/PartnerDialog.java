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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.gson.Gson;

public class PartnerDialog extends EntityDialog
{
	/***********************************************************************************************
	 * Implements a dialog to manage ONC Partner organizations. This dialog looks like the main
	 * screen. It allows the user to scroll thru the partner data base, search the data base 
	 * for partners by ID, name or contact email address, edit the various fields that are
	 * contained in a partner object, and add and delete partners from the data base.
	 * 
	 * The partner data base is implemented by the PartnerDB class. The data base is an array list
	 * of ONCPartner objects. 
	 *********************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int	CONFIRMED_STATUS_INDEX = 5;
	
	private ONCRegions regions;
	
	private JLabel lblCYAssigned, lblCYDel, lblCYRecBefore, lblCYRecAfter;
	private JLabel lblOrgID, lblRegion, lblDateChanged, lblChangedBy;
    private JComboBox typeCB, statusCB, collectionCB;
    private JTextPane otherTP, specialNotesTP, deliverToTP;
    private JLabel lblPYReq, lblPYAssigned, lblPYDel, lblPYRecBefore, lblPYRecAfter;
    private JTextField nameTF, cyReqTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, phoneTF;
    private JTextField contact1TF, email1TF, phone1TF;
    private JTextField contact2TF, email2TF, phone2TF;
    private JPanel pyPanel, cyPanel;
    private PartnerDB partnerDB;
    private int partnerCount = 0, wishCount = 0;	//Holds the navigation panel overall counts
    
    private ONCPartner currPartner;	//reference to ONCPartner object being displayed

	PartnerDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("Our Neighbor's Child - Gift Partner Information");
		
		regions = ONCRegions.getInstance();
		partnerDB = PartnerDB.getInstance();
		
		//register to listen for partner, global variable, child and  and childwish
		//data changed events
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
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
        nav = new ONCNavPanel(parentFrame, partnerDB);
        nav.setDefaultMssg("Our Neighbor's Child Gift Partners");
        nav.setCount1("Confirmed: " + Integer.toString(0));
        nav.setCount2("Assigned: " + Integer.toString(0));
        nav.setNextButtonText("Next Partner");
        nav.setPreviousButtonText("Previous Partner");

        //set up the edit organization panel
//      entityPanel.setBorder(BorderFactory.createTitledBorder("Gift Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
//      JPanel op5 = new JPanel(new GridBagLayout());
        JPanel op6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
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
        typeCB.setPreferredSize(new Dimension (136, 48));
        typeCB.setBorder(BorderFactory.createTitledBorder("Partner Type"));
        typeCB.addActionListener(dcListener);
        
        collectionCB = new JComboBox();
        collectionCB.setModel(new DefaultComboBoxModel(GiftCollection.selectionValues()));
        collectionCB.setPreferredSize(new Dimension (128, 48));
        collectionCB.setBorder(BorderFactory.createTitledBorder("Collection Type"));
        collectionCB.addActionListener(dcListener);
                     
        String[] status = {"No Action Yet", "1st Email Sent", "Responded", "2nd Email Sent", "Called, Left Mssg", "Confirmed", "Not Participating"}; 
        statusCB = new JComboBox(status);
        statusCB.setPreferredSize(new Dimension (144, 48));
        statusCB.setBorder(BorderFactory.createTitledBorder("Status"));
        statusCB.addActionListener(dcListener);
        
        op1.add(lblOrgID);
        op1.add(nameTF);
        op1.add(typeCB);
        op1.add(collectionCB);
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
        
        email1TF = new JTextField(22);
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
        
        email2TF = new JTextField(22);
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
        otherTP.setPreferredSize(new Dimension (280, 100));
        SimpleAttributeSet attribs = new SimpleAttributeSet(); 
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        otherTP.setParagraphAttributes(attribs,true);             
	   	otherTP.setEditable(true);
	   	
	    //Create a scroll pane and add the other text pane to it.
        JScrollPane otherTPSP = new JScrollPane(otherTP);
        otherTPSP.setBorder(BorderFactory.createTitledBorder("General Partner Information"));
              
        specialNotesTP = new JTextPane();
        specialNotesTP.setPreferredSize(new Dimension (288, 100));  
        specialNotesTP.setParagraphAttributes(attribs,true);             
	   	specialNotesTP.setEditable(true);
	   	
        JScrollPane specialNotesTPSP = new JScrollPane(specialNotesTP);
        specialNotesTPSP.setBorder(BorderFactory.createTitledBorder("Current Year Notes"));
                   
        deliverToTP = new JTextPane();
        deliverToTP.setPreferredSize(new Dimension (180, 100));  
        deliverToTP.setParagraphAttributes(attribs,true);             
        deliverToTP.setEditable(true);
        
        JScrollPane deliverToTPSP = new JScrollPane(deliverToTP);
        deliverToTPSP.setBorder(BorderFactory.createTitledBorder("Gift Delivery Information"));
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
        Dimension ornDimension = new Dimension(72,48);
        
        Border loweredBevel = BorderFactory.createLoweredBevelBorder();
        TitledBorder pyBorder = BorderFactory.createTitledBorder(loweredBevel, "Prior Year Performance");
        pyBorder.setTitleJustification(TitledBorder.CENTER);
        pyPanel.setBorder(pyBorder);
        
        lblPYReq = new JLabel();
        lblPYReq.setPreferredSize(ornDimension);
        lblPYReq.setToolTipText("Number of prior year ornaments reqeusted by partner");
        lblPYReq.setBorder(BorderFactory.createTitledBorder("Request"));
        lblPYReq.setHorizontalAlignment(JLabel.RIGHT);
        
        lblPYAssigned = new JLabel();
        lblPYAssigned.setPreferredSize(ornDimension);
        lblPYAssigned.setToolTipText("Number of prior year ornaments assigned to partner");
        lblPYAssigned.setBorder(BorderFactory.createTitledBorder("Assign."));
        lblPYAssigned.setHorizontalAlignment(JLabel.RIGHT);
        
        lblPYDel = new JLabel();
        lblPYDel.setPreferredSize(ornDimension);
        lblPYDel.setToolTipText("Number of prior year ornaments delivered to partner");
        lblPYDel.setBorder(BorderFactory.createTitledBorder("Deliv."));
        lblPYDel.setHorizontalAlignment(JLabel.RIGHT);
        
        lblPYRecBefore = new JLabel();
        lblPYRecBefore.setPreferredSize(ornDimension);
        lblPYRecBefore.setToolTipText("Number of prior year gifts received from partner before deadline");
        lblPYRecBefore.setBorder(BorderFactory.createTitledBorder("On Time"));
        lblPYRecBefore.setHorizontalAlignment(JLabel.RIGHT);
        
        lblPYRecAfter = new JLabel();
        lblPYRecAfter.setPreferredSize(ornDimension);
        lblPYRecAfter.setToolTipText("Number of prior year gifts received from partner after deadline");
        lblPYRecAfter.setBorder(BorderFactory.createTitledBorder("Late"));
        lblPYRecAfter.setHorizontalAlignment(JLabel.RIGHT);
        
        TitledBorder cyBorder = BorderFactory.createTitledBorder(loweredBevel, "Current Year Performance");
        cyBorder.setTitleJustification(TitledBorder.CENTER);
        cyPanel.setBorder(cyBorder);
        
        cyReqTF = new JTextField();
        cyReqTF.setPreferredSize(ornDimension);
        cyReqTF.setToolTipText("Number of ornaments reqeusted by partner");
        cyReqTF.setBorder(BorderFactory.createTitledBorder("Request"));
        cyReqTF.setHorizontalAlignment(JTextField.RIGHT);
        cyReqTF.addActionListener(dcListener);
        
        lblCYAssigned = new JLabel();
        lblCYAssigned.setPreferredSize(ornDimension);
        lblCYAssigned.setToolTipText("Number of ornnaments assigned to this partner");
        lblCYAssigned.setBorder(BorderFactory.createTitledBorder("Assign."));
        lblCYAssigned.setHorizontalAlignment(JLabel.RIGHT);
        
        lblCYDel = new JLabel();
        lblCYDel.setPreferredSize(ornDimension);
        lblCYDel.setToolTipText("Number of current year ornaments delivered to partner");
        lblCYDel.setBorder(BorderFactory.createTitledBorder("Deliv."));
        lblCYDel.setHorizontalAlignment(JLabel.RIGHT);
        
        lblCYRecBefore = new JLabel();
        lblCYRecBefore.setPreferredSize(ornDimension);
        lblCYRecBefore.setToolTipText("Number of current year gifts received from partner before the deadline");
        lblCYRecBefore.setBorder(BorderFactory.createTitledBorder("On Time"));
        lblCYRecBefore.setHorizontalAlignment(JLabel.RIGHT);
        
        lblCYRecAfter = new JLabel();
        lblCYRecAfter.setPreferredSize(ornDimension);
        lblCYRecAfter.setToolTipText("Number of current year gifts received from partner after the deadline");
        lblCYRecAfter.setBorder(BorderFactory.createTitledBorder("Late"));
        lblCYRecAfter.setHorizontalAlignment(JLabel.RIGHT);
        
        pyPanel.add(lblPYReq);
        pyPanel.add(lblPYAssigned);
        pyPanel.add(lblPYDel);
        pyPanel.add(lblPYRecBefore);
        pyPanel.add(lblPYRecAfter);
        
        cyPanel.add(cyReqTF);
        cyPanel.add(lblCYAssigned);
        cyPanel.add(lblCYDel);
        cyPanel.add(lblCYRecBefore);
        cyPanel.add(lblCYRecAfter);
        
        op6.add(pyPanel);
        op6.add(cyPanel);
        
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
	
	void updatePartnerPerformanceBorders()
	{
        //determine year
        GlobalVariables gvs = GlobalVariables.getInstance();
        Calendar seasonStartCal = Calendar.getInstance();
        seasonStartCal.setTime(gvs.getSeasonStartDate());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String currentYear = sdf.format(seasonStartCal.getTime());
        
        seasonStartCal.add(Calendar.YEAR, -1);
        String priorYear = sdf.format(seasonStartCal.getTime());
        
        TitledBorder pyBorder = (TitledBorder) pyPanel.getBorder();
        pyBorder.setTitle(priorYear + " Performance");
        
        TitledBorder cyBorder = (TitledBorder) cyPanel.getBorder();
        cyBorder.setTitle(currentYear + " Performance");
	}
	
	void display(ONCEntity partner)	//displays currOrg
	{
		if(partnerDB.size() <=0 )
		{
			currPartner = null;
			clear();
			lblOrgID.setText("No Orgs Yet");	//If no organizations, display this message
			nav.btnNextSetEnabled(false);
			nav.btnPreviousSetEnabled(false);
		}
		else
		{
			//Determine what to display based on currDriver and driver
			if(currPartner == null && partner == null)
				currPartner = partnerDB.getObjectAtIndex(0);
			else if(partner != null  && currPartner != partner)
				currPartner = (ONCPartner) partner;
			
			bIgnoreEvents = true;
			
			lblOrgID.setText(Long.toString(currPartner.getID()));
			nameTF.setText(currPartner.getName());
			nameTF.setCaretPosition(0);
			statusCB.setSelectedIndex(currPartner.getStatus());
			typeCB.setSelectedIndex(currPartner.getType());
			collectionCB.setSelectedItem(currPartner.getGiftCollectionType());
			
			//Can't change stats or collection type of organization with ornaments assigned
			if(currPartner.getNumberOfOrnamentsAssigned() == 0)
			{
				statusCB.setEnabled(true);
				collectionCB.setEnabled(true);
			}
			else
			{
				statusCB.setEnabled(false);
				collectionCB.setEnabled(false);
			}
			
			//CANNOT DELETE A ORGANIZATION THAT IS CONFIRMED
			if(currPartner.getStatus() != CONFIRMED_STATUS_INDEX)
				btnDelete.setEnabled(true);	
			else
				btnDelete.setEnabled(false);
			
			cyReqTF.setText(Integer.toString(currPartner.getNumberOfOrnamentsRequested()));
			lblCYAssigned.setText(Integer.toString(currPartner.getNumberOfOrnamentsAssigned()));
			lblCYDel.setText(Integer.toString(currPartner.getNumberOfOrnamentsDelivered()));
			lblCYRecBefore.setText(Integer.toString(currPartner.getNumberOfOrnamentsReceivedBeforeDeadline()));
			lblCYRecAfter.setText(Integer.toString(currPartner.getNumberOfOrnamentsReceivedAfterDeadline()));
			otherTP.setText(currPartner.getOther());
			otherTP.setCaretPosition(0);
			specialNotesTP.setText(currPartner.getSpecialNotes());
			specialNotesTP.setCaretPosition(0);
			streetnumTF.setText(Integer.toString(currPartner.getStreetnum()));
			streetnameTF.setText(currPartner.getStreetname());
			unitTF.setText(currPartner.getUnit());
			cityTF.setText(currPartner.getCity());
			zipTF.setText(currPartner.getZipcode());
			phoneTF.setText(currPartner.getPhone());
			phoneTF.setCaretPosition(0);
			lblRegion.setText(regions.getRegionID(currPartner.getRegion()));
			deliverToTP.setText(currPartner.getDeliverTo());
			deliverToTP.setCaretPosition(0);
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
			lblDateChanged.setText(sdf.format(currPartner.getDateChanged()));
			lblChangedBy.setText(currPartner.getChangedBy());
			contact1TF.setText(currPartner.getContact());
			contact1TF.setCaretPosition(0);
			email1TF.setText(currPartner.getContact_email());
			email1TF.setCaretPosition(0);
			phone1TF.setText(currPartner.getContact_phone());
			phone1TF.setCaretPosition(0);
			contact2TF.setText(currPartner.getContact2());
			contact2TF.setCaretPosition(0);
			email2TF.setText(currPartner.getContact2_email());
			email2TF.setCaretPosition(0);
			phone2TF.setText(currPartner.getContact2_phone());
			phone1TF.setCaretPosition(0);
			lblPYReq.setText(Integer.toString(currPartner.getPriorYearRequested()));
			lblPYAssigned.setText(Integer.toString(currPartner.getPriorYearAssigned()));
			lblPYDel.setText(Integer.toString(currPartner.getPriorYearDelivered()));
			lblPYRecBefore.setText(Integer.toString(currPartner.getPriorYearReceivedBeforeDeadline()));
			lblPYRecAfter.setText(Integer.toString(currPartner.getPriorYearReceivedAfterDeadline()));
		
			int[] counts = partnerDB.getOrnamentAndWishCounts();
			partnerCount = counts[0];
			wishCount = counts[1];
			nav.setCount1("Confirmed: " + Integer.toString(partnerCount));
			nav.setCount2("Assigned: " + Integer.toString(wishCount));
			
			nav.setStoplightEntity(currPartner);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			bIgnoreEvents = false;
		}
	}
	
	void update()
	{
		//Check to see if user has changed any field, if so, save it
		ONCPartner reqPartner;
		
		if(currPartner != null)
			reqPartner = new ONCPartner(currPartner);	//make a copy for update request
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
		
		if(!nameTF.getText().equals(reqPartner.getName())) { reqPartner.setName(nameTF.getText()); bCD = true; }
		if(statusCB.getSelectedIndex() !=reqPartner.getStatus())
		{
			//Can only change status if not confirmed or if confirmed and no ornaments assigned
			if(reqPartner.getStatus() != CONFIRMED_STATUS_INDEX || reqPartner.getNumberOfOrnamentsAssigned() == 0)
			{
				reqPartner.setStatus(statusCB.getSelectedIndex());
				bCD = true;
			}
		}
		if(typeCB.getSelectedIndex() != reqPartner.getType())
		{
			//The partner type has changed, store the new type and update the 
			//confirmed organization list since changes between gift partners 
			//and clothing and coat donors are displayed differently
			//in the confirmed partner list. 
			reqPartner.setType(typeCB.getSelectedIndex());
			bCD = true;
		}
		if(!collectionCB.getSelectedItem().equals(reqPartner.getGiftCollectionType()))
		{
			//The partner collection type has changed, store the new type and update the 
			//confirmed partnerlist since changes between general and ornament affect 
			//the partner selection lists in other ui's
			reqPartner.setGiftCollectionType((GiftCollection) collectionCB.getSelectedItem());
			bCD = true;
		}
		if(cyReqTF.getText().isEmpty())
			reqPartner.setNumberOfOrnamentsRequested(0);
		else if((n=Integer.parseInt(cyReqTF.getText().trim().replaceAll(",", ""))) != 
					reqPartner.getNumberOfOrnamentsRequested())
		{
			reqPartner.setNumberOfOrnamentsRequested(n);
			bCD = true;
		}
		if(!otherTP.getText().equals(reqPartner.getOther())) {reqPartner.setOther(otherTP.getText()); bCD = true; }
		if(!specialNotesTP.getText().equals(reqPartner.getSpecialNotes())) { reqPartner.setSpecialNotes(specialNotesTP.getText()); bCD = true; }
		if(streetnumTF.getText().isEmpty())
		{
			reqPartner.setStreetnum(0);
			bCD = true;
		}
		else if((n=Integer.parseInt(streetnumTF.getText().trim())) != reqPartner.getStreetnum())
		{
			reqPartner.setStreetnum(n);
			bCD = true;
		}
		
		if(!streetnameTF.getText().equals(reqPartner.getStreetname()))
		{
			reqPartner.setStreetname(streetnameTF.getText());
			bCD = true;
		}
		if(!unitTF.getText().equals(reqPartner.getUnit())) { reqPartner.setUnit(unitTF.getText()); bCD = true; }
		if(!cityTF.getText().equals(reqPartner.getCity())) { reqPartner.setCity(cityTF.getText()); bCD = true; }
		if(!zipTF.getText().equals(reqPartner.getZipcode())) { reqPartner.setZipcode(zipTF.getText()); bCD = true; }
		if(!phoneTF.getText().equals(reqPartner.getPhone())) { reqPartner.setPhone(phoneTF.getText()); bCD = true; }
		if(!deliverToTP.getText().equals(reqPartner.getDeliverTo())) { reqPartner.setDeliverTo(deliverToTP.getText()); bCD = true; }
		if(!contact1TF.getText().equals(reqPartner.getContact())) { reqPartner.setContact(contact1TF.getText()); bCD = true; }
		if(!email1TF.getText().equals(reqPartner.getContact_email())) { reqPartner.setContact_email(email1TF.getText()); bCD = true; }
		if(!phone1TF.getText().equals(reqPartner.getContact_phone())) { reqPartner.setContact_phone(phone1TF.getText()); bCD = true; }
		if(!contact2TF.getText().equals(reqPartner.getContact2())) { reqPartner.setContact2(contact2TF.getText()); bCD = true; }
		if(!email2TF.getText().equals(reqPartner.getContact2_email())) { reqPartner.setContact2_email(email2TF.getText()); bCD = true; }
		if(!phone2TF.getText().equals(reqPartner.getContact2_phone())) { reqPartner.setContact2_phone(phone2TF.getText()); bCD = true; }
		
		if(bCD)	//If an update to partner data (not stop light data) was detected
		{
			reqPartner.setDateChanged(gvs.getTodaysDate());
			reqPartner.setChangedBy(userDB.getUserLNFI());
			
			String response = partnerDB.update(this, reqPartner);	//notify the database of the change
			
			if(response.startsWith("UPDATED_PARTNER"))
			{
				display(reqPartner);
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied Partner Update," +
						"try again later","Partner Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currPartner);
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
		statusCB.setEnabled(true);
		typeCB.setSelectedIndex(0);
		collectionCB.setSelectedIndex(0);
		collectionCB.setEnabled(true);
		cyReqTF.setText("");
		lblCYAssigned.setText("0");
		lblCYDel.setText("0");
		lblCYRecBefore.setText("0");
		lblCYRecAfter.setText("0");
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
		lblPYDel.setText("");
		lblPYRecBefore.setText("");
		lblPYRecAfter.setText("");
		nav.clearStoplight();
		
		bIgnoreEvents = false;
	}
	
	void setTextPaneFontSize(Integer fontSize)
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, fontSize);
        StyleConstants.setSpaceBelow(attribs, 3);
        
        otherTP.setParagraphAttributes(attribs, true);
        specialNotesTP.setParagraphAttributes(attribs, true);
        deliverToTP.setParagraphAttributes(attribs, true);
        
        if(currPartner != null)
        {
        	otherTP.setText(currPartner.getOther());
        	otherTP.setCaretPosition(0);
      
        	specialNotesTP.setText(currPartner.getSpecialNotes());
        	specialNotesTP.setCaretPosition(0);
		
        	deliverToTP.setText(currPartner.getDeliverTo());
        	deliverToTP.setCaretPosition(0);
        }
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
		ONCPartner delPartner = partnerDB.getObjectAtIndex(nav.getIndex());
		
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("Are you sure you want to delete %s from the data base?", 
											delPartner.getName());
	
		Object[] options= {"Cancel", "Delete"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Partner Database Deletion ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete"))
		{
			//send request to data base
			String response = partnerDB.delete(this, delPartner);
			
			if(response.startsWith("DELETED_PARTNER"))
			{
				processDeletedEntity(partnerDB);
			}
			else
			{
				String err_mssg = "ONC Server denied delete partner request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Delete Partner Request Failure",
												JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currPartner);
			}
		}
	}
	
	void onSaveNew()
	{
		//construct a new partner from user input	
		ONCPartner newPartner = new ONCPartner(-1, new Date(), userDB.getUserLNFI(),
				3, "Partner Created", userDB.getUserLNFI(),
				statusCB.getSelectedIndex(), typeCB.getSelectedIndex(),
				(GiftCollection) collectionCB.getSelectedItem(), nameTF.getText(), 
				streetnumTF.getText().isEmpty() ? 0 : Integer.parseInt(streetnumTF.getText()),
				streetnameTF.getText(), unitTF.getText(), cityTF.getText(), zipTF.getText(), 
				phoneTF.getText(), cyReqTF.getText().isEmpty() ? 0 : Integer.parseInt(cyReqTF.getText()),
				otherTP.getText(), deliverToTP.getText(), specialNotesTP.getText(), 
				contact1TF.getText(), email1TF.getText(), phone1TF.getText(),
				contact2TF.getText(), email2TF.getText(), phone2TF.getText());
		
		//send request to add new partner to the local data base
		String response = partnerDB.add(this, newPartner);
		
		if(response.startsWith("ADDED_PARTNER"))
		{
			//update the ui with new partner id assigned by the server 
			Gson gson = new Gson();
			ONCPartner addedOrg = gson.fromJson(response.substring(13), ONCPartner.class);
			
			//set the display index, on, to the new partner added and display organization
			nav.setIndex(partnerDB.getListIndexByID(partnerDB.getList(), addedOrg.getID()));
			display(addedOrg);
		}
		else
		{
			String err_mssg = "ONC Server denied add partner request, try again later";
			JOptionPane.showMessageDialog(this, err_mssg, "Add Partner Request Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(currPartner);
		}
		
		//reset to review mode and display the proper partner
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
		display(currPartner);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}
	 
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
//		System.out.println(dbe.getType());
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_PARTNER"))
		{
			ONCPartner updatedPartner = (ONCPartner) dbe.getObject1();
			
			//If current partner is being displayed has changed, reshow it
			if(isNumeric(lblOrgID.getText()) && Integer.parseInt(lblOrgID.getText()) == updatedPartner.getID() && !bAddingNewEntity)
				display(updatedPartner); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_PARTNER"))
		{
			//if the deleted partner was the only partner in data base, clear the display
			//otherwise, if the deleted partner is currently being displayed, change the on
			//to the next prior partner and display.
			if(partnerDB.size() == 0)
			{
				nav.setIndex(0);
				clear();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCPartner deletedPartner = (ONCPartner) dbe.getObject1();
				if(Integer.parseInt(lblOrgID.getText()) == deletedPartner.getID())
				{
					if(nav.getIndex() == 0)
						nav.setIndex(partnerDB.size() - 1);
					else
						nav.setIndex(nav.getIndex() - 1);
					
					display(partnerDB.getObjectAtIndex(nav.getIndex()));
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_PARTNERS"))
		{
			updatePartnerPerformanceBorders();
		}
		else if(!bAddingNewEntity && currPartner != null && dbe.getSource() != this &&
				(dbe.getType().equals("PARTNER_WISH_RECEIVED") || dbe.getType().equals("PARTNER_WISH_RECEIVE_UNDONE")))
		{
			ONCPartner wishPartner = (ONCPartner) dbe.getObject1();
			
			//if current partner being displayed has had their gifts received count changed, redisplay
			if(wishPartner != null && currPartner.getID() == wishPartner.getID())
				display(wishPartner);
		}
		else if(!bAddingNewEntity && currPartner != null && dbe.getSource() != this &&
				dbe.getType().equals("PARTNER_WISH_ASSIGNEE_CHANGED"))
		{
			ONCPartner oldWishPartner = (ONCPartner) dbe.getObject1();
			ONCPartner newWishPartner = (ONCPartner) dbe.getObject2();

			//if current partner being displayed has had their gifts assigned count changed, update
			//the current year assignee label by redisplaying the partner
			if(oldWishPartner != null && currPartner.getID() == oldWishPartner.getID())
				display(oldWishPartner);
			else if(newWishPartner != null && currPartner.getID() == newWishPartner.getID())
				display(newWishPartner);	
		}
		else if(dbe.getSource() != this && dbe.getType().equals("PARTNER_ORNAMENT_DELIVERED"))
		{
			ONCPartner updatedPartner = (ONCPartner) dbe.getObject1();
			
			//if current partner being displayed, refresh the displayed data
			if(!bAddingNewEntity && currPartner != null && (currPartner.getID() == updatedPartner.getID()))
				display(currPartner); 
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			if(this.isVisible())
			{
				//Assume that partner displayed gift count assigned has changed
				display(currPartner);
			}
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") ||
				dbe.getType().equals("CHANGED_USER")))
		{
			//determine if it's the currently logged in user
			ONCUser updatedUser = (ONCUser)dbe.getObject1();
			if(userDB.getLoggedInUser().getID() == updatedUser.getID())
				setTextPaneFontSize(updatedUser.getPreferences().getFontSize());
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
				ONCPartner partner = (ONCPartner) tse.getObject1();
				update();
				nav.setIndex(partnerDB.getListIndexByID(partnerDB.getList(), partner.getID()));
				display(partner);
			}
			else if(tse.getSource() == nav && tse.getType() == EntityType.PARTNER)
			{
				update();
				display(partnerDB.getObjectAtIndex(nav.getIndex()));
			}
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.PARTNER);
	}
}
