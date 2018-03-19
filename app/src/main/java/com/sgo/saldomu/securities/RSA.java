package com.sgo.saldomu.securities;

import android.util.Base64;

import com.sgo.saldomu.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Lenovo on 12/03/2018.
 */

public class RSA {

    public static String opensslEncrypt(String data, String strKey, String strIv) {

        String encryptedValue   = "";

        try {
            Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(strIv.getBytes(), 0, ciper.getBlockSize());

            // Encrypt
            ciper.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedCiperBytes = ciper.doFinal(data.getBytes());

            //String s = new String(encryptedCiperBytes);
            encryptedValue = Base64.encodeToString(encryptedCiperBytes, Base64.DEFAULT);
            encryptedValue  = encryptedValue.trim();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return encryptedValue;
    }

    private PublicKey publicKey;

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String readTxt(InputStream inputStream){


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }

    /*public byte[] readFileBytes(String filename) throws IOException
    {
        Path path = Paths.get(filename);
        return Files.readAllBytes(path);
    }

    public PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    public PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(readFileBytes(filename));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }*/

    public String encryptData(String txt)
    {
        String encoded      = "";
        byte[] encrypted    = null;
        try {
            byte[] publicBytes          = Base64.decode(this.publicKey.toString(), Base64.DEFAULT);
            X509EncodedKeySpec keySpec  = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory       = KeyFactory.getInstance("RSA");
            PublicKey pubKey            = keyFactory.generatePublic(keySpec);
            Cipher cipher               = Cipher.getInstance("RSA/ECB/PKCS7Padding"); //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted                   = cipher.doFinal(txt.getBytes());
            encoded                     = Base64.encodeToString(encrypted, Base64.DEFAULT);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }
}
