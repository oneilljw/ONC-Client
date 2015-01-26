package OurNeighborsChild;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVWriter;

/* These imports were removed when Jasper Reports was no longer used for Yellow Card print
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
*/

public class SortFamilyDialog extends SortTableDialog implements PropertyChangeListener								
{
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_NO_CHANGE_LIST_ITEM = "No Change";
	private static final int CHANGE_DELIVERY_STATUS_ASSIGNED = 4;
	private static final int ZIP_OUTOFAREA = 7;
	private static final int FAMILY_STATUS_PACKAGED = 5;
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;	
	private static final int YELLOW_CARDS_PER_PAGE = 2;	
	private static final int PACKAGING_SHEET_CHILDREN_PER_PAGE = 5;
	private static final int PACKAGING_SHEET = 0;
	private static final int PRE_PACKAGING_SHEET = 1;
	private static final int RECEIVING_SHEET = 2;
//	private static final int VERIFICATION_SHEET_MAX_CHILD_RECORDS_PER_PAGE = 5;
	private static final int VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT = 24;
//	private static final int VERIFICATION_SHEET_X_OFFSET = 16;	
	private static final int MAX_DIRECTION_STEPS_ON_FIRST_PAGE = 16;
	private static final int MAX_DIRECTION_STEPS_ON_NEXT_PAGES = 30;
	private static final int MIN_EMAIL_ADDRESS_LENGTH = 2;
	private static final int MIN_EMAIL_NAME_LENGTH = 2;
	private static final String FAMILY_EMAIL_SENDER_ADDRESS = "clientinformation@ourneighborschild.org";
	
	public enum FamilyStatus {Empty, InfoVerified, GiftsSelected, GiftsReveived, GiftsVerified, Packaged}

	//Unique gui elements for Sort Family Dialog
	private JComboBox oncCB, batchCB, regionCB, dnsCB, zipCB, fstatusCB, streetCB, lastnameCB;
	private JComboBox changedByCB, stoplightCB, dstatusCB;
	private ComboItem[] changeDelItem, changeFamItem;
	private JComboBox changeDNSCB;
	private JComboBox changeFStatusCB, changeDStatusCB;
	private DefaultComboBoxModel regionCBM, changedByCBM, changeDNSCBM;
	private JComboBox printCB, emailCB, callCB;
	
	private JProgressBar progressBar;
	private ONCEmailer oncEmailer;

//	private boolean bIgnoreSortDialogEvents = false;
	
	private int sortBatchNum = 0, sortFStatus = 0, sortDStatus=0;
	private int sortZip = 0, sortRegion = 0, sortChangedBy = 0, sortStoplight = 0;
	private String sortONC = "Any", sortLN = "Any", sortStreet= "Any", sortDNSCode;

	private static String[] dnsCodes = {"None", "Any", "DUP", "NC", "NISA", "OPT-OUT", "SA", "WA"};	
	private static String[] batchNums = {"Any","B-01","B-02","B-03","B-04","B-05","B-06","B-07","B-08","B-09","B-10", "B-CR"};	
	private static String[] printChoices = {"Print", "Print Listing", "Print Book Labels", 
											"Print Family Receiving Sheets",
											"Print Gift Inventory Sheets", "Print Packaging Sheets",
											"Print Delivery Cards", "Print Delivery Directions"};
	
	private static String[] columnToolTips = {"ONC Family Number", "Batch Number", "Do Not Serve Code", 
											  "Family Status", "Delivery Status", "Head of Household First Name", 
											  "Head of Household Last Name", "House Number","Street",
											  "Unit or Apartment Number", "Zip Code", "Region",
											  "Changed By", "Stoplight Color"};
	
	private static String[] columns = {"ONC", "Batch #", "DNS", "Fam Status", "Del Status", "First", "Last",
										"House", "Street", "Unit", "Zip", "Reg", "Changed By", "SL"};
	
	private static int[] colWidths = {32, 48, 48, 72, 72, 72, 72, 48, 128, 72, 48, 32, 72, 24};
	
	private static int [] center_cols = {1, 11, 13};
	
	SortFamilyDialog(JFrame pf)
	{
		super(pf, columnToolTips, columns, colWidths, center_cols);
		this.setTitle("Our Neighbor's Child - Family Management");
		
		ONCRegions regions = ONCRegions.getInstance();
		if(regions != null)
			regions.addDatabaseListener(this);
		
		UserDB userDB = UserDB.getInstance();
		if(userDB != null)
			userDB.addDatabaseListener(this);
		
		//Initialize the sort table array list
		stAL = new ArrayList<ONCFamily>();

		//set up search comparison variables
		sortDNSCode = dnsCodes[0];
		
		//Set up unique serach criteria gui
    	String[] oncStrings = {"Any", "NNA", "OOR", "RNV", "DEL"};
    	oncCB = new JComboBox(oncStrings);
    	oncCB.setEditable(true);
    	oncCB.setPreferredSize(new Dimension(88,56));
		oncCB.setBorder(BorderFactory.createTitledBorder("ONC #"));
		oncCB.addActionListener(this);
    	
		batchCB = new JComboBox(batchNums);
		batchCB.setBorder(BorderFactory.createTitledBorder("Batch #"));
		batchCB.addActionListener(this);
		
		fstatusCB = new JComboBox(famstatus);
		fstatusCB.setBorder(BorderFactory.createTitledBorder("Family Status"));
		fstatusCB.addActionListener(this);
		
		dstatusCB = new JComboBox(delstatus);
		dstatusCB.setBorder(BorderFactory.createTitledBorder("Delivery Status"));
		dstatusCB.addActionListener(this);
		
//		String[] regs = new String[regions.getNumberOfRegions()+1];
//		regs[0] = "Any";
		regionCBM = new DefaultComboBoxModel();
    	regionCBM.addElement("Any");
//		for(int i=0; i< regions.getNumberOfRegions(); i++)
//			regs[i+1] = regions.getRegionID(i);
		regionCB = new JComboBox();
		regionCB.setModel(regionCBM);
		regionCB.setBorder(BorderFactory.createTitledBorder("Region"));
		regionCB.addActionListener(this);
		
		dnsCB = new JComboBox(dnsCodes);
		dnsCB.setEditable(true);
		dnsCB.setPreferredSize(new Dimension(160, 56));
		dnsCB.setBorder(BorderFactory.createTitledBorder("DNS Code"));
		dnsCB.addActionListener(this);
		
		String[] any = {"Any", "UNDETERMINED"};
		lastnameCB = new JComboBox(any);
		lastnameCB.setEditable(true);
		lastnameCB.setPreferredSize(new Dimension(152, 56));
		lastnameCB.setBorder(BorderFactory.createTitledBorder("Last Name"));
		lastnameCB.addActionListener(this);
				
		streetCB = new JComboBox(any);
		streetCB.setEditable(true);
		streetCB.setPreferredSize(new Dimension(160, 56));
		streetCB.setBorder(BorderFactory.createTitledBorder("Street"));
		streetCB.addActionListener(this);
		
		String[] onczipCodes = {"Any", "20120", "20121", "20124", "20151", "22033", "22039", "Out Of Area"};
		zipCB = new JComboBox(onczipCodes);
		zipCB.setBorder(BorderFactory.createTitledBorder("Zip Code"));
		zipCB.addActionListener(this);
		
		changedByCB = new JComboBox();
		changedByCBM = new DefaultComboBoxModel();
	    changedByCBM.addElement("Anyone");
	    changedByCB.setModel(changedByCBM);
		changedByCB.setPreferredSize(new Dimension(144, 56));
		changedByCB.setBorder(BorderFactory.createTitledBorder("Changed By"));
		changedByCB.addActionListener(this);
		
		stoplightCB = new JComboBox(stoplt);
		stoplightCB.setPreferredSize(new Dimension(104, 56));
		stoplightCB.setBorder(BorderFactory.createTitledBorder("Stoplight"));
		stoplightCB.addActionListener(this);
		
		//Add all sort criteria components to search criteria panels
        sortCriteriaPanelTop.add(oncCB);
		sortCriteriaPanelTop.add(batchCB);				
		sortCriteriaPanelTop.add(dnsCB);
		sortCriteriaPanelTop.add(fstatusCB);
		sortCriteriaPanelTop.add(dstatusCB);
		sortCriteriaPanelBottom.add(lastnameCB);
		sortCriteriaPanelBottom.add(streetCB);
		sortCriteriaPanelBottom.add(zipCB);
		sortCriteriaPanelBottom.add(regionCB);
		sortCriteriaPanelBottom.add(changedByCB);
		sortCriteriaPanelBottom.add(stoplightCB);
		
		//Set the preferred size of the bottom panel since it was used here. The top panel preferred
		//size is set in the parent class
		sortCriteriaPanelBottom.setPreferredSize(new Dimension(sortTable.getWidth(), 64));
		
        //Set up change data panel gui
        changeDNSCB = new JComboBox();
		changeDNSCB.setEditable(true);
		changeDNSCBM = new DefaultComboBoxModel();
	    changeDNSCBM.addElement(DEFAULT_NO_CHANGE_LIST_ITEM);
	    for(int index=1; index < dnsCodes.length; index++)
	    	changeDNSCBM.addElement(dnsCodes[index]);
	    changeDNSCB.setModel(changeDNSCBM);
		changeDNSCB.setPreferredSize(new Dimension(172, 56));
		changeDNSCB.setBorder(BorderFactory.createTitledBorder("Change DNS Code"));
		changeDNSCB.addActionListener(this);
		
		changeFamItem = new ComboItem[7];	//Family status combo box list objects can be enabled/disabled
		changeFamItem[0] = new ComboItem(DEFAULT_NO_CHANGE_LIST_ITEM);
		changeFamItem[1] = new ComboItem("Unverified");
		changeFamItem[2] = new ComboItem("Info Verified");
		changeFamItem[3] = new ComboItem("Gifts Selected"); 
		changeFamItem[4] = new ComboItem("Gifts Received"); 
		changeFamItem[5] = new ComboItem("Gifts Verified");
		changeFamItem[6] = new ComboItem("Packaged", false);
				
        changeFStatusCB = new JComboBox(changeFamItem);
        changeFStatusCB.setRenderer(new ComboRenderer());
        changeFStatusCB.setPreferredSize(new Dimension(200, 56));
		changeFStatusCB.setBorder(BorderFactory.createTitledBorder("Change Family Status"));
		changeFStatusCB.addActionListener(new ComboListener(changeFStatusCB)); //Prevents selection of disabled combo box items
		changeFStatusCB.addActionListener(this); //Used to check for enabling the Apply Changes button
        
		changeDelItem = new ComboItem[9];	//Delivery status combo box list objects can be enabled/disabled
		changeDelItem[0] = new ComboItem(DEFAULT_NO_CHANGE_LIST_ITEM);
		changeDelItem[1] = new ComboItem("Empty");
		changeDelItem[2] = new ComboItem("Contacted");  
		changeDelItem[3] = new ComboItem("Confirmed");
		changeDelItem[4] = new ComboItem("Assigned", false);   
		changeDelItem[5] = new ComboItem("Attempted");
		changeDelItem[6] = new ComboItem("Returned");
		changeDelItem[7] = new ComboItem("Delivered");
		changeDelItem[8] = new ComboItem("Counselor Pick-Up");
		
        changeDStatusCB = new JComboBox(changeDelItem);
        changeDStatusCB.setRenderer(new ComboRenderer());
        changeDStatusCB.setPreferredSize(new Dimension(172, 56));
		changeDStatusCB.setBorder(BorderFactory.createTitledBorder("Change Delivery Status"));
		changeDStatusCB.addActionListener(new ComboListener(changeDStatusCB));	//Prevents selection of disabled combo box items
		changeDStatusCB.addActionListener(this);	//Used to check for enabling the Apply Changes button
				
		//Add the components to the change data panel			
		changeDataPanel.add(changeDNSCB);
		changeDataPanel.add(changeFStatusCB);
		changeDataPanel.add(changeDStatusCB);
		changeDataPanel.setPreferredSize(new Dimension(sortTable.getWidth()-300, 90));
		changeDataPanel.setBorder(BorderFactory.createTitledBorder("Change Select Family Data"));
         
		//set up the unique control gui for this dialog
        printCB = new JComboBox(printChoices);
        printCB.setPreferredSize(new Dimension(136, 28));
        printCB.setEnabled(false);
        printCB.addActionListener(this);
        
        String[] emailChoices = {"Email", "2014 Familiy Confirmation Email"};
        emailCB = new JComboBox(emailChoices);
        emailCB.setPreferredSize(new Dimension(136, 28));
        emailCB.setEnabled(false);
        emailCB.addActionListener(this);
        
      //Set up the email progress bar
      	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        String[] callChoices = {"Auto Call", "Call: Delivery Confirmation", "Export Call File"};
        callCB = new JComboBox(callChoices);
        callCB.setPreferredSize(new Dimension(136, 28));
        callCB.setEnabled(false);
        callCB.addActionListener(this);

        //Add the components to the control panel
        cntlPanel.add(callCB);
        cntlPanel.add(progressBar);
        cntlPanel.add(emailCB);
        cntlPanel.add(printCB);

        pack();
	}

