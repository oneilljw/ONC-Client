package ourneighborschild;

import java.text.SimpleDateFormat;

public class SortGiftObject extends ONCObject
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	private ONCFamily	 soFamily;
	private FamilyHistory soFamilyHistory;
	private ONCChild	 soChild;
	private ONCChildGift soChildGift;
	
	PartnerDB partnerDB;
	GiftCatalogDB cat;
	RegionDB regionDB;
	DNSCodeDB dnsCodeDB;
	
	String[] indicator = {"", "*", "#"};
	
	public SortGiftObject(int itemID, ONCFamily fam, FamilyHistory fh, ONCChild c, ONCChildGift cw) 
	{
		super(itemID);
		soFamily = fam;
		soFamilyHistory = fh;
		soChild = c;
		soChildGift = cw;
		
		partnerDB = PartnerDB.getInstance();
		cat = GiftCatalogDB.getInstance();
		regionDB = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
	}
	
	public SortGiftObject(int itemID, ONCFamily fam, ONCChild c, ONCChildGift cw) 
	{
		super(itemID);
		soFamily = fam;
		soFamilyHistory = null;
		soChild = c;
		soChildGift = cw;
		
		partnerDB = PartnerDB.getInstance();
		cat = GiftCatalogDB.getInstance();
		regionDB = RegionDB.getInstance();
		dnsCodeDB = DNSCodeDB.getInstance();
	}
	
	//getters
	ONCFamily getFamily() { return soFamily; }
	FamilyHistory getFamilyHistory() { return soFamilyHistory; }
	ONCChild getChild() { return soChild; }
	ONCChildGift getChildGift() { return soChildGift; }
	
	public String[] getExportRow()
	{
		
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
		String dateChanged = dob.format(soChildGift.getDateChanged().getTime());
		
		ONCGift wish = cat.getGiftByID(soChildGift.getCatalogGiftID());
		String wishName = wish == null ? "None" : wish.getName();
		
		ONCPartner partner = partnerDB.getPartnerByID(soChildGift.getPartnerID());
		String partnerName = partner == null ? "" : partner.getLastName();
		
		DNSCode code = soFamilyHistory.getDNSCode() > -1 ? dnsCodeDB.getDNSCode(soFamilyHistory.getDNSCode()) : null;
		
		String[] exportRow = {soFamily.getONCNum(),
								code == null ? "" : code.getAcronym(),
								regionDB.getSchoolName(soFamily.getSchoolCode()),
								soChild.getChildGender(),
								soChild.getChildAge(),
								soChild.getChildDOBString("MM-dd-yyyy"), 
								indicator[soChildGift.getIndicator()],
								wishName,
								soChildGift.getDetail(),
								soChildGift.getGiftStatus().toString(),
								partnerName,
								soChildGift.getChangedBy(),
								dateChanged};
		return exportRow;
	}
	
	public String[] getSignUpGeniusExportRow()
	{
		
		String[] row = new String[1];
		
		row[0] = String.format("ONC#: %s ** %s %s ** %s" , soFamily.getONCNum(), soChild.getChildAge(),
				 soChild.getChildGender(), soChildGift.getDetail());
		
		return row;
	}
	
	String[] getReceivingSheetRow()
	{
		GiftCatalogDB cat = GiftCatalogDB.getInstance();
		
		ONCGift wish = cat.getGiftByID(soChildGift.getCatalogGiftID());
		String wishName = wish == null ? "None" : wish.getName();
		
		String[] rsr = new String[3];
		rsr[0] = soFamily.getONCNum();
		rsr[1] = soChild.getChildAge() + " " +  soChild.getChildGender();
		if(soChildGift.getDetail().isEmpty())
			rsr[2] = wishName;
		else if(wishName.equals("-"))
			rsr[2] = soChildGift.getDetail();
		else
			rsr[2] = wishName + "- " + soChildGift.getDetail();
		return rsr;
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
		
		if(soChildGift.getDetail().isEmpty())
		{
			ONCGift gift = cat.getGiftByID(soChildGift.getCatalogGiftID());
			String giftName = gift == null ? "None" : gift.getName();
			
			line[1] = giftName + "- ";
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + soFamily.getONCNum();
			line[3] = null;
		}	
		else
		{
			ONCGift catGift = cat.getGiftByID(soChildGift.getCatalogGiftID());
			String giftName = catGift == null ? "None" : catGift.getName();
			
			String gift = giftName.equals("-") ? soChildGift.getDetail() : 
				giftName + "- " + soChildGift.getDetail();
	
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
		if(soChildGift.getChildID() < 10)
			line[4] = "00000" + Integer.toString(soChildGift.getChildID());
		else if(soChildGift.getChildID() < 100)
			line[4] = "0000" + Integer.toString(soChildGift.getChildID());
		else if(soChildGift.getChildID() < 1000)
			line[4] = "000" + Integer.toString(soChildGift.getChildID());
		else if(soChildGift.getChildID() < 10000)
			line[4] = "00" + Integer.toString(soChildGift.getChildID());
		else if(soChildGift.getChildID() < 100000)
			line[4] = "0" + Integer.toString(soChildGift.getChildID());
		else if(soChildGift.getChildID() < 1000000)
			line[4] =  Integer.toString(soChildGift.getChildID());
		else
			line[4] = Integer.toString(soChildGift.getID());
		
		line[4] = line[4] + Integer.toString(soChildGift.getGiftNumber());	
	
		return line;
	}
	
	String getGiftPlusDetail()
	{
		ONCGift catGift = cat.getGiftByID(soChildGift.getCatalogGiftID());
		String giftName = catGift == null ? "None" : catGift.getName();
		
		return giftName.equals("-") ? soChildGift.getDetail() : 
			giftName + "- " + soChildGift.getDetail();
	}
	
	//determine if two SortGiftObjects match. If the childGift cn & gn match, the childGifts match,
	//regardless of the actual content of the wish. Override the method in the base ONCObject class
	@Override
	public boolean matches(ONCObject otherObj)
	{
		if(otherObj != null && otherObj.getClass() == SortGiftObject.class)
		{
			SortGiftObject otherSO = (SortGiftObject) otherObj;
				
			return otherSO.soFamily != null && otherSO.soFamily.getID() == soFamily.getID() &&
					otherSO.soChild != null && otherSO.soChild.getID() == soChild.getID() &&
					otherSO.soChildGift != null && otherSO.soChildGift.getChildID() == soChildGift.getChildID() &&
					otherSO.soChildGift.getGiftNumber() == soChildGift.getGiftNumber();		
		}
		else
			return false;
	}
	
	String getDiagnosticString()
	{
		return String.format("soFamily id= %d, soChild id= %d,"
								+ "soChildGiftNumber= %d, "
									+ "soChildGiftStatus = %s",
									soFamily.getID(), soChild.getID(), soChildGift.getGiftNumber(),
									soChildGift.getGiftStatus().toString());
	}
}
