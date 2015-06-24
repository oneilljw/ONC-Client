package ourneighborschild;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class AddFamilyDialog extends JDialog
{
	private JTextField hohFN, hohLN;
	
	public AddFamilyDialog(JFrame parentFrame)
	{
		super(parentFrame);
		
		hohFN = new JTextField(9);
		hohFN.setBorder(BorderFactory.createTitledBorder("First Name"));
		   
		hohLN= new JTextField(11);
		hohLN.setBorder(BorderFactory.createTitledBorder("Last Name"));
	   
	    
		JTextField homePhone = new JTextField(8);
		JTextField cellPhone= new JTextField(8);
		
		JComboBox language = new JComboBox();
		
		
	}
}
