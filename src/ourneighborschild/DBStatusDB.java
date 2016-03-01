package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DBStatusDB extends ONCDatabase
{
	private static DBStatusDB instance = null;
	
	private DBStatusDB()
	{
		super();
	}
	
	public static DBStatusDB getInstance()
	{
		if(instance == null)
			instance = new DBStatusDB();
		
		return instance;
	}
	
	List<DBYear> getDBStatus()
	{
		List<DBYear> dbYearList = null;
		
		//notify the server
		Gson gson = new Gson();
		Type listOfDBs = new TypeToken<ArrayList<DBYear>>(){}.getType();
		
		String response = null;
		ServerIF serverIF = ServerIF.getInstance();
		response = serverIF.sendRequest("GET<dbstatus>");
				
		//check response. If response from server indicates a successful return,
		//return the list of DB Years to the ui who requested the list.
		if(response != null && response.startsWith("DB_STATUS"))
		{	
			dbYearList = gson.fromJson(response.substring(9), listOfDBs);
			Collections.sort(dbYearList, new DBYearComparator());
		}
			
		return dbYearList;
	}
	
	String add(Object source)
	{
		String response = serverIF.sendRequest("POST<add_newseason>");
		
		if(response != null && response.startsWith("ADDED_DBYEAR"))
			processAddedDBYear(response.substring(12));
				
		return response;
	}
	
	void processAddedDBYear(String json)
	{
		//create the dbYear list object returned by the server
    	Gson gson = new Gson();
		Type listtype = new TypeToken<ArrayList<DBYear>>(){}.getType();
		ArrayList<DBYear> dbYearList =  gson.fromJson(json, listtype);
		Collections.sort(dbYearList, new DBYearComparator());
		
		//notify listeners of the modified dbYear list
		fireDataChanged(this, "ADDED_DBYEAR", dbYearList);
	}
	

	@Override
	String update(Object source, ONCObject oncObject)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		ServerIF serverIF = ServerIF.getInstance();
		response = serverIF.sendRequest("POST<update_dbyear>" + gson.toJson(oncObject, DBYear.class));
				
		//check response. If response from server indicates a successful update,
		//create and store the updated child in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response != null && response.startsWith("UPDATED_DBYEAR"))
			fireDataChanged(source, "UPDATED_DBYEAR", gson.fromJson(response.substring(14), DBYear.class));
				
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_DBYEAR"))
		{
	//		System.out.println("DBStatusDB.dataChanged: UPDATED_DBYEAR Received");
			Gson gson = new Gson();
			fireDataChanged(this, "UPDATED_DBYEAR", gson.fromJson(ue.getJson(), DBYear.class));
		}
		else if(ue.getType().equals("ADDED_DBYEAR"))
		{
			processAddedDBYear(ue.getJson());
		}
	}
	
	private class DBYearComparator implements Comparator<DBYear>
	{
		@Override
		public int compare(DBYear o1, DBYear o2)
		{
			//return the earlier year
			Integer dbYear1 = o1.getYear();
			Integer dbYear2 = o2.getYear();
			
			if(dbYear1 < dbYear2)
				return 1;
			else if(dbYear1 > dbYear2)
				return -1;
			else
				return 0;
		}
	}
}
