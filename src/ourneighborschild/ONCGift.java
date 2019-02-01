package ourneighborschild;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ONCGift extends ONCObject implements Serializable
{
	/***************************************************************************************
	 *This class provides the data structure for an ONC gift that can be selected from the
	 *catalog and assigned to a child for fulfillment. A gift consists of a name, 
	 *list index and detail required fields. The list index is used to determine which lists
	 *will contain the gift. The detail required fields are used to determine what type of 
	 *additional detail is needed from the user when a gift is selected for a child. 
	 ***************************************************************************************/
	private static final long serialVersionUID = -6954846574699093148L;
	
	private String name;
	private int listindex;
//	private int[] giftDetailID;
	private List<Integer> giftDetailIDList;
	
	//Constructor used when gifts are created internal to the app
	ONCGift(int id, String name, int li, List<Integer> giftDetailIDList2)	//Created from inside the app
	{
		super(id);
		this.name = name;
		listindex = li;
		giftDetailIDList = giftDetailIDList2;
	}
	
	//Constructor used when gifts are created internal to the app and don't require detail. An
	//example would be ONCGifts in lists for "All" or "None"
	ONCGift(int id, String name, int li)	
	{
		super(id);
		this.name = name;
		listindex = li;
		giftDetailIDList = null;
	}
	
	//constructor used to make a copy of an ONCGift
	public ONCGift(ONCGift w)
	{
		super(w.getID());
		this.name = w.name;
		listindex = w.listindex;
		giftDetailIDList = new LinkedList<Integer>();
		for(Integer id : w.giftDetailIDList)
			giftDetailIDList.add(id);
	}
	
	//Constructor used when gifts are imported from .csv file
	public ONCGift(String[] line)	
	{
		super(Integer.parseInt(line[0]));
		name = line[1].isEmpty() ? "" : line[1];
		listindex = line[2].isEmpty() ? 7 : Integer.parseInt(line[2]);
		giftDetailIDList = new LinkedList<Integer>();
		for(int col=3; col < line.length; col++)
			giftDetailIDList.add(line[col].isEmpty() ? -1 : Integer.parseInt(line[col]));
	}
	
	//getters
	public String getName() {return name;}
	public int getListindex() {return listindex;}
	int getGiftDetailID(int dn){ return dn < giftDetailIDList.size() ? giftDetailIDList.get(dn) : -1; }
	int getNumberOfDetails()
	{
		int numOfDetails = 0;
		for(Integer dn : giftDetailIDList)
			if(dn > -1)
				numOfDetails++;

		return numOfDetails;
	}
		
	//setters
	void setName(String n) {this.name = n;}
	void setListindex(int listindex) {this.listindex = listindex;}
	void setGiftDetailID(int dn, int id) 
	{
		if(dn < giftDetailIDList.size())
			giftDetailIDList.set(dn, id);
	}
	
	//information
	boolean isDetailRqrd() { return getNumberOfDetails() > 0; }
	
	public boolean canBeGift(int wn)
	{
		int mask = 1 << wn;
		return (listindex & mask) > 0;
	}
	
	@Override
	public String[] getExportRow()
	{
		List<String> columns = new ArrayList<String>();
		columns.add(Integer.toString(id));
		columns.add(name);
		columns.add(Integer.toString(listindex));
		for(Integer dn : giftDetailIDList)
			columns.add(Integer.toString(dn));
		
		return  columns.toArray(new String[0]);
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
