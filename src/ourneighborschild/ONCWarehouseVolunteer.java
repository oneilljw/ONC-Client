package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ONCWarehouseVolunteer extends ONCObject 
{
	private int volunteerID;
	private String group;
	private String comment;
	private Calendar timestamp;
	
	public ONCWarehouseVolunteer(int id, int volID, String group, String comment, Date ts) 
	{
		super(id);
		this.volunteerID = volID;
		this.group = group;
		this.comment = comment;
		timestamp = Calendar.getInstance();
		timestamp.setTime(ts);
	}
	
	public ONCWarehouseVolunteer(String nextLine[])
	{
		super(Integer.parseInt(nextLine[0]));
		this.volunteerID = Integer.parseInt(nextLine[1]);
		this.group = nextLine[2];
		this.comment = nextLine[3];
		this.timestamp = Calendar.getInstance();
		timestamp.setTimeInMillis(Long.parseLong(nextLine[4])); 
	}
	
	//getters
	public int getVolunteerID() { return volunteerID; }
	public Date getTimestamp() { return timestamp.getTime(); }
	public Calendar getCalTimestamp() { return timestamp; }
	public String getGroup() { return group; }
	public String getComment() { return comment; }

	@Override
	public String[] getExportRow() 
	{
		String[] row = new String[5];
		
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(volunteerID);
		row[2] = group;
		row[3] = comment;
		row[4] = Long.toString(timestamp.getTimeInMillis());
		
		return row;
	}
}
