package ourneighborschild;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public abstract class ONCPopup extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ONCPOPUP_DEFAULT_DELAY = 3;	//in seconds
	protected JLabel lblHeader;
	protected GridBagConstraints constraints;
	private int delay;	//pop-up display time in seconds
	
	ONCPopup(ImageIcon headingIcon, int delay)
	{
		this.delay = delay < ONCPOPUP_DEFAULT_DELAY ? ONCPOPUP_DEFAULT_DELAY : delay;
		this.setUndecorated(true);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				
		this.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
				
		constraints.gridx = 0;	
		constraints.gridy = 0;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
				
		lblHeader = new JLabel();
		lblHeader.setIcon(headingIcon); // --- use image icon you want to be as heading image.
		lblHeader.setOpaque(false);
		this.add(lblHeader, constraints);
				
		constraints.gridx++;
		constraints.weightx = 0f;
		constraints.weighty = 0f;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTH;
		
		Action closeAction = new CloseAction("X");
		JButton cloesButton = new JButton(closeAction);
		cloesButton.setMargin(new Insets(1, 4, 1, 4));
		cloesButton.setFocusable(false);
		this.add(cloesButton, constraints);
				
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;							
	}

	abstract void show(String header, String message);
	
	void makeVisible()
	{
		pack();
		this.setVisible(true);
		
		new Thread()
		{
			@Override
			public void run() {
				try {
					Thread.sleep(delay * 1000); // time after which pop up will disappear.
					dispose();
			    } catch (InterruptedException e) {
			         e.printStackTrace();
			    }
			
			};
		}.start();
	}
	
	class CloseAction extends AbstractAction
	{
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public CloseAction(String text)
	    {
	        super(text);
	    }
	    public void actionPerformed(ActionEvent e) {
	    	dispose();
	    }
	}
}
