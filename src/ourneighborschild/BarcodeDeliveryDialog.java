package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EnumSet;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

public class BarcodeDeliveryDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int SOUND_DURATION = 250;
	private static final int SUCCESS_SOUND_FREQ = 500;
	private static final int FAILED_SOUND_FREQ = 150;

	//database references
	private FamilyDB familyDB;
	private FamilyHistoryDB familyHistoryDB;
	
	private ONCFamily fam, lastFamChanged;
	private FamilyHistory familyHistory, lastFamilyHistoryChanged;
	private String[] deliveryCardData;
	
	private Image img;
	private String errMessage;
	private Color pBkColor; //Used to restore background for panels
	private Color[] successColor;
	private int successColorIndex;
	
	private JTextField barcodeTF;
	private JButton btnClear, btnUndo;
	private JLabel lblResult;
	private DeliveryCardPanel dcPanel;
	private JPanel topPanel, barcodePanel;
	
	private Color delCardBackgroundColor;
	
	public BarcodeDeliveryDialog(JFrame parentFrame)
	{
		super(parentFrame);
		
		if(dbMgr != null)
			dbMgr.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		
		familyHistoryDB = FamilyHistoryDB.getInstance();
		
		img = gvs.getSeasonIcon().getImage();
		errMessage = "Ready to Scan a Delivery Card";
		
		//initialize the colors
		successColor = new Color[2];
		successColor[0] = new Color(0, 225, 0);
		successColor[1] = new Color(0, 125, 0);
		successColorIndex = 0;
		
		//set up the gui
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.setPreferredSize(new Dimension(550, 100));
		
		pBkColor = topPanel.getBackground();
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodePanel = new JPanel();
		barcodeTF = new JTextField(12);
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Delivery Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		barcodePanel.add(barcodeTF);
		
		topPanel.add(lblONCIcon);
		topPanel.add(barcodePanel);
		
		//create the delivery card panel
		JPanel midPanel = new JPanel();
		dcPanel = new DeliveryCardPanel();
		delCardBackgroundColor = new Color(252, 236, 3);
		midPanel.add(dcPanel);
		
		//set up a bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		//set up the control panel with an undo button and label to the control panel.
		JPanel cntlPanel = new JPanel();
		
		btnUndo = new JButton(gvs.getImageIcon(16));
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
        cntlPanel.add(btnUndo);
        
		btnClear = new JButton("Clear");
		btnClear.setToolTipText("Click to clear fields");
	    btnClear.addActionListener(this);
	    cntlPanel.add(btnClear);
	    
	    lblResult = new JLabel();
        cntlPanel.add(lblResult);

	    bottomPanel.add(cntlPanel, BorderLayout.LINE_START);
	    
	    //set up the dialog pane
	  	this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
	  	this.getContentPane().add(topPanel);
	  	this.getContentPane().add(midPanel);
	  	this.getContentPane().add(bottomPanel);
	  		
	  	this.setMinimumSize(new Dimension(910, 656));
	  	
	  	this.addFocusListener(new BarcodeFocusListener());
	}
	
	void onUndoSubmittal()
	{
//    	String result = familyDB.update(this, lastFamChanged);
    	FamilyHistory result = familyHistoryDB.add(this, lastFamilyHistoryChanged);
    		
		if(result != null)
			alert(Result.UNDO, String.format("Delivery Undone: Family # %s",lastFamChanged.getONCNum()));
		else
			alert(Result.FAILURE, String.format("Undo Failed: Family # %s", lastFamChanged.getONCNum()));
		
		lastFamChanged = null;
		lastFamilyHistoryChanged = null;
		btnUndo.setEnabled(false);
		clearBarcodeTF();
	}
	
	void alert(Result result, String message)
	{
		if(result == Result.SUCCESS)
		{
			setBackgroundColor(successColor[successColorIndex]);
			sound(SUCCESS_SOUND_FREQ, SOUND_DURATION);
			successColorIndex = (successColorIndex + 1) % 2;
			lblResult.setText(message);
		}
		else if(result == Result.UNDO)
		{
			setBackgroundColor(Color.YELLOW);
			sound(SUCCESS_SOUND_FREQ, SOUND_DURATION);
			lblResult.setText(message);
		}
		else if(result == Result.FAILURE)
		{
			setBackgroundColor(Color.RED);
			sound(FAILED_SOUND_FREQ, SOUND_DURATION);
			lblResult.setText(message);
		}
	}
	
	void sound(int frequency, int duration)
	{
		try 
		{
			SoundUtils.tone(frequency, duration);
		} 
		catch (LineUnavailableException e1) 
		{
		
		}
	}
	
	void clearBarcodeTF()
	{
		barcodeTF.setText("");
		barcodeTF.requestFocusInWindow();
		barcodePanel.setBackground(Color.GREEN);
	}
	
	void clearTopPanel()
	{
		setBackgroundColor(pBkColor);
		
		errMessage = "Ready to Scan a Delivery Card";
		fam = null;
		familyHistory = null;
		dcPanel.setBackground(Color.WHITE);
		dcPanel.revalidate();
		dcPanel.repaint();
		
		clearBarcodeTF();
	}
	
	void setBackgroundColor(Color color)
	{
		topPanel.setBackground(color);
		barcodePanel.setBackground(color);
	}
	
	void showDialog(boolean tf)
	{
		this.setVisible(tf);
		this.requestFocus();
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes()
	{
		return EnumSet.of(EntityType.FAMILY);
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getType().contains("LOADED_DATABASE"))
		{
			this.setTitle(String.format("Our Neighbor's Child - %d Family Gift Delivery", gvs.getCurrentSeason()));
			img = gvs.getSeasonIcon().getImage();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == barcodeTF)
		{
			dcPanel.setBackground(Color.WHITE);
			if(gvs.getBarcodeCode().length() != barcodeTF.getText().length())
			{
				fam = null;
				alert(Result.FAILURE,String.format("Barcode %s is not a valid length", barcodeTF.getText()));
				errMessage = "Invalid Barcode, Ready to Scan Another Delivery Card";
			}
			else
			{
				//if using UPC-E, eliminate check digits before converting to ONC Number
				String barcode = barcodeTF.getText().replaceFirst("^0+(?!$)", "");
				String oncNum = gvs.getBarcodeCode() == Barcode.UPCE  ?  barcode.substring(0, barcode.length()-1) : barcode;
				
				if((fam = familyDB.searchForFamilyByONCNum(oncNum)) == null)
				{
					alert(Result.FAILURE, "Unable to Find Family " + oncNum + " in database");
					errMessage = "Delivery Card Not Found, Ready to Scan Another Delivery Card";
				}
				else
				{
					dcPanel.setBackground(delCardBackgroundColor);
					btnUndo.setEnabled(false);
					
					if(lastFamChanged != null && fam.getID() == lastFamChanged.getID() && lastFamilyHistoryChanged.getGiftStatus()==FamilyGiftStatus.Assigned)	
						alert(Result.SUCCESS, String.format("Delivered: Family# %s Gift Delivery Confirmed On Last Scan!", fam.getONCNum()));
					else if(familyHistory.getGiftStatus()==FamilyGiftStatus.Delivered)
						alert(Result.SUCCESS, String.format("Delivered: Family# %s Gift Delivery Previously Confirmed", fam.getONCNum()));	
					else if(familyHistory.getGiftStatus() != FamilyGiftStatus.Assigned)
    					alert(Result.FAILURE, String.format("Unable to Confirm Delivery: Improper Gift Status For Family# %s",
    							fam.getONCNum(),familyHistory.getGiftStatus().toString()));
					else if(familyHistory.getGiftStatus() == FamilyGiftStatus.Assigned)
					{
						//found the family, status is poised to be changed to delivered, so do so
						ONCFamily priorLastFamChanged = lastFamChanged;
						FamilyHistory priorLastFamilyHistoryChanged = lastFamilyHistoryChanged;
						
						lastFamChanged = new ONCFamily(fam);	//make a copy in case we have an undo action
						lastFamilyHistoryChanged = new FamilyHistory(familyHistory);
						
						//create a copy to send to the server and set the gift status to Delivered
						FamilyHistory addFamilyHistoryReq = new FamilyHistory(familyHistory);
						addFamilyHistoryReq.setFamilyGiftStatus(FamilyGiftStatus.Delivered);

						//send the request to the server via the local family data base
						FamilyHistory result = familyHistoryDB.add(this,  addFamilyHistoryReq);
						if(result != null)
						{	
							alert(Result.SUCCESS, String.format("Confirmed: Family# %s Gifts Delivered!", fam.getONCNum()));
							btnUndo.setEnabled(true);
						}
						else
						{
							alert(Result.FAILURE, String.format("Server Error: Family# %s Gift Delivery Confirmation Failed", fam.getONCNum()));
							lastFamChanged = priorLastFamChanged;
							lastFamilyHistoryChanged = priorLastFamilyHistoryChanged;
						}
					}
					
					deliveryCardData = familyDB.getYellowCardData(fam);
				}	
			}
			
			dcPanel.revalidate();
			dcPanel.repaint();			
			clearBarcodeTF();
		}
		else if(e.getSource() == btnClear)
		{
			lblResult.setText("");
			clearTopPanel();
		}
		else if(e.getSource() == btnUndo)
		{
			fam = lastFamChanged;
			familyHistory = lastFamilyHistoryChanged;
			dcPanel.revalidate();
			dcPanel.repaint();
			onUndoSubmittal();
		}
	}
	
	private enum Result{ SUCCESS, UNDO, FAILURE; }
	
	private class DeliveryCardPanel extends JPanel
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int X_MARGIN = 20;
		private static final int Y_MARGIN = 20;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		private static final int LABEL_ERR_MESSAGE_FONT_SIZE = 13;
		
		private static final int AVERY_LABEL_X_BARCODE_OFFSET = 0;
		private static final int AVERY_LABEL_Y_BARCODE_OFFSET = 4;
		
		private Font[] delCardFont;
		
		public DeliveryCardPanel()
		{
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			this.setBackground(Color.white);
			
			delCardFont = new Font[5];
		    delCardFont[0] = new Font("SansSerif", Font.BOLD, 18);	//Driver Font
		    delCardFont[1] = new Font("SansSerif", Font.BOLD, 10);	//Fixed Text Font
		    delCardFont[2] = new Font("SansSerif", Font.BOLD, 24);	//ONC Number Text Font
		    delCardFont[3] = new Font("SansSerif", Font.PLAIN, 10);	//Variable Text Font
		    delCardFont[4] = new Font("SansSerif", Font.BOLD, 10);	//Bottom Line Font
		    
		    this.setPreferredSize(new Dimension(890,540));
		}

		/**
		 * paintComponent paints the delivery card on the panel using
		 * 
		 */
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.scale(X_DISPLAY_SCALE_FACTOR, Y_DISPLAY_SCALE_FACTOR);
			
		    //If the family object is valid, use it to draw the label in the panel.
			//Otherwise draw an error message
			if(fam != null)
				drawDeliveryCard(10, 10, deliveryCardData, Integer.toString(gvs.getCurrentSeason()), g2d);
			else
			{
				g2d.setFont(new Font("SansSerif", Font.ITALIC, LABEL_ERR_MESSAGE_FONT_SIZE));
				FontMetrics metrics = g2d.getFontMetrics();
				int x=X_MARGIN, y=Y_MARGIN;
				
				int maxLines = (this.getHeight() - y)/metrics.getHeight();
				double maxLineLength = (this.getWidth()-(X_MARGIN*2))/X_DISPLAY_SCALE_FACTOR;
				int lineLengthLeft = (int) maxLineLength;
				
				//break the error message into words
				String[] errMessageWords = errMessage.split(" ");
				
				int wordIndex = 0;
				int line = 0;
				String wordToPrint;
				
				while(line < maxLines && wordIndex < errMessageWords.length)
				{
					//add a white space after the each word, unless its the last word in the string
					if(wordIndex < errMessageWords.length-1)
						wordToPrint = errMessageWords[wordIndex] + " ";
					else
						wordToPrint = errMessageWords[wordIndex];
						
					//will word w/ whitespace adjustment fit on line
					if(metrics.stringWidth(wordToPrint) < lineLengthLeft)
					{
						//it fits on line, draw it and update x position, lineLenghtLeft
						g.drawString(wordToPrint, x, y);
						x += metrics.stringWidth(wordToPrint);
						lineLengthLeft -= metrics.stringWidth(wordToPrint);
						wordIndex++;
					}
					else
					{
						//it doesn't fit on line, reset to the next line
						line++;
						lineLengthLeft = (int) maxLineLength;
						x= X_MARGIN;
						y = Y_MARGIN * line + Y_MARGIN;
					}
				}
			}
		}
		
		void drawDeliveryCard(int x, int y, String[] line, String season, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    //Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x+484, y+76, x+484+destX1, y+76+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
	         
		    //Draw the delivery card fixed text, all bold text
		    g2d.setFont(delCardFont[0]);
		    
		    if(line[16].isEmpty())
		    {	
		    	g2d.drawString("Driver #: _____", x+182, y+27);
		    	g2d.drawString("Name: ________________", x+324, y+27);
		    }
		    else
		    {
		    	g2d.drawString("Driver #: " + line[16], x+182, y+27);
		    	g2d.drawString("Name: " + line[17], x+324, y+27);
		    }
		    
		    g2d.setFont(delCardFont[1]);
		    g2d.drawString("ONC "+season, x+430, y+106);
		    g2d.drawString("Elem School:", x+0, y+124);
		    g2d.drawString("Region:", x+238, y+124);
		    g2d.drawString("Primary Phone:", x+0, y+150);
		    g2d.drawString("Alternate:",  x+238, y+150);
		    g2d.drawString("Language:", x+0, y+186);
		    g2d.drawString("Special Delivery Comments:", x+0, y+220);
		    	    
		    //Draw delivery card string information
		    g2d.setFont(delCardFont[2]);
			g2d.drawString(line[0], x, y+27); 	//ONC Number
			
			//draw ONC number bar code
			drawBarCode( String.format("%07d", Integer.parseInt(line[0])), x+484, y+290, g2d);	//bottom
			
			g2d.setFont(delCardFont[3]);
			g2d.drawString(line[1], x, y+68);		//First and Last Name
			g2d.drawString(line[2], x, y+80);		//Street Address
			g2d.drawString(line[3], x, y+92);		//City, State, Zip
			g2d.drawString(line[15], x+72, y+124); 	//Elementary School    
			g2d.drawString(line[4], x+290, y+124); 	//Region	    
			g2d.drawString(line[5], x+88, y+150);	//Home Phone 1
			if(!line[6].isEmpty())
				g2d.drawString("or " + line[6], x+144, y+150);	//Home Phone 2
		    g2d.drawString(line[7], x+300, y+150);	//Other Phone 1
		    if(!line[8].isEmpty())
		    	g2d.drawString("or " + line[8], x+378, y+150);	//Other Phone 2
			g2d.drawString(line[9], x+66, y+186);	//Language
		    g2d.drawString(line[10], x+158, y+220); //ONC Delivery Instructions	    
		    
		    //Draw the last line
		    g2d.setFont(delCardFont[4]);
		    g2d.drawString("TOY BAGS", x+92, y+266);
		    g2d.drawString("BIKE(S)", x+216, y+266);
		    g2d.drawString("OTHER LARGE ITEMS", x+326, y+266);
		    
		    //Draw the # of Bikes. If the family status == PACKAGED, draw the # of bags and large items
		    g2d.setFont(delCardFont[2]);
		    g2d.drawString(line[12], x+226, y+296);	//Draw # of bikes assigned to families children
		    
//		    if(line[14].equals(Integer.toString(FAMILY_STATUS_PACKAGED)))
//		    {
//		    	g2d.drawString(line[11], x+88, y+296);	//Draw # of bags used to package family
//		    	g2d.drawString(line[13], x+394, y+296);	//Draw # of large items assigned to family
//		    }
		}
		
		private void drawBarCode(String code, int x, int y, Graphics2D g2d)
		{
			//create the bar code
			AbstractBarcodeBean bean = gvs.getBarcodeCode() == Barcode.CODE128 ? new Code128Bean() : new UPCEBean();
			
			//get a temporary graphics context
			Graphics2D tempg2d = (Graphics2D) g2d.create();
			
			//create the canvass
			Java2DCanvasProvider cc = new Java2DCanvasProvider(tempg2d, 0);
			tempg2d.translate(x + AVERY_LABEL_X_BARCODE_OFFSET, y + AVERY_LABEL_Y_BARCODE_OFFSET);
			
			tempg2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			tempg2d.scale(2.4, 2.4);	//scale from millimeters to points

			//set the bean content
			bean.generateBarcode(cc, code);
			
			//release the graphics context
			tempg2d.dispose();
		}
	}
	
	private class BarcodeFocusListener implements FocusListener
	{

		@Override
		public void focusGained(FocusEvent e)
		{
			barcodeTF.requestFocusInWindow();
		}

		@Override
		public void focusLost(FocusEvent e)
		{

		}
	}
}
