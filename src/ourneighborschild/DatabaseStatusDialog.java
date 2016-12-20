package ourneighborschild;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.google.gson.Gson;

public class DatabaseStatusDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DatabaseManager statusDB;
	private List<DBYearAndButton> dbYearButtonList;

	DatabaseStatusDialog(JFrame pFrame)
	{
		super(pFrame);
		this.setTitle("Database Status");
		
		//get a reference to the DBStatus data base and the User data base
		statusDB = DatabaseManager.getInstance();
		if(statusDB != null)
			statusDB.addDatabaseListener(this);
		
		//initialize the year/button list
		dbYearButtonList = new ArrayList<DBYearAndButton>();
		
		//get the list of DBYears
		List<DBYear> dbYearList = statusDB.getDBStatus();
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
//		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		JPanel topPanel = new JPanel();
		
		JPanel bottomPanel = new JPanel(new GridLayout(0,3));
//		bottomPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		//add the logo icon to the left panel
		JLabel lblONCIcon = new JLabel(GlobalVariables.getONCLogo(), JLabel.LEFT);
		topPanel.add(lblONCIcon);
		
		String mssg ="<html><b><FONT COLOR=BLUE>To allow/prevent changes to a database,"
				+ "<br>click the database year's lock below:</FONT></b></html>";		
		JLabel lblMssg = new JLabel(mssg);
		topPanel.add(lblMssg);
		
		//for each year in the list, create a panel and add it to the content pane
		//first, get online users from user data base which is used to enable or disable
		//changing the lock. Cannot change a lock if the database is in use
		List<ONCUser> onlineUserList = null;
		UserDB oncUserDB = UserDB.getInstance();
    	if(oncUserDB != null && (onlineUserList = oncUserDB.getOnlineUsers()) != null)
    	{
    		for(DBYear dbYear: dbYearList)
    		{
    			JPanel yearPanel = new JPanel();
    			JLabel lblYear = new JLabel(dbYear.toString() + ":");
    			DBYearAndButton dbYearButton = new DBYearAndButton(dbYear);
    			dbYearButtonList.add(dbYearButton);
    			dbYearButton.getrbLock().addActionListener(this);
			
    			setEnabledYearLocks(onlineUserList);
			
    			yearPanel.add(lblYear);
    			yearPanel.add(dbYearButton.getrbLock());
			
    			bottomPanel.add(yearPanel);
    		}
		}
    	else
    	{
    		//Couldn't get online users so can't modify database locks
    		String errMsg = "Unable to dertermine database usage, please try again later";
    		JOptionPane.showMessageDialog(this, errMsg, "Database Error", JOptionPane.ERROR_MESSAGE,
    										GlobalVariables.getONCLogo());
    		
    		dispose();
    	}
		
    	contentPane.add(topPanel);
    	contentPane.add(bottomPanel);
		pack();
	}
	
	void setEnabledYearLocks(List<ONCUser> onlineUserList)
	{
    	for(DBYearAndButton dbYearButton: dbYearButtonList)
    	{
    		int index = 0;
    		while(index < onlineUserList.size() &&
    			   onlineUserList.get(index).getClientYear() != dbYearButton.getYear())
    			index++;
    			
    		if(index < onlineUserList.size())
    			dbYearButton.setRBLockEnabled(false);
    	}
	}
	
	void updateLock(DBYearAndButton dbYearButton)
	{
		//make a copy of the current dbYear
		DBYear reqUpdateDBYear = new DBYear(dbYearButton.getDBYear());
		
		//toggle the lock in the request
		reqUpdateDBYear.setLock(dbYearButton.isLocked() ? false:true);
		
		//send update request to the server
		String response;
		response = statusDB.update(this, reqUpdateDBYear);
		
		if(response.startsWith("UPDATED_DBYEAR"))
		{
			Gson gson = new Gson();
			processUpdatedDBYear(gson.fromJson(response.substring(14), DBYear.class));
		}
	}
	
	void processUpdatedDBYear(DBYear updatedDBYear)
	{
		//find the year in the list
		int index=0;
		while(index < dbYearButtonList.size() && dbYearButtonList.get(index).getYear() != updatedDBYear.getYear())
			index++;
			
		if(index < dbYearButtonList.size())
		{
			dbYearButtonList.get(index).setLock(updatedDBYear.isLocked());	//update the dbYear
			
			if(updatedDBYear.isLocked())
				dbYearButtonList.get(index).setRBLockIcon(GlobalVariables.getLockedIcon());
			else
				dbYearButtonList.get(index).setRBLockIcon(GlobalVariables.getUnLockedIcon());
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{	
		//find the dbYear who's locked was clicked
		int index=0;
		while(index < dbYearButtonList.size() && !dbYearButtonList.get(index).toString().equals(e.getActionCommand()))
			index++;
		
		if(index < dbYearButtonList.size())
			updateLock(dbYearButtonList.get(index));
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_DBYEAR"))
		{
			DBYear updatedDBYear = (DBYear) dbe.getObject1();
			
			//find the year and update the lock status
			if(this.isVisible())
				processUpdatedDBYear(updatedDBYear);
		}
	}
	
	private class DBYearAndButton extends DBYear
	{
		private JRadioButton rbLock;
		
		DBYearAndButton(DBYear dbYear)
		{
			super(dbYear);
			rbLock = new JRadioButton();
			rbLock.setActionCommand(dbYear.toString());
			
			if(dbYear.isLocked())
				rbLock.setIcon(GlobalVariables.getLockedIcon());
			else
				rbLock.setIcon(GlobalVariables.getUnLockedIcon());
		}
		
		JRadioButton getrbLock() { return rbLock; }
		void setRBLockIcon(ImageIcon icon) { rbLock.setIcon(icon); }
		void setRBLockEnabled(boolean tf) { rbLock.setEnabled(tf); }
	}
}
