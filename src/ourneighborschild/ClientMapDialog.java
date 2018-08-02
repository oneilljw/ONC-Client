package ourneighborschild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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
	private RegionDB regions;
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
		this.setTitle("Distribution of ONC Families Served Gifts by Region");
		
		GlobalVariablesDB globalDB = GlobalVariablesDB.getInstance();
		if(globalDB != null)
			globalDB.addDatabaseListener(this);	//listen for warehouse address change
			
		famDB = FamilyDB.getInstance();
		if(famDB != null)
			famDB.addDatabaseListener(this);	//listen for family changes
		
		regions = RegionDB.getInstance();
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
        setSize(800,640);
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
/*		
		String[] regionAddresses = {"Unassigned, no Address",	//PLACE HOLDER FOR UNASSIGNED FAMILIES
				"Munsey+Pl,+Centreville,+VA",
				"Knoughton+Way,+Centreville,+VA",
				"Four+Chimney+Dr,+Centreville,+VA",
				"Lynhodge+Ct,+Centreville,+VA",
				"Gold+Post+Ct,+Dr+Centreville,+VA",
				"Ormond+Stone+Cir,+Centreville,+VA",
				"Rock+Landing+Ct,+Centrevile,+VA",
				"Battalion+St,+Centrevile,+VA",
				"Mossy+Bank+Ln,+Centreville,+VA",
				"Emerald+Green+Ct,+Centreville,+VA",
				"Rock+Hollow+Ln,+Clifton,+VA",
				"Water+St,+Clifton,+VA",
				"Sydney+Rd,+Fairfax+Station,+VA",
				"Donegal+Church+Ct,+Chantilly,+VA",
				"Northeast+Pl,+Chantilly,+VA",
				"Canoe+Birch+Ct,+Fairfax,+VA",
				"Holton+Pl,+Chantilly,+VA",
				"Marble+Rock+Ct,+Chantilly,+VA",
				"Ruben+Simpson+Ct,+Fairfax,+VA",
				"Maple+Hill+Rd,+Fairfax,+VA",
				"Field+Lark+Ln,+Fairfax,+VA",
				"Maepine+Ct,+Fairfax,+VA",
				"Fair+Valley+Ct,+Fairfax,+VA",
				"Edman+Cir,+Centreville,+VA"
				};
*/				
				
		if(!bClientMapStored)
		{
			String url = "http://maps.googleapis.com/maps/api/staticmap?";
			String parms = "&size=640x600&zoom=12&center=38.84765,-77.40215";
			GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
			String marker = "&markers=color:green%7C" + gvs.getWarehouseAddress();
			StringBuffer markers = new StringBuffer(marker);		
			
			String[] regionLatLong = 
					{
					"Unassigned, no Address",	//PLACE HOLDER FOR UNASSIGNED FAMILIES
					"38.8605972,-77.4596217",	//Region A PIN 1
					"38.8592872,-77.4474546",	//Region B PIN 2
					"38.8465375,-77.4492307",	//Region C PIN 3
					"38.8451097,-77.4607807",	//Region D PIN 4
					"38.844207,-77.477659",		//Region E PIN 5
					"38.8438858,-77.4188626",	//Region F PIN 6
					"38.8332837,-77.4274877",	//Region G PIN 7
					"38.8298497,-77.4462841",	//Region H PIN 8
					"38.8192277,-77.4528976",	//Region I PIN 9
					"38.820919,-77.433434",		//Region J PIN 10
					"38.8167603,-77.4226005",	//Region K PIN 11
					"38.7797701,-77.384582",		//Region L PIN 12
					"38.7817242,-77.3223597",	//Region M PIN 13
					"38.8969661,-77.4755806",	//Region N PIN 14
					"38.8978402,-77.4536521",	//Region O PIN 15
					"38.8958435,-77.4164022",	//Region P PIN 16
					"38.8830304,-77.4255473",	//Region Q PIN 17
					"38.8624991,-77.4148267",	//Region R PIN 18
					"38.881464,-77.3812288",		//Region S PIN 19
					"38.8699022,-77.3515107",	//Region T PIN 20
					"38.8671858,-77.374384",		//Region U PIN 21
					"38.8734542,-77.3969387",	//Region V PIN 22
					"38.862498,-77.3935485",		//Region W PIN 23
					"38.832765,-77.459801"		//Region X PIN 24
//					"38.832765,-77.4619244",	    //Region X PIN 25
//					"38.839880,-77.405274"
//					"38.828027,-77.404695"		//Region Y PIN 26
//					"38.858007,-77.389963"		//Region Z PIN 27
					};
			
			for(int i=1; i<regionLatLong.length; i++)
				markers.append("&markers=label:"+ regions.getRegionID(i)+"%7C" + regionLatLong[i]);
			
			DrivingDirections ddir = new DrivingDirections();
			String completeURL = url+parms+markers.toString()+"&key=" + EncryptionManager.getKey("key1");
			System.out.println("ClientMapDlg.buildClientMap: completeURL= " + completeURL);
			BufferedImage map = ddir.getGoogleMap((url+parms+markers.toString()+"&key=" + EncryptionManager.getKey("key1")));
			
			if(map != null)	
			{
				iiClientMap.setImage(map);
				lblClientMap.setIcon(iiClientMap);
				mapPanel.add(lblClientMap);
				bClientMapStored = true;
			}
			else
				JOptionPane.showMessageDialog(null, "Can't get Map from Google, check your Internet Connection",
				  		"Google Maps Access Issue", JOptionPane.ERROR_MESSAGE);
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
			
			DataChange regionChange = (DataChange) dbe.getObject1();
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
		else if(dbe.getType().equals("LOADED_FAMILIES"))
		{
			this.setTitle(String.format("Distribution of %d ONC Families Served Gifts by Region", GlobalVariablesDB.getCurrentSeason()));
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
