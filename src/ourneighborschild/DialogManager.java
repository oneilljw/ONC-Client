package ourneighborschild;

import java.awt.Point;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.json.JSONException;

public class DialogManager implements EntitySelectionListener
{	
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
	private GiftCatalogDialog catDlg;
	private ManageUsersDialog userDlg;
	private WarehouseSignInDialog signInDlg;
	private ManageVolDialog manageVolDlg;
	private ManageBatteryDialog manageBatteryDlg;
	private ManageActivitiesDialog manageActDlg;
	private ManageGroupsDialog manageGroupsDlg;
	private ViewONCDatabaseDialog dbDlg;
	private PYChildConnectionDialog pyConnectionDlg;
	private AngelAutoCallDialog angelDlg;
	private FamilyHistoryChangesDialog famHistChgDlg;
	private BatteryDialog batteryDlg;
	private ReceiveGiftsByBarcodeDialog recGiftDialog;
		
	//dialogs that inherit from HistoryDialog
	private Map<String, HistoryDialog> historyDlgMap;
	private FamilyHistoryDialog famStatusHistoryDlg;
	private MealDialog mealDlg;
		
	//dialogs that inherit from InfoDialog
	private Map<String, InfoDialog> familyInfoDlgMap;
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
	private SortGiftsDialog sortGiftsDlg;
	private SortMealsDialog sortMealsDlg;
	private ReceiveGiftsDialog recGiftsDlg;
		
	//dialogs that inherit from Entity Dialog
	private Map<String, EntityDialog> entityDlgMap;
	private PartnerDialog orgDlg;
	private GroupDialog groupDlg;
	private VolunteerDialog volunteerDlg;
	private ActivityDialog activityDlg;
	private EditUserDialog editUserDlg;
	private NoteDialog noteDlg;
	private DNSCodeDialog dnsCodeDlg;
		
	private PreferencesDialog prefsDlg;
	private BarcodeWishHistoryDialog barcodeWHDlg;
	private InventoryDialog inventoryDlg;
	private CrosscheckDialog ccDlg;
	
	public static DialogManager getInstance()
	{
		if(instance == null)
			instance = new DialogManager();
		
		return instance;	
	}
	
