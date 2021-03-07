package ourneighborschild;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	private String		childFirstName;
	private String		childLastName;
	private String		childSchool;
	private String		childGender;
	private String		sChildAge;
	private int 		nChildAge = -1;	//-1: Unknown, else age in years is valid from 0 (DOB) and older
	private long		childDOB;	//GMT time in milliseconds 
	private int			pyChildID;
		
	//Constructor for a new child created by the user
	public ONCChild(int id, int famid, String fn, String ln, String gender, long dob, String school, int currYear)
	{
		super(id);
		this.famid = famid;
		childFirstName = fn;
		childGender = gender;
		childDOB = dob;
		sChildAge = calculateAge(currYear);
		childLastName = ln;
		childSchool = school;
    	pyChildID = -1;
	}
	
	//Constructor used to make a copy
	public ONCChild(ONCChild c)
	{
		super(c.id);
		this.famid = c.famid;
		childFirstName = c.childFirstName;
		childLastName = c.childLastName;
		childGender = c.childGender;
		childDOB = c.childDOB;
		sChildAge = c.sChildAge;
		nChildAge = c.nChildAge;
		childSchool = c.childSchool;
		pyChildID = c.pyChildID;
	}
	
	//Constructor used when importing data base from CSV by the server
	public ONCChild(int currYear, String [] nextLine)
	{
		super(Integer.parseInt(nextLine[0]));
		this.famid = Integer.parseInt(nextLine[1]);
//		childNumber = Integer.parseInt(nextLine[2]);
		childFirstName = nextLine[2].isEmpty() ? "" : nextLine[2];
		childLastName = nextLine[3].isEmpty() ? "" : nextLine[3];
		childGender = nextLine[4];

		if(nextLine[5].isEmpty())
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
			childDOB = Long.parseLong(nextLine[5]);

		sChildAge = calculateAge(currYear);
		childSchool = nextLine[6].isEmpty() ? "" : nextLine[6];
    	pyChildID = Integer.parseInt(nextLine[7]);
	}
	
	//Constructor that uses ODB/WFCM child name string which has format First Name Last Name - Gender - DOB
	public ONCChild(int id, int famid, String c, int currYear)
	{
		super(id);
		this.famid = famid;	
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
    	   		
       		sChildAge = calculateChildsAgeAndCalendarDOB(childdata[2].trim(), currYear);
		}
		else
		{
    			childFirstName = "MISSING";
    			childLastName = "MISSING";
    			childGender = "Unknown";
    			sChildAge = "Unknown";
		}
    	
		childSchool = "";
    	pyChildID = -1;
	}
	
	//Getters
	public int			getFamID() { return famid; }
	public String		getChildFirstName() {return childFirstName;}
	public String		getChildLastName() {return childLastName;}
	String		getChildSchool() {return childSchool;}
	public String		getChildGender() {return childGender;}
	public String		getChildAge(){return sChildAge;}
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

	public void setPriorYearChildID(int pyid) { pyChildID = pyid; }
	
	void updateChildData(String first, String last, String school, String gender, long dob, int currYear)
	{
		childFirstName = first;
		childLastName = last;
		childSchool = school;
		childGender = gender;
		childDOB = dob;
		sChildAge = calculateAge(currYear);
	}

	//This method takes the sChildDOB string, determines if its in legitimate date format, and if it is
	//set the childDOB Calendar field and return a string of the childs age. It also sets a member 
	//integer variable, nChildAge, to the actual age (0 and older) or leaves it -1 for invalid DOB's
	String calculateChildsAgeAndCalendarDOB(String zDOB, int currYear)
	{	
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
			return "Unknown";
		}
		else
		{
			childDOB = now.getTimeInMillis();
			
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
			{							//unless child's birthday is in the last week of December
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
		String[] row= {Long.toString(id), Integer.toString(famid), childFirstName, childLastName,
						childGender, Long.toString(childDOB), childSchool, Long.toString(pyChildID)};
						
		return row;
	}
}
