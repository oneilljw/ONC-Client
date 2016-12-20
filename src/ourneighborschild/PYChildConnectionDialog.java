package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;

public class PYChildConnectionDialog extends JDialog implements ActionListener, 
																DatabaseListener, EntitySelectionListener
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int SORT_TABLE_VERTICAL_SCROLL_WIDTH = 24;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final int NUMBER_OF_PRIOR_YEARS = 2;
	
	private JTextField firstnameTF, lastnameTF, dobTF, genderTF, searchLastnameTF;
	private JComboBox searchGenderCB;
	private JDateChooser searchDobDC;
	private ONCTable resultTable;
	private DefaultTableModel resultDefaultTableModel;
	private JButton btnSearch, btnConnect;
	
	ONCFamily currFamily;
	ONCChild currChild;
	ONCPriorYearChild pyChild;
	ChildDB childDB;
	
	PYChildConnectionDialog(JFrame parentFrame)
	{
		super(parentFrame, false);
		this.setTitle("Connect Current Child & Prior Year Child");
		
		//connect to the child database
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		//create a reference to the content pane
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JPanel currChildPanel = new JPanel();
		currChildPanel.setLayout(new BoxLayout(currChildPanel, BoxLayout.X_AXIS));
		currChildPanel.setBorder(BorderFactory.createTitledBorder("Current Child"));
		
		//Set up the child info text fields and age label
        firstnameTF = new JTextField();
        firstnameTF.setPreferredSize(new Dimension(112, 48));
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.setEditable(false);
        
        lastnameTF = new JTextField();
        lastnameTF.setPreferredSize(new Dimension(120, 48));
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.setEditable(false);
        
        dobTF = new JTextField();
		dobTF.setMinimumSize(new Dimension(120, 48));
		dobTF.setBorder(BorderFactory.createTitledBorder("Date of Birth"));
		dobTF.setEditable(false);
	
        genderTF = new JTextField();
        genderTF.setPreferredSize(new Dimension(64, 48));
        genderTF.setBorder(BorderFactory.createTitledBorder("Gender"));
        genderTF.setEditable(false);
   
        //add the gui items to the child info panel             
        currChildPanel.add(firstnameTF);
        currChildPanel.add(lastnameTF);
        currChildPanel.add(dobTF);
        currChildPanel.add(genderTF);
        
        //Set up the py search for child panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search for Prior Year Child"));
        
        searchLastnameTF = new JTextField();
        searchLastnameTF.setPreferredSize(new Dimension(120, 48));
        searchLastnameTF.setBorder(BorderFactory.createTitledBorder("PY Last Name"));
        
        searchDobDC = new JDateChooser();
        searchDobDC.setMinimumSize(new Dimension(160, 48));
        searchDobDC.setBorder(BorderFactory.createTitledBorder("PY Date of Birth"));
        
        String[] genders = {"Boy", "Girl"};
        searchGenderCB = new JComboBox(genders);
        searchGenderCB.setPreferredSize(new Dimension(96, 48));
        searchGenderCB.setBorder(BorderFactory.createTitledBorder("PY Gender"));
   
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(this);
   
        //add the gui items to the search panel             
        searchPanel.add(searchLastnameTF);
        searchPanel.add(searchDobDC);
        searchPanel.add(searchGenderCB);
        searchPanel.add(btnSearch);
        
        //set up the search result table
        String[] colToolTips = {"Year", "Wish Number", "Gift Provided"};
        String[] columns =  {"Year", "Wish #", "Gift Provided"};
        resultTable = new ONCTable(colToolTips, new Color(240,248,255));

		//Set up the table model. Cells are not editable
		resultDefaultTableModel = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;
			@Override
			//All cells are locked from being changed by user
			public boolean isCellEditable(int row, int column) {return false;}
		};

		//Set the table model, select ability to select multiple rows and add a listener to 
		//check if the user has selected a row. 
		resultTable.setModel(resultDefaultTableModel);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Set table column widths
		int[] colWidths = {48, 36, 392};
		int tablewidth = 0;
		for(int i=0; i < colWidths.length; i++)
		{
			resultTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
			tablewidth += colWidths[i];
		}
		tablewidth += SORT_TABLE_VERTICAL_SCROLL_WIDTH; 	//Account for vertical scroll bar

		//Set up the table header
		JTableHeader anHeader = resultTable.getTableHeader();
		anHeader.setForeground( Color.black);
		anHeader.setBackground( new Color(161,202,241));

		//Center cell entries for specified cells. If parameter is null, no cells to cnter
        int[] center_cols = {1};
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<center_cols.length; i++)
        	resultTable.getColumnModel().getColumn(center_cols[i]).setCellRenderer(dtcr);

		resultTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		resultTable.setFillsViewportHeight(true);

		//Create the result scroll pane and add the table to it.
		JScrollPane resultScrollPane = new JScrollPane(resultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
											JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		resultScrollPane.setPreferredSize(new Dimension(tablewidth, 
				resultTable.getRowHeight()*((NUMBER_OF_WISHES_PER_CHILD * NUMBER_OF_PRIOR_YEARS)+2)));
        
		
        //set up the control panel
        JPanel cntlPanel = new JPanel();
        
        btnConnect = new JButton("Connect Children");
        btnConnect.setEnabled(false);
        btnConnect.addActionListener(this);
        
        cntlPanel.add(btnConnect);
        
        //add the sub panels
        contentPane.add(currChildPanel);
        contentPane.add(searchPanel);
        contentPane.add(resultScrollPane);
        contentPane.add(cntlPanel);
        
        pack();
        this.setMinimumSize(new Dimension(500, 200));
	}
	
	void display(ONCFamily f, ONCChild c)
	{
		if(f != null && c != null)
		{
			currFamily = f;
			currChild = c;
			
			firstnameTF.setText(c.getChildFirstName());
			lastnameTF.setText(c.getChildLastName());
			dobTF.setText(c.getChildDOBString("MM/dd/yyyy"));
			genderTF.setText(c.getChildGender());
		}
	}
	
	void displayResultTable(ONCPriorYearChild pyc)
	{
		pyChild = pyc;
		//clear the table
		while (resultDefaultTableModel.getRowCount() > 0)
			resultDefaultTableModel.removeRow(0);
		
		//check to see that pyChild isn't null, if it is, display not found message
		if(pyChild != null)
		{
			String[] pyWishes = pyChild.getPriorYearWishes();
			int year = GlobalVariables.getCurrentSeason() - 1;
			int wishnum = 0;
			
			int index = 0;
			while(index < NUMBER_OF_WISHES_PER_CHILD * NUMBER_OF_PRIOR_YEARS) //# of rows in table
			{
				String[] row = {Integer.toString(year), Integer.toString(wishnum+1), pyWishes[index++]};
				resultDefaultTableModel.addRow(row);
				wishnum++;
				
				if(index == NUMBER_OF_WISHES_PER_CHILD)
				{
					year--;
					wishnum = 0;
				}
			}
			
			btnConnect.setEnabled(true);
		}
		else
		{
			//build search failed message and display
			String[] row = {"", "", "Prior Year Child Not Found"};
			resultDefaultTableModel.addRow(row);
			btnConnect.setEnabled(false);
		}
	}
	
	void onSearchForPYChild()
	{
		//create the request. All fields must be complete,
		if(!searchLastnameTF.getText().isEmpty())
		{
			long pyChildDOB = convertCalendarDOBToGMT(searchDobDC.getCalendar());
			ONCChild searchPYChild = new ONCChild(-1, currFamily.getID(), "", searchLastnameTF.getText().trim(),
				(String)searchGenderCB.getSelectedItem(),  pyChildDOB, "", GlobalVariables.getCurrentSeason());
			ONCPriorYearChild pyChildReq = new ONCPriorYearChild(-1, searchPYChild);
		
			ONCPriorYearChild resultPYChild = null;
			resultPYChild = childDB.searchForPriorYearChild(pyChildReq);
		
			displayResultTable(resultPYChild);
		}
	}
	
	/****************************************************************************************
	 * Takes a local time zone Calendar date of birth and returns the date of birth 
	 * in milliseconds (GMT)
	 * Calendar object of the date of birth
	 * @param gmtDOB
	 * @return
	 ***************************************************************************************/
	public long convertCalendarDOBToGMT(Calendar dobCal)
	{
		//gives you the current offset in ms from GMT at the current date
		dobCal.set(Calendar.HOUR, 0);
		dobCal.set(Calendar.MINUTE, 0);
		dobCal.set(Calendar.SECOND, 0);
		dobCal.set(Calendar.MILLISECOND, 0);
		
		TimeZone tz = dobCal.getTimeZone();
		int offsetFromUTC = tz.getOffset(dobCal.getTimeInMillis());
		return dobCal.getTimeInMillis() + offsetFromUTC;
	}
	
	void clearChildData()
	{
		currChild = null;
		
		firstnameTF.setText("");
		lastnameTF.setText("");
		dobTF.setText("");
		genderTF.setText("");
	}
	
	void onConnectChildren()
	{
		//create a updateChild request object and set the prior year id
		ONCChild updateChildReq = new ONCChild(currChild);
		updateChildReq.setPriorYearChildID(pyChild.getID());
		
		//send the request
		String response = childDB.update(this, updateChildReq);
		
		
		if(response.startsWith("UPDATED_CHILD"))
		{
			//display a success message and disable the connect button
			String mssg = String.format("%s %s and prior year %s connected",
					currChild.getChildFirstName(), currChild.getChildLastName(), pyChild.getLastName());
			
			JOptionPane.showMessageDialog(this, mssg, 
					"Children Connected", JOptionPane.INFORMATION_MESSAGE,
					GlobalVariables.getInstance().getImageIcon(0));
		}
		else
		{
			//display an error message that update request failed
			JOptionPane.showMessageDialog(this, "ONC Server denied connection," +
						"try again later","Child Connection Failed", JOptionPane.ERROR_MESSAGE,
						GlobalVariables.getInstance().getImageIcon(0));
		}
		
		btnConnect.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnSearch)
			onSearchForPYChild();
		else if(e.getSource() == btnConnect)
			onConnectChildren();
		
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType() == EntityType.FAMILY)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = childDB.getChildren(fam.getID());
			
			//check to see if there are children in the family, is so, display first child
			if(childList != null && !childList.isEmpty())
				display(fam, childList.get(0));
			else
				clearChildData();
		}
		else if(tse.getType() == EntityType.CHILD)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ONCChild child = (ONCChild) tse.getObject2();
			display(fam, child);
		}
		else if(tse.getType() == EntityType.WISH)
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ONCChild child = (ONCChild) tse.getObject2();
			display(fam, child);
		}
	}
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.WISH);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject1();
			if(updatedChild != null && updatedChild.getID() == currChild.getID())
				display(currFamily, updatedChild);
		}
		else if(dbe.getSource() != this && dbe.getType().equals("DELETED_CHILD"))
		{
			ONCChild deletedChild = (ONCChild) dbe.getObject1();
			if(deletedChild != null && deletedChild.getID() == currChild.getID())
				clearChildData();
		}
	}
}
