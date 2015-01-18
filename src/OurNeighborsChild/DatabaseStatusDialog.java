package OurNeighborsChild;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class DatabaseStatusDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	DatabaseStatusDialog()
	{
		super();
		this.setTitle("Database Status");
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
	}
}
