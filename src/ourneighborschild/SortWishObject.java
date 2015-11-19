package ourneighborschild;

import java.text.SimpleDateFormat;

public class SortWishObject extends ONCObject
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	private ONCFamily	 soFamily;
	private ONCChild	 soChild;
	private ONCChildWish soChildWish;
	
	ONCOrgs partnerDB;
	ONCWishCatalog cat;
	
	String[] indicator = {"", "*", "#"};
	
	public SortWishObject(int itemID, ONCFamily fam, ONCChild c, ONCChildWish cw) 
	{
		super(itemID);
		soFamily = fam;
		soChild = c;
		soChildWish = cw;
		
		partnerDB = ONCOrgs.getInstance();
		cat = ONCWishCatalog.getInstance();
	}
	
	//getters
	ONCFamily getFamily() { return soFamily; }
	ONCChild getChild() { return soChild; }
	ONCChildWish getChildWish() { return soChildWish; }
	
	public String[] getExportRow()
	{
		
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
		String dateChanged = dob.format(soChildWish.getChildWishDateChanged().getTime());
		
		ONCWish wish = cat.getWishByID(soChildWish.getWishID());
		String wishName = wish == null ? "None" : wish.getName();
		
		Organization partner = partnerDB.getOrganizationByID(soChildWish.getChildWishAssigneeID());
		String partnerName = partner == null ? "" : partner.getName();
		
		String[] exportRow = {soFamily.getONCNum(), soChild.getChildGender(),
								soChild.getChildAge(),
								soChild.getChildDOBString("MM-dd-yyyy"), 
								indicator[soChildWish.getChildWishIndicator()],
								wishName,
								soChildWish.getChildWishDetail(),
								soChildWish.getChildWishStatus().toString(),
								partnerName,
								soChildWish.getChildWishChangedBy(),
								dateChanged};
		return exportRow;
	}
	
	String[] getReceivingSheetRow()
	{
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		ONCWish wish = cat.getWishByID(soChildWish.getWishID());
		String wishName = wish == null ? "None" : wish.getName();
		
		String[] rsr = new String[3];
		rsr[0] = soFamily.getONCNum();
		rsr[1] = soChild.getChildAge() + " " +  soChild.getChildGender();
		if(soChildWish.getChildWishDetail().isEmpty())
			rsr[2] = wishName;
		else
			rsr[2] = wishName + "- " + soChildWish.getChildWishDetail();
		return rsr;
	}
	
	String[] getWishLabel()
	{	
		GlobalVariables gvs = GlobalVariables.getInstance();
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		String[] line = new String[4];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
		String[] indicator = {"", "*", "#"};
		
		line[0] = soChild.getChildAge() + " " + soChild.getChildGender();
		
		//Combine the wish base and wish detail and return one or two lines depending
		//on the length of the combined string. If two lines are required, break the
		//string on a word boundary. Limit the second string to a max number of
		//characters based on MAX_LABEL_LINE_LENGTH
/*		
		StringBuilder l1 = new StringBuilder(cat.getWishByID(soChildWish.getWishID()).getName());
		
		if(!soChildWish.getChildWishDetail().isEmpty())	//Wish detail may need two lines
		{
			l1.append(" - ");
		
			String[] wishDetail = soChildWish.getChildWishDetail().split(" ");
			int index = 0;
		
			//Build 2nd line. Limit it to MAX_LABEL_LINE_LENGTH on a word boundary
			while(index < wishDetail.length &&
					l1.length() + wishDetail[index].length() + 1 < MAX_LABEL_LINE_LENGTH)
			{
				l1.append(wishDetail[index++] + " ");
			}
			line[1] = l1.toString();
		
			//If wish is too long to fit on one line, break it into a 2nd line
			StringBuilder l2 = new StringBuilder("");
			while(index < wishDetail.length &&
				l2.length() + wishDetail[index].length() + 1 < MAX_LABEL_LINE_LENGTH)
			{
				l2.append(wishDetail[index++] + " ");
			}
		
			//If the wish required two lines make the 3rd line the ONC Year line 4
			//else make the ONC Year line 3
			if(l2.length() > 0)
			{
				line[2] = l2.toString();
				line[3] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
			}
			else
			{			
				line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
				line[3] = null;
			}
		}
		else	//No wish detail
		{
			line[1] = l1.toString();
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
					" |  Family # " + soFamily.getONCNum();
			line[3] = null;
		}
*/		
		if(soChildWish.getChildWishDetail().isEmpty())
		{
			ONCWish wish = cat.getWishByID(soChildWish.getWishID());
			String wishName = wish == null ? "None" : wish.getName();
			
			line[1] = indicator[soChildWish.getChildWishIndicator()] + wishName + " - ";
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
			line[3] = null;
		}	
		else
		{
			ONCWish catWish = cat.getWishByID(soChildWish.getWishID());
			String wishName = catWish == null ? "None" : catWish.getName();
			
			String wish = indicator[soChildWish.getChildWishIndicator()] + wishName +
					" - " + soChildWish.getChildWishDetail();
	
			//does it fit on one line?
			if(wish.length() <= MAX_LABEL_LINE_LENGTH)
			{
				line[1] = wish.trim();
			}
			else	//split into two lines
			{
				int index = MAX_LABEL_LINE_LENGTH;
				while(index > 0 && wish.charAt(index) != ' ')	//find the line break
					index--;
	
				line[1] = wish.substring(0, index);
				line[2] = wish.substring(index);
				if(line[2].length() > MAX_LABEL_LINE_LENGTH)
				{
//					System.out.println(String.format("SortWishObject.getWishLabel: line[2].length = %d",
//							line[2].length()));
					line[2] = wish.substring(index, index + MAX_LABEL_LINE_LENGTH);
				}
			}

			//If the wish required two lines make the ONC Year line 4
			//else make the ONC Year line 3
			if(line[2] != null)
			{
				line[3] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
							" |  Family # " + soFamily.getONCNum();
			}
			else
			{			
				line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
							" |  Family # " + soFamily.getONCNum();
				line[3] = null;
			}
		}
		
		return line;
	}
	
	//determine if two SortWishObjects match
	@Override
	public boolean matches(ONCObject otherObj)
	{
		if(otherObj != null && otherObj.getClass() == SortWishObject.class)
		{
			SortWishObject otherSO = (SortWishObject) otherObj;
			
//			System.out.println(String.format("ONCSortObject.matches: SO.cwID = %d, otherSO.cwID = %d",
//					soChildWish.getID(), otherSO.soChildWish.getID()));
//				
			return otherSO.soFamily != null && otherSO.soFamily.getID() == soFamily.getID() &&
					otherSO.soChild != null && otherSO.soChild.getID() == soChild.getID() &&
					otherSO.soChildWish != null && otherSO.soChildWish.getID() == soChildWish.getID();			
		}
		else
			return false;
	}
}
