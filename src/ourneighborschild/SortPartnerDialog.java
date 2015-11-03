package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.mail.internet.MimeBodyPart;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import au.com.bytecode.opencsv.CSVWriter;

public class SortPartnerDialog extends ChangeDialog implements ActionListener, ListSelectionListener, 
															PropertyChangeListener, DatabaseListener															
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_ROWS_TO_DISPLAY = 15;
	private static final String DEFAULT_NO_CHANGE_LIST_ITEM = "No Change";
	private static final int MIN_PHONE_NUMBER_LENGTH = 10;
	private static final int MIN_NAME_LENGTH = 2;
	private static final int MIN_EMAIL_ADDRESS_LENGTH = 2;
	private static final String GIFT_PARTNER_EMAIL_SENDER_ADDRESS = "partnercontact@ourneighborschild.org";
	private static final String CLOTHING_PARTNER_EMAIL_SENDER_ADDRESS = "volunteer@ourneighborschild.org";
	
	private ONCRegions regions;
	private ONCOrgs orgs;
	private ChildDB childDB;
	
	private JComboBox regionCB, statusCB, typeCB;
	private JComboBox changedByCB, stoplightCB;
	private ComboItem[] changePartItem;
	private JComboBox changePStatusCB, changeOrnReqCB;
	private DefaultComboBoxModel regionCBM;
	private DefaultComboBoxModel changedByCBM;
	private JButton btnExport;
	private JComboBox printCB, emailCB;
	private JLabel lblOrnReq;
	private ArrayList<Organization> stAL;
//	private ArrayList<Organization> tableRowSelectedObjectList;

	private int sortStatus = 0, sortType = 0, sortRegion = 0, sortChangedBy = 0, sortStoplight = 0;
	
	private String[] status = {"Any","No Action Yet", "1st Email Sent", "Responded", "2nd Email Sent", "Called, Left Mssg",
							   "Confirmed", "Not Participating"};
	
	private String[] types = {"Any","Business","Church","School", "Clothing", "Coat", "ONC Shopper"};
	
	private String[] columns;

	private JProgressBar progressBar;
	private ONCEmailer oncEmailer;
	
	SortPartnerDialog(JFrame pf, String[] columnToolTips, String[] cols, int[] colWidths, int[] center_cols)
	{
		super(pf, columnToolTips, cols, colWidths, center_cols);
		this.columns = cols;
		this.setTitle("Our Neighbor's Child - Gift Partner Management");
		
		regions = ONCRegions.getInstance();
		orgs = ONCOrgs.getInstance();
		childDB = ChildDB.getInstance();
		
		//Get reference for data base listeners
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
				
		if(orgs != null)
			orgs.addDatabaseListener(this);
		
		if(regions != null)
			regions.addDatabaseListener(this);
		
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		ChildWishDB childwishDB = ChildWishDB.getInstance();
		if(childwishDB != null)
			childwishDB.addDatabaseListener(this);	//listen for partner gift assignment changes
		
		//Set up the array lists
		stAL = new ArrayList<Organization>();
//		tableRowSelectedObjectList = new ArrayList<Organization>();
				
		//Set up the search criteria panel      
		statusCB = new JComboBox(status);
		statusCB.setBorder(BorderFactory.createTitledBorder("Partner Status"));
		statusCB.addActionListener(this);
				
		typeCB = new JComboBox(types);
		typeCB.setBorder(BorderFactory.createTitledBorder("Partner Type"));
		typeCB.addActionListener(this);
				
		regionCBM = new DefaultComboBoxModel();
    	regionCBM.addElement("Any");
		regionCB = new JComboBox();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.addActionListener(this);
				
		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
		changedByCBM.addElement("Anyone");
		changedByCB.setModel(changedByCBM);
		changedByCB.setPreferredSize(new Dimension(144, 56));
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.addActionListener(this);
				
//		stoplightCB = new JComboBox(stoplt);
		stoplightCB = new JComboBox(GlobalVariables.getLights());
		stoplightCB.setPreferredSize(new Dimension(80, 56));
		stoplightCB.setBorder(BorderFactory.createTitledBorder("Stoplight"));
		stoplightCB.addActionListener(this);
				
		//Add all sort criteria components to dialog pane
		sortCriteriaPanelTop.add(statusCB);
		sortCriteriaPanelTop.add(typeCB);
		sortCriteriaPanelTop.add(changedByCB);
		sortCriteriaPanelTop.add(regionCB);
		sortCriteriaPanelTop.add(stoplightCB);
		        
		//Set up the change panel holding count panel, orn assigned panel and change panel
		itemCountPanel.setBorder(BorderFactory.createTitledBorder("Partners Meeting Criteria"));

		JPanel ornReqPanel = new JPanel();       
        lblOrnReq = new JLabel("0");
        ornReqPanel.setBorder(BorderFactory.createTitledBorder("# Orn Requested"));
        ornReqPanel.setPreferredSize(new Dimension(125, 80));
        ornReqPanel.add(lblOrnReq);
        
        gbc.gridx = 1;
        gbc.ipadx = 0;
        gbc.weightx = 0.8;
        changePanel.add(ornReqPanel, gbc);

		changePartItem = new ComboItem[8];	//Delivery status combo box list objects can be enabled/disabled
		changePartItem[0] = new ComboItem(DEFAULT_NO_CHANGE_LIST_ITEM);
		changePartItem[1] = new ComboItem("No Action Yet");
		changePartItem[2] = new ComboItem("1st Email Sent");
		changePartItem[3] = new ComboItem("Responded");  
		changePartItem[4] = new ComboItem("2nd Email Sent");
		changePartItem[5] = new ComboItem("Called, Left Mssg");  
		changePartItem[6] = new ComboItem("Confirmed");
		changePartItem[7] = new ComboItem("Not Participating");   
				
		changePStatusCB = new JComboBox(changePartItem);
		changePStatusCB.setRenderer(new ComboRenderer());
		changePStatusCB.setPreferredSize(new Dimension(172, 56));
		changePStatusCB.setBorder(BorderFactory.createTitledBorder("Change Partner Status"));
		changePStatusCB.addActionListener(new ComboListener(changePStatusCB));	//Prevents selection of disabled combo box items
		changePStatusCB.addActionListener(this);	//Used to check for enabling the Apply Changes button
				
		String[] choices = {DEFAULT_NO_CHANGE_LIST_ITEM, "25", "50", "75", "100",
									"125", "150", "175", "200", "250", "300", "400", "500"};
		changeOrnReqCB = new JComboBox(choices);
		changeOrnReqCB.setEditable(true);
		changeOrnReqCB.setPreferredSize(new Dimension(192, 56));
		changeOrnReqCB.setBorder(BorderFactory.createTitledBorder("Change # Ornaments Req"));
		changeOrnReqCB.addActionListener(this);

		//Add the components to the change data panel			
		changeDataPanel.add(changePStatusCB);
		changeDataPanel.add(changeOrnReqCB);
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Select Partner Data"));
		
		gbc.gridx = 2;
        gbc.ipadx = 0;
        gbc.weightx = 1.0;
        changePanel.add(changeDataPanel, gbc);
        
		//Set up the control panel
		btnExport = new JButton("Export Data");
	    btnExport.setEnabled(false);
	    btnExport.addActionListener(this);
		
		//Set up the email progress bar
		progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
				
		String[] emailChoices = {"Email", "New 2015 Season Email",
//								 "2014 Gift Drop-Off Reminder",
//								 "Ornament Drop-Off Email","2014 Clothing Donor Email",
//								 "2014 Clothing Donor Reminder Email",
//								 "2014 Giving Tree Warehouse Email",
//								 "Clothing Donor Not Too Late Email",
//								 "GIft Drop Off Reminder Email",
//								 "Clothing Donor Drop Off Reminder Email"
								 };
		emailCB = new JComboBox(emailChoices);
		emailCB.setPreferredSize(new Dimension(136, 28));
		emailCB.setEnabled(false);
		emailCB.addActionListener(this);
			    
		String[] printChoices = {"Print", "Print Listing", "Print Partner Info"};
		printCB = new JComboBox(printChoices);
		printCB.setPreferredSize(new Dimension(136, 28));
		printCB.setEnabled(false);
		printCB.addActionListener(this);
  
		//Add the components to the control panel
		cntlPanel.add(progressBar);
		cntlPanel.add(btnExport);
		cntlPanel.add(emailCB);
		cntlPanel.add(printCB);

		//Add the change and bottom panels to the dialog pane
		 this.add(changePanel);
	     this.add(bottomPanel);
	        
		pack();
	}
