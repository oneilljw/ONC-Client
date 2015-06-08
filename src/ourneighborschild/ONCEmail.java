package ourneighborschild;

import java.util.ArrayList;

public class ONCEmail
{
	private String subject;
	private String body;
	private ArrayList<EmailAddress> toAddresses;
	
	ONCEmail(String subject, String body, ArrayList<EmailAddress> toAdds)
	{
		this.subject = subject;
		this.body = body;
		toAddresses = toAdds;
	}
	
	//getters
	String getEmailSubject() { return subject; }
	String getEmailBody() { return body; }
	ArrayList<EmailAddress> getToAddressees() { return toAddresses; }
}
