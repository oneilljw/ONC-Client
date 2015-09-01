package ourneighborschild;

public class ONCWebsiteFamilyExtended extends ONCWebsiteFamily 
{
	private String	Language;
	private String	HouseNum;
	private String	Street;
	private String	UnitNum;
	private String	City;
	private String	ZipCode;
	private String	substituteDeliveryAddress;	//in Google Map Address format		
	private String	HomePhone;
	private String	OtherPhone;
	private String	FamilyEmail;
	private String	details;
	private int		mealID;
	
	public ONCWebsiteFamilyExtended(ONCFamily f)
	{
		super(f);
		this.Language = f.getLanguage();
		this.HouseNum = f.getHouseNum();
		this.Street = f.getStreet();
		this.UnitNum = f.getUnitNum();
		this.City = f.getCity();
		this.ZipCode = f.getZipCode();
		this.substituteDeliveryAddress = f.getSubstituteDeliveryAddress();	//in Google Map Address format		
		this.HomePhone = f.getHomePhone();
		this.OtherPhone = f.getOtherPhon();
		this.FamilyEmail = f.getFamilyEmail();
		this.details = f.getDetails();
		this.mealID = f.getMealID();
	}
	
	//getters
	String getLanguage() {return Language;}
	String getHouseNum() {return HouseNum;}
	String getStreet() {return Street;}
	String getUnitNum() {return UnitNum;}
	String getCity() {return City;}
	String getZipCode() {return ZipCode;}
	String getSubstituteDeliveryAddress() {return substituteDeliveryAddress;}
	String getHomePhone() {return HomePhone;}
	String getOtherPhone() {return OtherPhone;}
	String getFamilyEmail() {return FamilyEmail;}
	String getDetails() {return details;}
	int getMealID() {return mealID;}

	//setters
	void setLanguage(String language) {Language = language;}
	void setHouseNum(String houseNum) {HouseNum = houseNum;}
	void setStreet(String street) {Street = street;}
	void setUnitNum(String unitNum) {UnitNum = unitNum;}
	void setCity(String city) {City = city;}
	void setZipCode(String zipCode) {ZipCode = zipCode;}
	void setSubstituteDeliveryAddress(String substituteDeliveryAddress) {this.substituteDeliveryAddress = substituteDeliveryAddress;}
	void setHomePhone(String homePhone) {HomePhone = homePhone;}
	void setOtherPhone(String otherPhone) {OtherPhone = otherPhone;}
	void setFamilyEmail(String familyEmail) {FamilyEmail = familyEmail;}
	void setDetails(String details) {this.details = details;}
	void setMealID(int mealID) {this.mealID = mealID;}		
}