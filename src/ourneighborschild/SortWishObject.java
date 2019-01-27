package ourneighborschild;

import java.text.SimpleDateFormat;

public class SortWishObject extends ONCObject
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	private ONCFamily	 soFamily;
	private ONCChild	 soChild;
	private ONCChildGift soChildWish;
	
	PartnerDB partnerDB;
	GiftCatalogDB cat;
	
	String[] indicator = {"", "*", "#"};
	
	public SortWishObject(int itemID, ONCFamily fam, ONCChild c, ONCChildGift cw) 
	{
		super(itemID);
		soFamily = fam;
		soChild = c;
		soChildWish = cw;
		
		partnerDB = PartnerDB.getInstance();
		cat = GiftCatalogDB.getInstance();
	}
	
	//getters
	ONCFamily getFamily() { return soFamily; }
	ONCChild getChild() { return soChild; }
	ONCChildGift getChildWish() { return soChildWish; }
	
	public String[] getExportRow()
	{
		
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
		String dateChanged = dob.format(soChildWish.getDateChanged().getTime());
		
		ONCWish wish = cat.getWishByID(soChildWish.getGiftID());
		String wishName = wish == null ? "None" : wish.getName();
		
		ONCPartner partner = partnerDB.getPartnerByID(soChildWish.getPartnerID());
		String partnerName = partner == null ? "" : partner.getLastName();
		
		String[] exportRow = {soFamily.getONCNum(), soChild.getChildGender(),
								soChild.getChildAge(),
								soChild.getChildDOBString("MM-dd-yyyy"), 
								indicator[soChildWish.getIndicator()],
								wishName,
								soChildWish.getDetail(),
								soChildWish.getGiftStatus().toString(),
								partnerName,
								soChildWish.getChangedBy(),
								dateChanged};
		return exportRow;
	}
	
	String[] getReceivingSheetRow()
	{
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		ONCWish wish = cat.getWishByID(soChildWish.getGiftID());
		String wishName = wish == null ? "None" : wish.getName();
		
		String[] rsr = new String[3];
		rsr[0] = soFamily.getONCNum();
		rsr[1] = soChild.getChildAge() + " " +  soChild.getChildGender();
		if(soChildWish.getDetail().isEmpty())
			rsr[2] = wishName;
		else if(wishName.equals("-"))
			rsr[2] = soChildWish.getDetail();
		else
			rsr[2] = wishName + "- " + soChildWish.getDetail();
		return rsr;
	}
	
	String[] getWishLabel()
	{	
		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		String[] line = new String[5];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
//		String[] indicator = {"", "*", "#"};
		
		//Changed when adding bar code to labels			
		line[0] = soChild.getChildAge() + " " + soChild.getChildGender();
		
		if(soChildWish.getDetail().isEmpty())
		{
			ONCWish wish = cat.getWishByID(soChildWish.getGiftID());
			String wishName = wish == null ? "None" : wish.getName();
			
			line[1] = wishName + "- ";
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
			line[3] = null;
		}	
		else
		{
			ONCWish catWish = cat.getWishByID(soChildWish.getGiftID());
			String wishName = catWish == null ? "None" : catWish.getName();
			
			String wish = wishName.equals("-") ? soChildWish.getDetail() : 
				wishName + "- " + soChildWish.getDetail();
	
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
		
		//generate leading zero padded bar code string for child # & wish #
		if(soChildWish.getChildID() < 10)
			line[4] = "00000" + Integer.toString(soChildWish.getChildID());
		else if(soChildWish.getChildID() < 100)
			line[4] = "0000" + Integer.toString(soChildWish.getChildID());
		else if(soChildWish.getChildID() < 1000)
			line[4] = "000" + Integer.toString(soChildWish.getChildID());
		else if(soChildWish.getChildID() < 10000)
			line[4] = "00" + Integer.toString(soChildWish.getChildID());
		else if(soChildWish.getChildID() < 100000)
			line[4] = "0" + Integer.toString(soChildWish.getChildID());
		else if(soChildWish.getChildID() < 1000000)
			line[4] =  Integer.toString(soChildWish.getChildID());
		else
			line[4] = Integer.toString(soChildWish.getID());
		
		line[4] = line[4] + Integer.toString(soChildWish.getGiftNumber());	
	
		return line;
	}
	
	String getWishPlusDetail()
	{
		ONCWish catWish = cat.getWishByID(soChildWish.getGiftID());
		String wishName = catWish == null ? "None" : catWish.getName();
		
		return wishName.equals("-") ? soChildWish.getDetail() : 
			wishName + "- " + soChildWish.getDetail();
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
