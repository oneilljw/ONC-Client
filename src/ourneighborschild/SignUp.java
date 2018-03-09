package ourneighborschild;

/***
 * This class is a blueprint for the json returned by Sign Up Genius via the API when requesting
 * sign ups that have been created
 * @author johnoneil
 *
 */
public class SignUp
{
	private String contactname;
	private long enddate;
	private String enddatestring;
	private long endtime;
	private String group;
	private int groupid;
	private int signupid;
	private String mainimage;
	private String signupurl;
	private long startdate;
	private String startdatestring;
	private long starttime;
	private String thumbnail;
	private String title;
	private String offset;
	
	//getters
	private String getContactname() { return contactname; }
	private long getEnddate() { return enddate; }
	private String getEnddatestring() { return enddatestring; }
	private long getEndtime() { return endtime; }
	private String getGroup() { return group; }
	private int getGroupid() { return groupid; }
	public int getSignupid() { return signupid; }
	private String getMainimage() { return mainimage; }
	private String getSignupurl() { return signupurl; }
	private long getStartdate() { return startdate; }
	private String getStartdatestring() { return startdatestring; }
	private long getStarttime() { return starttime; }
	private String getThumbnail() { return thumbnail; }
	public String getTitle() { return title; }
}
