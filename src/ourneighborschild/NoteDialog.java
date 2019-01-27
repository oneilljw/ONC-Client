package ourneighborschild;

import java.awt.FlowLayout;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class NoteDialog extends EntityDialog
{
	private ONCFamily currFam;
	
	private TitledBorder border;
	private JTextField noteTitle;
	private JTextArea noteTA, responseTA;
	private JLabel lblTimeCreated, lblTimeViewed, lblTimeResponse, lblFamily;
	
	NoteDialog(JFrame pf)
	{
		super(pf);
		
		//set up the edit user panel
		border = BorderFactory.createTitledBorder("Family Notes");
        entityPanel.setBorder(border);
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));	
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void update()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void display(ONCEntity e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void clear()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void onNew()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void onCancelNew()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void onSaveNew()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	void onDelete()
	{
		// TODO Auto-generated method stub
		
	}

}
