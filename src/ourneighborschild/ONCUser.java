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
	protected int agentID;
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			Date last, int agentID)
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
		this.agentID = agentID;
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln,  UserStatus stat, UserAccess acc, UserPermission perm,
			long clientID, int clientYear, long nSessions, Calendar lastLogin, int agentID)
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
		this.agentID = agentID;
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
		this.agentID = u.agentID;
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
