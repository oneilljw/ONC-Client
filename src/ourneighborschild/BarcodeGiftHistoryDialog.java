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

public class BarcodeGiftHistoryDialog extends BarcodeTableDialog
{
	/**
	 * This class implements a dialog which allows the user to manage users
	 */
	private static final long serialVersionUID = 1L;
	private static final int GIFT_COL= 0;
	private static final int DETAIL_COL = 1;
	private static final int IND_COL = 2;
	private static final int STATUS_COL = 3;
	private static final int ASSIGNEE_COL = 4;
	private static final int CLONED_GIFT_COL = 5;
	private static final int CHANGEDBY_COL = 6;
	private static final int TIMESTAMP_COL = 7;
	private static final int DEFAULT_TABLE_ROW_COUNT = 8;
	private static final int CLONED_GIFT_FIRST_GIFT_NUMBER = 3;
	
	private FamilyDB fDB;
	private ChildDB cDB;
	private ChildGiftDB cgDB;
	private ClonedGiftDB clonedGiftDB;
	private GiftCatalogDB cat;
	private PartnerDB partnerDB;
	private UserDB userDB;
	
	private ArrayList<ClonedGift> clonedGiftList;
	
	private boolean bClonedGift;
	private ONCChildGift cg;
	private ClonedGift clonedGift;
		
	public BarcodeGiftHistoryDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Gift History From Barcode");
		
		//Save the reference to the one gift catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the gift catalog, including
		//this dialog
		fDB = FamilyDB.getInstance();
		cDB = ChildDB.getInstance();
		cgDB = ChildGiftDB.getInstance();
		if(cgDB != null)
			cgDB.addDatabaseListener(this);
		clonedGiftDB = ClonedGiftDB.getInstance();
		if(clonedGiftDB != null)
			cgDB.addDatabaseListener(this);
		cat = GiftCatalogDB.getInstance();
		partnerDB = PartnerDB.getInstance();
		userDB = UserDB.getInstance();
		
		stAL = new ArrayList<ONCChildGift>();
		clonedGiftList = new ArrayList<ClonedGift>();
		
