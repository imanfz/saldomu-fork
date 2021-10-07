package com.sgo.saldomu.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.sgo.saldomu.coreclass.CustomSecurePref;
import com.sgo.saldomu.coreclass.DefineValue;
import com.sgo.saldomu.coreclass.WebParams;

import java.util.Locale;

import timber.log.Timber;

public class LocaleManager {
    public static Context setLocale(Context mContext) {
        return updateResources(mContext, getLanguagePref());
    }

    /**
     * Set new Locale with context
     * @param mContext
     * @param mLocaleKey
     * @return
     */
    public static Context setNewLocale(Context mContext, String mLocaleKey) {
        setLanguagePref(mLocaleKey);
        return updateResources(mContext, mLocaleKey);
    }

    /**
     * Get saved Locale from SharedPreferences
     * @return current locale key by default return english locale
     */
    public static String getLanguagePref() {
        String language = "";

        boolean isBahasa = CustomSecurePref.getInstance().getBoolean(DefineValue.IS_BAHASA);

        Timber.d("Locale Manager IS BAHASA ===== %s", isBahasa);

        language = DefineValue.LANGUAGE_CODE_IND;

        return language;
    }

    //
//    /**
//     *  set pref key
//     * @param mContext
//     * @param localeKey
//     */
    private static void setLanguagePref(String localeKey) {

        boolean isBahasa;

//        if(localeKey.equals(DefineValue.LANGUAGE_CODE_ENG)){
//            isBahasa = false;
//        }else{
            isBahasa = true;
//        }
        Timber.d("SET LANGUAGE PREFERENCE : %s", localeKey);

        CustomSecurePref.getInstance().setBoolean(DefineValue.IS_BAHASA, isBahasa);
    }

    /**
     * update resource
     * @param context
     * @param language
     * @return
     */
    private static Context updateResources(Context context, String language) {

        Timber.d("UPDATE RESOURCE LANGUAGE : %s", language);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }
}
