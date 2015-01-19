package OurNeighborsChild;

import java.util.Date;

public class ONCServerUser extends ONCUser 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userid;
	private String password;
	
	public ONCServerUser(int id, Date today, String chgby, int slpos, String slmssg, String slchgby,
					String fn, String ln, int perm, String uid, String pw)
	{
		super(id, today, chgby, slpos, slmssg, slchgby, fn, ln, perm);
		userid = uid;
		password = pw;
	}
	
	public ONCServerUser(String[] nextLine, Date date_changed)
	{
		super(Integer.parseInt(nextLine[0]), date_changed, nextLine[7], Integer.parseInt(nextLine[8]),
				nextLine[9], nextLine[10], nextLine[4], nextLine[5], Integer.parseInt(nextLine[3]));
				
		userid = nextLine[1];
		password = nextLine[2];
	}
	
	public boolean idMatch(String uid) { return userid.equals(uid); }
	public boolean pwMatch(String pw) { return password.equals(pw); }
	
	public String getUserID() { return userid; }
	public String getUserPW() { return password; }
	
	public void setUserPW(String pw) { password = pw; }
	
	//creates and returns a new ONCUser object from this ONCServerUserObject
	public ONCUser getUserFromServerUser()
	{
		return new ONCUser(id, dateChanged.getTime(), changedBy, slPos, slMssg, slChangedBy, 
				firstname, lastname, permission, clientID, clientYear);	
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row = {Integer.toString(id), userid, password, Integer.toString(permission), firstname, lastname,
						Long.toString(dateChanged.getTimeInMillis()), changedBy, Integer.toString(slPos), slMssg,
						slChangedBy};
		return row;
	}	
}
