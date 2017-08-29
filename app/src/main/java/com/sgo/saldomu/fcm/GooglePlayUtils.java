package com.sgo.saldomu.fcm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by yuddistirakiki on 8/15/17.
 */

public class GooglePlayUtils {

    public static boolean isGooglePlayServicesAvailable(Context mContext) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        return status == ConnectionResult.SUCCESS;
    }
}
