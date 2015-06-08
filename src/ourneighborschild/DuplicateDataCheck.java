package ourneighborschild;

import java.util.ArrayList;
import java.util.Calendar;

public class DuplicateDataCheck 
{
	private static final int MAX_YEARS_OF_BIRTH = 30;
	
	boolean duplicateChildCheck(ArrayList<ONCFamily> fAL, boolean[] criteria, ChildDB cDB, ArrayList<DupItem> dupAL)
	{
		//Check for valid criteria. If no valid check criteria abort the comparison
		if(!criteria[0] && !criteria[1] && !criteria[2] && !criteria[3] && !criteria[4])
		{
			dupAL.add(new DupItem(null, null, "No Criteria", null, null));
			return false;
		}
		
		if(!criteria[0]) 	//Checking for duplicate children within a family only, ONC Numbers must match
		{
			for(ONCFamily f:fAL)
			{
				if(isNumeric(f.getONCNum()))
				{
					for(int i=0; i<cDB.getNumberOfChildrenInFamily(f.getID()); i++)
					{
						ONCChild sc = cDB.getChildren(f.getID()).get(i);
						CompareChild sourceChild = new CompareChild(f, sc);
						
						for(int j=0; j<cDB.getNumberOfChildrenInFamily(f.getID()); j++)
						{
							if(i != j)	//Don't compare a child to himself
							{
								ONCChild tc = cDB.getChildren(f.getID()).get(j);
								CompareChild targetChild = new CompareChild(f, tc);
								//Compare the children, if they compare, then they are dupes
								dupChildCheck(f, sourceChild, targetChild, criteria, dupAL);
					
							}
						}
					}
				}
			}
		}
		else	//Checking for duplicate children in different families only, ONC numbers must be different
		{	
			//Create ArrayList of each child in database for all families that have valid ONC numbers
			ArrayList<CompareChild> ccAL = new ArrayList<CompareChild>();
			
			for(ONCFamily f:fAL)
			{
				if(isNumeric(f.getONCNum()))
					for(ONCChild c:cDB.getChildren(f.getID()))
						ccAL.add(new CompareChild(f, c));	
			}
		
			//Sort the children into array lists by birth year
			ArrayList<ArrayList<CompareChild>> ccYOBAL = new ArrayList<ArrayList<CompareChild>>();
					
			for(int i=0; i<MAX_YEARS_OF_BIRTH; i++)	//Create an array list for each year of birth
				ccYOBAL.add(new ArrayList<CompareChild>());
			
			//Separate the children into each year by birth year
			for(CompareChild cc:ccAL)
			{
				int index =  Calendar.getInstance().get(Calendar.YEAR) - cc.getChild().getChildDOB().get(Calendar.YEAR);
				
				//Check for over age children and children with future birthdates
				if(index >= MAX_YEARS_OF_BIRTH)
					index = MAX_YEARS_OF_BIRTH-1;
				else if(index < 0)
					index = 0;
										
				ccYOBAL.get(index).add(cc);
			}
					
			//Compare each child for a duplicate. Check to see that a child's age doesn't cause
			//an array index to be out of bounds
			for(CompareChild sourceChild:ccAL)
			{
				int byIndex = sourceChild.getBYIndex();
				
				if(byIndex >= MAX_YEARS_OF_BIRTH)
					byIndex = MAX_YEARS_OF_BIRTH-1;
				else if(byIndex < 0)
					byIndex = 0;
				
				for(CompareChild targetChild:ccYOBAL.get(byIndex))
					dupChildCheck(sourceChild.getFamily(), sourceChild, targetChild, criteria, dupAL);
			}
		}
	
		if (dupAL.size() > 0)
			return true;
		else 
		{
			dupAL.add(new DupItem(null, null, "No Matches", null, null));
			return false;
		}
		
	}
	
