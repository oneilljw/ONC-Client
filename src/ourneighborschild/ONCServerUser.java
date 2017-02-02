package ourneighborschild;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ONCServerUser extends ONCUser 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userid;
	private String password;
	
	public ONCServerUser(int id, Date today, String chgby, int slpos, String slmssg,
							String slchgby, String fn, String ln, 
							UserStatus stat, UserAccess acc, UserPermission perm,
							String uid, String pw, long nSessions, long last_login,
							boolean bResetPassword, 
							String org, String title, String email, String phone, 
							List<Integer> groupList)
	{
		super(id, today, chgby, slpos, slmssg, slchgby, fn, ln, stat, acc, perm, nSessions, last_login, 
				 org, title, email, phone, groupList);
		userid = uid;
		password = pw;
	}
	
	public ONCServerUser(ONCServerUser currUser)	//make a new object copy
	{
		
		super(currUser.id, currUser.dateChanged.getTime(), currUser.changedBy, currUser.slPos, currUser.slMssg,
				currUser.slChangedBy, currUser.firstname, currUser.lastname, currUser.status, 
				currUser.access, currUser.permission, currUser.nSessions, currUser.lastLogin, 
				currUser.org, currUser.title, currUser.email, currUser.phone, currUser.groupList);
		userid = currUser.userid;
		password = currUser.password;
	}
	
	public ONCServerUser(String[] nextLine, Date date_changed)
	{
		super(Integer.parseInt(nextLine[0]), date_changed, nextLine[9], Integer.parseInt(nextLine[10]),
				nextLine[11], nextLine[12], nextLine[6], nextLine[7], UserStatus.valueOf(nextLine[3]),
				UserAccess.valueOf(nextLine[4]), UserPermission.valueOf(nextLine[5]),
				Long.parseLong(nextLine[13]), Long.parseLong(nextLine[14]), 
				nextLine[15], nextLine[16], nextLine[17], nextLine[18], createGroupList(nextLine[19]),
				Integer.parseInt(nextLine[20]), Integer.parseInt(nextLine[21]), Integer.parseInt(nextLine[22]));
				
		userid = nextLine[1];
		password = nextLine[2];
	}
	
	static List<Integer> createGroupList(String delimitedGroups)
	{
		List<Integer> groupList = new LinkedList<Integer>();
		String[] groups = delimitedGroups.trim().split("_");
		for(String group: groups)
			if(!group.isEmpty() && isNumeric(group))
				groupList.add(Integer.parseInt(group));
		
		return groupList;	
	}
	
	public boolean idMatch(String uid) { return userid.equals(uid); }
	public boolean pwMatch(String pw) { return password.equals(pw); }
	
	public String getUserID() { return userid; }
	public String getUserPW() { return password; }
	
	public void setUserPW(String pw) { password = pw; }
	public void setUserID(String uid) { userid = uid; }
	
	//creates and returns a new ONCUser object from this ONCServerUserObject
	public ONCUser getUserFromServerUser()
	{
		return new ONCUser(id, dateChanged.getTime(), changedBy, slPos, slMssg, slChangedBy, 
				firstname, lastname, status, access, permission, clientID, clientYear, nSessions, 
				lastLogin, org, title, email, phone, groupList, preferences);	
	}
	
	public boolean doesUserMatch(ONCServerUser compUser)
	{
		return firstname.equalsIgnoreCase(compUser.firstname) && 
				lastname.equalsIgnoreCase(compUser.lastname);
	}
	
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {
						Integer.toString(id), 
						EncryptionManager.encrypt(userid), EncryptionManager.encrypt(password),
						status.toString(), access.toString(), 
						permission.toString(), firstname, lastname,
						Long.toString(dateChanged.getTimeInMillis()), changedBy, Integer.toString(slPos), 
						slMssg,slChangedBy, Long.toString(nSessions), 
						Long.toString(lastLogin),
						org, title, email, phone, getGroupListAsDelimitedString(),
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
}
