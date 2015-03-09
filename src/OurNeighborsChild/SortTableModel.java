package OurNeighborsChild;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SortTableModel extends AbstractTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ONCSortObject> stAL;
	private String[] columns;
	
	SortTableModel(List<ONCSortObject> stAL, String[] columns)
	{
		this.stAL = stAL;
		this.columns = columns;
	}
	@Override
	public int getColumnCount() { return columns.length; }
	 
	@Override
	public int getRowCount() { return stAL.size(); }
	
	@Override
	public String getColumnName(int col) { return columns[col]; }
	
	@Override
	public Object getValueAt(int row, int col) { return stAL.get(row).getTableCell(col); }
	
	@Override
	public boolean isCellEditable(int row, int col)
	{
	    //Only the check boxes can be edited and then only if there is not
		//a wish already selected from the list associated with that column
		return false;
	}
}