	private DialogManager()
	{
		eeManager = EntityEventManager.getInstance();
		eeManager.registerEntitySelectionListener(this);
		
		//create the log dialog.. this initializes the ability to log data
		@SuppressWarnings("unused")
		LogDialog logDlg = new LogDialog();	//create the static log dialog
        
        // set up the preferences dialog
        prefsDlg = new PreferencesDialog(GlobalVariablesDB.getFrame());
        
		//initialize the dialog maps
        stDlgMap = new HashMap<String, SortTableDialog>();
        familyInfoDlgMap = new HashMap<String, InfoDialog>();
        entityDlgMap = new HashMap<String, EntityDialog>();
        checkDlgMap = new HashMap<String, CheckDialog>();
        historyDlgMap = new HashMap<String, HistoryDialog>();
        
        //Set up delivery history dialog box 
        famStatusHistoryDlg = new FamilyHistoryDialog(GlobalVariablesDB.getFrame());
        historyDlgMap.put("Family Status History", famStatusHistoryDlg);
        eeManager.registerEntitySelectionListener(famStatusHistoryDlg);
        
        //Set up meal history dialog box 
        mealDlg = new MealDialog(GlobalVariablesDB.getFrame());
        historyDlgMap.put("Meal History", mealDlg);
        eeManager.registerEntitySelectionListener(mealDlg);
        
        //Set up adult dialog box 
        adultDlg = new AdultDialog(GlobalVariablesDB.getFrame());
        eeManager.registerEntitySelectionListener(adultDlg);
       
        //Set up delivery directions dialog box 
        try { dirDlg = new DirectionsDialog(GlobalVariablesDB.getFrame()); }
		catch (JSONException e1) {// TODO Auto-generated catch block 
			e1.printStackTrace();}
        eeManager.registerEntitySelectionListener(dirDlg);
        
        //Set up client map dialog
        cmDlg = new ClientMapDialog(GlobalVariablesDB.getFrame()); 
        
        //Set up the sort gifts dialog
        sortGiftsDlg = new SortGiftsDialog(GlobalVariablesDB.getFrame());
        eeManager.registerEntitySelector(sortGiftsDlg);
        stDlgMap.put("Gifts", sortGiftsDlg);

        	//Set up the manage catalog dialog
    		catDlg = new GiftCatalogDialog(GlobalVariablesDB.getFrame());
    	
    		//Set up the manage user dialog
    		userDlg = new ManageUsersDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(userDlg);
    	
    		//Set up the sign-in dialog
    		signInDlg = new WarehouseSignInDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(signInDlg);
    		
    		//Set up the battery dialog
    		batteryDlg = new BatteryDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(batteryDlg);
    		
    		recGiftDialog = new ReceiveGiftsByBarcodeDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(recGiftDialog);
    		
    		//Set up the manage batteries dialog
    		manageBatteryDlg = new ManageBatteryDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(manageBatteryDlg);
    	
    		//Set up the manage volunteer dialog
    		manageVolDlg = new ManageVolDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(manageVolDlg);
    	
    		//Set up the manage activities dialog
    		manageActDlg = new ManageActivitiesDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(manageActDlg);
    	
    		//set up the manage groups dialog
    		manageGroupsDlg = new ManageGroupsDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(manageGroupsDlg);
    		
    		//set up the manage groups dialog
    		famHistChgDlg = new FamilyHistoryChangesDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(famHistChgDlg);
    	
    		//Set up the sort family dialog
    		sortFamiliesDlg = new SortFamilyDialog(GlobalVariablesDB.getFrame());
    		stDlgMap.put("Families", sortFamiliesDlg);
    		eeManager.registerEntitySelector(sortFamiliesDlg);
        
        //Set up the sort meals dialog
        sortMealsDlg = new SortMealsDialog(GlobalVariablesDB.getFrame());
        stDlgMap.put("Meals", sortMealsDlg);
        eeManager.registerEntitySelector(sortMealsDlg);

    		//Set up the dialog to edit family transportation info
    		transportationDlg = new TransportationDialog(GlobalVariablesDB.getFrame(), false);
    		familyInfoDlgMap.put("Transportation", transportationDlg);
    		eeManager.registerEntitySelectionListener(transportationDlg);
    	
    		//Set up the dialog to add a meal to family
    		addMealDlg = new AddMealDialog(GlobalVariablesDB.getFrame(), true);
    		familyInfoDlgMap.put("Add Meal", addMealDlg);
    	
    		//Set up the dialog to change family ONC Number
    		changeONCNumberDlg = new ChangeONCNumberDialog(GlobalVariablesDB.getFrame());
    		familyInfoDlgMap.put("Change ONC #", changeONCNumberDlg);
    	
    		//Set up the dialog to change family ODB Number
    		changeReferenceNumberDlg = new ChangeReferenceNumberDialog(GlobalVariablesDB.getFrame());
    		familyInfoDlgMap.put("Change Ref #", changeReferenceNumberDlg);
    	
    		//Set up the dialog to change family batch number
    		changeBatchNumberDlg = new ChangeBatchNumberDialog(GlobalVariablesDB.getFrame());
    		familyInfoDlgMap.put("Change Batch #", changeBatchNumberDlg);

    		//Set up the sort agent dialog
    		sortAgentDlg = new SortAgentDialog(GlobalVariablesDB.getFrame());
    		stDlgMap.put("Agents", sortAgentDlg);
    		eeManager.registerEntitySelector(sortAgentDlg);
    	
    		//set up the assign delivery dialog
    		assignDeliveryDlg = new AssignDeliveryDialog(GlobalVariablesDB.getFrame());
    		stDlgMap.put("Deliveries", assignDeliveryDlg);
    		eeManager.registerEntitySelector(sortAgentDlg);
    	
    		//set up the sort driver dialog
    		sortDriverDlg = new SortDriverDialog(GlobalVariablesDB.getFrame());
    		stDlgMap.put("Drivers", sortDriverDlg);
    		eeManager.registerEntitySelector(sortDriverDlg);
    	
    		//Set up the edit volunteer dialog and register it to listen for Family 
    		//Selection events from particular ui's that have driver's associated
        volunteerDlg = new VolunteerDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Volunteers", volunteerDlg);
        eeManager.registerEntitySelectionListener(volunteerDlg);
        
        //Set up the edit activities dialog and register it to listen for activity 
        //selection events from particular ui's that have activities associated
        activityDlg = new ActivityDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Activities", activityDlg);
        eeManager.registerEntitySelectionListener(activityDlg);
        
        //Set up the edit users dialog and register it to listen for user 
        //selection events from particular ui's that have users associated
        editUserDlg = new EditUserDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Users", editUserDlg);
        eeManager.registerEntitySelectionListener(editUserDlg);
        
        //Set up the notes dialog and register it to listen for 
        //selection events from particular ui's that have notes associated
        noteDlg = new NoteDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Notes", noteDlg);
        eeManager.registerEntitySelectionListener(noteDlg);
        
        //Set up the DNS Code dialog and register it to listen for 
        //selection events from particular ui's that have DNS codes associated
        dnsCodeDlg = new DNSCodeDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Codes", dnsCodeDlg);
        eeManager.registerEntitySelectionListener(dnsCodeDlg);
        
        //Set up the view family database dialog
        dbDlg = new ViewONCDatabaseDialog(GlobalVariablesDB.getFrame());
        
        //Set up the edit gift partner dialog
        orgDlg = new PartnerDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Partners", orgDlg);
        eeManager.registerEntitySelectionListener(orgDlg);
        
        //Set up the edit group dialog
        groupDlg = new GroupDialog(GlobalVariablesDB.getFrame());
        entityDlgMap.put("Edit Groups", groupDlg);
        eeManager.registerEntitySelectionListener(groupDlg);
        
        //Set up the Angel auto-call dialog
        angelDlg = new AngelAutoCallDialog(GlobalVariablesDB.getFrame());
        eeManager.registerEntitySelector(angelDlg);
   	    
        //Set up the sort gift partner dialog
        sortPartnerDlg = new SortPartnerDialog(GlobalVariablesDB.getFrame());
        stDlgMap.put("Partners", sortPartnerDlg);
        eeManager.registerEntitySelector(sortPartnerDlg);
        
        //set up the data check dialog and table row selection listener
        dcDlg = new ChildCheckDialog(GlobalVariablesDB.getFrame());
        checkDlgMap.put("Duplicate Children Check", dcDlg);
        eeManager.registerEntitySelector(dcDlg);
        
        //set up the family check dialog and table row selection listener
        dfDlg = new FamilyCheckDialog(GlobalVariablesDB.getFrame());
        checkDlgMap.put("Duplicate Family Check", dfDlg);
        eeManager.registerEntitySelector(dfDlg);
    	
        //set up a dialog to search for wish history from bar code scan
        barcodeWHDlg = new BarcodeWishHistoryDialog(GlobalVariablesDB.getFrame());
        eeManager.registerEntitySelector(barcodeWHDlg);
    	
        //set up a dialog to search for wish history from bar code scan
    		inventoryDlg = new InventoryDialog(GlobalVariablesDB.getFrame());
    		eeManager.registerEntitySelector(inventoryDlg);
        
        //set up a dialog to connect prior year children
    		pyConnectionDlg = new PYChildConnectionDialog(GlobalVariablesDB.getFrame());
        eeManager.registerEntitySelectionListener(pyConnectionDlg);
        
        //set up a dialog to receive gifts. This is last so it's the last called when a 
        //change is received at the server. This allows the bar code text field to retain
        //focus.
      	recGiftsDlg = new ReceiveGiftsDialog(GlobalVariablesDB.getFrame(), GiftStatus.Received);
      	stDlgMap.put("Receive Gifts", recGiftsDlg);
      	eeManager.registerEntitySelector(recGiftsDlg);
      	
      	//set up a dialog to perform CBO cross checks agains our family data base
      	ccDlg = new CrosscheckDialog(GlobalVariablesDB.getFrame());
	}
	
