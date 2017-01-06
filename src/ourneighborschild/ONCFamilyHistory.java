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
	FamilyGiftStatus giftStatus;
	String dDelBy;
	String dNotes;
	String dChangedBy;
	Calendar dDateChanged;

	//Constructor used after separating ONC Deliveries from ONC Families
	public ONCFamilyHistory(int id, int famid, FamilyGiftStatus dStat, String dBy, String notes, String cb, Calendar dateChanged)
	{
		super(id);
		famID = famid;
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
		giftStatus = FamilyGiftStatus.getFamilyGiftStatus(Integer.parseInt(del[2]));			
		dDelBy = del[3].isEmpty() ? "" : del[3];
		dNotes = del[4].isEmpty() ? "" : del[4];	
		dChangedBy = del[5].isEmpty() ? "" : del[5];
		dDateChanged = Calendar.getInstance();
		dDateChanged.setTimeInMillis(Long.parseLong(del[6]));
	}

	//Getters
	public int getFamID() { return famID; }
	public FamilyGiftStatus getdStatus() {return giftStatus;}	
	public String getdDelBy() {return dDelBy;}
	String getdNotes() {return dNotes;}
	String getdChangedBy() { return dChangedBy; }
	Date getdChanged() { return dDateChanged.getTime(); }
	
	//Setters
	void setdDelBy(String db) { dDelBy = db; }
	void setdNotes(String s) {dNotes = s; }
	void setdChangedBy(String cb) { dChangedBy = cb; }	
	void setDateChanged(Date d) { dDateChanged.setTime(d); }
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Integer.toString(id), Integer.toString(famID), Integer.toString(giftStatus.statusIndex()),
							  dDelBy, dNotes, dChangedBy, Long.toString(dDateChanged.getTimeInMillis())};
		
		return exportRow;
		
	}
}