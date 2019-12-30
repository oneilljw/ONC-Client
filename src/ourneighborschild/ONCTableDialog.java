package ourneighborschild;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public abstract class ONCTableDialog extends JDialog implements ActionListener, ListSelectionListener, 
																DatabaseListener, EntitySelector
{
	/**
	 * Implements an abstract class for all dialogs that contain tables that display ONC Objects. This
	 * class provides a blueprint for the inheriting class proving the ability to register entity 
	 * selection listeners and fire entity selection events
	 */
	private static final long serialVersionUID = 1L;
	protected JFrame parentFrame;
	protected GlobalVariablesDB gvs;
	protected JPanel searchCriteriaPanelTop, searchCriteriaPanelBottom;
	protected ONCTable dlgTable;
	protected AbstractTableModel dlgTableModel;
	protected JComboBox<String> exportCB;
	private JButton btnResetFilters;
	private JLabel lblCount;
	
	//Map of registered listeners for table selection events
	private Map<EntityType, ArrayList<EntitySelectionListener>> listenerMap;
    
    public ONCTableDialog(JFrame parentFrame)
    {
    		super(parentFrame);
    		this.parentFrame = parentFrame;
    		gvs = GlobalVariablesDB.getInstance();
    		
    		listenerMap = new HashMap<EntityType, ArrayList<EntitySelectionListener>>();
    		for(EntityType entityType : getEntityEventSelectorEntityTypes())
			listenerMap.put(entityType, new ArrayList<EntitySelectionListener>());
    		
    		//set up the top panel
    		JPanel searchPanel = new JPanel();
    		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
    		
    		//Set up the upeer search criteria panel      
    		searchCriteriaPanelTop = new JPanel();
    		searchCriteriaPanelTop.setLayout(new FlowLayout(FlowLayout.LEFT));
				
    		//Create the ONC Icon label and add it to the search criteria panel
    		JLabel lblONCicon = new JLabel(GlobalVariablesDB.getONCLogo());
    		lblONCicon.setToolTipText("ONC Client v" + GlobalVariablesDB.getVersion());
    		lblONCicon.setAlignmentX(Component.LEFT_ALIGNMENT );//0.0
    		searchCriteriaPanelTop.add(lblONCicon);
    		
    		//Set up the lower search criteria panel      
    		searchCriteriaPanelBottom = new JPanel();
    		searchCriteriaPanelBottom.setLayout(new FlowLayout(FlowLayout.CENTER));
    							
    		searchPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
    		searchPanel.add(searchCriteriaPanelTop);
    		searchPanel.add(searchCriteriaPanelBottom);
    		
    		//Create the table model
    		dlgTableModel = createTableModel();
    		
    		//create the table
    		dlgTable = new ONCTable(dlgTableModel, columnToolTips(), new Color(240,248,255));

    		dlgTable.setSelectionMode(listSelectionModel());
    		dlgTable.getSelectionModel().addListSelectionListener(this);
    		
    		//Set table column widths
    		int tablewidth = 0;
    		for(int col=0; col < columnWidths().length; col++)
    		{
    			dlgTable.getColumnModel().getColumn(col).setPreferredWidth(columnWidths()[col]);
    			tablewidth += columnWidths()[col];
    		}
    		tablewidth += 24; 	//count for vertical scroll bar
    		
        dlgTable.setAutoCreateRowSorter(true);	//add a sorter
            
        JTableHeader anHeader = dlgTable.getTableHeader();
        anHeader.setForeground( Color.black);
        anHeader.setBackground( new Color(161,202,241));
            
        //justify wish count column
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(SwingConstants.CENTER);
        	for(int col=0; col < centeredColumns().length; col++)
        		dlgTable.getColumnModel().getColumn(centeredColumns()[col]).setCellRenderer(dtcr);
        	
        	dtcr.setHorizontalAlignment(SwingConstants.LEFT);
        	for(int col=0; col < leftColumns().length; col++)
        		dlgTable.getColumnModel().getColumn(centeredColumns()[col]).setCellRenderer(dtcr);
            
        //Create the scroll pane and add the table to it.
        JScrollPane dsScrollPane = new JScrollPane(dlgTable);
        dsScrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        
        JPanel countPanel = new JPanel();
        countPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        lblCount = new JLabel("# of History Items: 0");
        countPanel.add(lblCount);
        
        JPanel cntlPanel = new JPanel();
        cntlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        //set up the custom control gui for this dialog
        exportCB = new JComboBox<String>(exportChoices());
        exportCB.setPreferredSize(new Dimension(136, 28));
        exportCB.setEnabled(false);
        exportCB.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent ae) { onExport(); }});
       
        cntlPanel.add(exportCB);
        
        btnResetFilters = new JButton("Reset Filters");
        btnResetFilters.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent ae) { resetFilters(); }});
        cntlPanel.add(btnResetFilters);
       
        bottomPanel.add(countPanel);
        bottomPanel.add(cntlPanel);
            
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(searchPanel);
        getContentPane().add(dsScrollPane);
        getContentPane().add(bottomPanel);
            
        pack();
        this.setMinimumSize(new Dimension(tablewidth, 240));   		
    }
    
    abstract void resetFilters();
    abstract String[] columnToolTips();
    abstract int[] columnWidths();
    abstract int[] leftColumns();
    abstract int[] centeredColumns();
    abstract int listSelectionModel();
    abstract AbstractTableModel createTableModel();
    abstract void buildTableList();
    abstract String[] exportChoices();
    abstract void onExport();
    
    void setCount(int count)
    {
    		lblCount.setText(String.format("# of History Items: %d", count));
    }
    
    Date getTomorrowsDate()
	{
		Calendar tomorrow = Calendar.getInstance();
		
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
	    tomorrow.set(Calendar.MINUTE, 0);
	    tomorrow.set(Calendar.SECOND, 0);
	   	tomorrow.set(Calendar.MILLISECOND, 0);
		return tomorrow.getTime();
	}
    
    /** Register a listener for Entity Selection events */
    synchronized public void addEntitySelectionListener(EntityType type, EntitySelectionListener l)
    {
    		listenerMap.get(type).add(l);
    }  

    /** Remove a listener for Entity Selection events */
    synchronized public void removeEntitySelectionListener(EntityType type, EntitySelectionListener l)
    {
    		listenerMap.get(type).remove(l);
    }
    
    /** Fire an Entity Selection event to all registered listeners */
    @SuppressWarnings("unchecked")
	public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2)
    {
    		// if we have no listeners, do nothing...
    		ArrayList<EntitySelectionListener> listeners = listenerMap.get(entityType);
    		if (listeners != null && !listeners.isEmpty())
    		{
    			// create the event object to send
    			EntitySelectionEvent event = new EntitySelectionEvent(source, entityType, obj1, obj2);
    		
    			// make a copy of the listener list in case anyone adds/removes listeners
    			ArrayList<EntitySelectionListener> targets;
    			synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    			// walk through the cloned listener list and call the dataChanged method in each
    			for(EntitySelectionListener l:targets)
    				l.entitySelected(event);
    		}
    }
    
    @SuppressWarnings("unchecked")
	public void fireEntitySelected(Object source, EntityType entityType, Object obj1, Object obj2, Object obj3)
    {
    		// if we have no listeners, do nothing...
    		ArrayList<EntitySelectionListener> listeners = listenerMap.get(entityType);
    		if (listeners != null && !listeners.isEmpty())
    		{
    			// create the event object to send
    			EntitySelectionEvent event = new EntitySelectionEvent(source, entityType, obj1, obj2, obj3);
    		
    			// make a copy of the listener list in case anyone adds/removes listeners
    			ArrayList<EntitySelectionListener> targets;
    			synchronized (this) { targets = (ArrayList<EntitySelectionListener>) listeners.clone(); }

    			// walk through the cloned listener list and call the dataChanged method in each
    			for(EntitySelectionListener l:targets)
    				l.entitySelected(event);
    		}
    }
}