	protected void disableControls()
	{
		printCB.setEnabled(false);
		emailCB.setEnabled(false);
		callCB.setEnabled(false);
	}
	
	/**********************************************************************************
	 * This method builds an array of strings for each row in the family table. It is
	 * called by the super class display table method. 
	 **********************************************************************************/
	protected String[] getTableRow(ONCFamily f)
	{
		String[] tablerow = {f.getONCNum(), 
			f.getBatchNum(),
			f.getDNSCode(),
			famstatus[f.getFamilyStatus()+1],
			delstatus[f.getDeliveryStatus()+1],
			f.getHOHFirstName(),
			f.getHOHLastName(),
			f.getHouseNum(),
			f.getStreet(),
			f.getUnitNum(),
			f.getZipCode(),
			regions.getRegionID(f.getRegion()),
			f.getChangedBy(),
			stoplt[f.getStoplightPos()+1].substring(0,1)};
		
		return tablerow;
	}
	
	/***********************************************************************************
	 * This method builds and displays the sort table. Each sort table row contains columns 
	 * that are class variables of an ONCFamily object for the user to see. An array list, stAL,
	 * holds a reference to each ONCObject displayed in the sort table by row. For example, 
	 * element 0 in the stAL contains a reference to the first family that has met the sort
	 * criteria selected by the user. A series of helper methods determines whether a family
	 * object meets a particular criteria. Each family object is compared against the sort 
	 * criteria and, if the family object meets the criteria, it is added to the array list
	 * are To build Based on sort criteria
	 * selected by the 
	 ************************************************************************************/
	public void buildTableList()
	{
		stAL.clear();	//Clear the prior table data array list
		
		for(ONCFamily f:fDB.getList())
		{
			if(doesONCNumMatch(f.getONCNum()) &&
				doesBatchNumMatch(f.getBatchNum()) &&
				 doesDNSCodeMatch(f.getDNSCode()) &&
				  doesFStatusMatch(f.getFamilyStatus()) &&
				   doesDStatusMatch(f.getDeliveryStatus()) &&
				    doesLastNameMatch(f.getHOHLastName()) &&
				     doesStreetMatch(f.getStreet()) &&
				      doesZipMatch(f.getZipCode()) &&
				       doesRegionMatch(f.getRegion()) &&
				        doesChangedByMatch(f.getChangedBy()) &&
				         doesStoplightMatch(f.getStoplightPos()))	//Family criteria pass
			{
				stAL.add(f);
			}
		}
		
		//update the family count. If the sort criteria is set such that only served
		//family's are displayed, change the panel border to so indicate
		lblNumOfTableItems.setText(Integer.toString(stAL.size()));
		if(sortONC.equals("Any") && sortBatchNum == 0 && sortDNSCode.equals(dnsCodes[0])  &&
			sortFStatus == 0 && sortDStatus == 0 && sortLN.equals("Any") && 
			sortStreet.equals("Any") && sortZip == 0 && sortRegion == 0 && sortChangedBy == 0 &&
			sortStoplight == 0)
		{
			itemCountPanel.setBorder(BorderFactory.createTitledBorder("Families Served Total"));
		}
		else
			itemCountPanel.setBorder(BorderFactory.createTitledBorder("Families Meeting Criteria"));
		
		displaySortTable();		//Display the table after table array list is built					
	}
	
