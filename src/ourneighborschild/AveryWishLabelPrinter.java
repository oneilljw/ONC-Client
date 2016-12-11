package ourneighborschild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;

import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

public class AveryWishLabelPrinter implements Printable 
{
	private static final int AVERY_LABELS_PER_PAGE = 30;	//5160 label sheet
	private static final int AVERY_COLUMNS_PER_PAGE = 3;
	private static final int AVERY_LABEL_HEIGHT = 72;
	private static final int AVERY_LABEL_WIDTH = 196;
	private static final int AVERY_LABEL_X_BARCODE_OFFSET = 0;
	private static final int AVERY_LABEL_Y_BARCODE_OFFSET = 4;
	
	private GlobalVariables gvs;
	private ONCTable sortTable;
	private List<SortWishObject> stAL; 
	private int totalNumOfLabelsToPrint;
	private Point position;
	
	//constructor used when drawing labels on a Swing component
	public AveryWishLabelPrinter()
	{
		gvs = GlobalVariables.getInstance();
		this.stAL = null;
		this.sortTable = null;
		this.totalNumOfLabelsToPrint = 0;
		this.position = new Point(0,0);
	}
	
	//constructor used when drawing labels on a sheet via the printable interface
	public AveryWishLabelPrinter(List<SortWishObject> stAL, ONCTable sortTable, int numOfLabels, Point position)
	{
		gvs = GlobalVariables.getInstance();
		this.stAL = stAL;
		this.sortTable = sortTable;
		this.totalNumOfLabelsToPrint = numOfLabels;
		this.position = position;
	}
	
	void drawLabel(int x, int y, String[] line, Font[] lFont, Image img, Graphics2D g2d)
	{
		//draw either the season Icon or a bar code
		if(GlobalVariables.getInstance().includeBarcodeOnLabels())
			drawBarCode(line[4], x, y, g2d);	//draw the bar code on label
		else
			drawHolidayIcon(x, y, img, g2d);	//draw this seasons ONC icon on label

	    //Draw the label text, either 3 or 4 lines, depending on the wish base + detail length
	    g2d.setFont(lFont[0]);
	    drawCenteredString(line[0], 120, x+50, y+5, g2d, Color.BLACK);	//Draw line 1
	    
	    g2d.setFont(lFont[1]);
	    drawCenteredString(line[1], 120, x+50, y+20, g2d, Color.BLACK);	//Draw line 2
	    
	    if(line[3] == null)	//Only a 3 line label
	    {
	    	g2d.setFont(lFont[2]);
	    	drawCenteredString(line[2], 120, x+50, y+35, g2d, Color.BLACK);	//Draw line 3
	    }
	    else	//A 4 line label
	    {	    	
	    	drawCenteredString(line[2], 120, x+50, y+35, g2d, Color.BLACK);	//Draw line 3	    	
	    	g2d.setFont(lFont[2]);
	    	drawCenteredString(line[3], 120, x+50, y+50, g2d, Color.BLACK);	//Draw line 4
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
	
	private void drawCenteredString(String s, int width, int XPos, int YPos, Graphics2D g2d, Color color)
	{  
		Color originalColor = g2d.getColor();
		g2d.setColor(color);
        int stringLen = (int) g2d.getFontMetrics().getStringBounds(s, g2d).getWidth();  
        int start = width/2 - stringLen/2;  
        g2d.drawString(s, start + XPos, YPos);
        g2d.setColor(originalColor);
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
//		tempg2d.translate(x, y);
		
		tempg2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//		tempg2d.scale(2.835, 2.835);	//scale from millimeters to points
		tempg2d.scale(2.4, 2.4);	//scale from millimeters to points

		//set the bean content
		bean.generateBarcode(cc, code);
		
		//release the graphics context
		tempg2d.dispose();
		
		//draw the corner hat
		final Image img = GlobalVariables.getInstance().getImage(45);
		
		double scaleFactor = (72d / 300d) * 2;
	     
	    // Now we perform our rendering 	       	    
	    int destX1 = (int) (img.getWidth(null) * scaleFactor);
	    int destY1 = (int) (img.getHeight(null) * scaleFactor);
	    
	    //Draw image scaled to fit image clip region on the label
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.drawImage(img, x-7, y-7, x+destX1-7, y+destY1-7, 0,0, img.getWidth(null),img.getHeight(null),null); 
	}

	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException
	{
		if (page > (totalNumOfLabelsToPrint+1)/AVERY_LABELS_PER_PAGE)		//'page' is zero-based 
		{ 
			return NO_SUCH_PAGE;
	    }
		
		final Image img = GlobalVariables.getSeasonIcon().getImage();

		Font[] lFont = new Font[3];
	    lFont[0] = new Font("Calibri", Font.ITALIC, 11);
	    lFont[1] = new Font("Calibri", Font.BOLD, 11);
	    lFont[2] = new Font("Calibri", Font.PLAIN, 10);	     
	    
	    int endOfSelection = 0, index = 0;
	    int[] row_sel = sortTable.getSelectedRows();
 		if(sortTable.getSelectedRowCount() > 0)
 		{	//print a label for each row selected
 			index = page * AVERY_LABELS_PER_PAGE;
 			endOfSelection = row_sel.length;
 		}
	    	 
	    // User (0,0) is typically outside the imageable area, so we must
	    //translate by the X and Y values in the PageFormat to avoid clipping
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.translate(gvs.getAveryLabelOffset().x, gvs.getAveryLabelOffset().y);	//For a 8150 Label
	    
	    String line[];
	    int row = 0, col = 0;
	    if(totalNumOfLabelsToPrint == 1)
	    {
	    	row = position.y;
	    	col = position.x;
	    }
	    
	    while(row < AVERY_LABELS_PER_PAGE/AVERY_COLUMNS_PER_PAGE && index < endOfSelection)
	    {
	    	line = stAL.get(row_sel[index++]).getWishLabel();
	    	drawLabel(col * AVERY_LABEL_WIDTH, row * AVERY_LABEL_HEIGHT, line, lFont, img, g2d);	
	    	if(++col == AVERY_COLUMNS_PER_PAGE)
	    	{ 
	    		row++; 
	    		col = 0;
	    	} 	
	    }
	    	    
	     /* tell the caller that this page is part of the printed document */
	     return PAGE_EXISTS;
	}
}
