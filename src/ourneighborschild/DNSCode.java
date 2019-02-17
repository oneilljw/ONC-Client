package ourneighborschild;

import java.util.Date;

public class DNSCode extends ONCEntity
{
	/**
	 * Plain old java object for DNS codes. DNS codes are used to provide details why a client
	 * family referred for holiday assistance is not being served.
	 */
	private static final long serialVersionUID = 1L;
	String acronym;
	String name;
	String definition;
	
	DNSCode(int id, String acronym, String name, String definition)
	{
		super(id, new Date(), "", 3, "DNS Code Created", "");
		this.acronym = acronym;
		this.name = name;
		this.definition = definition;
	}
	
	public DNSCode()
	{
		super(-1, new Date(), "", 3, "", "");
		this.acronym = "";
		this.name = "";
		this.definition = "";
	}
	
	DNSCode(DNSCode dc)
	{
		super(dc.id, new Date(), dc.changedBy, dc.slPos, dc.slMssg, dc.slChangedBy);
		this.acronym = dc.acronym;
		this.name = dc.name;
		this.definition = dc.definition;
	}
	
	public DNSCode(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[4]), line[5], Integer.parseInt(line[6]), 
				line[7].isEmpty() ? "" : line[7], line[8].isEmpty() ? "" : line[8]);
		this.acronym = line[1];
		this.name = line[2];
		this.definition = line[3];
	}
	
	//getters
	public String getAcronym() { return acronym; }
	public String getName() { return name; }
	public String getDefinition() { return definition; }
	
	//setters
	void setAcronym(String acronym) { this.acronym = acronym; }
	void setName(String name) { this.name = name; }
	void setDefinition(String definition) { this.definition = definition; }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[9];
		row[0] = Integer.toString(id);
		row[1] = acronym;
		row[2] = name;
		row[3] = definition;
		row[4] = Long.toString(dateChanged.getTimeInMillis());
		row[5] = changedBy;
		row[6] = Integer.toString(slPos);
		row[7] = slMssg;
		row[8] = slChangedBy;
		
		return row;
	}
}

