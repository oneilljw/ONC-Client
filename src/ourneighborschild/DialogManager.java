package ourneighborschild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogManager 
{
	/*
	 * This class manages the all singleton dialogs in the ONC client. It implements the ability
	 * for dialogs that select ONCEntity's or listen for ONCEntity's to register for 
	 * EntitySelectionEvent notifications
	 */
	private static DialogManager instance = null;
	
	private Map<EntityType, List<EntitySelector>> selectorMap;
	private Map<EntityType, List<EntitySelectionListener>>listenerMap;

	private DialogManager()
	{
		//initialize the selector and listener maps and lists
		selectorMap = new HashMap<EntityType, List<EntitySelector>>();
		for(EntityType entityType : EntityType.values())
			selectorMap.put(entityType, new ArrayList<EntitySelector>());
		
		listenerMap = new HashMap<EntityType, List<EntitySelectionListener>>();
		for(EntityType entityType : EntityType.values())
			listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());
		
	}
	public static DialogManager getInstance()
	{
		if(instance == null)
			instance = new DialogManager();
		
		return instance;
	}
	/*        
    //test
    List<EntitySelectionListener> listenerList = new ArrayList<EntitySelectionListener>();
    listenerList.add(dsDlg);
   
    List<EntitySelector> selectorList = new ArrayList<EntitySelector>();
    selectorList.add(nav);
    selectorList.add(this);
    selectorList.add(sortWishesDlg);
    
    for(EntitySelector es: selectorList)
    	for(EntitySelectionListener listener: listenerList)
    		es.addEntitySelectionListener(listener);
*/
	
	void registerEntitySelector(EntitySelector es, List<EntityType> selTypeList)
	{
		//for each of the entity types in the list, add the selector the list for that entity type
		for(EntityType selEntityType : selTypeList)
			for(EntityType entityType : EntityType.values())
				if(selEntityType == entityType)
					selectorMap.get(EntityType.AGENT).add(es);
		
		//add all registered listeners for the entity type
		for (Map.Entry<EntityType, List<EntitySelectionListener>> entry : listenerMap.entrySet()) 
		{
			
		}
	}
	
	void registerEntitySelectionListener(EntitySelectionListener esl, List<EntityType> types)
	{
		//for each of the entity types indicated, add the listener to the list for that entity type
		
		//add the listener to all selectors for the entity type
	}
}
