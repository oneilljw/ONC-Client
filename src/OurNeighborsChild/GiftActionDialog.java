package OurNeighborsChild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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

public abstract class GiftActionDialog extends ONCTableDialog implements ActionListener, ListSelectionListener,
															DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_ROWS_TO_DISPLAY = 15;
	
	private GlobalVariables sdGVs;
	
	public JTable sortTable;
	private DefaultTableModel sortTableModel;
	private JTextField oncnumTF;
	private JComboBox startAgeCB, genderCB;
	private JButton btnResetCriteria;
	private JButton btnGiftAction, btnUndo; 
	
	private Families fDB;
	private ChildDB cDB;
	private ChildWishDB cwDB;
	private ONCWishCatalog cat;
	
	private ArrayList<GiftSortItem> stAL;
	protected LastWishChanged lastWishChanged;	//Holds the last wish received for undo function
	private boolean bChangingTable = false;	//Semaphore used to indicate the sort table is being changed
	private boolean bSortTableBuildRqrd = false;	//Used to determine a build to sort table is needed
	private boolean bResetInProcess = false;	//Prevents recursive build of sort table by event handlers during reset event
	private int sortStartAge = 0, sortGender = 0;
	private String sortONCNum = "";
	private static String[] genders = {"Any", "Boy", "Girl"};
	
	GiftActionDialog(JFrame pf, WishStatus dialogType)
	{
		super(pf);
		
		fDB = Families.getInstance();
		cDB = ChildDB.getInstance();
		cwDB = ChildWishDB.getInstance();
		cat = ONCWishCatalog.getInstance();
		sdGVs = GlobalVariables.getInstance();
		
		if(cDB != null)
			cDB.addDatabaseListener(this);	//Child updates
		if(cwDB != null)
			cwDB.addDatabaseListener(this);	//Wish updates
		
		//Create the class variables
		stAL = new ArrayList<GiftSortItem>();
		
		//set the title in accordance with the purpose
		this.setTitle(String.format("Our Neighbor's Child - %s Gifts", dialogType.presentTense()));
		
		//Set up the search criteria panel      
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
		JPanel cntlPanel = new JPanel(new FlowLayout());
		
		btnUndo = new JButton(sdGVs.getImageIcon(16));
        btnUndo.setToolTipText(String.format("Click to undo last gift %s", dialogType.toString()));
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
                    
        btnResetCriteria = new JButton("Reset Criteria");
        btnResetCriteria.addActionListener(this);        
        
        btnGiftAction = new JButton(String.format("%s Gift", dialogType.presentTense()));
        btnGiftAction.setEnabled(false);
        btnGiftAction.addActionListener(this);
        
        cntlPanel.add(btnUndo);
        cntlPanel.add(btnResetCriteria);
        cntlPanel.add(btnGiftAction);
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
	}
	
	void displaySortTable()
	{
		bChangingTable = true;	//don't process table messages while being changed
		
		while (sortTableModel.getRowCount() > 0)	//Clear the current table
			sortTableModel.removeRow(0);
		
		for(GiftSortItem si:stAL)	//Build the new table
			sortTableModel.addRow(si.getSortTableRow());
				
		bChangingTable = false;	
	}
	
	public void buildSortTableList()
	{
		stAL.clear();	//Clear the prior table information in the array list
		int index = 0;
		for(ONCFamily f:fDB.getList())
		{
			//ONC number is valid and matches criteria
			if(isNumeric(f.getONCNum()) && doesONCNumMatch(f.getONCNum()))	
			{
				for(ONCChild c:cDB.getChildren(f.getID()))
				{
					if(isAgeInRange(c) && doesGenderMatch(c))	//Children criteria pass
					{
						for(int i=0; i< cwDB.getNumberOfWishesPerChild(); i++)
						{	
							ONCChildWish cw = cwDB.getWish(c.getChildWishID(i));
							
							//Status matches and wish was assigned (Wish indicator is not *)
							if(cw != null && doesChildWishStatusMatch(cw))
							{
								stAL.add(new GiftSortItem(index++, f, c, cw));
							}
						}
					}
				}
			}
		}
		
		displaySortTable();		//Display the table after table array list is built	
	}
	
	void onGiftAction()
	{
		bChangingTable = true;
		
		int row_sel = sortTable.getSelectedRow();
		
		//Find child and wish number for selected
		int oncID = stAL.get(row_sel).getSortObjectFamily().getID();
		
		ONCFamily fam = fDB.searchForFamilyByID(oncID);
			
		ONCChild c = stAL.get(row_sel).getSortObjectChild();
		int wn = stAL.get(row_sel).getSortObjectChildWish().getWishNumber();
		ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));

		//Get current wish information
		int wishtypeid = cw.getWishID(); //Not implemented yet
