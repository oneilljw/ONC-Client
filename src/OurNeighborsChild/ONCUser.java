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
	protected int clientYear;
	
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, int perm)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		permission = perm;
		clientID = -1;
		clientYear = -1;
	}
	
	//overloaded to allow conversion from ONCServerUser to ONCUser by creating a copy
	public ONCUser(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
			String fn, String ln, int perm, long clientID, int clientYear)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		
		firstname = fn;
		lastname = ln;
		permission = perm;
		this.clientID = clientID;
		this.clientYear = clientYear;
	}
	
	public long getClientID() { return clientID; }
	public void setClientID(long clientID) { this.clientID = clientID; }
	public int getClientYear() { return clientYear; }
	public void setClientYear(int year) { clientYear = year; }
	
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

	public void setPermission(int permission) {
		this.permission = permission;
	}

	public String getLNFI()
	{
		if(firstname.isEmpty())
    		return lastname;
    	else
    		return lastname + ", " + firstname.charAt(0);
	}
	
	public ONCUser getUser() { return this; }

	@Override
	public String[] getExportRow() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String toString()
	{
		return firstname + " " + lastname;	
	}

	@Override
	Object getTableCell(int col)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getColumnClass(int col)
	{
		return String.class;
	}
}
