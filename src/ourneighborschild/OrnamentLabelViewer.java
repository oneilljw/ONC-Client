package ourneighborschild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class OrnamentLabelViewer extends JDialog implements DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	GlobalVariablesDB gvDB;
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildGiftDB childGiftDB;
	private ClonedGiftDB clonedGiftDB;
	
	private GiftLabelPanel labelPanel;
	
	private final Image img;
	
	private ONCChild child;	//current child being displayed
	private ONCChildGift currGift;	//current gift being displayed
	
	OrnamentLabelViewer(JFrame pf, ONCChildGift childGift, ONCChild child)
	{
		super(pf, true);
		this.child = child;
		this.currGift = childGift;
		this.setTitle("Gift Ornament Label");
		
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
			//Get the added wish to extract the child
			ONCChildGift addedGift = (ONCChildGift) dbe.getObject1();
		
			//If the current child is being displayed has a gift added update the 
			//gift label to show the added wish
			if(currGift != null && addedGift.getChildID() == currGift.getChildID() &&
					addedGift.getGiftNumber() == currGift.getGiftNumber())
				labelPanel.repaint();
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildGift updatedGift = (ONCChildGift) dbe.getObject1();
			
			//If the current gift is being displayed has a gift added, update the label
			if(currGift != null && updatedGift.getChildID() == currGift.getChildID() &&
					updatedGift.getGiftNumber() == currGift.getGiftNumber())
				labelPanel.repaint();
		}
		else if(dbe.getType().equals("ADDED_LIST_CLONED_GIFTS"))
		{		
			//extract the list of added clones
			@SuppressWarnings("unchecked")
			List<ONCChildGift> addedCGList = (List<ONCChildGift>) dbe.getObject1();
			for(ONCChildGift clonedGift : addedCGList)
			{
				//If the current child is being displayed has a cloned gift added update the 
				//gift label to show the added gift
				if(currGift != null && clonedGift.getChildID() == currGift.getChildID() && 
					clonedGift.getGiftNumber() == currGift.getGiftNumber())
				{
					labelPanel.repaint();
					break;
				}
			}
		}
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject1();
			if(currGift != null && currGift.getChildID() == updatedChild.getID())
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
		    lFont[0] = new Font("Times New Roman", Font.ITALIC, 11);
		    lFont[1] = new Font("Times New Roman", Font.BOLD, 11);
		    lFont[2] = new Font("Times New Roman", Font.PLAIN, 10);
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
		    
		    //create the sort wish object list
			if(currGift != null && child != null)
			{
				ONCFamily fam = familyDB.getFamily(child.getFamID());
				SortGiftObject swo = new SortGiftObject(0, fam, child, currGift);
			    
				awlp.drawLabel(10, 20, swo.getGiftLabel(), lFont, img, g2d);
			}
		}
	}	
}
