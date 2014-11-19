package OurNeighborsChild;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class EmailStatusDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextPane statusTP;
	
	public EmailStatusDialog(JDialog parent, GlobalVariables gvs, String title)
	{
		super(parent);
		this.setTitle(title);
		
		//Set up the Email Status Text Pane
		statusTP = new JTextPane();
        statusTP.setToolTipText("EMail Status");
        SimpleAttributeSet attribs = new SimpleAttributeSet(); 
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, gvs.getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        statusTP.setParagraphAttributes(attribs,true);             
	   	statusTP.setEditable(false);
	   	statusTP.setPreferredSize(new Dimension(600, 160));
	   	statusTP.setBorder(BorderFactory.createTitledBorder(title));
	   	
	   	//Create the Status scroll pane and add the Status text pane to it.
        JScrollPane statusscrollPane = new JScrollPane(statusTP, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
																 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        //Add the scroll pane to the dialog content pane
        this.getContentPane().add(statusscrollPane);
        
        pack();
        
	}
	
	void addEmailStatus(String status)
	{
		String oldStatus = statusTP.getText();
		String newStatus = oldStatus + "\n" + status;
		statusTP.setText(newStatus);
	}
	
	void clearData()
	{
		statusTP.setText("");
	}
}
