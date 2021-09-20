package ourneighborschild;

public class DNSCode extends ONCEntity
{
	/**
	 * Plain old java object for DNS codes. DNS codes are used to provide details why a client
	 * family referred for holiday assistance is not being served.
	 */

	private static final long serialVersionUID = 1L;
	
	public static final int DUP = 0;
	public static final int WAITLIST = 1;
	public static final int FOOD_ONLY = 2;
	public static final int NOT_IN_SERVING_AREA = 3;
	public static final int OPT_OUT = 4;
	public static final int SALVATION_ARMY = 5;
	public static final int SERVED_BY_OTHERS = 6;
	public static final int WISH_ANTICIPATION = 7;
	public static final int NO_CHILDREN = 8;
	public static final int SA_FO = 9;
	
	String acronym;
	String name;
	String definition;
	boolean bShowOnWebsite;
	
	public DNSCode(int id, String acronym, String name, String definition, boolean bShowOnWebsite)
	{
		super(id, System.currentTimeMillis(), "", 3, "DNS Code Created", "");
		this.acronym = acronym;
		this.name = name;
		this.definition = definition;
		this.bShowOnWebsite = bShowOnWebsite;
	}
	
	public DNSCode()
	{
		super(-1, System.currentTimeMillis(), "", 3, "", "");
		this.acronym = "";
		this.name = "";
		this.definition = "";
		this.bShowOnWebsite = false;
	}
	
	DNSCode(DNSCode dc)
	{
		super(dc.id, System.currentTimeMillis(), dc.changedBy, dc.slPos, dc.slMssg, dc.slChangedBy);
		this.acronym = dc.acronym;
		this.name = dc.name;
		this.definition = dc.definition;
		this.bShowOnWebsite = dc.bShowOnWebsite;
	}
	
	public DNSCode(String[] line)
	{
		super(Integer.parseInt(line[0]), Long.parseLong(line[4]), line[5], Integer.parseInt(line[6]), 
				line[7].isEmpty() ? "" : line[7], line[8].isEmpty() ? "" : line[8]);
		this.acronym = line[1];
		this.name = line[2];
		this.definition = line[3];
		this.bShowOnWebsite = line[9].equalsIgnoreCase("Y") ? true : false;
	}
	
	//getters
	public String getAcronym() { return acronym; }
	public String getName() { return name; }
	public String getDefinition() { return definition; }
	public boolean showOnWebsite() { return bShowOnWebsite; }
	
	//setters
	void setAcronym(String acronym) { this.acronym = acronym; }
	void setName(String name) { this.name = name; }
	void setDefinition(String definition) { this.definition = definition; }
	void setShowOnWebstite(boolean tf) { this.bShowOnWebsite = tf; }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[10];
		row[0] = Integer.toString(id);
		row[1] = acronym;
		row[2] = name;
		row[3] = definition;
		row[4] = Long.toString(timestamp);
		row[5] = changedBy;
		row[6] = Integer.toString(slPos);
		row[7] = slMssg;
		row[8] = slChangedBy;
		row[9] = bShowOnWebsite ? "Y" : "N";
		
		return row;
	}
	
	@Override
	public String toString() { return acronym; }
	
	@Override
	public boolean equals(Object o) 
	{
	    if (o == this) { return true; }
	    if (!(o instanceof DNSCode)) { return false; }
	    DNSCode dnsCode = (DNSCode) o;
	    return dnsCode.id == id;
	}
	
	@Override
	public int hashCode() 
	{
	    int result = 17;
	    result = 31 * result + id;
	    return result;
	}
}

