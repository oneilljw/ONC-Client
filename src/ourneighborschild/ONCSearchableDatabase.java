package ourneighborschild;

import java.util.ArrayList;
import java.util.List;

public abstract class ONCSearchableDatabase extends ONCDatabase 
{
	protected EntityType dbType ;
	
	public ONCSearchableDatabase(EntityType dbType)
	{
		this.dbType = dbType;
	}
	
	abstract String searchForListItem(ArrayList<Integer> searchAL, String data);
	
	Integer getListIndexByID(List<? extends ONCEntity> list, Integer searchID)
	{
		int index = 0;
		
		while(index < list.size() && list.get(index).getID() != searchID )
			index++;
		
		return index < list.size() ? index: -1;	//was entity in the list? 
	}
	
	abstract int size();
	
	abstract List<? extends ONCEntity> getList();
	
	abstract ONCEntity getObjectAtIndex(int index);
	
	EntityType getDBType() { return dbType; }
}
