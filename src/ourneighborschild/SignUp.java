package ourneighborschild;

import java.util.Calendar;

/***
 * This class is a blueprint for the json returned by Sign Up Genius via the API when requesting
 * sign ups that have been created
 * @author johnoneil
 *
 */
public class SignUp
{
	private long lastImportTime;
	private Frequency frequency;
	private SignUpType signUpType;
//	private String contactname;
//	private long enddate;
//	private String enddatestring;
	private long endtime;
//	private String group;
//	private int groupid;
	private int signupid;
//	private String mainimage;
//	private String signupurl;
//	private long startdate;
//	private String startdatestring;
//	private long starttime;
//	private String thumbnail;
	private String title;
//	private String offset;
	
	public SignUp(String[] nextLine)
	{
		this.lastImportTime =  nextLine[0].isEmpty() ? 0 : Long.parseLong(nextLine[0]);
		this.signupid = nextLine[1].isEmpty() ? -1 : Integer.parseInt(nextLine[1]);
		this.title = nextLine[2];
		this.endtime = nextLine[3].isEmpty() ? 0 : Long.parseLong(nextLine[3]);
		this.frequency = nextLine[4].isEmpty() ? Frequency.NEVER : Frequency.valueOf(nextLine[4]);
		this.signUpType = nextLine[5].isEmpty() ? SignUpType.Unknown : SignUpType.valueOf(nextLine[5]);
	}
	
	public SignUp(SignUp su)
	{
		this.lastImportTime = su.lastImportTime;
		this.signupid = su.signupid;
		this.title = su.title;
		this.endtime = su.endtime;
		this.frequency = su.frequency;
		this.signUpType = su.signUpType;
	}
	
	//getters
//	String getContactname() { return contactname; }
//	long getEnddate() { return enddate; }
//	String getEnddatestring() { return enddatestring; }
	Calendar getEndTime()
	{
		Calendar endtimeCal = Calendar.getInstance();
		endtimeCal.setTimeInMillis(endtime);
		return endtimeCal;
	}
	public long getEndtimeInMillis() { return endtime; }
//	String getGroup() { return group; }
//	int getGroupid() { return groupid; }
	public int getSignupid() { return signupid; }
//	String getMainimage() { return mainimage; }
//	String getSignupurl() { return signupurl; }
//	long getStartdate() { return startdate; }
//	String getStartdatestring() { return startdatestring; }
//	long getStarttime() { return starttime; }
//	String getThumbnail() { return thumbnail; }
	public String getTitle() { return title; }
//	String getOffset() { return offset; }
	Calendar getLastImportTime()
	{
		Calendar lastImport = Calendar.getInstance();
		lastImport.setTimeInMillis(lastImportTime);
		return lastImport;
	}
	public long getLastImportTimeInMillis() { return lastImportTime; }
	public long getInterval() { return frequency.interval(); }
	public Frequency getFrequency() { return frequency; }
	public SignUpType getSignUpType() { return signUpType; }
	
	//setters
	public void setLastImportTimeInMillis(long time) { this.lastImportTime = time; }
	public void setFrequency(Frequency frequency) { this.frequency = frequency; }
	public void setEndtime(long time) { this.endtime = time; }
	public void setSignUpType(SignUpType type) { this.signUpType = type; }
	
	public String[] getExportRow()
	{
		String[] row = new String[6];
		row[0] = Long.toString(lastImportTime);
		row[1] = Integer.toString(signupid);
		row[2] = title;
		row[3] = Long.toString(endtime);
		row[4] = frequency.name();
		row[5] = signUpType.toString();
		
		return row;
	}
}
