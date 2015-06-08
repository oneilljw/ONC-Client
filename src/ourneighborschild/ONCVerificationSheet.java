package ourneighborschild;

import java.util.ArrayList;

/*******************************************************************************************
 * This class implements an object that holds the necessary information to print a packaging
 * sheet for a family. A packaging sheet requires the ONC Number, gift information for 
 * each child in the family and the page number. A family with 5 or more children will require
 * two packaging sheets. The print method for Packaging Sheets will use this object to 
 * access gift information for each child as well as the ONC Number an page information. These
 * objects are added to an array list when the user elects to print packaging sheets. 
 * @author johnwoneill
 *******************************************************************************************/
public class ONCVerificationSheet
{
	private ArrayList<ONCChild> cAL;
	private String zBikes;
	private String oncNum;
	private int pageNum;
	
	ONCVerificationSheet(ArrayList<ONCChild> cal, String zbikes, String oncnum, int pn)
	{
		cAL = cal;
		zBikes = zbikes;
		oncNum = oncnum;
		pageNum = pn;
	}
	
	//getters
	ArrayList<ONCChild> getChildArrayList() { return cAL; }
	String getNumOfBikes() { return zBikes; }
	String getONCNum() { return oncNum; }
	int getpageNum() { return pageNum; }
}
