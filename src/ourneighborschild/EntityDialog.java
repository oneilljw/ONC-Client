package ourneighborschild;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class EntityDialog extends JDialog implements ActionListener, DatabaseListener,
																EntitySelectionListener
{
	/**
	 * Implements an abstract class for the editor dialogs
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	protected GlobalVariables gvs;
	protected UserDB userDB;
	
	//listeners
	protected FieldChangeListener dcListener;
	
	//ui panels
	protected ONCNavPanel nav;
	protected JPanel contentPane, entityPanel, cntlPanel;
	
	//ui components
    protected JButton btnNew, btnCancel, btnDelete, btnSave;
    protected Color pBkColor; //Used to restore background for panels 1-3, btnShowPriorHistory when changed
    
    //semaphores
    protected boolean bIgnoreEvents;	//set when updating ui's that trigger events
    protected boolean bAddingNewEntity;	//set when in add new Entity mode
    
    EntityDialog(JFrame pf)
    {
    	super(pf);
		this.setTitle("Our Neighbor's Child - Driver Management");
		
		gvs = GlobalVariables.getInstance();	//Reference to the one global variable object
		userDB = UserDB.getInstance();
		
		dcListener = new FieldChangeListener();
		
		//Create a content panel for the dialog and add panel components to it.
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        
        //Set up entity panel
        entityPanel = new JPanel();
        entityPanel.setLayout(new BoxLayout(entityPanel, BoxLayout.Y_AXIS));
        pBkColor = entityPanel.getBackground();
        
        //Set up control panel
        cntlPanel = new JPanel();
        
        btnNew = new JButton();
        btnNew.setEnabled(true);
        btnNew.addActionListener(this);
        
        btnDelete = new JButton();
    	btnDelete.setEnabled(false);
    	btnDelete.setVisible(true);
        btnDelete.addActionListener(this);
        
        btnSave = new JButton();
    	btnSave.setVisible(false);
        btnSave.addActionListener(this);
        
        btnCancel = new JButton();
    	btnCancel.setVisible(false);
        btnCancel.addActionListener(this);
        
        cntlPanel.add(btnNew);
        cntlPanel.add(btnDelete);
        cntlPanel.add(btnSave);
        cntlPanel.add(btnCancel);
        
        bIgnoreEvents = false;
        bAddingNewEntity = false;
    }
    
    void processDeletedEntity(ONCSearchableDatabase db)	//assumes deleted entity is displayed
	{
		if(nav.getIndex() == 0 && db.size() > 0)
		{
			nav.setIndex(db.size() - 1);
			display(db.getObjectAtIndex(nav.getIndex()));
		}
		else if(db.size() == 0)
		{
			nav.setIndex(0);
			clear();
			btnDelete.setEnabled(false);
		}
		else
		{
			nav.setIndex(nav.getIndex() - 1);
			display(db.getObjectAtIndex(nav.getIndex()));
		}
		
		nav.clearSearch();	//clear the search function; the deleted object maybe in the search
	}
    
    void setControlState()
	{
		btnNew.setVisible(!bAddingNewEntity);
		btnDelete.setVisible(!bAddingNewEntity);
		btnSave.setVisible(bAddingNewEntity);
		btnCancel.setVisible(bAddingNewEntity);
	}
    
    boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    
    //methods that handle update and display of the entity
    abstract void update();
    abstract void display(ONCEntity e);
    abstract void clear();
    
    //methods that handle control buttons
    abstract void onNew();
    abstract void onCancelNew();
    abstract void onSaveNew();
    abstract void onDelete();
    
    @Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnNew)
			onNew();	
		else if(e.getSource() == btnCancel)
			onCancelNew();
		else if(e.getSource() == btnSave)
			onSaveNew();
		else if(e.getSource() == btnDelete)			
			onDelete();	
	}
    
    /***********************************************************************************************
	 * This class implements a listener for the fields that need to check for 
	 * data updates when the user presses the <Enter> key. The only action this listener takes is to
	 * call the update method which checks if the data has changed, if it has 
	 * it sends a change request to the server.
	 ***********************************************************************************************/
	private class FieldChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(!bIgnoreEvents && !bAddingNewEntity)
				update();
		}
	}
}
