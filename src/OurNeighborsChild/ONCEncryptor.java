package OurNeighborsChild;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ONCEncryptor
{
	private static String key = "ONC12/25ONC12/25"; // 128 bit key
	
	public static String encrypt(String text)
	{
		byte[] encrypted = null;
		
		try
		{
			Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES");

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
			Cipher cipher = Cipher.getInstance("AES");

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
}
