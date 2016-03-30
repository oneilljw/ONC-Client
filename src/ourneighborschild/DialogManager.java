package ourneighborschild;

import java.awt.Point;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.json.JSONException;

public class DialogManager implements EntitySelectionListener
{
	private static final int FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX = 6;
	
	private static DialogManager instance;		//instance variable for this singleton class
	
	//reference to the EntityEventManager
    EntityEventManager eeManager;
    
    //track the current family and current child selected in the client
    private ONCFamily currFam;
    private ONCChild currChild;
    
	//Dialogs that are children of the family panel
	private static ClientMapDialog cmDlg;
		
	private AdultDialog adultDlg;
	private DirectionsDialog dirDlg;
	private WishCatalogDialog catDlg;
	private ONCUserDialog userDlg;
	private ViewONCDatabaseDialog dbDlg;
	private PYChildConnectionDialog pyConnectionDlg;
	private AngelAutoCallDialog angelDlg;
		
	//dialogs that inherit from HistoryDialog
	private Map<String, HistoryDialog> historyDlgMap;
	private DeliveryHistoryDialog dsDlg;
	private MealDialog mealDlg;
		
	//dialogs that inherit from InfoDialog
	private Map<String, InfoDialog> familyInfoDlgMap;
	private AgentInfoDialog agentInfoDlg;
	private TransportationDialog transportationDlg;
	private AddMealDialog addMealDlg;
	private ChangeONCNumberDialog changeONCNumberDlg;
	private ChangeReferenceNumberDialog changeReferenceNumberDlg;
	private ChangeBatchNumberDialog changeBatchNumberDlg;
		
	//dialogs that inherit from CheckDialog
	private Map<String, CheckDialog> checkDlgMap;
	private ChildCheckDialog dcDlg;
	private FamilyCheckDialog dfDlg;
		
	//dialogs that inherit from SortTableDialog
	private Map<String, SortTableDialog> stDlgMap;
	private SortFamilyDialog sortFamiliesDlg;
	private SortAgentDialog sortAgentDlg;
	private AssignDeliveryDialog assignDeliveryDlg;
	private SortDriverDialog sortDriverDlg;
	private SortPartnerDialog sortPartnerDlg;
	private SortWishDialog sortWishesDlg;
	private SortMealsDialog sortMealsDlg;
	private ReceiveGiftsDialog recGiftsDlg;
		
	//dialogs that inherit from Entity Dialog
	private Map<String, EntityDialog> entityDlgMap;
	private OrganizationDialog orgDlg;
	private DriverDialog driverDlg;
		
//	private LogDialog logDlg;
	public static DialogManager getInstance()
	{
		if(instance == null)
			instance = new DialogManager();
		
		return instance;	
	}
	
