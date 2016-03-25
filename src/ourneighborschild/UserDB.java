package ourneighborschild;

import java.awt.Point;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.USER;
	private static UserDB instance = null;
	private List<ONCUser> uAL;
	
	private UserDB()
	{
		super();
		uAL = new ArrayList<ONCUser>();
	}

	public static UserDB getInstance()
	{
		if(instance == null)
			instance = new UserDB();
		
		return instance;
	}
	
	ONCUser getUser(int userid)
	{
		int index = 0;
		while (index < uAL.size() && uAL.get(index).getID() != userid)
			index++;
		
		if(index < uAL.size())
			return uAL.get(index);
		else
			return null;
	}
	
	ONCUser getUserFromIndex(int index)
	{
		if(index < uAL.size())
			return uAL.get(index);
		else
			return null;
	}
	
	int findModelIndexFromID(int userID)
	{
		int index = 0;
		while(index < uAL.size() && uAL.get(index).getID() != userID)
			index++;
		
		if(index < uAL.size())
			return index;
		else
			return -1;
	}
	
	int add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_user>" + 
											gson.toJson(entity, ONCServerUser.class));
		
		if(response != null && response.startsWith("ADDED_USER"))
			return processAddedObject(source, response.substring(10));
		else
			return -1;
		
	}

	/*
	void processAddedObject(Object source, String json)
	{
		//Store added ONCUser object in local data base
		Gson gson = new Gson();
		ONCUser addedObject = gson.fromJson(json, ONCUser.class);
		
		//save added user in local data base and sort by last name, first initial
		uAL.add(addedObject);
		Collections.sort(uAL, new UserLNFIComparator());
		
		//Notify local user IFs that a user was added. This will
		//be all UI's that sort on user information
		fireDataChanged(source, "ADDED_USER", addedObject);
	}
*/	
	int processAddedObject(Object source, String json)
	{
		//Store added catalog wish in local wish catalog
		Gson gson = new Gson();
		ONCUser addedUser = gson.fromJson(json, ONCUser.class);
		
		//add the wish in the proper spot alphabetically
		int index = 0;
		while(index < uAL.size() &&
				(uAL.get(index).getLastname().compareTo(addedUser.getFirstname())) < 0)
			index++;
		
		if(index < uAL.size())
			uAL.add(index, addedUser);
		else
			uAL.add(addedUser);
		
		//Notify local user IFs that a user was added. This will
		//be all UI's that sort on user information
		fireDataChanged(source, "ADDED_USER", addedUser);
		
		return index;
	}
	
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_user>" + 
											gson.toJson(entity, ONCUser.class));
		
		if(response != null && response.startsWith("UPDATED_USER") && 
				(processUpdatedObject(source, response.substring(12)) != null))
			return "UPDATED_USER";
		else
			return "UPDATE_FAILED";
	}
	
	ONCUser processUpdatedObject(Object source, String json)
	{
		//Store added catalog wish in local wish catalog
		Gson gson = new Gson();
		ONCUser updatedUser = gson.fromJson(json, ONCUser.class);
		
		//find the user
		int index = 0;
		while(index < uAL.size() && uAL.get(index).getID() != updatedUser.getID())
			index++;
		
		if(index < uAL.size())
		{
			//if found, replace the list entity and notify ui clients
			ONCUser oldUser = uAL.get(index);
			if(oldUser.getNSessions() != updatedUser.getNSessions())
			{
				//we have a user who logged in, notify this user
				ONCPopupMessage popup = new ONCPopupMessage(GlobalVariables.getONCLogo());
    			Point loc = GlobalVariables.getFrame().getLocationOnScreen();
    			popup.setLocation(loc.x+450, loc.y+70);
    			String mssg = String.format("%s %s is now online", updatedUser.getFirstname(),
    											updatedUser.getLastname());
    			popup.show("Message from ONC Server", mssg);
			}
		
			uAL.set(index, updatedUser);
			fireDataChanged(source, "UPDATED_USER", updatedUser);
			
			//notify the GlobalVariableDB of the update
			GlobalVariables gvs = GlobalVariables.getInstance();
			gvs.processUpdatedUser(updatedUser);
			
			return updatedUser;
		}
		else
			return null;
	}
	
	String importUserDatabase()
	{
		String response = "NO_USERS";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCUser>>(){}.getType();
			
			response = serverIF.sendRequest("GET<users>");
			uAL = gson.fromJson(response, listtype);				
			
			if(uAL.size() > 1)
				Collections.sort(uAL, new UserLNFIComparator());
		}
		
		if(response != null & !response.startsWith("NO_USERS"))
			fireDataChanged(this, "LOADED_USERS", null);
		
		return response;
	}
	
	List<ONCUser> getUserList() { return uAL; }
	
	int getUserID(String name)
	{
		int index = 0;
		while(index < uAL.size() && 
				!(name.contains(uAL.get(index).getFirstname()) && name.contains(uAL.get(index).getLastname())))
			index++;
		
		if(index < uAL.size())
			return uAL.get(index).getID();
		else
			return -1;
	}
	
	String getUserName(int uID)
	{
		String username = null;
		int index = 0;
		while(index < uAL.size() && uAL.get(index).getID() != uID)
			index++;
		
		if(index < uAL.size())
			username = uAL.get(index).getFirstname() + " " + uAL.get(index).getLastname();
		
		return username;
	}
	
	
	List<ONCUser> getOnlineUsers()
	{
		ArrayList<ONCUser> userList = null;
		
		if(serverIF != null && serverIF.isConnected())
		{
			String response = serverIF.sendRequest("GET<online_users>");
			
			if(response != null && response.startsWith("ONLINE_USERS"))
			{
				Gson gson = new Gson();
				Type listtype = new TypeToken<ArrayList<ONCUser>>(){}.getType();
				
				userList = gson.fromJson(response.substring(12), listtype);
			}
		}
		
		return userList;
	}
	
	String changePassword(Object source, ChangePasswordRequest cpwReq)
	{
		String response = "Server is not available, try again later";
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<change_password>" + 
												gson.toJson(cpwReq, ChangePasswordRequest.class));
			
			//need to determine if password change was required. If so, need to notify
			//listeners that the user object was updated
			
			if(response != null && response.startsWith("PASSWORD_CHANGED"))
				return response.substring(16);
			else
				return response.substring(22);	
		}
		
		return response;
	}
	
	void loginMessagePopup(ONCUser u, String status)
	{
		GlobalVariables gvs = GlobalVariables.getInstance();
		
		ONCPopupMessage popup = new ONCPopupMessage(gvs.getImageIcon(0));
		Point loc = GlobalVariables.getFrame().getLocationOnScreen();
		popup.setLocation(loc.x+450, loc.y+70);
		popup.show("ONC Elf Status Change", u.getFirstname() + " " + u.getLastname() + " is  now " + status);
	}
	
	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("ADDED_USER"))
		{
			processAddedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_USER"))
		{
			processUpdatedObject(this, ue.getJson());
		}
	}

	private class UserLNFIComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)			{
			return o1.getLNFI().compareTo(o2.getLNFI());
		}
	}

	@Override
	String searchForListItem(ArrayList<Integer> searchAL, String data)
	{
		searchAL.clear();
		String searchType = "";

    	if(!data.isEmpty())
    	{
    		searchType = "User Name";
			for(ONCUser u:uAL)
			{
				if(u.getFirstname().toLowerCase().contains(data.toLowerCase()) ||
					u.getLastname().toLowerCase().contains(data.toLowerCase()))
				{
					searchAL.add(u.getID());
				}
			}
    	}
    	
		return searchType;
	}

	@Override
	int size() { return uAL.size(); }

	@Override
	List<? extends ONCEntity> getList() { return uAL; }

	@Override
	ONCEntity getObjectAtIndex(int index) {  return uAL.get(index); }

	@Override
	EntityType getDBType() { return DB_TYPE; }
}
