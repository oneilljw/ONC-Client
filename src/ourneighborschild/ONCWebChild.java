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
	
	public ONCWebChild(ONCChild c)
	{
		this.id = c.getID();
		this.famid = c.getFamID();
		this.firstname = c.getChildFirstName();
		this.lastname = c.getChildLastName();
		this.school = c.getChildSchool();
		this.gender = c.getChildGender();
		this.sDOB = c.getChildDOBString("M/dd/yyyy");
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
