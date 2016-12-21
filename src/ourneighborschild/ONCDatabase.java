package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public abstract class ONCDatabase implements ServerListener
{
	protected static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	protected ServerIF serverIF;
	
	public ONCDatabase()
	{
		serverIF = ServerIF.getInstance();
		serverIF.addServerListener(this);
	}
	
	ONCObject find(List<? extends ONCObject> list, int id)
	{
		int index = 0;
		while(index < list.size() && id != list.get(index).getID())
			index++;
		
		if(index == list.size())
			return null;
		else
			return list.get(index);		
	}
	
	//All databases must implement an update class for notifications of changes
	abstract String update(Object source, ONCObject entity);
	
	protected static boolean isNumeric(String str)
	{
		if(str == null || str.isEmpty())
			return false;
		else
			return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	 //List of registered listeners for Sever data changed events
    private ArrayList<DatabaseListener> listeners;
    
    /** Register a listener for database DataChange events */
    synchronized public void addDatabaseListener(DatabaseListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<DatabaseListener>();
    	listeners.add(l);
    }  

    /** Remove a listener for server DataChange */
    synchronized public void removeDatabaseListener(DatabaseListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<DatabaseListener>();
    	listeners.remove(l);
    }
    
    /** Fire a Data ChangedEvent to all registered listeners */
    protected void fireDataChanged(Object source, String eventType, Object eventObject)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		DatabaseEvent event = new DatabaseEvent(source, eventType, eventObject);

    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<DatabaseListener> targets;
    		synchronized (this) { targets = (ArrayList<DatabaseListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(DatabaseListener l:targets)
    			l.dataChanged(event);
    	}
    }
    
    protected void fireDataChanged(Object source, String eventType, Object eventObject1, Object eventObject2)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		DatabaseEvent event = new DatabaseEvent(source, eventType, eventObject1, eventObject2);

    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<DatabaseListener> targets;
    		synchronized (this) { targets = (ArrayList<DatabaseListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(DatabaseListener l:targets)
    			l.dataChanged(event);
    	}
    }
    
    ArrayList<DatabaseListener> getListenerList() { return listeners; }
}
