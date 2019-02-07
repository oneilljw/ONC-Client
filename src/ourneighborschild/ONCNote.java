package ourneighborschild;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ONCNote extends ONCEntity
{
	/**
	 * Note object used within the ONC Data Management System
	 */
	private static final long serialVersionUID = 1L;
	public static final int SENT = 1;	//black
	public static final int READ = 2;	//blue
	public static final int RESPONDED = 3;	//green
	public static final int LATE = 4;	//red
	
	private int ownerID;
	private int status;
	private String title;
	private String note;
	private String response;
	private String respondedBy;
	private Calendar timeViewed;		//should always be UTC
	private Calendar timeResponse;	//should always be UTC
	
	public ONCNote(int id, int ownerID, String title, String note)
	{
		super(id, new Date(), "", 0, "Note Created", "");
		this.ownerID = ownerID;
		this.status = ONCNote.SENT;
		this.title = title;
		this.note = note;
		this.response = "";
		this.respondedBy = "";
		this.timeViewed = Calendar.getInstance();
		this.timeViewed.setTimeInMillis(0);
		this.timeResponse = Calendar.getInstance();
		this.timeResponse.setTimeInMillis(0);
	}
	
	public ONCNote(ONCNote n)
	{
		super(n.id, new Date(), n.changedBy, n.slPos, n.slMssg, n.slChangedBy);
		this.ownerID = n.ownerID;
		this.status = n.status;
		this.title = n.title;
		this.note = n.note;
		this.response = n.response;
		this.respondedBy = n.respondedBy;
		this.timeViewed = Calendar.getInstance();
		this.timeViewed.setTimeInMillis(n.timeViewed.getTimeInMillis());
		this.timeResponse = Calendar.getInstance();
		this.timeResponse.setTimeInMillis(n.timeViewed.getTimeInMillis());
	}
	
	public ONCNote(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[8]), line[5],
				Integer.parseInt(line[11]), line[12], line[13]);
		
		this.ownerID = line[1].isEmpty() ? 0 : Integer.parseInt(line[1]);
		this.status= line[2].isEmpty() ? 0 : Integer.parseInt(line[2]);
		this.title = line[3];
		this.note = line[4];
		this.response = line[6];
		this.respondedBy = line[7];
		
		TimeZone tz = TimeZone.getDefault();	//Local time zone
		int offsetFromUTC;
		
		this.timeViewed = Calendar.getInstance();
		if(!line[9].isEmpty())
		{
			offsetFromUTC = tz.getOffset(Long.parseLong(line[9]));
			this.timeViewed.setTimeInMillis(Long.parseLong(line[9]));
			this.timeViewed.add(Calendar.MILLISECOND, offsetFromUTC);
		}
		
		this.timeResponse = Calendar.getInstance();
		if(!line[10].isEmpty())
		{
			offsetFromUTC = tz.getOffset(Long.parseLong(line[10]));
			this.timeResponse.setTimeInMillis(Long.parseLong(line[10]));
			this.timeResponse.add(Calendar.MILLISECOND, offsetFromUTC);
		}
	}
	
	public ONCNote()
	{
		super(-1, new Date(), "", 0, "", "");
		this.ownerID = -1;
		this.status = ONCNote.SENT;
		this.title = "";
		this.note = "";
		this.response = "";
		this.respondedBy = "";
		this.timeViewed = Calendar.getInstance();
		this.timeViewed.setTimeInMillis(0);
		this.timeResponse = Calendar.getInstance();
		this.timeResponse.setTimeInMillis(0);
	}
	
	//getters
	public int getOwnerID() { return ownerID; }
	public int getStatus() { return status; }
	String getTitle() { return title; }
	String getNote() { return note; }
	String getResponse() { return response; }
	String getRespondedBy() { return respondedBy; }
	Calendar getTimeViewed() { return timeViewed; }
	Calendar getTimeResponse() { return timeResponse; }
	
	//setters
	void setStatus(int status) { this.status = status; }
	void setTitle(String title) { this.title = title; }
	void setNote(String note) { this.note = note; }
	public void noteViewed(String viewedBy)
	{
		this.timeViewed = Calendar.getInstance();
		if(this.status == SENT)
		{
			this.respondedBy = viewedBy;
			this.status = READ;
		}
	}
	public void setResponse(String response, String respondedBy)
	{
		this.response = response;
		this.respondedBy = respondedBy;
		this.status = RESPONDED;
		this.timeResponse = Calendar.getInstance();
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = new String[14];
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(ownerID);
		row[2] = Integer.toString(status);
		row[3] = title;
		row[4] = note;
		row[5] = changedBy;
		row[6] = response;
		row[7] = respondedBy;
		row[8] = Long.toString(dateChanged.getTimeInMillis());
		row[9] = Long.toString(timeViewed.getTimeInMillis());
		row[10] = Long.toString(timeResponse.getTimeInMillis());
		row[11] = Integer.toString(slPos);
		row[12] = slMssg;
		row[13] = slChangedBy;
		return row;
	}
}
