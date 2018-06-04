package ourneighborschild;

import java.util.Date;

public class ONCGroup extends ONCEntity 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int NOT_SHARING = 0;
	public static final int SHARING = 1;
	
	private String name;
	private GroupType type;
	private int permission;
	private boolean bWebpage;
	private boolean bContactInfoRqrd;
	
	public ONCGroup(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
						String name, GroupType type, int permission, boolean bWebpage, boolean bContactInfoRqrd) 
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.name = name;
		this.type = type;
		this.permission = permission;
		this.bWebpage = bWebpage;
		this.bContactInfoRqrd = bContactInfoRqrd;
	}
	
	public ONCGroup(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[1]), nextLine[2], Integer.parseInt(nextLine[3]), nextLine[4], nextLine[5]);
		this.name = nextLine[6];
		this.type = GroupType.valueOf(nextLine[7]);
		this.permission = Integer.parseInt(nextLine[8]);
		this.bWebpage = nextLine[9].isEmpty() ? false : nextLine[9].equalsIgnoreCase("T") ? true : false;
		this.bContactInfoRqrd = nextLine[10].isEmpty() ? false : nextLine[10].equalsIgnoreCase("T") ? true : false;
	}
	
	public ONCGroup(ONCGroup g)
	{
		super(g.id, g.dateChanged.getTime(), g.changedBy, g.slPos, g.slMssg, g.slChangedBy);
		this.name = g.name;
		this.type = g.type;
		this.permission = g.permission;
		this.bWebpage = g.bWebpage;
		this.bContactInfoRqrd = g.bContactInfoRqrd;
	}
	
	//getters
	public String getName() { return name; }
	public GroupType getType() { return type; }
	public int getPermission() { return permission; }
	public boolean includeOnWebpage() { return bWebpage; }
	public boolean contactInfoRqrd() { return bContactInfoRqrd; }
	
	//setters
	void setName(String name) { this.name = name; }
	void setType(GroupType type) { this.type = type; }
	void setPermission(int permission) { this.permission = permission; }
	void setIncludeOnWebpage(boolean bWebpage) { this.bWebpage = bWebpage; }
	void setContactInfoRqrd(boolean bContactInfoRqrd) { this.bContactInfoRqrd = bContactInfoRqrd; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Long.toString(dateChanged.getTimeInMillis()), 
					   changedBy, Integer.toString(slPos), slMssg, slChangedBy,
					   name, type.toString(), Integer.toString(permission),
					   bWebpage ? "T" : "F", bContactInfoRqrd ? "T" : "F"};
		return row;
	}
	
	@Override
	public String toString() { return this.name; }
}