	private DialogManager()
	{
		eeManager = EntityEventManager.getInstance();
		
		 //initialize the dialog maps
        stDlgMap = new HashMap<String, SortTableDialog>();
        familyInfoDlgMap = new HashMap<String, InfoDialog>();
        entityDlgMap = new HashMap<String, EntityDialog>();
        checkDlgMap = new HashMap<String, CheckDialog>();
        historyDlgMap = new HashMap<String, HistoryDialog>();
        
        //Set up delivery history dialog box 
        dsDlg = new DeliveryHistoryDialog(GlobalVariables.getFrame());
        historyDlgMap.put("Delivery History", dsDlg);
        eeManager.registerEntitySelectionListener(dsDlg);
        
        //Set up meal history dialog box 
        mealDlg = new MealDialog(GlobalVariables.getFrame());
        historyDlgMap.put("Meal History", mealDlg);
        eeManager.registerEntitySelectionListener(mealDlg);
        
        //Set up adult dialog box 
        adultDlg = new AdultDialog(GlobalVariables.getFrame());
        eeManager.registerEntitySelectionListener(adultDlg);
       
        //Set up delivery directions dialog box 
        try { dirDlg = new DirectionsDialog(GlobalVariables.getFrame()); }
		catch (JSONException e1) {// TODO Auto-generated catch block 
			e1.printStackTrace();}
        eeManager.registerEntitySelectionListener(dirDlg);
        
        //Set up client map dialog
        cmDlg = new ClientMapDialog(GlobalVariables.getFrame()); 
        
        //Set up the sort wishes dialog
        sortWishesDlg = new SortWishDialog(GlobalVariables.getFrame());
        eeManager.registerEntitySelector(sortWishesDlg);
        stDlgMap.put("Wishes", sortWishesDlg);

    	//Set up the manage catalog dialog
    	catDlg = new WishCatalogDialog(GlobalVariables.getFrame());
    	
    	//Set up the manage user dialog
    	userDlg = new ONCUserDialog(GlobalVariables.getFrame());
    	
    	 //Set up the sort family dialog
        sortFamiliesDlg = new SortFamilyDialog(GlobalVariables.getFrame());
        stDlgMap.put("Families", sortFamiliesDlg);
        eeManager.registerEntitySelector(sortFamiliesDlg);
        
        //Set up the sort meals dialog
        sortMealsDlg = new SortMealsDialog(GlobalVariables.getFrame());
        stDlgMap.put("Meals", sortMealsDlg);
        eeManager.registerEntitySelector(sortMealsDlg);
    	
    	//Set up the dialog to edit agent info
    	String[] tfNames = {"Name", "Organization", "Title", "Email", "Phone"};
    	agentInfoDlg = new AgentInfoDialog(GlobalVariables.getFrame(), tfNames, false);
    	familyInfoDlgMap.put("Agent", agentInfoDlg);
    	eeManager.registerEntitySelectionListener(agentInfoDlg);
    	
    	//Set up the dialog to edit family transportation info
    	String[] transNames = {"ONC #", "Last Name", "Has Transportation?"};
    	transportationDlg = new TransportationDialog(GlobalVariables.getFrame(), false, transNames);
    	familyInfoDlgMap.put("Transportation", transportationDlg);
    	eeManager.registerEntitySelectionListener(transportationDlg);
    	
    	//Set up the dialog to add a meal to family
    	String[] mealNames = {"ONC #", "Last Name", "Meal Type", "Restrictions"};
    	addMealDlg = new AddMealDialog(GlobalVariables.getFrame(), true, mealNames);
    	familyInfoDlgMap.put("Add Meal", addMealDlg);
    	
    	//Set up the dialog to change family ONC Number
    	String[] oncNum = {"Change ONC #"};
    	changeONCNumberDlg = new ChangeONCNumberDialog(GlobalVariables.getFrame(), oncNum);
    	familyInfoDlgMap.put("Change ONC #", changeONCNumberDlg);
    	
    	//Set up the dialog to change family ODB Number
    	String[] refNum = {"Change Ref #"};
    	changeReferenceNumberDlg = new ChangeReferenceNumberDialog(GlobalVariables.getFrame(), refNum);
    	familyInfoDlgMap.put("Change Ref #", changeReferenceNumberDlg);
    	
    	//Set up the dialog to change family batch number
    	String[] batchNum = {"Change Batch #"};
    	changeBatchNumberDlg = new ChangeBatchNumberDialog(GlobalVariables.getFrame(), batchNum);
    	familyInfoDlgMap.put("Change Batch #", changeBatchNumberDlg);

    	//Set up the sort agent dialog
    	sortAgentDlg = new SortAgentDialog(GlobalVariables.getFrame());
    	stDlgMap.put("Agents", sortAgentDlg);
    	eeManager.registerEntitySelector(sortAgentDlg);
    	
    	//set up the assign delivery dialog
    	assignDeliveryDlg = new AssignDeliveryDialog(GlobalVariables.getFrame());
    	stDlgMap.put("Deliveries", assignDeliveryDlg);
    	eeManager.registerEntitySelector(sortAgentDlg);
    	
    	//set up the sort driver dialog
    	sortDriverDlg = new SortDriverDialog(GlobalVariables.getFrame());
    	stDlgMap.put("Drivers", sortDriverDlg);
    	eeManager.registerEntitySelector(sortDriverDlg);
    	
    	//Set up the edit driver (deliverer) dialog and register it to listen for Family 
    	//Selection events from particular ui's that have driver's associated
        driverDlg = new DriverDialog(GlobalVariables.getFrame());
        entityDlgMap.put("Edit Delivery Partners", driverDlg);
        eeManager.registerEntitySelectionListener(driverDlg);
        
        //Set up the view family database dialog
        dbDlg = new ViewONCDatabaseDialog(GlobalVariables.getFrame());
        
        //Set up the edit gift partner dialog
        orgDlg = new OrganizationDialog(GlobalVariables.getFrame());
        entityDlgMap.put("Edit Partners", orgDlg);
        eeManager.registerEntitySelectionListener(orgDlg);
        
        //Set up the Angel auto-call dialog
       angelDlg = new AngelAutoCallDialog(GlobalVariables.getFrame());
   	   eeManager.registerEntitySelector(angelDlg);
   	   
        
        //Set up the sort gift partner dialog
        sortPartnerDlg = new SortPartnerDialog(GlobalVariables.getFrame());
        stDlgMap.put("Partners", sortPartnerDlg);
        eeManager.registerEntitySelector(sortPartnerDlg);
        
        //set up the data check dialog and table row selection listener
        dcDlg = new ChildCheckDialog(GlobalVariables.getFrame());
        checkDlgMap.put("Duplicate Children Check", dcDlg);
        eeManager.registerEntitySelector(dcDlg);
        
        //set up the family check dialog and table row selection listener
        dfDlg = new FamilyCheckDialog(GlobalVariables.getFrame());
        checkDlgMap.put("Duplicate Family Check", dfDlg);
        eeManager.registerEntitySelector(dfDlg);
        
        //set up a dialog to receive gifts
		recGiftsDlg = new ReceiveGiftsDialog(GlobalVariables.getFrame(), WishStatus.Received);
		stDlgMap.put("Receive Gifts", recGiftsDlg);
    	eeManager.registerEntitySelector(recGiftsDlg);
        
        //set up a dialog to connect prior year children
    	pyConnectionDlg = new PYChildConnectionDialog(GlobalVariables.getFrame());
        eeManager.registerEntitySelectionListener(pyConnectionDlg);
	}
	
