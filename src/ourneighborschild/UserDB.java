package ourneighborschild;

import java.awt.Point;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserDB extends ONCSearchableDatabase
{
	private static final EntityType DB_TYPE = EntityType.USER;
	private static UserDB instance = null;
	private List<ONCUser> uAL;
	
	private ONCUser loggedInUser;
	
	private UserDB()
	{
		super(DB_TYPE);
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
	
	ONCUser setLoggedInUser(ONCUser u) 
	{
		if(loggedInUser == null || loggedInUser.getID() != u.getID())
		{
			loggedInUser = u;
			this.fireDataChanged(this, "CHANGED_USER", loggedInUser);
		}
		
		return loggedInUser; 
	}
	
	ONCUser getLoggedInUser() { return loggedInUser; }
	
	String getUserFNLI()
	{
		if(loggedInUser.getFirstName().isEmpty())
			return loggedInUser.getLastName();
		else
			return loggedInUser.getFirstName() + " " + loggedInUser.getLastName().charAt(0);
	}
	
	String getUserLNFI()
	{
		if(loggedInUser.getFirstName().isEmpty())
			return loggedInUser.getLastName();
		else
			return loggedInUser.getLastName() + ", " + loggedInUser.getFirstName().charAt(0);
	}
	
	UserPreferences getUserPreferences()
	{
		if(loggedInUser != null)
			return loggedInUser.getPreferences();
		else
			return new UserPreferences();
	}
	
//	void setUserPermission(UserPermission p) { user_permission = p; }
	
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
	
	ONCUser add(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<add_user>" + 
											gson.toJson(entity, ONCServerUser.class));
		
		if(response != null && response.startsWith("ADDED_USER"))
			return processAddedObject(source, response.substring(10));
		else
			return null;
	}

	ONCUser processAddedObject(Object source, String json)
	{
		Gson gson = new Gson();
		ONCUser addedUser = gson.fromJson(json, ONCUser.class);
		
		//add the user in the proper spot alphabetically
		int index = 0;
		while(index < uAL.size() &&
				(uAL.get(index).getLastName().compareTo(addedUser.getFirstName())) < 0)
			index++;
		
		if(index < uAL.size())
			uAL.add(index, addedUser);
		else
			uAL.add(addedUser);
		
		//Notify local user IFs that a user was added. This will be all UI's that sort on user information
		fireDataChanged(source, "ADDED_USER", addedUser);
		
		return addedUser;
	}
	
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_user>" + 
											gson.toJson(entity, ONCUser.class));
		
		if(response != null && response.startsWith("UPDATED_USER") && 
				(processUpdatedObject(source, response.substring(12)) != null))
			return response;
		else
			return "UPDATE_FAILED";
	}
	
	ONCUser processUpdatedObject(Object source, String json)
	{
		//store updated User object in local data base
		//extract the object from the json
		Gson gson = new Gson();
		ONCUser updatedUser = gson.fromJson(json, ONCUser.class);
		
		//find the user in the local data base
		int index = 0;
		while(index < uAL.size() && uAL.get(index).getID() != updatedUser.getID())
			index++;
		
		if(index < uAL.size())
		{
			//if found, replace the user and notify ui clients. Also,
			//check to see if it's a new login. If so, broadcast a GUI message
			ONCUser oldUser = uAL.get(index);
			if(oldUser.getNSessions() != updatedUser.getNSessions())
			{
				//we have a user who logged in, notify this user
				ONCPopupMessage popup = new ONCPopupMessage(GlobalVariablesDB.getONCLogo());
    			Point loc = GlobalVariablesDB.getFrame().getLocationOnScreen();
    			popup.setLocation(loc.x+450, loc.y+70);
    			String mssg = String.format("%s %s is now online", updatedUser.getFirstName(),
    											updatedUser.getLastName());
    			popup.show("Message from ONC Server", mssg);
			}
		
			uAL.set(index, updatedUser);
			
			//update the current user if they were the update. Do not update the client id, as the
			//a user may be logged in more than once
			if(updatedUser.getID() == loggedInUser.getID())
			{
				loggedInUser.setFirstName(updatedUser.getFirstName());
				loggedInUser.setLastName(updatedUser.getLastName());
				loggedInUser.setStatus(updatedUser.getStatus());
				loggedInUser.setAccess(updatedUser.getAccess());
				loggedInUser.setPermission(updatedUser.getPermission());
				loggedInUser.setNSessions(updatedUser.getNSessions());
				loggedInUser.setLastLogin(updatedUser.getLastLogin());
				loggedInUser.setEmail(updatedUser.getEmail());
				loggedInUser.setTitle(updatedUser.getTitle());
				loggedInUser.setOrganization(updatedUser.getOrganization());
				loggedInUser.setCellPhone(updatedUser.getCellPhone());
				loggedInUser.setGroupList(updatedUser.getGroupList());
				loggedInUser.setPreferences(updatedUser.getPreferences());
			}
			
//			System.out.println(String.format("UserDB.processUpdatedObject: font: %d, wishIndex: %d, dnsIndex: %d",
//					updatedUser.getPreferences().getFontSize(),
//					updatedUser.getPreferences().getWishAssigneeFilter(),
//					updatedUser.getPreferences().getFamilyDNSFilter()));
			
//			//notify the GlobalVariableDB of the update since it holds the logged in user
//			GlobalVariables gvs = GlobalVariables.getInstance();
//			gvs.processUpdatedUser(updatedUser);
			
			//notify all the ui's
			fireDataChanged(source, "UPDATED_USER", updatedUser);
			
			return updatedUser;
		}
		else
			return null;
	}
	
	@Override
	boolean importDB()
	{
		boolean bImportComplete = false;
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCUser>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<users>");
			
			if(response != null)
			{
				uAL = gson.fromJson(response, listtype);
				
				if(uAL.size() > 1)
					Collections.sort(uAL, new UserLNFIComparator());
				bImportComplete = true;
			}
		}
		
		return bImportComplete;
	}
	
	String sendSeasonWelcomeEmail(List<ONCUser> emailList)
	{
		//convert user email list to json for array of user ID's
		EmailUserIDs emailUserIDs = new EmailUserIDs(emailList);
		Gson gson = new Gson();
		String response = serverIF.sendRequest("POST<send_user_email>" + gson.toJson(emailUserIDs, EmailUserIDs.class));
		
		return response;
	}
	
	private class EmailUserIDs
	{
		int[] emailUserIDs;
		
		EmailUserIDs(List<ONCUser> emailList)
		{
			emailUserIDs = new int[emailList.size()];
			for(int i=0; i<emailList.size(); i++)
				emailUserIDs[i] = emailList.get(i).getID();
		}
	}

