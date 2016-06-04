package ourneighborschild;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

public class ONCNavPanel extends ONCPanel implements ActionListener
{
	/**
	 * This class implements a panel used in the ONC Edit dialogs and the family panel to display
	 * ONC Entities. The class implements next and previous navigation buttons that incrementally
	 * scroll through the associated ONC Entity data base, maintaining the index of the current 
	 * entity being displayed. The panel also implements a search text field that allows the
	 * user to search the associated data base for the entity. In addition, the panel 
	 * implements a stop light function for the entity, and two message fields.
	 * 
	 *  When the user changes the displayed ONC Entity, the panel fires an EntitySelection event
	 *  to all registered listeners. It inherits from ONCPanel for EntitySelection functionality
	 */
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MSSG = "Welcome to Our Neighbor's Child";
	
    //UI objects
    private JButton btnNext, btnPrevious;
    private JButton  rbSrchNext, rbSrchPrev;
//  private JButton btnLogoff;	//logoff not implemented at this time
    private JTextField searchTF;
    private StoplightPanel sl;
    private JLabel lblCount1, lblCount2;
    private JLabel lblMssg;
    private ArrayList<Integer> searchAL;
    private int srchALindex;
    private int index = 0;	//Index for ONCEntity array list
    private ONCSearchableDatabase searchableDB;
    private String defaultMssg;
    
	public ONCNavPanel(JFrame parentFrame, ONCSearchableDatabase db)
	{
		super(parentFrame, db.getDBType());
		this.searchableDB = db;
		defaultMssg = DEFAULT_MSSG;
		
		//register to fire entity selection events
		EntityEventManager.getInstance().registerEntitySelector(this);
		
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
	    	
	    JLabel lblONCicon = new JLabel(GlobalVariables.getONCLogo());
	    lblONCicon.setToolTipText("ONC Client v" + GlobalVariables.getVersion());
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
//	    searchTF.addKeyListener(new SearchTFKeyListener());
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
	    	
	    sl = new StoplightPanel(parentFrame, db);
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
	EntityType getEntityType() { return searchableDB.getDBType(); }
	
	//setters
	void setIndex(int index) 
	{
		if(!searchAL.isEmpty())	//clear the search if a new entity is selected
			clearSearch();
		
		this.index = index; 
	}
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
	
	void clearSearch()
	{
		searchTF.setText("");
		searchAL.clear();
		srchALindex = 0;
		rbSrchNext.setVisible(false);
		rbSrchPrev.setVisible(false);
		lblMssg.setText(defaultMssg);
	}
	
	void clearSearchText()
	{
		searchTF.setText("");
		searchTF.requestFocusInWindow();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == btnNext || e.getSource() == btnPrevious)
		{
			if(e.getSource() == btnNext)
			{						
				if(++index == searchableDB.size())
					index=0;
			}
			else if(e.getSource() == btnPrevious)
			{
				if(--index< 0)
					index = searchableDB.size()-1;
			}
			
			if(!searchAL.isEmpty())
				clearSearch();	//clear a prior search, if there is one displayed
			
			fireEntitySelected(this, searchableDB.getDBType(), searchableDB.getObjectAtIndex(index), null);
		}
		else if(e.getSource() == searchTF && !searchTF.getText().isEmpty())
		{
			String data = searchTF.getText();
			String type = searchableDB.searchForListItem(searchAL, data);	//builds searchAL
			
			if(searchAL.size() > 1)	//duplicate ONC Entities were found   	
	    		setMssg(Integer.toString(searchAL.size()) +" " + type + " " + data + "'s were found");
	    	else if(searchAL.size() == 1)
	    		setMssg(type + " " + data + " found");
	    	else
	    		setMssg(type + " " + data + " not found");
			
			if(searchAL.size() > 0)	//Match found, search list has elements
			{				
    			srchALindex=0;
    			index = searchableDB.getListIndexByID(searchableDB.getList(), searchAL.get(srchALindex));
    			fireEntitySelected(this, searchableDB.getDBType(), searchableDB.getObjectAtIndex(index), null);
			}
			else
			{
				setMssg(searchTF.getText() + " Not Found");
				searchTF.setCaretPosition(0);
			}
			
			//If multiple partners found, show scroll radio buttons
			rbSrchNext.setVisible(searchAL.size() > 1);
			rbSrchPrev.setVisible(searchAL.size() > 1);
			
			clearSearchText();
		}
		else if(e.getSource() == rbSrchNext)
		{
			if(++srchALindex == searchAL.size())
				srchALindex=0;
			
			index = searchableDB.getListIndexByID(searchableDB.getList(), searchAL.get( srchALindex));	
			fireEntitySelected(this, searchableDB.getDBType(), searchableDB.getObjectAtIndex(index), null);
		}
		else if(e.getSource() == rbSrchPrev)
		{
			if(--srchALindex == -1)
				srchALindex=searchAL.size()-1;
			
			index = searchableDB.getListIndexByID(searchableDB.getList(), searchAL.get(srchALindex));
			fireEntitySelected(this, searchableDB.getDBType(), searchableDB.getObjectAtIndex(index), null);
		}
//		else if(e.getSource() == btnLogoff)
//		{
//			System.out.println("ONCNavPanel.actionPerformed: btnLogoff Event");
//		}
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{	
		return EnumSet.of(searchableDB.getDBType());
	}
}
