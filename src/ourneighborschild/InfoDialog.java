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
	protected JFrame owner;
	protected JPanel contentPanel;
	protected JPanel[] infopanel;
	protected JLabel[] lblTF;
	protected JTextField[] tf;
	protected JLabel lblONCIcon;
	protected JButton btnAction, btnDelete;
	protected DataChangeListener dcl;
	protected TFKeyListener tfkl;
	protected boolean result;
	protected GlobalVariables gvs;
	protected UserDB userDB;
	
	InfoDialog(JFrame pf, boolean bModal)
	{
		super(pf, bModal);
		this.owner = pf;
		
		result = false;
		
		//Set up the main panel
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		gvs = GlobalVariables.getInstance();
		userDB = UserDB.getInstance();
		
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		lblONCIcon = new JLabel(gvs.getImageIcon(0), JLabel.LEFT);
		lblONCIcon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
		toppanel.add(lblONCIcon);
		
		String[] tfNames = getDialogFieldNames();
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
		
		//Set up the main panel, loop to set up components associated with names
//		for(int pn=0; pn < getDialogFieldNames().length; pn++)
//		{
//			tf[pn] = new JTextField(12);
//			tf[pn].addKeyListener(tfkl);
//			tf[pn].setEnabled(false);
//			infopanel[pn].add(tf[pn]);
//		}
		
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
	
	abstract String[] getDialogFieldNames();
	
	abstract void display(ONCObject obj);
	
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
		else if(ae.getSource() == btnDelete)
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
