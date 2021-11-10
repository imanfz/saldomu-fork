package com.sgo.saldomu.coreclass;/*
  Created by Administrator on 11/6/2015.
 */

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class CustomEncryptedSharedPreferences {

    private static CustomEncryptedSharedPreferences singleton = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private CustomEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            sharedPreferences = EncryptedSharedPreferences.create(context, DefineValue.SEC_PREF_NAME, masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            editor = sharedPreferences.edit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CustomEncryptedSharedPreferences getInstance() {
        return singleton;
    }

    public static CustomEncryptedSharedPreferences initialize(Context _context) {
        if (singleton == null) {
            singleton = new CustomEncryptedSharedPreferences(_context);
        }
        return singleton;
    }

    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        String str = sharedPreferences.getString(key, null);
        if (str == null)
            return defaultValue;
        else
            return str;
    }
}
