package ourneighborschild;

public class ONCWebChild
{
	private int		id;
	private int		famid;
	private String	firstname;
	private String	lastname;
	private String	school;
	private String	gender;
	private String	sDOB;
	@SuppressWarnings("unused")
	private String  wish0;	//childs first wish
	@SuppressWarnings("unused")
	private String  wish1;	//childs second wish
	@SuppressWarnings("unused")
	private String  wish2;	//childs first wish
	@SuppressWarnings("unused")
	private String	wish3;	//childs alternate wish
	
	
	public ONCWebChild(ONCChild c, boolean bIncludeSchool)
	{
		this.id = c.getID();
		this.famid = c.getFamID();
		this.firstname = c.getChildFirstName();
		this.lastname = c.getChildLastName();
		this.school = bIncludeSchool ? c.getChildSchool() : "";
		this.gender = c.getChildGender();
		this.sDOB = c.getChildDOBString("M/dd/yyyy");
		this.wish0 = "";
//		this.wish1 = "";
		this.wish1 = "Age Appropriate Gift";	//Changed in 2020: Wish 2 requried to be "Age Appropriate Gift"
//		this.wish2 = "Age Appropriate Gift";	//Changed in 2020: Only 2 Gifts allowed. 
		this.wish2 = "No Wish 3 - Covid 19";
		this.wish3 = "";
	}
	
	//getters
	int getID() { return id; }
	int getFamID() { return famid; }
	String getFirstName() { return firstname; }
	String getLirstName() { return lastname; }
	String getSchool() { return school; }
	String getGender() { return gender; }
	String getDOB() { return sDOB; }
}
