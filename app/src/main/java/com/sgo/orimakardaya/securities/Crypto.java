package com.sgo.orimakardaya.securities;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {

    private static final String TAG = Crypto.class.getSimpleName();

    private static String DELIMITER = "]";

    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static int KEY_LENGTH = 256;

    private static SecureRandom random = new SecureRandom();

    private Crypto() {
    }

    @SuppressLint("DefaultLocale")
    public static void listAlgorithms(String algFilter) {
        Provider[] providers = Security.getProviders();
        for (Provider p : providers) {
            String providerStr = String.format("%s/%s/%f\n", p.getName(),
                    p.getInfo(), p.getVersion());
            Log.d(TAG, providerStr);
            Set<Service> services = p.getServices();
            List<String> algs = new ArrayList<String>();
            for (Service s : services) {
                boolean match = true;
                if (algFilter != null) {
                    match = s.getAlgorithm().toLowerCase()
                            .contains(algFilter.toLowerCase());
                }

                if (match) {
                    String algStr = String.format("\t%s/%s/%s", s.getType(),
                            s.getAlgorithm(), s.getClassName());
                    algs.add(algStr);
                }
            }

            Collections.sort(algs);
            for (String alg : algs) {
                Log.d(TAG, "\t" + alg);
            }
            Log.d(TAG, "");
        }
    }

    public static SecretKey generateAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(KEY_LENGTH);
            SecretKey key = kg.generateKey();

            return key;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);

        return b;
    }

    public static String encryptAesCbc(String plaintext, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            byte[] iv = generateIv(cipher.getBlockSize());
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: "
                    + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            return String.format("%s%s%s", toBase64(iv), DELIMITER,
                    toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }





    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    public static String decryptAesCbc(String ciphertext, SecretKey key) {
        try {
            String[] fields = ciphertext.split(DELIMITER);
            if (fields.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid encypted text format");
            }

            byte[] iv = fromBase64(fields[0]);
            byte[] cipherBytes = fromBase64(fields[1]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
            byte[] plaintext = cipher.doFinal(cipherBytes);
            String plainrStr = new String(plaintext, "UTF-8");

            return plainrStr;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPublicKey createPublicKey(byte[] pubKeyBytes)
            throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpec);

        return pubKey;
    }



    // PKCS#1 padding
    public static byte[] padPkcs1(byte[] in, int keySize) {
        if (in.length > keySize) {
            throw new IllegalArgumentException("Data too long");
        }
        byte[] result = new byte[keySize / 8];

        result[0] = 0x0;
        result[1] = 0x01; // BT 1

        // PS
        for (int i = 2; i != result.length - in.length - 1; i++) {
            result[i] = (byte) 0xff;
        }

        // end of padding
        result[result.length - in.length - 1] = 0x00;
        // D
        System.arraycopy(in, 0, result, result.length - in.length, in.length);

        return result;
    }





    public static RSAPublicKey loadRsaPublicKey(String keyAlias)
            throws GeneralSecurityException, IOException {
        return (RSAPublicKey) loadPublicKey(keyAlias);
    }

    public static PublicKey loadPublicKey(String keyAlias)
            throws GeneralSecurityException, IOException {
        java.security.KeyStore ks = java.security.KeyStore
                .getInstance("AndroidKeyStore");
        ks.load(null);
        java.security.KeyStore.Entry keyEntry = ks.getEntry(keyAlias, null);

        return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                .getCertificate().getPublicKey();
    }

    public static PrivateKey loadPrivateKey(String keyAlias)
            throws GeneralSecurityException, IOException {
        java.security.KeyStore ks = java.security.KeyStore
                .getInstance("AndroidKeyStore");
        ks.load(null);
        java.security.KeyStore.Entry keyEntry = ks.getEntry(keyAlias, null);

        return ((java.security.KeyStore.PrivateKeyEntry) keyEntry)
                .getPrivateKey();
    }

}
