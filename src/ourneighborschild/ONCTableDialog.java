package ourneighborschild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;

public abstract class ONCTableDialog extends JDialog implements EntitySelector
{
	/**
	 * Implements an abstract class for all dialogs that contain tables that display ONC Entities. This
	 * class provides a blueprint for the inheriting class proving the ability to register entity 
	 * selection listeners and fire entity selection events
	 */
	private static final long serialVersionUID = 1L;
	protected JFrame parentFrame;
	protected GlobalVariables gvs;
	
	//Map of registered listeners for table selection events
	private Map<EntityType, ArrayList<EntitySelectionListener>> listenerMap;
    
    public ONCTableDialog(JFrame parentFrame)
    {
    	super(parentFrame);
    	this.parentFrame = parentFrame;
    	gvs = GlobalVariables.getInstance();
    	
    	listenerMap = new HashMap<EntityType, ArrayList<EntitySelectionListener>>();
    	for(EntityType entityType : getEntityEventSelectorEntityTypes())
			listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());	
    }
    
    /** Register a listener for Entity Selection events */
    synchronized public void addEntitySelectionListener(EntityType type, EntitySelectionListener l)
    {
    	listenerMap.get(type).add(l);
    }  

    /** Remove a listener for Entity Selection events */
    synchronized public void removeEntitySelectionListener(EntityType type, EntitySelectionListener l)
    {
    	
    	listenerMap.get(type).remove(l);
    }
    
    /** Fire an Entity Selection event to all registered listeners */
    public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2)
    {
    	// if we have no listeners, do nothing...
    	ArrayList<EntitySelectionListener> listeners = listenerMap.get(entityType);
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		EntitySelectionEvent event = new EntitySelectionEvent(source, entityType, obj1, obj2);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<EntitySelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(EntitySelectionListener l:targets)
    			l.entitySelected(event);
    	}
    }
    
    public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2, Object obj3)
    {
    	// if we have no listeners, do nothing...
    	ArrayList<EntitySelectionListener> listeners = listenerMap.get(entityType);
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		EntitySelectionEvent event = new EntitySelectionEvent(source, entityType, obj1, obj2, obj3);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<EntitySelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(EntitySelectionListener l:targets)
    			l.entitySelected(event);
    	}
    }
}
