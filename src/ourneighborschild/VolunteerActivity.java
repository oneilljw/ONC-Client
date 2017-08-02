package ourneighborschild;

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
	private Calendar startTime;
	private Calendar endTime;
	private String location;
	private String description;
	
	public VolunteerActivity(int id, String category, String name, Calendar startTime,
								Calendar endTime, String location, String description, String username) 
	{
		super(id, new Date(), username, 3, "New Activity", username);
		this.category = category;
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.location = location;
		this.description = description;
	}
	
	public VolunteerActivity(VolunteerActivity activity)
	{
		super(activity.id, activity.dateChanged.getTimeInMillis(), activity.changedBy, activity.slPos, activity.slMssg, activity.slChangedBy);
		this.category = activity.category;
		this.name = activity.name;
		this.startTime = activity.startTime;
		this.endTime = activity.endTime;
		this.location = activity.location;
		this.description = activity.description;
	}
	
	public VolunteerActivity(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[7]), line[8],
				Integer.parseInt(line[9]), line[10], line[11]);
		
		this.category = line[1];
		this.name = line[2];
		
		this.startTime = Calendar.getInstance();
		startTime.setTimeInMillis(Long.parseLong(line[3]));
		
		this.endTime = Calendar.getInstance();
		endTime.setTimeInMillis(Long.parseLong(line[4]));
		
		this.location = line[5];
		this.description = line[6];
	}
	
	//getters
	String getCategory() { return category; }
	public String getName() { return name; }
	public Calendar getStartTime() { return startTime; }
	public Calendar getEndTime() { return endTime; }
	String getLocation() { return location; }
	String getDescription() { return description; }
	
	//setters
	void setCategory(String category) { this.category = category; }
	void setName(String name) { this.name = name; }
	public void setStartTime(Calendar startTime) { this.startTime = startTime; }
	public void setEndTime(Calendar endTime) { this.endTime = endTime; }
	void setLocation(String location) { this.location = location; }
	void setDescription(String description) { this.description = description; }

	@Override
	public String[] getExportRow() 
	{
		String[] row = new String[7];
		
		row[0] = Integer.toString(id);
		row[1] = category;
		row[2] = name;
		row[3] = Long.toString(startTime.getTimeInMillis());
		row[4] = Long.toString(endTime.getTimeInMillis());
		row[5] = location;
		row[6] = description;
		
		return row;
	}
}
