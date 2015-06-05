package OurNeighborsChild;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class ONCEncryptor
{
	private static String key = "ONC12/25ONC12/25"; // 128 bit key
	private static String secretKey = "XMzDdG4D03CKm2IxIWQw7g==";

/*
	public static String encrypt(String text)
	{
		byte[] encrypted = null;
		
		try
		{
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			// encrypt the text
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			encrypted = cipher.doFinal(text.getBytes());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return new String(encrypted);
	}
	
	public static String decrypt(String encryptedText)
	{
		byte[] encryptedBytes = encryptedText.getBytes();
		String decrypted = null;
		
		try
		{
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			// encrypt the text
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			decrypted = new String(cipher.doFinal(encryptedBytes));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return decrypted;
	}
*/	
	public static String encrypt(String text)
	{
        byte[] raw;
        String encryptedString;
        SecretKeySpec skeySpec;
        byte[] encryptText = text.getBytes();
        Cipher cipher;
        try {
            raw = Base64.decodeBase64(secretKey);
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
            raw = Base64.decodeBase64(secretKey);
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
