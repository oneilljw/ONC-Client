package OurNeighborsChild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class ReceiveGiftsDialog extends ONCSortTableDialog implements ActionListener, ListSelectionListener,
															DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int CHILD_WISH_STATUS_RECEIVED = 4;
	private static final int FAMILY_STATUS_GIFTS_RECEIVED = 3;
	private static final int CHILD_WISH_IND_NOT_ASSIGNED = 1;
	private static final int NUM_ROWS_TO_DISPLAY = 15;
	
	private JFrame parentFrame;
	private GlobalVariables sdGVs;
	
	public JTable sortTable;
	private DefaultTableModel sortTableModel;
	private JTextField oncnumTF;
	private JComboBox startAgeCB, genderCB;
	private JButton btnResetCriteria;
	private JButton btnReceiveGift, btnUndo; 
	
	private Families fDB;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ArrayList<ONCRecGiftSortItem> stAL;
	private LastWishReceived lastWishReceived;	//Holds the last wish received for undo function
	private boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
//	private boolean bChildDataChanged = false;
	private int sortStartAge = 0, sortGender = 0;
	private String sortONCNum = "";
	private static String[] genders = {"Any", "Boy", "Girl"};
	private static String [] status = {"Any", "Empty", "Selected", "Assigned", "Received",
										"Distributed", "Verified"};
	String[] ages = {"Any", "<1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
			"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
	
	ReceiveGiftsDialog(JFrame pf)
	{
		super(pf);
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		sdGVs = GlobalVariables.getInstance();
		parentFrame = sdGVs.getFrame();
		
		cDB.addDatabaseListener(this);	//Child updates
		cwDB.addDatabaseListener(this);	//Wish updates
		
		//Create the class variables
		stAL = new ArrayList<ONCRecGiftSortItem>();
		
		this.setTitle("Our Neighbor's Child - Receive Gifts");
		
		//Set up the search criteria panel      
//        JPanel sortCriteriaPanel = new JPanel();
//        sortCriteriaPanel.setLayout(new BoxLayout(sortCriteriaPanel, BoxLayout.Y_AXIS));
		JPanel sortCriteriaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));		
    	JLabel lblONCicon = new JLabel(sdGVs.getImageIcon(0));
    	
    	oncnumTF = new JTextField();
    	oncnumTF.setEditable(true);
    	oncnumTF.setPreferredSize(new Dimension(88,56));
		oncnumTF.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncnumTF.setToolTipText("Type ONC Family # and press <enter>");
		oncnumTF.addActionListener(this);
		oncnumTF.addKeyListener(new ONCNumberKeyListener());
    	
    	String[] ages = {"Any", "<1", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
				"11","12", "13", "14", "15", "16", "17", "18", "19", "20", "21"};
		startAgeCB = new JComboBox(ages);
		startAgeCB.setBorder(BorderFactory.createTitledBorder("Child Age"));
		startAgeCB.setToolTipText("Select or Type Child's Age and press <enter>");
		startAgeCB.setPreferredSize(new Dimension(88,56));
		startAgeCB.setEditable(true);
		startAgeCB.addActionListener(this);
		
		genderCB = new JComboBox(genders);
		genderCB.setBorder(BorderFactory.createTitledBorder("Gender"));
		genderCB.setToolTipText("Select Child's Gender");
		genderCB.setSize(new Dimension(96,56));
		genderCB.addActionListener(this);
		
		sortCriteriaPanel.add(lblONCicon);
		sortCriteriaPanel.add(oncnumTF);
		sortCriteriaPanel.add(startAgeCB);
		sortCriteriaPanel.add(genderCB);
		
//		sortCriteriaPanel.add(sortCriteriaPanel);
		sortCriteriaPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
		
		//Set up the sort table panel
