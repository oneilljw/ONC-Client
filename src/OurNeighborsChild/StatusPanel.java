package OurNeighborsChild;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StatusPanel extends JPanel implements DatabaseListener
{
	private static final long serialVersionUID = 1L;
	private JLabel lblFCount, lblCCount;
	public JLabel  lblONCmssg;
    public JTextField searchTF;
    public JButton btnNext, btnPrevious;
    public JButton  rbSrchNext, rbSrchPrev;
    private Stoplight sl;
    
    public StatusPanel(JFrame pf, ONCDatabase db)
    {
    	GlobalVariables spGVs = GlobalVariables.getInstance();
    	
    	//listen for served count update family and child events
    	Families familyDB = Families.getInstance();
    	if(familyDB != null)
    		familyDB.addDatabaseListener(this);
    	
    	ChildDB childDB = ChildDB.getInstance();
    	if(childDB != null)
    		childDB.addDatabaseListener(this);
    	
    	//layout the ui
    	JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	JPanel p2 = new JPanel(new GridLayout(2,2));
    	JPanel p3 = new JPanel(new GridLayout(2,0));
    	JPanel p4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	
    	JLabel lblONCicon = new JLabel(spGVs.getImageIcon(0));
    	p1.add(lblONCicon);
    	   	
    	lblFCount = new JLabel("Served Familes: 0");
    	lblCCount = new JLabel("Served Children: 0");      
           	           	
    	btnNext = new JButton("Next Family", spGVs.getImageIcon(2));
    	btnNext.setHorizontalTextPosition(JButton.LEFT);
    	btnNext.setToolTipText("Click to see next ONC family");
        btnNext.setEnabled(false);
               
        btnPrevious = new JButton("Prev. Family", spGVs.getImageIcon(3));
        btnPrevious.setHorizontalTextPosition(JButton.RIGHT);
        btnPrevious.setToolTipText("Click to see previous ONC family");
        btnPrevious.setEnabled(false);
        
        p2.add(lblFCount);
        p2.add(lblCCount);
        p2.add(btnPrevious);
        p2.add(btnNext);
        
        JPanel searchsubpanel = new JPanel();
      
    	JLabel lblSearch = new JLabel("Search For:");

    	searchTF = new JTextField(14);
    	searchTF.setToolTipText("Type what you want to search for and press <Enter>");
        searchTF.setLayout(new BorderLayout());
        
        rbSrchNext = new JButton(spGVs.getImageIcon(2));
        rbSrchNext.setVisible(false);
        
        rbSrchPrev = new JButton(spGVs.getImageIcon(3));
        rbSrchPrev.setVisible(false);
          	    	
    	searchsubpanel.add(lblSearch);
    	searchsubpanel.add(searchTF);
    	searchsubpanel.add(rbSrchPrev);
    	searchsubpanel.add(rbSrchNext);

    	p3.add(searchsubpanel);
    	
    	JPanel mssgsubpanel = new JPanel();
    	mssgsubpanel.setLayout(new GridBagLayout());
    	lblONCmssg = new JLabel("Welcome to Our Neighbor's Child");
    	mssgsubpanel.add(lblONCmssg);
    	p3.add(mssgsubpanel);
    	
    	sl = new Stoplight(pf, db);
    	p4.add(sl);
    	  	
    	this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    	this.add(p1);
        this.add(p2);
        this.add(p3);
        this.add(p4);
    }
    
    void ClearData()
    {
    	lblFCount.setText("Served Families: 0");
    	lblCCount.setText("Served Children: 0");
    	lblONCmssg.setText("ONC database cleared");
    	SetEnabledNavButtons(false);
    	sl.clear();
    }
    
    void updateDBStatus(int[] dbCounts)
    {
    	lblFCount.setText("Served Families: " + Integer.toString((dbCounts[0])));
    	lblCCount.setText("Served Children: " + Integer.toString((dbCounts[1])));
    }
    
    void SetEnabledNavButtons(boolean state)
    {
    	btnNext.setEnabled(state);
    	btnPrevious.setEnabled(state);
    }
 
	void setStatusMssg(String mssg)
	{ 
		lblONCmssg.setText(mssg);
	}
	
	void setStoplightEntity(ONCEntity e) { sl.setEntity(e); }

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_SERVED_COUNTS"))
		{
//			System.out.println(String.format("StatusPanel DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			DataChange servedCountsChange = (DataChange) dbe.getObject();
			int[] changes = {servedCountsChange.getOldData(), servedCountsChange.getNewData()};
			updateDBStatus(changes);
		}	
	}
}
