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
//	protected String firstName;
//	protected String lastName;
	protected UserStatus status;
	protected UserAccess access;
	protected UserPermission permission; 	
	protected long clientID; 	//holds the server id of the client when the user is logged in
	protected int clientYear;
	protected long nSessions;
	protected long lastLogin;	//should always be UTC
//	protected String email;
//	protected String cellPhone;
	protected String title;
//	protected String organization;
	protected List<Integer> groupList;
	protected UserPreferences preferences;
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			long last, String org, String title, String email, String phone, List<Integer> groupList)
	{
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);
		
//		firstName = fn;
//		lastName = ln;
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = last;
//		this.organization = org;
		this.title = title;
//		this.email = email;
//		this.cellPhone = phone;
		this.groupList = groupList;
		this.preferences = new UserPreferences();
	}
	
	//overloaded -- used when including UserPreferences in the construction
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, UserStatus stat, UserAccess acc, UserPermission perm, long nSessions,
			long last, String org, String title,String email, String phone, List<Integer> groupList,
			int fontSize, int wafPos, int fdfPos)
	{
//		super(id, today, changedBy, slpos, slmssg, slchgby);
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);
		
//		firstName = fn;
//		lastName = ln;
		status = stat;
		access = acc;
		permission = perm;
		clientID = -1;
		clientYear = -1;
		this.nSessions = nSessions;
		lastLogin = last;
//		this.organization = org;
		this.title = title;
//		this.email = email;
//		this.cellPhone = phone;
		this.groupList = groupList;
		this.preferences = new UserPreferences(fontSize, wafPos, fdfPos);
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a deep copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln,  UserStatus stat, UserAccess acc, UserPermission perm,
			long clientID, int clientYear, long nSessions, long lastLogin, 
			String org, String title, String email, String phone, List<Integer> oldGroupList, UserPreferences prefs)
	{
//		super(id, today, changedBy, slpos, slmssg, slchgby);
		super(id, fn, ln, email, phone, phone, "","","","","","New User", org, today, changedBy, 
				slpos, slmssg, slchgby);
		
//		firstName = fn;
//		lastName = ln;
		status = stat;
		access = acc;
		permission = perm;
		this.clientID = clientID;
		this.clientYear = clientYear;
		this.nSessions = nSessions;
		this.lastLogin = lastLogin;
//		this.organization = org;
		this.title = title;
//		this.email = email;
//		this.cellPhone = phone;
		
		this.groupList = new LinkedList<Integer>();
		for(Integer groupID : oldGroupList)
			this.groupList.add(groupID);
		
		this.preferences = prefs;
	}
	
	public ONCUser(ONCUser u)
	{
//		super(u.id, u.dateChanged.getTime(), u.changedBy, u.slPos, u.slMssg, u.slChangedBy);
		super(u.id, u.firstName, u.lastName, u.email, u.homePhone, u.cellPhone, u.houseNum,u.street,u.unit,u.city,u.zipCode, u.comment, u.organization, u.dateChanged, u.changedBy, 
				u.slPos, u.slMssg, u.slChangedBy);
		
//		firstName = u.firstName;
//		lastName = u.lastName;
		status = u.status;
		access = u.access;
		permission = u.permission;
		this.clientID = u.clientID;
		this.clientYear = u.clientYear;
		this.nSessions = u.nSessions;
		this.lastLogin = u.lastLogin;
//		this.organization = u.organization;
		this.title = u.title;
//		this.email = u.email;
//		this.cellPhone = u.cellPhone;
		
//		this.groupList = new LinkedList<Integer>();
//		for(Integer groupID : groupList)
//			groupList.add(groupID);
		
		List<Integer> newGroupList = new LinkedList<Integer>();
		for(Integer groupID : groupList)
			newGroupList.add(groupID);
		
		this.groupList = newGroupList;
		
		this.preferences = u.preferences;
	}
	
	public ONCUser()
	{
//		super(-1, new Date(), "", 3, "Dummy User Agent", "");
		super(-1, "Dummy", "User/Agent", "None", "None", "None", "","","","","","Dummy User Agent", "None",  new Date(), "", 3, "Dummy User Agent", "");

		
//		firstName = "Dummy";
//		lastName = "User/Agent";
		status = UserStatus.Inactive;
		access = UserAccess.App;
		permission = UserPermission.General;
		this.clientID = -1;
		this.clientYear = -1;
		this.nSessions = 0;
		this.lastLogin = System.currentTimeMillis();
//		this.organization = "None";
		this.title = "None";
//		this.email = "None";
//		this.cellPhone = "None";
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
//	public String getFirstName() { return firstName; }
//	public String getLastName() { return lastName; }
//	public String getOrganization() { return organization; }
	public String getTitle() { return title; }
//	public String getEmail() { return email; }
//	public String getCellPhone() { return cellPhone; }
	public UserPermission getPermission() { return permission; }
	public void setPermission(UserPermission permission) { this.permission = permission; }
	public UserStatus getStatus() { return status; }
	public UserAccess getAccess() { return access; }
	public List<Integer> getGroupList() { return groupList; }
	
	//setters
//	public void setFirstName(String firstname) { this.firstName = firstname; }
//	public void setLastName(String lastname) { this.lastName = lastname; }
	public void setStatus(UserStatus status) { this.status = status; }
	public void setAccess(UserAccess access) { this.access = access; }
//	public void setOrganization(String s) { this.organization = s; }
	public void setTitle(String s) { this.title = s; }
//	public void setEmail(String s) { this.email = s; }
//	public void setCellPhone(String s) { this.cellPhone = s; }
	public void setGroupList(List<Integer> groupList) { this.groupList = groupList; }
	public void setPreferences(UserPreferences prefs) { this.preferences = prefs; }
	
	public boolean isInGroup(Integer groupID)
	{
		int index = 0;
		while(index < groupList.size())
			index++;
		
		return index < groupList.size();
	}
	public void addGroup(Integer groupID) { groupList.add(groupID); }
	public void removeGroup(Integer groupID) {groupList.remove(groupID); }
	

//	public String getLNFI()
//	{
//		if(firstName.isEmpty())
//    		return lastName;
//    	else
//    		return lastName + ", " + firstName.charAt(0);
//	}
	
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
