package ourneighborschild;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class is an example of how to invoke the Angel out bound rest application program interface.
 *
 */

public class OutboundApiInvoker {
	// constants
    public static final String POST = "POST";
    public static final String CONTENT_TYPE = "application/xml";
    // private variables required to send requests
    private String subscriberId = null;
    private String siteNumber = null;
    private String apiKey = null;

    /**
     * Parameterless constructor conforming to bean architecture
     */
    public OutboundApiInvoker() {
    }

    /**
     * Places and immediate outbound call
     *
     * Immediate call: https://api.angel.com/outbound-rest/<SubscriberID>/<SiteNumber>/immediate?apiKey=<yourapikey>
     *
     * @param waitTime
     * @param phoneNumber
     * @param variables
     */
    public void immediateCall(int waitTime, String phoneNumber, Map<String, String> variables) {
        
        try {
            StringBuilder sb = new StringBuilder();

            // Dialer task will be an object which has values for varibles to be sent to outbound api while making a call.    
            sb.append("<callItem>");
            sb.append("  <maxWaitTime>").append(String.format("%d", waitTime)).append("</maxWaitTime>");
            sb.append("  <phoneNumbers>").append(phoneNumber).append("</phoneNumbers>");

            // add the variables to the request
            if (variables != null && !variables.isEmpty()) {
                // iterate the entries
                for (Entry<String, String> entries : variables.entrySet()) {
                    sb.append("  <variables>");
                    sb.append("    <name>").append(entries.getKey()).append("</name>");
                    sb.append("    <value>").append(entries.getValue()).append("</value>");
                    sb.append("  </variables>");
                }
            }
            // close the tag
            sb.append("</callItem>");

            System.out.println("Outbound Api invoke string" + sb.toString());

            // create the rest url for the request
            String serviceURL = String.format("https://api.angel.com/outbound-rest/%s/%s/immediate?apiKey=%s",
            									subscriberId, siteNumber, apiKey);
             
            // send the request
            String responseXML = sendRequest(serviceURL, sb.toString());
            System.out.println("xml response -> " + responseXML);


            // validate the response at this point
            if (responseXML != null && responseXML.trim().length() > 0) {
                // ResponseXML can be processed here to get back jobid, callguid, statuscode and Message.
            }
            else {
                System.out.println("Outbound Api returned null or zero length response");
            }
        }
        catch (Exception e) {
            System.out.println("Problem encountered while calling outBoundApi:"+ e);
        }
    }

    /**
     * Send the http request to the specified url
     *
     * @param serviceUrl the location the http request is sent to
     * @param dataToSend the content of the request
     * @param requestMethod the http method type
     * @param contentType the content type that will be sent
     * @return
     */
    protected String sendRequest(String serviceUrl, String dataToSend) throws IOException {
        // url connection
        HttpURLConnection urlConnection = null;
        OutputStreamWriter writer = null;
        InputStreamReader reader = null;
        InputStream input = null;

        // the http response content
        String response = null;
        try {
            System.out.println("Making a call to :" + serviceUrl);
            
            URL url = new URL(serviceUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", CONTENT_TYPE);
            urlConnection.setRequestMethod(POST);
            urlConnection.connect();

            // send the content to the url
            writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(dataToSend);
            writer.flush();

            // read the response code
            int responseCode = urlConnection.getResponseCode();
            System.out.println(String.format("http response code %d", responseCode));
            input = urlConnection.getInputStream();
            reader = new InputStreamReader(input);

            // marshal the response into a string
            StringBuilder buf = new StringBuilder();
            char[] cbuf = new char[2048];
            int bytesRead;
            while (-1 != (bytesRead = reader.read(cbuf))) {
                buf.append(cbuf, 0, bytesRead);
            }
            response = buf.toString();
        }
        catch (IOException ioe) {
            System.out.println(ioe);
            throw ioe;
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    // do nothing
                }
            }
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException ex) {
                    // do nothing
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ex) {
                    // do nothing
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return response;
    }

    /**
     * Command line invocation of the api invoker
     *
     * @param args
     
    public static void main(String args[]) {

        // no args means we need help
        if (args.length != 4) {
            // display help
            System.out.println("Required arguments <subId> <siteId> <apiKey> <phoneNumber>");
        }

        // grab the arguements
        OutboundApiInvoker invoker = new OutboundApiInvoker();
        invoker.setSubscriberId(args[0]);
        invoker.setSiteNumber(args[1]);
        invoker.setApiKey(args[2]);

        // invoke without a variable map
        invoker.immediateCall(3, args[3], null);
    }
	*/
    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getSiteNumber() {
        return siteNumber;
    }
    
    public void setSiteNumber(String siteNumber) {
        this.siteNumber = siteNumber;
    }
    
    public String getSubscriberId() {
        return subscriberId;
    }
    
    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }
    //</editor-fold>

}
