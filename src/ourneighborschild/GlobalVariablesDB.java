package ourneighborschild;

import java.awt.Image;
import java.awt.Point;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.google.gson.Gson;

public class GlobalVariablesDB extends ServerListenerComponent implements ServerListener
{
	/**
	 * This class holds global variables common to all classes in the ONC program
	 */
	private static final int NUM_OF_XMAS_ICONS = 5;
	private static final int XMAS_ICON_OFFSET = 9;
	private static final int AVERY_LABEL_DEFAULT_X_OFFSET = 24;
	private static final int AVERY_LABEL_DEFAULT_Y_OFFSET = 50;
	
	private static GlobalVariablesDB instance = null;
	
	private static JFrame oncFrame;
	
	private ServerGVs serverGVs;
	
	private transient String[] sGrwth_pcts = {"5%", "10%", "15%", "20%", "25%"};
	private transient int[] nGrwth_pcts = {5,10,15,20,25};
	private transient int startONCNum, ytyGrwthIndex;
	private static ImageIcon imageIcons[];

	private static String version;
	private static WebsiteStatus websiteStatus;
	private static boolean bBarcodeOnOrnmament;
	private Barcode barcode;
	private Point averyLabelOffsetPoint;
	
	public static GlobalVariablesDB getInstance()
	{
		if(instance == null)
			instance = new GlobalVariablesDB();
		
		return instance;
	}
	
	private GlobalVariablesDB()
	{	
		super();
		
		if(serverIF != null)
			serverIF.addServerListener(this);
		
		//Initialize class variables
		serverGVs = new ServerGVs();
	   
	    imageIcons = new ImageIcon[60];
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
		imageIcons[9] = createImageIcon("Gift-icon.png", "Gift Icon");
		imageIcons[10] = createImageIcon("Christmas-Mistletoe-icon.gif", "Mistletoe Icon");
		imageIcons[11] = createImageIcon("ONCLabelIcon2012.gif", "Snowman Icon");
		imageIcons[12] = createImageIcon("Santa-icon.gif", "Santa Icon");
		imageIcons[13] = createImageIcon("Stocking-icon.gif", "Stocking Icon");
		
		imageIcons[14] = createImageIcon("family-history-icon.png", "Family History Icon");
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
		
		imageIcons[28] = createImageIcon("103.png", "Add");
		imageIcons[29] = createImageIcon("101.png", "Remove");
		
		imageIcons[30] = createImageIcon("Meal-icon.png", "Meals");
		imageIcons[31] = createImageIcon("vw-beetle-icon.png", "VW-Beetle");
		imageIcons[32] = createImageIcon("History-icon.png", "History");
		imageIcons[33] = createImageIcon("Agent-icon.png", "Agent Info");
		imageIcons[34] = createImageIcon("FamilyInformation-icon.png", "Family Info");
		imageIcons[35] = createImageIcon("phone-icon.png", "All Phone Info");
		imageIcons[36] = createImageIcon("NoCar-icon.png", "No car");
		imageIcons[37] = createImageIcon("ReferredMeal-icon.png", "Referred Meal");
		imageIcons[38] = createImageIcon("NoMeal.jpg", "No car");
		imageIcons[39] = createImageIcon("GoogleMaps-icon.png", "Google Maps");
		imageIcons[40] = createImageIcon("gift_black.png", "No Gift Card");
		imageIcons[41] = createImageIcon("gift_white.png", "Gift Card");
		imageIcons[42] = createImageIcon("adults.png", "Adults");
		imageIcons[43] = createImageIcon("no_adults.png", "No Adults");
		imageIcons[44] = createImageIcon("Green_tag.png", "Gift Label");
		imageIcons[45] = createImageIcon("cornerhat.png", "Corner Hat");
		imageIcons[46] = createImageIcon("candle.png", "Candle");
		
		//clipboards
		imageIcons[47] = createImageIcon("clipboard-gray-32.png", "No Family Note");
		imageIcons[48] = createImageIcon("clipboard-black-32.png", "Family Note Sent");
		imageIcons[49] = createImageIcon("clipboard-blue-32.png", "Family Note Read");
		imageIcons[50] = createImageIcon("clipboard-green-32.png", "Family Note Responded");
		imageIcons[51] = createImageIcon("clipboard-red-32.png", "Family Note Past Deadline");
		
		imageIcons[52] = createImageIcon("clipboard-gray-16.png", "No Family Note_small");
		imageIcons[53] = createImageIcon("clipboard-black-16.png", "Family Note Sent_small");
		imageIcons[54] = createImageIcon("clipboard-blue-16.png", "Family Note Read_small");
		imageIcons[55] = createImageIcon("clipboard-green-16.png", "Family Note Responded_small");
		imageIcons[56] = createImageIcon("clipboard-red-16.png", "Family Note Past Deadline_small");
		
		imageIcons[57] = createImageIcon("giftpickup.png", "Gift Pickup");
		imageIcons[58] = createImageIcon("homedelivery.png", "Home Delivery");
		imageIcons[59] = createImageIcon("contract.png", "Delivery Signature");
		
		startONCNum = 100;
		ytyGrwthIndex = 2;
		version = "N/A";
		bBarcodeOnOrnmament = true;
		barcode = Barcode.UPCE;
		averyLabelOffsetPoint = new Point(AVERY_LABEL_DEFAULT_X_OFFSET,
											AVERY_LABEL_DEFAULT_Y_OFFSET);
	}
	
