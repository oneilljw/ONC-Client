package ourneighborschild;

public class AddressValidation 
{
	private int returnCode;
	private String errMssg;
		
	public AddressValidation()
	{
		this.returnCode = 0;
		this.errMssg = "";
	}
	
	public AddressValidation(int errorCode, String errMssg)
	{
		this.returnCode = errorCode;
		this.errMssg = errMssg;
	}
		
	//getters
	public int getReturnCode() { return returnCode; }
	public String getErrorMessage() { return errMssg; }
		
	//setters
	void setReturnCode(int ec) { this.returnCode = ec; }
	void setErrorMessage(String errMssg) { this.errMssg = errMssg; }
}