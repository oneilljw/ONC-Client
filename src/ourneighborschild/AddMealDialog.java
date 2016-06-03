package ourneighborschild;

import java.awt.Dimension;
import java.util.Date;
import java.util.EnumSet;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AddMealDialog extends InfoDialog implements EntitySelectionListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily f;
	private MealDB mealDB;
	private JComboBox mealRequestCB;

	AddMealDialog(JFrame owner, boolean bModal) 
	{
		super(owner, bModal);
		this.setTitle("Add Meal Request");

		lblONCIcon.setText("<html><font color=blue>Add Meal Request<br>Information Below</font></html>");
		
		//initialize reference to family data base
		mealDB = MealDB.getInstance();

		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			tf[pn].setEnabled(false);
			infopanel[pn].add(tf[pn]);
		}
		
		//set up the transformation panel
		mealRequestCB = new JComboBox(MealType.getSelectionList());
		mealRequestCB.setPreferredSize(new Dimension(158,36));
		infopanel[2].remove(tf[2]);
		infopanel[2].add(mealRequestCB);
		tf[3].setEnabled(true);
		
		//add text to action button
		btnAction.setText("Add Meal");
				
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		
		tf[0].setText(f.getONCNum());
		tf[1].setText(f.getHOHLastName());
		mealRequestCB.setSelectedItem(0);
		tf[3].setText("");
	}

	@Override
	void update() 
	{
		ONCMeal addMealReq = new ONCMeal(-1, f.getID(), MealStatus.Requested, (MealType) mealRequestCB.getSelectedItem(), 
										tf[3].getText(), -1, userDB.getUserLNFI(), 
										new Date(), 4, "Added Meal", userDB.getUserLNFI());
		
		ONCMeal updatedMeal = mealDB.add(this, addMealReq);
		
		if(updatedMeal == null)
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), 
					"ONC Server denied add meal request, try again later","Add Meal Failed",  
					JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}
		
		this.setVisible(false);
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
		
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String[] getDialogFieldNames() 
	{
		return new String[] {"ONC #", "Last Name", "Meal Type", "Restrictions"};
	}
}
