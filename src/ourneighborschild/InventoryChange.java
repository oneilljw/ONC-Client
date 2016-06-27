package ourneighborschild;

/******************************************************************
 * This class implements a request object that is used when
 * the sever processes an inventory request from a client
 * @author johnwoneill
 *
 ****************************************************************/
public class InventoryChange
{
	private int id;
	private int count;
	private int commits;
	
	public InventoryChange(int id, int count, int commits)
	{
		this.id = id;
		this.count = count;
		this.commits = commits;
	}
	
	public int getID() { return id; }
	public int getCount() { return count; }
	public int getCommits() { return commits; }
}
