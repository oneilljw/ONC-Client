package ourneighborschild;

public class ChatMessage 
{
	protected long senderClientID;
	protected long receiverClientID;
	protected String message;
	
	ChatMessage(long senderClientID, long receiverClientID, String mssg)
	{
		this.senderClientID = senderClientID;
		this.receiverClientID = receiverClientID;
		message = mssg;
	}
	
	ChatMessage(long senderClientID, long receiverClientID)
	{
		this.senderClientID = senderClientID;
		this.receiverClientID = receiverClientID;
		message = "";
	}
	
	public long getSenderClientID() { return senderClientID; }
	public long getReceiverClientID() { return receiverClientID; }
	public String getMessage() { return message; }	
}
