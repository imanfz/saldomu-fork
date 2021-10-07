package com.sgo.saldomu.securities;

import android.util.Base64;

import com.sgo.saldomu.BuildConfig;
import com.sgo.saldomu.coreclass.Singleton.MyApiClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * Created by Lenovo on 12/03/2018.
 */

public class RSA {
    private static final int pswdIterations = 10;
    private static final int keySize = 128;
    private static final String cypherInstance = "AES/CBC/PKCS5Padding";
    private static final String secretKeyInstance = "PBKDF2WithHmacSHA1";
    private static final String plainText = "sampleText";
    private static final String AESSalt = "exampleSalt";
    private static final String initializationVector = "8119745113154120";
    public static String opensslEncrypt(String data) {

        String encryptedValue = "";
        String strKey = BuildConfig.OPENSSL_ENCRYPT_KEY;
        String strIv = BuildConfig.OPENSSL_ENCRYPT_IV;

        try {
            Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(strIv.getBytes(), 0, ciper.getBlockSize());

            // Encrypt
            ciper.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedCiperBytes = ciper.doFinal(data.getBytes());

            //String s = new String(encryptedCiperBytes);
            encryptedValue = Base64.encodeToString(encryptedCiperBytes, Base64.DEFAULT);
            encryptedValue = encryptedValue.trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedValue;
    }

    public static String opensslEncrypt(String uuid, String datetime, String userID, String data, String link) {

        String encryptedValue = "";
        String strKey = uuid +
                datetime +
                BuildConfig.APP_ID +
                link +
                MyApiClient.COMM_ID +
                userID;

        encryptedValue = encrypt(strKey, data);
        return encryptedValue;
    }

    public static String opensslEncryptLogin(String strKey, String data) {
        return encrypt(strKey, data);
    }

    public static String opensslEncryptCommID(String commID, String uuid, String datetime, String userID, String data, String link) {

        String encryptedValue = "";
        String strKey = uuid +
                datetime +
                BuildConfig.APP_ID +
                link +
                commID +
                userID;

        encryptedValue = encrypt(strKey, data);
        return encryptedValue;
    }

    public static String encrypt(String strKey, String data) {
        Timber.d("key: %s", strKey);
        strKey = Md5.hashMd5(strKey);
        Timber.d("md5 key: %s", strKey);
        Timber.d("data: %s", data);
        String strIv = BuildConfig.AES_ENCRYPT_IV;
        String encryptedValue = "";
        try {
            Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(strIv.getBytes(), 0, ciper.getBlockSize());

            // Encrypt
            ciper.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedCiperBytes = ciper.doFinal(data.getBytes(StandardCharsets.UTF_8));

            encryptedValue = Base64.encodeToString(encryptedCiperBytes, Base64.DEFAULT);
            encryptedValue = encryptedValue.trim();
            Timber.d("encrypt data: %s", encryptedValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedValue;
    }

    public static String encrypt2(String textToEncrypt) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(getRaw(plainText, AESSalt), "AES");
        Cipher cipher = Cipher.getInstance(cypherInstance);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
        byte[] encrypted = cipher.doFinal(textToEncrypt.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public static String decrypt2(String textToDecrypt) throws Exception {

        byte[] encryted_bytes = Base64.decode(textToDecrypt, Base64.DEFAULT);
        SecretKeySpec skeySpec = new SecretKeySpec(getRaw(plainText, AESSalt), "AES");
        Cipher cipher = Cipher.getInstance(cypherInstance);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(initializationVector.getBytes()));
        byte[] decrypted = cipher.doFinal(encryted_bytes);
        return new String(decrypted, "UTF-8");
    }

    private static byte[] getRaw(String plainText, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyInstance);
            KeySpec spec = new PBEKeySpec(plainText.toCharArray(), salt.getBytes(), pswdIterations, keySize);
            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String decrypt(String strKey, String data){

        String strIv = BuildConfig.AES_ENCRYPT_IV;
        String decryptedValue = "";
        try {
            Cipher ciper = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(strKey.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(strIv.getBytes());

            // Decrypt
            ciper.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] original = ciper.doFinal(Base64.decode(data,0));

            decryptedValue = new String(original);

            Timber.d("decrypt data: %s", decryptedValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedValue;
    }

    private PublicKey publicKey;

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String readTxt(InputStream inputStream) {


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
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

    public String encryptData(String txt) {
        String encoded = "";
        byte[] encrypted = null;
        try {
            byte[] publicBytes = Base64.decode(this.publicKey.toString(), Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS7Padding"); //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted = cipher.doFinal(txt.getBytes());
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }
}
