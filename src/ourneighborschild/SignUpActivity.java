package ourneighborschild;

public class SignUpActivity
{
	private String location;
	private long startdate;	//time in seconds, not milliseconds
	private long endtime;
	private String phone;
	private String state;
	private String firstname;
//	private long itemmemberid;
	private String email;
	private int myqty;
	private String comment;
	private String enddatestring;
	private String status;
	private String country;
	private String lastname;
	private long slotitemid;
	private String phonetype;
	private String offset;
	private String startdatestring;
	private String address1;
	private String address2;
//	private int hastime;
	private String zipcode;
	private String amountpaid;
	private long enddate;	//time in seconds, not milliseconds
	private String item;
	private long starttime;
	private String city;
	
	//getters
	String getLocation() {return location;}
	long getStartdate(){return startdate;}
	public long getEndtime(){return endtime;}
	String getPhone(){return phone;}
	String getState(){return state;}
	public String getFirstname(){return firstname;}
//	long getItemmemberid() {return itemmemberid;}
	public String getEmail() {return email;}
	public int getMyqty() {return myqty;}
	public String getComment() {return comment;}
	String getEnddatestring() {return enddatestring;}
	String getStatus() {return status;}
	String getCountry() {return country;}
	public String getLastname() {return lastname;}
	public long getSlotitemid() {return slotitemid;}
	String getPhonetype() {return phonetype;}
	String getOffset() {return offset;}
	String getStartdatestring() {return startdatestring;}
	String getAddress1() {return address1;}
	String getAddress2() {return address2;}
//	int getHastime() {return hastime;}
	String getZipcode() {return zipcode;}
	String getAmountpaid() {return amountpaid;}
	public long getEnddate() {return enddate;}
	public String getItem() {return item;}
	long getStarttime() {return starttime;}
	String getCity() {return city;}
	
	//setters
	public void setEnddate(long enddate) { this.enddate = enddate; }
	
	public String[] getExportRow()
	{
		String[] row = new String[11];
		row[0] = firstname;
		row[1] = lastname;
		row[2] = email;
		row[3] = phone;
		row[4] = phonetype;
		row[5] = address1;
		row[6] = address2;
		row[7] = zipcode;
		row[8] = Integer.toString(myqty);
		row[9] = item;
		row[10] = comment;
		
		return row;
	}
}
