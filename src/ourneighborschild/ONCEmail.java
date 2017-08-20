package ourneighborschild;

import java.util.ArrayList;

public class ONCEmail
{
	private String subject;
	private String body;
	private ArrayList<EmailAddress> toAddresses;
	
	public ONCEmail(String subject, String body, ArrayList<EmailAddress> toAdds)
	{
		this.subject = subject;
		this.body = body;
		toAddresses = toAdds;
	}
	
	//getters
	public String getEmailSubject() { return subject; }
	public String getEmailBody() { return body; }
	public ArrayList<EmailAddress> getToAddressees() { return toAddresses; }
}
