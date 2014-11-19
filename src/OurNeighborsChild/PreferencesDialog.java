package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import com.toedter.calendar.JDateChooser;

public class PreferencesDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * This class implements the preferences dialog for ONC
	 */
	private static final long serialVersionUID = 1L;
	private static final String ONC_NUMBER_START = "100";
	private GlobalVariables pdGVs;
	private JLabel lblMssg;
	private JDateChooser dc_today, dc_delivery, dc_seasonstart;
	private JTextArea oncWarehouseAddressTA;
	private JTextField startONCNumTF;
	public JComboBox oncFontSizeCB, ytyGrwthPctCB;
	private JButton btnOK;
	private boolean bIgnoreDialogEvents;
	
	PreferencesDialog(JFrame parentFrame)
	{
		super(parentFrame, true);
		pdGVs = GlobalVariables.getInstance();
		this.setTitle("Our Neighbor's Child Preferences");
		
		bIgnoreDialogEvents = false;
		
		JPanel p1 = new JPanel();
//		p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
		p1.setLayout(new GridLayout(4,2));
		JPanel cntlPanel = new JPanel();
		
		String mssg ="<html><b><FONT COLOR=BLUE>Set Preferences, then click OK</FONT></b></html>";		
		lblMssg = new JLabel(mssg, JLabel.CENTER);
		p1.add(lblMssg);
		
		Dimension dateSize = new Dimension(144, 56);
		dc_today = new JDateChooser(pdGVs.getTodaysDate());
		dc_today.setPreferredSize(dateSize);
		dc_today.setBorder(BorderFactory.createTitledBorder("Today's Date"));
		dc_today.setEnabled(false);
		dc_today.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() 
		{
			@Override
			public void propertyChange(PropertyChangeEvent pce)
			{
				if("date".equals(pce.getPropertyName()))
				{
					pdGVs.setTodaysDate((dc_today.getDate()));
				}					
			}
		});
		p1.add(dc_today);
		
		dc_seasonstart = new JDateChooser(pdGVs.getSeasonStartDate());
		dc_seasonstart.setPreferredSize(dateSize);
		dc_seasonstart.setBorder(BorderFactory.createTitledBorder("Season Start Date"));
		dc_seasonstart.setEnabled(false);
		dc_seasonstart.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() 
		{
			@Override
			public void propertyChange(PropertyChangeEvent pce)
			{
				if(!bIgnoreDialogEvents && "date".equals(pce.getPropertyName()))
				{
//					pdGVs.setSeasonStartDate(dc_seasonstart.getDate());
					update();
				}				
			}
		});
		p1.add(dc_seasonstart);
		
		dc_delivery = new JDateChooser(pdGVs.getDeliveryDate());
		dc_delivery.setPreferredSize(dateSize);
		dc_delivery.setBorder(BorderFactory.createTitledBorder("Delivery Date"));
		dc_delivery.setEnabled(false);
		dc_delivery.getDateEditor().addPropertyChangeListener(new PropertyChangeListener() 
		{
			@Override
			public void propertyChange(PropertyChangeEvent pce)
			{
				if(!bIgnoreDialogEvents && "date".equals(pce.getPropertyName()))
				{
//					pdGVs.setDeliveryDate(dc_delivery.getDate());
					update();
				}					
			}
		});
		p1.add(dc_delivery);
		
		oncWarehouseAddressTA = new JTextArea();
		oncWarehouseAddressTA.setText(pdGVs.getWarehouseAddress());
		oncWarehouseAddressTA.setBorder(BorderFactory.createTitledBorder("Warehouse Address"));
		oncWarehouseAddressTA.setEnabled(false);
