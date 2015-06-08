package ourneighborschild;

import java.io.Serializable;

public class WishDetail extends ONCObject implements Serializable
{
	/**
	 * This class provides the data for one of the choices that a user must select from when
	 * they assign a wish to a child and that wish requires additional detail to be specified.
	 * In the ONC application, these objects are stored in an ONCWish object which are stored in the
	 * Wish Catalog
	 */
	private static final long serialVersionUID = -571220931522912662L;
	private String name;
//	private String[] choices;
	private String zChoices;
	
	WishDetail(int id, String name, String choices)
	{
		super(id);
		this.name = name;
		this.zChoices = choices;			
	}
	
	//constructor used when making a copy of a WishDetail
	public WishDetail(WishDetail wd)
	{
		super(wd.id);
		this.name = wd.name;
		this.zChoices = wd.zChoices;			
	}
	
	//Constructor used when wish detail objects are imported from .csv file
	public WishDetail(String[] wishdetail)	
	{
		super(Integer.parseInt(wishdetail[0]));
		name = wishdetail[1];
		zChoices = wishdetail[2];	
	}
	
	//getters
	String getWishDetailName() { return name; }
	String[] getWishDetailChoices() { return zChoices.split(";"); }
	String getWishDetailChoiceString() { return zChoices; }
	
	//setters
	void setDetailName(String n) { name = n; }
	void setDetailChoices(String[] choices)
	{
		if(choices.length == 0)
			zChoices = "No Choices;";
		else
		{
			StringBuffer buf = new StringBuffer("");
			for(String s:choices)
				buf.append(s + ";");
			zChoices = buf.toString();
		}	
	}
	
	@Override
	public String[] getExportRow()
	{
		String[] exportRow = {Long.toString(id), name, zChoices};
		
		return exportRow;
		
	}
}
