package ourneighborschild;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ONCServerUser extends ONCUser 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final long UNIX_EPOCH = 0L;
	private static final int RECOVERY_ID_LENGTH = 16;
	
	private String userid;
	private String password;
	private int failedLoginCount;
	private String recoveryID;
	private long recoveryIDTime;
	
	public ONCServerUser(int id, long today, String chgby, int slpos, String slmssg,
							String slchgby, String fn, String ln, 
							UserStatus stat, UserAccess acc, UserPermission perm,
							String uid, String pw, long nSessions, long last_login,
							boolean bResetPassword, 
							String org, String title, String email, String workphone, String cellphone,
							List<Integer> groupList)
	{
		super(id, today, chgby, slpos, slmssg, slchgby, fn, ln, stat, acc, perm, nSessions, last_login, 
				 org, title, email, workphone, cellphone, groupList);
		userid = uid;
		password = pw;
		failedLoginCount = 0;
		recoveryID="";
		recoveryIDTime = UNIX_EPOCH;
	}
	
	public ONCServerUser(ONCServerUser currUser)	//make a new object copy
	{
		
		super(currUser.id, currUser.timestamp, currUser.changedBy, currUser.slPos, currUser.slMssg,
				currUser.slChangedBy, currUser.firstName, currUser.lastName, currUser.status, 
				currUser.access, currUser.permission, currUser.nSessions, currUser.lastLogin, 
				currUser.organization, currUser.title, currUser.email, currUser.homePhone,currUser.cellPhone, currUser.groupList);
		userid = currUser.userid;
		password = currUser.password;
		failedLoginCount = currUser.failedLoginCount;
		recoveryID = currUser.recoveryID;
		recoveryIDTime = currUser.recoveryIDTime;
	}
	
	public ONCServerUser(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[8]), nextLine[9], Integer.parseInt(nextLine[10]),
				nextLine[11], nextLine[12], nextLine[6], nextLine[7], UserStatus.valueOf(nextLine[3]),
				UserAccess.valueOf(nextLine[4]), UserPermission.valueOf(nextLine[5]),
				Long.parseLong(nextLine[13]), Long.parseLong(nextLine[14]), 
				nextLine[15], nextLine[16], nextLine[17], nextLine[18], nextLine[19], createGroupList(nextLine[20]),
				Integer.parseInt(nextLine[21]), Integer.parseInt(nextLine[22]),
				getFilterDNSCode(Integer.parseInt(nextLine[23])));
				
		userid = nextLine[1];
		password = nextLine[2];
		failedLoginCount = nextLine[24].isEmpty() ? 0 : Integer.parseInt(nextLine[24]);
		recoveryID = nextLine[25];
	 	recoveryIDTime = nextLine[26].isEmpty() ? UNIX_EPOCH : Long.parseLong(nextLine[26]);
	}
	
	private static DNSCode getFilterDNSCode(int dnsCodeID)
	{
		return dnsCodeID == -4 ? new DNSCode(-4, "No Codes", "No Codes", "Families being Served") : new DNSCode(-3, "Any", "Any", "Any");
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
	public int getFailedLoginCount() { return failedLoginCount; }
	public String getRecoveryID() { return recoveryID; }
	public long getRecoveryIDTime() { return recoveryIDTime; }
	
	public void setUserPW(String pw) { password = pw; }
	public void setUserID(String uid) { userid = uid; }
	public void setFailedLoginCount(int flc) { failedLoginCount = flc; }
	
	private String createRandomString(int length) 
	{ 
		char[] characterSet = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789!".toCharArray();
		Random random = new SecureRandom();
		char[] result = new char[length];
		for (int i = 0; i < result.length; i++) 
		{
			// picks a random index out of character set > random character
		    int randomCharIndex = random.nextInt(characterSet.length);
		       result[i] = characterSet[randomCharIndex];
		}
		
		return new String(result);
	}
	
	public String setRecoveryStatusAndPassword()
	{
		this.setStatus(UserStatus.Recovery);
		
	    //Generate a 6 digit random number recovery code from 0 to 999999
	    Random rnd = new Random();
	    int number = rnd.nextInt(999999);

	    //set a temporary user password to the recovery code
	    String recoveryString = String.format("%06d", number);
	    this.setUserPW(recoveryString);
	    
	    return recoveryString;
	}

	public String createRecoveryIDAndTime()
	{
		recoveryID = createRandomString(RECOVERY_ID_LENGTH );
		
		//set the creation time
		Calendar now = Calendar.getInstance();
		recoveryIDTime = now.getTimeInMillis();
		
		return recoveryID;
	}
	
	public void disableRecoveryID()
	{
		//set the recoveryID to "DI$@BLED" and set the recovery time to two hours before 
		//current time. This will disable the recovery process as recovery links are only active
		//for one hour. It will also preserve evidence of a recovery occurrence
		recoveryID = "DI$@BLED";
		//set the creation time
		Calendar now = Calendar.getInstance();
		long two_hours_millis = 1000 * 60 * 60 * 2;
		recoveryIDTime = now.getTimeInMillis() - two_hours_millis;
	}
	
	//creates and returns a new ONCUser object from this ONCServerUserObject
	public ONCUser getUserFromServerUser()
	{
		return new ONCUser(id, timestamp, changedBy, slPos, slMssg, slChangedBy, 
				firstName, lastName, status, access, permission, clientID, clientYear, nSessions, 
				lastLogin, organization, title, email, homePhone, cellPhone, groupList, preferences);	
	}
	
	public boolean doesUserMatch(ONCServerUser compUser)
	{
		return firstName.equalsIgnoreCase(compUser.firstName) && 
				lastName.equalsIgnoreCase(compUser.lastName);
	}
	
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {
						Integer.toString(id), 
						EncryptionManager.encrypt(userid), EncryptionManager.encrypt(password),
						status.toString(), access.toString(), 
						permission.toString(), firstName, lastName,
						Long.toString(timestamp), changedBy, Integer.toString(slPos), 
						slMssg,slChangedBy, Long.toString(nSessions), 
						Long.toString(lastLogin),
						organization, title, email, homePhone, cellPhone, getGroupListAsDelimitedString(),
						Integer.toString(preferences.getFontSize()), 
						Integer.toString(preferences.getWishAssigneeFilter()), 
						Integer.toString(preferences.getFamilyDNSFilterCode().getID()),
						Integer.toString(failedLoginCount),
						recoveryID,
						Long.toString(recoveryIDTime)
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
