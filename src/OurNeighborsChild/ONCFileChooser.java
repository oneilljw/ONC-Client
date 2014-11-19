package OurNeighborsChild;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ONCFileChooser 
{
	private static final int ONC_OPEN_FILE = 0;
	
	JFrame parentFrame;
	
	public ONCFileChooser(JFrame pf)
	{
		parentFrame = pf;
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
    		returnVal = chooser.showOpenDialog(parentFrame);
    	else
    		returnVal = chooser.showSaveDialog(parentFrame);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {
	    	return chooser.getSelectedFile();
	    }
	    else
	    	return null;
    }
}
