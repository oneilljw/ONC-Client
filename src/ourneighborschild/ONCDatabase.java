package ourneighborschild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import au.com.bytecode.opencsv.CSVWriter;

public abstract class ONCDatabase extends ServerListenerComponent implements ServerListener
{
	protected static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	protected String title;
	
	public ONCDatabase()
	{
		serverIF.addServerListener(this);
/*		
		try 
		{
            // The newInstance() call is a work around for some broken Java implementations
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
		catch (Exception ex) 
		{
            // handle the error
        }
*/        
	}
	
	ONCObject find(List<? extends ONCObject> list, int id)
	{
		int index = 0;
		while(index < list.size() && id != list.get(index).getID())
			index++;
		
		if(index == list.size())
			return null;
		else
			return list.get(index);		
	}
	
	String title() { return title; }
	
	//All databases must implement an update class for notifications of changes
	abstract boolean importDB();
	abstract String update(Object source, ONCObject entity);
	abstract String[] getExportHeader();
	abstract List<? extends ONCObject> getList();
	
//	void onDBImportComplete(Integer year)
//	{
//		this.fireDataChanged(this, "LOADED_DATABASE",  year);
//	}
	
	boolean exportDBToCSV(JFrame pf, String filename)
    {
		try
		{
			File oncwritefile = new File(filename);
	    	CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	
	    	String[] header = this.getExportHeader();
	    	if(header != null)	//a component db will return a null header if can't be exported
	    	{
	    		writer.writeNext(this.getExportHeader());
	    	    
	    		for(ONCObject o : getList())
	    	    writer.writeNext(o.getExportRow());
	    	 
	    		writer.close();
	    	}
	    	
	    	return true;
	    }
		catch (NullPointerException npe)
	    {
	    	return false;
	    }
	    catch (IOException x)
	    {
	    	return false;
	    }
    }
	
	protected static boolean isNumeric(String str)
	{
		if(str == null || str.isEmpty())
			return false;
		else
			return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
}