	void showAdultDialog()
	{
		if(!adultDlg.isShowing())
		{
			adultDlg.setLocationRelativeTo(GlobalVariables.getFrame());
			adultDlg.display(currFam);
			adultDlg.setVisible(true);
		}
	}
	
	void showDrivingDirections()
	{
		if(!dirDlg.isShowing())
		{
			dirDlg.display(currFam);
			dirDlg.setVisible(true);
		}
	}
		
	void showClientMap()
	{
		if(!cmDlg.isShowing())
		{
			cmDlg.display();
			cmDlg.setVisible(true);
		}
	}

	void showWishCatalogDialog()
	{
		if(!catDlg.isVisible())
		{
	        catDlg.setLocationRelativeTo(GlobalVariables.getFrame());
			catDlg.setVisible(true);
		}
	}
		
	void showUserDialog()
	{
		if(!userDlg.isVisible())
		{
	        userDlg.setLocationRelativeTo(GlobalVariables.getFrame());
			userDlg.setVisible(true);
		}
	}
		
	void showAngelCallDialog()
	{
		if(!angelDlg.isVisible())
		{	
			Point pt = GlobalVariables.getFrame().getLocation();
		    angelDlg.setLocation(pt.x + 5, pt.y + 20);
			angelDlg.setVisible(true);
		}
	}
		
	void showSortDialog(String name, Point offsetPt)
	{
		//retrieve the sort dialog from the map
		if(stDlgMap.containsKey(name))
		{
			if(!stDlgMap.get(name).isVisible())
			{
				stDlgMap.get(name).initializeFilters();
				stDlgMap.get(name).buildTableList(true);
					
				Point originPt = GlobalVariables.getFrame().getLocation();
				stDlgMap.get(name).setLocation(originPt.x + offsetPt.x, originPt.y + offsetPt.y);
		        stDlgMap.get(name).setVisible(true);
			}
		}
		else
			showDialogError(name);
	}
		
