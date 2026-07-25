package com.q4magic.util;


import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DecryptString {
    public static String md5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        return String.format("%032x", number);
    }

    public static String setEncDecUser(String data, String act, String flag) {
        String secretKey = "]d=3T[N}+:}YiA;cv418j*dCs";
        String initialVectorString = "9eQV5B41wgCyqQb9";
        if(data != null) {
            try {
                if(flag.equals("")) {
                    return data.replaceAll("$","");
                } else {
                    SecretKeySpec skeySpec = new SecretKeySpec(md5(secretKey).getBytes(), "AES");
                    Base64 obj64 = new org.apache.commons.codec.binary.Base64();
                    IvParameterSpec initialVector = new IvParameterSpec(initialVectorString.getBytes());
                    Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");

                    if(act == "display") {
                        cipher.init(Cipher.DECRYPT_MODE, skeySpec, initialVector);

                        // New Code Start
                        //data = URLDecoder.decode(data, "UTF-8");
                        // New Code End

                        byte[] encryptedByteArray = obj64.decode(data.getBytes());
                        byte[] decryptedByteArray = cipher.doFinal(encryptedByteArray);
                        data = new String(decryptedByteArray, "UTF8");
                    } else {
                        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, initialVector);
                        byte[] decryptedByteArray = cipher.doFinal(data.getBytes());
                        byte[] encryptedByteArray = obj64.encode(decryptedByteArray);
                        data = new String(encryptedByteArray, "UTF8");

                        // New Code Start
                        //data = URLEncoder.encode(data, "UTF-8");
                        // New Code End
                    }
                }
            } catch (Exception e) {
                log.error("setEncDecUser error"+ e);
            }
        }
        return data;
    }

    public static String decrypt(String encryptedData, String initialVectorString, String secretKey) {
        log.debug("inside decrypt method");
        log.debug("initialVectorString",initialVectorString);
        log.debug("secretKey",secretKey);
        String decryptedData = null;
        try
        {
            SecretKeySpec skeySpec = new SecretKeySpec(md5(secretKey).getBytes(), "AES");
            Base64 obj64 = new org.apache.commons.codec.binary.Base64();
            IvParameterSpec initialVector = new IvParameterSpec(initialVectorString.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, initialVector);
            byte[] encryptedByteArray = obj64.decode(encryptedData.getBytes());
            byte[] decryptedByteArray = cipher.doFinal(encryptedByteArray);
            decryptedData = new String(decryptedByteArray, "UTF8");
        }
        catch (Exception e)
        {
            System.out.println(e);
            log.error("DecryptString class called "+ e);
        }
        return decryptedData;
    }
}