//		oncWarehouseAddressTA.addActionListener(this);
		p1.add(oncWarehouseAddressTA);
		
		oncFontSizeCB = new JComboBox(pdGVs.getFontSizes());
		oncFontSizeCB.setSelectedIndex(pdGVs.getFontIndex());
		oncFontSizeCB.setBorder(BorderFactory.createTitledBorder("Font Size"));
		p1.add(oncFontSizeCB);
		
		startONCNumTF = new JTextField(ONC_NUMBER_START);
		startONCNumTF.setHorizontalAlignment(JTextField.CENTER);
		startONCNumTF.setBorder(BorderFactory.createTitledBorder("Starting ONC #"));
		startONCNumTF.setEnabled(false);
//		startONCNumTF.addActionListener(this);
		p1.add(startONCNumTF);
								
		ytyGrwthPctCB = new JComboBox(pdGVs.getGrwthPcts());
		ytyGrwthPctCB.setBorder(BorderFactory.createTitledBorder("YTY % Growth Allowance"));
		ytyGrwthPctCB.setSelectedIndex(2); 	//Set default to middle of range
		ytyGrwthPctCB.setEnabled(false);
//		ytyGrwthPctCB.addActionListener(this);
		p1.add(ytyGrwthPctCB);
				
		btnOK = new JButton("OK");
		btnOK.addActionListener(this);
		cntlPanel.add(btnOK);
		
		//Add the components to the frame pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().add(p1);
        this.getContentPane().add(cntlPanel);
              
        this.setResizable(false);     
        this.pack();
        btnOK.requestFocusInWindow();
	}
	
	void updateData()	//Update gui with preference data changes (called when an saved ONC object loaded)
	{
		bIgnoreDialogEvents = true;
		
		dc_today.setDate(pdGVs.getTodaysDate());
		dc_delivery.setDate(pdGVs.getDeliveryDate());
		dc_seasonstart.setDate(pdGVs.getSeasonStartDate());
		oncWarehouseAddressTA.setText(pdGVs.getWarehouseAddress());
		oncFontSizeCB.setSelectedIndex(pdGVs.getFontIndex());
		ytyGrwthPctCB.setSelectedIndex(pdGVs.getYTYGrwthIndex());
		startONCNumTF.setText(Integer.toString(pdGVs.getStartONCNum()));
		
		bIgnoreDialogEvents = false;
	}
	
	//update the server if serverGVs have changed
	void update()
	{
		int cf = 0;
		if(!pdGVs.getSeasonStartDate().equals(dc_seasonstart.getDate())) { cf |= 1;}
		if(!pdGVs.getDeliveryDate().equals(dc_delivery.getDate())) { cf |= 2; }
		if(!pdGVs.getWarehouseAddress().equals(oncWarehouseAddressTA.getText())) {cf |= 4;}
		
		if(cf > 0)
		{
			ServerGVs updateGVreq = new ServerGVs(dc_delivery.getDate(), 
													dc_seasonstart.getDate(), 
														oncWarehouseAddressTA.getText());
			
			String response = pdGVs.update(this, updateGVreq);
			if(!response.startsWith("UPDATED_GLOBALS"))
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(pdGVs.getFrame(), "ONC Server denied global update," +
						"try again later","Global Update Failed",  
						JOptionPane.ERROR_MESSAGE, pdGVs.getImageIcon(0));
			}
			else
			{
				updateData();
			}
		}
	}
	
	void setEnabledDateToday(boolean tf) { dc_today.setEnabled(tf); }
	void setEnabledRestrictedPrefrences(boolean tf)
	{
		dc_delivery.setEnabled(tf);
		dc_seasonstart.setEnabled(tf);
		oncWarehouseAddressTA.setEnabled(tf);
		ytyGrwthPctCB.setEnabled(tf);
		startONCNumTF.setEnabled(tf);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!bIgnoreDialogEvents && e.getSource()==btnOK)
		{
//			pdGVs.setWarehouseAddress(oncWarehouseAddressTA.getText());
			pdGVs.setYTYGrowthIndex(ytyGrwthPctCB.getSelectedIndex());
			pdGVs.setStartONCNum(Integer.parseInt(startONCNumTF.getText()));
			update();
			this.setVisible(false);
			this.dispose();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}	
}
