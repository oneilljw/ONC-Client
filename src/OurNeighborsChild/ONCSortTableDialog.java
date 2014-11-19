package OurNeighborsChild;

import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFrame;

public abstract class ONCSortTableDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JFrame parentFrame;
	protected GlobalVariables gvs;
	
	//List of registered listeners for table selection events
    private ArrayList<TableSelectionListener> listeners;
    
    
    public ONCSortTableDialog(JFrame parentFrame)
    {
    	super(parentFrame);
    	this.parentFrame = parentFrame;
    	gvs = GlobalVariables.getInstance();
    }
    
    /** Register a listener for database DataChange events */
    synchronized public void addTableSelectionListener(TableSelectionListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<TableSelectionListener>();
    	listeners.add(l);
    }  

    /** Remove a listener for server DataChange */
    synchronized public void removeTableSelectionListener(TableSelectionListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<TableSelectionListener>();
    	listeners.remove(l);
    }
    
    /** Fire a Data ChangedEvent to all registered listeners */
    protected void fireDataChanged(Object source, String eventType, Object obj1, Object obj2)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		ONCTableSelectionEvent event = new ONCTableSelectionEvent(source, eventType, obj1, obj2);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<TableSelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<TableSelectionListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(TableSelectionListener l:targets)
    			l.tableRowSelected(event);
    	}
    }
}
