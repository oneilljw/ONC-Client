package ourneighborschild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.google.gson.Gson;

public class ChatManager extends ONCDatabase implements ActionListener
{
	private static ChatManager instance = null;
	private UserDB userDB;
	
	private ChatManager()
	{
		super();
		userDB = UserDB.getInstance();
	}
	
	public static ChatManager getInstance()
	{
		if(instance == null)
			instance = new ChatManager();
		
		return instance;
	}
	
	String requestChat(Object source, ChatMessage chatMsg)
	{
		//send chat request to server
		String response = "SERVER_COMMUNICATION_ERROR";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
					
			response = serverIF.sendRequest("POST<chat_request>" + gson.toJson(chatMsg, ChatMessage.class));		
			
		}
				
		return response;	
	}
	
	String acceptChat(Object source, ChatMessage chatMsg)
	{
		//send chat request to server
		String response = "SERVER_COMMUNICATION_ERROR";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();		
			response = serverIF.sendRequest("POST<chat_accepted>" + gson.toJson(chatMsg, ChatMessage.class));			
		}
				
		return response;	
	}
	
	String rejectChat(Object source, ChatMessage chatMsg)
	{
		//send chat request to server
		String response = "SERVER_COMMUNICATION_ERROR";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();		
			response = serverIF.sendRequest("POST<chat_ended>" + gson.toJson(chatMsg, ChatMessage.class));			
		}
				
		return response;	
	}
	
	/***************************************************************************************************
	 * When a chat request is received from the server, handle it here. Create a new chat dialog for the
	 * client user to determine whether they want to accept or reject the chat request.
	 ***************************************************************************************************/
	void processChatRequest(Object source, ChatMessage reqMssg)
	{
		//Determine if the chat request was sent to this user
		if(reqMssg.getReceiverClientID() == userDB.getLoggedInUser().getClientID())
		{
			//create and display a new chat dialog with the combo box set to the requesting user
			//and state sent to chat
			ChatDialog chatDlg = new ChatDialog(GlobalVariables.getFrame(), false, reqMssg.getSenderClientID());	//false = user accepted chat
			chatDlg.setLocationRelativeTo(GlobalVariables.getFrame());
			chatDlg.setVisible(true);
		}
	}
	
	String sendMessage(Object source, ChatMessage chatMssg)
	{
		Gson gson = new Gson();
		
		//send chat request to server
		String response = "SERVER_COMMUNICATION_ERROR";
				
		if(serverIF != null && serverIF.isConnected())
		{			
			response = serverIF.sendRequest("POST<chat_message>" + gson.toJson(chatMssg, ChatMessage.class));		
		}
		
		return response;
			
	}
	
	String endChat(Object source, ChatMessage endChat)
	{
		Gson gson = new Gson();
		
		//send chat request to server
		String response = "SERVER_COMMUNICATION_ERROR";
				
		if(serverIF != null && serverIF.isConnected())
			response = serverIF.sendRequest("POST<chat_ended>" + gson.toJson(endChat, ChatMessage.class));		
		
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().startsWith("CHAT_"))	//is the server event a chat command?
		{
			Gson gson = new Gson();
			ChatMessage chatMssg = gson.fromJson(ue.getJson(), ChatMessage.class);
			
			if(chatMssg.getReceiverClientID() == userDB.getLoggedInUser().getClientID())	//is this client the chat message target?
			{
				//The chat command was sent to this client. If it was a chat request, process it in this object.
				//All other chat commands are sent to the chat dialogs to process
				if(ue.getType().equals("CHAT_REQUESTED"))
					processChatRequest(this, chatMssg);
				else
					fireDataChanged(this, ue.getType(), chatMssg);
			}	
		}
	}

	@Override
	String update(Object source, ONCObject entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
