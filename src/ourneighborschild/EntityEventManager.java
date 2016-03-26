package ourneighborschild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityEventManager 
{
	/***
	 * This singleton class manages the connections between dialogs or panels that fire EntitySelection
	 * events and the dialogs or panels that listen for EntitySelection events. The class
	 * maintains lists of EntitySelectors and EntitySelectionListenrs for each EntitySelection
	 * event type. Event types are maintained in the EventType enumeration; EventType.FAMILY is an 
	 * example. The selector and listener lists are stored in maps using EventType as keys.
	 */
	private static EntityEventManager eventManagerInstance = null;	//singleton instance
	
	private Map<EntityType, List<EntitySelector>> selectorMap;	//map of EntitySelectors by EventType
	private Map<EntityType, List<EntitySelectionListener>>listenerMap;	//map of EntitySelectionListeners

	private EntityEventManager()	//private constructor
	{
		//initialize the selector and listener maps and lists
		selectorMap = new HashMap<EntityType, List<EntitySelector>>();
		for(EntityType entityType : EntityType.values())
			selectorMap.put(entityType, new ArrayList<EntitySelector>());
		
		listenerMap = new HashMap<EntityType, List<EntitySelectionListener>>();
		for(EntityType entityType : EntityType.values())
			listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());
		
	}
	
	public static EntityEventManager getInstance()	
	{
		if(eventManagerInstance == null)
			eventManagerInstance = new EntityEventManager();
		
		return eventManagerInstance;
	}
	
	/***
	 * Adds an EntitySelector panel or dialog to the EntitySelector list for the provided
	 * EntityType and adds all currently registered listeners for the EntityType to the
	 * EntitySelector
	 * @param es - EntitySelector being registered
	 * @param selectorEntityType - EntityType that the EntitySelector selects
	 */
	void registerEntitySelector(EntitySelector es, EntityType selectorEntityType)
	{
		//add the selector to the list for the selectorEntityType
		selectorMap.get(selectorEntityType).add(es);
		
		//add all currently registered listeners for that entityType to the selector		
		for(EntitySelectionListener esl : listenerMap.get(selectorEntityType))
			es.addEntitySelectionListener(esl);
	}
	
	/***
	 * Adds an EntitySelectionListener panel or dialog to the EntitySelectionListener list for
	 * the provided EntityType and adds the listener to the all EntitySelectors for the EnityType.
	 * Note that some panels and dialogs will call this method more than once, since they listener
	 * for more than one EntityType.
	 * @param esl - EntitySelectionLister being registered
	 * @param selectorEntityType - EntityType that the EntitySelectionListener listens for
	 */
	void registerEntitySelectionListener(EntitySelectionListener esl, EntityType listenerEntityType)
	{
		//add the listener to the list for that listenerEntityType
		listenerMap.get(listenerEntityType).add(esl);
		
		//add the listener to all selectors for the entity type
		for(EntitySelector es: selectorMap.get(listenerEntityType))
			es.addEntitySelectionListener(esl);
	}
}
