package OurNeighborsChild;

/******************************************************************
 * This class implements a request object that is used when
 * obtaining either a child's wish history or a family's delivery
 * history from the ONC Server for display to the user
 * @author johnwoneill
 *
 ****************************************************************/
public class HistoryRequest
{
	private int id;
	private int num;
	
	HistoryRequest(int id, int wn)
	{
		this.id = id;
		num = wn;
	}
	
	public int getID() { return id; }
	public int getWishNumber() { return num; }
}
