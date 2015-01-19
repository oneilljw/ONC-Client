package OurNeighborsChild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import com.google.gson.Gson;

public class DatabaseStatusDialog extends JDialog implements ActionListener, DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int DB_UNLOCKED_IMAGE_INDEX = 17;
	private static final int DB_LOCKED_IMAGE_INDEX = 18;
	
	private DBStatusDB statusDB;
	private List<DBYearAndButton> dbYearButtonList;

	DatabaseStatusDialog(JFrame pFrame)
	{
		super(pFrame);
		this.setTitle("Database Status");
		
		//get a reference to the DBStatus data base and the User data base
		statusDB = DBStatusDB.getInstance();
		if(statusDB != null)
			statusDB.addDatabaseListener(this);
		
		//initialize the year/button list
		dbYearButtonList = new ArrayList<DBYearAndButton>();
		
		//get the list of DBYears
		List<DBYear> dbYearList = statusDB.getDBStatus();
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		//for each year in the list, create a panel and add it to the content pane
		for(DBYear dbYear: dbYearList)
		{
			JPanel yearPanel = new JPanel();
			JLabel lblYear = new JLabel(dbYear.toString());
			DBYearAndButton dbYearButton = new DBYearAndButton(dbYear);
			dbYearButtonList.add(dbYearButton);
			dbYearButton.getrbLock().addActionListener(this);
			
			setEnabledYearLocks();
			
			yearPanel.add(lblYear);
			yearPanel.add(dbYearButton.getrbLock());
			
			contentPane.add(yearPanel);
		}
		
		pack();
	}
	
	void setEnabledYearLocks()
	{
		List<ONCUser> onlineUserList = null;
		
    	//get online users from user data base
		UserDB oncUserDB = UserDB.getInstance();
    	if(oncUserDB != null && (onlineUserList = oncUserDB.getOnlineUsers()) != null)
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
		
		System.out.println("DatabaseStatusDialog.updateLock: " + response);
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
			
			GlobalVariables gvs = GlobalVariables.getInstance();
			if(updatedDBYear.isLocked())
				dbYearButtonList.get(index).setRBLockIcon(gvs.getImageIcon(DB_LOCKED_IMAGE_INDEX));
			else
				dbYearButtonList.get(index).setRBLockIcon(gvs.getImageIcon(DB_UNLOCKED_IMAGE_INDEX));
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
			DBYear updatedDBYear = (DBYear) dbe.getObject();
			
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
			
			GlobalVariables gvs = GlobalVariables.getInstance();
			if(dbYear.isLocked())
				rbLock.setIcon(gvs.getImageIcon(DB_LOCKED_IMAGE_INDEX));
			else
				rbLock.setIcon(gvs.getImageIcon(DB_UNLOCKED_IMAGE_INDEX));
		}
		
		JRadioButton getrbLock() { return rbLock; }
		void setRBLockIcon(ImageIcon icon) { rbLock.setIcon(icon); }
		void setRBLockEnabled(boolean tf) { rbLock.setEnabled(tf); }
	}
}
