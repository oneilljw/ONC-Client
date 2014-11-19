package OurNeighborsChild;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

public class ONCChild extends ONCObject implements Serializable
{
	/**
	 * This class represents a child in ONC and contains member fields for a child
	 */
	private static final long serialVersionUID = -9209587946068856376L;

	private static final String ODB_FAMILY_MEMBER_COLUMN_SEPARATOR = " - ";
	
	private int			famid;
	private int 		childNumber;	//Range is 1 to x, there is no child 0
	private String		childFirstName;
	private String		childLastName;
	private String		childSchool;
	private String		childGender;
	private String		sChildDOB;
	private String		sChildAge;
	private int 		nChildAge = -1;	//-1: Unknown, else age in years is valid from 0 (DOB) and older
	private Calendar	childDOB = Calendar.getInstance();
	private int			childWish1ID;
	private int			childWish2ID;
	private int			childWish3ID;
	private int			pyChildID;
		
	//Constructor for a new child created by the user
	ONCChild(int id, int famid, String fn, String ln, String gender, Date dob, String school)
	{
		super(id);
		this.famid = famid;
		childNumber = 0; //Will be set once children are sorted in chronological order
		childFirstName = fn;
		childGender = gender;
		childDOB = Calendar.getInstance();
		childDOB.setTime(dob);
		childDOB.set(Calendar.HOUR_OF_DAY, 0);
	    childDOB.set(Calendar.MINUTE, 0);
	    childDOB.set(Calendar.SECOND, 0);
	    childDOB.set(Calendar.MILLISECOND, 0);
		sChildAge = calculateAge();
		childLastName = ln;
		childSchool = school;
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
    	sChildDOB = sdf.format(childDOB.getTime());
    	
    	childWish1ID = -1;	//Set the wish id's to "no wish selected"
    	childWish2ID = -1;
    	childWish3ID = -1;
    	pyChildID = -1;
	}
	
	//Constructor used to make a copy
	public ONCChild(ONCChild c)
	{
		super(c.id);
		this.famid = c.famid;
		childNumber = c.childNumber;
		childFirstName = c.childFirstName;
		childLastName = c.childLastName;
		childGender = c.childGender;
		childDOB = c.childDOB;
		sChildAge = c.sChildAge;
		childSchool = c.childSchool;
		childWish1ID = c.childWish1ID;	//Set the wish id's to "no wish selected"
    	childWish2ID = c.childWish2ID;
    	childWish3ID = c.childWish3ID;
    	pyChildID = c.pyChildID;
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
    	sChildDOB = sdf.format(childDOB.getTime());
	}
	
	//Constructor used when importing data base from CSV
	public ONCChild(String [] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.famid = Integer.parseInt(nextLine[1]);
		childNumber = Integer.parseInt(nextLine[2]);
		childFirstName = nextLine[3].isEmpty() ? "" : nextLine[3];
		childLastName = nextLine[4].isEmpty() ? "" : nextLine[4];
		childGender = nextLine[5];
		childDOB = Calendar.getInstance();
		childDOB.setTimeInMillis(Long.parseLong(nextLine[6]));
		childDOB.set(Calendar.HOUR_OF_DAY, 0);
	    childDOB.set(Calendar.MINUTE, 0);
	    childDOB.set(Calendar.SECOND, 0);
	    childDOB.set(Calendar.MILLISECOND, 0);
		sChildAge = calculateAge();
		childSchool = nextLine[7].isEmpty() ? "" : nextLine[7];
		childWish1ID = Integer.parseInt(nextLine[8]);	//Set the wish id's to "no wish selected"
    	childWish2ID = Integer.parseInt(nextLine[9]);
    	childWish3ID = Integer.parseInt(nextLine[10]);
    	pyChildID = Integer.parseInt(nextLine[11]);
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
    	sChildDOB = sdf.format(childDOB.getTime());
	}
	
	//Constructor that uses ODB/WFCM child name string which has format First Name Last Name - Gender - DOB
	ONCChild(int id, int famid, String c)
	{
		super(id);
		this.famid = famid;
		childNumber = 0; //Will be set once children are sorted in chronological order
		
		String[] childdata = c.split(ODB_FAMILY_MEMBER_COLUMN_SEPARATOR, 3);
		
    	if(c.length() > 0 && childdata.length == 3)
    	{  		   		
    		String allnames = childdata[0].trim();
    		String[] names = allnames.split(" ", 2);
    		childFirstName = names[0].trim();
    		childLastName = names[1].trim();
    		
    		String childgender = childdata[1].trim();
    		if(childgender.contains("Male"))
    			childGender = "Boy";
    		else if(childgender.contains("Female") || c.contains("female"))
    			childGender = "Girl";
    		else
    			childGender = "Unknown";
    	   		
    		sChildDOB = childdata[2].trim();
       		sChildAge = CalculateChildsAgeAndCalendarDOB();
    	}
    		
    	else
    	{
    		childFirstName = "MISSING";
    		childLastName = "MISSING";
    		childGender = "Unknown";
    		sChildDOB = "Unknown";
    		sChildAge = "Unknown";
    	}
    	
    	childSchool = "";
    	
    	childWish1ID = -1;	//Set the wish id's to "no wish selected"
    	childWish2ID = -1;
    	childWish3ID = -1;
    	pyChildID = -1;
	}
	
