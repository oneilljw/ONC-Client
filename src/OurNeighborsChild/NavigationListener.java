package OurNeighborsChild;

import java.util.EventListener;

public interface NavigationListener extends EventListener
{
	public void indexChanged(NavigationEvent ne);
}
