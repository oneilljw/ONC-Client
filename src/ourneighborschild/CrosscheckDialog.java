package ourneighborschild;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import au.com.bytecode.opencsv.CSVReader;

public class CrosscheckDialog extends JDialog implements ActionListener, DatabaseListener,
															ListSelectionListener, EntitySelector 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int CROSSCHECK_FILE_RECORD_LENGTH = 19;
	
	private static final int CBO_COL= 0;
	private static final int ONC_NUM_COL = 1;
	private static final int FIRST_NAME_COL= 2;
	private static final int LAST_NAME_COL = 3;
	private static final int PHONE_COL = 4;
	private static final int ADDRESS_COL = 5;
	private static final int UNIT_COL = 6;
	private static final int CITY_COL = 7;
	private static final int ZIP_COL = 8;
	
	private JFrame owner;
	private ONCTable dlgTable;
	private AbstractTableModel dlgTableModel;
	private JButton btnImport, btnExport;
	private JLabel lblMatchFamilies;
	private FamilyDB fDB;
	
	private List<CrosscheckFamily> inputList;
	private List<CrosscheckFamily> matchList;
	
	public CrosscheckDialog(JFrame pf)
	{
		super(pf);
		this.owner = pf;
		this.setTitle("CBO Crosscheck");
		
		inputList = new ArrayList<CrosscheckFamily>();
		matchList = new ArrayList<CrosscheckFamily>();
		
		//Save the reference to the one wish catalog object in the app. It is created in the 
		//top level object and passed to all objects that require the wish catalog, including
		//this dialog
		fDB = FamilyDB.getInstance();
		if(fDB != null)
			fDB.addDatabaseListener(this);
		
		//Create the table model
		dlgTableModel = new DialogTableModel();
		
		//create the table
		String[] colToolTips = {"CBO", "ONC#", "First Name", "Last Name", "Phone", "Delivery Address",
								"Unit", "City", "Zip"};
		
		dlgTable = new ONCTable(dlgTableModel, colToolTips, new Color(240,248,255));

		dlgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dlgTable.getSelectionModel().addListSelectionListener(this);
		
		//Set table column widths
		int tablewidth = 0;
		int[] colWidths = {40, 40, 96, 96, 80, 224, 56, 80, 40};
		for(int col=0; col < colWidths.length; col++)
		{
			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(colWidths[col]);
			tablewidth += colWidths[col];
		}
		tablewidth += 24; 	//count for vertical scroll bar
		
        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
        
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        //left justify wish count column
//        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
//        dtcr.setHorizontalAlignment(SwingConstants.LEFT);
//        dlgTable.getColumnModel().getColumn(LOGINS_COL).setCellRenderer(dtcr);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new BorderLayout());
        
        JPanel leftCntlPanel = new JPanel();
        lblMatchFamilies = new JLabel("# of matching families: 0");
        leftCntlPanel.add(lblMatchFamilies);
        
        JPanel rightCntlPanel = new JPanel();
        btnImport = new JButton("Import");
        btnImport.setToolTipText("Import crosscheck file to compare");
        btnImport.addActionListener(this);
        rightCntlPanel.add(btnImport);
        
        btnExport = new JButton("Export");
        btnExport.setToolTipText("Export a crosscheck file");
        btnExport.addActionListener(this);
        rightCntlPanel.add(btnExport);
        
        cntlPanel.add(leftCntlPanel, BorderLayout.WEST);
        cntlPanel.add(rightCntlPanel, BorderLayout.EAST);
       
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(dsScrollPane);
        getContentPane().add(cntlPanel);
        
        pack();
        this.setMinimumSize(new Dimension(tablewidth, 240));
	}
	
	void buildCrosscheckList()
	{
		matchList.clear();
		
		for(CrosscheckFamily checkFamily : inputList)
		{
			int index = 0;
			String matchingONCNum = "None";
			while(index < fDB.getList().size() && 
					(matchingONCNum = checkFamily.getMatch(fDB.getObjectAtIndex(index))).equals("None"))
			{
				index++;	
			}
			
			if(index < fDB.getList().size())
			{
				checkFamily.setONCNum(matchingONCNum);
				matchList.add(checkFamily);
			}
		}
		
		dlgTableModel.fireTableDataChanged();
		lblMatchFamilies.setText(String.format("# of matching families: %d", matchList.size()));
	}
	
	String getInputCrosscheckFile()
	{
		ONCFileChooser oncFC = new ONCFileChooser(owner);
    	File ccfile= oncFC.getFile("Select Crosscheck .csv file to import", 
    									new FileNameExtensionFilter("CSV Files", "csv"), 0);
    	String filename = "";
    	
    	if( ccfile!= null)
    	{
	    	filename = ccfile.getName();
	    	
	    	//If user selected OK in batch dialog, then proceed with the import
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(ccfile.getAbsoluteFile()));
	    		String[] nextLine;
	    		String[] header;
	    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read cross check File line by line
	    			if(header.length == CROSSCHECK_FILE_RECORD_LENGTH)
	    				while ((nextLine = reader.readNext()) != null)	// nextLine[] is an array of values from the line
	    					inputList.add(new CrosscheckFamily(nextLine));
	    			else
	    				JOptionPane.showMessageDialog(owner, 
	    						ccfile.getName() + " is not in correct format, cannot be imported", 
	    						"Invalid Crosscheck Format", JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo()); 			    			
	    		}
	    		
	    		reader.close();
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IOException: %s%n", x);
	    	}
    	}
    	
    	return filename;
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnImport)
		{
			//get the input cross check file
			getInputCrosscheckFile();
			
			//perform the cross check
			buildCrosscheckList();
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventSelectorEntityTypes() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEntitySelectionListener(EntityType type, EntitySelectionListener l)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void removeEntitySelectionListener(EntityType type, EntitySelectionListener l)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2, Object obj3) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) {
		// TODO Auto-generated method stub
		
	}
	
	class DialogTableModel extends AbstractTableModel
	{
        /**
		 * Implements the table model for the Wish Catalog Dialog
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = {"CBO", "ONC #", "First Name", "Last Name", "Phone", "Delivery Address",
										"Unit", "City", "Zip"};
 
        public int getColumnCount() { return columnNames.length; }
 
        public int getRowCount() { return matchList.size(); }
 
        public String getColumnName(int col) { return columnNames[col]; }
 
        public Object getValueAt(int row, int col)
        {
        	CrosscheckFamily matchFam = matchList.get(row);
        	
        	if(col == CBO_COL)
        		return matchFam.getCBO();
        	else if(col == ONC_NUM_COL)
        		return matchFam.getONCNum();
        	else if(col == FIRST_NAME_COL)  
        		return matchFam.getHOHFN();
        	else if(col == LAST_NAME_COL)
        		return matchFam.getHOHLN();
        	else if (col == PHONE_COL)
        		return matchFam.getPhone();
        	else if (col == ADDRESS_COL)
        		return matchFam.getAddress();
        	else if (col == UNIT_COL)
        		return matchFam.getUnit();
        	else if (col == CITY_COL)
        		return matchFam.getCity();
        	else if (col == ZIP_COL)
        		return matchFam.getZip();
        	else
        		return "Error";
        }
        
        //JTable uses this method to determine the default renderer/editor for each cell.
        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }
 
        public boolean isCellEditable(int row, int col)
        {
        	return false;
        }
    }
	
	private class CrosscheckFamily
	{
		private String cbo;
		private String oncNum;
		private String hohFN;
		private String hohLN;
		private String phone;
		private String delAddress;
		private String unit;
		private String city;
		private String zip;
		private String state;
		private String email;
		private boolean bThanksgiving;
		private boolean bDecember;
		private boolean bGifts;
		private int nAdults;
		private int nChildren;
		private int nTotalMembers;
		private boolean bSpeaksEnglish;
		private String language;
		private String remarks;
	
		CrosscheckFamily(String[] line)
		{
			this.cbo = line[0].trim();
			this.hohFN = line[1].trim();
			this.hohLN = line[2].trim();
			this.phone = line[3].trim();
			this.delAddress = line[4].trim();
			this.unit = line[5].trim();
			this.city = line[6].trim();
			this.zip = line[7].trim();
			this.state = line[8].trim();
			this.email = line[9].trim();
			this.bThanksgiving = line[10].equalsIgnoreCase("Y");
			this.bDecember = line[11].equalsIgnoreCase("Y");
			this.bGifts = line[12].equalsIgnoreCase("Y");
			this.nAdults = line[13].isEmpty() ? 0 : Integer.parseInt(line[13]);
			this.nChildren = line[14].isEmpty() ? 0 : Integer.parseInt(line[14]);
			this.nTotalMembers = line[15].isEmpty() ? 0 : Integer.parseInt(line[15]);
			this.bSpeaksEnglish = line[16].equalsIgnoreCase("Y");
			this.language = line[17].trim();
			this.remarks = line[18].trim();
			
			oncNum = "None";
		}
		
		//getters
		private String getCBO() { return cbo; }
		private String getONCNum() { return oncNum; }
		private String getHOHFN() { return hohFN; }
		private String getHOHLN() { return hohLN; }
		private String getPhone() { return phone; }
		private String getAddress() { return delAddress; }
		private String getUnit() { return unit; }
		private String getCity() { return city; }
		private String getZip() { return zip; }
		
		//setters
		private void setONCNum(String s) { this.oncNum = s; }
		
		String getMatch(ONCFamily f)
		{
			String lastname = "";
			if(hohLN.contains("Household"))
			{
				String[] parts = hohLN.split(" ");
				
				for(int i=0; i<parts.length-1; i++)
					lastname = lastname.concat(parts[i] + " ");	
				
				lastname.trim();
			}
			else
				lastname = hohLN.trim();
			
			String matchAddress = f.getHouseNum() + " " + f.getStreet();
			
			//DEBUG *************************************************
			if(f.getHOHLastName().equalsIgnoreCase("Abdelshayed") && hohLN.equals("Abdelshayed Household"))
				System.out.println(String.format("Crosscheck Dlg: ONC LN: %s, hohLN: %s, modified LN: %s", f.getHOHLastName(), hohLN, lastname));
			
			
			if(f.getHOHLastName().equalsIgnoreCase(lastname) && matchAddress.equalsIgnoreCase(delAddress) ||
					!phone.isEmpty() && f.getHomePhone().contains(phone))
			{
				System.out.println(String.format("CrosscheckDlg.getMatch.match: FN: %s,  LN: %s", hohFN, lastname));
				return f.getONCNum();
			}
			else
				return "None";
		}
		
		String[] getExportRow()
		{
			List<String> rowList = new ArrayList<String>();
			rowList.add(cbo);
			rowList.add(hohFN);		
			rowList.add(hohLN);
			rowList.add(phone);
			rowList.add(delAddress);	
			rowList.add(unit);
			rowList.add(city);
			rowList.add(zip);
			rowList.add(state);
			rowList.add(email);			
			rowList.add(bThanksgiving ? "Y" : "N");
			rowList.add(bDecember ? "Y" : "N");
			rowList.add(bGifts ? "Y" : "N");
			rowList.add(Integer.toString(nAdults));
			rowList.add(Integer.toString(nChildren));
			rowList.add(Integer.toString(nTotalMembers));
			rowList.add(bSpeaksEnglish ? "Y" : "N");
			rowList.add(language);
			rowList.add(remarks);
			
			return rowList.toArray(new String[rowList.size()]);
		}		
	}
}
