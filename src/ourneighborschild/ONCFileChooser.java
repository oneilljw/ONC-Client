package ourneighborschild;

import java.awt.Window;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ONCFileChooser 
{
	private static final int ONC_OPEN_FILE = 0;
	
	Window parentWindow;
	
	public ONCFileChooser(Window pf)
	{
		parentWindow = pf;
	}
	
	public File getFile(String title, FileNameExtensionFilter fnef, int op)
    {
    	//Create the chooser
    	JFileChooser chooser = new JFileChooser();
    	
    	//Set the dialog title
    	chooser.setDialogTitle(title);
    	
    	//Set the filter
    	chooser.setFileFilter(fnef);
    	
	    //Show dialog and return File object if user approves selection, else return a
    	//null File object if user cancels or an error occurs
    	int returnVal;
    	if(op == ONC_OPEN_FILE)
    		returnVal = chooser.showOpenDialog(parentWindow);
    	else
    		returnVal = chooser.showSaveDialog(parentWindow);
	    
    	return returnVal == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }
	
	public File getDirectory(String title)
    {
    	//Create the chooser
    	JFileChooser chooser = new JFileChooser();
    	
    	//Set the dialog title
    	chooser.setDialogTitle(title);
    	
    	//set to dialog only and disable the all files option
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
    	
	    //Show dialog and return File object if user approves selection, else return a
    	//null File object if user cancels or an error occurs
    	int returnVal = chooser.showSaveDialog(parentWindow);
	    
    	return returnVal == JFileChooser.APPROVE_OPTION ? chooser.getCurrentDirectory() : null;	    
    }
}
