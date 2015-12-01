package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class AngelAutoCallDialog extends ONCTableDialog implements ActionListener, ListSelectionListener
{
	/**
	 * This class implements a dialog that allows a user to view the results of importing Angel
	 * calls and and updating family status based on call results
	 */
	private static final long serialVersionUID = 1L;
	private static final String ANGEL_DELIVERY_CONFIRMED = "Delivery Confirmed";
	private static final int DELIVERY_STATUS_CONTACTED = 1;
	private static final int DELIVERY_STATUS_CONFIRMED = 2;
	
	private static final int ONC_NUM_COL= 0;
	private static final int PHONE_NUM_COL = 1;
	private static final int DATE_COL = 2;
	private static final int TIME_COL = 3;
	private static final int DURATION_COL = 4;
	private static final int DIRECTION_COL = 5;
	private static final int LANGUAGE_COL = 6;
	private static final int RESULT_COL = 7;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnImport, btnExport, btnPrint, btnProcess, btnClear;
	private JLabel lblNumOfCalls;
	
	private Families familyDB;
	private ArrayList<AngelCallItem> stAL;
	
	private boolean bCallsProcessed;
	
//	private boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
//	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
//	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
//	private boolean bFamilyDeliveryChanged = false;
//	private boolean bIgnoreSortDialogEvents = false;
	
//	private int sortCallResult = 0, sortDirection = 0, sortDStatus=0;

//	String[] callResult = {"Any", "Empty", "Info Verified", "Gifts Selected", "Gifts Received", "Gifts Verified", "Packaged"};
	
	AngelAutoCallDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("ONC Automated Call Results");
		
		familyDB = Families.getInstance();
		stAL = new ArrayList<AngelCallItem>();
		bCallsProcessed = false;
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
				
		//create the table
		String[] colToolTips = {"ONC Number", "Phone Number Involved", "Date call was placed", 
						"Time call was placed", "Length of Call", "Call Direction",
						"Language caller or recipient chose", "Result of Call"};
				
				dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

				dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				dlgTable.getSelectionModel().addListSelectionListener(this);
				
				//Set table column widths
				int tablewidth = 0;
				int[] colWidths = {48, 112, 88, 88, 32, 72, 78, 220};
				for(int col=0; col < colWidths.length; col++)
				{
					dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
					tablewidth += colWidths[col];
				}
				tablewidth += 24; 	//count for vertical scroll bar
				
		        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
		        
		        JTableHeader anHeader = dlgTable.getTableHeader();
		        anHeader.setForeground( Color.black);
		        anHeader.setBackground( new Color(161,202,241));
		        
		        //left justify duration column
//		        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//		        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
//		        dlgTable.getColumnModel().getColumn(DURATION_COL).setCellRenderer(dtcr);
		        
		        //Create the scroll pane and add the table to it.
		        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
		        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		        
		        JPanel cntlPanel = new JPanel();
		        
		        lblNumOfCalls = new JLabel("Number of Calls: 0");

		        btnImport = new JButton("Import");
		        btnImport.setToolTipText("Import Call Results from csv file");
		        btnImport.addActionListener(this);
		      
		        btnExport = new JButton("Export");
		        btnExport.setToolTipText("Export Call Results to csv file");
		        btnExport.setEnabled(false);
		        btnExport.addActionListener(this);
		        
		        btnPrint = new JButton("Print");
		        btnPrint.setToolTipText("Print call results");
		        btnPrint.setEnabled(false);
		        btnPrint.addActionListener(this);
		        
		        btnProcess = new JButton("Process Calls");
		        btnProcess.setToolTipText("Update Family Delivery Status from Call Results");
		        btnProcess.setEnabled(false);
		        btnProcess.addActionListener(this);
		        
		        btnClear = new JButton("Clear");
		        btnClear.setToolTipText("Clear Call Results");
		        btnClear.setEnabled(false);
		        btnClear.addActionListener(this);
		        
		        cntlPanel.add(lblNumOfCalls);
		        cntlPanel.add(btnImport);
		        cntlPanel.add(btnExport);
		        cntlPanel.add(btnPrint);
		        cntlPanel.add(btnProcess);
		        cntlPanel.add(btnClear);
		        
		        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		        getContentPane().add(dsScrollPane);
		        getContentPane().add(cntlPanel);
		        
		        pack();
		        this.setMinimumSize(new Dimension(tablewidth, 240));
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
	    				ONCFamily fam = null;
	    				
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
	    						fam = fdb.searchForFamilyByID(oncID);
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
	    					stAL.add(new AngelCallItem(callitem++,	fam, oncID, oncNum,	
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
	    		
	    		reader.close();
	    	}	    		
	    	
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	    
	    return callitem;
	}
	
	String readAngelCallResults()
	{
		//Initialize the call items read counter
		int callitem = 0;
		String filename = "";
		
		//Get the file to import. First, create the chooser
    	JFileChooser chooser = new JFileChooser();
    	
    	//Set the dialog title
    	chooser.setDialogTitle("Select Angel call data .csv file to read from");
    	
    	//Set the filter
    	chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    	
	    //Show dialog and return File object if user approves selection, else return a
    	//null File object if user cancels or an error occurs
	    if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
	    {
	    	File angelFile =  chooser.getSelectedFile();
	    	filename = angelFile.getName();
	    	
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
	    				ONCFamily fam = null;
	    				
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
	    					familyDB.searchDB(srchNum, resultAL);
	    					
	    					if(resultAL.size() == 0)	//Search for phone number failed
	    					{
	    						oncID = 0;
	    						oncNum = "N/A";
	    					}
	    					else
	    					{
	    						oncID = resultAL.get(0);
	    						fam = familyDB.searchForFamilyByID(oncID);
	    						oncNum = fam.getONCNum();
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
	    					stAL.add(new AngelCallItem(callitem++,	fam, oncID, oncNum,	
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
	    			JOptionPane.showMessageDialog(this, 
	    					angelFile.getName() + " is not in Angel Call Report format, cannot be imported", 
	    					"Invalid Call Report Format", JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
	    		
	    		reader.close();
	    	}	    		
	    	
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	    
	    return filename;
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
							 					GlobalVariables.getUserLNFI(),
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
							 					GlobalVariables.getUserLNFI(),
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
	 
	 void checkAndEnableControls()
	 {
		 if(stAL.size() > 0)
		 {
			 btnImport.setEnabled(false);
			 btnExport.setEnabled(true);
			 btnPrint.setEnabled(true);
			 btnProcess.setEnabled(!bCallsProcessed);
			 btnClear.setEnabled(true);
		 }
		 else
		 {
			 btnImport.setEnabled(true);
			 btnExport.setEnabled(false);
			 btnPrint.setEnabled(false);
			 btnProcess.setEnabled(false);
			 btnClear.setEnabled(false);
		 }
	 }
		
	
	private class AngelCallItem
	{
//		private int callItemNum;
		private ONCFamily family; //reference to ONC Family this call is associated with
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
		
		AngelCallItem(int cin, ONCFamily fam, int oncid, String oncnum, String phone, String d, String t, int dur, 
						String dir, String pagehist, String lang, String callres)
		{
//			callItemNum = cin;
			family = fam;
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
		ONCFamily getFamily() { return family; }
		int getONCID() { return oncID; }
		String getONCNum() { return oncNum; }
		String getPhoneNum() { return phoneNum; }
		String getDate() { return date; }
		String getTime() { return time; }
		String getDuration() { return Integer.toString(duration); }
		String getDirection() { return direction; }
		String getLanguage() { return language; }
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
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnProcess)
		{
			//Confirm with the user that the call processing is really intended
			String confirmMssg = "Are you sure you want to process calls?";	
		
			Object[] options= {"Cancel", "Process Call Items"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								gvs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Process Call Items ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Process Call Items"))
				updateFamilyDeliveryStatus();
//				writeAngelCallResults(oncFrame);
			else
				clearCallItemData(); //User chose not to process call items
			
			dlgTableModel.fireTableDataChanged();
			lblNumOfCalls.setText(String.format("Number of Calls: %d", stAL.size()));
			
			checkAndEnableControls();
		}
		else if(e.getSource() == btnImport)
		{
			String filename = readAngelCallResults();
			if(stAL.size() > 0)
			{	
				dlgTableModel.fireTableDataChanged();
				lblNumOfCalls.setText(String.format("Number of Calls: %d", stAL.size()));
				this.setTitle("ONCAutomatedCallResults - " + filename);
			}
			
			checkAndEnableControls();
		}
		else if(e.getSource() == btnClear)
		{
			clearCallItemData();
			dlgTableModel.fireTableDataChanged();
			lblNumOfCalls.setText(String.format("Number of Calls: %d", stAL.size()));
			checkAndEnableControls();
		}
	
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
						dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
		
		if(modelRow > -1 && lse.getSource() == dlgTable.getSelectionModel() && stAL.get(modelRow) != null)
		{
			fireEntitySelected(this, "FAMILY_SELECTED", stAL.get(modelRow).getFamily(), null);
			requestFocus();
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"ONC #", "Phone #", "Date", "Time", "Dur",
				"Direction", "Language", "Result"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return stAL.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	AngelCallItem aci = stAL.get(row);
        	
        	if(col == ONC_NUM_COL)  
        		return aci.getONCNum();
        	else if(col == PHONE_NUM_COL)
        		return aci.getPhoneNum();
        	else if (col == DATE_COL)
        		return aci.getDate();
        	else if (col == TIME_COL)
        		return aci.getTime();
        	else if (col == DURATION_COL)
        		return aci.getDuration();
        	else if (col == DIRECTION_COL)
        	{
        		return aci.getDirection();
//        		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:mm:ss");
//        		return sdf.format(user.getLastLogin());
        	}
        	else if (col == LANGUAGE_COL)
        		return aci.getLanguage();
        	else if (col == RESULT_COL)
        		return aci.getCallResult();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
       		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
/*
        public void setValueAt(Object value, int row, int col)
        { 
        	ONCUser currUser = userDB.getUserFromIndex(row);
        	ONCUser reqUpdateUser = null;
        	
        	//determine if the user made a change to a user object
        	if(col == LAST_NAME_COL && !currUser.getLastname().equals((String)value))
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setLastname((String) value);
        	}
        	else if(col == FIRST_NAME_COL && !currUser.getFirstname().equals((String) value))
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setFirstname((String) value);
        	}
        	else if(col == PERMISSION_COL && currUser.getPermission() != (UserPermission) value)
        	{
        		reqUpdateUser = new ONCUser(currUser);	//make a copy
        		reqUpdateUser.setPermission((UserPermission) value);
        	}
        	
        	//if the user made a change in the table, attempt to update the user object in
        	//the local user data base
        	if(reqUpdateUser != null)
        	{
        		String response = userDB.update(this, reqUpdateUser);        		
        		if(response == null || (response !=null && !response.startsWith("UPDATED_USER")))
        		{
        			//request failed
        			String err_mssg = "ONC Server denied update user request, try again later";
        			JOptionPane.showMessageDialog(GlobalVariables.getFrame(), err_mssg, "Update User Request Failure",
													JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
        		}
        	}
        }
*/          
    }
}
