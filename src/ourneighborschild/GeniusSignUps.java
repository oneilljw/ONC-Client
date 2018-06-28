package ourneighborschild;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class GeniusSignUps
{
	private List<SignUp> signUpList;
	private long lastSignUpListImportTime;
	private boolean bSignUpImportEnabled;
	
	public GeniusSignUps()
	{
		signUpList = new LinkedList<SignUp>();
		lastSignUpListImportTime = 0;
		bSignUpImportEnabled = false;
	}
	
	public GeniusSignUps(List<SignUp> signUpList)
	{
		this.signUpList = signUpList;
		this.lastSignUpListImportTime = System.currentTimeMillis();
		this.bSignUpImportEnabled = false;
	}
	
	public GeniusSignUps(GeniusSignUps gsu)	//copy constructor
	{
		this.bSignUpImportEnabled = gsu.bSignUpImportEnabled;
		this.lastSignUpListImportTime = gsu.lastSignUpListImportTime;
		signUpList = new LinkedList<SignUp>();
		for(SignUp su : gsu.signUpList)	//deep copy of each sign-up
			signUpList.add(new SignUp(su));
	}
	
	//getters
	public List<SignUp> getSignUpList() { return signUpList; }
	public long getLastSignUpListImportTime() { return lastSignUpListImportTime; }
	public boolean isImportEnabled() { return bSignUpImportEnabled; }
	
	public Calendar getLastImportTime()
	{
		Calendar lastImport = Calendar.getInstance();
		lastImport.setTimeInMillis(lastSignUpListImportTime);
		return lastImport;
	}
	
	//setters
	public void setSignUpList(List<SignUp> signUpList) { this.signUpList = signUpList; }
	public void setLastSignUpListImportTime(long time) { this.lastSignUpListImportTime = time; }
	public void setSignUpImportEnabled(boolean tf) { this.bSignUpImportEnabled = tf; }
	
	public void add(SignUp signUp) { signUpList.add(signUp); }
}
