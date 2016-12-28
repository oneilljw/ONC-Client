package ourneighborschild;

import java.util.EnumSet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ChangeReferenceNumberDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener 
{
	/**
	 * This dialog allows the user to change a family's ODB Number.
	 */
	private static final long serialVersionUID = 1L;
	private ONCFamily f;

	ChangeReferenceNumberDialog(JFrame owner)
	{
		super(owner, false);

		//Set dialog title and add type label info
		this.setTitle("Change Reference Number");
		lblONCIcon.setText("<html><font color=blue>Enter New Reference Number<br>and Click Save</font></html>");
		
		//set dialog to always be on top of owner
		this.setAlwaysOnTop(true);
		
		//connect to local Family DB
		FamilyDB familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addActionListener(dcl);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		btnAction.setText("Save New Reference #");
		
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		tf[0].setText(f.getReferenceNum());
		
		btnAction.setEnabled(false);
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

	@Override
	void update() 
	{
		
		//was it really a change? 
		if(!tf[0].getText().equals(f.getReferenceNum()))
		{
			//It was a change, first see if client user really wants a change. If they do, process it
				
			//get reference to Global Variables
			GlobalVariables gvs = GlobalVariables.getInstance();
		
			//Confirm with the user that the deletion is really intended
			String confirmMssg = String.format("<html>Are you sure you want to change<br> the Ref # from %s to %s?</html>",
												f.getReferenceNum(), tf[0].getText()); 
										
			Object[] options= {"Cancel", "Change Ref #"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
														gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Reference # Change ***");
			confirmDlg.setLocationRelativeTo(this);
			this.setAlwaysOnTop(false);
			confirmDlg.setVisible(true);
	
			Object selectedValue = confirmOP.getValue();
		
			//if the client user confirmed, change the ODB Number
			FamilyDB familyDB = FamilyDB.getInstance();
			if(selectedValue != null && selectedValue.toString().equals(options[1]))
			{
				ONCFamily reqFamily = new ONCFamily(f);	//make a copy of current family
		
				if(!tf[0].getText().equals(f.getReferenceNum()))
				{
					reqFamily.setReferenceNum(tf[0].getText());
				
					String response = familyDB.update(this, reqFamily);
					if(!response.startsWith("UPDATED_FAMILY"))
					{
						//display an error message that update request failed
						JOptionPane.showMessageDialog(this, "ONC Server denied Reference # change," +
														"try again later","Reference # Change Failed",  
														JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					}
				}
			}
		
			//family id will not change from update request, get updated family
			//from the data base and display
			display(familyDB.getFamily(f.getID()));
		
			this.setAlwaysOnTop(true);
		}
	}

	@Override
	boolean fieldUnchanged() 
	{		
		return tf[0].getText().equals(f.getReferenceNum());
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			if(this.isShowing())	//If Change ODB Number dialog visible, notify agent selection change
			{
				ONCFamily selFamily = (ONCFamily) tse.getObject1();
				this.display(selFamily);	//Display newly selected agent
			}

		}	
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY);
	}

	@Override
	void delete() { } // Unused, we don't delete ODB Numbers

	@Override
	String[] getDialogFieldNames() { return new String[]  {"Change Ref #"}; }
}
