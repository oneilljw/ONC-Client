package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//public abstract class ONCConnectDialog extends JDialog implements ActionListener
public abstract class ONCConnectDialog extends JDialog implements ActionListener
{
	/*************************************************************************************
	 * This abstract class implements a dialog that communicates with the ONC Server to
	 * establish server address or authenticate user logins.
	 ************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int LOCATION_X_OFFSET = 260;
	private static final int LOCATION_Y_OFFSET = 420;
	
	protected JPanel p1, p2, p3, p4, p5;
	protected JLabel lblMssg1, lblMssg2, lblTF1, lblTF2;
	protected JTextField tf1;
	protected JButton btnAction;
	protected ServerIF serverIF;
		
	public ONCConnectDialog(final JFrame parent)
	{
		super(parent, true);
		
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				if(serverIF != null && serverIF.isConnected())
		    	{
					serverIF.sendRequest("LOGOUT");
		    		serverIF.close();
		    	}

		    	System.exit(0);
			}
		});
		
		parent.addComponentListener(new ComponentAdapter()
		{
		    public void componentMoved(ComponentEvent e)
		    {
		    	Point pt = parent.getLocation();
		        setLocation(pt.x + LOCATION_X_OFFSET, pt.y + LOCATION_Y_OFFSET);
		    }
		});
       	
       	//Get reference for Server interface and register server listener
      	serverIF = ServerIF.getInstance();
		
		//Layout GUI
		p1 = new JPanel();
		lblMssg1 = new JLabel();
		p1.add(lblMssg1);
		
		p2 = new JPanel();
		lblMssg2= new JLabel();
		p2.add(lblMssg2);
				
		p3 = new JPanel();
		lblTF1 = new JLabel();
		tf1 = new JTextField(12);
		p3.add(lblTF1);
		p3.add(tf1);
		
		p4 = new JPanel();
		lblTF2 = new JLabel();
		p4.add(lblTF2);
		
		p5 = new JPanel();		
		btnAction = new JButton();
		btnAction.addActionListener(this);    		
		p5.add(btnAction);
				
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));		
		this.getContentPane().add(p1);
		this.getContentPane().add(p2);
		this.getContentPane().add(p3);
		this.getContentPane().add(p4);
		this.getContentPane().add(p5);
/*			
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));		
		this.add(p1);
		this.add(p2);
		this.add(p3);
		this.add(p4);
		this.add(p5);
*/		
		this.getRootPane().setDefaultButton(btnAction);
			
		this.pack();
		this.setSize(344, 190);
		this.setResizable(false);
		Point pt = parent.getLocation();
		this.setLocation(pt.x + LOCATION_X_OFFSET, pt.y + LOCATION_Y_OFFSET);
	}
}
