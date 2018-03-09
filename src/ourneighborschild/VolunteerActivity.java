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
	private int 	   geniusID;
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
	
	public VolunteerActivity(int id, int genisuID, String category, String name, String startDate, 
								String startTime, String endDate, String endTime, String location, 
								String description, String volComment,
								boolean bOpen, boolean bEmailReminder, String username) 
	{
		super(id, new Date(), username, 3, "New Activity", username);
		this.geniusID = geniusID;
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
		super(activity.id, activity.dateChanged.getTimeInMillis(), activity.changedBy, activity.slPos,
				activity.slMssg, activity.slChangedBy);
		this.geniusID = activity.geniusID;
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
		super(Integer.parseInt(line[0]), Long.parseLong(line[12]), line[13],
				Integer.parseInt(line[14]), line[15], line[16]);
		
		this.geniusID = line[1].isEmpty() ? -1 : Integer.parseInt(line[1]);
		this.category = line[2];
		this.name = line[3];
		this.startDate = line[4];
		this.startTime = line[5];
		this.endDate = line[6];
		this.endTime = line[7];
		this.location = line[8];
		this.description = line[9];
		this.volComment = "";
		this.bOpen = !line[10].isEmpty() && line[10].charAt(0) == 'T' ? true : false;
		this.bEmailReminder = !line[11].isEmpty() && line[11].charAt(0) == 'T' ? true : false;
		
//		System.out.println(String.format("VolAct: id %d, start time %s end time %s", id, startTime, endTime));
	}
	
	//getters
	public int getGeniusID()  { return geniusID; }
	String getCategory() { return category; }
	public String getName() { return name; }
	public String getStartDate() { return startDate; }
	public String getStartTime() { return startTime; }
	public String getEndDate() { return endDate; }
	String getEndTime() { return endTime; }
	Calendar getStartCal() { return createCal(startDate, startTime); }
	Calendar getEndCal() { return createCal(endDate, endTime); }
	public String getLocation() { return location; }
	String getDescription() { return description; }
	public String getComment() { return volComment; }
	public boolean isOpen() { return bOpen; }
	public boolean sendReminder() { return bEmailReminder; }
	
	//setters
	public void setGeniusID(int gID) { this.geniusID = gID; }
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
	
	/***
	 * Convert Volunteer Activity date and time class variables to a Calendar object.
	 * Note: Start and End times are strings formatted with AM or PM designators at the end
	 * @param date
	 * @param time
	 * @return
	 */
	Calendar createCal(String date, String time)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy h:mm a");
		
		try
		{
			cal.setTime(sdf.parse(String.format("%s %s", date, time)));
			return cal;
		}
		catch (ParseException e)
		{
			return cal;
		}
	}

	@Override
	public String[] getExportRow() 
	{
		String[] row = new String[17];
		
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(geniusID);
		row[2] = category;
		row[3] = name;
		row[4] = startDate;
		row[5] = startTime;
		row[6] = endDate;
		row[7] = endTime;
		row[8] = location;
		row[9] = description;
		row[10] = bOpen ? "T" : "F";
		row[11] = bEmailReminder ? "T" : "F";
		row[12] = Long.toString(dateChanged.getTimeInMillis());
		row[13] = changedBy;
		row[14] = Integer.toString(slPos);
		row[15] = slMssg;
		row[16] = slChangedBy;
		
		return row;
	}
}
