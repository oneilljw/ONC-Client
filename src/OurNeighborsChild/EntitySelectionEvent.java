package OurNeighborsChild;

import java.util.EventObject;

public class EntitySelectionEvent extends EventObject {

	/**
	 * Implements an event that is passed when an entity is selected in any of
	 * the ONC user interface components.
	 */
	private static final long serialVersionUID = 1L;
	private String eventType;
	private Object eventObject1;
	private Object eventObject2;
	private Object eventObject3;

	public EntitySelectionEvent(Object source, String eventType, Object eventObj1, Object eventObj2) 
	{
		super(source);
		this.eventType = eventType;
		this.eventObject1 = eventObj1;
		this.eventObject2 = eventObj2;
		this.eventObject3 = null;
	}
	
	public EntitySelectionEvent(Object source, String eventType, Object eventObj1,
			Object eventObj2, Object eventObj3) 
	{
		super(source);
		this.eventType = eventType;
		this.eventObject1 = eventObj1;
		this.eventObject2 = eventObj2;
		this.eventObject3 = eventObj3;
	}
		
	String getType() { return eventType; }
	Object getObject1() { return eventObject1; }
	Object getObject2() { return eventObject2; }
	Object getObject3() { return eventObject3; }
}
