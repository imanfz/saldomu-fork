package com.sgo.hpku.securities;

import com.sgo.hpku.coreclass.MyApiClient;

import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by yuddistirakiki on 1/13/16.
 */
public class AES {

    private String padding = "ISO10126Padding"; //"ISO10126Padding", "PKCS5Padding"

    private byte[] iv;
    private byte[] key;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    private AES(byte[] key, byte[] iv) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        this.key = key;
        this.iv = iv;

        initEncryptor();
//        initDecryptor();
    }

    private void initEncryptor() throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException
    {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        //encryptCipher = Cipher.getInstance("AES/ECB/"+padding);
        encryptCipher = Cipher.getInstance("AES/CBC/" + padding);
        encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
    }

    private  void initDecryptor() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        decryptCipher = Cipher.getInstance("AES/CBC/" + padding);
        decryptCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
    }

    private byte[] encrypt(byte[] dataBytes) throws IOException {
        ByteArrayInputStream bIn =
                new ByteArrayInputStream(dataBytes);
        @SuppressWarnings("resource")
        CipherInputStream cIn =
                new CipherInputStream(bIn, encryptCipher);
        ByteArrayOutputStream bOut =
                new ByteArrayOutputStream();
        int ch;
        while ((ch = cIn.read()) >= 0) {
            bOut.write(ch);
        }
        return bOut.toByteArray();
    }

    public byte[] decrypt(byte[] dataBytes) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        CipherOutputStream cOut =
                new CipherOutputStream(bOut, decryptCipher);
        cOut.write(dataBytes);
        cOut.close();
        return bOut.toByteArray();
    }

    private static String encryptAESString(String userId, String message){

        byte[] MesageBytes = message.getBytes();


        String dkey = userId +"_=_"+ MyApiClient.COMM_ID + "_=_" + MyApiClient.APP_ID +
                "_=_" + "app590";

        byte[] KeyBytes = dkey.getBytes();
        byte[] IvBytes = "5904pp3MoN3y".getBytes();

        AES aesHelper;
        try {
            aesHelper = new AES(KeyBytes, IvBytes);
            byte[] encryptedMsg = aesHelper.encrypt(MesageBytes);
            return new String(encryptedMsg);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKeySpec generateMySQLAESKey(String key, String encoding) {
        try {
            byte[] finalKey = new byte[16];
            int i = 0;
            for(byte b : key.getBytes(encoding))
                finalKey[i++%16] ^= b;
            return new SecretKeySpec(finalKey, "AES");
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String aes_encrypt(String password, String userId) throws IllegalBlockSizeException {
        try {

            String dkey = userId +"_=_"+ MyApiClient.COMM_ID + "_=_" + MyApiClient.APP_ID +
                    "_=_" + "app590";
//            Timber.d(dkey);
            dkey = Md5.hashMd5(dkey);
//            Timber.d(dkey);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, generateMySQLAESKey(dkey,"UTF-8"));

//            Timber.d(password);
            byte[] cleartext = password.getBytes("UTF-8");
            byte[] ciphertextBytes = cipher.doFinal(cleartext);
            String _aes = new String(Hex.encodeHex(ciphertextBytes));
//            Timber.d("cipher : "+_aes);

            return _aes;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
