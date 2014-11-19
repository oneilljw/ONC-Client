package OurNeighborsChild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ServerLogDialog extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextPane logTP;
	private StyledDocument logDoc;
	private SimpleAttributeSet logAttribs;
	private JButton btnClear, btnRefresh;
	private ArrayList<String> serverLog;
	
	ServerLogDialog(ArrayList<String> serverLog)
	{
		super();
		this.setModal(false);
		this.serverLog = serverLog;	//reference to the log
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		//create the text pane that displays chat text
		logTP = new JTextPane();
        logTP.setToolTipText("Displays log messages to/from ONC Server");
        
        //set the styled document
        logDoc = logTP.getStyledDocument();
        
        //create the log text style
        logAttribs = new SimpleAttributeSet();
        StyleConstants.setForeground(logAttribs, Color.BLACK);
        
        //create the paragraph attributes
        SimpleAttributeSet paragraphAttribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(paragraphAttribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(paragraphAttribs, 12);
        StyleConstants.setSpaceBelow(paragraphAttribs, 3);
        
        //set the paragraph attributes, disable editing and set the pane size
        logTP.setParagraphAttributes(paragraphAttribs, true);
  	   	logTP.setEditable(false);
  	   	logTP.setPreferredSize(new Dimension(550,350));
      
  	   	displayServerLog();
  	   	
        //Create the chat text scroll pane and add the Wish List text pane to it.
        JScrollPane logScrollPane = new JScrollPane(logTP);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Server Log"));
//	   	logPanel.add(logScrollPane);
  	   	
  	   //create the control panel and add the buttons
  	   	JPanel cntlPanel = new JPanel();
  	   	
  	   	btnRefresh = new JButton("Refresh");
	   	btnRefresh.addActionListener(this);
	   	cntlPanel.add(btnRefresh);
  	   	
  	   	btnClear = new JButton("Clear");
  	   	btnClear.addActionListener(this);
  	   	cntlPanel.add(btnClear);
  	   	
  	   	contentPane.add(logScrollPane);
  	   	contentPane.add(cntlPanel);
  	   	
  	   	pack();
	}
	
	void displayServerLog()
	{
		 //add the server log to the text pane
        for(String s : serverLog)
			try {
				logDoc.insertString(logDoc.getLength(), s + "\n", logAttribs);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnRefresh)
		{
			//clear the text pane and reset
			logTP.setText("");
			displayServerLog();	
		}
		
	}
}
