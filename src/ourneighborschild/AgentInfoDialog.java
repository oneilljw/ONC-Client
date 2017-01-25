package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
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
	 *  information about a user who is an agent. It subclasses InfoDialog and registers with
	 *  the local user data base to make and listen for user_agent changes.
	 */
	private static final long serialVersionUID = 1L;
	private JPanel changeAgtPanel;
	private JComboBox agtSelectCB;
	private DefaultComboBoxModel agtSelectCBM;
	private ChangeAgentCBListener agtSelectCBListener;
	private boolean bChangeAgtVisible;
	
	private ONCUser currUserAgent;
	private ONCFamily currFamily;
	private UserDB userDB;
	private boolean bAgentSelectedEnabled;
	
	AgentInfoDialog(JFrame owner, boolean bAgentSelectedEnabled)
	{
		super(owner, false);
		this.bAgentSelectedEnabled = bAgentSelectedEnabled;

		//Set dialog title and add type label info
		this.setTitle("Agent Information");
		lblONCIcon.setText("<html><font color=blue>Agent Information</font></html>");
		
		//set dialog to always be on top of owner
		this.setAlwaysOnTop(true);
		
		//connect to local User DB
		userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//Set up the main panel, loop to set up components associated with names
		for(int pn=0; pn < getDialogFieldNames().length; pn++)
		{
			tf[pn] = new JTextField(13);
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
		
		agtSelectCB = new JComboBox();
		agtSelectCBM = new DefaultComboBoxModel();
		agtSelectCBM.addElement(new ONCUser()); //create a dummy user_agent for use if there are no agents in the system
		agtSelectCB.setModel(agtSelectCBM);
		agtSelectCB.setPreferredSize(new Dimension(240, 56));
		agtSelectCB.setBorder(BorderFactory.createTitledBorder("Select New Family Agent"));
		agtSelectCB.setEditable(false);
		agtSelectCBListener = new ChangeAgentCBListener();
		agtSelectCB.addActionListener(agtSelectCBListener);
		
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
			currUserAgent = (ONCUser) userDB.getUser(currFamily.getAgentID());
		}
		else
			currUserAgent = (ONCUser) obj;
		
		tf[0].setText(currUserAgent.getFirstname());
		tf[0].setCaretPosition(0);
		
		tf[1].setText(currUserAgent.getLastname());
		tf[1].setCaretPosition(0);
		
		tf[2].setText(currUserAgent.getOrg());
		tf[2].setCaretPosition(0);
		
		tf[3].setText(currUserAgent.getTitle());
		tf[3].setCaretPosition(0);
		
		tf[4].setText(currUserAgent.getEmail());
		tf[4].setCaretPosition(0);
		
		tf[5].setText(currUserAgent.getPhone());
		tf[5].setCaretPosition(0);
		
		btnAction.setEnabled(false);
		
		//can only change an agent if there is a family identified, the user is Admin or higher
		//and the dialog source is not a sort dialog
		agtSelectCB.setSelectedItem(currUserAgent);
		boolean bUserCanChangeAgent = userDB.getLoggedInUser() != null &&
				userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0;
				
		btnDelete.setVisible(currFamily != null && !bAgentSelectedEnabled && bUserCanChangeAgent);
		
		bChangeAgtVisible = false;
		changeAgtPanel.setVisible(bChangeAgtVisible);
	}
	
	void updateAgentList()
	{	
		agtSelectCB.removeActionListener(agtSelectCBListener);
		
		agtSelectCBM.removeAllElements();
		@SuppressWarnings("unchecked")
		List<ONCUser> userList = (List<ONCUser>) userDB.getList();
		Collections.sort(userList, new ONCUserLastNameComparator());
		
		for(ONCUser u : userList)
			if(u.getPermission().compareTo(UserPermission.Agent) >= 0)
				agtSelectCBM.addElement(u);

		agtSelectCB.addActionListener(agtSelectCBListener);
	}
	
	void changeFamilyAgent()
	{
		ONCUser changeToAgentReq = (ONCUser) agtSelectCB.getSelectedItem();
		
		//verify user really wants to change the family's agent.
		//Confirm with the user that the deletion is really intended
		String confirmMssg =String.format("Please confirm changing the %s family's agent from %s to %s?", 
					currFamily.getHOHLastName(), currUserAgent.getLastname(), changeToAgentReq.getLastname());
	
		Object[] options= {"Cancel", "Confirm Agent Change"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
							GlobalVariables.getONCLogo(), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Referring Agent Change ***");
		confirmDlg.setVisible(true);
	
		Object selectedValue = confirmOP.getValue();
		if(selectedValue != null && selectedValue.toString().equals("Change"))
		{
			System.out.println(String.format("AgtInfoDlg.changeFamilyAgent: Changing from %s to %s",
					currUserAgent.getLastname(), changeToAgentReq.getLastname()));
		}
	}
	
	@Override
	void update()
	{
		ONCUser reqUser = new ONCUser(currUserAgent);	//make a copy of current user
		
		byte cf = 0;
		if(!tf[0].getText().equals(currUserAgent.getFirstname())) { reqUser.setFirstname(tf[0].getText());  cf |= 1; }
		if(!tf[1].getText().equals(currUserAgent.getLastname())) { reqUser.setLastname(tf[1].getText());  cf |= 2; }
		if(!tf[2].getText().equals(currUserAgent.getOrg())) { reqUser.setOrg(tf[2].getText());  cf |= 4; }
		if(!tf[3].getText().equals(currUserAgent.getTitle())) { reqUser.setTitle(tf[3].getText()); cf |= 8; }
		if(!tf[4].getText().equals(currUserAgent.getEmail())) { reqUser.setEmail(tf[4].getText()); cf |= 16; }
		if(!tf[5].getText().equals(currUserAgent.getPhone())) { reqUser.setPhone(tf[5].getText()); cf |= 32; }
		
		if(cf > 0)
		{
			String response = userDB.update(this, reqUser);
			if(response.startsWith("UPDATED_USER"))
			{
				//user id will not change from update request, get updated user
				//from the data base and display
				display((ONCUser) userDB.getUser(reqUser.getID()));
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
		return tf[0].getText().equals(currUserAgent.getFirstname()) &&
				tf[1].getText().equals(currUserAgent.getLastname()) &&
				tf[2].getText().equals(currUserAgent.getOrg()) &&
				 tf[3].getText().equals(currUserAgent.getTitle()) &&
				  tf[4].getText().equals(currUserAgent.getEmail()) &&
				   tf[5].getText().equals(currUserAgent.getPhone());
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_USER"))
		{
			ONCUser updatedUser = (ONCUser) dbe.getObject1();
			
			if(this.isVisible() && currUserAgent.getID() == updatedUser.getID())
				display(updatedUser);
			
			updateAgentList();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_USER"))
		{
			ONCUser deletedUser = (ONCUser) dbe.getObject1();
			
			updateAgentList();
			
			if(this.isVisible() && currUserAgent.getID() == deletedUser.getID())
				this.dispose();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("LOADED_USERS"))
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
				ONCUser selUserAgent = (ONCUser) tse.getObject1();
				this.update();	//Save current info first, if changed
				this.display(selUserAgent);	//Display newly selected agent
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
		return new String[] {"First Name", "Last Name", "Organization", "Title", "Email", "Phone"};
	}
	
	private class ChangeAgentCBListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			// TODO Auto-generated method stub
			
		}	
	}
	
	private class ONCUserLastNameComparator implements Comparator<ONCUser>
	{
		@Override
		public int compare(ONCUser o1, ONCUser o2)
		{
			return o1.getLastname().compareTo(o2.getLastname());
		}
	}
}
