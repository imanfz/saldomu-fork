package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 11/6/2015.
 */

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.securepreferences.SecurePreferences;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.File;
import java.security.GeneralSecurityException;

import timber.log.Timber;

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

    private static String getDeviceId() {
        if(android.os.Build.SERIAL != null || !android.os.Build.SERIAL.isEmpty())
            return Settings.Secure.ANDROID_ID+android.os.Build.SERIAL;
        else
            return Settings.Secure.ANDROID_ID;
    }

    private CustomSecurePref(Context _context){
        if(getmSecurePrefs() ==null){

            try {
                isFilePrefExist(_context);
//                String test = Settings.Secure.ANDROID_ID;
                String test = getDeviceId();
                final byte[] salt = test.getBytes();
                AesCbcWithIntegrity.SecretKeys myKey = AesCbcWithIntegrity.generateKeyFromPassword(Build.SERIAL,salt,500);
                setmSecurePrefs(new SecurePreferences(_context, myKey, DefineValue.SEC_PREF_NAME));

                String i = getmSecurePrefs().getString(DefineValue.LEVEL_VALUE, "0");
                if(i == null) {
                    deleteData(_context);
                    setmSecurePrefs(null);
                    setmSecurePrefs(new SecurePreferences(_context, myKey, DefineValue.SEC_PREF_NAME));
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void isFilePrefExist(Context _context) {
        String outFileName = "pin_pref.xml.xml";
        File file = new File(_context.getApplicationInfo().dataDir+"/shared_prefs", outFileName);
        File fileOut = new File(_context.getApplicationInfo().dataDir+"/shared_prefs",DefineValue.SEC_PREF_NAME+".xml");
        if(file.exists()){
            Boolean check = file.renameTo(fileOut);
            if(check)
                Timber.d("Success Rename");
            else
                Timber.d("Failed Rename");
        }
        else
            Timber.d("gak ada");
    }

    private void deleteData(Context _context) {
        File fileOut = new File(_context.getApplicationInfo().dataDir+"/shared_prefs",DefineValue.SEC_PREF_NAME+".xml");
        if(fileOut.exists()){
            Boolean check = fileOut.delete();
            if(check)
                Timber.d("Success Delete");
            else
                Timber.d("Failed Delete");
        }
        else
            Timber.d("gak ada clearAllData");

    }

    private void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public SecurePreferences getmSecurePrefs() {
        return mSecurePrefs;
    }

    private void setmSecurePrefs(SecurePreferences mSecurePrefs) {
        this.mSecurePrefs = mSecurePrefs;
    }

    public void ClearAllCustomData(){
        SecurePreferences.Editor mEdit = getInstance().getmSecurePrefs().edit();

        mEdit.remove(DefineValue.CONTACT_FIRST_TIME);
        mEdit.remove(DefineValue.BALANCE_AMOUNT);
        mEdit.remove(DefineValue.BALANCE_CCYID);
        mEdit.putString(DefineValue.BALANCE_REMAIN_LIMIT,"0");
        mEdit.remove(DefineValue.BALANCE_PERIOD_LIMIT);
        mEdit.remove(DefineValue.BALANCE_NEXT_RESET);
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
        mEdit.remove(DefineValue.BANK_ATM_CODE);
        mEdit.remove(DefineValue.NO_VA);
        mEdit.remove(DefineValue.BANK_ATM_NAME);
        mEdit.remove(DefineValue.BANK_CASHOUT);
        mEdit.remove(DefineValue.MEMBER_CODE);
        mEdit.remove(DefineValue.MEMBER_DAP);
		mEdit.remove(DefineValue.MEMBER_ID);
        mEdit.remove(DefineValue.MEMBER_NAME);
        mEdit.remove(DefineValue.MAX_TOPUP);
        mEdit.remove(DefineValue.CONTACT_FIRST_TIME);
        mEdit.remove(DefineValue.TIMELINE_FIRST_TIME);

        mEdit.apply();

    }
}
