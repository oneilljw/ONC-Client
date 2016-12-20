package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This dialog implements the user interface for conducting private chats between two ONC elves online.
	 * The dialog works with the ChatManager singleton instance to send and receive CHAT_ commands with the ONC server.
	 * A chat has three states: IDLE, REQUESTED and ACTIVE. In the IDLE state, the user can select another online
	 * elf and request a chat. A combo box list of online elves is provided at the top of dialog. Once the user
	 * selects an elf to chat with, a chat request is sent to that elf via the ONC server and the chat state becomes
	 * requested. If the requested elf accepts the chat, the user is notified and the chat state becomes active.
	 * In this state, the combo box holding the selected elf is disabled and the text field for creating chat text
	 * becomes editable. Once a chat is active, it remains active until one of the two chatting elves closes
	 * their chat dialog. When this occurs, a message is sent to the other elf, closing the notified elf's dialog.
	 */
	private static final long serialVersionUID = 1L;
	private static final int CHAT_TEXT_FONT_SIZE = 13;
	
	//references to singletons
	private UserDB oncUserDB;
	private ChatManager chatMgr;
	
	//class variables
	private ChatState chatState;	//idle, requested, active enumeration
	private long chatTargetClientID;	//Client ID, as assigned by ONC Server, of chat partner
	private ArrayList<ONCUser> onlineUserList;
	
	//user interface
	private JPanel contentPane, entrypanel, requestpanel;
	private JComboBox chattersCB;	//holds  a list of online users that can chat with this user
	private DefaultComboBoxModel chattersCBM;
	private JLabel lblChatters;
	private JTextField dataTF;	//user types chat text to be sent in this text field
	private JTextPane chatTP;	//chat text and state information is displayed in this text pane
	private StyledDocument chatDoc;	//document that holds chat text
	private SimpleAttributeSet systemAttribs, clientAttribs, partnerAttribs;	//text styles for chatters
	private JButton btnAccept, btnReject;
	
	//semaphores
	private boolean bIgnoreChatCBRequests;	//true when application changes the list of online chatters
	
	public ChatDialog(JFrame parentFrame, boolean bInitiateChat, long chatTargetClientID)
	{
		super(parentFrame);
		this.setTitle("Chat");
		
		//detect when user closes the window, which causes the chat to end
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we){ endChat(); }});
		
		//references to singletons
		oncUserDB = UserDB.getInstance();
		chatMgr = ChatManager.getInstance();
		
		//listen for chat commands
		if(chatMgr != null)
			chatMgr.addDatabaseListener(this);	
		
		if(bInitiateChat)
		{
			chatState =  ChatState.IDLE;
			this.chatTargetClientID = -1;
			bIgnoreChatCBRequests = false;
		}
		else
		{
			chatState = ChatState.REQUESTED;
			this.chatTargetClientID = chatTargetClientID;
			bIgnoreChatCBRequests = true;
		}

		//create a reference to the content pane
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		//create a combo box that holds possible chat partners
		JPanel chatterspanel = new JPanel();
		
		lblChatters = new JLabel("Select Elf:");
		
		chattersCBM = new DefaultComboBoxModel();
    	chattersCBM.addElement("- Online Elves List -");
		
    	//create a list of online users and create the elf selection combo box list
    	onlineUserList = new ArrayList<ONCUser>();
		for(ONCUser onlineuser:oncUserDB.getOnlineUsers())
		{
			//test to see if a user can be a chat partner. Cannot chat with a web user or yourself
			if(onlineuser.getClientID() > -1 && 
				onlineuser.getClientID() != oncUserDB.getLoggedInUser().getClientID())
			{
				onlineUserList.add(onlineuser);
				chattersCBM.addElement(onlineuser);
			}
		}
		
		chattersCB = new JComboBox(chattersCBM);
		chattersCB.addActionListener(this);
		
		chatterspanel.add(lblChatters);
		chatterspanel.add(chattersCB);
		
		//create chat text display panel
		JPanel textpanel = new JPanel();
		
		//create the text pane that displays chat text
		chatTP = new JTextPane();
        
        //create the document that holds chat text
        chatDoc = chatTP.getStyledDocument();
        
        //create the system chat text style
        systemAttribs = new SimpleAttributeSet();
        StyleConstants.setForeground(systemAttribs, Color.RED);
