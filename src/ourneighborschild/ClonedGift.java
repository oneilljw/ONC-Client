package ourneighborschild;

import java.util.Calendar;
import java.util.TimeZone;

public class ClonedGift extends ONCObject
{
	private int childID;	//id of the child the wish belongs to
	private int giftID;		//id of gift from the Gift Catalog: NOTE: it's not the CHILD GIFT database id
	private String giftDetail;
	private int giftnumber;
	private int indicator;	//0 - Blank, 1-*, 2-#
	private ClonedGiftStatus status;
	private String changedBy;	
	private long timestamp;
	private int assignee0ID;
	private int priorIDInChain;
	private int nextIDInChain;
	
	public ClonedGift(int id, String changedBy, ONCChildGift sourceGift, int offset)
	{
		super(id);
		this.childID = sourceGift.getChildID();
		this.giftID = sourceGift.getGiftID();
		this.indicator = sourceGift.getIndicator();
		this.giftDetail = sourceGift.getDetail();
		this.giftnumber = sourceGift.getGiftNumber() + offset;
		this.status = ClonedGiftStatus.Unassigned;
		this.assignee0ID = -1;
		this.changedBy = changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorIDInChain = -1;
		this.nextIDInChain = -1;
	}
	
	public ClonedGift(String changedBy, ClonedGift priorGift)
	{
		super(priorGift.id);
		this.childID = priorGift.childID;
		this.giftID = priorGift.giftID;
		this.indicator = priorGift.indicator;
		this.giftDetail = priorGift.giftDetail;
		this.giftnumber = priorGift.giftnumber;
		this.status = priorGift.status;
		this.assignee0ID = priorGift.assignee0ID;
		this.changedBy = changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorIDInChain = priorGift.priorIDInChain;
		this.nextIDInChain = -1;
	}
	
	//used when receiving gifts
	public ClonedGift(ClonedGiftStatus status, ClonedGift priorGift)
	{
		super(priorGift.id);
		this.childID = priorGift.childID;
		this.giftID = priorGift.giftID;
		this.indicator = priorGift.indicator;
		this.giftDetail = priorGift.giftDetail;
		this.giftnumber = priorGift.giftnumber;
		this.status = status;
		this.assignee0ID = priorGift.assignee0ID;
		this.changedBy = priorGift.changedBy;
		this.timestamp = System.currentTimeMillis();	
		this.priorIDInChain = priorGift.priorIDInChain;
		this.nextIDInChain = -1;
	}
	
	//Constructor for cloned gift read from .csv file		
	public ClonedGift(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		childID = Integer.parseInt(nextLine[1]);
		giftID = Integer.parseInt(nextLine[2]);
		giftDetail = nextLine[3].isEmpty() ? "" : nextLine[3];
		giftnumber = Integer.parseInt(nextLine[4]);
		indicator = Integer.parseInt(nextLine[5]);
		status = ClonedGiftStatus.valueOf(nextLine[6]);
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
		
		try
		{
			priorIDInChain = Integer.parseInt(nextLine[10]);
		}
		catch (NumberFormatException nfe)
		{
			priorIDInChain = -1;
		}
		
		try
		{
			nextIDInChain = Integer.parseInt(nextLine[11]);
		}
		catch (NumberFormatException nfe)
		{
			nextIDInChain = -1;
		}
	}
	
	public int getChildID() { return childID; }
	public int getGiftID() { return giftID; }
	public String getDetail() { return giftDetail; }
	public int getGiftNumber() { return giftnumber; }
	public int getIndicator() { return indicator; }
	public ClonedGiftStatus getGiftStatus() {return status;}
	public int getPartnerID() {return assignee0ID;}
	public String getChangedBy() {return changedBy;}
	public Long getTimestamp() { return timestamp; }
	public Calendar getDateChanged() 
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTimeInMillis(timestamp);
		return calendar;
	}
	public int getPriorID() { return priorIDInChain; }
	public int getNextID() { return nextIDInChain; }

	public void setWishID(int id) { giftID = id; }
	public void setGiftDetail(String cwd) { giftDetail = cwd; }
	void setChildWishIndicator(int cwi) { indicator = cwi; }
	public void setGiftStatus(ClonedGiftStatus cws) { status = cws;}
	public void setGiftAssignee0ID(int id) { this.assignee0ID = id;}
	void setChangedBy(String name) {changedBy = name;}
	void setDateChanged(long dc) {timestamp = dc;}
	public void setPriorID(int priorIDInChain) { this.priorIDInChain = priorIDInChain; }
	public void setNextID(int nextIDInChain) { this.nextIDInChain = nextIDInChain; }

	@Override
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(childID),Integer.toString(giftID), 
						giftDetail, Integer.toString(giftnumber),
						Integer.toString(indicator), status.toString(), 
						changedBy, Long.toString(timestamp),
						Integer.toString(assignee0ID),
						Integer.toString(priorIDInChain),
						Integer.toString(nextIDInChain)
						};
		return row;
	}
}
