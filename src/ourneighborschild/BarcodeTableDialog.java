package ourneighborschild;

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
	protected JTextField barcodeTF;
	protected ONCTable dlgTable;
	protected AbstractTableModel dlgTableModel;
	protected JLabel lblInfo;
	private JButton btnPrint;
	
	protected List<? extends ONCObject> stAL;

	public BarcodeTableDialog(JFrame parentFrame)
	{
		super(parentFrame);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodeTF = new JTextField(9);
    	barcodeTF.setMaximumSize(new Dimension(136,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		
		topPanel.add(lblONCIcon);
		topPanel.add(barcodeTF);
		
		//Create the table model
		dlgTableModel = getDialogTableModel();
		
		//create the table
		dlgTable = new ONCTable(dlgTableModel, getColumnToolTips(), new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
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
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, dlgTable.getRowHeight()*7));
        
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lblInfo = new JLabel("");
        textPanel.add(lblInfo);
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
	
	abstract String[] getColumnToolTips();
	abstract int[] getColumnWidths(); 
	abstract int[] getCenteredColumns();
	abstract int[] getLeftColumns();
	abstract AbstractTableModel getDialogTableModel();
	abstract void onBarcodeTFEvent();
	abstract String getPrintTitle();
	
	
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
			barcodeTF.requestFocus();
		}
		else if(e.getSource() == btnPrint)
		{
			print(); 
		}		
	}
}
