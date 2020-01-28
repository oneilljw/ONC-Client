package ourneighborschild;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BatteryDialog extends GiftLabelDialog
{
	/**
	 * Records batteries for gifts by scanning label, then allowing user to scan battery size and
	 * quantity for two battery types.
	 */
	private static final long serialVersionUID = 1L;
	
	//database references
	private BatteryDB batteryDB;
	private ScanState scanState;	//holds current state of battery scanning process
	
	private List<Battery> submittalList;	//holds battery or batteries submitted for undo
	
	private String size1, size2;
	private Integer qty1, qty2;
	
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
		
		childGiftDB = ChildGiftDB.getInstance();
		if(childGiftDB != null)
			childGiftDB.addDatabaseListener(this);

		size1 = BatterySize.NONE.toString();
		qty1 = 0;
		size2 =  BatterySize.NONE.toString();
		qty1 = 0;
		
		submittalList = new ArrayList<Battery>();
		
		//set up battery info panel
		JPanel batteryPanel = new JPanel();
		batteryPanel.setLayout(new BoxLayout(batteryPanel, BoxLayout.X_AXIS));
		
		JPanel batteryQtyPanel1 = new JPanel();
		batteryQtyPanel1.setBorder(BorderFactory.createTitledBorder("1st Battery Info"));
		batteryPanel1 = new JPanel();
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
		
		//set up unique tool tips
		btnUndo.setToolTipText("Click to undo last battery ");
		
		//set up the dialog pane
		this.getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.getContentPane().add(topPanel);
		this.getContentPane().add(batteryPanel);
		this.getContentPane().add(bottomPanel);
		
		this.setMinimumSize(new Dimension(580,280));
//		pack();
		
		setScanState(ScanState.INITIAL);
	}
	
	void onClearOtherPanels()
	{
		qtyCB2.removeActionListener(this);
		qtyCB1.removeActionListener(this);
		batterySizeCB2.removeActionListener(this);
		batterySizeCB1.removeActionListener(this); 
		
		batterySizeCB1.setSelectedIndex(0);
		size1 = BatterySize.NONE.toString();
		
		batterySizeCB2.setSelectedIndex(0);
		size2 = BatterySize.NONE.toString();
		
		qtyCB1.setSelectedIndex(0);
		qty1 = 0;
		
		qtyCB2.setSelectedIndex(0);
		qty2 = 0;
		
		if(scanState != ScanState.INITIAL)
			setScanState(ScanState.INITIAL);
		else
			barcodeTF.requestFocus();
		
		batterySizeCB1.addActionListener(this); 
		batterySizeCB2.addActionListener(this);
		qtyCB1.addActionListener(this);
		qtyCB2.addActionListener(this);
		
		setScanState(ScanState.INITIAL);
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
				
				lblResult.setText("Gift Located, Choose 1st Battery Size");
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
				
				lblResult.setText("Choose 1st Battery Qty");
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
				
				lblResult.setText("Choose 2st Battery Size (if required) or Submit");
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
				
				lblResult.setText("Choose 2nd Battery Qty");
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
				
				lblResult.setText("Entry Complete - Submit when ready!");
			}
		}
	}
	
	void onSubmit()
	{
		if(scanState.compareTo(ScanState.QTY1) > 0)
		{	
			submittalList.clear();
			List<Battery> batteryReqList = new ArrayList<Battery>();
		
			batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildGift().getGiftNumber(), size1, qty1));
			if(scanState == ScanState.READY_TO_SUBMIT)	//there are two batteries for this gift
				batteryReqList.add(new Battery(-1, swo.getChild().getID(), swo.getChildGift().getGiftNumber(), size2, qty2));
		
			Battery addedBattery;
			for(Battery addBatteryReq : batteryReqList)
			{
				addedBattery = (Battery)batteryDB.add(this, addBatteryReq);
				if(addedBattery != null)
					submittalList.add(addedBattery);
			}
		
			if(submittalList.isEmpty())
				alert(Result.FAILURE, String.format("Battery Add Failed, Family # %s", swo.getFamily().getONCNum()));
			else if(submittalList.size() == 1)
				alert(Result.SUCCESS, String.format(" 1 Battery Size Added Successfully, Family # %s", swo.getFamily().getONCNum()));
			else
				alert(Result.SUCCESS, String.format("2 Batteries Size Added Successfully, Family # %s", swo.getFamily().getONCNum()));
			
			btnUndo.setEnabled(!submittalList.isEmpty());
			onClearOtherPanels();
			setScanState(ScanState.INITIAL);
		}
	}
	
	void onUndoSubmittal()
	{
		if(!submittalList.isEmpty())
		{	
			//To undo the battery add(s), delete the submittal
			List<Battery> deletedBatteryList = new ArrayList<Battery>();
		
			for(Battery delBatteryReq : submittalList)
				deletedBatteryList.add(batteryDB.delete(this, delBatteryReq));
		
			if(deletedBatteryList.size() > 1)
				alert(Result.UNDO, "2 Battery Sizes Deleted Successfully");
			else if(deletedBatteryList.size() == 1)
				alert(Result.UNDO, "1 Battery Size Deleted Successfully");
			else
				alert(Result.FAILURE, "Battery Delete Failed");
		
			submittalList.clear();
			btnUndo.setEnabled(false);
			onClearOtherPanels();
			setScanState(ScanState.INITIAL);
		}
	}
	
	void onGiftLabelFound(SortGiftObject swo)
	{
		setBackgroundColor(pBkColor);
		setScanState(ScanState.SIZE1);
	}
	
	void onGiftLabelNotFound()
	{
		alert(Result.FAILURE, "Unable to Add Battiers, Gift Not Received Yet");
		
		errMessage += ", it hasn't been received yet. Please receive gift before adding batteries.";
		topPanel.revalidate();
		topPanel.repaint();
		setScanState(ScanState.INITIAL);
	}
	
	void onActionPerformed(ActionEvent e)
	{
		if(e.getSource() == batterySizeCB1 && scanState == ScanState.SIZE1)
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
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("LOADED_BATTERIES"))
		{
			//get the initial data and set title
			this.setTitle(String.format("Record Batteries for Gifts - %d Season",
							gvs.getCurrentSeason()));
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes()
	{
		return EnumSet.of(EntityType.GIFT);
	}
	
	@Override
	boolean isGiftEligible(ONCChildGift cw)
	{
		return cw.getGiftStatus() == GiftStatus.Received;
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
