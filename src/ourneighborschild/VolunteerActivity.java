package ourneighborschild;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * Basic java object that describes activities that ONC volunteers can sign up to perform
 * @author johnoneill
 */
public class VolunteerActivity extends ONCEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String category;
	private String name;
	private String startDate;
	private String startTime;
	private String endDate;
	private String endTime;
	private String location;
	private String description;
	private String volComment;	//personalized by each volunteer for each of their activities
	private boolean bOpen;
	private boolean bEmailReminder;
	
	public VolunteerActivity(int id, String category, String name, String startDate, String startTime,
								String endDate, String endTime, String location, String description,
								String volComment,
								boolean bOpen, boolean bEmailReminder, String username) 
	{
		super(id, new Date(), username, 3, "New Activity", username);
		this.category = category;
		this.name = name;
		this.startDate = startDate;
		this.startTime = startTime;
		this.endDate = endDate;
		this.endTime = endTime;
		this.location = location;
		this.description = description;
		this.volComment = volComment;
		this.bOpen = bOpen;
		this.bEmailReminder = bEmailReminder;
	}
	
	public VolunteerActivity(VolunteerActivity activity)
	{
		super(activity.id, activity.dateChanged.getTimeInMillis(), activity.changedBy, activity.slPos, activity.slMssg, activity.slChangedBy);
		this.category = activity.category;
		this.name = activity.name;
		this.startDate = activity.startDate;
		this.startTime = activity.startTime;
		this.endDate = activity.endDate;
		this.endTime = activity.endTime;
		this.location = activity.location;
		this.description = activity.description;
		this.volComment = activity.volComment;
		this.bOpen = activity.bOpen;
		this.bEmailReminder = activity.bEmailReminder;
	}
	
	public VolunteerActivity(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[11]), line[12],
				Integer.parseInt(line[13]), line[14], line[15]);
		
		this.category = line[1];
		this.name = line[2];
		this.startDate = line[3];
		this.startTime = line[4];
		this.endDate = line[5];
		this.endTime = line[6];
		this.location = line[7];
		this.description = line[8];
		this.volComment = "";
		this.bOpen = !line[9].isEmpty() && line[9].charAt(0) == 'T' ? true : false;
		this.bEmailReminder = !line[10].isEmpty() && line[10].charAt(0) == 'T' ? true : false;
		
//		System.out.println(String.format("VolAct: id %d, start time %s end time %s", id, startTime, endTime));
	}
	
	//getters
	String getCategory() { return category; }
	public String getName() { return name; }
	public String getStartDate() { return startDate; }
	public String getStartTime() { return startTime; }
	public String getEndDate() { return endDate; }
	String getEndTime() { return endTime; }
	public String getLocation() { return location; }
	String getDescription() { return description; }
	public String getComment() { return volComment; }
	public boolean isOpen() { return bOpen; }
	public boolean sendReminder() { return bEmailReminder; }
	
	//setters
	void setCategory(String category) { this.category = category; }
	void setName(String name) { this.name = name; }
	public void setStartDate(String startDate) { this.startDate = startDate; }
	public void setStartTime(String startTime) { this.startTime = startTime; }
	public void setEndDate(String endDate) { this.endDate = endDate; }
	public void setEndTime(String endTime) { this.endTime = endTime; }
	void setLocation(String location) { this.location = location; }
	public void setComment(String volComment) { this.volComment = volComment; }
	void setDescription(String description) { this.description = description; }
	void setOpen (boolean bOpen) { this.bOpen = bOpen; }
	void setReminder (boolean bRemind) { this.bEmailReminder = bRemind; }
	
	Calendar getStartCal()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:MM a");
		Calendar cal = Calendar.getInstance();
		
		try
		{
			
			cal.setTime(sdf.parse(startDate + " " + startTime));
			cal.set(Calendar.MILLISECOND, 0);
			System.out.println("VolAct.getStartCal: " + startDate + " " + startTime +" cal= " + cal);
		}
		catch (ParseException e)
		{
			return cal;
		}
		
		return cal;
	}
	
	Calendar getEndCal()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:MM a");
		Calendar cal = Calendar.getInstance();
		
		try
		{
			cal.setTime(sdf.parse(endDate + " " + endTime));
			cal.set(Calendar.MILLISECOND, 0);
		}
		catch (ParseException e)
		{
			return cal;
		}
		
		return cal;
	}

	@Override
	public String[] getExportRow() 
	{
		String[] row = new String[16];
		
		row[0] = Integer.toString(id);
		row[1] = category;
		row[2] = name;
		row[3] = startDate;
		row[4] = startTime;
		row[5] = endDate;
		row[6] = endTime;
		row[7] = location;
		row[8] = description;
		row[9] = bOpen ? "T" : "F";
		row[10] = bEmailReminder ? "T" : "F";
		row[11] = Long.toString(dateChanged.getTimeInMillis());
		row[12] = changedBy;
		row[13] = Integer.toString(slPos);
		row[14] = slMssg;
		row[15] = slChangedBy;
		
		return row;
	}
}
