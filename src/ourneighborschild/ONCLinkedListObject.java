package ourneighborschild;

public abstract class ONCLinkedListObject extends ONCObject
{
	protected int priorID;
	protected int nextID;
	
	public ONCLinkedListObject(int id,  int priorID, int nextID)
	{
		super(id);
		this.priorID = priorID;
		this.nextID = nextID;
	}
	
	//getters
	public int getPriorID() { return priorID; }
	public int getNextID() { return nextID; }
	
	//setters
	public void setPriorID(int priorID) { this.priorID = priorID; }
	public void setNextID(int nextID) { this.nextID = nextID; }
}
