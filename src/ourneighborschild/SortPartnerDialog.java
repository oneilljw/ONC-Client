package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
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
    private static final String GIFT__BUSINESS_PARTNER_EMAIL_SENDER_ADDRESS = "partnercontact@ourneighborschild.org";
    private static final String GIFT_CHURCH_SCHOOL_EMAIL_SENDER_ADDRESS = "GiftCoordinator@ourneighborschild.org";
//	private static final String CLOTHING_PARTNER_EMAIL_SENDER_ADDRESS = "Clothing@ourneighborschild.org";
    
    private RegionDB regions;
    private PartnerDB orgs;
    private ChildDB childDB;
	
    private JComboBox<String> regionCB, statusCB, typeCB, changedByCB, changeOrnReqCB;
    private JComboBox<GiftCollectionType> collectionCB;
    private JComboBox<ImageIcon> stoplightCB;
    private ComboItem[] changePartItem;
    private JComboBox<ComboItem> changePStatusCB;
    private DefaultComboBoxModel<String> regionCBM, changedByCBM;
    private JComboBox<String> printCB, emailCB, exportCB;
    private JLabel lblOrnReq;
    private ArrayList<ONCPartner> stAL;

    private int sortStatus = 0, sortType = 0, sortRegion = 0, sortChangedBy = 0, sortStoplight = 0;
    private GiftCollectionType sortCollection = GiftCollectionType.Any;
	
    private String[] status = {"Any","No Action Yet", "1st Email Sent", "Responded", "2nd Email Sent", "Called, Left Mssg",
							   "Confirmed", "Not Participating"};
	
    private String[] types = {"Any","Business","Church","School", "Individual", "Internal"};
	
    private String[] columns;

    private JProgressBar progressBar;
    private ONCEmailer oncEmailer;
	
    SortPartnerDialog(JFrame pf)
    {
    		super(pf);
    		this.columns = getColumnNames();
    		this.setTitle("Our Neighbor's Child - Partner Management");
		
    		regions = RegionDB.getInstance();
    		orgs = PartnerDB.getInstance();
    		childDB = ChildDB.getInstance();
		
    		//Get reference for data base listeners
    		UserDB userDB = UserDB.getInstance();
    		if(userDB != null)
    			userDB.addDatabaseListener(this);
    		
    		if(dbMgr != null)
    			dbMgr.addDatabaseListener(this);
				
    		if(orgs != null)
    			orgs.addDatabaseListener(this);
		
    		if(regions != null)
    			regions.addDatabaseListener(this);
		
    		if(childDB != null)
    			childDB.addDatabaseListener(this);
		
    		ChildGiftDB childwishDB = ChildGiftDB.getInstance();
    		if(childwishDB != null)
    			childwishDB.addDatabaseListener(this);	//listen for partner gift assignment changes
		
    		//Set up the array lists
    		stAL = new ArrayList<ONCPartner>();
				
    		//Set up the search criteria panel      
    		statusCB = new JComboBox<String>(status);
    		statusCB.setBorder(BorderFactory.createTitledBorder("Partner Status"));
    		statusCB.addActionListener(this);
				
    		typeCB = new JComboBox<String>(types);
    		typeCB.setBorder(BorderFactory.createTitledBorder("Partner Type"));
    		typeCB.addActionListener(this);
		
    		collectionCB = new JComboBox<GiftCollectionType>(GiftCollectionType.values());
    		collectionCB.setBorder(BorderFactory.createTitledBorder("Collection Type"));
    		collectionCB.addActionListener(this);
				
    		regionCBM = new DefaultComboBoxModel<String>();
    		regionCBM.addElement("Any");
    		regionCB = new JComboBox<String>();
    		regionCB.setModel(regionCBM);
    		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
    		regionCB.addActionListener(this);
				
    		changedByCB = new JComboBox<String>();
    		changedByCBM = new DefaultComboBoxModel<String>();
    		changedByCBM.addElement("Anyone");
    		changedByCB.setModel(changedByCBM);
    		changedByCB.setPreferredSize(new Dimension(144, 56));
    		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
    		changedByCB.addActionListener(this);
				
    		stoplightCB = new JComboBox<ImageIcon>(GlobalVariablesDB.getLights());
    		stoplightCB.setPreferredSize(new Dimension(80, 56));
    		stoplightCB.setBorder(BorderFactory.createTitledBorder("Stoplight"));
    		stoplightCB.addActionListener(this);
				
    		//Add all sort criteria components to dialog pane
    		sortCriteriaPanelTop.add(statusCB);
    		sortCriteriaPanelTop.add(typeCB);
    		sortCriteriaPanelTop.add(collectionCB);
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
				
    		changePStatusCB = new JComboBox<ComboItem>(changePartItem);
    		changePStatusCB.setRenderer(new ComboRenderer());
    		changePStatusCB.setPreferredSize(new Dimension(172, 56));
    		changePStatusCB.setBorder(BorderFactory.createTitledBorder("Change Partner Status"));
    		changePStatusCB.addActionListener(new ComboListener(changePStatusCB));	//Prevents selection of disabled combo box items
    		changePStatusCB.addActionListener(this);	//Used to check for enabling the Apply Changes button
				
    		String[] choices = {DEFAULT_NO_CHANGE_LIST_ITEM, "25", "50", "75", "100",
									"125", "150", "175", "200", "250", "300", "400", "500"};
    		changeOrnReqCB = new JComboBox<String>(choices);
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
        
    		//Set up the control panel that goes in the bottom panel with border layout
        JPanel cntlPanel = new JPanel();
      	cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      	
      	String[] exportChoices = {
      				"Export Data",
      				"Gmail Contact List",
      				"Partner Info",
      				"Partner Performance",
      				"Partner Letter Data"
				};
      	exportCB = new JComboBox<String>(exportChoices);
      	exportCB.setPreferredSize(new Dimension(136, 28));
      	exportCB.setEnabled(false);
      	exportCB.addActionListener(this);
		
      	//Set up the email progress bar
      	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
				
        String[] emailChoices = {
								"Email",
								"2021 Church Email - Diane Church",
								"2021 School Email - Diane Church",
								"2021 Business Email - Kathleen McDonald"
								};
        emailCB = new JComboBox<String>(emailChoices);
        emailCB.setPreferredSize(new Dimension(136, 28));
        emailCB.setEnabled(false);
        emailCB.addActionListener(this);
			    
        String[] printChoices = {"Print", "Print Listing", "Print Partner Info", "Print Bag Labels"};
        printCB = new JComboBox<String>(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
  
        cntlPanel.add(progressBar);
        cntlPanel.add(exportCB);
        cntlPanel.add(emailCB);
        cntlPanel.add(printCB);
		
        bottomPanel.add(cntlPanel, BorderLayout.CENTER);

        //Add the change and bottom panels to the dialog pane
        this.add(changePanel);
        this.add(bottomPanel);
	        
        pack();
    }

    @Override
    public Object[] getTableRow(ONCObject obj)
    {
    		ONCPartner o = (ONCPartner) obj;
    		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    		Object[] sorttablerow = {o.getLastName(), status[o.getStatus()+1], types[o.getType()],
				o.getGiftCollectionType().toString(),
				Integer.toString(o.getNumberOfOrnamentsRequested()),
				Integer.toString(o.getNumberOfOrnamentsAssigned()),
				Integer.toString(o.getNumberOfOrnamentsDelivered()),
				Integer.toString(o.getNumberOfOrnamentsReceivedBeforeDeadline()),
				Integer.toString(o.getNumberOfOrnamentsReceivedAfterDeadline()),
				o.getDeliverTo(),
				sdf.format(o.getTimestampDate().getTime()),
				o.getChangedBy(),
				regions.getRegionID(o.getRegion()),
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
		
    		for(ONCPartner o : orgs.getList())
    		{
    			if(doesStatusMatch(o.getStatus()) &&
    				doesTypeMatch(o.getType()) &&
    				 doesCollectionMatch(o.getGiftCollectionType()) && 
    				  doesRegionMatch(o.getRegion()) &&
    				   doesChangedByMatch(o.getChangedBy()) &&
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
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
				emailCB.setEnabled(true);	//Only admins or higher can send email
			exportCB.setEnabled(true);
		}
		else
		{
			printCB.setEnabled(false);
			emailCB.setEnabled(false);
			exportCB.setEnabled(false);
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
			ONCPartner updatedOrg = new ONCPartner(stAL.get(row_sel[i]));	//make a copy for update request
			
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
				updatedOrg.setDateChanged(System.currentTimeMillis());
				updatedOrg.setStoplightChangedBy(userDB.getUserLNFI());
				
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
//		String cid0 = null, cid1 = null;
		String emailBody = null, subject = null;
		boolean bIncludesAttachments = false;
		boolean bAttachmentsSelected = false;
		
		//Create the subject
		subject = "Greetings From Our Neighbor's Child";
/*		
		//Create the subject and attachment array list
		if(emailType == 1)	//church email
		{
			subject = "Greetings From Our Neighbor's Child";
			bIncludesAttachments = false;
			
			//construct a file chooser
			ONCFileChooser fc = new ONCFileChooser(this);
			FileNameExtensionFilter fnef = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
			
			//get first file to attach
			File attachmentfile0= fc.getFile("Select 1st jpg file to include with the email",
					fnef, ONCFileChooser.OPEN_FILE);
			
			if(attachmentfile0 != null)
			{
				System.out.println(attachmentfile0.getAbsolutePath());
				cid0 = ContentIDGenerator.getContentId();
				attachmentAL.add(new ONCEmailAttachment(attachmentfile0, cid0, MimeBodyPart.INLINE));
				
				File attachmentfile1= fc.getFile("Select 2nd jpg file to include with the email",
						new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"), ONCFileChooser.OPEN_FILE);
				
				//get second file to attach
				if(attachmentfile1 != null)
				{
					cid1 = ContentIDGenerator.getContentId();
					attachmentAL.add(new ONCEmailAttachment(attachmentfile1, cid1, MimeBodyPart.INLINE));
					bAttachmentsSelected = true;
				}
			}
				
		}
		else if(emailType == 2) //business email
		{
			subject = "Greetings From Our Neighbor's Child";
			bIncludesAttachments = false;
						
			//construct a file chooser
			ONCFileChooser fc = new ONCFileChooser(this);
			FileNameExtensionFilter fnef = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
			
			//get first file to attach
			File attachmentfile0= fc.getFile("Please select 1st jpg file to include with the partner email",
					fnef, ONCFileChooser.OPEN_FILE);
			
			if(attachmentfile0 != null)
			{
				cid0 = ContentIDGenerator.getContentId();
				attachmentAL.add(new ONCEmailAttachment(attachmentfile0, cid0, MimeBodyPart.INLINE));
				
				File attachmentfile1= fc.getFile("Please select 2nd jpg file to include with the partner email",
						fnef, ONCFileChooser.OPEN_FILE);
				
				//get second file to attach
				if(attachmentfile1 != null)
				{
					cid1 = ContentIDGenerator.getContentId();
					attachmentAL.add(new ONCEmailAttachment(attachmentfile1, cid1, MimeBodyPart.INLINE));
					bAttachmentsSelected = true;
				}
			}				
		}
		if(emailType == 3)	//church & school gift drop off email
		{
			subject = "ONC Gift Drop Off Times and Location";
		}
		else if(emailType == 4) //business gift drop off email
		{
			subject = "ONC Gift Drop Off Times and Location";
		}
*/			
		//For each organization selected, create the email body and recipient information in an
		//ONCEMail object and add it to the emailAL
		int[] row_sel = sortTable.getSelectedRows();
		for(int row=0; row< sortTable.getSelectedRowCount(); row++)
		{
			//Get organization object
			ONCPartner o = stAL.get(row_sel[row]);
			
			//Create the email body and potentially subject
	        if(emailType == 1)	//Church Email
	        	emailBody = create2021SeasonChurchEmailBody(o);
	        if(emailType == 2)	//School Email
	        	emailBody = create2021SeasonSchoolEmailBody(o);
	        if(emailType == 3)	//Business Email
	        	emailBody = create2021SeasonBusinessEmailBody(o);
	        
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
     
	        emailAL.add(new ONCEmail(subject, emailBody, toAddressList));     
		}
		
		//Create the from address string array
		EmailAddress fromAddress = null;
		if(emailType == 1)
		    fromAddress = new EmailAddress(GIFT_CHURCH_SCHOOL_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child - Diane Church");
		else if(emailType == 2)
		    fromAddress = new EmailAddress(GIFT_CHURCH_SCHOOL_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child - Diane Church");
		else if(emailType == 3)
		    fromAddress = new EmailAddress(GIFT__BUSINESS_PARTNER_EMAIL_SENDER_ADDRESS , "Our Neighbor's Child - Kathleen McDonald");
		
		//Create the blind carbon copy list of EmailAddress objects
		ArrayList<EmailAddress> bccList = new ArrayList<EmailAddress>();
		if(emailType == 1 || emailType == 2)
		    bccList.add(new EmailAddress(GIFT_CHURCH_SCHOOL_EMAIL_SENDER_ADDRESS, "Gift Partner Coordinator"));
		else if(emailType == 3)
		    bccList.add(new EmailAddress(GIFT__BUSINESS_PARTNER_EMAIL_SENDER_ADDRESS , "Partner Contact"));
		
		//Create mail server credentials, then the mailer background task and execute it
		ServerCredentials creds = null;
		if(emailType == 1 || emailType == 2)
		    creds = new ServerCredentials("smtp.gmail.com", GIFT_CHURCH_SCHOOL_EMAIL_SENDER_ADDRESS, "ChurchSchool!");
		else if(emailType == 3)
		    creds = new ServerCredentials("smtp.gmail.com", GIFT__BUSINESS_PARTNER_EMAIL_SENDER_ADDRESS, "crazyelf");
	
	    if(fromAddress != null && creds != null && (!bIncludesAttachments || (bIncludesAttachments && bAttachmentsSelected)))
	    {
	    	//Everything looks good to send. Confirm with the user that sending email is really intended
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
	    			oncEmailer = new ONCEmailer(this, progressBar, fromAddress, bccList, emailAL, attachmentAL, creds);
	    			oncEmailer.addPropertyChangeListener(this);
	    			oncEmailer.execute();
	    	}
	    }
	    
	    emailCB.setEnabled(false);		
	}
/*	
	void sendTestGmail()
	{
		
	}
	
	 public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException
	 {
		 Properties props = new Properties();
		 Session session = Session.getDefaultInstance(props, null);

		 MimeMessage email = new MimeMessage(session);

		 email.setFrom(new InternetAddress(from));
		 email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
		 email.setSubject(subject);
		 email.setText(bodyText);
		 return email;
	 }
*/	 
/*	
	String create2015ClothingDonorEmailBody(String cid0)
	{	
		String msg = String.format("<html><body>" +
				"<div><p>Deck the Halls and Ho! Ho! Ho!  It's that time of year again! Our Neighbor's Child is " +
				"gearing up for our 24th wonderful holiday season!</p>" +
				"<p>I'm reaching out to you because you have generously helped us in the past and "
				+ "we are really hoping you can do it again this year!  ONC is " +
				"so thankful for any help you can provide, whether it's donating a coat for one child or \"adopting\" the " +
				"clothing needs of an entire family. We expect to have all of our \"wish lists\" from the families we " +
				"serve by " +
				"<font color=\"red\">Wednesday, November 18th</font> " +
				"(in time for Black Friday shopping!!)</p>" +
				"<p>If you would like to help again this year, just reply to this e-mail and let me know how "
				+ "many children you " +
				"would like to provide clothing for and if you have a preference for a certain age or " +
				"gender.  I will do my best to match you up with your request and will get back to you " +
				"as soon as the \"wish list\" is available.</p> " +
				"<p><b>OH WAIT!!</b> If you have friends or family, or bunko groups, bible study groups, boy scouts, girl scouts, " +
				"sports teams, etc. who you think would also be interested in participating, please forward " +
				"this e-mail!!!  I've heard back from a number of different clubs that really enjoy going out " +
				"shopping together and/or pooling their time and money into providing for lots of families!! " +
				"We'd love your help in getting the word out about Our Neighbor's Child!!</p>"
				+ "<p>For more information " +
				"about ONC and other volunteer opportunities, please visit our website: " +
				"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a> " +
				"We can't thank you enough for all you do!</p>" +
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
				"<a href=\"https://goo.gl/maps/KR64TFEqJGw\">4315 Walney Road in Chantilly</a></b>.</p>" +
				"</p>It's between Route 28 and Route 50, just south of Willard Rd. Look for our ONC directional signs!</p>" +
				"<p>As a reminder, these are the dates for this season's Gift Drop-Off:</p>" +
				"<p><b>Sunday, December 6th: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 7th: 3:30PM - 6:30PM</b></p>" +
				"<p><b>Tuesday, December 8th: 3:30PM - 6:30PM</b></p>" +
				"<p>It is critically important for us to receive all gifts by Tuesday the 8th at 6PM. " +
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
				"<p>Warmest Regards,</p>" +
				"<p>Stephanie Somers<br>Clothing Coordinator<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>");
		
		return msg;
	}
	
	String create2016ClothingDonorEmailBody(String donorFN, String cid0)
	{	
		String msg = String.format("<html><body><div>" +
				"<p>Dear %s,</p>" +
				"<p><font color=\"green\">\"Unless someone like you cares a whole awful lot, nothing is going to get better. It's not.\"    Dr. Seuss.</font></p>" +
				"<p>Thankfully, someone like you HAS cared.  ONC is celebrating " +
				"<font color=\"red\"><b>25 years</b></font>" +
				" of providing " +
				"holiday joy to our neighbors in need.  YOU made that possible.</p>" +
				"<p>Would you, could you . . . help us again this year?  " +
				"It's as easy as replying to this email with what sort of family you can \"adopt\".  " +
				"We have families with one or two children all the way up to ten!  We can match you with children of a particular age or a mix, from infants to teens.</p>" +
				"<p><b>I will have wish information to send on November 16th</b> this year . . . in plenty of time for on-line or Black Friday shopping!</p>" +
				"<p>NEW THIS YEAR!  You can request a \"Front Porch Pick-Up\" of your donations on Sunday, December 11 from 9AM to 12 Noon.</p>" +
				"<p>If you prefer to drop off your donations at our always festive ONC warehouse " +
				"location (TBD), please know you are ALWAYS welcome!  Our \"elves\" from Westfield, " +
				"Chantilly and Centreville High School will be at the curb for drive by drop offs " +
				"or feel free to park and come on in!</p>" +
				"<p>This year's drop off dates and times: </p>" +
				"<p><b>Sunday, December 11th: 12:00 Noon - 2:00PM</b></p>" +
				"<p><b>Monday, December 12th: 3:30PM - 6:30PM</b></p>" +
				"<p><b>Tuesday, December 13th: 3:30PM - 6:30PM</b></p>" +
				"<p>Whether or not you are able to give this year, we sincerely appreciate your " +
				"past support and hope you will consider forwarding this email or sharing the information " +
				"on Facebook, Twitter, Instagram or in a good old fashioned conversation!</p>" +
				"<p>Did you know that some of the children we've served are now participating WITH us " +
				"as volunteers?  It's true.  They remember what it meant to them and want to bring " +
				"that feeling (that people truly CARE) to other children in need.</p>" +
				"<p>More volunteer opportunities are available by clicking the Volunteer link on the ONC homepage at " +
				"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>" +
				".</p>" +
				"<p>I can't think of a better way to celebrate the holiday season.</p>" +
				"<p>Best Regards,</p>" +
				"<p>Stephanie Somers<br>Clothing Coordinator/Volunteer<br>" +
				"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
				"<img src=\"cid:" + cid0 + "\" /></p>" +
				"</div></body></html>", donorFN);
		
		return msg;
	}
*/	
	
	String create2017McDonaldGiftDropOffEmailBody()
	{
		String msg = String.format("<html><body><div>"
				+ "<p>Just a reminder of the three ONC Gift Drop-Off dates that begin next Sunday, December 10<sup>th</sup>.</p>"
				+ "<p>Each year we rely on a new donated warehouse space for our community service efforts. "
				+ "This year was a challenge, but we were finally able to secure a <b>different</b> unit in last year's building: "
				+ "<a href=\"https://goo.gl/maps/quPpmCNBjsM2\">3900 Stonecroft Boulevard in Chantilly</a></b>.</p>"
				+ "<p>From Route 50 (west of Route 28), take a right on Stonecroft. Make a left at 3900 "
				+ "and look for ONC directional signs!  We're in the last unit on the right, past Cosmos Granite "
				+ "and before the chain link fence.</p>"
				+ "<p>This season's Gift Drop-Off dates/times are:</p>"
				+ "<p><b>Sunday, December 10: 12PM - 2PM</b></p>"
				+ "<p><b>Monday, December 11: 3:30PM - 6:30PM</b></p>"
				+ "<p><b>Tuesday, December 12: 3:30PM - 6:30PM</b></p>"
				+ "<p>Please park first and check in at the front desk with the name of your organization. "
				+ "Student volunteers will be happy to assist in unloading the unwrapped gifts from your vehicle.</p>"
				+ "<p>PLEASE be sure that your gifts arrive <b>no later</b> than 6:30PM on Tuesday "
				+ "evening. Any gift that hasn't arrived by that time will be added to "
				+ "our \"missing gift list\" and we'll need to send volunteers out to shop for them. "
				+ "We greatly appreciate your efforts to help keep that list to a minimum.</p>"
				+ "<p><b>In case of a major weather event, please check our website for updates and "
				+ "drop off your gifts as soon as safely possible!</b></p>"
				+ "<p>Thank you for all your efforts to make the holidays brighter for the children in our community.</p>"
				+ "<p>If you have any questions or concerns, email is best, but please feel free to contact me at (703) 785-8048.</p>"
				+ "<p>Best wishes for your own happy holidays!</p>" 
				+"<p>Sincerely,</p>"
				+"<p>Kathleen McDonald<br>"
				+"Gift Partner Coordinator<br>"
				+"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>"
				+"</div></body></html>");
		
		return msg;
	}
	
	String create2017ChurchGiftDropOffEmailBody()
	{
		String msg = String.format("<html><body><div>"
				+ "<p>Just a reminder of the three ONC Gift Drop-Off dates that begin next Sunday, December 10<sup>th</sup>.</p>"
				+ "<p>Each year we rely on a new donated warehouse space for our community service efforts. "
				+ "This year was a challenge, but we were finally able to secure a <b>different</b> unit in last year's building: "
				+ "<a href=\"https://goo.gl/maps/quPpmCNBjsM2\">3900 Stonecroft Boulevard in Chantilly</a></b>.</p>"
				+ "<p>From Route 50 (west of Route 28), take a right on Stonecroft. Make a left at 3900 "
				+ "and look for ONC directional signs!  We're in the last unit on the right, past Cosmos Granite "
				+ "and before the chain link fence.</p>"
				+ "<p>This season's Gift Drop-Off dates/times are:</p>"
				+ "<p><b>Sunday, December 10: 12PM - 2PM</b></p>"
				+ "<p><b>Monday, December 11: 3:30PM - 6:30PM</b></p>"
				+ "<p><b>Tuesday, December 12: 3:30PM - 6:30PM</b></p>"
				+ "<p>Please park first and check in at the front desk with the name of your organization. "
				+ "Student volunteers will be happy to assist in unloading the unwrapped gifts from your vehicle.</p>"
				+ "<p>PLEASE be sure that your gifts arrive <b>no later</b> than 6:30PM on Tuesday "
				+ "evening. Any gift that hasn't arrived by that time will be added to "
				+ "our \"missing gift list\" and we'll need to send volunteers out to shop for them. "
				+ "We greatly appreciate your efforts to help keep that list to a minimum.</p>"
				+ "<p><b>In case of a major weather event, please check our website for updates and "
				+ "drop off your gifts as soon as safely possible!</b></p>"
				+ "<p>Thank you for all your efforts to make the holidays brighter for the children in our community.</p>"
				+ "<p>If you have any questions or concerns, email is best, but please feel free to contact me at (703) 615-1934.</p>"
				+ "<p>Best wishes for your own happy holidays!</p>" 
				+"<p>Sincerely,</p>"
				+"<p>Diane Church<br>"
				+"Gift Partner Coordinator<br>"
				+"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>"
				+"</div></body></html>");
		
		return msg;
	}
/*	
	String create2016ClothingDropOffEmailBody(String partnerName)
	{
		String msg = String.format("<html><body><div>"
				+ "<p>Dear %s,</p>"
				+ "<p>Just a reminder of the three ONC Clothing Gift Drop-Off dates that begin next Sunday, December 11<sup>th</sup>.</p>"
				+ "<p>Each year we rely on a new donated warehouse space for our community service efforts. "
				+ "This year's <b>NEW LOCATION</b> is: "
				+ "<a href=\"https://goo.gl/maps/DUGd19Jzdqk\">3900 Stonecroft Boulevard in Chantilly</a></b>.</p>"
				+ "<p>From Route 50 (west of Route 28), take a right on Stonecroft and look for ONC directional signs!</p>"
				+ "<p>This season's Clothing Gift Drop-Off dates/times are:</p>"
				+ "<p><b>Sunday, December 11: 12PM - 2PM</b></p>"
				+ "<p><b>Monday, December 12: 3:30PM - 6:30PM</b></p>"
				+ "<p><b>Tuesday, December 13: 3:30PM - 6:30PM</b></p>"
				+ "<p>If you are a Centreville resident and would like to use our Front Porch "
				+ "Pick-up option, please reply to this email with \"Front Porch Pick-Up\" "
				+ "in the subject line NO LATER than <u>8AM on Sunday, December 11</u>. "
				+ "Student volunteers will make pick-ups that day from 9AM- 12PM.</p>"
				+ "<p><i>Please don't forget to clearly label your clothing gifts!!!</i></p>"
				+ "<p>It is vitally important to our efforts that all gifts arrive at the warehouse "
				+ "no later than 6:30PM Tuesday evening.  We inventory <i>all</i> gifts on Wednesday and "
				+ "need to send volunteers out to purchase any missing gifts that day.</p>"
				+ "<p><b>In case of a major snow event, please check our website for updates and "
				+ "drop off your clothing gifts as soon as safely possible!</b></p>"
				+ "<p>Thank you for your willingness to support the children of our community in this way.</p>"
				+ "<p>If you have any questions or concerns, please reply to this email.</p>"
				+ "<p>Best wishes for your own happy holidays!</p>" 
				+"<p>Sincerely,</p>"
				+"<p>Stephanie Somers<br>ONC Clothing Coordinator<br>"
				+"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>"
				+"</div></body></html>", partnerName);
		
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
	
	String create2016ClothingDonorNotTooLateEmailBody()
	{
		String msg = String.format("<html><body><div>" +
			"<p>Hello Again!</p>" + 
			"<p>Just wanted to send you a quick e-mail and let you know that it's NOT TOO LATE!!  " +
			"I'm still busy assigning clothing wishes for the children, and wanted to make sure " +
			"that I didn't miss anyone who wanted to adopt a child or two or even an entire family.</p>" + 
			"<p>Please know that we have sincerely appreciated your generous past support.  If you are " +
			"unable to participate in clothing \"adoptions\" this season, we hope you'll consider joining " +
			"us in other ways or at a future time.  I only send a follow-up e-mail to be sure that none " +
			"were missed or sent to SPAM!</p>" +
			"<p>Please let me know if you'd like to help and I will assign you your kids right away.  " +
			"Drop off days are <b>December 11 (12:00-2:00 p.m.)</b>, and <b>December 12-13 (3:30-6:30 p.m.)</b>.</p>" +
			"<p>This year's donated warehouse space has been confirmed: " +
			"<a href=\"https://goo.gl/maps/DUGd19Jzdqk\">3900 Stonecroft Boulevard in Chantilly</a></b>."
			+ " From Route 50 (west of Route 28), take a right on Stonecroft and look for ONC directional signs!</p>" +
			"<p>Thanks so much,</p>" +
			"<p>Stephanie Somers<br>Clothing Coordinator<br>" +
			"<a href=\"http://www.ourneighborschild.org\">Our Neighbor's Child</a></p></div>" +
			"</div></body></html>");
		
		return msg;
	}
	
	String create2015GivingTreeEmailBody()
	{
		String msg = String.format("<html><body>" +
				"<div><p>Good evening!</p>" +
				"<p>We're happy to report that our ONC volunteers will have all " +
				"ornaments delivered to our Giving Tree locations by <b>this Wednesday, November 18th.</b></p>" +
				"<p>If your organization hosts a generic Gift Collection, there will be no ornaments, " +
				"but the information below is for you as well:</p>" +
				"<p>As in year's past we are fortunate to have a new donated warehouse space. The <b>NEW LOCATION is: " +
				"<a href=\"https://goo.gl/maps/KR64TFEqJGw\">4315 Walney Road in Chantilly</a></b>.</p>" +
				"</p>It's between Route 28 and Route 50, just south of Willard Rd. Look for our ONC directional signs!</p>" +
				"<p>As a reminder, these are are the dates for this season's Gift Drop-Off:</p>" + 
				"<p><b>Sunday, December 6th: 12PM - 2PM</b></p>" +
				"<p><b>Monday, December 7th: 3:30PM - 6:30PM</b></p>" +
				"<p><b>Tuesday, December 8th: 3:30PM - 6:30PM</b></p>" + 
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
*/
	String create2018SeasonChurchSchoolEmailBody(ONCPartner o, String cid0, String cid1)
	{
		 //Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>With your continued and valued support, our <b>all-volunteer</b> team at "
        		+ "<b>Our Neighbor's Child</b> is gearing up to coordinate "
        		+ "holiday assistance for children from low income families in western Fairfax County.</p>"
        		+ "<p>This neighbor-to-neighbor effort is only possible when our community "
        		+ "comes together and through the consistent, generous support of ONC partners like %s.</p>"
        		+ "<p>Many families in our area struggle to meet their most basic needs. "
        		+ "When we help out with children's gifts at the holidays, it allows many of these "
        		+ "families to direct their financial resources toward essential housing costs, "
        		+ "utilities and critically important food essentials.</p>"
        		+ "<p>We sincerely hope participating makes a difference in your life, and the lives of "
        		+ "others fortunate and generous enough to give.</p>"
        		+ "<p><b>Would you mind taking a moment to review our notes from last year? "
        		+ "Accurate information is KEY to our successful partnership:</b></p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact.toUpperCase(), contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2.toUpperCase(), contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2017:</font>   <b>%d</b><br>"
        		+ "<p><b>Please reply at your earliest convenience with any corrections, updates or "
        		+ "questions - especially if our point of contact has changed.</b></p>"
        		+ "<p>Here are a few important dates:</p>"
        		+" <p>Gift wish \"ornaments\" will be delivered on <b>Wednesday, November 14</b>.</p>"
        		+ "<p>Gift drop-off dates will be:<br>"
        		+ "<b>Sunday, December 9, Noon to 2pm<br>"
        		+ "Monday, December 10, 3:30 - 6:30pm<br>"
        		+ "Tuesday, December 11, 3:30pm - 6:30pm</b>"
        		+ "<p>Gifts will be home delivered on <b>Sunday, December 16</b>.</p>"
        		+"<p>We rely on donated warehouse space each year and will forward the (Chantilly area) "
        		+ "address as soon as it's confirmed.</p>"
        		+ "<p>I've included a few photos from prior seasons and hope you'll visit our website for more photos "
        		+ "and information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>. "
        		+ "We welcome you and anyone associated with your organization to join us in other "
        		+ "volunteer activities as well.</p>"
        		+ "<p>Though the number of families needing holiday assistance in our community has "
        		+ "grown exponentially in 26 years, ONC has continued as an ALL volunteer "
        		+ "organization with EVERY donation dollar going directly to gifts delivered to a child in need.</p>"
        		+ "<p>We are deeply grateful for your support and look forward to working with you "
        		+ "again this holiday season! Please feel free to contact me with any questions at 703-615-1934.</p>"
        		+ "<p>I will keep in contact with you or the current contact as our season of giving progresses.</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "703-615-1934<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "P.O. Box 276<br>"
        		+ "Centreville, VA  20120<br>" 
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "<p><div><img src=\"cid:" + cid0 + "\" /></div></p>"
        		+ "<p><div><img src=\"cid:" + cid1 + "\" /></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2018SeasonBusinessEmailBody(ONCPartner o, String cid0, String cid1)
	{
		 //Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>With your continued and valued support, your <b>all-volunteer</b> team here at "
        		+ "<b>Our Neighbor's Child</b> is gearing up to coordinate "
        		+ "holiday assistance for children from low income families in western Fairfax County.</p>"
        		+ "<p>This neighbor-to-neighbor effort is only possible when our community "
        		+ "comes together and through the consistent, generous support of ONC partners like %s.</p>"
        		+ "<p>Hundreds of families in our area still struggle to meet their most basic needs. "
        		+ "When we help out with children's gifts during the holidays, it allows many of these "
        		+ "families to direct their stretched financial resources toward essential housing costs, "
        		+ "utilities and critically important food essentials.</p>"
        		+ "<p>We sincerely hope participating makes a difference in your life, and the lives of "
        		+ "others fortunate and generous enough to give.</p>"
        		+ "<p><b>Would you mind taking a moment to review our notes from last year? "
        		+ "Accurate information is KEY to our successful partnership:</b></p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2017:</font>   <b>%d</b><br>"
        		+ "<p><b>Please reply at your earliest convenience with any corrections, updates or questions.</b></p>"
        		+ "<p>Here are a few important dates: Gift wish \"ornaments\" will be delivered on <b>Wednesday, November 14</b>.</p>"
        		+ "<p>Gift drop-off dates will be:<br>"
        		+ "<b>Sunday, December 9, Noon to 2pm<br>"
        		+ "Monday, December 10, 3:30 - 6:30pm<br>"
        		+ "Tuesday, December 11, 3:30pm - 6:30pm</b>"
        		+ "<p>Gifts will be home delivered to each family on <b>Sunday, December 16</b>.</p>"
        		+"<p>We rely on donated warehouse space each year and will forward the (Chantilly area) "
        		+ "address as soon as it's confirmed.</p>"
        		+ "<p>I've included a few photos from prior seasons and hope you'll visit our website for more photos "
        		+ "and information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>. "
        		+ "We welcome you and anyone associated with your organization to join us in other "
        		+ "volunteer activities as well.</p>"
        		+ "<p>Though the number of families needing holiday assistance in our community has "
        		+ "grown exponentially in 26 years, ONC has continued as an ALL volunteer "
        		+ "organization with EVERY donation dollar going directly to gifts delivered to a child in need.</p>"
        		+ "<p>We are deeply grateful for your support and look forward to working with you "
        		+ "again this holiday season! Please feel free to email me with any questions or call 703-785-8048.</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Kathleen McDonald<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "703-785-8048<br>" 
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "<p><div><img src=\"cid:" + cid0 + "\" /></div></p>"
        		+ "<p><div><img src=\"cid:" + cid1 + "\" /></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2019SeasonChurchSchoolEmailBody(ONCPartner o, String cid0, String cid1)
	{	
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>With weather so warm, it's hard to imagine the holidays are just around the corner! "
        		+ "Our <b>all-volunteer</b> team at <b>Our Neighbor's Child</b> is gearing up to coordinate "
        		+ "a 28th season of holiday assistance for children from low income families in western "
        		+ "Fairfax County and we are grateful for your continued support.</p>"
        		+ "<p>This neighbor-to-neighbor effort is only possible when our community "
        		+ "comes together and through the consistent, generous support of ONC partners like "
        		+ "<font color=\"red\">%s</font>.</p>"
        		+ "<p>Many families in our area still struggle to meet their most basic needs. "
        		+ "When we help out with children's gifts during the holidays, it allows many of these "
        		+ "families to direct their stretched financial resources toward essential housing costs, "
        		+ "utilities and critically important food essentials.</p>"
        		+ "<p>We sincerely hope participating makes a difference in your life, and the lives of "
        		+ "others fortunate and generous enough to give.</p>"
        		+ "<p><b>Would you mind taking a moment to review our notes from last year? "
        		+ "Accurate information is KEY to our successful partnership:</b></p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2018:</font>   <b>%d</b><br>"
        		+ "<p><b>Please reply at your earliest convenience with any corrections, updates or questions - "
        		+ "<mark>especially if our point of contact has changed.</mark></b></p>"
        		+ "<p>Here are a few important dates: Gift wish \"ornaments\" will be delivered on <b>Wednesday, November 13</b>.</p>"
        		+ "<p>Gift drop-off dates will be:<br>"
        		+ "<b>Sunday, December 8, Noon to 2pm<br>"
        		+ "Monday, December 9, 3:30 - 6:30pm<br>"
        		+ "Tuesday, December 10, 3:30pm - 6:30pm</b>"
        		+ "<p>Gifts will be home delivered on <b>Sunday, December 15</b>.</p>"
        		+"<p><b>Important:</b> These dates are within one day of last year's dates, however, Thanksgiving "
        		+ "is nearly a week later (11/28). We hope you'll consider sharing ornaments with your donors a bit "
        		+ "earlier to ensure they'll have plenty of time to shop for and return their gifts (and perhaps "
        		+ "take advantage of \"Black Friday\" sales!).</p>"
        		+" <p>ONC relies on donated warehouse space each year and confirmation often arrives "
        		+ "in November. We will forward the (Chantilly area) address as soon as it's confirmed.</p>"
        		+ "<p>I've included a few photos from prior seasons and hope you'll visit our website for more photos "
        		+ "and information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>. "
        		+ "We welcome you and anyone associated with your organization to join us in other "
        		+ "volunteer activities as well.</p>"
        		+ "<p>Though the number of families needing holiday assistance in our community has "
        		+ "grown exponentially in 27 years, ONC is proud to continue as an ALL volunteer "
        		+ "organization that turns EVERY donation dollar directly to gifts provided to our local neighbors in need.</p>"
        		+ "<p>I will keep in contact with you or the current contact as our season of giving progresses. "
        		+ "Please feel free to contact me with any questions at 703-615-1934. "
        		+ "We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "703-615-1934<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "P.O. Box 276<br>"
        		+ "Centreville, VA  20120<br>" 
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "<p><div><img src=\"cid:" + cid0 + "\" /></div></p>"
        		+ "<p><div><img src=\"cid:" + cid1 + "\" /></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2019SeasonBusinessEmailBody(ONCPartner o, String cid0, String cid1)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>With weather so warm, it's hard to imagine the holidays are just around the corner! "
        		+ "Our <b>all-volunteer</b> team here at <b>Our Neighbor's Child</b> is gearing up to coordinate "
        		+ "a 28th season of holiday assistance for children from low income families in western "
        		+ "Fairfax County and we are grateful for your continued support!</p>"
        		+ "<p>This neighbor-to-neighbor effort is only possible when our community "
        		+ "comes together and through the consistent, generous support of ONC partners like "
        		+ "<font color=\"red\">%s</font>.</p>"
        		+ "<p>Hundreds of families in our area still struggle to meet their most basic needs. "
        		+ "When we help out with children's gifts during the holidays, it allows many of these "
        		+ "families to direct their stretched financial resources toward essential housing costs, "
        		+ "utilities and critically important food essentials.</p>"
        		+ "<p>We sincerely hope participating makes a difference in your life, and the lives of "
        		+ "others fortunate and generous enough to give.</p>"
        		+ "<p><b>Would you mind taking a moment to review our notes from last year? "
        		+ "Accurate information is KEY to our successful partnership:</b></p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2018:</font>   <b>%d</b><br>"
        		+ "<p><b>Please reply at your earliest convenience with any corrections, updates or questions - "
        		+ "<mark>especially if our point of contact has changed.</mark></b></p>"
        		+ "<p>Here are a few important dates: Gift wish \"ornaments\" will be delivered on <b>Wednesday, November 13</b>.</p>"
        		+ "<p>Gift drop-off dates will be:<br>"
        		+ "<b>Sunday, December 8, Noon to 2pm<br>"
        		+ "Monday, December 9, 3:30 - 6:30pm<br>"
        		+ "Tuesday, December 10, 3:30pm - 6:30pm</b>"
        		+ "<p>Gifts will be home delivered to each family on <b>Sunday, December 15</b>.</p>"
        		+"<p><b>Important:</b> These dates are within one day of last year's dates, however, Thanksgiving "
        		+ "is nearly a week later (11/28). We hope you'll consider sharing ornaments with your donors a bit "
        		+ "earlier to ensure they'll have plenty of time to shop for and return their gifts (and perhaps "
        		+ "take advantage of \"Black Friday\" sales!).</p>"
        		+" <p>We rely on donated warehouse space for our efforts each year and confirmation generally arrives "
        		+ "in November. We will forward the (Chantilly area) address as soon as it's confirmed.</p>"
        		+ "<p>I've included a few photos from prior seasons and hope you'll visit our website for more photos "
        		+ "and information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>. "
        		+ "We welcome you and anyone associated with your organization to join us in other "
        		+ "volunteer activities as well.</p>"
        		+ "<p>Though the number of families needing holiday assistance in our community has "
        		+ "grown exponentially in 27 years, ONC is proud to continue as an ALL volunteer "
        		+ "organization that turns EVERY donation dollar directly to gifts provided to the children of our local neighbors in need.</p>"
        		+ "<p>We are deeply grateful for your support and look forward to working with you "
        		+ "again this holiday season! Please feel free to email me with any questions or call 703-785-8048.</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Kathleen McDonald<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "703-785-8048<br>" 
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "<p><div><img src=\"cid:" + cid0 + "\" /></div></p>"
        		+ "<p><div><img src=\"cid:" + cid1 + "\" /></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2020SeasonSchoolEmailBody(ONCPartner o)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>What challenging times we are ALL facing!  We hope this email finds you, your loved ones, friends and co-workers safe and well.</p>"
        		+ "<p>In nearly 30 years there have been many contingency plans in place at Our Neighbor's Child. Not one of us imagined preparing for a global pandemic.</p>"
        		+ "<p>Amidst all the uncertainty, one thing was always clear: we'd find a way.  We felt certain our caring community of volunteers would agree that providing a sense of \"normalcy\" and holiday cheer to the children of our financially struggling neighbors would be even more important in this 2020 season.</p>"
        		+ "<p>So we've made a few changes. </p>"
        		+ "<p>Our traditional \"many hands\" effort involved hundreds of volunteers working side by side under one roof.  Social distancing standards will require us to work in smaller groups with an expanded timeline to ensure the safety of our volunteers.</p>"
        		+ "<p>Our planning and organizing has always been only part of this effort. Providing the gifts depends on the generous support of our community and dedicated partners like %s.</p>"
        		+ "<p>ONC volunteers have been working with (and greatly appreciating) our local school counselors and social workers. They're making it possible to have gift wishes available earlier this year: <b>November 11</b>.</p>" 
        		+ "<p><b>Gift Drop Off Dates</b> will need to be slightly earlier this year as well: <b>December 1, 2 and 3</b> with an extended time window of <b>9AM - 7PM</b> (by appointment to save waiting time and ensure social distancing).</p>"
        		+ "<p>Please help us by confirming and/or updating the information below:</p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2019:</font>   <b>%d</b><br>"
        		+ "<p>We are still able to provide our regular wish ornaments and collect physical gifts. We are also able to provide several virtual, no-contact options as well.  We'd love to find a plan that works best for you and maintain our valued partnership and your much needed support. Please just let me know at your earliest convenience.</p>"
        		+ "<p>We sincerely hope participating makes a difference and adds cheer to your life, and the lives of others fortunate and generous enough to give.</p>"
        		+ "<p>Families will receive their gifts at contact-free pickup locations on <b>Sunday, December 13</b>.</p>"
        		+ "<p><b>Important:</b></p>"
        		+ "<p>ONC relies on donated warehouse space each year and confirmation often arrives in November. We will forward the (Chantilly area) address as soon as it's confirmed.</p>"
        		+ "<p>We encourage you to visit our website for more information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>.</p>"
        		+ "<p>I will keep in contact with you (or the current contact) as our season of giving progresses. Please feel free to contact me with any questions by email or calling 703-615-1934. We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "703-615-1934<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "P.O. Box 276<br>"
        		+ "Centreville, VA 20120<br>"
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2020SeasonChurchEmailBody(ONCPartner o)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>What challenging times we are ALL facing!  We hope this email finds you, your loved ones, friends and co-workers safe and well.</p>"
        		+ "<p>In nearly 30 years there have been many contingency plans in place at Our Neighbor's Child. Not one of us imagined preparing for a global pandemic.</p>"
        		+ "<p>Amidst all the uncertainty, one thing was always clear: we'd find a way.  We felt certain our caring community of volunteers would agree that providing a sense of \"normalcy\" and holiday cheer to the children of our financially struggling neighbors would be even more important in this 2020 season.</p>"
        		+ "<p>So we've made a few changes. </p>"
        		+ "<p>Our traditional \"many hands\" effort involved hundreds of volunteers working side by side under one roof.  Social distancing standards will require us to work in smaller groups with an expanded timeline to ensure the safety of our volunteers.</p>"
        		+ "<p>Our planning and organizing has always been only part of this effort. Providing the gifts depends on the generous support of our community and dedicated partners like %s.</p>"
        		+ "<p>ONC volunteers have been working with (and greatly appreciating) our local school counselors and social workers. They're making it possible to have gift wishes available earlier this year: <b>November 11</b>.</p>" 
        		+ "<p><b>Gift Drop Off Dates</b> will need to be slightly earlier this year as well. <b>The Gift Drop Off date for churches will be Sunday, December 6th from 12 noon to 2PM.</b></p>"
        		+ "<p>Please help us by confirming and/or updating the information below:</p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		" &emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2019:</font>   <b>%d</b><br>"
        		+ "<p>We are still able to provide our regular wish ornaments and collect physical gifts. We are also able to provide several virtual, no-contact options as well.  We'd love to find a plan that works best for you and maintain our valued partnership and your much needed support. Please just let me know at your earliest convenience.</p>"
        		+ "<p>We sincerely hope participating makes a difference and adds cheer to your life, and the lives of others fortunate and generous enough to give.</p>"
        		+ "<p>Families will receive their gifts at contact-free pickup locations on <b>Sunday, December 13</b>.</p>"
        		+ "<p><b>Important:</b></p>"
        		+ "<p>ONC relies on donated warehouse space each year and confirmation often arrives in November. We will forward the (Chantilly area) address as soon as it's confirmed.</p>"
        		+ "<p>We encourage you to visit our website for more information about Our Neighbor's Child: <a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a>.</p>"
        		+ "<p>I will keep in contact with you (or the current contact) as our season of giving progresses. Please feel free to contact me with any questions by email or calling 703-615-1934. We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "703-615-1934<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "P.O. Box 276<br>"
        		+ "Centreville, VA 20120<br>"
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2021SeasonBusinessEmailBody(ONCPartner o)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>Thanks to the wonderful support of our community (in the midst of a worldwide pandemic!), our all volunteer team at Our Neighbor's Child (ONC) was able to successfully coordinate and distribute gifts to every child referred by local schools in 2020.</p>"
        		+ "<p>We are deeply grateful to you and all those who pulled together as we redesigned a safe and socially distant way to provide support for these struggling families. </p>"
        		+ "<p>As Covid uncertainty continues, last year's modifications will remain in place for ONC's 30th season of service.</p>"
        		+ "<p><b>IMPORTANT DATES:</b></p>"
        		+ "<ul>"
        		+ "<li><mark>The deadline for requesting children's wish ornaments is <b>Wednesday, November 10.</b></mark></li>"
        		+ "<li>Wishes will be distributed on <b>Wednesday, November 17th</b></li>"
        		+ "<li>Gift Drop Off Dates are <b>Wednesday, December 1 and Thursday, December 2</b> from 9AM to 6PM (Chantilly location TBD).</b></li>"
        		+ "</ul>"
        		+ "<p><b>WISH ORNAMENTS</b></p>"
        		+ "<p>You may choose to receive children's wishes in one of the following formats:</p>"
        		+ "<ul>"
        		+ "<li>Physical wish ornaments (construction paper ornaments with label attached)</li>"
        		+ "<li>PDF of wish labels via email attachment</li>"
        		+ "</ul>"
        		+ "<p><b>It is very important that a clearly printed label is attached to each gift so the bar code can be successfully scanned.</b> Ideally, labels are affixed to paper first and scotch taped to each package. This makes it easier if removal is necessary and doesn't damage the gift.</p>"
        		+ "<p>Accurate, up-to-date information is key to our efforts. Please assist us by confirming and/or updating the information below:</p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		"&emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2020:</font>   <b>%d</b>"
        		+ "<p>Families will receive their gifts at contact-free pickup locations on Sunday, December 12.</p>"
        		+ "<p>We appreciate your continued participation and look forward to your response <b>on or before November 10.</b>  I'll be sending the address and directions for the Gift Drop Off and other updates to all confirmed participants as details become available.</p>"
        		+ "<p>Please feel free to contact me with any questions.  We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Kathleen McDonald<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "703-785-8048<br>"
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2021SeasonChurchEmailBody(ONCPartner o)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>Thanks to the wonderful support of our community (in the midst of a worldwide pandemic!), our all volunteer team at Our Neighbor's Child (ONC) was able to successfully coordinate and distribute gifts to every child referred by local schools in 2020.</p>"
        		+ "<p>We are deeply grateful to you and all those who pulled together as we redesigned a safe and socially distant way to provide support for these struggling families. </p>"
        		+ "<p>As Covid uncertainty continues, last year's modifications will remain in place for ONC's 30th season of service.</p>"
        		+ "<p><b>IMPORTANT DATES:</b></p>"
        		+ "<ul>"
        		+ "<li><mark>The deadline for requesting children's wish ornaments is <b>Wednesday, November 10.</b></mark></li>"
        		+ "<li>Wishes will be distributed on <b>Wednesday, November 17th</b></li>"
        		+ "<li>Gift Drop Off is <b>Sunday, December 5</b> from 9AM to 1PM  at Rocky Run Middle School.</b></li>"
        		+ "</ul>"
        		+ "<p><b>WISH ORNAMENTS</b></p>"
        		+ "<p>You may choose to receive children's wishes in one of the following formats:</p>"
        		+ "<ul>"
        		+ "<li>Physical wish ornaments (construction paper ornaments with label attached)</li>"
        		+ "<li>PDF of wish labels via email attachment</li>"
        		+ "</ul>"
        		+ "<p><b>It is very important that a clearly printed label is attached to each gift so the bar code can be successfully scanned.</b> Ideally, labels are affixed to paper first and scotch taped to each package. This makes it easier if removal is necessary and doesn't damage the gift.</p>"
        		+ "<p>Accurate, up-to-date information is key to our efforts. Please assist us by confirming and/or updating the information below:</p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		"&emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2020:</font>   <b>%d</b>"
        		+ "<p>Families will receive their gifts at contact-free pickup locations on Sunday, December 12.</p>"
        		+ "<p>We appreciate your continued participation and look forward to your response <b>on or before November 10.</b>  I'll be sending updates and reminders to all confirmed participants as the season progresses.</p>"
        		+ "<p>Please feel free to contact me with any questions.  We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "(703) 615-1934 (cell)<br>"
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
        return msgtop + msgmid + msgbot;
	}
	
	String create2021SeasonSchoolEmailBody(ONCPartner o)
	{
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        int orn_requested = o.getPriorYearRequested();
//      String specNotes = o.getSpecialNotes();
//      int orn_receivedByDeadline = o.getPriorYearReceived();
        
//        String notes = "None";
//        if(o.getSpecialNotes().length() > 1)
//        	notes = o.getSpecialNotes();
        
        String msgtop = String.format("<html><body><div>"
        		+ "<p>Dear %s,</p>"
        		+ "<p>Thanks to the wonderful support of our community (in the midst of a worldwide pandemic!), our all volunteer team at Our Neighbor's Child (ONC) was able to successfully coordinate and distribute gifts to every child referred by local schools in 2020.</p>"
        		+ "<p>We are deeply grateful to you and all those who pulled together as we redesigned a safe and socially distant way to provide support for these struggling families. </p>"
        		+ "<p>As Covid uncertainty continues, last year's modifications will remain in place for ONC's 30th season of service.</p>"
        		+ "<p><b>IMPORTANT DATES:</b></p>"
        		+ "<ul>"
        		+ "<li><mark>The deadline for requesting children's wish ornaments is <b>Wednesday, November 10.</b></mark></li>"
        		+ "<li>Wishes will be distributed on <b>Wednesday, November 17th</b></li>"
        		+ "<li>Gift Drop Off Dates are <b>Wednesday, December 1 and Thursday, December 2</b> from 9AM to 6PM (Chantilly location TBD).</b></li>"
        		+ "<li>General Gift Collections may be dropped off on <b>Sunday, December 5 from 9AM to 1PM</b> at Rocky Run Middle School.</li>"
        		+ "</ul>"
        		+ "<p><b>WISH ORNAMENTS</b></p>"
        		+ "<p>You may choose to receive children's wishes in one of the following formats:</p>"
        		+ "<ul>"
        		+ "<li>Physical wish ornaments (construction paper ornaments with label attached)</li>"
        		+ "<li>PDF of wish labels via email attachment</li>"
        		+ "</ul>"
        		+ "<p><b>It is very important that a clearly printed label is attached to each gift so the bar code can be successfully scanned.</b> Ideally, labels are affixed to paper first and scotch taped to each package. This makes it easier if removal is necessary and doesn't damage the gift.</p>"
        		+ "<p>Accurate, up-to-date information is key to our efforts. Please assist us by confirming and/or updating the information below:</p>"
        		+ "&emsp;<font color=\"red\">ONC Gift Partner:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Address:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" 
        		+ "&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", fn, name, address, busphone, contact, contactphone, contactemail);
        
        //Create the middle part of the message if 2nd contact exists
        String msgmid = "";
        if(contact2.length() > MIN_NAME_LENGTH)
        {
        	msgmid = String.format(
        		"&emsp;<font color=\"red\">Contact:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Phone #:</font>   <b>%s</b><br>" +
        		"&emsp;<font color=\"red\">Email:</font>   <b>%s</b><br>", contact2, contact2phone, contact2email);
        }
        
        //Create the bottom part of the text part of the email using html
        String msgbot = String.format(
        		"&emsp;<font color=\"red\">Gift Collection Type:</font>  <b>%s</b><br>"
        		+ "&emsp;<font color=\"red\">Ornaments Requested in 2020:</font>   <b>%d</b>"
        		+ "<p>Families will receive their gifts at contact-free pickup locations on Sunday, December 12.</p>"
        		+ "<p>We appreciate your continued participation and look forward to your response <b>on or before November 10.</b>  I'll be sending the address and directions for the Gift Drop Off and other updates to all confirmed participants as details become available.</p>"
        		+ "<p>Please feel free to contact me with any questions.  We are deeply grateful for your support and look forward to working with you again this holiday season!</p>"
        		+ "<p>Sincerely,<br><br>"
        		+ "Diane Church<br>"
        		+ "Gift Partner Coordinator<br>"
        		+ "Our Neighbor's Child<br>"
        		+ "(703) 615-1934 (cell)<br>"
        		+ "<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a><br><br></div></p>"
        		+ "</body></html>", giftCollectionType, orn_requested);
        
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
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser user : userList)
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
	
	void onPrintBagLabels()
	{
		if(sortTable.getSelectedRowCount() > 0)	 //Print selected rows. If no rows selected, do nothing
		{
			PrinterJob pj = PrinterJob.getPrinterJob();

			pj.setPrintable(new AveryBagLabelPrinter());
         
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
        printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	void onPrintPartnerInfo()
	{
		//pop up a dialog with the table and print it
		JDialog infoDlg = new InfoDialog(parentFrame);
		infoDlg.setLocationRelativeTo(printCB);
//		infoDlg.setVisible(true);
	}
	
	void onExportContactGroup()
	{
		//get group name input from user. If null exit
		String groupName = (String) JOptionPane.showInputDialog(this, "Please enter Contact Group Name:",  
				"Contact Group Name", JOptionPane.QUESTION_MESSAGE, gvs.getImageIcon(0),
				null, "");
		
		if(groupName != null)
		{
			ONCFileChooser oncfc = new ONCFileChooser(this);
			File oncwritefile = oncfc.getFile("Select file for export of Agent Gmail Contact Group" ,
       							new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE, groupName);
			if(oncwritefile!= null)
			{
				//If user types a new filename without extension.csv, add it
				String filePath = oncwritefile.getPath();
				if(!filePath.toLowerCase().endsWith(".csv")) 
					oncwritefile = new File(filePath + ".csv");
	    	
				try 
				{
					CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
					writer.writeNext(ONCGmailContactEntity.getGmailContactCSVHeader());
	    	    
					int[] row_sel = sortTable.getSelectedRows();
					for(int i=0; i< sortTable.getSelectedRowCount(); i++)
					{
						int index = sortTable.convertRowIndexToModel(row_sel[i]);
						ONCPartner selPartner = stAL.get(index);
						if(selPartner.getContact_email().length() > 6)
							writer.writeNext(stAL.get(index).getGmailContactExportRow(1, groupName));
						if(selPartner.getContact2_email().length() > 6)
							writer.writeNext(stAL.get(index).getGmailContactExportRow(2, groupName));
					}
	    	   
					writer.close();
	    	    
					JOptionPane.showMessageDialog(this, 
							sortTable.getSelectedRowCount() + " partner contacts sucessfully exported to " + oncwritefile.getName(), 
							"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
				} 
				catch (IOException x)
				{
					JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
							"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					System.err.format("IOException: %s%n", x);
				}
			}
		}
	}
	
	void onExportRequested()
	{
		//Write the selected row data to a .csv file
		String[] header = {"Partner ID", "Name", " Partner Type", "Status", "Collection Type", "Other", 
							"Special Notes", "Orn Req.", "Orn Assign.", "Del To",
    						"Address", "Unit", "City", "Zip", "Phhone #", 
    						"1st Contact", "1st Contact Email", "1st Contact Phone",
    						"2nd Contact", "2nd Contact Email", "2nd Contact Phone",
    						"Date Changed"};
		
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
    	String fileDlgMssg = "Select File For Export of Partner Data"; 
    											
       	File oncwritefile = oncfc.getFile(fileDlgMssg, 
       			new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
						sortTable.getSelectedRowCount() + " partners info sucessfully exported to " + oncwritefile.getName(), 
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
	
	void onExportPerformanceRequested()
	{
		String[] header = {"Partner ID", "Name", " Partner Type", "Status", "Collection Type", 
					"Orn Request", "Orn Assigned", "Orn Delivered", "Gifts Received Before Deadline",
					"Gifts Received After Deadline"};
		
		
    	
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
    	String fileDlgMssg = "Select File For Export of Partner Performance Data"; 
    											
       	File oncwritefile = oncfc.getFile(fileDlgMssg, 
       			new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
	    	    	writer.writeNext(getExportPerformanceRow(row_sel[i]));
	    	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " partners info sucessfully exported to " + oncwritefile.getName(), 
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
	
	void onPartnerLetterExportRequested()
	{
		String[] header = {"First Name", "Name", "Address", "Business Phone", "Contact", 
					"Conatact Phone", "Contact Email", "Contact 2", "Contact 2 Phone",
					"Conatact 2 Email", "Gift Collectiion Type", "# of Prior Seasons Ornaments RequestedI"};
	
    	
    	ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
    	String fileDlgMssg = "Select File For Export of Partner Letter Data"; 
    											
       	File oncwritefile = oncfc.getFile(fileDlgMssg, 
       			new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
	    	    	writer.writeNext(getPartnerLetterExportRow(row_sel[i]));
	    	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " partners letter info sucessfully exported to " + oncwritefile.getName(), 
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
		ONCPartner o = stAL.get(index);
		
		SimpleDateFormat date = new SimpleDateFormat("MM-dd-yyyy");
		
		String[] row = {
						Long.toString(o.getID()),
						o.getLastName(),
						types[o.getType()],
						status[o.getStatus()+1],
						o.getGiftCollectionType().toString(),
						o.getOther(),
						o.getSpecialNotes(),
						Integer.toString(o.getNumberOfOrnamentsRequested()),
						Integer.toString(o.getNumberOfOrnamentsAssigned()),
						o.getDeliverTo(),
						o.getHouseNum() + " " + o.getStreet(),
						o.getUnit(),
						o.getCity(),
						o.getZipCode(),
						o.getHomePhone(),
						o.getContact(),
						o.getContact_email(),
						o.getContact_phone(),
						o.getContact2(),
						o.getContact2_email(),
						o.getContact2_phone(),
						date.format(o.getTimestampDate())};
		return row;
	}
	
	String[] getPartnerLetterExportRow(int index)
	{
		ONCPartner o = stAL.get(index);
		
		//Create the variables for the body of the email     
        String name = o.getLastName();
        
        //The next section of code is a temporary fix until the ONCPartner object is updated
        //to split the contact and contact2 name fields into contact fn, contact ln
        //contact2 fn and contact2 ln fields.
        String fn = "";
        if(o.getContact().length() > 1)
        {
        	 String[] names1 = o.getContact().split(" ");
        	if(names1.length == 1 || names1.length == 2)
        		fn = names1[0];
        	else
        		fn = names1[0] + " " + names1[1];
		}
        
        String fn2 = "";
        if(o.getContact2().length() > 1)
        {
        	String[] names2 = o.getContact2().split(" ");
        	if(names2.length == 1 || names2.length == 2)
        		fn2 = names2[0];
        	else
        		fn2 = names2[0] + " " + names2[1];
        }
        
        if(fn.length() <= 1 && fn2.length() > 1)	//contact 1 empty, contact2 exists
        	fn = fn2;
        else if(fn.length() > 1 && fn2.length() > 1)	//both contacts exist
        	fn = fn.concat(" & " + fn2);        
        //End of temporary code to handle contact name splitting
   
        String address = o.getHouseNum() + " " + o.getStreet() + " " + o.getUnit() + " " +
        				 o.getCity() + ", VA " + o.getZipCode();
        String contact = o.getContact();
        String busphone = o.getHomePhone();
       
        //Pick the 1st contact phone if it is valid, else try the organization phone number
        String contactphone = "";
        if(o.getContact_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getContact_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contactphone = o.getHomePhone();	
        String contactemail = o.getContact_email();
        
        //Pick the 2nd contact phone if it is valid, else try the organization phone number
        String contact2 = o.getContact2().trim();
        String contact2phone = "";
        if(o.getContact2_phone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getContact2_phone();
        else if(o.getHomePhone().length() >= MIN_PHONE_NUMBER_LENGTH)
        	contact2phone = o.getHomePhone();	
        String contact2email = o.getContact2_email();
        
        String giftCollectionType = o.getGiftCollectionType().toString();
        String orn_requested = Integer.toString(o.getPriorYearRequested());
		
		String[] row = {
						fn, name, address, busphone, contact, contactphone, contactemail,
						contact2, contact2phone, contact2email,
						giftCollectionType, orn_requested
						};
		return row;
	}
	
	String[] getExportPerformanceRow(int index)
	{
		ONCPartner o = stAL.get(index);
		
		String[] row = {
						Long.toString(o.getID()),
						o.getLastName(),
						types[o.getType()],
						status[o.getStatus()+1],
						o.getGiftCollectionType().toString(),
						Integer.toString(o.getNumberOfOrnamentsRequested()),
						Integer.toString(o.getNumberOfOrnamentsAssigned()),
						Integer.toString(o.getNumberOfOrnamentsDelivered()),
						Integer.toString(o.getNumberOfOrnamentsReceivedBeforeDeadline()),
						Integer.toString(o.getNumberOfOrnamentsReceivedAfterDeadline()),
						};
		return row;
	}
	
	boolean doesStatusMatch(int st) { return sortStatus == 0 || st == statusCB.getSelectedIndex()-1; }
	
	boolean doesTypeMatch(int ty) { return sortType == 0 || ty == typeCB.getSelectedIndex(); }
	
	boolean doesCollectionMatch(GiftCollectionType gc) { return sortCollection == GiftCollectionType.Any || gc == collectionCB.getSelectedItem(); }
	
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
	
	boolean doesStoplightMatch(int sl) { return sortStoplight == 0 || sl == stoplightCB.getSelectedIndex()-1; }
	
	@Override
	String[] getColumnToolTips()
	{
		String[] toolTips = {"ONC Partner", "Partner Status","Type of Organization",
				"Number of Ornaments Requested","Number of Ornaments Assigned", 
				"Number of ornaments given to partenr to fulfill", 
				"Number of gifts received from the partner before the deadline",
				"Number of gifts received from the partner after the deadline",
				"Gift Delivery Info for Partner","Date Partner Info Last Changed", 
				"ONC User that last changed partner info", "ONC Region that partner is located",
				"Partner Stop Light Color"};
		return toolTips;
	}
	@Override
	String[] getColumnNames()
	{
		String[] columns = {"Partner","Status", "Type", "Collection", "Req", "Assigned", "Del", "Rec", "Late", "Delivery Information",
				"Date Changed","Changed By","Reg", "SL"};
		return columns;
	}
	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {180, 96, 68, 68, 48, 56, 48, 48, 48, 180, 72, 80, 28, 24};
		return colWidths;
	}
	@Override
	int[] getCenteredColumns()
	{
		 int[] center_cols = {4, 5, 6, 7, 8, 12};
		return center_cols;
	}
	
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
		else if(e.getSource() == collectionCB && collectionCB.getSelectedItem() != sortCollection)
		{
			sortCollection = (GiftCollectionType) collectionCB.getSelectedItem();
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
			else if(printCB.getSelectedIndex() == 3) 
			{
				onPrintBagLabels();
			}
		}
		else if(e.getSource() == emailCB && emailCB.getSelectedIndex() > 0 && emailCB.getSelectedIndex() < 4)
		{
			createAndSendPartnerEmail(emailCB.getSelectedIndex());
			emailCB.setSelectedIndex(0);	//Reset the email combo choice
		}
		else if(e.getSource() == exportCB && exportCB.getSelectedIndex() > 0)
		{
			if(exportCB.getSelectedIndex() == 1)
			{
				onExportContactGroup();
			}
			else if(exportCB.getSelectedIndex() == 2)
			{
				onExportRequested();
			}
			else if(exportCB.getSelectedIndex() == 3)
			{
				onExportPerformanceRequested();
			}
			else if(exportCB.getSelectedIndex() == 4)
			{
				onPartnerLetterExportRequested();
			}
			
			exportCB.setSelectedIndex(0);
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
		
		collectionCB.removeActionListener(this);
		collectionCB.setSelectedItem(GiftCollectionType.Any);
		sortCollection = GiftCollectionType.Any; 
		collectionCB.addActionListener(this);
		
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
			fireEntitySelected(this, EntityType.PARTNER, stAL.get(sortTable.getSelectedRow()), null);
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
			   dbe.getType().equals("PARTNER_WISH_ASSIGNEE_CHANGED") ||
			    dbe.getType().equals("PARTNER_ORNAMENT_DELIVERED") ||
			     dbe.getType().equals("PARTNER_WISH_RECEIVED") ||
			      dbe.getType().equals("PARTNER_WISH_RECEIVE_UNDONE") ||
			       dbe.getType().equals("DELETED_PARTNER") ||
			        dbe.getType().equals("DELETED_CHILD")))
		{
			buildTableList(true);		
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject1();
			updateRegionList(regList);
		}
		else if(dbe.getType().contains("_USER"))
		{
			updateUserList();
		}
		else if(dbe.getType().equals("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Partner Management", gvs.getCurrentSeason()));
			updateUserList();
		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.PARTNER);
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
			for(ONCPartner o:stAL)	//Build the new table
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
	
	@Override
	void initializeFilters() {
		// TODO Auto-generated method stub
		
	}
	
	/*********************************************************************************************
	 * This class implements the Printable interface for printing ONC Book Labels on Avery 5164
	 * label sheets. It contains a method that knows how to print a book label. To print a label,
	 * the x, y position of the label on the sheet, the label content, the fonts to be used,
	 * the current season, the ONC icon and a Graphics2D object are passed. The 
	 * @author John O'Neill
	 ********************************************************************************************/
	private class AveryBagLabelPrinter implements Printable
	{
		private static final int AVERY_SHEET_X_OFFSET = 6;
		private static final int AVERY_SHEET_Y_OFFSET = 20;	//30
		private static final int AVERY_LABEL_X_COORDINATE_OFFSET = 18;	//Used for coordinate translation
		private static final int AVERY_LABEL_Y_COORDINATE_OFFSET = 36;
		private static final int AVERY_LABEL_Y_OFFSET = 0;	//Distance from to of label to 1st text
		private static final int AVERY_LABELS_PER_PAGE = 6;
		private static final int AVERY_COLUMNS_PER_PAGE = 2;
		private static final int AVERY_LABEL_HEIGHT = 239;
		private static final int AVERY_LABEL_WIDTH = 300;
		private static final int AVERY_LABEL_IMAGE_X_OFFSET = 180;	//210
		private static final int AVERY_LABEL_IMAGE_Y_OFFSET = 8;	//-18
		
		private static final int NUM_OF_XMAS_ICONS = 5;
		private static final int XMAS_ICON_OFFSET = 9;
		
		void printLabel(int x, int y, List<String> line, Font[] lFont, String season, Image img, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw ONC image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x+AVERY_LABEL_IMAGE_X_OFFSET, y + AVERY_LABEL_IMAGE_Y_OFFSET,
		    				x+AVERY_LABEL_IMAGE_X_OFFSET+destX1, y+AVERY_LABEL_IMAGE_Y_OFFSET+destY1,
		    				0,0, img.getWidth(null),img.getHeight(null),null); 
	         
		    //Draw the ONC Season
		    g2d.setFont(lFont[1]);
		    g2d.drawString("ONC " + season, x + AVERY_LABEL_IMAGE_X_OFFSET-2, y+58+AVERY_LABEL_IMAGE_Y_OFFSET);
		    
		    //Draw Partner Name
		    g2d.setFont(lFont[1]);
			g2d.drawString(line.get(0), x, y+AVERY_LABEL_Y_OFFSET); 	//Partner Name
			
			//Draw Partner ID #
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(1), x, y+AVERY_LABEL_Y_OFFSET+16); 
			
			//Draw Street Address 
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(2), x, y+AVERY_LABEL_Y_OFFSET+64);
			
			//Draw City, Zip
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(3), x, y+AVERY_LABEL_Y_OFFSET+80);
			
			//Contact Name
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(4), x, y+AVERY_LABEL_Y_OFFSET+136);
			
			//Partner Phone #
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(5), x, y+AVERY_LABEL_Y_OFFSET+156);
			
			//Contact Phone #
		    g2d.setFont(lFont[2]);
			g2d.drawString(line.get(6), x, y+AVERY_LABEL_Y_OFFSET+172);
			
			//# Ornaments Assigned
		    g2d.setFont(lFont[3]);
		    int numOrnamentsLength = line.get(7).length();
		    int offset = numOrnamentsLength == 2 ? 182 : 172;
			g2d.drawString(line.get(7), x+offset, y+AVERY_LABEL_Y_OFFSET+128);
			
			//# Ornaments Assigned Label
		    g2d.setFont(lFont[0]);
			g2d.drawString("Ornaments", x+178, y+AVERY_LABEL_Y_OFFSET+144);
		    
		    //For each child, draw the child line
//		    g2d.setFont(lFont[1]);
//		    for(int i=1; i<line.size(); i++)
//		    		g2d.drawString(line.get(i), x, i*AVERY_LABEL_CHILD_ROW_HEIGHT + y+AVERY_LABEL_Y_OFFSET);
		}

		@Override
		public int print(Graphics g, PageFormat pf, int page) throws PrinterException
		{
			if (page > (sortTable.getSelectedRowCount()+1)/AVERY_LABELS_PER_PAGE)		//'page' is zero-based 
			{ 
				return NO_SUCH_PAGE;
		    }
			
			int idx = gvs.getCurrentSeason() % NUM_OF_XMAS_ICONS;
			final Image img = gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();
		     
			Font[] lFont = new Font[4];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 12);
		    lFont[1] = new Font("Calibri", Font.BOLD, 14);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 12);
		    lFont[3] = new Font("Calibri", Font.BOLD, 44);	//ONC Number Text Font
		    
		    int endOfSelection = 0, index = 0;
		    int[] row_sel = sortTable.getSelectedRows();
	 		if(sortTable.getSelectedRowCount() > 0)
	 		{	//print a label for each row selected
	 			index = page * AVERY_LABELS_PER_PAGE;
	 			endOfSelection = row_sel.length;
	 		}
		    	 
		    // User (0,0) is typically outside the imageable area, so we must
		    //translate by the X and Y values in the PageFormat to avoid clipping
		    Graphics2D g2d = (Graphics2D)g;
		    g2d.translate(AVERY_LABEL_X_COORDINATE_OFFSET, AVERY_LABEL_Y_COORDINATE_OFFSET);
		    
		    int row = 0, col = 0;
		    
		    while(row < AVERY_LABELS_PER_PAGE/AVERY_COLUMNS_PER_PAGE && index < endOfSelection)
		    {
		    		//Get a reference to the selected family
		    		ONCPartner p = stAL.get(row_sel[index]);
		    	
		    		//Create a string array, one element for each child in the family
				List<String> line = new ArrayList<String>();
				
				line.add(p.getLastName());
				line.add(Integer.toString(p.getID()));
				line.add(String.format("%s %s", p.getHouseNum(), p.getStreet()));
				line.add(String.format("%s, %s", p.getCity(), p.getZipCode()));
				line.add(p.getContact());
				line.add(p.getHomePhone());
				line.add(p.getContact_phone());
				line.add(Integer.toString(p.getNumberOfOrnamentsAssigned()));
				
				//only print a label if there is qualifying information
				if(line.size() > 1)	//more than just the ID number
				{	
					printLabel(col * AVERY_LABEL_WIDTH + AVERY_SHEET_X_OFFSET,
		    				row * AVERY_LABEL_HEIGHT + AVERY_SHEET_Y_OFFSET,
		    				line, lFont, String.format("%d", gvs.getCurrentSeason()), img, g2d);	
		    	
					if(++col == AVERY_COLUMNS_PER_PAGE) { row++; col = 0; }
				}
		    	
				index++; //Increment the total number of family book labels processed
		    }
		    	    
		     /* tell the caller that this page is part of the printed document */
		     return PAGE_EXISTS;
		}
	}		
}
