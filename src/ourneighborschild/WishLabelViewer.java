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

//		this.getContentPane().setBackground(Color.WHITE);
		
		labelPanel = new WishLabelPanel();
		
		img = GlobalVariables.getSeasonIcon().getImage();
		
		this.setContentPane(labelPanel);
		
		this.setSize(300, 140);
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
			ONCChildWish addedWish = (ONCChildWish) dbe.getObject();
		
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
//			if(child != null && addedWish.getChildID() == child.getID())
//				displayLabel(child, addedWish.getWishNumber());
			
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildWish updatedWish = (ONCChildWish) dbe.getObject();
			
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
//			if(child != null && updatedWish.getChildID() == child.getID())	
//				displayLabel(child, updatedWish.getWishNumber());
		}
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject();
//			if(child != null && child.getID() == updatedChild.getID())
//			{
//				//the age or gender of the child may have changed, update the label
//				displayLabel(updatedChild, wn);
//			}
		}
	}
	
	private class WishLabelPanel extends JPanel
	{
		private static final int AVERY_LABEL_X_BARCODE_OFFSET = 0;
		private static final int AVERY_LABEL_Y_BARCODE_OFFSET = 4;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		public WishLabelPanel()
		{
			this.setBackground(Color.white);
		}
		
		/**
		 * paintComponent paints the shapes that are
		 * in the shapeList
		 */
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.scale(X_DISPLAY_SCALE_FACTOR, Y_DISPLAY_SCALE_FACTOR);
			
			Font[] lFont = new Font[3];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 11);
		    lFont[1] = new Font("Calibri", Font.BOLD, 11);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 10);	
		    
		    //create the sort wish object list. We're reusing the AveryWishLabelPrinter class
		    ONCFamily fam = familyDB.getFamily(child.getFamID());
			ONCChildWish cw = childWishDB.getWish(child.getID(), wn);
		    SortWishObject swo = new SortWishObject(0, fam, child, cw);
		    String[] line = swo.getWishLabel();
		    
		    AveryWishLabelPrinter awlp = new AveryWishLabelPrinter(null, null, 0);
		    awlp.drawLabel(10, 20, line, lFont, img, g2d);
		}
/*		
		void drawLabel(int x, int y, String[] line, Font[] lFont, Image img, Graphics2D g2d)
		{
			//draw either the season Icon or a bar code
			if(GlobalVariables.getInstance().includeBarcodeOnLabels())
				drawBarCode(line[4], x, y, g2d);	//draw the bar code on label
			else
				drawHolidayIcon(x, y, img, g2d);	//draw this seasons ONC icon on label
  
		    //Draw the label text, either 3 or 4 lines, depending on the wish base + detail length
			g2d.setColor(Color.BLACK);
		    g2d.setFont(lFont[0]);
		    drawCenteredString(line[0], 120, x+50, y+5, g2d);	//Draw line 1
		    
		    g2d.setFont(lFont[1]);
		    drawCenteredString(line[1], 120, x+50, y+20, g2d);	//Draw line 2
		    
		    if(line[3] == null)	//Only a 3 line label
		    {
		    	g2d.setFont(lFont[2]);
		    	drawCenteredString(line[2], 120, x+50, y+35, g2d);	//Draw line 3
		    }
		    else	//A 4 line label
		    {	    	
		    	drawCenteredString(line[2], 120, x+50, y+35, g2d);	//Draw line 3	    	
		    	g2d.setFont(lFont[2]);
		    	drawCenteredString(line[3], 120, x+50, y+50, g2d);	//Draw line 4
		    }
		}
		
		private void drawHolidayIcon(int x, int y, Image img, Graphics2D g2d)
		{
			double scaleFactor = (72d / 300d) * 2;
    	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null),null); 
		}
		
		private void drawCenteredString(String s, int width, int XPos, int YPos, Graphics2D g2d)
		{  
	        int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();  
	        int start = width/2 - stringLen/2; 
	        g2d.drawString(s, start + XPos, YPos);  
		}

		private void drawBarCode(String code, int x, int y, Graphics2D g2d)
		{
			//create the bar code
			
			AbstractBarcodeBean bean;
			if(gvs.getBarcodeCode() == Barcode.CODE128)
				 bean = new Code128Bean();
			else
				bean = new UPCEBean();
			
			//get a temporary graphics context
			Graphics2D tempg2d = (Graphics2D) g2d.create();
			
			//create the canvass
			Java2DCanvasProvider cc = new Java2DCanvasProvider(tempg2d, 0);
			tempg2d.translate(x + AVERY_LABEL_X_BARCODE_OFFSET, y + AVERY_LABEL_Y_BARCODE_OFFSET);
//			tempg2d.translate(x, y);
			
			tempg2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//			tempg2d.scale(2.835, 2.835);	//scale from millimeters to points
			tempg2d.scale(2.4, 2.4);	//scale from millimeters to points

			//set the bean content
			bean.generateBarcode(cc, code);
			
			//release the graphics context
			tempg2d.dispose(); 
		}
*/		
	}	
}
