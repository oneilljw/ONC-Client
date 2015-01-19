package OurNeighborsChild;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class ONCNavPanel extends JPanel implements ActionListener
{
	/**
	 * This class implements a panel used in the ONC Edit dialogs to display ONC Entities.
	 * The class implements next and previous navigation buttons that incrementally scroll 
	 * through the associated ONC Entity data base, maintaining the index of the current 
	 * entity being displayed. The panel also implements a search text field that allows the
	 * user to search the associated data base for the entity. In addition, the panel 
	 * implements a stop light function for the entity, and two message fields.
	 * 
	 *  When the user changes the displayed ONC Entity, the panel fires a Navigation Event
	 *  to all registered listeners. 
	 */
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MSSG = "Welcome to Our Neighbor's Child";
	
	//List of registered listeners for navigation events
//	private ArrayList<NavigationListener> listeners;
    private ArrayList<EntitySelectionListener> elisteners;
    
    //UI objects
    private JButton btnNext, btnPrevious;
    private JButton  rbSrchNext, rbSrchPrev;
//  private JButton btnLogoff;
    private JTextField searchTF;
    private Stoplight sl;
    private JLabel lblCount1, lblCount2;
    private JLabel lblMssg;
    private ArrayList<Integer> searchAL;
    private int srchALindex;
    private int index = 0;	//Index for ONCEntity array list
    private ONCSearchableDatabase db;
    private String defaultMssg;
    
	public ONCNavPanel(JFrame parentFrame, ONCSearchableDatabase db)
	{
		//get Global Variables reference
		GlobalVariables gvs = GlobalVariables.getInstance();
		this.db = db;
		defaultMssg = DEFAULT_MSSG;
		
		//Create the list that holds search results
		searchAL = new ArrayList<Integer>();
		srchALindex=0;
		
		//Top panel
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
		JPanel tp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    JPanel tp2 = new JPanel(new GridLayout(2,2));
	    JPanel tp3 = new JPanel(new GridLayout(2,0));
	    JPanel tp4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//	    JPanel tp4 = new JPanel();
//	    tp4.setLayout(new BoxLayout(tp4, BoxLayout.Y_AXIS));
	    	
	    JLabel lblONCicon = new JLabel(gvs.getImageIcon(0));
	    tp1.add(lblONCicon);
	    	   	
	    lblCount1 = new JLabel();
	    lblCount2 = new JLabel();      
	           	           	
	    btnNext = new JButton(gvs.getImageIcon(2));
	    btnNext.setHorizontalTextPosition(JButton.LEFT);
	    btnNext.setToolTipText("Click to see next ONC Partner");
	    btnNext.setEnabled(false);
	    btnNext.requestFocus();
	    btnNext.addActionListener(this);
	               
	    btnPrevious = new JButton(gvs.getImageIcon(3));
	    btnPrevious.setHorizontalTextPosition(JButton.RIGHT);
	    btnPrevious.setToolTipText("Click to see previous ONC Partner");
	    btnPrevious.setEnabled(false);
	    btnPrevious.addActionListener(this);
	        
	    tp2.add(lblCount1);
	    tp2.add(lblCount2);
	    tp2.add(btnPrevious);
	    tp2.add(btnNext);
	        
	    JPanel searchsubpanel = new JPanel();
	    JLabel lblSearch = new JLabel("Search For:");
	    searchsubpanel.add(lblSearch);

	    searchTF = new JTextField(13);
	    searchTF.setToolTipText("Type the data you want to find, then press <Enter>");
	    searchTF.addActionListener(this);
	    searchTF.addKeyListener(new SearchTFKeyListener());
	    searchsubpanel.add(searchTF);
	    	
	    rbSrchPrev = new JButton(gvs.getImageIcon(3));
	    rbSrchPrev.setVisible(false);
	    rbSrchPrev.addActionListener(this);
	    searchsubpanel.add(rbSrchPrev);
	        
	    rbSrchNext = new JButton(gvs.getImageIcon(2));
	    rbSrchNext.setVisible(false);
	    rbSrchNext.addActionListener(this);
	    searchsubpanel.add(rbSrchNext);
	      
	    tp3.add(searchsubpanel);
	    	
	    JPanel mssgsubpanel = new JPanel();
	    mssgsubpanel.setLayout(new GridBagLayout());
	    lblMssg = new JLabel();
	    mssgsubpanel.add(lblMssg);
	    tp3.add(mssgsubpanel);
	    	
	    sl = new Stoplight(parentFrame, db);
/*	    
//	    btnLogoff = new JButton("<html><font color=\"blue\">Log Out</font></html>");
	    btnLogoff = new JButton("Logoff");
//	    btnLogoff.setFocusPainted(false);
        btnLogoff.setMargin(new Insets(0, 0, 0, 0));
        btnLogoff.setContentAreaFilled(false);
        btnLogoff.setBorderPainted(false);
        btnLogoff.setOpaque(false);
        btnLogoff.addActionListener(this);
*/	    
	    tp4.add(sl);
//	    tp4.add(btnLogoff);
	   	
	    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    this.add(tp1);
	    this.add(tp2);
	    this.add(tp3);
	    this.add(tp4);
	}
	
	//getters
	int getIndex() { return index; }
	
	//setters
	void setIndex(int index) { this.index = index; }
	void setNextButtonText(String text) {btnNext.setText(text); }
	void setPreviousButtonText(String text) {btnPrevious.setText(text); }
	void btnNextSetEnabled(boolean tf) { btnNext.setEnabled(tf); }
	void btnPreviousSetEnabled(boolean tf) { btnPrevious.setEnabled(tf); }
	void setMssg(String mssg) { lblMssg.setText(mssg); }
	void setCount1(String text) { lblCount1.setText(text); }
	void setCount2(String text) { lblCount2.setText(text); }
	void setStoplightEntity(ONCEntity e) { sl.setEntity(e); }
	void setStoplight(int pos, String mssg) { sl.set(pos, mssg); }
	void clearStoplight() { sl.clear(); }
	
	void setDefaultMssg(String mssg)
	{
		defaultMssg = mssg;
		lblMssg.setText(mssg);
	}
	
	void navSetEnabled(boolean tf)
	{
		btnNext.setEnabled(tf);
		btnPrevious.setEnabled(tf);
		rbSrchNext.setEnabled(tf);
		rbSrchPrev.setEnabled(tf);
		searchTF.setEnabled(tf);
	}
	
    /** Register a listener for Entity Selection events */
    synchronized public void addEntitySelectionListener(EntitySelectionListener l)
    {
    	if (elisteners == null)
    		elisteners = new ArrayList<EntitySelectionListener>();
    	elisteners.add(l);
    }  

    /** Remove a listener for Entity Selection events */
    synchronized public void removeEntitySelectionListener(EntitySelectionListener l)
    {
    	if (elisteners == null)
    		elisteners = new ArrayList<EntitySelectionListener>();
    	elisteners.remove(l);
    }
    
    /** Fire an Entity Selection event to all registered listeners */
    protected void fireEntitySelected(Object source, String eventType, Object obj1, Object obj2)
    {
    	// if we have no listeners, do nothing...
    	if (elisteners != null && !elisteners.isEmpty())
    	{
    		// create the event object to send
    		EntitySelectionEvent event = new EntitySelectionEvent(source, eventType, obj1, obj2);
    		
    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<EntitySelectionListener> targets;
    		synchronized (this) { targets = (ArrayList<EntitySelectionListener>) elisteners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(EntitySelectionListener el:targets)
    			el.entitySelected(event);
    	}
    }

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnNext || e.getSource() == btnPrevious)
		{
			if(e.getSource() == btnNext)
			{						
				if(++index == db.size())
					index=0;
			}
			else if(e.getSource() == btnPrevious)
			{
				if(--index< 0)
					index = db.size()-1;
			}
			
//			fireNavChanged(this, "INDEX_CHANGE", index);
			fireEntitySelected(this, db.getDBType() +"_SELECTED", db.getObjectAtIndex(index), null);
		}
		else if(e.getSource() == searchTF && !searchTF.getText().isEmpty())
		{
			String data = searchTF.getText();
			String type = db.searchForListItem(searchAL, data);	//builds searchAL
			
			if(searchAL.size() > 1)	//duplicate ONC Entities were found   	
	    		setMssg(Integer.toString(searchAL.size()) +" " + type + " " + data + "'s were found");
	    	else if(searchAL.size() == 1)
	    		setMssg(type + " " + data + " found");
	    	else
	    		setMssg(type + " " +data + " not found");
			
			if(searchAL.size() > 0)	//Match found in array list has elements
			{				
    			srchALindex=0;
    			index = db.getListIndexByID(db.getList(), searchAL.get(srchALindex));
//    			fireNavChanged(this, "INDEX_CHANGE", index);
    			fireEntitySelected(this, db.getDBType() +"_SELECTED", db.getObjectAtIndex(index), null);
			}
			else
			{
				searchTF.setText(searchTF.getText() + " Not Found");
				searchTF.setCaretPosition(0);
			}
			
			//If multiple partners found, show scroll radio buttons
			rbSrchNext.setVisible(searchAL.size() > 1);
			rbSrchPrev.setVisible(searchAL.size() > 1);
		}
		else if(e.getSource() == rbSrchNext)
		{
			if(++srchALindex == searchAL.size())
				srchALindex=0;
			
			index = db.getListIndexByID(db.getList(), searchAL.get( srchALindex));	
//			fireNavChanged(this, "INDEX_CHANGE", index);
			fireEntitySelected(this, db.getDBType() +"_SELECTED", db.getObjectAtIndex(index), null);
		}
		else if(e.getSource() == rbSrchPrev)
		{
			if(--srchALindex == -1)
				srchALindex=searchAL.size()-1;
			
			index = db.getListIndexByID(db.getList(), searchAL.get(srchALindex));
//			fireNavChanged(this, "INDEX_CHANGE", index);
			fireEntitySelected(this, db.getDBType() +"_SELECTED", db.getObjectAtIndex(index), null);
		}
//		else if(e.getSource() == btnLogoff)
//		{
//			System.out.println("ONCNavPanel.actionPerformed: btnLogoff Event");
//		}
		
	}
	
	/***********************************************************************************
	 * This class implements a key listener for the OrganizationDialog class that
	 * listens to the search text field to determine when it is empty. If it becomes empty,
	 * the listener clears the search array list, and sets the scroll radio buttons invisible.
	 * It also sets the displayed message to the original GENERIC_MSSG
	 * @author johnwoneill
	 ***********************************************************************************/
	 private class SearchTFKeyListener implements KeyListener
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
			if(searchTF.getText().isEmpty())
			{
				searchAL.clear();
				srchALindex = 0;
				rbSrchNext.setVisible(false);
				rbSrchPrev.setVisible(false);
				lblMssg.setText(defaultMssg);
			}	
		}
	 }
}
