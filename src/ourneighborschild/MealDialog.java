package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class MealDialog extends JDialog implements ActionListener, DatabaseListener,
													EntitySelectionListener
{
	/**
	 * This class implements a dialog which allows the user to see the history of meals
	 * requested by a family. Only the most recent meal is editable. Meals cannot be edited
	 * they have been referred to a partner. A meal can be deleted.
	 */
	private static final long serialVersionUID = 1L;
	private static final int HOLIDAY_COL= 0;
	private static final int RESTRICTIONS_COL = 1;
	private static final int STATUS_COL = 2;
	private static final int PARTNER_COL = 3;
	private static final int CHANGED_BY_COL = 4;
	private static final int DATE_CHANGED_COL = 5;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnDelete, btnPrint;
	
	private MealDB mealDB;
	private ONCOrgs partnerDB;
	
	private List<ONCMeal> mealList;	//list of meals for current family
	private Comparator<ONCMeal> mealDateChangedComparator;
	private ONCFamily currFam; 	//current family
	
	public MealDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Family Meal History");
		
		//Save the reference to the family and meal databases.
		Families familyDB = Families.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		mealDB = MealDB.getInstance();
		if(mealDB != null)
			mealDB.addDatabaseListener(this);
		
		partnerDB = ONCOrgs.getInstance();
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		
		mealList =  new ArrayList<ONCMeal>();
		mealDateChangedComparator = new MealDateChangedComparator();
		
		//Create the meal table model
		dlgTableModel = new DialogTableModel();
		
		//create the catalog table
		String[] colToolTips = {"When is meal requested","Family Dietary Restrictions",
								"Status of request", "Partner meal referred to",
								"Elf who last changed the meal", "When meal was last changed"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		TableColumn holidayColumn = dlgTable.getColumnModel().getColumn(HOLIDAY_COL);
		JComboBox comboBox = new JComboBox(MealType.getSelectionList());
		holidayColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {80, 160, 72, 160, 96, 104};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
//      dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify columns
//      DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//      dtcr.setHorizontalAlignment(SwingConstants.LEFT);
//      dlgTable.getColumnModel().getColumn(PARTNER_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 96));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        btnPrint = new JButton("Print Meal History");
        btnPrint.setToolTipText("Print the meal history");
        btnPrint.setEnabled(false);
        btnPrint.addActionListener(this);
        
        btnDelete = new JButton("Delete Family Meal");
        btnDelete.setToolTipText("Remove meal request from family");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(this);
          
        cntlPanel.add(btnDelete);
        cntlPanel.add(btnPrint);
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
  //    this.setMinimumSize(new Dimension(tablewidth, 240));
	}
	
	void setFamilyToDisplay(ONCFamily family)
	{
		this.currFam = family;
		setDialogTitle();
		mealList = getSortedMealList();
		dlgTableModel.fireTableDataChanged();
	}
	
	List<ONCMeal> getSortedMealList()
	{
		List<ONCMeal> familyMealList = mealDB.getFamilyMealHistory(currFam.getID());
		Collections.sort(familyMealList, mealDateChangedComparator);
		
		return familyMealList;
	}
	
	void setDialogTitle()
	{
		if(GlobalVariables.isUserAdmin())
			this.setTitle(currFam.getHOHLastName() +" Family Meal History");
		else
			this.setTitle("ONC# " + currFam.getONCNum() + " Family Meal History");
	}
	
	void addMeal(MealType holiday, String restrictions)
	{
		ONCMeal addMealReq = new ONCMeal(-1, currFam.getID(), holiday,
									restrictions, mealList.get(0).getPartnerID(),
									GlobalVariables.getUserLNFI(), new Date(),
									mealList.get(0).getStoplightPos(),
									mealList.get(0).getStoplightMssg(),
									mealList.get(0).getStoplightChangedBy());
		
		ONCMeal addedMeal = mealDB.add(this, addMealReq);
		
		if(addedMeal != null)
		{
			mealList = getSortedMealList();
			dlgTableModel.fireTableDataChanged();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_MEAL") ||
										dbe.getType().equals("DELETED_MEAL")))
		{
			//update the meal table if added meal was for this family
			ONCMeal addedMeal = (ONCMeal) dbe.getObject();
			if(currFam != null && addedMeal.getFamilyID() == currFam.getID())
			{
				mealList = getSortedMealList();
				dlgTableModel.fireTableDataChanged();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFamily = (ONCFamily) dbe.getObject();
			if(currFam != null && currFam.getID() == updatedFamily.getID())
			{
				currFam = updatedFamily;
				setDialogTitle();
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CONFIRMED_PARTNER_NAME"))
		{
			//refresh the table, no new data, just partner name change
			if(!mealList.isEmpty())
				dlgTableModel.fireTableDataChanged();
		}		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			currFam = (ONCFamily) tse.getObject1();
			if(currFam != null)
			{
				setDialogTitle();
				mealList = getSortedMealList();
				//update the table for new family selection
				dlgTableModel.fireTableDataChanged();	
			}	
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Meal History Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"Holiday", "Dietary Restrictions", "Status",
										"Meal Partner", "Changed By", "Last Changed"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return mealList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	Object value;
        	
        	ONCMeal meal = mealList.get(row);
        	
        	if(col == HOLIDAY_COL)
        		value = meal.getType().toString();
        	else if(col == RESTRICTIONS_COL)  
        		value = meal.getRestricitons();
        	else if (col == STATUS_COL)
        	{
        		if(meal.getPartnerID() > -1)
        			value = MealStatus.Referred.toString();
        		else if(meal.getPartnerID() == -1 && currFam.getMealID() > -1)
        			value = MealStatus.Requested.toString();
        		else
        			value = "Deleted";
        	}
        	else if (col == PARTNER_COL)
        	{
        		if(meal.getPartnerID() > -1)
        		{
        			Organization partner = partnerDB.getOrganizationByID(meal.getPartnerID());
        			value = partner.getName();
        		}
        		else
        			value = "";
        	}
        	else if (col == CHANGED_BY_COL)
        		value = meal.getChangedBy();
        	else if (col == DATE_CHANGED_COL)
        	{
        		SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yy H:mm");
        		value = sdf.format(meal.getDateChanged());
        	}
        	else
        		value = "Error";
        	
//        	System.out.println(String.format("MealDlg.getValueAt: row=%d, col=%d, mealID=%d, value= %s",
//					row, col, meal.getID(), (String)value));

        	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == HOLIDAY_COL)
        		return MealType.class;
        	else
        		return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
            //Only the check boxes can be edited and then only if there is not
        	//a wish already selected from the list associated with that column
//        	System.out.println(String.format("MealDlg.isCellEditable: currFam MealStatus: %s", currFam.getMealStatus().toString()));
        	if(row == 0 && (col == HOLIDAY_COL || col == RESTRICTIONS_COL) &&
        			currFam.getMealStatus() != MealStatus.Referred)
        		return true;
        	else
        		return false;
        }

        public void setValueAt(Object value, int row, int col)
        { 
        	//verify user changed the meal type or restrictions and if so,
        	//add a new meal to the history and redisplay. Table row 0 always
        	//contains the most recent meal
        	if(!mealList.isEmpty() && row == 0 && col == 0 &&
        		(MealType) value != mealList.get(0).getType())
        	{
        		addMeal((MealType)value, mealList.get(0).getRestricitons());
        	}
        	else if(!mealList.isEmpty() && row == 0 && col == 1 &&
        				!mealList.get(0).getRestricitons().equals((String)value))
        	{
        		addMeal(mealList.get(0).getType(), (String)value);
        	}
        }
    }
	
	private class MealDateChangedComparator implements Comparator<ONCMeal>
	{
		@Override
		public int compare(ONCMeal o1, ONCMeal o2)
		{
			return o2.getDateChanged().compareTo(o1.getDateChanged());
		}
	}
}
