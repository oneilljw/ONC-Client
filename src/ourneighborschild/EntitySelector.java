package ourneighborschild;

import java.util.EnumSet;

public interface EntitySelector 
{
	/** Returns an EnumSet of the EntityTypes selected by the EntitySelector **/
	EnumSet<EntityType> getEntityEventSelectorEntityTypes();
	
	/** Register a listener for Entity Selection events */
    void addEntitySelectionListener(EntityType type, EntitySelectionListener l);
    
    /** Remove a listener for Entity Selection events */
    void removeEntitySelectionListener(EntityType type, EntitySelectionListener l);
 
    /** Fire an Entity Selection event to all registered listeners */
    void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2);
    
    void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2, Object obj3);
}
