package OurNeighborsChild;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class ONCChildWish extends ONCObject implements Serializable
{
	/***********************************************************************************************************
	 * This class provides the data structure of a child's wish. A child's wish has a base component that
	 * is provided by the wish catalog. It contains a detail field that specifies info about the wish. It
	 * contains an indicator used to sort and manage wishes. It contains a status of the wish as it progresses
	 * thru the life cycle from empty to selected to verified in a family bag. The wish keeps track of who
	 * changed it last and when the change occurred. In addition, the wish keeps track of who is assigned to
	 * fulfill it
	 ************************************************************************************************************/
	private static final long serialVersionUID = -2478929652422873065L;
	
	private int childID;	//Id of the child the wish belongs to
	private int wishID;	//Id of the wish from the catalog
//	private String childWishBase = "None";
	private String childWishDetail = "";
	private int wishnumber;
	private int childWishIndicator = 0;	//0 - Blank, 1-*, 2-#
	private int childWishStatus = 1;
	private String changedBy = "";	
	private Calendar dateChanged = Calendar.getInstance();
	private int childWishAssigneeID = 0;
//	private String childWishAssigneeName = "None";

/*	
	public ONCChildWish(String cw, String cb, Date dc)	//constructor for wish read from ONC .csv file
	{
		String[] pWish = cw.split("-", 2);
		
		if(pWish[0].length() > 0 && pWish[0].charAt(0) == '*')
			childWishIndicator = 1;		
		else if(pWish[0].length() > 0 && pWish[0].charAt(0) == '#')
			childWishIndicator = 2;
		
		String[] importwishes = {"Accessories", "Baby Item", "Bedding" , "Bike", "Blanket","Book", "Boots",
				"CD", "Coat", "Clothes", "Craft", "Dress", "Doll", "DVD","Education", "Electronic", "Game",
				"Gift Card", "Helmet", "Jewelery", "Musical Inst", "Puzzle","Sports", "Scooter",
				"Shoes", "Skateboard", "Special Req.", "Stroller", "Toy", "Video Game", "Book or Game", 
				"Game or Book", "Games or Puzzles", "Books & Puzzles", "Books or Puzzles"};
		
		for(int i=0; i<importwishes.length; i++)
		{
			if(pWish[0].contains(importwishes[i]))
			{
				childWishBase = importwishes[i];
				if(i == 23 || i == 25)	//Scooter & Skateboard now have /Hlmt appended in 2013
					childWishBase += "/Hlmt";
			}	
		}
			
		if(!childWishBase.equals("None"))
			childWishStatus = 2; //Child wish was not empty when read
			
		if(pWish.length > 1)
			childWishDetail = pWish[1].trim();
		
		changedBy = cb;		
		dateChanged.setTime(dc);
	}
*/	
	//Constructor for wish merge		
	public ONCChildWish( String wd, int wi, int ws, int waID, String cb, Date dc)
	{
		super(0);
		this.id=0;
		childID = 0;
		wishID = 0;
//		childWishBase = wb;
		childWishIndicator = wi;
		childWishDetail = wd;
		wishnumber = 0;
		childWishStatus = ws;
		childWishAssigneeID = waID;
//		childWishAssigneeName = waName;
		changedBy = cb;
		dateChanged.setTime(dc);	    
	}
	
	//Constructor for wish created or changed internally		
	public ONCChildWish(int id, int childid, int wishid, String wd, int wishnum, int wi, int ws, int waID, String cb, Date dc)
	{
		super(id);
		childID = childid;
		wishID = wishid;
//		childWishBase = wb;
		childWishIndicator = wi;
		childWishDetail = wd;
		wishnumber = wishnum;
		childWishStatus = ws;
		childWishAssigneeID = waID;
//		childWishAssigneeName = waName;
		changedBy = cb;
		dateChanged.setTime(dc);	    
	}
	
	//Constructor for wish created or changed internally		
	public ONCChildWish(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		childID = Integer.parseInt(nextLine[1]);
		wishID = Integer.parseInt(nextLine[2]);
//		childWishBase = getDBString(nextLine[3]);
		childWishDetail = getDBString(nextLine[3]);
		wishnumber = Integer.parseInt(nextLine[4]);
		childWishIndicator = Integer.parseInt(nextLine[5]);
		childWishStatus = Integer.parseInt(nextLine[6]);
		changedBy = getDBString(nextLine[7]);
		dateChanged.setTimeInMillis(Long.parseLong(nextLine[8]));
		childWishAssigneeID = Integer.parseInt(nextLine[9]);
//		childWishAssigneeName = getDBString(nextLine[11]);
	}
	
	String getDBString(String s)
	{
		return s.isEmpty() ? "" : s;
	}
	
	public int getChildID() { return childID; }
	public int getWishID() { return wishID; }
//	public String getChildWishBase() {return childWishBase;}
	public String getChildWishDetail() { return childWishDetail; }
	public int getWishNumber() { return wishnumber; }
	int getChildWishIndicator() { return childWishIndicator; }
	public int getChildWishStatus() {return childWishStatus;}
	public int getChildWishAssigneeID() {return childWishAssigneeID;}
//	String getChildWishAssigneeName() {return childWishAssigneeName;}
	public String getChildWishChangedBy() {return changedBy;}
	public Calendar getChildWishDateChanged() {return dateChanged;}
//	public String getChildWishBaseAndDetail() { return childWishBase + "- " + childWishDetail; }
/*	
	String getChildWishAll()
	{		
		String s = "";
		if(childWishIndicator == 1)
			s="*";
		if(childWishIndicator == 2)
			s="#";
		
		return s + childWishBase + "- " +  childWishDetail;
	}
*/		
	public void setWishID(int id) { wishID = id; }
	void setChildWishDetail(String cwd) { childWishDetail = cwd; }
	void setChildWishIndicator(int cwi) { childWishIndicator = cwi; }
	void setChildWishStatus(int cws) { childWishStatus = cws;}
	void setChildWishAssigneeID(int id) {childWishAssigneeID = id;}
//	void setChildWishAssigneeName(String n) {childWishAssigneeName = n; }
	void setChildWishChangedBy(String name) {changedBy = name;}
	void setChildWishDateChanged(Calendar dc) {dateChanged = dc;}
	
	boolean isComparisonWishIdentical(ONCChildWish compWish)
	{		
		return compWish.getWishID() == wishID &&
				compWish.getChildWishDetail().equals(childWishDetail) &&
					compWish.getChildWishIndicator() == childWishIndicator &&
						compWish.getChildWishStatus() == childWishStatus &&
							compWish.getChildWishAssigneeID() == childWishAssigneeID;
	}
	
	boolean isComparisonWishAfter(ONCChildWish compWish)
	{
		return compWish.getChildWishDateChanged().after(dateChanged);
	}
	
	@Override
	public String[] getDBExportRow()
	{
		String[] row= {Integer.toString(id), Integer.toString(childID),Integer.toString(wishID), 
						childWishDetail, Integer.toString(wishnumber),
						Integer.toString(childWishIndicator), Integer.toString(childWishStatus), 
						changedBy, Long.toString(dateChanged.getTimeInMillis()),
						Integer.toString(childWishAssigneeID)};
		return row;
	}
}
