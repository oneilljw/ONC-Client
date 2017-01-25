package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVWriter;


public class SortAgentDialog extends DependantTableDialog implements PropertyChangeListener
															
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String AGENT_EMAIL_SENDER_ADDRESS = "schoolcontact@ourneighborschild.org";
//	private static final String TEST_AGENT_EMAIL_SENDER_ADDRESS = "johnwoneill1@gmail.com";
	private static final int MIN_EMAIL_ADDRESS_LENGTH = 2;
	private static final int MIN_EMAIL_NAME_LENGTH = 1;
	
	private JComboBox orgCB, titleCB;
	private DefaultComboBoxModel orgCBM, titleCBM;
	private String sortOrg, sortTitle;
	private JCheckBox allAgentsCxBox;

	private JButton btnEditAgentInfo;
	private JComboBox printCB, emailCB;
	
	private JProgressBar progressBar;
	private ONCEmailer oncEmailer;

	private UserDB userDB;
	private List<ONCUser> atAL;	//Holds references to agent objects for agent table
	
	private AgentInfoDialog aiDlg;
	
	SortAgentDialog(JFrame pf)
	{
		super(pf, 10);
		this.setTitle("Our Neighbor's Child - Agent Management");

		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//Set up the agent table content array list
		atAL = new ArrayList<ONCUser>();
		
		//Initialize the sort criteria variables
		sortOrg = "Any";
		sortTitle = "Any";
		
		//Set up the search criteria panel      
		orgCB = new JComboBox();
		orgCBM = new DefaultComboBoxModel();
	    orgCBM.addElement("Any");
	    orgCB.setModel(orgCBM);
		orgCB.setPreferredSize(new Dimension(144, 56));
		orgCB.setBorder(BorderFactory.createTitledBorder("Organization"));
		orgCB.addActionListener(this);
		
		titleCB = new JComboBox();
		titleCBM = new DefaultComboBoxModel();
	    titleCBM.addElement("Any");
	    titleCB.setModel(titleCBM);
		titleCB.setPreferredSize(new Dimension(144, 56));
		titleCB.setBorder(BorderFactory.createTitledBorder("Title"));
		titleCB.addActionListener(this);
		
		//Add all sort criteria components to dialog pane
        sortCriteriaPanelTop.add(orgCB);
		sortCriteriaPanelTop.add(titleCB);
		sortCriteriaPanelTop.add(new JPanel());
		
		//Set the text for the agent count label
		lblObjectMssg.setText("# of Agents:");
		
		//set up an info panel for the all agents text box
		JPanel infoPanel = new JPanel();
    
		allAgentsCxBox = new JCheckBox("Show All Agents:");
		allAgentsCxBox.setSelected(false);
		allAgentsCxBox.addActionListener(this);
		
		infoPanel.add(allAgentsCxBox);
      
		//set up a control panel for buttons
		JPanel cntlPanel = new JPanel();
      	cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
      	//Set up the email progress bar
      	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        String[] emailChoices = {"Email", "2016 Season Gift Confirmation Email"};
        emailCB = new JComboBox(emailChoices);
        emailCB.setPreferredSize(new Dimension(136, 28));
        emailCB.setEnabled(false);
        emailCB.addActionListener(this);
              
      	//Create a print button for agent information
      	String[] agentPrintChoices = {"Print", "Print Agent Listing"};
        printCB = new JComboBox(agentPrintChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
        
      	//Create the middle control panel buttons
      	btnEditAgentInfo = new JButton("Edit Agent Info");
      	btnEditAgentInfo.setEnabled(false);
      	btnEditAgentInfo.addActionListener(this);
         
        cntlPanel.add(objectCountPanel);
        cntlPanel.add(progressBar);
        cntlPanel.add(emailCB);
        cntlPanel.add(printCB);
        cntlPanel.add(btnEditAgentInfo);
        
        //set the border title for the family table 
        familyTableScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(), "Families Represented By Selected Agent(s)"));

        //set up the Agent Info Dialog and register it for just Entity Selection Events from this
        //dialog, not from any other. To do that, don't use the Entity Event Manager registration for
        //global entity selection event registration
    	aiDlg = new AgentInfoDialog(GlobalVariables.getFrame(), true);
    	this.addEntitySelectionListener(EntityType.AGENT, aiDlg);
        
    	bottomPanel.add(infoPanel, BorderLayout.LINE_START);
    	bottomPanel.add(cntlPanel, BorderLayout.CENTER);
    	
        this.add(bottomPanel);
        this.add(familyTableScrollPane);
        this.add(lowercntlpanel);
        
        pack();
    }

	void buildFamilyTableListAndDisplay()
	{
		int[] row_sel = sortTable.getSelectedRows();
		
		stAL.clear();	//Clear the prior table data array list
		clearFamilyTable();
		
		for(int i=0; i< row_sel.length; i++)
			for(ONCFamily f:fDB.getList())
				if(f.getAgentID() == atAL.get(row_sel[i]).getID())
					stAL.add(f);
					
		lblNumOfFamilies.setText(Integer.toString(stAL.size()));
		displayFamilyTable();		//Display the table after table array list is built					
	}
	
	@Override
	void buildTableList(boolean bPreserveSelections) 
	{
		//archive the table rows selected prior to rebuild so the can be reselected if the
		//build occurred due to an external modification of the table
		tableRowSelectedObjectList.clear();
		if(bPreserveSelections)
			archiveTableSelections(atAL);
		else
			tableSortCol = -1;
		
		atAL.clear();	//Clear the prior table data array list
		stAL.clear();
		
		clearFamilyTable();
		familyTable.clearSelection();
		
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser u : userList)
			if(u.getPermission().compareTo(UserPermission.Agent) >= 0 && 
			    doesOrgMatch(u.getOrg()) &&
				 doesTitleMatch(u.getTitle()) &&
				  didAgentRefer(u))
				atAL.add(u);
		
		if(allAgentsCxBox.isSelected())
			lblObjectMssg.setText("Referring Agents in DB:");
		else
			lblObjectMssg.setText(String.format("%d Referring Agents: ", GlobalVariables.getCurrentSeason()));
		
		lblNumOfObjects.setText(Integer.toString(atAL.size()));
		displaySortTable(atAL, true, tableRowSelectedObjectList);
	}
	
	void updateOrgCBList()
	{
		bIgnoreCBEvents = true;
		
		ArrayList<String> orgItemAL = new ArrayList<String>();
		
		orgCBM.removeAllElements();
		
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser u : userList)
			if(u.getPermission().compareTo(UserPermission.Agent) >= 0 && !u.getOrg().trim().isEmpty())
			{
				int index = 0;
				while(index < orgItemAL.size() && !u.getOrg().equals(orgItemAL.get(index)))
					index++;
				
				if(index == orgItemAL.size())
					orgItemAL.add(u.getOrg());	
			}
		
		Collections.sort(orgItemAL);
		orgCBM.addElement("Any");
		for(String s:orgItemAL)
			orgCBM.addElement(s);
		
		bIgnoreCBEvents = false;
	}
	
	void updateTitleCBList()
	{
		bIgnoreCBEvents = true;
		
		ArrayList<String> titleItemAL = new ArrayList<String>();
		
		titleCBM.removeAllElements();
		
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		for(ONCUser u : userList)
		{
			if(u.getPermission().compareTo(UserPermission.Agent) >= 0 && !u.getTitle().trim().isEmpty())
			{
				int index = 0;
				while(index < titleItemAL.size() && !u.getTitle().equals(titleItemAL.get(index)))
					index++;
			
				if(index == titleItemAL.size())
					titleItemAL.add(u.getTitle());
			}
		}
		
		Collections.sort(titleItemAL);
		titleCBM.addElement("Any");
		for(String s:titleItemAL)
			titleCBM.addElement(s);
		
		bIgnoreCBEvents = false;
	}
	
	void onExportDependantTableRequested()
	{
		//Write the selected row data to a .csv file
    	String[] header = {"Agent Name", "ONC #", "Batch #", "DNS", "Family Status", "Delivery Status",
    						"Meal Status", "First Name", "Last Name", "House #", "Street",
    						"Unit", "Zip", "Region", "Changed By"};
    	
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
	    	    
	    	    int[] row_sel = familyTable.getSelectedRows();
	    	    for(int i=0; i<familyTable.getSelectedRowCount(); i++)
	    	    	writer.writeNext(getDependantTableExportRow(row_sel[i]));
	    	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " families sucessfully exported to " + oncwritefile.getName(), 
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
	
	String[] getDependantTableExportRow(int index)
	{
		ONCFamily f = stAL.get(index);
		ONCUser u = userDB.getUser(f.getAgentID());
		
		String[] row = {
						u.getFirstname() + " " + u.getLastname(),
						f.getONCNum(),
						f.getBatchNum(),
						f.getDNSCode(),
						f.getFamilyStatus().toString(),
						f.getGiftStatus().toString(),
						f.getMealStatus().toString(),
						f.getHOHFirstName(),
						f.getHOHLastName(),
						f.getHouseNum(),
						f.getStreet(),
						f.getUnitNum(),
						f.getZipCode(),
						regions.getRegionID(f.getRegion()),
						f.getChangedBy()
						};
		return row;
	}

	void checkPrintandEmailEnabled()
	{
		if(familyTable.getSelectedRowCount() > 0)
		{
			btnDependantTableExport.setEnabled(true);
			famPrintCB.setEnabled(true);
		}
		else
		{
			btnDependantTableExport.setEnabled(false);
			famPrintCB.setEnabled(false);
		}
		
		if(sortTable.getSelectedRowCount() > 0)
			printCB.setEnabled(true);
		else
			printCB.setEnabled(false);
		
		if(sortTable.getSelectedRowCount() > 0 &&
			userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 )
		{
			emailCB.setEnabled(true);
		}
		else
			emailCB.setEnabled(false);				
	}
	
	void createAndSendAgentEmail(int emailType)
	{
		//build the email
		ArrayList<ONCEmail> emailAL = new ArrayList<ONCEmail>();
		ArrayList<ONCEmailAttachment> attachmentAL = new ArrayList<ONCEmailAttachment>();
		String cid0 = null, cid1 = null;
		String subject = null;
		
		//Create the subject and attachment array list
//		if(emailType == 1)
//		{
//			subject = "2015 Holiday Information From Our Neighbor's Child";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("DSC_0154.jpeg", cid0 , MimeBodyPart.INLINE));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
//		}
//		else if(emailType == 2)//Reminder email
//		{
//			subject = "Important Reminder from Our Neighbor's Child";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("DSC_0154.jpeg", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
//		}
		if(emailType == 1) //Delivery Status Email
		{
			subject = "Delivery Status Update from Our Neighbor's Child";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("ONC Family Referral Worksheet.xlsx", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
//		else if(emailType == 2) //December Gift Confirmation Email
//		{
//			subject = "December Gift Confirmations";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("ONC Family Referral Worksheet.xlsx", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
//		}
		
		//For each agent selected, create the email body and recipient information in an
		//ONCEmail object and add it to the email array list
		int[] row_sel = sortTable.getSelectedRows();
		for(int row=0; row< sortTable.getSelectedRowCount(); row++)
		{
			//Get selected agent object
			ONCUser user = atAL.get(row_sel[row]);
			
			//Create the email body if the agent exists
			String emailBody = createEmailBody(emailType, user, cid0, cid1);
			
	        //Create recipient list for email.
	        ArrayList<EmailAddress> recipientAdressList = createRecipientList(user);
	        
	        //If the agent email isn't valid, the message will not be sent.
	        if(emailBody != null && !recipientAdressList.isEmpty())
	        	emailAL.add(new ONCEmail(subject, emailBody, recipientAdressList));     	
		}
		
		//Create the from address string array
		EmailAddress fromAddress = new EmailAddress(AGENT_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child");
//		EmailAddress fromAddress = new EmailAddress(TEST_AGENT_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child");
		
		//Create the blind carbon copy list 
		ArrayList<EmailAddress> bccList = new ArrayList<EmailAddress>();
		bccList.add(new EmailAddress(AGENT_EMAIL_SENDER_ADDRESS, "School Contact"));
//		bccList.add(new EmailAddress("kellylavin1@gmail.com", "Kelly Lavin"));
//		bccList.add(new EmailAddress("mnrogers123@msn.com", "Nicole Rogers"));
//		bccList.add(new EmailAddress("johnwoneill@cox.net", "John O'Neill"));
		
		//Create mail server accreditation, then the mailer background task and execute it
		//Go Daddy Mail
//		ServerCredentials creds = new ServerCredentials("smtpout.secureserver.net", "director@act4others.org", "crazyelf1");
		//Google Mail
		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", AGENT_EMAIL_SENDER_ADDRESS, "crazyelf");
//		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", TEST_AGENT_EMAIL_SENDER_ADDRESS, "erin1992");
		
	    oncEmailer = new ONCEmailer(this, progressBar, fromAddress, bccList, emailAL, attachmentAL, creds);
	    oncEmailer.addPropertyChangeListener(this);
	    oncEmailer.execute();
	    emailCB.setEnabled(false);		
	}
	
	/**************************************************************************************************
	 *Creates a new email body each agent email. If agent is valid or doesn't have a valid first name
	 *a null body is returned
	 **************************************************************************************************/
	String createEmailBody(int emailType, ONCUser user, String cid0, String cid1)
	{
		String emailBody = null;
		
		//verify the agent has a valid name. If not, return an empty list
		if(user != null && user.getFirstname() != null && user.getFirstname().length() > MIN_EMAIL_NAME_LENGTH)
//			emailBody = createAgentEmailText(agent, cid0, cid1); 	//2013 Email Body
//			if(emailType == 1)
//				emailBody = create2015AgentEmailText(agent.getAgentFirstName());	//2015 email body
//			else if(emailType == 2)
//				emailBody = create2014AgentReminderEmail(agent.getAgentFirstName());
//			else if(emailType == 3)
//				emailBody = create2014AgentIntakeEmail(agent.getAgentFirstName(), cid0);
			if(emailType == 1)
				emailBody = create2016AgentDecemberGiftConfirmationEmail(user);
//			else if(emailType == 3)
//				emailBody = create2015AgentDeliveryStatusEmail(agent);
		return emailBody;
	}
	
	
	/**************************************************************************************************
	 *Creates a new list of recipients for each agent email. For agents, there is only one recipient per
	 *email. For other ONC email recipients, there may be two or more recipients for an email
	 *If the agent does not have a valid email or name, an empty list is returned
	 **************************************************************************************************/
	ArrayList<EmailAddress> createRecipientList(ONCUser user)
	{
		ArrayList<EmailAddress> recipientAddressList = new ArrayList<EmailAddress>();
		
		//verify the agent has a valid email address and name. If not, return an empty list
		if(user != null && user.getEmail() != null && user.getEmail().length() > MIN_EMAIL_ADDRESS_LENGTH &&
				user.getLastname() != null && user.getLastname().trim().length() > MIN_EMAIL_NAME_LENGTH)
        {
			//LIVE EMAIL ADDRESS
			EmailAddress toAddress = new EmailAddress(user.getEmail(), user.getLastname());	//live
			recipientAddressList.add(toAddress);

			//TEST EMAIL ADDRESS
//			EmailAddress toAddress1 = new EmailAddress("johnwoneill1@gmail.com", "John O'Neill");	//test
//        	recipientAddressList.add(toAddress1);      	
        }
		
		return recipientAddressList;
	}
	
	String createAgentEmailText(ONCUser user, String cid1, String cid2)
	{
        //Create the text part of the email using html
        String msg = "<html>" +
        		"<body><div>" +
        		"<p><font color=\"red\"><b>" +
        		"Zip Code information corrected. Please delete earlier copy." +
        		"</b></font></p>"+
        		"<p><b>Warmest Greetings to FCPS Counselors, Social Workers and " +
        		"other Holiday Assistance Referring Agents;</b></p>" +
        		"<p>This is just a quick note from <b>Our Neighbors Child (ONC)</b> to be " +
        		"sure you've received the <b>2013 Holiday Program Instructions and Guidelines</b> " +
        		"distributed by Our Daily Bread (ODB).  Please feel free to forward this message if there " +
        		"are new referring agents in your school or office.</p>" +
        		"<p>For clarification:</p>" +
        		"<p><b>Our Daily Bread (ODB)</b> is the organization contracted to serve as the " +
        		"County's \"central clearinghouse\" for Holiday Assistance Referrals.  Once received, " +
        		"the referrals are redirected to local Community Based Organizations (as available) " +
        		"who will provide the holiday food and/or gifts for your families.</p>" +
        		"<p><b>Our Neighbor's " +
        		"Child (ONC)</b> is the local, community-based organization that provides " +
        		"<b>Holiday gifts</b> in Centreville, Chantilly " +
        		"and western Fairfax (<b>20120, 20121, 22033, 20151, 20150, 22039 and 20124</b>).  " +
        		"<b>Holiday Food</b> will come from another local organization.</p>" +
        		"<p>We are pleased to have <b>Stephanie Somers</b> return as <b>ONC's Referring Agent " +
        		"Liaison</b>.  We're an " +
        		"all-volunteer organization and Stephanie is happy to help with any questions about " +
        		"the referral process and gifts ONC will provide.  All replies to this e-mail will " +
        		"go directly to Stephanie.</p>" +
        		"<p><b><font color=\"red\">Reminder: Holiday Gift referrals must be received by ODB's deadline of " +
        		"November 15th.</font></b>" +
        		"</p><p>We know how much you care about the children you refer. Here's what will really help:</p>" +
        		"<p><b>Accuracy is key.</b>  ONC (and volunteers throughout our community) will be " +
        		"serving more than 750 local families again this year (our 22nd).  " +
        		"We're only able to do this through " +
        		"partnerships with local schools, churches, businesses and individuals who generously " +
        		"donate the gifts.  Our ability to deliver correct gifts to each child at their " +
        		"correct address is only possible with your careful efforts.</p>" +
        		"<p><b>The best way to " +
        		"ensure that a child you refer will receive gifts they'd most enjoy is to list " +
        		"their wishes (up to three) in their order of importance.</b>  If it's a clothing wish, " +
        		"please include information on size and even color (if there's a preference).  " +
        		"If a parent is uncertain about what their child would like, list \"age appropriate\"" +
        		"or \"educational toy\".  If you know specific information about the child that may " +
        		"help in selecting a suitable gift, please add that information in \"comments\". " +
        		"Information such as \"likes arts and crafts\" or \"loves horses\" can be particularly helpful.</p>" +
        		"<p>If a child's wish is a bike, please include the child's height and a color preference (if any). " +
        		"When we are able to provide bikes, we always include a helmet.</p>" +
        		"<p>The families we serve will receive home delivery of their gifts on " +
        		"<font color=\"red\"><b>Sunday, December 15th, between 1PM and 4PM.</font></b></p>" +
        		"<p><b>Please remind your families that they can sign up with only one " +
        		"organization</b>.  The Salvation Army now has a Fairfax location and accepts in-person sign-ups " +
        		"for children up to age 12.  These lists are shared to avoid duplication of services.  " +
        		"Any family on the Salvation Army list will be subsequently removed from the Our Neighbor's Child list.  " +
        		"(Please note that ONC serves children through their senior year in high school).</p>" +
        		"<p><font color=\"red\"><b>" +
        		"ONC introduced a new automated calling system in 2012 in an effort to provide more " +
        		"timely communication with each family.</b>  This interactive system " +
        		"(generously donated by Angel/Genesys) allows families to " +
        		"choose their message in <b>English or Spanish</b>.  Please tell the families you refer that the " +
        		"automated calls " +
        		"will begin the week after Thanksgiving.</font></p>" +
        		"<p>We know you're all very busy and we appreciate your efforts on behalf of each child. We hope " +
        		"you'll enjoy these photos from last season. The first is of students from " +
        		"three local high schools (Centreville, Westfield and Chantilly) who, like you, " +
        		"play an important role in serving these families! The second is a view of our donated " +
        		"warehouse space. While the spaces are numbered to protect each family's privacy, " +
        		"the children are not numbers to us.  Our volunteers spend months in careful planning " +
        		"to ensure that EVERY child receives the thoughtful selection of gifts that will " +
        		"help make their holidays bright.</p>" +
        		"<p>Best wishes for your own happy holidays!</p>" +
        		"<p>Sincerely,</p></div>" +
//       		"<div id=\"wrap\">" +
//       		"<div id=\"left_col\">Kelly<br><br>Kelly Murray Lavin<br>Executive Director</div>" +
//        		"<div id=\"right_col\">Stephanie<br><br>Stephanie Somers<br>Referral Agent Liason<br>(703) XXX-XXXX (Referring Agents only please)</div></div>" +
        		"<table width=\"70%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
        	    "<tr>" +
        	        "<td width=\"40%\" valign=\"top\">" +
        	            "<p><b><i>Kelly</i></b><br><br>Kelly Murray Lavin<br>Founder/Executive Director</p>" +
        	        "</td>" +
        	        "<td width=\"60%\" valign=\"top\">" +
    	            "<p><b><i>Stephanie</b></i><br><br>Stephanie Somers<br>Referral Agent Liason</p>" +
    	            "</td>" +
        	    "</tr>" +
        	    "</table>" +	
        		"<p>Our Neighbor's Child<br>P.O. Box 276<br>Centreville, VA  20120<br>" +
        	    "<a href=\"http://www.ourneighborschild.org\">www.ourneighborshild.org</a></p></div>" +
        		"<div><p><img src=\"cid:" + cid1 + "\" /></p></div>" +
        		"<div><p><img src=\"cid:" + cid2 + "\" /></p></div>" +
        		"</body></html>";
        return msg;
	}
	
	String create2015AgentEmailText(String recipientFirstName)
	{
        //Create the text part of the email using html
        String msg = String.format(
        	"<html><body><div>" +
     //   	"<p><b>Hello FCPS Counselors, Social Workers and other Holiday Assistance Referring Agents!!</p></b>"+
        	"<p>Hello %s,</p>"+
        	"<p>With all this warm (and wet) weather, it's hard to believe the holidays are just "
        	+ "around the corner!</p>"
        	+ "<p>We hope it's a comfort to know that our all-volunteer team at Our Neighbor's Child (ONC) "
        	+ "has committed to a 24th year of coordinating gift giving efforts for children from "
        	+ "low-income families from your shcool.</p>"
        	+ "<p>ONC is the local, community-based organization that provides Holiday Gifts in "
        	+ "December for children living in Centreville, Chantilly and western " +
        	" Fairfax (20120, 20121, 20124, 20150, 20151, 22033 and 22039).</p>"
        	+ "<p>By now you have received instructions from Our Daily Bread (ODB) on how you can "
        	+ "submit referrals. They are the organization contracted to collect all the "
        	+ "information countywide and they re-direct the requests to organizations "
        	+ "(like ONC) who serve that area.</p>"
        	+ "<p><b>Please remind your families that they can sign up with only one organization</b>. "
        	+ "Families who elect to sign up with the Salvation Army or other groups will be "
        	+ "removed from the ONC list. (It is important to note that the Salvation Army only "
        	+ "serves children up to age 12.  ONC will serve children through age 17 or high school "
        	+ "graduation).</p>"
        	+ "<p><b>Thanksgiving Referrals are due by October 30th.</b></p>"
        	+ "<p><b>December Referrals are due by November 20th.</b></p>"
        	+ "<p><font color=\"red\"><b>Important: ODB will stop taking referrals once they reach 3,000. Last "
        	+ "year this occurred in <b><i>October</i></b>, several days before the Thanksgiving deadline, so be "
        	+ "sure to send your referrals as early as possible!</b></font></p>"
        	+ "<p>We know how much you care about the children you refer. Please help us in our efforts "
        	+ "by remembering that:</p>"
        	+ "<p><b>Accuracy is key</b>. ONC (and volunteers throughout our community) will be serving more than "
        	+ "750 local families again this year. We're only able to do this through partnership with local "
        	+ "schools, churches, businesses and individuals who generously donate the gifts. Our ability to "
        	+ "deliver the correct gifts to the child at their correct address is only possible with your "
        	+ "careful efforts.</p>"
        	+ "<p><b>Give DETAILS!!! The more, the better!!!</b> When it comes to shopping for the "
        	+ "perfect gift, the wonderful folks "
        	+ "who \"adopt\" these families really do appreciate all the specifics and guidance only "
        	+ "you can provide! The best way to ensure that the child you refer will "
        	+ "receive the gift they'd most enjoy is to list their wishes in the order of importance. "
        	+ "If it's a coat, let us know if they want a hoodie, a rain coat, a warm winter coat, etc.  "
        	+ "If a parent is uncertain about what their child would like, list \"age appropriate\" or \"educational toy\". "
        	+ "If you know specific information about the child that may help in selecting a suitable gift, "
        	+ "please add that information in the \"comments\". Information such as \"likes arts and crafts\" or "
        	+ "\"loves horses\" can be particularly helpful.  If a child's wish is a bike, please include the child's "
        	+ "height and a color preference (if any). When we are able to provide bikes, we always include a helmet.</p>"
        	+ "<p>An ONC volunteer will deliver gifts to each child's home on<b> Sunday, December 13th, between 1:00pm and 4:00pm.</b></p>"
        	+ "<p><b>Families will be notified by ONC's automated calling system</b> the week after Thanksgiving. This interactive "
        	+ "system allows families to choose their message in <b>English</b> or <b>Spanish</b> and there is an opportunity to submit changes "
        	+ "to phone number or address information.</p>"
        	+"<p>We know you're all very busy and we appreciate everything you do each and every day.  Thank you "
        	+ "for helping us make sure that EVERY child receives the thoughtful selection of gifts that will help make their holidays bright!</p>"
        	+ "<p>I'm so excited for this, our 24th season!  Should you have any questions, please don't hesitate to shoot"
        	+ " me an e-mail.  Also, if you have referring agents who are new this year, PLEASE let me know so that "
        	+ "I can add their names and contact information to our database!  And feel free to forward them this "
        	+ "email so that they have my contact information as well.</p>"
        	+ "<p>All the best,</p>"
        	+"<p>Stephanie Somers<br>"
        	+"ONC Referring Agent Liaison<br>"
        	+"Our Neighbor's Child<br>"
        	+"P.O. Box 276<br>"
        	+"Centreville, VA 20120<br>"
        	+"<a href=\"http://www.ourneighborschild.org\">www.ourneighborshild.org</a></p>" 
        	+"</div></body></html>", recipientFirstName);
        return msg;
	}
	
	String create2014AgentReminderEmail(String recipientFirstName)
	{
        //Create the text part of the email using html
        String msg = String.format(
        	"<html><body><div>" +
        	"<p><b>Hi again, %s!!</b></p>"+
        	"<p>Just wanted to send you a quick reminder that TODAY is the deadline for registering families for " +
        	"Thanksgiving assistance through Our Daily Bread (ODB).  It would be terrific if you could register your " +
        	"families for their Holiday Gifts at the same time.</p>" +
        	"<p>While the ODB deadline for Holiday Gift referrals is not until November 14th, the earlier we " +
        	"receive the information, the better we are able to meet all the needs and gift requests.  Plus, " +
        	"ODB has set a limit on the number of families they will allow to be referred this year.  Getting " +
        	"all of your families in as soon as possible is really important.</p>" +
        	"<p>Thanks so much and I hope everyone is doing well!</p>" +
        	"<p>All the best,</p>" +
        	"<p>Stephanie Somers<br>" +
        	"ONC Referring Agent Liaison<br>" +
        	"Our Neighbor's Child<br>" +
        	"P.O. Box 276<br>" +
        	"Centreville, VA 20120<br>" +
        	"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p>" +
        	"</div></body></html>", recipientFirstName);
        return msg;
	}
	
	String create2014AgentIntakeEmail(String recipientFirstName, String cid1)
	{
        //Create the text part of the email using html
        String msg = String.format(
        	"<html><body><div>" +
        	"<p><b>Dear %s,</b></p>"+
        	"<p>We understand that Our Daily Bread (ODB) has closed their intake for holiday assistance.</p>" +
        	"<p>Our Neighbor's Child (ONC) is still able to accept referrals for clients in our zip code " +
        	"serving area:  20120, 20121, 22033, 20151, 20152, 22039 and 22124.</p>" +
        	"<p>While we are not set up for \"intake\" at this time, we have created an emergency intake form " +
        	"(attached) that will enable us to integrate your family information into our system.</p>" +
        	"<p>If you will fill in the blanks with complete and accurate information on your families, we " +
        	"will ensure that they receive holiday gifts for their children.  If they are requesting " +
        	"assistance with food, we will pass this information on to Western Fairfax Christian Ministries " +
        	"and they will notify you separately regarding their ability to serve.</p>" +
        	"<p>The deadline for these referrals will be <b>Friday, November 14th.</b></p>" +
        	"<p><b>Please don't wait until the deadline to send in this information.  The sooner we have the " +
        	"information the better we are able to serve.</b></p>" +
        	"<p>Thanks for your care and concern for these families.</p>" +
        	"<p>Sincerely,</p>" +
        	"<p>Stephanie Somers<br>" +
        	"ONC Referring Agent Liaison<br>" +
        	"Our Neighbor's Child<br>" +
        	"P.O. Box 276<br>" +
        	"Centreville, VA 20120<br>" +
        	"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p></div>" +
        	"<div><p><img src=\"cid:" + cid1 + "\" /></p></div>" +
        	"</body></html>", recipientFirstName);
        return msg;
	}
	
	String create2016AgentDecemberGiftConfirmationEmail(ONCUser user)
	{
        //Create the text part of the email using html
		String msg = "<html><body><div>" +
				"<p>Dear " + user.getFirstname() + ",</p>"
				+"<p>We realize you and the families you've referred are anxious to receive confirmation about " 
				+"December gift assistance from Our Neighbor's Child.</p>"
				+"<p>If you included an e-mail address for any family you referred, the family will "
				+"received an e-mail at that address today or tomorrow. It's written in both English and Spanish. "
				+ "For your reference we've included an English version sample at the bottom of this email.</p>"
				+"<p>We have also included a table of families you referred. "
				+"<b>If a valid e-mail address was not provided with the referral, the family will not receive an e-mail confirmation</b>. "
				+"For these families, you may choose to send a note home in their backpack, or simply use the list "
				+"provided as a reference should the family contact you to check on their status.</p>"
				+"<p>If a family you referred has SA, SBO, or DUP in the Code column next to their street address, "
				+"it is because they were "
				+"a duplicate of an earlier referral or they signed up with the Salvation Army or "
				+"other agency as well as ONC. The legend for the Code column is: " 
				+"SA = Served by The Salvation Army, SBO = Served by Other, DUP = Duplicate Family Referral. "
				+"FO = Food Only, Gift Assistance Not Requested, NISA - Not In ONC's Serving Area. "
				+"In that event, the family has been removed from the ONC gift list and should expect to "
				+"pick up their gifts with the Salvation Army or follow the instructions provided by their serving agency.</p>"
				+"<p>We will still use our automated calling system (after Thanksgiving) to notify all families who have not "
				+"acknowledged our email nor confirmed that they will be home on "
				+"Sunday, December 18th from 1 to 4PM to receive their children's gifts.</p>"
				+"<p>Our all-volunteer team at Our Neighbor's Child is actively working to collect their gifts and organize all the community "
				+"volunteers who help make this day possible.</p>"
		        +"<p>As always, thanks so much for your support!</p>"
		        +"<p>Kelly</p>"
		        +"<p>Kelly Murray Lavin<br>"
		        +"Executive Director/Volunteer<br>"
		        +"Our Neighbor's Child<br>"
		        +"P.O. Box 276<br>"
		        +"Centreville, VA 20120<br>"
		        +"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p></div>"
		        +"<p><b>" + user.getLastname() + " referrals scheduled for ONC gift delivery:</b></p>"
		        +createServedFamiliesRepresentedTableHTML(user)
		        +"<p><b>Sample family confirmation email (English):</b></p>"
		        +createSampleFamilyConfirmationEmail()
		        +"</body></html>";
        return msg;
	}
	
	String create2015AgentDeliveryStatusEmail(ONCUser user)
	{
        //Create the text part of the email using html
		String msg = "<html><body><div>" +
				"<p>Dear " + user.getFirstname() + ",</p>"
				+"<p>We are sending this email to help you respond to famiies you referred that were scheduled for " 
				+"December gift assistance delivery from Our Neighbor's Child. "
				+"<p>Below is a list of families you referred and the status of their delivery. We made or attempted "
				+ "all deliveries yesterday afternoon. If your family delivery was sucessful, the delivery status "
				+ "will be \"Delivered\". If the delivery attempt was unsuccessful due to an adult not home or an address change, "
				+ "the delivery status will be \"Attempted\" or \"Returned\".</p>"
				+"<p>If a family you referred has SA, SBO, or DUP in the Code column next to their delivery status, "
				+"it is because they were "
				+"a duplicate of an earlier referral or they signed up with the Salvation Army or "
				+"other agency as well as ONC. The legend for the Code column is: " 
				+"SA = Served by The Salvation Army, SBO = Served by Other, DUP = Duplicate Family Referral. "
				+"In that event, the family was removed from the ONC gift list and should expect to "
				+"pick up their gifts with the Salvation Army or follow the instructions provided by their serving agency.</p>"
		        +"<p>As always, thanks so much for your support!</p>"
		        +"<p>Kelly</p>"
		        +"<p>Kelly Murray Lavin<br>"
		        +"Executive Director/Volunteer<br>"
		        +"Our Neighbor's Child<br>"
		        +"P.O. Box 276<br>"
		        +"Centreville, VA 20120<br>"
		        +"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p></div>"
		        +"<p><b>" + user.getLastname() + " ONC referrals gift delivery status:</b></p>"
		        +createFamiliesDeliveryStatusTableHTML(user)
		        +"</body></html>";
        return msg;
	}
	
	String createServedFamiliesRepresentedTableHTML(ONCUser user)
	{
		StringBuilder familyTableHTML = new StringBuilder("<table style=\"width:100%\">");
		familyTableHTML.append("<th align=\"left\">Last Name</th>");
		familyTableHTML.append("<th align=\"left\">First Name</th>");
		familyTableHTML.append("<th align=\"left\">E-Mail Address</th>");
		familyTableHTML.append("<th align=\"left\">Street Address</th>");
//		familyTableHTML.append("<th align=\"left\">City</th>");
		familyTableHTML.append("<th align=\"left\">Code</th>");
		
		for(ONCFamily f:fDB.getList())
			if(user != null && f.getAgentID() == user.getID())	//family is represented by agent and is being served
			{
				familyTableHTML.append("<tr><td>" + f.getHOHLastName() + "</td>");
				familyTableHTML.append("<td>" + f.getHOHFirstName() + "</td>");
				familyTableHTML.append("<td>" + f.getEmail() + "</td>");
				familyTableHTML.append("<td>" + f.getHouseNum() + " " + f.getStreet() + " " + f.getUnitNum() + "</td>");
//				familyTableHTML.append("<td>" + f.getCity() + "</td></tr>");
				familyTableHTML.append("<td>" + f.getDNSCode() + "</td></tr>");
			}
			
		familyTableHTML.append("</table>");
				
		return familyTableHTML.toString();
	}
	
	String createFamiliesDeliveryStatusTableHTML(ONCUser user)
	{
		StringBuilder familyTableHTML = new StringBuilder("<table style=\"width:100%\">");
		familyTableHTML.append("<th align=\"left\">Last Name</th>");
		familyTableHTML.append("<th align=\"left\">First Name</th>");
		familyTableHTML.append("<th align=\"left\">Street Address</th>");
//		familyTableHTML.append("<th align=\"left\">Family Status</th>");
		familyTableHTML.append("<th align=\"left\">Delivery Status</th>");
//		familyTableHTML.append("<th align=\"left\">Meal Status</th>");
		familyTableHTML.append("<th align=\"left\">Code</th>");
		
		for(ONCFamily f:fDB.getList())
			if(user != null && f.getAgentID() == user.getID())	//family is represented by agent and is being served
			{
				familyTableHTML.append("<tr><td>" + f.getHOHLastName() + "</td>");
				familyTableHTML.append("<td>" + f.getHOHFirstName() + "</td>");
				familyTableHTML.append("<td>" + f.getHouseNum() + " " + f.getStreet() + " " + f.getUnitNum() + "</td>");
//				familyTableHTML.append("<td>" + famstatus[f.getFamilyStatus()] + "</td>");
				familyTableHTML.append("<td>" + f.getGiftStatus().toString() + "</td>");
//				familyTableHTML.append("<td>" + f.getMealStatus().toString() + "</td></tr>");
				familyTableHTML.append("<td>" + f.getDNSCode() + "</td></tr>");
			}
			
		familyTableHTML.append("</table>");
				
		return familyTableHTML.toString();
	}
	
	String createSampleFamilyConfirmationEmail()
	{
		 //Create the text part of the email using html
        String msg =
        	"<html><body><div>" +
        	"<p>Dear [Client Family First Name],</p>"+
        	"<p>Your request for Holiday Assistance has been received by Our Neighbor's Child, the local, " +
        	"community-based volunteer organization that provides holiday gifts to children in your area.</p>" +
        	"<p><b>Please read this email carefully. Your reply is required to ensure your family receives gifts.</b></p>" +
        	"<p>This e-mail only pertains to <b>HOLIDAY GIFTS</b> for your child/children.  Holiday food assistance is " +
        	"handled by other organizations and notification is separate.</p>" +
        	"<p><b>Here is the information that was provided by your School Counselor or other referring agent:</b></p>" +
    		"&emsp;<b>Family Name:</b>  <br>" +
    		"&emsp;<b>Address:</b>  <br>" +
    		"&emsp;<b>Address:</b>  <br>" +
    		"&emsp;<b>Home Phone #:</b>  <br>" +
    		"&emsp;<b>Other Phone #:</b>  <br>" +
    		"&emsp;<b>Email Address:</b>  <br>" + 
    		"&emsp;<b>Alternate Delivery Address:</b>  <br>" +
    		"&emsp;<b>Alternate Delivery Address:</b>  <br>" + 
        	"<p>An Our Neighbor's Child volunteer will deliver your children's gifts to the address listed above " +
        	"on <b>Sunday, December 18th between 1 and 4PM.</b>  "
        	+"Please reply to this email (in English or Spanish) to confirm that an adult will be home that "
        	+"day to receive your children's gifts. We may also attempt to contact you with an automated phone call.</p>" +
        	"<p><b>Important:  Families will only be served by one organization.</b> If your child/children's name " +
        	"appear on any other list (i.e. The Salvation Army), ONC will remove them from our list and will be " +
        	"unable to deliver gifts to your home.</p>" +
        	"<p>If your address or telephone number should change, PLEASE include those changes in your REPLY to this e-mail. "
        	+ "We are unable to accept any gift requests or changes to gift requests.</p>" +
        	"<p>If an emergency arises and you are unable to have an adult home on Sunday, December 18th between 1 " +
        	"and 4PM - PLEASE REPLY to this e-mail with an alternate local address (Centreville, Chantilly, Clifton or Fairfax) where " +
        	"someone will be home to receive the gifts on that day between 1 and 4PM.</p>"+
        	"<p>Thank you for your assistance and Happy Holidays!</p>" +
        	"<p><b>Our Neighbor's Child</b></p>";
        
        return msg;
	}
	
	boolean doesOrgMatch(String org) {return sortOrg.equals("Any") || sortOrg.equals(org);}
	boolean doesTitleMatch(String title) {return sortTitle.equals("Any") || sortTitle.equals(title); }
	boolean didAgentRefer(ONCUser u){ return allAgentsCxBox.isSelected() || fDB.didAgentRefer(u.getID()); }
	
	@Override
	String[] getColumnToolTips() 
	{
		String[] colToolTips = {"Agent First Name", "Agent Last Name", "Organization", "Title", "EMail Address", "Phone"};
		return colToolTips;
	}

	@Override
	String[] getColumnNames() 
	{
		String[] columns = {"First Name", "Last Name", "Org", "Title", "EMail", "Phone"};
		return columns;
	}

	@Override
	int[] getColumnWidths() 
	{
		int[] colWidths = {72, 80, 160, 120, 168, 120};
		return colWidths;
	}

	@Override
	int[] getCenteredColumns() 
	{
		return null;
	}
	
	@Override 
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == orgCB && !bIgnoreCBEvents && !orgCB.getSelectedItem().toString().equals(sortOrg))
		{
			sortTable.clearSelection();
			familyTable.clearSelection();
			
			sortOrg = orgCB.getSelectedItem().toString();
			buildTableList(false);			
		}				
		else if(e.getSource() == titleCB && !bIgnoreCBEvents && !titleCB.getSelectedItem().toString().equals(sortTitle))
		{
			sortTable.clearSelection();
			familyTable.clearSelection();
			
			sortTitle = titleCB.getSelectedItem().toString();
			buildTableList(false);			
		}
		else if(e.getSource() == allAgentsCxBox )
		{
			buildTableList(true);
			buildFamilyTableListAndDisplay();
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedIndex() == 1)
				onPrintListing("ONC Agents");
			
			printCB.setSelectedIndex(0);
		}
		else if(e.getSource() == famPrintCB)
		{
			if(famPrintCB.getSelectedIndex() == 1)
			{ 
				onPrintFamilyListing();
				famPrintCB.setSelectedIndex(0);
			}
		}
		else if(e.getSource() == btnDependantTableExport)
		{
			onExportDependantTableRequested();	
		}
		else if(e.getSource() == emailCB && emailCB.getSelectedIndex() > 0 )
		{
			//Confirm with the user that the deletion is really intended
			String confirmMssg = "Are you sure you want to send " + emailCB.getSelectedItem() + "?"; 
											
			Object[] options= {"Cancel", "Send"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
					gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Send Agent Email ***");
			confirmDlg.setLocationRelativeTo(this);
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Send"))
				createAndSendAgentEmail(emailCB.getSelectedIndex());
			
			emailCB.setSelectedIndex(0);	//Reset the combo box choice
		}
		else if(e.getSource() == btnEditAgentInfo)
		{
	    	if(!aiDlg.isVisible())
	    	{
	    		aiDlg.display(atAL.get(sortTable.getSelectedRow()));
	    		aiDlg.setLocationRelativeTo(btnEditAgentInfo);
	    		aiDlg.showDialog();
	    	}
		}
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
		if(dbe.getType().equals("UPDATED_FAMILY") || dbe.getType().equals("ADDED_DELIVERY"))
		{
//			System.out.println(String.format("Sort Agent Dialog DB event, Source: %s, Type %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			buildFamilyTableListAndDisplay();		
		}
		else if(dbe.getType().equals("LOADED_USERS"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Agent Management", GlobalVariables.getCurrentSeason()));
			updateOrgCBList();
			updateTitleCBList();
		}
		else if(dbe.getType().equals("ADDED_USER") ||
				dbe.getType().equals("UPDATED_USER") ||
				dbe.getType().equals("DELTED_USER"))	//build on add, update or delete event
		{
			//update the agent table and update the org and title combo box models
			buildTableList(true);
			updateOrgCBList();
			updateTitleCBList();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
//		System.out.println("SortAgtDlg.valueChanged: valueIsAdjusting: " + lse.getValueIsAdjusting());
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel() 
				&& !bChangingTable)
		{
			if(sortTable.getSelectedRowCount() == 0)	//No selection
			{
//				System.out.println("SortAgtDlg.valueChanged: lse event occurred, agent row count = 0");
				stAL.clear();
				clearFamilyTable();
				btnEditAgentInfo.setEnabled(false);
			}
			else	//Agent selected, build new family table associated with the agent
			{
//				System.out.println("SortAgtDlg.valueChanged: lse event occurred, agent selected");
				buildFamilyTableListAndDisplay();
				btnEditAgentInfo.setEnabled(true);
			
				fireEntitySelected(this, EntityType.AGENT, atAL.get(sortTable.getSelectedRow()), null);
				requestFocus();
			}		
		}
		else if (!lse.getValueIsAdjusting() && lse.getSource() == familyTable.getSelectionModel() &&
					!bChangingFamilyTable)
		{
			fireEntitySelected(this, EntityType.FAMILY, stAL.get(familyTable.getSelectedRow()), null);
			requestFocus();
		}
	
		checkPrintandEmailEnabled();
	}

	@Override
	int sortTableList(int col) 
	{
		archiveTableSelections(atAL);
		
		if(sortList(atAL, columns[col]))
		{
			displaySortTable(atAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;	
	}
	
	boolean sortList(List<ONCUser> aAL, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("First Name"))	//Sort on Agent Name
    		Collections.sort(aAL, new ONCUserFirstNameComparator());
		else if(dbField.equals("Last Name"))	//Sort on Agent Name
    		Collections.sort(aAL, new ONCUserLastNameComparator());
    	else if(dbField.contains("Org"))	// Sort on Agent Organization
    		Collections.sort(aAL, new ONCUserOrgComparator());
    	else if (dbField.equals("Title"))	//Sort on Agent Title
    		Collections.sort(aAL, new ONCUserTitleComparator());
		else
			bSortOccurred = false;
		
		return bSortOccurred;	
	}

	@Override
	void setEnabledControls(boolean tf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	Object[] getTableRow(ONCObject o) 
	{
		ONCUser u = (ONCUser) o;
		Object[] ai = {u.getFirstname(), u.getLastname(), u.getOrg(), u.getTitle(), 
						u.getEmail(), u.getPhone()};
		return ai;
	}
	
	@Override
	void onResetCriteriaClicked()
	{
		stAL.clear();
		clearFamilyTable();
		sortTable.clearSelection();
		familyTable.clearSelection();
		
		orgCB.removeActionListener(this);
		orgCB.setSelectedIndex(0);		//Will trigger the CB event handler which
		sortOrg = "Any";
		orgCB.addActionListener(this);;
		
		titleCB.removeActionListener(this);
		titleCB.setSelectedIndex(0);	//Will trigger the CB event handler which
		sortTitle = "Any";
		titleCB.addActionListener(this);
		
		allAgentsCxBox.removeActionListener(this);
		allAgentsCxBox.setSelected(false);
		allAgentsCxBox.addActionListener(this);
		
		buildTableList(false);
	}

	@Override
	void initializeFilters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.AGENT, EntityType.FAMILY);
	}
	
	private class ONCUserFirstNameComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)
		{
			return o1.getFirstname().compareTo(o2.getFirstname());
		}
	}
	
	private class ONCUserLastNameComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)
		{
			return o1.getLastname().compareTo(o2.getLastname());
		}
	}
	
	private class ONCUserOrgComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)
		{			
			return o1.getOrg().compareTo(o2.getOrg());
		}
	}
	
	private class ONCUserTitleComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)
		{			
			return o1.getTitle().compareTo(o2.getTitle());
		}
	}
}
