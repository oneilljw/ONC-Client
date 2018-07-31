package ourneighborschild;

public class MyNeighborhoodReport
{
	private Metadata metadata;
	private Result[] results;
	
	FCSchool getElementarySchool() 
	{ 
		return results.length == 1 ? results[0].getElementarySchool()  : null;
	}
	
	private class Metadata
	{
		private String point;
	}
	
	private class Result
	{
		private District vadelegatedistrict;
		private District censustracts;
		private District vasenatedistrict;
		private District supervisordistrict;
		private District policestation;
		private District firestation;
		private District uscongressionaldistrict;
		private FCSchool elementaryschool;
		private FCSchool middleschool;
		private FCSchool highschool;
		private VotingLocation votinglocation;
		private Demographics demographics;
		private USSenator[] ussenators;
		private SchoolBoardMemberAtLarge[] schoolboardmembersatlarge;
		
		FCSchool getElementarySchool() { return elementaryschool; }
	}
	private class District
	{
		private String label;
		private String name;
		private String url;
		private String point;
		private int distance;
		private String address;
		private String city;
		private String zip;
	}
	
	private class FCSchool
	{
		private String label;
		private String name;
		private String url;
		private String point;
		private int distance;
		private String address;
		private String city;
		private String zip;
		private int schoolnumber;
	}
	
	private class VotingLocation
	{
		private String label;
		private String name;
		private String url;
		private String point;
		private int distance;
		private String address;
		private String city;
		private String zip;
		private int schoolnumber;
		private String precinct;
		private int precinctnumber;
	}
	
	private class Demographics
	{
		private String district;
		private String name;
		private String url;
	}
	
	private class USSenator
	{
		private String district;
		private String name;
		private String url;
	}
	
	private class SchoolBoardMemberAtLarge
	{
		private String district;
		private String name;
		private String url;
	}
}
