package ourneighborschild;

public class AddressValidation 
{
	private boolean bAddressValid, bAddressIsSchool;
	private int errorCode;
	private String errMssg;
		
	public AddressValidation(boolean bAddressValid, boolean bAddressIsSchool, int errorCode)
	{
		this.bAddressValid = bAddressValid;
		this.bAddressIsSchool = bAddressIsSchool;
		this.errorCode = errorCode;
		this.errMssg = "";
	}
	
	public AddressValidation(boolean bAddressValid, boolean bAddressIsSchool, int errorCode, 
							String errMssg)
	{
		this.bAddressValid = bAddressValid;
		this.bAddressIsSchool = bAddressIsSchool;
		this.errorCode = errorCode;
		this.errMssg = errMssg;
	}
		
	//getters
	boolean isAddressValid() { return bAddressValid; }
	boolean isAddressSchool() { return bAddressIsSchool; }
	int getErrorCode() { return errorCode; }
	String getErrorMessage() { return errMssg; }
		
	//setters
	void setAddressValid(boolean tf) { this.bAddressValid = tf; }
	void setAddressIsSchool(boolean tf) { this.bAddressIsSchool = tf; }
	void setErrorCode(int ec) { this.errorCode = ec; }
	void setErrorMessage(String errMssg) { this.errMssg = errMssg; }
}