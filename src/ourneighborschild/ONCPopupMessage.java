package ourneighborschild;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ONCPopupMessage extends ONCPopup
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel lblMssg;
	
	ONCPopupMessage(ImageIcon headingIcon)
	{
		super(headingIcon, 5);	
		lblMssg = new JLabel();
		this.add(lblMssg, constraints);
	}
	
	@Override
	void show(String header, String message)
	{
		lblHeader.setText(header);
		lblMssg.setText("<html>" + message + "</html>");
		this.makeVisible();
	}
}
