package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;


public class TransportationDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily f;
	private Families familyDB;
	private JComboBox transportationCB;

	TransportationDialog(JFrame owner, boolean bModal, String[] tfNames) 
	{
		super(owner, bModal, tfNames);
		this.setTitle("Family Has Transportation?");

		lblONCIcon.setText("<html><font color=blue>Edit Family Transportation<br>Information Below</font></html>");
		
		//initialize reference to family data base
		familyDB = Families.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < tfNames.length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			tf[pn].setEnabled(false);
			infopanel[pn].add(tf[pn]);
		}
		
		//set up the transformation panel
		transportationCB = new JComboBox(Transportation.getEditChoicesList());
		transportationCB.setPreferredSize(new Dimension(158,36));
		transportationCB.addActionListener(new TransportationListener());
		infopanel[2].remove(tf[2]);
		infopanel[2].add(transportationCB);
		
		//add text to action button
		btnAction.setText("Apply Change");
				
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		
		tf[0].setText(f.getONCNum());
		tf[1].setText(f.getHOHLastName());
		transportationCB.setSelectedItem(f.getTransportation());
		
		btnAction.setEnabled(false);
		
	}

	@Override
	void update() 
	{
		ONCFamily updateFamReq = new ONCFamily(f);
		Transportation hasTransportation = (Transportation) transportationCB.getSelectedItem();
		updateFamReq.setTransportation(hasTransportation);
		
		String response = familyDB.update(this, updateFamReq);
		
		if(!response.startsWith("UPDATED_FAMILY"))
			transportationCB.setSelectedItem(f.getTransportation());
	}

	@Override
	void delete() {
		// TODO Auto-generated method stub

	}

	@Override
	boolean fieldUnchanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			if(this.isShowing())	//If Agent Info dialog visible, notify agent selection change
			{
				display((ONCFamily) tse.getObject1());	//Display newly selected agent
			}
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFamily = (ONCFamily) dbe.getObject();
			
			if(this.isVisible() && f.getID() == updatedFamily.getID())
				display(updatedFamily);
		}
	}
	
	private class TransportationListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			Transportation hasTransportation = (Transportation) transportationCB.getSelectedItem();
			if(hasTransportation == f.getTransportation())
				btnAction.setEnabled(false);
			else
				btnAction.setEnabled(true);
		}
		
	}

}