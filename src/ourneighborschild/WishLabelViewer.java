package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WishLabelViewer extends JDialog implements DatabaseListener, EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int MAX_LABEL_LINE_LENGTH = 26;
	
	//database references
	private GlobalVariables gvs;
	private ONCWishCatalog cat;
	private Families familyDB;
	private ChildDB childDB;
	private ChildWishDB childWishDB;
	
	private JLabel wishLabel;
	
	
	private ONCChild child;	//current child being displayed
	private int wn;			//current wish number being displayed
	
	WishLabelViewer(JFrame pf)
	{
		super(pf);
		
		gvs = GlobalVariables.getInstance();
		cat = ONCWishCatalog.getInstance();
		familyDB = Families.getInstance();
		
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childWishDB = ChildWishDB.getInstance();
		if(childWishDB != null)
			childWishDB.addDatabaseListener(this);

		this.setTitle("Ornament Label");
		this.getContentPane().setBackground(Color.WHITE);
		
		JPanel dummyPanel = new JPanel();
		dummyPanel.setBackground(Color.WHITE);
		
		wishLabel = new JLabel();
		wishLabel.setHorizontalAlignment(JLabel.LEFT);
		wishLabel.setIcon(GlobalVariables.getSeasonIcon());
		
		//Create a content panel for the frame and add components to it.
		this.getContentPane().setLayout(new BorderLayout(20, 0));
		this.getContentPane().add(dummyPanel, BorderLayout.WEST);
		this.getContentPane().add(wishLabel, BorderLayout.CENTER);
		
		pack();
		this.setSize(320, 140);
		this.setResizable(false);
	}
	
	void setLabelIcon(ImageIcon icon)
	{
		BufferedImage bi = new BufferedImage(70, 70, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = (Graphics2D) bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(icon.getImage(), 0, 0, 70, 70, null);
	    g2d.dispose();
		wishLabel.setIcon(new ImageIcon(bi));
	}

	void displayLabel(ONCChild c, int wn)
	{
		this.setTitle(String.format("Wish %d Ornament Label", wn+1));
		
		child = c;
		this.wn = wn;
		
		String[] line = getWishLabel(c, wn);
		
		String labelText;
		if(line[3] == null)
			labelText = String.format("<html><center><i>%s</i><br><b>%s</b><br>%s</center></html>",
				line[0], line[1], line[2]);
		else
			labelText = String.format("<html><center><i>%s</i><br><b>%s<br>%s<br></b>%s</center></html>",
					line[0], line[1], line[2], line[3]);
		
		wishLabel.setText(labelText);
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
		String logEntry = String.format("WishLabelViewer.getWishLabel: Got DB references");
		LogDialog.add(logEntry, "M");
		
		ONCFamily fam = familyDB.getFamily(c.getFamID());
		ONCChildWish cw = childWishDB.getWish(c.getID(), wn);
		
		String[] line = new String[4];
		SimpleDateFormat sYear = new SimpleDateFormat("yyyy");
		
		String[] indicator = {"", "*", "#"};
		
		line[0] = c.getChildAge() + " " + c.getChildGender();
		
		//Combine the wish base and wish detail and return one or two lines depending
		//on the length of the combined string. If two lines are required, break the
		//string on a word boundary. Limit the second string to a max number of
		//characters based on MAX_LABEL_LINE_LENGTH
		ONCWish catWish = cat.getWishByID(cw.getWishID());
		String wishName = catWish == null ? "None" : catWish.getName();
		StringBuilder l1 = new StringBuilder(wishName);
		
		if(!cw.getChildWishDetail().isEmpty())	//Wish detail may need two lines
		{

			String wish = indicator[cw.getChildWishIndicator()] +
					cat.getWishByID(cw.getWishID()).getName() + " - " + cw.getChildWishDetail();
			//does it fit on one line?
			if(wish.length() <= MAX_LABEL_LINE_LENGTH)
			{
				line[1] = wish.trim();
			}
			else	//split into two lines
			{
				int index = MAX_LABEL_LINE_LENGTH;
				while(index > 0 && wish.charAt(index) != ' ')	//find the line break
					index--;
			
				line[1] = wish.substring(0, index);
				line[2] = wish.substring(index);
				if(line[2].length() > MAX_LABEL_LINE_LENGTH)
					line[2] = wish.substring(index, index + MAX_LABEL_LINE_LENGTH);		
			}
		
			//If the wish required two lines make the ONC Year line 4
			//else make the ONC Year line 3
			if(line[2] != null)
			{
//				line[2] = l2.toString();
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

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(tse.getType().equals("FAMILY_SELECTED"))
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			ArrayList<ONCChild> childList = childDB.getChildren(fam.getID());
			if(childList != null && childList.size() > 0 && childList.get(0).getChildWishID(0) != -1)
			{
				displayLabel(childList.get(0), 0);
			}	
		}
		else if(tse.getType().equals("CHILD_SELECTED"))
		{
//			System.out.println(String.format("FamilyPanel.entitySelected: Type = %s", tse.getType()));
			ONCChild child = (ONCChild) tse.getObject2();
			
			if(child.getChildWishID(0) != -1)
			{
				displayLabel(child, 0);
			}	
		}
		else if(tse.getType().equals("WISH_SELECTED"))
		{
//			System.out.println(String.format("FamilyPanel.entitySelected: Type = %s", tse.getType()));
			ONCChild child = (ONCChild) tse.getObject2();
			ONCChildWish cw = (ONCChildWish) tse.getObject3();
			
			if(cw != null)
				displayLabel(child, cw.getWishNumber());
		}
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
			if(child != null && addedWish.getChildID() == child.getID())	
				displayLabel(child, addedWish.getWishNumber());
			
		}
		else if(dbe.getSource() != this && dbe.getType().equals("UPDATED_CHILD_WISH"))
		{
			//Get the updated wish to extract the ONCChildWish. For updates, the ONCChildWish
			//id will remain the same
			ONCChildWish updatedWish = (ONCChildWish) dbe.getObject();
			
			//If the current child is being displayed has a wish added update the 
			//wish label to show the added wish
			if(child != null && updatedWish.getChildID() == child.getID())	
				displayLabel(child, updatedWish.getWishNumber());
		}
		else if(dbe.getType().equals("UPDATED_CHILD"))
		{
			ONCChild updatedChild = (ONCChild) dbe.getObject();
			if(child != null && child.getID() == updatedChild.getID())
			{
				//the age or gender of the child may have changed, update the label
				displayLabel(updatedChild, wn);
			}
		}
	}
}
