package OurNeighborsChild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.google.gson.Gson;

public class DriverDialog extends JDialog implements ActionListener, DatabaseListener, TableSelectionListener
{
	/**
	 * Implements a dialog to manage ONC Delivery Drivers
	 */
	private static final long serialVersionUID = 1L;
	private static final String NO_DRIVER_MSSG = "No Partners";
	
	private static DriverDB ddb;
	private DeliveryDB deliveryDB;
	private JPanel orgpanel;
	private static JLabel lblMssg;
	private JLabel lblDriverID, lblFamDel;
	private GlobalVariables ddGVs;
   
    private JTextField searchTF, firstnameTF,lastnameTF;
    private JTextField streetnumTF, streetnameTF, unitTF, cityTF, zipTF, hPhoneTF, cPhoneTF;
    private JTextField emailTF;
    private JButton btnNext, btnPrevious, btnNew, btnCancel, btnDelete;
    private JButton btnSave;
    private Stoplight sl;
    private Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
    
    private int dn;	//Index for organization array list
    private boolean bAddingNewObject;
    
	public DriverDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Delivery Partner Management");
		
		//Initialize object variables
		ddb = DriverDB.getInstance();	//Reference to the driver data base
		if(ddb != null)
			ddb.addDatabaseListener(this);
		
		deliveryDB = DeliveryDB.getInstance();
		ddGVs = GlobalVariables.getInstance();	//Reference to the one global variable object
		dn = 0;	//Set the index for the driver array list
		
		//Create a content panel for the dialog and add panel components to it.
        JPanel odContentPane = new JPanel();
        odContentPane.setLayout(new BoxLayout(odContentPane, BoxLayout.PAGE_AXIS));
        
		//Top panel
		JPanel toppanel = new JPanel();
		toppanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
    	JPanel tp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	JPanel tp2 = new JPanel(new GridLayout(2,2));
    	JPanel tp3 = new JPanel(new GridLayout(2,0));
    	JPanel tp4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
    	JLabel lblONCicon = new JLabel(ddGVs.getImageIcon(0));
    	tp1.add(lblONCicon);
    	   	
    	btnNext = new JButton("Next Driver", ddGVs.getImageIcon(2));
    	btnNext.setHorizontalTextPosition(JButton.LEFT);
    	btnNext.setToolTipText("Click to see next ONC Delivery Partner");
        btnNext.setEnabled(false);
        btnNext.addActionListener(this);
               
        btnPrevious = new JButton("Prev. Driver", ddGVs.getImageIcon(3));
        btnPrevious.setHorizontalTextPosition(JButton.RIGHT);
        btnPrevious.setToolTipText("Click to see previous ONC Delivery Partner");
        btnPrevious.setEnabled(false);
        btnPrevious.addActionListener(this);
       
        tp2.add(new JLabel());
        tp2.add(new JLabel());
        tp2.add(btnPrevious);
        tp2.add(btnNext);
        
        JPanel searchsubpanel = new JPanel();
    	JLabel lblSearch = new JLabel("Search For:");
    	
    	searchTF = new JTextField(10);
    	searchTF.setToolTipText("Type ID # or Last Name, then press <Enter> to search");
    	searchTF.addActionListener(this);
    	searchsubpanel.add(lblSearch);
    	searchsubpanel.add(searchTF);
    	tp3.add(searchsubpanel);
    	
    	JPanel mssgsubpanel = new JPanel();
    	mssgsubpanel.setLayout(new GridBagLayout());
    	lblMssg = new JLabel("ONC's Delivery Partners");
    	mssgsubpanel.add(lblMssg);
    	tp3.add(mssgsubpanel);

    	sl = new Stoplight(pf, ddb);
    	tp4.add(sl);
   	
    	toppanel.setLayout(new BoxLayout(toppanel, BoxLayout.X_AXIS));
    	toppanel.add(tp1);
        toppanel.add(tp2);
        toppanel.add(tp3);
        toppanel.add(tp4);
        