	//Returns a boolean that a change to DNS, Family or Delivery Status occurred
	boolean onApplyChanges()
	{		
		int[] row_sel = sortTable.getSelectedRows();
		bChangingTable = true;
		boolean bDataChanged = false;

		for(int i=0; i<row_sel.length; i++)
		{
			ONCFamily f = stAL.get(row_sel[i]);
			boolean bFamilyChangeDetected = false;	
			
			//If a change to the DNS Code, process it
			if(!changeDNSCB.getSelectedItem().equals(DEFAULT_NO_CHANGE_LIST_ITEM) && 
					!f.getDNSCode().equals(changeDNSCB.getSelectedItem()))
			{
				String newDNSCode = (String) changeDNSCB.getSelectedItem();
				String chngdBy = oncGVs.getUserLNFI();
				
				f.setDNSCode(newDNSCode);
				f.setChangedBy(chngdBy);	//Set the changed by field to current user
				
				bFamilyChangeDetected = true;
			}
			
			//If a change to the Family Status, process it
			if(changeFStatusCB.getSelectedIndex() > 0 && 
					f.getFamilyStatus() != changeFStatusCB.getSelectedIndex()-1)
			{
				//If family status is changing from PACKAGED, number of family bags must be set to 0
				if(f.getFamilyStatus() == FAMILY_STATUS_PACKAGED)	//If changing away from PACKAGED, reset bags
					f.setNumOfBags(0);
				
				f.setFamilyStatus(changeFStatusCB.getSelectedIndex()-1);
				f.setChangedBy(oncGVs.getUserLNFI());	//Set the changed by field to current user

				bFamilyChangeDetected = true;
			}
			//If a change to Delivery Status, process it
			if(changeDStatusCB.getSelectedIndex() > 0 && 
					f.getDeliveryStatus() != changeDStatusCB.getSelectedIndex()-1)
			{
				//Add a new delivery to the delivery history with the assigned driver
				//and the status set to assigned.
				ONCDelivery reqDelivery = new ONCDelivery(-1, f.getID(),
						changeDStatusCB.getSelectedIndex()-1,
						deliveryDB.getDeliveredBy(f.getDeliveryID()),
						"Delivery Status Changed",
						oncGVs.getUserLNFI(),
						Calendar.getInstance());

				String response = deliveryDB.add(this, reqDelivery);
				if(response.startsWith("ADDED_DELIVERY"))
					bDataChanged = true;
				else
				{
					//display an error message that update request failed
					GlobalVariables gvs = GlobalVariables.getInstance();
					JOptionPane.showMessageDialog(this, "ONC Server denied Delivery Update," +
							"try again later","Delivery Update Failed",  
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}
			}
			
			if(bFamilyChangeDetected)	//submit change to local db. If successful, set table to rebuild
			{
				String response = fDB.update(this, f);
				if(response.startsWith("UPDATED_FAMILY"))
					bDataChanged = true;
				else
				{	//display an error message that update request failed
					GlobalVariables gvs = GlobalVariables.getInstance();
					JOptionPane.showMessageDialog(this, "ONC Server denied Family Update," +
						"try again later","Family Update Failed",  
						JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}
			}
		}
		
		if(bDataChanged);
			buildTableList();
			
		//Reset the change combo boxes to DEFAULT_NO_CHANGE_LIST_ITEM
		changeFStatusCB.setSelectedIndex(0);
		changeDNSCB.setSelectedIndex(0);
		changeDStatusCB.setSelectedIndex(0);
		
		//Changes were applied, disable until user selects new table row(s) and values
		btnApplyChanges.setEnabled(false);
		
		bChangingTable = false;
		
		return bDataChanged;
	}
	
	void updateUserList()
	{	
		UserDB userDB = UserDB.getInstance();
		
		bIngoreCBEvents = true;
		changedByCB.setEnabled(false);
		String curr_sel = changedByCB.getSelectedItem().toString();
		int selIndex = 0;
		
		changedByCBM.removeAllElements();
		
		changedByCBM.addElement("Anyone");
		
		int index = 0;
		for(ONCUser user:userDB.getUserList())
		{
			changedByCBM.addElement(user.getLNFI());
			index++;
			if(curr_sel.equals(user.getLNFI()))
				selIndex = index;
		}
		
		changedByCB.setSelectedIndex(selIndex); //Keep current selection in sort criteria
		sortChangedBy = selIndex;
		
		changedByCB.setEnabled(true);
		bIngoreCBEvents = false;
	}
	
	void updateRegionList(String[] regions)
	{
		regionCB.setEnabled(false);
		String currSel = regionCB.getSelectedItem().toString();
		
		regionCBM.removeAllElements();	//Clear the combo box selection list
		regionCBM.addElement("Any");
		
		for(String s: regions)	//Add new list elements
				regionCBM.addElement(s);
			
		//Reselect the prior region, if it still exists
		regionCB.setSelectedItem(currSel);
		
		regionCB.setEnabled(true);
	}
	
	void addUser(String user)
	{
		changedByCB.setEnabled(false);		
		changedByCBM.addElement(user);
		changedByCB.setEnabled(true);
	}
	
	void onPrintDeliveryCards()
	{
		if(sortTable.getSelectedRowCount() > 0)	//Only print selected rows
		{
			PrinterJob pj = PrinterJob.getPrinterJob();
			pj.setPrintable(new DeliveryCardPrinter());
         
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		else
		{
			JOptionPane.showMessageDialog(parentFrame, 
					"Please select family's to print delivery cards",  
					"No Family's Selected", JOptionPane.ERROR_MESSAGE, oncGVs.getImageIcon(0));
		}
		
		 printCB.setSelectedIndex(0);
	}
	
	/*******************************************************************************************
	 * This method is called in response to a user packaging sheet print request. The method 
	 * builds a page array list used by the Packaging Sheet Printer  object to print each 
	 * page of the delivery directions print document. Each item in the array represents a page
	 * in the document. 
	 ********************************************************************************************/
	void onPrintVerificationSheets(int verification_sheet_type)
	{
		if(sortTable.getSelectedRowCount() > 0)	//Only print selected rows
		{
			//build the array list to print. For each row selected by the user, get the family object 
			//reference and build the packaging sheet array list. The packaging sheet array list is
			//used by the print method to print each page. A family packaging sheet may have two pages
			//if the family has more than 5 children
			ArrayList<ONCVerificationSheet> psAL = new ArrayList<ONCVerificationSheet>();
			
			int[] row_sel = sortTable.getSelectedRows();
			for(int i=0; i<sortTable.getSelectedRowCount(); i++)
			{
				ONCFamily f = stAL.get(row_sel[i]);
				
				//Build the packaging sheet array list. If the family has more that five children
				//a second page is needed
				psAL.add(new ONCVerificationSheet(cDB.getChildren(f.getID()), Integer.toString(fDB.getNumberOfBikesSelectedForFamily(f)), f.getONCNum(), 0));
				if(cDB.getChildren(f.getID()).size() > PACKAGING_SHEET_CHILDREN_PER_PAGE)
					psAL.add(new ONCVerificationSheet(cDB.getChildren(f.getID()), Integer.toString(fDB.getNumberOfBikesSelectedForFamily(f)), f.getONCNum(), 1));			
			}
			
			//Create the info required for the print job
			SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(twodigitYear.format(oncGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = oncGVs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
			String oncSeason = "ONC " + Integer.toString(GlobalVariables.getCurrentSeason());			
			
			
			//Create the print job
			PrinterJob pj = PrinterJob.getPrinterJob();
			if(verification_sheet_type == PACKAGING_SHEET)
				pj.setPrintable(new PackagingSheetPrinter(psAL, img, oncSeason, cwDB));
			else if(verification_sheet_type == PRE_PACKAGING_SHEET)
				pj.setPrintable(new GiftInventorytSheetPrinter(psAL, img, oncSeason, cwDB));
			else if(verification_sheet_type == RECEIVING_SHEET)
				pj.setPrintable(new FamilyReceivingCheckSheetPrinter(psAL, img, oncSeason, cwDB));
			else
			{
				printCB.setSelectedIndex(0);	//Invalid request
				return;	
			}
         
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
		 printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	/*******************************************************************************************
	 * This method is called in response to a user delivery directions print request. The method 
	 * builds a page array list used by the Delivery Directions Printer  object to print each 
	 * page of the delivery directions print document. Each item in the array represents a page
	 * in the document. 
	 * @throws JSONException
	 ********************************************************************************************/
	void onPrintDeliveryDirections() throws JSONException
	{	
		//build the array list to print. For each row selected by the user, get the family object 
		//referenced and build the delivery directions page array list. This array list is
		//used by the print method to print each page. Family delivery directions may have two pages
		//if the directions contains more than MAX_DIRECTION_STEPS_ON_FIRST_PAGE steps
			
		DrivingDirections ddir = new DrivingDirections();	//Driving direction services object
		ArrayList<DDPrintPage> ddpAL = new ArrayList<DDPrintPage>();	//The print array list
		
		//For each family selected in the sort table, add a page to the delivery direction
		//document. Each page is an entry in the Delivery Direction Page array list, which
		//holds Delivery Direction Page objects. If not families were selected, then simply
		//return after reseting the user print command request
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i<sortTable.getSelectedRowCount(); i++)
		{
			ONCFamily f = stAL.get(row_sel[i]);
				
			//Get family address and format it for the URL request to Google Maps
//			String dbdestAddress = f.getHouseNum().trim() + "+" + f.getStreet().trim() + 
//										"+" + f.getCity().trim() + ",VA";		
//			String destAddress = dbdestAddress.replaceAll(" ", "+");
			
			String destAddress = f.getGoogleMapAddress();
				
			//Get direction JSON, then trip route and steps
			JSONObject dirJSONObject = ddir.getGoogleDirections(oncGVs.getWarehouseAddress(), destAddress);
			JSONObject leg = ddir.getTripRoute(dirJSONObject);
			JSONArray steps = ddir.getDrivingSteps(leg);
				
			//Determine the number of pages needed to print directions for family
			int totalpagesforfamily = 1;
			if(steps.length() > MAX_DIRECTION_STEPS_ON_FIRST_PAGE)
				totalpagesforfamily++;
				
			//Build the delivery directions page array list. If the delivery directions contain
			//more than MAX_DIRECTION_STEPS_ON_FIRST_PAGE steps, a second page is needed
			ArrayList<String[]> pagestepsAL = new ArrayList<String[]>();
			int stepindex, familypage;
				
			//Build page 0
			pagestepsAL.clear();
			stepindex = 0;
			familypage = 1;	//Used by the footer for page number, so index starts at 1
			while(stepindex < MAX_DIRECTION_STEPS_ON_FIRST_PAGE && stepindex < steps.length())
					pagestepsAL.add(getDirectionsTableRow(steps, stepindex++));
					
			ddpAL.add(new DDPrintPage(destAddress, leg, fDB.getYellowCardData(f),
								pagestepsAL, familypage, totalpagesforfamily));
				
			//Add other pages as necessary
			while(stepindex < MAX_DIRECTION_STEPS_ON_NEXT_PAGES && stepindex < steps.length())	
			{
				familypage++;
				pagestepsAL.clear();
				while(stepindex < MAX_DIRECTION_STEPS_ON_NEXT_PAGES && stepindex < steps.length())
					pagestepsAL.add(getDirectionsTableRow(steps, stepindex++));
						
				ddpAL.add(new DDPrintPage(destAddress, leg, fDB.getYellowCardData(f),
											pagestepsAL, familypage, totalpagesforfamily));
			}
		}
			
		//Create the print job. First, create the ONC season image and string. Then instantiate
		//a DeliveryDirectionsPrinter object. Then show the print dialog and execute printing
		SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
		int idx = Integer.parseInt(twodigitYear.format(oncGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
		final Image img = oncGVs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
		String oncSeason = "ONC " + Integer.toString(GlobalVariables.getCurrentSeason());
			
		DeliveryDirectionsPrinter ddp = new DeliveryDirectionsPrinter(ddpAL, img, oncSeason);
			
		PrinterJob pj = PrinterJob.getPrinterJob();
		pj.setPrintable(ddp);
    
		boolean ok = pj.printDialog();
		if (ok)
		{
			try
			{
				pj.print();
			}
			catch (PrinterException ex)
			{
				/* The job did not successfully complete */
			}       
		}
		
		printCB.setSelectedIndex(0);
	}
	
	void onPrintBookLabels()
	{
		if(sortTable.getSelectedRowCount() > 0)	 //Print selected rows. If no rows selected, do nothing
		{
			PrinterJob pj = PrinterJob.getPrinterJob();

			pj.setPrintable(new AveryBookLabelPrinter());
         
			boolean ok = pj.printDialog();
			if (ok)
			{
				try
				{
					pj.print();
				}
				catch (PrinterException ex)
				{
					/* The job did not successfully complete */
				}       
			}
		}
		
        printCB.setSelectedIndex(0);	//Reset the user print request
	}
	
	/********************************************************************************
	 * This method takes a JSONArray produced by the Google Driving Directions API and
	 * a step number and returns a 4 element string array that represents the step
	 * information. Element 1 holds the step number, element 2 holds the step direction
	 * text, element 3 holds the distance and element 4 holds the time duration for 
	 * the step. 
	 * @param steps
	 * @param stepnum
	 * @return String[4] of parsed driving direction steps in the JSONArray source
	 * @throws JSONException
	 ********************************************************************************/
	String[] getDirectionsTableRow(JSONArray steps, int stepnum) throws JSONException
	{
		String[] tablerow = new String[4];
		
		JSONObject step = steps.getJSONObject(stepnum);
		
    	tablerow[0] = (String) Integer.toString(stepnum+1)+".";
    	tablerow[1] = removeHTMLTags((String) step.getString("html_instructions"));
    	tablerow[2] = (String) step.getJSONObject("distance").getString("text");
    	tablerow[3] = (String) step.getJSONObject("duration").getString("text");
				
		return tablerow;
	}
	
	/********************************************************************************
	 * This method takes a source string and removes all characters between a '<' 
	 * and a '>' found in the string.  It is used in printing ONC delivery directions
	 * to remove the html tags in delivery direction steps contained the JSON received
	 * through the Google Driving Directions API. The method also corrects the format of
	 * the "Destination" string message found in the last step from the API.
	 * @param src
	 * @return string with html tag info removed
	 *********************************************************************************/
	String removeHTMLTags(String src)
	{
		StringBuffer out = new StringBuffer("");
		int index = 0;
		
		while(index < src.length())
		{
			if(src.charAt(index) == '<')
			{
				index++;
				while(index < src.length() && src.charAt(index++) != '>');
			}
			else
				out.append(src.charAt(index++));
		}
		
		return out.toString().replace("Destination will", ", destination will");
	}
	
	void onExportAngelInputFile()
	{
		String[][] abbreviations = {{"rd", "Road"},
									{"dr", "Drive"}, 
									{"ct", "Court"}, 
									{"ave", "Avenue"}, 
									{"cir", "Circle"},
									{"ln", "Lane"},
									{"pl", "Place"},
									{"st", "Street"},
									{"sq", "Square"},
									{"trl", "Trail"}};
		
		//Solicit user on which phone number to export for each family
		Object[] options= {"Cancel", "Home Phone", "Other Phone"};
		JOptionPane confirmOP = new JOptionPane("Which family phone # would you like to call?",
												JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
												oncGVs.getImageIcon(0), options, "Cancel");
		JDialog confirmDlg = confirmOP.createDialog(parentFrame, "Select Phone Number to Call");
		confirmDlg.setLocationRelativeTo(this);
		confirmDlg.setVisible(true);
	
		String phonereq = confirmOP.getValue().toString();
		if(phonereq != null && !phonereq.equals("Cancel"))
		{
			//Get the export file and rite the selected row data
    		String[] header = {"number", "Address", "ONCnumber"};
    		
    		ONCFileChooser oncfc = new ONCFileChooser(parentFrame);
    		File oncwritefile = oncfc.getFile("Select file for export of selected rows" ,
       											new FileNameExtensionFilter("CSV Files", "csv"), 1);
    		if(oncwritefile!= null)
    		{
    			//If user types a new filename without extension.csv, add it
    			String filePath = oncwritefile.getPath();
    			if(!filePath.toLowerCase().endsWith(".csv")) 
    				oncwritefile = new File(filePath + ".csv");
	    	
    			try 
    			{
    				CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
    				writer.writeNext(header);
		
    				//For each user selected construct a call item. Each family to be called must have a valid
    				//phone number and address. If either isn't valid, omit them from the call. 
    				int[] row_sel = sortTable.getSelectedRows();
    				for(int i=0; i<sortTable.getSelectedRowCount(); i++)
    				{
    					//Determine the family object
    					ONCFamily f = stAL.get(row_sel[i]);
			
    					//Build the phone number to call. Replace all dashes so that the resultant
    					//phone number is a 10 digit number
    					String phonenum = "None Found";
    					if(f.getHomePhone().equals("None Found") && f.getOtherPhon().equals("None Found"))
    						phonenum = "None Found";
    					else if(phonereq.equals("Home Phone") && !f.getHomePhone().equals("None Found"))
    						phonenum = f.getHomePhone().split("\n",2)[0];
    					else if(phonereq.equals("Home Phone") && f.getHomePhone().equals("None Found"))
    						phonenum = f.getOtherPhon().split("\n",2)[0];
    					else if(phonereq.equals("Other Phone") && !f.getOtherPhon().equals("None Found"))
    						phonenum = f.getOtherPhon().split("\n",2)[0];
    					else if(phonereq.equals("Other Phone") && f.getOtherPhon().equals("None Found"))
    						phonenum = f.getHomePhone().split("\n",2)[0];
    					phonenum = phonenum.replaceAll("-", "");
			
    					//Build the address to call. Convert a street suffix to a whole word if abbreviated
    					//Also, include the unit, if there is one. Check to see if were using the actual address
    					//or the substitute delivery address

    			        //determine whether family has a substitute delivery address
    					StringBuffer address = new StringBuffer("");
    					String houseNum, streetName, unit;
    					
    			        if(f.getSubstituteDeliveryAddress()!= null && !f.getSubstituteDeliveryAddress().isEmpty() &&
    			      		f.getSubstituteDeliveryAddress().split("_").length == 5)
    			      	{
    			        	//use the substitute delivery address
    			      		String[] addPart = f.getSubstituteDeliveryAddress().split("_");
    			      		houseNum = addPart[0];
    			      		streetName = addPart[1];
    			      		unit = addPart[2].equals("None") ? "" : addPart[2];
    			      		
    			      	}
    			        else	//use the actual address
    			        {
    			        	houseNum = f.getHouseNum();
    			        	streetName = f.getStreet();
    			        	unit = f.getUnitNum();
    			        }
    			        
    			        //process the address
    			        String[] streetname = streetName.split(" ");
    			        if(streetname.length > 1)	//Must be a valid street name with a name and suffix
    			        {
        			        if(!houseNum.isEmpty())
        			        	address.append(houseNum + " ");
    			        	
    			        	//Check for street direction. If found append it. Then append the street name.
    			        	//If a street direction was found, start with streetname[1], else
    			        	//start with streetname[0]
    			        	int index = appendStreetDirection(streetname[0], address) ? 1 : 0;
				
    			        	while(index < streetname.length -1)
    			        		address.append(streetname[index++] + " ");
				
    			        	//Append the suffix
    			        	String suffix = streetname[streetname.length-1];
    			        	suffix = suffix.replace(".", "");	//Check for period and replace it
    			
    			        	for(int abbr=0; abbr < abbreviations.length; abbr++)	//Check for abbreviations and replace them
    			        		if(suffix.equalsIgnoreCase(abbreviations[abbr][0]))
    			        			suffix = abbreviations[abbr][1];
	    	    		
    			        	address.append(suffix);
	    	    		
    			        	//Append the unit, if any. If it has the abbrev Apt or Apt., spell out Apartment
    			        	unit = unit.replace(".", "");
    			        	unit = unit.replace("Apt", "Apartment");
    			        	address.append(" " + unit);
    			        
    			        }
			
    					String[] exportRow = {phonenum, address.toString().trim(), f.getONCNum()};
    					writer.writeNext(exportRow);
    				}
	    	   
    				writer.close();
	    	    
    				JOptionPane.showMessageDialog(parentFrame, 
						sortTable.getSelectedRowCount() + " call items sucessfully exported to " + oncwritefile.getName(), 
						"Export Successful", JOptionPane.INFORMATION_MESSAGE, oncGVs.getImageIcon(0));
    			} 
    			catch (IOException x)
    			{
    				JOptionPane.showMessageDialog(parentFrame, 
						"Export Failed, I/O Error: "  + x.getMessage(),  
						"Export Failed", JOptionPane.ERROR_MESSAGE, oncGVs.getImageIcon(0));
    				System.err.format("IOException: %s%n", x);
    			}
    		}
		}
       	
       	callCB.setSelectedIndex(0); //Reset the combo box
	}
	
	boolean appendStreetDirection(String streetdir, StringBuffer address)
	{
		boolean bStreetDirectionFound = false;
		if(streetdir.equalsIgnoreCase("n."))
		{
			address.append("North ");
			bStreetDirectionFound = true;
		}
		else if(streetdir.equalsIgnoreCase("s."))
		{
			address.append("South ");
			bStreetDirectionFound = true;
		}
		else if(streetdir.equalsIgnoreCase("e."))
		{
			address.append("East ");
			bStreetDirectionFound = true;
		}
		else if(streetdir.equalsIgnoreCase("w."))
		{
			address.append("West ");
			bStreetDirectionFound = true;
		}
			
		return bStreetDirectionFound;
	}

	/*********************************************************************************************
	 * This method uses Jasper Reports library to produce a .pdf file that contains the yellow 
	 * cards. The libraries were removed from the classpath when this became obsolete. The necessary 
	 * libraries are: 
	 * 
	 *********************************************************************************************
	void onPrintYellowCard()
	{
		
		//create the parameters map
		Map<java.lang.String, java.lang.Object> parameters = new HashMap<java.lang.String, java.lang.Object>();
		parameters.put("oncseason", "ONC " + Integer.toString(oncGVs.getCurrentSeason()));
		
		SimpleDateFormat sYear = new SimpleDateFormat("yy");
		int idx = Integer.parseInt(sYear.format(oncGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
		
		BufferedImage bi = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.createGraphics();
		// paint the Icon to the BufferedImage.
		oncGVs.getImageIcon(idx + XMAS_ICON_OFFSET).paintIcon(null, g, 0,0);
		g.dispose();
		parameters.put("photo", bi);
		
		//Create the data source for field info
		ArrayList<ONCYellowCard>  ycAL= new ArrayList<ONCYellowCard>();	
		int[] row_sel = sortTable.getSelectedRows();
		for(int i=0; i< row_sel.length; i++)
		{
			int index = 0;
			while(stAL.get(row_sel[i]).getSortItemONCID() != fAL.get(index).getONCID())	//Find the family index
				index++;
			ONCFamily f = fAL.get(index);	//get a reference to ONCFamily object
			
			String reg = regions.getRegionID(f.getRegion());
			ycAL.add(new ONCYellowCard(f.getYellowCardData(), reg));
		}
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(ycAL);
		
		//Fill the report
		String sourceFileName = System.getProperty("user.dir") +"/yellowcard.jasper";
		String destFileName = System.getProperty("user.dir") +"/yellowcard.pdf";
		try
		{
			JasperPrint jprintfile = JasperFillManager.fillReport(sourceFileName, parameters, beanColDataSource);
			JasperExportManager.exportReportToPdfFile(jprintfile, destFileName);  
		}
		catch (JRException e)
		{
			e.printStackTrace();
		}
	}
**************************************************************************************************/	
	
	void checkApplyChangesEnabled()
	{
		//Can't change family status to packaged or delivery status to assigned from this dialog,
		//since a change to "Packaged" requires user input of # of bags and # of large items.
		//Similarly, a delivery status changed to assigned requires user input making the
		//delivery. Enable apply changes button when there are table rows selected and the 
		//combo boxes have valid values set 
		if(sortTable.getSelectedRows().length > 0 &&
				(changeFStatusCB.getSelectedIndex() != 0 ||					
				     !(changeDStatusCB.getSelectedIndex() == 0 ||
				        changeDStatusCB.getSelectedIndex() == CHANGE_DELIVERY_STATUS_ASSIGNED) ||
					     !changeDNSCB.getSelectedItem().toString().equals(DEFAULT_NO_CHANGE_LIST_ITEM)))
			btnApplyChanges.setEnabled(true);
		else
			btnApplyChanges.setEnabled(false);
	
		if(sortTable.getSelectedRowCount() > 0)
		{
			printCB.setEnabled(true);
			
			if(oncGVs.isUserAdmin())
			{
				emailCB.setEnabled(true);
				callCB.setEnabled(true);
			}
		}
	}
	
	//Resets each search criteria gui and its corresponding member variable to the initial
	//condition and then rebuilds the table array. It disables the gui event before changing
	//to prevent multiple builds of the table.
	void onResetCriteriaClicked()
	{
		oncCB.setEnabled(false);
		oncCB.setSelectedIndex(0);
		sortONC = "Any";
		oncCB.setEnabled(true);
		
		batchCB.setEnabled(false);
		batchCB.setSelectedIndex(0);
		sortBatchNum = 0;
		batchCB.setEnabled(true);
		
		dnsCB.setEnabled(false);
		dnsCB.setSelectedIndex(0);
		sortDNSCode = dnsCodes[0];
		dnsCB.setEnabled(true);
		
		fstatusCB.setEnabled(false);
		fstatusCB.setSelectedIndex(0);
		sortFStatus = 0;
		fstatusCB.setEnabled(true);
		
		dstatusCB.setEnabled(false);
		dstatusCB.setSelectedIndex(0);
		sortDStatus = 0;
		dstatusCB.setEnabled(true);
		
		lastnameCB.setEnabled(false);
		lastnameCB.setSelectedIndex(0);
		sortLN = "Any";
		lastnameCB.setEnabled(true);
		
		streetCB.setEnabled(false);
		streetCB.setSelectedIndex(0);
		sortStreet = "Any";
		streetCB.setEnabled(true);
		
		zipCB.setEnabled(false);
		zipCB.setSelectedIndex(0);
		sortZip = 0;
		zipCB.setEnabled(true);
		
		regionCB.setEnabled(false);
		regionCB.setSelectedIndex(0);
		sortRegion = 0;
		regionCB.setEnabled(true);
		
		changedByCB.setEnabled(false);
		changedByCB.setSelectedIndex(0);
		sortChangedBy = 0;
		changedByCB.setEnabled(true);
		
		stoplightCB.setEnabled(false);
		stoplightCB.setSelectedIndex(0);
		sortStoplight = 0;
		stoplightCB.setEnabled(true);
		
		buildTableList();
	}
	
	void createAndSendFamilyEmail(int emailType)
	{
		//build the email
		ArrayList<ONCEmail> emailAL = new ArrayList<ONCEmail>();
		ArrayList<ONCEmailAttachment> attachmentAL = new ArrayList<ONCEmailAttachment>();
		String cid0 = null, cid1 = null;
		String subject = null;
		
		//Create the attachment array list
		if(emailType == 1)
		{	
			//Create the email subject
			subject = "Holiday Gift Confirmation from Our Neighbor's Child (Desplacese hacia abajo para espanol)";  			
//			cid0 = ContentIDGenerator.getContentId();
//			cid1 = ContentIDGenerator.getContentId();
//			attachmentAL.add(new ONCEmailAttachment("DSC_0154.jpeg", cid0 , MimeBodyPart.INLINE));
//			attachmentAL.add(new ONCEmailAttachment("Warehouse 3.jpeg", cid1, MimeBodyPart.INLINE));
		}
		
		//For each family selected, create the email subject, body and recipient information in an
		//ONCEmail object and add it to the email array list
		int[] row_sel = sortTable.getSelectedRows();
		for(int row=0; row< sortTable.getSelectedRowCount(); row++)
		{
			//Get selected family object
			ONCFamily fam = stAL.get(row_sel[row]);
			
			//Create the email body, method call generates a new email body string
			String emailBody = createEmailBody(fam, cid0, cid1);
			
	        //Create recipient list for email. Method call creates a new List of EmailAddresses
	        ArrayList<EmailAddress> recipientAdressList = createRecipientList(fam);
	       
	        //If the email isn't valid, the message will not be sent.
	        if(emailBody != null && !recipientAdressList.isEmpty())
	        	emailAL.add(new ONCEmail(subject, emailBody, recipientAdressList));     	
		}
		
		//Create the from address string array
		EmailAddress fromAddress = new EmailAddress(FAMILY_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child");
		
		//Create the blind carbon copy list 
		ArrayList<EmailAddress> bccList = new ArrayList<EmailAddress>();
		bccList.add(new EmailAddress(FAMILY_EMAIL_SENDER_ADDRESS, "Our Neighbor's Child"));
//		bccList.add(new EmailAddress("mnrogers123@msn.com", "Nicole Rogers"));
//		bccList.add(new EmailAddress("johnwoneill@cox.net", "John O'Neill"));
		
		//Create mail server accreditation, then the mailer background task and execute it
		//Go Daddy Mail
//		ServerCredentials creds = new ServerCredentials("smtpout.secureserver.net", "director@act4others.org", "crazyelf1");
		//Google Mail
		ServerCredentials creds = new ServerCredentials("smtp.gmail.com", "clientinformation@ourneighborschild.org", "crazyelf");
		
	    oncEmailer = new ONCEmailer(this, progressBar, fromAddress, bccList, emailAL, attachmentAL, creds);
	    oncEmailer.addPropertyChangeListener(this);
	    oncEmailer.execute();
	    emailCB.setEnabled(false);		
	}
	
	/**************************************************************************************************
	 *Creates a new email body for each family email. If family is valid or doesn't have a valid first 
	 *name, a null body is returned
	 **************************************************************************************************/
	String createEmailBody(ONCFamily fam, String cid0, String cid1)
	{
		String emailBody = null;
		
		//verify the family has a valid name. If not, return a null body
		if(fam != null && fam.getHOHFirstName() != null && fam.getHOHFirstName().length() > MIN_EMAIL_NAME_LENGTH) 
		{
			emailBody = create2014FamilyEmailText(fam);
		}
        	
		return emailBody;
	}
	
	String create2014FamilyEmailText(ONCFamily fam)
	{
		//Create the variables for the body of the email 
		String hohFirstName = fam.getHOHFirstName();
        String familyname = fam.getClientFamily();
        String streetaddress = fam.getHouseNum() + " " + fam.getStreet() + " " + fam.getUnitNum();
        String citystatezip = fam.getCity() + ", VA " + fam.getZipCode();
        
        String altstreetaddress = "";
        String altcitystatezip = "";
  
        //determine whether family has a substitute delivery address
        if(fam.getSubstituteDeliveryAddress()!= null && !fam.getSubstituteDeliveryAddress().isEmpty() &&
      		fam.getSubstituteDeliveryAddress().split("_").length == 5)
      	{
      		String[] addPart = fam.getSubstituteDeliveryAddress().split("_");
      		String unit = addPart[2].equals("None") ? "" : addPart[2];
      		altstreetaddress = addPart[0] + " " + addPart[1] + " " + unit;
      		altcitystatezip = addPart[3] + ", VA " + addPart[4];
      	}
        
        String homephones = fam.getHomePhone();
        String otherphones = fam.getOtherPhon();
        String emailaddress = fam.getFamilyEmail().length() > MIN_EMAIL_ADDRESS_LENGTH ? fam.getFamilyEmail() : "None Found";
       
        //Create the text part of the email using html
        String msg = String.format(
        	"<html><body><div>" +
        	"<p>Dear %s,</p>"+
        	"<p>Your request for Holiday Assistance has been received by Our Neighbor's Child, the local, " +
        	"community-based volunteer organization that provides holiday gifts to children in your area.</p>" +
        	"<p>This e-mail is being sent to you (if you included an e-mail address) and/or your referring agent " +
        	"(if no e-mail address was provided).</p>" +
        	"<p>This e-mail only pertains to <b>HOLIDAY GIFTS</b> for your child/children.  Holiday food assistance is " +
        	"handled by other organizations and notification is separate.</p>" +
        	"<p><b>Here is the information that was provided by your School Counselor or other referring agent:</b></p>" +
    		"&emsp;<b>Family Name:</b>  %s<br>" +
    		"&emsp;<b>Address:</b>  %s<br>" +
    		"&emsp;<b>Address:</b>  %s<br>" +
    		"&emsp;<b>Home Phone #:</b>  %s<br>" +
    		"&emsp;<b>Other Phone #:</b>  %s<br>" +
    		"&emsp;<b>Email Address:</b>  %s<br>" + 
    		"&emsp;<b>Alternate Delivery Address:</b>  %s<br>" +
    		"&emsp;<b>Alternate Delivery Address:</b>  %s<br>" + 
        	"<p>An Our Neighbor's Child volunteer will deliver your children's gifts to the address listed above " +
        	"on <b>Sunday, December 14th between 1 and 4PM.</b>  You will receive an automated phone call reminder and " +
        	"an adult must be home to receive the gifts.</p>" +
        	"<p><b>Important:  Families may only be served by one organization.</b> If your child/children's name " +
        	"appear on any other list (i.e. The Salvation Army), ONC will remove them from this list and will be " +
        	"unable to deliver gifts to your home.</p>" +
        	"<p>If your address or telephone number should change, PLEASE REPLY to this e-mail.  We are unable to " +
        	"accept any gift requests or changes to gift requests.</p>" +
        	"<p>If an emergency arises and you are unable to have an adult home on Sunday, December 14th between 1 " +
        	"and 4PM - PLEASE REPLY to this e-mail with an alternate local address (Centreville, Chantilly, Clifton or Fairfax) where " +
        	"someone will be home to receive the gifts on that day between 1 and 4PM.</p>"+
        	"<p>Thank you for your assistance and Happy Holidays!</p>" +
        	"<p><b>Our Neighbor's Child</b></p>" +
        	"<br>------------------------------<br>" +
        	"<p>Querido %s,</p>"+
        	"<p>Su solicitud para Asistencia de Navidad ha sido recibida por Our Neighbor's Child, la organizaci&#243;n " +
        	"local de voluntarios que proporciona regalos de Navidad a los ni&#241;os en su comunidad.</p>" +
        	"<p>Se recibe este mensaje (si se incluyo una direcci&#243;n de correo electr&#243;nico) y/o el agente que lo " +
        	"refiri&#243; (si no se proporcion&#243; una direcci&#243;n de correo electr&#243;nico).</p>" +
        	"<p>Este mensaje electr&#243;nico sol&#243; pertenece a LOS REGALOS DE NAVIDAD para su hijo/hijos. La asistencia " +
        	"de comida de Navidad se hace por otras organizaciones y la notificaci&#243;n es separada.</p>" +
        	"<p><b>Aqu&#237; est&#225; la informaci&#243;n que fue proporcionado por su consejero de la escuela o el otro agente " +
        	"que lo refiri&#243;:</b></p>" +
    		"&emsp;<b>Apellido:</b>  %s<br>" +
    		"&emsp;<b>Direcci&#243;n:</b>  %s<br>" +
    		"&emsp;<b>Direcci&#243;n:</b>  %s<br>" +
    		"&emsp;<b>Numero de tel&#233;fono:</b>  %s<br>" +
    		"&emsp;<b>Numero alternativo:</b>  %s<br>" +
    		"&emsp;<b>Correo electr&#243;nico:</b>  %s<br>" + 
    		"&emsp;<b>Direcci&#243;n alternativo:</b>  %s<br>" +
    		"&emsp;<b>Direcci&#243;n alternativo:</b>  %s<br>" +
        	"<p>Un voluntario de Our Neighbor's Child entregar&#225; los regalos para su hijo/hijos a la direcci&#243;n de " +
        	"arriba el domingo, 14 de diciembre entre la 1 y la 4 de la tarde. Recibir&#225; una llamada de tel&#233;fono " +
        	"automatizada como un recordatorio y un adulto debe estar en casa para recibir los regalos</p>" +
        	"<p><b>Importante: Sol&#243; una organizaci&#243;n puede servir cada familia</b>. Si su nombre o el nombre de " +
        	"su hijo aparezca en cualquier otra lista (como The Salvation Army), Our Neighbor's Child le quitar&#225; " +
        	"de nuestro lista y no podr&#225; entregar los regalos a su hogar</p>" +
        	"<p>Si su direcci&#243;n o numero de tel&#233;fono cambia, <b>POR FAVOR RESPONDA</b> a este mensaje. Sin embargo, no " +
        	"podemos aceptar peticiones de regalos o cambios de peticiones.</p>" +
        	"<p>Si hay una emergencia y un adulto no puede estar en su casa el domingo, 14 de diciembre, entre " +
        	"la 1 y las 4 de la tarde <b>POR FAVOR RESPONDA</b> a este mensaje con una direcci&#243;n local alternativa " +
        	"(en Centreville, Clifton, o Fairfax) donde un adulto estar&#225; durante el d&#237;a de entrega entre la 1 y " +
        	"las 4.</p>"+
        	"<p>Gracias para su asistencia y Feliz Navidad!</p>" +
        	"<p><b>Our Neighbor's Child</b></p>" +
        	"</div></body></html>", hohFirstName, familyname, streetaddress, citystatezip, homephones, otherphones, 
        	emailaddress, altstreetaddress, altcitystatezip, hohFirstName, familyname, streetaddress, citystatezip, 
        	homephones, otherphones, emailaddress, altstreetaddress, altcitystatezip);
        return msg;
	}
	
	/**************************************************************************************************
	 *Creates a new list of recipients for each family email. There are two recipients for each family
	 *email, the family using the provided email address and the agent, using the agent email address.
	 *Only valid email addresses are used. If both are invalid, the email recipient list is empty. 
	 **************************************************************************************************/
	ArrayList<EmailAddress> createRecipientList(ONCFamily fam)
	{
		ArrayList<EmailAddress> recipientAddressList = new ArrayList<EmailAddress>();
		
		//get the family info if family isn't null
		if(fam != null)
		{
			//get the family head of house hold name
			String famHOHFullName = fam.getHOHFirstName() + " " + fam.getHOHLastName();
		
			//verify the family has a valid email address and name. 
			if(fam.getFamilyEmail().length() > MIN_EMAIL_ADDRESS_LENGTH &&
				famHOHFullName.length() > MIN_EMAIL_NAME_LENGTH)
			{
				EmailAddress toAddress = new EmailAddress(fam.getFamilyEmail(), famHOHFullName);
//				EmailAddress toAddress = new EmailAddress("johnwoneill@cox.net", famHOHFullName);	//test
				recipientAddressList.add(toAddress);
			}

/***************** NO LONGER ARE SENDING NOTIFICATION EMAILS TO AGENTS, JUST FAMILIES - NOV 13, 2014 *************
 			//Get reference to Agent DB so we can add agent e-mail address
//			ONCAgents agentDB = ONCAgents.getInstance();
//			Agent agent = agentDB.getAgent(fam.getAgentID());
 			
			//verify the agent has a valid email address and name
			if(agent != null && agent.getAgentEmail() != null && agent.getAgentEmail().length() > MIN_EMAIL_ADDRESS_LENGTH &&
					agent.getAgentName() != null && agent.getAgentName().length() > MIN_EMAIL_NAME_LENGTH)
			{
				EmailAddress toAddress = new EmailAddress(agent.getAgentEmail(), agent.getAgentName());	//live
//				EmailAddress toAddress = new EmailAddress("jwoneill1@aol.com", agent.getAgentName());	//test
				recipientAddressList.add(toAddress);				
			}
******************* NO LONGER ARE SENDING NOTIFICATION EMAILS TO AGENTS, JUST FAMILIES _ NOV 13, 2014 *************/
		}
		
		return recipientAddressList;
	}
	
	//set up the search criteria filters
	boolean doesONCNumMatch(String oncn) {return sortONC.equals("Any") || oncn.equals(oncCB.getSelectedItem().toString());}
	
	boolean doesBatchNumMatch(String bn) {return sortBatchNum == 0 ||  bn.equals(batchCB.getSelectedItem());}
	
	boolean doesDNSCodeMatch(String dnsc)
	{
		if(sortDNSCode.equals("Any"))
			return true;
		else if(sortDNSCode.equals("None") && dnsc.isEmpty())
			return true;
		else if(dnsc.equalsIgnoreCase(dnsCB.getSelectedItem().toString()))
			return true;
		else
			return false;
	}
	
	boolean doesFStatusMatch(int fstat) {return sortFStatus == 0  || fstat == fstatusCB.getSelectedIndex()-1;}
	
	boolean doesDStatusMatch(int dstat) {return sortDStatus == 0  || dstat == dstatusCB.getSelectedIndex()-1;}
	
	boolean doesLastNameMatch(String ln) {return sortLN.equals("Any") || ln.toLowerCase().contains(lastnameCB.getSelectedItem().toString().toLowerCase());}
	
	boolean doesStreetMatch(String street) {return sortStreet.equals("Any") || street.toLowerCase().contains(streetCB.getSelectedItem().toString().toLowerCase());}
	
	//If search zip selected is NISA - Not in service area, all zip codes outside the ONC assigned zip codes
	//are a match
	boolean doesZipMatch(String zip)
	{
		if(sortZip == ZIP_OUTOFAREA)
			return !(zip.equals("20120") || zip.equals("20121") || zip.equals("20124") ||
					zip.equals("20151") || zip.equals("22033") || zip.equals("22039"));	
		else
			return sortZip == 0 || zip.equals(zipCB.getSelectedItem());
	}
	
	boolean doesRegionMatch(int fr) { return sortRegion == 0 || fr == regionCB.getSelectedIndex()-1; }
	
	boolean doesChangedByMatch(String cb) { return sortChangedBy == 0 || cb.equals(changedByCB.getSelectedItem()); }
	
	boolean doesStoplightMatch(int sl) { return sortStoplight == 0 || sl == stoplightCB.getSelectedIndex()-1; }
	
	void setFamilyStatusComboItemEnabled(int index, boolean tf) {changeFamItem[index].setEnabled(tf); }

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == oncCB && !oncCB.getSelectedItem().toString().equals(sortONC))
		{
			sortONC = oncCB.getSelectedItem().toString();
			buildTableList();		
		}				
		else if(e.getSource() == batchCB && batchCB.getSelectedIndex() != sortBatchNum)
		{
			sortBatchNum = batchCB.getSelectedIndex();
			buildTableList();			
		}		
		else if(e.getSource() == dnsCB && !dnsCB.getSelectedItem().toString().equals(sortDNSCode))
		{
			sortDNSCode = dnsCB.getSelectedItem().toString();
			buildTableList();
		}
		else if(e.getSource() == fstatusCB && fstatusCB.getSelectedIndex() != sortFStatus)
		{						
			sortFStatus = fstatusCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == dstatusCB && dstatusCB.getSelectedIndex() != sortDStatus)
		{						
			sortDStatus = dstatusCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == lastnameCB && !lastnameCB.getSelectedItem().toString().equals(sortLN))
		{			
			sortLN = lastnameCB.getSelectedItem().toString();
			buildTableList();
		}	
		else if(e.getSource() == streetCB && !streetCB.getSelectedItem().toString().equals(sortStreet))
		{			
			sortStreet = streetCB.getSelectedItem().toString();
			buildTableList();
		}	
		else if(e.getSource() == zipCB && zipCB.getSelectedIndex() != sortZip )
		{						
			sortZip = zipCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == regionCB && regionCB.getSelectedIndex() != sortRegion)
		{
			sortRegion = regionCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == changedByCB && changedByCB.getSelectedIndex() != sortChangedBy && !bIngoreCBEvents)
		{
			sortChangedBy = changedByCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == stoplightCB && stoplightCB.getSelectedIndex() != sortStoplight)
		{
			sortStoplight = stoplightCB.getSelectedIndex();
			buildTableList();
		}
		else if(e.getSource() == printCB)
		{
			if(printCB.getSelectedItem().toString().equals(printChoices[1])) { onPrintListing("ONC Families"); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[2])) { onPrintBookLabels(); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[3])) { onPrintVerificationSheets(RECEIVING_SHEET); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[4])) { onPrintVerificationSheets(PRE_PACKAGING_SHEET); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[5])) { onPrintVerificationSheets(PACKAGING_SHEET); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[6])) { onPrintDeliveryCards(); }
			else if(printCB.getSelectedItem().toString().equals(printChoices[7]) && sortTable.getSelectedRowCount() > 0)
			{
				//Only print selected rows)	//Print delivery directions 
				try
				{
					onPrintDeliveryDirections();
				}
				catch (JSONException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
			}
		}
		else if(e.getSource() == callCB)
		{
			if(callCB.getSelectedIndex() == 1)	//Place immediate call
			{
//				System.out.println("Requesting automated call");
//				
//				OutboundApiInvoker roboCaller = new OutboundApiInvoker();
//				
//				roboCaller.setApiKey("0a140220-0b-13af5fb0a48-b1c2dd47-777@0a140225-04-13af5011fcf-8f6bc6a2-" +
//									 "c74@c7ff532e@1352746601032@10@8020c008eb24ff586599107f630e7bba");
//				roboCaller.setSiteNumber("200000126686");
//				roboCaller.setSubscriberId("0a140225-04-13af5011fcf-8f6bc6a2-c74");
//				
//				Map<String,String> variableMap = new HashMap<String,String>();
//				variableMap.put("zipcode", "20120");
//				variableMap.put("Address", "6211 Point Circle, Centreville VA");
//				
//				roboCaller.immediateCall(5,"5713440902", variableMap);
			}
			else if(callCB.getSelectedItem().toString().equals("Export Call File"))
			{
				onExportAngelInputFile();
			}
		}
		else if(e.getSource() == emailCB && emailCB.getSelectedIndex() > 0)	//only one email currently
		{
			//Confirm with the user that the deletion is really intended
			String confirmMssg = "Are you sure you want to send family email?"; 
											
			Object[] options= {"Cancel", "Send"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								oncGVs.getImageIcon(0), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(parentFrame, "*** Confirm Send Family Email ***");
			confirmDlg.setLocationRelativeTo(this);
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Send"))
				createAndSendFamilyEmail(emailCB.getSelectedIndex());
			
			emailCB.setSelectedIndex(0);	//Reset the combo box choice
		}
		else if(e.getSource() == btnApplyChanges)
		{			
			//Apply the changes. If changes occurred, update the current family display, 
			//it may have been changed and update the assign driver dialog sort table as well
			onApplyChanges();
		}

		checkApplyChangesEnabled();	//Check to see if user postured to change status or assignee. 
	}
	
	@Override
	public void dataChanged(DatabaseEvent dbe)
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_FAMILY") ||
			dbe.getType().equals("ADDED_FAMILY") || dbe.getType().equals("ADDED_DELIVERY"))
		{
			buildTableList();		
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
			String[] regList = (String[]) dbe.getObject();
			updateRegionList(regList);
		}
		else if(dbe.getType().equals("LOADED_USERS") || dbe.getType().equals("ADDED_USER"))
		{
			updateUserList();
		}
	}
	
	
	/**********************************************************************************************
	 * This class implements the Printable interface to print ONC Delivery Cards. The user selects
	 * which families they want to print delivery cards for in the sort table. The sort table 
	 * array list, stAL, holds a reference to each family object in the table. 
	 * 
	 * The method uses the number of family's selected to determine the midpoint. It prints two
	 * family's delivery information cards on a single 8.5" x 11" sheet of paper. The first sheet
	 * will contain deliver cards for the first family and the first family after the midpoint. The
	 * pattern will repeat for each sheet until all family's selected have had delivery cards
	 * printed. This allows for easy collation of delivery cards after the delivery card sheets have
	 * been cut into halves. 
	 * @author John O'Neill
	 ***********************************************************************************************/
	private class DeliveryCardPrinter implements Printable
	{
		void printDeliveryCard(int x, int y, String[] line, Font[] ycFont, String season, Image img, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x+484, y+76, x+484+destX1, y+76+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
	         
		    //Draw the yellow card fixed text, all bold text
		    g2d.setFont(ycFont[0]);
		    g2d.drawString("Driver:________________", x+224, y+27);
		    
		    g2d.setFont(ycFont[1]);
		    g2d.drawString(season, x+430, y+106);
		    g2d.drawString("Region:", x+0, y+124);
		    g2d.drawString("Phone Info:",  x+0, y+150);
		    g2d.drawString("Alternate:",  x+238, y+150);
		    g2d.drawString("Language:", x+0, y+186);
		    g2d.drawString("Special Delivery Comments:", x+0, y+220);
		    	    
		    //Draw yellow card string information
		    g2d.setFont(ycFont[2]);
			g2d.drawString(line[0], x, y+27); 	//ONC Number
			
			g2d.setFont(ycFont[3]);
			g2d.drawString(line[1], x, y+64);		//First and Last Name
			g2d.drawString(line[2], x, y+76);		//Street Address
			g2d.drawString(line[3], x, y+88);		//City, State, Zip
			g2d.drawString(line[4], x+52, y+124); 	//Region	    
			g2d.drawString(line[5], x+66, y+150);	//Home Phone 1
			if(!line[6].isEmpty())
				g2d.drawString("or " + line[6], x+144, y+150);	//Home Phone 2
		    g2d.drawString(line[7], x+300, y+150);	//Other Phone 1
		    if(!line[8].isEmpty())
		    	g2d.drawString("or " + line[8], x+378, y+150);	//Other Phone 2
			g2d.drawString(line[9], x+66, y+186);	//Language
		    g2d.drawString(line[10], x+158, y+220); //ONC Delivery Instructions	    
		    
		    //Draw the last line
		    g2d.setFont(ycFont[4]);
		    g2d.drawString("TOY BAGS", x+72, y+266);
		    g2d.drawString("BIKE(S)", x+216, y+266);
		    g2d.drawString("OTHER LARGE ITEMS", x+346, y+266);
		    
		    //Draw the # of Bikes. If the family status == PACKAGED, draw the # of bags and large items
		    g2d.setFont(ycFont[2]);
		    g2d.drawString(line[12], x+226, y+296);	//Draw # of bikes assigned to families children
		    
		    if(line[14].equals(Integer.toString(FAMILY_STATUS_PACKAGED)))
		    {
		    	g2d.drawString(line[11], x+88, y+296);	//Draw # of bags used to package family
		    	g2d.drawString(line[13], x+394, y+296);	//Draw # of large items assigned to family
		    }
		}
		
		@Override
		public int print (Graphics g, PageFormat pf, int page) throws PrinterException
		{		
			if(page > (sortTable.getSelectedRowCount()-1)/YELLOW_CARDS_PER_PAGE)	//'page' is zero-based 
			{
				return NO_SUCH_PAGE;
		    }
			
			SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
			int idx = Integer.parseInt(twodigitYear.format(oncGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = oncGVs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();
			
			String oncSeason = "ONC " + Integer.toString(GlobalVariables.getCurrentSeason());
			
			String carddata[];	//Holds all string data for a card
		     
			Font[] cFonts = new Font[5];
		    cFonts[0] = new Font("SansSerif", Font.BOLD, 18);	//Driver Font
		    cFonts[1] = new Font("SansSerif", Font.BOLD, 10);	//Fixed Text Font
		    cFonts[2] = new Font("SansSerif", Font.BOLD, 24);	//ONC Number Text Font
		    cFonts[3] = new Font("SansSerif", Font.PLAIN, 10);	//Variable Text Font
		    cFonts[4] = new Font("SansSerif", Font.BOLD, 10);	//Bottom Line Font
		    	    
	 		//Print a label for each sort table line selected
		    int[] row_sel = sortTable.getSelectedRows();
		    int cardnum = 0;	//0 or 1, since there are two cards per page
//		    int row = page * YELLOW_CARDS_PER_PAGE;
		    int midpoint = (sortTable.getSelectedRowCount()+1)/2;
	 			    	 
		    // User (0,0) is typically outside the imageable area, so we must
		    //translate by the X and Y values in the PageFormat to avoid clipping
		    Graphics2D g2d = (Graphics2D)g;
		    g2d.translate(pf.getImageableX(), pf.getImageableY());
		    		   	    
//		    while(cardnum < YELLOW_CARDS_PER_PAGE && row+cardnum < sortTable.getSelectedRowCount())
		    while(cardnum < YELLOW_CARDS_PER_PAGE && cardnum * midpoint + page < sortTable.getSelectedRowCount())
		    {
		    	//Create the data for the yellow card. First, obtain a reference to the family object
		    	//for the selected family. 
				ONCFamily f = stAL.get(row_sel[cardnum * midpoint + page]);	
				
		    	carddata = fDB.getYellowCardData(f);
		    	carddata[4] = regions.getRegionID(f.getRegion());
		    	
		    	printDeliveryCard(0, cardnum * 396, carddata, cFonts, oncSeason, img, g2d);
		    	cardnum++;
		    }
		    	    
		    // tell the caller that this page is part of the printed document	   
		    return PAGE_EXISTS;
		}		
	}
	
	/***************************************************************************************
	 * This class extends the VerificationSheetPrinter class to print ONC Packaging Sheets. 
	 * An array list containing ONCVerificationSheets is passed at class instantiation, along
	 * with the seasons ONC icon and year. The class overrides the print header, print footer
	 * and print sheet methods to print the specific format of the ONC Packaging Sheet
	 * @author John O'Neill
	 ***************************************************************************************/
	private class PackagingSheetPrinter extends VerificationSheetPrinter
	{	
		PackagingSheetPrinter(ArrayList<ONCVerificationSheet> psal, Image img, String season, ChildWishDB cwdb)
		{
			super(psal);
			this.img = img;
			oncSeason = season;
		}
	
		void printVerificationSheetHeader(int x, int y, Image img, String season, String fn,
											Font[] psFont, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
	         
		    //Draw the ONC Season
		    g2d.setFont(psFont[1]);
		    g2d.drawString(season, x+54, y+22);
		    
		    //Draw the packaging sheet title
			g2d.setFont(psFont[1]);
			g2d.drawString("ONC Packaging Sheet", x+200, y+20);
			
		    //Draw header ONC Number
		    g2d.setFont(psFont[2]);
			g2d.drawString(fn, x+424, y+20); 	//ONC Number
			
//			//Draw the fulfillment check instruction
//		    g2d.setFont(psFont[0]);
//		    g2d.drawString("CHECK BOX(     ) WHEN ITEM HAS BEEN FULFILLED", x+10, y+56);
//		    drawThickRect(g2d, x+70, y+46, 12, 12, false);
//		    
//		    //Draw the Packaged By:
//		    g2d.setFont(psFont[6]);
//		    g2d.drawString("Packaged By:", x+310, y+56);		
		}
		
		/*************************************************************************************
		 * This method prints the body of a Packaging Sheet. If a child's gift 1 contains
		 * "Bike" the gift 1 check box is pre=checked. If gift 1 is a "Bike" and gift 2 contains
		 * "Helmet", the gift 2 check box is pre-checked
		 *************************************************************************************/
		void printVerificationSheetChild(int x, int y, int childnum, String childdata,
				String giftdata[], Font[] psFont, Graphics2D g2d)
		{
			//Draw Separator Line
			g2d.drawLine(x, y, x+520, y);

			//Draw Child Info
			g2d.setFont(psFont[3]);
			g2d.drawString("CHILD #" + Integer.toString(childnum) +":", x, y+14);
			g2d.drawString(childdata, x+56, y + 14);


			g2d.setFont(psFont[0]);
			//Draw Child Gift 1
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT, 12, 12, giftdata[0].contains("Bike-"));
			g2d.drawString("GIFT #1:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);
			g2d.drawString(giftdata[0], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);

			//Draw Child Gift 2		
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2, 12, 12, giftdata[0].contains("Bike-") && giftdata[1].contains("Helmet-"));
			g2d.drawString("GIFT #2:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);
			g2d.drawString(giftdata[1], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);

			//Draw Child Gift 3		
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3, 12, 12, false);
			g2d.drawString("GIFT #3:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);
			g2d.drawString(giftdata[2], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);

			//Draw Battery Info		
			drawThickRect(g2d, x+276, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4-0, 12, 12, false);
			g2d.drawString("BATTERIES- TYPE_______ QUANTITY_______", x+296, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4+12);		
}
			
		void printVerificationSheetFooter(int x, int y, int pgNum, int pgTotal, String zBikes, Font[] psFont, Graphics2D g2d)
		{			     
			//Draw separator line
			g2d.drawLine(x, y, x+525, y);
			
//			//Draw Packager Comments instruction
//			g2d.setFont(psFont[0]);
//			g2d.drawString("PACKAGER NOTES/SPECIAL COMMENTS:", x, y+16);
			
			//Draw the Packager Name Box
			g2d.setFont(psFont[0]);
			g2d.drawString("Packager Name:", x+4, y+20);
			drawThickRect(g2d, x, y+8, 160, 48, false);
			
			//Draw the Bikes, Large Items Text and Box, fill in the # of Bikes box
			g2d.setFont(psFont[4]);
			g2d.drawString("# OF BAGS", x+308, y+16);
			drawThickRect(g2d, x+314, y+24, 46, 46, false);
			
			g2d.setFont(psFont[4]);
			g2d.drawString("# OF BIKES", x+388, y+16);
			g2d.setFont(psFont[5]);
			g2d.drawString(zBikes, x+404, y+62);
			drawThickRect(g2d, x+392, y+24, 46, 46, false);

			//Draw the # of Bags text and box
			g2d.setFont(psFont[4]);
			g2d.drawString("LARGE ITEMS", x+460, y+16);
			drawThickRect(g2d, x+470, y+24, 46, 46, false);
			
			//Draw the page number 
			g2d.setFont(psFont[0]);
			String pageinfo = String.format("%d of %d", pgNum, pgTotal);
			g2d.drawString(pageinfo, x+262, y+76);
		}
	}
	
	/***************************************************************************************
	 * This class extends the VerificationSheetPrinter class to print ONC Gift Inventory Sheets. 
	 * An array list containing ONCVerificationSheets is passed at class instantiation, along
	 * with the seasons ONC icon and year. The class overrides the print header, print footer
	 * and print sheet methods to print the specific format of the ONC Gift Inventory Sheet
	 * @author John O'Neill
	 ***************************************************************************************/
	private class GiftInventorytSheetPrinter extends VerificationSheetPrinter
	{	
		GiftInventorytSheetPrinter(ArrayList<ONCVerificationSheet> vsal, Image img, String season, ChildWishDB cwdb) 
		{
			super(vsal);
			this.img = img;
			oncSeason = season;
		}
	
		void printVerificationSheetHeader(int x, int y, Image img, String season, String fn,
											Font[] psFont, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
	         
		    //Draw the ONC Season
		    g2d.setFont(psFont[1]);
		    g2d.drawString(season, x+54, y+22);
		    
		    //Draw the packaging sheet title
			g2d.setFont(psFont[1]);
			g2d.drawString("ONC Gift Inventory Sheet", x+190, y+20);
			
		    //Draw header ONC Number
		    g2d.setFont(psFont[2]);
			g2d.drawString(fn, x+424, y+20); 	//ONC Number
			
			//Draw the fulfillment check instruction
		    g2d.setFont(psFont[0]);
		    g2d.drawString("CHECK BOX(     ) IF ITEM IS IN FAMILY BAG, CIRCLE ITEM IF MISSING", x+10, y+56);
		    drawThickRect(g2d, x+70, y+46, 12, 12, false);
/*		    
		    //Draw the Packaged By:
		    g2d.setFont(psFont[6]);
		    g2d.drawString("Checked By:", x+370, y+56);	
*/	
		}
		
		/*************************************************************************************
		 * This method prints the body of a Gift Inventory Sheet. If a child's gift 1 contains
		 * "Bike" the gift 1 check box is pre-checked. If gift 1 is a "Bike" and gift 2 contains
		 * "Helmet", the gift 2 check box is pre-checked. If any of the gifts are "Video Game"
		 * or "Gift Card" the check box is pre-checked.
		 *************************************************************************************/
		void printVerificationSheetChild(int x, int y, int childnum, String childdata,
				String giftdata[], Font[] psFont, Graphics2D g2d)
		{
			//Draw Separator Line
			g2d.drawLine(x, y, x+520, y);

			//Draw Child Info
			g2d.setFont(psFont[3]);
			g2d.drawString("CHILD #" + Integer.toString(childnum) +":", x, y+14);
			g2d.drawString(childdata, x+56, y + 14);


			g2d.setFont(psFont[0]);
			//Draw Child Gift 1
			boolean addCheck1 = giftdata[0].contains("Bike") || giftdata[0].contains("Video Game") || giftdata[0].contains("Gift Card");
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT, 12, 12, addCheck1);
			g2d.drawString("GIFT #1:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);
			g2d.drawString(giftdata[0], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);

			//Draw Child Gift 2
			boolean addCheck2 = (giftdata[0].contains("Bike") && giftdata[1].contains("Helmet")) || giftdata[1].contains("Video Game") || giftdata[1].contains("Gift Card");
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2, 12, 12, addCheck2);
			g2d.drawString("GIFT #2:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);
			g2d.drawString(giftdata[1], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);

			//Draw Child Gift 3
			boolean addCheck3 = giftdata[2].contains("Video Game") || giftdata[2].contains("Gift Card");
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3, 12, 12, addCheck3);
			g2d.drawString("GIFT #3:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);
			g2d.drawString(giftdata[2], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);

			//Draw Duplicate + Battery Info
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4, 12, 12, false);
			g2d.drawString("Duplicate Items?", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4+12);
			drawThickRect(g2d, x+276, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4-0, 12, 12, false);
			g2d.drawString("BATTERIES- TYPE_______ QUANTITY_______", x+296, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4+12);		

		}
			
		void printVerificationSheetFooter(int x, int y, int pgNum, int pgTotal, String zBikes, Font[] psFont, Graphics2D g2d)
		{			     
			//Draw separator line
			g2d.drawLine(x, y, x+525, y);
			
//			//Draw Packager Comments instruction
//			g2d.setFont(psFont[0]);
//			g2d.drawString("NOTES/SPECIAL COMMENTS:", x, y+16);
			
			//Draw the Packager Name Box
			g2d.setFont(psFont[0]);
			g2d.drawString("Inventoried By: (name)", x+4, y+20);
			drawThickRect(g2d, x, y+8, 160, 48, false);
			
			//Draw the page number 
			g2d.setFont(psFont[0]);
			String pageinfo = String.format("%d of %d", pgNum, pgTotal);
			g2d.drawString(pageinfo, x+262, y+76);
		}
	}
	
	public class FamilyReceivingCheckSheetPrinter extends VerificationSheetPrinter
	{	
		FamilyReceivingCheckSheetPrinter(ArrayList<ONCVerificationSheet> vsal, Image img, String season, ChildWishDB cwdb) 
		{
			super(vsal);
			this.img = img;
			oncSeason = season;
		}
	
		void printVerificationSheetHeader(int x, int y, Image img, String season, String fn,
											Font[] psFont, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x, y, x+destX1, y+destY1, 0,0, img.getWidth(null),img.getHeight(null), null); 
	         
		    //Draw the ONC Season
		    g2d.setFont(psFont[1]);
		    g2d.drawString(season, x+54, y+22);
		    
		    //Draw the packaging sheet title
			g2d.setFont(psFont[1]);
			g2d.drawString("ONC Family Receiving Check Sheet", x+160, y+20);
			
		    //Draw header ONC Number
		    g2d.setFont(psFont[2]);
			g2d.drawString(fn, x+424, y+20); 	//ONC Number
			
			//Draw the fulfillment check instruction
		    g2d.setFont(psFont[0]);
		    g2d.drawString("CHECK BOX(     ) WHEN ITEM IS RECEIVED", x+10, y+56);
		    drawThickRect(g2d, x+70, y+46, 12, 12, false);
/*		    
		    //Draw the Packaged By:
		    g2d.setFont(psFont[6]);
		    g2d.drawString("Checked By:", x+370, y+56);
*/		
		}
		
		/*************************************************************************************
		 * This method prints the body of a Gift Inventory Sheet. If a child's gift 1 contains
		 * "Bike" the gift 1 check box is pre-checked. If gift 1 is a "Bike" and gift 2 contains
		 * "Helmet", the gift 2 check box is pre-checked
		 *************************************************************************************/
		void printVerificationSheetChild(int x, int y, int childnum, String childdata,
				String giftdata[], Font[] psFont, Graphics2D g2d)
		{
			//Draw Separator Line
			g2d.drawLine(x, y, x+520, y);

			//Draw Child Info
			g2d.setFont(psFont[3]);
			g2d.drawString("CHILD #" + Integer.toString(childnum) +":", x, y+14);
			g2d.drawString(childdata, x+56, y + 14);


			g2d.setFont(psFont[0]);
			//Draw Child Gift 1
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT, 12, 12, giftdata[0].contains("Bike"));
			g2d.drawString("GIFT #1:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);
			g2d.drawString(giftdata[0], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT+12);

			//Draw Child Gift 2		
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2, 12, 12, giftdata[0].contains("Bike") && giftdata[1].contains("Helmet"));
			g2d.drawString("GIFT #2:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);
			g2d.drawString(giftdata[1], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*2+12);

			//Draw Child Gift 3		
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3, 12, 12, false);
			g2d.drawString("GIFT #3:", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);
			g2d.drawString(giftdata[2], x+76, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*3+12);
/*
			//Draw Duplicate + Battery Info
			drawThickRect(g2d, x, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4, 12, 12, false);
			g2d.drawString("Duplicate Items?", x+24, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4+12);
			drawThickRect(g2d, x+276, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4-0, 12, 12, false);
			g2d.drawString("BATTERIES- TYPE_______ QUANTITY_______", x+296, y+VERIFICATION_SHEET_CHILD_RECORD_LINE_HEIGHT*4+12);		
*/
		}
			
		void printVerificationSheetFooter(int x, int y, int pgNum, int pgTotal, String zBikes, Font[] psFont, Graphics2D g2d)
		{			     
			//Draw separator line
			g2d.drawLine(x, y, x+525, y);
			
//			//Draw Packager Comments instruction
//			g2d.setFont(psFont[0]);
//			g2d.drawString("NOTES/SPECIAL COMMENTS:", x, y+16);
			
			//Draw the Packager Name Box
			g2d.setFont(psFont[0]);
			g2d.drawString("Received By: (name)", x+4, y+20);
			drawThickRect(g2d, x, y+8, 160, 48, false);
			
			//Draw the page number 
			g2d.setFont(psFont[0]);
			String pageinfo = String.format("%d of %d", pgNum, pgTotal);
			g2d.drawString(pageinfo, x+262, y+76);
		}
	}
	
	/*********************************************************************************************
	 * This class implements the Printable interface for printing ONC Book Labels on Avery 5164
	 * label sheets. It contains a method that knows how to print a book label. To print a label,
	 * the x, y position of the label on the sheet, the label content, the fonts to be used,
	 * the current season, the ONC icon and a Graphics2D object are passed. The 
	 * @author John O'Neill
	 ********************************************************************************************/
	private class AveryBookLabelPrinter implements Printable
	{
		private static final int AVERY_SHEET_X_OFFSET = 6;
		private static final int AVERY_SHEET_Y_OFFSET = 30;
		private static final int AVERY_LABEL_X_COORDINATE_OFFSET = 18;	//Used for coordinate translation
		private static final int AVERY_LABEL_Y_COORDINATE_OFFSET = 36;
		private static final int AVERY_LABEL_Y_OFFSET = 0;	//Distance from to of label to 1st text
		private static final int AVERY_LABELS_PER_PAGE = 6;
		private static final int AVERY_COLUMNS_PER_PAGE = 2;
		private static final int AVERY_LABEL_HEIGHT = 239;
		private static final int AVERY_LABEL_WIDTH = 300;
		private static final int AVERY_LABEL_IMAGE_X_OFFSET = 210;
		private static final int AVERY_LABEL_IMAGE_Y_OFFSET = -18;
		private static final int AVERY_LABEL_CHILD_ROW_HEIGHT = 20;
		
		void printLabel(int x, int y, String[] line, Font[] lFont, String season, Image img, Graphics2D g2d)
		{			     
		    double scaleFactor = (72d / 300d) * 2;
		     	     
		    // Now we perform our rendering 	       	    
		    int destX1 = (int) (img.getWidth(null) * scaleFactor);
		    int destY1 = (int) (img.getHeight(null) * scaleFactor);
		    
		    //Draw ONC image scaled to fit image clip region on the label
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    g2d.drawImage(img, x+AVERY_LABEL_IMAGE_X_OFFSET, y + AVERY_LABEL_IMAGE_Y_OFFSET,
		    				x+AVERY_LABEL_IMAGE_X_OFFSET+destX1, y+AVERY_LABEL_IMAGE_Y_OFFSET+destY1,
		    				0,0, img.getWidth(null),img.getHeight(null),null); 
	         
		    //Draw the ONC Season
		    g2d.setFont(lFont[1]);
		    g2d.drawString("ONC " + season, x + AVERY_LABEL_IMAGE_X_OFFSET-2, y+58+AVERY_LABEL_IMAGE_Y_OFFSET);
		    
		    //Draw ONC number
		    g2d.setFont(lFont[3]);
			g2d.drawString("Family # " + line[0], x, y+AVERY_LABEL_Y_OFFSET); 	//ONC Number
		    
		    //For each child, draw the child line
		    g2d.setFont(lFont[0]);
		    for(int i=1; i<line.length; i++)
		    	g2d.drawString(line[i], x+12, i*AVERY_LABEL_CHILD_ROW_HEIGHT + y+AVERY_LABEL_Y_OFFSET);
		}

		@Override
		public int print(Graphics g, PageFormat pf, int page) throws PrinterException
		{
			if (page > (sortTable.getSelectedRowCount()+1)/AVERY_LABELS_PER_PAGE)		//'page' is zero-based 
			{ 
				return NO_SUCH_PAGE;
		    }
			
			SimpleDateFormat sYear = new SimpleDateFormat("yy");
			SimpleDateFormat sSeason = new SimpleDateFormat("yyyy");
			
			int idx = Integer.parseInt(sYear.format(oncGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
			final Image img = oncGVs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();
		     
			Font[] lFont = new Font[4];
		    lFont[0] = new Font("Calibri", Font.ITALIC, 16);
		    lFont[1] = new Font("Calibri", Font.BOLD, 12);
		    lFont[2] = new Font("Calibri", Font.PLAIN, 10);
		    lFont[3] = new Font("Calibri", Font.BOLD, 24);	//ONC Number Text Font
		    
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
		    g2d.translate(AVERY_LABEL_X_COORDINATE_OFFSET, AVERY_LABEL_Y_COORDINATE_OFFSET);
		    
		    int row = 0, col = 0;
		    
		    while(row < AVERY_LABELS_PER_PAGE/AVERY_COLUMNS_PER_PAGE && index < endOfSelection)
		    {
		    	//Get a reference to the selected family
		    	ONCFamily f = stAL.get(row_sel[index]);
		    	
		    	//Create a string array, one element for each child in the family
//				int nChildren = f.getNumberOfChildren();
				int nChildren = cDB.getNumberOfChildrenInFamily(f.getID());
				String[] line = new String[nChildren+1];
				
				//Add ONC Number to line 0
				int linenum = 0;
				line[linenum++] = f.getONCNum();
				
				//Create a line for each child
//				ArrayList<ONCChild> cAL = f.getChildArrayList();
				ArrayList<ONCChild> cAL = cDB.getChildren(f.getID());
//				for(ONCChild c:cAL)
				for(int i=0; i< cAL.size(); i++)
					line[linenum++] = "Child " + Integer.toString(i+1) + ": " +
										cAL.get(i).getChildAge() +" " + 
											cAL.get(i).getChildGender().toLowerCase();
				
		    	printLabel(col * AVERY_LABEL_WIDTH + AVERY_SHEET_X_OFFSET,
		    				row * AVERY_LABEL_HEIGHT + AVERY_SHEET_Y_OFFSET,
		    				line, lFont, sSeason.format(oncGVs.getSeasonStartDate()), img, g2d);	
		    	
		    	if(++col == AVERY_COLUMNS_PER_PAGE) { row++; col = 0; }
		    	
		    	index++; //Increment the total number of book labels printed
		    }
		    	    
		     /* tell the caller that this page is part of the printed document */
		     return PAGE_EXISTS;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (evt.getPropertyName() == "progress")
		{
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
		else if(evt.getPropertyName() == "state")
		{
			if(evt.getNewValue() == SwingWorker.StateValue.DONE)
			{
				emailCB.setSelectedIndex(0);
				emailCB.setEnabled(true);
			}
		}		
		
	}

	
}