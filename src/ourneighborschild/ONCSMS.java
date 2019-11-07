package ourneighborschild;

import java.util.ArrayList;

public class ONCSMS extends ONCObject
{
	private EntityType type;
	private int	entityID;
	private String phoneNum;
	private SMSDirection direction;
	private String body;
	private SMSStatus status;
	private long timestamp;
	
	public ONCSMS(int id, EntityType type, int entityID, String phoneNum, SMSDirection direction,
					String body, SMSStatus status)
	{
		super(id);
		this.type = type;
		this.entityID = entityID;
		this.phoneNum = phoneNum;
		this.direction = direction;
		this.body = body;
		this.status = status;
		this.timestamp = System.currentTimeMillis();
	}
	
	//Constructor used when importing data base from CSV by the server
	public ONCSMS(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.type = EntityType.valueOf(nextLine[1]);
		this.entityID = Integer.parseInt(nextLine[2]);
		this.phoneNum = nextLine[3];
		this.direction = SMSDirection.valueOf(nextLine[4]);
		this.body = nextLine[5];
		this.status = SMSStatus.valueOf(nextLine[6]);
		this.timestamp = Long.parseLong(nextLine[7]);
	}
	
	EntityType getType() { return type; }
	int getEntityID() { return entityID; }
	public String getPhoneNum() { return phoneNum; }
	public SMSDirection getDirection() { return direction; }
	public String getBody() { return body; }
	public SMSStatus getStatus() { return status; }
	long getTimestamp() { return timestamp; }
	
	void setType(EntityType type) { this.type = type; }
	void setEntityID(int entityID) { this.entityID = entityID; }
	public void setPhoneNumber(String phoneNum) { this.phoneNum = phoneNum; }
	public void setDirection(SMSDirection direction) {this.direction = direction; }
	void setBody(String body) { this.body = body; }
	public void setStatus(SMSStatus status) { this.status = status; }
	

	@Override
	public String[] getExportRow()
	{
		ArrayList<String> row = new ArrayList<String>();
		row.add(Integer.toString(id));
		row.add(type.toString());
		row.add(Integer.toString(entityID));
		row.add(phoneNum);
		row.add(direction.toString());
		row.add(body);
		row.add(status.toString());
		row.add(Long.toString(timestamp));
		
		return row.toArray(new String[row.size()]);
	}
}