//		String cwb = cat.getWishByID(cw.getWishID()).getName();
		String cwd = cw.getChildWishDetail();
		int cwi = cw.getChildWishIndicator();
		int cwaID = cw.getChildWishAssigneeID();
//		String cwaName = cw.getChildWishAssigneeName();
			
		//Process change to wish status. Store the new wish to be added in case of an undo operation 
		//and add the new wish to the child wish history
		lastWishChanged = new LastWishChanged(fam, c, cw);
			
		int wishid = cwDB.add(this, c.getID(), wishtypeid, cwd, wn, cwi, getGiftStatusAction(),
								cwaID, sdGVs.getUserLNFI(), sdGVs.getTodaysDate());
			
		c.setChildWishID(wishid, wn);				
				
		//Update the sort table itself
		buildSortTableList();
		
		sortTable.clearSelection();
		
		btnGiftAction.setEnabled(false);
		
		btnUndo.setEnabled(true);
		
		bChangingTable = false;
	}
	
	abstract int getGiftStatusAction();
	
	void onUndoReceiveGift()
	{
		bChangingTable = true;
		
		//To undo the wish, add the old wish back with the previous status		
		ONCChild lastChild = lastWishChanged.getLastChild();
		ONCChildWish lastWish = lastWishChanged.getLastWishReceived();
		
		int wishid = cwDB.add(this, lastWishChanged.getLastChild().getID(),
									lastWish.getWishID(),
									lastWish.getChildWishDetail(),
									lastWish.getWishNumber(),
									lastWish.getChildWishIndicator(),
									lastWish.getChildWishStatus(),
									lastWish.getChildWishAssigneeID(),
									sdGVs.getUserLNFI(),
									sdGVs.getTodaysDate());
		
		lastChild.setChildWishID(wishid, lastWish.getWishNumber());
		
		//Update the receive gifts sort table itself
		buildSortTableList();
				
		//check to see if family status should change as well. If the family status had
		//changed to gifts received with the receive that is being undone, the family 
		//status must be reset to the prior status.
		if(changeFamilyStatus())
			lastWishChanged.getLastFamily().setFamilyStatus(lastWishChanged.getLastFamilyStatus());
		
		sortTable.clearSelection();
		
		btnGiftAction.setEnabled(false);
		
		btnUndo.setEnabled(false);
		
		bChangingTable = false;
	}
	
	abstract boolean changeFamilyStatus();
	
	void checkReceiveGiftEnabled()
	{
//		System.out.println(String.format("Checking enabling of Apply Changes button: %d, %d, %d, %d", 
//				sortTable.getSelectedRowCount(), changeResCB.getSelectedIndex(),
//				changeStatusCB.getSelectedIndex(), changeAssigneeCB.getSelectedIndex()));
		
		if(sortTable.getSelectedRows().length > 0)	
			btnGiftAction.setEnabled(true);
		else
			btnGiftAction.setEnabled(false);
		
	}
	
	ListSelectionModel getSortWishTableLSM() { return sortTable.getSelectionModel(); }
	
	int getSelectedSortItemONCID(){return stAL.get(sortTable.getSelectedRow()).getSortObjectFamily().getID();}
	
	public ONCChild getSelectedSortItemONCChild()	{return stAL.get(sortTable.getSelectedRow()).getSortObjectChild();}

	
	boolean isSortTableChanging() { return bChangingTable; }
	
	private boolean isAgeInRange(ONCChild c)
	{
		return sortStartAge == 0 || c.getChildIntegerAge() == startAgeCB.getSelectedIndex()-1;		
	}
	
	private boolean doesGenderMatch(ONCChild c)
	{
		return sortGender == 0 || (c.getChildGender().equalsIgnoreCase(genders[sortGender]));		
	}
	
	abstract boolean doesChildWishStatusMatch(ONCChildWish cw);
