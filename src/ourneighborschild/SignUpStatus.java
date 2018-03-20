package ourneighborschild;

public enum SignUpStatus
{
	IMPORT("all", "Import SignUps"),
	ALL ("all", "Import All SignUps"),
	ACTIVE ("active", "Import Active SignUps"),
	EXPIRED ("expired", "Import Expired SignUps");
	
	private final String urlCommand;
	private final String title;
	
	SignUpStatus(String command, String title)
	{
		this.urlCommand = command;
		this.title = title;
	}
	
	public String urlCommand() { return urlCommand; }
	
	@Override
	public String toString() { return title; }
}
