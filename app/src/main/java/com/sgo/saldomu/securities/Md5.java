package com.sgo.saldomu.securities;/*
  Created by Administrator on 11/10/2014.
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
    private static String hashMd5(byte[] p){
        try {
            MessageDigest digest;
            digest = MessageDigest.getInstance("MD5");
            digest.update(p);
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String hashMd5(String p){
       return hashMd5(p.getBytes());
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