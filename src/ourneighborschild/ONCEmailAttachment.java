package ourneighborschild;

import java.io.File;

public class ONCEmailAttachment
{
	private File file;
	private String cid;
	private String disposition;
	
	ONCEmailAttachment(File file, String cid, String disp)
	{
		this.file = file;
		this.cid = cid;
		this.disposition = disp;
	}
	
	//getters
	public File getFile() { return file; }
	public String getCID() { return cid; }
	public String getDisposition() { return disposition; }
	
}
