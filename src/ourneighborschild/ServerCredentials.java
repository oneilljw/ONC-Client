package ourneighborschild;

public class ServerCredentials 
{
	private String serverName;
	private String userID;
	private String password;
	
	public ServerCredentials(String serverName, String userID, String password)
	{
		this.serverName = serverName;
		this.userID = userID;
		this.password = password;	
	}

	public String getServerName() {
		return serverName;
	}

	public String getUserID() {
		return userID;
	}

	public String getPassword() {
		return password;
	}

	void setServerName(String serverName) {
		this.serverName = serverName;
	}

	void setUserID(String userID) {
		this.userID = userID;
	}

	void setPassword(String password) {
		this.password = password;
	}
}
