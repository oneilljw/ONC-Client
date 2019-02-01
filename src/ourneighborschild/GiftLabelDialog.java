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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class GiftLabelDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener
{
	/**
	 * Abstract class provides the foundation for dialogs that allow the user to scan a gift label bar code, 
	 * locate the gift in the wish/gift database and then process the gift. A label viewer displays the label
	 * in the top bar of the dialog. 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int SOUND_DURATION = 100;
	private static final int SUCCESS_SOUND_FREQ = 500;
	private static final int FAILED_SOUND_FREQ = 150;
	
	//database references
	protected FamilyDB familyDB;
	protected ChildDB childDB;
	protected ChildGiftDB childGiftDB;
	
	protected SortGiftObject swo; //holds current family, child and gift info for gift getting batteries assigned
	protected SortGiftObject lastWishChanged;	//Holds the info for last gift label displayed
	
	private final Image img;
	protected String errMessage;
	protected Color pBkColor; //Used to restore background for panels
	private Color[] successColor;
	private int successColorIndex;
	
	protected JTextField barcodeTF;
	protected JButton btnSubmit, btnClear, btnUndo;
	protected JLabel lblResult;
	private GiftLabelPanel giftLabelPanel;
	protected JPanel topPanel,bottomPanel, barcodePanel;
	
	GiftLabelDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.parentFrame = parentFrame;
		
		familyDB = FamilyDB.getInstance();
		
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childGiftDB = ChildGiftDB.getInstance();
		if(childGiftDB != null)
			childGiftDB.addDatabaseListener(this);

		img = GlobalVariablesDB.getSeasonIcon().getImage();
		errMessage = "Ready to Scan a Gift Label";
		swo = null;
		
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
		barcodeTF = new JTextField(9);
		barcodeTF.setMaximumSize(new Dimension(136,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Gift Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		barcodePanel.add(barcodeTF);
		
		giftLabelPanel = new GiftLabelPanel();

		topPanel.add(lblONCIcon);
		topPanel.add(barcodePanel);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(giftLabelPanel);
		
		//set up a bottom panel
		bottomPanel = new JPanel(new BorderLayout());
		
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
		
	    JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		btnSubmit = new JButton("Submit");
	    btnSubmit.addActionListener(this);
	    btnSubmit.setEnabled(true);
	    submitPanel.add(btnSubmit);
	    
	    bottomPanel.add(cntlPanel, BorderLayout.LINE_START);
	    bottomPanel.add(submitPanel, BorderLayout.LINE_END);
	}
	
	abstract void onClearOtherPanels();
	abstract void onSubmit();
	abstract void onUndoSubmittal();
	abstract boolean isGiftEligible(ONCChildGift cw);
	abstract void onGiftLabelFound(SortGiftObject swo);
	abstract void onGiftLabelNotFound();
	abstract void onActionPerformed(ActionEvent e);
	
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
		barcodeTF.requestFocus();
		barcodePanel.setBackground(Color.GREEN);
	}
	
	void clearTopPanel()
	{
		setBackgroundColor(pBkColor);
		
		errMessage ="Ready to Scan a Gift Label";
		swo = null;
		topPanel.revalidate();
		topPanel.repaint();
		
		clearBarcodeTF();
	}
	
	void setBackgroundColor(Color color)
	{
		topPanel.setBackground(color);
		barcodePanel.setBackground(color);
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes()
	{
		return EnumSet.of(EntityType.GIFT);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == barcodeTF)
		{
			//if using UPC-E, eliminate check digits before converting to childwishID integer
			int cID = -1, wn = -1;
			String s = barcodeTF.getText();
			if(gvs.getBarcodeCode() == Barcode.UPCE && s.length() == 8)
			{
				cID = Integer.parseInt(s.substring(0, s.length()-2));
				wn = Integer.parseInt(s.substring(s.length()-2, s.length()-1));	
			}
			else if(gvs.getBarcodeCode() == Barcode.CODE128 && s.length() == 7)
			{
				cID = Integer.parseInt(s.substring(0, s.length()-1));
				wn = Integer.parseInt(s.substring(s.length()-1, s.length()-0));
			}
				
			//find the gift by child ID and wish number. Only gifts that have already been 
			//received can have batteries recorded.
			ONCChildGift cw = childGiftDB.getWish(cID, wn);
			ONCChild child = null;
			ONCFamily family = null;
			
			//clear the previous label and error message
			swo = null; 
			errMessage = "";
			
			if(cID == -1 || wn == -1)
			{
				errMessage="Unable To Scan Barcode";
				onGiftLabelNotFound();
			}	
			else if(cw == null)
			{
				errMessage="Unable To Find Gift";
				onGiftLabelNotFound();
			}
			else if(!isGiftEligible(cw))
			{
				errMessage="Gift Status Ineligible";
				onGiftLabelNotFound();
			}
			else if((child = childDB.getChild(cID)) == null)
			{
				errMessage="Unable To Find Child";
				onGiftLabelNotFound();
			}
			else if((family = familyDB.getFamily(child.getFamID())) == null)
			{
				errMessage="Unable To Find Family";
				onGiftLabelNotFound();
			}
			else
			{
				//child wish was found, is in an eligible state
				swo = new SortGiftObject(0, family, child, cw);
				onGiftLabelFound(swo);	
			}
			
			topPanel.revalidate();
			topPanel.repaint();
			
			clearBarcodeTF();
		}
		else if(e.getSource() == btnClear)
		{
			lblResult.setText("");
			clearTopPanel();
			onClearOtherPanels();
		}
		else if(e.getSource() == btnUndo)
		{
			swo = lastWishChanged;
			topPanel.revalidate();
			topPanel.repaint();
			onUndoSubmittal();
		}
		else if(e.getSource() == btnSubmit)
		{
			onSubmit();
		}
		else
			onActionPerformed(e);
	}
	
	protected enum Result{ SUCCESS, UNDO, FAILURE; }
	
    private class GiftLabelPanel extends JPanel
    {
        	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int X_MARGIN = 6;
		private static final int Y_MARGIN = 14;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		private static final int LABEL_ERR_MESSAGE_FONT_SIZE = 13;
		
		private Font[] lFont;
		private AveryWishLabelPrinter awlp;
		
		public GiftLabelPanel()
		{
			awlp = new AveryWishLabelPrinter();
			
			this.setBackground(Color.white);
			
			lFont = new Font[3];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 11);
		    lFont[1] = new Font("Calibri", Font.BOLD, 11);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 10);
		    
		    this.setPreferredSize(new Dimension(300, 90));
		}
		
		/**
		 * paintComponent paints the label on the panel using
		 * the AveryWishLabelPrinter.
		 */
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.scale(X_DISPLAY_SCALE_FACTOR, Y_DISPLAY_SCALE_FACTOR);
		    
		    //If the SortWishObject is valid, use it to draw the label in the panel.
			//Otherwise draw an error message
			if(swo != null)
				awlp.drawLabel(10, 10, swo.getGiftLabel(), lFont, img, g2d);
			else
			{
				g2d.setFont(new Font("Calibri", Font.ITALIC, LABEL_ERR_MESSAGE_FONT_SIZE));
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
	}
}
