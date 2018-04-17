package ourneighborschild;

public class VolAct extends ONCObject
{
	private int volID;
	private int actID;
	private long geniusID;
	private int qty;
	private String comment;

	public VolAct(int id, int volID, int actID, long geniusID, int qty, String comment)
	{
		super(id);
		this.volID= volID;
		this.actID = actID;
		this.geniusID = geniusID;
		this.qty = qty;
		this.comment = comment;
	}
	
	public VolAct(VolAct va)
	{
		super(va.id);
		this.volID= va.volID;
		this.actID = va.actID;
		this.geniusID = va.geniusID;
		this.qty = va.qty;
		this.comment = va.comment;
	}
	
	public VolAct(String[] line)
	{
		super(Integer.parseInt(line[0]));
		this.volID = line[1].isEmpty() ? -1 : Integer.parseInt(line[1]);
		this.actID = line[2].isEmpty() ? -1 : Integer.parseInt(line[2]);
		this.geniusID = line[3].isEmpty() ? -1 :Long.parseLong(line[3]);
		this.qty = line[4].isEmpty() ? 0 : Integer.parseInt(line[4]);
		this.comment = line[5];
	}
	
	//getters
	public int getVolID() {  return volID; }
	public int getActID() {  return actID; }
	public long getGeniusID() {  return geniusID; }
	public int getQty() {  return qty; }
	public String getComment() { return comment; }
	
	//setters
	public void setVolID(int volID) { this.volID = volID; }
	public void setQty(int qty) { this.qty = qty; }
	public void setComment(String comment) { this.comment = comment; }

	@Override
	public String[] getExportRow()
	{
		String[] row = new String[6];
		row[0] = Integer.toString(id);
		row[1] = Integer.toString(volID);
		row[2] = Integer.toString(actID);
		row[3] = Long.toString(geniusID);
		row[4] = Integer.toString(qty);
		row[5] = comment;
		
		return row;
	}
}
