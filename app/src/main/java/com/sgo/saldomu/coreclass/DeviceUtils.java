package com.sgo.saldomu.coreclass;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.securepreferences.SecurePreferences;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by yuddistirakiki on 3/3/16.
 */
public class DeviceUtils {

    public String getWifiMcAddress(){
        WifiManager wifiManager = (WifiManager) CoreApp.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        return wInfo.getMacAddress();
    }

    public String getDeviceModelID(){
        return Build.MODEL;
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidID(){
        return Settings.Secure.getString(CoreApp.getAppContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        manufacturer = manufacturer.substring(0, 1).toUpperCase() + manufacturer.substring(1);

        String model = Build.MODEL;
        model = model.substring(0, 1).toUpperCase() + model.substring(1);

        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceAPILevel() {
        return Build.VERSION.SDK_INT + "";
    }

    public static String getDeviceOS() {
        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                return fieldName;
            }
        }
        return "UNSPECIFIED";
    }

    public static double getLastLng() {
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        return Double.parseDouble(prefs.getString(DefineValue.LAST_CURRENT_LONGITUDE, "0"));
    }

    public static double getLastlat() {
        SecurePreferences prefs = CustomSecurePref.getInstance().getmSecurePrefs();
        return Double.parseDouble(prefs.getString(DefineValue.LAST_CURRENT_LATITUDE, "0"));
    }

    public static String getDeviceMemory() {
        ActivityManager actManager = (ActivityManager) CoreApp.getAppContext().getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long bytes = memInfo.totalMem;
        String lastValue;

        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "";
        lastValue =  String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);

        return lastValue;
    }

    public  String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getPinCode() {
        Context mContext = CoreApp.getAppContext();
        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> address;
        try {
            address = geoCoder.getFromLocation(getLastlat(), getLastLng(), 1);
            if (address.size() > 0) {
                String postCode = address.get(0).getPostalCode();

                if (postCode != null) {
                    return postCode;
                }
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return "";
    }

    public static String getDeviceTimeZone() {
        return TimeZone.getDefault().getID();
    }
}
