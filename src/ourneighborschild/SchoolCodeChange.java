package ourneighborschild;

public class SchoolCodeChange
{
	private String loosingCode;
	private String gainingCode;
	
	public SchoolCodeChange(String loosingCode, String gainingCode)
	{
		this.loosingCode = loosingCode;
		this.gainingCode = gainingCode;
	}
	
	//getters
	public String getDecCode() { return loosingCode; }
	public String getIncCode() { return gainingCode; }
}
