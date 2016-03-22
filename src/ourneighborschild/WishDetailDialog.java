package ourneighborschild;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.Gson;

public class WishDetailDialog extends JDialog implements ActionListener
{
	/*****************************************************************************************
	 * This class implements a dialog that allows the user to specify up to four additional
	 * detail from the user regarding gift selection details. It provides customizable 
	 * combo boxes and a detail text field. 
	 ******************************************************************************************/
	private static final long serialVersionUID = 1L;
	private static final int MAX_NUM_OF_WISH_DETAILS_PER_WISH = 4;
	
	private JPanel drpanel;
	private JPanel[] drsubpanel;
	private JTextField wishnameTF;
	private ArrayList<JTextField> titleTFAL, contentTFAL;
	private JButton btnSave, btnCancel;
	private boolean bWishChanged;
	
	WishDetailDB wdDB;	//reference to the wish detail data base
	ONCWish w; //wish being edited
	
	WishDetailDialog(JDialog owner, String title)
	{
		super(owner, title, true);	
		
		//get reference to wish detail data base
		wdDB = WishDetailDB.getInstance();
		
		titleTFAL = new ArrayList<JTextField>();
		contentTFAL = new ArrayList<JTextField>();
		
		//Create the wish name panel
		JPanel wishnamepanel = new JPanel();
		
		JLabel lblWishMssg = new JLabel("Wish Name: ");
		wishnameTF = new JTextField(8);
		
		wishnamepanel.add(lblWishMssg);
		wishnamepanel.add(wishnameTF);
		
		//Create the variable detail required panel, make it null
		drpanel = new JPanel();
		drpanel.setLayout(new BoxLayout(drpanel, BoxLayout.Y_AXIS));
		
		drsubpanel = new JPanel[MAX_NUM_OF_WISH_DETAILS_PER_WISH];
		for(int i=0; i< drsubpanel.length; i++)
		{
			drsubpanel[i] = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			JTextField titleTF = new JTextField();
			titleTF.setPreferredSize(new Dimension (88, 44));
			titleTF.setBorder(BorderFactory.createTitledBorder("List Name"));
			titleTF.addActionListener(this);
			titleTFAL.add(titleTF);	//Add detail name to array list
			drsubpanel[i].add(titleTF);
			
			JTextField contentTF = new JTextField();
			contentTF.setPreferredSize(new Dimension (392, 44));
			contentTF.setBorder(BorderFactory.createTitledBorder("List Contents ( separate list items with a ; )"));
			contentTFAL.add(contentTF);
			drsubpanel[i].add(contentTF);
			
			drpanel.add(drsubpanel[i]);
		}
		
		JPanel cntlpanel = new JPanel();
		btnSave = new JButton("Save Wish");
		btnSave.addActionListener(this);
		cntlpanel.add(btnSave);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);
		cntlpanel.add(btnCancel);
		
		 //Add the components to the frame pane
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.add(wishnamepanel);
        this.add(drpanel);
        this.add(cntlpanel);
        
