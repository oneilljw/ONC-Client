package ourneighborschild;

public class ONCEmailAttachment
{
	private String filename;
	private String cid;
	private String disposition;
	
	ONCEmailAttachment(String fn, String cid, String disp)
	{
		filename = fn;
		this.cid = cid;
		disposition = disp;
	}
	
	//getters
	public String getFilename() { return filename; }
	public String getCID() { return cid; }
	public String getDisposition() { return disposition; }
	
}
