package OurNeighborsChild;

import java.text.SimpleDateFormat;

public class ONCSortObject extends ONCObject
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	private ONCFamily	 soFamily;
	private ONCChild	 soChild;
	private ONCChildWish soChildWish;
	
	public ONCSortObject(int itemID, ONCFamily fam, ONCChild c, ONCChildWish cw)
	{
		super(itemID);
		soFamily = fam;
		soChild = c;
		soChildWish = cw;
	}
	
	//getters
	ONCFamily getSortObjectFamily() { return soFamily; }
	ONCChild getSortObjectChild() { return soChild; }
	ONCChildWish getSortObjectChildWish() { return soChildWish; }

	public String[] getSortTableRow()
	{
		ONCOrgs partnerDB = ONCOrgs.getInstance();
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		String[] indicator = {"", "*", "#"};
		String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
							"Distributed", "Verified"};
		
		String ds = new SimpleDateFormat("MM/dd H:mm").format(soChildWish.getChildWishDateChanged().getTime());
		String[] sorttablerow = {soFamily.getONCNum(),
									soChild.getChildAge().split("old", 2)[0].trim(), //Take the word "old" out of string
									soChild.getChildGender(),
									Integer.toString(soChildWish.getWishNumber()+1),
									cat.getWishByID(soChildWish.getWishID()).getName(),
									soChildWish.getChildWishDetail(),
									indicator[soChildWish.getChildWishIndicator()],
									status[soChildWish.getChildWishStatus()], 
									partnerDB.getOrganizationByID(soChildWish.getChildWishAssigneeID()).getName(), 
									soChildWish.getChildWishChangedBy(), ds};
		return sorttablerow;
	}
	
	
	String[] getReceivingSheetRow()
	{
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		String[] rsr = new String[3];
		rsr[0] = soFamily.getONCNum();
		rsr[1] = soChild.getChildAge() + " " +  soChild.getChildGender();
		if(soChildWish.getChildWishDetail().isEmpty())
			rsr[2] = cat.getWishName(soChildWish.getWishID());
		else
			rsr[2] = cat.getWishName(soChildWish.getWishID()) + "- " + soChildWish.getChildWishDetail();
		return rsr;
	}
	
	String[] getWishLabel()
	{	
		GlobalVariables gvs = GlobalVariables.getInstance();
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		
		String[] line = new String[4];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
		line[0] = soChild.getChildAge() + " " + soChild.getChildGender();
		
		//Combine the wish base and wish detail and return one or two lines depending
		//on the length of the combined string. If two lines are required, break the
		//string on a word boundary. Limit the second string to a max number of
		//characters based on MAX_LABEL_LINE_LENGTH
		StringBuilder l1 = new StringBuilder(cat.getWishName(soChildWish.getWishID()));
		
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

		return line;
	}
	
	@Override
	public String[] getExportRow()
	{
		ONCOrgs partnerDB = ONCOrgs.getInstance();
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		String[] indicator = {"", "*", "#"};
		String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
							"Distributed", "Verified"};
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
		String dateChanged = dob.format(soChildWish.getChildWishDateChanged().getTime());
		
		String[] exportRow = {soFamily.getONCNum(), soChild.getChildGender(),
								soChild.getChildAge(),
								soChild.getChildDOBString("MM-dd-yyyy"), 
								indicator[soChildWish.getChildWishIndicator()],
								cat.getWishByID(soChildWish.getWishID()).getName(),
								soChildWish.getChildWishDetail(),
								status[soChildWish.getChildWishStatus()],
								partnerDB.getOrganizationByID(soChildWish.getChildWishAssigneeID()).getName(),
								soChildWish.getChildWishChangedBy(),
								dateChanged};
		return exportRow;
	}
}