	//Getters
	public int			getFamID() { return famid; }
	public String		getChildFirstName() {return childFirstName;}
	public String		getChildLastName() {return childLastName;}
	String		getChildSchool() {return childSchool;}
	public String		getChildGender() {return childGender;}
	String		getChildDOBString() {return sChildDOB;}
	String		getChildAge(){return sChildAge;}
	int			getChildIntegerAge(){return nChildAge;}
	public int			getPriorYearChildID() { return pyChildID; }
	public Calendar	getChildDOB() {return childDOB;}

	public int getChildWishID(int wn)
	{
		int wishid = -1;
		
		if(wn==0)
			wishid = childWish1ID;
		else if(wn==1)
			wishid = childWish2ID;
		else if(wn==2)
			wishid = childWish3ID;
		
		return wishid;
	}
	
	//Setters
	public void setChildWishID(int wishid, int wn)
	{
		if(wn == 0)
			childWish1ID = wishid;
		else if(wn == 1)
			childWish2ID = wishid;
		else if(wn == 2)
			childWish3ID = wishid;
	}
	
	public void setPriorYearChildID(int pyid) { pyChildID = pyid; }
	
	void updateChildData(String first, String last, String school, String gender, Date dob)
	{
		childFirstName = first;
		childLastName = last;
		childSchool = school;
		childGender = gender;
		childDOB.setTime(dob);
		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
		sChildDOB = oncdf.format(dob);
		sChildAge = calculateAge();
	}

	public void setChildNumber(int cn) {childNumber = cn+1;} //Range is 1 to x
	
	//This method takes the sChildDOB string, determines if its in legitimate date format, and if it is
	//set the childDOB Calendar field and return a string of the childs age. It also sets a member 
	//integer variable, nChildAge, to the actual age (0 and older) or leaves it -1 for invalid DOB's
	String CalculateChildsAgeAndCalendarDOB()
	{		
		Calendar now = Calendar.getInstance();	//Need to zero the hour, min, sec fields
		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
		
		//First, parse the sChildDOB string to create an Calendar variable for DOB
		//If it can't be determined, set DOB = today. 
		if(sChildDOB.contains("-"))	//ODB format
		{			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    try 
		    {
				childDOB.setTime(sdf.parse(sChildDOB));
				sChildDOB = oncdf.format(childDOB.getTime());
			}
		    catch (ParseException e)
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// all done
		}
		else if(sChildDOB.contains("/"))	//ONC format
		{
		    try 
		    {
				childDOB.setTime(oncdf.parse(sChildDOB));
				sChildDOB = oncdf.format(childDOB.getTime());
			}
		    catch (ParseException e)
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// all done
		    
		}
		else if(sChildDOB.contains("nknown"))
		{
			childDOB = now;
			sChildDOB = oncdf.format(childDOB.getTime());
			return "Unknown";
		}
		else
		{
			childDOB = now;
			sChildDOB = oncdf.format(childDOB.getTime());
			JOptionPane.showMessageDialog(null, "DOB is not in ONC or ODB format, age cannot be determined", 
					"Invalid Date of Birth Format", JOptionPane.ERROR_MESSAGE);
			return "";
		}
		
		//Only use day, month and year in the app
		 childDOB.clear(Calendar.HOUR_OF_DAY);
		 childDOB.clear(Calendar.MINUTE);
		 childDOB.clear(Calendar.SECOND);
		 childDOB.clear(Calendar.MILLISECOND);
		
		return calculateAge();
	}
	
	String calculateAge()
	{
		Calendar now = Calendar.getInstance();
		
		if (childDOB.after(now))
		{
		  return "Future DOB";
		}
		else
		{		
			int year1 = now.get(Calendar.YEAR);
			int year2 = childDOB.get(Calendar.YEAR);
			nChildAge = year1 - year2;
		
			int month1 = now.get(Calendar.MONTH);
			int month2 = childDOB.get(Calendar.MONTH);
			if (month2 > month1)
			{
				nChildAge--;
			} 
			else if (month1 == month2)
			{
				int day1 = now.get(Calendar.DAY_OF_MONTH);
				int day2 = childDOB.get(Calendar.DAY_OF_MONTH);
				if (day2 > day1)
				{
					nChildAge--;
				}
			}
		
			if(nChildAge > 0)
				return Integer.toString(nChildAge) + " yr. old";
			else if(nChildAge == 0 && year2==year1 && month2 == month1)
				return "Newborn";
			else if(nChildAge == 0 && year2==year1)
				return (Integer.toString(month1-month2) + " mos. old");
			else
				return (Integer.toString(month1+12-month2) + " mos. old");
		}
	}
	
