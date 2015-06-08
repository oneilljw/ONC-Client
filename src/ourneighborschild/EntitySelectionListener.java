package ourneighborschild;

import java.util.EventListener;

public interface EntitySelectionListener extends EventListener 
{
	public void entitySelected(EntitySelectionEvent tse);
}