        pack();
	}
	
	boolean showDialog()
	{
		bWishChanged = false;
		
		this.setVisible(true);
		
		return bWishChanged;
	}

	void displayWishDetail(ONCWish wish, int totalCount)
	{
		this.w = wish;
		wishnameTF.setText(w.getName());
		
		if(totalCount > 0)
			wishnameTF.setEnabled(false);
		else
			wishnameTF.setEnabled(true);
		
		int wishdetailID;
		String name, choices;
		for(int dn=0; dn<MAX_NUM_OF_WISH_DETAILS_PER_WISH; dn++)
		{
			if((wishdetailID = wish.getWishDetailID(dn)) > -1)
			{
				name = wdDB.getWishDetail(wishdetailID).getWishDetailName();
				choices = wdDB.getWishDetail(wishdetailID).getWishDetailChoiceString();
			}
			else
			{
				name="";
				choices="";
			}
			
			titleTFAL.get(dn).setText(name);
			contentTFAL.get(dn).setText(choices);
		}
	}
	
	boolean hasWishDetailChanged() { return bWishChanged; }

	/***************************************************************************************************
	 * Tests to see if a change has occurred to the wish. If so, updates the wish. Examines each
	 * wish field against the dialog box for changes.  
	 * @param w - ONCWish object
	 * @return - true if a change was detected, false if wish is unchanged
	 **************************************************************************************************/
	boolean getWishDetailChanges()
	{	
		bWishChanged = false;
		
		if(!w.getName().equals(wishnameTF.getText()))	//Wish name change?
		{
			w.setName(wishnameTF.getText().trim());
			bWishChanged = true;
		}
		
		int wdID;
			
		//Examine Wish Detail Blocks for changes	
		for(int wdn=0; wdn < MAX_NUM_OF_WISH_DETAILS_PER_WISH; wdn++)
		{
			//Wish Detail is currently empty and Text Fields are not => add new wish detail
			if((wdID = w.getWishDetailID(wdn)) == -1 && !titleTFAL.get(wdn).getText().isEmpty())
			{
				StringBuffer buf = new StringBuffer("");
				for(String s:convertWishStringtoList(contentTFAL.get(wdn).getText()))
					buf.append(s + ";");
				
				WishDetail reqWishDetail = new WishDetail(-1, titleTFAL.get(wdn).getText(), 
															buf.toString());
				
				String response = wdDB.add(this, reqWishDetail);
				
				if(response.startsWith("ADDED_WISH_DETAIL"))
				{
					Gson gson = new Gson();
					WishDetail addedWishDetail = gson.fromJson(response.substring(17), WishDetail.class);
					w.setWishDetailID(wdn, addedWishDetail.getID());
					bWishChanged = true;
				}
				else
				{
					GlobalVariables gvs = GlobalVariables.getInstance();
					String err_mssg = "ONC Server denied add wish detail request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Add Wish Detail Request Failure",
													JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}		
			}
			
			//Wish Detail is currently not empty and Text Fields are empty => Remove Wish Detail
			//To do this, move all other wish descriptions up and clear the final wish description
			if((wdID = w.getWishDetailID(wdn)) > -1 && titleTFAL.get(wdn).getText().isEmpty())
			{
				//Clear the content field
				contentTFAL.get(wdn).setText("");
				
				//Delete the wish detail from the server
				WishDetail delreqWD = wdDB.getWishDetail(w.getWishDetailID(wdn));
				String response = wdDB.delete(this, delreqWD);

				if(response.startsWith("DELETED_WISH_DETAIL"))
				{
					//Move the remaining wish descriptions and text fields up
					for(int i=wdn; i < MAX_NUM_OF_WISH_DETAILS_PER_WISH-1; i++)
					{
						w.setWishDetailID(i, w.getWishDetailID(i+1));
						titleTFAL.get(i).setText(titleTFAL.get(i+1).getText());
						contentTFAL.get(i).setText(contentTFAL.get(i+1).getText());
					}
				
					w.setWishDetailID(MAX_NUM_OF_WISH_DETAILS_PER_WISH-1, -1);
					titleTFAL.get(MAX_NUM_OF_WISH_DETAILS_PER_WISH-1).setText("");
					contentTFAL.get(MAX_NUM_OF_WISH_DETAILS_PER_WISH-1).setText("");
				
					bWishChanged = true;
				}
				else
				{
					GlobalVariables gvs = GlobalVariables.getInstance();
					String err_mssg = "ONC Server denied delete wish detail request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Delete Wish Detail Request Failure",
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}		
			}
			
			//Wish Detail is not empty and Text Fields are not empty, but either the names or choices
			//are different => Update Name and Contents
			if((wdID = w.getWishDetailID(wdn)) > -1 && !titleTFAL.get(wdn).getText().isEmpty() &&
					 (!wdDB.getWishDetail(wdID).getWishDetailName().equals(titleTFAL.get(wdn).getText()) ||
					   !wdDB.getWishDetail(wdID).getWishDetailChoiceString().equals(contentTFAL.get(wdn).getText())))
			{
				wdDB.getWishDetail(wdID).setDetailName(titleTFAL.get(wdn).getText());
				wdDB.getWishDetail(wdID).setDetailChoices(convertWishStringtoList(contentTFAL.get(wdn).getText()));
				bWishChanged = true;
				
				WishDetail updateWishDetail = new WishDetail(w.getWishDetailID(wdn),
															  titleTFAL.get(wdn).getText(), 
															   contentTFAL.get(wdn).getText());

				String response = wdDB.update(this, updateWishDetail);

				if(response.startsWith("UPDATED_WISH_DETAIL"))
				{
					bWishChanged = true;
				}
				else
				{
					GlobalVariables gvs = GlobalVariables.getInstance();
					String err_mssg = "ONC Server denied update wish detail request, try again later";
					JOptionPane.showMessageDialog(this, err_mssg, "Update Wish Detail Request Failure",
							JOptionPane.ERROR_MESSAGE, gvs.getImageIcon(0));
				}		
			}
		}
		
		return bWishChanged;	
	}

	String convertWishListtoString(String[] wishlist)
	{
		StringBuffer content = new StringBuffer("");
		for(int i=0; i < wishlist.length-1; i++)
			content.append(wishlist[i] + "; ");
		content.append(wishlist[wishlist.length-1]);
		
		return wishlist.toString();
	}
	
	String[] convertWishStringtoList(String wishstring)
	{
		return wishstring.split(";");
	}
	
	boolean autoaddContent(String keyword, JTextField targetTF)
	{
		boolean bChangesmade = false;
		if(keyword.equalsIgnoreCase("Color"))
		{
			targetTF.setText("?; black; blue; gold;  green; pink; purple; red; yellow; white;");
			bChangesmade = true;
		}
		else if(keyword.equalsIgnoreCase("Size"))
		{
			targetTF.setText("YS; YM; YL; S; M; L; XL; XXL; 2; 4; 6; 8; 10; 12; 14; 16;");
			bChangesmade=true;
		}
		else if(keyword.isEmpty())
		{
			targetTF.setText("");
			bChangesmade = true;
		}
		return bChangesmade;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == btnCancel)
		{
			this.setVisible(false);
			dispose();
		}
		else if(e.getSource() == btnSave)
		{
			getWishDetailChanges();
			this.setVisible(false);
			dispose();
		}
		else if(e.getSource() == titleTFAL.get(0))
		{
			autoaddContent(titleTFAL.get(0).getText(), contentTFAL.get(0));
		}
		else if(e.getSource() == titleTFAL.get(1))
		{
			autoaddContent(titleTFAL.get(1).getText(), contentTFAL.get(1));
		}
		else if(e.getSource() == titleTFAL.get(2))
		{
			autoaddContent(titleTFAL.get(2).getText(), contentTFAL.get(2));
		}
		else if(e.getSource() == titleTFAL.get(3))
		{
			autoaddContent(titleTFAL.get(3).getText(), contentTFAL.get(3));
		}
	}
}