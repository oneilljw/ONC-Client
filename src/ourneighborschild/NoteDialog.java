package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class NoteDialog extends EntityDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private NoteDB noteDB;
	
	private ONCFamily currFam;
	private ONCNote currNote;
	
	
	private TitledBorder familyBorder, noteBorder, responseBorder;
	private JTextField titleTF;
	private JTextArea noteTA, responseTA;
	private SimpleDateFormat timeFormat;
	
	NoteDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - Edit Notes For Family");
		
		//get data base references
		noteDB = NoteDB.getInstance();
		if(noteDB != null)
			noteDB.addDatabaseListener(this);
		
		//initialize member variables
		currFam = null;
		currNote = null;
		
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, noteDB);
        nav.setDefaultMssg("Family Notes");
        nav.setCount1("Family Notes: " + Integer.toString(0));
        nav.setCount2("Viewed: " + Integer.toString(0));
		
		//set up the edit note user interface
		familyBorder = BorderFactory.createTitledBorder("Family Notes");
        entityPanel.setBorder(familyBorder);
      
        TitleAndNoteChangeListener changeListener = new TitleAndNoteChangeListener();
        //set up title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleTF = new JTextField(32);
        titleTF.setBorder(BorderFactory.createTitledBorder("Title"));
        titleTF.addActionListener(dcListener);
        titleTF.addKeyListener(changeListener);
        
        titlePanel.add(titleTF);
        
        //set up note and response panel
        JPanel noteAndResponsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Dimension textAreaDimension = new Dimension(280, 120);
        timeFormat = new SimpleDateFormat("M/dd/yy h:mm a");
        
        //set up the note Panel
        JPanel notePanel = new JPanel();
        
        noteTA = new JTextArea();
		noteTA.setPreferredSize(textAreaDimension);
	    noteTA.setToolTipText("Family specific note entered by ONC Elf");
	    noteTA.setLineWrap(true);
	    noteTA.setWrapStyleWord(true);
	    noteTA.setEditable(false);
	    noteTA.addKeyListener(changeListener);
	    
	    JScrollPane noteScrollPane = new JScrollPane(noteTA);
	    noteBorder = BorderFactory.createTitledBorder("Note");
		noteScrollPane.setBorder(noteBorder);
		notePanel.add(noteScrollPane);
       
        //set up response Panel
        JPanel responsePanel = new JPanel();
        
        responseTA = new JTextArea();
        responseTA.setPreferredSize(textAreaDimension);
        responseTA.setToolTipText("Note response provided by Agent");
        responseTA.setLineWrap(true);
        responseTA.setWrapStyleWord(true);
        responseTA.setEditable(false);
	    
	    JScrollPane responseScrollPane = new JScrollPane(responseTA);
	    responseBorder = BorderFactory.createTitledBorder("Response");
		responseScrollPane.setBorder(responseBorder);
		responsePanel.add(responseScrollPane);
		
		noteAndResponsePanel.add(notePanel);
		noteAndResponsePanel.add(responsePanel);
     
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
        this.setMinimumSize(new Dimension(640, 300));
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
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
			if(currNote != null && updatedNote.getID() == currNote.getID())
				display(updatedNote);
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
		// TODO Auto-generated method stub
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
			
			if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
				familyBorder.setTitle(String.format("Note %d of %d for %s family, ONC #%s",
					nav.getIndex()+1, noteDB.size(), currFam.getLastName(), currFam.getONCNum()));
			else
				familyBorder.setTitle(String.format("Note %d of %d for ONC #%s",
						nav.getIndex()+1, noteDB.size(), currFam.getONCNum()));
				
			titleTF.setText(currNote.getTitle());
			titleTF.setCaretPosition(0);
			
			//set the note border
			String noteBorderText = String.format("Sent by %s on %s", currNote.getChangedBy(),
									timeFormat.format(currNote.getDateChanged()));
			noteBorder.setTitle(noteBorderText);
			
			noteTA.setText(currNote.getNote());
			noteTA.setEditable(true);
			
//			System.out.println(String.format("NoteDlg.disp: created: %d, viewed: %d response: %d",
//					currNote.getDateChanged().getTime(), 
//					currNote.getTimeViewed().getTimeInMillis(),
//					currNote.getTimeResponse().getTimeInMillis()));
					
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
			
			responseTA.setText(currNote.getResponse());
			
			nav.setCount1("Total Notes: " + Integer.toString(noteDB.size()));
			nav.setCount2("Unread Notes: " + Integer.toString(noteDB.size()));
			
			nav.setStoplightEntity(currNote);
			nav.btnNextSetEnabled(true);
			nav.btnPreviousSetEnabled(true);
			
			entityPanel.repaint();	//mark for painting to refresh border changes

			bIgnoreEvents = false;
		}
	}
	
	void checkForChanges()
	{
		if(bAddingNewEntity)
			btnSave.setEnabled(!titleTF.getText().isEmpty() && !noteTA.getText().isEmpty());
		else
		{
			//if no changes, btn text should allow new note creation. If changes,
			//it should allow update to existing note
			if(currNote != null && (!titleTF.getText().equals(currNote.getTitle()) ||
					   !noteTA.getText().equals(currNote.getNote())))
			{
				btnNew.setText("Update Note");
			}
			else
				btnNew.setText("Add New Note");
		}
	}
	@Override
	void clear()
	{
		bIgnoreEvents = true;
		
		if(userDB.getLoggedInUser().getPermission().compareTo(UserPermission.Admin) >= 0)
			familyBorder.setTitle(String.format("No notes for %s family, ONC #%s",
											currFam.getLastName(), currFam.getONCNum()));
		else
			familyBorder.setTitle(String.format("No notes for ONC #%s", currFam.getONCNum()));
			
		titleTF.setText("");
		titleTF.setCaretPosition(0);
		noteBorder.setTitle("");
		noteTA.setText("");
		noteTA.setEditable(false);
		responseBorder.setTitle("");
		responseTA.setText("");
		
		nav.setCount1("Total Notes: 0");
		nav.setCount2("Unread Notes: 0");
		
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
		bAddingNewEntity = true;
		
		nav.navSetEnabled(false);
		clear();
		familyBorder.setTitle("Enter New Note");
		
		Calendar now = Calendar.getInstance();
		String title = String.format("Created by %s on %s", userDB.getLoggedInUser().getLNFI(),
				timeFormat.format(now.getTime()));
		noteBorder.setTitle(title);
		noteTA.setEditable(true);
		entityPanel.setBackground(Color.CYAN);	//Use color to indicate add org mode vs. review mode
		entityPanel.repaint();
		btnSave.setEnabled(false);
		setControlState();
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
		if(!titleTF.getText().isEmpty() && !noteTA.getText().isEmpty())
		{	
			ONCNote reqAddNote = new ONCNote(-1, currFam.getID(), titleTF.getText(), noteTA.getText());
		
			ONCNote addedNote = (ONCNote) noteDB.add(this, reqAddNote);
			if(addedNote != null)
			{
				//set the display index to the newly added note and display the note
				nav.setIndex(noteDB.getListIndexByID(noteDB.getNotesForFamily(currFam.getID()), addedNote.getID()));
				display(addedNote);
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
		// TODO Auto-generated method stub
		
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
}
