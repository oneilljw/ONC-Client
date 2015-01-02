package OurNeighborsChild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class AngelAutoCallDialog extends JDialog implements ActionListener 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String ANGEL_DELIVERY_CONFIRMED = "Delivery Confirmed";
	private static final int DELIVERY_STATUS_CONTACTED = 1;
	private static final int DELIVERY_STATUS_CONFIRMED = 2;
	
//	public JTable sortTable;
//	private DefaultTableModel sortTableModel;
//	private JComboBox resultCB, directionCB;
//	private JButton btnResetCriteria, btnPrintListing;
	public 	JButton btnProcessCallResults;
//	private JLabel lblNumOfCalls;
	private ArrayList<AngelCallItem> stAL;
	
//	private boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
//	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
//	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
//	private boolean bFamilyDeliveryChanged = false;
//	private boolean bIgnoreSortDialogEvents = false;
	
//	private int sortCallResult = 0, sortDirection = 0, sortDStatus=0;

//	String[] callResult = {"Any", "Empty", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
	
	AngelAutoCallDialog()
	{
		stAL = new ArrayList<AngelCallItem>();
	}
	
	int readAngelCallResults(JFrame pFrame, GlobalVariables gvs, Families fdb)
	{
		//Initialize the call items read counter
		int callitem = 0;
		
		//Get the file to import. First, create the chooser
    	JFileChooser chooser = new JFileChooser();
    	
    	//Set the dialog title
    	chooser.setDialogTitle("Select Angel call data .csv file to read from");
    	
    	//Set the filter
    	chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    	
	    //Show dialog and return File object if user approves selection, else return a
    	//null File object if user cancels or an error occurs
	    if(chooser.showOpenDialog(pFrame) == JFileChooser.APPROVE_OPTION)
	    {
	    	File angelFile =  chooser.getSelectedFile();
	    	
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(angelFile.getAbsoluteFile()));
	    		String[] nextLine;
	    		String[] header;
	    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Determine the Angel file is in the proper format
	    			if(header.length == 9)
	    			{
	    				ArrayList<Integer> resultAL = new ArrayList<Integer>();
	    				int oncID;
	    				String oncNum = "N/A/";
	    				
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					//clear the result list from the last search
	    					resultAL.clear();
	    					
	    					//eliminate any dashes in the phone number
	    					String srchNum = nextLine[4].replaceAll("-", "");
	    					
	    					//eliminate a leading 1 in the number if it has 11 digits
	    					if(srchNum.charAt(0) == '1' && srchNum.length() == 11)
	    						srchNum = srchNum.substring(1);
	    					
	    					//search the family data base for the number and see if we can match it
	    					//to an ONC #. Returns a list of family IDs that match the phone #
	    					fdb.searchDB(srchNum, resultAL);
	    					
	    					if(resultAL.size() == 0)	//Search for phone number failed
	    					{
	    						oncID = 0;
	    						oncNum = "N/A";
	    					}
	    					else
	    					{
	    						oncID = resultAL.get(0);
	    						oncNum = fdb.searchForFamilyByID(oncID).getONCNum();
	    					}
	    					
	    					//Strip out Page History from Persisted Variables
	    					String pageHistory;
	    					String[] varParts = nextLine[8].split(";");
	    					int part = 0;
	    					while(part < varParts.length && !varParts[part].contains("PageHistory"))
	    						part++;
	    					
	    					if(part==varParts.length)
	    						pageHistory = "";
	    					else
	    						pageHistory = varParts[part] + ";";
	    						
	    					//Add the call item to the array list
	    					stAL.add(new AngelCallItem(callitem++,	oncID, oncNum,	
	    									nextLine[4],						//Phone Number
	    									nextLine[2],						//Date
	    									nextLine[3],						//Time
	    									Integer.parseInt(nextLine[5]),		//Duration
	    									nextLine[6],						//Direction
	    									pageHistory,						//Page History
	    									determineCallLanguage(pageHistory),	//Call Language
	    									determineCallResult(nextLine[6], pageHistory)));	//Call Result
	    				}
	    			}
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pFrame, 
	    					angelFile.getName() + " is not in Angel Call Report format, cannot be imported", 
	    					"Invalid Call Report Format", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0)); 			    			
	    	}	    		
	    	
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	    
	    return callitem;
	}
	
	String determineCallLanguage(String pageHistory)
	{
		if(pageHistory.isEmpty() || pageHistory.equals("PageHistory:200000126686/1;") || 
				pageHistory.equals("PageHistory:200000126686/200;"))
			return "";
		else if(pageHistory.contains("200000126686/100") || pageHistory.contains("200000126686/300"))
			return "Spanish";
		else
			return "English";
	}
	
	String determineCallResult(String direction, String pageHistory)
	{
		String result ="Incomplete Call";
		if(pageHistory.contains("200000126686/9,"))
		{
			result = "Opted Out";
		}
		else if(pageHistory.isEmpty())
		{
			result = "No Page History";
		}
		else if(direction.equals("Outbound") && (pageHistory.contains("200000126686/12,") || pageHistory.contains("200000126686/104")))				
		{
				result = ANGEL_DELIVERY_CONFIRMED;
		}
		else if(direction.equals("Inbound") && (pageHistory.contains("200000126686/215") || pageHistory.contains("200000126686/315")))				
		{
				result = ANGEL_DELIVERY_CONFIRMED;
		}
		else if(pageHistory.contains("200000126686/10,") || pageHistory.contains("200000126686/105"))
		{
			result = "Incorrect Address";
		}
		else if(pageHistory.equals("PageHistory:200000126686/25;"))
		{
			result = "Left Voice Mail";
		}
		else if(pageHistory.contains("200000126686/210") || pageHistory.contains("200000126686/310"))
		{
			result = "Incorrect Address - Left Voice Mail?";
		}
		else if(pageHistory.equals("PageHistory:200000126686/1;") || pageHistory.equals("PageHistory:200000126686/200;"))
		{
			result = "Hangup";
		}
		
		return result;	
	}
	
	 String writeAngelCallResults(JFrame pFrame)
	 {
	    String filename = "";
	    String[] header = {"ONCNum", "Phone #", "Date", "Time", "Dur", "Direction", "Language",
	    					"Result","Page History"};
	    	
	    //Get the file to import. First, create the chooser
	    JFileChooser chooser = new JFileChooser();
	    	
	   	//Set the dialog title
	    chooser.setDialogTitle("Select a .csv file to save Angel call results to");
	    	
	    //Set the filter
	    chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
	    	
	    //Show dialog and return File object if user approves selection, else return a
	    //null File object if user cancels or an error occurs
		if(chooser.showSaveDialog(pFrame) == JFileChooser.APPROVE_OPTION)
		{
		    File angelWriteFile =  chooser.getSelectedFile();

	    	//If user types a new filename and doesn't include the .csv, add it
		    String filePath = angelWriteFile.getPath();		
		    if(!filePath.toLowerCase().endsWith(".csv")) 
		    	angelWriteFile = new File(filePath + ".csv");
	 
		    try 
		    {
		    	CSVWriter writer = new CSVWriter(new FileWriter(angelWriteFile.getAbsoluteFile()));
		    	writer.writeNext(header);
		    	     
		    	for(AngelCallItem aci:stAL)
		    	    writer.writeNext(aci.getCallItemTableRow());
		    	   
		    	writer.close();
		    	      	    
		    } 
		    catch (IOException x)
		    {
		    	System.err.format("IOException: %s%n", x);
		    	JOptionPane.showMessageDialog(pFrame, angelWriteFile.getName() + " could not be saved", 
							"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
		    }
		}
		return filename;
	 }
	 
	 boolean updateFamilyDeliveryStatus()
	 {
		 boolean bChangedDeliveryStatus = false;
		 
		 Families familyDB = Families.getInstance();
//		 DriverDB driverDB = DriverDB.getInstance();
		 DeliveryDB deliveryDB = DeliveryDB.getInstance();
		 GlobalVariables gvs = GlobalVariables.getInstance();
				 
		 for(int i=stAL.size()-1; i >=0; i--)
		 {
			 int oncID = stAL.get(i).getONCID();
			 if(oncID > 0)
			 {
				 ONCFamily f = familyDB.searchForFamilyByID(stAL.get(i).getONCID());
				 ONCFamily reqFamUpdate = new ONCFamily(f); //make a copy of the family object
			 
				 //If status == confirmed is an upgrade to status, change the family status and
				 //create a new ONCDelivery object
				 if(f.getDeliveryStatus() < DELIVERY_STATUS_CONFIRMED && 
						 stAL.get(i).getCallResult().equals(ANGEL_DELIVERY_CONFIRMED))
				 {
					 //add a new delivery to the delivery data base
					 ONCDelivery reqDelivery = new ONCDelivery(-1, f.getID(), DELIVERY_STATUS_CONFIRMED,
							 					deliveryDB.getDeliveredBy(f.getDeliveryID()),
							 					"Angel Call Result: Confirmed",
							 					gvs.getUserLNFI(),
							 					Calendar.getInstance());
					 
					 String response = deliveryDB.add(this, reqDelivery);
					 if(response.startsWith("ADDED_DELIVERY"))
					 {
						Gson gson = new Gson();
						ONCDelivery addedDelivery = gson.fromJson(response.substring(14), ONCDelivery.class);
						reqFamUpdate.setDeliveryID(addedDelivery.getID());
						reqFamUpdate.setDeliveryStatus(DELIVERY_STATUS_CONFIRMED);

					 }
					 else
					 {
						//display an error message that update request failed
						JOptionPane.showMessageDialog(this, "ONC Server denied Delivery Update," +
								"try again later","Driver Update Failed",  
								JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
 					 }
					 
//					 int did = delDB.addDelivery(f.getID(),
//							 	DELIVERY_STATUS_CONFIRMED,
//								ddb.getDriverLNFI(delDB.getDeliveredBy(f.getDeliveryID())),
//								"Angel Call Result: Confirmed",
//								gvs.getUserLNFI(),
//								Calendar.getInstance());
	
					 //update the family in the data base
//					 f.setDeliveryID(did);
//					 f.setChangedBy(gvs.getUserLNFI());
//
//					 f.setDeliveryStatus(DELIVERY_STATUS_CONFIRMED);
					 bChangedDeliveryStatus = true;
				 }
				 
				 else if(f.getDeliveryStatus() < DELIVERY_STATUS_CONTACTED)
				 {
					//add a new delivery to the delivery data base
					 ONCDelivery reqDelivery = new ONCDelivery(-1, f.getID(), DELIVERY_STATUS_CONTACTED,
							 					deliveryDB.getDeliveredBy(f.getDeliveryID()),
							 					"Angel Call Result: Contacted",
							 					gvs.getUserLNFI(),
							 					Calendar.getInstance());
					 
					 String response = deliveryDB.add(this, reqDelivery);
					 if(response.startsWith("ADDED_DELIVERY"))
					 {
						Gson gson = new Gson();
						ONCDelivery addedDelivery = gson.fromJson(response.substring(14), ONCDelivery.class);
						reqFamUpdate.setDeliveryID(addedDelivery.getID());
						reqFamUpdate.setDeliveryStatus(DELIVERY_STATUS_CONFIRMED);

					 }
					 else
					 {
						//display an error message that update request failed
						JOptionPane.showMessageDialog(this, "ONC Server denied Driver Update," +
								"try again later","Driver Update Failed",  
								JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
 					 }
//					 int did = delDB.addDelivery(f.getID(),
//							 	DELIVERY_STATUS_CONTACTED,
//								ddb.getDriverLNFI(delDB.getDeliveredBy(f.getDeliveryID())),
//								"Angel Call Result: Contacted",
//								gvs.getUserLNFI(),
//								Calendar.getInstance());
//	
//					 f.setDeliveryID(did);
//					 f.setChangedBy(gvs.getUserLNFI());
//					 f.setDeliveryStatus(DELIVERY_STATUS_CONTACTED);
					 bChangedDeliveryStatus = true;
				 }
			 }
		 }
		 
		 return bChangedDeliveryStatus;
	 }
	 
	 void clearCallItemData() { stAL.clear(); }	//Called if user cancels processing after read of calls
		
	
	private class AngelCallItem
	{
//		private int callItemNum;
		private int oncID;
		private String oncNum;
		private String phoneNum;
		private String date;
		private String time;
		private int duration;
		private String direction;
		private String result;
		private String language;
		private String pageHistory;
		
		AngelCallItem(int cin,  int oncid, String oncnum, String phone, String d, String t, int dur, 
						String dir, String pagehist, String lang, String callres)
		{
//			callItemNum = cin;
			oncID = oncid;
			oncNum = oncnum;
			phoneNum = phone;
			date = d;
			time = t;
			duration = dur;
			direction = dir;
			pageHistory = pagehist;
			language = lang;
			result = callres;
			
			//Get direction & result
		}
		
		//getters
//		int getCallItemNum() { return callItemNum; }
		int getONCID() { return oncID; }
//		String getONCNum() { return oncNum; }
//		String getPhoneNum() { return phoneNum; }
//		String getDuration() { return Integer.toString(duration); }
//		String getDirection() { return direction; }
		String getCallResult() {return result; }
		
		String[] getCallItemTableRow()
		{
			String[] row = new String[9];
			row[0] = oncNum;
			row[1] = phoneNum;
			row[2] = date;
			row[3] = time; 
			row[4] = Integer.toString(duration);
			row[5] = direction;
			row[6] = language;
			row[7] = result;
			row[8] = pageHistory;
			
			return row;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// TODO Auto-generated method stub	
	}
}
