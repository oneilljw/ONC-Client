package ourneighborschild;

import java.util.ArrayList;

import org.json.JSONObject;


/*********************************************************************************************
 * This class holds page information for printing a driving directions document page.
 * Directions for each family require a map and a table of steps. To obtain the map, 
 * the address of the family is required. If the directions contain more steps than can 
 * fit on a page, the table is continued on subsequent pages. Objects of this class are 
 * stored in an array list that is used by the print method to determine what to print on
 * each page of the document. 
 ********************************************************************************************/
public class DDPrintPage
{
	private String address;			//Address used to fetch map, if direction page 0 for family
	private JSONObject route;		//JSON that holds route from warehouse to family address
	private String[] familyInfo;	//Strings of select family data
	private ArrayList<String[]> stepsAL;	//Array of direction steps to be printed on page
	private int pagenum;			//Delivery directions for family page number 
	private int pagetotal;			//Delivery directions for family total number of pages
	
	public DDPrintPage(String addr, JSONObject rte, String[] famInfo, ArrayList<String[]> stAL, int pn, int pt)
	{
		address = addr;
		route = rte;
		familyInfo = famInfo;
		
		stepsAL = new ArrayList<String[]>();	//Each object has its own list of steps
		for(String[] s:stAL)
			stepsAL.add(s);
		
		pagenum = pn;
		pagetotal = pt;
	}
	
	ArrayList<String[]> getStepsAL() { return stepsAL; }	
	String getAddress() { return address; }
	String[] getFamilyInfo() { return familyInfo; }
	JSONObject getRoute() { return route; }
	int getPageNumber() { return pagenum; }
	int getPageTotal() { return pagetotal; }		
}
