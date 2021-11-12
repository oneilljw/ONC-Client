package ourneighborschild;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;


public class DistributionDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily f;
	private DatabaseManager dbMgr;
	private FamilyDB familyDB;
	private DistributionCenterDB distCenterDB;
	private UserDB userDB;
	private JComboBox<GiftDistribution> distributionCB;
	private JComboBox<DistributionCenter> centerCB;
	private DefaultComboBoxModel<DistributionCenter> centerCBM;
	DistributionListener distListener;

	DistributionDialog(JFrame owner, boolean bModal) 
	{
		super(owner, bModal);
		this.setTitle("Gift Distribution Method");

		lblONCIcon.setText("<html><font color=blue>Edit Gift Distribution <br>Method Below</font></html>");
		
		//initialize reference to data bases
		dbMgr = DatabaseManager.getInstance();
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		distCenterDB = DistributionCenterDB.getInstance();
		if(distCenterDB != null)
			distCenterDB.addDatabaseListener(this);
		
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
		distListener = new DistributionListener();
		distributionCB = new JComboBox<GiftDistribution>(GiftDistribution.getDistributionOptions());
		distributionCB.setPreferredSize(new Dimension(120,36));
		distributionCB.addActionListener(distListener);
		
		infopanel[2].remove(tf[2]);
		infopanel[2].add(distributionCB);
		
		centerCB = new JComboBox<DistributionCenter>();
	    centerCB.setRenderer(new DistributionCenterComboBoxRenderer());
		centerCBM = new DefaultComboBoxModel<DistributionCenter>();
		centerCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
		centerCB.setModel(centerCBM);
		centerCB.setPreferredSize(new Dimension(140,36));
		centerCB.addActionListener(distListener);
		
		infopanel[3].remove(tf[3]);
		infopanel[3].add(centerCB);
		
		//add text to action button
		btnAction.setText("Apply Change");
				
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		
		tf[0].setText(f.getONCNum());
		tf[1].setText(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0 ? f.getLastName() : "");
		distributionCB.setSelectedItem(f.getGiftDistribution());
		
		if(f.getGiftDistribution() == GiftDistribution.Pickup)
		{	
			if(f.getDistributionCenterID() < 0)
				centerCB.setSelectedIndex(0);
			else
				centerCB.setSelectedItem(distCenterDB.getDistributionCenter(f.getDistributionCenterID()));
			
			centerCB.setEnabled(true);
			
		}
		else
			centerCB.setEnabled(false);
		
		btnAction.setEnabled(false);		
	}
	
	void updateDistributionCenterLists()
	{	
		centerCB.removeActionListener(distListener);
		
		DistributionCenter curr_filter_sel = (DistributionCenter) centerCB.getSelectedItem();
		int filterSelIndex = 0;
		
		centerCBM.removeAllElements();
		
		//creates a dummy with code "Any"
		centerCBM.addElement(new DistributionCenter(-1, "Not Assigned", "Not Assigned"));
		
		int index = 0;
		for(DistributionCenter dc : distCenterDB.getList())
		{
			centerCBM.addElement(dc);
			index++;
			if(curr_filter_sel.matches(dc))
				filterSelIndex = index;
		}
		
		centerCB.setSelectedIndex(filterSelIndex); //Keep current selection in sort criteria
		
		centerCB.addActionListener(distListener);
	}

	@Override
	void update() 
	{
		ONCFamily updateFamReq = new ONCFamily(f);
		
		GiftDistribution distribution = (GiftDistribution) distributionCB.getSelectedItem();
		updateFamReq.setGiftDistribution(distribution);
		
		DistributionCenter center = (DistributionCenter) centerCB.getSelectedItem();
		updateFamReq.setDistributionCenter(center);
		
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
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CENTER"))
		{
			DistributionCenter center = (DistributionCenter) dbe.getObject1();
			
			if(this.isVisible() && f.getDistributionCenterID() == center.getID())
				tf[2].setText(center.getName());
		}
		else if(dbe.getType().contentEquals("LOADED_DATABASE"))
		{
			updateDistributionCenterLists();
		}
	}
	
	private class DistributionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == distributionCB || e.getSource() == centerCB)
			{
				GiftDistribution distribution = (GiftDistribution) distributionCB.getSelectedItem();
				DistributionCenter center = (DistributionCenter) centerCB.getSelectedItem();
				
				if(distribution != f.getGiftDistribution() || center.getID() != f.getDistributionCenterID())
					btnAction.setEnabled(true);
				else
					btnAction.setEnabled(false);
			}
		}
	}

	@Override
	String[] getDialogFieldNames() 
	{
		return new String[] {"ONC #", "Last Name", "Distribution Method", "Dist. Center"};
	}
	
	private class DistributionCenterComboBoxRenderer extends JLabel implements ListCellRenderer<Object> 
	{    
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	            int index, boolean isSelected, boolean cellHasFocus) 
	    {
	    	DistributionCenter dc = (DistributionCenter) value;
	        setText(dc.getAcronym());
	        return this;
	    }
	}
}
