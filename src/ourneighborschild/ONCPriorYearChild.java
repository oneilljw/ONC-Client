package ourneighborschild;

import java.io.Serializable;

public class ONCPriorYearChild extends ONCObject implements Serializable
{	
	/**
	 * Represents a prior year child to allow user to view gift history when choosing
	 * current year gifts. Provides a comparison method that determines whether a current
	 * child matches this prior year child based on last name, gender and date of birth
	 */
	private static final long serialVersionUID = 5504459743415463631L;
//	private String		childFirstName;		//Removed from match criteria due to nicknames
	private String		childLastName;
	private String		childGender;
	private long		childDOB;
	private String		lychildWish1;
	private String		lychildWish2;
	private String 		lychildWish3;
	private String		pychildWish1;
	private String		pychildWish2;
	private String 		pychildWish3;
	
	//Constructor used for data from Prior Year Child Database .csv file format. Only available to superusers
	public ONCPriorYearChild(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		childLastName = nextLine[1].isEmpty() ? "" : nextLine[1];
		childGender = nextLine[2].isEmpty() ? "" : nextLine[2];
		childDOB= Long.parseLong(nextLine[3]);
		lychildWish1 =  nextLine[4].isEmpty() ? "" : nextLine[4];
		lychildWish2 =  nextLine[5].isEmpty() ? "" : nextLine[5];
		lychildWish3 =  nextLine[6].isEmpty() ? "" : nextLine[6];
		pychildWish1 =  nextLine[7].isEmpty() ? "" : nextLine[7];
		pychildWish2 =  nextLine[8].isEmpty() ? "" : nextLine[8];
		pychildWish3 =  nextLine[9].isEmpty() ? "" : nextLine[9];
	}
		
	//Constructor used for data from an existing ONCChild object
	public ONCPriorYearChild(int id, ONCChild c, String wish1, String wish2, String wish3)
	{
		super(id);
//		childFirstName = c.getChildFirstName();
		childLastName = c.getChildLastName();
		childGender = c.getChildGender();
		childDOB = c.getChildDateOfBirth();
		lychildWish1 = wish1.isEmpty() ? "" : wish1;
		lychildWish2 = wish2.isEmpty() ? "" : wish2;
		lychildWish3 = wish3.isEmpty() ? "" : wish3;
		pychildWish1 = "";
		pychildWish2 = "";
		pychildWish3 = "";
	}
	
	//constructor used to search for a prior year child
	public ONCPriorYearChild(int id, ONCChild c)
	{
		super(id);
//		childFirstName = c.getChildFirstName();
		childLastName = c.getChildLastName();
		childGender = c.getChildGender();
		childDOB = c.getChildDateOfBirth();
		lychildWish1 = "";
		lychildWish2 = "";
		lychildWish3 = "";
		pychildWish1 = "";
		pychildWish2 = "";
		pychildWish3 = "";
	}
	
	//Constructor used when retaining a prior year child for a new year
	public ONCPriorYearChild(int id, ONCPriorYearChild pyc)
	{
		super(id);
//		childFirstName = c.getChildFirstName();
		childLastName = pyc.childLastName;
		childGender = pyc.childGender;
		childDOB = pyc.childDOB;
		lychildWish1 = "";
		lychildWish2 = "";
		lychildWish3 = "";
		pychildWish1 = pyc.lychildWish1;
		pychildWish2 = pyc.lychildWish2;
		pychildWish3 = pyc.lychildWish3;
	}
	
	public String getLastName() { return childLastName; }
	public String getGender() { return childGender; }
	public long getDOB() { return childDOB; }

	public void setDOB(long dob) { childDOB = dob; }
		
	public String[] getPriorYearWishes()
	{
		String[] pyWishes = {lychildWish1, lychildWish2, lychildWish3,
								pychildWish1, pychildWish2, pychildWish3};
		return pyWishes;
	}
	

	//Determines if a child is a match with this prior year child. Returns true if the child's
	//last Name, gender and date of birth match			
	public boolean isMatch(String cfn, String cln, String gender, long dob)
	{
		return childLastName.equalsIgnoreCase(cln) && 	// REMOVED CHILD FIRST NAME COMPARISON - NICKNAME ISSUE
				childGender.equalsIgnoreCase(gender) && childDOB == dob;	
	}
	
	public void makeLastYearWishesPriorYearWishes()
	{		
		pychildWish1 = lychildWish1;
		pychildWish2 = lychildWish2;
		pychildWish3 = lychildWish3;
		lychildWish1 = "";
		lychildWish2 = "";
		lychildWish3 = "";
	}
	
	public boolean onlyHasEmptyWishes()
	{
		return lychildWish1.isEmpty() && lychildWish2.isEmpty() && lychildWish3.isEmpty() &&
					pychildWish1.isEmpty() && pychildWish2.isEmpty() && pychildWish3.isEmpty();
	}
	
	public boolean hasLastYearWishes()
	{
		return !(lychildWish1.isEmpty() && lychildWish2.isEmpty() && lychildWish3.isEmpty());
	}
			
	public void addChildWishes(String wish1, String wish2, String wish3)
	{
		lychildWish1 = wish1;
		lychildWish2 = wish2;
		lychildWish3 = wish3;
	}
	
	public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), childLastName, childGender, 
						Long.toString(childDOB),
						lychildWish1, lychildWish2, lychildWish3,
						pychildWish1, pychildWish2, pychildWish3};				
		return row;
	}
}

