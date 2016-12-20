package ourneighborschild;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.toedter.calendar.JDateChooser;

public class PreferencesDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This class implements the preferences dialog for ONC
	 */
	private static final long serialVersionUID = 1L;
	private static final Integer DEFAULT_FONT_SIZE = 13;
	
	private GlobalVariables pdGVs;
	private UserDB userDB;
	
	private UserPreferences uPrefs;
	
	private JDateChooser dc_today, dc_delivery, dc_seasonstart, dc_giftsreceived;
	private JDateChooser dc_DecemberCutoff, dc_InfoEditCutoff, dc_ThanksgivingCutoff;
	private JTextField whStreetNumTF, whStreetTF, whCityTF, whStateTF;
	private String whStreetNum, whStreet,whCity, whState;
	public JComboBox oncFontSizeCB;
	private JButton btnApplyDateChanges, btnApplyAddressChanges;
	private boolean bIgnoreDialogEvents;
	private JCheckBox barcodeCkBox;
	private JComboBox barcodeCB, wishAssigneeFilterDefaultCB, fdnsFilterDefaultCB;
	private JSpinner averyXOffsetSpinner, averyYOffsetSpinner;
	private Integer[] fontSizes = {8, 10, 12, 13, 14, 16, 18};
	
	PreferencesDialog(JFrame parentFrame)
	{
		super(parentFrame, false);
		this.setTitle("Our Neighbor's Child Elf & Season Settings");
		
		pdGVs = GlobalVariables.getInstance();
		if(pdGVs != null)
			pdGVs.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		bIgnoreDialogEvents = false;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//set up personal elf settings tab
		JPanel elfSettingsPanel = new JPanel();
		elfSettingsPanel.setLayout(new BoxLayout(elfSettingsPanel, BoxLayout.Y_AXIS));
		
		JPanel fontPanel = new JPanel();
		JLabel lblFont = new JLabel("Font Size:");
		oncFontSizeCB = new JComboBox(fontSizes);
		oncFontSizeCB.setSelectedItem(DEFAULT_FONT_SIZE);
		oncFontSizeCB.setEnabled(false);
		oncFontSizeCB.addActionListener(this);
		fontPanel.add(lblFont);
		fontPanel.add(oncFontSizeCB);
		
		JPanel wafdPanel = new JPanel();
		JLabel lblWAFD = new JLabel("Wish Assignee Filter Default:");
		String[] choices = {"Any", "Unassigned"};
		wishAssigneeFilterDefaultCB = new JComboBox(choices);
		wishAssigneeFilterDefaultCB.setSelectedIndex(0);
		wishAssigneeFilterDefaultCB.setEnabled(false);
		wishAssigneeFilterDefaultCB.addActionListener(this);
		wafdPanel.add(lblWAFD);
		wafdPanel.add(wishAssigneeFilterDefaultCB);
		
		JPanel fdfPanel = new JPanel();
		JLabel lblFDF = new JLabel("Family Do Not Serve Filter Default:");
		String[] options = {"None", "Any"};
		fdnsFilterDefaultCB = new JComboBox(options);
		fdnsFilterDefaultCB.setSelectedIndex(0);
		fdnsFilterDefaultCB.setEnabled(false);
		fdnsFilterDefaultCB.addActionListener(this);
		fdfPanel.add(lblFDF);
		fdfPanel.add(fdnsFilterDefaultCB);
		
		elfSettingsPanel.add(fontPanel);
		elfSettingsPanel.add(wafdPanel);
		elfSettingsPanel.add(fdfPanel);
		
		tabbedPane.addTab("Elf Filters/Font", elfSettingsPanel);
		
		//set up the date tab
		JPanel dateTab = new JPanel();
		dateTab.setLayout(new BoxLayout(dateTab, BoxLayout.Y_AXIS));
		
		JPanel datePanel = new JPanel();
		datePanel.setLayout(new GridLayout(3,3));
		DateChangeListener dcl = new DateChangeListener();	//listener for all date changes
		
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
		
		btnApplyDateChanges = new JButton("Apply Date Changes");
		btnApplyDateChanges.setEnabled(false);
		btnApplyDateChanges.addActionListener(this);
		
		dateTab.add(datePanel);
		dateTab.add(btnApplyDateChanges);
		
		tabbedPane.addTab("Season Dates", dateTab);
		
		//set up the address tab
		JPanel addressTab = new JPanel();
		addressTab.setLayout(new BoxLayout(addressTab, BoxLayout.Y_AXIS));
		addressTab.setBorder(BorderFactory.createTitledBorder("Warehouse Address:"));
		
		JPanel addressPanel = new JPanel();
		AddressKeyListener akl = new AddressKeyListener();	//address key listener
		
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
		
		JPanel addressCntlPanel = new JPanel();
		btnApplyAddressChanges = new JButton("Apply Address Changes");
		btnApplyAddressChanges.setEnabled(false);
		btnApplyAddressChanges.addActionListener(this);
		addressCntlPanel.add(btnApplyAddressChanges);
		
		addressTab.add(addressPanel);
		addressTab.add(addressCntlPanel);
		
		tabbedPane.addTab("Warehouse", addressTab);
		
		//set up the ornament label tab
		JPanel wishlabelPanel = new JPanel();
		wishlabelPanel.setLayout(new BoxLayout(wishlabelPanel, BoxLayout.Y_AXIS));
		JPanel wishlabelPanelTop = new JPanel();
		JPanel wishlabelPanelBottom = new JPanel();
		
		barcodeCkBox = new JCheckBox("Use barcode instead of icon on label using barcode:");
		barcodeCkBox.setSelected(pdGVs.includeBarcodeOnLabels());
		barcodeCkBox.addActionListener(this);
		wishlabelPanelTop.add(barcodeCkBox);
		
		barcodeCB = new JComboBox(Barcode.values());
		barcodeCB.setSelectedItem(pdGVs.getBarcodeCode());
		barcodeCB.addActionListener(this);;
		wishlabelPanelTop.add(barcodeCB);
		
		Point lblOffset = pdGVs.getAveryLabelOffset();
		averyXOffsetSpinner = new JSpinner(new SpinnerNumberModel(lblOffset.x, 0, 100, 1));
		averyYOffsetSpinner = new JSpinner(new SpinnerNumberModel(lblOffset.y, 0, 100, 1));
		
		SpinnerChangeListener listener = new SpinnerChangeListener();
		averyXOffsetSpinner.addChangeListener(listener);
		averyYOffsetSpinner.addChangeListener(listener);
		
		wishlabelPanelBottom.add(new JLabel("Label X Offset:"));
		wishlabelPanelBottom.add(averyXOffsetSpinner);
		wishlabelPanelBottom.add(new JLabel("Label Y Offset:"));
		wishlabelPanelBottom.add(averyYOffsetSpinner);
		
		wishlabelPanel.add(wishlabelPanelTop);
		wishlabelPanel.add(wishlabelPanelBottom);
		
		tabbedPane.addTab("Ornament Labels", wishlabelPanel);
		
		//Add the components to the frame pane
        this.getContentPane().add(tabbedPane);
               
        this.pack();
        this.setMinimumSize(new Dimension(520, 200));
        btnApplyDateChanges.requestFocusInWindow();
	}
	
	void display(UserPreferences uPrefs)	//Update gui with preference data changes (called when an saved ONC object loaded)
	{
		if(uPrefs != null)
			this.uPrefs = uPrefs;
		
		bIgnoreDialogEvents = true;
		
		dc_today.setDate(pdGVs.getTodaysDate());
		dc_delivery.setDate(pdGVs.getDeliveryDate());
		dc_seasonstart.setDate(pdGVs.getSeasonStartDate());
		dc_giftsreceived.setDate(pdGVs.getGiftsReceivedDate());
		dc_ThanksgivingCutoff.setDate(pdGVs.getThanksgivingDeadline());
		dc_DecemberCutoff.setDate(pdGVs.getDecemberDeadline());
		dc_InfoEditCutoff.setDate(pdGVs.getFamilyEditDeadline());
		
		displayWarehouseAddress();
		
		ONCUser user = userDB.getLoggedInUser();
		if(user != null)
		{
//			System.out.println(String.format("PrefDlg.display: font: %d, wishIndex: %d, dnsIndex: %d",
//					user.getPreferences().getFontSize(),
//					user.getPreferences().getWishAssigneeFilter(),
//					user.getPreferences().getFamilyDNSFilter()));
			
			oncFontSizeCB.setSelectedItem(user.getPreferences().getFontSize());
			wishAssigneeFilterDefaultCB.setSelectedIndex(user.getPreferences().getWishAssigneeFilter());
			fdnsFilterDefaultCB.setSelectedIndex(user.getPreferences().getFamilyDNSFilter());
		}
		else
		{
			oncFontSizeCB.setSelectedItem(DEFAULT_FONT_SIZE);
			wishAssigneeFilterDefaultCB.setSelectedIndex(0);
			fdnsFilterDefaultCB.setSelectedIndex(0);
		}
		
		barcodeCkBox.setSelected(pdGVs.includeBarcodeOnLabels());
		
		btnApplyDateChanges.setEnabled(false);
		
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
				display(uPrefs);
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
			!pdGVs.getFamilyEditDeadline().equals(dc_InfoEditCutoff.getDate()))
		{
			btnApplyDateChanges.setEnabled(true);
		}
		else
			btnApplyDateChanges.setEnabled(false);
		
		if(!whStreetNum.equals(whStreetNumTF.getText()) ||
			!whStreet.equals(whStreetTF.getText()) ||
			!whCity.equals(whCityTF.getText()) ||
			!whState.equals(whStateTF.getText()))
		{
			btnApplyAddressChanges.setEnabled(true);
		}
		else
			btnApplyAddressChanges.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!bIgnoreDialogEvents && e.getSource()==btnApplyDateChanges)
			update();
		else if(!bIgnoreDialogEvents && e.getSource()==btnApplyAddressChanges)
			update();
		else if(!bIgnoreDialogEvents && e.getSource() == oncFontSizeCB && 
				userDB.getLoggedInUser().getPreferences().getFontSize() != (Integer) oncFontSizeCB.getSelectedItem())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource() == barcodeCkBox)
			pdGVs.setIncludeBarcodeOnLabels(barcodeCkBox.isSelected());
		else if(!bIgnoreDialogEvents && e.getSource() == barcodeCB && pdGVs.getBarcodeCode() != barcodeCB.getSelectedItem())
			pdGVs.setBarcode( (Barcode) barcodeCB.getSelectedItem());
		else if(!bIgnoreDialogEvents && e.getSource() == wishAssigneeFilterDefaultCB &&
				userDB.getLoggedInUser().getPreferences().getWishAssigneeFilter() != wishAssigneeFilterDefaultCB.getSelectedIndex())
		{
			updateUserPreferences();
		}
		else if(!bIgnoreDialogEvents && e.getSource() == fdnsFilterDefaultCB &&
				userDB.getUserPreferences().getFamilyDNSFilter() != fdnsFilterDefaultCB.getSelectedIndex())
		{
			updateUserPreferences();
		}
	}
	
	void updateUserPreferences()
	{
		ONCUser updateUserReq = new ONCUser(userDB.getLoggedInUser());
		UserPreferences userPrefs = updateUserReq.getPreferences();
		
		userPrefs.setFontSize((Integer)oncFontSizeCB.getSelectedItem());
		userPrefs.setWishAssigneeFilter(wishAssigneeFilterDefaultCB.getSelectedIndex());
		userPrefs.setFamilyDNSFilter(fdnsFilterDefaultCB.getSelectedIndex());
		
		String response = UserDB.getInstance().update(this, updateUserReq);
		if(!response.equals("UDATED_USER"))
		{
			//we have a failure, display the original preferences
			display(uPrefs);
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_GLOBALS"))
		{
			display(uPrefs);
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") || 
				dbe.getType().equals("CHANGED_USER")))	
		{
			//verify one of the user preferences has changed for the current user
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			if(updatedUser != null && userDB.getLoggedInUser().getID() == updatedUser.getID() &&
				(updatedUser.getPreferences().getWishAssigneeFilter() != wishAssigneeFilterDefaultCB.getSelectedIndex() ||
				 updatedUser.getPreferences().getFamilyDNSFilter() != fdnsFilterDefaultCB.getSelectedIndex()) ||
				 updatedUser.getPreferences().getFontSize() != (Integer)oncFontSizeCB.getSelectedItem())
			{
				display(updatedUser.getPreferences());
			}
			
			//we have a user, so enable changing preferences
			oncFontSizeCB.setEnabled(true);
			wishAssigneeFilterDefaultCB.setEnabled(true);
			fdnsFilterDefaultCB.setEnabled(true);
		}
	} 
	
	private class DateChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent pce)
		{
			if(!bIgnoreDialogEvents && "date".equals(pce.getPropertyName()))
				checkApplyChangesEnabled();
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
	 
	 private class SpinnerChangeListener implements ChangeListener
	 {

		@Override
		public void stateChanged(ChangeEvent ce)
		{
			if(ce.getSource() == averyXOffsetSpinner || ce.getSource() == averyYOffsetSpinner)
			{
				Point offset = new Point((Integer)averyXOffsetSpinner.getValue(), 
										 (Integer)averyYOffsetSpinner.getValue());
				
				pdGVs.setAveryLabelOffset(offset);
			}
			
		}
	 }
}
