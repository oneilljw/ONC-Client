package ourneighborschild;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

public class ONCProgressBar extends ONCPopup implements PropertyChangeListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	
	ONCProgressBar(ImageIcon headingIcon, int delay)	//delay in seconds
	{
		super(headingIcon, delay);	//Show the pop-up for 100 seconds
		
		progressBar = new JProgressBar(0, 100);
		this.add(progressBar, constraints);
		
		pack();
	}

	@Override
	void show(String header, String message)
	{
		lblHeader.setText(header);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		this.makeVisible();	
	}
	void updateHeaderText(String header)
	{
		lblHeader.setText(header);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == "progress")
		{
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }	
	}

}
