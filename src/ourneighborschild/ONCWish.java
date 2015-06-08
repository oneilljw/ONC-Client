package ourneighborschild;

import java.io.Serializable;

public class ONCWish extends ONCObject implements Serializable
{
	/***************************************************************************************
	 *This class provides the data structure for an ONC wish that can be selected from the
	 *catalog and assigned to a child for fulfillment. A wish consists of a name, 
	 *list index and detail required fields. The list index is used to determine which lists
	 *will contain the wish. The detail required fields are used to determine what type of 
	 *additional detail is needed from the user when a wish is selected for a child. 
	 ***************************************************************************************/
	private static final long serialVersionUID = -6954846574699093148L;
	
	private String name;
	private int listindex;
	private int wishDetail1ID;
	private int wishDetail2ID;
	private int wishDetail3ID;
	private int wishDetail4ID;
	
	//Constructor used when wishes are created internal to the app
	ONCWish(int id, String name, int li)	//Created from inside the app
	{
		super(id);
		this.name = name;
		listindex = li;
		wishDetail1ID = -1;
		wishDetail2ID = -1;
		wishDetail3ID = -1;
		wishDetail4ID = -1;
	}
	
	//constructor used to make a copy of an ONCWish
	public ONCWish(ONCWish w)
	{
		super(w.getID());
		this.name = w.getName();
		listindex = w.getListindex();
		wishDetail1ID = w.getWishDetailID(0);
		wishDetail2ID = w.getWishDetailID(1);
		wishDetail3ID = w.getWishDetailID(2);
		wishDetail4ID = w.getWishDetailID(3);	
	}
	
	//Constructor used when wishes are imported from .csv file
	public ONCWish(String[] wish)	
	{
		super(Integer.parseInt(wish[0]));
		name = wish[1].isEmpty() ? "" : wish[1];
		listindex = wish[2].isEmpty() ? 7 : Integer.parseInt(wish[2]);
		wishDetail1ID = wish[3].isEmpty() ? -1 : Integer.parseInt(wish[3]);
		wishDetail2ID = wish[4].isEmpty() ? -1 : Integer.parseInt(wish[4]);
		wishDetail3ID = wish[5].isEmpty() ? -1 : Integer.parseInt(wish[5]);
		wishDetail4ID = wish[6].isEmpty() ? -1 : Integer.parseInt(wish[6]);
	}
	
	//getters
	public String getName() {return name;}
	int getListindex() {return listindex;}
//	ArrayList<WishDetail> getWishDetailAL() { return drAL; }
	int getWishDetailID(int dn)
	{
		if(dn == 0) return wishDetail1ID;
		else if(dn == 1) return wishDetail2ID;
		else if(dn == 2) return wishDetail3ID;
		else if(dn == 3) return wishDetail4ID;
		else  return -1;
	}
	int getNumberOfDetails()
	{
		int numOfDetails = 0;
		if(wishDetail1ID > -1) numOfDetails++;
		if(wishDetail2ID > -1) numOfDetails++;
		if(wishDetail3ID > -1) numOfDetails++;
		if(wishDetail4ID > -1) numOfDetails++;
		
		return numOfDetails;
	}
		
	//setters
	void setName(String n) {this.name = n;}
	void setListindex(int listindex) {this.listindex = listindex;}
	void setWishDetailID(int dn, int id)
	{
		if(dn==0) { wishDetail1ID = id; }
		else if(dn == 1) { wishDetail2ID = id; }
		else if(dn == 2) { wishDetail3ID = id; }
		else if(dn == 3) { wishDetail4ID = id; } 
	}
	
	//information
	boolean isDetailRqrd()
	{
		return wishDetail1ID > -1 || wishDetail2ID > -1 || wishDetail3ID > -1 || wishDetail4ID > -1;
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Integer.toString(id), name, Integer.toString(listindex),
				Integer.toString(wishDetail1ID), Integer.toString(wishDetail2ID),
				Integer.toString(wishDetail3ID), Integer.toString(wishDetail4ID)};
		
		return exportRow;
		
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
