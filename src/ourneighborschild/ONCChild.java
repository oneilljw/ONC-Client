package ourneighborschild;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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
//	private String		sChildDOB;
	private String		sChildAge;
	private int 		nChildAge = -1;	//-1: Unknown, else age in years is valid from 0 (DOB) and older
	private long		childDOB;	//GMT time in milliseconds 
	private int			childWish1ID;
	private int			childWish2ID;
	private int			childWish3ID;
	private int			pyChildID;
		
	//Constructor for a new child created by the user
	public ONCChild(int id, int famid, String fn, String ln, String gender, long dob, String school, int currYear)
	{
		super(id);
		this.famid = famid;
		childNumber = 0; //Will be set once children are sorted in chronological order
		childFirstName = fn;
		childGender = gender;
		childDOB = dob;
		sChildAge = calculateAge(currYear);
		childLastName = ln;
		childSchool = school;
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
		nChildAge = c.nChildAge;
		childSchool = c.childSchool;
		childWish1ID = c.childWish1ID;	//Set the wish id's to "no wish selected"
    	childWish2ID = c.childWish2ID;
    	childWish3ID = c.childWish3ID;
    	pyChildID = c.pyChildID;
    	
//    	SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
//    	sChildDOB = sdf.format(childDOB.getTime());
	}
	
	//Constructor used when importing data base from CSV by the server
	public ONCChild(int currYear, String [] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.famid = Integer.parseInt(nextLine[1]);
		childNumber = Integer.parseInt(nextLine[2]);
		childFirstName = nextLine[3].isEmpty() ? "" : nextLine[3];
		childLastName = nextLine[4].isEmpty() ? "" : nextLine[4];
		childGender = nextLine[5];

		if(nextLine[6].isEmpty())
		{
			Locale locale = new Locale("en", "US");
			TimeZone timezone = TimeZone.getTimeZone("GMT");
			Calendar today = Calendar.getInstance(timezone, locale);
			today.set(Calendar.HOUR_OF_DAY, 0);
		    today.set(Calendar.MINUTE, 0);
		    today.set(Calendar.SECOND, 0);
		    today.set(Calendar.MILLISECOND, 0);
			childDOB = today.getTimeInMillis();
		}
		else
			childDOB = Long.parseLong(nextLine[6]);

		sChildAge = calculateAge(currYear);
		childSchool = nextLine[7].isEmpty() ? "" : nextLine[7];
		childWish1ID = Integer.parseInt(nextLine[8]);	//Set the wish id's to "no wish selected"
    	childWish2ID = Integer.parseInt(nextLine[9]);
    	childWish3ID = Integer.parseInt(nextLine[10]);
    	pyChildID = Integer.parseInt(nextLine[11]);
    	
//    	SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
//    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//    	sChildDOB = sdf.format(childDOB.getTime());
	}
	
	//Constructor that uses ODB/WFCM child name string which has format First Name Last Name - Gender - DOB
	public ONCChild(int id, int famid, String c, int currYear)
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
    	   		
