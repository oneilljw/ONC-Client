package OurNeighborsChild;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ONCWishDlgSortItem
{
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	private ONCFamily	sortItemFamily;
	private int			sortItemONCID;
	private String 		sortItemoncFamNum;
	private ONCChild	sortItemChild;
	private int			sortItemchildWishNum;
	private String 		sortItemchildAge;
	private Calendar	sortItemchildDateOfBirth;
	private String 		sortItemchildGender;
	private String		sortItemchildWishInd;
	private String		sortItemchildWishBase;
	private String 		sortItemchildWishDetail;
	private String 		sortItemchildWishStatus;
	private String		sortItemchildWishAssignee;
	private String		sortItemchildWishChangedBy;
	private Calendar	sortItemchildWishDateChanged;
	
	public ONCWishDlgSortItem(ONCFamily fam, int id, String fnum, ONCChild c, int cwn, String ca, Calendar dob, String cg, 
							int cwi, String cwb, String cwd,
							String cws, String cwa, String cwcb, Calendar cdc)
	{
		sortItemFamily = fam;
		sortItemONCID = id;
		sortItemoncFamNum = fnum;
		sortItemChild = c;
		sortItemchildWishNum = cwn;
		sortItemchildAge = ca.trim();	
		sortItemchildDateOfBirth = dob;
		sortItemchildGender = cg;
		if(cwi == 2)
			sortItemchildWishInd = "#";
		else if(cwi == 1)
			sortItemchildWishInd = "*";
		else
			sortItemchildWishInd = "";
		sortItemchildWishBase = cwb;
		sortItemchildWishDetail = cwd.trim();
		sortItemchildWishStatus = cws;
		sortItemchildWishAssignee = cwa;
		sortItemchildWishChangedBy = cwcb;
		sortItemchildWishDateChanged = cdc;
	}
	
	//getters
	ONCFamily getSortItemFamily() { return sortItemFamily; }
	int getSortItemONCID() { return sortItemONCID; }
	String getSortItemONCFamilyNum()	{ return sortItemoncFamNum; }
	ONCChild getSortItemChild() { return sortItemChild; }
	Integer	getSortItemChildWishNumber() { return sortItemchildWishNum; }
	Calendar getSortItemChildDOB() { return sortItemchildDateOfBirth; }
	String getSortItemChildGender() { return sortItemchildGender; }
	String getSortItemChildWishIndicator() { return sortItemchildWishInd; }
	String getSortItemChildWishBase() { return sortItemchildWishBase; }
	String getSortItemChildWishDetail() { return sortItemchildWishDetail; }
	String getSortItemChildWishStatus() { return sortItemchildWishStatus; }
	String getSortItemChildWishAssignee() { return sortItemchildWishAssignee; }
	String getSortItemChildWishChangedBy() { return sortItemchildWishAssignee; }
	Calendar getSortItemChildWishDateChanged() { return sortItemchildWishDateChanged; }

	public String[] getSortTableRow()
	{
		String ds = new SimpleDateFormat("MM/dd H:mm").format(sortItemchildWishDateChanged.getTime());
		String[] sorttablerow = {sortItemoncFamNum,
//									Integer.toString(sortItemchildNum),
									sortItemchildAge.split("old", 2)[0].trim(), //Take the word "old" out of string
									sortItemchildGender, Integer.toString(sortItemchildWishNum+1),
									sortItemchildWishBase, sortItemchildWishDetail,
									sortItemchildWishInd, sortItemchildWishStatus, 
									sortItemchildWishAssignee, sortItemchildWishChangedBy, ds};
		return sorttablerow;
	}
	
	String[] getExportRow()
	{
		SimpleDateFormat dob = new SimpleDateFormat("MM-dd-yyyy");
		String childDoB = dob.format(sortItemchildDateOfBirth.getTime());
		
		String[] exportRow = {sortItemoncFamNum, sortItemchildGender, sortItemchildAge,
								childDoB, sortItemchildWishInd, sortItemchildWishBase, sortItemchildWishDetail};
		return exportRow;
	}
	
	String[] getReceivingSheetRow()
	{
		String[] rsr = new String[3];
		rsr[0] = sortItemoncFamNum;
		rsr[1] = sortItemchildAge + " " +  sortItemchildGender;
		if(sortItemchildWishDetail.isEmpty())
			rsr[2] = sortItemchildWishBase;
		else
			rsr[2] = sortItemchildWishBase + "- " + sortItemchildWishDetail;
		return rsr;
	}
	
	String[] getWishLabel()
	{	
		GlobalVariables gvs = GlobalVariables.getInstance();
		
		String[] line = new String[4];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
		line[0] = sortItemchildAge + " " + sortItemchildGender;
		
		//Combine the wish base and wish detail and return one or two lines depending
		//on the length of the combined string. If two lines are required, break the
		//string on a word boundary. Limit the second string to a max number of
		//characters based on MAX_LABEL_LINE_LENGTH
		StringBuilder l1 = new StringBuilder(sortItemchildWishBase);
		
		if(!sortItemchildWishDetail.isEmpty())	//Wish detail may need two lines
		{
			l1.append(" - ");
		
			String[] wishDetail = sortItemchildWishDetail.split(" ");
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
						" |  Family # " + sortItemoncFamNum;
			}
			else
			{			
				line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + sortItemoncFamNum;
				line[3] = null;
			}
		}
		else	//No wish detail
		{
			line[1] = l1.toString();
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
					" |  Family # " + sortItemoncFamNum;
			line[3] = null;
		}

		return line;
	}
	
	void setSortItemChildWishInd(String cwi) { sortItemchildWishInd = cwi; }
	void setSortItemChildWishStatus(String cws) { sortItemchildWishStatus = cws; }
	void setSortItemChildWishAssignee(String cwa) { sortItemchildWishAssignee = cwa; }
}
