package ourneighborschild;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class CompareFamilyData 
{
	/**
	 * This class implements a compares families in an ODB or WFCM data base file,
	 * determines how well the family matches to families in an ONC data base and 
	 * writes the comparison result for the family to an output file 
	 * Methods are provided to read a ODB or WFCM input .csv file and compare each 
	 * family record against a specified ONC data base using Head of Household (HOH)
	 * first name, last name, street number and street address criteria. If two or 
	 * more of the criteria match, a ODB or WFCM Family is deemed to have matched.
	 * For each ODB or WFCM family compared, a record in the output file is created
	 * showing the results of all 4 comparisons. Using file open and file save
	 * dialogs, the user is queried to provide the name of the ODB or WFCM file to
	 * be compared and the name of the output file for the results.
	 */
	private static final int ONC_OPEN_FILE = 0;
	private static final int ONC_SAVE_FILE = 1;
	
	ArrayList<String[]> sourceAL;
	ArrayList<String[]> outputAL;
	ArrayList<ONCFamily> fAL;
	GlobalVariables cfdGVs;
	JFrame parentFrame;
	
	CompareFamilyData(JFrame pFrame, GlobalVariables gvs, ArrayList<ONCFamily> fal)
	{
		parentFrame = pFrame;
		cfdGVs = gvs;
		fAL = fal;
		sourceAL = new ArrayList<String[]>();
		outputAL = new ArrayList<String[]>();
	}
	
	String readWFCMCSVfile(String source, String action)
	{
//		ONCRegions regions = new ONCRegions(cfdGVs.getImageIcon(0));
		ONCRegions regions = ONCRegions.getInstance();
		
		ONCFileChooser oncFC = new ONCFileChooser(parentFrame);
    	File odbfile= oncFC.getFile("Select " + source + " .csv file to " + action, 
    									new FileNameExtensionFilter("CSV Files", "csv"), ONC_OPEN_FILE);
    	String filename = "";
    	
    	if( odbfile!= null)
    	{
	    	filename = odbfile.getName();
	    	
	    	//If user selected OK in batch dialog, then proceed with the import
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(odbfile.getAbsoluteFile()));
	    		String[] nextLine;
	    		String[] header;
	    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			
	    			//Read WFCM File line by line
	    			if(header.length == 20)
	    			{
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    				{
	    					String[] outputLine = new String[26];
	    					String[] matches = new String[4];
	    					for(int i=6; i<26; i++)
	    						outputLine[i] = nextLine[i-6];
	    					
	    					if(isInONCFamilyDB(nextLine, matches))
	    					{
	    						outputLine[0] = "Yes";
	    						outputLine[1] = matches[0];
	    						outputLine[2] = matches[1];
	    						outputLine[3] = matches[2];
	    						outputLine[4] = matches[3];
	    					}
	    					else
	    					{
	    						outputLine[0] = "No";
	    						outputLine[1] = "";
	    						outputLine[2] = "";
	    						outputLine[3] = "";
	    						outputLine[4] = "";
	    					}
	    					int reg = regions.getRegionMatch(nextLine[4], nextLine[5]);
	    					outputLine[5] = regions.getRegionID(reg);
	    					outputAL.add(outputLine);
	    				}
	    			}
	    			
	    			else
	    				JOptionPane.showMessageDialog(parentFrame, 
	    						odbfile.getName() + " is not in correct format, cannot be imported", 
	    						"Invalid ODB/WFCM Format", JOptionPane.ERROR_MESSAGE, cfdGVs.getImageIcon(0)); 			    			
	    		}
	    		
	    		reader.close();
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	   
	    return filename;
    }
	
	String writeWFCMCSVFile(String action)
    {
    	String filename = "";
    	String[] header = {"ONC Match", "FN Match", "LN Match", "Hse # Match", "Street Match",
    						"ONC Region", "Xmas", "ID", "Last Name", "First Name", "Hse #", "Address", "Unit",
    						"City", "Zip", "Home", "Work", "Cell", "Email", "19+", "14-18", "0-12",
    						"MaxOfAge", "OldestKidsLTS", "x", " "};
    	
    	ONCFileChooser oncFC = new ONCFileChooser(parentFrame);
       	File oncwritefile= oncFC.getFile("Select  file to " + action,
       										new FileNameExtensionFilter("CSV Files", "csv"), ONC_SAVE_FILE);
       	if(oncwritefile!= null)
       	{
	    	filename = oncwritefile.getName();
 
	    	try 
	    	{
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	     
	    	    for(String[] outputLine: outputAL)
	    	    	writer.writeNext(outputLine);
	    	   
	    	    writer.close();
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
	    }
	    return filename;
    }
	
	boolean isInONCFamilyDB(String[] wfcmLine, String[] result)
	{
		//Separate street suffix from street name
		String[] streetparts = wfcmLine[5].split(" ");
		StringBuffer streetname = new StringBuffer("");
		for(int i=0; i < streetparts.length-1; i++)
			streetname.append(streetparts[i] + " ");
		String stName = streetname.toString().trim().toLowerCase();
		System.out.println(stName);
		
		int index = 0, nTrue = 0;
		while(index < fAL.size() && nTrue < 2)
		{
			nTrue = 0;
			if(fAL.get(index).getHOHFirstName().equalsIgnoreCase(wfcmLine[3]))
			{
				result[0] = "Yes";
				nTrue++;
			}
			else
				result[0] = "No";

			if(fAL.get(index).getHOHLastName().equalsIgnoreCase(wfcmLine[2]))
			{
				result[1] = "Yes";
				nTrue++;
			}
			else
				result[1] = "No";
			
			if(fAL.get(index).getHouseNum().equalsIgnoreCase(wfcmLine[4]))
			{
				result[2] = "Yes";
				nTrue++;
			}
			else
				result[2] = "No";

			if(fAL.get(index).getStreet().toLowerCase().contains(stName))
			{
				result[3] = "Yes";
				nTrue++;
			}
			else
				result[3] = "No";
						
			index++;
		}
		
		if(index < fAL.size())
			return true;
		else
			return false;
	}
}
