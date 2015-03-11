package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserDialog extends EntityDialog
{
	private static final long serialVersionUID = 1L;
	
	//database references
	private UserDB uDB;
	
	//ui components
	private JLabel lblSessions;
    private JTextField firstnameTF, lastnameTF;
    private JTextField hPhoneTF, cPhoneTF, emailTF;
    
    private ONCUser currUser;	//reference to the current ONCUser object being displayed
    
	UserDialog(JFrame pf)
	{
		super(pf);
		this.setTitle("Our Neighbor's Child - User Information");
		
		//Initialize object variables
		uDB = UserDB.getInstance();	//Reference to the driver data base
		if(uDB != null)
			uDB.addDatabaseListener(this);
		
		currUser = null;
        
        //set up the navigation panel at the top of dialog
        nav = new ONCNavPanel(pf, uDB);
        nav.setDefaultMssg("Our Neighbor's Child Delivery Partners");
        nav.setCount1("Attempted: " + Integer.toString(0));
        nav.setCount2("Delivered: " + Integer.toString(0));
        nav.setNextButtonText("Next Partner");
        nav.setPreviousButtonText("Previous Partner");
//      nav.addNavigationListener(this);
        nav.addEntitySelectionListener(this);

        //Set up driver panel
        entityPanel.setBorder(BorderFactory.createTitledBorder("Delivery Partner Information"));
        JPanel op1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel op2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        firstnameTF = new JTextField(12);
        firstnameTF.setBorder(BorderFactory.createTitledBorder("First Name"));
        firstnameTF.addActionListener(dcListener);
        
        lastnameTF = new JTextField(12);
        lastnameTF.setBorder(BorderFactory.createTitledBorder("Last Name"));
        lastnameTF.addActionListener(dcListener);
        
        lblSessions = new JLabel("0", JLabel.RIGHT);
        lblSessions.setPreferredSize(new Dimension (52, 48));
        lblSessions.setToolTipText("# Deliveries Partner Made");
        lblSessions.setBorder(BorderFactory.createTitledBorder("# Del"));
        
        hPhoneTF = new JTextField(9);
        hPhoneTF.setToolTipText("Delivery partner home phone #");
        hPhoneTF.setBorder(BorderFactory.createTitledBorder("Home Phone #"));
        hPhoneTF.addActionListener(dcListener);
        
        cPhoneTF = new JTextField(9);
        cPhoneTF.setToolTipText("Delivery partner cell phone #");
        cPhoneTF.setBorder(BorderFactory.createTitledBorder(" Cell Phone #"));
        cPhoneTF.addActionListener(dcListener);
                
        op1.add(lblSessions);
        op1.add(firstnameTF);
        op1.add(lastnameTF);
        op1.add(hPhoneTF);
        op1.add(cPhoneTF);
                                           
        emailTF = new JTextField(18);
        emailTF.setToolTipText("User email address");
        emailTF.setBorder(BorderFactory.createTitledBorder("Email Address"));
        emailTF.setHorizontalAlignment(JTextField.LEFT);
        emailTF.addActionListener(dcListener);
              
        op2.add(emailTF);
                      
        entityPanel.add(op1);
        entityPanel.add(op2);
        
        //Set up control panel
        btnNew.setText("Add New Driver");
    	btnNew.setToolTipText("Click to add a new driverr");
     
        btnDelete.setText("Delete Driver");
    	btnDelete.setToolTipText("Click to delete this driver");
    	
        
        btnSave.setText("Save NewDriver");
    	btnSave.setToolTipText("Click to save the new driver");
        
        btnCancel.setText("Cancel Add New Driver");
    	btnCancel.setToolTipText("Click to cancel adding a new drivonconer");

    	//add the panels to the content pane
        contentPane.add(nav);
        contentPane.add(entityPanel);
        contentPane.add(cntlPanel);
        
        //add the content pane to the dialog and arrange
        this.setContentPane(contentPane);
        pack();
        setResizable(true);
        Point pt = pf.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) {
		// TODO Auto-generated method stub

	}

	@Override
	void update() {
		// TODO Auto-generated method stub

	}

	@Override
	void display(ONCEntity e) {
		// TODO Auto-generated method stub

	}

	@Override
	void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	void onNew() {
		// TODO Auto-generated method stub

	}

	@Override
	void onCancelNew() {
		// TODO Auto-generated method stub

	}

	@Override
	void onSaveNew() {
		// TODO Auto-generated method stub

	}

	@Override
	void onDelete() {
		// TODO Auto-generated method stub

	}

}
