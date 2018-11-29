package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BatteryDialog extends ONCEntityTableDialog implements ActionListener, DatabaseListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildWishDB childWishDB;
	
	private SortWishObject swo; //holds current family, child and gift info for gift getting batteries assigned
	private ScanState scanState;	//holds current state of battery scanning process
	
	private BatterySize size1, size2;
	private Integer qty1, qty2;
	
	private final Image img;
	private String errMessage;
	private Color pBkColor; //Used to restore background for panels
	
	private JTextField barcodeTF;
	private JButton btnSubmit, btnClear, btnPrint;
	private WishLabelPanel wishLabelPanel;
	private JPanel barcodePanel;
	private JPanel batteryPanel1, qtyPanel1, batteryPanel2, qtyPanel2;
	private JComboBox<String> batterySizeCB1, batterySizeCB2;
	private JComboBox<Integer> qtyCB1, qtyCB2;
	
	BatteryDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.parentFrame = parentFrame;
		this.setTitle("Assign Batteries for Gifts - 2018");
		
		familyDB = FamilyDB.getInstance();
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childWishDB = ChildWishDB.getInstance();
		if(childWishDB != null)
			childWishDB.addDatabaseListener(this);

		img = GlobalVariablesDB.getSeasonIcon().getImage();
		errMessage = "Please scan barcode";
		swo = null;
		
		size1 = BatterySize.NONE;
		qty1 = 0;
		size2 =  BatterySize.NONE;
		qty1 = 0;
		
		//set up the gui
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
//		JPanel topPanel = new JPanel(new BorderLayout());
		
		barcodePanel = new JPanel();
		barcodePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		barcodePanel.setPreferredSize(new Dimension(540, 100));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodeTF = new JTextField(9);
		barcodeTF.setMaximumSize(new Dimension(136,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Gift Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		
//		wishLabelPanel = null;
		wishLabelPanel = new WishLabelPanel();

		barcodePanel.add(lblONCIcon);
		barcodePanel.add(barcodeTF);
		barcodePanel.add(wishLabelPanel);
		
//		topPanel.add(barcodePanel, BorderLayout.WEST);
		topPanel.add(barcodePanel);
		
		Integer[] quantities = {0,1,2,3,4,5,6};
		
		//set up battery info panel
		JPanel batteryPanel = new JPanel();
		batteryPanel.setLayout(new BoxLayout(batteryPanel, BoxLayout.X_AXIS));
		
		JPanel batteryQtyPanel1 = new JPanel();
		batteryQtyPanel1.setBorder(BorderFactory.createTitledBorder("1st Battery Info"));
		batteryPanel1 = new JPanel();
		pBkColor = batteryPanel1.getBackground();
		qtyPanel1 = new JPanel();
		
		batterySizeCB1= new JComboBox<String>(BatterySize.textValues());
		batterySizeCB1.setBorder(BorderFactory.createTitledBorder("Size"));
		batterySizeCB1.setEditable(true);
		batterySizeCB1.addActionListener(this);
		
		qtyCB1= new JComboBox<Integer>(quantities);
		qtyCB1.setBorder(BorderFactory.createTitledBorder("Quantity"));
		qtyCB1.setEditable(true);
		qtyCB1.addActionListener(this);

		batteryPanel1.add(batterySizeCB1);
		qtyPanel1.add(qtyCB1);
		batteryQtyPanel1.add(batteryPanel1);
		batteryQtyPanel1.add(qtyPanel1);
		
		//set up battery info panel
		JPanel batteryQtyPanel2 = new JPanel();
		batteryQtyPanel2.setBorder(BorderFactory.createTitledBorder("2nd Battery Info (as required)"));
		batteryPanel2 = new JPanel();
		qtyPanel2 = new JPanel();
		
		batterySizeCB2= new JComboBox<String>(BatterySize.textValues());
		batterySizeCB2.setBorder(BorderFactory.createTitledBorder("Size"));
		batterySizeCB2.setEditable(true);
		batterySizeCB2.addActionListener(this);
		
		qtyCB2= new JComboBox<Integer>(quantities);
		qtyCB2.setBorder(BorderFactory.createTitledBorder("Quantity"));
		qtyCB2.setEditable(true);
		qtyCB2.addActionListener(this);

		batteryPanel2.add(batterySizeCB2);
		qtyPanel2.add(qtyCB2);
		batteryQtyPanel2.add(batteryPanel2);
		batteryQtyPanel2.add(qtyPanel2);
		
		batteryPanel.add(batteryQtyPanel1);
		batteryPanel.add(batteryQtyPanel2);
		
		//set up the control panel
		JPanel cntlPanel = new JPanel();
		
		btnPrint = new JButton("Print");
	    btnPrint.addActionListener(this);
	    cntlPanel.add(btnPrint);
		
		btnClear = new JButton("Clear");
	    btnClear.addActionListener(this);
	    cntlPanel.add(btnClear);
		
		btnSubmit = new JButton("Submit");
	    btnSubmit.addActionListener(this);
	    btnSubmit.setEnabled(true);
	    cntlPanel.add(btnSubmit);
		
		//set up the dialog pane
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(topPanel);
		this.getContentPane().add(batteryPanel);
		this.getContentPane().add(cntlPanel);
		
		this.setMinimumSize(new Dimension(550,280));
//		pack();
		
		setScanState(ScanState.INITIAL);
	}
	
	void onClear()
	{
		qtyCB2.removeActionListener(this);
		qtyCB1.removeActionListener(this);
		batterySizeCB2.removeActionListener(this);
		batterySizeCB1.removeActionListener(this); 
		barcodeTF.removeActionListener(this);
		
		barcodeTF.setText("");
		
		batterySizeCB1.setSelectedIndex(0);
		size1 = BatterySize.NONE;
		
		batterySizeCB2.setSelectedIndex(0);
		size2 = BatterySize.NONE;
		
		qtyCB1.setSelectedIndex(0);
		qty1 = 0;
		
		qtyCB2.setSelectedIndex(0);
		qty2 = 0;
		
		errMessage = "Please scan a gift";
		swo = null;
		barcodePanel.revalidate();
		barcodePanel.repaint();
		
		barcodeTF.addActionListener(this);
		batterySizeCB1.addActionListener(this); 
		batterySizeCB2.addActionListener(this);
		qtyCB1.addActionListener(this);
		qtyCB2.addActionListener(this);
	}
	
	void setScanState(ScanState newState)
	{
		if(scanState != newState)
		{
			scanState = newState;
			if(scanState == ScanState.INITIAL)
			{
				batteryPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				 barcodeTF.requestFocus();
			}
			else if(scanState == ScanState.SIZE1)
			{
				batteryPanel1.setBackground(Color.GREEN);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				batterySizeCB1.setEnabled(true);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				batterySizeCB1.requestFocus();
			}
			else if(scanState == ScanState.QTY1)
			{
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(Color.GREEN);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(true);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				qtyCB1.requestFocus();
			}
			else if(scanState == ScanState.SIZE2)
			{
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(Color.GREEN);
				qtyPanel2.setBackground(pBkColor);
				
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(true);
				qtyCB2.setEnabled(false);
				btnSubmit.setEnabled(true);
				
				batterySizeCB2.requestFocus();
			}
			else if(scanState == ScanState.QTY2)
			{
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(Color.GREEN);
				
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(true);
				
				btnSubmit.setEnabled(false);
				
				qtyCB2.requestFocus();
			}
		}
	}
	
	void onPrintBatterySheet()
	{
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(new BatteryBarcodeSheetPrinter());
         
		boolean ok = pj.printDialog();
		if (ok)
		{
			try { pj.print(); }
			catch (PrinterException ex) { /* The job did not successfully complete */ }       
		}
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
			else
				errMessage = "Invalid Barcode";
				
			//get Wish History for bar code wish id. If found, notify entity listeners of
			//the Wish entity selection.
			if(cID > -1 && wn > -1)
			{
				ONCChildWish cw = childWishDB.getWish(cID, wn);
				if(cw != null 
//					&& cw.getChildWishStatus() == WishStatus.Received
				  )
				{
					ONCChild child = childDB.getChild(cID);
					if(child != null)
					{
						ONCFamily family = familyDB.getFamily(child.getFamID());
						if(family != null)
						{
							swo = new SortWishObject(0, family, child, cw);
							barcodePanel.revalidate();
							barcodePanel.repaint();
							setScanState(ScanState.SIZE1);
						}
					}
				}
				else
				{
					errMessage = "Gift is not received";
					barcodePanel.revalidate();
					barcodePanel.repaint();
					setScanState(ScanState.INITIAL);
				}
			}	
		}
		else if(e.getSource() == batterySizeCB1 && scanState == ScanState.SIZE1)
		{
			batterySizeCB1.removeActionListener(this);
			String s = (String) batterySizeCB1.getSelectedItem();
			BatterySize newSize = null;
			
			if(isNumeric(s) && (s.length() == 8 || s.length() == 7))
			{		
				newSize = BatterySize.find(s.substring(0, s.length()-1));
				if(newSize != null)
				{
					size1 = newSize;
					setScanState(ScanState.QTY1);
					batterySizeCB1.setSelectedItem(newSize.toString());
				}
				else
				{
					batterySizeCB1.setSelectedIndex(0);
				}
			}
			else if(s.equals(BatterySize.NONE.toString()))
			{
				batterySizeCB1.setSelectedIndex(0);
			}
				
			//if the size is the same, do nothing
			batterySizeCB1.addActionListener(this);
		}
		else if(e.getSource() == qtyCB1)
		{
			Integer newQty = (Integer) qtyCB1.getSelectedItem();
			if(qty1 != newQty && newQty != 0)
			{
				qty1 = newQty;
				setScanState(ScanState.SIZE2);
			}
		}
		else if(e.getSource() == batterySizeCB2)
		{
			batterySizeCB2.removeActionListener(this);
			
			//if the size is the same, do nothing
			String code = (String) batterySizeCB2.getSelectedItem();
			BatterySize newSize = BatterySize.find(code);
			if(newSize != null && size2 != newSize)
			{
				size2 = newSize;
				setScanState(ScanState.QTY1);
				batterySizeCB2.setSelectedItem(newSize.toString());
			}
			else
				batterySizeCB2.setSelectedIndex(0);
			
			batterySizeCB2.addActionListener(this);
		}
		else if(e.getSource() == qtyCB2)
		{
			Integer newQty = (Integer) qtyCB2.getSelectedItem();
			if(qty2 != newQty && newQty != 0)
			{
				qty2 = newQty;
				setScanState(ScanState.READY_TO_SUBMIT);
			}
		}
		else if(e.getSource() == btnPrint)
		{
			onPrintBatterySheet();
		}
		else if(e.getSource() == btnClear)
		{
			onClear();
			setScanState(ScanState.INITIAL);
		}
		else if(e.getSource() == btnSubmit)
		{
			onClear();
			setScanState(ScanState.INITIAL);
		}
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes()
	{
		return EnumSet.of(EntityType.WISH);
	}
	
	private class WishLabelPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final double X_DISPLAY_SCALE_FACTOR = 1.6;
		private static final double Y_DISPLAY_SCALE_FACTOR = 1.4;
		
		private Font[] lFont;
		private AveryWishLabelPrinter awlp;
		
		public WishLabelPanel()
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
				awlp.drawLabel(10, 10, swo.getWishLabel(), lFont, img, g2d);
			else
				 g.drawString(errMessage, 20, 20);
		}
	}
	
	private enum ScanState
	{
		INITIAL,
		SIZE1,
		QTY1,
		SIZE2,
		QTY2,
		READY_TO_SUBMIT;
	}
}