	boolean duplicateFamilyCheck(ArrayList<ONCFamily> fAL, boolean[] criteria, ArrayList<DupItem> dupAL)
	{
		//Check for valid criteria. If no valid check criteria abort the comparison
		if(!criteria[0] && !criteria[1] && !criteria[2] && !criteria[3] && !criteria[4])
		{
			dupAL.add(new DupItem(null, null,"No Criteria",null, null));
			return false;
		}
		else
		{			
			//Compare each family in the database to every other family. Do not compare families with the same ONCID, they are the 
			//same family
			for(ONCFamily sourceFamily: fAL)
				for(ONCFamily targetFamily:fAL)
					if(sourceFamily.getID() != targetFamily.getID())
						dupFamilyCheck(sourceFamily, targetFamily, criteria, dupAL);	
		}
	
		if (dupAL.size() > 0)
			return true;
		else 
		{
			dupAL.add(new DupItem(null, null, "No Matches", null, null));
			return false;
		}
		
	}
	
	void dupChildCheck(ONCFamily sourceFam, CompareChild sourceChild, CompareChild targetChild, boolean[] criteria, ArrayList<DupItem> dupAL)
	{
		if(sourceChild.isChildDuplicate(targetChild, criteria))
		{
			dupAL.add(new DupItem(sourceChild.getFamily(), sourceChild.getChild(), 
							"matches",
							targetChild.getFamily(), targetChild.getChild()));	
		}	
	}
	
	boolean dupFamilyCheck(ONCFamily sourceFamily, ONCFamily targetFamily, boolean[] criteria, ArrayList<DupItem> dupAL)
	{
		if(sourceFamily.isFamilyDuplicate(targetFamily, criteria))
		{		
			dupAL.add(new DupItem(sourceFamily, null, "matches", targetFamily, null));
			
			return true;
		}
		
		return false;
	}
	
	public static boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	private class CompareChild
	{
		private ONCFamily f;
		private ONCChild c;
		
		CompareChild(ONCFamily f, ONCChild c)
		{
			this.f = f;
			this.c = c;
		}
		
		//getters
		int getBYIndex(){ return Calendar.getInstance().get(Calendar.YEAR) - c.getChildDOB().get(Calendar.YEAR); }
		ONCFamily getFamily() { return f; }
		ONCChild getChild() { return c; }
		
		boolean isChildDuplicate(CompareChild cc, boolean[] criteria)
		{
			if(criteria[0])	//Children must be in different families
				return !cc.getFamily().getONCNum().equals(f.getONCNum()) &&
					doesDOBMatch(cc.getChild().getChildDateOfBirth(), criteria[1]) &&
					 doesGenderMatch(cc.getChild().getChildGender(), criteria[2]) &&
					  doesFirstNameMatch(cc.getChild().getChildFirstName(), criteria[3]) &&
					   doesLastNameMatch(cc.getChild().getChildLastName(), criteria[4]) &&
					    doesLastNamePartiallyMatch(cc.getChild().getChildLastName(), criteria[5]);
			
			else	//Children must be in the same family
				return cc.getFamily().getONCNum().equals(f.getONCNum()) &&
						doesDOBMatch(cc.getChild().getChildDateOfBirth(), criteria[1]) &&
						 doesGenderMatch(cc.getChild().getChildGender(), criteria[2]) &&
						  doesFirstNameMatch(cc.getChild().getChildFirstName(), criteria[3]) &&
						   doesLastNameMatch(cc.getChild().getChildLastName(), criteria[4]) &&
						    doesLastNamePartiallyMatch(cc.getChild().getChildLastName(), criteria[5]);		
		}
	
		boolean doesDOBMatch(long ccdob, boolean criteria) { return !criteria || (criteria && c.getChildDateOfBirth() == ccdob); }
		boolean doesGenderMatch(String gen, boolean criteria) { return !criteria || (criteria && c.getChildGender().equalsIgnoreCase(gen)); }
		boolean doesFirstNameMatch(String fn, boolean criteria) { return !criteria || (criteria && c.getChildFirstName().equalsIgnoreCase(fn)); }
		boolean doesLastNameMatch(String ln, boolean criteria) { return !criteria || (criteria && c.getChildLastName().equalsIgnoreCase(ln)); }
		boolean doesLastNamePartiallyMatch(String ln, boolean criteria) { return !criteria || (criteria && c.getChildLastName().toLowerCase().contains(ln.toLowerCase())); }		
	}
}
