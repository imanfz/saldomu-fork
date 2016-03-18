package com.sgo.mdevcash.coreclass;/*
  Created by Administrator on 11/12/2015.
 */

import android.app.Activity;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class GcmManager {

    public static boolean checkPlayServices(Activity mContext) {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    int resultCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (apiAvailability.isUserResolvableError(resultCode)) {
        apiAvailability.getErrorDialog(mContext, resultCode, DefineValue.PLAY_SERVICES_RESOLUTION_REQUEST)
                .show();
      } else {
        Log.i("GoogleApi Availability", "This device is not supported.");
        mContext.finish();
      }
      return false;
    }
    return true;
  }
}
