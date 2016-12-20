package ourneighborschild;

import java.util.EventObject;

public class DatabaseEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String eventType;
	private Object eventObject1;
	private Object eventObject2;
	
	public DatabaseEvent(Object source, String eventType, Object eventObject) 
	{
		super(source);
		this.eventType = eventType;
		this.eventObject1 = eventObject;
		this.eventObject2 = null;
	}
	
	public DatabaseEvent(Object source, String eventType, Object eventObject1, Object eventObject2) 
	{
		super(source);
		this.eventType = eventType;
		this.eventObject1 = eventObject1;
		this.eventObject2 = eventObject2;
	}
	
	String getType() { return eventType; }
	Object getObject1() { return eventObject1; }
	Object getObject2() { return eventObject2; }
}
