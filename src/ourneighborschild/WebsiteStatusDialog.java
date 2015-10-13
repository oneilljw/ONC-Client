package ourneighborschild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class WebsiteStatusDialog extends InfoDialog implements DatabaseListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private WebsiteStatus websiteStatus;
	private ButtonGroup onlineBG;
	private JRadioButton rbOnline, rbOffline;

	WebsiteStatusDialog(JFrame owner, boolean bModal, String[] tfNames)
	{
		super(owner, bModal, tfNames);
		
		lblONCIcon.setText("<html><font color=blue>Review/Change ONC Referrral<br>Website Status Below</font></html>");
		
		if(gvs != null)
		{
			gvs.addDatabaseListener(this);
			websiteStatus = gvs.getWebsiteStatus();
		}
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < tfNames.length; pn++)
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
		
		//add an action listeners to the time back up text field and radio buttons
		WebsiteStatusActionListener wsListener = new WebsiteStatusActionListener();
		tf[0].addActionListener(wsListener);
		rbOnline.addActionListener(wsListener);
		rbOffline.addActionListener(wsListener);
				
		//add text to action button
		btnAction.setText("Change Online Status");
						
		pack();
	}
	
	void display(WebsiteStatus ws)
	{
		websiteStatus = ws;
		
		tf[0].setText(ws.getTimeBackUp());
		if(ws.getWebsiteStatus())
			rbOnline.setSelected(true);
		else
			rbOffline.setSelected(true);
		
		btnAction.setEnabled(false);		
	}

	@Override
	void update() 
	{
		System.out.println("executing update in WSStatusDlg");
		
		WebsiteStatus updateWSReq = new WebsiteStatus(rbOnline.isSelected(), tf[0].getText());
		
		String response = gvs.updateWebsiteStatus(this, updateWSReq);
		
		if(!response.startsWith("UPDATED_WEBSITE_STATUS"))
			rbOnline.setSelected(websiteStatus.getWebsiteStatus());
		else
			websiteStatus = updateWSReq;
			
		btnAction.setEnabled(false);
	}

	@Override
	void delete()
	{
		// TODO Auto-generated method stub
	}

	@Override
	boolean fieldUnchanged()
	{
		if(rbOnline.isSelected() && !websiteStatus.getWebsiteStatus() && tf[0].equals("Online") || 
		    rbOffline.isSelected() && websiteStatus.getWebsiteStatus() && !tf[0].getText().isEmpty() && !tf[0].getText().equals("Online"))
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
			WebsiteStatus updatedWebsiteStatus= (WebsiteStatus) dbe.getObject();
			
			if(this.isVisible() && 
					(updatedWebsiteStatus.getWebsiteStatus() != websiteStatus.getWebsiteStatus() ||
					 !updatedWebsiteStatus.getTimeBackUp().equals(websiteStatus.getTimeBackUp())))
				display(updatedWebsiteStatus);
		}
	}
	
	void checkApplyChangesEnabled()
	{
		
		if(rbOnline.isSelected() == websiteStatus.getWebsiteStatus())
			btnAction.setEnabled(false);
		else if(rbOnline.isSelected() && tf[0].getText().equals("Online"))
			btnAction.setEnabled(true);
		else if(rbOffline.isSelected() && !tf[0].getText().isEmpty() && !tf[0].getText().equals("Online"))
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
}
