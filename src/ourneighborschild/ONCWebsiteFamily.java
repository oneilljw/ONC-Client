package ourneighborschild;

public class ONCWebsiteFamily
{
	private int		id;
	private String	oncNum;
	private String	fstatus;
	private String	dstatus;	
	private String 	DNSCode;
	private String	HOHFirstName;
	private String	HOHLastName;	
	private String	mealStatus;
	
	public ONCWebsiteFamily(ONCFamily f)
	{
		String[] famstatus = {"Unverified", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
		String[] delstatus = {"Empty", "Contacted", "Confirmed", "Assigned", "Attempted", "Returned", "Delivered", "Counselor Pick-Up"};
		id = f.id;
		oncNum = f.getONCNum();
		fstatus = famstatus[f.getFamilyStatus()];
		fstatus = delstatus[f.getDeliveryStatus()];
		DNSCode = f.getDNSCode();
		HOHFirstName = f.getHOHFirstName();
		HOHLastName = f.getHOHLastName();
		mealStatus = f.getMealStatus().toString();
	}

	/**
	 * @return the id
	 */
	int getId() {
		return id;
	}

	/**
	 * @return the oncNum
	 */
	String getOncNum() {
		return oncNum;
	}

	/**
	 * @return the fstatus
	 */
	String getFstatus() {
		return fstatus;
	}

	/**
	 * @return the dstatus
	 */
	String getDstatus() {
		return dstatus;
	}

	/**
	 * @return the dNSCode
	 */
	String getDNSCode() {
		return DNSCode;
	}

	/**
	 * @return the hOHFirstName
	 */
	String getHOHFirstName() {
		return HOHFirstName;
	}

	/**
	 * @return the hOHLastName
	 */
	String getHOHLastName() {
		return HOHLastName;
	}

	/**
	 * @return the mealStatus
	 */
	String getMealStatus() {
		return mealStatus;
	}

	/**
	 * @param id the id to set
	 */
	void setId(int id) {
		this.id = id;
	}

	/**
	 * @param oncNum the oncNum to set
	 */
	void setOncNum(String oncNum) {
		this.oncNum = oncNum;
	}

	/**
	 * @param fstatus the fstatus to set
	 */
	void setFstatus(String fstatus) {
		this.fstatus = fstatus;
	}

	/**
	 * @param dstatus the dstatus to set
	 */
	void setDstatus(String dstatus) {
		this.dstatus = dstatus;
	}

	/**
	 * @param dNSCode the dNSCode to set
	 */
	void setDNSCode(String dNSCode) {
		DNSCode = dNSCode;
	}

	/**
	 * @param hOHFirstName the hOHFirstName to set
	 */
	void setHOHFirstName(String hOHFirstName) {
		HOHFirstName = hOHFirstName;
	}

	/**
	 * @param hOHLastName the hOHLastName to set
	 */
	void setHOHLastName(String hOHLastName) {
		HOHLastName = hOHLastName;
	}

	/**
	 * @param mealStatus the mealStatus to set
	 */
	void setMealStatus(String mealStatus) {
		this.mealStatus = mealStatus;
	}
}
