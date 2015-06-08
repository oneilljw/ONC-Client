package ourneighborschild;

import java.awt.Image;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class GlobalVariables extends ONCDatabase implements Serializable
{
	/**
	 * This class holds global variables common to all classes in the ONC program
	 */
	private static final long serialVersionUID = -7710761545913066682L;
	private static final int NUMBER_OF_WISHES_PER_CHILD = 3;
	private static final int GV_CSV_HEADER_LENGTH = 3;
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;
	
	private static GlobalVariables instance = null;
	
	private static JFrame oncFrame;
	private transient Calendar oncDateToday;
	private Calendar oncDeliveryDate;
	private static Calendar oncSeasonStartDate;
	private Calendar oncGiftsReceivedDate;
	private String warehouseAddress;
	private transient String[] fontSizes = {"8", "10", "12", "13", "14", "16", "18"};
	private transient UserPermission user_permission;
	private transient String[] sGrwth_pcts = {"5%", "10%", "15%", "20%", "25%"};
	private transient int[] nGrwth_pcts = {5,10,15,20,25};
	private transient int fontIndex, startONCNum, ytyGrwthIndex;
	private static ImageIcon imageIcons[];
	private transient ONCUser user;
	private static String version;
	
	public static GlobalVariables getInstance()
	{
		if(instance == null)
			instance = new GlobalVariables();
		
		return instance;
	}
	
	private GlobalVariables()
	{	
		//call superclass constructor
		super();
		
		//Initialize class variables
		oncDateToday = Calendar.getInstance();
	    
	    oncDeliveryDate = Calendar.getInstance();
	    oncDeliveryDate.set(Calendar.HOUR_OF_DAY, 0);
	    oncDeliveryDate.set(Calendar.MINUTE, 0);
	    oncDeliveryDate.set(Calendar.SECOND, 0);
	    oncDeliveryDate.set(Calendar.MILLISECOND, 0);
	    
	    oncGiftsReceivedDate = Calendar.getInstance();
	    oncGiftsReceivedDate.set(Calendar.HOUR_OF_DAY, 0);
	    oncGiftsReceivedDate.set(Calendar.MINUTE, 0);
	    oncGiftsReceivedDate.set(Calendar.SECOND, 0);
	    oncGiftsReceivedDate.set(Calendar.MILLISECOND, 0);
	    
	    oncSeasonStartDate = Calendar.getInstance();
	    oncSeasonStartDate.set(Calendar.HOUR_OF_DAY, 0);
	    oncSeasonStartDate.set(Calendar.MINUTE, 0);
	    oncSeasonStartDate.set(Calendar.SECOND, 0);
	    oncSeasonStartDate.set(Calendar.MILLISECOND, 0);
	   
	    imageIcons = new ImageIcon[28];
		imageIcons[0] = createImageIcon("onclogosmall.gif", "ONC Logo");
		imageIcons[1] = createImageIcon("InfoIcon.gif", "Info Icon");
		imageIcons[2] = createImageIcon("Button-Next-icon.gif", "Next Icon");
		imageIcons[3] = createImageIcon("Button-Previous-icon.gif", "Previous Icon");
		imageIcons[4] = createImageIcon("GiftIcon.gif", "Gift Icon");	//Child wish history icon
		
		//Icons used for stop light status
		imageIcons[5] = createImageIcon("traffic-lights-green-icon.gif", "Green Light Icon");
		imageIcons[6] = createImageIcon("traffic-lights-yellow-icon.gif", "Yellow Light Icon");
		imageIcons[7] = createImageIcon("traffic-lights-red-icon.gif", "Red Light Icon");
		imageIcons[8] = createImageIcon("traffic-lights-off-icon.gif", "Off Light Icon");
		
		//Icons used on a 5-year cycle to visually indicate which year
		imageIcons[9] = createImageIcon("Christmas-tree-icon.gif", "Christmas Tree Icon");
		imageIcons[10] = createImageIcon("Christmas-Mistletoe-icon.gif", "Mistletoe Icon");
		imageIcons[11] = createImageIcon("ONCLabelIcon2012.gif", "Snowman Icon");
		imageIcons[12] = createImageIcon("Santa-icon.gif", "Santa Icon");
		imageIcons[13] = createImageIcon("Stocking-icon.gif", "Stocking Icon");
		
		imageIcons[14] = createImageIcon("Sleigh-icon.gif", "Sleigh Icon");	//Delivery history icon
		imageIcons[15] = createImageIcon("oncsplash.gif", "ONC Full Screen Logo");
		imageIcons[16] = createImageIcon("undo.gif", "ONC Undo Icon");
		imageIcons[17] = createImageIcon("lock_open.gif", "Open Lock");
		imageIcons[18] = createImageIcon("lock_locked.gif", "Closed Lock");
		
		imageIcons[19] = createImageIcon("address-icon-none.png", "No Alt Address Icon");
		imageIcons[20] = createImageIcon("address-icon-one.png", "Alt Address Icon");
		
		imageIcons[21] = createImageIcon("check.png", "No Alt Address Icon");
		imageIcons[22] = createImageIcon("bullet_cross.png", "Alt Address Icon");
		
		imageIcons[23] = createImageIcon("Button-Blank-Green-icon.png", "Green Light");
		imageIcons[24] = createImageIcon("Button-Blank-Yellow-icon.png", "Yellow Light");
		imageIcons[25] = createImageIcon("Button-Blank-Red-icon.png", "Red Light");
		imageIcons[26] = createImageIcon("Button-Blank-Gray-icon.png", "Gray Light");
		imageIcons[27] = createImageIcon("Button-Blank-Any-icon.png", "Any Light");
	
		warehouseAddress = "6476+Trillium+House+Lane+Centreville,VA";
		fontIndex = 3;
		startONCNum = 100;
		ytyGrwthIndex = 2;
		user_permission = UserPermission.GENERAL;
		version = "N/A";
	}
	
	//Getters
	static JFrame getFrame() { return oncFrame; }
	Date getTodaysDate() { return Calendar.getInstance().getTime(); }
	
	Date getTomorrowsDate()
	{
		Calendar tomorrow = Calendar.getInstance();
		
		if(user_permission.compareTo(UserPermission.ADMIN) > 0) 	//If superuser, return today set in preference dialog		
			tomorrow = oncDateToday;
		
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
	    tomorrow.set(Calendar.MINUTE, 0);
	    tomorrow.set(Calendar.SECOND, 0);
	   	tomorrow.set(Calendar.MILLISECOND, 0);
		return tomorrow.getTime();
	}
	
	static int getCurrentSeason() { return oncSeasonStartDate.get(Calendar.YEAR); }
	int getCurrentYear() { return oncDateToday.get(Calendar.YEAR); }
	public Date getDeliveryDate() { return oncDeliveryDate.getTime(); }
	public Date getGiftsReceivedDate() { return oncGiftsReceivedDate.getTime(); }
	public Date getSeasonStartDate() { return oncSeasonStartDate.getTime(); }
	public String getWarehouseAddress() { return warehouseAddress; }
	int getStartONCNum() { return startONCNum; }
	int getFontSize() { return Integer.parseInt(fontSizes[fontIndex]); }
	public int getFontIndex() { return fontIndex; }
	int getYTYGrwthIndex() { return ytyGrwthIndex; }
	int getYTYGrwthPct() {return nGrwth_pcts[ytyGrwthIndex]; }
	String[] getFontSizes() { return fontSizes; }
	String[] getGrwthPcts() { return sGrwth_pcts; }
	ONCUser getUser() {return user; }
	String getUserLNFI()
	{
		if(user.getFirstname().isEmpty())
			return user.getLastname();
		else
			return user.getLastname() + ", " + user.getFirstname().charAt(0);
	}
	String getUserFNLI()
	{
		if(user.getFirstname().isEmpty())
			return user.getLastname();
		else
			return user.getFirstname() + " " + user.getLastname().charAt(0);
	}
	boolean isUserAdmin() {return user_permission.compareTo(UserPermission.ADMIN) >= 0; }
	boolean isUserSuperUser() {return user_permission == UserPermission.SYS_ADMIN; }
	public ImageIcon getImageIcon(int icon) {return imageIcons[icon]; }
	public static ImageIcon getONCLogo() { return imageIcons[0]; }
	public static ImageIcon getUnLockedIcon() { return imageIcons[17]; }
	public static ImageIcon getLockedIcon() { return imageIcons[18]; }
	public static ImageIcon[] getLights()
	{
		ImageIcon[] lights = {imageIcons[27], imageIcons[23], imageIcons[24],
				imageIcons[25], imageIcons[26]};
		
		return lights;
	}
	ImageIcon[] getImageIcons() {return imageIcons; }
	ImageIcon getONCFullScreenLogo() {return imageIcons[15]; }
	Image getImage(int icon) { return imageIcons[icon].getImage(); }
	int getNumberOfWishesPerChild() { return NUMBER_OF_WISHES_PER_CHILD; }
	static String getVersion() { return version; }
	public static ImageIcon getSeasonIcon()
	{
		return imageIcons[oncSeasonStartDate.get(Calendar.YEAR) % NUM_OF_XMAS_ICONS + XMAS_ICON_OFFSET];
	}
	
	//setters globally used - need to update at the server and broadcast
	public void setDeliveryDate(Date dd) { oncDeliveryDate.setTime(dd); }
	public void setGiftsReceivedDate(Date grd) { oncGiftsReceivedDate.setTime(grd); }
	public void setSeasonStartDate(Date ssd) { oncSeasonStartDate.setTime(ssd); }
	public void setWarehouseAddress(String address) {warehouseAddress = address; }
	
	//Setters locally used
	void setFrame(JFrame frame) { oncFrame = frame; }
	void setTodaysDate(Date today) { oncDateToday.setTime(today);}
	public void setFontIndex(int fs) { fontIndex = fs; }
	void setImageIcons(ImageIcon[] ii) {imageIcons = ii; }
	ONCUser setUser(ONCUser u) { user = u; return user; }
	void setUserPermission(UserPermission p) { user_permission = p; }
	void setYTYGrowthIndex(int index) { ytyGrwthIndex = index; }
	void setStartONCNum(int startoncnum) { startONCNum = startoncnum; }
	void setVersion(String version) { GlobalVariables.version = version; }
	
	 /** Returns an ImageIcon, or null if the path was invalid. */
	ImageIcon createImageIcon(String path, String description)
	{
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) { return new ImageIcon(imgURL, description); } 
		else { System.err.println("Couldn't find file: " + path); return null; }
	}
	
	String importGlobalVariableDatabase()
	{
		ServerGVs gvs = null;
		String response = "NO_GLOBALS";
		ServerIF serverIF = ServerIF.getInstance();
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("GET<globals>");
			if(response.startsWith("GLOBALS"))
				gvs = gson.fromJson(response.substring(7), ServerGVs.class);				

			if(gvs != null && !response.startsWith("NO_GLOBALS"))
			{
				oncDeliveryDate.setTime(gvs.getDeliveryDate());
				oncGiftsReceivedDate.setTime(gvs.getGiftsReceivedDate());
				oncSeasonStartDate.setTime(gvs.getSeasonStartDate());
				warehouseAddress = gvs.getWarehouseAddress();
				
				response = "GLOBALS_LOADED";
			}
		}
		
		return response;
	}
	
	String[] importGlobalVariables(JFrame pf, ImageIcon oncIcon, String path)	//Only used by superuser to import from .csv file
	{
		String[] nextLine = null;	
		File pyfile;
		JFileChooser chooser;
		String filename = "";
		int returnVal = JFileChooser.CANCEL_OPTION;
		
		if(path != null)
		{
			pyfile = new File(path + "GlobalVariables.csv");
			returnVal = JFileChooser.APPROVE_OPTION;
		}
		else
		{
    		chooser = new JFileChooser();
    		chooser.setDialogTitle("Select Delivery DB .csv file to import");	
    		chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
    		returnVal = chooser.showOpenDialog(pf);
    		pyfile = chooser.getSelectedFile();
		}
		
	    if(returnVal == JFileChooser.APPROVE_OPTION)
	    {	    
	    	filename = pyfile.getName();
	    	try 
	    	{
	    		CSVReader reader = new CSVReader(new FileReader(pyfile.getAbsoluteFile()));
	    		String[] header;
    		
	    		if((header = reader.readNext()) != null)
	    		{
	    			//Read the ONC CSV File
	    			if(header.length == GV_CSV_HEADER_LENGTH || header.length == 24)
	    			{
	    				if((nextLine = reader.readNext()) != null)
	    				{
	    					//Read first line, it's the gv's
	    					oncDeliveryDate.setTimeInMillis(Long.parseLong(nextLine[0]));
	    					oncSeasonStartDate.setTimeInMillis(Long.parseLong(nextLine[1]));
	    					warehouseAddress = nextLine[2].isEmpty() ? "6476+Trillium+House+Lane+Centreville,VA" : nextLine[2];
	    					oncGiftsReceivedDate.setTimeInMillis(Long.parseLong(nextLine[3]));
	    					
	    					//Read the second line, it's the oncnumRegionRanges
	    					nextLine = reader.readNext();	
	    				}
	    				else
	    					JOptionPane.showMessageDialog(pf, "Global Variable file corrupted, no data", 
	        						"Invalid Global Variable File", JOptionPane.ERROR_MESSAGE, oncIcon); 	
	    			}
	    			else
	    			{
	    				for(int i=0; i<header.length; i++)
	    					System.out.print(header[i] + ", ");
	    				
	    				JOptionPane.showMessageDialog(pf, "Global Variable file corrupted, header length = " + Integer.toString(header.length), 
    						"Invalid Global Variable File", JOptionPane.ERROR_MESSAGE, oncIcon);
	    			}
	    		}
	    		else
	    			JOptionPane.showMessageDialog(pf, "Couldn't read header in Global Variable file: " + filename, 
						"Invalid Global Variable File", JOptionPane.ERROR_MESSAGE, oncIcon); 
	    	} 
	    	catch (IOException x)
	    	{
	    		JOptionPane.showMessageDialog(pf, "Unable to open Global Variable file: " + filename, 
    				"Global Variable file not found", JOptionPane.ERROR_MESSAGE, oncIcon);
	    	}
	    }
	    
	    return nextLine;    
	}
	
	String exportGlobalVariablesToCSV(JFrame pf, String filename, String[] oncnumRanges)
    {
		File oncwritefile = null;
		
    	if(filename == null)
    	{
    		ONCFileChooser fc = new ONCFileChooser(pf);
    		oncwritefile = fc.getFile("Select .csv file to save Global Variables to",
										new FileNameExtensionFilter("CSV Files", "csv"), 1);
    	}
    	else
    		oncwritefile = new File(filename);
    	
    	if(oncwritefile!= null)
    	{
    		//If user types a new filename and doesn't include the .csv, add it
	    	String filePath = oncwritefile.getPath();		
	    	if(!filePath.toLowerCase().endsWith(".csv")) 
	    		oncwritefile = new File(filePath + ".csv");
	    	
	    	try 
	    	{
	    		 String[] header = {"Delivery Date", "Season Start Date", "Warehouse Address", "Gifts Received Date"};
	    		
	    		CSVWriter writer = new CSVWriter(new FileWriter(oncwritefile.getAbsoluteFile()));
	    	    writer.writeNext(header);
	    	    
	    	    //Crate the gv row
	    	    String[] row = {Long.toString(oncDeliveryDate.getTimeInMillis()),
	    	    				Long.toString(oncSeasonStartDate.getTimeInMillis()),
	    	    				warehouseAddress,
	    	    				Long.toString(oncGiftsReceivedDate.getTimeInMillis())};
	    	    
	    	    writer.writeNext(row);	//Write gv row
	    	    writer.writeNext(oncnumRanges);	//Write the ONC Number region range array
	    	 
	    	    writer.close();
	    	    filename = oncwritefile.getName();
	    	       	    
	    	} 
	    	catch (IOException x)
	    	{
	    		System.err.format("IO Exception: %s%n", x);
	    		JOptionPane.showMessageDialog(pf, oncwritefile.getName() + " could not be saved", 
						"ONC File Save Error", JOptionPane.ERROR_MESSAGE);
	    	}
	    }
    	
	    return filename;
    }

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_GLOBALS"))
		{
			processUpdatedObject(this, ue.getJson());
		}
	}

	@Override
	String update(Object source, ONCObject entity)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_globals>" + 
											gson.toJson(entity, ServerGVs.class));
		if(response.startsWith("UPDATED_GLOBALS"))
		{
			processUpdatedObject(source, response.substring(15));
		}
		
		return response;
	}
	
	void processUpdatedObject(Object source, String json)
	{
		Gson gson = new Gson();
		ServerGVs updatedObj = gson.fromJson(json, ServerGVs.class);
		
		//store updated object in local data base
		oncDeliveryDate.setTime(updatedObj.getDeliveryDate());
		oncSeasonStartDate.setTime(updatedObj.getSeasonStartDate());
		warehouseAddress = updatedObj.getWarehouseAddress();
		oncGiftsReceivedDate.setTime(updatedObj.getGiftsReceivedDate());
		
		//Notify local user IFs that a change occurred
		fireDataChanged(source, "UPDATED_GLOBALS", updatedObj);
	}
}
