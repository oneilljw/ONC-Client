package ourneighborschild;

import java.util.Date;

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
							String uid, String pw, long nSessions, Date last_login,
							boolean bResetPassword, 
							String title, String org, String email, String phone, int agtID)
	{
		super(id, today, chgby, slpos, slmssg, slchgby, fn, ln, stat, acc, perm, nSessions, last_login, 
				title, org, email, phone, agtID);
		userid = uid;
		password = pw;
	}
	
	public ONCServerUser(String[] nextLine, Date date_changed, Date last_login)
	{
		super(Integer.parseInt(nextLine[0]), date_changed, nextLine[9], Integer.parseInt(nextLine[10]),
				nextLine[11], nextLine[12], nextLine[6], nextLine[7], UserStatus.valueOf(nextLine[3]),
				UserAccess.valueOf(nextLine[4]), UserPermission.valueOf(nextLine[5]),
				Long.parseLong(nextLine[13]), last_login, 
				nextLine[15], nextLine[16], nextLine[17], nextLine[18], Integer.parseInt(nextLine[19]));
				
		userid = nextLine[1];
		password = nextLine[2];
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
				lastLogin, org, title, email, phone, agentID);	
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {
						Integer.toString(id), 
						ONCEncryptor.encrypt(userid), ONCEncryptor.encrypt(password),
						status.toString(), access.toString(), 
						permission.toString(), firstname, lastname,
						Long.toString(dateChanged.getTimeInMillis()), changedBy, Integer.toString(slPos), 
						slMssg,slChangedBy, Long.toString(nSessions), 
						Long.toString(lastLogin.getTimeInMillis()),
						org, title, email, phone, Integer.toString(agentID)
						};
		return row;
	}	
}
