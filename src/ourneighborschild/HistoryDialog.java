package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

public abstract class HistoryDialog extends JDialog implements ActionListener, EntitySelectionListener,
																DatabaseListener
{
	/**
	 * Blueprint for ONC dialogs that display history of an ONC Entity for a family or child, 
	 * such as deliveries, meals, or wishes.
	 */
	private static final long serialVersionUID = 1L;
	
	protected ONCTable dlgTable;
	protected JButton btnDelete, btnPrint;
	
	protected ONCFamily currFam;
	
	private String type;

	public HistoryDialog(JFrame pf, String type) 
	{
		super(pf);
		this.type = type;
		
		//create the history table
		dlgTable = new ONCTable(createTableModel(), getColumnToolTips(), new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		dlgTable.getSelectionModel().addListSelectionListener(this);
		
//		TableColumn holidayColumn = dlgTable.getColumnModel().getColumn(HOLIDAY_COL);
//		JComboBox comboBox = new JComboBox(MealType.getSelectionList());
//		holidayColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = getColumnWidths();
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
//      dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify columns
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.LEFT);
//      dlgTable.getColumnModel().getColumn(PARTNER_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 96));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        btnPrint = new JButton(String.format("Print %s History", type));
        btnPrint.setToolTipText(String.format("Print the %s history", type.toLowerCase()));
        btnPrint.addActionListener(this);
        
        btnDelete = new JButton(String.format("Delete Family %s", type));
        btnDelete.setToolTipText(String.format("Remove %s request from family", type.toLowerCase()));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(this);
          
        cntlPanel.add(btnDelete);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
  //    this.setMinimumSize(new Dimension(tablewidth, 240));
	}
	
	abstract AbstractTableModel createTableModel();
	
	abstract int[] getColumnWidths();
	
	abstract String[] getColumnToolTips();
	
	abstract void display(ONCObject obj);
	
	abstract void delete();
	
	void setDialogTitle()
	{
		if(GlobalVariables.isUserAdmin())
			this.setTitle(String.format("%s Family %s History", currFam.getHOHLastName(), type)); 
		else
			this.setTitle(String.format("ONC# %s Family %s History", currFam.getONCNum(), type));
	}
	
	void onPrintListing(String tablename)
	{
		try
		{
			MessageFormat headerFormat = new MessageFormat(tablename);
			MessageFormat footerFormat = new MessageFormat("- {0} -");
			dlgTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage(), "Print Failed",
					JOptionPane.ERROR_MESSAGE, GlobalVariables.getInstance().getImageIcon(0));
				e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnPrint)
			onPrintListing(String.format("%s History for ONC Family #%s", type, currFam.getONCNum()));
		else if(e.getSource() == btnDelete)
			delete();
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && (tse.getType().equals("FAMILY_SELECTED") || 
				tse.getType().equals("WISH_SELECTED")))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			if(fam != null)
			{
				display(fam);	
			}	
		}
	}
}