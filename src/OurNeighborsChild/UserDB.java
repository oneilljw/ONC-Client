package OurNeighborsChild;

import java.awt.Point;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserDB extends ONCDatabase
{
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
	
	String add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_user>" + 
											gson.toJson(entity, ONCServerUser.class));
		
		if(response.startsWith("ADDED_USER"))
			processAddedObject(source, response.substring(10));
		
		return response;	
	}
	
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
	}

	@Override
	String update(Object source, ONCObject entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class UserLNFIComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)			{
			return o1.getLNFI().compareTo(o2.getLNFI());
		}
	}
}
