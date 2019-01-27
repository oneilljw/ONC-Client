package ourneighborschild;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AgentNoteDialog extends JDialog implements ActionListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextArea notesTA;
	private JButton btnSave;
	
	private FamilyDB familyDB;
	private ONCFamily currFam;
	
	AgentNoteDialog(JFrame pf, ONCFamily f)
	{
		super(pf, true);
		this.setTitle("Agent Notes");
		
		this.currFam = f;
		familyDB = FamilyDB.getInstance();
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JPanel notesPanel = new JPanel();
//		notesTF = new JTextField();
//		notesTF.setPreferredSize(new Dimension(72, 44));
//		notesTF.setBorder(BorderFactory.createTitledBorder("Notes"));
//		notesTF.setText("Why is this area not showing at all, in any way shape or form?");
//		notesPanel.add(notesTF);
		
		notesTA = new JTextArea();
		notesTA.setPreferredSize( new Dimension( 290, 160 ) );
	    notesTA.setToolTipText("Family specific notes entered by ONC elf");
	    notesTA.setLineWrap(true);
	    notesTA.setWrapStyleWord(true);
	    notesTA.addKeyListener(new TAKeyListener());
		
		notesTA.setText(currFam.getAgentNote());
	
		JScrollPane scrollPane = new JScrollPane(notesTA);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Agent Notes"));
		notesPanel.add(scrollPane);
		
		JPanel cntlPanel = new JPanel();
		btnSave = new JButton("Save");
		btnSave.setEnabled(false);
		btnSave.addActionListener(this);
		cntlPanel.add(btnSave);
		
		contentPanel.add(notesPanel);
		contentPanel.add(cntlPanel);
		this.setContentPane(contentPanel);
		
		this.setMinimumSize(new Dimension(300,200));
	}
	
	void setAgentNote(ONCFamily f)
	{
		notesTA.setText(f.getAgentNote());
	}
	
	void display() { this.setVisible(true); }
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnSave)
		{
			ONCFamily updateFamReq = new ONCFamily(currFam);
			updateFamReq.setAgentNote(notesTA.getText());
			
			String response = familyDB.update(this, updateFamReq);
			
			if(!response.startsWith("UPDATED_FAMILY"))
				notesTA.setText(currFam.getAgentNote());
			else
				currFam = updateFamReq;
				
			btnSave.setEnabled(false);
		}
	}
	
	private class TAKeyListener implements KeyListener
    {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyReleased(KeyEvent arg0) 
		{
			btnSave.setEnabled(!notesTA.getText().equals(currFam.getAgentNote()));	
		}

		@Override
		public void keyTyped(KeyEvent arg0)
		{	
			btnSave.setEnabled(!notesTA.getText().equals(currFam.getAgentNote()));	
		}
    }
}
