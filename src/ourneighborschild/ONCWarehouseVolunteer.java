package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ONCWarehouseVolunteer extends ONCObject 
{
	private int volunteerID;
	private Calendar timestamp;
	
	public ONCWarehouseVolunteer(int id, int volID, Date ts) 
	{
		super(id);
		this.volunteerID = volID;
		timestamp = Calendar.getInstance();
		timestamp.setTime(ts);
	}
	
	public ONCWarehouseVolunteer(String nextLine[])
	{
		super(Integer.parseInt(nextLine[0]));
		this.volunteerID = Integer.parseInt(nextLine[1]);
		this.timestamp = Calendar.getInstance();
		timestamp.setTimeInMillis(Long.parseLong(nextLine[2])); 
	}
	
	//getters
	public int getVolunteerID() { return volunteerID; }
	public Date getTimestamp() { return timestamp.getTime(); }

	@Override
	public String[] getExportRow() 
	{
		String[] row = new String[3];
		
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(volunteerID);
		row[2] = Long.toString(timestamp.getTimeInMillis());
		
		return row;
	}
}
