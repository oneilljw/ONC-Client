package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;


public class DistributionDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily f;
	private FamilyDB familyDB;
	private UserDB userDB;
	private JComboBox<GiftDistribution> distributionCB;

	DistributionDialog(JFrame owner, boolean bModal) 
	{
		super(owner, bModal);
		this.setTitle("Gift Distribution Method");

		lblONCIcon.setText("<html><font color=blue>Edit Gift Distribution <br>Method Below</font></html>");
		
		//initialize reference to family data base
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		userDB = UserDB.getInstance();

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			tf[pn].setEnabled(false);
			infopanel[pn].add(tf[pn]);
		}

		//set up the transformation panel
		distributionCB = new JComboBox<GiftDistribution>(GiftDistribution.getDistributionOptions());
		distributionCB.setPreferredSize(new Dimension(120,36));
		distributionCB.addActionListener(new DistributionListener());
		
		infopanel[2].remove(tf[2]);
		infopanel[2].add(distributionCB);
		
		//add text to action button
		btnAction.setText("Apply Change");
				
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		
		tf[0].setText(f.getONCNum());
		tf[1].setText(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 ? f.getLastName() : "");
		distributionCB.setSelectedItem(f.getTransportation());
		
		btnAction.setEnabled(false);		
	}

	@Override
	void update() 
	{
		ONCFamily updateFamReq = new ONCFamily(f);
		GiftDistribution distribution = (GiftDistribution) distributionCB.getSelectedItem();
		updateFamReq.setGiftDistribution(distribution);
		
		String response = familyDB.update(this, updateFamReq);
		
		if(!response.startsWith("UPDATED_FAMILY"))
			distributionCB.setSelectedItem(f.getTransportation());
		else
			f = updateFamReq;
			
		btnAction.setEnabled(false);
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
		if(tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.GIFT)
		{
			if(this.isShowing())	//update for family or gift selection events
			{
				display((ONCFamily) tse.getObject1());	//Display newly selected agent
			}
		}
	}
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.GIFT);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFamily = (ONCFamily) dbe.getObject1();
			
			if(this.isVisible() && f.getID() == updatedFamily.getID())
				display(updatedFamily);
		}
	}
	
	private class DistributionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			GiftDistribution distribution = (GiftDistribution) distributionCB.getSelectedItem();
			if(distribution == f.getGiftDistribution())
				btnAction.setEnabled(false);
			else
				btnAction.setEnabled(true);
		}
		
	}

	@Override
	String[] getDialogFieldNames() 
	{
		return new String[] {"ONC #", "Last Name", "Distribution Method"};
	}
}
