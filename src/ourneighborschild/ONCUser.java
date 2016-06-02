package ourneighborschild;

import java.util.Calendar;
import java.util.Date;

public class ONCUser extends ONCEntity
{
	/*****
	 * This class implements a user pojo consisting of a first name, last name and
	 * permission fields
	 */
	private static final long serialVersionUID = 1L;
	protected String firstname;
	protected String lastname;
	protected UserStatus status;
	protected UserAccess access;
	protected UserPermission permission; 	
	protected long clientID; 	//holds the server id of the client when the user is logged in
	protected int clientYear;
	protected long nSessions;
	protected Calendar lastLogin;
	protected String email;
	protected String phone;
	protected String title;
	protected String org;
	protected int agentID;
	protected UserPreferences preferences;
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			Date last, String org, String title,String email, String phone, int agentID)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = Calendar.getInstance();
		lastLogin.setTime(last);
		this.org = org;
		this.title = title;
		this.email = email;
		this.phone = phone;
		this.agentID = agentID;
		this.preferences = new UserPreferences();
	}
	
	//overloaded -- used when including UserPreferences in the construction
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			Date last, String org, String title,String email, String phone, int agentID,
			int fontSize, int wafPos, int fdfPos)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = Calendar.getInstance();
		lastLogin.setTime(last);
		this.org = org;
		this.title = title;
		this.email = email;
		this.phone = phone;
		this.agentID = agentID;
		this.preferences = new UserPreferences(fontSize, wafPos, fdfPos);
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln,  UserStatus stat, UserAccess acc, UserPermission perm,
			long clientID, int clientYear, long nSessions, Calendar lastLogin, 
			String org, String title, String email, String phone, int agentID, UserPreferences prefs)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		status = stat;
		access = acc;
		permission = perm;
		this.clientID = clientID;
		this.clientYear = clientYear;
		this.nSessions = nSessions;
		this.lastLogin = lastLogin;
		this.org = org;
		this.title = title;
		this.email = email;
		this.phone = phone;
		this.agentID = agentID;
		this.preferences = prefs;
	}
	
	public ONCUser(ONCUser u)
	{
		super(u.id, u.dateChanged.getTime(), u.changedBy, u.slPos, u.slMssg, u.slChangedBy);
		
		firstname = u.firstname;
		lastname = u.lastname;
		status = u.status;
		access = u.access;
		permission = u.permission;
		this.clientID = u.clientID;
		this.clientYear = u.clientYear;
		this.nSessions = u.nSessions;
		this.lastLogin = u.lastLogin;
		this.org = u.org;
		this.title = u.title;
		this.email = u.email;
		this.phone = u.phone;
		this.agentID = u.agentID;
		this.preferences = u.preferences;
	}
	
	public long getClientID() { return clientID; }
	public void setClientID(long clientID) { this.clientID = clientID; }
	public int getClientYear() { return clientYear; }
	public void setClientYear(int year) { clientYear = year; }
	public long getNSessions() { return nSessions; }
	public Date getLastLogin() { return lastLogin.getTime(); }
	public void setLastLogin(Date last_login) { lastLogin.setTime(last_login); }
	public long incrementSessions() { return ++nSessions; }
	public boolean changePasswordRqrd() { return status.equals(UserStatus.Change_PW); }
	public UserPreferences getPreferences() { return preferences; }
	
	public String getFirstname() { return firstname; }
	public void setFirstname(String firstname) { this.firstname = firstname; }
	public String getLastname() { return lastname; }
	public void setLastname(String lastname) { this.lastname = lastname; }
	public UserPermission getPermission() { return permission; }
	public void setPermission(UserPermission permission) { this.permission = permission; }
	public UserStatus getStatus() { return status; }
	public void setStatus(UserStatus status) { this.status = status; }
	public UserAccess getAccess() { return access; }
	public void setAccess(UserAccess access) { this.access = access; }
	public int getAgentID() { return agentID; }
	public void setAgentID(int agtID) { this.agentID = agtID; }
	public void setPreferences(UserPreferences prefs) { this.preferences = prefs; }
	
	public String getOrg() { return org; }
	public String getTitle() { return title; }
	public String getEmail() { return email; }
	public String getPhone() { return phone; }
	
	public void setOrg(String s) { this.org = s; }
	public void setTitle(String s) { this.title = s; }
	public void setEmail(String s) { this.email = s; }
	public void setPhone(String s) { this.phone = s; }

	public String getLNFI()
	{
		if(firstname.isEmpty())
    		return lastname;
    	else
    		return lastname + ", " + firstname.charAt(0);
	}
	
	public ONCUser getUser() { return this; }

	@Override
	public String[] getExportRow()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString()
	{
		return firstname + " " + lastname;	
	}
}
