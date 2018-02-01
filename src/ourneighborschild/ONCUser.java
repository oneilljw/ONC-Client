package ourneighborschild;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class ONCUser extends ONCGmailContactEntity
{
	/*****
	 * This class implements a user pojo consisting of a first name, last name and
	 * permission fields
	 */
	private static final long serialVersionUID = 1L;

	protected UserStatus status;
	protected UserAccess access;
	protected UserPermission permission; 	
	protected long clientID; 	//holds the server id of the client when the user is logged in
	protected int clientYear;
	protected long nSessions;
	protected long lastLogin;	//should always be UTC
	protected String title;
	protected List<Integer> groupList;
	protected UserPreferences preferences;
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			long last, String org, String title, String email, String phone, List<Integer> groupList)
	{
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);
		
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = last;
		this.title = title;
		this.groupList = groupList;
		this.preferences = new UserPreferences();
	}
	
	//overloaded -- used when including UserPreferences in the construction
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			long last, String org, String title,String email, String phone, List<Integer> groupList,
			int fontSize, int wafPos, int fdfPos)
	{
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);
		
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = last;
		this.title = title;
		this.groupList = groupList;
		this.preferences = new UserPreferences(fontSize, wafPos, fdfPos);
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a deep copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln,  UserStatus stat, UserAccess acc, UserPermission perm,
			long clientID, int clientYear, long nSessions, long lastLogin, 
			String org, String title, String email, String phone, List<Integer> oldGroupList, UserPreferences prefs)
	{
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);

		status = stat;
		access = acc;
		permission = perm;
		this.clientID = clientID;
		this.clientYear = clientYear;
		this.nSessions = nSessions;
		this.lastLogin = lastLogin;
		this.title = title;
		
		this.groupList = new LinkedList<Integer>();
		for(Integer groupID : oldGroupList)
			this.groupList.add(groupID);
		
		this.preferences = prefs;
	}
	
	public ONCUser(ONCUser u)
	{
		super(u.id, u.firstName, u.lastName, u.email, u.homePhone, u.cellPhone, u.houseNum,u.street,u.unit,u.city,u.zipCode, u.comment, u.organization, u.dateChanged, u.changedBy, 
				u.slPos, u.slMssg, u.slChangedBy);

		status = u.status;
		access = u.access;
		permission = u.permission;
		this.clientID = u.clientID;
		this.clientYear = u.clientYear;
		this.nSessions = u.nSessions;
		this.lastLogin = u.lastLogin;
		this.title = u.title;
		
		List<Integer> newGroupList = new LinkedList<Integer>();
		for(Integer groupID : u.getGroupList())
			newGroupList.add(groupID);
		
		this.groupList = newGroupList;
		
		this.preferences = u.preferences;
	}
	
	public ONCUser()
	{
		super(-1, "Dummy", "User/Agent", "None", "None", "None", "","","","","","Dummy User Agent", "None",  new Date(), "", 3, "Dummy User Agent", "");

		status = UserStatus.Inactive;
		access = UserAccess.App;
		permission = UserPermission.General;
		this.clientID = -1;
		this.clientYear = -1;
		this.nSessions = 0;
		this.lastLogin = System.currentTimeMillis();
		this.title = "None";
		groupList = new LinkedList<Integer>();
		this.preferences = new UserPreferences(13, 1, 1);
	}
	
	public ONCUser(String fn, String ln)
	{
		super(-1, fn, ln, "None", "None", "None", "","","","","","Dummy Chat User", "None",  new Date(), "", 3, "Dummy Chat User", "");

		status = UserStatus.Inactive;
		access = UserAccess.App;
		permission = UserPermission.General;
		this.clientID = -1;
		this.clientYear = -1;
		this.nSessions = 0;
		this.lastLogin = System.currentTimeMillis();
		this.title = "None";
		groupList = new LinkedList<Integer>();
		this.preferences = new UserPreferences(13, 1, 1);
	}
	
	//getters
	public long getClientID() { return clientID; }
	public void setClientID(long clientID) { this.clientID = clientID; }
	public int getClientYear() { return clientYear; }
	public void setClientYear(int year) { clientYear = year; }
	public long getNSessions() { return nSessions; }
	public void setNSessions(long nSessions) { this.nSessions = nSessions; }
	public long getLastLogin() { return lastLogin; }
	public void setLastLogin(long last_login) { this.lastLogin = last_login; }
	public long incrementSessions() { return ++nSessions; }
	public boolean changePasswordRqrd() { return status.equals(UserStatus.Change_PW); }
	public UserPreferences getPreferences() { return preferences; }
	public String getTitle() { return title; }
	public UserPermission getPermission() { return permission; }
	public void setPermission(UserPermission permission) { this.permission = permission; }
	public UserStatus getStatus() { return status; }
	public UserAccess getAccess() { return access; }
	public List<Integer> getGroupList() { return groupList; }
	
	//setters
	public void setStatus(UserStatus status) { this.status = status; }
	public void setAccess(UserAccess access) { this.access = access; }
	public void setTitle(String s) { this.title = s; }
	public void setGroupList(List<Integer> groupList) { this.groupList = groupList; }
	public void setPreferences(UserPreferences prefs) { this.preferences = prefs; }
	
	public boolean isInGroup(Integer groupID)
	{
		//check to see if the group id is in the users list of groups
		int index = 0;
		while(index < groupList.size() && groupList.get(index) != groupID)
			index++;
		
		return index < groupList.size();
	}
	public void addGroup(Integer groupID) { groupList.add(groupID); }
	public void removeGroup(Integer groupID) {groupList.remove(groupID); }
	
	public ONCUser getUser() { return this; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {
						Integer.toString(id), firstName, lastName,
						status.toString(), access.toString(), permission.toString(), 
						Long.toString(dateChanged.getTimeInMillis()), changedBy, Integer.toString(slPos), 
						slMssg,slChangedBy, Long.toString(nSessions), 
						Long.toString(lastLogin),
						organization, title, email, cellPhone, getGroupListAsDelimitedString(),
						Integer.toString(preferences.getFontSize()), 
						Integer.toString(preferences.getWishAssigneeFilter()), 
						Integer.toString(preferences.getFamilyDNSFilter())
						};
		return row;
	}
	
	String getGroupListAsDelimitedString()
	{
		if(groupList.isEmpty())
			return "";
		else if(groupList.size() == 1)
			return Integer.toString(groupList.get(0));
		else
		{
			StringBuffer buff  = new StringBuffer(Integer.toString(groupList.get(0)));
			for(int index = 1; index < groupList.size(); index++)
				buff.append("_" + Integer.toString(groupList.get(index)));
			
			return buff.toString();
		}	
	}
	
	@Override
	public String toString()
	{
		return firstName + " " + lastName;	
	}
}
