package com.sgo.orimakardaya.coreclass;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
  Created by Administrator on 9/15/2014.
 */
public class InetHandler {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

}
