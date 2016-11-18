package ourneighborschild;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;

/**************************************************************************************
 * This class is a base class for printing ONC Packaging Sheet and ONC PrePackaging 
 * Sheets. It implements common methods for drawing rectangles and printing the child
 * gift verification section of the two sheets. Printing sheet headers and footers is
 * left to the inheriting class and the methods for each are abstract in this class. 
 * 
 * This class requires an array list, passed in the constructor, that hold a
 * ONCVeriicationSheet object for each page in the document to be printed
 * by this class.
 ************************************************************************************/
public abstract class VerificationSheetPrinter implements Printable
{
	private static final int VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE = 5;
	private static final int VERIFICATION_SHEET_X_OFFSET = 16;
	private static final int NUM_GIFTS_PER_CHILD = 3;
	
	ArrayList<ONCVerificationSheet> vsAL;
	Image img;
	String oncSeason;
	ChildWishDB cwDB;
	ONCWishCatalog cat;
	
	VerificationSheetPrinter(ArrayList<ONCVerificationSheet> vsal)
	{
		vsAL = vsal;
		cwDB = ChildWishDB.getInstance();
		cat = ONCWishCatalog.getInstance();
		
	}
	
	/*********************************************************************************************
	 * This method draws a gift check box rectangle at the specified x, y location. If the
	 * checkmark parameter is true, a check mark is printed in the box.
	 * @param g2d
	 * @param x
	 * @param y
	 ********************************************************************************************/
	void drawThickRect(Graphics2D g2d, int x, int y, int width, int height, boolean checkmark)
	{
		float thickness = new Float(1.5);
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(thickness));
		g2d.drawRect(x, y, width, height);
		g2d.setStroke(oldStroke);
		
		if(checkmark)
		{
			Font oldFont = g2d.getFont();
			g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
			g2d.drawString("\u2713", x+1, y+height-1); 	//Check mark
			g2d.setFont(oldFont);
		}
	}
	
	abstract void printVerificationSheetHeader(int x, int y, Image img, String season, String fn,
			Font[] psFont, Graphics2D g2d);
	
	abstract void printVerificationSheetFooter(int x, int y, int pgNum, int pgTotal, String zBikes, Font[] psFont, Graphics2D g2d);

	abstract void printVerificationSheetChild(int x, int y, int childnum, String childdata,
												String giftdata[], Font[] psFont, Graphics2D g2d);

	
	@Override
	public int print (Graphics g, PageFormat pf, int page) throws PrinterException
	{		
		if(page > vsAL.size())	//'page' is zero-based 
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
	    cFonts[0] = new Font("Calibri", Font.PLAIN, 12);//Instructions Font
	    cFonts[1] = new Font("Calibri", Font.BOLD, 14);	//Season Font
	    cFonts[2] = new Font("Calibri", Font.BOLD, 20);	//ONC Num Text Font
	    cFonts[3] = new Font("Calibri", Font.BOLD, 12);	//Child Font
	    cFonts[4] = new Font("Calibri", Font.BOLD, 13);	//Footer Font
	    cFonts[5] = new Font("Calibri", Font.BOLD, 44);	//Footer # of Bikes Font
	    cFonts[6] = new Font("Calibri", Font.BOLD + Font.ITALIC, 14);	//Packaged By: Font
		
		//Print page header
	    String oncFamNum = "Family " + vsAL.get(page).getONCNum();
		printVerificationSheetHeader(VERIFICATION_SHEET_X_OFFSET, 0, img, oncSeason, oncFamNum, cFonts, g2d);
		
		int nchildrenonpage;
		//For each child on the page, print the childs gift info
		if(vsAL.get(page).getChildArrayList().size() >  VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE && 
				vsAL.get(page).getpageNum() == 0)
			nchildrenonpage = VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE;
		else
			nchildrenonpage = vsAL.get(page).getChildArrayList().size();
			
		int childnum = vsAL.get(page).getpageNum() *  VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE;	//0 or 5 
		
		String childdata;
		String[] giftdata = new String[3];
		String[] restrictions = {" ", "*", "#"};
				
		int y = 64;
		while(childnum < nchildrenonpage)
		{
			childdata = vsAL.get(page).getChildArrayList().get(childnum).getChildAge() + " " +
							vsAL.get(page).getChildArrayList().get(childnum).getChildGender();
			for(int wn=0; wn < NUM_GIFTS_PER_CHILD; wn++)
			{
				
				
				ONCChild c = vsAL.get(page).getChildArrayList().get(childnum);
				ONCChildWish cw = cwDB.getWish(c.getChildWishID(wn));
				ONCWish catWish = cat.getWishByID(cw.getWishID());
				String wishName = catWish == null ? "None" : catWish.getName();
				
				String restriction = restrictions[cw.getChildWishIndicator()];
				String wish = wishName;
				String detail = cw.getChildWishDetail();
				giftdata[wn] = restriction + wish + "- " +  detail;
			}
			
			printVerificationSheetChild(26, y, childnum+1, childdata, giftdata, cFonts, g2d);
			childnum++;
			y += 118;
		}			
		
		//Print page footer
		int pagetotal = vsAL.get(page).getChildArrayList().size() <= VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE ? 1 : 2;	//1 or 2
		String zBikes = vsAL.get(page).getNumOfBikes();
		printVerificationSheetFooter(16, 656, vsAL.get(page).getpageNum()+1, pagetotal, zBikes, cFonts, g2d);
	    	    
	    // tell the caller that this page is part of the printed document	   
	    return PAGE_EXISTS;
  	}
}
