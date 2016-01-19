package ourneighborschild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class ONCChildWish extends ONCObject implements Serializable
{
	/***********************************************************************************************************
	 * This class provides the data structure of a child's wish. A child's wish has a base component that
	 * is provided by the wish catalog. It contains a detail field that specifies info about the wish. It
	 * contains an indicator used to sort and manage wishes. It contains a status of the wish as it progresses
	 * thru the life cycle from empty to selected to verified in a family bag. The wish keeps track of who
	 * changed it last and when the change occurred. In addition, the wish keeps track of who is assigned to
	 * fulfill it
	 ************************************************************************************************************/
	private static final long serialVersionUID = -2478929652422873065L;
	
	private int childID;	//id of the child the wish belongs to
	private int wishID;		//id of the wish from the catalog
	private String childWishDetail = "";
	private int wishnumber;
	private int childWishIndicator = 0;	//0 - Blank, 1-*, 2-#
	private WishStatus childWishStatus = WishStatus.Not_Selected;
	private String changedBy = "";	
	private Calendar dateChanged = Calendar.getInstance();
	private int childWishAssigneeID = 0;

	//Constructor for wish created or changed internally		
	public ONCChildWish(int id, int childid, int wishid, String wd, int wishnum, int wi, WishStatus ws, int waID, String cb, Date dc)
	{
		super(id);
		childID = childid;
		wishID = wishid;
		childWishIndicator = wi;
		childWishDetail = wd;
		wishnumber = wishnum;
		childWishStatus = ws;
		childWishAssigneeID = waID;
		changedBy = cb;
		dateChanged.setTime(dc);	    
	}
	
	//Constructor for wish created or changed internally		
	public ONCChildWish(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		childID = Integer.parseInt(nextLine[1]);
		wishID = Integer.parseInt(nextLine[2]);
		childWishDetail = nextLine[3].isEmpty() ? "" : nextLine[3];
		wishnumber = Integer.parseInt(nextLine[4]);
		childWishIndicator = Integer.parseInt(nextLine[5]);
		childWishStatus = WishStatus.valueOf(nextLine[6]);
		changedBy = nextLine[7].isEmpty() ? "" : nextLine[7];
		dateChanged.setTimeInMillis(Long.parseLong(nextLine[8]));
		childWishAssigneeID = Integer.parseInt(nextLine[9]);
	}
	
	public int getChildID() { return childID; }
	public int getWishID() { return wishID; }
	public String getChildWishDetail() { return childWishDetail; }
	public int getWishNumber() { return wishnumber; }
	public int getChildWishIndicator() { return childWishIndicator; }
	public WishStatus getChildWishStatus() {return childWishStatus;}
	public int getChildWishAssigneeID() {return childWishAssigneeID;}
	public String getChildWishChangedBy() {return changedBy;}
	public Calendar getChildWishDateChanged() {return dateChanged;}

	public void setWishID(int id) { wishID = id; }
	void setChildWishDetail(String cwd) { childWishDetail = cwd; }
	void setChildWishIndicator(int cwi) { childWishIndicator = cwi; }
	void setChildWishStatus(WishStatus cws) { childWishStatus = cws;}
	void setChildWishAssigneeID(int id) {childWishAssigneeID = id;}
	void setChildWishChangedBy(String name) {changedBy = name;}
	void setChildWishDateChanged(Calendar dc) {dateChanged = dc;}
	
	boolean isComparisonWishIdentical(ONCChildWish compWish)
	{		
		return compWish.getWishID() == wishID &&
				compWish.getChildWishDetail().equals(childWishDetail) &&
					compWish.getChildWishIndicator() == childWishIndicator &&
						compWish.getChildWishStatus() == childWishStatus &&
							compWish.getChildWishAssigneeID() == childWishAssigneeID;
	}
	
	boolean isComparisonWishAfter(ONCChildWish compWish)
	{
		return compWish.getChildWishDateChanged().after(dateChanged);
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(childID),Integer.toString(wishID), 
						childWishDetail, Integer.toString(wishnumber),
						Integer.toString(childWishIndicator), childWishStatus.toString(), 
						changedBy, Long.toString(dateChanged.getTimeInMillis()),
						Integer.toString(childWishAssigneeID)};
		return row;
	}
}
