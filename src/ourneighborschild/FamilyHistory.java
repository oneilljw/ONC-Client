package ourneighborschild;

import java.io.Serializable;

public class FamilyHistory extends ONCObject implements Serializable
{
	/**
	 * This class implements the data structure for Family History objects. When an ONC Family objects 
	 * FamilyStatus or FamilyGift Status changes, this object is created and stored to archive the change
	 */
	private static final long serialVersionUID = 5109480607565108347L;
	
	int famID;
	FamilyStatus familyStatus;
	FamilyGiftStatus giftStatus;
	String deliveredBy;
	String notes;
	String changedBy;
	long timestamp;
	int dnsCode;

	//Constructor used after separating ONC Deliveries from ONC Families
	public FamilyHistory(int id, int famid, FamilyStatus fStat, FamilyGiftStatus dStat, String dBy, 
							String notes, String cb, long dateChanged, int dnsCode)
	{
		super(id);
		this.famID = famid;
		this.familyStatus = fStat;
		this.giftStatus = dStat;			
		this.deliveredBy = dBy;
		this.notes = notes;		
		this.changedBy = cb;
		this.timestamp = dateChanged;
		this.dnsCode = dnsCode;
	}
		
	//Copy Constructor
	public FamilyHistory(FamilyHistory d)
	{	
		super(d.id);
		famID = d.famID;
		familyStatus = d.familyStatus;
		giftStatus = d.giftStatus;			
		deliveredBy = d.deliveredBy;
		notes = d.notes;		
		changedBy = d.changedBy;
		timestamp = System.currentTimeMillis();;
		this.dnsCode = d.dnsCode;
	}
	
	//Constructor used when reading from Delivery .csv file
	public FamilyHistory(String[] del)
	{
		super(Integer.parseInt(del[0]));
		this.famID = Integer.parseInt(del[1]);
		this.familyStatus = FamilyStatus.getFamilyStatus(Integer.parseInt(del[2]));
		this.giftStatus = FamilyGiftStatus.getFamilyGiftStatus(Integer.parseInt(del[3]));			
		this.deliveredBy = del[4].isEmpty() ? "" : del[4];
		this.notes = del[5].isEmpty() ? "" : del[5];	
		this.changedBy = del[6].isEmpty() ? "" : del[6];
		this.timestamp = Long.parseLong(del[7]);
		this.dnsCode = del[8].isEmpty() ? -1 : Integer.parseInt(del[8]);
	}

	//Getters
	public int getFamID() { return famID; }
	public FamilyStatus getFamilyStatus() {return familyStatus;}
	public FamilyGiftStatus getGiftStatus() {return giftStatus;}	
	public String getdDelBy() {return deliveredBy;}
	String getNotes() {return notes;}
	String getChangedBy() { return changedBy; }
	public long getTimestamp() { return timestamp; }
	public int getDNSCode() { return dnsCode; }
	
	//Setters
	public void setFamilyStatus(FamilyStatus fs) { this.familyStatus = fs; }
	public void setFamilyGiftStatus(FamilyGiftStatus fgs) { this.giftStatus = fgs; }
	public void setDeliveredBy(String db) { this.deliveredBy = db; }
	public void setdNotes(String s) { this.notes = s; }
	public void setChangedBy(String cb) { this.changedBy = cb; }	
	public void setDateChanged(long timestamp) { this.timestamp = timestamp; }
	public void setDNSCode( int code) { this.dnsCode = code; }
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Integer.toString(id), Integer.toString(famID),
							  Integer.toString(familyStatus.statusIndex()),
							  Integer.toString(giftStatus.statusIndex()),
							  deliveredBy, notes, changedBy, Long.toString(timestamp),
							  Integer.toString(dnsCode)};
		
		return exportRow;
		
	}
}