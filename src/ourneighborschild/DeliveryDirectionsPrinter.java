package ourneighborschild;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class DeliveryDirectionsPrinter implements Printable
{
	private static final int DELIVERY_SHEET_X_OFFSET = 0;
	private ArrayList<DDPrintPage> ddpAL;
	private Image oncSeasonImg;
	private String oncSeason;
		
	public DeliveryDirectionsPrinter(ArrayList<DDPrintPage> ddpal, Image seasonImg, String season)
	{
		ddpAL = ddpal;
		oncSeasonImg = seasonImg;
		this.oncSeason = season;
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
		g2d.drawString("ONC " + season, x+452, y+24);

		//Draw header ONC Number
		g2d.setFont(ddFont[2]);
		g2d.drawString("Family # " + familyInfo[0], x, y+24); 	//ONC Number

		//Draw the delivery instructions
//		g2d.setFont(ddFont[1]);
//		g2d.drawString("Delivery Map & Directions", x+200, y+20);
		
		g2d.setFont(ddFont[6]);
		g2d.drawString(familyInfo[1], x, y+48);	//First and Last Name
		g2d.drawString(familyInfo[2], x, y+60);	//Street Address
		g2d.drawString(familyInfo[3], x, y+72);	//City, State, Zip
		
		//delivery content info
		g2d.setFont(ddFont[7]);	//should be underlined font
		g2d.drawString("# Bags", x+168, y+22);
		g2d.drawString("# Bikes", x+228, y+22);
		g2d.drawString("# Large Items", x+288, y+22);
//		g2d.drawString("---------", x+170, y+34);
//		g2d.drawString("---------", x+230, y+34);
//		g2d.drawString("------------------", x+290, y+34);
		g2d.drawString("______", x+168, y+26);
		g2d.drawString("______", x+228, y+26);
		g2d.drawString("___________", x+288, y+26);
		
		g2d.setFont(ddFont[8]);
		g2d.drawString(familyInfo[11], x+186, y+56);	//number of bags
		g2d.drawString(familyInfo[12], x+248, y+56); //number of bikes
		g2d.drawString(familyInfo[13], x+318, y+56); //number of large items
		
		//family phone info
		g2d.setFont(ddFont[6]);
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
	
	void printSignatureBlock(int x, int y, int width, int height, Font[] ddFont, Graphics2D g2d)
	{
		//draw the rectangle in red
		drawThickRect(g2d, x, y, width, height);
		//draw the text
		g2d.setFont(ddFont[4]);
		g2d.drawString("RECEIVED BY (sign): ________________________", x+4, y+18);		//first row
		g2d.drawString("RECEIVED BY (print): _______________________", x+4, y+38);		//first row
		g2d.drawString("DELIVERED BY (print): _____________________", x+4, y+58);		//second row
		g2d.setFont(ddFont[0]);
		g2d.drawString("Please complete the above, then use the Delivery Confirmation QR code", x+4, y+76);		//third row
		g2d.drawString("to upload a photo of the signature box to ONC's server.", x+4, y+88);	//fourth row
	}
	
	void printLabelRow(int x, int y, Font[] ddFont, String[] familyInfo, Graphics2D g2d)
	{
		g2d.setFont(ddFont[3]);
		g2d.drawString("Google Map", x, y+14);					
		g2d.drawString(" Family #" + familyInfo[0] + " Delivery Acceptance Signature", x+150, y+16);
		g2d.drawString("Delivery", x+450, y);
		g2d.drawString("Confirmation", x+440, y +16);
	}
	
	/*********************************************************************************************
	 * This method draws a gift check box rectangle at the specified x, y location.
	 * @param g2d
	 * @param x
	 * @param y
	 ********************************************************************************************/
	void drawThickRect(Graphics2D g2d, int x, int y, int width, int height)
	{
		float thickness = 1.5f;
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(thickness));
		g2d.drawRect(x, y, width, height);
		g2d.setStroke(oldStroke);
	}
	
	void printDeliveryDirectionsSheetFooter(int x, int y, int pgNum, int pgTotal, Font[] ddFont, Graphics2D g2d)
	{
		//Draw the page number 
		g2d.setFont(ddFont[0]);
		g2d.drawString(String.format("%d of %d", pgNum, pgTotal), x+262, y+76);
	}
	
	private void drawQRCode(String code, int x, int y, Graphics2D g2d) throws WriterException, UnsupportedEncodingException
	{
		String myCodeText = new String(code.getBytes("UTF-8"), "UTF-8");
		
		//create the hint map
		Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		
		//Now with zxing version 3.2.1 you could change border size (white border size to just 1)
		hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		
		//create the bar code
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(myCodeText, BarcodeFormat.QR_CODE, 80, 80, hintMap);
		int width = byteMatrix.getWidth();
		BufferedImage img = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
		img.createGraphics();

		//create the image.
		Graphics2D graphics = (Graphics2D) img.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, width, width);
		graphics.setColor(Color.BLACK);
		for(int i = 0; i < width; i++)
			for(int j = 0; j < width; j++)
				if(byteMatrix.get(i, j))
					graphics.fillRect(i, j, 1, 1);
		
		//release the graphics context
		graphics.dispose();
		
		//Draw image scaled to fit image clip region on the delivery card
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.drawImage(img, x, y, 80, 80, null);
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
		Font[] cFonts = new Font[10];
	    cFonts[0] = new Font("Times New Roman", Font.PLAIN, 10);//Instructions Font
	    cFonts[1] = new Font("Times New Roman", Font.BOLD, 14);	//Season Font
	    cFonts[2] = new Font("Times New Roman", Font.BOLD, 20);	//ONC Num Text Font
	    cFonts[3] = new Font("Times New Roman", Font.BOLD, 12);	//Child Font
	    cFonts[4] = new Font("Times New Roman", Font.BOLD, 13);	//Footer Font
	    cFonts[5] = new Font("Times New Roman", Font.ITALIC, 13);	//Table header Font
	    cFonts[6] = new Font("Times New Roman", Font.PLAIN, 12);	//Header Name, Address, Phone# Font
	    cFonts[7] = new Font("Times New Roman", Font.BOLD, 16);	//Bag Info Headers - Bikes, #Bags, etc.
	    cFonts[8] = new Font("Times New Roman", Font.BOLD, 28);	//Bag Info - Bikes, #Bags, etc.
	    cFonts[9] = new Font("Times New Roman", Font.PLAIN, 8);	//Bag Info - Bikes, #Bags, etc.
	    
//	    Hashtable<TextAttribute, Object> fontmap = new Hashtable<TextAttribute, Object>();
//	    fontmap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
//	    cFonts[7] = new Font("Calibri", Font.BOLD, 16).deriveFont(fontmap);	//Bag Info - Bikes, #Bags, etc.
		
		//Print page header			
		printDeliveryDirectionsSheetHeader(DELIVERY_SHEET_X_OFFSET, 0, oncSeasonImg, oncSeason, 
											ddpAL.get(page).getFamilyInfo(), cFonts, g2d);

/*
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
*/
		
		if(ddpAL.get(page).getPageNumber() == 1)
		{
			String[] familyInfo = ddpAL.get(page).getFamilyInfo();
			
			//print delivery confirmation qr code
			String mapURL = String.format("https://www.google.com/maps/dir/?api=1&destination=%s", ddpAL.get(page).getAddress());
			String deliveryURL = String.format("https://%s:%d/giftdelivery?year=%s&famid=%s&refnum=%s",
					"oncdms.org", 8902, oncSeason,familyInfo[18], familyInfo[19]);
			
			try
			{
				drawQRCode(mapURL, 20, 120, g2d);	//map qr code
				drawQRCode(deliveryURL, 460, 116, g2d);	//gift delivery confirmation qr code
			}
			catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (WriterException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			//print signature block
			printSignatureBlock(120, 123, 320, 96, cFonts, g2d);
			
			printLabelRow(28,100, cFonts, ddpAL.get(page).getFamilyInfo(), g2d);
		}
		
		//Print page footer
		int pagetotal = ddpAL.get(page).getPageTotal();	//1 or 2
		printDeliveryDirectionsSheetFooter(DELIVERY_SHEET_X_OFFSET, 656,
											ddpAL.get(page).getPageNumber(), pagetotal, cFonts, g2d);
	    	    
	    // tell the caller that this page is part of the printed document	   
	    return PAGE_EXISTS;
  	}	
}

