package ourneighborschild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ONCPanel extends JPanel implements EntitySelector
{
	/**
	 * This class is a base class for JPanels in the ONC desktop client that must fire
	 * entity selection events. An entity selection event is fired whenever the user selects
	 * a new ONC entity in one of the UI's. ONC entities, such as families, children, partners,
	 * drivers all inherit from the ONCEntity class.
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<EntityType, ArrayList<EntitySelectionListener>> listenerMap;
    protected JFrame parentFrame;
    protected GlobalVariables gvs;
    
    //constructor used when multiple Entity Types are selected by a panel
    public ONCPanel(JFrame parentFrame)
    {
    	super();
    	this.parentFrame = parentFrame;
    	gvs = GlobalVariables.getInstance();
    	listenerMap = new HashMap<EntityType, ArrayList<EntitySelectionListener>>();
    	for(EntityType entityType : getEntityEventSelectorEntityTypes())
			listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());
    }
    
    //constructor used when only one entity type is selected by a panel
    public ONCPanel(JFrame parentFrame, EntityType entityType) 
    {
    	super();
    	this.parentFrame = parentFrame;
    	gvs = GlobalVariables.getInstance();
    	listenerMap = new HashMap<EntityType, ArrayList<EntitySelectionListener>>();
    	listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());
//    	listeners = new ArrayList<EntitySelectionListener>();
    }
    
   

	/** Register a listener for Entity Selection events 
     * @return */
    synchronized public void addEntitySelectionListener(EntityType type, EntitySelectionListener l)
    {
    	listenerMap.get(type).add(l);
//    	listeners.add(l);
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
    	ArrayList<EntitySelectionListener> listeners = listenerMap.get(entityType);
    	
    	// if we have no listeners, do nothing...
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
