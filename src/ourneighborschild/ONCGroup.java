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
	private boolean bShareInfo;
	private boolean bWebpage;
	private boolean bContactInfoRqrd;
	private boolean bMembersRefer;
	private boolean bAllowInProfile;
	
	public ONCGroup(int id, Date today, String changedBy, int slpos, String slmssg, String slchgby, 
						String name, GroupType type, boolean bShareInfo, boolean bWebpage,
						boolean bContactInfoRqrd, boolean bMembersRefer, boolean bAllowInProfile) 
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.name = name;
		this.type = type;
		this.bShareInfo = bShareInfo;
		this.bWebpage = bWebpage;
		this.bContactInfoRqrd = bContactInfoRqrd;
		this.bMembersRefer = bMembersRefer;
		this.bAllowInProfile = bAllowInProfile;
	}
	
	public ONCGroup(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[1]), nextLine[2], Integer.parseInt(nextLine[3]), nextLine[4], nextLine[5]);
		this.name = nextLine[6];
		this.type = GroupType.valueOf(nextLine[7]);
		this.bShareInfo = nextLine[8].isEmpty() ? false : nextLine[8].equalsIgnoreCase("T") ? true : false;
		this.bWebpage = nextLine[9].isEmpty() ? false : nextLine[9].equalsIgnoreCase("T") ? true : false;
		this.bContactInfoRqrd = nextLine[10].isEmpty() ? false : nextLine[10].equalsIgnoreCase("T") ? true : false;
		this.bMembersRefer = nextLine[11].isEmpty() ? false : nextLine[11].equalsIgnoreCase("T") ? true : false;
		this.bAllowInProfile = nextLine[12].isEmpty() ? false : nextLine[12].equalsIgnoreCase("T") ? true : false;
	}
	
	public ONCGroup(ONCGroup g)
	{
		super(g.id, g.dateChanged.getTime(), g.changedBy, g.slPos, g.slMssg, g.slChangedBy);
		this.name = g.name;
		this.type = g.type;
		this.bShareInfo = g.bShareInfo;
		this.bWebpage = g.bWebpage;
		this.bContactInfoRqrd = g.bContactInfoRqrd;
		this.bMembersRefer = g.bMembersRefer;
		this.bAllowInProfile = g.bAllowInProfile;
	}
	
	//getters
	public String getName() { return name; }
	public GroupType getType() { return type; }
	public boolean groupSharesInfo() { return bShareInfo; }
	public boolean includeOnWebpage() { return bWebpage; }
	public boolean contactInfoRqrd() { return bContactInfoRqrd; }
	public boolean memberRefer() { return bMembersRefer; }
	public boolean allowInProfile() { return bAllowInProfile; }
	
	//setters
	void setName(String name) { this.name = name; }
	void setType(GroupType type) { this.type = type; }
	void setGroupSharesInfo(boolean bShareInfo) { this.bShareInfo = bShareInfo; }
	void setIncludeOnWebpage(boolean bWebpage) { this.bWebpage = bWebpage; }
	void setContactInfoRqrd(boolean bContactInfoRqrd) { this.bContactInfoRqrd = bContactInfoRqrd; }
	void setMembersRefer(boolean bMembersRefer) { this.bMembersRefer = bMembersRefer; }
	void setAllowInProfile(boolean bAllowInProfile) { this.bAllowInProfile = bAllowInProfile; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Long.toString(dateChanged.getTimeInMillis()), 
					   changedBy, Integer.toString(slPos), slMssg, slChangedBy,
					   name, type.toString(), bShareInfo ? "T" : "F",
					   bWebpage ? "T" : "F", bContactInfoRqrd ? "T" : "F",
					   bMembersRefer ? "T" : "F", bAllowInProfile ? "T" : "F"};
		return row;
	}
	
	@Override
	public String toString() { return this.name; }
}
