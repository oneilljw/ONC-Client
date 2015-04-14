package OurNeighborsChild;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides an interface to Google Maps to get driving directions and maps
 */
public class DrivingDirections 
{
	JSONObject getGoogleDirections(String startAddress, String destAddress) throws JSONException
	{
		//Make request to Google Maps API to get Driving Directions, returns JSON
	    StringBuffer response = new StringBuffer(4096);
	    
	    //Set the driving direction JSON request into a string - April 2015 - Google Maps
	    //API no loner requires the sensor parameter
	    String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
    		startAddress + "&destination=" + destAddress; // + "&sensor=false";
	 
	    //Turn the string into a valid URL
	    URL dirurl= null;
		try {dirurl = new URL(stringUrl);} 
		catch (MalformedURLException e2) {e2.printStackTrace();}
		
		//Attempt to open the URL via a network connection to the Internet
	    HttpURLConnection httpconn= null;
		try {httpconn = (HttpURLConnection)dirurl.openConnection();} 
		catch (IOException e1) {e1.printStackTrace();}
		
		//It opened successfully, get the data
	    try {
	    		if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
	    		{
	    			BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
	    			String strLine = null;
	    			while ((strLine = input.readLine()) != null)
					    response.append(strLine);					
	    			input.close();
	    			
//	    			System.out.println(response.toString());
				}
	    	}
		catch (IOException e1)
		{
			 JOptionPane.showMessageDialog(null, "Can't get Google Driving Directions",
						"Google Driving Direction Issue", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	    
	    //Create a JSON Object from the data stream received
	    return new JSONObject(response.toString());
	}
	
	BufferedImage getGoogleMap(JSONObject route, String destAddress) throws JSONException
	{	
		//Get start_end lat/lngs
		String[] start_end = getStartEndLocations(route);
		
		//Get Map
	  	URL mapURL = null;
	    BufferedImage map = null;
	    String url = "http://maps.googleapis.com/maps/api/staticmap?";
	    String parms = "&size=600x350";
	    String markers = "&markers=color:green%7Clabel:S%7C" + start_end[0] +"," + start_end[1] +
	  						"&markers=color:red%7Clabel:D%7C" + start_end[2] +"," + start_end[3] +"&sensor=false";

	    try
	    {
	    	mapURL = new URL(url+destAddress+parms+markers);
	  	}
	    catch (MalformedURLException e1) 
	    {
	        // TODO Auto-generated catch block
	  		e1.printStackTrace();
	  		 JOptionPane.showMessageDialog(null, "Can't get Static Google Map",
	  				"Google Map - Static Map Access Issue", JOptionPane.ERROR_MESSAGE);
	  	}
	          
	    try 
	    {
	    	map = ImageIO.read(mapURL);
	  	} 
	    catch (IOException e1) {
	  		// TODO Auto-generated catch block
	  		e1.printStackTrace();
	  	}
	    
	    return map;
	}
	
	JSONObject getTripRoute(JSONObject dirJSONObject) throws JSONException
	{
	    // routesArray contains ALL routes (should only be one) and
	    // Take all legs (should only be one) from the route and grab the first (and only) leg 
	    JSONArray routesArray = dirJSONObject.getJSONArray("routes");
		JSONObject route = routesArray.getJSONObject(0);
			         	
		JSONArray legs = route.getJSONArray("legs");
	    return  legs.getJSONObject(0);
	}
	
	String[] getStartEndLocations(JSONObject leg) throws JSONException
	{
		String[] startend = new String[4];
		
		JSONObject start_locObject = leg.getJSONObject("start_location");		
		startend[0] = Double.toString(start_locObject.getDouble("lat"));
		startend[1] = Double.toString(start_locObject.getDouble("lng"));
		    
		JSONObject end_locObject = leg.getJSONObject("end_location");		
		startend[2] = Double.toString(end_locObject.getDouble("lat"));
		startend[3] = Double.toString(end_locObject.getDouble("lng"));
		
		return startend;
	}
	
	JSONArray getDrivingSteps(JSONObject leg) throws JSONException
	{
		return leg.getJSONArray("steps");
	}
	
	String getTripDuration(JSONObject leg) throws JSONException
	{		
		return leg.getJSONObject("duration").getString("text");
	}
}
