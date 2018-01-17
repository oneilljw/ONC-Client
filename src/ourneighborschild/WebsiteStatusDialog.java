package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class WebsiteStatusDialog extends InfoDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private WebsiteStatus websiteStatus;
	private ButtonGroup onlineBG, loggingBG;
	private JRadioButton rbOnline, rbOffline, rbLoggingEnabled, rbLoggingDisabled;

	WebsiteStatusDialog(JFrame owner, boolean bModal)
	{
		super(owner, bModal);
		
		lblONCIcon.setText("<html><font color=blue>Review/Change/Reload<br>ONC DMS Website Status</font></html>");
		
		if(gvs != null)
		{
			gvs.addDatabaseListener(this);
			websiteStatus = gvs.getWebsiteStatus();
		}
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}

		//set up the web site status panel
		rbOnline = new JRadioButton("Online");
		rbOffline = new JRadioButton("Offline");
		onlineBG = new ButtonGroup();
		onlineBG.add(rbOnline);
		onlineBG.add(rbOffline);
				
		infopanel[1].remove(tf[1]);
		infopanel[1].add(rbOnline);
		infopanel[1].add(rbOffline);
		
		//set up the web site status panel
		rbLoggingEnabled = new JRadioButton("Enabled");
		rbLoggingDisabled= new JRadioButton("Disabled");
		loggingBG = new ButtonGroup();
		loggingBG.add(rbLoggingEnabled);
		loggingBG.add(rbLoggingDisabled);
		
		infopanel[2].remove(tf[2]);
		infopanel[2].add(rbLoggingEnabled);
		infopanel[2].add(rbLoggingDisabled);
		
		//add an action listeners to the time back up text field and radio buttons
		WebsiteStatusActionListener wsListener = new WebsiteStatusActionListener();
		tf[0].addActionListener(wsListener);
		rbOnline.addActionListener(wsListener);
		rbOffline.addActionListener(wsListener);
		rbLoggingEnabled.addActionListener(wsListener);
		rbLoggingDisabled.addActionListener(wsListener);
				
		//add text to action and delete buttons
		btnAction.setText("Change Website Status");
		btnDelete.setText("Reload Webpages");
		btnDelete.setVisible(true);
						
		pack();
	}
	
	void display(ONCObject obj)
	{
		
		tf[0].setText(websiteStatus.getTimeBackUp());
		
		if(websiteStatus.isWebsiteOnline())
			rbOnline.setSelected(true);
		else
			rbOffline.setSelected(true);
		
		if(websiteStatus.isWebsiteLoggingEnabled())
			rbLoggingEnabled.setSelected(true);
		else
			rbLoggingDisabled.setSelected(true);
		
		btnAction.setEnabled(false);		
	}

	@Override
	void update() 
	{	
		boolean bUpdateWebsiteStatus = true;
		
		if(rbOffline.isSelected())
		{
			//Confirm with the user that they really want to take the web-site off-line
			String confirmMssg = "<html>Are you sure you want to take the<br>"
								+ "ONC Data Management website offline?</html>"; 
									
			Object[] options= {"Cancel", "Take Offline"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Take Website Offline ***");
			confirmDlg.setLocationRelativeTo(this);
			this.setAlwaysOnTop(false);
			confirmDlg.setVisible(true);

			Object selectedValue = confirmOP.getValue();
	
			//if the client user confirmed, take the website offline
			if(selectedValue == null || selectedValue.toString().equals(options[0]))
				bUpdateWebsiteStatus = false;
		}
		
		if(rbLoggingDisabled.isSelected())
		{
			//Confirm with the user that they really want to take the web-site off-line
			String confirmMssg = "<html>Are you sure you want to disable logging<br>"
								+ "for the ONC Data Management website?</html>"; 
									
			Object[] options= {"Cancel", "Turn Off Logging"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Disable Logging ***");
			confirmDlg.setLocationRelativeTo(this);
			this.setAlwaysOnTop(false);
			confirmDlg.setVisible(true);

			Object selectedValue = confirmOP.getValue();
	
			//if the client user confirmed, take the web site offline
			if(selectedValue == null || selectedValue.toString().equals(options[0]))
				bUpdateWebsiteStatus = false;
		}
		
		if(bUpdateWebsiteStatus)
		{

			WebsiteStatus updateWSReq = new WebsiteStatus(websiteStatus);
			updateWSReq.setWebsiteStatus(rbOnline.isSelected());
			updateWSReq.setTimeBackUp(tf[0].getText());
			updateWSReq.setWebsiteLogginEnabled(rbLoggingEnabled.isSelected());
			
			String response = gvs.updateWebsiteStatus(this, updateWSReq);
			
			if(response.startsWith("UPDATED_WEBSITE_STATUS"))
				websiteStatus = updateWSReq;
			
			btnAction.setEnabled(false);
		}
	}
		
	void reloadWebpages()
	{
		String response = gvs.reloadWebpages(this);
		ONCPopupMessage popup = new ONCPopupMessage(gvs.getImageIcon(0));
		Point loc = GlobalVariablesDB.getFrame().getLocationOnScreen();
		popup.setLocation(loc.x+450, loc.y+70);
		popup.show("Message from ONC Server", response);
	}

	@Override
	void delete()
	{
		//Confirm with the user that they really want to take the web-site off-line
		String confirmMssg = "<html>Are you sure you want to reload ONC<br>"
							+ "Data Management System Webpages?</html>"; 
								
		Object[] options= {"Cancel", "Reload"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
												gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Reload Webpages ***");
		confirmDlg.setLocationRelativeTo(this);
		this.setAlwaysOnTop(false);
		confirmDlg.setVisible(true);

		Object selectedValue = confirmOP.getValue();

		//if the client user confirmed, take the website offline
		if(selectedValue != null && selectedValue.toString().equals(options[1]))
			reloadWebpages();
	}

	@Override
	boolean fieldUnchanged()
	{
		if(rbOnline.isSelected() && !websiteStatus.isWebsiteOnline() && tf[0].equals("Online") || 
		    rbOffline.isSelected() && websiteStatus.isWebsiteOnline() && !tf[0].getText().isEmpty() && !tf[0].getText().equals("Online"))
		{
			return false;
		}
		else
			return true;
			
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_WEBSITE_STATUS"))
		{
			WebsiteStatus updatedWebsiteStatus= (WebsiteStatus) dbe.getObject1();
			
			//verify we need to update the dialog. It's a modal dialog and it initializes every time it's
			//instantiated, so if it's not visible, no need to update. If visible, only update if there's a
			//change.
			if(this.isVisible() && 
					(updatedWebsiteStatus.isWebsiteOnline() != websiteStatus.isWebsiteOnline() ||
					 !updatedWebsiteStatus.getTimeBackUp().equals(websiteStatus.getTimeBackUp()) ||
					  updatedWebsiteStatus.isWebsiteLoggingEnabled() != websiteStatus.isWebsiteLoggingEnabled()))
			{
				websiteStatus = updatedWebsiteStatus;
				display(null);
			}
		}
	}
	
	void checkApplyChangesEnabled()
	{
		//check enabling for online/offline and logging enabled/disabled
		if(rbOnline.isSelected() && tf[0].getText().equals("Online"))
			btnAction.setEnabled(true);
		else if(rbOffline.isSelected() && !tf[0].getText().isEmpty() && !tf[0].getText().equals("Online"))
			btnAction.setEnabled(true);
		else if(rbLoggingEnabled.isSelected() && !websiteStatus.isWebsiteLoggingEnabled())
			btnAction.setEnabled(true);
		else if(rbLoggingEnabled.isSelected() && websiteStatus.isWebsiteLoggingEnabled())
			btnAction.setEnabled(true);
		else
			btnAction.setEnabled(false);
	}
	
	private class WebsiteStatusActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == rbOnline || e.getSource() == rbOffline)
				tf[0].setText(rbOnline.isSelected() ? "Online" : "");
			
			checkApplyChangesEnabled();
		}
	}

	@Override
	String[] getDialogFieldNames() 
	{
		// TODO Auto-generated method stub
		return new String[] {"Time Back Online", "Website Status", "Website Logging"};
	}
}
