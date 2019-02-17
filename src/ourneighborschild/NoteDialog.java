package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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

public class NoteDialog extends EntityDialog implements ListSelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int TITLE_COL = 0;
	private static final int CREATED_BY_COL = 1;
	private static final int TIME_CREATED_COL = 2;
	private static final int STATUS_COL = 3;
	private static final int RESPONDED_BY_COL = 4;
	private static final int TIME_RESPONSE_COL = 5;
	
	private NoteDB noteDB;
	
	private ONCFamily currFam;
	private ONCNote currNote;
	
	private Mode mode;
	
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private TitledBorder familyBorder, noteBorder, responseBorder;
	private JTextField titleTF;
	private JCheckBox ckBoxSendEmail;
	private JTextPane notePane, responsePane;
	private SimpleDateFormat timeFormat;
	
	NoteDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Edit Notes For Family");
		
		//get data base references
		noteDB = NoteDB.getInstance();
		if(noteDB != null)
			noteDB.addDatabaseListener(this);
		
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//initialize member variables
		currFam = null;
		currNote = null;
		
		mode = Mode.NORMAL;
		
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, noteDB);
        nav.setDefaultMssg("Family Notes");
        nav.setCount1("Family Notes: 0");
        nav.setCount2("Unread: 0");
		
		//set up the edit note user interface
		familyBorder = BorderFactory.createTitledBorder("Family Notes");
        entityPanel.setBorder(familyBorder);
      
        TitleAndNoteChangeListener changeListener = new TitleAndNoteChangeListener();
        
        //set up the table panel
        dlgTableModel = new DialogTableModel();
        String[] colTT = {"Title", "User who wrote the note", "Time the note was written", 
        						"Has the note been read?", "Agent who viewed or responded to the note",
        						"Time the agent read or responded"};
        
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
      	dlgTable.getColumnModel().getColumn(TIME_CREATED_COL).setCellRenderer(tableCellRenderer);
      	dlgTable.getColumnModel().getColumn(TIME_RESPONSE_COL).setCellRenderer(tableCellRenderer);

      	//Set table column widths
      	int tablewidth = 0;
      	int[] colWidths = {192,80,120,80,90,120};
      	for(int col=0; col < colWidths.length; col++)
      	{
      		dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
      		tablewidth += colWidths[col];
      	}
      	tablewidth += 24; 	//count for vertical scroll bar
      		
