package ourneighborschild;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class PNGViewer extends JDialog implements ActionListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FamilyDB familyDB;
	private SMSDB smsDB;
	private PNGPanel pngPanel;
	private ONCFamily family;
	
	private BufferedImage image;
	private String error;
	private JButton btnSendText;
	
	PNGViewer(JFrame pf, ONCFamily family)
	{
		super(pf, true);
		this.setTitle("Delivery Confirmation Image");
		this.setLocationRelativeTo(pf);
		
		this.family = family;
		familyDB = FamilyDB.getInstance();
		smsDB = SMSDB.getInstance();
		
		JPanel controlPanel = new JPanel();
		btnSendText = new JButton("Send Text");
		btnSendText.setEnabled(false);
		btnSendText.addActionListener(this);
		controlPanel.add(btnSendText);
		
		String encodedImage = getEncodedPNGFromServer();
		if(error == null)
			image = convertEncodedImageToBinary(encodedImage);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		pngPanel = new PNGPanel();
		
		contentPanel.add(pngPanel);
		contentPanel.add(controlPanel);
		
		this.setContentPane(contentPanel);
		
		this.setSize(300, 340);
		this.setResizable(true);
	}
	
	String getEncodedPNGFromServer()
	{
		String response = familyDB.getConfirmationImage(this, family);
		
		//check for errors returned from the server
		if(response.startsWith("ERROR:"))
		{	
			error = response;
			btnSendText.setEnabled(false);
		}
		else
		{
			error = null;
			btnSendText.setEnabled(true);
			
		}
		
		return response;
	}
	
	void sendTextWithPNGAttachment(int phoneNumChoice)
	{
		//build text
		String message = "This message is from Our Neighbor's Child confirming  delivery of your child(ren)'s gifts."
				+ " Please see the attached signature photo";
		
		String response = smsDB.sendSingleSMSRequest(message, phoneNumChoice, EntityType.FAMILY, family.getID());
		
		//Get selected family object
		
		
		
		//put up a pop-up with the response
		ONCPopupMessage smsResponsePU = new ONCPopupMessage(GlobalVariablesDB.getONCLogo());
		smsResponsePU.setLocationRelativeTo(this);
		smsResponsePU.show("Server SMS Request Response", response);
	}
/*	
	BufferedImage getPNGFromServer()
	{
		BufferedImage img = null;
		String encodedImage = familyDB.getConfirmationImage(this, family);
		
		//check for errors returned from the server
		if(encodedImage.startsWith("Error:"))
		{
			
		}
		else
		{
    		//decode the image from a Base64 encoded string to a buffered image
    		byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
    		ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
    		try
    		{
    			img = ImageIO.read(bis);
    			bis.close();
    		}
    		catch (IOException e)
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    		return img;
		}
	}
*/	
	BufferedImage convertEncodedImageToBinary(String encodedImage)
	{
		//decode the image from a Base64 encoded string to a buffered image
		BufferedImage img = null;
		try
		{
			byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
			img = ImageIO.read(bis);
			bis.close();
		}
		catch (IllegalArgumentException iae)
		{
			
		}
		catch (IOException e)
		{
			
		}
		
		return img;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btnSendText)
		{
			sendTextWithPNGAttachment(1);
		}
	}
	
	private class PNGPanel extends JPanel
	{
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		PNGPanel()
		{
			this.setPreferredSize(new Dimension(280,300));
		}

	    @Override
	    protected void paintComponent(Graphics g)
	    {
	        super.paintComponent(g);
	        
	        String famIDMssg = String.format("Family %s delivery signature", family.getONCNum());
	        g.drawString(famIDMssg, 30, 20);
	        
	        if(error == null)
	        	g.drawImage(image, 30, 30, this); // see javadoc for more info on the parameters
	        else
	        	g.drawString(error, 30, 100);
	    }
	}
}
