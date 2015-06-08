package ourneighborschild;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StoplightDialog extends JDialog 
{
	/**
	 * Implements a simple dialog which allows the user to change the stop light
	 * color and add a message that shows as a tool tip. Allowed values are 0-green, 
	 * 1-yellow, 2-red and 3-off. If a value out of range is received, the position is
	 * set to off.
	 */
	private static final long serialVersionUID = 1L;
	
	private JComboBox stoplightCB;
	private JTextField stoplightTF;
	private JLabel lblChangedBy;

	StoplightDialog(JFrame parentFrame, String title)
	{
		super(parentFrame, true);
		if(title == null)
			this.setTitle("Untitled");
		else
			this.setTitle(title);
		
		String[] lightPos = {"Green", "Yellow", "Red", "Off"};
		stoplightCB = new JComboBox(lightPos);
		stoplightCB.setBorder(BorderFactory.createTitledBorder("Choose Color"));
		stoplightCB.setSelectedIndex(3);
		
		stoplightTF = new JTextField("Message");
		stoplightTF.setBorder(BorderFactory.createTitledBorder("Message"));
		
		lblChangedBy = new JLabel();
		lblChangedBy.setBorder(BorderFactory.createTitledBorder("Last Changed By"));
	
		JPanel cntlPanel = new JPanel();
		JButton btnOK = new JButton("Ok");
		btnOK.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				dispose();				
			}
		});
		cntlPanel.add(btnOK);
		

		//Add components to dialog pane
		this.getContentPane().setLayout(new GridLayout(4,0));
		this.getContentPane().add(stoplightCB);
		this.getContentPane().add(stoplightTF);
		this.getContentPane().add(lblChangedBy);
		this.getContentPane().add(cntlPanel);
		
		 pack();
		 setSize(250, 230);
		 setResizable(true);
	}
	
	void setData(int pos, String mssg, String cb)
	{
		if(pos >= 0 && pos < 4)
			stoplightCB.setSelectedIndex(pos);
		else
			stoplightCB.setSelectedIndex(3);
		
		stoplightTF.setText(mssg);
		stoplightTF.setCaretPosition(0);
		
		lblChangedBy.setText(cb);
	}
	
	int getStoplightPos() { return stoplightCB.getSelectedIndex(); }
	String getStoplightMssg() { return stoplightTF.getText(); }
}
