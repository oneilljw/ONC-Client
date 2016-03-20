package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class ChildCheckDialog extends CheckDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ChildDB cDB;
	
	private static String[] columnToolTips = {"Family #1 ONC Number", "Child #1 First Name", "Child #1 Last Name", 
										"Child #1 Gender", "Child #1 DOB", "Result", 
										"Family #2 ONC Number", "Child #2 First Name", "Child #2 Last Name", 
										"Child #2 Gender", "Child #2 DOB"};
	
	private static String[] columns = {"ONC#", "First Name","Last Name", "Gen.", "DOB", "Result", 
									   "ONC #", "First Name", "Last Name", "Gen.", "DOB"};
	
	private static int[] colWidths = {40, 88, 96, 48, 72, 72, 40, 88, 96, 48, 72};
	
	private static int[] colsCentered = {3, 5, 9};
		
	ChildCheckDialog(JFrame pf)
	{
		super(pf, "Child", columnToolTips, columns, colWidths, colsCentered);
		
		//Listen for data base changes
		cDB = ChildDB.getInstance();
		if(cDB != null)
			cDB.addDatabaseListener(this);
		
		//set up the sort criteria
		cbArray = new JCheckBox[6];
		cbArray[0] = new JCheckBox("Different Families?");
		cbArray[0].setSelected(true);
		cbArray[1] = new JCheckBox("Date of Birth");
		cbArray[1].setSelected(true);
		cbArray[2] = new JCheckBox("Gender");
		cbArray[2].setSelected(true);
		cbArray[3] = new JCheckBox("First Name");
		cbArray[4] = new JCheckBox("Exact Last Name");
		cbArray[4].setSelected(true);
		cbArray[5] = new JCheckBox("Partial Last Name");
		
		for(int i=0; i<cbArray.length; i++)
		{
			cbArray[i].addActionListener(this);
			checkCriteriaPanel.add(cbArray[i]);
		}

		//pack and locate the dialog
		pack();
		Point pt = pf.getLocation();
		setLocation(pt.x + 20, pt.y + 20);		
	}
	
	void sortDupTable(String colName)
	{
		if(colName.equals("ONC#"))	//Sort on Family 1 ONC Family
    		Collections.sort(dupAL, new DupItemONCNumComparator());
    	else if(colName.equals("Last Name"))	// Sort on Last Name
    		Collections.sort(dupAL, new DupItemChild1LastNameComparator());
    	else if(colName.equals("DOB"))	//Sort on DOB
    		Collections.sort(dupAL, new DupItemChild1AgeComparator());
        else
    		return;
	}

	boolean performDupCheck(boolean[] criteria)
	{
		DuplicateDataCheck datachecker = new DuplicateDataCheck();
		boolean bDupesFound =  datachecker.duplicateChildCheck(fDB.getList(), criteria, cDB, dupAL);
		if(bDupesFound)
			Collections.sort(dupAL, new DupItemChild1LastNameComparator());
		return bDupesFound;
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnPrint) { onPrintDataCheck(type + " Data Check"); }
		else
		{
			if(ae.getSource() == cbArray[0] && cbArray[0].isSelected())
				cbArray[3].setSelected(false);
			else if(ae.getSource() == cbArray[0] && !cbArray[0].isSelected())
				cbArray[3].setSelected(true);
			else if(ae.getSource() == cbArray[4] && cbArray[4].isSelected())
				cbArray[5].setSelected(false);
			else if(ae.getSource() == cbArray[5] && cbArray[5].isSelected())
				cbArray[4].setSelected(false);
		}
		
		buildTableList();		
	}

	private class DupItemChild1AgeComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			return d1.getChild1().getChildDOB().compareTo(d2.getChild1().getChildDOB());
		}
	}
	
	private class DupItemChild1LastNameComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			return d1.getChild1().getChildLastName().compareTo(d2.getChild1().getChildLastName());
		}
	}

}
