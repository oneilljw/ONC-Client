package ourneighborschild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;

public class ONCChildGift extends ONCObject implements Serializable
{
	/***********************************************************************************************************
	 * This class provides the data structure of a child's gift. A child's gift has a base component that
	 * is provided by the gift catalog. It contains a detail field that specifies info about the gift. It
	 * contains an indicator used to sort and manage gifts. It contains a status of the gift as it progresses
	 * thru the life cycle from empty to selected to verified in a family bag. The gift keeps track of who
	 * changed it last and when the change occurred. In addition, the gift keeps track of who is assigned to
	 * fulfill it.
	 ************************************************************************************************************/
	private static final long serialVersionUID = -2478929652422873065L;
	
	private int childID;	//id of the child the wish belongs to
	private int giftID;		//id of gift from the Gift Catalog: NOTE: it's not the CHILD GIFT database id
	private String giftDetail = "";
	private int giftnumber;
	private int indicator = 0;	//0 - Blank, 1-*, 2-#
	private GiftStatus status = GiftStatus.Not_Selected;
	private String changedBy = "";	
	private long timestamp = System.currentTimeMillis();
	private int assigneeID = 0;

	//Constructor for wish created or changed internally		
	public ONCChildGift(int id, int childid, int wishid, String wd, int wishnum, int wi, GiftStatus ws, int waID, String cb, long dc)
	{
		super(id);
		childID = childid;
		giftID = wishid;
		indicator = wi;
		giftDetail = wd;
		giftnumber = wishnum;
		status = ws;
		assigneeID = waID;
		changedBy = cb;
		timestamp = dc;	    
	}
	
	//Constructor for wish created or changed internally		
	public ONCChildGift(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		childID = Integer.parseInt(nextLine[1]);
		giftID = Integer.parseInt(nextLine[2]);
		giftDetail = nextLine[3].isEmpty() ? "" : nextLine[3];
		giftnumber = Integer.parseInt(nextLine[4]);
		indicator = Integer.parseInt(nextLine[5]);
		status = GiftStatus.valueOf(nextLine[6]);
		changedBy = nextLine[7].isEmpty() ? "" : nextLine[7];
		timestamp = Long.parseLong(nextLine[8]);
		assigneeID = Integer.parseInt(nextLine[9]);
	}
	
	public int getChildID() { return childID; }
	public int getGiftID() { return giftID; }
	public String getDetail() { return giftDetail; }
	public int getGiftNumber() { return giftnumber; }
	public int getIndicator() { return indicator; }
	public GiftStatus getGiftStatus() {return status;}
	public int getPartnerID() {return assigneeID;}
	public String getChangedBy() {return changedBy;}
	public Calendar getDateChanged() 
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(timestamp);
		
//		if(childID == 184 && status == GiftStatus.Received && giftDetail.equals("Large dinosaur"))
//			System.out.println("ONCChildGift.getDateChanged: calendar:" + calendar);
		
		return calendar;
	}

	public void setWishID(int id) { giftID = id; }
	public void setChildWishDetail(String cwd) { giftDetail = cwd; }
	void setChildWishIndicator(int cwi) { indicator = cwi; }
	public void setChildWishStatus(GiftStatus cws) { status = cws;}
	public void setChildWishAssigneeID(int id) {assigneeID = id;}
	void setChildWishChangedBy(String name) {changedBy = name;}
	void setChildWishDateChanged(long dc) {timestamp = dc;}
	
	boolean isComparisonWishIdentical(ONCChildGift compWish)
	{		
		return compWish.getGiftID() == giftID &&
				compWish.getDetail().equals(giftDetail) &&
					compWish.getIndicator() == indicator &&
						compWish.getGiftStatus() == status &&
							compWish.getPartnerID() == assigneeID;
	}
	
	boolean isComparisonWishAfter(ONCChildGift compWish)
	{
		return compWish.getDateChanged().after(timestamp);
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(childID),Integer.toString(giftID), 
						giftDetail, Integer.toString(giftnumber),
						Integer.toString(indicator), status.toString(), 
						changedBy, Long.toString(timestamp),
						Integer.toString(assigneeID)};
		return row;
	}
}
