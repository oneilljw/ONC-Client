package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LogDialog extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static StyledDocument serverLogDoc;
	private static StyledDocument messageLogDoc;
	private static SimpleAttributeSet logAttribs;
	private static JTabbedPane tabbedPane;
	private JButton btnClear, btnRefresh;
	
	LogDialog()
	{
		super();
		this.setTitle("Message Log Viewer");
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		tabbedPane = new JTabbedPane();
		tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		
		//create the text panes that displays text
		JTextPane serverLogTP = new JTextPane();
        JTextPane messageLogTP = new JTextPane();
        
        //set the styled document
        serverLogDoc = serverLogTP.getStyledDocument();
        messageLogDoc = messageLogTP.getStyledDocument();
        
        //create the log text style
        logAttribs = new SimpleAttributeSet();
        StyleConstants.setForeground(logAttribs, Color.BLACK);
        
        //create the paragraph attributes
        SimpleAttributeSet paragraphAttribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(paragraphAttribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(paragraphAttribs, 12);
        StyleConstants.setSpaceBelow(paragraphAttribs, 3);
        
        //set the paragraph attributes, disable editing and set the pane size
        serverLogTP.setParagraphAttributes(paragraphAttribs, true);
  	   	serverLogTP.setEditable(false);
  	   	serverLogTP.setPreferredSize(new Dimension(550,350));
  	   	
  	   	messageLogTP.setParagraphAttributes(paragraphAttribs, true);
	   	messageLogTP.setEditable(false);
	   	messageLogTP.setPreferredSize(new Dimension(600,350));
  	   	
        //Create the chat text scroll pane and add the Wish List text pane to it.
        JScrollPane serverLogScrollPane = new JScrollPane(serverLogTP);
        JScrollPane messageLogScrollPane = new JScrollPane(messageLogTP);
//      logScrollPane.setBorder(BorderFactory.createTitledBorder("Server Log"));
//	   	logPanel.add(logScrollPane);
  	   	
  	   //create the control panel and add the buttons
  	   	JPanel cntlPanel = new JPanel();
  	   	
  	   	btnRefresh = new JButton("Refresh");
	   	btnRefresh.addActionListener(this);
	   	btnRefresh.setEnabled(false);
	   	cntlPanel.add(btnRefresh);
  	   	
  	   	btnClear = new JButton("Clear");
  	   	btnClear.addActionListener(this);
  	   	cntlPanel.add(btnClear);
  	   	
  	   	tabbedPane.addTab("Internal", messageLogScrollPane);
  	   	tabbedPane.addTab("Server", serverLogScrollPane);
  		
  	   	contentPane.add(tabbedPane);
  	   	contentPane.add(cntlPanel);
  	   	
  	   	pack();
	}
	
	static void add(String line, String log)
	{
		if(!log.isEmpty() && ! line.isEmpty())
		{
			Calendar timestamp = Calendar.getInstance();
			String time = new SimpleDateFormat("H:mm:ss.SSS").format(timestamp.getTime());
			if(log.charAt(0) == 'M')
			{
				try {
					messageLogDoc.insertString(messageLogDoc.getLength(), time + ": " + line + "\n", logAttribs);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(log.charAt(0) == 'S')
			{
				try {
					serverLogDoc.insertString(serverLogDoc.getLength(), time + ": " + line + "\n", logAttribs);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
	

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnClear)
		{
			//get the selected tab
			int tab = tabbedPane.getSelectedIndex();
			
			try {
				
				if(tab == 0)	//only two panes
					messageLogDoc.remove(0, messageLogDoc.getLength());
				else
					serverLogDoc.remove(0, messageLogDoc.getLength());
		
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