        //Set up organization panel
        orgpanel = new JPanel();
        orgpanel.setLayout(new BoxLayout(orgpanel, BoxLayout.Y_AXIS));
        pBkColor = orgpanel.getBackground();
        orgpanel.setBorder(BorderFactory.createTitledBorder("Delivery Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        lblDriverID = new JLabel(NO_DRIVER_MSSG);
        lblDriverID.setPreferredSize(new Dimension (80, 48));
        lblDriverID.setBorder(BorderFactory.createTitledBorder("Partner ID"));
        lblDriverID.setHorizontalAlignment(JLabel.RIGHT);
        
        
        DataChangeListener dcl = new DataChangeListener();
        
        firstnameTF = new JTextField(12);
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.addActionListener(dcl);
        
        lastnameTF = new JTextField(12);
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.addActionListener(dcl);
        
        lblFamDel = new JLabel("0", JLabel.RIGHT);
        lblFamDel.setPreferredSize(new Dimension (96, 48));
        lblFamDel.setToolTipText("Delieries Made");
        lblFamDel.setBorder(BorderFactory.createTitledBorder("# Deliveries"));
                
        op1.add(lblDriverID);
        op1.add(firstnameTF);
        op1.add(lastnameTF);
        op1.add(lblFamDel);
                                           
        emailTF = new JTextField(20);
        emailTF.setToolTipText("Delivery partner email address");
        emailTF.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailTF.setHorizontalAlignment(JTextField.LEFT);
        emailTF.addActionListener(dcl);
        
        hPhoneTF = new JTextField(10);
        hPhoneTF.setToolTipText("Delivery partner home phone #");
        hPhoneTF.setBorder(BorderFactory.createTitledBorder("Home Phone #"));
        hPhoneTF.addActionListener(dcl);
        
        cPhoneTF = new JTextField(10);
        cPhoneTF.setToolTipText("Delivery partner cell phone #");
        cPhoneTF.setBorder(BorderFactory.createTitledBorder(" Cell Phone #"));
        cPhoneTF.addActionListener(dcl);
        
        op2.add(emailTF);
        op2.add(hPhoneTF);
        op2.add(cPhoneTF); 
              
        streetnumTF = new JTextField(4);
        streetnumTF.setToolTipText("Address of delivery partner");
        streetnumTF.setBorder(BorderFactory.createTitledBorder("St. #"));
        streetnumTF.addActionListener(dcl);
        
        streetnameTF = new JTextField(14);
        streetnameTF.setToolTipText("Address of delivery partner");
        streetnameTF.setBorder(BorderFactory.createTitledBorder("Street"));
        streetnameTF.addActionListener(dcl);
            
        unitTF = new JTextField(7);
        unitTF.setToolTipText("Address of delivery partner");
        unitTF.setBorder(BorderFactory.createTitledBorder("Unit #"));
        unitTF.addActionListener(dcl);
        
        cityTF = new JTextField(7);
        cityTF.setToolTipText("Address of delivery partner");
        cityTF.setBorder(BorderFactory.createTitledBorder("City"));
        cityTF.addActionListener(dcl);
        
        zipTF = new JTextField(4);
        zipTF.setToolTipText("Address of deliverer");
        zipTF.setBorder(BorderFactory.createTitledBorder("Zip"));
        zipTF.addActionListener(dcl);
              
        op3.add(streetnumTF);
        op3.add(streetnameTF);
        op3.add(unitTF);
        op3.add(cityTF);
        op3.add(zipTF);
                      
        orgpanel.add(op1);
        orgpanel.add(op2);
        orgpanel.add(op3);
        
        //Set up control panel
        JPanel cntlpanel = new JPanel();
        
        btnNew = new JButton("Add New Delivery Partner");
    	btnNew.setToolTipText("Click to add a new delivery partner");
        btnNew.setEnabled(true);
        btnNew.addActionListener(this);
        
        btnDelete = new JButton("Delete Delivey Partner");
    	btnDelete.setToolTipText("Click to delete this delivery partner");
    	btnDelete.setEnabled(false);
    	btnDelete.setVisible(true);
        btnDelete.addActionListener(this);
        
        btnSave = new JButton("Save New Delivery Partner");
    	btnSave.setToolTipText("Click to save the new delivery partner");
    	btnSave.setVisible(false);
        btnSave.addActionListener(this);
        
        btnCancel = new JButton("Cancel Add New Delivery Partner");
    	btnCancel.setToolTipText("Click to cancel adding a new delivery partner");
    	btnCancel.setVisible(false);
        btnCancel.addActionListener(this);
        
        cntlpanel.add(btnNew);
        cntlpanel.add(btnDelete);
        cntlpanel.add(btnSave);
        cntlpanel.add(btnCancel);

        odContentPane.add(toppanel);
        odContentPane.add(orgpanel);
        odContentPane.add(cntlpanel);
        
        this.setContentPane(odContentPane);
        
        btnNext.requestFocus();
        
        pack();
 //     setSize(780, 220);
        setResizable(true);
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void update()
	{
		//Check to see if user has changed any field, if so, save it	
		ONCDriver d = new ONCDriver(ddb.getDriver(dn));	//make a copy of current driver
		boolean bCD = false; //used to indicate a change has been detected
		
		if(!firstnameTF.getText().equals(d.getfName()))
		{
//			System.out.println(String.format("DriverDialog updateDriver, firstname change detected: %s", d.getfName()));
			d.setfName(firstnameTF.getText());
//			System.out.println(String.format("DriverDialog updateDriver, firstname changed: %s", d.getfName()));
			bCD = true;
		}
		if(!lastnameTF.getText().equals(d.getlName())) { d.setlName(lastnameTF.getText()); bCD = true; }
		if(!hPhoneTF.getText().equals(d.getHomePhone())) { d.setHomePhone(hPhoneTF.getText()); bCD = true; }
		if(!cPhoneTF.getText().equals(d.getCellPhone())) { d.setCellPhone(cPhoneTF.getText()); bCD = true; }
		if(!emailTF.getText().equals(d.getEmail())) { d.setEmail(emailTF.getText()); bCD = true; }
		if(!streetnumTF.getText().equals(d.gethNum())) { d.sethNum(streetnumTF.getText()); bCD = true; }
		if(!streetnameTF.getText().equals(d.getStreet())) { d.setStreet(streetnameTF.getText()); bCD = true; }		
		if(!unitTF.getText().equals(d.getUnit())) { d.setUnit(unitTF.getText()); bCD = true; }
		if(!cityTF.getText().equals(d.getCity())) { d.setCity(cityTF.getText()); bCD = true; }
		if(!zipTF.getText().equals(d.getZipcode())) { d.setZipcode(zipTF.getText()); bCD = true; }
		
		if(bCD)	//If an update to organization data (not stop light data) was detected
		{
			d.setDateChanged(ddGVs.getTodaysDate());
			
			//request an update from the server
			String response = ddb.update(this, d);
			
			if(response.startsWith("UPDATED_DRIVER"))
			{
				displayDriver();
			}
			else
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(ddGVs.getFrame(), "ONC Server denied Driver Update," +
						"try again later","Driver Update Failed",  
						JOptionPane.ERROR_MESSAGE, ddGVs.getImageIcon(0));
			}
			
			bCD = false;
		}
	}
	
	void displayDriver()
	{
//		System.out.println(String.format("DriverDlg displayDriver - # of drivers: %d",
//				ddb.getNumOfDrivers()));
		
		if(ddb.getNumOfDrivers() > 0)
		{
			ONCDriver d = ddb.getDriver(dn);
			
//			System.out.println(String.format("DriverDlg displayDriver - driver last name: %s",
//					d.getlName()));
			
			lblDriverID.setText(Long.toString(d.getID()));
			firstnameTF.setText(d.getfName());
			firstnameTF.setCaretPosition(0);
			lastnameTF.setText(d.getlName());
			lastnameTF.setCaretPosition(0);
			emailTF.setText(d.getEmail());
			emailTF.setCaretPosition(0);
			hPhoneTF.setText(d.getHomePhone());
			hPhoneTF.setCaretPosition(0);
			cPhoneTF.setText(d.getCellPhone());
			cPhoneTF.setCaretPosition(0);
						
			lblFamDel.setText("0");
			
			streetnumTF.setText(d.gethNum());
			streetnameTF.setText(d.getStreet());
			unitTF.setText(d.getUnit());
			cityTF.setText(d.getCity());
			zipTF.setText(d.getZipcode());
			
			sl.setEntity(d);

			if(d.getDelAssigned() == 0)	//Can only delete if not assigned to fulfill a wish
				btnDelete.setEnabled(true);
			else
				btnDelete.setEnabled(false);
			
			btnNext.setEnabled(true);
			btnPrevious.setEnabled(true);
		}
		else
		{
			clearDisplay();
			lblDriverID.setText(NO_DRIVER_MSSG);	//If no organizations, display this message
		}
	}
	
	void clearDisplay()
	{
		lblDriverID.setText("");
		firstnameTF.setText("");
		lastnameTF.setText("");
		lblFamDel.setText("0");
		hPhoneTF.setText("");
		cPhoneTF.setText("");
		emailTF.setText("");		
		streetnumTF.setText("");
		streetnameTF.setText("");
		unitTF.setText("");
		cityTF.setText("");
		zipTF.setText("");
		sl.clear();
	}
	
	int searchForDriver(String data)
    {
    	int sn = -1; 	//-1 indicates driver not found
    	int foundcount = 0;
    	String upper1stchar = "";
    		  	
    	//If a numeric string, then search for ID match, else search for last name match
		if(!data.isEmpty() && data.matches("-?\\d+(\\.\\d+)?"))
		{
    		sn = ddb.getDriverIndex(Integer.parseInt(data));
		
    		if(sn > -1)
    			foundcount = 1;
		}
		else
		{
			if(data.length() == 1)	//Convert character to upper case for search? 
    				upper1stchar = data.toUpperCase();
			else   				
				upper1stchar = Character.toUpperCase(data.charAt(0)) + data.substring(1);
    				
    		for(int i=0; i<ddb.getNumOfDrivers(); i++)   			
    			if(ddb.getDriver(i).getlName().contains(upper1stchar))
    			{
    				sn=i;
    				foundcount++;
    			}
		}
    		    	    	
    	//If the requested driver is found, save changes, if any to currently displayed driver
    	//then set the driver index to the new driver and display that driver
    	if(!bAddingNewObject && sn >= 0)
    	{   		
    		update();
    		
    		dn = sn;   				      			
    		displayDriver();
    	}
    	
    	return foundcount;
    }

	void onSaveNewDriver()
	{
		//construct a new driver from user input	
		ONCDriver newDriver = new ONCDriver(-1, firstnameTF.getText(), lastnameTF.getText(),
				emailTF.getText(), streetnumTF.getText(), streetnameTF.getText(), 
				unitTF.getText(), cityTF.getText(), zipTF.getText(), 
				hPhoneTF.getText(), cPhoneTF.getText(), "", "", new Date(), ddGVs.getUserLNFI());
				
		//send add request to the local data base
		String response = ddb.add(this, newDriver);
				
		if(response.startsWith("ADDED_DRIVER"))
		{
			//update the ui with new id assigned by the server 
			Gson gson = new Gson();
			ONCDriver addedDriver = gson.fromJson(response.substring(12), ONCDriver.class);
					
			//set the display index, on, to the new partner added and display organization
			dn = ddb.getDriverIndex(addedDriver.getID());
		}
		else
		{
			String err_mssg = "ONC Server denied add driver request, try again later";
			JOptionPane.showMessageDialog(ddGVs.getFrame(), err_mssg, "Add Driver Request Failure",
											JOptionPane.ERROR_MESSAGE, ddGVs.getImageIcon(0));
		}
		
		btnNext.setEnabled(true);
		btnPrevious.setEnabled(true);
		searchTF.setEnabled(true);
		orgpanel.setBorder(BorderFactory.createTitledBorder("Delivery Partner Information"));
		displayDriver();
		orgpanel.setBackground(pBkColor);
		btnNew.setVisible(true);
		btnDelete.setVisible(true);
		btnSave.setVisible(false);
		btnCancel.setVisible(false);
		
		bAddingNewObject = false;
	}
	
	/***********************************************************************************************
	 * This class implements a listener for the fields that need to check for 
	 * data updates when the user presses the <Enter> key. The only action this listener takes is to
	 * call the update method which checks if the data has changed, if it has 
	 * it sends a change request to the server.
	 ***********************************************************************************************/
	private class DataChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(!bAddingNewObject)
			{
				update();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(!bAddingNewObject && (e.getSource() == btnNext || e.getSource() == btnPrevious))
		{
			//Save changes, if any to both family and child info
			if(ddGVs.isUserAdmin())	//Only admin user can make driver changes
				update();
		
			if(e.getSource() == btnNext)
			{						
				if(++dn == ddb.getNumOfDrivers())
					dn=0;
			}
			else if(e.getSource() == btnPrevious)
			{
				if(--dn< 0)
					dn = ddb.getNumOfDrivers()-1;
			}
		
			//display new organization
			displayDriver();
			
		}
		else if(e.getSource() == searchTF  && !bAddingNewObject)
		{
			//>=0:successful search, on is valid; <0:data not found
			int foundcount = searchForDriver(searchTF.getText());	
			if(foundcount > 1)  	
	    		lblMssg.setText(Integer.toString(foundcount) + " drivers with the same name found");
	    	else if(foundcount == 1)
	    		lblMssg.setText("Driver sucessfully located");
	    	else
	    		lblMssg.setText("Driver not found in database");
			
			searchTF.selectAll();			
		}
		else if(!bAddingNewObject && e.getSource() == firstnameTF && !firstnameTF.getText().equals(ddb.getDriver(dn).getfName()) ||
				!bAddingNewObject && e.getSource() == lastnameTF && !lastnameTF.getText().equals(ddb.getDriver(dn).getlName()))
		{
			ddb.getDriver(dn).setfName(firstnameTF.getText());
			ddb.getDriver(dn).setlName(lastnameTF.getText());

//				assignDeliveryDlg.buildSortTable();
		}
		else if(e.getSource() == btnNew)
		{
			bAddingNewObject = true; //Used to disable certain action listeners
			
			btnNext.setEnabled(false);
			btnPrevious.setEnabled(false);
			searchTF.setEnabled(false);
			orgpanel.setBorder(BorderFactory.createTitledBorder("Enter New Delivery Partner's Information"));
			clearDisplay();
			orgpanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
			btnNew.setVisible(false);
			btnDelete.setVisible(false);
			btnSave.setVisible(true);
			btnCancel.setVisible(true);
			
			lblDriverID.setText("New");
		}		
		else if(e.getSource() == btnCancel)
		{
			btnNext.setEnabled(true);
			btnPrevious.setEnabled(true);
			searchTF.setEnabled(true);
			orgpanel.setBorder(BorderFactory.createTitledBorder("Delivery Partner Information"));
			displayDriver();
			orgpanel.setBackground(pBkColor);
			btnNew.setVisible(true);
			btnDelete.setVisible(true);
			btnSave.setVisible(false);
			btnCancel.setVisible(false);
			
			bAddingNewObject = false;
		}
		else if(e.getSource() == btnSave)
		{
			onSaveNewDriver();	
		}
		else if(e.getSource() == btnDelete)
		{			
			//TODO: add Delete Driver processing here			
		}
	}

	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DRIVER"))
		{
			ONCDriver updatedDriver = (ONCDriver) dbe.getObject();
			
			//If current driver is being displayed has changed, re-display it
			if(Integer.parseInt(lblDriverID.getText()) == updatedDriver.getID() && !bAddingNewObject)
				displayDriver();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("ADDED_DRIVER"))
		{
			//If no driver is being displayed, display the added one
			if(lblDriverID.getText().equals(NO_DRIVER_MSSG) && ddb.getNumOfDrivers() > 0 &&
			    !bAddingNewObject)
				
				displayDriver();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_DRIVER"))
		{
			//if the deleted driver was the only driver in data base, clear the display
			//otherwise, if the deleted driver is currently being displayed, change the
			//index to the previous driver in the database and display.
			if(ddb.getNumOfDrivers() == 0)
			{
				dn = 0;
				clearDisplay();
				btnDelete.setEnabled(false);
			}
			else
			{
				ONCDriver deletedDriver = (ONCDriver) dbe.getObject();
				if(Integer.parseInt(lblDriverID.getText()) == deletedDriver.getID())
				{
					if(dn == 0)
						dn = ddb.getNumOfDrivers() - 1;
					else
						dn--;
					
					displayDriver();
				}
			}
		}
	}

	@Override
	public void tableRowSelected(ONCTableSelectionEvent tse)
	{
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			if(fam.getDeliveryID() > -1)
			{
				ONCDelivery delivery = deliveryDB.getDelivery(fam.getDeliveryID());
				
				int rtn = ddb.getDriverIndex(delivery.getdDelBy());
				if(rtn > -1)
				{
					update();
					dn = rtn;
					displayDriver();
				}
			}
		}
	}
}
