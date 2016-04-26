package com.sgo.orimakardaya.coreclass;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

/**
 * Created by yuddistirakiki on 3/3/16.
 */
public class DeviceUtils {
    private Context mContext;

    public DeviceUtils(Context _context){
        mContext = _context;
    }

    public String getWifiMcAddress(){
        WifiManager wifiManager = (WifiManager) getmContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getDeviceModelID(){
        return Build.MODEL;
    }
    public String getAndroidID(){
        return Settings.Secure.getString(getmContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }
}
