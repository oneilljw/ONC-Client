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

public class WishHistoryDialog extends JDialog
{
	/**
	 * This class provides the blueprint for the dialog that displays the history
	 * associated with each child's wish. 
	 */
	private static final long serialVersionUID = 1L;
	private JTable wishHistoryTable;
	private DefaultTableModel wishHistoryTableModel;

	WishHistoryDialog(JFrame parentFrame, ArrayList<String[]> whTable, int wn, String name)
	{
		super(parentFrame, true);	//Make the dialog modal
		this.setTitle(name +"'s Wish " + Integer.toString(wn+1) + " History");
	
		wishHistoryTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			//Implement table header tool tips.
			protected String[] columnToolTips = {"Wish Category", "Detailed Wish Description",
												"# - ONC Determined Wish or * - Don't Assign Wish", 
												"Wish Life Cycle State", "Who's fulfilling?", 
												"Who Changed the Wish?", "Date Wish Changed"};
		
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

		String[] columns = {"Wish", "Details", "Ind", "Status", "Assignee", "Changed By", "Time Stamp"};
		wishHistoryTableModel = new DefaultTableModel(columns, 0)
		{
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		}; 
    
		wishHistoryTable.setModel(wishHistoryTableModel);
		wishHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
   
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {84, 120, 24, 84, 112, 112, 108};
		for(int i=0; i < colWidths.length; i++)
		{
			wishHistoryTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			tablewidth += colWidths[i];
		}
    
    	JTableHeader anHeader = wishHistoryTable.getTableHeader();
    	anHeader.setForeground( Color.black);
    	anHeader.setBackground( new Color(161,202,241));
 
    	//Center cell entries in column 2 -- Indicator
    	DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
    	wishHistoryTable.getColumnModel().getColumn(2).setCellRenderer(dtcr);
    	
    	//Add wishes in history from current wish revision to creation
    	int index = whTable.size() - 1; //iterator for wish history array
    	while(index >= 0)
    		wishHistoryTableModel.addRow(whTable.get(index--));
    	   
    	wishHistoryTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
    	wishHistoryTable.setFillsViewportHeight(true);
    
    	//Create the scroll pane and add the table to it.
    	JScrollPane wHTScrollPane = new JScrollPane(wishHistoryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
    												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);           
    	//Add the components to the frame pane
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    	this.getContentPane().add(wHTScrollPane);
    	  
    	pack();
    	setSize(tablewidth, 120);
    	setResizable(true);
	}
}