	//Getters
	static JFrame getFrame() { return oncFrame; }
	Date getTodaysDate() { return Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime(); }
	
	
	ServerGVs getServerGVs() { return serverGVs; }
	int getCurrentSeason()
	{ 
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(serverGVs.getSeasonStartDateMillis());
		return cal.get(Calendar.YEAR);
	}
	public Long getDeliveryDateMillis() { return serverGVs.getDeliveryDayMillis(); }
	public Long getGiftsReceivedDate() { return serverGVs.getGiftsReceivedDeadlineMillis(); }
	public Long getThanksgivingMealDeadline() { return serverGVs.getThanksgivingMealDeadlineMillis(); }
	public Long getDecemberGiftDeadline() { return serverGVs.getDecemberGiftDeadlineMillis(); }
	public Long getDecemberMealDeadline() { return serverGVs.getDecemberMealDeadlineMillis(); }
	public Long getWaitlistGiftDeadline() { return serverGVs.getWaitListGiftDeadlineMillis(); }
	public Long getFamilyEditDeadline() { return serverGVs.getFamilyEditDeadlineMillis(); }
	public Long getSeasonStartDate() { return serverGVs.getSeasonStartDateMillis(); }
	Calendar getSeasonStartCal()
	{ 
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(serverGVs.getSeasonStartDateMillis());
		return cal;
	}
	public String getWarehouseAddress() { return serverGVs.getWarehouseAddress(); }
	int getStartONCNum() { return startONCNum; };
	int getYTYGrwthIndex() { return ytyGrwthIndex; }
	int getYTYGrwthPct() { return nGrwth_pcts[ytyGrwthIndex]; } 
	String[] getGrwthPcts() { return sGrwth_pcts; }
	int getDefaultGiftID() { return serverGVs.getDefaultGiftID(); }
	int getDefaultGiftCardID() { return serverGVs.getDefaultGiftCardID(); }
	int getNumberOfGiftsPerChild() { return serverGVs.getNumberOfGiftsPerChild(); }
	int getChildWishIntakeConfiguration() { return serverGVs.getChildWishIntakeConfiguraiton(); }
	GiftDistribution getGiftDistribution() { return serverGVs.getGiftDistribution(); }
	int getMealIntake() { return serverGVs.getMealIntake(); } 

