package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public abstract class CheckDialog extends ONCTableDialog implements ActionListener, ListSelectionListener,
																		DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_ROWS_TO_DISPLAY = 10;
	private static final Integer MAXIMUM_ONC_NUMBER = 9999;
	
	protected ONCTable dupTable;
	protected DefaultTableModel dupTableModel;
	protected JCheckBox[] cbArray;
	protected JLabel lblCount;
	protected JButton btnPrint;
	protected ArrayList<DupItem> dupAL;
	protected FamilyDB fDB;
	
	protected boolean bChangingTable;
	
	protected JPanel topPanel;
	protected JPanel checkCriteriaPanel;
	
	protected String type;	//type of dup table, family or child check as examples

	public CheckDialog(JFrame parentFrame, String type, String[] columnToolTips, String[] columns,
			int[] colWidths, int[] colsCentered) 
	{
		super(parentFrame);
		this.type = type;
		this.setTitle("Our Neighbor's Child -" + type + " Database Checks");

		fDB = FamilyDB.getInstance();
		
		//Initialize Dup Table data structure
		dupAL = new ArrayList<DupItem>();
		
		//Listen for data base changes
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.setBorder(BorderFactory.createTitledBorder(type + " Data Check Criteria"));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		checkCriteriaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		topPanel.add(lblONCIcon);
		
		//add sort criteria to dialog. Criteria built by subclass
		topPanel.add(checkCriteriaPanel);

		dupTable = new ONCTable(columnToolTips, new Color(240,248,255));

        dupTableModel = new DefaultTableModel(columns, 0)
        {
        	private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
     
        
        dupTable.setModel(dupTableModel);
        dupTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
	    dupTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        
	    //Set table column widths
	    int tablewidth = 0;
	    for(int i=0; i < colWidths.length; i++)
	    {
	    	dupTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
	    	tablewidth += colWidths[i];
	    }
	  
	    tablewidth += 24; 	//Account for vertical scroll bar
       
	    //set the table header colors
        JTableHeader anHeader = dupTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //mouse listener for header click
        anHeader.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	String colName = dupTableModel.getColumnName(dupTable.columnAtPoint(e.getPoint()));
            	sortDupTable(colName);
            	displayDupTable();
            }
        });
        
        //Center cell entries for Batch # and Region
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
    	for(int i=0; i<colsCentered.length; i++)
    		dupTable.getColumnModel().getColumn(colsCentered[i]).setCellRenderer(dtcr);
    	        
        dupTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        dupTable.setFillsViewportHeight(true);
        
        dupTable.getSelectionModel().addListSelectionListener(this);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dupScrollPane = new JScrollPane(dupTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
        									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        dupScrollPane.setPreferredSize(new Dimension(tablewidth, dupTable.getRowHeight()*NUM_ROWS_TO_DISPLAY));
        
        //Create the bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS ));
        
        //Create the count panel
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblCount = new JLabel(type + " Count: 0");
        bottomPanel.add(lblCount);
        
        //Create the control panel
        JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));     
        btnPrint = new JButton("Print");
        btnPrint.setEnabled(false);
        btnPrint.addActionListener(this);
        cntlPanel.add(btnPrint);
        
        bottomPanel.add(countPanel);
        bottomPanel.add(cntlPanel);
		
		contentPane.add(topPanel);
		contentPane.add(dupScrollPane);
		contentPane.add(bottomPanel);
		
		setResizable(true);
	}
	
	abstract void sortDupTable(String colName);
	
	abstract boolean performDupCheck(boolean[] criteria);
	
	protected void buildTableList()
	{	
		dupAL.clear();
		btnPrint.setEnabled(false);
		dupTable.setRowSelectionAllowed(false);
		
		boolean[] criteria = new boolean[cbArray.length];
		for(int i=0; i< criteria.length; i++)
			criteria[i] = cbArray[i].isSelected();
    	
    	//If child comparison returns a match, sort by child 1 last name, 
		//allow table row selections and user print
    	if(performDupCheck(criteria))
    	{
    		lblCount.setText(type + " Count: " + dupAL.size());
    		dupTable.setRowSelectionAllowed(true);
    		btnPrint.setEnabled(true);
    	}
    	else
    		lblCount.setText(type + " Count: 0");
    	  	
    	displayDupTable();
	}
	
	protected void displayDupTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		while (dupTableModel.getRowCount() > 0)	//Clear the current table
			dupTableModel.removeRow(0);
			
		for(DupItem di:dupAL)	//Build the new table
			dupTableModel.addRow(di.getDupTableRow(type));

		bChangingTable = false;	
	}
	
	protected void onPrintDataCheck(String title)
	{
		try
		{
			 MessageFormat headerFormat = new MessageFormat(title);
             MessageFormat footerFormat = new MessageFormat("- {0} -");
             dupTable.print(JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat);           
		} 
		catch (PrinterException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == dupTable.getSelectionModel() &&
				!bChangingTable)
		{
			DupItem di = dupAL.get(dupTable.getSelectedRow());
			
			ONCFamily fam = di.getFamily1();
			ONCChild child = di.getChild1();
			
			fireEntitySelected(this, EntityType.FAMILY, fam, child);
			requestFocus();
		}
	}

	
	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_CHILD") ||
										dbe.getType().equals("DELETED_CHILD")))
		{
			buildTableList();
		}			
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(EntityType.FAMILY);
	}
	
	protected class DupItemONCNumComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			Integer onc1, onc2;
			
			if(!d1.getFamily1().getONCNum().isEmpty() && isNumeric(d1.getFamily1().getONCNum()))
				onc1 = Integer.parseInt(d1.getFamily1().getONCNum());
			else
				onc1 = MAXIMUM_ONC_NUMBER;
							
			if(!d2.getFamily1().getONCNum().isEmpty() && isNumeric(d2.getFamily1().getONCNum()))
				onc2 = Integer.parseInt(d2.getFamily1().getONCNum());
			else
				onc2 = MAXIMUM_ONC_NUMBER;
			
			return onc1.compareTo(onc2);
		}
	}
}
