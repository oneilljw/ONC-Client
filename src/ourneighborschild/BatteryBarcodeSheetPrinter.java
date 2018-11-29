package ourneighborschild;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

public class BatteryBarcodeSheetPrinter implements Printable
{
	private final String[] batteryQty = {"1", "2", "3","4","5", "6"};
	
	private final String[] batteryQtyCode = {"0002001", "0002002", "0002003", "0002004", "0002005", "0002006"};
			
	private GlobalVariablesDB gvs;
	
	//constructor used when drawing labels on a Swing component
	public BatteryBarcodeSheetPrinter()
	{
		gvs = GlobalVariablesDB.getInstance();
	}
	
	private void drawBarCode(String code, String label, int x, int y, Graphics2D g2d)
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
//		tempg2d.translate(x + AVERY_LABEL_X_BARCODE_OFFSET, y + AVERY_LABEL_Y_BARCODE_OFFSET);
		tempg2d.translate(x+2, y+4);
		
		tempg2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		tempg2d.scale(2.835, 2.835);	//scale from millimeters to points
//		tempg2d.scale(2.4, 2.4);	//scale from millimeters to points
		tempg2d.scale(3.2,3.2);	//scale from millimeters to points

		//set the bean content
		bean.generateBarcode(cc, code);
		
		//release the graphics context
		tempg2d.dispose();
		
		//draw the bounding rectangle around the BarCode
		g2d.drawRect(x, y, 80, 70);
		
		//draw the label text below the bounding rectangle
		drawCenteredString(label, 80, x, y+96, g2d, Color.BLACK);
	}
	
	private void drawCenteredString(String s, int width, int XPos, int YPos, Graphics2D g2d, Color color)
	{  
		Color originalColor = g2d.getColor();
		g2d.setColor(color);
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();  
        int start = width/2 - stringLen/2;  
        g2d.drawString(s, start + XPos, YPos);
        g2d.setColor(originalColor);
	}

	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException
	{
		if (page > 0)	//'page' is zero-based 
		{ 
			return NO_SUCH_PAGE;
	    }
		
		 Graphics2D g2d = (Graphics2D)g;
		
		//print the battery size barcodes
		int count = 0, x = 40, y = 40;
		for(BatterySize bs : BatterySize.values())
		{
			drawBarCode(bs.code(), bs.toString(), x, y, g2d);
			if(++count % 4 == 0)
			{
				x=40;
				y+=100;
			}
			else
			{
				x += 120;
			}
			
			System.out.println(String.format("BatteryBarcodePrinter: printing battery size %s at (%d,%d)",
					bs.toString(), x, y));
		}
		  
	     /* tell the caller that this page is part of the printed document */
	     return PAGE_EXISTS;
	}
}
