package ourneighborschild;

public class DistributionCenter extends ONCEntity
{
	/**
	 * Basic object for distribution centers, which are locations or facilities where
	 * families can pick up their gifts. 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String acronym;
	private String streetnum;
	private String street;
	private String suffix;
	private String city;
	private String zipcode;
	private String googleMapURL;

	public DistributionCenter(int id, String name, String acronym, String streetnum, String street, 
			String suffix, String city, String zipcode, String url, long today, String changedBy,
			int slpos, String slmssg, String slchgby)
	{
		super(id, today, changedBy, slpos, slmssg, slchgby);
		this.name = name;
		this.acronym = acronym;
		this.streetnum = streetnum;
		this.street = street;
		this.suffix = suffix;
		this.city = city;
		this.zipcode = zipcode;
		this.googleMapURL = url;
	}
	
	public DistributionCenter(String[] nextLine)
	{
		super(Integer.parseInt(nextLine[0]), Long.parseLong(nextLine[10]), nextLine[9],
				Integer.parseInt(nextLine[11]), nextLine[12], nextLine[13]);
		this.name = nextLine[1];
		this.acronym = nextLine[2];
		this.streetnum = nextLine[3];
		this.street = nextLine[4];
		this.suffix = nextLine[5];
		this.city = nextLine[6];
		this.zipcode = nextLine[7];
		this.googleMapURL = nextLine[8];
	}
	
	public DistributionCenter(DistributionCenter dc)
	{
		super(dc.id, dc.timestamp, dc.changedBy, dc.slPos, dc.slMssg, dc.slChangedBy);
		this.name = dc.name;
		this.acronym = dc.acronym;
		this.streetnum = dc.streetnum;
		this.street =dc.street;
		this.suffix = dc.suffix;
		this.city = dc.city;
		this.zipcode = dc.zipcode;
		this.googleMapURL = dc.googleMapURL;
	}
	
	DistributionCenter(int id, String name, String acronym)
	{
		super(id, System.currentTimeMillis(), "", 3, "","");
		this.name = name;
		this.acronym = acronym;
		this.streetnum = "";
		this.street = "";
		this.suffix = "";
		this.city = "";
		this.zipcode = "";
		this.googleMapURL = "";
	}
	
	//getters
	public String getName() { return name; }
	public String getAcronym() { return acronym; }
	public String getStreetNum() { return streetnum; }
	public String getStreet() { return street; }
	public String getSuffix() { return suffix; }
	public String getCity() { return city; }
	String getZipcode() { return zipcode; }
	public String getGoogleMapURL() { return googleMapURL; }
	
	@Override
	public String toString() { return name; }
	
	//setters
	void setName(String name) {  this.name = name; }
	void setAcronym(String acronym) { this.acronym = acronym; }
	void setStreetNum(String streenum) {  this.streetnum = streenum; }
	void setStreet(String street) { this.street = street; }
	void setSuffix(String suffix) {  this.suffix = suffix; }
	void setCity(String city) {  this.city = city; }
	void setZipcode(String zipcode) { this.zipcode = zipcode; }
	void setGoogleMapURL(String url) { this.googleMapURL = url; }

	@Override
		public String[] getExportRow()
	{
		String[] row= {Integer.toString(id), name, acronym, streetnum, street, suffix, city, zipcode, googleMapURL,
						changedBy, Long.toString(timestamp), Integer.toString(slPos), slMssg, slChangedBy};
		return row;
	}
}
