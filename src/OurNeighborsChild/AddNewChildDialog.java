package OurNeighborsChild;

import java.awt.Dimension;
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
	
	AddNewChildDialog(JFrame pf, String[] tfNames,  ONCFamily f)
	{
		super(pf, true, tfNames);
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
		
		dobDC = new JDateChooser(gvs.getTodaysDate());
		dobDC.setPreferredSize(new Dimension(144, 28));
		infopanel[4].add(dobDC);
		
		//add text to action label
		btnAction.setText("Add Child");

		pack();
	}
	
	ONCChild getNewChild() { return newchild; }
	
	@Override
	void update()
	{
		newchild = new ONCChild(0, fam.getID(), tf[0].getText(), tf[1].getText(),
				genderCB.getSelectedItem().toString(), dobDC.getDate(), tf[2].getText());
		
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
}
