package ourneighborschild;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import org.json.JSONException;

public class DeliveryDirectionsPrinter implements Printable
{
	private static final int DELIVERY_SHEET_X_OFFSET = 0;
	private ArrayList<DDPrintPage> ddpAL;
	private Image oncSeasonImg;
	private String oncSeason;
		
	public DeliveryDirectionsPrinter(ArrayList<DDPrintPage> ddpal, Image seasonImg, String oncSeason)
	{
		ddpAL = ddpal;
		oncSeasonImg = seasonImg;
		this.oncSeason = oncSeason;
	}
	void printDeliveryDirectionsSheetHeader(int x, int y, Image img, String season,
			String[] familyInfo, Font[] ddFont, Graphics2D g2d)
	{			     
		double scaleFactor = (72d / 300d) * 2;

		// Now we perform our rendering 	       	    
		int destX1 = (int) (img.getWidth(null) * scaleFactor);
		int destY1 = (int) (img.getHeight(null) * scaleFactor);

		//Draw image scaled to fit image clip region on the label
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(img, x+520, y, x+520+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 

		//Draw the ONC Season
		g2d.setFont(ddFont[1]);
		g2d.drawString(season, x+466, y+20);

		//Draw header ONC Number
		g2d.setFont(ddFont[2]);
		g2d.drawString("Family # " + familyInfo[0], x, y+24); 	//ONC Number

		//Draw the delivery instructions
		g2d.setFont(ddFont[1]);
		g2d.drawString("Delivery Map & Directions", x+200, y+20);
		
		g2d.setFont(ddFont[6]);
		g2d.drawString(familyInfo[1], x, y+48);	//First and Last Name
		g2d.drawString(familyInfo[2], x, y+60);	//Street Address
		g2d.drawString(familyInfo[3], x, y+72);	//City, State, Zip	    
		g2d.drawString("Home Phone: " + familyInfo[5], x+422, y+60);	//Home Phone
	    g2d.drawString("Alt. Phone: " + familyInfo[7], x+434, y+72);	//Other Phone
	}
	
	void printDeliveryDirectionsMap(int x, int y, Image map, Graphics2D g2d)
	{
		double scaleFactor = (72d / 300d) * 4;

		// Now we perform our rendering 	       	    
		int destX1 = (int) (map.getWidth(null) * scaleFactor);
		int destY1 = (int) (map.getHeight(null) * scaleFactor);

		//Draw image scaled to fit image clip region on the label
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(map, x, y, x+destX1, y+destY1, 0,0, map.getWidth(null),map.getHeight(null), null);
	}
	
	void printDeliveryDirectionsSheetTable(int x, int y, ArrayList<String[]> rows, Font[] ddFont, Graphics2D g2d)
	{	
		//Draw the table header
		g2d.setFont(ddFont[5]);
		g2d.drawString("#", x, 440);		//Step column
		g2d.drawString("Directions", x+14, 440);		//Directions column
		g2d.drawString("Dist.", x+504, 440);	//Distance column
		g2d.drawString("Time", x+544, 440);	//Duration column
		
		//Draw the table rows
		g2d.setFont(ddFont[0]);
		for(int row=0; row<rows.size(); row++)
		{
			int row_offset = 458 + (row * 18);
			g2d.drawString(rows.get(row)[0], x, row_offset);		//Step column
			g2d.drawString(rows.get(row)[1], x+14, row_offset);		//Directions column
			g2d.drawString(rows.get(row)[2], x+504, row_offset);	//Distance column
			g2d.drawString(rows.get(row)[3], x+544, row_offset);	//Duration column
		}
	}
	
	void printDeliveryDirectionsSheetFooter(int x, int y, int pgNum, int pgTotal, Font[] ddFont, Graphics2D g2d)
	{
		//Draw the page number 
		g2d.setFont(ddFont[0]);
		g2d.drawString(String.format("%d of %d", pgNum, pgTotal), x+262, y+76);
	}
	@Override
	public int print (Graphics g, PageFormat pf, int page) throws PrinterException
	{		
		if(page > ddpAL.size())	//'page' is zero-based 
		{
			return NO_SUCH_PAGE;
	    }
		
		//Create 2d graphics context
		// User (0,0) is typically outside the imageable area, so we must
	    //translate by the X and Y values in the PageFormat to avoid clipping
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.translate(pf.getImageableX(), pf.getImageableY());
		
		//Create fonts
		Font[] cFonts = new Font[7];
	    cFonts[0] = new Font("Calibri", Font.PLAIN, 10);//Instructions Font
	    cFonts[1] = new Font("Calibri", Font.BOLD, 14);	//Season Font
	    cFonts[2] = new Font("Calibri", Font.BOLD, 20);	//ONC Num Text Font
	    cFonts[3] = new Font("Calibri", Font.BOLD, 12);	//Child Font
	    cFonts[4] = new Font("Calibri", Font.BOLD, 13);	//Footer Font
	    cFonts[5] = new Font("Calibri", Font.ITALIC, 13);	//Table header Font
	    cFonts[6] = new Font("Calibri", Font.PLAIN, 12);	//Header Name, Address, Phone# Font
		
		//Print page header			
		printDeliveryDirectionsSheetHeader(DELIVERY_SHEET_X_OFFSET, 0, oncSeasonImg, oncSeason, 
											ddpAL.get(page).getFamilyInfo(), cFonts, g2d);
		
		//If this is first page for family, print the map
		if(ddpAL.get(page).getPageNumber() == 1)	//family page numbers are '1' based
		{
			//Get the map
			DrivingDirections ddir = new DrivingDirections();
			BufferedImage map = null;
			try 
			{
				map = ddir.getDirectionsGoogleMap(ddpAL.get(page).getRoute(), ddpAL.get(page).getAddress());
			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(map != null)
				printDeliveryDirectionsMap(DELIVERY_SHEET_X_OFFSET, 80, map, g2d);
			else
				System.out.println("Map wasn't retrieved");
		}
					
		//Print page direction table
		printDeliveryDirectionsSheetTable(DELIVERY_SHEET_X_OFFSET, 500,
											ddpAL.get(page).getStepsAL(), cFonts, g2d);
		//Print page footer
		int pagetotal = ddpAL.get(page).getPageTotal();	//1 or 2
		printDeliveryDirectionsSheetFooter(DELIVERY_SHEET_X_OFFSET, 656,
											ddpAL.get(page).getPageNumber(), pagetotal, cFonts, g2d);
	    	    
	    // tell the caller that this page is part of the printed document	   
	    return PAGE_EXISTS;
  	}	
}

