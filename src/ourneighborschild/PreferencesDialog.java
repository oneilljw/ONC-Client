package ourneighborschild;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

public class PreferencesDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This class implements the preferences dialog for ONC
	 */
	private static final long serialVersionUID = 1L;
	private GlobalVariables pdGVs;
//	private JLabel lblMssg;
	private JDateChooser dc_today, dc_delivery, dc_seasonstart, dc_giftsreceived;
	private JDateChooser dc_DecemberCutoff, dc_InfoEditCutoff, dc_ThanksgivingCutoff;
	private JTextField whStreetNumTF, whStreetTF, whCityTF, whStateTF;
	private String whStreetNum, whStreet,whCity, whState;
	public JComboBox oncFontSizeCB;
	private JButton btnApplyChanges;
	private boolean bIgnoreDialogEvents;
	
	PreferencesDialog(JFrame parentFrame)
	{
		super(parentFrame, true);
		pdGVs = GlobalVariables.getInstance();
		if(pdGVs != null)
			pdGVs.addDatabaseListener(this);
		this.setTitle("Our Neighbor's Child Season Settings");
		
		bIgnoreDialogEvents = false;
		
		JPanel datePanel = new JPanel();
//		p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
		datePanel.setLayout(new GridLayout(3,3));
		datePanel.setBorder(BorderFactory.createTitledBorder("ONC Season Dates:"));
		
//		String mssg ="<html><b><FONT COLOR=BLUE>Set Preferences, then click OK</FONT></b></html>";		
//		lblMssg = new JLabel(mssg, JLabel.CENTER);
//		p1.add(lblMssg);
		
		//set up a listener for all date changes
		DateChangeListener dcl = new DateChangeListener();
		
		Dimension dateSize = new Dimension(120, 56);
		dc_today = new JDateChooser(pdGVs.getTodaysDate());
		dc_today.setPreferredSize(dateSize);
		dc_today.setBorder(BorderFactory.createTitledBorder("Today's Date"));
		dc_today.setEnabled(false);
		dc_today.getDateEditor().addPropertyChangeListener(dcl); 
		datePanel.add(dc_today);
		
		dc_seasonstart = new JDateChooser(pdGVs.getSeasonStartDate());
		dc_seasonstart.setPreferredSize(dateSize);
		dc_seasonstart.setBorder(BorderFactory.createTitledBorder("Season Start Date"));
		dc_seasonstart.setEnabled(false);
		dc_seasonstart.getDateEditor().addPropertyChangeListener(dcl);	
		datePanel.add(dc_seasonstart);
		
		dc_delivery = new JDateChooser(pdGVs.getDeliveryDate());
		dc_delivery.setPreferredSize(dateSize);
		dc_delivery.setBorder(BorderFactory.createTitledBorder("Delivery Date"));
		dc_delivery.setEnabled(false);
		dc_delivery.getDateEditor().addPropertyChangeListener(dcl);
		datePanel.add(dc_delivery);
		
		dc_ThanksgivingCutoff = new JDateChooser();
		dc_ThanksgivingCutoff.setPreferredSize(dateSize);
		dc_ThanksgivingCutoff.setBorder(BorderFactory.createTitledBorder("Thanksgiving Deadline"));
		dc_ThanksgivingCutoff.setEnabled(false);
		dc_ThanksgivingCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanel.add(dc_ThanksgivingCutoff);
		
		dc_DecemberCutoff = new JDateChooser();
		dc_DecemberCutoff.setPreferredSize(dateSize);
		dc_DecemberCutoff.setBorder(BorderFactory.createTitledBorder("December Deadline"));
		dc_DecemberCutoff.setEnabled(false);
		dc_DecemberCutoff.getDateEditor().addPropertyChangeListener(dcl);
		datePanel.add(dc_DecemberCutoff);
		
		dc_InfoEditCutoff = new JDateChooser();
		dc_InfoEditCutoff.setPreferredSize(dateSize);
		dc_InfoEditCutoff.setBorder(BorderFactory.createTitledBorder("Family Update Deadline"));
		dc_InfoEditCutoff.setEnabled(false);
		dc_InfoEditCutoff.getDateEditor().addPropertyChangeListener(dcl);		
		datePanel.add(dc_InfoEditCutoff);
		
		dc_giftsreceived = new JDateChooser(pdGVs.getGiftsReceivedDate());
		dc_giftsreceived.setPreferredSize(dateSize);
		dc_giftsreceived.setToolTipText("<html>All gifts must be received from partners <b><i>BEFORE</i></b> this date</html>");
		dc_giftsreceived.setBorder(BorderFactory.createTitledBorder("Gifts Received Deadline"));
		dc_giftsreceived.setEnabled(false);
		dc_giftsreceived.getDateEditor().addPropertyChangeListener(dcl);		
		datePanel.add(dc_giftsreceived);
		
		JPanel addressPanel = new JPanel();
		addressPanel.setBorder(BorderFactory.createTitledBorder("Warehouse Address:"));
		
		//create a address key listener
		AddressKeyListener akl = new AddressKeyListener();
		
		whStreetNumTF = new JTextField(5);
		whStreetNumTF.setBorder(BorderFactory.createTitledBorder("Street #"));
		whStreetNumTF.addKeyListener(akl);
		
		whStreetTF = new JTextField(12);
		whStreetTF.setBorder(BorderFactory.createTitledBorder("Street Name"));
		whStreetTF.addKeyListener(akl);
		
		whCityTF = new JTextField(8);
		whCityTF.setBorder(BorderFactory.createTitledBorder("City"));
		whCityTF.addKeyListener(akl);
		
		whStateTF = new JTextField(3);
		whStateTF.setBorder(BorderFactory.createTitledBorder("State"));
		whStateTF.addKeyListener(akl);
		
		addressPanel.add(whStreetNumTF);
		addressPanel.add(whStreetTF);
		addressPanel.add(whCityTF);
		addressPanel.add(whStateTF);
		
		JPanel viewPanel = new JPanel();
		viewPanel.setBorder(BorderFactory.createTitledBorder("Change View Parameters:"));
		
		JLabel lblFont = new JLabel("Font Size:");
		oncFontSizeCB = new JComboBox(pdGVs.getFontSizes());
		oncFontSizeCB.setSelectedIndex(pdGVs.getFontIndex());
//		oncFontSizeCB.setBorder(BorderFactory.createTitledBorder("Font Size"));
		viewPanel.add(lblFont);
		viewPanel.add(oncFontSizeCB);
			
		JPanel cntlPanel = new JPanel();
		btnApplyChanges = new JButton("Apply Date or Address Changes");
		btnApplyChanges.setEnabled(false);
		btnApplyChanges.addActionListener(this);
		cntlPanel.add(btnApplyChanges);
		
		//Add the components to the frame pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(datePanel);
        this.getContentPane().add(addressPanel);
        this.getContentPane().add(viewPanel);
        this.getContentPane().add(cntlPanel);
               
        this.pack();
        this.setMinimumSize(new Dimension(520, 200));
        btnApplyChanges.requestFocusInWindow();
	}
	
	void display()	//Update gui with preference data changes (called when an saved ONC object loaded)
	{
		bIgnoreDialogEvents = true;
		
		dc_today.setDate(pdGVs.getTodaysDate());
		dc_delivery.setDate(pdGVs.getDeliveryDate());
		dc_seasonstart.setDate(pdGVs.getSeasonStartDate());
		dc_giftsreceived.setDate(pdGVs.getGiftsReceivedDate());
		dc_ThanksgivingCutoff.setDate(pdGVs.getThanksgivingDeadline());
		dc_DecemberCutoff.setDate(pdGVs.getDecemberDeadline());
		dc_InfoEditCutoff.setDate(pdGVs.getFamilyEditDeadline());
		oncFontSizeCB.setSelectedIndex(pdGVs.getFontIndex());
		displayWarehouseAddress();
		
		btnApplyChanges.setEnabled(false);
		
		bIgnoreDialogEvents = false;
	}
	
	/***
	 * Takes the warehouse address string in GoogleMap format and separates it into
	 * its component parts and displays in address component text fields.
	 * GoogleMap format is a continuous line separated by '+'.
	 * Example: 1000 Main Street Anywhere, USA in GoogleMap format is
	 * 1000+Main+Street+Anywhere,USA. 
	 */
	void displayWarehouseAddress()
	{
		String[] whAddressPart = pdGVs.getWarehouseAddress().split("\\+");
		if(whAddressPart.length > 2)
		{	
			int index=0;
			whStreetNum = whAddressPart[index++];
			whStreetNumTF.setText(whStreetNum);	//set street number
			
			//determine what parts belong to the street name
			StringBuffer buff = new StringBuffer(whAddressPart[index++]);
			while(index < whAddressPart.length-1)
				buff.append(" " + whAddressPart[index++]);
			whStreet = buff.toString();
			whStreetTF.setText(whStreet);
			
			//set the city and state
			String[] whAddressCityAndState = whAddressPart[whAddressPart.length-1].split(",");
			whCity = whAddressCityAndState[0];
			whCityTF.setText(whCity);
			whState = whAddressCityAndState[1];
			whStateTF.setText(whState);
		}
	}
	
	String getWarehouseAddressInGoogleMapsFormat()
	{
		StringBuffer buff = new StringBuffer(whStreetNumTF.getText().trim());
		
		String[] streetNamePart = whStreetTF.getText().split(" ");
		for(int i=0; i<streetNamePart.length; i++)
			buff.append("+" + streetNamePart[i].trim());
		
		buff.append("+" + whCityTF.getText().trim() + "," + whStateTF.getText().trim());
		
		return buff.toString();
	}
	
	//update the server if serverGVs have changed
	void update()
	{
		int cf = 0;
		if(!pdGVs.getSeasonStartDate().equals(dc_seasonstart.getDate())) { cf |= 1;}
		if(!pdGVs.getDeliveryDate().equals(dc_delivery.getDate())) { cf |= 2; }
		if(!pdGVs.getWarehouseAddress().equals(getWarehouseAddressInGoogleMapsFormat())) {cf |= 4;}
		if(!pdGVs.getGiftsReceivedDate().equals(dc_giftsreceived.getDate())) {cf |= 8;}
		if(!pdGVs.getThanksgivingDeadline().equals(dc_ThanksgivingCutoff.getDate())) {cf |= 16;}
		if(!pdGVs.getDecemberDeadline().equals(dc_DecemberCutoff.getDate())) {cf |= 32;}
		if(!pdGVs.getFamilyEditDeadline().equals(dc_InfoEditCutoff.getDate())) {cf |= 64;}
		
		if(cf > 0)
		{
			ServerGVs updateGVreq = new ServerGVs(dc_delivery.getDate(), 
													dc_seasonstart.getDate(), 
													 getWarehouseAddressInGoogleMapsFormat(),
													  dc_giftsreceived.getDate(),
													   dc_ThanksgivingCutoff.getDate(),
													    dc_DecemberCutoff.getDate(),
													     dc_InfoEditCutoff.getDate());
			
			String response = pdGVs.update(this, updateGVreq);
			if(!response.startsWith("UPDATED_GLOBALS"))
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(this, "ONC Server denied global update," +
						"try again later","Global Update Failed",  
						JOptionPane.ERROR_MESSAGE, pdGVs.getImageIcon(0));
			}
			else
			{
				display();
			}
		}
	}
	
	void setEnabledDateToday(boolean tf) { dc_today.setEnabled(tf); }
	void setEnabledRestrictedPrefrences(boolean tf)
	{
		dc_delivery.setEnabled(tf);
		dc_giftsreceived.setEnabled(tf);
		dc_seasonstart.setEnabled(tf);
		dc_ThanksgivingCutoff.setEnabled(tf);
		dc_DecemberCutoff.setEnabled(tf);
		dc_InfoEditCutoff.setEnabled(tf);
		whStreetNumTF.setEnabled(tf);
		whStreetTF.setEnabled(tf);
		whCityTF.setEnabled(tf);
		whStateTF.setEnabled(tf);
	}
	
	void checkApplyChangesEnabled()
	{
		if(!pdGVs.getSeasonStartDate().equals(dc_seasonstart.getDate()) || 
			!pdGVs.getDeliveryDate().equals(dc_delivery.getDate()) ||
			!pdGVs.getGiftsReceivedDate().equals(dc_giftsreceived.getDate()) ||
			!pdGVs.getThanksgivingDeadline().equals(dc_ThanksgivingCutoff.getDate()) ||
			!pdGVs.getDecemberDeadline().equals(dc_DecemberCutoff.getDate()) ||
			!pdGVs.getFamilyEditDeadline().equals(dc_InfoEditCutoff.getDate()) ||
			!whStreetNum.equals(whStreetNumTF.getText()) ||
			!whStreet.equals(whStreetTF.getText()) ||
			!whCity.equals(whCityTF.getText()) ||
			!whState.equals(whStateTF.getText()))
		{
			btnApplyChanges.setEnabled(true);
		}
		else
			btnApplyChanges.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!bIgnoreDialogEvents && e.getSource()==btnApplyChanges)
		{
//			pdGVs.setWarehouseAddress(oncWarehouseAddressTA.getText());
//			System.out.println(getWarehouseAddressInGoogleMapsFormat());
			update();
//			this.setVisible(false);
//			this.dispose();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			display();
		}	
	}
	
	private class DateChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(!bIgnoreDialogEvents && "date".equals(pce.getPropertyName()))
			{
//				update();
				checkApplyChangesEnabled();
			}					
		}
	}
	
	/***********************************************************************************
	 * This class implements a key listener for the  that
	 * listens a warehouse address text field to determine when it has changed. When it has
	 * changed, it calls the checkApplyChangesEnabled method
	 ***********************************************************************************/
	 protected class AddressKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent ke) 
		{
			// TODO Auto-generated method stub		
		}
		@Override
		public void keyReleased(KeyEvent ke)
		{
			checkApplyChangesEnabled();
		}
		@Override
		public void keyTyped(KeyEvent ke)
		{
			
		}
	 }
}
