package com.sgo.orimakardaya.coreclass;/*
  Created by Administrator on 11/6/2015.
 */

import android.content.Context;
import android.os.Build;

import com.securepreferences.SecurePreferences;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;

public class CustomSecurePref {

    private static CustomSecurePref singleton = null;
    private SecurePreferences mSecurePrefs;
    private Context mContext;

    public static CustomSecurePref getInstance( ) {
        return singleton;
    }

    public static CustomSecurePref initialize(Context _context) {
        if(singleton == null) {
            singleton = new CustomSecurePref(_context);
        }
        singleton.setmContext(_context);
        return singleton;
    }


    private CustomSecurePref(Context _context){
        if(getmSecurePrefs() ==null){
            try {
                String test = Build.ID;
                final byte[] salt = test.getBytes();
                AesCbcWithIntegrity.SecretKeys myKey = AesCbcWithIntegrity.generateKeyFromPassword(Build.SERIAL,salt,1000);
                setmSecurePrefs(new SecurePreferences(_context, myKey, DefineValue.SEC_PREF_NAME));
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public SecurePreferences getmSecurePrefs() {
        return mSecurePrefs;
    }

    public void setmSecurePrefs(SecurePreferences mSecurePrefs) {
        this.mSecurePrefs = mSecurePrefs;
    }
}
