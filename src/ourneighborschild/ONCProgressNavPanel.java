package ourneighborschild;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ONCProgressNavPanel extends ONCNavPanel implements PropertyChangeListener
{
	/**
	 * Extends ONCNavPanel to add a progress bar in lieu of the message label
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	
	public ONCProgressNavPanel(JFrame parentFrame, ONCSearchableDatabase db)
	{
		super(parentFrame, db);
		{
			progressBar = new JProgressBar(0, 100);
			progressBar.setPreferredSize(new Dimension(260, 28));
			progressBar.setString("Loading Database");
			progressBar.setStringPainted(true);
		}
	}
	
	void setVisibleProgressBar(boolean tf)
	{
		if(tf)
		{
			lblMssg.setText("");
			mssgsubpanel.add(progressBar);
		}
		else
			mssgsubpanel.remove(progressBar);
	}
	
	JProgressBar getProgressBar() { return progressBar; }
	
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName() == "progress")
		{
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            mssgsubpanel.revalidate();
        }	
	}
}
