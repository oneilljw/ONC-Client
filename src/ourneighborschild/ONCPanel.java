package ourneighborschild;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ONCPanel extends JPanel
{
	/**
	 * This class is a base class for JPanels in the ONC desktop client that must fire
	 * entity selection events. An entity selection event is fired whenever the user selects
	 * a new ONC entity in one of the UI's. ONC entities, such as families, children, partners,
	 * drivers all inherit from the ONCEntity class.
	 */
	private static final long serialVersionUID = 1L;
	
    private ArrayList<EntitySelectionListener> listeners;	//List of listeners for entity selection events
    protected JFrame parentFrame;
    protected GlobalVariables gvs;
    
    public ONCPanel(JFrame parentFrame)
    {
    	super();
    	this.parentFrame = parentFrame;
    	gvs = GlobalVariables.getInstance();
    }
    
    /** Register a listener for Entity Selection events */
    synchronized public void addEntitySelectionListener(EntitySelectionListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<EntitySelectionListener>();
    	
    	listeners.add(l);
    }  

    /** Remove a listener for Entity Selection events */
    synchronized public void removeEntitySelectionListener(EntitySelectionListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<EntitySelectionListener>();
    	listeners.remove(l);
    }
    
    /** Fire an Entity Selection event to all registered listeners */
    protected void fireEntitySelected(Object source, String eventType, Object obj1, Object obj2)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		EntitySelectionEvent event = new EntitySelectionEvent(source, eventType, obj1, obj2);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<EntitySelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(EntitySelectionListener l:targets)
    			l.entitySelected(event); 
    	}
    }
    
    protected void fireEntitySelected(Object source, String eventType, Object obj1, Object obj2, Object obj3)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		EntitySelectionEvent event = new EntitySelectionEvent(source, eventType, obj1, obj2, obj3);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<EntitySelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(EntitySelectionListener l:targets)
    			l.entitySelected(event);
    	}
    }
}