//	List<ONCUser> getList() { return uAL; }
	
	int getUserID(String name)
	{
		int index = 0;
		while(index < uAL.size() && 
				!(name.contains(uAL.get(index).getFirstName()) && name.contains(uAL.get(index).getLastName())))
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
			username = uAL.get(index).getFirstName() + " " + uAL.get(index).getLastName();
		
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
	
	int getActiveUserCount()
	{
		int count = 0;
		for(ONCUser u : uAL)
			if(u.getStatus() != UserStatus.Inactive)
				count++;
		
		return count;
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
	
	List<ONCUser> getGroupMembers(int groupID, EnumSet<UserStatus> us )
	{
		List<ONCUser> memberList = new ArrayList<ONCUser>();
		
		EnumSet<UserStatus> activeUserSet = EnumSet.of(UserStatus.Active, UserStatus.Change_PW, 
													UserStatus.Update_Profile);
		for(ONCUser u : uAL)
			for(Integer gID : u.getGroupList())
			{
				if(gID == groupID && us.equals(EnumSet.allOf(UserStatus.class)))
					memberList.add(u);
				else if(gID == groupID && activeUserSet.contains(u.getStatus()))
					memberList.add(u);
		}
				
		return memberList;
	}
	
	List<ONCUser> getCandidateGroupMembers()
	{
		List<ONCUser> memberList = new ArrayList<ONCUser>();
		
		for(ONCUser u : uAL)
			if(u.getPermission().compareTo(UserPermission.Agent) >= 0)
				memberList.add(u);
				
		return memberList;
	}
	
	void loginMessagePopup(ONCUser u, String status)
	{
		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		
		ONCPopupMessage popup = new ONCPopupMessage(gvs.getImageIcon(0));
		Point loc = GlobalVariablesDB.getFrame().getLocationOnScreen();
		popup.setLocation(loc.x+450, loc.y+70);
		popup.show("ONC Elf Status Change", u.getFirstName() + " " + u.getLastName() + " is  now " + status);
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

    		if(!data.isEmpty() && data.contains("@") && data.contains("."))
    		{
    			searchType = "Email Address";
    			for(ONCUser u:uAL)
    				if(u.getEmail().toLowerCase().equals(data.toLowerCase()))
    					searchAL.add(u.getID());
    		}
    		else if(!data.isEmpty() && isNumeric(data) && data.length() == 10)
    		{
    			//treat as phone number. eliminate the dashes
    			searchType = "Phone #";
    			for(ONCUser u:uAL)
    			{	
    				String up = u.getHomePhone().replaceAll("-", "");
    				if(isNumeric(up) && up.equals(data))
    					searchAL.add(u.getID());
    			}
    		}
    		else if(!data.isEmpty() && data.contains("-") && data.length() == 12)
    		{
    			//treat as phone number. eliminate the dashes
    			String target = data.replaceAll("-", "");
    			searchType = "Phone #";
    			for(ONCUser u:uAL)
    			{	
    				String up = u.getHomePhone().replaceAll("-", "");
    				if(isNumeric(up) && up.equals(target))
    					searchAL.add(u.getID());
    			}
    		}
    		else	 if(!data.isEmpty())
    		{
    			searchType = "User Name";
    			for(ONCUser u:uAL)
    				if(u.getFirstName().toLowerCase().contains(data.toLowerCase()) ||
    						u.getLastName().toLowerCase().contains(data.toLowerCase()))
				searchAL.add(u.getID());
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
	String[] getExportHeader()
	{
		return new String[] {"ID", "First Name", "Last Name", "Status", "Access", "Permission",
				"Date Changed", "Changed By", "SL Position", "SL Message", 
				"SL Changed By", "Sessions", "Last Login", "Orginization", "Title",
				"Email", "Phone", "Agent ID", "Font Size", "Wish Assignee Filter",
				"Family DNS Filter"};
	}	
}
