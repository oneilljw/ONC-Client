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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AveryLabelPrintPosDialog extends JDialog implements  ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSpinner averyRowPosSpinner, averyColPosSpinner;
	private JButton btnOK, btnCancel;
	private Point position;
	
	AveryLabelPrintPosDialog(JDialog owner)
	{		
		super(owner, true);	//Make the dialog modal
		this.setTitle("Select Label Print Position");
		
		position = new Point(0,0);	//initialize to first position
		
		averyRowPosSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
		averyColPosSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
		
		SpinnerChangeListener listener = new SpinnerChangeListener();
		averyColPosSpinner.addChangeListener(listener);
		averyRowPosSpinner.addChangeListener(listener);
				
		btnOK = new JButton("Print");
		btnOK.addActionListener(this);
		
		btnCancel = new JButton("Cancel Print");
		btnCancel.addActionListener(this);
		
		//Add the components to the frame pane
		JPanel mainPanel = new JPanel();
		mainPanel.add(new JLabel("Rown # For Label:"));
		mainPanel.add(averyColPosSpinner);
		mainPanel.add(new JLabel("Column # for Labe:"));
		mainPanel.add(averyRowPosSpinner);
		
		JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cntlPanel.add(btnCancel);
		cntlPanel.add(btnOK);
		
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));  	    	
    	this.getContentPane().add(mainPanel);
    	this.getContentPane().add(cntlPanel);
    	
    	pack();
    	setSize(400, 120);
    	setResizable(false);
	}
	
	Point showDialog(JDialog owner)
	{
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
		
		return position;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnOK)
		{
			this.dispose();
		}
		else if(e.getSource() == btnCancel)
		{
			position = null;
			this.dispose();
		}
	}
	
	private class SpinnerChangeListener implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent ce)
		{
			if(ce.getSource() == averyColPosSpinner)
				position.x = (Integer) averyColPosSpinner.getValue(); 
			else if(ce.getSource() == averyRowPosSpinner)
				position.y = (Integer) averyRowPosSpinner.getValue(); 
		}
	}
}
