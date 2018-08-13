package ourneighborschild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ClientMapDialog extends JDialog implements DatabaseListener
{
	/**
	 * This class implements the ONC served family distribution maps. The
	 * maps are obtained from Google Maps using its API. 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String GOOGLE_STATIC_MAP_URL = "http://maps.googleapis.com/maps/api/staticmap?";
	private static final int MAP_ZOOM = 12;
	
	private static final String SCHOOL_MAP_CENTER = "38.886055,-77.435431";
	private static final int SCHOOL_MAP_WIDTH = 480;
	private static final int SCHOOL_MAP_HEIGHT = 640;
	
	private static final int REGION_MAP_WIDTH = 560;
	private static final int REGION_MAP_HEIGHT = 640;
	private static final String REGION_MAP_CENTER = "38.845114,-77.374760";
	
	private RegionDB regionDB;
	private JPanel schoolMapPanel, schoolDistPanel, regionMapPanel, regionDistPanel;
	private JLabel lblSchoolClientMap = null, lblRegionClientMap = null;
	private boolean bClientMapsInMemory;
	private ImageIcon iiSchoolClientMap, iiRegionClientMap;
	private List<ZoneCountLabel> schoolCountLabelList, regionCountLabelList;
	private JLabel lblRegionTotalServed, lblSchoolTotalServed;
	private int totalSchoolsServed, totalRegionsServed;
	
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
		
		regionDB = RegionDB.getInstance();
		if(regionDB != null)
			regionDB.addDatabaseListener(this);
		
		//Initialize instance variables
		bClientMapsInMemory = false;
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//Set up the school map and distribution panels and the labels that will hold 
		//the map and distribution counts
		JPanel schoolPanel = new JPanel();
		schoolPanel.setLayout(new BoxLayout(schoolPanel, BoxLayout.X_AXIS));
		schoolMapPanel = new JPanel();	
		schoolDistPanel = new JPanel();
		schoolDistPanel.setLayout(new BoxLayout(schoolDistPanel, BoxLayout.Y_AXIS));
		lblSchoolClientMap = new JLabel();
		iiSchoolClientMap = new ImageIcon();
		lblSchoolTotalServed = new JLabel("Total Served: 0");
		
		//create the distribution labels
		schoolCountLabelList = new ArrayList<ZoneCountLabel>();

		//Add the map panel and the distribution panel to the school panel
		schoolPanel.add(schoolMapPanel);
        schoolPanel.add(schoolDistPanel);
        
        //Set up the region map and region distribution panels and the labels that will hold 
        //the map and distribution counts
        JPanel regionPanel = new JPanel();
        regionPanel.setLayout(new BoxLayout(regionPanel, BoxLayout.X_AXIS));
        regionMapPanel = new JPanel();	
        regionDistPanel = new JPanel();
        regionDistPanel.setLayout(new BoxLayout(regionDistPanel, BoxLayout.Y_AXIS));
        lblRegionClientMap = new JLabel();
      	iiRegionClientMap = new ImageIcon();
      	lblRegionTotalServed = new JLabel("Total Served: 0");
      		
      	//create the distribution labels
      	regionCountLabelList = new ArrayList<ZoneCountLabel>();

      	//Add the map panel and the distribution panel to the school panel
      	regionPanel.add(regionMapPanel);
        regionPanel.add(regionDistPanel);
        
        //Add the region panel to the tabbed pane
        tabbedPane.addTab("Distribution By Elementary School", schoolPanel);
        tabbedPane.addTab("Distribution By Region", regionPanel);
        this.getContentPane().add(tabbedPane);
       
        pack();
        setSize(740,660);
        Point pt = parent.getLocation();
        setLocation(pt.x + 20, pt.y + 20);
	}
	
	void display()
	{		
		//Build # of families in each region
		buildRegionCounts(famDB.getList());
						
		//Build the map
		buildClientMaps();
	}
	
	void buildClientMaps()
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
		if(!bClientMapsInMemory)
		{
			BufferedImage schoolMap = getMap(SCHOOL_MAP_WIDTH, SCHOOL_MAP_HEIGHT, MAP_ZOOM, SCHOOL_MAP_CENTER, "School");
			if(schoolMap != null)	
			{
				iiSchoolClientMap.setImage(schoolMap);
				lblSchoolClientMap.setIcon(iiSchoolClientMap);
				schoolMapPanel.add(lblSchoolClientMap);
			}
			else
				JOptionPane.showMessageDialog(null, "Can't get School Map from Google, check your Internet Connection",
				  		"Google Maps Access Issue", JOptionPane.ERROR_MESSAGE);
			
			BufferedImage regionMap = getMap(REGION_MAP_WIDTH, REGION_MAP_HEIGHT, MAP_ZOOM, REGION_MAP_CENTER, "Region");
			if(regionMap != null)	
			{
				iiRegionClientMap.setImage(regionMap);
				lblRegionClientMap.setIcon(iiRegionClientMap);
				regionMapPanel.add(lblRegionClientMap);
				bClientMapsInMemory = true;
			}
			else
				JOptionPane.showMessageDialog(null, "Can't get Region Map from Google, check your Internet Connection",
				  		"Google Maps Access Issue", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	String getMarkers(String type)
	{
		GlobalVariablesDB gvs = GlobalVariablesDB.getInstance();
		StringBuffer markers = new StringBuffer("&markers=color:green%7C" + gvs.getWarehouseAddress());
		if(type.equals("School"))
			for(School sch : regionDB.getServedSchoolList(SchoolType.ES))
				markers.append("&markers=label:"+ sch.getCode()+"%7C" + sch.getLatLong());
		else
		{
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
//				"38.832765,-77.4619244",	    //Region X PIN 25
//				"38.839880,-77.405274"
//				"38.828027,-77.404695"		//Region Y PIN 26
//				"38.858007,-77.389963"		//Region Z PIN 27
				};
			
			for(int i=1; i<regionLatLong.length; i++)
				markers.append("&markers=label:"+ regionDB.getRegionID(i)+"%7C" + regionLatLong[i]);
		}
		
		return markers.toString();
	}
	
	BufferedImage getMap(int width, int height, int zoom, String center, String type)
	{
		String parms = String.format("&size=%dx%d&zoom=%d&center=%s", width, height, zoom, center);
		DrivingDirections ddir = new DrivingDirections();
		BufferedImage map = ddir.getGoogleMap(GOOGLE_STATIC_MAP_URL+parms+getMarkers(type)+"&key=" + EncryptionManager.getKey("key1"));
		
		return map;
	}
	
	void updateSchoolList()
	{
		schoolDistPanel.removeAll();
		schoolCountLabelList.clear();
		totalSchoolsServed = 0;
	
		schoolDistPanel.add(new JLabel("<html><center><b>Served Family<br><u>Distribution Legend:</u></b></html>"));

		for(School sch : regionDB.getServedSchoolList(SchoolType.ES))
			schoolCountLabelList.add(new ZoneCountLabel(0, sch.getName(), sch.getCode()));
		
		//add labels for not in pyramid and not in zipcode families
		schoolCountLabelList.add(new ZoneCountLabel(0, "Not in Pyramid", "Y"));
		schoolCountLabelList.add(new ZoneCountLabel(0, "Not in Serving Area", "Z"));
		
		//sort the regionCountLabelList by SchoolCode and add each label to the panel
		Collections.sort(schoolCountLabelList, new ZoneCountLabelListCodeSorter());
		for(ZoneCountLabel rcl : schoolCountLabelList)
			schoolDistPanel.add(rcl);
		
		schoolDistPanel.add(lblSchoolTotalServed);
	}
	
	void updateRegionList()
	{
		regionDistPanel.removeAll();
		regionCountLabelList.clear();
		totalRegionsServed = 0;
	
		regionDistPanel.add(new JLabel("<html><center><b>Served Family<br><u>Distribution Legend:</u></b></html>"));
		
		for(int i=0; i<regionDB.size(); i++)
		{	
			ZoneCountLabel rcl = new ZoneCountLabel(0, regionDB.getRegionID(i));
			regionCountLabelList.add(rcl);
			regionDistPanel.add(rcl);
		}
		
		regionDistPanel.add(lblRegionTotalServed);
	}
	
	void buildRegionCounts(ArrayList<ONCFamily> fAL)
	{
		//Clear current counts
		for(ZoneCountLabel rcl: schoolCountLabelList)
			rcl.setCount(0);
		totalSchoolsServed = 0;
		
		//Separate families by region, only count served families. Served families
		//have numeric ONC#'s and don't have a Do Not Serve code (empty)
		for(ONCFamily f:fAL)
		{
			//only count served families
			if(isNumeric(f.getONCNum()) && f.getDNSCode().isEmpty())
			{
				ZoneCountLabel associatedSchoolLbl = findLabelByZoneCode(schoolCountLabelList, f.getSchoolCode());
				if(associatedSchoolLbl != null)
				{
					associatedSchoolLbl.addToCount(1);
					totalSchoolsServed++;
				}
				
				ZoneCountLabel associatedRegionLbl = findLabelByZoneCode(regionCountLabelList, regionDB.getRegionID(f.getRegion()));
				if(associatedRegionLbl != null)
				{
					associatedRegionLbl.addToCount(1);
					totalRegionsServed++;
				}
				
				
			}
		}
		
		lblSchoolTotalServed.setText("Total Served: " + Integer.toString(totalSchoolsServed));
		lblRegionTotalServed.setText("Total Served: " + Integer.toString(totalRegionsServed));
	}
	
	public static boolean isNumeric(String str)
    {
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
	
	void updateSchoolZoneCounts(SchoolCodeChange scc)
	{
		if(scc.getDecCode() != null)	//null indicates an unchanged count
		{
			//school zone is losing a family. Find the label by code and subtract 1
			ZoneCountLabel decremetedLabel = findLabelByZoneCode(schoolCountLabelList, scc.getDecCode());
			if(decremetedLabel != null)
			{
				decremetedLabel.addToCount(-1);
				totalSchoolsServed--;
			}	
		}
		
		if(scc.getIncCode() != null)
		{
			//school zone is gaining a family. Find the label by school code and add 1
			ZoneCountLabel incrementedLabel = findLabelByZoneCode(schoolCountLabelList, scc.getIncCode());
			if(incrementedLabel != null)
			{
				incrementedLabel.addToCount(1);
				totalSchoolsServed++;
			}	
		}
		
		lblSchoolTotalServed.setText("Total Served: " + Integer.toString(totalSchoolsServed));
	}
	
	void updateRegionCounts(SchoolCodeChange scc)
	{
		if(scc.getDecCode() != null)	//null indicates an unchanged count
		{
			//region is losing a family. Find the label by code and subtract 1
			ZoneCountLabel decremetedLabel = findLabelByZoneCode(regionCountLabelList, scc.getDecCode());
			if(decremetedLabel != null)
			{
				decremetedLabel.addToCount(-1);
				totalRegionsServed--;
			}	
		}
		
		if(scc.getIncCode() != null)
		{
			//region is gaining a family. Find the label by school code and add 1
			ZoneCountLabel incrementedLabel = findLabelByZoneCode(regionCountLabelList, scc.getIncCode());
			if(incrementedLabel != null)
			{
				incrementedLabel.addToCount(1);
				totalRegionsServed++;
			}	
		}
		
		lblRegionTotalServed.setText("Total Served: " + Integer.toString(totalRegionsServed));
	}
	
	ZoneCountLabel findLabelByZoneCode(List<ZoneCountLabel> labelList, String zoneCode)
	{
		int index = 0;
		while(index < labelList.size() && !labelList.get(index).matches(zoneCode))
			index++;
				
		return index < labelList.size() ? labelList.get(index) : null; 
	}

	@Override
	public void dataChanged(DatabaseEvent dbe) 
	{
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_SCHOOL_CODE"))
		{
//			System.out.println(String.format("Client Map Dlg DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			SchoolCodeChange scc = (SchoolCodeChange) dbe.getObject1();
			updateSchoolZoneCounts(scc);	
		}
		if(dbe.getSource() != this && dbe.getType().equals("UPDATED_REGION_CODE"))
		{
//			System.out.println(String.format("Client Map Dlg DB Event: Source: %s, Type: %s, Object: %s",
//					dbe.getSource().toString(), dbe.getType(), dbe.getObject().toString()));
			
			SchoolCodeChange scc = (SchoolCodeChange) dbe.getObject1();
			updateRegionCounts(scc);	
		}
		else if(dbe.getType().equals("UPDATED_REGION_LIST"))
		{
//			System.out.println(String.format("ClientMapDialog dataChanged Updating Region List Event"));
//			String[] regList = (String[]) dbe.getObject();
			updateRegionList();
		}
		else if(dbe.getType().equals("UPDATED_GLOBALS") && this.isVisible())
		{
			schoolMapPanel.remove(lblSchoolClientMap);
			bClientMapsInMemory = false;
			buildClientMaps();
			this.getContentPane().repaint();
		}
		else if(dbe.getType().equals("LOADED_FAMILIES"))
		{
			this.setTitle(String.format("Distribution of %d ONC Families Served Gifts", GlobalVariablesDB.getCurrentSeason()));
		}
		else if(dbe.getType().equals("LOADED_SCHOOLS"))
		{
			//now that we have schools in the database, rebuild the region list
			updateSchoolList();
		}
	}
	
	private class ZoneCountLabel extends JLabel
	{
		/**
		 * extends JLabel to provide a label for each ONC zone displayed in
		 * the ClientMapDialog. Instance variables hold the object count in 
		 * each zone and the String name of the zone
		 */
		private static final long serialVersionUID = 1L;
		private int count;
		private String zoneName;
		private String zoneCode;
		
		ZoneCountLabel(int count, String code)
		{	
			super("Region " + code +": " + Integer.toString(count));
			this.count = count;
			this.zoneName = "";
			this.zoneCode = code;
		}
		
		ZoneCountLabel(int count, String name, String code)
		{	
			super(name +": " + Integer.toString(count));
			this.count = count;
			this.zoneName = name;
			this.zoneCode = code;
		}
	
		void setCount(int count) 
		{ 
			this.count = count;
			if(zoneName.isEmpty())
				setText(String.format("Region %s: %d", zoneCode, count));
			else
				setText(String.format("%s-%s: %d", zoneCode, zoneName, count));
		}
	
		void addToCount(int adder) 
		{
			count += adder;
			if(zoneName.isEmpty())
				setText(String.format("Region %s: %d", zoneCode, count));
			else
				setText(String.format("%s-%s: %d", zoneCode, zoneName, count));
		}
		
		String getCode() { return zoneCode; }
		boolean matches(String code) { return zoneCode.equals(code); }
	}
	
	private class ZoneCountLabelListCodeSorter implements Comparator<ZoneCountLabel>
	{
		public int compare(ZoneCountLabel o1, ZoneCountLabel o2)
		{
		
			return o1.getCode().compareTo(o2.getCode());
		}
	}
}