//	{
//		return cw.getChildWishStatus() < CHILD_WISH_STATUS_RECEIVED;
//	}
	
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
			
			oncnumTF.setText("");	//its a text field, not a cb, so need to clear sortONCNum also
			sortONCNum = "";
			
			startAgeCB.setSelectedIndex(0);	//Will trigger the CB event handler which
											//will determine if the CB changed. Therefore,
			genderCB.setSelectedIndex(0);	//no need to test for a change here.
			
				
			bSortTableBuildRqrd = true;	//Force a table build to reset order change from header click
			bResetInProcess = false;	//Restore sort table build by event handlers
		}
		else if(e.getSource() == btnGiftAction)
		{
			//Receive gifts dialog recGift button event handler is in this class for coordination
			//with update of the child panel and family status
			onGiftAction();
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
			buildSortTableList();
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
			ONCFamily fam = stAL.get(sortTable.getSelectedRow()).getSortObjectFamily();
			ONCChild child = stAL.get(sortTable.getSelectedRow()).getSortObjectChild();
			fireEntitySelected(this, "WISH_SELECTED", fam, child);
			sortTable.requestFocus();
		}
		
		checkReceiveGiftEnabled();	//Check to see if user postured to change status or assignee.
	}

	private class GiftSortItem extends ONCObject
	{
		private ONCFamily	soFamily;
		private ONCChild	soChild;
		private ONCChildWish soChildWish;
		
		public GiftSortItem(int id, ONCFamily fam, ONCChild c, ONCChildWish cw)
		{
			super(id);
			soFamily = fam;
			soChild = c;
			soChildWish = cw;
		}
		
		//getters
		ONCFamily getSortObjectFamily() { return soFamily; }
		ONCChild getSortObjectChild() { return soChild; }
		ONCChildWish getSortObjectChildWish() { return soChildWish; }
		
		public String[] getSortTableRow()
		{
			String[] sorttablerow = {soFamily.getONCNum(), soChild.getChildGender(),
										soChild.getChildAge(),
										cat.getWishByID(soChildWish.getWishID()).getName(),
										soChildWish.getChildWishDetail()};
			return sorttablerow;
		}

		@Override
		public String[] getExportRow() {
			//Not used for Gift Actions
			return null;
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
				buildSortTableList();
			}	
		}
	 }
	 
	 /************************************************************************************
	  * This class defines the object used to store the last wish received by the Receive
	  * Gifts dialog user in support of the undo function.
	  * @author John O'Neill
	  ************************************************************************************/
	 class LastWishChanged
	 {
		 private ONCFamily fam;
		 private int lastFamilyStatus;
		 private ONCChild child;
		 private ONCChildWish lastWishReceived;
		 
		 LastWishChanged(ONCFamily f, ONCChild c, ONCChildWish lwr)
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
		if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_CHILD") || 
										dbe.getType().equals("DELETED_CHILD")
//										 dbe.getType().equals("WISH_ADDED")
										 ))
		{
			buildSortTableList();
		}		
		
	}
}