//		JPanel sortTablePanel = new JPanel();
		
		sortTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			//Implement table header tool tips.
			protected String[] columnToolTips = {"ONC Family Number", "Child Gender", "Child's Age",  
													"Wish Selected for Child", "Wish Detail"};
			
			public boolean getScrollableTracksViewportWidth()
	        {
				return getPreferredSize().width < getParent().getWidth();
	        }
			
		    protected JTableHeader createDefaultTableHeader()
		    {
		        return new JTableHeader(columnModel)
		        {
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e)
		            {
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return columnToolTips[realIndex];
		            }
		        };
		    }
	    
			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
			{
			  Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			  		 
			  if(isRowSelected(Index_row))
				  comp.setBackground(comp.getBackground());
			  else if (Index_row % 2 == 1)			  
				  comp.setBackground(new Color(240,248,255));
			  else
				  comp.setBackground(Color.white);
			  
			  return comp;
			}
			
		};

		String[] columns = {"ONC", "Gend", "Age", "Wish Type", "Details"};
        sortTableModel = new DefaultTableModel(columns, 0)
        {
        	private static final long serialVersionUID = 1L;
            @Override
            //All cells are locked from being changed by user
            public boolean isCellEditable(int row, int column) {return false;}
        };
     
        
        sortTable.setModel(sortTableModel);
        sortTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
	    sortTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF);
        
	  //Set table column widths
	  		int tablewidth = 0;
	  		int[] colWidths = {40, 48, 72, 120, 248};
	  		for(int i=0; i < colWidths.length; i++)
	  		{
	  			sortTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
	  			tablewidth += colWidths[i];
	  		}
	  		tablewidth += 24; 	//Account for vertical scroll bar
       
        
        JTableHeader anHeader = sortTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //Center cell entries for Wish Restrictions
