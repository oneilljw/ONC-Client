package ourneighborschild;

public class DupItem
{
	private ONCFamily	f1;
	private ONCChild	c1;
	private ONCFamily	f2;
	private ONCChild 	c2;
	private String 		result;
	
	public DupItem(ONCFamily family1, ONCChild child1, String rslt, ONCFamily family2, ONCChild child2) 
		
	{
		this.f1 = family1;
		this.c1 = child1;
		result = rslt;
		this.f2 = family2;
		this.c2 = child2;
	}

	//getters
	ONCFamily getFamily1() { return f1; }
	ONCFamily getFamily2() { return f2; }
	ONCChild getChild1() { return c1; }
	ONCChild getChild2() { return c2; }

	public String[] getDupTableRow(String name)
	{
		if(result.startsWith("No "))
		{
			String[] duptablerow = {"","","","","", result, "", "", "", "", ""};
			return duptablerow;
		}
		else if(name.equals("Child"))
		{
			String[] duptablerow = {f1.getONCNum(), c1.getChildFirstName(), c1.getChildLastName(), 
									c1.getChildGender(), c1.getChildDOBString("M/dd/yy"), result,
									f2.getONCNum(), c2.getChildFirstName(), c2.getChildLastName(), 
									c2.getChildGender(), c2.getChildDOBString("M/dd/yy")};
			return duptablerow;
		}
		else
		{
			String[] duptablerow = {f1.getONCNum(), f1.getHOHFirstName(), f1.getHOHLastName(), 
									f1.getHouseNum(), f1.getStreet(), result,
									f2.getONCNum(), f2.getHOHFirstName(), f2.getHOHLastName(),
									f2.getHouseNum(), f2.getStreet()};
			return duptablerow;
		}
	}
}
