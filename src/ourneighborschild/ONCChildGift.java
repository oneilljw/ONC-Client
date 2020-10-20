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
	private int assignee0ID = 0;
//	private PartnerGiftStatus part0GiftStatus;
//	private int assignee1ID = 0;
//	private PartnerGiftStatus part1GiftStatus;
	
	//Constructor for wish created or changed internally		
	public ONCChildGift(int id, int childid, int wishid, String wd, int wishnum, int wi, GiftStatus ws,
							int wa0ID, String cb, long dc)
	{
		super(id);
		childID = childid;
		giftID = wishid;
		indicator = wi;
		giftDetail = wd;
		giftnumber = wishnum;
		status = ws;
		assignee0ID = wa0ID;
//		part0GiftStatus = PartnerGiftStatus.Unassigned;
//		assignee1ID = -1;
//		part1GiftStatus = PartnerGiftStatus.Unassigned;
		changedBy = cb;
		timestamp = dc;	    
	}
/*
	//Constructor for wish created or changed internally		
	public ONCChildGift(int id, int childid, int wishid, String wd, int wishnum, int wi, GiftStatus ws,
						int wa0ID, PartnerGiftStatus p0gs, int wa1ID, PartnerGiftStatus p1gs, String cb, long dc)
	{
		super(id);
		childID = childid;
		giftID = wishid;
		indicator = wi;
		giftDetail = wd;
		giftnumber = wishnum;
		status = ws;
		assignee0ID = wa0ID;
		part0GiftStatus = p0gs;
		assignee1ID = wa1ID;
		part1GiftStatus = p1gs;
		changedBy = cb;
		timestamp = dc;	    
	}
*/	
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
		
		try
		{
			assignee0ID = Integer.parseInt(nextLine[9]);
		}
		catch (NumberFormatException nfe)
		{
			assignee0ID = -1;
		}
/*		
		try
		{
			int statusIndex = Integer.parseInt(nextLine[10]);
			part0GiftStatus = PartnerGiftStatus.getPartnerGiftStatus(statusIndex);
		}
		catch (NumberFormatException nfe)
		{
			part0GiftStatus = PartnerGiftStatus.Unassigned;
		}
		
		try
		{
			assignee0ID = Integer.parseInt(nextLine[11]);
		}
		catch (NumberFormatException nfe)
		{
			assignee0ID = -1;
		}
		
		try
		{
			int statusIndex = Integer.parseInt(nextLine[12]);
			part1GiftStatus = PartnerGiftStatus.getPartnerGiftStatus(statusIndex);
		}
		catch (NumberFormatException nfe)
		{
			part1GiftStatus = PartnerGiftStatus.Unassigned;
		}
*/		
	}
	
	public int getChildID() { return childID; }
	public int getGiftID() { return giftID; }
	public String getDetail() { return giftDetail; }
	public int getGiftNumber() { return giftnumber; }
	public int getIndicator() { return indicator; }
	public GiftStatus getGiftStatus() {return status;}
	public int getPartnerID() {return assignee0ID;}
//	public PartnerGiftStatus getPartner0GiftStatus() { return part0GiftStatus; }
//	public int getPartner1ID() {return assignee1ID;}
//	public PartnerGiftStatus getPartner1GiftStatus() { return part1GiftStatus; }
	public String getChangedBy() {return changedBy;}
	public Long getTimestamp() { return timestamp; }
	public Calendar getDateChanged() 
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(timestamp);
		return calendar;
	}

	public void setWishID(int id) { giftID = id; }
	public void setChildWishDetail(String cwd) { giftDetail = cwd; }
	void setChildWishIndicator(int cwi) { indicator = cwi; }
	public void setChildWishStatus(GiftStatus cws) { status = cws;}
	public void setChildWishAssignee0ID(int id) { this.assignee0ID = id;}
//	public void setChildWishPartner0GiftStatus(PartnerGiftStatus pgs) { this.part0GiftStatus = pgs; }
//	public void setChildWishAssignee1ID(int id) { this.assignee1ID = id;}
//	public void setChildWishPartner1GiftStatus(PartnerGiftStatus pgs) { this.part1GiftStatus = pgs; }
	void setChildWishChangedBy(String name) {changedBy = name;}
	void setChildWishDateChanged(long dc) {timestamp = dc;}
	
//	boolean isComparisonWishIdentical(ONCChildGift compWish)
//	{		
//		return compWish.getGiftID() == giftID &&
//				compWish.getDetail().equals(giftDetail) &&
//					compWish.getIndicator() == indicator &&
//						compWish.getGiftStatus() == status &&
//							compWish.getPartner0ID() == assignee0ID;
//	}
	
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
						Integer.toString(assignee0ID),
//						Integer.toString(part0GiftStatus.statusIndex()),
//						Integer.toString(assignee1ID),
//						Integer.toString(part1GiftStatus.statusIndex())
						};
		return row;
	}
}
