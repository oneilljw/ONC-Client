package ourneighborschild;

import java.util.List;

public class SMSRequest
{
	private int year;
	private int messageID;	//if -1, send message; else which message to send to multiple phone numbers
	private String message;
	private boolean bAttachment;
	private int phoneChoice;	//which phone number to use 0=primary, 1=alternate
	private EntityType type;
	private List<Integer> entityIDList;
	
	SMSRequest(int year, int messageID, int phoneChoice, EntityType type, List<Integer> entityIDList)
	{
		this.year = year;
		this.messageID = messageID;
		this.message = null;
		this.bAttachment = false;
		this.phoneChoice = phoneChoice;
		this.type = type;
		this.entityIDList = entityIDList;
	}
	
	SMSRequest(int year, String message, boolean bAttachment, int phoneChoice, EntityType type, List<Integer> entityIDList)
	{
		this.year = year;
		this.messageID = -1;
		this.message = message;
		this.bAttachment = bAttachment;
		this.phoneChoice = phoneChoice;
		this.type = type;
		this.entityIDList = entityIDList;
	}
	
	//getters
	public int getYear() { return year; }
	public int getMessageID() { return messageID; }
	public String getMessage() { return message; }
	public boolean hasAttachment() { return bAttachment; }
	public int getPhoneChoice() { return phoneChoice; }
	public EntityType getEntityType() { return type; }
	public List<Integer> getEntityIDList() { return entityIDList; }
}
