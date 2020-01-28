package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.google.gson.Gson;

public class DNSCodeDialog extends EntityDialog implements DatabaseListener, ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int ACRONYM_COL = 0;
	private static final int NAME_COL = 1;
	private static final int CHANGED_BY_COL = 2;
	private static final int TIME_CHANGED_COL = 3;
	private static final int STOPLIGHT_COL = 4;
	
	private DNSCodeDB dnsCodeDB;
	
	private DNSCode currCode;
	
	private Mode mode;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private TitledBorder dnsCodeBorder, definitionBorder;
	private JTextField acronymTF, nameTF;
	private JTextPane definitionPane;
	
	DNSCodeDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Edit DNS Codes");
		
		//get data base references
		dnsCodeDB = DNSCodeDB.getInstance();
		if(dnsCodeDB != null)
			dnsCodeDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//initialize member variables
		currCode = null;
		mode = Mode.NORMAL;
		
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, dnsCodeDB);
        nav.setDefaultMssg("DNS Codes");
        nav.setCount1("DNS Codes: 0");
        nav.setCount2("");
		
		//set up the edit note user interface
		dnsCodeBorder = BorderFactory.createTitledBorder("DNS Codes");
        entityPanel.setBorder(dnsCodeBorder);
      
        DNSCodeChangeListener changeListener = new DNSCodeChangeListener();
        
        //set up the table panel
        dlgTableModel = new DialogTableModel();
        String[] colTT = {"Acronym for the DNS Code", "Name of the Code", "User who last changed the code",
        					 "Time code was  last changed", "DNS Code Stoplight Color"};
        
      	dlgTable = new ONCTable(dlgTableModel, colTT, new Color(240,248,255));
      	dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      	dlgTable.getSelectionModel().addListSelectionListener(this);
      	
      	//set up a cell renderer for the LAST_LOGINS column to display the date 
      	TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer()
      	{
      		private static final long serialVersionUID = 1L;
      		SimpleDateFormat f = new SimpleDateFormat("M/dd/yy h:mm a");

      		public Component getTableCellRendererComponent(JTable table, Object value,
      		            boolean isSelected, boolean hasFocus, int row, int column)
      		{ 
      		    if(value instanceof java.util.Date)
      		        value = f.format(value);
      		        
      		    return super.getTableCellRendererComponent(table, value, isSelected,
      		            hasFocus, row, column);
      		}
      	};
      	dlgTable.getColumnModel().getColumn(TIME_CHANGED_COL).setCellRenderer(tableCellRenderer);

      	//Set table column widths
      	int tablewidth = 0;
      	int[] colWidths = {64,184,144,112, 80};
      	for(int col=0; col < colWidths.length; col++)
      	{
      		dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
      		tablewidth += colWidths[col];
      	}
      	tablewidth += 24; 	//count for vertical scroll bar
      		
      	dlgTable.setAutoCreateRowSorter(true);	//add a sorter
              
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
              
        //justify columns
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
//      dlgTable.getColumnModel().getColumn(STOPLIGHT_COL).setCellRenderer(dtcr);
              
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setPreferredSize(new Dimension(tablewidth, 144));
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel dnsCodePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        //set up acronym and name panel
        JPanel acronymAndNamePanel = new JPanel();
        acronymAndNamePanel.setLayout(new BoxLayout(acronymAndNamePanel, BoxLayout.Y_AXIS));
       
        acronymTF = new JTextField(12);
        acronymTF.setBorder(BorderFactory.createTitledBorder("DNS Code Acronym"));
        acronymTF.addActionListener(dcListener);
        acronymTF.addKeyListener(changeListener);
        
        nameTF = new JTextField(12);
        nameTF.setBorder(BorderFactory.createTitledBorder("DNS Code Name"));
        nameTF.addActionListener(dcListener);
        nameTF.addKeyListener(changeListener);
        
        acronymAndNamePanel.add(acronymTF) ;
        acronymAndNamePanel.add(nameTF);
        
        //set up note and response panel
        JPanel definitionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        
        definitionPane = new JTextPane();
        definitionPane.setPreferredSize(new Dimension(464, 80));
        definitionPane.setToolTipText("DNS Code definition will appear on website");
        definitionPane.setParagraphAttributes(attribs,true);
        definitionPane.addKeyListener(changeListener);
	    
        JScrollPane noteScrollPane = new JScrollPane(definitionPane);
        definitionBorder = BorderFactory.createTitledBorder("DNS Code Definition");
        noteScrollPane.setBorder(definitionBorder);
		
        definitionPanel.add(noteScrollPane);
	   
        dnsCodePanel.add(acronymAndNamePanel);
        dnsCodePanel.add(definitionPanel);
     
		entityPanel.add(dsScrollPane);
        entityPanel.add(dnsCodePanel);
        
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New Code");
        btnNew.setToolTipText("Click to add a new DNS Code");
        
        btnDelete.setText("Delete Code");
        btnDelete.setToolTipText("Click to delete this DNS Code");
        
        btnSave.setText("Save New Code");
        btnSave.setToolTipText("Click to save the new code");
        
        btnCancel.setText("Cancel Add Code");
        btnCancel.setToolTipText("Click to cancel adding this code");
    		
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        this.pack();
        this.setResizable(true);
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(!bAddingNewEntity)
		{	
			if(tse.getSource() == nav && tse.getType() == EntityType.DNS_CODE)
			{
				update();
				display(dnsCodeDB.getObjectAtIndex(nav.getIndex()));
			}
			else if(tse.getSource() != nav && tse.getType() == EntityType.FAMILY)
			{
				//determine if family has a dns code. If it does, display that
				//code in this dialog
				ONCFamily selectedFam = (ONCFamily) tse.getObject1();
				if(selectedFam.getDNSCode() > -1)
				{
					//family has a dns code, find it in the database
					@SuppressWarnings("unchecked")
					List<DNSCode> codeList = (List<DNSCode>) dnsCodeDB.getList();
					int index = 0;
					while(index < codeList.size() &&
							!codeList.get(index).getAcronym().equals(selectedFam.getDNSCode()))
						index++;
					
					if(index < codeList.size())
					{
						update();
						nav.setIndex(dnsCodeDB.getIndexForObject(codeList.get(index)));
						display(codeList.get(index));
					}
				}
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.DNS_CODE);
	}

	@Override
	public void valueChanged(ListSelectionEvent lse)
	{
		if(!lse.getValueIsAdjusting() && !bIgnoreEvents &&!bAddingNewEntity)
		{
			int modelRow = dlgTable.getSelectedRow() == -1 ? -1 : 
				dlgTable.convertRowIndexToModel(dlgTable.getSelectedRow());
			
			if(modelRow > -1)
			{
				DNSCode code = (DNSCode) dnsCodeDB.getObjectAtIndex(modelRow);
				nav.setIndex(modelRow);
				display(code);
			}
		}
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("LOADED_DNSCODES"))
		{
			if(dnsCodeDB.size() > 0)
			{
				currCode = (DNSCode) dnsCodeDB.getObjectAtIndex(0);
				dlgTableModel.fireTableDataChanged();
				display(currCode);
			}
		}
		else if(dbe.getType().equals("UPDATED_DNSCODE"))
		{
			DNSCode updatedCode = (DNSCode) dbe.getObject1();
			if(currCode != null && updatedCode.getID() == currCode.getID())
				display(updatedCode);
			
			dlgTableModel.fireTableDataChanged();
		}
		else if(dbe.getType().equals("ADDED_NOTE"))
		{
			dlgTableModel.fireTableDataChanged();
		}
		else if(dbe.getSource() != this && (dbe.getType().equals("UPDATED_USER") ||
				dbe.getType().equals("CHANGED_USER")))
		{
			//determine if it's the currently logged in user
			ONCUser updatedUser = (ONCUser)dbe.getObject1();
			if(userDB.getLoggedInUser().getID() == updatedUser.getID())
				setTextPaneFontSize(updatedUser.getPreferences().getFontSize());
		}
	}

	@Override
	void update()
	{
		//if the note or title has changed, update the note. Only the note and title 
		//of the last note can be edited and only before the note is viewed
		if(currCode != null)
		{
			int bCD = 0;
			if(!currCode.getAcronym().equals(acronymTF.getText())) { bCD = bCD | 1; }
			if(!currCode.getName().equals(nameTF.getText())) { bCD = bCD | 2; }
			if(!currCode.getDefinition().equals(definitionPane.getText())) { bCD = bCD | 4; }
			
			if(bCD > 0)
			{
				DNSCode updateCodeReq = new DNSCode(currCode);
				updateCodeReq.setAcronym(acronymTF.getText());
				updateCodeReq.setName(nameTF.getText());
				updateCodeReq.setDefinition(definitionPane.getText());
				
				String response = dnsCodeDB.update(this, updateCodeReq);
				if(response.startsWith("UPDATED_DNSCODE"))
				{
					Gson gson = new Gson();
					DNSCode updatedCode = gson.fromJson(response.substring(15), DNSCode.class);
					
					display(updatedCode);
				}
				else
				{
					//display an error message that update request failed
					JOptionPane.showMessageDialog(this, "ONC Server denied DNS Code Update," +
											"try again later","DNS Code Update Failed",  
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					display(currCode);
				}
			}
		}
	}

	void display(ONCEntity code)	//displays currNote for currFam
	{
		//Determine what to display based on currFam && currNote and
		if(currCode == null || dnsCodeDB.size() <= 0 )
		{
			clear();
		}
		else
		{
			if(code == null)
				currCode = (DNSCode) dnsCodeDB.getObjectAtIndex(0);
			else if(code != null)
				currCode = (DNSCode) code;
			
			bIgnoreEvents = true;
			
			acronymTF.setText(currCode.getAcronym());
			acronymTF.setCaretPosition(0);
			
			nameTF.setText(currCode.getName());
			nameTF.setCaretPosition(0);

			definitionPane.setText(currCode.getDefinition());
			definitionPane.setCaretPosition(0);
			
			mode = Mode.NORMAL;
			btnNew.setText("Add New Code");
		    btnNew.setToolTipText("Click to add a new DNS Code");
		    
		    btnDelete.setText("Delete Code");
	        btnDelete.setToolTipText("Click to delete this DNS Code");
			
			nav.setCount1("DNS Codes: " + Integer.toString(dnsCodeDB.size()));
			
			nav.setStoplightEntity(currCode);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			//nav is set to the model row, table is set to the view row
			int tableRow = dlgTable.convertRowIndexToView(nav.getIndex());
			dlgTable.setRowSelectionInterval(tableRow, tableRow);

			bIgnoreEvents = false;
		}
	}
	
	void setTextPaneFontSize(Integer fontSize)
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, fontSize);
        StyleConstants.setSpaceBelow(attribs, 3);
        definitionPane.setParagraphAttributes(attribs, true);
        
        if(currCode != null)
        	display(currCode);
	}

	@Override
	void clear()
	{
		bIgnoreEvents = true;
		
		acronymTF.setText("");
		acronymTF.setCaretPosition(0);
		
		nameTF.setText("");
		nameTF.setCaretPosition(0);
		
		definitionBorder.setTitle("");
		definitionPane.setText("");

		nav.setCount1("Family Notes: 0");
		
		nav.setIndex(0);
		nav.clearStoplight();
		nav.btnNextSetEnabled(false);
		nav.btnPreviousSetEnabled(false);
		
		bIgnoreEvents = false;	
	}

	@Override
	void onNew() 
	{
		if(mode == Mode.NORMAL)
		{
			bAddingNewEntity = true;
		
			nav.navSetEnabled(false);
			clear();
		
			nav.setDefaultMssg("Add DNS Code");
			dnsCodeBorder.setTitle("Add DNS Code");
			
			definitionBorder.setTitle("New DNS Code Definition");
			acronymTF.setEditable(true);
			nameTF.setEditable(true);
			entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
			btnSave.setEnabled(false);
			setControlState();
		}
		else
			update();
	}

	@Override
	void onCancelNew() 
	{
		nav.navSetEnabled(true);
		display(currCode);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}


	@Override
	void onSaveNew() 
	{	
		//construct a new DNS Code if all the fields are valid
		if(!acronymTF.getText().isEmpty() && !nameTF.getText().isEmpty() &&
				!definitionPane.getText().isEmpty())
		{	
			//zDNSCode(int id, String acronym, String name, String definition)
			DNSCode reqAddCode = new DNSCode(-1, acronymTF.getText().toUpperCase(), nameTF.getText(),
												definitionPane.getText());
		
			DNSCode addedCode = (DNSCode) dnsCodeDB.add(this, reqAddCode);
			if(addedCode != null)
			{
				//set the display index to the newly added DNS code and displa
				nav.setIndex(dnsCodeDB.getListIndexByID(dnsCodeDB.getList(), addedCode.getID()));
				display(addedCode);
				dlgTableModel.fireTableDataChanged();
			}
			else
			{
				String err_mssg = "ONC Server denied add DNS Code request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add DNS Code Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currCode);
			}
					
			//reset to review mode and display the proper partner
			nav.navSetEnabled(true);
			entityPanel.setBackground(pBkColor);
				
			bAddingNewEntity = false;
			setControlState();
		}
		else
		{
			String err_mssg = "A note must have a title and text content, please try again";
			JOptionPane.showMessageDialog(this, err_mssg, "Invaild Note",
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
		}		
	}
	@Override
	void onDelete()
	{
		if(mode == Mode.NORMAL)
		{
			//delete the note
		}
		else
		{
			//cancel any changes made by the user
			display(currCode);
		}
	}
	
	void checkForChanges()
	{
		if(bAddingNewEntity)
			btnSave.setEnabled(!acronymTF.getText().isEmpty() && !nameTF.getText().isEmpty() &&
					!definitionPane.getText().isEmpty());
		else
		{
			//if no changes, btn text should allow dns code modification
			if(currCode != null && (!acronymTF.getText().equals(currCode.getAcronym()) ||
				!nameTF.getText().equals(currCode.getName()) ||
					   !definitionPane.getText().equals(currCode.getDefinition())))
			{
				mode = Mode.UPDATE;
				btnNew.setText("Update Note");
				btnNew.setToolTipText("Click to save DNS Code changes");
				btnDelete.setText("Cancel Update");
			}
			else
			{
				mode = Mode.NORMAL;
				btnNew.setText("Add New DNS Code");
				btnDelete.setText("Delete DNS Code");
			}
		}
	}
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Acronym", "Name", "Last Changed By", "Time Changed", "Stoplight"};
		
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return dnsCodeDB == null ? 0 : dnsCodeDB.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		Object value;
        	
        		DNSCode dc = dnsCodeDB.getDNSCode(row);
        	
        		if(col == ACRONYM_COL)
        			value = dc.getAcronym();
        		else if(col == NAME_COL)
        			value = dc.getName();
        		else if(col == CHANGED_BY_COL)
        			value = dc.getChangedBy();
        		else if(col == TIME_CHANGED_COL)
        			value = new Date(dc.getTimestamp());
        		else if(col == STOPLIGHT_COL)
        			value = gvs.getImageIcon(23 + dc.getStoplightPos());
        		else
        			value = "Error";
        	
         	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == TIME_CHANGED_COL)
        			return Date.class;
        		else if(column == STOPLIGHT_COL)
        			return ImageIcon.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
    }
	private class DNSCodeChangeListener implements KeyListener
	{

		@Override
		public void keyTyped(KeyEvent e)
		{
			checkForChanges();
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			checkForChanges();
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			checkForChanges();
		}
	}
	
	private enum Mode { NORMAL, UPDATE }
}
