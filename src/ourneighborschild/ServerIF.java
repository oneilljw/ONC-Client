package ourneighborschild;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerIF
{
	/***
	 * Implements the server interface for the ONC client. The serverIF is a singleton that is
	 * instantiated once in each ONC Client instance. The class maintains a reference that is 
	 * accessed by the getInstance() method. 
	 */
	
	private static final int SOCKET_CREATION_TIMEOUT = 1000 * 3;	//Timeout delay if server doesn't respond, in milliseconds
	private static final int SOCKET_TRANSMISSION_TIMEOUT = 0;	//Timeout delay if server doesn't respond, in milliseconds
	private static final int NETWORK_TIME_LIMIT = 1000 * 10;	//Timeout delay if server doesn't respond, in milliseconds
	private static final int SERVER_LOG_TIME_INTERVAL = 1000 * 60 * 5;	//time interval between writing server logs
	private static final int NORMAL_POLLING_RATE = 1000 * 1;	//frequency of server polling, in milliseconds
//	private static final int ACTIVE_POLLING_RATE = 100;	//frequency of server polling, in milliseconds
	private static final int SERVER_LOG_LINE_LENGTH = 96;	//maximum number of characters in log line stored
	private static final String LOG_FILE_A = "LogFile_A.txt";
	private static final String LOG_FILE_B = "LogFile_B.txt";
	
	//Reference to the singleton object
	private static ServerIF instance;
	
	//Server Connection
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
//  private PrintWriter out;
    private boolean bConnected;
    private String loginMssg;
    private Timer timer;
    private long timeCommandSent;
    private long timeLastLogWritten;
    private int nServerErrorsDetected;
    private ArrayList<String> serverLog;
    private boolean bLogFileA; 	//determines which log file (A or B) to write
    private boolean bDatabaseLoaded;	//true when the user has loaded local data base from the server
    
    //List of registered listeners for Sever data changed events
    private ArrayList<ServerListener> listeners;
    
    ServerIF(String serverAddress, int port) throws UnknownHostException, IOException, SocketTimeoutException
    {    	
    	bConnected = false;
    	timeCommandSent = 0;
    	timeLastLogWritten = System.currentTimeMillis();
    	nServerErrorsDetected = 0;
    	bLogFileA = true;
    	bDatabaseLoaded = false;
    	
    	socket = new Socket();
    	
    	timeCommandSent = System.currentTimeMillis();
//    	time = new Date(timeCommandSent);
    	
//    	System.out.println("ServerIF Connecting to Server at: " + timeCommandSent);
    	
    	socket.connect(new InetSocketAddress(serverAddress, port), SOCKET_CREATION_TIMEOUT);
    	socket.setSoTimeout ( SOCKET_TRANSMISSION_TIMEOUT );

        if(socket != null)
        {
        	try 
        	{
        		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()),1); 
//        		out = new PrintWriter(socket.getOutputStream(), true);
        	} 
        	catch (IOException e1)
        	{
        		// TODO Auto-generated catch block
        		e1.printStackTrace();
        	}
        
        	try 
        	{
        		//should encrypt the login message
        		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		loginMssg = EncryptionManager.decrypt(in.readLine());
        		
//        		long timeElapsed = System.currentTimeMillis() - timeCommandSent;
//        		System.out.println(String.format("ServerIF: Took %d milliseconds to connect to Server", timeElapsed));
			
        		if (loginMssg.startsWith("LOGIN"))
        			bConnected = true;
        	} 
        	catch (IOException e1) 
        	{
        		// TODO Auto-generated catch block
        		e1.printStackTrace();
        	}
        
        	//Create the polling timer
        	timer = new Timer(NORMAL_POLLING_RATE, new TimerListener());
        }
        
        serverLog = new ArrayList<String>();
      
        instance = this;
    }
    
    public synchronized static ServerIF getInstance()
    {
    	return instance; 
    }
    
    void setEnabledServerPolling(boolean tf)
    {
    	if(tf)
    		timer.start();
    	else
    		timer.stop();
    }
    
    void setDatabaseLoaded(boolean tf) { bDatabaseLoaded = tf; }
    
    synchronized String sendRequest(String request)
    {
    	timeCommandSent = System.currentTimeMillis();

    	try {
			out.write(request);
			out.newLine();
	    	out.flush();
	    	
	    	if(request.length() > SERVER_LOG_LINE_LENGTH)
	    		addServerLogItem("Request: " + request.substring(0, SERVER_LOG_LINE_LENGTH-1));
	    	else
	    		addServerLogItem("Request: " + request);
	    		
		} 
    	catch (IOException e1) 
    	{
    		GlobalVariables gvs = GlobalVariables.getInstance();
    		
	    	String mssg = String.format("Error sending command<br>%s<br> to ONC Server,<br>netwok connection may be lost." +
	    			"<br>Server errors detected: %d", request, nServerErrorsDetected); 			   		
	    	
	    	ONCPopupMessage clientIDPU = new ONCPopupMessage(gvs.getImageIcon(0));
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			clientIDPU.show("ONC Server I/F Exception", mssg);
	    	
			e1.printStackTrace();
		}
    	
    	String response = null;
    		
    	try { 
    		response = in.readLine(); 
    	}	//Blocks until response received or timeout occurs
		catch (IOException e) { 
			serverConnectionIssue();
		}
    	
    	//if the network response time is very slow, notify the user
    	long elapsedTime = System.currentTimeMillis() - timeCommandSent;
//    	System.out.println("Elapsed Time: " + elapsedTime);
    	
		if(elapsedTime > NETWORK_TIME_LIMIT && bDatabaseLoaded)	//Don't show pop-up until local data loaded
		{
	    	GlobalVariables gvs = GlobalVariables.getInstance();
	    	String mssg = String.format("Server I/F is slow,<br>last transaction took %d seconds", 
	    									elapsedTime/1000);
	    	
	    	ONCPopupMessage clientIDPU = new ONCPopupMessage(gvs.getImageIcon(0));
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			clientIDPU.show("ONC Server I/F Notification", mssg);
		}
		
    	if(response == null)
		{
			GlobalVariables gvs = GlobalVariables.getInstance();
	    	String mssg = "Server did not respond,<br>netwok connection may be lost"; 			   		
	    	
	    	ONCPopupMessage clientIDPU = new ONCPopupMessage(gvs.getImageIcon(0));
			clientIDPU.setLocationRelativeTo(GlobalVariables.getFrame());
			clientIDPU.show("ONC Server I/F Exception", mssg);
			
	    	response ="ERROR_SERVER_DID_NOT_RESPOND";
	    	addServerLogItem("Response: " + response);  
		}
    	else
    	{
    		if(response.length() > SERVER_LOG_LINE_LENGTH)
	    		addServerLogItem("Response: " + response.substring(0, SERVER_LOG_LINE_LENGTH-1));
	    	else
	    		addServerLogItem("Response: " + response);
    	}
    	
       	return response;
    }
    void serverConnectionIssue()
    {
    	//if the server if is not connected, notify the user and exit
    	GlobalVariables gvs = GlobalVariables.getInstance();
    	String mssg = "<html>Server I/F: Connection with ONC Server lost<br>" +
    				"all changes have been saved</html>";
        		
    	JOptionPane.showMessageDialog(GlobalVariables.getFrame(), mssg, "ONC Server Connecton Error", 
    									JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
    	
    	//need to write server log to disk prior to exit
    	writeServerLogFile();
    	
    	//exit the application
    	System.exit(0);
    }
    
    String getLoginMssg() { return loginMssg; }
    
    void close()
    {
    	timer.stop();
    	
    	writeServerLogFile();
    	
    	try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void addServerLogItem(String item)
    {
    	Calendar timestamp = Calendar.getInstance();
		String line = new SimpleDateFormat("H:mm:ss.SSS").format(timestamp.getTime());
		serverLog.add(line + " " + item);
//		LogDialog.add(item, "ServerLog");
    }
    
    void writeServerLogFile()
    {
    	PrintWriter outputStream = null;
        FileWriter fileWriter = null;
        
        String filename = bLogFileA ? LOG_FILE_A : LOG_FILE_B;
        
		try
		{
			fileWriter = new FileWriter(System.getProperty("user.dir") + "/" + filename);
			outputStream = new PrintWriter(fileWriter);
			 for(String s: serverLog) 
		        	outputStream.println(s);
			 
			 serverLog.clear();
			 bLogFileA = !bLogFileA;	//switch to opposite log file
			 timeLastLogWritten = System.currentTimeMillis();	//set the last time written
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally 
		{
			if(outputStream != null)
				outputStream.close();	
		}
    }
    
    ArrayList<String> getServerLog() { return serverLog; }
    
    boolean isConnected() { return bConnected; }
    
    void processChanges(String qContentJson)
    {
    	String[] restrictedResponses = {"UPDATED_GLOBALS",
    						  "USER_ONLINE", "USER_OFFLINE",
    						  "ADDED_FAMILY", "UPDATED_FAMILY",
    						  "ADDED_MEAL","UPDATED_MEAL", "DELETED_MEAL",
    						  "ADDED_AGENT","UPDATED_AGENT", "DELETED_AGENT",
    						  "ADDED_GROUP","UPDATED_GROUP", "DELETED_GROUP",
    						  "ADDED_CHILD","UPDATED_CHILD", "DELETED_CHILD",
    						  "ADDED_ADULT","UPDATED_ADULT", "DELETED_ADULT",
    						  "WISH_ADDED", "UPDATED_CHILD_WISH",
    						  "ADDED_PARTNER", "UPDATED_PARTNER", "DELETED_PARTNER", 
    						  "ADDED_CATALOG_WISH", "UPDATED_CATALOG_WISH", "DELETED_CATALOG_WISH",
    						  "ADDED_WISH_DETAIL", "UPDATED_WISH_DETAIL", "DELETED_WISH_DETAIL",
    						  "ADDED_DELIVERY","UPDATED_DELIVERY",
    						  "ADDED_DRIVER", "UPDATED_DRIVER", "DELETED_DRIVER",
    						  "ADDED_INVENTORY_ITEM", "INCREMENTED_INVENTORY_ITEM",
    						  "UPDATED_INVENTORY_ITEM", "DELETED_INVENTORY_ITEM",
    						  "UPDATED_WEBSITE_STATUS"};
    	
    	String[] chatResponses = {"CHAT_REQUESTED", "CHAT_ACCEPTED", "CHAT_MESSAGE", "CHAT_ENDED",  "ADDED_NEW_YEAR"};
    	
    	String[] unrestrictedResponses = {"UPDATED_DBYEAR", "ADDED_DBYEAR", "ADDED_USER", "UPDATED_USER",};
    	
    	Gson gson = new Gson();
    	Type listtype = new TypeToken<ArrayList<String>>(){}.getType();
    	ArrayList<String> changeList = gson.fromJson(qContentJson, listtype);
    	
    	//loop thru list of changes, processing each one
    	for(String change: changeList)
    	{
    		if(change.startsWith("GLOBAL_MESSAGE"))
    		{
    			ONCPopupMessage popup = new ONCPopupMessage(GlobalVariables.getONCLogo());
    			Point loc = GlobalVariables.getFrame().getLocationOnScreen();
    			popup.setLocation(loc.x+450, loc.y+70);
    			popup.show("Message from ONC Server", change.substring(14));
    		}
    		else if(change.startsWith("CHAT_"))
    		{
    			//if a message is a CHAT message, we don't have to wait for the user to load a local data base
    			//chats can occur prior to the local data base being loaded
    			int index = 0;
    			while(index < chatResponses.length && !change.startsWith(chatResponses[index]))
    				index++;
    	
    			if(index < chatResponses.length)
    				fireDataChanged(chatResponses[index], change.substring(chatResponses[index].length()));
    		}
    		else if(change.contains("_DBYEAR") || change.contains("_USER"))
    		{
    			int index = 0;
    			while(index < unrestrictedResponses.length && !change.startsWith(unrestrictedResponses[index]))
    				index++;
    	
    			if(index < unrestrictedResponses.length)
    				fireDataChanged(unrestrictedResponses[index], change.substring(unrestrictedResponses[index].length()));
    		
    		}
    		else if(bDatabaseLoaded)
    		{
    			//all other change processing requires local data bases to be loaded from the server first
    			//otherwise we run the risk of updating data without a local copy present
    			int index = 0;
    			while(index < restrictedResponses.length && !change.startsWith(restrictedResponses[index]))
    				index++;
    			
    			if(index < restrictedResponses.length)
    			{
    				String logEntry = String.format("Server IF.processChanges Event: %s, Message: %s",
    						restrictedResponses[index], change);
    				LogDialog.add(logEntry, "S");
    				fireDataChanged(restrictedResponses[index], change.substring(restrictedResponses[index].length()));
    			}
    		}
    	}
    }
    
    /** Register a listener for server DataChange events */
    synchronized public void addServerListener(ServerListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<ServerListener>();
    	listeners.add(l);
    }  

    /** Remove a listener for server DataChange */
    synchronized public void removeServerListener(ServerListener l)
    {
    	if (listeners == null)
    		listeners = new ArrayList<ServerListener>();
    	listeners.remove(l);
    }
    
    /** Fire a Data ChangedEvent to all registered listeners */
    protected void fireDataChanged(String event_type, String json)
    {
    	// if we have no listeners, do nothing...
    	if (listeners != null && !listeners.isEmpty())
    	{
    		// create the event object to send
    		ServerEvent event = new ServerEvent(this, event_type, json);

    		// make a copy of the listener list in case anyone adds/removes listeners
    		ArrayList<ServerListener> targets;
    		synchronized (this) { targets = (ArrayList<ServerListener>) listeners.clone(); }

    		// walk through the cloned listener list and call the dataChanged method in each
    		for(ServerListener l:targets)
    			l.dataChanged(event);
    	}
    }
    
    private class TimerListener implements ActionListener
    {
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			
			if(e.getSource() == timer)
			{
				//pause the timer so this EDT thread can complete. It should happen quickly, 
				//but not necessarily. Don't stack up timer events
				timer.stop();     	
        	
				String response = sendRequest("GET<changes>");
        	
				if(response.startsWith("ERROR"))
				{
					//Server communication error occurred, deal with heart beat issues here
				}
				else if(response.equals("NO_CHANGES"))
				{
//					timer.setInitialDelay(NORMAL_POLLING_RATE);
				}
				else
				{
					processChanges(response);
//					timer.setInitialDelay(ACTIVE_POLLING_RATE);
				}
        	
				if(System.currentTimeMillis() > timeLastLogWritten + SERVER_LOG_TIME_INTERVAL)
					writeServerLogFile();
				
				timer.start();
			}
        }
    }
}