	void showAdultDialog()
	{
		if(!adultDlg.isShowing())
		{
			adultDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
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
	        catDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			catDlg.setVisible(true);
		}
	}
		
	void showUserDialog()
	{
		if(!userDlg.isVisible())
		{
	        userDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			userDlg.setVisible(true);
		}
	}
	
	void showSignInDialog()
	{
		if(!signInDlg.isVisible())
		{
	        signInDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			signInDlg.setVisible(true);
		}
	}
	
	void showManageVolDialog()
	{
		if(!manageVolDlg.isVisible())
		{
	        manageVolDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			manageVolDlg.setVisible(true);
		}
	}
	
	void showBatteryDialog()
	{
		if(!batteryDlg.isVisible())
		{
	        batteryDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			batteryDlg.setVisible(true);
		}
	}
	
	void showReceiveGiftDialog()
	{
		if(!recGiftDialog.isVisible())
		{
			recGiftDialog.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			recGiftDialog.setVisible(true);
		}
	}
	
	void showManageBatteryDialog()
	{
		if(!manageBatteryDlg.isVisible())
		{
	        manageBatteryDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			manageBatteryDlg.setVisible(true);
		}
	}
	
	void showManageActDialog()
	{
		if(!manageActDlg.isVisible())
		{
	        manageActDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			manageActDlg.setVisible(true);
		}
	}
	
