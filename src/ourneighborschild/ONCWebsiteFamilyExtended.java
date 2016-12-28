package ourneighborschild;

public class ONCWebsiteFamilyExtended extends ONCWebsiteFamily 
{
	private String	BatchNum;
	private String	Language;
	private String	HouseNum;
	private String	Street;
	private String	UnitNum;
	private String	City;
	private String	ZipCode;
	private String 	Region;
	private String	substituteDeliveryAddress;	//in Google Map Address format		
	private String	HomePhone;
	private String	OtherPhone;
	private String	FamilyEmail;
	private String	details;
	private String  transportation;
	private int		mealID;
	private String 	notes;
	private String  delInstr;
	private boolean bGiftCardOnly;
	
	public ONCWebsiteFamilyExtended(ONCFamily f, String region)
	{
		super(f);
		this.BatchNum = f.getBatchNum();
		this.Language = f.getLanguage();
		this.HouseNum = f.getHouseNum();
		this.Street = f.getStreet();
		this.UnitNum = f.getUnitNum();
		this.City = f.getCity();
		this.ZipCode = f.getZipCode();
		this.Region = region;
		this.substituteDeliveryAddress = f.getSubstituteDeliveryAddress();	//in Google Map Address format		
		this.HomePhone = f.getHomePhone();
		this.OtherPhone = f.getOtherPhon();
		this.FamilyEmail = f.getEmail();
		this.details = f.getDetails();
		this.transportation = f.getTransportation().toString();
		this.mealID = f.getMealID();
		this.notes = f.getNotes();
		this.delInstr = f.getDeliveryInstructions();
		this.bGiftCardOnly = f.isGiftCardOnly();
	}
	
	//getters
	String getBatchNum() {return BatchNum; }
	String getLanguage() {return Language;}
	String getHouseNum() {return HouseNum;}
	String getStreet() {return Street;}
	String getUnitNum() {return UnitNum;}
	String getCity() {return City;}
	String getZipCode() {return ZipCode;}
	String getRegion() { return Region; }
	String getSubstituteDeliveryAddress() {return substituteDeliveryAddress;}
	String getHomePhone() {return HomePhone;}
	String getOtherPhone() {return OtherPhone;}
	String getFamilyEmail() {return FamilyEmail;}
	String getDetails() {return details;}
	String getTransportation() { return transportation; }
	int getMealID() {return mealID;}
	String getNotes() { return notes; }
	String getDeliveryInstructions() { return delInstr; }
	boolean isGiftCardOnly() { return bGiftCardOnly; }

	//setters
	void setBatchNum(String bn) {BatchNum = bn;}
	void setLanguage(String language) {Language = language;}
	void setHouseNum(String houseNum) {HouseNum = houseNum;}
	void setStreet(String street) {Street = street;}
	void setUnitNum(String unitNum) {UnitNum = unitNum;}
	void setCity(String city) {City = city;}
	void setZipCode(String zipCode) {ZipCode = zipCode;}
	public void setRegion(String reg) { Region = reg; }
	void setSubstituteDeliveryAddress(String substituteDeliveryAddress) {this.substituteDeliveryAddress = substituteDeliveryAddress;}
	void setHomePhone(String homePhone) {HomePhone = homePhone;}
	void setOtherPhone(String otherPhone) {OtherPhone = otherPhone;}
	void setFamilyEmail(String familyEmail) {FamilyEmail = familyEmail;}
	void setDetails(String details) {this.details = details;}
	void setTransportation(Transportation t) {this.transportation = t.toString(); }
	void setMealID(int mealID) {this.mealID = mealID;}
	void setNotes( String notes) { this.notes = notes; }
	void setDeliveryInstructions( String di) { this.delInstr = di; }
	void setGiftCardOnly( boolean gco) { this.bGiftCardOnly = gco; }
}
