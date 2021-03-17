package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public class ONCMeal extends ONCEntity
{
	/**
	 * This class is a pojo associated with meals. 
	 */
	private static final long serialVersionUID = 1L;
	
	private int famID;
	private MealStatus status;
	private MealType type;
	private String dietaryRestrictions;
	private int partnerID;
//	private int priorID;
//	private int nextID;
	
	public ONCMeal(int id, int famID, MealStatus status, MealType type, String restrictions, 
			int partnerID, String changedBy, long today, int slpos, String slmssg, String slchgby)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.famID = famID;
		this.status = status;
		this.type = type;
		this.dietaryRestrictions = restrictions;
		this.partnerID = partnerID;
//		this.priorID = -1;
//		this.nextID = -1;
	}
	
	public ONCMeal(ONCMeal m)	//copy constructor
	{
		super(m.id, System.currentTimeMillis(),m.changedBy, m.slPos, m.slMssg, m.slChangedBy);
		this.famID = m.famID;
		this.status = m.status;
		this.type = m.type;
		this.dietaryRestrictions = m.dietaryRestrictions;
		this.partnerID = m.partnerID;
//		this.priorID = -1;
//		this.nextID = -1;
	}
	
	//Constructor for meal read from .csv file		
	public ONCMeal(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), parseTimestamp(nextLine[7]),
				nextLine[6].isEmpty() ? "" : nextLine[6], Integer.parseInt(nextLine[8]),
				nextLine[9].isEmpty() ? "" : nextLine[9], nextLine[10].isEmpty() ? "" : nextLine[10]);
		
		famID = Integer.parseInt(nextLine[1]);
		status = MealStatus.valueOf(nextLine[2]);
		type = MealType.valueOf(nextLine[3]);
		partnerID = Integer.parseInt(nextLine[4]);
		dietaryRestrictions = nextLine[5].isEmpty() ? "" : nextLine[5];
//		this.priorID = nextLine[11].isEmpty() ? -1 : Integer.parseInt(nextLine[11]);
//		this.nextID = nextLine[12].isEmpty() ? -1 : Integer.parseInt(nextLine[12]);
	}
	
	//getters
	public int getFamilyID() { return famID; }
	public MealStatus getStatus() { return status; }
	public MealType getType() { return type;}
	public String getRestricitons() { return dietaryRestrictions; }
	public int getPartnerID() { return partnerID; }
//	public int getPriorID() { return priorID; }
//	public int getNextID() { return nextID; }
	
	//setters
	public void setFamilyID(int famID) { this.famID = famID; }
	public void setMealStatus(MealStatus status) { this.status = status; }
	public void setType(MealType type) { this.type = type; }
	public void setRestrictions(String restrictions) { this.dietaryRestrictions = restrictions; }
	public void setPartnerID(int id) { partnerID = id; }
//	public void setPriorID(int priorID) { this.priorID = priorID; }
//	public void setNextID(int nextID) { this.nextID = nextID; }
	
	@Override
	public String[] getExportRow()
	{
		List<String> row = new ArrayList<String>();
		
		row.add(Integer.toString(id));
		row.add(Integer.toString(famID));
		row.add(status.toString());
		row.add(type.name());
		row.add(Integer.toString(partnerID));
		row.add(dietaryRestrictions);
		row.add(changedBy);
		row.add(Long.toString(timestamp));
		row.add(Integer.toString(slPos));
		row.add(slMssg);
		row.add(slChangedBy);
//		row.add(Integer.toString(priorID));
//		row.add(Integer.toString(nextID));

		String[] rowArr = new String[row.size()];
		rowArr = row.toArray(rowArr);
		return rowArr;		
	}
	
	public String getPrintRow()
	{
		return String.format("mealID=%d, famID=%d, status= %s, type=%s, res=%s, partnerID=%d, cb=%s",
				id, famID, status.toString(), type.toString(), dietaryRestrictions, partnerID, changedBy); 
	}
}
