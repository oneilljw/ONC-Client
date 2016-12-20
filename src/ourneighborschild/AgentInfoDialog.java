package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class AgentInfoDialog extends InfoDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 *  This class implements a dialog allowing application client users to edit 
	 *  information about an agent. It subclasses InfoDialog and registers with
	 *  the local agent data base to make and listen for agent changes.
	 */
	private static final long serialVersionUID = 1L;
	private JFrame owner;
	private JPanel changeAgtPanel;
	private JComboBox agtSelectCB;
	private DefaultComboBoxModel agtSelectCBM;
	private boolean bChangeAgtVisible;
	
	private Agent currAgent, dummyAgent;
	private ONCFamily currFamily;
//	private FamilyDB familyDB;
	private AgentDB agentDB;
	private boolean bAgentSelectedEnabled;
	
	AgentInfoDialog(JFrame owner, boolean bAgentSelectedEnabled)
	{
		super(owner, false);
		this.owner = owner;
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
//		familyDB = FamilyDB.getInstance();
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addActionListener(dcl);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		btnDelete.setText("Change Agent");
		btnAction.setText("Save Agent");
		
		//add change agent panel
		changeAgtPanel = new JPanel();
		changeAgtPanel.setLayout(new BoxLayout(changeAgtPanel, BoxLayout.Y_AXIS));
		
		JPanel agtSelectPanel = new JPanel();
		
		//create a dummy agent for use if there are no agents in the system
		dummyAgent = new Agent(-1, "No Agents", "None", "None", "None", "None");
				
		agtSelectCB = new JComboBox();
		agtSelectCBM = new DefaultComboBoxModel();
		agtSelectCBM.addElement(dummyAgent);
		agtSelectCB.setModel(agtSelectCBM);
		agtSelectCB.setPreferredSize(new Dimension(240, 56));
		agtSelectCB.setBorder(BorderFactory.createTitledBorder("Select New Family Agent"));
		agtSelectCB.setEditable(false);
		agtSelectCB.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(currAgent != null && !agtSelectCB.getSelectedItem().equals(currAgent))
					changeFamilyAgent();
			}
		
		});
		
//		AutoCompletion.enable(agtSelectCB);
		
		agtSelectPanel.add(agtSelectCB);
		
		changeAgtPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		changeAgtPanel.add(agtSelectPanel);
		
		contentPanel.add(changeAgtPanel);
		
		pack();
	}
	
	void display(ONCObject obj)
	{
		if (obj instanceof ONCFamily)
		{
			currFamily = (ONCFamily) obj;
			currAgent = (Agent) agentDB.getONCObject(currFamily.getAgentID());
		}
		else
			currAgent = (Agent) obj;
		
		tf[0].setText(currAgent.getAgentName());
		tf[0].setCaretPosition(0);
		
		tf[1].setText(currAgent.getAgentOrg());
		tf[1].setCaretPosition(0);
		
		tf[2].setText(currAgent.getAgentTitle());
		tf[2].setCaretPosition(0);
		
		tf[3].setText(currAgent.getAgentEmail());
		tf[3].setCaretPosition(0);
		
		tf[4].setText(currAgent.getAgentPhone());
		tf[4].setCaretPosition(0);
		
		btnAction.setEnabled(false);
		
		//can only change an agent if there is a family identified, the user is Admin or higher
		//and the dialog source is not a sort dialog
		agtSelectCB.setSelectedItem(currAgent);
		boolean bUserCanChangeAgent = userDB.getLoggedInUser() != null &&
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0;
				
		btnDelete.setVisible(currFamily != null && !bAgentSelectedEnabled && bUserCanChangeAgent);
		
		bChangeAgtVisible = false;
		changeAgtPanel.setVisible(bChangeAgtVisible);
	}
	
	void updateAgentList()
	{	
		AgentDB agentDB = AgentDB.getInstance();
		
		agtSelectCB.removeActionListener(this);
		
		agtSelectCBM.removeAllElements();
		
		List<Agent> agentList = (List<Agent>) agentDB.getList();
		
		for(Agent agt : agentList)
		{
			agtSelectCBM.addElement(agt);
		}

		agtSelectCB.addActionListener(this);
	}
	
	void changeFamilyAgent()
	{
		Agent changeToAgentReq = (Agent) agtSelectCB.getSelectedItem();
		//verify user really wants to change the family's agent.
		//Confirm with the user that the deletion is really intended
		String confirmMssg =String.format("Please confirm changing the %s family's agent from %s to %s?", 
					currFamily.getHOHLastName(), currAgent.getAgentName(), changeToAgentReq.getAgentName());
	
		Object[] options= {"Cancel", "Confirm Agent Change"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							GlobalVariables.getONCLogo(), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Referring Agent Change ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Change"))
		{
			System.out.println(String.format("AgtInfoDlg.changeFamilyAgent: Changing from %s to %s",
					currAgent.getAgentName(), changeToAgentReq.getAgentName()));
		}
	}
	
	@Override
	void update()
	{
		Agent reqAgt = new Agent(currAgent);	//make a copy of current agent
		
		byte cf = 0;
		if(!tf[0].getText().equals(currAgent.getAgentName())) { reqAgt.setAgentName(tf[0].getText());  cf |= 1; }
		if(!tf[1].getText().equals(currAgent.getAgentOrg())) { reqAgt.setAgentOrg(tf[1].getText());  cf |= 2; }
		if(!tf[2].getText().equals(currAgent.getAgentTitle())) { reqAgt.setAgentTitle(tf[2].getText()); cf |= 4; }
		if(!tf[3].getText().equals(currAgent.getAgentEmail())) { reqAgt.setAgentEmail(tf[3].getText()); cf |= 8; }
		if(!tf[4].getText().equals(currAgent.getAgentPhone())) { reqAgt.setAgentPhone(tf[4].getText()); cf |= 16; }
		
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
	
	//CANT DELTE AN AGENT AS OF 10-13-16 *** It's really now change agent.
	void delete()
	{
		//can only change an agent if the dialog owner is not a sort dialog
		bChangeAgtVisible =  bAgentSelectedEnabled ? false : !bChangeAgtVisible;
		changeAgtPanel.setVisible(bChangeAgtVisible);
/*
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
*/		
	}
	
	@Override
	boolean fieldUnchanged()
	{
		return tf[0].getText().equals(currAgent.getAgentName()) &&
				tf[1].getText().equals(currAgent.getAgentOrg()) &&
				 tf[2].getText().equals(currAgent.getAgentTitle()) &&
				  tf[3].getText().equals(currAgent.getAgentEmail()) &&
				   tf[4].getText().equals(currAgent.getAgentPhone());
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_AGENT"))
		{
			Agent updatedAgent = (Agent) dbe.getObject1();
			
			if(this.isVisible() && currAgent.getID() == updatedAgent.getID())
				display(updatedAgent);
			
			updateAgentList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_AGENT"))
		{
			Agent updatedAgent = (Agent) dbe.getObject1();
			
			updateAgentList();
			
			if(this.isVisible() && currAgent.getID() == updatedAgent.getID())
				this.dispose();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_AGENTS"))
		{
			updateAgentList();
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
