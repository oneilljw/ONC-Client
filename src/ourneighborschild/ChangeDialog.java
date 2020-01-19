package ourneighborschild;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class ChangeDialog extends SortTableDialog
{
	/**
	 * Hierarchy class for dialogs that implement a panel that allows changes of pojo 
	 * fields on a multiple object basis from a table
	 */
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_TABLE_ROW_COUNT = 15;
	protected JPanel changePanel;
	protected JPanel itemCountPanel, changeDataPanel;
	protected JLabel lblNumOfTableItems;
	protected  GridBagConstraints gbc;
	
	public ChangeDialog(JFrame pf)
	{
		super(pf, DEFAULT_TABLE_ROW_COUNT);
		
		//Set up the third panel holding count panel and change panel using a GridBag layout
        changePanel = new JPanel();
        changePanel.setLayout( new GridBagLayout() );
        
        itemCountPanel = new JPanel();       
        lblNumOfTableItems = new JLabel();	//subclasses set text for lable
        itemCountPanel.add(lblNumOfTableItems);
        itemCountPanel.setBorder(BorderFactory.createTitledBorder("Families Meeting Criteria"));
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 120;
        gbc.weightx = 0.1;
        changePanel.add(itemCountPanel, gbc);
        
        changeDataPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	}
}