/*	
	void displaySortTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		while (sortTableModel.getRowCount() > 0)	//Clear the current table
			sortTableModel.removeRow(0);
		
		//Sort table empty, disable print
		printCB.setEnabled(false);
		emailCB.setEnabled(false);
		btnExport.setEnabled(false);
		
		for(Organization o:stAL)	//Build the new table
			sortTableModel.addRow(getTableRow(o));
		
		//If the sort table has data, enable printing listing info
		if(!stAL.isEmpty())
			printCB.setEnabled(true);
				
		bChangingTable = false;	
	}
*/	
	@Override
	public Object[] getTableRow(ONCObject obj)
	{
		Organization o = (Organization) obj;
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		Object[] sorttablerow = {o.getName(), status[o.getStatus()+1], types[o.getType()],
								 Integer.toString(o.getNumberOfOrnamentsRequested()),
								 Integer.toString(o.getNumberOfOrnamentsAssigned()),
								 o.getSpecialNotes(),
								 sdf.format(o.getDateChanged().getTime()),
								 o.getChangedBy(),
								 regions.getRegionID(o.getRegion()),
//								 stoplt[o.getStoplightPos()+1].substring(0,1)};
		 						 gvs.getImageIcon(23 + o.getStoplightPos())};
		return sorttablerow;
	}
	
	public void buildTableList(boolean bPreserveSelections)
	{
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(stAL);
		else
			tableSortCol = -1;
		
		stAL.clear();	//Clear the prior table data array list
		int totalornreq = 0;	//total number of orn requested in table
		
		for(Organization o : orgs.getList())
		{
			if(doesStatusMatch(o.getStatus()) &&
				doesTypeMatch(o.getType()) &&
				       doesRegionMatch(o.getRegion()) &&
				        doesChangedByMatch(o.getStoplightChangedBy()) &&
				         doesStoplightMatch(o.getStoplightPos()))	//Search criteria pass
				{
					stAL.add(o);
					totalornreq += o.getNumberOfOrnamentsRequested();
				}
		}
		
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));
		lblOrnReq.setText(Integer.toString(totalornreq));
		displaySortTable(stAL, true, tableRowSelectedObjectList);		//Display the table after table array list is built						
	}
