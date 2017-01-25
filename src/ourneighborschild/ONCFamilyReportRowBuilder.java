package ourneighborschild;

import java.util.ArrayList;

public class ONCFamilyReportRowBuilder 
{	
	/**************************
	 * This class exports family related data from the ONC Client to a .csv file. The class provides two
	 * export formats. The first format, the family report is an export of family, child and child wish
	 * data in a format readable by Excel that maintains consistency with the spreadsheet used by ONC
	 * prior to the adoption of this application. 
	 * 
	 * The second format is the export of all data stored in an ONC Family object. It is used to export
	 * ONC family objects to and from data base structures.
	 * 
	 * Two methods provide external access to the two report formats. Two header methods and two body
	 * methods are available externally that create the respective headers and bodies.A series of
	 * internal methods are used to provide the reports, maximizing the commonality between the two reports. 
	 ********************/
	
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private UserDB userDB;
	private ONCWishCatalog cat;
	
	ONCFamilyReportRowBuilder()
	{
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		userDB = UserDB.getInstance();
		cat = ONCWishCatalog.getInstance();
	}
	
	String[] getFamilyReportCSVRowData(ONCFamily f)	//Used when writing the database to a .csv file
	{
		String[] row = new String[getCommonFamilyHeader().length + getFamilyReportChildColumns().length +
		                              getFamilyReportAgentColumns().length];
		 
		 int headerindex = 0;
		 
		 for(String s:getFamilyCommonCSVRowData(f))
			 row[headerindex++] = s;
		 
		 for(String s:getFamilyChildReportCSVRowData(f))
			 row[headerindex++] = s;
		 
		 for(String s:getFamilyAgentReportCSVRowData(f))
			 row[headerindex++] = s;
		 
		 return row;
	}
	
	String[] getFamilyExportOjectCSVRowData(ONCFamily f)	//Used when writing the database to a .csv file
	{
		String[] row = new String[getFamilyObjectRowLength()];
		 
		 int headerindex = 0;
		 
		 for(String s:getFamilyCommonCSVRowData(f))
			 row[headerindex++] = s;
		 
		 for(String s:getFamilyObjectEndingCSVRowData(f))
			 row[headerindex++] = s;
		 
		 return row;
	}
	
	private String[] getFamilyCommonCSVRowData(ONCFamily f)	//Used when writing the database to a .csv file
	{
//		String[] famstatus = {"Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
//		String[] delstatus = {"Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
		
		String[] row = new String[getCommonFamilyHeader().length];
		int index = 0;
		
		row[index++] = 	Integer.toString(f.getID());
		row[index++] =	f.getONCNum();		
		row[index++] = 	Integer.toString(f.getRegion());
		row[index++] = 	f.getReferenceNum();
		row[index++] = 	f.getBatchNum();	
		row[index++] =  f.getDNSCode();
		row[index++] = 	Integer.toString(f.getFamilyStatus().statusIndex());
		row[index++] = 	Integer.toString(f.getGiftStatus().statusIndex());
		row[index++] = 	f.getSpeakEnglish();
		row[index++] = 	f.getLanguage();			
		row[index++] = 	f.getChangedBy();
		row[index++] = 	f.getNotes();
		row[index++] = 	f.getDeliveryInstructions();
		row[index++] =  f.getClientFamily();
		row[index++] = 	f.getHOHFirstName();
		row[index++] = 	f.getHOHLastName();
		row[index++] = 	f.getHouseNum();
		row[index++] = 	f.getStreet();
		row[index++] = 	f.getUnitNum();
		row[index++] = 	f.getCity();
		row[index++] = 	f.getZipCode();
		row[index++] = 	f.getSubstituteDeliveryAddress();
		row[index++] = 	f.getAllPhoneNumbers();			
		row[index++] = 	f.getHomePhone();
		row[index++] = 	f.getOtherPhon();
		row[index++] = 	f.getEmail();
		row[index++] = 	f.getDetails();
		row[index++] = 	f.getNamesOfChildren();
		row[index++] = 	f.getSchools();
		row[index] = 	f.getWishList();

		return row;		
	}
	
