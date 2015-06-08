package ourneighborschild;

import java.util.EventObject;

public class DatabaseEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String eventType;
	private Object eventObject;
	
	public DatabaseEvent(Object source, String eventType, Object eventObject) 
	{
		super(source);
		this.eventType = eventType;
		this.eventObject = eventObject;
	}
	
	String getType() { return eventType; }
	Object getObject() { return eventObject; }
}
