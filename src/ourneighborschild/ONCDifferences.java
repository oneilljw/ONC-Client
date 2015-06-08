package ourneighborschild;

public class ONCDifferences
{
	String		ONCNum;
	String		ODBFamilyNum;
	String		BatchNum;
	String		DNSCode;
	String 		ClientFamily;
	String		ODBHouseNum;
	String		ONCHouseNum;
	String		ODBStreet;
	String 		ONCStreet;
	String		ODBUnitNum;
	String		ONCUnitNum;
	String		ODBAllPhoneNumbers;
	String		ONCAllPhoneNumbers;
	String		ODBFamilyEmail;
	String		ONCFamilyEmail;
	String		ODBDetails;
	String		ONCDetails;
	String		ODBWishList;
	String		ONCWishList;
	
	//Constructor
	public ONCDifferences(String oncnum, String odbFamilyNum, String batchnum, String dnsCode, String clientfam, String odbHN, String oncHN,
				String odbStreet, String oncStreet, String odbUnitNum, String oncUnitNum, String odbAPN, String oncAPN, 
				String odbFamilyEmail, String oncFamilyEmail, String odbDetails, String oncDetails, String odbWL, 
				String oncWL)
			{
			ONCNum = oncnum;
			ODBFamilyNum = odbFamilyNum;
			BatchNum = batchnum;
			DNSCode = dnsCode;
			ClientFamily = clientfam;
			ODBHouseNum =  odbHN;
			ONCHouseNum = oncHN;
			ODBStreet = odbStreet;
			ONCStreet = oncStreet;
			ODBUnitNum = odbUnitNum;
			ONCUnitNum = oncUnitNum;
			ODBAllPhoneNumbers = odbAPN;
			ONCAllPhoneNumbers = oncAPN;
			ODBFamilyEmail = odbFamilyEmail;
			ONCFamilyEmail = oncFamilyEmail;
			ODBDetails = odbDetails;
			ONCDetails = oncDetails;
			ODBWishList = odbWL;
			ONCWishList = oncWL;
			}
	
	String[] GetDifferencesCSVRow()
	{
		String[] diff = new String[19];
		
		diff[0] =	ONCNum;
		diff[1] =	ODBFamilyNum;
		diff[2] =	BatchNum;
		diff[3] = 	DNSCode;
		diff[4] =	ClientFamily;
		diff[5] =	ODBHouseNum;
		diff[6] =	ONCHouseNum;
		diff[7] =	ODBStreet;
		diff[8] =	ONCStreet;
		diff[9] =	ODBUnitNum;
		diff[10] =	ONCUnitNum;
		diff[11] =	ODBAllPhoneNumbers;
		diff[12] =	ONCAllPhoneNumbers;
		diff[13] =	ODBFamilyEmail;
		diff[14] =	ONCFamilyEmail;		
		diff[15] =	ODBDetails;
		diff[16] =	ONCDetails;
		diff[17] =	ODBWishList;
		diff[18] =	ONCWishList;
		
		return diff;
	}
			
}
