package ourneighborschild;

public class ChangePasswordRequest 
{
	private int userID;
	private String currPW, newPW, firstName, lastName;
	
	ChangePasswordRequest(int userid, String fn, String ln, String currPW, String newPW)
	{
		userID = userid;
		firstName = fn;
		lastName = ln;
		this.currPW = currPW;
		this.newPW = newPW;
	}

	//getters
	public int getUserID() { return userID; }
	
	public String getCurrPW() { return currPW; }

	public String getNewPW() { return newPW; }

	public String getFirstName() { return firstName; }

	public String getLastName() { return lastName; }
}