	@Override
	public String[] getDBExportRow()
	{
		String[] row= {Long.toString(id), Integer.toString(famid), Integer.toString(childNumber), 
						childFirstName, childLastName, childGender, 
						Long.toString(childDOB.getTimeInMillis()), childSchool, 
						Long.toString(childWish1ID), Long.toString(childWish2ID),
						Long.toString(childWish3ID), Long.toString(pyChildID)};
						
		return row;
	}
/*	
	boolean mergeWishes(ONCChild mc)
	{
		ArrayList<ChildWishHistory> mcwhAL = mc.getChildWishHistoryAL();
		boolean bChildWishDataChangeDetected = false;
		//Check that the number of wishes is equal. For ONC, this should be 3 per child currently
		//If they are not, don't merge
		if(mcwhAL.size() == childWishesAL.size())	 
		{
			for(int index=0; index< childWishesAL.size(); index++)
			{
				if(childWishesAL.get(index).mergeWishHistories(mcwhAL.get(index).getWishHistory()))
					bChildWishDataChangeDetected = true;
			}
		}
		
		return bChildWishDataChangeDetected;
	}
*/	
	/*******************************************************************************
	 * After a merge, method is called to verify assignees are confirmed for each of
	 * the child's current wishes. If the confirmed status isn't verified, a change 
	 * was made to the organization's status after the fork and prior to the merge of
	 * the selected merge file. In that case, reset the ID and Name of the assignee
	 * to the initial condition as if no assignment had occurred.
	 ******************************************************************************/
/*	
	void verifyCurrentWishAssignees(ONCOrgs orgs, String user, Date today)
	{
		for(int wn=0; wn<childWishesAL.size(); wn++)
		{
			//If the wish has been assigned an organization, check if status of the organization
			//is confirmed. 0 = not confirmed or not found. If it is no longer confirmed, 
			//reset the assigned organization to none and reset status to selected. 
			//Add the revised wish to the wish history
			if(getChildWishAssigneeID(wn) > 0 && 
					orgs.getConfirmedOrganizationIndex(getChildWishAssigneeID(wn)) == 0)	
			{	
				AddWish(new ONCChildWish(getChildWishBase(wn), getChildWishDetail(wn), 
											getChildWishIndicator(wn), WISHSTATUS_SELECTED,
											ORGANIZATION_UNNASSIGNED, ORGANIZATION_UNNAMED, 
											user, today), wn);
			}
		}			
	}
	
	int compareCurrentWishAssignees(long orgID)
	{
		int assignee_count = 0;
		for(int wn=0; wn<childWishesAL.size(); wn++)
			if(getChildWishAssigneeID(wn) == orgID)
				assignee_count++;
		return assignee_count;
	}
*/	
	class ChildWishHistory implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1487985298719174837L;
		private ArrayList<ONCChildWish> wishHistoryAL = new ArrayList<ONCChildWish>();
			
		ChildWishHistory()
		{
//			wishHistory.add(new ONCChildWish());
		}
			
		void addWish(ONCChildWish wish)
		{
			wishHistoryAL.add(wish);
		}
			
		ONCChildWish getCurrentWish(){return wishHistoryAL.get(wishHistoryAL.size()-1);}
		ArrayList<ONCChildWish> getWishHistory() {return wishHistoryAL;}
		
		/**************************************************************************************************
		* Method takes a child wish history array list of child wishes and merges it with the current
		* wish history list. Any identical wishes are ignored. Non identical wishes are added immediately
		* after the last current wish that was generated (time stamped) before a merge wish. 
		* @param mwhAL
		* @return
		******************************************************************************************************/
		boolean mergeWishHistories(ArrayList<ONCChildWish> mwhAL)
		
		{
			boolean bWishChangeDetected = false;
			for(ONCChildWish mw:mwhAL)
			{
				//Check to see if merge wish is identical to a current wish in wish history array list
				boolean bWishIdentical = false;
				for(ONCChildWish cw:wishHistoryAL)
					if(cw.isComparisonWishIdentical(mw))
						bWishIdentical = true;
							
				if(!bWishIdentical) //if no identical wish found, place merge wish in wish history array list
				{
					//Place merge wish based on time stamp
					int index = 0 ;
					while (index < wishHistoryAL.size() && wishHistoryAL.get(index).isComparisonWishAfter(mw))
						index++;
					
					if(index == wishHistoryAL.size())
						wishHistoryAL.add(mw);					
					else
						wishHistoryAL.add(index, mw);
					
					bWishChangeDetected = true;
				}
			}
			
			return bWishChangeDetected;
		}
	}
}
