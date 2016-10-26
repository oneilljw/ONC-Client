package ourneighborschild;

public class Agent extends ONCObject
{
	/**
	 * This class provides the blueprint for agent objects in the ONC app. 
	 */
	private String name;
	private String org;
	private String title;
	private String email;
	private String phone;
	
	public Agent(int id, String n, String o, String t, String e, String p)
	{
		super(id); 
		name = n;
		org = o;
		title = t;
		email = e;
		phone = p;
	}
	
	//make a copy of Agent
	public Agent(Agent agt)
	{
		super(agt.id); 
		name = agt.name;
		org = agt.org;
		title = agt.title;
		email = agt.email;
		phone = agt.phone;
	}
	
	//toString()
	public String toString() { return name; }
	
	//getters
	public String getAgentName() { return name; }
	public String getAgentOrg() { return org; }
	public String getAgentTitle() { return title; }
	public String getAgentEmail() { return email; }
	public String getAgentPhone() { return phone; }
	public String getAgentFirstName() 
	{
		String[] name_parts = name.trim().split(" ");
		if(name_parts.length == 0)
			return "No Name";
		else if(name_parts.length == 1 || name_parts.length == 2)
			return name_parts[0];
		else
			return name_parts[0] + " " + name_parts[1];
	}
	
	//setters
	public void setAgentName(String n) { name = n; }
	public void setAgentOrg(String o) { org = o; }
	public void setAgentTitle(String t) { title = t; }
	public void setAgentEmail(String e) { email = e; }
	public void setAgentPhone(String p) { phone = p; }
	
	
	public boolean doesAgentNameMatch(String compAgentName)
	{
		boolean result = false;
		
		String[] compAgentNameParts = compAgentName.split(" ");
		String[] agentNameParts = name.split(" ");
		
		if(compAgentNameParts.length == agentNameParts.length)	//names have same number of parts
		{	
			int index = 0;
			while(index < agentNameParts.length && 
					compAgentNameParts[index].trim().toLowerCase().equals(agentNameParts[index].trim().toLowerCase()))
				index++;
			
			if(index == agentNameParts.length)	//agent name matches part for part, in order
				result = true;
		}	
		
		return result;
	}
	
	//get string array of agent info
	String[] getAgentInfo()
	{
		String[] ai = {name, org, title, email, phone};
		return ai;
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), name, org, title, email, phone};
		return row;
	}
}
