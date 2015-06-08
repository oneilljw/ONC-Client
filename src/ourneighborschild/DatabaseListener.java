package ourneighborschild;

import java.util.EventListener;

public interface DatabaseListener extends EventListener
{
	public void dataChanged(DatabaseEvent dbe);
}
