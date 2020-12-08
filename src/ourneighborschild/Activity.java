package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public class Activity extends ONCEntity
{
	/***
	 * Basic java object that describes activities that ONC volunteers can sign up for
	 * @author johnoneill
	 */
	private static final long serialVersionUID = 1L;
	public static final int VOLUNTEER_ACTIVITY_EXACT_MATCH = 3;
	public static final int VOLUNTEER_ACTIVITY_GENIUS_MATCH = 2;
	public static final int VOLUNTEER_ACTIVITY_NAME_TIME_MATCH = 1;
	public static final int VOLUNTEER_ACTIVITY_DOES_NOT_MATCH = 0;
	private int geniusID;
	private String category;
	private String name;
	private long startTimeInMillis;
	private long endTimeInMillis;
	private String location;
	private String description;
	private boolean bOpen;
	private boolean bEmailReminder;
	private int deliveryActivity;	//0 = not delivery act, 1 = delivery act, 2 = default delivery act
	
	public Activity(int id, int geniusID, String category, String name, long start, long end, 
								String location, String description, String volComment,
								boolean bOpen, boolean bEmailReminder, int deliveryActivity, String username) 
	{
		super(id, System.currentTimeMillis(), username, 3, "New Activity", username);
		this.geniusID = geniusID;
		this.category = category;
		this.name = name;
		this.startTimeInMillis = start;
		this.endTimeInMillis = end;
		this.location = location;
		this.description = description;
		this.bOpen = bOpen;
		this.bEmailReminder = bEmailReminder;
		this.deliveryActivity = deliveryActivity;
	}
	
	public Activity(Activity activity)
	{
		super(activity.id, activity.timestamp, activity.changedBy, activity.slPos,
				activity.slMssg, activity.slChangedBy);
		this.geniusID = activity.geniusID;
		this.category = activity.category;
		this.name = activity.name;
		this.startTimeInMillis = activity.startTimeInMillis;
		this.endTimeInMillis = activity.endTimeInMillis;
		this.location = activity.location;
		this.description = activity.description;
		this.bOpen = activity.bOpen;
		this.bEmailReminder = activity.bEmailReminder;
		this.deliveryActivity = activity.deliveryActivity;
	}
	
	/***
	 * Constructs a new VolunteerActivity object from a SignUp Genius activity object
	 * Note the SignUpGenius object will have start and end dates in seconds, not milliseconds
	 * @param sua
	 */
	public Activity(SignUpActivity sua)
	{
		super(-1, System.currentTimeMillis(), "Lavin, K", 3,
				"New activity from SignUpGenius", "Lavin, K");
		this.geniusID = (int) sua.getSlotitemid();
		this.category = sua.getItem();
		this.name = sua.getItem();
		this.startTimeInMillis = sua.getStartdate() * 1000; //convert seconds to millis
		this.endTimeInMillis = sua.getEnddate() * 1000;
		this.location = sua.getLocation();
		this.description = "";
		this.bOpen = false;
		this.bEmailReminder = false;
		this.deliveryActivity = 0;
	}
	
	public Activity(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[11]), line[12],
				Integer.parseInt(line[13]), line[14], line[15]);
		
		this.geniusID = line[1].isEmpty() ? -1 : Integer.parseInt(line[1]);
		this.category = line[2];
		this.name = line[3];
		this.startTimeInMillis = line[4].isEmpty() ? 0 : Long.parseLong(line[4]);
		this.endTimeInMillis = line[5].isEmpty() ? 0 : Long.parseLong(line[5]);
		this.location = line[6];
		this.description = line[7];
		this.bOpen = !line[8].isEmpty() && line[8].charAt(0) == 'T' ? true : false;
		this.bEmailReminder = !line[9].isEmpty() && line[9].charAt(0) == 'T' ? true : false;
		this.deliveryActivity = line[10].isEmpty() ? 0 : Integer.parseInt(line[10]);
	}
	
	//dummy Activity
	public Activity(int id, String name) 
	{
		super(id, System.currentTimeMillis(), "Lavin, K", 3, "Default Delivery Activity", "Lavin, K");
		this.geniusID = -1;
		this.category = "";
		this.name = name;
		this.startTimeInMillis = 0;
		this.endTimeInMillis = 0;
		this.location = "";
		this.description = "";
		this.bOpen = false;
		this.bEmailReminder = false;
		this.deliveryActivity = 0;
}
	
	//getters
	public int getGeniusID()  { return geniusID; }
	String getCategory() { return category; }
	public String getName() { return name; }
	public long getStartDate() { return startTimeInMillis; }
	public long getEndDate() { return endTimeInMillis; }
	public String getLocation() { return location; }
	String getDescription() { return description; }
	public boolean isOpen() { return bOpen; }
	public boolean sendReminder() { return bEmailReminder; }
	public boolean isDeliveryActivity() { return deliveryActivity > 0; }
	public boolean isDefaultDeliveryActivity() { return deliveryActivity == 2; }
	
	//setters
	public void setGeniusID(int gID) { this.geniusID = gID; }
	void setCategory(String category) { this.category = category; }
	void setName(String name) { this.name = name; }
	public void setStartDate(long startDate) { this.startTimeInMillis = startDate; }
	public void setEndDate(long endDate) { this.endTimeInMillis = endDate; }
	void setLocation(String location) { this.location = location; }
	void setDescription(String description) { this.description = description; }
	void setOpen (boolean bOpen) { this.bOpen = bOpen; }
	void setReminder (boolean bRemind) { this.bEmailReminder = bRemind; }
//	void setDeliveryActivity(boolean bDeliveryActivity) { this.deliveryActivity = bDeliveryActivity ? 1 : 0; }
	void setDeliveryActivity(boolean bDeliveryActivity, boolean bDefaultDeliveryActivity)
	{ 
		deliveryActivity = bDefaultDeliveryActivity ? 2 : bDeliveryActivity ? 1 : 0; 
	}

	@Override
	public String[] getExportRow() 
	{
		List<String> row = new ArrayList<String>();
		
		row.add(Integer.toString(id));
		row.add(Integer.toString(geniusID));
		row.add(category);
		row.add(name);
		row.add(Long.toString(startTimeInMillis));
		row.add(Long.toString(endTimeInMillis));
		row.add(location);
		row.add(description);
		row.add(bOpen ? "T" : "F");
		row.add(bEmailReminder ? "T" : "F");
		row.add(Integer.toString(deliveryActivity));
		row.add(Long.toString(timestamp));
		row.add(changedBy);
		row.add(Integer.toString(slPos));
		row.add(slMssg);
		row.add(slChangedBy);
		
		return row.toArray(new String[row.size()]);
	}
	
	public int compareActivities(Activity va)
	{
		int match = VOLUNTEER_ACTIVITY_DOES_NOT_MATCH;
		
		if(va.getName().equals(this.name) && 
			va.getStartDate() == this.startTimeInMillis && va.getEndDate() == this.endTimeInMillis)
			match = match | VOLUNTEER_ACTIVITY_NAME_TIME_MATCH;
		
		if(va.getGeniusID() == this.geniusID)
			match = match | VOLUNTEER_ACTIVITY_GENIUS_MATCH;
		
		return match;
	}
	
	@Override
	public String toString() { return name; }
}
