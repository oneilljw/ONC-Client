package ourneighborschild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	 * 
	 * Objects that wish to globally produce or consume EntitySelection events register
	 * using this manager. Objects may still register for EntitySelections events directly with
	 * a producer if the scope is less than global.
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
	 */
	void registerEntitySelector(EntitySelector es)
	{
		//for each of the selector's EntityTypes, add the selector to the list 
		//then, add the listeners for each of the selector's EntityTypes to the selector
		Iterator<EntityType> it = es.getEntityEventSelectorEntityTypes().iterator();
        while(it.hasNext())
        {
        	EntityType selectorEntityType = it.next();
            selectorMap.get(selectorEntityType).add(es);
		
            //add all currently registered listeners for that entityType to the selector		
            for(EntitySelectionListener esl : listenerMap.get(selectorEntityType))
            	es.addEntitySelectionListener(selectorEntityType, esl);
        }
	}
	
	/***
	 * Removes an EntitySelector panel or dialog to the EntitySelector list for the provided
	 * EntityType and removes all currently registered listeners for the EntityType from the
	 * EntitySelector
	 * @param es - EntitySelector being registered
	 */
	void removeEntitySelector(EntitySelector es)
	{
		//for each of the selector's EntityTypes, remove the listeners for each of the 
		//selector's EntityTypes from the selector then remove the selector from the list 
		Iterator<EntityType> it = es.getEntityEventSelectorEntityTypes().iterator();
        while(it.hasNext())
        {
        	EntityType selectorEntityType = it.next();
        	//remove all currently registered listeners for that entityType to the selector		
            for(EntitySelectionListener esl : listenerMap.get(selectorEntityType))
            	es.removeEntitySelectionListener(selectorEntityType, esl);
        	
            selectorMap.get(selectorEntityType).remove(es);    
        }
	}
	
	/***
	 * Adds an EntitySelectionListener panel or dialog to the EntitySelectionListener list for
	 * the provided EntityType and adds the listener to the all EntitySelectors for the EnityType.
	 * @param esl - EntitySelectionLister being registered
	 */
	void registerEntitySelectionListener(EntitySelectionListener esl)
	{	
		Iterator<EntityType> it = esl.getEntityEventListenerEntityTypes().iterator();
        while(it.hasNext())
        {
        	EntityType listenerEntityType = it.next();
        	listenerMap.get(listenerEntityType).add(esl);	//add the listener to the list for that listenerEntityType
	
        	//add the listener to all selectors for the entity type
        	for(EntitySelector es: selectorMap.get(listenerEntityType))
        		es.addEntitySelectionListener(listenerEntityType, esl);
		}  
	}
	
	/***
	 * Removes an EntitySelectionListener panel or dialog from the EntitySelectionListener list for
	 * the provided EntityType and removes the listener from the all EntitySelectors for the EnityType.
	 * @param esl - EntitySelectionLister being removed
	 */
	void removeEntitySelectionListener(EntitySelectionListener esl)
	{
		Iterator<EntityType> it = esl.getEntityEventListenerEntityTypes().iterator();
        while(it.hasNext())
        {
        	EntityType listenerEntityType = it.next();
        	//remove the listener to all selectors for the entity type
        	for(EntitySelector es: selectorMap.get(listenerEntityType))
        		es.removeEntitySelectionListener(listenerEntityType, esl);
        	
        	//add the listener to the list for that listenerEntityType
        	listenerMap.get(listenerEntityType).remove(esl);	
		}
	}
}
