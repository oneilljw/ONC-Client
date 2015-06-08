package ourneighborschild;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ChoiceDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JButton btnAccept, btnReject;
	
	ChoiceDialog(JFrame parentFrame, String question)
	{
		super(parentFrame);
		this.setTitle("Chat");
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		//create the icon and question panel
		GlobalVariables gvs = GlobalVariables.getInstance();
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0), JLabel.LEFT);
		lblONCIcon.setText(question);
		toppanel.add(lblONCIcon);
		
		//Create a control panel
		JPanel cntlpanel = new JPanel();
		cntlpanel.setLayout((new FlowLayout(FlowLayout.RIGHT)));
				
		btnAccept = new JButton("Accept");
		btnReject = new JButton("Not Now");
		
		cntlpanel.add(btnReject);
		cntlpanel.add(btnAccept);
		
		contentPane.add(toppanel);
		contentPane.add(cntlpanel);
	}
}
