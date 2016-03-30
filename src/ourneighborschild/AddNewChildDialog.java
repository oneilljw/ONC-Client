package ourneighborschild;

import java.awt.Dimension;
import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

public class AddNewChildDialog extends InfoDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox genderCB;
	private JDateChooser dobDC;
	private ONCChild newchild;
	private ONCFamily fam;
	
	AddNewChildDialog(JFrame pf,  ONCFamily f)
	{
		super(pf, true);
		this.setTitle("Add Child to Family");
		
		//initialize members
		fam = f;
		newchild = null;
		
		//Set up the main panel, loop to set up components associated with names
		lblONCIcon.setText("<html><font color=blue>Enter New Child's<br>Information Below</font></html>");
		
		for(int pn=0; pn < 3; pn++)
		{
			tf[pn] = new JTextField(12);
			tf[pn].addKeyListener(tfkl);
			infopanel[pn].add(tf[pn]);
		}
		
		String[] genders = {"Boy", "Girl"};
		genderCB = new JComboBox(genders);
		genderCB.setPreferredSize(new Dimension(158,36));
		infopanel[3].add(genderCB);
		
		//set up the data chooser
		dobDC = new JDateChooser(gvs.getTodaysDate());
		dobDC.setPreferredSize(new Dimension(144, 28));
		infopanel[4].add(dobDC);
		
		//add text to action label
		btnAction.setText("Add Child");

		pack();
	}
	
	ONCChild getNewChild() { return newchild; }
	
	public long convertCalendarDOBToGMT(Calendar dobCal)
	{
		dobCal.set(Calendar.HOUR, 0);
		dobCal.set(Calendar.MINUTE, 0);
		dobCal.set(Calendar.SECOND, 0);
		dobCal.set(Calendar.MILLISECOND, 0);
		//gives you the current offset in ms from GMT at the current date
		TimeZone tz = dobCal.getTimeZone();
		int offsetFromUTC = tz.getOffset(dobCal.getTimeInMillis());
		return dobCal.getTimeInMillis() + offsetFromUTC;
	}
	
	@Override
	void update()
	{
		newchild = new ONCChild(0, fam.getID(), tf[0].getText(), tf[1].getText(),
				genderCB.getSelectedItem().toString(),
				convertCalendarDOBToGMT(dobDC.getCalendar()), tf[2].getText(),
				GlobalVariables.getCurrentSeason());
		
		result = true;
		dispose();
	}

	@Override
	boolean fieldUnchanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void display(ONCObject obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	String[] getDialogFieldNames() 
	{
		return new String[] {"First Name", "Last Name", "School", "Gender", "Date of Birth"};
	}
}
