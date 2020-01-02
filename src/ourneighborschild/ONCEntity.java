package ourneighborschild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public abstract class ONCEntity extends ONCObject implements Serializable
{
	/**
	 * This pojo class subclasses ONCObjects, adding stop light data as well as data change tracking
	 * It is a superclass for all ONC pojo classes that require both change tracking and a stop light
	 */
	private static final long serialVersionUID = -1348547623757168307L;
	protected static final int STOPLIGHT_RED = 2;
	protected static final int STOPLIGHT_OFF = 3;
	
	protected long timestamp;
	protected String changedBy;
	protected int slPos;
	protected String slMssg;
	protected String slChangedBy;
	
	public ONCEntity(int id, long today, String changedBy, int slpos, String slmssg, String slchgby)
	{
		super(id);
		
//		TimeZone tz = TimeZone.getDefault();	//Local time zone
//		int offsetFromUTC;
//		
//		this.timestamp = Calendar.getInstance();
//		if(today != null && today.getTime() > 0)
//		{
//			offsetFromUTC = tz.getOffset(today.getTime());
//			this.timestamp.setTimeInMillis(today.getTime());
//			this.timestamp.add(Calendar.MILLISECOND, offsetFromUTC);
//		}
//		
//		dateChanged = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		dateChanged.setTime(today);
		
		this.timestamp = today;
		this.changedBy = changedBy;
		slPos = slpos;
		slMssg = slmssg;
		slChangedBy = slchgby;		
	}
	
	//copy constructor
	public ONCEntity(ONCEntity e)
	{
		super(e.getID());
		this.timestamp = e.timestamp;
		this.changedBy = e.changedBy;
		this.slPos = e.slPos;
		this.slMssg = e.slMssg;
		this.slChangedBy = e.slChangedBy;
	}
	
//	public ONCEntity(int id, long timeInMillis, String changedBy, int slpos, String slmssg, String slchgby)
//	{
//		super(id);
//		this.timestamp = timeInMillis;
//		this.changedBy = changedBy;
//		this.slPos = slpos;
//		this.slMssg = slmssg;
//		this.slChangedBy = slchgby;		
//	}
	
//	public ONCEntity(int id, Calendar dateChanged, String changedBy, int slpos, String slmssg, String slchgby)
//	{
//		super(id);
//		this.timestamp = dateChanged;
//		this.changedBy = changedBy;
//		this.slPos = slpos;
//		this.slMssg = slmssg;
//		this.slChangedBy = slchgby;		
//	}
	
	//getters
	public Date getTimestampDate()  
	{ 
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(timestamp);
		return calendar.getTime();
	}
	public long getTimestamp() { return timestamp; }
	public String getChangedBy() { return changedBy; }
	public int getStoplightPos() { return slPos; }
	public String getStoplightMssg() { return slMssg; }
	public String getStoplightChangedBy() { return slChangedBy; }
		
	//setters
	public void setDateChanged(long timeInMillis) { this.timestamp = timeInMillis; }
//	public void setDateChanged(Date  dc)	{ timestamp.setTime(dc); }
	public void setChangedBy(String cb) { changedBy = cb; }
	public void setStoplightPos(int slp) { slPos = slp; }
	public void setStoplightMssg(String s) { slMssg = s; }
	public void setStoplightChangedBy(String s) { slChangedBy = s; }
}
