package ourneighborschild;

import java.util.ArrayList;

public abstract class ServerListenerComponent
{
	protected ServerIF serverIF;
	
	ServerListenerComponent()
	{
		serverIF = ServerIF.getInstance();
	}
	
	//List of registered listeners for server data changed events
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
    @SuppressWarnings("unchecked")
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
    
    @SuppressWarnings("unchecked")
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
