package ourneighborschild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

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
	Calendar dDateChanged;

	//Constructor used after separating ONC Deliveries from ONC Families
	public ONCFamilyHistory(int id, int famid, FamilyStatus fStat, FamilyGiftStatus dStat, String dBy, 
							String notes, String cb, Calendar dateChanged)
	{
		super(id);
		famID = famid;
		familyStatus = fStat;
		giftStatus = dStat;			
		dDelBy = dBy;
		dNotes = notes;		
		dChangedBy = cb;
		dDateChanged = Calendar.getInstance();
		dDateChanged = dateChanged;
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
		dDateChanged = Calendar.getInstance();
	}
	
	//Constructor used when reading from Delivery .csv file
	public ONCFamilyHistory(String[] del)
	{
		super(Integer.parseInt(del[0]));
		famID = Integer.parseInt(del[1]);
		familyStatus = FamilyStatus.getFamilyStatus(Integer.parseInt(del[2]));
		giftStatus = FamilyGiftStatus.getFamilyGiftStatus(Integer.parseInt(del[3]));			
		dDelBy = del[4].isEmpty() ? "" : del[4];
		dNotes = del[5].isEmpty() ? "" : del[5];	
		dChangedBy = del[6].isEmpty() ? "" : del[6];
		dDateChanged = Calendar.getInstance();
		dDateChanged.setTimeInMillis(Long.parseLong(del[7]));
	}

	//Getters
	public int getFamID() { return famID; }
	public FamilyStatus getFamilyStatus() {return familyStatus;}
	public FamilyGiftStatus getGiftStatus() {return giftStatus;}	
	public String getdDelBy() {return dDelBy;}
	String getdNotes() {return dNotes;}
	String getdChangedBy() { return dChangedBy; }
	public Date getdChanged() { return dDateChanged.getTime(); }
	
	//Setters
	public void setdDelBy(String db) { dDelBy = db; }
	void setdNotes(String s) {dNotes = s; }
	void setdChangedBy(String cb) { dChangedBy = cb; }	
	void setDateChanged(Date d) { dDateChanged.setTime(d); }
	public void setDateChanged(Calendar calDateChanged) { dDateChanged = calDateChanged; }
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Integer.toString(id), Integer.toString(famID),
							  Integer.toString(familyStatus.statusIndex()),
							  Integer.toString(giftStatus.statusIndex()),
							  dDelBy, dNotes, dChangedBy, Long.toString(dDateChanged.getTimeInMillis())};
		
		return exportRow;
		
	}
}