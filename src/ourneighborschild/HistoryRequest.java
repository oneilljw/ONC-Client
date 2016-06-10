package ourneighborschild;

/******************************************************************
 * This class implements a request object that is used when
 * obtaining either a child's wish history, a family's delivery
 * history or an inventory item update from the ONC Server for 
 * display to the user
 * @author johnwoneill
 *
 ****************************************************************/
public class HistoryRequest
{
	private int id;
	private int num;
	
	public HistoryRequest(int id, int wn)
	{
		this.id = id;
		num = wn;
	}
	
	public int getID() { return id; }
	public int getNumber() { return num; }
}
