package ourneighborschild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OrnamentLabelViewer extends JDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int CLONED_GIFT_FIRST_GIFT_NUMBER = 3;
	
	//database references
	GlobalVariablesDB gvDB;
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildGiftDB childGiftDB;
	private ClonedGiftDB clonedGiftDB;
	
	private GiftLabelPanel labelPanel;
	
	private final Image img;
	
	private ONCChild child;	//current child being displayed
	private int wn;			//current wish number being displayed
	
	OrnamentLabelViewer(JFrame pf, ONCChild child, int wn)
	{
		super(pf, true);
		this.child = child;
		this.wn = wn;
		this.setTitle(String.format("Gift %d Ornament Label", wn+1));
		
		familyDB = FamilyDB.getInstance();
		GlobalVariablesDB gvDB = GlobalVariablesDB.getInstance();
		
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childGiftDB = ChildGiftDB.getInstance();
		if(childGiftDB != null)
			childGiftDB.addDatabaseListener(this);
		
		clonedGiftDB = ClonedGiftDB.getInstance();
		if(clonedGiftDB != null)
			clonedGiftDB.addDatabaseListener(this);

		labelPanel = new GiftLabelPanel();
		img = gvDB.getSeasonIcon().getImage();
		
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
			ONCChildGift addedWish = (ONCChildGift) dbe.getObject1();
		
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
			if(child != null && addedWish.getChildID() == child.getID() &&
					addedWish.getGiftNumber() < CLONED_GIFT_FIRST_GIFT_NUMBER)
				labelPanel.repaint();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildGift updatedWish = (ONCChildGift) dbe.getObject1();
			
			//If the current child is being displayed has a gift added, update the label
			if(child != null && updatedWish.getChildID() == child.getID() &&
					updatedWish.getGiftNumber() < CLONED_GIFT_FIRST_GIFT_NUMBER)
				labelPanel.repaint();
		}
		else if(dbe.getType().equals("ADDED_CLONED_GIFT"))
		{
//			System.out.println(String.format("Child Panel DB Event: Source %s, Type %s, Object %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			//Get the added wish to extract the child
			ClonedGift clonedGift = (ClonedGift) dbe.getObject1();
		
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
			if(child != null && clonedGift.getChildID() == child.getID() && 
					clonedGift.getGiftNumber() >= CLONED_GIFT_FIRST_GIFT_NUMBER)
				labelPanel.repaint();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CLONED_GIFT"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ClonedGift updatedClonedGift = (ClonedGift) dbe.getObject1();
			
			//If the current child is being displayed has a gift added, update the label
			if(child != null && updatedClonedGift.getChildID() == child.getID() && 
					updatedClonedGift.getGiftNumber() >= CLONED_GIFT_FIRST_GIFT_NUMBER)
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
	
	private class GiftLabelPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		private Font[] lFont;
		private AveryGiftLabelPrinter awlp;
		
		public GiftLabelPanel()
		{
			awlp = new AveryGiftLabelPrinter();
			
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
		    
		    if(wn < CLONED_GIFT_FIRST_GIFT_NUMBER)
		    {
		    	ONCChildGift cw = childGiftDB.getGift(child.getID(), wn);
			    SortGiftObject swo = new SortGiftObject(0, fam, child, cw);
			    
			    awlp.drawLabel(10, 20, swo.getGiftLabel(), lFont, img, g2d);
		    }
		    else
		    {
		    	ClonedGift cw = clonedGiftDB.getClonedGift(child.getID(), wn);
			    SortClonedGiftObject scgo = new SortClonedGiftObject(0, fam, child, cw);
			    
			    awlp.drawLabel(10, 20, scgo.getGiftLabel(), lFont, img, g2d);
		    }
			
		}
	}	
}
