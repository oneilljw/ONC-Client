package ourneighborschild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

public class BatteryBarcodeSheetPrinter implements Printable
{
	private static final int NUMBER_BARCODES_LINE = 3;
	private static final int BARCODE_INITIAL_X_POS = 60;
	private static final int BARCODE_INITIAL_Y_POS = 80;
	private static final int WIDTH_BETWEEN_BARCODES = 190;
	private static final int HEIGHT_BETWEEN_BARCODES = 110;
//	private static final int HEIGHT_BETWEEN_SIZE_AND_QTY = 480;
	
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
		tempg2d.translate(x+2, y+4);
		
		tempg2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		tempg2d.scale(2.835, 2.835);	//scale from millimeters to points
//		tempg2d.scale(2.4, 2.4);	//scale from millimeters to points
		tempg2d.scale(3.4,3.4);	//scale from millimeters to points

		//set the bean content
		bean.generateBarcode(cc, code);
		
		//release the graphics context
		tempg2d.dispose();
		
		//draw the bounding rectangle around the BarCode
//		g2d.drawRect(x, y, 80, 70);
		
		//draw the label text above the bounding rectangle
		drawCenteredString(label, 80, x, y - 4, g2d, Color.BLACK);
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
		if (page > 1)	//'page' is zero-based 
		{ 
			return NO_SUCH_PAGE;
	    }
		
//		Graphics2D g2d = (Graphics2D)g;
		
		List<BarcodeInfo> barcodeInfoList = new ArrayList<BarcodeInfo>();
		if(page == 0)
		{
			//print battery size barcodes
			for(BatterySize bs : BatterySize.searchList())
				barcodeInfoList.add(new BarcodeInfo(bs));
			
			printPage(g, "Battery Size", barcodeInfoList);
		}
		else		//must be page 1
		{
			//print battery quantity barcodes
			for(BatteryQty bq : BatteryQty.printValues())
				barcodeInfoList.add(new BarcodeInfo(bq));
			
			printPage(g, "Battery Quantity", barcodeInfoList);
		}
/*		 
		//draw the battery size title and bounding box
		g2d.setFont(new Font("Calibri", Font.BOLD, 16));
		drawCenteredString("Battery Size", 590, 0, 40, g2d, Color.BLACK);
//		g2d.drawRect(30, 50, 530, 440);
		
		//print the battery size bar codes
		int count = 0, x =BARCODE_INITIAL_X_POS, y = BARCODE_INITIAL_Y_POS;
		for(BatterySize bs : BatterySize.searchList())
		{
			drawBarCode(bs.code(), bs.toString(), x, y, g2d);
			if(++count % NUMBER_BARCODES_LINE == 0)
			{
				x=BARCODE_INITIAL_X_POS;
				y+=HEIGHT_BETWEEN_BARCODES;
			}
			else
				x += WIDTH_BETWEEN_BARCODES;
		}
		
		//draw the battery quantity title and bounding box
		drawCenteredString("Battery Quantity", 590, 0, 520, g2d, Color.BLACK);
//		g2d.drawRect(30, 528, 530, 216);
		
		//print the battery quantity bar codes
		count = 0; x = BARCODE_INITIAL_X_POS; y = BARCODE_INITIAL_Y_POS + HEIGHT_BETWEEN_SIZE_AND_QTY;
		for(BatteryQty bq : BatteryQty.printValues())
		{
			drawBarCode(bq.code(), bq.name(), x, y, g2d);
			if(++count % NUMBER_BARCODES_LINE == 0)
			{
				x = BARCODE_INITIAL_X_POS;
				y += HEIGHT_BETWEEN_BARCODES;
			}
			else
				x += WIDTH_BETWEEN_BARCODES;
		}
*/		  
	     /* tell the caller that this page is part of the printed document */
	     return PAGE_EXISTS;
	}
	
	void printPage(Graphics g, String title, List<BarcodeInfo> infoList)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		int count = 0;
		int x = BARCODE_INITIAL_X_POS;
		int y = BARCODE_INITIAL_Y_POS; 
		
		//draw the battery quantity title and bounding box
		g2d.setFont(new Font("Calibri", Font.BOLD, 16));
		drawCenteredString(title, 590, 0, BARCODE_INITIAL_Y_POS-30, g2d, Color.BLACK);
		
		//print the bar codes
		for(BarcodeInfo bci : infoList)
		{
			drawBarCode(bci.code(), bci.label(), x, y, g2d);
			if(++count % NUMBER_BARCODES_LINE == 0)
			{
				x = BARCODE_INITIAL_X_POS;
				y += HEIGHT_BETWEEN_BARCODES;
			}
			else
				x += WIDTH_BETWEEN_BARCODES;
		}
	}
}
