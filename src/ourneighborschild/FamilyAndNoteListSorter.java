package ourneighborschild;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FamilyAndNoteListSorter
{
	private FamilyDB familyDB;
	private DNSCodeDB dnsCodeDB;
	private VolunteerDB volunteerDB;
	private MealDB mealDB;
	private RegionDB regionDB;
	
	public FamilyAndNoteListSorter()
	{
		familyDB = FamilyDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
		volunteerDB = VolunteerDB.getInstance();
		mealDB = MealDB.getInstance();
		regionDB = RegionDB.getInstance();
	}
	
	boolean sortFamilyAndNoteDB(List<ONCFamilyAndNote> fal, String dbField)
	{
		boolean bSortOccurred = true;
		
		if(dbField.equals("ONC")) { Collections.sort(fal, new ONCFamilyAndNoteONCNumComparator()); }
		else if(dbField.equals("Batch #")) { Collections.sort(fal, new ONCFamilyAndNoteBatchNumComparator()); }
		else if(dbField.equals("Ref #")) { Collections.sort(fal, new ONCFamilyAndNoteReferenceNumComparator()); }
		else if(dbField.equals("DNS")) { Collections.sort(fal, new ONCFamilyAndNoteDNSComparator()); }
		else if(dbField.equals("Fam Status")) { Collections.sort(fal, new ONCFamilyAndNoteStatusComparator()); }
		else if(dbField.equals("Gift Status")) { Collections.sort(fal, new ONCFamilyAndNoteGiftStatusComparator()); }
		else if(dbField.equals("Meal Status")) { Collections.sort(fal, new ONCFamilyAndNoteMealStatusComparator()); }
		else if(dbField.equals("First")) { Collections.sort(fal, new ONCFamilyAndNoteFNComparator()); }
		else if(dbField.equals("Last")) { Collections.sort(fal, new ONCFamilyAndNoteLNComparator()); }
		else if(dbField.equals("House")) { Collections.sort(fal, new ONCFamilyAndNoteHouseNumComparator()); }
		else if(dbField.equals("Street")) { Collections.sort(fal, new ONCFamilyAndNoteStreetComparator()); }
		else if(dbField.equals("Zip")) { Collections.sort(fal, new ONCFamilyAndNoteZipComparator()); }
		else if(dbField.equals("Reg")) { Collections.sort(fal, new ONCFamilyAndNoteRegionComparator()); }
		else if(dbField.equals("School")) { Collections.sort(fal, new ONCFamilyAndNoteSchoolComparator()); }
		else if(dbField.equals("Changed By")) { Collections.sort(fal, new ONCFamilyAndNoteCallerComparator()); }
		else if(dbField.equals("GCO")) { Collections.sort(fal, new ONCFamilyAndNoteGiftCardOnlyComparator()); }
		else if(dbField.equals("SL")) { Collections.sort(fal, new ONCFamilyAndNoteStoplightComparator()); }
		else if(dbField.equals("# Bikes")) { Collections.sort(fal, new ONCFamilyAndNoteBikesComparator()); }
		else if(dbField.equals("Deliverer")) { Collections.sort(fal, new ONCFamilyAndNoteDelivererComparator()); }
		else if(dbField.equals("CB")) { Collections.sort(fal, new ONCFamilyAndNoteNoteStatusComparator()); }
		else
			bSortOccurred = false;
		
		return bSortOccurred;
	}
	
	private static boolean isNumeric(String str)
	{
		if(str == null || str.isEmpty())
			return false;
		else
			return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	private class ONCFamilyAndNoteONCNumComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			if(isNumeric(o1.getFamily().getONCNum()) && isNumeric(o2.getFamily().getONCNum()))
			{
				Integer onc1 = Integer.parseInt(o1.getFamily().getONCNum());
				Integer onc2 = Integer.parseInt(o2.getFamily().getONCNum());
				return onc1.compareTo(onc2);
			}
			else if(isNumeric(o1.getFamily().getONCNum()) && !isNumeric(o2.getFamily().getONCNum()))
				return -1;
			else if(!isNumeric(o1.getFamily().getONCNum()) && isNumeric(o2.getFamily().getONCNum()))
				return 1;
			else
				return o1.getFamily().getONCNum().compareTo(o2.getFamily().getONCNum());
		}
	}
		
	private class ONCFamilyAndNoteDNSComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{	
			if(o1.getFamilyHistory().getDNSCode() == -1 && o2.getFamilyHistory().getDNSCode() == -1)
				return 0;
			else if(o1.getFamilyHistory().getDNSCode() == -1 && o2.getFamilyHistory().getDNSCode() > -1)
				return 1;
			else if(o1.getFamilyHistory().getDNSCode() > -1 && o2.getFamilyHistory().getDNSCode() == -1)
				return -1;
			else
				return dnsCodeDB.getDNSCode(o1.getFamilyHistory().getDNSCode()).getAcronym().compareTo(
						dnsCodeDB.getDNSCode(o2.getFamilyHistory().getDNSCode()).getAcronym());
		}
	}
		
	private class ONCFamilyAndNoteBatchNumComparator implements Comparator<ONCFamilyAndNote>
	{
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
		
			return o1.getFamily().getBatchNum().compareTo(o2.getFamily().getBatchNum());
		}
	}
	
	private class ONCFamilyAndNoteReferenceNumComparator implements Comparator<ONCFamilyAndNote>
	{
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
		
			return o1.getFamily().getReferenceNum().compareTo(o2.getFamily().getReferenceNum());
		}
	}
	
	private class ONCFamilyAndNoteStatusComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return o1.getFamilyHistory().getFamilyStatus().compareTo(o2.getFamilyHistory().getFamilyStatus());
		}
	}
	
	private class ONCFamilyAndNoteGiftStatusComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return o1.getFamilyHistory().getGiftStatus().compareTo(o2.getFamilyHistory().getGiftStatus());
		}
	}
	
	private class ONCFamilyAndNoteMealStatusComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			ONCMeal m1 = mealDB.getFamiliesCurrentMeal(o1.getFamily().getID());
			ONCMeal m2 = mealDB.getFamiliesCurrentMeal(o2.getFamily().getID());
		
			if(m1 == null && m2 == null)
				return 0;
			else if(m1 == null && m2 != null)
				return -1;
			else if(m1 != null && m2 == null)
				return 1;
			else
				return m1.getStatus().compareTo(m2.getStatus());
		}
	}
		
	private class ONCFamilyAndNoteFNComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return o1.getFamily().getFirstName().compareTo(o2.getFamily().getFirstName());
		}
	}
		
	private class ONCFamilyAndNoteLNComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)			{
			return o1.getFamily().getLastName().compareTo(o2.getFamily().getLastName());
		}
	}
	
	private class ONCFamilyAndNoteHouseNumComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			String zHN1 = o1.getFamily().getHouseNum().trim();
			String zHN2 = o2.getFamily().getHouseNum().trim();
			
			//four cases. Both numeric, one numeric, one not, both non-numeric
			//house numbers that are numeric are always ordered before house numbers
			//than contain other non-numeric characters
			if(isNumeric(zHN1) && isNumeric(zHN2))
			{
				Integer hn1 = Integer.parseInt(zHN1);
				Integer hn2 = Integer.parseInt(zHN2);
				
				return hn1.compareTo(hn2);
			}
			else if(isNumeric(zHN1))
				return -1;	
			else if(isNumeric(zHN2))
				return 1;
			else
				return zHN1.compareTo(zHN2);
		}
	}
		
	private class ONCFamilyAndNoteStreetComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			if(o1.getFamily().getStreet().equals(o2.getFamily().getStreet()))
			{
				String zHN1 = o1.getFamily().getHouseNum().trim();
				String zHN2 = o2.getFamily().getHouseNum().trim();
				
				//four cases. Both numeric, one numeric, one not, both non-numeric
				//house numbers that are numeric are always ordered before house numbers
				//than contain other non-numeric characters
				if(isNumeric(zHN1) && isNumeric(zHN2))
				{
					Integer hn1 = Integer.parseInt(zHN1);
					Integer hn2 = Integer.parseInt(zHN2);
					
					return hn1.compareTo(hn2);
				}
				else if(isNumeric(zHN1))
					return -1;	
				else if(isNumeric(zHN2))
					return 1;
				else
					return zHN1.compareTo(zHN2);
			}
			else
				return o1.getFamily().getStreet().compareTo(o2.getFamily().getStreet());
		}
	}
		
	private class ONCFamilyAndNoteZipComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return o1.getFamily().getZipCode().compareTo(o2.getFamily().getZipCode());
		}
	}
		
	private class ONCFamilyAndNoteRegionComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			Integer o1Reg = (Integer) o1.getFamily().getRegion();
			Integer o2Reg = (Integer) o2.getFamily().getRegion();
			return o1Reg.compareTo(o2Reg);
		}
	}
	
	private class ONCFamilyAndNoteSchoolComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			String o1School = regionDB.getSchoolName(o1.getFamily().getSchoolCode());
			String o2School = regionDB.getSchoolName(o2.getFamily().getSchoolCode());
			
			return o1School.compareTo(o2School);
		}
	}
		
	private class ONCFamilyAndNoteCallerComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return o1.getFamily().getChangedBy().compareTo(o2.getFamily().getChangedBy());
		}
	}
	
	private class ONCFamilyAndNoteGiftCardOnlyComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			if(!o1.getFamily().isGiftCardOnly() && o2.getFamily().isGiftCardOnly())
				return 1;
			else if(o1.getFamily().isGiftCardOnly() && !o2.getFamily().isGiftCardOnly())
				return -1;
			else
				return 0;
		}
	}
		
	private class ONCFamilyAndNoteStoplightComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			Integer o1SL = (Integer) o1.getFamily().getStoplightPos();
			Integer o2SL = (Integer) o2.getFamily().getStoplightPos();
			return o1SL.compareTo(o2SL);
		}
	}
	
	private class ONCFamilyAndNoteBikesComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			Integer nb1 = familyDB.getNumberOfBikesSelectedForFamily(o1.getFamily());
			Integer nb2 = familyDB.getNumberOfBikesSelectedForFamily(o2.getFamily());
			return nb1.compareTo(nb2);
		}
	}
	
	private class ONCFamilyAndNoteDelivererComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			return volunteerDB.getDriverLNFN(o1.getFamilyHistory().getdDelBy()).compareTo
					(volunteerDB.getDriverLNFN(o2.getFamilyHistory().getdDelBy()));
		}
	}
	
	private class ONCFamilyAndNoteNoteStatusComparator implements Comparator<ONCFamilyAndNote>
	{
		@Override
		public int compare(ONCFamilyAndNote o1, ONCFamilyAndNote o2)
		{
			if(o1.getNote() == null && o2.getNote() != null)
				return 1;
			else if(o1.getNote() == null && o2.getNote() == null)
				return 0;
			else if(o1.getNote() != null && o2.getNote() == null)
				return -1;
			else if(o1.getNote().getStatus() > o2.getNote().getStatus())
				return -1;
			else if(o1.getNote().getStatus() == o2.getNote().getStatus())
				return 0;
			else if(o1.getNote().getStatus() < o2.getNote().getStatus())
				return 1;
			else
				return 0;
		}
	}
}
