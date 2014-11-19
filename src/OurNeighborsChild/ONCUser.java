package OurNeighborsChild;

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
	protected int permission; 	//0 - general user, 1- admin user, 2- super user
	protected long clientID; 	//holds the server id of the client when the user is logged in
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, int perm)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		permission = perm;
		clientID = -1;
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, int perm, long clientID)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		permission = perm;
		this.clientID = clientID;
	}
	
	public long getClientID() { return clientID; }
	public void setClientID(long clientID) { this.clientID = clientID; }
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public int getPermission() {
		return permission;
	}
	
//	public boolean isUserOnline() {
//		return bOnline;
//	}

	public void setPermission(int permission) {
		this.permission = permission;
	}
	
//	public void setUserOnline(boolean tf) {
//		 bOnline = tf;
//	}
	
	public String getLNFI()
	{
		if(firstname.isEmpty())
    		return lastname;
    	else
    		return lastname + ", " + firstname.charAt(0);
	}
	
	public ONCUser getUser() { return this; }

	@Override
	public String[] getDBExportRow() {
		// TODO Auto-generated method stub
		return null;
	}	
}
