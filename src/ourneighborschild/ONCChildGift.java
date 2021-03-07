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
	private int catalogGiftID;		//id of gift from the Gift Catalog: NOTE: it's not the CHILD GIFT database id
	private String detail = "";
	private int giftnumber;
	private int restriction = 0;	//0 - Blank, 1-*, 2-#
	private GiftStatus status = GiftStatus.Not_Selected;
	private String changedBy = "";	
	private long timestamp = System.currentTimeMillis();
	private int partnerID = 0;
	private int priorID;
	private int nextID;
	private boolean bClonedGift;
	
	//Constructor for wish created or changed internally		
	public ONCChildGift(int id, int childid, int catGiftID, String detail, int giftnum, int restriction, GiftStatus giftStatus,
							int partnerID, String cb, long dc)
	{
		super(id);
		this.childID = childid;
		this.catalogGiftID = catGiftID;
		this.restriction = restriction;
		this.detail = detail;
		this.giftnumber = giftnum;
		this.status = giftStatus;
		this.partnerID = partnerID;
		this.changedBy = cb;
		this.timestamp = dc;
		this.priorID = -1;
		this.nextID = -1;
		this.bClonedGift = false;
	}

	//Constructor for wish created or changed internally		
	public ONCChildGift(String[] nextLine, boolean bClonedGift)
	{
		super(Integer.parseInt(nextLine[0]));
		childID = Integer.parseInt(nextLine[1]);
		catalogGiftID = Integer.parseInt(nextLine[2]);
		detail = nextLine[3].isEmpty() ? "" : nextLine[3];
		giftnumber = Integer.parseInt(nextLine[4]);
		restriction = Integer.parseInt(nextLine[5]);
		try
		{
			status = GiftStatus.getGiftStatus(Integer.parseInt(nextLine[6]));
		}
		catch (NumberFormatException nfe)
		{
			status = GiftStatus.Not_Selected;
		}
		
		changedBy = nextLine[7].isEmpty() ? "" : nextLine[7];
		timestamp = Long.parseLong(nextLine[8]);
		
		try
		{
			this.partnerID = nextLine[9].isEmpty() ? -1 : Integer.parseInt(nextLine[9]);
		}
		catch (NumberFormatException nfe)
		{
			this.partnerID = -1;
		}
		try
		{
			this.priorID = nextLine[10].isEmpty() ? -1 : Integer.parseInt(nextLine[10]);
		}
		catch (NumberFormatException nfe)
		{
			this.priorID = -1;
		}
		try
		{
			this.nextID = nextLine[11].isEmpty() ? -1 : Integer.parseInt(nextLine[11]);
		}
		catch (NumberFormatException nfe)
		{
			this.nextID = -1;
		}
		this.bClonedGift = bClonedGift;
	}
	
	public ONCChildGift(int id, String changedBy, ONCChildGift sourceGift, int offset)
	{
		super(-1);
		this.childID = sourceGift.getChildID();
		this.catalogGiftID = sourceGift.getCatalogGiftID();
		this.restriction = sourceGift.getIndicator();
		this.detail = sourceGift.getDetail();
		this.giftnumber = sourceGift.getGiftNumber() + offset;
//		this.status = ClonedGiftStatus.Unassigned;
		this.status = GiftStatus.Unassigned;
		this.partnerID = -1;
		this.changedBy = changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorID = -1;
		this.nextID = -1;
		this.bClonedGift = true;
	}
	
	public ONCChildGift(String changedBy, ONCChildGift priorGift)
	{
		super(-1);
		this.childID = priorGift.childID;
		this.catalogGiftID = priorGift.catalogGiftID;
		this.restriction = priorGift.restriction;
		this.detail = priorGift.detail;
		this.giftnumber = priorGift.giftnumber;
		this.status = priorGift.status;
		this.partnerID = priorGift.partnerID;
		this.changedBy = changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorID = priorGift.priorID;
		this.nextID = -1;
		this.bClonedGift = true;
	}
	
	//used when receiving gifts
//	public ClonedGift(ClonedGiftStatus status, ClonedGift priorGift)
	public ONCChildGift(GiftStatus status, ONCChildGift priorGift, boolean bClonedGift)
	{
		super(-1);
		this.childID = priorGift.childID;
		this.catalogGiftID = priorGift.catalogGiftID;
		this.restriction = priorGift.restriction;
		this.detail = priorGift.detail;
		this.giftnumber = priorGift.giftnumber;
		this.status = status;
		this.partnerID = priorGift.partnerID;
		this.changedBy = priorGift.changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorID = priorGift.priorID;
		this.nextID = -1;
		this.bClonedGift = bClonedGift;
	}
	
	//getters
	public int getChildID() { return childID; }
	public int getCatalogGiftID() { return catalogGiftID; }
	public String getDetail() { return detail; }
	public int getGiftNumber() { return giftnumber; }
	public int getIndicator() { return restriction; }
	public GiftStatus getGiftStatus() {return status;}
	public int getPartnerID() {return partnerID;}
	public String getChangedBy() {return changedBy;}
	public Long getTimestamp() { return timestamp; }
	public int getPriorID() { return priorID; }
	public int getNextID() { return nextID; }
	public Calendar getDateChanged() 
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(timestamp);
		return calendar;
	}
	public boolean isClonedGift() { return bClonedGift; }

	//setters
	public void setCatalogGiftID(int id) { catalogGiftID = id; }
	public void setDetail(String cwd) { detail = cwd; }
	void setIndicator(int cwi) { restriction = cwi; }
	public void setGiftStatus(GiftStatus cws) { status = cws;}
	public void setPartnerID(int id) { this.partnerID = id;}
	public void setChangedBy(String name) {changedBy = name;}
	public void setTimestamp(long dc) {timestamp = dc;}
	public void setPriorID(int priorID) { this.priorID = priorID; }
	public void setNextID(int nextID) { this.nextID = nextID; }
	
	@Override
	public String[] getExportRow()
	{
		String[] row= { Integer.toString(id),
						Integer.toString(childID),
						Integer.toString(catalogGiftID), 
						detail, 
						Integer.toString(giftnumber),
						Integer.toString(restriction), 
						Integer.toString(status.statusIndex()), 
						changedBy,
						Long.toString(timestamp),
						Integer.toString(partnerID),
						Integer.toString(priorID),
						Integer.toString(nextID)
					  };
		return row;
	}
}
