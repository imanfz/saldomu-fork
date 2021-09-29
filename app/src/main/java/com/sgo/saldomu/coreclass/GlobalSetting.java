package com.sgo.saldomu.coreclass;


import android.content.Context;
import android.provider.Settings;

/**
 * Created by Lenovo on 10/07/2017.
 */

public class GlobalSetting {
    public static final int RC_LOCATION_PERM = 500;

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
}
