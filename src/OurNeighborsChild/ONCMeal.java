package OurNeighborsChild;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ONCMeal extends ONCEntity
{
	/**
	 * This class is a pojo associated with meals. 
	 */
	private static final long serialVersionUID = 1L;
	
	protected int famID;
	protected MealType type;
	protected String dietaryRestrictions;
	protected int status;
	
	public ONCMeal(int id, int famID, int status, MealType type, String restrictions, 
			String changedBy, Date today, int slpos, String slmssg, String slchgby)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.famID = famID;
		this.status = status;
		this.type = type;
		this.dietaryRestrictions = restrictions;
	}
	
	//Constructor for wish created or changed internally		
	public ONCMeal(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[6]),
				nextLine[5].isEmpty() ? "" : nextLine[7], Integer.parseInt(nextLine[7]),
				nextLine[8].isEmpty() ? "" : nextLine[8], nextLine[9].isEmpty() ? "" : nextLine[9]);
		
		famID = Integer.parseInt(nextLine[1]);
		type = MealType.valueOf(nextLine[2]);
		status = Integer.parseInt(nextLine[3]);
		dietaryRestrictions = nextLine[4].isEmpty() ? "" : nextLine[4];
	}
	
	//getters
	public int getFamilyID() { return famID; }
	public int getStatus() { return status; }
	public MealType getType() { return type;}
	public String getRestricitons() { return dietaryRestrictions; }
	
	//setters
	public void setFamilyID(int famID) { this.famID = famID; }
	public void setStatus(int status) { this.status = status; }
	public void setType(MealType type) { this.type = type; }
	public void setRestrictions(String restrictions) { this.dietaryRestrictions = restrictions; }

	@Override
	public String[] getExportRow()
	{
		List<String> row = new ArrayList<String>();
		
		row.add(Integer.toString(id));
		row.add(Integer.toString(famID));		
		row.add(type.toString());
		row.add(Integer.toString(status));
		row.add(dietaryRestrictions);
		row.add(changedBy);
		row.add(Long.toString(dateChanged.getTimeInMillis()));
		row.add(Integer.toString(slPos));
		row.add(slMssg);
		row.add(slChangedBy);

		String[] rowArr = new String[row.size()];
		rowArr = row.toArray(rowArr);
		return rowArr;		
	}
	
	private enum MealType 
	{
		None,
		Thanksgiving,
		December,
		Both;
	}
}