//    		sChildDOB = childdata[2].trim();
       		sChildAge = calculateChildsAgeAndCalendarDOB(childdata[2].trim(), currYear);
    	}
    		
    	else
    	{
    		childFirstName = "MISSING";
    		childLastName = "MISSING";
    		childGender = "Unknown";
//    		sChildDOB = "Unknown";
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
//	int		getChildNumber() { return childNumber; }
	public String		getChildFirstName() {return childFirstName;}
	public String		getChildLastName() {return childLastName;}
	String		getChildSchool() {return childSchool;}
	public String		getChildGender() {return childGender;}
	String		getChildAge(){return sChildAge;}
	int			getChildIntegerAge(){return nChildAge;}
	public int	getPriorYearChildID() { return pyChildID; }
	public Long getChildDateOfBirth() { return childDOB; }
	
	public void setChildDateOfBirth(long dob) { childDOB = dob; }
	
	/**********************************************************************************************
	 * return a GMT based Calendar for DOB
	 * @return
	 */
	public Calendar	getChildDOB()
	{
		Locale locale = new Locale("en", "US");
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		Calendar gmtDOB = Calendar.getInstance(timezone, locale);
		gmtDOB.setTimeInMillis(childDOB);
		
		return gmtDOB;
	}
	
	String	getChildDOBString(String format)
	{
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    	
    	Locale locale = new Locale("en", "US");
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		Calendar gmtDOB = Calendar.getInstance(timezone, locale);
		gmtDOB.setTimeInMillis(childDOB);
    	
    	return sdf.format(gmtDOB.getTime());
	}

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
	
	void updateChildData(String first, String last, String school, String gender, long dob, int currYear)
	{
		childFirstName = first;
		childLastName = last;
		childSchool = school;
		childGender = gender;
		childDOB = dob;
//		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
//		sChildDOB = oncdf.format(dob);
		sChildAge = calculateAge(currYear);
	}

	public void setChildNumber(int cn) {childNumber = cn+1;} //Range is 1 to x
	
	//This method takes the sChildDOB string, determines if its in legitimate date format, and if it is
	//set the childDOB Calendar field and return a string of the childs age. It also sets a member 
	//integer variable, nChildAge, to the actual age (0 and older) or leaves it -1 for invalid DOB's
	String calculateChildsAgeAndCalendarDOB(String zDOB, int currYear)
	{	
//		Locale locale = new Locale("en", "US");
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		Calendar now = Calendar.getInstance(timezone);
		now.clear(Calendar.HOUR_OF_DAY);
		now.clear(Calendar.MINUTE);
		now.clear(Calendar.SECOND);
		now.clear(Calendar.MILLISECOND);
		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
		
		//First, parse the sChildDOB string to create an Calendar variable for DOB
		//If it can't be determined, set DOB = today. 
		if(zDOB.contains("-"))	//ODB format
		{			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		    try 
		    {
		    	
				Calendar gmtDOB = Calendar.getInstance(timezone);
				gmtDOB.setTime(sdf.parse(zDOB));
				childDOB = gmtDOB.getTimeInMillis();
//				sChildDOB = oncdf.format(childDOB.getTime());
			}
		    catch (ParseException e)
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// all done
		}
		else if(zDOB.contains("/"))	//ONC format
		{
			SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		    try 
		    {
				Calendar gmtDOB = Calendar.getInstance(timezone);
				gmtDOB.setTime(oncdf.parse(zDOB));
				childDOB = gmtDOB.getTimeInMillis();
//				sChildDOB = oncdf.format(childDOB.getTime());
			}
		    catch (ParseException e)
		    {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// all done
		    
		}
		else if(zDOB.contains("nknown"))
		{
			childDOB = now.getTimeInMillis();
//			sChildDOB = oncdf.format(childDOB.getTime());
			return "Unknown";
		}
		else
		{
			childDOB = now.getTimeInMillis();
			
//			sChildDOB = oncdf.format(childDOB.getTime());
			JOptionPane.showMessageDialog(null, "DOB is not in ONC or ODB format, age cannot be determined", 
					"Invalid Date of Birth Format", JOptionPane.ERROR_MESSAGE);
			return "";
		}
		
		return calculateAge(currYear);
	}
	
	/****************************************************************************************
	 * Calculates the child's age as of 12/25 in the current season. If the child is at least
	 * one year old on 12/25, their age is calculated in years. If the child is less then a 
	 * year old the child's age is calculated in months. The age relative to 12/25 corrects
	 * the issue of a child's birthday occurring between the time ornament labels are printed
	 * for the child and the time the gift is received showing different ages (one year off).
	 * @return A string of the child's age, in either years or months
	 ***************************************************************************************/
	String calculateAge(int currYear)
	{
		Calendar christmasDayCal = Calendar.getInstance();
		christmasDayCal.set(currYear, Calendar.DECEMBER, 25, 0, 0, 0);
		Calendar dobCal = Calendar.getInstance();
		dobCal.setTimeInMillis(childDOB);
		
		if (dobCal.after(christmasDayCal))
		{
			nChildAge = 0;
			return "Future DOB";
		}
		else
		{		
			int sesaonYear = christmasDayCal.get(Calendar.YEAR);
			int dobYear = dobCal.get(Calendar.YEAR);
			nChildAge = sesaonYear - dobYear;
		
			int december = christmasDayCal.get(Calendar.MONTH);
			int dobMonth = dobCal.get(Calendar.MONTH);
			if (dobMonth > december)	//can't really happen now that we calculate to 12/25
			{
				nChildAge--;
			} 
			else if (december == dobMonth)
			{
				int christmasDay = christmasDayCal.get(Calendar.DAY_OF_MONTH);
				int dobDay = dobCal.get(Calendar.DAY_OF_MONTH);
				if (dobDay > christmasDay)
				{
					nChildAge--;
				}
			}
		
			if(nChildAge > 0)
				return Integer.toString(nChildAge) + " yr. old";
			else if(nChildAge == 0 && dobYear == sesaonYear && dobMonth == december)
				return "Newborn";
			else if(nChildAge == 0 && dobYear == sesaonYear)
				return (Integer.toString(december-dobMonth) + " mos. old");
			else
				return (Integer.toString(december+12-dobMonth) + " mos. old");
		}
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] row= {Long.toString(id), Integer.toString(famid), Integer.toString(childNumber), 
						childFirstName, childLastName, childGender, Long.toString(childDOB), childSchool, 
						Long.toString(childWish1ID), Long.toString(childWish2ID),
						Long.toString(childWish3ID), Long.toString(pyChildID)};
						
		return row;
	}

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
