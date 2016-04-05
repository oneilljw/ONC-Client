package ourneighborschild;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class AltAddressDialog extends InfoDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily fam;
	private String[] origAltAdd; 
	
	AltAddressDialog(JFrame owner, boolean bModal)
	{
		super(owner, true);

		//Set dialog title and add type label info
		this.setTitle("Alternate Delivery Address");
		lblONCIcon.setText("<html><font color=blue>Enter Alternate<br>Delivery Address</font></html>");
		
		//set dialog to always be on top of owner
		this.setAlwaysOnTop(true);
		
		//initialize the variables
		origAltAdd = new String[5];
		for(int i=0; i< origAltAdd.length; i++)
			origAltAdd[i] = "";
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		btnDelete.setText("Delete");
		btnDelete.setToolTipText("Erase the family alternate delivery address");
		
		btnAction.setText("Save");
		btnAction.setToolTipText("Update the family alternate delivery address");
		
		pack();
	}
	
	void display(ONCObject obj)
	{
		fam = (ONCFamily) obj;
		
		if(fam.getSubstituteDeliveryAddress() != null && !fam.getSubstituteDeliveryAddress().isEmpty())
		{
			String[] addParts = fam.getSubstituteDeliveryAddress().split("_");
		
			if(addParts.length == 5)	//Address must have house num, street, unit city and zip to be valid
			{
				for(int i=0; i<addParts.length; i++)
				{
					origAltAdd[i] = addParts[i];
					tf[i].setText(addParts[i]);
					tf[i].setCaretPosition(0);
				}
				btnDelete.setVisible(true);
			}
		
			btnAction.setEnabled(false);
		}
	}
	
	@Override
	void update() 
	{
		//save the updated family
		ONCFamily reqFam = new ONCFamily(fam);	//make a copy for update
				
		//create the Substititue Delivery Address String
		StringBuffer altAddressSB = new StringBuffer(tf[0].getText().isEmpty() ? "None" : tf[0].getText().trim());
		for(int i=1; i<tf.length; i++)
		{
			String tfContents = tf[i].getText().isEmpty() ? "None" : tf[i].getText().trim();
			altAddressSB.append("_" + tfContents );
		}
		reqFam.setSubstituteDeliveryAddress(altAddressSB.toString());
				
		FamilyDB familyDB = FamilyDB.getInstance();
		String response = familyDB.update(this, reqFam);
				
		if(!response.startsWith("UPDATED_FAMILY"))
		{
			//display an error message that update request failed
			GlobalVariables gvs = GlobalVariables.getInstance();
			JOptionPane.showMessageDialog(this, "ONC Family Update Error: " + response +
							", try again later","Family Update Failed",  
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(fam);	
		}
		else
			this.dispose();
	}

	@Override
	void delete()
	{
		//save the updated family with empty Substitute Delivery Address
		ONCFamily reqFam = new ONCFamily(fam);	//make a copy for update
		reqFam.setSubstituteDeliveryAddress("");
						
		FamilyDB familyDB = FamilyDB.getInstance();
		String response = familyDB.update(this, reqFam);
						
		if(!response.startsWith("UPDATED_FAMILY"))
		{
			//display an error message that update request failed
			GlobalVariables gvs = GlobalVariables.getInstance();
			JOptionPane.showMessageDialog(this, "ONC Family Update Error: " + response +
									", try again later","Family Update Failed",  
									JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			display(fam);	
		}
		else
			this.dispose();
	}

	@Override
	boolean fieldUnchanged() 
	{
		//return false if it's ok to save, return true if it's not
		boolean bFieldEmpty = tf[0].getText().isEmpty() || tf[1].getText().isEmpty() ||
							 tf[3].getText().isEmpty() || tf[4].getText().isEmpty();
		
		boolean bAddressUnchanged = origAltAdd[0].equals(tf[0].getText()) &&
									origAltAdd[1].equals(tf[1].getText()) &&
									origAltAdd[2].equals(tf[2].getText()) &&
									origAltAdd[3].equals(tf[1].getText()) &&
									origAltAdd[4].equals(tf[4].getText());
		
		return bFieldEmpty || bAddressUnchanged;
	}

	@Override
	String[] getDialogFieldNames()
	{
		return new String[] {"House #", "Street", "Unit", "City", "Zip Code"};
	}
}
