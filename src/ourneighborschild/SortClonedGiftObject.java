package ourneighborschild;

import java.text.SimpleDateFormat;

public class SortClonedGiftObject extends ONCObject
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	private ONCFamily	 soFamily;
	private ONCChild	 soChild;
	private ClonedGift soClonedGift;
	
	PartnerDB partnerDB;
	GiftCatalogDB cat;
	RegionDB regionDB;
	DNSCodeDB dnsCodeDB;
	
	String[] indicator = {"", "*", "#"};
	
	public SortClonedGiftObject(int itemID, ONCFamily fam, ONCChild c, ClonedGift cg) 
	{
		super(itemID);
		this.soFamily = fam;
		this.soChild = c;
		this.soClonedGift = cg;
		
		partnerDB = PartnerDB.getInstance();
		cat = GiftCatalogDB.getInstance();
		regionDB = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
	}
	
	//getters
	ONCFamily getFamily() { return soFamily; }
	ONCChild getChild() { return soChild; }
	ClonedGift getClonedGift() { return soClonedGift; }
	
	public String[] getExportRow()
	{
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
//		String dateChanged = dob.format(soClonedGift.getDateChanged().getTime());
		String dateChanged = "FIX DATE CHANGE";
		
		ONCGift wish = cat.getGiftByID(soClonedGift.getGiftID());
		String wishName = wish == null ? "None" : wish.getName();
		
		ONCPartner partner = partnerDB.getPartnerByID(soClonedGift.getPartnerID());
		String partnerName = partner == null ? "" : partner.getLastName();
		
		DNSCode code = soFamily.getDNSCode() > -1 ? dnsCodeDB.getDNSCode(soFamily.getDNSCode()) : null;
		
		String[] exportRow = {soFamily.getONCNum(),
								code == null ? "" : code.getAcronym(),
								regionDB.getSchoolName(soFamily.getSchoolCode()),
								soChild.getChildGender(),
								soChild.getChildAge(),
								soChild.getChildDOBString("MM-dd-yyyy"), 
								indicator[soClonedGift.getIndicator()],
								wishName,
								soClonedGift.getDetail(),
								soClonedGift.getGiftStatus().toString(),
								partnerName,
								soClonedGift.getChangedBy(),
								dateChanged};
		return exportRow;
	}
	
	String[] getGiftLabel()
	{	
		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		String[] line = new String[5];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
//		String[] indicator = {"", "*", "#"};
		
		//Changed when adding bar code to labels			
		line[0] = soChild.getChildAge() + " " + soChild.getChildGender();
		
		if(soClonedGift.getDetail().isEmpty())
		{
			ONCGift gift = cat.getGiftByID(soClonedGift.getGiftID());
			String giftName = gift == null ? "None" : gift.getName();
			
			line[1] = giftName + "- ";
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
			line[3] = null;
		}	
		else
		{
			ONCGift catGift = cat.getGiftByID(soClonedGift.getGiftID());
			String giftName = catGift == null ? "None" : catGift.getName();
			
			String gift = giftName.equals("-") ? soClonedGift.getDetail() : 
				giftName + "- " + soClonedGift.getDetail();
	
			//does it fit on one line?
			if(gift.length() <= MAX_LABEL_LINE_LENGTH)
			{
				line[1] = gift.trim();
			}
			else	//split into two lines
			{
				int index = MAX_LABEL_LINE_LENGTH;
				while(index > 0 && gift.charAt(index) != ' ')	//find the line break
					index--;
	
				line[1] = gift.substring(0, index);
				line[2] = gift.substring(index);
				if(line[2].length() > MAX_LABEL_LINE_LENGTH)
					line[2] = gift.substring(index, index + MAX_LABEL_LINE_LENGTH);
			}

			//If the gift required two lines make the ONC Year line 4
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
		
		//generate leading zero padded bar code string for child # & gift #
		if(soClonedGift.getChildID() < 10)
			line[4] = "00000" + Integer.toString(soClonedGift.getChildID());
		else if(soClonedGift.getChildID() < 100)
			line[4] = "0000" + Integer.toString(soClonedGift.getChildID());
		else if(soClonedGift.getChildID() < 1000)
			line[4] = "000" + Integer.toString(soClonedGift.getChildID());
		else if(soClonedGift.getChildID() < 10000)
			line[4] = "00" + Integer.toString(soClonedGift.getChildID());
		else if(soClonedGift.getChildID() < 100000)
			line[4] = "0" + Integer.toString(soClonedGift.getChildID());
		else if(soClonedGift.getChildID() < 1000000)
			line[4] =  Integer.toString(soClonedGift.getChildID());
		else
			line[4] = Integer.toString(soClonedGift.getID());
		
		line[4] = line[4] + Integer.toString(soClonedGift.getGiftNumber());	
	
		return line;
	}
	
	String getGiftPlusDetail()
	{
		ONCGift catGift = cat.getGiftByID(soClonedGift.getGiftID());
		String giftName = catGift == null ? "None" : catGift.getName();
		
		return giftName.equals("-") ? soClonedGift.getDetail() : 
			giftName + "- " + soClonedGift.getDetail();
	}
	
	//determine if two SortClonedGiftObjects match. If the clonedGift cn & gn match, the childGifts match,
	//regardless of the actual content of the wish. Override the method in the base ONCObject class
	@Override
	public boolean matches(ONCObject otherObj)
	{
		if(otherObj != null && otherObj.getClass() == SortClonedGiftObject.class)
		{
			SortClonedGiftObject otherSO = (SortClonedGiftObject) otherObj;
				
			return otherSO.soFamily != null && otherSO.soFamily.getID() == soFamily.getID() &&
					otherSO.soChild != null && otherSO.soChild.getID() == soChild.getID() &&
					otherSO.soClonedGift != null && otherSO.soClonedGift.getChildID() == soClonedGift.getChildID() &&
					otherSO.soClonedGift.getGiftNumber() == soClonedGift.getGiftNumber();		
		}
		else
			return false;
	}
}
