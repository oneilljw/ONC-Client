package ourneighborschild;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class InfoDialog extends JDialog implements ActionListener
{
	/**
	 * This class implements a blueprint for the dialogs that ask for information
	 * in the ONC client application. The class is abstract and takes names of each
	 * information field. Components to add information are contained in the subclass
	 */
	private static final long serialVersionUID = 1L;
	protected JPanel[] infopanel;
	protected JLabel[] lblTF;
	protected JTextField[] tf;
	protected JLabel lblONCIcon;
	protected JButton btnAction, btnDelete;
	protected DataChangeListener dcl;
	protected TFKeyListener tfkl;
	protected boolean result;
	protected GlobalVariables gvs;
	
	InfoDialog(JFrame owner, boolean bModal, String tfNames[])
	{
		super(owner, bModal);
		
		result = false;
		
		//Set up the main panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		gvs = GlobalVariables.getInstance();
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		lblONCIcon = new JLabel(gvs.getImageIcon(0), JLabel.LEFT);
		lblONCIcon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		toppanel.add(lblONCIcon);
		
		dcl = new DataChangeListener();
		tfkl = new TFKeyListener();
		
		infopanel = new JPanel[tfNames.length];
		lblTF = new JLabel[tfNames.length];
		tf = new JTextField[tfNames.length];
		
		//loop to set up each panel. Edit components are added by the subclasses
		for(int pn=0; pn < tfNames.length; pn++)
		{
			infopanel[pn] = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			lblTF[pn] = new JLabel();
			lblTF[pn].setText(String.format("%1$10s:", tfNames[pn]));
			infopanel[pn].add(lblTF[pn]);
		}
		
		//Add control panel
		JPanel cntlpanel = new JPanel();
		cntlpanel.setLayout((new FlowLayout(FlowLayout.RIGHT)));
		
		btnDelete = new JButton();
		btnDelete.setVisible(false);
		btnDelete.addActionListener(this);
		cntlpanel.add(btnDelete);
		
		btnAction = new JButton();
		btnAction.setEnabled(false);
		btnAction.addActionListener(this);
		cntlpanel.add(btnAction);
		
		//add components to dialog
		contentPanel.add(toppanel);
		
		for(int pn=0; pn < tfNames.length; pn++)
			contentPanel.add(infopanel[pn]);
		
		contentPanel.add(cntlpanel);
		
		this.setContentPane(contentPanel);
	}
	
//	abstract void display(ONCObject obj);
	
	abstract void update();
	
	abstract void delete();
	
	abstract boolean fieldUnchanged();
	
	boolean showDialog()
	{
	    setVisible(true);
	    return result;
	}

	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if(ae.getSource() == btnAction) 
			update();
		if(ae.getSource() == btnDelete)
			delete();		
	}
	
	private class TFKeyListener implements KeyListener
    {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) 
		{
			if(fieldUnchanged())
				btnAction.setEnabled(false);
			else
				btnAction.setEnabled(true);	
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{	
			if(fieldUnchanged())
				btnAction.setEnabled(false);
			else
				btnAction.setEnabled(true);
		}
    }
	
	/***********************************************************************************************
	 * This class implements a listener for the fields in the dialog that need to check for 
	 * data updates when the user presses the <Enter> key. The only action this listener takes is to
	 * call the checkAndUpdateAgentInfo method which checks if the data has changed, if it has 
	 * it saves the new data in the local object data base 
	 ***********************************************************************************************/
	private class DataChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			update();
		}	
	}
}