	public ImageIcon getImageIcon(int icon){ return imageIcons[icon]; }
	public ImageIcon getImageIcon(String description)
	{
		int index=0;
		while(index < imageIcons.length && !imageIcons[index].getDescription().equals(description))
			index++;
		
		if(index < imageIcons.length)
			return imageIcons[index];
		else
			return null;		
	}
	public static ImageIcon getONCLogo() { return imageIcons[0]; }
	public static ImageIcon getUnLockedIcon() { return imageIcons[17]; }
	public static ImageIcon getLockedIcon() { return imageIcons[18]; }
	public static ImageIcon[] getLights()
	{
		ImageIcon[] lights = {imageIcons[27], imageIcons[23], imageIcons[24],
				imageIcons[25], imageIcons[26]};
		
		return lights;
	}
	public static ImageIcon[] getTinyClipboradIcons()
	{
		ImageIcon[] lights = {imageIcons[27], imageIcons[52], imageIcons[53],
				imageIcons[54], imageIcons[55], imageIcons[56]};
		
		return lights;
	}
	
	ImageIcon getTinyClipboardIcon(ONCNote n)
	{
		if(n == null)
			return imageIcons[52];
		else if(n.getStatus() == ONCNote.SENT)
			return imageIcons[53];
		else if(n.getStatus() == ONCNote.READ)
			return imageIcons[54];
		else if(n.getStatus() == ONCNote.RESPONDED)
			return imageIcons[55];
		else
			return imageIcons[56];
	}
	
	ImageIcon[] getImageIcons() {return imageIcons; }
	ImageIcon getONCFullScreenLogo() {return imageIcons[15]; }
	Image getImage(int icon) { return imageIcons[icon].getImage(); }
	static String getVersion() { return version; }
	public ImageIcon getSeasonIcon()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(serverGVs.getSeasonStartDateMillis());
		
