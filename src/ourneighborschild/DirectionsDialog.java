package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class DirectionsDialog extends JDialog implements ActionListener, DatabaseListener,
															EntitySelectionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;	
	private static final int MAX_DIRECTION_STEPS_ON_FIRST_PAGE = 16;
	private static final int MAX_DIRECTION_STEPS_ON_NEXT_PAGES = 30;
	
	private GlobalVariables ddGVs;
	private JLabel lblMap, lblHeader;
	private JPanel mapPanel;
	private JTable dirTable;
	private DefaultTableModel dirTableModel;
	private JButton btnPrint;
	private ONCFamily f;
	private FamilyDB fDB;
	private JSONArray steps;
	private JSONObject leg;
	private String destAddress;

	public DirectionsDialog(JFrame parent) throws JSONException								
	{
		super(parent);
		fDB = FamilyDB.getInstance();
		ddGVs = GlobalVariables.getInstance();
		if(ddGVs != null)
			ddGVs.addDatabaseListener(this);
		
		//Set up the header panel
		JPanel headerPanel = new JPanel();
		lblHeader = new JLabel();
		headerPanel.add(lblHeader);
		
		//Set up the directions table
		dirTable = new JTable()
		{
			private static final long serialVersionUID = 1L;

			//Implement table header tool tips.
			protected String[] columnToolTips = {"Step", "Driving Directions", "Distance", "Time"}; 
			
		    protected JTableHeader createDefaultTableHeader()
		    {
		        return new JTableHeader(columnModel)
		        {
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e)
		            {
		                java.awt.Point p = e.getPoint();
		                int index = columnModel.getColumnIndexAtX(p.x);
		                int realIndex = columnModel.getColumn(index).getModelIndex();
		                return columnToolTips[realIndex];
		            }
		        };
		    }
		    
			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col)
			{
			  Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			  		 
			  if(isRowSelected(Index_row))
				  comp.setBackground(comp.getBackground());
			  else if (Index_row % 2 == 1)			  
				  comp.setBackground(new Color(240,248,255));
			  else
				  comp.setBackground(Color.white);
			  
			  return comp;
			}
		};
		dirTable.setEnabled(false);
		
        dirTableModel = new DefaultTableModel(new Object[]{"#", "Directions", "Dist.", "Time"}, 0)
        {
                private static final long serialVersionUID = 1L;
                @Override
                	//No cells can be edited
                	public boolean isCellEditable(int row, int column) {return false;}
        }; 
        
        dirTable.setModel(dirTableModel);
        dirTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        dirTable.getColumnModel().getColumn(0).setPreferredWidth(24);
        dirTable.getColumnModel().getColumn(1).setPreferredWidth(422);
        dirTable.getColumnModel().getColumn(2).setPreferredWidth(72);
        dirTable.getColumnModel().getColumn(3).setPreferredWidth(72);
      
        JTableHeader anHeader = dirTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
        
        dirTable.setRowHeight(dirTable.getRowHeight() * 2);
        dirTable.setSize(dirTable.getRowHeight() * 4, dirTable.getWidth());
        dirTable.setFillsViewportHeight(true);
        
        //Create the scroll pane and add the table to it.
        JScrollPane dirScrollPane = new JScrollPane(dirTable);
        dirTable.setPreferredScrollableViewportSize(new Dimension(dirTable.getRowHeight() * 10, dirTable.getWidth()));
        dirScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
		
        //Set up the map panel and the Jlabel that will hold the map
		mapPanel = new JPanel();
		lblMap = new JLabel();
		mapPanel.add(lblMap);
		    			
        //Set up control panel for Print & Close buttons
        JPanel cntlPanel = new JPanel();
        btnPrint = new JButton("Print Map & Directions");
        btnPrint.addActionListener(this);         	              
        cntlPanel.add(btnPrint);
        
        //Add all panels to the dialog content pane
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(headerPanel);
        getContentPane().add(mapPanel);
        getContentPane().add(dirScrollPane);
        getContentPane().add(cntlPanel);
       
        pack();
        setSize(600,600);
        Point pt = parent.getLocation();
        setLocation(pt.x + 360, pt.y + 80);
	}
	
	void display(ONCFamily fam)
	{
		//Hold reference to ONCFamily object for responding to print
		f = fam;
		
		//Create driving direction object
		DrivingDirections ddir = new DrivingDirections();
		
		//UpdateDialog Title
		this.setTitle("Driving directions for ONC family #" + f.getONCNum());
		
		//Update the header displayed in the top panel. Determine if using alternate delivery address
		if(f.getSubstituteDeliveryAddress()!= null && !f.getSubstituteDeliveryAddress().isEmpty() &&
			f.getSubstituteDeliveryAddress().split("_").length == 5)
		{
			String[] addPart = f.getSubstituteDeliveryAddress().split("_");
//			String unit = addPart[2].equals("None") ? "" : addPart[2];
			lblHeader.setText("ONC Family #"+ f.getONCNum() +" address: " + addPart[0]+ " " + 
					addPart[1] + " " + addPart[3] + ", VA " + addPart[4]);	
		}
		else	//no alternate delivery address
			lblHeader.setText("ONC Family #"+ f.getONCNum() +" address: " + f.getHouseNum().trim() + " " + 
								f.getStreet().trim() + " " + f.getCity().trim() + ", VA " + f.getUnitNum());
						
		//Get family address and format it for the URL request to Google Maps
//		String dbdestAddress = f.getHouseNum().trim() + "+" + f.getStreet().trim() + 
//								"+" + f.getCity().trim() + ",VA";
//		dbdestAddress.replaceAll(" ", "+");
		destAddress = f.getGoogleMapAddress();
		
		//Get direction JSON
		JSONObject dirJSONObject;
		try 
		{
			dirJSONObject = ddir.getGoogleDirections(ddGVs.getWarehouseAddress(), destAddress);
			leg = ddir.getTripRoute(dirJSONObject);	//Get the trip duration and steps
			steps = ddir.getDrivingSteps(leg);	//String duration = ddir.getTripDuration(leg);
			
			//Parse JSON steps array to get data, add it to the directions table after 	  	
		  	//Clearing currently displayed directions in table
		  	for(int i = dirTableModel.getRowCount() - 1; i >=0; i--)		
		  		dirTableModel.removeRow(i);
		  	
		  	JSONObject step;   
		    for(int i=0; i<steps.length(); i++)
		    {
		    	step = steps.getJSONObject(i);
		    	dirTableModel.addRow(new Object[]{(String) Integer.toString(i+1)+".", "<html>" +
		    									(String) step.getString("html_instructions"),
		    									(String) step.getJSONObject("distance").getString("text"),
		    									(String) step.getJSONObject("duration").getString("text")});
		    }
		    
			//Remove the current map from the panel
		    mapPanel.remove(lblMap);
		    
		  //Get the updated map from Google Maps with start and end markers and path and add it to 
		    //the map panel
		    BufferedImage biMap = ddir.getDirectionsGoogleMap(leg, destAddress);
		    
		    if(biMap != null)
		    	lblMap.setIcon(new ImageIcon(biMap));
		    else
		    	lblMap.setIcon(ddGVs.getImageIcon(0));
		    
		    mapPanel.add(lblMap);
		    	    
		   //Mark the dialog for repainting since directions table and map have changed
		    mapPanel.validate();
		    mapPanel.repaint();
		  	dirTable.validate();
		} 
		catch (JSONException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		  	
	}
/*
	void OnPrintButtonClicked()
	{
		PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok)
        {
            try
            {
                 job.print();
                 
                 MessageFormat hf = new MessageFormat(lblHeader.getText());
                 dirTable.print(JTable.PrintMode.FIT_WIDTH, hf, null, false, null, false, job.getPrintService());
            }
            catch (PrinterException ex)
            {
             // The job did not successfully complete 
            }
        }
	}
*/	
	/*******************************************************************************************
	 * This method is called in response to a user delivery directions print request. The method 
	 * builds a page array list used by the Delivery Directions Printer  object to print each 
	 * page of the delivery directions print document. Each item in the array represents a page
	 * in the document. 
	 * @throws JSONException
	 ********************************************************************************************/
	void onPrintDeliveryDirections() throws JSONException
	{	
		//build the array list containing one page to print. Get the family object 
		//referenced and build the delivery directions page array list. This array list is
		//used by the print method to print. Family delivery directions may have two pages
		//if the directions contains more than MAX_DIRECTION_STEPS_ON_FIRST_PAGE steps
		ArrayList<DDPrintPage> ddpAL = new ArrayList<DDPrintPage>();	//The print array list
				
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
			
		//Create the print job. First, create the ONC season image and string. Then instantiate
		//a DeliveryDirectionsPrinter object. Then show the print dialog and execute printing
		SimpleDateFormat twodigitYear = new SimpleDateFormat("yy");
		int idx = Integer.parseInt(twodigitYear.format(ddGVs.getSeasonStartDate())) % NUM_OF_XMAS_ICONS;
		final Image img = ddGVs.getImageIcon(idx + XMAS_ICON_OFFSET).getImage();				
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
/*	
	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException 
	{
		if (page > 0)
		{ // We have only one page, and 'page' is zero-based 
            return NO_SUCH_PAGE;
        }
 
        // User (0,0) is typically outside the imageable area, so we must
        // translate by the X and Y values in the PageFormat to avoid clipping
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
 
//       this.getContentPane().printAll(g2d);
        // Now we perform our rendering 
        lblHeader.paint(g2d);
        lblMap.paint(g2d);
         
        
//       g.drawImage(iiMap.getImage(), 0, 20, iiMap.getImageObserver());
//      dirTable.print(JTable.PrintMode.FIT_WIDTH, null, null, false, null, false, null);
 
        //tell the caller that this page is part of the printed document
        return PAGE_EXISTS;
	}
*/
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnPrint) 
		{
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

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getType().equals("UPDATED_GLOBALS") && this.isVisible())
		{
			display(f);
		}	
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse)
	{
		if(this.isVisible() && (tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.WISH)) 
		{
			ONCFamily fam = (ONCFamily) tse.getObject1();
			if(fam != null)
				display(fam);
		}	
	}
	
	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.WISH);
	}
	
}