//        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
//    	dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//        sortTable.getColumnModel().getColumn(5).setCellRenderer(dtcr);
        
        sortTable.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        sortTable.setFillsViewportHeight(true);
        
        sortTable.getSelectionModel().addListSelectionListener(this);
        
        //Create the scroll pane and add the table to it.
        JScrollPane sortScrollPane = new JScrollPane(sortTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
        												JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        sortScrollPane.setPreferredSize(new Dimension(tablewidth, sortTable.getRowHeight()*NUM_ROWS_TO_DISPLAY));
//      sortTablePanel.add(sortScrollPane);
		
        //Set up the button control panel
		JPanel cntlPanel = new JPanel();
		
		btnUndo = new JButton(sdGVs.getImageIcon(16));
        btnUndo.setToolTipText("Click to undo last gift received");
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
                    
        btnResetCriteria = new JButton("Reset Criteria");
        btnResetCriteria.addActionListener(this);        
        
        btnReceiveGift = new JButton("Receive Gift");
        btnReceiveGift.setEnabled(false);
        btnReceiveGift.addActionListener(this);
        
        cntlPanel.add(btnUndo);
        cntlPanel.add(btnResetCriteria);
        cntlPanel.add(btnReceiveGift);
        cntlPanel.setMaximumSize(new Dimension(tablewidth, 32));
        
        sortCriteriaPanel.setMaximumSize(new Dimension(tablewidth, 44));
         
        //Add the components to the frame pane
//      this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS)); 
        this.add(sortCriteriaPanel);
        this.add(sortScrollPane);
        this.add(cntlPanel);
       
        pack();
        setSize(tablewidth, 400);
        setResizable(true);
        Point pt = parentFrame.getLocation();
        setLocation(pt.x + parentFrame.getWidth() - tablewidth, pt.y + 20);
	}
	
	void displaySortTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		while (sortTableModel.getRowCount() > 0)	//Clear the current table
			sortTableModel.removeRow(0);
		
		for(ONCRecGiftSortItem si:stAL)	//Build the new table
			sortTableModel.addRow(si.getSortTableRow());
				
		bChangingTable = false;	
	}
	
	public void buildSortTableArrayList()
	{
		stAL.clear();	//Clear the prior table information in the array list
		
		for(ONCFamily f:fDB.getList())
		{
			//ONC number is valid and matches criteria
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()))	
			{
//				for(ONCChild c:f.getChildArrayList())
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c))	//Children criteria pass
					{
						for(int i=0; i< cwDB.getNumberOfWishesPerChild(); i++)
						{	
							ONCChildWish cw = cwDB.getWish(c.getChildWishID(i));
							
							//Status matches and wish was assigned (Wish indicator is not *)
							if(cw != null && cw.getChildWishStatus() < CHILD_WISH_STATUS_RECEIVED && 
								cw.getChildWishIndicator() != CHILD_WISH_IND_NOT_ASSIGNED)
							{
								stAL.add(new ONCRecGiftSortItem(f, f.getID(),f.getONCNum(), c.getChildAge(), i, 
										c, c.getChildDOB(), c.getChildGender(),
										cw.getChildWishIndicator(), cw.getChildWishBase(),
										cw.getChildWishDetail(), status[cw.getChildWishStatus()],
										cw.getChildWishAssigneeName()));
							}
						}
					}
				}
			}
		}
		
		displaySortTable();		//Display the table after table array list is built	
	}
	
	void onReceiveGift()
	{
		bChangingTable = true;
		
		int row_sel = sortTable.getSelectedRow();
		
			//Find child and wish number for selected
			int oncID = stAL.get(row_sel).getSortItemONCID();
		
			ONCFamily fam = fDB.searchForFamilyByID(oncID);
			
			ONCChild c = stAL.get(row_sel).getSortItemChild();
			int wn = stAL.get(row_sel).getSortItemChildWishNumber();
			ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));

			//Get current wish information
			String cwb = cw.getChildWishBase();
			String cwd = cw.getChildWishDetail();
			int cwi = cw.getChildWishIndicator();
			int cwaID = cw.getChildWishAssigneeID();
			String cwaName = cw.getChildWishAssigneeName();
			
			//Process change to wish status
			//Store the new wish to be added in case of an undo operation and add the new wish 
			//to the child wish history
			lastWishReceived = new LastWishReceived(fam, c, cw);
			
			int wishtypeid = 0; //Not implemented yet
			int wishid = cwDB.add(c.getID(), wishtypeid, cwb, cwd, wn, cwi, CHILD_WISH_STATUS_RECEIVED,
										cwaID, cwaName, sdGVs.getUserLNFI(), sdGVs.getTodaysDate());
			
			c.setChildWishID(wishid, wn);				
				
			//Update the sort table itself
			buildSortTableArrayList();
				
			//check to see if family status should change as well. Family status will change 
			//automatically when the last child wish status changes if status change rules are met
			//the getLowestFamilyStatus() method implements those rules. 
