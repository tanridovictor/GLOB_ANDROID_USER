package com.enseval.gcmuser.API;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**Class untuk enkripsi query*/
public class QueryEncryption {

    public static String Decrypt(String text) throws Exception {
        String key = "gcm-e-commerce19";
        String iv = "19gcm-e-commerce";

        Cipher cipher = Cipher.getInstance
                ("AES/CBC/PKCS5Padding"); //this parameters should not be changed
        byte[] keyBytes = new byte[16];
        byte[] ivBytes = iv.getBytes();
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] results = new byte[text.length()];
//		BASE64Decoder decoder = new BASE64Decoder();
        try {
//			results = cipher.doFinal(decoder.decodeBuffer(text));

            results = cipher.doFinal(Base64.decode(text, Base64.DEFAULT));

        } catch (Exception e) {
            Log.d("Erron in Decryption", e.toString());
        }
        Log.d("Data", new String(results, "UTF-8"));
        return new String(results, "UTF-8"); // it returns the result as a String
    }

    public static String Encrypt(String text) throws Exception {
        String key = "gcm-e-commerce19";
        String iv = "19gcm-e-commerce";

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] keyBytes = new byte[16];
        byte[] ivBytes = iv.getBytes();
        byte[] b = key.getBytes("UTF-8");

        int len = b.length;
        if (len > keyBytes.length)
            len = keyBytes.length;
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
//		BASE64Encoder encoder = new BASE64Encoder();
//		return encoder.encode(results); // it returns the result as a String

        return new String(Base64.encodeToString(results, Base64.NO_WRAP));
    }
}
