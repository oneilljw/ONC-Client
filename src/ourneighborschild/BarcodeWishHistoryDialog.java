package ourneighborschild;


import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import au.com.bytecode.opencsv.CSVWriter;

public class BarcodeWishHistoryDialog extends BarcodeTableDialog
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int WISH_COL= 0;
	private static final int DETAIL_COL = 1;
	private static final int IND_COL = 2;
	private static final int STATUS_COL = 3;
	private static final int ASSIGNEE_COL = 4;
	private static final int CHANGEDBY_COL = 5;
	private static final int TIMESTAMP_COL = 6;
	private static final int DEFAULT_TABLE_ROW_COUNT = 7;
	
	private FamilyDB fDB;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ONCWishCatalog cat;
	private PartnerDB partnerDB;
	private UserDB userDB;
	
	private ONCChildWish cw;
		
	public BarcodeWishHistoryDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Wish History From Barcode");
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		fDB = FamilyDB.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		if(cwDB != null)
			cwDB.addDatabaseListener(this);
		cat = ONCWishCatalog.getInstance();
		partnerDB = PartnerDB.getInstance();
		userDB = UserDB.getInstance();
		
		stAL = new ArrayList<ONCChildWish>();
		
		btnAction.setVisible(false);
	}
	
	@Override
	String[] getColumnToolTips()
	{
		return new String[] {"Wish Category", "Detailed Wish Description",
							"# - ONC Determined Wish or * - Don't Assign Wish", 
							"Wish Life Cycle State", "Who's fulfilling?", 
							"Who Changed the Wish?", "Date Wish Changed"};
	}
	
	@Override
	int[] getColumnWidths() { return new int[] {72, 128, 24, 80, 112, 96, 128}; }
	
	@Override
	int[] getLeftColumns() { return new int[] {IND_COL}; }
	
	@Override
	int[] getCenteredColumns() { return new int[] {}; }
	
	@Override
	int getDefaultRowCount() { return DEFAULT_TABLE_ROW_COUNT; }
	
	@Override
	AbstractTableModel getDialogTableModel() { return new DialogTableModel(); }
	
	@Override
	void onExport()
	{
    	String[] header = {"Wish", "Details", "Ind", "Status", "Assignee", "Changed By", "Time Stamp"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of Wish History" ,
       										new FileNameExtensionFilter("CSV Files", "csv"), 1);
       	if(oncwritefile!= null)
       	{
       		//If user types a new filename without extension.csv, add it
	    	String filePath = oncwritefile.getPath();
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    int[] row_sel = dlgTable.getSelectedRows();
	    	    ArrayList<String> exportRow = new ArrayList<String>();
	    	    
	    	    for(int i=0; i<dlgTable.getSelectedRowCount(); i++)
	    	    {
	    	    	//build the export row
	    	    	int row = row_sel[i];
	    	    	for(int col=0; col < header.length; col++)
	    	    		exportRow.add((String) dlgTable.getValueAt(row,  col));
		    	    	
	    	    	writer.writeNext(exportRow.toArray(new String[exportRow.size()]));
	    	    	
	    	    	exportRow.clear();
	    	    }
	    	   
	    	    writer.close();
	    	    
	    	    JOptionPane.showMessageDialog(this, 
						dlgTable.getSelectedRowCount() + " inventory gifts sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, gvs.getImageIcon(0));
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	}
	
	void getWishHistory(ONCChildWish cw)
	{	
		if(cw != null)
		{
			stAL = cwDB.getWishHistory(cw.getChildID(), cw.getWishNumber());	
		}
		else
			stAL.clear();
	}

	@Override
	String getPrintTitle() { return "ONC Wish History"; }
	
	void onBarcodeTFEvent()
	{
		//if using UPC-E, eliminate check digits before converting to childwishID integer
		int cID, wn;
		String s = barcodeTF.getText();
		if(gvs.getBarcodeCode() == Barcode.UPCE)
		{
			cID = Integer.parseInt(s.substring(0, s.length()-2));
			wn = Integer.parseInt(s.substring(s.length()-2, s.length()-1));
				
		}
		else
		{
			cID = Integer.parseInt(s.substring(0, s.length()-1));
			wn = Integer.parseInt(s.substring(s.length()-1, s.length()-0));
		}
		
		//get Wish History for bar code wish id. If found, notify entity listeners of
		//the Wish entity selection.
		cw = cwDB.getWish(cID, wn);
		if(cw != null)
		{
			getWishHistory(cw);
			ONCChild child = cDB.getChild(cID);
			if(child != null)
			{
				ONCFamily family = fDB.getFamily(child.getFamID());
				if(family != null)
				{
					fireEntitySelected(this, EntityType.WISH, family, child, cw);
					if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
						lblInfo.setText(String.format("Wish History for %s %s, Wish %d, Family #%s",
							child.getChildFirstName(), child.getChildLastName(),
							cw.getWishNumber()+1, family.getONCNum()));
					else
						lblInfo.setText(String.format("Wish History for %s %d,  Wish %d, Family #%s",
							"Child", cDB.getChildNumber(child),
							cw.getWishNumber()+1, family.getONCNum()));
				}
			}
		}
		else
		{
			stAL.clear();
			lblInfo.setText(String.format("Barcode %s not found", barcodeTF.getText()));
		}
		
		dlgTableModel.fireTableDataChanged();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_WISH"))
		{
			//update the wish history displayed if the added wish is from same child and wish #
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject1();
			if(cw != null && addedWish.getChildID() == cw.getChildID() &&
					addedWish.getWishNumber() == cw.getWishNumber())
			{
				getWishHistory(cw);
				dlgTableModel.fireTableDataChanged();
			}
		}
	}
	
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.WISH);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Barcode Wish History Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Wish", "Details", "Ind", "Status", "Assignee", "Changed By", "Time Stamp"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return stAL != null ? stAL.size() : 0; }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCChildWish cw = (ONCChildWish) stAL.get(row);
        	if(col == WISH_COL)
        	{
        		ONCWish wish = cat.getWishByID(cw.getWishID());
				return wish == null ? "None" : wish.getName();
        	}
        	else if(col == DETAIL_COL)
        		return cw.getChildWishDetail();
        	else if (col == STATUS_COL)
        		return cw.getChildWishStatus().toString();
        	else if (col == IND_COL)
        	{
        		String[] indicators = {"", "*", "#"};
        		return indicators[cw.getChildWishIndicator()];
        	}
        	else if (col == CHANGEDBY_COL)
        		return cw.getChildWishChangedBy();
        	else if (col == TIMESTAMP_COL)
        	{
        		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:mm:ss");
        		return sdf.format(cw.getChildWishDateChanged().getTime());
        	}
        	else if (col == ASSIGNEE_COL)
        	{
        		ONCPartner partner = partnerDB.getPartnerByID(cw.getChildWishAssigneeID());
        		return partner == null ? "None" : partner.getName();
        	}
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }  
    }

	@Override
	void onActionEvent(ActionEvent e) {
		// TODO Auto-generated method stub
	}
}
