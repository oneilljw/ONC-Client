package ourneighborschild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class StoplightPanel extends JPanel implements ActionListener, DatabaseListener
{
	/**
	 * This class implements a stop light that the user can click and set the color and
	 * add a message as a tool tip. All objects that have stop lights in the ONC application
	 * are derived from ONCEntity. This class listens to data base changes. If change objects
	 * are derived from type ONCEntity, the the stop light is updated. 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton rb;
	private ONCDatabase db;
	private ONCEntity e;
	private GlobalVariables gvs;
	private JFrame frame;
	
	StoplightPanel(JFrame frame, ONCDatabase db)
	{
		this.frame = frame;
		this.db = db;
		e = null;
		
		gvs = GlobalVariables.getInstance();
		rb = new JRadioButton(gvs.getImageIcon(8));
		rb.setEnabled(false);
		rb.addActionListener(this);
		
		db.addDatabaseListener(this);
		
		this.add(rb);
	}
	
	void setEntity(ONCEntity e)
	{
		this.e = e;
		
		if(e != null)
		{
			this.set(e.getStoplightPos(), e.getStoplightMssg() + ": " + e.getStoplightChangedBy() );
			rb.setEnabled(true);
		}
		else
			rb.setEnabled(false);
	}
	
	void set(int pos, String mssg)
	{
//		System.out.println(String.format("Stoplight.set: pos= %d, mssg= %s", pos, mssg));
		if(pos >= 0 && pos < 4)
			rb.setIcon(gvs.getImageIcon(pos+5));
		else
			rb.setIcon(gvs.getImageIcon(8));
		
		rb.setToolTipText(mssg);
	}
	void clear()
	{
		rb.setIcon(gvs.getImageIcon(8));
		rb.setToolTipText("");
		rb.setEnabled(false);
	}	
	
	
	boolean showStoplightDialog()
	{
		StoplightDialog slDlg = new StoplightDialog(frame, "Stoplight");
		
		slDlg.setData(e.getStoplightPos(), e.getStoplightMssg(), e.getStoplightChangedBy());
		slDlg.setLocationRelativeTo(this);
		
		slDlg.setVisible(true);
	
		if(slDlg.getStoplightPos() != e.getStoplightPos() ||
				!slDlg.getStoplightMssg().equals(e.getStoplightMssg()))
		{
			//user changed stop light, update ONCEntity
			e.setStoplightPos(slDlg.getStoplightPos());
			e.setStoplightMssg(slDlg.getStoplightMssg());
			e.setStoplightChangedBy(UserDB.getInstance().getUserLNFI());
			
			//notify data base of update
			db.update(this, e);
			
			//change the radio button to reflect the dialog user changes
			set(e.getStoplightPos(), e.getStoplightMssg() + ": " + e.getStoplightChangedBy());
			
			return true;
		}
		else
			return false;	//Data didn't change
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		showStoplightDialog();	
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getObject1() != null && 
				ONCEntity.class.isAssignableFrom(dbe.getObject1().getClass()))
		{
			//didn't originate the change and is an ONCEntity subclass so it has a stop light
			ONCEntity ue = (ONCEntity) dbe.getObject1();
			
			if(e != null && ue.getID() == e.getID())  
				set(ue.getStoplightPos(), ue.getStoplightMssg() + ": " + ue.getStoplightChangedBy());	
		}	
	}
}
