package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ChangeBatchNumberDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener 
{
	/**
	 * This dialog allows the user to change a family's Batch Number.
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox batchNumCB;
	private ONCFamily f;

	ChangeBatchNumberDialog(JFrame owner)
	{
		super(owner, false);

		//Set dialog title and add type label info
		this.setTitle("Change Batch Number");
		lblONCIcon.setText("<html><font color=blue>Enter New Batch Number<br>and Click Save</font></html>");
		
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
		
		//set up the transformation panel
		String[] batchnums = {"B-01","B-02","B-03","B-04","B-05","B-06","B-07","B-08","B-09","B-10",
		"B-CR", "B-DI"};
		batchNumCB = new JComboBox(batchnums);
		batchNumCB.setPreferredSize(new Dimension(158,36));
		batchNumCB.addActionListener(new BatchNumberActionListener());
		infopanel[0].remove(tf[0]);
		infopanel[0].add(batchNumCB);
		
		btnAction.setText("Save New Batch #");
		
		pack();
	}
	
	void display(ONCObject obj)
	{
		f = (ONCFamily) obj;
		batchNumCB.setSelectedItem(f.getBatchNum());
		
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
		String reqBatchNum = (String) batchNumCB.getSelectedItem();
		//was it really a change? 
		if(!reqBatchNum.equals(f.getReferenceNum()))
		{
			//It was a change, first see if client user really wants a change. If they do, process it
				
			//get reference to Global Variables
			GlobalVariables gvs = GlobalVariables.getInstance();
		
			//Confirm with the user that the deletion is really intended
			String confirmMssg = String.format("<html>Are you sure you want to change<br>the Batch # from %s to %s?</html>",
												f.getBatchNum(), reqBatchNum); 
										
			Object[] options= {"Cancel", "Change Batch #"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
														gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Batch # Change ***");
			confirmDlg.setLocationRelativeTo(this);
			this.setAlwaysOnTop(false);
			confirmDlg.setVisible(true);
	
			Object selectedValue = confirmOP.getValue();
		
			//if the client user confirmed, change the Batch Number
			FamilyDB familyDB = FamilyDB.getInstance();
			if(selectedValue != null && selectedValue.toString().equals(options[1]))
			{
				ONCFamily reqFamily = new ONCFamily(f);	//make a copy of current family
				if(!reqBatchNum.equals(f.getBatchNum()))
				{
					reqFamily.setBatchNum(reqBatchNum);
				
					String response = familyDB.update(this, reqFamily);
					if(!response.startsWith("UPDATED_FAMILY"))
					{
						//display an error message that update request failed
						JOptionPane.showMessageDialog(this, "ONC Server denied Batch # change," +
														"try again later","Batch # Change Failed",  
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
		return batchNumCB.getSelectedItem().toString().equals(f.getBatchNum());
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			if(this.isShowing())	//If Change batch number dialog visible, notify batch # change
			{
				ONCFamily selFamily = (ONCFamily) tse.getObject1();
				this.display(selFamily);	//Display newly selected batch number
			}

		}	
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY);
	}

	@Override
	void delete() { } // Unused, we don't delete Batch Numbers
	
	private class BatchNumberActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == batchNumCB && 
				!f.getBatchNum().equals((String)batchNumCB.getSelectedItem()))
					btnAction.setEnabled(true);
				else
					btnAction.setEnabled(false);
		}
	}

	@Override
	String[] getDialogFieldNames()
	{
		return new String[] {"Change Batch #"};
	}
}
