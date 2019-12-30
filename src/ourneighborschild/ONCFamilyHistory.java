package ourneighborschild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ONCFamilyHistory extends ONCObject implements Serializable
{
	/**
	 * This class implements the data structure for Family History objects. When an ONC Family objects 
	 * FamilyStatus or FamilyGift Status changes, this object is created and stored to archive the change
	 */
	private static final long serialVersionUID = 5109480607565108347L;
	
	int famID;
	FamilyStatus familyStatus;
	FamilyGiftStatus giftStatus;
	String dDelBy;
	String dNotes;
	String dChangedBy;
	long dDateChanged;
	int dnsCode;

	//Constructor used after separating ONC Deliveries from ONC Families
	public ONCFamilyHistory(int id, int famid, FamilyStatus fStat, FamilyGiftStatus dStat, String dBy, 
							String notes, String cb, long dateChanged, int dnsCode)
	{
		super(id);
		this.famID = famid;
		this.familyStatus = fStat;
		this.giftStatus = dStat;			
		this.dDelBy = dBy;
		this.dNotes = notes;		
		this.dChangedBy = cb;
		this.dDateChanged = dateChanged;
		this.dnsCode = dnsCode;
	}
		
	//Copy Constructor
	public ONCFamilyHistory(ONCFamilyHistory d)
	{	
		super(d.id);
		famID = d.famID;
		familyStatus = d.familyStatus;
		giftStatus = d.giftStatus;			
		dDelBy = d.dDelBy;
		dNotes = d.dNotes;		
		dChangedBy = d.dChangedBy;
		dDateChanged = System.currentTimeMillis();;
		this.dnsCode = d.dnsCode;
	}
	
	//Constructor used when reading from Delivery .csv file
	public ONCFamilyHistory(String[] del)
	{
		super(Integer.parseInt(del[0]));
		this.famID = Integer.parseInt(del[1]);
		this.familyStatus = FamilyStatus.getFamilyStatus(Integer.parseInt(del[2]));
		this.giftStatus = FamilyGiftStatus.getFamilyGiftStatus(Integer.parseInt(del[3]));			
		this.dDelBy = del[4].isEmpty() ? "" : del[4];
		this.dNotes = del[5].isEmpty() ? "" : del[5];	
		this.dChangedBy = del[6].isEmpty() ? "" : del[6];
		this.dDateChanged = Long.parseLong(del[7]);
		this.dnsCode = del[8].isEmpty() ? -1 : Integer.parseInt(del[8]);
	}

	//Getters
	public int getFamID() { return famID; }
	public FamilyStatus getFamilyStatus() {return familyStatus;}
	public FamilyGiftStatus getGiftStatus() {return giftStatus;}	
	public String getdDelBy() {return dDelBy;}
	String getdNotes() {return dNotes;}
	String getdChangedBy() { return dChangedBy; }
	public Date getDateChanged() 
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(dDateChanged);
		return cal.getTime(); 
	}
	
	Calendar getDateChangedCal() 
	{ 
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(dDateChanged);
		return cal; 
	}
	
	int getDNSCode() { return dnsCode; }
	
	//Setters
	public void setdDelBy(String db) { dDelBy = db; }
	void setdNotes(String s) {dNotes = s; }
	void setdChangedBy(String cb) { dChangedBy = cb; }	
	public void setDateChanged(long calDateChanged) { dDateChanged = calDateChanged; }
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Integer.toString(id), Integer.toString(famID),
							  Integer.toString(familyStatus.statusIndex()),
							  Integer.toString(giftStatus.statusIndex()),
							  dDelBy, dNotes, dChangedBy, Long.toString(dDateChanged),
							  Integer.toString(dnsCode)};
		
		return exportRow;
		
	}
}