		btnAction.setVisible(false);
	}
	
	@Override
	String[] getColumnToolTips()
	{
		return new String[] {"Gift Category", "Detailed Gift Description",
							"# - ONC Determined Gift or * - Don't Assign Gift", 
							"Gift Life Cycle State", "Who's fulfilling?", "Is it a cloned gift?", 
							"Who Changed the Gift?", "Date Gift Changed"};
	}
	
	@Override
	int[] getColumnWidths() { return new int[] {72, 128, 24, 80, 112, 56, 96, 128}; }
	
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
    	String[] header = {"Gift", "Details", "Ind", "Status", "Assignee", "Changed By", "Time Stamp"};
    
    	ONCFileChooser oncfc = new ONCFileChooser(this);
       	File oncwritefile = oncfc.getFile("Select file for export of Gift History" ,
       							new FileNameExtensionFilter("CSV Files", "csv"), ONCFileChooser.SAVE_FILE);
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
						dlgTable.getSelectedRowCount() + " gifts sucessfully exported to " + oncwritefile.getName(), 
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
	
	void getGiftHistory(ONCChildGift cg)
	{	
		if(cg != null)
		{
			stAL = cgDB.getGiftHistory(cg.getChildID(), cg.getGiftNumber());	
		}
		else
			stAL.clear();
	}
	
	void getGiftHistory(ClonedGift clonedGift)
	{	
		if(clonedGift != null)
			clonedGiftList = (ArrayList<ClonedGift>) clonedGiftDB.getGiftHistory(clonedGift.getChildID(), clonedGift.getGiftNumber());	
		else
			clonedGiftList.clear();
	}

	@Override
	String getPrintTitle() { return bClonedGift ? "ONC Cloned Gift History" : "ONC Gift History"; }
	
	void onBarcodeTFEvent()
	{
		//if using UPC-E, eliminate check digits before converting to childwishID integer
		int cID, gn;
		String s = barcodeTF.getText();
		if(gvs.getBarcodeCode() == Barcode.UPCE)
		{
			cID = Integer.parseInt(s.substring(0, s.length()-2));
			gn = Integer.parseInt(s.substring(s.length()-2, s.length()-1));
		}
		else
		{
			cID = Integer.parseInt(s.substring(0, s.length()-1));
			gn = Integer.parseInt(s.substring(s.length()-1, s.length()-0));
		}
		
		//get gift history for bar code gift id. If found, notify entity listeners of
		//the gift entity selection.
		bClonedGift = gn >= CLONED_GIFT_FIRST_GIFT_NUMBER;
		System.out.println(String.format("BarcodeGiftHist.onBarcodeTFEvent: cID= %d, gn= %d", cID, gn));
		
		if(bClonedGift)
		{	
			clonedGift = clonedGiftDB.getClonedGift(cID, gn);
			if(clonedGift != null)
				System.out.println(String.format("BarcodeGiftHist.onBarcodeTFEvent: cloneID= %d, detail= %s",
						clonedGift.getID(), clonedGift.getDetail()));
			else
				System.out.println("BarcodeGiftHist.onBarcodeTFEvent: clonedGift is null");
		}
		else
			cg = cgDB.getGift(cID, gn);
		
		if(!bClonedGift && cg != null)
		{
			getGiftHistory(cg);
			ONCChild child = cDB.getChild(cID);
			if(child != null)
			{
				ONCFamily family = fDB.getFamily(child.getFamID());
				if(family != null)
				{
					fireEntitySelected(this, EntityType.GIFT, family, child, cg);
					if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
						lblInfo.setText(String.format("Gift History for %s %s, Gift %d, Family #%s",
							child.getChildFirstName(), child.getChildLastName(),
							cg.getGiftNumber()+1, family.getONCNum()));
					else
						lblInfo.setText(String.format("Gift History for %s %d,  Gift %d, Family #%s",
							"Child", cDB.getChildNumber(child),
							cg.getGiftNumber()+1, family.getONCNum()));
				}
			}
		}
		else if(bClonedGift && clonedGift != null)
		{
			getGiftHistory(clonedGift);
			
			System.out.println(String.format("BarcodeGiftHist.onBarcodeTFEvent: clonedGiftList.size() = %d", clonedGiftList.size()));
			
			ONCChild child = cDB.getChild(cID);
			if(child != null)
			{
				ONCFamily family = fDB.getFamily(child.getFamID());
				if(family != null)
				{
					fireEntitySelected(this, EntityType.GIFT, family, child, cg);
					if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
						lblInfo.setText(String.format("Gift History for %s %s, Gift %d, Family #%s",
							child.getChildFirstName(), child.getChildLastName(),
							clonedGift.getGiftNumber()+1, family.getONCNum()));
					else
						lblInfo.setText(String.format("Gift History for %s %d,  Gift %d, Family #%s",
							"Child", cDB.getChildNumber(child),
							clonedGift.getGiftNumber()+1, family.getONCNum()));
				}
			}
		}
		else
		{
			stAL.clear();
			clonedGiftList.clear();
			lblInfo.setText(String.format("Barcode %s not found", barcodeTF.getText()));
		}
		
		dlgTableModel.fireTableDataChanged();
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_WISH"))
		{
			//update the wish gift displayed if the added gift is from same child and gift #
			ONCChildGift addedGift = (ONCChildGift) dbe.getObject1();
			if(cg != null && addedGift.getChildID() == cg.getChildID() &&
					addedGift.getGiftNumber() == cg.getGiftNumber())
			{
				getGiftHistory(cg);
				dlgTableModel.fireTableDataChanged();
			}
		}
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_CLONED_GIFT"))
		{
			//update the wish gift displayed if the added gift is from same child and gift #
			ClonedGift addedGift = (ClonedGift) dbe.getObject1();
			if(clonedGift != null && addedGift.getChildID() == clonedGift.getChildID() &&
					addedGift.getGiftNumber() == clonedGift.getGiftNumber())
			{
				getGiftHistory(clonedGift);
				dlgTableModel.fireTableDataChanged();
			}
		}
	}
	
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.GIFT);
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Barcode Gift History Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Gift", "Details", "Ind", "Status", "Assignee", "Clone?", "Changed By", "Time Stamp"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount()
        {
        	if(bClonedGift)
        		return clonedGiftList != null ? clonedGiftList.size() : 0;
        	else
        		return stAL != null ? stAL.size() : 0; 
        }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCChildGift g = null;
        	ClonedGift clonedGift = null;
        	
        	if(bClonedGift)
        		 clonedGift = clonedGiftList.get(row);
        	else
        		 g = (ONCChildGift) stAL.get(row);
        	
        	if(col == GIFT_COL)
        	{
        		int giftID = bClonedGift ? clonedGift.getGiftID() : g.getGiftID();
        		ONCGift gift = cat.getGiftByID(giftID);
				return gift == null ? "None" : gift.getName();
        	}
        	else if(col == DETAIL_COL)
        		return bClonedGift ? clonedGift.getDetail() : g.getDetail();
        	else if (col == STATUS_COL)
        		return bClonedGift ? clonedGift.getGiftStatus().toString() : g.getGiftStatus().toString();
        	else if (col == IND_COL)
        	{
        		String[] indicators = {"", "*", "#"};
        		int resIndex = bClonedGift ? clonedGift.getIndicator() : g.getIndicator();
        		return indicators[resIndex];
        	}
        	else if(col == CLONED_GIFT_COL)
        		return bClonedGift;
        	else if (col == CHANGEDBY_COL)
        		return bClonedGift ? clonedGift.getChangedBy() : g.getChangedBy();
        	else if (col == TIMESTAMP_COL)
        	{
        		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:mm:ss");
        		return sdf.format(bClonedGift ? clonedGift.getDateChanged().getTime() : g.getDateChanged().getTime());
        	}
        	else if (col == ASSIGNEE_COL)
        	{
        		int partnerID = bClonedGift ? clonedGift.getPartnerID() : g.getPartnerID();
        		ONCPartner partner = partnerDB.getPartnerByID(partnerID);
        		return partner == null ? "None" : partner.getLastName();
        	}
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == CLONED_GIFT_COL)
        		return Boolean.class;
        	else
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
