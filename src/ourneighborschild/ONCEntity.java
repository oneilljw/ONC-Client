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
	
	protected Calendar dateChanged;
	protected String changedBy;
	protected int slPos;
	protected String slMssg;
	protected String slChangedBy;
	
	public ONCEntity(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby)
	{
		super(id);
		dateChanged = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateChanged.setTime(today);
		this.changedBy = changedBy;
		slPos = slpos;
		slMssg = slmssg;
		slChangedBy = slchgby;		
	}
	
	//copy constructor
	public ONCEntity(ONCEntity e)
	{
		super(e.getID());
		this.dateChanged = Calendar.getInstance();
		this.dateChanged = e.dateChanged;
		this.changedBy = e.changedBy;
		this.slPos = e.slPos;
		this.slMssg = e.slMssg;
		this.slChangedBy = e.slChangedBy;
	}
	
	public ONCEntity(int id, long timeInMillis, String changedBy, int slpos, String slmssg, String slchgby)
	{
		super(id);
		dateChanged = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateChanged.setTimeInMillis(timeInMillis);
		this.changedBy = changedBy;
		slPos = slpos;
		slMssg = slmssg;
		slChangedBy = slchgby;		
	}
	
	//getters
	public Date getDateChanged()	{ return dateChanged.getTime(); }
	public String getChangedBy() { return changedBy; }
	public int getStoplightPos() { return slPos; }
	public String getStoplightMssg() { return slMssg; }
	public String getStoplightChangedBy() { return slChangedBy; }
		
	//setters
	public void setDateChanged(Date  dc)	{ dateChanged.setTime(dc); }
	public void setChangedBy(String cb) { changedBy = cb; }
	public void setStoplightPos(int slp) { slPos = slp; }
	public void setStoplightMssg(String s) { slMssg = s; }
	public void setStoplightChangedBy(String s) { slChangedBy = s; }
}
