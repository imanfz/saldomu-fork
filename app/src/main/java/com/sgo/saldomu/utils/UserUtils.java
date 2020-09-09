package com.sgo.saldomu.utils;

import com.securepreferences.SecurePreferences;
import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;

/**
 * Created by yuddistirakiki on 1/11/18.
 */

public class UserUtils {

    private UserUtils(){}

    public static Boolean isLogin(){
        SecurePreferences sp = CustomSecurePref.getSecurePrefsInstance();
        String flagLogin = sp.getString(DefineValue.FLAG_LOGIN, DefineValue.STRING_NO);
        if(flagLogin == null)
            flagLogin = DefineValue.STRING_NO;
        return flagLogin.equals(DefineValue.STRING_YES);
    }
}