	void showManageGroupsDialog()
	{
		if(!manageGroupsDlg.isVisible())
		{
	        manageGroupsDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			manageGroupsDlg.setVisible(true);
		}
	}
	
	void showFamilyHistoriesDialog()
	{
		if(!famHistChgDlg.isVisible())
		{
			famHistChgDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			famHistChgDlg.setVisible(true);
		}
	}
		
	void showAngelCallDialog()
	{
		if(!angelDlg.isVisible())
		{	
			Point pt = GlobalVariablesDB.getFrame().getLocation();
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
					
				Point originPt = GlobalVariablesDB.getFrame().getLocation();
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
				familyInfoDlgMap.get(name).setLocationRelativeTo(GlobalVariablesDB.getFrame());
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
				Point originPt = GlobalVariablesDB.getFrame().getLocation();
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
				checkDlgMap.get(name).setLocationRelativeTo(GlobalVariablesDB.getFrame());
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
				historyDlgMap.get(name).setLocationRelativeTo(GlobalVariablesDB.getFrame());
				historyDlgMap.get(name).display(currFam);
				historyDlgMap.get(name).setVisible(true);
			}
		}
		else
			showDialogError(name);
	}
	
	void showPreferencesDialog()
	{
		if(!prefsDlg.isShowing())
		{
			prefsDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			prefsDlg.display(null);	//keep current user preferences, no change
			prefsDlg.setVisible(true);
		}
	}
	
	void showBarcodeWishHistoryDialog()
	{
		if(!barcodeWHDlg.isShowing())
		{
			barcodeWHDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			barcodeWHDlg.setVisible(true);	
		}
	}
	
	void showInventoryDialog()
	{
		if(!inventoryDlg.isShowing())
		{
			inventoryDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			inventoryDlg.setVisible(true);	
		}
	}
	
	void showCrosscheckDialog()
	{
		if(!ccDlg.isShowing())
		{
			ccDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
			ccDlg.setVisible(true);	
		}
	}
	
	void showAboutONCDialog()
	{
		//User has chosen to view the About ONC dialog
		String versionMsg = String.format("Our Neighbor's Child Client Version %s\n%s", 
				GlobalVariablesDB.getVersion(),  "\u00A92012 - 2016 John W. O'Neill");
		
		JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), versionMsg, "About the ONC App", 
										JOptionPane.INFORMATION_MESSAGE, GlobalVariablesDB.getONCLogo());
	}

	void showDialogError(String name)
	{
		String errMssg = String.format("<html>Show Family Info Dialog Error:<br>%s dialog doesn't exist,<br>"
				+ "						please contact the ONC IT Director</html>", name);
		JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), errMssg, "System Error - Show Family Info Dialog",
				JOptionPane.ERROR_MESSAGE, GlobalVariablesDB.getONCLogo());
	}

	/****************************************************************************************
	 * Shows a dialog to connect prior year child for the family displayed. 
	 */
	void showConnectPYChildDialog()
	{
	    if(!pyConnectionDlg.isVisible())
		{
	    	pyConnectionDlg.display(currFam, currChild);
	    	pyConnectionDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
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
	
	void showOnlineUsers()
	{
		OnlineUserDialog onlineUserDlg = new OnlineUserDialog(GlobalVariablesDB.getFrame());
		onlineUserDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
		onlineUserDlg.setVisible(true);
	}
		
	void onAddNewChildClicked()
	{
		AddNewChildDialog newchildDlg = new AddNewChildDialog(GlobalVariablesDB.getFrame(), currFam);
		newchildDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
		newchildDlg.showDialog();
			
		ONCChild newchild = newchildDlg.getNewChild();
		if(newchild != null)
		{
			ChildDB.getInstance().add(this, newchild);
		}
	}
	
	/*************************************************************************************************************
	 * This method is called when the user requests to mark a child as an adult from the menu bar. 
	 * The child object is added to the adult db and deleted from the child db.The child to be
	 * marked as an adult is the child currently selected in child table in the family panel.
	 * The first step in mark is to confirm with the user that they intended to mark as an adult. 
	 **********************************************************************************************************/
	void onMarkChildAsAdult()
	{
		//Obtain the child to be marked as an Adult
		AdultDB adultDB = AdultDB.getInstance();
		ChildDB childDB = ChildDB.getInstance();
		if(currFam != null && currChild != null && childDB != null && adultDB != null)
		{
			//Save any changed family data prior to moving the child to adult
//			checkAndUpdateFamilyData(currFam);
			
			ONCChild delChild = currChild;
		
			//Confirm with the user that the mark is really intended
			String confirmMssg =String.format("Are you sure you want to change %s %s to an adult?", 
											delChild.getChildFirstName(), delChild.getChildLastName());
		
			Object[] options= {"Cancel", "Make Adult"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								GlobalVariablesDB.getONCLogo(), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(GlobalVariablesDB.getFrame(), "*** Confirm Child to Adult Change ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Make Adult"))
			{
				//create a new adult object and add it to the adult database
				String name = delChild.getChildFirstName() + " " + delChild.getChildLastName();
				AdultGender gender;
				if(delChild.getChildGender().toLowerCase().equals("girl"))
					gender = AdultGender.Female;
				else if(delChild.getChildGender().toLowerCase().equals("boy"))
					gender = AdultGender.Male;
				else
					gender = AdultGender.Unknown;
				
				//add to the adult database
				adultDB.add(this, new ONCAdult(-1, delChild.getFamID(), name, gender));
				
				//delete from the child data base
				childDB.delete(this, delChild);
			}
		}
	}
	
	/*************************************************************************************************************
	 * This method is called when the user requests to delete a child from the menu bar. The child to be deleted
	 * is the child currently selected in child table in the family panel. The first step in deletion is to confirm
	 * with the user that they intended to delete the child. 
	 **********************************************************************************************************/
	void onDeleteChild()
	{
		ChildDB childDB = ChildDB.getInstance();
		//Obtain the child to be deleted
		if(currFam != null && childDB != null)
		{
			//Save any changed family data prior to the deletion of the child
//			checkAndUpdateFamilyData(currFam);
			
			ONCChild delChild = currChild;
		
			//Confirm with the user that the deletion is really intended
			String confirmMssg =String.format("Are you sure you want to delete %s %s from the data base?", 
											delChild.getChildFirstName(), delChild.getChildLastName());
		
			Object[] options= {"Cancel", "Delete"};
			JOptionPane confirmOP = new JOptionPane(confirmMssg, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION,
								GlobalVariablesDB.getONCLogo(), options, "Cancel");
			JDialog confirmDlg = confirmOP.createDialog(GlobalVariablesDB.getFrame(), "*** Confirm Child Database Deletion ***");
			confirmDlg.setVisible(true);
		
			Object selectedValue = confirmOP.getValue();
			if(selectedValue != null && selectedValue.toString().equals("Delete"))
				childDB.delete(this, delChild);
		}
	}
/*   
   void onWhoIsOnline()
   {
   		//get online users from user data base
   		UserDB userDB = UserDB.getInstance();
   		if(userDB != null)
   		{
   			List<ONCUser> onlineUserList = userDB.getOnlineUsers();
   			if(onlineUserList != null)
   			{
   				StringBuffer sb = new StringBuffer("<html>");
			
   				for(ONCUser ou: onlineUserList)
   					if(ou != null)	//can be null if server log in failure -- known server issue Oct 16, 2014
   					{
   						String year = ou.getClientYear() == -1 ? "None" : Integer.toString(ou.getClientYear());
   						sb.append(ou.getFirstname() + " " + ou.getLastname() + ": " + year + "<br>");
   					}
			
   				sb.append("</html>");
					
   				//Show user list
   				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), sb.toString(),
   						"ONC Elves Online", JOptionPane.INFORMATION_MESSAGE, GlobalVariables.getONCLogo());
   			}
   			else
   			{
   				JOptionPane.showMessageDialog(GlobalVariables.getFrame(), "Couldn't retrive online users from ONC Server",
   						"ONC Server Error", JOptionPane.ERROR_MESSAGE, GlobalVariables.getONCLogo());
   			}
   		}
   }
*/   
   void onChat()
   {
   		ChatDialog chatDlg = new ChatDialog(GlobalVariablesDB.getFrame(), true, -1);	//true=user initiated chat, -1=no target yet
   		chatDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
   		chatDlg.setVisible(true);
   }
   
   void onWebsiteStatus()
   { 
   		WebsiteStatusDialog wsDlg = new WebsiteStatusDialog(GlobalVariablesDB.getFrame(), false);
   		wsDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
   		wsDlg.display(null);	//deosn't require a display parameter
   		wsDlg.setVisible(true);
   }
   
   void onDBStatusClicked()
   {
   		DatabaseStatusDialog statusDlg = new DatabaseStatusDialog(GlobalVariablesDB.getFrame());
   		statusDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
   		statusDlg.setVisible(true);
   }
   
   void onEditProfile()
   {
	   	//construct and display a UserProfile Dialog. If the user is an Agent, Admin or Sys_Admin, 
	   	//use the dialog that includes groups, otherwise use the simplified profile dialog.
	   	ONCUser user = UserDB.getInstance().getLoggedInUser();
	   	if(user.getPermission().compareTo(UserPermission.Agent) >=0)
	   	{
	   		UpdateProfileWithGroupsDialog upDlg = new UpdateProfileWithGroupsDialog(GlobalVariablesDB.getFrame(), user, false);
	   		upDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
	   		upDlg.showDialog();
	   	}
	   	else
	   	{
	   		UserProfileDialog upDlg = new UserProfileDialog(GlobalVariablesDB.getFrame(), user, null);
	   		upDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
	   		upDlg.showDialog();
	   	}
   }
   
   boolean onChangePassword()
   {
		ChangePasswordDialog cpDlg = new ChangePasswordDialog(GlobalVariablesDB.getFrame());
		cpDlg.setLocationRelativeTo(GlobalVariablesDB.getFrame());
		String result = "<html>New and re-entered passwords didn't match.<br>Please try again.</html>";
		boolean bPasswordChanged = false;
		
		if(cpDlg.showDialog())
		{
			if(cpDlg.doNewPasswordsMatch())
			{
				ONCUser currUser = UserDB.getInstance().getLoggedInUser();
				String[] pwInfo = cpDlg.getPWInfo();
				
				ChangePasswordRequest cpwReq = new ChangePasswordRequest(currUser.getID(),
						currUser.getFirstName(), currUser.getLastName(),
						EncryptionManager.encrypt(pwInfo[0]), EncryptionManager.encrypt(pwInfo[1]));
				
				if((result = UserDB.getInstance().changePassword(this, cpwReq)).contains("changed"))
					bPasswordChanged = true;
			}
			
			JOptionPane.showMessageDialog(GlobalVariablesDB.getFrame(), result,"Change Password Result",
       			 JOptionPane.ERROR_MESSAGE,GlobalVariablesDB.getONCLogo());
		}
		
		return bPasswordChanged;
   }
   
   void onClearMenuItemClicked()
   {
/*    	
   	//Clear the GUI's which disables buttons that are dependent on db being loaded
   	//These methods clear the buttons as necessary
   	oncStatusPanel.ClearData();
   	oncFamilyPanel.ClearFamilyData();
   	
   	//Clear any existing persistent data
   	if(oncFamDB.size() > 0)	//Clear child and delivery data before clearing family data
   		for(ONCFamily fam:oncFamDB.getList())
   		{
//  			fam.getChildArrayList().clear();
//   			fam.getDeliveryStatusAL().clear();	//NO LONGER NEEDED WHEN SEPARATE DELIVERY DB CREATED
   		}
   	
   	oncFamDB.clear();	
   	oncOrgDB.clear();
//   	oncPYCDB.clear();
   	oncWishCat.clearCatalogData();
   	
   	//Close all dialogs
   	oncFamilyPanel.closeAllDialogs();
   	
   	//Disable the menu bar for items that can't function without data
   	oncMenuBar.SetEnabledMenuItems(false);
   	oncMenuBar.SetEnabledRestrictedMenuItems(false);
   	
   	oncFrame.setTitle(APPNAME);
   	
   	return;
*/    	  	
   }
   
   void setEnabledAdminPrivileges(boolean tf)
   {
	   prefsDlg.setEnabledRestrictedPrefrences(tf);
   }
	
	void setEnabledSuperuserPrivileges(boolean tf)
	{
//    	prefsDlg.setEnabledDateToday(true);
		prefsDlg.setEnabledRestrictedPrefrences(tf);
//		sortFamiliesDlg.setFamilyStatusComboItemEnabled(FAMILY_STATUS_SELECTION_LIST_PACKAGED_INDEX, tf);
	}
	
	boolean receiveGiftBarcodeRequestFocus()
	{
		if(recGiftsDlg != null && recGiftsDlg.isVisible())
		{
			recGiftsDlg.barcodeRequestFocus();
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void entitySelected(EntitySelectionEvent tse) 
	{	
		if(tse.getType() == EntityType.FAMILY || tse.getType() == EntityType.CHILD || 
				tse.getType() == EntityType.GIFT)
		{
			currFam = (ONCFamily) tse.getObject1();
			
			if(tse.getObject2() != null)
				currChild = (ONCChild) tse.getObject2();
			else
			{
				ChildDB cDB = ChildDB.getInstance();
				List<ONCChild> childList = cDB.getChildren(currFam.getID());
				if(!childList.isEmpty())
					currChild = childList.get(0);
			}
		}
	}

	@Override
	public EnumSet<EntityType> getEntityEventListenerEntityTypes() 
	{
		return EnumSet.of(EntityType.FAMILY, EntityType.CHILD, EntityType.GIFT);
	}
}
