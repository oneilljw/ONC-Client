package OurNeighborsChild;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVReader;

public class RAFamilyImporter extends ONCSortTableDialog
{
	/*************************************************************************************************
	 * This class imports families from locally stored .csv files that contain ONC Family Referral 
	 * work sheets. Even though it is not a dialog, this class subclasses ONCSortTableDialog to 
	 * implement the ability to notify registered listeners that a family has been imported. This importer
	 * interfaces with the user to loop thru a process of selecting a .csv file that contains an 
	 * ONC Family Referral work sheet, assigning a batch number (for the first family imported, all 
	 * others imported in the loop reuse the batch number), and then open the file, read  the family
	 * information and create/store Agent, Family and Children objects in the local data bases 
	 * which maintain sync with the ONC Server
	 ************************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int ONC_FAMILY_REFERRAL_WORKSHEET_RECORD_LENGTH = 11;
	private String defaultDirectory;
	private String batchNum;
	private ONCAgents agentDB;
	private Families famDB;
	private ChildDB childDB;
	
	RAFamilyImporter(JFrame parentFrame)
	{
		super(parentFrame);
		
		//initialize directory & batch string
		batchNum = null;	//holds the user selected batch number for first family import and reused
		defaultDirectory = null;	//holds the path for the first family file imported and reused
		
		//set up data base references. Each DB is a singleton
		agentDB = ONCAgents.getInstance();
		famDB = Families.getInstance();
		childDB = ChildDB.getInstance();
	}
	
	/**************************************************************************************************
	 * This method implements a loop allowing the user to import multiple ONC Family Referral Work Sheets.
	 * First, the method displays a file chooser dialog for the user to select a .csv file to import. If a 
	 * .csv file is selected and the format is correct, the file is read into a List<String[]> for subsequent
	 * processing. If it's the first time thru the loop, a dialog prompts for the Batch Number. Then
	 * the list is passed to the processing method. Upon completion, a result dialog is displayed and
	 * a TableSelectionEvent is created to notify registered listeners that a family has been imported. 
	 * At the bottom of the loop, a dialog that asks the user if they wish to import another file. 
	 * If the answer is no, the loop ends. If the user does not select a file, the loop also ends
	 ***************************************************************************************************/
	void onImportRAFMenuItemClicked()
    {
		JFileChooser chooser;
		String result;
		int mssgType;
		int returnVal = JFileChooser.CANCEL_OPTION;		//causes import to terminate
		
		do {
			if(defaultDirectory != null)	//if user imports more than one, start in same directory
				chooser = new JFileChooser(defaultDirectory);
			else
				chooser = new JFileChooser();
			
			chooser.setDialogTitle("Select Referring Agent Family .csv file to import");	
			chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
			
			String filename = "";
			File pyfile = null;
			returnVal = chooser.showOpenDialog(GlobalVariables.getFrame());
			List<String[]> inputRows = new ArrayList<String[]>();
	    
			if(returnVal == JFileChooser.APPROVE_OPTION)	//will exit if user doesn't select a file
			{
				//set the default directory which is reused for locating the next file for import
				pyfile = chooser.getSelectedFile();
				defaultDirectory = pyfile.getAbsolutePath();
				filename = pyfile.getName();
				
				try 
				{
					CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
					String[] nextLine, header;
    		
					if((header = reader.readNext()) != null)
					{
						//Read the ONC Family Referral work sheet .csv file
						if(header.length == ONC_FAMILY_REFERRAL_WORKSHEET_RECORD_LENGTH)
						{
							while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
								inputRows.add(nextLine);
							
							//determine if we need to get a batch number. Only the first family import in the loop
							//queries the user for a batch number. 
							if(batchNum == null && famDB != null)
							{
								BatchNumDialog bnDlg = new BatchNumDialog(parentFrame, famDB.generateRecommendedImportBatchNum());
								bnDlg.setVisible(true);
								batchNum = bnDlg.getBatchNumberFromDlg();
								bnDlg.dispose();
							}
							
							//Don't go any further if batchNum is still null, which happens if user doesn't select
							//or provide a batch number through proper use of the dialog
							if(batchNum != null)
							{
								//if the batch number is ok, process the file, creating a new family and children
								//and possibly a new agent. The family object is returned
								;
							
								//if the data base successfully created the new family object
								//notify the registered listeners of the import. This is why
								//this class subclasses ONCSortTableDialog
								ONCFamily addedFam;
								if((addedFam = processRAFInput(inputRows)) != null)
								{
									fireEntitySelected(this, "FAMILY_SELECTED", addedFam, null);
									result = addedFam.getHOHLastName() + " family imported";
									mssgType = JOptionPane.INFORMATION_MESSAGE;
								}
								else	//data base didn't not add the family
								{
									result = "Error - ONC Server unable to add family";
									mssgType = JOptionPane.ERROR_MESSAGE;
								}
							}
							else	//batch number wasn't provided by user
							{
								result = "No Batch Number Provided, Family Import Canceled";
								mssgType = JOptionPane.ERROR_MESSAGE;
							}
						}
						else	//selected .csv file did not have the correct column length
						{
							result = "RA Family .csv file corrupted, header length = " + Integer.toString(header.length);
							mssgType = JOptionPane.ERROR_MESSAGE;
						}
					}
					else	//file was empty
					{
						result = "Couldn't read header in RA Family .csv file: " + filename;
						mssgType = JOptionPane.ERROR_MESSAGE;
					}
				} 
				catch (IOException x)	//unable to open the file using a FileReader or CSVReader
				{
					result = "Unable to open RA Family .csv file: " + filename;
					mssgType = JOptionPane.ERROR_MESSAGE;
				}
				
				//show a import result dialog for each attempted family import
				String title = mssgType == JOptionPane.ERROR_MESSAGE ? "Family Import Error" : "Family Import Result";
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), result, title, mssgType, gvs.getImageIcon(0));
			}
		}
		//loop until user wants to quit importing, which is indicated thru a confirm dialog,
		//failing to choose a file or failing to choose/provide a valid batch number
		while(returnVal == JFileChooser.APPROVE_OPTION  && batchNum != null &&
				continueToImportFamilies() == JOptionPane.YES_OPTION);	
	    
    }
    
	/**************************************************************************************************
	 * This method processes a ONC Family Referral Worksheet that has been read into a List<String[]>
	 * data structure. Each sheet contains  one agent, one family and child information. A new object
	 * is created for the agent, family and each child and added to the respective data base. 
	 ***************************************************************************************************/
    ONCFamily processRAFInput(List<String[]> inputRows)
    {	
    	//add the agent to the local data base. The data base returns a new agent ID if the agent doesn't exist
    	//or if the agent already exists in the data base, it returns the current agent id
		Agent reqAgent = new Agent(-1, inputRows.get(2)[2] + " " + inputRows.get(2)[3], inputRows.get(2)[7],
				inputRows.get(2)[6], inputRows.get(2)[4], processPhoneNumber(inputRows.get(2)[5]));
		
		Agent responseAgt = (Agent) agentDB.add(this, reqAgent);
		int agentID = (responseAgt == null) ? -1 : responseAgt.getID();
		
		//create family constructor inputs
		//staring with generation of the ODB #
		String oncODBNum = famDB.generateIntakeODBNumber();
		
		//then split the address into house number and street
		String houseNum ="", street = "";
		String[] addParts = inputRows.get(7)[7].split(" ", 2);
		if(addParts.length == 2)
		{
			houseNum = addParts[0];
			street = addParts[1];
		}
		else
		{
			houseNum = "0000";
			street = inputRows.get(7)[7];
		}
		
		//this code handles determining the language for the family
		String speakEnglish, language;
		String inLang = inputRows.get(9)[4].toLowerCase();
    	if(inLang.contains("yes"))
    	{
    		speakEnglish = "Yes";
        	language =  "English";
    	}
    	else
    	{
    		String famLanguage = "Other";
    		String[] languages = {"Spanish", "Arabic", "Korean", "Vietnamese"};
    		String langLC = inputRows.get(10)[4].toLowerCase();
    		int index = 0;
    		while(index < languages.length)
    		{
    			if(langLC.contains(languages[index].toLowerCase()))
    			{
    				famLanguage = languages[index];
    				break;
    			}
    		
    			index++;	
    		}
    		
    		speakEnglish = "No";
        	language = famLanguage;
    	}
    	
    	//generate wish list for the family
    	StringBuilder odbWishList = new StringBuilder();
    	int index = findTableStartingRow(inputRows, "Children in the Family");
    	int nChildren = 0;
    	while(index > -1 && index < inputRows.size()) 
    	{
    		if(!inputRows.get(index)[2].isEmpty())
    		{
    			String cfn = inputRows.get(index)[2].trim();
    			String cln = inputRows.get(index)[3].trim();
    			
    			if(nChildren > 0)
    				odbWishList.append("\n\n");
    			odbWishList.append(String.format("%s %s: %s", cfn, cln, inputRows.get(index)[7]));
    		}
    		
    		nChildren++;
    		index++;
    	}
		
		//add the family to the local family data base
		ONCFamily reqAddFam = new ONCFamily(-1, gvs.getUserLNFI(), "NNA", oncODBNum, batchNum,
				speakEnglish, language, inputRows.get(7)[2], inputRows.get(7)[3], houseNum,
				street, "", inputRows.get(7)[8], inputRows.get(7)[10],
				processPhoneNumber(inputRows.get(7)[5]), processPhoneNumber(inputRows.get(7)[6]),
				inputRows.get(7)[4], inputRows.get(13)[4].trim(), odbWishList.toString(), agentID);
		
		ONCFamily addedFam = (ONCFamily) famDB.add(this, reqAddFam);
		
		//if family db add was successful, add children to child data base
		if(addedFam != null)
		{
			//add the children, one at a time. First, find the row that starts the Child Table in the
			//work sheet. It is possible that a referring agent has added or deleted rows in the work
			//sheet. We can't assume it will be in the same row, but we do assume the title will
			//always be in column C
			index = findTableStartingRow(inputRows, "Children in the Family");
	    	while(index > -1 && index < inputRows.size())
	    	{
	    		if(!inputRows.get(index)[2].isEmpty())
	    		{
	    			String cfn = inputRows.get(index)[2].trim();	//first name
	    			String cln = inputRows.get(index)[3].trim();	//last name
	    			
	    			//Determine child gender
	    			String cGender;
	    			String inGender = inputRows.get(index)[4].toLowerCase();
	    			if(inGender.startsWith("f") || inGender.contains("female"))
	    				cGender = "Girl";
	    			else if(inGender.startsWith("m") || inGender.contains("male"))
	    				cGender = "Boy";
	    			else
	    				cGender = "Unknown";
	    			
	    			//create a new child object
	    			ONCChild childReq = new ONCChild(-1, addedFam.getID(), cfn, cln, cGender,
	    											  createChildDOB(inputRows.get(index)[5]),
	    											  inputRows.get(index)[6],
	    											  GlobalVariables.getCurrentSeason());
	    			
	    			//add the child object to the local data base
	    			childDB.add(this, childReq);	
	    		}
	    		
	    		index++;
	    	}
		} 	
    	
    	return addedFam;
    }
    
    /**************************************************************************************************
	 * This method processes a phone number. It creates a 10 character array of '?'. Then, it examines
	 * each character in the input parameter string. If a character is a valid ASCII digit, it is 
	 * added to the character array. If it is not, it is discarded. The examination continues until
	 * either the end of the input string is reached or 10 valid digits have been found.
	 * The resultant string of length 10 is returned.
	 ***************************************************************************************************/
    String processPhoneNumber(String input)
    {
    	if(input.isEmpty())
    		return "None Found";	
    	else
    	{		
    		char[] number = new char[10];
    		for(int i=0; i<number.length; i++)	//initialize number to "??????????"
    			number[i] = '?';
    	
    		int index = 0, pos = 0;
    		while(index < input.length() && pos < 10)	//ten digit phone numbers
    		{
    			if(input.charAt(index) == '0' || input.charAt(index) == '1' || input.charAt(index) == '2' ||
    					input.charAt(index) == '3' || input.charAt(index) == '4' || input.charAt(index) == '5' ||
    					input.charAt(index) == '6' || input.charAt(index) == '7' || input.charAt(index) == '8' ||
    					input.charAt(index) == '9')
    			{
    				number[pos++] = input.charAt(index);
    			}
    		
    			index++;
    		}
    	
    	return new String(number);
    	
    	}
    }
    
    /**************************************************************************************************
	 * This method queries the user as to whether they would like to import another family using a 
	 * simple YES_NO JOptionPane. The selected integer value is returned. 
	 ***************************************************************************************************/
    int continueToImportFamilies()
    {
    	return JOptionPane.showConfirmDialog(GlobalVariables.getFrame(), "Do you wish to import another family?",
    			"More Families to Import?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, gvs.getImageIcon(0));	
    }
    
    /**************************************************************************************************
	 * This method takes a string date in one of two formats (yyyy-MM-dd or M/D/yy) and returns a Date
	 * object from the string. If the input string is not of either format, the current date is returned.
	 ***************************************************************************************************/
    Long createChildDOB(String dob)
    {
//    	Locale locale = new Locale("en", "US");
		TimeZone timezone = TimeZone.getTimeZone("GMT");
		Calendar gmtDOB = Calendar.getInstance(timezone);
    	
    	//First, parse the input string based on format to create an Calendar variable for DOB
    	//If it can't be determined, set DOB = today. 
    	if(dob.length() == 10 && dob.contains("-"))	//format one
    	{			
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    		try
    		{
				gmtDOB.setTime(sdf.parse(dob));
			}
    		catch (ParseException e)
    		{
    			String errMssg = "Couldn't determine DOB from input: " + dob;
    			 JOptionPane.showMessageDialog(GlobalVariables.getFrame(), errMssg, "DoB Error", 
    					 						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
    	}
    	else if(dob.contains("/"))	//format two
    	{
    		SimpleDateFormat oncdf = new SimpleDateFormat("M/d/yy");
    		oncdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    		try
    		{
				gmtDOB.setTime(oncdf.parse(dob));
			}
    		catch (ParseException e)
    		{
    			String errMssg = "Couldn't determine DOB from input: " + dob;
   			 	JOptionPane.showMessageDialog(GlobalVariables.getFrame(), errMssg, "DoB Error",
   			 									JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
			}
    	}
    	
    	//then convert the Calendar to a Date and return it
    	return gmtDOB.getTimeInMillis();
    }

    /**************************************************************************************************
   	 * This method takes a .csv file in a List<String[]> format and searches column C of each row looking
   	 * for the target string. When found, it returns the index of the first field in that row.
   	 ***************************************************************************************************/
    int findTableStartingRow(List<String[]> inputRows, String target)
    {
    	int row = 0;
    	while(row < inputRows.size() && !inputRows.get(row)[2].startsWith(target))
    		row++;
    	
    	if(row < inputRows.size())
    		return row+2;	//Account for the header row in file and the table header row
    	else
    		return -1;
    }
}
