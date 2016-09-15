package ourneighborschild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ClientMapDialog extends JDialog implements DatabaseListener
{
	/**
	 * This class implements the ONC served family distribution map. The
	 * map is obtained from Google Maps using its API. 
	 */
	private static final long serialVersionUID = 1L;
	private ONCRegions regions;
	private JPanel mapPanel, distPanel;
	private JLabel lblClientMap = null;
	private boolean bClientMapStored;
	private ImageIcon iiClientMap;
	private List<RegionCountLabel> regionCountLabelList;
	private JLabel lblLegend, lblTotalServed;
	private int totalServed;
	
	private FamilyDB famDB;
	
	public ClientMapDialog(JFrame parent)
	{
		super(parent);
		this.setTitle("Map of Served ONC Families by Region");
		
		GlobalVariables globalDB = GlobalVariables.getInstance();
		if(globalDB != null)
			globalDB.addDatabaseListener(this);	//listen for warehouse address change
			
		famDB = FamilyDB.getInstance();
		if(famDB != null)
			famDB.addDatabaseListener(this);	//listen for family changes
		
		regions = ONCRegions.getInstance();
		if(regions != null)
			regions.addDatabaseListener(this);
		
		//Initialize instance variables
		bClientMapStored = false;
		
		//Set up the map and distribution panels and the labels that will hold the map and distribution
		//counts
		mapPanel = new JPanel();	
		distPanel = new JPanel();
		distPanel.setLayout(new BoxLayout(distPanel, BoxLayout.Y_AXIS));
		
		lblClientMap = new JLabel();
		iiClientMap = new ImageIcon();
		
		//create the region distribution labels
		lblLegend = new JLabel("<html><center><b>Served Family<br><u>Distribution Legend:</u></b></html>");
		lblTotalServed = new JLabel("Total Served: 0");
		regionCountLabelList = new ArrayList<RegionCountLabel>();
		distPanel.add(lblTotalServed);

		//Add all panels to the dialog content pane
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(mapPanel);
        getContentPane().add(distPanel);
       
        pack();
        setSize(800,550);
        Point pt = parent.getLocation();
        setLocation(pt.x + 50, pt.y + 50);
	}
	
	void display()
	{		
		//Build # of families in each region-note: regionCount[0] is for unassigned served families		
		//If the family is served (no DNS Code) and the region contains a string that has
		//numeric values, parse the string to an integer and increment that regions count
		//else if the family is served and the region string isn't numeric, increment the unassigned count
		buildRegionCounts(famDB.getList());
						
		//Build the map
		buildClientMap();
	}
	
	void buildClientMap()
	{
		//Get the updated map from Google Maps if the map isn't stored
		if(!bClientMapStored)
		{
			GlobalVariables gvs = GlobalVariables.getInstance();
					
			URL mapURL = null;
			BufferedImage map = null;
			String url = "http://maps.googleapis.com/maps/api/staticmap?";
			String parms = "&size=640x525&zoom=12&center=38.84765,-77.40215";
			String marker = "&markers=color:green%7C" + gvs.getWarehouseAddress();
			StringBuffer markers = new StringBuffer(marker);
			String sensor = "&sensor=false";
					
			String[] regionAddresses = {"Unassigned, no Address",	//PLACE HOLDER FOR UNASSIGNED FAMILIES
					"Munsey+Pl+Centreville,VA",
					"Knoughton+Way+Centreville,VA",
					"Four+Chimney+Dr+Centreville,VA",
					"Lynhodge+Ct+Centreville,VA",
					"Gold+Post+Ct+Dr+Centreville,VA",
					"Ormond+Stone+Cir+Centreville,VA",
					"Rock+Landing+Ct+Centrevile,VA",
					"Battalion+St+Centrevile,VA",
					"Mossy+Bank+Ln+Centreville,VA",
					"Emerald+Green+Ct+Centreville,VA",
					"Rock+Hollow+Ln+Clifton,VA",
					"Water+St+Clifton,VA",
					"Sydney+Rd+Fairfax+Station,VA",
					"Donegal+Church+Ct+Chantilly,VA",
					"Northeast+Pl+Chantilly,VA",
					"Canoe+Birch+Ct+Fairfax,VA",
					"Holton+Pl+Chantilly,VA",
					"Marble+Rock+Ct+Chantilly,VA",
					"Ruben+Simpson+Ct+Fairfax,VA",
					"Maple+Hill+Rd+Fairfax,VA",
					"Field+Lark+Ln+Fairfax,VA",
					"Maepine+Ct+Fairfax,VA",
					"Fair+Valley+Ct+Fairfax,VA",
					"Edman+Cir+Centreville,VA"};
					
			for(int i=1; i<regionAddresses.length; i++)
					markers.append("&markers=label:"+ regions.getRegionID(i)+"%7C" + regionAddresses[i]);
					
			try
			{
				mapURL = new URL(url+parms+markers.toString()+sensor);
			}
			catch (MalformedURLException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		        
			try 
			{
				map = ImageIO.read(mapURL);
				if(map != null)	
				{
					iiClientMap.setImage(map);
					lblClientMap.setIcon(iiClientMap);
					mapPanel.add(lblClientMap);
					bClientMapStored = true;
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Can't get Map from Google, check your Internet Connection",
					  		"Google Maps Access Issue", JOptionPane.ERROR_MESSAGE);
				}
			} 
			catch (IOException e2)
			{
				JOptionPane.showMessageDialog(null, "Can't get Map from Google, check your Internet Connection",
			  		"Google Maps Access Issue", JOptionPane.ERROR_MESSAGE);
			}
		}	  	
	}
	
	void updateRegionList()
	{
		distPanel.removeAll();
		regionCountLabelList.clear();
		totalServed = 0;
	
		distPanel.add(lblLegend);
		
		for(int i=0; i<regions.size(); i++)
		{	
			RegionCountLabel rcl = new RegionCountLabel(0, regions.getRegionID(i));
			regionCountLabelList.add(rcl);
			distPanel.add(rcl);
		}
		
		distPanel.add(lblTotalServed);
	}
	
	void buildRegionCounts(ArrayList<ONCFamily> fAL)
	{
		//Clear current counts
		for(RegionCountLabel rcl: regionCountLabelList)
			rcl.setCount(0);
		totalServed = 0;
		
		//Separate families by region, only count served families. Served families
		//have numeric ONC#'s and don't have a Do Not Serve code (empty)
		for(ONCFamily f:fAL)
		{
			//only count served families
			if(isNumeric(f.getONCNum()) && f.getDNSCode().isEmpty())
			{
				regionCountLabelList.get(f.getRegion()).addToCount(1);
				totalServed++;
			}
		}
		
		lblTotalServed.setText("Total Served: " + Integer.toString(totalServed));
	}
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	/**************************************************************************
	 * For a region change, the DataChange object holds the integer region
	 * numbers of the regions being decremented (oldData instance variable) and 
	 * incremented (newData instance variable). If either variables equals =1
	 * only an decrement or increment is taking place, not both. 
	 * @param regionChange
	 ***********************************************************************/
	void updateRegionCounts(DataChange regionChange)
	{
		if(regionChange.getOldData() >= 0)	//-1 indicates an unchanged region
		{
			//region is losing an object, decrement count
			regionCountLabelList.get(regionChange.getOldData()).addToCount(-1);
			totalServed--;	
		}
		
		if(regionChange.getNewData() >= 0)
		{
			//region is gaining an object, increment count
			regionCountLabelList.get(regionChange.getNewData()).addToCount(1);
			totalServed++;
		}
		
		lblTotalServed.setText("Total Served: " + Integer.toString(totalServed));
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_REGIONS"))
		{
//			System.out.println(String.format("Client Map Dlg DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			DataChange regionChange = (DataChange) dbe.getObject();
			updateRegionCounts(regionChange);	
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
//			System.out.println(String.format("ClientMapDialog dataChanged Updating Region List Event"));
//			String[] regList = (String[]) dbe.getObject();
			updateRegionList();
		}
		else if(dbe.getType().equals("UPDATED_GLOBALS") && this.isVisible())
		{
			mapPanel.remove(lblClientMap);
			bClientMapStored = false;
			buildClientMap();
			this.getContentPane().repaint();
		}
	}
	
	private class RegionCountLabel extends JLabel
	{
		/**
		 * extends JLabel to provide a label for each ONC region displayed in
		 * the ClientMapDialog. Instance variables hold the object count in 
		 * each region and the String name of the region
		 */
		private static final long serialVersionUID = 1L;
		private int count;
		private String region;
		
		RegionCountLabel(int count, String region)
		{		
			super("Region " + region +": " + Integer.toString(count) + " families");
			this.count = count;
			this.region = region;
		}
	
		void setCount(int count) 
		{ 
			this.count = count;
			setText("Region " + region + ": " + Integer.toString(count) + " families");
		}
	
		void addToCount(int adder) 
		{
			count += adder;
			setText("Region " + region + ": " + Integer.toString(count) + " families");
		}
	}
}
