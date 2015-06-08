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
	String getFilename() { return filename; }
	String getCID() { return cid; }
	String getDisposition() { return disposition; }
	
}
