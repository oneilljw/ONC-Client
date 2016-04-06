package ourneighborschild;

import java.util.EnumSet;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class AgentInfoDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 *  This class implements a dialog allowing application client users to edit 
	 *  information about an agent. It subclasses InfoDialog and registers with
	 *  the local agent data base to make and listen for agent changes.
	 */
	private static final long serialVersionUID = 1L;
	private Agent a;
	private FamilyDB familyDB;
	AgentDB agentDB;
	boolean bAgentSelectedEnabled;
	
	AgentInfoDialog(JFrame owner, boolean bAgentSelectedEnabled)
	{
		super(owner, false);
		this.bAgentSelectedEnabled = bAgentSelectedEnabled;

		//Set dialog title and add type label info
		this.setTitle("Agent Information");
		lblONCIcon.setText("<html><font color=blue>Agent Information</font></html>");
		
		//set dialog to always be on top of owner
		this.setAlwaysOnTop(true);
		
		//connect to local Agent DB
		agentDB = AgentDB.getInstance();
		if(agentDB != null)
			agentDB.addDatabaseListener(this);
		
		//get a reference to Family Db
		familyDB = FamilyDB.getInstance();
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addActionListener(dcl);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		btnDelete.setText("Delete Agent");
		btnAction.setText("Save Agent");
		
		pack();
	}
	
	void display(ONCObject obj)
	{
		if (obj instanceof ONCFamily)
		{
			ONCFamily fam = (ONCFamily) obj;
			a = (Agent) agentDB.getONCObject(fam.getAgentID());
		}
		else
			a = (Agent) obj;
		
		tf[0].setText(a.getAgentName());
		tf[0].setCaretPosition(0);
		
		tf[1].setText(a.getAgentOrg());
		tf[1].setCaretPosition(0);
		
		tf[2].setText(a.getAgentTitle());
		tf[2].setCaretPosition(0);
		
		tf[3].setText(a.getAgentEmail());
		tf[3].setCaretPosition(0);
		
		tf[4].setText(a.getAgentPhone());
		tf[4].setCaretPosition(0);
		
		btnAction.setEnabled(false);
		
		//can only delete an agent if no family assigned
		btnDelete.setVisible(familyDB.getNuberOfFamiliesWithAgent(a.getID()) == 0);
	}
	
	@Override
	void update()
	{
		Agent reqAgt = new Agent(a);	//make a copy of current agent
		
		byte cf = 0;
		if(!tf[0].getText().equals(a.getAgentName())) { reqAgt.setAgentName(tf[0].getText());  cf |= 1; }
		if(!tf[1].getText().equals(a.getAgentOrg())) { reqAgt.setAgentOrg(tf[1].getText());  cf |= 2; }
		if(!tf[2].getText().equals(a.getAgentTitle())) { reqAgt.setAgentTitle(tf[2].getText()); cf |= 4; }
		if(!tf[3].getText().equals(a.getAgentEmail())) { reqAgt.setAgentEmail(tf[3].getText()); cf |= 8; }
		if(!tf[4].getText().equals(a.getAgentPhone())) { reqAgt.setAgentPhone(tf[4].getText()); cf |= 16; }
		
		if(cf > 0)
		{
			AgentDB agentDB = AgentDB.getInstance();
			String response = agentDB.update(this, reqAgt);
			if(response.startsWith("UPDATED_AGENT"))
			{
				//agent id will not change from update request, get updated agent
				//from the data base and display
				display((Agent) agentDB.getONCObject(reqAgt.getID()));
			}
			else
			{
				//display an error message that update request failed
				GlobalVariables gvs = GlobalVariables.getInstance();
				
				JOptionPane.showMessageDialog(this, "ONC Server denied agent update," +
						"try again later","Agent Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
	}
	
	void delete()
	{
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("<html>Are you sure you want to delete<br><b>%s</b> from the database?</html>", a.getAgentName()); 
										
		Object[] options= {"Cancel", "Delete Agent"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							gvs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Agent Deletion ***");
		confirmDlg.setLocationRelativeTo(this);
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Delete Agent"))
		{
			Agent reqDelAgt = new Agent(a);	//make a copy of current agent
			
			AgentDB agentDB = AgentDB.getInstance();
			Agent delAgent = (Agent)agentDB.delete(this, reqDelAgt);
			
			if(delAgent != null && delAgent.getID() == reqDelAgt.getID())
			{
				//agent was removed, close the dialog
				this.dispose();
			}
			else
			{
				//display an error message that update request failed
				GlobalVariables gvs = GlobalVariables.getInstance();
				
				JOptionPane.showMessageDialog(this, "ONC Server denied agent deletion," +
						"try again later","Agent Deletion Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
		}
	}
	
	@Override
	boolean fieldUnchanged()
	{
		return tf[0].getText().equals(a.getAgentName()) &&
				tf[1].getText().equals(a.getAgentOrg()) &&
				 tf[2].getText().equals(a.getAgentTitle()) &&
				  tf[3].getText().equals(a.getAgentEmail()) &&
				   tf[4].getText().equals(a.getAgentPhone());
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_AGENT"))
		{
			Agent updatedAgent = (Agent) dbe.getObject();
			
			if(this.isVisible() && a.getID() == updatedAgent.getID())
				display(updatedAgent);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_AGENT"))
		{
			Agent updatedAgent = (Agent) dbe.getObject();
			if(this.isVisible() && a.getID() == updatedAgent.getID())
				this.dispose();
		}
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(tse.getType() == EntityType.AGENT  && bAgentSelectedEnabled)
		{
			if(this.isShowing())	//If Agent Info dialog visible, notify agent selection change
			{
				Agent selAgent = (Agent) tse.getObject1();
				this.update();	//Save current info first, if changed
				this.display(selAgent);	//Display newly selected agent
			}
		}
		else if(tse.getType() == EntityType.FAMILY)
		{
			if(this.isShowing())	//If Agent Info dialog visible, notify agent selection change
			{
				ONCFamily selFamily = (ONCFamily) tse.getObject1();
				this.update();	//Save current info first, if changed
				this.display(selFamily);	//Display newly selected family's agent
			}
		}	
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.AGENT, EntityType.FAMILY);
	}

	@Override
	String[] getDialogFieldNames()
	{
		return new String[] {"Name", "Organization", "Title", "Email", "Phone"};
	}
}