	private String getFullWish(ONCChild c, int wn)
	{
		ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
		if(c.getChildWishID(wn) == -1 || cw == null)
			return "";
		else
		{
			ONCWish wish = cat.getWishByID(cw.getWishID());
		
			if(wish == null)
				return "";
			else
			{
				String[] restrictions = {" ", "*", "#"};
				String restriction = restrictions[cw.getChildWishIndicator()];
				String wishbase = wish.getName();
				String detail = cw.getChildWishDetail();
		
				return restriction + wishbase + "- " +  detail;
			}
		}
	}

	private String[] getFamilyChildReportCSVRowData(ONCFamily f)
	{
		String[] row = new String[getFamilyReportChildColumns().length];
		 
		int cn = 0;		//Child number index to iterate thru the nine potential children in each family
		ArrayList<ONCChild> fChildren = cDB.getChildren(f.getID());
			
		for(int i=0; i < 81; i+=9)	//Legacy ONC Excel format: 9 children in each row, 9 columns per child
		{
			if(cn < fChildren.size())
			{
				ONCChild c = fChildren.get(cn);
				row[i] = c.getChildFirstName();
				row[i+1] = c.getChildLastName();
				row[i+2] = c.getChildSchool();
				row[i+3] = c.getChildGender();
				row[i+4] = c.getChildDOBString("MM/dd/yy");
				row[i+5] = c.getChildAge();
//				row[i+6] = c.getChildWishID(0) == -1 ? "" : cwDB.getWish(c.getChildWishID(0)).getChildWishAll();
//				row[i+7] = c.getChildWishID(1) == -1 ? "" : cwDB.getWish(c.getChildWishID(1)).getChildWishAll();
//				row[i+7] = c.getChildWishID(2) == -1 ? "" : cwDB.getWish(c.getChildWishID(2)).getChildWishAll();
				row[i+6] = getFullWish(c, 0);
				row[i+7] = getFullWish(c, 1);
				row[i+8] = getFullWish(c, 2);
			}
			else	//If there are less than 9 children, the remaining columns are left empty
			{
				row[i] = "";	
				row[i+1] = "";
				row[i+2] = "";
				row[i+3] = "";
				row[i+4] = "";
				row[i+5] = "";
				row[i+6] = "";
				row[i+7] = "";
				row[i+8] =	"";
			}
				
			cn++;
		}		
				
		return row;		
	}
	 
	private String[] getFamilyAgentReportCSVRowData(ONCFamily f)
	{
		String[] row = new String[getFamilyReportAgentColumns().length];
		int index = 0;
		 
		ONCUser  famAgent = (ONCUser) userDB.getUser(f.getAgentID());
		row[index++] = 	f.getAdoptedFor();
		row[index++] = 	famAgent.getLastname();
		row[index++] = 	famAgent.getOrg();
		row[index++] = 	famAgent.getTitle();
		row[index++] = 	famAgent.getEmail();
		row[index] = 	famAgent.getPhone();
				
		return row;		
	}
	
	private String[] getFamilyObjectEndingCSVRowData(ONCFamily f)	//Used when writing the database to a .csv file
	{
		String[] row = new String[getFamilyObjectEndingHeader().length];
		int index = 0;
		 
		row[index++] = 	f.getAdoptedFor();
		row[index++] = 	Integer.toString(f.getAgentID());
		row[index++] = 	Long.toString(f.getDeliveryID());
		row[index++] = 	Integer.toString(f.getNumOfBags());
		row[index++] = 	Integer.toString(f.getNumOfLargeItems());
		row[index++] = 	Integer.toString(f.getStoplightPos());
		row[index++] = 	f.getStoplightMssg();
		row[index] = 	f.getStoplightChangedBy();
				
		return row;		
	}
	 
	String[] getFamilyReportHeader()
	{
		String[] header = new String[getCommonFamilyHeader().length + getFamilyReportChildColumns().length +
		                              getFamilyReportAgentColumns().length];
		 
		int headerindex = 0;
		 
		for(String s:getCommonFamilyHeader())
			header[headerindex++] = s;
		 
		for(String s:getFamilyReportChildColumns())
			header[headerindex++] = s;
		 
		for(String s:getFamilyReportAgentColumns())
			header[headerindex++] = s;
		 
		return header;
	}
	 
