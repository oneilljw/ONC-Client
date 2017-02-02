package ourneighborschild;

import java.util.Date;

public class ONCGroup extends ONCEntity 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private GroupType type;
	private int permission;
	
	public ONCGroup(int id, Date today, String changedBy, int slpos,
						String slmssg, String slchgby, String name, GroupType type, int permission) 
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.name = name;
		this.type = type;
		this.permission = permission;
	}
	
	public ONCGroup(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[1]), nextLine[2], Integer.parseInt(nextLine[3]), nextLine[4], nextLine[5]);
		this.name = nextLine[6];
		this.type = GroupType.valueOf(nextLine[7]);
		this.permission = Integer.parseInt(nextLine[8]);
	}
	
	public ONCGroup(ONCGroup g)
	{
		super(g.id, g.dateChanged.getTime(), g.changedBy, g.slPos, g.slMssg, g.slChangedBy);
		this.name = g.name;
		this.type = g.type;
		this.permission = g.permission;
	}
	
	//getters
	public String getName() { return name; }
	GroupType getType() { return type; }
	public int getPermission() { return permission; }
	
	//setters
	void setName(String name) { this.name = name; }
	void setType(GroupType type) { this.type = type; }
	void setPermission(int permission) { this.permission = permission; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Long.toString(dateChanged.getTimeInMillis()), 
					   changedBy, Integer.toString(slPos), slMssg, slChangedBy,
					   name, type.toString(), Integer.toString(permission)};
		return row;
	}
}
