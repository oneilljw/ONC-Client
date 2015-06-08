package ourneighborschild;

import javax.swing.JFrame;

public class ServerIPDialog extends ONCConnectDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String serverAddress;
	
	ServerIPDialog(JFrame pf, String failedaddress)
	{
		super(pf);
		this.setTitle("Enter ONC Server IP Address");
		
		lblMssg1.setText("<html><b><i><font color=red>Unable to establish a connection with the " +								
						 "<br><center>ONC Server at IP Address:</center></font></i></b></html>");
		lblMssg2.setText("<html><center>" + failedaddress + "</center></html>");
		lblTF1.setText("Enter New IP Address:");
		btnAction.setText("Connect");		
	}
	
	void onServerAttempt()
	{
		//place holder for super class, should never be called
	}
	
	void onLocalAttempt()
	{
		serverAddress = tf1.getText().trim();
		this.dispose();
	}
	
	String getNewAddress() { return serverAddress; }

	@Override
	void connectedToServer() {
		// TODO Auto-generated method stub	
	}

	@Override
	void notConnectedToServer() {
		// TODO Auto-generated method stub	
	}
}
