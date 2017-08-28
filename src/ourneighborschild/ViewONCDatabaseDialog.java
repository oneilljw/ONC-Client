package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import au.com.bytecode.opencsv.CSVWriter;

public class ViewONCDatabaseDialog extends JDialog implements ActionListener, ListSelectionListener
{
	/**
	 * This class implements a static view of the ONC Family data base. The view
	 * is constructed at the time the dialog is created by user selection from 
	 * the menu bar. It is not updated when family data changes. 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_ACT_TABLE_ROWS = 32;
	JTable dbTable;
	DefaultTableModel dbTableModel;
	ONCFamilyReportRowBuilder rb;
	JButton btnExport, btnClearSelection;
	boolean bChangingTable = false;
	
	ViewONCDatabaseDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.setTitle("ONC Database");
		
		rb = new ONCFamilyReportRowBuilder();
	
		dbTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
			{
				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
		  		 
				if(isRowSelected(Index_row))
					comp.setBackground(comp.getBackground());
				else if (Index_row % 2 == 1)			  
					comp.setBackground(new Color(240,248,255));
				else
					comp.setBackground(Color.white);
		  
				return comp;
			}
		};
		dbTable.setGridColor(Color.DARK_GRAY);
		dbTable.setShowGrid(true);

		dbTableModel = new DefaultTableModel(rb.getFamilyReportHeader(), 0)
		{
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		}; 
    
		dbTable.setModel(dbTableModel);
		dbTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dbTable.getSelectionModel().addListSelectionListener(this);
		dbTable.setAutoCreateRowSorter(true);
		dbTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
    
    	JTableHeader anHeader = dbTable.getTableHeader();
    	anHeader.setForeground( Color.black);
    	anHeader.setBackground( new Color(161,202,241));
     
    	//Center cell entries in columns 1 
//    	DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
//    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//    	dbTable.getColumnModel().getColumn(1).setCellRenderer(dtcr);
    	
    	dbTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
//    	dbTable.setFillsViewportHeight(true);
    
    	//Create the scroll pane and add the table to it.
    	JScrollPane dbTScrollPane = new JScrollPane(dbTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
    												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	dbTScrollPane.setPreferredSize(new Dimension(774, dbTable.getRowHeight()*NUM_ACT_TABLE_ROWS));
    	
    	//create an export button in a control panel
    	JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
    	btnClearSelection = new JButton("Clear Selection");
    	btnClearSelection.setEnabled(false);
//    	btnClearSelection.setPreferredSize(new Dimension(96,28));
    	btnClearSelection.addActionListener(this);
    	
    	btnExport = new JButton("Export");
    	btnExport.setEnabled(false);
//    	btnExport.setPreferredSize(new Dimension(96,28));
    	btnExport.addActionListener(this);
    	
    	cntlPanel.add(btnClearSelection);
    	cntlPanel.add(btnExport);
    	
    	//Add the components to the frame pane
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    	this.getContentPane().add(dbTScrollPane);
    	this.getContentPane().add(cntlPanel);
    	  
    	pack();
//    	setSize(800, 600);
    	setResizable(true);
    	Point pt = parentFrame.getLocation();
    	setLocation(pt.x + 10, pt.y + 10);
	}
	
	void buildDatabase()
	{	
		FamilyDB fDB = FamilyDB.getInstance();
		
		bChangingTable = true;	//don't process table messages while being changed
		
		while (dbTableModel.getRowCount() > 0)	//Clear the Sort Table
			dbTableModel.removeRow(0);
		
		//Add rows to table
    	for(ONCFamily f:fDB.getList())
    		dbTableModel.addRow(rb.getFamilyReportCSVRowData(f));
    	
    	btnExport.setEnabled(dbTable.getRowCount() > 0);
    	bChangingTable = false;
    	
    	ColumnsAutoSizer dbTAS = new ColumnsAutoSizer();   	
    	dbTAS.sizeColumnsToFit(dbTable);
	}
	
	public class ColumnsAutoSizer 
	{
		public void sizeColumnsToFit(JTable table)	{sizeColumnsToFit(table, 5);}

	    public void sizeColumnsToFit(JTable table, int columnMargin)
	    {
	        JTableHeader tableHeader = table.getTableHeader();

	        if(tableHeader == null) {
	            // can't auto size a table without a header
	            return;
	        }

	        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

	        int[] minWidths = new int[table.getColumnCount()];
	        int[] maxWidths = new int[table.getColumnCount()];

	        for(int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
	            int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));

	            minWidths[columnIndex] = headerWidth + columnMargin;

	            int maxWidth = getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);

	            maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + columnMargin;
	        }

	        adjustMaximumWidths(table, minWidths, maxWidths);

	        for(int i = 0; i < minWidths.length; i++) {
	            if(minWidths[i] > 0) {
	                table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
	            }

	            if(maxWidths[i] > 0) {
	                table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);

	                table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
	            }
	        }
	    }

	    private void adjustMaximumWidths(JTable table, int[] minWidths, int[] maxWidths) {
	        if(table.getWidth() > 0) {
	            // to prevent infinite loops in exceptional situations
	            int breaker = 0;

	            // keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the table
	            while(sum(maxWidths) > table.getWidth() && breaker < 10000) {
	                int highestWidthIndex = findLargestIndex(maxWidths);

	                maxWidths[highestWidthIndex] -= 1;

	                maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex]);

	                breaker++;
	            }
	        }
	    }

	    private int getMaximalRequiredColumnWidth(JTable table, int columnIndex, int headerWidth) {
	        int maxWidth = headerWidth;

	        TableColumn column = table.getColumnModel().getColumn(columnIndex);

	        TableCellRenderer cellRenderer = column.getCellRenderer();

	        if(cellRenderer == null) {
	            cellRenderer = new DefaultTableCellRenderer();
	        }

	        for(int row = 0; row < table.getModel().getRowCount(); row++) {
	            Component rendererComponent = cellRenderer.getTableCellRendererComponent(table,
	                table.getModel().getValueAt(row, columnIndex),
	                false,
	                false,
	                row,
	                columnIndex);

	            double valueWidth = rendererComponent.getPreferredSize().getWidth();

	            maxWidth = (int) Math.max(maxWidth, valueWidth);
	        }

	        return maxWidth;
	    }

	    private int findLargestIndex(int[] widths) {
	        int largestIndex = 0;
	        int largestValue = 0;

	        for(int i = 0; i < widths.length; i++) {
	            if(widths[i] > largestValue) {
	                largestIndex = i;
	                largestValue = widths[i];
	            }
	        }

	        return largestIndex;
	    }

	    private int sum(int[] widths) {
	        int sum = 0;

	        for(int width : widths) {
	            sum += width;
	        }

	        return sum;
	    }
	}
	
	void onExportTableContents()
	{
		ONCFileChooser oncfc = new ONCFileChooser(this);
		File oncwritefile = oncfc.getFile("Select file for export of Family data" ,
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
				writer.writeNext(rb.getFamilyDataExportColumns());
				int exportCount = 0;
				
				String[] rowContent = new String[dbTable.getColumnCount()];
				if(dbTable.getRowCount() > 0 && dbTable.getSelectedRowCount() == 0)
				{
					//export all rows
					for(int row=0; row < dbTable.getRowCount(); row++)
					{	
						int modelRow = dbTable.convertRowIndexToModel(row);
						for(int col=0; col < dbTable.getColumnCount(); col++)
							rowContent[col] = (String) dbTableModel.getValueAt(modelRow,  col);
					
						writer.writeNext(rowContent);
						exportCount++;
					}
				}
				else if(dbTable.getSelectedRowCount() > 0)
				{
					//export selected rows
					int[] row_sel = dbTable.getSelectedRows();
					while(exportCount < row_sel.length)
					{
						int modelRow = dbTable.convertRowIndexToModel(row_sel[exportCount]);
						for(int col=0; col < dbTable.getColumnCount(); col++)
							rowContent[col] = (String) dbTableModel.getValueAt(modelRow, col);
						
						writer.writeNext(rowContent);
						exportCount++;
					}
				}
				
	    	   	writer.close();
	    	    
				JOptionPane.showMessageDialog(this, 
							Integer.toString(exportCount) + " records ucessfully exported to " + oncwritefile.getName(), 
							"Export Successful", JOptionPane.INFORMATION_MESSAGE, 
							GlobalVariablesDB.getONCLogo());
			} 
			catch (IOException x)
			{
				JOptionPane.showMessageDialog(this, "Export Failed, I/O Error: "  + x.getMessage(),  
							"Export Failed", JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
				System.err.format("IOException: %s%n", x);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnExport)
		{
			onExportTableContents();
		}
		else if(e.getSource() == btnClearSelection)
		{
			dbTable.clearSelection();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent lse) 
	{
		btnClearSelection.setEnabled(dbTable.getSelectedRowCount() > 0);
	}
}
