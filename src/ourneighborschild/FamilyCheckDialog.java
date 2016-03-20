package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

public class FamilyCheckDialog extends CheckDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String[] columnToolTips = {"Family #1 ONC Number", "Family #1 First Name", 
									   "Family # 1 Last Name", "Family #1 Street #",
									   "Family #1 Steet Name", "Result", "Family #2 ONC Number",
									   "Family #2 First Name", "Family # 2 Last Name", 
									   "Family #2 Street #", "Family #2 Steet Name"};
	
	private static String[] columns = {"ONC#", "First Name","Last Name", "St. #", "St. Name", "Result", 
								"ONC#", "First Name", "Last Name", "St. #", "St. Name"};
	
	private static int[] colWidths = {40, 88, 104, 48, 104, 68, 40, 88, 104, 48, 104};
	
	private static int[] colsCentered = {3, 5, 9};
	
	FamilyCheckDialog(JFrame pf)
	{
		super(pf, "Family", columnToolTips, columns, colWidths, colsCentered);

		//set up the sort criteria
		cbArray = new JCheckBox[5];
		cbArray[0] = new JCheckBox("First Name");
		cbArray[0].setSelected(true);
		cbArray[1] = new JCheckBox("Last Name");
		cbArray[1].setSelected(true);
		cbArray[2] = new JCheckBox("Partial Last Name");
		cbArray[2].setSelected(false);
		cbArray[3] = new JCheckBox("House #");
		cbArray[3].setSelected(false);
		cbArray[4] = new JCheckBox("Street");
		cbArray[4].setSelected(false);
		
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
    	else if(colName.equals("First Name"))	// Sort on HOH FN
    		Collections.sort(dupAL, new DupItemFamily1HOHFirstNameComparator());
    	else if(colName.equals("Last Name"))	//Sort on HOH LN
    		Collections.sort(dupAL, new DupItemFamily1HOHLastNameComparator());
    	else if(colName.equals("St. #"))	// Sort on House Num
    		Collections.sort(dupAL, new DupItemFamily1HouseNumComparator());
    	else if(colName.equals("St. Name"))	//Sort on Street
    		Collections.sort(dupAL, new DupItemFamily1StreetComparator());
        else
    		return;
	}

	boolean performDupCheck(boolean[] criteria)
	{
		DuplicateDataCheck datachecker = new DuplicateDataCheck();
		return datachecker.duplicateFamilyCheck(fDB.getList(), criteria, dupAL);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == btnPrint) { onPrintDataCheck("Family Data Check"); }
		else
		{
			if(ae.getSource() == cbArray[1] && cbArray[1].isSelected())
				cbArray[2].setSelected(false);
			else if(ae.getSource() == cbArray[2] && cbArray[2].isSelected())
				cbArray[1].setSelected(false);
		}
		
		buildTableList();
		
	}

	private class DupItemFamily1HOHFirstNameComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			return d1.getFamily1().getHOHFirstName().compareTo(d2.getFamily1().getHOHFirstName());
		}
	}
	
	private class DupItemFamily1HOHLastNameComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			return d1.getFamily1().getHOHLastName().compareTo(d2.getFamily1().getHOHLastName());
		}
	}
	private class DupItemFamily1HouseNumComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			String zHN1 = d1.getFamily1().getHouseNum().trim();
			String zHN2 = d2.getFamily1().getHouseNum().trim();
			
			//four cases. Both numeric, one numeric, one not, both non-numeric
			//house numbers that are numeric are always ordered before house numbers
			//than contain other non-numeric characters
			if(isNumeric(zHN1) && isNumeric(zHN2))
			{
				Integer hn1 = Integer.parseInt(zHN1);
				Integer hn2 = Integer.parseInt(zHN2);
				
				return hn1.compareTo(hn2);
			}
			else if(isNumeric(zHN1))
				return -1;	
			else if(isNumeric(zHN2))
				return 1;
			else
				return zHN1.compareTo(zHN2);
		}
	}
	private class DupItemFamily1StreetComparator implements Comparator<DupItem>
	{
		@Override
		public int compare(DupItem d1, DupItem d2)
		{
			return d1.getFamily1().getStreet().compareTo(d2.getFamily1().getStreet());
		}
	}
}
