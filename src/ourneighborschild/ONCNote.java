package ourneighborschild;

import java.util.Calendar;
import java.util.TimeZone;

public class ONCNote extends ONCEntity
{
	/**
	 * Note object used within the ONC Data Management System
	 */
	private static final long serialVersionUID = 1L;
	public static final int UNREAD = 0;
	public static final int READ = 1;
	public static final int RESONDED = 2;
	
	private int ownerID;
	private int status;
	private String note;
	private String response;
	private String respondedBy;
	private Calendar timeViewed;		//should always be UTC
	private Calendar timeResponse;	//should always be UTC
	
	public ONCNote(int id, int ownerID, int status, String note, String createdBy  )
	{
		super(id, Calendar.getInstance().getTime(), createdBy, 0, "Note Created", createdBy);
		this.ownerID = ownerID;
		this.status = status;
		this.note = note;
		this.response = "";
		this.respondedBy = "";
		this.timeViewed = Calendar.getInstance();
		this.timeResponse = Calendar.getInstance();
	}
	
	public ONCNote(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[7]), line[4],
				Integer.parseInt(line[10]), line[11], line[12]);
		
		this.ownerID = line[1].isEmpty() ? 0 : Integer.parseInt(line[1]);
		this.status= line[2].isEmpty() ? 0 : Integer.parseInt(line[2]);
		this.note = line[3];
		this.response = line[5];
		this.respondedBy = line[6];
		
		TimeZone tz = TimeZone.getDefault();	//Local time zone
		int offsetFromUTC;
		
		this.timeViewed = Calendar.getInstance();
		if(!line[8].isEmpty())
		{
			offsetFromUTC = tz.getOffset(Long.parseLong(line[8]));
			this.timeViewed.setTimeInMillis(Long.parseLong(line[8]));
			this.timeViewed.add(Calendar.MILLISECOND, offsetFromUTC);
		}
		
		this.timeResponse = Calendar.getInstance();
		if(!line[9].isEmpty())
		{
			offsetFromUTC = tz.getOffset(Long.parseLong(line[9]));
			this.timeViewed.setTimeInMillis(Long.parseLong(line[9]));
			this.timeViewed.add(Calendar.MILLISECOND, offsetFromUTC);
		}
	}
	
	//getters
	int getOwnerID() { return ownerID; }
	int getStatus() { return status; }
	String getNote() { return note; }
	String getResponse() { return response; }
	String getRespondedBy() { return respondedBy; }
	Calendar getTimeViewed() { return timeViewed; }
	Calendar getTimeResponse() { return timeResponse; }
	
	//setters
	void setStatus(int status) { this.status = status; }
	void setNote(String note) { this.note = note; }
	void setTimeViewed() { this.timeViewed = Calendar.getInstance(); }
	void setResponse(String response, String respondedBy)
	{
		this.response = response;
		this.respondedBy = respondedBy;
		this.timeResponse = Calendar.getInstance();
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = new String[13];
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(ownerID);
		row[2] = Integer.toString(status);
		row[3] = note;
		row[4] = changedBy;
		row[5] = response;
		row[6] = respondedBy;
		row[7] = Long.toString(dateChanged.getTimeInMillis());
		row[8] = Long.toString(timeViewed.getTimeInMillis());
		row[9] = Long.toString(timeResponse.getTimeInMillis());
		row[10] = Integer.toString(slPos);
		row[11] = slMssg;
		row[12] = slChangedBy;
		return row;
	}
}
