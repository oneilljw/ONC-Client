package ourneighborschild;

public class EmailAddress 
{
	private String emailAddress;
	private String name;
	
	EmailAddress(String emailAddress, String name)
	{
		this.emailAddress = emailAddress;
		this.name = name;
	}
	
	//getters
	String getEmailAddress() { return emailAddress; }
	String getName() { return name; }
	
	//setters
	void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
	void setName(String name) { this.name = name; }	
}
