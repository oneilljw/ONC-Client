package OurNeighborsChild;

import java.util.ArrayList;
import java.util.List;

public abstract class ONCSearchableDatabase extends ONCDatabase 
{
	abstract String searchForListItem(ArrayList<Integer> searchAL, String data);
	
	Integer getListIndexByID(List<? extends ONCEntity> list, Integer searchID)
	{
		int index = 0;
		
		while(index < list.size() && list.get(index).getID() != searchID )
			index++;
		
		if(index < list.size())	//The org was found 
			return index;
		else
			return -1;	
	}
	
	abstract int size();
	
	abstract List<? extends ONCEntity> getList();
	
	abstract ONCEntity getObjectAtIndex(int index);
	
	abstract String getDBType();
}
