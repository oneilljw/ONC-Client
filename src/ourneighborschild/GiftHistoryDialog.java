package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class GiftHistoryDialog extends JDialog
{
	/**
	 * This class provides the blueprint for the dialog that displays the history
	 * associated with each child's gift. 
	 */
	private static final long serialVersionUID = 1L;
	private JTable historyTable;
	private DefaultTableModel historyTM;

	GiftHistoryDialog(JFrame parentFrame, ArrayList<String[]> whTable, int wn, String name)
	{
		super(parentFrame, true);	//Make the dialog modal
		this.setTitle(name +"'s Gift " + Integer.toString(wn+1) + " History");
	
		historyTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			//Implement table header tool tips.
			protected String[] columnToolTips = {"Gift Category", "Detailed Gift Description",
												"# - ONC Determined Gift or * - Don't Assign Gift", 
												"GIft Life Cycle State", "Who's fulfilling?", 
												"Who Changed the Gift?", "When did gift change?"};
		
			protected JTableHeader createDefaultTableHeader()
			{
				return new JTableHeader(columnModel)
				{
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e)
					{
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnToolTips[realIndex];
					}
				};
			}
	    
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

		String[] columns = {"Gift", "Details", "Ind", "Status", "Assignee", "Changed By", "Timestamp"};
		historyTM = new DefaultTableModel(columns, 0)
		{
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		}; 
    
		historyTable.setModel(historyTM);
		historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {84, 120, 24, 84, 112, 112, 108};
		for(int i=0; i < colWidths.length; i++)
		{
			historyTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			tablewidth += colWidths[i];
		}
    
    	JTableHeader anHeader = historyTable.getTableHeader();
    	anHeader.setForeground( Color.black);
    	anHeader.setBackground( new Color(161,202,241));
 
    	//Center cell entries in column 2 -- Indicator
    	DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
    	historyTable.getColumnModel().getColumn(2).setCellRenderer(dtcr);
    	
    	//Add gifts in history from current gift revision to creation
    	int index = whTable.size() - 1; //iterator for gift history array
    	while(index >= 0)
    		historyTM.addRow(whTable.get(index--));
    	   
    	historyTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
    	historyTable.setFillsViewportHeight(true);
    
    	//Create the scroll pane and add the table to it.
    	JScrollPane wHTScrollPane = new JScrollPane(historyTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
    												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);           
    	//Add the components to the frame pane
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    	this.getContentPane().add(wHTScrollPane);
    	  
    	pack();
    	setSize(tablewidth, 120);
    	setResizable(true);
	}
}
