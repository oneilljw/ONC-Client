package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.mail.internet.MimeBodyPart;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;


public class SortAgentDialog extends DependantTableDialog implements PropertyChangeListener
															
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private static final String AGENT_EMAIL_SENDER_ADDRESS = "somerss@cox.net";
	private static final String AGENT_EMAIL_SENDER_ADDRESS = "volunteer@ourneighborschild.org";
//	private static final String AGENT_EMAIL_SENDER_ADDRESS = "johnwoneill1@gmail.com";
	private static final int MIN_EMAIL_ADDRESS_LENGTH = 2;
	private static final int MIN_EMAIL_NAME_LENGTH = 2;
	
	private JComboBox orgCB, titleCB;
	private DefaultComboBoxModel orgCBM, titleCBM;
	private String sortOrg, sortTitle;

	private JButton btnEditAgentInfo;
	private JComboBox printCB, emailCB;
	
	private JProgressBar progressBar;
	private ONCEmailer oncEmailer;

	private ONCAgents agentDB;
	private ArrayList<Agent> atAL;	//Holds references to agent objects for agent table
	
	private AgentInfoDialog aiDlg;
	

	SortAgentDialog(JFrame pf, String[] colToolTips, String[] cols, int[] colWidths, int[] center_cols)
	{
		super(pf, colToolTips, cols, colWidths, center_cols, 10);
		this.setTitle("Our Neighbor's Child - Agent Management");

		agentDB = ONCAgents.getInstance();
		if(agentDB != null)
			agentDB.addDatabaseListener(this);
		
		//Set up the agent table content array list
		atAL = new ArrayList<Agent>();
		
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
		
        //Set up the cntl panel
		//Set the text for the agent count label
		lblObjectMssg.setText("# of Agents:"); 
      
      	//Set up the email progress bar
      	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        String[] emailChoices = {"Email", "2014 Season Agent Email", "2014 Season Reminder Email",
        							"2014 Intake Reminder Email", "2014 December Gift Confirmation Email"};
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
                      
        //Add the components to the control panel
      	GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,0,0,180);
      	cntlPanel.add(objectCountPanel, gbc);
      	gbc.gridx = 1;
        gbc.insets = new Insets(0,0,0,0);
        cntlPanel.add(progressBar, gbc);
        gbc.gridx = 2;
        cntlPanel.add(emailCB, gbc);
        gbc.gridx = 3;
        cntlPanel.add(printCB, gbc);
        gbc.gridx = 4;
        cntlPanel.add(btnEditAgentInfo, gbc);
        
        //set the border title for the family table 
        familyTableScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(), "Families Represented By Selected Agent(s)"));

        //set up the Agent Info Dialog
        String[] tfNames = {"Name", "Organization", "Title", "Email", "Phone"};
    	aiDlg = new AgentInfoDialog(GlobalVariables.getFrame(), tfNames, true);
    	this.addEntitySelectionListener(aiDlg);
        
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
		
		for(Agent a:agentDB.getAgentsAL())
			if(doesOrgMatch(a.getAgentOrg()) && doesTitleMatch(a.getAgentTitle()))
				atAL.add(a);
			
		lblNumOfObjects.setText(Integer.toString(atAL.size()));
		displaySortTable(atAL, true, tableRowSelectedObjectList);
	}
	
	void updateOrgCBList()
	{
		bIgnoreCBEvents = true;
		
		ArrayList<String> orgItemAL = new ArrayList<String>();
		
		orgCBM.removeAllElements();
		
		
		for(Agent a:agentDB.getAgentsAL())
		{
			int index = 0;
			while(index < orgItemAL.size() && !a.getAgentOrg().equals(orgItemAL.get(index)))
				index++;
			
			if(index == orgItemAL.size())
				orgItemAL.add(a.getAgentOrg());	
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
		
		
		for(Agent a:agentDB.getAgentsAL())
		{
			int index = 0;
			while(index < titleItemAL.size() && !a.getAgentTitle().equals(titleItemAL.get(index)))
				index++;
			
			
			if(index == titleItemAL.size())
				titleItemAL.add(a.getAgentTitle());		
		}
		
		Collections.sort(titleItemAL);
		titleCBM.addElement("Any");
		for(String s:titleItemAL)
			titleCBM.addElement(s);
		
		bIgnoreCBEvents = false;
	}

	void checkPrintandEmailEnabled()
	{
		if(familyTable.getSelectedRowCount() > 0)
		{
			famPrintCB.setEnabled(true);
		}
		
		if(sortTable.getSelectedRowCount() > 0)
			printCB.setEnabled(true);
		else
			printCB.setEnabled(false);
		
		if(sortTable.getSelectedRowCount() > 0 && gvs.isUserAdmin())
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
		if(emailType == 1)
		{
			subject = "2014 Holiday Information From Our Neighbor's Child";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("DSC_0154.jpeg", cid0 , MimeBodyPart.INLINE));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
		else if(emailType == 2)//Reminder email
		{
			subject = "Important Reminder from Our Neighbor's Child";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("DSC_0154.jpeg", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
		else if(emailType == 3) //In-take work sheet e-mail
		{
			subject = "Holiday Assitance Update from Our Neighbor's Child";
			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
			attachmentAL.add(new ONCEmailAttachment("ONC Family Referral Worksheet.xlsx", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
		else if(emailType == 4) //December Gift Confirmation Email
		{
			subject = "December Gift Confirmations";
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("ONC Family Referral Worksheet.xlsx", cid0 , MimeBodyPart.ATTACHMENT));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
		
		//For each agent selected, create the email body and recipient information in an
		//ONCEmail object and add it to the email array list
		int[] row_sel = sortTable.getSelectedRows();
		for(int row=0; row< sortTable.getSelectedRowCount(); row++)
		{
			//Get selected agent object
			Agent agent = atAL.get(row_sel[row]);
			
			//Create the email body if the agent exists
			String emailBody = createEmailBody(emailType, agent, cid0, cid1);
			
	        //Create recipient list for email.
	        ArrayList<EmailAddress> recipientAdressList = createRecipientList(agent);
	        
	        //If the agent email isn't valid, the message will not be sent.
	        if(emailBody != null && !recipientAdressList.isEmpty())
	        	emailAL.add(new ONCEmail(subject, emailBody, recipientAdressList));     	
		}
		
		//Create the from address string array
		EmailAddress fromAddress = new EmailAddress(AGENT_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child");
		
		//Create the blind carbon copy list 
		ArrayList<EmailAddress> bccList = new ArrayList<EmailAddress>();
		bccList.add(new EmailAddress(AGENT_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child"));
		bccList.add(new EmailAddress("kellylavin1@gmail.com", "Kelly Lavin"));
//		bccList.add(new EmailAddress("mnrogers123@msn.com", "Nicole Rogers"));
//		bccList.add(new EmailAddress("johnwoneill@cox.net", "John O'Neill"));
		
		//Create mail server accreditation, then the mailer background task and execute it
		//Go Daddy Mail
//		ServerCredentials creds = new ServerCredentials("smtpout.secureserver.net", "director@act4others.org", "crazyelf1");
		//Google Mail
		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", AGENT_EMAIL_SENDER_ADDRESS, "ONC vols");
//		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", AGENT_EMAIL_SENDER_ADDRESS, "erin1992");
		
	    oncEmailer = new ONCEmailer(this, progressBar, fromAddress, bccList, emailAL, attachmentAL, creds);
	    oncEmailer.addPropertyChangeListener(this);
	    oncEmailer.execute();
	    emailCB.setEnabled(false);		
	}
	
	/**************************************************************************************************
	 *Creates a new email body each agent email. If agent is valid or doesn't have a valid first name
	 *a null body is returned
	 **************************************************************************************************/
	String createEmailBody(int emailType, Agent agent, String cid0, String cid1)
	{
		String emailBody = null;
		
		//verify the agent has a valid name. If not, return an empty list
		if(agent != null && agent.getAgentFirstName() != null && agent.getAgentFirstName().length() > MIN_EMAIL_NAME_LENGTH)
//			emailBody = createAgentEmailText(agent, cid0, cid1); 	//2013 Email Body
			if(emailType == 1)
				emailBody = create2014AgentEmailText(agent.getAgentFirstName());	//2014 email body
			else if(emailType == 2)
				emailBody = create2014AgentReminderEmail(agent.getAgentFirstName());
			else if(emailType == 3)
				emailBody = create2014AgentIntakeEmail(agent.getAgentFirstName(), cid0);
			else if(emailType == 4)
				emailBody = create2014AgentDecemberGiftConfirmationEmail(agent);
		return emailBody;
	}
	
	
	/**************************************************************************************************
	 *Creates a new list of recipients for each agent email. For agents, there is only one recipient per
	 *email. For other ONC email recipients, there may be two or more recipients for an email
	 *If the agent does not have a valid email or name, an empty list is returned
	 **************************************************************************************************/
	ArrayList<EmailAddress> createRecipientList(Agent agent)
	{
		ArrayList<EmailAddress> recipientAddressList = new ArrayList<EmailAddress>();
		
		//verify the agent has a valid email address and name. If not, return an empty list
		if(agent != null && agent.getAgentEmail() != null && agent.getAgentEmail().length() > MIN_EMAIL_ADDRESS_LENGTH &&
				agent.getAgentName() != null && agent.getAgentName().length() > MIN_EMAIL_NAME_LENGTH)
        {
			EmailAddress toAddress = new EmailAddress(agent.getAgentEmail(), agent.getAgentName());	//live
			recipientAddressList.add(toAddress);

//			EmailAddress toAddress1 = new EmailAddress("johnwoneill@cox.net", "John O'Neill");	//test
//        	recipientAddressList.add(toAddress1);      	
        }
		
		return recipientAddressList;
	}
	
	String createAgentEmailText(Agent agent, String cid1, String cid2)
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
	
	String create2014AgentEmailText(String agentFirstName)
	{
        //Create the text part of the email using html
        String msg = String.format(
        	"<html><body><div>" +
     //   	"<p><b>Hello FCPS Counselors, Social Workers and other Holiday Assistance Referring Agents!!</p></b>"+
        	"<p>Hello %s,</p>"+
        	"<p>It's that time of year again!  <b>Our Neighbors Child (ONC)</b>,  the local, community-based " +
        	"organization that provides Holiday Gifts in December for Centreville, Chantilly and western " +
        	"Fairfax (22020, 22021, 22033, 22051, 22050, 22039 and 20124), is gearing up for our 23rd year!!!  " +
        	"We wanted to make sure you've received the <b>2014 Holiday Program Instructions and Guidelines</b> " +
        	"distributed by Our Daily Bread (ODB).</p>" +
        	"<p>For clarification:</p>" +
        	"<p><b>Our Daily Bread (ODB)</b> is the organization contracted to serve as the County's " +
        	"\"central clearinghouse\" for Holiday Assistance Referrals. Once received, the referrals are " +
        	"redirected to local Community Based Organization (as available) who will provide the holiday " +
        	"food and/or gifts for your families. The deadline to register as a Referring Agent with ODB is " +
        	"October 20, 2014.</p>" +
        	"<p><b>Our Neighbor's Child (ONC)</b> is the local, community-based organization that provides " +
        	"<b>Holiday gifts</b>.  Holiday food will come from another local organization.</p>" +
        	"<p><b>Please remind your families that they can sign up with only one organization</b>. " +
        	"The Salvation Army now has a Fairfax location and accepts in-person sign-ups for children up " +
        	"to age 12. These lists are shared to avoid duplication of services.  A family who signs up at the " +
        	"Salvation Army will be removed from the Our Neighbor's Child list. (Note: ONC serves children " +
        	"through their senior year in high school).</p><br>" +
        	"<p><font color=\"red\"><b>Reminder: Holiday Gift referrals must be received by ODB's deadline of " +
        	"November 14th.</b></font></p>" + 
        	"<p>***Please note: This year, ODB is limiting the number of families that can be registered.  " +
        	"Once this limit has been reached, you will no longer be able to submit your families for holiday " +
        	"assistance through ODB.  We want to make sure you know this and would ask that you " +
        	"PLEASE REGISTER AS EARLY AS YOU CAN!!! IF YOU ARE REFERRING FAMILIES FOR THANKSGIVING ASSISTANCE " +
        	"(WHICH HAS A 10/24/2014 DEADLINE), IT WOULD BE GREAT IF YOU COULD GO AHEAD AND REGISTER YOUR " +
        	"FAMILIES FOR HOLIDAY GIFTS AT THE SAME TIME.***</p><br>" +
        	"<p><font color=\"red\"><b>Each year, we strive to make it the BEST HOLIDAY EVER for our families " +
        	"and YOU are one of the most important parts!!</b></font></p>" +
        	"<p>We know how much you care about the children you refer.  Please help us in our efforts by " +
        	"remembering that:</p>" +
        	"<p><b>Accuracy is key</b>. ONC (and volunteers throughout our community) will be serving more than " +
        	"750 local families again this year. We're only able to do this through partnership with local " +
        	"schools, churches, businesses and individuals who generously donate the gifts. Our ability to " +
        	"deliver the correct gifts to the child at their correct address is only possible with your " +
        	"careful efforts.</p>" +
        	"<p><b>Give DETAILS!!! The more, the better!!!</b> The wonderful folks " +
        	"who \"adopt\" these families really do appreciate all the specifics and guidance only you can provide " +
        	"when it comes to picking out the perfect gifts!  The best way to ensure that the child you refer will " +
        	"receive the gift they'd most enjoy is to list their wishes (up to three) in the order of importance. " +
        	"If it's a clothing wish, please include information on size and color (if there's a preference). " +
        	"If it's a coat, let us know if they want a hoodie, a rain coat, a warm winter coat, etc.  " +
        	"If a parent is uncertain about what their child would like, list \"age appropriate\" or \"educational toy\". " +
        	"If you know specific information about the child that may help in selecting a suitable gift, " +
        	"please add that information in the \"comments\". Information such as \"likes arts and crafts\" or " +
        	"\"loves horses\" can be particularly helpful.  If a child's wish is a bike, please include the child's " +
        	"height and a color preference (if any). When we are able to provide bikes, we always include a helmet.</p>" +
        	"<p><b>Sunday, December 14th, between 1:00 and 4:00 PM</b> is the date our ONC families will receive home " +
        	"delivery of their holiday gifts.</p>" +
        	"<p><b>ONC introduced a new automated calling system in 2012 in an effort to provide more timely " +
        	" with each family</b>. This interactive system (generously donated by Angel/Genesys) allows families to " +
        	"choose their message in <b>English</b> or <b>Spanish</b>. Please tell the families you refer that calls " +
        	"are scheduled to begin the week after Thanksgiving.</p><br>" +
        	"<p>We know you're all very busy and we appreciate everything you do each and every day.  Thank you " +
        	"for helping us make sure that EVERY child receives the thoughtful selection of gifts that will help make their holidays bright!</p>" +
        	"<p>I'm so excited for this season!  Should you have any questions, please don't hesitate to shoot" +
        	" me an e-mail.  Also, if you have referring agents that are new this year, PLEASE let me know so that " +
        	"I can add their names and contact information to our data base!  And feel free to forward them this " +
        	"email so that they have my contact information as well.</p>" +
        	"<p>All the best,</p>" +
        	"<p>Stephanie Somers<br>" +
        	"ONC Referring Agent Liaison<br>" +
        	"Our Neighbor's Child<br>" +
        	"P.O. Box 276<br>" +
        	"Centreville, VA 20120<br>" +
        	"<a href=\"http://www.ourneighborschild.org\">www.ourneighborshild.org</a></p>" +
        	"</div></body></html>", agentFirstName);
        return msg;
	}
	
	String create2014AgentReminderEmail(String agentFirstName)
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
        	"</div></body></html>", agentFirstName);
        return msg;
	}
	
	String create2014AgentIntakeEmail(String agentFirstName, String cid1)
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
        	"</body></html>", agentFirstName);
        return msg;
	}
	
	String create2014AgentDecemberGiftConfirmationEmail(Agent agt)
	{
        //Create the text part of the email using html
		String msg = "<html><body><div>" +
			"<p>Dear " + agt.getAgentFirstName() + ",</p>" +
        	"<p>We realize you and the families you've referred are anxious to receive confirmation about " +
        	"December gift assistance from Our Neighbor's Child.</p>" +
        	"<p>If you included an e-mail address for any family you referred, the family will " +
        	"receive an e-mail at that address today.</p>" +
        	"<p>Below you will find a table of families you referred that will be served by ONC. " +
        	"<b>If no valid e-mail address has been provided, the family will not receive an e-mail confirmation</b>. " +
        	"For these families, you may choose to send a note home in their backpack, or simply use the list " +
        	"provided as a reference should the family contact you to check on their status.</p>" +
        	"<p>If a family you referred does not appear on the ONC list below, it is possible that they signed up " +
        	"with the Salvation Army.  For your convenience, we have also included a table of families in " +
        	"our area on the Salvation Army's list.  They have been removed from the ONC list and should expect to " +
        	"pick up their gifts with the Salvation Army as they have arranged.</p>" +
        	"<p>This is the first season we have had the ability to provide this \"early acknowledgement\".  " +
        	"We hope it helps.</p>" +
        	"<p>We will still use our automated calling system (after Thanksgiving) to notify <b>ALL</b> families of our " +
        	"Delivery Day (December 14th).</p>" +
        	"<p>Our all-volunteer team at Our Neighbor's Child is actively working to organize all the community " +
        	"volunteers who help make this day possible.</p>" +
        	"<p>As always, thanks so much for your support!</p>" +
        	"<p>Kelly</p>" +
        	"<p>Kelly Murray Lavin<br>" +
        	"Executive Director/Volunteer<br>" +
        	"Our Neighbor's Child<br>" +
        	"P.O. Box 276<br>" +
        	"Centreville, VA 20120<br>" +
        	"<a href=\"http://www.ourneighborschild.org\">www.ourneighborschild.org</a></p></div>" +
        	"<p><b>" + agt.getAgentName() + " referrals scheduled for ONC gift delivery:</b></p>" +
        	createServedFamiliesRepresentedTableHTML(agt) +
        	"<p>Listed below are the 84 families in our zip code areas who will be served by the Salvation Army. " +
        	"28 of those families had also been referred to ONC and have been removed from our serving list. " +
        	"644 families remain on our list and will be served by Our Neighbor's Child.  This information is " +
        	"confidential and is provided to Referring Agents only for the express purpose of ensuring holiday " +
        	"assistance is provided to all families in need.</p>" +
        	"<p><b>Local families being served by SALVATION ARMY:</b></p>" +
        	create2014SalvationArmyTableHTML() +	
        	"</body></html>";
        return msg;
	}
	
	String createServedFamiliesRepresentedTableHTML(Agent a)
	{
		StringBuilder familyTableHTML = new StringBuilder("<table style=\"width:100%\">");
		familyTableHTML.append("<th align=\"left\">Last Name</th>");
		familyTableHTML.append("<th align=\"left\">First Name</th>");
		familyTableHTML.append("<th align=\"left\">E-Mail Address</th>");
		familyTableHTML.append("<th align=\"left\">Street Address</th>");
		familyTableHTML.append("<th align=\"left\">City</th>");
		
		for(ONCFamily f:fDB.getList())
			if(f.getDNSCode().isEmpty() && f.getAgentID() == a.getID())	//family is represented by agent and is being served
			{
				familyTableHTML.append("<tr><td>" + f.getHOHLastName() + "</td>");
				familyTableHTML.append("<td>" + f.getHOHFirstName() + "</td>");
				familyTableHTML.append("<td>" + f.getFamilyEmail() + "</td>");
				familyTableHTML.append("<td>" + f.getHouseNum() + " " + f.getStreet() + " " + f.getUnitNum() + "</td>");
				familyTableHTML.append("<td>" + f.getCity() + "</td></tr>");
			}
			
		familyTableHTML.append("</table>");
				
		return familyTableHTML.toString();
	}
	
	String create2014SalvationArmyTableHTML()
	{
		return
		"<table style=\"width:90%\">" +
		"<th align=\"left\">Last Name</th>" +
		"<th align=\"left\">First Name</th>" +
		"<th align=\"left\">Street Address</th>" +
		"<th align=\"left\">City</th>" +
		"<tr><td>Abdalaal Mohammad</td><td>Shema Yahya</td><td>11465 Cypress Point Ct.</td><td>Centreville</td></tr>" + 
		"<tr><td>Abdel Shayed</td><td>Adel</td><td>6167 Gothwaite Dr</td><td>Centreville</td></tr>" + 
		"<tr><td>Abida</td><td>Naoual</td><td>3924 PenderviewDr. Apt. 208</td><td>Fairfax</td></tr>" + 
		"<tr><td>Acevedo</td><td>Ruth</td><td>6015 Raina Dr</td><td>Centreville</td></tr>" + 
		"<tr><td>Aleman</td><td>Sandra Janneth</td><td>14517 Black Horse Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Alvarez Martinez</td><td>Steven Alexander</td><td>13507 Prairie Mallow Lane</td><td>Centreville</td></tr>" + 
		"<tr><td>Aziz</td><td>Dalia J</td><td>4002 Royal Lytham Drive</td><td>Fairfax</td></tr>" + 
		"<tr><td>Baires</td><td>Reyna</td><td>14863 Lambert Square</td><td>Centreville</td></tr>" + 
		"<tr><td>Bazurto Pinargote</td><td>Angela Guadalupe</td><td>6218 William Mosby Drive</td><td>Centreville</td></tr>" + 
		"<tr><td>Berasko</td><td>Maryana</td><td>14748 Basingstoke Loop</td><td>Centreville</td></tr>" + 
		"<tr><td>Bonilla</td><td>Sulma</td><td>14800 Lynhodge Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Bonilla Canales</td><td>Ana Maritza</td><td>14521 Lanica Circle</td><td>Chantilly</td></tr>" + 
		"<tr><td>Breskone</td><td>Salah</td><td>14936 Lady Madonna Court</td><td>Centreville</td></tr>" + 
		"<tr><td>Brooks</td><td>Jaqueline Martha</td><td>14350 Jacob Lane</td><td>Centreville</td></tr>" + 
		"<tr><td>Brown</td><td>Angel Nicole</td><td>5616 Oakham PL</td><td>Centreville</td></tr>" + 
		"<tr><td>Castillo</td><td>Idalma Marisol</td><td>5701 Cedar Walk Way #301</td><td>Centreville</td></tr>" + 
		"<tr><td>Chang</td><td>NamGug</td><td>13701 Winding Oak Cir #305</td><td>Centreville</td></tr>" + 
		"<tr><td>Delcid</td><td>Delia</td><td>6107C Hoskins Hollow Circle</td><td>Centreville</td></tr>" + 
		"<tr><td>Dunham</td><td>Desoraie</td><td>4128 Chantilly Lace Court</td><td>Chantilly</td></tr>" + 
		"<tr><td>Dykes</td><td>Tinneil Marsha</td><td>6107C Hoskins Hollow Circle</td><td>Centreville</td></tr>" + 
		"<tr><td>Galouaa</td><td>Carol</td><td>6100 Havener House Way Apt 6</td><td>Centreville</td></tr>" + 
		"<tr><td>Gebrael</td><td>Ayman Samir Nazim</td><td>6028 Chestnut Hollow Court</td><td>Centreville</td></tr>" + 
		"<tr><td>GeGril</td><td>Silvana</td><td>6102 Havener House Way</td><td>Centreville</td></tr>" + 
		"<tr><td>Georgy</td><td>Sameh Moawad</td><td>13615 Bent Tree Circle, Apt. 201</td><td>Centreville</td></tr>" + 
		"<tr><td>Gonzales Terceros</td><td>Carmen Janeth</td><td>14364 Avocado Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Haji-badri</td><td>Jane Ismael</td><td>4513 Lees Corner Road</td><td>Chantilly</td></tr>" + 
		"<tr><td>Han</td><td>Soon</td><td>13855 BRADDOCK ROAD SPRINGS RD</td><td>Centreville</td></tr>" + 
		"<tr><td>Haritos</td><td>Myriam</td><td>12228 Apple Orchard Court</td><td>Fairfax</td></tr>" + 
		"<tr><td>Henderson</td><td>Ramonica</td><td>14222 Glade Spring Dr</td><td>Centreville</td></tr>" + 
		"<tr><td>Henriquez</td><td>Evelyn</td><td>14919 Rydell Rd. Apt. 103</td><td>Centreville</td></tr>" + 
		"<tr><td>heo</td><td>juyeon</td><td>7732 miller rd</td><td>Centreville</td></tr>" + 
		"<tr><td>Hernandez - Negron</td><td>Rebeca</td><td>13442 Fiery Dawn Dr</td><td>Centreville</td></tr>" + 
		"<tr><td>Huarcaya Saire</td><td>Maria</td><td>14516 United Dr</td><td>Chantilly</td></tr>" + 
		"<tr><td>Ibrahim</td><td>Janet Mouris</td><td>6112 Havener House Way #4</td><td>Centreville</td></tr>" + 
		"<tr><td>Ibrahim</td><td>Jacqueline, Nassif Bakhoum</td><td>6407 Paddington Ct. Apt. 101</td><td>Centreville</td></tr>" + 
		"<tr><td>Jackson</td><td>Shaunte Lywann</td><td>4127 Chantilly Lace Court</td><td>Chantilly</td></tr>" + 
		"<tr><td>Joya</td><td>Jacqueline, Marisol</td><td>665 Dulles Park Ct. Apt. 207</td><td>Centreville</td></tr>" + 
		"<tr><td>Kaldas</td><td>Hawaida Kaldas Matta</td><td>13930 Antonia Ford Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Kerlous</td><td>Botros</td><td>14190 Asher View</td><td>Centreville</td></tr>" + 
		"<tr><td>kwon</td><td>chang</td><td>13971 tanners houseway</td><td>Centreville</td></tr>" + 
		"<tr><td>Kyebi</td><td>Abena</td><td>12113 Ragan Oaks Ct, #203</td><td>Fairfax</td></tr>" + 
		"<tr><td>LEE</td><td>EUNJOO</td><td>13329 CONNOR DR #1</td><td>Centreville</td></tr>" + 
		"<tr><td>lee</td><td>hyang won</td><td>8199 tiswell</td><td>Fairfax Statio</td></tr>" + 
		"<tr><td>Lee</td><td>Sangmi</td><td>12110 GREEN LEDGE CT #101</td><td>Fairfax</td></tr>" + 
		"<tr><td>Lizarazu Velasquez</td><td>Erlinda</td><td>13305 Jasper Rd</td><td>Fairfax</td></tr>" + 
		"<tr><td>Lopez</td><td>Yoryina</td><td>4017 Majestic Ln, Apt D</td><td>Fairfax</td></tr>" + 
		"<tr><td>Lopez Vasquez</td><td>Roselia Marcelina</td><td>4045 Majestic Ln, Apt A</td><td>Fairfax</td></tr>" + 
		"<tr><td>Luna</td><td>Maria</td><td>3920 Kernstown Ct.</td><td>Fairfax</td></tr>" + 
		"<tr><td>Maawad</td><td>Abaer Isak Amin</td><td>13648 Bent Tree Cr #202</td><td>Centreville</td></tr>" + 
		"<tr><td>Mansoor</td><td>Thamer</td><td>12901 Fair Briar Lane</td><td>Fairfax</td></tr>" + 
		"<tr><td>Martinez</td><td>Sandra, Patricia</td><td>13223 Poplar Tree Road</td><td>Fairfax</td></tr>" + 
		"<tr><td>Massoud</td><td>Mounira Kassab Mourcos</td><td>3858 Billberry Drive</td><td>Fairfax</td></tr>" + 
		"<tr><td>Matta</td><td>Manal</td><td>14503 Northeast Place</td><td>Chantilly</td></tr>" + 
		"<tr><td>Mawed</td><td>Mariam</td><td>13694 Bent Tree Circle, Apt 302</td><td>Centreville</td></tr>" + 
		"<tr><td>Metias</td><td>Hanan S</td><td>14711 Bonnet Terrace</td><td>Centreville</td></tr>" + 
		"<tr><td>Moanes</td><td>Rania M</td><td>15401 Eagle Tavern Ln.</td><td>Centreville</td></tr>" + 
		"<tr><td>Montecinos Joya</td><td>Ingrid Lisseth</td><td>14509 Saint Germain Drive</td><td>Centreville</td></tr>" + 
		"<tr><td>Monzon de Torres</td><td>Elizabeth Norma</td><td>3746 Farmland Drive</td><td>Fairfax</td></tr>" + 
		"<tr><td>Newton</td><td>Bethany</td><td>5522 Sully Lake Drive</td><td>Centreville</td></tr>" + 
		"<tr><td>Ochoa Escobar</td><td>Carmen</td><td>13686 BentTree Circle Apt.#302</td><td>Centreville</td></tr>" + 
		"<tr><td>Ortube</td><td>Marco</td><td>14004 B Franklin Fox Drive</td><td>Centreville</td></tr>" + 
		"<tr><td>park</td><td>joo</td><td>6064 clay spor ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Ramirez Hernandez</td><td>Graciela</td><td>13760 Autumn Valley Court</td><td>Chantilly</td></tr>" + 
		"<tr><td>Retes Senzano</td><td>Carmen Rosa</td><td>3924 Penderview Drive, # 236</td><td>Fairfax</td></tr>" + 
		"<tr><td>Reynoso Morrobel</td><td>Rosa Yessenia</td><td>14517 Black Horse Court</td><td>Centreville</td></tr>" + 
		"<tr><td>Rivera</td><td>Regina Mayrene</td><td>14424 Four Chimeny Dr</td><td>Centreville</td></tr>" + 
		"<tr><td>Saad,</td><td>Mary Saeed Aittea</td><td>14374 Haysickle Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Salazar</td><td>Dora Magaly</td><td>14538 Iberia Cr.</td><td>Chantilly</td></tr>" + 
		"<tr><td>Sanchez Arutaype</td><td>Viviana, Esstefa</td><td>13848 Laura Ratcliff Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Sindi</td><td>Jane</td><td>4662 Kearns Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Stokes</td><td>Veronia Roxanne</td><td>3902 Golf Tee Ct Apt 101</td><td>Fairfax</td></tr>" + 
		"<tr><td>Taboada</td><td>Katherine Paola</td><td>14207 Belt Buckle Ct.</td><td>Centreville</td></tr>" + 
		"<tr><td>Talyot</td><td>Nagwa Eid Said</td><td>6921 Hovingham Court</td><td>Centreville</td></tr>" + 
		"<tr><td>Toma</td><td>Merzk</td><td>14802 Lynhodges Ct</td><td>Centreville</td></tr>" + 
		"<tr><td>Torres</td><td>Dina Esmeralda</td><td>4045 Majestic Lane, Apt A</td><td>Fairfax</td></tr>" + 
		"<tr><td>Turcios Mejia</td><td>Jessenia Maria</td><td>5520 Sully Lake Drive</td><td>Centreville</td></tr>" + 
		"<tr><td>Valts</td><td>Samia Rawny</td><td>14626 Croatan Dr.</td><td>Centreville</td></tr>" + 
		"<tr><td>Velasquez</td><td>Lilian</td><td>7902 John Adams Ct. #202</td><td>Fairfax</td></tr>" + 
		"<tr><td>Von Der Heyde</td><td>Cesar Augusto</td><td>4418 Lees Comer Rd</td><td>Chantilly</td></tr>" + 
		"<tr><td>Worthey</td><td>Khairina, Molly</td><td>6427 Paddinston Court #203</td><td>Centreville</td></tr>" + 
		"<tr><td>Zuniga Soriano</td><td>Rosa Dina</td><td>14619 Stone Crossing Court</td><td>Centreville</td></tr>" +
		"</table>";
	}
	
	boolean doesOrgMatch(String agentorg) {return sortOrg.equals("Any") || sortOrg.equals(agentorg);}
	boolean doesTitleMatch(String agenttitle) {return sortTitle.equals("Any") || sortTitle.equals(agenttitle); }
	
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
				onPrintListing("ONC Families for Agent");
				famPrintCB.setSelectedIndex(0);
			}
		}
		else if(e.getSource() == emailCB && emailCB.getSelectedIndex() > 1 )
		{
			//Confirm with the user that the deletion is really intended
			String confirmMssg = "Are you sure you want to send referring agent email?"; 
											
			Object[] options= {"Cancel", "Send"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
					gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Send Partner Email ***");
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
		else if(dbe.getType().contains("_AGENT"))	//build on add, update or delete event
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
			
				fireEntitySelected(this, "AGENT_SELECTED", atAL.get(sortTable.getSelectedRow()), null);
				requestFocus();
			}		
		}
		else if (!lse.getValueIsAdjusting() && lse.getSource() == familyTable.getSelectionModel() &&
					!bChangingFamilyTable)
		{
			fireEntitySelected(this, "FAMILY_SELECTED", stAL.get(familyTable.getSelectedRow()), null);
			requestFocus();
		}
	
		checkPrintandEmailEnabled();
	}

	@Override
	int sortTableList(int col) 
	{
		archiveTableSelections(atAL);
		
		if(agentDB.sortDB(atAL, columns[col]))
		{
			displaySortTable(atAL, false, tableRowSelectedObjectList);
			return col;
		}
		else
			return -1;	
	}

	@Override
	void setEnabledControls(boolean tf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String[] getTableRow(ONCObject o) 
	{
		Agent a = (Agent) o;
		String[] ai = {a.getAgentName(), a.getAgentOrg(), a.getAgentTitle(), 
						a.getAgentEmail(), a.getAgentPhone()};
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
		
		buildTableList(false);
	}
}
