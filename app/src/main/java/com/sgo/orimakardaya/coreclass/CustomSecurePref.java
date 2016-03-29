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


    public void ClearAllCustomData(){
        SecurePreferences.Editor mEdit = getInstance().getmSecurePrefs().edit();

        mEdit.remove(DefineValue.CONTACT_FIRST_TIME);
        mEdit.remove(DefineValue.BALANCE);
        mEdit.remove(DefineValue.USERID_PHONE);
        mEdit.remove(DefineValue.USER_NAME);
        mEdit.remove(DefineValue.CUST_ID);
        mEdit.remove(DefineValue.CUST_NAME);

        mEdit.remove(DefineValue.PROFILE_DOB);
        mEdit.remove(DefineValue.PROFILE_ADDRESS);
        mEdit.remove(DefineValue.PROFILE_BIO);
        mEdit.remove(DefineValue.PROFILE_COUNTRY);
        mEdit.remove(DefineValue.PROFILE_EMAIL);
        mEdit.remove(DefineValue.PROFILE_FULL_NAME);
        mEdit.remove(DefineValue.PROFILE_SOCIAL_ID);
        mEdit.remove(DefineValue.PROFILE_HOBBY);
        mEdit.remove(DefineValue.PROFILE_POB);
        mEdit.remove(DefineValue.PROFILE_GENDER);
        mEdit.remove(DefineValue.PROFILE_ID_TYPE);
        mEdit.remove(DefineValue.PROFILE_VERIFIED);
        mEdit.remove(DefineValue.PROFILE_BOM);

        mEdit.remove(DefineValue.LIST_ID_TYPES);
        mEdit.remove(DefineValue.LIST_CONTACT_CENTER);
        mEdit.remove(DefineValue.IS_CHANGED_PASS);
        mEdit.remove(DefineValue.IMG_URL);
        mEdit.remove(DefineValue.IMG_SMALL_URL);
        mEdit.remove(DefineValue.IMG_MEDIUM_URL);
        mEdit.remove(DefineValue.IMG_LARGE_URL);
        mEdit.remove(DefineValue.ACCESS_KEY);
        mEdit.remove(DefineValue.ACCESS_SECRET);

        mEdit.remove(DefineValue.IS_REGISTERED_LEVEL);
        mEdit.remove(DefineValue.COMMUNITY_LENGTH);
        mEdit.remove(DefineValue.COMMUNITY_ID);
        mEdit.remove(DefineValue.CALLBACK_URL_TOPUP);
        mEdit.remove(DefineValue.API_KEY_TOPUP);
        mEdit.remove(DefineValue.COMMUNITY_CODE);
        mEdit.remove(DefineValue.COMMUNITY_NAME);
        mEdit.remove(DefineValue.BUSS_SCHEME_CODE);

        mEdit.remove(DefineValue.AUTHENTICATION_TYPE);
        mEdit.remove(DefineValue.LENGTH_AUTH);
        mEdit.remove(DefineValue.IS_HAVE_PIN);
        mEdit.remove(DefineValue.LEVEL_VALUE);
        mEdit.remove(DefineValue.ALLOW_MEMBER_LEVEL);
        mEdit.remove(DefineValue.MAX_MEMBER_TRANS);
        mEdit.remove(DefineValue.MEMBER_DAP);

        mEdit.apply();

    }
}