	String[] getFamilyObjectExportHeader()
	{
		String[] header = new String[getFamilyObjectRowLength()];
		 
		int headerindex = 0;
		 
		for(String s:getCommonFamilyHeader())
			header[headerindex++] = s;
		 
		for(String s:getFamilyObjectEndingHeader())
			header[headerindex++] = s;
		 
		return header;
	}
	 
	private String[] getCommonFamilyHeader()
	{
		String[] header = {"ONC ID", "ONCNum", "Region", "ODB Family #", "Batch #", "DNS Code", "Family Status", "Delivery Status",
					"Speak English?","Language if No", "Caller", "Notes", "Delivery Instructions",
					"Client Family", "First Name", "Last Name", "House #", "Street", "Unit #", "City", "Zip Code",
					"Substitute Delivery Address", "All Phone #'s", "Home Phone", "Other Phone", "Family Email", 
					"ODB Details", "Children Names", "Schools", "ODB WishList"}; 
		return header;
	}
	 
	private String[] getFamilyReportChildColumns()
	{
		String[] header = {
				"Child 1 FN", "Child 1 LN", "Child 1 School", "Child 1 Gender", "Child 1 DOB", "Child 1 Age",
				"Child 1 Wish 1", "Child 1 Wish 2", "Child 1 Wish 3",
				"Child 2 FN", "Child 2 LN", "Child 2 School", "Child 2 Gender", "Child 2 DOB", "Child 2 Age",
				"Child 2 Wish 1", "Child 2 Wish 2", "Child 2 Wish 3",
				"Child 3 FN", "Child 3 LN", "Child 3 School", "Child 3 Gender", "Child 3 DOB", "Child 3 Age",
				"Child 3 Wish 3", "Child 3 Wish 2", "Child 3 Wish 3",
				"Child 4 FN", "Child 4 LN", "Child 4 School", "Child 4 Gender", "Child 4 DOB", "Child 4 Age",
				"Child 4 Wish 1", "Child 4 Wish 2", "Child 4 Wish 3",
				"Child 5 FN", "Child 5 LN", "Child 5 School", "Child 5 Gender", "Child 5 DOB", "Child 5 Age",
				"Child 5 Wish 1", "Child 5 Wish 2", "Child 5 Wish 3",
				"Child 6 FN", "Child 6 LN", "Child 6 School", "Child 6 Gender", "Child 6 DOB", "Child 6 Age",
				"Child 6 Wish 1", "Child 6 Wish 2", "Child 6 Wish 3",
				"Child 7 FN", "Child 7 LN", "Child 7 School", "Child 7 Gender", "Child 7 DOB", "Child 7 Age",
				"Child 7 Wish 3", "Child 7 Wish 2", "Child 7 Wish 3",
				"Child 8 FN", "Child 8 LN", "Child 8 School", "Child 8 Gender", "Child 8 DOB", "Child 8 Age",
				"Child 8 Wish 1", "Child 8 Wish 2", "Child 8 Wish 3",
				"Child 9 FN", "Child 9 LN", "Child 9 School", "Child 9 Gender", "Child 9 DOB", "Child 9 Age",
				"Child 9 Wish 1", "Child 9 Wish 2", "Child 9 Wish 3"};
		 return header;
	}
	 
	private String[] getFamilyReportAgentColumns()
	{
		String[] header = {"Adopted For", "Referring Agent Name", "Referring Agent Organizatoin",
							"Referring Agent Title", "Referring Agent Email", "Referring Agent Phone"}; 
		return header;
	}
	
	private String[] getFamilyObjectEndingHeader()
	{
		String[] header = {"Adopted For", "Agent ID", "Delivery ID", "# of Bags", "# of Large Items", 
							"Stoplight Pos", "Stoplight Mssg", "Stopligh C/B"};	 
		return header;
	}
	
	int getFamilyObjectRowLength() { return getCommonFamilyHeader().length + getFamilyObjectEndingHeader().length; }
}
