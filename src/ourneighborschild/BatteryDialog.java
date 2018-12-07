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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
	 * Records batteries for gifts by scanning label, then allowing user to scan battery size and
	 * quantity for two battery types.
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	private BatteryDB batteryDB;
	private FamilyDB familyDB;
	private ChildDB childDB;
	private ChildWishDB childWishDB;
	
	private SortWishObject swo; //holds current family, child and gift info for gift getting batteries assigned
	private ScanState scanState;	//holds current state of battery scanning process
	
	private List<Battery> submittalList;	//holds battery or batteries submitted for undo
	
	private String size1, size2;
	private Integer qty1, qty2;
	
	private final Image img;
	private List<String> errMessage;
	private Color pBkColor; //Used to restore background for panels
	
	private JTextField barcodeTF;
	private JButton btnSubmit, btnClear, btnUndo;
	private JLabel lblResult;
	private WishLabelPanel wishLabelPanel;
	private JPanel topPanel, barcodePanel;
	private JPanel batteryPanel1, qtyPanel1, batteryPanel2, qtyPanel2;
	private JComboBox<String> batterySizeCB1, batterySizeCB2, qtyCB1, qtyCB2;
	
	BatteryDialog(JFrame parentFrame)
	{
		super(parentFrame);
		this.parentFrame = parentFrame;
		this.setTitle("Record Batteries for Gifts");
		
		batteryDB = BatteryDB.getInstance();
		if(batteryDB != null)
			batteryDB.addDatabaseListener(this);
		
		familyDB = FamilyDB.getInstance();
		childDB = ChildDB.getInstance();
		if(childDB != null)
			childDB.addDatabaseListener(this);
		
		childWishDB = ChildWishDB.getInstance();
		if(childWishDB != null)
			childWishDB.addDatabaseListener(this);

		img = GlobalVariablesDB.getSeasonIcon().getImage();
		errMessage = new ArrayList<String>();
		errMessage.add("Ready to Scan a Gift Label");
		swo = null;
		
		size1 = BatterySize.NONE.toString();
		qty1 = 0;
		size2 =  BatterySize.NONE.toString();
		qty1 = 0;
		
		submittalList = new ArrayList<Battery>();
		
		//set up the gui
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.setPreferredSize(new Dimension(550, 100));
		
		JLabel lblONCIcon = new JLabel(gvs.getImageIcon(0));
		
		barcodePanel = new JPanel();
		barcodeTF = new JTextField(9);
		barcodeTF.setMaximumSize(new Dimension(136,56));
		barcodeTF.setBorder(BorderFactory.createTitledBorder("Gift Barcode"));
		barcodeTF.setToolTipText("Scan barcode or type barcode # and press <enter>");
		barcodeTF.addActionListener(this);
		barcodePanel.add(barcodeTF);
		
		wishLabelPanel = new WishLabelPanel();

		topPanel.add(lblONCIcon);
		topPanel.add(barcodePanel);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(wishLabelPanel);
		
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
		
		qtyCB1= new JComboBox<String>(BatteryQty.textValues());
		qtyCB1.setPreferredSize(new Dimension(88, 56));
		qtyCB1.setBorder(BorderFactory.createTitledBorder("Quantity"));
		qtyCB1.setEditable(true);
		qtyCB1.addActionListener(this);

		batteryPanel1.add(batterySizeCB1);
		qtyPanel1.add(qtyCB1);
		batteryQtyPanel1.add(batteryPanel1);
		batteryQtyPanel1.add(qtyPanel1);
		
		//set up battery info panel
		JPanel batteryQtyPanel2 = new JPanel();
		batteryQtyPanel2.setBorder(BorderFactory.createTitledBorder("2nd Battery Info (if required)"));
		batteryPanel2 = new JPanel();
		qtyPanel2 = new JPanel();
		
		batterySizeCB2= new JComboBox<String>(BatterySize.textValues());
		batterySizeCB2.setBorder(BorderFactory.createTitledBorder("Size"));
		batterySizeCB2.setEditable(true);
		batterySizeCB2.addActionListener(this);
		
		qtyCB2= new JComboBox<String>(BatteryQty.textValues());
		qtyCB2.setPreferredSize(new Dimension(88, 56));
		qtyCB2.setBorder(BorderFactory.createTitledBorder("Quantity"));
		qtyCB2.setEditable(true);
		qtyCB2.addActionListener(this);

		batteryPanel2.add(batterySizeCB2);
		qtyPanel2.add(qtyCB2);
		batteryQtyPanel2.add(batteryPanel2);
		batteryQtyPanel2.add(qtyPanel2);
		
		batteryPanel.add(batteryQtyPanel1);
		batteryPanel.add(batteryQtyPanel2);
		
		//set up a bottom panel
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		//set up the control panel with an undo button and label to the control panel.
		JPanel cntlPanel = new JPanel();
		
		btnUndo = new JButton(gvs.getImageIcon(16));
        btnUndo.setToolTipText("Click to undo last battery ");
        btnUndo.setEnabled(false);
        btnUndo.addActionListener(this);
        cntlPanel.add(btnUndo);
        
		btnClear = new JButton("Clear");
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
		
		//set up the dialog pane
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(topPanel);
		this.getContentPane().add(batteryPanel);
		this.getContentPane().add(bottomPanel);
		
		this.setMinimumSize(new Dimension(550,280));
//		pack();
		
		setScanState(ScanState.INITIAL);
	}
	
	void clear()
	{
		qtyCB2.removeActionListener(this);
		qtyCB1.removeActionListener(this);
		batterySizeCB2.removeActionListener(this);
		batterySizeCB1.removeActionListener(this); 
		barcodeTF.removeActionListener(this);
		
		barcodeTF.setText("");
		
		batterySizeCB1.setSelectedIndex(0);
		size1 = BatterySize.NONE.toString();
		
		batterySizeCB2.setSelectedIndex(0);
		size2 = BatterySize.NONE.toString();
		
		qtyCB1.setSelectedIndex(0);
		qty1 = 0;
		
		qtyCB2.setSelectedIndex(0);
		qty2 = 0;
		
		errMessage.clear();
		errMessage.add("Ready to Scan a Gift Label");
		swo = null;
		topPanel.revalidate();
		topPanel.repaint();
		
		if(scanState != ScanState.INITIAL)
			setScanState(ScanState.INITIAL);
		else
			barcodeTF.requestFocus();
		
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
				barcodePanel.setBackground(Color.GREEN);
				batteryPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				barcodeTF.setEnabled(true);
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				barcodeTF.requestFocus();
			}
			else if(scanState == ScanState.SIZE1)
			{
				barcodePanel.setBackground(pBkColor);
				batteryPanel1.setBackground(Color.GREEN);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				barcodeTF.setEnabled(false);
				batterySizeCB1.setEnabled(true);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				batterySizeCB1.requestFocus();
				
				lblResult.setText("");
			}
			else if(scanState == ScanState.QTY1)
			{
				barcodePanel.setBackground(pBkColor);
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(Color.GREEN);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				barcodeTF.setEnabled(false);
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(true);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(false);
				
				qtyCB1.requestFocus();
				
				lblResult.setText("");
			}
			else if(scanState == ScanState.SIZE2)
			{
				barcodePanel.setBackground(pBkColor);
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(Color.GREEN);
				qtyPanel2.setBackground(pBkColor);
				
				barcodeTF.setEnabled(false);
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(true);
				qtyCB2.setEnabled(false);
				btnSubmit.setEnabled(true);
				
				batterySizeCB2.requestFocus();
				
				lblResult.setText("");
			}
			else if(scanState == ScanState.QTY2)
			{
				barcodePanel.setBackground(pBkColor);
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(Color.GREEN);
				
				barcodeTF.setEnabled(false);
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(true);
				
				btnSubmit.setEnabled(false);
				
				qtyCB2.requestFocus();
				
				lblResult.setText("");
			}
			else if(scanState == ScanState.READY_TO_SUBMIT)
			{
				barcodePanel.setBackground(pBkColor);
				batteryPanel1.setBackground(pBkColor);
				qtyPanel1.setBackground(pBkColor);
				batteryPanel2.setBackground(pBkColor);
				qtyPanel2.setBackground(pBkColor);
				
				barcodeTF.setEnabled(false);
				batterySizeCB1.setEnabled(false);
				qtyCB1.setEnabled(false);
				batterySizeCB2.setEnabled(false);
				qtyCB2.setEnabled(false);
				
				btnSubmit.setEnabled(true);
				btnSubmit.requestFocus();
				
				lblResult.setText("");
			}
		}
	}
	
	void onSubmit()
	{
		submittalList.clear();
		List<Battery> batteryReqList = new ArrayList<Battery>();
		
		batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildWish().getWishNumber(), size1, qty1));
		if(scanState == ScanState.READY_TO_SUBMIT)	//there are two batteries for this gift
			batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildWish().getWishNumber(), size2, qty2));
		
		Battery addedBattery;
		for(Battery addBatteryReq : batteryReqList)
		{
			addedBattery = (Battery)batteryDB.add(this, addBatteryReq);
			if(addedBattery != null)
				submittalList.add(addedBattery);
		}
		
		if(submittalList.isEmpty())
			lblResult.setText(String.format("Battery Add Failed, Family # %s", swo.getFamily().getONCNum()));
		else if(submittalList.size() == 1)
			lblResult.setText(String.format(" 1 Battery Type Added Successfully, Family # %s", swo.getFamily().getONCNum()));
		else
			lblResult.setText(String.format("2 Batteries Types Added Successfully, Family # %s", swo.getFamily().getONCNum()));
		
		btnUndo.setEnabled(!submittalList.isEmpty());
		clear();
		setScanState(ScanState.INITIAL);
	}
	void onUndoSubmittal()
	{
		//To undo the battery add(s), delete the submittal
		List<Battery> deletedBatteryList = new ArrayList<Battery>();
		
		for(Battery delBatteryReq : submittalList)
			deletedBatteryList.add(batteryDB.delete(this, delBatteryReq));
		
		if(deletedBatteryList.size() > 1)
			lblResult.setText("2 Batteries Types Deleted Successfully");
		else if(deletedBatteryList.size() == 1)
			lblResult.setText("1 Battery Types Deleted Successfully");
		else
			lblResult.setText("Battery Delete Failed");
		
		submittalList.clear();
		btnUndo.setEnabled(false);
		clear();
		setScanState(ScanState.INITIAL);
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
			{
				errMessage.clear();
				errMessage.add("Invalid Barcode");
				topPanel.revalidate();
				topPanel.repaint();
			}
				
			//find the gift by child ID and wish number. Only gifts that have already been 
			//received can have batteries recorded.
			if(cID > -1 && wn > -1)
			{
				ONCChildWish cw = childWishDB.getWish(cID, wn);
				if(cw != null && cw.getChildWishStatus() == WishStatus.Received)
				{
					ONCChild child = childDB.getChild(cID);
					if(child != null)
					{
						ONCFamily family = familyDB.getFamily(child.getFamID());
						if(family != null)
						{
							swo = new SortWishObject(0, family, child, cw);
							topPanel.revalidate();
							topPanel.repaint();
							setScanState(ScanState.SIZE1);
						}
					}
				}
				else
				{
					errMessage.clear();
					errMessage.add("Gift is not yet received, ");
					errMessage.add("please receive gift before ");
					errMessage.add("battery scanning");
					topPanel.revalidate();
					topPanel.repaint();
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
					size1 = newSize.toString();
					setScanState(ScanState.QTY1);
					batterySizeCB1.setSelectedItem(newSize.toString());
				}
				else
				{
					size1 = BatterySize.NONE.toString();
					batterySizeCB1.setSelectedIndex(0);
				}
			}
			else if(s.equals(BatterySize.NONE.toString()))
			{
				size1 = BatterySize.NONE.toString();
			}
			else
			{
				size1 = (String) batterySizeCB1.getSelectedItem();
				setScanState(ScanState.QTY1);
			}
				
			//if the size is the same, do nothing
			batterySizeCB1.addActionListener(this);
		}
		else if(e.getSource() == qtyCB1 && scanState == ScanState.QTY1)
		{
			qtyCB1.removeActionListener(this);
			String s = (String) qtyCB1.getSelectedItem();
			BatteryQty newQty = null;
			
			if(isNumeric(s) && (s.length() == 8 || s.length() == 7))
			{		
				newQty = BatteryQty.find(s.substring(0, s.length()-1));
				if(newQty != null)
				{
					qty1 = newQty.value();
					setScanState(ScanState.SIZE2);
					qtyCB1.setSelectedItem(newQty.toString());
				}
				else
				{
					qty1 = 0;
					qtyCB1.setSelectedIndex(0);	
				}
			}
			else if(isNumeric(s))
			{
				qty1 = Integer.parseInt((String) qtyCB1.getSelectedItem());
				setScanState(ScanState.SIZE2);
			}
			else
			{
				qtyCB1.setSelectedIndex(0);
				qty1 = 0;
				setScanState(ScanState.QTY1);
			}
				
			//if the quantity is the same, do nothing
			qtyCB1.addActionListener(this);
		}
		else if(e.getSource() == batterySizeCB2 && scanState == ScanState.SIZE2)
		{
			batterySizeCB2.removeActionListener(this);
			String s = (String) batterySizeCB2.getSelectedItem();
			BatterySize newSize = null;
			
			if(isNumeric(s) && (s.length() == 8 || s.length() == 7))
			{		
				newSize = BatterySize.find(s.substring(0, s.length()-1));
				if(newSize != null)
				{
					size2 = newSize.toString();
					setScanState(ScanState.QTY2);
					batterySizeCB2.setSelectedItem(newSize.toString());
				}
				else
				{
					size2 = BatterySize.NONE.toString();
					batterySizeCB2.setSelectedIndex(0);	
				}
			}
			else if(s.equals(BatterySize.NONE.toString()))
			{
				size2 = BatterySize.NONE.toString();
				setScanState(ScanState.SIZE2);
			}
			else
			{
				size2 = (String) batterySizeCB2.getSelectedItem();
				setScanState(ScanState.QTY2);
			}
				
			//if the size is the same, do nothing
			batterySizeCB2.addActionListener(this);
		}
		else if(e.getSource() == qtyCB2 && scanState == ScanState.QTY2)
		{
			qtyCB2.removeActionListener(this);
			String s = (String) qtyCB2.getSelectedItem();
			BatteryQty newQty = null;
			
			if(isNumeric(s) && (s.length() == 8 || s.length() == 7))
			{		
				newQty = BatteryQty.find(s.substring(0, s.length()-1));
				if(newQty != null)
				{
					qty2 = newQty.value();
					setScanState(ScanState.READY_TO_SUBMIT);
					qtyCB2.setSelectedItem(newQty.toString());
				}
				else
				{
					qty2 = 0;
					qtyCB2.setSelectedIndex(0);	
				}
			}
			else if(isNumeric(s))
			{
				qty2 = Integer.parseInt((String) qtyCB2.getSelectedItem());
				setScanState(ScanState.READY_TO_SUBMIT);
			}
			else
			{
				qtyCB2.setSelectedIndex(0);
				qty2 = 0;
				setScanState(ScanState.QTY2);
			}
				
			//if the quantity is the same, do nothing
			qtyCB2.addActionListener(this);
		}
		else if(e.getSource() == btnClear)
		{
			clear();
			setScanState(ScanState.INITIAL);
		}
		else if(e.getSource() == btnUndo && !submittalList.isEmpty())
		{
			onUndoSubmittal();
		}
		else if(e.getSource() == btnSubmit && scanState.compareTo(ScanState.QTY1) > 0)
		{
			onSubmit();
/*			
			List<Battery> batteryReqList = new ArrayList<Battery>();
			
			batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildWish().getWishNumber(), size1, qty1));
			if(scanState == ScanState.READY_TO_SUBMIT)	//there are two batteries for this gift
				batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildWish().getWishNumber(), size2, qty2));
			
			Battery addedBattery;
			int count = 0;
			for(Battery addBatteryReq : batteryReqList)
			{
				addedBattery = (Battery)batteryDB.add(this, addBatteryReq);
				if(addedBattery != null)
					count++;
			}
			
			if(count > 1)
				errMessage = "Batteries Successfully Added";
			else if(count == 1)
				errMessage = "Battery Successfully Added";
			else
				errMessage = "Failed to Add";
					
			clear();
			setScanState(ScanState.INITIAL);
*/			
		}
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("LOADED_BATTERIES"))
		{
			//get the initial data and set title
			this.setTitle(String.format("Record Batteries for Gifts - %d", GlobalVariablesDB.getCurrentSeason()));
		}
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
			{
				g2d.setFont(new Font("Calibri", Font.ITALIC, 16));
				int x=8, y=16;
				for(int i=0; i<errMessage.size(); i++)
				{
					g.drawString(errMessage.get(i), x, y);
					y += 14;
				}
			}
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
