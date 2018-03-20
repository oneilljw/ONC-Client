package ourneighborschild;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class GeniusSignUps
{
	private List<SignUp> signUpList;
	private long lastSignUpListImportTime;
	
	public GeniusSignUps()
	{
		signUpList = new LinkedList<SignUp>();
		lastSignUpListImportTime = 0;
	}
	
	public GeniusSignUps(List<SignUp> signUpList)
	{
		this.signUpList = signUpList;
		this.lastSignUpListImportTime = System.currentTimeMillis();
	}
	
	//getters
	public List<SignUp> getSignUpList() { return signUpList; }
	public long getLastSignUpListImportTime() { return lastSignUpListImportTime; }
	
	public Calendar getLastImportTime()
	{
		Calendar lastImport = Calendar.getInstance();
		lastImport.setTimeInMillis(lastSignUpListImportTime);
		return lastImport;
	}
	
	//setters
	public void setSignUpList(List<SignUp> signUpList) { this.signUpList = signUpList; }
	public void setLastSignUpListImportTime(long time) { this. lastSignUpListImportTime = time; }
	
	public void add(SignUp signUp) { signUpList.add(signUp); }
}
