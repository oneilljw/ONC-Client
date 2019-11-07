package ourneighborschild;

import java.util.List;

public class SMSRequest
{
	private int year;
	private int messageID;	//which message will be sent by sms from the message library
	private int phoneChoice;	//which phone number to use 0=primary, 1=alternate
	private EntityType type;
	private List<Integer> entityIDList;
	
	SMSRequest(int year, int messageID, int phoneChoice, EntityType type, List<Integer> entityIDList)
	{
		this.year = year;
		this.messageID = messageID;
		this.phoneChoice = phoneChoice;
		this.type = type;
		this.entityIDList = entityIDList;
	}
	
	//getters
	public int getYear() { return year; }
	public int getMessageID() { return messageID; }
	public int getPhoneChoice() { return phoneChoice; }
	public EntityType getEntityType() { return type; }
	public List<Integer> getEntityIDList() { return entityIDList; }
}
