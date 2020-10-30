package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DNSCodeDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.DNS_CODE;
	private static DNSCodeDB instance = null;
	private List<DNSCode> dnsCodeList;
	
	private DNSCodeDB()
	{
		super(DB_TYPE);
		this.dnsCodeList = new ArrayList<DNSCode>();
	}
	
	public static DNSCodeDB getInstance()
	{
		if(instance == null)
			instance = new DNSCodeDB();
		
		return instance;
	}
	
	@Override
	int size() { return dnsCodeList.size(); }
	
	@Override
	ONCEntity getObjectAtIndex(int index)
	{
		return index < dnsCodeList.size() ? dnsCodeList.get(index) : null; 
	}
	
	int getIndexForObject(ONCEntity entity)
	{
		int index = 0;
		while(index < dnsCodeList.size() && dnsCodeList.get(index).getID() != entity.getID())
			index++;
		
		return index < dnsCodeList.size() ? index : -1;
	}
	
	@Override
	List<? extends ONCEntity> getList() { return dnsCodeList; }
	
	DNSCode getDNSCode(int id)
	{
		if(id < 0)
			return null;
		else
		{	
    		int index = 0;
    		while(index < dnsCodeList.size() && dnsCodeList.get(index).getID() != id)
    			index++;
    		
    		return index < dnsCodeList.size() ? dnsCodeList.get(index) : null;
		}
	}
	
	//Used to create a dnsCode internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedDNSCode = null;
					
		//send add request to server
		String response = "";
					
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_dnscode>" + gson.toJson(entity, DNSCode.class));
					
			//if the server added the DNS code,  add the new code to the data base and notify ui's
			if(response.startsWith("ADDED_DNSCODE"))		
				addedDNSCode =  processAddedDNSCode(source, response.substring(13));
		}
					
		return addedDNSCode;
	}
				
	ONCObject processAddedDNSCode(Object source, String json)
	{
		DNSCode addedDNSCode = null;
		Gson gson = new Gson();
		addedDNSCode = gson.fromJson(json, DNSCode.class);
				
		if(addedDNSCode != null)
		{	
			dnsCodeList.add(addedDNSCode);
			fireDataChanged(source, "ADDED_DNSCODE", addedDNSCode);
		}
				
			return addedDNSCode;
	}
	/***************************************************************
	 * This method is called when a DNSCode has been updated by 
	 * the user. The request updated code object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedDNSCode)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_dnscode>" + gson.toJson(updatedDNSCode, DNSCode.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated DNS Code in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_DNSCODE"))
			processUpdatedDNSCode(source, response.substring(15));
		
		return response;
	}
	
	void processUpdatedDNSCode(Object source, String json)
	{
		//Create a DNSCode object for the updated code
		Gson gson = new Gson();
		DNSCode updatedCode = gson.fromJson(json, DNSCode.class);
		
		//Find the position for the current code being updated
		int index = 0;
		while(index < dnsCodeList.size() &&  dnsCodeList.get(index).getID() != updatedCode.getID())
			index++;
		
		//Replace the current DNSCode object with the update
		if(index <  dnsCodeList.size())
		{
			dnsCodeList.set(index, updatedCode);
			fireDataChanged(source, "UPDATED_DNSCODE", updatedCode);
		}
		else
			System.out.println(String.format("DNS Code DB processUpdatedCode - DNS Code id %d not found",
					updatedCode.getID()));
	}
	
	String importDB()
	{
		String response = "NO_DNSCODES";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<DNSCode>>(){}.getType();
			
			response = serverIF.sendRequest("GET<dnscodes>");
			dnsCodeList = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_DNSCODES"))
			{
				response =  "DNSCODES_LOADED";
				fireDataChanged(this, "LOADED_DNSCODES", null);
			}
		}
		
		return response;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_DNSCODE"))
		{
			processUpdatedDNSCode(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_DNSCODE"))
		{
			processAddedDNSCode(this, ue.getJson());
		}		
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		searchAL.clear();
		
		//Determine the type of search based on characteristics of search string
		for(DNSCode code : dnsCodeList)
			if(code.getAcronym().toLowerCase().contains(data.toLowerCase()) ||
			    code.getName().toLowerCase().contains(data.toLowerCase()) ||
			     code.getDefinition().toLowerCase().contains(data.toLowerCase()))
		{
				searchAL.add(code.getID());
		}
		
		return "Text";
	}
}
