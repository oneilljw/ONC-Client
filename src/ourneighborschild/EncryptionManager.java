package ourneighborschild;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EncryptionManager
{
	private static EncryptionManager instance;
	private static Map<String, String> keyMap;
	
	private static String defaultKey = "XMzDdG4D03CKm2IxIWQw7g==";
	
	public static EncryptionManager getInstance()
	{
		if(instance == null)
			instance = new EncryptionManager();
		
		return instance;
	}
	
	private EncryptionManager()
	{
		keyMap = new HashMap<String, String>();
	}
	
	int importKeyMapFromServer()
	{
		ServerIF serverIF = ServerIF.getInstance();
		
		if(serverIF != null && serverIF.isConnected())
		{
			Gson gson = new Gson();
			Type mapType = new TypeToken<Map<String, String>>(){}.getType();
			
			String response = serverIF.sendRequest("GET<keys>");
			
			if(response != null && !response.equals("KEY_REQUEST_FAILED"))
				keyMap = gson.fromJson(response, mapType);	
		}
		
		//get Key Map from server
//		Set<String> keyset = keyMap.keySet();
//		for(String key:keyset)
//			System.out.println(String.format("/keyMap key=%s, value=%s", key, keyMap.get(key)));
		
		return keyMap.size();
	}
	
	//getters
	static String getKey(String key)
	{
		return keyMap == null ? null : keyMap.containsKey(key) ? keyMap.get(key) : null;
	}

	public static String encrypt(String text)
	{
        byte[] raw;
        String encryptedString;
        SecretKeySpec skeySpec;
        byte[] encryptText = text.getBytes();
        Cipher cipher;
        try {
          raw = Base64.decodeBase64(defaultKey);
//            raw = Base64.decodeBase64(getKey("key0") == null ? defaultKey : keyMap.get("key0"));
            skeySpec = new SecretKeySpec(raw, "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encryptedString = Base64.encodeBase64String(cipher.doFinal(encryptText));
        } 
        catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
        return encryptedString;
    }

    public static String decrypt(String text)
    {
        Cipher cipher;
        String encryptedString;
        byte[] encryptText = null;
        byte[] raw;
        SecretKeySpec skeySpec;
        try {
        	raw = Base64.decodeBase64(defaultKey);
//          raw = Base64.decodeBase64(getKey("key0") == null ? defaultKey : keyMap.get("key0"));
            skeySpec = new SecretKeySpec(raw, "AES");
            encryptText = Base64.decodeBase64(text);
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            encryptedString = new String(cipher.doFinal(encryptText));
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
        return encryptedString;
    }
}
