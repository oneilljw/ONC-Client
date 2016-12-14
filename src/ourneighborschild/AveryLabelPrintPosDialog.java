package ourneighborschild;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class AveryLabelPrintPosDialog extends JDialog implements  ActionListener
{
	/**
	 * Queries the user for the row and column on the 5160/8160 Avery Label Sheet on
	 * which to print the single label. Returns a Point with column (x) values from 1-3
	 * and a row (y) values from 1-10. Returns (-1,-1) if the user does not close the
	 * dialog via the "Print" button.  
	 */
	private static final long serialVersionUID = 1L;
	private JSpinner averyRowPosSpinner, averyColPosSpinner;
	private JButton btnPrint, btnCancel;
	private Point position;
	
	AveryLabelPrintPosDialog(JDialog owner) 
	{		
		super(owner, true);	//modal dialog
		this.setTitle("Select Label Print Position");
		
		//initialize return value to invalid position. This value is returned if the
		//user cancels the dialog by the Cancel Button or the Window Close icon decoration
		position = new Point(-1,-1);	
		
		//there are 10 rows and 3 columns on a 5160/8160 Avery Label Sheet
		averyRowPosSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		averyColPosSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
				
		btnPrint = new JButton("Print");
		btnPrint.addActionListener(this);
		
		btnCancel = new JButton("Cancel Print");
		btnCancel.addActionListener(this);
		
		//Add the components to the frame pane
		JPanel mainPanel = new JPanel();
		mainPanel.add(new JLabel("Choose Row # For Label:"));
		mainPanel.add(averyRowPosSpinner);
		mainPanel.add(new JLabel("Column # for Label:"));
		mainPanel.add(averyColPosSpinner);
		
		JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cntlPanel.add(btnCancel);
		cntlPanel.add(btnPrint);
		
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));  	    	
    	this.getContentPane().add(mainPanel);
    	this.getContentPane().add(cntlPanel);
    	
    	pack();
    	setSize(400, 120);
    	setResizable(false);
	}
	
	/***
	 * shows the dialog relative to the owner
	 * @param owner - owner of the dialog
	 * @return - point  with the row & col selected by the user. 
	 */
	Point showDialog(JDialog owner)
	{
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
		
		return position;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnPrint)
		{
			//set the return value to the row and column #'s the user selected
			position.x = (Integer) averyColPosSpinner.getValue(); 
			position.y = (Integer) averyRowPosSpinner.getValue(); 
			this.dispose();
		}
		else if(e.getSource() == btnCancel)
			this.dispose();
	}
}
