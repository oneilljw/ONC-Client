package OurNeighborsChild;

import java.util.EventObject;

public class NavigationEvent extends EventObject 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String eventType;
	private int eventIndex;

	public NavigationEvent(Object source, String eventType, int eventIndex) 
	{
		super(source);
		this.eventType = eventType;
		this.eventIndex = eventIndex;
	}
	
	String getType() { return eventType; }
	int getIndex() { return eventIndex; }

}
