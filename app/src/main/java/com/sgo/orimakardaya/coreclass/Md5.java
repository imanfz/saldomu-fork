package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 11/10/2014.
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
    public static String hashMd5(String p) throws NoSuchAlgorithmException {

        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(p.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : messageDigest) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
    }

    /* USAGE :

    public static void main(String[] args) {
        String password = "123123";
        try {
            password = Md5.hashMd5(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println(password);
    }
    */
}