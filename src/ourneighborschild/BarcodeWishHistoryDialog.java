package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class BarcodeWishHistoryDialog extends ONCTableDialog implements ActionListener, DatabaseListener
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
	
	private JTextField barcodeTF;
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JLabel lblChildInfo;
	private JButton btnPrint;
	private FamilyDB fDB;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ONCWishCatalog cat;
	private PartnerDB partnerDB;
	
	private List<ONCChildWish> stAL;
		
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
		
		stAL = new ArrayList<ONCChildWish>();
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodeTF = new JTextField(6);
    	barcodeTF.setMaximumSize(new Dimension(112,56));
//    	oncnumTF.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		
		topPanel.add(lblONCIcon);
		topPanel.add(barcodeTF);
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"Wish Category", "Detailed Wish Description",
								"# - ONC Determined Wish or * - Don't Assign Wish", 
								"Wish Life Cycle State", "Who's fulfilling?", 
								"Who Changed the Wish?", "Date Wish Changed"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {72, 128, 24, 80, 112, 96, 128};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        dlgTable.getColumnModel().getColumn(IND_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, dlgTable.getRowHeight()*7));
        
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lblChildInfo = new JLabel("");
        textPanel.add(lblChildInfo);
        bottomPanel.add(textPanel);
        
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        btnPrint = new JButton("Print");
        btnPrint.setToolTipText("Print the wish history");
        btnPrint.addActionListener(this);
        cntlPanel.add(btnPrint);
        bottomPanel.add(cntlPanel);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(topPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(bottomPanel);
        
        pack();
        barcodeTF.requestFocus();
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
	
	void print(String name)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(name);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             dlgTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			String err_mssg = "Unable to print wish history: " + e.getMessage();
			JOptionPane.showMessageDialog(this, err_mssg, "Print Wish History",
										JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("ADDED_WISH"))
		{
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == barcodeTF)
		{
			//if using UPC-E, eliminate check digits before converting to childwishID integer
			int cwID;
			if(gvs.getBarcodeCode() == Barcode.UPCE)
				cwID = Integer.parseInt(barcodeTF.getText().substring(0, barcodeTF.getText().length()-1));
			else
				cwID = Integer.parseInt(barcodeTF.getText());
			
			//get Wish History for bar code wish id. If found, notify entity listeners of
			//the Wish entity selection.
			ONCChildWish cw = cwDB.getWish(cwID);
			if(cw != null)
			{
				getWishHistory(cw);
				ONCChild child = cDB.getChild(cw.getChildID());
				if(child != null)
				{
					ONCFamily family = fDB.getFamily(child.getFamID());
					if(family != null)
					{
						fireEntitySelected(this, EntityType.WISH, family, child, cw);
						if(GlobalVariables.isUserAdmin())
							lblChildInfo.setText(String.format("Wish History for %s %s, Wish %d, Family #%s, Barcode %s",
								child.getChildFirstName(), child.getChildLastName(),
								cw.getWishNumber()+1, family.getONCNum(), barcodeTF.getText()));
						else
							lblChildInfo.setText(String.format("Wish History for %s %d,  Wish %d, Family #%s, Barcode %s",
								"Child", cDB.getChildNumber(child),
								cw.getWishNumber()+1, family.getONCNum(), barcodeTF.getText()));
					}
				}
			}
			else
			{
				stAL.clear();
				lblChildInfo.setText(String.format("Barcode %s not found", barcodeTF.getText()));
			}
			
			dlgTableModel.fireTableDataChanged();
			
			barcodeTF.removeActionListener(this);
			barcodeTF.setText("");
			barcodeTF.addActionListener(this);
			barcodeTF.requestFocus();
			
		}
		else if(e.getSource() == btnPrint)
		{
			print("ONC Wish History");
		}		
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Wish", "Details", "Ind", "Status", "Assignee", "Changed By", "Time Stamp"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return stAL.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	ONCChildWish cw = stAL.get(row);
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
        		ONCPartner partner = partnerDB.getOrganizationByID(cw.getChildWishAssigneeID());
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
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.WISH);
	}
}
