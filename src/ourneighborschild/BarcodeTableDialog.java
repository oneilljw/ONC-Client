package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
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

public abstract class BarcodeTableDialog extends ONCTableDialog implements ActionListener, DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JPanel topPanel, cntlPanel;
	protected JTextField barcodeTF;
	protected ONCTable dlgTable;
	protected AbstractTableModel dlgTableModel;
	protected JLabel lblInfo;
	protected JButton btnAction, btnDelete, btnExport;
	private JButton btnPrint;
	protected JFrame parentFrame;
	
	protected List<? extends ONCObject> stAL;

	public BarcodeTableDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.parentFrame = parentFrame;
		
		topPanel = new JPanel(new BorderLayout());
		
		JPanel barcodePanel = new JPanel();
		barcodePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodeTF = new JTextField(9);
    	barcodeTF.setMaximumSize(new Dimension(136,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		
		barcodePanel.add(lblONCIcon);
		barcodePanel.add(barcodeTF);
		
		topPanel.add(barcodePanel, BorderLayout.WEST);
		
		//Create the table model
		dlgTableModel = getDialogTableModel();
		
		//create the table
		dlgTable = new ONCTable(dlgTableModel, getColumnToolTips(), new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = getColumnWidths();
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
        int[] cols = getLeftColumns();
        for(int i=0; i< cols.length; i++)
        	dlgTable.getColumnModel().getColumn(cols[i]).setCellRenderer(dtcr);
        
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        cols = getCenteredColumns();
        for(int i=0; i< cols.length; i++)
        	dlgTable.getColumnModel().getColumn(cols[i]).setCellRenderer(dtcr);
      
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, dlgTable.getRowHeight()*getDefaultRowCount()));
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
 
        lblInfo = new JLabel("");
        bottomPanel.add(lblInfo, BorderLayout.WEST);

        
        cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(cntlPanel, BorderLayout.CENTER);
        
        btnExport = new JButton("Export");
        btnExport.addActionListener(this);
        cntlPanel.add(btnExport);
               
        btnDelete = new JButton("Delete");
        btnDelete.setToolTipText("Delete the item from Inventory if Qty = 0");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(this);
        cntlPanel.add(btnDelete);
        
        btnAction = new JButton();
        btnAction.addActionListener(this);
        cntlPanel.add(btnAction);
        
        btnPrint = new JButton("Print");
        btnPrint.addActionListener(this);
        cntlPanel.add(btnPrint);
        
        bottomPanel.add(cntlPanel, BorderLayout.EAST);
       
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(topPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(bottomPanel);
        
        pack();
        barcodeTF.requestFocus();
	}
	
	abstract String[] getColumnToolTips();
	abstract int[] getColumnWidths(); 
	abstract int[] getCenteredColumns();
	abstract int[] getLeftColumns();
	abstract int getDefaultRowCount();
	abstract AbstractTableModel getDialogTableModel();
	abstract void onBarcodeTFEvent();
	abstract String getPrintTitle();
	abstract void onActionEvent(ActionEvent e);
	abstract void onExport();
	
	void print()
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(getPrintTitle());
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
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == barcodeTF)
		{ 
			onBarcodeTFEvent();
			barcodeTF.removeActionListener(this);
			barcodeTF.setText("");
			barcodeTF.addActionListener(this);
		}
		else if(e.getSource() == btnExport)
		{
			onExport();
		}
		else if(e.getSource() == btnPrint)
		{
			print(); 
		}
		else if(e.getSource() == btnAction || e.getSource() == btnDelete) 
		{
			onActionEvent(e);
		}
		
		barcodeTF.requestFocus();
	}
}