//      StyleConstants.setBackground(clientAttribs, Color.YELLOW);
        StyleConstants.setBold(systemAttribs, true);
        
        //create the client chat text style
        clientAttribs = new SimpleAttributeSet();
        StyleConstants.setForeground(clientAttribs, Color.BLACK);
      
        //create the chat partner text style
        partnerAttribs = new SimpleAttributeSet();
        StyleConstants.setForeground(partnerAttribs, Color.BLUE);
        
        //create the paragraph attributes
        SimpleAttributeSet paragraphAttribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(paragraphAttribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(paragraphAttribs, CHAT_TEXT_FONT_SIZE);
        StyleConstants.setSpaceBelow(paragraphAttribs, 3);
        
        //set the paragraph attributes, disable editing and set the pane size
        chatTP.setParagraphAttributes(paragraphAttribs, true);
  	   	chatTP.setEditable(false);
  	   	chatTP.setPreferredSize(new Dimension(280,120));
  	   
  	   	//Create the chat text scroll pane and add the Wish List text pane to it.
        JScrollPane chatScrollPane = new JScrollPane(chatTP);
        chatScrollPane.setBorder(BorderFactory.createTitledBorder("Chat"));
  	   	textpanel.add(chatScrollPane);
		
		//create chat text entry panel
		entrypanel = new JPanel();
		entrypanel.setBorder(BorderFactory.createTitledBorder("Type message, <Enter> key to send"));
		dataTF = new JTextField(20);
		dataTF.setEditable(false);
		dataTF.addActionListener(this);
		entrypanel.add(dataTF);
		
		//create the request answer panel
		requestpanel = new JPanel();
		requestpanel.setLayout((new FlowLayout(FlowLayout.RIGHT)));
		requestpanel.setBorder(BorderFactory.createTitledBorder("Accept or Reject Chat Request"));
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(this);
		btnReject = new JButton("Not Now");
		btnReject.addActionListener(this);
		requestpanel.add(btnReject);
		requestpanel.add(btnAccept);
		
		//add user interface components to content pane
		contentPane.add(chatterspanel);
		contentPane.add(textpanel);
		
		if(!bInitiateChat)	//dialog instantiated in response to chat request
		{
			ONCUser chatPartner = getChatPartnerUser(chatTargetClientID);
			if(chatPartner != null)
			{
				//found the requester client ID in the list of online users
				this.setTitle("Chat Request From " + chatPartner.toString());
				lblChatters.setText("Request from:");
				displayChatText("Accept or Reject Chat Request from " + chatPartner.toString(), systemAttribs);
				chattersCB.setEnabled(false);
				bIgnoreChatCBRequests = true;
			
				chattersCB.setSelectedItem(chatPartner);
				contentPane.add(requestpanel);
				try {
					SoundUtils.tone(250,50);
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				//didn't find the requester in the online user list - ERROR
			}
		}
		else
		{
			displayChatText("Select an elf to chat with using the above list", systemAttribs);
			contentPane.add(entrypanel);
		}
		
		pack();
	}
	
	void displayChatText(String line, SimpleAttributeSet sas)
	{
		try {
			chatDoc.insertString(chatDoc.getLength(), line + "\n", sas );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void sendChatRequest()
	{
		//form the chat request ChatMessage Object
//		chatTargetClientID = onlineUserList.get(chattersCB.getSelectedIndex()-1).getClientID();
		chatTargetClientID = ((ONCUser) chattersCBM.getSelectedItem()).getClientID();
		
		ChatMessage reqMssg = new ChatMessage(oncUserDB.getLoggedInUser().getClientID(), chatTargetClientID);
		
		String response = chatMgr.requestChat(this,reqMssg);
		
		if(response.equals("CHAT_REQUEST_SENT"))
		{
			chatState = ChatState.REQUESTED;	//set state to requested
			this.setTitle("Chat Requested");
			chattersCB.setEnabled(false);	//disable another request until chat has ended or request is denied
//			displayChatText("Chat request sent to " + (String) chattersCB.getSelectedItem(), systemAttribs);
			displayChatText("Chat request sent to " + chattersCB.getSelectedItem().toString(), systemAttribs);
		}
		else
			displayChatText("CHAT ERROR: Chat request failed, please try again", systemAttribs);
	}
	
	void sendChatMessage(String message)
	{	
		ChatMessage chatMssg = new ChatMessage(oncUserDB.getLoggedInUser().getClientID(), chatTargetClientID, message);
		String response = chatMgr.sendMessage(this,chatMssg);
		
		if(response.equals("CHAT_MSSG_SENT"))
			displayChatText(message, clientAttribs);
		else
			displayChatText("CHAT ERROR: Failure occured sending message", systemAttribs);		
	}
	
	void endChat()
	{
		//when the dialog closes, send an end_chat message to the chat partner if chat state wasn't idle
		if(chatState != ChatState.IDLE)
		{
			ChatMessage endChat = new ChatMessage(oncUserDB.getLoggedInUser().getClientID(), chatTargetClientID, "");
		
			String response = chatMgr.endChat(this, endChat);
			if(response.equals("CHAT_END_SENT"))
			{
				//an error occurred with sending the chat end request
			}
		}
	}
	
	void onAcceptChatRequest()
	{
		//send message back to requester that chat accepted
		String response = "SERVER_COMMUNICATION_ERROR";
				
		ChatMessage retMssg = new ChatMessage(oncUserDB.getLoggedInUser().getClientID(), chatTargetClientID);
			
		response = chatMgr.acceptChat(this, retMssg);		
		if(response.equals("CHAT_ACCEPTED_SENT"))
		{
			ONCUser user = getChatPartnerUser(chatTargetClientID);
			this.setTitle("Chatting with " + user.getFirstname());
			
			lblChatters.setText("Chatting with:");
			displayChatText("You're now chatting with " + user.toString(), systemAttribs);
			
			chatState = ChatState.ACTIVE;
			dataTF.setEditable(true);
			
			contentPane.remove(requestpanel);
			contentPane.add(entrypanel);
		}
		else
		{
			//We have an error condition, either the server is down or the server could not find the
			//client to accept
		}
	}
	
	void onRejectChatRequest()
	{
		//send message back to requester that chat accepted
		String response = "SERVER_COMMUNICATION_ERROR";
				
		ChatMessage retMssg = new ChatMessage(oncUserDB.getLoggedInUser().getClientID(), chatTargetClientID);
			
		response = chatMgr.rejectChat(this, retMssg);		
		if(response.equals("CHAT_END_SENT"))
		{
			chatState = ChatState.IDLE;
			this.dispose();
		}
		else 
		{
			//We have an error condition, either the server is down or the server could not find the
			//client to accept
		}
	}
	
	/*************************************************************************************
	 * Helper method to find chat partner ONCUser
	 ************************************************************************************/
	ONCUser getChatPartnerUser(long chatPartnerClientID)
	{
		//find the target client ID in the list of onlineUsers
		int index = 1;
		while(index < chattersCBM.getSize() && ((ONCUser) chattersCBM.getElementAt(index)).getClientID() != chatTargetClientID)
			index++;
		
		if(index < chattersCBM.getSize())
			return (ONCUser) chattersCBM.getElementAt(index);
		else
			return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == chattersCB && !bIgnoreChatCBRequests)
		{
			if(chattersCB.getSelectedIndex() > 0)	//user has requested a chat
				sendChatRequest();
		}
		else if(e.getSource() == dataTF)
		{
			if(!dataTF.getText().isEmpty())
			{
				sendChatMessage(oncUserDB.getUserFNLI() + ": " + dataTF.getText());
				dataTF.setText("");
			}
		}
		else if(e.getSource() == btnAccept)
		{
			onAcceptChatRequest();
		}
		else if(e.getSource() == btnReject)
		{
			onRejectChatRequest();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("CHAT_MESSAGE"))		
		{
			//if the state is ACTIVE and the sender is the chat partner, display the name of the sender and the message
			ChatMessage chatMssg = (ChatMessage) dbe.getObject1();
			if(chatState == ChatState.ACTIVE && chatMssg.getSenderClientID() == chatTargetClientID)
			{
				displayChatText(chatMssg.getMessage(), partnerAttribs);
				try {
					SoundUtils.tone(500,75);
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("CHAT_ACCEPTED"))		
		{
			//if the state is REQUESTED and the id matches the id of the requested chat target
			//set the state to ACTIVE
			ChatMessage chatAcceptMssg = (ChatMessage) dbe.getObject1();
			
			if(chatState == ChatState.REQUESTED && chatAcceptMssg.getSenderClientID() == chatTargetClientID)
			{
				chatState = ChatState.ACTIVE;
				this.setTitle("Chatting with " + getChatPartnerUser(chatTargetClientID).getFirstname());
				lblChatters.setText("Chatting with:");
				displayChatText("Chat request accepted", systemAttribs);
				dataTF.setEditable(true);
			}			
		}
		else if(dbe.getSource() != this && dbe.getType().equals("CHAT_ENDED"))		
		{
			ChatMessage chatMssg = (ChatMessage) dbe.getObject1();
			
			//if the state is REQUESTED, then user has denied chat, reset to IDLE be able to request another chat
			if(chatState == ChatState.REQUESTED && chatMssg.getSenderClientID() == chatTargetClientID)
			{
				//target denied chat request, inform and reset
				chatState = ChatState.IDLE;
				this.setTitle("Chat");
				chattersCB.setSelectedIndex(0);
				chattersCB.setEnabled(true);
				bIgnoreChatCBRequests = false;
				
				String username = getChatPartnerUser(chatTargetClientID).toString();
				displayChatText("Chat request not accepted by " + username +
						", Select another elf to chat with using the above list", systemAttribs);
			}
			//if the state is active and the id matches the id of the requested chat target, close the dialog
			else if(chatState == ChatState.ACTIVE && chatMssg.getSenderClientID() == chatTargetClientID)
			{
				this.setVisible(false);
				this.dispose();
			}			
		}
	}
	
	private enum ChatState { IDLE, REQUESTED, ACTIVE }
}
