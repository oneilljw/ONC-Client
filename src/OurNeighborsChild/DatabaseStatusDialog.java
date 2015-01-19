package OurNeighborsChild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
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
	private List<DBYear> dbYearList;
	private List<JRadioButton> rbList;

	DatabaseStatusDialog(JFrame pFrame)
	{
		super(pFrame);
		this.setTitle("Database Status");
		GlobalVariables gvs = GlobalVariables.getInstance();
		
		//get a reference to the DBStatus data base
		statusDB = DBStatusDB.getInstance();
		if(statusDB != null)
			statusDB.addDatabaseListener(this);
		
		//get the list of DBYears
		dbYearList = statusDB.getDBStatus();
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		//for each year in the list, create a panel and add it to the content pane
		rbList = new ArrayList<JRadioButton>();
		for(DBYear dbYear: dbYearList)
		{
			JPanel yearPanel = new JPanel();
			JLabel lblYear = new JLabel(dbYear.toString());
			JRadioButton rbLock = new JRadioButton();
			rbList.add(rbLock);
			rbLock.setActionCommand(dbYear.toString());
			rbLock.addActionListener(this);
			
			if(dbYear.isLocked())
				rbLock.setIcon(gvs.getImageIcon(DB_LOCKED_IMAGE_INDEX));
			else
				rbLock.setIcon(gvs.getImageIcon(DB_UNLOCKED_IMAGE_INDEX));
			
			yearPanel.add(lblYear);
			yearPanel.add(rbLock);
			
			contentPane.add(yearPanel);
		}
		
		pack();
	}
	
	void updateLock(DBYear dbYear)
	{
		//make a copy of the current dbYear
		DBYear reqUpdateDBYear = new DBYear(dbYear);
		
		//toggle the lock in the request
		reqUpdateDBYear.setLock(dbYear.isLocked() ? false:true);
		
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
		while(index < dbYearList.size() && dbYearList.get(index).getYear() != updatedDBYear.getYear())
			index++;
			
		if(index < dbYearList.size())
		{
			dbYearList.get(index).setLock(updatedDBYear.isLocked());	//update the dbYear
			
			GlobalVariables gvs = GlobalVariables.getInstance();
			if(updatedDBYear.isLocked())
				rbList.get(index).setIcon(gvs.getImageIcon(DB_LOCKED_IMAGE_INDEX));
			else
				rbList.get(index).setIcon(gvs.getImageIcon(DB_UNLOCKED_IMAGE_INDEX));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{	
		//find the dbYear who's locked was clicked
		int index=0;
		while(index < dbYearList.size() && !dbYearList.get(index).toString().equals(e.getActionCommand()))
			index++;
		
		if(index < dbYearList.size())
			updateLock(dbYearList.get(index));
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
}
