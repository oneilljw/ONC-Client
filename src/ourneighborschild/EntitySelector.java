package ourneighborschild;

public interface EntitySelector 
{
	/** Register a listener for Entity Selection events */
    void addEntitySelectionListener(EntitySelectionListener l);
    
    /** Remove a listener for Entity Selection events */
    void removeEntitySelectionListener(EntitySelectionListener l);
 
    /** Fire an Entity Selection event to all registered listeners */
    void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2);
    
    void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2, Object obj3);
}