	void showFamilyInfoDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(familyInfoDlgMap.containsKey(name))
		{
			if(!familyInfoDlgMap.get(name).isVisible())
			{
				familyInfoDlgMap.get(name).display(currFam);
				familyInfoDlgMap.get(name).setLocationRelativeTo(GlobalVariables.getFrame());
				familyInfoDlgMap.get(name).showDialog();
			}
		}	
		else
			showDialogError(name);
	}
		
	void showEntityDialog(String name, Point offsetPt)
	{
		//retrieve the sort dialog from the map
		if(entityDlgMap.containsKey(name))
		{
			if(!entityDlgMap.get(name).isVisible())
			{
				entityDlgMap.get(name).display(null);
				Point originPt = GlobalVariables.getFrame().getLocation();
				entityDlgMap.get(name).setLocation(originPt.x + offsetPt.x, originPt.y + offsetPt.y);
				entityDlgMap.get(name).setVisible(true);
			}
		}	
		else
			showDialogError(name);
	}
		
	void showCheckDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(checkDlgMap.containsKey(name))
		{
			if(!checkDlgMap.get(name).isVisible())
			{
				checkDlgMap.get(name).buildTableList();
				checkDlgMap.get(name).setLocationRelativeTo(GlobalVariables.getFrame());
				checkDlgMap.get(name).setVisible(true);
			}
		}	
		else
			showDialogError(name);
	}
	
	void showHistoryDialog(String name)
	{
		//retrieve the sort dialog from the map
		if(historyDlgMap.containsKey(name))
		{
			if(!historyDlgMap.get(name).isShowing())
			{
				historyDlgMap.get(name).setLocationRelativeTo(GlobalVariables.getFrame());
				historyDlgMap.get(name).display(currFam);
				historyDlgMap.get(name).setVisible(true);
			}
		}
		else
			showDialogError(name);
	}

	void showDialogError(String name)
	{
		String errMssg = String.format("<html>Show Famaily Info Dialog Error:<br>%s dialog doesn't exist,<br>"
				+ "						please contact the ONC IT Director</html>", name);
		JOptionPane.showMessageDialog(GlobalVariables.getFrame(), errMssg, "System Error - Show Family Info Dialog",
				JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
	}

	/****************************************************************************************
	 * Shows a dialog to connect prior year child for the family displayed. 
	 */
	void showConnectPYChildDialog()
	{
	    if(!pyConnectionDlg.isVisible())
		{
	    	pyConnectionDlg.display(currFam, currChild);
	    	pyConnectionDlg.setLocationRelativeTo(GlobalVariables.getFrame());
	    	pyConnectionDlg.setVisible(true);
		}
	}
		
	void showEntireDatabase()
	{
		if(!dbDlg.isVisible())
		{
			dbDlg.buildDatabase();
			dbDlg.setVisible(true);
		}
	}
		
	void onAddNewChildClicked()
	{
		ChildDB cDB = ChildDB.getInstance();
		String[] fieldNames = {"First Name", "Last Name", "School", "Gender", "Date of Birth"};
		AddNewChildDialog newchildDlg = new AddNewChildDialog(GlobalVariables.getFrame(), fieldNames, currFam);
		newchildDlg.setLocationRelativeTo(GlobalVariables.getFrame());
		newchildDlg.showDialog();
			
		ONCChild newchild = newchildDlg.getNewChild();
		if(newchild != null)
		{
			cDB.add(this, newchild);
		}
	}
	

	 /*********************************************************************************************
    * This method imports a .csv file that contains a ONC Family Referral Worksheet. It creates
    * a RAFamilyImporter object and registers the family panel to receive family selection 
    * events from the importer. Then it executes the importer which will interface with 
    * the user to select and import a sequence of ONC Family Referral Worksheets
    *********************************************************************************************/
   void onImportRAFMenuItemClicked()
   {
   	//get a reference to the EntityEventManager
       EntityEventManager eeManager = EntityEventManager.getInstance();
       
   		RAFamilyImporter importer = new RAFamilyImporter(GlobalVariables.getFrame());
   		eeManager.registerEntitySelector(importer);
 //  	importer.addEntitySelectionListener(familyChildSelectionListener);
   		importer.onImportRAFMenuItemClicked();
   		eeManager.removeEntitySelector(importer);
 //  	importer.removeEntitySelectionListener(familyChildSelectionListener);
   }
	
	void setEnabledSuperuserPrivileges(boolean tf)
	{
		sortFamiliesDlg.setFamilyStatusComboItemEnabled(FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX, tf);
	}
	
	void setTextPaneFontSize()
	{
		 orgDlg.setTextPaneFontSize();
	}

	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{
		if(tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.CHILD || 
				tse.getType() == EntityType.WISH)
		{
			currFam = (ONCFamily) tse.getObject1();
			currChild = (ONCChild) tse.getObject2();
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.WISH);
	}
}
