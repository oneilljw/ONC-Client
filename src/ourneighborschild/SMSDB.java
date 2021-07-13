package ourneighborschild;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class SMSDB extends ONCDatabase
{
	private static SMSDB instance = null;
	private List<ONCSMS> smsList;
	
	GlobalVariablesDB gvDB = GlobalVariablesDB.getInstance();
	
	private SMSDB()
	{
		super();
		smsList = new ArrayList<ONCSMS>();
	}
	
	public static SMSDB getInstance()
	{
		if(instance == null)
			instance = new SMSDB();
		
		return instance;
	}
	
	@Override
	List<ONCSMS> getList() { return smsList; }

	ONCSMS getSMS(int id)
	{
		int index = 0;
		while(index < smsList.size() && smsList.get(index).getID() != id)
			index++;
		
		if(index == smsList.size())
			return null;		//SMS wasn't found
		else
			return smsList.get(index);
	}
	
	//Used to create an SMS internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedSMS = null;
				
		//send add SMS request to server
		String response = "";
				
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_sms>" + gson.toJson(entity, ONCSMS.class));
				
			//if the server added the SMS,  add the new SMS to the data base and notify ui's
			if(response.startsWith("ADDED_SMS"))		
				addedSMS =  processAddedSMS(source, response.substring(11));
		}
				
		return addedSMS;
	}
	
	String sendSingleSMSRequest(String message, int phone, EntityType entType, int targetID)
	{
		List<Integer> targetIDList = new ArrayList<Integer>();
		targetIDList.add(targetID);
		
		SMSRequest request = new SMSRequest(gvDB.getCurrentSeason(), message, true, phone, entType, targetIDList);
		
		Gson gson = new Gson();
		String response = serverIF.sendRequest("POST<sms_request>" + gson.toJson(request, SMSRequest.class));
		
		return response;
	}
	
	String sendMultipleSMSRequest(Object source, int messageID, int phoneNum, List<Integer> famIDList)
	{
		//form a SMS Request
		SMSRequest request = new SMSRequest(gvDB.getCurrentSeason(), messageID, phoneNum, 
											EntityType.FAMILY, famIDList);
		
		Gson gson = new Gson();
		String response = serverIF.sendRequest("POST<sms_request>" + gson.toJson(request, SMSRequest.class));
		
		return response;
	}
			
	ONCObject processAddedSMS(Object source, String json)
	{
		ONCSMS addedSMS = null;
		Gson gson = new Gson();
		addedSMS = gson.fromJson(json, ONCSMS.class);
			
		if(addedSMS != null)
		{
			smsList.add(addedSMS);
			fireDataChanged(source, "ADDED_SMS", addedSMS);
		}
			
		return addedSMS;
	}
	
	/***************************************************************
	 * This method is called when an SMS has been updated by 
	 * the user. The request updated SMS object is passed. 
	 *************************************************************/
	@Override
	String update(Object source, ONCObject updatedSMS)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		response = serverIF.sendRequest("POST<update_sms>" + 
												gson.toJson(updatedSMS, ONCSMS.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated sms in the local data base and notify local
		//ui listeners of a change.
		if(response.startsWith("UPDATED_SMS"))
			processUpdatedSMS(source, response.substring(11));
		
		return response;
	}
	
	void processUpdatedSMS(Object source, String json)
	{
		//Create a TwilioSMSReceive object for the updated SMS
		Gson gson = new Gson();
		ONCSMS updatedSMS = gson.fromJson(json, ONCSMS.class);
		
		//Find the position for the current SMS being updated
		int index = 0;
		while(index < smsList.size() && smsList.get(index).getID() != updatedSMS.getID())
			index++;
		
		//Replace the current SMS object with the update
		if(index < smsList.size())
		{
			smsList.set(index, updatedSMS);
			fireDataChanged(source, "UPDATED_SMS", updatedSMS);
		}
		else
			System.out.println(String.format("SMS DB processUpdatedSMS - SMS id %d not found",
					updatedSMS.getID()));
	}
	
	/*******************************************************************************************
	 * This method is called when the user requests to delete a SMS
	 **********************************************************************************************************/
	ONCSMS delete(Object source, ONCSMS reqDelSMS)
	{
		String response = "";
		ONCSMS deletedSMS = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_SMS>" + gson.toJson(reqDelSMS, ONCSMS.class));		
			
			//if the server deleted the sms, notify ui's
			if(response.startsWith("DELETED_SMS"))		
				deletedSMS = processDeletedSMS(source, response.substring(11));
		}
		
		return deletedSMS;
	}
	
	ONCSMS processDeletedSMS(Object source, String json)
	{
		//remove the SMS from this local data base
		Gson gson = new Gson();
		ONCSMS deletedSMS = removeSMS(source, gson.fromJson(json, ONCSMS.class).getID());
		
		if(deletedSMS != null)
			fireDataChanged(source, "DELETED_SMS", deletedSMS);
		
		return deletedSMS;
	}
	
	ONCSMS removeSMS(Object source, int smsID)
	{
		//remove the sms from this data base
		ONCSMS deletedSMS = null;
		
		int index = 0;
		while(index < smsList.size() && smsList.get(index).getID() != smsID)
				index++;
				
		if(index < smsList.size())
		{
			deletedSMS = smsList.get(index);
			smsList.remove(index);	
		}
		
		return deletedSMS;
	}
	
/*	
	List<TwilioSMSReceive> getSMSForFamily(int famID)
	{
		List<ONCAdult> famAdultList = new ArrayList<ONCAdult>();
		
		for(ONCAdult adult:smsList)
			if(adult.getFamID() == famID)
				famAdultList.add(adult);
		
		return famAdultList;
	}
*/	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCSMS>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<sms_messages>");
			
			
			if(response != null)
			{
				smsList = gson.fromJson(response, listtype);
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_SMS"))
		{
			processUpdatedSMS(this, ue.getJson());
		}
		else if(ue.getType().equals("ADDED_SMS"))
		{
			processAddedSMS(this, ue.getJson());
		}
		else if(ue.getType().equals("DELETED_SMS"))
		{
			processDeletedSMS(this, ue.getJson());
		}			
	}

	@Override
	String[] getExportHeader()
	{
		return new String[] {"ID", "Message SID", "Type", "Entity ID", "Phone Num", "Direction", "Body",
				"Status", "Timestamp"};
	}
}
