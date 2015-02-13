package OurNeighborsChild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WishLabelViewer extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	private JLabel wishLabel;
	
	WishLabelViewer(JFrame pf)
	{
		super(pf);
		
		GlobalVariables gvs = GlobalVariables.getInstance();
		
//		this.setUndecorated(true);
		this.setTitle("Ornament Label");
		this.getContentPane().setBackground(Color.WHITE);
		
		SimpleDateFormat sYear = new SimpleDateFormat("yy");
		int idx = Integer.parseInt(sYear.format(gvs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
		BufferedImage bi = resize(gvs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage(), 70, 70);
		wishLabel = new JLabel(new ImageIcon(bi));
		wishLabel.setHorizontalAlignment(JLabel.LEFT);
		wishLabel.setText("<html><center><i>17 yr. old Girl</i><br><b>Accessories - Purse<br>line 3<br></b>ONC 2014 | Family # 001</center></html>");
		
		//Create a content panel for the frame and add components to it.
		this.getContentPane().setLayout(new BorderLayout(20, 0));
		this.getContentPane().add(new JPanel(), BorderLayout.WEST);
		this.getContentPane().add(wishLabel, BorderLayout.CENTER);
		
		pack();
		this.setSize(300, 140);
	}
	
	void displayLabel(SortWishObject swo)
	{
		String[] line = swo.getWishLabel();
		String labelText;
		if(line[3] == null)
			labelText = String.format("<html><center><i>%s</i><br><b>%s</b><br>%s</center></html>",
				line[0], line[1], line[2]);
		else
			labelText = String.format("<html><center><i>%s</i><br><b>%s<br>%s<br></b>%s</center></html>",
					line[0], line[1], line[2], line[3]);
		
		wishLabel.setText(labelText);
		this.setVisible(true);
	}
	
	void displayLabel(ONCChild c, int wn)
	{
		String[] line = getWishLabel(c, wn);
		String labelText;
		if(line[3] == null)
			labelText = String.format("<html><center><i>%s</i><br><b>%s</b><br>%s</center></html>",
				line[0], line[1], line[2]);
		else
			labelText = String.format("<html><center><i>%s</i><br><b>%s<br>%s<br></b>%s</center></html>",
					line[0], line[1], line[2], line[3]);
		
		wishLabel.setText(labelText);
		if(!this.isVisible())
			this.setVisible(true);
	}
	
	public static BufferedImage resize(Image image, int width, int height)
	{
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = (Graphics2D) bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(image, 0, 0, width, height, null);
	    g2d.dispose();
	    return bi;
	}
	
	String[] getWishLabel(ONCChild c, int wn)
	{	
		GlobalVariables gvs = GlobalVariables.getInstance();
		ONCWishCatalog cat = ONCWishCatalog.getInstance();
		Families familyDB = Families.getInstance();
		ChildWishDB cwDB = ChildWishDB.getInstance();
		ONCFamily fam = familyDB.getFamily(c.getFamID());
		ONCChildWish cw = cwDB.getWish(c.getID(), wn);
		
		String[] line = new String[4];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
		line[0] = c.getChildAge() + " " + c.getChildGender();
		
		//Combine the wish base and wish detail and return one or two lines depending
		//on the length of the combined string. If two lines are required, break the
		//string on a word boundary. Limit the second string to a max number of
		//characters based on MAX_LABEL_LINE_LENGTH
		StringBuilder l1 = new StringBuilder(cat.getWishByID(cw.getWishID()).getName());
		
		if(!cw.getChildWishDetail().isEmpty())	//Wish detail may need two lines
		{
			l1.append(" - ");
		
			String[] wishDetail = cw.getChildWishDetail().split(" ");
			int index = 0;
		
			//Build 2nd line. Limit it to MAX_LABEL_LINE_LENGTH on a word boundary
			while(index < wishDetail.length &&
					l1.length() + wishDetail[index].length() + 1 < MAX_LABEL_LINE_LENGTH)
			{
				l1.append(wishDetail[index++] + " ");
			}
			line[1] = l1.toString();
		
			//If wish is too long to fit on one line, break it into a 2nd line
			StringBuilder l2 = new StringBuilder("");
			while(index < wishDetail.length &&
				l2.length() + wishDetail[index].length() + 1 < MAX_LABEL_LINE_LENGTH)
			{
				l2.append(wishDetail[index++] + " ");
			}
		
			//If the wish required two lines make the 3rd line the ONC Year line 4
			//else make the ONC Year line 3
			if(l2.length() > 0)
			{
				line[2] = l2.toString();
				line[3] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + fam.getONCNum();
			}
			else
			{			
				line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
						" |  Family # " + fam.getONCNum();
				line[3] = null;
			}
		}
		else	//No wish detail
		{
			line[1] = l1.toString();
			line[2] = "ONC " + sYear.format(gvs.getSeasonStartDate()) + 
					" |  Family # " + fam.getONCNum();
			line[3] = null;
		}

		return line;
	}

}
