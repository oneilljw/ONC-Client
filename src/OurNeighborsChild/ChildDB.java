package OurNeighborsChild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ChildDB extends ONCDatabase
{
	private static final int CHILD_DB_HEADER_LENGTH = 12;
	private static ChildDB instance = null;
	private ArrayList<ONCChild> childAL;
	
	private ChildDB()
	{
		super();
		childAL = new ArrayList<ONCChild>();
	}
	
	public static ChildDB getInstance()
	{
		if(instance == null)
			instance = new ChildDB();
		
		return instance;
	}
	
	//Used to create a child internal to the application via the ONC Server
	ONCObject add(Object source, ONCObject entity)
	{	
		ONCObject addedChild = null;
		
		//send add child request to server
		String response = "";
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("POST<add_child>" + gson.toJson(entity, ONCChild.class));		
			
			//if the server added the child,  add the new child to the data base and notify ui's
			if(response.startsWith("ADDED_CHILD"))		
				addedChild =  processAddedChild(response.substring(11));
		}
		
		return addedChild;
	}
	
	ONCObject processAddedChild(String json)
	{
		ONCChild addedChild = null;
		Gson gson = new Gson();
		addedChild = gson.fromJson(json, ONCChild.class);
		
		if(addedChild != null)
		{
			childAL.add(addedChild);
			fireDataChanged(this, "ADDED_CHILD", addedChild);
			
			Families fDB = Families.getInstance();
			int[] countsChange = fDB.getServedFamilyAndChildCount();
			DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
			fireDataChanged(this, "UPDATED_SERVED_COUNTS", servedCountsChange);
		}
		
		return addedChild;
	}

	/*************************************************************************************************************
	 * This method is called when the user requests to delete a child from the menu bar. The child to be deleted
	 * is the child currently selected in child table in the family panel. The first step in deletion is to confirm
	 * with the user that they intended to delete the child. Prior to removing the child from the 
	 * family objects child array list, the method checks for wish assignees and, if any, removes the child's
	 * assigned ornament count prior to removal from the child array list. 
	 **********************************************************************************************************/
	void deleteChild(ONCChild reqDelChild)
	{
		String response = "";
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			
			response = serverIF.sendRequest("POST<delete_child>" + gson.toJson(reqDelChild, ONCChild.class));		
			
			//if the server added the child,  add the new child to the data base and notify ui's
			if(response.startsWith("DELETED_CHILD"))		
				processDeletedChild(response.substring(13));
		}
	}
	
	void processDeletedChild(String json)
	{
		ChildWishDB cwDB = ChildWishDB.getInstance();
		ONCOrgs partnerDB = ONCOrgs.getInstance();
		
		Gson gson = new Gson();
		ONCChild deletedChild = gson.fromJson(json, ONCChild.class);
				
		//remove child from local partner and wish databases
		//and remove the child from this data base
		partnerDB.deleteChildWishAssignments(deletedChild);
		ArrayList<WishBaseOrOrgChange> wishbasechanges = cwDB.deleteChildWishes(deletedChild);
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != deletedChild.getID())
			index++;
		
		if(index < childAL.size())
		{
			childAL.remove(index);
			for(WishBaseOrOrgChange change: wishbasechanges)
				//deleted wish - need to tell wish catalog dialog to adjust wish counts
				fireDataChanged(this, "WISH_BASE_CHANGED", change);
			
			Families fDB = Families.getInstance();
			int[] countsChange = fDB.getServedFamilyAndChildCount();
			DataChange servedCountsChange = new DataChange(countsChange[0], countsChange[1]);
			fireDataChanged(this, "UPDATED_SERVED_COUNTS", servedCountsChange);
						
			fireDataChanged(this, "DELETED_CHILD", deletedChild);
		}
		else
			System.out.println("Child DB: Child removal failed, childID not found");
	}
	
	/***************************************************************
	 * This method is called when a child wish has been updated by 
	 * the user. The request update child object is passed. 
	 *************************************************************/
	String update(Object source, ONCObject updatedChild)
	{
		//notify the server
		Gson gson = new Gson();
		String response = null;
		ServerIF serverIF = ServerIF.getInstance();
		response = serverIF.sendRequest("POST<update_child>" + 
												gson.toJson(updatedChild, ONCChild.class));
		
		//check response. If response from server indicates a successful update,
		//create and store the updated child in the local data base and notify local
		//ui listeners of a change. The server may have updated the prior year ID
		if(response.startsWith("UPDATED_CHILD"))
			processUpdatedChild(response.substring(13));
		
		return response;
	}
	
	void processUpdatedChild(String json)
	{
		//Create a child object for the updated child
		Gson gson = new Gson();
		ONCChild updatedChild = gson.fromJson(json, ONCChild.class);
		
		//Find the position for the current child being updated
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != updatedChild.getID())
			index++;
		
		//Replace the current child object with the update
		if(index < childAL.size())
		{
			childAL.set(index, updatedChild);
			fireDataChanged(this, "UPDATED_CHILD", updatedChild);
		}
		else
			System.out.println(String.format("ChildDB processUpdatedChild - child id %d not found",
					updatedChild.getID()));
	}
	
	ONCChild getChild(int childid)
	{
		int index = 0;
		while(index < childAL.size() && childAL.get(index).getID() != childid)
			index++;
		
		if(index==childAL.size())
			return null;	//Child wasn't found
		else
			return childAL.get(index);
	}
		
	public Integer findHighestID(ArrayList<ONCChild> list)
	{
		Integer hID = 0;
		for(int i=0; i< list.size(); i++)
			if(list.get(i).getID() > hID)
				hID = list.get(i).getID();
		
		return hID;
	}
	
	ArrayList<ONCChild> getChildren(int famid)
	{
		ArrayList<ONCChild> fChildrenAL = new ArrayList<ONCChild>();
		
		for(ONCChild c:childAL)
			if(c.getFamID() == famid)
				fChildrenAL.add(c);
		
		//sort by age if there is more than one child in the family
		if(fChildrenAL.size() > 1)
			Collections.sort(fChildrenAL, new ONCChildAgeComparator());
		
		return fChildrenAL;
	}
	
	void assignChildNumbers(int famid)
	{
		ArrayList<ONCChild> fChildAL = getChildren(famid);
		
		Collections.sort(fChildAL, new ONCChildAgeComparator());
		for(int cn=0; cn < fChildAL.size(); cn++)
			fChildAL.get(cn).setChildNumber(cn);
	}
	
	int getTotalNumberOfChildren() { return childAL.size(); }
	
	int getNumberOfChildrenInFamily(int famid)
	{
		int count = 0;
		for(ONCChild c:childAL)
		{
			if(c.getFamID() == famid)
				count++;
		}
		
		return count;
	}
	
	void setChildWishID(int childid, int newWishID, int wishnumber)
	{
		ONCChild c = getChild(childid);
		if(c != null)
			c.setChildWishID(newWishID, wishnumber);	
	}
	
	ArrayList<ONCChild> getChildDB() { return childAL; }
	
	String importChildDatabase()
	{
		String response = "NO_CHILDREN";
		
		if(serverIF != null && serverIF.isConnected())
		{		
			Gson gson = new Gson();
			Type listtype = new TypeToken<ArrayList<ONCChild>>(){}.getType();
			
			response = serverIF.sendRequest("GET<children>");
			childAL = gson.fromJson(response, listtype);
			
			if(!response.startsWith("NO_CHILDREN"))		
				response =  "CHILDREN_LOADED";
		}
		
		return response;
	}
	
	String importChildDB(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "ChildDB.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Child DB .csv file to import");	
    		chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    		returnVal = chooser.showOpenDialog(pf);
    		pyfile = chooser.getSelectedFile();
		}
		
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	filename = pyfile.getName();
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] nextLine, header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == CHILD_DB_HEADER_LENGTH)
	    			{
	    				childAL.clear();
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					childAL.add(new ONCChild(nextLine));
	    				
	    			}
	    			else
	    				JOptionPane.showMessageDialog(pf, "Child DB file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Child DB File", JOptionPane.ERROR_MESSAGE, oncIcon);   			
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Child DB file: " + filename, 
	    					"Invalid Child DB File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Child DB file: " + filename, 
    				"Child DB file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return filename;    
	}
	
	String exportChildDBToCSV(JFrame pf, String filename)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile= fc.getFile("Select .csv file to save Child DB to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
    	}
    	else
    		oncwritefile = new File(filename);
    	
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		 String[] header = {"Child ID", "Family ID", "Child #", "First Name", "Last Name",
	    				 			"Gender", "DOB", "School", "Wish 1 ID", "Wish 2 ID",
	    				 			"Wish 3 ID", "Prior Year Child ID"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    for(ONCChild c:childAL)
	    	    	writer.writeNext(c.getDBExportRow());	//Get family data
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }
	
	@Override
	public void dataChanged(ServerEvent ue) 
	{
		if(ue.getType().equals("UPDATED_CHILD"))
		{
			processUpdatedChild(ue.getJson());
		}
		else if(ue.getType().equals("ADDED_CHILD"))
		{
			processAddedChild(ue.getJson());
		}
		else if(ue.getType().equals("DELETED_CHILD"))
		{
			processDeletedChild(ue.getJson());
		}			
	}
	
	private class ONCChildAgeComparator implements Comparator<ONCChild>
	{
	    @Override
	    public int compare(ONCChild o1, ONCChild o2)
	    {
	        return o1.getChildDOB().compareTo(o2.getChildDOB());
	    }
	}
}