//			int lowestfamilystatus = fDB.getLowestFamilyStatus(fam, cDB, cwDB);
//			if(fam.getFamilyStatus() < lowestfamilystatus)
//				fam.setFamilyStatus(lowestfamilystatus);
			
		sortTable.clearSelection();
		
		btnReceiveGift.setEnabled(false);
		
		btnUndo.setEnabled(true);
		
		bChangingTable = false;
	}
	
	void onUndoReceiveGift()
	{
		bChangingTable = true;
		
		//To undo the wish, add the old wish back with the previous status		
		ONCChild lastChild = lastWishReceived.getLastChild();
		ONCChildWish lastWish = lastWishReceived.getLastWishReceived();
		
		int wishid = cwDB.add(lastWishReceived.getLastChild().getID(),
									lastWish.getWishID(),
									lastWish.getChildWishBase(),
									lastWish.getChildWishDetail(),
									lastWish.getWishNumber(),
									lastWish.getChildWishIndicator(),
									lastWish.getChildWishStatus(),
									lastWish.getChildWishAssigneeID(),
									lastWish.getChildWishAssigneeName(),
									sdGVs.getUserLNFI(),
									sdGVs.getTodaysDate());
		
		lastChild.setChildWishID(wishid, lastWish.getWishNumber());
		
		//Update the receive gifts sort table itself
		buildSortTableArrayList();
				
		//check to see if family status should change as well. If the family status had
		//changed to gifts received with the receive that is being undone, the family 
		//status must be reset to the prior status.
		if(lastWishReceived.getLastFamily().getFamilyStatus() == FAMILY_STATUS_GIFTS_RECEIVED)
			lastWishReceived.getLastFamily().setFamilyStatus(lastWishReceived.getLastFamilyStatus());
		
		sortTable.clearSelection();
		
		btnReceiveGift.setEnabled(false);
		
		btnUndo.setEnabled(false);
		
		bChangingTable = false;
	}
	
	void checkReceiveGiftEnabled()
	{
//		System.out.println(String.format("Checking enabling of Apply Changes button: %d, %d, %d, %d", 
//				sortTable.getSelectedRowCount(), changeResCB.getSelectedIndex(),
//				changeStatusCB.getSelectedIndex(), changeAssigneeCB.getSelectedIndex()));
		
		if(sortTable.getSelectedRows().length > 0)	
			btnReceiveGift.setEnabled(true);
		else
			btnReceiveGift.setEnabled(false);
		
	}
	
	ListSelectionModel getSortWishTableLSM() { return sortTable.getSelectionModel(); }
	
	int getSelectedSortItemONCID(){return stAL.get(sortTable.getSelectedRow()).getSortItemONCID();}
	
	public ONCChild getSelectedSortItemONCChild()	{return stAL.get(sortTable.getSelectedRow()).getSortItemChild();}

	
	boolean isSortTableChanging() { return bChangingTable; }
	
	private boolean isAgeInRange(ONCChild c)
	{
		return sortStartAge == 0 || c.getChildIntegerAge() == startAgeCB.getSelectedIndex()-1;		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	private boolean doesONCNumMatch(String s) { return sortONCNum.isEmpty() || sortONCNum.equals(s); }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == startAgeCB && startAgeCB.getSelectedIndex() != sortStartAge)
		{
			sortStartAge = startAgeCB.getSelectedIndex();
			sortONCNum = oncnumTF.getText();	//TF might have changed, without enter key
			bSortTableBuildRqrd = true;			
		}		
		else if(e.getSource() == genderCB && genderCB.getSelectedIndex() != sortGender)
		{
			sortGender = genderCB.getSelectedIndex();
			sortONCNum = oncnumTF.getText();	//TF might have changed, without enter key
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == oncnumTF && !sortONCNum.equals(oncnumTF.getText()))
		{
			sortONCNum = oncnumTF.getText();
			bSortTableBuildRqrd = true;
		}
		else if(e.getSource() == btnResetCriteria)
		{
			bResetInProcess = true;	//Prevent building of new sort table by event handlers
			
			oncnumTF.setText("");	//its a text field, not a cb, so need to sclear sortONCNum also
			sortONCNum = "";
			
			startAgeCB.setSelectedIndex(0);	//Will trigger the CB event handler which
											//will determine if the CB changed. Therefore,
			genderCB.setSelectedIndex(0);	//no need to test for a change here.
			
				
			bSortTableBuildRqrd = true;	//Force a table build to reset order change from header click
			bResetInProcess = false;	//Restore sort table build by event handlers
		}
		else if(e.getSource() == btnReceiveGift)
		{
			//Receive gifts dialog recGift button event handler is in this class for coordination
			//with update of the child panel and family status
			onReceiveGift();
		}
		else if(e.getSource() == btnUndo)
		{
			//Receive gifts dialog recGift button event handler is in this class for coordination
			//with update of the child panel and family status
			onUndoReceiveGift();
		}
		
		//Only build one time if multiple changes occur, i.e. Reset Button event
		if(bSortTableBuildRqrd && !bResetInProcess )
		{
			buildSortTableArrayList();
			bSortTableBuildRqrd = false;
		}
		