//       dlgTable.setAutoCreateRowSorter(true);	//add a sorter
              
         JTableHeader anHeader = dlgTable.getTableHeader();
         anHeader.setForeground( Color.black);
         anHeader.setBackground( new Color(161,202,241));
              
         //left justify columns
         DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
         dtcr.setHorizontalAlignment(SwingConstants.CENTER);
         dlgTable.getColumnModel().getColumn(STATUS_COL).setCellRenderer(dtcr);
              
         //Create the scroll pane and add the table to it.
         JScrollPane dsScrollPane = new JScrollPane(dlgTable);
         dsScrollPane.setPreferredSize(new Dimension(tablewidth, 96));
         dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
         
        //set up title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleTF = new JTextField(16);
        titleTF.setBorder(BorderFactory.createTitledBorder("Title"));
        titleTF.addActionListener(dcListener);
        titleTF.addKeyListener(changeListener);
        
        ckBoxSendEmail = new JCheckBox("Send separate new note(s) notification email to agent?");
        ckBoxSendEmail.setToolTipText("If checked, agent will get an email notification of new notes");
        
        titlePanel.add(titleTF);
        titlePanel.add(ckBoxSendEmail);
        
        //set up note and response panel
        JPanel noteAndResponsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Dimension textAreaDimension = new Dimension(320, 100);
        timeFormat = new SimpleDateFormat("M/dd/yy h:mm a");
        
        //set up the note Panel
        JPanel notePanel = new JPanel();
        
        SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, userDB.getUserPreferences().getFontSize());
        StyleConstants.setSpaceBelow(attribs, 3);
        
        notePane = new JTextPane();
		notePane.setPreferredSize(textAreaDimension);
	    notePane.setToolTipText("Family specific note entered by ONC Elf");
	    notePane.setParagraphAttributes(attribs,true); 
	    notePane.setEditable(false);
	    notePane.addKeyListener(changeListener);
	    
	    JScrollPane noteScrollPane = new JScrollPane(notePane);
	    noteBorder = BorderFactory.createTitledBorder("Note");
		noteScrollPane.setBorder(noteBorder);
		notePanel.add(noteScrollPane);
       
        //set up response Panel
        JPanel responsePanel = new JPanel();
        
        responsePane = new JTextPane();
        responsePane.setPreferredSize(textAreaDimension);
        responsePane.setToolTipText("Note response provided by Agent");
        responsePane.setParagraphAttributes(attribs,true);
        responsePane.setEditable(false);
	    
	    JScrollPane responseScrollPane = new JScrollPane(responsePane);
	    responseBorder = BorderFactory.createTitledBorder("Response");
		responseScrollPane.setBorder(responseBorder);
		responsePanel.add(responseScrollPane);
		
		noteAndResponsePanel.add(notePanel);
		noteAndResponsePanel.add(responsePanel);
     
		entityPanel.add(dsScrollPane);
        entityPanel.add(titlePanel);
        entityPanel.add(noteAndResponsePanel);
        
        //Set the button names and tool tips for control panel
        btnNew.setText("Add New Note");
        btnNew.setToolTipText("Click to add a new note");
        
        btnDelete.setText("Delete Note");
        btnDelete.setToolTipText("Click to delete this note");
        
        btnSave.setText("Send Note");
        btnSave.setToolTipText("Click to send the note");
        
        btnCancel.setText("Cancel Note");
    		btnCancel.setToolTipText("Click to cancel adding this note");
    		
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        this.setContentPane(contentPane);

        this.pack();
        this.setResizable(true);
        this.setMinimumSize(new Dimension(720, 360));
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void setTextPaneFontSize(Integer fontSize)
	{
		SimpleAttributeSet attribs = new SimpleAttributeSet();  
        StyleConstants.setAlignment(attribs , StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontSize(attribs, fontSize);
        StyleConstants.setSpaceBelow(attribs, 3);
        notePane.setParagraphAttributes(attribs, true);
        responsePane.setParagraphAttributes(attribs, true);
        
        if(currFam != null && currNote != null)
        	display(currNote);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("LOADED_NOTES"))
			this.setTitle(String.format("Our Neighbor's Child - Agent Family Notes For %d Season",
					GlobalVariablesDB.getCurrentSeason()));
		else if(dbe.getType().equals("UPDATED_NOTE"))
		{
			ONCNote updatedNote = (ONCNote) dbe.getObject1();
			if(currFam != null && updatedNote.getOwnerID() == currFam.getID())
				dlgTableModel.fireTableDataChanged();
			
			if(currNote != null && updatedNote.getID() == currNote.getID())
				display(updatedNote);
		}
		else if(dbe.getType().equals("ADDED_NOTE"))
		{
			ONCNote addedNote = (ONCNote) dbe.getObject1();
			if(currFam != null && addedNote.getOwnerID() == currFam.getID())
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
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(!bAddingNewEntity)
		{	
			if(tse.getSource() == nav && tse.getType() == EntityType.NOTE)
			{
				update();
				display(noteDB.getObjectAtIndex(nav.getIndex()));
			}
			else if(tse.getSource() != nav && tse.getType() == EntityType.FAMILY)
			{
//				update();
				if((currFam = (ONCFamily) tse.getObject1()) != null)
				{	
					noteDB.setFilter(currFam.getID());
					nav.setIndex(0);
					ONCNote famNote = (ONCNote) noteDB.getObjectAtIndex(0);
					display(famNote);	//will be null if no notes for family
				}
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.NOTE, EntityType.FAMILY);
	}

	@Override
	void update()
	{
		//if the note or title has changed, update the note. Only the note and title 
		//of the last note can be edited and only before the note is viewed
		if(nav.getIndex() == 0  && currNote.getStatus() == ONCNote.SENT)
		{
			int bCD = 0;
			if(!currNote.getTitle().equals(titleTF.getText())) { bCD = bCD | 1; }
			if(!currNote.getNote().equals(notePane.getText())) { bCD = bCD | 2; }
			
			if(bCD > 0)
			{
				ONCNote updateNoteReq = new ONCNote(currNote);
				updateNoteReq.setTitle(titleTF.getText());
				updateNoteReq.setNote(notePane.getText());
				
				String response = noteDB.update(this, updateNoteReq);
				if(response.startsWith("UPDATED_NOTE"))
				{
					Gson gson = new Gson();
					ONCNote updatedNote = gson.fromJson(response.substring(12), ONCNote.class);
					
					display(updatedNote);
				}
				else
				{
					//display an error message that update request failed
					JOptionPane.showMessageDialog(this, "ONC Server denied Noter Update," +
											"try again later","Note Update Failed",  
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
					display(currNote);
				}
			}
		}
	}

	void display(ONCEntity note)	//displays currNote for currFam
	{
		//Determine what to display based on currFam && currNote and
		if(currFam == null || noteDB.size() <= 0 )
		{
			clear();
		}
		else
		{
			if(note == null)
				currNote = (ONCNote) noteDB.getObjectAtIndex(0);
			else if(note != null)
				currNote = (ONCNote) note;
			
			bIgnoreEvents = true;
			
			dlgTable.setRowSelectionInterval(nav.getIndex(), nav.getIndex());
			
			nav.setDefaultMssg("Notes for "+ currFam.getLastName() + " Family");
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			{
				nav.setDefaultMssg("Notes for "+ currFam.getLastName() + " Family");
				familyBorder.setTitle(String.format("Notes for %s family, ONC #%s",
													currFam.getLastName(), currFam.getONCNum()));
			}
			else
			{
				nav.setDefaultMssg("Notes for ONC #" + currFam.getONCNum());
				familyBorder.setTitle(String.format("Notes for ONC #%s", currFam.getONCNum()));
			}
				
			titleTF.setText(currNote.getTitle());
			titleTF.setCaretPosition(0);
			
			ckBoxSendEmail.setEnabled(false);
			ckBoxSendEmail.setSelected(currNote.sendEmail());

			//set the note border
			String noteBorderText = String.format("Sent by %s on %s", currNote.getChangedBy(),
									timeFormat.format(currNote.getDateChanged()));
			noteBorder.setTitle(noteBorderText);
			notePane.setText(currNote.getNote());
				
			//set the response border
			String responseBorderText;
			if(currNote.getStatus() == ONCNote.RESPONDED)	
				responseBorderText = String.format("Response from %s on %s", currNote.getRespondedBy(),
									timeFormat.format(currNote.getTimeResponse().getTime()));
			else if(currNote.getStatus() == ONCNote.READ)	
				responseBorderText = String.format("Read by %s on %s", currNote.getRespondedBy(),
									timeFormat.format(currNote.getTimeViewed().getTime()));
			else
				responseBorderText = "No Response: Not Yet Viewed";
			
			
			responseBorder.setTitle(responseBorderText);
			responsePane.setText(currNote.getResponse());
			
			setEditableInput(currNote);
			
			List<ONCNote> familyNoteList = noteDB.getNotesForFamily(currFam.getID());
			int unread = 0;
			for(ONCNote n : familyNoteList)
				if(n.getStatus() < ONCNote.READ)
					unread++;
			
			nav.setCount1("Family Notes: " + Integer.toString(noteDB.size()));
			nav.setCount2("Unread: " + Integer.toString(unread));
			
			nav.setStoplightEntity(currNote);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			entityPanel.repaint();	//mark for painting to refresh border changes

			bIgnoreEvents = false;
		}
	}
	
	void setEditableInput(ONCNote note)
	{
		if(note.getStatus() == ONCNote.RESPONDED)
		{
			notePane.setEditable(false);
			titleTF.setEditable(false);
		}
		else if(note.getStatus() == ONCNote.READ)
		{
			notePane.setEditable(false);
			titleTF.setEditable(false);
		}
		else if(note.getStatus() == ONCNote.SENT)
		{
			notePane.setEditable(true);
			titleTF.setEditable(true);
		}
	}
	
	void checkForChanges()
	{
		if(bAddingNewEntity)
			btnSave.setEnabled(!titleTF.getText().isEmpty() && !notePane.getText().isEmpty());
		else
		{
			//if no changes, btn text should allow new note creation. If changes,
			//it should allow update to existing note
			if(currNote != null && (!titleTF.getText().equals(currNote.getTitle()) ||
					   !notePane.getText().equals(currNote.getNote())))
			{
				mode = Mode.UPDATE;
				btnNew.setText("Update Note");
				btnDelete.setText("Cancel Update");
			}
			else
			{
				mode = Mode.NORMAL;
				btnNew.setText("Add New Note");
				btnDelete.setText("Cancel Update");
			}
		}
	}
	@Override
	void clear()
	{
		bIgnoreEvents = true;
		
		if(currFam != null)
		{	
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			{
				nav.setDefaultMssg("No Notes for "+ currFam.getLastName() + " Family");
				familyBorder.setTitle(String.format("No notes for %s family, ONC #%s",
											currFam.getLastName(), currFam.getONCNum()));
			}
			else
			{
				nav.setDefaultMssg("No Notes for ONC #" + currFam.getONCNum());
				familyBorder.setTitle(String.format("No notes for ONC #%s", currFam.getONCNum()));
			}
		}
		else
		{
			nav.setDefaultMssg("Family Notes");
			familyBorder.setTitle("No Notes");
		}
		titleTF.setText("");
		titleTF.setCaretPosition(0);
		ckBoxSendEmail.setEnabled(false);
		ckBoxSendEmail.setSelected(false);
		noteBorder.setTitle("");
		notePane.setText("");
		notePane.setEditable(false);
		responseBorder.setTitle("");
		responsePane.setText("");
		
		nav.setCount1("Family Notes: 0");
		nav.setCount2("Unread: 0");
		
		nav.setIndex(0);
		nav.clearStoplight();
		nav.btnNextSetEnabled(false);
		nav.btnPreviousSetEnabled(false);
		
		entityPanel.repaint();	//mark for painting to refresh border changes

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
		
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			{	
				nav.setDefaultMssg("Add Note for "+ currFam.getLastName() + " Family");
				familyBorder.setTitle(String.format("Add note for %s family, ONC #%s",
										currFam.getLastName(), currFam.getONCNum()));
			}
			else
			{	
				nav.setDefaultMssg("Add Note for ONC # " + currFam.getONCNum());
				familyBorder.setTitle(String.format("Add note for ONC #%s", currFam.getONCNum()));
			}
		
			Calendar now = Calendar.getInstance();
			String title = String.format("Created by %s on %s", userDB.getLoggedInUser().getLNFI(),
					timeFormat.format(now.getTime()));
			noteBorder.setTitle(title);
			titleTF.setEditable(true);
			ckBoxSendEmail.setEnabled(true);
			notePane.setEditable(true);
			entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
			entityPanel.repaint();
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
		display(currNote);
		entityPanel.setBackground(pBkColor);
		bAddingNewEntity = false;
		setControlState();
	}

	@Override
	void onSaveNew() 
	{	
		//construct a new ONCNote if all the fields are valid
		if(!titleTF.getText().isEmpty() && !notePane.getText().isEmpty())
		{	
			ONCNote reqAddNote = new ONCNote(-1, currFam.getID(), titleTF.getText(), notePane.getText(),
												ckBoxSendEmail.isSelected());
		
			ONCNote addedNote = (ONCNote) noteDB.add(this, reqAddNote);
			if(addedNote != null)
			{
				//set the display index to the newly added note and display the note
				nav.setIndex(noteDB.getListIndexByID(noteDB.getNotesForFamily(currFam.getID()), addedNote.getID()));
				display(addedNote);
				dlgTableModel.fireTableDataChanged();
			}
			else
			{
				String err_mssg = "ONC Server denied add note request, try again later";
				JOptionPane.showMessageDialog(this, err_mssg, "Add Note Failure",
											JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				display(currNote);
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
			display(currNote);
		}
		
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
				nav.setIndex(modelRow);
				display(noteDB.getObjectAtIndex(modelRow));
			}
		}
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Delivery History Dialog
		 */
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Title", "Written By", "Time", "Status", "Agent", "Time"};
		private String[] statusText;
		
		public DialogTableModel()
		{
			statusText =  new String[] {"No Note", "Unread", "Read", "Responded"};
		}

        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return noteDB == null ? 0 : noteDB.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        		Object value;
        	
        		ONCNote n = noteDB.getNotesForFamily(currFam.getID()).get(row);
        	
        		if(col == TITLE_COL)
        			value = n.getTitle();
        		else if(col == CREATED_BY_COL)
        			value = n.getChangedBy();
        		else if(col == TIME_CREATED_COL)
        			value = n.getDateChanged();
        		else if(col == STATUS_COL)
        		{
        			if(n.getStatus() >= ONCNote.SENT && n.getStatus() <= ONCNote.RESPONDED)
        				value = statusText[n.getStatus()];
        			else
        				value = "";
        		}
        		else if(col == RESPONDED_BY_COL)
        			value = n.getRespondedBy();
        		else if(col == TIME_RESPONSE_COL)
        		{
        			if(n.getStatus() == ONCNote.READ)
        				value = n.getTimeViewed().getTime();
        			else if(n.getStatus() == ONCNote.RESPONDED)
        				value = n.getTimeResponse().getTime();
        			else
        				value = "";
        		}
        		else
        			value = "Error";
        	
         	return value;
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        		if(column == TIME_CREATED_COL || column == TIME_RESPONSE_COL)
        			return Date.class;
        		else
        			return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        		return false;
        }
    }
	private class TitleAndNoteChangeListener implements KeyListener
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
