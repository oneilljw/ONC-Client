package ourneighborschild;

public class ComboItem implements CanEnable
{
	Object object;  
    boolean isEnable;  

    ComboItem(Object obj, boolean isEnable)
    {  
        this.object = obj;  
        this.isEnable = isEnable;  
    }  

    ComboItem(Object obj)
    {  
        this(obj, true);  
    }  

    public boolean isEnabled()
    {  
        return isEnable;  
    }  

    public void setEnabled(boolean isEnable)
    {  
        this.isEnable = isEnable;  
    }  

    public String toString()
    {  
        return object.toString();  
    }
    
    public Object getComboItem()
    {
    	return object;
    }
}  

interface CanEnable
{  	  
	public void setEnabled(boolean isEnable);  
	  
	public boolean isEnabled();  
}  