package ourneighborschild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WishLabelViewer extends JDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildWishDB childWishDB;
	
	private JPanel labelPanel;
	
	private final Image img;
	
	private ONCChild child;	//current child being displayed
	private int wn;			//current wish number being displayed
	
	WishLabelViewer(JFrame pf, ONCChild child, int wn)
	{
		super(pf, true);
		this.child = child;
		this.wn = wn;
		this.setTitle(String.format("Wish %d Ornament Label", wn+1));
		
		familyDB = FamilyDB.getInstance();
		
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childWishDB = ChildWishDB.getInstance();
		if(childWishDB != null)
			childWishDB.addDatabaseListener(this);

		labelPanel = new WishLabelPanel();
		img = GlobalVariables.getSeasonIcon().getImage();
		
		this.setContentPane(labelPanel);
		
		this.setSize(300, 136);
		this.setResizable(false);
	}

	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().equals("WISH_ADDED"))
		{
//			System.out.println(String.format("Child Panel DB Event: Source %s, Type %s, Object %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			//Get the added wish to extract the child
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject1();
		
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
			if(child != null && addedWish.getChildID() == child.getID())
				labelPanel.repaint();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildWish updatedWish = (ONCChildWish) dbe.getObject1();
			
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
			if(child != null && updatedWish.getChildID() == child.getID())
				labelPanel.repaint();
		}
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject1();
			if(child != null && child.getID() == updatedChild.getID())
			{
				//the age or gender of the child may have changed, update the label
				labelPanel.repaint();
			}
		}
	}
	
	private class WishLabelPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		private Font[] lFont;
		private AveryWishLabelPrinter awlp;
		
		public WishLabelPanel()
		{
			awlp = new AveryWishLabelPrinter();
			
			this.setBackground(Color.white);
			
			lFont = new Font[3];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 11);
		    lFont[1] = new Font("Calibri", Font.BOLD, 11);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 10);
		}
		
		/**
		 * paintComponent paints the label on the panel using
		 * the AveryWishLabelPrinter.
		 */
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.scale(X_DISPLAY_SCALE_FACTOR, Y_DISPLAY_SCALE_FACTOR);
		    
		    //create the sort wish object list.
		    ONCFamily fam = familyDB.getFamily(child.getFamID());
			ONCChildWish cw = childWishDB.getWish(child.getID(), wn);
		    SortWishObject swo = new SortWishObject(0, fam, child, cw);
		    
		    awlp.drawLabel(10, 20, swo.getWishLabel(), lFont, img, g2d);
		}
	}	
}
