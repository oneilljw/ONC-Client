package ourneighborschild;

public class AddressValidation 
{
	private boolean  bAddressIsSchool;
	private int errorCode;
	private String errMssg;
		
	public AddressValidation(boolean bAddressIsSchool, int errorCode)
	{
		this.bAddressIsSchool = bAddressIsSchool;
		this.errorCode = errorCode;
		this.errMssg = "";
	}
	
	public AddressValidation(boolean bAddressIsSchool, int errorCode, String errMssg)
	{
		this.bAddressIsSchool = bAddressIsSchool;
		this.errorCode = errorCode;
		this.errMssg = errMssg;
	}
		
	//getters
	boolean isAddressSchool() { return bAddressIsSchool; }
	int getErrorCode() { return errorCode; }
	String getErrorMessage() { return errMssg; }
		
	//setters
	void setAddressIsSchool(boolean tf) { this.bAddressIsSchool = tf; }
	void setErrorCode(int ec) { this.errorCode = ec; }
	void setErrorMessage(String errMssg) { this.errMssg = errMssg; }
}