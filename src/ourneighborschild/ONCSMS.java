package ourneighborschild;

import java.util.ArrayList;

public class ONCSMS extends ONCObject
{
	private String messageSID;
	private EntityType type;
	private int	entityID;
	private String phoneNum;
	private SMSDirection direction;
	private String body;
	private String mediaURL;
	private SMSStatus status;
	private long timestamp;
	
	public ONCSMS(int id, String messageSID, EntityType type, int entityID, String phoneNum, SMSDirection direction,
					String body, SMSStatus status)
	{
		super(id);
		this.messageSID = messageSID;
		this.type = type;
		this.entityID = entityID;
		this.phoneNum = phoneNum;
		this.direction = direction;
		this.body = body;
		this.mediaURL = null;
		this.status = status;
		this.timestamp = System.currentTimeMillis();
	}
	
	public ONCSMS(int id, String messageSID, EntityType type, int entityID, String phoneNum, SMSDirection direction,
			String body, String mediaURL, SMSStatus status)
    {
        super(id);
        this.messageSID = messageSID;
        this.type = type;
        this.entityID = entityID;
        this.phoneNum = phoneNum;
        this.direction = direction;
        this.body = body;
        this.mediaURL = mediaURL;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }
	
	//Constructor used when importing data base from CSV by the server
	public ONCSMS(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.messageSID = nextLine[1].isEmpty() ?  "" : nextLine[1];
		this.type = EntityType.valueOf(nextLine[2].toUpperCase());
		this.entityID = Integer.parseInt(nextLine[3]);
		this.phoneNum = nextLine[4];
		this.direction = SMSDirection.valueOf(nextLine[5].toUpperCase());
		this.body = nextLine[6];
		this.status = SMSStatus.valueOf(nextLine[7].toUpperCase());
		this.timestamp = Long.parseLong(nextLine[8]);
	}
	
	public String getMessageSID() { return messageSID; }
	EntityType getType() { return type; }
	public int getEntityID() { return entityID; }
	public String getPhoneNum() { return phoneNum; }
	public SMSDirection getDirection() { return direction; }
	public String getBody() { return body; }
	public String getMediaURL() { return mediaURL; }
	public SMSStatus getStatus() { return status; }
	long getTimestamp() { return timestamp; }
	
	public void setMessageSID(String messageSID) { this.messageSID = messageSID; }
	void setType(EntityType type) { this.type = type; }
	void setEntityID(int entityID) { this.entityID = entityID; }
	public void setPhoneNumber(String phoneNum) { this.phoneNum = phoneNum; }
	public void setDirection(SMSDirection direction) {this.direction = direction; }
	void setBody(String body) { this.body = body; }
	void setMediaURL(String url) { this.mediaURL = url; }
	public void setStatus(SMSStatus status) { this.status = status; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	
	@Override
	public String[] getExportRow()
	{
		ArrayList<String> row = new ArrayList<String>();
		row.add(Integer.toString(id));
		row.add(messageSID);
		row.add(type.toString());
		row.add(Integer.toString(entityID));
		row.add(phoneNum);
		row.add(direction.toString());
		row.add(body);
		row.add(status.toString());
		row.add(Long.toString(timestamp));
		
		return row.toArray(new String[row.size()]);
	}
	
	@Override
	public String toString()
	{
		return String.format("ONCSMS: id=%d, messageSID= %s, entityType=%s, entityID=%d, phoneNum=%s, direction=%s, body=%s, status=%s, timestamp=%s",
				id, messageSID, type.toString(), entityID, phoneNum, direction.toString(), body, status.toString(), timestamp);
	}
	
	public static String[] getExportRowHeader()
	{
		return new String[] {"ID", "Message SID", "Entity Type", "Entity ID", "Phone #", "Direction", "Body", "Status", "Timestamp"};
	}
}
