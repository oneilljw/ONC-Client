package ourneighborschild;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

public class MealDialog extends HistoryDialog
{
	/**
	 * This class implements a dialog which allows the user to see the history of meals
	 * requested by a family. Only the most recent meal is editable. Meals cannot be edited
	 * once they have been referred to a partner. A meal can be deleted.
	 */
	private static final long serialVersionUID = 1L;
	private static final int HOLIDAY_COL= 0;
	private static final int RESTRICTIONS_COL = 1;
	private static final int STATUS_COL = 2;
	private static final int PARTNER_COL = 3;
	private static final int CHANGED_BY_COL = 4;
	private static final int DATE_CHANGED_COL = 5;
	
	private AbstractTableModel dlgTableModel;
	
	private FamilyDB familyDB;
	private MealDB mealDB;
	private PartnerDB partnerDB;
	
	private List<ONCMeal> mealList;	//list of meals for current family
	private Comparator<ONCMeal> mealDateChangedComparator;
	
	public MealDialog(JFrame pf)
	{
		super(pf, "Meals");
		this.setTitle("Family Meal History");
		
		//Save the reference to the family and meal databases.
		familyDB = FamilyDB.getInstance();
		if(familyDB != null)
			familyDB.addDatabaseListener(this);
		
		mealDB = MealDB.getInstance();
		if(mealDB != null)
			mealDB.addDatabaseListener(this);
		
		partnerDB = PartnerDB.getInstance();
		if(partnerDB != null)
			partnerDB.addDatabaseListener(this);
		
		mealList =  new ArrayList<ONCMeal>();
		mealDateChangedComparator = new MealDateChangedComparator();

		TableColumn holidayColumn = dlgTable.getColumnModel().getColumn(HOLIDAY_COL);
		JComboBox comboBox = new JComboBox(MealType.getSelectionList());
		holidayColumn.setCellEditor(new DefaultCellEditor(comboBox));
	}
	
	@Override
	void display(ONCObject family)
	{
		this.currFam = (ONCFamily) family;
		setDialogTitle();
		mealList = getSortedMealList();
		dlgTableModel.fireTableDataChanged();
		btnDelete.setEnabled(!mealList.isEmpty() && mealList.get(0).getPartnerID() == -1);
		
	}
	
	List<ONCMeal> getSortedMealList()
	{
		List<ONCMeal> familyMealList = mealDB.getFamilyMealHistory(currFam.getID());
		Collections.sort(familyMealList, mealDateChangedComparator);
		
		return familyMealList;
	}

	void addMeal(MealType holiday, String restrictions)
	{
		ONCMeal addMealReq = new ONCMeal(-1, currFam.getID(), mealList.get(0).getStatus(), 
									holiday,restrictions, mealList.get(0).getPartnerID(),
									userDB.getUserLNFI(), new Date(),
									mealList.get(0).getStoplightPos(),
									mealList.get(0).getStoplightMssg(),
									mealList.get(0).getStoplightChangedBy());
		
		ONCMeal addedMeal = mealDB.add(this, addMealReq);
		
		if(addedMeal != null)
		{
			mealList = getSortedMealList();
			dlgTableModel.fireTableDataChanged();
			btnDelete.setEnabled(!mealList.isEmpty() && mealList.get(0).getPartnerID() == -1);
		}
	}
	
	@Override
	void delete()
	{
		//Confirm with the user that the deletion is really intended
		String confirmMssg = String.format("<html>Are you sure you want to delete<br>the meal for family #%s </html>",
											currFam.getONCNum()); 
									
		Object[] options= {"Cancel", "Delete Meal"};
		JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
													GlobalVariables.getInstance().getImageIcon(0),
													options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(this, "*** Confirm Delete Meal ***");
		confirmDlg.setLocationRelativeTo(this);
		this.setAlwaysOnTop(false);
		confirmDlg.setVisible(true);

		Object selectedValue = confirmOP.getValue();
	
		//if the client user confirmed, delete the meal
		FamilyDB familyDB = FamilyDB.getInstance();
		if(selectedValue != null && selectedValue.toString().equals(options[1]))
		{
			ONCFamily delFamMealReq = new ONCFamily(currFam);
			delFamMealReq.setMealID(-1);
			delFamMealReq.setMealStatus(MealStatus.None);
		
			String response = familyDB.update(this,  delFamMealReq);
		
			if(!response.startsWith("UPDATED_FAMILY"))
			{
				//display an error message that update request failed
				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), 
					"ONC Server denied add meal request, try again later","Add Meal Failed",  
					JOptionPane.ERROR_MESSAGE, GlobalVariables.getInstance().getImageIcon(0));
			}
			else
				this.dispose();
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("ADDED_MEAL") ||
										dbe.getType().equals("DELETED_MEAL")))
		{
			//update the meal table if added meal was for this family
			ONCMeal addedMeal = (ONCMeal) dbe.getObject1();
			if(currFam != null && addedMeal.getFamilyID() == currFam.getID())
			{
				mealList = getSortedMealList();
				dlgTableModel.fireTableDataChanged();
				btnDelete.setEnabled(!mealList.isEmpty() && mealList.get(0).getPartnerID() == -1);
			}
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY"))
		{
			ONCFamily updatedFamily = (ONCFamily) dbe.getObject1();
			if(currFam != null && currFam.getID() == updatedFamily.getID())
			{
				if(updatedFamily.getMealID() == -1)
					this.dispose();
				else
					display(updatedFamily);
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
	AbstractTableModel createTableModel()
	{
		dlgTableModel = new DialogTableModel();
		return dlgTableModel;
	}
	
	@Override
	String[] getColumnToolTips() 
	{
		String[] colToolTips = {"When is meal requested","Family Dietary Restrictions",
				"Status of request", "Partner meal referred to",
				"Elf who last changed the meal", "When meal was last changed"};
		return colToolTips;
	}
	
	@Override
	int[] getColumnWidths()
	{
		int[] colWidths = {80, 160, 160, 160, 96, 104};;
		return colWidths;
	}

	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Meal History Dialog
		 */
		
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Holiday", "Dietary Restrictions", "Status",
										"Meal Partner", "Changed By", "Last Changed"};
		SimpleDateFormat sdf;
		
		public DialogTableModel()
		{
			sdf = new SimpleDateFormat("M/dd/yy H:mm");
		}
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return mealList == null ? 0 : mealList.size(); }
 
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
        		value = meal.getStatus().toString();
        	else if (col == PARTNER_COL)
        	{
        		if(meal.getPartnerID() > -1)
        		{
        			ONCPartner partner = partnerDB.getPartnerByID(meal.getPartnerID());
        			value = partner.getName();
        		}
        		else
        			value = "";
        	}
        	else if (col == CHANGED_BY_COL)
        		value = meal.getChangedBy();
        	else if (col == DATE_CHANGED_COL)
        		value = sdf.format(meal.getDateChanged());
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
        	if(row == 0 && (col == HOLIDAY_COL || col == RESTRICTIONS_COL) &&
        			mealList.get(0).getStatus().compareTo(MealStatus.Referred) < 0)
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
