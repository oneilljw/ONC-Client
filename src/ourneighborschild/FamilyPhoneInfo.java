package ourneighborschild;

public class FamilyPhoneInfo
{
	private PhoneInfo[] famPhoneInfo;
	private int famPhoneCode;
	
	public FamilyPhoneInfo()
	{
		famPhoneInfo = new PhoneInfo[3];
		famPhoneInfo[0] = new PhoneInfo();
		famPhoneInfo[1] = new PhoneInfo();
		famPhoneInfo[2] = new PhoneInfo();
		
		famPhoneCode = 0;
	}
	
	//getters
	public PhoneInfo getPhoneInfo(int phoneid)
	{
		if(phoneid >= 0 && phoneid <=2)
			return famPhoneInfo[phoneid];
		else
			return null;
	}
	
	public int getFamilyPhoneCode() { return famPhoneCode; }
	
	boolean isPhoneValid(int phoneid)
	{
		return famPhoneInfo[phoneid].isPhoneValid();
	}
	
	boolean isPhoneEmpty(int phoneid)
	{
		return famPhoneInfo[phoneid].isEmpty();
	}
	
	public int getNumberOfValidPhoneNumbers()
	{
		int nValidPhoneNumbers = 0;
		for(PhoneInfo pi : famPhoneInfo)
			if(pi.isPhoneValid())
				nValidPhoneNumbers++;
		
		return nValidPhoneNumbers;
	}
	
	//setters
	public void setPhoneInfo(int phoneid, PhoneInfo pi)
	{
		if(phoneid >= 0 && phoneid <=2)
			famPhoneInfo[phoneid] = pi;
	}
	
	public void setFamilyPhoneCode(int code) { this.famPhoneCode = code; }
}
