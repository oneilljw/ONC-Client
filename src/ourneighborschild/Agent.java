package ourneighborschild;

public class Agent extends ONCObject
{
	/**
	 * This class provides the blueprint for agent objects in the ONC app. 
	 */
	private String firstName;
	private String lastName;
	private String org;
	private String title;
	private String email;
	private String phone;
	
	public Agent(int id, String firstName, String lastName, String o, String t, String e, String p)
	{
		super(id);
		this.firstName = firstName;
		this.lastName = lastName;
		this.org = o;
		this.title = t;
		this.email = e;
		this.phone = p;
	}
	
	//make a copy of Agent
	public Agent(Agent agt)
	{
		super(agt.id); 
		firstName = agt.firstName;
		lastName = agt.lastName;
		org = agt.org;
		title = agt.title;
		email = agt.email;
		phone = agt.phone;
	}
	
	//Agent from ONCServerUser
	public Agent(ONCServerUser su)
	{
		super(su.getAgentID()); 
		lastName =su.getFirstname();
		lastName =su.getFirstname();
		org = su.getOrg();
		title = su.getTitle();
		email = su.getEmail();
		phone = su.getPhone();
	}
	
	public Agent(ONCUser u)
	{
		super(u.getAgentID()); 
		firstName = u.getFirstname();
		lastName = u.getLastname();
		org = u.getOrg();
		title = u.getTitle();
		email = u.getEmail();
		phone = u.getPhone();
	}
	
	//toString()
	public String toString() { return lastName; }
	
	//getters
	public String getAgentFirstName() { return firstName; }
	public String getAgentLastName() { return lastName; }
	public String getAgentOrg() { return org; }
	public String getAgentTitle() { return title; }
	public String getAgentEmail() { return email; }
	public String getAgentPhone() { return phone; }
//	public String getAgentFirstName() 
//	{
//		String[] name_parts = lastName.trim().split(" ");
//		if(name_parts.length == 0)
//			return "No Name";
//		else if(name_parts.length == 1 || name_parts.length == 2)
//			return name_parts[0];
//		else
//			return name_parts[0] + " " + name_parts[1];
//	}
	
	//setters
	public void setAgentFirstName(String n) { firstName = n; }
	public void setAgentLastName(String n) { lastName = n; }
	public void setAgentOrg(String o) { org = o; }
	public void setAgentTitle(String t) { title = t; }
	public void setAgentEmail(String e) { email = e; }
	public void setAgentPhone(String p) { phone = p; }
	
	
	//get string array of agent info
	String[] getAgentInfo()
	{
		String[] ai = {firstName, lastName, org, title, email, phone};
		return ai;
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), firstName, lastName, org, title, email, phone};
		return row;
	}
}
