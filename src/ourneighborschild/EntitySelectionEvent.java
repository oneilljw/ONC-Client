package ourneighborschild;

import java.util.EventObject;

public class EntitySelectionEvent extends EventObject {

	/**
	 * Implements an event that is passed when an entity is selected in any of
	 * the ONC user interface components.
	 */
	private static final long serialVersionUID = 1L;
	private EntityType entityType;
	private Object eventObject1;
	private Object eventObject2;
	private Object eventObject3;

	public EntitySelectionEvent(Object source, EntityType entityType, Object eventObj1, Object eventObj2) 
	{
		super(source);
		this.entityType = entityType;
		this.eventObject1 = eventObj1;
		this.eventObject2 = eventObj2;
		this.eventObject3 = null;
	}
	
	public EntitySelectionEvent(Object source, EntityType entityType, Object eventObj1, Object eventObj2, Object eventObj3) 
	{
		super(source);
		this.entityType = entityType;
		this.eventObject1 = eventObj1;
		this.eventObject2 = eventObj2;
		this.eventObject3 = eventObj3;
	}
			
	EntityType getType() { return entityType; }
	Object getObject1() { return eventObject1; }
	Object getObject2() { return eventObject2; }
	Object getObject3() { return eventObject3; }
}
