package ourneighborschild;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BatchNumDialog extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox batchCB;
	private JButton btnOK, btnCancel;
	private String batchNum;
	
	BatchNumDialog(JFrame pf, String recBN)
	{		
		super(pf, true);	//Make the dialog modal
		this.setTitle("Select Batch Number for Import");
		
		batchNum = null;	//only gets set if user clicks Ok button, else remains null
		
		JLabel lblMssg = new JLabel("Select or enter a batch number for this import:");
									
		String[] batchnums = {"B-01","B-02","B-03","B-04","B-05","B-06","B-07","B-08","B-09","B-10",
								"B-CR"};
		batchCB = new JComboBox(batchnums);
//		batchCB.setEditable(true);
		batchCB.setMaximumSize(new Dimension(96, 56));
		batchCB.setSelectedItem(recBN);
		
		btnOK = new JButton("Ok");
		btnOK.addActionListener(this);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		
		//Add the components to the frame pane
		JPanel mainPanel = new JPanel();
		mainPanel.add(lblMssg);
		mainPanel.add(batchCB);
		
		JPanel cntlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		cntlPanel.add(btnCancel);
		cntlPanel.add(btnOK);
		
    	this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));  	    	
    	this.getContentPane().add(mainPanel);
    	this.getContentPane().add(cntlPanel);
    	
    	pack();
    	setSize(400, 120);
    	setResizable(false);
    	this.setLocationRelativeTo(pf);
	}
	
	String getBatchNumberFromDlg() { return batchNum; }

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnOK)
		{
			batchNum = batchCB.getSelectedItem().toString();
			this.dispose();
		}
		else if(e.getSource() == btnCancel)
		{
			this.dispose();
		}
		
	}
}
