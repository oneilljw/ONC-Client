package ourneighborschild;

public class UPCFailure 
{
	private String valid;
	private String reason;
	
	public UPCFailure(String valid, String reason)
	{
		this.valid = valid;
		this.reason = reason;
	}
	
	//getters
	public String getValid() { return valid; }
	public String getReason() { return reason; }
}
