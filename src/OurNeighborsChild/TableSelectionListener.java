package OurNeighborsChild;

import java.util.EventListener;

public interface TableSelectionListener extends EventListener 
{
	public void tableRowSelected(ONCTableSelectionEvent tse);
}
