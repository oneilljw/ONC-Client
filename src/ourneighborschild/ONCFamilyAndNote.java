package ourneighborschild;

public class ONCFamilyAndNote extends ONCObject
{
	private ONCFamily f;
	private ONCNote n;
		
	ONCFamilyAndNote(int id, ONCFamily fam, ONCNote note)
	{
		super(id);
		this.f = fam;
		this.n = note;
	}
		
	ONCFamily getFamily() { return f; }
	ONCNote getNote() { return n; }

	@Override
	public String[] getExportRow()
	{
		return null;
	}
}
