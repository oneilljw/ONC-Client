package ourneighborschild;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ONCDelivery extends ONCObject implements Serializable
{
	/**
	 * This class implements the data structure for an ONCDelivery object. It holds an integer 
	 * status of the delivery, a string of who has been assigned or made the delivery, 
	 * notes associated with the delivery, which user created the delivery object and
	 * what date it was created. 
	 */
	private static final long serialVersionUID = 5109480607565108347L;
	
	int famID;
	int dStatus;
	String dDelBy;
	String dNotes;
	String dChangedBy;
	Calendar dDateChanged;

	//Constructor used after separating ONC Deliveries from ONC Families
	public ONCDelivery(int id, int famid, int dStat, String dBy, String notes, String cb, Calendar dateChanged)
	{
		super(id);
		famID = famid;
		dStatus = dStat;			
		dDelBy = dBy;
		dNotes = notes;		
		dChangedBy = cb;
		dDateChanged = Calendar.getInstance();
		dDateChanged = dateChanged;
	}
		
	//Copy Constructor
	public ONCDelivery(ONCDelivery d)
	{	
		super(d.id);
		famID = d.famID;
		dStatus = d.dStatus;			
		dDelBy = d.dDelBy;
		dNotes = d.dNotes;		
		dChangedBy = d.dChangedBy;
		dDateChanged = Calendar.getInstance();
	}
	
	//Constructor used when reading from Delivery .csv file
	public ONCDelivery(String[] del)
	{
		super(Integer.parseInt(del[0]));
		famID = Integer.parseInt(del[1]);
		dStatus = Integer.parseInt(del[2]);			
		dDelBy = del[3].isEmpty() ? "" : del[3];
		dNotes = del[4].isEmpty() ? "" : del[4];	
		dChangedBy = del[5].isEmpty() ? "" : del[5];
		dDateChanged = Calendar.getInstance();
		dDateChanged.setTimeInMillis(Long.parseLong(del[6]));
	}

	//Getters
	public int getFamID() { return famID; }
	public int getdStatus() {return dStatus;}	
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
		String[] exportRow = {Integer.toString(id), Integer.toString(famID), Integer.toString(dStatus),
							  dDelBy, dNotes, dChangedBy, Long.toString(dDateChanged.getTimeInMillis())};
		
		return exportRow;
		
	}
}