//		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee. 
	}
	
	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && lse.getSource() == sortTable.getSelectionModel() &&
				!bChangingTable)
		{
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getSortItemFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getSortItemChild();
			fireDataChanged(this, "WISH_SELECTED", fam, child);
		}
		
		checkReceiveGiftEnabled();	//Check to see if user postured to change status or assignee.
	}

	private class ONCRecGiftSortItem
	{
		private ONCFamily	sortItemFamily;
		private int			sortItemONCID;
		private String 		sortItemoncFamNum;
		private ONCChild	sortItemChild;
		private int			sortItemchildWishNum;
		private String 		sortItemchildAge;
//		private Calendar	sortItemchildDateOfBirth;
		private String 		sortItemchildGender;
//		private String		sortItemchildWishInd;
		private String		sortItemchildWishBase;
		private String 		sortItemchildWishDetail;
//		private String 		sortItemchildWishStatus;
//		private String		sortItemchildWishAssignee;
		
		public ONCRecGiftSortItem(ONCFamily fam, int id, String fnum, String ca, int cwn, ONCChild c, Calendar dob, String cg, 
								int cwi, String cwb, String cwd,
								String cws, String cwa)
		{
			sortItemFamily = fam;
			sortItemONCID = id;
			sortItemoncFamNum = fnum;
			sortItemChild = c;
			sortItemchildWishNum = cwn;
			sortItemchildGender = cg;
			sortItemchildAge = ca.trim();	
//			sortItemchildDateOfBirth = dob;
//			if(cwi == 2)
//				sortItemchildWishInd = "#";
//			else if(cwi == 1)
//				sortItemchildWishInd = "*";
//			else
//				sortItemchildWishInd = "";
			sortItemchildWishBase = cwb;
			sortItemchildWishDetail = cwd.trim();
//			sortItemchildWishStatus = cws;
//			sortItemchildWishAssignee = cwa;
		}
		
		//getters
		ONCFamily getSortItemFamily() { return sortItemFamily; }
		int getSortItemONCID() { return sortItemONCID; }
		ONCChild getSortItemChild() { return sortItemChild; }
		Integer	getSortItemChildWishNumber() { return sortItemchildWishNum; }
		
		public String[] getSortTableRow()
		{
			String[] sorttablerow = {sortItemoncFamNum, sortItemchildGender,
										sortItemchildAge, sortItemchildWishBase,
										sortItemchildWishDetail};
			return sorttablerow;
		}
	}
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	/***********************************************************************************
	 * This class implements a key listener for the ReceiveGiftDialog class that
	 * listens to the ONC Number text field to determine when it is empty. If it becomes empty,
	 * the listener rebuilds the sort table array list
	 ***********************************************************************************/
	 private class ONCNumberKeyListener implements KeyListener
	 {
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub
				
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{
			if(oncnumTF.getText().isEmpty())
			{
				sortONCNum = "";
				buildSortTableArrayList();
			}	
		}
	 }
	 
	 /************************************************************************************
	  * This class defines the object used to store the last wish received by the Receive
	  * Gifts dialog user in support of the undo function.
	  * @author John O'Neill
	  ************************************************************************************/
	 private class LastWishReceived
	 {
		 private ONCFamily fam;
		 private int lastFamilyStatus;
		 private ONCChild child;
		 private ONCChildWish lastWishReceived;
		 
		 LastWishReceived(ONCFamily f, ONCChild c, ONCChildWish lwr)
		 {
			 fam = f;
			 lastFamilyStatus = f.getFamilyStatus();
			 child = c;
			 lastWishReceived = lwr;
		 }
		 
		 //getters
		 ONCFamily getLastFamily() { return fam; }
		 int getLastFamilyStatus() { return lastFamilyStatus; }
		 ONCChild getLastChild() { return child; }
		 ONCChildWish getLastWishReceived() { return lastWishReceived; }
	 }

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && (dbe.getType().equals("WISH_ADDED") ||
										dbe.getType().equals("UPDATED_CHILD") || 
										 dbe.getType().equals("DELETED_CHILD")))
		{
			buildSortTableArrayList();
		}		
		
	}
}
