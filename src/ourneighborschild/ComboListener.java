package ourneighborschild;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;

public class ComboListener implements ActionListener
{
	JComboBox jcombo;  
    Object curItem; 
	
	ComboListener(JComboBox combobox)
    {  
        this.jcombo = combobox;  
        combobox.setSelectedIndex(0);  
        curItem = combobox.getSelectedItem();  
    }  

	public void actionPerformed(ActionEvent e)
	{
		Object tempItem = jcombo.getSelectedItem();
        
        if (!((CanEnable) tempItem).isEnabled())
        	jcombo.setSelectedItem(curItem);  
        else
            curItem = tempItem;   
	}	
}