		return imageIcons[cal.get(Calendar.YEAR) % NUM_OF_XMAS_ICONS + XMAS_ICON_OFFSET];
	}
	WebsiteStatus getWebsiteStatus() {return websiteStatus; }
	boolean includeBarcodeOnLabels() { return bBarcodeOnOrnmament; }
	Barcode getBarcodeCode() { return barcode; }
	Point getAveryLabelOffset() { return averyLabelOffsetPoint; }

	//setters globally used - need to update at the server and broadcast
	public void setDeliveryDate(Long dd) { serverGVs.setDeliveryDayMillis(dd); }
	public void setGiftsReceivedDate(Long grd) { serverGVs.setGiftsReceivedDateMillis(grd); }
	public void setThanksgivingMealDeadline(Long td) { serverGVs.setThanksgivingMealDeadlineMillis(td); }
	public void setDecemberGiftDeadline(Long dd) { serverGVs.setDecemberGiftDeadlineMillis(dd); }
	public void setDecemberMealDeadline(Long dmd) { serverGVs.setDecemberMealDeadlineMillis(dmd); }
	public void setWaitlistGiftDeadline(Long wlgd) { serverGVs.setWaitListGiftDeadlineMillis(wlgd); }
	public void setFamilyEditDeadline(Long fed) { serverGVs.setFamilyEditDeadlineMillis(fed); }
	public void setSeasonStartDate(Long ssd) { serverGVs.setSeasonStartDateMillis(ssd); }
	public void setWarehouseAddress(String address) {serverGVs.setWarehouseAddress(address); }
	public void setIncludeBarcodeOnLabels(boolean tf) { bBarcodeOnOrnmament = tf; }
	public void setBarcode(Barcode barcode) { this.barcode = barcode; }
	public void setAveryLabelOffset(Point offset) { this.averyLabelOffsetPoint = offset; }
	public void setDefaultGift(ONCGift w) { serverGVs.setDefaultGiftID(w.getID()); }
	public void setDefaultGiftCard(ONCGift w) { serverGVs.setDefaultGiftCardID(w.getID()); }
	public void setNumberOfGiftsPerChild(int numGifts) { serverGVs.setNumberOfGiftsPerChild(numGifts); }
	public void setChildWishIntakeConfiguration(int config) { serverGVs.setChildWishIntakeConfiguration(config); }
	public void setGiftDistribution(GiftDistribution dist) { serverGVs.setGiftDistribution(dist); }
	public void setMealIntake(int intake) { serverGVs.setMealIntake(intake); }
	
	//Setters locally used
	void setFrame(JFrame frame) { oncFrame = frame; }
	void setImageIcons(ImageIcon[] ii) {imageIcons = ii; }
	void setYTYGrowthIndex(int index) { ytyGrwthIndex = index; }
	void setStartONCNum(int startoncnum) { startONCNum = startoncnum; }
	void setVersion(String version) { GlobalVariablesDB.version = version; }
	
	//helpers
	static Calendar getCalendar(Long day)
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(day);
		return cal;
	}
	
	boolean isDayBeforeOrDeliveryDay()
	{
		return serverGVs.isDeliveryDayOrDayBefore();
	}
	
	 /** Returns an ImageIcon, or null if the path was invalid. */
	ImageIcon createImageIcon(String path, String description)
	{
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) { return new ImageIcon(imgURL, description); } 
		else { System.err.println("Couldn't find file: " + path); return null; }
	}
	
	boolean importDB()
	{
		String response;
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			response = serverIF.sendRequest("GET<globals>");
			if(response != null && response.startsWith("GLOBALS"))
			{
				this.serverGVs = gson.fromJson(response.substring(7), ServerGVs.class);
				return true;
			}
			else
				return false;
		}
		
		return false;
	}
	
	String initializeWebsiteStatusFromServer()
	{	
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			String response = serverIF.sendRequest("GET<website_status>");
			if(response.startsWith("WEBSITE_STATUS"))
			{
				GlobalVariablesDB.websiteStatus = gson.fromJson(response.substring(14), WebsiteStatus.class);
				return "WEBSITE_STATUS_RECEIVED";
			}
		}
		
		return "WEBISTE_STATUS_FAILED";
	}

	@Override
	public void dataChanged(ServerEvent ue)
	{
		if(ue.getType().equals("UPDATED_GLOBALS"))
		{
			processUpdatedObject(this, ue.getJson());
		}
		else if(ue.getType().equals("UPDATED_WEBSITE_STATUS"))
		{
			processUpdatedWebsiteStatus(this, ue.getJson());
		}
	}

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
		serverGVs = gson.fromJson(json, ServerGVs.class);

		//Notify local user IFs that a change occurred
		fireDataChanged(source, "UPDATED_GLOBALS", serverGVs);
	}
	
	String updateWebsiteStatus(Object source, WebsiteStatus updateWSReq)
	{
		Gson gson = new Gson();
		String response = "";
		
		response = serverIF.sendRequest("POST<update_website_status>" + 
											gson.toJson(updateWSReq, WebsiteStatus.class));
		if(response.startsWith("UPDATED_WEBSITE_STATUS"))
		{
			processUpdatedWebsiteStatus(source, response.substring(22));
		}
		
		return response;
	}
	
	String reloadWebpages(Object source)
	{
		String response = "";
		
		response = serverIF.sendRequest("POST<update_webpages>");
		if(response.startsWith("UPDATED_WEBPAGES"))
		{
			processUpdatedWebpages(source);
		}
		
		return response;
	}
	
	void processUpdatedWebsiteStatus(Object source, String websiteStatusJson)
	{
		Gson gson = new Gson();
		WebsiteStatus updatedWebsiteStatus = gson.fromJson(websiteStatusJson, WebsiteStatus.class);
		
		//store updated websiteStatus in local data base
		websiteStatus = updatedWebsiteStatus;
		
		//Notify local user IFs that a change occurred
		fireDataChanged(source, "UPDATED_WEBSITE_STATUS", updatedWebsiteStatus);
	}
	
	void processUpdatedWebpages(Object source)
	{	
		//Notify local user IFs that a change occurred
		fireDataChanged(source, "UPDATED_WEBPAGES", null);
	}
}
