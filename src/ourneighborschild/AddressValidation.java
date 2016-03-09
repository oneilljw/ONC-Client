package ourneighborschild;

public class AddressValidation 
{
	private boolean bAddressValid;
	private int errorCode;
		
	public AddressValidation(boolean bAddressValid, int errorCode)
	{
		this.bAddressValid = bAddressValid;
		this.errorCode = errorCode;
	}
		
	//getters
	boolean isAddressValid() { return bAddressValid; }
	int getErrorCode() { return errorCode; }
		
	//setters
	void setAddressValid(boolean tf) { this.bAddressValid = tf; }
	void setErrorCode(int ec) { this.errorCode = ec; }
}