/*	
	void archiveTableSelections(ArrayList<? extends ONCObject> stAL)
	{
		tableRowSelectedObjectList.clear();
		
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<row_sel.length; i++)
		{
			Organization o = (Organization) stAL.get(row_sel[i]);
			tableRowSelectedObjectList.add(o);
		}
	}
*/	
	@Override
	int sortTableList(int col)
	{
		archiveTableSelections(stAL);
		
		if(orgs.sortDB(stAL, columns[col]))
		{
			displaySortTable(stAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;
	}
	
	@Override
	void setEnabledControls(boolean tf)
	{
		if(tf && sortTable.getSelectedRowCount() > 0)
		{
			printCB.setEnabled(true);
			if(GlobalVariables.isUserAdmin())	//Only admins or higher can send email
				emailCB.setEnabled(true);
			btnExport.setEnabled(true);
		}
		else
		{
			printCB.setEnabled(false);
			emailCB.setEnabled(false);
			btnExport.setEnabled(false);
		}
	}
	
	//Returns a boolean that a change to organization data occurred
	boolean onApplyChanges()
	{
		int[] row_sel = sortTable.getSelectedRows();
		boolean bOrgDataChangeDetected = false;		//Flag set if partner change detected
		boolean bOrgChanged = false;	//flag set if partner change requested
	
		for(int i=0; i<row_sel.length; i++)
		{
			//Find index for organization selected
//			Organization o = stAL.get(row_sel[i]);
			Organization updatedOrg = new Organization(stAL.get(row_sel[i]));	//make a copy for update request
			
			//If status changed, process it
			int oldstatus = updatedOrg.getStatus();
			int newstatus = orgs.requestOrgStatusChange(updatedOrg, changePStatusCB.getSelectedIndex()-1);
			
			if(oldstatus != newstatus)
				bOrgChanged = true;	
			
			
			//If ornament requested change, process it
			String ornCBtext = changeOrnReqCB.getSelectedItem().toString().trim();
			
			//Determine if the user is requesting a valid change. It must be a number
			if(!ornCBtext.equals(DEFAULT_NO_CHANGE_LIST_ITEM)  && isNumeric(ornCBtext))
			{
				int ornreq = Integer.parseInt(ornCBtext);
				
				//Determine if the number of ornaments requested by the user is in fact
				//a change in quantity. If it is, store the new request and note the user
				//has changed the data
				if(updatedOrg.getNumberOfOrnamentsRequested() != ornreq)
				{
					updatedOrg.setNumberOfOrnamentsRequested(ornreq);
					bOrgChanged = true;
				}
			}
			
			//if status or # of ornaments requested changed, need to send an update request to the server
			if(bOrgChanged)
			{
				updatedOrg.setDateChanged(gvs.getTodaysDate());
				updatedOrg.setStoplightChangedBy(GlobalVariables.getUserLNFI());
				
				String response = orgs.update(this, updatedOrg);	//notify the database of the change
				
				if(response.startsWith("UPDATED_PARTNER"))
					bOrgDataChangeDetected = true;
				else
				{
					//display an error message that update request failed
					JOptionPane.showMessageDialog(this, "ONC Server denied Partner Update," +
							"try again later","Partner Update Failed",  
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}
				
				bOrgChanged = false;
			}
		}
		
		sortTable.clearSelection();
		if(bOrgDataChangeDetected)
			buildTableList(false);
		
		//Reset the change combo boxes to DEFAULT_NO_CHANGE_LIST_ITEM
		changePStatusCB.setSelectedIndex(0);
		changeOrnReqCB.setSelectedItem(DEFAULT_NO_CHANGE_LIST_ITEM);
		
		//Changes were applied, disable until user selects new table row(s) and values
		btnApplyChanges.setEnabled(false);
		
		return bOrgDataChangeDetected;
	}
	
	void createAndSendPartnerEmail(int emailType)
	{
		//build the email
		ArrayList<ONCEmail> emailAL = new ArrayList<ONCEmail>();
		ArrayList<ONCEmailAttachment> attachmentAL = new ArrayList<ONCEmailAttachment>();
		String cid0 = null, cid1 = null;
		String emailBody = null, subject = null;
		
		//Create the subject and attachment array list
		if(emailType == 1)
		{
			subject = "Greetings From Our Neighbor's Child";
			cid0 = ContentIDGenerator.getContentId();
			cid1 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("DSC_0704.JPG", cid0, MimeBodyPart.INLINE));
			attachmentAL.add(new ONCEmailAttachment("DSC_0764.JPG", cid1, MimeBodyPart.INLINE));
		}
		else if(emailType == 2)
		{
			subject = "ONC Gift Drop Off Reminder";
//			cid0 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 3)
		{
			subject = "ONC Ornament Drop Off Date: 11/20";
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 4)	//2014 Clothing Donor Email
		{
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 5)	//Clothing Donor Reminder Email
		{
			subject = "A Friendly ONC Reminder!";
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 6)	//Giving Tree Warehouse Email
		{
			subject = "Ornament Delivery & Gift Drop-Off Dates/Location";
//			cid0 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 7)	//Clothing Donor Not Too Late Email
		{
			subject = "ONC - children's wishes still available!";
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 8)	//Drop Off Reminder Email
		{
			subject = "ONC Gift Drop-Off Dates and Warehouse Directions";
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		else if(emailType == 9)	//Clothing Drop Off Reminder Email
		{
			subject = "Final Reminder: Our Neighbor's Child Clothing 2013";
			cid0 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("onclogosmall.jpg", cid0, MimeBodyPart.INLINE));
		}
		
		//For each organization selected, create the email body and recipient information in an
		//ONCEMail object and add it to the emailAL
		int[] row_sel = sortTable.getSelectedRows();
		for(int row=0; row< sortTable.getSelectedRowCount(); row++)
		{
			//Get organization object
			Organization o = stAL.get(row_sel[row]);
			
			//Create the email body and potentially subject
	        if(emailType == 1)
	        	emailBody = create2015SeasonOrganizationEmailBody(o, cid0, cid1);
	        
/*	        else if(emailType == 2)
	        	emailBody = create2014DonorReminderEmailBody();
//	        	emailBody = createDropOffOrganizationEmailBody(cid0);
	        else if(emailType == 3)
	        	emailBody = createOrnamentDropOffEmailBody(cid0);
	        else if(emailType == 4)
	        {	
	        	//get the clothing donor's first name
	        	String[] names = o.getName().split(",");
	        	if(names.length == 2)
	        		subject = String.format("Hi %s from Our Neighbor's Child", names[1].trim());
	        	else
	        		subject = "Hi from Our Neighbor's Child";
	        	
	        	emailBody = create2014ClothingDonorEmailBody(cid0);
	        }
	        else if(emailType == 5)
	        	emailBody = create2014ClothingDonorReminderEmailBody(cid0);
	        else if(emailType == 6)
	        	emailBody = create2014GivingTreeEmailBody();
	        else if(emailType == 7)
	        	emailBody = createClothingDonorNotTooLateEmailBody(cid0);
	        else if(emailType == 8)
	        	emailBody = createDropOffReminderEmailBody(cid0);
	        else if(emailType == 9)
	        	emailBody = createClothingDropOffReminderEmailBody(cid0);
*/	        
	        //Create To: recipients. If the partner has a second contact with a valid email address, the
	        //To: field will contain both email contacts. If there isn't a valid second contact email
	        //then only the first email address will be used. If the first email address isn't valid, 
	        //the message will not be sent.
	        ArrayList<EmailAddress> toAddressList = new ArrayList<EmailAddress>();
      
	        //*********** THIS BLOCK OF CODE IS FOR REAL EMAILS *************************	         
	        if(o != null && o.getContact_email()!= null && o.getContact_email().length() > MIN_EMAIL_ADDRESS_LENGTH)
	        	toAddressList.add(new EmailAddress(o.getContact_email(), o.getContact()));
	        
	        if(o != null && o.getContact2_email()!= null && o.getContact2_email().length() > MIN_EMAIL_ADDRESS_LENGTH)
	        	toAddressList.add(new EmailAddress(o.getContact2_email(), o.getContact2()));	  	        	
/*	        
			//*********** THIS BLOCK OF CODE IS FOR TEST EMAILS *************************
			//Create test email addresses
//	        EmailAddress toAddressTestJWO = new EmailAddress("johnwoneill1@gmail.com", "John O'Neill");
//	        EmailAddress toAddressTestJWO = new EmailAddress("johnwoneill@cox.net", "John O'Neill");
//	        EmailAddress toAddressTestCH = new EmailAddress("hobbsfamily@cox.net", "Chris Hobbs");
	        EmailAddress toAddressTestKL = new EmailAddress("kellylavin1@gmail.com", "Kelly Lavin");
//	        EmailAddress toAddressTestKLAOL = new EmailAddress("kmlavin@aol.com", "Kelly Lavin");
//	        EmailAddress toAddressTestSS = new EmailAddress("somerss@cox.net", "Stephanie Somers");
	        	        
	        //Add test email addresses
//	        toAddressList.add(toAddressTestKL);
//	        toAddressList.add(toAddressTestJWO);
//	        toAddressList.add(toAddressTestKLAOL);
	        toAddressList.add(toAddressTestKL);
//	        toAddressList.add(toAddressTestCH);
//	        toAddressList.add(toAddressTestSS);
*/	        
	        emailAL.add(new ONCEmail(subject, emailBody, toAddressList));     
		}
		
		//Create the from address string array
		EmailAddress fromAddress;
		if(emailType == 4 || emailType == 5 || emailType == 7 || emailType == 9)
			fromAddress = new EmailAddress(CLOTHING_PARTNER_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child - Stephanie Somers");
		else
			fromAddress = new EmailAddress(GIFT_PARTNER_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child");
		
		//Create the blind carbon copy list of EmailAddress objects
		ArrayList<EmailAddress> bccList = new ArrayList<EmailAddress>();
		
		if(emailType == 4 || emailType == 5 || emailType == 7 || emailType == 9)
		{
			bccList.add(new EmailAddress(CLOTHING_PARTNER_EMAIL_SENDER_ADDRESS, "Stephanie Somers"));
			bccList.add(new EmailAddress("volunteer@ourneighborschild.org", "ONC Volunteer"));
		}
		else
			bccList.add(new EmailAddress(GIFT_PARTNER_EMAIL_SENDER_ADDRESS, "Partner Contact"));

		
//		bccList.add(new EmailAddress("kellylavin1@gmail.com", "Kelly Lavin"));
//		bccList.add(new EmailAddress("jwoneill1@aol.com", "John O'Neill"));
//		bccList.add(new EmailAddress("johnwoneill@cox.net", "John O'Neill"));
		
		//Create mail server credentials, then the mailer background task and execute it 
//		ServerCredentials creds = new ServerCredentials("smtpout.secureserver.net", "director@act4others.org", "crazyelf1");
//		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", "SchoolContact@ourneighborschild.org", "crazyelf");
		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", "partnercontact@ourneighborschild.org", "crazyelf");
		
	    oncEmailer = new ONCEmailer(this, progressBar, fromAddress, bccList, emailAL, attachmentAL, creds);
	    oncEmailer.addPropertyChangeListener(this);
	    oncEmailer.execute();
	    emailCB.setEnabled(false);		
	}
	
	String create2014ClothingDonorEmailBody(String cid0)
	{	
		String msg = String.format("<html><body>" +
				"<div><p>Ho! Ho! Ho!  It's that time of year again, and all of us at Our Neighbor's Child are " +
				"gearing up for another wonderful holiday season!</p>" +
				"<p>This is our 23rd year of assisting our area's needy children, and I'm contacting you because " +
				"you have generously helped us in the past.  I'm really hoping you can do it again this year!  ONC is " +
				"so thankful for any help you can give us, whether it's donating a coat for one child or \"adopting\" the " +
				"clothing needs of an entire family. We expect to have all of our \"wish lists\" from the families we " +
				"serve by " +
				"<font color=\"red\">Wednesday, November 19th</font> " +
				"(in time for Black Friday shopping!!)</p>" +
				"<p>If you would like to help again this year, please contact me at " +
				"<a href=\"mailto:somerss@cox.net\">Somerss@cox.net</a> "+
				"- or just reply to this e-mail - and let me know how many children you " +
				"would like to provide clothing for and if you have a preference for a certain age or " +
				"gender.  I will do my best to match you up with your request and will get back to you " +
				"as soon as the \"wish list\" is available.  " +
				"<b>AND!!</b> If you have friends or family, or bunko groups, bible study groups, boy scouts, girl scouts, " +
				"sports teams, etc. who you think would also be interested in participating, please forward " +
				"this e-mail!!!  I've heard back from a number of different clubs that really enjoy going out " +
				"shopping together and/or pooling their time and money into providing for lots of families!! " +
				"We'd love your help in getting the word out about Our Neighbor's Child!! For more information " +
				"about ONC and other volunteer opportunities, please visit our website: " +
				"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p>" +
				"<p>We can't thank you enough for all you do!</p>" +
				"<p>Warmest Regards,</p>" +
				"<p>Stephanie Somers<br>Clothing Coordinator<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String create2014ClothingDonorReminderEmailBody(String cid0)
	{	
		String msg = String.format("<html><body>" +
				"<div><p>Hi again!</p>" +
				"<p>Just in case you missed the first email (hopefully it's not in SPAM or JUNK!),  we wanted you "
				+ "to know that we still have nearly 200 children's clothing wishes available for \"adoption\".  " +
				"If you're able to help - just reply to this email. </p>" +
				"<p>All gifts must be dropped off at our ONC warehouse. We have a <b>NEW LOCATION:</b> " +
				"<a href=\"https://goo.gl/maps/PiRro\">3863 Centerview Drive in Chantilly</a></b>.</p>" +
				"<p>As a reminder, these are the dates for this season's Gift Drop-Off:</p>" +
				"<p><b>Sunday, December 7th: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 8th: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 9th: 3PM - 6PM</b></p>" +
				"<p>It is critically important for us to receive all gifts by Tuesday the 9th at 6PM. " +
				"We inventory all gifts on Wednesday and must send volunteers out to purchase " +
				"any missing gifts that day.</p>" +
				"<p><b>In case of a major snow event, please check our website for updates " +
				"and drop off your gifts as soon as safely possible!</b></p>" +
				"<p><b>AND Remember!!</b> If you have friends or family, bunko groups, bible study groups, "
				+ "boy scouts, girl scouts, sports teams, etc. who you think would also be interested in "
				+ "participating, please forward this e-mail!!!  We'd love your help in getting the word out "
				+ "about Our Neighbor's Child!! For more information about ONC and other volunteer opportunities, "
				+ "please visit our website: "
				+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>.</p></div></p>" +
				"<p>We can't thank you enough for all you do!</p>" +
//				"<p>If you have any questions, please don't hesitate to contact me at" +
//				" <a href=\"mailto:somerss@cox.net\">Somerss@cox.net</a>.</p>" +
				"<p>Warmest Regards,</p>" +
				"<p>Stephanie Somers<br>Clothing Coordinator<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String create2014DonorReminderEmailBody()
	{	
		String msg = String.format("<html><body><div>" +
				"<p>Just a reminder of the final three ONC Gift Drop dates ahead. Please remember our " +
				"<b>NEW LOCATION:</b> is: " +
				"<a href=\"https://goo.gl/maps/PiRro\">3863 Centerview Drive in Chantilly</a></b>. " +
				"It's just off Route 50, across the highway from Sullyfield Circle near Bob Evans.</p>  " +
				"<p>Gift drop off dates/times are: </p>" +
				"<p><b>Sunday, December 7th: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 8th: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 9th: 3PM - 6PM</b></p>" +
				"<p>Please provide the name of your organization at the Gift Drop Off desk and student " +
				"volunteers will assist in unloading the unwrapped gifts from your vehicle. It is critically " +
				"important for us to receive all gifts by Tuesday evening. " +
				"We inventory all gifts on Wednesday and must send volunteers out to purchase " +
				"any missing gifts that day.</p>" +
				"<p><b>In case of a major snow event, please check our website for updates " +
				"and drop off your gifts as soon as safely possible!</b></p>" +
				"<p>Thank you for your willingness to support the children of our community in this way.</p>" +
				"<p>If you have any questions or concerns, please contact me at " +
				"<a href=\"mailto:oncdenise@aol.com\">oncdenise@aol.com</a>.</p>"+
				"<p>Best wishes for your own happy holidays!</p>" +
				"<p>Sincerely,</p>" +
				"<p>Denise McInerney<br>Gift Partner Coordinator, Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"</div></body></html>");
		
		return msg;
	}
	String createDropOffOrganizationEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good afternoon!</p>" + 
				"<p>Here are the dates for this season's Gift Drop-Off:</p>" + 
				"<p><b>Sunday, December 1st - 12PM - 2PM</b><br>" + 
				"(This is an \"undecorated\" drop-off, primarily for churches with " +
				"limited space to store gifts).</p>" +
				"<p><b>Sunday, December 8, 2013: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 9, 2013: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 10, 2013: 3PM - 6PM</b></p>" + 
				"<p>I'll send an e-mail with this year's warehouse location and directions " +
				"as soon as that information is confirmed.</p>" +
				"<p>I hope you're having a great weekend!</p>" + 
				"<p>Sincerely,</p>" +
				"<p>Chris Hobbs<br><br>Gift Partner Coordinator, " +
				"Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	String createDropOffReminderEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good afternoon!</p>" +
				"<p>This is a reminder that the LAST DAY for ONC Gift Drop Off is fast approaching!  " +
				"We greatly appreciate your efforts to help us serve these children!</p>" +
				"<p>This year's donated warehouse space is in a <b>NEW LOCATION:  " +
				"<a href=\"http://goo.gl/maps/eWCEr\">4311 Walney Road in Chantilly</a></b>.</p>" +
				"<p>Please return the unwrapped gifts with the tag securely attached on any of the " +
				"following dates:  </p>" + 
				"<p><b>Sunday, December 8, 2013: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 9, 2013: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 10, 2013: 3PM - 6PM</b></p>" +
				"<p>Please provide the name of your organization at the Gift Drop Off desk and student " +
				"volunteers will assist in unloading the gifts from your vehicle.</p>" +
				"<p>We are deeply grateful for your support of Our Neighbor's Child. Many hundreds of " +
				"children in need will have a brighter holiday because we all joined together in this " +
				"meaningful community effort.</p>" +
				"<p>Best wishes for your own happy holidays!</p>" + 
				"<p>Sincerely,</p>" +
				"<p>Chris Hobbs<br><br>Gift Partner Coordinator, " +
				"Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String createClothingDropOffReminderEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good afternoon!</p>" +
				"<p>It's almost time!!!  Just wanted to send you a quick reminder about the clothing " +
				"wishes you have so generously shopped for ONC this year.  </p>" +
				"<p>Before you bring your gifts to the warehouse, please make sure to " +
				"<font color=\"red\"><b><u>write the Family # IN BIG NUMBERS </u></b></font>" +
				"on the price tags or attached to each item somehow.  You might want to " +
				"print this note and staple or tape it to the tags or bags as well.  Without the " +
				"family number, it's really hard for us to match the items to the correct family.  " +
				"And remember, it's not necessary to wrap the gifts!!  We like to let the parents " +
				"see everything, and we also provide each family with wrapping paper. </p>" +
				"<p>Please note: <b>WE HAVE A NEW LOCATION THIS YEAR!!!!</b>  " +
				"This year's donated ONC warehouse address is " +
				"<a href=\"http://goo.gl/maps/eWCEr\">4311 Walney Road in Chantilly, VA 20151</a></b>.  " +
				"Please mapquest or google map if you need detailed directions.</p>" +
				"<p>Drop off dates are as follows:</p>" + 
				"<p><font color=\"green\"><b>**Sunday, December 8 from 12 noon until 2:00 p.m.</b></font></p>" +
				"<p><font color=\"green\"><b>**Monday, December 9 from 3:00-6:00 p.m.</b></font></p>" +
				"<p><font color=\"green\"><b>**Tuesday, December 10 from 3:00-6:00 p.m.</b></font></p>" +
				"<p>We are deeply grateful for your support of Our Neighbor's Child. Many hundreds of " +
				"children in need will have a brighter holiday because we all joined together in this " +
				"meaningful community effort.</p>" +
				"<p>I can't wait to see you at the warehouse!!!  Thanks again for all you do!!!</p>" + 
				"<p>Best Regards,</p>" +
				"<p>Stephanie Somers<br>ONC Clothing Coordinator<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String createClothingDonorNotTooLateEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
			"<div><p>Hello Again!</p>" + 
			"<p>Just wanted to send you a quick e-mail and let you know that it's NOT TOO LATE!!  " +
			"I'm still busy assigning clothing wishes for the children, and wanted to make sure " +
			"that I didn't miss anyone who wanted to adopt a child or two or even an entire family.</p>" + 
			"<p>Please know that we have sincerely appreciated your generous past support.  If you are " +
			"unable to participate in clothing \"adoptions\" this season, we hope you'll consider joining " +
			"us in other ways or at a future time.  I only send a follow-up e-mail to be sure that none " +
			"were missed or sent to SPAM!</p>" +
			"<p>Please let me know if you'd like to help and I will assign you your kids right away.  " +
			"Drop off days are <b>December 8 (noon-2:00 p.m.)</b>, and <b>December 9-10 (3:00-6:00 p.m.)</b>.</p>" +
			"<p>We have a new donated warehouse space this year: " +
			"<a href=\"http://goo.gl/maps/eWCEr\">4311 Walney Road, Chantilly</a></b>.</p>" +
			"<p>Thanks so much,</p>" +
			"<p>Stephanie Somers<br>Clothing Coordinator<br>" +
			"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
			"<img src=\"cid:" + cid0 + "\" /></p>" +
			"</div></body></html>");
		
		return msg;
	}
	
	String create2014GivingTreeEmailBody()
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good evening!</p>" +
				"<p>We're happy to report that our ONC volunteers will have all " +
				"ornaments delivered to our Giving Tree locations by <b>this Wednesday, November 19th.</b></p>" +
				"<p>If your organization hosts a generic Gift Collection, there will be no ornaments, " +
				"but the information below is for you as well:</p>" +
				"<p>As in year's past we are fortunate to have a new donated warehouse space. The <b>NEW LOCATION is: " +
				"<a href=\"https://goo.gl/maps/PiRro\">3863 Centerview Drive in Chantilly</a></b>.</p>" +
				"</p>It's just off Route 50, across the highway from Sullyfield Circle near Bob Evans.</p>" +
				"<p>As a reminder, these are are the dates for this season's Gift Drop-Off:</p>" + 
				"<p><b>Sunday, November 30th: 12PM - 2PM</b><br>" + 
				"(This is an \"undecorated\" drop-off, primarily for churches with " +
				"limited space to store gifts).</p>" +
				"<p><b>Sunday, December 7th: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 8th: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 9th: 3PM - 6PM</b></p>" + 
				"<p>Please provide the name of your organization at the Gift Drop Off desk and " +
				"student volunteers will assist in unloading the gifts from your vehicle. " +
				"It is critically important for us to receive all gifts by Tuesday evening. " +
				"We inventory all gifts on Wednesday and must send volunteers out to purchase " +
				"any missing gifts that day.</p>" +
				"<p>In the event of a major snow event, please check our website for updates " +
				"and drop off your gifts as soon as safely possible!</p>" +
				"<p>We are deeply grateful for your faithful support of Our Neighbor's Child.  " +
				"Many hundreds " +
				"of children in need will have a brighter holiday because we all joined together " +
				"in this meaningful community effort.</p>" +
				"<p>Best wishes for your own happy holidays!</p>" +
				"<p>Sincerely,</p>" +
				"<p>Denise McInerney<br><br>Gift Partner Coordinator, " +
				"Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p>" +
//				"<img src=\"cid:" + cid0 + "\" />" +
				"</div></body></html>");
		
		return msg;
	}

	
	String createGivingTreeEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good afternoon!</p>" +
				"<p>We're happy to report that our ONC volunteers are on track to deliver " +
				"ornaments to all Giving Tree locations this Wednesday, November 20th.</p>" +
				"<p>This year's donated warehouse space is in a <b>NEW LOCATION:  " +
				"<a href=\"http://goo.gl/maps/eWCEr\">4311 Walney Road in Chantilly</a></b>.</p>" +
				"<p>As a reminder, these are are the dates for this season's Gift Drop-Off:</p>" + 
				"<p><b>Sunday, December 1st - 12PM - 2PM</b><br>" + 
				"(This is an \"undecorated\" drop-off, primarily for churches with " +
				"limited space to store gifts).</p>" +
				"<p><b>Sunday, December 8: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 9: 3PM - 6PM</b></p>" +
				"<p><b>Tuesday, December 10: 3PM - 6PM</b></p>" + 
				"<p>Please provide the name of your organization at the Gift Drop Off desk and " +
				"student volunteers will assist in unloading the gifts from your vehicle.</p>" +
				"<p>We are deeply grateful for your support of Our Neighbor's Child.  Many hundreds " +
				"of children in need will have a brighter holiday because we all joined together " +
				"in this meaningful community effort.</p>" +
				"<p>Best wishes for your own happy holidays!</p>" +
				"<p>Sincerely,</p>" +
				"<p>Chris Hobbs<br><br>Gift Partner Coordinator, " +
				"Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String createOrnamentDropOffEmailBody(String cid0)
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good morning!</p>" + 
				"<p>I meant to include this in my earlier e-mail:</p>" + 
				"<p><b>Ornament Delivery date: Wednesday, November 20th.</b></p>" + 
				"<p>Have a great day!</p>" + 
				"<p>Sincerely,</p>" +
				"<p>Chris Hobbs<br><br>Gift Partner Coordinator, " +
				"Giving Trees & General Gift Collection<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String create2015SeasonOrganizationEmailBody(Organization o, String cid0, String cid1)
//	String create2014SeasonOrganizationEmailBody(Organization o)
	{
		 //Create the variables for the body of the email     
        String name = o.getName();
        String address = o.getStreetnum() + " " + o.getStreetname() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipcode();
        String contact = o.getContact();
        String busphone = o.getPhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getPhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getPhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getPhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getPhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>The weather may be warm (and wet!) but planning is well underway to serve "
        		+ "our area's less fortunate children this holiday season.</p>"
        		+ "<p>With your continued and valued support, the <b>all-volunteer</b> team at "
        		+ "<b>Our Neighbor's Child</b> is gearing up to coordinate "
        		+ "our 24th year of community service. This effort is only possible when our community "
        		+ "comes together, including the consistent and generous support of ONC partners like %s.</p>"
        		+" <p>We hope you're able to join us again this year.  The need continues to be great, with hundreds "
        		+ "of families in our area still struggling to meet their families' most basic needs.  " 
        		+ "When we provide children's gifts at the holidays, it allows many of these families "
        		+ "to direct their limited financial resources toward essential housing, utilities and especially food.</p>"
        		+ "<p>We also hope it makes a difference in your life, and others like you who are fortunate enough to give.</p>"
        		+ "<p><b>Kindly review the following information we currently have on file for your organization:</b></p>"
        		+ "<font color=\"red\">"
        		+ "&emsp;ONC Gift Partner:  %s<br>"
        		+ "&emsp;Address:  %s<br>" 
        		+ "&emsp;Phone #:  %s<br>" 
        		+ "&emsp;Contact:  %s<br>" 
        		+ "&emsp;Phone #:  %s<br>" 
        		+ "&emsp;Email:  %s</font><br>", name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"<font color=\"red\">" +
        		"&emsp;Contact:  %s<br>" +
        		"&emsp;Phone #:  %s<br>" +
        		"&emsp;Email:  %s</font><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		"<font color=\"red\">"
        		+ "&emsp;Gift Collection Type: %s<br>"
        		+ "&emsp;Ornaments Requested in 2014:  %d<br>"
//        		+ "&emsp;Gifts Received By Deadline in 2014:  %d<br>"
        		+ "&emsp;Special Notes or Instructions:</font><br>"
        		+ "<p><b>Please reply at your earliest convenience with any corrections, updates or questions.</b></p>"
        		+ "I'll be your ONC Contact again this year as Gift Partner Coordinator and I hope you'll feel free "
        		+ "to contact me with any questions at any time.</p>"
        		+ "<p>This year we'll be delivering gift wish \"ornaments\" on Wednesday, November 18th.</p>"
        		+ "<p>Gift drop-off dates will be Sunday, Monday and Tuesday, December 6, 7, and 8. Delivery to the "
        		+ "families' homes will be Sunday, December 13.<p>"
        		+ "<p>I've included a few photos from prior seasons and hope you'll visit our web-site for more photos "
        		+ "and information on Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>. "
        		+ "We welcome you and anyone associated with your organization to join us in other volunteer activities as well.</p>"
        		+ "<p>Please remember that ONC remains an all volunteer organization and every penny we raise goes "
        		+ "directly to provide a gift for a child in need.</p>"
        		+ "<p>We look forward to working with you this season.</p>"
        		+" <p>Fondly,<br><br>"
        		+ "Denise McInerney<br>"
        		+ "Gift Partner Coordinator<br>"
        		+"Our Neighbor's Child<br>"
        		+"P.O. Box 276<br>"
        		+"Centreville, VA  20120<br>" 
        		+"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"+
        		"<p><div><img src=\"cid:" + cid0 + "\" /></div></p>" +
        		"<p><div><img src=\"cid:" + cid1 + "\" /></div></p>" +
        		"</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	void updateUserList()
	{
		UserDB userDB = UserDB.getInstance();
		
		bIgnoreCBEvents = true;
		changedByCB.setEnabled(false);
		String curr_sel = changedByCB.getSelectedItem().toString();
		int selIndex = 0;
		
		changedByCBM.removeAllElements();
		
		changedByCBM.addElement("Anyone");
		
		int index = 0;
		for(ONCUser user:userDB.getUserList())
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.setEnabled(true);
		bIgnoreCBEvents = false;
	}
	
	void updateRegionList(String[] regions)
	{
		regionCB.setEnabled(false);
		String currSel = regionCB.getSelectedItem().toString();
		
		regionCBM.removeAllElements();	//Clear the combo box selection list
		regionCBM.addElement("Any");
		
		for(String s: regions)	//Add new list elements
				regionCBM.addElement(s);
			
		//Reselect the prior region, if it still exists
		regionCB.setSelectedItem(currSel);
		
		regionCB.setEnabled(true);
	}
/*	
	void checkPrintEnabled()
	{
		if(sortTable.getSelectedRowCount() > 0)
		{
			printCB.setEnabled(true);
			if(gvs.isUserAdmin())	//Only admins or higher can send email
				emailCB.setEnabled(true);
			btnExport.setEnabled(true);
		}
	}
*/	
	void checkApplyChangesEnabled()
	{
		if(sortTable.getSelectedRows().length > 0 && (changePStatusCB.getSelectedIndex() > 0 || 
				!changeOrnReqCB.getSelectedItem().toString().equals(DEFAULT_NO_CHANGE_LIST_ITEM)))
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	}
	
	void onPrintListing()
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat("ONC Partners");
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             sortTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
             printCB.setSelectedIndex(0);
		} 
		catch (PrinterException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void onPrintPartnerInfo()
	{
		//pop up a dialog with the table and print it
		JDialog infoDlg = new InfoDialog(parentFrame);
		infoDlg.setLocationRelativeTo(printCB);
//		infoDlg.setVisible(true);
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"Partner ID", "Name", "Type", "Status", "Other", "Special Notes",
    						"Orn Req.", "Orn Assign.", "Del To",
    						"Address", "Unit", "City", "Zip", "Phhone #", 
    						"1st Contact", "1st Contact Email", "1st Contact Phone",
    						"2nd Contact", "2nd Contact Email", "2nd Contact Phone",
    						"Date Changed"};
    	
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
       	File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       										new FileNameExtensionFilter("CSV Files", "csv"), 1);
       	if(oncwritefile!= null)
       	{
       		//If user types a new filename without extension.csv, add it
	    	String filePath = oncwritefile.getPath();
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    int[] row_sel = sortTable.getSelectedRows();
	    	    for(int i=0; i<sortTable.getSelectedRowCount(); i++)
	    	    	writer.writeNext(getExportRow(row_sel[i]));
	    	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " partners sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(parentFrame, 
						"Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}
	
	String[] getExportRow(int index)
	{
		Organization o = stAL.get(index);
		
		SimpleDateFormat date = new SimpleDateFormat("MM-dd-yyyy");
		
		String[] row = {
						Long.toString(o.getID()),
						o.getName(),
						types[o.getType()],
						status[o.getStatus()+1],
						o.getOther(),
						o.getSpecialNotes(),
						Integer.toString(o.getNumberOfOrnamentsRequested()),
						Integer.toString(o.getNumberOfOrnamentsAssigned()),
						o.getDeliverTo(),
						Integer.toString(o.getStreetnum()) + " " + o.getStreetname(),
						o.getUnit(),
						o.getCity(),
						o.getZipcode(),
						o.getPhone(),
						o.getContact(),
						o.getContact_email(),
						o.getContact_phone(),
						o.getContact2(),
						o.getContact2_email(),
						o.getContact2_phone(),
						date.format(o.getDateChanged())};
		return row;
	}
	
	boolean doesStatusMatch(int st) { return sortStatus == 0 || st == statusCB.getSelectedIndex()-1; }
	
	boolean doesTypeMatch(int ty) { return sortType == 0 || ty == typeCB.getSelectedIndex(); }
	
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
	
	boolean doesStoplightMatch(int sl) { return sortStoplight == 0 || sl == stoplightCB.getSelectedIndex()-1; }
	
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == statusCB && statusCB.getSelectedIndex() != sortStatus)
		{						
			sortStatus = statusCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == typeCB && typeCB.getSelectedIndex() != sortType)
		{
			sortType = typeCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == regionCB && regionCB.getSelectedIndex() != sortRegion && !bIgnoreCBEvents)
		{						
			sortRegion = regionCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == changedByCB && changedByCB.getSelectedIndex() != sortChangedBy  && !bIgnoreCBEvents)
		{						
			sortChangedBy = changedByCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == stoplightCB && stoplightCB.getSelectedIndex() != sortStoplight  && !bIgnoreCBEvents)
		{						
			sortStoplight = stoplightCB.getSelectedIndex();
			buildTableList(false);
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1) { 
				onPrintListing();
			}
			else if(printCB.getSelectedIndex() == 2) {
				onPrintPartnerInfo();
			}
		}
		else if(e.getSource() == emailCB && emailCB.getSelectedIndex() > 0 && emailCB.getSelectedIndex() < 10)
		{
			//Confirm with the user that the deletion is really intended
			String confirmMssg = "Are you sure you want to send " + 
								 emailCB.getSelectedItem().toString() + "?"; 
											
			Object[] options= {"Cancel", "Send"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
					gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm " + emailCB.getSelectedItem().toString() + " ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Send"))
			{
				createAndSendPartnerEmail(emailCB.getSelectedIndex());	
			}
	
			emailCB.setSelectedIndex(0);	//Reset the email combo choice
		}
		else if(e.getSource() == btnExport)
		{
			onExportRequested();	
		}
		else if(e.getSource() == changePStatusCB)
		{
			checkApplyChangesEnabled();
		}
		else if(e.getSource() == changeOrnReqCB)
		{
			checkApplyChangesEnabled();
		}
	}
	
	void onResetCriteriaClicked()
	{
		statusCB.removeActionListener(this);
		statusCB.setSelectedIndex(0);
		sortStatus = 0;
		statusCB.addActionListener(this);
		
		typeCB.removeActionListener(this);
		typeCB.setSelectedIndex(0);
		sortType = 0; 
		typeCB.addActionListener(this);
		
		regionCB.removeActionListener(this);
		regionCB.setSelectedIndex(0);
		sortRegion = 0; 
		regionCB.addActionListener(this);
		
		changedByCB.removeActionListener(this);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.addActionListener(this);
		
		stoplightCB.removeActionListener(this);
		stoplightCB.setSelectedIndex(0);
		sortStoplight = 0;
		stoplightCB.addActionListener(this);
		
		buildTableList(false);
	}
	

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		//If user selects an organization in the table, notify the organization dialog that
		//the selection occurred so the dialog can display that organization
		ListSelectionModel stLSM = sortTable.getSelectionModel();
		if(!lse.getValueIsAdjusting() && lse.getSource() == stLSM &&
				sortTable.getSelectedRow() > -1 && !bChangingTable)
		{
			fireEntitySelected(this, "PARTNER_SELECTED", stAL.get(sortTable.getSelectedRow()), null);
			setEnabledControls(true);
		}
		
		checkApplyChangesEnabled();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName() == "progress")
		{
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
		else if(evt.getPropertyName() == "state")
		{
			if(evt.getNewValue() == SwingWorker.StateValue.DONE)
			{
				emailCB.setSelectedIndex(0);
				emailCB.setEnabled(true);
			}
		}
		
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && 
			(dbe.getType().equals("UPDATED_PARTNER") ||
			  dbe.getType().equals("ADDED_PARTNER") ||
			   dbe.getType().equals("WISH_PARTNER_CHANGED") ||
			    dbe.getType().equals("DELETED_PARTNER") ||
			     dbe.getType().equals("DELETED_CHILD")))
		{
			buildTableList(true);		
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject();
			updateRegionList(regList);
		}
		else if(dbe.getType().contains("_USER"))
		{
			updateUserList();
		}
	}

	private class InfoDialog extends JDialog
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		InfoDialog(JFrame parentFrame)
		{
			super(parentFrame, true);
			
			//Create a table with partner info	
			JTable infoTable = new JTable()
			{
				private static final long serialVersionUID = 1L;
				
				public boolean getScrollableTracksViewportWidth()
				{
					return getPreferredSize().width < getParent().getWidth();
				}
				
				public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
				{
					Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
								  		 
					if(isRowSelected(Index_row))
						comp.setBackground(comp.getBackground());
					else if (Index_row % 2 == 1)			  
						comp.setBackground(new Color(240,248,255));
					else
						comp.setBackground(Color.white);
								  
					return comp;
				}
			};

			//Set up columns
			String[] columns = {"Partner", "Phone", "1st Contact", "1st Contact Email", "2nd Contact", "2nd Contact Email"};
			DefaultTableModel infoTableModel = new DefaultTableModel(columns, 0)
			{
				private static final long serialVersionUID = 1L;
				@Override
				//All cells are locked from being changed by user
					public boolean isCellEditable(int row, int column) {return false;}
			};
					     	        
			infoTable.setModel(infoTableModel);
					        
//			infoTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
					        
			//Set table column widths
			int[] colWidths = {180, 128, 120, 164, 120, 164};
			for(int i=0; i < colWidths.length; i++)
				infoTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			
			//Set header color
			JTableHeader anHeader = infoTable.getTableHeader();
			anHeader.setForeground( Color.black);
			anHeader.setBackground( new Color(161,202,241));	
	
			//add rows to the table
			for(Organization o:stAL)	//Build the new table
				infoTableModel.addRow(o.getOrgInfoTableRow());
			
			infoTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
			infoTable.setFillsViewportHeight(true);
			
			//Create the scroll pane and add the table to it.
			JScrollPane infoScrollPane = new JScrollPane(infoTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
					        									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					        
			infoScrollPane.setPreferredSize(new Dimension(800, infoTable.getRowHeight()*NUM_ROWS_TO_DISPLAY));
			
			//render it
			this.getContentPane().add(infoScrollPane);
			
			this.pack();
			
			//print the jtable
			try
			{
				 MessageFormat headerFormat = new MessageFormat("ONC Partner Information");
	             MessageFormat footerFormat = new MessageFormat("- {0} -");
	             infoTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);
	             printCB.setSelectedIndex(0);
			} 
			catch (PrinterException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	boolean isONCNumContainerEmpty() { return false; }	
}
