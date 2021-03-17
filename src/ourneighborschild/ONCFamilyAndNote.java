package ourneighborschild;

public class ONCFamilyAndNote extends ONCObject
{
	private ONCFamily f;
	private FamilyHistory fh;
	private ONCNote n;
		
	ONCFamilyAndNote(int id, ONCFamily fam, FamilyHistory fh,  ONCNote note)
	{
		super(id);
		this.f = fam;
		this.fh = fh;
		this.n = note;
	}
		
	ONCFamily getFamily() { return f; }
	FamilyHistory getFamilyHistory() { return fh; }
	ONCNote getNote() { return n; }

	@Override
	public String[] getExportRow()
	{
		return null;
	}
}
